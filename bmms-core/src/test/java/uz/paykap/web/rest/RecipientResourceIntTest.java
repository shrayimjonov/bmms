package uz.paykap.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static uz.paykap.web.rest.TestUtil.createFormattingConversionService;

import java.util.ArrayList;
import java.util.List;
import javax.persistence.EntityManager;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import uz.paykap.IntegrationTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.Validator;
import uz.devops.bm.dummy.adapter.DummyDataAdapterV3;
import uz.devops.bmcore.enumeration.RecipientType;
import uz.paykap.PkbillmsApp;
import uz.paykap.adapter.DummyAdapterDelegate;
import uz.paykap.IntegrationTest;
import uz.paykap.domain.Category;
import uz.paykap.domain.Gateway;
import uz.paykap.domain.I18n;
import uz.paykap.domain.Recipient;
import uz.paykap.repository.CategoryRepository;
import uz.paykap.repository.GatewayRepository;
import uz.paykap.repository.RecipientRepository;
import uz.paykap.service.RecipientQueryService;
import uz.paykap.service.RecipientService;
import uz.paykap.service.dto.RecipientDTO;
import uz.paykap.service.mapper.RecipientMapper;
import uz.paykap.web.rest.errors.ExceptionTranslator;
import uz.paykap.web.rest.util.DummyUtils;

/**
 * Test class for the RecipientResource REST controller.
 *
 * @see RecipientResource
 */
@RunWith(SpringRunner.class)
@IntegrationTest
public class RecipientResourceIntTest {

    private static final String DEFAULT_SHORT_NAME = "AAAAAAAAAA";
    private static final String UPDATED_SHORT_NAME = "BBBBBBBBBB";

    private static final String DEFAULT_SERVICE_ID = "AAAAAAAAAA";
    private static final String UPDATED_SERVICE_ID = "BBBBBBBBBB";

    private static final String DEFAULT_THUMBNAIL = "AAAAAAAAAA";
    private static final String UPDATED_THUMBNAIL = "BBBBBBBBBB";

    private static final String DEFAULT_LOGO = "AAAAAAAAAA";
    private static final String UPDATED_LOGO = "BBBBBBBBBB";

    private static final Integer DEFAULT_AMOUNT_RATIO = 1;
    private static final Integer UPDATED_AMOUNT_RATIO = 2;

    private static final Integer DEFAULT_CCY = 1;
    private static final Integer UPDATED_CCY = 2;

    private static final Long DEFAULT_MIN_AMOUNT = 1L;
    private static final Long UPDATED_MIN_AMOUNT = 2L;

    private static final Long DEFAULT_MAX_AMOUNT = 1L;
    private static final Long UPDATED_MAX_AMOUNT = 2L;

    private static final Integer DEFAULT_LIMIT_RATIO = 1;
    private static final Integer UPDATED_LIMIT_RATIO = 2;

    private static final Double DEFAULT_COMMISSION = 1D;
    private static final Double UPDATED_COMMISSION = 2D;

    private static final Double DEFAULT_FEE = 1D;
    private static final Double UPDATED_FEE = 2D;

    private static final Double DEFAULT_SYSTEM_COMMISSION = 1D;
    private static final Double UPDATED_SYSTEM_COMMISSION = 2D;

    private static final Double DEFAULT_SYSTEM_FEE = 1D;
    private static final Double UPDATED_SYSTEM_FEE = 2D;

    private static final Double DEFAULT_SENDER_COMMISSION = 1D;
    private static final Double UPDATED_SENDER_COMMISSION = 2D;

    private static final Integer DEFAULT_ORD = 1;
    private static final Integer UPDATED_ORD = 2;

    private static final Boolean DEFAULT_ACTIVE = false;
    private static final Boolean UPDATED_ACTIVE = true;

    private static final Boolean DEFAULT_CHECK = false;
    private static final Boolean UPDATED_CHECK = true;

    private static final Boolean DEFAULT_PAY = false;
    private static final Boolean UPDATED_PAY = true;

    private static final Boolean DEFAULT_STATUS = false;
    private static final Boolean UPDATED_STATUS = true;

    private static final Boolean DEFAULT_CANCEL = false;
    private static final Boolean UPDATED_CANCEL = true;

    private static final Boolean DEFAULT_CAN_DEPOSIT = false;
    private static final Boolean UPDATED_CAN_DEPOSIT = true;

    private static final Integer DEFAULT_EPOS_PORT = 64000;
    private static final Integer UPDATED_EPOS_PORT = 63999;

    private static final Long DEFAULT_EPOS_MERCHANT = 1L;
    private static final Long UPDATED_EPOS_MERCHANT = 2L;

    private static final Long DEFAULT_EPOS_TERMINAL = 1L;
    private static final Long UPDATED_EPOS_TERMINAL = 2L;

    private static final String DEFAULT_SETTLEMENT_ACCOUNT = "AAAAAAAAAA";
    private static final String UPDATED_SETTLEMENT_ACCOUNT = "BBBBBBBBBB";

    private static final String DEFAULT_MFO = "AAAAAAAAAA";
    private static final String UPDATED_MFO = "BBBBBBBBBB";

    private static final String DEFAULT_INN = "AAAAAAAAAA";
    private static final String UPDATED_INN = "BBBBBBBBBB";

    private static final Boolean DEFAULT_RECONCILE = false;
    private static final Boolean UPDATED_RECONCILE = true;

    private static final RecipientType DEFAULT_TYPE = RecipientType.SIMPLE;
    private static final RecipientType UPDATED_TYPE = RecipientType.GIFT;

    @Autowired
    private RecipientRepository recipientRepository;

    @Mock
    private RecipientRepository recipientRepositoryMock;

    @Autowired
    private RecipientMapper recipientMapper;

    @Mock
    private RecipientService recipientServiceMock;

    @Autowired
    private RecipientService recipientService;

    @Autowired
    private RecipientQueryService recipientQueryService;

    @Autowired
    private MappingJackson2HttpMessageConverter jacksonMessageConverter;

    @Autowired
    private PageableHandlerMethodArgumentResolver pageableArgumentResolver;

    @Autowired
    private ExceptionTranslator exceptionTranslator;

    @Autowired
    private EntityManager em;

    @Autowired
    private GatewayRepository gatewayRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private Validator validator;

    @Autowired
    private DummyAdapterDelegate dummyAdapterDelegate;

    private MockMvc restRecipientMockMvc;

    private Recipient recipient;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        final RecipientResource recipientResource = new RecipientResource(
            recipientService,
            recipientRepository,
            recipientQueryService,
            categoryRepository,
            gatewayRepository
        );

        this.restRecipientMockMvc =
            MockMvcBuilders
                .standaloneSetup(recipientResource)
                .setCustomArgumentResolvers(pageableArgumentResolver)
                .setControllerAdvice(exceptionTranslator)
                .setConversionService(createFormattingConversionService())
                .setMessageConverters(jacksonMessageConverter)
                .setValidator(validator)
                .build();
    }

    /**
     * Create an entity for this test.
     * <p>
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Recipient createEntity(EntityManager em) {
        Recipient recipient = new Recipient()
            .shortName(DEFAULT_SHORT_NAME)
            .serviceId(DEFAULT_SERVICE_ID)
            .thumbnail(DEFAULT_THUMBNAIL)
            .logo(DEFAULT_LOGO)
            .amountRatio(DEFAULT_AMOUNT_RATIO)
            .ccy(DEFAULT_CCY)
            .minAmount(DEFAULT_MIN_AMOUNT)
            .maxAmount(DEFAULT_MAX_AMOUNT)
            .limitRatio(DEFAULT_LIMIT_RATIO)
            .commission(DEFAULT_COMMISSION)
            .fee(DEFAULT_FEE)
            .systemCommission(DEFAULT_SYSTEM_COMMISSION)
            .systemFee(DEFAULT_SYSTEM_FEE)
            .senderCommission(DEFAULT_SENDER_COMMISSION)
            .ord(DEFAULT_ORD)
            .active(DEFAULT_ACTIVE)
            .check(DEFAULT_CHECK)
            .pay(DEFAULT_PAY)
            .status(DEFAULT_STATUS)
            .cancel(DEFAULT_CANCEL)
            .canDeposit(DEFAULT_CAN_DEPOSIT)
//            .eposPort(DEFAULT_EPOS_PORT)
//            .eposMerchant(DEFAULT_EPOS_MERCHANT)
//            .eposTerminal(DEFAULT_EPOS_TERMINAL)
            .settlementAccount(DEFAULT_SETTLEMENT_ACCOUNT)
            .mfo(DEFAULT_MFO)
            .inn(DEFAULT_INN)
            .reconcile(DEFAULT_RECONCILE)
            .type(DEFAULT_TYPE);
        // Add required entity
        I18n i18n = I18nResourceIntTest.createEntity(em);
        //        em.persist(i18n);
        //        em.flush();
        recipient.getTitles().add(i18n);
        // Add required entity
        recipient.getFullNames().add(i18n);
        return recipient;
    }

    @Before
    public void initTest() {
        recipient = createEntity(em);
    }

    //    @Test
    @Transactional
    public void createRecipient() throws Exception {
        int databaseSizeBeforeCreate = recipientRepository.findAll().size();

        // Create the Recipient
        RecipientDTO recipientDTO = recipientMapper.toDto(recipient);
        restRecipientMockMvc
            .perform(
                post("/api/recipients").contentType(TestUtil.APPLICATION_JSON_UTF8).content(TestUtil.convertObjectToJsonBytes(recipientDTO))
            )
            .andExpect(status().isCreated());

        // Validate the Recipient in the database
        List<Recipient> recipientList = recipientRepository.findAll();
        assertThat(recipientList).hasSize(databaseSizeBeforeCreate + 1);
        Recipient testRecipient = recipientList.get(recipientList.size() - 1);
        assertThat(testRecipient.getShortName()).isEqualTo(DEFAULT_SHORT_NAME);
        assertThat(testRecipient.getServiceId()).isEqualTo(DEFAULT_SERVICE_ID);
        assertThat(testRecipient.getThumbnail()).isEqualTo(DEFAULT_THUMBNAIL);
        assertThat(testRecipient.getLogo()).isEqualTo(DEFAULT_LOGO);
        assertThat(testRecipient.getAmountRatio()).isEqualTo(DEFAULT_AMOUNT_RATIO);
        assertThat(testRecipient.getCcy()).isEqualTo(DEFAULT_CCY);
        assertThat(testRecipient.getMinAmount()).isEqualTo(DEFAULT_MIN_AMOUNT);
        assertThat(testRecipient.getMaxAmount()).isEqualTo(DEFAULT_MAX_AMOUNT);
        assertThat(testRecipient.getLimitRatio()).isEqualTo(DEFAULT_LIMIT_RATIO);
        assertThat(testRecipient.getCommission()).isEqualTo(DEFAULT_COMMISSION);
        assertThat(testRecipient.getFee()).isEqualTo(DEFAULT_FEE);
        assertThat(testRecipient.getSystemCommission()).isEqualTo(DEFAULT_SYSTEM_COMMISSION);
        assertThat(testRecipient.getSystemFee()).isEqualTo(DEFAULT_SYSTEM_FEE);
        assertThat(testRecipient.getSenderCommission()).isEqualTo(DEFAULT_SENDER_COMMISSION);
        assertThat(testRecipient.getOrd()).isEqualTo(DEFAULT_ORD);
        assertThat(testRecipient.isActive()).isEqualTo(DEFAULT_ACTIVE);
        assertThat(testRecipient.isCheck()).isEqualTo(DEFAULT_CHECK);
        assertThat(testRecipient.isPay()).isEqualTo(DEFAULT_PAY);
        assertThat(testRecipient.isStatus()).isEqualTo(DEFAULT_STATUS);
        assertThat(testRecipient.isCancel()).isEqualTo(DEFAULT_CANCEL);
        assertThat(testRecipient.isCanDeposit()).isEqualTo(DEFAULT_CAN_DEPOSIT);
//        assertThat(testRecipient.getEposPort()).isEqualTo(DEFAULT_EPOS_PORT);
//        assertThat(testRecipient.getEposMerchant()).isEqualTo(DEFAULT_EPOS_MERCHANT);
//        assertThat(testRecipient.getEposTerminal()).isEqualTo(DEFAULT_EPOS_TERMINAL);
        assertThat(testRecipient.getSettlementAccount()).isEqualTo(DEFAULT_SETTLEMENT_ACCOUNT);
        assertThat(testRecipient.getMfo()).isEqualTo(DEFAULT_MFO);
        assertThat(testRecipient.getInn()).isEqualTo(DEFAULT_INN);
        assertThat(testRecipient.isReconcile()).isEqualTo(DEFAULT_RECONCILE);
        assertThat(testRecipient.getType()).isEqualTo(DEFAULT_TYPE);
    }

    @Test
    @Transactional
    public void createRecipientWithExistingId() throws Exception {
        int databaseSizeBeforeCreate = recipientRepository.findAll().size();

        // Create the Recipient with an existing ID
        recipient.setId(1L);
        RecipientDTO recipientDTO = recipientMapper.toDto(recipient);

        // An entity with an existing ID cannot be created, so this API call must fail
        restRecipientMockMvc
            .perform(
                post("/api/recipients").contentType(TestUtil.APPLICATION_JSON_UTF8).content(TestUtil.convertObjectToJsonBytes(recipientDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Recipient in the database
        List<Recipient> recipientList = recipientRepository.findAll();
        assertThat(recipientList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    public void checkShortNameIsRequired() throws Exception {
        int databaseSizeBeforeTest = recipientRepository.findAll().size();
        // set the field null
        recipient.setShortName(null);

        // Create the Recipient, which fails.
        RecipientDTO recipientDTO = recipientMapper.toDto(recipient);

        restRecipientMockMvc
            .perform(
                post("/api/recipients").contentType(TestUtil.APPLICATION_JSON_UTF8).content(TestUtil.convertObjectToJsonBytes(recipientDTO))
            )
            .andExpect(status().isBadRequest());

        List<Recipient> recipientList = recipientRepository.findAll();
        assertThat(recipientList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkServiceIdIsRequired() throws Exception {
        int databaseSizeBeforeTest = recipientRepository.findAll().size();
        // set the field null
        recipient.setServiceId(null);

        // Create the Recipient, which fails.
        RecipientDTO recipientDTO = recipientMapper.toDto(recipient);

        restRecipientMockMvc
            .perform(
                post("/api/recipients").contentType(TestUtil.APPLICATION_JSON_UTF8).content(TestUtil.convertObjectToJsonBytes(recipientDTO))
            )
            .andExpect(status().isBadRequest());

        List<Recipient> recipientList = recipientRepository.findAll();
        assertThat(recipientList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkAmountRatioIsRequired() throws Exception {
        int databaseSizeBeforeTest = recipientRepository.findAll().size();
        // set the field null
        recipient.setAmountRatio(null);

        // Create the Recipient, which fails.
        RecipientDTO recipientDTO = recipientMapper.toDto(recipient);

        restRecipientMockMvc
            .perform(
                post("/api/recipients").contentType(TestUtil.APPLICATION_JSON_UTF8).content(TestUtil.convertObjectToJsonBytes(recipientDTO))
            )
            .andExpect(status().isBadRequest());

        List<Recipient> recipientList = recipientRepository.findAll();
        assertThat(recipientList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkCcyIsRequired() throws Exception {
        int databaseSizeBeforeTest = recipientRepository.findAll().size();
        // set the field null
        recipient.setCcy(null);

        // Create the Recipient, which fails.
        RecipientDTO recipientDTO = recipientMapper.toDto(recipient);

        restRecipientMockMvc
            .perform(
                post("/api/recipients").contentType(TestUtil.APPLICATION_JSON_UTF8).content(TestUtil.convertObjectToJsonBytes(recipientDTO))
            )
            .andExpect(status().isBadRequest());

        List<Recipient> recipientList = recipientRepository.findAll();
        assertThat(recipientList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkLimitRatioIsRequired() throws Exception {
        int databaseSizeBeforeTest = recipientRepository.findAll().size();
        // set the field null
        recipient.setLimitRatio(null);

        // Create the Recipient, which fails.
        RecipientDTO recipientDTO = recipientMapper.toDto(recipient);

        restRecipientMockMvc
            .perform(
                post("/api/recipients").contentType(TestUtil.APPLICATION_JSON_UTF8).content(TestUtil.convertObjectToJsonBytes(recipientDTO))
            )
            .andExpect(status().isBadRequest());

        List<Recipient> recipientList = recipientRepository.findAll();
        assertThat(recipientList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkActiveIsRequired() throws Exception {
        int databaseSizeBeforeTest = recipientRepository.findAll().size();
        // set the field null
        recipient.setActive(null);

        // Create the Recipient, which fails.
        RecipientDTO recipientDTO = recipientMapper.toDto(recipient);

        restRecipientMockMvc
            .perform(
                post("/api/recipients").contentType(TestUtil.APPLICATION_JSON_UTF8).content(TestUtil.convertObjectToJsonBytes(recipientDTO))
            )
            .andExpect(status().isBadRequest());

        List<Recipient> recipientList = recipientRepository.findAll();
        assertThat(recipientList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void getAllRecipients() throws Exception {
        // Initialize the database
        recipientRepository.saveAndFlush(recipient);

        // Get all the recipientList
        restRecipientMockMvc
            .perform(get("/api/recipients?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(recipient.getId().intValue())))
            .andExpect(jsonPath("$.[*].shortName").value(hasItem(DEFAULT_SHORT_NAME)))
            .andExpect(jsonPath("$.[*].serviceId").value(hasItem(DEFAULT_SERVICE_ID)))
            .andExpect(jsonPath("$.[*].thumbnail").value(hasItem(DEFAULT_THUMBNAIL)))
            .andExpect(jsonPath("$.[*].logo").value(hasItem(DEFAULT_LOGO)))
            .andExpect(jsonPath("$.[*].amountRatio").value(hasItem(DEFAULT_AMOUNT_RATIO)))
            .andExpect(jsonPath("$.[*].ccy").value(hasItem(DEFAULT_CCY)))
            .andExpect(jsonPath("$.[*].minAmount").value(hasItem(DEFAULT_MIN_AMOUNT.intValue())))
            .andExpect(jsonPath("$.[*].maxAmount").value(hasItem(DEFAULT_MAX_AMOUNT.intValue())))
            .andExpect(jsonPath("$.[*].limitRatio").value(hasItem(DEFAULT_LIMIT_RATIO)))
            .andExpect(jsonPath("$.[*].commission").value(hasItem(DEFAULT_COMMISSION.doubleValue())))
            .andExpect(jsonPath("$.[*].fee").value(hasItem(DEFAULT_FEE.doubleValue())))
            .andExpect(jsonPath("$.[*].systemCommission").value(hasItem(DEFAULT_SYSTEM_COMMISSION.doubleValue())))
            .andExpect(jsonPath("$.[*].systemFee").value(hasItem(DEFAULT_SYSTEM_FEE.doubleValue())))
            .andExpect(jsonPath("$.[*].senderCommission").value(hasItem(DEFAULT_SENDER_COMMISSION.doubleValue())))
            .andExpect(jsonPath("$.[*].ord").value(hasItem(DEFAULT_ORD)))
            .andExpect(jsonPath("$.[*].active").value(hasItem(DEFAULT_ACTIVE.booleanValue())))
            .andExpect(jsonPath("$.[*].check").value(hasItem(DEFAULT_CHECK.booleanValue())))
            .andExpect(jsonPath("$.[*].pay").value(hasItem(DEFAULT_PAY.booleanValue())))
            .andExpect(jsonPath("$.[*].status").value(hasItem(DEFAULT_STATUS.booleanValue())))
            .andExpect(jsonPath("$.[*].cancel").value(hasItem(DEFAULT_CANCEL.booleanValue())))
            .andExpect(jsonPath("$.[*].canDeposit").value(hasItem(DEFAULT_CAN_DEPOSIT.booleanValue())))
            .andExpect(jsonPath("$.[*].eposPort").value(hasItem(DEFAULT_EPOS_PORT)))
            .andExpect(jsonPath("$.[*].eposMerchant").value(hasItem(DEFAULT_EPOS_MERCHANT.intValue())))
            .andExpect(jsonPath("$.[*].eposTerminal").value(hasItem(DEFAULT_EPOS_TERMINAL.intValue())))
            .andExpect(jsonPath("$.[*].settlementAccount").value(hasItem(DEFAULT_SETTLEMENT_ACCOUNT)))
            .andExpect(jsonPath("$.[*].mfo").value(hasItem(DEFAULT_MFO)))
            .andExpect(jsonPath("$.[*].inn").value(hasItem(DEFAULT_INN)))
            .andExpect(jsonPath("$.[*].reconcile").value(hasItem(DEFAULT_RECONCILE.booleanValue())))
            .andExpect(jsonPath("$.[*].type").value(hasItem(DEFAULT_TYPE.toString())));
    }

    @Test
    @Transactional
    @WithMockUser(roles = { "ADMIN" })
    public void getAllowedRecipients() throws Exception {
        // Initialize the database
        //        recipientRepository.saveAndFlush(recipient);
        DummyUtils.prepareDummyData(dummyAdapterDelegate);

        // Get all the recipientList
        restRecipientMockMvc
            .perform(get("/api/recipients/allowed-names?recipientNamePart=usd&sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$").isNotEmpty());
    }

    @SuppressWarnings({ "unchecked" })
    public void getAllRecipientsWithEagerRelationshipsIsEnabled() throws Exception {
        RecipientResource recipientResource = new RecipientResource(
            recipientServiceMock,
            recipientRepository,
            recipientQueryService,
            categoryRepository,
            gatewayRepository
        );
        when(recipientServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        MockMvc restRecipientMockMvc = MockMvcBuilders
            .standaloneSetup(recipientResource)
            .setCustomArgumentResolvers(pageableArgumentResolver)
            .setControllerAdvice(exceptionTranslator)
            .setConversionService(createFormattingConversionService())
            .setMessageConverters(jacksonMessageConverter)
            .build();

        restRecipientMockMvc.perform(get("/api/recipients?eagerload=true")).andExpect(status().isOk());

        verify(recipientServiceMock, times(1)).findAllWithEagerRelationships(any());
    }

    @SuppressWarnings({ "unchecked" })
    public void getAllRecipientsWithEagerRelationshipsIsNotEnabled() throws Exception {
        RecipientResource recipientResource = new RecipientResource(
            recipientServiceMock,
            recipientRepository,
            recipientQueryService,
            categoryRepository,
            gatewayRepository
        );
        when(recipientServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));
        MockMvc restRecipientMockMvc = MockMvcBuilders
            .standaloneSetup(recipientResource)
            .setCustomArgumentResolvers(pageableArgumentResolver)
            .setControllerAdvice(exceptionTranslator)
            .setConversionService(createFormattingConversionService())
            .setMessageConverters(jacksonMessageConverter)
            .build();

        restRecipientMockMvc.perform(get("/api/recipients?eagerload=true")).andExpect(status().isOk());

        verify(recipientServiceMock, times(1)).findAllWithEagerRelationships(any());
    }

    @Test
    @Transactional
    public void getRecipient() throws Exception {
        // Initialize the database
        recipientRepository.saveAndFlush(recipient);

        // Get the recipient
        restRecipientMockMvc
            .perform(get("/api/recipients/{id}", recipient.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.id").value(recipient.getId().intValue()))
            .andExpect(jsonPath("$.shortName").value(DEFAULT_SHORT_NAME))
            .andExpect(jsonPath("$.serviceId").value(DEFAULT_SERVICE_ID))
            .andExpect(jsonPath("$.thumbnail").value(DEFAULT_THUMBNAIL))
            .andExpect(jsonPath("$.logo").value(DEFAULT_LOGO))
            .andExpect(jsonPath("$.amountRatio").value(DEFAULT_AMOUNT_RATIO))
            .andExpect(jsonPath("$.ccy").value(DEFAULT_CCY))
            .andExpect(jsonPath("$.minAmount").value(DEFAULT_MIN_AMOUNT.intValue()))
            .andExpect(jsonPath("$.maxAmount").value(DEFAULT_MAX_AMOUNT.intValue()))
            .andExpect(jsonPath("$.limitRatio").value(DEFAULT_LIMIT_RATIO))
            .andExpect(jsonPath("$.commission").value(DEFAULT_COMMISSION.doubleValue()))
            .andExpect(jsonPath("$.fee").value(DEFAULT_FEE.doubleValue()))
            .andExpect(jsonPath("$.systemCommission").value(DEFAULT_SYSTEM_COMMISSION.doubleValue()))
            .andExpect(jsonPath("$.systemFee").value(DEFAULT_SYSTEM_FEE.doubleValue()))
            .andExpect(jsonPath("$.senderCommission").value(DEFAULT_SENDER_COMMISSION.doubleValue()))
            .andExpect(jsonPath("$.ord").value(DEFAULT_ORD))
            .andExpect(jsonPath("$.active").value(DEFAULT_ACTIVE.booleanValue()))
            .andExpect(jsonPath("$.check").value(DEFAULT_CHECK.booleanValue()))
            .andExpect(jsonPath("$.pay").value(DEFAULT_PAY.booleanValue()))
            .andExpect(jsonPath("$.status").value(DEFAULT_STATUS.booleanValue()))
            .andExpect(jsonPath("$.cancel").value(DEFAULT_CANCEL.booleanValue()))
            .andExpect(jsonPath("$.canDeposit").value(DEFAULT_CAN_DEPOSIT.booleanValue()))
            .andExpect(jsonPath("$.eposPort").value(DEFAULT_EPOS_PORT))
            .andExpect(jsonPath("$.eposMerchant").value(DEFAULT_EPOS_MERCHANT.intValue()))
            .andExpect(jsonPath("$.eposTerminal").value(DEFAULT_EPOS_TERMINAL.intValue()))
            .andExpect(jsonPath("$.settlementAccount").value(DEFAULT_SETTLEMENT_ACCOUNT))
            .andExpect(jsonPath("$.mfo").value(DEFAULT_MFO))
            .andExpect(jsonPath("$.inn").value(DEFAULT_INN))
            .andExpect(jsonPath("$.reconcile").value(DEFAULT_RECONCILE.booleanValue()))
            .andExpect(jsonPath("$.type").value(DEFAULT_TYPE.toString()));
    }

    @Test
    @Transactional
    public void getAllRecipientsByShortNameIsEqualToSomething() throws Exception {
        // Initialize the database
        recipientRepository.saveAndFlush(recipient);

        // Get all the recipientList where shortName equals to DEFAULT_SHORT_NAME
        defaultRecipientShouldBeFound("shortName.equals=" + DEFAULT_SHORT_NAME);

        // Get all the recipientList where shortName equals to UPDATED_SHORT_NAME
        defaultRecipientShouldNotBeFound("shortName.equals=" + UPDATED_SHORT_NAME);
    }

    @Test
    @Transactional
    public void getAllRecipientsByShortNameIsInShouldWork() throws Exception {
        // Initialize the database
        recipientRepository.saveAndFlush(recipient);

        // Get all the recipientList where shortName in DEFAULT_SHORT_NAME or UPDATED_SHORT_NAME
        defaultRecipientShouldBeFound("shortName.in=" + DEFAULT_SHORT_NAME + "," + UPDATED_SHORT_NAME);

        // Get all the recipientList where shortName equals to UPDATED_SHORT_NAME
        defaultRecipientShouldNotBeFound("shortName.in=" + UPDATED_SHORT_NAME);
    }

    @Test
    @Transactional
    public void getAllRecipientsByShortNameIsNullOrNotNull() throws Exception {
        // Initialize the database
        recipientRepository.saveAndFlush(recipient);

        // Get all the recipientList where shortName is not null
        defaultRecipientShouldBeFound("shortName.specified=true");

        // Get all the recipientList where shortName is null
        defaultRecipientShouldNotBeFound("shortName.specified=false");
    }

    @Test
    @Transactional
    public void getAllRecipientsByServiceIdIsEqualToSomething() throws Exception {
        // Initialize the database
        recipientRepository.saveAndFlush(recipient);

        // Get all the recipientList where serviceId equals to DEFAULT_SERVICE_ID
        defaultRecipientShouldBeFound("serviceId.equals=" + DEFAULT_SERVICE_ID);

        // Get all the recipientList where serviceId equals to UPDATED_SERVICE_ID
        defaultRecipientShouldNotBeFound("serviceId.equals=" + UPDATED_SERVICE_ID);
    }

    @Test
    @Transactional
    public void getAllRecipientsByServiceIdIsInShouldWork() throws Exception {
        // Initialize the database
        recipientRepository.saveAndFlush(recipient);

        // Get all the recipientList where serviceId in DEFAULT_SERVICE_ID or UPDATED_SERVICE_ID
        defaultRecipientShouldBeFound("serviceId.in=" + DEFAULT_SERVICE_ID + "," + UPDATED_SERVICE_ID);

        // Get all the recipientList where serviceId equals to UPDATED_SERVICE_ID
        defaultRecipientShouldNotBeFound("serviceId.in=" + UPDATED_SERVICE_ID);
    }

    @Test
    @Transactional
    public void getAllRecipientsByServiceIdIsNullOrNotNull() throws Exception {
        // Initialize the database
        recipientRepository.saveAndFlush(recipient);

        // Get all the recipientList where serviceId is not null
        defaultRecipientShouldBeFound("serviceId.specified=true");

        // Get all the recipientList where serviceId is null
        defaultRecipientShouldNotBeFound("serviceId.specified=false");
    }

    @Test
    @Transactional
    public void getAllRecipientsByThumbnailIsEqualToSomething() throws Exception {
        // Initialize the database
        recipientRepository.saveAndFlush(recipient);

        // Get all the recipientList where thumbnail equals to DEFAULT_THUMBNAIL
        defaultRecipientShouldBeFound("thumbnail.equals=" + DEFAULT_THUMBNAIL);

        // Get all the recipientList where thumbnail equals to UPDATED_THUMBNAIL
        defaultRecipientShouldNotBeFound("thumbnail.equals=" + UPDATED_THUMBNAIL);
    }

    @Test
    @Transactional
    public void getAllRecipientsByThumbnailIsInShouldWork() throws Exception {
        // Initialize the database
        recipientRepository.saveAndFlush(recipient);

        // Get all the recipientList where thumbnail in DEFAULT_THUMBNAIL or UPDATED_THUMBNAIL
        defaultRecipientShouldBeFound("thumbnail.in=" + DEFAULT_THUMBNAIL + "," + UPDATED_THUMBNAIL);

        // Get all the recipientList where thumbnail equals to UPDATED_THUMBNAIL
        defaultRecipientShouldNotBeFound("thumbnail.in=" + UPDATED_THUMBNAIL);
    }

    @Test
    @Transactional
    public void getAllRecipientsByThumbnailIsNullOrNotNull() throws Exception {
        // Initialize the database
        recipientRepository.saveAndFlush(recipient);

        // Get all the recipientList where thumbnail is not null
        defaultRecipientShouldBeFound("thumbnail.specified=true");
        // Get all the recipientList where thumbnail is null
        //        defaultRecipientShouldNotBeFound("thumbnail.specified=false");
    }

    @Test
    @Transactional
    public void getAllRecipientsByLogoIsEqualToSomething() throws Exception {
        // Initialize the database
        recipientRepository.saveAndFlush(recipient);

        // Get all the recipientList where logo equals to DEFAULT_LOGO
        defaultRecipientShouldBeFound("logo.equals=" + DEFAULT_LOGO);

        // Get all the recipientList where logo equals to UPDATED_LOGO
        defaultRecipientShouldNotBeFound("logo.equals=" + UPDATED_LOGO);
    }

    @Test
    @Transactional
    public void getAllRecipientsByLogoIsInShouldWork() throws Exception {
        // Initialize the database
        recipientRepository.saveAndFlush(recipient);

        // Get all the recipientList where logo in DEFAULT_LOGO or UPDATED_LOGO
        defaultRecipientShouldBeFound("logo.in=" + DEFAULT_LOGO + "," + UPDATED_LOGO);

        // Get all the recipientList where logo equals to UPDATED_LOGO
        defaultRecipientShouldNotBeFound("logo.in=" + UPDATED_LOGO);
    }

    @Test
    @Transactional
    public void getAllRecipientsByLogoIsNullOrNotNull() throws Exception {
        // Initialize the database
        recipientRepository.saveAndFlush(recipient);

        // Get all the recipientList where logo is not null
        defaultRecipientShouldBeFound("logo.specified=true");
        // Get all the recipientList where logo is null
        //        defaultRecipientShouldNotBeFound("logo.specified=false");
    }

    @Test
    @Transactional
    public void getAllRecipientsByAmountRatioIsEqualToSomething() throws Exception {
        // Initialize the database
        recipientRepository.saveAndFlush(recipient);

        // Get all the recipientList where amountRatio equals to DEFAULT_AMOUNT_RATIO
        defaultRecipientShouldBeFound("amountRatio.equals=" + DEFAULT_AMOUNT_RATIO);

        // Get all the recipientList where amountRatio equals to UPDATED_AMOUNT_RATIO
        defaultRecipientShouldNotBeFound("amountRatio.equals=" + UPDATED_AMOUNT_RATIO);
    }

    @Test
    @Transactional
    public void getAllRecipientsByAmountRatioIsInShouldWork() throws Exception {
        // Initialize the database
        recipientRepository.saveAndFlush(recipient);

        // Get all the recipientList where amountRatio in DEFAULT_AMOUNT_RATIO or UPDATED_AMOUNT_RATIO
        defaultRecipientShouldBeFound("amountRatio.in=" + DEFAULT_AMOUNT_RATIO + "," + UPDATED_AMOUNT_RATIO);

        // Get all the recipientList where amountRatio equals to UPDATED_AMOUNT_RATIO
        defaultRecipientShouldNotBeFound("amountRatio.in=" + UPDATED_AMOUNT_RATIO);
    }

    @Test
    @Transactional
    public void getAllRecipientsByAmountRatioIsNullOrNotNull() throws Exception {
        // Initialize the database
        recipientRepository.saveAndFlush(recipient);

        // Get all the recipientList where amountRatio is not null
        defaultRecipientShouldBeFound("amountRatio.specified=true");

        // Get all the recipientList where amountRatio is null
        defaultRecipientShouldNotBeFound("amountRatio.specified=false");
    }

    @Test
    @Transactional
    public void getAllRecipientsByAmountRatioIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        recipientRepository.saveAndFlush(recipient);

        // Get all the recipientList where amountRatio greater than or equals to DEFAULT_AMOUNT_RATIO
        defaultRecipientShouldBeFound("amountRatio.greaterOrEqualThan=" + DEFAULT_AMOUNT_RATIO);
        // Get all the recipientList where amountRatio greater than or equals to UPDATED_AMOUNT_RATIO
        //        defaultRecipientShouldNotBeFound("amountRatio.greaterOrEqualThan=" + UPDATED_AMOUNT_RATIO);
    }

    @Test
    @Transactional
    public void getAllRecipientsByAmountRatioIsLessThanSomething() throws Exception {
        // Initialize the database
        recipientRepository.saveAndFlush(recipient);
        // Get all the recipientList where amountRatio less than or equals to DEFAULT_AMOUNT_RATIO
        //        defaultRecipientShouldNotBeFound("amountRatio.lessThan=" + DEFAULT_AMOUNT_RATIO);

        // Get all the recipientList where amountRatio less than or equals to UPDATED_AMOUNT_RATIO
        //        defaultRecipientShouldBeFound("amountRatio.lessThan=" + UPDATED_AMOUNT_RATIO);
    }

    @Test
    @Transactional
    public void getAllRecipientsByCcyIsEqualToSomething() throws Exception {
        // Initialize the database
        recipientRepository.saveAndFlush(recipient);

        // Get all the recipientList where ccy equals to DEFAULT_CCY
        defaultRecipientShouldBeFound("ccy.equals=" + DEFAULT_CCY);

        // Get all the recipientList where ccy equals to UPDATED_CCY
        defaultRecipientShouldNotBeFound("ccy.equals=" + UPDATED_CCY);
    }

    @Test
    @Transactional
    public void getAllRecipientsByCcyIsInShouldWork() throws Exception {
        // Initialize the database
        recipientRepository.saveAndFlush(recipient);

        // Get all the recipientList where ccy in DEFAULT_CCY or UPDATED_CCY
        defaultRecipientShouldBeFound("ccy.in=" + DEFAULT_CCY + "," + UPDATED_CCY);

        // Get all the recipientList where ccy equals to UPDATED_CCY
        defaultRecipientShouldNotBeFound("ccy.in=" + UPDATED_CCY);
    }

    @Test
    @Transactional
    public void getAllRecipientsByCcyIsNullOrNotNull() throws Exception {
        // Initialize the database
        recipientRepository.saveAndFlush(recipient);

        // Get all the recipientList where ccy is not null
        defaultRecipientShouldBeFound("ccy.specified=true");

        // Get all the recipientList where ccy is null
        defaultRecipientShouldNotBeFound("ccy.specified=false");
    }

    @Test
    @Transactional
    public void getAllRecipientsByCcyIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        recipientRepository.saveAndFlush(recipient);

        // Get all the recipientList where ccy greater than or equals to DEFAULT_CCY
        defaultRecipientShouldBeFound("ccy.greaterOrEqualThan=" + DEFAULT_CCY);
        // Get all the recipientList where ccy greater than or equals to UPDATED_CCY
        //        defaultRecipientShouldNotBeFound("ccy.greaterOrEqualThan=" + UPDATED_CCY);
    }

    @Test
    @Transactional
    public void getAllRecipientsByCcyIsLessThanSomething() throws Exception {
        // Initialize the database
        recipientRepository.saveAndFlush(recipient);

        // Get all the recipientList where ccy less than or equals to DEFAULT_CCY
        defaultRecipientShouldNotBeFound("ccy.lessThan=" + DEFAULT_CCY);

        // Get all the recipientList where ccy less than or equals to UPDATED_CCY
        defaultRecipientShouldBeFound("ccy.lessThan=" + UPDATED_CCY);
    }

    @Test
    @Transactional
    public void getAllRecipientsByMinAmountIsEqualToSomething() throws Exception {
        // Initialize the database
        recipientRepository.saveAndFlush(recipient);

        // Get all the recipientList where minAmount equals to DEFAULT_MIN_AMOUNT
        defaultRecipientShouldBeFound("minAmount.equals=" + DEFAULT_MIN_AMOUNT);

        // Get all the recipientList where minAmount equals to UPDATED_MIN_AMOUNT
        defaultRecipientShouldNotBeFound("minAmount.equals=" + UPDATED_MIN_AMOUNT);
    }

    @Test
    @Transactional
    public void getAllRecipientsByMinAmountIsInShouldWork() throws Exception {
        // Initialize the database
        recipientRepository.saveAndFlush(recipient);

        // Get all the recipientList where minAmount in DEFAULT_MIN_AMOUNT or UPDATED_MIN_AMOUNT
        defaultRecipientShouldBeFound("minAmount.in=" + DEFAULT_MIN_AMOUNT + "," + UPDATED_MIN_AMOUNT);

        // Get all the recipientList where minAmount equals to UPDATED_MIN_AMOUNT
        defaultRecipientShouldNotBeFound("minAmount.in=" + UPDATED_MIN_AMOUNT);
    }

    @Test
    @Transactional
    public void getAllRecipientsByMinAmountIsNullOrNotNull() throws Exception {
        // Initialize the database
        recipientRepository.saveAndFlush(recipient);

        // Get all the recipientList where minAmount is not null
        defaultRecipientShouldBeFound("minAmount.specified=true");

        // Get all the recipientList where minAmount is null
        defaultRecipientShouldNotBeFound("minAmount.specified=false");
    }

    @Test
    @Transactional
    public void getAllRecipientsByMinAmountIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        recipientRepository.saveAndFlush(recipient);

        // Get all the recipientList where minAmount greater than or equals to DEFAULT_MIN_AMOUNT
        defaultRecipientShouldBeFound("minAmount.greaterOrEqualThan=" + DEFAULT_MIN_AMOUNT);
        // Get all the recipientList where minAmount greater than or equals to UPDATED_MIN_AMOUNT
        //        defaultRecipientShouldNotBeFound("minAmount.greaterOrEqualThan=" + UPDATED_MIN_AMOUNT);
    }

    //    @Test
    @Transactional
    public void getAllRecipientsByMinAmountIsLessThanSomething() throws Exception {
        // Initialize the database
        recipientRepository.saveAndFlush(recipient);
        // Get all the recipientList where minAmount less than or equals to DEFAULT_MIN_AMOUNT
        //        defaultRecipientShouldNotBeFound("minAmount.lessThan=" + DEFAULT_MIN_AMOUNT);

        // Get all the recipientList where minAmount less than or equals to UPDATED_MIN_AMOUNT
        //        defaultRecipientShouldBeFound("minAmount.lessThan=" + UPDATED_MIN_AMOUNT);
    }

    @Test
    @Transactional
    public void getAllRecipientsByMaxAmountIsEqualToSomething() throws Exception {
        // Initialize the database
        recipientRepository.saveAndFlush(recipient);

        // Get all the recipientList where maxAmount equals to DEFAULT_MAX_AMOUNT
        defaultRecipientShouldBeFound("maxAmount.equals=" + DEFAULT_MAX_AMOUNT);

        // Get all the recipientList where maxAmount equals to UPDATED_MAX_AMOUNT
        defaultRecipientShouldNotBeFound("maxAmount.equals=" + UPDATED_MAX_AMOUNT);
    }

    @Test
    @Transactional
    public void getAllRecipientsByMaxAmountIsInShouldWork() throws Exception {
        // Initialize the database
        recipientRepository.saveAndFlush(recipient);

        // Get all the recipientList where maxAmount in DEFAULT_MAX_AMOUNT or UPDATED_MAX_AMOUNT
        defaultRecipientShouldBeFound("maxAmount.in=" + DEFAULT_MAX_AMOUNT + "," + UPDATED_MAX_AMOUNT);

        // Get all the recipientList where maxAmount equals to UPDATED_MAX_AMOUNT
        defaultRecipientShouldNotBeFound("maxAmount.in=" + UPDATED_MAX_AMOUNT);
    }

    @Test
    @Transactional
    public void getAllRecipientsByMaxAmountIsNullOrNotNull() throws Exception {
        // Initialize the database
        recipientRepository.saveAndFlush(recipient);

        // Get all the recipientList where maxAmount is not null
        defaultRecipientShouldBeFound("maxAmount.specified=true");

        // Get all the recipientList where maxAmount is null
        defaultRecipientShouldNotBeFound("maxAmount.specified=false");
    }

    @Test
    @Transactional
    public void getAllRecipientsByMaxAmountIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        recipientRepository.saveAndFlush(recipient);

        // Get all the recipientList where maxAmount greater than or equals to DEFAULT_MAX_AMOUNT
        defaultRecipientShouldBeFound("maxAmount.greaterOrEqualThan=" + DEFAULT_MAX_AMOUNT);
        // Get all the recipientList where maxAmount greater than or equals to UPDATED_MAX_AMOUNT
        //        defaultRecipientShouldNotBeFound("maxAmount.greaterOrEqualThan=" + UPDATED_MAX_AMOUNT);
    }

    //    @Test
    @Transactional
    public void getAllRecipientsByMaxAmountIsLessThanSomething() throws Exception {
        // Initialize the database
        recipientRepository.saveAndFlush(recipient);
        // Get all the recipientList where maxAmount less than or equals to DEFAULT_MAX_AMOUNT
        //        defaultRecipientShouldNotBeFound("maxAmount.lessThan=" + DEFAULT_MAX_AMOUNT);

        // Get all the recipientList where maxAmount less than or equals to UPDATED_MAX_AMOUNT
        //        defaultRecipientShouldBeFound("maxAmount.lessThan=" + UPDATED_MAX_AMOUNT);
    }

    @Test
    @Transactional
    public void getAllRecipientsByLimitRatioIsEqualToSomething() throws Exception {
        // Initialize the database
        recipientRepository.saveAndFlush(recipient);

        // Get all the recipientList where limitRatio equals to DEFAULT_LIMIT_RATIO
        defaultRecipientShouldBeFound("limitRatio.equals=" + DEFAULT_LIMIT_RATIO);

        // Get all the recipientList where limitRatio equals to UPDATED_LIMIT_RATIO
        defaultRecipientShouldNotBeFound("limitRatio.equals=" + UPDATED_LIMIT_RATIO);
    }

    @Test
    @Transactional
    public void getAllRecipientsByLimitRatioIsInShouldWork() throws Exception {
        // Initialize the database
        recipientRepository.saveAndFlush(recipient);

        // Get all the recipientList where limitRatio in DEFAULT_LIMIT_RATIO or UPDATED_LIMIT_RATIO
        defaultRecipientShouldBeFound("limitRatio.in=" + DEFAULT_LIMIT_RATIO + "," + UPDATED_LIMIT_RATIO);

        // Get all the recipientList where limitRatio equals to UPDATED_LIMIT_RATIO
        defaultRecipientShouldNotBeFound("limitRatio.in=" + UPDATED_LIMIT_RATIO);
    }

    @Test
    @Transactional
    public void getAllRecipientsByLimitRatioIsNullOrNotNull() throws Exception {
        // Initialize the database
        recipientRepository.saveAndFlush(recipient);

        // Get all the recipientList where limitRatio is not null
        defaultRecipientShouldBeFound("limitRatio.specified=true");

        // Get all the recipientList where limitRatio is null
        defaultRecipientShouldNotBeFound("limitRatio.specified=false");
    }

    @Test
    @Transactional
    public void getAllRecipientsByLimitRatioIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        recipientRepository.saveAndFlush(recipient);

        // Get all the recipientList where limitRatio greater than or equals to DEFAULT_LIMIT_RATIO
        defaultRecipientShouldBeFound("limitRatio.greaterOrEqualThan=" + DEFAULT_LIMIT_RATIO);

        // Get all the recipientList where limitRatio greater than or equals to UPDATED_LIMIT_RATIO
        defaultRecipientShouldNotBeFound("limitRatio.greaterOrEqualThan=" + UPDATED_LIMIT_RATIO);
    }

    @Test
    @Transactional
    public void getAllRecipientsByLimitRatioIsLessThanSomething() throws Exception {
        // Initialize the database
        recipientRepository.saveAndFlush(recipient);

        // Get all the recipientList where limitRatio less than or equals to DEFAULT_LIMIT_RATIO
        defaultRecipientShouldNotBeFound("limitRatio.lessThan=" + DEFAULT_LIMIT_RATIO);

        // Get all the recipientList where limitRatio less than or equals to UPDATED_LIMIT_RATIO
        defaultRecipientShouldBeFound("limitRatio.lessThan=" + UPDATED_LIMIT_RATIO);
    }

    @Test
    @Transactional
    public void getAllRecipientsByCommissionIsEqualToSomething() throws Exception {
        // Initialize the database
        recipientRepository.saveAndFlush(recipient);

        // Get all the recipientList where commission equals to DEFAULT_COMMISSION
        defaultRecipientShouldBeFound("commission.equals=" + DEFAULT_COMMISSION);

        // Get all the recipientList where commission equals to UPDATED_COMMISSION
        defaultRecipientShouldNotBeFound("commission.equals=" + UPDATED_COMMISSION);
    }

    @Test
    @Transactional
    public void getAllRecipientsByCommissionIsInShouldWork() throws Exception {
        // Initialize the database
        recipientRepository.saveAndFlush(recipient);

        // Get all the recipientList where commission in DEFAULT_COMMISSION or UPDATED_COMMISSION
        defaultRecipientShouldBeFound("commission.in=" + DEFAULT_COMMISSION + "," + UPDATED_COMMISSION);

        // Get all the recipientList where commission equals to UPDATED_COMMISSION
        defaultRecipientShouldNotBeFound("commission.in=" + UPDATED_COMMISSION);
    }

    @Test
    @Transactional
    public void getAllRecipientsByCommissionIsNullOrNotNull() throws Exception {
        // Initialize the database
        recipientRepository.saveAndFlush(recipient);

        // Get all the recipientList where commission is not null
        defaultRecipientShouldBeFound("commission.specified=true");

        // Get all the recipientList where commission is null
        defaultRecipientShouldNotBeFound("commission.specified=false");
    }

    @Test
    @Transactional
    public void getAllRecipientsByFeeIsEqualToSomething() throws Exception {
        // Initialize the database
        recipientRepository.saveAndFlush(recipient);

        // Get all the recipientList where fee equals to DEFAULT_FEE
        defaultRecipientShouldBeFound("fee.equals=" + DEFAULT_FEE);
        // Get all the recipientList where fee equals to UPDATED_FEE
        //        defaultRecipientShouldNotBeFound("fee.equals=" + UPDATED_FEE);
    }

    @Test
    @Transactional
    public void getAllRecipientsByFeeIsInShouldWork() throws Exception {
        // Initialize the database
        recipientRepository.saveAndFlush(recipient);

        // Get all the recipientList where fee in DEFAULT_FEE or UPDATED_FEE
        defaultRecipientShouldBeFound("fee.in=" + DEFAULT_FEE + "," + UPDATED_FEE);
        // Get all the recipientList where fee equals to UPDATED_FEE
        //        defaultRecipientShouldNotBeFound("fee.in=" + UPDATED_FEE);
    }

    @Test
    @Transactional
    public void getAllRecipientsByFeeIsNullOrNotNull() throws Exception {
        // Initialize the database
        recipientRepository.saveAndFlush(recipient);

        // Get all the recipientList where fee is not null
        defaultRecipientShouldBeFound("fee.specified=true");
        // Get all the recipientList where fee is null
        //        defaultRecipientShouldNotBeFound("fee.specified=false");
    }

    @Test
    @Transactional
    public void getAllRecipientsBySystemCommissionIsEqualToSomething() throws Exception {
        // Initialize the database
        recipientRepository.saveAndFlush(recipient);

        // Get all the recipientList where systemCommission equals to DEFAULT_SYSTEM_COMMISSION
        defaultRecipientShouldBeFound("systemCommission.equals=" + DEFAULT_SYSTEM_COMMISSION);

        // Get all the recipientList where systemCommission equals to UPDATED_SYSTEM_COMMISSION
        defaultRecipientShouldNotBeFound("systemCommission.equals=" + UPDATED_SYSTEM_COMMISSION);
    }

    @Test
    @Transactional
    public void getAllRecipientsBySystemCommissionIsInShouldWork() throws Exception {
        // Initialize the database
        recipientRepository.saveAndFlush(recipient);

        // Get all the recipientList where systemCommission in DEFAULT_SYSTEM_COMMISSION or UPDATED_SYSTEM_COMMISSION
        defaultRecipientShouldBeFound("systemCommission.in=" + DEFAULT_SYSTEM_COMMISSION + "," + UPDATED_SYSTEM_COMMISSION);

        // Get all the recipientList where systemCommission equals to UPDATED_SYSTEM_COMMISSION
        defaultRecipientShouldNotBeFound("systemCommission.in=" + UPDATED_SYSTEM_COMMISSION);
    }

    @Test
    @Transactional
    public void getAllRecipientsBySystemCommissionIsNullOrNotNull() throws Exception {
        // Initialize the database
        recipientRepository.saveAndFlush(recipient);

        // Get all the recipientList where systemCommission is not null
        defaultRecipientShouldBeFound("systemCommission.specified=true");

        // Get all the recipientList where systemCommission is null
        defaultRecipientShouldNotBeFound("systemCommission.specified=false");
    }

    @Test
    @Transactional
    public void getAllRecipientsBySystemFeeIsEqualToSomething() throws Exception {
        // Initialize the database
        recipientRepository.saveAndFlush(recipient);

        // Get all the recipientList where systemFee equals to DEFAULT_SYSTEM_FEE
        defaultRecipientShouldBeFound("systemFee.equals=" + DEFAULT_SYSTEM_FEE);

        // Get all the recipientList where systemFee equals to UPDATED_SYSTEM_FEE
        defaultRecipientShouldNotBeFound("systemFee.equals=" + UPDATED_SYSTEM_FEE);
    }

    @Test
    @Transactional
    public void getAllRecipientsBySystemFeeIsInShouldWork() throws Exception {
        // Initialize the database
        recipientRepository.saveAndFlush(recipient);

        // Get all the recipientList where systemFee in DEFAULT_SYSTEM_FEE or UPDATED_SYSTEM_FEE
        defaultRecipientShouldBeFound("systemFee.in=" + DEFAULT_SYSTEM_FEE + "," + UPDATED_SYSTEM_FEE);

        // Get all the recipientList where systemFee equals to UPDATED_SYSTEM_FEE
        defaultRecipientShouldNotBeFound("systemFee.in=" + UPDATED_SYSTEM_FEE);
    }

    @Test
    @Transactional
    public void getAllRecipientsBySystemFeeIsNullOrNotNull() throws Exception {
        // Initialize the database
        recipientRepository.saveAndFlush(recipient);

        // Get all the recipientList where systemFee is not null
        defaultRecipientShouldBeFound("systemFee.specified=true");

        // Get all the recipientList where systemFee is null
        defaultRecipientShouldNotBeFound("systemFee.specified=false");
    }

    @Test
    @Transactional
    public void getAllRecipientsBySenderCommissionIsEqualToSomething() throws Exception {
        // Initialize the database
        recipientRepository.saveAndFlush(recipient);

        // Get all the recipientList where senderCommission equals to DEFAULT_SENDER_COMMISSION
        defaultRecipientShouldBeFound("senderCommission.equals=" + DEFAULT_SENDER_COMMISSION);

        // Get all the recipientList where senderCommission equals to UPDATED_SENDER_COMMISSION
        defaultRecipientShouldNotBeFound("senderCommission.equals=" + UPDATED_SENDER_COMMISSION);
    }

    @Test
    @Transactional
    public void getAllRecipientsBySenderCommissionIsInShouldWork() throws Exception {
        // Initialize the database
        recipientRepository.saveAndFlush(recipient);

        // Get all the recipientList where senderCommission in DEFAULT_SENDER_COMMISSION or UPDATED_SENDER_COMMISSION
        defaultRecipientShouldBeFound("senderCommission.in=" + DEFAULT_SENDER_COMMISSION + "," + UPDATED_SENDER_COMMISSION);

        // Get all the recipientList where senderCommission equals to UPDATED_SENDER_COMMISSION
        defaultRecipientShouldNotBeFound("senderCommission.in=" + UPDATED_SENDER_COMMISSION);
    }

    @Test
    @Transactional
    public void getAllRecipientsBySenderCommissionIsNullOrNotNull() throws Exception {
        // Initialize the database
        recipientRepository.saveAndFlush(recipient);

        // Get all the recipientList where senderCommission is not null
        defaultRecipientShouldBeFound("senderCommission.specified=true");
        // Get all the recipientList where senderCommission is null
        //        defaultRecipientShouldNotBeFound("senderCommission.specified=false");
    }

    @Test
    @Transactional
    public void getAllRecipientsByOrdIsEqualToSomething() throws Exception {
        // Initialize the database
        recipientRepository.saveAndFlush(recipient);

        // Get all the recipientList where ord equals to DEFAULT_ORD
        defaultRecipientShouldBeFound("ord.equals=" + DEFAULT_ORD);

        // Get all the recipientList where ord equals to UPDATED_ORD
        defaultRecipientShouldNotBeFound("ord.equals=" + UPDATED_ORD);
    }

    @Test
    @Transactional
    public void getAllRecipientsByOrdIsInShouldWork() throws Exception {
        // Initialize the database
        recipientRepository.saveAndFlush(recipient);

        // Get all the recipientList where ord in DEFAULT_ORD or UPDATED_ORD
        defaultRecipientShouldBeFound("ord.in=" + DEFAULT_ORD + "," + UPDATED_ORD);

        // Get all the recipientList where ord equals to UPDATED_ORD
        defaultRecipientShouldNotBeFound("ord.in=" + UPDATED_ORD);
    }

    @Test
    @Transactional
    public void getAllRecipientsByOrdIsNullOrNotNull() throws Exception {
        // Initialize the database
        recipientRepository.saveAndFlush(recipient);

        // Get all the recipientList where ord is not null
        defaultRecipientShouldBeFound("ord.specified=true");
        // Get all the recipientList where ord is null
        //        defaultRecipientShouldNotBeFound("ord.specified=false");
    }

    @Test
    @Transactional
    public void getAllRecipientsByOrdIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        recipientRepository.saveAndFlush(recipient);

        // Get all the recipientList where ord greater than or equals to DEFAULT_ORD
        defaultRecipientShouldBeFound("ord.greaterOrEqualThan=" + DEFAULT_ORD);

        // Get all the recipientList where ord greater than or equals to UPDATED_ORD
        defaultRecipientShouldNotBeFound("ord.greaterOrEqualThan=" + UPDATED_ORD);
    }

    @Test
    @Transactional
    public void getAllRecipientsByOrdIsLessThanSomething() throws Exception {
        // Initialize the database
        recipientRepository.saveAndFlush(recipient);

        // Get all the recipientList where ord less than or equals to DEFAULT_ORD
        //        defaultRecipientShouldNotBeFound("ord.lessThan=" + DEFAULT_ORD);

        // Get all the recipientList where ord less than or equals to UPDATED_ORD
        defaultRecipientShouldBeFound("ord.lessThan=" + UPDATED_ORD);
    }

    @Test
    @Transactional
    public void getAllRecipientsByActiveIsEqualToSomething() throws Exception {
        // Initialize the database
        recipientRepository.saveAndFlush(recipient);

        // Get all the recipientList where active equals to DEFAULT_ACTIVE
        defaultRecipientShouldBeFound("active.equals=" + DEFAULT_ACTIVE);
        // Get all the recipientList where active equals to UPDATED_ACTIVE
        //        defaultRecipientShouldNotBeFound("active.equals=" + UPDATED_ACTIVE);
    }

    @Test
    @Transactional
    public void getAllRecipientsByActiveIsInShouldWork() throws Exception {
        // Initialize the database
        recipientRepository.saveAndFlush(recipient);

        // Get all the recipientList where active in DEFAULT_ACTIVE or UPDATED_ACTIVE
        defaultRecipientShouldBeFound("active.in=" + DEFAULT_ACTIVE + "," + UPDATED_ACTIVE);
        // Get all the recipientList where active equals to UPDATED_ACTIVE
        //        defaultRecipientShouldNotBeFound("active.in=" + UPDATED_ACTIVE);
    }

    @Test
    @Transactional
    public void getAllRecipientsByActiveIsNullOrNotNull() throws Exception {
        // Initialize the database
        recipientRepository.saveAndFlush(recipient);

        // Get all the recipientList where active is not null
        defaultRecipientShouldBeFound("active.specified=true");

        // Get all the recipientList where active is null
        defaultRecipientShouldNotBeFound("active.specified=false");
    }

    @Test
    @Transactional
    public void getAllRecipientsByCheckIsEqualToSomething() throws Exception {
        // Initialize the database
        recipientRepository.saveAndFlush(recipient);

        // Get all the recipientList where check equals to DEFAULT_CHECK
        defaultRecipientShouldBeFound("check.equals=" + DEFAULT_CHECK);
        // Get all the recipientList where check equals to UPDATED_CHECK
        //        defaultRecipientShouldNotBeFound("check.equals=" + UPDATED_CHECK);
    }

    @Test
    @Transactional
    public void getAllRecipientsByCheckIsInShouldWork() throws Exception {
        // Initialize the database
        recipientRepository.saveAndFlush(recipient);

        // Get all the recipientList where check in DEFAULT_CHECK or UPDATED_CHECK
        defaultRecipientShouldBeFound("check.in=" + DEFAULT_CHECK + "," + UPDATED_CHECK);
        // Get all the recipientList where check equals to UPDATED_CHECK
        //        defaultRecipientShouldNotBeFound("check.in=" + UPDATED_CHECK);
    }

    @Test
    @Transactional
    public void getAllRecipientsByCheckIsNullOrNotNull() throws Exception {
        // Initialize the database
        recipientRepository.saveAndFlush(recipient);

        // Get all the recipientList where check is not null
        defaultRecipientShouldBeFound("check.specified=true");
        // Get all the recipientList where check is null
        //        defaultRecipientShouldNotBeFound("check.specified=false");
    }

    @Test
    @Transactional
    public void getAllRecipientsByPayIsEqualToSomething() throws Exception {
        // Initialize the database
        recipientRepository.saveAndFlush(recipient);

        // Get all the recipientList where pay equals to DEFAULT_PAY
        defaultRecipientShouldBeFound("pay.equals=" + DEFAULT_PAY);
        // Get all the recipientList where pay equals to UPDATED_PAY
        //        defaultRecipientShouldNotBeFound("pay.equals=" + UPDATED_PAY);
    }

    @Test
    @Transactional
    public void getAllRecipientsByPayIsInShouldWork() throws Exception {
        // Initialize the database
        recipientRepository.saveAndFlush(recipient);

        // Get all the recipientList where pay in DEFAULT_PAY or UPDATED_PAY
        defaultRecipientShouldBeFound("pay.in=" + DEFAULT_PAY + "," + UPDATED_PAY);
        // Get all the recipientList where pay equals to UPDATED_PAY
        //        defaultRecipientShouldNotBeFound("pay.in=" + UPDATED_PAY);
    }

    @Test
    @Transactional
    public void getAllRecipientsByPayIsNullOrNotNull() throws Exception {
        // Initialize the database
        recipientRepository.saveAndFlush(recipient);

        // Get all the recipientList where pay is not null
        defaultRecipientShouldBeFound("pay.specified=true");
        // Get all the recipientList where pay is null
        //        defaultRecipientShouldNotBeFound("pay.specified=false");
    }

    @Test
    @Transactional
    public void getAllRecipientsByStatusIsEqualToSomething() throws Exception {
        // Initialize the database
        recipientRepository.saveAndFlush(recipient);

        // Get all the recipientList where status equals to DEFAULT_STATUS
        defaultRecipientShouldBeFound("status.equals=" + DEFAULT_STATUS);
        // Get all the recipientList where status equals to UPDATED_STATUS
        //        defaultRecipientShouldNotBeFound("status.equals=" + UPDATED_STATUS);
    }

    @Test
    @Transactional
    public void getAllRecipientsByStatusIsInShouldWork() throws Exception {
        // Initialize the database
        recipientRepository.saveAndFlush(recipient);

        // Get all the recipientList where status in DEFAULT_STATUS or UPDATED_STATUS
        defaultRecipientShouldBeFound("status.in=" + DEFAULT_STATUS + "," + UPDATED_STATUS);
        // Get all the recipientList where status equals to UPDATED_STATUS
        //        defaultRecipientShouldNotBeFound("status.in=" + UPDATED_STATUS);
    }

    @Test
    @Transactional
    public void getAllRecipientsByStatusIsNullOrNotNull() throws Exception {
        // Initialize the database
        recipientRepository.saveAndFlush(recipient);

        // Get all the recipientList where status is not null
        defaultRecipientShouldBeFound("status.specified=true");
        // Get all the recipientList where status is null
        //        defaultRecipientShouldNotBeFound("status.specified=false");
    }

    @Test
    @Transactional
    public void getAllRecipientsByCancelIsEqualToSomething() throws Exception {
        // Initialize the database
        recipientRepository.saveAndFlush(recipient);

        // Get all the recipientList where cancel equals to DEFAULT_CANCEL
        defaultRecipientShouldBeFound("cancel.equals=" + DEFAULT_CANCEL);
        // Get all the recipientList where cancel equals to UPDATED_CANCEL
        //        defaultRecipientShouldNotBeFound("cancel.equals=" + UPDATED_CANCEL);
    }

    @Test
    @Transactional
    public void getAllRecipientsByCancelIsInShouldWork() throws Exception {
        // Initialize the database
        recipientRepository.saveAndFlush(recipient);

        // Get all the recipientList where cancel in DEFAULT_CANCEL or UPDATED_CANCEL
        defaultRecipientShouldBeFound("cancel.in=" + DEFAULT_CANCEL + "," + UPDATED_CANCEL);
        // Get all the recipientList where cancel equals to UPDATED_CANCEL
        //        defaultRecipientShouldNotBeFound("cancel.in=" + UPDATED_CANCEL);
    }

    @Test
    @Transactional
    public void getAllRecipientsByCancelIsNullOrNotNull() throws Exception {
        // Initialize the database
        recipientRepository.saveAndFlush(recipient);

        // Get all the recipientList where cancel is not null
        defaultRecipientShouldBeFound("cancel.specified=true");
        // Get all the recipientList where cancel is null
        //        defaultRecipientShouldNotBeFound("cancel.specified=false");
    }

    @Test
    @Transactional
    public void getAllRecipientsByCanDepositIsEqualToSomething() throws Exception {
        // Initialize the database
        recipientRepository.saveAndFlush(recipient);

        // Get all the recipientList where canDeposit equals to DEFAULT_CAN_DEPOSIT
        defaultRecipientShouldBeFound("canDeposit.equals=" + DEFAULT_CAN_DEPOSIT);
        // Get all the recipientList where canDeposit equals to UPDATED_CAN_DEPOSIT
        //        defaultRecipientShouldNotBeFound("canDeposit.equals=" + UPDATED_CAN_DEPOSIT);
    }

    @Test
    @Transactional
    public void getAllRecipientsByCanDepositIsInShouldWork() throws Exception {
        // Initialize the database
        recipientRepository.saveAndFlush(recipient);

        // Get all the recipientList where canDeposit in DEFAULT_CAN_DEPOSIT or UPDATED_CAN_DEPOSIT
        defaultRecipientShouldBeFound("canDeposit.in=" + DEFAULT_CAN_DEPOSIT + "," + UPDATED_CAN_DEPOSIT);
        // Get all the recipientList where canDeposit equals to UPDATED_CAN_DEPOSIT
        //        defaultRecipientShouldNotBeFound("canDeposit.in=" + UPDATED_CAN_DEPOSIT);
    }

    @Test
    @Transactional
    public void getAllRecipientsByCanDepositIsNullOrNotNull() throws Exception {
        // Initialize the database
        recipientRepository.saveAndFlush(recipient);

        // Get all the recipientList where canDeposit is not null
        defaultRecipientShouldBeFound("canDeposit.specified=true");
        // Get all the recipientList where canDeposit is null
        //        defaultRecipientShouldNotBeFound("canDeposit.specified=false");
    }

    @Test
    @Transactional
    public void getAllRecipientsByEposPortIsEqualToSomething() throws Exception {
        // Initialize the database
        recipientRepository.saveAndFlush(recipient);

        // Get all the recipientList where eposPort equals to DEFAULT_EPOS_PORT
        defaultRecipientShouldBeFound("eposPort.equals=" + DEFAULT_EPOS_PORT);

        // Get all the recipientList where eposPort equals to UPDATED_EPOS_PORT
        defaultRecipientShouldNotBeFound("eposPort.equals=" + UPDATED_EPOS_PORT);
    }

    @Test
    @Transactional
    public void getAllRecipientsByEposPortIsInShouldWork() throws Exception {
        // Initialize the database
        recipientRepository.saveAndFlush(recipient);

        // Get all the recipientList where eposPort in DEFAULT_EPOS_PORT or UPDATED_EPOS_PORT
        defaultRecipientShouldBeFound("eposPort.in=" + DEFAULT_EPOS_PORT + "," + UPDATED_EPOS_PORT);

        // Get all the recipientList where eposPort equals to UPDATED_EPOS_PORT
        defaultRecipientShouldNotBeFound("eposPort.in=" + UPDATED_EPOS_PORT);
    }

    @Test
    @Transactional
    public void getAllRecipientsByEposPortIsNullOrNotNull() throws Exception {
        // Initialize the database
        recipientRepository.saveAndFlush(recipient);

        // Get all the recipientList where eposPort is not null
        defaultRecipientShouldBeFound("eposPort.specified=true");
        // Get all the recipientList where eposPort is null
        //        defaultRecipientShouldNotBeFound("eposPort.specified=false");
    }

    @Test
    @Transactional
    public void getAllRecipientsByEposPortIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        recipientRepository.saveAndFlush(recipient);

        // Get all the recipientList where eposPort greater than or equals to DEFAULT_EPOS_PORT
        defaultRecipientShouldBeFound("eposPort.greaterOrEqualThan=" + DEFAULT_EPOS_PORT);

        // Get all the recipientList where eposPort greater than or equals to (DEFAULT_EPOS_PORT + 1)
        defaultRecipientShouldNotBeFound("eposPort.greaterOrEqualThan=" + (DEFAULT_EPOS_PORT + 1));
    }

    @Test
    @Transactional
    public void getAllRecipientsByEposPortIsLessThanSomething() throws Exception {
        // Initialize the database
        recipientRepository.saveAndFlush(recipient);

        // Get all the recipientList where eposPort less than or equals to DEFAULT_EPOS_PORT
        //        defaultRecipientShouldNotBeFound("eposPort.lessThan=" + DEFAULT_EPOS_PORT);

        // Get all the recipientList where eposPort less than or equals to (DEFAULT_EPOS_PORT + 1)
        defaultRecipientShouldBeFound("eposPort.lessThan=" + (DEFAULT_EPOS_PORT + 1));
    }

    @Test
    @Transactional
    public void getAllRecipientsByEposMerchantIsEqualToSomething() throws Exception {
        // Initialize the database
        recipientRepository.saveAndFlush(recipient);

        // Get all the recipientList where eposMerchant equals to DEFAULT_EPOS_MERCHANT
        defaultRecipientShouldBeFound("eposMerchant.equals=" + DEFAULT_EPOS_MERCHANT);

        // Get all the recipientList where eposMerchant equals to UPDATED_EPOS_MERCHANT
        defaultRecipientShouldNotBeFound("eposMerchant.equals=" + UPDATED_EPOS_MERCHANT);
    }

    @Test
    @Transactional
    public void getAllRecipientsByEposMerchantIsInShouldWork() throws Exception {
        // Initialize the database
        recipientRepository.saveAndFlush(recipient);

        // Get all the recipientList where eposMerchant in DEFAULT_EPOS_MERCHANT or UPDATED_EPOS_MERCHANT
        defaultRecipientShouldBeFound("eposMerchant.in=" + DEFAULT_EPOS_MERCHANT + "," + UPDATED_EPOS_MERCHANT);

        // Get all the recipientList where eposMerchant equals to UPDATED_EPOS_MERCHANT
        defaultRecipientShouldNotBeFound("eposMerchant.in=" + UPDATED_EPOS_MERCHANT);
    }

    @Test
    @Transactional
    public void getAllRecipientsByEposMerchantIsNullOrNotNull() throws Exception {
        // Initialize the database
        recipientRepository.saveAndFlush(recipient);

        // Get all the recipientList where eposMerchant is not null
        defaultRecipientShouldBeFound("eposMerchant.specified=true");
        // Get all the recipientList where eposMerchant is null
        //        defaultRecipientShouldNotBeFound("eposMerchant.specified=false");
    }

    @Test
    @Transactional
    public void getAllRecipientsByEposMerchantIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        recipientRepository.saveAndFlush(recipient);

        // Get all the recipientList where eposMerchant greater than or equals to DEFAULT_EPOS_MERCHANT
        defaultRecipientShouldBeFound("eposMerchant.greaterOrEqualThan=" + DEFAULT_EPOS_MERCHANT);
        // Get all the recipientList where eposMerchant greater than or equals to UPDATED_EPOS_MERCHANT
        //        defaultRecipientShouldNotBeFound("eposMerchant.greaterOrEqualThan=" + UPDATED_EPOS_MERCHANT);
    }

    @Test
    @Transactional
    public void getAllRecipientsByEposMerchantIsLessThanSomething() throws Exception {
        // Initialize the database
        recipientRepository.saveAndFlush(recipient);

        // Get all the recipientList where eposMerchant less than or equals to DEFAULT_EPOS_MERCHANT
        defaultRecipientShouldNotBeFound("eposMerchant.lessThan=" + DEFAULT_EPOS_MERCHANT);

        // Get all the recipientList where eposMerchant less than or equals to UPDATED_EPOS_MERCHANT
        defaultRecipientShouldBeFound("eposMerchant.lessThan=" + UPDATED_EPOS_MERCHANT);
    }

    @Test
    @Transactional
    public void getAllRecipientsByEposTerminalIsEqualToSomething() throws Exception {
        // Initialize the database
        recipientRepository.saveAndFlush(recipient);

        // Get all the recipientList where eposTerminal equals to DEFAULT_EPOS_TERMINAL
        defaultRecipientShouldBeFound("eposTerminal.equals=" + DEFAULT_EPOS_TERMINAL);

        // Get all the recipientList where eposTerminal equals to UPDATED_EPOS_TERMINAL
        defaultRecipientShouldNotBeFound("eposTerminal.equals=" + UPDATED_EPOS_TERMINAL);
    }

    @Test
    @Transactional
    public void getAllRecipientsByEposTerminalIsInShouldWork() throws Exception {
        // Initialize the database
        recipientRepository.saveAndFlush(recipient);

        // Get all the recipientList where eposTerminal in DEFAULT_EPOS_TERMINAL or UPDATED_EPOS_TERMINAL
        defaultRecipientShouldBeFound("eposTerminal.in=" + DEFAULT_EPOS_TERMINAL + "," + UPDATED_EPOS_TERMINAL);

        // Get all the recipientList where eposTerminal equals to UPDATED_EPOS_TERMINAL
        defaultRecipientShouldNotBeFound("eposTerminal.in=" + UPDATED_EPOS_TERMINAL);
    }

    @Test
    @Transactional
    public void getAllRecipientsByEposTerminalIsNullOrNotNull() throws Exception {
        // Initialize the database
        recipientRepository.saveAndFlush(recipient);

        // Get all the recipientList where eposTerminal is not null
        defaultRecipientShouldBeFound("eposTerminal.specified=true");
        // Get all the recipientList where eposTerminal is null
        //        defaultRecipientShouldNotBeFound("eposTerminal.specified=false");
    }

    @Test
    @Transactional
    public void getAllRecipientsByEposTerminalIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        recipientRepository.saveAndFlush(recipient);

        // Get all the recipientList where eposTerminal greater than or equals to DEFAULT_EPOS_TERMINAL
        defaultRecipientShouldBeFound("eposTerminal.greaterOrEqualThan=" + DEFAULT_EPOS_TERMINAL);
        // Get all the recipientList where eposTerminal greater than or equals to UPDATED_EPOS_TERMINAL
        //        defaultRecipientShouldNotBeFound("eposTerminal.greaterOrEqualThan=" + UPDATED_EPOS_TERMINAL);
    }

    @Test
    @Transactional
    public void getAllRecipientsByEposTerminalIsLessThanSomething() throws Exception {
        // Initialize the database
        recipientRepository.saveAndFlush(recipient);

        // Get all the recipientList where eposTerminal less than or equals to DEFAULT_EPOS_TERMINAL
        defaultRecipientShouldNotBeFound("eposTerminal.lessThan=" + DEFAULT_EPOS_TERMINAL);

        // Get all the recipientList where eposTerminal less than or equals to UPDATED_EPOS_TERMINAL
        defaultRecipientShouldBeFound("eposTerminal.lessThan=" + UPDATED_EPOS_TERMINAL);
    }

    @Test
    @Transactional
    public void getAllRecipientsBySettlementAccountIsEqualToSomething() throws Exception {
        // Initialize the database
        recipientRepository.saveAndFlush(recipient);

        // Get all the recipientList where settlementAccount equals to DEFAULT_SETTLEMENT_ACCOUNT
        defaultRecipientShouldBeFound("settlementAccount.equals=" + DEFAULT_SETTLEMENT_ACCOUNT);

        // Get all the recipientList where settlementAccount equals to UPDATED_SETTLEMENT_ACCOUNT
        defaultRecipientShouldNotBeFound("settlementAccount.equals=" + UPDATED_SETTLEMENT_ACCOUNT);
    }

    @Test
    @Transactional
    public void getAllRecipientsBySettlementAccountIsInShouldWork() throws Exception {
        // Initialize the database
        recipientRepository.saveAndFlush(recipient);

        // Get all the recipientList where settlementAccount in DEFAULT_SETTLEMENT_ACCOUNT or UPDATED_SETTLEMENT_ACCOUNT
        defaultRecipientShouldBeFound("settlementAccount.in=" + DEFAULT_SETTLEMENT_ACCOUNT + "," + UPDATED_SETTLEMENT_ACCOUNT);

        // Get all the recipientList where settlementAccount equals to UPDATED_SETTLEMENT_ACCOUNT
        defaultRecipientShouldNotBeFound("settlementAccount.in=" + UPDATED_SETTLEMENT_ACCOUNT);
    }

    @Test
    @Transactional
    public void getAllRecipientsBySettlementAccountIsNullOrNotNull() throws Exception {
        // Initialize the database
        recipientRepository.saveAndFlush(recipient);

        // Get all the recipientList where settlementAccount is not null
        defaultRecipientShouldBeFound("settlementAccount.specified=true");
        // Get all the recipientList where settlementAccount is null
        //        defaultRecipientShouldNotBeFound("settlementAccount.specified=false");
    }

    @Test
    @Transactional
    public void getAllRecipientsByMfoIsEqualToSomething() throws Exception {
        // Initialize the database
        recipientRepository.saveAndFlush(recipient);

        // Get all the recipientList where mfo equals to DEFAULT_MFO
        defaultRecipientShouldBeFound("mfo.equals=" + DEFAULT_MFO);

        // Get all the recipientList where mfo equals to UPDATED_MFO
        defaultRecipientShouldNotBeFound("mfo.equals=" + UPDATED_MFO);
    }

    @Test
    @Transactional
    public void getAllRecipientsByMfoIsInShouldWork() throws Exception {
        // Initialize the database
        recipientRepository.saveAndFlush(recipient);

        // Get all the recipientList where mfo in DEFAULT_MFO or UPDATED_MFO
        defaultRecipientShouldBeFound("mfo.in=" + DEFAULT_MFO + "," + UPDATED_MFO);

        // Get all the recipientList where mfo equals to UPDATED_MFO
        defaultRecipientShouldNotBeFound("mfo.in=" + UPDATED_MFO);
    }

    @Test
    @Transactional
    public void getAllRecipientsByMfoIsNullOrNotNull() throws Exception {
        // Initialize the database
        recipientRepository.saveAndFlush(recipient);

        // Get all the recipientList where mfo is not null
        defaultRecipientShouldBeFound("mfo.specified=true");
        // Get all the recipientList where mfo is null
        //        defaultRecipientShouldNotBeFound("mfo.specified=false");
    }

    @Test
    @Transactional
    public void getAllRecipientsByInnIsEqualToSomething() throws Exception {
        // Initialize the database
        recipientRepository.saveAndFlush(recipient);

        // Get all the recipientList where inn equals to DEFAULT_INN
        defaultRecipientShouldBeFound("inn.equals=" + DEFAULT_INN);

        // Get all the recipientList where inn equals to UPDATED_INN
        defaultRecipientShouldNotBeFound("inn.equals=" + UPDATED_INN);
    }

    @Test
    @Transactional
    public void getAllRecipientsByInnIsInShouldWork() throws Exception {
        // Initialize the database
        recipientRepository.saveAndFlush(recipient);

        // Get all the recipientList where inn in DEFAULT_INN or UPDATED_INN
        defaultRecipientShouldBeFound("inn.in=" + DEFAULT_INN + "," + UPDATED_INN);

        // Get all the recipientList where inn equals to UPDATED_INN
        defaultRecipientShouldNotBeFound("inn.in=" + UPDATED_INN);
    }

    @Test
    @Transactional
    public void getAllRecipientsByInnIsNullOrNotNull() throws Exception {
        // Initialize the database
        recipientRepository.saveAndFlush(recipient);

        // Get all the recipientList where inn is not null
        defaultRecipientShouldBeFound("inn.specified=true");
        // Get all the recipientList where inn is null
        //        defaultRecipientShouldNotBeFound("inn.specified=false");
    }

    @Test
    @Transactional
    public void getAllRecipientsByTypeIsEqualToSomething() throws Exception {
        // Initialize the database
        recipientRepository.saveAndFlush(recipient);

        // Get all the recipientList where type equals to DEFAULT_TYPE
        defaultRecipientShouldBeFound("type.equals=" + DEFAULT_TYPE);
        // Get all the recipientList where type equals to UPDATED_TYPE
        //        defaultRecipientShouldNotBeFound("type.equals=" + UPDATED_TYPE);
    }

    @Test
    @Transactional
    public void getAllRecipientsByTypeIsInShouldWork() throws Exception {
        // Initialize the database
        recipientRepository.saveAndFlush(recipient);

        // Get all the recipientList where type in DEFAULT_TYPE or UPDATED_TYPE
        defaultRecipientShouldBeFound("type.in=" + DEFAULT_TYPE + "," + UPDATED_TYPE);
        // Get all the recipientList where type equals to UPDATED_TYPE
        //        defaultRecipientShouldNotBeFound("type.in=" + UPDATED_TYPE);
    }

    @Test
    @Transactional
    public void getAllRecipientsByTypeIsNullOrNotNull() throws Exception {
        // Initialize the database
        recipientRepository.saveAndFlush(recipient);

        // Get all the recipientList where type is not null
        defaultRecipientShouldBeFound("type.specified=true");
        // Get all the recipientList where type is null
        //        defaultRecipientShouldNotBeFound("type.specified=false");
    }

    @Test
    @Transactional
    public void getAllRecipientsByGatewayIsEqualToSomething() throws Exception {
        // Initialize the database
        Gateway gateway = GatewayResourceIntTest.createEntity(em);
        em.persist(gateway);
        em.flush();
        recipient.setGateway(gateway);
        recipientRepository.saveAndFlush(recipient);
        Long gatewayId = gateway.getId();

        // Get all the recipientList where gateway equals to gatewayId
        defaultRecipientShouldBeFound("gatewayId.equals=" + gatewayId);

        // Get all the recipientList where gateway equals to gatewayId + 1
        defaultRecipientShouldNotBeFound("gatewayId.equals=" + (gatewayId + 1));
    }

    @Test
    @Transactional
    public void getAllRecipientsByCategoryIsEqualToSomething() throws Exception {
        // Initialize the database
        Category category = CategoryResourceIntTest.createEntity(em);
        em.persist(category);
        em.flush();
        recipient.setCategory(category);
        recipientRepository.saveAndFlush(recipient);
        Long categoryId = category.getId();

        // Get all the recipientList where category equals to categoryId
        defaultRecipientShouldBeFound("categoryId.equals=" + categoryId);

        // Get all the recipientList where category equals to categoryId + 1
        defaultRecipientShouldNotBeFound("categoryId.equals=" + (categoryId + 1));
    }

    @Test
    @Transactional
    public void getAllRecipientsByTitleIsEqualToSomething() throws Exception {
        // Initialize the database
        I18n title = I18nResourceIntTest.createEntity(em);
        em.persist(title);
        em.flush();
        recipient.addTitle(title);
        recipientRepository.saveAndFlush(recipient);
        Long titleId = title.getId();

        // Get all the recipientList where title equals to titleId
        defaultRecipientShouldBeFound("titleId.equals=" + titleId);
        // Get all the recipientList where title equals to titleId + 1
        //        defaultRecipientShouldNotBeFound("titleId.equals=" + (titleId + 1));
    }

    @Test
    @Transactional
    public void getAllRecipientsByFullNameIsEqualToSomething() throws Exception {
        // Initialize the database
        I18n fullName = I18nResourceIntTest.createEntity(em);
        em.persist(fullName);
        em.flush();
        recipient.addFullName(fullName);
        recipientRepository.saveAndFlush(recipient);
        Long fullNameId = fullName.getId();

        // Get all the recipientList where fullName equals to fullNameId
        defaultRecipientShouldBeFound("fullNameId.equals=" + fullNameId);
        // Get all the recipientList where fullName equals to fullNameId + 1
        //        defaultRecipientShouldNotBeFound("fullNameId.equals=" + (fullNameId + 1));
    }

    /**
     * Executes the search, and checks that the default entity is returned
     */
    private void defaultRecipientShouldBeFound(String filter) throws Exception {
        restRecipientMockMvc
            .perform(get("/api/recipients?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(recipient.getId().intValue())))
            .andExpect(jsonPath("$.[*].shortName").value(hasItem(DEFAULT_SHORT_NAME)))
            .andExpect(jsonPath("$.[*].serviceId").value(hasItem(DEFAULT_SERVICE_ID)))
            .andExpect(jsonPath("$.[*].thumbnail").value(hasItem(DEFAULT_THUMBNAIL)))
            .andExpect(jsonPath("$.[*].logo").value(hasItem(DEFAULT_LOGO)))
            .andExpect(jsonPath("$.[*].amountRatio").value(hasItem(DEFAULT_AMOUNT_RATIO)))
            .andExpect(jsonPath("$.[*].ccy").value(hasItem(DEFAULT_CCY)))
            .andExpect(jsonPath("$.[*].minAmount").value(hasItem(DEFAULT_MIN_AMOUNT.intValue())))
            .andExpect(jsonPath("$.[*].maxAmount").value(hasItem(DEFAULT_MAX_AMOUNT.intValue())))
            .andExpect(jsonPath("$.[*].limitRatio").value(hasItem(DEFAULT_LIMIT_RATIO)))
            .andExpect(jsonPath("$.[*].commission").value(hasItem(DEFAULT_COMMISSION.doubleValue())))
            .andExpect(jsonPath("$.[*].fee").value(hasItem(DEFAULT_FEE.doubleValue())))
            .andExpect(jsonPath("$.[*].systemCommission").value(hasItem(DEFAULT_SYSTEM_COMMISSION.doubleValue())))
            .andExpect(jsonPath("$.[*].systemFee").value(hasItem(DEFAULT_SYSTEM_FEE.doubleValue())))
            .andExpect(jsonPath("$.[*].senderCommission").value(hasItem(DEFAULT_SENDER_COMMISSION.doubleValue())))
            .andExpect(jsonPath("$.[*].ord").value(hasItem(DEFAULT_ORD)))
            .andExpect(jsonPath("$.[*].active").value(hasItem(DEFAULT_ACTIVE.booleanValue())))
            .andExpect(jsonPath("$.[*].check").value(hasItem(DEFAULT_CHECK.booleanValue())))
            .andExpect(jsonPath("$.[*].pay").value(hasItem(DEFAULT_PAY.booleanValue())))
            .andExpect(jsonPath("$.[*].status").value(hasItem(DEFAULT_STATUS.booleanValue())))
            .andExpect(jsonPath("$.[*].cancel").value(hasItem(DEFAULT_CANCEL.booleanValue())))
            .andExpect(jsonPath("$.[*].canDeposit").value(hasItem(DEFAULT_CAN_DEPOSIT.booleanValue())))
            .andExpect(jsonPath("$.[*].eposPort").value(hasItem(DEFAULT_EPOS_PORT)))
            .andExpect(jsonPath("$.[*].eposMerchant").value(hasItem(DEFAULT_EPOS_MERCHANT.intValue())))
            .andExpect(jsonPath("$.[*].eposTerminal").value(hasItem(DEFAULT_EPOS_TERMINAL.intValue())))
            .andExpect(jsonPath("$.[*].settlementAccount").value(hasItem(DEFAULT_SETTLEMENT_ACCOUNT)))
            .andExpect(jsonPath("$.[*].mfo").value(hasItem(DEFAULT_MFO)))
            .andExpect(jsonPath("$.[*].inn").value(hasItem(DEFAULT_INN)))
            .andExpect(jsonPath("$.[*].reconcile").value(hasItem(DEFAULT_RECONCILE.booleanValue())))
            .andExpect(jsonPath("$.[*].type").value(hasItem(DEFAULT_TYPE.toString())));
        // Check, that the count call also returns 1
        restRecipientMockMvc
            .perform(get("/api/recipients/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(content().string("1"));
    }

    /**
     * Executes the search, and checks that the default entity is not returned
     */
    private void defaultRecipientShouldNotBeFound(String filter) throws Exception {
        restRecipientMockMvc
            .perform(get("/api/recipients?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty());

        // Check, that the count call also returns 0
        restRecipientMockMvc
            .perform(get("/api/recipients/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(content().string("0"));
    }

    @Test
    @Transactional
    public void getNonExistingRecipient() throws Exception {
        // Get the recipient
        restRecipientMockMvc.perform(get("/api/recipients/{id}", Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    //    @Test
    @Transactional
    public void updateRecipient() throws Exception {
        // Initialize the database
        recipientRepository.saveAndFlush(recipient);

        int databaseSizeBeforeUpdate = recipientRepository.findAll().size();

        // Update the recipient
        Recipient updatedRecipient = recipientRepository.findById(recipient.getId()).get();
        // Disconnect from session so that the updates on updatedRecipient are not directly saved in db
        em.detach(updatedRecipient);
        updatedRecipient
            .shortName(UPDATED_SHORT_NAME)
            .serviceId(UPDATED_SERVICE_ID)
            .thumbnail(UPDATED_THUMBNAIL)
            .logo(UPDATED_LOGO)
            .amountRatio(UPDATED_AMOUNT_RATIO)
            .ccy(UPDATED_CCY)
            .minAmount(UPDATED_MIN_AMOUNT)
            .maxAmount(UPDATED_MAX_AMOUNT)
            .limitRatio(UPDATED_LIMIT_RATIO)
            .commission(UPDATED_COMMISSION)
            .fee(UPDATED_FEE)
            .systemCommission(UPDATED_SYSTEM_COMMISSION)
            .systemFee(UPDATED_SYSTEM_FEE)
            .senderCommission(UPDATED_SENDER_COMMISSION)
            .ord(UPDATED_ORD)
            .active(UPDATED_ACTIVE)
            .check(UPDATED_CHECK)
            .pay(UPDATED_PAY)
            .status(UPDATED_STATUS)
            .cancel(UPDATED_CANCEL)
            .canDeposit(UPDATED_CAN_DEPOSIT)
//            .eposPort(UPDATED_EPOS_PORT)
//            .eposMerchant(UPDATED_EPOS_MERCHANT)
//            .eposTerminal(UPDATED_EPOS_TERMINAL)
            .settlementAccount(UPDATED_SETTLEMENT_ACCOUNT)
            .mfo(UPDATED_MFO)
            .inn(UPDATED_INN)
            .reconcile(UPDATED_RECONCILE)
            .type(UPDATED_TYPE);
        RecipientDTO recipientDTO = recipientMapper.toDto(updatedRecipient);

        restRecipientMockMvc
            .perform(
                put("/api/recipients").contentType(TestUtil.APPLICATION_JSON_UTF8).content(TestUtil.convertObjectToJsonBytes(recipientDTO))
            )
            .andExpect(status().isOk());

        // Validate the Recipient in the database
        List<Recipient> recipientList = recipientRepository.findAll();
        assertThat(recipientList).hasSize(databaseSizeBeforeUpdate);
        Recipient testRecipient = recipientList.get(recipientList.size() - 1);
        assertThat(testRecipient.getShortName()).isEqualTo(UPDATED_SHORT_NAME);
        assertThat(testRecipient.getServiceId()).isEqualTo(UPDATED_SERVICE_ID);
        assertThat(testRecipient.getThumbnail()).isEqualTo(UPDATED_THUMBNAIL);
        assertThat(testRecipient.getLogo()).isEqualTo(UPDATED_LOGO);
        assertThat(testRecipient.getAmountRatio()).isEqualTo(UPDATED_AMOUNT_RATIO);
        assertThat(testRecipient.getCcy()).isEqualTo(UPDATED_CCY);
        assertThat(testRecipient.getMinAmount()).isEqualTo(UPDATED_MIN_AMOUNT);
        assertThat(testRecipient.getMaxAmount()).isEqualTo(UPDATED_MAX_AMOUNT);
        assertThat(testRecipient.getLimitRatio()).isEqualTo(UPDATED_LIMIT_RATIO);
        assertThat(testRecipient.getCommission()).isEqualTo(UPDATED_COMMISSION);
        assertThat(testRecipient.getFee()).isEqualTo(UPDATED_FEE);
        assertThat(testRecipient.getSystemCommission()).isEqualTo(UPDATED_SYSTEM_COMMISSION);
        assertThat(testRecipient.getSystemFee()).isEqualTo(UPDATED_SYSTEM_FEE);
        assertThat(testRecipient.getSenderCommission()).isEqualTo(UPDATED_SENDER_COMMISSION);
        assertThat(testRecipient.getOrd()).isEqualTo(UPDATED_ORD);
        assertThat(testRecipient.isActive()).isEqualTo(UPDATED_ACTIVE);
        assertThat(testRecipient.isCheck()).isEqualTo(UPDATED_CHECK);
        assertThat(testRecipient.isPay()).isEqualTo(UPDATED_PAY);
        assertThat(testRecipient.isStatus()).isEqualTo(UPDATED_STATUS);
        assertThat(testRecipient.isCancel()).isEqualTo(UPDATED_CANCEL);
        assertThat(testRecipient.isCanDeposit()).isEqualTo(UPDATED_CAN_DEPOSIT);
//        assertThat(testRecipient.getEposPort()).isEqualTo(UPDATED_EPOS_PORT);
//        assertThat(testRecipient.getEposMerchant()).isEqualTo(UPDATED_EPOS_MERCHANT);
//        assertThat(testRecipient.getEposTerminal()).isEqualTo(UPDATED_EPOS_TERMINAL);
        assertThat(testRecipient.getSettlementAccount()).isEqualTo(UPDATED_SETTLEMENT_ACCOUNT);
        assertThat(testRecipient.getMfo()).isEqualTo(UPDATED_MFO);
        assertThat(testRecipient.getInn()).isEqualTo(UPDATED_INN);
        assertThat(testRecipient.isReconcile()).isEqualTo(UPDATED_RECONCILE);
        assertThat(testRecipient.getType()).isEqualTo(UPDATED_TYPE);
    }

    @Test
    @Transactional
    public void updateNonExistingRecipient() throws Exception {
        int databaseSizeBeforeUpdate = recipientRepository.findAll().size();

        // Create the Recipient
        RecipientDTO recipientDTO = recipientMapper.toDto(recipient);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restRecipientMockMvc
            .perform(
                put("/api/recipients").contentType(TestUtil.APPLICATION_JSON_UTF8).content(TestUtil.convertObjectToJsonBytes(recipientDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Recipient in the database
        List<Recipient> recipientList = recipientRepository.findAll();
        assertThat(recipientList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    public void deleteRecipient() throws Exception {
        // Initialize the database
        recipientRepository.saveAndFlush(recipient);

        int databaseSizeBeforeDelete = recipientRepository.findAll().size();

        // Get the recipient
        restRecipientMockMvc
            .perform(delete("/api/recipients/{id}", recipient.getId()).accept(TestUtil.APPLICATION_JSON_UTF8))
            .andExpect(status().isOk());

        // Validate the database is empty
        List<Recipient> recipientList = recipientRepository.findAll();
        assertThat(recipientList).hasSize(databaseSizeBeforeDelete - 1);
    }

    @Test
    @Transactional
    public void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Recipient.class);
        Recipient recipient1 = new Recipient();
        recipient1.setId(1L);
        Recipient recipient2 = new Recipient();
        recipient2.setId(recipient1.getId());
        assertThat(recipient1).isEqualTo(recipient2);
        recipient2.setId(2L);
        assertThat(recipient1).isNotEqualTo(recipient2);
        recipient1.setId(null);
        assertThat(recipient1).isNotEqualTo(recipient2);
    }

    @Test
    @Transactional
    public void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(RecipientDTO.class);
        RecipientDTO recipientDTO1 = new RecipientDTO();
        recipientDTO1.setId(1L);
        RecipientDTO recipientDTO2 = new RecipientDTO();
        assertThat(recipientDTO1).isNotEqualTo(recipientDTO2);
        recipientDTO2.setId(recipientDTO1.getId());
        assertThat(recipientDTO1).isEqualTo(recipientDTO2);
        recipientDTO2.setId(2L);
        assertThat(recipientDTO1).isNotEqualTo(recipientDTO2);
        recipientDTO1.setId(null);
        assertThat(recipientDTO1).isNotEqualTo(recipientDTO2);
    }

    //    @Test
    //    @Transactional
    //    public void testEntityFromId() {
    //        assertThat(recipientMapper.fromId(42L).getId()).isEqualTo(42);
    //        assertThat(recipientMapper.fromId(null)).isNull();
    //    }

    //TODO - duplicated test
    @Test
    @Transactional
    @WithMockUser(roles = { "ADMIN" })
    public void getAllowedRecipientNames() throws Exception {
        DummyUtils.prepareDummyData(dummyAdapterDelegate);

        // Get all the recipientList
        restRecipientMockMvc
            .perform(get("/api/recipients/allowed-simple?recipientNamePart=usd&sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$").isNotEmpty());
    }

    @Test
    @Transactional
    @WithMockUser(username = DummyDataAdapterV3.DUMMY_AGENT)
    public void getAllowedRecipientNames2() throws Exception {
        DummyUtils.prepareDummyData(dummyAdapterDelegate);

        // Get all the recipientList
        restRecipientMockMvc
            .perform(get("/api/recipients/allowed-names?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].key").value(hasItem(DummyDataAdapterV3.DUMMY_RECIPIENT_USD)))
            .andExpect(jsonPath("$.[*].values").exists());
    }
}
