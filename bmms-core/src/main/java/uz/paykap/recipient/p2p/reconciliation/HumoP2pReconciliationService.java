package uz.paykap.recipient.p2p.reconciliation;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uz.javlon.commons.base.BasicResult;
import uz.paykap.adapter.verification.pc.PcTranType;
import uz.paykap.domain.enumeration.P2pProcessorType;
import uz.paykap.service.humo.HumoP2pHelperService;
import uz.paykap.service.humo.P2pRecoSummaryResult;
import uz.paykap.service.reconciliation.p2p.AbstractP2pRecoService;

import java.time.Instant;

@Slf4j
@Service
@AllArgsConstructor
public class HumoP2pReconciliationService extends AbstractP2pRecoService {

    private final HumoP2pHelperService humoHelperService;

    @Override
    public P2pProcessorType getProcessorType() {
        return P2pProcessorType.HUMO;
    }

    @Override
    public BasicResult reconcile(Instant fromDate, Instant toDate) {
        log.debug("begin humo p2p credit reconcile");
        BasicResult creditReconcileResult = doCreditReconcile(fromDate, toDate);
        log.debug("humo p2p credit reconcile result: {}", creditReconcileResult);

        log.debug("begin humo p2p debit reconcile");
        BasicResult debitReconcileResult = doDebitReconcile(fromDate, toDate);
        log.debug("humo p2p debit reconcile result: {}", debitReconcileResult);
//        BasicResult debitHoldReconcileResult = doDebitHoldReconcile(fromDate, toDate);

        boolean resultIsSuccess = creditReconcileResult.isSuccess() && debitReconcileResult.isSuccess();
        return new BasicResult(resultIsSuccess, "humo-reconcile end");
    }

    private BasicResult doDebitHoldReconcile(Instant fromDate, Instant toDate) {
        P2pRecoSummaryResult humoP2pDebitHoldSumResult =  null;
        try {
            humoP2pDebitHoldSumResult = humoHelperService.summaryReconcile(PcTranType.DEBIT_HOLD, fromDate, toDate);
        } catch (Exception e) {
            return new BasicResult(false, e.getMessage());
        }
        return this.getRecoHelperService().compareP2pDebitHold(humoP2pDebitHoldSumResult, true, P2pProcessorType.HUMO, fromDate, toDate);
    }

    private BasicResult doDebitReconcile(Instant fromDate, Instant toDate) {
        P2pRecoSummaryResult humoP2pDebitSumResult = null;
        try {
            humoP2pDebitSumResult = humoHelperService.summaryReconcile(PcTranType.DEBIT, fromDate, toDate);
        } catch (Exception e) {
            return new BasicResult(false, e.getMessage());
        }
        return this.getRecoHelperService().compareP2pDebitConfirm(humoP2pDebitSumResult, true, P2pProcessorType.HUMO, fromDate, toDate);
    }

    private BasicResult doCreditReconcile(Instant fromDate, Instant toDate) {
        P2pRecoSummaryResult humoP2pCreditSumResult = null;
        try {
            humoP2pCreditSumResult = humoHelperService.summaryReconcile(PcTranType.CREDIT, fromDate, toDate);
        } catch (Exception e) {
            return new BasicResult(false, e.getMessage());
        }
        return this.getRecoHelperService().compareP2pCreditConfirm(humoP2pCreditSumResult, true, P2pProcessorType.HUMO, fromDate, toDate);
    }
}
