package uz.paykap.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDate;
import java.time.ZoneId;
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
import uz.paykap.domain.ReconcileSummary;
import uz.paykap.domain.enumeration.ReconcilePeriod;
import uz.paykap.domain.enumeration.ReconcileSummaryStatus;
import uz.paykap.domain.enumeration.ReconcileSummaryType;
import uz.paykap.repository.ReconcileSummaryRepository;
import uz.paykap.service.dto.ReconcileSummaryDTO;
import uz.paykap.service.mapper.ReconcileSummaryMapper;

/**
 * Integration tests for the {@link ReconcileSummaryResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class ReconcileSummaryResourceIT {

    private static final Long DEFAULT_GATEWAY_ID = 1L;
    private static final Long UPDATED_GATEWAY_ID = 2L;
    private static final Long SMALLER_GATEWAY_ID = 1L - 1L;

    private static final Long DEFAULT_SYSTEM_AMOUNT = 1L;
    private static final Long UPDATED_SYSTEM_AMOUNT = 2L;
    private static final Long SMALLER_SYSTEM_AMOUNT = 1L - 1L;

    private static final Long DEFAULT_GATEWAY_AMOUNT = 1L;
    private static final Long UPDATED_GATEWAY_AMOUNT = 2L;
    private static final Long SMALLER_GATEWAY_AMOUNT = 1L - 1L;

    private static final Long DEFAULT_SYSTEM_COUNT = 1L;
    private static final Long UPDATED_SYSTEM_COUNT = 2L;
    private static final Long SMALLER_SYSTEM_COUNT = 1L - 1L;

    private static final Long DEFAULT_GATEWAY_COUNT = 1L;
    private static final Long UPDATED_GATEWAY_COUNT = 2L;
    private static final Long SMALLER_GATEWAY_COUNT = 1L - 1L;

    private static final LocalDate DEFAULT_DATE = LocalDate.ofEpochDay(0L);
    private static final LocalDate UPDATED_DATE = LocalDate.now(ZoneId.systemDefault());
    private static final LocalDate SMALLER_DATE = LocalDate.ofEpochDay(-1L);

    private static final ReconcileSummaryStatus DEFAULT_STATUS = ReconcileSummaryStatus.COMPARED_OK;
    private static final ReconcileSummaryStatus UPDATED_STATUS = ReconcileSummaryStatus.COMPARED_FALSE;

    private static final Boolean DEFAULT_SUCCESS = false;
    private static final Boolean UPDATED_SUCCESS = true;

    private static final ReconcilePeriod DEFAULT_PERIOD = ReconcilePeriod.DAY;
    private static final ReconcilePeriod UPDATED_PERIOD = ReconcilePeriod.MONTH;

    private static final String DEFAULT_GATEWAY_NAME = "AAAAAAAAAA";
    private static final String UPDATED_GATEWAY_NAME = "BBBBBBBBBB";

    private static final ReconcileSummaryType DEFAULT_TYPE = ReconcileSummaryType.PAYMENT;
    private static final ReconcileSummaryType UPDATED_TYPE = ReconcileSummaryType.TERMINAL;

    private static final String ENTITY_API_URL = "/api/reconcile-summaries";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong count = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private ReconcileSummaryRepository reconcileSummaryRepository;

    @Autowired
    private ReconcileSummaryMapper reconcileSummaryMapper;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restReconcileSummaryMockMvc;

    private ReconcileSummary reconcileSummary;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static ReconcileSummary createEntity(EntityManager em) {
        ReconcileSummary reconcileSummary = new ReconcileSummary()
            .gatewayId(DEFAULT_GATEWAY_ID)
            .systemAmount(DEFAULT_SYSTEM_AMOUNT)
            .gatewayAmount(DEFAULT_GATEWAY_AMOUNT)
            .systemCount(DEFAULT_SYSTEM_COUNT)
            .gatewayCount(DEFAULT_GATEWAY_COUNT)
            .date(DEFAULT_DATE)
            .status(DEFAULT_STATUS)
            .success(DEFAULT_SUCCESS)
            .period(DEFAULT_PERIOD)
            .gatewayName(DEFAULT_GATEWAY_NAME)
            .type(DEFAULT_TYPE);
        return reconcileSummary;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static ReconcileSummary createUpdatedEntity(EntityManager em) {
        ReconcileSummary reconcileSummary = new ReconcileSummary()
            .gatewayId(UPDATED_GATEWAY_ID)
            .systemAmount(UPDATED_SYSTEM_AMOUNT)
            .gatewayAmount(UPDATED_GATEWAY_AMOUNT)
            .systemCount(UPDATED_SYSTEM_COUNT)
            .gatewayCount(UPDATED_GATEWAY_COUNT)
            .date(UPDATED_DATE)
            .status(UPDATED_STATUS)
            .success(UPDATED_SUCCESS)
            .period(UPDATED_PERIOD)
            .gatewayName(UPDATED_GATEWAY_NAME)
            .type(UPDATED_TYPE);
        return reconcileSummary;
    }

    @BeforeEach
    public void initTest() {
        reconcileSummary = createEntity(em);
    }

    @Test
    @Transactional
    void createReconcileSummary() throws Exception {
        int databaseSizeBeforeCreate = reconcileSummaryRepository.findAll().size();
        // Create the ReconcileSummary
        ReconcileSummaryDTO reconcileSummaryDTO = reconcileSummaryMapper.toDto(reconcileSummary);
        restReconcileSummaryMockMvc
            .perform(
                post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(reconcileSummaryDTO))
            )
            .andExpect(status().isCreated());

        // Validate the ReconcileSummary in the database
        List<ReconcileSummary> reconcileSummaryList = reconcileSummaryRepository.findAll();
        assertThat(reconcileSummaryList).hasSize(databaseSizeBeforeCreate + 1);
        ReconcileSummary testReconcileSummary = reconcileSummaryList.get(reconcileSummaryList.size() - 1);
        assertThat(testReconcileSummary.getGatewayId()).isEqualTo(DEFAULT_GATEWAY_ID);
        assertThat(testReconcileSummary.getSystemAmount()).isEqualTo(DEFAULT_SYSTEM_AMOUNT);
        assertThat(testReconcileSummary.getGatewayAmount()).isEqualTo(DEFAULT_GATEWAY_AMOUNT);
        assertThat(testReconcileSummary.getSystemCount()).isEqualTo(DEFAULT_SYSTEM_COUNT);
        assertThat(testReconcileSummary.getGatewayCount()).isEqualTo(DEFAULT_GATEWAY_COUNT);
        assertThat(testReconcileSummary.getDate()).isEqualTo(DEFAULT_DATE);
        assertThat(testReconcileSummary.getStatus()).isEqualTo(DEFAULT_STATUS);
        assertThat(testReconcileSummary.getSuccess()).isEqualTo(DEFAULT_SUCCESS);
        assertThat(testReconcileSummary.getPeriod()).isEqualTo(DEFAULT_PERIOD);
        assertThat(testReconcileSummary.getGatewayName()).isEqualTo(DEFAULT_GATEWAY_NAME);
        assertThat(testReconcileSummary.getType()).isEqualTo(DEFAULT_TYPE);
    }

    @Test
    @Transactional
    void createReconcileSummaryWithExistingId() throws Exception {
        // Create the ReconcileSummary with an existing ID
        reconcileSummary.setId(1L);
        ReconcileSummaryDTO reconcileSummaryDTO = reconcileSummaryMapper.toDto(reconcileSummary);

        int databaseSizeBeforeCreate = reconcileSummaryRepository.findAll().size();

        // An entity with an existing ID cannot be created, so this API call must fail
        restReconcileSummaryMockMvc
            .perform(
                post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(reconcileSummaryDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the ReconcileSummary in the database
        List<ReconcileSummary> reconcileSummaryList = reconcileSummaryRepository.findAll();
        assertThat(reconcileSummaryList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkGatewayIdIsRequired() throws Exception {
        int databaseSizeBeforeTest = reconcileSummaryRepository.findAll().size();
        // set the field null
        reconcileSummary.setGatewayId(null);

        // Create the ReconcileSummary, which fails.
        ReconcileSummaryDTO reconcileSummaryDTO = reconcileSummaryMapper.toDto(reconcileSummary);

        restReconcileSummaryMockMvc
            .perform(
                post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(reconcileSummaryDTO))
            )
            .andExpect(status().isBadRequest());

        List<ReconcileSummary> reconcileSummaryList = reconcileSummaryRepository.findAll();
        assertThat(reconcileSummaryList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkSystemAmountIsRequired() throws Exception {
        int databaseSizeBeforeTest = reconcileSummaryRepository.findAll().size();
        // set the field null
        reconcileSummary.setSystemAmount(null);

        // Create the ReconcileSummary, which fails.
        ReconcileSummaryDTO reconcileSummaryDTO = reconcileSummaryMapper.toDto(reconcileSummary);

        restReconcileSummaryMockMvc
            .perform(
                post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(reconcileSummaryDTO))
            )
            .andExpect(status().isBadRequest());

        List<ReconcileSummary> reconcileSummaryList = reconcileSummaryRepository.findAll();
        assertThat(reconcileSummaryList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkGatewayAmountIsRequired() throws Exception {
        int databaseSizeBeforeTest = reconcileSummaryRepository.findAll().size();
        // set the field null
        reconcileSummary.setGatewayAmount(null);

        // Create the ReconcileSummary, which fails.
        ReconcileSummaryDTO reconcileSummaryDTO = reconcileSummaryMapper.toDto(reconcileSummary);

        restReconcileSummaryMockMvc
            .perform(
                post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(reconcileSummaryDTO))
            )
            .andExpect(status().isBadRequest());

        List<ReconcileSummary> reconcileSummaryList = reconcileSummaryRepository.findAll();
        assertThat(reconcileSummaryList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkSystemCountIsRequired() throws Exception {
        int databaseSizeBeforeTest = reconcileSummaryRepository.findAll().size();
        // set the field null
        reconcileSummary.setSystemCount(null);

        // Create the ReconcileSummary, which fails.
        ReconcileSummaryDTO reconcileSummaryDTO = reconcileSummaryMapper.toDto(reconcileSummary);

        restReconcileSummaryMockMvc
            .perform(
                post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(reconcileSummaryDTO))
            )
            .andExpect(status().isBadRequest());

        List<ReconcileSummary> reconcileSummaryList = reconcileSummaryRepository.findAll();
        assertThat(reconcileSummaryList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkGatewayCountIsRequired() throws Exception {
        int databaseSizeBeforeTest = reconcileSummaryRepository.findAll().size();
        // set the field null
        reconcileSummary.setGatewayCount(null);

        // Create the ReconcileSummary, which fails.
        ReconcileSummaryDTO reconcileSummaryDTO = reconcileSummaryMapper.toDto(reconcileSummary);

        restReconcileSummaryMockMvc
            .perform(
                post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(reconcileSummaryDTO))
            )
            .andExpect(status().isBadRequest());

        List<ReconcileSummary> reconcileSummaryList = reconcileSummaryRepository.findAll();
        assertThat(reconcileSummaryList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllReconcileSummaries() throws Exception {
        // Initialize the database
        reconcileSummaryRepository.saveAndFlush(reconcileSummary);

        // Get all the reconcileSummaryList
        restReconcileSummaryMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(reconcileSummary.getId().intValue())))
            .andExpect(jsonPath("$.[*].gatewayId").value(hasItem(DEFAULT_GATEWAY_ID.intValue())))
            .andExpect(jsonPath("$.[*].systemAmount").value(hasItem(DEFAULT_SYSTEM_AMOUNT.intValue())))
            .andExpect(jsonPath("$.[*].gatewayAmount").value(hasItem(DEFAULT_GATEWAY_AMOUNT.intValue())))
            .andExpect(jsonPath("$.[*].systemCount").value(hasItem(DEFAULT_SYSTEM_COUNT.intValue())))
            .andExpect(jsonPath("$.[*].gatewayCount").value(hasItem(DEFAULT_GATEWAY_COUNT.intValue())))
            .andExpect(jsonPath("$.[*].date").value(hasItem(DEFAULT_DATE.toString())))
            .andExpect(jsonPath("$.[*].status").value(hasItem(DEFAULT_STATUS.toString())))
            .andExpect(jsonPath("$.[*].success").value(hasItem(DEFAULT_SUCCESS.booleanValue())))
            .andExpect(jsonPath("$.[*].period").value(hasItem(DEFAULT_PERIOD.toString())))
            .andExpect(jsonPath("$.[*].gatewayName").value(hasItem(DEFAULT_GATEWAY_NAME)))
            .andExpect(jsonPath("$.[*].type").value(hasItem(DEFAULT_TYPE.toString())));
    }

    @Test
    @Transactional
    void getReconcileSummary() throws Exception {
        // Initialize the database
        reconcileSummaryRepository.saveAndFlush(reconcileSummary);

        // Get the reconcileSummary
        restReconcileSummaryMockMvc
            .perform(get(ENTITY_API_URL_ID, reconcileSummary.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(reconcileSummary.getId().intValue()))
            .andExpect(jsonPath("$.gatewayId").value(DEFAULT_GATEWAY_ID.intValue()))
            .andExpect(jsonPath("$.systemAmount").value(DEFAULT_SYSTEM_AMOUNT.intValue()))
            .andExpect(jsonPath("$.gatewayAmount").value(DEFAULT_GATEWAY_AMOUNT.intValue()))
            .andExpect(jsonPath("$.systemCount").value(DEFAULT_SYSTEM_COUNT.intValue()))
            .andExpect(jsonPath("$.gatewayCount").value(DEFAULT_GATEWAY_COUNT.intValue()))
            .andExpect(jsonPath("$.date").value(DEFAULT_DATE.toString()))
            .andExpect(jsonPath("$.status").value(DEFAULT_STATUS.toString()))
            .andExpect(jsonPath("$.success").value(DEFAULT_SUCCESS.booleanValue()))
            .andExpect(jsonPath("$.period").value(DEFAULT_PERIOD.toString()))
            .andExpect(jsonPath("$.gatewayName").value(DEFAULT_GATEWAY_NAME))
            .andExpect(jsonPath("$.type").value(DEFAULT_TYPE.toString()));
    }

    @Test
    @Transactional
    void getReconcileSummariesByIdFiltering() throws Exception {
        // Initialize the database
        reconcileSummaryRepository.saveAndFlush(reconcileSummary);

        Long id = reconcileSummary.getId();

        defaultReconcileSummaryShouldBeFound("id.equals=" + id);
        defaultReconcileSummaryShouldNotBeFound("id.notEquals=" + id);

        defaultReconcileSummaryShouldBeFound("id.greaterThanOrEqual=" + id);
        defaultReconcileSummaryShouldNotBeFound("id.greaterThan=" + id);

        defaultReconcileSummaryShouldBeFound("id.lessThanOrEqual=" + id);
        defaultReconcileSummaryShouldNotBeFound("id.lessThan=" + id);
    }

    @Test
    @Transactional
    void getAllReconcileSummariesByGatewayIdIsEqualToSomething() throws Exception {
        // Initialize the database
        reconcileSummaryRepository.saveAndFlush(reconcileSummary);

        // Get all the reconcileSummaryList where gatewayId equals to DEFAULT_GATEWAY_ID
        defaultReconcileSummaryShouldBeFound("gatewayId.equals=" + DEFAULT_GATEWAY_ID);

        // Get all the reconcileSummaryList where gatewayId equals to UPDATED_GATEWAY_ID
        defaultReconcileSummaryShouldNotBeFound("gatewayId.equals=" + UPDATED_GATEWAY_ID);
    }

    @Test
    @Transactional
    void getAllReconcileSummariesByGatewayIdIsNotEqualToSomething() throws Exception {
        // Initialize the database
        reconcileSummaryRepository.saveAndFlush(reconcileSummary);

        // Get all the reconcileSummaryList where gatewayId not equals to DEFAULT_GATEWAY_ID
        defaultReconcileSummaryShouldNotBeFound("gatewayId.notEquals=" + DEFAULT_GATEWAY_ID);

        // Get all the reconcileSummaryList where gatewayId not equals to UPDATED_GATEWAY_ID
        defaultReconcileSummaryShouldBeFound("gatewayId.notEquals=" + UPDATED_GATEWAY_ID);
    }

    @Test
    @Transactional
    void getAllReconcileSummariesByGatewayIdIsInShouldWork() throws Exception {
        // Initialize the database
        reconcileSummaryRepository.saveAndFlush(reconcileSummary);

        // Get all the reconcileSummaryList where gatewayId in DEFAULT_GATEWAY_ID or UPDATED_GATEWAY_ID
        defaultReconcileSummaryShouldBeFound("gatewayId.in=" + DEFAULT_GATEWAY_ID + "," + UPDATED_GATEWAY_ID);

        // Get all the reconcileSummaryList where gatewayId equals to UPDATED_GATEWAY_ID
        defaultReconcileSummaryShouldNotBeFound("gatewayId.in=" + UPDATED_GATEWAY_ID);
    }

    @Test
    @Transactional
    void getAllReconcileSummariesByGatewayIdIsNullOrNotNull() throws Exception {
        // Initialize the database
        reconcileSummaryRepository.saveAndFlush(reconcileSummary);

        // Get all the reconcileSummaryList where gatewayId is not null
        defaultReconcileSummaryShouldBeFound("gatewayId.specified=true");

        // Get all the reconcileSummaryList where gatewayId is null
        defaultReconcileSummaryShouldNotBeFound("gatewayId.specified=false");
    }

    @Test
    @Transactional
    void getAllReconcileSummariesByGatewayIdIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        reconcileSummaryRepository.saveAndFlush(reconcileSummary);

        // Get all the reconcileSummaryList where gatewayId is greater than or equal to DEFAULT_GATEWAY_ID
        defaultReconcileSummaryShouldBeFound("gatewayId.greaterThanOrEqual=" + DEFAULT_GATEWAY_ID);

        // Get all the reconcileSummaryList where gatewayId is greater than or equal to UPDATED_GATEWAY_ID
        defaultReconcileSummaryShouldNotBeFound("gatewayId.greaterThanOrEqual=" + UPDATED_GATEWAY_ID);
    }

    @Test
    @Transactional
    void getAllReconcileSummariesByGatewayIdIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        reconcileSummaryRepository.saveAndFlush(reconcileSummary);

        // Get all the reconcileSummaryList where gatewayId is less than or equal to DEFAULT_GATEWAY_ID
        defaultReconcileSummaryShouldBeFound("gatewayId.lessThanOrEqual=" + DEFAULT_GATEWAY_ID);

        // Get all the reconcileSummaryList where gatewayId is less than or equal to SMALLER_GATEWAY_ID
        defaultReconcileSummaryShouldNotBeFound("gatewayId.lessThanOrEqual=" + SMALLER_GATEWAY_ID);
    }

    @Test
    @Transactional
    void getAllReconcileSummariesByGatewayIdIsLessThanSomething() throws Exception {
        // Initialize the database
        reconcileSummaryRepository.saveAndFlush(reconcileSummary);

        // Get all the reconcileSummaryList where gatewayId is less than DEFAULT_GATEWAY_ID
        defaultReconcileSummaryShouldNotBeFound("gatewayId.lessThan=" + DEFAULT_GATEWAY_ID);

        // Get all the reconcileSummaryList where gatewayId is less than UPDATED_GATEWAY_ID
        defaultReconcileSummaryShouldBeFound("gatewayId.lessThan=" + UPDATED_GATEWAY_ID);
    }

    @Test
    @Transactional
    void getAllReconcileSummariesByGatewayIdIsGreaterThanSomething() throws Exception {
        // Initialize the database
        reconcileSummaryRepository.saveAndFlush(reconcileSummary);

        // Get all the reconcileSummaryList where gatewayId is greater than DEFAULT_GATEWAY_ID
        defaultReconcileSummaryShouldNotBeFound("gatewayId.greaterThan=" + DEFAULT_GATEWAY_ID);

        // Get all the reconcileSummaryList where gatewayId is greater than SMALLER_GATEWAY_ID
        defaultReconcileSummaryShouldBeFound("gatewayId.greaterThan=" + SMALLER_GATEWAY_ID);
    }

    @Test
    @Transactional
    void getAllReconcileSummariesBySystemAmountIsEqualToSomething() throws Exception {
        // Initialize the database
        reconcileSummaryRepository.saveAndFlush(reconcileSummary);

        // Get all the reconcileSummaryList where systemAmount equals to DEFAULT_SYSTEM_AMOUNT
        defaultReconcileSummaryShouldBeFound("systemAmount.equals=" + DEFAULT_SYSTEM_AMOUNT);

        // Get all the reconcileSummaryList where systemAmount equals to UPDATED_SYSTEM_AMOUNT
        defaultReconcileSummaryShouldNotBeFound("systemAmount.equals=" + UPDATED_SYSTEM_AMOUNT);
    }

    @Test
    @Transactional
    void getAllReconcileSummariesBySystemAmountIsNotEqualToSomething() throws Exception {
        // Initialize the database
        reconcileSummaryRepository.saveAndFlush(reconcileSummary);

        // Get all the reconcileSummaryList where systemAmount not equals to DEFAULT_SYSTEM_AMOUNT
        defaultReconcileSummaryShouldNotBeFound("systemAmount.notEquals=" + DEFAULT_SYSTEM_AMOUNT);

        // Get all the reconcileSummaryList where systemAmount not equals to UPDATED_SYSTEM_AMOUNT
        defaultReconcileSummaryShouldBeFound("systemAmount.notEquals=" + UPDATED_SYSTEM_AMOUNT);
    }

    @Test
    @Transactional
    void getAllReconcileSummariesBySystemAmountIsInShouldWork() throws Exception {
        // Initialize the database
        reconcileSummaryRepository.saveAndFlush(reconcileSummary);

        // Get all the reconcileSummaryList where systemAmount in DEFAULT_SYSTEM_AMOUNT or UPDATED_SYSTEM_AMOUNT
        defaultReconcileSummaryShouldBeFound("systemAmount.in=" + DEFAULT_SYSTEM_AMOUNT + "," + UPDATED_SYSTEM_AMOUNT);

        // Get all the reconcileSummaryList where systemAmount equals to UPDATED_SYSTEM_AMOUNT
        defaultReconcileSummaryShouldNotBeFound("systemAmount.in=" + UPDATED_SYSTEM_AMOUNT);
    }

    @Test
    @Transactional
    void getAllReconcileSummariesBySystemAmountIsNullOrNotNull() throws Exception {
        // Initialize the database
        reconcileSummaryRepository.saveAndFlush(reconcileSummary);

        // Get all the reconcileSummaryList where systemAmount is not null
        defaultReconcileSummaryShouldBeFound("systemAmount.specified=true");

        // Get all the reconcileSummaryList where systemAmount is null
        defaultReconcileSummaryShouldNotBeFound("systemAmount.specified=false");
    }

    @Test
    @Transactional
    void getAllReconcileSummariesBySystemAmountIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        reconcileSummaryRepository.saveAndFlush(reconcileSummary);

        // Get all the reconcileSummaryList where systemAmount is greater than or equal to DEFAULT_SYSTEM_AMOUNT
        defaultReconcileSummaryShouldBeFound("systemAmount.greaterThanOrEqual=" + DEFAULT_SYSTEM_AMOUNT);

        // Get all the reconcileSummaryList where systemAmount is greater than or equal to UPDATED_SYSTEM_AMOUNT
        defaultReconcileSummaryShouldNotBeFound("systemAmount.greaterThanOrEqual=" + UPDATED_SYSTEM_AMOUNT);
    }

    @Test
    @Transactional
    void getAllReconcileSummariesBySystemAmountIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        reconcileSummaryRepository.saveAndFlush(reconcileSummary);

        // Get all the reconcileSummaryList where systemAmount is less than or equal to DEFAULT_SYSTEM_AMOUNT
        defaultReconcileSummaryShouldBeFound("systemAmount.lessThanOrEqual=" + DEFAULT_SYSTEM_AMOUNT);

        // Get all the reconcileSummaryList where systemAmount is less than or equal to SMALLER_SYSTEM_AMOUNT
        defaultReconcileSummaryShouldNotBeFound("systemAmount.lessThanOrEqual=" + SMALLER_SYSTEM_AMOUNT);
    }

    @Test
    @Transactional
    void getAllReconcileSummariesBySystemAmountIsLessThanSomething() throws Exception {
        // Initialize the database
        reconcileSummaryRepository.saveAndFlush(reconcileSummary);

        // Get all the reconcileSummaryList where systemAmount is less than DEFAULT_SYSTEM_AMOUNT
        defaultReconcileSummaryShouldNotBeFound("systemAmount.lessThan=" + DEFAULT_SYSTEM_AMOUNT);

        // Get all the reconcileSummaryList where systemAmount is less than UPDATED_SYSTEM_AMOUNT
        defaultReconcileSummaryShouldBeFound("systemAmount.lessThan=" + UPDATED_SYSTEM_AMOUNT);
    }

    @Test
    @Transactional
    void getAllReconcileSummariesBySystemAmountIsGreaterThanSomething() throws Exception {
        // Initialize the database
        reconcileSummaryRepository.saveAndFlush(reconcileSummary);

        // Get all the reconcileSummaryList where systemAmount is greater than DEFAULT_SYSTEM_AMOUNT
        defaultReconcileSummaryShouldNotBeFound("systemAmount.greaterThan=" + DEFAULT_SYSTEM_AMOUNT);

        // Get all the reconcileSummaryList where systemAmount is greater than SMALLER_SYSTEM_AMOUNT
        defaultReconcileSummaryShouldBeFound("systemAmount.greaterThan=" + SMALLER_SYSTEM_AMOUNT);
    }

    @Test
    @Transactional
    void getAllReconcileSummariesByGatewayAmountIsEqualToSomething() throws Exception {
        // Initialize the database
        reconcileSummaryRepository.saveAndFlush(reconcileSummary);

        // Get all the reconcileSummaryList where gatewayAmount equals to DEFAULT_GATEWAY_AMOUNT
        defaultReconcileSummaryShouldBeFound("gatewayAmount.equals=" + DEFAULT_GATEWAY_AMOUNT);

        // Get all the reconcileSummaryList where gatewayAmount equals to UPDATED_GATEWAY_AMOUNT
        defaultReconcileSummaryShouldNotBeFound("gatewayAmount.equals=" + UPDATED_GATEWAY_AMOUNT);
    }

    @Test
    @Transactional
    void getAllReconcileSummariesByGatewayAmountIsNotEqualToSomething() throws Exception {
        // Initialize the database
        reconcileSummaryRepository.saveAndFlush(reconcileSummary);

        // Get all the reconcileSummaryList where gatewayAmount not equals to DEFAULT_GATEWAY_AMOUNT
        defaultReconcileSummaryShouldNotBeFound("gatewayAmount.notEquals=" + DEFAULT_GATEWAY_AMOUNT);

        // Get all the reconcileSummaryList where gatewayAmount not equals to UPDATED_GATEWAY_AMOUNT
        defaultReconcileSummaryShouldBeFound("gatewayAmount.notEquals=" + UPDATED_GATEWAY_AMOUNT);
    }

    @Test
    @Transactional
    void getAllReconcileSummariesByGatewayAmountIsInShouldWork() throws Exception {
        // Initialize the database
        reconcileSummaryRepository.saveAndFlush(reconcileSummary);

        // Get all the reconcileSummaryList where gatewayAmount in DEFAULT_GATEWAY_AMOUNT or UPDATED_GATEWAY_AMOUNT
        defaultReconcileSummaryShouldBeFound("gatewayAmount.in=" + DEFAULT_GATEWAY_AMOUNT + "," + UPDATED_GATEWAY_AMOUNT);

        // Get all the reconcileSummaryList where gatewayAmount equals to UPDATED_GATEWAY_AMOUNT
        defaultReconcileSummaryShouldNotBeFound("gatewayAmount.in=" + UPDATED_GATEWAY_AMOUNT);
    }

    @Test
    @Transactional
    void getAllReconcileSummariesByGatewayAmountIsNullOrNotNull() throws Exception {
        // Initialize the database
        reconcileSummaryRepository.saveAndFlush(reconcileSummary);

        // Get all the reconcileSummaryList where gatewayAmount is not null
        defaultReconcileSummaryShouldBeFound("gatewayAmount.specified=true");

        // Get all the reconcileSummaryList where gatewayAmount is null
        defaultReconcileSummaryShouldNotBeFound("gatewayAmount.specified=false");
    }

    @Test
    @Transactional
    void getAllReconcileSummariesByGatewayAmountIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        reconcileSummaryRepository.saveAndFlush(reconcileSummary);

        // Get all the reconcileSummaryList where gatewayAmount is greater than or equal to DEFAULT_GATEWAY_AMOUNT
        defaultReconcileSummaryShouldBeFound("gatewayAmount.greaterThanOrEqual=" + DEFAULT_GATEWAY_AMOUNT);

        // Get all the reconcileSummaryList where gatewayAmount is greater than or equal to UPDATED_GATEWAY_AMOUNT
        defaultReconcileSummaryShouldNotBeFound("gatewayAmount.greaterThanOrEqual=" + UPDATED_GATEWAY_AMOUNT);
    }

    @Test
    @Transactional
    void getAllReconcileSummariesByGatewayAmountIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        reconcileSummaryRepository.saveAndFlush(reconcileSummary);

        // Get all the reconcileSummaryList where gatewayAmount is less than or equal to DEFAULT_GATEWAY_AMOUNT
        defaultReconcileSummaryShouldBeFound("gatewayAmount.lessThanOrEqual=" + DEFAULT_GATEWAY_AMOUNT);

        // Get all the reconcileSummaryList where gatewayAmount is less than or equal to SMALLER_GATEWAY_AMOUNT
        defaultReconcileSummaryShouldNotBeFound("gatewayAmount.lessThanOrEqual=" + SMALLER_GATEWAY_AMOUNT);
    }

    @Test
    @Transactional
    void getAllReconcileSummariesByGatewayAmountIsLessThanSomething() throws Exception {
        // Initialize the database
        reconcileSummaryRepository.saveAndFlush(reconcileSummary);

        // Get all the reconcileSummaryList where gatewayAmount is less than DEFAULT_GATEWAY_AMOUNT
        defaultReconcileSummaryShouldNotBeFound("gatewayAmount.lessThan=" + DEFAULT_GATEWAY_AMOUNT);

        // Get all the reconcileSummaryList where gatewayAmount is less than UPDATED_GATEWAY_AMOUNT
        defaultReconcileSummaryShouldBeFound("gatewayAmount.lessThan=" + UPDATED_GATEWAY_AMOUNT);
    }

    @Test
    @Transactional
    void getAllReconcileSummariesByGatewayAmountIsGreaterThanSomething() throws Exception {
        // Initialize the database
        reconcileSummaryRepository.saveAndFlush(reconcileSummary);

        // Get all the reconcileSummaryList where gatewayAmount is greater than DEFAULT_GATEWAY_AMOUNT
        defaultReconcileSummaryShouldNotBeFound("gatewayAmount.greaterThan=" + DEFAULT_GATEWAY_AMOUNT);

        // Get all the reconcileSummaryList where gatewayAmount is greater than SMALLER_GATEWAY_AMOUNT
        defaultReconcileSummaryShouldBeFound("gatewayAmount.greaterThan=" + SMALLER_GATEWAY_AMOUNT);
    }

    @Test
    @Transactional
    void getAllReconcileSummariesBySystemCountIsEqualToSomething() throws Exception {
        // Initialize the database
        reconcileSummaryRepository.saveAndFlush(reconcileSummary);

        // Get all the reconcileSummaryList where systemCount equals to DEFAULT_SYSTEM_COUNT
        defaultReconcileSummaryShouldBeFound("systemCount.equals=" + DEFAULT_SYSTEM_COUNT);

        // Get all the reconcileSummaryList where systemCount equals to UPDATED_SYSTEM_COUNT
        defaultReconcileSummaryShouldNotBeFound("systemCount.equals=" + UPDATED_SYSTEM_COUNT);
    }

    @Test
    @Transactional
    void getAllReconcileSummariesBySystemCountIsNotEqualToSomething() throws Exception {
        // Initialize the database
        reconcileSummaryRepository.saveAndFlush(reconcileSummary);

        // Get all the reconcileSummaryList where systemCount not equals to DEFAULT_SYSTEM_COUNT
        defaultReconcileSummaryShouldNotBeFound("systemCount.notEquals=" + DEFAULT_SYSTEM_COUNT);

        // Get all the reconcileSummaryList where systemCount not equals to UPDATED_SYSTEM_COUNT
        defaultReconcileSummaryShouldBeFound("systemCount.notEquals=" + UPDATED_SYSTEM_COUNT);
    }

    @Test
    @Transactional
    void getAllReconcileSummariesBySystemCountIsInShouldWork() throws Exception {
        // Initialize the database
        reconcileSummaryRepository.saveAndFlush(reconcileSummary);

        // Get all the reconcileSummaryList where systemCount in DEFAULT_SYSTEM_COUNT or UPDATED_SYSTEM_COUNT
        defaultReconcileSummaryShouldBeFound("systemCount.in=" + DEFAULT_SYSTEM_COUNT + "," + UPDATED_SYSTEM_COUNT);

        // Get all the reconcileSummaryList where systemCount equals to UPDATED_SYSTEM_COUNT
        defaultReconcileSummaryShouldNotBeFound("systemCount.in=" + UPDATED_SYSTEM_COUNT);
    }

    @Test
    @Transactional
    void getAllReconcileSummariesBySystemCountIsNullOrNotNull() throws Exception {
        // Initialize the database
        reconcileSummaryRepository.saveAndFlush(reconcileSummary);

        // Get all the reconcileSummaryList where systemCount is not null
        defaultReconcileSummaryShouldBeFound("systemCount.specified=true");

        // Get all the reconcileSummaryList where systemCount is null
        defaultReconcileSummaryShouldNotBeFound("systemCount.specified=false");
    }

    @Test
    @Transactional
    void getAllReconcileSummariesBySystemCountIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        reconcileSummaryRepository.saveAndFlush(reconcileSummary);

        // Get all the reconcileSummaryList where systemCount is greater than or equal to DEFAULT_SYSTEM_COUNT
        defaultReconcileSummaryShouldBeFound("systemCount.greaterThanOrEqual=" + DEFAULT_SYSTEM_COUNT);

        // Get all the reconcileSummaryList where systemCount is greater than or equal to UPDATED_SYSTEM_COUNT
        defaultReconcileSummaryShouldNotBeFound("systemCount.greaterThanOrEqual=" + UPDATED_SYSTEM_COUNT);
    }

    @Test
    @Transactional
    void getAllReconcileSummariesBySystemCountIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        reconcileSummaryRepository.saveAndFlush(reconcileSummary);

        // Get all the reconcileSummaryList where systemCount is less than or equal to DEFAULT_SYSTEM_COUNT
        defaultReconcileSummaryShouldBeFound("systemCount.lessThanOrEqual=" + DEFAULT_SYSTEM_COUNT);

        // Get all the reconcileSummaryList where systemCount is less than or equal to SMALLER_SYSTEM_COUNT
        defaultReconcileSummaryShouldNotBeFound("systemCount.lessThanOrEqual=" + SMALLER_SYSTEM_COUNT);
    }

    @Test
    @Transactional
    void getAllReconcileSummariesBySystemCountIsLessThanSomething() throws Exception {
        // Initialize the database
        reconcileSummaryRepository.saveAndFlush(reconcileSummary);

        // Get all the reconcileSummaryList where systemCount is less than DEFAULT_SYSTEM_COUNT
        defaultReconcileSummaryShouldNotBeFound("systemCount.lessThan=" + DEFAULT_SYSTEM_COUNT);

        // Get all the reconcileSummaryList where systemCount is less than UPDATED_SYSTEM_COUNT
        defaultReconcileSummaryShouldBeFound("systemCount.lessThan=" + UPDATED_SYSTEM_COUNT);
    }

    @Test
    @Transactional
    void getAllReconcileSummariesBySystemCountIsGreaterThanSomething() throws Exception {
        // Initialize the database
        reconcileSummaryRepository.saveAndFlush(reconcileSummary);

        // Get all the reconcileSummaryList where systemCount is greater than DEFAULT_SYSTEM_COUNT
        defaultReconcileSummaryShouldNotBeFound("systemCount.greaterThan=" + DEFAULT_SYSTEM_COUNT);

        // Get all the reconcileSummaryList where systemCount is greater than SMALLER_SYSTEM_COUNT
        defaultReconcileSummaryShouldBeFound("systemCount.greaterThan=" + SMALLER_SYSTEM_COUNT);
    }

    @Test
    @Transactional
    void getAllReconcileSummariesByGatewayCountIsEqualToSomething() throws Exception {
        // Initialize the database
        reconcileSummaryRepository.saveAndFlush(reconcileSummary);

        // Get all the reconcileSummaryList where gatewayCount equals to DEFAULT_GATEWAY_COUNT
        defaultReconcileSummaryShouldBeFound("gatewayCount.equals=" + DEFAULT_GATEWAY_COUNT);

        // Get all the reconcileSummaryList where gatewayCount equals to UPDATED_GATEWAY_COUNT
        defaultReconcileSummaryShouldNotBeFound("gatewayCount.equals=" + UPDATED_GATEWAY_COUNT);
    }

    @Test
    @Transactional
    void getAllReconcileSummariesByGatewayCountIsNotEqualToSomething() throws Exception {
        // Initialize the database
        reconcileSummaryRepository.saveAndFlush(reconcileSummary);

        // Get all the reconcileSummaryList where gatewayCount not equals to DEFAULT_GATEWAY_COUNT
        defaultReconcileSummaryShouldNotBeFound("gatewayCount.notEquals=" + DEFAULT_GATEWAY_COUNT);

        // Get all the reconcileSummaryList where gatewayCount not equals to UPDATED_GATEWAY_COUNT
        defaultReconcileSummaryShouldBeFound("gatewayCount.notEquals=" + UPDATED_GATEWAY_COUNT);
    }

    @Test
    @Transactional
    void getAllReconcileSummariesByGatewayCountIsInShouldWork() throws Exception {
        // Initialize the database
        reconcileSummaryRepository.saveAndFlush(reconcileSummary);

        // Get all the reconcileSummaryList where gatewayCount in DEFAULT_GATEWAY_COUNT or UPDATED_GATEWAY_COUNT
        defaultReconcileSummaryShouldBeFound("gatewayCount.in=" + DEFAULT_GATEWAY_COUNT + "," + UPDATED_GATEWAY_COUNT);

        // Get all the reconcileSummaryList where gatewayCount equals to UPDATED_GATEWAY_COUNT
        defaultReconcileSummaryShouldNotBeFound("gatewayCount.in=" + UPDATED_GATEWAY_COUNT);
    }

    @Test
    @Transactional
    void getAllReconcileSummariesByGatewayCountIsNullOrNotNull() throws Exception {
        // Initialize the database
        reconcileSummaryRepository.saveAndFlush(reconcileSummary);

        // Get all the reconcileSummaryList where gatewayCount is not null
        defaultReconcileSummaryShouldBeFound("gatewayCount.specified=true");

        // Get all the reconcileSummaryList where gatewayCount is null
        defaultReconcileSummaryShouldNotBeFound("gatewayCount.specified=false");
    }

    @Test
    @Transactional
    void getAllReconcileSummariesByGatewayCountIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        reconcileSummaryRepository.saveAndFlush(reconcileSummary);

        // Get all the reconcileSummaryList where gatewayCount is greater than or equal to DEFAULT_GATEWAY_COUNT
        defaultReconcileSummaryShouldBeFound("gatewayCount.greaterThanOrEqual=" + DEFAULT_GATEWAY_COUNT);

        // Get all the reconcileSummaryList where gatewayCount is greater than or equal to UPDATED_GATEWAY_COUNT
        defaultReconcileSummaryShouldNotBeFound("gatewayCount.greaterThanOrEqual=" + UPDATED_GATEWAY_COUNT);
    }

    @Test
    @Transactional
    void getAllReconcileSummariesByGatewayCountIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        reconcileSummaryRepository.saveAndFlush(reconcileSummary);

        // Get all the reconcileSummaryList where gatewayCount is less than or equal to DEFAULT_GATEWAY_COUNT
        defaultReconcileSummaryShouldBeFound("gatewayCount.lessThanOrEqual=" + DEFAULT_GATEWAY_COUNT);

        // Get all the reconcileSummaryList where gatewayCount is less than or equal to SMALLER_GATEWAY_COUNT
        defaultReconcileSummaryShouldNotBeFound("gatewayCount.lessThanOrEqual=" + SMALLER_GATEWAY_COUNT);
    }

    @Test
    @Transactional
    void getAllReconcileSummariesByGatewayCountIsLessThanSomething() throws Exception {
        // Initialize the database
        reconcileSummaryRepository.saveAndFlush(reconcileSummary);

        // Get all the reconcileSummaryList where gatewayCount is less than DEFAULT_GATEWAY_COUNT
        defaultReconcileSummaryShouldNotBeFound("gatewayCount.lessThan=" + DEFAULT_GATEWAY_COUNT);

        // Get all the reconcileSummaryList where gatewayCount is less than UPDATED_GATEWAY_COUNT
        defaultReconcileSummaryShouldBeFound("gatewayCount.lessThan=" + UPDATED_GATEWAY_COUNT);
    }

    @Test
    @Transactional
    void getAllReconcileSummariesByGatewayCountIsGreaterThanSomething() throws Exception {
        // Initialize the database
        reconcileSummaryRepository.saveAndFlush(reconcileSummary);

        // Get all the reconcileSummaryList where gatewayCount is greater than DEFAULT_GATEWAY_COUNT
        defaultReconcileSummaryShouldNotBeFound("gatewayCount.greaterThan=" + DEFAULT_GATEWAY_COUNT);

        // Get all the reconcileSummaryList where gatewayCount is greater than SMALLER_GATEWAY_COUNT
        defaultReconcileSummaryShouldBeFound("gatewayCount.greaterThan=" + SMALLER_GATEWAY_COUNT);
    }

    @Test
    @Transactional
    void getAllReconcileSummariesByDateIsEqualToSomething() throws Exception {
        // Initialize the database
        reconcileSummaryRepository.saveAndFlush(reconcileSummary);

        // Get all the reconcileSummaryList where date equals to DEFAULT_DATE
        defaultReconcileSummaryShouldBeFound("date.equals=" + DEFAULT_DATE);

        // Get all the reconcileSummaryList where date equals to UPDATED_DATE
        defaultReconcileSummaryShouldNotBeFound("date.equals=" + UPDATED_DATE);
    }

    @Test
    @Transactional
    void getAllReconcileSummariesByDateIsNotEqualToSomething() throws Exception {
        // Initialize the database
        reconcileSummaryRepository.saveAndFlush(reconcileSummary);

        // Get all the reconcileSummaryList where date not equals to DEFAULT_DATE
        defaultReconcileSummaryShouldNotBeFound("date.notEquals=" + DEFAULT_DATE);

        // Get all the reconcileSummaryList where date not equals to UPDATED_DATE
        defaultReconcileSummaryShouldBeFound("date.notEquals=" + UPDATED_DATE);
    }

    @Test
    @Transactional
    void getAllReconcileSummariesByDateIsInShouldWork() throws Exception {
        // Initialize the database
        reconcileSummaryRepository.saveAndFlush(reconcileSummary);

        // Get all the reconcileSummaryList where date in DEFAULT_DATE or UPDATED_DATE
        defaultReconcileSummaryShouldBeFound("date.in=" + DEFAULT_DATE + "," + UPDATED_DATE);

        // Get all the reconcileSummaryList where date equals to UPDATED_DATE
        defaultReconcileSummaryShouldNotBeFound("date.in=" + UPDATED_DATE);
    }

    @Test
    @Transactional
    void getAllReconcileSummariesByDateIsNullOrNotNull() throws Exception {
        // Initialize the database
        reconcileSummaryRepository.saveAndFlush(reconcileSummary);

        // Get all the reconcileSummaryList where date is not null
        defaultReconcileSummaryShouldBeFound("date.specified=true");

        // Get all the reconcileSummaryList where date is null
        defaultReconcileSummaryShouldNotBeFound("date.specified=false");
    }

    @Test
    @Transactional
    void getAllReconcileSummariesByDateIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        reconcileSummaryRepository.saveAndFlush(reconcileSummary);

        // Get all the reconcileSummaryList where date is greater than or equal to DEFAULT_DATE
        defaultReconcileSummaryShouldBeFound("date.greaterThanOrEqual=" + DEFAULT_DATE);

        // Get all the reconcileSummaryList where date is greater than or equal to UPDATED_DATE
        defaultReconcileSummaryShouldNotBeFound("date.greaterThanOrEqual=" + UPDATED_DATE);
    }

    @Test
    @Transactional
    void getAllReconcileSummariesByDateIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        reconcileSummaryRepository.saveAndFlush(reconcileSummary);

        // Get all the reconcileSummaryList where date is less than or equal to DEFAULT_DATE
        defaultReconcileSummaryShouldBeFound("date.lessThanOrEqual=" + DEFAULT_DATE);

        // Get all the reconcileSummaryList where date is less than or equal to SMALLER_DATE
        defaultReconcileSummaryShouldNotBeFound("date.lessThanOrEqual=" + SMALLER_DATE);
    }

    @Test
    @Transactional
    void getAllReconcileSummariesByDateIsLessThanSomething() throws Exception {
        // Initialize the database
        reconcileSummaryRepository.saveAndFlush(reconcileSummary);

        // Get all the reconcileSummaryList where date is less than DEFAULT_DATE
        defaultReconcileSummaryShouldNotBeFound("date.lessThan=" + DEFAULT_DATE);

        // Get all the reconcileSummaryList where date is less than UPDATED_DATE
        defaultReconcileSummaryShouldBeFound("date.lessThan=" + UPDATED_DATE);
    }

    @Test
    @Transactional
    void getAllReconcileSummariesByDateIsGreaterThanSomething() throws Exception {
        // Initialize the database
        reconcileSummaryRepository.saveAndFlush(reconcileSummary);

        // Get all the reconcileSummaryList where date is greater than DEFAULT_DATE
        defaultReconcileSummaryShouldNotBeFound("date.greaterThan=" + DEFAULT_DATE);

        // Get all the reconcileSummaryList where date is greater than SMALLER_DATE
        defaultReconcileSummaryShouldBeFound("date.greaterThan=" + SMALLER_DATE);
    }

    @Test
    @Transactional
    void getAllReconcileSummariesByStatusIsEqualToSomething() throws Exception {
        // Initialize the database
        reconcileSummaryRepository.saveAndFlush(reconcileSummary);

        // Get all the reconcileSummaryList where status equals to DEFAULT_STATUS
        defaultReconcileSummaryShouldBeFound("status.equals=" + DEFAULT_STATUS);

        // Get all the reconcileSummaryList where status equals to UPDATED_STATUS
        defaultReconcileSummaryShouldNotBeFound("status.equals=" + UPDATED_STATUS);
    }

    @Test
    @Transactional
    void getAllReconcileSummariesByStatusIsNotEqualToSomething() throws Exception {
        // Initialize the database
        reconcileSummaryRepository.saveAndFlush(reconcileSummary);

        // Get all the reconcileSummaryList where status not equals to DEFAULT_STATUS
        defaultReconcileSummaryShouldNotBeFound("status.notEquals=" + DEFAULT_STATUS);

        // Get all the reconcileSummaryList where status not equals to UPDATED_STATUS
        defaultReconcileSummaryShouldBeFound("status.notEquals=" + UPDATED_STATUS);
    }

    @Test
    @Transactional
    void getAllReconcileSummariesByStatusIsInShouldWork() throws Exception {
        // Initialize the database
        reconcileSummaryRepository.saveAndFlush(reconcileSummary);

        // Get all the reconcileSummaryList where status in DEFAULT_STATUS or UPDATED_STATUS
        defaultReconcileSummaryShouldBeFound("status.in=" + DEFAULT_STATUS + "," + UPDATED_STATUS);

        // Get all the reconcileSummaryList where status equals to UPDATED_STATUS
        defaultReconcileSummaryShouldNotBeFound("status.in=" + UPDATED_STATUS);
    }

    @Test
    @Transactional
    void getAllReconcileSummariesByStatusIsNullOrNotNull() throws Exception {
        // Initialize the database
        reconcileSummaryRepository.saveAndFlush(reconcileSummary);

        // Get all the reconcileSummaryList where status is not null
        defaultReconcileSummaryShouldBeFound("status.specified=true");

        // Get all the reconcileSummaryList where status is null
        defaultReconcileSummaryShouldNotBeFound("status.specified=false");
    }

    @Test
    @Transactional
    void getAllReconcileSummariesBySuccessIsEqualToSomething() throws Exception {
        // Initialize the database
        reconcileSummaryRepository.saveAndFlush(reconcileSummary);

        // Get all the reconcileSummaryList where success equals to DEFAULT_SUCCESS
        defaultReconcileSummaryShouldBeFound("success.equals=" + DEFAULT_SUCCESS);

        // Get all the reconcileSummaryList where success equals to UPDATED_SUCCESS
        defaultReconcileSummaryShouldNotBeFound("success.equals=" + UPDATED_SUCCESS);
    }

    @Test
    @Transactional
    void getAllReconcileSummariesBySuccessIsNotEqualToSomething() throws Exception {
        // Initialize the database
        reconcileSummaryRepository.saveAndFlush(reconcileSummary);

        // Get all the reconcileSummaryList where success not equals to DEFAULT_SUCCESS
        defaultReconcileSummaryShouldNotBeFound("success.notEquals=" + DEFAULT_SUCCESS);

        // Get all the reconcileSummaryList where success not equals to UPDATED_SUCCESS
        defaultReconcileSummaryShouldBeFound("success.notEquals=" + UPDATED_SUCCESS);
    }

    @Test
    @Transactional
    void getAllReconcileSummariesBySuccessIsInShouldWork() throws Exception {
        // Initialize the database
        reconcileSummaryRepository.saveAndFlush(reconcileSummary);

        // Get all the reconcileSummaryList where success in DEFAULT_SUCCESS or UPDATED_SUCCESS
        defaultReconcileSummaryShouldBeFound("success.in=" + DEFAULT_SUCCESS + "," + UPDATED_SUCCESS);

        // Get all the reconcileSummaryList where success equals to UPDATED_SUCCESS
        defaultReconcileSummaryShouldNotBeFound("success.in=" + UPDATED_SUCCESS);
    }

    @Test
    @Transactional
    void getAllReconcileSummariesBySuccessIsNullOrNotNull() throws Exception {
        // Initialize the database
        reconcileSummaryRepository.saveAndFlush(reconcileSummary);

        // Get all the reconcileSummaryList where success is not null
        defaultReconcileSummaryShouldBeFound("success.specified=true");

        // Get all the reconcileSummaryList where success is null
        defaultReconcileSummaryShouldNotBeFound("success.specified=false");
    }

    @Test
    @Transactional
    void getAllReconcileSummariesByPeriodIsEqualToSomething() throws Exception {
        // Initialize the database
        reconcileSummaryRepository.saveAndFlush(reconcileSummary);

        // Get all the reconcileSummaryList where period equals to DEFAULT_PERIOD
        defaultReconcileSummaryShouldBeFound("period.equals=" + DEFAULT_PERIOD);

        // Get all the reconcileSummaryList where period equals to UPDATED_PERIOD
        defaultReconcileSummaryShouldNotBeFound("period.equals=" + UPDATED_PERIOD);
    }

    @Test
    @Transactional
    void getAllReconcileSummariesByPeriodIsNotEqualToSomething() throws Exception {
        // Initialize the database
        reconcileSummaryRepository.saveAndFlush(reconcileSummary);

        // Get all the reconcileSummaryList where period not equals to DEFAULT_PERIOD
        defaultReconcileSummaryShouldNotBeFound("period.notEquals=" + DEFAULT_PERIOD);

        // Get all the reconcileSummaryList where period not equals to UPDATED_PERIOD
        defaultReconcileSummaryShouldBeFound("period.notEquals=" + UPDATED_PERIOD);
    }

    @Test
    @Transactional
    void getAllReconcileSummariesByPeriodIsInShouldWork() throws Exception {
        // Initialize the database
        reconcileSummaryRepository.saveAndFlush(reconcileSummary);

        // Get all the reconcileSummaryList where period in DEFAULT_PERIOD or UPDATED_PERIOD
        defaultReconcileSummaryShouldBeFound("period.in=" + DEFAULT_PERIOD + "," + UPDATED_PERIOD);

        // Get all the reconcileSummaryList where period equals to UPDATED_PERIOD
        defaultReconcileSummaryShouldNotBeFound("period.in=" + UPDATED_PERIOD);
    }

    @Test
    @Transactional
    void getAllReconcileSummariesByPeriodIsNullOrNotNull() throws Exception {
        // Initialize the database
        reconcileSummaryRepository.saveAndFlush(reconcileSummary);

        // Get all the reconcileSummaryList where period is not null
        defaultReconcileSummaryShouldBeFound("period.specified=true");

        // Get all the reconcileSummaryList where period is null
        defaultReconcileSummaryShouldNotBeFound("period.specified=false");
    }

    @Test
    @Transactional
    void getAllReconcileSummariesByGatewayNameIsEqualToSomething() throws Exception {
        // Initialize the database
        reconcileSummaryRepository.saveAndFlush(reconcileSummary);

        // Get all the reconcileSummaryList where gatewayName equals to DEFAULT_GATEWAY_NAME
        defaultReconcileSummaryShouldBeFound("gatewayName.equals=" + DEFAULT_GATEWAY_NAME);

        // Get all the reconcileSummaryList where gatewayName equals to UPDATED_GATEWAY_NAME
        defaultReconcileSummaryShouldNotBeFound("gatewayName.equals=" + UPDATED_GATEWAY_NAME);
    }

    @Test
    @Transactional
    void getAllReconcileSummariesByGatewayNameIsNotEqualToSomething() throws Exception {
        // Initialize the database
        reconcileSummaryRepository.saveAndFlush(reconcileSummary);

        // Get all the reconcileSummaryList where gatewayName not equals to DEFAULT_GATEWAY_NAME
        defaultReconcileSummaryShouldNotBeFound("gatewayName.notEquals=" + DEFAULT_GATEWAY_NAME);

        // Get all the reconcileSummaryList where gatewayName not equals to UPDATED_GATEWAY_NAME
        defaultReconcileSummaryShouldBeFound("gatewayName.notEquals=" + UPDATED_GATEWAY_NAME);
    }

    @Test
    @Transactional
    void getAllReconcileSummariesByGatewayNameIsInShouldWork() throws Exception {
        // Initialize the database
        reconcileSummaryRepository.saveAndFlush(reconcileSummary);

        // Get all the reconcileSummaryList where gatewayName in DEFAULT_GATEWAY_NAME or UPDATED_GATEWAY_NAME
        defaultReconcileSummaryShouldBeFound("gatewayName.in=" + DEFAULT_GATEWAY_NAME + "," + UPDATED_GATEWAY_NAME);

        // Get all the reconcileSummaryList where gatewayName equals to UPDATED_GATEWAY_NAME
        defaultReconcileSummaryShouldNotBeFound("gatewayName.in=" + UPDATED_GATEWAY_NAME);
    }

    @Test
    @Transactional
    void getAllReconcileSummariesByGatewayNameIsNullOrNotNull() throws Exception {
        // Initialize the database
        reconcileSummaryRepository.saveAndFlush(reconcileSummary);

        // Get all the reconcileSummaryList where gatewayName is not null
        defaultReconcileSummaryShouldBeFound("gatewayName.specified=true");

        // Get all the reconcileSummaryList where gatewayName is null
        defaultReconcileSummaryShouldNotBeFound("gatewayName.specified=false");
    }

    @Test
    @Transactional
    void getAllReconcileSummariesByGatewayNameContainsSomething() throws Exception {
        // Initialize the database
        reconcileSummaryRepository.saveAndFlush(reconcileSummary);

        // Get all the reconcileSummaryList where gatewayName contains DEFAULT_GATEWAY_NAME
        defaultReconcileSummaryShouldBeFound("gatewayName.contains=" + DEFAULT_GATEWAY_NAME);

        // Get all the reconcileSummaryList where gatewayName contains UPDATED_GATEWAY_NAME
        defaultReconcileSummaryShouldNotBeFound("gatewayName.contains=" + UPDATED_GATEWAY_NAME);
    }

    @Test
    @Transactional
    void getAllReconcileSummariesByGatewayNameNotContainsSomething() throws Exception {
        // Initialize the database
        reconcileSummaryRepository.saveAndFlush(reconcileSummary);

        // Get all the reconcileSummaryList where gatewayName does not contain DEFAULT_GATEWAY_NAME
        defaultReconcileSummaryShouldNotBeFound("gatewayName.doesNotContain=" + DEFAULT_GATEWAY_NAME);

        // Get all the reconcileSummaryList where gatewayName does not contain UPDATED_GATEWAY_NAME
        defaultReconcileSummaryShouldBeFound("gatewayName.doesNotContain=" + UPDATED_GATEWAY_NAME);
    }

    /**
     * Executes the search, and checks that the default entity is returned.
     */
    private void defaultReconcileSummaryShouldBeFound(String filter) throws Exception {
        restReconcileSummaryMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(reconcileSummary.getId().intValue())))
            .andExpect(jsonPath("$.[*].gatewayId").value(hasItem(DEFAULT_GATEWAY_ID.intValue())))
            .andExpect(jsonPath("$.[*].systemAmount").value(hasItem(DEFAULT_SYSTEM_AMOUNT.intValue())))
            .andExpect(jsonPath("$.[*].gatewayAmount").value(hasItem(DEFAULT_GATEWAY_AMOUNT.intValue())))
            .andExpect(jsonPath("$.[*].systemCount").value(hasItem(DEFAULT_SYSTEM_COUNT.intValue())))
            .andExpect(jsonPath("$.[*].gatewayCount").value(hasItem(DEFAULT_GATEWAY_COUNT.intValue())))
            .andExpect(jsonPath("$.[*].date").value(hasItem(DEFAULT_DATE.toString())))
            .andExpect(jsonPath("$.[*].status").value(hasItem(DEFAULT_STATUS.toString())))
            .andExpect(jsonPath("$.[*].success").value(hasItem(DEFAULT_SUCCESS.booleanValue())))
            .andExpect(jsonPath("$.[*].period").value(hasItem(DEFAULT_PERIOD.toString())))
            .andExpect(jsonPath("$.[*].gatewayName").value(hasItem(DEFAULT_GATEWAY_NAME)))
            .andExpect(jsonPath("$.[*].type").value(hasItem(DEFAULT_TYPE.toString())));

        // Check, that the count call also returns 1
        restReconcileSummaryMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("1"));
    }

    /**
     * Executes the search, and checks that the default entity is not returned.
     */
    private void defaultReconcileSummaryShouldNotBeFound(String filter) throws Exception {
        restReconcileSummaryMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty());

        // Check, that the count call also returns 0
        restReconcileSummaryMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("0"));
    }

    @Test
    @Transactional
    void getNonExistingReconcileSummary() throws Exception {
        // Get the reconcileSummary
        restReconcileSummaryMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putNewReconcileSummary() throws Exception {
        // Initialize the database
        reconcileSummaryRepository.saveAndFlush(reconcileSummary);

        int databaseSizeBeforeUpdate = reconcileSummaryRepository.findAll().size();

        // Update the reconcileSummary
        ReconcileSummary updatedReconcileSummary = reconcileSummaryRepository.findById(reconcileSummary.getId()).get();
        // Disconnect from session so that the updates on updatedReconcileSummary are not directly saved in db
        em.detach(updatedReconcileSummary);
        updatedReconcileSummary
            .gatewayId(UPDATED_GATEWAY_ID)
            .systemAmount(UPDATED_SYSTEM_AMOUNT)
            .gatewayAmount(UPDATED_GATEWAY_AMOUNT)
            .systemCount(UPDATED_SYSTEM_COUNT)
            .gatewayCount(UPDATED_GATEWAY_COUNT)
            .date(UPDATED_DATE)
            .status(UPDATED_STATUS)
            .success(UPDATED_SUCCESS)
            .period(UPDATED_PERIOD)
            .gatewayName(UPDATED_GATEWAY_NAME)
            .type(UPDATED_TYPE);
        ReconcileSummaryDTO reconcileSummaryDTO = reconcileSummaryMapper.toDto(updatedReconcileSummary);

        restReconcileSummaryMockMvc
            .perform(
                put(ENTITY_API_URL_ID, reconcileSummaryDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(reconcileSummaryDTO))
            )
            .andExpect(status().isOk());

        // Validate the ReconcileSummary in the database
        List<ReconcileSummary> reconcileSummaryList = reconcileSummaryRepository.findAll();
        assertThat(reconcileSummaryList).hasSize(databaseSizeBeforeUpdate);
        ReconcileSummary testReconcileSummary = reconcileSummaryList.get(reconcileSummaryList.size() - 1);
        assertThat(testReconcileSummary.getGatewayId()).isEqualTo(UPDATED_GATEWAY_ID);
        assertThat(testReconcileSummary.getSystemAmount()).isEqualTo(UPDATED_SYSTEM_AMOUNT);
        assertThat(testReconcileSummary.getGatewayAmount()).isEqualTo(UPDATED_GATEWAY_AMOUNT);
        assertThat(testReconcileSummary.getSystemCount()).isEqualTo(UPDATED_SYSTEM_COUNT);
        assertThat(testReconcileSummary.getGatewayCount()).isEqualTo(UPDATED_GATEWAY_COUNT);
        assertThat(testReconcileSummary.getDate()).isEqualTo(UPDATED_DATE);
        assertThat(testReconcileSummary.getStatus()).isEqualTo(UPDATED_STATUS);
        assertThat(testReconcileSummary.getSuccess()).isEqualTo(UPDATED_SUCCESS);
        assertThat(testReconcileSummary.getPeriod()).isEqualTo(UPDATED_PERIOD);
        assertThat(testReconcileSummary.getGatewayName()).isEqualTo(UPDATED_GATEWAY_NAME);
        assertThat(testReconcileSummary.getType()).isEqualTo(UPDATED_TYPE);
    }

    @Test
    @Transactional
    void putNonExistingReconcileSummary() throws Exception {
        int databaseSizeBeforeUpdate = reconcileSummaryRepository.findAll().size();
        reconcileSummary.setId(count.incrementAndGet());

        // Create the ReconcileSummary
        ReconcileSummaryDTO reconcileSummaryDTO = reconcileSummaryMapper.toDto(reconcileSummary);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restReconcileSummaryMockMvc
            .perform(
                put(ENTITY_API_URL_ID, reconcileSummaryDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(reconcileSummaryDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the ReconcileSummary in the database
        List<ReconcileSummary> reconcileSummaryList = reconcileSummaryRepository.findAll();
        assertThat(reconcileSummaryList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchReconcileSummary() throws Exception {
        int databaseSizeBeforeUpdate = reconcileSummaryRepository.findAll().size();
        reconcileSummary.setId(count.incrementAndGet());

        // Create the ReconcileSummary
        ReconcileSummaryDTO reconcileSummaryDTO = reconcileSummaryMapper.toDto(reconcileSummary);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restReconcileSummaryMockMvc
            .perform(
                put(ENTITY_API_URL_ID, count.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(reconcileSummaryDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the ReconcileSummary in the database
        List<ReconcileSummary> reconcileSummaryList = reconcileSummaryRepository.findAll();
        assertThat(reconcileSummaryList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamReconcileSummary() throws Exception {
        int databaseSizeBeforeUpdate = reconcileSummaryRepository.findAll().size();
        reconcileSummary.setId(count.incrementAndGet());

        // Create the ReconcileSummary
        ReconcileSummaryDTO reconcileSummaryDTO = reconcileSummaryMapper.toDto(reconcileSummary);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restReconcileSummaryMockMvc
            .perform(
                put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(reconcileSummaryDTO))
            )
            .andExpect(status().isMethodNotAllowed());

        // Validate the ReconcileSummary in the database
        List<ReconcileSummary> reconcileSummaryList = reconcileSummaryRepository.findAll();
        assertThat(reconcileSummaryList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateReconcileSummaryWithPatch() throws Exception {
        // Initialize the database
        reconcileSummaryRepository.saveAndFlush(reconcileSummary);

        int databaseSizeBeforeUpdate = reconcileSummaryRepository.findAll().size();

        // Update the reconcileSummary using partial update
        ReconcileSummary partialUpdatedReconcileSummary = new ReconcileSummary();
        partialUpdatedReconcileSummary.setId(reconcileSummary.getId());

        partialUpdatedReconcileSummary.gatewayAmount(UPDATED_GATEWAY_AMOUNT).period(UPDATED_PERIOD);

        restReconcileSummaryMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedReconcileSummary.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedReconcileSummary))
            )
            .andExpect(status().isOk());

        // Validate the ReconcileSummary in the database
        List<ReconcileSummary> reconcileSummaryList = reconcileSummaryRepository.findAll();
        assertThat(reconcileSummaryList).hasSize(databaseSizeBeforeUpdate);
        ReconcileSummary testReconcileSummary = reconcileSummaryList.get(reconcileSummaryList.size() - 1);
        assertThat(testReconcileSummary.getGatewayId()).isEqualTo(DEFAULT_GATEWAY_ID);
        assertThat(testReconcileSummary.getSystemAmount()).isEqualTo(DEFAULT_SYSTEM_AMOUNT);
        assertThat(testReconcileSummary.getGatewayAmount()).isEqualTo(UPDATED_GATEWAY_AMOUNT);
        assertThat(testReconcileSummary.getSystemCount()).isEqualTo(DEFAULT_SYSTEM_COUNT);
        assertThat(testReconcileSummary.getGatewayCount()).isEqualTo(DEFAULT_GATEWAY_COUNT);
        assertThat(testReconcileSummary.getDate()).isEqualTo(DEFAULT_DATE);
        assertThat(testReconcileSummary.getStatus()).isEqualTo(DEFAULT_STATUS);
        assertThat(testReconcileSummary.getSuccess()).isEqualTo(DEFAULT_SUCCESS);
        assertThat(testReconcileSummary.getPeriod()).isEqualTo(UPDATED_PERIOD);
        assertThat(testReconcileSummary.getGatewayName()).isEqualTo(DEFAULT_GATEWAY_NAME);
        assertThat(testReconcileSummary.getType()).isEqualTo(DEFAULT_TYPE);
    }

    @Test
    @Transactional
    void fullUpdateReconcileSummaryWithPatch() throws Exception {
        // Initialize the database
        reconcileSummaryRepository.saveAndFlush(reconcileSummary);

        int databaseSizeBeforeUpdate = reconcileSummaryRepository.findAll().size();

        // Update the reconcileSummary using partial update
        ReconcileSummary partialUpdatedReconcileSummary = new ReconcileSummary();
        partialUpdatedReconcileSummary.setId(reconcileSummary.getId());

        partialUpdatedReconcileSummary
            .gatewayId(UPDATED_GATEWAY_ID)
            .systemAmount(UPDATED_SYSTEM_AMOUNT)
            .gatewayAmount(UPDATED_GATEWAY_AMOUNT)
            .systemCount(UPDATED_SYSTEM_COUNT)
            .gatewayCount(UPDATED_GATEWAY_COUNT)
            .date(UPDATED_DATE)
            .status(UPDATED_STATUS)
            .success(UPDATED_SUCCESS)
            .period(UPDATED_PERIOD)
            .gatewayName(UPDATED_GATEWAY_NAME)
            .type(UPDATED_TYPE);

        restReconcileSummaryMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedReconcileSummary.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedReconcileSummary))
            )
            .andExpect(status().isOk());

        // Validate the ReconcileSummary in the database
        List<ReconcileSummary> reconcileSummaryList = reconcileSummaryRepository.findAll();
        assertThat(reconcileSummaryList).hasSize(databaseSizeBeforeUpdate);
        ReconcileSummary testReconcileSummary = reconcileSummaryList.get(reconcileSummaryList.size() - 1);
        assertThat(testReconcileSummary.getGatewayId()).isEqualTo(UPDATED_GATEWAY_ID);
        assertThat(testReconcileSummary.getSystemAmount()).isEqualTo(UPDATED_SYSTEM_AMOUNT);
        assertThat(testReconcileSummary.getGatewayAmount()).isEqualTo(UPDATED_GATEWAY_AMOUNT);
        assertThat(testReconcileSummary.getSystemCount()).isEqualTo(UPDATED_SYSTEM_COUNT);
        assertThat(testReconcileSummary.getGatewayCount()).isEqualTo(UPDATED_GATEWAY_COUNT);
        assertThat(testReconcileSummary.getDate()).isEqualTo(UPDATED_DATE);
        assertThat(testReconcileSummary.getStatus()).isEqualTo(UPDATED_STATUS);
        assertThat(testReconcileSummary.getSuccess()).isEqualTo(UPDATED_SUCCESS);
        assertThat(testReconcileSummary.getPeriod()).isEqualTo(UPDATED_PERIOD);
        assertThat(testReconcileSummary.getGatewayName()).isEqualTo(UPDATED_GATEWAY_NAME);
        assertThat(testReconcileSummary.getType()).isEqualTo(UPDATED_TYPE);
    }

    @Test
    @Transactional
    void patchNonExistingReconcileSummary() throws Exception {
        int databaseSizeBeforeUpdate = reconcileSummaryRepository.findAll().size();
        reconcileSummary.setId(count.incrementAndGet());

        // Create the ReconcileSummary
        ReconcileSummaryDTO reconcileSummaryDTO = reconcileSummaryMapper.toDto(reconcileSummary);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restReconcileSummaryMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, reconcileSummaryDTO.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(reconcileSummaryDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the ReconcileSummary in the database
        List<ReconcileSummary> reconcileSummaryList = reconcileSummaryRepository.findAll();
        assertThat(reconcileSummaryList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchReconcileSummary() throws Exception {
        int databaseSizeBeforeUpdate = reconcileSummaryRepository.findAll().size();
        reconcileSummary.setId(count.incrementAndGet());

        // Create the ReconcileSummary
        ReconcileSummaryDTO reconcileSummaryDTO = reconcileSummaryMapper.toDto(reconcileSummary);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restReconcileSummaryMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, count.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(reconcileSummaryDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the ReconcileSummary in the database
        List<ReconcileSummary> reconcileSummaryList = reconcileSummaryRepository.findAll();
        assertThat(reconcileSummaryList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamReconcileSummary() throws Exception {
        int databaseSizeBeforeUpdate = reconcileSummaryRepository.findAll().size();
        reconcileSummary.setId(count.incrementAndGet());

        // Create the ReconcileSummary
        ReconcileSummaryDTO reconcileSummaryDTO = reconcileSummaryMapper.toDto(reconcileSummary);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restReconcileSummaryMockMvc
            .perform(
                patch(ENTITY_API_URL)
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(reconcileSummaryDTO))
            )
            .andExpect(status().isMethodNotAllowed());

        // Validate the ReconcileSummary in the database
        List<ReconcileSummary> reconcileSummaryList = reconcileSummaryRepository.findAll();
        assertThat(reconcileSummaryList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteReconcileSummary() throws Exception {
        // Initialize the database
        reconcileSummaryRepository.saveAndFlush(reconcileSummary);

        int databaseSizeBeforeDelete = reconcileSummaryRepository.findAll().size();

        // Delete the reconcileSummary
        restReconcileSummaryMockMvc
            .perform(delete(ENTITY_API_URL_ID, reconcileSummary.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        List<ReconcileSummary> reconcileSummaryList = reconcileSummaryRepository.findAll();
        assertThat(reconcileSummaryList).hasSize(databaseSizeBeforeDelete - 1);
    }
}
