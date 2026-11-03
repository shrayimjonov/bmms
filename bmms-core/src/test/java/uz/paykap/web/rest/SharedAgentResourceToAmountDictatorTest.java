//package uz.paykap.web.rest;
//
//import org.hamcrest.Matchers;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.params.ParameterizedTest;
//import org.junit.jupiter.params.provider.Arguments;
//import org.junit.jupiter.params.provider.MethodSource;
//import org.mockito.MockitoAnnotations;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Qualifier;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.core.io.ClassPathResource;
//import org.springframework.core.io.Resource;
//import org.springframework.data.web.PageableArgumentResolver;
//import org.springframework.http.HttpMethod;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.MediaType;
//import org.springframework.http.converter.ResourceHttpMessageConverter;
//import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
//import org.springframework.security.test.context.support.WithMockUser;
//import org.springframework.test.web.client.ExpectedCount;
//import org.springframework.test.web.client.MockRestServiceServer;
//import org.springframework.test.web.client.match.MockRestRequestMatchers;
//import org.springframework.test.web.client.response.MockRestResponseCreators;
//import org.springframework.test.web.servlet.MockMvc;
//import org.springframework.test.web.servlet.MvcResult;
//import org.springframework.test.web.servlet.setup.MockMvcBuilders;
//import org.springframework.transaction.annotation.Transactional;
//import org.springframework.web.client.RestTemplate;
//import uz.devops.bm.paynet.config.PaynetConstants;
//import uz.devops.bmcore.dto.request.CheckRequest;
//import uz.devops.bmcore.dto.request.PayRequest;
//import uz.devops.bmcore.dto.response.SimpleBillResponse;
//import uz.devops.bmcore.enumeration.PayType;
//import uz.devops.bmcore.util.FileUtil;
//import uz.devops.bmcore.util.SharedContentStoreService;
//import uz.javlon.commons.base.Result;
//import uz.javlon.paynet.config.PaynetProperties;
//import uz.javlon.svgate.service.simulation.PredefinedCardRepo2;
//import uz.paykap.PkbillmsApp;
//import uz.paykap.adapter.DataAdapterServiceV3;
//import uz.paykap.agent.tran.CommonTranServiceV3;
//import uz.paykap.config.Constants;
//import uz.paykap.config.TestSecurityConfiguration;
//import uz.paykap.domain.*;
//import uz.paykap.repository.*;
//import uz.paykap.web.rest.errors.ExceptionTranslator;
//
//import java.util.*;
//import java.util.stream.Stream;
//
//import static org.assertj.core.api.Assertions.assertThat;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
//import static uz.paykap.web.rest.TestUtil.createFormattingConversionService;
//
//@SpringBootTest(
//        classes = {PkbillmsApp.class, TestSecurityConfiguration.class},
//        properties = {"paynet.simulation=true", "bm-paynet.dataAdapterImpl=RPCv4", "bm-paynet.gatewayServiceImpl=RPCv5"/*, "bm-paynet.paynetJsonPath=paynet.json"*/}
//)
//public class SharedAgentResourceToAmountDictatorTest {
//
//    @Autowired
//    @Qualifier("paynetRestTemplate")
//    private RestTemplate restTemplate;
//
//    private MockRestServiceServer mockServer;
//
//    public static final String DUMMY_ACCOUNT = "DUMMY_ACCOUNT";
//    public static final String TRAN_ID = "TRAN_ID";
//    public static final Long DUMMY_AMOUNT = 50100L;
//    public static final String GAI_SHTRAF = FileUtil.cleanUpFileName("Штрафы ГУБДД");
//    public static final String GOS_NALOG = FileUtil.cleanUpFileName("Гос-й Налог. Комитет");
//    public static final String UCELL = FileUtil.cleanUpFileName("UCell");
//
//    @Autowired
//    private PaynetProperties paynetProperties;
//    @Autowired
//    private DataAdapterServiceV3 dataAdapterServiceV3;
//    @Autowired
//    private RecipientRepository recipientRepository;
//    @Autowired
//    private AgentRecipientRepository agentRecipientRepository;
//    @Autowired
//    private AgentRepository agentRepository;
//    @Autowired
//    private DepositRepository depositRepository;
//    @Autowired
//    private CommonTranServiceV3 commonTranServiceV3;
//    @Autowired
//    private SharedContentStoreService sharedContentStoreService;
//    @Autowired
//    private PageableArgumentResolver pageableArgumentResolver;
//    @Autowired
//    private ExceptionTranslator exceptionTranslator;
//    @Autowired
//    private MappingJackson2HttpMessageConverter jacksonMessageConverter;
//    @Autowired
//    private TransactionRepository transactionRepository;
//
//    private MockMvc restAgentMockMvc;
//
//    @BeforeEach
//    public void setup() {
//
//        MockitoAnnotations.openMocks(this);
//        final SharedAgentResourceV3 adapterResource = new SharedAgentResourceV3(commonTranServiceV3, sharedContentStoreService);
//        this.restAgentMockMvc = MockMvcBuilders.standaloneSetup(adapterResource)
//                .setCustomArgumentResolvers(pageableArgumentResolver)
//                .setControllerAdvice(exceptionTranslator)
//                .setConversionService(createFormattingConversionService())
//                .setMessageConverters(jacksonMessageConverter, new ResourceHttpMessageConverter()).build();
//    }
//
//    private static Stream<Arguments> provideStringsForIsBlank() {
//        return Stream.of(
//                Arguments.of(null, null, true, true),
//                Arguments.of(null, 0L, true, true),
//                Arguments.of(0L, 0L, false, true),
//                Arguments.of(100000L, null, true, true),
//                Arguments.of(100000L, 100000L, true, true),
//                Arguments.of(null, 100000L, true, true)
//        );
//    }
//
//    @ParameterizedTest
//    @MethodSource("provideStringsForIsBlank")
//    @Transactional
//    @WithMockUser("user")
//    public void testCheckGaiShtrafWithFieldInvoiceTypeAmountNull(Long checkAmount, Long payAmount, boolean checkResult, boolean payResult) throws Exception {
//
//        mockServer = MockRestServiceServer.createServer(restTemplate);
//
//        initResponseStub("/simulation-data/responses/check-ok-shtraf.xml");
//
//        syncPaynetAdapter();
//        addAgentRecipient(GAI_SHTRAF);
//
//        Optional<Recipient> optionalRecipient = recipientRepository.findTop1ByShortName(GAI_SHTRAF);
//        assertThat(optionalRecipient.isPresent()).isTrue();
//        Long recipientId = optionalRecipient.get().getId();
//
//
//        CheckRequest simpleCheckDTO = getSimpleCheckDTO();
//        simpleCheckDTO.setAmount(checkAmount);
//        simpleCheckDTO.setRecipientId(recipientId);
//
//        MvcResult mvcResult = restAgentMockMvc.perform(
//                        post("/api/agent/check/v3")
//                                .content(TestUtil.convertObjectToJsonBytes(simpleCheckDTO))
//                                .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
//                )
//                .andExpect(status().isOk())
//                .andReturn();
//
//
//        assertThat(mvcResult.getResponse().getContentAsString()).isNotEmpty();
//
//        SimpleBillResponse simpleBillDTO = jacksonMessageConverter.getObjectMapper().readValue(mvcResult.getResponse().getContentAsByteArray(), SimpleBillResponse.class);
//
//        assertThat(simpleBillDTO).isNotNull();
//        assertThat(simpleBillDTO.isSuccess()).isEqualTo(checkResult);
//
//        if (!checkResult) {
//            return;
//        }
//
//        assertThat(simpleBillDTO.getAmount()).isEqualTo(8746500L); /* amount from shtraf-ok.xml  */
//        assertThat(simpleBillDTO.getAccount()).isEqualTo(simpleCheckDTO.getAccount());
//
//        List<Transaction> transactionList = transactionRepository.findAll();
//        Transaction testTransaction = transactionList.get(transactionList.size() - 1);
//
//        assertThat(simpleBillDTO.getId()).isEqualTo(testTransaction.getId());
//        assertThat(testTransaction.getSenderAmount()).isEqualTo(8746500L);
//        assertThat(testTransaction.getRecipientAmount()).isEqualTo(8746500L);
//        assertThat(testTransaction.getGatewayAmount()).isEqualTo(87465L);
//
//        PayRequest payRequest = new PayRequest();
//        payRequest.setAmount(payAmount);
//        payRequest.setBillId(simpleBillDTO.getId());
//        payRequest.setType(PayType.UZCARD);
//        payRequest.setParams(Map.of(Constants.TOKEN_PARAM, PredefinedCardRepo2.DEFAULT_TOKEN));
//
//        mvcResult = restAgentMockMvc.perform(
//                        post("/api/agent/pay/v3")
//                                .content(TestUtil.convertObjectToJsonBytes(payRequest))
//                                .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
//                )
//                .andExpect(status().isOk())
//                .andReturn();
//
//        assertThat(mvcResult.getResponse().getContentAsString()).isNotEmpty();
//
//        SimpleBillResponse payResponse = jacksonMessageConverter.getObjectMapper().readValue(mvcResult.getResponse().getContentAsByteArray(), SimpleBillResponse.class);
//
//        assertThat(payResponse).isNotNull();
//        assertThat(payResponse.isSuccess()).isEqualTo(payResult);
//
//        if (!payResult) {
//            return;
//        }
//
//        assertThat(payResponse.getAmount()).isEqualTo(8746500L); /* amount from shtraf-ok.xml  */
//        assertThat(payResponse.getAccount()).isEqualTo(simpleCheckDTO.getAccount());
//    }
//
//    @Transactional
//    public void syncPaynetAdapter() {
//
////        ArrayList<DataAdapterResponse> allAdapters2 = dataAdapterServiceV3.getAllAdapters();
//
////        assertThat(allAdapters2).isNotEmpty();
//
////        for (DataAdapterResponse dataAdapterResponse : allAdapters2) {
////            if (dataAdapterResponse.getName().equals(PaynetConstants.PAYNET_RPC_DATA_ADAPTER)) {
//        Result sync = dataAdapterServiceV3.sync(PaynetConstants.PAYNET_RPC_DATA_ADAPTER);
//        assertThat(sync).isNotNull();
//        assertThat(sync.isSuccess()).isTrue();
////            }
////        }
//
//        recipientRepository.findAll();
//    }
//
//    @Transactional
//    public void addAgentRecipient(String... recipientShortNames) throws Exception {
//
//        Agent agent = new Agent();
//        agent.setActive(true);
//        agent.setShortName("user");
//        agent.setCurrencies(getCurrencies());
//        agent.setAmountRatio(100);
//        agent.setApiKey("bomBom123");
//        agent = agentRepository.save(agent);
//
//        Deposit deposit = new Deposit();
//        deposit.setBalance(10000000L);
//        deposit.setAgent(agent);
//        deposit.setBalanceCcy(860);
//        deposit.setBlocked(false);
//        deposit.setLimitInform(0L);
//        deposit.setLimitDisable(0L);
//        deposit.setUnlimit(false);
//        deposit.setOverdraftLimit(0L);
//        deposit.setBookedBalance(0L);
//        deposit = depositRepository.save(deposit);
//
//        agent.setDeposit(deposit);
//        agentRepository.save(agent);
//
//        for (String s : recipientShortNames) {
//            Recipient recipient = recipientRepository.findTop1ByShortName(s).orElseThrow(() -> new Exception("recipient not found"));
//
//            recipient.setActive(true);
//            recipient.setCheck(true);
//            recipient.setPay(true);
//
//            recipientRepository.save(recipient);
//
//            AgentRecipient agentRecipient = new AgentRecipient();
//            agentRecipient.setAgent(agent);
//            agentRecipient.setRecipient(recipient);
//            agentRecipient.setActive(true);
//            agentRecipientRepository.save(agentRecipient);
//        }
//
////        depositService.increaseAgentDeposit("user", 10000000L);
//    }
//
//    private Set<Integer> getCurrencies() {
//        Set<Integer> integers = new HashSet<>();
//        integers.add(860);
//        return integers;
//    }
//
//
//    private CheckRequest getSimpleCheckDTO() {
//        CheckRequest commonCheckDTO = new CheckRequest();
//
//        commonCheckDTO.setAccount(DUMMY_ACCOUNT);
//        commonCheckDTO.setAmount(DUMMY_AMOUNT);
//        commonCheckDTO.setCurrency(860);
//        commonCheckDTO.setAgentTranId(TRAN_ID);
//
//        return commonCheckDTO;
//    }
//
//    private void initResponseStub(String resourceFilePath) {
//        initResponseStub(resourceFilePath, ExpectedCount.between(1, 2));
//    }
//
//    private void initResponseStub(String resourceFilePath, ExpectedCount expectedCount) {
//        Resource shtrafResponseResource = new ClassPathResource(resourceFilePath);
//
//        mockServer
//                .expect(
//                        expectedCount,
//                        MockRestRequestMatchers.requestTo(Matchers.allOf(
//                                Matchers.matchesPattern(paynetProperties.getBaseUrl() + ".*"),
//                                Matchers.matchesPattern(".*TERMINAL_ID.*"),
//                                Matchers.matchesPattern(".*USERNAME.*"),
//                                Matchers.matchesPattern(".*ACT=2.*"),
//                                Matchers.matchesPattern(".*SERVICE_ID.*"),
//                                Matchers.matchesPattern(".*PASSWORD.*")
//                        ))
//                )
//                .andExpect(MockRestRequestMatchers.method(HttpMethod.GET))
//                .andRespond(MockRestResponseCreators.withStatus(HttpStatus.OK)
//                        .contentType(MediaType.APPLICATION_XML)
//                        .body(shtrafResponseResource)
//                );
//    }
//}
