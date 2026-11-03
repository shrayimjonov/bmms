package uz.paykap.agent.tran.helper;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uz.devops.bmcore.dto.GatewayQueryResult;
import uz.javlon.commons.result.CommonResultData;
import uz.javlon.commons.result.HasData;
import uz.paykap.service.dto.p2p.BmResultStatus;

@Slf4j
@Service
public class AgentHelper {

    public HasData<Integer> getAllowedCurrency4Agent(GatewayQueryResult gatewayQueryResult, Integer currency) {
        log.debug("Request currency : {}, allowed : {}", currency, gatewayQueryResult.getAgentCurrencies());
        if (!gatewayQueryResult.getAgentCurrencies().contains(currency)) {
            log.warn("Ccy {} not allowed for the agent, allowed only {}", currency, gatewayQueryResult.getAgentCurrencies());
//            throw new CodedException(ProblemEnum.INVALID_PARAMS, "currency not allowed : " + currency);
            return new CommonResultData<>(BmResultStatus.INVALID_PARAMS, null, "currency not allowed : " + currency);
        }
        return new CommonResultData<>(currency);
    }

}
