package uz.paykap.adapter.verification;

import com.google.common.collect.Sets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uz.javlon.commons.base.Result;
import uz.javlon.commons.base.ResultData;
import uz.paykap.adapter.verification.common.ReconcileDelegateService;
import uz.paykap.domain.ReconcileDiscrepancy;
import uz.paykap.domain.ReconcileSummary;
import uz.paykap.domain.enumeration.ReconcileSummaryStatus;
import uz.paykap.event.dto.ReconciliationFinishedEvent;
import uz.paykap.gateway.dto.GatewayTranDTO;
import uz.paykap.gateway.dto.ReconcileGatewayDTO;

import java.time.Instant;
import java.util.HashSet;
import java.util.Objects;

public abstract class ReconcileServiceV2 {

    private final Logger log = LoggerFactory.getLogger(ReconcileServiceV2.class);

    private final String gatewayName;
    private final ReconcileDelegateService reconcileDelegateService;

    public ReconcileServiceV2(String gatewayName, ReconcileDelegateService reconcileDelegateService) {
        this.gatewayName = gatewayName;
        this.reconcileDelegateService = reconcileDelegateService;
    }

    public abstract Result reconcile(Instant fromTime, Instant toTime);

    public String getGatewayName() {
        return gatewayName;
    }

    protected Result reconcileInner(HashSet<GatewayTranDTO> transactions, ReconcileGatewayDTO gatewayTrans, Instant fromTime, Instant toTime) {
        return reconcileInner(transactions, gatewayTrans, fromTime, toTime, 100);
    }

    protected Result reconcileInner(HashSet<GatewayTranDTO> transactions, ReconcileGatewayDTO gatewayTrans, Instant fromTime, Instant toTime, Integer amountRatio) {

        if (transactions == null) {
            log.warn("BAD - transactions is null for {}", getGatewayName());
            return Result.FAILED;
        }
        log.debug("Our transactions : {}", transactions);

        if (gatewayTrans == null) {
            log.warn("BAD - unsuccessful gatewayTrans for {}", getGatewayName());
            return Result.FAILED;
        }

        Sets.SetView<GatewayTranDTO> difference = Sets.symmetricDifference(transactions, gatewayTrans.getGatewayTranDTOS());

        if (difference.isEmpty()) {
            //ALl ok, just result

            reconcileDelegateService.getReconcileSummaryService().saveReconcileSummary(true, getGatewayName(), fromTime, toTime, gatewayTrans.getTotalAmountTiyin(), gatewayTrans.getTotalCount(), amountRatio, ReconcileSummaryStatus.COMPARED_OK);

            reconcileDelegateService.getPublisher().publishEvent(new ReconciliationFinishedEvent(this, getGatewayName(), new ResultData<>(true, null, null)));

            return Result.SUCCESS;
        }

        ReconcileSummary reconcileSummary = reconcileDelegateService.getReconcileSummaryService().saveReconcileSummary(false, getGatewayName(), fromTime, toTime, gatewayTrans.getTotalAmountTiyin(), gatewayTrans.getTotalCount(), amountRatio, ReconcileSummaryStatus.COMPARED_FALSE);

        //If differs
        if (!Objects.equals(getGatewayName(), "Ucell")) {
            saveReconcileDiscrepancy(difference, getGatewayName(), reconcileSummary);
        }
        reconcileDelegateService.getPublisher().publishEvent(new ReconciliationFinishedEvent(this, getGatewayName(), new ResultData<>(false, difference, null)));

        return Result.FAILED;
    }

    private Result saveReconcileDiscrepancy(Sets.SetView<GatewayTranDTO> difference, String gatewayName, ReconcileSummary reconcileSummary) {

        for (GatewayTranDTO gatewayTranDTO : difference) {
            try {
                createOrUpdate(gatewayTranDTO, reconcileSummary);
            } catch (Exception e) {
                log.warn("Error when saving reconcileDiscrepancy : {} ", gatewayTranDTO.toString());
                log.debug("Exception :{}", e.getLocalizedMessage());
            }
        }

        return Result.SUCCESS;
    }

    private ReconcileDiscrepancy createOrUpdate(GatewayTranDTO gatewayTranDTO, ReconcileSummary reconcileSummary) {

        log.debug("Gateway tran to save in reconcile discrepancy :{}", gatewayTranDTO.toString());

        ReconcileDiscrepancy reconcileDiscrepancy = reconcileDelegateService.getReconcileDiscrepancyService().findBySystemTranId(gatewayTranDTO.getId()).orElse(null);

        if (reconcileDiscrepancy == null) {
            reconcileDiscrepancy = new ReconcileDiscrepancy();
            reconcileDiscrepancy.setSystemTransactionId(gatewayTranDTO.getId());
        }

        if (gatewayTranDTO.getGatewayTransaction() == Boolean.TRUE) {
            reconcileDiscrepancy.setGatewayAmount(gatewayTranDTO.getAmount().longValue());
            reconcileDiscrepancy.setGatewayStatus(gatewayTranDTO.getSuccess());
            reconcileDiscrepancy.setGatewayTransactionId(String.valueOf(gatewayTranDTO.getId()));
//            if (gatewayTranDTO.getExtId() != null) {
//                reconcileDiscrepancy.setGatewayTransactionId(Long.valueOf(gatewayTranDTO.getExtId()));
//            }
        } else {
            reconcileDiscrepancy.setSystemAmount(gatewayTranDTO.getAmount().longValue());
            reconcileDiscrepancy.setSystemStatus(gatewayTranDTO.getSuccess());
        }

        reconcileDiscrepancy.setReconcileSummary(reconcileSummary);
        reconcileDiscrepancy.setGatewayName(reconcileSummary.getGatewayName());

        if (reconcileDiscrepancy.getGatewayTransactionId() == null) {
            reconcileDiscrepancy.setGatewayTransactionId(String.valueOf(-1L));
        }

        if (reconcileDiscrepancy.getSystemTransactionId() == null) {
            reconcileDiscrepancy.setSystemTransactionId(-1L);
        }

        return reconcileDelegateService.getReconcileDiscrepancyService().save(reconcileDiscrepancy);
    }

}
