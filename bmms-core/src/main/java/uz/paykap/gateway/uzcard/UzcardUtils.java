package uz.paykap.gateway.uzcard;

import uz.javlon.svgate.dto.TranDTO;
import uz.paykap.config.Constants;

import java.util.HashMap;

public class UzcardUtils {

    //    public static final String REFNUM = "refnum";
    public static final String DATE = "date";
    public static final String STAN = "stan";
    public static final String STATUS = "status";

    public static void setTranData(TranDTO tranDTO, HashMap<String, Object> data) {
        data.put(Constants.REFERENCE_NUMBER_PARAM, tranDTO.getRefNum());
        data.put(DATE, String.valueOf(tranDTO.getDate7()));
        data.put(STAN, String.valueOf(tranDTO.getStan()));
        data.put(STATUS, String.valueOf(tranDTO.getResp()));
//        data.put("statusText", StringUtils.substring(tranDTO.getRespDesc(), 0, 128));
    }
}
