package asl.abdelouahed;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Environment;

import org.neuroph.contrib.imgrec.ImageRecognitionPlugin;
import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDouble;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import static asl.abdelouahed.UtilsConstants.MAX_VALUE;
import static asl.abdelouahed.UtilsConstants.THRESHOLD;
import static asl.abdelouahed.UtilsConstants.kernel;
import static org.opencv.imgproc.Imgproc.THRESH_BINARY;

/**
 * Created by abdelouahed on 2/15/18.
 */

public abstract class UtilsImage {

    public static void matToBinary(Mat mat) {
        Imgproc.threshold(mat, mat, THRESHOLD, MAX_VALUE, THRESH_BINARY);
        // morphological operation
        Imgproc.erode(mat, mat, kernel);
        // Gaussian Filter
        Imgproc.GaussianBlur(mat, mat, new Size(5, 5), 2);
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

    public static HashMap<String, Double> recognizeImage(ImageRecognitionPlugin imageRecognition, File file) {
        HashMap<String, Double> result = null;
        try {
            result = imageRecognition.recognizeImage(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    public static String getAnswerRecognition(HashMap<String, Double> result) {
        double highest = 0;
        String answer = "";
        for (Map.Entry<String, Double> entry : result.entrySet()) {
            if (entry.getValue() > highest) {
                highest = entry.getValue();
                answer = entry.getKey();
            }
        }
        return answer;
    }

    public static File bitmapToFile(Context context , Bitmap bitmap) {
        File fileDir = context.getFilesDir();
        File imageFile = new File(fileDir , "asl-abdelouahed.jpg");
        OutputStream os;
        try {
            os = new FileOutputStream(imageFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, os);
            os.flush();
            os.close();
        } catch (Exception e) {
        }
        return imageFile;
    }
}
