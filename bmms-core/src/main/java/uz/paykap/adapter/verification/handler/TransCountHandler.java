package uz.paykap.adapter.verification.handler;

import java.time.Instant;

public interface TransCountHandler {
    Long getGatewayTransCount(Instant fromTime, Instant toTime);

    Long getSystemTransCount(Instant fromTime, Instant toTime);
}
