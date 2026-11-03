package uz.paykap.adapter.verification;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import uz.devops.bmcore.enumeration.TransactionStatus;
import uz.devops.pusher.event.PusherEvent;
import uz.paykap.config.ApplicationProperties;
import uz.paykap.event.dto.DangerType;
import uz.paykap.gateway.dto.GatewaySuccessErrorTran;
import uz.paykap.repository.TransactionRepository;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@AllArgsConstructor
@ConditionalOnProperty(prefix = "application.transactionVerifier", name = "active", havingValue = "true")
public class AutomaticTransactionVerifierService {

    private final static Integer VERIFY_LAST_MINUTES = 30;

    @Scheduled(cron = "${application.transactionVerifier.failedCountCron:0 */5 0 * * *}")
    public void startVerification() {
        log.info("----Verification time ! ----");
        if (properties.getTransactionVerifier().getActive()) {
            Integer transactionCount = transactionRepository.countTransactionByDateBetweenAndStatusOrStatus(LocalDateTime.now().minusSeconds(properties.getTransactionVerifier().getVerificationTime() / 1000).toInstant(ZoneOffset.ofHours(0)), LocalDateTime.now().toInstant(ZoneOffset.ofHours(0)), TransactionStatus.FAILED, TransactionStatus.CHECK_FAIL);

            if (transactionCount >= properties.getTransactionVerifier().getYellowDangerCount()) {
                DangerType dangerType = DangerType.YELLOW;
                if (transactionCount >= properties.getTransactionVerifier().getOrangeDangerCount()) {
                    dangerType = DangerType.ORANGE;
                }
                if (transactionCount >= properties.getTransactionVerifier().getRedDangerCount()) {
                    dangerType = DangerType.RED;
                }

                String message = "МНОГО ОШИБОЧНЫХ ТРАНЗАКЦИЙ! Тип опасности: " + dangerType.name() + " Количество неудачных транзакции: " + transactionCount;
                log.info(message);

//                applicationContext.publishEvent(new SystemFailureEvent(this, dangerType, properties.getTransactionVerifier().getRecipients(), properties.getTransactionVerifier().getPushType(), message));
                applicationContext.publishEvent(new PusherEvent(this, Collections.singletonMap("message", message), properties.getTransactionVerifier().getPusherTags().split(",")));
            }
        }

        log.info("----Verification ended ! ----");
    }

    @Scheduled(cron = "${application.transactionVerifier.failedByGatewayCron:0 */15 * * * *}")
    public void runVerificationEvery15minutes() {
        log.info("----Verification time ! ----");
        if (properties.getTransactionVerifier().getActive()) {

            Set<GatewaySuccessErrorTran> transCountByGateway = transactionRepository.getTransCountByGateway(Instant.now().minus(15, ChronoUnit.MINUTES), Instant.now(), properties.getTransactionVerifier().getErrorStatuses());

            log.debug("Result : {}", transCountByGateway);

            List<GatewaySuccessErrorTran> collect = transCountByGateway.stream().filter(gatewaySuccessErrorTran -> gatewaySuccessErrorTran.getPrc() >= properties.getTransactionVerifier().getLimitPercent() && gatewaySuccessErrorTran.getTotal() > properties.getTransactionVerifier().getMinTotalCount()).collect(Collectors.toList());

            if (collect.isEmpty()) {
                // No enough errors to notify
                log.info("No enough errors to notify");
                return;
            }

            StringBuilder s = new StringBuilder();
            for (GatewaySuccessErrorTran errorTran : collect) {
                s.append(errorTran.getGatewayName()).append(" : ").append(errorTran.getPrc()).append("% ").append("(").append(errorTran.getErrorCount()).append("/").append(errorTran.getTotal()).append(")");
            }

            String message = properties.getTransactionVerifier().getMessagePrefix() + s;

            log.info(message);

//            HashMap<String, String> hashMap = new HashMap<>();
//            hashMap.put("message", message);

//            applicationContext.publishEvent(new SendSmsEvent(this, message, properties.getTransactionVerifier().getRecipients().split(",")));
            applicationContext.publishEvent(new PusherEvent(this, Collections.singletonMap("message", message), properties.getTransactionVerifier().getPusherTags().split(",")));
        }

        log.info("----Verification ended ! ----");
    }

    private final ApplicationProperties properties;
    private final TransactionRepository transactionRepository;
    private final ApplicationContext applicationContext;
}
