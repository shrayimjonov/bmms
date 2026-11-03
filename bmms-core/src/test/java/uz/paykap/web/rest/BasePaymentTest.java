package uz.paykap.web.rest;

import lombok.extern.slf4j.Slf4j;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.web.PageableArgumentResolver;
import org.springframework.http.converter.ResourceHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import uz.devops.bmcore.dto.request.CheckRequest;
import uz.devops.bmcore.util.SharedContentStoreService;
import uz.javlon.commons.base.Result;
import uz.javlon.svgate.domain.Bank;
import uz.javlon.svgate.repository.BankRepository;
import uz.paykap.adapter.DataAdapterResponse;
import uz.paykap.adapter.DataAdapterServiceV3;
import uz.paykap.agent.tran.CommonTranServiceV3;
import uz.paykap.domain.Agent;
import uz.paykap.domain.AgentRecipient;
import uz.paykap.domain.Deposit;
import uz.paykap.domain.Recipient;
import uz.paykap.repository.*;
import uz.paykap.web.rest.errors.ExceptionTranslator;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static uz.paykap.web.rest.TestUtil.createFormattingConversionService;

@Slf4j
public class BasePaymentTest {

    public static final String DUMMY_ACCOUNT = "DUMMY_ACCOUNT";
    public static final String TRAN_ID = "TRAN_ID";
    public static final Long DUMMY_AMOUNT = 50100L;
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
    protected MappingJackson2HttpMessageConverter jacksonMessageConverter;
    @Autowired
    protected TransactionRepository transactionRepository;
    @Autowired
    private BankRepository bankRepository;

    protected MockMvc restAgentMockMvc;

    protected void initSharedAgentResource() {
        MockitoAnnotations.initMocks(this);
        final SharedAgentResourceV3 adapterResource = new SharedAgentResourceV3(commonTranServiceV3, sharedContentStoreService);
        this.restAgentMockMvc = MockMvcBuilders.standaloneSetup(adapterResource)
                .setCustomArgumentResolvers(pageableArgumentResolver)
                .setControllerAdvice(exceptionTranslator)
                .setConversionService(createFormattingConversionService())
                .setMessageConverters(jacksonMessageConverter, new ResourceHttpMessageConverter()).build();
    }

    @Transactional
    public void syncAdapter(String adapterName) {

        ArrayList<DataAdapterResponse> adapters = dataAdapterServiceV3.getAllAdapters();

        assertThat(adapters).isNotEmpty();

        for (DataAdapterResponse dataAdapterResponse : adapters) {
            if (dataAdapterResponse.getName().equals(adapterName)) {
                Result sync = dataAdapterServiceV3.sync(dataAdapterResponse.getName());
                assertThat(sync).isNotNull();
                assertThat(sync.isSuccess()).isTrue();
            }
        }

        final List<Recipient> recipients = recipientRepository.findAll();

        log.info("Recipients imported : {}", recipients.size());
    }

    protected void addAgentRecipient(String agentName, String recipientShortName) throws Exception {
        addAgentRecipient(agentName, List.of(recipientShortName));
    }

    @Transactional
    protected void addAgentRecipient(String agentName, Collection<String> recipientShortNames) throws Exception {

        Agent agent = new Agent();
        agent.setActive(true);
        agent.setShortName(agentName);
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

    protected Set<Integer> getCurrencies() {
        Set<Integer> integers = new HashSet<>();
        integers.add(860);
        return integers;
    }

    protected void prepareBank(String cardBin) {
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


    protected CheckRequest getSimpleCheckDTO() {
        CheckRequest commonCheckDTO = new CheckRequest();

        commonCheckDTO.setAccount(DUMMY_ACCOUNT);
        commonCheckDTO.setAmount(DUMMY_AMOUNT);
        commonCheckDTO.setCurrency(860);
        commonCheckDTO.setAgentTranId(TRAN_ID);

        return commonCheckDTO;
    }
}
