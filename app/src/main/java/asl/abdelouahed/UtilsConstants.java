package asl.abdelouahed;

import org.opencv.core.CvType;
import org.opencv.core.Mat;

/**
 * Created by abdelouahed on 2/15/18.
 */

public abstract class UtilsConstants {

    public static final Mat kernel = Mat.ones(5, 5, CvType.CV_32F);
    public static int THRESHOLD = 250;
    public static final double MAX_VALUE = 255;

}
