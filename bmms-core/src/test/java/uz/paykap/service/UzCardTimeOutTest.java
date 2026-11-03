package uz.paykap.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import uz.devops.humoMiddleware.service.impl.DummyHumoMiddlewareServiceImpl;
import uz.javlon.svgate.dto.TranDTO;
import uz.javlon.svgate.service.simulation.PredefinedCardRepo2;
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
import uz.paykap.service.p2p.dto.response.P2pResponse;
import uz.paykap.web.rest.errors.BmTransactionException;
import uz.paykap.web.rest.util.DummyUtils;

import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static uz.devops.bm.dummy.adapter.DummyDataAdapterV3.DUMMY_AGENT;

@Ignore
@Slf4j
@RunWith(SpringRunner.class)
@IntegrationTest
public class UzCardTimeOutTest {

    public static final String EXP_DATE = "0327";
    public static final String GATEWAY_ACCOUNT = "933999959";
    public static final String UZCARD_DEBIT = "uzcard-debit";

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
    @Autowired
    private ObjectMapper objectMapper;

    private static long checkId;

    @SneakyThrows
    @Test
    @Transactional
    @WithMockUser(DummyDataAdapterV3.DUMMY_AGENT)
    public void p2pUzCardPanToPanSenderTimeOutTest() {
        int databaseSizeBeforeCreate = billRepository.findAll().size();

        P2pPayRequest request = getRequest(PredefinedCardRepo2.REAL_PAN_ANOR_TIMEOUT, PredefinedCardRepo2.REAL_PAN, P2pAccountType.PAN, P2pAccountType.PAN);

        P2pResponse simpleBillResponse = p2pCheckRequestDecoratorService.p2pPay(request);

        assertResultToWaiting(simpleBillResponse);

        List<Bill> billList = billRepository.findAll();

        assertBillSize(billList, databaseSizeBeforeCreate, 2);

        assertP2pBill(billList, "UNIVERSAL", RequestType.P2P_CREDIT, false, TransactionStatus.WAITING.name());

        List<Transaction> transactions = transactionRepository.findAll();

        assertTransactionIsNotNull(transactions);

        Transaction transaction = transactions.get(0);

        assertTransactionStatus(transaction, TransactionStatus.R_NEED_CONFIRM);

        assertTransactionSender(transaction, String.valueOf(BmResultStatus.WAITING.getCode()), false, PredefinedCardRepo2.REAL_PAN_ANOR_TIMEOUT);

        assertTransactionRecipient(transaction, TransactionStatus.WAITING.name(), false, PredefinedCardRepo2.REAL_PAN);
    }

    @SneakyThrows
    @Test
    @Transactional
    @WithMockUser(DummyDataAdapterV3.DUMMY_AGENT)
    public void p2pUzCardPanToPanReceiverTimeOutTest() {
        int databaseSizeBeforeCreate = billRepository.findAll().size();

        P2pPayRequest request = getRequest(PredefinedCardRepo2.REAL_PAN, PredefinedCardRepo2.REAL_PAN_ANOR_TIMEOUT, P2pAccountType.PAN, P2pAccountType.PAN);

        P2pResponse simpleBillResponse = p2pCheckRequestDecoratorService.p2pPay(request);

        assertResultToWaiting(simpleBillResponse);

        List<Bill> billList = billRepository.findAll();

        assertBillSize(billList, databaseSizeBeforeCreate, 2);

        assertP2pBill(billList, "UNIVERSAL", RequestType.P2P_CREDIT, false, TransactionStatus.WAITING.name());

        List<Transaction> transactions = transactionRepository.findAll();

        assertTransactionIsNotNull(transactions);

        Transaction transaction = transactions.get(0);

        assertTransactionStatus(transaction, TransactionStatus.R_NEED_CONFIRM);

        assertTransactionSender(transaction, String.valueOf(BmResultStatus.WAITING.getCode()), false, PredefinedCardRepo2.REAL_PAN);

        assertTransactionRecipient(transaction, TransactionStatus.WAITING.name(), false, PredefinedCardRepo2.REAL_PAN_ANOR_TIMEOUT);
    }

    @SneakyThrows
    @Test()
    @Transactional
    @WithMockUser(DummyDataAdapterV3.DUMMY_AGENT)
    public void agentUzCardPanSenderTimeOutTest() {
        int databaseSizeBeforeCreate = billRepository.findAll().size();

        check();

        CommonPayRequest payRequest = new PayRequest();
        payRequest.setBillId(checkId);
        payRequest.setType(PayType.PC);
        Map<String, Object> params = new HashMap<>();
        params.put(Constants.PAN_PARAM, PredefinedCardRepo2.REAL_PAN_ANOR_TIMEOUT);
        params.put(Constants.EXPIRY_PARAM, EXP_DATE);
        payRequest.setParams(params);

        try {
            SimpleBillResponse payResult = commonTranServiceV3.pay(payRequest);
        } catch (BmTransactionException e) {
            assertThat(e.getTransaction()).isNotNull();
        }

        List<Bill> billList = billRepository.findAll();

        assertBillSize(billList, databaseSizeBeforeCreate, 2);

        assertP2pBill(billList, UZCARD_DEBIT, RequestType.PAY, false, TransactionStatus.WAITING.name());

        List<Transaction> transactions = transactionRepository.findAll();

        assertTransactionIsNotNull(transactions);

        Transaction transaction = transactions.get(0);

        assertTransactionStatus(transaction, TransactionStatus.S_NEED_CONFIRM);

        assertTransactionSender(transaction, TransactionStatus.WAITING.name(), false, PredefinedCardRepo2.REAL_PAN_ANOR_TIMEOUT);

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

    @SneakyThrows
    @org.junit.jupiter.api.Test
    void testSvgateTranDto() {
//        ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

        final TranDTO tranDTO = new TranDTO();
        tranDTO.setDate7(ZonedDateTime.now());

        final String tran = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(tranDTO);

        System.out.println(tran);
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

        String paramsString2 = getPanParam(recipientPan, recipientType);
        var recipientAccount = new P2pAccount().withType(recipientType).withCurrency(860).withParams(jacksonMessageConverter.getObjectMapper().readTree(paramsString2));

        return new P2pPayRequest()
                .withFrom(senderAccount)
                .withTo(recipientAccount)
                .withAgentTranId("123")
                .withAmount(10000L);
    }

    private String getPanParam(String account, P2pAccountType type) {
        if (Objects.equals(type, P2pAccountType.PAN)) {
            return "{ \"pan\": \"" + account + "\", \"exp\":\"0327\" }";
        }
        if (Objects.equals(type, P2pAccountType.TOKEN)) {
            return "{ \"token\": \"" + account + "\", \"bin\": \"" + account.substring(0, 6) + "\" }";
        }
        return "";
    }

    private void assertTransactionIsNotNull(List<Transaction> transactions) {
//        assertThat(transactions.stream().map(Transaction::toString).collect(Collectors.joining())).isEqualTo("");
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
        assertThat(simpleBillResponse.getStatusMessage().replace(":", "")).isEqualToIgnoringWhitespace(BmResultStatus.WAITING.name());
    }
}
