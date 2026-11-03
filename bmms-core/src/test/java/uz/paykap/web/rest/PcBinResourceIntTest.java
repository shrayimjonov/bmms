package uz.paykap.web.rest;

import uz.paykap.PkbillmsApp;

import uz.paykap.domain.PcBin;
import uz.paykap.repository.PcBinRepository;
import uz.paykap.service.PcBinService;
import uz.paykap.service.dto.PcBinDTO;
import uz.paykap.service.mapper.PcBinMapper;
import uz.paykap.web.rest.errors.ExceptionTranslator;
import uz.paykap.service.dto.PcBinCriteria;
import uz.paykap.service.PcBinQueryService;

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
import java.util.List;


import static uz.paykap.web.rest.TestUtil.createFormattingConversionService;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import uz.paykap.domain.enumeration.BinPcType;
/**
 * Test class for the PcBinResource REST controller.
 *
 * @see PcBinResource
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = PkbillmsApp.class)
public class PcBinResourceIntTest {

    private static final String DEFAULT_BIN = "AAAAAAAAAA";
    private static final String UPDATED_BIN = "BBBBBBBBBB";

    private static final String DEFAULT_BANK_NAME = "AAAAAAAAAA";
    private static final String UPDATED_BANK_NAME = "BBBBBBBBBB";

    private static final BinPcType DEFAULT_PC_TYPE = BinPcType.UZCARD;
    private static final BinPcType UPDATED_PC_TYPE = BinPcType.HUMO;

    private static final Boolean DEFAULT_USE_CREDIT = false;
    private static final Boolean UPDATED_USE_CREDIT = true;

    private static final Boolean DEFAULT_USE_DEBIT = false;
    private static final Boolean UPDATED_USE_DEBIT = true;

    private static final Boolean DEFAULT_USE_REVERSE = false;
    private static final Boolean UPDATED_USE_REVERSE = true;

    private static final Boolean DEFAULT_USE_RECONCILE = false;
    private static final Boolean UPDATED_USE_RECONCILE = true;

    private static final String DEFAULT_COMMENT = "AAAAAAAAAA";
    private static final String UPDATED_COMMENT = "BBBBBBBBBB";

    @Autowired
    private PcBinRepository pcBinRepository;

    @Autowired
    private PcBinMapper pcBinMapper;

    @Autowired
    private PcBinService pcBinService;

    @Autowired
    private PcBinQueryService pcBinQueryService;

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

    private MockMvc restPcBinMockMvc;

    private PcBin pcBin;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        final PcBinResource pcBinResource = new PcBinResource(pcBinService, pcBinQueryService);
        this.restPcBinMockMvc = MockMvcBuilders.standaloneSetup(pcBinResource)
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
    public static PcBin createEntity(EntityManager em) {
        PcBin pcBin = new PcBin()
            .bin(DEFAULT_BIN)
            .bankName(DEFAULT_BANK_NAME)
            .pcType(DEFAULT_PC_TYPE)
            .useCredit(DEFAULT_USE_CREDIT)
            .useDebit(DEFAULT_USE_DEBIT)
            .useReverse(DEFAULT_USE_REVERSE)
            .useReconcile(DEFAULT_USE_RECONCILE)
            .comment(DEFAULT_COMMENT);
        return pcBin;
    }

    @Before
    public void initTest() {
        pcBin = createEntity(em);
    }

    @Test
    @Transactional
    public void createPcBin() throws Exception {
        int databaseSizeBeforeCreate = pcBinRepository.findAll().size();

        // Create the PcBin
        PcBinDTO pcBinDTO = pcBinMapper.toDto(pcBin);
        restPcBinMockMvc.perform(post("/api/pc-bins")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(pcBinDTO)))
            .andExpect(status().isCreated());

        // Validate the PcBin in the database
        List<PcBin> pcBinList = pcBinRepository.findAll();
        assertThat(pcBinList).hasSize(databaseSizeBeforeCreate + 1);
        PcBin testPcBin = pcBinList.get(pcBinList.size() - 1);
        assertThat(testPcBin.getBin()).isEqualTo(DEFAULT_BIN);
        assertThat(testPcBin.getBankName()).isEqualTo(DEFAULT_BANK_NAME);
        assertThat(testPcBin.getPcType()).isEqualTo(DEFAULT_PC_TYPE);
        assertThat(testPcBin.isUseCredit()).isEqualTo(DEFAULT_USE_CREDIT);
        assertThat(testPcBin.isUseDebit()).isEqualTo(DEFAULT_USE_DEBIT);
        assertThat(testPcBin.isUseReverse()).isEqualTo(DEFAULT_USE_REVERSE);
        assertThat(testPcBin.isUseReconcile()).isEqualTo(DEFAULT_USE_RECONCILE);
        assertThat(testPcBin.getComment()).isEqualTo(DEFAULT_COMMENT);
    }

    @Test
    @Transactional
    public void createPcBinWithExistingId() throws Exception {
        int databaseSizeBeforeCreate = pcBinRepository.findAll().size();

        // Create the PcBin with an existing ID
        pcBin.setId(1L);
        PcBinDTO pcBinDTO = pcBinMapper.toDto(pcBin);

        // An entity with an existing ID cannot be created, so this API call must fail
        restPcBinMockMvc.perform(post("/api/pc-bins")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(pcBinDTO)))
            .andExpect(status().isBadRequest());

        // Validate the PcBin in the database
        List<PcBin> pcBinList = pcBinRepository.findAll();
        assertThat(pcBinList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    public void checkBinIsRequired() throws Exception {
        int databaseSizeBeforeTest = pcBinRepository.findAll().size();
        // set the field null
        pcBin.setBin(null);

        // Create the PcBin, which fails.
        PcBinDTO pcBinDTO = pcBinMapper.toDto(pcBin);

        restPcBinMockMvc.perform(post("/api/pc-bins")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(pcBinDTO)))
            .andExpect(status().isBadRequest());

        List<PcBin> pcBinList = pcBinRepository.findAll();
        assertThat(pcBinList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkUseCreditIsRequired() throws Exception {
        int databaseSizeBeforeTest = pcBinRepository.findAll().size();
        // set the field null
        pcBin.setUseCredit(null);

        // Create the PcBin, which fails.
        PcBinDTO pcBinDTO = pcBinMapper.toDto(pcBin);

        restPcBinMockMvc.perform(post("/api/pc-bins")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(pcBinDTO)))
            .andExpect(status().isBadRequest());

        List<PcBin> pcBinList = pcBinRepository.findAll();
        assertThat(pcBinList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkUseDebitIsRequired() throws Exception {
        int databaseSizeBeforeTest = pcBinRepository.findAll().size();
        // set the field null
        pcBin.setUseDebit(null);

        // Create the PcBin, which fails.
        PcBinDTO pcBinDTO = pcBinMapper.toDto(pcBin);

        restPcBinMockMvc.perform(post("/api/pc-bins")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(pcBinDTO)))
            .andExpect(status().isBadRequest());

        List<PcBin> pcBinList = pcBinRepository.findAll();
        assertThat(pcBinList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkUseReverseIsRequired() throws Exception {
        int databaseSizeBeforeTest = pcBinRepository.findAll().size();
        // set the field null
        pcBin.setUseReverse(null);

        // Create the PcBin, which fails.
        PcBinDTO pcBinDTO = pcBinMapper.toDto(pcBin);

        restPcBinMockMvc.perform(post("/api/pc-bins")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(pcBinDTO)))
            .andExpect(status().isBadRequest());

        List<PcBin> pcBinList = pcBinRepository.findAll();
        assertThat(pcBinList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkUseReconcileIsRequired() throws Exception {
        int databaseSizeBeforeTest = pcBinRepository.findAll().size();
        // set the field null
        pcBin.setUseReconcile(null);

        // Create the PcBin, which fails.
        PcBinDTO pcBinDTO = pcBinMapper.toDto(pcBin);

        restPcBinMockMvc.perform(post("/api/pc-bins")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(pcBinDTO)))
            .andExpect(status().isBadRequest());

        List<PcBin> pcBinList = pcBinRepository.findAll();
        assertThat(pcBinList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void getAllPcBins() throws Exception {
        // Initialize the database
        pcBinRepository.saveAndFlush(pcBin);

        // Get all the pcBinList
        restPcBinMockMvc.perform(get("/api/pc-bins?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(pcBin.getId().intValue())))
            .andExpect(jsonPath("$.[*].bin").value(hasItem(DEFAULT_BIN.toString())))
            .andExpect(jsonPath("$.[*].bankName").value(hasItem(DEFAULT_BANK_NAME.toString())))
            .andExpect(jsonPath("$.[*].pcType").value(hasItem(DEFAULT_PC_TYPE.toString())))
            .andExpect(jsonPath("$.[*].useCredit").value(hasItem(DEFAULT_USE_CREDIT.booleanValue())))
            .andExpect(jsonPath("$.[*].useDebit").value(hasItem(DEFAULT_USE_DEBIT.booleanValue())))
            .andExpect(jsonPath("$.[*].useReverse").value(hasItem(DEFAULT_USE_REVERSE.booleanValue())))
            .andExpect(jsonPath("$.[*].useReconcile").value(hasItem(DEFAULT_USE_RECONCILE.booleanValue())))
            .andExpect(jsonPath("$.[*].comment").value(hasItem(DEFAULT_COMMENT.toString())));
    }
    
    @Test
    @Transactional
    public void getPcBin() throws Exception {
        // Initialize the database
        pcBinRepository.saveAndFlush(pcBin);

        // Get the pcBin
        restPcBinMockMvc.perform(get("/api/pc-bins/{id}", pcBin.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.id").value(pcBin.getId().intValue()))
            .andExpect(jsonPath("$.bin").value(DEFAULT_BIN.toString()))
            .andExpect(jsonPath("$.bankName").value(DEFAULT_BANK_NAME.toString()))
            .andExpect(jsonPath("$.pcType").value(DEFAULT_PC_TYPE.toString()))
            .andExpect(jsonPath("$.useCredit").value(DEFAULT_USE_CREDIT.booleanValue()))
            .andExpect(jsonPath("$.useDebit").value(DEFAULT_USE_DEBIT.booleanValue()))
            .andExpect(jsonPath("$.useReverse").value(DEFAULT_USE_REVERSE.booleanValue()))
            .andExpect(jsonPath("$.useReconcile").value(DEFAULT_USE_RECONCILE.booleanValue()))
            .andExpect(jsonPath("$.comment").value(DEFAULT_COMMENT.toString()));
    }

    @Test
    @Transactional
    public void getAllPcBinsByBinIsEqualToSomething() throws Exception {
        // Initialize the database
        pcBinRepository.saveAndFlush(pcBin);

        // Get all the pcBinList where bin equals to DEFAULT_BIN
        defaultPcBinShouldBeFound("bin.equals=" + DEFAULT_BIN);

        // Get all the pcBinList where bin equals to UPDATED_BIN
        defaultPcBinShouldNotBeFound("bin.equals=" + UPDATED_BIN);
    }

    @Test
    @Transactional
    public void getAllPcBinsByBinIsInShouldWork() throws Exception {
        // Initialize the database
        pcBinRepository.saveAndFlush(pcBin);

        // Get all the pcBinList where bin in DEFAULT_BIN or UPDATED_BIN
        defaultPcBinShouldBeFound("bin.in=" + DEFAULT_BIN + "," + UPDATED_BIN);

        // Get all the pcBinList where bin equals to UPDATED_BIN
        defaultPcBinShouldNotBeFound("bin.in=" + UPDATED_BIN);
    }

    @Test
    @Transactional
    public void getAllPcBinsByBinIsNullOrNotNull() throws Exception {
        // Initialize the database
        pcBinRepository.saveAndFlush(pcBin);

        // Get all the pcBinList where bin is not null
        defaultPcBinShouldBeFound("bin.specified=true");

        // Get all the pcBinList where bin is null
        defaultPcBinShouldNotBeFound("bin.specified=false");
    }

    @Test
    @Transactional
    public void getAllPcBinsByBankNameIsEqualToSomething() throws Exception {
        // Initialize the database
        pcBinRepository.saveAndFlush(pcBin);

        // Get all the pcBinList where bankName equals to DEFAULT_BANK_NAME
        defaultPcBinShouldBeFound("bankName.equals=" + DEFAULT_BANK_NAME);

        // Get all the pcBinList where bankName equals to UPDATED_BANK_NAME
        defaultPcBinShouldNotBeFound("bankName.equals=" + UPDATED_BANK_NAME);
    }

    @Test
    @Transactional
    public void getAllPcBinsByBankNameIsInShouldWork() throws Exception {
        // Initialize the database
        pcBinRepository.saveAndFlush(pcBin);

        // Get all the pcBinList where bankName in DEFAULT_BANK_NAME or UPDATED_BANK_NAME
        defaultPcBinShouldBeFound("bankName.in=" + DEFAULT_BANK_NAME + "," + UPDATED_BANK_NAME);

        // Get all the pcBinList where bankName equals to UPDATED_BANK_NAME
        defaultPcBinShouldNotBeFound("bankName.in=" + UPDATED_BANK_NAME);
    }

    @Test
    @Transactional
    public void getAllPcBinsByBankNameIsNullOrNotNull() throws Exception {
        // Initialize the database
        pcBinRepository.saveAndFlush(pcBin);

        // Get all the pcBinList where bankName is not null
        defaultPcBinShouldBeFound("bankName.specified=true");

        // Get all the pcBinList where bankName is null
        defaultPcBinShouldNotBeFound("bankName.specified=false");
    }

    @Test
    @Transactional
    public void getAllPcBinsByPcTypeIsEqualToSomething() throws Exception {
        // Initialize the database
        pcBinRepository.saveAndFlush(pcBin);

        // Get all the pcBinList where pcType equals to DEFAULT_PC_TYPE
        defaultPcBinShouldBeFound("pcType.equals=" + DEFAULT_PC_TYPE);

        // Get all the pcBinList where pcType equals to UPDATED_PC_TYPE
        defaultPcBinShouldNotBeFound("pcType.equals=" + UPDATED_PC_TYPE);
    }

    @Test
    @Transactional
    public void getAllPcBinsByPcTypeIsInShouldWork() throws Exception {
        // Initialize the database
        pcBinRepository.saveAndFlush(pcBin);

        // Get all the pcBinList where pcType in DEFAULT_PC_TYPE or UPDATED_PC_TYPE
        defaultPcBinShouldBeFound("pcType.in=" + DEFAULT_PC_TYPE + "," + UPDATED_PC_TYPE);

        // Get all the pcBinList where pcType equals to UPDATED_PC_TYPE
        defaultPcBinShouldNotBeFound("pcType.in=" + UPDATED_PC_TYPE);
    }

    @Test
    @Transactional
    public void getAllPcBinsByPcTypeIsNullOrNotNull() throws Exception {
        // Initialize the database
        pcBinRepository.saveAndFlush(pcBin);

        // Get all the pcBinList where pcType is not null
        defaultPcBinShouldBeFound("pcType.specified=true");

        // Get all the pcBinList where pcType is null
        defaultPcBinShouldNotBeFound("pcType.specified=false");
    }

    @Test
    @Transactional
    public void getAllPcBinsByUseCreditIsEqualToSomething() throws Exception {
        // Initialize the database
        pcBinRepository.saveAndFlush(pcBin);

        // Get all the pcBinList where useCredit equals to DEFAULT_USE_CREDIT
        defaultPcBinShouldBeFound("useCredit.equals=" + DEFAULT_USE_CREDIT);

        // Get all the pcBinList where useCredit equals to UPDATED_USE_CREDIT
        defaultPcBinShouldNotBeFound("useCredit.equals=" + UPDATED_USE_CREDIT);
    }

    @Test
    @Transactional
    public void getAllPcBinsByUseCreditIsInShouldWork() throws Exception {
        // Initialize the database
        pcBinRepository.saveAndFlush(pcBin);

        // Get all the pcBinList where useCredit in DEFAULT_USE_CREDIT or UPDATED_USE_CREDIT
        defaultPcBinShouldBeFound("useCredit.in=" + DEFAULT_USE_CREDIT + "," + UPDATED_USE_CREDIT);

        // Get all the pcBinList where useCredit equals to UPDATED_USE_CREDIT
        defaultPcBinShouldNotBeFound("useCredit.in=" + UPDATED_USE_CREDIT);
    }

    @Test
    @Transactional
    public void getAllPcBinsByUseCreditIsNullOrNotNull() throws Exception {
        // Initialize the database
        pcBinRepository.saveAndFlush(pcBin);

        // Get all the pcBinList where useCredit is not null
        defaultPcBinShouldBeFound("useCredit.specified=true");

        // Get all the pcBinList where useCredit is null
        defaultPcBinShouldNotBeFound("useCredit.specified=false");
    }

    @Test
    @Transactional
    public void getAllPcBinsByUseDebitIsEqualToSomething() throws Exception {
        // Initialize the database
        pcBinRepository.saveAndFlush(pcBin);

        // Get all the pcBinList where useDebit equals to DEFAULT_USE_DEBIT
        defaultPcBinShouldBeFound("useDebit.equals=" + DEFAULT_USE_DEBIT);

        // Get all the pcBinList where useDebit equals to UPDATED_USE_DEBIT
        defaultPcBinShouldNotBeFound("useDebit.equals=" + UPDATED_USE_DEBIT);
    }

    @Test
    @Transactional
    public void getAllPcBinsByUseDebitIsInShouldWork() throws Exception {
        // Initialize the database
        pcBinRepository.saveAndFlush(pcBin);

        // Get all the pcBinList where useDebit in DEFAULT_USE_DEBIT or UPDATED_USE_DEBIT
        defaultPcBinShouldBeFound("useDebit.in=" + DEFAULT_USE_DEBIT + "," + UPDATED_USE_DEBIT);

        // Get all the pcBinList where useDebit equals to UPDATED_USE_DEBIT
        defaultPcBinShouldNotBeFound("useDebit.in=" + UPDATED_USE_DEBIT);
    }

    @Test
    @Transactional
    public void getAllPcBinsByUseDebitIsNullOrNotNull() throws Exception {
        // Initialize the database
        pcBinRepository.saveAndFlush(pcBin);

        // Get all the pcBinList where useDebit is not null
        defaultPcBinShouldBeFound("useDebit.specified=true");

        // Get all the pcBinList where useDebit is null
        defaultPcBinShouldNotBeFound("useDebit.specified=false");
    }

    @Test
    @Transactional
    public void getAllPcBinsByUseReverseIsEqualToSomething() throws Exception {
        // Initialize the database
        pcBinRepository.saveAndFlush(pcBin);

        // Get all the pcBinList where useReverse equals to DEFAULT_USE_REVERSE
        defaultPcBinShouldBeFound("useReverse.equals=" + DEFAULT_USE_REVERSE);

        // Get all the pcBinList where useReverse equals to UPDATED_USE_REVERSE
        defaultPcBinShouldNotBeFound("useReverse.equals=" + UPDATED_USE_REVERSE);
    }

    @Test
    @Transactional
    public void getAllPcBinsByUseReverseIsInShouldWork() throws Exception {
        // Initialize the database
        pcBinRepository.saveAndFlush(pcBin);

        // Get all the pcBinList where useReverse in DEFAULT_USE_REVERSE or UPDATED_USE_REVERSE
        defaultPcBinShouldBeFound("useReverse.in=" + DEFAULT_USE_REVERSE + "," + UPDATED_USE_REVERSE);

        // Get all the pcBinList where useReverse equals to UPDATED_USE_REVERSE
        defaultPcBinShouldNotBeFound("useReverse.in=" + UPDATED_USE_REVERSE);
    }

    @Test
    @Transactional
    public void getAllPcBinsByUseReverseIsNullOrNotNull() throws Exception {
        // Initialize the database
        pcBinRepository.saveAndFlush(pcBin);

        // Get all the pcBinList where useReverse is not null
        defaultPcBinShouldBeFound("useReverse.specified=true");

        // Get all the pcBinList where useReverse is null
        defaultPcBinShouldNotBeFound("useReverse.specified=false");
    }

    @Test
    @Transactional
    public void getAllPcBinsByUseReconcileIsEqualToSomething() throws Exception {
        // Initialize the database
        pcBinRepository.saveAndFlush(pcBin);

        // Get all the pcBinList where useReconcile equals to DEFAULT_USE_RECONCILE
        defaultPcBinShouldBeFound("useReconcile.equals=" + DEFAULT_USE_RECONCILE);

        // Get all the pcBinList where useReconcile equals to UPDATED_USE_RECONCILE
        defaultPcBinShouldNotBeFound("useReconcile.equals=" + UPDATED_USE_RECONCILE);
    }

    @Test
    @Transactional
    public void getAllPcBinsByUseReconcileIsInShouldWork() throws Exception {
        // Initialize the database
        pcBinRepository.saveAndFlush(pcBin);

        // Get all the pcBinList where useReconcile in DEFAULT_USE_RECONCILE or UPDATED_USE_RECONCILE
        defaultPcBinShouldBeFound("useReconcile.in=" + DEFAULT_USE_RECONCILE + "," + UPDATED_USE_RECONCILE);

        // Get all the pcBinList where useReconcile equals to UPDATED_USE_RECONCILE
        defaultPcBinShouldNotBeFound("useReconcile.in=" + UPDATED_USE_RECONCILE);
    }

    @Test
    @Transactional
    public void getAllPcBinsByUseReconcileIsNullOrNotNull() throws Exception {
        // Initialize the database
        pcBinRepository.saveAndFlush(pcBin);

        // Get all the pcBinList where useReconcile is not null
        defaultPcBinShouldBeFound("useReconcile.specified=true");

        // Get all the pcBinList where useReconcile is null
        defaultPcBinShouldNotBeFound("useReconcile.specified=false");
    }

    @Test
    @Transactional
    public void getAllPcBinsByCommentIsEqualToSomething() throws Exception {
        // Initialize the database
        pcBinRepository.saveAndFlush(pcBin);

        // Get all the pcBinList where comment equals to DEFAULT_COMMENT
        defaultPcBinShouldBeFound("comment.equals=" + DEFAULT_COMMENT);

        // Get all the pcBinList where comment equals to UPDATED_COMMENT
        defaultPcBinShouldNotBeFound("comment.equals=" + UPDATED_COMMENT);
    }

    @Test
    @Transactional
    public void getAllPcBinsByCommentIsInShouldWork() throws Exception {
        // Initialize the database
        pcBinRepository.saveAndFlush(pcBin);

        // Get all the pcBinList where comment in DEFAULT_COMMENT or UPDATED_COMMENT
        defaultPcBinShouldBeFound("comment.in=" + DEFAULT_COMMENT + "," + UPDATED_COMMENT);

        // Get all the pcBinList where comment equals to UPDATED_COMMENT
        defaultPcBinShouldNotBeFound("comment.in=" + UPDATED_COMMENT);
    }

    @Test
    @Transactional
    public void getAllPcBinsByCommentIsNullOrNotNull() throws Exception {
        // Initialize the database
        pcBinRepository.saveAndFlush(pcBin);

        // Get all the pcBinList where comment is not null
        defaultPcBinShouldBeFound("comment.specified=true");

        // Get all the pcBinList where comment is null
        defaultPcBinShouldNotBeFound("comment.specified=false");
    }
    /**
     * Executes the search, and checks that the default entity is returned
     */
    private void defaultPcBinShouldBeFound(String filter) throws Exception {
        restPcBinMockMvc.perform(get("/api/pc-bins?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(pcBin.getId().intValue())))
            .andExpect(jsonPath("$.[*].bin").value(hasItem(DEFAULT_BIN.toString())))
            .andExpect(jsonPath("$.[*].bankName").value(hasItem(DEFAULT_BANK_NAME.toString())))
            .andExpect(jsonPath("$.[*].pcType").value(hasItem(DEFAULT_PC_TYPE.toString())))
            .andExpect(jsonPath("$.[*].useCredit").value(hasItem(DEFAULT_USE_CREDIT.booleanValue())))
            .andExpect(jsonPath("$.[*].useDebit").value(hasItem(DEFAULT_USE_DEBIT.booleanValue())))
            .andExpect(jsonPath("$.[*].useReverse").value(hasItem(DEFAULT_USE_REVERSE.booleanValue())))
            .andExpect(jsonPath("$.[*].useReconcile").value(hasItem(DEFAULT_USE_RECONCILE.booleanValue())))
            .andExpect(jsonPath("$.[*].comment").value(hasItem(DEFAULT_COMMENT.toString())));

        // Check, that the count call also returns 1
        restPcBinMockMvc.perform(get("/api/pc-bins/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(content().string("1"));
    }

    /**
     * Executes the search, and checks that the default entity is not returned
     */
    private void defaultPcBinShouldNotBeFound(String filter) throws Exception {
        restPcBinMockMvc.perform(get("/api/pc-bins?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty());

        // Check, that the count call also returns 0
        restPcBinMockMvc.perform(get("/api/pc-bins/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(content().string("0"));
    }


    @Test
    @Transactional
    public void getNonExistingPcBin() throws Exception {
        // Get the pcBin
        restPcBinMockMvc.perform(get("/api/pc-bins/{id}", Long.MAX_VALUE))
            .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updatePcBin() throws Exception {
        // Initialize the database
        pcBinRepository.saveAndFlush(pcBin);

        int databaseSizeBeforeUpdate = pcBinRepository.findAll().size();

        // Update the pcBin
        PcBin updatedPcBin = pcBinRepository.findById(pcBin.getId()).get();
        // Disconnect from session so that the updates on updatedPcBin are not directly saved in db
        em.detach(updatedPcBin);
        updatedPcBin
            .bin(UPDATED_BIN)
            .bankName(UPDATED_BANK_NAME)
            .pcType(UPDATED_PC_TYPE)
            .useCredit(UPDATED_USE_CREDIT)
            .useDebit(UPDATED_USE_DEBIT)
            .useReverse(UPDATED_USE_REVERSE)
            .useReconcile(UPDATED_USE_RECONCILE)
            .comment(UPDATED_COMMENT);
        PcBinDTO pcBinDTO = pcBinMapper.toDto(updatedPcBin);

        restPcBinMockMvc.perform(put("/api/pc-bins")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(pcBinDTO)))
            .andExpect(status().isOk());

        // Validate the PcBin in the database
        List<PcBin> pcBinList = pcBinRepository.findAll();
        assertThat(pcBinList).hasSize(databaseSizeBeforeUpdate);
        PcBin testPcBin = pcBinList.get(pcBinList.size() - 1);
        assertThat(testPcBin.getBin()).isEqualTo(UPDATED_BIN);
        assertThat(testPcBin.getBankName()).isEqualTo(UPDATED_BANK_NAME);
        assertThat(testPcBin.getPcType()).isEqualTo(UPDATED_PC_TYPE);
        assertThat(testPcBin.isUseCredit()).isEqualTo(UPDATED_USE_CREDIT);
        assertThat(testPcBin.isUseDebit()).isEqualTo(UPDATED_USE_DEBIT);
        assertThat(testPcBin.isUseReverse()).isEqualTo(UPDATED_USE_REVERSE);
        assertThat(testPcBin.isUseReconcile()).isEqualTo(UPDATED_USE_RECONCILE);
        assertThat(testPcBin.getComment()).isEqualTo(UPDATED_COMMENT);
    }

    @Test
    @Transactional
    public void updateNonExistingPcBin() throws Exception {
        int databaseSizeBeforeUpdate = pcBinRepository.findAll().size();

        // Create the PcBin
        PcBinDTO pcBinDTO = pcBinMapper.toDto(pcBin);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restPcBinMockMvc.perform(put("/api/pc-bins")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(pcBinDTO)))
            .andExpect(status().isBadRequest());

        // Validate the PcBin in the database
        List<PcBin> pcBinList = pcBinRepository.findAll();
        assertThat(pcBinList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    public void deletePcBin() throws Exception {
        // Initialize the database
        pcBinRepository.saveAndFlush(pcBin);

        int databaseSizeBeforeDelete = pcBinRepository.findAll().size();

        // Get the pcBin
        restPcBinMockMvc.perform(delete("/api/pc-bins/{id}", pcBin.getId())
            .accept(TestUtil.APPLICATION_JSON_UTF8))
            .andExpect(status().isOk());

        // Validate the database is empty
        List<PcBin> pcBinList = pcBinRepository.findAll();
        assertThat(pcBinList).hasSize(databaseSizeBeforeDelete - 1);
    }

    @Test
    @Transactional
    public void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(PcBin.class);
        PcBin pcBin1 = new PcBin();
        pcBin1.setId(1L);
        PcBin pcBin2 = new PcBin();
        pcBin2.setId(pcBin1.getId());
        assertThat(pcBin1).isEqualTo(pcBin2);
        pcBin2.setId(2L);
        assertThat(pcBin1).isNotEqualTo(pcBin2);
        pcBin1.setId(null);
        assertThat(pcBin1).isNotEqualTo(pcBin2);
    }

    @Test
    @Transactional
    public void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(PcBinDTO.class);
        PcBinDTO pcBinDTO1 = new PcBinDTO();
        pcBinDTO1.setId(1L);
        PcBinDTO pcBinDTO2 = new PcBinDTO();
        assertThat(pcBinDTO1).isNotEqualTo(pcBinDTO2);
        pcBinDTO2.setId(pcBinDTO1.getId());
        assertThat(pcBinDTO1).isEqualTo(pcBinDTO2);
        pcBinDTO2.setId(2L);
        assertThat(pcBinDTO1).isNotEqualTo(pcBinDTO2);
        pcBinDTO1.setId(null);
        assertThat(pcBinDTO1).isNotEqualTo(pcBinDTO2);
    }

    @Test
    @Transactional
    public void testEntityFromId() {
        assertThat(pcBinMapper.fromId(42L).getId()).isEqualTo(42);
        assertThat(pcBinMapper.fromId(null)).isNull();
    }
}
