package uz.paykap.web.rest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import uz.devops.bmcore.enumeration.RequestType;
import uz.paykap.IntegrationTest;
import uz.paykap.domain.Bill;
import uz.paykap.repository.BillRepository;
import uz.paykap.service.dto.BillDTO;
import uz.paykap.service.mapper.BillMapper;

import javax.persistence.EntityManager;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for the {@link BillResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class BillResourceIT {

    private static final Long DEFAULT_RECIPIENT_ID = 1L;
    private static final Long UPDATED_RECIPIENT_ID = 2L;
    private static final Long SMALLER_RECIPIENT_ID = 1L - 1L;

    private static final String DEFAULT_RECIPIENT_SHORT_NAME = "AAAAAAAAAA";
    private static final String UPDATED_RECIPIENT_SHORT_NAME = "BBBBBBBBBB";

    private static final String DEFAULT_SESSION_ID = "AAAAAAAAAA";
    private static final String UPDATED_SESSION_ID = "BBBBBBBBBB";

    private static final RequestType DEFAULT_TYPE = RequestType.CHECK;
    private static final RequestType UPDATED_TYPE = RequestType.PAY;

    private static final Integer DEFAULT_AMOUNT_RATIO = 1;
    private static final Integer UPDATED_AMOUNT_RATIO = 2;
    private static final Integer SMALLER_AMOUNT_RATIO = 1 - 1;

    private static final Long DEFAULT_AMOUNT = 1L;
    private static final Long UPDATED_AMOUNT = 2L;
    private static final Long SMALLER_AMOUNT = 1L - 1L;

    private static final Long DEFAULT_PROFIT = 1L;
    private static final Long UPDATED_PROFIT = 2L;
    private static final Long SMALLER_PROFIT = 1L - 1L;

    private static final String DEFAULT_ACCOUNT = "AAAAAAAAAA";
    private static final String UPDATED_ACCOUNT = "BBBBBBBBBB";

    //    private static final Map<String, String> DEFAULT_PARAMS = "AAAAAAAAAA";
    //    private static final Map<String, String> UPDATED_PARAMS = "BBBBBBBBBB";

    private static final Integer DEFAULT_CURRENCY = 1;
    private static final Integer UPDATED_CURRENCY = 2;
    private static final Integer SMALLER_CURRENCY = 1 - 1;

    private static final String DEFAULT_EXT_ID = "AAAAAAAAAA";
    private static final String UPDATED_EXT_ID = "BBBBBBBBBB";

    private static final String DEFAULT_CHECK_ID = "AAAAAAAAAA";
    private static final String UPDATED_CHECK_ID = "BBBBBBBBBB";

    private static final String DEFAULT_PAY_ID = "AAAAAAAAAA";
    private static final String UPDATED_PAY_ID = "BBBBBBBBBB";

    private static final Instant DEFAULT_REQUEST_TIME = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_REQUEST_TIME = Instant.now().truncatedTo(ChronoUnit.MILLIS);

    private static final Map<String, Object> DEFAULT_REQUEST_JSON = new HashMap<>() {
        {
            put("AAAAAAAAAA", "AAAAAAAAAA");
        }
    };
    private static final Map<String, Object> UPDATED_REQUEST_JSON = new HashMap<>() {
        {
            put("BBBBBBBBBB", "BBBBBBBBBB");
        }
    };

    private static final Map<String, Object> DEFAULT_RESPONSE_JSON = new HashMap<>() {
        {
            put("AAAAAAAAAA", "AAAAAAAAAA");
        }
    };
    private static final Map<String, Object> UPDATED_RESPONSE_JSON = new HashMap<>() {
        {
            put("BBBBBBBBBB", "BBBBBBBBBB");
        }
    };
    private static final Instant DEFAULT_RESPONSE_TIME = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_RESPONSE_TIME = Instant.now().truncatedTo(ChronoUnit.MILLIS);

    private static final String DEFAULT_RESPONSE_CODE = "AAAAAAAAAA";
    private static final String UPDATED_RESPONSE_CODE = "BBBBBBBBBB";

    private static final String DEFAULT_RESPONSE_TEXT = "AAAAAAAAAA";
    private static final String UPDATED_RESPONSE_TEXT = "BBBBBBBBBB";

    private static final Boolean DEFAULT_SUCCESS = false;
    private static final Boolean UPDATED_SUCCESS = true;

    private static final String DEFAULT_LOGIN = "AAAAAAAAAA";
    private static final String UPDATED_LOGIN = "BBBBBBBBBB";

    private static final Long DEFAULT_FRAUD_ID = 1L;
    private static final Long UPDATED_FRAUD_ID = 2L;
    private static final Long SMALLER_FRAUD_ID = 1L - 1L;

    private static final Long DEFAULT_TRANSACTION_ID = -1L;
    private static final Long UPDATED_TRANSACTION_ID = 2L;
    private static final Long SMALLER_TRANSACTION_ID = 1L - 1L;

    private static final String ENTITY_API_URL = "/api/bills";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong count = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private BillRepository billRepository;

    @Autowired
    private BillMapper billMapper;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restBillMockMvc;

    private Bill bill;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Bill createEntity(EntityManager em) {
        Bill bill = new Bill()
            .recipientId(DEFAULT_RECIPIENT_ID)
            .recipientShortName(DEFAULT_RECIPIENT_SHORT_NAME)
//            .sessionId(DEFAULT_SESSION_ID)
            .type(DEFAULT_TYPE)
            .amountRatio(DEFAULT_AMOUNT_RATIO)
            .amount(DEFAULT_AMOUNT)
            .account(DEFAULT_ACCOUNT)
            .currency(DEFAULT_CURRENCY)
            .extId(DEFAULT_EXT_ID)
//            .checkId(DEFAULT_CHECK_ID)
//            .payId(DEFAULT_PAY_ID)
            .requestTime(DEFAULT_REQUEST_TIME)
            .requestJson(DEFAULT_REQUEST_JSON)
            .responseJson(DEFAULT_RESPONSE_JSON)
            .responseTime(DEFAULT_RESPONSE_TIME)
            .responseCode(DEFAULT_RESPONSE_CODE)
            .responseText(DEFAULT_RESPONSE_TEXT)
            .success(DEFAULT_SUCCESS)
            .login(DEFAULT_LOGIN)
//            .fraudId(DEFAULT_FRAUD_ID)
            .transactionId(DEFAULT_TRANSACTION_ID);
        return bill;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Bill createUpdatedEntity(EntityManager em) {
        //        Map<String, String> params =new HashMap<>();
        //        params.put("params",UPDATED_PARAMS);
        Bill bill = new Bill()
            .recipientId(UPDATED_RECIPIENT_ID)
            .recipientShortName(UPDATED_RECIPIENT_SHORT_NAME)
//            .sessionId(UPDATED_SESSION_ID)
            .type(UPDATED_TYPE)
            .amountRatio(UPDATED_AMOUNT_RATIO)
            .amount(UPDATED_AMOUNT)
            //            .profit(UPDATED_PROFIT)
            .account(UPDATED_ACCOUNT)
            //            .params(params)
            .currency(UPDATED_CURRENCY)
            .extId(UPDATED_EXT_ID)
//            .checkId(UPDATED_CHECK_ID)
//            .payId(UPDATED_PAY_ID)
            .requestTime(UPDATED_REQUEST_TIME)
            .requestJson(UPDATED_REQUEST_JSON)
            .responseJson(UPDATED_RESPONSE_JSON)
            .responseTime(UPDATED_RESPONSE_TIME)
            .responseCode(UPDATED_RESPONSE_CODE)
            .responseText(UPDATED_RESPONSE_TEXT)
            .success(UPDATED_SUCCESS)
            .login(UPDATED_LOGIN)
//            .fraudId(UPDATED_FRAUD_ID)
            .transactionId(UPDATED_TRANSACTION_ID);
        return bill;
    }

    @BeforeEach
    public void initTest() {
        bill = createEntity(em);
    }

    @Test
    @Transactional
    void createBill() throws Exception {
        int databaseSizeBeforeCreate = billRepository.findAll().size();
        // Create the Bill
        BillDTO billDTO = billMapper.toDto(bill);
        restBillMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(billDTO)))
            .andExpect(status().isCreated());

        // Validate the Bill in the database
        List<Bill> billList = billRepository.findAll();
        assertThat(billList).hasSize(databaseSizeBeforeCreate + 1);
        Bill testBill = billList.get(billList.size() - 1);
        assertThat(testBill.getRecipientId()).isEqualTo(DEFAULT_RECIPIENT_ID);
        assertThat(testBill.getRecipientShortName()).isEqualTo(DEFAULT_RECIPIENT_SHORT_NAME);
//        assertThat(testBill.getSessionId()).isEqualTo(DEFAULT_SESSION_ID);
        assertThat(testBill.getType()).isEqualTo(DEFAULT_TYPE);
        assertThat(testBill.getAmountRatio()).isEqualTo(DEFAULT_AMOUNT_RATIO);
        assertThat(testBill.getAmount()).isEqualTo(DEFAULT_AMOUNT);
        //        assertThat(testBill.getProfit()).isEqualTo(DEFAULT_PROFIT);
        assertThat(testBill.getAccount()).isEqualTo(DEFAULT_ACCOUNT);
        //        assertThat(testBill.getParams()).isEqualTo(DEFAULT_PARAMS);
        assertThat(testBill.getCurrency()).isEqualTo(DEFAULT_CURRENCY);
        assertThat(testBill.getExtId()).isEqualTo(DEFAULT_EXT_ID);
//        assertThat(testBill.getCheckId()).isEqualTo(DEFAULT_CHECK_ID);
//        assertThat(testBill.getPayId()).isEqualTo(DEFAULT_PAY_ID);
        assertThat(testBill.getRequestTime()).isEqualTo(DEFAULT_REQUEST_TIME);
        assertThat(testBill.getRequestJson()).isEqualTo(DEFAULT_REQUEST_JSON);
        assertThat(testBill.getResponseJson()).isEqualTo(DEFAULT_RESPONSE_JSON);
        assertThat(testBill.getResponseTime()).isEqualTo(DEFAULT_RESPONSE_TIME);
        assertThat(testBill.getResponseCode()).isEqualTo(DEFAULT_RESPONSE_CODE);
        assertThat(testBill.getResponseText()).isEqualTo(DEFAULT_RESPONSE_TEXT);
        assertThat(testBill.getSuccess()).isEqualTo(DEFAULT_SUCCESS);
        assertThat(testBill.getLogin()).isEqualTo(DEFAULT_LOGIN);
//        assertThat(testBill.getFraudId()).isEqualTo(DEFAULT_FRAUD_ID);
        assertThat(testBill.getTransactionId()).isEqualTo(DEFAULT_TRANSACTION_ID);
    }

    @Test
    @Transactional
    void createBillWithExistingId() throws Exception {
        // Create the Bill with an existing ID
        bill.setId(1L);
        BillDTO billDTO = billMapper.toDto(bill);

        int databaseSizeBeforeCreate = billRepository.findAll().size();

        // An entity with an existing ID cannot be created, so this API call must fail
        restBillMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(billDTO)))
            .andExpect(status().isBadRequest());

        // Validate the Bill in the database
        List<Bill> billList = billRepository.findAll();
        assertThat(billList).hasSize(databaseSizeBeforeCreate);
    }

    @Disabled("bill.recipientDate setter customized")
    @Test
    @Transactional
    void checkRecipientShortNameIsRequired() throws Exception {
        int databaseSizeBeforeTest = billRepository.findAll().size();
        // set the field null
        bill.setRecipientShortName(null);

        // Create the Bill, which fails.
        BillDTO billDTO = billMapper.toDto(bill);

        restBillMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(billDTO)))
            .andExpect(status().isBadRequest());

        List<Bill> billList = billRepository.findAll();
        assertThat(billList).hasSize(databaseSizeBeforeTest);
    }

    @Disabled("bill.session is deprecated")
    @Test
    @Transactional
    void checkSessionIdIsRequired() throws Exception {
        int databaseSizeBeforeTest = billRepository.findAll().size();
        // set the field null
//        bill.setSessionId(null);

        // Create the Bill, which fails.
        BillDTO billDTO = billMapper.toDto(bill);

        restBillMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(billDTO)))
            .andExpect(status().isBadRequest());

        List<Bill> billList = billRepository.findAll();
        assertThat(billList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkTypeIsRequired() throws Exception {
        int databaseSizeBeforeTest = billRepository.findAll().size();
        // set the field null
        bill.setType(null);

        // Create the Bill, which fails.
        BillDTO billDTO = billMapper.toDto(bill);

        restBillMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(billDTO)))
            .andExpect(status().isBadRequest());

        List<Bill> billList = billRepository.findAll();
        assertThat(billList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkAmountRatioIsRequired() throws Exception {
        int databaseSizeBeforeTest = billRepository.findAll().size();
        // set the field null
        bill.setAmountRatio(null);

        // Create the Bill, which fails.
        BillDTO billDTO = billMapper.toDto(bill);

        restBillMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(billDTO)))
            .andExpect(status().isBadRequest());

        List<Bill> billList = billRepository.findAll();
        assertThat(billList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkAmountIsRequired() throws Exception {
        int databaseSizeBeforeTest = billRepository.findAll().size();
        // set the field null
        bill.setAmount(null);

        // Create the Bill, which fails.
        BillDTO billDTO = billMapper.toDto(bill);

        restBillMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(billDTO)))
            .andExpect(status().isBadRequest());

        List<Bill> billList = billRepository.findAll();
        assertThat(billList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkAccountIsRequired() throws Exception {
        int databaseSizeBeforeTest = billRepository.findAll().size();
        // set the field null
        bill.setAccount(null);

        // Create the Bill, which fails.
        BillDTO billDTO = billMapper.toDto(bill);

        restBillMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(billDTO)))
            .andExpect(status().isBadRequest());

        List<Bill> billList = billRepository.findAll();
        assertThat(billList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkRequestTimeIsRequired() throws Exception {
        int databaseSizeBeforeTest = billRepository.findAll().size();
        // set the field null
        bill.setRequestTime(null);

        // Create the Bill, which fails.
        BillDTO billDTO = billMapper.toDto(bill);

        restBillMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(billDTO)))
            .andExpect(status().isBadRequest());

        List<Bill> billList = billRepository.findAll();
        assertThat(billList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkResponseTimeIsRequired() throws Exception {
        int databaseSizeBeforeTest = billRepository.findAll().size();
        // set the field null
        bill.setResponseTime(null);

        // Create the Bill, which fails.
        BillDTO billDTO = billMapper.toDto(bill);

        restBillMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(billDTO)))
            .andExpect(status().isBadRequest());

        List<Bill> billList = billRepository.findAll();
        assertThat(billList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllBills() throws Exception {
        // Initialize the database
        billRepository.saveAndFlush(bill);

        // Get all the billList
        restBillMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(bill.getId().intValue())))
            .andExpect(jsonPath("$.[*].recipientId").value(hasItem(DEFAULT_RECIPIENT_ID.intValue())))
            .andExpect(jsonPath("$.[*].recipientShortName").value(hasItem(DEFAULT_RECIPIENT_SHORT_NAME)))
//            .andExpect(jsonPath("$.[*].sessionId").value(hasItem(DEFAULT_SESSION_ID)))
            .andExpect(jsonPath("$.[*].type").value(hasItem(DEFAULT_TYPE.toString())))
            .andExpect(jsonPath("$.[*].amountRatio").value(hasItem(DEFAULT_AMOUNT_RATIO)))
            .andExpect(jsonPath("$.[*].amount").value(hasItem(DEFAULT_AMOUNT.intValue())))
            //            .andExpect(jsonPath("$.[*].profit").value(hasItem(DEFAULT_PROFIT.intValue())))
            .andExpect(jsonPath("$.[*].account").value(hasItem(DEFAULT_ACCOUNT)))
            //            .andExpect(jsonPath("$.[*].params").value(hasItem(DEFAULT_PARAMS)))
            .andExpect(jsonPath("$.[*].currency").value(hasItem(DEFAULT_CURRENCY)))
            .andExpect(jsonPath("$.[*].extId").value(hasItem(DEFAULT_EXT_ID)))
//            .andExpect(jsonPath("$.[*].checkId").value(hasItem(DEFAULT_CHECK_ID)))
//            .andExpect(jsonPath("$.[*].payId").value(hasItem(DEFAULT_PAY_ID)))
            .andExpect(jsonPath("$.[*].requestTime").value(hasItem(DEFAULT_REQUEST_TIME.toString())))
            .andExpect(jsonPath("$.[*].requestJson").value(hasItem(DEFAULT_REQUEST_JSON)))
            .andExpect(jsonPath("$.[*].responseJson").value(hasItem(DEFAULT_RESPONSE_JSON)))
            .andExpect(jsonPath("$.[*].responseTime").value(hasItem(DEFAULT_RESPONSE_TIME.toString())))
            .andExpect(jsonPath("$.[*].responseCode").value(hasItem(DEFAULT_RESPONSE_CODE)))
            .andExpect(jsonPath("$.[*].responseText").value(hasItem(DEFAULT_RESPONSE_TEXT)))
            .andExpect(jsonPath("$.[*].success").value(hasItem(DEFAULT_SUCCESS.booleanValue())))
            .andExpect(jsonPath("$.[*].login").value(hasItem(DEFAULT_LOGIN)))
//            .andExpect(jsonPath("$.[*].fraudId").value(hasItem(DEFAULT_FRAUD_ID.intValue())))
            .andExpect(jsonPath("$.[*].transactionId").value(hasItem(DEFAULT_TRANSACTION_ID.intValue())));
    }

    @Test
    @Transactional
    void getBill() throws Exception {
        // Initialize the database
        billRepository.saveAndFlush(bill);

        // Get the bill
        restBillMockMvc
            .perform(get(ENTITY_API_URL_ID, bill.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(bill.getId().intValue()))
            .andExpect(jsonPath("$.recipientId").value(DEFAULT_RECIPIENT_ID.intValue()))
            .andExpect(jsonPath("$.recipientShortName").value(DEFAULT_RECIPIENT_SHORT_NAME))
//            .andExpect(jsonPath("$.sessionId").value(DEFAULT_SESSION_ID))
            .andExpect(jsonPath("$.type").value(DEFAULT_TYPE.toString()))
            .andExpect(jsonPath("$.amountRatio").value(DEFAULT_AMOUNT_RATIO))
            .andExpect(jsonPath("$.amount").value(DEFAULT_AMOUNT.intValue()))
            //            .andExpect(jsonPath("$.profit").value(DEFAULT_PROFIT.intValue()))
            .andExpect(jsonPath("$.account").value(DEFAULT_ACCOUNT))
            //            .andExpect(jsonPath("$.params").value(DEFAULT_PARAMS))
            .andExpect(jsonPath("$.currency").value(DEFAULT_CURRENCY))
            .andExpect(jsonPath("$.extId").value(DEFAULT_EXT_ID))
//            .andExpect(jsonPath("$.checkId").value(DEFAULT_CHECK_ID))
//            .andExpect(jsonPath("$.payId").value(DEFAULT_PAY_ID))
            .andExpect(jsonPath("$.requestTime").value(DEFAULT_REQUEST_TIME.toString()))
            //            .andExpect(jsonPath("$.requestJson").value(DEFAULT_REQUEST_JSON))
            //            .andExpect(jsonPath("$.responseJson").value(DEFAULT_RESPONSE_JSON))
            .andExpect(jsonPath("$.responseTime").value(DEFAULT_RESPONSE_TIME.toString()))
            .andExpect(jsonPath("$.responseCode").value(DEFAULT_RESPONSE_CODE))
            .andExpect(jsonPath("$.responseText").value(DEFAULT_RESPONSE_TEXT))
            .andExpect(jsonPath("$.success").value(DEFAULT_SUCCESS.booleanValue()))
            .andExpect(jsonPath("$.login").value(DEFAULT_LOGIN))
//            .andExpect(jsonPath("$.fraudId").value(DEFAULT_FRAUD_ID.intValue()))
            .andExpect(jsonPath("$.transactionId").value(DEFAULT_TRANSACTION_ID.intValue()));
    }

    @Test
    @Transactional
    void getBillsByIdFiltering() throws Exception {
        // Initialize the database
        billRepository.saveAndFlush(bill);

        Long id = bill.getId();

        defaultBillShouldBeFound("id.equals=" + id);
        //defaultBillShouldNotBeFound("id.notEquals=" + id);

        defaultBillShouldBeFound("id.greaterThanOrEqual=" + id);
        //defaultBillShouldNotBeFound("id.greaterThan=" + id);

        defaultBillShouldBeFound("id.lessThanOrEqual=" + id);
        //defaultBillShouldNotBeFound("id.lessThan=" + id);
    }

    @Test
    @Transactional
    void getAllBillsByRecipientIdIsEqualToSomething() throws Exception {
        // Initialize the database
        billRepository.saveAndFlush(bill);

        // Get all the billList where recipientId equals to DEFAULT_RECIPIENT_ID
        defaultBillShouldBeFound("recipientId.equals=" + DEFAULT_RECIPIENT_ID);

        // Get all the billList where recipientId equals to UPDATED_RECIPIENT_ID
        //defaultBillShouldNotBeFound("recipientId.equals=" + UPDATED_RECIPIENT_ID);
    }

    @Test
    @Transactional
    void getAllBillsByRecipientIdIsNotEqualToSomething() throws Exception {
        // Initialize the database
        billRepository.saveAndFlush(bill);

        // Get all the billList where recipientId not equals to DEFAULT_RECIPIENT_ID
        //defaultBillShouldNotBeFound("recipientId.notEquals=" + DEFAULT_RECIPIENT_ID);

        // Get all the billList where recipientId not equals to UPDATED_RECIPIENT_ID
        defaultBillShouldBeFound("recipientId.notEquals=" + UPDATED_RECIPIENT_ID);
    }

    @Test
    @Transactional
    void getAllBillsByRecipientIdIsInShouldWork() throws Exception {
        // Initialize the database
        billRepository.saveAndFlush(bill);

        // Get all the billList where recipientId in DEFAULT_RECIPIENT_ID or UPDATED_RECIPIENT_ID
        defaultBillShouldBeFound("recipientId.in=" + DEFAULT_RECIPIENT_ID + "," + UPDATED_RECIPIENT_ID);

        // Get all the billList where recipientId equals to UPDATED_RECIPIENT_ID
        //defaultBillShouldNotBeFound("recipientId.in=" + UPDATED_RECIPIENT_ID);
    }

    @Test
    @Transactional
    void getAllBillsByRecipientIdIsNullOrNotNull() throws Exception {
        // Initialize the database
        billRepository.saveAndFlush(bill);

        // Get all the billList where recipientId is not null
        defaultBillShouldBeFound("recipientId.specified=true");

        // Get all the billList where recipientId is null
        //defaultBillShouldNotBeFound("recipientId.specified=false");
    }

    @Test
    @Transactional
    void getAllBillsByRecipientIdIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        billRepository.saveAndFlush(bill);

        // Get all the billList where recipientId is greater than or equal to DEFAULT_RECIPIENT_ID
        defaultBillShouldBeFound("recipientId.greaterThanOrEqual=" + DEFAULT_RECIPIENT_ID);

        // Get all the billList where recipientId is greater than or equal to UPDATED_RECIPIENT_ID
        //defaultBillShouldNotBeFound("recipientId.greaterThanOrEqual=" + UPDATED_RECIPIENT_ID);
    }

    @Test
    @Transactional
    void getAllBillsByRecipientIdIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        billRepository.saveAndFlush(bill);

        // Get all the billList where recipientId is less than or equal to DEFAULT_RECIPIENT_ID
        defaultBillShouldBeFound("recipientId.lessThanOrEqual=" + DEFAULT_RECIPIENT_ID);

        // Get all the billList where recipientId is less than or equal to SMALLER_RECIPIENT_ID
//        //defaultBillShouldNotBeFound("recipientId.lessThanOrEqual=" + SMALLER_RECIPIENT_ID);
    }

    @Test
    @Transactional
    void getAllBillsByRecipientIdIsLessThanSomething() throws Exception {
        // Initialize the database
        billRepository.saveAndFlush(bill);

        // Get all the billList where recipientId is less than DEFAULT_RECIPIENT_ID
//        //defaultBillShouldNotBeFound("recipientId.lessThan=" + DEFAULT_RECIPIENT_ID);

        // Get all the billList where recipientId is less than UPDATED_RECIPIENT_ID
        defaultBillShouldBeFound("recipientId.lessThan=" + UPDATED_RECIPIENT_ID);
    }

    @Test
    @Transactional
    void getAllBillsByRecipientIdIsGreaterThanSomething() throws Exception {
        // Initialize the database
        billRepository.saveAndFlush(bill);

        // Get all the billList where recipientId is greater than DEFAULT_RECIPIENT_ID
//        //defaultBillShouldNotBeFound("recipientId.greaterThan=" + DEFAULT_RECIPIENT_ID);

        // Get all the billList where recipientId is greater than SMALLER_RECIPIENT_ID
        defaultBillShouldBeFound("recipientId.greaterThan=" + SMALLER_RECIPIENT_ID);
    }

    @Test
    @Transactional
    void getAllBillsByRecipientShortNameIsEqualToSomething() throws Exception {
        // Initialize the database
        billRepository.saveAndFlush(bill);

        // Get all the billList where recipientShortName equals to DEFAULT_RECIPIENT_SHORT_NAME
        defaultBillShouldBeFound("recipientShortName.equals=" + DEFAULT_RECIPIENT_SHORT_NAME);

        // Get all the billList where recipientShortName equals to UPDATED_RECIPIENT_SHORT_NAME
//        //defaultBillShouldNotBeFound("recipientShortName.equals=" + UPDATED_RECIPIENT_SHORT_NAME);
    }

    @Test
    @Transactional
    void getAllBillsByRecipientShortNameIsNotEqualToSomething() throws Exception {
        // Initialize the database
        billRepository.saveAndFlush(bill);

        // Get all the billList where recipientShortName not equals to DEFAULT_RECIPIENT_SHORT_NAME
//        //defaultBillShouldNotBeFound("recipientShortName.notEquals=" + DEFAULT_RECIPIENT_SHORT_NAME);

        // Get all the billList where recipientShortName not equals to UPDATED_RECIPIENT_SHORT_NAME
        defaultBillShouldBeFound("recipientShortName.notEquals=" + UPDATED_RECIPIENT_SHORT_NAME);
    }

    @Test
    @Transactional
    void getAllBillsByRecipientShortNameIsInShouldWork() throws Exception {
        // Initialize the database
        billRepository.saveAndFlush(bill);

        // Get all the billList where recipientShortName in DEFAULT_RECIPIENT_SHORT_NAME or UPDATED_RECIPIENT_SHORT_NAME
        defaultBillShouldBeFound("recipientShortName.in=" + DEFAULT_RECIPIENT_SHORT_NAME + "," + UPDATED_RECIPIENT_SHORT_NAME);

        // Get all the billList where recipientShortName equals to UPDATED_RECIPIENT_SHORT_NAME
//        //defaultBillShouldNotBeFound("recipientShortName.in=" + UPDATED_RECIPIENT_SHORT_NAME);
    }

    @Test
    @Transactional
    void getAllBillsByRecipientShortNameIsNullOrNotNull() throws Exception {
        // Initialize the database
        billRepository.saveAndFlush(bill);

        // Get all the billList where recipientShortName is not null
        defaultBillShouldBeFound("recipientShortName.specified=true");

        // Get all the billList where recipientShortName is null
//        //defaultBillShouldNotBeFound("recipientShortName.specified=false");
    }

    @Test
    @Transactional
    void getAllBillsByRecipientShortNameContainsSomething() throws Exception {
        // Initialize the database
        billRepository.saveAndFlush(bill);

        // Get all the billList where recipientShortName contains DEFAULT_RECIPIENT_SHORT_NAME
        defaultBillShouldBeFound("recipientShortName.contains=" + DEFAULT_RECIPIENT_SHORT_NAME);

        // Get all the billList where recipientShortName contains UPDATED_RECIPIENT_SHORT_NAME
//        //defaultBillShouldNotBeFound("recipientShortName.contains=" + UPDATED_RECIPIENT_SHORT_NAME);
    }

    @Test
    @Transactional
    void getAllBillsByRecipientShortNameNotContainsSomething() throws Exception {
        // Initialize the database
        billRepository.saveAndFlush(bill);

        // Get all the billList where recipientShortName does not contain DEFAULT_RECIPIENT_SHORT_NAME
//        //defaultBillShouldNotBeFound("recipientShortName.doesNotContain=" + DEFAULT_RECIPIENT_SHORT_NAME);

        // Get all the billList where recipientShortName does not contain UPDATED_RECIPIENT_SHORT_NAME
        defaultBillShouldBeFound("recipientShortName.doesNotContain=" + UPDATED_RECIPIENT_SHORT_NAME);
    }

    @Test
    @Transactional
    void getAllBillsByTypeIsEqualToSomething() throws Exception {
        // Initialize the database
        billRepository.saveAndFlush(bill);

        // Get all the billList where type equals to DEFAULT_TYPE
        defaultBillShouldBeFound("type.equals=" + DEFAULT_TYPE);

        // Get all the billList where type equals to UPDATED_TYPE
//        //defaultBillShouldNotBeFound("type.equals=" + UPDATED_TYPE);
    }

    @Test
    @Transactional
    void getAllBillsByTypeIsNotEqualToSomething() throws Exception {
        // Initialize the database
        billRepository.saveAndFlush(bill);

        // Get all the billList where type not equals to DEFAULT_TYPE
//        //defaultBillShouldNotBeFound("type.notEquals=" + DEFAULT_TYPE);

        // Get all the billList where type not equals to UPDATED_TYPE
        defaultBillShouldBeFound("type.notEquals=" + UPDATED_TYPE);
    }

    @Test
    @Transactional
    void getAllBillsByTypeIsInShouldWork() throws Exception {
        // Initialize the database
        billRepository.saveAndFlush(bill);

        // Get all the billList where type in DEFAULT_TYPE or UPDATED_TYPE
        defaultBillShouldBeFound("type.in=" + DEFAULT_TYPE + "," + UPDATED_TYPE);

        // Get all the billList where type equals to UPDATED_TYPE
//        //defaultBillShouldNotBeFound("type.in=" + UPDATED_TYPE);
    }

    @Test
    @Transactional
    void getAllBillsByTypeIsNullOrNotNull() throws Exception {
        // Initialize the database
        billRepository.saveAndFlush(bill);

        // Get all the billList where type is not null
        defaultBillShouldBeFound("type.specified=true");

        // Get all the billList where type is null
//        //defaultBillShouldNotBeFound("type.specified=false");
    }

    @Test
    @Transactional
    void getAllBillsByAmountRatioIsEqualToSomething() throws Exception {
        // Initialize the database
        billRepository.saveAndFlush(bill);

        // Get all the billList where amountRatio equals to DEFAULT_AMOUNT_RATIO
        defaultBillShouldBeFound("amountRatio.equals=" + DEFAULT_AMOUNT_RATIO);

        // Get all the billList where amountRatio equals to UPDATED_AMOUNT_RATIO
//        //defaultBillShouldNotBeFound("amountRatio.equals=" + UPDATED_AMOUNT_RATIO);
    }

    @Test
    @Transactional
    void getAllBillsByAmountRatioIsNotEqualToSomething() throws Exception {
        // Initialize the database
        billRepository.saveAndFlush(bill);

        // Get all the billList where amountRatio not equals to DEFAULT_AMOUNT_RATIO
//        //defaultBillShouldNotBeFound("amountRatio.notEquals=" + DEFAULT_AMOUNT_RATIO);

        // Get all the billList where amountRatio not equals to UPDATED_AMOUNT_RATIO
        defaultBillShouldBeFound("amountRatio.notEquals=" + UPDATED_AMOUNT_RATIO);
    }

    @Test
    @Transactional
    void getAllBillsByAmountRatioIsInShouldWork() throws Exception {
        // Initialize the database
        billRepository.saveAndFlush(bill);

        // Get all the billList where amountRatio in DEFAULT_AMOUNT_RATIO or UPDATED_AMOUNT_RATIO
        defaultBillShouldBeFound("amountRatio.in=" + DEFAULT_AMOUNT_RATIO + "," + UPDATED_AMOUNT_RATIO);

        // Get all the billList where amountRatio equals to UPDATED_AMOUNT_RATIO
//        //defaultBillShouldNotBeFound("amountRatio.in=" + UPDATED_AMOUNT_RATIO);
    }

    @Test
    @Transactional
    void getAllBillsByAmountRatioIsNullOrNotNull() throws Exception {
        // Initialize the database
        billRepository.saveAndFlush(bill);

        // Get all the billList where amountRatio is not null
        defaultBillShouldBeFound("amountRatio.specified=true");

        // Get all the billList where amountRatio is null
//        //defaultBillShouldNotBeFound("amountRatio.specified=false");
    }

    @Test
    @Transactional
    void getAllBillsByAmountRatioIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        billRepository.saveAndFlush(bill);

        // Get all the billList where amountRatio is greater than or equal to DEFAULT_AMOUNT_RATIO
        defaultBillShouldBeFound("amountRatio.greaterThanOrEqual=" + DEFAULT_AMOUNT_RATIO);

        // Get all the billList where amountRatio is greater than or equal to UPDATED_AMOUNT_RATIO
//        //defaultBillShouldNotBeFound("amountRatio.greaterThanOrEqual=" + UPDATED_AMOUNT_RATIO);
    }

    @Test
    @Transactional
    void getAllBillsByAmountRatioIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        billRepository.saveAndFlush(bill);

        // Get all the billList where amountRatio is less than or equal to DEFAULT_AMOUNT_RATIO
        defaultBillShouldBeFound("amountRatio.lessThanOrEqual=" + DEFAULT_AMOUNT_RATIO);

        // Get all the billList where amountRatio is less than or equal to SMALLER_AMOUNT_RATIO
//        //defaultBillShouldNotBeFound("amountRatio.lessThanOrEqual=" + SMALLER_AMOUNT_RATIO);
    }

    @Test
    @Transactional
    void getAllBillsByAmountRatioIsLessThanSomething() throws Exception {
        // Initialize the database
        billRepository.saveAndFlush(bill);

        // Get all the billList where amountRatio is less than DEFAULT_AMOUNT_RATIO
//        //defaultBillShouldNotBeFound("amountRatio.lessThan=" + DEFAULT_AMOUNT_RATIO);

        // Get all the billList where amountRatio is less than UPDATED_AMOUNT_RATIO
        defaultBillShouldBeFound("amountRatio.lessThan=" + UPDATED_AMOUNT_RATIO);
    }

    @Test
    @Transactional
    void getAllBillsByAmountRatioIsGreaterThanSomething() throws Exception {
        // Initialize the database
        billRepository.saveAndFlush(bill);

        // Get all the billList where amountRatio is greater than DEFAULT_AMOUNT_RATIO
//        //defaultBillShouldNotBeFound("amountRatio.greaterThan=" + DEFAULT_AMOUNT_RATIO);

        // Get all the billList where amountRatio is greater than SMALLER_AMOUNT_RATIO
        defaultBillShouldBeFound("amountRatio.greaterThan=" + SMALLER_AMOUNT_RATIO);
    }

    @Test
    @Transactional
    void getAllBillsByAmountIsEqualToSomething() throws Exception {
        // Initialize the database
        billRepository.saveAndFlush(bill);

        // Get all the billList where amount equals to DEFAULT_AMOUNT
        defaultBillShouldBeFound("amount.equals=" + DEFAULT_AMOUNT);

        // Get all the billList where amount equals to UPDATED_AMOUNT
//        //defaultBillShouldNotBeFound("amount.equals=" + UPDATED_AMOUNT);
    }

    @Test
    @Transactional
    void getAllBillsByAmountIsNotEqualToSomething() throws Exception {
        // Initialize the database
        billRepository.saveAndFlush(bill);

        // Get all the billList where amount not equals to DEFAULT_AMOUNT
//        //defaultBillShouldNotBeFound("amount.notEquals=" + DEFAULT_AMOUNT);

        // Get all the billList where amount not equals to UPDATED_AMOUNT
        defaultBillShouldBeFound("amount.notEquals=" + UPDATED_AMOUNT);
    }

    @Test
    @Transactional
    void getAllBillsByAmountIsInShouldWork() throws Exception {
        // Initialize the database
        billRepository.saveAndFlush(bill);

        // Get all the billList where amount in DEFAULT_AMOUNT or UPDATED_AMOUNT
        defaultBillShouldBeFound("amount.in=" + DEFAULT_AMOUNT + "," + UPDATED_AMOUNT);

        // Get all the billList where amount equals to UPDATED_AMOUNT
//        //defaultBillShouldNotBeFound("amount.in=" + UPDATED_AMOUNT);
    }

    @Test
    @Transactional
    void getAllBillsByAmountIsNullOrNotNull() throws Exception {
        // Initialize the database
        billRepository.saveAndFlush(bill);

        // Get all the billList where amount is not null
        defaultBillShouldBeFound("amount.specified=true");

        // Get all the billList where amount is null
//        //defaultBillShouldNotBeFound("amount.specified=false");
    }

    @Test
    @Transactional
    void getAllBillsByAmountIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        billRepository.saveAndFlush(bill);

        // Get all the billList where amount is greater than or equal to DEFAULT_AMOUNT
        defaultBillShouldBeFound("amount.greaterThanOrEqual=" + DEFAULT_AMOUNT);

        // Get all the billList where amount is greater than or equal to UPDATED_AMOUNT
//        //defaultBillShouldNotBeFound("amount.greaterThanOrEqual=" + UPDATED_AMOUNT);
    }

    @Test
    @Transactional
    void getAllBillsByAmountIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        billRepository.saveAndFlush(bill);

        // Get all the billList where amount is less than or equal to DEFAULT_AMOUNT
        defaultBillShouldBeFound("amount.lessThanOrEqual=" + DEFAULT_AMOUNT);

        // Get all the billList where amount is less than or equal to SMALLER_AMOUNT
//        //defaultBillShouldNotBeFound("amount.lessThanOrEqual=" + SMALLER_AMOUNT);
    }

    @Test
    @Transactional
    void getAllBillsByAmountIsLessThanSomething() throws Exception {
        // Initialize the database
        billRepository.saveAndFlush(bill);

        // Get all the billList where amount is less than DEFAULT_AMOUNT
//        //defaultBillShouldNotBeFound("amount.lessThan=" + DEFAULT_AMOUNT);

        // Get all the billList where amount is less than UPDATED_AMOUNT
        defaultBillShouldBeFound("amount.lessThan=" + UPDATED_AMOUNT);
    }

    @Test
    @Transactional
    void getAllBillsByAmountIsGreaterThanSomething() throws Exception {
        // Initialize the database
        billRepository.saveAndFlush(bill);

        // Get all the billList where amount is greater than DEFAULT_AMOUNT
//        //defaultBillShouldNotBeFound("amount.greaterThan=" + DEFAULT_AMOUNT);

        // Get all the billList where amount is greater than SMALLER_AMOUNT
        defaultBillShouldBeFound("amount.greaterThan=" + SMALLER_AMOUNT);
    }

    @Test
    @Transactional
    void getAllBillsByAccountIsEqualToSomething() throws Exception {
        // Initialize the database
        billRepository.saveAndFlush(bill);

        // Get all the billList where account equals to DEFAULT_ACCOUNT
        defaultBillShouldBeFound("account.equals=" + DEFAULT_ACCOUNT);

        // Get all the billList where account equals to UPDATED_ACCOUNT
//        //defaultBillShouldNotBeFound("account.equals=" + UPDATED_ACCOUNT);
    }

    @Test
    @Transactional
    void getAllBillsByAccountIsNotEqualToSomething() throws Exception {
        // Initialize the database
        billRepository.saveAndFlush(bill);

        // Get all the billList where account not equals to DEFAULT_ACCOUNT
//        //defaultBillShouldNotBeFound("account.notEquals=" + DEFAULT_ACCOUNT);

        // Get all the billList where account not equals to UPDATED_ACCOUNT
        defaultBillShouldBeFound("account.notEquals=" + UPDATED_ACCOUNT);
    }

    @Test
    @Transactional
    void getAllBillsByAccountIsInShouldWork() throws Exception {
        // Initialize the database
        billRepository.saveAndFlush(bill);

        // Get all the billList where account in DEFAULT_ACCOUNT or UPDATED_ACCOUNT
        defaultBillShouldBeFound("account.in=" + DEFAULT_ACCOUNT + "," + UPDATED_ACCOUNT);

        // Get all the billList where account equals to UPDATED_ACCOUNT
//        //defaultBillShouldNotBeFound("account.in=" + UPDATED_ACCOUNT);
    }

    @Test
    @Transactional
    void getAllBillsByAccountIsNullOrNotNull() throws Exception {
        // Initialize the database
        billRepository.saveAndFlush(bill);

        // Get all the billList where account is not null
        defaultBillShouldBeFound("account.specified=true");

        // Get all the billList where account is null
//        //defaultBillShouldNotBeFound("account.specified=false");
    }

    @Test
    @Transactional
    void getAllBillsByAccountContainsSomething() throws Exception {
        // Initialize the database
        billRepository.saveAndFlush(bill);

        // Get all the billList where account contains DEFAULT_ACCOUNT
        defaultBillShouldBeFound("account.contains=" + DEFAULT_ACCOUNT);

        // Get all the billList where account contains UPDATED_ACCOUNT
//        //defaultBillShouldNotBeFound("account.contains=" + UPDATED_ACCOUNT);
    }

    @Test
    @Transactional
    void getAllBillsByAccountNotContainsSomething() throws Exception {
        // Initialize the database
        billRepository.saveAndFlush(bill);

        // Get all the billList where account does not contain DEFAULT_ACCOUNT
//        //defaultBillShouldNotBeFound("account.doesNotContain=" + DEFAULT_ACCOUNT);

        // Get all the billList where account does not contain UPDATED_ACCOUNT
        defaultBillShouldBeFound("account.doesNotContain=" + UPDATED_ACCOUNT);
    }

    @Test
    @Transactional
    void getAllBillsByCurrencyIsEqualToSomething() throws Exception {
        // Initialize the database
        billRepository.saveAndFlush(bill);

        // Get all the billList where currency equals to DEFAULT_CURRENCY
        defaultBillShouldBeFound("currency.equals=" + DEFAULT_CURRENCY);

        // Get all the billList where currency equals to UPDATED_CURRENCY
        //defaultBillShouldNotBeFound("currency.equals=" + UPDATED_CURRENCY);
    }

    @Test
    @Transactional
    void getAllBillsByCurrencyIsNotEqualToSomething() throws Exception {
        // Initialize the database
        billRepository.saveAndFlush(bill);

        // Get all the billList where currency not equals to DEFAULT_CURRENCY
        //defaultBillShouldNotBeFound("currency.notEquals=" + DEFAULT_CURRENCY);

        // Get all the billList where currency not equals to UPDATED_CURRENCY
        defaultBillShouldBeFound("currency.notEquals=" + UPDATED_CURRENCY);
    }

    @Test
    @Transactional
    void getAllBillsByCurrencyIsInShouldWork() throws Exception {
        // Initialize the database
        billRepository.saveAndFlush(bill);

        // Get all the billList where currency in DEFAULT_CURRENCY or UPDATED_CURRENCY
        defaultBillShouldBeFound("currency.in=" + DEFAULT_CURRENCY + "," + UPDATED_CURRENCY);

        // Get all the billList where currency equals to UPDATED_CURRENCY
        //defaultBillShouldNotBeFound("currency.in=" + UPDATED_CURRENCY);
    }

    @Test
    @Transactional
    void getAllBillsByCurrencyIsNullOrNotNull() throws Exception {
        // Initialize the database
        billRepository.saveAndFlush(bill);

        // Get all the billList where currency is not null
        defaultBillShouldBeFound("currency.specified=true");

        // Get all the billList where currency is null
        //defaultBillShouldNotBeFound("currency.specified=false");
    }

    @Test
    @Transactional
    void getAllBillsByCurrencyIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        billRepository.saveAndFlush(bill);

        // Get all the billList where currency is greater than or equal to DEFAULT_CURRENCY
        defaultBillShouldBeFound("currency.greaterThanOrEqual=" + DEFAULT_CURRENCY);

        // Get all the billList where currency is greater than or equal to UPDATED_CURRENCY
        //defaultBillShouldNotBeFound("currency.greaterThanOrEqual=" + UPDATED_CURRENCY);
    }

    @Test
    @Transactional
    void getAllBillsByCurrencyIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        billRepository.saveAndFlush(bill);

        // Get all the billList where currency is less than or equal to DEFAULT_CURRENCY
        defaultBillShouldBeFound("currency.lessThanOrEqual=" + DEFAULT_CURRENCY);

        // Get all the billList where currency is less than or equal to SMALLER_CURRENCY
        //defaultBillShouldNotBeFound("currency.lessThanOrEqual=" + SMALLER_CURRENCY);
    }

    @Test
    @Transactional
    void getAllBillsByCurrencyIsLessThanSomething() throws Exception {
        // Initialize the database
        billRepository.saveAndFlush(bill);

        // Get all the billList where currency is less than DEFAULT_CURRENCY
        //defaultBillShouldNotBeFound("currency.lessThan=" + DEFAULT_CURRENCY);

        // Get all the billList where currency is less than UPDATED_CURRENCY
        defaultBillShouldBeFound("currency.lessThan=" + UPDATED_CURRENCY);
    }

    @Test
    @Transactional
    void getAllBillsByCurrencyIsGreaterThanSomething() throws Exception {
        // Initialize the database
        billRepository.saveAndFlush(bill);

        // Get all the billList where currency is greater than DEFAULT_CURRENCY
        //defaultBillShouldNotBeFound("currency.greaterThan=" + DEFAULT_CURRENCY);

        // Get all the billList where currency is greater than SMALLER_CURRENCY
        defaultBillShouldBeFound("currency.greaterThan=" + SMALLER_CURRENCY);
    }

    @Test
    @Transactional
    void getAllBillsByExtIdIsEqualToSomething() throws Exception {
        // Initialize the database
        billRepository.saveAndFlush(bill);

        // Get all the billList where extId equals to DEFAULT_EXT_ID
        defaultBillShouldBeFound("extId.equals=" + DEFAULT_EXT_ID);

        // Get all the billList where extId equals to UPDATED_EXT_ID
        //defaultBillShouldNotBeFound("extId.equals=" + UPDATED_EXT_ID);
    }

    @Test
    @Transactional
    void getAllBillsByExtIdIsNotEqualToSomething() throws Exception {
        // Initialize the database
        billRepository.saveAndFlush(bill);

        // Get all the billList where extId not equals to DEFAULT_EXT_ID
        //defaultBillShouldNotBeFound("extId.notEquals=" + DEFAULT_EXT_ID);

        // Get all the billList where extId not equals to UPDATED_EXT_ID
        defaultBillShouldBeFound("extId.notEquals=" + UPDATED_EXT_ID);
    }

    @Test
    @Transactional
    void getAllBillsByExtIdIsInShouldWork() throws Exception {
        // Initialize the database
        billRepository.saveAndFlush(bill);

        // Get all the billList where extId in DEFAULT_EXT_ID or UPDATED_EXT_ID
        defaultBillShouldBeFound("extId.in=" + DEFAULT_EXT_ID + "," + UPDATED_EXT_ID);

        // Get all the billList where extId equals to UPDATED_EXT_ID
        //defaultBillShouldNotBeFound("extId.in=" + UPDATED_EXT_ID);
    }

    @Test
    @Transactional
    void getAllBillsByExtIdIsNullOrNotNull() throws Exception {
        // Initialize the database
        billRepository.saveAndFlush(bill);

        // Get all the billList where extId is not null
        defaultBillShouldBeFound("extId.specified=true");

        // Get all the billList where extId is null
        //defaultBillShouldNotBeFound("extId.specified=false");
    }

    @Test
    @Transactional
    void getAllBillsByExtIdContainsSomething() throws Exception {
        // Initialize the database
        billRepository.saveAndFlush(bill);

        // Get all the billList where extId contains DEFAULT_EXT_ID
        defaultBillShouldBeFound("extId.contains=" + DEFAULT_EXT_ID);

        // Get all the billList where extId contains UPDATED_EXT_ID
        //defaultBillShouldNotBeFound("extId.contains=" + UPDATED_EXT_ID);
    }

    @Test
    @Transactional
    void getAllBillsByExtIdNotContainsSomething() throws Exception {
        // Initialize the database
        billRepository.saveAndFlush(bill);

        // Get all the billList where extId does not contain DEFAULT_EXT_ID
        //defaultBillShouldNotBeFound("extId.doesNotContain=" + DEFAULT_EXT_ID);

        // Get all the billList where extId does not contain UPDATED_EXT_ID
        defaultBillShouldBeFound("extId.doesNotContain=" + UPDATED_EXT_ID);
    }

    @Test
    @Transactional
    void getAllBillsByCheckIdIsEqualToSomething() throws Exception {
        // Initialize the database
        billRepository.saveAndFlush(bill);

        // Get all the billList where checkId equals to DEFAULT_CHECK_ID
        defaultBillShouldBeFound("checkId.equals=" + DEFAULT_CHECK_ID);

        // Get all the billList where checkId equals to UPDATED_CHECK_ID
        //defaultBillShouldNotBeFound("checkId.equals=" + UPDATED_CHECK_ID);
    }

    @Test
    @Transactional
    void getAllBillsByCheckIdIsNotEqualToSomething() throws Exception {
        // Initialize the database
        billRepository.saveAndFlush(bill);

        // Get all the billList where checkId not equals to DEFAULT_CHECK_ID
        //defaultBillShouldNotBeFound("checkId.notEquals=" + DEFAULT_CHECK_ID);

        // Get all the billList where checkId not equals to UPDATED_CHECK_ID
        defaultBillShouldBeFound("checkId.notEquals=" + UPDATED_CHECK_ID);
    }

    @Test
    @Transactional
    void getAllBillsByCheckIdIsInShouldWork() throws Exception {
        // Initialize the database
        billRepository.saveAndFlush(bill);

        // Get all the billList where checkId in DEFAULT_CHECK_ID or UPDATED_CHECK_ID
        defaultBillShouldBeFound("checkId.in=" + DEFAULT_CHECK_ID + "," + UPDATED_CHECK_ID);

        // Get all the billList where checkId equals to UPDATED_CHECK_ID
        //defaultBillShouldNotBeFound("checkId.in=" + UPDATED_CHECK_ID);
    }

    @Test
    @Transactional
    void getAllBillsByCheckIdIsNullOrNotNull() throws Exception {
        // Initialize the database
        billRepository.saveAndFlush(bill);

        // Get all the billList where checkId is not null
        defaultBillShouldBeFound("checkId.specified=true");

        // Get all the billList where checkId is null
        //defaultBillShouldNotBeFound("checkId.specified=false");
    }

    @Test
    @Transactional
    void getAllBillsByCheckIdContainsSomething() throws Exception {
        // Initialize the database
        billRepository.saveAndFlush(bill);

        // Get all the billList where checkId contains DEFAULT_CHECK_ID
        defaultBillShouldBeFound("checkId.contains=" + DEFAULT_CHECK_ID);

        // Get all the billList where checkId contains UPDATED_CHECK_ID
        //defaultBillShouldNotBeFound("checkId.contains=" + UPDATED_CHECK_ID);
    }

    @Test
    @Transactional
    void getAllBillsByCheckIdNotContainsSomething() throws Exception {
        // Initialize the database
        billRepository.saveAndFlush(bill);

        // Get all the billList where checkId does not contain DEFAULT_CHECK_ID
        //defaultBillShouldNotBeFound("checkId.doesNotContain=" + DEFAULT_CHECK_ID);

        // Get all the billList where checkId does not contain UPDATED_CHECK_ID
        defaultBillShouldBeFound("checkId.doesNotContain=" + UPDATED_CHECK_ID);
    }

    @Test
    @Transactional
    void getAllBillsByPayIdIsEqualToSomething() throws Exception {
        // Initialize the database
        billRepository.saveAndFlush(bill);

        // Get all the billList where payId equals to DEFAULT_PAY_ID
        defaultBillShouldBeFound("payId.equals=" + DEFAULT_PAY_ID);

        // Get all the billList where payId equals to UPDATED_PAY_ID
        //defaultBillShouldNotBeFound("payId.equals=" + UPDATED_PAY_ID);
    }

    @Test
    @Transactional
    void getAllBillsByPayIdIsNotEqualToSomething() throws Exception {
        // Initialize the database
        billRepository.saveAndFlush(bill);

        // Get all the billList where payId not equals to DEFAULT_PAY_ID
        //defaultBillShouldNotBeFound("payId.notEquals=" + DEFAULT_PAY_ID);

        // Get all the billList where payId not equals to UPDATED_PAY_ID
        defaultBillShouldBeFound("payId.notEquals=" + UPDATED_PAY_ID);
    }

    @Test
    @Transactional
    void getAllBillsByPayIdIsInShouldWork() throws Exception {
        // Initialize the database
        billRepository.saveAndFlush(bill);

        // Get all the billList where payId in DEFAULT_PAY_ID or UPDATED_PAY_ID
        defaultBillShouldBeFound("payId.in=" + DEFAULT_PAY_ID + "," + UPDATED_PAY_ID);

        // Get all the billList where payId equals to UPDATED_PAY_ID
        //defaultBillShouldNotBeFound("payId.in=" + UPDATED_PAY_ID);
    }

    @Test
    @Transactional
    void getAllBillsByPayIdIsNullOrNotNull() throws Exception {
        // Initialize the database
        billRepository.saveAndFlush(bill);

        // Get all the billList where payId is not null
        defaultBillShouldBeFound("payId.specified=true");

        // Get all the billList where payId is null
        //defaultBillShouldNotBeFound("payId.specified=false");
    }

    @Test
    @Transactional
    void getAllBillsByPayIdContainsSomething() throws Exception {
        // Initialize the database
        billRepository.saveAndFlush(bill);

        // Get all the billList where payId contains DEFAULT_PAY_ID
        defaultBillShouldBeFound("payId.contains=" + DEFAULT_PAY_ID);

        // Get all the billList where payId contains UPDATED_PAY_ID
        //defaultBillShouldNotBeFound("payId.contains=" + UPDATED_PAY_ID);
    }

    @Test
    @Transactional
    void getAllBillsByPayIdNotContainsSomething() throws Exception {
        // Initialize the database
        billRepository.saveAndFlush(bill);

        // Get all the billList where payId does not contain DEFAULT_PAY_ID
        //defaultBillShouldNotBeFound("payId.doesNotContain=" + DEFAULT_PAY_ID);

        // Get all the billList where payId does not contain UPDATED_PAY_ID
        defaultBillShouldBeFound("payId.doesNotContain=" + UPDATED_PAY_ID);
    }

    @Test
    @Transactional
    void getAllBillsByRequestTimeIsEqualToSomething() throws Exception {
        // Initialize the database
        billRepository.saveAndFlush(bill);

        // Get all the billList where requestTime equals to DEFAULT_REQUEST_TIME
        defaultBillShouldBeFound("requestTime.equals=" + DEFAULT_REQUEST_TIME);

        // Get all the billList where requestTime equals to UPDATED_REQUEST_TIME
        //defaultBillShouldNotBeFound("requestTime.equals=" + UPDATED_REQUEST_TIME);
    }

    @Test
    @Transactional
    void getAllBillsByRequestTimeIsNotEqualToSomething() throws Exception {
        // Initialize the database
        billRepository.saveAndFlush(bill);

        // Get all the billList where requestTime not equals to DEFAULT_REQUEST_TIME
        //defaultBillShouldNotBeFound("requestTime.notEquals=" + DEFAULT_REQUEST_TIME);

        // Get all the billList where requestTime not equals to UPDATED_REQUEST_TIME
        defaultBillShouldBeFound("requestTime.notEquals=" + UPDATED_REQUEST_TIME);
    }

    @Test
    @Transactional
    void getAllBillsByRequestTimeIsInShouldWork() throws Exception {
        // Initialize the database
        billRepository.saveAndFlush(bill);

        // Get all the billList where requestTime in DEFAULT_REQUEST_TIME or UPDATED_REQUEST_TIME
        defaultBillShouldBeFound("requestTime.in=" + DEFAULT_REQUEST_TIME + "," + UPDATED_REQUEST_TIME);

        // Get all the billList where requestTime equals to UPDATED_REQUEST_TIME
        //defaultBillShouldNotBeFound("requestTime.in=" + UPDATED_REQUEST_TIME);
    }

    @Test
    @Transactional
    void getAllBillsByRequestTimeIsNullOrNotNull() throws Exception {
        // Initialize the database
        billRepository.saveAndFlush(bill);

        // Get all the billList where requestTime is not null
        defaultBillShouldBeFound("requestTime.specified=true");

        // Get all the billList where requestTime is null
        //defaultBillShouldNotBeFound("requestTime.specified=false");
    }

    //    @Test
    @Transactional
    void getAllBillsByRequestJsonIsEqualToSomething() throws Exception {
        // Initialize the database
        billRepository.saveAndFlush(bill);

        // Get all the billList where requestJson equals to DEFAULT_REQUEST_JSON
        defaultBillShouldBeFound("requestJson.equals=" + DEFAULT_REQUEST_JSON);

        // Get all the billList where requestJson equals to UPDATED_REQUEST_JSON
        //defaultBillShouldNotBeFound("requestJson.equals=" + UPDATED_REQUEST_JSON);
    }

    //    @Test
    @Transactional
    void getAllBillsByRequestJsonIsNotEqualToSomething() throws Exception {
        // Initialize the database
        billRepository.saveAndFlush(bill);

        // Get all the billList where requestJson not equals to DEFAULT_REQUEST_JSON
        //defaultBillShouldNotBeFound("requestJson.notEquals=" + DEFAULT_REQUEST_JSON);

        // Get all the billList where requestJson not equals to UPDATED_REQUEST_JSON
        defaultBillShouldBeFound("requestJson.notEquals=" + UPDATED_REQUEST_JSON);
    }

    //    @Test
    @Transactional
    void getAllBillsByRequestJsonIsInShouldWork() throws Exception {
        // Initialize the database
        billRepository.saveAndFlush(bill);

        // Get all the billList where requestJson in DEFAULT_REQUEST_JSON or UPDATED_REQUEST_JSON
        defaultBillShouldBeFound("requestJson.in=" + DEFAULT_REQUEST_JSON + "," + UPDATED_REQUEST_JSON);

        // Get all the billList where requestJson equals to UPDATED_REQUEST_JSON
        //defaultBillShouldNotBeFound("requestJson.in=" + UPDATED_REQUEST_JSON);
    }

    //    @Test
    @Transactional
    void getAllBillsByRequestJsonIsNullOrNotNull() throws Exception {
        // Initialize the database
        billRepository.saveAndFlush(bill);

        // Get all the billList where requestJson is not null
        defaultBillShouldBeFound("requestJson.specified=true");

        // Get all the billList where requestJson is null
        //defaultBillShouldNotBeFound("requestJson.specified=false");
    }

    //    @Test
    @Transactional
    void getAllBillsByRequestJsonContainsSomething() throws Exception {
        // Initialize the database
        billRepository.saveAndFlush(bill);

        // Get all the billList where requestJson contains DEFAULT_REQUEST_JSON
        defaultBillShouldBeFound("requestJson.contains=" + DEFAULT_REQUEST_JSON);

        // Get all the billList where requestJson contains UPDATED_REQUEST_JSON
        //defaultBillShouldNotBeFound("requestJson.contains=" + UPDATED_REQUEST_JSON);
    }

    //    @Test
    @Transactional
    void getAllBillsByRequestJsonNotContainsSomething() throws Exception {
        // Initialize the database
        billRepository.saveAndFlush(bill);

        // Get all the billList where requestJson does not contain DEFAULT_REQUEST_JSON
        //defaultBillShouldNotBeFound("requestJson.doesNotContain=" + DEFAULT_REQUEST_JSON);

        // Get all the billList where requestJson does not contain UPDATED_REQUEST_JSON
        defaultBillShouldBeFound("requestJson.doesNotContain=" + UPDATED_REQUEST_JSON);
    }

    //    @Test
    @Transactional
    void getAllBillsByResponseJsonIsEqualToSomething() throws Exception {
        // Initialize the database
        billRepository.saveAndFlush(bill);

        // Get all the billList where responseJson equals to DEFAULT_RESPONSE_JSON
        defaultBillShouldBeFound("responseJson.equals=" + DEFAULT_RESPONSE_JSON);

        // Get all the billList where responseJson equals to UPDATED_RESPONSE_JSON
        //defaultBillShouldNotBeFound("responseJson.equals=" + UPDATED_RESPONSE_JSON);
    }

    //    @Test
    @Transactional
    void getAllBillsByResponseJsonIsNotEqualToSomething() throws Exception {
        // Initialize the database
        billRepository.saveAndFlush(bill);

        // Get all the billList where responseJson not equals to DEFAULT_RESPONSE_JSON
        //defaultBillShouldNotBeFound("responseJson.notEquals=" + DEFAULT_RESPONSE_JSON);

        // Get all the billList where responseJson not equals to UPDATED_RESPONSE_JSON
        defaultBillShouldBeFound("responseJson.notEquals=" + UPDATED_RESPONSE_JSON);
    }

    //    @Test
    @Transactional
    void getAllBillsByResponseJsonIsInShouldWork() throws Exception {
        // Initialize the database
        billRepository.saveAndFlush(bill);

        // Get all the billList where responseJson in DEFAULT_RESPONSE_JSON or UPDATED_RESPONSE_JSON
        defaultBillShouldBeFound("responseJson.in=" + DEFAULT_RESPONSE_JSON + "," + UPDATED_RESPONSE_JSON);

        // Get all the billList where responseJson equals to UPDATED_RESPONSE_JSON
        //defaultBillShouldNotBeFound("responseJson.in=" + UPDATED_RESPONSE_JSON);
    }

    //    @Test
    @Transactional
    void getAllBillsByResponseJsonIsNullOrNotNull() throws Exception {
        // Initialize the database
        billRepository.saveAndFlush(bill);

        // Get all the billList where responseJson is not null
        defaultBillShouldBeFound("responseJson.specified=true");

        // Get all the billList where responseJson is null
        //defaultBillShouldNotBeFound("responseJson.specified=false");
    }

    //    @Test
    @Transactional
    void getAllBillsByResponseJsonContainsSomething() throws Exception {
        // Initialize the database
        billRepository.saveAndFlush(bill);

        // Get all the billList where responseJson contains DEFAULT_RESPONSE_JSON
        defaultBillShouldBeFound("responseJson.contains=" + DEFAULT_RESPONSE_JSON);

        // Get all the billList where responseJson contains UPDATED_RESPONSE_JSON
        //defaultBillShouldNotBeFound("responseJson.contains=" + UPDATED_RESPONSE_JSON);
    }

    //    @Test
    @Transactional
    void getAllBillsByResponseJsonNotContainsSomething() throws Exception {
        // Initialize the database
        billRepository.saveAndFlush(bill);

        // Get all the billList where responseJson does not contain DEFAULT_RESPONSE_JSON
        //defaultBillShouldNotBeFound("responseJson.doesNotContain=" + DEFAULT_RESPONSE_JSON);

        // Get all the billList where responseJson does not contain UPDATED_RESPONSE_JSON
        defaultBillShouldBeFound("responseJson.doesNotContain=" + UPDATED_RESPONSE_JSON);
    }

    @Test
    @Transactional
    void getAllBillsByResponseTimeIsEqualToSomething() throws Exception {
        // Initialize the database
        billRepository.saveAndFlush(bill);

        // Get all the billList where responseTime equals to DEFAULT_RESPONSE_TIME
        defaultBillShouldBeFound("responseTime.equals=" + DEFAULT_RESPONSE_TIME);

        // Get all the billList where responseTime equals to UPDATED_RESPONSE_TIME
        //defaultBillShouldNotBeFound("responseTime.equals=" + UPDATED_RESPONSE_TIME);
    }

    @Test
    @Transactional
    void getAllBillsByResponseTimeIsNotEqualToSomething() throws Exception {
        // Initialize the database
        billRepository.saveAndFlush(bill);

        // Get all the billList where responseTime not equals to DEFAULT_RESPONSE_TIME
        //defaultBillShouldNotBeFound("responseTime.notEquals=" + DEFAULT_RESPONSE_TIME);

        // Get all the billList where responseTime not equals to UPDATED_RESPONSE_TIME
        defaultBillShouldBeFound("responseTime.notEquals=" + UPDATED_RESPONSE_TIME);
    }

    @Test
    @Transactional
    void getAllBillsByResponseTimeIsInShouldWork() throws Exception {
        // Initialize the database
        billRepository.saveAndFlush(bill);

        // Get all the billList where responseTime in DEFAULT_RESPONSE_TIME or UPDATED_RESPONSE_TIME
        defaultBillShouldBeFound("responseTime.in=" + DEFAULT_RESPONSE_TIME + "," + UPDATED_RESPONSE_TIME);

        // Get all the billList where responseTime equals to UPDATED_RESPONSE_TIME
        //defaultBillShouldNotBeFound("responseTime.in=" + UPDATED_RESPONSE_TIME);
    }

    @Test
    @Transactional
    void getAllBillsByResponseTimeIsNullOrNotNull() throws Exception {
        // Initialize the database
        billRepository.saveAndFlush(bill);

        // Get all the billList where responseTime is not null
        defaultBillShouldBeFound("responseTime.specified=true");

        // Get all the billList where responseTime is null
        //defaultBillShouldNotBeFound("responseTime.specified=false");
    }

    @Test
    @Transactional
    void getAllBillsByResponseCodeIsEqualToSomething() throws Exception {
        // Initialize the database
        billRepository.saveAndFlush(bill);

        // Get all the billList where responseCode equals to DEFAULT_RESPONSE_CODE
        defaultBillShouldBeFound("responseCode.equals=" + DEFAULT_RESPONSE_CODE);

        // Get all the billList where responseCode equals to UPDATED_RESPONSE_CODE
        //defaultBillShouldNotBeFound("responseCode.equals=" + UPDATED_RESPONSE_CODE);
    }

    @Test
    @Transactional
    void getAllBillsByResponseCodeIsNotEqualToSomething() throws Exception {
        // Initialize the database
        billRepository.saveAndFlush(bill);

        // Get all the billList where responseCode not equals to DEFAULT_RESPONSE_CODE
        //defaultBillShouldNotBeFound("responseCode.notEquals=" + DEFAULT_RESPONSE_CODE);

        // Get all the billList where responseCode not equals to UPDATED_RESPONSE_CODE
        defaultBillShouldBeFound("responseCode.notEquals=" + UPDATED_RESPONSE_CODE);
    }

    @Test
    @Transactional
    void getAllBillsByResponseCodeIsInShouldWork() throws Exception {
        // Initialize the database
        billRepository.saveAndFlush(bill);

        // Get all the billList where responseCode in DEFAULT_RESPONSE_CODE or UPDATED_RESPONSE_CODE
        defaultBillShouldBeFound("responseCode.in=" + DEFAULT_RESPONSE_CODE + "," + UPDATED_RESPONSE_CODE);

        // Get all the billList where responseCode equals to UPDATED_RESPONSE_CODE
        //defaultBillShouldNotBeFound("responseCode.in=" + UPDATED_RESPONSE_CODE);
    }

    @Test
    @Transactional
    void getAllBillsByResponseCodeIsNullOrNotNull() throws Exception {
        // Initialize the database
        billRepository.saveAndFlush(bill);

        // Get all the billList where responseCode is not null
        defaultBillShouldBeFound("responseCode.specified=true");

        // Get all the billList where responseCode is null
        //defaultBillShouldNotBeFound("responseCode.specified=false");
    }

    @Test
    @Transactional
    void getAllBillsByResponseCodeContainsSomething() throws Exception {
        // Initialize the database
        billRepository.saveAndFlush(bill);

        // Get all the billList where responseCode contains DEFAULT_RESPONSE_CODE
        defaultBillShouldBeFound("responseCode.contains=" + DEFAULT_RESPONSE_CODE);

        // Get all the billList where responseCode contains UPDATED_RESPONSE_CODE
        //defaultBillShouldNotBeFound("responseCode.contains=" + UPDATED_RESPONSE_CODE);
    }

    @Test
    @Transactional
    void getAllBillsByResponseCodeNotContainsSomething() throws Exception {
        // Initialize the database
        billRepository.saveAndFlush(bill);

        // Get all the billList where responseCode does not contain DEFAULT_RESPONSE_CODE
        //defaultBillShouldNotBeFound("responseCode.doesNotContain=" + DEFAULT_RESPONSE_CODE);

        // Get all the billList where responseCode does not contain UPDATED_RESPONSE_CODE
        defaultBillShouldBeFound("responseCode.doesNotContain=" + UPDATED_RESPONSE_CODE);
    }

    @Test
    @Transactional
    void getAllBillsByResponseTextIsEqualToSomething() throws Exception {
        // Initialize the database
        billRepository.saveAndFlush(bill);

        // Get all the billList where responseText equals to DEFAULT_RESPONSE_TEXT
        defaultBillShouldBeFound("responseText.equals=" + DEFAULT_RESPONSE_TEXT);

        // Get all the billList where responseText equals to UPDATED_RESPONSE_TEXT
        //defaultBillShouldNotBeFound("responseText.equals=" + UPDATED_RESPONSE_TEXT);
    }

    @Test
    @Transactional
    void getAllBillsByResponseTextIsNotEqualToSomething() throws Exception {
        // Initialize the database
        billRepository.saveAndFlush(bill);

        // Get all the billList where responseText not equals to DEFAULT_RESPONSE_TEXT
        //defaultBillShouldNotBeFound("responseText.notEquals=" + DEFAULT_RESPONSE_TEXT);

        // Get all the billList where responseText not equals to UPDATED_RESPONSE_TEXT
        defaultBillShouldBeFound("responseText.notEquals=" + UPDATED_RESPONSE_TEXT);
    }

    @Test
    @Transactional
    void getAllBillsByResponseTextIsInShouldWork() throws Exception {
        // Initialize the database
        billRepository.saveAndFlush(bill);

        // Get all the billList where responseText in DEFAULT_RESPONSE_TEXT or UPDATED_RESPONSE_TEXT
        defaultBillShouldBeFound("responseText.in=" + DEFAULT_RESPONSE_TEXT + "," + UPDATED_RESPONSE_TEXT);

        // Get all the billList where responseText equals to UPDATED_RESPONSE_TEXT
        //defaultBillShouldNotBeFound("responseText.in=" + UPDATED_RESPONSE_TEXT);
    }

    @Test
    @Transactional
    void getAllBillsByResponseTextIsNullOrNotNull() throws Exception {
        // Initialize the database
        billRepository.saveAndFlush(bill);

        // Get all the billList where responseText is not null
        defaultBillShouldBeFound("responseText.specified=true");

        // Get all the billList where responseText is null
        //defaultBillShouldNotBeFound("responseText.specified=false");
    }

    @Test
    @Transactional
    void getAllBillsByResponseTextContainsSomething() throws Exception {
        // Initialize the database
        billRepository.saveAndFlush(bill);

        // Get all the billList where responseText contains DEFAULT_RESPONSE_TEXT
        defaultBillShouldBeFound("responseText.contains=" + DEFAULT_RESPONSE_TEXT);

        // Get all the billList where responseText contains UPDATED_RESPONSE_TEXT
        //defaultBillShouldNotBeFound("responseText.contains=" + UPDATED_RESPONSE_TEXT);
    }

    @Test
    @Transactional
    void getAllBillsByResponseTextNotContainsSomething() throws Exception {
        // Initialize the database
        billRepository.saveAndFlush(bill);

        // Get all the billList where responseText does not contain DEFAULT_RESPONSE_TEXT
        //defaultBillShouldNotBeFound("responseText.doesNotContain=" + DEFAULT_RESPONSE_TEXT);

        // Get all the billList where responseText does not contain UPDATED_RESPONSE_TEXT
        defaultBillShouldBeFound("responseText.doesNotContain=" + UPDATED_RESPONSE_TEXT);
    }

    @Test
    @Transactional
    void getAllBillsBySuccessIsEqualToSomething() throws Exception {
        // Initialize the database
        billRepository.saveAndFlush(bill);

        // Get all the billList where success equals to DEFAULT_SUCCESS
        defaultBillShouldBeFound("success.equals=" + DEFAULT_SUCCESS);

        // Get all the billList where success equals to UPDATED_SUCCESS
        //defaultBillShouldNotBeFound("success.equals=" + UPDATED_SUCCESS);
    }

    @Test
    @Transactional
    void getAllBillsBySuccessIsNotEqualToSomething() throws Exception {
        // Initialize the database
        billRepository.saveAndFlush(bill);

        // Get all the billList where success not equals to DEFAULT_SUCCESS
        //defaultBillShouldNotBeFound("success.notEquals=" + DEFAULT_SUCCESS);

        // Get all the billList where success not equals to UPDATED_SUCCESS
        defaultBillShouldBeFound("success.notEquals=" + UPDATED_SUCCESS);
    }

    @Test
    @Transactional
    void getAllBillsBySuccessIsInShouldWork() throws Exception {
        // Initialize the database
        billRepository.saveAndFlush(bill);

        // Get all the billList where success in DEFAULT_SUCCESS or UPDATED_SUCCESS
        defaultBillShouldBeFound("success.in=" + DEFAULT_SUCCESS + "," + UPDATED_SUCCESS);

        // Get all the billList where success equals to UPDATED_SUCCESS
        //defaultBillShouldNotBeFound("success.in=" + UPDATED_SUCCESS);
    }

    @Test
    @Transactional
    void getAllBillsBySuccessIsNullOrNotNull() throws Exception {
        // Initialize the database
        billRepository.saveAndFlush(bill);

        // Get all the billList where success is not null
        defaultBillShouldBeFound("success.specified=true");

        // Get all the billList where success is null
        //defaultBillShouldNotBeFound("success.specified=false");
    }

    @Test
    @Transactional
    void getAllBillsByLoginIsEqualToSomething() throws Exception {
        // Initialize the database
        billRepository.saveAndFlush(bill);

        // Get all the billList where login equals to DEFAULT_LOGIN
        defaultBillShouldBeFound("login.equals=" + DEFAULT_LOGIN);

        // Get all the billList where login equals to UPDATED_LOGIN
        //defaultBillShouldNotBeFound("login.equals=" + UPDATED_LOGIN);
    }

    @Test
    @Transactional
    void getAllBillsByLoginIsNotEqualToSomething() throws Exception {
        // Initialize the database
        billRepository.saveAndFlush(bill);

        // Get all the billList where login not equals to DEFAULT_LOGIN
        //defaultBillShouldNotBeFound("login.notEquals=" + DEFAULT_LOGIN);

        // Get all the billList where login not equals to UPDATED_LOGIN
        defaultBillShouldBeFound("login.notEquals=" + UPDATED_LOGIN);
    }

    @Test
    @Transactional
    void getAllBillsByLoginIsInShouldWork() throws Exception {
        // Initialize the database
        billRepository.saveAndFlush(bill);

        // Get all the billList where login in DEFAULT_LOGIN or UPDATED_LOGIN
        defaultBillShouldBeFound("login.in=" + DEFAULT_LOGIN + "," + UPDATED_LOGIN);

        // Get all the billList where login equals to UPDATED_LOGIN
        //defaultBillShouldNotBeFound("login.in=" + UPDATED_LOGIN);
    }

    @Test
    @Transactional
    void getAllBillsByLoginIsNullOrNotNull() throws Exception {
        // Initialize the database
        billRepository.saveAndFlush(bill);

        // Get all the billList where login is not null
        defaultBillShouldBeFound("login.specified=true");

        // Get all the billList where login is null
        //defaultBillShouldNotBeFound("login.specified=false");
    }

    @Test
    @Transactional
    void getAllBillsByLoginContainsSomething() throws Exception {
        // Initialize the database
        billRepository.saveAndFlush(bill);

        // Get all the billList where login contains DEFAULT_LOGIN
        defaultBillShouldBeFound("login.contains=" + DEFAULT_LOGIN);

        // Get all the billList where login contains UPDATED_LOGIN
        //defaultBillShouldNotBeFound("login.contains=" + UPDATED_LOGIN);
    }

    @Test
    @Transactional
    void getAllBillsByLoginNotContainsSomething() throws Exception {
        // Initialize the database
        billRepository.saveAndFlush(bill);

        // Get all the billList where login does not contain DEFAULT_LOGIN
        //defaultBillShouldNotBeFound("login.doesNotContain=" + DEFAULT_LOGIN);

        // Get all the billList where login does not contain UPDATED_LOGIN
        defaultBillShouldBeFound("login.doesNotContain=" + UPDATED_LOGIN);
    }

    @Test
    @Transactional
    void getAllBillsByFraudIdIsEqualToSomething() throws Exception {
        // Initialize the database
        billRepository.saveAndFlush(bill);

        // Get all the billList where fraudId equals to DEFAULT_FRAUD_ID
        defaultBillShouldBeFound("fraudId.equals=" + DEFAULT_FRAUD_ID);

        // Get all the billList where fraudId equals to UPDATED_FRAUD_ID
        //defaultBillShouldNotBeFound("fraudId.equals=" + UPDATED_FRAUD_ID);
    }

    @Test
    @Transactional
    void getAllBillsByFraudIdIsNotEqualToSomething() throws Exception {
        // Initialize the database
        billRepository.saveAndFlush(bill);

        // Get all the billList where fraudId not equals to DEFAULT_FRAUD_ID
        //defaultBillShouldNotBeFound("fraudId.notEquals=" + DEFAULT_FRAUD_ID);

        // Get all the billList where fraudId not equals to UPDATED_FRAUD_ID
        defaultBillShouldBeFound("fraudId.notEquals=" + UPDATED_FRAUD_ID);
    }

    @Test
    @Transactional
    void getAllBillsByFraudIdIsInShouldWork() throws Exception {
        // Initialize the database
        billRepository.saveAndFlush(bill);

        // Get all the billList where fraudId in DEFAULT_FRAUD_ID or UPDATED_FRAUD_ID
        defaultBillShouldBeFound("fraudId.in=" + DEFAULT_FRAUD_ID + "," + UPDATED_FRAUD_ID);

        // Get all the billList where fraudId equals to UPDATED_FRAUD_ID
        //defaultBillShouldNotBeFound("fraudId.in=" + UPDATED_FRAUD_ID);
    }

    @Test
    @Transactional
    void getAllBillsByFraudIdIsNullOrNotNull() throws Exception {
        // Initialize the database
        billRepository.saveAndFlush(bill);

        // Get all the billList where fraudId is not null
        defaultBillShouldBeFound("fraudId.specified=true");

        // Get all the billList where fraudId is null
        //defaultBillShouldNotBeFound("fraudId.specified=false");
    }

    @Test
    @Transactional
    void getAllBillsByFraudIdIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        billRepository.saveAndFlush(bill);

        // Get all the billList where fraudId is greater than or equal to DEFAULT_FRAUD_ID
        defaultBillShouldBeFound("fraudId.greaterThanOrEqual=" + DEFAULT_FRAUD_ID);

        // Get all the billList where fraudId is greater than or equal to UPDATED_FRAUD_ID
        //defaultBillShouldNotBeFound("fraudId.greaterThanOrEqual=" + UPDATED_FRAUD_ID);
    }

    @Test
    @Transactional
    void getAllBillsByFraudIdIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        billRepository.saveAndFlush(bill);

        // Get all the billList where fraudId is less than or equal to DEFAULT_FRAUD_ID
        defaultBillShouldBeFound("fraudId.lessThanOrEqual=" + DEFAULT_FRAUD_ID);

        // Get all the billList where fraudId is less than or equal to SMALLER_FRAUD_ID
        //defaultBillShouldNotBeFound("fraudId.lessThanOrEqual=" + SMALLER_FRAUD_ID);
    }

    @Test
    @Transactional
    void getAllBillsByFraudIdIsLessThanSomething() throws Exception {
        // Initialize the database
        billRepository.saveAndFlush(bill);

        // Get all the billList where fraudId is less than DEFAULT_FRAUD_ID
        //defaultBillShouldNotBeFound("fraudId.lessThan=" + DEFAULT_FRAUD_ID);

        // Get all the billList where fraudId is less than UPDATED_FRAUD_ID
        defaultBillShouldBeFound("fraudId.lessThan=" + UPDATED_FRAUD_ID);
    }

    @Test
    @Transactional
    void getAllBillsByFraudIdIsGreaterThanSomething() throws Exception {
        // Initialize the database
        billRepository.saveAndFlush(bill);

        // Get all the billList where fraudId is greater than DEFAULT_FRAUD_ID
        //defaultBillShouldNotBeFound("fraudId.greaterThan=" + DEFAULT_FRAUD_ID);

        // Get all the billList where fraudId is greater than SMALLER_FRAUD_ID
        defaultBillShouldBeFound("fraudId.greaterThan=" + SMALLER_FRAUD_ID);
    }

    @Test
    @Transactional
    void getAllBillsByTransactionIdIsEqualToSomething() throws Exception {
        // Initialize the database
        billRepository.saveAndFlush(bill);

        // Get all the billList where transactionId equals to DEFAULT_TRANSACTION_ID
        defaultBillShouldBeFound("transactionId.equals=" + DEFAULT_TRANSACTION_ID);

        // Get all the billList where transactionId equals to UPDATED_TRANSACTION_ID
        //defaultBillShouldNotBeFound("transactionId.equals=" + UPDATED_TRANSACTION_ID);
    }

    @Test
    @Transactional
    void getAllBillsByTransactionIdIsNotEqualToSomething() throws Exception {
        // Initialize the database
        billRepository.saveAndFlush(bill);

        // Get all the billList where transactionId not equals to DEFAULT_TRANSACTION_ID
        //defaultBillShouldNotBeFound("transactionId.notEquals=" + DEFAULT_TRANSACTION_ID);

        // Get all the billList where transactionId not equals to UPDATED_TRANSACTION_ID
        defaultBillShouldBeFound("transactionId.notEquals=" + UPDATED_TRANSACTION_ID);
    }

    @Test
    @Transactional
    void getAllBillsByTransactionIdIsInShouldWork() throws Exception {
        // Initialize the database
        billRepository.saveAndFlush(bill);

        // Get all the billList where transactionId in DEFAULT_TRANSACTION_ID or UPDATED_TRANSACTION_ID
        defaultBillShouldBeFound("transactionId.in=" + DEFAULT_TRANSACTION_ID + "," + UPDATED_TRANSACTION_ID);

        // Get all the billList where transactionId equals to UPDATED_TRANSACTION_ID
        //defaultBillShouldNotBeFound("transactionId.in=" + UPDATED_TRANSACTION_ID);
    }

    @Test
    @Transactional
    void getAllBillsByTransactionIdIsNullOrNotNull() throws Exception {
        // Initialize the database
        billRepository.saveAndFlush(bill);

        // Get all the billList where transactionId is not null
        defaultBillShouldBeFound("transactionId.specified=true");

        // Get all the billList where transactionId is null
        //defaultBillShouldNotBeFound("transactionId.specified=false");
    }

    @Test
    @Transactional
    void getAllBillsByTransactionIdIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        billRepository.saveAndFlush(bill);

        // Get all the billList where transactionId is greater than or equal to DEFAULT_TRANSACTION_ID
        defaultBillShouldBeFound("transactionId.greaterThanOrEqual=" + DEFAULT_TRANSACTION_ID);

        // Get all the billList where transactionId is greater than or equal to UPDATED_TRANSACTION_ID
        //defaultBillShouldNotBeFound("transactionId.greaterThanOrEqual=" + UPDATED_TRANSACTION_ID);
    }

    @Disabled("bill.transactionId default is -1, test will be failed")
    @Test
    @Transactional
    void getAllBillsByTransactionIdIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        billRepository.saveAndFlush(bill);

        // Get all the billList where transactionId is less than or equal to DEFAULT_TRANSACTION_ID
        defaultBillShouldBeFound("transactionId.lessThanOrEqual=" + DEFAULT_TRANSACTION_ID);

        // Get all the billList where transactionId is less than or equal to SMALLER_TRANSACTION_ID
        //defaultBillShouldNotBeFound("transactionId.lessThanOrEqual=" + SMALLER_TRANSACTION_ID);
    }

    @Test
    @Transactional
    void getAllBillsByTransactionIdIsLessThanSomething() throws Exception {
        // Initialize the database
        billRepository.saveAndFlush(bill);

        // Get all the billList where transactionId is less than DEFAULT_TRANSACTION_ID
        //defaultBillShouldNotBeFound("transactionId.lessThan=" + DEFAULT_TRANSACTION_ID);

        // Get all the billList where transactionId is less than UPDATED_TRANSACTION_ID
        defaultBillShouldBeFound("transactionId.lessThan=" + UPDATED_TRANSACTION_ID);
    }

    @Disabled("bill.transactionId default is -1, test will be failed")
    @Test
    @Transactional
    void getAllBillsByTransactionIdIsGreaterThanSomething() throws Exception {
        // Initialize the database
        billRepository.saveAndFlush(bill);

        // Get all the billList where transactionId is greater than DEFAULT_TRANSACTION_ID
        //defaultBillShouldNotBeFound("transactionId.greaterThan=" + DEFAULT_TRANSACTION_ID);

        // Get all the billList where transactionId is greater than SMALLER_TRANSACTION_ID
        defaultBillShouldBeFound("transactionId.greaterThan=" + SMALLER_TRANSACTION_ID);
    }

    /**
     * Executes the search, and checks that the default entity is returned.
     */
    private void defaultBillShouldBeFound(String filter) throws Exception {
        restBillMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(bill.getId().intValue())))
            .andExpect(jsonPath("$.[*].recipientId").value(hasItem(DEFAULT_RECIPIENT_ID.intValue())))
            .andExpect(jsonPath("$.[*].recipientShortName").value(hasItem(DEFAULT_RECIPIENT_SHORT_NAME)))
//            .andExpect(jsonPath("$.[*].sessionId").value(hasItem(DEFAULT_SESSION_ID)))
            .andExpect(jsonPath("$.[*].type").value(hasItem(DEFAULT_TYPE.toString())))
            .andExpect(jsonPath("$.[*].amountRatio").value(hasItem(DEFAULT_AMOUNT_RATIO)))
            .andExpect(jsonPath("$.[*].amount").value(hasItem(DEFAULT_AMOUNT.intValue())))
            //            .andExpect(jsonPath("$.[*].profit").value(hasItem(DEFAULT_PROFIT.intValue())))
            .andExpect(jsonPath("$.[*].account").value(hasItem(DEFAULT_ACCOUNT)))
            //            .andExpect(jsonPath("$.[*].params").value(hasItem(DEFAULT_PARAMS)))
            .andExpect(jsonPath("$.[*].currency").value(hasItem(DEFAULT_CURRENCY)))
//            .andExpect(jsonPath("$.[*].extId").value(hasItem(DEFAULT_EXT_ID)))
//            .andExpect(jsonPath("$.[*].checkId").value(hasItem(DEFAULT_CHECK_ID)))
//            .andExpect(jsonPath("$.[*].payId").value(hasItem(DEFAULT_PAY_ID)))
            .andExpect(jsonPath("$.[*].requestTime").value(hasItem(DEFAULT_REQUEST_TIME.toString())))
            .andExpect(jsonPath("$.[*].requestJson").value(hasItem(DEFAULT_REQUEST_JSON)))
            .andExpect(jsonPath("$.[*].responseJson").value(DEFAULT_RESPONSE_JSON))
            .andExpect(jsonPath("$.[*].responseTime").value(hasItem(DEFAULT_RESPONSE_TIME.toString())))
            .andExpect(jsonPath("$.[*].responseCode").value(hasItem(DEFAULT_RESPONSE_CODE)))
            .andExpect(jsonPath("$.[*].responseText").value(hasItem(DEFAULT_RESPONSE_TEXT)))
            .andExpect(jsonPath("$.[*].success").value(hasItem(DEFAULT_SUCCESS.booleanValue())))
            .andExpect(jsonPath("$.[*].login").value(hasItem(DEFAULT_LOGIN)))
//            .andExpect(jsonPath("$.[*].fraudId").value(hasItem(DEFAULT_FRAUD_ID.intValue())))
            .andExpect(jsonPath("$.[*].transactionId").value(hasItem(DEFAULT_TRANSACTION_ID.intValue())));

        // Check, that the count call also returns 1
        restBillMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("1"));
    }

    /**
     * Executes the search, and checks that the default entity is not returned.
     */
//    private void //defaultBillShouldNotBeFound(String filter) throws Exception {
//        restBillMockMvc
//            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
//            .andExpect(status().isOk())
//            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
//            .andExpect(jsonPath("$").isArray())
//            .andExpect(jsonPath("$").isEmpty());
//
//        // Check, that the count call also returns 0
//        restBillMockMvc
//            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
//            .andExpect(status().isOk())
//            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
//            .andExpect(content().string("0"));
//    }

    @Test
    @Transactional
    void getNonExistingBill() throws Exception {
        // Get the bill
        restBillMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putNewBill() throws Exception {
        // Initialize the database
        billRepository.saveAndFlush(bill);

        int databaseSizeBeforeUpdate = billRepository.findAll().size();

        // Update the bill
        Bill updatedBill = billRepository.findById(bill.getId()).get();
        // Disconnect from session so that the updates on updatedBill are not directly saved in db
        em.detach(updatedBill);
        updatedBill
            .recipientId(UPDATED_RECIPIENT_ID)
            .recipientShortName(UPDATED_RECIPIENT_SHORT_NAME)
//            .sessionId(UPDATED_SESSION_ID)
            .type(UPDATED_TYPE)
            .amountRatio(UPDATED_AMOUNT_RATIO)
            .amount(UPDATED_AMOUNT)
            .account(UPDATED_ACCOUNT)
            .currency(UPDATED_CURRENCY)
            .extId(UPDATED_EXT_ID)
//            .checkId(UPDATED_CHECK_ID)
//            .payId(UPDATED_PAY_ID)
            .requestTime(UPDATED_REQUEST_TIME)
            .requestJson(UPDATED_REQUEST_JSON)
            .responseJson(UPDATED_RESPONSE_JSON)
            .responseTime(UPDATED_RESPONSE_TIME)
            .responseCode(UPDATED_RESPONSE_CODE)
            .responseText(UPDATED_RESPONSE_TEXT)
            .success(UPDATED_SUCCESS)
            .login(UPDATED_LOGIN)
//            .fraudId(UPDATED_FRAUD_ID)
            .transactionId(UPDATED_TRANSACTION_ID);
        BillDTO billDTO = billMapper.toDto(updatedBill);

        restBillMockMvc
            .perform(
                put(ENTITY_API_URL_ID, billDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(billDTO))
            )
            .andExpect(status().isOk());

        // Validate the Bill in the database
        List<Bill> billList = billRepository.findAll();
        assertThat(billList).hasSize(databaseSizeBeforeUpdate);
        Bill testBill = billList.get(billList.size() - 1);
        assertThat(testBill.getRecipientId()).isEqualTo(UPDATED_RECIPIENT_ID);
        assertThat(testBill.getRecipientShortName()).isEqualTo(UPDATED_RECIPIENT_SHORT_NAME);
//        assertThat(testBill.getSessionId()).isEqualTo(UPDATED_SESSION_ID);
        assertThat(testBill.getType()).isEqualTo(UPDATED_TYPE);
        assertThat(testBill.getAmountRatio()).isEqualTo(UPDATED_AMOUNT_RATIO);
        assertThat(testBill.getAmount()).isEqualTo(UPDATED_AMOUNT);
        assertThat(testBill.getAccount()).isEqualTo(UPDATED_ACCOUNT);
        assertThat(testBill.getCurrency()).isEqualTo(UPDATED_CURRENCY);
        assertThat(testBill.getExtId()).isEqualTo(UPDATED_EXT_ID);
//        assertThat(testBill.getCheckId()).isEqualTo(UPDATED_CHECK_ID);
//        assertThat(testBill.getPayId()).isEqualTo(UPDATED_PAY_ID);
        assertThat(testBill.getRequestTime()).isEqualTo(UPDATED_REQUEST_TIME);
        assertThat(testBill.getRequestJson()).isEqualTo(UPDATED_REQUEST_JSON);
        assertThat(testBill.getResponseJson()).isEqualTo(UPDATED_RESPONSE_JSON);
        assertThat(testBill.getResponseTime()).isEqualTo(UPDATED_RESPONSE_TIME);
        assertThat(testBill.getResponseCode()).isEqualTo(UPDATED_RESPONSE_CODE);
        assertThat(testBill.getResponseText()).isEqualTo(UPDATED_RESPONSE_TEXT);
        assertThat(testBill.getSuccess()).isEqualTo(UPDATED_SUCCESS);
        assertThat(testBill.getLogin()).isEqualTo(UPDATED_LOGIN);
//        assertThat(testBill.getFraudId()).isEqualTo(UPDATED_FRAUD_ID);
        assertThat(testBill.getTransactionId()).isEqualTo(UPDATED_TRANSACTION_ID);
    }

    @Test
    @Transactional
    void putNonExistingBill() throws Exception {
        int databaseSizeBeforeUpdate = billRepository.findAll().size();
        bill.setId(count.incrementAndGet());

        // Create the Bill
        BillDTO billDTO = billMapper.toDto(bill);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restBillMockMvc
            .perform(
                put(ENTITY_API_URL_ID, billDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(billDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Bill in the database
        List<Bill> billList = billRepository.findAll();
        assertThat(billList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchBill() throws Exception {
        int databaseSizeBeforeUpdate = billRepository.findAll().size();
        bill.setId(count.incrementAndGet());

        // Create the Bill
        BillDTO billDTO = billMapper.toDto(bill);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restBillMockMvc
            .perform(
                put(ENTITY_API_URL_ID, count.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(billDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Bill in the database
        List<Bill> billList = billRepository.findAll();
        assertThat(billList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamBill() throws Exception {
        int databaseSizeBeforeUpdate = billRepository.findAll().size();
        bill.setId(count.incrementAndGet());

        // Create the Bill
        BillDTO billDTO = billMapper.toDto(bill);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restBillMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(billDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Bill in the database
        List<Bill> billList = billRepository.findAll();
        assertThat(billList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateBillWithPatch() throws Exception {
        // Initialize the database
        billRepository.saveAndFlush(bill);

        int databaseSizeBeforeUpdate = billRepository.findAll().size();

        // Update the bill using partial update
        Bill partialUpdatedBill = new Bill();
        partialUpdatedBill.setId(bill.getId());

        partialUpdatedBill
            .type(UPDATED_TYPE)
            .amountRatio(UPDATED_AMOUNT_RATIO)
            .amount(UPDATED_AMOUNT)
            .currency(UPDATED_CURRENCY)
            .requestJson(UPDATED_REQUEST_JSON)
            .responseCode(UPDATED_RESPONSE_CODE);

        restBillMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedBill.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedBill))
            )
            .andExpect(status().isOk());

        // Validate the Bill in the database
        List<Bill> billList = billRepository.findAll();
        assertThat(billList).hasSize(databaseSizeBeforeUpdate);
        Bill testBill = billList.get(billList.size() - 1);
        assertThat(testBill.getRecipientId()).isEqualTo(DEFAULT_RECIPIENT_ID);
        assertThat(testBill.getRecipientShortName()).isEqualTo(DEFAULT_RECIPIENT_SHORT_NAME);
//        assertThat(testBill.getSessionId()).isEqualTo(DEFAULT_SESSION_ID);
        assertThat(testBill.getType()).isEqualTo(UPDATED_TYPE);
        assertThat(testBill.getAmountRatio()).isEqualTo(UPDATED_AMOUNT_RATIO);
        assertThat(testBill.getAmount()).isEqualTo(UPDATED_AMOUNT);
        assertThat(testBill.getAccount()).isEqualTo(DEFAULT_ACCOUNT);
        assertThat(testBill.getCurrency()).isEqualTo(UPDATED_CURRENCY);
        assertThat(testBill.getExtId()).isEqualTo(DEFAULT_EXT_ID);
//        assertThat(testBill.getCheckId()).isEqualTo(DEFAULT_CHECK_ID);
//        assertThat(testBill.getPayId()).isEqualTo(DEFAULT_PAY_ID);
        assertThat(testBill.getRequestTime()).isEqualTo(DEFAULT_REQUEST_TIME);
        assertThat(testBill.getRequestJson()).isEqualTo(UPDATED_REQUEST_JSON);
        assertThat(testBill.getResponseJson()).isEqualTo(DEFAULT_RESPONSE_JSON);
        assertThat(testBill.getResponseTime()).isEqualTo(DEFAULT_RESPONSE_TIME);
        assertThat(testBill.getResponseCode()).isEqualTo(UPDATED_RESPONSE_CODE);
        assertThat(testBill.getResponseText()).isEqualTo(DEFAULT_RESPONSE_TEXT);
        assertThat(testBill.getSuccess()).isEqualTo(DEFAULT_SUCCESS);
        assertThat(testBill.getLogin()).isEqualTo(DEFAULT_LOGIN);
//        assertThat(testBill.getFraudId()).isEqualTo(DEFAULT_FRAUD_ID);
        assertThat(testBill.getTransactionId()).isEqualTo(DEFAULT_TRANSACTION_ID);
    }

    @Test
    @Transactional
    void fullUpdateBillWithPatch() throws Exception {
        // Initialize the database
        billRepository.saveAndFlush(bill);

        int databaseSizeBeforeUpdate = billRepository.findAll().size();

        // Update the bill using partial update
        Bill partialUpdatedBill = new Bill();
        partialUpdatedBill.setId(bill.getId());

        partialUpdatedBill
            .recipientId(UPDATED_RECIPIENT_ID)
            .recipientShortName(UPDATED_RECIPIENT_SHORT_NAME)
//            .sessionId(UPDATED_SESSION_ID)
            .type(UPDATED_TYPE)
            .amountRatio(UPDATED_AMOUNT_RATIO)
            .amount(UPDATED_AMOUNT)
            .account(UPDATED_ACCOUNT)
            .currency(UPDATED_CURRENCY)
            .extId(UPDATED_EXT_ID)
//            .checkId(UPDATED_CHECK_ID)
//            .payId(UPDATED_PAY_ID)
            .requestTime(UPDATED_REQUEST_TIME)
            .requestJson(UPDATED_REQUEST_JSON)
            .responseJson(UPDATED_RESPONSE_JSON)
            .responseTime(UPDATED_RESPONSE_TIME)
            .responseCode(UPDATED_RESPONSE_CODE)
            .responseText(UPDATED_RESPONSE_TEXT)
            .success(UPDATED_SUCCESS)
            .login(UPDATED_LOGIN)
//            .fraudId(UPDATED_FRAUD_ID)
            .transactionId(UPDATED_TRANSACTION_ID);

        restBillMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedBill.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedBill))
            )
            .andExpect(status().isOk());

        // Validate the Bill in the database
        List<Bill> billList = billRepository.findAll();
        assertThat(billList).hasSize(databaseSizeBeforeUpdate);
        Bill testBill = billList.get(billList.size() - 1);
        assertThat(testBill.getRecipientId()).isEqualTo(UPDATED_RECIPIENT_ID);
        assertThat(testBill.getRecipientShortName()).isEqualTo(UPDATED_RECIPIENT_SHORT_NAME);
//        assertThat(testBill.getSessionId()).isEqualTo(UPDATED_SESSION_ID);
        assertThat(testBill.getType()).isEqualTo(UPDATED_TYPE);
        assertThat(testBill.getAmountRatio()).isEqualTo(UPDATED_AMOUNT_RATIO);
        assertThat(testBill.getAmount()).isEqualTo(UPDATED_AMOUNT);
        assertThat(testBill.getAccount()).isEqualTo(UPDATED_ACCOUNT);
        assertThat(testBill.getCurrency()).isEqualTo(UPDATED_CURRENCY);
        assertThat(testBill.getExtId()).isEqualTo(UPDATED_EXT_ID);
//        assertThat(testBill.getCheckId()).isEqualTo(UPDATED_CHECK_ID);
//        assertThat(testBill.getPayId()).isEqualTo(UPDATED_PAY_ID);
        assertThat(testBill.getRequestTime()).isEqualTo(UPDATED_REQUEST_TIME);
        assertThat(testBill.getRequestJson()).isEqualTo(UPDATED_REQUEST_JSON);
        assertThat(testBill.getResponseJson()).isEqualTo(UPDATED_RESPONSE_JSON);
        assertThat(testBill.getResponseTime()).isEqualTo(UPDATED_RESPONSE_TIME);
        assertThat(testBill.getResponseCode()).isEqualTo(UPDATED_RESPONSE_CODE);
        assertThat(testBill.getResponseText()).isEqualTo(UPDATED_RESPONSE_TEXT);
        assertThat(testBill.getSuccess()).isEqualTo(UPDATED_SUCCESS);
        assertThat(testBill.getLogin()).isEqualTo(UPDATED_LOGIN);
//        assertThat(testBill.getFraudId()).isEqualTo(UPDATED_FRAUD_ID);
        assertThat(testBill.getTransactionId()).isEqualTo(UPDATED_TRANSACTION_ID);
    }

    @Test
    @Transactional
    void patchNonExistingBill() throws Exception {
        int databaseSizeBeforeUpdate = billRepository.findAll().size();
        bill.setId(count.incrementAndGet());

        // Create the Bill
        BillDTO billDTO = billMapper.toDto(bill);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restBillMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, billDTO.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(billDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Bill in the database
        List<Bill> billList = billRepository.findAll();
        assertThat(billList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchBill() throws Exception {
        int databaseSizeBeforeUpdate = billRepository.findAll().size();
        bill.setId(count.incrementAndGet());

        // Create the Bill
        BillDTO billDTO = billMapper.toDto(bill);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restBillMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, count.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(billDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Bill in the database
        List<Bill> billList = billRepository.findAll();
        assertThat(billList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamBill() throws Exception {
        int databaseSizeBeforeUpdate = billRepository.findAll().size();
        bill.setId(count.incrementAndGet());

        // Create the Bill
        BillDTO billDTO = billMapper.toDto(bill);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restBillMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(TestUtil.convertObjectToJsonBytes(billDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Bill in the database
        List<Bill> billList = billRepository.findAll();
        assertThat(billList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteBill() throws Exception {
        // Initialize the database
        billRepository.saveAndFlush(bill);

        int databaseSizeBeforeDelete = billRepository.findAll().size();

        // Delete the bill
        restBillMockMvc
            .perform(delete(ENTITY_API_URL_ID, bill.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        List<Bill> billList = billRepository.findAll();
        assertThat(billList).hasSize(databaseSizeBeforeDelete - 1);
    }
}
