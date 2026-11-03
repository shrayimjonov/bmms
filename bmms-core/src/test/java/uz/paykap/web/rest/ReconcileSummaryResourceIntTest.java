package uz.paykap.web.rest;

import uz.paykap.PkbillmsApp;

import uz.paykap.domain.ReconcileSummary;
import uz.paykap.repository.ReconcileSummaryRepository;
import uz.paykap.service.ReconcileSummaryService;
import uz.paykap.service.dto.ReconcileSummaryDTO;
import uz.paykap.service.mapper.ReconcileSummaryMapper;
import uz.paykap.web.rest.errors.ExceptionTranslator;
import uz.paykap.service.dto.ReconcileSummaryCriteria;
import uz.paykap.service.ReconcileSummaryQueryService;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.Validator;

import javax.persistence.EntityManager;
import java.time.LocalDate;
import java.time.Instant;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.List;


import static uz.paykap.web.rest.TestUtil.createFormattingConversionService;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import uz.paykap.domain.enumeration.ReconcileSummaryStatus;
import uz.paykap.domain.enumeration.ReconcilePeriod;
import uz.paykap.domain.enumeration.ReconcileSummaryType;
/**
 * Test class for the ReconcileSummaryResource REST controller.
 *
 * @see ReconcileSummaryResource
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = PkbillmsApp.class)
public class ReconcileSummaryResourceIntTest {

    private static final Long DEFAULT_GATEWAY_ID = 1L;
    private static final Long UPDATED_GATEWAY_ID = 2L;

    private static final Long DEFAULT_SYSTEM_AMOUNT = 1L;
    private static final Long UPDATED_SYSTEM_AMOUNT = 2L;

    private static final Long DEFAULT_GATEWAY_AMOUNT = 1L;
    private static final Long UPDATED_GATEWAY_AMOUNT = 2L;

    private static final Long DEFAULT_SYSTEM_COUNT = 1L;
    private static final Long UPDATED_SYSTEM_COUNT = 2L;

    private static final Long DEFAULT_GATEWAY_COUNT = 1L;
    private static final Long UPDATED_GATEWAY_COUNT = 2L;

    private static final LocalDate DEFAULT_DATE = LocalDate.ofEpochDay(0L);
    private static final LocalDate UPDATED_DATE = LocalDate.now(ZoneId.systemDefault());

    private static final ReconcileSummaryStatus DEFAULT_STATUS = ReconcileSummaryStatus.COMPARED_OK;
    private static final ReconcileSummaryStatus UPDATED_STATUS = ReconcileSummaryStatus.COMPARED_FALSE;

    private static final Boolean DEFAULT_SUCCESS = false;
    private static final Boolean UPDATED_SUCCESS = true;

    private static final ReconcilePeriod DEFAULT_PERIOD = ReconcilePeriod.DAY;
    private static final ReconcilePeriod UPDATED_PERIOD = ReconcilePeriod.MONTH;

    private static final String DEFAULT_GATEWAY_NAME = "AAAAAAAAAA";
    private static final String UPDATED_GATEWAY_NAME = "BBBBBBBBBB";

    private static final Instant DEFAULT_FROM_DATE = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_FROM_DATE = Instant.now().truncatedTo(ChronoUnit.MILLIS);

    private static final Instant DEFAULT_TO_DATE = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_TO_DATE = Instant.now().truncatedTo(ChronoUnit.MILLIS);

    private static final ReconcileSummaryType DEFAULT_TYPE = ReconcileSummaryType.PAYMENT;
    private static final ReconcileSummaryType UPDATED_TYPE = ReconcileSummaryType.TERMINAL;

    @Autowired
    private ReconcileSummaryRepository reconcileSummaryRepository;

    @Autowired
    private ReconcileSummaryMapper reconcileSummaryMapper;

    @Autowired
    private ReconcileSummaryService reconcileSummaryService;

    @Autowired
    private ReconcileSummaryQueryService reconcileSummaryQueryService;

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

    private MockMvc restReconcileSummaryMockMvc;

    private ReconcileSummary reconcileSummary;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        final ReconcileSummaryResource reconcileSummaryResource = new ReconcileSummaryResource(reconcileSummaryService, reconcileSummaryRepository, reconcileSummaryQueryService);
        this.restReconcileSummaryMockMvc = MockMvcBuilders.standaloneSetup(reconcileSummaryResource)
            .setCustomArgumentResolvers(pageableArgumentResolver)
            .setControllerAdvice(exceptionTranslator)
            .setConversionService(createFormattingConversionService())
            .setMessageConverters(jacksonMessageConverter)
            .setValidator(validator).build();
    }

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
            .fromDate(DEFAULT_FROM_DATE)
            .toDate(DEFAULT_TO_DATE)
            .type(DEFAULT_TYPE);
        return reconcileSummary;
    }

    @Before
    public void initTest() {
        reconcileSummary = createEntity(em);
    }

    @Test
    @Transactional
    public void createReconcileSummary() throws Exception {
        int databaseSizeBeforeCreate = reconcileSummaryRepository.findAll().size();

        // Create the ReconcileSummary
        ReconcileSummaryDTO reconcileSummaryDTO = reconcileSummaryMapper.toDto(reconcileSummary);
        restReconcileSummaryMockMvc.perform(post("/api/reconcile-summaries")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(reconcileSummaryDTO)))
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
        assertThat(testReconcileSummary.isSuccess()).isEqualTo(DEFAULT_SUCCESS);
        assertThat(testReconcileSummary.getPeriod()).isEqualTo(DEFAULT_PERIOD);
        assertThat(testReconcileSummary.getGatewayName()).isEqualTo(DEFAULT_GATEWAY_NAME);
        assertThat(testReconcileSummary.getFromDate()).isEqualTo(DEFAULT_FROM_DATE);
        assertThat(testReconcileSummary.getToDate()).isEqualTo(DEFAULT_TO_DATE);
        assertThat(testReconcileSummary.getType()).isEqualTo(DEFAULT_TYPE);
    }

    @Test
    @Transactional
    public void createReconcileSummaryWithExistingId() throws Exception {
        int databaseSizeBeforeCreate = reconcileSummaryRepository.findAll().size();

        // Create the ReconcileSummary with an existing ID
        reconcileSummary.setId(1L);
        ReconcileSummaryDTO reconcileSummaryDTO = reconcileSummaryMapper.toDto(reconcileSummary);

        // An entity with an existing ID cannot be created, so this API call must fail
        restReconcileSummaryMockMvc.perform(post("/api/reconcile-summaries")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(reconcileSummaryDTO)))
            .andExpect(status().isBadRequest());

        // Validate the ReconcileSummary in the database
        List<ReconcileSummary> reconcileSummaryList = reconcileSummaryRepository.findAll();
        assertThat(reconcileSummaryList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    public void checkGatewayIdIsRequired() throws Exception {
        int databaseSizeBeforeTest = reconcileSummaryRepository.findAll().size();
        // set the field null
        reconcileSummary.setGatewayId(null);

        // Create the ReconcileSummary, which fails.
        ReconcileSummaryDTO reconcileSummaryDTO = reconcileSummaryMapper.toDto(reconcileSummary);

        restReconcileSummaryMockMvc.perform(post("/api/reconcile-summaries")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(reconcileSummaryDTO)))
            .andExpect(status().isBadRequest());

        List<ReconcileSummary> reconcileSummaryList = reconcileSummaryRepository.findAll();
        assertThat(reconcileSummaryList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkSystemAmountIsRequired() throws Exception {
        int databaseSizeBeforeTest = reconcileSummaryRepository.findAll().size();
        // set the field null
        reconcileSummary.setSystemAmount(null);

        // Create the ReconcileSummary, which fails.
        ReconcileSummaryDTO reconcileSummaryDTO = reconcileSummaryMapper.toDto(reconcileSummary);

        restReconcileSummaryMockMvc.perform(post("/api/reconcile-summaries")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(reconcileSummaryDTO)))
            .andExpect(status().isBadRequest());

        List<ReconcileSummary> reconcileSummaryList = reconcileSummaryRepository.findAll();
        assertThat(reconcileSummaryList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkGatewayAmountIsRequired() throws Exception {
        int databaseSizeBeforeTest = reconcileSummaryRepository.findAll().size();
        // set the field null
        reconcileSummary.setGatewayAmount(null);

        // Create the ReconcileSummary, which fails.
        ReconcileSummaryDTO reconcileSummaryDTO = reconcileSummaryMapper.toDto(reconcileSummary);

        restReconcileSummaryMockMvc.perform(post("/api/reconcile-summaries")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(reconcileSummaryDTO)))
            .andExpect(status().isBadRequest());

        List<ReconcileSummary> reconcileSummaryList = reconcileSummaryRepository.findAll();
        assertThat(reconcileSummaryList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkSystemCountIsRequired() throws Exception {
        int databaseSizeBeforeTest = reconcileSummaryRepository.findAll().size();
        // set the field null
        reconcileSummary.setSystemCount(null);

        // Create the ReconcileSummary, which fails.
        ReconcileSummaryDTO reconcileSummaryDTO = reconcileSummaryMapper.toDto(reconcileSummary);

        restReconcileSummaryMockMvc.perform(post("/api/reconcile-summaries")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(reconcileSummaryDTO)))
            .andExpect(status().isBadRequest());

        List<ReconcileSummary> reconcileSummaryList = reconcileSummaryRepository.findAll();
        assertThat(reconcileSummaryList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkGatewayCountIsRequired() throws Exception {
        int databaseSizeBeforeTest = reconcileSummaryRepository.findAll().size();
        // set the field null
        reconcileSummary.setGatewayCount(null);

        // Create the ReconcileSummary, which fails.
        ReconcileSummaryDTO reconcileSummaryDTO = reconcileSummaryMapper.toDto(reconcileSummary);

        restReconcileSummaryMockMvc.perform(post("/api/reconcile-summaries")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(reconcileSummaryDTO)))
            .andExpect(status().isBadRequest());

        List<ReconcileSummary> reconcileSummaryList = reconcileSummaryRepository.findAll();
        assertThat(reconcileSummaryList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void getAllReconcileSummaries() throws Exception {
        // Initialize the database
        reconcileSummaryRepository.saveAndFlush(reconcileSummary);

        // Get all the reconcileSummaryList
        restReconcileSummaryMockMvc.perform(get("/api/reconcile-summaries?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
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
            .andExpect(jsonPath("$.[*].gatewayName").value(hasItem(DEFAULT_GATEWAY_NAME.toString())))
            .andExpect(jsonPath("$.[*].fromDate").value(hasItem(DEFAULT_FROM_DATE.toString())))
            .andExpect(jsonPath("$.[*].toDate").value(hasItem(DEFAULT_TO_DATE.toString())))
            .andExpect(jsonPath("$.[*].type").value(hasItem(DEFAULT_TYPE.toString())));
    }
    
    @Test
    @Transactional
    public void getReconcileSummary() throws Exception {
        // Initialize the database
        reconcileSummaryRepository.saveAndFlush(reconcileSummary);

        // Get the reconcileSummary
        restReconcileSummaryMockMvc.perform(get("/api/reconcile-summaries/{id}", reconcileSummary.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
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
            .andExpect(jsonPath("$.gatewayName").value(DEFAULT_GATEWAY_NAME.toString()))
            .andExpect(jsonPath("$.fromDate").value(DEFAULT_FROM_DATE.toString()))
            .andExpect(jsonPath("$.toDate").value(DEFAULT_TO_DATE.toString()))
            .andExpect(jsonPath("$.type").value(DEFAULT_TYPE.toString()));
    }

    @Test
    @Transactional
    public void getAllReconcileSummariesByGatewayIdIsEqualToSomething() throws Exception {
        // Initialize the database
        reconcileSummaryRepository.saveAndFlush(reconcileSummary);

        // Get all the reconcileSummaryList where gatewayId equals to DEFAULT_GATEWAY_ID
        defaultReconcileSummaryShouldBeFound("gatewayId.equals=" + DEFAULT_GATEWAY_ID);

        // Get all the reconcileSummaryList where gatewayId equals to UPDATED_GATEWAY_ID
        defaultReconcileSummaryShouldNotBeFound("gatewayId.equals=" + UPDATED_GATEWAY_ID);
    }

    @Test
    @Transactional
    public void getAllReconcileSummariesByGatewayIdIsInShouldWork() throws Exception {
        // Initialize the database
        reconcileSummaryRepository.saveAndFlush(reconcileSummary);

        // Get all the reconcileSummaryList where gatewayId in DEFAULT_GATEWAY_ID or UPDATED_GATEWAY_ID
        defaultReconcileSummaryShouldBeFound("gatewayId.in=" + DEFAULT_GATEWAY_ID + "," + UPDATED_GATEWAY_ID);

        // Get all the reconcileSummaryList where gatewayId equals to UPDATED_GATEWAY_ID
        defaultReconcileSummaryShouldNotBeFound("gatewayId.in=" + UPDATED_GATEWAY_ID);
    }

    @Test
    @Transactional
    public void getAllReconcileSummariesByGatewayIdIsNullOrNotNull() throws Exception {
        // Initialize the database
        reconcileSummaryRepository.saveAndFlush(reconcileSummary);

        // Get all the reconcileSummaryList where gatewayId is not null
        defaultReconcileSummaryShouldBeFound("gatewayId.specified=true");

        // Get all the reconcileSummaryList where gatewayId is null
        defaultReconcileSummaryShouldNotBeFound("gatewayId.specified=false");
    }

    @Test
    @Transactional
    public void getAllReconcileSummariesByGatewayIdIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        reconcileSummaryRepository.saveAndFlush(reconcileSummary);

        // Get all the reconcileSummaryList where gatewayId greater than or equals to DEFAULT_GATEWAY_ID
        defaultReconcileSummaryShouldBeFound("gatewayId.greaterOrEqualThan=" + DEFAULT_GATEWAY_ID);

        // Get all the reconcileSummaryList where gatewayId greater than or equals to UPDATED_GATEWAY_ID
        defaultReconcileSummaryShouldNotBeFound("gatewayId.greaterOrEqualThan=" + UPDATED_GATEWAY_ID);
    }

    @Test
    @Transactional
    public void getAllReconcileSummariesByGatewayIdIsLessThanSomething() throws Exception {
        // Initialize the database
        reconcileSummaryRepository.saveAndFlush(reconcileSummary);

        // Get all the reconcileSummaryList where gatewayId less than or equals to DEFAULT_GATEWAY_ID
        defaultReconcileSummaryShouldNotBeFound("gatewayId.lessThan=" + DEFAULT_GATEWAY_ID);

        // Get all the reconcileSummaryList where gatewayId less than or equals to UPDATED_GATEWAY_ID
        defaultReconcileSummaryShouldBeFound("gatewayId.lessThan=" + UPDATED_GATEWAY_ID);
    }


    @Test
    @Transactional
    public void getAllReconcileSummariesBySystemAmountIsEqualToSomething() throws Exception {
        // Initialize the database
        reconcileSummaryRepository.saveAndFlush(reconcileSummary);

        // Get all the reconcileSummaryList where systemAmount equals to DEFAULT_SYSTEM_AMOUNT
        defaultReconcileSummaryShouldBeFound("systemAmount.equals=" + DEFAULT_SYSTEM_AMOUNT);

        // Get all the reconcileSummaryList where systemAmount equals to UPDATED_SYSTEM_AMOUNT
        defaultReconcileSummaryShouldNotBeFound("systemAmount.equals=" + UPDATED_SYSTEM_AMOUNT);
    }

    @Test
    @Transactional
    public void getAllReconcileSummariesBySystemAmountIsInShouldWork() throws Exception {
        // Initialize the database
        reconcileSummaryRepository.saveAndFlush(reconcileSummary);

        // Get all the reconcileSummaryList where systemAmount in DEFAULT_SYSTEM_AMOUNT or UPDATED_SYSTEM_AMOUNT
        defaultReconcileSummaryShouldBeFound("systemAmount.in=" + DEFAULT_SYSTEM_AMOUNT + "," + UPDATED_SYSTEM_AMOUNT);

        // Get all the reconcileSummaryList where systemAmount equals to UPDATED_SYSTEM_AMOUNT
        defaultReconcileSummaryShouldNotBeFound("systemAmount.in=" + UPDATED_SYSTEM_AMOUNT);
    }

    @Test
    @Transactional
    public void getAllReconcileSummariesBySystemAmountIsNullOrNotNull() throws Exception {
        // Initialize the database
        reconcileSummaryRepository.saveAndFlush(reconcileSummary);

        // Get all the reconcileSummaryList where systemAmount is not null
        defaultReconcileSummaryShouldBeFound("systemAmount.specified=true");

        // Get all the reconcileSummaryList where systemAmount is null
        defaultReconcileSummaryShouldNotBeFound("systemAmount.specified=false");
    }

    @Test
    @Transactional
    public void getAllReconcileSummariesBySystemAmountIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        reconcileSummaryRepository.saveAndFlush(reconcileSummary);

        // Get all the reconcileSummaryList where systemAmount greater than or equals to DEFAULT_SYSTEM_AMOUNT
        defaultReconcileSummaryShouldBeFound("systemAmount.greaterOrEqualThan=" + DEFAULT_SYSTEM_AMOUNT);

        // Get all the reconcileSummaryList where systemAmount greater than or equals to UPDATED_SYSTEM_AMOUNT
        defaultReconcileSummaryShouldNotBeFound("systemAmount.greaterOrEqualThan=" + UPDATED_SYSTEM_AMOUNT);
    }

    @Test
    @Transactional
    public void getAllReconcileSummariesBySystemAmountIsLessThanSomething() throws Exception {
        // Initialize the database
        reconcileSummaryRepository.saveAndFlush(reconcileSummary);

        // Get all the reconcileSummaryList where systemAmount less than or equals to DEFAULT_SYSTEM_AMOUNT
        defaultReconcileSummaryShouldNotBeFound("systemAmount.lessThan=" + DEFAULT_SYSTEM_AMOUNT);

        // Get all the reconcileSummaryList where systemAmount less than or equals to UPDATED_SYSTEM_AMOUNT
        defaultReconcileSummaryShouldBeFound("systemAmount.lessThan=" + UPDATED_SYSTEM_AMOUNT);
    }


    @Test
    @Transactional
    public void getAllReconcileSummariesByGatewayAmountIsEqualToSomething() throws Exception {
        // Initialize the database
        reconcileSummaryRepository.saveAndFlush(reconcileSummary);

        // Get all the reconcileSummaryList where gatewayAmount equals to DEFAULT_GATEWAY_AMOUNT
        defaultReconcileSummaryShouldBeFound("gatewayAmount.equals=" + DEFAULT_GATEWAY_AMOUNT);

        // Get all the reconcileSummaryList where gatewayAmount equals to UPDATED_GATEWAY_AMOUNT
        defaultReconcileSummaryShouldNotBeFound("gatewayAmount.equals=" + UPDATED_GATEWAY_AMOUNT);
    }

    @Test
    @Transactional
    public void getAllReconcileSummariesByGatewayAmountIsInShouldWork() throws Exception {
        // Initialize the database
        reconcileSummaryRepository.saveAndFlush(reconcileSummary);

        // Get all the reconcileSummaryList where gatewayAmount in DEFAULT_GATEWAY_AMOUNT or UPDATED_GATEWAY_AMOUNT
        defaultReconcileSummaryShouldBeFound("gatewayAmount.in=" + DEFAULT_GATEWAY_AMOUNT + "," + UPDATED_GATEWAY_AMOUNT);

        // Get all the reconcileSummaryList where gatewayAmount equals to UPDATED_GATEWAY_AMOUNT
        defaultReconcileSummaryShouldNotBeFound("gatewayAmount.in=" + UPDATED_GATEWAY_AMOUNT);
    }

    @Test
    @Transactional
    public void getAllReconcileSummariesByGatewayAmountIsNullOrNotNull() throws Exception {
        // Initialize the database
        reconcileSummaryRepository.saveAndFlush(reconcileSummary);

        // Get all the reconcileSummaryList where gatewayAmount is not null
        defaultReconcileSummaryShouldBeFound("gatewayAmount.specified=true");

        // Get all the reconcileSummaryList where gatewayAmount is null
        defaultReconcileSummaryShouldNotBeFound("gatewayAmount.specified=false");
    }

    @Test
    @Transactional
    public void getAllReconcileSummariesByGatewayAmountIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        reconcileSummaryRepository.saveAndFlush(reconcileSummary);

        // Get all the reconcileSummaryList where gatewayAmount greater than or equals to DEFAULT_GATEWAY_AMOUNT
        defaultReconcileSummaryShouldBeFound("gatewayAmount.greaterOrEqualThan=" + DEFAULT_GATEWAY_AMOUNT);

        // Get all the reconcileSummaryList where gatewayAmount greater than or equals to UPDATED_GATEWAY_AMOUNT
        defaultReconcileSummaryShouldNotBeFound("gatewayAmount.greaterOrEqualThan=" + UPDATED_GATEWAY_AMOUNT);
    }

    @Test
    @Transactional
    public void getAllReconcileSummariesByGatewayAmountIsLessThanSomething() throws Exception {
        // Initialize the database
        reconcileSummaryRepository.saveAndFlush(reconcileSummary);

        // Get all the reconcileSummaryList where gatewayAmount less than or equals to DEFAULT_GATEWAY_AMOUNT
        defaultReconcileSummaryShouldNotBeFound("gatewayAmount.lessThan=" + DEFAULT_GATEWAY_AMOUNT);

        // Get all the reconcileSummaryList where gatewayAmount less than or equals to UPDATED_GATEWAY_AMOUNT
        defaultReconcileSummaryShouldBeFound("gatewayAmount.lessThan=" + UPDATED_GATEWAY_AMOUNT);
    }


    @Test
    @Transactional
    public void getAllReconcileSummariesBySystemCountIsEqualToSomething() throws Exception {
        // Initialize the database
        reconcileSummaryRepository.saveAndFlush(reconcileSummary);

        // Get all the reconcileSummaryList where systemCount equals to DEFAULT_SYSTEM_COUNT
        defaultReconcileSummaryShouldBeFound("systemCount.equals=" + DEFAULT_SYSTEM_COUNT);

        // Get all the reconcileSummaryList where systemCount equals to UPDATED_SYSTEM_COUNT
        defaultReconcileSummaryShouldNotBeFound("systemCount.equals=" + UPDATED_SYSTEM_COUNT);
    }

    @Test
    @Transactional
    public void getAllReconcileSummariesBySystemCountIsInShouldWork() throws Exception {
        // Initialize the database
        reconcileSummaryRepository.saveAndFlush(reconcileSummary);

        // Get all the reconcileSummaryList where systemCount in DEFAULT_SYSTEM_COUNT or UPDATED_SYSTEM_COUNT
        defaultReconcileSummaryShouldBeFound("systemCount.in=" + DEFAULT_SYSTEM_COUNT + "," + UPDATED_SYSTEM_COUNT);

        // Get all the reconcileSummaryList where systemCount equals to UPDATED_SYSTEM_COUNT
        defaultReconcileSummaryShouldNotBeFound("systemCount.in=" + UPDATED_SYSTEM_COUNT);
    }

    @Test
    @Transactional
    public void getAllReconcileSummariesBySystemCountIsNullOrNotNull() throws Exception {
        // Initialize the database
        reconcileSummaryRepository.saveAndFlush(reconcileSummary);

        // Get all the reconcileSummaryList where systemCount is not null
        defaultReconcileSummaryShouldBeFound("systemCount.specified=true");

        // Get all the reconcileSummaryList where systemCount is null
        defaultReconcileSummaryShouldNotBeFound("systemCount.specified=false");
    }

    @Test
    @Transactional
    public void getAllReconcileSummariesBySystemCountIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        reconcileSummaryRepository.saveAndFlush(reconcileSummary);

        // Get all the reconcileSummaryList where systemCount greater than or equals to DEFAULT_SYSTEM_COUNT
        defaultReconcileSummaryShouldBeFound("systemCount.greaterOrEqualThan=" + DEFAULT_SYSTEM_COUNT);

        // Get all the reconcileSummaryList where systemCount greater than or equals to UPDATED_SYSTEM_COUNT
        defaultReconcileSummaryShouldNotBeFound("systemCount.greaterOrEqualThan=" + UPDATED_SYSTEM_COUNT);
    }

    @Test
    @Transactional
    public void getAllReconcileSummariesBySystemCountIsLessThanSomething() throws Exception {
        // Initialize the database
        reconcileSummaryRepository.saveAndFlush(reconcileSummary);

        // Get all the reconcileSummaryList where systemCount less than or equals to DEFAULT_SYSTEM_COUNT
        defaultReconcileSummaryShouldNotBeFound("systemCount.lessThan=" + DEFAULT_SYSTEM_COUNT);

        // Get all the reconcileSummaryList where systemCount less than or equals to UPDATED_SYSTEM_COUNT
        defaultReconcileSummaryShouldBeFound("systemCount.lessThan=" + UPDATED_SYSTEM_COUNT);
    }


    @Test
    @Transactional
    public void getAllReconcileSummariesByGatewayCountIsEqualToSomething() throws Exception {
        // Initialize the database
        reconcileSummaryRepository.saveAndFlush(reconcileSummary);

        // Get all the reconcileSummaryList where gatewayCount equals to DEFAULT_GATEWAY_COUNT
        defaultReconcileSummaryShouldBeFound("gatewayCount.equals=" + DEFAULT_GATEWAY_COUNT);

        // Get all the reconcileSummaryList where gatewayCount equals to UPDATED_GATEWAY_COUNT
        defaultReconcileSummaryShouldNotBeFound("gatewayCount.equals=" + UPDATED_GATEWAY_COUNT);
    }

    @Test
    @Transactional
    public void getAllReconcileSummariesByGatewayCountIsInShouldWork() throws Exception {
        // Initialize the database
        reconcileSummaryRepository.saveAndFlush(reconcileSummary);

        // Get all the reconcileSummaryList where gatewayCount in DEFAULT_GATEWAY_COUNT or UPDATED_GATEWAY_COUNT
        defaultReconcileSummaryShouldBeFound("gatewayCount.in=" + DEFAULT_GATEWAY_COUNT + "," + UPDATED_GATEWAY_COUNT);

        // Get all the reconcileSummaryList where gatewayCount equals to UPDATED_GATEWAY_COUNT
        defaultReconcileSummaryShouldNotBeFound("gatewayCount.in=" + UPDATED_GATEWAY_COUNT);
    }

    @Test
    @Transactional
    public void getAllReconcileSummariesByGatewayCountIsNullOrNotNull() throws Exception {
        // Initialize the database
        reconcileSummaryRepository.saveAndFlush(reconcileSummary);

        // Get all the reconcileSummaryList where gatewayCount is not null
        defaultReconcileSummaryShouldBeFound("gatewayCount.specified=true");

        // Get all the reconcileSummaryList where gatewayCount is null
        defaultReconcileSummaryShouldNotBeFound("gatewayCount.specified=false");
    }

    @Test
    @Transactional
    public void getAllReconcileSummariesByGatewayCountIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        reconcileSummaryRepository.saveAndFlush(reconcileSummary);

        // Get all the reconcileSummaryList where gatewayCount greater than or equals to DEFAULT_GATEWAY_COUNT
        defaultReconcileSummaryShouldBeFound("gatewayCount.greaterOrEqualThan=" + DEFAULT_GATEWAY_COUNT);

        // Get all the reconcileSummaryList where gatewayCount greater than or equals to UPDATED_GATEWAY_COUNT
        defaultReconcileSummaryShouldNotBeFound("gatewayCount.greaterOrEqualThan=" + UPDATED_GATEWAY_COUNT);
    }

    @Test
    @Transactional
    public void getAllReconcileSummariesByGatewayCountIsLessThanSomething() throws Exception {
        // Initialize the database
        reconcileSummaryRepository.saveAndFlush(reconcileSummary);

        // Get all the reconcileSummaryList where gatewayCount less than or equals to DEFAULT_GATEWAY_COUNT
        defaultReconcileSummaryShouldNotBeFound("gatewayCount.lessThan=" + DEFAULT_GATEWAY_COUNT);

        // Get all the reconcileSummaryList where gatewayCount less than or equals to UPDATED_GATEWAY_COUNT
        defaultReconcileSummaryShouldBeFound("gatewayCount.lessThan=" + UPDATED_GATEWAY_COUNT);
    }


    @Test
    @Transactional
    public void getAllReconcileSummariesByDateIsEqualToSomething() throws Exception {
        // Initialize the database
        reconcileSummaryRepository.saveAndFlush(reconcileSummary);

        // Get all the reconcileSummaryList where date equals to DEFAULT_DATE
        defaultReconcileSummaryShouldBeFound("date.equals=" + DEFAULT_DATE);

        // Get all the reconcileSummaryList where date equals to UPDATED_DATE
        defaultReconcileSummaryShouldNotBeFound("date.equals=" + UPDATED_DATE);
    }

    @Test
    @Transactional
    public void getAllReconcileSummariesByDateIsInShouldWork() throws Exception {
        // Initialize the database
        reconcileSummaryRepository.saveAndFlush(reconcileSummary);

        // Get all the reconcileSummaryList where date in DEFAULT_DATE or UPDATED_DATE
        defaultReconcileSummaryShouldBeFound("date.in=" + DEFAULT_DATE + "," + UPDATED_DATE);

        // Get all the reconcileSummaryList where date equals to UPDATED_DATE
        defaultReconcileSummaryShouldNotBeFound("date.in=" + UPDATED_DATE);
    }

    @Test
    @Transactional
    public void getAllReconcileSummariesByDateIsNullOrNotNull() throws Exception {
        // Initialize the database
        reconcileSummaryRepository.saveAndFlush(reconcileSummary);

        // Get all the reconcileSummaryList where date is not null
        defaultReconcileSummaryShouldBeFound("date.specified=true");

        // Get all the reconcileSummaryList where date is null
        defaultReconcileSummaryShouldNotBeFound("date.specified=false");
    }

    @Test
    @Transactional
    public void getAllReconcileSummariesByDateIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        reconcileSummaryRepository.saveAndFlush(reconcileSummary);

        // Get all the reconcileSummaryList where date greater than or equals to DEFAULT_DATE
        defaultReconcileSummaryShouldBeFound("date.greaterOrEqualThan=" + DEFAULT_DATE);

        // Get all the reconcileSummaryList where date greater than or equals to UPDATED_DATE
        defaultReconcileSummaryShouldNotBeFound("date.greaterOrEqualThan=" + UPDATED_DATE);
    }

    @Test
    @Transactional
    public void getAllReconcileSummariesByDateIsLessThanSomething() throws Exception {
        // Initialize the database
        reconcileSummaryRepository.saveAndFlush(reconcileSummary);

        // Get all the reconcileSummaryList where date less than or equals to DEFAULT_DATE
        defaultReconcileSummaryShouldNotBeFound("date.lessThan=" + DEFAULT_DATE);

        // Get all the reconcileSummaryList where date less than or equals to UPDATED_DATE
        defaultReconcileSummaryShouldBeFound("date.lessThan=" + UPDATED_DATE);
    }


    @Test
    @Transactional
    public void getAllReconcileSummariesByStatusIsEqualToSomething() throws Exception {
        // Initialize the database
        reconcileSummaryRepository.saveAndFlush(reconcileSummary);

        // Get all the reconcileSummaryList where status equals to DEFAULT_STATUS
        defaultReconcileSummaryShouldBeFound("status.equals=" + DEFAULT_STATUS);

        // Get all the reconcileSummaryList where status equals to UPDATED_STATUS
        defaultReconcileSummaryShouldNotBeFound("status.equals=" + UPDATED_STATUS);
    }

    @Test
    @Transactional
    public void getAllReconcileSummariesByStatusIsInShouldWork() throws Exception {
        // Initialize the database
        reconcileSummaryRepository.saveAndFlush(reconcileSummary);

        // Get all the reconcileSummaryList where status in DEFAULT_STATUS or UPDATED_STATUS
        defaultReconcileSummaryShouldBeFound("status.in=" + DEFAULT_STATUS + "," + UPDATED_STATUS);

        // Get all the reconcileSummaryList where status equals to UPDATED_STATUS
        defaultReconcileSummaryShouldNotBeFound("status.in=" + UPDATED_STATUS);
    }

    @Test
    @Transactional
    public void getAllReconcileSummariesByStatusIsNullOrNotNull() throws Exception {
        // Initialize the database
        reconcileSummaryRepository.saveAndFlush(reconcileSummary);

        // Get all the reconcileSummaryList where status is not null
        defaultReconcileSummaryShouldBeFound("status.specified=true");

        // Get all the reconcileSummaryList where status is null
        defaultReconcileSummaryShouldNotBeFound("status.specified=false");
    }

    @Test
    @Transactional
    public void getAllReconcileSummariesBySuccessIsEqualToSomething() throws Exception {
        // Initialize the database
        reconcileSummaryRepository.saveAndFlush(reconcileSummary);

        // Get all the reconcileSummaryList where success equals to DEFAULT_SUCCESS
        defaultReconcileSummaryShouldBeFound("success.equals=" + DEFAULT_SUCCESS);

        // Get all the reconcileSummaryList where success equals to UPDATED_SUCCESS
        defaultReconcileSummaryShouldNotBeFound("success.equals=" + UPDATED_SUCCESS);
    }

    @Test
    @Transactional
    public void getAllReconcileSummariesBySuccessIsInShouldWork() throws Exception {
        // Initialize the database
        reconcileSummaryRepository.saveAndFlush(reconcileSummary);

        // Get all the reconcileSummaryList where success in DEFAULT_SUCCESS or UPDATED_SUCCESS
        defaultReconcileSummaryShouldBeFound("success.in=" + DEFAULT_SUCCESS + "," + UPDATED_SUCCESS);

        // Get all the reconcileSummaryList where success equals to UPDATED_SUCCESS
        defaultReconcileSummaryShouldNotBeFound("success.in=" + UPDATED_SUCCESS);
    }

    @Test
    @Transactional
    public void getAllReconcileSummariesBySuccessIsNullOrNotNull() throws Exception {
        // Initialize the database
        reconcileSummaryRepository.saveAndFlush(reconcileSummary);

        // Get all the reconcileSummaryList where success is not null
        defaultReconcileSummaryShouldBeFound("success.specified=true");

        // Get all the reconcileSummaryList where success is null
        defaultReconcileSummaryShouldNotBeFound("success.specified=false");
    }

    @Test
    @Transactional
    public void getAllReconcileSummariesByPeriodIsEqualToSomething() throws Exception {
        // Initialize the database
        reconcileSummaryRepository.saveAndFlush(reconcileSummary);

        // Get all the reconcileSummaryList where period equals to DEFAULT_PERIOD
        defaultReconcileSummaryShouldBeFound("period.equals=" + DEFAULT_PERIOD);

        // Get all the reconcileSummaryList where period equals to UPDATED_PERIOD
        defaultReconcileSummaryShouldNotBeFound("period.equals=" + UPDATED_PERIOD);
    }

    @Test
    @Transactional
    public void getAllReconcileSummariesByPeriodIsInShouldWork() throws Exception {
        // Initialize the database
        reconcileSummaryRepository.saveAndFlush(reconcileSummary);

        // Get all the reconcileSummaryList where period in DEFAULT_PERIOD or UPDATED_PERIOD
        defaultReconcileSummaryShouldBeFound("period.in=" + DEFAULT_PERIOD + "," + UPDATED_PERIOD);

        // Get all the reconcileSummaryList where period equals to UPDATED_PERIOD
        defaultReconcileSummaryShouldNotBeFound("period.in=" + UPDATED_PERIOD);
    }

    @Test
    @Transactional
    public void getAllReconcileSummariesByPeriodIsNullOrNotNull() throws Exception {
        // Initialize the database
        reconcileSummaryRepository.saveAndFlush(reconcileSummary);

        // Get all the reconcileSummaryList where period is not null
        defaultReconcileSummaryShouldBeFound("period.specified=true");

        // Get all the reconcileSummaryList where period is null
        defaultReconcileSummaryShouldNotBeFound("period.specified=false");
    }

    @Test
    @Transactional
    public void getAllReconcileSummariesByGatewayNameIsEqualToSomething() throws Exception {
        // Initialize the database
        reconcileSummaryRepository.saveAndFlush(reconcileSummary);

        // Get all the reconcileSummaryList where gatewayName equals to DEFAULT_GATEWAY_NAME
        defaultReconcileSummaryShouldBeFound("gatewayName.equals=" + DEFAULT_GATEWAY_NAME);

        // Get all the reconcileSummaryList where gatewayName equals to UPDATED_GATEWAY_NAME
        defaultReconcileSummaryShouldNotBeFound("gatewayName.equals=" + UPDATED_GATEWAY_NAME);
    }

    @Test
    @Transactional
    public void getAllReconcileSummariesByGatewayNameIsInShouldWork() throws Exception {
        // Initialize the database
        reconcileSummaryRepository.saveAndFlush(reconcileSummary);

        // Get all the reconcileSummaryList where gatewayName in DEFAULT_GATEWAY_NAME or UPDATED_GATEWAY_NAME
        defaultReconcileSummaryShouldBeFound("gatewayName.in=" + DEFAULT_GATEWAY_NAME + "," + UPDATED_GATEWAY_NAME);

        // Get all the reconcileSummaryList where gatewayName equals to UPDATED_GATEWAY_NAME
        defaultReconcileSummaryShouldNotBeFound("gatewayName.in=" + UPDATED_GATEWAY_NAME);
    }

    @Test
    @Transactional
    public void getAllReconcileSummariesByGatewayNameIsNullOrNotNull() throws Exception {
        // Initialize the database
        reconcileSummaryRepository.saveAndFlush(reconcileSummary);

        // Get all the reconcileSummaryList where gatewayName is not null
        defaultReconcileSummaryShouldBeFound("gatewayName.specified=true");

        // Get all the reconcileSummaryList where gatewayName is null
        defaultReconcileSummaryShouldNotBeFound("gatewayName.specified=false");
    }

    @Test
    @Transactional
    public void getAllReconcileSummariesByFromDateIsEqualToSomething() throws Exception {
        // Initialize the database
        reconcileSummaryRepository.saveAndFlush(reconcileSummary);

        // Get all the reconcileSummaryList where fromDate equals to DEFAULT_FROM_DATE
        defaultReconcileSummaryShouldBeFound("fromDate.equals=" + DEFAULT_FROM_DATE);

        // Get all the reconcileSummaryList where fromDate equals to UPDATED_FROM_DATE
        defaultReconcileSummaryShouldNotBeFound("fromDate.equals=" + UPDATED_FROM_DATE);
    }

    @Test
    @Transactional
    public void getAllReconcileSummariesByFromDateIsInShouldWork() throws Exception {
        // Initialize the database
        reconcileSummaryRepository.saveAndFlush(reconcileSummary);

        // Get all the reconcileSummaryList where fromDate in DEFAULT_FROM_DATE or UPDATED_FROM_DATE
        defaultReconcileSummaryShouldBeFound("fromDate.in=" + DEFAULT_FROM_DATE + "," + UPDATED_FROM_DATE);

        // Get all the reconcileSummaryList where fromDate equals to UPDATED_FROM_DATE
        defaultReconcileSummaryShouldNotBeFound("fromDate.in=" + UPDATED_FROM_DATE);
    }

    @Test
    @Transactional
    public void getAllReconcileSummariesByFromDateIsNullOrNotNull() throws Exception {
        // Initialize the database
        reconcileSummaryRepository.saveAndFlush(reconcileSummary);

        // Get all the reconcileSummaryList where fromDate is not null
        defaultReconcileSummaryShouldBeFound("fromDate.specified=true");

        // Get all the reconcileSummaryList where fromDate is null
        defaultReconcileSummaryShouldNotBeFound("fromDate.specified=false");
    }

    @Test
    @Transactional
    public void getAllReconcileSummariesByToDateIsEqualToSomething() throws Exception {
        // Initialize the database
        reconcileSummaryRepository.saveAndFlush(reconcileSummary);

        // Get all the reconcileSummaryList where toDate equals to DEFAULT_TO_DATE
        defaultReconcileSummaryShouldBeFound("toDate.equals=" + DEFAULT_TO_DATE);

        // Get all the reconcileSummaryList where toDate equals to UPDATED_TO_DATE
        defaultReconcileSummaryShouldNotBeFound("toDate.equals=" + UPDATED_TO_DATE);
    }

    @Test
    @Transactional
    public void getAllReconcileSummariesByToDateIsInShouldWork() throws Exception {
        // Initialize the database
        reconcileSummaryRepository.saveAndFlush(reconcileSummary);

        // Get all the reconcileSummaryList where toDate in DEFAULT_TO_DATE or UPDATED_TO_DATE
        defaultReconcileSummaryShouldBeFound("toDate.in=" + DEFAULT_TO_DATE + "," + UPDATED_TO_DATE);

        // Get all the reconcileSummaryList where toDate equals to UPDATED_TO_DATE
        defaultReconcileSummaryShouldNotBeFound("toDate.in=" + UPDATED_TO_DATE);
    }

    @Test
    @Transactional
    public void getAllReconcileSummariesByToDateIsNullOrNotNull() throws Exception {
        // Initialize the database
        reconcileSummaryRepository.saveAndFlush(reconcileSummary);

        // Get all the reconcileSummaryList where toDate is not null
        defaultReconcileSummaryShouldBeFound("toDate.specified=true");

        // Get all the reconcileSummaryList where toDate is null
        defaultReconcileSummaryShouldNotBeFound("toDate.specified=false");
    }

    @Test
    @Transactional
    public void getAllReconcileSummariesByTypeIsEqualToSomething() throws Exception {
        // Initialize the database
        reconcileSummaryRepository.saveAndFlush(reconcileSummary);

        // Get all the reconcileSummaryList where type equals to DEFAULT_TYPE
        defaultReconcileSummaryShouldBeFound("type.equals=" + DEFAULT_TYPE);

        // Get all the reconcileSummaryList where type equals to UPDATED_TYPE
        defaultReconcileSummaryShouldNotBeFound("type.equals=" + UPDATED_TYPE);
    }

    @Test
    @Transactional
    public void getAllReconcileSummariesByTypeIsInShouldWork() throws Exception {
        // Initialize the database
        reconcileSummaryRepository.saveAndFlush(reconcileSummary);

        // Get all the reconcileSummaryList where type in DEFAULT_TYPE or UPDATED_TYPE
        defaultReconcileSummaryShouldBeFound("type.in=" + DEFAULT_TYPE + "," + UPDATED_TYPE);

        // Get all the reconcileSummaryList where type equals to UPDATED_TYPE
        defaultReconcileSummaryShouldNotBeFound("type.in=" + UPDATED_TYPE);
    }

    @Test
    @Transactional
    public void getAllReconcileSummariesByTypeIsNullOrNotNull() throws Exception {
        // Initialize the database
        reconcileSummaryRepository.saveAndFlush(reconcileSummary);

        // Get all the reconcileSummaryList where type is not null
        defaultReconcileSummaryShouldBeFound("type.specified=true");

        // Get all the reconcileSummaryList where type is null
        defaultReconcileSummaryShouldNotBeFound("type.specified=false");
    }
    /**
     * Executes the search, and checks that the default entity is returned
     */
    private void defaultReconcileSummaryShouldBeFound(String filter) throws Exception {
        restReconcileSummaryMockMvc.perform(get("/api/reconcile-summaries?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
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
            .andExpect(jsonPath("$.[*].gatewayName").value(hasItem(DEFAULT_GATEWAY_NAME.toString())))
            .andExpect(jsonPath("$.[*].fromDate").value(hasItem(DEFAULT_FROM_DATE.toString())))
            .andExpect(jsonPath("$.[*].toDate").value(hasItem(DEFAULT_TO_DATE.toString())))
            .andExpect(jsonPath("$.[*].type").value(hasItem(DEFAULT_TYPE.toString())));

        // Check, that the count call also returns 1
        restReconcileSummaryMockMvc.perform(get("/api/reconcile-summaries/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(content().string("1"));
    }

    /**
     * Executes the search, and checks that the default entity is not returned
     */
    private void defaultReconcileSummaryShouldNotBeFound(String filter) throws Exception {
        restReconcileSummaryMockMvc.perform(get("/api/reconcile-summaries?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty());

        // Check, that the count call also returns 0
        restReconcileSummaryMockMvc.perform(get("/api/reconcile-summaries/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(content().string("0"));
    }


    @Test
    @Transactional
    public void getNonExistingReconcileSummary() throws Exception {
        // Get the reconcileSummary
        restReconcileSummaryMockMvc.perform(get("/api/reconcile-summaries/{id}", Long.MAX_VALUE))
            .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updateReconcileSummary() throws Exception {
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
            .fromDate(UPDATED_FROM_DATE)
            .toDate(UPDATED_TO_DATE)
            .type(UPDATED_TYPE);
        ReconcileSummaryDTO reconcileSummaryDTO = reconcileSummaryMapper.toDto(updatedReconcileSummary);

        restReconcileSummaryMockMvc.perform(put("/api/reconcile-summaries")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(reconcileSummaryDTO)))
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
        assertThat(testReconcileSummary.isSuccess()).isEqualTo(UPDATED_SUCCESS);
        assertThat(testReconcileSummary.getPeriod()).isEqualTo(UPDATED_PERIOD);
        assertThat(testReconcileSummary.getGatewayName()).isEqualTo(UPDATED_GATEWAY_NAME);
        assertThat(testReconcileSummary.getFromDate()).isEqualTo(UPDATED_FROM_DATE);
        assertThat(testReconcileSummary.getToDate()).isEqualTo(UPDATED_TO_DATE);
        assertThat(testReconcileSummary.getType()).isEqualTo(UPDATED_TYPE);
    }

    @Test
    @Transactional
    public void updateNonExistingReconcileSummary() throws Exception {
        int databaseSizeBeforeUpdate = reconcileSummaryRepository.findAll().size();

        // Create the ReconcileSummary
        ReconcileSummaryDTO reconcileSummaryDTO = reconcileSummaryMapper.toDto(reconcileSummary);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restReconcileSummaryMockMvc.perform(put("/api/reconcile-summaries")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(reconcileSummaryDTO)))
            .andExpect(status().isBadRequest());

        // Validate the ReconcileSummary in the database
        List<ReconcileSummary> reconcileSummaryList = reconcileSummaryRepository.findAll();
        assertThat(reconcileSummaryList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    public void deleteReconcileSummary() throws Exception {
        // Initialize the database
        reconcileSummaryRepository.saveAndFlush(reconcileSummary);

        int databaseSizeBeforeDelete = reconcileSummaryRepository.findAll().size();

        // Get the reconcileSummary
        restReconcileSummaryMockMvc.perform(delete("/api/reconcile-summaries/{id}", reconcileSummary.getId())
            .accept(TestUtil.APPLICATION_JSON_UTF8))
            .andExpect(status().isOk());

        // Validate the database is empty
        List<ReconcileSummary> reconcileSummaryList = reconcileSummaryRepository.findAll();
        assertThat(reconcileSummaryList).hasSize(databaseSizeBeforeDelete - 1);
    }

    @Test
    @Transactional
    public void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(ReconcileSummary.class);
        ReconcileSummary reconcileSummary1 = new ReconcileSummary();
        reconcileSummary1.setId(1L);
        ReconcileSummary reconcileSummary2 = new ReconcileSummary();
        reconcileSummary2.setId(reconcileSummary1.getId());
        assertThat(reconcileSummary1).isEqualTo(reconcileSummary2);
        reconcileSummary2.setId(2L);
        assertThat(reconcileSummary1).isNotEqualTo(reconcileSummary2);
        reconcileSummary1.setId(null);
        assertThat(reconcileSummary1).isNotEqualTo(reconcileSummary2);
    }

    @Test
    @Transactional
    public void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(ReconcileSummaryDTO.class);
        ReconcileSummaryDTO reconcileSummaryDTO1 = new ReconcileSummaryDTO();
        reconcileSummaryDTO1.setId(1L);
        ReconcileSummaryDTO reconcileSummaryDTO2 = new ReconcileSummaryDTO();
        assertThat(reconcileSummaryDTO1).isNotEqualTo(reconcileSummaryDTO2);
        reconcileSummaryDTO2.setId(reconcileSummaryDTO1.getId());
        assertThat(reconcileSummaryDTO1).isEqualTo(reconcileSummaryDTO2);
        reconcileSummaryDTO2.setId(2L);
        assertThat(reconcileSummaryDTO1).isNotEqualTo(reconcileSummaryDTO2);
        reconcileSummaryDTO1.setId(null);
        assertThat(reconcileSummaryDTO1).isNotEqualTo(reconcileSummaryDTO2);
    }
    //    @Test
    //    @Transactional
    //    public void testEntityFromId() {
    //        assertThat(reconcileSummaryMapper.fromId(42L).getId()).isEqualTo(42);
    //        assertThat(reconcileSummaryMapper.fromId(null)).isNull();
    //    }
}
