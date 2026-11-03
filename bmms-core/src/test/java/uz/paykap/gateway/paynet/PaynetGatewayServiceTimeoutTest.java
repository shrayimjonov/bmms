package uz.paykap.gateway.paynet;

import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.converter.ResourceHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.client.ExpectedCount;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.client.match.MockRestRequestMatchers;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import uz.devops.bm.dummy.adapter.DummyDataAdapterV3;
import uz.devops.bmcore.dto.request.CheckRequest;
import uz.devops.bmcore.dto.request.CommonCheckRequest;
import uz.devops.bmcore.dto.request.CommonPayRequest;
import uz.devops.bmcore.dto.request.PayRequest;
import uz.devops.bmcore.dto.response.SimpleBillResponse;
import uz.devops.bmcore.enumeration.RequestType;
import uz.devops.bmcore.util.SharedContentStoreService;
import uz.javlon.paynet.config.PaynetConfiguration;
import uz.paykap.PkbillmsApp;
import uz.paykap.adapter.DataAdapterServiceV3;
import uz.paykap.agent.tran.CommonTranServiceV3;
import uz.paykap.config.Constants;
import uz.paykap.config.DynamicGatewayServiceConfig;
import uz.paykap.config.SecurityBeanOverrideConfiguration;
import uz.paykap.domain.*;
import uz.paykap.domain.enumeration.MethodType;
import uz.paykap.gateway.common.CommonGatewayServiceV3;
import uz.paykap.repository.*;
import uz.paykap.service.AgentRecipientService;
import uz.paykap.service.AgentService;
import uz.paykap.service.DepositService;
import uz.paykap.service.GroupSettingService;
import uz.paykap.service.dto.AgentDTO;
import uz.paykap.web.rest.*;
import uz.paykap.web.rest.errors.ExceptionTranslator;
import uz.paykap.web.rest.util.DummyUtils;

import javax.persistence.EntityManager;
import java.net.SocketTimeoutException;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uz.paykap.web.rest.TestUtil.createFormattingConversionService;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {PkbillmsApp.class, SecurityBeanOverrideConfiguration.class}, properties = {"paynet.simulation=false"})
public class PaynetGatewayServiceTimeoutTest {

    public static final String DUMMY_ACCOUNT = "974415655";
    public static final long DUMMY_AMOUNT = 3000000L;
    public static final String TRAN_ID = "TRAN_ID";
    public static final String TEST_ACCOUNT = "22222222222222222222";

    @Autowired
    @Qualifier("paynetRestTemplate")
    private RestTemplate restTemplate;

    private MockRestServiceServer mockServer;

    @Autowired
    private GroupSettingService groupSettingService;
    @Autowired
    private DepositService depositService;
    @Autowired
    private RecipientRepository recipientRepository;
    @Autowired
    private CommonTranServiceV3 commonTranServiceV3;
    @Autowired
    private AgentRecipientService agentRecipientService;
    @Autowired
    private TransactionRepository transactionRepository;
    @Autowired
    private MappingJackson2HttpMessageConverter jacksonMessageConverter;
    @Autowired
    private PageableHandlerMethodArgumentResolver pageableArgumentResolver;
    @Autowired
    private ExceptionTranslator exceptionTranslator;
    @Autowired
    private DataAdapterServiceV3 dataAdapterServiceV3;
    @Autowired
    private SharedContentStoreService sharedContentStoreService;

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
    public void testPay() throws Exception {

        mockServer = MockRestServiceServer.createServer(restTemplate);

        int databaseSizeBeforeCreate = transactionRepository.findAll().size();

        CheckRequest simpleCheckDTO = getSimpleCheckDTO();

        SimpleBillResponse simpleBillDTO = getSimpleBillDTO(simpleCheckDTO);

        assertThat(simpleBillDTO).isNotNull();
        assertThat(simpleBillDTO.isSuccess()).isTrue();
        assertThat(simpleBillDTO.getAmount()).isEqualTo(simpleCheckDTO.getAmount());
        assertThat(simpleBillDTO.getAccount()).isEqualTo(simpleCheckDTO.getAccount());

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
//        payDTO.setAmount(DUMMY_AMOUNT);
        payDTO.setGatewayAmount(30000.03);
        payDTO.setBillId(simpleBillDTO.getId());

        MvcResult mvcResult = restAgentMockMvc.perform(
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
        assertThat(simpleBillDTO.getAccount()).isEqualTo(DUMMY_ACCOUNT);

        List<Transaction> transactionList = transactionRepository.findAll();
        assertThat(transactionList).hasSize(databaseSizeBeforeCreate + 1);
        Transaction testTransaction = transactionList.get(transactionList.size() - 1);

//        testPayTransaction(testTransaction, instant, simpleCheckDTO, DummyDataAdapterV3.DUMMY_AGENT);
    }

    private CheckRequest getSimpleCheckDTO() {
        return getSimpleCheckDTO(DUMMY_AMOUNT);
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
        DummyUtils.prepareDummyData(dataAdapterServiceV3, groupSettingService, recipientRepository, agentRecipientService);
    }

}
