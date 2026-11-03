package uz.paykap.utils;

import lombok.experimental.UtilityClass;

import java.text.NumberFormat;
import java.util.Locale;

@UtilityClass
public class NumberUtil {

    private static final NumberFormat NUMBER_FORMAT = NumberFormat.getInstance(new Locale("ru", "RU"));

    public static String format(Double number) {
        return NUMBER_FORMAT.format(number);
    }

    public static String format(Long number) {

        if (number == null) {
            return NUMBER_FORMAT.format(0);
        }

        return NUMBER_FORMAT.format(number);
    }

    public static String formatCents(Long number) {

        if (number == null) {
            return format(0d);
        }

        return format(number / 100.0);
    }

}
