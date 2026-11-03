package uz.paykap.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;
import javax.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import uz.paykap.IntegrationTest;
import uz.paykap.domain.ForeignPay;
import uz.paykap.domain.enumeration.ForeignPayStatus;
import uz.paykap.repository.ForeignPayRepository;
import uz.paykap.service.criteria.ForeignPayCriteria;
import uz.paykap.service.dto.ForeignPayDTO;
import uz.paykap.service.mapper.ForeignPayMapper;

/**
 * Integration tests for the {@link ForeignPayResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class ForeignPayResourceIT {

    private static final Instant DEFAULT_VERIFIED_DATE = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_VERIFIED_DATE = Instant.now().truncatedTo(ChronoUnit.MILLIS);

    private static final ForeignPayStatus DEFAULT_STATUS = ForeignPayStatus.NEW;
    private static final ForeignPayStatus UPDATED_STATUS = ForeignPayStatus.VERIFIED;

    private static final String DEFAULT_CARD_TOKEN = "AAAAAAAAAA";
    private static final String UPDATED_CARD_TOKEN = "BBBBBBBBBB";

    private static final Long DEFAULT_BILL_ID = 1L;
    private static final Long UPDATED_BILL_ID = 2L;
    private static final Long SMALLER_BILL_ID = 1L - 1L;

    private static final String DEFAULT_OTP = "AAAAAA";
    private static final String UPDATED_OTP = "BBBBBB";

    private static final String DEFAULT_PHONE = "AAAAAAAAAA";
    private static final String UPDATED_PHONE = "BBBBBBBBBB";

    private static final Integer DEFAULT_ENTRY_COUNT = 1;
    private static final Integer UPDATED_ENTRY_COUNT = 2;
    private static final Integer SMALLER_ENTRY_COUNT = 1 - 1;

    private static final String ENTITY_API_URL = "/api/foreign-pays";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong count = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private ForeignPayRepository foreignPayRepository;

    @Autowired
    private ForeignPayMapper foreignPayMapper;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restForeignPayMockMvc;

    private ForeignPay foreignPay;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static ForeignPay createEntity(EntityManager em) {
        ForeignPay foreignPay = new ForeignPay()
            .verifiedDate(DEFAULT_VERIFIED_DATE)
            .status(DEFAULT_STATUS)
            .cardToken(DEFAULT_CARD_TOKEN)
            .billId(DEFAULT_BILL_ID)
            .otp(DEFAULT_OTP)
            .phone(DEFAULT_PHONE)
            .entryCount(DEFAULT_ENTRY_COUNT);
        return foreignPay;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static ForeignPay createUpdatedEntity(EntityManager em) {
        ForeignPay foreignPay = new ForeignPay()
            .verifiedDate(UPDATED_VERIFIED_DATE)
            .status(UPDATED_STATUS)
            .cardToken(UPDATED_CARD_TOKEN)
            .billId(UPDATED_BILL_ID)
            .otp(UPDATED_OTP)
            .phone(UPDATED_PHONE)
            .entryCount(UPDATED_ENTRY_COUNT);
        return foreignPay;
    }

    @BeforeEach
    public void initTest() {
        foreignPay = createEntity(em);
    }

    @Test
    @Transactional
    void createForeignPay() throws Exception {
        int databaseSizeBeforeCreate = foreignPayRepository.findAll().size();
        // Create the ForeignPay
        ForeignPayDTO foreignPayDTO = foreignPayMapper.toDto(foreignPay);
        restForeignPayMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(foreignPayDTO)))
            .andExpect(status().isCreated());

        // Validate the ForeignPay in the database
        List<ForeignPay> foreignPayList = foreignPayRepository.findAll();
        assertThat(foreignPayList).hasSize(databaseSizeBeforeCreate + 1);
        ForeignPay testForeignPay = foreignPayList.get(foreignPayList.size() - 1);
        assertThat(testForeignPay.getVerifiedDate()).isEqualTo(DEFAULT_VERIFIED_DATE);
        assertThat(testForeignPay.getStatus()).isEqualTo(DEFAULT_STATUS);
        assertThat(testForeignPay.getCardToken()).isEqualTo(DEFAULT_CARD_TOKEN);
        assertThat(testForeignPay.getBillId()).isEqualTo(DEFAULT_BILL_ID);
        assertThat(testForeignPay.getOtp()).isEqualTo(DEFAULT_OTP);
        assertThat(testForeignPay.getPhone()).isEqualTo(DEFAULT_PHONE);
        assertThat(testForeignPay.getEntryCount()).isEqualTo(DEFAULT_ENTRY_COUNT);
    }

    @Test
    @Transactional
    void createForeignPayWithExistingId() throws Exception {
        // Create the ForeignPay with an existing ID
        foreignPay.setId(1L);
        ForeignPayDTO foreignPayDTO = foreignPayMapper.toDto(foreignPay);

        int databaseSizeBeforeCreate = foreignPayRepository.findAll().size();

        // An entity with an existing ID cannot be created, so this API call must fail
        restForeignPayMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(foreignPayDTO)))
            .andExpect(status().isBadRequest());

        // Validate the ForeignPay in the database
        List<ForeignPay> foreignPayList = foreignPayRepository.findAll();
        assertThat(foreignPayList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkVerifiedDateIsRequired() throws Exception {
        int databaseSizeBeforeTest = foreignPayRepository.findAll().size();
        // set the field null
        foreignPay.setVerifiedDate(null);

        // Create the ForeignPay, which fails.
        ForeignPayDTO foreignPayDTO = foreignPayMapper.toDto(foreignPay);

        restForeignPayMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(foreignPayDTO)))
            .andExpect(status().isBadRequest());

        List<ForeignPay> foreignPayList = foreignPayRepository.findAll();
        assertThat(foreignPayList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkStatusIsRequired() throws Exception {
        int databaseSizeBeforeTest = foreignPayRepository.findAll().size();
        // set the field null
        foreignPay.setStatus(null);

        // Create the ForeignPay, which fails.
        ForeignPayDTO foreignPayDTO = foreignPayMapper.toDto(foreignPay);

        restForeignPayMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(foreignPayDTO)))
            .andExpect(status().isBadRequest());

        List<ForeignPay> foreignPayList = foreignPayRepository.findAll();
        assertThat(foreignPayList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkCardTokenIsRequired() throws Exception {
        int databaseSizeBeforeTest = foreignPayRepository.findAll().size();
        // set the field null
        foreignPay.setCardToken(null);

        // Create the ForeignPay, which fails.
        ForeignPayDTO foreignPayDTO = foreignPayMapper.toDto(foreignPay);

        restForeignPayMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(foreignPayDTO)))
            .andExpect(status().isBadRequest());

        List<ForeignPay> foreignPayList = foreignPayRepository.findAll();
        assertThat(foreignPayList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkBillIdIsRequired() throws Exception {
        int databaseSizeBeforeTest = foreignPayRepository.findAll().size();
        // set the field null
        foreignPay.setBillId(null);

        // Create the ForeignPay, which fails.
        ForeignPayDTO foreignPayDTO = foreignPayMapper.toDto(foreignPay);

        restForeignPayMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(foreignPayDTO)))
            .andExpect(status().isBadRequest());

        List<ForeignPay> foreignPayList = foreignPayRepository.findAll();
        assertThat(foreignPayList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkOtpIsRequired() throws Exception {
        int databaseSizeBeforeTest = foreignPayRepository.findAll().size();
        // set the field null
        foreignPay.setOtp(null);

        // Create the ForeignPay, which fails.
        ForeignPayDTO foreignPayDTO = foreignPayMapper.toDto(foreignPay);

        restForeignPayMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(foreignPayDTO)))
            .andExpect(status().isBadRequest());

        List<ForeignPay> foreignPayList = foreignPayRepository.findAll();
        assertThat(foreignPayList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkPhoneIsRequired() throws Exception {
        int databaseSizeBeforeTest = foreignPayRepository.findAll().size();
        // set the field null
        foreignPay.setPhone(null);

        // Create the ForeignPay, which fails.
        ForeignPayDTO foreignPayDTO = foreignPayMapper.toDto(foreignPay);

        restForeignPayMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(foreignPayDTO)))
            .andExpect(status().isBadRequest());

        List<ForeignPay> foreignPayList = foreignPayRepository.findAll();
        assertThat(foreignPayList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkEntryCountIsRequired() throws Exception {
        int databaseSizeBeforeTest = foreignPayRepository.findAll().size();
        // set the field null
        foreignPay.setEntryCount(null);

        // Create the ForeignPay, which fails.
        ForeignPayDTO foreignPayDTO = foreignPayMapper.toDto(foreignPay);

        restForeignPayMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(foreignPayDTO)))
            .andExpect(status().isBadRequest());

        List<ForeignPay> foreignPayList = foreignPayRepository.findAll();
        assertThat(foreignPayList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllForeignPays() throws Exception {
        // Initialize the database
        foreignPayRepository.saveAndFlush(foreignPay);

        // Get all the foreignPayList
        restForeignPayMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(foreignPay.getId().intValue())))
            .andExpect(jsonPath("$.[*].verifiedDate").value(hasItem(DEFAULT_VERIFIED_DATE.toString())))
            .andExpect(jsonPath("$.[*].status").value(hasItem(DEFAULT_STATUS.toString())))
            .andExpect(jsonPath("$.[*].cardToken").value(hasItem(DEFAULT_CARD_TOKEN)))
            .andExpect(jsonPath("$.[*].billId").value(hasItem(DEFAULT_BILL_ID.intValue())))
            .andExpect(jsonPath("$.[*].otp").value(hasItem(DEFAULT_OTP)))
            .andExpect(jsonPath("$.[*].phone").value(hasItem(DEFAULT_PHONE)))
            .andExpect(jsonPath("$.[*].entryCount").value(hasItem(DEFAULT_ENTRY_COUNT)));
    }

    @Test
    @Transactional
    void getForeignPay() throws Exception {
        // Initialize the database
        foreignPayRepository.saveAndFlush(foreignPay);

        // Get the foreignPay
        restForeignPayMockMvc
            .perform(get(ENTITY_API_URL_ID, foreignPay.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(foreignPay.getId().intValue()))
            .andExpect(jsonPath("$.verifiedDate").value(DEFAULT_VERIFIED_DATE.toString()))
            .andExpect(jsonPath("$.status").value(DEFAULT_STATUS.toString()))
            .andExpect(jsonPath("$.cardToken").value(DEFAULT_CARD_TOKEN))
            .andExpect(jsonPath("$.billId").value(DEFAULT_BILL_ID.intValue()))
            .andExpect(jsonPath("$.otp").value(DEFAULT_OTP))
            .andExpect(jsonPath("$.phone").value(DEFAULT_PHONE))
            .andExpect(jsonPath("$.entryCount").value(DEFAULT_ENTRY_COUNT));
    }

    @Test
    @Transactional
    void getForeignPaysByIdFiltering() throws Exception {
        // Initialize the database
        foreignPayRepository.saveAndFlush(foreignPay);

        Long id = foreignPay.getId();

        defaultForeignPayShouldBeFound("id.equals=" + id);
        defaultForeignPayShouldNotBeFound("id.notEquals=" + id);

        defaultForeignPayShouldBeFound("id.greaterThanOrEqual=" + id);
        defaultForeignPayShouldNotBeFound("id.greaterThan=" + id);

        defaultForeignPayShouldBeFound("id.lessThanOrEqual=" + id);
        defaultForeignPayShouldNotBeFound("id.lessThan=" + id);
    }

    @Test
    @Transactional
    void getAllForeignPaysByVerifiedDateIsEqualToSomething() throws Exception {
        // Initialize the database
        foreignPayRepository.saveAndFlush(foreignPay);

        // Get all the foreignPayList where verifiedDate equals to DEFAULT_VERIFIED_DATE
        defaultForeignPayShouldBeFound("verifiedDate.equals=" + DEFAULT_VERIFIED_DATE);

        // Get all the foreignPayList where verifiedDate equals to UPDATED_VERIFIED_DATE
        defaultForeignPayShouldNotBeFound("verifiedDate.equals=" + UPDATED_VERIFIED_DATE);
    }

    @Test
    @Transactional
    void getAllForeignPaysByVerifiedDateIsNotEqualToSomething() throws Exception {
        // Initialize the database
        foreignPayRepository.saveAndFlush(foreignPay);

        // Get all the foreignPayList where verifiedDate not equals to DEFAULT_VERIFIED_DATE
        defaultForeignPayShouldNotBeFound("verifiedDate.notEquals=" + DEFAULT_VERIFIED_DATE);

        // Get all the foreignPayList where verifiedDate not equals to UPDATED_VERIFIED_DATE
        defaultForeignPayShouldBeFound("verifiedDate.notEquals=" + UPDATED_VERIFIED_DATE);
    }

    @Test
    @Transactional
    void getAllForeignPaysByVerifiedDateIsInShouldWork() throws Exception {
        // Initialize the database
        foreignPayRepository.saveAndFlush(foreignPay);

        // Get all the foreignPayList where verifiedDate in DEFAULT_VERIFIED_DATE or UPDATED_VERIFIED_DATE
        defaultForeignPayShouldBeFound("verifiedDate.in=" + DEFAULT_VERIFIED_DATE + "," + UPDATED_VERIFIED_DATE);

        // Get all the foreignPayList where verifiedDate equals to UPDATED_VERIFIED_DATE
        defaultForeignPayShouldNotBeFound("verifiedDate.in=" + UPDATED_VERIFIED_DATE);
    }

    @Test
    @Transactional
    void getAllForeignPaysByVerifiedDateIsNullOrNotNull() throws Exception {
        // Initialize the database
        foreignPayRepository.saveAndFlush(foreignPay);

        // Get all the foreignPayList where verifiedDate is not null
        defaultForeignPayShouldBeFound("verifiedDate.specified=true");

        // Get all the foreignPayList where verifiedDate is null
        defaultForeignPayShouldNotBeFound("verifiedDate.specified=false");
    }

    @Test
    @Transactional
    void getAllForeignPaysByStatusIsEqualToSomething() throws Exception {
        // Initialize the database
        foreignPayRepository.saveAndFlush(foreignPay);

        // Get all the foreignPayList where status equals to DEFAULT_STATUS
        defaultForeignPayShouldBeFound("status.equals=" + DEFAULT_STATUS);

        // Get all the foreignPayList where status equals to UPDATED_STATUS
        defaultForeignPayShouldNotBeFound("status.equals=" + UPDATED_STATUS);
    }

    @Test
    @Transactional
    void getAllForeignPaysByStatusIsNotEqualToSomething() throws Exception {
        // Initialize the database
        foreignPayRepository.saveAndFlush(foreignPay);

        // Get all the foreignPayList where status not equals to DEFAULT_STATUS
        defaultForeignPayShouldNotBeFound("status.notEquals=" + DEFAULT_STATUS);

        // Get all the foreignPayList where status not equals to UPDATED_STATUS
        defaultForeignPayShouldBeFound("status.notEquals=" + UPDATED_STATUS);
    }

    @Test
    @Transactional
    void getAllForeignPaysByStatusIsInShouldWork() throws Exception {
        // Initialize the database
        foreignPayRepository.saveAndFlush(foreignPay);

        // Get all the foreignPayList where status in DEFAULT_STATUS or UPDATED_STATUS
        defaultForeignPayShouldBeFound("status.in=" + DEFAULT_STATUS + "," + UPDATED_STATUS);

        // Get all the foreignPayList where status equals to UPDATED_STATUS
        defaultForeignPayShouldNotBeFound("status.in=" + UPDATED_STATUS);
    }

    @Test
    @Transactional
    void getAllForeignPaysByStatusIsNullOrNotNull() throws Exception {
        // Initialize the database
        foreignPayRepository.saveAndFlush(foreignPay);

        // Get all the foreignPayList where status is not null
        defaultForeignPayShouldBeFound("status.specified=true");

        // Get all the foreignPayList where status is null
        defaultForeignPayShouldNotBeFound("status.specified=false");
    }

    @Test
    @Transactional
    void getAllForeignPaysByCardTokenIsEqualToSomething() throws Exception {
        // Initialize the database
        foreignPayRepository.saveAndFlush(foreignPay);

        // Get all the foreignPayList where cardToken equals to DEFAULT_CARD_TOKEN
        defaultForeignPayShouldBeFound("cardToken.equals=" + DEFAULT_CARD_TOKEN);

        // Get all the foreignPayList where cardToken equals to UPDATED_CARD_TOKEN
        defaultForeignPayShouldNotBeFound("cardToken.equals=" + UPDATED_CARD_TOKEN);
    }

    @Test
    @Transactional
    void getAllForeignPaysByCardTokenIsNotEqualToSomething() throws Exception {
        // Initialize the database
        foreignPayRepository.saveAndFlush(foreignPay);

        // Get all the foreignPayList where cardToken not equals to DEFAULT_CARD_TOKEN
        defaultForeignPayShouldNotBeFound("cardToken.notEquals=" + DEFAULT_CARD_TOKEN);

        // Get all the foreignPayList where cardToken not equals to UPDATED_CARD_TOKEN
        defaultForeignPayShouldBeFound("cardToken.notEquals=" + UPDATED_CARD_TOKEN);
    }

    @Test
    @Transactional
    void getAllForeignPaysByCardTokenIsInShouldWork() throws Exception {
        // Initialize the database
        foreignPayRepository.saveAndFlush(foreignPay);

        // Get all the foreignPayList where cardToken in DEFAULT_CARD_TOKEN or UPDATED_CARD_TOKEN
        defaultForeignPayShouldBeFound("cardToken.in=" + DEFAULT_CARD_TOKEN + "," + UPDATED_CARD_TOKEN);

        // Get all the foreignPayList where cardToken equals to UPDATED_CARD_TOKEN
        defaultForeignPayShouldNotBeFound("cardToken.in=" + UPDATED_CARD_TOKEN);
    }

    @Test
    @Transactional
    void getAllForeignPaysByCardTokenIsNullOrNotNull() throws Exception {
        // Initialize the database
        foreignPayRepository.saveAndFlush(foreignPay);

        // Get all the foreignPayList where cardToken is not null
        defaultForeignPayShouldBeFound("cardToken.specified=true");

        // Get all the foreignPayList where cardToken is null
        defaultForeignPayShouldNotBeFound("cardToken.specified=false");
    }

    @Test
    @Transactional
    void getAllForeignPaysByCardTokenContainsSomething() throws Exception {
        // Initialize the database
        foreignPayRepository.saveAndFlush(foreignPay);

        // Get all the foreignPayList where cardToken contains DEFAULT_CARD_TOKEN
        defaultForeignPayShouldBeFound("cardToken.contains=" + DEFAULT_CARD_TOKEN);

        // Get all the foreignPayList where cardToken contains UPDATED_CARD_TOKEN
        defaultForeignPayShouldNotBeFound("cardToken.contains=" + UPDATED_CARD_TOKEN);
    }

    @Test
    @Transactional
    void getAllForeignPaysByCardTokenNotContainsSomething() throws Exception {
        // Initialize the database
        foreignPayRepository.saveAndFlush(foreignPay);

        // Get all the foreignPayList where cardToken does not contain DEFAULT_CARD_TOKEN
        defaultForeignPayShouldNotBeFound("cardToken.doesNotContain=" + DEFAULT_CARD_TOKEN);

        // Get all the foreignPayList where cardToken does not contain UPDATED_CARD_TOKEN
        defaultForeignPayShouldBeFound("cardToken.doesNotContain=" + UPDATED_CARD_TOKEN);
    }

    @Test
    @Transactional
    void getAllForeignPaysByBillIdIsEqualToSomething() throws Exception {
        // Initialize the database
        foreignPayRepository.saveAndFlush(foreignPay);

        // Get all the foreignPayList where billId equals to DEFAULT_BILL_ID
        defaultForeignPayShouldBeFound("billId.equals=" + DEFAULT_BILL_ID);

        // Get all the foreignPayList where billId equals to UPDATED_BILL_ID
        defaultForeignPayShouldNotBeFound("billId.equals=" + UPDATED_BILL_ID);
    }

    @Test
    @Transactional
    void getAllForeignPaysByBillIdIsNotEqualToSomething() throws Exception {
        // Initialize the database
        foreignPayRepository.saveAndFlush(foreignPay);

        // Get all the foreignPayList where billId not equals to DEFAULT_BILL_ID
        defaultForeignPayShouldNotBeFound("billId.notEquals=" + DEFAULT_BILL_ID);

        // Get all the foreignPayList where billId not equals to UPDATED_BILL_ID
        defaultForeignPayShouldBeFound("billId.notEquals=" + UPDATED_BILL_ID);
    }

    @Test
    @Transactional
    void getAllForeignPaysByBillIdIsInShouldWork() throws Exception {
        // Initialize the database
        foreignPayRepository.saveAndFlush(foreignPay);

        // Get all the foreignPayList where billId in DEFAULT_BILL_ID or UPDATED_BILL_ID
        defaultForeignPayShouldBeFound("billId.in=" + DEFAULT_BILL_ID + "," + UPDATED_BILL_ID);

        // Get all the foreignPayList where billId equals to UPDATED_BILL_ID
        defaultForeignPayShouldNotBeFound("billId.in=" + UPDATED_BILL_ID);
    }

    @Test
    @Transactional
    void getAllForeignPaysByBillIdIsNullOrNotNull() throws Exception {
        // Initialize the database
        foreignPayRepository.saveAndFlush(foreignPay);

        // Get all the foreignPayList where billId is not null
        defaultForeignPayShouldBeFound("billId.specified=true");

        // Get all the foreignPayList where billId is null
        defaultForeignPayShouldNotBeFound("billId.specified=false");
    }

    @Test
    @Transactional
    void getAllForeignPaysByBillIdIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        foreignPayRepository.saveAndFlush(foreignPay);

        // Get all the foreignPayList where billId is greater than or equal to DEFAULT_BILL_ID
        defaultForeignPayShouldBeFound("billId.greaterThanOrEqual=" + DEFAULT_BILL_ID);

        // Get all the foreignPayList where billId is greater than or equal to UPDATED_BILL_ID
        defaultForeignPayShouldNotBeFound("billId.greaterThanOrEqual=" + UPDATED_BILL_ID);
    }

    @Test
    @Transactional
    void getAllForeignPaysByBillIdIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        foreignPayRepository.saveAndFlush(foreignPay);

        // Get all the foreignPayList where billId is less than or equal to DEFAULT_BILL_ID
        defaultForeignPayShouldBeFound("billId.lessThanOrEqual=" + DEFAULT_BILL_ID);

        // Get all the foreignPayList where billId is less than or equal to SMALLER_BILL_ID
        defaultForeignPayShouldNotBeFound("billId.lessThanOrEqual=" + SMALLER_BILL_ID);
    }

    @Test
    @Transactional
    void getAllForeignPaysByBillIdIsLessThanSomething() throws Exception {
        // Initialize the database
        foreignPayRepository.saveAndFlush(foreignPay);

        // Get all the foreignPayList where billId is less than DEFAULT_BILL_ID
        defaultForeignPayShouldNotBeFound("billId.lessThan=" + DEFAULT_BILL_ID);

        // Get all the foreignPayList where billId is less than UPDATED_BILL_ID
        defaultForeignPayShouldBeFound("billId.lessThan=" + UPDATED_BILL_ID);
    }

    @Test
    @Transactional
    void getAllForeignPaysByBillIdIsGreaterThanSomething() throws Exception {
        // Initialize the database
        foreignPayRepository.saveAndFlush(foreignPay);

        // Get all the foreignPayList where billId is greater than DEFAULT_BILL_ID
        defaultForeignPayShouldNotBeFound("billId.greaterThan=" + DEFAULT_BILL_ID);

        // Get all the foreignPayList where billId is greater than SMALLER_BILL_ID
        defaultForeignPayShouldBeFound("billId.greaterThan=" + SMALLER_BILL_ID);
    }

    @Test
    @Transactional
    void getAllForeignPaysByOtpIsEqualToSomething() throws Exception {
        // Initialize the database
        foreignPayRepository.saveAndFlush(foreignPay);

        // Get all the foreignPayList where otp equals to DEFAULT_OTP
        defaultForeignPayShouldBeFound("otp.equals=" + DEFAULT_OTP);

        // Get all the foreignPayList where otp equals to UPDATED_OTP
        defaultForeignPayShouldNotBeFound("otp.equals=" + UPDATED_OTP);
    }

    @Test
    @Transactional
    void getAllForeignPaysByOtpIsNotEqualToSomething() throws Exception {
        // Initialize the database
        foreignPayRepository.saveAndFlush(foreignPay);

        // Get all the foreignPayList where otp not equals to DEFAULT_OTP
        defaultForeignPayShouldNotBeFound("otp.notEquals=" + DEFAULT_OTP);

        // Get all the foreignPayList where otp not equals to UPDATED_OTP
        defaultForeignPayShouldBeFound("otp.notEquals=" + UPDATED_OTP);
    }

    @Test
    @Transactional
    void getAllForeignPaysByOtpIsInShouldWork() throws Exception {
        // Initialize the database
        foreignPayRepository.saveAndFlush(foreignPay);

        // Get all the foreignPayList where otp in DEFAULT_OTP or UPDATED_OTP
        defaultForeignPayShouldBeFound("otp.in=" + DEFAULT_OTP + "," + UPDATED_OTP);

        // Get all the foreignPayList where otp equals to UPDATED_OTP
        defaultForeignPayShouldNotBeFound("otp.in=" + UPDATED_OTP);
    }

    @Test
    @Transactional
    void getAllForeignPaysByOtpIsNullOrNotNull() throws Exception {
        // Initialize the database
        foreignPayRepository.saveAndFlush(foreignPay);

        // Get all the foreignPayList where otp is not null
        defaultForeignPayShouldBeFound("otp.specified=true");

        // Get all the foreignPayList where otp is null
        defaultForeignPayShouldNotBeFound("otp.specified=false");
    }

    @Test
    @Transactional
    void getAllForeignPaysByOtpContainsSomething() throws Exception {
        // Initialize the database
        foreignPayRepository.saveAndFlush(foreignPay);

        // Get all the foreignPayList where otp contains DEFAULT_OTP
        defaultForeignPayShouldBeFound("otp.contains=" + DEFAULT_OTP);

        // Get all the foreignPayList where otp contains UPDATED_OTP
        defaultForeignPayShouldNotBeFound("otp.contains=" + UPDATED_OTP);
    }

    @Test
    @Transactional
    void getAllForeignPaysByOtpNotContainsSomething() throws Exception {
        // Initialize the database
        foreignPayRepository.saveAndFlush(foreignPay);

        // Get all the foreignPayList where otp does not contain DEFAULT_OTP
        defaultForeignPayShouldNotBeFound("otp.doesNotContain=" + DEFAULT_OTP);

        // Get all the foreignPayList where otp does not contain UPDATED_OTP
        defaultForeignPayShouldBeFound("otp.doesNotContain=" + UPDATED_OTP);
    }

    @Test
    @Transactional
    void getAllForeignPaysByPhoneIsEqualToSomething() throws Exception {
        // Initialize the database
        foreignPayRepository.saveAndFlush(foreignPay);

        // Get all the foreignPayList where phone equals to DEFAULT_PHONE
        defaultForeignPayShouldBeFound("phone.equals=" + DEFAULT_PHONE);

        // Get all the foreignPayList where phone equals to UPDATED_PHONE
        defaultForeignPayShouldNotBeFound("phone.equals=" + UPDATED_PHONE);
    }

    @Test
    @Transactional
    void getAllForeignPaysByPhoneIsNotEqualToSomething() throws Exception {
        // Initialize the database
        foreignPayRepository.saveAndFlush(foreignPay);

        // Get all the foreignPayList where phone not equals to DEFAULT_PHONE
        defaultForeignPayShouldNotBeFound("phone.notEquals=" + DEFAULT_PHONE);

        // Get all the foreignPayList where phone not equals to UPDATED_PHONE
        defaultForeignPayShouldBeFound("phone.notEquals=" + UPDATED_PHONE);
    }

    @Test
    @Transactional
    void getAllForeignPaysByPhoneIsInShouldWork() throws Exception {
        // Initialize the database
        foreignPayRepository.saveAndFlush(foreignPay);

        // Get all the foreignPayList where phone in DEFAULT_PHONE or UPDATED_PHONE
        defaultForeignPayShouldBeFound("phone.in=" + DEFAULT_PHONE + "," + UPDATED_PHONE);

        // Get all the foreignPayList where phone equals to UPDATED_PHONE
        defaultForeignPayShouldNotBeFound("phone.in=" + UPDATED_PHONE);
    }

    @Test
    @Transactional
    void getAllForeignPaysByPhoneIsNullOrNotNull() throws Exception {
        // Initialize the database
        foreignPayRepository.saveAndFlush(foreignPay);

        // Get all the foreignPayList where phone is not null
        defaultForeignPayShouldBeFound("phone.specified=true");

        // Get all the foreignPayList where phone is null
        defaultForeignPayShouldNotBeFound("phone.specified=false");
    }

    @Test
    @Transactional
    void getAllForeignPaysByPhoneContainsSomething() throws Exception {
        // Initialize the database
        foreignPayRepository.saveAndFlush(foreignPay);

        // Get all the foreignPayList where phone contains DEFAULT_PHONE
        defaultForeignPayShouldBeFound("phone.contains=" + DEFAULT_PHONE);

        // Get all the foreignPayList where phone contains UPDATED_PHONE
        defaultForeignPayShouldNotBeFound("phone.contains=" + UPDATED_PHONE);
    }

    @Test
    @Transactional
    void getAllForeignPaysByPhoneNotContainsSomething() throws Exception {
        // Initialize the database
        foreignPayRepository.saveAndFlush(foreignPay);

        // Get all the foreignPayList where phone does not contain DEFAULT_PHONE
        defaultForeignPayShouldNotBeFound("phone.doesNotContain=" + DEFAULT_PHONE);

        // Get all the foreignPayList where phone does not contain UPDATED_PHONE
        defaultForeignPayShouldBeFound("phone.doesNotContain=" + UPDATED_PHONE);
    }

    @Test
    @Transactional
    void getAllForeignPaysByEntryCountIsEqualToSomething() throws Exception {
        // Initialize the database
        foreignPayRepository.saveAndFlush(foreignPay);

        // Get all the foreignPayList where entryCount equals to DEFAULT_ENTRY_COUNT
        defaultForeignPayShouldBeFound("entryCount.equals=" + DEFAULT_ENTRY_COUNT);

        // Get all the foreignPayList where entryCount equals to UPDATED_ENTRY_COUNT
        defaultForeignPayShouldNotBeFound("entryCount.equals=" + UPDATED_ENTRY_COUNT);
    }

    @Test
    @Transactional
    void getAllForeignPaysByEntryCountIsNotEqualToSomething() throws Exception {
        // Initialize the database
        foreignPayRepository.saveAndFlush(foreignPay);

        // Get all the foreignPayList where entryCount not equals to DEFAULT_ENTRY_COUNT
        defaultForeignPayShouldNotBeFound("entryCount.notEquals=" + DEFAULT_ENTRY_COUNT);

        // Get all the foreignPayList where entryCount not equals to UPDATED_ENTRY_COUNT
        defaultForeignPayShouldBeFound("entryCount.notEquals=" + UPDATED_ENTRY_COUNT);
    }

    @Test
    @Transactional
    void getAllForeignPaysByEntryCountIsInShouldWork() throws Exception {
        // Initialize the database
        foreignPayRepository.saveAndFlush(foreignPay);

        // Get all the foreignPayList where entryCount in DEFAULT_ENTRY_COUNT or UPDATED_ENTRY_COUNT
        defaultForeignPayShouldBeFound("entryCount.in=" + DEFAULT_ENTRY_COUNT + "," + UPDATED_ENTRY_COUNT);

        // Get all the foreignPayList where entryCount equals to UPDATED_ENTRY_COUNT
        defaultForeignPayShouldNotBeFound("entryCount.in=" + UPDATED_ENTRY_COUNT);
    }

    @Test
    @Transactional
    void getAllForeignPaysByEntryCountIsNullOrNotNull() throws Exception {
        // Initialize the database
        foreignPayRepository.saveAndFlush(foreignPay);

        // Get all the foreignPayList where entryCount is not null
        defaultForeignPayShouldBeFound("entryCount.specified=true");

        // Get all the foreignPayList where entryCount is null
        defaultForeignPayShouldNotBeFound("entryCount.specified=false");
    }

    @Test
    @Transactional
    void getAllForeignPaysByEntryCountIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        foreignPayRepository.saveAndFlush(foreignPay);

        // Get all the foreignPayList where entryCount is greater than or equal to DEFAULT_ENTRY_COUNT
        defaultForeignPayShouldBeFound("entryCount.greaterThanOrEqual=" + DEFAULT_ENTRY_COUNT);

        // Get all the foreignPayList where entryCount is greater than or equal to UPDATED_ENTRY_COUNT
        defaultForeignPayShouldNotBeFound("entryCount.greaterThanOrEqual=" + UPDATED_ENTRY_COUNT);
    }

    @Test
    @Transactional
    void getAllForeignPaysByEntryCountIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        foreignPayRepository.saveAndFlush(foreignPay);

        // Get all the foreignPayList where entryCount is less than or equal to DEFAULT_ENTRY_COUNT
        defaultForeignPayShouldBeFound("entryCount.lessThanOrEqual=" + DEFAULT_ENTRY_COUNT);

        // Get all the foreignPayList where entryCount is less than or equal to SMALLER_ENTRY_COUNT
        defaultForeignPayShouldNotBeFound("entryCount.lessThanOrEqual=" + SMALLER_ENTRY_COUNT);
    }

    @Test
    @Transactional
    void getAllForeignPaysByEntryCountIsLessThanSomething() throws Exception {
        // Initialize the database
        foreignPayRepository.saveAndFlush(foreignPay);

        // Get all the foreignPayList where entryCount is less than DEFAULT_ENTRY_COUNT
        defaultForeignPayShouldNotBeFound("entryCount.lessThan=" + DEFAULT_ENTRY_COUNT);

        // Get all the foreignPayList where entryCount is less than UPDATED_ENTRY_COUNT
        defaultForeignPayShouldBeFound("entryCount.lessThan=" + UPDATED_ENTRY_COUNT);
    }

    @Test
    @Transactional
    void getAllForeignPaysByEntryCountIsGreaterThanSomething() throws Exception {
        // Initialize the database
        foreignPayRepository.saveAndFlush(foreignPay);

        // Get all the foreignPayList where entryCount is greater than DEFAULT_ENTRY_COUNT
        defaultForeignPayShouldNotBeFound("entryCount.greaterThan=" + DEFAULT_ENTRY_COUNT);

        // Get all the foreignPayList where entryCount is greater than SMALLER_ENTRY_COUNT
        defaultForeignPayShouldBeFound("entryCount.greaterThan=" + SMALLER_ENTRY_COUNT);
    }

    /**
     * Executes the search, and checks that the default entity is returned.
     */
    private void defaultForeignPayShouldBeFound(String filter) throws Exception {
        restForeignPayMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(foreignPay.getId().intValue())))
            .andExpect(jsonPath("$.[*].verifiedDate").value(hasItem(DEFAULT_VERIFIED_DATE.toString())))
            .andExpect(jsonPath("$.[*].status").value(hasItem(DEFAULT_STATUS.toString())))
            .andExpect(jsonPath("$.[*].cardToken").value(hasItem(DEFAULT_CARD_TOKEN)))
            .andExpect(jsonPath("$.[*].billId").value(hasItem(DEFAULT_BILL_ID.intValue())))
            .andExpect(jsonPath("$.[*].otp").value(hasItem(DEFAULT_OTP)))
            .andExpect(jsonPath("$.[*].phone").value(hasItem(DEFAULT_PHONE)))
            .andExpect(jsonPath("$.[*].entryCount").value(hasItem(DEFAULT_ENTRY_COUNT)));

        // Check, that the count call also returns 1
        restForeignPayMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("1"));
    }

    /**
     * Executes the search, and checks that the default entity is not returned.
     */
    private void defaultForeignPayShouldNotBeFound(String filter) throws Exception {
        restForeignPayMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty());

        // Check, that the count call also returns 0
        restForeignPayMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("0"));
    }

    @Test
    @Transactional
    void getNonExistingForeignPay() throws Exception {
        // Get the foreignPay
        restForeignPayMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putNewForeignPay() throws Exception {
        // Initialize the database
        foreignPayRepository.saveAndFlush(foreignPay);

        int databaseSizeBeforeUpdate = foreignPayRepository.findAll().size();

        // Update the foreignPay
        ForeignPay updatedForeignPay = foreignPayRepository.findById(foreignPay.getId()).get();
        // Disconnect from session so that the updates on updatedForeignPay are not directly saved in db
        em.detach(updatedForeignPay);
        updatedForeignPay
            .verifiedDate(UPDATED_VERIFIED_DATE)
            .status(UPDATED_STATUS)
            .cardToken(UPDATED_CARD_TOKEN)
            .billId(UPDATED_BILL_ID)
            .otp(UPDATED_OTP)
            .phone(UPDATED_PHONE)
            .entryCount(UPDATED_ENTRY_COUNT);
        ForeignPayDTO foreignPayDTO = foreignPayMapper.toDto(updatedForeignPay);

        restForeignPayMockMvc
            .perform(
                put(ENTITY_API_URL_ID, foreignPayDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(foreignPayDTO))
            )
            .andExpect(status().isOk());

        // Validate the ForeignPay in the database
        List<ForeignPay> foreignPayList = foreignPayRepository.findAll();
        assertThat(foreignPayList).hasSize(databaseSizeBeforeUpdate);
        ForeignPay testForeignPay = foreignPayList.get(foreignPayList.size() - 1);
        assertThat(testForeignPay.getVerifiedDate()).isEqualTo(UPDATED_VERIFIED_DATE);
        assertThat(testForeignPay.getStatus()).isEqualTo(UPDATED_STATUS);
        assertThat(testForeignPay.getCardToken()).isEqualTo(UPDATED_CARD_TOKEN);
        assertThat(testForeignPay.getBillId()).isEqualTo(UPDATED_BILL_ID);
        assertThat(testForeignPay.getOtp()).isEqualTo(UPDATED_OTP);
        assertThat(testForeignPay.getPhone()).isEqualTo(UPDATED_PHONE);
        assertThat(testForeignPay.getEntryCount()).isEqualTo(UPDATED_ENTRY_COUNT);
    }

    @Test
    @Transactional
    void putNonExistingForeignPay() throws Exception {
        int databaseSizeBeforeUpdate = foreignPayRepository.findAll().size();
        foreignPay.setId(count.incrementAndGet());

        // Create the ForeignPay
        ForeignPayDTO foreignPayDTO = foreignPayMapper.toDto(foreignPay);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restForeignPayMockMvc
            .perform(
                put(ENTITY_API_URL_ID, foreignPayDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(foreignPayDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the ForeignPay in the database
        List<ForeignPay> foreignPayList = foreignPayRepository.findAll();
        assertThat(foreignPayList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchForeignPay() throws Exception {
        int databaseSizeBeforeUpdate = foreignPayRepository.findAll().size();
        foreignPay.setId(count.incrementAndGet());

        // Create the ForeignPay
        ForeignPayDTO foreignPayDTO = foreignPayMapper.toDto(foreignPay);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restForeignPayMockMvc
            .perform(
                put(ENTITY_API_URL_ID, count.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(foreignPayDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the ForeignPay in the database
        List<ForeignPay> foreignPayList = foreignPayRepository.findAll();
        assertThat(foreignPayList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamForeignPay() throws Exception {
        int databaseSizeBeforeUpdate = foreignPayRepository.findAll().size();
        foreignPay.setId(count.incrementAndGet());

        // Create the ForeignPay
        ForeignPayDTO foreignPayDTO = foreignPayMapper.toDto(foreignPay);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restForeignPayMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(foreignPayDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the ForeignPay in the database
        List<ForeignPay> foreignPayList = foreignPayRepository.findAll();
        assertThat(foreignPayList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateForeignPayWithPatch() throws Exception {
        // Initialize the database
        foreignPayRepository.saveAndFlush(foreignPay);

        int databaseSizeBeforeUpdate = foreignPayRepository.findAll().size();

        // Update the foreignPay using partial update
        ForeignPay partialUpdatedForeignPay = new ForeignPay();
        partialUpdatedForeignPay.setId(foreignPay.getId());

        partialUpdatedForeignPay
            .status(UPDATED_STATUS)
            .cardToken(UPDATED_CARD_TOKEN)
            .billId(UPDATED_BILL_ID)
            .otp(UPDATED_OTP)
            .entryCount(UPDATED_ENTRY_COUNT);

        restForeignPayMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedForeignPay.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedForeignPay))
            )
            .andExpect(status().isOk());

        // Validate the ForeignPay in the database
        List<ForeignPay> foreignPayList = foreignPayRepository.findAll();
        assertThat(foreignPayList).hasSize(databaseSizeBeforeUpdate);
        ForeignPay testForeignPay = foreignPayList.get(foreignPayList.size() - 1);
        assertThat(testForeignPay.getVerifiedDate()).isEqualTo(DEFAULT_VERIFIED_DATE);
        assertThat(testForeignPay.getStatus()).isEqualTo(UPDATED_STATUS);
        assertThat(testForeignPay.getCardToken()).isEqualTo(UPDATED_CARD_TOKEN);
        assertThat(testForeignPay.getBillId()).isEqualTo(UPDATED_BILL_ID);
        assertThat(testForeignPay.getOtp()).isEqualTo(UPDATED_OTP);
        assertThat(testForeignPay.getPhone()).isEqualTo(DEFAULT_PHONE);
        assertThat(testForeignPay.getEntryCount()).isEqualTo(UPDATED_ENTRY_COUNT);
    }

    @Test
    @Transactional
    void fullUpdateForeignPayWithPatch() throws Exception {
        // Initialize the database
        foreignPayRepository.saveAndFlush(foreignPay);

        int databaseSizeBeforeUpdate = foreignPayRepository.findAll().size();

        // Update the foreignPay using partial update
        ForeignPay partialUpdatedForeignPay = new ForeignPay();
        partialUpdatedForeignPay.setId(foreignPay.getId());

        partialUpdatedForeignPay
            .verifiedDate(UPDATED_VERIFIED_DATE)
            .status(UPDATED_STATUS)
            .cardToken(UPDATED_CARD_TOKEN)
            .billId(UPDATED_BILL_ID)
            .otp(UPDATED_OTP)
            .phone(UPDATED_PHONE)
            .entryCount(UPDATED_ENTRY_COUNT);

        restForeignPayMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedForeignPay.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedForeignPay))
            )
            .andExpect(status().isOk());

        // Validate the ForeignPay in the database
        List<ForeignPay> foreignPayList = foreignPayRepository.findAll();
        assertThat(foreignPayList).hasSize(databaseSizeBeforeUpdate);
        ForeignPay testForeignPay = foreignPayList.get(foreignPayList.size() - 1);
        assertThat(testForeignPay.getVerifiedDate()).isEqualTo(UPDATED_VERIFIED_DATE);
        assertThat(testForeignPay.getStatus()).isEqualTo(UPDATED_STATUS);
        assertThat(testForeignPay.getCardToken()).isEqualTo(UPDATED_CARD_TOKEN);
        assertThat(testForeignPay.getBillId()).isEqualTo(UPDATED_BILL_ID);
        assertThat(testForeignPay.getOtp()).isEqualTo(UPDATED_OTP);
        assertThat(testForeignPay.getPhone()).isEqualTo(UPDATED_PHONE);
        assertThat(testForeignPay.getEntryCount()).isEqualTo(UPDATED_ENTRY_COUNT);
    }

    @Test
    @Transactional
    void patchNonExistingForeignPay() throws Exception {
        int databaseSizeBeforeUpdate = foreignPayRepository.findAll().size();
        foreignPay.setId(count.incrementAndGet());

        // Create the ForeignPay
        ForeignPayDTO foreignPayDTO = foreignPayMapper.toDto(foreignPay);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restForeignPayMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, foreignPayDTO.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(foreignPayDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the ForeignPay in the database
        List<ForeignPay> foreignPayList = foreignPayRepository.findAll();
        assertThat(foreignPayList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchForeignPay() throws Exception {
        int databaseSizeBeforeUpdate = foreignPayRepository.findAll().size();
        foreignPay.setId(count.incrementAndGet());

        // Create the ForeignPay
        ForeignPayDTO foreignPayDTO = foreignPayMapper.toDto(foreignPay);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restForeignPayMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, count.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(foreignPayDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the ForeignPay in the database
        List<ForeignPay> foreignPayList = foreignPayRepository.findAll();
        assertThat(foreignPayList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamForeignPay() throws Exception {
        int databaseSizeBeforeUpdate = foreignPayRepository.findAll().size();
        foreignPay.setId(count.incrementAndGet());

        // Create the ForeignPay
        ForeignPayDTO foreignPayDTO = foreignPayMapper.toDto(foreignPay);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restForeignPayMockMvc
            .perform(
                patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(TestUtil.convertObjectToJsonBytes(foreignPayDTO))
            )
            .andExpect(status().isMethodNotAllowed());

        // Validate the ForeignPay in the database
        List<ForeignPay> foreignPayList = foreignPayRepository.findAll();
        assertThat(foreignPayList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteForeignPay() throws Exception {
        // Initialize the database
        foreignPayRepository.saveAndFlush(foreignPay);

        int databaseSizeBeforeDelete = foreignPayRepository.findAll().size();

        // Delete the foreignPay
        restForeignPayMockMvc
            .perform(delete(ENTITY_API_URL_ID, foreignPay.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        List<ForeignPay> foreignPayList = foreignPayRepository.findAll();
        assertThat(foreignPayList).hasSize(databaseSizeBeforeDelete - 1);
    }
}
