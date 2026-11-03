package uz.paykap.gateway.humo;

import org.apache.commons.lang3.StringUtils;

import static uz.paykap.service.humo.HumoP2pHelperService.MASTER_CARD_BIN;

public class HumoUtils {

    public static String mask(String pan) {
        if (pan != null && !pan.isEmpty() && !pan.contains("******")) {
            if (StringUtils.substring(pan, 0, 6).equals(MASTER_CARD_BIN)) {
                return StringUtils.substring(pan, 0, 8) + "****" + StringUtils.substring(pan, 12, 16);
            }
            return StringUtils.substring(pan, 0, 6) + "******" + StringUtils.substring(pan, 12, 16);
        } else {
            return pan;
        }

    }
}
