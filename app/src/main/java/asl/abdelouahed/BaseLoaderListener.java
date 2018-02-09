package asl.abdelouahed;

import android.content.Context;
import android.view.View;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;

/**
 * Created by abdelouahed on 2/9/18.
 */

public class BaseLoaderListener extends BaseLoaderCallback {

    private CameraView cameraView;
    private View.OnTouchListener listener;
    private CameraBridgeViewBase.CvCameraViewListener2 cameraListener;

    private BaseLoaderListener(Context AppContext){
        super(AppContext);
    }

    private BaseLoaderListener(Context AppContext, CameraView cameraView, View.OnTouchListener listener , CameraBridgeViewBase.CvCameraViewListener2 cameraListener) {
        super(AppContext);
        this.cameraView = cameraView;
        this.listener = listener;
        this.cameraListener = cameraListener;
    }

    @Override
    public void onManagerConnected(int status) {
        switch (status) {
            case LoaderCallbackInterface.SUCCESS: {
                cameraView.enableView();
                cameraView.setOnTouchListener(listener);
                cameraView.setCvCameraViewListener(cameraListener);
            }
            break;
            default: {
                super.onManagerConnected(status);
            }
            break;
        }
    }

    public static class BUILDER{
        private Context context;
        private CameraView cameraView;
        private View.OnTouchListener touchListener;
        private CameraBridgeViewBase.CvCameraViewListener2 cameraListener;

        public BUILDER setContext(Context context) {
            this.context = context;
            return this;
        }

        public BUILDER setCameraView(CameraView cameraView) {
            this.cameraView = cameraView;
            return this;
        }

        public BUILDER setTouchListener(View.OnTouchListener touchListener) {
            this.touchListener = touchListener;
            return this;
        }

        public BUILDER setCameraListener(CameraBridgeViewBase.CvCameraViewListener2 cameraListener) {
            this.cameraListener = cameraListener;
            return this;
        }
        public BaseLoaderListener build(){
            if (context == null || cameraView == null || touchListener == null || cameraListener == null)
                return null;
            return new BaseLoaderListener(context , cameraView , touchListener , cameraListener);
        }
    }
}
