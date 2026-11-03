package uz.paykap.adapter.verification.handler;

import java.time.Instant;

public interface TransSummaryHandler {

    /*
    * if no related method to get total amount from gateway, return null or negative number
    * */
    Long getGatewaySummaryTiyin(Instant fromTime, Instant toTime);

    Long getSystemSummaryTiyin(Instant fromTime, Instant toTime);

    /*
     * if no related method to get total amount from gateway, return null or negative number
     * */
    Long getGatewayTransCount(Instant fromTime, Instant toTime);

    Long getSystemTransCount(Instant fromTime, Instant toTime);
}
