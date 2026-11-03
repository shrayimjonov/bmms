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
import uz.devops.bmcore.enumeration.RequestType;
import uz.paykap.IntegrationTest;
import uz.paykap.domain.Bill;
import uz.paykap.repository.BillRepository;
import uz.paykap.service.BillQueryService;
import uz.paykap.service.BillService;
import uz.paykap.service.dto.BillDTO;
import uz.paykap.service.mapper.BillMapper;
import uz.paykap.web.rest.errors.ExceptionTranslator;

import javax.persistence.EntityManager;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static uz.paykap.web.rest.TestUtil.createFormattingConversionService;

/**
 * Test class for the BillResource REST controller.
 *
 * @see BillResource
 */
@RunWith(SpringRunner.class)
@IntegrationTest
public class BillResourceIntTest {

    private static final Long DEFAULT_RECIPIENT_ID = 1L;
    private static final Long UPDATED_RECIPIENT_ID = 2L;

    private static final String DEFAULT_RECIPIENT_SHORT_NAME = "AAAAAAAAAA";
    private static final String UPDATED_RECIPIENT_SHORT_NAME = "BBBBBBBBBB";

    private static final String DEFAULT_SESSION_ID = "AAAAAAAAAA";
    private static final String UPDATED_SESSION_ID = "BBBBBBBBBB";

    private static final RequestType DEFAULT_TYPE = RequestType.CHECK;
    private static final RequestType UPDATED_TYPE = RequestType.PAY;

    private static final Integer DEFAULT_AMOUNT_RATIO = 1;
    private static final Integer UPDATED_AMOUNT_RATIO = 2;

    private static final Long DEFAULT_AMOUNT = 1L;
    private static final Long UPDATED_AMOUNT = 2L;

    private static final Long DEFAULT_PROFIT = 1L;
    private static final Long UPDATED_PROFIT = 2L;

    private static final String DEFAULT_ACCOUNT = "AAAAAAAAAA";
    private static final String UPDATED_ACCOUNT = "BBBBBBBBBB";

    private static final String DEFAULT_PARAMS = "AAAAAAAAAA";
    private static final String UPDATED_PARAMS = "BBBBBBBBBB";

    private static final Map<String, Object> DEFAULT_PARAMS_JSON = Collections.singletonMap("AAAAAAAAAA", "AAAAAAAAAA");
    private static final Map<String, Object> UPDATED_PARAMS_JSON = Collections.singletonMap("BBBBBBBBBB", "BBBBBBBBBB");

    private static final Integer DEFAULT_CURRENCY = 1;
    private static final Integer UPDATED_CURRENCY = 2;

    private static final String DEFAULT_EXT_ID = "AAAAAAAAAA";
    private static final String UPDATED_EXT_ID = "BBBBBBBBBB";

    private static final String DEFAULT_CHECK_ID = "AAAAAAAAAA";
    private static final String UPDATED_CHECK_ID = "BBBBBBBBBB";

    private static final String DEFAULT_PAY_ID = "AAAAAAAAAA";
    private static final String UPDATED_PAY_ID = "BBBBBBBBBB";

    private static final Instant DEFAULT_REQUEST_TIME = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_REQUEST_TIME = Instant.now().truncatedTo(ChronoUnit.MILLIS);

    private static final Map<String, Object> DEFAULT_REQUEST_JSON = Collections.singletonMap("AAAAAAAAAA", "AAAAAAAAAA");
    private static final Map<String, Object> UPDATED_REQUEST_JSON = Collections.singletonMap("BBBBBBBBBB", "BBBBBBBBBB");

    private static final Map<String, Object> DEFAULT_RESPONSE_JSON = Collections.singletonMap("AAAAAAAAAA", "AAAAAAAAAA");
    private static final Map<String, Object> UPDATED_RESPONSE_JSON = Collections.singletonMap("BBBBBBBBBB", "BBBBBBBBBB");

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

    private static final Long DEFAULT_TRANSACTION_ID = -1L;
    private static final Long UPDATED_TRANSACTION_ID = 0L;

    @Autowired
    private BillRepository billRepository;

    @Autowired
    private BillMapper billMapper;

    @Autowired
    private BillService billService;

    @Autowired
    private BillQueryService billQueryService;

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
    private JaversEntityAuditResource javersEntityAuditResource;

    private MockMvc restBillMockMvc;

    private Bill bill;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        final BillResource billResource = new BillResource(billService, billRepository, billQueryService);
        this.restBillMockMvc =
            MockMvcBuilders
                .standaloneSetup(billResource)
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
    public static Bill createEntity(EntityManager em) {
        return createEntity(em, DEFAULT_LOGIN);
    }

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Bill createEntity(EntityManager em, String login) {
        Bill bill = new Bill()
            .recipientId(DEFAULT_RECIPIENT_ID)
            .recipientShortName(DEFAULT_RECIPIENT_SHORT_NAME)
//            .sessionId(DEFAULT_SESSION_ID)
            .type(DEFAULT_TYPE)
            .amountRatio(DEFAULT_AMOUNT_RATIO)
            .amount(DEFAULT_AMOUNT)
            //            .profit(DEFAULT_PROFIT)
            .account(DEFAULT_ACCOUNT)
            .requestJson(DEFAULT_PARAMS_JSON)
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
            .login(login)
//            .fraudId(DEFAULT_FRAUD_ID)
            .transactionId(DEFAULT_TRANSACTION_ID);
        return bill;
    }

    @Before
    public void initTest() {
        bill = createEntity(em);
    }

    @Test
    @Transactional
    public void createBill() throws Exception {
        int databaseSizeBeforeCreate = billRepository.findAll().size();

        // Create the Bill
        BillDTO billDTO = billMapper.toDto(bill);
        restBillMockMvc
            .perform(post("/api/bills").contentType(TestUtil.APPLICATION_JSON_UTF8).content(TestUtil.convertObjectToJsonBytes(billDTO)))
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
//        assertThat(testBill.getParams()).isEqualTo(DEFAULT_PARAMS_JSON);
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
        assertThat(testBill.isSuccess()).isEqualTo(DEFAULT_SUCCESS);
        assertThat(testBill.getLogin()).isEqualTo(DEFAULT_LOGIN);
//        assertThat(testBill.getFraudId()).isEqualTo(DEFAULT_FRAUD_ID);
        assertThat(testBill.getTransactionId()).isEqualTo(DEFAULT_TRANSACTION_ID);
    }

    @Test
    @Transactional
    public void createBillWithExistingId() throws Exception {
        int databaseSizeBeforeCreate = billRepository.findAll().size();

        // Create the Bill with an existing ID
        bill.setId(1L);
        BillDTO billDTO = billMapper.toDto(bill);

        // An entity with an existing ID cannot be created, so this API call must fail
        restBillMockMvc
            .perform(post("/api/bills").contentType(TestUtil.APPLICATION_JSON_UTF8).content(TestUtil.convertObjectToJsonBytes(billDTO)))
            .andExpect(status().isBadRequest());

        // Validate the Bill in the database
        List<Bill> billList = billRepository.findAll();
        assertThat(billList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    public void checkRecipientShortNameIsRequired() throws Exception {
        int databaseSizeBeforeTest = billRepository.findAll().size();
        // set the field null
        bill.setRecipientShortName(null);

        // Create the Bill, which fails.
        BillDTO billDTO = billMapper.toDto(bill);

        restBillMockMvc
            .perform(post("/api/bills").contentType(TestUtil.APPLICATION_JSON_UTF8).content(TestUtil.convertObjectToJsonBytes(billDTO)))
            .andExpect(status().isBadRequest());

        List<Bill> billList = billRepository.findAll();
        assertThat(billList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkTypeIsRequired() throws Exception {
        int databaseSizeBeforeTest = billRepository.findAll().size();
        // set the field null
        bill.setType(null);

        // Create the Bill, which fails.
        BillDTO billDTO = billMapper.toDto(bill);

        restBillMockMvc
            .perform(post("/api/bills").contentType(TestUtil.APPLICATION_JSON_UTF8).content(TestUtil.convertObjectToJsonBytes(billDTO)))
            .andExpect(status().isBadRequest());

        List<Bill> billList = billRepository.findAll();
        assertThat(billList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkAmountRatioIsRequired() throws Exception {
        int databaseSizeBeforeTest = billRepository.findAll().size();
        // set the field null
        bill.setAmountRatio(null);

        // Create the Bill, which fails.
        BillDTO billDTO = billMapper.toDto(bill);

        restBillMockMvc
            .perform(post("/api/bills").contentType(TestUtil.APPLICATION_JSON_UTF8).content(TestUtil.convertObjectToJsonBytes(billDTO)))
            .andExpect(status().isBadRequest());

        List<Bill> billList = billRepository.findAll();
        assertThat(billList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkAmountIsRequired() throws Exception {
        int databaseSizeBeforeTest = billRepository.findAll().size();
        // set the field null
        bill.setAmount(null);

        // Create the Bill, which fails.
        BillDTO billDTO = billMapper.toDto(bill);

        restBillMockMvc
            .perform(post("/api/bills").contentType(TestUtil.APPLICATION_JSON_UTF8).content(TestUtil.convertObjectToJsonBytes(billDTO)))
            .andExpect(status().isBadRequest());

        List<Bill> billList = billRepository.findAll();
        assertThat(billList).hasSize(databaseSizeBeforeTest);
    }

    //    @Test
    @Transactional
    public void checkProfitIsRequired() throws Exception {
        int databaseSizeBeforeTest = billRepository.findAll().size();
        // set the field null
        //        bill.setProfit(null);

        // Create the Bill, which fails.
        BillDTO billDTO = billMapper.toDto(bill);

        restBillMockMvc
            .perform(post("/api/bills").contentType(TestUtil.APPLICATION_JSON_UTF8).content(TestUtil.convertObjectToJsonBytes(billDTO)))
            .andExpect(status().isBadRequest());

        List<Bill> billList = billRepository.findAll();
        assertThat(billList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkAccountIsRequired() throws Exception {
        int databaseSizeBeforeTest = billRepository.findAll().size();
        // set the field null
        bill.setAccount(null);

        // Create the Bill, which fails.
        BillDTO billDTO = billMapper.toDto(bill);

        restBillMockMvc
            .perform(post("/api/bills").contentType(TestUtil.APPLICATION_JSON_UTF8).content(TestUtil.convertObjectToJsonBytes(billDTO)))
            .andExpect(status().isBadRequest());

        List<Bill> billList = billRepository.findAll();
        assertThat(billList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkRequestTimeIsRequired() throws Exception {
        int databaseSizeBeforeTest = billRepository.findAll().size();
        // set the field null
        bill.setRequestTime(null);

        // Create the Bill, which fails.
        BillDTO billDTO = billMapper.toDto(bill);

        restBillMockMvc
            .perform(post("/api/bills").contentType(TestUtil.APPLICATION_JSON_UTF8).content(TestUtil.convertObjectToJsonBytes(billDTO)))
            .andExpect(status().isBadRequest());

        List<Bill> billList = billRepository.findAll();
        assertThat(billList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkResponseTimeIsRequired() throws Exception {
        int databaseSizeBeforeTest = billRepository.findAll().size();
        // set the field null
        bill.setResponseTime(null);

        // Create the Bill, which fails.
        BillDTO billDTO = billMapper.toDto(bill);

        restBillMockMvc
            .perform(post("/api/bills").contentType(TestUtil.APPLICATION_JSON_UTF8).content(TestUtil.convertObjectToJsonBytes(billDTO)))
            .andExpect(status().isBadRequest());

        List<Bill> billList = billRepository.findAll();
        assertThat(billList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void getAllBills() throws Exception {
        // Initialize the database
        billRepository.saveAndFlush(bill);

        // Get all the billList
        restBillMockMvc
            .perform(get("/api/bills?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(bill.getId().intValue())))
            .andExpect(jsonPath("$.[*].recipientId").value(hasItem(DEFAULT_RECIPIENT_ID.intValue())))
            .andExpect(jsonPath("$.[*].recipientShortName").value(hasItem(DEFAULT_RECIPIENT_SHORT_NAME.toString())))
            .andExpect(jsonPath("$.[*].sessionId").value(hasItem(DEFAULT_SESSION_ID.toString())))
            .andExpect(jsonPath("$.[*].type").value(hasItem(DEFAULT_TYPE.toString())))
            .andExpect(jsonPath("$.[*].amountRatio").value(hasItem(DEFAULT_AMOUNT_RATIO)))
            .andExpect(jsonPath("$.[*].amount").value(hasItem(DEFAULT_AMOUNT.intValue())))
            //            .andExpect(jsonPath("$.[*].profit").value(hasItem(DEFAULT_PROFIT.intValue())))
            .andExpect(jsonPath("$.[*].account").value(hasItem(DEFAULT_ACCOUNT.toString())))
            //            .andExpect(jsonPath("$.[*].params").value(hasItem(DEFAULT_PARAMS_JSON)))
            .andExpect(jsonPath("$.[*].currency").value(hasItem(DEFAULT_CURRENCY)))
            .andExpect(jsonPath("$.[*].extId").value(hasItem(DEFAULT_EXT_ID.toString())))
            .andExpect(jsonPath("$.[*].checkId").value(hasItem(DEFAULT_CHECK_ID.toString())))
            .andExpect(jsonPath("$.[*].payId").value(hasItem(DEFAULT_PAY_ID.toString())))
            .andExpect(jsonPath("$.[*].requestTime").value(hasItem(DEFAULT_REQUEST_TIME.toString())))
            .andExpect(jsonPath("$.[*].requestJson").value(hasItem(DEFAULT_REQUEST_JSON)))
            .andExpect(jsonPath("$.[*].responseJson").value(hasItem(DEFAULT_RESPONSE_JSON)))
            .andExpect(jsonPath("$.[*].responseTime").value(hasItem(DEFAULT_RESPONSE_TIME.toString())))
            .andExpect(jsonPath("$.[*].responseCode").value(hasItem(DEFAULT_RESPONSE_CODE.toString())))
            .andExpect(jsonPath("$.[*].responseText").value(hasItem(DEFAULT_RESPONSE_TEXT.toString())))
            .andExpect(jsonPath("$.[*].success").value(hasItem(DEFAULT_SUCCESS.booleanValue())))
            .andExpect(jsonPath("$.[*].login").value(hasItem(DEFAULT_LOGIN.toString())))
            .andExpect(jsonPath("$.[*].fraudId").value(hasItem(DEFAULT_FRAUD_ID.intValue())))
            .andExpect(jsonPath("$.[*].transactionId").value(hasItem(DEFAULT_TRANSACTION_ID.intValue())));
    }

    @Test
    @Transactional
    public void getBill() throws Exception {
        // Initialize the database
        billRepository.saveAndFlush(bill);

        // Get the bill
        restBillMockMvc
            .perform(get("/api/bills/{id}", bill.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.id").value(bill.getId().intValue()))
            .andExpect(jsonPath("$.recipientId").value(DEFAULT_RECIPIENT_ID.intValue()))
            .andExpect(jsonPath("$.recipientShortName").value(DEFAULT_RECIPIENT_SHORT_NAME.toString()))
            .andExpect(jsonPath("$.sessionId").value(DEFAULT_SESSION_ID.toString()))
            .andExpect(jsonPath("$.type").value(DEFAULT_TYPE.toString()))
            .andExpect(jsonPath("$.amountRatio").value(DEFAULT_AMOUNT_RATIO))
            .andExpect(jsonPath("$.amount").value(DEFAULT_AMOUNT.intValue()))
            //            .andExpect(jsonPath("$.profit").value(DEFAULT_PROFIT.intValue()))
            .andExpect(jsonPath("$.account").value(DEFAULT_ACCOUNT.toString()))
            //            .andExpect(jsonPath("$.params").exists())
            .andExpect(jsonPath("$.currency").value(DEFAULT_CURRENCY))
            .andExpect(jsonPath("$.extId").value(DEFAULT_EXT_ID.toString()))
            .andExpect(jsonPath("$.checkId").value(DEFAULT_CHECK_ID.toString()))
            .andExpect(jsonPath("$.payId").value(DEFAULT_PAY_ID.toString()))
            .andExpect(jsonPath("$.requestTime").value(DEFAULT_REQUEST_TIME.toString()))
            .andExpect(jsonPath("$.requestJson").exists())
            .andExpect(jsonPath("$.responseJson").exists())
            .andExpect(jsonPath("$.responseTime").value(DEFAULT_RESPONSE_TIME.toString()))
            .andExpect(jsonPath("$.responseCode").value(DEFAULT_RESPONSE_CODE.toString()))
            .andExpect(jsonPath("$.responseText").value(DEFAULT_RESPONSE_TEXT.toString()))
            .andExpect(jsonPath("$.success").value(DEFAULT_SUCCESS.booleanValue()))
            .andExpect(jsonPath("$.login").value(DEFAULT_LOGIN.toString()))
            .andExpect(jsonPath("$.fraudId").value(DEFAULT_FRAUD_ID.intValue()))
            .andExpect(jsonPath("$.transactionId").value(DEFAULT_TRANSACTION_ID.intValue()));
    }

    @Test
    @Transactional
    public void getAllBillsByRecipientIdIsEqualToSomething() throws Exception {
        // Initialize the database
        billRepository.saveAndFlush(bill);

        // Get all the billList where recipientId equals to DEFAULT_RECIPIENT_ID
        defaultBillShouldBeFound("recipientId.equals=" + DEFAULT_RECIPIENT_ID);

        // Get all the billList where recipientId equals to UPDATED_RECIPIENT_ID
        defaultBillShouldNotBeFound("recipientId.equals=" + UPDATED_RECIPIENT_ID);
    }

    @Test
    @Transactional
    public void getAllBillsByRecipientIdIsInShouldWork() throws Exception {
        // Initialize the database
        billRepository.saveAndFlush(bill);

        // Get all the billList where recipientId in DEFAULT_RECIPIENT_ID or UPDATED_RECIPIENT_ID
        defaultBillShouldBeFound("recipientId.in=" + DEFAULT_RECIPIENT_ID + "," + UPDATED_RECIPIENT_ID);

        // Get all the billList where recipientId equals to UPDATED_RECIPIENT_ID
        defaultBillShouldNotBeFound("recipientId.in=" + UPDATED_RECIPIENT_ID);
    }

    @Test
    @Transactional
    public void getAllBillsByRecipientIdIsNullOrNotNull() throws Exception {
        // Initialize the database
        billRepository.saveAndFlush(bill);

        // Get all the billList where recipientId is not null
        defaultBillShouldBeFound("recipientId.specified=true");

        // Get all the billList where recipientId is null
        defaultBillShouldNotBeFound("recipientId.specified=false");
    }

    @Test
    @Transactional
    public void getAllBillsByRecipientIdIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        billRepository.saveAndFlush(bill);

        // Get all the billList where recipientId greater than or equals to DEFAULT_RECIPIENT_ID
        defaultBillShouldBeFound("recipientId.greaterOrEqualThan=" + DEFAULT_RECIPIENT_ID);

        // Get all the billList where recipientId greater than or equals to UPDATED_RECIPIENT_ID
        defaultBillShouldNotBeFound("recipientId.greaterOrEqualThan=" + UPDATED_RECIPIENT_ID);
    }

    @Test
    @Transactional
    public void getAllBillsByRecipientIdIsLessThanSomething() throws Exception {
        // Initialize the database
        billRepository.saveAndFlush(bill);

        // Get all the billList where recipientId less than or equals to DEFAULT_RECIPIENT_ID
        defaultBillShouldNotBeFound("recipientId.lessThan=" + DEFAULT_RECIPIENT_ID);

        // Get all the billList where recipientId less than or equals to UPDATED_RECIPIENT_ID
        defaultBillShouldBeFound("recipientId.lessThan=" + UPDATED_RECIPIENT_ID);
    }

    @Test
    @Transactional
    public void getAllBillsByRecipientShortNameIsEqualToSomething() throws Exception {
        // Initialize the database
        billRepository.saveAndFlush(bill);

        // Get all the billList where recipientShortName equals to DEFAULT_RECIPIENT_SHORT_NAME
        defaultBillShouldBeFound("recipientShortName.equals=" + DEFAULT_RECIPIENT_SHORT_NAME);

        // Get all the billList where recipientShortName equals to UPDATED_RECIPIENT_SHORT_NAME
        defaultBillShouldNotBeFound("recipientShortName.equals=" + UPDATED_RECIPIENT_SHORT_NAME);
    }

    @Test
    @Transactional
    public void getAllBillsByRecipientShortNameIsInShouldWork() throws Exception {
        // Initialize the database
        billRepository.saveAndFlush(bill);

        // Get all the billList where recipientShortName in DEFAULT_RECIPIENT_SHORT_NAME or UPDATED_RECIPIENT_SHORT_NAME
        defaultBillShouldBeFound("recipientShortName.in=" + DEFAULT_RECIPIENT_SHORT_NAME + "," + UPDATED_RECIPIENT_SHORT_NAME);

        // Get all the billList where recipientShortName equals to UPDATED_RECIPIENT_SHORT_NAME
        defaultBillShouldNotBeFound("recipientShortName.in=" + UPDATED_RECIPIENT_SHORT_NAME);
    }

    @Test
    @Transactional
    public void getAllBillsByRecipientShortNameIsNullOrNotNull() throws Exception {
        // Initialize the database
        billRepository.saveAndFlush(bill);

        // Get all the billList where recipientShortName is not null
        defaultBillShouldBeFound("recipientShortName.specified=true");

        // Get all the billList where recipientShortName is null
        defaultBillShouldNotBeFound("recipientShortName.specified=false");
    }

    @Test
    @Transactional
    public void getAllBillsBySessionIdIsEqualToSomething() throws Exception {
        // Initialize the database
        billRepository.saveAndFlush(bill);

        // Get all the billList where sessionId equals to DEFAULT_SESSION_ID
        defaultBillShouldBeFound("sessionId.equals=" + DEFAULT_SESSION_ID);

        // Get all the billList where sessionId equals to UPDATED_SESSION_ID
        defaultBillShouldNotBeFound("sessionId.equals=" + UPDATED_SESSION_ID);
    }

    @Test
    @Transactional
    public void getAllBillsBySessionIdIsInShouldWork() throws Exception {
        // Initialize the database
        billRepository.saveAndFlush(bill);

        // Get all the billList where sessionId in DEFAULT_SESSION_ID or UPDATED_SESSION_ID
        defaultBillShouldBeFound("sessionId.in=" + DEFAULT_SESSION_ID + "," + UPDATED_SESSION_ID);

        // Get all the billList where sessionId equals to UPDATED_SESSION_ID
        defaultBillShouldNotBeFound("sessionId.in=" + UPDATED_SESSION_ID);
    }

    @Test
    @Transactional
    public void getAllBillsBySessionIdIsNullOrNotNull() throws Exception {
        // Initialize the database
        billRepository.saveAndFlush(bill);

        // Get all the billList where sessionId is not null
        defaultBillShouldBeFound("sessionId.specified=true");

        // Get all the billList where sessionId is null
        defaultBillShouldNotBeFound("sessionId.specified=false");
    }

    @Test
    @Transactional
    public void getAllBillsByTypeIsEqualToSomething() throws Exception {
        // Initialize the database
        billRepository.saveAndFlush(bill);

        // Get all the billList where type equals to DEFAULT_TYPE
        defaultBillShouldBeFound("type.equals=" + DEFAULT_TYPE);

        // Get all the billList where type equals to UPDATED_TYPE
        defaultBillShouldNotBeFound("type.equals=" + UPDATED_TYPE);
    }

    @Test
    @Transactional
    public void getAllBillsByTypeIsInShouldWork() throws Exception {
        // Initialize the database
        billRepository.saveAndFlush(bill);

        // Get all the billList where type in DEFAULT_TYPE or UPDATED_TYPE
        defaultBillShouldBeFound("type.in=" + DEFAULT_TYPE + "," + UPDATED_TYPE);

        // Get all the billList where type equals to UPDATED_TYPE
        defaultBillShouldNotBeFound("type.in=" + UPDATED_TYPE);
    }

    @Test
    @Transactional
    public void getAllBillsByTypeIsNullOrNotNull() throws Exception {
        // Initialize the database
        billRepository.saveAndFlush(bill);

        // Get all the billList where type is not null
        defaultBillShouldBeFound("type.specified=true");

        // Get all the billList where type is null
        defaultBillShouldNotBeFound("type.specified=false");
    }

    @Test
    @Transactional
    public void getAllBillsByAmountRatioIsEqualToSomething() throws Exception {
        // Initialize the database
        billRepository.saveAndFlush(bill);

        // Get all the billList where amountRatio equals to DEFAULT_AMOUNT_RATIO
        defaultBillShouldBeFound("amountRatio.equals=" + DEFAULT_AMOUNT_RATIO);

        // Get all the billList where amountRatio equals to UPDATED_AMOUNT_RATIO
        defaultBillShouldNotBeFound("amountRatio.equals=" + UPDATED_AMOUNT_RATIO);
    }

    @Test
    @Transactional
    public void getAllBillsByAmountRatioIsInShouldWork() throws Exception {
        // Initialize the database
        billRepository.saveAndFlush(bill);

        // Get all the billList where amountRatio in DEFAULT_AMOUNT_RATIO or UPDATED_AMOUNT_RATIO
        defaultBillShouldBeFound("amountRatio.in=" + DEFAULT_AMOUNT_RATIO + "," + UPDATED_AMOUNT_RATIO);

        // Get all the billList where amountRatio equals to UPDATED_AMOUNT_RATIO
        defaultBillShouldNotBeFound("amountRatio.in=" + UPDATED_AMOUNT_RATIO);
    }

    @Test
    @Transactional
    public void getAllBillsByAmountRatioIsNullOrNotNull() throws Exception {
        // Initialize the database
        billRepository.saveAndFlush(bill);

        // Get all the billList where amountRatio is not null
        defaultBillShouldBeFound("amountRatio.specified=true");

        // Get all the billList where amountRatio is null
        defaultBillShouldNotBeFound("amountRatio.specified=false");
    }

    @Test
    @Transactional
    public void getAllBillsByAmountRatioIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        billRepository.saveAndFlush(bill);

        // Get all the billList where amountRatio greater than or equals to DEFAULT_AMOUNT_RATIO
        defaultBillShouldBeFound("amountRatio.greaterOrEqualThan=" + DEFAULT_AMOUNT_RATIO);

        // Get all the billList where amountRatio greater than or equals to UPDATED_AMOUNT_RATIO
        defaultBillShouldNotBeFound("amountRatio.greaterOrEqualThan=" + UPDATED_AMOUNT_RATIO);
    }

    @Test
    @Transactional
    public void getAllBillsByAmountRatioIsLessThanSomething() throws Exception {
        // Initialize the database
        billRepository.saveAndFlush(bill);

        // Get all the billList where amountRatio less than or equals to DEFAULT_AMOUNT_RATIO
        defaultBillShouldNotBeFound("amountRatio.lessThan=" + DEFAULT_AMOUNT_RATIO);

        // Get all the billList where amountRatio less than or equals to UPDATED_AMOUNT_RATIO
        defaultBillShouldBeFound("amountRatio.lessThan=" + UPDATED_AMOUNT_RATIO);
    }

    @Test
    @Transactional
    public void getAllBillsByAmountIsEqualToSomething() throws Exception {
        // Initialize the database
        billRepository.saveAndFlush(bill);

        // Get all the billList where amount equals to DEFAULT_AMOUNT
        defaultBillShouldBeFound("amount.equals=" + DEFAULT_AMOUNT);

        // Get all the billList where amount equals to UPDATED_AMOUNT
        defaultBillShouldNotBeFound("amount.equals=" + UPDATED_AMOUNT);
    }

    @Test
    @Transactional
    public void getAllBillsByAmountIsInShouldWork() throws Exception {
        // Initialize the database
        billRepository.saveAndFlush(bill);

        // Get all the billList where amount in DEFAULT_AMOUNT or UPDATED_AMOUNT
        defaultBillShouldBeFound("amount.in=" + DEFAULT_AMOUNT + "," + UPDATED_AMOUNT);

        // Get all the billList where amount equals to UPDATED_AMOUNT
        defaultBillShouldNotBeFound("amount.in=" + UPDATED_AMOUNT);
    }

    @Test
    @Transactional
    public void getAllBillsByAmountIsNullOrNotNull() throws Exception {
        // Initialize the database
        billRepository.saveAndFlush(bill);

        // Get all the billList where amount is not null
        defaultBillShouldBeFound("amount.specified=true");

        // Get all the billList where amount is null
        defaultBillShouldNotBeFound("amount.specified=false");
    }

    @Test
    @Transactional
    public void getAllBillsByAmountIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        billRepository.saveAndFlush(bill);

        // Get all the billList where amount greater than or equals to DEFAULT_AMOUNT
        defaultBillShouldBeFound("amount.greaterOrEqualThan=" + DEFAULT_AMOUNT);

        // Get all the billList where amount greater than or equals to UPDATED_AMOUNT
        defaultBillShouldNotBeFound("amount.greaterOrEqualThan=" + UPDATED_AMOUNT);
    }

    @Test
    @Transactional
    public void getAllBillsByAmountIsLessThanSomething() throws Exception {
        // Initialize the database
        billRepository.saveAndFlush(bill);

        // Get all the billList where amount less than or equals to DEFAULT_AMOUNT
        defaultBillShouldNotBeFound("amount.lessThan=" + DEFAULT_AMOUNT);

        // Get all the billList where amount less than or equals to UPDATED_AMOUNT
        defaultBillShouldBeFound("amount.lessThan=" + UPDATED_AMOUNT);
    }

    @Test
    @Transactional
    public void getAllBillsByProfitIsEqualToSomething() throws Exception {
        // Initialize the database
        billRepository.saveAndFlush(bill);

        // Get all the billList where profit equals to DEFAULT_PROFIT
        defaultBillShouldBeFound("profit.equals=" + DEFAULT_PROFIT);

        // Get all the billList where profit equals to UPDATED_PROFIT
        defaultBillShouldNotBeFound("profit.equals=" + UPDATED_PROFIT);
    }

    @Test
    @Transactional
    public void getAllBillsByProfitIsInShouldWork() throws Exception {
        // Initialize the database
        billRepository.saveAndFlush(bill);

        // Get all the billList where profit in DEFAULT_PROFIT or UPDATED_PROFIT
        defaultBillShouldBeFound("profit.in=" + DEFAULT_PROFIT + "," + UPDATED_PROFIT);

        // Get all the billList where profit equals to UPDATED_PROFIT
        defaultBillShouldNotBeFound("profit.in=" + UPDATED_PROFIT);
    }

    @Test
    @Transactional
    public void getAllBillsByProfitIsNullOrNotNull() throws Exception {
        // Initialize the database
        billRepository.saveAndFlush(bill);

        // Get all the billList where profit is not null
        defaultBillShouldBeFound("profit.specified=true");

        // Get all the billList where profit is null
        defaultBillShouldNotBeFound("profit.specified=false");
    }

    @Test
    @Transactional
    public void getAllBillsByProfitIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        billRepository.saveAndFlush(bill);

        // Get all the billList where profit greater than or equals to DEFAULT_PROFIT
        defaultBillShouldBeFound("profit.greaterOrEqualThan=" + DEFAULT_PROFIT);

        // Get all the billList where profit greater than or equals to UPDATED_PROFIT
        defaultBillShouldNotBeFound("profit.greaterOrEqualThan=" + UPDATED_PROFIT);
    }

    @Test
    @Transactional
    public void getAllBillsByProfitIsLessThanSomething() throws Exception {
        // Initialize the database
        billRepository.saveAndFlush(bill);

        // Get all the billList where profit less than or equals to DEFAULT_PROFIT
        defaultBillShouldNotBeFound("profit.lessThan=" + DEFAULT_PROFIT);

        // Get all the billList where profit less than or equals to UPDATED_PROFIT
        defaultBillShouldBeFound("profit.lessThan=" + UPDATED_PROFIT);
    }

    @Test
    @Transactional
    public void getAllBillsByAccountIsEqualToSomething() throws Exception {
        // Initialize the database
        billRepository.saveAndFlush(bill);

        // Get all the billList where account equals to DEFAULT_ACCOUNT
        defaultBillShouldBeFound("account.equals=" + DEFAULT_ACCOUNT);

        // Get all the billList where account equals to UPDATED_ACCOUNT
        defaultBillShouldNotBeFound("account.equals=" + UPDATED_ACCOUNT);
    }

    @Test
    @Transactional
    public void getAllBillsByAccountIsInShouldWork() throws Exception {
        // Initialize the database
        billRepository.saveAndFlush(bill);

        // Get all the billList where account in DEFAULT_ACCOUNT or UPDATED_ACCOUNT
        defaultBillShouldBeFound("account.in=" + DEFAULT_ACCOUNT + "," + UPDATED_ACCOUNT);

        // Get all the billList where account equals to UPDATED_ACCOUNT
        defaultBillShouldNotBeFound("account.in=" + UPDATED_ACCOUNT);
    }

    @Test
    @Transactional
    public void getAllBillsByAccountIsNullOrNotNull() throws Exception {
        // Initialize the database
        billRepository.saveAndFlush(bill);

        // Get all the billList where account is not null
        defaultBillShouldBeFound("account.specified=true");

        // Get all the billList where account is null
        defaultBillShouldNotBeFound("account.specified=false");
    }

    //    @Test
    @Transactional
    public void getAllBillsByParamsIsEqualToSomething() throws Exception {
        // Initialize the database
        billRepository.saveAndFlush(bill);

        // Get all the billList where params equals to DEFAULT_PARAMS
        defaultBillShouldBeFound("params.equals=" + DEFAULT_PARAMS);

        // Get all the billList where params equals to UPDATED_PARAMS
        defaultBillShouldNotBeFound("params.equals=" + UPDATED_PARAMS);
    }

    //    @Test
    @Transactional
    public void getAllBillsByParamsIsInShouldWork() throws Exception {
        // Initialize the database
        billRepository.saveAndFlush(bill);

        // Get all the billList where params in DEFAULT_PARAMS or UPDATED_PARAMS
        defaultBillShouldBeFound("params.in=" + DEFAULT_PARAMS + "," + UPDATED_PARAMS);

        // Get all the billList where params equals to UPDATED_PARAMS
        defaultBillShouldNotBeFound("params.in=" + UPDATED_PARAMS);
    }

    //    @Test
    @Transactional
    public void getAllBillsByParamsIsNullOrNotNull() throws Exception {
        // Initialize the database
        billRepository.saveAndFlush(bill);

        // Get all the billList where params is not null
        defaultBillShouldBeFound("params.specified=true");

        // Get all the billList where params is null
        defaultBillShouldNotBeFound("params.specified=false");
    }

    @Test
    @Transactional
    public void getAllBillsByCurrencyIsEqualToSomething() throws Exception {
        // Initialize the database
        billRepository.saveAndFlush(bill);

        // Get all the billList where currency equals to DEFAULT_CURRENCY
        defaultBillShouldBeFound("currency.equals=" + DEFAULT_CURRENCY);

        // Get all the billList where currency equals to UPDATED_CURRENCY
        defaultBillShouldNotBeFound("currency.equals=" + UPDATED_CURRENCY);
    }

    @Test
    @Transactional
    public void getAllBillsByCurrencyIsInShouldWork() throws Exception {
        // Initialize the database
        billRepository.saveAndFlush(bill);

        // Get all the billList where currency in DEFAULT_CURRENCY or UPDATED_CURRENCY
        defaultBillShouldBeFound("currency.in=" + DEFAULT_CURRENCY + "," + UPDATED_CURRENCY);

        // Get all the billList where currency equals to UPDATED_CURRENCY
        defaultBillShouldNotBeFound("currency.in=" + UPDATED_CURRENCY);
    }

    @Test
    @Transactional
    public void getAllBillsByCurrencyIsNullOrNotNull() throws Exception {
        // Initialize the database
        billRepository.saveAndFlush(bill);

        // Get all the billList where currency is not null
        defaultBillShouldBeFound("currency.specified=true");

        // Get all the billList where currency is null
        defaultBillShouldNotBeFound("currency.specified=false");
    }

    @Test
    @Transactional
    public void getAllBillsByCurrencyIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        billRepository.saveAndFlush(bill);

        // Get all the billList where currency greater than or equals to DEFAULT_CURRENCY
        defaultBillShouldBeFound("currency.greaterOrEqualThan=" + DEFAULT_CURRENCY);

        // Get all the billList where currency greater than or equals to UPDATED_CURRENCY
        defaultBillShouldNotBeFound("currency.greaterOrEqualThan=" + UPDATED_CURRENCY);
    }

    @Test
    @Transactional
    public void getAllBillsByCurrencyIsLessThanSomething() throws Exception {
        // Initialize the database
        billRepository.saveAndFlush(bill);

        // Get all the billList where currency less than or equals to DEFAULT_CURRENCY
        defaultBillShouldNotBeFound("currency.lessThan=" + DEFAULT_CURRENCY);

        // Get all the billList where currency less than or equals to UPDATED_CURRENCY
        defaultBillShouldBeFound("currency.lessThan=" + UPDATED_CURRENCY);
    }

    @Test
    @Transactional
    public void getAllBillsByExtIdIsEqualToSomething() throws Exception {
        // Initialize the database
        billRepository.saveAndFlush(bill);

        // Get all the billList where extId equals to DEFAULT_EXT_ID
        defaultBillShouldBeFound("extId.equals=" + DEFAULT_EXT_ID);

        // Get all the billList where extId equals to UPDATED_EXT_ID
        defaultBillShouldNotBeFound("extId.equals=" + UPDATED_EXT_ID);
    }

    @Test
    @Transactional
    public void getAllBillsByExtIdIsInShouldWork() throws Exception {
        // Initialize the database
        billRepository.saveAndFlush(bill);

        // Get all the billList where extId in DEFAULT_EXT_ID or UPDATED_EXT_ID
        defaultBillShouldBeFound("extId.in=" + DEFAULT_EXT_ID + "," + UPDATED_EXT_ID);

        // Get all the billList where extId equals to UPDATED_EXT_ID
        defaultBillShouldNotBeFound("extId.in=" + UPDATED_EXT_ID);
    }

    @Test
    @Transactional
    public void getAllBillsByExtIdIsNullOrNotNull() throws Exception {
        // Initialize the database
        billRepository.saveAndFlush(bill);

        // Get all the billList where extId is not null
        defaultBillShouldBeFound("extId.specified=true");

        // Get all the billList where extId is null
        defaultBillShouldNotBeFound("extId.specified=false");
    }

    @Test
    @Transactional
    public void getAllBillsByCheckIdIsEqualToSomething() throws Exception {
        // Initialize the database
        billRepository.saveAndFlush(bill);

        // Get all the billList where checkId equals to DEFAULT_CHECK_ID
        defaultBillShouldBeFound("checkId.equals=" + DEFAULT_CHECK_ID);

        // Get all the billList where checkId equals to UPDATED_CHECK_ID
        defaultBillShouldNotBeFound("checkId.equals=" + UPDATED_CHECK_ID);
    }

    @Test
    @Transactional
    public void getAllBillsByCheckIdIsInShouldWork() throws Exception {
        // Initialize the database
        billRepository.saveAndFlush(bill);

        // Get all the billList where checkId in DEFAULT_CHECK_ID or UPDATED_CHECK_ID
        defaultBillShouldBeFound("checkId.in=" + DEFAULT_CHECK_ID + "," + UPDATED_CHECK_ID);

        // Get all the billList where checkId equals to UPDATED_CHECK_ID
        defaultBillShouldNotBeFound("checkId.in=" + UPDATED_CHECK_ID);
    }

    @Test
    @Transactional
    public void getAllBillsByCheckIdIsNullOrNotNull() throws Exception {
        // Initialize the database
        billRepository.saveAndFlush(bill);

        // Get all the billList where checkId is not null
        defaultBillShouldBeFound("checkId.specified=true");

        // Get all the billList where checkId is null
        defaultBillShouldNotBeFound("checkId.specified=false");
    }

    @Test
    @Transactional
    public void getAllBillsByPayIdIsEqualToSomething() throws Exception {
        // Initialize the database
        billRepository.saveAndFlush(bill);

        // Get all the billList where payId equals to DEFAULT_PAY_ID
        defaultBillShouldBeFound("payId.equals=" + DEFAULT_PAY_ID);

        // Get all the billList where payId equals to UPDATED_PAY_ID
        defaultBillShouldNotBeFound("payId.equals=" + UPDATED_PAY_ID);
    }

    @Test
    @Transactional
    public void getAllBillsByPayIdIsInShouldWork() throws Exception {
        // Initialize the database
        billRepository.saveAndFlush(bill);

        // Get all the billList where payId in DEFAULT_PAY_ID or UPDATED_PAY_ID
        defaultBillShouldBeFound("payId.in=" + DEFAULT_PAY_ID + "," + UPDATED_PAY_ID);

        // Get all the billList where payId equals to UPDATED_PAY_ID
        defaultBillShouldNotBeFound("payId.in=" + UPDATED_PAY_ID);
    }

    @Test
    @Transactional
    public void getAllBillsByPayIdIsNullOrNotNull() throws Exception {
        // Initialize the database
        billRepository.saveAndFlush(bill);

        // Get all the billList where payId is not null
        defaultBillShouldBeFound("payId.specified=true");

        // Get all the billList where payId is null
        defaultBillShouldNotBeFound("payId.specified=false");
    }

    @Test
    @Transactional
    public void getAllBillsByRequestTimeIsEqualToSomething() throws Exception {
        // Initialize the database
        billRepository.saveAndFlush(bill);

        // Get all the billList where requestTime equals to DEFAULT_REQUEST_TIME
        defaultBillShouldBeFound("requestTime.equals=" + DEFAULT_REQUEST_TIME);

        // Get all the billList where requestTime equals to UPDATED_REQUEST_TIME
        defaultBillShouldNotBeFound("requestTime.equals=" + UPDATED_REQUEST_TIME);
    }

    @Test
    @Transactional
    public void getAllBillsByRequestTimeIsInShouldWork() throws Exception {
        // Initialize the database
        billRepository.saveAndFlush(bill);

        // Get all the billList where requestTime in DEFAULT_REQUEST_TIME or UPDATED_REQUEST_TIME
        defaultBillShouldBeFound("requestTime.in=" + DEFAULT_REQUEST_TIME + "," + UPDATED_REQUEST_TIME);

        // Get all the billList where requestTime equals to UPDATED_REQUEST_TIME
        defaultBillShouldNotBeFound("requestTime.in=" + UPDATED_REQUEST_TIME);
    }

    @Test
    @Transactional
    public void getAllBillsByRequestTimeIsNullOrNotNull() throws Exception {
        // Initialize the database
        billRepository.saveAndFlush(bill);

        // Get all the billList where requestTime is not null
        defaultBillShouldBeFound("requestTime.specified=true");

        // Get all the billList where requestTime is null
        defaultBillShouldNotBeFound("requestTime.specified=false");
    }

    //    @Test
    @Transactional
    public void getAllBillsByRequestJsonIsEqualToSomething() throws Exception {
        // Initialize the database
        billRepository.saveAndFlush(bill);

        // Get all the billList where requestJson equals to DEFAULT_REQUEST_JSON
        defaultBillShouldBeFound("requestJson.equals=" + DEFAULT_REQUEST_JSON);

        // Get all the billList where requestJson equals to UPDATED_REQUEST_JSON
        defaultBillShouldNotBeFound("requestJson.equals=" + UPDATED_REQUEST_JSON);
    }

    //    @Test
    @Transactional
    public void getAllBillsByRequestJsonIsInShouldWork() throws Exception {
        // Initialize the database
        billRepository.saveAndFlush(bill);

        // Get all the billList where requestJson in DEFAULT_REQUEST_JSON or UPDATED_REQUEST_JSON
        defaultBillShouldBeFound("requestJson.in=" + DEFAULT_REQUEST_JSON + "," + UPDATED_REQUEST_JSON);

        // Get all the billList where requestJson equals to UPDATED_REQUEST_JSON
        defaultBillShouldNotBeFound("requestJson.in=" + UPDATED_REQUEST_JSON);
    }

    //    @Test
    @Transactional
    public void getAllBillsByRequestJsonIsNullOrNotNull() throws Exception {
        // Initialize the database
        billRepository.saveAndFlush(bill);

        // Get all the billList where requestJson is not null
        defaultBillShouldBeFound("requestJson.specified=true");

        // Get all the billList where requestJson is null
        defaultBillShouldNotBeFound("requestJson.specified=false");
    }

    //    @Test
    @Transactional
    public void getAllBillsByResponseJsonIsEqualToSomething() throws Exception {
        // Initialize the database
        billRepository.saveAndFlush(bill);

        // Get all the billList where responseJson equals to DEFAULT_RESPONSE_JSON
        defaultBillShouldBeFound("responseJson.equals=" + DEFAULT_RESPONSE_JSON);

        // Get all the billList where responseJson equals to UPDATED_RESPONSE_JSON
        defaultBillShouldNotBeFound("responseJson.equals=" + UPDATED_RESPONSE_JSON);
    }

    //    @Test
    @Transactional
    public void getAllBillsByResponseJsonIsInShouldWork() throws Exception {
        // Initialize the database
        billRepository.saveAndFlush(bill);

        // Get all the billList where responseJson in DEFAULT_RESPONSE_JSON or UPDATED_RESPONSE_JSON
        defaultBillShouldBeFound("responseJson.in=" + DEFAULT_RESPONSE_JSON + "," + UPDATED_RESPONSE_JSON);

        // Get all the billList where responseJson equals to UPDATED_RESPONSE_JSON
        defaultBillShouldNotBeFound("responseJson.in=" + UPDATED_RESPONSE_JSON);
    }

    //    @Test
    @Transactional
    public void getAllBillsByResponseJsonIsNullOrNotNull() throws Exception {
        // Initialize the database
        billRepository.saveAndFlush(bill);

        // Get all the billList where responseJson is not null
        defaultBillShouldBeFound("responseJson.specified=true");

        // Get all the billList where responseJson is null
        defaultBillShouldNotBeFound("responseJson.specified=false");
    }

    @Test
    @Transactional
    public void getAllBillsByResponseTimeIsEqualToSomething() throws Exception {
        // Initialize the database
        billRepository.saveAndFlush(bill);

        // Get all the billList where responseTime equals to DEFAULT_RESPONSE_TIME
        defaultBillShouldBeFound("responseTime.equals=" + DEFAULT_RESPONSE_TIME);

        // Get all the billList where responseTime equals to UPDATED_RESPONSE_TIME
        defaultBillShouldNotBeFound("responseTime.equals=" + UPDATED_RESPONSE_TIME);
    }

    @Test
    @Transactional
    public void getAllBillsByResponseTimeIsInShouldWork() throws Exception {
        // Initialize the database
        billRepository.saveAndFlush(bill);

        // Get all the billList where responseTime in DEFAULT_RESPONSE_TIME or UPDATED_RESPONSE_TIME
        defaultBillShouldBeFound("responseTime.in=" + DEFAULT_RESPONSE_TIME + "," + UPDATED_RESPONSE_TIME);

        // Get all the billList where responseTime equals to UPDATED_RESPONSE_TIME
        defaultBillShouldNotBeFound("responseTime.in=" + UPDATED_RESPONSE_TIME);
    }

    @Test
    @Transactional
    public void getAllBillsByResponseTimeIsNullOrNotNull() throws Exception {
        // Initialize the database
        billRepository.saveAndFlush(bill);

        // Get all the billList where responseTime is not null
        defaultBillShouldBeFound("responseTime.specified=true");

        // Get all the billList where responseTime is null
        defaultBillShouldNotBeFound("responseTime.specified=false");
    }

    @Test
    @Transactional
    public void getAllBillsByResponseCodeIsEqualToSomething() throws Exception {
        // Initialize the database
        billRepository.saveAndFlush(bill);

        // Get all the billList where responseCode equals to DEFAULT_RESPONSE_CODE
        defaultBillShouldBeFound("responseCode.equals=" + DEFAULT_RESPONSE_CODE);

        // Get all the billList where responseCode equals to UPDATED_RESPONSE_CODE
        defaultBillShouldNotBeFound("responseCode.equals=" + UPDATED_RESPONSE_CODE);
    }

    @Test
    @Transactional
    public void getAllBillsByResponseCodeIsInShouldWork() throws Exception {
        // Initialize the database
        billRepository.saveAndFlush(bill);

        // Get all the billList where responseCode in DEFAULT_RESPONSE_CODE or UPDATED_RESPONSE_CODE
        defaultBillShouldBeFound("responseCode.in=" + DEFAULT_RESPONSE_CODE + "," + UPDATED_RESPONSE_CODE);

        // Get all the billList where responseCode equals to UPDATED_RESPONSE_CODE
        defaultBillShouldNotBeFound("responseCode.in=" + UPDATED_RESPONSE_CODE);
    }

    @Test
    @Transactional
    public void getAllBillsByResponseCodeIsNullOrNotNull() throws Exception {
        // Initialize the database
        billRepository.saveAndFlush(bill);

        // Get all the billList where responseCode is not null
        defaultBillShouldBeFound("responseCode.specified=true");

        // Get all the billList where responseCode is null
        defaultBillShouldNotBeFound("responseCode.specified=false");
    }

    @Test
    @Transactional
    public void getAllBillsByResponseTextIsEqualToSomething() throws Exception {
        // Initialize the database
        billRepository.saveAndFlush(bill);

        // Get all the billList where responseText equals to DEFAULT_RESPONSE_TEXT
        defaultBillShouldBeFound("responseText.equals=" + DEFAULT_RESPONSE_TEXT);

        // Get all the billList where responseText equals to UPDATED_RESPONSE_TEXT
        defaultBillShouldNotBeFound("responseText.equals=" + UPDATED_RESPONSE_TEXT);
    }

    @Test
    @Transactional
    public void getAllBillsByResponseTextIsInShouldWork() throws Exception {
        // Initialize the database
        billRepository.saveAndFlush(bill);

        // Get all the billList where responseText in DEFAULT_RESPONSE_TEXT or UPDATED_RESPONSE_TEXT
        defaultBillShouldBeFound("responseText.in=" + DEFAULT_RESPONSE_TEXT + "," + UPDATED_RESPONSE_TEXT);

        // Get all the billList where responseText equals to UPDATED_RESPONSE_TEXT
        defaultBillShouldNotBeFound("responseText.in=" + UPDATED_RESPONSE_TEXT);
    }

    @Test
    @Transactional
    public void getAllBillsByResponseTextIsNullOrNotNull() throws Exception {
        // Initialize the database
        billRepository.saveAndFlush(bill);

        // Get all the billList where responseText is not null
        defaultBillShouldBeFound("responseText.specified=true");

        // Get all the billList where responseText is null
        defaultBillShouldNotBeFound("responseText.specified=false");
    }

    @Test
    @Transactional
    public void getAllBillsBySuccessIsEqualToSomething() throws Exception {
        // Initialize the database
        billRepository.saveAndFlush(bill);

        // Get all the billList where success equals to DEFAULT_SUCCESS
        defaultBillShouldBeFound("success.equals=" + DEFAULT_SUCCESS);

        // Get all the billList where success equals to UPDATED_SUCCESS
        defaultBillShouldNotBeFound("success.equals=" + UPDATED_SUCCESS);
    }

    @Test
    @Transactional
    public void getAllBillsBySuccessIsInShouldWork() throws Exception {
        // Initialize the database
        billRepository.saveAndFlush(bill);

        // Get all the billList where success in DEFAULT_SUCCESS or UPDATED_SUCCESS
        defaultBillShouldBeFound("success.in=" + DEFAULT_SUCCESS + "," + UPDATED_SUCCESS);

        // Get all the billList where success equals to UPDATED_SUCCESS
        defaultBillShouldNotBeFound("success.in=" + UPDATED_SUCCESS);
    }

    @Test
    @Transactional
    public void getAllBillsBySuccessIsNullOrNotNull() throws Exception {
        // Initialize the database
        billRepository.saveAndFlush(bill);

        // Get all the billList where success is not null
        defaultBillShouldBeFound("success.specified=true");

        // Get all the billList where success is null
        defaultBillShouldNotBeFound("success.specified=false");
    }

    @Test
    @Transactional
    public void getAllBillsByLoginIsEqualToSomething() throws Exception {
        // Initialize the database
        billRepository.saveAndFlush(bill);

        // Get all the billList where login equals to DEFAULT_LOGIN
        defaultBillShouldBeFound("login.equals=" + DEFAULT_LOGIN);

        // Get all the billList where login equals to UPDATED_LOGIN
        defaultBillShouldNotBeFound("login.equals=" + UPDATED_LOGIN);
    }

    @Test
    @Transactional
    public void getAllBillsByLoginIsInShouldWork() throws Exception {
        // Initialize the database
        billRepository.saveAndFlush(bill);

        // Get all the billList where login in DEFAULT_LOGIN or UPDATED_LOGIN
        defaultBillShouldBeFound("login.in=" + DEFAULT_LOGIN + "," + UPDATED_LOGIN);

        // Get all the billList where login equals to UPDATED_LOGIN
        defaultBillShouldNotBeFound("login.in=" + UPDATED_LOGIN);
    }

    @Test
    @Transactional
    public void getAllBillsByLoginIsNullOrNotNull() throws Exception {
        // Initialize the database
        billRepository.saveAndFlush(bill);

        // Get all the billList where login is not null
        defaultBillShouldBeFound("login.specified=true");

        // Get all the billList where login is null
        defaultBillShouldNotBeFound("login.specified=false");
    }

    @Test
    @Transactional
    public void getAllBillsByFraudIdIsEqualToSomething() throws Exception {
        // Initialize the database
        billRepository.saveAndFlush(bill);

        // Get all the billList where fraudId equals to DEFAULT_FRAUD_ID
        defaultBillShouldBeFound("fraudId.equals=" + DEFAULT_FRAUD_ID);

        // Get all the billList where fraudId equals to UPDATED_FRAUD_ID
        defaultBillShouldNotBeFound("fraudId.equals=" + UPDATED_FRAUD_ID);
    }

    @Test
    @Transactional
    public void getAllBillsByFraudIdIsInShouldWork() throws Exception {
        // Initialize the database
        billRepository.saveAndFlush(bill);

        // Get all the billList where fraudId in DEFAULT_FRAUD_ID or UPDATED_FRAUD_ID
        defaultBillShouldBeFound("fraudId.in=" + DEFAULT_FRAUD_ID + "," + UPDATED_FRAUD_ID);

        // Get all the billList where fraudId equals to UPDATED_FRAUD_ID
        defaultBillShouldNotBeFound("fraudId.in=" + UPDATED_FRAUD_ID);
    }

    @Test
    @Transactional
    public void getAllBillsByFraudIdIsNullOrNotNull() throws Exception {
        // Initialize the database
        billRepository.saveAndFlush(bill);

        // Get all the billList where fraudId is not null
        defaultBillShouldBeFound("fraudId.specified=true");

        // Get all the billList where fraudId is null
        defaultBillShouldNotBeFound("fraudId.specified=false");
    }

    @Test
    @Transactional
    public void getAllBillsByFraudIdIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        billRepository.saveAndFlush(bill);

        // Get all the billList where fraudId greater than or equals to DEFAULT_FRAUD_ID
        defaultBillShouldBeFound("fraudId.greaterOrEqualThan=" + DEFAULT_FRAUD_ID);

        // Get all the billList where fraudId greater than or equals to UPDATED_FRAUD_ID
        defaultBillShouldNotBeFound("fraudId.greaterOrEqualThan=" + UPDATED_FRAUD_ID);
    }

    @Test
    @Transactional
    public void getAllBillsByFraudIdIsLessThanSomething() throws Exception {
        // Initialize the database
        billRepository.saveAndFlush(bill);

        // Get all the billList where fraudId less than or equals to DEFAULT_FRAUD_ID
        defaultBillShouldNotBeFound("fraudId.lessThan=" + DEFAULT_FRAUD_ID);

        // Get all the billList where fraudId less than or equals to UPDATED_FRAUD_ID
        defaultBillShouldBeFound("fraudId.lessThan=" + UPDATED_FRAUD_ID);
    }

    @Test
    @Transactional
    public void getAllBillsByTransactionIdIsEqualToSomething() throws Exception {
        // Initialize the database
        billRepository.saveAndFlush(bill);

        // Get all the billList where transactionId equals to DEFAULT_TRANSACTION_ID
        defaultBillShouldBeFound("transactionId.equals=" + DEFAULT_TRANSACTION_ID);

        // Get all the billList where transactionId equals to UPDATED_TRANSACTION_ID
        defaultBillShouldNotBeFound("transactionId.equals=" + UPDATED_TRANSACTION_ID);
    }

    @Test
    @Transactional
    public void getAllBillsByTransactionIdIsInShouldWork() throws Exception {
        // Initialize the database
        billRepository.saveAndFlush(bill);

        // Get all the billList where transactionId in DEFAULT_TRANSACTION_ID or UPDATED_TRANSACTION_ID
        defaultBillShouldBeFound("transactionId.in=" + DEFAULT_TRANSACTION_ID + "," + UPDATED_TRANSACTION_ID);

        // Get all the billList where transactionId equals to UPDATED_TRANSACTION_ID
        defaultBillShouldNotBeFound("transactionId.in=" + UPDATED_TRANSACTION_ID);
    }

    @Test
    @Transactional
    public void getAllBillsByTransactionIdIsNullOrNotNull() throws Exception {
        // Initialize the database
        billRepository.saveAndFlush(bill);

        // Get all the billList where transactionId is not null
        defaultBillShouldBeFound("transactionId.specified=true");

        // Get all the billList where transactionId is null
        defaultBillShouldNotBeFound("transactionId.specified=false");
    }

    @Test
    @Transactional
    public void getAllBillsByTransactionIdIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        billRepository.saveAndFlush(bill);

        // Get all the billList where transactionId greater than or equals to DEFAULT_TRANSACTION_ID
        defaultBillShouldBeFound("transactionId.greaterOrEqualThan=" + DEFAULT_TRANSACTION_ID);

        // Get all the billList where transactionId greater than or equals to UPDATED_TRANSACTION_ID
        defaultBillShouldNotBeFound("transactionId.greaterOrEqualThan=" + UPDATED_TRANSACTION_ID);
    }

    @Test
    @Transactional
    public void getAllBillsByTransactionIdIsLessThanSomething() throws Exception {
        // Initialize the database
        billRepository.saveAndFlush(bill);

        // Get all the billList where transactionId less than or equals to DEFAULT_TRANSACTION_ID
        defaultBillShouldNotBeFound("transactionId.lessThan=" + DEFAULT_TRANSACTION_ID);

        // Get all the billList where transactionId less than or equals to UPDATED_TRANSACTION_ID
        defaultBillShouldBeFound("transactionId.lessThan=" + UPDATED_TRANSACTION_ID);
    }

    /**
     * Executes the search, and checks that the default entity is returned
     */
    private void defaultBillShouldBeFound(String filter) throws Exception {
        restBillMockMvc
            .perform(get("/api/bills?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(bill.getId().intValue())))
            .andExpect(jsonPath("$.[*].recipientId").value(hasItem(DEFAULT_RECIPIENT_ID.intValue())))
            .andExpect(jsonPath("$.[*].recipientShortName").value(hasItem(DEFAULT_RECIPIENT_SHORT_NAME.toString())))
            .andExpect(jsonPath("$.[*].sessionId").value(hasItem(DEFAULT_SESSION_ID.toString())))
            .andExpect(jsonPath("$.[*].type").value(hasItem(DEFAULT_TYPE.toString())))
            .andExpect(jsonPath("$.[*].amountRatio").value(hasItem(DEFAULT_AMOUNT_RATIO)))
            .andExpect(jsonPath("$.[*].amount").value(hasItem(DEFAULT_AMOUNT.intValue())))
            //            .andExpect(jsonPath("$.[*].profit").value(hasItem(DEFAULT_PROFIT.intValue())))
            .andExpect(jsonPath("$.[*].account").value(hasItem(DEFAULT_ACCOUNT.toString())))
            //            .andExpect(jsonPath("$.[*].params").value(hasItem(DEFAULT_PARAMS_JSON)))
            .andExpect(jsonPath("$.[*].currency").value(hasItem(DEFAULT_CURRENCY)))
            .andExpect(jsonPath("$.[*].extId").value(hasItem(DEFAULT_EXT_ID.toString())))
            .andExpect(jsonPath("$.[*].checkId").value(hasItem(DEFAULT_CHECK_ID.toString())))
            .andExpect(jsonPath("$.[*].payId").value(hasItem(DEFAULT_PAY_ID.toString())))
            .andExpect(jsonPath("$.[*].requestTime").value(hasItem(DEFAULT_REQUEST_TIME.toString())))
            .andExpect(jsonPath("$.[*].requestJson").value(hasItem(DEFAULT_REQUEST_JSON)))
            .andExpect(jsonPath("$.[*].responseJson").value(hasItem(DEFAULT_RESPONSE_JSON)))
            .andExpect(jsonPath("$.[*].responseTime").value(hasItem(DEFAULT_RESPONSE_TIME.toString())))
            .andExpect(jsonPath("$.[*].responseCode").value(hasItem(DEFAULT_RESPONSE_CODE.toString())))
            .andExpect(jsonPath("$.[*].responseText").value(hasItem(DEFAULT_RESPONSE_TEXT.toString())))
            .andExpect(jsonPath("$.[*].success").value(hasItem(DEFAULT_SUCCESS.booleanValue())))
            .andExpect(jsonPath("$.[*].login").value(hasItem(DEFAULT_LOGIN.toString())))
            .andExpect(jsonPath("$.[*].fraudId").value(hasItem(DEFAULT_FRAUD_ID.intValue())))
            .andExpect(jsonPath("$.[*].transactionId").value(hasItem(DEFAULT_TRANSACTION_ID.intValue())));
        // Check, that the count call also returns 1
        restBillMockMvc
            .perform(get("/api/bills/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(content().string("1"));
    }

    /**
     * Executes the search, and checks that the default entity is not returned
     */
    private void defaultBillShouldNotBeFound(String filter) throws Exception {
        restBillMockMvc
            .perform(get("/api/bills?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty());

        // Check, that the count call also returns 0
        restBillMockMvc
            .perform(get("/api/bills/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(content().string("0"));
    }

    @Test
    @Transactional
    public void getNonExistingBill() throws Exception {
        // Get the bill
        restBillMockMvc.perform(get("/api/bills/{id}", Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updateBill() throws Exception {
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
            //            .profit(UPDATED_PROFIT)
            .account(UPDATED_ACCOUNT)
            .requestJson(UPDATED_PARAMS_JSON)
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
            .perform(put("/api/bills").contentType(TestUtil.APPLICATION_JSON_UTF8).content(TestUtil.convertObjectToJsonBytes(billDTO)))
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
        //        assertThat(testBill.getProfit()).isEqualTo(UPDATED_PROFIT);
        assertThat(testBill.getAccount()).isEqualTo(UPDATED_ACCOUNT);
//        assertThat(testBill.getParams()).isEqualTo(UPDATED_PARAMS_JSON);
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
        assertThat(testBill.isSuccess()).isEqualTo(UPDATED_SUCCESS);
        assertThat(testBill.getLogin()).isEqualTo(UPDATED_LOGIN);
//        assertThat(testBill.getFraudId()).isEqualTo(UPDATED_FRAUD_ID);
        assertThat(testBill.getTransactionId()).isEqualTo(UPDATED_TRANSACTION_ID);
    }

    @Test
    @Transactional
    public void updateNonExistingBill() throws Exception {
        int databaseSizeBeforeUpdate = billRepository.findAll().size();

        // Create the Bill
        BillDTO billDTO = billMapper.toDto(bill);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restBillMockMvc
            .perform(put("/api/bills").contentType(TestUtil.APPLICATION_JSON_UTF8).content(TestUtil.convertObjectToJsonBytes(billDTO)))
            .andExpect(status().isBadRequest());

        // Validate the Bill in the database
        List<Bill> billList = billRepository.findAll();
        assertThat(billList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    public void deleteBill() throws Exception {
        // Initialize the database
        billRepository.saveAndFlush(bill);

        int databaseSizeBeforeDelete = billRepository.findAll().size();

        // Get the bill
        restBillMockMvc.perform(delete("/api/bills/{id}", bill.getId()).accept(TestUtil.APPLICATION_JSON_UTF8)).andExpect(status().isOk());

        // Validate the database is empty
        List<Bill> billList = billRepository.findAll();
        assertThat(billList).hasSize(databaseSizeBeforeDelete - 1);
    }

    @Test
    @Transactional
    public void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Bill.class);
        Bill bill1 = new Bill();
        bill1.setId(1L);
        Bill bill2 = new Bill();
        bill2.setId(bill1.getId());
        assertThat(bill1).isEqualTo(bill2);
        bill2.setId(2L);
        assertThat(bill1).isNotEqualTo(bill2);
        bill1.setId(null);
        assertThat(bill1).isNotEqualTo(bill2);
    }

    @Test
    @Transactional
    public void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(BillDTO.class);
        BillDTO billDTO1 = new BillDTO();
        billDTO1.setId(1L);
        BillDTO billDTO2 = new BillDTO();
        assertThat(billDTO1).isNotEqualTo(billDTO2);
        billDTO2.setId(billDTO1.getId());
        assertThat(billDTO1).isEqualTo(billDTO2);
        billDTO2.setId(2L);
        assertThat(billDTO1).isNotEqualTo(billDTO2);
        billDTO1.setId(null);
        assertThat(billDTO1).isNotEqualTo(billDTO2);
    }

    @Test
    @Transactional
    public void testEntityFromId() {
        assertThat(billMapper.fromId(42L).getId()).isEqualTo(42);
        assertThat(billMapper.fromId(null)).isNull();
    }
}
