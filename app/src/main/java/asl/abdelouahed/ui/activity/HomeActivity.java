package asl.abdelouahed.ui.activity;


import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.List;

import asl.abdelouahed.ICameraListener;
import asl.abdelouahed.R;
import asl.abdelouahed.models.Classifier;
import asl.abdelouahed.models.TensorFlowImageClassifier;
import asl.abdelouahed.utils.UtilsTranslate;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static asl.abdelouahed.utils.UtilsConstants.IMAGE_MEAN;
import static asl.abdelouahed.utils.UtilsConstants.IMAGE_STD;
import static asl.abdelouahed.utils.UtilsConstants.INPUT_NAME;
import static asl.abdelouahed.utils.UtilsConstants.INPUT_SIZE;
import static asl.abdelouahed.utils.UtilsConstants.LABEL_FILE;
import static asl.abdelouahed.utils.UtilsConstants.MIN_CONFIDENCE;
import static asl.abdelouahed.utils.UtilsConstants.MODEL_FILE;
import static asl.abdelouahed.utils.UtilsConstants.OUTPUT_NAME;

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

    private List<Classifier.Recognition> results;
    private Classifier classifier;
    private Bitmap bRgba, bGray;
    private String targetWord = "";
    private String ch = "";
    private int threshold = 150;
    private int count = 0;

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
            if (!results.isEmpty()) {
                Classifier.Recognition recognition = results.get(0);
                String res = (recognition.getConfidence() >= MIN_CONFIDENCE) ? UtilsTranslate.translate(recognition.getTitle()) : "";
                if (res.equals(ch) || ch.isEmpty()) {
                    count++;
                    ch = res;
                } else {
                    ch = res;
                    count = 1;
                }
                if (count == 3) {
                    ch = "";
                    count = 0;
                }
                targetWord += res;
                txvResult.setText(targetWord);
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

        classifier = TensorFlowImageClassifier.create(getAssets(),
                MODEL_FILE,
                LABEL_FILE,
                INPUT_SIZE,
                IMAGE_MEAN,
                IMAGE_STD,
                INPUT_NAME,
                OUTPUT_NAME);

        sbThreshold.setProgress(threshold);
        sbThreshold.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                threshold = progress;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                threshold = seekBar.getProgress();
            }
        });
    }

    @OnClick(R.id.img_clear)
    public void onClickClear() {
        count = 0;
        ch = "";
        targetWord = "";
        txvResult.setText(targetWord);
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
    public int onGetThreshold() {
        return threshold;
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
