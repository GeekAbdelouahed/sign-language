package asl.abdelouahed;

import android.app.Application;

/**
 * Created by abdelouahed on 2/9/18.
 */

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        System.loadLibrary("opencv_java3");
    }
}
