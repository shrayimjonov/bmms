package uz.paykap.service.cached;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import uz.devops.bmcore.cache.CacheEvictable;
import uz.devops.bmcore.dto.GatewayQueryResult;
import uz.devops.bmcore.projection.GatewayResultParamProjection;
import uz.javlon.commons.exception.CodedException;
import uz.javlon.commons.exception.ProblemEnum;
import uz.paykap.repository.AgentRepository;
import uz.paykap.repository.RecipientRepository;
import uz.paykap.service.mapper.GatewayMapper;

import java.util.List;

@Slf4j
@Service
@AllArgsConstructor
public class GatewaySelectorService implements CacheEvictable {

    public static final String GET_VALID_GATEWAY_QUERY_RESULT_V5 = "getValidGatewayQueryResultV5";

    private final RecipientRepository recipientRepository;
    private final GatewayMapper gatewayMapper;

    @Cacheable(value = GET_VALID_GATEWAY_QUERY_RESULT_V5, sync = true)
    public GatewayQueryResult getValidGatewayQueryResultV5(Long recipientId, String login, String requestGroup) {

        if (requestGroup == null || requestGroup.isEmpty()) {
            requestGroup = login;
        }

        List<GatewayResultParamProjection> gatewayQueryResults = recipientRepository.getGatewayQueryResultV5(recipientId, login, requestGroup, PageRequest.of(0, 1));

        if (gatewayQueryResults == null || gatewayQueryResults.isEmpty()) {
            throw new CodedException(ProblemEnum.SERVICE_NOT_FOUND, "agent-recipient with recipientId : " + recipientId + " and agent login : " + login + " not found");
        }

        /*
         * We need mapping from projection, bcz caching (Hazlcast) can not deserialize interfaces;
         * */

        GatewayQueryResult gatewayQueryResult = gatewayMapper.fromProjection(gatewayQueryResults.get(0));

        if (!gatewayQueryResult.getRecipientActive() || !gatewayQueryResult.getGatewayActive() || !gatewayQueryResult.getAgentRecipientActive()) {
            throw new CodedException(ProblemEnum.SERVICE_NOT_ACTIVE, "recipient : " + recipientId);
        }

        log.info("OK - GatewayQueryResult :\n{}", gatewayQueryResult);

        return gatewayQueryResult;
    }

    @Override
    @CacheEvict(value = {GET_VALID_GATEWAY_QUERY_RESULT_V5}, allEntries = true)
    public void evictAll() {
        log.info("Gateway selector caches " + GET_VALID_GATEWAY_QUERY_RESULT_V5 + " evicting...");
    }
}
