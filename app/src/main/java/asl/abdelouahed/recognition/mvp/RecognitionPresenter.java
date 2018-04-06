package asl.abdelouahed.recognition.mvp;

import android.graphics.Bitmap;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by abdelouahed on 4/5/18.
 */

public class RecognitionPresenter implements RecognitionContract.Presenter {

    private final RecognitionContract.View view;
    private final RecognitionContract.Model model;

    private final CompositeDisposable compositeDisposable = new CompositeDisposable();

    public RecognitionPresenter(RecognitionContract.View view, RecognitionContract.Model model) {
        this.view = view;
        this.model = model;
    }

    @Override
    public void recognition(Bitmap bitmap) {
        Disposable disposable = model.Recognition(bitmap)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(view.onRecognitionSuccess());
        compositeDisposable.add(disposable);
    }

    @Override
    public void onDestroy() {
        compositeDisposable.dispose();
    }
}
