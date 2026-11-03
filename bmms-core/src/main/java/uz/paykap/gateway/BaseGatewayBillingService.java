package uz.paykap.gateway;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import uz.devops.bmcore.dto.GatewayResult;

import java.time.Instant;
import java.util.Collections;

@Slf4j
public class BaseGatewayBillingService {

    protected GatewayResult getErrorGatewayResult(String errorText) {
        return getErrorGatewayResult(new GatewayResult(), errorText);
    }

    protected GatewayResult getErrorGatewayResult(GatewayResult gatewayResult, String errorText) {
        log.error(errorText);
        return gatewayResult
            .responseJson(Collections.singletonMap("error", errorText))
            .responseTime(Instant.now())
            .responseText(StringUtils.substring(errorText, 0, 126))
            .responseCode("FAILED")
            .success(false)
            ;
    }

}
