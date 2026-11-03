package uz.paykap.web.rest;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.Validator;
import uz.devops.bmcore.enumeration.PayType;
import uz.devops.bmcore.enumeration.TransactionStatus;
import uz.javlon.commons.util.CardUtil;
import uz.paykap.IntegrationTest;
import uz.paykap.client.PaymsClient;
import uz.paykap.config.ApplicationProperties;
import uz.paykap.domain.Transaction;
import uz.paykap.repository.TransactionRepository;
import uz.paykap.service.TransactionQueryService;
import uz.paykap.service.TransactionService;
import uz.paykap.service.action.transaction.TransactionActionService;
import uz.paykap.service.dto.TransactionDTO;
import uz.paykap.service.mapper.TransactionMapper;
import uz.paykap.web.rest.errors.ExceptionTranslator;

import javax.persistence.EntityManager;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static uz.paykap.web.rest.TestUtil.createFormattingConversionService;

/**
 * Test class for the TransactionResource REST controller.
 *
 * @see TransactionResource
 */
@RunWith(SpringRunner.class)
@IntegrationTest
public class TransactionResourceIntTest {

    private static final Long DEFAULT_REQUEST_AMOUNT = 1L;
    private static final Long UPDATED_REQUEST_AMOUNT = 2L;

    private static final Integer DEFAULT_REQUEST_CCY = 1;
    private static final Integer UPDATED_REQUEST_CCY = 2;

    private static final String DEFAULT_REQUEST_TR_ID = "AAAAAAAAAA";
    private static final String UPDATED_REQUEST_TR_ID = "BBBBBBBBBB";

    private static final String DEFAULT_REQUEST_LOGIN = "AAAAAAAAAA";
    private static final String UPDATED_REQUEST_LOGIN = "BBBBBBBBBB";

    private static final String DEFAULT_REQUEST_GROUP = "AAAAAAAAAA";
    private static final String UPDATED_REQUEST_GROUP = "BBBBBBBBBB";

    private static final String DEFAULT_SENDER_NAME = "AAAAAAAAAA";
    private static final String UPDATED_SENDER_NAME = "BBBBBBBBBB";

    private static final String DEFAULT_SENDER_ACCOUNT = "AAAAAAAAAA";
    private static final String UPDATED_SENDER_ACCOUNT = "BBBBBBBBBB";

    private static final Long DEFAULT_SENDER_SERVICE_ID = 1L;
    private static final Long UPDATED_SENDER_SERVICE_ID = 2L;

    private static final Long DEFAULT_SENDER_AMOUNT = 1L;
    private static final Long UPDATED_SENDER_AMOUNT = 2L;

    private static final Integer DEFAULT_SENDER_CCY = 1;
    private static final Integer UPDATED_SENDER_CCY = 2;

    private static final String DEFAULT_SENDER_TR_ID = "AAAAAAAAAA";
    private static final String UPDATED_SENDER_TR_ID = "BBBBBBBBBB";

    private static final Boolean DEFAULT_SENDER_SUCCESS = false;
    private static final Boolean UPDATED_SENDER_SUCCESS = true;

    private static final Instant DEFAULT_SENDER_DATE = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_SENDER_DATE = Instant.now().truncatedTo(ChronoUnit.MILLIS);

    private static final String DEFAULT_SENDER_STATUS = "AAAAAAAAAA";
    private static final String UPDATED_SENDER_STATUS = "BBBBBBBBBB";

    private static final String DEFAULT_SENDER_STATUS_TEXT = "AAAAAAAAAA";
    private static final String UPDATED_SENDER_STATUS_TEXT = "BBBBBBBBBB";

    private static final Long DEFAULT_SENDER_BILL_ID = 1L;
    private static final Long UPDATED_SENDER_BILL_ID = 2L;

    private static final String DEFAULT_SENDER_GATEWAY = "AAAAAAAAAA";
    private static final String UPDATED_SENDER_GATEWAY = "BBBBBBBBBB";

    private static final Double DEFAULT_GATEWAY_AMOUNT = 1D;
    private static final Double UPDATED_GATEWAY_AMOUNT = 2D;

    private static final String DEFAULT_RECIPIENT_NAME = "AAAAAAAAAA";
    private static final String UPDATED_RECIPIENT_NAME = "BBBBBBBBBB";

    private static final String DEFAULT_RECIPIENT_ACCOUNT = "AAAAAAAAAA";
    private static final String UZCARD_RECIPIENT_ACCOUNT = "8600140000000000";
    private static final String UPDATED_RECIPIENT_ACCOUNT = "BBBBBBBBBB";

    private static final Long DEFAULT_RECIPIENT_SERVICE_ID = 1L;
    private static final Long UPDATED_RECIPIENT_SERVICE_ID = 2L;

    private static final Long DEFAULT_RECIPIENT_AMOUNT = 1L;
    private static final Long UPDATED_RECIPIENT_AMOUNT = 2L;

    private static final Integer DEFAULT_RECIPIENT_CCY = 1;
    private static final Integer UPDATED_RECIPIENT_CCY = 2;

    private static final String DEFAULT_RECIPIENT_TR_ID = "AAAAAAAAAA";
    private static final String UPDATED_RECIPIENT_TR_ID = "BBBBBBBBBB";

    private static final Boolean DEFAULT_RECIPIENT_SUCCESS = false;
    private static final Boolean UPDATED_RECIPIENT_SUCCESS = true;

    private static final Instant DEFAULT_RECIPIENT_DATE = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_RECIPIENT_DATE = Instant.now().truncatedTo(ChronoUnit.MILLIS);

    private static final String DEFAULT_RECIPIENT_STATUS = "AAAAAAAAAA";
    private static final String UPDATED_RECIPIENT_STATUS = "BBBBBBBBBB";

    private static final String DEFAULT_RECIPIENT_STATUS_TEXT = "AAAAAAAAAA";
    private static final String UPDATED_RECIPIENT_STATUS_TEXT = "BBBBBBBBBB";

    private static final Long DEFAULT_RECIPIENT_BILL_ID = 1L;
    private static final Long UPDATED_RECIPIENT_BILL_ID = 2L;

    private static final String DEFAULT_RECIPIENT_GATEWAY = "AAAAAAAAAA";
    private static final String UPDATED_RECIPIENT_GATEWAY = "BBBBBBBBBB";

    private static final Long DEFAULT_SYSTEM_COMMISSION_UP_AMOUNT = 1L;
    private static final Long UPDATED_SYSTEM_COMMISSION_UP_AMOUNT = 2L;

    private static final Long DEFAULT_SYSTEM_COMMISSION_DOWN_AMOUNT = 1L;
    private static final Long UPDATED_SYSTEM_COMMISSION_DOWN_AMOUNT = 2L;

    private static final Long DEFAULT_SYSTEM_FEE_AMOUNT = 1L;
    private static final Long UPDATED_SYSTEM_FEE_AMOUNT = 2L;

    private static final Long DEFAULT_AGENT_FEE_AMOUNT = 1L;
    private static final Long UPDATED_AGENT_FEE_AMOUNT = 2L;

    private static final Double DEFAULT_ROUND_AMOUNT = 0D;
    private static final Double UPDATED_ROUND_AMOUNT = 1D;

    private static final Double DEFAULT_SYSTEM_FEE = 0D;
    private static final Double UPDATED_SYSTEM_FEE = 1D;

    private static final Integer DEFAULT_SYSTEM_CURRENCY = 1;
    private static final Integer UPDATED_SYSTEM_CURRENCY = 2;

    private static final Integer DEFAULT_CCY_RATE = 1;
    private static final Integer UPDATED_CCY_RATE = 2;

    private static final TransactionStatus DEFAULT_STATUS = TransactionStatus.NEW;
    private static final TransactionStatus UPDATED_STATUS = TransactionStatus.CHECK_OK;

    private static final String DEFAULT_COMMENT = "AAAAAAAAAA";
    private static final String UPDATED_COMMENT = "BBBBBBBBBB";

    private static final Instant DEFAULT_DATE = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_DATE = Instant.now().truncatedTo(ChronoUnit.MILLIS);

    private static final PayType DEFAULT_PAY_TYPE = PayType.CASH;
    private static final PayType UPDATED_PAY_TYPE = PayType.UZCARD;

    private static final String DEFAULT_REQUEST_SUB_ACCOUNT = "AAAAAAAAAA";
    private static final String UPDATED_REQUEST_SUB_ACCOUNT = "BBBBBBBBBB";

    private static final String DEFAULT_EPOS_MERCHANT = "AAAAAAAAAA";
    private static final String UPDATED_EPOS_MERCHANT = "BBBBBBBBBB";

    private static final String DEFAULT_EPOS_TERMINAL = "AAAAAAAAAA";
    private static final String UPDATED_EPOS_TERMINAL = "BBBBBBBBBB";

    private static final Boolean DEFAULT_MAIN = false;
    private static final Boolean UPDATED_MAIN = true;

    private static final Integer DEFAULT_AMOUNT_RATIO = 1;
    private static final Integer UPDATED_AMOUNT_RATIO = 2;

    private static final String DEFAULT_SENDER_BEAN = "AAAAAAAAAA";
    private static final String UPDATED_SENDER_BEAN = "BBBBBBBBBB";

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private TransactionMapper transactionMapper;

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private TransactionQueryService transactionQueryService;

    @Autowired
    private MappingJackson2HttpMessageConverter jacksonMessageConverter;

    @Autowired
    private PageableHandlerMethodArgumentResolver pageableArgumentResolver;

    @Autowired
    private ExceptionTranslator exceptionTranslator;

    @Autowired
    private EntityManager em;

    @Autowired
    private Validator validator;

    @Autowired
    private PaymsClient paymsClient;

    @Autowired
    private ApplicationProperties applicationProperties;

    @Autowired
    private JaversEntityAuditResource javersEntityAuditResource;

    @Autowired
    private TransactionActionService actionService;

    private MockMvc restTransactionMockMvc;

    private Transaction transaction;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        final TransactionResource transactionResource = new TransactionResource(
                transactionService,
                transactionRepository,
                transactionQueryService,
                paymsClient,
                applicationProperties, actionService);
        this.restTransactionMockMvc =
            MockMvcBuilders
                .standaloneSetup(transactionResource)
                .setCustomArgumentResolvers(pageableArgumentResolver)
                .setControllerAdvice(exceptionTranslator)
                .setConversionService(createFormattingConversionService())
                .setMessageConverters(jacksonMessageConverter)
                .setValidator(validator)
                .build();
    }

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Transaction createEntity(EntityManager em) {
        return createEntity(em, DEFAULT_REQUEST_TR_ID);
    }

    /**
     * Create an entity for this test.
     * <p>
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Transaction createEntity(EntityManager em, String requestTrId) {
        Transaction transaction = new Transaction()
            .requestAmount(DEFAULT_REQUEST_AMOUNT)
            .requestCcy(DEFAULT_REQUEST_CCY)
            .requestTrId(requestTrId)
            .requestLogin(DEFAULT_REQUEST_LOGIN)
            .requestGroup(DEFAULT_REQUEST_GROUP)
            .senderName(DEFAULT_SENDER_NAME)
            .senderAccount(DEFAULT_SENDER_ACCOUNT)
            .senderServiceId(DEFAULT_SENDER_SERVICE_ID)
            .senderAmount(DEFAULT_SENDER_AMOUNT)
            .senderCcy(DEFAULT_SENDER_CCY)
            .senderTrId(DEFAULT_SENDER_TR_ID)
            .senderSuccess(DEFAULT_SENDER_SUCCESS)
            .senderDate(DEFAULT_SENDER_DATE)
            .senderStatus(DEFAULT_SENDER_STATUS)
            .senderStatusText(DEFAULT_SENDER_STATUS_TEXT)
            .senderBillId(DEFAULT_SENDER_BILL_ID)
            .senderGateway(DEFAULT_SENDER_GATEWAY)
            .gatewayAmount(DEFAULT_GATEWAY_AMOUNT)
            .recipientName(DEFAULT_RECIPIENT_NAME)
            .recipientAccount(DEFAULT_RECIPIENT_ACCOUNT)
            .recipientServiceId(DEFAULT_RECIPIENT_SERVICE_ID)
            .recipientAmount(DEFAULT_RECIPIENT_AMOUNT)
            .recipientCcy(DEFAULT_RECIPIENT_CCY)
            .recipientTrId(DEFAULT_RECIPIENT_TR_ID)
            .recipientSuccess(DEFAULT_RECIPIENT_SUCCESS)
            .recipientDate(DEFAULT_RECIPIENT_DATE)
            .recipientStatus(DEFAULT_RECIPIENT_STATUS)
            .recipientStatusText(DEFAULT_RECIPIENT_STATUS_TEXT)
            .recipientBillId(DEFAULT_RECIPIENT_BILL_ID)
            .recipientGateway(DEFAULT_RECIPIENT_GATEWAY)
            .systemCommissionUpAmount(DEFAULT_SYSTEM_COMMISSION_UP_AMOUNT)
            .systemCommissionDownAmount(DEFAULT_SYSTEM_COMMISSION_DOWN_AMOUNT)
            .systemFeeAmount(DEFAULT_SYSTEM_FEE_AMOUNT)
            .agentFeeAmount(DEFAULT_AGENT_FEE_AMOUNT)
            .roundAmount(DEFAULT_ROUND_AMOUNT)
            .systemFee(DEFAULT_SYSTEM_FEE)
            .systemCurrency(DEFAULT_SYSTEM_CURRENCY)
            .ccyRate(DEFAULT_CCY_RATE)
            .status(DEFAULT_STATUS)
            .comment(DEFAULT_COMMENT)
            .date(DEFAULT_DATE)
            .payType(DEFAULT_PAY_TYPE)
            .requestSubAccount(DEFAULT_REQUEST_SUB_ACCOUNT)
            .eposMerchant(DEFAULT_EPOS_MERCHANT)
            .eposTerminal(DEFAULT_EPOS_TERMINAL)
            .main(DEFAULT_MAIN)
            .amountRatio(DEFAULT_AMOUNT_RATIO)
            .senderBean(DEFAULT_SENDER_BEAN);
        return transaction;
    }

    @Before
    public void initTest() {
        transaction = createEntity(em);
    }

    @Test
    @Transactional
    public void createTransaction() throws Exception {
        int databaseSizeBeforeCreate = transactionRepository.findAll().size();

        // Create the Transaction
        TransactionDTO transactionDTO = transactionMapper.toDto(transaction);
        restTransactionMockMvc
            .perform(
                post("/api/transactions")
                    .contentType(TestUtil.APPLICATION_JSON_UTF8)
                    .content(TestUtil.convertObjectToJsonBytes(transactionDTO))
            )
            .andExpect(status().isCreated());

        // Validate the Transaction in the database
        List<Transaction> transactionList = transactionRepository.findAll();
        assertThat(transactionList).hasSize(databaseSizeBeforeCreate + 1);
        Transaction testTransaction = transactionList.get(transactionList.size() - 1);
        assertThat(testTransaction.getRequestAmount()).isEqualTo(DEFAULT_REQUEST_AMOUNT);
        assertThat(testTransaction.getRequestCcy()).isEqualTo(DEFAULT_REQUEST_CCY);
        assertThat(testTransaction.getRequestTrId()).isEqualTo(DEFAULT_REQUEST_TR_ID);
        assertThat(testTransaction.getRequestLogin()).isEqualTo(DEFAULT_REQUEST_LOGIN);
        assertThat(testTransaction.getRequestGroup()).isEqualTo(DEFAULT_REQUEST_GROUP);
        assertThat(testTransaction.getSenderName()).isEqualTo(DEFAULT_SENDER_NAME);
        assertThat(testTransaction.getSenderAccount()).isEqualTo(DEFAULT_SENDER_ACCOUNT);
        assertThat(testTransaction.getSenderServiceId()).isEqualTo(DEFAULT_SENDER_SERVICE_ID);
        assertThat(testTransaction.getSenderAmount()).isEqualTo(DEFAULT_SENDER_AMOUNT);
        assertThat(testTransaction.getSenderCcy()).isEqualTo(DEFAULT_SENDER_CCY);
        assertThat(testTransaction.getSenderTrId()).isEqualTo(DEFAULT_SENDER_TR_ID);
        assertThat(testTransaction.getSenderSuccess()).isEqualTo(DEFAULT_SENDER_SUCCESS);
        assertThat(testTransaction.getSenderDate()).isEqualTo(DEFAULT_SENDER_DATE);
        assertThat(testTransaction.getSenderStatus()).isEqualTo(DEFAULT_SENDER_STATUS);
        assertThat(testTransaction.getSenderStatusText()).isEqualTo(DEFAULT_SENDER_STATUS_TEXT);
        assertThat(testTransaction.getSenderBillId()).isEqualTo(DEFAULT_SENDER_BILL_ID);
        assertThat(testTransaction.getSenderGateway()).isEqualTo(DEFAULT_SENDER_GATEWAY);
        assertThat(testTransaction.getGatewayAmount()).isEqualTo(DEFAULT_GATEWAY_AMOUNT);
        assertThat(testTransaction.getRecipientName()).isEqualTo(DEFAULT_RECIPIENT_NAME);
        assertThat(testTransaction.getRecipientAccount()).isEqualTo(DEFAULT_RECIPIENT_ACCOUNT);
        assertThat(testTransaction.getRecipientServiceId()).isEqualTo(DEFAULT_RECIPIENT_SERVICE_ID);
        assertThat(testTransaction.getRecipientAmount()).isEqualTo(DEFAULT_RECIPIENT_AMOUNT);
        assertThat(testTransaction.getRecipientCcy()).isEqualTo(DEFAULT_RECIPIENT_CCY);
        assertThat(testTransaction.getRecipientTrId()).isEqualTo(DEFAULT_RECIPIENT_TR_ID);
        assertThat(testTransaction.getRecipientSuccess()).isEqualTo(DEFAULT_RECIPIENT_SUCCESS);
        assertThat(testTransaction.getRecipientDate()).isEqualTo(DEFAULT_RECIPIENT_DATE);
        assertThat(testTransaction.getRecipientStatus()).isEqualTo(DEFAULT_RECIPIENT_STATUS);
        assertThat(testTransaction.getRecipientStatusText()).isEqualTo(DEFAULT_RECIPIENT_STATUS_TEXT);
        assertThat(testTransaction.getRecipientBillId()).isEqualTo(DEFAULT_RECIPIENT_BILL_ID);
        assertThat(testTransaction.getRecipientGateway()).isEqualTo(DEFAULT_RECIPIENT_GATEWAY);
        assertThat(testTransaction.getSystemCommissionUpAmount()).isEqualTo(DEFAULT_SYSTEM_COMMISSION_UP_AMOUNT);
        assertThat(testTransaction.getSystemCommissionDownAmount()).isEqualTo(DEFAULT_SYSTEM_COMMISSION_DOWN_AMOUNT);
        assertThat(testTransaction.getSystemFeeAmount()).isEqualTo(DEFAULT_SYSTEM_FEE_AMOUNT);
        assertThat(testTransaction.getAgentFeeAmount()).isEqualTo(DEFAULT_AGENT_FEE_AMOUNT);
        assertThat(testTransaction.getRoundAmount()).isEqualTo(DEFAULT_ROUND_AMOUNT);
        assertThat(testTransaction.getSystemFee()).isEqualTo(DEFAULT_SYSTEM_FEE);
        assertThat(testTransaction.getSystemCurrency()).isEqualTo(DEFAULT_SYSTEM_CURRENCY);
        assertThat(testTransaction.getCcyRate()).isEqualTo(DEFAULT_CCY_RATE);
        assertThat(testTransaction.getStatus()).isEqualTo(DEFAULT_STATUS);
        assertThat(testTransaction.getComment()).isEqualTo(DEFAULT_COMMENT);
        assertThat(testTransaction.getDate()).isEqualTo(DEFAULT_DATE);
        assertThat(testTransaction.getPayType()).isEqualTo(DEFAULT_PAY_TYPE);
        assertThat(testTransaction.getRequestSubAccount()).isEqualTo(DEFAULT_REQUEST_SUB_ACCOUNT);
        assertThat(testTransaction.getEposMerchant()).isEqualTo(DEFAULT_EPOS_MERCHANT);
        assertThat(testTransaction.getEposTerminal()).isEqualTo(DEFAULT_EPOS_TERMINAL);
        assertThat(testTransaction.getMain()).isEqualTo(DEFAULT_MAIN);
        assertThat(testTransaction.getAmountRatio()).isEqualTo(DEFAULT_AMOUNT_RATIO);
        assertThat(testTransaction.getSenderBean()).isEqualTo(DEFAULT_SENDER_BEAN);
    }

    @Test
    @Transactional
    public void createTransactionWithExistingId() throws Exception {
        int databaseSizeBeforeCreate = transactionRepository.findAll().size();

        // Create the Transaction with an existing ID
        transaction.setId(1L);
        TransactionDTO transactionDTO = transactionMapper.toDto(transaction);

        // An entity with an existing ID cannot be created, so this API call must fail
        restTransactionMockMvc
            .perform(
                post("/api/transactions")
                    .contentType(TestUtil.APPLICATION_JSON_UTF8)
                    .content(TestUtil.convertObjectToJsonBytes(transactionDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Transaction in the database
        List<Transaction> transactionList = transactionRepository.findAll();
        assertThat(transactionList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    public void checkRequestTrIdIsRequired() throws Exception {
        int databaseSizeBeforeTest = transactionRepository.findAll().size();
        // set the field null
        transaction.setRequestTrId(null);

        // Create the Transaction, which fails.
        TransactionDTO transactionDTO = transactionMapper.toDto(transaction);

        restTransactionMockMvc
            .perform(
                post("/api/transactions")
                    .contentType(TestUtil.APPLICATION_JSON_UTF8)
                    .content(TestUtil.convertObjectToJsonBytes(transactionDTO))
            )
            .andExpect(status().isBadRequest());

        List<Transaction> transactionList = transactionRepository.findAll();
        assertThat(transactionList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkRequestLoginIsRequired() throws Exception {
        int databaseSizeBeforeTest = transactionRepository.findAll().size();
        // set the field null
        transaction.setRequestLogin(null);

        // Create the Transaction, which fails.
        TransactionDTO transactionDTO = transactionMapper.toDto(transaction);

        restTransactionMockMvc
            .perform(
                post("/api/transactions")
                    .contentType(TestUtil.APPLICATION_JSON_UTF8)
                    .content(TestUtil.convertObjectToJsonBytes(transactionDTO))
            )
            .andExpect(status().isBadRequest());

        List<Transaction> transactionList = transactionRepository.findAll();
        assertThat(transactionList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkCommentIsRequired() throws Exception {
        int databaseSizeBeforeTest = transactionRepository.findAll().size();
        // set the field null
        transaction.setComment(null);

        // Create the Transaction, which fails.
        TransactionDTO transactionDTO = transactionMapper.toDto(transaction);

        restTransactionMockMvc
            .perform(
                post("/api/transactions")
                    .contentType(TestUtil.APPLICATION_JSON_UTF8)
                    .content(TestUtil.convertObjectToJsonBytes(transactionDTO))
            )
            .andExpect(status().isBadRequest());

        List<Transaction> transactionList = transactionRepository.findAll();
        assertThat(transactionList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkPayTypeIsRequired() throws Exception {
        int databaseSizeBeforeTest = transactionRepository.findAll().size();
        // set the field null
        transaction.setPayType(null);

        // Create the Transaction, which fails.
        TransactionDTO transactionDTO = transactionMapper.toDto(transaction);

        restTransactionMockMvc
            .perform(
                post("/api/transactions")
                    .contentType(TestUtil.APPLICATION_JSON_UTF8)
                    .content(TestUtil.convertObjectToJsonBytes(transactionDTO))
            )
            .andExpect(status().isBadRequest());

        List<Transaction> transactionList = transactionRepository.findAll();
        assertThat(transactionList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkAmountRatioIsRequired() throws Exception {
        int databaseSizeBeforeTest = transactionRepository.findAll().size();
        // set the field null
        transaction.setAmountRatio(null);

        // Create the Transaction, which fails.
        TransactionDTO transactionDTO = transactionMapper.toDto(transaction);

        restTransactionMockMvc
            .perform(
                post("/api/transactions")
                    .contentType(TestUtil.APPLICATION_JSON_UTF8)
                    .content(TestUtil.convertObjectToJsonBytes(transactionDTO))
            )
            .andExpect(status().isBadRequest());

        List<Transaction> transactionList = transactionRepository.findAll();
        assertThat(transactionList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void getAllTransactions() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList
        restTransactionMockMvc
            .perform(get("/api/transactions?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(transaction.getId().intValue())))
            .andExpect(jsonPath("$.[*].requestAmount").value(hasItem(DEFAULT_REQUEST_AMOUNT.intValue())))
            .andExpect(jsonPath("$.[*].requestCcy").value(hasItem(DEFAULT_REQUEST_CCY)))
            .andExpect(jsonPath("$.[*].requestTrId").value(hasItem(DEFAULT_REQUEST_TR_ID.toString())))
            .andExpect(jsonPath("$.[*].requestLogin").value(hasItem(DEFAULT_REQUEST_LOGIN.toString())))
            .andExpect(jsonPath("$.[*].requestGroup").value(hasItem(DEFAULT_REQUEST_GROUP.toString())))
            .andExpect(jsonPath("$.[*].senderName").value(hasItem(DEFAULT_SENDER_NAME.toString())))
            .andExpect(jsonPath("$.[*].senderAccount").value(hasItem(DEFAULT_SENDER_ACCOUNT.toString())))
            .andExpect(jsonPath("$.[*].senderServiceId").value(hasItem(DEFAULT_SENDER_SERVICE_ID.intValue())))
            .andExpect(jsonPath("$.[*].senderAmount").value(hasItem(DEFAULT_SENDER_AMOUNT.intValue())))
            .andExpect(jsonPath("$.[*].senderCcy").value(hasItem(DEFAULT_SENDER_CCY)))
            .andExpect(jsonPath("$.[*].senderTrId").value(hasItem(DEFAULT_SENDER_TR_ID.toString())))
            .andExpect(jsonPath("$.[*].senderSuccess").value(hasItem(DEFAULT_SENDER_SUCCESS.booleanValue())))
            .andExpect(jsonPath("$.[*].senderDate").value(hasItem(DEFAULT_SENDER_DATE.toString())))
            .andExpect(jsonPath("$.[*].senderStatus").value(hasItem(DEFAULT_SENDER_STATUS.toString())))
            .andExpect(jsonPath("$.[*].senderStatusText").value(hasItem(DEFAULT_SENDER_STATUS_TEXT.toString())))
            .andExpect(jsonPath("$.[*].senderBillId").value(hasItem(DEFAULT_SENDER_BILL_ID.intValue())))
            .andExpect(jsonPath("$.[*].senderGateway").value(hasItem(DEFAULT_SENDER_GATEWAY.toString())))
            .andExpect(jsonPath("$.[*].gatewayAmount").value(hasItem(DEFAULT_GATEWAY_AMOUNT.doubleValue())))
            .andExpect(jsonPath("$.[*].recipientName").value(hasItem(DEFAULT_RECIPIENT_NAME.toString())))
            .andExpect(jsonPath("$.[*].recipientAccount").value(hasItem(DEFAULT_RECIPIENT_ACCOUNT.toString())))
            .andExpect(jsonPath("$.[*].recipientServiceId").value(hasItem(DEFAULT_RECIPIENT_SERVICE_ID.intValue())))
            .andExpect(jsonPath("$.[*].recipientAmount").value(hasItem(DEFAULT_RECIPIENT_AMOUNT.intValue())))
            .andExpect(jsonPath("$.[*].recipientCcy").value(hasItem(DEFAULT_RECIPIENT_CCY)))
            .andExpect(jsonPath("$.[*].recipientTrId").value(hasItem(DEFAULT_RECIPIENT_TR_ID.toString())))
            .andExpect(jsonPath("$.[*].recipientSuccess").value(hasItem(DEFAULT_RECIPIENT_SUCCESS.booleanValue())))
            .andExpect(jsonPath("$.[*].recipientDate").value(hasItem(DEFAULT_RECIPIENT_DATE.toString())))
            .andExpect(jsonPath("$.[*].recipientStatus").value(hasItem(DEFAULT_RECIPIENT_STATUS.toString())))
            .andExpect(jsonPath("$.[*].recipientStatusText").value(hasItem(DEFAULT_RECIPIENT_STATUS_TEXT.toString())))
            .andExpect(jsonPath("$.[*].recipientBillId").value(hasItem(DEFAULT_RECIPIENT_BILL_ID.intValue())))
            .andExpect(jsonPath("$.[*].recipientGateway").value(hasItem(DEFAULT_RECIPIENT_GATEWAY.toString())))
            .andExpect(jsonPath("$.[*].systemCommissionUpAmount").value(hasItem(DEFAULT_SYSTEM_COMMISSION_UP_AMOUNT.intValue())))
            .andExpect(jsonPath("$.[*].systemCommissionDownAmount").value(hasItem(DEFAULT_SYSTEM_COMMISSION_DOWN_AMOUNT.intValue())))
            .andExpect(jsonPath("$.[*].systemFeeAmount").value(hasItem(DEFAULT_SYSTEM_FEE_AMOUNT.intValue())))
            .andExpect(jsonPath("$.[*].agentFeeAmount").value(hasItem(DEFAULT_AGENT_FEE_AMOUNT.intValue())))
            .andExpect(jsonPath("$.[*].roundAmount").value(hasItem(DEFAULT_ROUND_AMOUNT.doubleValue())))
            .andExpect(jsonPath("$.[*].systemFee").value(hasItem(DEFAULT_SYSTEM_FEE.doubleValue())))
            .andExpect(jsonPath("$.[*].systemCurrency").value(hasItem(DEFAULT_SYSTEM_CURRENCY)))
            .andExpect(jsonPath("$.[*].ccyRate").value(hasItem(DEFAULT_CCY_RATE)))
            .andExpect(jsonPath("$.[*].status").value(hasItem(DEFAULT_STATUS.toString())))
            .andExpect(jsonPath("$.[*].comment").value(hasItem(DEFAULT_COMMENT.toString())))
            .andExpect(jsonPath("$.[*].date").value(hasItem(DEFAULT_DATE.toString())))
            .andExpect(jsonPath("$.[*].payType").value(hasItem(DEFAULT_PAY_TYPE.toString())))
            .andExpect(jsonPath("$.[*].requestSubAccount").value(hasItem(DEFAULT_REQUEST_SUB_ACCOUNT.toString())))
            .andExpect(jsonPath("$.[*].eposMerchant").value(hasItem(DEFAULT_EPOS_MERCHANT.toString())))
            .andExpect(jsonPath("$.[*].eposTerminal").value(hasItem(DEFAULT_EPOS_TERMINAL.toString())))
            .andExpect(jsonPath("$.[*].main").value(hasItem(DEFAULT_MAIN.booleanValue())))
            .andExpect(jsonPath("$.[*].amountRatio").value(hasItem(DEFAULT_AMOUNT_RATIO)))
            .andExpect(jsonPath("$.[*].senderBean").value(hasItem(DEFAULT_SENDER_BEAN.toString())));
    }

    @Test
    @Transactional
    public void getTransaction() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get the transaction
        restTransactionMockMvc
            .perform(get("/api/transactions/{id}", transaction.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.id").value(transaction.getId().intValue()))
            .andExpect(jsonPath("$.requestAmount").value(DEFAULT_REQUEST_AMOUNT.intValue()))
            .andExpect(jsonPath("$.requestCcy").value(DEFAULT_REQUEST_CCY))
            .andExpect(jsonPath("$.requestTrId").value(DEFAULT_REQUEST_TR_ID.toString()))
            .andExpect(jsonPath("$.requestLogin").value(DEFAULT_REQUEST_LOGIN.toString()))
            .andExpect(jsonPath("$.requestGroup").value(DEFAULT_REQUEST_GROUP.toString()))
            .andExpect(jsonPath("$.senderName").value(DEFAULT_SENDER_NAME.toString()))
            .andExpect(jsonPath("$.senderAccount").value(DEFAULT_SENDER_ACCOUNT.toString()))
            .andExpect(jsonPath("$.senderServiceId").value(DEFAULT_SENDER_SERVICE_ID.intValue()))
            .andExpect(jsonPath("$.senderAmount").value(DEFAULT_SENDER_AMOUNT.intValue()))
            .andExpect(jsonPath("$.senderCcy").value(DEFAULT_SENDER_CCY))
            .andExpect(jsonPath("$.senderTrId").value(DEFAULT_SENDER_TR_ID.toString()))
            .andExpect(jsonPath("$.senderSuccess").value(DEFAULT_SENDER_SUCCESS.booleanValue()))
            .andExpect(jsonPath("$.senderDate").value(DEFAULT_SENDER_DATE.toString()))
            .andExpect(jsonPath("$.senderStatus").value(DEFAULT_SENDER_STATUS.toString()))
            .andExpect(jsonPath("$.senderStatusText").value(DEFAULT_SENDER_STATUS_TEXT.toString()))
            .andExpect(jsonPath("$.senderBillId").value(DEFAULT_SENDER_BILL_ID.intValue()))
            .andExpect(jsonPath("$.senderGateway").value(DEFAULT_SENDER_GATEWAY.toString()))
            .andExpect(jsonPath("$.gatewayAmount").value(DEFAULT_GATEWAY_AMOUNT.doubleValue()))
            .andExpect(jsonPath("$.recipientName").value(DEFAULT_RECIPIENT_NAME.toString()))
            .andExpect(jsonPath("$.recipientAccount").value(DEFAULT_RECIPIENT_ACCOUNT.toString()))
            .andExpect(jsonPath("$.recipientServiceId").value(DEFAULT_RECIPIENT_SERVICE_ID.intValue()))
            .andExpect(jsonPath("$.recipientAmount").value(DEFAULT_RECIPIENT_AMOUNT.intValue()))
            .andExpect(jsonPath("$.recipientCcy").value(DEFAULT_RECIPIENT_CCY))
            .andExpect(jsonPath("$.recipientTrId").value(DEFAULT_RECIPIENT_TR_ID.toString()))
            .andExpect(jsonPath("$.recipientSuccess").value(DEFAULT_RECIPIENT_SUCCESS.booleanValue()))
            .andExpect(jsonPath("$.recipientDate").value(DEFAULT_RECIPIENT_DATE.toString()))
            .andExpect(jsonPath("$.recipientStatus").value(DEFAULT_RECIPIENT_STATUS.toString()))
            .andExpect(jsonPath("$.recipientStatusText").value(DEFAULT_RECIPIENT_STATUS_TEXT.toString()))
            .andExpect(jsonPath("$.recipientBillId").value(DEFAULT_RECIPIENT_BILL_ID.intValue()))
            .andExpect(jsonPath("$.recipientGateway").value(DEFAULT_RECIPIENT_GATEWAY.toString()))
            .andExpect(jsonPath("$.systemCommissionUpAmount").value(DEFAULT_SYSTEM_COMMISSION_UP_AMOUNT.intValue()))
            .andExpect(jsonPath("$.systemCommissionDownAmount").value(DEFAULT_SYSTEM_COMMISSION_DOWN_AMOUNT.intValue()))
            .andExpect(jsonPath("$.systemFeeAmount").value(DEFAULT_SYSTEM_FEE_AMOUNT.intValue()))
            .andExpect(jsonPath("$.agentFeeAmount").value(DEFAULT_AGENT_FEE_AMOUNT.intValue()))
            .andExpect(jsonPath("$.roundAmount").value(DEFAULT_ROUND_AMOUNT.doubleValue()))
            .andExpect(jsonPath("$.systemFee").value(DEFAULT_SYSTEM_FEE.doubleValue()))
            .andExpect(jsonPath("$.systemCurrency").value(DEFAULT_SYSTEM_CURRENCY))
            .andExpect(jsonPath("$.ccyRate").value(DEFAULT_CCY_RATE))
            .andExpect(jsonPath("$.status").value(DEFAULT_STATUS.toString()))
            .andExpect(jsonPath("$.comment").value(DEFAULT_COMMENT.toString()))
            .andExpect(jsonPath("$.date").value(DEFAULT_DATE.toString()))
            .andExpect(jsonPath("$.payType").value(DEFAULT_PAY_TYPE.toString()))
            .andExpect(jsonPath("$.requestSubAccount").value(DEFAULT_REQUEST_SUB_ACCOUNT.toString()))
            .andExpect(jsonPath("$.eposMerchant").value(DEFAULT_EPOS_MERCHANT.toString()))
            .andExpect(jsonPath("$.eposTerminal").value(DEFAULT_EPOS_TERMINAL.toString()))
            .andExpect(jsonPath("$.main").value(DEFAULT_MAIN.booleanValue()))
            .andExpect(jsonPath("$.amountRatio").value(DEFAULT_AMOUNT_RATIO))
            .andExpect(jsonPath("$.senderBean").value(DEFAULT_SENDER_BEAN.toString()));
    }

    @Test
    @Transactional
    public void getTransactionWithUzcardAccount() throws Exception {
        // Initialize the database
        transaction.setRecipientAccount(UZCARD_RECIPIENT_ACCOUNT);
        transactionRepository.saveAndFlush(transaction);

        // Get the transaction
        restTransactionMockMvc
            .perform(get("/api/transactions/{id}", transaction.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.id").value(transaction.getId().intValue()))
            .andExpect(jsonPath("$.requestAmount").value(DEFAULT_REQUEST_AMOUNT.intValue()))
            .andExpect(jsonPath("$.requestCcy").value(DEFAULT_REQUEST_CCY))
            .andExpect(jsonPath("$.requestTrId").value(DEFAULT_REQUEST_TR_ID.toString()))
            .andExpect(jsonPath("$.requestLogin").value(DEFAULT_REQUEST_LOGIN.toString()))
            .andExpect(jsonPath("$.requestGroup").value(DEFAULT_REQUEST_GROUP.toString()))
            .andExpect(jsonPath("$.senderName").value(DEFAULT_SENDER_NAME.toString()))
            .andExpect(jsonPath("$.senderAccount").value(DEFAULT_SENDER_ACCOUNT.toString()))
            .andExpect(jsonPath("$.senderServiceId").value(DEFAULT_SENDER_SERVICE_ID.intValue()))
            .andExpect(jsonPath("$.senderAmount").value(DEFAULT_SENDER_AMOUNT.intValue()))
            .andExpect(jsonPath("$.senderCcy").value(DEFAULT_SENDER_CCY))
            .andExpect(jsonPath("$.senderTrId").value(DEFAULT_SENDER_TR_ID.toString()))
            .andExpect(jsonPath("$.senderSuccess").value(DEFAULT_SENDER_SUCCESS.booleanValue()))
            .andExpect(jsonPath("$.senderDate").value(DEFAULT_SENDER_DATE.toString()))
            .andExpect(jsonPath("$.senderStatus").value(DEFAULT_SENDER_STATUS.toString()))
            .andExpect(jsonPath("$.senderStatusText").value(DEFAULT_SENDER_STATUS_TEXT.toString()))
            .andExpect(jsonPath("$.senderBillId").value(DEFAULT_SENDER_BILL_ID.intValue()))
            .andExpect(jsonPath("$.senderGateway").value(DEFAULT_SENDER_GATEWAY.toString()))
            .andExpect(jsonPath("$.gatewayAmount").value(DEFAULT_GATEWAY_AMOUNT.doubleValue()))
            .andExpect(jsonPath("$.recipientName").value(DEFAULT_RECIPIENT_NAME.toString()))
            .andExpect(jsonPath("$.recipientAccount").value(CardUtil.mask(UZCARD_RECIPIENT_ACCOUNT).toString()))
            .andExpect(jsonPath("$.recipientServiceId").value(DEFAULT_RECIPIENT_SERVICE_ID.intValue()))
            .andExpect(jsonPath("$.recipientAmount").value(DEFAULT_RECIPIENT_AMOUNT.intValue()))
            .andExpect(jsonPath("$.recipientCcy").value(DEFAULT_RECIPIENT_CCY))
            .andExpect(jsonPath("$.recipientTrId").value(DEFAULT_RECIPIENT_TR_ID.toString()))
            .andExpect(jsonPath("$.recipientSuccess").value(DEFAULT_RECIPIENT_SUCCESS.booleanValue()))
            .andExpect(jsonPath("$.recipientDate").value(DEFAULT_RECIPIENT_DATE.toString()))
            .andExpect(jsonPath("$.recipientStatus").value(DEFAULT_RECIPIENT_STATUS.toString()))
            .andExpect(jsonPath("$.recipientStatusText").value(DEFAULT_RECIPIENT_STATUS_TEXT.toString()))
            .andExpect(jsonPath("$.recipientBillId").value(DEFAULT_RECIPIENT_BILL_ID.intValue()))
            .andExpect(jsonPath("$.recipientGateway").value(DEFAULT_RECIPIENT_GATEWAY.toString()))
            .andExpect(jsonPath("$.systemCommissionUpAmount").value(DEFAULT_SYSTEM_COMMISSION_UP_AMOUNT.intValue()))
            .andExpect(jsonPath("$.systemCommissionDownAmount").value(DEFAULT_SYSTEM_COMMISSION_DOWN_AMOUNT.intValue()))
            .andExpect(jsonPath("$.systemFeeAmount").value(DEFAULT_SYSTEM_FEE_AMOUNT.intValue()))
            .andExpect(jsonPath("$.agentFeeAmount").value(DEFAULT_AGENT_FEE_AMOUNT.intValue()))
            .andExpect(jsonPath("$.roundAmount").value(DEFAULT_ROUND_AMOUNT.doubleValue()))
            .andExpect(jsonPath("$.systemFee").value(DEFAULT_SYSTEM_FEE.doubleValue()))
            .andExpect(jsonPath("$.systemCurrency").value(DEFAULT_SYSTEM_CURRENCY))
            .andExpect(jsonPath("$.ccyRate").value(DEFAULT_CCY_RATE))
            .andExpect(jsonPath("$.status").value(DEFAULT_STATUS.toString()))
            .andExpect(jsonPath("$.comment").value(DEFAULT_COMMENT.toString()))
            .andExpect(jsonPath("$.date").value(DEFAULT_DATE.toString()))
            .andExpect(jsonPath("$.payType").value(DEFAULT_PAY_TYPE.toString()))
            .andExpect(jsonPath("$.requestSubAccount").value(DEFAULT_REQUEST_SUB_ACCOUNT.toString()))
            .andExpect(jsonPath("$.eposMerchant").value(DEFAULT_EPOS_MERCHANT.toString()))
            .andExpect(jsonPath("$.eposTerminal").value(DEFAULT_EPOS_TERMINAL.toString()));
    }

    @Test
    @Transactional
    public void getAllTransactionsByRequestAmountIsEqualToSomething() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where requestAmount equals to DEFAULT_REQUEST_AMOUNT
        defaultTransactionShouldBeFound("requestAmount.equals=" + DEFAULT_REQUEST_AMOUNT);

        // Get all the transactionList where requestAmount equals to UPDATED_REQUEST_AMOUNT
        defaultTransactionShouldNotBeFound("requestAmount.equals=" + UPDATED_REQUEST_AMOUNT);
    }

    @Test
    @Transactional
    public void getAllTransactionsByRequestAmountIsInShouldWork() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where requestAmount in DEFAULT_REQUEST_AMOUNT or UPDATED_REQUEST_AMOUNT
        defaultTransactionShouldBeFound("requestAmount.in=" + DEFAULT_REQUEST_AMOUNT + "," + UPDATED_REQUEST_AMOUNT);

        // Get all the transactionList where requestAmount equals to UPDATED_REQUEST_AMOUNT
        defaultTransactionShouldNotBeFound("requestAmount.in=" + UPDATED_REQUEST_AMOUNT);
    }

    @Test
    @Transactional
    public void getAllTransactionsByRequestAmountIsNullOrNotNull() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where requestAmount is not null
        defaultTransactionShouldBeFound("requestAmount.specified=true");

        // Get all the transactionList where requestAmount is null
        defaultTransactionShouldNotBeFound("requestAmount.specified=false");
    }

    @Test
    @Transactional
    public void getAllTransactionsByRequestAmountIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where requestAmount greater than or equals to DEFAULT_REQUEST_AMOUNT
        defaultTransactionShouldBeFound("requestAmount.greaterOrEqualThan=" + DEFAULT_REQUEST_AMOUNT);

        // Get all the transactionList where requestAmount greater than or equals to UPDATED_REQUEST_AMOUNT
        defaultTransactionShouldNotBeFound("requestAmount.greaterOrEqualThan=" + UPDATED_REQUEST_AMOUNT);
    }

    @Test
    @Transactional
    public void getAllTransactionsByRequestAmountIsLessThanSomething() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where requestAmount less than or equals to DEFAULT_REQUEST_AMOUNT
        defaultTransactionShouldNotBeFound("requestAmount.lessThan=" + DEFAULT_REQUEST_AMOUNT);

        // Get all the transactionList where requestAmount less than or equals to UPDATED_REQUEST_AMOUNT
        defaultTransactionShouldBeFound("requestAmount.lessThan=" + UPDATED_REQUEST_AMOUNT);
    }

    @Test
    @Transactional
    public void getAllTransactionsByRequestCcyIsEqualToSomething() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where requestCcy equals to DEFAULT_REQUEST_CCY
        defaultTransactionShouldBeFound("requestCcy.equals=" + DEFAULT_REQUEST_CCY);

        // Get all the transactionList where requestCcy equals to UPDATED_REQUEST_CCY
        defaultTransactionShouldNotBeFound("requestCcy.equals=" + UPDATED_REQUEST_CCY);
    }

    @Test
    @Transactional
    public void getAllTransactionsByRequestCcyIsInShouldWork() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where requestCcy in DEFAULT_REQUEST_CCY or UPDATED_REQUEST_CCY
        defaultTransactionShouldBeFound("requestCcy.in=" + DEFAULT_REQUEST_CCY + "," + UPDATED_REQUEST_CCY);

        // Get all the transactionList where requestCcy equals to UPDATED_REQUEST_CCY
        defaultTransactionShouldNotBeFound("requestCcy.in=" + UPDATED_REQUEST_CCY);
    }

    @Test
    @Transactional
    public void getAllTransactionsByRequestCcyIsNullOrNotNull() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where requestCcy is not null
        defaultTransactionShouldBeFound("requestCcy.specified=true");

        // Get all the transactionList where requestCcy is null
        defaultTransactionShouldNotBeFound("requestCcy.specified=false");
    }

    @Test
    @Transactional
    public void getAllTransactionsByRequestCcyIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where requestCcy greater than or equals to DEFAULT_REQUEST_CCY
        defaultTransactionShouldBeFound("requestCcy.greaterOrEqualThan=" + DEFAULT_REQUEST_CCY);

        // Get all the transactionList where requestCcy greater than or equals to UPDATED_REQUEST_CCY
        defaultTransactionShouldNotBeFound("requestCcy.greaterOrEqualThan=" + UPDATED_REQUEST_CCY);
    }

    @Test
    @Transactional
    public void getAllTransactionsByRequestCcyIsLessThanSomething() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where requestCcy less than or equals to DEFAULT_REQUEST_CCY
        defaultTransactionShouldNotBeFound("requestCcy.lessThan=" + DEFAULT_REQUEST_CCY);

        // Get all the transactionList where requestCcy less than or equals to UPDATED_REQUEST_CCY
        defaultTransactionShouldBeFound("requestCcy.lessThan=" + UPDATED_REQUEST_CCY);
    }

    @Test
    @Transactional
    public void getAllTransactionsByRequestTrIdIsEqualToSomething() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where requestTrId equals to DEFAULT_REQUEST_TR_ID
        defaultTransactionShouldBeFound("requestTrId.equals=" + DEFAULT_REQUEST_TR_ID);

        // Get all the transactionList where requestTrId equals to UPDATED_REQUEST_TR_ID
        defaultTransactionShouldNotBeFound("requestTrId.equals=" + UPDATED_REQUEST_TR_ID);
    }

    @Test
    @Transactional
    public void getAllTransactionsByRequestTrIdIsInShouldWork() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where requestTrId in DEFAULT_REQUEST_TR_ID or UPDATED_REQUEST_TR_ID
        defaultTransactionShouldBeFound("requestTrId.in=" + DEFAULT_REQUEST_TR_ID + "," + UPDATED_REQUEST_TR_ID);

        // Get all the transactionList where requestTrId equals to UPDATED_REQUEST_TR_ID
        defaultTransactionShouldNotBeFound("requestTrId.in=" + UPDATED_REQUEST_TR_ID);
    }

    @Test
    @Transactional
    public void getAllTransactionsByRequestTrIdIsNullOrNotNull() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where requestTrId is not null
        defaultTransactionShouldBeFound("requestTrId.specified=true");

        // Get all the transactionList where requestTrId is null
        defaultTransactionShouldNotBeFound("requestTrId.specified=false");
    }

    @Test
    @Transactional
    public void getAllTransactionsByRequestLoginIsEqualToSomething() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where requestLogin equals to DEFAULT_REQUEST_LOGIN
        defaultTransactionShouldBeFound("requestLogin.equals=" + DEFAULT_REQUEST_LOGIN);

        // Get all the transactionList where requestLogin equals to UPDATED_REQUEST_LOGIN
        defaultTransactionShouldNotBeFound("requestLogin.equals=" + UPDATED_REQUEST_LOGIN);
    }

    @Test
    @Transactional
    public void getAllTransactionsByRequestLoginIsInShouldWork() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where requestLogin in DEFAULT_REQUEST_LOGIN or UPDATED_REQUEST_LOGIN
        defaultTransactionShouldBeFound("requestLogin.in=" + DEFAULT_REQUEST_LOGIN + "," + UPDATED_REQUEST_LOGIN);

        // Get all the transactionList where requestLogin equals to UPDATED_REQUEST_LOGIN
        defaultTransactionShouldNotBeFound("requestLogin.in=" + UPDATED_REQUEST_LOGIN);
    }

    @Test
    @Transactional
    public void getAllTransactionsByRequestLoginIsNullOrNotNull() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where requestLogin is not null
        defaultTransactionShouldBeFound("requestLogin.specified=true");

        // Get all the transactionList where requestLogin is null
        defaultTransactionShouldNotBeFound("requestLogin.specified=false");
    }

    @Test
    @Transactional
    public void getAllTransactionsByRequestGroupIsEqualToSomething() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where requestGroup equals to DEFAULT_REQUEST_GROUP
        defaultTransactionShouldBeFound("requestGroup.equals=" + DEFAULT_REQUEST_GROUP);

        // Get all the transactionList where requestGroup equals to UPDATED_REQUEST_GROUP
        defaultTransactionShouldNotBeFound("requestGroup.equals=" + UPDATED_REQUEST_GROUP);
    }

    @Test
    @Transactional
    public void getAllTransactionsByRequestGroupIsInShouldWork() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where requestGroup in DEFAULT_REQUEST_GROUP or UPDATED_REQUEST_GROUP
        defaultTransactionShouldBeFound("requestGroup.in=" + DEFAULT_REQUEST_GROUP + "," + UPDATED_REQUEST_GROUP);

        // Get all the transactionList where requestGroup equals to UPDATED_REQUEST_GROUP
        defaultTransactionShouldNotBeFound("requestGroup.in=" + UPDATED_REQUEST_GROUP);
    }

    @Test
    @Transactional
    public void getAllTransactionsByRequestGroupIsNullOrNotNull() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where requestGroup is not null
        defaultTransactionShouldBeFound("requestGroup.specified=true");

        // Get all the transactionList where requestGroup is null
        defaultTransactionShouldNotBeFound("requestGroup.specified=false");
    }

    @Test
    @Transactional
    public void getAllTransactionsBySenderNameIsEqualToSomething() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where senderName equals to DEFAULT_SENDER_NAME
        defaultTransactionShouldBeFound("senderName.equals=" + DEFAULT_SENDER_NAME);

        // Get all the transactionList where senderName equals to UPDATED_SENDER_NAME
        defaultTransactionShouldNotBeFound("senderName.equals=" + UPDATED_SENDER_NAME);
    }

    @Test
    @Transactional
    public void getAllTransactionsBySenderNameIsInShouldWork() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where senderName in DEFAULT_SENDER_NAME or UPDATED_SENDER_NAME
        defaultTransactionShouldBeFound("senderName.in=" + DEFAULT_SENDER_NAME + "," + UPDATED_SENDER_NAME);

        // Get all the transactionList where senderName equals to UPDATED_SENDER_NAME
        defaultTransactionShouldNotBeFound("senderName.in=" + UPDATED_SENDER_NAME);
    }

    @Test
    @Transactional
    public void getAllTransactionsBySenderNameIsNullOrNotNull() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where senderName is not null
        defaultTransactionShouldBeFound("senderName.specified=true");

        // Get all the transactionList where senderName is null
        defaultTransactionShouldNotBeFound("senderName.specified=false");
    }

    @Test
    @Transactional
    public void getAllTransactionsBySenderAccountIsEqualToSomething() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where senderAccount equals to DEFAULT_SENDER_ACCOUNT
        defaultTransactionShouldBeFound("senderAccount.equals=" + DEFAULT_SENDER_ACCOUNT);

        // Get all the transactionList where senderAccount equals to UPDATED_SENDER_ACCOUNT
        defaultTransactionShouldNotBeFound("senderAccount.equals=" + UPDATED_SENDER_ACCOUNT);
    }

    @Test
    @Transactional
    public void getAllTransactionsBySenderAccountIsInShouldWork() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where senderAccount in DEFAULT_SENDER_ACCOUNT or UPDATED_SENDER_ACCOUNT
        defaultTransactionShouldBeFound("senderAccount.in=" + DEFAULT_SENDER_ACCOUNT + "," + UPDATED_SENDER_ACCOUNT);

        // Get all the transactionList where senderAccount equals to UPDATED_SENDER_ACCOUNT
        defaultTransactionShouldNotBeFound("senderAccount.in=" + UPDATED_SENDER_ACCOUNT);
    }

    @Test
    @Transactional
    public void getAllTransactionsBySenderAccountIsNullOrNotNull() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where senderAccount is not null
        defaultTransactionShouldBeFound("senderAccount.specified=true");

        // Get all the transactionList where senderAccount is null
        defaultTransactionShouldNotBeFound("senderAccount.specified=false");
    }

    @Test
    @Transactional
    public void getAllTransactionsBySenderServiceIdIsEqualToSomething() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where senderServiceId equals to DEFAULT_SENDER_SERVICE_ID
        defaultTransactionShouldBeFound("senderServiceId.equals=" + DEFAULT_SENDER_SERVICE_ID);

        // Get all the transactionList where senderServiceId equals to UPDATED_SENDER_SERVICE_ID
        defaultTransactionShouldNotBeFound("senderServiceId.equals=" + UPDATED_SENDER_SERVICE_ID);
    }

    @Test
    @Transactional
    public void getAllTransactionsBySenderServiceIdIsInShouldWork() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where senderServiceId in DEFAULT_SENDER_SERVICE_ID or UPDATED_SENDER_SERVICE_ID
        defaultTransactionShouldBeFound("senderServiceId.in=" + DEFAULT_SENDER_SERVICE_ID + "," + UPDATED_SENDER_SERVICE_ID);

        // Get all the transactionList where senderServiceId equals to UPDATED_SENDER_SERVICE_ID
        defaultTransactionShouldNotBeFound("senderServiceId.in=" + UPDATED_SENDER_SERVICE_ID);
    }

    @Test
    @Transactional
    public void getAllTransactionsBySenderServiceIdIsNullOrNotNull() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where senderServiceId is not null
        defaultTransactionShouldBeFound("senderServiceId.specified=true");

        // Get all the transactionList where senderServiceId is null
        defaultTransactionShouldNotBeFound("senderServiceId.specified=false");
    }

    @Test
    @Transactional
    public void getAllTransactionsBySenderServiceIdIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where senderServiceId greater than or equals to DEFAULT_SENDER_SERVICE_ID
        defaultTransactionShouldBeFound("senderServiceId.greaterOrEqualThan=" + DEFAULT_SENDER_SERVICE_ID);

        // Get all the transactionList where senderServiceId greater than or equals to UPDATED_SENDER_SERVICE_ID
        defaultTransactionShouldNotBeFound("senderServiceId.greaterOrEqualThan=" + UPDATED_SENDER_SERVICE_ID);
    }

    @Test
    @Transactional
    public void getAllTransactionsBySenderServiceIdIsLessThanSomething() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where senderServiceId less than or equals to DEFAULT_SENDER_SERVICE_ID
        defaultTransactionShouldNotBeFound("senderServiceId.lessThan=" + DEFAULT_SENDER_SERVICE_ID);

        // Get all the transactionList where senderServiceId less than or equals to UPDATED_SENDER_SERVICE_ID
        defaultTransactionShouldBeFound("senderServiceId.lessThan=" + UPDATED_SENDER_SERVICE_ID);
    }

    @Test
    @Transactional
    public void getAllTransactionsBySenderAmountIsEqualToSomething() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where senderAmount equals to DEFAULT_SENDER_AMOUNT
        defaultTransactionShouldBeFound("senderAmount.equals=" + DEFAULT_SENDER_AMOUNT);

        // Get all the transactionList where senderAmount equals to UPDATED_SENDER_AMOUNT
        defaultTransactionShouldNotBeFound("senderAmount.equals=" + UPDATED_SENDER_AMOUNT);
    }

    @Test
    @Transactional
    public void getAllTransactionsBySenderAmountIsInShouldWork() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where senderAmount in DEFAULT_SENDER_AMOUNT or UPDATED_SENDER_AMOUNT
        defaultTransactionShouldBeFound("senderAmount.in=" + DEFAULT_SENDER_AMOUNT + "," + UPDATED_SENDER_AMOUNT);

        // Get all the transactionList where senderAmount equals to UPDATED_SENDER_AMOUNT
        defaultTransactionShouldNotBeFound("senderAmount.in=" + UPDATED_SENDER_AMOUNT);
    }

    @Test
    @Transactional
    public void getAllTransactionsBySenderAmountIsNullOrNotNull() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where senderAmount is not null
        defaultTransactionShouldBeFound("senderAmount.specified=true");

        // Get all the transactionList where senderAmount is null
        defaultTransactionShouldNotBeFound("senderAmount.specified=false");
    }

    @Test
    @Transactional
    public void getAllTransactionsBySenderAmountIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where senderAmount greater than or equals to DEFAULT_SENDER_AMOUNT
        defaultTransactionShouldBeFound("senderAmount.greaterOrEqualThan=" + DEFAULT_SENDER_AMOUNT);

        // Get all the transactionList where senderAmount greater than or equals to UPDATED_SENDER_AMOUNT
        defaultTransactionShouldNotBeFound("senderAmount.greaterOrEqualThan=" + UPDATED_SENDER_AMOUNT);
    }

    @Test
    @Transactional
    public void getAllTransactionsBySenderAmountIsLessThanSomething() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where senderAmount less than or equals to DEFAULT_SENDER_AMOUNT
        defaultTransactionShouldNotBeFound("senderAmount.lessThan=" + DEFAULT_SENDER_AMOUNT);

        // Get all the transactionList where senderAmount less than or equals to UPDATED_SENDER_AMOUNT
        defaultTransactionShouldBeFound("senderAmount.lessThan=" + UPDATED_SENDER_AMOUNT);
    }

    @Test
    @Transactional
    public void getAllTransactionsBySenderCcyIsEqualToSomething() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where senderCcy equals to DEFAULT_SENDER_CCY
        defaultTransactionShouldBeFound("senderCcy.equals=" + DEFAULT_SENDER_CCY);

        // Get all the transactionList where senderCcy equals to UPDATED_SENDER_CCY
        defaultTransactionShouldNotBeFound("senderCcy.equals=" + UPDATED_SENDER_CCY);
    }

    @Test
    @Transactional
    public void getAllTransactionsBySenderCcyIsInShouldWork() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where senderCcy in DEFAULT_SENDER_CCY or UPDATED_SENDER_CCY
        defaultTransactionShouldBeFound("senderCcy.in=" + DEFAULT_SENDER_CCY + "," + UPDATED_SENDER_CCY);

        // Get all the transactionList where senderCcy equals to UPDATED_SENDER_CCY
        defaultTransactionShouldNotBeFound("senderCcy.in=" + UPDATED_SENDER_CCY);
    }

    @Test
    @Transactional
    public void getAllTransactionsBySenderCcyIsNullOrNotNull() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where senderCcy is not null
        defaultTransactionShouldBeFound("senderCcy.specified=true");

        // Get all the transactionList where senderCcy is null
        defaultTransactionShouldNotBeFound("senderCcy.specified=false");
    }

    @Test
    @Transactional
    public void getAllTransactionsBySenderCcyIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where senderCcy greater than or equals to DEFAULT_SENDER_CCY
        defaultTransactionShouldBeFound("senderCcy.greaterOrEqualThan=" + DEFAULT_SENDER_CCY);

        // Get all the transactionList where senderCcy greater than or equals to UPDATED_SENDER_CCY
        defaultTransactionShouldNotBeFound("senderCcy.greaterOrEqualThan=" + UPDATED_SENDER_CCY);
    }

    @Test
    @Transactional
    public void getAllTransactionsBySenderCcyIsLessThanSomething() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where senderCcy less than or equals to DEFAULT_SENDER_CCY
        defaultTransactionShouldNotBeFound("senderCcy.lessThan=" + DEFAULT_SENDER_CCY);

        // Get all the transactionList where senderCcy less than or equals to UPDATED_SENDER_CCY
        defaultTransactionShouldBeFound("senderCcy.lessThan=" + UPDATED_SENDER_CCY);
    }

    @Test
    @Transactional
    public void getAllTransactionsBySenderTrIdIsEqualToSomething() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where senderTrId equals to DEFAULT_SENDER_TR_ID
        defaultTransactionShouldBeFound("senderTrId.equals=" + DEFAULT_SENDER_TR_ID);

        // Get all the transactionList where senderTrId equals to UPDATED_SENDER_TR_ID
        defaultTransactionShouldNotBeFound("senderTrId.equals=" + UPDATED_SENDER_TR_ID);
    }

    @Test
    @Transactional
    public void getAllTransactionsBySenderTrIdIsInShouldWork() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where senderTrId in DEFAULT_SENDER_TR_ID or UPDATED_SENDER_TR_ID
        defaultTransactionShouldBeFound("senderTrId.in=" + DEFAULT_SENDER_TR_ID + "," + UPDATED_SENDER_TR_ID);

        // Get all the transactionList where senderTrId equals to UPDATED_SENDER_TR_ID
        defaultTransactionShouldNotBeFound("senderTrId.in=" + UPDATED_SENDER_TR_ID);
    }

    @Test
    @Transactional
    public void getAllTransactionsBySenderTrIdIsNullOrNotNull() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where senderTrId is not null
        defaultTransactionShouldBeFound("senderTrId.specified=true");

        // Get all the transactionList where senderTrId is null
        defaultTransactionShouldNotBeFound("senderTrId.specified=false");
    }

    @Test
    @Transactional
    public void getAllTransactionsBySenderSuccessIsEqualToSomething() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where senderSuccess equals to DEFAULT_SENDER_SUCCESS
        defaultTransactionShouldBeFound("senderSuccess.equals=" + DEFAULT_SENDER_SUCCESS);

        // Get all the transactionList where senderSuccess equals to UPDATED_SENDER_SUCCESS
        defaultTransactionShouldNotBeFound("senderSuccess.equals=" + UPDATED_SENDER_SUCCESS);
    }

    @Test
    @Transactional
    public void getAllTransactionsBySenderSuccessIsInShouldWork() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where senderSuccess in DEFAULT_SENDER_SUCCESS or UPDATED_SENDER_SUCCESS
        defaultTransactionShouldBeFound("senderSuccess.in=" + DEFAULT_SENDER_SUCCESS + "," + UPDATED_SENDER_SUCCESS);

        // Get all the transactionList where senderSuccess equals to UPDATED_SENDER_SUCCESS
        defaultTransactionShouldNotBeFound("senderSuccess.in=" + UPDATED_SENDER_SUCCESS);
    }

    @Test
    @Transactional
    public void getAllTransactionsBySenderSuccessIsNullOrNotNull() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where senderSuccess is not null
        defaultTransactionShouldBeFound("senderSuccess.specified=true");

        // Get all the transactionList where senderSuccess is null
        defaultTransactionShouldNotBeFound("senderSuccess.specified=false");
    }

    @Test
    @Transactional
    public void getAllTransactionsBySenderDateIsEqualToSomething() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where senderDate equals to DEFAULT_SENDER_DATE
        defaultTransactionShouldBeFound("senderDate.equals=" + DEFAULT_SENDER_DATE);

        // Get all the transactionList where senderDate equals to UPDATED_SENDER_DATE
        defaultTransactionShouldNotBeFound("senderDate.equals=" + UPDATED_SENDER_DATE);
    }

    @Test
    @Transactional
    public void getAllTransactionsBySenderDateIsInShouldWork() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where senderDate in DEFAULT_SENDER_DATE or UPDATED_SENDER_DATE
        defaultTransactionShouldBeFound("senderDate.in=" + DEFAULT_SENDER_DATE + "," + UPDATED_SENDER_DATE);

        // Get all the transactionList where senderDate equals to UPDATED_SENDER_DATE
        defaultTransactionShouldNotBeFound("senderDate.in=" + UPDATED_SENDER_DATE);
    }

    @Test
    @Transactional
    public void getAllTransactionsBySenderDateIsNullOrNotNull() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where senderDate is not null
        defaultTransactionShouldBeFound("senderDate.specified=true");

        // Get all the transactionList where senderDate is null
        defaultTransactionShouldNotBeFound("senderDate.specified=false");
    }

    @Test
    @Transactional
    public void getAllTransactionsBySenderStatusIsEqualToSomething() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where senderStatus equals to DEFAULT_SENDER_STATUS
        defaultTransactionShouldBeFound("senderStatus.equals=" + DEFAULT_SENDER_STATUS);

        // Get all the transactionList where senderStatus equals to UPDATED_SENDER_STATUS
        defaultTransactionShouldNotBeFound("senderStatus.equals=" + UPDATED_SENDER_STATUS);
    }

    @Test
    @Transactional
    public void getAllTransactionsBySenderStatusIsInShouldWork() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where senderStatus in DEFAULT_SENDER_STATUS or UPDATED_SENDER_STATUS
        defaultTransactionShouldBeFound("senderStatus.in=" + DEFAULT_SENDER_STATUS + "," + UPDATED_SENDER_STATUS);

        // Get all the transactionList where senderStatus equals to UPDATED_SENDER_STATUS
        defaultTransactionShouldNotBeFound("senderStatus.in=" + UPDATED_SENDER_STATUS);
    }

    @Test
    @Transactional
    public void getAllTransactionsBySenderStatusIsNullOrNotNull() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where senderStatus is not null
        defaultTransactionShouldBeFound("senderStatus.specified=true");

        // Get all the transactionList where senderStatus is null
        defaultTransactionShouldNotBeFound("senderStatus.specified=false");
    }

    @Test
    @Transactional
    public void getAllTransactionsBySenderStatusTextIsEqualToSomething() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where senderStatusText equals to DEFAULT_SENDER_STATUS_TEXT
        defaultTransactionShouldBeFound("senderStatusText.equals=" + DEFAULT_SENDER_STATUS_TEXT);

        // Get all the transactionList where senderStatusText equals to UPDATED_SENDER_STATUS_TEXT
        defaultTransactionShouldNotBeFound("senderStatusText.equals=" + UPDATED_SENDER_STATUS_TEXT);
    }

    @Test
    @Transactional
    public void getAllTransactionsBySenderStatusTextIsInShouldWork() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where senderStatusText in DEFAULT_SENDER_STATUS_TEXT or UPDATED_SENDER_STATUS_TEXT
        defaultTransactionShouldBeFound("senderStatusText.in=" + DEFAULT_SENDER_STATUS_TEXT + "," + UPDATED_SENDER_STATUS_TEXT);

        // Get all the transactionList where senderStatusText equals to UPDATED_SENDER_STATUS_TEXT
        defaultTransactionShouldNotBeFound("senderStatusText.in=" + UPDATED_SENDER_STATUS_TEXT);
    }

    @Test
    @Transactional
    public void getAllTransactionsBySenderStatusTextIsNullOrNotNull() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where senderStatusText is not null
        defaultTransactionShouldBeFound("senderStatusText.specified=true");

        // Get all the transactionList where senderStatusText is null
        defaultTransactionShouldNotBeFound("senderStatusText.specified=false");
    }

    @Test
    @Transactional
    public void getAllTransactionsBySenderBillIdIsEqualToSomething() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where senderBillId equals to DEFAULT_SENDER_BILL_ID
        defaultTransactionShouldBeFound("senderBillId.equals=" + DEFAULT_SENDER_BILL_ID);

        // Get all the transactionList where senderBillId equals to UPDATED_SENDER_BILL_ID
        defaultTransactionShouldNotBeFound("senderBillId.equals=" + UPDATED_SENDER_BILL_ID);
    }

    @Test
    @Transactional
    public void getAllTransactionsBySenderBillIdIsInShouldWork() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where senderBillId in DEFAULT_SENDER_BILL_ID or UPDATED_SENDER_BILL_ID
        defaultTransactionShouldBeFound("senderBillId.in=" + DEFAULT_SENDER_BILL_ID + "," + UPDATED_SENDER_BILL_ID);

        // Get all the transactionList where senderBillId equals to UPDATED_SENDER_BILL_ID
        defaultTransactionShouldNotBeFound("senderBillId.in=" + UPDATED_SENDER_BILL_ID);
    }

    @Test
    @Transactional
    public void getAllTransactionsBySenderBillIdIsNullOrNotNull() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where senderBillId is not null
        defaultTransactionShouldBeFound("senderBillId.specified=true");

        // Get all the transactionList where senderBillId is null
        defaultTransactionShouldNotBeFound("senderBillId.specified=false");
    }

    @Test
    @Transactional
    public void getAllTransactionsBySenderBillIdIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where senderBillId greater than or equals to DEFAULT_SENDER_BILL_ID
        defaultTransactionShouldBeFound("senderBillId.greaterOrEqualThan=" + DEFAULT_SENDER_BILL_ID);

        // Get all the transactionList where senderBillId greater than or equals to UPDATED_SENDER_BILL_ID
        defaultTransactionShouldNotBeFound("senderBillId.greaterOrEqualThan=" + UPDATED_SENDER_BILL_ID);
    }

    @Test
    @Transactional
    public void getAllTransactionsBySenderBillIdIsLessThanSomething() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where senderBillId less than or equals to DEFAULT_SENDER_BILL_ID
        defaultTransactionShouldNotBeFound("senderBillId.lessThan=" + DEFAULT_SENDER_BILL_ID);

        // Get all the transactionList where senderBillId less than or equals to UPDATED_SENDER_BILL_ID
        defaultTransactionShouldBeFound("senderBillId.lessThan=" + UPDATED_SENDER_BILL_ID);
    }

    @Test
    @Transactional
    public void getAllTransactionsBySenderGatewayIsEqualToSomething() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where senderGateway equals to DEFAULT_SENDER_GATEWAY
        defaultTransactionShouldBeFound("senderGateway.equals=" + DEFAULT_SENDER_GATEWAY);

        // Get all the transactionList where senderGateway equals to UPDATED_SENDER_GATEWAY
        defaultTransactionShouldNotBeFound("senderGateway.equals=" + UPDATED_SENDER_GATEWAY);
    }

    @Test
    @Transactional
    public void getAllTransactionsBySenderGatewayIsInShouldWork() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where senderGateway in DEFAULT_SENDER_GATEWAY or UPDATED_SENDER_GATEWAY
        defaultTransactionShouldBeFound("senderGateway.in=" + DEFAULT_SENDER_GATEWAY + "," + UPDATED_SENDER_GATEWAY);

        // Get all the transactionList where senderGateway equals to UPDATED_SENDER_GATEWAY
        defaultTransactionShouldNotBeFound("senderGateway.in=" + UPDATED_SENDER_GATEWAY);
    }

    @Test
    @Transactional
    public void getAllTransactionsBySenderGatewayIsNullOrNotNull() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where senderGateway is not null
        defaultTransactionShouldBeFound("senderGateway.specified=true");

        // Get all the transactionList where senderGateway is null
        defaultTransactionShouldNotBeFound("senderGateway.specified=false");
    }

    @Test
    @Transactional
    public void getAllTransactionsByGatewayAmountIsEqualToSomething() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where gatewayAmount equals to DEFAULT_GATEWAY_AMOUNT
        defaultTransactionShouldBeFound("gatewayAmount.equals=" + DEFAULT_GATEWAY_AMOUNT);

        // Get all the transactionList where gatewayAmount equals to UPDATED_GATEWAY_AMOUNT
        defaultTransactionShouldNotBeFound("gatewayAmount.equals=" + UPDATED_GATEWAY_AMOUNT);
    }

    @Test
    @Transactional
    public void getAllTransactionsByGatewayAmountIsInShouldWork() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where gatewayAmount in DEFAULT_GATEWAY_AMOUNT or UPDATED_GATEWAY_AMOUNT
        defaultTransactionShouldBeFound("gatewayAmount.in=" + DEFAULT_GATEWAY_AMOUNT + "," + UPDATED_GATEWAY_AMOUNT);

        // Get all the transactionList where gatewayAmount equals to UPDATED_GATEWAY_AMOUNT
        defaultTransactionShouldNotBeFound("gatewayAmount.in=" + UPDATED_GATEWAY_AMOUNT);
    }

    @Test
    @Transactional
    public void getAllTransactionsByGatewayAmountIsNullOrNotNull() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where gatewayAmount is not null
        defaultTransactionShouldBeFound("gatewayAmount.specified=true");

        // Get all the transactionList where gatewayAmount is null
        defaultTransactionShouldNotBeFound("gatewayAmount.specified=false");
    }

    @Test
    @Transactional
    public void getAllTransactionsByRecipientNameIsEqualToSomething() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where recipientName equals to DEFAULT_RECIPIENT_NAME
        defaultTransactionShouldBeFound("recipientName.equals=" + DEFAULT_RECIPIENT_NAME);

        // Get all the transactionList where recipientName equals to UPDATED_RECIPIENT_NAME
        defaultTransactionShouldNotBeFound("recipientName.equals=" + UPDATED_RECIPIENT_NAME);
    }

    @Test
    @Transactional
    public void getAllTransactionsByRecipientNameIsInShouldWork() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where recipientName in DEFAULT_RECIPIENT_NAME or UPDATED_RECIPIENT_NAME
        defaultTransactionShouldBeFound("recipientName.in=" + DEFAULT_RECIPIENT_NAME + "," + UPDATED_RECIPIENT_NAME);

        // Get all the transactionList where recipientName equals to UPDATED_RECIPIENT_NAME
        defaultTransactionShouldNotBeFound("recipientName.in=" + UPDATED_RECIPIENT_NAME);
    }

    @Test
    @Transactional
    public void getAllTransactionsByRecipientNameIsNullOrNotNull() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where recipientName is not null
        defaultTransactionShouldBeFound("recipientName.specified=true");

        // Get all the transactionList where recipientName is null
        defaultTransactionShouldNotBeFound("recipientName.specified=false");
    }

    @Test
    @Transactional
    public void getAllTransactionsByRecipientAccountIsEqualToSomething() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where recipientAccount equals to DEFAULT_RECIPIENT_ACCOUNT
        defaultTransactionShouldBeFound("recipientAccount.equals=" + DEFAULT_RECIPIENT_ACCOUNT);

        // Get all the transactionList where recipientAccount equals to UPDATED_RECIPIENT_ACCOUNT
        defaultTransactionShouldNotBeFound("recipientAccount.equals=" + UPDATED_RECIPIENT_ACCOUNT);
    }

    @Test
    @Transactional
    public void getAllTransactionsByRecipientAccountIsInShouldWork() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where recipientAccount in DEFAULT_RECIPIENT_ACCOUNT or UPDATED_RECIPIENT_ACCOUNT
        defaultTransactionShouldBeFound("recipientAccount.in=" + DEFAULT_RECIPIENT_ACCOUNT + "," + UPDATED_RECIPIENT_ACCOUNT);

        // Get all the transactionList where recipientAccount equals to UPDATED_RECIPIENT_ACCOUNT
        defaultTransactionShouldNotBeFound("recipientAccount.in=" + UPDATED_RECIPIENT_ACCOUNT);
    }

    @Test
    @Transactional
    public void getAllTransactionsByRecipientAccountIsNullOrNotNull() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where recipientAccount is not null
        defaultTransactionShouldBeFound("recipientAccount.specified=true");

        // Get all the transactionList where recipientAccount is null
        defaultTransactionShouldNotBeFound("recipientAccount.specified=false");
    }

    @Test
    @Transactional
    public void getAllTransactionsByRecipientServiceIdIsEqualToSomething() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where recipientServiceId equals to DEFAULT_RECIPIENT_SERVICE_ID
        defaultTransactionShouldBeFound("recipientServiceId.equals=" + DEFAULT_RECIPIENT_SERVICE_ID);

        // Get all the transactionList where recipientServiceId equals to UPDATED_RECIPIENT_SERVICE_ID
        defaultTransactionShouldNotBeFound("recipientServiceId.equals=" + UPDATED_RECIPIENT_SERVICE_ID);
    }

    @Test
    @Transactional
    public void getAllTransactionsByRecipientServiceIdIsInShouldWork() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where recipientServiceId in DEFAULT_RECIPIENT_SERVICE_ID or UPDATED_RECIPIENT_SERVICE_ID
        defaultTransactionShouldBeFound("recipientServiceId.in=" + DEFAULT_RECIPIENT_SERVICE_ID + "," + UPDATED_RECIPIENT_SERVICE_ID);

        // Get all the transactionList where recipientServiceId equals to UPDATED_RECIPIENT_SERVICE_ID
        defaultTransactionShouldNotBeFound("recipientServiceId.in=" + UPDATED_RECIPIENT_SERVICE_ID);
    }

    @Test
    @Transactional
    public void getAllTransactionsByRecipientServiceIdIsNullOrNotNull() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where recipientServiceId is not null
        defaultTransactionShouldBeFound("recipientServiceId.specified=true");

        // Get all the transactionList where recipientServiceId is null
        defaultTransactionShouldNotBeFound("recipientServiceId.specified=false");
    }

    @Test
    @Transactional
    public void getAllTransactionsByRecipientServiceIdIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where recipientServiceId greater than or equals to DEFAULT_RECIPIENT_SERVICE_ID
        defaultTransactionShouldBeFound("recipientServiceId.greaterOrEqualThan=" + DEFAULT_RECIPIENT_SERVICE_ID);

        // Get all the transactionList where recipientServiceId greater than or equals to UPDATED_RECIPIENT_SERVICE_ID
        defaultTransactionShouldNotBeFound("recipientServiceId.greaterOrEqualThan=" + UPDATED_RECIPIENT_SERVICE_ID);
    }

    @Test
    @Transactional
    public void getAllTransactionsByRecipientServiceIdIsLessThanSomething() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where recipientServiceId less than or equals to DEFAULT_RECIPIENT_SERVICE_ID
        defaultTransactionShouldNotBeFound("recipientServiceId.lessThan=" + DEFAULT_RECIPIENT_SERVICE_ID);

        // Get all the transactionList where recipientServiceId less than or equals to UPDATED_RECIPIENT_SERVICE_ID
        defaultTransactionShouldBeFound("recipientServiceId.lessThan=" + UPDATED_RECIPIENT_SERVICE_ID);
    }

    @Test
    @Transactional
    public void getAllTransactionsByRecipientAmountIsEqualToSomething() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where recipientAmount equals to DEFAULT_RECIPIENT_AMOUNT
        defaultTransactionShouldBeFound("recipientAmount.equals=" + DEFAULT_RECIPIENT_AMOUNT);

        // Get all the transactionList where recipientAmount equals to UPDATED_RECIPIENT_AMOUNT
        defaultTransactionShouldNotBeFound("recipientAmount.equals=" + UPDATED_RECIPIENT_AMOUNT);
    }

    @Test
    @Transactional
    public void getAllTransactionsByRecipientAmountIsInShouldWork() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where recipientAmount in DEFAULT_RECIPIENT_AMOUNT or UPDATED_RECIPIENT_AMOUNT
        defaultTransactionShouldBeFound("recipientAmount.in=" + DEFAULT_RECIPIENT_AMOUNT + "," + UPDATED_RECIPIENT_AMOUNT);

        // Get all the transactionList where recipientAmount equals to UPDATED_RECIPIENT_AMOUNT
        defaultTransactionShouldNotBeFound("recipientAmount.in=" + UPDATED_RECIPIENT_AMOUNT);
    }

    @Test
    @Transactional
    public void getAllTransactionsByRecipientAmountIsNullOrNotNull() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where recipientAmount is not null
        defaultTransactionShouldBeFound("recipientAmount.specified=true");

        // Get all the transactionList where recipientAmount is null
        defaultTransactionShouldNotBeFound("recipientAmount.specified=false");
    }

    @Test
    @Transactional
    public void getAllTransactionsByRecipientAmountIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where recipientAmount greater than or equals to DEFAULT_RECIPIENT_AMOUNT
        defaultTransactionShouldBeFound("recipientAmount.greaterOrEqualThan=" + DEFAULT_RECIPIENT_AMOUNT);

        // Get all the transactionList where recipientAmount greater than or equals to UPDATED_RECIPIENT_AMOUNT
        defaultTransactionShouldNotBeFound("recipientAmount.greaterOrEqualThan=" + UPDATED_RECIPIENT_AMOUNT);
    }

    @Test
    @Transactional
    public void getAllTransactionsByRecipientAmountIsLessThanSomething() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where recipientAmount less than or equals to DEFAULT_RECIPIENT_AMOUNT
        defaultTransactionShouldNotBeFound("recipientAmount.lessThan=" + DEFAULT_RECIPIENT_AMOUNT);

        // Get all the transactionList where recipientAmount less than or equals to UPDATED_RECIPIENT_AMOUNT
        defaultTransactionShouldBeFound("recipientAmount.lessThan=" + UPDATED_RECIPIENT_AMOUNT);
    }

    @Test
    @Transactional
    public void getAllTransactionsByRecipientCcyIsEqualToSomething() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where recipientCcy equals to DEFAULT_RECIPIENT_CCY
        defaultTransactionShouldBeFound("recipientCcy.equals=" + DEFAULT_RECIPIENT_CCY);

        // Get all the transactionList where recipientCcy equals to UPDATED_RECIPIENT_CCY
        defaultTransactionShouldNotBeFound("recipientCcy.equals=" + UPDATED_RECIPIENT_CCY);
    }

    @Test
    @Transactional
    public void getAllTransactionsByRecipientCcyIsInShouldWork() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where recipientCcy in DEFAULT_RECIPIENT_CCY or UPDATED_RECIPIENT_CCY
        defaultTransactionShouldBeFound("recipientCcy.in=" + DEFAULT_RECIPIENT_CCY + "," + UPDATED_RECIPIENT_CCY);

        // Get all the transactionList where recipientCcy equals to UPDATED_RECIPIENT_CCY
        defaultTransactionShouldNotBeFound("recipientCcy.in=" + UPDATED_RECIPIENT_CCY);
    }

    @Test
    @Transactional
    public void getAllTransactionsByRecipientCcyIsNullOrNotNull() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where recipientCcy is not null
        defaultTransactionShouldBeFound("recipientCcy.specified=true");

        // Get all the transactionList where recipientCcy is null
        defaultTransactionShouldNotBeFound("recipientCcy.specified=false");
    }

    @Test
    @Transactional
    public void getAllTransactionsByRecipientCcyIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where recipientCcy greater than or equals to DEFAULT_RECIPIENT_CCY
        defaultTransactionShouldBeFound("recipientCcy.greaterOrEqualThan=" + DEFAULT_RECIPIENT_CCY);

        // Get all the transactionList where recipientCcy greater than or equals to UPDATED_RECIPIENT_CCY
        defaultTransactionShouldNotBeFound("recipientCcy.greaterOrEqualThan=" + UPDATED_RECIPIENT_CCY);
    }

    @Test
    @Transactional
    public void getAllTransactionsByRecipientCcyIsLessThanSomething() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where recipientCcy less than or equals to DEFAULT_RECIPIENT_CCY
        defaultTransactionShouldNotBeFound("recipientCcy.lessThan=" + DEFAULT_RECIPIENT_CCY);

        // Get all the transactionList where recipientCcy less than or equals to UPDATED_RECIPIENT_CCY
        defaultTransactionShouldBeFound("recipientCcy.lessThan=" + UPDATED_RECIPIENT_CCY);
    }

    @Test
    @Transactional
    public void getAllTransactionsByRecipientTrIdIsEqualToSomething() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where recipientTrId equals to DEFAULT_RECIPIENT_TR_ID
        defaultTransactionShouldBeFound("recipientTrId.equals=" + DEFAULT_RECIPIENT_TR_ID);

        // Get all the transactionList where recipientTrId equals to UPDATED_RECIPIENT_TR_ID
        defaultTransactionShouldNotBeFound("recipientTrId.equals=" + UPDATED_RECIPIENT_TR_ID);
    }

    @Test
    @Transactional
    public void getAllTransactionsByRecipientTrIdIsInShouldWork() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where recipientTrId in DEFAULT_RECIPIENT_TR_ID or UPDATED_RECIPIENT_TR_ID
        defaultTransactionShouldBeFound("recipientTrId.in=" + DEFAULT_RECIPIENT_TR_ID + "," + UPDATED_RECIPIENT_TR_ID);

        // Get all the transactionList where recipientTrId equals to UPDATED_RECIPIENT_TR_ID
        defaultTransactionShouldNotBeFound("recipientTrId.in=" + UPDATED_RECIPIENT_TR_ID);
    }

    @Test
    @Transactional
    public void getAllTransactionsByRecipientTrIdIsNullOrNotNull() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where recipientTrId is not null
        defaultTransactionShouldBeFound("recipientTrId.specified=true");

        // Get all the transactionList where recipientTrId is null
        defaultTransactionShouldNotBeFound("recipientTrId.specified=false");
    }

    @Test
    @Transactional
    public void getAllTransactionsByRecipientSuccessIsEqualToSomething() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where recipientSuccess equals to DEFAULT_RECIPIENT_SUCCESS
        defaultTransactionShouldBeFound("recipientSuccess.equals=" + DEFAULT_RECIPIENT_SUCCESS);

        // Get all the transactionList where recipientSuccess equals to UPDATED_RECIPIENT_SUCCESS
        defaultTransactionShouldNotBeFound("recipientSuccess.equals=" + UPDATED_RECIPIENT_SUCCESS);
    }

    @Test
    @Transactional
    public void getAllTransactionsByRecipientSuccessIsInShouldWork() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where recipientSuccess in DEFAULT_RECIPIENT_SUCCESS or UPDATED_RECIPIENT_SUCCESS
        defaultTransactionShouldBeFound("recipientSuccess.in=" + DEFAULT_RECIPIENT_SUCCESS + "," + UPDATED_RECIPIENT_SUCCESS);

        // Get all the transactionList where recipientSuccess equals to UPDATED_RECIPIENT_SUCCESS
        defaultTransactionShouldNotBeFound("recipientSuccess.in=" + UPDATED_RECIPIENT_SUCCESS);
    }

    @Test
    @Transactional
    public void getAllTransactionsByRecipientSuccessIsNullOrNotNull() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where recipientSuccess is not null
        defaultTransactionShouldBeFound("recipientSuccess.specified=true");

        // Get all the transactionList where recipientSuccess is null
        defaultTransactionShouldNotBeFound("recipientSuccess.specified=false");
    }

    @Test
    @Transactional
    public void getAllTransactionsByRecipientDateIsEqualToSomething() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where recipientDate equals to DEFAULT_RECIPIENT_DATE
        defaultTransactionShouldBeFound("recipientDate.equals=" + DEFAULT_RECIPIENT_DATE);

        // Get all the transactionList where recipientDate equals to UPDATED_RECIPIENT_DATE
        defaultTransactionShouldNotBeFound("recipientDate.equals=" + UPDATED_RECIPIENT_DATE);
    }

    @Test
    @Transactional
    public void getAllTransactionsByRecipientDateIsInShouldWork() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where recipientDate in DEFAULT_RECIPIENT_DATE or UPDATED_RECIPIENT_DATE
        defaultTransactionShouldBeFound("recipientDate.in=" + DEFAULT_RECIPIENT_DATE + "," + UPDATED_RECIPIENT_DATE);

        // Get all the transactionList where recipientDate equals to UPDATED_RECIPIENT_DATE
        defaultTransactionShouldNotBeFound("recipientDate.in=" + UPDATED_RECIPIENT_DATE);
    }

    @Test
    @Transactional
    public void getAllTransactionsByRecipientDateIsNullOrNotNull() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where recipientDate is not null
        defaultTransactionShouldBeFound("recipientDate.specified=true");

        // Get all the transactionList where recipientDate is null
        defaultTransactionShouldNotBeFound("recipientDate.specified=false");
    }

    @Test
    @Transactional
    public void getAllTransactionsByRecipientStatusIsEqualToSomething() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where recipientStatus equals to DEFAULT_RECIPIENT_STATUS
        defaultTransactionShouldBeFound("recipientStatus.equals=" + DEFAULT_RECIPIENT_STATUS);

        // Get all the transactionList where recipientStatus equals to UPDATED_RECIPIENT_STATUS
        defaultTransactionShouldNotBeFound("recipientStatus.equals=" + UPDATED_RECIPIENT_STATUS);
    }

    @Test
    @Transactional
    public void getAllTransactionsByRecipientStatusIsInShouldWork() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where recipientStatus in DEFAULT_RECIPIENT_STATUS or UPDATED_RECIPIENT_STATUS
        defaultTransactionShouldBeFound("recipientStatus.in=" + DEFAULT_RECIPIENT_STATUS + "," + UPDATED_RECIPIENT_STATUS);

        // Get all the transactionList where recipientStatus equals to UPDATED_RECIPIENT_STATUS
        defaultTransactionShouldNotBeFound("recipientStatus.in=" + UPDATED_RECIPIENT_STATUS);
    }

    @Test
    @Transactional
    public void getAllTransactionsByRecipientStatusIsNullOrNotNull() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where recipientStatus is not null
        defaultTransactionShouldBeFound("recipientStatus.specified=true");

        // Get all the transactionList where recipientStatus is null
        defaultTransactionShouldNotBeFound("recipientStatus.specified=false");
    }

    @Test
    @Transactional
    public void getAllTransactionsByRecipientStatusTextIsEqualToSomething() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where recipientStatusText equals to DEFAULT_RECIPIENT_STATUS_TEXT
        defaultTransactionShouldBeFound("recipientStatusText.equals=" + DEFAULT_RECIPIENT_STATUS_TEXT);

        // Get all the transactionList where recipientStatusText equals to UPDATED_RECIPIENT_STATUS_TEXT
        defaultTransactionShouldNotBeFound("recipientStatusText.equals=" + UPDATED_RECIPIENT_STATUS_TEXT);
    }

    @Test
    @Transactional
    public void getAllTransactionsByRecipientStatusTextIsInShouldWork() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where recipientStatusText in DEFAULT_RECIPIENT_STATUS_TEXT or UPDATED_RECIPIENT_STATUS_TEXT
        defaultTransactionShouldBeFound("recipientStatusText.in=" + DEFAULT_RECIPIENT_STATUS_TEXT + "," + UPDATED_RECIPIENT_STATUS_TEXT);

        // Get all the transactionList where recipientStatusText equals to UPDATED_RECIPIENT_STATUS_TEXT
        defaultTransactionShouldNotBeFound("recipientStatusText.in=" + UPDATED_RECIPIENT_STATUS_TEXT);
    }

    @Test
    @Transactional
    public void getAllTransactionsByRecipientStatusTextIsNullOrNotNull() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where recipientStatusText is not null
        defaultTransactionShouldBeFound("recipientStatusText.specified=true");

        // Get all the transactionList where recipientStatusText is null
        defaultTransactionShouldNotBeFound("recipientStatusText.specified=false");
    }

    @Test
    @Transactional
    public void getAllTransactionsByRecipientBillIdIsEqualToSomething() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where recipientBillId equals to DEFAULT_RECIPIENT_BILL_ID
        defaultTransactionShouldBeFound("recipientBillId.equals=" + DEFAULT_RECIPIENT_BILL_ID);

        // Get all the transactionList where recipientBillId equals to UPDATED_RECIPIENT_BILL_ID
        defaultTransactionShouldNotBeFound("recipientBillId.equals=" + UPDATED_RECIPIENT_BILL_ID);
    }

    @Test
    @Transactional
    public void getAllTransactionsByRecipientBillIdIsInShouldWork() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where recipientBillId in DEFAULT_RECIPIENT_BILL_ID or UPDATED_RECIPIENT_BILL_ID
        defaultTransactionShouldBeFound("recipientBillId.in=" + DEFAULT_RECIPIENT_BILL_ID + "," + UPDATED_RECIPIENT_BILL_ID);

        // Get all the transactionList where recipientBillId equals to UPDATED_RECIPIENT_BILL_ID
        defaultTransactionShouldNotBeFound("recipientBillId.in=" + UPDATED_RECIPIENT_BILL_ID);
    }

    @Test
    @Transactional
    public void getAllTransactionsByRecipientBillIdIsNullOrNotNull() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where recipientBillId is not null
        defaultTransactionShouldBeFound("recipientBillId.specified=true");

        // Get all the transactionList where recipientBillId is null
        defaultTransactionShouldNotBeFound("recipientBillId.specified=false");
    }

    @Test
    @Transactional
    public void getAllTransactionsByRecipientBillIdIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where recipientBillId greater than or equals to DEFAULT_RECIPIENT_BILL_ID
        defaultTransactionShouldBeFound("recipientBillId.greaterOrEqualThan=" + DEFAULT_RECIPIENT_BILL_ID);

        // Get all the transactionList where recipientBillId greater than or equals to UPDATED_RECIPIENT_BILL_ID
        defaultTransactionShouldNotBeFound("recipientBillId.greaterOrEqualThan=" + UPDATED_RECIPIENT_BILL_ID);
    }

    @Test
    @Transactional
    public void getAllTransactionsByRecipientBillIdIsLessThanSomething() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where recipientBillId less than or equals to DEFAULT_RECIPIENT_BILL_ID
        defaultTransactionShouldNotBeFound("recipientBillId.lessThan=" + DEFAULT_RECIPIENT_BILL_ID);

        // Get all the transactionList where recipientBillId less than or equals to UPDATED_RECIPIENT_BILL_ID
        defaultTransactionShouldBeFound("recipientBillId.lessThan=" + UPDATED_RECIPIENT_BILL_ID);
    }

    @Test
    @Transactional
    public void getAllTransactionsByRecipientGatewayIsEqualToSomething() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where recipientGateway equals to DEFAULT_RECIPIENT_GATEWAY
        defaultTransactionShouldBeFound("recipientGateway.equals=" + DEFAULT_RECIPIENT_GATEWAY);

        // Get all the transactionList where recipientGateway equals to UPDATED_RECIPIENT_GATEWAY
        defaultTransactionShouldNotBeFound("recipientGateway.equals=" + UPDATED_RECIPIENT_GATEWAY);
    }

    @Test
    @Transactional
    public void getAllTransactionsByRecipientGatewayIsInShouldWork() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where recipientGateway in DEFAULT_RECIPIENT_GATEWAY or UPDATED_RECIPIENT_GATEWAY
        defaultTransactionShouldBeFound("recipientGateway.in=" + DEFAULT_RECIPIENT_GATEWAY + "," + UPDATED_RECIPIENT_GATEWAY);

        // Get all the transactionList where recipientGateway equals to UPDATED_RECIPIENT_GATEWAY
        defaultTransactionShouldNotBeFound("recipientGateway.in=" + UPDATED_RECIPIENT_GATEWAY);
    }

    @Test
    @Transactional
    public void getAllTransactionsByRecipientGatewayIsNullOrNotNull() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where recipientGateway is not null
        defaultTransactionShouldBeFound("recipientGateway.specified=true");

        // Get all the transactionList where recipientGateway is null
        defaultTransactionShouldNotBeFound("recipientGateway.specified=false");
    }

    @Test
    @Transactional
    public void getAllTransactionsBySystemCommissionUpAmountIsEqualToSomething() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where systemCommissionUpAmount equals to DEFAULT_SYSTEM_COMMISSION_UP_AMOUNT
        defaultTransactionShouldBeFound("systemCommissionUpAmount.equals=" + DEFAULT_SYSTEM_COMMISSION_UP_AMOUNT);

        // Get all the transactionList where systemCommissionUpAmount equals to UPDATED_SYSTEM_COMMISSION_UP_AMOUNT
        defaultTransactionShouldNotBeFound("systemCommissionUpAmount.equals=" + UPDATED_SYSTEM_COMMISSION_UP_AMOUNT);
    }

    @Test
    @Transactional
    public void getAllTransactionsBySystemCommissionUpAmountIsInShouldWork() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where systemCommissionUpAmount in DEFAULT_SYSTEM_COMMISSION_UP_AMOUNT or UPDATED_SYSTEM_COMMISSION_UP_AMOUNT
        defaultTransactionShouldBeFound(
            "systemCommissionUpAmount.in=" + DEFAULT_SYSTEM_COMMISSION_UP_AMOUNT + "," + UPDATED_SYSTEM_COMMISSION_UP_AMOUNT
        );

        // Get all the transactionList where systemCommissionUpAmount equals to UPDATED_SYSTEM_COMMISSION_UP_AMOUNT
        defaultTransactionShouldNotBeFound("systemCommissionUpAmount.in=" + UPDATED_SYSTEM_COMMISSION_UP_AMOUNT);
    }

    @Test
    @Transactional
    public void getAllTransactionsBySystemCommissionUpAmountIsNullOrNotNull() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where systemCommissionUpAmount is not null
        defaultTransactionShouldBeFound("systemCommissionUpAmount.specified=true");

        // Get all the transactionList where systemCommissionUpAmount is null
        defaultTransactionShouldNotBeFound("systemCommissionUpAmount.specified=false");
    }

    @Test
    @Transactional
    public void getAllTransactionsBySystemCommissionUpAmountIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where systemCommissionUpAmount greater than or equals to DEFAULT_SYSTEM_COMMISSION_UP_AMOUNT
        defaultTransactionShouldBeFound("systemCommissionUpAmount.greaterOrEqualThan=" + DEFAULT_SYSTEM_COMMISSION_UP_AMOUNT);

        // Get all the transactionList where systemCommissionUpAmount greater than or equals to UPDATED_SYSTEM_COMMISSION_UP_AMOUNT
        defaultTransactionShouldNotBeFound("systemCommissionUpAmount.greaterOrEqualThan=" + UPDATED_SYSTEM_COMMISSION_UP_AMOUNT);
    }

    @Test
    @Transactional
    public void getAllTransactionsBySystemCommissionUpAmountIsLessThanSomething() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where systemCommissionUpAmount less than or equals to DEFAULT_SYSTEM_COMMISSION_UP_AMOUNT
        defaultTransactionShouldNotBeFound("systemCommissionUpAmount.lessThan=" + DEFAULT_SYSTEM_COMMISSION_UP_AMOUNT);

        // Get all the transactionList where systemCommissionUpAmount less than or equals to UPDATED_SYSTEM_COMMISSION_UP_AMOUNT
        defaultTransactionShouldBeFound("systemCommissionUpAmount.lessThan=" + UPDATED_SYSTEM_COMMISSION_UP_AMOUNT);
    }

    @Test
    @Transactional
    public void getAllTransactionsBySystemCommissionDownAmountIsEqualToSomething() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where systemCommissionDownAmount equals to DEFAULT_SYSTEM_COMMISSION_DOWN_AMOUNT
        defaultTransactionShouldBeFound("systemCommissionDownAmount.equals=" + DEFAULT_SYSTEM_COMMISSION_DOWN_AMOUNT);

        // Get all the transactionList where systemCommissionDownAmount equals to UPDATED_SYSTEM_COMMISSION_DOWN_AMOUNT
        defaultTransactionShouldNotBeFound("systemCommissionDownAmount.equals=" + UPDATED_SYSTEM_COMMISSION_DOWN_AMOUNT);
    }

    @Test
    @Transactional
    public void getAllTransactionsBySystemCommissionDownAmountIsInShouldWork() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where systemCommissionDownAmount in DEFAULT_SYSTEM_COMMISSION_DOWN_AMOUNT or UPDATED_SYSTEM_COMMISSION_DOWN_AMOUNT
        defaultTransactionShouldBeFound(
            "systemCommissionDownAmount.in=" + DEFAULT_SYSTEM_COMMISSION_DOWN_AMOUNT + "," + UPDATED_SYSTEM_COMMISSION_DOWN_AMOUNT
        );

        // Get all the transactionList where systemCommissionDownAmount equals to UPDATED_SYSTEM_COMMISSION_DOWN_AMOUNT
        defaultTransactionShouldNotBeFound("systemCommissionDownAmount.in=" + UPDATED_SYSTEM_COMMISSION_DOWN_AMOUNT);
    }

    @Test
    @Transactional
    public void getAllTransactionsBySystemCommissionDownAmountIsNullOrNotNull() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where systemCommissionDownAmount is not null
        defaultTransactionShouldBeFound("systemCommissionDownAmount.specified=true");

        // Get all the transactionList where systemCommissionDownAmount is null
        defaultTransactionShouldNotBeFound("systemCommissionDownAmount.specified=false");
    }

    @Test
    @Transactional
    public void getAllTransactionsBySystemCommissionDownAmountIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where systemCommissionDownAmount greater than or equals to DEFAULT_SYSTEM_COMMISSION_DOWN_AMOUNT
        defaultTransactionShouldBeFound("systemCommissionDownAmount.greaterOrEqualThan=" + DEFAULT_SYSTEM_COMMISSION_DOWN_AMOUNT);

        // Get all the transactionList where systemCommissionDownAmount greater than or equals to UPDATED_SYSTEM_COMMISSION_DOWN_AMOUNT
        defaultTransactionShouldNotBeFound("systemCommissionDownAmount.greaterOrEqualThan=" + UPDATED_SYSTEM_COMMISSION_DOWN_AMOUNT);
    }

    @Test
    @Transactional
    public void getAllTransactionsBySystemCommissionDownAmountIsLessThanSomething() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where systemCommissionDownAmount less than or equals to DEFAULT_SYSTEM_COMMISSION_DOWN_AMOUNT
        defaultTransactionShouldNotBeFound("systemCommissionDownAmount.lessThan=" + DEFAULT_SYSTEM_COMMISSION_DOWN_AMOUNT);

        // Get all the transactionList where systemCommissionDownAmount less than or equals to UPDATED_SYSTEM_COMMISSION_DOWN_AMOUNT
        defaultTransactionShouldBeFound("systemCommissionDownAmount.lessThan=" + UPDATED_SYSTEM_COMMISSION_DOWN_AMOUNT);
    }

    @Test
    @Transactional
    public void getAllTransactionsBySystemFeeAmountIsEqualToSomething() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where systemFeeAmount equals to DEFAULT_SYSTEM_FEE_AMOUNT
        defaultTransactionShouldBeFound("systemFeeAmount.equals=" + DEFAULT_SYSTEM_FEE_AMOUNT);

        // Get all the transactionList where systemFeeAmount equals to UPDATED_SYSTEM_FEE_AMOUNT
        defaultTransactionShouldNotBeFound("systemFeeAmount.equals=" + UPDATED_SYSTEM_FEE_AMOUNT);
    }

    @Test
    @Transactional
    public void getAllTransactionsBySystemFeeAmountIsInShouldWork() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where systemFeeAmount in DEFAULT_SYSTEM_FEE_AMOUNT or UPDATED_SYSTEM_FEE_AMOUNT
        defaultTransactionShouldBeFound("systemFeeAmount.in=" + DEFAULT_SYSTEM_FEE_AMOUNT + "," + UPDATED_SYSTEM_FEE_AMOUNT);

        // Get all the transactionList where systemFeeAmount equals to UPDATED_SYSTEM_FEE_AMOUNT
        defaultTransactionShouldNotBeFound("systemFeeAmount.in=" + UPDATED_SYSTEM_FEE_AMOUNT);
    }

    @Test
    @Transactional
    public void getAllTransactionsBySystemFeeAmountIsNullOrNotNull() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where systemFeeAmount is not null
        defaultTransactionShouldBeFound("systemFeeAmount.specified=true");

        // Get all the transactionList where systemFeeAmount is null
        defaultTransactionShouldNotBeFound("systemFeeAmount.specified=false");
    }

    @Test
    @Transactional
    public void getAllTransactionsBySystemFeeAmountIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where systemFeeAmount greater than or equals to DEFAULT_SYSTEM_FEE_AMOUNT
        defaultTransactionShouldBeFound("systemFeeAmount.greaterOrEqualThan=" + DEFAULT_SYSTEM_FEE_AMOUNT);

        // Get all the transactionList where systemFeeAmount greater than or equals to UPDATED_SYSTEM_FEE_AMOUNT
        defaultTransactionShouldNotBeFound("systemFeeAmount.greaterOrEqualThan=" + UPDATED_SYSTEM_FEE_AMOUNT);
    }

    @Test
    @Transactional
    public void getAllTransactionsBySystemFeeAmountIsLessThanSomething() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where systemFeeAmount less than or equals to DEFAULT_SYSTEM_FEE_AMOUNT
        defaultTransactionShouldNotBeFound("systemFeeAmount.lessThan=" + DEFAULT_SYSTEM_FEE_AMOUNT);

        // Get all the transactionList where systemFeeAmount less than or equals to UPDATED_SYSTEM_FEE_AMOUNT
        defaultTransactionShouldBeFound("systemFeeAmount.lessThan=" + UPDATED_SYSTEM_FEE_AMOUNT);
    }

    @Test
    @Transactional
    public void getAllTransactionsByAgentFeeAmountIsEqualToSomething() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where agentFeeAmount equals to DEFAULT_AGENT_FEE_AMOUNT
        defaultTransactionShouldBeFound("agentFeeAmount.equals=" + DEFAULT_AGENT_FEE_AMOUNT);

        // Get all the transactionList where agentFeeAmount equals to UPDATED_AGENT_FEE_AMOUNT
        defaultTransactionShouldNotBeFound("agentFeeAmount.equals=" + UPDATED_AGENT_FEE_AMOUNT);
    }

    @Test
    @Transactional
    public void getAllTransactionsByAgentFeeAmountIsInShouldWork() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where agentFeeAmount in DEFAULT_AGENT_FEE_AMOUNT or UPDATED_AGENT_FEE_AMOUNT
        defaultTransactionShouldBeFound("agentFeeAmount.in=" + DEFAULT_AGENT_FEE_AMOUNT + "," + UPDATED_AGENT_FEE_AMOUNT);

        // Get all the transactionList where agentFeeAmount equals to UPDATED_AGENT_FEE_AMOUNT
        defaultTransactionShouldNotBeFound("agentFeeAmount.in=" + UPDATED_AGENT_FEE_AMOUNT);
    }

    @Test
    @Transactional
    public void getAllTransactionsByAgentFeeAmountIsNullOrNotNull() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where agentFeeAmount is not null
        defaultTransactionShouldBeFound("agentFeeAmount.specified=true");

        // Get all the transactionList where agentFeeAmount is null
        defaultTransactionShouldNotBeFound("agentFeeAmount.specified=false");
    }

    @Test
    @Transactional
    public void getAllTransactionsByAgentFeeAmountIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where agentFeeAmount greater than or equals to DEFAULT_AGENT_FEE_AMOUNT
        defaultTransactionShouldBeFound("agentFeeAmount.greaterOrEqualThan=" + DEFAULT_AGENT_FEE_AMOUNT);

        // Get all the transactionList where agentFeeAmount greater than or equals to UPDATED_AGENT_FEE_AMOUNT
        defaultTransactionShouldNotBeFound("agentFeeAmount.greaterOrEqualThan=" + UPDATED_AGENT_FEE_AMOUNT);
    }

    @Test
    @Transactional
    public void getAllTransactionsByAgentFeeAmountIsLessThanSomething() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where agentFeeAmount less than or equals to DEFAULT_AGENT_FEE_AMOUNT
        defaultTransactionShouldNotBeFound("agentFeeAmount.lessThan=" + DEFAULT_AGENT_FEE_AMOUNT);

        // Get all the transactionList where agentFeeAmount less than or equals to UPDATED_AGENT_FEE_AMOUNT
        defaultTransactionShouldBeFound("agentFeeAmount.lessThan=" + UPDATED_AGENT_FEE_AMOUNT);
    }

    @Test
    @Transactional
    public void getAllTransactionsByRoundAmountIsEqualToSomething() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where roundAmount equals to DEFAULT_ROUND_AMOUNT
        defaultTransactionShouldBeFound("roundAmount.equals=" + DEFAULT_ROUND_AMOUNT);

        // Get all the transactionList where roundAmount equals to UPDATED_ROUND_AMOUNT
        defaultTransactionShouldNotBeFound("roundAmount.equals=" + UPDATED_ROUND_AMOUNT);
    }

    @Test
    @Transactional
    public void getAllTransactionsByRoundAmountIsInShouldWork() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where roundAmount in DEFAULT_ROUND_AMOUNT or UPDATED_ROUND_AMOUNT
        defaultTransactionShouldBeFound("roundAmount.in=" + DEFAULT_ROUND_AMOUNT + "," + UPDATED_ROUND_AMOUNT);

        // Get all the transactionList where roundAmount equals to UPDATED_ROUND_AMOUNT
        defaultTransactionShouldNotBeFound("roundAmount.in=" + UPDATED_ROUND_AMOUNT);
    }

    @Test
    @Transactional
    public void getAllTransactionsByRoundAmountIsNullOrNotNull() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where roundAmount is not null
        defaultTransactionShouldBeFound("roundAmount.specified=true");

        // Get all the transactionList where roundAmount is null
        defaultTransactionShouldNotBeFound("roundAmount.specified=false");
    }

    @Test
    @Transactional
    public void getAllTransactionsBySystemFeeIsEqualToSomething() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where systemFee equals to DEFAULT_SYSTEM_FEE
        defaultTransactionShouldBeFound("systemFee.equals=" + DEFAULT_SYSTEM_FEE);

        // Get all the transactionList where systemFee equals to UPDATED_SYSTEM_FEE
        defaultTransactionShouldNotBeFound("systemFee.equals=" + UPDATED_SYSTEM_FEE);
    }

    @Test
    @Transactional
    public void getAllTransactionsBySystemFeeIsInShouldWork() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where systemFee in DEFAULT_SYSTEM_FEE or UPDATED_SYSTEM_FEE
        defaultTransactionShouldBeFound("systemFee.in=" + DEFAULT_SYSTEM_FEE + "," + UPDATED_SYSTEM_FEE);

        // Get all the transactionList where systemFee equals to UPDATED_SYSTEM_FEE
        defaultTransactionShouldNotBeFound("systemFee.in=" + UPDATED_SYSTEM_FEE);
    }

    @Test
    @Transactional
    public void getAllTransactionsBySystemFeeIsNullOrNotNull() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where systemFee is not null
        defaultTransactionShouldBeFound("systemFee.specified=true");

        // Get all the transactionList where systemFee is null
        defaultTransactionShouldNotBeFound("systemFee.specified=false");
    }

    @Test
    @Transactional
    public void getAllTransactionsBySystemCurrencyIsEqualToSomething() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where systemCurrency equals to DEFAULT_SYSTEM_CURRENCY
        defaultTransactionShouldBeFound("systemCurrency.equals=" + DEFAULT_SYSTEM_CURRENCY);

        // Get all the transactionList where systemCurrency equals to UPDATED_SYSTEM_CURRENCY
        defaultTransactionShouldNotBeFound("systemCurrency.equals=" + UPDATED_SYSTEM_CURRENCY);
    }

    @Test
    @Transactional
    public void getAllTransactionsBySystemCurrencyIsInShouldWork() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where systemCurrency in DEFAULT_SYSTEM_CURRENCY or UPDATED_SYSTEM_CURRENCY
        defaultTransactionShouldBeFound("systemCurrency.in=" + DEFAULT_SYSTEM_CURRENCY + "," + UPDATED_SYSTEM_CURRENCY);

        // Get all the transactionList where systemCurrency equals to UPDATED_SYSTEM_CURRENCY
        defaultTransactionShouldNotBeFound("systemCurrency.in=" + UPDATED_SYSTEM_CURRENCY);
    }

    @Test
    @Transactional
    public void getAllTransactionsBySystemCurrencyIsNullOrNotNull() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where systemCurrency is not null
        defaultTransactionShouldBeFound("systemCurrency.specified=true");

        // Get all the transactionList where systemCurrency is null
        defaultTransactionShouldNotBeFound("systemCurrency.specified=false");
    }

    @Test
    @Transactional
    public void getAllTransactionsBySystemCurrencyIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where systemCurrency greater than or equals to DEFAULT_SYSTEM_CURRENCY
        defaultTransactionShouldBeFound("systemCurrency.greaterOrEqualThan=" + DEFAULT_SYSTEM_CURRENCY);

        // Get all the transactionList where systemCurrency greater than or equals to UPDATED_SYSTEM_CURRENCY
        defaultTransactionShouldNotBeFound("systemCurrency.greaterOrEqualThan=" + UPDATED_SYSTEM_CURRENCY);
    }

    @Test
    @Transactional
    public void getAllTransactionsBySystemCurrencyIsLessThanSomething() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where systemCurrency less than or equals to DEFAULT_SYSTEM_CURRENCY
        defaultTransactionShouldNotBeFound("systemCurrency.lessThan=" + DEFAULT_SYSTEM_CURRENCY);

        // Get all the transactionList where systemCurrency less than or equals to UPDATED_SYSTEM_CURRENCY
        defaultTransactionShouldBeFound("systemCurrency.lessThan=" + UPDATED_SYSTEM_CURRENCY);
    }

    @Test
    @Transactional
    public void getAllTransactionsByCcyRateIsEqualToSomething() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where ccyRate equals to DEFAULT_CCY_RATE
        defaultTransactionShouldBeFound("ccyRate.equals=" + DEFAULT_CCY_RATE);

        // Get all the transactionList where ccyRate equals to UPDATED_CCY_RATE
        defaultTransactionShouldNotBeFound("ccyRate.equals=" + UPDATED_CCY_RATE);
    }

    @Test
    @Transactional
    public void getAllTransactionsByCcyRateIsInShouldWork() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where ccyRate in DEFAULT_CCY_RATE or UPDATED_CCY_RATE
        defaultTransactionShouldBeFound("ccyRate.in=" + DEFAULT_CCY_RATE + "," + UPDATED_CCY_RATE);

        // Get all the transactionList where ccyRate equals to UPDATED_CCY_RATE
        defaultTransactionShouldNotBeFound("ccyRate.in=" + UPDATED_CCY_RATE);
    }

    @Test
    @Transactional
    public void getAllTransactionsByCcyRateIsNullOrNotNull() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where ccyRate is not null
        defaultTransactionShouldBeFound("ccyRate.specified=true");

        // Get all the transactionList where ccyRate is null
        defaultTransactionShouldNotBeFound("ccyRate.specified=false");
    }

    @Test
    @Transactional
    public void getAllTransactionsByCcyRateIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where ccyRate greater than or equals to DEFAULT_CCY_RATE
        defaultTransactionShouldBeFound("ccyRate.greaterOrEqualThan=" + DEFAULT_CCY_RATE);

        // Get all the transactionList where ccyRate greater than or equals to UPDATED_CCY_RATE
        defaultTransactionShouldNotBeFound("ccyRate.greaterOrEqualThan=" + UPDATED_CCY_RATE);
    }

    @Test
    @Transactional
    public void getAllTransactionsByCcyRateIsLessThanSomething() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where ccyRate less than or equals to DEFAULT_CCY_RATE
        defaultTransactionShouldNotBeFound("ccyRate.lessThan=" + DEFAULT_CCY_RATE);

        // Get all the transactionList where ccyRate less than or equals to UPDATED_CCY_RATE
        defaultTransactionShouldBeFound("ccyRate.lessThan=" + UPDATED_CCY_RATE);
    }

    @Test
    @Transactional
    public void getAllTransactionsByStatusIsEqualToSomething() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where status equals to DEFAULT_STATUS
        defaultTransactionShouldBeFound("status.equals=" + DEFAULT_STATUS);

        // Get all the transactionList where status equals to UPDATED_STATUS
        defaultTransactionShouldNotBeFound("status.equals=" + UPDATED_STATUS);
    }

    @Test
    @Transactional
    public void getAllTransactionsByStatusIsInShouldWork() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where status in DEFAULT_STATUS or UPDATED_STATUS
        defaultTransactionShouldBeFound("status.in=" + DEFAULT_STATUS + "," + UPDATED_STATUS);

        // Get all the transactionList where status equals to UPDATED_STATUS
        defaultTransactionShouldNotBeFound("status.in=" + UPDATED_STATUS);
    }

    @Test
    @Transactional
    public void getAllTransactionsByStatusIsNullOrNotNull() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where status is not null
        defaultTransactionShouldBeFound("status.specified=true");

        // Get all the transactionList where status is null
        defaultTransactionShouldNotBeFound("status.specified=false");
    }

    @Test
    @Transactional
    public void getAllTransactionsByCommentIsEqualToSomething() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where comment equals to DEFAULT_COMMENT
        defaultTransactionShouldBeFound("comment.equals=" + DEFAULT_COMMENT);

        // Get all the transactionList where comment equals to UPDATED_COMMENT
        defaultTransactionShouldNotBeFound("comment.equals=" + UPDATED_COMMENT);
    }

    @Test
    @Transactional
    public void getAllTransactionsByCommentIsInShouldWork() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where comment in DEFAULT_COMMENT or UPDATED_COMMENT
        defaultTransactionShouldBeFound("comment.in=" + DEFAULT_COMMENT + "," + UPDATED_COMMENT);

        // Get all the transactionList where comment equals to UPDATED_COMMENT
        defaultTransactionShouldNotBeFound("comment.in=" + UPDATED_COMMENT);
    }

    @Test
    @Transactional
    public void getAllTransactionsByCommentIsNullOrNotNull() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where comment is not null
        defaultTransactionShouldBeFound("comment.specified=true");

        // Get all the transactionList where comment is null
        defaultTransactionShouldNotBeFound("comment.specified=false");
    }

    @Test
    @Transactional
    public void getAllTransactionsByDateIsEqualToSomething() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where date equals to DEFAULT_DATE
        defaultTransactionShouldBeFound("date.equals=" + DEFAULT_DATE);

        // Get all the transactionList where date equals to UPDATED_DATE
        defaultTransactionShouldNotBeFound("date.equals=" + UPDATED_DATE);
    }

    @Test
    @Transactional
    public void getAllTransactionsByDateIsInShouldWork() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where date in DEFAULT_DATE or UPDATED_DATE
        defaultTransactionShouldBeFound("date.in=" + DEFAULT_DATE + "," + UPDATED_DATE);

        // Get all the transactionList where date equals to UPDATED_DATE
        defaultTransactionShouldNotBeFound("date.in=" + UPDATED_DATE);
    }

    @Test
    @Transactional
    public void getAllTransactionsByDateIsNullOrNotNull() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where date is not null
        defaultTransactionShouldBeFound("date.specified=true");

        // Get all the transactionList where date is null
        defaultTransactionShouldNotBeFound("date.specified=false");
    }

    @Test
    @Transactional
    public void getAllTransactionsByPayTypeIsEqualToSomething() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where payType equals to DEFAULT_PAY_TYPE
        defaultTransactionShouldBeFound("payType.equals=" + DEFAULT_PAY_TYPE);

        // Get all the transactionList where payType equals to UPDATED_PAY_TYPE
        defaultTransactionShouldNotBeFound("payType.equals=" + UPDATED_PAY_TYPE);
    }

    @Test
    @Transactional
    public void getAllTransactionsByPayTypeIsInShouldWork() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where payType in DEFAULT_PAY_TYPE or UPDATED_PAY_TYPE
        defaultTransactionShouldBeFound("payType.in=" + DEFAULT_PAY_TYPE + "," + UPDATED_PAY_TYPE);

        // Get all the transactionList where payType equals to UPDATED_PAY_TYPE
        defaultTransactionShouldNotBeFound("payType.in=" + UPDATED_PAY_TYPE);
    }

    @Test
    @Transactional
    public void getAllTransactionsByPayTypeIsNullOrNotNull() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where payType is not null
        defaultTransactionShouldBeFound("payType.specified=true");

        // Get all the transactionList where payType is null
        defaultTransactionShouldNotBeFound("payType.specified=false");
    }

    @Test
    @Transactional
    public void getAllTransactionsByRequestSubAccountIsEqualToSomething() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where requestSubAccount equals to DEFAULT_REQUEST_SUB_ACCOUNT
        defaultTransactionShouldBeFound("requestSubAccount.equals=" + DEFAULT_REQUEST_SUB_ACCOUNT);

        // Get all the transactionList where requestSubAccount equals to UPDATED_REQUEST_SUB_ACCOUNT
        defaultTransactionShouldNotBeFound("requestSubAccount.equals=" + UPDATED_REQUEST_SUB_ACCOUNT);
    }

    @Test
    @Transactional
    public void getAllTransactionsByRequestSubAccountIsInShouldWork() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where requestSubAccount in DEFAULT_REQUEST_SUB_ACCOUNT or UPDATED_REQUEST_SUB_ACCOUNT
        defaultTransactionShouldBeFound("requestSubAccount.in=" + DEFAULT_REQUEST_SUB_ACCOUNT + "," + UPDATED_REQUEST_SUB_ACCOUNT);

        // Get all the transactionList where requestSubAccount equals to UPDATED_REQUEST_SUB_ACCOUNT
        defaultTransactionShouldNotBeFound("requestSubAccount.in=" + UPDATED_REQUEST_SUB_ACCOUNT);
    }

    @Test
    @Transactional
    public void getAllTransactionsByRequestSubAccountIsNullOrNotNull() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where requestSubAccount is not null
        defaultTransactionShouldBeFound("requestSubAccount.specified=true");

        // Get all the transactionList where requestSubAccount is null
        defaultTransactionShouldNotBeFound("requestSubAccount.specified=false");
    }

    @Test
    @Transactional
    public void getAllTransactionsByEposMerchantIsEqualToSomething() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where eposMerchant equals to DEFAULT_EPOS_MERCHANT
        defaultTransactionShouldBeFound("eposMerchant.equals=" + DEFAULT_EPOS_MERCHANT);

        // Get all the transactionList where eposMerchant equals to UPDATED_EPOS_MERCHANT
        defaultTransactionShouldNotBeFound("eposMerchant.equals=" + UPDATED_EPOS_MERCHANT);
    }

    @Test
    @Transactional
    public void getAllTransactionsByEposMerchantIsInShouldWork() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where eposMerchant in DEFAULT_EPOS_MERCHANT or UPDATED_EPOS_MERCHANT
        defaultTransactionShouldBeFound("eposMerchant.in=" + DEFAULT_EPOS_MERCHANT + "," + UPDATED_EPOS_MERCHANT);

        // Get all the transactionList where eposMerchant equals to UPDATED_EPOS_MERCHANT
        defaultTransactionShouldNotBeFound("eposMerchant.in=" + UPDATED_EPOS_MERCHANT);
    }

    @Test
    @Transactional
    public void getAllTransactionsByEposMerchantIsNullOrNotNull() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where eposMerchant is not null
        defaultTransactionShouldBeFound("eposMerchant.specified=true");

        // Get all the transactionList where eposMerchant is null
        defaultTransactionShouldNotBeFound("eposMerchant.specified=false");
    }

    @Test
    @Transactional
    public void getAllTransactionsByEposTerminalIsEqualToSomething() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where eposTerminal equals to DEFAULT_EPOS_TERMINAL
        defaultTransactionShouldBeFound("eposTerminal.equals=" + DEFAULT_EPOS_TERMINAL);

        // Get all the transactionList where eposTerminal equals to UPDATED_EPOS_TERMINAL
        defaultTransactionShouldNotBeFound("eposTerminal.equals=" + UPDATED_EPOS_TERMINAL);
    }

    @Test
    @Transactional
    public void getAllTransactionsByEposTerminalIsInShouldWork() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where eposTerminal in DEFAULT_EPOS_TERMINAL or UPDATED_EPOS_TERMINAL
        defaultTransactionShouldBeFound("eposTerminal.in=" + DEFAULT_EPOS_TERMINAL + "," + UPDATED_EPOS_TERMINAL);

        // Get all the transactionList where eposTerminal equals to UPDATED_EPOS_TERMINAL
        defaultTransactionShouldNotBeFound("eposTerminal.in=" + UPDATED_EPOS_TERMINAL);
    }

    @Test
    @Transactional
    public void getAllTransactionsByEposTerminalIsNullOrNotNull() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where eposTerminal is not null
        defaultTransactionShouldBeFound("eposTerminal.specified=true");

        // Get all the transactionList where eposTerminal is null
        defaultTransactionShouldNotBeFound("eposTerminal.specified=false");
    }

    @Test
    @Transactional
    public void getAllTransactionsByMainIsEqualToSomething() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where main equals to DEFAULT_MAIN
        defaultTransactionShouldBeFound("main.equals=" + DEFAULT_MAIN);

        // Get all the transactionList where main equals to UPDATED_MAIN
        defaultTransactionShouldNotBeFound("main.equals=" + UPDATED_MAIN);
    }

    @Test
    @Transactional
    public void getAllTransactionsByMainIsInShouldWork() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where main in DEFAULT_MAIN or UPDATED_MAIN
        defaultTransactionShouldBeFound("main.in=" + DEFAULT_MAIN + "," + UPDATED_MAIN);

        // Get all the transactionList where main equals to UPDATED_MAIN
        defaultTransactionShouldNotBeFound("main.in=" + UPDATED_MAIN);
    }

    @Test
    @Transactional
    public void getAllTransactionsByMainIsNullOrNotNull() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where main is not null
        defaultTransactionShouldBeFound("main.specified=true");

        // Get all the transactionList where main is null
        defaultTransactionShouldNotBeFound("main.specified=false");
    }

    @Test
    @Transactional
    public void getAllTransactionsByAmountRatioIsEqualToSomething() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where amountRatio equals to DEFAULT_AMOUNT_RATIO
        defaultTransactionShouldBeFound("amountRatio.equals=" + DEFAULT_AMOUNT_RATIO);

        // Get all the transactionList where amountRatio equals to UPDATED_AMOUNT_RATIO
        defaultTransactionShouldNotBeFound("amountRatio.equals=" + UPDATED_AMOUNT_RATIO);
    }

    @Test
    @Transactional
    public void getAllTransactionsByAmountRatioIsInShouldWork() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where amountRatio in DEFAULT_AMOUNT_RATIO or UPDATED_AMOUNT_RATIO
        defaultTransactionShouldBeFound("amountRatio.in=" + DEFAULT_AMOUNT_RATIO + "," + UPDATED_AMOUNT_RATIO);

        // Get all the transactionList where amountRatio equals to UPDATED_AMOUNT_RATIO
        defaultTransactionShouldNotBeFound("amountRatio.in=" + UPDATED_AMOUNT_RATIO);
    }

    @Test
    @Transactional
    public void getAllTransactionsByAmountRatioIsNullOrNotNull() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where amountRatio is not null
        defaultTransactionShouldBeFound("amountRatio.specified=true");

        // Get all the transactionList where amountRatio is null
        defaultTransactionShouldNotBeFound("amountRatio.specified=false");
    }

    @Test
    @Transactional
    public void getAllTransactionsByAmountRatioIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where amountRatio greater than or equals to DEFAULT_AMOUNT_RATIO
        defaultTransactionShouldBeFound("amountRatio.greaterOrEqualThan=" + DEFAULT_AMOUNT_RATIO);

        // Get all the transactionList where amountRatio greater than or equals to UPDATED_AMOUNT_RATIO
        defaultTransactionShouldNotBeFound("amountRatio.greaterOrEqualThan=" + UPDATED_AMOUNT_RATIO);
    }

    @Test
    @Transactional
    public void getAllTransactionsByAmountRatioIsLessThanSomething() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where amountRatio less than or equals to DEFAULT_AMOUNT_RATIO
        defaultTransactionShouldNotBeFound("amountRatio.lessThan=" + DEFAULT_AMOUNT_RATIO);

        // Get all the transactionList where amountRatio less than or equals to UPDATED_AMOUNT_RATIO
        defaultTransactionShouldBeFound("amountRatio.lessThan=" + UPDATED_AMOUNT_RATIO);
    }

    @Test
    @Transactional
    public void getAllTransactionsBySenderBeanIsEqualToSomething() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where senderBean equals to DEFAULT_SENDER_BEAN
        defaultTransactionShouldBeFound("senderBean.equals=" + DEFAULT_SENDER_BEAN);

        // Get all the transactionList where senderBean equals to UPDATED_SENDER_BEAN
        defaultTransactionShouldNotBeFound("senderBean.equals=" + UPDATED_SENDER_BEAN);
    }

    @Test
    @Transactional
    public void getAllTransactionsBySenderBeanIsInShouldWork() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where senderBean in DEFAULT_SENDER_BEAN or UPDATED_SENDER_BEAN
        defaultTransactionShouldBeFound("senderBean.in=" + DEFAULT_SENDER_BEAN + "," + UPDATED_SENDER_BEAN);

        // Get all the transactionList where senderBean equals to UPDATED_SENDER_BEAN
        defaultTransactionShouldNotBeFound("senderBean.in=" + UPDATED_SENDER_BEAN);
    }

    @Test
    @Transactional
    public void getAllTransactionsBySenderBeanIsNullOrNotNull() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where senderBean is not null
        defaultTransactionShouldBeFound("senderBean.specified=true");

        // Get all the transactionList where senderBean is null
        defaultTransactionShouldNotBeFound("senderBean.specified=false");
    }

    /**
     * Executes the search, and checks that the default entity is returned
     */
    private void defaultTransactionShouldBeFound(String filter) throws Exception {
        restTransactionMockMvc
            .perform(get("/api/transactions?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(transaction.getId().intValue())))
            .andExpect(jsonPath("$.[*].requestAmount").value(hasItem(DEFAULT_REQUEST_AMOUNT.intValue())))
            .andExpect(jsonPath("$.[*].requestCcy").value(hasItem(DEFAULT_REQUEST_CCY)))
            .andExpect(jsonPath("$.[*].requestTrId").value(hasItem(DEFAULT_REQUEST_TR_ID.toString())))
            .andExpect(jsonPath("$.[*].requestLogin").value(hasItem(DEFAULT_REQUEST_LOGIN.toString())))
            .andExpect(jsonPath("$.[*].requestGroup").value(hasItem(DEFAULT_REQUEST_GROUP.toString())))
            .andExpect(jsonPath("$.[*].senderName").value(hasItem(DEFAULT_SENDER_NAME.toString())))
            .andExpect(jsonPath("$.[*].senderAccount").value(hasItem(DEFAULT_SENDER_ACCOUNT.toString())))
            .andExpect(jsonPath("$.[*].senderServiceId").value(hasItem(DEFAULT_SENDER_SERVICE_ID.intValue())))
            .andExpect(jsonPath("$.[*].senderAmount").value(hasItem(DEFAULT_SENDER_AMOUNT.intValue())))
            .andExpect(jsonPath("$.[*].senderCcy").value(hasItem(DEFAULT_SENDER_CCY)))
            .andExpect(jsonPath("$.[*].senderTrId").value(hasItem(DEFAULT_SENDER_TR_ID.toString())))
            .andExpect(jsonPath("$.[*].senderSuccess").value(hasItem(DEFAULT_SENDER_SUCCESS.booleanValue())))
            .andExpect(jsonPath("$.[*].senderDate").value(hasItem(DEFAULT_SENDER_DATE.toString())))
            .andExpect(jsonPath("$.[*].senderStatus").value(hasItem(DEFAULT_SENDER_STATUS.toString())))
            .andExpect(jsonPath("$.[*].senderStatusText").value(hasItem(DEFAULT_SENDER_STATUS_TEXT.toString())))
            .andExpect(jsonPath("$.[*].senderBillId").value(hasItem(DEFAULT_SENDER_BILL_ID.intValue())))
            .andExpect(jsonPath("$.[*].senderGateway").value(hasItem(DEFAULT_SENDER_GATEWAY.toString())))
            .andExpect(jsonPath("$.[*].gatewayAmount").value(hasItem(DEFAULT_GATEWAY_AMOUNT.doubleValue())))
            .andExpect(jsonPath("$.[*].recipientName").value(hasItem(DEFAULT_RECIPIENT_NAME.toString())))
            .andExpect(jsonPath("$.[*].recipientAccount").value(hasItem(DEFAULT_RECIPIENT_ACCOUNT.toString())))
            .andExpect(jsonPath("$.[*].recipientServiceId").value(hasItem(DEFAULT_RECIPIENT_SERVICE_ID.intValue())))
            .andExpect(jsonPath("$.[*].recipientAmount").value(hasItem(DEFAULT_RECIPIENT_AMOUNT.intValue())))
            .andExpect(jsonPath("$.[*].recipientCcy").value(hasItem(DEFAULT_RECIPIENT_CCY)))
            .andExpect(jsonPath("$.[*].recipientTrId").value(hasItem(DEFAULT_RECIPIENT_TR_ID.toString())))
            .andExpect(jsonPath("$.[*].recipientSuccess").value(hasItem(DEFAULT_RECIPIENT_SUCCESS.booleanValue())))
            .andExpect(jsonPath("$.[*].recipientDate").value(hasItem(DEFAULT_RECIPIENT_DATE.toString())))
            .andExpect(jsonPath("$.[*].recipientStatus").value(hasItem(DEFAULT_RECIPIENT_STATUS.toString())))
            .andExpect(jsonPath("$.[*].recipientStatusText").value(hasItem(DEFAULT_RECIPIENT_STATUS_TEXT.toString())))
            .andExpect(jsonPath("$.[*].recipientBillId").value(hasItem(DEFAULT_RECIPIENT_BILL_ID.intValue())))
            .andExpect(jsonPath("$.[*].recipientGateway").value(hasItem(DEFAULT_RECIPIENT_GATEWAY.toString())))
            .andExpect(jsonPath("$.[*].systemCommissionUpAmount").value(hasItem(DEFAULT_SYSTEM_COMMISSION_UP_AMOUNT.intValue())))
            .andExpect(jsonPath("$.[*].systemCommissionDownAmount").value(hasItem(DEFAULT_SYSTEM_COMMISSION_DOWN_AMOUNT.intValue())))
            .andExpect(jsonPath("$.[*].systemFeeAmount").value(hasItem(DEFAULT_SYSTEM_FEE_AMOUNT.intValue())))
            .andExpect(jsonPath("$.[*].agentFeeAmount").value(hasItem(DEFAULT_AGENT_FEE_AMOUNT.intValue())))
            .andExpect(jsonPath("$.[*].roundAmount").value(hasItem(DEFAULT_ROUND_AMOUNT.doubleValue())))
            .andExpect(jsonPath("$.[*].systemFee").value(hasItem(DEFAULT_SYSTEM_FEE.doubleValue())))
            .andExpect(jsonPath("$.[*].systemCurrency").value(hasItem(DEFAULT_SYSTEM_CURRENCY)))
            .andExpect(jsonPath("$.[*].ccyRate").value(hasItem(DEFAULT_CCY_RATE)))
            .andExpect(jsonPath("$.[*].status").value(hasItem(DEFAULT_STATUS.toString())))
            .andExpect(jsonPath("$.[*].comment").value(hasItem(DEFAULT_COMMENT.toString())))
            .andExpect(jsonPath("$.[*].date").value(hasItem(DEFAULT_DATE.toString())))
            .andExpect(jsonPath("$.[*].payType").value(hasItem(DEFAULT_PAY_TYPE.toString())))
            .andExpect(jsonPath("$.[*].requestSubAccount").value(hasItem(DEFAULT_REQUEST_SUB_ACCOUNT.toString())))
            .andExpect(jsonPath("$.[*].eposMerchant").value(hasItem(DEFAULT_EPOS_MERCHANT.toString())))
            .andExpect(jsonPath("$.[*].eposTerminal").value(hasItem(DEFAULT_EPOS_TERMINAL.toString())))
            .andExpect(jsonPath("$.[*].main").value(hasItem(DEFAULT_MAIN.booleanValue())))
            .andExpect(jsonPath("$.[*].amountRatio").value(hasItem(DEFAULT_AMOUNT_RATIO)))
            .andExpect(jsonPath("$.[*].senderBean").value(hasItem(DEFAULT_SENDER_BEAN.toString())));
        // Check, that the count call also returns 1
        restTransactionMockMvc
            .perform(get("/api/transactions/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(content().string("1"));
    }

    /**
     * Executes the search, and checks that the default entity is not returned
     */
    private void defaultTransactionShouldNotBeFound(String filter) throws Exception {
        restTransactionMockMvc
            .perform(get("/api/transactions?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty());

        // Check, that the count call also returns 0
        restTransactionMockMvc
            .perform(get("/api/transactions/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(content().string("0"));
    }

    @Test
    @Transactional
    public void getNonExistingTransaction() throws Exception {
        // Get the transaction
        restTransactionMockMvc.perform(get("/api/transactions/{id}", Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updateTransaction() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        int databaseSizeBeforeUpdate = transactionRepository.findAll().size();

        // Update the transaction
        Transaction updatedTransaction = transactionRepository.findById(transaction.getId()).get();
        // Disconnect from session so that the updates on updatedTransaction are not directly saved in db
        em.detach(updatedTransaction);
        updatedTransaction
            .requestAmount(UPDATED_REQUEST_AMOUNT)
            .requestCcy(UPDATED_REQUEST_CCY)
            .requestTrId(UPDATED_REQUEST_TR_ID)
            .requestLogin(UPDATED_REQUEST_LOGIN)
            .requestGroup(UPDATED_REQUEST_GROUP)
            .senderName(UPDATED_SENDER_NAME)
            .senderAccount(UPDATED_SENDER_ACCOUNT)
            .senderServiceId(UPDATED_SENDER_SERVICE_ID)
            .senderAmount(UPDATED_SENDER_AMOUNT)
            .senderCcy(UPDATED_SENDER_CCY)
            .senderTrId(UPDATED_SENDER_TR_ID)
            .senderSuccess(UPDATED_SENDER_SUCCESS)
            .senderDate(UPDATED_SENDER_DATE)
            .senderStatus(UPDATED_SENDER_STATUS)
            .senderStatusText(UPDATED_SENDER_STATUS_TEXT)
            .senderBillId(UPDATED_SENDER_BILL_ID)
            .senderGateway(UPDATED_SENDER_GATEWAY)
            .gatewayAmount(UPDATED_GATEWAY_AMOUNT)
            .recipientName(UPDATED_RECIPIENT_NAME)
            .recipientAccount(UPDATED_RECIPIENT_ACCOUNT)
            .recipientServiceId(UPDATED_RECIPIENT_SERVICE_ID)
            .recipientAmount(UPDATED_RECIPIENT_AMOUNT)
            .recipientCcy(UPDATED_RECIPIENT_CCY)
            .recipientTrId(UPDATED_RECIPIENT_TR_ID)
            .recipientSuccess(UPDATED_RECIPIENT_SUCCESS)
            .recipientDate(UPDATED_RECIPIENT_DATE)
            .recipientStatus(UPDATED_RECIPIENT_STATUS)
            .recipientStatusText(UPDATED_RECIPIENT_STATUS_TEXT)
            .recipientBillId(UPDATED_RECIPIENT_BILL_ID)
            .recipientGateway(UPDATED_RECIPIENT_GATEWAY)
            .systemCommissionUpAmount(UPDATED_SYSTEM_COMMISSION_UP_AMOUNT)
            .systemCommissionDownAmount(UPDATED_SYSTEM_COMMISSION_DOWN_AMOUNT)
            .systemFeeAmount(UPDATED_SYSTEM_FEE_AMOUNT)
            .agentFeeAmount(UPDATED_AGENT_FEE_AMOUNT)
            .roundAmount(UPDATED_ROUND_AMOUNT)
            .systemFee(UPDATED_SYSTEM_FEE)
            .systemCurrency(UPDATED_SYSTEM_CURRENCY)
            .ccyRate(UPDATED_CCY_RATE)
            .status(UPDATED_STATUS)
            .comment(UPDATED_COMMENT)
            .date(UPDATED_DATE)
            .payType(UPDATED_PAY_TYPE)
            .requestSubAccount(UPDATED_REQUEST_SUB_ACCOUNT)
            .eposMerchant(UPDATED_EPOS_MERCHANT)
            .eposTerminal(UPDATED_EPOS_TERMINAL)
            .main(UPDATED_MAIN)
            .amountRatio(UPDATED_AMOUNT_RATIO)
            .senderBean(UPDATED_SENDER_BEAN);

        TransactionDTO transactionDTO = transactionMapper.toDto(updatedTransaction);

        restTransactionMockMvc
            .perform(
                put("/api/transactions")
                    .contentType(TestUtil.APPLICATION_JSON_UTF8)
                    .content(TestUtil.convertObjectToJsonBytes(transactionDTO))
            )
            .andExpect(status().isOk());

        // Validate the Transaction in the database
        List<Transaction> transactionList = transactionRepository.findAll();
        assertThat(transactionList).hasSize(databaseSizeBeforeUpdate);
        Transaction testTransaction = transactionList.get(transactionList.size() - 1);
        assertThat(testTransaction.getRequestAmount()).isEqualTo(UPDATED_REQUEST_AMOUNT);
        assertThat(testTransaction.getRequestCcy()).isEqualTo(UPDATED_REQUEST_CCY);
        assertThat(testTransaction.getRequestTrId()).isEqualTo(UPDATED_REQUEST_TR_ID);
        assertThat(testTransaction.getRequestLogin()).isEqualTo(UPDATED_REQUEST_LOGIN);
        assertThat(testTransaction.getRequestGroup()).isEqualTo(UPDATED_REQUEST_GROUP);
        assertThat(testTransaction.getSenderName()).isEqualTo(UPDATED_SENDER_NAME);
        assertThat(testTransaction.getSenderAccount()).isEqualTo(UPDATED_SENDER_ACCOUNT);
        assertThat(testTransaction.getSenderServiceId()).isEqualTo(UPDATED_SENDER_SERVICE_ID);
        assertThat(testTransaction.getSenderAmount()).isEqualTo(UPDATED_SENDER_AMOUNT);
        assertThat(testTransaction.getSenderCcy()).isEqualTo(UPDATED_SENDER_CCY);
        assertThat(testTransaction.getSenderTrId()).isEqualTo(UPDATED_SENDER_TR_ID);
        assertThat(testTransaction.isSenderSuccess()).isEqualTo(UPDATED_SENDER_SUCCESS);
        assertThat(testTransaction.getSenderDate()).isEqualTo(UPDATED_SENDER_DATE);
        assertThat(testTransaction.getSenderStatus()).isEqualTo(UPDATED_SENDER_STATUS);
        assertThat(testTransaction.getSenderStatusText()).isEqualTo(UPDATED_SENDER_STATUS_TEXT);
        assertThat(testTransaction.getSenderBillId()).isEqualTo(UPDATED_SENDER_BILL_ID);
        assertThat(testTransaction.getSenderGateway()).isEqualTo(UPDATED_SENDER_GATEWAY);
        assertThat(testTransaction.getGatewayAmount()).isEqualTo(UPDATED_GATEWAY_AMOUNT);
        assertThat(testTransaction.getRecipientName()).isEqualTo(UPDATED_RECIPIENT_NAME);
        assertThat(testTransaction.getRecipientAccount()).isEqualTo(UPDATED_RECIPIENT_ACCOUNT);
        assertThat(testTransaction.getRecipientServiceId()).isEqualTo(UPDATED_RECIPIENT_SERVICE_ID);
        assertThat(testTransaction.getRecipientAmount()).isEqualTo(UPDATED_RECIPIENT_AMOUNT);
        assertThat(testTransaction.getRecipientCcy()).isEqualTo(UPDATED_RECIPIENT_CCY);
        assertThat(testTransaction.getRecipientTrId()).isEqualTo(UPDATED_RECIPIENT_TR_ID);
        assertThat(testTransaction.getRecipientSuccess()).isEqualTo(UPDATED_RECIPIENT_SUCCESS);
        assertThat(testTransaction.getRecipientDate()).isEqualTo(UPDATED_RECIPIENT_DATE);
        assertThat(testTransaction.getRecipientStatus()).isEqualTo(UPDATED_RECIPIENT_STATUS);
        assertThat(testTransaction.getRecipientStatusText()).isEqualTo(UPDATED_RECIPIENT_STATUS_TEXT);
        assertThat(testTransaction.getRecipientBillId()).isEqualTo(UPDATED_RECIPIENT_BILL_ID);
        assertThat(testTransaction.getRecipientGateway()).isEqualTo(UPDATED_RECIPIENT_GATEWAY);
        assertThat(testTransaction.getSystemCommissionUpAmount()).isEqualTo(UPDATED_SYSTEM_COMMISSION_UP_AMOUNT);
        assertThat(testTransaction.getSystemCommissionDownAmount()).isEqualTo(UPDATED_SYSTEM_COMMISSION_DOWN_AMOUNT);
        assertThat(testTransaction.getSystemFeeAmount()).isEqualTo(UPDATED_SYSTEM_FEE_AMOUNT);
        assertThat(testTransaction.getAgentFeeAmount()).isEqualTo(UPDATED_AGENT_FEE_AMOUNT);
        assertThat(testTransaction.getRoundAmount()).isEqualTo(UPDATED_ROUND_AMOUNT);
        assertThat(testTransaction.getSystemFee()).isEqualTo(UPDATED_SYSTEM_FEE);
        assertThat(testTransaction.getSystemCurrency()).isEqualTo(UPDATED_SYSTEM_CURRENCY);
        assertThat(testTransaction.getCcyRate()).isEqualTo(UPDATED_CCY_RATE);
        assertThat(testTransaction.getStatus()).isEqualTo(UPDATED_STATUS);
        assertThat(testTransaction.getComment()).isEqualTo(UPDATED_COMMENT);
        assertThat(testTransaction.getDate()).isEqualTo(UPDATED_DATE);
        assertThat(testTransaction.getPayType()).isEqualTo(UPDATED_PAY_TYPE);
        assertThat(testTransaction.getRequestSubAccount()).isEqualTo(UPDATED_REQUEST_SUB_ACCOUNT);
        assertThat(testTransaction.getEposMerchant()).isEqualTo(UPDATED_EPOS_MERCHANT);
        assertThat(testTransaction.getEposTerminal()).isEqualTo(UPDATED_EPOS_TERMINAL);
        assertThat(testTransaction.getMain()).isEqualTo(UPDATED_MAIN);
        assertThat(testTransaction.getAmountRatio()).isEqualTo(UPDATED_AMOUNT_RATIO);
        assertThat(testTransaction.getSenderBean()).isEqualTo(UPDATED_SENDER_BEAN);
    }

    @Test
    @Transactional
    public void updateNonExistingTransaction() throws Exception {
        int databaseSizeBeforeUpdate = transactionRepository.findAll().size();

        // Create the Transaction
        TransactionDTO transactionDTO = transactionMapper.toDto(transaction);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restTransactionMockMvc
            .perform(
                put("/api/transactions")
                    .contentType(TestUtil.APPLICATION_JSON_UTF8)
                    .content(TestUtil.convertObjectToJsonBytes(transactionDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Transaction in the database
        List<Transaction> transactionList = transactionRepository.findAll();
        assertThat(transactionList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    public void deleteTransaction() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        int databaseSizeBeforeDelete = transactionRepository.findAll().size();

        // Get the transaction
        restTransactionMockMvc
            .perform(delete("/api/transactions/{id}", transaction.getId()).accept(TestUtil.APPLICATION_JSON_UTF8))
            .andExpect(status().isOk());

        // Validate the database is empty
        List<Transaction> transactionList = transactionRepository.findAll();
        assertThat(transactionList).hasSize(databaseSizeBeforeDelete - 1);
    }

    @Test
    @Transactional
    public void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Transaction.class);
        Transaction transaction1 = new Transaction();
        transaction1.setId(1L);
        Transaction transaction2 = new Transaction();
        transaction2.setId(transaction1.getId());
        assertThat(transaction1).isEqualTo(transaction2);
        transaction2.setId(2L);
        assertThat(transaction1).isNotEqualTo(transaction2);
        transaction1.setId(null);
        assertThat(transaction1).isNotEqualTo(transaction2);
    }

    @Test
    @Transactional
    public void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(TransactionDTO.class);
        TransactionDTO transactionDTO1 = new TransactionDTO();
        transactionDTO1.setId(1L);
        TransactionDTO transactionDTO2 = new TransactionDTO();
        assertThat(transactionDTO1).isNotEqualTo(transactionDTO2);
        transactionDTO2.setId(transactionDTO1.getId());
        assertThat(transactionDTO1).isEqualTo(transactionDTO2);
        transactionDTO2.setId(2L);
        assertThat(transactionDTO1).isNotEqualTo(transactionDTO2);
        transactionDTO1.setId(null);
        assertThat(transactionDTO1).isNotEqualTo(transactionDTO2);
    }

    @Test
    @Transactional
    public void testEntityFromId() {
        assertThat(transactionMapper.fromId(42L).getId()).isEqualTo(42);
        assertThat(transactionMapper.fromId(null)).isNull();
    }
}
