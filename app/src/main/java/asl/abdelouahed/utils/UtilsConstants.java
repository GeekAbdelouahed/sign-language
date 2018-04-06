package asl.abdelouahed.utils;

import org.opencv.core.CvType;
import org.opencv.core.Mat;

/**
 * Created by abdelouahed on 2/15/18.
 */

public abstract class UtilsConstants {

    public static final int INPUT_SIZE = 299;
    public static final int IMAGE_MEAN = 117;
    public static final float IMAGE_STD = 1;

    public static final String INPUT_NAME = "Mul";
    public static final String OUTPUT_NAME = "final_result";
    public static final String MODEL_FILE = "file:///android_asset/output_graph.pb";
    public static final String LABEL_FILE = "file:///android_asset/output_labels.txt";

    public static final Mat KERNEL = Mat.ones(5, 5, CvType.CV_32F);
    public static int THRESHOLD = 250;
    public static final double MAX_VALUE = 255;

}
