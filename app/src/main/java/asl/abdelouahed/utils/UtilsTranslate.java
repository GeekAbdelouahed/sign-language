package asl.abdelouahed.utils;

/**
 * Created by abdelouahed on 4/8/18.
 */

public abstract class UtilsTranslate {

    public static String translate(String value) {
        switch (value) {
            case "1":
                return "أ";
            case "2":
                return "ب";
            case "3":
                return "ت";
            case "4":
                return "ث";
            case "5":
                return "ج";
            case "6":
                return "ح";
            case "7":
                return "خ";
            case "8":
                return "د";
            case "9":
                return "ذ";
            case "10":
                return "ر";
            case "11":
                return "ز";
            case "12":
                return "س";
            case "13":
                return "ش";
            case "14":
                return "ص";
            case "15":
                return "ض";
            case "16":
                return "ط";
            case "17":
                return "ظ";
            case "18":
                return "ع";
            case "19":
                return "غ";
            case "20":
                return "ف";
            case "21":
                return "ق";
            case "22":
                return "ك";
            case "23":
                return "ل";
            case "24":
                return "م";
            case "25":
                return "ن";
            case "26":
                return "هـ";
            case "27":
                return "و";
            case "28":
                return "ي";
            default:
                return "";

        }
    }
}
