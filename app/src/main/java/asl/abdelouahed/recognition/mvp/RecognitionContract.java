package asl.abdelouahed.recognition.mvp;

import android.graphics.Bitmap;

import asl.abdelouahed.models.Classifier;
import io.reactivex.Flowable;
import io.reactivex.functions.Consumer;

/**
 * Created by abdelouahed on 4/5/18.
 */

public interface RecognitionContract {

    interface View {
        Consumer<Classifier.Recognition> onRecognitionSuccess();
    }

    interface Model {
        Flowable<Classifier.Recognition> Recognition(Bitmap bitmap);
    }

    interface Presenter {

        void recognition(Bitmap bitmap);

        void onDestroy();
    }
}
