package asl.abdelouahed.recognition.mvp;

import android.graphics.Bitmap;

import java.util.List;

import asl.abdelouahed.models.Classifier;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;

/**
 * Created by abdelouahed on 4/5/18.
 */

public class RecognitionModel implements RecognitionContract.Model {
    private final Classifier classifier;

    public RecognitionModel(Classifier classifier) {
        this.classifier = classifier;
    }

    @Override
    public Flowable<Classifier.Recognition> Recognition(Bitmap bitmap) {
        return Flowable.create(e -> {
            List<Classifier.Recognition> results = classifier.recognizeImage(bitmap);
            for (Classifier.Recognition recognition : results) {
                e.onNext(recognition);
            }
        }, BackpressureStrategy.LATEST);
    }
}
