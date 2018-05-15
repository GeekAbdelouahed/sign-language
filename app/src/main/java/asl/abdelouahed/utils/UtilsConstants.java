package asl.abdelouahed.utils;

import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

/**
 * Created by abdelouahed on 2/15/18.
 */

public class UtilsConstants {

    private UtilsConstants() {
    }

    public static final int INPUT_SIZE = 299;
    public static final int IMAGE_MEAN = 128;
    public static final float IMAGE_STD = 128f;

    public static double MIN_CONFIDENCE = 0.4;

    public static final String INPUT_NAME = "Mul";
    public static final String OUTPUT_NAME = "final_result";
    public static final String MODEL_FILE = "file:///android_asset/output_graph.pb";
    public static final String LABEL_FILE = "file:///android_asset/output_labels.txt";

    public static final int GAUSSIAN_BLUR = 3;
    public static final Mat KERNEL = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(2, 2));
    public static int THRESHOLD = 150;
    public static final double MAX_VALUE = 255;

}
