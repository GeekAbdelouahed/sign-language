package asl.abdelouahed.recognition.dagger;

import asl.abdelouahed.app.AslComponent;
import asl.abdelouahed.recognition.mvp.RecognitionFragment;
import dagger.Component;

/**
 * Created by abdelouahed on 4/5/18.
 */
@RecognitionScope
@Component(modules = RecognitionModule.class, dependencies = AslComponent.class)
public interface RecognitionComponent {
    void inject(RecognitionFragment fragment);
}
