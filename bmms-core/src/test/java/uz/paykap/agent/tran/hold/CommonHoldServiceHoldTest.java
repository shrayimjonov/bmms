package uz.paykap.agent.tran.hold;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.http.converter.ResourceHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import uz.devops.bm.dummy.adapter.DummyDataAdapterV3;
import uz.devops.bmcore.dto.request.CheckRequest;
import uz.devops.bmcore.dto.request.CommonCheckRequest;
import uz.devops.bmcore.dto.request.CommonPayRequest;
import uz.devops.bmcore.dto.request.PayRequest;
import uz.devops.bmcore.dto.response.SimpleBillResponse;
import uz.devops.bmcore.enumeration.PayType;
import uz.devops.bmcore.enumeration.TransactionStatus;
import uz.devops.bmcore.util.SharedContentStoreService;
import uz.javlon.commons.exception.CodedException;
import uz.javlon.commons.exception.ProblemEnum;
import uz.javlon.svgate.service.simulation.PredefinedCardRepo2;
import uz.paykap.IntegrationTest;
import uz.paykap.adapter.DataAdapterServiceV3;
import uz.paykap.agent.tran.CachedCbuService;
import uz.paykap.agent.tran.CommonHoldTranService;
import uz.paykap.agent.tran.CommonTranServiceV3;
import uz.paykap.config.Constants;
import uz.paykap.domain.*;
import uz.paykap.domain.enumeration.Prop;
import uz.paykap.repository.*;
import uz.paykap.sender.impl.HumoSenderService;
import uz.paykap.service.AgentRecipientService;
import uz.paykap.service.DepositService;
import uz.paykap.service.GroupSettingService;
import uz.paykap.service.hold.PartialReverseRequest;
import uz.paykap.service.uzcard.UzcardHelperService;
import uz.paykap.web.rest.SharedAgentResourceV3;
import uz.paykap.web.rest.TestUtil;
import uz.paykap.web.rest.errors.ExceptionTranslator;
import uz.paykap.web.rest.util.DummyUtils;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uz.paykap.web.rest.TestUtil.createFormattingConversionService;

@RunWith(SpringRunner.class)
@IntegrationTest
@Transactional
public class CommonHoldServiceHoldTest {

    public static final long SENDER_AMOUNT = 1000000L;
    public static final int TIYIN = 100;
    public static final String TEST_USER = "TEST_USER";
    public static final String TEST_ACCOUNT = "TEST_ACCOUNT";
    public static final String TRAN_ID = "TRAN_ID";
    public static final int UZS = 860;
    public static final String HUMO_TEST_PAN = "9860600000000000";
    public static final String HUMO_TEXT_EXP_DATE = "0327";

    @Autowired
    protected TransactionRepository transactionRepository;
    @Autowired
    protected BillRepository billRepository;
    @Autowired
    protected MappingJackson2HttpMessageConverter jacksonMessageConverter;
    @Autowired
    protected PageableHandlerMethodArgumentResolver pageableArgumentResolver;
    @Autowired
    protected ExceptionTranslator exceptionTranslator;
    @Autowired
    protected CachedCbuService cbuService;
    @Autowired
    protected DepositService depositService;
    @Autowired
    protected DataAdapterServiceV3 dataAdapterServiceV3;
    @Autowired
    protected RecipientRepository recipientRepository;
    @Autowired
    protected CommonTranServiceV3 commonTranServiceV3;
    @Autowired
    protected SharedContentStoreService sharedContentStoreService;
    @Autowired
    protected GroupSettingService groupSettingService;
    @Autowired
    protected AgentRecipientService agentRecipientService;
    @Autowired
    private SettingRepository settingRepository;
    @Autowired
    private SettingPropertyRepository settingPropertyRepository;
    @Autowired
    private CommonHoldServiceHoldHelper holdHelper;
    @Autowired
    private CommonHoldTranService commonHoldTranService;

    private MockMvc restAgentMockMvc;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        final SharedAgentResourceV3 adapterResource = new SharedAgentResourceV3(commonTranServiceV3, sharedContentStoreService);
        this.restAgentMockMvc = MockMvcBuilders.standaloneSetup(adapterResource)
                .setCustomArgumentResolvers(pageableArgumentResolver)
                .setControllerAdvice(exceptionTranslator)
                .setConversionService(createFormattingConversionService())
                .setMessageConverters(jacksonMessageConverter, new ResourceHttpMessageConverter()).build();
    }

    @Test
    @Transactional
    @WithMockUser(username = DummyDataAdapterV3.DUMMY_AGENT)
    public void testCheck() throws Exception {

        int databaseSizeBeforeCreate = transactionRepository.findAll().size();

        Instant instant = Instant.now();

        CheckRequest simpleCheckDTO = getSimpleCheckDTO();

        SimpleBillResponse simpleBillDTO = getSimpleBillDTO(simpleCheckDTO);

        assertThat(simpleBillDTO).isNotNull();
        assertThat(simpleBillDTO.isSuccess()).isTrue();
        assertThat(simpleBillDTO.getAmount()).isEqualTo(simpleCheckDTO.getAmount());
        assertThat(simpleBillDTO.getAccount()).isEqualTo(simpleCheckDTO.getAccount());

        List<Transaction> transactionList = transactionRepository.findAll();
        assertThat(transactionList).hasSize(databaseSizeBeforeCreate + 1);
        Transaction testTransaction = transactionList.get(transactionList.size() - 1);

        assertThat(simpleBillDTO.getId()).isEqualTo(testTransaction.getId());

    }

    @Test
    @Transactional
    @WithMockUser(DummyDataAdapterV3.DUMMY_AGENT)
    public void testUzCardHoldToSuccess() throws Exception {
        testCheck();
        CommonPayRequest payRequest = createPayRequest(PredefinedCardRepo2.REAL_PAN, PredefinedCardRepo2.DEFAULT_EXPIRY);
        SimpleBillResponse holdResponse = holdHelper.holdForAgent(payRequest);

        assertThat(holdResponse).isNotNull();
        assertThat(holdResponse.getId()).isEqualTo(payRequest.getBillId());
        assertThat(holdResponse.getStatus()).isEqualTo(TransactionStatus.HOLD_SUCCESS);

        verifyDataBase(holdResponse.getId(), UzcardHelperService.UZ_CARD_HOLD_CREATE,true);
    }

    @Test
    @Transactional
    @WithMockUser(DummyDataAdapterV3.DUMMY_AGENT)
    public void testUzCardHold30DownToSuccess() throws Exception {
        testCheck();
        CommonPayRequest payRequest = createPayRequest(PredefinedCardRepo2.REAL_PAN, PredefinedCardRepo2.DEFAULT_EXPIRY);
        SimpleBillResponse holdResponse = holdHelper.holdForAgent(payRequest);

        payRequest.setAmount(900000L);
        SimpleBillResponse reHoldResponse = holdHelper.reHold(payRequest);
        assertThat(holdResponse).isNotNull();
        assertThat(holdResponse.getId()).isEqualTo(payRequest.getBillId());
        assertThat(holdResponse.getStatus()).isEqualTo(TransactionStatus.HOLD_SUCCESS);

        assertThat(reHoldResponse).isNotNull();
        assertThat(reHoldResponse.getId()).isEqualTo(payRequest.getBillId());
        assertThat(reHoldResponse.getStatus()).isEqualTo(TransactionStatus.HOLD_SUCCESS);

        verifyDataBase(holdResponse.getId(), UzcardHelperService.UZ_CARD_HOLD_CREATE, true);
    }

    @Test
    @Transactional
    @WithMockUser(DummyDataAdapterV3.DUMMY_AGENT)
    public void testUzCardHold30UpToSuccess() throws Exception {
        testCheck();
        CommonPayRequest payRequest = createPayRequest(PredefinedCardRepo2.REAL_PAN, PredefinedCardRepo2.DEFAULT_EXPIRY);
        SimpleBillResponse holdResponse = holdHelper.holdForAgent(payRequest);

        payRequest.setAmount(1300000L);
        SimpleBillResponse reHoldResponse = holdHelper.reHold(payRequest);
        assertThat(holdResponse).isNotNull();
        assertThat(holdResponse.getId()).isEqualTo(payRequest.getBillId());
        assertThat(holdResponse.getStatus()).isEqualTo(TransactionStatus.HOLD_SUCCESS);

        assertThat(reHoldResponse).isNotNull();
        assertThat(reHoldResponse.getId()).isEqualTo(payRequest.getBillId());
        assertThat(reHoldResponse.getStatus()).isEqualTo(TransactionStatus.HOLD_SUCCESS);

        verifyDataBase(holdResponse.getId(), UzcardHelperService.UZ_CARD_HOLD_CREATE, true);
    }

    @Test
    @Transactional
    @WithMockUser(DummyDataAdapterV3.DUMMY_AGENT)
    public void testHumoHoldToSuccess() throws Exception {
        testCheck();
        CommonPayRequest payRequest = createPayRequest(HUMO_TEST_PAN, HUMO_TEXT_EXP_DATE);
        SimpleBillResponse holdResponse = holdHelper.holdForAgent(payRequest);

        assertThat(holdResponse).isNotNull();
        assertThat(holdResponse.getId()).isEqualTo(payRequest.getBillId());
        assertThat(holdResponse.getStatus()).isEqualTo(TransactionStatus.HOLD_SUCCESS);

        verifyDataBase(holdResponse.getId(), HumoSenderService.HUMO_HOLD_CREATE,true);
    }

    @Test
    @Transactional
    @WithMockUser(DummyDataAdapterV3.DUMMY_AGENT)
    public void testUzCardToCheckNotFound() throws Exception {
        testCheck();
        CommonPayRequest payRequest = createPayRequest(PredefinedCardRepo2.REAL_PAN, PredefinedCardRepo2.DEFAULT_EXPIRY);
        payRequest.setBillId(-1L);
        CodedException codedException = assertThrows(CodedException.class, () -> {
            holdHelper.holdForAgent(payRequest);
        });
        assertThat(codedException.getDetail()).isEqualTo(ProblemEnum.BILL_NOT_FOUND.getDetail());
    }

    @Test
    @Transactional
    public void testUzCardToUsernameNotFound() throws Exception {
        saveCheckOkTransaction();
        CommonPayRequest payRequest = createPayRequest(PredefinedCardRepo2.REAL_PAN, PredefinedCardRepo2.DEFAULT_EXPIRY);
        CodedException codedException = assertThrows(CodedException.class, () -> {
            holdHelper.holdForAgent(payRequest);
        });
        assertThat(codedException.getDetail()).isEqualTo(ProblemEnum.USERNAME_NOT_FOUND.getDetail());
    }

    @Test
    @Transactional
    @WithMockUser(DummyDataAdapterV3.DUMMY_AGENT)
    public void withoutSenderParamsNeedReturnErrorInvalidParams() {
        saveCheckOkTransaction();
        CommonPayRequest payRequest = createPayRequest(PredefinedCardRepo2.REAL_PAN, PredefinedCardRepo2.DEFAULT_EXPIRY);
        payRequest.setParams(new HashMap<>());
        SimpleBillResponse holdResponse = holdHelper.holdForAgent(payRequest);

        assertThat(holdResponse).isNotNull();
        assertThat(holdResponse.getStatus()).isEqualTo(TransactionStatus.HOLD_ERROR);
        assertThat(holdResponse.getResponseText()).isEqualTo("token and pan not found");
    }

    @Test
    @Ignore
    @Transactional
    @WithMockUser(DummyDataAdapterV3.DUMMY_AGENT)
    public void withoutAmountNeedReturnError() {
        prepareDummyData();
        Transaction transaction = saveCheckOkTransaction();
        transaction.setSenderAmount(null);
        transaction.setRequestAmount(null);
        transactionRepository.save(transaction);

        CommonPayRequest payRequest = createPayRequest(PredefinedCardRepo2.REAL_PAN, PredefinedCardRepo2.DEFAULT_EXPIRY);
        payRequest.setAmount(null);

        SimpleBillResponse holdResponse = holdHelper.holdForAgent(payRequest);

        assertThat(holdResponse).isNotNull();
        assertThat(holdResponse.getStatus()).isEqualTo(TransactionStatus.HOLD_ERROR);
        assertThat(holdResponse.getResponseText()).isEqualTo(ProblemEnum.INVALID_PARAMS.getDetail() + " . Amount not given, OK");
    }

    @Test
    @Transactional
    @WithMockUser(DummyDataAdapterV3.DUMMY_AGENT)
    public void testPartialReverseHoldToSuccess() throws Exception {
        testUzCardHoldToSuccess();
        List<Transaction> holdTransactions = transactionRepository.findAll();
        assertThat(holdTransactions).isNotNull();
        assertThat(holdTransactions.size()).isEqualTo(1);
        assertThat(holdTransactions.get(0)).isNotNull();

        Transaction holdTransaction = holdTransactions.get(0);
        assertThat(holdTransaction.getStatus()).isEqualTo(TransactionStatus.HOLD_SUCCESS);

        Long holdPartialReverseAmount = 200L;
        SimpleBillResponse holdPartialReverseResult = commonHoldTranService.partialReverse(new PartialReverseRequest(holdTransaction.getId(), holdPartialReverseAmount));
        assertThat(holdPartialReverseResult).isNotNull();
        assertThat(holdPartialReverseResult.getStatus()).isEqualTo(TransactionStatus.HOLD_SUCCESS);
        assertThat(holdPartialReverseResult.getAmount()).isEqualTo(holdTransaction.getRequestAmount() - holdPartialReverseAmount);
    }

    private Transaction saveCheckOkTransaction() {
        Transaction transaction = new Transaction()
                .requestAmount(1000000L)
                .requestCcy(860)
                .requestTrId("TRAN_ID")
                .requestLogin("dummy-agent")
                .requestGroup("dummy-agent")
                .senderName("dummy-agent")
                .senderAccount("dummy-agent")
                .senderServiceId(0L)
                .senderAmount(1000000L)
                .senderCcy(null)
                .senderTrId("TRAN_ID")
                .senderSuccess(null)
                .senderDate(null)
                .senderStatus(null)
                .senderStatusText(null)
                .senderBillId(0L)
                .senderGateway("dummy-agent")
                .gatewayAmount(1.19)
                .recipientName("dummy-recipient-usd")
                .recipientAccount("TEST_ACCOUNT")
                .recipientServiceId(100000351L)
                .recipientAmount(930000L)
                .recipientCcy(840)
                .recipientTrId("EXT_CHECK_ID")
                .recipientSuccess(true)
                .recipientDate(Instant.now())
                .recipientStatus("true")
                .recipientStatusText("OK")
                .recipientBillId(100000701L)
                .recipientGateway("dummy-gateway")
                .systemCommissionUpAmount(0L)
                .systemCommissionDownAmount(70000L)
                .systemFeeAmount(18600L)
                .agentFeeAmount(50000L)
                .roundAmount(0.0)
                .systemFee(2.0)
                .systemCurrency(860)
                .ccyRate(778305)
                .status(TransactionStatus.CHECK_OK)
                .comment("")
                .date(Instant.now())
                .payType(PayType.UNKNOWN)
                .requestSubAccount("dummy-agent")
                .eposMerchant("111111")
                .eposTerminal("222222")
                .eposTerminal("222222")
                .main(true)
                .amountRatio(1)
                .senderBean(null)
                .category(null)
                .promo(null);

        return transactionRepository.save(transaction);
    }

    private void verifyDataBase(Long id, String recipientShortName, boolean billSuccess) {
        Optional<Transaction> dbTransaction = transactionRepository.findById(id);
        assertThat(dbTransaction.isPresent()).isTrue();
        Transaction transaction = dbTransaction.get();
        assertThat(transaction.getStatus()).isEqualTo(TransactionStatus.HOLD_SUCCESS);
        assertThat(transaction.getSenderAmount()).isEqualTo(SENDER_AMOUNT);
        assertThat(transaction.getSenderAccount()).isNotNull();
        assertThat(transaction.getSenderTrId()).isNotEmpty();

        Optional<Bill> holdBill = billRepository.findTop1ByRecipientShortNameAndSuccess(recipientShortName, billSuccess);
        assertThat(holdBill.isPresent()).isTrue();
        assertThat(holdBill.get().getExtId()).isNotEmpty();
        assertThat(holdBill.get().getSuccess()).isTrue();
    }

    private CommonPayRequest createPayRequest(String pan, String expDate) {
        Long checkTranId = getCheckTranId();
        CommonPayRequest payRequest = new PayRequest();
        payRequest.setBillId(checkTranId);
        payRequest.setType(PayType.PC);
        Map<String, Object> params = getParams(pan, expDate);
        payRequest.setParams(params);
        return payRequest;
    }

    private Map<String, Object> getParams(String pan, String expDate) {
        Map<String, Object> params = new HashMap<>();
        params.put(Constants.PAN_PARAM, pan);
        params.put(Constants.EXPIRY_PARAM, expDate);
        return params;
    }

    private Long getCheckTranId() {
        Optional<Transaction> checkTran = transactionRepository.findTop1ByRequestLoginAndRequestTrId(DummyDataAdapterV3.DUMMY_AGENT, TRAN_ID);
        assertThat(checkTran.isPresent()).isTrue();
        Transaction transaction = checkTran.get();
        assertThat(transaction.getStatus()).isEqualTo(TransactionStatus.CHECK_OK);
        return transaction.getId();
    }

    protected CheckRequest getSimpleCheckDTO() {
        CheckRequest commonCheckDTO = new CheckRequest();
        commonCheckDTO.setAccount(TEST_ACCOUNT);
        commonCheckDTO.setAmount(SENDER_AMOUNT);
        commonCheckDTO.setCurrency(UZS);
        commonCheckDTO.setAgentTranId(TRAN_ID);
        return commonCheckDTO;
    }


    protected SimpleBillResponse getSimpleBillDTO(CommonCheckRequest commonCheckDTO) throws Exception {
//        dummyDataAdapterV3.sync(DummyDataAdapterV3.NAME);
        prepareDummyData();

        depositService.increaseAgentDeposit(DummyDataAdapterV3.DUMMY_AGENT, 10000000L);

        Optional<Recipient> recipient = recipientRepository.findTop1ByShortName(DummyDataAdapterV3.DUMMY_RECIPIENT_USD);

        assertThat(recipient.isPresent()).isTrue();
        Long recipientId = recipient.get().getId();

        commonCheckDTO = commonCheckDTO == null ? getSimpleCheckDTO() : commonCheckDTO;

        commonCheckDTO.setRecipientId(recipientId);

        MvcResult mvcResult = restAgentMockMvc.perform(
                        post("/api/agent/check/v3")
                                .content(TestUtil.convertObjectToJsonBytes(commonCheckDTO))
                                .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
                )
                .andExpect(status().isOk())
                .andReturn();

        assertThat(mvcResult.getResponse().getContentAsString()).isNotEmpty();

        return jacksonMessageConverter.getObjectMapper().readValue(mvcResult.getResponse().getContentAsByteArray(), SimpleBillResponse.class);
    }

    public void prepareDummyData() {

        Setting settingDTO = new Setting();
        settingDTO.setActive(true);
        settingDTO.setOrd(1);
        settingDTO.setShortName("UZCARD_SETTINGS");

        Map<Prop, SettingProperty> eposSetting = new HashMap<>();
        SettingProperty uzCardMerchantProperty = new SettingProperty();
        uzCardMerchantProperty.setKey(Prop.UZCARD_MERCHANT);
        uzCardMerchantProperty.setValue("12345");
        SettingProperty uzCardTerminalProperty = new SettingProperty();
        uzCardTerminalProperty.setKey(Prop.UZCARD_TERMINAL);
        uzCardTerminalProperty.setValue("54321");

        uzCardMerchantProperty = settingPropertyRepository.save(uzCardMerchantProperty);
        uzCardTerminalProperty = settingPropertyRepository.save(uzCardTerminalProperty);

        eposSetting.put(Prop.UZCARD_MERCHANT, uzCardMerchantProperty);
        eposSetting.put(Prop.UZCARD_TERMINAL, uzCardTerminalProperty);
        settingDTO.setProps(eposSetting);
        settingDTO = settingRepository.save(settingDTO);
        DummyUtils.prepareDummyData(dataAdapterServiceV3, groupSettingService, recipientRepository, agentRecipientService, settingDTO.getId());
    }
}
