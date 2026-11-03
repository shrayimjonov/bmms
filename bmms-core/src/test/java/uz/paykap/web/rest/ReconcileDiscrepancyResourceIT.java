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
import uz.paykap.domain.ReconcileDiscrepancy;
import uz.paykap.domain.ReconcileSummary;
import uz.paykap.repository.ReconcileDiscrepancyRepository;
import uz.paykap.service.criteria.ReconcileDiscrepancyCriteria;
import uz.paykap.service.dto.ReconcileDiscrepancyDTO;
import uz.paykap.service.mapper.ReconcileDiscrepancyMapper;

/**
 * Integration tests for the {@link ReconcileDiscrepancyResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class ReconcileDiscrepancyResourceIT {

    private static final String DEFAULT_GATEWAY_NAME = "AAAAAAAAAA";
    private static final String UPDATED_GATEWAY_NAME = "BBBBBBBBBB";

    private static final Long DEFAULT_SYSTEM_TRANSACTION_ID = 1L;
    private static final Long UPDATED_SYSTEM_TRANSACTION_ID = 2L;
    private static final Long SMALLER_SYSTEM_TRANSACTION_ID = 1L - 1L;

    private static final String DEFAULT_GATEWAY_TRANSACTION_ID = "AAAAAAAAAA";
    private static final String UPDATED_GATEWAY_TRANSACTION_ID = "BBBBBBBBBB";

    private static final Long DEFAULT_SYSTEM_AMOUNT = 1L;
    private static final Long UPDATED_SYSTEM_AMOUNT = 2L;
    private static final Long SMALLER_SYSTEM_AMOUNT = 1L - 1L;

    private static final Long DEFAULT_GATEWAY_AMOUNT = 1L;
    private static final Long UPDATED_GATEWAY_AMOUNT = 2L;
    private static final Long SMALLER_GATEWAY_AMOUNT = 1L - 1L;

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

    private static final String ENTITY_API_URL = "/api/reconcile-discrepancies";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong count = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private ReconcileDiscrepancyRepository reconcileDiscrepancyRepository;

    @Autowired
    private ReconcileDiscrepancyMapper reconcileDiscrepancyMapper;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restReconcileDiscrepancyMockMvc;

    private ReconcileDiscrepancy reconcileDiscrepancy;

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

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static ReconcileDiscrepancy createUpdatedEntity(EntityManager em) {
        ReconcileDiscrepancy reconcileDiscrepancy = new ReconcileDiscrepancy()
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
        return reconcileDiscrepancy;
    }

    @BeforeEach
    public void initTest() {
        reconcileDiscrepancy = createEntity(em);
    }

    @Test
    @Transactional
    void createReconcileDiscrepancy() throws Exception {
        int databaseSizeBeforeCreate = reconcileDiscrepancyRepository.findAll().size();
        // Create the ReconcileDiscrepancy
        ReconcileDiscrepancyDTO reconcileDiscrepancyDTO = reconcileDiscrepancyMapper.toDto(reconcileDiscrepancy);
        restReconcileDiscrepancyMockMvc
            .perform(
                post(ENTITY_API_URL)
                    .contentType(MediaType.APPLICATION_JSON)
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
        assertThat(testReconcileDiscrepancy.getSystemStatus()).isEqualTo(DEFAULT_SYSTEM_STATUS);
        assertThat(testReconcileDiscrepancy.getGatewayStatus()).isEqualTo(DEFAULT_GATEWAY_STATUS);
        assertThat(testReconcileDiscrepancy.getSystemDate()).isEqualTo(DEFAULT_SYSTEM_DATE);
        assertThat(testReconcileDiscrepancy.getGatewayDate()).isEqualTo(DEFAULT_GATEWAY_DATE);
        assertThat(testReconcileDiscrepancy.getClosed()).isEqualTo(DEFAULT_CLOSED);
        assertThat(testReconcileDiscrepancy.getComment()).isEqualTo(DEFAULT_COMMENT);
    }

    @Test
    @Transactional
    void createReconcileDiscrepancyWithExistingId() throws Exception {
        // Create the ReconcileDiscrepancy with an existing ID
        reconcileDiscrepancy.setId(1L);
        ReconcileDiscrepancyDTO reconcileDiscrepancyDTO = reconcileDiscrepancyMapper.toDto(reconcileDiscrepancy);

        int databaseSizeBeforeCreate = reconcileDiscrepancyRepository.findAll().size();

        // An entity with an existing ID cannot be created, so this API call must fail
        restReconcileDiscrepancyMockMvc
            .perform(
                post(ENTITY_API_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(reconcileDiscrepancyDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the ReconcileDiscrepancy in the database
        List<ReconcileDiscrepancy> reconcileDiscrepancyList = reconcileDiscrepancyRepository.findAll();
        assertThat(reconcileDiscrepancyList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkGatewayNameIsRequired() throws Exception {
        int databaseSizeBeforeTest = reconcileDiscrepancyRepository.findAll().size();
        // set the field null
        reconcileDiscrepancy.setGatewayName(null);

        // Create the ReconcileDiscrepancy, which fails.
        ReconcileDiscrepancyDTO reconcileDiscrepancyDTO = reconcileDiscrepancyMapper.toDto(reconcileDiscrepancy);

        restReconcileDiscrepancyMockMvc
            .perform(
                post(ENTITY_API_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(reconcileDiscrepancyDTO))
            )
            .andExpect(status().isBadRequest());

        List<ReconcileDiscrepancy> reconcileDiscrepancyList = reconcileDiscrepancyRepository.findAll();
        assertThat(reconcileDiscrepancyList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkSystemTransactionIdIsRequired() throws Exception {
        int databaseSizeBeforeTest = reconcileDiscrepancyRepository.findAll().size();
        // set the field null
        reconcileDiscrepancy.setSystemTransactionId(null);

        // Create the ReconcileDiscrepancy, which fails.
        ReconcileDiscrepancyDTO reconcileDiscrepancyDTO = reconcileDiscrepancyMapper.toDto(reconcileDiscrepancy);

        restReconcileDiscrepancyMockMvc
            .perform(
                post(ENTITY_API_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(reconcileDiscrepancyDTO))
            )
            .andExpect(status().isBadRequest());

        List<ReconcileDiscrepancy> reconcileDiscrepancyList = reconcileDiscrepancyRepository.findAll();
        assertThat(reconcileDiscrepancyList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkGatewayTransactionIdIsRequired() throws Exception {
        int databaseSizeBeforeTest = reconcileDiscrepancyRepository.findAll().size();
        // set the field null
        reconcileDiscrepancy.setGatewayTransactionId(null);

        // Create the ReconcileDiscrepancy, which fails.
        ReconcileDiscrepancyDTO reconcileDiscrepancyDTO = reconcileDiscrepancyMapper.toDto(reconcileDiscrepancy);

        restReconcileDiscrepancyMockMvc
            .perform(
                post(ENTITY_API_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(reconcileDiscrepancyDTO))
            )
            .andExpect(status().isBadRequest());

        List<ReconcileDiscrepancy> reconcileDiscrepancyList = reconcileDiscrepancyRepository.findAll();
        assertThat(reconcileDiscrepancyList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllReconcileDiscrepancies() throws Exception {
        // Initialize the database
        reconcileDiscrepancyRepository.saveAndFlush(reconcileDiscrepancy);

        // Get all the reconcileDiscrepancyList
        restReconcileDiscrepancyMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
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
    void getReconcileDiscrepancy() throws Exception {
        // Initialize the database
        reconcileDiscrepancyRepository.saveAndFlush(reconcileDiscrepancy);

        // Get the reconcileDiscrepancy
        restReconcileDiscrepancyMockMvc
            .perform(get(ENTITY_API_URL_ID, reconcileDiscrepancy.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
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
    void getReconcileDiscrepanciesByIdFiltering() throws Exception {
        // Initialize the database
        reconcileDiscrepancyRepository.saveAndFlush(reconcileDiscrepancy);

        Long id = reconcileDiscrepancy.getId();

        defaultReconcileDiscrepancyShouldBeFound("id.equals=" + id);
        defaultReconcileDiscrepancyShouldNotBeFound("id.notEquals=" + id);

        defaultReconcileDiscrepancyShouldBeFound("id.greaterThanOrEqual=" + id);
        defaultReconcileDiscrepancyShouldNotBeFound("id.greaterThan=" + id);

        defaultReconcileDiscrepancyShouldBeFound("id.lessThanOrEqual=" + id);
        defaultReconcileDiscrepancyShouldNotBeFound("id.lessThan=" + id);
    }

    @Test
    @Transactional
    void getAllReconcileDiscrepanciesByGatewayNameIsEqualToSomething() throws Exception {
        // Initialize the database
        reconcileDiscrepancyRepository.saveAndFlush(reconcileDiscrepancy);

        // Get all the reconcileDiscrepancyList where gatewayName equals to DEFAULT_GATEWAY_NAME
        defaultReconcileDiscrepancyShouldBeFound("gatewayName.equals=" + DEFAULT_GATEWAY_NAME);

        // Get all the reconcileDiscrepancyList where gatewayName equals to UPDATED_GATEWAY_NAME
        defaultReconcileDiscrepancyShouldNotBeFound("gatewayName.equals=" + UPDATED_GATEWAY_NAME);
    }

    @Test
    @Transactional
    void getAllReconcileDiscrepanciesByGatewayNameIsNotEqualToSomething() throws Exception {
        // Initialize the database
        reconcileDiscrepancyRepository.saveAndFlush(reconcileDiscrepancy);

        // Get all the reconcileDiscrepancyList where gatewayName not equals to DEFAULT_GATEWAY_NAME
        defaultReconcileDiscrepancyShouldNotBeFound("gatewayName.notEquals=" + DEFAULT_GATEWAY_NAME);

        // Get all the reconcileDiscrepancyList where gatewayName not equals to UPDATED_GATEWAY_NAME
        defaultReconcileDiscrepancyShouldBeFound("gatewayName.notEquals=" + UPDATED_GATEWAY_NAME);
    }

    @Test
    @Transactional
    void getAllReconcileDiscrepanciesByGatewayNameIsInShouldWork() throws Exception {
        // Initialize the database
        reconcileDiscrepancyRepository.saveAndFlush(reconcileDiscrepancy);

        // Get all the reconcileDiscrepancyList where gatewayName in DEFAULT_GATEWAY_NAME or UPDATED_GATEWAY_NAME
        defaultReconcileDiscrepancyShouldBeFound("gatewayName.in=" + DEFAULT_GATEWAY_NAME + "," + UPDATED_GATEWAY_NAME);

        // Get all the reconcileDiscrepancyList where gatewayName equals to UPDATED_GATEWAY_NAME
        defaultReconcileDiscrepancyShouldNotBeFound("gatewayName.in=" + UPDATED_GATEWAY_NAME);
    }

    @Test
    @Transactional
    void getAllReconcileDiscrepanciesByGatewayNameIsNullOrNotNull() throws Exception {
        // Initialize the database
        reconcileDiscrepancyRepository.saveAndFlush(reconcileDiscrepancy);

        // Get all the reconcileDiscrepancyList where gatewayName is not null
        defaultReconcileDiscrepancyShouldBeFound("gatewayName.specified=true");

        // Get all the reconcileDiscrepancyList where gatewayName is null
        defaultReconcileDiscrepancyShouldNotBeFound("gatewayName.specified=false");
    }

    @Test
    @Transactional
    void getAllReconcileDiscrepanciesByGatewayNameContainsSomething() throws Exception {
        // Initialize the database
        reconcileDiscrepancyRepository.saveAndFlush(reconcileDiscrepancy);

        // Get all the reconcileDiscrepancyList where gatewayName contains DEFAULT_GATEWAY_NAME
        defaultReconcileDiscrepancyShouldBeFound("gatewayName.contains=" + DEFAULT_GATEWAY_NAME);

        // Get all the reconcileDiscrepancyList where gatewayName contains UPDATED_GATEWAY_NAME
        defaultReconcileDiscrepancyShouldNotBeFound("gatewayName.contains=" + UPDATED_GATEWAY_NAME);
    }

    @Test
    @Transactional
    void getAllReconcileDiscrepanciesByGatewayNameNotContainsSomething() throws Exception {
        // Initialize the database
        reconcileDiscrepancyRepository.saveAndFlush(reconcileDiscrepancy);

        // Get all the reconcileDiscrepancyList where gatewayName does not contain DEFAULT_GATEWAY_NAME
        defaultReconcileDiscrepancyShouldNotBeFound("gatewayName.doesNotContain=" + DEFAULT_GATEWAY_NAME);

        // Get all the reconcileDiscrepancyList where gatewayName does not contain UPDATED_GATEWAY_NAME
        defaultReconcileDiscrepancyShouldBeFound("gatewayName.doesNotContain=" + UPDATED_GATEWAY_NAME);
    }

    @Test
    @Transactional
    void getAllReconcileDiscrepanciesBySystemTransactionIdIsEqualToSomething() throws Exception {
        // Initialize the database
        reconcileDiscrepancyRepository.saveAndFlush(reconcileDiscrepancy);

        // Get all the reconcileDiscrepancyList where systemTransactionId equals to DEFAULT_SYSTEM_TRANSACTION_ID
        defaultReconcileDiscrepancyShouldBeFound("systemTransactionId.equals=" + DEFAULT_SYSTEM_TRANSACTION_ID);

        // Get all the reconcileDiscrepancyList where systemTransactionId equals to UPDATED_SYSTEM_TRANSACTION_ID
        defaultReconcileDiscrepancyShouldNotBeFound("systemTransactionId.equals=" + UPDATED_SYSTEM_TRANSACTION_ID);
    }

    @Test
    @Transactional
    void getAllReconcileDiscrepanciesBySystemTransactionIdIsNotEqualToSomething() throws Exception {
        // Initialize the database
        reconcileDiscrepancyRepository.saveAndFlush(reconcileDiscrepancy);

        // Get all the reconcileDiscrepancyList where systemTransactionId not equals to DEFAULT_SYSTEM_TRANSACTION_ID
        defaultReconcileDiscrepancyShouldNotBeFound("systemTransactionId.notEquals=" + DEFAULT_SYSTEM_TRANSACTION_ID);

        // Get all the reconcileDiscrepancyList where systemTransactionId not equals to UPDATED_SYSTEM_TRANSACTION_ID
        defaultReconcileDiscrepancyShouldBeFound("systemTransactionId.notEquals=" + UPDATED_SYSTEM_TRANSACTION_ID);
    }

    @Test
    @Transactional
    void getAllReconcileDiscrepanciesBySystemTransactionIdIsInShouldWork() throws Exception {
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
    void getAllReconcileDiscrepanciesBySystemTransactionIdIsNullOrNotNull() throws Exception {
        // Initialize the database
        reconcileDiscrepancyRepository.saveAndFlush(reconcileDiscrepancy);

        // Get all the reconcileDiscrepancyList where systemTransactionId is not null
        defaultReconcileDiscrepancyShouldBeFound("systemTransactionId.specified=true");

        // Get all the reconcileDiscrepancyList where systemTransactionId is null
        defaultReconcileDiscrepancyShouldNotBeFound("systemTransactionId.specified=false");
    }

    @Test
    @Transactional
    void getAllReconcileDiscrepanciesBySystemTransactionIdIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        reconcileDiscrepancyRepository.saveAndFlush(reconcileDiscrepancy);

        // Get all the reconcileDiscrepancyList where systemTransactionId is greater than or equal to DEFAULT_SYSTEM_TRANSACTION_ID
        defaultReconcileDiscrepancyShouldBeFound("systemTransactionId.greaterThanOrEqual=" + DEFAULT_SYSTEM_TRANSACTION_ID);

        // Get all the reconcileDiscrepancyList where systemTransactionId is greater than or equal to UPDATED_SYSTEM_TRANSACTION_ID
        defaultReconcileDiscrepancyShouldNotBeFound("systemTransactionId.greaterThanOrEqual=" + UPDATED_SYSTEM_TRANSACTION_ID);
    }

    @Test
    @Transactional
    void getAllReconcileDiscrepanciesBySystemTransactionIdIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        reconcileDiscrepancyRepository.saveAndFlush(reconcileDiscrepancy);

        // Get all the reconcileDiscrepancyList where systemTransactionId is less than or equal to DEFAULT_SYSTEM_TRANSACTION_ID
        defaultReconcileDiscrepancyShouldBeFound("systemTransactionId.lessThanOrEqual=" + DEFAULT_SYSTEM_TRANSACTION_ID);

        // Get all the reconcileDiscrepancyList where systemTransactionId is less than or equal to SMALLER_SYSTEM_TRANSACTION_ID
        defaultReconcileDiscrepancyShouldNotBeFound("systemTransactionId.lessThanOrEqual=" + SMALLER_SYSTEM_TRANSACTION_ID);
    }

    @Test
    @Transactional
    void getAllReconcileDiscrepanciesBySystemTransactionIdIsLessThanSomething() throws Exception {
        // Initialize the database
        reconcileDiscrepancyRepository.saveAndFlush(reconcileDiscrepancy);

        // Get all the reconcileDiscrepancyList where systemTransactionId is less than DEFAULT_SYSTEM_TRANSACTION_ID
        defaultReconcileDiscrepancyShouldNotBeFound("systemTransactionId.lessThan=" + DEFAULT_SYSTEM_TRANSACTION_ID);

        // Get all the reconcileDiscrepancyList where systemTransactionId is less than UPDATED_SYSTEM_TRANSACTION_ID
        defaultReconcileDiscrepancyShouldBeFound("systemTransactionId.lessThan=" + UPDATED_SYSTEM_TRANSACTION_ID);
    }

    @Test
    @Transactional
    void getAllReconcileDiscrepanciesBySystemTransactionIdIsGreaterThanSomething() throws Exception {
        // Initialize the database
        reconcileDiscrepancyRepository.saveAndFlush(reconcileDiscrepancy);

        // Get all the reconcileDiscrepancyList where systemTransactionId is greater than DEFAULT_SYSTEM_TRANSACTION_ID
        defaultReconcileDiscrepancyShouldNotBeFound("systemTransactionId.greaterThan=" + DEFAULT_SYSTEM_TRANSACTION_ID);

        // Get all the reconcileDiscrepancyList where systemTransactionId is greater than SMALLER_SYSTEM_TRANSACTION_ID
        defaultReconcileDiscrepancyShouldBeFound("systemTransactionId.greaterThan=" + SMALLER_SYSTEM_TRANSACTION_ID);
    }

    @Test
    @Transactional
    void getAllReconcileDiscrepanciesByGatewayTransactionIdIsEqualToSomething() throws Exception {
        // Initialize the database
        reconcileDiscrepancyRepository.saveAndFlush(reconcileDiscrepancy);

        // Get all the reconcileDiscrepancyList where gatewayTransactionId equals to DEFAULT_GATEWAY_TRANSACTION_ID
        defaultReconcileDiscrepancyShouldBeFound("gatewayTransactionId.equals=" + DEFAULT_GATEWAY_TRANSACTION_ID);

        // Get all the reconcileDiscrepancyList where gatewayTransactionId equals to UPDATED_GATEWAY_TRANSACTION_ID
        defaultReconcileDiscrepancyShouldNotBeFound("gatewayTransactionId.equals=" + UPDATED_GATEWAY_TRANSACTION_ID);
    }

    @Test
    @Transactional
    void getAllReconcileDiscrepanciesByGatewayTransactionIdIsNotEqualToSomething() throws Exception {
        // Initialize the database
        reconcileDiscrepancyRepository.saveAndFlush(reconcileDiscrepancy);

        // Get all the reconcileDiscrepancyList where gatewayTransactionId not equals to DEFAULT_GATEWAY_TRANSACTION_ID
        defaultReconcileDiscrepancyShouldNotBeFound("gatewayTransactionId.notEquals=" + DEFAULT_GATEWAY_TRANSACTION_ID);

        // Get all the reconcileDiscrepancyList where gatewayTransactionId not equals to UPDATED_GATEWAY_TRANSACTION_ID
        defaultReconcileDiscrepancyShouldBeFound("gatewayTransactionId.notEquals=" + UPDATED_GATEWAY_TRANSACTION_ID);
    }

    @Test
    @Transactional
    void getAllReconcileDiscrepanciesByGatewayTransactionIdIsInShouldWork() throws Exception {
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
    void getAllReconcileDiscrepanciesByGatewayTransactionIdIsNullOrNotNull() throws Exception {
        // Initialize the database
        reconcileDiscrepancyRepository.saveAndFlush(reconcileDiscrepancy);

        // Get all the reconcileDiscrepancyList where gatewayTransactionId is not null
        defaultReconcileDiscrepancyShouldBeFound("gatewayTransactionId.specified=true");

        // Get all the reconcileDiscrepancyList where gatewayTransactionId is null
        defaultReconcileDiscrepancyShouldNotBeFound("gatewayTransactionId.specified=false");
    }

    @Test
    @Transactional
    void getAllReconcileDiscrepanciesByGatewayTransactionIdContainsSomething() throws Exception {
        // Initialize the database
        reconcileDiscrepancyRepository.saveAndFlush(reconcileDiscrepancy);

        // Get all the reconcileDiscrepancyList where gatewayTransactionId contains DEFAULT_GATEWAY_TRANSACTION_ID
        defaultReconcileDiscrepancyShouldBeFound("gatewayTransactionId.contains=" + DEFAULT_GATEWAY_TRANSACTION_ID);

        // Get all the reconcileDiscrepancyList where gatewayTransactionId contains UPDATED_GATEWAY_TRANSACTION_ID
        defaultReconcileDiscrepancyShouldNotBeFound("gatewayTransactionId.contains=" + UPDATED_GATEWAY_TRANSACTION_ID);
    }

    @Test
    @Transactional
    void getAllReconcileDiscrepanciesByGatewayTransactionIdNotContainsSomething() throws Exception {
        // Initialize the database
        reconcileDiscrepancyRepository.saveAndFlush(reconcileDiscrepancy);

        // Get all the reconcileDiscrepancyList where gatewayTransactionId does not contain DEFAULT_GATEWAY_TRANSACTION_ID
        defaultReconcileDiscrepancyShouldNotBeFound("gatewayTransactionId.doesNotContain=" + DEFAULT_GATEWAY_TRANSACTION_ID);

        // Get all the reconcileDiscrepancyList where gatewayTransactionId does not contain UPDATED_GATEWAY_TRANSACTION_ID
        defaultReconcileDiscrepancyShouldBeFound("gatewayTransactionId.doesNotContain=" + UPDATED_GATEWAY_TRANSACTION_ID);
    }

    @Test
    @Transactional
    void getAllReconcileDiscrepanciesBySystemAmountIsEqualToSomething() throws Exception {
        // Initialize the database
        reconcileDiscrepancyRepository.saveAndFlush(reconcileDiscrepancy);

        // Get all the reconcileDiscrepancyList where systemAmount equals to DEFAULT_SYSTEM_AMOUNT
        defaultReconcileDiscrepancyShouldBeFound("systemAmount.equals=" + DEFAULT_SYSTEM_AMOUNT);

        // Get all the reconcileDiscrepancyList where systemAmount equals to UPDATED_SYSTEM_AMOUNT
        defaultReconcileDiscrepancyShouldNotBeFound("systemAmount.equals=" + UPDATED_SYSTEM_AMOUNT);
    }

    @Test
    @Transactional
    void getAllReconcileDiscrepanciesBySystemAmountIsNotEqualToSomething() throws Exception {
        // Initialize the database
        reconcileDiscrepancyRepository.saveAndFlush(reconcileDiscrepancy);

        // Get all the reconcileDiscrepancyList where systemAmount not equals to DEFAULT_SYSTEM_AMOUNT
        defaultReconcileDiscrepancyShouldNotBeFound("systemAmount.notEquals=" + DEFAULT_SYSTEM_AMOUNT);

        // Get all the reconcileDiscrepancyList where systemAmount not equals to UPDATED_SYSTEM_AMOUNT
        defaultReconcileDiscrepancyShouldBeFound("systemAmount.notEquals=" + UPDATED_SYSTEM_AMOUNT);
    }

    @Test
    @Transactional
    void getAllReconcileDiscrepanciesBySystemAmountIsInShouldWork() throws Exception {
        // Initialize the database
        reconcileDiscrepancyRepository.saveAndFlush(reconcileDiscrepancy);

        // Get all the reconcileDiscrepancyList where systemAmount in DEFAULT_SYSTEM_AMOUNT or UPDATED_SYSTEM_AMOUNT
        defaultReconcileDiscrepancyShouldBeFound("systemAmount.in=" + DEFAULT_SYSTEM_AMOUNT + "," + UPDATED_SYSTEM_AMOUNT);

        // Get all the reconcileDiscrepancyList where systemAmount equals to UPDATED_SYSTEM_AMOUNT
        defaultReconcileDiscrepancyShouldNotBeFound("systemAmount.in=" + UPDATED_SYSTEM_AMOUNT);
    }

    @Test
    @Transactional
    void getAllReconcileDiscrepanciesBySystemAmountIsNullOrNotNull() throws Exception {
        // Initialize the database
        reconcileDiscrepancyRepository.saveAndFlush(reconcileDiscrepancy);

        // Get all the reconcileDiscrepancyList where systemAmount is not null
        defaultReconcileDiscrepancyShouldBeFound("systemAmount.specified=true");

        // Get all the reconcileDiscrepancyList where systemAmount is null
        defaultReconcileDiscrepancyShouldNotBeFound("systemAmount.specified=false");
    }

    @Test
    @Transactional
    void getAllReconcileDiscrepanciesBySystemAmountIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        reconcileDiscrepancyRepository.saveAndFlush(reconcileDiscrepancy);

        // Get all the reconcileDiscrepancyList where systemAmount is greater than or equal to DEFAULT_SYSTEM_AMOUNT
        defaultReconcileDiscrepancyShouldBeFound("systemAmount.greaterThanOrEqual=" + DEFAULT_SYSTEM_AMOUNT);

        // Get all the reconcileDiscrepancyList where systemAmount is greater than or equal to UPDATED_SYSTEM_AMOUNT
        defaultReconcileDiscrepancyShouldNotBeFound("systemAmount.greaterThanOrEqual=" + UPDATED_SYSTEM_AMOUNT);
    }

    @Test
    @Transactional
    void getAllReconcileDiscrepanciesBySystemAmountIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        reconcileDiscrepancyRepository.saveAndFlush(reconcileDiscrepancy);

        // Get all the reconcileDiscrepancyList where systemAmount is less than or equal to DEFAULT_SYSTEM_AMOUNT
        defaultReconcileDiscrepancyShouldBeFound("systemAmount.lessThanOrEqual=" + DEFAULT_SYSTEM_AMOUNT);

        // Get all the reconcileDiscrepancyList where systemAmount is less than or equal to SMALLER_SYSTEM_AMOUNT
        defaultReconcileDiscrepancyShouldNotBeFound("systemAmount.lessThanOrEqual=" + SMALLER_SYSTEM_AMOUNT);
    }

    @Test
    @Transactional
    void getAllReconcileDiscrepanciesBySystemAmountIsLessThanSomething() throws Exception {
        // Initialize the database
        reconcileDiscrepancyRepository.saveAndFlush(reconcileDiscrepancy);

        // Get all the reconcileDiscrepancyList where systemAmount is less than DEFAULT_SYSTEM_AMOUNT
        defaultReconcileDiscrepancyShouldNotBeFound("systemAmount.lessThan=" + DEFAULT_SYSTEM_AMOUNT);

        // Get all the reconcileDiscrepancyList where systemAmount is less than UPDATED_SYSTEM_AMOUNT
        defaultReconcileDiscrepancyShouldBeFound("systemAmount.lessThan=" + UPDATED_SYSTEM_AMOUNT);
    }

    @Test
    @Transactional
    void getAllReconcileDiscrepanciesBySystemAmountIsGreaterThanSomething() throws Exception {
        // Initialize the database
        reconcileDiscrepancyRepository.saveAndFlush(reconcileDiscrepancy);

        // Get all the reconcileDiscrepancyList where systemAmount is greater than DEFAULT_SYSTEM_AMOUNT
        defaultReconcileDiscrepancyShouldNotBeFound("systemAmount.greaterThan=" + DEFAULT_SYSTEM_AMOUNT);

        // Get all the reconcileDiscrepancyList where systemAmount is greater than SMALLER_SYSTEM_AMOUNT
        defaultReconcileDiscrepancyShouldBeFound("systemAmount.greaterThan=" + SMALLER_SYSTEM_AMOUNT);
    }

    @Test
    @Transactional
    void getAllReconcileDiscrepanciesByGatewayAmountIsEqualToSomething() throws Exception {
        // Initialize the database
        reconcileDiscrepancyRepository.saveAndFlush(reconcileDiscrepancy);

        // Get all the reconcileDiscrepancyList where gatewayAmount equals to DEFAULT_GATEWAY_AMOUNT
        defaultReconcileDiscrepancyShouldBeFound("gatewayAmount.equals=" + DEFAULT_GATEWAY_AMOUNT);

        // Get all the reconcileDiscrepancyList where gatewayAmount equals to UPDATED_GATEWAY_AMOUNT
        defaultReconcileDiscrepancyShouldNotBeFound("gatewayAmount.equals=" + UPDATED_GATEWAY_AMOUNT);
    }

    @Test
    @Transactional
    void getAllReconcileDiscrepanciesByGatewayAmountIsNotEqualToSomething() throws Exception {
        // Initialize the database
        reconcileDiscrepancyRepository.saveAndFlush(reconcileDiscrepancy);

        // Get all the reconcileDiscrepancyList where gatewayAmount not equals to DEFAULT_GATEWAY_AMOUNT
        defaultReconcileDiscrepancyShouldNotBeFound("gatewayAmount.notEquals=" + DEFAULT_GATEWAY_AMOUNT);

        // Get all the reconcileDiscrepancyList where gatewayAmount not equals to UPDATED_GATEWAY_AMOUNT
        defaultReconcileDiscrepancyShouldBeFound("gatewayAmount.notEquals=" + UPDATED_GATEWAY_AMOUNT);
    }

    @Test
    @Transactional
    void getAllReconcileDiscrepanciesByGatewayAmountIsInShouldWork() throws Exception {
        // Initialize the database
        reconcileDiscrepancyRepository.saveAndFlush(reconcileDiscrepancy);

        // Get all the reconcileDiscrepancyList where gatewayAmount in DEFAULT_GATEWAY_AMOUNT or UPDATED_GATEWAY_AMOUNT
        defaultReconcileDiscrepancyShouldBeFound("gatewayAmount.in=" + DEFAULT_GATEWAY_AMOUNT + "," + UPDATED_GATEWAY_AMOUNT);

        // Get all the reconcileDiscrepancyList where gatewayAmount equals to UPDATED_GATEWAY_AMOUNT
        defaultReconcileDiscrepancyShouldNotBeFound("gatewayAmount.in=" + UPDATED_GATEWAY_AMOUNT);
    }

    @Test
    @Transactional
    void getAllReconcileDiscrepanciesByGatewayAmountIsNullOrNotNull() throws Exception {
        // Initialize the database
        reconcileDiscrepancyRepository.saveAndFlush(reconcileDiscrepancy);

        // Get all the reconcileDiscrepancyList where gatewayAmount is not null
        defaultReconcileDiscrepancyShouldBeFound("gatewayAmount.specified=true");

        // Get all the reconcileDiscrepancyList where gatewayAmount is null
        defaultReconcileDiscrepancyShouldNotBeFound("gatewayAmount.specified=false");
    }

    @Test
    @Transactional
    void getAllReconcileDiscrepanciesByGatewayAmountIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        reconcileDiscrepancyRepository.saveAndFlush(reconcileDiscrepancy);

        // Get all the reconcileDiscrepancyList where gatewayAmount is greater than or equal to DEFAULT_GATEWAY_AMOUNT
        defaultReconcileDiscrepancyShouldBeFound("gatewayAmount.greaterThanOrEqual=" + DEFAULT_GATEWAY_AMOUNT);

        // Get all the reconcileDiscrepancyList where gatewayAmount is greater than or equal to UPDATED_GATEWAY_AMOUNT
        defaultReconcileDiscrepancyShouldNotBeFound("gatewayAmount.greaterThanOrEqual=" + UPDATED_GATEWAY_AMOUNT);
    }

    @Test
    @Transactional
    void getAllReconcileDiscrepanciesByGatewayAmountIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        reconcileDiscrepancyRepository.saveAndFlush(reconcileDiscrepancy);

        // Get all the reconcileDiscrepancyList where gatewayAmount is less than or equal to DEFAULT_GATEWAY_AMOUNT
        defaultReconcileDiscrepancyShouldBeFound("gatewayAmount.lessThanOrEqual=" + DEFAULT_GATEWAY_AMOUNT);

        // Get all the reconcileDiscrepancyList where gatewayAmount is less than or equal to SMALLER_GATEWAY_AMOUNT
        defaultReconcileDiscrepancyShouldNotBeFound("gatewayAmount.lessThanOrEqual=" + SMALLER_GATEWAY_AMOUNT);
    }

    @Test
    @Transactional
    void getAllReconcileDiscrepanciesByGatewayAmountIsLessThanSomething() throws Exception {
        // Initialize the database
        reconcileDiscrepancyRepository.saveAndFlush(reconcileDiscrepancy);

        // Get all the reconcileDiscrepancyList where gatewayAmount is less than DEFAULT_GATEWAY_AMOUNT
        defaultReconcileDiscrepancyShouldNotBeFound("gatewayAmount.lessThan=" + DEFAULT_GATEWAY_AMOUNT);

        // Get all the reconcileDiscrepancyList where gatewayAmount is less than UPDATED_GATEWAY_AMOUNT
        defaultReconcileDiscrepancyShouldBeFound("gatewayAmount.lessThan=" + UPDATED_GATEWAY_AMOUNT);
    }

    @Test
    @Transactional
    void getAllReconcileDiscrepanciesByGatewayAmountIsGreaterThanSomething() throws Exception {
        // Initialize the database
        reconcileDiscrepancyRepository.saveAndFlush(reconcileDiscrepancy);

        // Get all the reconcileDiscrepancyList where gatewayAmount is greater than DEFAULT_GATEWAY_AMOUNT
        defaultReconcileDiscrepancyShouldNotBeFound("gatewayAmount.greaterThan=" + DEFAULT_GATEWAY_AMOUNT);

        // Get all the reconcileDiscrepancyList where gatewayAmount is greater than SMALLER_GATEWAY_AMOUNT
        defaultReconcileDiscrepancyShouldBeFound("gatewayAmount.greaterThan=" + SMALLER_GATEWAY_AMOUNT);
    }

    @Test
    @Transactional
    void getAllReconcileDiscrepanciesBySystemStatusIsEqualToSomething() throws Exception {
        // Initialize the database
        reconcileDiscrepancyRepository.saveAndFlush(reconcileDiscrepancy);

        // Get all the reconcileDiscrepancyList where systemStatus equals to DEFAULT_SYSTEM_STATUS
        defaultReconcileDiscrepancyShouldBeFound("systemStatus.equals=" + DEFAULT_SYSTEM_STATUS);

        // Get all the reconcileDiscrepancyList where systemStatus equals to UPDATED_SYSTEM_STATUS
        defaultReconcileDiscrepancyShouldNotBeFound("systemStatus.equals=" + UPDATED_SYSTEM_STATUS);
    }

    @Test
    @Transactional
    void getAllReconcileDiscrepanciesBySystemStatusIsNotEqualToSomething() throws Exception {
        // Initialize the database
        reconcileDiscrepancyRepository.saveAndFlush(reconcileDiscrepancy);

        // Get all the reconcileDiscrepancyList where systemStatus not equals to DEFAULT_SYSTEM_STATUS
        defaultReconcileDiscrepancyShouldNotBeFound("systemStatus.notEquals=" + DEFAULT_SYSTEM_STATUS);

        // Get all the reconcileDiscrepancyList where systemStatus not equals to UPDATED_SYSTEM_STATUS
        defaultReconcileDiscrepancyShouldBeFound("systemStatus.notEquals=" + UPDATED_SYSTEM_STATUS);
    }

    @Test
    @Transactional
    void getAllReconcileDiscrepanciesBySystemStatusIsInShouldWork() throws Exception {
        // Initialize the database
        reconcileDiscrepancyRepository.saveAndFlush(reconcileDiscrepancy);

        // Get all the reconcileDiscrepancyList where systemStatus in DEFAULT_SYSTEM_STATUS or UPDATED_SYSTEM_STATUS
        defaultReconcileDiscrepancyShouldBeFound("systemStatus.in=" + DEFAULT_SYSTEM_STATUS + "," + UPDATED_SYSTEM_STATUS);

        // Get all the reconcileDiscrepancyList where systemStatus equals to UPDATED_SYSTEM_STATUS
        defaultReconcileDiscrepancyShouldNotBeFound("systemStatus.in=" + UPDATED_SYSTEM_STATUS);
    }

    @Test
    @Transactional
    void getAllReconcileDiscrepanciesBySystemStatusIsNullOrNotNull() throws Exception {
        // Initialize the database
        reconcileDiscrepancyRepository.saveAndFlush(reconcileDiscrepancy);

        // Get all the reconcileDiscrepancyList where systemStatus is not null
        defaultReconcileDiscrepancyShouldBeFound("systemStatus.specified=true");

        // Get all the reconcileDiscrepancyList where systemStatus is null
        defaultReconcileDiscrepancyShouldNotBeFound("systemStatus.specified=false");
    }

    @Test
    @Transactional
    void getAllReconcileDiscrepanciesByGatewayStatusIsEqualToSomething() throws Exception {
        // Initialize the database
        reconcileDiscrepancyRepository.saveAndFlush(reconcileDiscrepancy);

        // Get all the reconcileDiscrepancyList where gatewayStatus equals to DEFAULT_GATEWAY_STATUS
        defaultReconcileDiscrepancyShouldBeFound("gatewayStatus.equals=" + DEFAULT_GATEWAY_STATUS);

        // Get all the reconcileDiscrepancyList where gatewayStatus equals to UPDATED_GATEWAY_STATUS
        defaultReconcileDiscrepancyShouldNotBeFound("gatewayStatus.equals=" + UPDATED_GATEWAY_STATUS);
    }

    @Test
    @Transactional
    void getAllReconcileDiscrepanciesByGatewayStatusIsNotEqualToSomething() throws Exception {
        // Initialize the database
        reconcileDiscrepancyRepository.saveAndFlush(reconcileDiscrepancy);

        // Get all the reconcileDiscrepancyList where gatewayStatus not equals to DEFAULT_GATEWAY_STATUS
        defaultReconcileDiscrepancyShouldNotBeFound("gatewayStatus.notEquals=" + DEFAULT_GATEWAY_STATUS);

        // Get all the reconcileDiscrepancyList where gatewayStatus not equals to UPDATED_GATEWAY_STATUS
        defaultReconcileDiscrepancyShouldBeFound("gatewayStatus.notEquals=" + UPDATED_GATEWAY_STATUS);
    }

    @Test
    @Transactional
    void getAllReconcileDiscrepanciesByGatewayStatusIsInShouldWork() throws Exception {
        // Initialize the database
        reconcileDiscrepancyRepository.saveAndFlush(reconcileDiscrepancy);

        // Get all the reconcileDiscrepancyList where gatewayStatus in DEFAULT_GATEWAY_STATUS or UPDATED_GATEWAY_STATUS
        defaultReconcileDiscrepancyShouldBeFound("gatewayStatus.in=" + DEFAULT_GATEWAY_STATUS + "," + UPDATED_GATEWAY_STATUS);

        // Get all the reconcileDiscrepancyList where gatewayStatus equals to UPDATED_GATEWAY_STATUS
        defaultReconcileDiscrepancyShouldNotBeFound("gatewayStatus.in=" + UPDATED_GATEWAY_STATUS);
    }

    @Test
    @Transactional
    void getAllReconcileDiscrepanciesByGatewayStatusIsNullOrNotNull() throws Exception {
        // Initialize the database
        reconcileDiscrepancyRepository.saveAndFlush(reconcileDiscrepancy);

        // Get all the reconcileDiscrepancyList where gatewayStatus is not null
        defaultReconcileDiscrepancyShouldBeFound("gatewayStatus.specified=true");

        // Get all the reconcileDiscrepancyList where gatewayStatus is null
        defaultReconcileDiscrepancyShouldNotBeFound("gatewayStatus.specified=false");
    }

    @Test
    @Transactional
    void getAllReconcileDiscrepanciesBySystemDateIsEqualToSomething() throws Exception {
        // Initialize the database
        reconcileDiscrepancyRepository.saveAndFlush(reconcileDiscrepancy);

        // Get all the reconcileDiscrepancyList where systemDate equals to DEFAULT_SYSTEM_DATE
        defaultReconcileDiscrepancyShouldBeFound("systemDate.equals=" + DEFAULT_SYSTEM_DATE);

        // Get all the reconcileDiscrepancyList where systemDate equals to UPDATED_SYSTEM_DATE
        defaultReconcileDiscrepancyShouldNotBeFound("systemDate.equals=" + UPDATED_SYSTEM_DATE);
    }

    @Test
    @Transactional
    void getAllReconcileDiscrepanciesBySystemDateIsNotEqualToSomething() throws Exception {
        // Initialize the database
        reconcileDiscrepancyRepository.saveAndFlush(reconcileDiscrepancy);

        // Get all the reconcileDiscrepancyList where systemDate not equals to DEFAULT_SYSTEM_DATE
        defaultReconcileDiscrepancyShouldNotBeFound("systemDate.notEquals=" + DEFAULT_SYSTEM_DATE);

        // Get all the reconcileDiscrepancyList where systemDate not equals to UPDATED_SYSTEM_DATE
        defaultReconcileDiscrepancyShouldBeFound("systemDate.notEquals=" + UPDATED_SYSTEM_DATE);
    }

    @Test
    @Transactional
    void getAllReconcileDiscrepanciesBySystemDateIsInShouldWork() throws Exception {
        // Initialize the database
        reconcileDiscrepancyRepository.saveAndFlush(reconcileDiscrepancy);

        // Get all the reconcileDiscrepancyList where systemDate in DEFAULT_SYSTEM_DATE or UPDATED_SYSTEM_DATE
        defaultReconcileDiscrepancyShouldBeFound("systemDate.in=" + DEFAULT_SYSTEM_DATE + "," + UPDATED_SYSTEM_DATE);

        // Get all the reconcileDiscrepancyList where systemDate equals to UPDATED_SYSTEM_DATE
        defaultReconcileDiscrepancyShouldNotBeFound("systemDate.in=" + UPDATED_SYSTEM_DATE);
    }

    @Test
    @Transactional
    void getAllReconcileDiscrepanciesBySystemDateIsNullOrNotNull() throws Exception {
        // Initialize the database
        reconcileDiscrepancyRepository.saveAndFlush(reconcileDiscrepancy);

        // Get all the reconcileDiscrepancyList where systemDate is not null
        defaultReconcileDiscrepancyShouldBeFound("systemDate.specified=true");

        // Get all the reconcileDiscrepancyList where systemDate is null
        defaultReconcileDiscrepancyShouldNotBeFound("systemDate.specified=false");
    }

    @Test
    @Transactional
    void getAllReconcileDiscrepanciesByGatewayDateIsEqualToSomething() throws Exception {
        // Initialize the database
        reconcileDiscrepancyRepository.saveAndFlush(reconcileDiscrepancy);

        // Get all the reconcileDiscrepancyList where gatewayDate equals to DEFAULT_GATEWAY_DATE
        defaultReconcileDiscrepancyShouldBeFound("gatewayDate.equals=" + DEFAULT_GATEWAY_DATE);

        // Get all the reconcileDiscrepancyList where gatewayDate equals to UPDATED_GATEWAY_DATE
        defaultReconcileDiscrepancyShouldNotBeFound("gatewayDate.equals=" + UPDATED_GATEWAY_DATE);
    }

    @Test
    @Transactional
    void getAllReconcileDiscrepanciesByGatewayDateIsNotEqualToSomething() throws Exception {
        // Initialize the database
        reconcileDiscrepancyRepository.saveAndFlush(reconcileDiscrepancy);

        // Get all the reconcileDiscrepancyList where gatewayDate not equals to DEFAULT_GATEWAY_DATE
        defaultReconcileDiscrepancyShouldNotBeFound("gatewayDate.notEquals=" + DEFAULT_GATEWAY_DATE);

        // Get all the reconcileDiscrepancyList where gatewayDate not equals to UPDATED_GATEWAY_DATE
        defaultReconcileDiscrepancyShouldBeFound("gatewayDate.notEquals=" + UPDATED_GATEWAY_DATE);
    }

    @Test
    @Transactional
    void getAllReconcileDiscrepanciesByGatewayDateIsInShouldWork() throws Exception {
        // Initialize the database
        reconcileDiscrepancyRepository.saveAndFlush(reconcileDiscrepancy);

        // Get all the reconcileDiscrepancyList where gatewayDate in DEFAULT_GATEWAY_DATE or UPDATED_GATEWAY_DATE
        defaultReconcileDiscrepancyShouldBeFound("gatewayDate.in=" + DEFAULT_GATEWAY_DATE + "," + UPDATED_GATEWAY_DATE);

        // Get all the reconcileDiscrepancyList where gatewayDate equals to UPDATED_GATEWAY_DATE
        defaultReconcileDiscrepancyShouldNotBeFound("gatewayDate.in=" + UPDATED_GATEWAY_DATE);
    }

    @Test
    @Transactional
    void getAllReconcileDiscrepanciesByGatewayDateIsNullOrNotNull() throws Exception {
        // Initialize the database
        reconcileDiscrepancyRepository.saveAndFlush(reconcileDiscrepancy);

        // Get all the reconcileDiscrepancyList where gatewayDate is not null
        defaultReconcileDiscrepancyShouldBeFound("gatewayDate.specified=true");

        // Get all the reconcileDiscrepancyList where gatewayDate is null
        defaultReconcileDiscrepancyShouldNotBeFound("gatewayDate.specified=false");
    }

    @Test
    @Transactional
    void getAllReconcileDiscrepanciesByClosedIsEqualToSomething() throws Exception {
        // Initialize the database
        reconcileDiscrepancyRepository.saveAndFlush(reconcileDiscrepancy);

        // Get all the reconcileDiscrepancyList where closed equals to DEFAULT_CLOSED
        defaultReconcileDiscrepancyShouldBeFound("closed.equals=" + DEFAULT_CLOSED);

        // Get all the reconcileDiscrepancyList where closed equals to UPDATED_CLOSED
        defaultReconcileDiscrepancyShouldNotBeFound("closed.equals=" + UPDATED_CLOSED);
    }

    @Test
    @Transactional
    void getAllReconcileDiscrepanciesByClosedIsNotEqualToSomething() throws Exception {
        // Initialize the database
        reconcileDiscrepancyRepository.saveAndFlush(reconcileDiscrepancy);

        // Get all the reconcileDiscrepancyList where closed not equals to DEFAULT_CLOSED
        defaultReconcileDiscrepancyShouldNotBeFound("closed.notEquals=" + DEFAULT_CLOSED);

        // Get all the reconcileDiscrepancyList where closed not equals to UPDATED_CLOSED
        defaultReconcileDiscrepancyShouldBeFound("closed.notEquals=" + UPDATED_CLOSED);
    }

    @Test
    @Transactional
    void getAllReconcileDiscrepanciesByClosedIsInShouldWork() throws Exception {
        // Initialize the database
        reconcileDiscrepancyRepository.saveAndFlush(reconcileDiscrepancy);

        // Get all the reconcileDiscrepancyList where closed in DEFAULT_CLOSED or UPDATED_CLOSED
        defaultReconcileDiscrepancyShouldBeFound("closed.in=" + DEFAULT_CLOSED + "," + UPDATED_CLOSED);

        // Get all the reconcileDiscrepancyList where closed equals to UPDATED_CLOSED
        defaultReconcileDiscrepancyShouldNotBeFound("closed.in=" + UPDATED_CLOSED);
    }

    @Test
    @Transactional
    void getAllReconcileDiscrepanciesByClosedIsNullOrNotNull() throws Exception {
        // Initialize the database
        reconcileDiscrepancyRepository.saveAndFlush(reconcileDiscrepancy);

        // Get all the reconcileDiscrepancyList where closed is not null
        defaultReconcileDiscrepancyShouldBeFound("closed.specified=true");

        // Get all the reconcileDiscrepancyList where closed is null
        defaultReconcileDiscrepancyShouldNotBeFound("closed.specified=false");
    }

    @Test
    @Transactional
    void getAllReconcileDiscrepanciesByCommentIsEqualToSomething() throws Exception {
        // Initialize the database
        reconcileDiscrepancyRepository.saveAndFlush(reconcileDiscrepancy);

        // Get all the reconcileDiscrepancyList where comment equals to DEFAULT_COMMENT
        defaultReconcileDiscrepancyShouldBeFound("comment.equals=" + DEFAULT_COMMENT);

        // Get all the reconcileDiscrepancyList where comment equals to UPDATED_COMMENT
        defaultReconcileDiscrepancyShouldNotBeFound("comment.equals=" + UPDATED_COMMENT);
    }

    @Test
    @Transactional
    void getAllReconcileDiscrepanciesByCommentIsNotEqualToSomething() throws Exception {
        // Initialize the database
        reconcileDiscrepancyRepository.saveAndFlush(reconcileDiscrepancy);

        // Get all the reconcileDiscrepancyList where comment not equals to DEFAULT_COMMENT
        defaultReconcileDiscrepancyShouldNotBeFound("comment.notEquals=" + DEFAULT_COMMENT);

        // Get all the reconcileDiscrepancyList where comment not equals to UPDATED_COMMENT
        defaultReconcileDiscrepancyShouldBeFound("comment.notEquals=" + UPDATED_COMMENT);
    }

    @Test
    @Transactional
    void getAllReconcileDiscrepanciesByCommentIsInShouldWork() throws Exception {
        // Initialize the database
        reconcileDiscrepancyRepository.saveAndFlush(reconcileDiscrepancy);

        // Get all the reconcileDiscrepancyList where comment in DEFAULT_COMMENT or UPDATED_COMMENT
        defaultReconcileDiscrepancyShouldBeFound("comment.in=" + DEFAULT_COMMENT + "," + UPDATED_COMMENT);

        // Get all the reconcileDiscrepancyList where comment equals to UPDATED_COMMENT
        defaultReconcileDiscrepancyShouldNotBeFound("comment.in=" + UPDATED_COMMENT);
    }

    @Test
    @Transactional
    void getAllReconcileDiscrepanciesByCommentIsNullOrNotNull() throws Exception {
        // Initialize the database
        reconcileDiscrepancyRepository.saveAndFlush(reconcileDiscrepancy);

        // Get all the reconcileDiscrepancyList where comment is not null
        defaultReconcileDiscrepancyShouldBeFound("comment.specified=true");

        // Get all the reconcileDiscrepancyList where comment is null
        defaultReconcileDiscrepancyShouldNotBeFound("comment.specified=false");
    }

    @Test
    @Transactional
    void getAllReconcileDiscrepanciesByCommentContainsSomething() throws Exception {
        // Initialize the database
        reconcileDiscrepancyRepository.saveAndFlush(reconcileDiscrepancy);

        // Get all the reconcileDiscrepancyList where comment contains DEFAULT_COMMENT
        defaultReconcileDiscrepancyShouldBeFound("comment.contains=" + DEFAULT_COMMENT);

        // Get all the reconcileDiscrepancyList where comment contains UPDATED_COMMENT
        defaultReconcileDiscrepancyShouldNotBeFound("comment.contains=" + UPDATED_COMMENT);
    }

    @Test
    @Transactional
    void getAllReconcileDiscrepanciesByCommentNotContainsSomething() throws Exception {
        // Initialize the database
        reconcileDiscrepancyRepository.saveAndFlush(reconcileDiscrepancy);

        // Get all the reconcileDiscrepancyList where comment does not contain DEFAULT_COMMENT
        defaultReconcileDiscrepancyShouldNotBeFound("comment.doesNotContain=" + DEFAULT_COMMENT);

        // Get all the reconcileDiscrepancyList where comment does not contain UPDATED_COMMENT
        defaultReconcileDiscrepancyShouldBeFound("comment.doesNotContain=" + UPDATED_COMMENT);
    }

    @Test
    @Transactional
    void getAllReconcileDiscrepanciesByReconcileSummaryIsEqualToSomething() throws Exception {
        // Initialize the database
        reconcileDiscrepancyRepository.saveAndFlush(reconcileDiscrepancy);
        ReconcileSummary reconcileSummary = ReconcileSummaryResourceIT.createEntity(em);
        em.persist(reconcileSummary);
        em.flush();
        reconcileDiscrepancy.setReconcileSummary(reconcileSummary);
        reconcileDiscrepancyRepository.saveAndFlush(reconcileDiscrepancy);
        Long reconcileSummaryId = reconcileSummary.getId();

        // Get all the reconcileDiscrepancyList where reconcileSummary equals to reconcileSummaryId
        defaultReconcileDiscrepancyShouldBeFound("reconcileSummaryId.equals=" + reconcileSummaryId);

        // Get all the reconcileDiscrepancyList where reconcileSummary equals to (reconcileSummaryId + 1)
        defaultReconcileDiscrepancyShouldNotBeFound("reconcileSummaryId.equals=" + (reconcileSummaryId + 1));
    }

    /**
     * Executes the search, and checks that the default entity is returned.
     */
    private void defaultReconcileDiscrepancyShouldBeFound(String filter) throws Exception {
        restReconcileDiscrepancyMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
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
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("1"));
    }

    /**
     * Executes the search, and checks that the default entity is not returned.
     */
    private void defaultReconcileDiscrepancyShouldNotBeFound(String filter) throws Exception {
        restReconcileDiscrepancyMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty());

        // Check, that the count call also returns 0
        restReconcileDiscrepancyMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("0"));
    }

    @Test
    @Transactional
    void getNonExistingReconcileDiscrepancy() throws Exception {
        // Get the reconcileDiscrepancy
        restReconcileDiscrepancyMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putNewReconcileDiscrepancy() throws Exception {
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
                put(ENTITY_API_URL_ID, reconcileDiscrepancyDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
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
        assertThat(testReconcileDiscrepancy.getSystemStatus()).isEqualTo(UPDATED_SYSTEM_STATUS);
        assertThat(testReconcileDiscrepancy.getGatewayStatus()).isEqualTo(UPDATED_GATEWAY_STATUS);
        assertThat(testReconcileDiscrepancy.getSystemDate()).isEqualTo(UPDATED_SYSTEM_DATE);
        assertThat(testReconcileDiscrepancy.getGatewayDate()).isEqualTo(UPDATED_GATEWAY_DATE);
        assertThat(testReconcileDiscrepancy.getClosed()).isEqualTo(UPDATED_CLOSED);
        assertThat(testReconcileDiscrepancy.getComment()).isEqualTo(UPDATED_COMMENT);
    }

    @Test
    @Transactional
    void putNonExistingReconcileDiscrepancy() throws Exception {
        int databaseSizeBeforeUpdate = reconcileDiscrepancyRepository.findAll().size();
        reconcileDiscrepancy.setId(count.incrementAndGet());

        // Create the ReconcileDiscrepancy
        ReconcileDiscrepancyDTO reconcileDiscrepancyDTO = reconcileDiscrepancyMapper.toDto(reconcileDiscrepancy);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restReconcileDiscrepancyMockMvc
            .perform(
                put(ENTITY_API_URL_ID, reconcileDiscrepancyDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(reconcileDiscrepancyDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the ReconcileDiscrepancy in the database
        List<ReconcileDiscrepancy> reconcileDiscrepancyList = reconcileDiscrepancyRepository.findAll();
        assertThat(reconcileDiscrepancyList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchReconcileDiscrepancy() throws Exception {
        int databaseSizeBeforeUpdate = reconcileDiscrepancyRepository.findAll().size();
        reconcileDiscrepancy.setId(count.incrementAndGet());

        // Create the ReconcileDiscrepancy
        ReconcileDiscrepancyDTO reconcileDiscrepancyDTO = reconcileDiscrepancyMapper.toDto(reconcileDiscrepancy);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restReconcileDiscrepancyMockMvc
            .perform(
                put(ENTITY_API_URL_ID, count.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(reconcileDiscrepancyDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the ReconcileDiscrepancy in the database
        List<ReconcileDiscrepancy> reconcileDiscrepancyList = reconcileDiscrepancyRepository.findAll();
        assertThat(reconcileDiscrepancyList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamReconcileDiscrepancy() throws Exception {
        int databaseSizeBeforeUpdate = reconcileDiscrepancyRepository.findAll().size();
        reconcileDiscrepancy.setId(count.incrementAndGet());

        // Create the ReconcileDiscrepancy
        ReconcileDiscrepancyDTO reconcileDiscrepancyDTO = reconcileDiscrepancyMapper.toDto(reconcileDiscrepancy);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restReconcileDiscrepancyMockMvc
            .perform(
                put(ENTITY_API_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(reconcileDiscrepancyDTO))
            )
            .andExpect(status().isMethodNotAllowed());

        // Validate the ReconcileDiscrepancy in the database
        List<ReconcileDiscrepancy> reconcileDiscrepancyList = reconcileDiscrepancyRepository.findAll();
        assertThat(reconcileDiscrepancyList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateReconcileDiscrepancyWithPatch() throws Exception {
        // Initialize the database
        reconcileDiscrepancyRepository.saveAndFlush(reconcileDiscrepancy);

        int databaseSizeBeforeUpdate = reconcileDiscrepancyRepository.findAll().size();

        // Update the reconcileDiscrepancy using partial update
        ReconcileDiscrepancy partialUpdatedReconcileDiscrepancy = new ReconcileDiscrepancy();
        partialUpdatedReconcileDiscrepancy.setId(reconcileDiscrepancy.getId());

        partialUpdatedReconcileDiscrepancy
            .gatewayName(UPDATED_GATEWAY_NAME)
            .systemTransactionId(UPDATED_SYSTEM_TRANSACTION_ID)
            .systemAmount(UPDATED_SYSTEM_AMOUNT)
            .closed(UPDATED_CLOSED)
            .comment(UPDATED_COMMENT);

        restReconcileDiscrepancyMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedReconcileDiscrepancy.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedReconcileDiscrepancy))
            )
            .andExpect(status().isOk());

        // Validate the ReconcileDiscrepancy in the database
        List<ReconcileDiscrepancy> reconcileDiscrepancyList = reconcileDiscrepancyRepository.findAll();
        assertThat(reconcileDiscrepancyList).hasSize(databaseSizeBeforeUpdate);
        ReconcileDiscrepancy testReconcileDiscrepancy = reconcileDiscrepancyList.get(reconcileDiscrepancyList.size() - 1);
        assertThat(testReconcileDiscrepancy.getGatewayName()).isEqualTo(UPDATED_GATEWAY_NAME);
        assertThat(testReconcileDiscrepancy.getSystemTransactionId()).isEqualTo(UPDATED_SYSTEM_TRANSACTION_ID);
        assertThat(testReconcileDiscrepancy.getGatewayTransactionId()).isEqualTo(DEFAULT_GATEWAY_TRANSACTION_ID);
        assertThat(testReconcileDiscrepancy.getSystemAmount()).isEqualTo(UPDATED_SYSTEM_AMOUNT);
        assertThat(testReconcileDiscrepancy.getGatewayAmount()).isEqualTo(DEFAULT_GATEWAY_AMOUNT);
        assertThat(testReconcileDiscrepancy.getSystemStatus()).isEqualTo(DEFAULT_SYSTEM_STATUS);
        assertThat(testReconcileDiscrepancy.getGatewayStatus()).isEqualTo(DEFAULT_GATEWAY_STATUS);
        assertThat(testReconcileDiscrepancy.getSystemDate()).isEqualTo(DEFAULT_SYSTEM_DATE);
        assertThat(testReconcileDiscrepancy.getGatewayDate()).isEqualTo(DEFAULT_GATEWAY_DATE);
        assertThat(testReconcileDiscrepancy.getClosed()).isEqualTo(UPDATED_CLOSED);
        assertThat(testReconcileDiscrepancy.getComment()).isEqualTo(UPDATED_COMMENT);
    }

    @Test
    @Transactional
    void fullUpdateReconcileDiscrepancyWithPatch() throws Exception {
        // Initialize the database
        reconcileDiscrepancyRepository.saveAndFlush(reconcileDiscrepancy);

        int databaseSizeBeforeUpdate = reconcileDiscrepancyRepository.findAll().size();

        // Update the reconcileDiscrepancy using partial update
        ReconcileDiscrepancy partialUpdatedReconcileDiscrepancy = new ReconcileDiscrepancy();
        partialUpdatedReconcileDiscrepancy.setId(reconcileDiscrepancy.getId());

        partialUpdatedReconcileDiscrepancy
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

        restReconcileDiscrepancyMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedReconcileDiscrepancy.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedReconcileDiscrepancy))
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
        assertThat(testReconcileDiscrepancy.getSystemStatus()).isEqualTo(UPDATED_SYSTEM_STATUS);
        assertThat(testReconcileDiscrepancy.getGatewayStatus()).isEqualTo(UPDATED_GATEWAY_STATUS);
        assertThat(testReconcileDiscrepancy.getSystemDate()).isEqualTo(UPDATED_SYSTEM_DATE);
        assertThat(testReconcileDiscrepancy.getGatewayDate()).isEqualTo(UPDATED_GATEWAY_DATE);
        assertThat(testReconcileDiscrepancy.getClosed()).isEqualTo(UPDATED_CLOSED);
        assertThat(testReconcileDiscrepancy.getComment()).isEqualTo(UPDATED_COMMENT);
    }

    @Test
    @Transactional
    void patchNonExistingReconcileDiscrepancy() throws Exception {
        int databaseSizeBeforeUpdate = reconcileDiscrepancyRepository.findAll().size();
        reconcileDiscrepancy.setId(count.incrementAndGet());

        // Create the ReconcileDiscrepancy
        ReconcileDiscrepancyDTO reconcileDiscrepancyDTO = reconcileDiscrepancyMapper.toDto(reconcileDiscrepancy);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restReconcileDiscrepancyMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, reconcileDiscrepancyDTO.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(reconcileDiscrepancyDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the ReconcileDiscrepancy in the database
        List<ReconcileDiscrepancy> reconcileDiscrepancyList = reconcileDiscrepancyRepository.findAll();
        assertThat(reconcileDiscrepancyList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchReconcileDiscrepancy() throws Exception {
        int databaseSizeBeforeUpdate = reconcileDiscrepancyRepository.findAll().size();
        reconcileDiscrepancy.setId(count.incrementAndGet());

        // Create the ReconcileDiscrepancy
        ReconcileDiscrepancyDTO reconcileDiscrepancyDTO = reconcileDiscrepancyMapper.toDto(reconcileDiscrepancy);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restReconcileDiscrepancyMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, count.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(reconcileDiscrepancyDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the ReconcileDiscrepancy in the database
        List<ReconcileDiscrepancy> reconcileDiscrepancyList = reconcileDiscrepancyRepository.findAll();
        assertThat(reconcileDiscrepancyList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamReconcileDiscrepancy() throws Exception {
        int databaseSizeBeforeUpdate = reconcileDiscrepancyRepository.findAll().size();
        reconcileDiscrepancy.setId(count.incrementAndGet());

        // Create the ReconcileDiscrepancy
        ReconcileDiscrepancyDTO reconcileDiscrepancyDTO = reconcileDiscrepancyMapper.toDto(reconcileDiscrepancy);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restReconcileDiscrepancyMockMvc
            .perform(
                patch(ENTITY_API_URL)
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(reconcileDiscrepancyDTO))
            )
            .andExpect(status().isMethodNotAllowed());

        // Validate the ReconcileDiscrepancy in the database
        List<ReconcileDiscrepancy> reconcileDiscrepancyList = reconcileDiscrepancyRepository.findAll();
        assertThat(reconcileDiscrepancyList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteReconcileDiscrepancy() throws Exception {
        // Initialize the database
        reconcileDiscrepancyRepository.saveAndFlush(reconcileDiscrepancy);

        int databaseSizeBeforeDelete = reconcileDiscrepancyRepository.findAll().size();

        // Delete the reconcileDiscrepancy
        restReconcileDiscrepancyMockMvc
            .perform(delete(ENTITY_API_URL_ID, reconcileDiscrepancy.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        List<ReconcileDiscrepancy> reconcileDiscrepancyList = reconcileDiscrepancyRepository.findAll();
        assertThat(reconcileDiscrepancyList).hasSize(databaseSizeBeforeDelete - 1);
    }
}
