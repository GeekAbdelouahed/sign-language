package asl.abdelouahed.app.module;

import android.content.Context;

import java.io.IOException;

import asl.abdelouahed.app.AslScope;
import asl.abdelouahed.models.Classifier;
import asl.abdelouahed.models.TensorFlowImageClassifier;
import dagger.Module;
import dagger.Provides;

/**
 * Created by abdelouahed on 4/5/18.
 */
@Module
public class TensorflowModule {

    @AslScope
    @Provides
    public Classifier classifier(Context context) {
        try {
            return TensorFlowImageClassifier.create(context.getAssets());
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
