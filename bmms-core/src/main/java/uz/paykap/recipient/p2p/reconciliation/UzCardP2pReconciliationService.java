package uz.paykap.recipient.p2p.reconciliation;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uz.javlon.commons.base.BasicResult;
import uz.javlon.commons.exception.CodedException;
import uz.javlon.commons.exception.ProblemEnum;
import uz.paykap.adapter.verification.pc.PcTranType;
import uz.paykap.domain.enumeration.P2pProcessorType;
import uz.paykap.service.humo.P2pRecoSummaryResult;
import uz.paykap.service.reconciliation.p2p.AbstractP2pRecoService;
import uz.paykap.service.uzcard.UzcardHelperService;

import java.time.Instant;

@Slf4j
@Service
@AllArgsConstructor
public class UzCardP2pReconciliationService extends AbstractP2pRecoService {

    private final UzcardHelperService uzcardHelperService;

    @Override
    public P2pProcessorType getProcessorType() {
        return P2pProcessorType.UZCARD;
    }

    @Override
    public BasicResult reconcile(Instant fromDate, Instant toDate) {
        log.debug("uzcard p2p reconcile, fromDate: {}, toDate: {}", fromDate, toDate);
        log.debug("uzcard doCreditReconcile..");
        BasicResult creditReconcileResult = doCreditReconcile(fromDate, toDate);
        log.debug("uzcard creditReconcile result: {}", creditReconcileResult);

        log.debug("uzcard doDebitReconcile...");
        BasicResult debitReconcileResult = doDebitReconcile(fromDate, toDate);
        log.debug("uzcard debitReconcile result: {}", debitReconcileResult);

        boolean resultIsSuccess = creditReconcileResult.isSuccess() && debitReconcileResult.isSuccess();
        log.debug("uzcard reconcile isSuccess: {}", resultIsSuccess);
        return new BasicResult(resultIsSuccess, "uzcard-reconcile end");
    }

    private BasicResult doCreditReconcile(Instant fromDate, Instant toDate) {
        P2pRecoSummaryResult uzCardP2pCreditSumResult = null;
        try {
            uzCardP2pCreditSumResult = uzcardHelperService.summaryReconcile(PcTranType.CREDIT, fromDate, toDate);
        } catch (Exception e) {
            e.printStackTrace();
            return new BasicResult(false, e.getMessage());
        }
        return this.getRecoHelperService().compareP2pCreditConfirm(uzCardP2pCreditSumResult, true, P2pProcessorType.UZCARD, fromDate, toDate);
    }

    private BasicResult doDebitReconcile(Instant fromDate, Instant toDate) {
        P2pRecoSummaryResult uzCardP2pDebitSumResult = null;
        try {
            uzCardP2pDebitSumResult = uzcardHelperService.summaryReconcile(PcTranType.DEBIT, fromDate, toDate);
        } catch (Exception e) {
            return new BasicResult(false, e.getMessage());
        }
        return this.getRecoHelperService().compareP2pDebitConfirm(uzCardP2pDebitSumResult, true, P2pProcessorType.UZCARD, fromDate, toDate);
    }
}
