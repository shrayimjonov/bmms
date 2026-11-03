package uz.paykap.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.text.StrSubstitutor;
import org.apache.commons.text.StringSubstitutor;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;
import uz.devops.bm.dummy.adapter.DummyDataAdapterV3;
import uz.devops.bmcore.enumeration.TransactionStatus;
import uz.javlon.svgate.service.simulation.PredefinedCardRepo2;
import uz.paykap.PkbillmsApp;
import uz.paykap.config.Constants;
import uz.paykap.config.TestSecurityConfiguration;
import uz.paykap.domain.*;
import uz.paykap.domain.enumeration.DepositState;
import uz.paykap.domain.enumeration.P2pProcessorType;
import uz.paykap.repository.*;
import uz.paykap.service.abs.AbstractAbsService;
import uz.paykap.service.abs.response.AbsAccountResult;
import uz.paykap.service.abs.response.MultiTransactionResultDTO;
import uz.paykap.service.dto.p2p.BmResultStatus;
import uz.paykap.service.dto.p2p.P2pAccount;
import uz.paykap.service.dto.p2p.P2pAccountType;
import uz.paykap.service.dto.p2p.P2pPayRequest;
import uz.paykap.service.dto.result.BmResultData;
import uz.paykap.service.p2p.dto.response.P2pResponse;
import uz.paykap.web.rest.AgentResourceIntTest;

import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@ActiveProfiles(profiles = {"davr", "default"})
@RunWith(SpringRunner.class)
@SpringBootTest(
    classes = {PkbillmsApp.class, TestSecurityConfiguration.class},
    properties = {"rateService.impl=davrRateService", "nci-is-api.simulate=true"}
)
public class NciAccountToUzcardMultiTranTest {
    @Autowired
    private P2pCheckRequestDecoratorService p2pCheckRequestDecoratorService;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private BillRepository billRepository;
    @Autowired
    private P2pParamRepository p2pParamRepository;
    @Autowired
    private P2pCommissionRepository p2pCommissionRepository;
    @Autowired
    private P2pConditionRepository p2pConditionRepository;
    @Autowired
    private TransactionRepository transactionRepository;
    @MockBean
    private AbstractAbsService abstractAbsService;
    @Autowired
    private AgentRepository agentRepository;
    @Autowired
    private DepositRepository depositRepository;

    public void setupMock() {
        MultiTransactionResultDTO multiTransactionResultDTO = new MultiTransactionResultDTO();
        Mockito.when(abstractAbsService.doMultiTransaction(ArgumentMatchers.any())).thenReturn(new BmResultData<>(BmResultStatus.SUCCESS, Collections.singletonList(multiTransactionResultDTO)));
        AbsAccountResult accountResult = new AbsAccountResult();
        accountResult.setErrorCode("0");
        accountResult.setErrorNote("SUCCESS");
        Mockito.when(abstractAbsService.sendGetAccountRequest(ArgumentMatchers.any())).thenReturn(new BmResultData<>(BmResultStatus.SUCCESS, accountResult));
    }

    @SneakyThrows
    @Test
    public void testString() {
        ObjectMapper objectMapper = new ObjectMapper();

        Map<String, Object> baseMap = new HashMap<String, Object>();

        baseMap.put("tran", objectMapper.writeValueAsString(new Transaction().id(15L)));
        baseMap.put("bill", objectMapper.writeValueAsString(new Bill().id(115L)));

        StringSubstitutor strsub = new StringSubstitutor(baseMap);

        String str = "This is ${bill.id} from ${tran.id}";
        System.out.println(new StringSubstitutor(objectMapper.convertValue(baseMap, Map.class)).replace(str));
        System.out.println(strsub.replace(str));
    }

    @SneakyThrows
    @Test
    public void testString2() {
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> baseMap = new HashMap<String, Object>();

        baseMap.put("Name", "Raja");

        Map<String, Object> address = new HashMap<String, Object>();
        address.put("Street", "RAMA");

        baseMap.put("Address", address);
        StrSubstitutor strsub = new StrSubstitutor(baseMap);
        StrSubstitutor baseSub = new StrSubstitutor(address);

        String str = "This is ${Name} from ${Address[Street]}";
        StringSubstitutor
            .replaceSystemProperties("You are running with java.version = ${java.version} and os.name = ${os.name}.");
        System.out.println(baseSub.replace(strsub.replace(str)));
    }

    @SneakyThrows
    @Test
    @Transactional
    @WithMockUser(DummyDataAdapterV3.DUMMY_AGENT)
    public void payNciAccount2UzcardToken() {
        createDeposit("TOSHMAT ESHMATOV");
        setupMock();
        int databaseSizeBeforeCreate = transactionRepository.findAll().size();
        int billSizeBeforeCreate = billRepository.findAll().size();
        Instant instant = Instant.now();
        long amount = 10000L;
//        var p2pParam = p2pParamRepository.save(getP2pParam());
        String senderNciAccount = "20208000000548837006";

        var paramsString1 = "{ \"account\": \"" + senderNciAccount + "\", \"mfo\": \"00491\", \"name\": \"TOSHMAT ESHMATOV\", \"comment\": \"Test payment\" }";

        var senderAccount = new P2pAccount()
            .withType(P2pAccountType.ACCOUNT)
            .withCurrency(860)
            .withParams(objectMapper.readTree(paramsString1));

        var paramsString2 = "{ \"token\": \"" + PredefinedCardRepo2.DEFAULT_TOKEN + "\", \"bin\": \"860049\" }";

        var recipientAccount = new P2pAccount()
            .withType(P2pAccountType.TOKEN)
            .withCurrency(860)
            .withParams(objectMapper.readTree(paramsString2));

        P2pPayRequest p2PPayRequest = new P2pPayRequest()
            .withFrom(senderAccount)
            .withTo(recipientAccount)
            .withAgentTranId("123")
            .withAmount(amount);

        P2pResponse simpleBillResponse = p2pCheckRequestDecoratorService.p2pPay(p2PPayRequest);

        log.info(String.valueOf(simpleBillResponse));

        Assertions.assertThat(simpleBillResponse).isNotNull();
        Assertions.assertThat(simpleBillResponse.isSuccess()).isTrue();

        List<Transaction> transactionList = transactionRepository.findAll();
        assertThat(transactionList).hasSize(databaseSizeBeforeCreate + 1);
        Transaction testTransaction = transactionList.get(transactionList.size() - 1);

        testPayTransactionV2(
            testTransaction,
            instant,
            DummyDataAdapterV3.DUMMY_AGENT,
            TransactionStatus.SUCCESS,
            amount,
            0L,
            amount,
            PredefinedCardRepo2.DEFAULT_TOKEN,
            senderNciAccount
        );

        List<Bill> billList = billRepository.findAll();
        assertThat(billList).hasSize(billSizeBeforeCreate + 3);

        assertThat(testTransaction.getComment()).isEqualTo("ACCOUNT -> TOKEN (type:UNIVERSAL_CREDIT)");

        testCommon(testTransaction, TransactionStatus.SUCCESS, amount, instant, DummyDataAdapterV3.DUMMY_AGENT, 0L, 0L);
        testSender(testTransaction, P2pProcessorType.NCI, senderNciAccount, amount + 100L, instant, true);
        testRecipient(testTransaction, P2pProcessorType.UZCARD, "AAAAAAAAAA", amount, instant, true);
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

    private Agent createAgent(String agentName) {
        Agent agent = AgentResourceIntTest.createEntity(null);
        agent.setShortName(agentName);
        agent.orgName("sd");
        agent.setActive(true);
        return agentRepository.save(agent);
    }

    private Deposit createDeposit(String agentName) {
        Deposit deposit = new Deposit();
        deposit.setBalance(100000000000L);
        deposit.setAgent(createAgent(agentName));
        deposit.setOverdraftLimit(100000000000L);
        deposit.setBalanceCcy(860);
        deposit.setBookedBalance(100000000000L);
        deposit.setLimitInform(10000000000L);
        deposit.setLimitDisable(0L);
        deposit.setBlocked(false);
        deposit.setUnlimit(true);
        deposit.setState(DepositState.ACTIVE);
        return depositRepository.save(deposit);
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
//        assertThat(testTransaction.getSenderAmount()).isEqualTo(senderAmount);
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

    private void testCommon(Transaction testTransaction, TransactionStatus status, Long requestAmount, Instant beforeTranInstant, String agentLogin, Long commissionDownAmount, Long commissionUpAmount) {
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
            0L,
            10100L
        );
    }

    public void testPayTransactionV2(Transaction testTransaction, Instant beforeTranInstant, String agentLogin, TransactionStatus status, Long requestAmount, Long commDown, Long recipientAmount, String recipientAccount, String senderAccount, P2pProcessorType recipientPc, Long commissionUpAmount, Long senderAmount) {
        testPayTransactionV2(testTransaction, beforeTranInstant, agentLogin, status, requestAmount, commDown, recipientAmount, recipientAccount, senderAccount, recipientPc, commissionUpAmount, senderAmount, P2pProcessorType.NCI);
    }

    public void testPayTransactionV2(Transaction testTransaction, Instant beforeTranInstant, String agentLogin, TransactionStatus status, Long requestAmount, Long commissionDownAmount, Long recipientAmount, String recipientAccount, String senderAccount, P2pProcessorType recipientPc, Long commissionUpAmount, Long senderAmount, P2pProcessorType senderProcessorType) {
        testCommon(testTransaction, status, requestAmount, beforeTranInstant, agentLogin, commissionDownAmount, commissionUpAmount);

        testRecipient(testTransaction, recipientPc, recipientAccount, recipientAmount, beforeTranInstant);

        testSender(testTransaction, senderProcessorType, senderAccount, senderAmount, beforeTranInstant);
    }

    private P2pParam getP2pParam() {
        P2pCondition p2pCondition = getP2pCondition();
        P2pCommission p2pCommission = getP2pCommission();

        P2pParam p2pParam = new P2pParam();
        p2pParam.setPriority(1);
        p2pParam.setActive(true);
        p2pParam.setSenderPc(P2pProcessorType.NCI);
        p2pParam.setRecipientPc(P2pProcessorType.UZCARD);
        p2pParam.setSenderCondition(p2pCondition);
        p2pParam.setRecipientCondition(getUzcardP2pCondition());
        p2pParam.setSenderPercent(p2pCommission);
        p2pParam.setRecipientPercent(p2pCommission);
        p2pParam.setValue("{\n" +
            "  \"sender\": {\n" +
            "    \"data\": [\n" +
            "      {\n" +
            "        \"CL\": \"${CL}\",\n" +
            "        \"CO\": \"${CO}\",\n" +
            "        \"SUM\": \"0\",\n" +
            "        \"PUR\": \"${purposeId} ${purpose}\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"CL\": \"${CL}\",\n" +
            "        \"CO\": \"${CO}\",\n" +
            "        \"SUM\": \"0\",\n" +
            "        \"PUR\": \"${purposeId} Тестовый платеж2\"\n" +
            "      }\n" +
            "    ]\n" +
            "  },\n" +
            "  \"recipient\": {\n" +
            "    \"merchantId\": 123,\n" +
            "    \"terminalId\": 234,\n" +
            "    \"port\": 4444,\n" +
            "    \"creditType\": \"UNIVERSAL_CREDIT\"\n" +
            "  }\n" +
            "}\n");
        return p2pParamRepository.save(p2pParam);
    }

    private P2pCommission getP2pCommission() {
        P2pCommission p2pCommission = new P2pCommission();
        p2pCommission.setName("test");
        p2pCommission.setUp(0D);
        p2pCommission.setDown(0D);
        p2pCommission.setFee(0D);
        p2pCommission.setDescription("test");

        return p2pCommissionRepository.save(p2pCommission);
    }

    private P2pCondition getP2pCondition() {
        P2pCondition p2pCondition = new P2pCondition();
        p2pCondition.setValue("20208");
        return p2pConditionRepository.save(p2pCondition);
    }

    private P2pCondition getUzcardP2pCondition() {
        P2pCondition p2pCondition = new P2pCondition();
        p2pCondition.setValue("8600");
        return p2pConditionRepository.save(p2pCondition);
    }
}
