package asl.abdelouahed;

import android.graphics.Bitmap;
import android.hardware.Camera;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;

import java.io.File;

public class CascadeFragment extends BaseFragment implements CameraBridgeViewBase.CvCameraViewListener2, BaseLoaderListener.OnCascadeLoadListener {

    private static final String TAG = "Asl::CascadeFragment";

    private ImageView ivTestRgb, ivTestGray, ivSwitchCam;
    private CameraView cameraView;
    private BaseLoaderListener baseLoaderListener;
    private final Runnable runnable = new Runnable() {
        @Override
        public void run() {
            matToBitmap(mRgba, mGray, boundRect);
        }
    };

    private static final Scalar HAND_RECT_COLOR = new Scalar(200, 255, 200, 255);
    private Rect boundRect;
    private Mat mRgba;
    private Mat mGray;
    private CascadeClassifier cascadeClassifier;

    @Override
    public void onLoadListener(File cascadeFile) {
        cascadeClassifier = new CascadeClassifier(cascadeFile.getAbsolutePath());
        if (cascadeClassifier.empty()) {
            makeToast("Failed to load cascade classifier");
        } else
            makeToast("Loaded cascade classifier from " + cascadeFile.getAbsolutePath());

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_cascade, container, false);

        cameraView = view.findViewById(R.id.camera_view);
        ivTestRgb = view.findViewById(R.id.iv_test_rgb);
        ivTestGray = view.findViewById(R.id.iv_test_gray);
        ivSwitchCam = view.findViewById(R.id.iv_switch_cam);

        baseLoaderListener = new BaseLoaderListener
                .BUILDER()
                .setContext(getContext())
                .setCameraView(cameraView)
                .setCameraListener(this)
                .setCascadeLoadListener(this)
                .build();

        ivSwitchCam.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                int cameraIndex = cameraView.getCameraIndex();

                cameraView.disableView();

                cameraView.setCameraIndex(
                        cameraIndex == CameraView.CAMERA_ID_BACK ?
                                CameraView.CAMERA_ID_FRONT :
                                CameraView.CAMERA_ID_BACK

                );

                ivSwitchCam.setImageResource(
                        cameraIndex == CameraView.CAMERA_ID_BACK ?
                                R.drawable.ic_camera_front :
                                R.drawable.ic_camera_rear
                );

                cameraView.enableView();

            }
        });

        return view;
    }

    public void onCameraViewStarted(int width, int height) {
        mGray = new Mat();
        mRgba = new Mat();
        Camera.Size resolution = cameraView.getResolution();
        String caption = "Resolution " + Integer.valueOf(resolution.width).toString() + "x" + Integer.valueOf(resolution.height).toString();
        makeToast(caption);

        Camera.Parameters cParams = cameraView.getParameters();
        cParams.setFocusMode(Camera.Parameters.FOCUS_MODE_INFINITY);
        cameraView.setParameters(cParams);

        makeToast("Focus mode : " + cParams.getFocusMode());

        mRgba = new Mat(height, width, CvType.CV_8UC4);

    }

    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        mRgba = inputFrame.rgba();
        mGray = inputFrame.gray();
        Mat bwMath = new Mat();
        Imgproc.threshold(mGray, bwMath, 100, 255, Imgproc.THRESH_BINARY);
        MatOfRect hands = new MatOfRect();
        cascadeClassifier.detectMultiScale(bwMath, hands);
        if (!hands.empty()) {
            Rect rect = hands.toList().get(0);
            Imgproc.rectangle(mRgba, rect.tl(), rect.br(), HAND_RECT_COLOR);
            boundRect = rect;
            getActivity().runOnUiThread(runnable);
        }
        return mRgba;
    }

    public void onCameraViewStopped() {
        mGray.release();
        mRgba.release();
    }

    private Bitmap matToBitmap(Mat mRgba, Mat mGray, Rect rect) {
        try {
            Mat mMat = new Mat(mRgba, rect);
            Bitmap bitmap = Bitmap.createBitmap(mMat.cols(), mMat.rows(), Bitmap.Config.ARGB_8888);
            Utils.matToBitmap(mMat, bitmap);
            ivTestRgb.setImageBitmap(bitmap);
            mMat = new Mat(mGray, rect);
            bitmap = Bitmap.createBitmap(mMat.cols(), mMat.rows(), Bitmap.Config.ARGB_8888);
            Utils.matToBitmap(mMat, bitmap);
            ivTestGray.setImageBitmap(bitmap);
            return bitmap;
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (cameraView != null) {
            cameraView.disableView();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_4_0, getContext(), baseLoaderListener);
    }

    public void onDestroy() {
        super.onDestroy();
        cameraView.disableView();
    }

}
