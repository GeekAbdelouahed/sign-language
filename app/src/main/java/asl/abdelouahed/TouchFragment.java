package asl.abdelouahed;

import android.graphics.Bitmap;
import android.hardware.Camera;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import org.neuroph.contrib.imgrec.ImageRecognitionPlugin;
import org.neuroph.core.NeuralNetwork;
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

import java.io.File;
import java.io.InputStream;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import static asl.abdelouahed.UtilsConstants.THRESHOLD;

public class TouchFragment extends BaseFragment implements OnTouchListener, CvCameraViewListener2 {

    private static final String TAG = "Asl::CascadeFragment";

    private SeekBar sbThreshold;
    private ImageView ivTestRgb, ivTestGray, ivSwitchCam;
    private CameraView cameraView;
    private TextView tvFingersCount;
    private BaseLoaderListener baseLoaderListener;
    private final Runnable runnable = new Runnable() {
        @Override
        public void run() {
            try {
                tvFingersCount.setText(numberOfFingers + "");

                bRgba = UtilsImage.matToBitmap(mRgba, rBound);
                bGray = UtilsImage.matToBitmap(mGray, rBound);

                ivTestRgb.setImageBitmap(bRgba);
                ivTestGray.setImageBitmap(bGray);

                arrayPixels = UtilsImage.matToPixels(mGray);

                File file = UtilsImage.bitmapToFile(getActivity(), bGray);
                HashMap<String, Double> result = UtilsImage.recognizeImage(imageRecognition, file);
                String answer = UtilsImage.getAnswerRecognition(result);
                makeToast(answer);

            } catch (Exception e) {
                makeToast(e.getMessage());
            }
        }
    };

    private Runnable loadDataRunnable = new Runnable() {
        public void run() {
            // open neural network
            InputStream is = getResources().openRawResource(R.raw.animals_net);
            // load neural network
            nnet = NeuralNetwork.load(is);
            imageRecognition = (ImageRecognitionPlugin) nnet.getPlugin(ImageRecognitionPlugin.class);
        }
    };

    private NeuralNetwork nnet;
    private ImageRecognitionPlugin imageRecognition;

    private double[] arrayPixels;
    private Bitmap bGray, bRgba;
    private Mat mRgba;
    private Mat mGray;
    private boolean isColorSelected = false;
    private Rect rBound;
    private Scalar sBlobColorHsv;
    private Scalar sBlobColorRgba;
    private ColorBlobDetector detector;
    private Mat mSpectrum;
    private Size spectrumSize;
    private Scalar contourColor;
    private Scalar contourColorWhite;
    private int numberOfFingers = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        new Thread(null, loadDataRunnable, "dataLoader", 32000).start();

        View view = inflater.inflate(R.layout.fragment_touch, container, false);

        cameraView = view.findViewById(R.id.camera_view_touch);
        ivTestRgb = view.findViewById(R.id.iv_test_rgb_touch);
        ivTestGray = view.findViewById(R.id.iv_test_gray_touch);
        ivSwitchCam = view.findViewById(R.id.iv_switch_cam_touch);
        tvFingersCount = view.findViewById(R.id.tv_finger_count);
        sbThreshold = view.findViewById(R.id.sb_threshold);
        sbThreshold.setProgress(THRESHOLD);

        baseLoaderListener = new BaseLoaderListener
                .BUILDER()
                .setContext(getContext())
                .setCameraView(cameraView)
                .setTouchListener(this)
                .setCameraListener(this)
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

        sbThreshold.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                makeToast(progress + "");
                THRESHOLD = progress;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                makeToast(seekBar.getProgress() + "");
                THRESHOLD = seekBar.getProgress();
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
        mSpectrum = new Mat();
        sBlobColorRgba = new Scalar(255);
        sBlobColorHsv = new Scalar(255);
        spectrumSize = new Size(200, 64);
        contourColor = new Scalar(255, 0, 0, 255);
        contourColorWhite = new Scalar(255, 255, 255, 255);
        detector = new ColorBlobDetector();
    }

    public boolean onTouch(View v, MotionEvent event) {
        try {
            int cols = mRgba.cols();
            int rows = mRgba.rows();

            int xOffset = (cameraView.getWidth() - cols) / 2;
            int yOffset = (cameraView.getHeight() - rows) / 2;

            int x = (int) event.getX() - xOffset;
            int y = (int) event.getY() - yOffset;

            Log.i(TAG, "Touch image coordinates: (" + x + ", " + y + ")");

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

            sBlobColorRgba = UtilsImage.convertScalarHsv2Rgba(sBlobColorHsv);

            Log.i(TAG, "Touched rgba color: (" + sBlobColorRgba.val[0] + ", " + sBlobColorRgba.val[1] +
                    ", " + sBlobColorRgba.val[2] + ", " + sBlobColorRgba.val[3] + ")");

            detector.setHsvColor(sBlobColorHsv);

            Imgproc.resize(detector.getSpectrum(), mSpectrum, spectrumSize);

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
        UtilsImage.matToBinary(mGray);

        if (!isColorSelected) return mRgba;

        List<MatOfPoint> contours = detector.getContours();

        detector.process(mRgba);

        Log.d(TAG, "Contours count: " + contours.size());

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


        Log.d(TAG,
                " Row start [" +
                        (int) rBound.tl().y + "] row end [" +
                        (int) rBound.br().y + "] Col start [" +
                        (int) rBound.tl().x + "] Col end [" +
                        (int) rBound.br().x + "]");

        double a = rBound.br().y - rBound.tl().y;
        a = a * 0.7;
        a = rBound.tl().y + a;

        Log.d(TAG, " A [" + a + "] br y - tl y = [" + (rBound.br().y - rBound.tl().y) + "]");

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
            Log.d(TAG, "defects [" + j + "] " + convexDefect.toList().get(j + 3));
        }

        MatOfPoint e2 = new MatOfPoint();
        e2.fromList(listPo);
        defectPoints.add(e2);

        Log.d(TAG, "hull: " + hull.toList());
        Log.d(TAG, "defects: " + convexDefect.toList());

        Imgproc.drawContours(mRgba, hullPoints, -1, contourColor, 3);

        int defectsTotal = (int) convexDefect.total();
        Log.d(TAG, "Defect total " + defectsTotal);

        numberOfFingers = listPoDefect.size();
        if (numberOfFingers > 5) numberOfFingers = 5;

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
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_4_0, getContext(), baseLoaderListener);
    }

    public void onDestroy() {
        super.onDestroy();
        cameraView.disableView();
    }
}
