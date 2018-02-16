package asl.abdelouahed;

import android.graphics.Bitmap;

import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDouble;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import static asl.abdelouahed.UtilsConstants.MAX_VALUE;
import static asl.abdelouahed.UtilsConstants.THRESHOLD;
import static org.opencv.imgproc.Imgproc.THRESH_BINARY;

/**
 * Created by abdelouahed on 2/15/18.
 */

public abstract class UtilsImage {

    public static void matToBinary(Mat mat) {
        Imgproc.threshold(mat, mat, THRESHOLD, MAX_VALUE, THRESH_BINARY);
    }

    public static Bitmap matToBitmap(Mat mat, Rect rect) {
        try {
            Mat mMat = new Mat(mat, rect);
            Bitmap bitmap = Bitmap.createBitmap(mMat.cols(), mMat.rows(), Bitmap.Config.ARGB_8888);
            Utils.matToBitmap(mMat, bitmap);
            return bitmap;
        } catch (Exception e) {
            return null;
        }
    }

    public static Scalar convertScalarHsv2Rgba(Scalar hsvColor) {
        Mat pointMatRgba = new Mat();
        Mat pointMatHsv = new Mat(1, 1, CvType.CV_8UC3, hsvColor);
        Imgproc.cvtColor(pointMatHsv, pointMatRgba, Imgproc.COLOR_HSV2RGB_FULL, 4);

        return new Scalar(pointMatRgba.get(0, 0));
    }

    public static double[] matToPixels(Mat mat) {
        MatOfDouble matOfDouble = new MatOfDouble(CvType.CV_64F);
        mat.convertTo(matOfDouble, CvType.CV_64F);
        double[] pixels = new double[(int) (matOfDouble.total() * matOfDouble.channels())];
        matOfDouble.get(0, 0, pixels);
        return pixels;
    }
}