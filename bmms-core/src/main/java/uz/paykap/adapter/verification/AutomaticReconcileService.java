package uz.paykap.adapter.verification;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import uz.devops.bmcore.enumeration.TransactionStatus;
import uz.javlon.commons.base.Result;
import uz.paykap.config.ApplicationProperties;
import uz.paykap.gateway.GatewayServiceResolver;
import uz.paykap.repository.GatewayRepository;
import uz.paykap.repository.TransactionRepository;
import uz.paykap.utils.DateUtil;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
@AllArgsConstructor
@ConditionalOnProperty(prefix = "application.reconciliation", name = "enableAuto", havingValue = "true")
public class AutomaticReconcileService {

    private final GatewayRepository gatewayRepository;
    private final GatewayServiceResolver gatewayServiceResolver;
    private final TransactionRepository transactionRepository;
    private final HumoReconcileService humoReconcileService;
    private final ReconciliationServiceV3 reconciliationServiceV3;
    private final ApplicationProperties properties;

//    private final static DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'");

    //    @Scheduled(cron = "0 05 0 * * ?")
    public void startReconciliation() {
        log.info("----Reconciliation time ! ----");

        var springBeanName = gatewayRepository.findDistinctSpringBeanName();

        if (springBeanName == null || springBeanName.size() == 0) {
            log.info("No active gateways found to reconcile for dates {} and {}!", DateUtil.getPrevDayBegin(), DateUtil.getPrevDayEnd());
            return;
        }

        for (String name : springBeanName) {
            log.debug("This gateway : " + name + " reconciling");
            try {
                gatewayServiceResolver.reconcile(DateUtil.getPrevDayBegin(), DateUtil.getPrevDayEnd(), name);
            } catch (Exception e) {
                log.debug("Error reconcile with gateway :" + springBeanName + " , error : " + e.getLocalizedMessage());
            }
        }
        log.info("----Reconciliation ended ! ----");
    }

    @Scheduled(cron = "0 0 1 * * ?")
    public void startDailyReconciliationV3() {

        if (!properties.getReconciliation().isEnableAuto()) {
            log.info("Auto reconciliation is disabled by prop 'application.reconciliation.enableAuto=false'");
            return;
        }

        Result result = reconciliationServiceV3.startReconcileByGateway(DateUtil.getPrevDayBegin(), DateUtil.getPrevDayEnd());
    }

    @Scheduled(cron = "${application.reconciliation.humoTerminalCron:0 0 3,8,15,20 * * ?}")
    public void startHumoReconciliation() {

        if (!properties.getReconciliation().isEnableAuto()) {
            log.info("Auto reconciliation is disabled by prop 'application.reconciliation.enableAuto=false'");
            return;
        }

        log.info("--- Reconciliation humo time! ---");

        var terminalIds = transactionRepository.getHumoTerminalIds(Instant.now().minus(2, ChronoUnit.DAYS));
        log.info("Terminals to reconcile: {}", terminalIds);

        for (String terminalId : terminalIds) {
            var bill = humoReconcileService.reconcile(terminalId);
            log.info("Reco Bill status: {}", bill.isSuccess());
        }

        log.info("!-!-! P2p terminals reconcile !-!-!");

        var p2pTerminals = getP2pTerminals();
        for (String p2pTerminal : p2pTerminals) {
            var bill = humoReconcileService.reconcile(p2pTerminal);
            log.info("Reco Bill status: {}", bill.isSuccess());
        }
        log.info("=== Reconciliation humo end");
    }

    public List<String> getP2pTerminals() {
        log.info("Get p2p terminals");

        var result = new ArrayList<String>();

        log.debug("Get sender humo terminals");
        var p2pTerminals = transactionRepository.findAllP2pSenderHumoTerminals(TransactionStatus.SUCCESS, Instant.now().minus(1, ChronoUnit.DAYS));
        log.debug("p2p sender humo terminals : {}", p2pTerminals);

        for (String p2pTerminal : p2pTerminals) {
            if (p2pTerminal == null || p2pTerminal.isEmpty()) {
                log.warn("p2p terminal null or empty");
                continue;
            }
            var p2pTerminalsArray = StringUtils.split(p2pTerminal, "->");

            var senderTerminal = p2pTerminalsArray[0];
            if (!senderTerminal.isEmpty() && !Objects.equals(senderTerminal, "null") && !result.contains(senderTerminal)) {
                result.add(senderTerminal);
            }

        }
        log.debug("Get recipient humo terminals");
        var p2pTerminalsRecipient = transactionRepository.findAllP2pRecipientHumoTerminals(TransactionStatus.SUCCESS, Instant.now().minus(1, ChronoUnit.DAYS));
        log.debug("p2p recipient humo terminals : {}", p2pTerminals);

        for (String p2pTerminal : p2pTerminalsRecipient) {
            if (p2pTerminal == null || p2pTerminal.isEmpty()) {
                log.warn("p2p terminal null or empty");
                continue;
            }
            var p2pTerminalsArray = StringUtils.split(p2pTerminal, "->");

            var recipientTerminal = p2pTerminalsArray[0];
            if (p2pTerminalsArray.length == 2) {
                recipientTerminal = p2pTerminalsArray[1];
            }

            if (!recipientTerminal.isEmpty() && !Objects.equals(recipientTerminal, "null") && !result.contains(recipientTerminal)) {
                result.add(recipientTerminal);
            }
        }
        return result;
    }

}
