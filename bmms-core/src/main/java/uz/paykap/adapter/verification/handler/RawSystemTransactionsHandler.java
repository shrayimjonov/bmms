package uz.paykap.adapter.verification.handler;

import uz.paykap.domain.Transaction;

import java.time.Instant;
import java.util.HashSet;

public interface RawSystemTransactionsHandler {
    HashSet<Transaction> getRawTransactions(Instant fromTime, Instant toTime);
}
