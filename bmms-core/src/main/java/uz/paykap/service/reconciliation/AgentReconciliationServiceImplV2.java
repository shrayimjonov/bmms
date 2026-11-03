package uz.paykap.service.reconciliation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import uz.paykap.config.CustomAsyncConfig;
import uz.paykap.domain.enumeration.ReconcilePeriod;
import uz.paykap.domain.enumeration.ReconcileSummaryStatus;
import uz.paykap.repository.ReconcileSummaryRepository;
import uz.paykap.security.SecurityUtils;
import uz.paykap.service.AgentReconciliationService;
import uz.paykap.service.ReconcileSummaryService;
import uz.paykap.service.dto.ReconcileSummaryDTO;
import uz.paykap.service.dto.reconcile.*;
import uz.paykap.service.dto.reconcile.enumeration.AgentReconcileStatus;
import uz.paykap.utils.AgentReconcileV2Util;

@Slf4j
@Service(AgentReconciliationServiceImplV2.SERVICE_NAME)
@RequiredArgsConstructor
public class AgentReconciliationServiceImplV2 implements AgentReconciliationService<AgentReconcileRequestV2, String> {

    public static final String SERVICE_NAME = "agentReconciliationServiceImplV2";

    private final ReconcileSummaryService summaryService;
    private final ReconcileSummaryRepository summaryRepository;
    private final AgentReconcileV2Util util;

    @Override
    public AgentReconcileStatus validateCreationRequest(AgentReconcileRequestV2 request) {
        log.debug("validateCreationRequest started");

        if (request.getTransactions() == null) {
            log.warn("reconcile request has empty transaction collection");
            return AgentReconcileStatus.EMPTY_TRANSACTIONS;
        }

        var login = SecurityUtils.getCurrentUserLogin();

        if (login.isEmpty()) {
            log.warn("current logged user name is unknown");
            return AgentReconcileStatus.AGENT_GET_ERROR;
        }

        return util.getReconciliation(util.getGatewayName(login.get(), request.getGroup(), request.getSubgroup()));
    }

    @Override
    public CreateReconcileResult createReconciliation(String agentCredentials) {
        log.debug("createReconciliation started for : {}", agentCredentials);

        if (agentCredentials == null) {
            log.debug("agent credentials are null");
            return new CreateReconcileResult(null, null, null, AgentReconcileStatus.AGENT_CREDENTIALS_ARE_EMPTY);
        }

        ReconcileSummaryDTO reconcile = util.createReconcileSummaryDTO(agentCredentials);
        var dto = summaryService.save(reconcile);

        return new CreateReconcileResult(dto.getId(), dto.getGatewayId(), agentCredentials, AgentReconcileStatus.SUCCESS);
    }

    @Async(value = CustomAsyncConfig.ASYNC_TASK_NAME)
    @Override
    public void runReconcileAsync(CreateReconcileResult result, AgentReconcileRequestV2 request, String login) {
        log.debug("runReconcileAsync started for reconciliation id : {}, and period from : {}, to : {}", result.getReconcileId(),
                request.getFromDate(), request.getToDate());

        util.changeReconciliationStatus(result.getReconcileId());

        var systemTransactions = util.getSystemTransactions(login, request.getGroup(),
                request.getSubgroup(), request.getFromDate(), request.getToDate());

        var systemTranSummary = util.getSystemTranSummary(login, request.getGroup(),
                request.getSubgroup(), request.getFromDate(), request.getToDate());

        var difference = util.getTransactionDifference(request, systemTransactions);
        var agentRequestTotalAmount = request.getTransactions().stream().mapToDouble(ComplexReconcileTransaction::getAmount).sum();

        var gatewayName = util.getGatewayName(login, request.getGroup(), request.getSubgroup());

        util.saveReconcileSummary(
                this,
                RecoSummaryParam.of(
                        result.getReconcileId(),
                        difference.isEmpty(),
                        request.getToDate(),
                        Double.valueOf(agentRequestTotalAmount).longValue(),
                        (long) request.getTransactions().size(),
                        systemTranSummary.getTotalAmountTiyin(),
                        systemTranSummary.getTotalCount(),
                        100,
                        difference.isEmpty() ? ReconcileSummaryStatus.COMPARED_OK : ReconcileSummaryStatus.COMPARED_FALSE,
                        result.getGatewayId(),
                        ReconcilePeriod.DAY,
                        gatewayName),
                difference,
                util::saveDiscrepancies);
    }

    @Override
    public AgentReconcileStatusResponse getStatus(AgentReconcileStatusRequest request) {
        log.debug("getStatus started with request : {}", request);

        var reconcile = summaryRepository.findById(request.getId());

        if (reconcile.isEmpty()) {
            log.warn("reconcile not found with id : {}", request.getId());
            return util.buildCommonTranEmptyResponse(AgentReconcileStatus.RECONCILE_NOT_FOUND, false);
        }

        return util.resolveResult(reconcile.get());
    }
}
