package uz.devops.bmcore.dto;

import java.time.Instant;
import java.util.Map;

public interface HasGatewayResult {

    void setBillId(Long billId);

    void setSuccess(Boolean success);

    void setResponseJson(Map<String, Object> responseJson);

    void setResponseTime(Instant responseTime);

    void setResponseCode(String responseCode);

    void setResponseText(String responseText);

    void setExtId(String extId);

    Boolean isSuccess();

    Map<String, Object> getResponseJson();

    Instant getResponseTime();

    String getResponseCode();

    String getResponseText();

    String getExtId();

    Long getBillId();

}
