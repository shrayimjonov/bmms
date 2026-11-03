package uz.paykap.utils;

import lombok.experimental.UtilityClass;

import java.util.regex.Pattern;

@UtilityClass
public class NciUtil {

    public static final String NCI_ACCOUNT_REGEXP = "\\d{20}|\\d{9}|\\d{12}";
    public static final String NCI_PHONE_REGEXP = "998\\d{9}";
    public static final String NCI_INN_REGEXP = "\\d{9}";

    public static final Pattern nciAccountPattern = Pattern.compile(NCI_ACCOUNT_REGEXP);

    public static boolean isValidRecipientPhoneNumber(String phone) {
        return phone != null && phone.matches(NCI_PHONE_REGEXP);
    }

    public static boolean isValidRecipientInn(String inn) {
        return inn != null && inn.matches(NCI_INN_REGEXP);
    }

}
