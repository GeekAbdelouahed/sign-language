package asl.abdelouahed.utils;

/**
 * Created by abdelouahed on 4/8/18.
 */

public class UtilsTranslate {

    private UtilsTranslate() {
    }

    public static String translate(String value) {
        if (value.equals("1"))
            return "أ";
        if (value.equals("2"))
            return "ب";
        if (value.equals("3"))
            return "ت";
        if (value.equals("4"))
            return "ث";
        if (value.equals("5"))
            return "ج";
        if (value.equals("6"))
            return "ح";
        if (value.equals("7"))
            return "خ";
        if (value.equals("8"))
            return "د";
        if (value.equals("9"))
            return "ذ";
        if (value.equals("10"))
            return "ر";
        if (value.equals("11"))
            return "ز";
        if (value.equals("12"))
            return "س";
        if (value.equals("13"))
            return "ش";
        if (value.equals("14"))
            return "ص";
        if (value.equals("15"))
            return "ض";
        if (value.equals("16"))
            return "ط";
        if (value.equals("17"))
            return "ظ";
        if (value.equals("18"))
            return "ع";
        if (value.equals("19"))
            return "غ";
        if (value.equals("20"))
            return "ف";
        if (value.equals("21"))
            return "ق";
        if (value.equals("22"))
            return "ك";
        if (value.equals("23"))
            return "ل";
        if (value.equals("24"))
            return "م";
        if (value.equals("25"))
            return "ن";
        if (value.equals("26"))
            return "هـ";
        if (value.equals("27"))
            return "و";
        if (value.equals("28"))
            return "ي";
        return "";

    }
}
