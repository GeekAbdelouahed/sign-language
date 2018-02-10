package asl.abdelouahed;

import android.content.Context;
import android.view.View;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by abdelouahed on 2/9/18.
 */

public class BaseLoaderListener extends BaseLoaderCallback {

    private Context context;
    private CameraView cameraView;
    private View.OnTouchListener touchListener;
    private CameraBridgeViewBase.CvCameraViewListener2 cameraListener;
    private OnCascadeLoadListener cascadeLoadListener;

    private BaseLoaderListener(Context AppContext) {
        super(AppContext);
    }

    private BaseLoaderListener(Context context, CameraView cameraView, View.OnTouchListener touchListener, CameraBridgeViewBase.CvCameraViewListener2 cameraListener, OnCascadeLoadListener cascadeLoadListener) {
        super(context);
        this.context = context;
        this.cameraView = cameraView;
        this.touchListener = touchListener;
        this.cameraListener = cameraListener;
        this.cascadeLoadListener = cascadeLoadListener;
    }

    @Override
    public void onManagerConnected(int status) {
        switch (status) {
            case LoaderCallbackInterface.SUCCESS: {
                cameraView.enableView();
                if (touchListener != null)
                    cameraView.setOnTouchListener(touchListener);
                if (cameraListener != null)
                    cameraView.setCvCameraViewListener(cameraListener);

                try {
                    InputStream is = context.getResources().openRawResource(R.raw.cascade);
                    File cascadeDir = context.getDir("cascade", Context.MODE_PRIVATE);
                    File mCascadeFile = new File(cascadeDir, "cascade.xml");
                    FileOutputStream os = new FileOutputStream(mCascadeFile);
                    byte[] buffer = new byte[4096];
                    int bytesRead;
                    while ((bytesRead = is.read(buffer)) != -1) {
                        os.write(buffer, 0, bytesRead);
                    }
                    is.close();
                    os.close();
                    cascadeLoadListener.onLoadListener(mCascadeFile);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            break;
            default: {
                super.onManagerConnected(status);
            }
            break;
        }
    }

    public static class BUILDER {
        private Context context;
        private CameraView cameraView;
        private View.OnTouchListener touchListener;
        private CameraBridgeViewBase.CvCameraViewListener2 cameraListener;
        private OnCascadeLoadListener cascadeLoadListener;

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

        public BUILDER setCascadeLoadListener(OnCascadeLoadListener cascadeLoadListener) {
            this.cascadeLoadListener = cascadeLoadListener;
            return this;
        }

        public BaseLoaderListener build() {
            return new BaseLoaderListener(context, cameraView, touchListener, cameraListener, cascadeLoadListener);
        }
    }


    public interface OnCascadeLoadListener {
        void onLoadListener(File cascadeFile);
    }
}
