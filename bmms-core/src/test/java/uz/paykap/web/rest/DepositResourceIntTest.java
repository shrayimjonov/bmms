package uz.paykap.web.rest;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.Validator;
import uz.devops.bm.dummy.adapter.DummyDataAdapterV3;
import uz.devops.bmcore.Constants;
import uz.javlon.commons.base.Result;
import uz.javlon.commons.exception.CodedException;
import uz.paykap.IntegrationTest;
import uz.paykap.adapter.DummyAdapterDelegate;
import uz.paykap.domain.*;
import uz.paykap.repository.AgentRepository;
import uz.paykap.repository.DepositRepository;
import uz.paykap.repository.DepositTransactionRepository;
import uz.paykap.repository.GatewayRepository;
import uz.paykap.service.DepositQueryService;
import uz.paykap.service.DepositService;
import uz.paykap.service.dto.DepositDTO;
import uz.paykap.service.dto.ReplenishDepositDTO;
import uz.paykap.service.mapper.DepositMapper;
import uz.paykap.web.rest.errors.ExceptionTranslator;
import uz.paykap.web.rest.util.DummyUtils;

import javax.persistence.EntityManager;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static uz.paykap.web.rest.TestUtil.createFormattingConversionService;

/**
 * Test class for the DepositResource REST controller.
 *
 * @see DepositResource
 */
@RunWith(SpringRunner.class)
@IntegrationTest
public class DepositResourceIntTest {

    private static final Long DEFAULT_OVERDRAFT_LIMIT = 1L;
    private static final Long UPDATED_OVERDRAFT_LIMIT = 2L;

    private static final Long DEFAULT_BALANCE = 1L;
    private static final Long UPDATED_BALANCE = 2L;

    private static final Integer DEFAULT_BALANCE_CCY = 1;
    private static final Integer UPDATED_BALANCE_CCY = 2;

    private static final Long DEFAULT_BOOKED_BALANCE = 1L;
    private static final Long UPDATED_BOOKED_BALANCE = 2L;

    private static final Long DEFAULT_LIMIT_INFORM = 1L;
    private static final Long UPDATED_LIMIT_INFORM = 2L;

    private static final Long DEFAULT_LIMIT_DISABLE = 1L;
    private static final Long UPDATED_LIMIT_DISABLE = 2L;

    private static final String DEFAULT_CONTACT_GROUP = "AAAAAAAAAA";
    private static final String UPDATED_CONTACT_GROUP = "BBBBBBBBBB";

    private static final Boolean DEFAULT_BLOCKED = false;
    private static final Boolean UPDATED_BLOCKED = true;

    private static final Boolean DEFAULT_UNLIMIT = false;
    private static final Boolean UPDATED_UNLIMIT = true;
    public static final long REPLENISH_OVERDRAFT_LIMIT = 123456L;
    public static final long REPLENISH_BALANCE = 234567L;
    public static final Integer REPLENISH_BALANCE_CURRENCY_UZS = 860;
    public static final Integer REPLENISH_BALANCE_CURRENCY_USD = 840;

    @Autowired
    private DummyAdapterDelegate dummyAdapterDelegate;

    @Autowired
    private DepositRepository depositRepository;

    @Autowired
    private DepositTransactionRepository depositTransactionRepository;

    @Autowired
    private DepositMapper depositMapper;

    @Autowired
    private DepositService depositService;

    @Autowired
    private DepositQueryService depositQueryService;

    @Autowired
    private MappingJackson2HttpMessageConverter jacksonMessageConverter;

    @Autowired
    private PageableHandlerMethodArgumentResolver pageableArgumentResolver;

    @Autowired
    private ExceptionTranslator exceptionTranslator;

    @Autowired
    private GatewayRepository gatewayRepository;

    @Autowired
    private AgentRepository agentRepository;

    @Autowired
    private EntityManager em;

    @Autowired
    private Validator validator;

    private MockMvc restDepositMockMvc;

    private Deposit deposit;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        final DepositResource depositResource = new DepositResource(
            depositService,
            depositQueryService,
            gatewayRepository,
            agentRepository,
            depositRepository
        );
        this.restDepositMockMvc =
            MockMvcBuilders
                .standaloneSetup(depositResource)
                .setCustomArgumentResolvers(pageableArgumentResolver)
                .setControllerAdvice(exceptionTranslator)
                .setConversionService(createFormattingConversionService())
                .setMessageConverters(jacksonMessageConverter)
                .setValidator(validator)
                .build();
    }

    /**
     * Create an entity for this test.
     * <p>
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Deposit createEntity(EntityManager em) {
        Deposit deposit = new Deposit()
            .overdraftLimit(DEFAULT_OVERDRAFT_LIMIT)
            .balance(DEFAULT_BALANCE)
            .balanceCcy(DEFAULT_BALANCE_CCY)
            .bookedBalance(DEFAULT_BOOKED_BALANCE)
            .limitInform(DEFAULT_LIMIT_INFORM)
            .limitDisable(DEFAULT_LIMIT_DISABLE)
            .contactGroup(DEFAULT_CONTACT_GROUP)
            .blocked(DEFAULT_BLOCKED)
            .unlimit(DEFAULT_UNLIMIT);
        return deposit;
    }

    @Before
    public void initTest() {
        deposit = createEntity(em);
    }

    @Test
    @Transactional
    public void createDeposit() throws Exception {
        int databaseSizeBeforeCreate = depositRepository.findAll().size();

        // Create the Deposit
        DepositDTO depositDTO = depositMapper.toDto(deposit);
        restDepositMockMvc
            .perform(
                post("/api/deposits").contentType(TestUtil.APPLICATION_JSON_UTF8).content(TestUtil.convertObjectToJsonBytes(depositDTO))
            )
            .andExpect(status().isCreated());

        // Validate the Deposit in the database
        List<Deposit> depositList = depositRepository.findAll();
        assertThat(depositList).hasSize(databaseSizeBeforeCreate + 1);
        Deposit testDeposit = depositList.get(depositList.size() - 1);
        assertThat(testDeposit.getOverdraftLimit()).isEqualTo(DEFAULT_OVERDRAFT_LIMIT);
        assertThat(testDeposit.getBalance()).isEqualTo(DEFAULT_BALANCE);
        assertThat(testDeposit.getBalanceCcy()).isEqualTo(DEFAULT_BALANCE_CCY);
        assertThat(testDeposit.getBookedBalance()).isEqualTo(DEFAULT_BOOKED_BALANCE);
        assertThat(testDeposit.getLimitInform()).isEqualTo(DEFAULT_LIMIT_INFORM);
        assertThat(testDeposit.getLimitDisable()).isEqualTo(DEFAULT_LIMIT_DISABLE);
        assertThat(testDeposit.getContactGroup()).isEqualTo(DEFAULT_CONTACT_GROUP);
        assertThat(testDeposit.isBlocked()).isEqualTo(DEFAULT_BLOCKED);
        assertThat(testDeposit.isUnlimit()).isEqualTo(DEFAULT_UNLIMIT);
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    @Transactional
    public void testEmmitDeposit() throws Exception {
        DummyUtils.prepareDummyData(dummyAdapterDelegate);


        // Create the Deposit
        ReplenishDepositDTO replenishDepositDTO = new ReplenishDepositDTO();
        //        replenishDepositDTO.setOverdraftLimit(REPLENISH_OVERDRAFT_LIMIT);
        replenishDepositDTO.setBalanceAmount(REPLENISH_BALANCE);
        replenishDepositDTO.setAgentLogin(DummyDataAdapterV3.DUMMY_AGENT);
        replenishDepositDTO.setDoc("test-doc");
        //        replenishDepositDTO.setPayerTaxnr("1234");
        //        replenishDepositDTO.setRecipientTaxnr("2345");
        //        replenishDepositDTO.setRecipientTaxnr(2345L);

        Optional<Deposit> depositOptional = depositRepository.findTop1ByAgent_ShortName(DummyDataAdapterV3.DUMMY_AGENT);
        assertThat(depositOptional.isPresent()).isTrue();

        Long beforeBalance = depositOptional.get().getBalance();
        int databaseSizeBeforeCreate = depositTransactionRepository.findAll().size();

        restDepositMockMvc
            .perform(
                post("/api/deposits/replenish")
                    .contentType(TestUtil.APPLICATION_JSON_UTF8)
                    .content(TestUtil.convertObjectToJsonBytes(replenishDepositDTO))
            )
            .andExpect(status().isOk());

        // Validate the Deposit in the database

        depositOptional = depositRepository.findTop1ByAgent_ShortName(DummyDataAdapterV3.DUMMY_AGENT);
        assertThat(depositOptional.isPresent()).isTrue();

        Deposit testDeposit = depositOptional.get();
        assertThat(testDeposit.getAgent()).isNotNull();
        assertThat(testDeposit.getAgent().getShortName()).isEqualTo(DummyDataAdapterV3.DUMMY_AGENT);
        //        assertThat(testDeposit.getOverdraftLimit()).isEqualTo(REPLENISH_OVERDRAFT_LIMIT);
        assertThat(testDeposit.getBalance()).isEqualTo(REPLENISH_BALANCE + beforeBalance);
        assertThat(testDeposit.getBalanceCcy()).isEqualTo(860);

        List<DepositTransaction> depositTransactionList = depositTransactionRepository.findAll();
        assertThat(depositTransactionList).hasSize(databaseSizeBeforeCreate + 1);
        DepositTransaction testDepositTransaction = depositTransactionList.get(depositTransactionList.size() - 1);
        assertThat(testDepositTransaction.getPayerTaxnr()).isEqualTo(Constants.SYSTEM_INN);
        assertThat(testDepositTransaction.getRecipientTaxnr()).isEqualTo(DummyDataAdapterV3.INN);
        assertThat(testDepositTransaction.getAmount()).isEqualTo(replenishDepositDTO.getBalanceAmount());
        assertThat(testDepositTransaction.getUpdateDate()).isNotNull();
        assertThat(testDepositTransaction.getComment()).isNotEmpty();
        assertThat(testDepositTransaction.getComment()).contains("860");
        assertThat(testDepositTransaction.getComment()).contains("UZS");
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    @Transactional
    public void testEmmitDepositUsd() throws Exception {
        DummyUtils.prepareDummyData(dummyAdapterDelegate);


        // Create the Deposit
        ReplenishDepositDTO replenishDepositDTO = new ReplenishDepositDTO();
        //        replenishDepositDTO.setOverdraftLimit(REPLENISH_OVERDRAFT_LIMIT);
        replenishDepositDTO.setBalanceAmount(REPLENISH_BALANCE);
        replenishDepositDTO.setBalanceCcy(REPLENISH_BALANCE_CURRENCY_USD);
        replenishDepositDTO.setAgentLogin(DummyDataAdapterV3.DUMMY_AGENT_USD);
        replenishDepositDTO.setDoc("test-doc");
        //        replenishDepositDTO.setPayerTaxnr("1234");
        //        replenishDepositDTO.setRecipientTaxnr("2345");
        //        replenishDepositDTO.setRecipientTaxnr(2345L);

        Optional<Deposit> depositOptional = depositRepository.findTop1ByAgent_ShortNameAndBalanceCcy(
            DummyDataAdapterV3.DUMMY_AGENT_USD,
            REPLENISH_BALANCE_CURRENCY_USD
        );
        assertThat(depositOptional.isPresent()).isTrue();

        Long beforeBalance = depositOptional.get().getBalance();
        int databaseSizeBeforeCreate = depositTransactionRepository.findAll().size();

        restDepositMockMvc
            .perform(
                post("/api/deposits/replenish")
                    .contentType(TestUtil.APPLICATION_JSON_UTF8)
                    .content(TestUtil.convertObjectToJsonBytes(replenishDepositDTO))
            )
            .andExpect(status().isOk());

        // Validate the Deposit in the database

        depositOptional =
            depositRepository.findTop1ByAgent_ShortNameAndBalanceCcy(DummyDataAdapterV3.DUMMY_AGENT_USD, REPLENISH_BALANCE_CURRENCY_USD);
        assertThat(depositOptional.isPresent()).isTrue();

        Deposit testDeposit = depositOptional.get();
        assertThat(testDeposit.getAgent()).isNotNull();
        assertThat(testDeposit.getAgent().getShortName()).isEqualTo(DummyDataAdapterV3.DUMMY_AGENT_USD);
        //        assertThat(testDeposit.getOverdraftLimit()).isEqualTo(REPLENISH_OVERDRAFT_LIMIT);
        assertThat(testDeposit.getBalance()).isEqualTo(REPLENISH_BALANCE + beforeBalance);
        assertThat(testDeposit.getBalanceCcy()).isEqualTo(840);

        List<DepositTransaction> depositTransactionList = depositTransactionRepository.findAll();
        assertThat(depositTransactionList).hasSize(databaseSizeBeforeCreate + 1);
        DepositTransaction testDepositTransaction = depositTransactionList.get(depositTransactionList.size() - 1);
        assertThat(testDepositTransaction.getPayerTaxnr()).isEqualTo(Constants.SYSTEM_INN);
        assertThat(testDepositTransaction.getRecipientTaxnr()).isEqualTo(DummyDataAdapterV3.INN);
        assertThat(testDepositTransaction.getAmount()).isEqualTo(replenishDepositDTO.getBalanceAmount());
        assertThat(testDepositTransaction.getUpdateDate()).isNotNull();
        assertThat(testDepositTransaction.getComment()).isNotEmpty();
        assertThat(testDepositTransaction.getComment()).contains("840");
        assertThat(testDepositTransaction.getComment()).contains("USD");
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    @Transactional
    public void testUnlimitedDeposit() {
        DummyUtils.prepareDummyData(dummyAdapterDelegate);


        Optional<Deposit> depositOptional = depositRepository.findTop1ByAgent_ShortName(DummyDataAdapterV3.DUMMY_AGENT);
        assertThat(depositOptional.isPresent()).isTrue();

        depositRepository.save(depositOptional.get().unlimit(true));

        Long beforeBalance = depositOptional.get().getBalance();

        boolean canAgentProceedPayment = depositService.canAgentProceedPayment(DummyDataAdapterV3.DUMMY_AGENT, beforeBalance * 10);

        assertThat(canAgentProceedPayment).isTrue();

        // Validate the Deposit in the database

        depositOptional = depositRepository.findTop1ByAgent_ShortName(DummyDataAdapterV3.DUMMY_AGENT);
        assertThat(depositOptional.isPresent()).isTrue();
        //        Deposit testDeposit = depositOptional.get();
        //        assertThat(testDeposit.getAgent()).isNotNull();
        //        assertThat(testDeposit.getAgent().getShortName()).isEqualTo(DummyDataAdapterV3.DUMMY_AGENT);
        //        assertThat(testDeposit.getBalance()).isEqualTo(REPLENISH_BALANCE + beforeBalance);
        //        assertThat(testDeposit.getBalanceCcy()).isEqualTo(860);

    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    @Transactional
    public void testCanNotProceed() {
        DummyUtils.prepareDummyData(dummyAdapterDelegate);


        Optional<Deposit> depositOptional = depositRepository.findTop1ByAgent_ShortName(DummyDataAdapterV3.DUMMY_AGENT);
        assertThat(depositOptional.isPresent()).isTrue();

        depositRepository.save(depositOptional.get().blocked(false).unlimit(false).overdraftLimit(0L).limitDisable(0L));

        Long beforeBalance = depositOptional.get().getBalance();

        boolean canAgentProceedPayment = depositService.canAgentProceedPayment(DummyDataAdapterV3.DUMMY_AGENT, beforeBalance + 1);

        assertThat(canAgentProceedPayment).isFalse();

        Result result = depositService.decreaseAgentDeposit(DummyDataAdapterV3.DUMMY_AGENT, beforeBalance + 1);

        assertThat(result).isNotNull();
        assertThat(result.isSuccess()).isFalse();
        // Validate the Deposit in the database

        //        depositOptional = depositRepository.findTop1ByAgent_ShortName(DummyDataAdapterV3.DUMMY_AGENT);
        //        assertThat(depositOptional.isPresent()).isTrue();

        //        Deposit testDeposit = depositOptional.get();
        //        assertThat(testDeposit.getAgent()).isNotNull();
        //        assertThat(testDeposit.getAgent().getShortName()).isEqualTo(DummyDataAdapterV3.DUMMY_AGENT);
        //        assertThat(testDeposit.getBalance()).isEqualTo(REPLENISH_BALANCE + beforeBalance);
        //        assertThat(testDeposit.getBalanceCcy()).isEqualTo(860);

    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    @Transactional
    public void testCanProceedPaymentWithoutOverdraft() {
        DummyUtils.prepareDummyData(dummyAdapterDelegate);


        Optional<Deposit> depositOptional = depositRepository.findTop1ByAgent_ShortName(DummyDataAdapterV3.DUMMY_AGENT);
        assertThat(depositOptional.isPresent()).isTrue();

        depositRepository.save(depositOptional.get().blocked(false).unlimit(false).overdraftLimit(0L).limitDisable(0L));

        Long beforeBalance = depositOptional.get().getBalance();

        boolean canAgentProceedPayment = depositService.canAgentProceedPayment(DummyDataAdapterV3.DUMMY_AGENT, beforeBalance - 1);

        assertThat(canAgentProceedPayment).isTrue();

        Result result = depositService.decreaseAgentDeposit(DummyDataAdapterV3.DUMMY_AGENT, beforeBalance - 1);

        assertThat(result).isNotNull();
        assertThat(result.isSuccess()).isTrue();

        // Validate the Deposit in the database

        depositOptional = depositRepository.findTop1ByAgent_ShortName(DummyDataAdapterV3.DUMMY_AGENT);
        assertThat(depositOptional.isPresent()).isTrue();

        Deposit testDeposit = depositOptional.get();
        assertThat(testDeposit.getAgent()).isNotNull();
        //        assertThat(testDeposit.getAgent().getShortName()).isEqualTo(DummyDataAdapterV3.DUMMY_AGENT);
        assertThat(testDeposit.getBalance()).isEqualTo(1);
        assertThat(testDeposit.getBalanceCcy()).isEqualTo(860);
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    @Transactional
    public void testCanProceedPaymentWithOverdraft() {
        DummyUtils.prepareDummyData(dummyAdapterDelegate);


        Optional<Deposit> depositOptional = depositRepository.findTop1ByAgent_ShortName(DummyDataAdapterV3.DUMMY_AGENT);
        assertThat(depositOptional.isPresent()).isTrue();

        long overdraftLimit = -10L;

        depositRepository.save(
            depositOptional.get().blocked(false).unlimit(false).overdraftLimit(overdraftLimit).limitDisable(overdraftLimit)
        );

        Long beforeBalance = depositOptional.get().getBalance();

        long paymentAmountInCcyInCents = beforeBalance + ((-1 * overdraftLimit) - 1);

        boolean canAgentProceedPayment = depositService.canAgentProceedPayment(DummyDataAdapterV3.DUMMY_AGENT, paymentAmountInCcyInCents);

        assertThat(canAgentProceedPayment).isTrue();

        Result result = depositService.decreaseAgentDeposit(DummyDataAdapterV3.DUMMY_AGENT, paymentAmountInCcyInCents);

        assertThat(result).isNotNull();
        assertThat(result.isSuccess()).isTrue();

        // Validate the Deposit in the database

        depositOptional = depositRepository.findTop1ByAgent_ShortName(DummyDataAdapterV3.DUMMY_AGENT);
        assertThat(depositOptional.isPresent()).isTrue();

        Deposit testDeposit = depositOptional.get();
        assertThat(testDeposit.getAgent()).isNotNull();
        //        assertThat(testDeposit.getAgent().getShortName()).isEqualTo(DummyDataAdapterV3.DUMMY_AGENT);
        assertThat(testDeposit.getBalance()).isEqualTo(beforeBalance - paymentAmountInCcyInCents);
        assertThat(testDeposit.getBalanceCcy()).isEqualTo(860);
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    @Transactional
    public void testCannotProceedPaymentWithLimitDisabledGreaterDeposit() {
        DummyUtils.prepareDummyData(dummyAdapterDelegate);


        Optional<Deposit> depositOptional = depositRepository.findTop1ByAgent_ShortName(DummyDataAdapterV3.DUMMY_AGENT);
        assertThat(depositOptional.isPresent()).isTrue();

        long overdraftLimit = -10L;
        long limitDisable = -5L;

        depositRepository.save(
            depositOptional.get().blocked(false).unlimit(false).overdraftLimit(overdraftLimit).limitDisable(limitDisable)
        );

        Long beforeBalance = depositOptional.get().getBalance();

        long paymentAmountInCcyInCents = beforeBalance + ((-1 * overdraftLimit) - 1);

        boolean canAgentProceedPayment = depositService.canAgentProceedPayment(DummyDataAdapterV3.DUMMY_AGENT, paymentAmountInCcyInCents);

        assertThat(canAgentProceedPayment).isFalse();

        Result result = depositService.decreaseAgentDeposit(DummyDataAdapterV3.DUMMY_AGENT, paymentAmountInCcyInCents);

        assertThat(result).isNotNull();
        assertThat(result.isSuccess()).isFalse();

        // Validate the Deposit in the database

        depositOptional = depositRepository.findTop1ByAgent_ShortName(DummyDataAdapterV3.DUMMY_AGENT);
        assertThat(depositOptional.isPresent()).isTrue();

        Deposit testDeposit = depositOptional.get();
        assertThat(testDeposit.getAgent()).isNotNull();
        //        assertThat(testDeposit.getAgent().getShortName()).isEqualTo(DummyDataAdapterV3.DUMMY_AGENT);
        assertThat(testDeposit.getBalance()).isEqualTo(beforeBalance);
        assertThat(testDeposit.getBalanceCcy()).isEqualTo(860);
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    @Transactional
    public void testCanProceedPaymentWithLimitDisabledGreaterDeposit() {
        DummyUtils.prepareDummyData(dummyAdapterDelegate);


        Optional<Deposit> depositOptional = depositRepository.findTop1ByAgent_ShortName(DummyDataAdapterV3.DUMMY_AGENT);
        assertThat(depositOptional.isPresent()).isTrue();

        long overdraftLimit = -10L;
        long limitDisable = -5L;

        depositRepository.save(
            depositOptional.get().blocked(false).unlimit(false).overdraftLimit(overdraftLimit).limitDisable(limitDisable)
        );

        Long beforeBalance = depositOptional.get().getBalance();

        long paymentAmountInCcyInCents = beforeBalance + ((-1 * limitDisable) - 1);

        boolean canAgentProceedPayment = depositService.canAgentProceedPayment(DummyDataAdapterV3.DUMMY_AGENT, paymentAmountInCcyInCents);

        assertThat(canAgentProceedPayment).isTrue();

        Result result = depositService.decreaseAgentDeposit(DummyDataAdapterV3.DUMMY_AGENT, paymentAmountInCcyInCents);

        assertThat(result).isNotNull();
        assertThat(result.isSuccess()).isTrue();

        // Validate the Deposit in the database

        depositOptional = depositRepository.findTop1ByAgent_ShortName(DummyDataAdapterV3.DUMMY_AGENT);
        assertThat(depositOptional.isPresent()).isTrue();

        Deposit testDeposit = depositOptional.get();
        assertThat(testDeposit.getAgent()).isNotNull();
        //        assertThat(testDeposit.getAgent().getShortName()).isEqualTo(DummyDataAdapterV3.DUMMY_AGENT);
        assertThat(testDeposit.getBalance()).isEqualTo(beforeBalance - paymentAmountInCcyInCents);
        assertThat(testDeposit.getBalanceCcy()).isEqualTo(860);
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    @Transactional
    public void testCannotProceedPaymentWithOverdraftGreaterDeposit() {
        DummyUtils.prepareDummyData(dummyAdapterDelegate);


        Optional<Deposit> depositOptional = depositRepository.findTop1ByAgent_ShortName(DummyDataAdapterV3.DUMMY_AGENT);
        assertThat(depositOptional.isPresent()).isTrue();

        long overdraftLimit = -10L;
        long limitDisable = -5L;

        // IT IS OK, over -> disabled, disabled -> over
        depositRepository.save(
            depositOptional.get().blocked(false).unlimit(false).overdraftLimit(limitDisable).limitDisable(overdraftLimit)
        );

        Long beforeBalance = depositOptional.get().getBalance();

        long paymentAmountInCcyInCents = beforeBalance + ((-1 * overdraftLimit) - 1);

        boolean canAgentProceedPayment = depositService.canAgentProceedPayment(DummyDataAdapterV3.DUMMY_AGENT, paymentAmountInCcyInCents);

        assertThat(canAgentProceedPayment).isFalse();

        Result result = depositService.decreaseAgentDeposit(DummyDataAdapterV3.DUMMY_AGENT, paymentAmountInCcyInCents);

        assertThat(result).isNotNull();
        assertThat(result.isSuccess()).isFalse();

        // Validate the Deposit in the database

        depositOptional = depositRepository.findTop1ByAgent_ShortName(DummyDataAdapterV3.DUMMY_AGENT);
        assertThat(depositOptional.isPresent()).isTrue();

        Deposit testDeposit = depositOptional.get();
        assertThat(testDeposit.getAgent()).isNotNull();
        //        assertThat(testDeposit.getAgent().getShortName()).isEqualTo(DummyDataAdapterV3.DUMMY_AGENT);
        assertThat(testDeposit.getBalance()).isEqualTo(beforeBalance);
        assertThat(testDeposit.getBalanceCcy()).isEqualTo(860);
    }

    @Test(expected = CodedException.class)
    @WithMockUser(username = "admin", roles = "ADMIN")
    @Transactional
    public void testBlockedDeposit() {
        DummyUtils.prepareDummyData(dummyAdapterDelegate);


        Optional<Deposit> depositOptional = depositRepository.findTop1ByAgent_ShortName(DummyDataAdapterV3.DUMMY_AGENT);
        assertThat(depositOptional.isPresent()).isTrue();

        depositRepository.save(depositOptional.get().blocked(true));

        Long beforeBalance = depositOptional.get().getBalance();

        boolean canAgentProceedPayment = depositService.canAgentProceedPayment(DummyDataAdapterV3.DUMMY_AGENT, beforeBalance / 2);

        assertThat(canAgentProceedPayment).isFalse();
        // Validate the Deposit in the database

        //        depositOptional = depositRepository.findTop1ByAgent_ShortName(DummyDataAdapterV3.DUMMY_AGENT);
        //        assertThat(depositOptional.isPresent()).isTrue();

        //        Deposit testDeposit = depositOptional.get();
        //        assertThat(testDeposit.getAgent()).isNotNull();
        //        assertThat(testDeposit.getAgent().getShortName()).isEqualTo(DummyDataAdapterV3.DUMMY_AGENT);
        //        assertThat(testDeposit.getBalance()).isEqualTo(REPLENISH_BALANCE + beforeBalance);
        //        assertThat(testDeposit.getBalanceCcy()).isEqualTo(860);

    }

    @Test
    @Transactional
    public void createDepositWithExistingId() throws Exception {
        int databaseSizeBeforeCreate = depositRepository.findAll().size();

        // Create the Deposit with an existing ID
        deposit.setId(1L);
        DepositDTO depositDTO = depositMapper.toDto(deposit);

        // An entity with an existing ID cannot be created, so this API call must fail
        restDepositMockMvc
            .perform(
                post("/api/deposits").contentType(TestUtil.APPLICATION_JSON_UTF8).content(TestUtil.convertObjectToJsonBytes(depositDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Deposit in the database
        List<Deposit> depositList = depositRepository.findAll();
        assertThat(depositList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    public void checkOverdraftLimitIsRequired() throws Exception {
        int databaseSizeBeforeTest = depositRepository.findAll().size();
        // set the field null
        deposit.setOverdraftLimit(null);

        // Create the Deposit, which fails.
        DepositDTO depositDTO = depositMapper.toDto(deposit);

        restDepositMockMvc
            .perform(
                post("/api/deposits").contentType(TestUtil.APPLICATION_JSON_UTF8).content(TestUtil.convertObjectToJsonBytes(depositDTO))
            )
            .andExpect(status().isBadRequest());

        List<Deposit> depositList = depositRepository.findAll();
        assertThat(depositList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkBalanceIsRequired() throws Exception {
        int databaseSizeBeforeTest = depositRepository.findAll().size();
        // set the field null
        deposit.setBalance(null);

        // Create the Deposit, which fails.
        DepositDTO depositDTO = depositMapper.toDto(deposit);

        restDepositMockMvc
            .perform(
                post("/api/deposits").contentType(TestUtil.APPLICATION_JSON_UTF8).content(TestUtil.convertObjectToJsonBytes(depositDTO))
            )
            .andExpect(status().isBadRequest());

        List<Deposit> depositList = depositRepository.findAll();
        assertThat(depositList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkBalanceCcyIsRequired() throws Exception {
        int databaseSizeBeforeTest = depositRepository.findAll().size();
        // set the field null
        deposit.setBalanceCcy(null);

        // Create the Deposit, which fails.
        DepositDTO depositDTO = depositMapper.toDto(deposit);

        restDepositMockMvc
            .perform(
                post("/api/deposits").contentType(TestUtil.APPLICATION_JSON_UTF8).content(TestUtil.convertObjectToJsonBytes(depositDTO))
            )
            .andExpect(status().isBadRequest());

        List<Deposit> depositList = depositRepository.findAll();
        assertThat(depositList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkBookedBalanceIsRequired() throws Exception {
        int databaseSizeBeforeTest = depositRepository.findAll().size();
        // set the field null
        deposit.setBookedBalance(null);

        // Create the Deposit, which fails.
        DepositDTO depositDTO = depositMapper.toDto(deposit);

        restDepositMockMvc
            .perform(
                post("/api/deposits").contentType(TestUtil.APPLICATION_JSON_UTF8).content(TestUtil.convertObjectToJsonBytes(depositDTO))
            )
            .andExpect(status().isBadRequest());

        List<Deposit> depositList = depositRepository.findAll();
        assertThat(depositList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkLimitInformIsRequired() throws Exception {
        int databaseSizeBeforeTest = depositRepository.findAll().size();
        // set the field null
        deposit.setLimitInform(null);

        // Create the Deposit, which fails.
        DepositDTO depositDTO = depositMapper.toDto(deposit);

        restDepositMockMvc
            .perform(
                post("/api/deposits").contentType(TestUtil.APPLICATION_JSON_UTF8).content(TestUtil.convertObjectToJsonBytes(depositDTO))
            )
            .andExpect(status().isBadRequest());

        List<Deposit> depositList = depositRepository.findAll();
        assertThat(depositList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkLimitDisableIsRequired() throws Exception {
        int databaseSizeBeforeTest = depositRepository.findAll().size();
        // set the field null
        deposit.setLimitDisable(null);

        // Create the Deposit, which fails.
        DepositDTO depositDTO = depositMapper.toDto(deposit);

        restDepositMockMvc
            .perform(
                post("/api/deposits").contentType(TestUtil.APPLICATION_JSON_UTF8).content(TestUtil.convertObjectToJsonBytes(depositDTO))
            )
            .andExpect(status().isBadRequest());

        List<Deposit> depositList = depositRepository.findAll();
        assertThat(depositList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkBlockedIsRequired() throws Exception {
        int databaseSizeBeforeTest = depositRepository.findAll().size();
        // set the field null
        deposit.setBlocked(null);

        // Create the Deposit, which fails.
        DepositDTO depositDTO = depositMapper.toDto(deposit);

        restDepositMockMvc
            .perform(
                post("/api/deposits").contentType(TestUtil.APPLICATION_JSON_UTF8).content(TestUtil.convertObjectToJsonBytes(depositDTO))
            )
            .andExpect(status().isBadRequest());

        List<Deposit> depositList = depositRepository.findAll();
        assertThat(depositList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkUnlimitIsRequired() throws Exception {
        int databaseSizeBeforeTest = depositRepository.findAll().size();
        // set the field null
        deposit.setUnlimit(null);

        // Create the Deposit, which fails.
        DepositDTO depositDTO = depositMapper.toDto(deposit);

        restDepositMockMvc
            .perform(
                post("/api/deposits").contentType(TestUtil.APPLICATION_JSON_UTF8).content(TestUtil.convertObjectToJsonBytes(depositDTO))
            )
            .andExpect(status().isBadRequest());

        List<Deposit> depositList = depositRepository.findAll();
        assertThat(depositList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void getAllDeposits() throws Exception {
        // Initialize the database
        depositRepository.saveAndFlush(deposit);

        // Get all the depositList
        restDepositMockMvc
            .perform(get("/api/deposits?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(deposit.getId().intValue())))
            .andExpect(jsonPath("$.[*].overdraftLimit").value(hasItem(DEFAULT_OVERDRAFT_LIMIT.intValue())))
            .andExpect(jsonPath("$.[*].balance").value(hasItem(DEFAULT_BALANCE.intValue())))
            .andExpect(jsonPath("$.[*].balanceCcy").value(hasItem(DEFAULT_BALANCE_CCY)))
            .andExpect(jsonPath("$.[*].bookedBalance").value(hasItem(DEFAULT_BOOKED_BALANCE.intValue())))
            .andExpect(jsonPath("$.[*].limitInform").value(hasItem(DEFAULT_LIMIT_INFORM.intValue())))
            .andExpect(jsonPath("$.[*].limitDisable").value(hasItem(DEFAULT_LIMIT_DISABLE.intValue())))
            .andExpect(jsonPath("$.[*].contactGroup").value(hasItem(DEFAULT_CONTACT_GROUP.toString())))
            .andExpect(jsonPath("$.[*].blocked").value(hasItem(DEFAULT_BLOCKED.booleanValue())))
            .andExpect(jsonPath("$.[*].unlimit").value(hasItem(DEFAULT_UNLIMIT.booleanValue())));
    }

    @Test
    @Transactional
    public void getDeposit() throws Exception {
        // Initialize the database
        depositRepository.saveAndFlush(deposit);

        // Get the deposit
        restDepositMockMvc
            .perform(get("/api/deposits/{id}", deposit.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.id").value(deposit.getId().intValue()))
            .andExpect(jsonPath("$.overdraftLimit").value(DEFAULT_OVERDRAFT_LIMIT.intValue()))
            .andExpect(jsonPath("$.balance").value(DEFAULT_BALANCE.intValue()))
            .andExpect(jsonPath("$.balanceCcy").value(DEFAULT_BALANCE_CCY))
            .andExpect(jsonPath("$.bookedBalance").value(DEFAULT_BOOKED_BALANCE.intValue()))
            .andExpect(jsonPath("$.limitInform").value(DEFAULT_LIMIT_INFORM.intValue()))
            .andExpect(jsonPath("$.limitDisable").value(DEFAULT_LIMIT_DISABLE.intValue()))
            .andExpect(jsonPath("$.contactGroup").value(DEFAULT_CONTACT_GROUP.toString()))
            .andExpect(jsonPath("$.blocked").value(DEFAULT_BLOCKED.booleanValue()))
            .andExpect(jsonPath("$.unlimit").value(DEFAULT_UNLIMIT.booleanValue()));
    }

    @Test
    @Transactional
    public void getAllDepositsByOverdraftLimitIsEqualToSomething() throws Exception {
        // Initialize the database
        depositRepository.saveAndFlush(deposit);

        // Get all the depositList where overdraftLimit equals to DEFAULT_OVERDRAFT_LIMIT
        defaultDepositShouldBeFound("overdraftLimit.equals=" + DEFAULT_OVERDRAFT_LIMIT);

        // Get all the depositList where overdraftLimit equals to UPDATED_OVERDRAFT_LIMIT
        defaultDepositShouldNotBeFound("overdraftLimit.equals=" + UPDATED_OVERDRAFT_LIMIT);
    }

    @Test
    @Transactional
    public void getAllDepositsByOverdraftLimitIsInShouldWork() throws Exception {
        // Initialize the database
        depositRepository.saveAndFlush(deposit);

        // Get all the depositList where overdraftLimit in DEFAULT_OVERDRAFT_LIMIT or UPDATED_OVERDRAFT_LIMIT
        defaultDepositShouldBeFound("overdraftLimit.in=" + DEFAULT_OVERDRAFT_LIMIT + "," + UPDATED_OVERDRAFT_LIMIT);

        // Get all the depositList where overdraftLimit equals to UPDATED_OVERDRAFT_LIMIT
        defaultDepositShouldNotBeFound("overdraftLimit.in=" + UPDATED_OVERDRAFT_LIMIT);
    }

    @Test
    @Transactional
    public void getAllDepositsByOverdraftLimitIsNullOrNotNull() throws Exception {
        // Initialize the database
        depositRepository.saveAndFlush(deposit);

        // Get all the depositList where overdraftLimit is not null
        defaultDepositShouldBeFound("overdraftLimit.specified=true");

        // Get all the depositList where overdraftLimit is null
        defaultDepositShouldNotBeFound("overdraftLimit.specified=false");
    }

    @Test
    @Transactional
    public void getAllDepositsByOverdraftLimitIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        depositRepository.saveAndFlush(deposit);

        // Get all the depositList where overdraftLimit greater than or equals to DEFAULT_OVERDRAFT_LIMIT
        defaultDepositShouldBeFound("overdraftLimit.greaterOrEqualThan=" + DEFAULT_OVERDRAFT_LIMIT);

        // Get all the depositList where overdraftLimit greater than or equals to UPDATED_OVERDRAFT_LIMIT
        defaultDepositShouldNotBeFound("overdraftLimit.greaterOrEqualThan=" + UPDATED_OVERDRAFT_LIMIT);
    }

    @Test
    @Transactional
    public void getAllDepositsByOverdraftLimitIsLessThanSomething() throws Exception {
        // Initialize the database
        depositRepository.saveAndFlush(deposit);

        // Get all the depositList where overdraftLimit less than or equals to DEFAULT_OVERDRAFT_LIMIT
        defaultDepositShouldNotBeFound("overdraftLimit.lessThan=" + DEFAULT_OVERDRAFT_LIMIT);

        // Get all the depositList where overdraftLimit less than or equals to UPDATED_OVERDRAFT_LIMIT
        defaultDepositShouldBeFound("overdraftLimit.lessThan=" + UPDATED_OVERDRAFT_LIMIT);
    }

    @Test
    @Transactional
    public void getAllDepositsByBalanceIsEqualToSomething() throws Exception {
        // Initialize the database
        depositRepository.saveAndFlush(deposit);

        // Get all the depositList where balance equals to DEFAULT_BALANCE
        defaultDepositShouldBeFound("balance.equals=" + DEFAULT_BALANCE);

        // Get all the depositList where balance equals to UPDATED_BALANCE
        defaultDepositShouldNotBeFound("balance.equals=" + UPDATED_BALANCE);
    }

    @Test
    @Transactional
    public void getAllDepositsByBalanceIsInShouldWork() throws Exception {
        // Initialize the database
        depositRepository.saveAndFlush(deposit);

        // Get all the depositList where balance in DEFAULT_BALANCE or UPDATED_BALANCE
        defaultDepositShouldBeFound("balance.in=" + DEFAULT_BALANCE + "," + UPDATED_BALANCE);

        // Get all the depositList where balance equals to UPDATED_BALANCE
        defaultDepositShouldNotBeFound("balance.in=" + UPDATED_BALANCE);
    }

    @Test
    @Transactional
    public void getAllDepositsByBalanceIsNullOrNotNull() throws Exception {
        // Initialize the database
        depositRepository.saveAndFlush(deposit);

        // Get all the depositList where balance is not null
        defaultDepositShouldBeFound("balance.specified=true");

        // Get all the depositList where balance is null
        defaultDepositShouldNotBeFound("balance.specified=false");
    }

    @Test
    @Transactional
    public void getAllDepositsByBalanceIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        depositRepository.saveAndFlush(deposit);

        // Get all the depositList where balance greater than or equals to DEFAULT_BALANCE
        defaultDepositShouldBeFound("balance.greaterOrEqualThan=" + DEFAULT_BALANCE);

        // Get all the depositList where balance greater than or equals to UPDATED_BALANCE
        defaultDepositShouldNotBeFound("balance.greaterOrEqualThan=" + UPDATED_BALANCE);
    }

    @Test
    @Transactional
    public void getAllDepositsByBalanceIsLessThanSomething() throws Exception {
        // Initialize the database
        depositRepository.saveAndFlush(deposit);

        // Get all the depositList where balance less than or equals to DEFAULT_BALANCE
        defaultDepositShouldNotBeFound("balance.lessThan=" + DEFAULT_BALANCE);

        // Get all the depositList where balance less than or equals to UPDATED_BALANCE
        defaultDepositShouldBeFound("balance.lessThan=" + UPDATED_BALANCE);
    }

    @Test
    @Transactional
    public void getAllDepositsByBalanceCcyIsEqualToSomething() throws Exception {
        // Initialize the database
        depositRepository.saveAndFlush(deposit);

        // Get all the depositList where balanceCcy equals to DEFAULT_BALANCE_CCY
        defaultDepositShouldBeFound("balanceCcy.equals=" + DEFAULT_BALANCE_CCY);

        // Get all the depositList where balanceCcy equals to UPDATED_BALANCE_CCY
        defaultDepositShouldNotBeFound("balanceCcy.equals=" + UPDATED_BALANCE_CCY);
    }

    @Test
    @Transactional
    public void getAllDepositsByBalanceCcyIsInShouldWork() throws Exception {
        // Initialize the database
        depositRepository.saveAndFlush(deposit);

        // Get all the depositList where balanceCcy in DEFAULT_BALANCE_CCY or UPDATED_BALANCE_CCY
        defaultDepositShouldBeFound("balanceCcy.in=" + DEFAULT_BALANCE_CCY + "," + UPDATED_BALANCE_CCY);

        // Get all the depositList where balanceCcy equals to UPDATED_BALANCE_CCY
        defaultDepositShouldNotBeFound("balanceCcy.in=" + UPDATED_BALANCE_CCY);
    }

    @Test
    @Transactional
    public void getAllDepositsByBalanceCcyIsNullOrNotNull() throws Exception {
        // Initialize the database
        depositRepository.saveAndFlush(deposit);

        // Get all the depositList where balanceCcy is not null
        defaultDepositShouldBeFound("balanceCcy.specified=true");

        // Get all the depositList where balanceCcy is null
        defaultDepositShouldNotBeFound("balanceCcy.specified=false");
    }

    @Test
    @Transactional
    public void getAllDepositsByBalanceCcyIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        depositRepository.saveAndFlush(deposit);

        // Get all the depositList where balanceCcy greater than or equals to DEFAULT_BALANCE_CCY
        defaultDepositShouldBeFound("balanceCcy.greaterOrEqualThan=" + DEFAULT_BALANCE_CCY);

        // Get all the depositList where balanceCcy greater than or equals to UPDATED_BALANCE_CCY
        defaultDepositShouldNotBeFound("balanceCcy.greaterOrEqualThan=" + UPDATED_BALANCE_CCY);
    }

    @Test
    @Transactional
    public void getAllDepositsByBalanceCcyIsLessThanSomething() throws Exception {
        // Initialize the database
        depositRepository.saveAndFlush(deposit);

        // Get all the depositList where balanceCcy less than or equals to DEFAULT_BALANCE_CCY
        defaultDepositShouldNotBeFound("balanceCcy.lessThan=" + DEFAULT_BALANCE_CCY);

        // Get all the depositList where balanceCcy less than or equals to UPDATED_BALANCE_CCY
        defaultDepositShouldBeFound("balanceCcy.lessThan=" + UPDATED_BALANCE_CCY);
    }

    @Test
    @Transactional
    public void getAllDepositsByBookedBalanceIsEqualToSomething() throws Exception {
        // Initialize the database
        depositRepository.saveAndFlush(deposit);

        // Get all the depositList where bookedBalance equals to DEFAULT_BOOKED_BALANCE
        defaultDepositShouldBeFound("bookedBalance.equals=" + DEFAULT_BOOKED_BALANCE);

        // Get all the depositList where bookedBalance equals to UPDATED_BOOKED_BALANCE
        defaultDepositShouldNotBeFound("bookedBalance.equals=" + UPDATED_BOOKED_BALANCE);
    }

    @Test
    @Transactional
    public void getAllDepositsByBookedBalanceIsInShouldWork() throws Exception {
        // Initialize the database
        depositRepository.saveAndFlush(deposit);

        // Get all the depositList where bookedBalance in DEFAULT_BOOKED_BALANCE or UPDATED_BOOKED_BALANCE
        defaultDepositShouldBeFound("bookedBalance.in=" + DEFAULT_BOOKED_BALANCE + "," + UPDATED_BOOKED_BALANCE);

        // Get all the depositList where bookedBalance equals to UPDATED_BOOKED_BALANCE
        defaultDepositShouldNotBeFound("bookedBalance.in=" + UPDATED_BOOKED_BALANCE);
    }

    @Test
    @Transactional
    public void getAllDepositsByBookedBalanceIsNullOrNotNull() throws Exception {
        // Initialize the database
        depositRepository.saveAndFlush(deposit);

        // Get all the depositList where bookedBalance is not null
        defaultDepositShouldBeFound("bookedBalance.specified=true");

        // Get all the depositList where bookedBalance is null
        defaultDepositShouldNotBeFound("bookedBalance.specified=false");
    }

    @Test
    @Transactional
    public void getAllDepositsByBookedBalanceIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        depositRepository.saveAndFlush(deposit);

        // Get all the depositList where bookedBalance greater than or equals to DEFAULT_BOOKED_BALANCE
        defaultDepositShouldBeFound("bookedBalance.greaterOrEqualThan=" + DEFAULT_BOOKED_BALANCE);

        // Get all the depositList where bookedBalance greater than or equals to UPDATED_BOOKED_BALANCE
        defaultDepositShouldNotBeFound("bookedBalance.greaterOrEqualThan=" + UPDATED_BOOKED_BALANCE);
    }

    @Test
    @Transactional
    public void getAllDepositsByBookedBalanceIsLessThanSomething() throws Exception {
        // Initialize the database
        depositRepository.saveAndFlush(deposit);

        // Get all the depositList where bookedBalance less than or equals to DEFAULT_BOOKED_BALANCE
        defaultDepositShouldNotBeFound("bookedBalance.lessThan=" + DEFAULT_BOOKED_BALANCE);

        // Get all the depositList where bookedBalance less than or equals to UPDATED_BOOKED_BALANCE
        defaultDepositShouldBeFound("bookedBalance.lessThan=" + UPDATED_BOOKED_BALANCE);
    }

    @Test
    @Transactional
    public void getAllDepositsByLimitInformIsEqualToSomething() throws Exception {
        // Initialize the database
        depositRepository.saveAndFlush(deposit);

        // Get all the depositList where limitInform equals to DEFAULT_LIMIT_INFORM
        defaultDepositShouldBeFound("limitInform.equals=" + DEFAULT_LIMIT_INFORM);

        // Get all the depositList where limitInform equals to UPDATED_LIMIT_INFORM
        defaultDepositShouldNotBeFound("limitInform.equals=" + UPDATED_LIMIT_INFORM);
    }

    @Test
    @Transactional
    public void getAllDepositsByLimitInformIsInShouldWork() throws Exception {
        // Initialize the database
        depositRepository.saveAndFlush(deposit);

        // Get all the depositList where limitInform in DEFAULT_LIMIT_INFORM or UPDATED_LIMIT_INFORM
        defaultDepositShouldBeFound("limitInform.in=" + DEFAULT_LIMIT_INFORM + "," + UPDATED_LIMIT_INFORM);

        // Get all the depositList where limitInform equals to UPDATED_LIMIT_INFORM
        defaultDepositShouldNotBeFound("limitInform.in=" + UPDATED_LIMIT_INFORM);
    }

    @Test
    @Transactional
    public void getAllDepositsByLimitInformIsNullOrNotNull() throws Exception {
        // Initialize the database
        depositRepository.saveAndFlush(deposit);

        // Get all the depositList where limitInform is not null
        defaultDepositShouldBeFound("limitInform.specified=true");

        // Get all the depositList where limitInform is null
        defaultDepositShouldNotBeFound("limitInform.specified=false");
    }

    @Test
    @Transactional
    public void getAllDepositsByLimitInformIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        depositRepository.saveAndFlush(deposit);

        // Get all the depositList where limitInform greater than or equals to DEFAULT_LIMIT_INFORM
        defaultDepositShouldBeFound("limitInform.greaterOrEqualThan=" + DEFAULT_LIMIT_INFORM);

        // Get all the depositList where limitInform greater than or equals to UPDATED_LIMIT_INFORM
        defaultDepositShouldNotBeFound("limitInform.greaterOrEqualThan=" + UPDATED_LIMIT_INFORM);
    }

    @Test
    @Transactional
    public void getAllDepositsByLimitInformIsLessThanSomething() throws Exception {
        // Initialize the database
        depositRepository.saveAndFlush(deposit);

        // Get all the depositList where limitInform less than or equals to DEFAULT_LIMIT_INFORM
        defaultDepositShouldNotBeFound("limitInform.lessThan=" + DEFAULT_LIMIT_INFORM);

        // Get all the depositList where limitInform less than or equals to UPDATED_LIMIT_INFORM
        defaultDepositShouldBeFound("limitInform.lessThan=" + UPDATED_LIMIT_INFORM);
    }

    @Test
    @Transactional
    public void getAllDepositsByLimitDisableIsEqualToSomething() throws Exception {
        // Initialize the database
        depositRepository.saveAndFlush(deposit);

        // Get all the depositList where limitDisable equals to DEFAULT_LIMIT_DISABLE
        defaultDepositShouldBeFound("limitDisable.equals=" + DEFAULT_LIMIT_DISABLE);

        // Get all the depositList where limitDisable equals to UPDATED_LIMIT_DISABLE
        defaultDepositShouldNotBeFound("limitDisable.equals=" + UPDATED_LIMIT_DISABLE);
    }

    @Test
    @Transactional
    public void getAllDepositsByLimitDisableIsInShouldWork() throws Exception {
        // Initialize the database
        depositRepository.saveAndFlush(deposit);

        // Get all the depositList where limitDisable in DEFAULT_LIMIT_DISABLE or UPDATED_LIMIT_DISABLE
        defaultDepositShouldBeFound("limitDisable.in=" + DEFAULT_LIMIT_DISABLE + "," + UPDATED_LIMIT_DISABLE);

        // Get all the depositList where limitDisable equals to UPDATED_LIMIT_DISABLE
        defaultDepositShouldNotBeFound("limitDisable.in=" + UPDATED_LIMIT_DISABLE);
    }

    @Test
    @Transactional
    public void getAllDepositsByLimitDisableIsNullOrNotNull() throws Exception {
        // Initialize the database
        depositRepository.saveAndFlush(deposit);

        // Get all the depositList where limitDisable is not null
        defaultDepositShouldBeFound("limitDisable.specified=true");

        // Get all the depositList where limitDisable is null
        defaultDepositShouldNotBeFound("limitDisable.specified=false");
    }

    @Test
    @Transactional
    public void getAllDepositsByLimitDisableIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        depositRepository.saveAndFlush(deposit);

        // Get all the depositList where limitDisable greater than or equals to DEFAULT_LIMIT_DISABLE
        defaultDepositShouldBeFound("limitDisable.greaterOrEqualThan=" + DEFAULT_LIMIT_DISABLE);

        // Get all the depositList where limitDisable greater than or equals to UPDATED_LIMIT_DISABLE
        defaultDepositShouldNotBeFound("limitDisable.greaterOrEqualThan=" + UPDATED_LIMIT_DISABLE);
    }

    @Test
    @Transactional
    public void getAllDepositsByLimitDisableIsLessThanSomething() throws Exception {
        // Initialize the database
        depositRepository.saveAndFlush(deposit);

        // Get all the depositList where limitDisable less than or equals to DEFAULT_LIMIT_DISABLE
        defaultDepositShouldNotBeFound("limitDisable.lessThan=" + DEFAULT_LIMIT_DISABLE);

        // Get all the depositList where limitDisable less than or equals to UPDATED_LIMIT_DISABLE
        defaultDepositShouldBeFound("limitDisable.lessThan=" + UPDATED_LIMIT_DISABLE);
    }

    @Test
    @Transactional
    public void getAllDepositsByContactGroupIsEqualToSomething() throws Exception {
        // Initialize the database
        depositRepository.saveAndFlush(deposit);

        // Get all the depositList where contactGroup equals to DEFAULT_CONTACT_GROUP
        defaultDepositShouldBeFound("contactGroup.equals=" + DEFAULT_CONTACT_GROUP);

        // Get all the depositList where contactGroup equals to UPDATED_CONTACT_GROUP
        defaultDepositShouldNotBeFound("contactGroup.equals=" + UPDATED_CONTACT_GROUP);
    }

    @Test
    @Transactional
    public void getAllDepositsByContactGroupIsInShouldWork() throws Exception {
        // Initialize the database
        depositRepository.saveAndFlush(deposit);

        // Get all the depositList where contactGroup in DEFAULT_CONTACT_GROUP or UPDATED_CONTACT_GROUP
        defaultDepositShouldBeFound("contactGroup.in=" + DEFAULT_CONTACT_GROUP + "," + UPDATED_CONTACT_GROUP);

        // Get all the depositList where contactGroup equals to UPDATED_CONTACT_GROUP
        defaultDepositShouldNotBeFound("contactGroup.in=" + UPDATED_CONTACT_GROUP);
    }

    @Test
    @Transactional
    public void getAllDepositsByContactGroupIsNullOrNotNull() throws Exception {
        // Initialize the database
        depositRepository.saveAndFlush(deposit);

        // Get all the depositList where contactGroup is not null
        defaultDepositShouldBeFound("contactGroup.specified=true");

        // Get all the depositList where contactGroup is null
        defaultDepositShouldNotBeFound("contactGroup.specified=false");
    }

    @Test
    @Transactional
    public void getAllDepositsByBlockedIsEqualToSomething() throws Exception {
        // Initialize the database
        depositRepository.saveAndFlush(deposit);

        // Get all the depositList where blocked equals to DEFAULT_BLOCKED
        defaultDepositShouldBeFound("blocked.equals=" + DEFAULT_BLOCKED);

        // Get all the depositList where blocked equals to UPDATED_BLOCKED
        defaultDepositShouldNotBeFound("blocked.equals=" + UPDATED_BLOCKED);
    }

    @Test
    @Transactional
    public void getAllDepositsByBlockedIsInShouldWork() throws Exception {
        // Initialize the database
        depositRepository.saveAndFlush(deposit);

        // Get all the depositList where blocked in DEFAULT_BLOCKED or UPDATED_BLOCKED
        defaultDepositShouldBeFound("blocked.in=" + DEFAULT_BLOCKED + "," + UPDATED_BLOCKED);

        // Get all the depositList where blocked equals to UPDATED_BLOCKED
        defaultDepositShouldNotBeFound("blocked.in=" + UPDATED_BLOCKED);
    }

    @Test
    @Transactional
    public void getAllDepositsByBlockedIsNullOrNotNull() throws Exception {
        // Initialize the database
        depositRepository.saveAndFlush(deposit);

        // Get all the depositList where blocked is not null
        defaultDepositShouldBeFound("blocked.specified=true");

        // Get all the depositList where blocked is null
        defaultDepositShouldNotBeFound("blocked.specified=false");
    }

    @Test
    @Transactional
    public void getAllDepositsByUnlimitIsEqualToSomething() throws Exception {
        // Initialize the database
        depositRepository.saveAndFlush(deposit);

        // Get all the depositList where unlimit equals to DEFAULT_UNLIMIT
        defaultDepositShouldBeFound("unlimit.equals=" + DEFAULT_UNLIMIT);

        // Get all the depositList where unlimit equals to UPDATED_UNLIMIT
        defaultDepositShouldNotBeFound("unlimit.equals=" + UPDATED_UNLIMIT);
    }

    @Test
    @Transactional
    public void getAllDepositsByUnlimitIsInShouldWork() throws Exception {
        // Initialize the database
        depositRepository.saveAndFlush(deposit);

        // Get all the depositList where unlimit in DEFAULT_UNLIMIT or UPDATED_UNLIMIT
        defaultDepositShouldBeFound("unlimit.in=" + DEFAULT_UNLIMIT + "," + UPDATED_UNLIMIT);

        // Get all the depositList where unlimit equals to UPDATED_UNLIMIT
        defaultDepositShouldNotBeFound("unlimit.in=" + UPDATED_UNLIMIT);
    }

    @Test
    @Transactional
    public void getAllDepositsByUnlimitIsNullOrNotNull() throws Exception {
        // Initialize the database
        depositRepository.saveAndFlush(deposit);

        // Get all the depositList where unlimit is not null
        defaultDepositShouldBeFound("unlimit.specified=true");

        // Get all the depositList where unlimit is null
        defaultDepositShouldNotBeFound("unlimit.specified=false");
    }

    @Test
    @Transactional
    public void getAllDepositsByGatewayIsEqualToSomething() throws Exception {
        // Initialize the database
        Gateway gateway = GatewayResourceIntTest.createEntity(em);
        em.persist(gateway);
        em.flush();
        deposit.setGateway(gateway);
        depositRepository.saveAndFlush(deposit);
        Long gatewayId = gateway.getId();

        // Get all the depositList where gateway equals to gatewayId
        defaultDepositShouldBeFound("gatewayId.equals=" + gatewayId);

        // Get all the depositList where gateway equals to gatewayId + 1
        defaultDepositShouldNotBeFound("gatewayId.equals=" + (gatewayId + 1));
    }

    @Test
    @Transactional
    public void getAllDepositsByAgentIsEqualToSomething() throws Exception {
        // Initialize the database
        Agent agent = AgentResourceIntTest.createEntity(em);
        em.persist(agent);
        em.flush();
        deposit.setAgent(agent);
        depositRepository.saveAndFlush(deposit);
        Long agentId = agent.getId();

        // Get all the depositList where agent equals to agentId
        defaultDepositShouldBeFound("agentId.equals=" + agentId);

        // Get all the depositList where agent equals to agentId + 1
        defaultDepositShouldNotBeFound("agentId.equals=" + (agentId + 1));
    }

    @Test
    @Transactional
    public void getAllDepositsByVirtualAccountIsEqualToSomething() throws Exception {
        // Initialize the database
        VirtualAccount virtualAccount = VirtualAccountResourceIntTest.createEntity(em);
        em.persist(virtualAccount);
        em.flush();
        deposit.setVirtualAccount(virtualAccount);
        depositRepository.saveAndFlush(deposit);
        Long virtualAccountId = virtualAccount.getId();

        // Get all the depositList where virtualAccount equals to virtualAccountId
        defaultDepositShouldBeFound("virtualAccountId.equals=" + virtualAccountId);

        // Get all the depositList where virtualAccount equals to virtualAccountId + 1
        defaultDepositShouldNotBeFound("virtualAccountId.equals=" + (virtualAccountId + 1));
    }

    /**
     * Executes the search, and checks that the default entity is returned
     */
    private void defaultDepositShouldBeFound(String filter) throws Exception {
        restDepositMockMvc
            .perform(get("/api/deposits?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(deposit.getId().intValue())))
            .andExpect(jsonPath("$.[*].overdraftLimit").value(hasItem(DEFAULT_OVERDRAFT_LIMIT.intValue())))
            .andExpect(jsonPath("$.[*].balance").value(hasItem(DEFAULT_BALANCE.intValue())))
            .andExpect(jsonPath("$.[*].balanceCcy").value(hasItem(DEFAULT_BALANCE_CCY)))
            .andExpect(jsonPath("$.[*].bookedBalance").value(hasItem(DEFAULT_BOOKED_BALANCE.intValue())))
            .andExpect(jsonPath("$.[*].limitInform").value(hasItem(DEFAULT_LIMIT_INFORM.intValue())))
            .andExpect(jsonPath("$.[*].limitDisable").value(hasItem(DEFAULT_LIMIT_DISABLE.intValue())))
            .andExpect(jsonPath("$.[*].contactGroup").value(hasItem(DEFAULT_CONTACT_GROUP.toString())))
            .andExpect(jsonPath("$.[*].blocked").value(hasItem(DEFAULT_BLOCKED.booleanValue())))
            .andExpect(jsonPath("$.[*].unlimit").value(hasItem(DEFAULT_UNLIMIT.booleanValue())));

        // Check, that the count call also returns 1
        restDepositMockMvc
            .perform(get("/api/deposits/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(content().string("1"));
    }

    /**
     * Executes the search, and checks that the default entity is not returned
     */
    private void defaultDepositShouldNotBeFound(String filter) throws Exception {
        restDepositMockMvc
            .perform(get("/api/deposits?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty());

        // Check, that the count call also returns 0
        restDepositMockMvc
            .perform(get("/api/deposits/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(content().string("0"));
    }

    @Test
    @Transactional
    public void getNonExistingDeposit() throws Exception {
        // Get the deposit
        restDepositMockMvc.perform(get("/api/deposits/{id}", Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updateDeposit() throws Exception {
        // Initialize the database
        depositRepository.saveAndFlush(deposit);

        int databaseSizeBeforeUpdate = depositRepository.findAll().size();

        // Update the deposit
        Deposit updatedDeposit = depositRepository.findById(deposit.getId()).get();
        // Disconnect from session so that the updates on updatedDeposit are not directly saved in db
        em.detach(updatedDeposit);
        updatedDeposit
            .overdraftLimit(UPDATED_OVERDRAFT_LIMIT)
            .balance(UPDATED_BALANCE)
            .balanceCcy(UPDATED_BALANCE_CCY)
            .bookedBalance(UPDATED_BOOKED_BALANCE)
            .limitInform(UPDATED_LIMIT_INFORM)
            .limitDisable(UPDATED_LIMIT_DISABLE)
            .contactGroup(UPDATED_CONTACT_GROUP)
            .blocked(UPDATED_BLOCKED)
            .unlimit(UPDATED_UNLIMIT);
        DepositDTO depositDTO = depositMapper.toDto(updatedDeposit);

        restDepositMockMvc
            .perform(
                put("/api/deposits").contentType(TestUtil.APPLICATION_JSON_UTF8).content(TestUtil.convertObjectToJsonBytes(depositDTO))
            )
            .andExpect(status().isOk());

        // Validate the Deposit in the database
        List<Deposit> depositList = depositRepository.findAll();
        assertThat(depositList).hasSize(databaseSizeBeforeUpdate);
        Deposit testDeposit = depositList.get(depositList.size() - 1);
        assertThat(testDeposit.getOverdraftLimit()).isEqualTo(UPDATED_OVERDRAFT_LIMIT);
        assertThat(testDeposit.getBalance()).isEqualTo(UPDATED_BALANCE);
        assertThat(testDeposit.getBalanceCcy()).isEqualTo(UPDATED_BALANCE_CCY);
        assertThat(testDeposit.getBookedBalance()).isEqualTo(UPDATED_BOOKED_BALANCE);
        assertThat(testDeposit.getLimitInform()).isEqualTo(UPDATED_LIMIT_INFORM);
        assertThat(testDeposit.getLimitDisable()).isEqualTo(UPDATED_LIMIT_DISABLE);
        assertThat(testDeposit.getContactGroup()).isEqualTo(UPDATED_CONTACT_GROUP);
        assertThat(testDeposit.isBlocked()).isEqualTo(UPDATED_BLOCKED);
        assertThat(testDeposit.isUnlimit()).isEqualTo(UPDATED_UNLIMIT);
    }

    @Test
    @Transactional
    public void updateNonExistingDeposit() throws Exception {
        int databaseSizeBeforeUpdate = depositRepository.findAll().size();

        // Create the Deposit
        DepositDTO depositDTO = depositMapper.toDto(deposit);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restDepositMockMvc
            .perform(
                put("/api/deposits").contentType(TestUtil.APPLICATION_JSON_UTF8).content(TestUtil.convertObjectToJsonBytes(depositDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Deposit in the database
        List<Deposit> depositList = depositRepository.findAll();
        assertThat(depositList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    public void deleteDeposit() throws Exception {
        // Initialize the database
        depositRepository.saveAndFlush(deposit);

        int databaseSizeBeforeDelete = depositRepository.findAll().size();

        // Get the deposit
        restDepositMockMvc
            .perform(delete("/api/deposits/{id}", deposit.getId()).accept(TestUtil.APPLICATION_JSON_UTF8))
            .andExpect(status().isOk());

        // Validate the database is empty
        List<Deposit> depositList = depositRepository.findAll();
        assertThat(depositList).hasSize(databaseSizeBeforeDelete - 1);
    }

    @Test
    @Transactional
    public void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Deposit.class);
        Deposit deposit1 = new Deposit();
        deposit1.setId(1L);
        Deposit deposit2 = new Deposit();
        deposit2.setId(deposit1.getId());
        assertThat(deposit1).isEqualTo(deposit2);
        deposit2.setId(2L);
        assertThat(deposit1).isNotEqualTo(deposit2);
        deposit1.setId(null);
        assertThat(deposit1).isNotEqualTo(deposit2);
    }

    @Test
    @Transactional
    public void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(DepositDTO.class);
        DepositDTO depositDTO1 = new DepositDTO();
        depositDTO1.setId(1L);
        DepositDTO depositDTO2 = new DepositDTO();
        assertThat(depositDTO1).isNotEqualTo(depositDTO2);
        depositDTO2.setId(depositDTO1.getId());
        assertThat(depositDTO1).isEqualTo(depositDTO2);
        depositDTO2.setId(2L);
        assertThat(depositDTO1).isNotEqualTo(depositDTO2);
        depositDTO1.setId(null);
        assertThat(depositDTO1).isNotEqualTo(depositDTO2);
    }
    //    @Test
    //    @Transactional
    //    public void testEntityFromId() {
    //        assertThat(depositMapper.fromId(42L).getId()).isEqualTo(42);
    //        assertThat(depositMapper.fromId(null)).isNull();
    //    }
}
