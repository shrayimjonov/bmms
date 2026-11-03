package uz.paykap.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static uz.paykap.web.rest.TestUtil.createFormattingConversionService;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import javax.persistence.EntityManager;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import uz.paykap.IntegrationTest;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.Validator;
import uz.paykap.PkbillmsApp;
import uz.paykap.IntegrationTest;
import uz.paykap.domain.ForeignPay;
import uz.paykap.domain.enumeration.ForeignPayStatus;
import uz.paykap.repository.ForeignPayRepository;
import uz.paykap.service.ForeignPayQueryService;
import uz.paykap.service.ForeignPayService;
import uz.paykap.service.dto.ForeignPayDTO;
import uz.paykap.service.mapper.ForeignPayMapper;
import uz.paykap.web.rest.errors.ExceptionTranslator;

/**
 * Test class for the ForeignPayResource REST controller.
 *
 * @see ForeignPayResource
 */
@RunWith(SpringRunner.class)
@IntegrationTest
public class ForeignPayResourceIntTest {

    private static final Instant DEFAULT_VERIFIED_DATE = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_VERIFIED_DATE = Instant.now().truncatedTo(ChronoUnit.MILLIS);

    private static final ForeignPayStatus DEFAULT_STATUS = ForeignPayStatus.NEW;
    private static final ForeignPayStatus UPDATED_STATUS = ForeignPayStatus.VERIFIED;

    private static final String DEFAULT_CARD_TOKEN = "AAAAAAAAAA";
    private static final String UPDATED_CARD_TOKEN = "BBBBBBBBBB";

    private static final Long DEFAULT_BILL_ID = 1L;
    private static final Long UPDATED_BILL_ID = 2L;

    private static final String DEFAULT_OTP = "AAAAAA";
    private static final String UPDATED_OTP = "BBBBBB";

    private static final String DEFAULT_PHONE = "AAAAAAAAAA";
    private static final String UPDATED_PHONE = "BBBBBBBBBB";

    private static final Integer DEFAULT_ENTRY_COUNT = 1;
    private static final Integer UPDATED_ENTRY_COUNT = 2;

    @Autowired
    private ForeignPayRepository foreignPayRepository;

    @Autowired
    private ForeignPayMapper foreignPayMapper;

    @Autowired
    private ForeignPayService foreignPayService;

    @Autowired
    private ForeignPayQueryService foreignPayQueryService;

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

    private MockMvc restForeignPayMockMvc;

    private ForeignPay foreignPay;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        final ForeignPayResource foreignPayResource = new ForeignPayResource(
            foreignPayService,
            foreignPayRepository,
            foreignPayQueryService
        );
        this.restForeignPayMockMvc =
            MockMvcBuilders
                .standaloneSetup(foreignPayResource)
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

    @Before
    public void initTest() {
        foreignPay = createEntity(em);
    }

    @Test
    @Transactional
    public void createForeignPay() throws Exception {
        int databaseSizeBeforeCreate = foreignPayRepository.findAll().size();

        // Create the ForeignPay
        ForeignPayDTO foreignPayDTO = foreignPayMapper.toDto(foreignPay);
        restForeignPayMockMvc
            .perform(
                post("/api/foreign-pays")
                    .contentType(TestUtil.APPLICATION_JSON_UTF8)
                    .content(TestUtil.convertObjectToJsonBytes(foreignPayDTO))
            )
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
    public void createForeignPayWithExistingId() throws Exception {
        int databaseSizeBeforeCreate = foreignPayRepository.findAll().size();

        // Create the ForeignPay with an existing ID
        foreignPay.setId(1L);
        ForeignPayDTO foreignPayDTO = foreignPayMapper.toDto(foreignPay);

        // An entity with an existing ID cannot be created, so this API call must fail
        restForeignPayMockMvc
            .perform(
                post("/api/foreign-pays")
                    .contentType(TestUtil.APPLICATION_JSON_UTF8)
                    .content(TestUtil.convertObjectToJsonBytes(foreignPayDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the ForeignPay in the database
        List<ForeignPay> foreignPayList = foreignPayRepository.findAll();
        assertThat(foreignPayList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    public void checkVerifiedDateIsRequired() throws Exception {
        int databaseSizeBeforeTest = foreignPayRepository.findAll().size();
        // set the field null
        foreignPay.setVerifiedDate(null);

        // Create the ForeignPay, which fails.
        ForeignPayDTO foreignPayDTO = foreignPayMapper.toDto(foreignPay);

        restForeignPayMockMvc
            .perform(
                post("/api/foreign-pays")
                    .contentType(TestUtil.APPLICATION_JSON_UTF8)
                    .content(TestUtil.convertObjectToJsonBytes(foreignPayDTO))
            )
            .andExpect(status().isBadRequest());

        List<ForeignPay> foreignPayList = foreignPayRepository.findAll();
        assertThat(foreignPayList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkStatusIsRequired() throws Exception {
        int databaseSizeBeforeTest = foreignPayRepository.findAll().size();
        // set the field null
        foreignPay.setStatus(null);

        // Create the ForeignPay, which fails.
        ForeignPayDTO foreignPayDTO = foreignPayMapper.toDto(foreignPay);

        restForeignPayMockMvc
            .perform(
                post("/api/foreign-pays")
                    .contentType(TestUtil.APPLICATION_JSON_UTF8)
                    .content(TestUtil.convertObjectToJsonBytes(foreignPayDTO))
            )
            .andExpect(status().isBadRequest());

        List<ForeignPay> foreignPayList = foreignPayRepository.findAll();
        assertThat(foreignPayList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkCardTokenIsRequired() throws Exception {
        int databaseSizeBeforeTest = foreignPayRepository.findAll().size();
        // set the field null
        foreignPay.setCardToken(null);

        // Create the ForeignPay, which fails.
        ForeignPayDTO foreignPayDTO = foreignPayMapper.toDto(foreignPay);

        restForeignPayMockMvc
            .perform(
                post("/api/foreign-pays")
                    .contentType(TestUtil.APPLICATION_JSON_UTF8)
                    .content(TestUtil.convertObjectToJsonBytes(foreignPayDTO))
            )
            .andExpect(status().isBadRequest());

        List<ForeignPay> foreignPayList = foreignPayRepository.findAll();
        assertThat(foreignPayList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkBillIdIsRequired() throws Exception {
        int databaseSizeBeforeTest = foreignPayRepository.findAll().size();
        // set the field null
        foreignPay.setBillId(null);

        // Create the ForeignPay, which fails.
        ForeignPayDTO foreignPayDTO = foreignPayMapper.toDto(foreignPay);

        restForeignPayMockMvc
            .perform(
                post("/api/foreign-pays")
                    .contentType(TestUtil.APPLICATION_JSON_UTF8)
                    .content(TestUtil.convertObjectToJsonBytes(foreignPayDTO))
            )
            .andExpect(status().isBadRequest());

        List<ForeignPay> foreignPayList = foreignPayRepository.findAll();
        assertThat(foreignPayList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkOtpIsRequired() throws Exception {
        int databaseSizeBeforeTest = foreignPayRepository.findAll().size();
        // set the field null
        foreignPay.setOtp(null);

        // Create the ForeignPay, which fails.
        ForeignPayDTO foreignPayDTO = foreignPayMapper.toDto(foreignPay);

        restForeignPayMockMvc
            .perform(
                post("/api/foreign-pays")
                    .contentType(TestUtil.APPLICATION_JSON_UTF8)
                    .content(TestUtil.convertObjectToJsonBytes(foreignPayDTO))
            )
            .andExpect(status().isBadRequest());

        List<ForeignPay> foreignPayList = foreignPayRepository.findAll();
        assertThat(foreignPayList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkPhoneIsRequired() throws Exception {
        int databaseSizeBeforeTest = foreignPayRepository.findAll().size();
        // set the field null
        foreignPay.setPhone(null);

        // Create the ForeignPay, which fails.
        ForeignPayDTO foreignPayDTO = foreignPayMapper.toDto(foreignPay);

        restForeignPayMockMvc
            .perform(
                post("/api/foreign-pays")
                    .contentType(TestUtil.APPLICATION_JSON_UTF8)
                    .content(TestUtil.convertObjectToJsonBytes(foreignPayDTO))
            )
            .andExpect(status().isBadRequest());

        List<ForeignPay> foreignPayList = foreignPayRepository.findAll();
        assertThat(foreignPayList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkEntryCountIsRequired() throws Exception {
        int databaseSizeBeforeTest = foreignPayRepository.findAll().size();
        // set the field null
        foreignPay.setEntryCount(null);

        // Create the ForeignPay, which fails.
        ForeignPayDTO foreignPayDTO = foreignPayMapper.toDto(foreignPay);

        restForeignPayMockMvc
            .perform(
                post("/api/foreign-pays")
                    .contentType(TestUtil.APPLICATION_JSON_UTF8)
                    .content(TestUtil.convertObjectToJsonBytes(foreignPayDTO))
            )
            .andExpect(status().isBadRequest());

        List<ForeignPay> foreignPayList = foreignPayRepository.findAll();
        assertThat(foreignPayList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void getAllForeignPays() throws Exception {
        // Initialize the database
        foreignPayRepository.saveAndFlush(foreignPay);

        // Get all the foreignPayList
        restForeignPayMockMvc
            .perform(get("/api/foreign-pays?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(foreignPay.getId().intValue())))
            .andExpect(jsonPath("$.[*].verifiedDate").value(hasItem(DEFAULT_VERIFIED_DATE.toString())))
            .andExpect(jsonPath("$.[*].status").value(hasItem(DEFAULT_STATUS.toString())))
            .andExpect(jsonPath("$.[*].cardToken").value(hasItem(DEFAULT_CARD_TOKEN.toString())))
            .andExpect(jsonPath("$.[*].billId").value(hasItem(DEFAULT_BILL_ID.intValue())))
            .andExpect(jsonPath("$.[*].otp").value(hasItem(DEFAULT_OTP.toString())))
            .andExpect(jsonPath("$.[*].phone").value(hasItem(DEFAULT_PHONE.toString())))
            .andExpect(jsonPath("$.[*].entryCount").value(hasItem(DEFAULT_ENTRY_COUNT)));
    }

    @Test
    @Transactional
    public void getForeignPay() throws Exception {
        // Initialize the database
        foreignPayRepository.saveAndFlush(foreignPay);

        // Get the foreignPay
        restForeignPayMockMvc
            .perform(get("/api/foreign-pays/{id}", foreignPay.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.id").value(foreignPay.getId().intValue()))
            .andExpect(jsonPath("$.verifiedDate").value(DEFAULT_VERIFIED_DATE.toString()))
            .andExpect(jsonPath("$.status").value(DEFAULT_STATUS.toString()))
            .andExpect(jsonPath("$.cardToken").value(DEFAULT_CARD_TOKEN.toString()))
            .andExpect(jsonPath("$.billId").value(DEFAULT_BILL_ID.intValue()))
            .andExpect(jsonPath("$.otp").value(DEFAULT_OTP.toString()))
            .andExpect(jsonPath("$.phone").value(DEFAULT_PHONE.toString()))
            .andExpect(jsonPath("$.entryCount").value(DEFAULT_ENTRY_COUNT));
    }

    @Test
    @Transactional
    public void getAllForeignPaysByVerifiedDateIsEqualToSomething() throws Exception {
        // Initialize the database
        foreignPayRepository.saveAndFlush(foreignPay);

        // Get all the foreignPayList where verifiedDate equals to DEFAULT_VERIFIED_DATE
        defaultForeignPayShouldBeFound("verifiedDate.equals=" + DEFAULT_VERIFIED_DATE);

        // Get all the foreignPayList where verifiedDate equals to UPDATED_VERIFIED_DATE
        defaultForeignPayShouldNotBeFound("verifiedDate.equals=" + UPDATED_VERIFIED_DATE);
    }

    @Test
    @Transactional
    public void getAllForeignPaysByVerifiedDateIsInShouldWork() throws Exception {
        // Initialize the database
        foreignPayRepository.saveAndFlush(foreignPay);

        // Get all the foreignPayList where verifiedDate in DEFAULT_VERIFIED_DATE or UPDATED_VERIFIED_DATE
        defaultForeignPayShouldBeFound("verifiedDate.in=" + DEFAULT_VERIFIED_DATE + "," + UPDATED_VERIFIED_DATE);

        // Get all the foreignPayList where verifiedDate equals to UPDATED_VERIFIED_DATE
        defaultForeignPayShouldNotBeFound("verifiedDate.in=" + UPDATED_VERIFIED_DATE);
    }

    @Test
    @Transactional
    public void getAllForeignPaysByVerifiedDateIsNullOrNotNull() throws Exception {
        // Initialize the database
        foreignPayRepository.saveAndFlush(foreignPay);

        // Get all the foreignPayList where verifiedDate is not null
        defaultForeignPayShouldBeFound("verifiedDate.specified=true");

        // Get all the foreignPayList where verifiedDate is null
        defaultForeignPayShouldNotBeFound("verifiedDate.specified=false");
    }

    @Test
    @Transactional
    public void getAllForeignPaysByStatusIsEqualToSomething() throws Exception {
        // Initialize the database
        foreignPayRepository.saveAndFlush(foreignPay);

        // Get all the foreignPayList where status equals to DEFAULT_STATUS
        defaultForeignPayShouldBeFound("status.equals=" + DEFAULT_STATUS);

        // Get all the foreignPayList where status equals to UPDATED_STATUS
        defaultForeignPayShouldNotBeFound("status.equals=" + UPDATED_STATUS);
    }

    @Test
    @Transactional
    public void getAllForeignPaysByStatusIsInShouldWork() throws Exception {
        // Initialize the database
        foreignPayRepository.saveAndFlush(foreignPay);

        // Get all the foreignPayList where status in DEFAULT_STATUS or UPDATED_STATUS
        defaultForeignPayShouldBeFound("status.in=" + DEFAULT_STATUS + "," + UPDATED_STATUS);

        // Get all the foreignPayList where status equals to UPDATED_STATUS
        defaultForeignPayShouldNotBeFound("status.in=" + UPDATED_STATUS);
    }

    @Test
    @Transactional
    public void getAllForeignPaysByStatusIsNullOrNotNull() throws Exception {
        // Initialize the database
        foreignPayRepository.saveAndFlush(foreignPay);

        // Get all the foreignPayList where status is not null
        defaultForeignPayShouldBeFound("status.specified=true");

        // Get all the foreignPayList where status is null
        defaultForeignPayShouldNotBeFound("status.specified=false");
    }

    @Test
    @Transactional
    public void getAllForeignPaysByCardTokenIsEqualToSomething() throws Exception {
        // Initialize the database
        foreignPayRepository.saveAndFlush(foreignPay);

        // Get all the foreignPayList where cardToken equals to DEFAULT_CARD_TOKEN
        defaultForeignPayShouldBeFound("cardToken.equals=" + DEFAULT_CARD_TOKEN);

        // Get all the foreignPayList where cardToken equals to UPDATED_CARD_TOKEN
        defaultForeignPayShouldNotBeFound("cardToken.equals=" + UPDATED_CARD_TOKEN);
    }

    @Test
    @Transactional
    public void getAllForeignPaysByCardTokenIsInShouldWork() throws Exception {
        // Initialize the database
        foreignPayRepository.saveAndFlush(foreignPay);

        // Get all the foreignPayList where cardToken in DEFAULT_CARD_TOKEN or UPDATED_CARD_TOKEN
        defaultForeignPayShouldBeFound("cardToken.in=" + DEFAULT_CARD_TOKEN + "," + UPDATED_CARD_TOKEN);

        // Get all the foreignPayList where cardToken equals to UPDATED_CARD_TOKEN
        defaultForeignPayShouldNotBeFound("cardToken.in=" + UPDATED_CARD_TOKEN);
    }

    @Test
    @Transactional
    public void getAllForeignPaysByCardTokenIsNullOrNotNull() throws Exception {
        // Initialize the database
        foreignPayRepository.saveAndFlush(foreignPay);

        // Get all the foreignPayList where cardToken is not null
        defaultForeignPayShouldBeFound("cardToken.specified=true");

        // Get all the foreignPayList where cardToken is null
        defaultForeignPayShouldNotBeFound("cardToken.specified=false");
    }

    @Test
    @Transactional
    public void getAllForeignPaysByBillIdIsEqualToSomething() throws Exception {
        // Initialize the database
        foreignPayRepository.saveAndFlush(foreignPay);

        // Get all the foreignPayList where billId equals to DEFAULT_BILL_ID
        defaultForeignPayShouldBeFound("billId.equals=" + DEFAULT_BILL_ID);

        // Get all the foreignPayList where billId equals to UPDATED_BILL_ID
        defaultForeignPayShouldNotBeFound("billId.equals=" + UPDATED_BILL_ID);
    }

    @Test
    @Transactional
    public void getAllForeignPaysByBillIdIsInShouldWork() throws Exception {
        // Initialize the database
        foreignPayRepository.saveAndFlush(foreignPay);

        // Get all the foreignPayList where billId in DEFAULT_BILL_ID or UPDATED_BILL_ID
        defaultForeignPayShouldBeFound("billId.in=" + DEFAULT_BILL_ID + "," + UPDATED_BILL_ID);

        // Get all the foreignPayList where billId equals to UPDATED_BILL_ID
        defaultForeignPayShouldNotBeFound("billId.in=" + UPDATED_BILL_ID);
    }

    @Test
    @Transactional
    public void getAllForeignPaysByBillIdIsNullOrNotNull() throws Exception {
        // Initialize the database
        foreignPayRepository.saveAndFlush(foreignPay);

        // Get all the foreignPayList where billId is not null
        defaultForeignPayShouldBeFound("billId.specified=true");

        // Get all the foreignPayList where billId is null
        defaultForeignPayShouldNotBeFound("billId.specified=false");
    }

    @Test
    @Transactional
    public void getAllForeignPaysByBillIdIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        foreignPayRepository.saveAndFlush(foreignPay);

        // Get all the foreignPayList where billId greater than or equals to DEFAULT_BILL_ID
        defaultForeignPayShouldBeFound("billId.greaterOrEqualThan=" + DEFAULT_BILL_ID);

        // Get all the foreignPayList where billId greater than or equals to UPDATED_BILL_ID
        defaultForeignPayShouldNotBeFound("billId.greaterOrEqualThan=" + UPDATED_BILL_ID);
    }

    @Test
    @Transactional
    public void getAllForeignPaysByBillIdIsLessThanSomething() throws Exception {
        // Initialize the database
        foreignPayRepository.saveAndFlush(foreignPay);

        // Get all the foreignPayList where billId less than or equals to DEFAULT_BILL_ID
        defaultForeignPayShouldNotBeFound("billId.lessThan=" + DEFAULT_BILL_ID);

        // Get all the foreignPayList where billId less than or equals to UPDATED_BILL_ID
        defaultForeignPayShouldBeFound("billId.lessThan=" + UPDATED_BILL_ID);
    }

    @Test
    @Transactional
    public void getAllForeignPaysByOtpIsEqualToSomething() throws Exception {
        // Initialize the database
        foreignPayRepository.saveAndFlush(foreignPay);

        // Get all the foreignPayList where otp equals to DEFAULT_OTP
        defaultForeignPayShouldBeFound("otp.equals=" + DEFAULT_OTP);

        // Get all the foreignPayList where otp equals to UPDATED_OTP
        defaultForeignPayShouldNotBeFound("otp.equals=" + UPDATED_OTP);
    }

    @Test
    @Transactional
    public void getAllForeignPaysByOtpIsInShouldWork() throws Exception {
        // Initialize the database
        foreignPayRepository.saveAndFlush(foreignPay);

        // Get all the foreignPayList where otp in DEFAULT_OTP or UPDATED_OTP
        defaultForeignPayShouldBeFound("otp.in=" + DEFAULT_OTP + "," + UPDATED_OTP);

        // Get all the foreignPayList where otp equals to UPDATED_OTP
        defaultForeignPayShouldNotBeFound("otp.in=" + UPDATED_OTP);
    }

    @Test
    @Transactional
    public void getAllForeignPaysByOtpIsNullOrNotNull() throws Exception {
        // Initialize the database
        foreignPayRepository.saveAndFlush(foreignPay);

        // Get all the foreignPayList where otp is not null
        defaultForeignPayShouldBeFound("otp.specified=true");

        // Get all the foreignPayList where otp is null
        defaultForeignPayShouldNotBeFound("otp.specified=false");
    }

    @Test
    @Transactional
    public void getAllForeignPaysByPhoneIsEqualToSomething() throws Exception {
        // Initialize the database
        foreignPayRepository.saveAndFlush(foreignPay);

        // Get all the foreignPayList where phone equals to DEFAULT_PHONE
        defaultForeignPayShouldBeFound("phone.equals=" + DEFAULT_PHONE);

        // Get all the foreignPayList where phone equals to UPDATED_PHONE
        defaultForeignPayShouldNotBeFound("phone.equals=" + UPDATED_PHONE);
    }

    @Test
    @Transactional
    public void getAllForeignPaysByPhoneIsInShouldWork() throws Exception {
        // Initialize the database
        foreignPayRepository.saveAndFlush(foreignPay);

        // Get all the foreignPayList where phone in DEFAULT_PHONE or UPDATED_PHONE
        defaultForeignPayShouldBeFound("phone.in=" + DEFAULT_PHONE + "," + UPDATED_PHONE);

        // Get all the foreignPayList where phone equals to UPDATED_PHONE
        defaultForeignPayShouldNotBeFound("phone.in=" + UPDATED_PHONE);
    }

    @Test
    @Transactional
    public void getAllForeignPaysByPhoneIsNullOrNotNull() throws Exception {
        // Initialize the database
        foreignPayRepository.saveAndFlush(foreignPay);

        // Get all the foreignPayList where phone is not null
        defaultForeignPayShouldBeFound("phone.specified=true");

        // Get all the foreignPayList where phone is null
        defaultForeignPayShouldNotBeFound("phone.specified=false");
    }

    @Test
    @Transactional
    public void getAllForeignPaysByEntryCountIsEqualToSomething() throws Exception {
        // Initialize the database
        foreignPayRepository.saveAndFlush(foreignPay);

        // Get all the foreignPayList where entryCount equals to DEFAULT_ENTRY_COUNT
        defaultForeignPayShouldBeFound("entryCount.equals=" + DEFAULT_ENTRY_COUNT);

        // Get all the foreignPayList where entryCount equals to UPDATED_ENTRY_COUNT
        defaultForeignPayShouldNotBeFound("entryCount.equals=" + UPDATED_ENTRY_COUNT);
    }

    @Test
    @Transactional
    public void getAllForeignPaysByEntryCountIsInShouldWork() throws Exception {
        // Initialize the database
        foreignPayRepository.saveAndFlush(foreignPay);

        // Get all the foreignPayList where entryCount in DEFAULT_ENTRY_COUNT or UPDATED_ENTRY_COUNT
        defaultForeignPayShouldBeFound("entryCount.in=" + DEFAULT_ENTRY_COUNT + "," + UPDATED_ENTRY_COUNT);

        // Get all the foreignPayList where entryCount equals to UPDATED_ENTRY_COUNT
        defaultForeignPayShouldNotBeFound("entryCount.in=" + UPDATED_ENTRY_COUNT);
    }

    @Test
    @Transactional
    public void getAllForeignPaysByEntryCountIsNullOrNotNull() throws Exception {
        // Initialize the database
        foreignPayRepository.saveAndFlush(foreignPay);

        // Get all the foreignPayList where entryCount is not null
        defaultForeignPayShouldBeFound("entryCount.specified=true");

        // Get all the foreignPayList where entryCount is null
        defaultForeignPayShouldNotBeFound("entryCount.specified=false");
    }

    @Test
    @Transactional
    public void getAllForeignPaysByEntryCountIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        foreignPayRepository.saveAndFlush(foreignPay);

        // Get all the foreignPayList where entryCount greater than or equals to DEFAULT_ENTRY_COUNT
        defaultForeignPayShouldBeFound("entryCount.greaterOrEqualThan=" + DEFAULT_ENTRY_COUNT);

        // Get all the foreignPayList where entryCount greater than or equals to UPDATED_ENTRY_COUNT
        defaultForeignPayShouldNotBeFound("entryCount.greaterOrEqualThan=" + UPDATED_ENTRY_COUNT);
    }

    @Test
    @Transactional
    public void getAllForeignPaysByEntryCountIsLessThanSomething() throws Exception {
        // Initialize the database
        foreignPayRepository.saveAndFlush(foreignPay);

        // Get all the foreignPayList where entryCount less than or equals to DEFAULT_ENTRY_COUNT
        defaultForeignPayShouldNotBeFound("entryCount.lessThan=" + DEFAULT_ENTRY_COUNT);

        // Get all the foreignPayList where entryCount less than or equals to UPDATED_ENTRY_COUNT
        defaultForeignPayShouldBeFound("entryCount.lessThan=" + UPDATED_ENTRY_COUNT);
    }

    /**
     * Executes the search, and checks that the default entity is returned
     */
    private void defaultForeignPayShouldBeFound(String filter) throws Exception {
        restForeignPayMockMvc
            .perform(get("/api/foreign-pays?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(foreignPay.getId().intValue())))
            .andExpect(jsonPath("$.[*].verifiedDate").value(hasItem(DEFAULT_VERIFIED_DATE.toString())))
            .andExpect(jsonPath("$.[*].status").value(hasItem(DEFAULT_STATUS.toString())))
            .andExpect(jsonPath("$.[*].cardToken").value(hasItem(DEFAULT_CARD_TOKEN.toString())))
            .andExpect(jsonPath("$.[*].billId").value(hasItem(DEFAULT_BILL_ID.intValue())))
            .andExpect(jsonPath("$.[*].otp").value(hasItem(DEFAULT_OTP.toString())))
            .andExpect(jsonPath("$.[*].phone").value(hasItem(DEFAULT_PHONE.toString())))
            .andExpect(jsonPath("$.[*].entryCount").value(hasItem(DEFAULT_ENTRY_COUNT)));

        // Check, that the count call also returns 1
        restForeignPayMockMvc
            .perform(get("/api/foreign-pays/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(content().string("1"));
    }

    /**
     * Executes the search, and checks that the default entity is not returned
     */
    private void defaultForeignPayShouldNotBeFound(String filter) throws Exception {
        restForeignPayMockMvc
            .perform(get("/api/foreign-pays?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty());

        // Check, that the count call also returns 0
        restForeignPayMockMvc
            .perform(get("/api/foreign-pays/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(content().string("0"));
    }

    @Test
    @Transactional
    public void getNonExistingForeignPay() throws Exception {
        // Get the foreignPay
        restForeignPayMockMvc.perform(get("/api/foreign-pays/{id}", Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updateForeignPay() throws Exception {
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
                put("/api/foreign-pays")
                    .contentType(TestUtil.APPLICATION_JSON_UTF8)
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
    public void updateNonExistingForeignPay() throws Exception {
        int databaseSizeBeforeUpdate = foreignPayRepository.findAll().size();

        // Create the ForeignPay
        ForeignPayDTO foreignPayDTO = foreignPayMapper.toDto(foreignPay);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restForeignPayMockMvc
            .perform(
                put("/api/foreign-pays")
                    .contentType(TestUtil.APPLICATION_JSON_UTF8)
                    .content(TestUtil.convertObjectToJsonBytes(foreignPayDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the ForeignPay in the database
        List<ForeignPay> foreignPayList = foreignPayRepository.findAll();
        assertThat(foreignPayList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    public void deleteForeignPay() throws Exception {
        // Initialize the database
        foreignPayRepository.saveAndFlush(foreignPay);

        int databaseSizeBeforeDelete = foreignPayRepository.findAll().size();

        // Get the foreignPay
        restForeignPayMockMvc
            .perform(delete("/api/foreign-pays/{id}", foreignPay.getId()).accept(TestUtil.APPLICATION_JSON_UTF8))
            .andExpect(status().isOk());

        // Validate the database is empty
        List<ForeignPay> foreignPayList = foreignPayRepository.findAll();
        assertThat(foreignPayList).hasSize(databaseSizeBeforeDelete - 1);
    }

    @Test
    @Transactional
    public void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(ForeignPay.class);
        ForeignPay foreignPay1 = new ForeignPay();
        foreignPay1.setId(1L);
        ForeignPay foreignPay2 = new ForeignPay();
        foreignPay2.setId(foreignPay1.getId());
        assertThat(foreignPay1).isEqualTo(foreignPay2);
        foreignPay2.setId(2L);
        assertThat(foreignPay1).isNotEqualTo(foreignPay2);
        foreignPay1.setId(null);
        assertThat(foreignPay1).isNotEqualTo(foreignPay2);
    }

    @Test
    @Transactional
    public void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(ForeignPayDTO.class);
        ForeignPayDTO foreignPayDTO1 = new ForeignPayDTO();
        foreignPayDTO1.setId(1L);
        ForeignPayDTO foreignPayDTO2 = new ForeignPayDTO();
        assertThat(foreignPayDTO1).isNotEqualTo(foreignPayDTO2);
        foreignPayDTO2.setId(foreignPayDTO1.getId());
        assertThat(foreignPayDTO1).isEqualTo(foreignPayDTO2);
        foreignPayDTO2.setId(2L);
        assertThat(foreignPayDTO1).isNotEqualTo(foreignPayDTO2);
        foreignPayDTO1.setId(null);
        assertThat(foreignPayDTO1).isNotEqualTo(foreignPayDTO2);
    }

    @Test
    @Transactional
    public void testEntityFromId() {
        assertThat(foreignPayMapper.fromId(42L).getId()).isEqualTo(42);
        assertThat(foreignPayMapper.fromId(null)).isNull();
    }
}
