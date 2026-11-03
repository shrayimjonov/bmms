package uz.paykap.web.rest;

import org.junit.Before;
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
import uz.devops.bm.dummy.gateway.DummyGatewayServiceV3;
import uz.devops.bmcore.dto.request.CheckRequest;
import uz.devops.bmcore.dto.request.CommonCheckRequest;
import uz.devops.bmcore.dto.request.PayRequest;
import uz.devops.bmcore.dto.response.SimpleBillResponse;
import uz.devops.bmcore.enumeration.PayType;
import uz.devops.bmcore.enumeration.TransactionStatus;
import uz.paykap.IntegrationTest;
import uz.paykap.adapter.DataAdapterServiceV3;
import uz.paykap.agent.tran.CachedCbuService;
import uz.paykap.agent.tran.CommonTranServiceV3;
import uz.paykap.agent.tran.confirm.DebitConfirmService;
import uz.paykap.config.Constants;
import uz.paykap.domain.Recipient;
import uz.paykap.domain.Transaction;
import uz.paykap.gateway.humo.HumoUtils;
import uz.paykap.repository.RecipientRepository;
import uz.paykap.repository.TransactionRepository;
import uz.paykap.service.AgentRecipientService;
import uz.paykap.service.DepositService;
import uz.paykap.service.GroupSettingService;
import uz.paykap.service.dto.HoldPayRequest;
import uz.paykap.web.rest.errors.ExceptionTranslator;
import uz.paykap.web.rest.util.DummyUtils;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uz.paykap.web.rest.TestUtil.createFormattingConversionService;


@RunWith(SpringRunner.class)
@IntegrationTest
public class SharedAgentUnregisteredPayResourceTest {

    public static final String HUMO_HOLD_PAN = "9860600000000000";
    public static final String HUMO_HOLD_EXP = "2501";
    public static final String HUMO = "HUMO";
    public static final String DUMMY_ACCOUNT = "974415655";
    public static final String TRAN_ID = "TRAN_ID";
    public static final long DUMMY_AMOUNT = 1000000L;
    public static final String TEST_OTP_CODE = "123321";
    public static final String HUMO_DEBIT_CONFIRM = "humo-debit-confirm";
    public static final String HUMO_SUCCESS_STATUS_CODE = "000";

    @Autowired
    private CachedCbuService cbuService;
    @Autowired
    private TransactionRepository transactionRepository;
    @Autowired
    private CommonTranServiceV3 commonTranServiceV3;
    @Autowired
    private MappingJackson2HttpMessageConverter jacksonMessageConverter;
    @Autowired
    private PageableHandlerMethodArgumentResolver pageableArgumentResolver;
    @Autowired
    private ExceptionTranslator exceptionTranslator;
    @Autowired
    private DataAdapterServiceV3 dataAdapterServiceV3;
    @Autowired
    private RecipientRepository recipientRepository;
    @Autowired
    private AgentRecipientService agentRecipientService;
    @Autowired
    private GroupSettingService groupSettingService;
    @Autowired
    private DepositService depositService;

    private MockMvc restAgentMockMvc;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        final SharedAgentUnregisteredPayResource adapterResource = new SharedAgentUnregisteredPayResource(commonTranServiceV3);
        this.restAgentMockMvc = MockMvcBuilders.standaloneSetup(adapterResource)
                .setCustomArgumentResolvers(pageableArgumentResolver)
                .setControllerAdvice(exceptionTranslator)
                .setConversionService(createFormattingConversionService())
                .setMessageConverters(jacksonMessageConverter, new ResourceHttpMessageConverter()).build();
    }
    private CheckRequest getSimpleCheckDTO(Long dummyAmount) {
        CheckRequest commonCheckDTO = new CheckRequest();

        commonCheckDTO.setAccount(DUMMY_ACCOUNT);
        commonCheckDTO.setAmount(dummyAmount);
        commonCheckDTO.setCurrency(860);
        commonCheckDTO.setAgentTranId(TRAN_ID);

        return commonCheckDTO;
    }
    private SimpleBillResponse getSimpleBillDTO(CommonCheckRequest commonCheckDTO) throws Exception {
//        dummyDataAdapterV3.sync(DummyDataAdapterV3.NAME);
        prepareDummyData();

        depositService.increaseAgentDeposit(DummyDataAdapterV3.DUMMY_AGENT, 10000000L);

        Optional<Recipient> recipient = recipientRepository.findTop1ByShortName(DummyDataAdapterV3.DUMMY_RECIPIENT_USD);

        assertThat(recipient.isPresent()).isTrue();
        Long recipientId = recipient.get().getId();

        commonCheckDTO = commonCheckDTO == null ? getSimpleCheckDTO(DUMMY_AMOUNT) : commonCheckDTO;
        commonCheckDTO.setRecipientId(recipientId);
        Map<String, Object> params = new HashMap<>();
        params.put(Constants.PAN_PARAM, HUMO_HOLD_PAN);
        params.put(Constants.EXPIRY_PARAM, HUMO_HOLD_EXP);
        commonCheckDTO.setParams(params);

        MvcResult mvcResult = restAgentMockMvc.perform(
                        post("/api/agent/unregistered-check")
                                .content(TestUtil.convertObjectToJsonBytes(commonCheckDTO))
                                .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
                )
                .andExpect(status().isOk())
                .andReturn();

        assertThat(mvcResult.getResponse().getContentAsString()).isNotEmpty();

        return jacksonMessageConverter.getObjectMapper().readValue(mvcResult.getResponse().getContentAsByteArray(), SimpleBillResponse.class);
    }
    public void prepareDummyData() {
        DummyUtils.prepareDummyData(dataAdapterServiceV3, groupSettingService, recipientRepository, agentRecipientService);
    }

    @Test
    @Transactional
    @WithMockUser(username = DummyDataAdapterV3.DUMMY_AGENT)
    public void testHoldPaymentToSuccess() throws Exception {
        int databaseSizeBeforeCreate = transactionRepository.findAll().size();

        Instant instant = Instant.now();

        CheckRequest simpleCheckDTO = getSimpleCheckDTO(DUMMY_AMOUNT);

        SimpleBillResponse simpleBillDTO = getSimpleBillDTO(simpleCheckDTO);

        assertThat(simpleBillDTO).isNotNull();
        assertThat(simpleBillDTO.isSuccess()).isTrue();
        assertThat(simpleBillDTO.getAmount()).isEqualTo(simpleCheckDTO.getAmount());
        assertThat(simpleBillDTO.getAccount()).isEqualTo(simpleCheckDTO.getAccount());

        HoldPayRequest payDTO = new HoldPayRequest();
//        payDTO.setAmount(DUMMY_AMOUNT);
        payDTO.setGatewayAmount(30000.03);
        payDTO.setBillId(simpleBillDTO.getId());
        payDTO.setIsOnlyHold(true);
        payDTO.setType(PayType.PC);
        Map<String, Object> params = new HashMap<>();
        params.put(Constants.PAN_PARAM, HUMO_HOLD_PAN);
        params.put(Constants.EXPIRY_PARAM, HUMO_HOLD_EXP);
        params.put(Constants.OTP_PARAM, TEST_OTP_CODE);
        payDTO.setParams(params);

        MvcResult mvcResult = restAgentMockMvc.perform(
                        post("/api/agent/hold")
                                .content(TestUtil.convertObjectToJsonBytes(payDTO))
                                .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
                )
                .andExpect(status().isOk())
                .andReturn();

        simpleBillDTO = jacksonMessageConverter.getObjectMapper().readValue(mvcResult.getResponse().getContentAsByteArray(), SimpleBillResponse.class);

        assertThat(simpleBillDTO).isNotNull();
        assertThat(simpleBillDTO.isSuccess()).isTrue();
        assertThat(simpleBillDTO.getAmount()).isEqualTo(DUMMY_AMOUNT);
        assertThat(simpleBillDTO.getAccount()).isEqualTo(DUMMY_ACCOUNT);

        List<Transaction> transactionList = transactionRepository.findAll();
        assertThat(transactionList).hasSize(databaseSizeBeforeCreate + 1);
        Transaction testTransaction = transactionList.get(transactionList.size() - 1);

        testPayTransaction(testTransaction, instant, simpleCheckDTO, DummyDataAdapterV3.DUMMY_AGENT, TransactionStatus.SUCCESS, DebitConfirmService.HOLD_SUCCESS, DebitConfirmService.HOLD_SUCCESS, HUMO, HUMO_HOLD_PAN);
    }

    @Test
    @Transactional
    @WithMockUser(username = DummyDataAdapterV3.DUMMY_AGENT)
    public void testConfirmPaymentToSuccess() throws Exception {
        int databaseSizeBeforeCreate = transactionRepository.findAll().size();

        Instant instant = Instant.now();

        CheckRequest simpleCheckDTO = getSimpleCheckDTO(DUMMY_AMOUNT);

        SimpleBillResponse simpleBillDTO = getSimpleBillDTO(simpleCheckDTO);

        assertThat(simpleBillDTO).isNotNull();
        assertThat(simpleBillDTO.isSuccess()).isTrue();
        assertThat(simpleBillDTO.getAmount()).isEqualTo(simpleCheckDTO.getAmount());
        assertThat(simpleBillDTO.getAccount()).isEqualTo(simpleCheckDTO.getAccount());

        HoldPayRequest payDTO = new HoldPayRequest();
//        payDTO.setAmount(DUMMY_AMOUNT);
        payDTO.setGatewayAmount(30000.03);
        payDTO.setBillId(simpleBillDTO.getId());
        payDTO.setIsOnlyHold(true);
        payDTO.setType(PayType.PC);

        Map<String, Object> params = new HashMap<>();
        params.put(Constants.PAN_PARAM, HUMO_HOLD_PAN);
        params.put(Constants.EXPIRY_PARAM, HUMO_HOLD_EXP);
        params.put(Constants.OTP_PARAM, TEST_OTP_CODE);
        payDTO.setParams(params);

        MvcResult mvcResult = restAgentMockMvc.perform(
                        post("/api/agent/hold")
                                .content(TestUtil.convertObjectToJsonBytes(payDTO))
                                .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
                )
                .andExpect(status().isOk())
                .andReturn();

        simpleBillDTO = jacksonMessageConverter.getObjectMapper().readValue(mvcResult.getResponse().getContentAsByteArray(), SimpleBillResponse.class);

        assertThat(simpleBillDTO).isNotNull();
        assertThat(simpleBillDTO.isSuccess()).isTrue();
        assertThat(simpleBillDTO.getAmount()).isEqualTo(DUMMY_AMOUNT);
        assertThat(simpleBillDTO.getAccount()).isEqualTo(DUMMY_ACCOUNT);

        List<Transaction> transactionList = transactionRepository.findAll();
        assertThat(transactionList).hasSize(databaseSizeBeforeCreate + 1);
        Transaction testTransaction = transactionList.get(transactionList.size() - 1);

        testPayTransaction(testTransaction, instant, simpleCheckDTO, DummyDataAdapterV3.DUMMY_AGENT, TransactionStatus.SUCCESS, DebitConfirmService.HOLD_SUCCESS, DebitConfirmService.HOLD_SUCCESS, HUMO, HUMO_HOLD_PAN);

        PayRequest payRequest = new PayRequest();
        payRequest.setBillId(simpleBillDTO.getId());

        Map<String, Object> confirmParams = new HashMap<>();
        confirmParams.put(Constants.PAN_PARAM, HUMO_HOLD_PAN);
        confirmParams.put(Constants.EXPIRY_PARAM, HUMO_HOLD_EXP);
        payRequest.setParams(confirmParams);


        MvcResult confirmResult = restAgentMockMvc.perform(
                        post("/api/agent/confirm")
                                .content(TestUtil.convertObjectToJsonBytes(payDTO))
                                .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
                )
                .andExpect(status().isOk())
                .andReturn();

        simpleBillDTO = jacksonMessageConverter.getObjectMapper().readValue(confirmResult.getResponse().getContentAsByteArray(), SimpleBillResponse.class);

        assertThat(simpleBillDTO).isNotNull();
        assertThat(simpleBillDTO.isSuccess()).isTrue();
        assertThat(simpleBillDTO.getAmount()).isEqualTo(DUMMY_AMOUNT);
        assertThat(simpleBillDTO.getAccount()).isEqualTo(HumoUtils.mask(HUMO_HOLD_PAN));
        assertThat(simpleBillDTO.getRecipientShortName()).isEqualTo(HUMO_DEBIT_CONFIRM);
        assertThat(simpleBillDTO.getExtId()).isNotNull();

        List<Transaction> transactionList2 = transactionRepository.findAll();
        assertThat(transactionList2).hasSize(databaseSizeBeforeCreate + 1);
        Transaction testTransaction2 = transactionList2.get(transactionList.size() - 1);

        assertThat(testTransaction2.getSenderBillId()).isEqualTo(simpleBillDTO.getId());
        assertThat(testTransaction2.getSenderTrId()).isEqualTo(simpleBillDTO.getExtId());
        testPayTransaction(testTransaction2, instant, simpleCheckDTO, DummyDataAdapterV3.DUMMY_AGENT, TransactionStatus.SUCCESS, HUMO_SUCCESS_STATUS_CODE, TransactionStatus.SUCCESS.name(), HUMO, HumoUtils.mask(HUMO_HOLD_PAN));

    }

    public void testPayTransaction(Transaction testTransaction, Instant beforeTranInstant, CheckRequest checkDTO, String agentLogin, TransactionStatus status, String senderStatus, String senderStatusText, String senderGateway, String senderAccount) {

        assertThat(testTransaction.getRequestLogin()).isEqualTo(agentLogin);
        assertThat(testTransaction.getRequestTrId()).isEqualTo(TRAN_ID);
        assertThat(testTransaction.getRequestGroup()).isEqualTo(DummyDataAdapterV3.DUMMY_AGENT);
        assertThat(testTransaction.getComment()).isNotNull();

//        assertThat(testTransaction.getSystemFeeAmount()).isEqualTo(DEFAULT_SYSTEM_FEE_AMOUNT);
//        assertThat(testTransaction.getSystemFeeCurrency()).isEqualTo(860);
//        assertThat(testTransaction.getSystemFeePercent()).isEqualTo(DEFAULT_SYSTEM_FEE_PERCENT);
        assertThat(testTransaction.getStatus()).isEqualTo(status);
        assertThat(testTransaction.getDate()).isAfter(beforeTranInstant);
        assertThat(testTransaction.getCcyRate()).isEqualTo(cbuService.getCurrencyRate(DummyDataAdapterV3.CURRENCY));

        assertThat(testTransaction.getRecipientAccount()).isEqualTo(checkDTO.getAccount());
        assertThat(testTransaction.getRecipientAmount()).isEqualTo(930000L);
        assertThat(testTransaction.getRecipientCcy()).isEqualTo(DummyDataAdapterV3.CURRENCY);
        assertThat(testTransaction.getRecipientGateway()).isEqualTo(DummyDataAdapterV3.DUMMY_GATEWAY_NAME);
        assertThat(testTransaction.getRecipientDate()).isAfter(beforeTranInstant);
        assertThat(testTransaction.getRecipientStatus()).isEqualTo(DummyGatewayServiceV3.RESPONSE_CODE);
        assertThat(testTransaction.getRecipientStatusText()).isEqualTo(DummyGatewayServiceV3.RESPONSE_TEXT);
        assertThat(testTransaction.isRecipientSuccess()).isEqualTo(true);
        assertThat(testTransaction.getRecipientBillId()).isNotNull();
        assertThat(testTransaction.getRecipientTrId()).isEqualTo(DummyGatewayServiceV3.EXT_PAY_ID);
        assertThat(testTransaction.getRecipientServiceId()).isEqualTo(checkDTO.getRecipientId());

        assertThat(testTransaction.getSenderServiceId()).isEqualTo(0L);
        assertThat(testTransaction.getSenderGateway()).isEqualTo(senderGateway);
        assertThat(testTransaction.getSenderAccount()).isEqualTo(senderAccount);
//        assertThat(testTransaction.getSenderAmount()).isEqualTo(checkDTO.getAmount()); //TODO - fix
        assertThat(testTransaction.getSenderCcy()).isEqualTo(DummyDataAdapterV3.CURRENCY);
        assertThat(testTransaction.getSenderTrId()).isNotNull();
        assertThat(testTransaction.isSenderSuccess()).isTrue();
        assertThat(testTransaction.getSenderDate()).isAfter(beforeTranInstant);
        assertThat(testTransaction.getSenderStatus()).isEqualTo(senderStatus);
        assertThat(testTransaction.getSenderStatusText()).isEqualTo(senderStatusText);
        assertThat(testTransaction.getSenderBillId()).isNotNull();
    }

}
