package uz.paykap.agent.tran.p2p;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import uz.devops.bmcore.enumeration.TransactionStatus;
import uz.devops.pusher.event.PusherWarnEvent;
import uz.javlon.commons.exception.CodedException;
import uz.javlon.commons.exception.ProblemEnum;
import uz.javlon.commons.result.HasData;
import uz.paykap.config.PusherConstants;
import uz.paykap.domain.Bill;
import uz.paykap.domain.Transaction;
import uz.paykap.domain.enumeration.P2pProcessorType;
import uz.paykap.recipient.p2p.P2pReverseService;
import uz.paykap.repository.BillRepository;
import uz.paykap.repository.TransactionRepository;
import uz.paykap.service.dto.p2p.BmResultStatus;
import uz.paykap.service.dto.p2p.P2pRequestScopedContext;
import uz.paykap.service.dto.result.BmResult;
import uz.paykap.service.dto.result.BmResultData;
import uz.paykap.service.p2p.dto.P2pParamResult;
import uz.paykap.service.p2p.dto.response.P2pResponse;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

@Slf4j
public abstract class P2pPaymentService {

    @Autowired
    protected ApplicationContext applicationContext;
    @Autowired
    protected P2pRequestScopedContext p2pRequestScopedContext;
    @Autowired
    protected TransactionRepository transactionRepository;
    @Autowired
    protected BillRepository billRepository;

    public abstract BmResult transactP2p(Transaction transaction, P2pParamResult p2pParamResult);

    public P2pResponse cancelP2p(Transaction transaction) {

        if (Objects.equals(transaction.isRecipientSuccess(), Boolean.TRUE)) {
            log.info("Cancelling in recipient");
            var abstractP2pRecipient = applicationContext.getBean(
                    StringUtils.uncapitalize(transaction.getRecipientName()),
                    P2pReverseService.class
            );
            log.debug("AbstractP2pSender : {}", abstractP2pRecipient);

            var bmResultRecipientReverse = abstractP2pRecipient.reverse(transaction);
            log.debug("Recipient reverse result : {}", bmResultRecipientReverse);

            if (!bmResultRecipientReverse.isSuccess() || bmResultRecipientReverse.getData() == null) {
                log.error("Error when cancelling in recipient : {}", bmResultRecipientReverse.getMessage());
                return p2pError(new CodedException(ProblemEnum.INTERNAL_ERROR, bmResultRecipientReverse.getMessage()));
            }

            var recipientReverseBill = bmResultRecipientReverse.getData();

            if (!recipientReverseBill.isSuccess()) {
                log.error("Error when cancelling in recipient : {}", bmResultRecipientReverse.getMessage());
                return p2pError(new CodedException(ProblemEnum.INTERNAL_ERROR, recipientReverseBill.getResponseText()));
            }

            transaction
                    .recipientStatus("cancelled")
                    .recipientSuccess(false)
                    .status(TransactionStatus.RECIPIENT_CANCELLED)
                    .nextRecipientStatusText(TransactionStatus.RECIPIENT_CANCELLED);
            transaction = transactionRepository.save(transaction);
        }

        if (!Objects.equals(transaction.isRecipientSuccess(), Boolean.TRUE) && Objects.equals(transaction.isSenderSuccess(), Boolean.TRUE)) {
            log.info("Cancelling in sender");

            var abstractP2pSender = applicationContext.getBean(
                    StringUtils.uncapitalize(transaction.getSenderName()),
                    P2pReverseService.class
            );
            log.debug("AbstractP2pSender : {}", abstractP2pSender);

            var bmResultSenderReverse = abstractP2pSender.reverse(transaction);
            log.debug("Bm result sender reverse : {}", bmResultSenderReverse);

            if (!bmResultSenderReverse.isSuccess() || bmResultSenderReverse.getData() == null) {
                log.error("Error when cancelling in recipient : {}", bmResultSenderReverse.getMessage());
                return p2pError(new CodedException(ProblemEnum.INTERNAL_ERROR, bmResultSenderReverse.getMessage()));
            }

            var senderReverseBill = bmResultSenderReverse.getData();
            if (!senderReverseBill.isSuccess()) {
                log.error("Error when cancelling in recipient : {}", senderReverseBill.getResponseText());
                return p2pError(new CodedException(ProblemEnum.INTERNAL_ERROR, senderReverseBill.getResponseText()));
            }

            transaction
                    .senderStatus("cancelled")
                    .senderSuccess(false)
                    .status(TransactionStatus.SENDER_CANCELLED)
                    .nextRecipientStatusText(TransactionStatus.SENDER_CANCELLED);
            transaction = transactionRepository.save(transaction);
        }

        if (Objects.equals(transaction.isRecipientSuccess(), Boolean.FALSE) && Objects.equals(transaction.isSenderSuccess(), Boolean.FALSE)) {
            transaction.status(TransactionStatus.CANCELLED).nextRecipientStatusText(TransactionStatus.CANCELLED);
            transaction = transactionRepository.save(transaction);

            log.debug("Transaction saved with status cancelled: {}", transaction);

            return new P2pResponse().withStatusCode(new BmResult(BmResultStatus.SUCCESS)).withSuccess(true).withAgentTranId(p2pRequestScopedContext.getAgentTranId());
        }

        return p2pError(new CodedException(ProblemEnum.INTERNAL_ERROR, "p2p reverse error"));
    }

    private P2pResponse p2pError(CodedException codedException) {
        String localizedMessage;

        if (codedException != null) {
            localizedMessage = codedException.getLocalizedMessage();
        } else {
            localizedMessage = "Unknown exception : " + p2pRequestScopedContext.getRequest().getAgentTranId();
        }

        log.error("Error occurred while doing p2p : {}", localizedMessage);

        Transaction transaction = p2pRequestScopedContext.getTransaction();

        transaction
                .recipientDate(Instant.now())
                //            .recipientName(result.getRecipientShortName())
                //            .recipientStatus(String.valueOf(result.isSuccess()))
                .nextRecipientStatusText(localizedMessage)
                //            .recipientSuccess(result.isSuccess())
                .status(TransactionStatus.CANCEL_FAILED)//            .recipientTrId(result.getExtId()) //            .recipientBillId(result.getId())
        //
        ;

        transactionRepository.save(transaction);

        return new P2pResponse().withStatusMessage(localizedMessage).withSuccess(false);
    }

    protected abstract List<P2pProcessorType> getSupportedSender();

    protected abstract List<P2pProcessorType> getSupportedRecipient();

    protected BmResult getP2pError(BmResult status, boolean isSender) {

        String errorMessage;

        if (status != null) {
            errorMessage = status.getMessage();
        } else {
            errorMessage = "Unknown exception : " + p2pRequestScopedContext.getRequest().getAgentTranId();
        }

        log.error("Error occurred while doing p2p : {}", errorMessage);

        Transaction transaction = p2pRequestScopedContext.getTransaction();
        if (isSender) {
            transaction
                    .senderDate(Instant.now())
                    .nextRecipientStatusText(errorMessage);
        } else {
            transaction
                    .recipientDate(Instant.now())
                    .recipientStatusText(errorMessage);
        }

        transaction.status(status != null && (Objects.equals(status.getStatus(), BmResultStatus.WAITING) || Objects.equals(status.getStatus(), BmResultStatus.TIMEOUT)) ? (isSender ? TransactionStatus.S_NEED_CONFIRM : TransactionStatus.R_NEED_CONFIRM) : TransactionStatus.FAILED);

        transactionRepository.save(transaction);

        return status;
    }

    protected BmResultData<Bill> p2pDebitHold(Transaction transaction, P2pParamResult p2pParamResult) {

        BmResultData<Bill> debitResult = p2pRequestScopedContext.getSenderService().debit(transaction, p2pRequestScopedContext.getSenderDto(), p2pRequestScopedContext.getRecipientDto(), p2pParamResult);

        if (debitResult.isTimeout()) {
            transaction.fillSenderPart(debitResult.getData());
            transaction.setSenderStatus("GATEWAY_TIMEOUT");
            transaction.setSenderStatusText("GATEWAY_TIMEOUT");
            transaction.setStatus(TransactionStatus.S_NEED_CONFIRM);
            transactionRepository.save(transaction);
            return debitResult;
        } else if (debitResult.getStatus() == BmResultStatus.WAITING) {

            transaction.fillSenderPart(debitResult.getData());
            transaction.setSenderStatus(TransactionStatus.WAITING.name());
            transaction.setSenderStatusText(TransactionStatus.WAITING.name());
            transaction.setStatus(TransactionStatus.S_NEED_CONFIRM);
            transactionRepository.save(transaction);
            return debitResult;

        } else if (!debitResult.isSuccess() || debitResult.getData() == null || !debitResult.getData().isSuccess()) {
            /*
             * Actual status may be SUCCESS, but data (Bill) can be failed
             * */

            if (debitResult.getStatus() == BmResultStatus.UNKNOWN_ERROR) {
                debitResult
                        .withStatus(BmResultStatus.SENDER_PC_ERROR);
            }

            debitResult
//                .withStatus(BmResultStatus.SENDER_PC_ERROR)
                    .withDetails(debitResult.getDetails() != null ? debitResult.getDetails() : (debitResult.getData() != null ? debitResult.getData().getResponseText() : debitResult.getStatus().getMessage()));

            log.warn("Debit failed : {}", debitResult.getMessage());
            return debitResult;
        }

        Bill senderBill = debitResult.getData();

        transaction.fillSenderPart(senderBill);

        transactionRepository.save(
                transaction.status(TransactionStatus.DEBITED)
        );

        return debitResult;
    }

    protected BmResultData<Bill> p2pCredit(Transaction transaction, P2pParamResult p2pParamResult) {

        BmResultData<Bill> creditResult = p2pRequestScopedContext.getRecipientService().credit(transaction, p2pRequestScopedContext.getSenderDto(), p2pRequestScopedContext.getRecipientDto(), p2pParamResult);

        if (creditResult.isTimeout()) {
            transaction.fillRecipientPart(creditResult.getData());
            transaction.setRecipientStatus("GATEWAY_TIMEOUT");
            transaction.setRecipientStatusText("GATEWAY_TIMEOUT");
            transaction.setStatus(TransactionStatus.R_NEED_CONFIRM);
            transactionRepository.save(transaction);
            return creditResult;
        }

        if (creditResult.getStatus() == BmResultStatus.WAITING) {

            transaction.fillRecipientPart(creditResult.getData());
            transaction.setRecipientStatus(TransactionStatus.WAITING.name());
            transaction.setRecipientStatusText(TransactionStatus.WAITING.name());
            transaction.setStatus(TransactionStatus.R_NEED_CONFIRM);
            transactionRepository.save(transaction);
            return creditResult;
        }
        if (!creditResult.isSuccess() || creditResult.getData() == null || !creditResult.getData().isSuccess()) {

            transaction
                    .status(TransactionStatus.FAILED)
                    .nextRecipientStatusText(creditResult.getMessage());

            transactionRepository.save(transaction);

            // It is failed result, just return it
            return creditResult;
        }

        Bill recipientBill = creditResult.getData();

        transaction.fillRecipientPart(recipientBill);

        transactionRepository.save(
                transaction.getStatus() == TransactionStatus.R_NEED_CONFIRM ? transaction.status(TransactionStatus.R_NEED_CONFIRM) : transaction.status(TransactionStatus.SUCCESS)
        );

        // It is success result, just return it
        return creditResult;
    }

    protected BmResultData<Bill> p2pDebitConfirm(Transaction transaction) {

        BmResultData<Bill> debitConfirmResult = p2pRequestScopedContext.getSenderService().debitConfirm(transaction);

        log.debug("Debit confirm result: {}", debitConfirmResult);

        if (debitConfirmResult == null) {
            return new BmResultData<>(BmResultStatus.S_NEED_CONFIRM, "Got null result in confirm, refnum=" + transaction.getId());
        }

        if (!debitConfirmResult.isSuccess() || debitConfirmResult.getData() == null || !debitConfirmResult.getData().isSuccess()) {
            log.error("Confirm error, please finish pay from UI");

            transaction
                    .status(TransactionStatus.S_NEED_CONFIRM);
            transactionRepository.save(transaction);

            return new BmResultData<>(BmResultStatus.S_NEED_CONFIRM, "Confirm error, please finish pay from UI, refnum=" + transaction.getId());
        }

        transactionRepository.save(transaction);

        return debitConfirmResult;
    }

    protected HasData<Bill> p2pCancelDebit(Transaction transaction) {

        var reversalResult = p2pRequestScopedContext.getSenderService().reverse(transaction);

        if (reversalResult.isSuccess()) {
            log.info("P2pDebit cancelled for account {}", transaction.getSenderAccount());
            transaction
                    .status(TransactionStatus.REVERSED)
                    .nextRecipientStatusText(TransactionStatus.REVERSED);
        } else {
            log.warn("P2pDebit NOT cancelled for account {}", transaction.getSenderAccount());
            transaction
                    .status(TransactionStatus.S_CANCEL_FAILED)
                    .nextRecipientStatusText(TransactionStatus.S_CANCEL_FAILED);

            applicationContext.publishEvent(new PusherWarnEvent(this, "P2p debit not canceled, please cancel from UI, refnum= " + transaction.getId() + " , " + reversalResult.getMessage(), reversalResult.getData(), PusherConstants.TAG_BM_PUSH_WARN));
        }

        transactionRepository.save(transaction);

        log.info("Sender tran reversed : {}", reversalResult);

        return reversalResult;
    }
}
