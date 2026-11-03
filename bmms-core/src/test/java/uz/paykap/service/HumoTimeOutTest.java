package uz.paykap.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assume;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;
import uz.devops.bm.dummy.adapter.DummyDataAdapterV3;
import uz.devops.bmcore.dto.request.CheckRequest;
import uz.devops.bmcore.dto.request.CommonCheckRequest;
import uz.devops.bmcore.dto.request.CommonPayRequest;
import uz.devops.bmcore.dto.request.PayRequest;
import uz.devops.bmcore.dto.response.SimpleBillResponse;
import uz.devops.bmcore.enumeration.PayType;
import uz.devops.bmcore.enumeration.RequestType;
import uz.devops.bmcore.enumeration.TransactionStatus;
import uz.devops.humo.service.impl.HumoGatewaySenderServiceSimulation;
import uz.devops.humoMiddleware.service.impl.DummyHumoMiddlewareServiceImpl;
import uz.paykap.IntegrationTest;
import uz.paykap.adapter.DummyAdapterDelegate;
import uz.paykap.agent.tran.CommonTranServiceV3;
import uz.paykap.config.Constants;
import uz.paykap.domain.Bill;
import uz.paykap.domain.Recipient;
import uz.paykap.domain.Transaction;
import uz.paykap.repository.BillRepository;
import uz.paykap.repository.RecipientRepository;
import uz.paykap.repository.TransactionRepository;
import uz.paykap.sender.impl.HumoSenderService;
import uz.paykap.service.dto.p2p.BmResultStatus;
import uz.paykap.service.dto.p2p.P2pAccount;
import uz.paykap.service.dto.p2p.P2pAccountType;
import uz.paykap.service.dto.p2p.P2pPayRequest;
import uz.paykap.service.humo.HumoP2pHelperService;
import uz.paykap.service.p2p.dto.response.P2pResponse;
import uz.paykap.web.rest.errors.BmTransactionException;
import uz.paykap.web.rest.util.DummyUtils;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static uz.devops.bm.dummy.adapter.DummyDataAdapterV3.DUMMY_AGENT;

@Ignore
@Slf4j
@RunWith(SpringRunner.class)
@IntegrationTest
public class HumoTimeOutTest {

    public static final String SENDER_HUMO_REAL_PAN = "9860600000000000";
    public static final String SENDER_HUMO_MASKED_PAN = "986060BBAACC0304";
    public static final String RECEIVER_HUMO_REAL_PAN = "9860600000000000";
    public static final String RECEIVER_HUMO_MASKED_PAN = "986060ABBCDE0203";
    public static final String HUMO_EXP_DATE = "0327";
    public static final String GATEWAY_ACCOUNT = "933999959";

    @Autowired
    private P2pCheckRequestDecoratorService p2pCheckRequestDecoratorService;
    @Autowired
    private CommonTranServiceV3 commonTranServiceV3;
    @Autowired
    private DummyAdapterDelegate dummyAdapterDelegate;
    @Autowired
    private RecipientRepository recipientRepository;
    @Autowired
    private MappingJackson2HttpMessageConverter jacksonMessageConverter;
    @Autowired
    private BillRepository billRepository;
    @Autowired
    private TransactionRepository transactionRepository;

    private static long checkId;

    @SneakyThrows
    @Test
    @Transactional
    @WithMockUser(DummyDataAdapterV3.DUMMY_AGENT)
    public void p2pHumoPanToPanSenderTimeOutTest() {
        int databaseSizeBeforeCreate = billRepository.findAll().size();

        P2pPayRequest request = getRequest(HumoGatewaySenderServiceSimulation.TIMEOUT_PAN, RECEIVER_HUMO_REAL_PAN, P2pAccountType.PAN, P2pAccountType.PAN);

        P2pResponse simpleBillResponse = p2pCheckRequestDecoratorService.p2pPay(request);

        assertResultToWaiting(simpleBillResponse);

        List<Bill> billList = billRepository.findAll();

        assertBillSize(billList, databaseSizeBeforeCreate, 2);

        assertP2pBill(billList, HumoSenderService.HUMO_DEBIT_CREATE, RequestType.P2P_DEBIT, false, TransactionStatus.WAITING.name());

        List<Transaction> transactions = transactionRepository.findAll();

        assertTransactionIsNotNull(transactions);

        Transaction transaction = transactions.get(0);

        assertTransactionStatus(transaction, TransactionStatus.S_NEED_CONFIRM);

        assertTransactionSender(transaction, TransactionStatus.WAITING.name(), false, HumoGatewaySenderServiceSimulation.TIMEOUT_PAN);

        assertTransactionRecipient(transaction, null, null, RECEIVER_HUMO_REAL_PAN);
    }

    @SneakyThrows
    @Test
    @Transactional
    @WithMockUser(DummyDataAdapterV3.DUMMY_AGENT)
    public void p2pHumoPanToPanReceiverTimeOutTest() {
        int databaseSizeBeforeCreate = billRepository.findAll().size();

        P2pPayRequest request = getRequest(SENDER_HUMO_REAL_PAN, HumoGatewaySenderServiceSimulation.TIMEOUT_PAN, P2pAccountType.PAN, P2pAccountType.PAN);

        P2pResponse simpleBillResponse = p2pCheckRequestDecoratorService.p2pPay(request);

        assertResultToWaiting(simpleBillResponse);

        List<Bill> billList = billRepository.findAll();

        assertBillSize(billList, databaseSizeBeforeCreate, 3);

        assertP2pBill(billList, HumoSenderService.HUMO_DEBIT_CREATE, RequestType.P2P_DEBIT, true, TransactionStatus.SUCCESS.name());

        assertP2pBill(billList, HumoP2pHelperService.HUMO_CREDIT_RECIPIENT_NAME, RequestType.P2P_CREDIT, false, TransactionStatus.WAITING.name());

        List<Transaction> transactions = transactionRepository.findAll();

        assertTransactionIsNotNull(transactions);

        Transaction transaction = transactions.get(0);

        assertTransactionStatus(transaction, TransactionStatus.R_NEED_CONFIRM);

        assertTransactionSender(transaction, "4", true, SENDER_HUMO_REAL_PAN);

        assertTransactionRecipient(transaction, TransactionStatus.WAITING.name(), false, HumoGatewaySenderServiceSimulation.TIMEOUT_PAN);
    }

    @SneakyThrows
    @Test
    @Transactional
    @WithMockUser(DummyDataAdapterV3.DUMMY_AGENT)
    public void p2pHumoTokenToTokenSenderTimeOutTest() {
        int databaseSizeBeforeCreate = billRepository.findAll().size();

        P2pPayRequest request = getRequest(DummyHumoMiddlewareServiceImpl.TIMEOUT_MASKED_PAN, RECEIVER_HUMO_MASKED_PAN, P2pAccountType.TOKEN, P2pAccountType.TOKEN);

        P2pResponse simpleBillResponse = p2pCheckRequestDecoratorService.p2pPay(request);

        assertResultToWaiting(simpleBillResponse);

        List<Bill> billList = billRepository.findAll();

        assertBillSize(billList, databaseSizeBeforeCreate, 2);

        assertP2pBill(billList, HumoSenderService.HUMO_DEBIT_CREATE, RequestType.P2P_DEBIT, false, TransactionStatus.WAITING.name());

        List<Transaction> transactions = transactionRepository.findAll();

        assertTransactionIsNotNull(transactions);

        Transaction transaction = transactions.get(0);

        assertTransactionStatus(transaction, TransactionStatus.S_NEED_CONFIRM);

        assertTransactionSender(transaction, TransactionStatus.WAITING.name(), false, DummyHumoMiddlewareServiceImpl.TIMEOUT_MASKED_PAN);

        assertTransactionRecipient(transaction, null, null, RECEIVER_HUMO_MASKED_PAN);
    }

    @SneakyThrows
    @Test
    @Transactional
    @WithMockUser(DummyDataAdapterV3.DUMMY_AGENT)
    public void p2pHumoTokenToTokenReceiverTimeOutTest() {
        int databaseSizeBeforeCreate = billRepository.findAll().size();

        P2pPayRequest request = getRequest(SENDER_HUMO_MASKED_PAN, DummyHumoMiddlewareServiceImpl.TIMEOUT_MASKED_PAN, P2pAccountType.TOKEN, P2pAccountType.TOKEN);

        P2pResponse simpleBillResponse = p2pCheckRequestDecoratorService.p2pPay(request);

        assertResultToWaiting(simpleBillResponse);

        List<Bill> billList = billRepository.findAll();

        assertBillSize(billList, databaseSizeBeforeCreate, 3);

        assertP2pBill(billList, HumoSenderService.HUMO_DEBIT_CREATE, RequestType.P2P_DEBIT, true, TransactionStatus.SUCCESS.name());

        assertP2pBill(billList, HumoP2pHelperService.HUMO_CREDIT_RECIPIENT_NAME, RequestType.P2P_CREDIT, false, TransactionStatus.WAITING.name());

        List<Transaction> transactions = transactionRepository.findAll();

        assertTransactionIsNotNull(transactions);

        Transaction transaction = transactions.get(0);

        assertTransactionStatus(transaction, TransactionStatus.R_NEED_CONFIRM);

        assertTransactionSender(transaction, "4", true, SENDER_HUMO_MASKED_PAN);

        assertTransactionRecipient(transaction, TransactionStatus.WAITING.name(), false, DummyHumoMiddlewareServiceImpl.TIMEOUT_MASKED_PAN);
    }

    @SneakyThrows
    @Test()
    @Transactional
    @WithMockUser(DummyDataAdapterV3.DUMMY_AGENT)
    public void agentHumoTokenSenderTimeOutTest() {
        int databaseSizeBeforeCreate = billRepository.findAll().size();

        check();

        CommonPayRequest payRequest = new PayRequest();
        payRequest.setBillId(checkId);
        payRequest.setType(PayType.PC);

        Map<String, Object> params = new HashMap<>();
        params.put(Constants.PAN_PARAM, DummyHumoMiddlewareServiceImpl.TIMEOUT_MASKED_PAN);
        params.put(Constants.EXPIRY_PARAM, HUMO_EXP_DATE);
        payRequest.setParams(params);

        try {
            SimpleBillResponse payResult = commonTranServiceV3.pay(payRequest);
        } catch (BmTransactionException e) {
            assertThat(e.getTransaction()).isNotNull();
        }

        List<Bill> billList = billRepository.findAll();

        assertBillSize(billList, databaseSizeBeforeCreate, 2);

        assertP2pBill(billList, HumoSenderService.HUMO_DEBIT_CREATE, RequestType.PAY, false, TransactionStatus.WAITING.name());

        List<Transaction> transactions = transactionRepository.findAll();

        assertTransactionIsNotNull(transactions);

        Transaction transaction = transactions.get(0);

        assertTransactionStatus(transaction, TransactionStatus.S_NEED_CONFIRM);

        assertTransactionSender(transaction, TransactionStatus.WAITING.name(), false, DummyHumoMiddlewareServiceImpl.TIMEOUT_REAL_PAN);

        assertTransactionRecipient(transaction, "true", true, GATEWAY_ACCOUNT);
    }

    @Test
    @WithMockUser(DUMMY_AGENT)
    @Transactional
    public void check() {

        DummyUtils.prepareDummyData(dummyAdapterDelegate);

        Optional<Recipient> optionalRecipient = recipientRepository.findTop1ByShortName(DummyDataAdapterV3.DUMMY_RECIPIENT_USD);

        Assume.assumeTrue(optionalRecipient.isPresent());

        CommonCheckRequest simpleCheckDTO = new CheckRequest();
        simpleCheckDTO.setAmount(1000000L);
        simpleCheckDTO.setAccount(GATEWAY_ACCOUNT);
        simpleCheckDTO.setRecipientId(optionalRecipient.get().getId());

        SimpleBillResponse check = commonTranServiceV3.check(simpleCheckDTO);

        assertThat(check).isNotNull();
        assertThat(check.getSuccess()).isTrue();
        assertThat(check.getStatus()).isEqualTo(TransactionStatus.CHECK_OK);

        checkId = check.getId();
    }

    private void assertP2pBill(List<Bill> billList, String recipientName, RequestType requestType, boolean isSuccess, String responseText) {
        Optional<Bill> optionalBill = billList.stream().filter(bill -> Objects.equals(bill.getRecipientShortName(), recipientName)).findAny();
        assertThat(optionalBill.isPresent()).isTrue();

        Bill bill = optionalBill.get();
        assertThat(bill.getSuccess()).isEqualTo(isSuccess);
        assertThat(bill.getType()).isEqualTo(requestType);
        assertThat(bill.getResponseText()).isEqualToIgnoringCase(responseText);
    }

    private P2pPayRequest getRequest(String senderPan, String recipientPan, P2pAccountType senderType, P2pAccountType recipientType) throws JsonProcessingException {
        String paramsString = getPanParam(senderPan, senderType);
        var senderAccount = new P2pAccount().withType(senderType).withCurrency(860).withParams(jacksonMessageConverter.getObjectMapper().readTree(paramsString));

        String paramsString2 = getPanParam(recipientPan, senderType);
        var recipientAccount = new P2pAccount().withType(recipientType).withCurrency(860).withParams(jacksonMessageConverter.getObjectMapper().readTree(paramsString2));

        return new P2pPayRequest()
                .withFrom(senderAccount)
                .withTo(recipientAccount)
                .withAgentTranId("123")
                .withAmount(10000L);
    }

    private String getPanParam(String account, P2pAccountType type) {
        if (Objects.equals(type, P2pAccountType.PAN)) {
            return "{ \"pan\": \"" + account + "\" }";
        }
        if (Objects.equals(type, P2pAccountType.TOKEN)) {
            return "{ \"token\": \"" + account + "\", \"bin\": \"" + account.substring(0, 6) + "\" }";
        }
        return "";
    }

    private void assertTransactionIsNotNull(List<Transaction> transactions) {
        assertThat(transactions).isNotNull();
        assertThat(transactions.size()).isEqualTo(1);
        assertThat(transactions.get(0)).isNotNull();

    }

    private void assertTransactionRecipient(Transaction transaction, String recipientStatus, Boolean recipientIsSuccess, String recipientAccount) {
        assertThat(transaction.getRecipientStatus()).isEqualTo(recipientStatus);
        assertThat(transaction.getRecipientSuccess()).isEqualTo(recipientIsSuccess);
        assertThat(transaction.getRecipientAccount()).isEqualTo(recipientAccount);
    }

    private void assertTransactionStatus(Transaction transaction, TransactionStatus transactionStatus) {
        assertThat(transaction.getStatus()).isEqualTo(transactionStatus);
    }

    private void assertTransactionSender(Transaction transaction, String senderStatus, Boolean senderIsSuccess, String senderAccount) {
        assertThat(transaction.getSenderStatus()).isEqualTo(senderStatus);
        assertThat(transaction.getSenderSuccess()).isEqualTo(senderIsSuccess);
        assertThat(transaction.getSenderAccount()).isEqualTo(senderAccount);
    }

    private void assertBillSize(List<Bill> billList, int databaseSizeBeforeCreate, int count) {
        assertThat(billList).hasSize(databaseSizeBeforeCreate + count); // CHECK (getInfo) -> UNIVERSAL
    }

    private void assertResultToWaiting(P2pResponse simpleBillResponse) {
        assertThat(simpleBillResponse).isNotNull();
        assertThat(simpleBillResponse.isSuccess()).isTrue();
        assertThat(simpleBillResponse.getStatusCode()).isEqualTo(BmResultStatus.WAITING.getCode());
        assertThat(simpleBillResponse.getStatusMessage()).containsIgnoringCase(BmResultStatus.WAITING.name());
    }
}
