package uz.paykap.agent.tran;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import uz.devops.bmcore.gateway.GatewayBillingService;

import javax.annotation.PostConstruct;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@AllArgsConstructor
public class GatewayBillingServiceResolver {

    private final ApplicationContext applicationContext;
    private Map<String, GatewayBillingService> map;

    @PostConstruct
    public void init() {
        map = applicationContext.getBeansOfType(GatewayBillingService.class);
        log.info("Inited gateway billing services : {}", map);
    }

    public Optional<GatewayBillingService> get(String name) {

        if (name == null || name.isEmpty()) {
            return Optional.empty();
        }

        return Optional.ofNullable(map.get(name));
    }

}
