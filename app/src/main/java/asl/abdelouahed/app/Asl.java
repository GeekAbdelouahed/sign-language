package asl.abdelouahed.app;

import android.app.Application;
import android.support.multidex.MultiDex;

/**
 * Created by abdelouahed on 4/5/18.
 */

public class Asl extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        MultiDex.install(this);

        System.loadLibrary("opencv_java3");

    }
}
