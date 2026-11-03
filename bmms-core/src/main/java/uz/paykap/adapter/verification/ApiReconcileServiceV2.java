package uz.paykap.adapter.verification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uz.javlon.commons.base.Result;
import uz.paykap.adapter.verification.common.ReconcileDelegateService;
import uz.paykap.adapter.verification.handler.GatewayTransactionsHandler;
import uz.paykap.adapter.verification.handler.SystemTransactionsHandler;
import uz.paykap.gateway.dto.GatewayTranDTO;
import uz.paykap.gateway.dto.ReconcileGatewayDTO;

import java.time.Instant;
import java.util.HashSet;

public class ApiReconcileServiceV2 extends ReconcileServiceV2 {

    private final Logger log = LoggerFactory.getLogger(ApiReconcileServiceV2.class);

    private final SystemTransactionsHandler systemTransactionsHandler;
    private final GatewayTransactionsHandler gatewayTransactionsHandler;
    private final ReconcileDelegateService reconcileDelegateService;

    public ApiReconcileServiceV2(String gatewayName, SystemTransactionsHandler systemTransactionsHandler, GatewayTransactionsHandler gatewayTransactionsHandler, ReconcileDelegateService reconcileDelegateService) {
        super(gatewayName, reconcileDelegateService);
        this.systemTransactionsHandler = systemTransactionsHandler;
        this.gatewayTransactionsHandler = gatewayTransactionsHandler;
        this.reconcileDelegateService = reconcileDelegateService;
    }

    public Result reconcile(Instant fromTime, Instant toTime) {

        HashSet<GatewayTranDTO> transactions = systemTransactionsHandler.getTransactions(fromTime, toTime);

        if (transactions == null || transactions.size() == 0) {
            reconcileDelegateService.getReconcileSummaryService().saveReconcileSummary(true, getGatewayName(), fromTime, toTime, 0L, 0L);
            log.warn("BAD - transactions is null for {}", getGatewayName());
            return Result.FAILED;
        }

        ReconcileGatewayDTO gatewayTrans = gatewayTransactionsHandler.getGatewayTransactions(fromTime, toTime);

        if (gatewayTrans == null || gatewayTrans.getGatewayTranDTOS() == null || gatewayTrans.getGatewayTranDTOS().size() == 0) {
            reconcileDelegateService.getReconcileSummaryService().saveReconcileSummary(false, getGatewayName(), fromTime, toTime, 0L, 0L);
            log.warn("BAD - unsuccessful gatewayTrans for {}", getGatewayName());
            return Result.FAILED;
        }

        return reconcileInner(transactions, gatewayTrans, fromTime, toTime);
    }

}
