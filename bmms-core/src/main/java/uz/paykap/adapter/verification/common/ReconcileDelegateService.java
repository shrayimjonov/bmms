package uz.paykap.adapter.verification.common;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import uz.paykap.config.ApplicationProperties;
import uz.paykap.service.GatewayService;
import uz.paykap.service.ReconcileDiscrepancyService;
import uz.paykap.service.ReconcileSummaryService;

@Service
public class ReconcileDelegateService {
    private final ApplicationProperties properties;
    private final ApplicationEventPublisher publisher;
    private final ReconcileSummaryService reconcileSummaryService;
    private final ReconcileDiscrepancyService reconcileDiscrepancyService;
    private final GatewayService gatewayService;

    public ReconcileDelegateService(ApplicationProperties properties, ApplicationEventPublisher publisher, ReconcileSummaryService reconcileSummaryService, ReconcileDiscrepancyService reconcileDiscrepancyService, GatewayService gatewayService) {
        this.properties = properties;
        this.publisher = publisher;
        this.reconcileSummaryService = reconcileSummaryService;
        this.reconcileDiscrepancyService = reconcileDiscrepancyService;
        this.gatewayService = gatewayService;
    }

    public ApplicationEventPublisher getPublisher() {
        return publisher;
    }

    public ReconcileSummaryService getReconcileSummaryService() {
        return reconcileSummaryService;
    }

    public ReconcileDiscrepancyService getReconcileDiscrepancyService() {
        return reconcileDiscrepancyService;
    }

    public ApplicationProperties getProperties() {
        return properties;
    }

    public GatewayService getGatewayService() {
        return gatewayService;
    }
}
