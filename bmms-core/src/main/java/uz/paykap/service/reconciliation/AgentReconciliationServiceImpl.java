package uz.paykap.service.reconciliation;

import com.google.common.collect.Sets;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import uz.javlon.commons.base.ResultData;
import uz.paykap.config.CustomAsyncConfig;
import uz.paykap.domain.enumeration.ReconcileSummaryStatus;
import uz.paykap.event.dto.AgentReconcileEventArg;
import uz.paykap.repository.ReconcileSummaryRepository;
import uz.paykap.service.AgentReconciliationService;
import uz.paykap.service.ReconcileSummaryService;
import uz.paykap.service.dto.ReconcileSummaryDTO;
import uz.paykap.service.dto.reconcile.*;
import uz.paykap.service.dto.reconcile.enumeration.AgentReconcileStatus;
import uz.paykap.utils.AgentReconcileUtil;

@Slf4j
@Service(AgentReconciliationServiceImpl.SERVICE_NAME)
@AllArgsConstructor
public class AgentReconciliationServiceImpl implements AgentReconciliationService<AgentReconcileRequest, Long> {

    public static final String SERVICE_NAME = "agentReconciliationServiceImpl";

    private final ApplicationEventPublisher eventPublisher;
    private final ReconcileSummaryService summaryService;
    private final ReconcileSummaryRepository summaryRepository;
    private final AgentReconcileUtil agentReconcileUtil;

    @Override
    public AgentReconcileStatus validateCreationRequest(AgentReconcileRequest request) {
        log.debug("validateCreationRequest started");

        if (request.getTransactions() == null) {
            log.warn("reconcile request has empty transaction collection");
            return AgentReconcileStatus.EMPTY_TRANSACTIONS;
        }

        return agentReconcileUtil.getReconciliationByRecipientIdAndStatusProcessing(request.getRecipientId());
    }

    @Override
    public CreateReconcileResult createReconciliation(Long recipientId) {
        log.debug("createReconciliation started");

        var recipient = agentReconcileUtil.getRecipient(recipientId);

        if (recipient == null) {
            log.debug("recipient get result is null");
            return new CreateReconcileResult(null, null, null, AgentReconcileStatus.RECIPIENT_NOT_FOUND);
        }

        if (recipient.getGateway() == null) {
            log.debug("gateway get result for recipient {} is null", recipient.getId());
            return new CreateReconcileResult(null, null, null, AgentReconcileStatus.GATEWAY_NOT_FOUND);
        }

        ReconcileSummaryDTO reconcile = agentReconcileUtil.getReconcileSummaryDTO(recipient);
        var dto = summaryService.save(reconcile);

        return new CreateReconcileResult(dto.getId(), recipient.getGateway().getId(), recipient.getGateway().getName(), AgentReconcileStatus.SUCCESS);
    }

    @Async(value = CustomAsyncConfig.ASYNC_TASK_NAME)
    @Override
    public void runReconcileAsync(CreateReconcileResult result, AgentReconcileRequest request, String login) {
        log.debug("reconcileAsync started for reconciliation id : {}, and period from : {}, to : {}", result.getReconcileId(),
                request.getFromDate(), request.getToDate());

        agentReconcileUtil.changeReconciliationStatus(result.getReconcileId());

        var systemTransactions = agentReconcileUtil.getSystemTransactions(request.getAgentName(),
                request.getFromDate(), request.getToDate());

        var systemTranSummary = agentReconcileUtil.getSystemTranSummary(request.getAgentName(),
                request.getFromDate(), request.getToDate());

        var difference = Sets.symmetricDifference(systemTransactions, request.getTransactions());
        var agentRequestTotalSum = request.getTransactions().stream().mapToDouble(ComplexReconcileTransaction::getAmount).sum();

        if (difference.isEmpty()) {
            agentReconcileUtil.successfulReconcileAction(result.getReconcileId(), request.getTransactions(), request.getAgentName(),
                request.getToDate(), systemTranSummary, agentRequestTotalSum, result.getGatewayId());
            return;
        }

        var reconcileSummary = summaryService.saveReconcileSummary(result.getReconcileId(), false,
            request.getAgentName(), request.getFromDate(), request.getToDate(), Double.valueOf(agentRequestTotalSum).longValue(),
            (long) request.getTransactions().size(), 1, ReconcileSummaryStatus.COMPARED_FALSE, result.getGatewayId());

        agentReconcileUtil.saveDiscrepancies(difference, reconcileSummary);
        eventPublisher.publishEvent(new AgentReconcileEventArg(this, request.getAgentName(), new ResultData<>(false, difference, null)));
    }

    @Override
    public AgentReconcileStatusResponse getStatus(AgentReconcileStatusRequest request) {
        log.debug("getStatus started");

        var reconcile = summaryRepository.findById(request.getId());

        if (!reconcile.isPresent()) {
            log.warn("reconcile not found with id : {}", request.getId());
            return agentReconcileUtil.buildCommonTranEmptyResponse(AgentReconcileStatus.RECONCILE_NOT_FOUND, false);
        }

        return agentReconcileUtil.resolveResult(reconcile.get());
    }
}
