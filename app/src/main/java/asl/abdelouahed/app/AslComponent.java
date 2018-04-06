package asl.abdelouahed.app;

import android.content.Context;

import asl.abdelouahed.app.module.ContextModule;
import asl.abdelouahed.app.module.TensorflowModule;
import asl.abdelouahed.models.Classifier;
import dagger.Component;

/**
 * Created by abdelouahed on 4/5/18.
 */
@AslScope
@Component(modules = {ContextModule.class, TensorflowModule.class})
public interface AslComponent {

    Context getContext();

    Classifier getClassifier();
}
