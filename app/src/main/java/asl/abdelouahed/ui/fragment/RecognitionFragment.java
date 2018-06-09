package asl.abdelouahed.ui.fragment;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.content.Context;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Rect;
import org.opencv.core.RotatedRect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.List;

import asl.abdelouahed.ICameraListener;
import asl.abdelouahed.R;
import asl.abdelouahed.ui.custom.CameraView;
import asl.abdelouahed.utils.UtilsColorBlobDetector;
import asl.abdelouahed.utils.UtilsImages;
import butterknife.BindView;
import butterknife.ButterKnife;


public class RecognitionFragment extends Fragment implements OnTouchListener, CvCameraViewListener2 {

    private static final String TAG = "TAG:RecognitionFragment";

    @BindView(R.id.fab_switch_camera)
    FloatingActionButton fabSwitchCam;
    @BindView(R.id.camera_view)
    CameraView cameraView;

    private ICameraListener listener;
    private Mat mRgba, mGray;
    private Mat mSpectrum;
    private Rect rBound;
    private Scalar sBlobColorHsv;
    private Size spectrumSize;
    private boolean isFront = false;
    private boolean isColorSelected = false;
    private AnimatorSet fabRotate;
    private Animator.AnimatorListener animatorListener = new AnimatorListenerAdapter() {
        @Override
        public void onAnimationEnd(Animator animation) {
            super.onAnimationEnd(animation);
            int camera_drawable = isFront ? R.drawable.ic_camera_front : R.drawable.ic_camera_rear;
            fabSwitchCam.setImageResource(camera_drawable);
        }
    };
    private final Runnable runnable = new Runnable() {
        @Override
        public void run() {
            try {

                Bitmap bGray = UtilsImages.matToBitmap(mGray, rBound);
                Bitmap bRgba = UtilsImages.matToBitmap(mRgba, rBound);
                bGray = UtilsImages.scaleBitmap(bGray);
                bRgba = UtilsImages.scaleBitmap(bRgba);
                float degree = isFront ? -90 : 90;
                bGray = UtilsImages.rotateBitmap(bGray, degree);
                bRgba = UtilsImages.rotateBitmap(bRgba, degree);
                listener.onFrameChanged(bRgba, bGray);

            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
            }
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_recognition, container, false);
        ButterKnife.bind(this, view);
        fabRotate = (AnimatorSet) AnimatorInflater.loadAnimator(getActivity(), R.animator.fab_rotate);
        fabRotate.setTarget(fabSwitchCam);
        fabRotate.addListener(animatorListener);
        fabSwitchCam.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fabRotate.start();
                int camera_id = isFront ? CameraView.CAMERA_ID_BACK : CameraView.CAMERA_ID_FRONT;
                cameraView.disableView();
                cameraView.setCameraIndex(camera_id);
                cameraView.enableView();
                isFront = !isFront;
            }
        });
        return view;
    }

    public void onCameraViewStarted(int width, int height) {
        mGray = new Mat();
        mRgba = new Mat();
        Camera.Parameters cParams = cameraView.getParameters();
        cParams.setFocusMode(Camera.Parameters.FOCUS_MODE_INFINITY);
        cameraView.setParameters(cParams);
        mRgba = new Mat(height, width, CvType.CV_8UC4);
        mSpectrum = new Mat();
        sBlobColorHsv = new Scalar(255);
        spectrumSize = new Size(200, 64);
    }

    public boolean onTouch(View v, MotionEvent event) {
        listener.onRestartHandler();
        try {
            int cols = mRgba.cols();
            int rows = mRgba.rows();
            int xOffset = (cameraView.getWidth() - cols) / 2;
            int yOffset = (cameraView.getHeight() - rows) / 2;
            int x = (int) event.getX() - xOffset;
            int y = (int) event.getY() - yOffset;
            if ((x < 0) || (y < 0) || (x > cols) || (y > rows))
                return false;
            Rect touchedRect = new Rect();
            touchedRect.x = (x > 5) ? x - 5 : 0;
            touchedRect.y = (y > 5) ? y - 5 : 0;
            touchedRect.width = (x + 5 < cols) ? x + 5 - touchedRect.x : cols - touchedRect.x;
            touchedRect.height = (y + 5 < rows) ? y + 5 - touchedRect.y : rows - touchedRect.y;
            Mat touchedRegionRgba = mRgba.submat(touchedRect);
            Mat touchedRegionHsv = new Mat();
            Imgproc.cvtColor(touchedRegionRgba, touchedRegionHsv, Imgproc.COLOR_RGB2HSV_FULL);
            // Calculate average color of touched region
            sBlobColorHsv = Core.sumElems(touchedRegionHsv);
            int pointCount = touchedRect.width * touchedRect.height;
            for (int i = 0; i < sBlobColorHsv.val.length; i++)
                sBlobColorHsv.val[i] /= pointCount;
            UtilsColorBlobDetector.setHsvColor(sBlobColorHsv);
            Imgproc.resize(UtilsColorBlobDetector.getSpectrum(), mSpectrum, spectrumSize);
            isColorSelected = true;
            touchedRegionRgba.release();
            touchedRegionHsv.release();
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
        return false;
    }

    public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
        mRgba = inputFrame.rgba();
        mGray = inputFrame.gray();
        // gray to binary
        int threshold = listener.onGetThreshold();
        UtilsImages.matToBinary(mGray, threshold);
        UtilsColorBlobDetector.process(mRgba);
        if (!isColorSelected)
            return mRgba;

        List<MatOfPoint> contours = UtilsColorBlobDetector.getContours();
        if (contours.size() <= 0)
            return mRgba;

        RotatedRect rect = Imgproc.minAreaRect(new MatOfPoint2f(contours.get(0).toArray()));
        double boundWidth = rect.size.width;
        double boundHeight = rect.size.height;
        int boundPos = 0;
        for (int i = 1; i < contours.size(); i++) {
            rect = Imgproc.minAreaRect(new MatOfPoint2f(contours.get(i).toArray()));
            if (rect.size.width * rect.size.height > boundWidth * boundHeight) {
                boundWidth = rect.size.width;
                boundHeight = rect.size.height;
                boundPos = i;
            }
        }
        rBound = Imgproc.boundingRect(new MatOfPoint(contours.get(boundPos).toArray()));
        getActivity().runOnUiThread(runnable);
        return mRgba;
    }

    public void onCameraViewStopped() {
        mGray.release();
        mRgba.release();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        listener = (ICameraListener) context;
    }

    @Override
    public void onResume() {
        super.onResume();
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_4_0, getContext(), new BaseLoaderCallback(getContext()) {
            @Override
            public void onManagerConnected(int status) {
                super.onManagerConnected(status);
                cameraView.enableView();
                cameraView.setOnTouchListener(RecognitionFragment.this);
                cameraView.setCvCameraViewListener(RecognitionFragment.this);
            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();
        if (cameraView != null) {
            cameraView.disableView();
        }
    }

    public void onDestroy() {
        super.onDestroy();
        cameraView.disableView();
        fabRotate.removeListener(animatorListener);
    }

}
