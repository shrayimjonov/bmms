package uz.paykap.service;

import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class GenerateNumberService {

    public static final String BILL_MS = "BillMs";

    public static final String SEPARATOR = "_";

    public String humoPaymentRef(String id) {
        return BILL_MS + SEPARATOR + id;
    }

    public String humoPaymentRef4Reco() {
        return humoPaymentRef4Reco(System.currentTimeMillis());
    }

    public String humoPaymentRef4Reco(Long id) {
        return humoPaymentRef(String.valueOf(id));
    }

    public String humoPaymentRef4Reco(String terminalId) {
        return humoSessionId("humoReco", terminalId);
    }

    public String humoSessionId(String serviceName, String id) {
        return BILL_MS + SEPARATOR + serviceName + SEPARATOR + id + SEPARATOR + Instant.now().toEpochMilli();
    }
}
