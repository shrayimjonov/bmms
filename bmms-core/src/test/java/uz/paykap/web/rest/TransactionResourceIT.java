package uz.paykap.web.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import uz.devops.bmcore.enumeration.PayType;
import uz.devops.bmcore.enumeration.TransactionStatus;
import uz.paykap.IntegrationTest;
import uz.paykap.domain.Transaction;
import uz.paykap.repository.TransactionRepository;
import uz.paykap.service.TransactionQueryService;
import uz.paykap.service.criteria.TransactionCriteria;
import uz.paykap.service.dto.TransactionDTO;
import uz.paykap.service.mapper.TransactionMapper;

import javax.persistence.EntityManager;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for the {@link TransactionResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class TransactionResourceIT {

    private static final Long DEFAULT_REQUEST_AMOUNT = 1L;
    private static final Long UPDATED_REQUEST_AMOUNT = 2L;
    private static final Long SMALLER_REQUEST_AMOUNT = 1L - 1L;

    private static final Integer DEFAULT_REQUEST_CCY = 1;
    private static final Integer UPDATED_REQUEST_CCY = 2;
    private static final Integer SMALLER_REQUEST_CCY = 1 - 1;

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

    private static final Long DEFAULT_SENDER_SERVICE_ID = 0L;
    private static final Long UPDATED_SENDER_SERVICE_ID = 2L;
    private static final Long SMALLER_SENDER_SERVICE_ID = 1L - 1L;

    private static final Long DEFAULT_SENDER_AMOUNT = 1L;
    private static final Long UPDATED_SENDER_AMOUNT = 2L;
    private static final Long SMALLER_SENDER_AMOUNT = 1L - 1L;

    private static final Integer DEFAULT_SENDER_CCY = 1;
    private static final Integer UPDATED_SENDER_CCY = 2;
    private static final Integer SMALLER_SENDER_CCY = 1 - 1;

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

    private static final Long DEFAULT_SENDER_BILL_ID = 0L;
    private static final Long UPDATED_SENDER_BILL_ID = 2L;
    private static final Long SMALLER_SENDER_BILL_ID = 0L - 1L;

    private static final String DEFAULT_SENDER_GATEWAY = "AAAAAAAAAA";
    private static final String UPDATED_SENDER_GATEWAY = "BBBBBBBBBB";

    private static final Double DEFAULT_GATEWAY_AMOUNT = 1D;
    private static final Double UPDATED_GATEWAY_AMOUNT = 2D;
    private static final Double SMALLER_GATEWAY_AMOUNT = 1D - 1D;

    private static final String DEFAULT_RECIPIENT_NAME = "AAAAAAAAAA";
    private static final String UPDATED_RECIPIENT_NAME = "BBBBBBBBBB";

    private static final String DEFAULT_RECIPIENT_ACCOUNT = "AAAAAAAAAA";
    private static final String UPDATED_RECIPIENT_ACCOUNT = "BBBBBBBBBB";

    private static final Long DEFAULT_RECIPIENT_SERVICE_ID = 1L;
    private static final Long UPDATED_RECIPIENT_SERVICE_ID = 2L;
    private static final Long SMALLER_RECIPIENT_SERVICE_ID = 1L - 1L;

    private static final Long DEFAULT_RECIPIENT_AMOUNT = 1L;
    private static final Long UPDATED_RECIPIENT_AMOUNT = 2L;
    private static final Long SMALLER_RECIPIENT_AMOUNT = 1L - 1L;

    private static final Integer DEFAULT_RECIPIENT_CCY = 1;
    private static final Integer UPDATED_RECIPIENT_CCY = 2;
    private static final Integer SMALLER_RECIPIENT_CCY = 1 - 1;

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
    private static final Long SMALLER_RECIPIENT_BILL_ID = 1L - 1L;

    private static final String DEFAULT_RECIPIENT_GATEWAY = "AAAAAAAAAA";
    private static final String UPDATED_RECIPIENT_GATEWAY = "BBBBBBBBBB";

    private static final Long DEFAULT_SYSTEM_COMMISSION_UP_AMOUNT = 1L;
    private static final Long UPDATED_SYSTEM_COMMISSION_UP_AMOUNT = 2L;
    private static final Long SMALLER_SYSTEM_COMMISSION_UP_AMOUNT = 1L - 1L;

    private static final Long DEFAULT_SYSTEM_COMMISSION_DOWN_AMOUNT = 1L;
    private static final Long UPDATED_SYSTEM_COMMISSION_DOWN_AMOUNT = 2L;
    private static final Long SMALLER_SYSTEM_COMMISSION_DOWN_AMOUNT = 1L - 1L;

    private static final Long DEFAULT_SYSTEM_FEE_AMOUNT = 1L;
    private static final Long UPDATED_SYSTEM_FEE_AMOUNT = 2L;
    private static final Long SMALLER_SYSTEM_FEE_AMOUNT = 1L - 1L;

    private static final Long DEFAULT_AGENT_FEE_AMOUNT = 1L;
    private static final Long UPDATED_AGENT_FEE_AMOUNT = 2L;
    private static final Long SMALLER_AGENT_FEE_AMOUNT = 1L - 1L;

    private static final Double DEFAULT_ROUND_AMOUNT = 0D;
    private static final Double UPDATED_ROUND_AMOUNT = 1D;
    private static final Double SMALLER_ROUND_AMOUNT = 0D - 1D;

    private static final Double DEFAULT_SYSTEM_FEE = 0D;
    private static final Double UPDATED_SYSTEM_FEE = 1D;
    private static final Double SMALLER_SYSTEM_FEE = 0D - 1D;

    private static final Integer DEFAULT_SYSTEM_CURRENCY = 1;
    private static final Integer UPDATED_SYSTEM_CURRENCY = 2;
    private static final Integer SMALLER_SYSTEM_CURRENCY = 1 - 1;

    private static final Integer DEFAULT_CCY_RATE = 1;
    private static final Integer UPDATED_CCY_RATE = 2;
    private static final Integer SMALLER_CCY_RATE = 1 - 1;

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
    private static final Integer SMALLER_AMOUNT_RATIO = 1 - 1;

    private static final String DEFAULT_SENDER_BEAN = "AAAAAAAAAA";
    private static final String UPDATED_SENDER_BEAN = "BBBBBBBBBB";

    private static final String DEFAULT_CATEGORY = "AAAAAAAAAA";
    private static final String UPDATED_CATEGORY = "BBBBBBBBBB";

    private static final String ENTITY_API_URL = "/api/transactions";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong count = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private TransactionQueryService transactionQueryService;

    @Autowired
    private TransactionMapper transactionMapper;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restTransactionMockMvc;

    private Transaction transaction;

    /**
     * Create an entity for this test.
     * <p>
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Transaction createEntity(EntityManager em) {
        Transaction transaction = new Transaction()
                .requestAmount(DEFAULT_REQUEST_AMOUNT)
                .requestCcy(DEFAULT_REQUEST_CCY)
                .requestTrId(DEFAULT_REQUEST_TR_ID)
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
                .senderBean(DEFAULT_SENDER_BEAN)
                .category(DEFAULT_CATEGORY);
        return transaction;
    }

    /**
     * Create an updated entity for this test.
     * <p>
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Transaction createUpdatedEntity(EntityManager em) {
        Transaction transaction = new Transaction()
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
                .senderBean(UPDATED_SENDER_BEAN)
                .category(UPDATED_CATEGORY);
        return transaction;
    }

    @BeforeEach
    public void initTest() {
        transaction = createEntity(em);
    }

    @Test
    @Transactional
    void createTransaction() throws Exception {
        int databaseSizeBeforeCreate = transactionRepository.findAll().size();
        // Create the Transaction
        TransactionDTO transactionDTO = transactionMapper.toDto(transaction);
        restTransactionMockMvc
                .perform(
                        post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(transactionDTO))
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
        assertThat(testTransaction.getCategory()).isEqualTo(DEFAULT_CATEGORY);
    }

    @Test
    @Transactional
    void createTransactionWithExistingId() throws Exception {
        // Create the Transaction with an existing ID
        transaction.setId(1L);
        TransactionDTO transactionDTO = transactionMapper.toDto(transaction);

        int databaseSizeBeforeCreate = transactionRepository.findAll().size();

        // An entity with an existing ID cannot be created, so this API call must fail
        restTransactionMockMvc
                .perform(
                        post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(transactionDTO))
                )
                .andExpect(status().isBadRequest());

        // Validate the Transaction in the database
        List<Transaction> transactionList = transactionRepository.findAll();
        assertThat(transactionList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkRequestTrIdIsRequired() throws Exception {
        int databaseSizeBeforeTest = transactionRepository.findAll().size();
        // set the field null
        transaction.setRequestTrId(null);

        // Create the Transaction, which fails.
        TransactionDTO transactionDTO = transactionMapper.toDto(transaction);

        restTransactionMockMvc
                .perform(
                        post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(transactionDTO))
                )
                .andExpect(status().isBadRequest());

        List<Transaction> transactionList = transactionRepository.findAll();
        assertThat(transactionList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkRequestLoginIsRequired() throws Exception {
        int databaseSizeBeforeTest = transactionRepository.findAll().size();
        // set the field null
        transaction.setRequestLogin(null);

        // Create the Transaction, which fails.
        TransactionDTO transactionDTO = transactionMapper.toDto(transaction);

        restTransactionMockMvc
                .perform(
                        post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(transactionDTO))
                )
                .andExpect(status().isBadRequest());

        List<Transaction> transactionList = transactionRepository.findAll();
        assertThat(transactionList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkCommentIsRequired() throws Exception {
        int databaseSizeBeforeTest = transactionRepository.findAll().size();
        // set the field null
        transaction.setComment(null);

        // Create the Transaction, which fails.
        TransactionDTO transactionDTO = transactionMapper.toDto(transaction);

        restTransactionMockMvc
                .perform(
                        post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(transactionDTO))
                )
                .andExpect(status().isBadRequest());

        List<Transaction> transactionList = transactionRepository.findAll();
        assertThat(transactionList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkPayTypeIsRequired() throws Exception {
        int databaseSizeBeforeTest = transactionRepository.findAll().size();
        // set the field null
        transaction.setPayType(null);

        // Create the Transaction, which fails.
        TransactionDTO transactionDTO = transactionMapper.toDto(transaction);

        restTransactionMockMvc
                .perform(
                        post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(transactionDTO))
                )
                .andExpect(status().isBadRequest());

        List<Transaction> transactionList = transactionRepository.findAll();
        assertThat(transactionList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkAmountRatioIsRequired() throws Exception {
        int databaseSizeBeforeTest = transactionRepository.findAll().size();
        // set the field null
        transaction.setAmountRatio(null);

        // Create the Transaction, which fails.
        TransactionDTO transactionDTO = transactionMapper.toDto(transaction);

        restTransactionMockMvc
                .perform(
                        post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(transactionDTO))
                )
                .andExpect(status().isBadRequest());

        List<Transaction> transactionList = transactionRepository.findAll();
        assertThat(transactionList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllTransactions() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList
        restTransactionMockMvc
                .perform(get(ENTITY_API_URL + "?sort=id,desc"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.[*].id").value(hasItem(transaction.getId().intValue())))
                .andExpect(jsonPath("$.[*].requestAmount").value(hasItem(DEFAULT_REQUEST_AMOUNT.intValue())))
                .andExpect(jsonPath("$.[*].requestCcy").value(hasItem(DEFAULT_REQUEST_CCY)))
                .andExpect(jsonPath("$.[*].requestTrId").value(hasItem(DEFAULT_REQUEST_TR_ID)))
                .andExpect(jsonPath("$.[*].requestLogin").value(hasItem(DEFAULT_REQUEST_LOGIN)))
                .andExpect(jsonPath("$.[*].requestGroup").value(hasItem(DEFAULT_REQUEST_GROUP)))
                .andExpect(jsonPath("$.[*].senderName").value(hasItem(DEFAULT_SENDER_NAME)))
                .andExpect(jsonPath("$.[*].senderAccount").value(hasItem(DEFAULT_SENDER_ACCOUNT)))
                .andExpect(jsonPath("$.[*].senderServiceId").value(hasItem(DEFAULT_SENDER_SERVICE_ID.intValue())))
                .andExpect(jsonPath("$.[*].senderAmount").value(hasItem(DEFAULT_SENDER_AMOUNT.intValue())))
                .andExpect(jsonPath("$.[*].senderCcy").value(hasItem(DEFAULT_SENDER_CCY)))
                .andExpect(jsonPath("$.[*].senderTrId").value(hasItem(DEFAULT_SENDER_TR_ID)))
                .andExpect(jsonPath("$.[*].senderSuccess").value(hasItem(DEFAULT_SENDER_SUCCESS.booleanValue())))
                .andExpect(jsonPath("$.[*].senderDate").value(hasItem(DEFAULT_SENDER_DATE.toString())))
                .andExpect(jsonPath("$.[*].senderStatus").value(hasItem(DEFAULT_SENDER_STATUS)))
                .andExpect(jsonPath("$.[*].senderStatusText").value(hasItem(DEFAULT_SENDER_STATUS_TEXT)))
                .andExpect(jsonPath("$.[*].senderBillId").value(hasItem(DEFAULT_SENDER_BILL_ID.intValue())))
                .andExpect(jsonPath("$.[*].senderGateway").value(hasItem(DEFAULT_SENDER_GATEWAY)))
                .andExpect(jsonPath("$.[*].gatewayAmount").value(hasItem(DEFAULT_GATEWAY_AMOUNT.doubleValue())))
                .andExpect(jsonPath("$.[*].recipientName").value(hasItem(DEFAULT_RECIPIENT_NAME)))
                .andExpect(jsonPath("$.[*].recipientAccount").value(hasItem(DEFAULT_RECIPIENT_ACCOUNT)))
                .andExpect(jsonPath("$.[*].recipientServiceId").value(hasItem(DEFAULT_RECIPIENT_SERVICE_ID.intValue())))
                .andExpect(jsonPath("$.[*].recipientAmount").value(hasItem(DEFAULT_RECIPIENT_AMOUNT.intValue())))
                .andExpect(jsonPath("$.[*].recipientCcy").value(hasItem(DEFAULT_RECIPIENT_CCY)))
                .andExpect(jsonPath("$.[*].recipientTrId").value(hasItem(DEFAULT_RECIPIENT_TR_ID)))
                .andExpect(jsonPath("$.[*].recipientSuccess").value(hasItem(DEFAULT_RECIPIENT_SUCCESS.booleanValue())))
                .andExpect(jsonPath("$.[*].recipientDate").value(hasItem(DEFAULT_RECIPIENT_DATE.toString())))
                .andExpect(jsonPath("$.[*].recipientStatus").value(hasItem(DEFAULT_RECIPIENT_STATUS)))
                .andExpect(jsonPath("$.[*].recipientStatusText").value(hasItem(DEFAULT_RECIPIENT_STATUS_TEXT)))
                .andExpect(jsonPath("$.[*].recipientBillId").value(hasItem(DEFAULT_RECIPIENT_BILL_ID.intValue())))
                .andExpect(jsonPath("$.[*].recipientGateway").value(hasItem(DEFAULT_RECIPIENT_GATEWAY)))
                .andExpect(jsonPath("$.[*].systemCommissionUpAmount").value(hasItem(DEFAULT_SYSTEM_COMMISSION_UP_AMOUNT.intValue())))
                .andExpect(jsonPath("$.[*].systemCommissionDownAmount").value(hasItem(DEFAULT_SYSTEM_COMMISSION_DOWN_AMOUNT.intValue())))
                .andExpect(jsonPath("$.[*].systemFeeAmount").value(hasItem(DEFAULT_SYSTEM_FEE_AMOUNT.intValue())))
                .andExpect(jsonPath("$.[*].agentFeeAmount").value(hasItem(DEFAULT_AGENT_FEE_AMOUNT.intValue())))
                .andExpect(jsonPath("$.[*].roundAmount").value(hasItem(DEFAULT_ROUND_AMOUNT.doubleValue())))
                .andExpect(jsonPath("$.[*].systemFee").value(hasItem(DEFAULT_SYSTEM_FEE.doubleValue())))
                .andExpect(jsonPath("$.[*].systemCurrency").value(hasItem(DEFAULT_SYSTEM_CURRENCY)))
                .andExpect(jsonPath("$.[*].ccyRate").value(hasItem(DEFAULT_CCY_RATE)))
                .andExpect(jsonPath("$.[*].status").value(hasItem(DEFAULT_STATUS.toString())))
                .andExpect(jsonPath("$.[*].comment").value(hasItem(DEFAULT_COMMENT)))
                .andExpect(jsonPath("$.[*].date").value(hasItem(DEFAULT_DATE.toString())))
                .andExpect(jsonPath("$.[*].payType").value(hasItem(DEFAULT_PAY_TYPE.toString())))
                .andExpect(jsonPath("$.[*].requestSubAccount").value(hasItem(DEFAULT_REQUEST_SUB_ACCOUNT)))
                .andExpect(jsonPath("$.[*].eposMerchant").value(hasItem(DEFAULT_EPOS_MERCHANT)))
                .andExpect(jsonPath("$.[*].eposTerminal").value(hasItem(DEFAULT_EPOS_TERMINAL)))
                .andExpect(jsonPath("$.[*].main").value(hasItem(DEFAULT_MAIN.booleanValue())))
                .andExpect(jsonPath("$.[*].amountRatio").value(hasItem(DEFAULT_AMOUNT_RATIO)))
                .andExpect(jsonPath("$.[*].senderBean").value(hasItem(DEFAULT_SENDER_BEAN)))
                .andExpect(jsonPath("$.[*].category").value(hasItem(DEFAULT_CATEGORY)));
    }

    @Test
    @Transactional
    void getTransaction() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get the transaction
        restTransactionMockMvc
                .perform(get(ENTITY_API_URL_ID, transaction.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.id").value(transaction.getId().intValue()))
                .andExpect(jsonPath("$.requestAmount").value(DEFAULT_REQUEST_AMOUNT.intValue()))
                .andExpect(jsonPath("$.requestCcy").value(DEFAULT_REQUEST_CCY))
                .andExpect(jsonPath("$.requestTrId").value(DEFAULT_REQUEST_TR_ID))
                .andExpect(jsonPath("$.requestLogin").value(DEFAULT_REQUEST_LOGIN))
                .andExpect(jsonPath("$.requestGroup").value(DEFAULT_REQUEST_GROUP))
                .andExpect(jsonPath("$.senderName").value(DEFAULT_SENDER_NAME))
                .andExpect(jsonPath("$.senderAccount").value(DEFAULT_SENDER_ACCOUNT))
                .andExpect(jsonPath("$.senderServiceId").value(DEFAULT_SENDER_SERVICE_ID.intValue()))
                .andExpect(jsonPath("$.senderAmount").value(DEFAULT_SENDER_AMOUNT.intValue()))
                .andExpect(jsonPath("$.senderCcy").value(DEFAULT_SENDER_CCY))
                .andExpect(jsonPath("$.senderTrId").value(DEFAULT_SENDER_TR_ID))
                .andExpect(jsonPath("$.senderSuccess").value(DEFAULT_SENDER_SUCCESS.booleanValue()))
                .andExpect(jsonPath("$.senderDate").value(DEFAULT_SENDER_DATE.toString()))
                .andExpect(jsonPath("$.senderStatus").value(DEFAULT_SENDER_STATUS))
                .andExpect(jsonPath("$.senderStatusText").value(DEFAULT_SENDER_STATUS_TEXT))
                .andExpect(jsonPath("$.senderBillId").value(DEFAULT_SENDER_BILL_ID.intValue()))
                .andExpect(jsonPath("$.senderGateway").value(DEFAULT_SENDER_GATEWAY))
                .andExpect(jsonPath("$.gatewayAmount").value(DEFAULT_GATEWAY_AMOUNT.doubleValue()))
                .andExpect(jsonPath("$.recipientName").value(DEFAULT_RECIPIENT_NAME))
                .andExpect(jsonPath("$.recipientAccount").value(DEFAULT_RECIPIENT_ACCOUNT))
                .andExpect(jsonPath("$.recipientServiceId").value(DEFAULT_RECIPIENT_SERVICE_ID.intValue()))
                .andExpect(jsonPath("$.recipientAmount").value(DEFAULT_RECIPIENT_AMOUNT.intValue()))
                .andExpect(jsonPath("$.recipientCcy").value(DEFAULT_RECIPIENT_CCY))
                .andExpect(jsonPath("$.recipientTrId").value(DEFAULT_RECIPIENT_TR_ID))
                .andExpect(jsonPath("$.recipientSuccess").value(DEFAULT_RECIPIENT_SUCCESS.booleanValue()))
                .andExpect(jsonPath("$.recipientDate").value(DEFAULT_RECIPIENT_DATE.toString()))
                .andExpect(jsonPath("$.recipientStatus").value(DEFAULT_RECIPIENT_STATUS))
                .andExpect(jsonPath("$.recipientStatusText").value(DEFAULT_RECIPIENT_STATUS_TEXT))
                .andExpect(jsonPath("$.recipientBillId").value(DEFAULT_RECIPIENT_BILL_ID.intValue()))
                .andExpect(jsonPath("$.recipientGateway").value(DEFAULT_RECIPIENT_GATEWAY))
                .andExpect(jsonPath("$.systemCommissionUpAmount").value(DEFAULT_SYSTEM_COMMISSION_UP_AMOUNT.intValue()))
                .andExpect(jsonPath("$.systemCommissionDownAmount").value(DEFAULT_SYSTEM_COMMISSION_DOWN_AMOUNT.intValue()))
                .andExpect(jsonPath("$.systemFeeAmount").value(DEFAULT_SYSTEM_FEE_AMOUNT.intValue()))
                .andExpect(jsonPath("$.agentFeeAmount").value(DEFAULT_AGENT_FEE_AMOUNT.intValue()))
                .andExpect(jsonPath("$.roundAmount").value(DEFAULT_ROUND_AMOUNT.doubleValue()))
                .andExpect(jsonPath("$.systemFee").value(DEFAULT_SYSTEM_FEE.doubleValue()))
                .andExpect(jsonPath("$.systemCurrency").value(DEFAULT_SYSTEM_CURRENCY))
                .andExpect(jsonPath("$.ccyRate").value(DEFAULT_CCY_RATE))
                .andExpect(jsonPath("$.status").value(DEFAULT_STATUS.toString()))
                .andExpect(jsonPath("$.comment").value(DEFAULT_COMMENT))
                .andExpect(jsonPath("$.date").value(DEFAULT_DATE.toString()))
                .andExpect(jsonPath("$.payType").value(DEFAULT_PAY_TYPE.toString()))
                .andExpect(jsonPath("$.requestSubAccount").value(DEFAULT_REQUEST_SUB_ACCOUNT))
                .andExpect(jsonPath("$.eposMerchant").value(DEFAULT_EPOS_MERCHANT))
                .andExpect(jsonPath("$.eposTerminal").value(DEFAULT_EPOS_TERMINAL))
                .andExpect(jsonPath("$.main").value(DEFAULT_MAIN.booleanValue()))
                .andExpect(jsonPath("$.amountRatio").value(DEFAULT_AMOUNT_RATIO))
                .andExpect(jsonPath("$.senderBean").value(DEFAULT_SENDER_BEAN))
                .andExpect(jsonPath("$.category").value(DEFAULT_CATEGORY));
    }

    @Test
    @Transactional
    void getTransactionsByIdFiltering() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        Long id = transaction.getId();

        defaultTransactionShouldBeFound("id.equals=" + id);
        defaultTransactionShouldNotBeFound("id.notEquals=" + id);

        defaultTransactionShouldBeFound("id.greaterThanOrEqual=" + id);
        defaultTransactionShouldNotBeFound("id.greaterThan=" + id);

        defaultTransactionShouldBeFound("id.lessThanOrEqual=" + id);
        defaultTransactionShouldNotBeFound("id.lessThan=" + id);
    }

    @Test
    @Transactional
    void getAllTransactionsByRequestAmountIsEqualToSomething() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where requestAmount equals to DEFAULT_REQUEST_AMOUNT
        defaultTransactionShouldBeFound("requestAmount.equals=" + DEFAULT_REQUEST_AMOUNT);

        // Get all the transactionList where requestAmount equals to UPDATED_REQUEST_AMOUNT
        defaultTransactionShouldNotBeFound("requestAmount.equals=" + UPDATED_REQUEST_AMOUNT);
    }

    @Test
    @Transactional
    void getAllTransactionsByRequestAmountIsNotEqualToSomething() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where requestAmount not equals to DEFAULT_REQUEST_AMOUNT
        defaultTransactionShouldNotBeFound("requestAmount.notEquals=" + DEFAULT_REQUEST_AMOUNT);

        // Get all the transactionList where requestAmount not equals to UPDATED_REQUEST_AMOUNT
        defaultTransactionShouldBeFound("requestAmount.notEquals=" + UPDATED_REQUEST_AMOUNT);
    }

    @Test
    @Transactional
    void getAllTransactionsByRequestAmountIsInShouldWork() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where requestAmount in DEFAULT_REQUEST_AMOUNT or UPDATED_REQUEST_AMOUNT
        defaultTransactionShouldBeFound("requestAmount.in=" + DEFAULT_REQUEST_AMOUNT + "," + UPDATED_REQUEST_AMOUNT);

        // Get all the transactionList where requestAmount equals to UPDATED_REQUEST_AMOUNT
        defaultTransactionShouldNotBeFound("requestAmount.in=" + UPDATED_REQUEST_AMOUNT);
    }

    @Test
    @Transactional
    void getAllTransactionsByRequestAmountIsNullOrNotNull() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where requestAmount is not null
        defaultTransactionShouldBeFound("requestAmount.specified=true");

        // Get all the transactionList where requestAmount is null
        defaultTransactionShouldNotBeFound("requestAmount.specified=false");
    }

    @Test
    @Transactional
    void getAllTransactionsByRequestAmountIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where requestAmount is greater than or equal to DEFAULT_REQUEST_AMOUNT
        defaultTransactionShouldBeFound("requestAmount.greaterThanOrEqual=" + DEFAULT_REQUEST_AMOUNT);

        // Get all the transactionList where requestAmount is greater than or equal to UPDATED_REQUEST_AMOUNT
        defaultTransactionShouldNotBeFound("requestAmount.greaterThanOrEqual=" + UPDATED_REQUEST_AMOUNT);
    }

    @Test
    @Transactional
    void getAllTransactionsByRequestAmountIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where requestAmount is less than or equal to DEFAULT_REQUEST_AMOUNT
        defaultTransactionShouldBeFound("requestAmount.lessThanOrEqual=" + DEFAULT_REQUEST_AMOUNT);

        // Get all the transactionList where requestAmount is less than or equal to SMALLER_REQUEST_AMOUNT
        defaultTransactionShouldNotBeFound("requestAmount.lessThanOrEqual=" + SMALLER_REQUEST_AMOUNT);
    }

    @Test
    @Transactional
    void getAllTransactionsByRequestAmountIsLessThanSomething() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where requestAmount is less than DEFAULT_REQUEST_AMOUNT
        defaultTransactionShouldNotBeFound("requestAmount.lessThan=" + DEFAULT_REQUEST_AMOUNT);

        // Get all the transactionList where requestAmount is less than UPDATED_REQUEST_AMOUNT
        defaultTransactionShouldBeFound("requestAmount.lessThan=" + UPDATED_REQUEST_AMOUNT);
    }

    @Test
    @Transactional
    void getAllTransactionsByRequestAmountIsGreaterThanSomething() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where requestAmount is greater than DEFAULT_REQUEST_AMOUNT
        defaultTransactionShouldNotBeFound("requestAmount.greaterThan=" + DEFAULT_REQUEST_AMOUNT);

        // Get all the transactionList where requestAmount is greater than SMALLER_REQUEST_AMOUNT
        defaultTransactionShouldBeFound("requestAmount.greaterThan=" + SMALLER_REQUEST_AMOUNT);
    }

    @Test
    @Transactional
    void getAllTransactionsByRequestCcyIsEqualToSomething() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where requestCcy equals to DEFAULT_REQUEST_CCY
        defaultTransactionShouldBeFound("requestCcy.equals=" + DEFAULT_REQUEST_CCY);

        // Get all the transactionList where requestCcy equals to UPDATED_REQUEST_CCY
        defaultTransactionShouldNotBeFound("requestCcy.equals=" + UPDATED_REQUEST_CCY);
    }

    @Test
    @Transactional
    void getAllTransactionsByRequestCcyIsNotEqualToSomething() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where requestCcy not equals to DEFAULT_REQUEST_CCY
        defaultTransactionShouldNotBeFound("requestCcy.notEquals=" + DEFAULT_REQUEST_CCY);

        // Get all the transactionList where requestCcy not equals to UPDATED_REQUEST_CCY
        defaultTransactionShouldBeFound("requestCcy.notEquals=" + UPDATED_REQUEST_CCY);
    }

    @Test
    @Transactional
    void getAllTransactionsByRequestCcyIsInShouldWork() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where requestCcy in DEFAULT_REQUEST_CCY or UPDATED_REQUEST_CCY
        defaultTransactionShouldBeFound("requestCcy.in=" + DEFAULT_REQUEST_CCY + "," + UPDATED_REQUEST_CCY);

        // Get all the transactionList where requestCcy equals to UPDATED_REQUEST_CCY
        defaultTransactionShouldNotBeFound("requestCcy.in=" + UPDATED_REQUEST_CCY);
    }

    @Test
    @Transactional
    void getAllTransactionsByRequestCcyIsNullOrNotNull() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where requestCcy is not null
        defaultTransactionShouldBeFound("requestCcy.specified=true");

        // Get all the transactionList where requestCcy is null
        defaultTransactionShouldNotBeFound("requestCcy.specified=false");
    }

    @Test
    @Transactional
    void getAllTransactionsByRequestCcyIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where requestCcy is greater than or equal to DEFAULT_REQUEST_CCY
        defaultTransactionShouldBeFound("requestCcy.greaterThanOrEqual=" + DEFAULT_REQUEST_CCY);

        // Get all the transactionList where requestCcy is greater than or equal to UPDATED_REQUEST_CCY
        defaultTransactionShouldNotBeFound("requestCcy.greaterThanOrEqual=" + UPDATED_REQUEST_CCY);
    }

    @Test
    @Transactional
    void getAllTransactionsByRequestCcyIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where requestCcy is less than or equal to DEFAULT_REQUEST_CCY
        defaultTransactionShouldBeFound("requestCcy.lessThanOrEqual=" + DEFAULT_REQUEST_CCY);

        // Get all the transactionList where requestCcy is less than or equal to SMALLER_REQUEST_CCY
        defaultTransactionShouldNotBeFound("requestCcy.lessThanOrEqual=" + SMALLER_REQUEST_CCY);
    }

    @Test
    @Transactional
    void getAllTransactionsByRequestCcyIsLessThanSomething() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where requestCcy is less than DEFAULT_REQUEST_CCY
        defaultTransactionShouldNotBeFound("requestCcy.lessThan=" + DEFAULT_REQUEST_CCY);

        // Get all the transactionList where requestCcy is less than UPDATED_REQUEST_CCY
        defaultTransactionShouldBeFound("requestCcy.lessThan=" + UPDATED_REQUEST_CCY);
    }

    @Test
    @Transactional
    void getAllTransactionsByRequestCcyIsGreaterThanSomething() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where requestCcy is greater than DEFAULT_REQUEST_CCY
        defaultTransactionShouldNotBeFound("requestCcy.greaterThan=" + DEFAULT_REQUEST_CCY);

        // Get all the transactionList where requestCcy is greater than SMALLER_REQUEST_CCY
        defaultTransactionShouldBeFound("requestCcy.greaterThan=" + SMALLER_REQUEST_CCY);
    }

    @Test
    @Transactional
    void getAllTransactionsByRequestTrIdIsEqualToSomething() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where requestTrId equals to DEFAULT_REQUEST_TR_ID
        defaultTransactionShouldBeFound("requestTrId.equals=" + DEFAULT_REQUEST_TR_ID);

        // Get all the transactionList where requestTrId equals to UPDATED_REQUEST_TR_ID
        defaultTransactionShouldNotBeFound("requestTrId.equals=" + UPDATED_REQUEST_TR_ID);
    }

    @Test
    @Transactional
    void getAllTransactionsByRequestTrIdIsNotEqualToSomething() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where requestTrId not equals to DEFAULT_REQUEST_TR_ID
        defaultTransactionShouldNotBeFound("requestTrId.notEquals=" + DEFAULT_REQUEST_TR_ID);

        // Get all the transactionList where requestTrId not equals to UPDATED_REQUEST_TR_ID
        defaultTransactionShouldBeFound("requestTrId.notEquals=" + UPDATED_REQUEST_TR_ID);
    }

    @Test
    @Transactional
    void getAllTransactionsByRequestTrIdIsInShouldWork() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where requestTrId in DEFAULT_REQUEST_TR_ID or UPDATED_REQUEST_TR_ID
        defaultTransactionShouldBeFound("requestTrId.in=" + DEFAULT_REQUEST_TR_ID + "," + UPDATED_REQUEST_TR_ID);

        // Get all the transactionList where requestTrId equals to UPDATED_REQUEST_TR_ID
        defaultTransactionShouldNotBeFound("requestTrId.in=" + UPDATED_REQUEST_TR_ID);
    }

    @Test
    @Transactional
    void getAllTransactionsByRequestTrIdIsNullOrNotNull() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where requestTrId is not null
        defaultTransactionShouldBeFound("requestTrId.specified=true");

        // Get all the transactionList where requestTrId is null
        defaultTransactionShouldNotBeFound("requestTrId.specified=false");
    }

    @Test
    @Transactional
    void getAllTransactionsByRequestTrIdContainsSomething() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where requestTrId contains DEFAULT_REQUEST_TR_ID
        defaultTransactionShouldBeFound("requestTrId.contains=" + DEFAULT_REQUEST_TR_ID);

        // Get all the transactionList where requestTrId contains UPDATED_REQUEST_TR_ID
        defaultTransactionShouldNotBeFound("requestTrId.contains=" + UPDATED_REQUEST_TR_ID);
    }

    @Test
    @Transactional
    void getAllTransactionsByRequestTrIdNotContainsSomething() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where requestTrId does not contain DEFAULT_REQUEST_TR_ID
        defaultTransactionShouldNotBeFound("requestTrId.doesNotContain=" + DEFAULT_REQUEST_TR_ID);

        // Get all the transactionList where requestTrId does not contain UPDATED_REQUEST_TR_ID
        defaultTransactionShouldBeFound("requestTrId.doesNotContain=" + UPDATED_REQUEST_TR_ID);
    }

    @Test
    @Transactional
    void getAllTransactionsByRequestLoginIsEqualToSomething() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where requestLogin equals to DEFAULT_REQUEST_LOGIN
        defaultTransactionShouldBeFound("requestLogin.equals=" + DEFAULT_REQUEST_LOGIN);

        // Get all the transactionList where requestLogin equals to UPDATED_REQUEST_LOGIN
        defaultTransactionShouldNotBeFound("requestLogin.equals=" + UPDATED_REQUEST_LOGIN);
    }

    @Test
    @Transactional
    void getAllTransactionsByRequestLoginIsNotEqualToSomething() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where requestLogin not equals to DEFAULT_REQUEST_LOGIN
        defaultTransactionShouldNotBeFound("requestLogin.notEquals=" + DEFAULT_REQUEST_LOGIN);

        // Get all the transactionList where requestLogin not equals to UPDATED_REQUEST_LOGIN
        defaultTransactionShouldBeFound("requestLogin.notEquals=" + UPDATED_REQUEST_LOGIN);
    }

    @Test
    @Transactional
    void getAllTransactionsByRequestLoginIsInShouldWork() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where requestLogin in DEFAULT_REQUEST_LOGIN or UPDATED_REQUEST_LOGIN
        defaultTransactionShouldBeFound("requestLogin.in=" + DEFAULT_REQUEST_LOGIN + "," + UPDATED_REQUEST_LOGIN);

        // Get all the transactionList where requestLogin equals to UPDATED_REQUEST_LOGIN
        defaultTransactionShouldNotBeFound("requestLogin.in=" + UPDATED_REQUEST_LOGIN);
    }

    @Test
    @Transactional
    void getAllTransactionsByRequestLoginIsNullOrNotNull() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where requestLogin is not null
        defaultTransactionShouldBeFound("requestLogin.specified=true");

        // Get all the transactionList where requestLogin is null
        defaultTransactionShouldNotBeFound("requestLogin.specified=false");
    }

    @Test
    @Transactional
    void getAllTransactionsByRequestLoginContainsSomething() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where requestLogin contains DEFAULT_REQUEST_LOGIN
        defaultTransactionShouldBeFound("requestLogin.contains=" + DEFAULT_REQUEST_LOGIN);

        // Get all the transactionList where requestLogin contains UPDATED_REQUEST_LOGIN
        defaultTransactionShouldNotBeFound("requestLogin.contains=" + UPDATED_REQUEST_LOGIN);
    }

    @Test
    @Transactional
    void getAllTransactionsByRequestLoginNotContainsSomething() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where requestLogin does not contain DEFAULT_REQUEST_LOGIN
        defaultTransactionShouldNotBeFound("requestLogin.doesNotContain=" + DEFAULT_REQUEST_LOGIN);

        // Get all the transactionList where requestLogin does not contain UPDATED_REQUEST_LOGIN
        defaultTransactionShouldBeFound("requestLogin.doesNotContain=" + UPDATED_REQUEST_LOGIN);
    }

    @Test
    @Transactional
    void getAllTransactionsByRequestGroupIsEqualToSomething() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where requestGroup equals to DEFAULT_REQUEST_GROUP
        defaultTransactionShouldBeFound("requestGroup.equals=" + DEFAULT_REQUEST_GROUP);

        // Get all the transactionList where requestGroup equals to UPDATED_REQUEST_GROUP
        defaultTransactionShouldNotBeFound("requestGroup.equals=" + UPDATED_REQUEST_GROUP);
    }

    @Test
    @Transactional
    void getAllTransactionsByRequestGroupIsNotEqualToSomething() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where requestGroup not equals to DEFAULT_REQUEST_GROUP
        defaultTransactionShouldNotBeFound("requestGroup.notEquals=" + DEFAULT_REQUEST_GROUP);

        // Get all the transactionList where requestGroup not equals to UPDATED_REQUEST_GROUP
        defaultTransactionShouldBeFound("requestGroup.notEquals=" + UPDATED_REQUEST_GROUP);
    }

    @Test
    @Transactional
    void getAllTransactionsByRequestGroupIsInShouldWork() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where requestGroup in DEFAULT_REQUEST_GROUP or UPDATED_REQUEST_GROUP
        defaultTransactionShouldBeFound("requestGroup.in=" + DEFAULT_REQUEST_GROUP + "," + UPDATED_REQUEST_GROUP);

        // Get all the transactionList where requestGroup equals to UPDATED_REQUEST_GROUP
        defaultTransactionShouldNotBeFound("requestGroup.in=" + UPDATED_REQUEST_GROUP);
    }

    @Test
    @Transactional
    void getAllTransactionsByRequestGroupIsNullOrNotNull() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where requestGroup is not null
        defaultTransactionShouldBeFound("requestGroup.specified=true");

        // Get all the transactionList where requestGroup is null
        defaultTransactionShouldNotBeFound("requestGroup.specified=false");
    }

    @Test
    @Transactional
    void getAllTransactionsByRequestGroupContainsSomething() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where requestGroup contains DEFAULT_REQUEST_GROUP
        defaultTransactionShouldBeFound("requestGroup.contains=" + DEFAULT_REQUEST_GROUP);

        // Get all the transactionList where requestGroup contains UPDATED_REQUEST_GROUP
        defaultTransactionShouldNotBeFound("requestGroup.contains=" + UPDATED_REQUEST_GROUP);
    }

    @Test
    @Transactional
    void getAllTransactionsByRequestGroupNotContainsSomething() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where requestGroup does not contain DEFAULT_REQUEST_GROUP
        defaultTransactionShouldNotBeFound("requestGroup.doesNotContain=" + DEFAULT_REQUEST_GROUP);

        // Get all the transactionList where requestGroup does not contain UPDATED_REQUEST_GROUP
        defaultTransactionShouldBeFound("requestGroup.doesNotContain=" + UPDATED_REQUEST_GROUP);
    }

    @Test
    @Transactional
    void getAllTransactionsBySenderNameIsEqualToSomething() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where senderName equals to DEFAULT_SENDER_NAME
        defaultTransactionShouldBeFound("senderName.equals=" + DEFAULT_SENDER_NAME);

        // Get all the transactionList where senderName equals to UPDATED_SENDER_NAME
        defaultTransactionShouldNotBeFound("senderName.equals=" + UPDATED_SENDER_NAME);
    }

    @Test
    @Transactional
    void getAllTransactionsBySenderNameIsNotEqualToSomething() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where senderName not equals to DEFAULT_SENDER_NAME
        defaultTransactionShouldNotBeFound("senderName.notEquals=" + DEFAULT_SENDER_NAME);

        // Get all the transactionList where senderName not equals to UPDATED_SENDER_NAME
        defaultTransactionShouldBeFound("senderName.notEquals=" + UPDATED_SENDER_NAME);
    }

    @Test
    @Transactional
    void getAllTransactionsBySenderNameIsInShouldWork() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where senderName in DEFAULT_SENDER_NAME or UPDATED_SENDER_NAME
        defaultTransactionShouldBeFound("senderName.in=" + DEFAULT_SENDER_NAME + "," + UPDATED_SENDER_NAME);

        // Get all the transactionList where senderName equals to UPDATED_SENDER_NAME
        defaultTransactionShouldNotBeFound("senderName.in=" + UPDATED_SENDER_NAME);
    }

    @Test
    @Transactional
    void getAllTransactionsBySenderNameIsNullOrNotNull() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where senderName is not null
        defaultTransactionShouldBeFound("senderName.specified=true");

        // Get all the transactionList where senderName is null
        defaultTransactionShouldNotBeFound("senderName.specified=false");
    }

    @Test
    @Transactional
    void getAllTransactionsBySenderNameContainsSomething() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where senderName contains DEFAULT_SENDER_NAME
        defaultTransactionShouldBeFound("senderName.contains=" + DEFAULT_SENDER_NAME);

        // Get all the transactionList where senderName contains UPDATED_SENDER_NAME
        defaultTransactionShouldNotBeFound("senderName.contains=" + UPDATED_SENDER_NAME);
    }

    @Test
    @Transactional
    void getAllTransactionsBySenderNameNotContainsSomething() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where senderName does not contain DEFAULT_SENDER_NAME
        defaultTransactionShouldNotBeFound("senderName.doesNotContain=" + DEFAULT_SENDER_NAME);

        // Get all the transactionList where senderName does not contain UPDATED_SENDER_NAME
        defaultTransactionShouldBeFound("senderName.doesNotContain=" + UPDATED_SENDER_NAME);
    }

    @Test
    @Transactional
    void getAllTransactionsBySenderAccountIsEqualToSomething() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where senderAccount equals to DEFAULT_SENDER_ACCOUNT
        defaultTransactionShouldBeFound("senderAccount.equals=" + DEFAULT_SENDER_ACCOUNT);

        // Get all the transactionList where senderAccount equals to UPDATED_SENDER_ACCOUNT
        defaultTransactionShouldNotBeFound("senderAccount.equals=" + UPDATED_SENDER_ACCOUNT);
    }

    @Test
    @Transactional
    void getAllTransactionsBySenderAccountIsNotEqualToSomething() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where senderAccount not equals to DEFAULT_SENDER_ACCOUNT
        defaultTransactionShouldNotBeFound("senderAccount.notEquals=" + DEFAULT_SENDER_ACCOUNT);

        // Get all the transactionList where senderAccount not equals to UPDATED_SENDER_ACCOUNT
        defaultTransactionShouldBeFound("senderAccount.notEquals=" + UPDATED_SENDER_ACCOUNT);
    }

    @Test
    @Transactional
    void getAllTransactionsBySenderAccountIsInShouldWork() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where senderAccount in DEFAULT_SENDER_ACCOUNT or UPDATED_SENDER_ACCOUNT
        defaultTransactionShouldBeFound("senderAccount.in=" + DEFAULT_SENDER_ACCOUNT + "," + UPDATED_SENDER_ACCOUNT);

        // Get all the transactionList where senderAccount equals to UPDATED_SENDER_ACCOUNT
        defaultTransactionShouldNotBeFound("senderAccount.in=" + UPDATED_SENDER_ACCOUNT);
    }

    @Test
    @Transactional
    void getAllTransactionsBySenderAccountIsNullOrNotNull() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where senderAccount is not null
        defaultTransactionShouldBeFound("senderAccount.specified=true");

        // Get all the transactionList where senderAccount is null
        defaultTransactionShouldNotBeFound("senderAccount.specified=false");
    }

    @Test
    @Transactional
    void getAllTransactionsBySenderAccountContainsSomething() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where senderAccount contains DEFAULT_SENDER_ACCOUNT
        defaultTransactionShouldBeFound("senderAccount.contains=" + DEFAULT_SENDER_ACCOUNT);

        // Get all the transactionList where senderAccount contains UPDATED_SENDER_ACCOUNT
        defaultTransactionShouldNotBeFound("senderAccount.contains=" + UPDATED_SENDER_ACCOUNT);
    }

    @Test
    @Transactional
    void getAllTransactionsBySenderAccountNotContainsSomething() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where senderAccount does not contain DEFAULT_SENDER_ACCOUNT
        defaultTransactionShouldNotBeFound("senderAccount.doesNotContain=" + DEFAULT_SENDER_ACCOUNT);

        // Get all the transactionList where senderAccount does not contain UPDATED_SENDER_ACCOUNT
        defaultTransactionShouldBeFound("senderAccount.doesNotContain=" + UPDATED_SENDER_ACCOUNT);
    }

    @Test
    @Transactional
    void getAllTransactionsBySenderServiceIdIsEqualToSomething() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where senderServiceId equals to DEFAULT_SENDER_SERVICE_ID
        defaultTransactionShouldBeFound("senderServiceId.equals=" + DEFAULT_SENDER_SERVICE_ID);

        // Get all the transactionList where senderServiceId equals to UPDATED_SENDER_SERVICE_ID
        defaultTransactionShouldNotBeFound("senderServiceId.equals=" + UPDATED_SENDER_SERVICE_ID);
    }

    @Test
    @Transactional
    void getAllTransactionsBySenderServiceIdIsNotEqualToSomething() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where senderServiceId not equals to DEFAULT_SENDER_SERVICE_ID
        defaultTransactionShouldNotBeFound("senderServiceId.notEquals=" + DEFAULT_SENDER_SERVICE_ID);

        // Get all the transactionList where senderServiceId not equals to UPDATED_SENDER_SERVICE_ID
        defaultTransactionShouldBeFound("senderServiceId.notEquals=" + UPDATED_SENDER_SERVICE_ID);
    }

    @Test
    @Transactional
    void getAllTransactionsBySenderServiceIdIsInShouldWork() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where senderServiceId in DEFAULT_SENDER_SERVICE_ID or UPDATED_SENDER_SERVICE_ID
        defaultTransactionShouldBeFound("senderServiceId.in=" + DEFAULT_SENDER_SERVICE_ID + "," + UPDATED_SENDER_SERVICE_ID);

        // Get all the transactionList where senderServiceId equals to UPDATED_SENDER_SERVICE_ID
        defaultTransactionShouldNotBeFound("senderServiceId.in=" + UPDATED_SENDER_SERVICE_ID);
    }

    @Test
    @Transactional
    void getAllTransactionsBySenderServiceIdIsNullOrNotNull() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where senderServiceId is not null
        defaultTransactionShouldBeFound("senderServiceId.specified=true");

        // Get all the transactionList where senderServiceId is null
        defaultTransactionShouldNotBeFound("senderServiceId.specified=false");
    }

    @Test
    @Transactional
    void getAllTransactionsBySenderServiceIdIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where senderServiceId is greater than or equal to DEFAULT_SENDER_SERVICE_ID
        defaultTransactionShouldBeFound("senderServiceId.greaterThanOrEqual=" + DEFAULT_SENDER_SERVICE_ID);

        // Get all the transactionList where senderServiceId is greater than or equal to UPDATED_SENDER_SERVICE_ID
        defaultTransactionShouldNotBeFound("senderServiceId.greaterThanOrEqual=" + UPDATED_SENDER_SERVICE_ID);
    }

//    @Test
//    @Transactional
//    void getAllTransactionsBySenderServiceIdIsLessThanOrEqualToSomething() throws Exception {
//        // Initialize the database
//        transactionRepository.saveAndFlush(transaction);
//
//        // Get all the transactionList where senderServiceId is less than or equal to DEFAULT_SENDER_SERVICE_ID
//        defaultTransactionShouldBeFound("senderServiceId.lessThanOrEqual=" + DEFAULT_SENDER_SERVICE_ID);
//
//        // Get all the transactionList where senderServiceId is less than or equal to SMALLER_SENDER_SERVICE_ID
//        defaultTransactionShouldNotBeFound("senderServiceId.lessThanOrEqual=" + SMALLER_SENDER_SERVICE_ID);
//    }

    @Test
    @Transactional
    void getAllTransactionsBySenderServiceIdIsLessThanSomething() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where senderServiceId is less than DEFAULT_SENDER_SERVICE_ID
        defaultTransactionShouldNotBeFound("senderServiceId.lessThan=" + DEFAULT_SENDER_SERVICE_ID);

        // Get all the transactionList where senderServiceId is less than UPDATED_SENDER_SERVICE_ID
        defaultTransactionShouldBeFound("senderServiceId.lessThan=" + UPDATED_SENDER_SERVICE_ID);
    }

//    @Test
//    @Transactional
//    void getAllTransactionsBySenderServiceIdIsGreaterThanSomething() throws Exception {
//        // Initialize the database
//        transactionRepository.saveAndFlush(transaction);
//
//        // Get all the transactionList where senderServiceId is greater than DEFAULT_SENDER_SERVICE_ID
//        defaultTransactionShouldNotBeFound("senderServiceId.greaterThan=" + DEFAULT_SENDER_SERVICE_ID);
//
//        // Get all the transactionList where senderServiceId is greater than SMALLER_SENDER_SERVICE_ID
//        defaultTransactionShouldBeFound("senderServiceId.greaterThan=" + SMALLER_SENDER_SERVICE_ID);
//    }

    @Test
    @Transactional
    void getAllTransactionsBySenderAmountIsEqualToSomething() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where senderAmount equals to DEFAULT_SENDER_AMOUNT
        defaultTransactionShouldBeFound("senderAmount.equals=" + DEFAULT_SENDER_AMOUNT);

        // Get all the transactionList where senderAmount equals to UPDATED_SENDER_AMOUNT
        defaultTransactionShouldNotBeFound("senderAmount.equals=" + UPDATED_SENDER_AMOUNT);
    }

    @Test
    @Transactional
    void getAllTransactionsBySenderAmountIsNotEqualToSomething() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where senderAmount not equals to DEFAULT_SENDER_AMOUNT
        defaultTransactionShouldNotBeFound("senderAmount.notEquals=" + DEFAULT_SENDER_AMOUNT);

        // Get all the transactionList where senderAmount not equals to UPDATED_SENDER_AMOUNT
        defaultTransactionShouldBeFound("senderAmount.notEquals=" + UPDATED_SENDER_AMOUNT);
    }

    @Test
    @Transactional
    void getAllTransactionsBySenderAmountIsInShouldWork() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where senderAmount in DEFAULT_SENDER_AMOUNT or UPDATED_SENDER_AMOUNT
        defaultTransactionShouldBeFound("senderAmount.in=" + DEFAULT_SENDER_AMOUNT + "," + UPDATED_SENDER_AMOUNT);

        // Get all the transactionList where senderAmount equals to UPDATED_SENDER_AMOUNT
        defaultTransactionShouldNotBeFound("senderAmount.in=" + UPDATED_SENDER_AMOUNT);
    }

    @Test
    @Transactional
    void getAllTransactionsBySenderAmountIsNullOrNotNull() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where senderAmount is not null
        defaultTransactionShouldBeFound("senderAmount.specified=true");

        // Get all the transactionList where senderAmount is null
        defaultTransactionShouldNotBeFound("senderAmount.specified=false");
    }

    @Test
    @Transactional
    void getAllTransactionsBySenderAmountIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where senderAmount is greater than or equal to DEFAULT_SENDER_AMOUNT
        defaultTransactionShouldBeFound("senderAmount.greaterThanOrEqual=" + DEFAULT_SENDER_AMOUNT);

        // Get all the transactionList where senderAmount is greater than or equal to UPDATED_SENDER_AMOUNT
        defaultTransactionShouldNotBeFound("senderAmount.greaterThanOrEqual=" + UPDATED_SENDER_AMOUNT);
    }

    @Test
    @Transactional
    void getAllTransactionsBySenderAmountIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where senderAmount is less than or equal to DEFAULT_SENDER_AMOUNT
        defaultTransactionShouldBeFound("senderAmount.lessThanOrEqual=" + DEFAULT_SENDER_AMOUNT);

        // Get all the transactionList where senderAmount is less than or equal to SMALLER_SENDER_AMOUNT
        defaultTransactionShouldNotBeFound("senderAmount.lessThanOrEqual=" + SMALLER_SENDER_AMOUNT);
    }

    @Test
    @Transactional
    void getAllTransactionsBySenderAmountIsLessThanSomething() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where senderAmount is less than DEFAULT_SENDER_AMOUNT
        defaultTransactionShouldNotBeFound("senderAmount.lessThan=" + DEFAULT_SENDER_AMOUNT);

        // Get all the transactionList where senderAmount is less than UPDATED_SENDER_AMOUNT
        defaultTransactionShouldBeFound("senderAmount.lessThan=" + UPDATED_SENDER_AMOUNT);
    }

    @Test
    @Transactional
    void getAllTransactionsBySenderAmountIsGreaterThanSomething() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where senderAmount is greater than DEFAULT_SENDER_AMOUNT
        defaultTransactionShouldNotBeFound("senderAmount.greaterThan=" + DEFAULT_SENDER_AMOUNT);

        // Get all the transactionList where senderAmount is greater than SMALLER_SENDER_AMOUNT
        defaultTransactionShouldBeFound("senderAmount.greaterThan=" + SMALLER_SENDER_AMOUNT);
    }

    @Test
    @Transactional
    void getAllTransactionsBySenderCcyIsEqualToSomething() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where senderCcy equals to DEFAULT_SENDER_CCY
        defaultTransactionShouldBeFound("senderCcy.equals=" + DEFAULT_SENDER_CCY);

        // Get all the transactionList where senderCcy equals to UPDATED_SENDER_CCY
        defaultTransactionShouldNotBeFound("senderCcy.equals=" + UPDATED_SENDER_CCY);
    }

    @Test
    @Transactional
    void getAllTransactionsBySenderCcyIsNotEqualToSomething() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where senderCcy not equals to DEFAULT_SENDER_CCY
        defaultTransactionShouldNotBeFound("senderCcy.notEquals=" + DEFAULT_SENDER_CCY);

        // Get all the transactionList where senderCcy not equals to UPDATED_SENDER_CCY
        defaultTransactionShouldBeFound("senderCcy.notEquals=" + UPDATED_SENDER_CCY);
    }

    @Test
    @Transactional
    void getAllTransactionsBySenderCcyIsInShouldWork() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where senderCcy in DEFAULT_SENDER_CCY or UPDATED_SENDER_CCY
        defaultTransactionShouldBeFound("senderCcy.in=" + DEFAULT_SENDER_CCY + "," + UPDATED_SENDER_CCY);

        // Get all the transactionList where senderCcy equals to UPDATED_SENDER_CCY
        defaultTransactionShouldNotBeFound("senderCcy.in=" + UPDATED_SENDER_CCY);
    }

    @Test
    @Transactional
    void getAllTransactionsBySenderCcyIsNullOrNotNull() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where senderCcy is not null
        defaultTransactionShouldBeFound("senderCcy.specified=true");

        // Get all the transactionList where senderCcy is null
        defaultTransactionShouldNotBeFound("senderCcy.specified=false");
    }

    @Test
    @Transactional
    void getAllTransactionsBySenderCcyIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where senderCcy is greater than or equal to DEFAULT_SENDER_CCY
        defaultTransactionShouldBeFound("senderCcy.greaterThanOrEqual=" + DEFAULT_SENDER_CCY);

        // Get all the transactionList where senderCcy is greater than or equal to UPDATED_SENDER_CCY
        defaultTransactionShouldNotBeFound("senderCcy.greaterThanOrEqual=" + UPDATED_SENDER_CCY);
    }

    @Test
    @Transactional
    void getAllTransactionsBySenderCcyIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where senderCcy is less than or equal to DEFAULT_SENDER_CCY
        defaultTransactionShouldBeFound("senderCcy.lessThanOrEqual=" + DEFAULT_SENDER_CCY);

        // Get all the transactionList where senderCcy is less than or equal to SMALLER_SENDER_CCY
        defaultTransactionShouldNotBeFound("senderCcy.lessThanOrEqual=" + SMALLER_SENDER_CCY);
    }

    @Test
    @Transactional
    void getAllTransactionsBySenderCcyIsLessThanSomething() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where senderCcy is less than DEFAULT_SENDER_CCY
        defaultTransactionShouldNotBeFound("senderCcy.lessThan=" + DEFAULT_SENDER_CCY);

        // Get all the transactionList where senderCcy is less than UPDATED_SENDER_CCY
        defaultTransactionShouldBeFound("senderCcy.lessThan=" + UPDATED_SENDER_CCY);
    }

    @Test
    @Transactional
    void getAllTransactionsBySenderCcyIsGreaterThanSomething() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where senderCcy is greater than DEFAULT_SENDER_CCY
        defaultTransactionShouldNotBeFound("senderCcy.greaterThan=" + DEFAULT_SENDER_CCY);

        // Get all the transactionList where senderCcy is greater than SMALLER_SENDER_CCY
        defaultTransactionShouldBeFound("senderCcy.greaterThan=" + SMALLER_SENDER_CCY);
    }

    @Test
    @Transactional
    void getAllTransactionsBySenderTrIdIsEqualToSomething() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where senderTrId equals to DEFAULT_SENDER_TR_ID
        defaultTransactionShouldBeFound("senderTrId.equals=" + DEFAULT_SENDER_TR_ID);

        // Get all the transactionList where senderTrId equals to UPDATED_SENDER_TR_ID
        defaultTransactionShouldNotBeFound("senderTrId.equals=" + UPDATED_SENDER_TR_ID);
    }

    @Test
    @Transactional
    void getAllTransactionsBySenderTrIdIsNotEqualToSomething() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where senderTrId not equals to DEFAULT_SENDER_TR_ID
        defaultTransactionShouldNotBeFound("senderTrId.notEquals=" + DEFAULT_SENDER_TR_ID);

        // Get all the transactionList where senderTrId not equals to UPDATED_SENDER_TR_ID
        defaultTransactionShouldBeFound("senderTrId.notEquals=" + UPDATED_SENDER_TR_ID);
    }

    @Test
    @Transactional
    void getAllTransactionsBySenderTrIdIsInShouldWork() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where senderTrId in DEFAULT_SENDER_TR_ID or UPDATED_SENDER_TR_ID
        defaultTransactionShouldBeFound("senderTrId.in=" + DEFAULT_SENDER_TR_ID + "," + UPDATED_SENDER_TR_ID);

        // Get all the transactionList where senderTrId equals to UPDATED_SENDER_TR_ID
        defaultTransactionShouldNotBeFound("senderTrId.in=" + UPDATED_SENDER_TR_ID);
    }

    @Test
    @Transactional
    void getAllTransactionsBySenderTrIdIsNullOrNotNull() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where senderTrId is not null
        defaultTransactionShouldBeFound("senderTrId.specified=true");

        // Get all the transactionList where senderTrId is null
        defaultTransactionShouldNotBeFound("senderTrId.specified=false");
    }

    @Test
    @Transactional
    void getAllTransactionsBySenderTrIdContainsSomething() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where senderTrId contains DEFAULT_SENDER_TR_ID
        defaultTransactionShouldBeFound("senderTrId.contains=" + DEFAULT_SENDER_TR_ID);

        // Get all the transactionList where senderTrId contains UPDATED_SENDER_TR_ID
        defaultTransactionShouldNotBeFound("senderTrId.contains=" + UPDATED_SENDER_TR_ID);
    }

    @Test
    @Transactional
    void getAllTransactionsBySenderTrIdNotContainsSomething() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where senderTrId does not contain DEFAULT_SENDER_TR_ID
        defaultTransactionShouldNotBeFound("senderTrId.doesNotContain=" + DEFAULT_SENDER_TR_ID);

        // Get all the transactionList where senderTrId does not contain UPDATED_SENDER_TR_ID
        defaultTransactionShouldBeFound("senderTrId.doesNotContain=" + UPDATED_SENDER_TR_ID);
    }

    @Test
    @Transactional
    void getAllTransactionsBySenderSuccessIsEqualToSomething() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where senderSuccess equals to DEFAULT_SENDER_SUCCESS
        defaultTransactionShouldBeFound("senderSuccess.equals=" + DEFAULT_SENDER_SUCCESS);

        // Get all the transactionList where senderSuccess equals to UPDATED_SENDER_SUCCESS
        defaultTransactionShouldNotBeFound("senderSuccess.equals=" + UPDATED_SENDER_SUCCESS);
    }

    @Test
    @Transactional
    void getAllTransactionsBySenderSuccessIsNotEqualToSomething() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where senderSuccess not equals to DEFAULT_SENDER_SUCCESS
        defaultTransactionShouldNotBeFound("senderSuccess.notEquals=" + DEFAULT_SENDER_SUCCESS);

        // Get all the transactionList where senderSuccess not equals to UPDATED_SENDER_SUCCESS
        defaultTransactionShouldBeFound("senderSuccess.notEquals=" + UPDATED_SENDER_SUCCESS);
    }

    @Test
    @Transactional
    void getAllTransactionsBySenderSuccessIsInShouldWork() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where senderSuccess in DEFAULT_SENDER_SUCCESS or UPDATED_SENDER_SUCCESS
        defaultTransactionShouldBeFound("senderSuccess.in=" + DEFAULT_SENDER_SUCCESS + "," + UPDATED_SENDER_SUCCESS);

        // Get all the transactionList where senderSuccess equals to UPDATED_SENDER_SUCCESS
        defaultTransactionShouldNotBeFound("senderSuccess.in=" + UPDATED_SENDER_SUCCESS);
    }

    @Test
    @Transactional
    void getAllTransactionsBySenderSuccessIsNullOrNotNull() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where senderSuccess is not null
        defaultTransactionShouldBeFound("senderSuccess.specified=true");

        // Get all the transactionList where senderSuccess is null
        defaultTransactionShouldNotBeFound("senderSuccess.specified=false");
    }

    @Test
    @Transactional
    void getAllTransactionsBySenderDateIsEqualToSomething() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where senderDate equals to DEFAULT_SENDER_DATE
        defaultTransactionShouldBeFound("senderDate.equals=" + DEFAULT_SENDER_DATE);

        // Get all the transactionList where senderDate equals to UPDATED_SENDER_DATE
        defaultTransactionShouldNotBeFound("senderDate.equals=" + UPDATED_SENDER_DATE);
    }

    @Test
    @Transactional
    void getAllTransactionsBySenderDateIsNotEqualToSomething() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where senderDate not equals to DEFAULT_SENDER_DATE
        defaultTransactionShouldNotBeFound("senderDate.notEquals=" + DEFAULT_SENDER_DATE);

        // Get all the transactionList where senderDate not equals to UPDATED_SENDER_DATE
        defaultTransactionShouldBeFound("senderDate.notEquals=" + UPDATED_SENDER_DATE);
    }

    @Test
    @Transactional
    void getAllTransactionsBySenderDateIsInShouldWork() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where senderDate in DEFAULT_SENDER_DATE or UPDATED_SENDER_DATE
        defaultTransactionShouldBeFound("senderDate.in=" + DEFAULT_SENDER_DATE + "," + UPDATED_SENDER_DATE);

        // Get all the transactionList where senderDate equals to UPDATED_SENDER_DATE
        defaultTransactionShouldNotBeFound("senderDate.in=" + UPDATED_SENDER_DATE);
    }

    @Test
    @Transactional
    void getAllTransactionsBySenderDateIsNullOrNotNull() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where senderDate is not null
        defaultTransactionShouldBeFound("senderDate.specified=true");

        // Get all the transactionList where senderDate is null
        defaultTransactionShouldNotBeFound("senderDate.specified=false");
    }

    @Test
    @Transactional
    void getAllTransactionsBySenderStatusIsEqualToSomething() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where senderStatus equals to DEFAULT_SENDER_STATUS
        defaultTransactionShouldBeFound("senderStatus.equals=" + DEFAULT_SENDER_STATUS);

        // Get all the transactionList where senderStatus equals to UPDATED_SENDER_STATUS
        defaultTransactionShouldNotBeFound("senderStatus.equals=" + UPDATED_SENDER_STATUS);
    }

    @Test
    @Transactional
    void getAllTransactionsBySenderStatusIsNotEqualToSomething() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where senderStatus not equals to DEFAULT_SENDER_STATUS
        defaultTransactionShouldNotBeFound("senderStatus.notEquals=" + DEFAULT_SENDER_STATUS);

        // Get all the transactionList where senderStatus not equals to UPDATED_SENDER_STATUS
        defaultTransactionShouldBeFound("senderStatus.notEquals=" + UPDATED_SENDER_STATUS);
    }

    @Test
    @Transactional
    void getAllTransactionsBySenderStatusIsInShouldWork() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where senderStatus in DEFAULT_SENDER_STATUS or UPDATED_SENDER_STATUS
        defaultTransactionShouldBeFound("senderStatus.in=" + DEFAULT_SENDER_STATUS + "," + UPDATED_SENDER_STATUS);

        // Get all the transactionList where senderStatus equals to UPDATED_SENDER_STATUS
        defaultTransactionShouldNotBeFound("senderStatus.in=" + UPDATED_SENDER_STATUS);
    }

    @Test
    @Transactional
    void getAllTransactionsBySenderStatusIsNullOrNotNull() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where senderStatus is not null
        defaultTransactionShouldBeFound("senderStatus.specified=true");

        // Get all the transactionList where senderStatus is null
        defaultTransactionShouldNotBeFound("senderStatus.specified=false");
    }

    @Test
    @Transactional
    void getAllTransactionsBySenderStatusContainsSomething() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where senderStatus contains DEFAULT_SENDER_STATUS
        defaultTransactionShouldBeFound("senderStatus.contains=" + DEFAULT_SENDER_STATUS);

        // Get all the transactionList where senderStatus contains UPDATED_SENDER_STATUS
        defaultTransactionShouldNotBeFound("senderStatus.contains=" + UPDATED_SENDER_STATUS);
    }

    @Test
    @Transactional
    void getAllTransactionsBySenderStatusNotContainsSomething() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where senderStatus does not contain DEFAULT_SENDER_STATUS
        defaultTransactionShouldNotBeFound("senderStatus.doesNotContain=" + DEFAULT_SENDER_STATUS);

        // Get all the transactionList where senderStatus does not contain UPDATED_SENDER_STATUS
        defaultTransactionShouldBeFound("senderStatus.doesNotContain=" + UPDATED_SENDER_STATUS);
    }

    @Test
    @Transactional
    void getAllTransactionsBySenderStatusTextIsEqualToSomething() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where senderStatusText equals to DEFAULT_SENDER_STATUS_TEXT
        defaultTransactionShouldBeFound("senderStatusText.equals=" + DEFAULT_SENDER_STATUS_TEXT);

        // Get all the transactionList where senderStatusText equals to UPDATED_SENDER_STATUS_TEXT
        defaultTransactionShouldNotBeFound("senderStatusText.equals=" + UPDATED_SENDER_STATUS_TEXT);
    }

    @Test
    @Transactional
    void getAllTransactionsBySenderStatusTextIsNotEqualToSomething() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where senderStatusText not equals to DEFAULT_SENDER_STATUS_TEXT
        defaultTransactionShouldNotBeFound("senderStatusText.notEquals=" + DEFAULT_SENDER_STATUS_TEXT);

        // Get all the transactionList where senderStatusText not equals to UPDATED_SENDER_STATUS_TEXT
        defaultTransactionShouldBeFound("senderStatusText.notEquals=" + UPDATED_SENDER_STATUS_TEXT);
    }

    @Test
    @Transactional
    void getAllTransactionsBySenderStatusTextIsInShouldWork() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where senderStatusText in DEFAULT_SENDER_STATUS_TEXT or UPDATED_SENDER_STATUS_TEXT
        defaultTransactionShouldBeFound("senderStatusText.in=" + DEFAULT_SENDER_STATUS_TEXT + "," + UPDATED_SENDER_STATUS_TEXT);

        // Get all the transactionList where senderStatusText equals to UPDATED_SENDER_STATUS_TEXT
        defaultTransactionShouldNotBeFound("senderStatusText.in=" + UPDATED_SENDER_STATUS_TEXT);
    }

    @Test
    @Transactional
    void getAllTransactionsBySenderStatusTextIsNullOrNotNull() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where senderStatusText is not null
        defaultTransactionShouldBeFound("senderStatusText.specified=true");

        // Get all the transactionList where senderStatusText is null
        defaultTransactionShouldNotBeFound("senderStatusText.specified=false");
    }

    @Test
    @Transactional
    void getAllTransactionsBySenderStatusTextContainsSomething() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where senderStatusText contains DEFAULT_SENDER_STATUS_TEXT
        defaultTransactionShouldBeFound("senderStatusText.contains=" + DEFAULT_SENDER_STATUS_TEXT);

        // Get all the transactionList where senderStatusText contains UPDATED_SENDER_STATUS_TEXT
        defaultTransactionShouldNotBeFound("senderStatusText.contains=" + UPDATED_SENDER_STATUS_TEXT);
    }

    @Test
    @Transactional
    void getAllTransactionsBySenderStatusTextNotContainsSomething() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where senderStatusText does not contain DEFAULT_SENDER_STATUS_TEXT
        defaultTransactionShouldNotBeFound("senderStatusText.doesNotContain=" + DEFAULT_SENDER_STATUS_TEXT);

        // Get all the transactionList where senderStatusText does not contain UPDATED_SENDER_STATUS_TEXT
        defaultTransactionShouldBeFound("senderStatusText.doesNotContain=" + UPDATED_SENDER_STATUS_TEXT);
    }

    @Test
    @Transactional
    void getAllTransactionsBySenderBillIdIsEqualToSomething() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where senderBillId equals to DEFAULT_SENDER_BILL_ID
        defaultTransactionShouldBeFound("senderBillId.equals=" + DEFAULT_SENDER_BILL_ID);

        // Get all the transactionList where senderBillId equals to UPDATED_SENDER_BILL_ID
        defaultTransactionShouldNotBeFound("senderBillId.equals=" + UPDATED_SENDER_BILL_ID);
    }

    @Test
    @Transactional
    void getAllTransactionsBySenderBillIdIsNotEqualToSomething() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where senderBillId not equals to DEFAULT_SENDER_BILL_ID
        defaultTransactionShouldNotBeFound("senderBillId.notEquals=" + DEFAULT_SENDER_BILL_ID);

        // Get all the transactionList where senderBillId not equals to UPDATED_SENDER_BILL_ID
        defaultTransactionShouldBeFound("senderBillId.notEquals=" + UPDATED_SENDER_BILL_ID);
    }

    @Test
    @Transactional
    void getAllTransactionsBySenderBillIdIsInShouldWork() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where senderBillId in DEFAULT_SENDER_BILL_ID or UPDATED_SENDER_BILL_ID
        defaultTransactionShouldBeFound("senderBillId.in=" + DEFAULT_SENDER_BILL_ID + "," + UPDATED_SENDER_BILL_ID);

        // Get all the transactionList where senderBillId equals to UPDATED_SENDER_BILL_ID
        defaultTransactionShouldNotBeFound("senderBillId.in=" + UPDATED_SENDER_BILL_ID);
    }

    @Test
    @Transactional
    void getAllTransactionsBySenderBillIdIsNullOrNotNull() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where senderBillId is not null
        defaultTransactionShouldBeFound("senderBillId.specified=true");

        // Get all the transactionList where senderBillId is null
        defaultTransactionShouldNotBeFound("senderBillId.specified=false");
    }

    @Test
    @Transactional
    void getAllTransactionsBySenderBillIdIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where senderBillId is greater than or equal to DEFAULT_SENDER_BILL_ID
        defaultTransactionShouldBeFound("senderBillId.greaterThanOrEqual=" + DEFAULT_SENDER_BILL_ID);

        // Get all the transactionList where senderBillId is greater than or equal to UPDATED_SENDER_BILL_ID
        defaultTransactionShouldNotBeFound("senderBillId.greaterThanOrEqual=" + UPDATED_SENDER_BILL_ID);
    }

    @Test
    @Transactional
    void getAllTransactionsBySenderBillIdIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where senderBillId is less than or equal to DEFAULT_SENDER_BILL_ID
        defaultTransactionShouldBeFound("senderBillId.lessThanOrEqual=" + DEFAULT_SENDER_BILL_ID);

        // Get all the transactionList where senderBillId is less than or equal to SMALLER_SENDER_BILL_ID
        defaultTransactionShouldNotBeFound("senderBillId.lessThanOrEqual=" + SMALLER_SENDER_BILL_ID);
    }

    @Test
    @Transactional
    void getAllTransactionsBySenderBillIdIsLessThanSomething() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where senderBillId is less than DEFAULT_SENDER_BILL_ID
        defaultTransactionShouldNotBeFound("senderBillId.lessThan=" + DEFAULT_SENDER_BILL_ID);

        // Get all the transactionList where senderBillId is less than UPDATED_SENDER_BILL_ID
        defaultTransactionShouldBeFound("senderBillId.lessThan=" + UPDATED_SENDER_BILL_ID);
    }

    @Test
    @Transactional
    void getAllTransactionsBySenderBillIdIsGreaterThanSomething() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where senderBillId is greater than DEFAULT_SENDER_BILL_ID
        defaultTransactionShouldNotBeFound("senderBillId.greaterThan=" + DEFAULT_SENDER_BILL_ID);

        // Get all the transactionList where senderBillId is greater than SMALLER_SENDER_BILL_ID
        defaultTransactionShouldBeFound("senderBillId.greaterThan=" + SMALLER_SENDER_BILL_ID);
    }

    @Test
    @Transactional
    void getAllTransactionsBySenderGatewayIsEqualToSomething() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where senderGateway equals to DEFAULT_SENDER_GATEWAY
        defaultTransactionShouldBeFound("senderGateway.equals=" + DEFAULT_SENDER_GATEWAY);

        // Get all the transactionList where senderGateway equals to UPDATED_SENDER_GATEWAY
        defaultTransactionShouldNotBeFound("senderGateway.equals=" + UPDATED_SENDER_GATEWAY);
    }

    @Test
    @Transactional
    void getAllTransactionsBySenderGatewayIsNotEqualToSomething() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where senderGateway not equals to DEFAULT_SENDER_GATEWAY
        defaultTransactionShouldNotBeFound("senderGateway.notEquals=" + DEFAULT_SENDER_GATEWAY);

        // Get all the transactionList where senderGateway not equals to UPDATED_SENDER_GATEWAY
        defaultTransactionShouldBeFound("senderGateway.notEquals=" + UPDATED_SENDER_GATEWAY);
    }

    @Test
    @Transactional
    void getAllTransactionsBySenderGatewayIsInShouldWork() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where senderGateway in DEFAULT_SENDER_GATEWAY or UPDATED_SENDER_GATEWAY
        defaultTransactionShouldBeFound("senderGateway.in=" + DEFAULT_SENDER_GATEWAY + "," + UPDATED_SENDER_GATEWAY);

        // Get all the transactionList where senderGateway equals to UPDATED_SENDER_GATEWAY
        defaultTransactionShouldNotBeFound("senderGateway.in=" + UPDATED_SENDER_GATEWAY);
    }

    @Test
    @Transactional
    void getAllTransactionsBySenderGatewayIsNullOrNotNull() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where senderGateway is not null
        defaultTransactionShouldBeFound("senderGateway.specified=true");

        // Get all the transactionList where senderGateway is null
        defaultTransactionShouldNotBeFound("senderGateway.specified=false");
    }

    @Test
    @Transactional
    void getAllTransactionsBySenderGatewayContainsSomething() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where senderGateway contains DEFAULT_SENDER_GATEWAY
        defaultTransactionShouldBeFound("senderGateway.contains=" + DEFAULT_SENDER_GATEWAY);

        // Get all the transactionList where senderGateway contains UPDATED_SENDER_GATEWAY
        defaultTransactionShouldNotBeFound("senderGateway.contains=" + UPDATED_SENDER_GATEWAY);
    }

    @Test
    @Transactional
    void getAllTransactionsBySenderGatewayNotContainsSomething() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where senderGateway does not contain DEFAULT_SENDER_GATEWAY
        defaultTransactionShouldNotBeFound("senderGateway.doesNotContain=" + DEFAULT_SENDER_GATEWAY);

        // Get all the transactionList where senderGateway does not contain UPDATED_SENDER_GATEWAY
        defaultTransactionShouldBeFound("senderGateway.doesNotContain=" + UPDATED_SENDER_GATEWAY);
    }

    @Test
    @Transactional
    void getAllTransactionsByGatewayAmountIsEqualToSomething() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where gatewayAmount equals to DEFAULT_GATEWAY_AMOUNT
        defaultTransactionShouldBeFound("gatewayAmount.equals=" + DEFAULT_GATEWAY_AMOUNT);

        // Get all the transactionList where gatewayAmount equals to UPDATED_GATEWAY_AMOUNT
        defaultTransactionShouldNotBeFound("gatewayAmount.equals=" + UPDATED_GATEWAY_AMOUNT);
    }

    @Test
    @Transactional
    void getAllTransactionsByGatewayAmountIsNotEqualToSomething() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where gatewayAmount not equals to DEFAULT_GATEWAY_AMOUNT
        defaultTransactionShouldNotBeFound("gatewayAmount.notEquals=" + DEFAULT_GATEWAY_AMOUNT);

        // Get all the transactionList where gatewayAmount not equals to UPDATED_GATEWAY_AMOUNT
        defaultTransactionShouldBeFound("gatewayAmount.notEquals=" + UPDATED_GATEWAY_AMOUNT);
    }

    @Test
    @Transactional
    void getAllTransactionsByGatewayAmountIsInShouldWork() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where gatewayAmount in DEFAULT_GATEWAY_AMOUNT or UPDATED_GATEWAY_AMOUNT
        defaultTransactionShouldBeFound("gatewayAmount.in=" + DEFAULT_GATEWAY_AMOUNT + "," + UPDATED_GATEWAY_AMOUNT);

        // Get all the transactionList where gatewayAmount equals to UPDATED_GATEWAY_AMOUNT
        defaultTransactionShouldNotBeFound("gatewayAmount.in=" + UPDATED_GATEWAY_AMOUNT);
    }

    @Test
    @Transactional
    void getAllTransactionsByGatewayAmountIsNullOrNotNull() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where gatewayAmount is not null
        defaultTransactionShouldBeFound("gatewayAmount.specified=true");

        // Get all the transactionList where gatewayAmount is null
        defaultTransactionShouldNotBeFound("gatewayAmount.specified=false");
    }

    @Test
    @Transactional
    void getAllTransactionsByGatewayAmountIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where gatewayAmount is greater than or equal to DEFAULT_GATEWAY_AMOUNT
        defaultTransactionShouldBeFound("gatewayAmount.greaterThanOrEqual=" + DEFAULT_GATEWAY_AMOUNT);

        // Get all the transactionList where gatewayAmount is greater than or equal to UPDATED_GATEWAY_AMOUNT
        defaultTransactionShouldNotBeFound("gatewayAmount.greaterThanOrEqual=" + UPDATED_GATEWAY_AMOUNT);
    }

    @Test
    @Transactional
    void getAllTransactionsByGatewayAmountIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where gatewayAmount is less than or equal to DEFAULT_GATEWAY_AMOUNT
        defaultTransactionShouldBeFound("gatewayAmount.lessThanOrEqual=" + DEFAULT_GATEWAY_AMOUNT);

        // Get all the transactionList where gatewayAmount is less than or equal to SMALLER_GATEWAY_AMOUNT
        defaultTransactionShouldNotBeFound("gatewayAmount.lessThanOrEqual=" + SMALLER_GATEWAY_AMOUNT);
    }

    @Test
    @Transactional
    void getAllTransactionsByGatewayAmountIsLessThanSomething() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where gatewayAmount is less than DEFAULT_GATEWAY_AMOUNT
        defaultTransactionShouldNotBeFound("gatewayAmount.lessThan=" + DEFAULT_GATEWAY_AMOUNT);

        // Get all the transactionList where gatewayAmount is less than UPDATED_GATEWAY_AMOUNT
        defaultTransactionShouldBeFound("gatewayAmount.lessThan=" + UPDATED_GATEWAY_AMOUNT);
    }

    @Test
    @Transactional
    void getAllTransactionsByGatewayAmountIsGreaterThanSomething() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where gatewayAmount is greater than DEFAULT_GATEWAY_AMOUNT
        defaultTransactionShouldNotBeFound("gatewayAmount.greaterThan=" + DEFAULT_GATEWAY_AMOUNT);

        // Get all the transactionList where gatewayAmount is greater than SMALLER_GATEWAY_AMOUNT
        defaultTransactionShouldBeFound("gatewayAmount.greaterThan=" + SMALLER_GATEWAY_AMOUNT);
    }

    @Test
    @Transactional
    void getAllTransactionsByRecipientNameIsEqualToSomething() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where recipientName equals to DEFAULT_RECIPIENT_NAME
        defaultTransactionShouldBeFound("recipientName.equals=" + DEFAULT_RECIPIENT_NAME);

        // Get all the transactionList where recipientName equals to UPDATED_RECIPIENT_NAME
        defaultTransactionShouldNotBeFound("recipientName.equals=" + UPDATED_RECIPIENT_NAME);
    }

    @Test
    @Transactional
    void getAllTransactionsByRecipientNameIsNotEqualToSomething() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where recipientName not equals to DEFAULT_RECIPIENT_NAME
        defaultTransactionShouldNotBeFound("recipientName.notEquals=" + DEFAULT_RECIPIENT_NAME);

        // Get all the transactionList where recipientName not equals to UPDATED_RECIPIENT_NAME
        defaultTransactionShouldBeFound("recipientName.notEquals=" + UPDATED_RECIPIENT_NAME);
    }

    @Test
    @Transactional
    void getAllTransactionsByRecipientNameIsInShouldWork() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where recipientName in DEFAULT_RECIPIENT_NAME or UPDATED_RECIPIENT_NAME
        defaultTransactionShouldBeFound("recipientName.in=" + DEFAULT_RECIPIENT_NAME + "," + UPDATED_RECIPIENT_NAME);

        // Get all the transactionList where recipientName equals to UPDATED_RECIPIENT_NAME
        defaultTransactionShouldNotBeFound("recipientName.in=" + UPDATED_RECIPIENT_NAME);
    }

    @Test
    @Transactional
    void getAllTransactionsByRecipientNameIsNullOrNotNull() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where recipientName is not null
        defaultTransactionShouldBeFound("recipientName.specified=true");

        // Get all the transactionList where recipientName is null
        defaultTransactionShouldNotBeFound("recipientName.specified=false");
    }

    @Test
    @Transactional
    void getAllTransactionsByRecipientNameContainsSomething() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where recipientName contains DEFAULT_RECIPIENT_NAME
        defaultTransactionShouldBeFound("recipientName.contains=" + DEFAULT_RECIPIENT_NAME);

        // Get all the transactionList where recipientName contains UPDATED_RECIPIENT_NAME
        defaultTransactionShouldNotBeFound("recipientName.contains=" + UPDATED_RECIPIENT_NAME);
    }

    @Test
    @Transactional
    void getAllTransactionsByRecipientNameNotContainsSomething() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where recipientName does not contain DEFAULT_RECIPIENT_NAME
        defaultTransactionShouldNotBeFound("recipientName.doesNotContain=" + DEFAULT_RECIPIENT_NAME);

        // Get all the transactionList where recipientName does not contain UPDATED_RECIPIENT_NAME
        defaultTransactionShouldBeFound("recipientName.doesNotContain=" + UPDATED_RECIPIENT_NAME);
    }

    @Test
    @Transactional
    void getAllTransactionsByRecipientAccountIsEqualToSomething() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where recipientAccount equals to DEFAULT_RECIPIENT_ACCOUNT
        defaultTransactionShouldBeFound("recipientAccount.equals=" + DEFAULT_RECIPIENT_ACCOUNT);

        // Get all the transactionList where recipientAccount equals to UPDATED_RECIPIENT_ACCOUNT
        defaultTransactionShouldNotBeFound("recipientAccount.equals=" + UPDATED_RECIPIENT_ACCOUNT);
    }

    @Test
    @Transactional
    void getAllTransactionsByRecipientAccountIsNotEqualToSomething() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where recipientAccount not equals to DEFAULT_RECIPIENT_ACCOUNT
        defaultTransactionShouldNotBeFound("recipientAccount.notEquals=" + DEFAULT_RECIPIENT_ACCOUNT);

        // Get all the transactionList where recipientAccount not equals to UPDATED_RECIPIENT_ACCOUNT
        defaultTransactionShouldBeFound("recipientAccount.notEquals=" + UPDATED_RECIPIENT_ACCOUNT);
    }

    @Test
    @Transactional
    void getAllTransactionsByRecipientAccountIsInShouldWork() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where recipientAccount in DEFAULT_RECIPIENT_ACCOUNT or UPDATED_RECIPIENT_ACCOUNT
        defaultTransactionShouldBeFound("recipientAccount.in=" + DEFAULT_RECIPIENT_ACCOUNT + "," + UPDATED_RECIPIENT_ACCOUNT);

        // Get all the transactionList where recipientAccount equals to UPDATED_RECIPIENT_ACCOUNT
        defaultTransactionShouldNotBeFound("recipientAccount.in=" + UPDATED_RECIPIENT_ACCOUNT);
    }

    @Test
    @Transactional
    void getAllTransactionsByRecipientAccountIsNullOrNotNull() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where recipientAccount is not null
        defaultTransactionShouldBeFound("recipientAccount.specified=true");

        // Get all the transactionList where recipientAccount is null
        defaultTransactionShouldNotBeFound("recipientAccount.specified=false");
    }

    @Test
    @Transactional
    void getAllTransactionsByRecipientAccountContainsSomething() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where recipientAccount contains DEFAULT_RECIPIENT_ACCOUNT
        defaultTransactionShouldBeFound("recipientAccount.contains=" + DEFAULT_RECIPIENT_ACCOUNT);

        // Get all the transactionList where recipientAccount contains UPDATED_RECIPIENT_ACCOUNT
        defaultTransactionShouldNotBeFound("recipientAccount.contains=" + UPDATED_RECIPIENT_ACCOUNT);
    }

    @Test
    @Transactional
    void getAllTransactionsByRecipientAccountNotContainsSomething() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where recipientAccount does not contain DEFAULT_RECIPIENT_ACCOUNT
        defaultTransactionShouldNotBeFound("recipientAccount.doesNotContain=" + DEFAULT_RECIPIENT_ACCOUNT);

        // Get all the transactionList where recipientAccount does not contain UPDATED_RECIPIENT_ACCOUNT
        defaultTransactionShouldBeFound("recipientAccount.doesNotContain=" + UPDATED_RECIPIENT_ACCOUNT);
    }

    @Test
    @Transactional
    void getAllTransactionsByRecipientServiceIdIsEqualToSomething() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where recipientServiceId equals to DEFAULT_RECIPIENT_SERVICE_ID
        defaultTransactionShouldBeFound("recipientServiceId.equals=" + DEFAULT_RECIPIENT_SERVICE_ID);

        // Get all the transactionList where recipientServiceId equals to UPDATED_RECIPIENT_SERVICE_ID
        defaultTransactionShouldNotBeFound("recipientServiceId.equals=" + UPDATED_RECIPIENT_SERVICE_ID);
    }

    @Test
    @Transactional
    void getAllTransactionsByRecipientServiceIdIsNotEqualToSomething() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where recipientServiceId not equals to DEFAULT_RECIPIENT_SERVICE_ID
        defaultTransactionShouldNotBeFound("recipientServiceId.notEquals=" + DEFAULT_RECIPIENT_SERVICE_ID);

        // Get all the transactionList where recipientServiceId not equals to UPDATED_RECIPIENT_SERVICE_ID
        defaultTransactionShouldBeFound("recipientServiceId.notEquals=" + UPDATED_RECIPIENT_SERVICE_ID);
    }

    @Test
    @Transactional
    void getAllTransactionsByRecipientServiceIdIsInShouldWork() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where recipientServiceId in DEFAULT_RECIPIENT_SERVICE_ID or UPDATED_RECIPIENT_SERVICE_ID
        defaultTransactionShouldBeFound("recipientServiceId.in=" + DEFAULT_RECIPIENT_SERVICE_ID + "," + UPDATED_RECIPIENT_SERVICE_ID);

        // Get all the transactionList where recipientServiceId equals to UPDATED_RECIPIENT_SERVICE_ID
        defaultTransactionShouldNotBeFound("recipientServiceId.in=" + UPDATED_RECIPIENT_SERVICE_ID);
    }

    @Test
    @Transactional
    void getAllTransactionsByRecipientServiceIdIsNullOrNotNull() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where recipientServiceId is not null
        defaultTransactionShouldBeFound("recipientServiceId.specified=true");

        // Get all the transactionList where recipientServiceId is null
        defaultTransactionShouldNotBeFound("recipientServiceId.specified=false");
    }

    @Test
    @Transactional
    void getAllTransactionsByRecipientServiceIdIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where recipientServiceId is greater than or equal to DEFAULT_RECIPIENT_SERVICE_ID
        defaultTransactionShouldBeFound("recipientServiceId.greaterThanOrEqual=" + DEFAULT_RECIPIENT_SERVICE_ID);

        // Get all the transactionList where recipientServiceId is greater than or equal to UPDATED_RECIPIENT_SERVICE_ID
        defaultTransactionShouldNotBeFound("recipientServiceId.greaterThanOrEqual=" + UPDATED_RECIPIENT_SERVICE_ID);
    }

    @Test
    @Transactional
    void getAllTransactionsByRecipientServiceIdIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where recipientServiceId is less than or equal to DEFAULT_RECIPIENT_SERVICE_ID
        defaultTransactionShouldBeFound("recipientServiceId.lessThanOrEqual=" + DEFAULT_RECIPIENT_SERVICE_ID);

        // Get all the transactionList where recipientServiceId is less than or equal to SMALLER_RECIPIENT_SERVICE_ID
        defaultTransactionShouldNotBeFound("recipientServiceId.lessThanOrEqual=" + SMALLER_RECIPIENT_SERVICE_ID);
    }

    @Test
    @Transactional
    void getAllTransactionsByRecipientServiceIdIsLessThanSomething() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where recipientServiceId is less than DEFAULT_RECIPIENT_SERVICE_ID
        defaultTransactionShouldNotBeFound("recipientServiceId.lessThan=" + DEFAULT_RECIPIENT_SERVICE_ID);

        // Get all the transactionList where recipientServiceId is less than UPDATED_RECIPIENT_SERVICE_ID
        defaultTransactionShouldBeFound("recipientServiceId.lessThan=" + UPDATED_RECIPIENT_SERVICE_ID);
    }

    @Test
    @Transactional
    void getAllTransactionsByRecipientServiceIdIsGreaterThanSomething() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where recipientServiceId is greater than DEFAULT_RECIPIENT_SERVICE_ID
        defaultTransactionShouldNotBeFound("recipientServiceId.greaterThan=" + DEFAULT_RECIPIENT_SERVICE_ID);

        // Get all the transactionList where recipientServiceId is greater than SMALLER_RECIPIENT_SERVICE_ID
        defaultTransactionShouldBeFound("recipientServiceId.greaterThan=" + SMALLER_RECIPIENT_SERVICE_ID);
    }

    @Test
    @Transactional
    void getAllTransactionsByRecipientAmountIsEqualToSomething() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where recipientAmount equals to DEFAULT_RECIPIENT_AMOUNT
        defaultTransactionShouldBeFound("recipientAmount.equals=" + DEFAULT_RECIPIENT_AMOUNT);

        // Get all the transactionList where recipientAmount equals to UPDATED_RECIPIENT_AMOUNT
        defaultTransactionShouldNotBeFound("recipientAmount.equals=" + UPDATED_RECIPIENT_AMOUNT);
    }

    @Test
    @Transactional
    void getAllTransactionsByRecipientAmountIsNotEqualToSomething() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where recipientAmount not equals to DEFAULT_RECIPIENT_AMOUNT
        defaultTransactionShouldNotBeFound("recipientAmount.notEquals=" + DEFAULT_RECIPIENT_AMOUNT);

        // Get all the transactionList where recipientAmount not equals to UPDATED_RECIPIENT_AMOUNT
        defaultTransactionShouldBeFound("recipientAmount.notEquals=" + UPDATED_RECIPIENT_AMOUNT);
    }

    @Test
    @Transactional
    void getAllTransactionsByRecipientAmountIsInShouldWork() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where recipientAmount in DEFAULT_RECIPIENT_AMOUNT or UPDATED_RECIPIENT_AMOUNT
        defaultTransactionShouldBeFound("recipientAmount.in=" + DEFAULT_RECIPIENT_AMOUNT + "," + UPDATED_RECIPIENT_AMOUNT);

        // Get all the transactionList where recipientAmount equals to UPDATED_RECIPIENT_AMOUNT
        defaultTransactionShouldNotBeFound("recipientAmount.in=" + UPDATED_RECIPIENT_AMOUNT);
    }

    @Test
    @Transactional
    void getAllTransactionsByRecipientAmountIsNullOrNotNull() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where recipientAmount is not null
        defaultTransactionShouldBeFound("recipientAmount.specified=true");

        // Get all the transactionList where recipientAmount is null
        defaultTransactionShouldNotBeFound("recipientAmount.specified=false");
    }

    @Test
    @Transactional
    void getAllTransactionsByRecipientAmountIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where recipientAmount is greater than or equal to DEFAULT_RECIPIENT_AMOUNT
        defaultTransactionShouldBeFound("recipientAmount.greaterThanOrEqual=" + DEFAULT_RECIPIENT_AMOUNT);

        // Get all the transactionList where recipientAmount is greater than or equal to UPDATED_RECIPIENT_AMOUNT
        defaultTransactionShouldNotBeFound("recipientAmount.greaterThanOrEqual=" + UPDATED_RECIPIENT_AMOUNT);
    }

    @Test
    @Transactional
    void getAllTransactionsByRecipientAmountIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where recipientAmount is less than or equal to DEFAULT_RECIPIENT_AMOUNT
        defaultTransactionShouldBeFound("recipientAmount.lessThanOrEqual=" + DEFAULT_RECIPIENT_AMOUNT);

        // Get all the transactionList where recipientAmount is less than or equal to SMALLER_RECIPIENT_AMOUNT
        defaultTransactionShouldNotBeFound("recipientAmount.lessThanOrEqual=" + SMALLER_RECIPIENT_AMOUNT);
    }

    @Test
    @Transactional
    void getAllTransactionsByRecipientAmountIsLessThanSomething() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where recipientAmount is less than DEFAULT_RECIPIENT_AMOUNT
        defaultTransactionShouldNotBeFound("recipientAmount.lessThan=" + DEFAULT_RECIPIENT_AMOUNT);

        // Get all the transactionList where recipientAmount is less than UPDATED_RECIPIENT_AMOUNT
        defaultTransactionShouldBeFound("recipientAmount.lessThan=" + UPDATED_RECIPIENT_AMOUNT);
    }

    @Test
    @Transactional
    void getAllTransactionsByRecipientAmountIsGreaterThanSomething() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where recipientAmount is greater than DEFAULT_RECIPIENT_AMOUNT
        defaultTransactionShouldNotBeFound("recipientAmount.greaterThan=" + DEFAULT_RECIPIENT_AMOUNT);

        // Get all the transactionList where recipientAmount is greater than SMALLER_RECIPIENT_AMOUNT
        defaultTransactionShouldBeFound("recipientAmount.greaterThan=" + SMALLER_RECIPIENT_AMOUNT);
    }

    @Test
    @Transactional
    void getAllTransactionsByRecipientCcyIsEqualToSomething() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where recipientCcy equals to DEFAULT_RECIPIENT_CCY
        defaultTransactionShouldBeFound("recipientCcy.equals=" + DEFAULT_RECIPIENT_CCY);

        // Get all the transactionList where recipientCcy equals to UPDATED_RECIPIENT_CCY
        defaultTransactionShouldNotBeFound("recipientCcy.equals=" + UPDATED_RECIPIENT_CCY);
    }

    @Test
    @Transactional
    void getAllTransactionsByRecipientCcyIsNotEqualToSomething() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where recipientCcy not equals to DEFAULT_RECIPIENT_CCY
        defaultTransactionShouldNotBeFound("recipientCcy.notEquals=" + DEFAULT_RECIPIENT_CCY);

        // Get all the transactionList where recipientCcy not equals to UPDATED_RECIPIENT_CCY
        defaultTransactionShouldBeFound("recipientCcy.notEquals=" + UPDATED_RECIPIENT_CCY);
    }

    @Test
    @Transactional
    void getAllTransactionsByRecipientCcyIsInShouldWork() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where recipientCcy in DEFAULT_RECIPIENT_CCY or UPDATED_RECIPIENT_CCY
        defaultTransactionShouldBeFound("recipientCcy.in=" + DEFAULT_RECIPIENT_CCY + "," + UPDATED_RECIPIENT_CCY);

        // Get all the transactionList where recipientCcy equals to UPDATED_RECIPIENT_CCY
        defaultTransactionShouldNotBeFound("recipientCcy.in=" + UPDATED_RECIPIENT_CCY);
    }

    @Test
    @Transactional
    void getAllTransactionsByRecipientCcyIsNullOrNotNull() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where recipientCcy is not null
        defaultTransactionShouldBeFound("recipientCcy.specified=true");

        // Get all the transactionList where recipientCcy is null
        defaultTransactionShouldNotBeFound("recipientCcy.specified=false");
    }

    @Test
    @Transactional
    void getAllTransactionsByRecipientCcyIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where recipientCcy is greater than or equal to DEFAULT_RECIPIENT_CCY
        defaultTransactionShouldBeFound("recipientCcy.greaterThanOrEqual=" + DEFAULT_RECIPIENT_CCY);

        // Get all the transactionList where recipientCcy is greater than or equal to UPDATED_RECIPIENT_CCY
        defaultTransactionShouldNotBeFound("recipientCcy.greaterThanOrEqual=" + UPDATED_RECIPIENT_CCY);
    }

    @Test
    @Transactional
    void getAllTransactionsByRecipientCcyIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where recipientCcy is less than or equal to DEFAULT_RECIPIENT_CCY
        defaultTransactionShouldBeFound("recipientCcy.lessThanOrEqual=" + DEFAULT_RECIPIENT_CCY);

        // Get all the transactionList where recipientCcy is less than or equal to SMALLER_RECIPIENT_CCY
        defaultTransactionShouldNotBeFound("recipientCcy.lessThanOrEqual=" + SMALLER_RECIPIENT_CCY);
    }

    @Test
    @Transactional
    void getAllTransactionsByRecipientCcyIsLessThanSomething() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where recipientCcy is less than DEFAULT_RECIPIENT_CCY
        defaultTransactionShouldNotBeFound("recipientCcy.lessThan=" + DEFAULT_RECIPIENT_CCY);

        // Get all the transactionList where recipientCcy is less than UPDATED_RECIPIENT_CCY
        defaultTransactionShouldBeFound("recipientCcy.lessThan=" + UPDATED_RECIPIENT_CCY);
    }

    @Test
    @Transactional
    void getAllTransactionsByRecipientCcyIsGreaterThanSomething() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where recipientCcy is greater than DEFAULT_RECIPIENT_CCY
        defaultTransactionShouldNotBeFound("recipientCcy.greaterThan=" + DEFAULT_RECIPIENT_CCY);

        // Get all the transactionList where recipientCcy is greater than SMALLER_RECIPIENT_CCY
        defaultTransactionShouldBeFound("recipientCcy.greaterThan=" + SMALLER_RECIPIENT_CCY);
    }

    @Test
    @Transactional
    void getAllTransactionsByRecipientTrIdIsEqualToSomething() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where recipientTrId equals to DEFAULT_RECIPIENT_TR_ID
        defaultTransactionShouldBeFound("recipientTrId.equals=" + DEFAULT_RECIPIENT_TR_ID);

        // Get all the transactionList where recipientTrId equals to UPDATED_RECIPIENT_TR_ID
        defaultTransactionShouldNotBeFound("recipientTrId.equals=" + UPDATED_RECIPIENT_TR_ID);
    }

    @Test
    @Transactional
    void getAllTransactionsByRecipientTrIdIsNotEqualToSomething() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where recipientTrId not equals to DEFAULT_RECIPIENT_TR_ID
        defaultTransactionShouldNotBeFound("recipientTrId.notEquals=" + DEFAULT_RECIPIENT_TR_ID);

        // Get all the transactionList where recipientTrId not equals to UPDATED_RECIPIENT_TR_ID
        defaultTransactionShouldBeFound("recipientTrId.notEquals=" + UPDATED_RECIPIENT_TR_ID);
    }

    @Test
    @Transactional
    void getAllTransactionsByRecipientTrIdIsInShouldWork() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where recipientTrId in DEFAULT_RECIPIENT_TR_ID or UPDATED_RECIPIENT_TR_ID
        defaultTransactionShouldBeFound("recipientTrId.in=" + DEFAULT_RECIPIENT_TR_ID + "," + UPDATED_RECIPIENT_TR_ID);

        // Get all the transactionList where recipientTrId equals to UPDATED_RECIPIENT_TR_ID
        defaultTransactionShouldNotBeFound("recipientTrId.in=" + UPDATED_RECIPIENT_TR_ID);
    }

    @Test
    @Transactional
    void getAllTransactionsByRecipientTrIdIsNullOrNotNull() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where recipientTrId is not null
        defaultTransactionShouldBeFound("recipientTrId.specified=true");

        // Get all the transactionList where recipientTrId is null
        defaultTransactionShouldNotBeFound("recipientTrId.specified=false");
    }

    @Test
    @Transactional
    void getAllTransactionsByRecipientTrIdContainsSomething() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where recipientTrId contains DEFAULT_RECIPIENT_TR_ID
        defaultTransactionShouldBeFound("recipientTrId.contains=" + DEFAULT_RECIPIENT_TR_ID);

        // Get all the transactionList where recipientTrId contains UPDATED_RECIPIENT_TR_ID
        defaultTransactionShouldNotBeFound("recipientTrId.contains=" + UPDATED_RECIPIENT_TR_ID);
    }

    @Test
    @Transactional
    void getAllTransactionsByRecipientTrIdNotContainsSomething() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where recipientTrId does not contain DEFAULT_RECIPIENT_TR_ID
        defaultTransactionShouldNotBeFound("recipientTrId.doesNotContain=" + DEFAULT_RECIPIENT_TR_ID);

        // Get all the transactionList where recipientTrId does not contain UPDATED_RECIPIENT_TR_ID
        defaultTransactionShouldBeFound("recipientTrId.doesNotContain=" + UPDATED_RECIPIENT_TR_ID);
    }

    @Test
    @Transactional
    void getAllTransactionsByRecipientSuccessIsEqualToSomething() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where recipientSuccess equals to DEFAULT_RECIPIENT_SUCCESS
        defaultTransactionShouldBeFound("recipientSuccess.equals=" + DEFAULT_RECIPIENT_SUCCESS);

        // Get all the transactionList where recipientSuccess equals to UPDATED_RECIPIENT_SUCCESS
        defaultTransactionShouldNotBeFound("recipientSuccess.equals=" + UPDATED_RECIPIENT_SUCCESS);
    }

    @Test
    @Transactional
    void getAllTransactionsByRecipientSuccessIsNotEqualToSomething() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where recipientSuccess not equals to DEFAULT_RECIPIENT_SUCCESS
        defaultTransactionShouldNotBeFound("recipientSuccess.notEquals=" + DEFAULT_RECIPIENT_SUCCESS);

        // Get all the transactionList where recipientSuccess not equals to UPDATED_RECIPIENT_SUCCESS
        defaultTransactionShouldBeFound("recipientSuccess.notEquals=" + UPDATED_RECIPIENT_SUCCESS);
    }

    @Test
    @Transactional
    void getAllTransactionsByRecipientSuccessIsInShouldWork() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where recipientSuccess in DEFAULT_RECIPIENT_SUCCESS or UPDATED_RECIPIENT_SUCCESS
        defaultTransactionShouldBeFound("recipientSuccess.in=" + DEFAULT_RECIPIENT_SUCCESS + "," + UPDATED_RECIPIENT_SUCCESS);

        // Get all the transactionList where recipientSuccess equals to UPDATED_RECIPIENT_SUCCESS
        defaultTransactionShouldNotBeFound("recipientSuccess.in=" + UPDATED_RECIPIENT_SUCCESS);
    }

    @Test
    @Transactional
    void getAllTransactionsByRecipientSuccessIsNullOrNotNull() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where recipientSuccess is not null
        defaultTransactionShouldBeFound("recipientSuccess.specified=true");

        // Get all the transactionList where recipientSuccess is null
        defaultTransactionShouldNotBeFound("recipientSuccess.specified=false");
    }

    @Test
    @Transactional
    void getAllTransactionsByRecipientDateIsEqualToSomething() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where recipientDate equals to DEFAULT_RECIPIENT_DATE
        defaultTransactionShouldBeFound("recipientDate.equals=" + DEFAULT_RECIPIENT_DATE);

        // Get all the transactionList where recipientDate equals to UPDATED_RECIPIENT_DATE
        defaultTransactionShouldNotBeFound("recipientDate.equals=" + UPDATED_RECIPIENT_DATE);
    }

    @Test
    @Transactional
    void getAllTransactionsByRecipientDateIsNotEqualToSomething() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where recipientDate not equals to DEFAULT_RECIPIENT_DATE
        defaultTransactionShouldNotBeFound("recipientDate.notEquals=" + DEFAULT_RECIPIENT_DATE);

        // Get all the transactionList where recipientDate not equals to UPDATED_RECIPIENT_DATE
        defaultTransactionShouldBeFound("recipientDate.notEquals=" + UPDATED_RECIPIENT_DATE);
    }

    @Test
    @Transactional
    void getAllTransactionsByRecipientDateIsInShouldWork() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where recipientDate in DEFAULT_RECIPIENT_DATE or UPDATED_RECIPIENT_DATE
        defaultTransactionShouldBeFound("recipientDate.in=" + DEFAULT_RECIPIENT_DATE + "," + UPDATED_RECIPIENT_DATE);

        // Get all the transactionList where recipientDate equals to UPDATED_RECIPIENT_DATE
        defaultTransactionShouldNotBeFound("recipientDate.in=" + UPDATED_RECIPIENT_DATE);
    }

    @Test
    @Transactional
    void getAllTransactionsByRecipientDateIsNullOrNotNull() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where recipientDate is not null
        defaultTransactionShouldBeFound("recipientDate.specified=true");

        // Get all the transactionList where recipientDate is null
        defaultTransactionShouldNotBeFound("recipientDate.specified=false");
    }

    @Test
    @Transactional
    void getAllTransactionsByRecipientStatusIsEqualToSomething() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where recipientStatus equals to DEFAULT_RECIPIENT_STATUS
        defaultTransactionShouldBeFound("recipientStatus.equals=" + DEFAULT_RECIPIENT_STATUS);

        // Get all the transactionList where recipientStatus equals to UPDATED_RECIPIENT_STATUS
        defaultTransactionShouldNotBeFound("recipientStatus.equals=" + UPDATED_RECIPIENT_STATUS);
    }

    @Test
    @Transactional
    void getAllTransactionsByRecipientStatusIsNotEqualToSomething() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where recipientStatus not equals to DEFAULT_RECIPIENT_STATUS
        defaultTransactionShouldNotBeFound("recipientStatus.notEquals=" + DEFAULT_RECIPIENT_STATUS);

        // Get all the transactionList where recipientStatus not equals to UPDATED_RECIPIENT_STATUS
        defaultTransactionShouldBeFound("recipientStatus.notEquals=" + UPDATED_RECIPIENT_STATUS);
    }

    @Test
    @Transactional
    void getAllTransactionsByRecipientStatusIsInShouldWork() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where recipientStatus in DEFAULT_RECIPIENT_STATUS or UPDATED_RECIPIENT_STATUS
        defaultTransactionShouldBeFound("recipientStatus.in=" + DEFAULT_RECIPIENT_STATUS + "," + UPDATED_RECIPIENT_STATUS);

        // Get all the transactionList where recipientStatus equals to UPDATED_RECIPIENT_STATUS
        defaultTransactionShouldNotBeFound("recipientStatus.in=" + UPDATED_RECIPIENT_STATUS);
    }

    @Test
    @Transactional
    void getAllTransactionsByRecipientStatusIsNullOrNotNull() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where recipientStatus is not null
        defaultTransactionShouldBeFound("recipientStatus.specified=true");

        // Get all the transactionList where recipientStatus is null
        defaultTransactionShouldNotBeFound("recipientStatus.specified=false");
    }

    @Test
    @Transactional
    void getAllTransactionsByRecipientStatusContainsSomething() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where recipientStatus contains DEFAULT_RECIPIENT_STATUS
        defaultTransactionShouldBeFound("recipientStatus.contains=" + DEFAULT_RECIPIENT_STATUS);

        // Get all the transactionList where recipientStatus contains UPDATED_RECIPIENT_STATUS
        defaultTransactionShouldNotBeFound("recipientStatus.contains=" + UPDATED_RECIPIENT_STATUS);
    }

    @Test
    @Transactional
    void getAllTransactionsByRecipientStatusNotContainsSomething() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where recipientStatus does not contain DEFAULT_RECIPIENT_STATUS
        defaultTransactionShouldNotBeFound("recipientStatus.doesNotContain=" + DEFAULT_RECIPIENT_STATUS);

        // Get all the transactionList where recipientStatus does not contain UPDATED_RECIPIENT_STATUS
        defaultTransactionShouldBeFound("recipientStatus.doesNotContain=" + UPDATED_RECIPIENT_STATUS);
    }

    @Test
    @Transactional
    void getAllTransactionsByRecipientStatusTextIsEqualToSomething() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where recipientStatusText equals to DEFAULT_RECIPIENT_STATUS_TEXT
        defaultTransactionShouldBeFound("recipientStatusText.equals=" + DEFAULT_RECIPIENT_STATUS_TEXT);

        // Get all the transactionList where recipientStatusText equals to UPDATED_RECIPIENT_STATUS_TEXT
        defaultTransactionShouldNotBeFound("recipientStatusText.equals=" + UPDATED_RECIPIENT_STATUS_TEXT);
    }

    @Test
    @Transactional
    void getAllTransactionsByRecipientStatusTextIsNotEqualToSomething() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where recipientStatusText not equals to DEFAULT_RECIPIENT_STATUS_TEXT
        defaultTransactionShouldNotBeFound("recipientStatusText.notEquals=" + DEFAULT_RECIPIENT_STATUS_TEXT);

        // Get all the transactionList where recipientStatusText not equals to UPDATED_RECIPIENT_STATUS_TEXT
        defaultTransactionShouldBeFound("recipientStatusText.notEquals=" + UPDATED_RECIPIENT_STATUS_TEXT);
    }

    @Test
    @Transactional
    void getAllTransactionsByRecipientStatusTextIsInShouldWork() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where recipientStatusText in DEFAULT_RECIPIENT_STATUS_TEXT or UPDATED_RECIPIENT_STATUS_TEXT
        defaultTransactionShouldBeFound("recipientStatusText.in=" + DEFAULT_RECIPIENT_STATUS_TEXT + "," + UPDATED_RECIPIENT_STATUS_TEXT);

        // Get all the transactionList where recipientStatusText equals to UPDATED_RECIPIENT_STATUS_TEXT
        defaultTransactionShouldNotBeFound("recipientStatusText.in=" + UPDATED_RECIPIENT_STATUS_TEXT);
    }

    @Test
    @Transactional
    void getAllTransactionsByRecipientStatusTextIsNullOrNotNull() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where recipientStatusText is not null
        defaultTransactionShouldBeFound("recipientStatusText.specified=true");

        // Get all the transactionList where recipientStatusText is null
        defaultTransactionShouldNotBeFound("recipientStatusText.specified=false");
    }

    @Test
    @Transactional
    void getAllTransactionsByRecipientStatusTextContainsSomething() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where recipientStatusText contains DEFAULT_RECIPIENT_STATUS_TEXT
        defaultTransactionShouldBeFound("recipientStatusText.contains=" + DEFAULT_RECIPIENT_STATUS_TEXT);

        // Get all the transactionList where recipientStatusText contains UPDATED_RECIPIENT_STATUS_TEXT
        defaultTransactionShouldNotBeFound("recipientStatusText.contains=" + UPDATED_RECIPIENT_STATUS_TEXT);
    }

    @Test
    @Transactional
    void getAllTransactionsByRecipientStatusTextNotContainsSomething() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where recipientStatusText does not contain DEFAULT_RECIPIENT_STATUS_TEXT
        defaultTransactionShouldNotBeFound("recipientStatusText.doesNotContain=" + DEFAULT_RECIPIENT_STATUS_TEXT);

        // Get all the transactionList where recipientStatusText does not contain UPDATED_RECIPIENT_STATUS_TEXT
        defaultTransactionShouldBeFound("recipientStatusText.doesNotContain=" + UPDATED_RECIPIENT_STATUS_TEXT);
    }

    @Test
    @Transactional
    void getAllTransactionsByRecipientBillIdIsEqualToSomething() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where recipientBillId equals to DEFAULT_RECIPIENT_BILL_ID
        defaultTransactionShouldBeFound("recipientBillId.equals=" + DEFAULT_RECIPIENT_BILL_ID);

        // Get all the transactionList where recipientBillId equals to UPDATED_RECIPIENT_BILL_ID
        defaultTransactionShouldNotBeFound("recipientBillId.equals=" + UPDATED_RECIPIENT_BILL_ID);
    }

    @Test
    @Transactional
    void getAllTransactionsByRecipientBillIdIsNotEqualToSomething() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where recipientBillId not equals to DEFAULT_RECIPIENT_BILL_ID
        defaultTransactionShouldNotBeFound("recipientBillId.notEquals=" + DEFAULT_RECIPIENT_BILL_ID);

        // Get all the transactionList where recipientBillId not equals to UPDATED_RECIPIENT_BILL_ID
        defaultTransactionShouldBeFound("recipientBillId.notEquals=" + UPDATED_RECIPIENT_BILL_ID);
    }

    @Test
    @Transactional
    void getAllTransactionsByRecipientBillIdIsInShouldWork() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where recipientBillId in DEFAULT_RECIPIENT_BILL_ID or UPDATED_RECIPIENT_BILL_ID
        defaultTransactionShouldBeFound("recipientBillId.in=" + DEFAULT_RECIPIENT_BILL_ID + "," + UPDATED_RECIPIENT_BILL_ID);

        // Get all the transactionList where recipientBillId equals to UPDATED_RECIPIENT_BILL_ID
        defaultTransactionShouldNotBeFound("recipientBillId.in=" + UPDATED_RECIPIENT_BILL_ID);
    }

    @Test
    @Transactional
    void getAllTransactionsByRecipientBillIdIsNullOrNotNull() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where recipientBillId is not null
        defaultTransactionShouldBeFound("recipientBillId.specified=true");

        // Get all the transactionList where recipientBillId is null
        defaultTransactionShouldNotBeFound("recipientBillId.specified=false");
    }

    @Test
    @Transactional
    void getAllTransactionsByRecipientBillIdIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where recipientBillId is greater than or equal to DEFAULT_RECIPIENT_BILL_ID
        defaultTransactionShouldBeFound("recipientBillId.greaterThanOrEqual=" + DEFAULT_RECIPIENT_BILL_ID);

        // Get all the transactionList where recipientBillId is greater than or equal to UPDATED_RECIPIENT_BILL_ID
        defaultTransactionShouldNotBeFound("recipientBillId.greaterThanOrEqual=" + UPDATED_RECIPIENT_BILL_ID);
    }

    @Test
    @Transactional
    void getAllTransactionsByRecipientBillIdIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where recipientBillId is less than or equal to DEFAULT_RECIPIENT_BILL_ID
        defaultTransactionShouldBeFound("recipientBillId.lessThanOrEqual=" + DEFAULT_RECIPIENT_BILL_ID);

        // Get all the transactionList where recipientBillId is less than or equal to SMALLER_RECIPIENT_BILL_ID
        defaultTransactionShouldNotBeFound("recipientBillId.lessThanOrEqual=" + SMALLER_RECIPIENT_BILL_ID);
    }

    @Test
    @Transactional
    void getAllTransactionsByRecipientBillIdIsLessThanSomething() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where recipientBillId is less than DEFAULT_RECIPIENT_BILL_ID
        defaultTransactionShouldNotBeFound("recipientBillId.lessThan=" + DEFAULT_RECIPIENT_BILL_ID);

        // Get all the transactionList where recipientBillId is less than UPDATED_RECIPIENT_BILL_ID
        defaultTransactionShouldBeFound("recipientBillId.lessThan=" + UPDATED_RECIPIENT_BILL_ID);
    }

    @Test
    @Transactional
    void getAllTransactionsByRecipientBillIdIsGreaterThanSomething() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where recipientBillId is greater than DEFAULT_RECIPIENT_BILL_ID
        defaultTransactionShouldNotBeFound("recipientBillId.greaterThan=" + DEFAULT_RECIPIENT_BILL_ID);

        // Get all the transactionList where recipientBillId is greater than SMALLER_RECIPIENT_BILL_ID
        defaultTransactionShouldBeFound("recipientBillId.greaterThan=" + SMALLER_RECIPIENT_BILL_ID);
    }

    @Test
    @Transactional
    void getAllTransactionsByRecipientGatewayIsEqualToSomething() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where recipientGateway equals to DEFAULT_RECIPIENT_GATEWAY
        defaultTransactionShouldBeFound("recipientGateway.equals=" + DEFAULT_RECIPIENT_GATEWAY);

        // Get all the transactionList where recipientGateway equals to UPDATED_RECIPIENT_GATEWAY
        defaultTransactionShouldNotBeFound("recipientGateway.equals=" + UPDATED_RECIPIENT_GATEWAY);
    }

    @Test
    @Transactional
    void getAllTransactionsByRecipientGatewayIsNotEqualToSomething() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where recipientGateway not equals to DEFAULT_RECIPIENT_GATEWAY
        defaultTransactionShouldNotBeFound("recipientGateway.notEquals=" + DEFAULT_RECIPIENT_GATEWAY);

        // Get all the transactionList where recipientGateway not equals to UPDATED_RECIPIENT_GATEWAY
        defaultTransactionShouldBeFound("recipientGateway.notEquals=" + UPDATED_RECIPIENT_GATEWAY);
    }

    @Test
    @Transactional
    void getAllTransactionsByRecipientGatewayIsInShouldWork() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where recipientGateway in DEFAULT_RECIPIENT_GATEWAY or UPDATED_RECIPIENT_GATEWAY
        defaultTransactionShouldBeFound("recipientGateway.in=" + DEFAULT_RECIPIENT_GATEWAY + "," + UPDATED_RECIPIENT_GATEWAY);

        // Get all the transactionList where recipientGateway equals to UPDATED_RECIPIENT_GATEWAY
        defaultTransactionShouldNotBeFound("recipientGateway.in=" + UPDATED_RECIPIENT_GATEWAY);
    }

    @Test
    @Transactional
    void getAllTransactionsByRecipientGatewayIsNullOrNotNull() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where recipientGateway is not null
        defaultTransactionShouldBeFound("recipientGateway.specified=true");

        // Get all the transactionList where recipientGateway is null
        defaultTransactionShouldNotBeFound("recipientGateway.specified=false");
    }

    @Test
    @Transactional
    void getAllTransactionsByRecipientGatewayContainsSomething() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where recipientGateway contains DEFAULT_RECIPIENT_GATEWAY
        defaultTransactionShouldBeFound("recipientGateway.contains=" + DEFAULT_RECIPIENT_GATEWAY);

        // Get all the transactionList where recipientGateway contains UPDATED_RECIPIENT_GATEWAY
        defaultTransactionShouldNotBeFound("recipientGateway.contains=" + UPDATED_RECIPIENT_GATEWAY);
    }

    @Test
    @Transactional
    void getAllTransactionsByRecipientGatewayNotContainsSomething() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where recipientGateway does not contain DEFAULT_RECIPIENT_GATEWAY
        defaultTransactionShouldNotBeFound("recipientGateway.doesNotContain=" + DEFAULT_RECIPIENT_GATEWAY);

        // Get all the transactionList where recipientGateway does not contain UPDATED_RECIPIENT_GATEWAY
        defaultTransactionShouldBeFound("recipientGateway.doesNotContain=" + UPDATED_RECIPIENT_GATEWAY);
    }

    @Test
    @Transactional
    void getAllTransactionsBySystemCommissionUpAmountIsEqualToSomething() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where systemCommissionUpAmount equals to DEFAULT_SYSTEM_COMMISSION_UP_AMOUNT
        defaultTransactionShouldBeFound("systemCommissionUpAmount.equals=" + DEFAULT_SYSTEM_COMMISSION_UP_AMOUNT);

        // Get all the transactionList where systemCommissionUpAmount equals to UPDATED_SYSTEM_COMMISSION_UP_AMOUNT
        defaultTransactionShouldNotBeFound("systemCommissionUpAmount.equals=" + UPDATED_SYSTEM_COMMISSION_UP_AMOUNT);
    }

    @Test
    @Transactional
    void getAllTransactionsBySystemCommissionUpAmountIsNotEqualToSomething() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where systemCommissionUpAmount not equals to DEFAULT_SYSTEM_COMMISSION_UP_AMOUNT
        defaultTransactionShouldNotBeFound("systemCommissionUpAmount.notEquals=" + DEFAULT_SYSTEM_COMMISSION_UP_AMOUNT);

        // Get all the transactionList where systemCommissionUpAmount not equals to UPDATED_SYSTEM_COMMISSION_UP_AMOUNT
        defaultTransactionShouldBeFound("systemCommissionUpAmount.notEquals=" + UPDATED_SYSTEM_COMMISSION_UP_AMOUNT);
    }

    @Test
    @Transactional
    void getAllTransactionsBySystemCommissionUpAmountIsInShouldWork() throws Exception {
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
    void getAllTransactionsBySystemCommissionUpAmountIsNullOrNotNull() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where systemCommissionUpAmount is not null
        defaultTransactionShouldBeFound("systemCommissionUpAmount.specified=true");

        // Get all the transactionList where systemCommissionUpAmount is null
        defaultTransactionShouldNotBeFound("systemCommissionUpAmount.specified=false");
    }

    @Test
    @Transactional
    void getAllTransactionsBySystemCommissionUpAmountIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where systemCommissionUpAmount is greater than or equal to DEFAULT_SYSTEM_COMMISSION_UP_AMOUNT
        defaultTransactionShouldBeFound("systemCommissionUpAmount.greaterThanOrEqual=" + DEFAULT_SYSTEM_COMMISSION_UP_AMOUNT);

        // Get all the transactionList where systemCommissionUpAmount is greater than or equal to UPDATED_SYSTEM_COMMISSION_UP_AMOUNT
        defaultTransactionShouldNotBeFound("systemCommissionUpAmount.greaterThanOrEqual=" + UPDATED_SYSTEM_COMMISSION_UP_AMOUNT);
    }

    @Test
    @Transactional
    void getAllTransactionsBySystemCommissionUpAmountIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where systemCommissionUpAmount is less than or equal to DEFAULT_SYSTEM_COMMISSION_UP_AMOUNT
        defaultTransactionShouldBeFound("systemCommissionUpAmount.lessThanOrEqual=" + DEFAULT_SYSTEM_COMMISSION_UP_AMOUNT);

        // Get all the transactionList where systemCommissionUpAmount is less than or equal to SMALLER_SYSTEM_COMMISSION_UP_AMOUNT
        defaultTransactionShouldNotBeFound("systemCommissionUpAmount.lessThanOrEqual=" + SMALLER_SYSTEM_COMMISSION_UP_AMOUNT);
    }

    @Test
    @Transactional
    void getAllTransactionsBySystemCommissionUpAmountIsLessThanSomething() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where systemCommissionUpAmount is less than DEFAULT_SYSTEM_COMMISSION_UP_AMOUNT
        defaultTransactionShouldNotBeFound("systemCommissionUpAmount.lessThan=" + DEFAULT_SYSTEM_COMMISSION_UP_AMOUNT);

        // Get all the transactionList where systemCommissionUpAmount is less than UPDATED_SYSTEM_COMMISSION_UP_AMOUNT
        defaultTransactionShouldBeFound("systemCommissionUpAmount.lessThan=" + UPDATED_SYSTEM_COMMISSION_UP_AMOUNT);
    }

    @Test
    @Transactional
    void getAllTransactionsBySystemCommissionUpAmountIsGreaterThanSomething() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where systemCommissionUpAmount is greater than DEFAULT_SYSTEM_COMMISSION_UP_AMOUNT
        defaultTransactionShouldNotBeFound("systemCommissionUpAmount.greaterThan=" + DEFAULT_SYSTEM_COMMISSION_UP_AMOUNT);

        // Get all the transactionList where systemCommissionUpAmount is greater than SMALLER_SYSTEM_COMMISSION_UP_AMOUNT
        defaultTransactionShouldBeFound("systemCommissionUpAmount.greaterThan=" + SMALLER_SYSTEM_COMMISSION_UP_AMOUNT);
    }

    @Test
    @Transactional
    void getAllTransactionsBySystemCommissionDownAmountIsEqualToSomething() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where systemCommissionDownAmount equals to DEFAULT_SYSTEM_COMMISSION_DOWN_AMOUNT
        defaultTransactionShouldBeFound("systemCommissionDownAmount.equals=" + DEFAULT_SYSTEM_COMMISSION_DOWN_AMOUNT);

        // Get all the transactionList where systemCommissionDownAmount equals to UPDATED_SYSTEM_COMMISSION_DOWN_AMOUNT
        defaultTransactionShouldNotBeFound("systemCommissionDownAmount.equals=" + UPDATED_SYSTEM_COMMISSION_DOWN_AMOUNT);
    }

    @Test
    @Transactional
    void getAllTransactionsBySystemCommissionDownAmountIsNotEqualToSomething() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where systemCommissionDownAmount not equals to DEFAULT_SYSTEM_COMMISSION_DOWN_AMOUNT
        defaultTransactionShouldNotBeFound("systemCommissionDownAmount.notEquals=" + DEFAULT_SYSTEM_COMMISSION_DOWN_AMOUNT);

        // Get all the transactionList where systemCommissionDownAmount not equals to UPDATED_SYSTEM_COMMISSION_DOWN_AMOUNT
        defaultTransactionShouldBeFound("systemCommissionDownAmount.notEquals=" + UPDATED_SYSTEM_COMMISSION_DOWN_AMOUNT);
    }

    @Test
    @Transactional
    void getAllTransactionsBySystemCommissionDownAmountIsInShouldWork() throws Exception {
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
    void getAllTransactionsBySystemCommissionDownAmountIsNullOrNotNull() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where systemCommissionDownAmount is not null
        defaultTransactionShouldBeFound("systemCommissionDownAmount.specified=true");

        // Get all the transactionList where systemCommissionDownAmount is null
        defaultTransactionShouldNotBeFound("systemCommissionDownAmount.specified=false");
    }

    @Test
    @Transactional
    void getAllTransactionsBySystemCommissionDownAmountIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where systemCommissionDownAmount is greater than or equal to DEFAULT_SYSTEM_COMMISSION_DOWN_AMOUNT
        defaultTransactionShouldBeFound("systemCommissionDownAmount.greaterThanOrEqual=" + DEFAULT_SYSTEM_COMMISSION_DOWN_AMOUNT);

        // Get all the transactionList where systemCommissionDownAmount is greater than or equal to UPDATED_SYSTEM_COMMISSION_DOWN_AMOUNT
        defaultTransactionShouldNotBeFound("systemCommissionDownAmount.greaterThanOrEqual=" + UPDATED_SYSTEM_COMMISSION_DOWN_AMOUNT);
    }

    @Test
    @Transactional
    void getAllTransactionsBySystemCommissionDownAmountIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where systemCommissionDownAmount is less than or equal to DEFAULT_SYSTEM_COMMISSION_DOWN_AMOUNT
        defaultTransactionShouldBeFound("systemCommissionDownAmount.lessThanOrEqual=" + DEFAULT_SYSTEM_COMMISSION_DOWN_AMOUNT);

        // Get all the transactionList where systemCommissionDownAmount is less than or equal to SMALLER_SYSTEM_COMMISSION_DOWN_AMOUNT
        defaultTransactionShouldNotBeFound("systemCommissionDownAmount.lessThanOrEqual=" + SMALLER_SYSTEM_COMMISSION_DOWN_AMOUNT);
    }

    @Test
    @Transactional
    void getAllTransactionsBySystemCommissionDownAmountIsLessThanSomething() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where systemCommissionDownAmount is less than DEFAULT_SYSTEM_COMMISSION_DOWN_AMOUNT
        defaultTransactionShouldNotBeFound("systemCommissionDownAmount.lessThan=" + DEFAULT_SYSTEM_COMMISSION_DOWN_AMOUNT);

        // Get all the transactionList where systemCommissionDownAmount is less than UPDATED_SYSTEM_COMMISSION_DOWN_AMOUNT
        defaultTransactionShouldBeFound("systemCommissionDownAmount.lessThan=" + UPDATED_SYSTEM_COMMISSION_DOWN_AMOUNT);
    }

    @Test
    @Transactional
    void getAllTransactionsBySystemCommissionDownAmountIsGreaterThanSomething() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where systemCommissionDownAmount is greater than DEFAULT_SYSTEM_COMMISSION_DOWN_AMOUNT
        defaultTransactionShouldNotBeFound("systemCommissionDownAmount.greaterThan=" + DEFAULT_SYSTEM_COMMISSION_DOWN_AMOUNT);

        // Get all the transactionList where systemCommissionDownAmount is greater than SMALLER_SYSTEM_COMMISSION_DOWN_AMOUNT
        defaultTransactionShouldBeFound("systemCommissionDownAmount.greaterThan=" + SMALLER_SYSTEM_COMMISSION_DOWN_AMOUNT);
    }

    @Test
    @Transactional
    void getAllTransactionsBySystemFeeAmountIsEqualToSomething() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where systemFeeAmount equals to DEFAULT_SYSTEM_FEE_AMOUNT
        defaultTransactionShouldBeFound("systemFeeAmount.equals=" + DEFAULT_SYSTEM_FEE_AMOUNT);

        // Get all the transactionList where systemFeeAmount equals to UPDATED_SYSTEM_FEE_AMOUNT
        defaultTransactionShouldNotBeFound("systemFeeAmount.equals=" + UPDATED_SYSTEM_FEE_AMOUNT);
    }

    @Test
    @Transactional
    void getAllTransactionsBySystemFeeAmountIsNotEqualToSomething() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where systemFeeAmount not equals to DEFAULT_SYSTEM_FEE_AMOUNT
        defaultTransactionShouldNotBeFound("systemFeeAmount.notEquals=" + DEFAULT_SYSTEM_FEE_AMOUNT);

        // Get all the transactionList where systemFeeAmount not equals to UPDATED_SYSTEM_FEE_AMOUNT
        defaultTransactionShouldBeFound("systemFeeAmount.notEquals=" + UPDATED_SYSTEM_FEE_AMOUNT);
    }

    @Test
    @Transactional
    void getAllTransactionsBySystemFeeAmountIsInShouldWork() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where systemFeeAmount in DEFAULT_SYSTEM_FEE_AMOUNT or UPDATED_SYSTEM_FEE_AMOUNT
        defaultTransactionShouldBeFound("systemFeeAmount.in=" + DEFAULT_SYSTEM_FEE_AMOUNT + "," + UPDATED_SYSTEM_FEE_AMOUNT);

        // Get all the transactionList where systemFeeAmount equals to UPDATED_SYSTEM_FEE_AMOUNT
        defaultTransactionShouldNotBeFound("systemFeeAmount.in=" + UPDATED_SYSTEM_FEE_AMOUNT);
    }

    @Test
    @Transactional
    void getAllTransactionsBySystemFeeAmountIsNullOrNotNull() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where systemFeeAmount is not null
        defaultTransactionShouldBeFound("systemFeeAmount.specified=true");

        // Get all the transactionList where systemFeeAmount is null
        defaultTransactionShouldNotBeFound("systemFeeAmount.specified=false");
    }

    @Test
    @Transactional
    void getAllTransactionsBySystemFeeAmountIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where systemFeeAmount is greater than or equal to DEFAULT_SYSTEM_FEE_AMOUNT
        defaultTransactionShouldBeFound("systemFeeAmount.greaterThanOrEqual=" + DEFAULT_SYSTEM_FEE_AMOUNT);

        // Get all the transactionList where systemFeeAmount is greater than or equal to UPDATED_SYSTEM_FEE_AMOUNT
        defaultTransactionShouldNotBeFound("systemFeeAmount.greaterThanOrEqual=" + UPDATED_SYSTEM_FEE_AMOUNT);
    }

    @Test
    @Transactional
    void getAllTransactionsBySystemFeeAmountIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where systemFeeAmount is less than or equal to DEFAULT_SYSTEM_FEE_AMOUNT
        defaultTransactionShouldBeFound("systemFeeAmount.lessThanOrEqual=" + DEFAULT_SYSTEM_FEE_AMOUNT);

        // Get all the transactionList where systemFeeAmount is less than or equal to SMALLER_SYSTEM_FEE_AMOUNT
        defaultTransactionShouldNotBeFound("systemFeeAmount.lessThanOrEqual=" + SMALLER_SYSTEM_FEE_AMOUNT);
    }

    @Test
    @Transactional
    void getAllTransactionsBySystemFeeAmountIsLessThanSomething() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where systemFeeAmount is less than DEFAULT_SYSTEM_FEE_AMOUNT
        defaultTransactionShouldNotBeFound("systemFeeAmount.lessThan=" + DEFAULT_SYSTEM_FEE_AMOUNT);

        // Get all the transactionList where systemFeeAmount is less than UPDATED_SYSTEM_FEE_AMOUNT
        defaultTransactionShouldBeFound("systemFeeAmount.lessThan=" + UPDATED_SYSTEM_FEE_AMOUNT);
    }

    @Test
    @Transactional
    void getAllTransactionsBySystemFeeAmountIsGreaterThanSomething() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where systemFeeAmount is greater than DEFAULT_SYSTEM_FEE_AMOUNT
        defaultTransactionShouldNotBeFound("systemFeeAmount.greaterThan=" + DEFAULT_SYSTEM_FEE_AMOUNT);

        // Get all the transactionList where systemFeeAmount is greater than SMALLER_SYSTEM_FEE_AMOUNT
        defaultTransactionShouldBeFound("systemFeeAmount.greaterThan=" + SMALLER_SYSTEM_FEE_AMOUNT);
    }

    @Test
    @Transactional
    void getAllTransactionsByAgentFeeAmountIsEqualToSomething() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where agentFeeAmount equals to DEFAULT_AGENT_FEE_AMOUNT
        defaultTransactionShouldBeFound("agentFeeAmount.equals=" + DEFAULT_AGENT_FEE_AMOUNT);

        // Get all the transactionList where agentFeeAmount equals to UPDATED_AGENT_FEE_AMOUNT
        defaultTransactionShouldNotBeFound("agentFeeAmount.equals=" + UPDATED_AGENT_FEE_AMOUNT);
    }

    @Test
    @Transactional
    void getAllTransactionsByAgentFeeAmountIsNotEqualToSomething() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where agentFeeAmount not equals to DEFAULT_AGENT_FEE_AMOUNT
        defaultTransactionShouldNotBeFound("agentFeeAmount.notEquals=" + DEFAULT_AGENT_FEE_AMOUNT);

        // Get all the transactionList where agentFeeAmount not equals to UPDATED_AGENT_FEE_AMOUNT
        defaultTransactionShouldBeFound("agentFeeAmount.notEquals=" + UPDATED_AGENT_FEE_AMOUNT);
    }

    @Test
    @Transactional
    void getAllTransactionsByAgentFeeAmountIsInShouldWork() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where agentFeeAmount in DEFAULT_AGENT_FEE_AMOUNT or UPDATED_AGENT_FEE_AMOUNT
        defaultTransactionShouldBeFound("agentFeeAmount.in=" + DEFAULT_AGENT_FEE_AMOUNT + "," + UPDATED_AGENT_FEE_AMOUNT);

        // Get all the transactionList where agentFeeAmount equals to UPDATED_AGENT_FEE_AMOUNT
        defaultTransactionShouldNotBeFound("agentFeeAmount.in=" + UPDATED_AGENT_FEE_AMOUNT);
    }

    @Test
    @Transactional
    void getAllTransactionsByAgentFeeAmountIsNullOrNotNull() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where agentFeeAmount is not null
        defaultTransactionShouldBeFound("agentFeeAmount.specified=true");

        // Get all the transactionList where agentFeeAmount is null
        defaultTransactionShouldNotBeFound("agentFeeAmount.specified=false");
    }

    @Test
    @Transactional
    void getAllTransactionsByAgentFeeAmountIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where agentFeeAmount is greater than or equal to DEFAULT_AGENT_FEE_AMOUNT
        defaultTransactionShouldBeFound("agentFeeAmount.greaterThanOrEqual=" + DEFAULT_AGENT_FEE_AMOUNT);

        // Get all the transactionList where agentFeeAmount is greater than or equal to UPDATED_AGENT_FEE_AMOUNT
        defaultTransactionShouldNotBeFound("agentFeeAmount.greaterThanOrEqual=" + UPDATED_AGENT_FEE_AMOUNT);
    }

    @Test
    @Transactional
    void getAllTransactionsByAgentFeeAmountIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where agentFeeAmount is less than or equal to DEFAULT_AGENT_FEE_AMOUNT
        defaultTransactionShouldBeFound("agentFeeAmount.lessThanOrEqual=" + DEFAULT_AGENT_FEE_AMOUNT);

        // Get all the transactionList where agentFeeAmount is less than or equal to SMALLER_AGENT_FEE_AMOUNT
        defaultTransactionShouldNotBeFound("agentFeeAmount.lessThanOrEqual=" + SMALLER_AGENT_FEE_AMOUNT);
    }

    @Test
    @Transactional
    void getAllTransactionsByAgentFeeAmountIsLessThanSomething() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where agentFeeAmount is less than DEFAULT_AGENT_FEE_AMOUNT
        defaultTransactionShouldNotBeFound("agentFeeAmount.lessThan=" + DEFAULT_AGENT_FEE_AMOUNT);

        // Get all the transactionList where agentFeeAmount is less than UPDATED_AGENT_FEE_AMOUNT
        defaultTransactionShouldBeFound("agentFeeAmount.lessThan=" + UPDATED_AGENT_FEE_AMOUNT);
    }

    @Test
    @Transactional
    void getAllTransactionsByAgentFeeAmountIsGreaterThanSomething() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where agentFeeAmount is greater than DEFAULT_AGENT_FEE_AMOUNT
        defaultTransactionShouldNotBeFound("agentFeeAmount.greaterThan=" + DEFAULT_AGENT_FEE_AMOUNT);

        // Get all the transactionList where agentFeeAmount is greater than SMALLER_AGENT_FEE_AMOUNT
        defaultTransactionShouldBeFound("agentFeeAmount.greaterThan=" + SMALLER_AGENT_FEE_AMOUNT);
    }

    @Test
    @Transactional
    void getAllTransactionsByRoundAmountIsEqualToSomething() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where roundAmount equals to DEFAULT_ROUND_AMOUNT
        defaultTransactionShouldBeFound("roundAmount.equals=" + DEFAULT_ROUND_AMOUNT);

        // Get all the transactionList where roundAmount equals to UPDATED_ROUND_AMOUNT
        defaultTransactionShouldNotBeFound("roundAmount.equals=" + UPDATED_ROUND_AMOUNT);
    }

    @Test
    @Transactional
    void getAllTransactionsByRoundAmountIsNotEqualToSomething() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where roundAmount not equals to DEFAULT_ROUND_AMOUNT
        defaultTransactionShouldNotBeFound("roundAmount.notEquals=" + DEFAULT_ROUND_AMOUNT);

        // Get all the transactionList where roundAmount not equals to UPDATED_ROUND_AMOUNT
        defaultTransactionShouldBeFound("roundAmount.notEquals=" + UPDATED_ROUND_AMOUNT);
    }

    @Test
    @Transactional
    void getAllTransactionsByRoundAmountIsInShouldWork() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where roundAmount in DEFAULT_ROUND_AMOUNT or UPDATED_ROUND_AMOUNT
        defaultTransactionShouldBeFound("roundAmount.in=" + DEFAULT_ROUND_AMOUNT + "," + UPDATED_ROUND_AMOUNT);

        // Get all the transactionList where roundAmount equals to UPDATED_ROUND_AMOUNT
        defaultTransactionShouldNotBeFound("roundAmount.in=" + UPDATED_ROUND_AMOUNT);
    }

    @Test
    @Transactional
    void getAllTransactionsByRoundAmountIsNullOrNotNull() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where roundAmount is not null
        defaultTransactionShouldBeFound("roundAmount.specified=true");

        // Get all the transactionList where roundAmount is null
        defaultTransactionShouldNotBeFound("roundAmount.specified=false");
    }

    @Test
    @Transactional
    void getAllTransactionsByRoundAmountIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where roundAmount is greater than or equal to DEFAULT_ROUND_AMOUNT
        defaultTransactionShouldBeFound("roundAmount.greaterThanOrEqual=" + DEFAULT_ROUND_AMOUNT);

        // Get all the transactionList where roundAmount is greater than or equal to UPDATED_ROUND_AMOUNT
        defaultTransactionShouldNotBeFound("roundAmount.greaterThanOrEqual=" + UPDATED_ROUND_AMOUNT);
    }

    @Test
    @Transactional
    void getAllTransactionsByRoundAmountIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where roundAmount is less than or equal to DEFAULT_ROUND_AMOUNT
        defaultTransactionShouldBeFound("roundAmount.lessThanOrEqual=" + DEFAULT_ROUND_AMOUNT);

        // Get all the transactionList where roundAmount is less than or equal to SMALLER_ROUND_AMOUNT
        defaultTransactionShouldNotBeFound("roundAmount.lessThanOrEqual=" + SMALLER_ROUND_AMOUNT);
    }

    @Test
    @Transactional
    void getAllTransactionsByRoundAmountIsLessThanSomething() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where roundAmount is less than DEFAULT_ROUND_AMOUNT
        defaultTransactionShouldNotBeFound("roundAmount.lessThan=" + DEFAULT_ROUND_AMOUNT);

        // Get all the transactionList where roundAmount is less than UPDATED_ROUND_AMOUNT
        defaultTransactionShouldBeFound("roundAmount.lessThan=" + UPDATED_ROUND_AMOUNT);
    }

    @Test
    @Transactional
    void getAllTransactionsByRoundAmountIsGreaterThanSomething() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where roundAmount is greater than DEFAULT_ROUND_AMOUNT
        defaultTransactionShouldNotBeFound("roundAmount.greaterThan=" + DEFAULT_ROUND_AMOUNT);

        // Get all the transactionList where roundAmount is greater than SMALLER_ROUND_AMOUNT
        defaultTransactionShouldBeFound("roundAmount.greaterThan=" + SMALLER_ROUND_AMOUNT);
    }

    @Test
    @Transactional
    void getAllTransactionsBySystemFeeIsEqualToSomething() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where systemFee equals to DEFAULT_SYSTEM_FEE
        defaultTransactionShouldBeFound("systemFee.equals=" + DEFAULT_SYSTEM_FEE);

        // Get all the transactionList where systemFee equals to UPDATED_SYSTEM_FEE
        defaultTransactionShouldNotBeFound("systemFee.equals=" + UPDATED_SYSTEM_FEE);
    }

    @Test
    @Transactional
    void getAllTransactionsBySystemFeeIsNotEqualToSomething() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where systemFee not equals to DEFAULT_SYSTEM_FEE
        defaultTransactionShouldNotBeFound("systemFee.notEquals=" + DEFAULT_SYSTEM_FEE);

        // Get all the transactionList where systemFee not equals to UPDATED_SYSTEM_FEE
        defaultTransactionShouldBeFound("systemFee.notEquals=" + UPDATED_SYSTEM_FEE);
    }

    @Test
    @Transactional
    void getAllTransactionsBySystemFeeIsInShouldWork() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where systemFee in DEFAULT_SYSTEM_FEE or UPDATED_SYSTEM_FEE
        defaultTransactionShouldBeFound("systemFee.in=" + DEFAULT_SYSTEM_FEE + "," + UPDATED_SYSTEM_FEE);

        // Get all the transactionList where systemFee equals to UPDATED_SYSTEM_FEE
        defaultTransactionShouldNotBeFound("systemFee.in=" + UPDATED_SYSTEM_FEE);
    }

    @Test
    @Transactional
    void getAllTransactionsBySystemFeeIsNullOrNotNull() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where systemFee is not null
        defaultTransactionShouldBeFound("systemFee.specified=true");

        // Get all the transactionList where systemFee is null
        defaultTransactionShouldNotBeFound("systemFee.specified=false");
    }

    @Test
    @Transactional
    void getAllTransactionsBySystemFeeIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where systemFee is greater than or equal to DEFAULT_SYSTEM_FEE
        defaultTransactionShouldBeFound("systemFee.greaterThanOrEqual=" + DEFAULT_SYSTEM_FEE);

        // Get all the transactionList where systemFee is greater than or equal to (DEFAULT_SYSTEM_FEE + 1)
        defaultTransactionShouldNotBeFound("systemFee.greaterThanOrEqual=" + (DEFAULT_SYSTEM_FEE + 1));
    }

    @Test
    @Transactional
    void getAllTransactionsBySystemFeeIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where systemFee is less than or equal to DEFAULT_SYSTEM_FEE
        defaultTransactionShouldBeFound("systemFee.lessThanOrEqual=" + DEFAULT_SYSTEM_FEE);

        // Get all the transactionList where systemFee is less than or equal to SMALLER_SYSTEM_FEE
        defaultTransactionShouldNotBeFound("systemFee.lessThanOrEqual=" + SMALLER_SYSTEM_FEE);
    }

    @Test
    @Transactional
    void getAllTransactionsBySystemFeeIsLessThanSomething() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where systemFee is less than DEFAULT_SYSTEM_FEE
        defaultTransactionShouldNotBeFound("systemFee.lessThan=" + DEFAULT_SYSTEM_FEE);

        // Get all the transactionList where systemFee is less than (DEFAULT_SYSTEM_FEE + 1)
        defaultTransactionShouldBeFound("systemFee.lessThan=" + (DEFAULT_SYSTEM_FEE + 1));
    }

    @Test
    @Transactional
    void getAllTransactionsBySystemFeeIsGreaterThanSomething() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where systemFee is greater than DEFAULT_SYSTEM_FEE
        defaultTransactionShouldNotBeFound("systemFee.greaterThan=" + DEFAULT_SYSTEM_FEE);

        // Get all the transactionList where systemFee is greater than SMALLER_SYSTEM_FEE
        defaultTransactionShouldBeFound("systemFee.greaterThan=" + SMALLER_SYSTEM_FEE);
    }

    @Test
    @Transactional
    void getAllTransactionsBySystemCurrencyIsEqualToSomething() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where systemCurrency equals to DEFAULT_SYSTEM_CURRENCY
        defaultTransactionShouldBeFound("systemCurrency.equals=" + DEFAULT_SYSTEM_CURRENCY);

        // Get all the transactionList where systemCurrency equals to UPDATED_SYSTEM_CURRENCY
        defaultTransactionShouldNotBeFound("systemCurrency.equals=" + UPDATED_SYSTEM_CURRENCY);
    }

    @Test
    @Transactional
    void getAllTransactionsBySystemCurrencyIsNotEqualToSomething() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where systemCurrency not equals to DEFAULT_SYSTEM_CURRENCY
        defaultTransactionShouldNotBeFound("systemCurrency.notEquals=" + DEFAULT_SYSTEM_CURRENCY);

        // Get all the transactionList where systemCurrency not equals to UPDATED_SYSTEM_CURRENCY
        defaultTransactionShouldBeFound("systemCurrency.notEquals=" + UPDATED_SYSTEM_CURRENCY);
    }

    @Test
    @Transactional
    void getAllTransactionsBySystemCurrencyIsInShouldWork() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where systemCurrency in DEFAULT_SYSTEM_CURRENCY or UPDATED_SYSTEM_CURRENCY
        defaultTransactionShouldBeFound("systemCurrency.in=" + DEFAULT_SYSTEM_CURRENCY + "," + UPDATED_SYSTEM_CURRENCY);

        // Get all the transactionList where systemCurrency equals to UPDATED_SYSTEM_CURRENCY
        defaultTransactionShouldNotBeFound("systemCurrency.in=" + UPDATED_SYSTEM_CURRENCY);
    }

    @Test
    @Transactional
    void getAllTransactionsBySystemCurrencyIsNullOrNotNull() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where systemCurrency is not null
        defaultTransactionShouldBeFound("systemCurrency.specified=true");

        // Get all the transactionList where systemCurrency is null
        defaultTransactionShouldNotBeFound("systemCurrency.specified=false");
    }

    @Test
    @Transactional
    void getAllTransactionsBySystemCurrencyIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where systemCurrency is greater than or equal to DEFAULT_SYSTEM_CURRENCY
        defaultTransactionShouldBeFound("systemCurrency.greaterThanOrEqual=" + DEFAULT_SYSTEM_CURRENCY);

        // Get all the transactionList where systemCurrency is greater than or equal to UPDATED_SYSTEM_CURRENCY
        defaultTransactionShouldNotBeFound("systemCurrency.greaterThanOrEqual=" + UPDATED_SYSTEM_CURRENCY);
    }

    @Test
    @Transactional
    void getAllTransactionsBySystemCurrencyIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where systemCurrency is less than or equal to DEFAULT_SYSTEM_CURRENCY
        defaultTransactionShouldBeFound("systemCurrency.lessThanOrEqual=" + DEFAULT_SYSTEM_CURRENCY);

        // Get all the transactionList where systemCurrency is less than or equal to SMALLER_SYSTEM_CURRENCY
        defaultTransactionShouldNotBeFound("systemCurrency.lessThanOrEqual=" + SMALLER_SYSTEM_CURRENCY);
    }

    @Test
    @Transactional
    void getAllTransactionsBySystemCurrencyIsLessThanSomething() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where systemCurrency is less than DEFAULT_SYSTEM_CURRENCY
        defaultTransactionShouldNotBeFound("systemCurrency.lessThan=" + DEFAULT_SYSTEM_CURRENCY);

        // Get all the transactionList where systemCurrency is less than UPDATED_SYSTEM_CURRENCY
        defaultTransactionShouldBeFound("systemCurrency.lessThan=" + UPDATED_SYSTEM_CURRENCY);
    }

    @Test
    @Transactional
    void getAllTransactionsBySystemCurrencyIsGreaterThanSomething() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where systemCurrency is greater than DEFAULT_SYSTEM_CURRENCY
        defaultTransactionShouldNotBeFound("systemCurrency.greaterThan=" + DEFAULT_SYSTEM_CURRENCY);

        // Get all the transactionList where systemCurrency is greater than SMALLER_SYSTEM_CURRENCY
        defaultTransactionShouldBeFound("systemCurrency.greaterThan=" + SMALLER_SYSTEM_CURRENCY);
    }

    @Test
    @Transactional
    void getAllTransactionsByCcyRateIsEqualToSomething() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where ccyRate equals to DEFAULT_CCY_RATE
        defaultTransactionShouldBeFound("ccyRate.equals=" + DEFAULT_CCY_RATE);

        // Get all the transactionList where ccyRate equals to UPDATED_CCY_RATE
        defaultTransactionShouldNotBeFound("ccyRate.equals=" + UPDATED_CCY_RATE);
    }

    @Test
    @Transactional
    void getAllTransactionsByCcyRateIsNotEqualToSomething() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where ccyRate not equals to DEFAULT_CCY_RATE
        defaultTransactionShouldNotBeFound("ccyRate.notEquals=" + DEFAULT_CCY_RATE);

        // Get all the transactionList where ccyRate not equals to UPDATED_CCY_RATE
        defaultTransactionShouldBeFound("ccyRate.notEquals=" + UPDATED_CCY_RATE);
    }

    @Test
    @Transactional
    void getAllTransactionsByCcyRateIsInShouldWork() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where ccyRate in DEFAULT_CCY_RATE or UPDATED_CCY_RATE
        defaultTransactionShouldBeFound("ccyRate.in=" + DEFAULT_CCY_RATE + "," + UPDATED_CCY_RATE);

        // Get all the transactionList where ccyRate equals to UPDATED_CCY_RATE
        defaultTransactionShouldNotBeFound("ccyRate.in=" + UPDATED_CCY_RATE);
    }

    @Test
    @Transactional
    void getAllTransactionsByCcyRateIsNullOrNotNull() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where ccyRate is not null
        defaultTransactionShouldBeFound("ccyRate.specified=true");

        // Get all the transactionList where ccyRate is null
        defaultTransactionShouldNotBeFound("ccyRate.specified=false");
    }

    @Test
    @Transactional
    void getAllTransactionsByCcyRateIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where ccyRate is greater than or equal to DEFAULT_CCY_RATE
        defaultTransactionShouldBeFound("ccyRate.greaterThanOrEqual=" + DEFAULT_CCY_RATE);

        // Get all the transactionList where ccyRate is greater than or equal to UPDATED_CCY_RATE
        defaultTransactionShouldNotBeFound("ccyRate.greaterThanOrEqual=" + UPDATED_CCY_RATE);
    }

    @Test
    @Transactional
    void getAllTransactionsByCcyRateIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where ccyRate is less than or equal to DEFAULT_CCY_RATE
        defaultTransactionShouldBeFound("ccyRate.lessThanOrEqual=" + DEFAULT_CCY_RATE);

        // Get all the transactionList where ccyRate is less than or equal to SMALLER_CCY_RATE
        defaultTransactionShouldNotBeFound("ccyRate.lessThanOrEqual=" + SMALLER_CCY_RATE);
    }

    @Test
    @Transactional
    void getAllTransactionsByCcyRateIsLessThanSomething() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where ccyRate is less than DEFAULT_CCY_RATE
        defaultTransactionShouldNotBeFound("ccyRate.lessThan=" + DEFAULT_CCY_RATE);

        // Get all the transactionList where ccyRate is less than UPDATED_CCY_RATE
        defaultTransactionShouldBeFound("ccyRate.lessThan=" + UPDATED_CCY_RATE);
    }

    @Test
    @Transactional
    void getAllTransactionsByCcyRateIsGreaterThanSomething() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where ccyRate is greater than DEFAULT_CCY_RATE
        defaultTransactionShouldNotBeFound("ccyRate.greaterThan=" + DEFAULT_CCY_RATE);

        // Get all the transactionList where ccyRate is greater than SMALLER_CCY_RATE
        defaultTransactionShouldBeFound("ccyRate.greaterThan=" + SMALLER_CCY_RATE);
    }

    @Test
    @Transactional
    void getAllTransactionsByStatusIsEqualToSomething() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where status equals to DEFAULT_STATUS
        defaultTransactionShouldBeFound("status.equals=" + DEFAULT_STATUS);

        // Get all the transactionList where status equals to UPDATED_STATUS
        defaultTransactionShouldNotBeFound("status.equals=" + UPDATED_STATUS);
    }

    @Test
    @Transactional
    void getAllTransactionsByStatusIsNotEqualToSomething() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where status not equals to DEFAULT_STATUS
        defaultTransactionShouldNotBeFound("status.notEquals=" + DEFAULT_STATUS);

        // Get all the transactionList where status not equals to UPDATED_STATUS
        defaultTransactionShouldBeFound("status.notEquals=" + UPDATED_STATUS);
    }

    @Test
    @Transactional
    void getAllTransactionsByStatusIsInShouldWork() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where status in DEFAULT_STATUS or UPDATED_STATUS
        defaultTransactionShouldBeFound("status.in=" + DEFAULT_STATUS + "," + UPDATED_STATUS);

        // Get all the transactionList where status equals to UPDATED_STATUS
        defaultTransactionShouldNotBeFound("status.in=" + UPDATED_STATUS);
    }

    @Test
    @Transactional
    void getAllTransactionsByStatusIsNullOrNotNull() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where status is not null
        defaultTransactionShouldBeFound("status.specified=true");

        // Get all the transactionList where status is null
        defaultTransactionShouldNotBeFound("status.specified=false");
    }

    @Test
    @Transactional
    void getAllTransactionsByCommentIsEqualToSomething() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where comment equals to DEFAULT_COMMENT
        defaultTransactionShouldBeFound("comment.equals=" + DEFAULT_COMMENT);

        // Get all the transactionList where comment equals to UPDATED_COMMENT
        defaultTransactionShouldNotBeFound("comment.equals=" + UPDATED_COMMENT);
    }

    @Test
    @Transactional
    void getAllTransactionsByCommentIsNotEqualToSomething() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where comment not equals to DEFAULT_COMMENT
        defaultTransactionShouldNotBeFound("comment.notEquals=" + DEFAULT_COMMENT);

        // Get all the transactionList where comment not equals to UPDATED_COMMENT
        defaultTransactionShouldBeFound("comment.notEquals=" + UPDATED_COMMENT);
    }

    @Test
    @Transactional
    void getAllTransactionsByCommentIsInShouldWork() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where comment in DEFAULT_COMMENT or UPDATED_COMMENT
        defaultTransactionShouldBeFound("comment.in=" + DEFAULT_COMMENT + "," + UPDATED_COMMENT);

        // Get all the transactionList where comment equals to UPDATED_COMMENT
        defaultTransactionShouldNotBeFound("comment.in=" + UPDATED_COMMENT);
    }

    @Test
    @Transactional
    void getAllTransactionsByCommentIsNullOrNotNull() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where comment is not null
        defaultTransactionShouldBeFound("comment.specified=true");

        // Get all the transactionList where comment is null
        defaultTransactionShouldNotBeFound("comment.specified=false");
    }

    @Test
    @Transactional
    void getAllTransactionsByCommentContainsSomething() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where comment contains DEFAULT_COMMENT
        defaultTransactionShouldBeFound("comment.contains=" + DEFAULT_COMMENT);

        // Get all the transactionList where comment contains UPDATED_COMMENT
        defaultTransactionShouldNotBeFound("comment.contains=" + UPDATED_COMMENT);
    }

    @Test
    @Transactional
    void getAllTransactionsByCommentNotContainsSomething() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where comment does not contain DEFAULT_COMMENT
        defaultTransactionShouldNotBeFound("comment.doesNotContain=" + DEFAULT_COMMENT);

        // Get all the transactionList where comment does not contain UPDATED_COMMENT
        defaultTransactionShouldBeFound("comment.doesNotContain=" + UPDATED_COMMENT);
    }

    @Test
    @Transactional
    void getAllTransactionsByDateIsEqualToSomething() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where date equals to DEFAULT_DATE
        defaultTransactionShouldBeFound("date.equals=" + DEFAULT_DATE);

        // Get all the transactionList where date equals to UPDATED_DATE
        defaultTransactionShouldNotBeFound("date.equals=" + UPDATED_DATE);
    }

    @Test
    @Transactional
    void getAllTransactionsByDateIsNotEqualToSomething() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where date not equals to DEFAULT_DATE
        defaultTransactionShouldNotBeFound("date.notEquals=" + DEFAULT_DATE);

        // Get all the transactionList where date not equals to UPDATED_DATE
        defaultTransactionShouldBeFound("date.notEquals=" + UPDATED_DATE);
    }

    @Test
    @Transactional
    void getAllTransactionsByDateIsInShouldWork() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where date in DEFAULT_DATE or UPDATED_DATE
        defaultTransactionShouldBeFound("date.in=" + DEFAULT_DATE + "," + UPDATED_DATE);

        // Get all the transactionList where date equals to UPDATED_DATE
        defaultTransactionShouldNotBeFound("date.in=" + UPDATED_DATE);
    }

    @Test
    @Transactional
    void getAllTransactionsByDateIsNullOrNotNull() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where date is not null
        defaultTransactionShouldBeFound("date.specified=true");

        // Get all the transactionList where date is null
        defaultTransactionShouldNotBeFound("date.specified=false");
    }

    @Test
    @Transactional
    void getAllTransactionsByPayTypeIsEqualToSomething() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where payType equals to DEFAULT_PAY_TYPE
        defaultTransactionShouldBeFound("payType.equals=" + DEFAULT_PAY_TYPE);

        // Get all the transactionList where payType equals to UPDATED_PAY_TYPE
        defaultTransactionShouldNotBeFound("payType.equals=" + UPDATED_PAY_TYPE);
    }

    @Test
    @Transactional
    void getAllTransactionsByPayTypeIsNotEqualToSomething() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where payType not equals to DEFAULT_PAY_TYPE
        defaultTransactionShouldNotBeFound("payType.notEquals=" + DEFAULT_PAY_TYPE);

        // Get all the transactionList where payType not equals to UPDATED_PAY_TYPE
        defaultTransactionShouldBeFound("payType.notEquals=" + UPDATED_PAY_TYPE);
    }

    @Test
    @Transactional
    void getAllTransactionsByPayTypeIsInShouldWork() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where payType in DEFAULT_PAY_TYPE or UPDATED_PAY_TYPE
        defaultTransactionShouldBeFound("payType.in=" + DEFAULT_PAY_TYPE + "," + UPDATED_PAY_TYPE);

        // Get all the transactionList where payType equals to UPDATED_PAY_TYPE
        defaultTransactionShouldNotBeFound("payType.in=" + UPDATED_PAY_TYPE);
    }

    @Test
    @Transactional
    void getAllTransactionsByPayTypeIsNullOrNotNull() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where payType is not null
        defaultTransactionShouldBeFound("payType.specified=true");

        // Get all the transactionList where payType is null
        defaultTransactionShouldNotBeFound("payType.specified=false");
    }

    @Test
    @Transactional
    void getAllTransactionsByRequestSubAccountIsEqualToSomething() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where requestSubAccount equals to DEFAULT_REQUEST_SUB_ACCOUNT
        defaultTransactionShouldBeFound("requestSubAccount.equals=" + DEFAULT_REQUEST_SUB_ACCOUNT);

        // Get all the transactionList where requestSubAccount equals to UPDATED_REQUEST_SUB_ACCOUNT
        defaultTransactionShouldNotBeFound("requestSubAccount.equals=" + UPDATED_REQUEST_SUB_ACCOUNT);
    }

    @Test
    @Transactional
    void getAllTransactionsByRequestSubAccountIsNotEqualToSomething() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where requestSubAccount not equals to DEFAULT_REQUEST_SUB_ACCOUNT
        defaultTransactionShouldNotBeFound("requestSubAccount.notEquals=" + DEFAULT_REQUEST_SUB_ACCOUNT);

        // Get all the transactionList where requestSubAccount not equals to UPDATED_REQUEST_SUB_ACCOUNT
        defaultTransactionShouldBeFound("requestSubAccount.notEquals=" + UPDATED_REQUEST_SUB_ACCOUNT);
    }

    @Test
    @Transactional
    void getAllTransactionsByRequestSubAccountIsInShouldWork() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where requestSubAccount in DEFAULT_REQUEST_SUB_ACCOUNT or UPDATED_REQUEST_SUB_ACCOUNT
        defaultTransactionShouldBeFound("requestSubAccount.in=" + DEFAULT_REQUEST_SUB_ACCOUNT + "," + UPDATED_REQUEST_SUB_ACCOUNT);

        // Get all the transactionList where requestSubAccount equals to UPDATED_REQUEST_SUB_ACCOUNT
        defaultTransactionShouldNotBeFound("requestSubAccount.in=" + UPDATED_REQUEST_SUB_ACCOUNT);
    }

    @Test
    @Transactional
    void getAllTransactionsByRequestSubAccountIsNullOrNotNull() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where requestSubAccount is not null
        defaultTransactionShouldBeFound("requestSubAccount.specified=true");

        // Get all the transactionList where requestSubAccount is null
        defaultTransactionShouldNotBeFound("requestSubAccount.specified=false");
    }

    @Test
    @Transactional
    void getAllTransactionsByRequestSubAccountContainsSomething() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where requestSubAccount contains DEFAULT_REQUEST_SUB_ACCOUNT
        defaultTransactionShouldBeFound("requestSubAccount.contains=" + DEFAULT_REQUEST_SUB_ACCOUNT);

        // Get all the transactionList where requestSubAccount contains UPDATED_REQUEST_SUB_ACCOUNT
        defaultTransactionShouldNotBeFound("requestSubAccount.contains=" + UPDATED_REQUEST_SUB_ACCOUNT);
    }

    @Test
    @Transactional
    void getAllTransactionsByRequestSubAccountNotContainsSomething() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where requestSubAccount does not contain DEFAULT_REQUEST_SUB_ACCOUNT
        defaultTransactionShouldNotBeFound("requestSubAccount.doesNotContain=" + DEFAULT_REQUEST_SUB_ACCOUNT);

        // Get all the transactionList where requestSubAccount does not contain UPDATED_REQUEST_SUB_ACCOUNT
        defaultTransactionShouldBeFound("requestSubAccount.doesNotContain=" + UPDATED_REQUEST_SUB_ACCOUNT);
    }

    @Test
    @Transactional
    void getAllTransactionsByEposMerchantIsEqualToSomething() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where eposMerchant equals to DEFAULT_EPOS_MERCHANT
        defaultTransactionShouldBeFound("eposMerchant.equals=" + DEFAULT_EPOS_MERCHANT);

        // Get all the transactionList where eposMerchant equals to UPDATED_EPOS_MERCHANT
        defaultTransactionShouldNotBeFound("eposMerchant.equals=" + UPDATED_EPOS_MERCHANT);
    }

    @Test
    @Transactional
    void getAllTransactionsByEposMerchantIsNotEqualToSomething() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where eposMerchant not equals to DEFAULT_EPOS_MERCHANT
        defaultTransactionShouldNotBeFound("eposMerchant.notEquals=" + DEFAULT_EPOS_MERCHANT);

        // Get all the transactionList where eposMerchant not equals to UPDATED_EPOS_MERCHANT
        defaultTransactionShouldBeFound("eposMerchant.notEquals=" + UPDATED_EPOS_MERCHANT);
    }

    @Test
    @Transactional
    void getAllTransactionsByEposMerchantIsInShouldWork() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where eposMerchant in DEFAULT_EPOS_MERCHANT or UPDATED_EPOS_MERCHANT
        defaultTransactionShouldBeFound("eposMerchant.in=" + DEFAULT_EPOS_MERCHANT + "," + UPDATED_EPOS_MERCHANT);

        // Get all the transactionList where eposMerchant equals to UPDATED_EPOS_MERCHANT
        defaultTransactionShouldNotBeFound("eposMerchant.in=" + UPDATED_EPOS_MERCHANT);
    }

    @Test
    @Transactional
    void getAllTransactionsByEposMerchantIsNullOrNotNull() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where eposMerchant is not null
        defaultTransactionShouldBeFound("eposMerchant.specified=true");

        // Get all the transactionList where eposMerchant is null
        defaultTransactionShouldNotBeFound("eposMerchant.specified=false");
    }

    @Test
    @Transactional
    void getAllTransactionsByEposMerchantContainsSomething() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where eposMerchant contains DEFAULT_EPOS_MERCHANT
        defaultTransactionShouldBeFound("eposMerchant.contains=" + DEFAULT_EPOS_MERCHANT);

        // Get all the transactionList where eposMerchant contains UPDATED_EPOS_MERCHANT
        defaultTransactionShouldNotBeFound("eposMerchant.contains=" + UPDATED_EPOS_MERCHANT);
    }

    @Test
    @Transactional
    void getAllTransactionsByEposMerchantNotContainsSomething() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where eposMerchant does not contain DEFAULT_EPOS_MERCHANT
        defaultTransactionShouldNotBeFound("eposMerchant.doesNotContain=" + DEFAULT_EPOS_MERCHANT);

        // Get all the transactionList where eposMerchant does not contain UPDATED_EPOS_MERCHANT
        defaultTransactionShouldBeFound("eposMerchant.doesNotContain=" + UPDATED_EPOS_MERCHANT);
    }

    @Test
    @Transactional
    void getAllTransactionsByEposTerminalIsEqualToSomething() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where eposTerminal equals to DEFAULT_EPOS_TERMINAL
        defaultTransactionShouldBeFound("eposTerminal.equals=" + DEFAULT_EPOS_TERMINAL);

        // Get all the transactionList where eposTerminal equals to UPDATED_EPOS_TERMINAL
        defaultTransactionShouldNotBeFound("eposTerminal.equals=" + UPDATED_EPOS_TERMINAL);
    }

    @Test
    @Transactional
    void getAllTransactionsByEposTerminalIsNotEqualToSomething() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where eposTerminal not equals to DEFAULT_EPOS_TERMINAL
        defaultTransactionShouldNotBeFound("eposTerminal.notEquals=" + DEFAULT_EPOS_TERMINAL);

        // Get all the transactionList where eposTerminal not equals to UPDATED_EPOS_TERMINAL
        defaultTransactionShouldBeFound("eposTerminal.notEquals=" + UPDATED_EPOS_TERMINAL);
    }

    @Test
    @Transactional
    void getAllTransactionsByEposTerminalIsInShouldWork() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where eposTerminal in DEFAULT_EPOS_TERMINAL or UPDATED_EPOS_TERMINAL
        defaultTransactionShouldBeFound("eposTerminal.in=" + DEFAULT_EPOS_TERMINAL + "," + UPDATED_EPOS_TERMINAL);

        // Get all the transactionList where eposTerminal equals to UPDATED_EPOS_TERMINAL
        defaultTransactionShouldNotBeFound("eposTerminal.in=" + UPDATED_EPOS_TERMINAL);
    }

    @Test
    @Transactional
    void getAllTransactionsByEposTerminalIsNullOrNotNull() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where eposTerminal is not null
        defaultTransactionShouldBeFound("eposTerminal.specified=true");

        // Get all the transactionList where eposTerminal is null
        defaultTransactionShouldNotBeFound("eposTerminal.specified=false");
    }

    @Test
    @Transactional
    void getAllTransactionsByEposTerminalContainsSomething() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where eposTerminal contains DEFAULT_EPOS_TERMINAL
        defaultTransactionShouldBeFound("eposTerminal.contains=" + DEFAULT_EPOS_TERMINAL);

        // Get all the transactionList where eposTerminal contains UPDATED_EPOS_TERMINAL
        defaultTransactionShouldNotBeFound("eposTerminal.contains=" + UPDATED_EPOS_TERMINAL);
    }

    @Test
    @Transactional
    void getAllTransactionsByEposTerminalNotContainsSomething() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where eposTerminal does not contain DEFAULT_EPOS_TERMINAL
        defaultTransactionShouldNotBeFound("eposTerminal.doesNotContain=" + DEFAULT_EPOS_TERMINAL);

        // Get all the transactionList where eposTerminal does not contain UPDATED_EPOS_TERMINAL
        defaultTransactionShouldBeFound("eposTerminal.doesNotContain=" + UPDATED_EPOS_TERMINAL);
    }

    @Test
    @Transactional
    void getAllTransactionsByMainIsEqualToSomething() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where main equals to DEFAULT_MAIN
        defaultTransactionShouldBeFound("main.equals=" + DEFAULT_MAIN);

        // Get all the transactionList where main equals to UPDATED_MAIN
        defaultTransactionShouldNotBeFound("main.equals=" + UPDATED_MAIN);
    }

    @Test
    @Transactional
    void getAllTransactionsByMainIsNotEqualToSomething() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where main not equals to DEFAULT_MAIN
        defaultTransactionShouldNotBeFound("main.notEquals=" + DEFAULT_MAIN);

        // Get all the transactionList where main not equals to UPDATED_MAIN
        defaultTransactionShouldBeFound("main.notEquals=" + UPDATED_MAIN);
    }

    @Test
    @Transactional
    void getAllTransactionsByMainIsInShouldWork() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where main in DEFAULT_MAIN or UPDATED_MAIN
        defaultTransactionShouldBeFound("main.in=" + DEFAULT_MAIN + "," + UPDATED_MAIN);

        // Get all the transactionList where main equals to UPDATED_MAIN
        defaultTransactionShouldNotBeFound("main.in=" + UPDATED_MAIN);
    }

    @Test
    @Transactional
    void getAllTransactionsByMainIsNullOrNotNull() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where main is not null
        defaultTransactionShouldBeFound("main.specified=true");

        // Get all the transactionList where main is null
        defaultTransactionShouldNotBeFound("main.specified=false");
    }

    @Test
    @Transactional
    void getAllTransactionsByAmountRatioIsEqualToSomething() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where amountRatio equals to DEFAULT_AMOUNT_RATIO
        defaultTransactionShouldBeFound("amountRatio.equals=" + DEFAULT_AMOUNT_RATIO);

        // Get all the transactionList where amountRatio equals to UPDATED_AMOUNT_RATIO
        defaultTransactionShouldNotBeFound("amountRatio.equals=" + UPDATED_AMOUNT_RATIO);
    }

    @Test
    @Transactional
    void getAllTransactionsByAmountRatioIsNotEqualToSomething() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where amountRatio not equals to DEFAULT_AMOUNT_RATIO
        defaultTransactionShouldNotBeFound("amountRatio.notEquals=" + DEFAULT_AMOUNT_RATIO);

        // Get all the transactionList where amountRatio not equals to UPDATED_AMOUNT_RATIO
        defaultTransactionShouldBeFound("amountRatio.notEquals=" + UPDATED_AMOUNT_RATIO);
    }

    @Test
    @Transactional
    void getAllTransactionsByAmountRatioIsInShouldWork() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where amountRatio in DEFAULT_AMOUNT_RATIO or UPDATED_AMOUNT_RATIO
        defaultTransactionShouldBeFound("amountRatio.in=" + DEFAULT_AMOUNT_RATIO + "," + UPDATED_AMOUNT_RATIO);

        // Get all the transactionList where amountRatio equals to UPDATED_AMOUNT_RATIO
        defaultTransactionShouldNotBeFound("amountRatio.in=" + UPDATED_AMOUNT_RATIO);
    }

    @Test
    @Transactional
    void getAllTransactionsByAmountRatioIsNullOrNotNull() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where amountRatio is not null
        defaultTransactionShouldBeFound("amountRatio.specified=true");

        // Get all the transactionList where amountRatio is null
        defaultTransactionShouldNotBeFound("amountRatio.specified=false");
    }

    @Test
    @Transactional
    void getAllTransactionsByAmountRatioIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where amountRatio is greater than or equal to DEFAULT_AMOUNT_RATIO
        defaultTransactionShouldBeFound("amountRatio.greaterThanOrEqual=" + DEFAULT_AMOUNT_RATIO);

        // Get all the transactionList where amountRatio is greater than or equal to UPDATED_AMOUNT_RATIO
        defaultTransactionShouldNotBeFound("amountRatio.greaterThanOrEqual=" + UPDATED_AMOUNT_RATIO);
    }

    @Test
    @Transactional
    void getAllTransactionsByAmountRatioIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where amountRatio is less than or equal to DEFAULT_AMOUNT_RATIO
        defaultTransactionShouldBeFound("amountRatio.lessThanOrEqual=" + DEFAULT_AMOUNT_RATIO);

        // Get all the transactionList where amountRatio is less than or equal to SMALLER_AMOUNT_RATIO
        defaultTransactionShouldNotBeFound("amountRatio.lessThanOrEqual=" + SMALLER_AMOUNT_RATIO);
    }

    @Test
    @Transactional
    void getAllTransactionsByAmountRatioIsLessThanSomething() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where amountRatio is less than DEFAULT_AMOUNT_RATIO
        defaultTransactionShouldNotBeFound("amountRatio.lessThan=" + DEFAULT_AMOUNT_RATIO);

        // Get all the transactionList where amountRatio is less than UPDATED_AMOUNT_RATIO
        defaultTransactionShouldBeFound("amountRatio.lessThan=" + UPDATED_AMOUNT_RATIO);
    }

    @Test
    @Transactional
    void getAllTransactionsByAmountRatioIsGreaterThanSomething() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where amountRatio is greater than DEFAULT_AMOUNT_RATIO
        defaultTransactionShouldNotBeFound("amountRatio.greaterThan=" + DEFAULT_AMOUNT_RATIO);

        // Get all the transactionList where amountRatio is greater than SMALLER_AMOUNT_RATIO
        defaultTransactionShouldBeFound("amountRatio.greaterThan=" + SMALLER_AMOUNT_RATIO);
    }

    @Test
    @Transactional
    void getAllTransactionsBySenderBeanIsEqualToSomething() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where senderBean equals to DEFAULT_SENDER_BEAN
        defaultTransactionShouldBeFound("senderBean.equals=" + DEFAULT_SENDER_BEAN);

        // Get all the transactionList where senderBean equals to UPDATED_SENDER_BEAN
        defaultTransactionShouldNotBeFound("senderBean.equals=" + UPDATED_SENDER_BEAN);
    }

    @Test
    @Transactional
    void getAllTransactionsBySenderBeanIsNotEqualToSomething() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where senderBean not equals to DEFAULT_SENDER_BEAN
        defaultTransactionShouldNotBeFound("senderBean.notEquals=" + DEFAULT_SENDER_BEAN);

        // Get all the transactionList where senderBean not equals to UPDATED_SENDER_BEAN
        defaultTransactionShouldBeFound("senderBean.notEquals=" + UPDATED_SENDER_BEAN);
    }

    @Test
    @Transactional
    void getAllTransactionsBySenderBeanIsInShouldWork() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where senderBean in DEFAULT_SENDER_BEAN or UPDATED_SENDER_BEAN
        defaultTransactionShouldBeFound("senderBean.in=" + DEFAULT_SENDER_BEAN + "," + UPDATED_SENDER_BEAN);

        // Get all the transactionList where senderBean equals to UPDATED_SENDER_BEAN
        defaultTransactionShouldNotBeFound("senderBean.in=" + UPDATED_SENDER_BEAN);
    }

    @Test
    @Transactional
    void getAllTransactionsBySenderBeanIsNullOrNotNull() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where senderBean is not null
        defaultTransactionShouldBeFound("senderBean.specified=true");

        // Get all the transactionList where senderBean is null
        defaultTransactionShouldNotBeFound("senderBean.specified=false");
    }

    @Test
    @Transactional
    void getAllTransactionsBySenderBeanContainsSomething() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where senderBean contains DEFAULT_SENDER_BEAN
        defaultTransactionShouldBeFound("senderBean.contains=" + DEFAULT_SENDER_BEAN);

        // Get all the transactionList where senderBean contains UPDATED_SENDER_BEAN
        defaultTransactionShouldNotBeFound("senderBean.contains=" + UPDATED_SENDER_BEAN);
    }

    @Test
    @Transactional
    void getAllTransactionsBySenderBeanNotContainsSomething() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where senderBean does not contain DEFAULT_SENDER_BEAN
        defaultTransactionShouldNotBeFound("senderBean.doesNotContain=" + DEFAULT_SENDER_BEAN);

        // Get all the transactionList where senderBean does not contain UPDATED_SENDER_BEAN
        defaultTransactionShouldBeFound("senderBean.doesNotContain=" + UPDATED_SENDER_BEAN);
    }

    /**
     * Executes the search, and checks that the default entity is returned.
     */
    private void defaultTransactionShouldBeFound(String filter) throws Exception {
        restTransactionMockMvc
                .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.[*].id").value(hasItem(transaction.getId().intValue())))
                .andExpect(jsonPath("$.[*].requestAmount").value(hasItem(DEFAULT_REQUEST_AMOUNT.intValue())))
                .andExpect(jsonPath("$.[*].requestCcy").value(hasItem(DEFAULT_REQUEST_CCY)))
                .andExpect(jsonPath("$.[*].requestTrId").value(hasItem(DEFAULT_REQUEST_TR_ID)))
                .andExpect(jsonPath("$.[*].requestLogin").value(hasItem(DEFAULT_REQUEST_LOGIN)))
                .andExpect(jsonPath("$.[*].requestGroup").value(hasItem(DEFAULT_REQUEST_GROUP)))
                .andExpect(jsonPath("$.[*].senderName").value(hasItem(DEFAULT_SENDER_NAME)))
                .andExpect(jsonPath("$.[*].senderAccount").value(hasItem(DEFAULT_SENDER_ACCOUNT)))
                .andExpect(jsonPath("$.[*].senderServiceId").value(hasItem(DEFAULT_SENDER_SERVICE_ID.intValue())))
                .andExpect(jsonPath("$.[*].senderAmount").value(hasItem(DEFAULT_SENDER_AMOUNT.intValue())))
                .andExpect(jsonPath("$.[*].senderCcy").value(hasItem(DEFAULT_SENDER_CCY)))
                .andExpect(jsonPath("$.[*].senderTrId").value(hasItem(DEFAULT_SENDER_TR_ID)))
                .andExpect(jsonPath("$.[*].senderSuccess").value(hasItem(DEFAULT_SENDER_SUCCESS.booleanValue())))
                .andExpect(jsonPath("$.[*].senderDate").value(hasItem(DEFAULT_SENDER_DATE.toString())))
                .andExpect(jsonPath("$.[*].senderStatus").value(hasItem(DEFAULT_SENDER_STATUS)))
                .andExpect(jsonPath("$.[*].senderStatusText").value(hasItem(DEFAULT_SENDER_STATUS_TEXT)))
                .andExpect(jsonPath("$.[*].senderBillId").value(hasItem(DEFAULT_SENDER_BILL_ID.intValue())))
                .andExpect(jsonPath("$.[*].senderGateway").value(hasItem(DEFAULT_SENDER_GATEWAY)))
                .andExpect(jsonPath("$.[*].gatewayAmount").value(hasItem(DEFAULT_GATEWAY_AMOUNT.doubleValue())))
                .andExpect(jsonPath("$.[*].recipientName").value(hasItem(DEFAULT_RECIPIENT_NAME)))
                .andExpect(jsonPath("$.[*].recipientAccount").value(hasItem(DEFAULT_RECIPIENT_ACCOUNT)))
                .andExpect(jsonPath("$.[*].recipientServiceId").value(hasItem(DEFAULT_RECIPIENT_SERVICE_ID.intValue())))
                .andExpect(jsonPath("$.[*].recipientAmount").value(hasItem(DEFAULT_RECIPIENT_AMOUNT.intValue())))
                .andExpect(jsonPath("$.[*].recipientCcy").value(hasItem(DEFAULT_RECIPIENT_CCY)))
                .andExpect(jsonPath("$.[*].recipientTrId").value(hasItem(DEFAULT_RECIPIENT_TR_ID)))
                .andExpect(jsonPath("$.[*].recipientSuccess").value(hasItem(DEFAULT_RECIPIENT_SUCCESS.booleanValue())))
                .andExpect(jsonPath("$.[*].recipientDate").value(hasItem(DEFAULT_RECIPIENT_DATE.toString())))
                .andExpect(jsonPath("$.[*].recipientStatus").value(hasItem(DEFAULT_RECIPIENT_STATUS)))
                .andExpect(jsonPath("$.[*].recipientStatusText").value(hasItem(DEFAULT_RECIPIENT_STATUS_TEXT)))
                .andExpect(jsonPath("$.[*].recipientBillId").value(hasItem(DEFAULT_RECIPIENT_BILL_ID.intValue())))
                .andExpect(jsonPath("$.[*].recipientGateway").value(hasItem(DEFAULT_RECIPIENT_GATEWAY)))
                .andExpect(jsonPath("$.[*].systemCommissionUpAmount").value(hasItem(DEFAULT_SYSTEM_COMMISSION_UP_AMOUNT.intValue())))
                .andExpect(jsonPath("$.[*].systemCommissionDownAmount").value(hasItem(DEFAULT_SYSTEM_COMMISSION_DOWN_AMOUNT.intValue())))
                .andExpect(jsonPath("$.[*].systemFeeAmount").value(hasItem(DEFAULT_SYSTEM_FEE_AMOUNT.intValue())))
                .andExpect(jsonPath("$.[*].agentFeeAmount").value(hasItem(DEFAULT_AGENT_FEE_AMOUNT.intValue())))
                .andExpect(jsonPath("$.[*].roundAmount").value(hasItem(DEFAULT_ROUND_AMOUNT.doubleValue())))
                .andExpect(jsonPath("$.[*].systemFee").value(hasItem(DEFAULT_SYSTEM_FEE.doubleValue())))
                .andExpect(jsonPath("$.[*].systemCurrency").value(hasItem(DEFAULT_SYSTEM_CURRENCY)))
                .andExpect(jsonPath("$.[*].ccyRate").value(hasItem(DEFAULT_CCY_RATE)))
                .andExpect(jsonPath("$.[*].status").value(hasItem(DEFAULT_STATUS.toString())))
                .andExpect(jsonPath("$.[*].comment").value(hasItem(DEFAULT_COMMENT)))
                .andExpect(jsonPath("$.[*].date").value(hasItem(DEFAULT_DATE.toString())))
                .andExpect(jsonPath("$.[*].payType").value(hasItem(DEFAULT_PAY_TYPE.toString())))
                .andExpect(jsonPath("$.[*].requestSubAccount").value(hasItem(DEFAULT_REQUEST_SUB_ACCOUNT)))
                .andExpect(jsonPath("$.[*].eposMerchant").value(hasItem(DEFAULT_EPOS_MERCHANT)))
                .andExpect(jsonPath("$.[*].eposTerminal").value(hasItem(DEFAULT_EPOS_TERMINAL)))
                .andExpect(jsonPath("$.[*].main").value(hasItem(DEFAULT_MAIN.booleanValue())))
                .andExpect(jsonPath("$.[*].amountRatio").value(hasItem(DEFAULT_AMOUNT_RATIO)))
                .andExpect(jsonPath("$.[*].senderBean").value(hasItem(DEFAULT_SENDER_BEAN)))
                .andExpect(jsonPath("$.[*].category").value(hasItem(DEFAULT_CATEGORY)));

        // Check, that the count call also returns 1
        restTransactionMockMvc
                .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(content().string("1"));
    }

    /**
     * Executes the search, and checks that the default entity is not returned.
     */
    private void defaultTransactionShouldNotBeFound(String filter) throws Exception {
        restTransactionMockMvc
                .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());

        // Check, that the count call also returns 0
        restTransactionMockMvc
                .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(content().string("0"));
    }

    @Test
    @Transactional
    void getNonExistingTransaction() throws Exception {
        // Get the transaction
        restTransactionMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putNewTransaction() throws Exception {
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
                .senderBean(UPDATED_SENDER_BEAN)
                .category(UPDATED_CATEGORY);
        TransactionDTO transactionDTO = transactionMapper.toDto(updatedTransaction);

        restTransactionMockMvc
                .perform(
                        put(ENTITY_API_URL_ID, transactionDTO.getId())
                                .contentType(MediaType.APPLICATION_JSON)
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
        assertThat(testTransaction.getSenderSuccess()).isEqualTo(UPDATED_SENDER_SUCCESS);
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
        assertThat(testTransaction.getCategory()).isEqualTo(UPDATED_CATEGORY);
    }

    @Test
    @Transactional
    void putNonExistingTransaction() throws Exception {
        int databaseSizeBeforeUpdate = transactionRepository.findAll().size();
        transaction.setId(count.incrementAndGet());

        // Create the Transaction
        TransactionDTO transactionDTO = transactionMapper.toDto(transaction);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restTransactionMockMvc
                .perform(
                        put(ENTITY_API_URL_ID, transactionDTO.getId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(TestUtil.convertObjectToJsonBytes(transactionDTO))
                )
                .andExpect(status().isBadRequest());

        // Validate the Transaction in the database
        List<Transaction> transactionList = transactionRepository.findAll();
        assertThat(transactionList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchTransaction() throws Exception {
        int databaseSizeBeforeUpdate = transactionRepository.findAll().size();
        transaction.setId(count.incrementAndGet());

        // Create the Transaction
        TransactionDTO transactionDTO = transactionMapper.toDto(transaction);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restTransactionMockMvc
                .perform(
                        put(ENTITY_API_URL_ID, count.incrementAndGet())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(TestUtil.convertObjectToJsonBytes(transactionDTO))
                )
                .andExpect(status().isBadRequest());

        // Validate the Transaction in the database
        List<Transaction> transactionList = transactionRepository.findAll();
        assertThat(transactionList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamTransaction() throws Exception {
        int databaseSizeBeforeUpdate = transactionRepository.findAll().size();
        transaction.setId(count.incrementAndGet());

        // Create the Transaction
        TransactionDTO transactionDTO = transactionMapper.toDto(transaction);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restTransactionMockMvc
                .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(transactionDTO)))
                .andExpect(status().isMethodNotAllowed());

        // Validate the Transaction in the database
        List<Transaction> transactionList = transactionRepository.findAll();
        assertThat(transactionList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateTransactionWithPatch() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        int databaseSizeBeforeUpdate = transactionRepository.findAll().size();

        // Update the transaction using partial update
        Transaction partialUpdatedTransaction = new Transaction();
        partialUpdatedTransaction.setId(transaction.getId());

        partialUpdatedTransaction
                .requestCcy(UPDATED_REQUEST_CCY)
                .senderAmount(UPDATED_SENDER_AMOUNT)
                .senderCcy(UPDATED_SENDER_CCY)
                .senderTrId(UPDATED_SENDER_TR_ID)
                .senderSuccess(UPDATED_SENDER_SUCCESS)
                .senderDate(UPDATED_SENDER_DATE)
                .recipientName(UPDATED_RECIPIENT_NAME)
                .recipientAccount(UPDATED_RECIPIENT_ACCOUNT)
                .recipientAmount(UPDATED_RECIPIENT_AMOUNT)
                .recipientTrId(UPDATED_RECIPIENT_TR_ID)
                .recipientDate(UPDATED_RECIPIENT_DATE)
                .recipientGateway(UPDATED_RECIPIENT_GATEWAY)
                .systemCommissionDownAmount(UPDATED_SYSTEM_COMMISSION_DOWN_AMOUNT)
                .roundAmount(UPDATED_ROUND_AMOUNT)
                .systemCurrency(UPDATED_SYSTEM_CURRENCY)
                .status(UPDATED_STATUS)
                .comment(UPDATED_COMMENT)
                .payType(UPDATED_PAY_TYPE)
                .eposMerchant(UPDATED_EPOS_MERCHANT)
                .main(UPDATED_MAIN)
                .category(UPDATED_CATEGORY);

        restTransactionMockMvc
                .perform(
                        patch(ENTITY_API_URL_ID, partialUpdatedTransaction.getId())
                                .contentType("application/merge-patch+json")
                                .content(TestUtil.convertObjectToJsonBytes(partialUpdatedTransaction))
                )
                .andExpect(status().isOk());

        // Validate the Transaction in the database
        List<Transaction> transactionList = transactionRepository.findAll();
        assertThat(transactionList).hasSize(databaseSizeBeforeUpdate);
        Transaction testTransaction = transactionList.get(transactionList.size() - 1);
        assertThat(testTransaction.getRequestAmount()).isEqualTo(DEFAULT_REQUEST_AMOUNT);
        assertThat(testTransaction.getRequestCcy()).isEqualTo(UPDATED_REQUEST_CCY);
        assertThat(testTransaction.getRequestTrId()).isEqualTo(DEFAULT_REQUEST_TR_ID);
        assertThat(testTransaction.getRequestLogin()).isEqualTo(DEFAULT_REQUEST_LOGIN);
        assertThat(testTransaction.getRequestGroup()).isEqualTo(DEFAULT_REQUEST_GROUP);
        assertThat(testTransaction.getSenderName()).isEqualTo(DEFAULT_SENDER_NAME);
        assertThat(testTransaction.getSenderAccount()).isEqualTo(DEFAULT_SENDER_ACCOUNT);
        assertThat(testTransaction.getSenderServiceId()).isEqualTo(DEFAULT_SENDER_SERVICE_ID);
        assertThat(testTransaction.getSenderAmount()).isEqualTo(UPDATED_SENDER_AMOUNT);
        assertThat(testTransaction.getSenderCcy()).isEqualTo(UPDATED_SENDER_CCY);
        assertThat(testTransaction.getSenderTrId()).isEqualTo(UPDATED_SENDER_TR_ID);
        assertThat(testTransaction.getSenderSuccess()).isEqualTo(UPDATED_SENDER_SUCCESS);
        assertThat(testTransaction.getSenderDate()).isEqualTo(UPDATED_SENDER_DATE);
        assertThat(testTransaction.getSenderStatus()).isEqualTo(DEFAULT_SENDER_STATUS);
        assertThat(testTransaction.getSenderStatusText()).isEqualTo(DEFAULT_SENDER_STATUS_TEXT);
        assertThat(testTransaction.getSenderBillId()).isEqualTo(DEFAULT_SENDER_BILL_ID);
        assertThat(testTransaction.getSenderGateway()).isEqualTo(DEFAULT_SENDER_GATEWAY);
        assertThat(testTransaction.getGatewayAmount()).isEqualTo(DEFAULT_GATEWAY_AMOUNT);
        assertThat(testTransaction.getRecipientName()).isEqualTo(UPDATED_RECIPIENT_NAME);
        assertThat(testTransaction.getRecipientAccount()).isEqualTo(UPDATED_RECIPIENT_ACCOUNT);
        assertThat(testTransaction.getRecipientServiceId()).isEqualTo(DEFAULT_RECIPIENT_SERVICE_ID);
        assertThat(testTransaction.getRecipientAmount()).isEqualTo(UPDATED_RECIPIENT_AMOUNT);
        assertThat(testTransaction.getRecipientCcy()).isEqualTo(DEFAULT_RECIPIENT_CCY);
        assertThat(testTransaction.getRecipientTrId()).isEqualTo(UPDATED_RECIPIENT_TR_ID);
        assertThat(testTransaction.getRecipientSuccess()).isEqualTo(DEFAULT_RECIPIENT_SUCCESS);
        assertThat(testTransaction.getRecipientDate()).isEqualTo(UPDATED_RECIPIENT_DATE);
        assertThat(testTransaction.getRecipientStatus()).isEqualTo(DEFAULT_RECIPIENT_STATUS);
        assertThat(testTransaction.getRecipientStatusText()).isEqualTo(DEFAULT_RECIPIENT_STATUS_TEXT);
        assertThat(testTransaction.getRecipientBillId()).isEqualTo(DEFAULT_RECIPIENT_BILL_ID);
        assertThat(testTransaction.getRecipientGateway()).isEqualTo(UPDATED_RECIPIENT_GATEWAY);
        assertThat(testTransaction.getSystemCommissionUpAmount()).isEqualTo(DEFAULT_SYSTEM_COMMISSION_UP_AMOUNT);
        assertThat(testTransaction.getSystemCommissionDownAmount()).isEqualTo(UPDATED_SYSTEM_COMMISSION_DOWN_AMOUNT);
        assertThat(testTransaction.getSystemFeeAmount()).isEqualTo(DEFAULT_SYSTEM_FEE_AMOUNT);
        assertThat(testTransaction.getAgentFeeAmount()).isEqualTo(DEFAULT_AGENT_FEE_AMOUNT);
        assertThat(testTransaction.getRoundAmount()).isEqualTo(UPDATED_ROUND_AMOUNT);
        assertThat(testTransaction.getSystemFee()).isEqualTo(DEFAULT_SYSTEM_FEE);
        assertThat(testTransaction.getSystemCurrency()).isEqualTo(UPDATED_SYSTEM_CURRENCY);
        assertThat(testTransaction.getCcyRate()).isEqualTo(DEFAULT_CCY_RATE);
        assertThat(testTransaction.getStatus()).isEqualTo(UPDATED_STATUS);
        assertThat(testTransaction.getComment()).isEqualTo(UPDATED_COMMENT);
        assertThat(testTransaction.getDate()).isEqualTo(DEFAULT_DATE);
        assertThat(testTransaction.getPayType()).isEqualTo(UPDATED_PAY_TYPE);
        assertThat(testTransaction.getRequestSubAccount()).isEqualTo(DEFAULT_REQUEST_SUB_ACCOUNT);
        assertThat(testTransaction.getEposMerchant()).isEqualTo(UPDATED_EPOS_MERCHANT);
        assertThat(testTransaction.getEposTerminal()).isEqualTo(DEFAULT_EPOS_TERMINAL);
        assertThat(testTransaction.getMain()).isEqualTo(UPDATED_MAIN);
        assertThat(testTransaction.getAmountRatio()).isEqualTo(DEFAULT_AMOUNT_RATIO);
        assertThat(testTransaction.getSenderBean()).isEqualTo(DEFAULT_SENDER_BEAN);
        assertThat(testTransaction.getCategory()).isEqualTo(UPDATED_CATEGORY);
    }

    @Test
    @Transactional
    void fullUpdateTransactionWithPatch() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        int databaseSizeBeforeUpdate = transactionRepository.findAll().size();

        // Update the transaction using partial update
        Transaction partialUpdatedTransaction = new Transaction();
        partialUpdatedTransaction.setId(transaction.getId());

        partialUpdatedTransaction
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
                .senderBean(UPDATED_SENDER_BEAN)
                .category(UPDATED_CATEGORY);

        restTransactionMockMvc
                .perform(
                        patch(ENTITY_API_URL_ID, partialUpdatedTransaction.getId())
                                .contentType("application/merge-patch+json")
                                .content(TestUtil.convertObjectToJsonBytes(partialUpdatedTransaction))
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
        assertThat(testTransaction.getSenderSuccess()).isEqualTo(UPDATED_SENDER_SUCCESS);
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
        assertThat(testTransaction.getCategory()).isEqualTo(UPDATED_CATEGORY);
    }

    @Test
    @Transactional
    void patchNonExistingTransaction() throws Exception {
        int databaseSizeBeforeUpdate = transactionRepository.findAll().size();
        transaction.setId(count.incrementAndGet());

        // Create the Transaction
        TransactionDTO transactionDTO = transactionMapper.toDto(transaction);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restTransactionMockMvc
                .perform(
                        patch(ENTITY_API_URL_ID, transactionDTO.getId())
                                .contentType("application/merge-patch+json")
                                .content(TestUtil.convertObjectToJsonBytes(transactionDTO))
                )
                .andExpect(status().isBadRequest());

        // Validate the Transaction in the database
        List<Transaction> transactionList = transactionRepository.findAll();
        assertThat(transactionList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchTransaction() throws Exception {
        int databaseSizeBeforeUpdate = transactionRepository.findAll().size();
        transaction.setId(count.incrementAndGet());

        // Create the Transaction
        TransactionDTO transactionDTO = transactionMapper.toDto(transaction);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restTransactionMockMvc
                .perform(
                        patch(ENTITY_API_URL_ID, count.incrementAndGet())
                                .contentType("application/merge-patch+json")
                                .content(TestUtil.convertObjectToJsonBytes(transactionDTO))
                )
                .andExpect(status().isBadRequest());

        // Validate the Transaction in the database
        List<Transaction> transactionList = transactionRepository.findAll();
        assertThat(transactionList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamTransaction() throws Exception {
        int databaseSizeBeforeUpdate = transactionRepository.findAll().size();
        transaction.setId(count.incrementAndGet());

        // Create the Transaction
        TransactionDTO transactionDTO = transactionMapper.toDto(transaction);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restTransactionMockMvc
                .perform(
                        patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(TestUtil.convertObjectToJsonBytes(transactionDTO))
                )
                .andExpect(status().isMethodNotAllowed());

        // Validate the Transaction in the database
        List<Transaction> transactionList = transactionRepository.findAll();
        assertThat(transactionList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteTransaction() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        int databaseSizeBeforeDelete = transactionRepository.findAll().size();

        // Delete the transaction
        restTransactionMockMvc
                .perform(delete(ENTITY_API_URL_ID, transaction.getId()).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        // Validate the database contains one less item
        List<Transaction> transactionList = transactionRepository.findAll();
        assertThat(transactionList).hasSize(databaseSizeBeforeDelete - 1);
    }


    @Test
    @Transactional
    @WithMockUser(value = "recipient", roles = {"RECIPIENT"})
    void getRecipientTransactions() throws Exception {
        fillRecipientTransactions();
        var result = transactionQueryService.findByCriteria(new TransactionCriteria(), Pageable.unpaged());
        assertThat(result.getSize()).isEqualTo(3);
    }

    private void fillRecipientTransactions() {

        var tran = createEntity(em);
        tran.setRecipientName("recipient");
        em.persist(tran);

        var tran1 = createEntity(em);
        tran1.setRecipientName("recipient");
        em.persist(tran1);

        var tran2 = createEntity(em);
        tran2.setRecipientName("recipient");
        em.persist(tran2);

        var tran3 = createEntity(em);
        tran3.setRecipientName("recipient2");
        em.persist(tran3);

        var tran4 = createEntity(em);
        tran4.setRecipientName(null);
        em.persist(tran4);

        em.flush();
        var tranList = transactionRepository.findAll();
        assertThat(tranList.size()).isEqualTo(5);
    }
}
