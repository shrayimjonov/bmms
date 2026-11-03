package uz.paykap.utils;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import uz.devops.bmcore.reconciliation.dto.HasRecoSummary;
import uz.javlon.commons.base.ResultData;
import uz.paykap.domain.Recipient;
import uz.paykap.domain.ReconcileDiscrepancy;
import uz.paykap.domain.ReconcileSummary;
import uz.paykap.domain.enumeration.ReconcileSummaryStatus;
import uz.paykap.event.dto.AgentReconcileEventArg;
import uz.paykap.repository.RecipientRepository;
import uz.paykap.repository.ReconcileDiscrepancyRepository;
import uz.paykap.repository.ReconcileSummaryRepository;
import uz.paykap.repository.TransactionRepository;
import uz.paykap.service.ReconcileDiscrepancyService;
import uz.paykap.service.ReconcileSummaryService;
import uz.paykap.service.dto.ReconcileSummaryDTO;
import uz.paykap.service.dto.reconcile.AgentReconcileStatusResponse;
import uz.paykap.service.dto.reconcile.ComplexReconcileTransaction;
import uz.paykap.service.dto.reconcile.SimpleReconcileTransaction;
import uz.paykap.service.dto.reconcile.enumeration.AgentReconcileStatus;

import javax.validation.constraints.NotNull;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

@Slf4j
@Component
@AllArgsConstructor
public class AgentReconcileUtil {

    private static final HashSet<SimpleReconcileTransaction> EMPTY_TRANSACTION_SET = new HashSet<>();
    private static final long EMPTY_AMOUNT = 0L;

    private final ReconcileSummaryRepository summaryRepository;
    private final ReconcileDiscrepancyRepository discrepancyRepository;
    private final TransactionRepository transactionRepository;
    private final ReconcileDiscrepancyService discrepancyService;
    private final ReconcileSummaryService summaryService;
    private final RecipientRepository recipientRepository;
    private final ApplicationEventPublisher eventPublisher;

    public AgentReconcileStatus getReconciliationByRecipientIdAndStatusProcessing(Long recipientId) {
        log.trace("getReconciliationStatus started for reconcile id : {}", recipientId);

        if (summaryRepository.findByRecipientId(recipientId).isPresent()) {
            return AgentReconcileStatus.PROCESS_BUSY;
        }

        return AgentReconcileStatus.SUCCESS;
    }

    public void changeReconciliationStatus(Long reconcileId) {
        log.trace("changeReconciliationStatus started for reconcile id : {}", reconcileId);

        summaryRepository.findTop1ByIdAndStatus(reconcileId, ReconcileSummaryStatus.NEW).ifPresent(summary -> {
            summaryRepository.save(summary.status(ReconcileSummaryStatus.PROCESSING));
        });
    }

    public HasRecoSummary getSystemTranSummary(String agentName, @NotNull final Instant from, @NotNull final Instant to) {
        log.debug("getReconcileSummary started");
        var result = transactionRepository.getSummaryBySubgroup(agentName, from, to);
        log.debug("reconcile summary got : {}", result);
        return result;
    }

    public Set<ComplexReconcileTransaction> getSystemTransactions(String agentName, @NotNull final Instant from, @NotNull final Instant to) {
        log.debug("getSystemTransactions started");
        var result = transactionRepository.getReconcileTransactionsBySubgroup(agentName, from, to);
        log.debug("Transactions found for subgroup {} with count : {}", agentName, result.size());
        return result;
    }

    public Set<SimpleReconcileTransaction> getGatewayDiscrepancies(ReconcileSummary summary) {
        log.debug("getGatewayDiscrepancies started");
        return discrepancyRepository.getGatewayDiscrepancies(summary.getId());
    }

    public Set<SimpleReconcileTransaction> getSystemDiscrepancies(ReconcileSummary summary) {
        log.debug("getSystemDiscrepancies started");
        return discrepancyRepository.getSystemDiscrepancies(summary.getId());
    }

    public void saveDiscrepancies(Set<ComplexReconcileTransaction> differences, ReconcileSummary reconcileSummary) {
        log.debug("saveReconcileDiscrepancy started for transactions with count : {}", differences.size());

        for (var transaction : differences) {
            try {
                createDiscrepancy(transaction, reconcileSummary);
            } catch (Exception e) {
                log.warn("discrepancy save error for transaction : " + transaction, e);
            }
        }
    }

    public ReconcileDiscrepancy createDiscrepancy(ComplexReconcileTransaction transaction, ReconcileSummary reconcileSummary) {
        log.debug("saving reconcile discrepancy for transaction :{}", transaction.getBillTranId());

        var reconcileDiscrepancy = getDiscrepancy(transaction);
        init(transaction, reconcileDiscrepancy);
        setCommonProperties(reconcileSummary, reconcileDiscrepancy, transaction);

        return discrepancyService.save(reconcileDiscrepancy);
    }

    public void successfulReconcileAction(Long recId, Set<ComplexReconcileTransaction> agentTransactions, String agentName, Instant to, HasRecoSummary systemTranSummary, double agentTotalSum, Long gatewayId) {
        log.debug("successfulReconcileAction started");

        summaryService.saveReconcileSummary(recId, true, agentName, to, Double.valueOf(agentTotalSum).longValue(),
            (long) agentTransactions.size(), systemTranSummary.getTotalAmountTiyin(), systemTranSummary.getTotalCount(),
            ReconcileSummaryStatus.COMPARED_OK, gatewayId);

        eventPublisher.publishEvent(new AgentReconcileEventArg(this, agentName, new ResultData<>(true, null, null)));
    }

    public AgentReconcileStatusResponse resolveResult(ReconcileSummary reconcile) {
        log.trace("resolveResult started");
        log.debug("reconcile status is : {}", reconcile.getStatus());

        switch (reconcile.getStatus()) {
            case COMPARED_OK:
                return buildCommonTranEmptyResponse(AgentReconcileStatus.SUCCESS, true);
            case COMPARED_FALSE:
                var gwDiscrepancies = getGatewayDiscrepancies(reconcile);
                var systemDiscrepancies = getSystemDiscrepancies(reconcile);
                return buildFailResponse(systemDiscrepancies, gwDiscrepancies);
            default:
                return buildCommonTranEmptyResponse(AgentReconcileStatus.PROCESSING, true);
        }
    }

    public AgentReconcileStatusResponse buildCommonTranEmptyResponse(AgentReconcileStatus status, boolean isSuccess) {
        log.trace("getEmptyTranStatusResponse started");
        return new AgentReconcileStatusResponse(EMPTY_TRANSACTION_SET, EMPTY_TRANSACTION_SET, isSuccess, status.getCode(), status.getMessage());
    }

    public Recipient getRecipient(Long recipientId) {
        log.trace("getGateway started");
        return recipientRepository
            .findTop1ByIdAndActiveIsTrue(recipientId)
            .orElse(null);
    }

    public ReconcileSummaryDTO getReconcileSummaryDTO(Recipient recipient) {
        var reconcile = new ReconcileSummaryDTO();
        reconcile.setGatewayId(recipient.getGateway().getId());
        reconcile.setSystemAmount(0L);
        reconcile.setGatewayAmount(0L);
        reconcile.setSystemCount(0L);
        reconcile.setGatewayCount(0L);
        reconcile.setStatus(ReconcileSummaryStatus.NEW);
        return reconcile;
    }

    private AgentReconcileStatusResponse buildFailResponse(Set<SimpleReconcileTransaction> billTrans, Set<SimpleReconcileTransaction> agentTrans) {
        log.trace("buildTranStatusResponse started");
        return new AgentReconcileStatusResponse(billTrans, agentTrans, false, AgentReconcileStatus.FAILED.getCode(), AgentReconcileStatus.FAILED.getMessage());
    }

    private ReconcileDiscrepancy getDiscrepancy(ComplexReconcileTransaction transaction) {
        log.trace("getDiscrepancy started");

        return transaction.getAgentTransaction()
            ? new ReconcileDiscrepancy()
            .gatewayTransactionId(transaction.getAgentTranId())
            .systemTransactionId(-1L)
            : new ReconcileDiscrepancy()
            .systemTransactionId(Long.valueOf(transaction.getBillTranId()))
            .gatewayTransactionId("-1L");
    }

    private void init(ComplexReconcileTransaction transaction, ReconcileDiscrepancy reconcileDiscrepancy) {
        log.trace("init started");
        if (transaction.getAgentTransaction()) {
            initAsGatewayTransaction(transaction, reconcileDiscrepancy);
        } else {
            initAsSystemTransaction(transaction, reconcileDiscrepancy);
        }
    }

    private void setCommonProperties(ReconcileSummary reconcileSummary, ReconcileDiscrepancy discrepancy, ComplexReconcileTransaction transaction) {
        log.trace("initCommonProperties started");
        log.debug("reconcileSummary : {}", reconcileSummary);
        log.debug("discrepancy : {}", discrepancy);
        log.debug("transaction : {}", transaction);

        discrepancy.reconcileSummary(reconcileSummary)
                .gatewayName(reconcileSummary.getGatewayName())
                .comment(transaction.getAccount());

    }

    private void initAsSystemTransaction(ComplexReconcileTransaction transaction, ReconcileDiscrepancy reconcileDiscrepancy) {
        log.trace("initAsSystemTransaction started");
        reconcileDiscrepancy
            .systemAmount(transaction.getAmount())
            .systemStatus(true)
            .gatewayAmount(EMPTY_AMOUNT)
            .gatewayStatus(false)
            .systemDate(transaction.getDate());
    }

    private void initAsGatewayTransaction(ComplexReconcileTransaction transaction, ReconcileDiscrepancy reconcileDiscrepancy) {
        log.trace("initAsGatewayTransaction started");
        reconcileDiscrepancy
            .gatewayAmount(transaction.getAmount())
            .gatewayStatus(true)
            .systemAmount(EMPTY_AMOUNT)
            .systemStatus(false)
            .gatewayDate(transaction.getDate());
    }
}
