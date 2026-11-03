package uz.paykap.utils;

import com.google.common.collect.Sets;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;
import uz.devops.bmcore.reconciliation.dto.HasRecoSummary;
import uz.javlon.commons.base.ResultData;
import uz.paykap.domain.ReconcileSummary;
import uz.paykap.domain.enumeration.ReconcileSummaryStatus;
import uz.paykap.event.dto.AgentReconcileEventArg;
import uz.paykap.repository.ReconcileSummaryRepository;
import uz.paykap.repository.TransactionRepository;
import uz.paykap.security.SecurityUtils;
import uz.paykap.service.ReconcileSummaryService;
import uz.paykap.service.dto.ReconcileSummaryDTO;
import uz.paykap.service.dto.reconcile.*;
import uz.paykap.service.dto.reconcile.enumeration.AgentReconcileStatus;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.Instant;
import java.util.Set;
import java.util.function.Consumer;

@Slf4j
@Component
@RequiredArgsConstructor
public class AgentReconcileV2Util {

    public static final long GATEWAY_ID = -998L;
    private final AgentReconcileUtil util;
    private final ReconcileSummaryRepository summaryRepository;
    private final TransactionRepository transactionRepository;
    private final ReconcileSummaryService summaryService;
    private final ApplicationEventPublisher eventPublisher;

    public AgentReconcileStatus getReconciliation(String producer) {
        log.trace("getReconciliation started for name id : {}", producer);

        if (summaryRepository.findTopByGatewayNameAndStatusOrderByIdDesc(producer, ReconcileSummaryStatus.PROCESSING).isPresent()) {
            return AgentReconcileStatus.PROCESS_BUSY;
        }

        return AgentReconcileStatus.SUCCESS;
    }

    public HasRecoSummary getSystemTranSummary(String agentName, String group, String subgroup,
                                               @NotNull final Instant from, @NotNull final Instant to) {
        log.debug("getReconcileSummary started");
        var result = transactionRepository.getSummaryByGroup(agentName, group, subgroup, from, to);
        log.debug("reconcile summary got : {}", result);
        return result;
    }

    public String getGatewayName(String login, String group, String subgroup) {
        log.trace("getGatewayName started for login '{}', group : '{}' and subgroup : '{}'", login, group, subgroup);
        return String.format("%s.%s.%s", login, group, subgroup);
    }

    public ReconcileSummary getReconcileSummary(@Size Long reconcileId) {
        log.trace("getReconcileSummary started for reconcile - '{}' ", reconcileId);
        return summaryRepository.findById(reconcileId).orElse(null);
    }

    public Set<ComplexReconcileTransaction> getSystemTransactions(String agentName, String group, String subgroup,
                                                                  @NotNull final Instant from, @NotNull final Instant to) {
        log.debug("getSystemTransactions started");
        var result = transactionRepository.getReconcileTransactionsByGroup(agentName, group, subgroup, from, to);
        log.debug("Transactions found for subgroup {} with count : {}", agentName, result.size());
        return result;
    }

    public ReconcileSummaryDTO createReconcileSummaryDTO(String agentCredentials) {
        log.trace("createReconcileSummaryDTO started for agent : {}", agentCredentials);
        var reconcile = new ReconcileSummaryDTO();
        reconcile.setGatewayId(GATEWAY_ID);
        reconcile.setGatewayName(agentCredentials);
        reconcile.setSystemAmount(0L);
        reconcile.setGatewayAmount(0L);
        reconcile.setSystemCount(0L);
        reconcile.setGatewayCount(0L);
        reconcile.setStatus(ReconcileSummaryStatus.NEW);
        return reconcile;
    }

    public void changeReconciliationStatus(Long reconcileId) {
        log.trace("changeReconciliationStatus started for reconcile id : {}", reconcileId);
        util.changeReconciliationStatus(reconcileId);
    }

    public Sets.SetView<ComplexReconcileTransaction> getTransactionDifference(AgentReconcileRequestV2 request, Set<ComplexReconcileTransaction> systemTransactions) {
        log.trace("getTransactionDifference started");
        return Sets.symmetricDifference(systemTransactions, request.getTransactions());
    }

    public AgentReconcileStatusResponse buildCommonTranEmptyResponse(AgentReconcileStatus status, boolean isSuccess) {
        log.trace("getEmptyTranStatusResponse started");
        return util.buildCommonTranEmptyResponse(status, isSuccess);
    }

    public AgentReconcileStatusResponse resolveResult(ReconcileSummary reconcile) {
        log.trace("resolveResult started");
        return util.resolveResult(reconcile);
    }

    public void saveReconcileSummary(Object supplier,
                                     RecoSummaryParam summaryParam,
                                     Set<ComplexReconcileTransaction> difference,
                                     Consumer<Pair<Set<ComplexReconcileTransaction>, ReconcileSummary>> differenceHandler) {
        log.debug("saveReconcileSummary started for summary : {}", summaryParam);

        var summary = summaryService.saveReconcileSummary(summaryParam,
                () -> this.getReconcileSummary(summaryParam.getReconcileId()));

        if (!summaryParam.getIsSuccess() && !difference.isEmpty()) {
            log.trace("handling difference");
            differenceHandler.accept(ImmutablePair.of(difference, summary));
        }

        eventPublisher.publishEvent(new AgentReconcileEventArg(supplier, summaryParam.getGatewayName(),
                new ResultData<>(summaryParam.getIsSuccess(), difference, null)));
    }

    public void saveDiscrepancies(Pair<Set<ComplexReconcileTransaction>, ReconcileSummary> pair) {
        log.debug("saveReconcileDiscrepancy started for transactions with count : {}", pair.getKey().size());
        util.saveDiscrepancies(pair.getKey(), pair.getValue());
    }

    public String getLogin() {
        log.trace("getLogin started");
        return SecurityUtils.getCurrentUserLogin().orElse("");
    }

    public AgentReconcileResponse getRunFailResponse(@RequestBody @Valid @NotNull AgentReconcileRequestV2 request, AgentReconcileStatus status) {
        log.trace("getRunFailResponse started");
        return new AgentReconcileResponse(null, request.getFromDate(), request.getToDate(), status.isSuccess(), status.getCode(), status.getMessage());
    }
}
