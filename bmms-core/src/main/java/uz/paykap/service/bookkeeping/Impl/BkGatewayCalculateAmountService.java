package uz.paykap.service.bookkeeping.Impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import uz.devops.bmcore.enumeration.TransactionStatus;
import uz.paykap.domain.BookkeepingTemplate;
import uz.paykap.gateway.dto.BktCost;
import uz.paykap.repository.TransactionRepository;
import uz.paykap.service.bookkeeping.BkCalculateAmountService;

import java.time.Instant;

@Service
public class BkGatewayCalculateAmountService extends BkCalculateAmountService {

    private final static Logger log = LoggerFactory.getLogger(BkGatewayCalculateAmountService.class);

    @Override
    public BktCost getTranList(BookkeepingTemplate bookkeepingTemplate, Instant instantFrom, Instant instantTo) {
        log.debug("Request to get transactions with gateway !\n, bkTemplate: {} \n, from: {}\n, to: {}\n", bookkeepingTemplate, instantFrom, instantTo);

        return transactionRepository.getGatewayBktAmount(bookkeepingTemplate.getGateway().getName(), TransactionStatus.SUCCESS, true, instantFrom, instantTo, bookkeepingTemplate.getPayTypes());
    }

    private final TransactionRepository transactionRepository;

    public BkGatewayCalculateAmountService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }
}
