package asl.abdelouahed.app;

import android.app.Application;
import android.support.multidex.MultiDex;

import asl.abdelouahed.app.module.ContextModule;
import asl.abdelouahed.app.module.TensorflowModule;

/**
 * Created by abdelouahed on 4/5/18.
 */

public class Asl extends Application {

    private AslComponent component;

    @Override
    public void onCreate() {
        super.onCreate();

        MultiDex.install(this);

        System.loadLibrary("opencv_java3");

        component = DaggerAslComponent.builder()
                .contextModule(new ContextModule(this))
                .tensorflowModule(new TensorflowModule())
                .build();

    }

    public static Asl app(Application application) {
        return (Asl) application;
    }

    public AslComponent getComponent() {
        return component;
    }


}
