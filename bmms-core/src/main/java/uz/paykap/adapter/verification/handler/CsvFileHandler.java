package uz.paykap.adapter.verification.handler;

import uz.paykap.domain.Transaction;

public interface CsvFileHandler {
    String getCsvLine(Transaction transaction);
}
