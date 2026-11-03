package uz.paykap.web.rest;

import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.jupiter.api.Disabled;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.data.web.PageableArgumentResolver;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.converter.ResourceHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.client.ExpectedCount;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.client.match.MockRestRequestMatchers;
import org.springframework.test.web.client.response.MockRestResponseCreators;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import uz.devops.bm.paynet.config.PaynetConstants;
import uz.devops.bmcore.dto.request.CheckRequest;
import uz.devops.bmcore.dto.request.CommonPayRequest;
import uz.devops.bmcore.dto.request.PayRequest;
import uz.devops.bmcore.dto.response.SimpleBillResponse;
import uz.devops.bmcore.enumeration.PayType;
import uz.devops.bmcore.util.FileUtil;
import uz.devops.bmcore.util.SharedContentStoreService;
import uz.javlon.commons.base.Result;
import uz.javlon.paynet.config.PaynetProperties;
import uz.javlon.paynet.service.impl.PaynetSenderSimulation;
import uz.javlon.svgate.domain.Bank;
import uz.javlon.svgate.repository.BankRepository;
import uz.javlon.svgate.service.simulation.PredefinedCardRepo2;
import uz.paykap.PkbillmsApp;
import uz.paykap.adapter.DataAdapterServiceV3;
import uz.paykap.adapter.uzcard.UzcardCreditDataAdapterV3;
import uz.paykap.adapter.uzcard.UzcardDebitDataAdapterV3;
import uz.paykap.agent.tran.CommonTranServiceV3;
import uz.paykap.config.Constants;
import uz.paykap.config.TestSecurityConfiguration;
import uz.paykap.domain.*;
import uz.paykap.gateway.uzcard.UzcardCreditGatewayServiceV3;
import uz.paykap.gateway.uzcard.UzcardDebitGatewayServiceV3;
import uz.paykap.repository.*;
import uz.paykap.web.rest.errors.ExceptionTranslator;

import java.net.SocketTimeoutException;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uz.paykap.web.rest.TestUtil.createFormattingConversionService;

//@EnableFeignClients(clients = {PaynetRpcClient.class})
@Disabled
@Ignore
@RunWith(SpringRunner.class)
@SpringBootTest(
        classes = {PkbillmsApp.class, TestSecurityConfiguration.class},
        properties = {"paynet.simulation=false", "bm-paynet.dataAdapterImpl=RPCv4", "bm-paynet.gatewayServiceImpl=RPCv5"/*, "bm-paynet.paynetJsonPath=paynet.json"*/}
)
public class SharedAgentResourceToPaynetJsonV3Test {

    @Autowired
    @Qualifier("paynetRestTemplate")
    private RestTemplate restTemplate;

    private MockRestServiceServer mockServer;

    public static final String DUMMY_ACCOUNT = "DUMMY_ACCOUNT";
    public static final String TRAN_ID = "TRAN_ID";
    public static final Long DUMMY_AMOUNT = 50100L;
    public static final String GAI_SHTRAF = FileUtil.cleanUpFileName("Штрафы ГУБДД");
    public static final String GOS_NALOG = FileUtil.cleanUpFileName("Гос-й Налог. Комитет");
    public static final String UCELL = FileUtil.cleanUpFileName("UCell");

    @Autowired
    private PaynetProperties paynetProperties;
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

        MockitoAnnotations.openMocks(this);
        final SharedAgentResourceV3 adapterResource = new SharedAgentResourceV3(commonTranServiceV3, sharedContentStoreService);
        this.restAgentMockMvc = MockMvcBuilders.standaloneSetup(adapterResource)
                .setCustomArgumentResolvers(pageableArgumentResolver)
                .setControllerAdvice(exceptionTranslator)
                .setConversionService(createFormattingConversionService())
                .setMessageConverters(jacksonMessageConverter, new ResourceHttpMessageConverter()).build();
    }

    @Transactional
    public void syncPaynetAdapter() {

//        ArrayList<DataAdapterResponse> allAdapters2 = dataAdapterServiceV3.getAllAdapters();

//        assertThat(allAdapters2).isNotEmpty();

//        for (DataAdapterResponse dataAdapterResponse : allAdapters2) {
//            if (dataAdapterResponse.getName().equals(PaynetConstants.PAYNET_RPC_DATA_ADAPTER)) {
        Result sync = dataAdapterServiceV3.sync(PaynetConstants.PAYNET_RPC_DATA_ADAPTER);
        assertThat(sync).isNotNull();
        assertThat(sync.isSuccess()).isTrue();
//            }
//        }

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

            recipient.setActive(true);
            recipient.setCheck(true);
            recipient.setPay(true);

            recipientRepository.save(recipient);

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

        mockServer = MockRestServiceServer.createServer(restTemplate);

        initResponseStub("/simulation-data/responses/check-ok-ucell.xml", ExpectedCount.between(1, 2));

        syncPaynetAdapter();
        addAgentRecipient(UCELL);

        Optional<Recipient> optionalRecipient = recipientRepository.findTop1ByShortName(UCELL);
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
    public void testCheckGaiShtrafWithFieldInvoiceTypeAmountNull() throws Exception {

        mockServer = MockRestServiceServer.createServer(restTemplate);

        initResponseStub("/simulation-data/responses/check-ok-shtraf.xml");

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
        assertThat(simpleBillDTO.getAmount()).isEqualTo(8746500L); /* amount from shtraf-ok.xml  */
        assertThat(simpleBillDTO.getAccount()).isEqualTo(simpleCheckDTO.getAccount());

        billId = simpleBillDTO.getId();

        List<Transaction> transactionList = transactionRepository.findAll();
        Transaction testTransaction = transactionList.get(transactionList.size() - 1);

        assertThat(simpleBillDTO.getId()).isEqualTo(testTransaction.getId());

        PayRequest payRequest = new PayRequest();
        payRequest.setAmount(null);
        payRequest.setBillId(billId);
        payRequest.setType(PayType.UZCARD);
        payRequest.setParams(Map.of(Constants.TOKEN_PARAM, PredefinedCardRepo2.DEFAULT_TOKEN));

        mvcResult = restAgentMockMvc.perform(
                        post("/api/agent/pay/v3")
                                .content(TestUtil.convertObjectToJsonBytes(payRequest))
                                .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
                )
                .andExpect(status().isOk())
                .andReturn();

        assertThat(mvcResult.getResponse().getContentAsString()).isNotEmpty();

        SimpleBillResponse payResponse = jacksonMessageConverter.getObjectMapper().readValue(mvcResult.getResponse().getContentAsByteArray(), SimpleBillResponse.class);

        assertThat(payResponse).isNotNull();
        assertThat(payResponse.isSuccess()).isTrue();
        assertThat(payResponse.getAmount()).isEqualTo(8746500L); /* amount from shtraf-ok.xml  */
        assertThat(payResponse.getAccount()).isEqualTo(simpleCheckDTO.getAccount());
    }

    private void initResponseStub(String resourceFilePath) {
        initResponseStub(resourceFilePath, ExpectedCount.between(1, 2));
    }

    private void initResponseStub(String resourceFilePath, ExpectedCount expectedCount) {
        Resource shtrafResponseResource = new ClassPathResource(resourceFilePath);

        mockServer
                .expect(
                        expectedCount,
                        MockRestRequestMatchers.requestTo(Matchers.allOf(
                                Matchers.matchesPattern(paynetProperties.getBaseUrl() + ".*"),
                                Matchers.matchesPattern(".*TERMINAL_ID.*"),
                                Matchers.matchesPattern(".*USERNAME.*"),
                                Matchers.matchesPattern(".*ACT=2.*"),
                                Matchers.matchesPattern(".*SERVICE_ID.*"),
                                Matchers.matchesPattern(".*PASSWORD.*")
                        ))
                )
                .andExpect(MockRestRequestMatchers.method(HttpMethod.GET))
                .andRespond(MockRestResponseCreators.withStatus(HttpStatus.OK)
                        .contentType(MediaType.APPLICATION_XML)
                        .body(shtrafResponseResource)
                );
    }

    @Test
    @Transactional
    @WithMockUser("user")
    public void testCheckZemelNalogWithFieldPayAmount() throws Exception {

        /*
         * !!!IMPORTANT!!! DO NOT CHANGE AMOUNT=51200
         *
         * pay_amount is 512 in simulation response,
         * amount must be 512 (51.200 cents)
         *  */
        final long nalogAmount = 51200L;
        final String nalogAccount = "514979491";

        mockServer = MockRestServiceServer.createServer(restTemplate);

        Resource shtrafResponseResource = new ClassPathResource("/simulation-data/responses/check-ok-nalog.xml");

        mockServer.expect(MockRestRequestMatchers.requestTo(
                        Matchers.allOf(
                                Matchers.matchesPattern(paynetProperties.getBaseUrl() + ".*"),
                                Matchers.matchesPattern(".*TERMINAL_ID.*"),
                                Matchers.matchesPattern(".*USERNAME.*"),
                                Matchers.matchesPattern(".*ACT=2.*"),
                                Matchers.matchesPattern(".*INN=" + nalogAccount + ".*"),
                                Matchers.matchesPattern(".*pay_amount=" + (nalogAmount / 100) + ".*"), // 512 bcz paynet accepts amountRatio=1
                                Matchers.matchesPattern(".*SERVICE_ID=12850.*"),
                                Matchers.matchesPattern(".*PASSWORD.*")
                        )))
                .andExpect(MockRestRequestMatchers.method(HttpMethod.GET))
                .andRespond(MockRestResponseCreators.withStatus(HttpStatus.OK)
                        .contentType(MediaType.APPLICATION_XML)
                        .body(shtrafResponseResource)
                );

//        initResponseStub("/simulation-data/responses/check-ok-nalog.xml");

        syncPaynetAdapter();
        addAgentRecipient(GOS_NALOG);

        Optional<Recipient> optionalRecipient = recipientRepository.findTop1ByShortName(GOS_NALOG);
        assertThat(optionalRecipient.isPresent()).isTrue();
        Long recipientId = optionalRecipient.get().getId();


//        CheckRequest simpleCheckDTO = getSimpleCheckDTO();
        CheckRequest simpleCheckDTO = new CheckRequest();

        simpleCheckDTO.setAccount(nalogAccount);
        simpleCheckDTO.setAmount(nalogAmount);
        simpleCheckDTO.setCurrency(860);
        simpleCheckDTO.setAgentTranId(TRAN_ID);
        simpleCheckDTO.setRecipientId(recipientId);

        Map<String, Object> fields = new HashMap<>();
        fields.put(PaynetSenderSimulation.IS_TAX_PROVIDER_CHECK_REQUEST, "TRUE");
        fields.put("SERVICE_ID", "12850,12851");
        simpleCheckDTO.setParams(fields);

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
        assertThat(simpleBillDTO.getAmount()).isEqualTo(102200L);
        assertThat(simpleBillDTO.getAccount()).isEqualTo(simpleCheckDTO.getAccount());

        billId = simpleBillDTO.getId();

        List<Transaction> transactionList = transactionRepository.findAll();
        Transaction testTransaction = transactionList.get(transactionList.size() - 1);

        assertThat(simpleBillDTO.getId()).isEqualTo(testTransaction.getId());

        /* PAY */

        mockServer = MockRestServiceServer.createServer(restTemplate);

        shtrafResponseResource = new ClassPathResource("/simulation-data/responses/check-ok-nalog.xml");

        mockServer.expect(MockRestRequestMatchers.requestTo(
                        Matchers.allOf(
                                Matchers.matchesPattern(paynetProperties.getBaseUrl() + ".*"),
                                Matchers.matchesPattern(".*TERMINAL_ID.*"),
                                Matchers.matchesPattern(".*USERNAME.*"),
                                Matchers.matchesPattern(".*ACT=2.*"),
                                Matchers.matchesPattern(".*INN=" + nalogAccount + ".*"),
                                Matchers.matchesPattern(".*pay_amount=" + (nalogAmount / 100) + ".*"), // 512 bcz paynet accepts amountRatio=1
                                Matchers.matchesPattern(".*amount=1022.*"), // From paynet
//                                Matchers.matchesPattern(".*select=10385be091144569b33bd86f7a545d47.*"), // From paynet
                                Matchers.matchesPattern(".*SERVICE_ID=12851.*"),
                                Matchers.matchesPattern(".*PASSWORD.*")
                        )))
                .andExpect(MockRestRequestMatchers.method(HttpMethod.GET))
                .andRespond(MockRestResponseCreators.withStatus(HttpStatus.OK)
                        .contentType(MediaType.APPLICATION_XML)
                        .body(shtrafResponseResource)
                );

//        initResponseStub("simulation-data/responses/pay-ok-nalog.xml");

        CommonPayRequest payDTO = new PayRequest();
        payDTO.setBillId(billId);
        payDTO.setType(PayType.HUMO);

        if (payDTO.getParams() == null)
            payDTO.setParams(new HashMap<>());

        payDTO.getParams().put("pan", "986060HQLNJM5367");

        mvcResult = restAgentMockMvc.perform(post("/api/agent/pay/v3")
                        .content(TestUtil.convertObjectToJsonBytes(payDTO))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()).andReturn();

        simpleBillDTO = jacksonMessageConverter.getObjectMapper().readValue(mvcResult.getResponse().getContentAsByteArray(), SimpleBillResponse.class);

        assertThat(simpleBillDTO).isNotNull();
        assertThat(simpleBillDTO.isSuccess()).isTrue();
        assertThat(simpleBillDTO.getAmount()).isEqualTo(102200L);
        assertThat(simpleBillDTO.getAccount()).isEqualTo(nalogAccount);
    }

    @Test
    @Transactional
    @WithMockUser("user")
    public void testSkipCheckThenPayAmount() throws Exception {

        /*
         * !!!IMPORTANT!!! DO NOT CHANGE AMOUNT=51200
         *
         * pay_amount is 512 in simulation response,
         * amount must be 512 (51.200 cents)
         *  */
        final long nalogAmount = 51200L;
        final String nalogAccount = "514979491";

        syncPaynetAdapter();
        addAgentRecipient(UCELL);

        Optional<Recipient> optionalRecipient = recipientRepository.findTop1ByShortName(UCELL);
        assertThat(optionalRecipient.isPresent()).isTrue();
        /*
        * Disable check to skip it
        * */
        recipientRepository.save(optionalRecipient.get().check(false));
        Long recipientId = optionalRecipient.get().getId();


//        CheckRequest simpleCheckDTO = getSimpleCheckDTO();
        CheckRequest simpleCheckDTO = new CheckRequest();

        simpleCheckDTO.setAccount(nalogAccount);
        simpleCheckDTO.setAmount(nalogAmount);
        simpleCheckDTO.setCurrency(860);
        simpleCheckDTO.setAgentTranId(TRAN_ID);
        simpleCheckDTO.setRecipientId(recipientId);

        Map<String, Object> fields = new HashMap<>();
//        fields.put(PaynetSenderSimulation.IS_TAX_PROVIDER_CHECK_REQUEST, "TRUE");
        fields.put("SERVICE_ID", "12850,12851");
        simpleCheckDTO.setParams(fields);

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

        /* PAY */

        mockServer = MockRestServiceServer.createServer(restTemplate);

        Resource shtrafResponseResource = new ClassPathResource("/simulation-data/responses/check-ok-ucell.xml");

        mockServer.expect(MockRestRequestMatchers.requestTo(
                        Matchers.allOf(
                                Matchers.matchesPattern(paynetProperties.getBaseUrl() + ".*"),
                                Matchers.matchesPattern(".*TERMINAL_ID.*"),
                                Matchers.matchesPattern(".*USERNAME.*"),
                                Matchers.matchesPattern(".*ACT=2.*"),
//                                Matchers.matchesPattern(".*INN=" + nalogAccount + ".*"),
                                Matchers.matchesPattern(".*amount=" + (nalogAmount / 100) + ".*"), // 512 bcz paynet accepts amountRatio=1
//                                Matchers.matchesPattern(".*amount=1022.*"), // From paynet
//                                Matchers.matchesPattern(".*select=10385be091144569b33bd86f7a545d47.*"), // From paynet
                                Matchers.matchesPattern(".*SERVICE_ID=12851.*"),
                                Matchers.matchesPattern(".*PASSWORD.*")
                        )))
                .andExpect(MockRestRequestMatchers.method(HttpMethod.GET))
                .andRespond(MockRestResponseCreators.withStatus(HttpStatus.OK)
                        .contentType(MediaType.APPLICATION_XML)
                        .body(shtrafResponseResource)
                );

//        initResponseStub("simulation-data/responses/pay-ok-nalog.xml");

        CommonPayRequest payDTO = new PayRequest();
        payDTO.setBillId(billId);
        payDTO.setType(PayType.HUMO);

        if (payDTO.getParams() == null)
            payDTO.setParams(new HashMap<>());

        payDTO.getParams().put("pan", "986060HQLNJM5367");
        payDTO.getParams().put("SERVICE_ID", "12850,12851");

        mvcResult = restAgentMockMvc.perform(post("/api/agent/pay/v3")
                        .content(TestUtil.convertObjectToJsonBytes(payDTO))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()).andReturn();

        simpleBillDTO = jacksonMessageConverter.getObjectMapper().readValue(mvcResult.getResponse().getContentAsByteArray(), SimpleBillResponse.class);

        assertThat(simpleBillDTO).isNotNull();
        assertThat(simpleBillDTO.isSuccess()).isTrue();
        assertThat(simpleBillDTO.getAmount()).isEqualTo(nalogAmount);
        assertThat(simpleBillDTO.getAccount()).isEqualTo(nalogAccount);
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
    public void testPayTimeout() throws Exception {

        int databaseSizeBeforeCreate = transactionRepository.findAll().size();

        testCheckToSuccess();

        mockServer = MockRestServiceServer.createServer(restTemplate);

        mockServer
                .expect(
                        ExpectedCount.once(),
                        MockRestRequestMatchers.requestTo(Matchers.allOf(
                                Matchers.matchesPattern(".*")
                        ))
                )
                .andExpect(MockRestRequestMatchers.method(HttpMethod.GET))
                .andRespond(
                        (response) -> {
                            throw new ResourceAccessException("Connection reset", new SocketTimeoutException("Connection timeout"));
                        }
                );

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
        assertThat(simpleBillDTO.isSuccess()).isFalse();
        assertThat(simpleBillDTO.getAmount()).isEqualTo(DUMMY_AMOUNT);
        assertThat(simpleBillDTO.getAccount()).isEqualTo(DUMMY_ACCOUNT);

        List<Transaction> transactionList = transactionRepository.findAll();
        assertThat(transactionList).hasSize(databaseSizeBeforeCreate + 1);
        Transaction testTransaction = transactionList.get(transactionList.size() - 1);

        assertThat(testTransaction).isNotNull();
//        assertThat(testTransaction.getStatus()).isEqualTo(TransactionStatus.GATEWAY_TIMEOUT);
        assertThat(testTransaction.getSenderSuccess()).isTrue();
        assertThat(testTransaction.getRecipientSuccess()).isFalse();
        assertThat(testTransaction.getRecipientStatusText()).contains("timeout");
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
