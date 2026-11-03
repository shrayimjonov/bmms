package uz.paykap.adapter.verification;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import uz.javlon.commons.base.Result;
import uz.paykap.config.ApplicationProperties;
import uz.paykap.utils.DateUtil;

@Slf4j
@Service
@AllArgsConstructor
@ConditionalOnProperty(prefix = "application.reconciliation", name = "enableAuto", havingValue = "true")
public class AutomaticP2pReconcileSummaryService {

    private final ReconciliationServiceV3 reconciliationServiceV3;
    private final ApplicationProperties properties;

    @Scheduled(cron = "0 20 * * * *")
    public void startHourlyReconciliationV3() {

        if (!properties.getReconciliation().isEnableAuto()) {
            log.info("Auto reconciliation is disabled by prop 'application.reconciliation.enableAuto=false'");
            return;
        }

        Result result = reconciliationServiceV3.startReconcileByP2p(DateUtil.getPrevHourBegin(), DateUtil.getPrevHourEnd());
    }
}
