package uz.paykap.adapter.verification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uz.javlon.commons.base.Result;
import uz.javlon.commons.base.ResultData;
import uz.paykap.adapter.verification.common.ReconcileDelegateService;
import uz.paykap.adapter.verification.handler.SystemTransactionsHandler;
import uz.paykap.gateway.dto.GatewayTranDTO;
import uz.paykap.gateway.dto.ReconcileGatewayDTO;

import java.time.Instant;
import java.util.HashSet;

public abstract class FromFileReconcileServiceV2 extends ReconcileServiceV2 {

    private final Logger log = LoggerFactory.getLogger(FromFileReconcileServiceV2.class);

    private final SystemTransactionsHandler systemTransactionsHandler;
    private Integer amountRatio = 100; //Tiyin

    FromFileReconcileServiceV2(String gatewayName, SystemTransactionsHandler systemTransactionsHandler, ReconcileDelegateService reconcileDelegateService) {
        super(gatewayName, reconcileDelegateService);
        this.systemTransactionsHandler = systemTransactionsHandler;
    }

    @Override
    public Result reconcile(Instant fromTime, Instant toTime) {

        ResultData<ReconcileGatewayDTO> gatewayTrans = readFromFile(getGatewayName(), fromTime, toTime);

        if (gatewayTrans == null) {
            log.warn("BAD - unsuccessful gatewayTrans from {}", getGatewayName());
            return Result.FAILED;
        }

        if (!gatewayTrans.isSuccess()) {
            log.warn(String.valueOf(gatewayTrans.getException()));
            return Result.FAILED;
        }

        if (gatewayTrans.getData() == null) {
            log.warn("returned data=null from {}", getGatewayName());
            return Result.FAILED;
        }

        HashSet<GatewayTranDTO> transactions = systemTransactionsHandler.getTransactions(fromTime, toTime);

        if (transactions == null) {
            log.warn("BAD - transactions is null");
            return Result.FAILED;
        }

        return reconcileInner(transactions, gatewayTrans.getData(), fromTime, toTime, amountRatio);
    }

    abstract ResultData<ReconcileGatewayDTO> readFromFile(String gateway, Instant fromTime, Instant toTime);

    public FromFileReconcileServiceV2 amountRatio(Integer amountRatio) {
        if (amountRatio != null) {
            this.amountRatio = amountRatio;
        }
        return this;
    }
}
