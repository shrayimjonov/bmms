package uz.paykap.utils;

import lombok.experimental.UtilityClass;
import uz.devops.bmcore.enumeration.PayType;

import java.util.regex.Pattern;

@UtilityClass
public class PcUtil {

//    public static final String HUMO_PAN_REGEXP = "(9860\\d{12})|(555536\\d{10})";
//    public static final String HUMO_TOKEN_REGEX = "(9860\\d{2}[A-Z]{6}\\d{4})|(5555\\d{2}[A-Z]{6}\\d{4})";

//    public static final String MASTER_CARD_PAN_REGEXP = "555536\\d{10}";
//    public static final String MASTER_CARD_TOKEN_REGEX = "5555\\d{2}[A-Z]{6}\\d{4}";

//    public static final String VISA_CARD_PAN_REGEXP = "(460294|490957)\\d{10}";
//    public static final String VISA_CARD_TOKEN_REGEX = "(460294|490957)[A-Z]{6}\\d{4}";

//    public static final String UZCARD_PAN_REGEXP = "8600\\d{12}";
    public static final String UZCARD_FOREIGN_REGEXP = "^(?=8600)(?!860063).*$";
    public static final String UZCARD_TOKEN_REGEXP = "[A-Z0-9]{32}|AAAAAAAAAA";

    public static final String TKB_TOKEN_REGEXP = "^[0-9]{5,12}$";

//    public static final Pattern humoPanPattern = Pattern.compile(HUMO_PAN_REGEXP);
//    public static final Pattern humoTokenPattern = Pattern.compile(HUMO_TOKEN_REGEX);

//    public static final Pattern masterCardPanPattern = Pattern.compile(MASTER_CARD_PAN_REGEXP);
//    public static final Pattern masterCardTokenPattern = Pattern.compile(MASTER_CARD_TOKEN_REGEX);

//    public static final Pattern davrVisaCardPanPattern = Pattern.compile(VISA_CARD_PAN_REGEXP);
//    public static final Pattern davrVisaCardTokenPattern = Pattern.compile(VISA_CARD_TOKEN_REGEX);

//    public static final Pattern uzcardPanPattern = Pattern.compile(UZCARD_PAN_REGEXP);
    public static final Pattern uzcardForeignPattern = Pattern.compile(UZCARD_FOREIGN_REGEXP);
//    public static final Pattern uzcardTokenPattern = Pattern.compile(UZCARD_TOKEN_REGEXP);
    public static final Pattern tkbTokenPattern = Pattern.compile(TKB_TOKEN_REGEXP);

    @Deprecated
    public static boolean isForeignUzcardBin(String binOrPan) {
        return uzcardForeignPattern.matcher(binOrPan).matches();
    }

//    @Deprecated
//    public static boolean isValidHumoPan(String pan) {
//        return pan != null && humoPanPattern.matcher(pan).matches();
//    }

//    @Deprecated
//    public static boolean isValidHumoToken(String token) {
//        return token != null && humoTokenPattern.matcher(token).matches();
//    }

//    @Deprecated
//    public static boolean isValidUzcardPan(String pan) {
//        return pan != null && uzcardPanPattern.matcher(pan).matches();
//    }

//    @Deprecated
//    public static boolean isValidUzcardToken(String token) {
//        return token != null && uzcardTokenPattern.matcher(token).matches();
//    }

//    @Deprecated
//    public static boolean isValidTkbToken(String token) {
//        return token != null && tkbTokenPattern.matcher(token).matches();
//    }

//    public static PayType getPayType(String s) {
//        if (isValidUzcardToken(s) || isValidUzcardPan(s)) {
//            return PayType.UZCARD;
//        } else if (isValidHumoPan(s) || isValidHumoToken(s)) {
//            return PayType.HUMO;
//        }
//
//        return PayType.UNKNOWN;
//    }

//    @Deprecated
//    public static boolean isValidUzcardId(String id) {
//        return isValidUzcardToken(id) || isValidUzcardPan(id);
//    }

//    @Deprecated
//    public static boolean isValidPcSenderId(String senderId) {
//        PayType payType = getPayType(senderId);
//        return payType == PayType.HUMO || payType == PayType.UZCARD;
//    }

}
