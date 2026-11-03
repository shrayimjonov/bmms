package uz.paykap.web.rest;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.web.PageableArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.http.converter.ResourceHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import uz.devops.bm.paynet.config.PaynetConstants;
import uz.devops.bm.paynet.service.feign.PaynetRpcClient;
import uz.devops.bmcore.dto.request.CheckRequest;
import uz.devops.bmcore.dto.request.CommonPayRequest;
import uz.devops.bmcore.dto.request.PayRequest;
import uz.devops.bmcore.dto.response.SimpleBillResponse;
import uz.devops.bmcore.enumeration.PayType;
import uz.devops.bmcore.util.FileUtil;
import uz.devops.bmcore.util.SharedContentStoreService;
import uz.javlon.commons.base.Result;
import uz.javlon.svgate.domain.Bank;
import uz.javlon.svgate.repository.BankRepository;
import uz.javlon.svgate.service.simulation.PredefinedCardRepo2;
import uz.paykap.PkbillmsApp;
import uz.paykap.adapter.DataAdapterResponse;
import uz.paykap.adapter.DataAdapterServiceV3;
import uz.paykap.adapter.uzcard.UzcardCreditDataAdapterV3;
import uz.paykap.adapter.uzcard.UzcardDebitDataAdapterV3;
import uz.paykap.agent.tran.CommonTranServiceV3;
import uz.paykap.config.Constants;
import uz.paykap.config.SecurityBeanOverrideConfiguration;
import uz.paykap.domain.*;
import uz.paykap.gateway.uzcard.UzcardCreditGatewayServiceV3;
import uz.paykap.gateway.uzcard.UzcardDebitGatewayServiceV3;
import uz.paykap.repository.*;
import uz.paykap.web.rest.errors.ExceptionTranslator;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uz.paykap.web.rest.TestUtil.createFormattingConversionService;

@Ignore("Veryyy long test, use only in development")
@EnableFeignClients(clients = {PaynetRpcClient.class})
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {PkbillmsApp.class, SecurityBeanOverrideConfiguration.class},
        properties = {
                "bm-paynet.fromFile=true",
                "bm-paynet.paynetJsonPath=C:\\\\Users\\\\javasparx\\\\Desktop\\\\paynet-11.18.21.json",
                "bm-paynet.syncAll=true",
                "bm-paynet.impl=RPCv5",
                "bm-paynet.rpc.url=https://localhost:8587/gw/transaction",
                "bm-paynet.rpc.username=demo",
                "bm-paynet.rpc.password=demopassword",
                "bm-paynet.rpc.terminal=111",
                "bm-paynet.simulate=false",
//                "logging.level.uz.paykap.adapter.DataAdapterServiceV3=DEBUG",
        }
)
public class SharedAgentResourceToPaynetRpcV3Test {

    public static final String DUMMY_ACCOUNT = "DUMMY_ACCOUNT";
    public static final String TRAN_ID = "TRAN_ID";
    public static final Long DUMMY_AMOUNT = 50100L;
    public static final String GAI_SHTRAF = "Штрафы ГУБДД";
    @Autowired
    private DataAdapterServiceV3 dataAdapterServiceV3;
    @Autowired
    private RecipientRepository recipientRepository;
    @Autowired
    private AgentRecipientRepository agentRecipientRepository;
    @Autowired
    private AgentRepository agentRepository;
    @Autowired
    private DepositRepository depositRepository;
    @Autowired
    private CommonTranServiceV3 commonTranServiceV3;
    @Autowired
    private SharedContentStoreService sharedContentStoreService;
    @Autowired
    private PageableArgumentResolver pageableArgumentResolver;
    @Autowired
    private ExceptionTranslator exceptionTranslator;
    @Autowired
    private MappingJackson2HttpMessageConverter jacksonMessageConverter;
    @Autowired
    private TransactionRepository transactionRepository;
    @Autowired
    private BankRepository bankRepository;


    private MockMvc restAgentMockMvc;

    private Long billId;

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

    @Transactional
    public void syncPaynetAdapter() {

        ArrayList<DataAdapterResponse> allAdapters2 = dataAdapterServiceV3.getAllAdapters();

        assertThat(allAdapters2).isNotEmpty();

        for (DataAdapterResponse dataAdapterResponse : allAdapters2) {
            if (dataAdapterResponse.getName().equals(PaynetConstants.PAYNET_RPC_DATA_ADAPTER)) {
                Result sync = dataAdapterServiceV3.sync(dataAdapterResponse.getName());
                assertThat(sync).isNotNull();
                assertThat(sync.isSuccess()).isTrue();
            }
        }

        recipientRepository.findAll();
    }

    @Transactional
    public void addAgentRecipient(String... recipientShortNames) throws Exception {

        Agent agent = new Agent();
        agent.setActive(true);
        agent.setShortName("user");
        agent.setCurrencies(getCurrencies());
        agent.setAmountRatio(100);
        agent.setApiKey("bomBom123");
        agent = agentRepository.save(agent);

        Deposit deposit = new Deposit();
        deposit.setBalance(10000000L);
        deposit.setAgent(agent);
        deposit.setBalanceCcy(860);
        deposit.setBlocked(false);
        deposit.setLimitInform(0L);
        deposit.setLimitDisable(0L);
        deposit.setUnlimit(false);
        deposit.setOverdraftLimit(0L);
        deposit.setBookedBalance(0L);
        deposit = depositRepository.save(deposit);

        agent.setDeposit(deposit);
        agentRepository.save(agent);

        for (String s : recipientShortNames) {
            Recipient recipient = recipientRepository.findTop1ByShortName(s).orElseThrow(() -> new Exception("recipient not found"));

            AgentRecipient agentRecipient = new AgentRecipient();
            agentRecipient.setAgent(agent);
            agentRecipient.setRecipient(recipient);
            agentRecipient.setActive(true);
            agentRecipientRepository.save(agentRecipient);
        }

//        depositService.increaseAgentDeposit("user", 10000000L);
    }

    private Set<Integer> getCurrencies() {
        Set<Integer> integers = new HashSet<>();
        integers.add(860);
        return integers;
    }

    @Test
    @Transactional
    @WithMockUser("user")
    public void testCheckToSuccess() throws Exception {
        syncPaynetAdapter();
        addAgentRecipient(FileUtil.cleanUpFileName("UCell"));


        Optional<Recipient> optionalRecipient = recipientRepository.findTop1ByShortName("UCell");
        assertThat(optionalRecipient.isPresent()).isTrue();
        Long recipientId = optionalRecipient.get().getId();


        CheckRequest simpleCheckDTO = getSimpleCheckDTO();
        simpleCheckDTO.setAmount(null);
        simpleCheckDTO.setRecipientId(recipientId);

        MvcResult mvcResult = restAgentMockMvc.perform(
                        post("/api/agent/check/v3")
                                .content(TestUtil.convertObjectToJsonBytes(simpleCheckDTO))
                                .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
                )
                .andExpect(status().isOk())
                .andReturn();


        assertThat(mvcResult.getResponse().getContentAsString()).isNotEmpty();

        SimpleBillResponse simpleBillDTO = jacksonMessageConverter.getObjectMapper().readValue(mvcResult.getResponse().getContentAsByteArray(), SimpleBillResponse.class);

        assertThat(simpleBillDTO).isNotNull();
        assertThat(simpleBillDTO.isSuccess()).isTrue();
        assertThat(simpleBillDTO.getAmount()).isEqualTo(simpleCheckDTO.getAmount());
        assertThat(simpleBillDTO.getAccount()).isEqualTo(simpleCheckDTO.getAccount());

        billId = simpleBillDTO.getId();

        List<Transaction> transactionList = transactionRepository.findAll();
        Transaction testTransaction = transactionList.get(transactionList.size() - 1);

        assertThat(simpleBillDTO.getId()).isEqualTo(testTransaction.getId());
    }

    @Test
    @Transactional
    @WithMockUser("user")
    public void testCheckGaiShtrafWithFieldInvoiceType() throws Exception {

        syncPaynetAdapter();
        addAgentRecipient(GAI_SHTRAF);

        Optional<Recipient> optionalRecipient = recipientRepository.findTop1ByShortName(GAI_SHTRAF);
        assertThat(optionalRecipient.isPresent()).isTrue();
        Long recipientId = optionalRecipient.get().getId();


        CheckRequest simpleCheckDTO = getSimpleCheckDTO();
        simpleCheckDTO.setAmount(null);
        simpleCheckDTO.setRecipientId(recipientId);

        MvcResult mvcResult = restAgentMockMvc.perform(
                        post("/api/agent/check/v3")
                                .content(TestUtil.convertObjectToJsonBytes(simpleCheckDTO))
                                .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
                )
                .andExpect(status().isOk())
                .andReturn();


        assertThat(mvcResult.getResponse().getContentAsString()).isNotEmpty();

        SimpleBillResponse simpleBillDTO = jacksonMessageConverter.getObjectMapper().readValue(mvcResult.getResponse().getContentAsByteArray(), SimpleBillResponse.class);

        assertThat(simpleBillDTO).isNotNull();
        assertThat(simpleBillDTO.isSuccess()).isTrue();
        assertThat(simpleBillDTO.getAmount()).isEqualTo(simpleCheckDTO.getAmount());
        assertThat(simpleBillDTO.getAccount()).isEqualTo(simpleCheckDTO.getAccount());

        billId = simpleBillDTO.getId();

        List<Transaction> transactionList = transactionRepository.findAll();
        Transaction testTransaction = transactionList.get(transactionList.size() - 1);

        assertThat(simpleBillDTO.getId()).isEqualTo(testTransaction.getId());
    }

    @Test
    @Transactional
    @WithMockUser("user")
    public void testPayToSuccess() throws Exception {
        testCheckToSuccess();

        CommonPayRequest payDTO = new PayRequest();
        payDTO.setBillId(billId);
        payDTO.setType(PayType.CASH);
        payDTO.setAmount(DUMMY_AMOUNT);

        if (payDTO.getParams() == null)
            payDTO.setParams(new HashMap<>());

        payDTO.getParams().put("pan", "986060HQLNJM5367");

        MvcResult mvcResult = restAgentMockMvc.perform(
                        post("/api/agent/pay/v3")
                                .content(TestUtil.convertObjectToJsonBytes(payDTO))
                                .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
                )
                .andExpect(status().isOk())
                .andReturn();

        SimpleBillResponse simpleBillDTO = jacksonMessageConverter.getObjectMapper().readValue(mvcResult.getResponse().getContentAsByteArray(), SimpleBillResponse.class);

        assertThat(simpleBillDTO).isNotNull();
        assertThat(simpleBillDTO.isSuccess()).isTrue();
        assertThat(simpleBillDTO.getAmount()).isEqualTo(DUMMY_AMOUNT);
        assertThat(simpleBillDTO.getAccount()).isEqualTo(DUMMY_ACCOUNT);
    }

    @Test
    @Transactional
    @WithMockUser("user")
    public void testUzcardDebitCheck() throws Exception {

        dataAdapterServiceV3.sync(UzcardDebitDataAdapterV3.UZCARD_DATA_ADAPTER);

//        addAgentRecipient();

        Optional<Recipient> optionalRecipient = recipientRepository.findTop1ByShortName(UzcardDebitGatewayServiceV3.NAME);
        assertThat(optionalRecipient.isPresent()).isTrue();
        Long recipientId = optionalRecipient.get().getId();

        addAgentRecipient(optionalRecipient.get().getShortName());

//        CheckRequest simpleCheckDTO = getSimpleCheckDTO();
        CheckRequest simpleCheckDTO = new CheckRequest();

        simpleCheckDTO.setAccount(PredefinedCardRepo2.REAL_PAN);
        simpleCheckDTO.setAmount(DUMMY_AMOUNT);
        simpleCheckDTO.setCurrency(860);
        simpleCheckDTO.setAgentTranId(TRAN_ID);
        simpleCheckDTO.setRecipientId(recipientId);
        simpleCheckDTO.setParams(Collections.singletonMap(Constants.EXPIRY_PARAM, "1234"));

        MvcResult mvcResult = restAgentMockMvc.perform(
                        post("/api/agent/check/v3")
                                .content(TestUtil.convertObjectToJsonBytes(simpleCheckDTO))
                                .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
                )
                .andExpect(status().isOk())
                .andReturn();


        assertThat(mvcResult.getResponse().getContentAsString()).isNotEmpty();

        SimpleBillResponse simpleBillDTO = jacksonMessageConverter.getObjectMapper().readValue(mvcResult.getResponse().getContentAsByteArray(), SimpleBillResponse.class);

        assertThat(simpleBillDTO).isNotNull();
        assertThat(simpleBillDTO.isSuccess()).isTrue();
        assertThat(simpleBillDTO.getAmount()).isEqualTo(simpleCheckDTO.getAmount());
        assertThat(simpleBillDTO.getAccount()).isEqualTo(simpleCheckDTO.getAccount());

        billId = simpleBillDTO.getId();

        List<Transaction> transactionList = transactionRepository.findAll();
        Transaction testTransaction = transactionList.get(transactionList.size() - 1);

        assertThat(simpleBillDTO.getId()).isEqualTo(testTransaction.getId());
    }

    @Test
    @Transactional
    @WithMockUser("user")
    public void testUzcardDebitPay() throws Exception {

        dataAdapterServiceV3.sync(UzcardDebitDataAdapterV3.UZCARD_DATA_ADAPTER);

//        addAgentRecipient();

        Optional<Recipient> optionalRecipient = recipientRepository.findTop1ByShortName(UzcardDebitGatewayServiceV3.NAME);
        assertThat(optionalRecipient.isPresent()).isTrue();
        Long recipientId = optionalRecipient.get().getId();

        addAgentRecipient(optionalRecipient.get().getShortName());

//        CheckRequest simpleCheckDTO = getSimpleCheckDTO();
        CheckRequest simpleCheckDTO = new CheckRequest();

        simpleCheckDTO.setAccount(PredefinedCardRepo2.REAL_PAN);
        simpleCheckDTO.setAmount(DUMMY_AMOUNT);
        simpleCheckDTO.setCurrency(860);
        simpleCheckDTO.setAgentTranId(TRAN_ID);
        simpleCheckDTO.setRecipientId(recipientId);
        simpleCheckDTO.setParams(Collections.singletonMap(Constants.EXPIRY_PARAM, "1234"));

        MvcResult mvcResult = restAgentMockMvc.perform(
                        post("/api/agent/check/v3")
                                .content(TestUtil.convertObjectToJsonBytes(simpleCheckDTO))
                                .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
                )
                .andExpect(status().isOk())
                .andReturn();


        assertThat(mvcResult.getResponse().getContentAsString()).isNotEmpty();

        SimpleBillResponse simpleBillDTO = jacksonMessageConverter.getObjectMapper().readValue(mvcResult.getResponse().getContentAsByteArray(), SimpleBillResponse.class);

        assertThat(simpleBillDTO).isNotNull();
        assertThat(simpleBillDTO.isSuccess()).isTrue();
        assertThat(simpleBillDTO.getAmount()).isEqualTo(simpleCheckDTO.getAmount());
        assertThat(simpleBillDTO.getAccount()).isEqualTo(simpleCheckDTO.getAccount());

        billId = simpleBillDTO.getId();

        List<Transaction> transactionList = transactionRepository.findAll();
        Transaction testTransaction = transactionList.get(transactionList.size() - 1);

        assertThat(simpleBillDTO.getId()).isEqualTo(testTransaction.getId());

        HashMap<String, Object> params = new HashMap<>();
        params.put(Constants.EPOS_MERCHANT_PARAM, "123");
        params.put(Constants.EPOS_TERMINAL_PARAM, "345");

        CommonPayRequest payDTO = new PayRequest();
        payDTO.setParams(params);
        payDTO.setBillId(billId);
        payDTO.setType(PayType.AGENT);

        if (payDTO.getParams() == null) {
            payDTO.setParams(new HashMap<>());
        }

//        payDTO.getParams().put("pan", "986060HQLNJM5367");

        mvcResult = restAgentMockMvc.perform(
                        post("/api/agent/pay/v3")
                                .content(TestUtil.convertObjectToJsonBytes(payDTO))
                                .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
                )
                .andExpect(status().isOk())
                .andReturn();

        simpleBillDTO = jacksonMessageConverter.getObjectMapper().readValue(mvcResult.getResponse().getContentAsByteArray(), SimpleBillResponse.class);

        assertThat(simpleBillDTO).isNotNull();
        assertThat(simpleBillDTO.isSuccess()).isTrue();
        assertThat(simpleBillDTO.getAmount()).isEqualTo(DUMMY_AMOUNT);
        assertThat(simpleBillDTO.getAccount()).isEqualTo(PredefinedCardRepo2.REAL_PAN);
    }

    @Test
    @Transactional
    @WithMockUser("user")
    public void testUzcardCreditPay() throws Exception {

        prepareBank("860049");

        dataAdapterServiceV3.sync(UzcardCreditDataAdapterV3.UZCARD_DATA_ADAPTER);

//        addAgentRecipient();

        Optional<Recipient> optionalRecipient = recipientRepository.findTop1ByShortName(UzcardCreditGatewayServiceV3.NAME);
        assertThat(optionalRecipient.isPresent()).isTrue();
        Long recipientId = optionalRecipient.get().getId();

        addAgentRecipient(optionalRecipient.get().getShortName());

//        CheckRequest simpleCheckDTO = getSimpleCheckDTO();
        CheckRequest simpleCheckDTO = new CheckRequest();

        simpleCheckDTO.setAccount(PredefinedCardRepo2.REAL_PAN);
        simpleCheckDTO.setAmount(DUMMY_AMOUNT);
        simpleCheckDTO.setCurrency(860);
        simpleCheckDTO.setAgentTranId(TRAN_ID);
        simpleCheckDTO.setRecipientId(recipientId);
        simpleCheckDTO.setParams(Collections.singletonMap(Constants.EXPIRY_PARAM, "1234"));

        MvcResult mvcResult = restAgentMockMvc.perform(
                        post("/api/agent/check/v3")
                                .content(TestUtil.convertObjectToJsonBytes(simpleCheckDTO))
                                .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
                )
                .andExpect(status().isOk())
                .andReturn();


        assertThat(mvcResult.getResponse().getContentAsString()).isNotEmpty();

        SimpleBillResponse simpleBillDTO = jacksonMessageConverter.getObjectMapper().readValue(mvcResult.getResponse().getContentAsByteArray(), SimpleBillResponse.class);

        assertThat(simpleBillDTO).isNotNull();
        assertThat(simpleBillDTO.isSuccess()).isTrue();
        assertThat(simpleBillDTO.getAmount()).isEqualTo(simpleCheckDTO.getAmount());
        assertThat(simpleBillDTO.getAccount()).isEqualTo(simpleCheckDTO.getAccount());

        billId = simpleBillDTO.getId();

        List<Transaction> transactionList = transactionRepository.findAll();
        Transaction testTransaction = transactionList.get(transactionList.size() - 1);

        assertThat(simpleBillDTO.getId()).isEqualTo(testTransaction.getId());

        HashMap<String, Object> params = new HashMap<>();
        params.put(Constants.EPOS_MERCHANT_PARAM, "123");
        params.put(Constants.EPOS_TERMINAL_PARAM, "345");

        CommonPayRequest payDTO = new PayRequest();
        payDTO.setParams(params);
        payDTO.setBillId(billId);
        payDTO.setType(PayType.AGENT);

        if (payDTO.getParams() == null) {
            payDTO.setParams(new HashMap<>());
        }

//        payDTO.getParams().put("pan", "986060HQLNJM5367");

        mvcResult = restAgentMockMvc.perform(
                        post("/api/agent/pay/v3")
                                .content(TestUtil.convertObjectToJsonBytes(payDTO))
                                .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
                )
                .andExpect(status().isOk())
                .andReturn();

        simpleBillDTO = jacksonMessageConverter.getObjectMapper().readValue(mvcResult.getResponse().getContentAsByteArray(), SimpleBillResponse.class);

        assertThat(simpleBillDTO).isNotNull();
        assertThat(simpleBillDTO.isSuccess()).isTrue();
        assertThat(simpleBillDTO.getAmount()).isEqualTo(DUMMY_AMOUNT);
        assertThat(simpleBillDTO.getAccount()).isEqualTo(PredefinedCardRepo2.REAL_PAN);
    }

    private void prepareBank(String cardBin) {
        Bank bank = new Bank();
        bank
                .cardBin(cardBin)
                .cardLimitIn(1000000L)
                .cardLimitOut(100000000L)
                .name("Test Bank")
                .p2p(Boolean.TRUE)
                .terminalId("12345")
                .port(1)
                .merchantId("54321")
        ;
        bankRepository.saveAndFlush(bank);
    }


    private CheckRequest getSimpleCheckDTO() {
        CheckRequest commonCheckDTO = new CheckRequest();

        commonCheckDTO.setAccount(DUMMY_ACCOUNT);
        commonCheckDTO.setAmount(DUMMY_AMOUNT);
        commonCheckDTO.setCurrency(860);
        commonCheckDTO.setAgentTranId(TRAN_ID);

        return commonCheckDTO;
    }
}
