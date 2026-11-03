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
import uz.paykap.domain.ReconcileDiscrepancy;
import uz.paykap.domain.ReconcileSummary;
import uz.paykap.repository.ReconcileDiscrepancyRepository;
import uz.paykap.service.ReconcileDiscrepancyQueryService;
import uz.paykap.service.ReconcileDiscrepancyService;
import uz.paykap.service.dto.ReconcileDiscrepancyDTO;
import uz.paykap.service.mapper.ReconcileDiscrepancyMapper;
import uz.paykap.web.rest.errors.ExceptionTranslator;

/**
 * Test class for the ReconcileDiscrepancyResource REST controller.
 *
 * @see ReconcileDiscrepancyResource
 */
@RunWith(SpringRunner.class)
@IntegrationTest
public class ReconcileDiscrepancyResourceIntTest {

    private static final String DEFAULT_GATEWAY_NAME = "AAAAAAAAAA";
    private static final String UPDATED_GATEWAY_NAME = "BBBBBBBBBB";

    private static final Long DEFAULT_SYSTEM_TRANSACTION_ID = 1L;
    private static final Long UPDATED_SYSTEM_TRANSACTION_ID = 2L;

    private static final String DEFAULT_GATEWAY_TRANSACTION_ID = "AAAAAAAAAA";
    private static final String UPDATED_GATEWAY_TRANSACTION_ID = "BBBBBBBBBB";

    private static final Long DEFAULT_SYSTEM_AMOUNT = 1L;
    private static final Long UPDATED_SYSTEM_AMOUNT = 2L;

    private static final Long DEFAULT_GATEWAY_AMOUNT = 1L;
    private static final Long UPDATED_GATEWAY_AMOUNT = 2L;

    private static final Boolean DEFAULT_SYSTEM_STATUS = false;
    private static final Boolean UPDATED_SYSTEM_STATUS = true;

    private static final Boolean DEFAULT_GATEWAY_STATUS = false;
    private static final Boolean UPDATED_GATEWAY_STATUS = true;

    private static final Instant DEFAULT_SYSTEM_DATE = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_SYSTEM_DATE = Instant.now().truncatedTo(ChronoUnit.MILLIS);

    private static final Instant DEFAULT_GATEWAY_DATE = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_GATEWAY_DATE = Instant.now().truncatedTo(ChronoUnit.MILLIS);

    private static final Boolean DEFAULT_CLOSED = false;
    private static final Boolean UPDATED_CLOSED = true;

    private static final String DEFAULT_COMMENT = "AAAAAAAAAA";
    private static final String UPDATED_COMMENT = "BBBBBBBBBB";

    @Autowired
    private ReconcileDiscrepancyRepository reconcileDiscrepancyRepository;

    @Autowired
    private ReconcileDiscrepancyMapper reconcileDiscrepancyMapper;

    @Autowired
    private ReconcileDiscrepancyService reconcileDiscrepancyService;

    @Autowired
    private ReconcileDiscrepancyQueryService reconcileDiscrepancyQueryService;

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

    private MockMvc restReconcileDiscrepancyMockMvc;

    private ReconcileDiscrepancy reconcileDiscrepancy;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        final ReconcileDiscrepancyResource reconcileDiscrepancyResource = new ReconcileDiscrepancyResource(
            reconcileDiscrepancyService,
            reconcileDiscrepancyRepository,
            reconcileDiscrepancyQueryService
        );
        this.restReconcileDiscrepancyMockMvc =
            MockMvcBuilders
                .standaloneSetup(reconcileDiscrepancyResource)
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
    public static ReconcileDiscrepancy createEntity(EntityManager em) {
        ReconcileDiscrepancy reconcileDiscrepancy = new ReconcileDiscrepancy()
            .gatewayName(DEFAULT_GATEWAY_NAME)
            .systemTransactionId(DEFAULT_SYSTEM_TRANSACTION_ID)
            .gatewayTransactionId(DEFAULT_GATEWAY_TRANSACTION_ID)
            .systemAmount(DEFAULT_SYSTEM_AMOUNT)
            .gatewayAmount(DEFAULT_GATEWAY_AMOUNT)
            .systemStatus(DEFAULT_SYSTEM_STATUS)
            .gatewayStatus(DEFAULT_GATEWAY_STATUS)
            .systemDate(DEFAULT_SYSTEM_DATE)
            .gatewayDate(DEFAULT_GATEWAY_DATE)
            .closed(DEFAULT_CLOSED)
            .comment(DEFAULT_COMMENT);
        return reconcileDiscrepancy;
    }

    @Before
    public void initTest() {
        reconcileDiscrepancy = createEntity(em);
    }

    @Test
    @Transactional
    public void createReconcileDiscrepancy() throws Exception {
        int databaseSizeBeforeCreate = reconcileDiscrepancyRepository.findAll().size();

        // Create the ReconcileDiscrepancy
        ReconcileDiscrepancyDTO reconcileDiscrepancyDTO = reconcileDiscrepancyMapper.toDto(reconcileDiscrepancy);
        restReconcileDiscrepancyMockMvc
            .perform(
                post("/api/reconcile-discrepancies")
                    .contentType(TestUtil.APPLICATION_JSON_UTF8)
                    .content(TestUtil.convertObjectToJsonBytes(reconcileDiscrepancyDTO))
            )
            .andExpect(status().isCreated());

        // Validate the ReconcileDiscrepancy in the database
        List<ReconcileDiscrepancy> reconcileDiscrepancyList = reconcileDiscrepancyRepository.findAll();
        assertThat(reconcileDiscrepancyList).hasSize(databaseSizeBeforeCreate + 1);
        ReconcileDiscrepancy testReconcileDiscrepancy = reconcileDiscrepancyList.get(reconcileDiscrepancyList.size() - 1);
        assertThat(testReconcileDiscrepancy.getGatewayName()).isEqualTo(DEFAULT_GATEWAY_NAME);
        assertThat(testReconcileDiscrepancy.getSystemTransactionId()).isEqualTo(DEFAULT_SYSTEM_TRANSACTION_ID);
        assertThat(testReconcileDiscrepancy.getGatewayTransactionId()).isEqualTo(DEFAULT_GATEWAY_TRANSACTION_ID);
        assertThat(testReconcileDiscrepancy.getSystemAmount()).isEqualTo(DEFAULT_SYSTEM_AMOUNT);
        assertThat(testReconcileDiscrepancy.getGatewayAmount()).isEqualTo(DEFAULT_GATEWAY_AMOUNT);
        assertThat(testReconcileDiscrepancy.isSystemStatus()).isEqualTo(DEFAULT_SYSTEM_STATUS);
        assertThat(testReconcileDiscrepancy.isGatewayStatus()).isEqualTo(DEFAULT_GATEWAY_STATUS);
        assertThat(testReconcileDiscrepancy.getSystemDate()).isEqualTo(DEFAULT_SYSTEM_DATE);
        assertThat(testReconcileDiscrepancy.getGatewayDate()).isEqualTo(DEFAULT_GATEWAY_DATE);
        assertThat(testReconcileDiscrepancy.isClosed()).isEqualTo(DEFAULT_CLOSED);
        assertThat(testReconcileDiscrepancy.getComment()).isEqualTo(DEFAULT_COMMENT);
    }

    @Test
    @Transactional
    public void createReconcileDiscrepancyWithExistingId() throws Exception {
        int databaseSizeBeforeCreate = reconcileDiscrepancyRepository.findAll().size();

        // Create the ReconcileDiscrepancy with an existing ID
        reconcileDiscrepancy.setId(1L);
        ReconcileDiscrepancyDTO reconcileDiscrepancyDTO = reconcileDiscrepancyMapper.toDto(reconcileDiscrepancy);

        // An entity with an existing ID cannot be created, so this API call must fail
        restReconcileDiscrepancyMockMvc
            .perform(
                post("/api/reconcile-discrepancies")
                    .contentType(TestUtil.APPLICATION_JSON_UTF8)
                    .content(TestUtil.convertObjectToJsonBytes(reconcileDiscrepancyDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the ReconcileDiscrepancy in the database
        List<ReconcileDiscrepancy> reconcileDiscrepancyList = reconcileDiscrepancyRepository.findAll();
        assertThat(reconcileDiscrepancyList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    public void checkGatewayNameIsRequired() throws Exception {
        int databaseSizeBeforeTest = reconcileDiscrepancyRepository.findAll().size();
        // set the field null
        reconcileDiscrepancy.setGatewayName(null);

        // Create the ReconcileDiscrepancy, which fails.
        ReconcileDiscrepancyDTO reconcileDiscrepancyDTO = reconcileDiscrepancyMapper.toDto(reconcileDiscrepancy);

        restReconcileDiscrepancyMockMvc
            .perform(
                post("/api/reconcile-discrepancies")
                    .contentType(TestUtil.APPLICATION_JSON_UTF8)
                    .content(TestUtil.convertObjectToJsonBytes(reconcileDiscrepancyDTO))
            )
            .andExpect(status().isBadRequest());

        List<ReconcileDiscrepancy> reconcileDiscrepancyList = reconcileDiscrepancyRepository.findAll();
        assertThat(reconcileDiscrepancyList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkSystemTransactionIdIsRequired() throws Exception {
        int databaseSizeBeforeTest = reconcileDiscrepancyRepository.findAll().size();
        // set the field null
        reconcileDiscrepancy.setSystemTransactionId(null);

        // Create the ReconcileDiscrepancy, which fails.
        ReconcileDiscrepancyDTO reconcileDiscrepancyDTO = reconcileDiscrepancyMapper.toDto(reconcileDiscrepancy);

        restReconcileDiscrepancyMockMvc
            .perform(
                post("/api/reconcile-discrepancies")
                    .contentType(TestUtil.APPLICATION_JSON_UTF8)
                    .content(TestUtil.convertObjectToJsonBytes(reconcileDiscrepancyDTO))
            )
            .andExpect(status().isBadRequest());

        List<ReconcileDiscrepancy> reconcileDiscrepancyList = reconcileDiscrepancyRepository.findAll();
        assertThat(reconcileDiscrepancyList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkGatewayTransactionIdIsRequired() throws Exception {
        int databaseSizeBeforeTest = reconcileDiscrepancyRepository.findAll().size();
        // set the field null
        reconcileDiscrepancy.setGatewayTransactionId(null);

        // Create the ReconcileDiscrepancy, which fails.
        ReconcileDiscrepancyDTO reconcileDiscrepancyDTO = reconcileDiscrepancyMapper.toDto(reconcileDiscrepancy);

        restReconcileDiscrepancyMockMvc
            .perform(
                post("/api/reconcile-discrepancies")
                    .contentType(TestUtil.APPLICATION_JSON_UTF8)
                    .content(TestUtil.convertObjectToJsonBytes(reconcileDiscrepancyDTO))
            )
            .andExpect(status().isBadRequest());

        List<ReconcileDiscrepancy> reconcileDiscrepancyList = reconcileDiscrepancyRepository.findAll();
        assertThat(reconcileDiscrepancyList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void getAllReconcileDiscrepancies() throws Exception {
        // Initialize the database
        reconcileDiscrepancyRepository.saveAndFlush(reconcileDiscrepancy);

        // Get all the reconcileDiscrepancyList
        restReconcileDiscrepancyMockMvc
            .perform(get("/api/reconcile-discrepancies?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(reconcileDiscrepancy.getId().intValue())))
            .andExpect(jsonPath("$.[*].gatewayName").value(hasItem(DEFAULT_GATEWAY_NAME)))
            .andExpect(jsonPath("$.[*].systemTransactionId").value(hasItem(DEFAULT_SYSTEM_TRANSACTION_ID.intValue())))
            .andExpect(jsonPath("$.[*].gatewayTransactionId").value(hasItem(DEFAULT_GATEWAY_TRANSACTION_ID)))
            .andExpect(jsonPath("$.[*].systemAmount").value(hasItem(DEFAULT_SYSTEM_AMOUNT.intValue())))
            .andExpect(jsonPath("$.[*].gatewayAmount").value(hasItem(DEFAULT_GATEWAY_AMOUNT.intValue())))
            .andExpect(jsonPath("$.[*].systemStatus").value(hasItem(DEFAULT_SYSTEM_STATUS.booleanValue())))
            .andExpect(jsonPath("$.[*].gatewayStatus").value(hasItem(DEFAULT_GATEWAY_STATUS.booleanValue())))
            .andExpect(jsonPath("$.[*].systemDate").value(hasItem(DEFAULT_SYSTEM_DATE.toString())))
            .andExpect(jsonPath("$.[*].gatewayDate").value(hasItem(DEFAULT_GATEWAY_DATE.toString())))
            .andExpect(jsonPath("$.[*].closed").value(hasItem(DEFAULT_CLOSED.booleanValue())))
            .andExpect(jsonPath("$.[*].comment").value(hasItem(DEFAULT_COMMENT)));
    }

    @Test
    @Transactional
    public void getReconcileDiscrepancy() throws Exception {
        // Initialize the database
        reconcileDiscrepancyRepository.saveAndFlush(reconcileDiscrepancy);

        // Get the reconcileDiscrepancy
        restReconcileDiscrepancyMockMvc
            .perform(get("/api/reconcile-discrepancies/{id}", reconcileDiscrepancy.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.id").value(reconcileDiscrepancy.getId().intValue()))
            .andExpect(jsonPath("$.gatewayName").value(DEFAULT_GATEWAY_NAME))
            .andExpect(jsonPath("$.systemTransactionId").value(DEFAULT_SYSTEM_TRANSACTION_ID.intValue()))
            .andExpect(jsonPath("$.gatewayTransactionId").value(DEFAULT_GATEWAY_TRANSACTION_ID))
            .andExpect(jsonPath("$.systemAmount").value(DEFAULT_SYSTEM_AMOUNT.intValue()))
            .andExpect(jsonPath("$.gatewayAmount").value(DEFAULT_GATEWAY_AMOUNT.intValue()))
            .andExpect(jsonPath("$.systemStatus").value(DEFAULT_SYSTEM_STATUS.booleanValue()))
            .andExpect(jsonPath("$.gatewayStatus").value(DEFAULT_GATEWAY_STATUS.booleanValue()))
            .andExpect(jsonPath("$.systemDate").value(DEFAULT_SYSTEM_DATE.toString()))
            .andExpect(jsonPath("$.gatewayDate").value(DEFAULT_GATEWAY_DATE.toString()))
            .andExpect(jsonPath("$.closed").value(DEFAULT_CLOSED.booleanValue()))
            .andExpect(jsonPath("$.comment").value(DEFAULT_COMMENT));
    }

    @Test
    @Transactional
    public void getAllReconcileDiscrepanciesByGatewayNameIsEqualToSomething() throws Exception {
        // Initialize the database
        reconcileDiscrepancyRepository.saveAndFlush(reconcileDiscrepancy);

        // Get all the reconcileDiscrepancyList where gatewayName equals to DEFAULT_GATEWAY_NAME
        defaultReconcileDiscrepancyShouldBeFound("gatewayName.equals=" + DEFAULT_GATEWAY_NAME);

        // Get all the reconcileDiscrepancyList where gatewayName equals to UPDATED_GATEWAY_NAME
        defaultReconcileDiscrepancyShouldNotBeFound("gatewayName.equals=" + UPDATED_GATEWAY_NAME);
    }

    @Test
    @Transactional
    public void getAllReconcileDiscrepanciesByGatewayNameIsInShouldWork() throws Exception {
        // Initialize the database
        reconcileDiscrepancyRepository.saveAndFlush(reconcileDiscrepancy);

        // Get all the reconcileDiscrepancyList where gatewayName in DEFAULT_GATEWAY_NAME or UPDATED_GATEWAY_NAME
        defaultReconcileDiscrepancyShouldBeFound("gatewayName.in=" + DEFAULT_GATEWAY_NAME + "," + UPDATED_GATEWAY_NAME);

        // Get all the reconcileDiscrepancyList where gatewayName equals to UPDATED_GATEWAY_NAME
        defaultReconcileDiscrepancyShouldNotBeFound("gatewayName.in=" + UPDATED_GATEWAY_NAME);
    }

    @Test
    @Transactional
    public void getAllReconcileDiscrepanciesByGatewayNameIsNullOrNotNull() throws Exception {
        // Initialize the database
        reconcileDiscrepancyRepository.saveAndFlush(reconcileDiscrepancy);

        // Get all the reconcileDiscrepancyList where gatewayName is not null
        defaultReconcileDiscrepancyShouldBeFound("gatewayName.specified=true");

        // Get all the reconcileDiscrepancyList where gatewayName is null
        defaultReconcileDiscrepancyShouldNotBeFound("gatewayName.specified=false");
    }

    @Test
    @Transactional
    public void getAllReconcileDiscrepanciesBySystemTransactionIdIsEqualToSomething() throws Exception {
        // Initialize the database
        reconcileDiscrepancyRepository.saveAndFlush(reconcileDiscrepancy);

        // Get all the reconcileDiscrepancyList where systemTransactionId equals to DEFAULT_SYSTEM_TRANSACTION_ID
        defaultReconcileDiscrepancyShouldBeFound("systemTransactionId.equals=" + DEFAULT_SYSTEM_TRANSACTION_ID);

        // Get all the reconcileDiscrepancyList where systemTransactionId equals to UPDATED_SYSTEM_TRANSACTION_ID
        defaultReconcileDiscrepancyShouldNotBeFound("systemTransactionId.equals=" + UPDATED_SYSTEM_TRANSACTION_ID);
    }

    @Test
    @Transactional
    public void getAllReconcileDiscrepanciesBySystemTransactionIdIsInShouldWork() throws Exception {
        // Initialize the database
        reconcileDiscrepancyRepository.saveAndFlush(reconcileDiscrepancy);

        // Get all the reconcileDiscrepancyList where systemTransactionId in DEFAULT_SYSTEM_TRANSACTION_ID or UPDATED_SYSTEM_TRANSACTION_ID
        defaultReconcileDiscrepancyShouldBeFound(
            "systemTransactionId.in=" + DEFAULT_SYSTEM_TRANSACTION_ID + "," + UPDATED_SYSTEM_TRANSACTION_ID
        );

        // Get all the reconcileDiscrepancyList where systemTransactionId equals to UPDATED_SYSTEM_TRANSACTION_ID
        defaultReconcileDiscrepancyShouldNotBeFound("systemTransactionId.in=" + UPDATED_SYSTEM_TRANSACTION_ID);
    }

    @Test
    @Transactional
    public void getAllReconcileDiscrepanciesBySystemTransactionIdIsNullOrNotNull() throws Exception {
        // Initialize the database
        reconcileDiscrepancyRepository.saveAndFlush(reconcileDiscrepancy);

        // Get all the reconcileDiscrepancyList where systemTransactionId is not null
        defaultReconcileDiscrepancyShouldBeFound("systemTransactionId.specified=true");

        // Get all the reconcileDiscrepancyList where systemTransactionId is null
        defaultReconcileDiscrepancyShouldNotBeFound("systemTransactionId.specified=false");
    }

    @Test
    @Transactional
    public void getAllReconcileDiscrepanciesBySystemTransactionIdIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        reconcileDiscrepancyRepository.saveAndFlush(reconcileDiscrepancy);

        // Get all the reconcileDiscrepancyList where systemTransactionId greater than or equals to DEFAULT_SYSTEM_TRANSACTION_ID
        defaultReconcileDiscrepancyShouldBeFound("systemTransactionId.greaterOrEqualThan=" + DEFAULT_SYSTEM_TRANSACTION_ID);

        // Get all the reconcileDiscrepancyList where systemTransactionId greater than or equals to UPDATED_SYSTEM_TRANSACTION_ID
        defaultReconcileDiscrepancyShouldNotBeFound("systemTransactionId.greaterOrEqualThan=" + UPDATED_SYSTEM_TRANSACTION_ID);
    }

    @Test
    @Transactional
    public void getAllReconcileDiscrepanciesBySystemTransactionIdIsLessThanSomething() throws Exception {
        // Initialize the database
        reconcileDiscrepancyRepository.saveAndFlush(reconcileDiscrepancy);

        // Get all the reconcileDiscrepancyList where systemTransactionId less than or equals to DEFAULT_SYSTEM_TRANSACTION_ID
        defaultReconcileDiscrepancyShouldNotBeFound("systemTransactionId.lessThan=" + DEFAULT_SYSTEM_TRANSACTION_ID);

        // Get all the reconcileDiscrepancyList where systemTransactionId less than or equals to UPDATED_SYSTEM_TRANSACTION_ID
        defaultReconcileDiscrepancyShouldBeFound("systemTransactionId.lessThan=" + UPDATED_SYSTEM_TRANSACTION_ID);
    }

    @Test
    @Transactional
    public void getAllReconcileDiscrepanciesByGatewayTransactionIdIsEqualToSomething() throws Exception {
        // Initialize the database
        reconcileDiscrepancyRepository.saveAndFlush(reconcileDiscrepancy);

        // Get all the reconcileDiscrepancyList where gatewayTransactionId equals to DEFAULT_GATEWAY_TRANSACTION_ID
        defaultReconcileDiscrepancyShouldBeFound("gatewayTransactionId.equals=" + DEFAULT_GATEWAY_TRANSACTION_ID);

        // Get all the reconcileDiscrepancyList where gatewayTransactionId equals to UPDATED_GATEWAY_TRANSACTION_ID
        defaultReconcileDiscrepancyShouldNotBeFound("gatewayTransactionId.equals=" + UPDATED_GATEWAY_TRANSACTION_ID);
    }

    @Test
    @Transactional
    public void getAllReconcileDiscrepanciesByGatewayTransactionIdIsInShouldWork() throws Exception {
        // Initialize the database
        reconcileDiscrepancyRepository.saveAndFlush(reconcileDiscrepancy);

        // Get all the reconcileDiscrepancyList where gatewayTransactionId in DEFAULT_GATEWAY_TRANSACTION_ID or UPDATED_GATEWAY_TRANSACTION_ID
        defaultReconcileDiscrepancyShouldBeFound(
            "gatewayTransactionId.in=" + DEFAULT_GATEWAY_TRANSACTION_ID + "," + UPDATED_GATEWAY_TRANSACTION_ID
        );

        // Get all the reconcileDiscrepancyList where gatewayTransactionId equals to UPDATED_GATEWAY_TRANSACTION_ID
        defaultReconcileDiscrepancyShouldNotBeFound("gatewayTransactionId.in=" + UPDATED_GATEWAY_TRANSACTION_ID);
    }

    @Test
    @Transactional
    public void getAllReconcileDiscrepanciesByGatewayTransactionIdIsNullOrNotNull() throws Exception {
        // Initialize the database
        reconcileDiscrepancyRepository.saveAndFlush(reconcileDiscrepancy);

        // Get all the reconcileDiscrepancyList where gatewayTransactionId is not null
        defaultReconcileDiscrepancyShouldBeFound("gatewayTransactionId.specified=true");

        // Get all the reconcileDiscrepancyList where gatewayTransactionId is null
        defaultReconcileDiscrepancyShouldNotBeFound("gatewayTransactionId.specified=false");
    }

    @Test
    @Transactional
    public void getAllReconcileDiscrepanciesBySystemAmountIsEqualToSomething() throws Exception {
        // Initialize the database
        reconcileDiscrepancyRepository.saveAndFlush(reconcileDiscrepancy);

        // Get all the reconcileDiscrepancyList where systemAmount equals to DEFAULT_SYSTEM_AMOUNT
        defaultReconcileDiscrepancyShouldBeFound("systemAmount.equals=" + DEFAULT_SYSTEM_AMOUNT);

        // Get all the reconcileDiscrepancyList where systemAmount equals to UPDATED_SYSTEM_AMOUNT
        defaultReconcileDiscrepancyShouldNotBeFound("systemAmount.equals=" + UPDATED_SYSTEM_AMOUNT);
    }

    @Test
    @Transactional
    public void getAllReconcileDiscrepanciesBySystemAmountIsInShouldWork() throws Exception {
        // Initialize the database
        reconcileDiscrepancyRepository.saveAndFlush(reconcileDiscrepancy);

        // Get all the reconcileDiscrepancyList where systemAmount in DEFAULT_SYSTEM_AMOUNT or UPDATED_SYSTEM_AMOUNT
        defaultReconcileDiscrepancyShouldBeFound("systemAmount.in=" + DEFAULT_SYSTEM_AMOUNT + "," + UPDATED_SYSTEM_AMOUNT);

        // Get all the reconcileDiscrepancyList where systemAmount equals to UPDATED_SYSTEM_AMOUNT
        defaultReconcileDiscrepancyShouldNotBeFound("systemAmount.in=" + UPDATED_SYSTEM_AMOUNT);
    }

    @Test
    @Transactional
    public void getAllReconcileDiscrepanciesBySystemAmountIsNullOrNotNull() throws Exception {
        // Initialize the database
        reconcileDiscrepancyRepository.saveAndFlush(reconcileDiscrepancy);

        // Get all the reconcileDiscrepancyList where systemAmount is not null
        defaultReconcileDiscrepancyShouldBeFound("systemAmount.specified=true");

        // Get all the reconcileDiscrepancyList where systemAmount is null
        defaultReconcileDiscrepancyShouldNotBeFound("systemAmount.specified=false");
    }

    @Test
    @Transactional
    public void getAllReconcileDiscrepanciesBySystemAmountIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        reconcileDiscrepancyRepository.saveAndFlush(reconcileDiscrepancy);

        // Get all the reconcileDiscrepancyList where systemAmount greater than or equals to DEFAULT_SYSTEM_AMOUNT
        defaultReconcileDiscrepancyShouldBeFound("systemAmount.greaterOrEqualThan=" + DEFAULT_SYSTEM_AMOUNT);

        // Get all the reconcileDiscrepancyList where systemAmount greater than or equals to UPDATED_SYSTEM_AMOUNT
        defaultReconcileDiscrepancyShouldNotBeFound("systemAmount.greaterOrEqualThan=" + UPDATED_SYSTEM_AMOUNT);
    }

    @Test
    @Transactional
    public void getAllReconcileDiscrepanciesBySystemAmountIsLessThanSomething() throws Exception {
        // Initialize the database
        reconcileDiscrepancyRepository.saveAndFlush(reconcileDiscrepancy);

        // Get all the reconcileDiscrepancyList where systemAmount less than or equals to DEFAULT_SYSTEM_AMOUNT
        defaultReconcileDiscrepancyShouldNotBeFound("systemAmount.lessThan=" + DEFAULT_SYSTEM_AMOUNT);

        // Get all the reconcileDiscrepancyList where systemAmount less than or equals to UPDATED_SYSTEM_AMOUNT
        defaultReconcileDiscrepancyShouldBeFound("systemAmount.lessThan=" + UPDATED_SYSTEM_AMOUNT);
    }

    @Test
    @Transactional
    public void getAllReconcileDiscrepanciesByGatewayAmountIsEqualToSomething() throws Exception {
        // Initialize the database
        reconcileDiscrepancyRepository.saveAndFlush(reconcileDiscrepancy);

        // Get all the reconcileDiscrepancyList where gatewayAmount equals to DEFAULT_GATEWAY_AMOUNT
        defaultReconcileDiscrepancyShouldBeFound("gatewayAmount.equals=" + DEFAULT_GATEWAY_AMOUNT);

        // Get all the reconcileDiscrepancyList where gatewayAmount equals to UPDATED_GATEWAY_AMOUNT
        defaultReconcileDiscrepancyShouldNotBeFound("gatewayAmount.equals=" + UPDATED_GATEWAY_AMOUNT);
    }

    @Test
    @Transactional
    public void getAllReconcileDiscrepanciesByGatewayAmountIsInShouldWork() throws Exception {
        // Initialize the database
        reconcileDiscrepancyRepository.saveAndFlush(reconcileDiscrepancy);

        // Get all the reconcileDiscrepancyList where gatewayAmount in DEFAULT_GATEWAY_AMOUNT or UPDATED_GATEWAY_AMOUNT
        defaultReconcileDiscrepancyShouldBeFound("gatewayAmount.in=" + DEFAULT_GATEWAY_AMOUNT + "," + UPDATED_GATEWAY_AMOUNT);

        // Get all the reconcileDiscrepancyList where gatewayAmount equals to UPDATED_GATEWAY_AMOUNT
        defaultReconcileDiscrepancyShouldNotBeFound("gatewayAmount.in=" + UPDATED_GATEWAY_AMOUNT);
    }

    @Test
    @Transactional
    public void getAllReconcileDiscrepanciesByGatewayAmountIsNullOrNotNull() throws Exception {
        // Initialize the database
        reconcileDiscrepancyRepository.saveAndFlush(reconcileDiscrepancy);

        // Get all the reconcileDiscrepancyList where gatewayAmount is not null
        defaultReconcileDiscrepancyShouldBeFound("gatewayAmount.specified=true");

        // Get all the reconcileDiscrepancyList where gatewayAmount is null
        defaultReconcileDiscrepancyShouldNotBeFound("gatewayAmount.specified=false");
    }

    @Test
    @Transactional
    public void getAllReconcileDiscrepanciesByGatewayAmountIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        reconcileDiscrepancyRepository.saveAndFlush(reconcileDiscrepancy);

        // Get all the reconcileDiscrepancyList where gatewayAmount greater than or equals to DEFAULT_GATEWAY_AMOUNT
        defaultReconcileDiscrepancyShouldBeFound("gatewayAmount.greaterOrEqualThan=" + DEFAULT_GATEWAY_AMOUNT);

        // Get all the reconcileDiscrepancyList where gatewayAmount greater than or equals to UPDATED_GATEWAY_AMOUNT
        defaultReconcileDiscrepancyShouldNotBeFound("gatewayAmount.greaterOrEqualThan=" + UPDATED_GATEWAY_AMOUNT);
    }

    @Test
    @Transactional
    public void getAllReconcileDiscrepanciesByGatewayAmountIsLessThanSomething() throws Exception {
        // Initialize the database
        reconcileDiscrepancyRepository.saveAndFlush(reconcileDiscrepancy);

        // Get all the reconcileDiscrepancyList where gatewayAmount less than or equals to DEFAULT_GATEWAY_AMOUNT
        defaultReconcileDiscrepancyShouldNotBeFound("gatewayAmount.lessThan=" + DEFAULT_GATEWAY_AMOUNT);

        // Get all the reconcileDiscrepancyList where gatewayAmount less than or equals to UPDATED_GATEWAY_AMOUNT
        defaultReconcileDiscrepancyShouldBeFound("gatewayAmount.lessThan=" + UPDATED_GATEWAY_AMOUNT);
    }

    @Test
    @Transactional
    public void getAllReconcileDiscrepanciesBySystemStatusIsEqualToSomething() throws Exception {
        // Initialize the database
        reconcileDiscrepancyRepository.saveAndFlush(reconcileDiscrepancy);

        // Get all the reconcileDiscrepancyList where systemStatus equals to DEFAULT_SYSTEM_STATUS
        defaultReconcileDiscrepancyShouldBeFound("systemStatus.equals=" + DEFAULT_SYSTEM_STATUS);

        // Get all the reconcileDiscrepancyList where systemStatus equals to UPDATED_SYSTEM_STATUS
        defaultReconcileDiscrepancyShouldNotBeFound("systemStatus.equals=" + UPDATED_SYSTEM_STATUS);
    }

    @Test
    @Transactional
    public void getAllReconcileDiscrepanciesBySystemStatusIsInShouldWork() throws Exception {
        // Initialize the database
        reconcileDiscrepancyRepository.saveAndFlush(reconcileDiscrepancy);

        // Get all the reconcileDiscrepancyList where systemStatus in DEFAULT_SYSTEM_STATUS or UPDATED_SYSTEM_STATUS
        defaultReconcileDiscrepancyShouldBeFound("systemStatus.in=" + DEFAULT_SYSTEM_STATUS + "," + UPDATED_SYSTEM_STATUS);

        // Get all the reconcileDiscrepancyList where systemStatus equals to UPDATED_SYSTEM_STATUS
        defaultReconcileDiscrepancyShouldNotBeFound("systemStatus.in=" + UPDATED_SYSTEM_STATUS);
    }

    @Test
    @Transactional
    public void getAllReconcileDiscrepanciesBySystemStatusIsNullOrNotNull() throws Exception {
        // Initialize the database
        reconcileDiscrepancyRepository.saveAndFlush(reconcileDiscrepancy);

        // Get all the reconcileDiscrepancyList where systemStatus is not null
        defaultReconcileDiscrepancyShouldBeFound("systemStatus.specified=true");

        // Get all the reconcileDiscrepancyList where systemStatus is null
        defaultReconcileDiscrepancyShouldNotBeFound("systemStatus.specified=false");
    }

    @Test
    @Transactional
    public void getAllReconcileDiscrepanciesByGatewayStatusIsEqualToSomething() throws Exception {
        // Initialize the database
        reconcileDiscrepancyRepository.saveAndFlush(reconcileDiscrepancy);

        // Get all the reconcileDiscrepancyList where gatewayStatus equals to DEFAULT_GATEWAY_STATUS
        defaultReconcileDiscrepancyShouldBeFound("gatewayStatus.equals=" + DEFAULT_GATEWAY_STATUS);

        // Get all the reconcileDiscrepancyList where gatewayStatus equals to UPDATED_GATEWAY_STATUS
        defaultReconcileDiscrepancyShouldNotBeFound("gatewayStatus.equals=" + UPDATED_GATEWAY_STATUS);
    }

    @Test
    @Transactional
    public void getAllReconcileDiscrepanciesByGatewayStatusIsInShouldWork() throws Exception {
        // Initialize the database
        reconcileDiscrepancyRepository.saveAndFlush(reconcileDiscrepancy);

        // Get all the reconcileDiscrepancyList where gatewayStatus in DEFAULT_GATEWAY_STATUS or UPDATED_GATEWAY_STATUS
        defaultReconcileDiscrepancyShouldBeFound("gatewayStatus.in=" + DEFAULT_GATEWAY_STATUS + "," + UPDATED_GATEWAY_STATUS);

        // Get all the reconcileDiscrepancyList where gatewayStatus equals to UPDATED_GATEWAY_STATUS
        defaultReconcileDiscrepancyShouldNotBeFound("gatewayStatus.in=" + UPDATED_GATEWAY_STATUS);
    }

    @Test
    @Transactional
    public void getAllReconcileDiscrepanciesByGatewayStatusIsNullOrNotNull() throws Exception {
        // Initialize the database
        reconcileDiscrepancyRepository.saveAndFlush(reconcileDiscrepancy);

        // Get all the reconcileDiscrepancyList where gatewayStatus is not null
        defaultReconcileDiscrepancyShouldBeFound("gatewayStatus.specified=true");

        // Get all the reconcileDiscrepancyList where gatewayStatus is null
        defaultReconcileDiscrepancyShouldNotBeFound("gatewayStatus.specified=false");
    }

    @Test
    @Transactional
    public void getAllReconcileDiscrepanciesBySystemDateIsEqualToSomething() throws Exception {
        // Initialize the database
        reconcileDiscrepancyRepository.saveAndFlush(reconcileDiscrepancy);

        // Get all the reconcileDiscrepancyList where systemDate equals to DEFAULT_SYSTEM_DATE
        defaultReconcileDiscrepancyShouldBeFound("systemDate.equals=" + DEFAULT_SYSTEM_DATE);

        // Get all the reconcileDiscrepancyList where systemDate equals to UPDATED_SYSTEM_DATE
        defaultReconcileDiscrepancyShouldNotBeFound("systemDate.equals=" + UPDATED_SYSTEM_DATE);
    }

    @Test
    @Transactional
    public void getAllReconcileDiscrepanciesBySystemDateIsInShouldWork() throws Exception {
        // Initialize the database
        reconcileDiscrepancyRepository.saveAndFlush(reconcileDiscrepancy);

        // Get all the reconcileDiscrepancyList where systemDate in DEFAULT_SYSTEM_DATE or UPDATED_SYSTEM_DATE
        defaultReconcileDiscrepancyShouldBeFound("systemDate.in=" + DEFAULT_SYSTEM_DATE + "," + UPDATED_SYSTEM_DATE);

        // Get all the reconcileDiscrepancyList where systemDate equals to UPDATED_SYSTEM_DATE
        defaultReconcileDiscrepancyShouldNotBeFound("systemDate.in=" + UPDATED_SYSTEM_DATE);
    }

    @Test
    @Transactional
    public void getAllReconcileDiscrepanciesBySystemDateIsNullOrNotNull() throws Exception {
        // Initialize the database
        reconcileDiscrepancyRepository.saveAndFlush(reconcileDiscrepancy);

        // Get all the reconcileDiscrepancyList where systemDate is not null
        defaultReconcileDiscrepancyShouldBeFound("systemDate.specified=true");

        // Get all the reconcileDiscrepancyList where systemDate is null
        defaultReconcileDiscrepancyShouldNotBeFound("systemDate.specified=false");
    }

    @Test
    @Transactional
    public void getAllReconcileDiscrepanciesByGatewayDateIsEqualToSomething() throws Exception {
        // Initialize the database
        reconcileDiscrepancyRepository.saveAndFlush(reconcileDiscrepancy);

        // Get all the reconcileDiscrepancyList where gatewayDate equals to DEFAULT_GATEWAY_DATE
        defaultReconcileDiscrepancyShouldBeFound("gatewayDate.equals=" + DEFAULT_GATEWAY_DATE);

        // Get all the reconcileDiscrepancyList where gatewayDate equals to UPDATED_GATEWAY_DATE
        defaultReconcileDiscrepancyShouldNotBeFound("gatewayDate.equals=" + UPDATED_GATEWAY_DATE);
    }

    @Test
    @Transactional
    public void getAllReconcileDiscrepanciesByGatewayDateIsInShouldWork() throws Exception {
        // Initialize the database
        reconcileDiscrepancyRepository.saveAndFlush(reconcileDiscrepancy);

        // Get all the reconcileDiscrepancyList where gatewayDate in DEFAULT_GATEWAY_DATE or UPDATED_GATEWAY_DATE
        defaultReconcileDiscrepancyShouldBeFound("gatewayDate.in=" + DEFAULT_GATEWAY_DATE + "," + UPDATED_GATEWAY_DATE);

        // Get all the reconcileDiscrepancyList where gatewayDate equals to UPDATED_GATEWAY_DATE
        defaultReconcileDiscrepancyShouldNotBeFound("gatewayDate.in=" + UPDATED_GATEWAY_DATE);
    }

    @Test
    @Transactional
    public void getAllReconcileDiscrepanciesByGatewayDateIsNullOrNotNull() throws Exception {
        // Initialize the database
        reconcileDiscrepancyRepository.saveAndFlush(reconcileDiscrepancy);

        // Get all the reconcileDiscrepancyList where gatewayDate is not null
        defaultReconcileDiscrepancyShouldBeFound("gatewayDate.specified=true");

        // Get all the reconcileDiscrepancyList where gatewayDate is null
        defaultReconcileDiscrepancyShouldNotBeFound("gatewayDate.specified=false");
    }

    @Test
    @Transactional
    public void getAllReconcileDiscrepanciesByClosedIsEqualToSomething() throws Exception {
        // Initialize the database
        reconcileDiscrepancyRepository.saveAndFlush(reconcileDiscrepancy);

        // Get all the reconcileDiscrepancyList where closed equals to DEFAULT_CLOSED
        defaultReconcileDiscrepancyShouldBeFound("closed.equals=" + DEFAULT_CLOSED);

        // Get all the reconcileDiscrepancyList where closed equals to UPDATED_CLOSED
        defaultReconcileDiscrepancyShouldNotBeFound("closed.equals=" + UPDATED_CLOSED);
    }

    @Test
    @Transactional
    public void getAllReconcileDiscrepanciesByClosedIsInShouldWork() throws Exception {
        // Initialize the database
        reconcileDiscrepancyRepository.saveAndFlush(reconcileDiscrepancy);

        // Get all the reconcileDiscrepancyList where closed in DEFAULT_CLOSED or UPDATED_CLOSED
        defaultReconcileDiscrepancyShouldBeFound("closed.in=" + DEFAULT_CLOSED + "," + UPDATED_CLOSED);

        // Get all the reconcileDiscrepancyList where closed equals to UPDATED_CLOSED
        defaultReconcileDiscrepancyShouldNotBeFound("closed.in=" + UPDATED_CLOSED);
    }

    @Test
    @Transactional
    public void getAllReconcileDiscrepanciesByClosedIsNullOrNotNull() throws Exception {
        // Initialize the database
        reconcileDiscrepancyRepository.saveAndFlush(reconcileDiscrepancy);

        // Get all the reconcileDiscrepancyList where closed is not null
        defaultReconcileDiscrepancyShouldBeFound("closed.specified=true");

        // Get all the reconcileDiscrepancyList where closed is null
        defaultReconcileDiscrepancyShouldNotBeFound("closed.specified=false");
    }

    @Test
    @Transactional
    public void getAllReconcileDiscrepanciesByCommentIsEqualToSomething() throws Exception {
        // Initialize the database
        reconcileDiscrepancyRepository.saveAndFlush(reconcileDiscrepancy);

        // Get all the reconcileDiscrepancyList where comment equals to DEFAULT_COMMENT
        defaultReconcileDiscrepancyShouldBeFound("comment.equals=" + DEFAULT_COMMENT);

        // Get all the reconcileDiscrepancyList where comment equals to UPDATED_COMMENT
        defaultReconcileDiscrepancyShouldNotBeFound("comment.equals=" + UPDATED_COMMENT);
    }

    @Test
    @Transactional
    public void getAllReconcileDiscrepanciesByCommentIsInShouldWork() throws Exception {
        // Initialize the database
        reconcileDiscrepancyRepository.saveAndFlush(reconcileDiscrepancy);

        // Get all the reconcileDiscrepancyList where comment in DEFAULT_COMMENT or UPDATED_COMMENT
        defaultReconcileDiscrepancyShouldBeFound("comment.in=" + DEFAULT_COMMENT + "," + UPDATED_COMMENT);

        // Get all the reconcileDiscrepancyList where comment equals to UPDATED_COMMENT
        defaultReconcileDiscrepancyShouldNotBeFound("comment.in=" + UPDATED_COMMENT);
    }

    @Test
    @Transactional
    public void getAllReconcileDiscrepanciesByCommentIsNullOrNotNull() throws Exception {
        // Initialize the database
        reconcileDiscrepancyRepository.saveAndFlush(reconcileDiscrepancy);

        // Get all the reconcileDiscrepancyList where comment is not null
        defaultReconcileDiscrepancyShouldBeFound("comment.specified=true");

        // Get all the reconcileDiscrepancyList where comment is null
        defaultReconcileDiscrepancyShouldNotBeFound("comment.specified=false");
    }

    @Test
    @Transactional
    public void getAllReconcileDiscrepanciesByReconcileSummaryIsEqualToSomething() throws Exception {
        // Initialize the database
        ReconcileSummary reconcileSummary = ReconcileSummaryResourceIntTest.createEntity(em);
        em.persist(reconcileSummary);
        em.flush();
        reconcileDiscrepancy.setReconcileSummary(reconcileSummary);
        reconcileDiscrepancyRepository.saveAndFlush(reconcileDiscrepancy);
        Long reconcileSummaryId = reconcileSummary.getId();

        // Get all the reconcileDiscrepancyList where reconcileSummary equals to reconcileSummaryId
        defaultReconcileDiscrepancyShouldBeFound("reconcileSummaryId.equals=" + reconcileSummaryId);

        // Get all the reconcileDiscrepancyList where reconcileSummary equals to reconcileSummaryId + 1
        defaultReconcileDiscrepancyShouldNotBeFound("reconcileSummaryId.equals=" + (reconcileSummaryId + 1));
    }

    /**
     * Executes the search, and checks that the default entity is returned
     */
    private void defaultReconcileDiscrepancyShouldBeFound(String filter) throws Exception {
        restReconcileDiscrepancyMockMvc
            .perform(get("/api/reconcile-discrepancies?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(reconcileDiscrepancy.getId().intValue())))
            .andExpect(jsonPath("$.[*].gatewayName").value(hasItem(DEFAULT_GATEWAY_NAME)))
            .andExpect(jsonPath("$.[*].systemTransactionId").value(hasItem(DEFAULT_SYSTEM_TRANSACTION_ID.intValue())))
            .andExpect(jsonPath("$.[*].gatewayTransactionId").value(hasItem(DEFAULT_GATEWAY_TRANSACTION_ID)))
            .andExpect(jsonPath("$.[*].systemAmount").value(hasItem(DEFAULT_SYSTEM_AMOUNT.intValue())))
            .andExpect(jsonPath("$.[*].gatewayAmount").value(hasItem(DEFAULT_GATEWAY_AMOUNT.intValue())))
            .andExpect(jsonPath("$.[*].systemStatus").value(hasItem(DEFAULT_SYSTEM_STATUS.booleanValue())))
            .andExpect(jsonPath("$.[*].gatewayStatus").value(hasItem(DEFAULT_GATEWAY_STATUS.booleanValue())))
            .andExpect(jsonPath("$.[*].systemDate").value(hasItem(DEFAULT_SYSTEM_DATE.toString())))
            .andExpect(jsonPath("$.[*].gatewayDate").value(hasItem(DEFAULT_GATEWAY_DATE.toString())))
            .andExpect(jsonPath("$.[*].closed").value(hasItem(DEFAULT_CLOSED.booleanValue())))
            .andExpect(jsonPath("$.[*].comment").value(hasItem(DEFAULT_COMMENT)));

        // Check, that the count call also returns 1
        restReconcileDiscrepancyMockMvc
            .perform(get("/api/reconcile-discrepancies/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(content().string("1"));
    }

    /**
     * Executes the search, and checks that the default entity is not returned
     */
    private void defaultReconcileDiscrepancyShouldNotBeFound(String filter) throws Exception {
        restReconcileDiscrepancyMockMvc
            .perform(get("/api/reconcile-discrepancies?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty());

        // Check, that the count call also returns 0
        restReconcileDiscrepancyMockMvc
            .perform(get("/api/reconcile-discrepancies/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(content().string("0"));
    }

    @Test
    @Transactional
    public void getNonExistingReconcileDiscrepancy() throws Exception {
        // Get the reconcileDiscrepancy
        restReconcileDiscrepancyMockMvc.perform(get("/api/reconcile-discrepancies/{id}", Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updateReconcileDiscrepancy() throws Exception {
        // Initialize the database
        reconcileDiscrepancyRepository.saveAndFlush(reconcileDiscrepancy);

        int databaseSizeBeforeUpdate = reconcileDiscrepancyRepository.findAll().size();

        // Update the reconcileDiscrepancy
        ReconcileDiscrepancy updatedReconcileDiscrepancy = reconcileDiscrepancyRepository.findById(reconcileDiscrepancy.getId()).get();
        // Disconnect from session so that the updates on updatedReconcileDiscrepancy are not directly saved in db
        em.detach(updatedReconcileDiscrepancy);
        updatedReconcileDiscrepancy
            .gatewayName(UPDATED_GATEWAY_NAME)
            .systemTransactionId(UPDATED_SYSTEM_TRANSACTION_ID)
            .gatewayTransactionId(UPDATED_GATEWAY_TRANSACTION_ID)
            .systemAmount(UPDATED_SYSTEM_AMOUNT)
            .gatewayAmount(UPDATED_GATEWAY_AMOUNT)
            .systemStatus(UPDATED_SYSTEM_STATUS)
            .gatewayStatus(UPDATED_GATEWAY_STATUS)
            .systemDate(UPDATED_SYSTEM_DATE)
            .gatewayDate(UPDATED_GATEWAY_DATE)
            .closed(UPDATED_CLOSED)
            .comment(UPDATED_COMMENT);
        ReconcileDiscrepancyDTO reconcileDiscrepancyDTO = reconcileDiscrepancyMapper.toDto(updatedReconcileDiscrepancy);

        restReconcileDiscrepancyMockMvc
            .perform(
                put("/api/reconcile-discrepancies")
                    .contentType(TestUtil.APPLICATION_JSON_UTF8)
                    .content(TestUtil.convertObjectToJsonBytes(reconcileDiscrepancyDTO))
            )
            .andExpect(status().isOk());

        // Validate the ReconcileDiscrepancy in the database
        List<ReconcileDiscrepancy> reconcileDiscrepancyList = reconcileDiscrepancyRepository.findAll();
        assertThat(reconcileDiscrepancyList).hasSize(databaseSizeBeforeUpdate);
        ReconcileDiscrepancy testReconcileDiscrepancy = reconcileDiscrepancyList.get(reconcileDiscrepancyList.size() - 1);
        assertThat(testReconcileDiscrepancy.getGatewayName()).isEqualTo(UPDATED_GATEWAY_NAME);
        assertThat(testReconcileDiscrepancy.getSystemTransactionId()).isEqualTo(UPDATED_SYSTEM_TRANSACTION_ID);
        assertThat(testReconcileDiscrepancy.getGatewayTransactionId()).isEqualTo(UPDATED_GATEWAY_TRANSACTION_ID);
        assertThat(testReconcileDiscrepancy.getSystemAmount()).isEqualTo(UPDATED_SYSTEM_AMOUNT);
        assertThat(testReconcileDiscrepancy.getGatewayAmount()).isEqualTo(UPDATED_GATEWAY_AMOUNT);
        assertThat(testReconcileDiscrepancy.isSystemStatus()).isEqualTo(UPDATED_SYSTEM_STATUS);
        assertThat(testReconcileDiscrepancy.isGatewayStatus()).isEqualTo(UPDATED_GATEWAY_STATUS);
        assertThat(testReconcileDiscrepancy.getSystemDate()).isEqualTo(UPDATED_SYSTEM_DATE);
        assertThat(testReconcileDiscrepancy.getGatewayDate()).isEqualTo(UPDATED_GATEWAY_DATE);
        assertThat(testReconcileDiscrepancy.isClosed()).isEqualTo(UPDATED_CLOSED);
        assertThat(testReconcileDiscrepancy.getComment()).isEqualTo(UPDATED_COMMENT);
    }

    @Test
    @Transactional
    public void updateNonExistingReconcileDiscrepancy() throws Exception {
        int databaseSizeBeforeUpdate = reconcileDiscrepancyRepository.findAll().size();

        // Create the ReconcileDiscrepancy
        ReconcileDiscrepancyDTO reconcileDiscrepancyDTO = reconcileDiscrepancyMapper.toDto(reconcileDiscrepancy);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restReconcileDiscrepancyMockMvc
            .perform(
                put("/api/reconcile-discrepancies")
                    .contentType(TestUtil.APPLICATION_JSON_UTF8)
                    .content(TestUtil.convertObjectToJsonBytes(reconcileDiscrepancyDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the ReconcileDiscrepancy in the database
        List<ReconcileDiscrepancy> reconcileDiscrepancyList = reconcileDiscrepancyRepository.findAll();
        assertThat(reconcileDiscrepancyList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    public void deleteReconcileDiscrepancy() throws Exception {
        // Initialize the database
        reconcileDiscrepancyRepository.saveAndFlush(reconcileDiscrepancy);

        int databaseSizeBeforeDelete = reconcileDiscrepancyRepository.findAll().size();

        // Get the reconcileDiscrepancy
        restReconcileDiscrepancyMockMvc
            .perform(delete("/api/reconcile-discrepancies/{id}", reconcileDiscrepancy.getId()).accept(TestUtil.APPLICATION_JSON_UTF8))
            .andExpect(status().isOk());

        // Validate the database is empty
        List<ReconcileDiscrepancy> reconcileDiscrepancyList = reconcileDiscrepancyRepository.findAll();
        assertThat(reconcileDiscrepancyList).hasSize(databaseSizeBeforeDelete - 1);
    }

    @Test
    @Transactional
    public void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(ReconcileDiscrepancy.class);
        ReconcileDiscrepancy reconcileDiscrepancy1 = new ReconcileDiscrepancy();
        reconcileDiscrepancy1.setId(1L);
        ReconcileDiscrepancy reconcileDiscrepancy2 = new ReconcileDiscrepancy();
        reconcileDiscrepancy2.setId(reconcileDiscrepancy1.getId());
        assertThat(reconcileDiscrepancy1).isEqualTo(reconcileDiscrepancy2);
        reconcileDiscrepancy2.setId(2L);
        assertThat(reconcileDiscrepancy1).isNotEqualTo(reconcileDiscrepancy2);
        reconcileDiscrepancy1.setId(null);
        assertThat(reconcileDiscrepancy1).isNotEqualTo(reconcileDiscrepancy2);
    }

    @Test
    @Transactional
    public void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(ReconcileDiscrepancyDTO.class);
        ReconcileDiscrepancyDTO reconcileDiscrepancyDTO1 = new ReconcileDiscrepancyDTO();
        reconcileDiscrepancyDTO1.setId(1L);
        ReconcileDiscrepancyDTO reconcileDiscrepancyDTO2 = new ReconcileDiscrepancyDTO();
        assertThat(reconcileDiscrepancyDTO1).isNotEqualTo(reconcileDiscrepancyDTO2);
        reconcileDiscrepancyDTO2.setId(reconcileDiscrepancyDTO1.getId());
        assertThat(reconcileDiscrepancyDTO1).isEqualTo(reconcileDiscrepancyDTO2);
        reconcileDiscrepancyDTO2.setId(2L);
        assertThat(reconcileDiscrepancyDTO1).isNotEqualTo(reconcileDiscrepancyDTO2);
        reconcileDiscrepancyDTO1.setId(null);
        assertThat(reconcileDiscrepancyDTO1).isNotEqualTo(reconcileDiscrepancyDTO2);
    }
    //    @Test
    //    @Transactional
    //    public void testEntityFromId() {
    //        assertThat(reconcileDiscrepancyMapper.fromId(42L).getId()).isEqualTo(42);
    //        assertThat(reconcileDiscrepancyMapper.fromId(null)).isNull();
    //    }
}
