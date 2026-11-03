package uz.paykap.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;
import uz.devops.bm.dummy.adapter.DummyDataAdapterV3;
import uz.devops.bmcore.enumeration.TransactionStatus;
import uz.paykap.PkbillmsApp;
import uz.paykap.config.Constants;
import uz.paykap.config.TestSecurityConfiguration;
import uz.paykap.domain.Transaction;
import uz.paykap.domain.enumeration.P2pProcessorType;
import uz.paykap.service.abs.AbstractAbsService;
import uz.paykap.service.dto.p2p.P2pAccount;
import uz.paykap.service.dto.p2p.P2pAccountType;
import uz.paykap.service.dto.p2p.P2pPayRequest;
import uz.paykap.service.dto.result.HasCodeAndMessage;
import uz.paykap.service.p2p.dto.response.P2pResponse;

import java.io.IOException;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@ActiveProfiles(profiles = {"davr", "default"})
@RunWith(SpringRunner.class)
@SpringBootTest(
        classes = {PkbillmsApp.class, TestSecurityConfiguration.class},
        properties = {"rateService.impl=cbuRateService"}
)
public class P2pNciForexTest {

    public static final String AGENT_TRAN_ID = "123";
    public static final long INFO_AMOUNT = 100L;
    public static final String TESHABOY_BOLTAYEV = "TESHABOY BOLTAYEV";
    public static final String JAVLON_ERALIYEV_COO = "TOSHMAT ESHMATOV COO";
    public static final String PROMO_CODE = "5mln";

    @Autowired
    private P2pCheckRequestDecoratorService p2pCheckRequestDecoratorService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AbstractAbsService abstractAbsService;

    @Test
    @Transactional
    @WithMockUser(DummyDataAdapterV3.DUMMY_AGENT)
    public void testNciToForexPay() throws IOException {
        var paramsString = "{ \"account\": \"22616000490016620002\", \"mfo\": \"00491\", \"name\": \"ESHMATOV TOSHMAT TESHABOYEVICH\", \"comment\": \"Test payment\" }";

        var senderAccount = new P2pAccount()
                .withType(P2pAccountType.ACCOUNT)
                .withCurrency(860)
                .withParams(objectMapper.readTree(paramsString));

        var paramsString2 =
                "{ \"account\": \"22616840890016620200\", \"comment\": \"00668Покупка (продажа) иностранной валюты\", \"fromSum\": 40880, \"toSum\": 4, \"cource\": 0.0001, \"fxDeal\":9}";

        var recipientAccount = new P2pAccount()
                .withType(P2pAccountType.FOREX)
                .withCurrency(840)
                .withParams(objectMapper.readTree(paramsString2));

        P2pPayRequest p2PPayRequest = new P2pPayRequest()
                .withFrom(senderAccount)
                .withTo(recipientAccount)
                .withAgentTranId("123")
                .withAmount(10000L);

//        Mockito.when(abstractAbsService.doForexTransaction(ArgumentMatchers.argThat(absForexTransactionRequest -> Objects.equals(absForexTransactionRequest.getAccCl(), "")))).thenCallRealMethod();

        P2pResponse simpleBillResponse = p2pCheckRequestDecoratorService.p2pPay(p2PPayRequest);
        assertThat(simpleBillResponse).isNotNull();
        assertThat(simpleBillResponse.isSuccess()).isTrue();
        assertThat(simpleBillResponse.getStatusCode()).isEqualTo(HasCodeAndMessage.SUCCESS_CODE);
        // TODO - add more realistic asserts
    }

    public void testPayTransactionV2(
            Transaction testTransaction,
            Instant beforeTranInstant,
            String agentLogin,
            TransactionStatus status,
            Long requestAmount,
            Long commDown,
            Long recipientAmount,
            String account,
            String senderAccount
    ) {
        testPayTransactionV2(
                testTransaction,
                beforeTranInstant,
                agentLogin,
                status,
                requestAmount,
                commDown,
                recipientAmount,
                account,
                senderAccount,
                P2pProcessorType.UZCARD,
                100L,
                10100L
        );
    }

    public void testPayTransactionV2(
            Transaction testTransaction,
            Instant beforeTranInstant,
            String agentLogin,
            TransactionStatus status,
            Long requestAmount,
            Long commDown,
            Long recipientAmount,
            String recipientAccount,
            String senderAccount,
            P2pProcessorType recipientPc,
            Long commissionUpAmount,
            Long senderAmount
    ) {
        testPayTransactionV2(
                testTransaction,
                beforeTranInstant,
                agentLogin,
                status,
                requestAmount,
                commDown,
                recipientAmount,
                recipientAccount,
                senderAccount,
                recipientPc,
                commissionUpAmount,
                senderAmount,
                P2pProcessorType.NCI
        );
    }

    public void testPayTransactionV2(
            Transaction testTransaction,
            Instant beforeTranInstant,
            String agentLogin,
            TransactionStatus status,
            Long requestAmount,
            Long commissionDownAmount,
            Long recipientAmount,
            String recipientAccount,
            String senderAccount,
            P2pProcessorType recipientPc,
            Long commissionUpAmount,
            Long senderAmount,
            P2pProcessorType senderProcessorType
    ) {
        testCommon(testTransaction, status, requestAmount, beforeTranInstant, agentLogin, commissionDownAmount, commissionUpAmount);

        testRecipient(testTransaction, recipientPc, recipientAccount, recipientAmount, beforeTranInstant);

        testSender(testTransaction, senderProcessorType, senderAccount, senderAmount, beforeTranInstant);
    }

    private void testCommon(
            Transaction testTransaction,
            TransactionStatus status,
            Long requestAmount,
            Instant beforeTranInstant,
            String agentLogin,
            Long commissionDownAmount,
            Long commissionUpAmount
    ) {
        assertThat(testTransaction.getRequestLogin()).isEqualTo(agentLogin);
        assertThat(testTransaction.getRequestAmount()).isEqualTo(requestAmount);
        //        assertThat(testTransaction.getRequestTrId()).isEqualTo(TRAN_ID);
        assertThat(testTransaction.getRequestGroup()).isEqualTo(agentLogin);
        assertThat(testTransaction.getComment()).isNotNull();

        assertThat(testTransaction.getSystemFeeAmount())
                .isEqualTo(
                        Double
                                .valueOf(
                                        testTransaction.getSystemFee() /
                                                100d *
                                                (testTransaction.getRequestAmount() - testTransaction.getSystemCommissionDownAmount())
                                )
                                .longValue()
                );
        assertThat(testTransaction.getSystemFee()).isEqualTo(0.0);
        assertThat(testTransaction.getSystemCommissionDownAmount()).isEqualTo(commissionDownAmount);
        assertThat(testTransaction.getSystemCommissionUpAmount()).isEqualTo(commissionUpAmount);
        assertThat(testTransaction.getAgentFeeAmount()).isEqualTo(0);

        assertThat(testTransaction.getStatus()).isEqualTo(status);
        assertThat(testTransaction.getDate()).isAfterOrEqualTo(beforeTranInstant);
        //        assertThat(testTransaction.getCcyRate()).isEqualTo(cbuService.getCurrencyRate(DummyDataAdapterV3.CURRENCY));
    }

    private void testSender(
            Transaction testTransaction,
            P2pProcessorType senderProcessorType,
            String senderAccount,
            Long senderAmount,
            Instant beforeTranInstant
    ) {
        testSender(testTransaction, senderProcessorType, senderAccount, senderAmount, beforeTranInstant, true);
    }

    private void testSender(
            Transaction testTransaction,
            P2pProcessorType senderProcessorType,
            String senderAccount,
            Long senderAmount,
            Instant beforeTranInstant,
            boolean success
    ) {
        testSender(testTransaction, senderProcessorType, senderAccount, senderAmount, beforeTranInstant, success, Constants.UZS_CURRENCY);
    }

    private void testSender(
            Transaction testTransaction,
            P2pProcessorType senderProcessorType,
            String senderAccount,
            Long senderAmount,
            Instant beforeTranInstant,
            boolean success,
            Integer senderCurrency
    ) {
        //        assertThat(testTransaction.getSenderServiceId()).isEqualTo(1183);//saved mfo in long format for confirm method // TODO - avazxon
        assertThat(testTransaction.getSenderGateway()).isEqualTo(senderProcessorType.name());
        assertThat(testTransaction.getSenderAccount()).isEqualTo(senderAccount);
        assertThat(testTransaction.getSenderAmount()).isEqualTo(senderAmount);
        assertThat(testTransaction.getSenderCcy()).isEqualTo(senderCurrency);
        //        assertThat(testTransaction.getSenderTrId()).isEqualTo(TRAN_ID);
        assertThat(testTransaction.isSenderSuccess()).isEqualTo(success);
        assertThat(testTransaction.getSenderDate()).isAfterOrEqualTo(beforeTranInstant);
        assertThat(testTransaction.getSenderStatus()).isNotBlank();
        assertThat(testTransaction.getSenderStatusText()).isNotEmpty();
        assertThat(testTransaction.getSenderBillId()).isNotNull();
    }

    private void testRecipient(
            Transaction testTransaction,
            P2pProcessorType recipientPc,
            String recipientAccount,
            Long recipientAmount,
            Instant beforeTranInstant
    ) {
        testRecipient(testTransaction, recipientPc, recipientAccount, recipientAmount, beforeTranInstant, true);
    }

    private void testRecipient(
            Transaction testTransaction,
            P2pProcessorType recipientPc,
            String recipientAccount,
            Long recipientAmount,
            Instant beforeTranInstant,
            boolean success
    ) {
        testRecipient(testTransaction, recipientPc, recipientAccount, recipientAmount, beforeTranInstant, success, Constants.UZS_CURRENCY);
    }

    private void testRecipient(
            Transaction testTransaction,
            P2pProcessorType recipientPc,
            String recipientAccount,
            Long recipientAmount,
            Instant beforeTranInstant,
            boolean success,
            Integer recipientCcy
    ) {
        assertThat(testTransaction.getRecipientAccount()).isEqualTo(recipientAccount);
        assertThat(testTransaction.getRecipientAmount()).isEqualTo(recipientAmount);
        assertThat(testTransaction.getRecipientCcy()).isEqualTo(recipientCcy);
        assertThat(testTransaction.getRecipientGateway()).isEqualTo(recipientPc.name());
        assertThat(testTransaction.getRecipientDate()).isAfterOrEqualTo(beforeTranInstant);
        assertThat(testTransaction.getRecipientStatus()).isNotEmpty();
        assertThat(testTransaction.getRecipientStatusText()).isNotEmpty();
        assertThat(testTransaction.isRecipientSuccess()).isEqualTo(success);
        assertThat(testTransaction.getRecipientBillId()).isNotNull();
        assertThat(testTransaction.getRecipientTrId()).isNotEmpty();
        //        assertThat(testTransaction.getRecipientServiceId()).isEqualTo(checkDTO.getRecipientId());
    }
}
