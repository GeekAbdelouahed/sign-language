package asl.abdelouahed;


import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.v7.app.AppCompatActivity;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import java.io.IOException;
import java.util.List;

import asl.abdelouahed.models.Classifier;
import asl.abdelouahed.models.TensorFlowImageClassifier;
import asl.abdelouahed.utils.UtilsTranslate;
import butterknife.BindView;
import butterknife.ButterKnife;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

import static asl.abdelouahed.utils.UtilsConstants.IMAGE_MEAN;
import static asl.abdelouahed.utils.UtilsConstants.IMAGE_STD;
import static asl.abdelouahed.utils.UtilsConstants.INPUT_NAME;
import static asl.abdelouahed.utils.UtilsConstants.INPUT_SIZE;
import static asl.abdelouahed.utils.UtilsConstants.LABEL_FILE;
import static asl.abdelouahed.utils.UtilsConstants.MODEL_FILE;
import static asl.abdelouahed.utils.UtilsConstants.OUTPUT_NAME;
import static asl.abdelouahed.utils.UtilsConstants.THRESHOLD;

public class HomeActivity extends AppCompatActivity implements CameraListener {

    @BindView(R.id.tv_result)
    TextView tvResult;
    @BindView(R.id.sb_threshold)
    SeekBar sbThreshold;
    @BindView(R.id.iv_test_rgb_touch)
    ImageView ivTestRgb;
    @BindView(R.id.iv_test_gray_touch)
    ImageView ivTestGray;

    private Bitmap bRgba, bGray;
    private Classifier classifier;
    private List<Classifier.Recognition> results;
    private Handler handler;
    private HandlerThread handlerThread;

    private Runnable runnableRecognition = new Runnable() {
        @Override
        public void run() {
            results = classifier.recognizeImage(bGray);
            runOnUiThread(runnableResult);
        }
    };
    private Runnable runnableResult = new Runnable() {
        @Override
        public void run() {
            if (results.size() > 0) {
                Classifier.Recognition recognition = results.get(0);
                String res = UtilsTranslate.translate(recognition.getTitle());
                tvResult.setText(res);
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_home);
        ButterKnife.bind(this);
        try {
            classifier = TensorFlowImageClassifier.create(getAssets(),
                    MODEL_FILE,
                    LABEL_FILE,
                    INPUT_SIZE,
                    IMAGE_MEAN,
                    IMAGE_STD,
                    INPUT_NAME,
                    OUTPUT_NAME);
        } catch (IOException e) {
            e.printStackTrace();
        }

        sbThreshold.setProgress(THRESHOLD);
        sbThreshold.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                THRESHOLD = progress;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                THRESHOLD = seekBar.getProgress();
            }
        });
    }

    @Override
    public void onFrameChanged(Bitmap bRgba, Bitmap bGray) {
        this.bGray = bGray;
        this.bRgba = bRgba;
        ivTestRgb.setImageBitmap(bRgba);
        ivTestGray.setImageBitmap(bGray);
        runInBackground(runnableRecognition);
    }

    private synchronized void runInBackground(final Runnable r) {
        if (handler != null) {
            handler.post(r);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        handlerThread = new HandlerThread("inference");
        handlerThread.start();
        handler = new Handler(handlerThread.getLooper());
    }

    @Override
    protected void onPause() {
        super.onPause();
        handlerThread.quitSafely();
        try {
            handlerThread.join();
            handlerThread = null;
            handler = null;
        } catch (final InterruptedException e) {
        }
    }

    @Override
    protected void attachBaseContext(Context context) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(context));
    }
}
