package asl.abdelouahed.views.activity;


import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import java.io.IOException;
import java.util.List;

import asl.abdelouahed.ICameraListener;
import asl.abdelouahed.R;
import asl.abdelouahed.models.Classifier;
import asl.abdelouahed.models.TensorFlowImageClassifier;
import asl.abdelouahed.utils.UtilsTranslate;
import butterknife.BindView;
import butterknife.ButterKnife;

import static asl.abdelouahed.utils.UtilsConstants.IMAGE_MEAN;
import static asl.abdelouahed.utils.UtilsConstants.IMAGE_STD;
import static asl.abdelouahed.utils.UtilsConstants.INPUT_NAME;
import static asl.abdelouahed.utils.UtilsConstants.INPUT_SIZE;
import static asl.abdelouahed.utils.UtilsConstants.LABEL_FILE;
import static asl.abdelouahed.utils.UtilsConstants.MODEL_FILE;
import static asl.abdelouahed.utils.UtilsConstants.OUTPUT_NAME;
import static asl.abdelouahed.utils.UtilsConstants.THRESHOLD;

public class HomeActivity extends BaseActivity implements ICameraListener {

    private static final String TAG = "TAG:HomeActivity";

    @BindView(R.id.txv_result)
    TextView txvResult;
    @BindView(R.id.sb_threshold)
    SeekBar sbThreshold;
    @BindView(R.id.img_rgba)
    ImageView imgRgb;
    @BindView(R.id.img_gray)
    ImageView imgGray;

    private Bitmap bRgba, bGray;
    private Classifier classifier;
    private List<Classifier.Recognition> results;


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
                txvResult.setText(res);
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
            Log.e(TAG, e.getMessage());
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
        imgRgb.setImageBitmap(bRgba);
        imgGray.setImageBitmap(bGray);
        runInBackground(runnableRecognition);
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


}
