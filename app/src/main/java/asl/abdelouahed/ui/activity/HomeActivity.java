package asl.abdelouahed.ui.activity;


import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.github.ybq.android.spinkit.SpinKitView;
import com.github.ybq.android.spinkit.sprite.Sprite;

import java.util.List;

import asl.abdelouahed.ICameraListener;
import asl.abdelouahed.R;
import asl.abdelouahed.model.Classifier;
import asl.abdelouahed.model.TensorFlowImageClassifier;
import asl.abdelouahed.utils.UtilsTranslate;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnLongClick;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

import static asl.abdelouahed.utils.UtilsConstants.DELAY_RECOGNITION;
import static asl.abdelouahed.utils.UtilsConstants.IMAGE_MEAN;
import static asl.abdelouahed.utils.UtilsConstants.IMAGE_STD;
import static asl.abdelouahed.utils.UtilsConstants.INPUT_NAME;
import static asl.abdelouahed.utils.UtilsConstants.INPUT_SIZE;
import static asl.abdelouahed.utils.UtilsConstants.LABEL_FILE;
import static asl.abdelouahed.utils.UtilsConstants.MIN_CONFIDENCE;
import static asl.abdelouahed.utils.UtilsConstants.MODEL_FILE;
import static asl.abdelouahed.utils.UtilsConstants.OUTPUT_NAME;

public class HomeActivity extends AppCompatActivity implements ICameraListener {

    private static final String TAG = "TAG:HomeActivity";

    @BindView(R.id.txv_result)
    TextView resultText;
    @BindView(R.id.sb_threshold)
    SeekBar thresholdSeekBar;
    @BindView(R.id.img_rgba)
    ImageView rgbaImage;
    @BindView(R.id.img_gray)
    ImageView grayImage;
    @BindView(R.id.spin_kit)
    SpinKitView spinKitView;

    private HandlerThread handlerThread;
    private Handler handler;
    private List<Classifier.Recognition> resultsRecognition;
    private Classifier classifier;
    private Bitmap rgbaBitmap, grayBitmap;
    private String resultWord = "";
    private int threshold = 150;
    private boolean isOnRecognizeState = false;

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

        thresholdSeekBar.setProgress(threshold);
        thresholdSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
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
        if (!resultWord.isEmpty()) {
            resultWord = resultWord.substring(0, resultWord.length() - 1);
            resultText.setText(resultWord);
        }
    }

    @OnLongClick(R.id.img_clear)
    public boolean onClickFullClear() {
        if (!resultWord.isEmpty()) {
            resultWord = "";
            resultText.setText(resultWord);
        }
        return false;
    }

    @Override
    public void onFrameChanged(Bitmap bRgba, Bitmap bGray) {
        this.grayBitmap = bGray;
        this.rgbaBitmap = bRgba;
        rgbaImage.setImageBitmap(bRgba);
        grayImage.setImageBitmap(bGray);
        runInBackground(recognitionRunnable);
    }

    private Runnable recognitionRunnable = new Runnable() {
        @Override
        public void run() {
            if (!isOnRecognizeState) {
                resultsRecognition = classifier.recognizeImage(grayBitmap);
                runOnUiThread(resultRunnable);
                isOnRecognizeState = true;
            }
        }
    };
    private Runnable resultRunnable = new Runnable() {
        @Override
        public void run() {
            if (!resultsRecognition.isEmpty()) {
                Classifier.Recognition recognition = resultsRecognition.get(0);
                String resultChar = (recognition.getConfidence() >= MIN_CONFIDENCE) ? UtilsTranslate.translate(recognition.getTitle()) : "";
                if (!resultWord.endsWith(resultChar)) {
                    resultWord += resultChar;
                    resultText.setText(resultWord);
                }
            }
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    isOnRecognizeState = false;
                    runOnUiThread(visibilityStateRunnable);
                }
            }, DELAY_RECOGNITION);
        }
    };

    private Runnable visibilityStateRunnable = new Runnable() {
        @Override
        public void run() {
            spinKitView.setVisibility(spinKitView.getVisibility() == View.VISIBLE ? View.INVISIBLE : View.VISIBLE);
        }
    };

    @Override
    public int onGetThreshold() {
        return threshold;
    }

    @Override
    public void onRestartHandler() {
        removeRunnable(recognitionRunnable);
    }

    @Override
    protected void onResume() {
        super.onResume();
        startHandler();
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopHandler();
    }

    private synchronized void runInBackground(final Runnable r) {
        if (handler != null) {
            handler.post(r);
        }
    }

    private void startHandler() {
        handlerThread = new HandlerThread("inference");
        handlerThread.start();
        handler = new Handler(handlerThread.getLooper());
    }

    private void stopHandler() {
        handlerThread.quitSafely();
        try {
            handlerThread.join();
            handlerThread = null;
            handler = null;
        } catch (final InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void removeRunnable(Runnable r) {
        handler.removeCallbacks(r);
    }

    @Override
    protected void attachBaseContext(Context context) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(context));
    }
}
