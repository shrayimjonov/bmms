package uz.paykap.adapter.verification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import uz.javlon.commons.base.Result;
import uz.javlon.commons.base.ResultData;
import uz.paykap.adapter.verification.common.ReconcileDelegateService;
import uz.paykap.adapter.verification.handler.TransSummaryHandler;
import uz.paykap.domain.enumeration.ReconcileSummaryStatus;
import uz.paykap.event.dto.ReconciliationFinishedEvent;
import uz.paykap.service.ReconcileSummaryService;

import java.time.Instant;
import java.util.Objects;

public class SummaryReconcileServiceV2 extends ReconcileServiceV2 {

    private final Logger log = LoggerFactory.getLogger(SummaryReconcileServiceV2.class);

    private final TransSummaryHandler transSummaryHandler;
    private final ReconcileSummaryService reconcileSummaryService;
    private final ApplicationEventPublisher publisher;

    public SummaryReconcileServiceV2(String gatewayName, TransSummaryHandler transSummaryHandler, ReconcileSummaryService reconcileSummaryService, ApplicationEventPublisher publisher, ReconcileDelegateService reconcileDelegateService) {
        super(gatewayName, reconcileDelegateService);
        this.transSummaryHandler = transSummaryHandler;
        this.reconcileSummaryService = reconcileSummaryService;
        this.publisher = publisher;
    }

    @Override
    public Result reconcile(Instant fromTime, Instant toTime) {
        log.debug("Summary reconcile started : {} - {}", fromTime, toTime);

        Long gatewaySummaryTiyin = transSummaryHandler.getGatewaySummaryTiyin(fromTime, toTime);
        Long systemSummaryTiyin = transSummaryHandler.getSystemSummaryTiyin(fromTime, toTime);
        Long gatewayTransCount = transSummaryHandler.getGatewayTransCount(fromTime, toTime);
        Long systemTotalCount = transSummaryHandler.getSystemTransCount(fromTime, toTime);

        log.debug("gatewaySummaryTiyin : {}", gatewaySummaryTiyin);
        log.debug("systemSummaryTiyin : {}", systemSummaryTiyin);
        log.debug("gatewayTransCount : {}", gatewayTransCount);
        log.debug("systemTotalCount : {}", systemTotalCount);

        boolean success = false;

        if (gatewaySummaryTiyin != null && gatewaySummaryTiyin >= 0) {
            success = Objects.equals(systemSummaryTiyin, gatewaySummaryTiyin);
            log.debug("Reconcile amounts : {}", success);
        }

        if (success && gatewayTransCount != null && gatewayTransCount >= 0) {
            success = Objects.equals(systemTotalCount, gatewayTransCount);
            log.debug("Reconcile counts : {}", success);
        }

        publisher.publishEvent(new ReconciliationFinishedEvent(this, getGatewayName(), new ResultData<>(success, null, null)));

        ReconcileSummaryStatus reconcileSummaryStatus;

        if (success) {
            reconcileSummaryStatus = ReconcileSummaryStatus.COMPARED_OK;
        } else {
            reconcileSummaryStatus = ReconcileSummaryStatus.COMPARED_FALSE;
        }

        reconcileSummaryService.saveReconcileSummary(success, getGatewayName(), fromTime, toTime, gatewaySummaryTiyin, gatewayTransCount, systemSummaryTiyin, systemTotalCount, reconcileSummaryStatus);

        return new Result(success);
    }

}
