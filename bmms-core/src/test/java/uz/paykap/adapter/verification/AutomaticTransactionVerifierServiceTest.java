package uz.paykap.adapter.verification;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import uz.paykap.IntegrationTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;
import uz.devops.bm.paynet.adapter.PaynetJsonDataAdapterV3;
import uz.devops.bm.paynet.config.PaynetConstants;
import uz.devops.bmcore.enumeration.TransactionStatus;
import uz.paykap.IntegrationTest;
import uz.paykap.PkbillmsApp;
import uz.paykap.config.ApplicationProperties;
import uz.paykap.config.Constants;
import uz.paykap.domain.Transaction;
import uz.paykap.repository.TransactionRepository;
import uz.paykap.web.rest.TransactionResourceIntTest;

import java.io.File;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@IntegrationTest
@ExtendWith(MockitoExtension.class)
@AutoConfigureMockMvc
@WithMockUser
public class AutomaticTransactionVerifierServiceTest {

    @Autowired
    private AutomaticTransactionVerifierService transactionVerifier;

    @Autowired
    private ApplicationProperties properties;

    @Autowired
    private TransactionRepository transactionRepository;

    private void initTest() {
        Transaction transaction = TransactionResourceIntTest.createEntity(null);
        transaction.setRequestTrId("1");
        transaction.setStatus(TransactionStatus.FAILED);
        transaction.requestGroup(Constants.WMAGENT_LOGIN);
        transaction.requestLogin(Constants.WMAGENT_LOGIN);
        transaction.setRecipientGateway(PaynetConstants.PAYNET_JSON_GATEWAY_NAME);
        transaction.setGatewayAmount(100d);
        transaction.setRecipientAmount(10000L);
        transaction.setRequestAmount(10000L);
//        System.out.println(Instant.now().atOffset(ZoneOffset.ofHours(0)));
        transaction.setDate(LocalDateTime.now().toInstant(ZoneOffset.ofHours(0)));
        transaction.setRecipientDate(transaction.getDate());

        transactionRepository.save(transaction);

        Transaction transaction2 = TransactionResourceIntTest.createEntity(null);
        transaction.setRequestTrId("2");
        transaction2.setStatus(TransactionStatus.CHECK_FAIL);
        transaction2.requestGroup(Constants.WMAGENT_LOGIN);
        transaction2.requestLogin(Constants.WMAGENT_LOGIN);
        transaction2.setRecipientGateway(PaynetConstants.PAYNET_JSON_GATEWAY_NAME);
        transaction2.setGatewayAmount(100d);
        transaction2.setRecipientAmount(10000L);
        transaction2.setRequestAmount(10000L);
//        System.out.println(Instant.now().atOffset(ZoneOffset.ofHours(0)));
        transaction2.setDate(LocalDateTime.now().toInstant(ZoneOffset.ofHours(0)));
        transaction2.setRecipientDate(transaction.getDate());

        transactionRepository.save(transaction2);
    }

    @Test
    @Transactional
    public void startVerificationTest() {
        if (properties.getTransactionVerifier().getActive()) {
            initTest();
            File file = deleteFile();

            transactionVerifier.startVerification();

            if (properties.getPusher() != null) {
                if (!properties.getPusher().getEnabled() && properties.getSimulateSendFileToEmail()) {
                    issetFile(file);
                }
            }
        }
    }

    public static void issetFile(File file) {
        try {
            Thread.sleep(5000);
            boolean status = file.exists();
            assertTrue(status);
        } catch (InterruptedException e) {
            fail();
            e.printStackTrace();
        }
    }

    private File deleteFile() {
        String fileName = System.getProperty("user.dir") + File.separator + "src" + File.separator + "main" + File.separator + "resources" + File.separator + "testEventListener" + File.separator + "test.txt";
        File file = new File(fileName);

        if (file.exists()) {
            file.delete();
        }
        return file;
    }
}
