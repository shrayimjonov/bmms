package uz.paykap.service.p2p.support;

import java.util.Map;

public interface BillHandler {

    void setRequest(Map<String, Object> request);

    void error();

    void error(String responseCode, String responseText);

    void error(Map<String, Object> response);

    void success();

    void success(Map<String, Object> response);

    Long getBillId();
}
