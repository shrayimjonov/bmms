package uz.paykap.gateway.impl;

import org.springframework.stereotype.Service;
import uz.devops.bmcore.CommonReconciliationService;
import uz.paykap.repository.ReconcileSummaryRepository;
import uz.paykap.repository.ReconciliationRepository;
import uz.paykap.service.TransactionService;

import java.time.Instant;

@Service
public class CommonReconciliationServiceImpl implements CommonReconciliationService {

    private final TransactionService transactionService;

    public CommonReconciliationServiceImpl(ReconciliationRepository reconciliationRepository, ReconcileSummaryRepository reconcileSummaryRepository, TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @Override
    public Long getTotalAmount(String gatewayName, Instant from, Instant to, int amountRatio) {
        return transactionService.getTotalAmount(gatewayName, from, to, amountRatio);
    }

    @Override
    public Long getTotalCount(String gatewayName, Instant from, Instant to) {
        return transactionService.getTotalCount(gatewayName, from, to);
    }
}
