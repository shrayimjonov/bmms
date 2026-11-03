package uz.paykap.web.rest.errors;

import lombok.Getter;
import uz.javlon.commons.exception.CodedException;
import uz.paykap.domain.Transaction;

import javax.validation.constraints.NotNull;

public class BmTransactionException extends RuntimeException {

    @Getter
    private final Transaction transaction;

    public BmTransactionException(@NotNull Transaction transaction) {
        super(transaction != null ? transaction.getFullStatusText() : "transaction is null");
        if (transaction == null) {
            transaction = new Transaction();
        }
        this.transaction = transaction;
    }

    public BmTransactionException(@NotNull Transaction transaction, String message) {
        super(message);
        if (transaction == null) {
            transaction = new Transaction();
        }
        this.transaction = transaction;
    }

    public BmTransactionException(@NotNull Transaction transaction, CodedException codedException) {
        super(codedException != null ? codedException.getLocalizedMessage() : (transaction.getFullStatusText()));
        if (transaction == null) {
            transaction = new Transaction();
        }
        this.transaction = transaction;
    }
}
