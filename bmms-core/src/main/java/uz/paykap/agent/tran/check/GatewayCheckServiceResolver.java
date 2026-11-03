package uz.paykap.agent.tran.check;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import uz.devops.bmcore.dto.GatewayQueryResult;
import uz.devops.bmcore.dto.request.CommonCheckRequest;
import uz.devops.bmcore.dto.response.SimpleBillResponse;
import uz.devops.bmcore.enumeration.RecipientType;

import javax.annotation.PostConstruct;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Slf4j
@Service
@AllArgsConstructor
public class GatewayCheckServiceResolver {

    private final CommonGatewayCheckService commonGatewayCheckService;
    private final ApplicationContext applicationContext;
    private final Map<RecipientType, AbstractGatewayCheckService> map = new HashMap<>();

    @PostConstruct
    public void init() {

        var beans = applicationContext.getBeansOfType(AbstractGatewayCheckService.class);

        beans.forEach((key, value) -> {
            if (!Objects.equals(key, CommonGatewayCheckService.NAME)) {
                for (RecipientType supportedType : value.getSupportedTypes()) {
                    map.put(supportedType, value);
                }
            }
        });

        log.info("Inited gateway check services : {}", map);
    }

    public SimpleBillResponse check(CommonCheckRequest checkDTO, String login, GatewayQueryResult gatewayQueryResult, Instant tranDate) {

        var checkService = map.get(gatewayQueryResult.getRecipientType());

        if (checkService == null) {
            return commonGatewayCheckService.check(checkDTO, login, gatewayQueryResult, tranDate);
        }

        return checkService.check(checkDTO, login, gatewayQueryResult, tranDate);
    }

}
