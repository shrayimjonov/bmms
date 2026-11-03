package uz.paykap.service.bookkeeping.Impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import uz.devops.bmcore.enumeration.PayType;
import uz.devops.bmcore.enumeration.TransactionStatus;
import uz.paykap.domain.Bill;
import uz.paykap.domain.BookkeepingTemplate;
import uz.paykap.domain.Transaction;
import uz.paykap.gateway.dto.BktCost;
import uz.paykap.repository.BillRepository;
import uz.paykap.repository.TransactionRepository;
import uz.paykap.service.bookkeeping.BkCalculateAmountService;

import java.time.Instant;
import java.util.*;

@Service
public class BkSubRecipientCalculateAmountService extends BkCalculateAmountService {

    private final static Logger log = LoggerFactory.getLogger(BkSubRecipientCalculateAmountService.class);
    public static final String SOATO = "soato";

    @Override
    public BktCost getTranList(BookkeepingTemplate bookkeepingTemplate, Instant instantFrom, Instant instantTo) {
        log.debug("Request to get transactions with sub recipient !\n, bkTemplate: {} \n, from: {}\n, to: {}\n", bookkeepingTemplate, instantFrom, instantTo);

        List<Transaction> transList = transactionRepository.findAllByRecipientNameAndStatusAndRecipientSuccessAndRecipientDateBetweenAndPayTypeIn(bookkeepingTemplate.getRecipient().getShortName(), TransactionStatus.SUCCESS, true, instantFrom, instantTo, bookkeepingTemplate.getPayTypes()).orElse(Collections.emptyList());

        List<Transaction> transactionsWithSoato = new ArrayList<>();

        for (Transaction transaction : transList) {

            Optional<Bill> billOptional = billRepository.findTop1ByIdAndSuccess(transaction.getRecipientBillId(), true);

            if (billOptional.isPresent()) {
                if (billOptional.get().getRequestJson().containsKey(SOATO)) {
                    String id = String.valueOf(billOptional.get().getRequestJson().get(SOATO));
                    if (Objects.equals(Long.valueOf(id), bookkeepingTemplate.getSubRecipientId())) {
                        transactionsWithSoato.add(transaction);
                    }
                }
            }

        }
        log.debug("Transactions with sub recipient: {}", transactionsWithSoato);
        Long amount = 0L;
        Long fee = 0L;
        Long commission = 0L;
        for (PayType payType : bookkeepingTemplate.getPayTypes()) {
            for (Transaction transaction : transactionsWithSoato) {
                if (payType == transaction.getPayType()) {
                    amount += transaction.getRequestAmount();
                    if (transaction.getSystemFeeAmount() != null) {
                        fee += transaction.getSystemFeeAmount();
                    }
                    if (transaction.getSystemCommissionDownAmount() != null) {
                        commission += transaction.getSystemCommissionDownAmount();
                    }
                }
            }
        }

        return new BktCost(amount, commission, fee);
    }

    private final TransactionRepository transactionRepository;
    private final BillRepository billRepository;

    public BkSubRecipientCalculateAmountService(TransactionRepository transactionRepository, BillRepository billRepository) {
        this.transactionRepository = transactionRepository;
        this.billRepository = billRepository;
    }
}
