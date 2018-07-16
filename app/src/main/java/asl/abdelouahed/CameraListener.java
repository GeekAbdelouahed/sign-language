package asl.abdelouahed;

import android.graphics.Bitmap;

/**
 * Created by abdelouahed on 4/8/18.
 */

public interface CameraListener {
    void onFrameChanged(Bitmap bRgba, Bitmap bGray);

    int onGetThreshold();

    void onRestartHandler();
}
