package uz.paykap.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;
import javax.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import uz.paykap.IntegrationTest;
import uz.paykap.domain.Category;
import uz.paykap.domain.Gateway;
import uz.paykap.domain.I18n;
import uz.paykap.domain.Recipient;
import uz.paykap.repository.RecipientRepository;
import uz.paykap.service.RecipientService;
import uz.paykap.service.criteria.RecipientCriteria;
import uz.paykap.service.dto.RecipientDTO;
import uz.paykap.service.mapper.RecipientMapper;

/**
 * Integration tests for the {@link RecipientResource} REST controller.
 */
@IntegrationTest
@ExtendWith(MockitoExtension.class)
@AutoConfigureMockMvc
@WithMockUser
public class RecipientResourceIT {

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
    private static final Integer SMALLER_AMOUNT_RATIO = 1 - 1;

    private static final Integer DEFAULT_CCY = 1;
    private static final Integer UPDATED_CCY = 2;
    private static final Integer SMALLER_CCY = 1 - 1;

    private static final Long DEFAULT_MIN_AMOUNT = 1L;
    private static final Long UPDATED_MIN_AMOUNT = 2L;
    private static final Long SMALLER_MIN_AMOUNT = 1L - 1L;

    private static final Long DEFAULT_MAX_AMOUNT = 1L;
    private static final Long UPDATED_MAX_AMOUNT = 2L;
    private static final Long SMALLER_MAX_AMOUNT = 1L - 1L;

    private static final Integer DEFAULT_LIMIT_RATIO = 1;
    private static final Integer UPDATED_LIMIT_RATIO = 2;
    private static final Integer SMALLER_LIMIT_RATIO = 1 - 1;

    private static final Double DEFAULT_COMMISSION = 1D;
    private static final Double UPDATED_COMMISSION = 2D;
    private static final Double SMALLER_COMMISSION = 1D - 1D;

    private static final Double DEFAULT_FEE = 1D;
    private static final Double UPDATED_FEE = 2D;
    private static final Double SMALLER_FEE = 1D - 1D;

    private static final Double DEFAULT_SYSTEM_COMMISSION = 1D;
    private static final Double UPDATED_SYSTEM_COMMISSION = 2D;
    private static final Double SMALLER_SYSTEM_COMMISSION = 1D - 1D;

    private static final Double DEFAULT_SYSTEM_FEE = 1D;
    private static final Double UPDATED_SYSTEM_FEE = 2D;
    private static final Double SMALLER_SYSTEM_FEE = 1D - 1D;

    private static final Double DEFAULT_SENDER_COMMISSION = 1D;
    private static final Double UPDATED_SENDER_COMMISSION = 2D;
    private static final Double SMALLER_SENDER_COMMISSION = 1D - 1D;

    private static final Integer DEFAULT_ORD = 1;
    private static final Integer UPDATED_ORD = 2;
    private static final Integer SMALLER_ORD = 1 - 1;

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
    private static final Integer SMALLER_EPOS_PORT = 64000 - 1;

    private static final Long DEFAULT_EPOS_MERCHANT = 1L;
    private static final Long UPDATED_EPOS_MERCHANT = 2L;
    private static final Long SMALLER_EPOS_MERCHANT = 1L - 1L;

    private static final Long DEFAULT_EPOS_TERMINAL = 1L;
    private static final Long UPDATED_EPOS_TERMINAL = 2L;
    private static final Long SMALLER_EPOS_TERMINAL = 1L - 1L;

    private static final String DEFAULT_SETTLEMENT_ACCOUNT = "AAAAAAAAAA";
    private static final String UPDATED_SETTLEMENT_ACCOUNT = "BBBBBBBBBB";

    private static final String DEFAULT_MFO = "AAAAAAAAAA";
    private static final String UPDATED_MFO = "BBBBBBBBBB";

    private static final String DEFAULT_INN = "AAAAAAAAAA";
    private static final String UPDATED_INN = "BBBBBBBBBB";

    private static final Boolean DEFAULT_RECONCILE = false;
    private static final Boolean UPDATED_RECONCILE = true;

    private static final String ENTITY_API_URL = "/api/recipients";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong count = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private RecipientRepository recipientRepository;

    @Mock
    private RecipientRepository recipientRepositoryMock;

    @Autowired
    private RecipientMapper recipientMapper;

    @Mock
    private RecipientService recipientServiceMock;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restRecipientMockMvc;

    private Recipient recipient;

    /**
     * Create an entity for this test.
     *
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
            .reconcile(DEFAULT_RECONCILE);
        // Add required entity
        I18n i18n;
        //        if (TestUtil.findAll(em, I18n.class).isEmpty()) {
        i18n = I18nResourceIT.createEntity(em);
        //            em.persist(i18n);
        //            em.flush();
        //        } else {
        //            i18n = TestUtil.findAll(em, I18n.class).get(0);
        //        }
        recipient.getTitles().add(i18n);
        // Add required entity
        recipient.getFullNames().add(I18nResourceIT.createEntity(em));
        return recipient;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Recipient createUpdatedEntity(EntityManager em) {
        Recipient recipient = new Recipient()
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
            .reconcile(UPDATED_RECONCILE);
        // Add required entity
        I18n i18n;
        if (TestUtil.findAll(em, I18n.class).isEmpty()) {
            i18n = I18nResourceIT.createUpdatedEntity(em);
            //            em.persist(i18n);
            //            em.flush();
        } else {
            i18n = TestUtil.findAll(em, I18n.class).get(0);
        }
        recipient.getTitles().add(i18n);
        // Add required entity
        //        recipient.getFullNames().add(i18n);
        return recipient;
    }

    @BeforeEach
    public void initTest() {
        recipient = createEntity(em);
    }

    @Test
    @Transactional
    void createRecipient() throws Exception {
        int databaseSizeBeforeCreate = recipientRepository.findAll().size();
        // Create the Recipient
        RecipientDTO recipientDTO = recipientMapper.toDto(recipient);
        restRecipientMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(recipientDTO)))
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
        assertThat(testRecipient.getActive()).isEqualTo(DEFAULT_ACTIVE);
        assertThat(testRecipient.getCheck()).isEqualTo(DEFAULT_CHECK);
        assertThat(testRecipient.getPay()).isEqualTo(DEFAULT_PAY);
        assertThat(testRecipient.getStatus()).isEqualTo(DEFAULT_STATUS);
        assertThat(testRecipient.getCancel()).isEqualTo(DEFAULT_CANCEL);
        assertThat(testRecipient.getCanDeposit()).isEqualTo(DEFAULT_CAN_DEPOSIT);
//        assertThat(testRecipient.getEposPort()).isEqualTo(DEFAULT_EPOS_PORT);
//        assertThat(testRecipient.getEposMerchant()).isEqualTo(DEFAULT_EPOS_MERCHANT);
//        assertThat(testRecipient.getEposTerminal()).isEqualTo(DEFAULT_EPOS_TERMINAL);
        assertThat(testRecipient.getSettlementAccount()).isEqualTo(DEFAULT_SETTLEMENT_ACCOUNT);
        assertThat(testRecipient.getMfo()).isEqualTo(DEFAULT_MFO);
        assertThat(testRecipient.getInn()).isEqualTo(DEFAULT_INN);
        assertThat(testRecipient.getReconcile()).isEqualTo(DEFAULT_RECONCILE);
    }

    @Test
    @Transactional
    void createRecipientWithExistingId() throws Exception {
        // Create the Recipient with an existing ID
        recipient.setId(1L);
        RecipientDTO recipientDTO = recipientMapper.toDto(recipient);

        int databaseSizeBeforeCreate = recipientRepository.findAll().size();

        // An entity with an existing ID cannot be created, so this API call must fail
        restRecipientMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(recipientDTO)))
            .andExpect(status().isBadRequest());

        // Validate the Recipient in the database
        List<Recipient> recipientList = recipientRepository.findAll();
        assertThat(recipientList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkShortNameIsRequired() throws Exception {
        int databaseSizeBeforeTest = recipientRepository.findAll().size();
        // set the field null
        recipient.setShortName(null);

        // Create the Recipient, which fails.
        RecipientDTO recipientDTO = recipientMapper.toDto(recipient);

        restRecipientMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(recipientDTO)))
            .andExpect(status().isBadRequest());

        List<Recipient> recipientList = recipientRepository.findAll();
        assertThat(recipientList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkServiceIdIsRequired() throws Exception {
        int databaseSizeBeforeTest = recipientRepository.findAll().size();
        // set the field null
        recipient.setServiceId(null);

        // Create the Recipient, which fails.
        RecipientDTO recipientDTO = recipientMapper.toDto(recipient);

        restRecipientMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(recipientDTO)))
            .andExpect(status().isBadRequest());

        List<Recipient> recipientList = recipientRepository.findAll();
        assertThat(recipientList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkAmountRatioIsRequired() throws Exception {
        int databaseSizeBeforeTest = recipientRepository.findAll().size();
        // set the field null
        recipient.setAmountRatio(null);

        // Create the Recipient, which fails.
        RecipientDTO recipientDTO = recipientMapper.toDto(recipient);

        restRecipientMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(recipientDTO)))
            .andExpect(status().isBadRequest());

        List<Recipient> recipientList = recipientRepository.findAll();
        assertThat(recipientList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkCcyIsRequired() throws Exception {
        int databaseSizeBeforeTest = recipientRepository.findAll().size();
        // set the field null
        recipient.setCcy(null);

        // Create the Recipient, which fails.
        RecipientDTO recipientDTO = recipientMapper.toDto(recipient);

        restRecipientMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(recipientDTO)))
            .andExpect(status().isBadRequest());

        List<Recipient> recipientList = recipientRepository.findAll();
        assertThat(recipientList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkLimitRatioIsRequired() throws Exception {
        int databaseSizeBeforeTest = recipientRepository.findAll().size();
        // set the field null
        recipient.setLimitRatio(null);

        // Create the Recipient, which fails.
        RecipientDTO recipientDTO = recipientMapper.toDto(recipient);

        restRecipientMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(recipientDTO)))
            .andExpect(status().isBadRequest());

        List<Recipient> recipientList = recipientRepository.findAll();
        assertThat(recipientList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkActiveIsRequired() throws Exception {
        int databaseSizeBeforeTest = recipientRepository.findAll().size();
        // set the field null
        recipient.setActive(null);

        // Create the Recipient, which fails.
        RecipientDTO recipientDTO = recipientMapper.toDto(recipient);

        restRecipientMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(recipientDTO)))
            .andExpect(status().isBadRequest());

        List<Recipient> recipientList = recipientRepository.findAll();
        assertThat(recipientList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllRecipients() throws Exception {
        // Initialize the database
        recipientRepository.saveAndFlush(recipient);

        // Get all the recipientList
        restRecipientMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
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
//            .andExpect(jsonPath("$.[*].eposPort").value(hasItem(DEFAULT_EPOS_PORT)))
//            .andExpect(jsonPath("$.[*].eposMerchant").value(hasItem(DEFAULT_EPOS_MERCHANT.intValue())))
//            .andExpect(jsonPath("$.[*].eposTerminal").value(hasItem(DEFAULT_EPOS_TERMINAL.intValue())))
            .andExpect(jsonPath("$.[*].settlementAccount").value(hasItem(DEFAULT_SETTLEMENT_ACCOUNT)))
            .andExpect(jsonPath("$.[*].mfo").value(hasItem(DEFAULT_MFO)))
            .andExpect(jsonPath("$.[*].inn").value(hasItem(DEFAULT_INN)))
            .andExpect(jsonPath("$.[*].reconcile").value(hasItem(DEFAULT_RECONCILE.booleanValue())));
    }

    @SuppressWarnings({ "unchecked" })
    void getAllRecipientsWithEagerRelationshipsIsEnabled() throws Exception {
        when(recipientServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restRecipientMockMvc.perform(get(ENTITY_API_URL + "?eagerload=true")).andExpect(status().isOk());

        verify(recipientServiceMock, times(1)).findAllWithEagerRelationships(any());
    }

    @SuppressWarnings({ "unchecked" })
    void getAllRecipientsWithEagerRelationshipsIsNotEnabled() throws Exception {
        when(recipientServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restRecipientMockMvc.perform(get(ENTITY_API_URL + "?eagerload=true")).andExpect(status().isOk());

        verify(recipientServiceMock, times(1)).findAllWithEagerRelationships(any());
    }

    @Test
    @Transactional
    void getRecipient() throws Exception {
        // Initialize the database
        recipientRepository.saveAndFlush(recipient);

        // Get the recipient
        restRecipientMockMvc
            .perform(get(ENTITY_API_URL_ID, recipient.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
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
//            .andExpect(jsonPath("$.eposPort").value(DEFAULT_EPOS_PORT))
//            .andExpect(jsonPath("$.eposMerchant").value(DEFAULT_EPOS_MERCHANT.intValue()))
//            .andExpect(jsonPath("$.eposTerminal").value(DEFAULT_EPOS_TERMINAL.intValue()))
            .andExpect(jsonPath("$.settlementAccount").value(DEFAULT_SETTLEMENT_ACCOUNT))
            .andExpect(jsonPath("$.mfo").value(DEFAULT_MFO))
            .andExpect(jsonPath("$.inn").value(DEFAULT_INN))
            .andExpect(jsonPath("$.reconcile").value(DEFAULT_RECONCILE.booleanValue()));
    }

    @Test
    @Transactional
    void getRecipientsByIdFiltering() throws Exception {
        // Initialize the database
        recipientRepository.saveAndFlush(recipient);

        Long id = recipient.getId();

        defaultRecipientShouldBeFound("id.equals=" + id);
        defaultRecipientShouldNotBeFound("id.notEquals=" + id);

        defaultRecipientShouldBeFound("id.greaterThanOrEqual=" + id);
        defaultRecipientShouldNotBeFound("id.greaterThan=" + id);

        defaultRecipientShouldBeFound("id.lessThanOrEqual=" + id);
        defaultRecipientShouldNotBeFound("id.lessThan=" + id);
    }

    @Test
    @Transactional
    void getAllRecipientsByShortNameIsEqualToSomething() throws Exception {
        // Initialize the database
        recipientRepository.saveAndFlush(recipient);

        // Get all the recipientList where shortName equals to DEFAULT_SHORT_NAME
        defaultRecipientShouldBeFound("shortName.equals=" + DEFAULT_SHORT_NAME);

        // Get all the recipientList where shortName equals to UPDATED_SHORT_NAME
        defaultRecipientShouldNotBeFound("shortName.equals=" + UPDATED_SHORT_NAME);
    }

    @Test
    @Transactional
    void getAllRecipientsByShortNameIsNotEqualToSomething() throws Exception {
        // Initialize the database
        recipientRepository.saveAndFlush(recipient);

        // Get all the recipientList where shortName not equals to DEFAULT_SHORT_NAME
        defaultRecipientShouldNotBeFound("shortName.notEquals=" + DEFAULT_SHORT_NAME);

        // Get all the recipientList where shortName not equals to UPDATED_SHORT_NAME
        defaultRecipientShouldBeFound("shortName.notEquals=" + UPDATED_SHORT_NAME);
    }

    @Test
    @Transactional
    void getAllRecipientsByShortNameIsInShouldWork() throws Exception {
        // Initialize the database
        recipientRepository.saveAndFlush(recipient);

        // Get all the recipientList where shortName in DEFAULT_SHORT_NAME or UPDATED_SHORT_NAME
        defaultRecipientShouldBeFound("shortName.in=" + DEFAULT_SHORT_NAME + "," + UPDATED_SHORT_NAME);

        // Get all the recipientList where shortName equals to UPDATED_SHORT_NAME
        defaultRecipientShouldNotBeFound("shortName.in=" + UPDATED_SHORT_NAME);
    }

    @Test
    @Transactional
    void getAllRecipientsByShortNameIsNullOrNotNull() throws Exception {
        // Initialize the database
        recipientRepository.saveAndFlush(recipient);

        // Get all the recipientList where shortName is not null
        defaultRecipientShouldBeFound("shortName.specified=true");

        // Get all the recipientList where shortName is null
        defaultRecipientShouldNotBeFound("shortName.specified=false");
    }

    @Test
    @Transactional
    void getAllRecipientsByShortNameContainsSomething() throws Exception {
        // Initialize the database
        recipientRepository.saveAndFlush(recipient);

        // Get all the recipientList where shortName contains DEFAULT_SHORT_NAME
        defaultRecipientShouldBeFound("shortName.contains=" + DEFAULT_SHORT_NAME);

        // Get all the recipientList where shortName contains UPDATED_SHORT_NAME
        defaultRecipientShouldNotBeFound("shortName.contains=" + UPDATED_SHORT_NAME);
    }

    @Test
    @Transactional
    void getAllRecipientsByShortNameNotContainsSomething() throws Exception {
        // Initialize the database
        recipientRepository.saveAndFlush(recipient);

        // Get all the recipientList where shortName does not contain DEFAULT_SHORT_NAME
        defaultRecipientShouldNotBeFound("shortName.doesNotContain=" + DEFAULT_SHORT_NAME);

        // Get all the recipientList where shortName does not contain UPDATED_SHORT_NAME
        defaultRecipientShouldBeFound("shortName.doesNotContain=" + UPDATED_SHORT_NAME);
    }

    @Test
    @Transactional
    void getAllRecipientsByServiceIdIsEqualToSomething() throws Exception {
        // Initialize the database
        recipientRepository.saveAndFlush(recipient);

        // Get all the recipientList where serviceId equals to DEFAULT_SERVICE_ID
        defaultRecipientShouldBeFound("serviceId.equals=" + DEFAULT_SERVICE_ID);

        // Get all the recipientList where serviceId equals to UPDATED_SERVICE_ID
        defaultRecipientShouldNotBeFound("serviceId.equals=" + UPDATED_SERVICE_ID);
    }

    @Test
    @Transactional
    void getAllRecipientsByServiceIdIsNotEqualToSomething() throws Exception {
        // Initialize the database
        recipientRepository.saveAndFlush(recipient);

        // Get all the recipientList where serviceId not equals to DEFAULT_SERVICE_ID
        defaultRecipientShouldNotBeFound("serviceId.notEquals=" + DEFAULT_SERVICE_ID);

        // Get all the recipientList where serviceId not equals to UPDATED_SERVICE_ID
        defaultRecipientShouldBeFound("serviceId.notEquals=" + UPDATED_SERVICE_ID);
    }

    @Test
    @Transactional
    void getAllRecipientsByServiceIdIsInShouldWork() throws Exception {
        // Initialize the database
        recipientRepository.saveAndFlush(recipient);

        // Get all the recipientList where serviceId in DEFAULT_SERVICE_ID or UPDATED_SERVICE_ID
        defaultRecipientShouldBeFound("serviceId.in=" + DEFAULT_SERVICE_ID + "," + UPDATED_SERVICE_ID);

        // Get all the recipientList where serviceId equals to UPDATED_SERVICE_ID
        defaultRecipientShouldNotBeFound("serviceId.in=" + UPDATED_SERVICE_ID);
    }

    @Test
    @Transactional
    void getAllRecipientsByServiceIdIsNullOrNotNull() throws Exception {
        // Initialize the database
        recipientRepository.saveAndFlush(recipient);

        // Get all the recipientList where serviceId is not null
        defaultRecipientShouldBeFound("serviceId.specified=true");

        // Get all the recipientList where serviceId is null
        defaultRecipientShouldNotBeFound("serviceId.specified=false");
    }

    @Test
    @Transactional
    void getAllRecipientsByServiceIdContainsSomething() throws Exception {
        // Initialize the database
        recipientRepository.saveAndFlush(recipient);

        // Get all the recipientList where serviceId contains DEFAULT_SERVICE_ID
        defaultRecipientShouldBeFound("serviceId.contains=" + DEFAULT_SERVICE_ID);

        // Get all the recipientList where serviceId contains UPDATED_SERVICE_ID
        defaultRecipientShouldNotBeFound("serviceId.contains=" + UPDATED_SERVICE_ID);
    }

    @Test
    @Transactional
    void getAllRecipientsByServiceIdNotContainsSomething() throws Exception {
        // Initialize the database
        recipientRepository.saveAndFlush(recipient);

        // Get all the recipientList where serviceId does not contain DEFAULT_SERVICE_ID
        defaultRecipientShouldNotBeFound("serviceId.doesNotContain=" + DEFAULT_SERVICE_ID);

        // Get all the recipientList where serviceId does not contain UPDATED_SERVICE_ID
        defaultRecipientShouldBeFound("serviceId.doesNotContain=" + UPDATED_SERVICE_ID);
    }

    @Test
    @Transactional
    void getAllRecipientsByThumbnailIsEqualToSomething() throws Exception {
        // Initialize the database
        recipientRepository.saveAndFlush(recipient);

        // Get all the recipientList where thumbnail equals to DEFAULT_THUMBNAIL
        defaultRecipientShouldBeFound("thumbnail.equals=" + DEFAULT_THUMBNAIL);

        // Get all the recipientList where thumbnail equals to UPDATED_THUMBNAIL
        defaultRecipientShouldNotBeFound("thumbnail.equals=" + UPDATED_THUMBNAIL);
    }

    @Test
    @Transactional
    void getAllRecipientsByThumbnailIsNotEqualToSomething() throws Exception {
        // Initialize the database
        recipientRepository.saveAndFlush(recipient);

        // Get all the recipientList where thumbnail not equals to DEFAULT_THUMBNAIL
        defaultRecipientShouldNotBeFound("thumbnail.notEquals=" + DEFAULT_THUMBNAIL);

        // Get all the recipientList where thumbnail not equals to UPDATED_THUMBNAIL
        defaultRecipientShouldBeFound("thumbnail.notEquals=" + UPDATED_THUMBNAIL);
    }

    @Test
    @Transactional
    void getAllRecipientsByThumbnailIsInShouldWork() throws Exception {
        // Initialize the database
        recipientRepository.saveAndFlush(recipient);

        // Get all the recipientList where thumbnail in DEFAULT_THUMBNAIL or UPDATED_THUMBNAIL
        defaultRecipientShouldBeFound("thumbnail.in=" + DEFAULT_THUMBNAIL + "," + UPDATED_THUMBNAIL);

        // Get all the recipientList where thumbnail equals to UPDATED_THUMBNAIL
        defaultRecipientShouldNotBeFound("thumbnail.in=" + UPDATED_THUMBNAIL);
    }

    @Test
    @Transactional
    void getAllRecipientsByThumbnailIsNullOrNotNull() throws Exception {
        // Initialize the database
        recipientRepository.saveAndFlush(recipient);

        // Get all the recipientList where thumbnail is not null
        defaultRecipientShouldBeFound("thumbnail.specified=true");

        // Get all the recipientList where thumbnail is null
        defaultRecipientShouldNotBeFound("thumbnail.specified=false");
    }

    @Test
    @Transactional
    void getAllRecipientsByThumbnailContainsSomething() throws Exception {
        // Initialize the database
        recipientRepository.saveAndFlush(recipient);

        // Get all the recipientList where thumbnail contains DEFAULT_THUMBNAIL
        defaultRecipientShouldBeFound("thumbnail.contains=" + DEFAULT_THUMBNAIL);

        // Get all the recipientList where thumbnail contains UPDATED_THUMBNAIL
        defaultRecipientShouldNotBeFound("thumbnail.contains=" + UPDATED_THUMBNAIL);
    }

    @Test
    @Transactional
    void getAllRecipientsByThumbnailNotContainsSomething() throws Exception {
        // Initialize the database
        recipientRepository.saveAndFlush(recipient);

        // Get all the recipientList where thumbnail does not contain DEFAULT_THUMBNAIL
        defaultRecipientShouldNotBeFound("thumbnail.doesNotContain=" + DEFAULT_THUMBNAIL);

        // Get all the recipientList where thumbnail does not contain UPDATED_THUMBNAIL
        defaultRecipientShouldBeFound("thumbnail.doesNotContain=" + UPDATED_THUMBNAIL);
    }

    @Test
    @Transactional
    void getAllRecipientsByLogoIsEqualToSomething() throws Exception {
        // Initialize the database
        recipientRepository.saveAndFlush(recipient);

        // Get all the recipientList where logo equals to DEFAULT_LOGO
        defaultRecipientShouldBeFound("logo.equals=" + DEFAULT_LOGO);

        // Get all the recipientList where logo equals to UPDATED_LOGO
        defaultRecipientShouldNotBeFound("logo.equals=" + UPDATED_LOGO);
    }

    @Test
    @Transactional
    void getAllRecipientsByLogoIsNotEqualToSomething() throws Exception {
        // Initialize the database
        recipientRepository.saveAndFlush(recipient);

        // Get all the recipientList where logo not equals to DEFAULT_LOGO
        defaultRecipientShouldNotBeFound("logo.notEquals=" + DEFAULT_LOGO);

        // Get all the recipientList where logo not equals to UPDATED_LOGO
        defaultRecipientShouldBeFound("logo.notEquals=" + UPDATED_LOGO);
    }

    @Test
    @Transactional
    void getAllRecipientsByLogoIsInShouldWork() throws Exception {
        // Initialize the database
        recipientRepository.saveAndFlush(recipient);

        // Get all the recipientList where logo in DEFAULT_LOGO or UPDATED_LOGO
        defaultRecipientShouldBeFound("logo.in=" + DEFAULT_LOGO + "," + UPDATED_LOGO);

        // Get all the recipientList where logo equals to UPDATED_LOGO
        defaultRecipientShouldNotBeFound("logo.in=" + UPDATED_LOGO);
    }

    @Test
    @Transactional
    void getAllRecipientsByLogoIsNullOrNotNull() throws Exception {
        // Initialize the database
        recipientRepository.saveAndFlush(recipient);

        // Get all the recipientList where logo is not null
        defaultRecipientShouldBeFound("logo.specified=true");

        // Get all the recipientList where logo is null
        defaultRecipientShouldNotBeFound("logo.specified=false");
    }

    @Test
    @Transactional
    void getAllRecipientsByLogoContainsSomething() throws Exception {
        // Initialize the database
        recipientRepository.saveAndFlush(recipient);

        // Get all the recipientList where logo contains DEFAULT_LOGO
        defaultRecipientShouldBeFound("logo.contains=" + DEFAULT_LOGO);

        // Get all the recipientList where logo contains UPDATED_LOGO
        defaultRecipientShouldNotBeFound("logo.contains=" + UPDATED_LOGO);
    }

    @Test
    @Transactional
    void getAllRecipientsByLogoNotContainsSomething() throws Exception {
        // Initialize the database
        recipientRepository.saveAndFlush(recipient);

        // Get all the recipientList where logo does not contain DEFAULT_LOGO
        defaultRecipientShouldNotBeFound("logo.doesNotContain=" + DEFAULT_LOGO);

        // Get all the recipientList where logo does not contain UPDATED_LOGO
        defaultRecipientShouldBeFound("logo.doesNotContain=" + UPDATED_LOGO);
    }

    @Test
    @Transactional
    void getAllRecipientsByAmountRatioIsEqualToSomething() throws Exception {
        // Initialize the database
        recipientRepository.saveAndFlush(recipient);

        // Get all the recipientList where amountRatio equals to DEFAULT_AMOUNT_RATIO
        defaultRecipientShouldBeFound("amountRatio.equals=" + DEFAULT_AMOUNT_RATIO);

        // Get all the recipientList where amountRatio equals to UPDATED_AMOUNT_RATIO
        defaultRecipientShouldNotBeFound("amountRatio.equals=" + UPDATED_AMOUNT_RATIO);
    }

    @Test
    @Transactional
    void getAllRecipientsByAmountRatioIsNotEqualToSomething() throws Exception {
        // Initialize the database
        recipientRepository.saveAndFlush(recipient);

        // Get all the recipientList where amountRatio not equals to DEFAULT_AMOUNT_RATIO
        defaultRecipientShouldNotBeFound("amountRatio.notEquals=" + DEFAULT_AMOUNT_RATIO);

        // Get all the recipientList where amountRatio not equals to UPDATED_AMOUNT_RATIO
        defaultRecipientShouldBeFound("amountRatio.notEquals=" + UPDATED_AMOUNT_RATIO);
    }

    @Test
    @Transactional
    void getAllRecipientsByAmountRatioIsInShouldWork() throws Exception {
        // Initialize the database
        recipientRepository.saveAndFlush(recipient);

        // Get all the recipientList where amountRatio in DEFAULT_AMOUNT_RATIO or UPDATED_AMOUNT_RATIO
        defaultRecipientShouldBeFound("amountRatio.in=" + DEFAULT_AMOUNT_RATIO + "," + UPDATED_AMOUNT_RATIO);

        // Get all the recipientList where amountRatio equals to UPDATED_AMOUNT_RATIO
        defaultRecipientShouldNotBeFound("amountRatio.in=" + UPDATED_AMOUNT_RATIO);
    }

    @Test
    @Transactional
    void getAllRecipientsByAmountRatioIsNullOrNotNull() throws Exception {
        // Initialize the database
        recipientRepository.saveAndFlush(recipient);

        // Get all the recipientList where amountRatio is not null
        defaultRecipientShouldBeFound("amountRatio.specified=true");

        // Get all the recipientList where amountRatio is null
        defaultRecipientShouldNotBeFound("amountRatio.specified=false");
    }

    @Test
    @Transactional
    void getAllRecipientsByAmountRatioIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        recipientRepository.saveAndFlush(recipient);

        // Get all the recipientList where amountRatio is greater than or equal to DEFAULT_AMOUNT_RATIO
        defaultRecipientShouldBeFound("amountRatio.greaterThanOrEqual=" + DEFAULT_AMOUNT_RATIO);

        // Get all the recipientList where amountRatio is greater than or equal to UPDATED_AMOUNT_RATIO
        defaultRecipientShouldNotBeFound("amountRatio.greaterThanOrEqual=" + UPDATED_AMOUNT_RATIO);
    }

    @Test
    @Transactional
    void getAllRecipientsByAmountRatioIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        recipientRepository.saveAndFlush(recipient);

        // Get all the recipientList where amountRatio is less than or equal to DEFAULT_AMOUNT_RATIO
        defaultRecipientShouldBeFound("amountRatio.lessThanOrEqual=" + DEFAULT_AMOUNT_RATIO);

        // Get all the recipientList where amountRatio is less than or equal to SMALLER_AMOUNT_RATIO
        defaultRecipientShouldNotBeFound("amountRatio.lessThanOrEqual=" + SMALLER_AMOUNT_RATIO);
    }

    @Test
    @Transactional
    void getAllRecipientsByAmountRatioIsLessThanSomething() throws Exception {
        // Initialize the database
        recipientRepository.saveAndFlush(recipient);

        // Get all the recipientList where amountRatio is less than DEFAULT_AMOUNT_RATIO
        defaultRecipientShouldNotBeFound("amountRatio.lessThan=" + DEFAULT_AMOUNT_RATIO);

        // Get all the recipientList where amountRatio is less than UPDATED_AMOUNT_RATIO
        defaultRecipientShouldBeFound("amountRatio.lessThan=" + UPDATED_AMOUNT_RATIO);
    }

    @Test
    @Transactional
    void getAllRecipientsByAmountRatioIsGreaterThanSomething() throws Exception {
        // Initialize the database
        recipientRepository.saveAndFlush(recipient);

        // Get all the recipientList where amountRatio is greater than DEFAULT_AMOUNT_RATIO
        defaultRecipientShouldNotBeFound("amountRatio.greaterThan=" + DEFAULT_AMOUNT_RATIO);

        // Get all the recipientList where amountRatio is greater than SMALLER_AMOUNT_RATIO
        defaultRecipientShouldBeFound("amountRatio.greaterThan=" + SMALLER_AMOUNT_RATIO);
    }

    @Test
    @Transactional
    void getAllRecipientsByCcyIsEqualToSomething() throws Exception {
        // Initialize the database
        recipientRepository.saveAndFlush(recipient);

        // Get all the recipientList where ccy equals to DEFAULT_CCY
        defaultRecipientShouldBeFound("ccy.equals=" + DEFAULT_CCY);

        // Get all the recipientList where ccy equals to UPDATED_CCY
        defaultRecipientShouldNotBeFound("ccy.equals=" + UPDATED_CCY);
    }

    @Test
    @Transactional
    void getAllRecipientsByCcyIsNotEqualToSomething() throws Exception {
        // Initialize the database
        recipientRepository.saveAndFlush(recipient);

        // Get all the recipientList where ccy not equals to DEFAULT_CCY
        defaultRecipientShouldNotBeFound("ccy.notEquals=" + DEFAULT_CCY);

        // Get all the recipientList where ccy not equals to UPDATED_CCY
        defaultRecipientShouldBeFound("ccy.notEquals=" + UPDATED_CCY);
    }

    @Test
    @Transactional
    void getAllRecipientsByCcyIsInShouldWork() throws Exception {
        // Initialize the database
        recipientRepository.saveAndFlush(recipient);

        // Get all the recipientList where ccy in DEFAULT_CCY or UPDATED_CCY
        defaultRecipientShouldBeFound("ccy.in=" + DEFAULT_CCY + "," + UPDATED_CCY);

        // Get all the recipientList where ccy equals to UPDATED_CCY
        defaultRecipientShouldNotBeFound("ccy.in=" + UPDATED_CCY);
    }

    @Test
    @Transactional
    void getAllRecipientsByCcyIsNullOrNotNull() throws Exception {
        // Initialize the database
        recipientRepository.saveAndFlush(recipient);

        // Get all the recipientList where ccy is not null
        defaultRecipientShouldBeFound("ccy.specified=true");

        // Get all the recipientList where ccy is null
        defaultRecipientShouldNotBeFound("ccy.specified=false");
    }

    @Test
    @Transactional
    void getAllRecipientsByCcyIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        recipientRepository.saveAndFlush(recipient);

        // Get all the recipientList where ccy is greater than or equal to DEFAULT_CCY
        defaultRecipientShouldBeFound("ccy.greaterThanOrEqual=" + DEFAULT_CCY);

        // Get all the recipientList where ccy is greater than or equal to UPDATED_CCY
        defaultRecipientShouldNotBeFound("ccy.greaterThanOrEqual=" + UPDATED_CCY);
    }

    @Test
    @Transactional
    void getAllRecipientsByCcyIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        recipientRepository.saveAndFlush(recipient);

        // Get all the recipientList where ccy is less than or equal to DEFAULT_CCY
        defaultRecipientShouldBeFound("ccy.lessThanOrEqual=" + DEFAULT_CCY);

        // Get all the recipientList where ccy is less than or equal to SMALLER_CCY
        defaultRecipientShouldNotBeFound("ccy.lessThanOrEqual=" + SMALLER_CCY);
    }

    @Test
    @Transactional
    void getAllRecipientsByCcyIsLessThanSomething() throws Exception {
        // Initialize the database
        recipientRepository.saveAndFlush(recipient);

        // Get all the recipientList where ccy is less than DEFAULT_CCY
        defaultRecipientShouldNotBeFound("ccy.lessThan=" + DEFAULT_CCY);

        // Get all the recipientList where ccy is less than UPDATED_CCY
        defaultRecipientShouldBeFound("ccy.lessThan=" + UPDATED_CCY);
    }

    @Test
    @Transactional
    void getAllRecipientsByCcyIsGreaterThanSomething() throws Exception {
        // Initialize the database
        recipientRepository.saveAndFlush(recipient);

        // Get all the recipientList where ccy is greater than DEFAULT_CCY
        defaultRecipientShouldNotBeFound("ccy.greaterThan=" + DEFAULT_CCY);

        // Get all the recipientList where ccy is greater than SMALLER_CCY
        defaultRecipientShouldBeFound("ccy.greaterThan=" + SMALLER_CCY);
    }

    @Test
    @Transactional
    void getAllRecipientsByMinAmountIsEqualToSomething() throws Exception {
        // Initialize the database
        recipientRepository.saveAndFlush(recipient);

        // Get all the recipientList where minAmount equals to DEFAULT_MIN_AMOUNT
        defaultRecipientShouldBeFound("minAmount.equals=" + DEFAULT_MIN_AMOUNT);

        // Get all the recipientList where minAmount equals to UPDATED_MIN_AMOUNT
        defaultRecipientShouldNotBeFound("minAmount.equals=" + UPDATED_MIN_AMOUNT);
    }

    @Test
    @Transactional
    void getAllRecipientsByMinAmountIsNotEqualToSomething() throws Exception {
        // Initialize the database
        recipientRepository.saveAndFlush(recipient);

        // Get all the recipientList where minAmount not equals to DEFAULT_MIN_AMOUNT
        defaultRecipientShouldNotBeFound("minAmount.notEquals=" + DEFAULT_MIN_AMOUNT);

        // Get all the recipientList where minAmount not equals to UPDATED_MIN_AMOUNT
        defaultRecipientShouldBeFound("minAmount.notEquals=" + UPDATED_MIN_AMOUNT);
    }

    @Test
    @Transactional
    void getAllRecipientsByMinAmountIsInShouldWork() throws Exception {
        // Initialize the database
        recipientRepository.saveAndFlush(recipient);

        // Get all the recipientList where minAmount in DEFAULT_MIN_AMOUNT or UPDATED_MIN_AMOUNT
        defaultRecipientShouldBeFound("minAmount.in=" + DEFAULT_MIN_AMOUNT + "," + UPDATED_MIN_AMOUNT);

        // Get all the recipientList where minAmount equals to UPDATED_MIN_AMOUNT
        defaultRecipientShouldNotBeFound("minAmount.in=" + UPDATED_MIN_AMOUNT);
    }

    @Test
    @Transactional
    void getAllRecipientsByMinAmountIsNullOrNotNull() throws Exception {
        // Initialize the database
        recipientRepository.saveAndFlush(recipient);

        // Get all the recipientList where minAmount is not null
        defaultRecipientShouldBeFound("minAmount.specified=true");

        // Get all the recipientList where minAmount is null
        defaultRecipientShouldNotBeFound("minAmount.specified=false");
    }

    @Test
    @Transactional
    void getAllRecipientsByMinAmountIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        recipientRepository.saveAndFlush(recipient);

        // Get all the recipientList where minAmount is greater than or equal to DEFAULT_MIN_AMOUNT
        defaultRecipientShouldBeFound("minAmount.greaterThanOrEqual=" + DEFAULT_MIN_AMOUNT);

        // Get all the recipientList where minAmount is greater than or equal to UPDATED_MIN_AMOUNT
        defaultRecipientShouldNotBeFound("minAmount.greaterThanOrEqual=" + UPDATED_MIN_AMOUNT);
    }

    @Test
    @Transactional
    void getAllRecipientsByMinAmountIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        recipientRepository.saveAndFlush(recipient);

        // Get all the recipientList where minAmount is less than or equal to DEFAULT_MIN_AMOUNT
        defaultRecipientShouldBeFound("minAmount.lessThanOrEqual=" + DEFAULT_MIN_AMOUNT);

        // Get all the recipientList where minAmount is less than or equal to SMALLER_MIN_AMOUNT
        defaultRecipientShouldNotBeFound("minAmount.lessThanOrEqual=" + SMALLER_MIN_AMOUNT);
    }

    @Test
    @Transactional
    void getAllRecipientsByMinAmountIsLessThanSomething() throws Exception {
        // Initialize the database
        recipientRepository.saveAndFlush(recipient);

        // Get all the recipientList where minAmount is less than DEFAULT_MIN_AMOUNT
        defaultRecipientShouldNotBeFound("minAmount.lessThan=" + DEFAULT_MIN_AMOUNT);

        // Get all the recipientList where minAmount is less than UPDATED_MIN_AMOUNT
        defaultRecipientShouldBeFound("minAmount.lessThan=" + UPDATED_MIN_AMOUNT);
    }

    @Test
    @Transactional
    void getAllRecipientsByMinAmountIsGreaterThanSomething() throws Exception {
        // Initialize the database
        recipientRepository.saveAndFlush(recipient);

        // Get all the recipientList where minAmount is greater than DEFAULT_MIN_AMOUNT
        defaultRecipientShouldNotBeFound("minAmount.greaterThan=" + DEFAULT_MIN_AMOUNT);

        // Get all the recipientList where minAmount is greater than SMALLER_MIN_AMOUNT
        defaultRecipientShouldBeFound("minAmount.greaterThan=" + SMALLER_MIN_AMOUNT);
    }

    @Test
    @Transactional
    void getAllRecipientsByMaxAmountIsEqualToSomething() throws Exception {
        // Initialize the database
        recipientRepository.saveAndFlush(recipient);

        // Get all the recipientList where maxAmount equals to DEFAULT_MAX_AMOUNT
        defaultRecipientShouldBeFound("maxAmount.equals=" + DEFAULT_MAX_AMOUNT);

        // Get all the recipientList where maxAmount equals to UPDATED_MAX_AMOUNT
        defaultRecipientShouldNotBeFound("maxAmount.equals=" + UPDATED_MAX_AMOUNT);
    }

    @Test
    @Transactional
    void getAllRecipientsByMaxAmountIsNotEqualToSomething() throws Exception {
        // Initialize the database
        recipientRepository.saveAndFlush(recipient);

        // Get all the recipientList where maxAmount not equals to DEFAULT_MAX_AMOUNT
        defaultRecipientShouldNotBeFound("maxAmount.notEquals=" + DEFAULT_MAX_AMOUNT);

        // Get all the recipientList where maxAmount not equals to UPDATED_MAX_AMOUNT
        defaultRecipientShouldBeFound("maxAmount.notEquals=" + UPDATED_MAX_AMOUNT);
    }

    @Test
    @Transactional
    void getAllRecipientsByMaxAmountIsInShouldWork() throws Exception {
        // Initialize the database
        recipientRepository.saveAndFlush(recipient);

        // Get all the recipientList where maxAmount in DEFAULT_MAX_AMOUNT or UPDATED_MAX_AMOUNT
        defaultRecipientShouldBeFound("maxAmount.in=" + DEFAULT_MAX_AMOUNT + "," + UPDATED_MAX_AMOUNT);

        // Get all the recipientList where maxAmount equals to UPDATED_MAX_AMOUNT
        defaultRecipientShouldNotBeFound("maxAmount.in=" + UPDATED_MAX_AMOUNT);
    }

    @Test
    @Transactional
    void getAllRecipientsByMaxAmountIsNullOrNotNull() throws Exception {
        // Initialize the database
        recipientRepository.saveAndFlush(recipient);

        // Get all the recipientList where maxAmount is not null
        defaultRecipientShouldBeFound("maxAmount.specified=true");

        // Get all the recipientList where maxAmount is null
        defaultRecipientShouldNotBeFound("maxAmount.specified=false");
    }

    @Test
    @Transactional
    void getAllRecipientsByMaxAmountIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        recipientRepository.saveAndFlush(recipient);

        // Get all the recipientList where maxAmount is greater than or equal to DEFAULT_MAX_AMOUNT
        defaultRecipientShouldBeFound("maxAmount.greaterThanOrEqual=" + DEFAULT_MAX_AMOUNT);

        // Get all the recipientList where maxAmount is greater than or equal to UPDATED_MAX_AMOUNT
        defaultRecipientShouldNotBeFound("maxAmount.greaterThanOrEqual=" + UPDATED_MAX_AMOUNT);
    }

    @Test
    @Transactional
    void getAllRecipientsByMaxAmountIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        recipientRepository.saveAndFlush(recipient);

        // Get all the recipientList where maxAmount is less than or equal to DEFAULT_MAX_AMOUNT
        defaultRecipientShouldBeFound("maxAmount.lessThanOrEqual=" + DEFAULT_MAX_AMOUNT);

        // Get all the recipientList where maxAmount is less than or equal to SMALLER_MAX_AMOUNT
        defaultRecipientShouldNotBeFound("maxAmount.lessThanOrEqual=" + SMALLER_MAX_AMOUNT);
    }

    @Test
    @Transactional
    void getAllRecipientsByMaxAmountIsLessThanSomething() throws Exception {
        // Initialize the database
        recipientRepository.saveAndFlush(recipient);

        // Get all the recipientList where maxAmount is less than DEFAULT_MAX_AMOUNT
        defaultRecipientShouldNotBeFound("maxAmount.lessThan=" + DEFAULT_MAX_AMOUNT);

        // Get all the recipientList where maxAmount is less than UPDATED_MAX_AMOUNT
        defaultRecipientShouldBeFound("maxAmount.lessThan=" + UPDATED_MAX_AMOUNT);
    }

    @Test
    @Transactional
    void getAllRecipientsByMaxAmountIsGreaterThanSomething() throws Exception {
        // Initialize the database
        recipientRepository.saveAndFlush(recipient);

        // Get all the recipientList where maxAmount is greater than DEFAULT_MAX_AMOUNT
        defaultRecipientShouldNotBeFound("maxAmount.greaterThan=" + DEFAULT_MAX_AMOUNT);

        // Get all the recipientList where maxAmount is greater than SMALLER_MAX_AMOUNT
        defaultRecipientShouldBeFound("maxAmount.greaterThan=" + SMALLER_MAX_AMOUNT);
    }

    @Test
    @Transactional
    void getAllRecipientsByLimitRatioIsEqualToSomething() throws Exception {
        // Initialize the database
        recipientRepository.saveAndFlush(recipient);

        // Get all the recipientList where limitRatio equals to DEFAULT_LIMIT_RATIO
        defaultRecipientShouldBeFound("limitRatio.equals=" + DEFAULT_LIMIT_RATIO);

        // Get all the recipientList where limitRatio equals to UPDATED_LIMIT_RATIO
        defaultRecipientShouldNotBeFound("limitRatio.equals=" + UPDATED_LIMIT_RATIO);
    }

    @Test
    @Transactional
    void getAllRecipientsByLimitRatioIsNotEqualToSomething() throws Exception {
        // Initialize the database
        recipientRepository.saveAndFlush(recipient);

        // Get all the recipientList where limitRatio not equals to DEFAULT_LIMIT_RATIO
        defaultRecipientShouldNotBeFound("limitRatio.notEquals=" + DEFAULT_LIMIT_RATIO);

        // Get all the recipientList where limitRatio not equals to UPDATED_LIMIT_RATIO
        defaultRecipientShouldBeFound("limitRatio.notEquals=" + UPDATED_LIMIT_RATIO);
    }

    @Test
    @Transactional
    void getAllRecipientsByLimitRatioIsInShouldWork() throws Exception {
        // Initialize the database
        recipientRepository.saveAndFlush(recipient);

        // Get all the recipientList where limitRatio in DEFAULT_LIMIT_RATIO or UPDATED_LIMIT_RATIO
        defaultRecipientShouldBeFound("limitRatio.in=" + DEFAULT_LIMIT_RATIO + "," + UPDATED_LIMIT_RATIO);

        // Get all the recipientList where limitRatio equals to UPDATED_LIMIT_RATIO
        defaultRecipientShouldNotBeFound("limitRatio.in=" + UPDATED_LIMIT_RATIO);
    }

    @Test
    @Transactional
    void getAllRecipientsByLimitRatioIsNullOrNotNull() throws Exception {
        // Initialize the database
        recipientRepository.saveAndFlush(recipient);

        // Get all the recipientList where limitRatio is not null
        defaultRecipientShouldBeFound("limitRatio.specified=true");

        // Get all the recipientList where limitRatio is null
        defaultRecipientShouldNotBeFound("limitRatio.specified=false");
    }

    @Test
    @Transactional
    void getAllRecipientsByLimitRatioIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        recipientRepository.saveAndFlush(recipient);

        // Get all the recipientList where limitRatio is greater than or equal to DEFAULT_LIMIT_RATIO
        defaultRecipientShouldBeFound("limitRatio.greaterThanOrEqual=" + DEFAULT_LIMIT_RATIO);

        // Get all the recipientList where limitRatio is greater than or equal to UPDATED_LIMIT_RATIO
        defaultRecipientShouldNotBeFound("limitRatio.greaterThanOrEqual=" + UPDATED_LIMIT_RATIO);
    }

    @Test
    @Transactional
    void getAllRecipientsByLimitRatioIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        recipientRepository.saveAndFlush(recipient);

        // Get all the recipientList where limitRatio is less than or equal to DEFAULT_LIMIT_RATIO
        defaultRecipientShouldBeFound("limitRatio.lessThanOrEqual=" + DEFAULT_LIMIT_RATIO);

        // Get all the recipientList where limitRatio is less than or equal to SMALLER_LIMIT_RATIO
        defaultRecipientShouldNotBeFound("limitRatio.lessThanOrEqual=" + SMALLER_LIMIT_RATIO);
    }

    @Test
    @Transactional
    void getAllRecipientsByLimitRatioIsLessThanSomething() throws Exception {
        // Initialize the database
        recipientRepository.saveAndFlush(recipient);

        // Get all the recipientList where limitRatio is less than DEFAULT_LIMIT_RATIO
        defaultRecipientShouldNotBeFound("limitRatio.lessThan=" + DEFAULT_LIMIT_RATIO);

        // Get all the recipientList where limitRatio is less than UPDATED_LIMIT_RATIO
        defaultRecipientShouldBeFound("limitRatio.lessThan=" + UPDATED_LIMIT_RATIO);
    }

    @Test
    @Transactional
    void getAllRecipientsByLimitRatioIsGreaterThanSomething() throws Exception {
        // Initialize the database
        recipientRepository.saveAndFlush(recipient);

        // Get all the recipientList where limitRatio is greater than DEFAULT_LIMIT_RATIO
        defaultRecipientShouldNotBeFound("limitRatio.greaterThan=" + DEFAULT_LIMIT_RATIO);

        // Get all the recipientList where limitRatio is greater than SMALLER_LIMIT_RATIO
        defaultRecipientShouldBeFound("limitRatio.greaterThan=" + SMALLER_LIMIT_RATIO);
    }

    @Test
    @Transactional
    void getAllRecipientsByCommissionIsEqualToSomething() throws Exception {
        // Initialize the database
        recipientRepository.saveAndFlush(recipient);

        // Get all the recipientList where commission equals to DEFAULT_COMMISSION
        defaultRecipientShouldBeFound("commission.equals=" + DEFAULT_COMMISSION);

        // Get all the recipientList where commission equals to UPDATED_COMMISSION
        defaultRecipientShouldNotBeFound("commission.equals=" + UPDATED_COMMISSION);
    }

    @Test
    @Transactional
    void getAllRecipientsByCommissionIsNotEqualToSomething() throws Exception {
        // Initialize the database
        recipientRepository.saveAndFlush(recipient);

        // Get all the recipientList where commission not equals to DEFAULT_COMMISSION
        defaultRecipientShouldNotBeFound("commission.notEquals=" + DEFAULT_COMMISSION);

        // Get all the recipientList where commission not equals to UPDATED_COMMISSION
        defaultRecipientShouldBeFound("commission.notEquals=" + UPDATED_COMMISSION);
    }

    @Test
    @Transactional
    void getAllRecipientsByCommissionIsInShouldWork() throws Exception {
        // Initialize the database
        recipientRepository.saveAndFlush(recipient);

        // Get all the recipientList where commission in DEFAULT_COMMISSION or UPDATED_COMMISSION
        defaultRecipientShouldBeFound("commission.in=" + DEFAULT_COMMISSION + "," + UPDATED_COMMISSION);

        // Get all the recipientList where commission equals to UPDATED_COMMISSION
        defaultRecipientShouldNotBeFound("commission.in=" + UPDATED_COMMISSION);
    }

    @Test
    @Transactional
    void getAllRecipientsByCommissionIsNullOrNotNull() throws Exception {
        // Initialize the database
        recipientRepository.saveAndFlush(recipient);

        // Get all the recipientList where commission is not null
        defaultRecipientShouldBeFound("commission.specified=true");

        // Get all the recipientList where commission is null
        defaultRecipientShouldNotBeFound("commission.specified=false");
    }

    @Test
    @Transactional
    void getAllRecipientsByCommissionIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        recipientRepository.saveAndFlush(recipient);

        // Get all the recipientList where commission is greater than or equal to DEFAULT_COMMISSION
        defaultRecipientShouldBeFound("commission.greaterThanOrEqual=" + DEFAULT_COMMISSION);

        // Get all the recipientList where commission is greater than or equal to UPDATED_COMMISSION
        defaultRecipientShouldNotBeFound("commission.greaterThanOrEqual=" + UPDATED_COMMISSION);
    }

    @Test
    @Transactional
    void getAllRecipientsByCommissionIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        recipientRepository.saveAndFlush(recipient);

        // Get all the recipientList where commission is less than or equal to DEFAULT_COMMISSION
        defaultRecipientShouldBeFound("commission.lessThanOrEqual=" + DEFAULT_COMMISSION);

        // Get all the recipientList where commission is less than or equal to SMALLER_COMMISSION
        defaultRecipientShouldNotBeFound("commission.lessThanOrEqual=" + SMALLER_COMMISSION);
    }

    @Test
    @Transactional
    void getAllRecipientsByCommissionIsLessThanSomething() throws Exception {
        // Initialize the database
        recipientRepository.saveAndFlush(recipient);

        // Get all the recipientList where commission is less than DEFAULT_COMMISSION
        defaultRecipientShouldNotBeFound("commission.lessThan=" + DEFAULT_COMMISSION);

        // Get all the recipientList where commission is less than UPDATED_COMMISSION
        defaultRecipientShouldBeFound("commission.lessThan=" + UPDATED_COMMISSION);
    }

    @Test
    @Transactional
    void getAllRecipientsByCommissionIsGreaterThanSomething() throws Exception {
        // Initialize the database
        recipientRepository.saveAndFlush(recipient);

        // Get all the recipientList where commission is greater than DEFAULT_COMMISSION
        defaultRecipientShouldNotBeFound("commission.greaterThan=" + DEFAULT_COMMISSION);

        // Get all the recipientList where commission is greater than SMALLER_COMMISSION
        defaultRecipientShouldBeFound("commission.greaterThan=" + SMALLER_COMMISSION);
    }

    @Test
    @Transactional
    void getAllRecipientsByFeeIsEqualToSomething() throws Exception {
        // Initialize the database
        recipientRepository.saveAndFlush(recipient);

        // Get all the recipientList where fee equals to DEFAULT_FEE
        defaultRecipientShouldBeFound("fee.equals=" + DEFAULT_FEE);

        // Get all the recipientList where fee equals to UPDATED_FEE
        defaultRecipientShouldNotBeFound("fee.equals=" + UPDATED_FEE);
    }

    @Test
    @Transactional
    void getAllRecipientsByFeeIsNotEqualToSomething() throws Exception {
        // Initialize the database
        recipientRepository.saveAndFlush(recipient);

        // Get all the recipientList where fee not equals to DEFAULT_FEE
        defaultRecipientShouldNotBeFound("fee.notEquals=" + DEFAULT_FEE);

        // Get all the recipientList where fee not equals to UPDATED_FEE
        defaultRecipientShouldBeFound("fee.notEquals=" + UPDATED_FEE);
    }

    @Test
    @Transactional
    void getAllRecipientsByFeeIsInShouldWork() throws Exception {
        // Initialize the database
        recipientRepository.saveAndFlush(recipient);

        // Get all the recipientList where fee in DEFAULT_FEE or UPDATED_FEE
        defaultRecipientShouldBeFound("fee.in=" + DEFAULT_FEE + "," + UPDATED_FEE);

        // Get all the recipientList where fee equals to UPDATED_FEE
        defaultRecipientShouldNotBeFound("fee.in=" + UPDATED_FEE);
    }

    @Test
    @Transactional
    void getAllRecipientsByFeeIsNullOrNotNull() throws Exception {
        // Initialize the database
        recipientRepository.saveAndFlush(recipient);

        // Get all the recipientList where fee is not null
        defaultRecipientShouldBeFound("fee.specified=true");

        // Get all the recipientList where fee is null
        defaultRecipientShouldNotBeFound("fee.specified=false");
    }

    @Test
    @Transactional
    void getAllRecipientsByFeeIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        recipientRepository.saveAndFlush(recipient);

        // Get all the recipientList where fee is greater than or equal to DEFAULT_FEE
        defaultRecipientShouldBeFound("fee.greaterThanOrEqual=" + DEFAULT_FEE);

        // Get all the recipientList where fee is greater than or equal to UPDATED_FEE
        defaultRecipientShouldNotBeFound("fee.greaterThanOrEqual=" + UPDATED_FEE);
    }

    @Test
    @Transactional
    void getAllRecipientsByFeeIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        recipientRepository.saveAndFlush(recipient);

        // Get all the recipientList where fee is less than or equal to DEFAULT_FEE
        defaultRecipientShouldBeFound("fee.lessThanOrEqual=" + DEFAULT_FEE);

        // Get all the recipientList where fee is less than or equal to SMALLER_FEE
        defaultRecipientShouldNotBeFound("fee.lessThanOrEqual=" + SMALLER_FEE);
    }

    @Test
    @Transactional
    void getAllRecipientsByFeeIsLessThanSomething() throws Exception {
        // Initialize the database
        recipientRepository.saveAndFlush(recipient);

        // Get all the recipientList where fee is less than DEFAULT_FEE
        defaultRecipientShouldNotBeFound("fee.lessThan=" + DEFAULT_FEE);

        // Get all the recipientList where fee is less than UPDATED_FEE
        defaultRecipientShouldBeFound("fee.lessThan=" + UPDATED_FEE);
    }

    @Test
    @Transactional
    void getAllRecipientsByFeeIsGreaterThanSomething() throws Exception {
        // Initialize the database
        recipientRepository.saveAndFlush(recipient);

        // Get all the recipientList where fee is greater than DEFAULT_FEE
        defaultRecipientShouldNotBeFound("fee.greaterThan=" + DEFAULT_FEE);

        // Get all the recipientList where fee is greater than SMALLER_FEE
        defaultRecipientShouldBeFound("fee.greaterThan=" + SMALLER_FEE);
    }

    @Test
    @Transactional
    void getAllRecipientsBySystemCommissionIsEqualToSomething() throws Exception {
        // Initialize the database
        recipientRepository.saveAndFlush(recipient);

        // Get all the recipientList where systemCommission equals to DEFAULT_SYSTEM_COMMISSION
        defaultRecipientShouldBeFound("systemCommission.equals=" + DEFAULT_SYSTEM_COMMISSION);

        // Get all the recipientList where systemCommission equals to UPDATED_SYSTEM_COMMISSION
        defaultRecipientShouldNotBeFound("systemCommission.equals=" + UPDATED_SYSTEM_COMMISSION);
    }

    @Test
    @Transactional
    void getAllRecipientsBySystemCommissionIsNotEqualToSomething() throws Exception {
        // Initialize the database
        recipientRepository.saveAndFlush(recipient);

        // Get all the recipientList where systemCommission not equals to DEFAULT_SYSTEM_COMMISSION
        defaultRecipientShouldNotBeFound("systemCommission.notEquals=" + DEFAULT_SYSTEM_COMMISSION);

        // Get all the recipientList where systemCommission not equals to UPDATED_SYSTEM_COMMISSION
        defaultRecipientShouldBeFound("systemCommission.notEquals=" + UPDATED_SYSTEM_COMMISSION);
    }

    @Test
    @Transactional
    void getAllRecipientsBySystemCommissionIsInShouldWork() throws Exception {
        // Initialize the database
        recipientRepository.saveAndFlush(recipient);

        // Get all the recipientList where systemCommission in DEFAULT_SYSTEM_COMMISSION or UPDATED_SYSTEM_COMMISSION
        defaultRecipientShouldBeFound("systemCommission.in=" + DEFAULT_SYSTEM_COMMISSION + "," + UPDATED_SYSTEM_COMMISSION);

        // Get all the recipientList where systemCommission equals to UPDATED_SYSTEM_COMMISSION
        defaultRecipientShouldNotBeFound("systemCommission.in=" + UPDATED_SYSTEM_COMMISSION);
    }

    @Test
    @Transactional
    void getAllRecipientsBySystemCommissionIsNullOrNotNull() throws Exception {
        // Initialize the database
        recipientRepository.saveAndFlush(recipient);

        // Get all the recipientList where systemCommission is not null
        defaultRecipientShouldBeFound("systemCommission.specified=true");

        // Get all the recipientList where systemCommission is null
        defaultRecipientShouldNotBeFound("systemCommission.specified=false");
    }

    @Test
    @Transactional
    void getAllRecipientsBySystemCommissionIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        recipientRepository.saveAndFlush(recipient);

        // Get all the recipientList where systemCommission is greater than or equal to DEFAULT_SYSTEM_COMMISSION
        defaultRecipientShouldBeFound("systemCommission.greaterThanOrEqual=" + DEFAULT_SYSTEM_COMMISSION);

        // Get all the recipientList where systemCommission is greater than or equal to UPDATED_SYSTEM_COMMISSION
        defaultRecipientShouldNotBeFound("systemCommission.greaterThanOrEqual=" + UPDATED_SYSTEM_COMMISSION);
    }

    @Test
    @Transactional
    void getAllRecipientsBySystemCommissionIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        recipientRepository.saveAndFlush(recipient);

        // Get all the recipientList where systemCommission is less than or equal to DEFAULT_SYSTEM_COMMISSION
        defaultRecipientShouldBeFound("systemCommission.lessThanOrEqual=" + DEFAULT_SYSTEM_COMMISSION);

        // Get all the recipientList where systemCommission is less than or equal to SMALLER_SYSTEM_COMMISSION
        defaultRecipientShouldNotBeFound("systemCommission.lessThanOrEqual=" + SMALLER_SYSTEM_COMMISSION);
    }

    @Test
    @Transactional
    void getAllRecipientsBySystemCommissionIsLessThanSomething() throws Exception {
        // Initialize the database
        recipientRepository.saveAndFlush(recipient);

        // Get all the recipientList where systemCommission is less than DEFAULT_SYSTEM_COMMISSION
        defaultRecipientShouldNotBeFound("systemCommission.lessThan=" + DEFAULT_SYSTEM_COMMISSION);

        // Get all the recipientList where systemCommission is less than UPDATED_SYSTEM_COMMISSION
        defaultRecipientShouldBeFound("systemCommission.lessThan=" + UPDATED_SYSTEM_COMMISSION);
    }

    @Test
    @Transactional
    void getAllRecipientsBySystemCommissionIsGreaterThanSomething() throws Exception {
        // Initialize the database
        recipientRepository.saveAndFlush(recipient);

        // Get all the recipientList where systemCommission is greater than DEFAULT_SYSTEM_COMMISSION
        defaultRecipientShouldNotBeFound("systemCommission.greaterThan=" + DEFAULT_SYSTEM_COMMISSION);

        // Get all the recipientList where systemCommission is greater than SMALLER_SYSTEM_COMMISSION
        defaultRecipientShouldBeFound("systemCommission.greaterThan=" + SMALLER_SYSTEM_COMMISSION);
    }

    @Test
    @Transactional
    void getAllRecipientsBySystemFeeIsEqualToSomething() throws Exception {
        // Initialize the database
        recipientRepository.saveAndFlush(recipient);

        // Get all the recipientList where systemFee equals to DEFAULT_SYSTEM_FEE
        defaultRecipientShouldBeFound("systemFee.equals=" + DEFAULT_SYSTEM_FEE);

        // Get all the recipientList where systemFee equals to UPDATED_SYSTEM_FEE
        defaultRecipientShouldNotBeFound("systemFee.equals=" + UPDATED_SYSTEM_FEE);
    }

    @Test
    @Transactional
    void getAllRecipientsBySystemFeeIsNotEqualToSomething() throws Exception {
        // Initialize the database
        recipientRepository.saveAndFlush(recipient);

        // Get all the recipientList where systemFee not equals to DEFAULT_SYSTEM_FEE
        defaultRecipientShouldNotBeFound("systemFee.notEquals=" + DEFAULT_SYSTEM_FEE);

        // Get all the recipientList where systemFee not equals to UPDATED_SYSTEM_FEE
        defaultRecipientShouldBeFound("systemFee.notEquals=" + UPDATED_SYSTEM_FEE);
    }

    @Test
    @Transactional
    void getAllRecipientsBySystemFeeIsInShouldWork() throws Exception {
        // Initialize the database
        recipientRepository.saveAndFlush(recipient);

        // Get all the recipientList where systemFee in DEFAULT_SYSTEM_FEE or UPDATED_SYSTEM_FEE
        defaultRecipientShouldBeFound("systemFee.in=" + DEFAULT_SYSTEM_FEE + "," + UPDATED_SYSTEM_FEE);

        // Get all the recipientList where systemFee equals to UPDATED_SYSTEM_FEE
        defaultRecipientShouldNotBeFound("systemFee.in=" + UPDATED_SYSTEM_FEE);
    }

    @Test
    @Transactional
    void getAllRecipientsBySystemFeeIsNullOrNotNull() throws Exception {
        // Initialize the database
        recipientRepository.saveAndFlush(recipient);

        // Get all the recipientList where systemFee is not null
        defaultRecipientShouldBeFound("systemFee.specified=true");

        // Get all the recipientList where systemFee is null
        defaultRecipientShouldNotBeFound("systemFee.specified=false");
    }

    @Test
    @Transactional
    void getAllRecipientsBySystemFeeIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        recipientRepository.saveAndFlush(recipient);

        // Get all the recipientList where systemFee is greater than or equal to DEFAULT_SYSTEM_FEE
        defaultRecipientShouldBeFound("systemFee.greaterThanOrEqual=" + DEFAULT_SYSTEM_FEE);

        // Get all the recipientList where systemFee is greater than or equal to UPDATED_SYSTEM_FEE
        defaultRecipientShouldNotBeFound("systemFee.greaterThanOrEqual=" + UPDATED_SYSTEM_FEE);
    }

    @Test
    @Transactional
    void getAllRecipientsBySystemFeeIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        recipientRepository.saveAndFlush(recipient);

        // Get all the recipientList where systemFee is less than or equal to DEFAULT_SYSTEM_FEE
        defaultRecipientShouldBeFound("systemFee.lessThanOrEqual=" + DEFAULT_SYSTEM_FEE);

        // Get all the recipientList where systemFee is less than or equal to SMALLER_SYSTEM_FEE
        defaultRecipientShouldNotBeFound("systemFee.lessThanOrEqual=" + SMALLER_SYSTEM_FEE);
    }

    @Test
    @Transactional
    void getAllRecipientsBySystemFeeIsLessThanSomething() throws Exception {
        // Initialize the database
        recipientRepository.saveAndFlush(recipient);

        // Get all the recipientList where systemFee is less than DEFAULT_SYSTEM_FEE
        defaultRecipientShouldNotBeFound("systemFee.lessThan=" + DEFAULT_SYSTEM_FEE);

        // Get all the recipientList where systemFee is less than UPDATED_SYSTEM_FEE
        defaultRecipientShouldBeFound("systemFee.lessThan=" + UPDATED_SYSTEM_FEE);
    }

    @Test
    @Transactional
    void getAllRecipientsBySystemFeeIsGreaterThanSomething() throws Exception {
        // Initialize the database
        recipientRepository.saveAndFlush(recipient);

        // Get all the recipientList where systemFee is greater than DEFAULT_SYSTEM_FEE
        defaultRecipientShouldNotBeFound("systemFee.greaterThan=" + DEFAULT_SYSTEM_FEE);

        // Get all the recipientList where systemFee is greater than SMALLER_SYSTEM_FEE
        defaultRecipientShouldBeFound("systemFee.greaterThan=" + SMALLER_SYSTEM_FEE);
    }

    @Test
    @Transactional
    void getAllRecipientsBySenderCommissionIsEqualToSomething() throws Exception {
        // Initialize the database
        recipientRepository.saveAndFlush(recipient);

        // Get all the recipientList where senderCommission equals to DEFAULT_SENDER_COMMISSION
        defaultRecipientShouldBeFound("senderCommission.equals=" + DEFAULT_SENDER_COMMISSION);

        // Get all the recipientList where senderCommission equals to UPDATED_SENDER_COMMISSION
        defaultRecipientShouldNotBeFound("senderCommission.equals=" + UPDATED_SENDER_COMMISSION);
    }

    @Test
    @Transactional
    void getAllRecipientsBySenderCommissionIsNotEqualToSomething() throws Exception {
        // Initialize the database
        recipientRepository.saveAndFlush(recipient);

        // Get all the recipientList where senderCommission not equals to DEFAULT_SENDER_COMMISSION
        defaultRecipientShouldNotBeFound("senderCommission.notEquals=" + DEFAULT_SENDER_COMMISSION);

        // Get all the recipientList where senderCommission not equals to UPDATED_SENDER_COMMISSION
        defaultRecipientShouldBeFound("senderCommission.notEquals=" + UPDATED_SENDER_COMMISSION);
    }

    @Test
    @Transactional
    void getAllRecipientsBySenderCommissionIsInShouldWork() throws Exception {
        // Initialize the database
        recipientRepository.saveAndFlush(recipient);

        // Get all the recipientList where senderCommission in DEFAULT_SENDER_COMMISSION or UPDATED_SENDER_COMMISSION
        defaultRecipientShouldBeFound("senderCommission.in=" + DEFAULT_SENDER_COMMISSION + "," + UPDATED_SENDER_COMMISSION);

        // Get all the recipientList where senderCommission equals to UPDATED_SENDER_COMMISSION
        defaultRecipientShouldNotBeFound("senderCommission.in=" + UPDATED_SENDER_COMMISSION);
    }

    @Test
    @Transactional
    void getAllRecipientsBySenderCommissionIsNullOrNotNull() throws Exception {
        // Initialize the database
        recipientRepository.saveAndFlush(recipient);

        // Get all the recipientList where senderCommission is not null
        defaultRecipientShouldBeFound("senderCommission.specified=true");

        // Get all the recipientList where senderCommission is null
        defaultRecipientShouldNotBeFound("senderCommission.specified=false");
    }

    @Test
    @Transactional
    void getAllRecipientsBySenderCommissionIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        recipientRepository.saveAndFlush(recipient);

        // Get all the recipientList where senderCommission is greater than or equal to DEFAULT_SENDER_COMMISSION
        defaultRecipientShouldBeFound("senderCommission.greaterThanOrEqual=" + DEFAULT_SENDER_COMMISSION);

        // Get all the recipientList where senderCommission is greater than or equal to UPDATED_SENDER_COMMISSION
        defaultRecipientShouldNotBeFound("senderCommission.greaterThanOrEqual=" + UPDATED_SENDER_COMMISSION);
    }

    @Test
    @Transactional
    void getAllRecipientsBySenderCommissionIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        recipientRepository.saveAndFlush(recipient);

        // Get all the recipientList where senderCommission is less than or equal to DEFAULT_SENDER_COMMISSION
        defaultRecipientShouldBeFound("senderCommission.lessThanOrEqual=" + DEFAULT_SENDER_COMMISSION);

        // Get all the recipientList where senderCommission is less than or equal to SMALLER_SENDER_COMMISSION
        defaultRecipientShouldNotBeFound("senderCommission.lessThanOrEqual=" + SMALLER_SENDER_COMMISSION);
    }

    @Test
    @Transactional
    void getAllRecipientsBySenderCommissionIsLessThanSomething() throws Exception {
        // Initialize the database
        recipientRepository.saveAndFlush(recipient);

        // Get all the recipientList where senderCommission is less than DEFAULT_SENDER_COMMISSION
        defaultRecipientShouldNotBeFound("senderCommission.lessThan=" + DEFAULT_SENDER_COMMISSION);

        // Get all the recipientList where senderCommission is less than UPDATED_SENDER_COMMISSION
        defaultRecipientShouldBeFound("senderCommission.lessThan=" + UPDATED_SENDER_COMMISSION);
    }

    @Test
    @Transactional
    void getAllRecipientsBySenderCommissionIsGreaterThanSomething() throws Exception {
        // Initialize the database
        recipientRepository.saveAndFlush(recipient);

        // Get all the recipientList where senderCommission is greater than DEFAULT_SENDER_COMMISSION
        defaultRecipientShouldNotBeFound("senderCommission.greaterThan=" + DEFAULT_SENDER_COMMISSION);

        // Get all the recipientList where senderCommission is greater than SMALLER_SENDER_COMMISSION
        defaultRecipientShouldBeFound("senderCommission.greaterThan=" + SMALLER_SENDER_COMMISSION);
    }

    @Test
    @Transactional
    void getAllRecipientsByOrdIsEqualToSomething() throws Exception {
        // Initialize the database
        recipientRepository.saveAndFlush(recipient);

        // Get all the recipientList where ord equals to DEFAULT_ORD
        defaultRecipientShouldBeFound("ord.equals=" + DEFAULT_ORD);

        // Get all the recipientList where ord equals to UPDATED_ORD
        defaultRecipientShouldNotBeFound("ord.equals=" + UPDATED_ORD);
    }

    @Test
    @Transactional
    void getAllRecipientsByOrdIsNotEqualToSomething() throws Exception {
        // Initialize the database
        recipientRepository.saveAndFlush(recipient);

        // Get all the recipientList where ord not equals to DEFAULT_ORD
        defaultRecipientShouldNotBeFound("ord.notEquals=" + DEFAULT_ORD);

        // Get all the recipientList where ord not equals to UPDATED_ORD
        defaultRecipientShouldBeFound("ord.notEquals=" + UPDATED_ORD);
    }

    @Test
    @Transactional
    void getAllRecipientsByOrdIsInShouldWork() throws Exception {
        // Initialize the database
        recipientRepository.saveAndFlush(recipient);

        // Get all the recipientList where ord in DEFAULT_ORD or UPDATED_ORD
        defaultRecipientShouldBeFound("ord.in=" + DEFAULT_ORD + "," + UPDATED_ORD);

        // Get all the recipientList where ord equals to UPDATED_ORD
        defaultRecipientShouldNotBeFound("ord.in=" + UPDATED_ORD);
    }

    @Test
    @Transactional
    void getAllRecipientsByOrdIsNullOrNotNull() throws Exception {
        // Initialize the database
        recipientRepository.saveAndFlush(recipient);

        // Get all the recipientList where ord is not null
        defaultRecipientShouldBeFound("ord.specified=true");

        // Get all the recipientList where ord is null
        defaultRecipientShouldNotBeFound("ord.specified=false");
    }

    @Test
    @Transactional
    void getAllRecipientsByOrdIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        recipientRepository.saveAndFlush(recipient);

        // Get all the recipientList where ord is greater than or equal to DEFAULT_ORD
        defaultRecipientShouldBeFound("ord.greaterThanOrEqual=" + DEFAULT_ORD);

        // Get all the recipientList where ord is greater than or equal to UPDATED_ORD
        defaultRecipientShouldNotBeFound("ord.greaterThanOrEqual=" + UPDATED_ORD);
    }

    @Test
    @Transactional
    void getAllRecipientsByOrdIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        recipientRepository.saveAndFlush(recipient);

        // Get all the recipientList where ord is less than or equal to DEFAULT_ORD
        defaultRecipientShouldBeFound("ord.lessThanOrEqual=" + DEFAULT_ORD);

        // Get all the recipientList where ord is less than or equal to SMALLER_ORD
        defaultRecipientShouldNotBeFound("ord.lessThanOrEqual=" + SMALLER_ORD);
    }

    @Test
    @Transactional
    void getAllRecipientsByOrdIsLessThanSomething() throws Exception {
        // Initialize the database
        recipientRepository.saveAndFlush(recipient);

        // Get all the recipientList where ord is less than DEFAULT_ORD
        defaultRecipientShouldNotBeFound("ord.lessThan=" + DEFAULT_ORD);

        // Get all the recipientList where ord is less than UPDATED_ORD
        defaultRecipientShouldBeFound("ord.lessThan=" + UPDATED_ORD);
    }

    @Test
    @Transactional
    void getAllRecipientsByOrdIsGreaterThanSomething() throws Exception {
        // Initialize the database
        recipientRepository.saveAndFlush(recipient);

        // Get all the recipientList where ord is greater than DEFAULT_ORD
        defaultRecipientShouldNotBeFound("ord.greaterThan=" + DEFAULT_ORD);

        // Get all the recipientList where ord is greater than SMALLER_ORD
        defaultRecipientShouldBeFound("ord.greaterThan=" + SMALLER_ORD);
    }

    @Test
    @Transactional
    void getAllRecipientsByActiveIsEqualToSomething() throws Exception {
        // Initialize the database
        recipientRepository.saveAndFlush(recipient);

        // Get all the recipientList where active equals to DEFAULT_ACTIVE
        defaultRecipientShouldBeFound("active.equals=" + DEFAULT_ACTIVE);

        // Get all the recipientList where active equals to UPDATED_ACTIVE
        defaultRecipientShouldNotBeFound("active.equals=" + UPDATED_ACTIVE);
    }

    @Test
    @Transactional
    void getAllRecipientsByActiveIsNotEqualToSomething() throws Exception {
        // Initialize the database
        recipientRepository.saveAndFlush(recipient);

        // Get all the recipientList where active not equals to DEFAULT_ACTIVE
        defaultRecipientShouldNotBeFound("active.notEquals=" + DEFAULT_ACTIVE);

        // Get all the recipientList where active not equals to UPDATED_ACTIVE
        defaultRecipientShouldBeFound("active.notEquals=" + UPDATED_ACTIVE);
    }

    @Test
    @Transactional
    void getAllRecipientsByActiveIsInShouldWork() throws Exception {
        // Initialize the database
        recipientRepository.saveAndFlush(recipient);

        // Get all the recipientList where active in DEFAULT_ACTIVE or UPDATED_ACTIVE
        defaultRecipientShouldBeFound("active.in=" + DEFAULT_ACTIVE + "," + UPDATED_ACTIVE);

        // Get all the recipientList where active equals to UPDATED_ACTIVE
        defaultRecipientShouldNotBeFound("active.in=" + UPDATED_ACTIVE);
    }

    @Test
    @Transactional
    void getAllRecipientsByActiveIsNullOrNotNull() throws Exception {
        // Initialize the database
        recipientRepository.saveAndFlush(recipient);

        // Get all the recipientList where active is not null
        defaultRecipientShouldBeFound("active.specified=true");

        // Get all the recipientList where active is null
        defaultRecipientShouldNotBeFound("active.specified=false");
    }

    @Test
    @Transactional
    void getAllRecipientsByCheckIsEqualToSomething() throws Exception {
        // Initialize the database
        recipientRepository.saveAndFlush(recipient);

        // Get all the recipientList where check equals to DEFAULT_CHECK
        defaultRecipientShouldBeFound("check.equals=" + DEFAULT_CHECK);

        // Get all the recipientList where check equals to UPDATED_CHECK
        defaultRecipientShouldNotBeFound("check.equals=" + UPDATED_CHECK);
    }

    @Test
    @Transactional
    void getAllRecipientsByCheckIsNotEqualToSomething() throws Exception {
        // Initialize the database
        recipientRepository.saveAndFlush(recipient);

        // Get all the recipientList where check not equals to DEFAULT_CHECK
        defaultRecipientShouldNotBeFound("check.notEquals=" + DEFAULT_CHECK);

        // Get all the recipientList where check not equals to UPDATED_CHECK
        defaultRecipientShouldBeFound("check.notEquals=" + UPDATED_CHECK);
    }

    @Test
    @Transactional
    void getAllRecipientsByCheckIsInShouldWork() throws Exception {
        // Initialize the database
        recipientRepository.saveAndFlush(recipient);

        // Get all the recipientList where check in DEFAULT_CHECK or UPDATED_CHECK
        defaultRecipientShouldBeFound("check.in=" + DEFAULT_CHECK + "," + UPDATED_CHECK);

        // Get all the recipientList where check equals to UPDATED_CHECK
        defaultRecipientShouldNotBeFound("check.in=" + UPDATED_CHECK);
    }

    @Test
    @Transactional
    void getAllRecipientsByCheckIsNullOrNotNull() throws Exception {
        // Initialize the database
        recipientRepository.saveAndFlush(recipient);

        // Get all the recipientList where check is not null
        defaultRecipientShouldBeFound("check.specified=true");

        // Get all the recipientList where check is null
        defaultRecipientShouldNotBeFound("check.specified=false");
    }

    @Test
    @Transactional
    void getAllRecipientsByPayIsEqualToSomething() throws Exception {
        // Initialize the database
        recipientRepository.saveAndFlush(recipient);

        // Get all the recipientList where pay equals to DEFAULT_PAY
        defaultRecipientShouldBeFound("pay.equals=" + DEFAULT_PAY);

        // Get all the recipientList where pay equals to UPDATED_PAY
        defaultRecipientShouldNotBeFound("pay.equals=" + UPDATED_PAY);
    }

    @Test
    @Transactional
    void getAllRecipientsByPayIsNotEqualToSomething() throws Exception {
        // Initialize the database
        recipientRepository.saveAndFlush(recipient);

        // Get all the recipientList where pay not equals to DEFAULT_PAY
        defaultRecipientShouldNotBeFound("pay.notEquals=" + DEFAULT_PAY);

        // Get all the recipientList where pay not equals to UPDATED_PAY
        defaultRecipientShouldBeFound("pay.notEquals=" + UPDATED_PAY);
    }

    @Test
    @Transactional
    void getAllRecipientsByPayIsInShouldWork() throws Exception {
        // Initialize the database
        recipientRepository.saveAndFlush(recipient);

        // Get all the recipientList where pay in DEFAULT_PAY or UPDATED_PAY
        defaultRecipientShouldBeFound("pay.in=" + DEFAULT_PAY + "," + UPDATED_PAY);

        // Get all the recipientList where pay equals to UPDATED_PAY
        defaultRecipientShouldNotBeFound("pay.in=" + UPDATED_PAY);
    }

    @Test
    @Transactional
    void getAllRecipientsByPayIsNullOrNotNull() throws Exception {
        // Initialize the database
        recipientRepository.saveAndFlush(recipient);

        // Get all the recipientList where pay is not null
        defaultRecipientShouldBeFound("pay.specified=true");

        // Get all the recipientList where pay is null
        defaultRecipientShouldNotBeFound("pay.specified=false");
    }

    @Test
    @Transactional
    void getAllRecipientsByStatusIsEqualToSomething() throws Exception {
        // Initialize the database
        recipientRepository.saveAndFlush(recipient);

        // Get all the recipientList where status equals to DEFAULT_STATUS
        defaultRecipientShouldBeFound("status.equals=" + DEFAULT_STATUS);

        // Get all the recipientList where status equals to UPDATED_STATUS
        defaultRecipientShouldNotBeFound("status.equals=" + UPDATED_STATUS);
    }

    @Test
    @Transactional
    void getAllRecipientsByStatusIsNotEqualToSomething() throws Exception {
        // Initialize the database
        recipientRepository.saveAndFlush(recipient);

        // Get all the recipientList where status not equals to DEFAULT_STATUS
        defaultRecipientShouldNotBeFound("status.notEquals=" + DEFAULT_STATUS);

        // Get all the recipientList where status not equals to UPDATED_STATUS
        defaultRecipientShouldBeFound("status.notEquals=" + UPDATED_STATUS);
    }

    @Test
    @Transactional
    void getAllRecipientsByStatusIsInShouldWork() throws Exception {
        // Initialize the database
        recipientRepository.saveAndFlush(recipient);

        // Get all the recipientList where status in DEFAULT_STATUS or UPDATED_STATUS
        defaultRecipientShouldBeFound("status.in=" + DEFAULT_STATUS + "," + UPDATED_STATUS);

        // Get all the recipientList where status equals to UPDATED_STATUS
        defaultRecipientShouldNotBeFound("status.in=" + UPDATED_STATUS);
    }

    @Test
    @Transactional
    void getAllRecipientsByStatusIsNullOrNotNull() throws Exception {
        // Initialize the database
        recipientRepository.saveAndFlush(recipient);

        // Get all the recipientList where status is not null
        defaultRecipientShouldBeFound("status.specified=true");

        // Get all the recipientList where status is null
        defaultRecipientShouldNotBeFound("status.specified=false");
    }

    @Test
    @Transactional
    void getAllRecipientsByCancelIsEqualToSomething() throws Exception {
        // Initialize the database
        recipientRepository.saveAndFlush(recipient);

        // Get all the recipientList where cancel equals to DEFAULT_CANCEL
        defaultRecipientShouldBeFound("cancel.equals=" + DEFAULT_CANCEL);

        // Get all the recipientList where cancel equals to UPDATED_CANCEL
        defaultRecipientShouldNotBeFound("cancel.equals=" + UPDATED_CANCEL);
    }

    @Test
    @Transactional
    void getAllRecipientsByCancelIsNotEqualToSomething() throws Exception {
        // Initialize the database
        recipientRepository.saveAndFlush(recipient);

        // Get all the recipientList where cancel not equals to DEFAULT_CANCEL
        defaultRecipientShouldNotBeFound("cancel.notEquals=" + DEFAULT_CANCEL);

        // Get all the recipientList where cancel not equals to UPDATED_CANCEL
        defaultRecipientShouldBeFound("cancel.notEquals=" + UPDATED_CANCEL);
    }

    @Test
    @Transactional
    void getAllRecipientsByCancelIsInShouldWork() throws Exception {
        // Initialize the database
        recipientRepository.saveAndFlush(recipient);

        // Get all the recipientList where cancel in DEFAULT_CANCEL or UPDATED_CANCEL
        defaultRecipientShouldBeFound("cancel.in=" + DEFAULT_CANCEL + "," + UPDATED_CANCEL);

        // Get all the recipientList where cancel equals to UPDATED_CANCEL
        defaultRecipientShouldNotBeFound("cancel.in=" + UPDATED_CANCEL);
    }

    @Test
    @Transactional
    void getAllRecipientsByCancelIsNullOrNotNull() throws Exception {
        // Initialize the database
        recipientRepository.saveAndFlush(recipient);

        // Get all the recipientList where cancel is not null
        defaultRecipientShouldBeFound("cancel.specified=true");

        // Get all the recipientList where cancel is null
        defaultRecipientShouldNotBeFound("cancel.specified=false");
    }

    @Test
    @Transactional
    void getAllRecipientsByCanDepositIsEqualToSomething() throws Exception {
        // Initialize the database
        recipientRepository.saveAndFlush(recipient);

        // Get all the recipientList where canDeposit equals to DEFAULT_CAN_DEPOSIT
        defaultRecipientShouldBeFound("canDeposit.equals=" + DEFAULT_CAN_DEPOSIT);

        // Get all the recipientList where canDeposit equals to UPDATED_CAN_DEPOSIT
        defaultRecipientShouldNotBeFound("canDeposit.equals=" + UPDATED_CAN_DEPOSIT);
    }

    @Test
    @Transactional
    void getAllRecipientsByCanDepositIsNotEqualToSomething() throws Exception {
        // Initialize the database
        recipientRepository.saveAndFlush(recipient);

        // Get all the recipientList where canDeposit not equals to DEFAULT_CAN_DEPOSIT
        defaultRecipientShouldNotBeFound("canDeposit.notEquals=" + DEFAULT_CAN_DEPOSIT);

        // Get all the recipientList where canDeposit not equals to UPDATED_CAN_DEPOSIT
        defaultRecipientShouldBeFound("canDeposit.notEquals=" + UPDATED_CAN_DEPOSIT);
    }

    @Test
    @Transactional
    void getAllRecipientsByCanDepositIsInShouldWork() throws Exception {
        // Initialize the database
        recipientRepository.saveAndFlush(recipient);

        // Get all the recipientList where canDeposit in DEFAULT_CAN_DEPOSIT or UPDATED_CAN_DEPOSIT
        defaultRecipientShouldBeFound("canDeposit.in=" + DEFAULT_CAN_DEPOSIT + "," + UPDATED_CAN_DEPOSIT);

        // Get all the recipientList where canDeposit equals to UPDATED_CAN_DEPOSIT
        defaultRecipientShouldNotBeFound("canDeposit.in=" + UPDATED_CAN_DEPOSIT);
    }

    @Test
    @Transactional
    void getAllRecipientsByCanDepositIsNullOrNotNull() throws Exception {
        // Initialize the database
        recipientRepository.saveAndFlush(recipient);

        // Get all the recipientList where canDeposit is not null
        defaultRecipientShouldBeFound("canDeposit.specified=true");

        // Get all the recipientList where canDeposit is null
        defaultRecipientShouldNotBeFound("canDeposit.specified=false");
    }

//    @Test
    @Transactional
    void getAllRecipientsByEposPortIsEqualToSomething() throws Exception {
        // Initialize the database
        recipientRepository.saveAndFlush(recipient);

        // Get all the recipientList where eposPort equals to DEFAULT_EPOS_PORT
        defaultRecipientShouldBeFound("eposPort.equals=" + DEFAULT_EPOS_PORT);

        // Get all the recipientList where eposPort equals to UPDATED_EPOS_PORT
        defaultRecipientShouldNotBeFound("eposPort.equals=" + UPDATED_EPOS_PORT);
    }

//    @Test
    @Transactional
    void getAllRecipientsByEposPortIsNotEqualToSomething() throws Exception {
        // Initialize the database
        recipientRepository.saveAndFlush(recipient);

        // Get all the recipientList where eposPort not equals to DEFAULT_EPOS_PORT
        defaultRecipientShouldNotBeFound("eposPort.notEquals=" + DEFAULT_EPOS_PORT);

        // Get all the recipientList where eposPort not equals to UPDATED_EPOS_PORT
        defaultRecipientShouldBeFound("eposPort.notEquals=" + UPDATED_EPOS_PORT);
    }

//    @Test
    @Transactional
    void getAllRecipientsByEposPortIsInShouldWork() throws Exception {
        // Initialize the database
        recipientRepository.saveAndFlush(recipient);

        // Get all the recipientList where eposPort in DEFAULT_EPOS_PORT or UPDATED_EPOS_PORT
        defaultRecipientShouldBeFound("eposPort.in=" + DEFAULT_EPOS_PORT + "," + UPDATED_EPOS_PORT);

        // Get all the recipientList where eposPort equals to UPDATED_EPOS_PORT
        defaultRecipientShouldNotBeFound("eposPort.in=" + UPDATED_EPOS_PORT);
    }

//    @Test
    @Transactional
    void getAllRecipientsByEposPortIsNullOrNotNull() throws Exception {
        // Initialize the database
        recipientRepository.saveAndFlush(recipient);

        // Get all the recipientList where eposPort is not null
        defaultRecipientShouldBeFound("eposPort.specified=true");

        // Get all the recipientList where eposPort is null
        defaultRecipientShouldNotBeFound("eposPort.specified=false");
    }

//    @Test
    @Transactional
    void getAllRecipientsByEposPortIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        recipientRepository.saveAndFlush(recipient);

        // Get all the recipientList where eposPort is greater than or equal to DEFAULT_EPOS_PORT
        defaultRecipientShouldBeFound("eposPort.greaterThanOrEqual=" + DEFAULT_EPOS_PORT);

        // Get all the recipientList where eposPort is greater than or equal to (DEFAULT_EPOS_PORT + 1)
        defaultRecipientShouldNotBeFound("eposPort.greaterThanOrEqual=" + (DEFAULT_EPOS_PORT + 1));
    }

//    @Test
    @Transactional
    void getAllRecipientsByEposPortIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        recipientRepository.saveAndFlush(recipient);

        // Get all the recipientList where eposPort is less than or equal to DEFAULT_EPOS_PORT
        defaultRecipientShouldBeFound("eposPort.lessThanOrEqual=" + DEFAULT_EPOS_PORT);

        // Get all the recipientList where eposPort is less than or equal to SMALLER_EPOS_PORT
        defaultRecipientShouldNotBeFound("eposPort.lessThanOrEqual=" + SMALLER_EPOS_PORT);
    }

//    @Test
    @Transactional
    void getAllRecipientsByEposPortIsLessThanSomething() throws Exception {
        // Initialize the database
        recipientRepository.saveAndFlush(recipient);

        // Get all the recipientList where eposPort is less than DEFAULT_EPOS_PORT
        defaultRecipientShouldNotBeFound("eposPort.lessThan=" + DEFAULT_EPOS_PORT);

        // Get all the recipientList where eposPort is less than (DEFAULT_EPOS_PORT + 1)
        defaultRecipientShouldBeFound("eposPort.lessThan=" + (DEFAULT_EPOS_PORT + 1));
    }

//    @Test
    @Transactional
    void getAllRecipientsByEposPortIsGreaterThanSomething() throws Exception {
        // Initialize the database
        recipientRepository.saveAndFlush(recipient);

        // Get all the recipientList where eposPort is greater than DEFAULT_EPOS_PORT
        defaultRecipientShouldNotBeFound("eposPort.greaterThan=" + DEFAULT_EPOS_PORT);

        // Get all the recipientList where eposPort is greater than SMALLER_EPOS_PORT
        defaultRecipientShouldBeFound("eposPort.greaterThan=" + SMALLER_EPOS_PORT);
    }

//    @Test
    @Transactional
    void getAllRecipientsByEposMerchantIsEqualToSomething() throws Exception {
        // Initialize the database
        recipientRepository.saveAndFlush(recipient);

        // Get all the recipientList where eposMerchant equals to DEFAULT_EPOS_MERCHANT
        defaultRecipientShouldBeFound("eposMerchant.equals=" + DEFAULT_EPOS_MERCHANT);

        // Get all the recipientList where eposMerchant equals to UPDATED_EPOS_MERCHANT
        defaultRecipientShouldNotBeFound("eposMerchant.equals=" + UPDATED_EPOS_MERCHANT);
    }

//    @Test
    @Transactional
    void getAllRecipientsByEposMerchantIsNotEqualToSomething() throws Exception {
        // Initialize the database
        recipientRepository.saveAndFlush(recipient);

        // Get all the recipientList where eposMerchant not equals to DEFAULT_EPOS_MERCHANT
        defaultRecipientShouldNotBeFound("eposMerchant.notEquals=" + DEFAULT_EPOS_MERCHANT);

        // Get all the recipientList where eposMerchant not equals to UPDATED_EPOS_MERCHANT
        defaultRecipientShouldBeFound("eposMerchant.notEquals=" + UPDATED_EPOS_MERCHANT);
    }

//    @Test
    @Transactional
    void getAllRecipientsByEposMerchantIsInShouldWork() throws Exception {
        // Initialize the database
        recipientRepository.saveAndFlush(recipient);

        // Get all the recipientList where eposMerchant in DEFAULT_EPOS_MERCHANT or UPDATED_EPOS_MERCHANT
        defaultRecipientShouldBeFound("eposMerchant.in=" + DEFAULT_EPOS_MERCHANT + "," + UPDATED_EPOS_MERCHANT);

        // Get all the recipientList where eposMerchant equals to UPDATED_EPOS_MERCHANT
        defaultRecipientShouldNotBeFound("eposMerchant.in=" + UPDATED_EPOS_MERCHANT);
    }

//    @Test
    @Transactional
    void getAllRecipientsByEposMerchantIsNullOrNotNull() throws Exception {
        // Initialize the database
        recipientRepository.saveAndFlush(recipient);

        // Get all the recipientList where eposMerchant is not null
        defaultRecipientShouldBeFound("eposMerchant.specified=true");

        // Get all the recipientList where eposMerchant is null
        defaultRecipientShouldNotBeFound("eposMerchant.specified=false");
    }

//    @Test
    @Transactional
    void getAllRecipientsByEposMerchantIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        recipientRepository.saveAndFlush(recipient);

        // Get all the recipientList where eposMerchant is greater than or equal to DEFAULT_EPOS_MERCHANT
        defaultRecipientShouldBeFound("eposMerchant.greaterThanOrEqual=" + DEFAULT_EPOS_MERCHANT);

        // Get all the recipientList where eposMerchant is greater than or equal to UPDATED_EPOS_MERCHANT
        defaultRecipientShouldNotBeFound("eposMerchant.greaterThanOrEqual=" + UPDATED_EPOS_MERCHANT);
    }

//    @Test
    @Transactional
    void getAllRecipientsByEposMerchantIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        recipientRepository.saveAndFlush(recipient);

        // Get all the recipientList where eposMerchant is less than or equal to DEFAULT_EPOS_MERCHANT
        defaultRecipientShouldBeFound("eposMerchant.lessThanOrEqual=" + DEFAULT_EPOS_MERCHANT);

        // Get all the recipientList where eposMerchant is less than or equal to SMALLER_EPOS_MERCHANT
        defaultRecipientShouldNotBeFound("eposMerchant.lessThanOrEqual=" + SMALLER_EPOS_MERCHANT);
    }

//    @Test
    @Transactional
    void getAllRecipientsByEposMerchantIsLessThanSomething() throws Exception {
        // Initialize the database
        recipientRepository.saveAndFlush(recipient);

        // Get all the recipientList where eposMerchant is less than DEFAULT_EPOS_MERCHANT
        defaultRecipientShouldNotBeFound("eposMerchant.lessThan=" + DEFAULT_EPOS_MERCHANT);

        // Get all the recipientList where eposMerchant is less than UPDATED_EPOS_MERCHANT
        defaultRecipientShouldBeFound("eposMerchant.lessThan=" + UPDATED_EPOS_MERCHANT);
    }

//    @Test
    @Transactional
    void getAllRecipientsByEposMerchantIsGreaterThanSomething() throws Exception {
        // Initialize the database
        recipientRepository.saveAndFlush(recipient);

        // Get all the recipientList where eposMerchant is greater than DEFAULT_EPOS_MERCHANT
        defaultRecipientShouldNotBeFound("eposMerchant.greaterThan=" + DEFAULT_EPOS_MERCHANT);

        // Get all the recipientList where eposMerchant is greater than SMALLER_EPOS_MERCHANT
        defaultRecipientShouldBeFound("eposMerchant.greaterThan=" + SMALLER_EPOS_MERCHANT);
    }

//    @Test
    @Transactional
    void getAllRecipientsByEposTerminalIsEqualToSomething() throws Exception {
        // Initialize the database
        recipientRepository.saveAndFlush(recipient);

        // Get all the recipientList where eposTerminal equals to DEFAULT_EPOS_TERMINAL
        defaultRecipientShouldBeFound("eposTerminal.equals=" + DEFAULT_EPOS_TERMINAL);

        // Get all the recipientList where eposTerminal equals to UPDATED_EPOS_TERMINAL
        defaultRecipientShouldNotBeFound("eposTerminal.equals=" + UPDATED_EPOS_TERMINAL);
    }

//    @Test
    @Transactional
    void getAllRecipientsByEposTerminalIsNotEqualToSomething() throws Exception {
        // Initialize the database
        recipientRepository.saveAndFlush(recipient);

        // Get all the recipientList where eposTerminal not equals to DEFAULT_EPOS_TERMINAL
        defaultRecipientShouldNotBeFound("eposTerminal.notEquals=" + DEFAULT_EPOS_TERMINAL);

        // Get all the recipientList where eposTerminal not equals to UPDATED_EPOS_TERMINAL
        defaultRecipientShouldBeFound("eposTerminal.notEquals=" + UPDATED_EPOS_TERMINAL);
    }

//    @Test
    @Transactional
    void getAllRecipientsByEposTerminalIsInShouldWork() throws Exception {
        // Initialize the database
        recipientRepository.saveAndFlush(recipient);

        // Get all the recipientList where eposTerminal in DEFAULT_EPOS_TERMINAL or UPDATED_EPOS_TERMINAL
        defaultRecipientShouldBeFound("eposTerminal.in=" + DEFAULT_EPOS_TERMINAL + "," + UPDATED_EPOS_TERMINAL);

        // Get all the recipientList where eposTerminal equals to UPDATED_EPOS_TERMINAL
        defaultRecipientShouldNotBeFound("eposTerminal.in=" + UPDATED_EPOS_TERMINAL);
    }

//    @Test
    @Transactional
    void getAllRecipientsByEposTerminalIsNullOrNotNull() throws Exception {
        // Initialize the database
        recipientRepository.saveAndFlush(recipient);

        // Get all the recipientList where eposTerminal is not null
        defaultRecipientShouldBeFound("eposTerminal.specified=true");

        // Get all the recipientList where eposTerminal is null
        defaultRecipientShouldNotBeFound("eposTerminal.specified=false");
    }

//    @Test
    @Transactional
    void getAllRecipientsByEposTerminalIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        recipientRepository.saveAndFlush(recipient);

        // Get all the recipientList where eposTerminal is greater than or equal to DEFAULT_EPOS_TERMINAL
        defaultRecipientShouldBeFound("eposTerminal.greaterThanOrEqual=" + DEFAULT_EPOS_TERMINAL);

        // Get all the recipientList where eposTerminal is greater than or equal to UPDATED_EPOS_TERMINAL
        defaultRecipientShouldNotBeFound("eposTerminal.greaterThanOrEqual=" + UPDATED_EPOS_TERMINAL);
    }

//    @Test
    @Transactional
    void getAllRecipientsByEposTerminalIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        recipientRepository.saveAndFlush(recipient);

        // Get all the recipientList where eposTerminal is less than or equal to DEFAULT_EPOS_TERMINAL
        defaultRecipientShouldBeFound("eposTerminal.lessThanOrEqual=" + DEFAULT_EPOS_TERMINAL);

        // Get all the recipientList where eposTerminal is less than or equal to SMALLER_EPOS_TERMINAL
        defaultRecipientShouldNotBeFound("eposTerminal.lessThanOrEqual=" + SMALLER_EPOS_TERMINAL);
    }

//    @Test
    @Transactional
    void getAllRecipientsByEposTerminalIsLessThanSomething() throws Exception {
        // Initialize the database
        recipientRepository.saveAndFlush(recipient);

        // Get all the recipientList where eposTerminal is less than DEFAULT_EPOS_TERMINAL
        defaultRecipientShouldNotBeFound("eposTerminal.lessThan=" + DEFAULT_EPOS_TERMINAL);

        // Get all the recipientList where eposTerminal is less than UPDATED_EPOS_TERMINAL
        defaultRecipientShouldBeFound("eposTerminal.lessThan=" + UPDATED_EPOS_TERMINAL);
    }

//    @Test
    @Transactional
    void getAllRecipientsByEposTerminalIsGreaterThanSomething() throws Exception {
        // Initialize the database
        recipientRepository.saveAndFlush(recipient);

        // Get all the recipientList where eposTerminal is greater than DEFAULT_EPOS_TERMINAL
        defaultRecipientShouldNotBeFound("eposTerminal.greaterThan=" + DEFAULT_EPOS_TERMINAL);

        // Get all the recipientList where eposTerminal is greater than SMALLER_EPOS_TERMINAL
        defaultRecipientShouldBeFound("eposTerminal.greaterThan=" + SMALLER_EPOS_TERMINAL);
    }

    @Test
    @Transactional
    void getAllRecipientsBySettlementAccountIsEqualToSomething() throws Exception {
        // Initialize the database
        recipientRepository.saveAndFlush(recipient);

        // Get all the recipientList where settlementAccount equals to DEFAULT_SETTLEMENT_ACCOUNT
        defaultRecipientShouldBeFound("settlementAccount.equals=" + DEFAULT_SETTLEMENT_ACCOUNT);

        // Get all the recipientList where settlementAccount equals to UPDATED_SETTLEMENT_ACCOUNT
        defaultRecipientShouldNotBeFound("settlementAccount.equals=" + UPDATED_SETTLEMENT_ACCOUNT);
    }

    @Test
    @Transactional
    void getAllRecipientsBySettlementAccountIsNotEqualToSomething() throws Exception {
        // Initialize the database
        recipientRepository.saveAndFlush(recipient);

        // Get all the recipientList where settlementAccount not equals to DEFAULT_SETTLEMENT_ACCOUNT
        defaultRecipientShouldNotBeFound("settlementAccount.notEquals=" + DEFAULT_SETTLEMENT_ACCOUNT);

        // Get all the recipientList where settlementAccount not equals to UPDATED_SETTLEMENT_ACCOUNT
        defaultRecipientShouldBeFound("settlementAccount.notEquals=" + UPDATED_SETTLEMENT_ACCOUNT);
    }

    @Test
    @Transactional
    void getAllRecipientsBySettlementAccountIsInShouldWork() throws Exception {
        // Initialize the database
        recipientRepository.saveAndFlush(recipient);

        // Get all the recipientList where settlementAccount in DEFAULT_SETTLEMENT_ACCOUNT or UPDATED_SETTLEMENT_ACCOUNT
        defaultRecipientShouldBeFound("settlementAccount.in=" + DEFAULT_SETTLEMENT_ACCOUNT + "," + UPDATED_SETTLEMENT_ACCOUNT);

        // Get all the recipientList where settlementAccount equals to UPDATED_SETTLEMENT_ACCOUNT
        defaultRecipientShouldNotBeFound("settlementAccount.in=" + UPDATED_SETTLEMENT_ACCOUNT);
    }

    @Test
    @Transactional
    void getAllRecipientsBySettlementAccountIsNullOrNotNull() throws Exception {
        // Initialize the database
        recipientRepository.saveAndFlush(recipient);

        // Get all the recipientList where settlementAccount is not null
        defaultRecipientShouldBeFound("settlementAccount.specified=true");

        // Get all the recipientList where settlementAccount is null
        defaultRecipientShouldNotBeFound("settlementAccount.specified=false");
    }

    @Test
    @Transactional
    void getAllRecipientsBySettlementAccountContainsSomething() throws Exception {
        // Initialize the database
        recipientRepository.saveAndFlush(recipient);

        // Get all the recipientList where settlementAccount contains DEFAULT_SETTLEMENT_ACCOUNT
        defaultRecipientShouldBeFound("settlementAccount.contains=" + DEFAULT_SETTLEMENT_ACCOUNT);

        // Get all the recipientList where settlementAccount contains UPDATED_SETTLEMENT_ACCOUNT
        defaultRecipientShouldNotBeFound("settlementAccount.contains=" + UPDATED_SETTLEMENT_ACCOUNT);
    }

    @Test
    @Transactional
    void getAllRecipientsBySettlementAccountNotContainsSomething() throws Exception {
        // Initialize the database
        recipientRepository.saveAndFlush(recipient);

        // Get all the recipientList where settlementAccount does not contain DEFAULT_SETTLEMENT_ACCOUNT
        defaultRecipientShouldNotBeFound("settlementAccount.doesNotContain=" + DEFAULT_SETTLEMENT_ACCOUNT);

        // Get all the recipientList where settlementAccount does not contain UPDATED_SETTLEMENT_ACCOUNT
        defaultRecipientShouldBeFound("settlementAccount.doesNotContain=" + UPDATED_SETTLEMENT_ACCOUNT);
    }

    @Test
    @Transactional
    void getAllRecipientsByMfoIsEqualToSomething() throws Exception {
        // Initialize the database
        recipientRepository.saveAndFlush(recipient);

        // Get all the recipientList where mfo equals to DEFAULT_MFO
        defaultRecipientShouldBeFound("mfo.equals=" + DEFAULT_MFO);

        // Get all the recipientList where mfo equals to UPDATED_MFO
        defaultRecipientShouldNotBeFound("mfo.equals=" + UPDATED_MFO);
    }

    @Test
    @Transactional
    void getAllRecipientsByMfoIsNotEqualToSomething() throws Exception {
        // Initialize the database
        recipientRepository.saveAndFlush(recipient);

        // Get all the recipientList where mfo not equals to DEFAULT_MFO
        defaultRecipientShouldNotBeFound("mfo.notEquals=" + DEFAULT_MFO);

        // Get all the recipientList where mfo not equals to UPDATED_MFO
        defaultRecipientShouldBeFound("mfo.notEquals=" + UPDATED_MFO);
    }

    @Test
    @Transactional
    void getAllRecipientsByMfoIsInShouldWork() throws Exception {
        // Initialize the database
        recipientRepository.saveAndFlush(recipient);

        // Get all the recipientList where mfo in DEFAULT_MFO or UPDATED_MFO
        defaultRecipientShouldBeFound("mfo.in=" + DEFAULT_MFO + "," + UPDATED_MFO);

        // Get all the recipientList where mfo equals to UPDATED_MFO
        defaultRecipientShouldNotBeFound("mfo.in=" + UPDATED_MFO);
    }

    @Test
    @Transactional
    void getAllRecipientsByMfoIsNullOrNotNull() throws Exception {
        // Initialize the database
        recipientRepository.saveAndFlush(recipient);

        // Get all the recipientList where mfo is not null
        defaultRecipientShouldBeFound("mfo.specified=true");

        // Get all the recipientList where mfo is null
        defaultRecipientShouldNotBeFound("mfo.specified=false");
    }

    @Test
    @Transactional
    void getAllRecipientsByMfoContainsSomething() throws Exception {
        // Initialize the database
        recipientRepository.saveAndFlush(recipient);

        // Get all the recipientList where mfo contains DEFAULT_MFO
        defaultRecipientShouldBeFound("mfo.contains=" + DEFAULT_MFO);

        // Get all the recipientList where mfo contains UPDATED_MFO
        defaultRecipientShouldNotBeFound("mfo.contains=" + UPDATED_MFO);
    }

    @Test
    @Transactional
    void getAllRecipientsByMfoNotContainsSomething() throws Exception {
        // Initialize the database
        recipientRepository.saveAndFlush(recipient);

        // Get all the recipientList where mfo does not contain DEFAULT_MFO
        defaultRecipientShouldNotBeFound("mfo.doesNotContain=" + DEFAULT_MFO);

        // Get all the recipientList where mfo does not contain UPDATED_MFO
        defaultRecipientShouldBeFound("mfo.doesNotContain=" + UPDATED_MFO);
    }

    @Test
    @Transactional
    void getAllRecipientsByInnIsEqualToSomething() throws Exception {
        // Initialize the database
        recipientRepository.saveAndFlush(recipient);

        // Get all the recipientList where inn equals to DEFAULT_INN
        defaultRecipientShouldBeFound("inn.equals=" + DEFAULT_INN);

        // Get all the recipientList where inn equals to UPDATED_INN
        defaultRecipientShouldNotBeFound("inn.equals=" + UPDATED_INN);
    }

    @Test
    @Transactional
    void getAllRecipientsByInnIsNotEqualToSomething() throws Exception {
        // Initialize the database
        recipientRepository.saveAndFlush(recipient);

        // Get all the recipientList where inn not equals to DEFAULT_INN
        defaultRecipientShouldNotBeFound("inn.notEquals=" + DEFAULT_INN);

        // Get all the recipientList where inn not equals to UPDATED_INN
        defaultRecipientShouldBeFound("inn.notEquals=" + UPDATED_INN);
    }

    @Test
    @Transactional
    void getAllRecipientsByInnIsInShouldWork() throws Exception {
        // Initialize the database
        recipientRepository.saveAndFlush(recipient);

        // Get all the recipientList where inn in DEFAULT_INN or UPDATED_INN
        defaultRecipientShouldBeFound("inn.in=" + DEFAULT_INN + "," + UPDATED_INN);

        // Get all the recipientList where inn equals to UPDATED_INN
        defaultRecipientShouldNotBeFound("inn.in=" + UPDATED_INN);
    }

    @Test
    @Transactional
    void getAllRecipientsByInnIsNullOrNotNull() throws Exception {
        // Initialize the database
        recipientRepository.saveAndFlush(recipient);

        // Get all the recipientList where inn is not null
        defaultRecipientShouldBeFound("inn.specified=true");

        // Get all the recipientList where inn is null
        defaultRecipientShouldNotBeFound("inn.specified=false");
    }

    @Test
    @Transactional
    void getAllRecipientsByInnContainsSomething() throws Exception {
        // Initialize the database
        recipientRepository.saveAndFlush(recipient);

        // Get all the recipientList where inn contains DEFAULT_INN
        defaultRecipientShouldBeFound("inn.contains=" + DEFAULT_INN);

        // Get all the recipientList where inn contains UPDATED_INN
        defaultRecipientShouldNotBeFound("inn.contains=" + UPDATED_INN);
    }

    @Test
    @Transactional
    void getAllRecipientsByInnNotContainsSomething() throws Exception {
        // Initialize the database
        recipientRepository.saveAndFlush(recipient);

        // Get all the recipientList where inn does not contain DEFAULT_INN
        defaultRecipientShouldNotBeFound("inn.doesNotContain=" + DEFAULT_INN);

        // Get all the recipientList where inn does not contain UPDATED_INN
        defaultRecipientShouldBeFound("inn.doesNotContain=" + UPDATED_INN);
    }

    @Test
    @Transactional
    void getAllRecipientsByReconcileIsEqualToSomething() throws Exception {
        // Initialize the database
        recipientRepository.saveAndFlush(recipient);

        // Get all the recipientList where reconcile equals to DEFAULT_RECONCILE
        defaultRecipientShouldBeFound("reconcile.equals=" + DEFAULT_RECONCILE);

        // Get all the recipientList where reconcile equals to UPDATED_RECONCILE
        defaultRecipientShouldNotBeFound("reconcile.equals=" + UPDATED_RECONCILE);
    }

    @Test
    @Transactional
    void getAllRecipientsByReconcileIsNotEqualToSomething() throws Exception {
        // Initialize the database
        recipientRepository.saveAndFlush(recipient);

        // Get all the recipientList where reconcile not equals to DEFAULT_RECONCILE
        defaultRecipientShouldNotBeFound("reconcile.notEquals=" + DEFAULT_RECONCILE);

        // Get all the recipientList where reconcile not equals to UPDATED_RECONCILE
        defaultRecipientShouldBeFound("reconcile.notEquals=" + UPDATED_RECONCILE);
    }

    @Test
    @Transactional
    void getAllRecipientsByReconcileIsInShouldWork() throws Exception {
        // Initialize the database
        recipientRepository.saveAndFlush(recipient);

        // Get all the recipientList where reconcile in DEFAULT_RECONCILE or UPDATED_RECONCILE
        defaultRecipientShouldBeFound("reconcile.in=" + DEFAULT_RECONCILE + "," + UPDATED_RECONCILE);

        // Get all the recipientList where reconcile equals to UPDATED_RECONCILE
        defaultRecipientShouldNotBeFound("reconcile.in=" + UPDATED_RECONCILE);
    }

    @Test
    @Transactional
    void getAllRecipientsByReconcileIsNullOrNotNull() throws Exception {
        // Initialize the database
        recipientRepository.saveAndFlush(recipient);

        // Get all the recipientList where reconcile is not null
        defaultRecipientShouldBeFound("reconcile.specified=true");

        // Get all the recipientList where reconcile is null
        defaultRecipientShouldNotBeFound("reconcile.specified=false");
    }

    @Test
    @Transactional
    void getAllRecipientsByGatewayIsEqualToSomething() throws Exception {
        // Initialize the database
        recipientRepository.saveAndFlush(recipient);
        Gateway gateway = GatewayResourceIT.createEntity(em);
        em.persist(gateway);
        em.flush();
        recipient.setGateway(gateway);
        recipientRepository.saveAndFlush(recipient);
        Long gatewayId = gateway.getId();

        // Get all the recipientList where gateway equals to gatewayId
        defaultRecipientShouldBeFound("gatewayId.equals=" + gatewayId);

        // Get all the recipientList where gateway equals to (gatewayId + 1)
        defaultRecipientShouldNotBeFound("gatewayId.equals=" + (gatewayId + 1));
    }

    @Test
    @Transactional
    void getAllRecipientsByCategoryIsEqualToSomething() throws Exception {
        // Initialize the database
        recipientRepository.saveAndFlush(recipient);
        Category category = CategoryResourceIT.createEntity(em);
        em.persist(category);
        em.flush();
        recipient.setCategory(category);
        recipientRepository.saveAndFlush(recipient);
        Long categoryId = category.getId();

        // Get all the recipientList where category equals to categoryId
        defaultRecipientShouldBeFound("categoryId.equals=" + categoryId);

        // Get all the recipientList where category equals to (categoryId + 1)
        defaultRecipientShouldNotBeFound("categoryId.equals=" + (categoryId + 1));
    }

    @Test
    @Transactional
    void getAllRecipientsByTitleIsEqualToSomething() throws Exception {
        // Initialize the database
        recipientRepository.saveAndFlush(recipient);
        I18n title = I18nResourceIT.createEntity(em);
        em.persist(title);
        em.flush();
        recipient.addTitle(title);
        recipientRepository.saveAndFlush(recipient);
        Long titleId = title.getId();

        // Get all the recipientList where title equals to titleId
        defaultRecipientShouldBeFound("titleId.equals=" + titleId);

        // Get all the recipientList where title equals to (titleId + 1)
        defaultRecipientShouldNotBeFound("titleId.equals=" + (titleId + 1));
    }

    @Test
    @Transactional
    void getAllRecipientsByFullNameIsEqualToSomething() throws Exception {
        // Initialize the database
        recipientRepository.saveAndFlush(recipient);
        I18n fullName = I18nResourceIT.createEntity(em);
        em.persist(fullName);
        em.flush();
        recipient.addFullName(fullName);
        recipientRepository.saveAndFlush(recipient);
        Long fullNameId = fullName.getId();

        // Get all the recipientList where fullName equals to fullNameId
        defaultRecipientShouldBeFound("fullNameId.equals=" + fullNameId);

        // Get all the recipientList where fullName equals to (fullNameId + 1)
        defaultRecipientShouldNotBeFound("fullNameId.equals=" + (fullNameId + 1));
    }

    /**
     * Executes the search, and checks that the default entity is returned.
     */
    private void defaultRecipientShouldBeFound(String filter) throws Exception {
        restRecipientMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
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
//            .andExpect(jsonPath("$.[*].eposPort").value(hasItem(DEFAULT_EPOS_PORT)))
//            .andExpect(jsonPath("$.[*].eposMerchant").value(hasItem(DEFAULT_EPOS_MERCHANT.intValue())))
//            .andExpect(jsonPath("$.[*].eposTerminal").value(hasItem(DEFAULT_EPOS_TERMINAL.intValue())))
            .andExpect(jsonPath("$.[*].settlementAccount").value(hasItem(DEFAULT_SETTLEMENT_ACCOUNT)))
            .andExpect(jsonPath("$.[*].mfo").value(hasItem(DEFAULT_MFO)))
            .andExpect(jsonPath("$.[*].inn").value(hasItem(DEFAULT_INN)))
            .andExpect(jsonPath("$.[*].reconcile").value(hasItem(DEFAULT_RECONCILE.booleanValue())));

        // Check, that the count call also returns 1
        restRecipientMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("1"));
    }

    /**
     * Executes the search, and checks that the default entity is not returned.
     */
    private void defaultRecipientShouldNotBeFound(String filter) throws Exception {
        restRecipientMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty());

        // Check, that the count call also returns 0
        restRecipientMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("0"));
    }

    @Test
    @Transactional
    void getNonExistingRecipient() throws Exception {
        // Get the recipient
        restRecipientMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putNewRecipient() throws Exception {
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
            .reconcile(UPDATED_RECONCILE);
        RecipientDTO recipientDTO = recipientMapper.toDto(updatedRecipient);

        restRecipientMockMvc
            .perform(
                put(ENTITY_API_URL_ID, recipientDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(recipientDTO))
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
        assertThat(testRecipient.getActive()).isEqualTo(UPDATED_ACTIVE);
        assertThat(testRecipient.getCheck()).isEqualTo(UPDATED_CHECK);
        assertThat(testRecipient.getPay()).isEqualTo(UPDATED_PAY);
        assertThat(testRecipient.getStatus()).isEqualTo(UPDATED_STATUS);
        assertThat(testRecipient.getCancel()).isEqualTo(UPDATED_CANCEL);
        assertThat(testRecipient.getCanDeposit()).isEqualTo(UPDATED_CAN_DEPOSIT);
//        assertThat(testRecipient.getEposPort()).isEqualTo(UPDATED_EPOS_PORT);
//        assertThat(testRecipient.getEposMerchant()).isEqualTo(UPDATED_EPOS_MERCHANT);
//        assertThat(testRecipient.getEposTerminal()).isEqualTo(UPDATED_EPOS_TERMINAL);
        assertThat(testRecipient.getSettlementAccount()).isEqualTo(UPDATED_SETTLEMENT_ACCOUNT);
        assertThat(testRecipient.getMfo()).isEqualTo(UPDATED_MFO);
        assertThat(testRecipient.getInn()).isEqualTo(UPDATED_INN);
        assertThat(testRecipient.getReconcile()).isEqualTo(UPDATED_RECONCILE);
    }

    @Test
    @Transactional
    void putNonExistingRecipient() throws Exception {
        int databaseSizeBeforeUpdate = recipientRepository.findAll().size();
        recipient.setId(count.incrementAndGet());

        // Create the Recipient
        RecipientDTO recipientDTO = recipientMapper.toDto(recipient);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restRecipientMockMvc
            .perform(
                put(ENTITY_API_URL_ID, recipientDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(recipientDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Recipient in the database
        List<Recipient> recipientList = recipientRepository.findAll();
        assertThat(recipientList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchRecipient() throws Exception {
        int databaseSizeBeforeUpdate = recipientRepository.findAll().size();
        recipient.setId(count.incrementAndGet());

        // Create the Recipient
        RecipientDTO recipientDTO = recipientMapper.toDto(recipient);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restRecipientMockMvc
            .perform(
                put(ENTITY_API_URL_ID, count.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(recipientDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Recipient in the database
        List<Recipient> recipientList = recipientRepository.findAll();
        assertThat(recipientList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamRecipient() throws Exception {
        int databaseSizeBeforeUpdate = recipientRepository.findAll().size();
        recipient.setId(count.incrementAndGet());

        // Create the Recipient
        RecipientDTO recipientDTO = recipientMapper.toDto(recipient);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restRecipientMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(recipientDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Recipient in the database
        List<Recipient> recipientList = recipientRepository.findAll();
        assertThat(recipientList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateRecipientWithPatch() throws Exception {
        // Initialize the database
        recipientRepository.saveAndFlush(recipient);

        int databaseSizeBeforeUpdate = recipientRepository.findAll().size();

        // Update the recipient using partial update
        Recipient partialUpdatedRecipient = new Recipient();
        partialUpdatedRecipient.setId(recipient.getId());

        partialUpdatedRecipient
            .minAmount(UPDATED_MIN_AMOUNT)
            .fee(UPDATED_FEE)
            .systemCommission(UPDATED_SYSTEM_COMMISSION)
            .systemFee(UPDATED_SYSTEM_FEE)
            .pay(UPDATED_PAY)
            .canDeposit(UPDATED_CAN_DEPOSIT)
//            .eposMerchant(UPDATED_EPOS_MERCHANT)
//            .eposTerminal(UPDATED_EPOS_TERMINAL)
            .settlementAccount(UPDATED_SETTLEMENT_ACCOUNT)
            .mfo(UPDATED_MFO)
            .inn(UPDATED_INN);

        restRecipientMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedRecipient.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedRecipient))
            )
            .andExpect(status().isOk());

        // Validate the Recipient in the database
        List<Recipient> recipientList = recipientRepository.findAll();
        assertThat(recipientList).hasSize(databaseSizeBeforeUpdate);
        Recipient testRecipient = recipientList.get(recipientList.size() - 1);
        assertThat(testRecipient.getShortName()).isEqualTo(DEFAULT_SHORT_NAME);
        assertThat(testRecipient.getServiceId()).isEqualTo(DEFAULT_SERVICE_ID);
        assertThat(testRecipient.getThumbnail()).isEqualTo(DEFAULT_THUMBNAIL);
        assertThat(testRecipient.getLogo()).isEqualTo(DEFAULT_LOGO);
        assertThat(testRecipient.getAmountRatio()).isEqualTo(DEFAULT_AMOUNT_RATIO);
        assertThat(testRecipient.getCcy()).isEqualTo(DEFAULT_CCY);
        assertThat(testRecipient.getMinAmount()).isEqualTo(UPDATED_MIN_AMOUNT);
        assertThat(testRecipient.getMaxAmount()).isEqualTo(DEFAULT_MAX_AMOUNT);
        assertThat(testRecipient.getLimitRatio()).isEqualTo(DEFAULT_LIMIT_RATIO);
        assertThat(testRecipient.getCommission()).isEqualTo(DEFAULT_COMMISSION);
        assertThat(testRecipient.getFee()).isEqualTo(UPDATED_FEE);
        assertThat(testRecipient.getSystemCommission()).isEqualTo(UPDATED_SYSTEM_COMMISSION);
        assertThat(testRecipient.getSystemFee()).isEqualTo(UPDATED_SYSTEM_FEE);
        assertThat(testRecipient.getSenderCommission()).isEqualTo(DEFAULT_SENDER_COMMISSION);
        assertThat(testRecipient.getOrd()).isEqualTo(DEFAULT_ORD);
        assertThat(testRecipient.getActive()).isEqualTo(DEFAULT_ACTIVE);
        assertThat(testRecipient.getCheck()).isEqualTo(DEFAULT_CHECK);
        assertThat(testRecipient.getPay()).isEqualTo(UPDATED_PAY);
        assertThat(testRecipient.getStatus()).isEqualTo(DEFAULT_STATUS);
        assertThat(testRecipient.getCancel()).isEqualTo(DEFAULT_CANCEL);
        assertThat(testRecipient.getCanDeposit()).isEqualTo(UPDATED_CAN_DEPOSIT);
//        assertThat(testRecipient.getEposPort()).isEqualTo(DEFAULT_EPOS_PORT);
//        assertThat(testRecipient.getEposMerchant()).isEqualTo(UPDATED_EPOS_MERCHANT);
//        assertThat(testRecipient.getEposTerminal()).isEqualTo(UPDATED_EPOS_TERMINAL);
        assertThat(testRecipient.getSettlementAccount()).isEqualTo(UPDATED_SETTLEMENT_ACCOUNT);
        assertThat(testRecipient.getMfo()).isEqualTo(UPDATED_MFO);
        assertThat(testRecipient.getInn()).isEqualTo(UPDATED_INN);
        assertThat(testRecipient.getReconcile()).isEqualTo(DEFAULT_RECONCILE);
    }

    @Test
    @Transactional
    void fullUpdateRecipientWithPatch() throws Exception {
        // Initialize the database
        recipientRepository.saveAndFlush(recipient);

        int databaseSizeBeforeUpdate = recipientRepository.findAll().size();

        // Update the recipient using partial update
        Recipient partialUpdatedRecipient = new Recipient();
        partialUpdatedRecipient.setId(recipient.getId());

        partialUpdatedRecipient
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
            .reconcile(UPDATED_RECONCILE);

        restRecipientMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedRecipient.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedRecipient))
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
        assertThat(testRecipient.getActive()).isEqualTo(UPDATED_ACTIVE);
        assertThat(testRecipient.getCheck()).isEqualTo(UPDATED_CHECK);
        assertThat(testRecipient.getPay()).isEqualTo(UPDATED_PAY);
        assertThat(testRecipient.getStatus()).isEqualTo(UPDATED_STATUS);
        assertThat(testRecipient.getCancel()).isEqualTo(UPDATED_CANCEL);
        assertThat(testRecipient.getCanDeposit()).isEqualTo(UPDATED_CAN_DEPOSIT);
//        assertThat(testRecipient.getEposPort()).isEqualTo(UPDATED_EPOS_PORT);
//        assertThat(testRecipient.getEposMerchant()).isEqualTo(UPDATED_EPOS_MERCHANT);
//        assertThat(testRecipient.getEposTerminal()).isEqualTo(UPDATED_EPOS_TERMINAL);
        assertThat(testRecipient.getSettlementAccount()).isEqualTo(UPDATED_SETTLEMENT_ACCOUNT);
        assertThat(testRecipient.getMfo()).isEqualTo(UPDATED_MFO);
        assertThat(testRecipient.getInn()).isEqualTo(UPDATED_INN);
        assertThat(testRecipient.getReconcile()).isEqualTo(UPDATED_RECONCILE);
    }

    @Test
    @Transactional
    void patchNonExistingRecipient() throws Exception {
        int databaseSizeBeforeUpdate = recipientRepository.findAll().size();
        recipient.setId(count.incrementAndGet());

        // Create the Recipient
        RecipientDTO recipientDTO = recipientMapper.toDto(recipient);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restRecipientMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, recipientDTO.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(recipientDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Recipient in the database
        List<Recipient> recipientList = recipientRepository.findAll();
        assertThat(recipientList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchRecipient() throws Exception {
        int databaseSizeBeforeUpdate = recipientRepository.findAll().size();
        recipient.setId(count.incrementAndGet());

        // Create the Recipient
        RecipientDTO recipientDTO = recipientMapper.toDto(recipient);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restRecipientMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, count.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(recipientDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Recipient in the database
        List<Recipient> recipientList = recipientRepository.findAll();
        assertThat(recipientList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamRecipient() throws Exception {
        int databaseSizeBeforeUpdate = recipientRepository.findAll().size();
        recipient.setId(count.incrementAndGet());

        // Create the Recipient
        RecipientDTO recipientDTO = recipientMapper.toDto(recipient);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restRecipientMockMvc
            .perform(
                patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(TestUtil.convertObjectToJsonBytes(recipientDTO))
            )
            .andExpect(status().isMethodNotAllowed());

        // Validate the Recipient in the database
        List<Recipient> recipientList = recipientRepository.findAll();
        assertThat(recipientList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteRecipient() throws Exception {
        // Initialize the database
        recipientRepository.saveAndFlush(recipient);

        int databaseSizeBeforeDelete = recipientRepository.findAll().size();

        // Delete the recipient
        restRecipientMockMvc
            .perform(delete(ENTITY_API_URL_ID, recipient.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        List<Recipient> recipientList = recipientRepository.findAll();
        assertThat(recipientList).hasSize(databaseSizeBeforeDelete - 1);
    }
}
