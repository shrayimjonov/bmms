package uz.paykap.service.cancel.resolver;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uz.devops.bmcore.enumeration.PayType;
import uz.devops.bmcore.enumeration.TransactionStatus;
import uz.javlon.commons.base.SimpleResultData;
import uz.paykap.domain.Transaction;
import uz.paykap.repository.TransactionRepository;
import uz.paykap.service.cancel.CancelService;

import java.util.Objects;

@Service
@Slf4j
public class CancelResolver {

    private final TransactionRepository transactionRepository;
    private final CancelTransactionService cancelTransactionService;
    private final CancelServiceUtil cancelServiceUtil;
    private final SyncTransactionService syncTransactionService;

    public CancelResolver(TransactionRepository transactionRepository, CancelTransactionService cancelTransactionService, CancelServiceUtil cancelServiceUtil, SyncTransactionService syncTransactionService) {
        this.transactionRepository = transactionRepository;
        this.cancelTransactionService = cancelTransactionService;
        this.cancelServiceUtil = cancelServiceUtil;
        this.syncTransactionService = syncTransactionService;
    }

    public SimpleResultData<Transaction> cancel(Long tranId) {
        log.info("Cancel service transaction id: {}", tranId);
        var optionalTransaction = transactionRepository.findFirstByPayTypeAndId(PayType.P2P, tranId);
        log.debug("Found transaction : {}", optionalTransaction);

        if (optionalTransaction.isEmpty()) {
            return new SimpleResultData<>(false, null, new RuntimeException("Transaction not found in db"));
        }

        var simpleResultData = syncTransactionService.syncTransaction(optionalTransaction.get().getId());
        log.debug("synced transaction : {}", simpleResultData);

        Transaction transaction = simpleResultData.getData();

        if (transaction.getStatus() == TransactionStatus.CANCELLED || transaction.getStatus() == TransactionStatus.REVERSED) {
            return new SimpleResultData<>(false, transaction, new RuntimeException("Transaction status is " + transaction.getStatus()));
        }

        if (transaction.isRecipientSuccess() == Boolean.TRUE) {
            CancelService recipientCancelService = cancelServiceUtil.getCancelService(transaction.getRecipientGateway());
            log.debug("Cancel recipient transaction service : {}", recipientCancelService);
            transaction = cancelTransactionService.cancelRecipient(transaction, recipientCancelService);
            log.info("Recipient modified transaction : {}", transaction);
        }

        if (transaction.isSenderSuccess() == Boolean.TRUE) {
            CancelService cancelService = cancelServiceUtil.getCancelService(transaction.getSenderGateway());
            log.debug("Cancel sender transaction service : {}", cancelService);
            transaction = cancelTransactionService.cancelSender(transaction, cancelService);
            log.info("Sender modified transaction : {}", transaction);
        }

        if (transaction.isRecipientSuccess() != Boolean.TRUE && transaction.isSenderSuccess() != Boolean.TRUE) {
            transaction
                    .status(TransactionStatus.CANCELLED)
                    .nextRecipientStatusText(TransactionStatus.CANCELLED);
            transaction = transactionRepository.save(transaction);
            log.debug("Transaction saved with status cancelled: {}", transaction);
            return new SimpleResultData<>(transaction);
        }

        log.debug("finally transaction : {}", transaction);
        return new SimpleResultData<>(Objects.equals(transaction.getStatus(), TransactionStatus.CANCELLED), transaction, null);
    }

    public SimpleResultData<Transaction> cancelRecipient(Long tranId) {
        log.info("Cancel service transaction id: {}", tranId);
        var transaction = transactionRepository.findFirstByPayTypeAndId(PayType.P2P, tranId).orElse(null);
        log.debug("Found transaction : {}", transaction);

        if (transaction == null) {
            return new SimpleResultData<>(false, null, new RuntimeException("Transaction not found in db"));
        }

        if (transaction.getStatus() == TransactionStatus.CANCELLED || transaction.getStatus() == TransactionStatus.REVERSED) {
            return new SimpleResultData<>(false, transaction, new RuntimeException("Transaction status is " + transaction.getStatus()));
        }

        if (transaction.isRecipientSuccess() == Boolean.TRUE) {
            CancelService recipientCancelService = cancelServiceUtil.getCancelService(transaction.getRecipientGateway());
            log.debug("Cancel recipient transaction service : {}", recipientCancelService);
            transaction = cancelTransactionService.cancelRecipient(transaction, recipientCancelService);
            log.info("Recipient modified transaction : {}", transaction);
        }

        log.debug("finally transaction : {}", transaction);
        return new SimpleResultData<>(transaction);
    }

    public SimpleResultData<Transaction> cancelSender(Long tranId) {
        log.info("Cancel service transaction id: {}", tranId);
        var transaction = transactionRepository.findFirstByPayTypeAndId(PayType.P2P, tranId).orElse(null);
        log.debug("Found transaction : {}", transaction);

        if (transaction == null) {
            return new SimpleResultData<>(false, null, new RuntimeException("Transaction not found in db"));
        }

        if (transaction.getStatus() == TransactionStatus.CANCELLED || transaction.getStatus() == TransactionStatus.REVERSED) {
            return new SimpleResultData<>(false, transaction, new RuntimeException("Transaction status is " + transaction.getStatus()));
        }

        if (transaction.isSenderSuccess() == Boolean.TRUE) {
            CancelService cancelService = cancelServiceUtil.getCancelService(transaction.getSenderGateway());
            log.debug("Cancel sender transaction service : {}", cancelService);
            transaction = cancelTransactionService.cancelSender(transaction, cancelService);
            log.info("Sender modified transaction : {}", transaction);
        }

        log.debug("finally transaction : {}", transaction);
        return new SimpleResultData<>(transaction);
    }
}
