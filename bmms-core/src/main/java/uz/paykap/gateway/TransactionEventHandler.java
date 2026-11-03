package uz.paykap.gateway;

import uz.devops.bmcore.enumeration.TransactionStatus;

import java.time.Instant;

public interface TransactionEventHandler {
    void refreshTransactionStatus(Long tranId, Instant time, TransactionStatus transactionStatus);
}
