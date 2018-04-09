package asl.abdelouahed.recognition;

import android.content.Context;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
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

import asl.abdelouahed.BaseFragment;
import asl.abdelouahed.CameraListener;
import asl.abdelouahed.R;
import asl.abdelouahed.utils.UtilsColorBlobDetector;
import asl.abdelouahed.utils.UtilsImages;
import asl.abdelouahed.views.CameraView;
import butterknife.BindView;
import butterknife.ButterKnife;

import static asl.abdelouahed.utils.UtilsConstants.THRESHOLD;

public class RecognitionFragment extends BaseFragment implements OnTouchListener, CvCameraViewListener2 {

    private CameraListener cameraListener;

    /*    @BindView(R.id.sb_threshold)
        SeekBar sbThreshold;
        @BindView(R.id.iv_test_rgb_touch)
        ImageView ivTestRgb;
        @BindView(R.id.iv_test_gray_touch)
        ImageView ivTestGray;*/
    @BindView(R.id.fab_switch_cam_touch)
    FloatingActionButton fabSwitchCam;
    @BindView(R.id.camera_view_back)
    CameraView cameraView;
/*    @BindView(R.id.tv_finger_count)
    TextView tvResult;*/

/*    private Classifier classifier;
    private List<Classifier.Recognition> results;*/

    private Bitmap bGray, bRgba;
    private Mat mRgba;
    private Mat mGray;
    private boolean isColorSelected = false;
    private Rect rBound;
    private Scalar sBlobColorHsv;
    private Mat mSpectrum;
    private Size spectrumSize;
    private Scalar contourColor;
    private Scalar contourColorWhite;

    private final Runnable runnable = new Runnable() {
        @Override
        public void run() {
            try {

                bRgba = UtilsImages.matToBitmap(mRgba, rBound);
                bGray = UtilsImages.matToBitmap(mGray, rBound);
                bGray = UtilsImages.scaleBitmap(bGray);
                bRgba = UtilsImages.scaleBitmap(bRgba);
                bGray = UtilsImages.rotateBitmap(bGray, 90);
                bRgba = UtilsImages.rotateBitmap(bRgba, 90);
                cameraListener.onFrameChanged(bRgba, bGray);

            } catch (Exception e) {
                makeToast(e.getMessage());
            }
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_recognition, container, false);
        ButterKnife.bind(this, view);

        fabSwitchCam.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int cameraIndex = cameraView.getCameraIndex();
                cameraView.disableView();
                cameraView.setCameraIndex(
                        cameraIndex == CameraView.CAMERA_ID_BACK ? CameraView.CAMERA_ID_FRONT : CameraView.CAMERA_ID_BACK

                );
                fabSwitchCam.setImageResource(
                        cameraIndex == CameraView.CAMERA_ID_BACK ? R.drawable.ic_camera_front : R.drawable.ic_camera_rear
                );
                cameraView.enableView();
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
        makeToast("Focus mode : " + cParams.getFocusMode());
        mRgba = new Mat(height, width, CvType.CV_8UC4);
        mSpectrum = new Mat();
        sBlobColorHsv = new Scalar(255);
        spectrumSize = new Size(200, 64);
        contourColor = new Scalar(255, 0, 0, 255);
        contourColorWhite = new Scalar(255, 255, 255, 255);
    }

    public boolean onTouch(View v, MotionEvent event) {
        try {
            int cols = mRgba.cols();
            int rows = mRgba.rows();
            int xOffset = (cameraView.getWidth() - cols) / 2;
            int yOffset = (cameraView.getHeight() - rows) / 2;
            int x = (int) event.getX() - xOffset;
            int y = (int) event.getY() - yOffset;
            if ((x < 0) || (y < 0) || (x > cols) || (y > rows)) return false;
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
            makeToast(e.getMessage());
        }
        return false;

    }

    public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
        mRgba = inputFrame.rgba();
        mGray = inputFrame.gray();
        // gray to binary
        UtilsImages.matToBinary(mGray);
        if (!isColorSelected) return mRgba;
        List<MatOfPoint> contours = UtilsColorBlobDetector.getContours();
        UtilsColorBlobDetector.process(mRgba);
        if (contours.size() <= 0) {
            return mRgba;
        }
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
        Imgproc.rectangle(mRgba, rBound.tl(), rBound.br(), contourColorWhite, 2, 8, 0);
        double a = rBound.br().y - rBound.tl().y;
        a = a * 0.7;
        a = rBound.tl().y + a;
        Imgproc.rectangle(mRgba, rBound.tl(), new Point(rBound.br().x, a), contourColor, 2, 8, 0);
        MatOfPoint2f pointMat = new MatOfPoint2f();
        Imgproc.approxPolyDP(new MatOfPoint2f(contours.get(boundPos).toArray()), pointMat, 3, true);
        contours.set(boundPos, new MatOfPoint(pointMat.toArray()));
        MatOfInt hull = new MatOfInt();
        MatOfInt4 convexDefect = new MatOfInt4();
        Imgproc.convexHull(new MatOfPoint(contours.get(boundPos).toArray()), hull);
        if (hull.toArray().length < 3) return mRgba;
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
            if (depth > THRESHOLD && farPoint.y < a) {
                listPoDefect.add(contours.get(boundPos).toList().get(convexDefect.toList().get(j + 2)));
            }
        }
        MatOfPoint e2 = new MatOfPoint();
        e2.fromList(listPo);
        defectPoints.add(e2);
        Imgproc.drawContours(mRgba, hullPoints, -1, contourColor, 3);
        for (Point p : listPoDefect) {
            Imgproc.circle(mRgba, p, 6, new Scalar(255, 0, 255));
        }
        getActivity().runOnUiThread(runnable);
        return mRgba;
    }

    public void onCameraViewStopped() {
        mGray.release();
        mRgba.release();
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

    public void onDestroy() {
        super.onDestroy();
        cameraView.disableView();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        cameraListener = (CameraListener) context;
    }
}
