package uz.paykap.agent.tran.p2p;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import uz.devops.pusher.event.PusherWarnEvent;
import uz.paykap.config.PusherConstants;
import uz.paykap.domain.Bill;
import uz.paykap.domain.Transaction;
import uz.paykap.domain.enumeration.P2pProcessorType;
import uz.paykap.service.dto.p2p.BmResultStatus;
import uz.paykap.service.dto.result.BmResult;
import uz.paykap.service.dto.result.BmResultData;
import uz.paykap.service.p2p.dto.P2pParamResult;

import java.util.Arrays;
import java.util.List;

@Slf4j
@Order
@Service(DefaultP2pTranService.NAME)
@AllArgsConstructor
public class DefaultP2pTranService extends P2pPaymentService {

    public static final String NAME = "defaultP2pTranService";

    public BmResult transactP2p(Transaction transaction, P2pParamResult p2pParamResult) {
        log.info("P2p debiting...");
        BmResultData<Bill> debitResult = p2pDebitHold(transaction, p2pParamResult);

        if (debitResult.isTimeout()) {
            return getP2pError(debitResult, true);
        }

        if (!debitResult.isSuccess() || debitResult.getData() == null || !debitResult.getData().isSuccess()) {
            return getP2pError(debitResult, true);
        }

        log.info("P2p crediting...");
        BmResultData<Bill> creditResult = p2pCredit(transaction, p2pParamResult);

        if (creditResult.isTimeout()) {
            return getP2pError(creditResult, false);
        }

        if (!creditResult.isSuccess() || creditResult.getData() == null || !creditResult.getData().isSuccess()) {

            log.info("P2p sender reversing...");
            var p2pCancelResult = p2pCancelDebit(transaction);

            String details = creditResult.getDetails();

            if (!p2pCancelResult.isSuccess() || p2pCancelResult.getData() == null || !p2pCancelResult.getData().isSuccess()) {
                details += ", sender NOT cancelled";
            } else {
                details += ", sender cancelled OK";
            }
            creditResult.setDetails(details);

            return getP2pError(creditResult, false);
        }

        log.info("P2p confirming...");

        BmResultData<Bill> debitConfirmResult = p2pDebitConfirm(transaction);
        if (!debitConfirmResult.isSuccess() || !debitConfirmResult.getData().isSuccess()) {
            applicationContext.publishEvent(new PusherWarnEvent(this, debitConfirmResult.getMessage(), debitConfirmResult.getData(), PusherConstants.TAG_BM_PUSH_WARN));

            log.warn("P2p debit not confirmed, confirmResult : {}", debitConfirmResult);

            return new BmResult(BmResultStatus.SUCCESS, "Need confirm manually, refnum=" + transaction.getId());
        }

        return new BmResult(BmResultStatus.SUCCESS);
    }

    @Override
    public List<P2pProcessorType> getSupportedSender() {
        return Arrays.asList(P2pProcessorType.values());
    }

    @Override
    public List<P2pProcessorType> getSupportedRecipient() {
        return Arrays.asList(P2pProcessorType.values());
    }

    private void continueP2p() {

    }

}
