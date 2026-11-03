package uz.paykap.agent.tran.check;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uz.devops.bmcore.enumeration.RecipientType;

import java.util.Set;

@Slf4j
@Service(value = CommonGatewayCheckService.NAME)
public class CommonGatewayCheckService extends AbstractGatewayCheckService {

    public static final String NAME = "commonGatewayCheckService";

    @Override
    protected void afterCheckResult() {
        log.debug("After check result received, DO NOTHING.");
    }

    @Override
    protected Set<RecipientType> getSupportedTypes() {
        return Set.of(RecipientType.SIMPLE);
    }

}
