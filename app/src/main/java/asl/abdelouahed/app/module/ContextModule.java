package asl.abdelouahed.app.module;

import android.content.Context;

import asl.abdelouahed.app.AslScope;
import dagger.Module;
import dagger.Provides;

/**
 * Created by abdelouahed on 4/5/18.
 */
@Module
public class ContextModule {

    private final Context context;

    public ContextModule(Context context) {
        this.context = context;
    }

    @AslScope
    @Provides
    public Context context() {
        return context;
    }

}
