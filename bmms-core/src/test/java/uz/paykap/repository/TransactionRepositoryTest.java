package uz.paykap.repository;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;
import uz.devops.bmcore.enumeration.PayType;
import uz.devops.bmcore.enumeration.TransactionStatus;
import uz.paykap.IntegrationTest;
import uz.paykap.domain.Gateway;
import uz.paykap.domain.Recipient;
import uz.paykap.domain.Transaction;
import uz.paykap.gateway.dto.GatewaySuccessErrorTran;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Transactional
@RunWith(SpringRunner.class)
@IntegrationTest
public class TransactionRepositoryTest {

    private final Logger log = LoggerFactory.getLogger(TransactionRepositoryTest.class);
    private static final Integer CCY_RATE_USD = 840;
    private static final Integer CCY_RATE_UZS = 860;
    Instant EPOCH = Instant.parse("2020-01-01T15:30:45.123Z");

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private GatewayRepository gatewayRepository;

    @Autowired
    private RecipientRepository recipientRepository;

    @Test
    public void createRecipient() {
        Recipient recipient = new Recipient();
        recipient.setShortName("rec2");
        recipient.setServiceId("123312");
        recipient.setAmountRatio(45);
        recipient.setCcy(860);
        recipient.setLimitRatio(7);
        recipient.active(true);
        recipient.setTitles(recipient.getTitles());
        recipient.setFullNames(recipient.getFullNames());
        Recipient savedRecipient = recipientRepository.save(recipient);
        log.info("Recipient Saved: {}", savedRecipient);
    }

    public void createGateway() {
        Gateway gateway = new Gateway();
        gateway.setName("gateway");
        gateway.setOrg("org");
        gateway.setActive(true);
        gateway.setMfo("mfo");
        gateway.setBan("ban");
        gateway.setContract("contract");
        Gateway savedGateway = gatewayRepository.save(gateway);
        log.info("Gateway Saved: {}", savedGateway);
    }

    @Test
    public void createTransactionUsd() {
        Transaction transaction = new Transaction();
        transaction.setRequestTrId("123456");
        transaction.setRequestLogin("login");
        transaction.setComment("comment");
        transaction.setPayType(PayType.UZCARD);
        transaction.setAmountRatio(123);

        transaction.setGatewayAmount(13.5);
        transaction.setRecipientDate(Instant.EPOCH);
        transaction.setCcyRate(CCY_RATE_UZS);
        transaction.setStatus(TransactionStatus.SUCCESS);
        Transaction savedTransaction = transactionRepository.save(transaction);
        log.info("Transaction saved: {}", savedTransaction);
    }

    @Test
    public void createTransactionUzs() {
        Transaction transaction = new Transaction();
        transaction.setRequestTrId("123456");
        transaction.setRequestLogin("login");
        transaction.setComment("comment");
        transaction.setPayType(PayType.HUMO);
        transaction.setAmountRatio(123);
        transaction.setDate(EPOCH);
        transaction.setRecipientName("rec2");

        transaction.setGatewayAmount(13.5);
        transaction.setRecipientDate(Instant.EPOCH);
        transaction.setCcyRate(CCY_RATE_USD);
        transaction.setStatus(TransactionStatus.SUCCESS);
        Transaction savedTransaction = transactionRepository.save(transaction);
        log.info("Transaction saved: {}", savedTransaction);
    }

    @Test
    public void getTransCountByGateway() {
        List<TransactionStatus> errorStatuses = new ArrayList<>();
        errorStatuses.add(TransactionStatus.FAILED);
        errorStatuses.add(TransactionStatus.CHECK_FAIL);
        errorStatuses.add(TransactionStatus.NEW);

        Set<GatewaySuccessErrorTran> transCountByGateway = transactionRepository.getTransCountByGateway(
                Instant.ofEpochSecond(0),
                Instant.now(),
                errorStatuses
        );
        //        Set<GatewaySuccessErrorTran> transCountByGateway2 = transactionRepository.getTransCountByGateway(Instant.now(), Instant.now().plusSeconds(300L), Collections.singleton(TransactionStatus.SUCCESS), Collections.singleton(TransactionStatus.FAILED), 80L);
        System.out.println(transCountByGateway);
    }
}
