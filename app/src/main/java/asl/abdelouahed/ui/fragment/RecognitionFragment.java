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
import org.opencv.core.MatOfInt;
import org.opencv.core.MatOfInt4;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.RotatedRect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.LinkedList;
import java.util.List;

import asl.abdelouahed.CameraListener;
import asl.abdelouahed.R;
import asl.abdelouahed.ui.custom.CameraView;
import asl.abdelouahed.utils.UtilsColorBlobDetector;
import asl.abdelouahed.utils.UtilsImages;
import butterknife.BindView;
import butterknife.ButterKnife;


public class RecognitionFragment extends Fragment implements OnTouchListener, CvCameraViewListener2 {

    private static final String TAG = "TAG:RecognitionFragment";

    @BindView(R.id.fab_switch_camera)
    FloatingActionButton switchCameraFab;
    @BindView(R.id.camera_view)
    CameraView cameraView;

    private CameraListener listener;

    private Mat rgbaMat, grayMat;
    private Mat spectrumMat;
    private Rect boundRect;
    private Scalar blobColorHsvScalar;
    private Size spectrumSize;
    private boolean isFront = false;
    private boolean isColorSelected = false;

    private AnimatorSet rotateFab;
    private Animator.AnimatorListener animatorListener = new AnimatorListenerAdapter() {
        @Override
        public void onAnimationEnd(Animator animation) {
            super.onAnimationEnd(animation);
            int camera_drawable = isFront ? R.drawable.ic_camera_front : R.drawable.ic_camera_rear;
            switchCameraFab.setImageResource(camera_drawable);
        }
    };

    private final Runnable frameRunnable = new Runnable() {
        @Override
        public void run() {
            try {
                Bitmap bGray = UtilsImages.matToBitmap(grayMat, boundRect);
                Bitmap bRgba = UtilsImages.matToBitmap(rgbaMat, boundRect);
                if (bGray != null)
                    bGray = UtilsImages.scaleBitmap(bGray);
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

        rotateFab = (AnimatorSet) AnimatorInflater.loadAnimator(getActivity(), R.animator.fab_rotate);
        rotateFab.setTarget(switchCameraFab);
        rotateFab.addListener(animatorListener);
        switchCameraFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rotateFab.start();
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
        grayMat = new Mat();
        rgbaMat = new Mat();
        Camera.Parameters cParams = cameraView.getParameters();
        cParams.setFocusMode(Camera.Parameters.FOCUS_MODE_INFINITY);
        cameraView.setParameters(cParams);
        rgbaMat = new Mat(height, width, CvType.CV_8UC4);
        spectrumMat = new Mat();
        blobColorHsvScalar = new Scalar(255);
        spectrumSize = new Size(200, 64);
    }

    public boolean onTouch(View v, MotionEvent event) {
        listener.onRestartHandler();
        try {
            int cols = rgbaMat.cols();
            int rows = rgbaMat.rows();
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
            Mat touchedRegionRgba = rgbaMat.submat(touchedRect);
            Mat touchedRegionHsv = new Mat();
            Imgproc.cvtColor(touchedRegionRgba, touchedRegionHsv, Imgproc.COLOR_RGB2HSV_FULL);
            // Calculate average color of touched region
            blobColorHsvScalar = Core.sumElems(touchedRegionHsv);
            int pointCount = touchedRect.width * touchedRect.height;
            for (int i = 0; i < blobColorHsvScalar.val.length; i++)
                blobColorHsvScalar.val[i] /= pointCount;
            UtilsColorBlobDetector.setHsvColor(blobColorHsvScalar);
            Imgproc.resize(UtilsColorBlobDetector.getSpectrum(), spectrumMat, spectrumSize);
            isColorSelected = true;
            touchedRegionRgba.release();
            touchedRegionHsv.release();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
        rgbaMat = inputFrame.rgba();
        grayMat = inputFrame.gray();
        // gray to binary
        int threshold = listener.onGetThreshold();
        UtilsImages.matToBinary(grayMat, threshold);
        UtilsColorBlobDetector.process(rgbaMat);
        if (!isColorSelected)
            return rgbaMat;

        List<MatOfPoint> contours = UtilsColorBlobDetector.getContours();
        if (contours.size() <= 0)
            return rgbaMat;

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
        boundRect = Imgproc.boundingRect(new MatOfPoint(contours.get(boundPos).toArray()));
        Imgproc.rectangle(rgbaMat, boundRect.tl(), boundRect.br(), new Scalar(255, 255, 255, 255), 2, 8, 0);

        double a = boundRect.br().y - boundRect.tl().y;
        a = a * 0.7;
        a = boundRect.tl().y + a;

        MatOfPoint2f pointMat = new MatOfPoint2f();
        Imgproc.approxPolyDP(new MatOfPoint2f(contours.get(boundPos).toArray()), pointMat, 3, true);
        contours.set(boundPos, new MatOfPoint(pointMat.toArray()));

        MatOfInt hull = new MatOfInt();
        MatOfInt4 convexDefect = new MatOfInt4();
        Imgproc.convexHull(new MatOfPoint(contours.get(boundPos).toArray()), hull);

        if (hull.toArray().length < 3) return rgbaMat;

        Imgproc.convexityDefects(new MatOfPoint(contours.get(boundPos).toArray()), hull, convexDefect);

        List<MatOfPoint> hullPoints = new LinkedList<>();
        List<Point> listPo = new LinkedList<>();
        for (int j = 0; j < hull.toList().size(); j++) {
            listPo.add(contours.get(boundPos).toList().get(hull.toList().get(j)));
        }

        MatOfPoint e = new MatOfPoint();
        e.fromList(listPo);
        hullPoints.add(e);

        List<MatOfPoint> defectPoints = new LinkedList<>();
        List<Point> listPoDefect = new LinkedList<>();
        for (int j = 0; j < convexDefect.toList().size(); j = j + 4) {
            Point farPoint = contours.get(boundPos).toList().get(convexDefect.toList().get(j + 2));
            Integer depth = convexDefect.toList().get(j + 3);
            if (depth > threshold && farPoint.y < a) {
                listPoDefect.add(contours.get(boundPos).toList().get(convexDefect.toList().get(j + 2)));
            }
        }

        MatOfPoint e2 = new MatOfPoint();
        e2.fromList(listPo);
        defectPoints.add(e2);

        Imgproc.drawContours(rgbaMat, hullPoints, -1, new Scalar(255, 0, 0, 255), 3);

        for (Point p : listPoDefect) {
            Imgproc.circle(rgbaMat, p, 6, new Scalar(255, 0, 255));
        }

        getActivity().runOnUiThread(frameRunnable);
        return rgbaMat;
    }

    public void onCameraViewStopped() {
        grayMat.release();
        rgbaMat.release();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        listener = (CameraListener) context;
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
        rotateFab.removeListener(animatorListener);
    }

}
