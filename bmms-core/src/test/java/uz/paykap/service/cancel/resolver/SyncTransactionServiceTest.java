package uz.paykap.service.cancel.resolver;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;
import uz.devops.bmcore.enumeration.RequestType;
import uz.paykap.IntegrationTest;
import uz.paykap.domain.Bill;
import uz.paykap.domain.Transaction;
import uz.paykap.repository.BillRepository;
import uz.paykap.repository.TransactionRepository;
import uz.paykap.security.SecurityUtils;
import uz.paykap.service.cancel.impl.HumoCancelService;

import java.time.Instant;

@Transactional
@RunWith(SpringRunner.class)
@IntegrationTest
public class SyncTransactionServiceTest {

    @Autowired
    private SyncTransactionService syncTransactionService;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private HumoCancelService humoCancelService;

    @Autowired
    private BillRepository billRepository;

    @Test
    @Transactional
    public void syncTransactionSender() {
        var transaction = CancelResolverTest.createTransaction(true, true);
        transactionRepository.save(transaction);
        createSenderBill(transaction);
        createRecipientBill(transaction);

        var resultData = syncTransactionService.syncSenderTransaction(transaction, humoCancelService);
        Assertions.assertThat(resultData).isNotNull();
        Assertions.assertThat(resultData.isSuccess()).isNotNull();
        Assertions.assertThat(resultData.isSuccess()).isTrue();
    }

    @Test
    @Transactional
    public void syncTransactionRecipient() {
        var transaction = CancelResolverTest.createTransaction(true, true);
        transactionRepository.save(transaction);
        createSenderBill(transaction);
        createRecipientBill(transaction);

        var resultData = syncTransactionService.syncRecipientTransaction(transaction, humoCancelService);
        Assertions.assertThat(resultData).isNotNull();
        Assertions.assertThat(resultData.isSuccess()).isNotNull();
        Assertions.assertThat(resultData.isSuccess()).isTrue();
    }

    public Bill createSenderBill(Transaction transaction) {
        var userLogin = SecurityUtils.getCurrentUserLogin();
        Bill bill = new Bill()
                .type(RequestType.P2P_DEBIT)
                .amountRatio(transaction.getAmountRatio())
                .login(userLogin.orElse(null))
                .requestTime(Instant.now())
                .transactionId(transaction.getId())
                .responseTime(Instant.now())
                .recipientShortName("debit")
                .amount(transaction.getSenderAmount())
                .account(transaction.getSenderAccount())
                .currency(transaction.getSenderCcy())
                .extId(transaction.getSenderTrId())
                .outExtId(String.valueOf(System.currentTimeMillis()));

        return billRepository.save(bill);
    }

    public Bill createRecipientBill(Transaction transaction) {
        var userLogin = SecurityUtils.getCurrentUserLogin();
        Bill bill = new Bill()
                .type(RequestType.P2P_CREDIT)
                .amountRatio(transaction.getAmountRatio())
                .login(userLogin.orElse(null))
                .requestTime(Instant.now())
                .transactionId(transaction.getId())
                .responseTime(Instant.now())
                .recipientShortName("credit")
                .amount(transaction.getRecipientAmount())
                .account(transaction.getRecipientAccount())
                .currency(transaction.getRecipientCcy())
                .extId(transaction.getRecipientTrId())
                .outExtId(String.valueOf(System.currentTimeMillis()));

        return billRepository.save(bill);
    }
}
