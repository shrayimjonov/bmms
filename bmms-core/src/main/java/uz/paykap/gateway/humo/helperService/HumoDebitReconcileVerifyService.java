package uz.paykap.gateway.humo.helperService;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uz.paykap.adapter.humo.HumoDebitDataAdapterV3;
import uz.paykap.config.ApplicationProperties;
import uz.paykap.domain.Gateway;
import uz.paykap.domain.ReconcileSummary;
import uz.paykap.domain.enumeration.ReconcilePeriod;
import uz.paykap.domain.enumeration.ReconcileSummaryStatus;
import uz.paykap.gateway.humo.HumoDebitGatewayServiceV3;
import uz.paykap.repository.GatewayRepository;
import uz.paykap.repository.ReconcileSummaryRepository;
import uz.paykap.repository.TransactionRepository;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@Service
@AllArgsConstructor
public class HumoDebitReconcileVerifyService {

    private final ReconcileSummaryRepository reconcileSummaryRepository;
    private final GatewayRepository gatewayRepository;
    private final TransactionRepository transactionRepository;
    private final ApplicationProperties properties;

    public void verify(Long amount, Long count) {
        log.debug("begin humoDebitGateway reconcile verify service");

        log.debug("find gateway by name: {}", HumoDebitGatewayServiceV3.NAME);
        Optional<Gateway> gatewayOptional = gatewayRepository.findTop1BySpringBeanNameAndActiveTrue(HumoDebitGatewayServiceV3.NAME);
        log.debug("gateway is: {}", gatewayOptional);

        log.debug("find reconcileSummary by gateway name: {}", HumoDebitDataAdapterV3.HUMO_DEBIT_GATEWAY_SHORT_NAME);
        Optional<ReconcileSummary> optionalOldReconcileSummary = reconcileSummaryRepository.findTop1ByGatewayNameOrderByCreatedDateDesc(HumoDebitDataAdapterV3.HUMO_DEBIT_GATEWAY_SHORT_NAME);
        log.debug("reconcileSummary is: {}", optionalOldReconcileSummary);

        Instant fromDate = optionalOldReconcileSummary.isPresent() ? optionalOldReconcileSummary.get().getCreatedDate() : Instant.EPOCH;
        Instant toDate = Instant.now();
        log.debug("reconcile for fromDate: {} and toDate: {}", fromDate, toDate);

        Long transactionAmount = transactionRepository.getSumSuccessTransactionGatewayAmountByRecipientNameAndPeriod(fromDate, toDate, HumoDebitDataAdapterV3.HUMO_DEBIT_RECIPIENT_SHORT_NAME);
        Long transactionCount = transactionRepository.getCountSuccessTransactionIdByRecipientNameAndPeriod(fromDate, toDate, HumoDebitDataAdapterV3.HUMO_DEBIT_RECIPIENT_SHORT_NAME);
        log.debug("System transactionAmount is: {},  transactionCount is: {}", transactionAmount, transactionCount);

        if (properties.getReconciliation().isSkipSaveZeroValues() && Objects.equals(transactionCount, 0L) && Objects.equals(count, 0L) && Objects.equals(transactionAmount, 0L) && Objects.equals(amount, 0L)) {
            log.debug("Skipped saving reconcileSummary, bcz all counts and amounts are equal to 0");
            return;
        }

        Boolean success = Objects.equals(transactionAmount, amount) && Objects.equals(transactionCount, count);
        ReconcileSummaryStatus status = success ? ReconcileSummaryStatus.COMPARED_OK : ReconcileSummaryStatus.COMPARED_FALSE;

        ReconcileSummary reconcileSummary = new ReconcileSummary()
                .gatewayId(gatewayOptional.isPresent() ? gatewayOptional.get().getId() : -998L)
                .systemAmount(transactionAmount)
                .gatewayAmount(amount)
                .systemCount(transactionCount)
                .gatewayCount(count)
                .date(LocalDate.now())
//                .status(ReconcileSummaryStatus.NEW)
                .period(ReconcilePeriod.HALF_DAY)
//                .success(false)
//                .date(LocalDate.now())
                .success(success)
                .status(status)
                .gatewayName(HumoDebitDataAdapterV3.HUMO_DEBIT_GATEWAY_SHORT_NAME);

        log.info("verify reconcile is success: {}", success);
        log.debug("save reconcileSummary: {}", reconcileSummary);
        reconcileSummaryRepository.save(reconcileSummary);
    }
}
