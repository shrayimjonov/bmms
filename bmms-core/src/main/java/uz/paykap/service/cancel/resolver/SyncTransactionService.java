package uz.paykap.service.cancel.resolver;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.devops.bmcore.enumeration.PayType;
import uz.devops.bmcore.enumeration.RequestType;
import uz.devops.bmcore.enumeration.TransactionStatus;
import uz.javlon.commons.exception.CodedException;
import uz.javlon.commons.exception.ProblemEnum;
import uz.javlon.commons.result.CommonResultData;
import uz.javlon.commons.result.HasData;
import uz.javlon.svgate.dto.enumeration.TranType;
import uz.paykap.domain.Bill;
import uz.paykap.domain.Transaction;
import uz.paykap.repository.BillRepository;
import uz.paykap.repository.TransactionRepository;
import uz.paykap.security.SecurityUtils;
import uz.paykap.service.cancel.CancelService;
import uz.paykap.service.dto.p2p.BmResultStatus;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static uz.paykap.service.cancel.resolver.CancelTransactionService.REQUEST_KEY_NAME;
import static uz.paykap.service.cancel.resolver.CancelTransactionService.RESPONSE_KEY_NANE;

@Service
@Slf4j
@Transactional
public class SyncTransactionService {
    private final TransactionRepository transactionRepository;
    private final CancelServiceUtil cancelServiceUtil;
    private final BillRepository billRepository;

    public SyncTransactionService(TransactionRepository transactionRepository, CancelServiceUtil cancelServiceUtil, BillRepository billRepository) {
        this.transactionRepository = transactionRepository;
        this.cancelServiceUtil = cancelServiceUtil;
        this.billRepository = billRepository;
    }

    public HasData<Transaction> syncTransaction(Long transactionId) {
        Optional<Transaction> optionalTransaction = transactionRepository.findFirstByPayTypeAndId(PayType.P2P, transactionId);
        log.debug("Sync transaction : {}", optionalTransaction);
        if (optionalTransaction.isEmpty()) {
            throw new CodedException(ProblemEnum.OBJECT_NOT_FOUND, "Transaction not found in db");
        }

        var transaction = optionalTransaction.get();
        var syncRecipientTransaction = syncRecipientTransaction(transaction, cancelServiceUtil.getCancelService(transaction.getRecipientGateway()));
        log.debug("synced recipient tran : {}", transaction);

        var syncSenderTransaction = syncSenderTransaction(transaction, cancelServiceUtil.getCancelService(transaction.getSenderGateway()));
        log.debug("synced sender tran : {}", transaction);

        if (transaction.getSenderSuccess() != null && transaction.isSenderSuccess() && transaction.getRecipientSuccess() != null && !transaction.isRecipientSuccess()) {
            transaction.status(TransactionStatus.DEBITED);
        }

        if (transaction.getSenderSuccess() != null && transaction.isSenderSuccess() && transaction.getRecipientSuccess() != null && transaction.isRecipientSuccess()) {
            transaction.status(TransactionStatus.SUCCESS);
        }

        if (transaction.getSenderSuccess() != null && !transaction.isSenderSuccess() && transaction.getRecipientSuccess() != null && !transaction.isRecipientSuccess()) {
            transaction.status(TransactionStatus.CANCELLED);
        }

        return new CommonResultData<>(BmResultStatus.SUCCESS, transactionRepository.save(transaction), syncRecipientTransaction.getMessage());
    }

    public Bill createBill(Transaction transaction, Boolean isSender) {
        var userLogin = SecurityUtils.getCurrentUserLogin();
        Bill bill = new Bill()
                .type(RequestType.STATUS)
                .amountRatio(transaction.getAmountRatio())
                .login(userLogin.orElse(null))
                .requestTime(Instant.now())
                .transactionId(transaction.getId())
                .responseTime(Instant.now());

        if (isSender) {
            bill
                    .recipientShortName("sync-sender-transaction")
                    .amount(transaction.getSenderAmount())
                    .account(transaction.getSenderAccount())
                    .currency(transaction.getSenderCcy())
                    .extId(transaction.getSenderTrId());
        } else {
            bill
                    .recipientShortName("sync-recipient-transaction")
                    .amount(transaction.getRecipientAmount())
                    .account(transaction.getRecipientAccount())
                    .currency(transaction.getRecipientCcy())
                    .extId(transaction.getRecipientTrId());
        }
        return billRepository.save(bill);
    }

    public HasData<Transaction> syncRecipientTransaction(Transaction transaction, CancelService cancelService) {
        var optionalBill = billRepository.findFirstByTransactionIdAndType(transaction.getId(), RequestType.P2P_CREDIT);
        log.info("Found transaction bill : {} on transaction id :{}", optionalBill, transaction.getId());

        if (optionalBill.isEmpty()) {
            return new CommonResultData<>(BmResultStatus.FAILED, transaction, "P2p credit bill not found on tran id " + transaction.getId());
        }

        var bill = createBill(transaction, false);
        log.info("Created bill : {}", bill);

        var transactionInfo = cancelService.getInfo(optionalBill.get(), TranType.P2P_CREDIT);

        Map<String, Object> request = new HashMap<>();
        Map<String, Object> response = new HashMap<>();

        log.debug("recipient tran from : {}  info : {}", cancelService.supportedType(), transactionInfo);
        log.debug("{} service return result : {}", cancelService.getClass().getSimpleName(), transactionInfo);

        if (!transactionInfo.isSuccess()) {
            request.put(REQUEST_KEY_NAME, transactionInfo.getData().getParams() == null ? null : transactionInfo.getData().getParams().getOrDefault(REQUEST_KEY_NAME, ""));
            response.put(RESPONSE_KEY_NANE, transactionInfo.getData().getParams() == null ? null : transactionInfo.getData().getParams().getOrDefault(RESPONSE_KEY_NANE, ""));

            bill
                    .responseJson(response)
                    .responseTime(Instant.now())
                    .requestJson(request)
                    .responseCode(String.valueOf(transactionInfo.getCode()))
                    .responseText(transactionInfo.getMessage())
                    .success(transactionInfo.isSuccess());
            billRepository.save(bill);
            transaction
                    .recipientStatus(transactionInfo.getData().getStatus())
                    .recipientSuccess(transactionInfo.getData().getSuccess())
                    .status(transactionInfo.getData().getTransactionStatus())
                    .recipientTrId(transactionInfo.getData().getPaymentId())
                    .nextRecipientStatusText(transactionInfo.getData().getStatus());
            transactionRepository.save(transaction);

            return new CommonResultData<>(BmResultStatus.FAILED, transaction, transactionInfo.getMessage());
        }
        request.put(REQUEST_KEY_NAME, transactionInfo.getData().getParams().getOrDefault(REQUEST_KEY_NAME, ""));
        response.put(RESPONSE_KEY_NANE, transactionInfo.getData().getParams().getOrDefault(RESPONSE_KEY_NANE, ""));

        bill
                .responseJson(response)
                .outExtId(transactionInfo.getData().getPaymentRef())
                .extId(transactionInfo.getData().getPaymentId())
                .responseText(transactionInfo.getData().getStatus())
                .responseTime(Instant.now())
                .requestJson(request)
                .success(transactionInfo.isSuccess());
        billRepository.save(bill);

        TransactionStatus recipientStatus = transactionInfo.getData().getTransactionStatus() == TransactionStatus.CANCELLED ? TransactionStatus.SENDER_CANCELLED : transactionInfo.getData().getTransactionStatus();
        transaction
                .recipientStatus(transactionInfo.getData().getStatus())
                .recipientSuccess(transactionInfo.getData().getSuccess())
                .status(recipientStatus)
                .recipientTrId(transactionInfo.getData().getPaymentId())
                .nextRecipientStatusText(transactionInfo.getData().getStatus());
        transaction = transactionRepository.save(transaction);

        return new CommonResultData<>(transaction);
    }

    public HasData<Transaction> syncSenderTransaction(Transaction transaction, CancelService cancelService) {
        var optionalBill = billRepository.findFirstByTransactionIdAndType(transaction.getId(), RequestType.P2P_DEBIT);
        log.info("Found transaction bill : {} on transaction id :{}", optionalBill, transaction.getId());
        if (optionalBill.isEmpty()) {
            return new CommonResultData<>(BmResultStatus.FAILED, transaction, "P2p debit bill not found on tran id " + transaction.getId());
        }

        var bill = createBill(transaction, true);
        log.info("Created bill : {}", bill);

        var transactionInfo = cancelService.getInfo(optionalBill.get(), TranType.P2P_DEBIT);
        log.debug("sender tran from : {}  info : {}", cancelService.supportedType(), transactionInfo);
        log.debug("{} service return result : {}", cancelService.getClass().getSimpleName(), transactionInfo);
        Map<String, Object> request = new HashMap<>();
        Map<String, Object> response = new HashMap<>();

        if (!transactionInfo.isSuccess()) {
            request.put(REQUEST_KEY_NAME, transactionInfo.getData().getParams() == null ? null : transactionInfo.getData().getParams().getOrDefault(REQUEST_KEY_NAME, ""));
            response.put(RESPONSE_KEY_NANE, transactionInfo.getData().getParams() == null ? null : transactionInfo.getData().getParams().getOrDefault(RESPONSE_KEY_NANE, ""));

            bill
                    .responseJson(response)
                    .responseTime(Instant.now())
                    .requestJson(request)
                    .success(transactionInfo.isSuccess());
            billRepository.save(bill);
            transaction
                    .recipientStatus(transactionInfo.getData().getStatus())
                    .recipientSuccess(transactionInfo.getData().getSuccess())
                    .status(transactionInfo.getData().getTransactionStatus())
                    .recipientTrId(transactionInfo.getData().getPaymentId())
                    .nextRecipientStatusText(transactionInfo.getData().getStatus());
            transactionRepository.save(transaction);
            return new CommonResultData<>(BmResultStatus.FAILED, transaction, transactionInfo.getMessage());
        }
        request.put(REQUEST_KEY_NAME, transactionInfo.getData().getParams().getOrDefault(REQUEST_KEY_NAME, ""));
        response.put(RESPONSE_KEY_NANE, transactionInfo.getData().getParams().getOrDefault(RESPONSE_KEY_NANE, ""));

        bill
                .outExtId(transactionInfo.getData().getPaymentRef())
                .extId(transactionInfo.getData().getPaymentId())
                .responseText(transactionInfo.getData().getStatus())
                .responseJson(response)
                .responseTime(Instant.now())
                .requestJson(request)
                .success(transactionInfo.isSuccess());
        billRepository.save(bill);
        TransactionStatus senderStatus = transactionInfo.getData().getTransactionStatus() == TransactionStatus.CANCELLED ? TransactionStatus.RECIPIENT_CANCELLED : transactionInfo.getData().getTransactionStatus();

        transaction
                .senderStatus(transactionInfo.getData().getStatus())
                .senderSuccess(transactionInfo.getData().getSuccess())
                .status(senderStatus)
                .senderTrId(transactionInfo.getData().getPaymentId())
                .nextSenderStatusText(transactionInfo.getData().getStatus());
        transaction = transactionRepository.save(transaction);

        return new CommonResultData<>(transaction);
    }
}
