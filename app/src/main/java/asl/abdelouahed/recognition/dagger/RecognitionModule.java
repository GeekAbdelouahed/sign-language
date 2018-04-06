package asl.abdelouahed.recognition.dagger;

import asl.abdelouahed.models.Classifier;
import asl.abdelouahed.recognition.mvp.RecognitionContract;
import asl.abdelouahed.recognition.mvp.RecognitionFragment;
import asl.abdelouahed.recognition.mvp.RecognitionModel;
import asl.abdelouahed.recognition.mvp.RecognitionPresenter;
import dagger.Module;
import dagger.Provides;

/**
 * Created by abdelouahed on 4/5/18.
 *
 */
@Module
public class RecognitionModule {

    private final RecognitionFragment view;

    public RecognitionModule(RecognitionFragment view) {
        this.view = view;
    }

    @RecognitionScope
    @Provides
    public RecognitionContract.View getView() {
        return view;
    }

    @RecognitionScope
    @Provides
    public RecognitionContract.Model getModel(Classifier classifier) {
        return new RecognitionModel(classifier);
    }

    @RecognitionScope
    @Provides
    public RecognitionContract.Presenter getPresenter(RecognitionContract.View view, RecognitionContract.Model model) {
        return new RecognitionPresenter(view, model);
    }
}
