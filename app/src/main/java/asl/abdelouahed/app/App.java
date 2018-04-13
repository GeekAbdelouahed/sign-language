package asl.abdelouahed.app;

import android.app.Application;
import android.support.multidex.MultiDex;

import asl.abdelouahed.R;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;

/**
 * Created by abdelouahed on 4/5/18.
 */

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        MultiDex.install(this);

        System.loadLibrary("opencv_java3");

        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/font.ttf")
                .setFontAttrId(R.attr.fontPath)
                .build()
        );
    }
}
