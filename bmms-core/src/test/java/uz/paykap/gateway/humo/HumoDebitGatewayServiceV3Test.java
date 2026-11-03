package uz.paykap.gateway.humo;

import org.junit.Before;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import uz.paykap.IntegrationTest;
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
import uz.devops.bmcore.dto.request.CheckRequest;
import uz.devops.bmcore.dto.request.CommonPayRequest;
import uz.devops.bmcore.dto.request.PayRequest;
import uz.devops.bmcore.dto.response.SimpleBillResponse;
import uz.devops.bmcore.enumeration.PayType;
import uz.devops.bmcore.util.SharedContentStoreService;
import uz.javlon.commons.base.Result;
import uz.paykap.IntegrationTest;
import uz.paykap.PkbillmsApp;
import uz.paykap.adapter.DataAdapterResponse;
import uz.paykap.adapter.DataAdapterServiceV3;
import uz.paykap.adapter.humo.HumoDebitDataAdapterV3;
import uz.paykap.agent.tran.CommonTranServiceV3;
import uz.paykap.domain.*;
import uz.paykap.repository.*;
import uz.paykap.service.ForeignPayService;
import uz.paykap.web.rest.SharedAgentResourceV3;
import uz.paykap.web.rest.TestUtil;
import uz.paykap.web.rest.errors.ExceptionTranslator;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uz.paykap.web.rest.TestUtil.createFormattingConversionService;

@IntegrationTest
@ExtendWith(MockitoExtension.class)
@AutoConfigureMockMvc
@WithMockUser
public class HumoDebitGatewayServiceV3Test {


    public static final long TEST_AMOUNT = 100000L;
    public static final String TEST_ACCOUNT = "9860090000000000";
    @Autowired
    private CommonTranServiceV3 commonTranServiceV3;

    @Autowired
    private ForeignPayService foreignPayService;

    @Autowired
    private SharedContentStoreService sharedContentStoreService;

    @Autowired
    private PageableArgumentResolver pageableArgumentResolver;

    @Autowired
    private ExceptionTranslator exceptionTranslator;

    @Autowired
    private MappingJackson2HttpMessageConverter jacksonMessageConverter;

    @Autowired
    private DataAdapterServiceV3 dataAdapterServiceV3;

    @Autowired
    private RecipientRepository recipientRepository;

    @Autowired
    private AgentRepository agentRepository;

    @Autowired
    private DepositRepository depositRepository;

    @Autowired
    private AgentRecipientRepository agentRecipientRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    private MockMvc restAgentMockMvc;

    private Long billId = 0L;

    @BeforeEach
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
    public void synchHumoDebitAdapter(){
        ArrayList<DataAdapterResponse> allAdapters2 = dataAdapterServiceV3.getAllAdapters();

        assertThat(allAdapters2).isNotEmpty();

        for (DataAdapterResponse dataAdapterResponse : allAdapters2) {
            if (dataAdapterResponse.getName().equals(HumoDebitDataAdapterV3.ADAPTER_NAME)) {
                Result sync = dataAdapterServiceV3.sync(dataAdapterResponse.getName());
                assertThat(sync).isNotNull();
                assertThat(sync.isSuccess()).isTrue();
            }
        }

        recipientRepository.findAll();
    }

    @Transactional
    public void addAgentRecipient() throws Exception {

        Recipient recipient = recipientRepository.findTop1ByShortName(HumoDebitDataAdapterV3.HUMO_DEBIT_RECIPIENT_SHORT_NAME).orElseThrow(() -> new Exception("recipient not found"));

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

        AgentRecipient agentRecipient = new AgentRecipient();
        agentRecipient.setAgent(agent);
        agentRecipient.setRecipient(recipient);
        agentRecipient.setActive(true);
        agentRecipient.setEposMerchant("444444");
        agentRecipient.setEposTerminal("555555");
        agentRecipientRepository.save(agentRecipient);


//        depositService.increaseAgentDeposit("user", 10000000L);
    }


    @Test
    @Transactional
    @WithMockUser("user")
    public void testCheckToSuccess() throws Exception {
        synchHumoDebitAdapter();
        addAgentRecipient();

        Optional<Recipient> optionalRecipient = recipientRepository.findTop1ByShortName(HumoDebitDataAdapterV3.HUMO_DEBIT_RECIPIENT_SHORT_NAME);
        assertThat(optionalRecipient.isPresent()).isTrue();

        Long recipientId = optionalRecipient.get().getId();

        CheckRequest simpleCheckDTO = getSimpleCheckDTO();
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
        payDTO.setType(PayType.AGENT);
        if (payDTO.getParams() == null)
            payDTO.setParams(new HashMap<>());

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
        assertThat(simpleBillDTO.getAmount()).isEqualTo(TEST_AMOUNT);
        assertThat(simpleBillDTO.getAccount()).isEqualTo(TEST_ACCOUNT);
    }

    private CheckRequest getSimpleCheckDTO() {
        CheckRequest commonCheckDTO = new CheckRequest();

        commonCheckDTO.setAccount(TEST_ACCOUNT);
        commonCheckDTO.setAmount(TEST_AMOUNT);
        commonCheckDTO.setCurrency(860);
        commonCheckDTO.setAgentTranId("-" + System.currentTimeMillis());

        Map<String, Object> params = new HashMap<>();
        params.put(HumoDebitDataAdapterV3.EXPIRY_FIELD_NAME, "2502");
        commonCheckDTO.setParams(params);
        return commonCheckDTO;
    }

    private Set<Integer> getCurrencies() {
        Set<Integer> integers = new HashSet<>();
        integers.add(860);
        return integers;
    }
}
