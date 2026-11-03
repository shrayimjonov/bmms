package uz.paykap.web.rest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import uz.paykap.IntegrationTest;
import uz.paykap.domain.P2pCommission;
import uz.paykap.domain.P2pCondition;
import uz.paykap.domain.P2pParam;
import uz.paykap.domain.enumeration.P2pProcessorType;
import uz.paykap.repository.P2pConditionRepository;
import uz.paykap.repository.P2pParamRepository;

import javax.persistence.EntityManager;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for the {@link P2pParamResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
public class P2pParamResourceIT {

    private static final Integer DEFAULT_PRIORITY = 1;
    private static final Integer UPDATED_PRIORITY = 2;
    private static final Integer SMALLER_PRIORITY = 1 - 1;

    private static final Boolean DEFAULT_ACTIVE = false;
    private static final Boolean UPDATED_ACTIVE = true;

    private static final P2pProcessorType DEFAULT_SENDER_PC = P2pProcessorType.UZCARD;
    private static final P2pProcessorType UPDATED_SENDER_PC = P2pProcessorType.HUMO;

    private static final P2pProcessorType DEFAULT_RECIPIENT_PC = P2pProcessorType.UZCARD;
    private static final P2pProcessorType UPDATED_RECIPIENT_PC = P2pProcessorType.HUMO;

    private static final String DEFAULT_VALUE = "AAAAAAAAAA";
    private static final String UPDATED_VALUE = "BBBBBBBBBB";

    private static final String DEFAULT_NAME = "AAAAAAAAAA";
    private static final String UPDATED_NAME = "BBBBBBBBBB";

    private static final String ENTITY_API_URL = "/api/p-2-p-params";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong count = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private P2pParamRepository p2pParamRepository;

    @Autowired
    private P2pConditionRepository p2pConditionRepository;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restP2pParamMockMvc;

    private P2pParam p2pParam;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static P2pParam createEntity(EntityManager em) {
        P2pParam p2pParam = new P2pParam()
            .priority(DEFAULT_PRIORITY)
            .active(DEFAULT_ACTIVE)
            .senderPc(DEFAULT_SENDER_PC)
            .recipientPc(DEFAULT_RECIPIENT_PC)
            .value(DEFAULT_VALUE)
            .name(DEFAULT_NAME);
        // Add required entity
        P2pCondition p2pCondition;
        if (TestUtil.findAll(em, P2pCondition.class).isEmpty()) {
            p2pCondition = P2pConditionResourceIT.createEntity(em);
            em.persist(p2pCondition);
            em.flush();
        } else {
            p2pCondition = TestUtil.findAll(em, P2pCondition.class).get(0);
        }
        p2pParam.setSenderCondition(p2pCondition);
        // Add required entity
        p2pParam.setRecipientCondition(p2pCondition);
        // Add required entity
        P2pCommission p2pCommission;
        if (TestUtil.findAll(em, P2pCommission.class).isEmpty()) {
            p2pCommission = P2pCommissionResourceIT.createEntity(em);
            em.persist(p2pCommission);
            em.flush();
        } else {
            p2pCommission = TestUtil.findAll(em, P2pCommission.class).get(0);
        }
        p2pParam.setSenderPercent(p2pCommission);
        // Add required entity
        p2pParam.setRecipientPercent(p2pCommission);
        return p2pParam;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static P2pParam createUpdatedEntity(EntityManager em) {
        P2pParam p2pParam = new P2pParam()
            .priority(UPDATED_PRIORITY)
            .active(UPDATED_ACTIVE)
            .senderPc(UPDATED_SENDER_PC)
            .recipientPc(UPDATED_RECIPIENT_PC)
            .value(UPDATED_VALUE)
            .name(UPDATED_NAME);
        // Add required entity
        P2pCondition p2pCondition;
        if (TestUtil.findAll(em, P2pCondition.class).isEmpty()) {
            p2pCondition = P2pConditionResourceIT.createUpdatedEntity(em);
            em.persist(p2pCondition);
            em.flush();
        } else {
            p2pCondition = TestUtil.findAll(em, P2pCondition.class).get(0);
        }
        p2pParam.setSenderCondition(p2pCondition);
        // Add required entity
        p2pParam.setRecipientCondition(p2pCondition);
        // Add required entity
        P2pCommission p2pCommission;
        if (TestUtil.findAll(em, P2pCommission.class).isEmpty()) {
            p2pCommission = P2pCommissionResourceIT.createUpdatedEntity(em);
            em.persist(p2pCommission);
            em.flush();
        } else {
            p2pCommission = TestUtil.findAll(em, P2pCommission.class).get(0);
        }
        p2pParam.setSenderPercent(p2pCommission);
        // Add required entity
        p2pParam.setRecipientPercent(p2pCommission);
        return p2pParam;
    }

    @BeforeEach
    public void initTest() {
        p2pParam = createEntity(em);
    }

    @Test
    @Transactional
    void createP2pParam() throws Exception {
        int databaseSizeBeforeCreate = p2pParamRepository.findAll().size();
        // Create the P2pParam
        restP2pParamMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(p2pParam)))
            .andExpect(status().isCreated());

        // Validate the P2pParam in the database
        List<P2pParam> p2pParamList = p2pParamRepository.findAll();
        assertThat(p2pParamList).hasSize(databaseSizeBeforeCreate + 1);
        P2pParam testP2pParam = p2pParamList.get(p2pParamList.size() - 1);
        assertThat(testP2pParam.getPriority()).isEqualTo(DEFAULT_PRIORITY);
        assertThat(testP2pParam.getActive()).isEqualTo(DEFAULT_ACTIVE);
        assertThat(testP2pParam.getSenderPc()).isEqualTo(DEFAULT_SENDER_PC);
        assertThat(testP2pParam.getRecipientPc()).isEqualTo(DEFAULT_RECIPIENT_PC);
        assertThat(testP2pParam.getValue()).isEqualTo(DEFAULT_VALUE);
        assertThat(testP2pParam.getName()).isEqualTo(DEFAULT_NAME);
    }

    @Test
    @Transactional
    void createP2pParamWithExistingId() throws Exception {
        // Create the P2pParam with an existing ID
        p2pParam.setId(1L);

        int databaseSizeBeforeCreate = p2pParamRepository.findAll().size();

        // An entity with an existing ID cannot be created, so this API call must fail
        restP2pParamMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(p2pParam)))
            .andExpect(status().isBadRequest());

        // Validate the P2pParam in the database
        List<P2pParam> p2pParamList = p2pParamRepository.findAll();
        assertThat(p2pParamList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkPriorityIsRequired() throws Exception {
        int databaseSizeBeforeTest = p2pParamRepository.findAll().size();
        // set the field null
        p2pParam.setPriority(null);

        // Create the P2pParam, which fails.

        restP2pParamMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(p2pParam)))
            .andExpect(status().isBadRequest());

        List<P2pParam> p2pParamList = p2pParamRepository.findAll();
        assertThat(p2pParamList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkActiveIsRequired() throws Exception {
        int databaseSizeBeforeTest = p2pParamRepository.findAll().size();
        // set the field null
        p2pParam.setActive(null);

        // Create the P2pParam, which fails.

        restP2pParamMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(p2pParam)))
            .andExpect(status().isBadRequest());

        List<P2pParam> p2pParamList = p2pParamRepository.findAll();
        assertThat(p2pParamList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkSenderPcIsRequired() throws Exception {
        int databaseSizeBeforeTest = p2pParamRepository.findAll().size();
        // set the field null
        p2pParam.setSenderPc(null);

        // Create the P2pParam, which fails.

        restP2pParamMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(p2pParam)))
            .andExpect(status().isBadRequest());

        List<P2pParam> p2pParamList = p2pParamRepository.findAll();
        assertThat(p2pParamList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkRecipientPcIsRequired() throws Exception {
        int databaseSizeBeforeTest = p2pParamRepository.findAll().size();
        // set the field null
        p2pParam.setRecipientPc(null);

        // Create the P2pParam, which fails.

        restP2pParamMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(p2pParam)))
            .andExpect(status().isBadRequest());

        List<P2pParam> p2pParamList = p2pParamRepository.findAll();
        assertThat(p2pParamList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllP2pParams() throws Exception {
        // Initialize the database
        p2pParamRepository.saveAndFlush(p2pParam);

        // Get all the p2pParamList
        restP2pParamMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(p2pParam.getId().intValue())))
            .andExpect(jsonPath("$.[*].priority").value(hasItem(DEFAULT_PRIORITY)))
            .andExpect(jsonPath("$.[*].active").value(hasItem(DEFAULT_ACTIVE.booleanValue())))
            .andExpect(jsonPath("$.[*].senderPc").value(hasItem(DEFAULT_SENDER_PC.toString())))
            .andExpect(jsonPath("$.[*].recipientPc").value(hasItem(DEFAULT_RECIPIENT_PC.toString())))
            .andExpect(jsonPath("$.[*].value").value(hasItem(DEFAULT_VALUE.toString())))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME)));
    }

    @Test
    @Transactional
    void getP2pParam() throws Exception {
        // Initialize the database
        p2pParamRepository.saveAndFlush(p2pParam);

        // Get the p2pParam
        restP2pParamMockMvc
            .perform(get(ENTITY_API_URL_ID, p2pParam.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(p2pParam.getId().intValue()))
            .andExpect(jsonPath("$.priority").value(DEFAULT_PRIORITY))
            .andExpect(jsonPath("$.active").value(DEFAULT_ACTIVE.booleanValue()))
            .andExpect(jsonPath("$.senderPc").value(DEFAULT_SENDER_PC.toString()))
            .andExpect(jsonPath("$.recipientPc").value(DEFAULT_RECIPIENT_PC.toString()))
            .andExpect(jsonPath("$.value").value(DEFAULT_VALUE.toString()))
            .andExpect(jsonPath("$.name").value(DEFAULT_NAME));
    }

    @Test
    @Transactional
    void getP2pParamsByIdFiltering() throws Exception {
        // Initialize the database
        p2pParamRepository.saveAndFlush(p2pParam);

        Long id = p2pParam.getId();

        defaultP2pParamShouldBeFound("id.equals=" + id);
//        defaultP2pParamShouldNotBeFound("id.notEquals=" + id);

        defaultP2pParamShouldBeFound("id.greaterThanOrEqual=" + id);
        defaultP2pParamShouldNotBeFound("id.greaterThan=" + id);

//        defaultP2pParamShouldBeFound("id.lessThanOrEqual=" + id);
//        defaultP2pParamShouldNotBeFound("id.lessThan=" + id);
    }

//    @Test
    @Transactional
    void getAllP2pParamsByPriorityIsEqualToSomething() throws Exception {
        // Initialize the database
        p2pParamRepository.saveAndFlush(p2pParam);

        // Get all the p2pParamList where priority equals to DEFAULT_PRIORITY
        defaultP2pParamShouldBeFound("priority.equals=" + DEFAULT_PRIORITY);

        // Get all the p2pParamList where priority equals to UPDATED_PRIORITY
        defaultP2pParamShouldNotBeFound("priority.equals=" + UPDATED_PRIORITY);
    }

//    @Test
    @Transactional
    void getAllP2pParamsByPriorityIsNotEqualToSomething() throws Exception {
        // Initialize the database
        p2pParamRepository.saveAndFlush(p2pParam);

        // Get all the p2pParamList where priority not equals to DEFAULT_PRIORITY
        defaultP2pParamShouldNotBeFound("priority.notEquals=" + DEFAULT_PRIORITY);

        // Get all the p2pParamList where priority not equals to UPDATED_PRIORITY
        defaultP2pParamShouldBeFound("priority.notEquals=" + UPDATED_PRIORITY);
    }

//    @Test
    @Transactional
    void getAllP2pParamsByPriorityIsInShouldWork() throws Exception {
        // Initialize the database
        p2pParamRepository.saveAndFlush(p2pParam);

        // Get all the p2pParamList where priority in DEFAULT_PRIORITY or UPDATED_PRIORITY
        defaultP2pParamShouldBeFound("priority.in=" + DEFAULT_PRIORITY + "," + UPDATED_PRIORITY);

        // Get all the p2pParamList where priority equals to UPDATED_PRIORITY
        defaultP2pParamShouldNotBeFound("priority.in=" + UPDATED_PRIORITY);
    }

//    @Test
    @Transactional
    void getAllP2pParamsByPriorityIsNullOrNotNull() throws Exception {
        // Initialize the database
        p2pParamRepository.saveAndFlush(p2pParam);

        // Get all the p2pParamList where priority is not null
        defaultP2pParamShouldBeFound("priority.specified=true");

        // Get all the p2pParamList where priority is null
        defaultP2pParamShouldNotBeFound("priority.specified=false");
    }

//    @Test
    @Transactional
    void getAllP2pParamsByPriorityIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        p2pParamRepository.saveAndFlush(p2pParam);

        // Get all the p2pParamList where priority is greater than or equal to DEFAULT_PRIORITY
        defaultP2pParamShouldBeFound("priority.greaterThanOrEqual=" + DEFAULT_PRIORITY);

        // Get all the p2pParamList where priority is greater than or equal to UPDATED_PRIORITY
        defaultP2pParamShouldNotBeFound("priority.greaterThanOrEqual=" + UPDATED_PRIORITY);
    }

//    @Test
    @Transactional
    void getAllP2pParamsByPriorityIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        p2pParamRepository.saveAndFlush(p2pParam);

        // Get all the p2pParamList where priority is less than or equal to DEFAULT_PRIORITY
        defaultP2pParamShouldBeFound("priority.lessThanOrEqual=" + DEFAULT_PRIORITY);

        // Get all the p2pParamList where priority is less than or equal to SMALLER_PRIORITY
        defaultP2pParamShouldNotBeFound("priority.lessThanOrEqual=" + SMALLER_PRIORITY);
    }

//    @Test
    @Transactional
    void getAllP2pParamsByPriorityIsLessThanSomething() throws Exception {
        // Initialize the database
        p2pParamRepository.saveAndFlush(p2pParam);

        // Get all the p2pParamList where priority is less than DEFAULT_PRIORITY
        defaultP2pParamShouldNotBeFound("priority.lessThan=" + DEFAULT_PRIORITY);

        // Get all the p2pParamList where priority is less than UPDATED_PRIORITY
        defaultP2pParamShouldBeFound("priority.lessThan=" + UPDATED_PRIORITY);
    }

//    @Test
    @Transactional
    void getAllP2pParamsByPriorityIsGreaterThanSomething() throws Exception {
        // Initialize the database
        p2pParamRepository.saveAndFlush(p2pParam);

        // Get all the p2pParamList where priority is greater than DEFAULT_PRIORITY
        defaultP2pParamShouldNotBeFound("priority.greaterThan=" + DEFAULT_PRIORITY);

        // Get all the p2pParamList where priority is greater than SMALLER_PRIORITY
        defaultP2pParamShouldBeFound("priority.greaterThan=" + SMALLER_PRIORITY);
    }

//    @Test
    @Transactional
    void getAllP2pParamsByActiveIsEqualToSomething() throws Exception {
        // Initialize the database
        p2pParamRepository.saveAndFlush(p2pParam);

        // Get all the p2pParamList where active equals to DEFAULT_ACTIVE
        defaultP2pParamShouldBeFound("active.equals=" + DEFAULT_ACTIVE);

        // Get all the p2pParamList where active equals to UPDATED_ACTIVE
        defaultP2pParamShouldNotBeFound("active.equals=" + UPDATED_ACTIVE);
    }

//    @Test
    @Transactional
    void getAllP2pParamsByActiveIsNotEqualToSomething() throws Exception {
        // Initialize the database
        p2pParamRepository.saveAndFlush(p2pParam);

        // Get all the p2pParamList where active not equals to DEFAULT_ACTIVE
        defaultP2pParamShouldNotBeFound("active.notEquals=" + DEFAULT_ACTIVE);

        // Get all the p2pParamList where active not equals to UPDATED_ACTIVE
        defaultP2pParamShouldBeFound("active.notEquals=" + UPDATED_ACTIVE);
    }

//    @Test
    @Transactional
    void getAllP2pParamsByActiveIsInShouldWork() throws Exception {
        // Initialize the database
        p2pParamRepository.saveAndFlush(p2pParam);

        // Get all the p2pParamList where active in DEFAULT_ACTIVE or UPDATED_ACTIVE
        defaultP2pParamShouldBeFound("active.in=" + DEFAULT_ACTIVE + "," + UPDATED_ACTIVE);

        // Get all the p2pParamList where active equals to UPDATED_ACTIVE
        defaultP2pParamShouldNotBeFound("active.in=" + UPDATED_ACTIVE);
    }

//    @Test
    @Transactional
    void getAllP2pParamsByActiveIsNullOrNotNull() throws Exception {
        // Initialize the database
        p2pParamRepository.saveAndFlush(p2pParam);

        // Get all the p2pParamList where active is not null
        defaultP2pParamShouldBeFound("active.specified=true");

        // Get all the p2pParamList where active is null
        defaultP2pParamShouldNotBeFound("active.specified=false");
    }

//    @Test
    @Transactional
    void getAllP2pParamsBySenderPcIsEqualToSomething() throws Exception {
        // Initialize the database
        p2pParamRepository.saveAndFlush(p2pParam);

        // Get all the p2pParamList where senderPc equals to DEFAULT_SENDER_PC
        defaultP2pParamShouldBeFound("senderPc.equals=" + DEFAULT_SENDER_PC);

        // Get all the p2pParamList where senderPc equals to UPDATED_SENDER_PC
        defaultP2pParamShouldNotBeFound("senderPc.equals=" + UPDATED_SENDER_PC);
    }

//    @Test
    @Transactional
    void getAllP2pParamsBySenderPcIsNotEqualToSomething() throws Exception {
        // Initialize the database
        p2pParamRepository.saveAndFlush(p2pParam);

        // Get all the p2pParamList where senderPc not equals to DEFAULT_SENDER_PC
        defaultP2pParamShouldNotBeFound("senderPc.notEquals=" + DEFAULT_SENDER_PC);

        // Get all the p2pParamList where senderPc not equals to UPDATED_SENDER_PC
        defaultP2pParamShouldBeFound("senderPc.notEquals=" + UPDATED_SENDER_PC);
    }

//    @Test
    @Transactional
    void getAllP2pParamsBySenderPcIsInShouldWork() throws Exception {
        // Initialize the database
        p2pParamRepository.saveAndFlush(p2pParam);

        // Get all the p2pParamList where senderPc in DEFAULT_SENDER_PC or UPDATED_SENDER_PC
        defaultP2pParamShouldBeFound("senderPc.in=" + DEFAULT_SENDER_PC + "," + UPDATED_SENDER_PC);

        // Get all the p2pParamList where senderPc equals to UPDATED_SENDER_PC
        defaultP2pParamShouldNotBeFound("senderPc.in=" + UPDATED_SENDER_PC);
    }

//    @Test
    @Transactional
    void getAllP2pParamsBySenderPcIsNullOrNotNull() throws Exception {
        // Initialize the database
        p2pParamRepository.saveAndFlush(p2pParam);

        // Get all the p2pParamList where senderPc is not null
        defaultP2pParamShouldBeFound("senderPc.specified=true");

        // Get all the p2pParamList where senderPc is null
        defaultP2pParamShouldNotBeFound("senderPc.specified=false");
    }

//    @Test
    @Transactional
    void getAllP2pParamsByRecipientPcIsEqualToSomething() throws Exception {
        // Initialize the database
        p2pParamRepository.saveAndFlush(p2pParam);

        // Get all the p2pParamList where recipientPc equals to DEFAULT_RECIPIENT_PC
        defaultP2pParamShouldBeFound("recipientPc.equals=" + DEFAULT_RECIPIENT_PC);

        // Get all the p2pParamList where recipientPc equals to UPDATED_RECIPIENT_PC
        defaultP2pParamShouldNotBeFound("recipientPc.equals=" + UPDATED_RECIPIENT_PC);
    }

//    @Test
    @Transactional
    void getAllP2pParamsByRecipientPcIsNotEqualToSomething() throws Exception {
        // Initialize the database
        p2pParamRepository.saveAndFlush(p2pParam);

        // Get all the p2pParamList where recipientPc not equals to DEFAULT_RECIPIENT_PC
        defaultP2pParamShouldNotBeFound("recipientPc.notEquals=" + DEFAULT_RECIPIENT_PC);

        // Get all the p2pParamList where recipientPc not equals to UPDATED_RECIPIENT_PC
        defaultP2pParamShouldBeFound("recipientPc.notEquals=" + UPDATED_RECIPIENT_PC);
    }

//    @Test
    @Transactional
    void getAllP2pParamsByRecipientPcIsInShouldWork() throws Exception {
        // Initialize the database
        p2pParamRepository.saveAndFlush(p2pParam);

        // Get all the p2pParamList where recipientPc in DEFAULT_RECIPIENT_PC or UPDATED_RECIPIENT_PC
        defaultP2pParamShouldBeFound("recipientPc.in=" + DEFAULT_RECIPIENT_PC + "," + UPDATED_RECIPIENT_PC);

        // Get all the p2pParamList where recipientPc equals to UPDATED_RECIPIENT_PC
        defaultP2pParamShouldNotBeFound("recipientPc.in=" + UPDATED_RECIPIENT_PC);
    }

//    @Test
    @Transactional
    void getAllP2pParamsByRecipientPcIsNullOrNotNull() throws Exception {
        // Initialize the database
        p2pParamRepository.saveAndFlush(p2pParam);

        // Get all the p2pParamList where recipientPc is not null
        defaultP2pParamShouldBeFound("recipientPc.specified=true");

        // Get all the p2pParamList where recipientPc is null
        defaultP2pParamShouldNotBeFound("recipientPc.specified=false");
    }

    //    @Test
    @Transactional
    void getAllP2pParamsBySenderConditionIsEqualToSomething() throws Exception {
        // Initialize the database
        p2pParamRepository.saveAndFlush(p2pParam);
        P2pCondition senderCondition = P2pConditionResourceIT.createEntity(em);
        em.persist(senderCondition);
        em.flush();
        p2pParam.setSenderCondition(senderCondition);
        p2pParamRepository.saveAndFlush(p2pParam);
        Long senderConditionId = senderCondition.getId();

        // Get all the p2pParamList where senderCondition equals to senderConditionId
        defaultP2pParamShouldBeFound("senderConditionId.equals=" + senderConditionId);

        // Get all the p2pParamList where senderCondition equals to (senderConditionId + 1)
        defaultP2pParamShouldNotBeFound("senderConditionId.equals=" + (senderConditionId + 1));
    }

    //    @Test
    @Transactional
    void getAllP2pParamsByRecipientConditionIsEqualToSomething() throws Exception {
        // Initialize the database
        p2pParamRepository.saveAndFlush(p2pParam);
        P2pCondition recipientCondition = P2pConditionResourceIT.createEntity(em);
        em.persist(recipientCondition);
        em.flush();
        p2pParam.setRecipientCondition(recipientCondition);
        p2pParamRepository.saveAndFlush(p2pParam);
        Long recipientConditionId = recipientCondition.getId();

        // Get all the p2pParamList where recipientCondition equals to recipientConditionId
        defaultP2pParamShouldBeFound("recipientConditionId.equals=" + recipientConditionId);

        // Get all the p2pParamList where recipientCondition equals to (recipientConditionId + 1)
        defaultP2pParamShouldNotBeFound("recipientConditionId.equals=" + (recipientConditionId + 1));
    }

    //    @Test
    @Transactional
    void getAllP2pParamsBySenderPercentIsEqualToSomething() throws Exception {
        // Initialize the database
        p2pParamRepository.saveAndFlush(p2pParam);
        P2pCommission senderPercent = P2pCommissionResourceIT.createEntity(em);
        em.persist(senderPercent);
        em.flush();
        p2pParam.setSenderPercent(senderPercent);
        p2pParamRepository.saveAndFlush(p2pParam);
        Long senderPercentId = senderPercent.getId();

        // Get all the p2pParamList where senderPercent equals to senderPercentId
        defaultP2pParamShouldBeFound("senderPercentId.equals=" + senderPercentId);

        // Get all the p2pParamList where senderPercent equals to (senderPercentId + 1)
        defaultP2pParamShouldNotBeFound("senderPercentId.equals=" + (senderPercentId + 1));
    }

    //    @Test
    @Transactional
    void getAllP2pParamsByRecipientPercentIsEqualToSomething() throws Exception {
        // Initialize the database
        p2pParamRepository.saveAndFlush(p2pParam);
        P2pCommission recipientPercent = P2pCommissionResourceIT.createEntity(em);
        em.persist(recipientPercent);
        em.flush();
        p2pParam.setRecipientPercent(recipientPercent);
        p2pParamRepository.saveAndFlush(p2pParam);
        Long recipientPercentId = recipientPercent.getId();

        // Get all the p2pParamList where recipientPercent equals to recipientPercentId
        defaultP2pParamShouldBeFound("recipientPercentId.equals=" + recipientPercentId);

        // Get all the p2pParamList where recipientPercent equals to (recipientPercentId + 1)
        defaultP2pParamShouldNotBeFound("recipientPercentId.equals=" + (recipientPercentId + 1));
    }

    /**
     * Executes the search, and checks that the default entity is returned.
     */
    private void defaultP2pParamShouldBeFound(String filter) throws Exception {
        restP2pParamMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(p2pParam.getId().intValue())))
            .andExpect(jsonPath("$.[*].priority").value(hasItem(DEFAULT_PRIORITY)))
            .andExpect(jsonPath("$.[*].active").value(hasItem(DEFAULT_ACTIVE.booleanValue())))
            .andExpect(jsonPath("$.[*].senderPc").value(hasItem(DEFAULT_SENDER_PC.toString())))
            .andExpect(jsonPath("$.[*].recipientPc").value(hasItem(DEFAULT_RECIPIENT_PC.toString())))
            .andExpect(jsonPath("$.[*].value").value(hasItem(DEFAULT_VALUE.toString())))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME)));

        // Check, that the count call also returns 1
        restP2pParamMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("1"));
    }

    /**
     * Executes the search, and checks that the default entity is not returned.
     */
    private void defaultP2pParamShouldNotBeFound(String filter) throws Exception {
        restP2pParamMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty());

        // Check, that the count call also returns 0
        restP2pParamMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("0"));
    }

    @Test
    @Transactional
    void getNonExistingP2pParam() throws Exception {
        // Get the p2pParam
        restP2pParamMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putNewP2pParam() throws Exception {
        // Initialize the database
        p2pParamRepository.saveAndFlush(p2pParam);

        int databaseSizeBeforeUpdate = p2pParamRepository.findAll().size();

        // Update the p2pParam
        P2pParam updatedP2pParam = p2pParamRepository.findById(p2pParam.getId()).get();
        // Disconnect from session so that the updates on updatedP2pParam are not directly saved in db
        em.detach(updatedP2pParam);
        updatedP2pParam
            .priority(UPDATED_PRIORITY)
            .active(UPDATED_ACTIVE)
            .senderPc(UPDATED_SENDER_PC)
            .recipientPc(UPDATED_RECIPIENT_PC)
            .value(UPDATED_VALUE)
            .name(UPDATED_NAME);

        restP2pParamMockMvc
            .perform(
                put(ENTITY_API_URL_ID, updatedP2pParam.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(updatedP2pParam))
            )
            .andExpect(status().isOk());

        // Validate the P2pParam in the database
        List<P2pParam> p2pParamList = p2pParamRepository.findAll();
        assertThat(p2pParamList).hasSize(databaseSizeBeforeUpdate);
        P2pParam testP2pParam = p2pParamList.get(p2pParamList.size() - 1);
        assertThat(testP2pParam.getPriority()).isEqualTo(UPDATED_PRIORITY);
        assertThat(testP2pParam.getActive()).isEqualTo(UPDATED_ACTIVE);
        assertThat(testP2pParam.getSenderPc()).isEqualTo(UPDATED_SENDER_PC);
        assertThat(testP2pParam.getRecipientPc()).isEqualTo(UPDATED_RECIPIENT_PC);
        assertThat(testP2pParam.getValue()).isEqualTo(UPDATED_VALUE);
        assertThat(testP2pParam.getName()).isEqualTo(UPDATED_NAME);
    }

    @Test
    @Transactional
    void putNonExistingP2pParam() throws Exception {
        int databaseSizeBeforeUpdate = p2pParamRepository.findAll().size();
        p2pParam.setId(count.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restP2pParamMockMvc
            .perform(
                put(ENTITY_API_URL_ID, p2pParam.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(p2pParam))
            )
            .andExpect(status().isBadRequest());

        // Validate the P2pParam in the database
        List<P2pParam> p2pParamList = p2pParamRepository.findAll();
        assertThat(p2pParamList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchP2pParam() throws Exception {
        int databaseSizeBeforeUpdate = p2pParamRepository.findAll().size();
        p2pParam.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restP2pParamMockMvc
            .perform(
                put(ENTITY_API_URL_ID, count.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(p2pParam))
            )
            .andExpect(status().isBadRequest());

        // Validate the P2pParam in the database
        List<P2pParam> p2pParamList = p2pParamRepository.findAll();
        assertThat(p2pParamList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamP2pParam() throws Exception {
        int databaseSizeBeforeUpdate = p2pParamRepository.findAll().size();
        p2pParam.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restP2pParamMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(p2pParam)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the P2pParam in the database
        List<P2pParam> p2pParamList = p2pParamRepository.findAll();
        assertThat(p2pParamList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateP2pParamWithPatch() throws Exception {
        // Initialize the database
        p2pParamRepository.saveAndFlush(p2pParam);

        int databaseSizeBeforeUpdate = p2pParamRepository.findAll().size();

        // Update the p2pParam using partial update
        P2pParam partialUpdatedP2pParam = new P2pParam();
        partialUpdatedP2pParam.setId(p2pParam.getId());

        partialUpdatedP2pParam
            .priority(UPDATED_PRIORITY)
            .senderPc(UPDATED_SENDER_PC)
            .recipientPc(UPDATED_RECIPIENT_PC)
            .value(UPDATED_VALUE)
            .name(UPDATED_NAME);

        restP2pParamMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedP2pParam.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedP2pParam))
            )
            .andExpect(status().isOk());

        // Validate the P2pParam in the database
        List<P2pParam> p2pParamList = p2pParamRepository.findAll();
        assertThat(p2pParamList).hasSize(databaseSizeBeforeUpdate);
        P2pParam testP2pParam = p2pParamList.get(p2pParamList.size() - 1);
        assertThat(testP2pParam.getPriority()).isEqualTo(UPDATED_PRIORITY);
        assertThat(testP2pParam.getActive()).isEqualTo(DEFAULT_ACTIVE);
        assertThat(testP2pParam.getSenderPc()).isEqualTo(UPDATED_SENDER_PC);
        assertThat(testP2pParam.getRecipientPc()).isEqualTo(UPDATED_RECIPIENT_PC);
        assertThat(testP2pParam.getValue()).isEqualTo(UPDATED_VALUE);
        assertThat(testP2pParam.getName()).isEqualTo(UPDATED_NAME);
    }

    @Test
    @Transactional
    void fullUpdateP2pParamWithPatch() throws Exception {
        // Initialize the database
        p2pParamRepository.saveAndFlush(p2pParam);

        int databaseSizeBeforeUpdate = p2pParamRepository.findAll().size();

        // Update the p2pParam using partial update
        P2pParam partialUpdatedP2pParam = new P2pParam();
        partialUpdatedP2pParam.setId(p2pParam.getId());

        partialUpdatedP2pParam
            .priority(UPDATED_PRIORITY)
            .active(UPDATED_ACTIVE)
            .senderPc(UPDATED_SENDER_PC)
            .recipientPc(UPDATED_RECIPIENT_PC)
            .value(UPDATED_VALUE)
            .name(UPDATED_NAME);

        restP2pParamMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedP2pParam.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedP2pParam))
            )
            .andExpect(status().isOk());

        // Validate the P2pParam in the database
        List<P2pParam> p2pParamList = p2pParamRepository.findAll();
        assertThat(p2pParamList).hasSize(databaseSizeBeforeUpdate);
        P2pParam testP2pParam = p2pParamList.get(p2pParamList.size() - 1);
        assertThat(testP2pParam.getPriority()).isEqualTo(UPDATED_PRIORITY);
        assertThat(testP2pParam.getActive()).isEqualTo(UPDATED_ACTIVE);
        assertThat(testP2pParam.getSenderPc()).isEqualTo(UPDATED_SENDER_PC);
        assertThat(testP2pParam.getRecipientPc()).isEqualTo(UPDATED_RECIPIENT_PC);
        assertThat(testP2pParam.getValue()).isEqualTo(UPDATED_VALUE);
        assertThat(testP2pParam.getName()).isEqualTo(UPDATED_NAME);
    }

    @Test
    @Transactional
    void patchNonExistingP2pParam() throws Exception {
        int databaseSizeBeforeUpdate = p2pParamRepository.findAll().size();
        p2pParam.setId(count.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restP2pParamMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, p2pParam.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(p2pParam))
            )
            .andExpect(status().isBadRequest());

        // Validate the P2pParam in the database
        List<P2pParam> p2pParamList = p2pParamRepository.findAll();
        assertThat(p2pParamList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchP2pParam() throws Exception {
        int databaseSizeBeforeUpdate = p2pParamRepository.findAll().size();
        p2pParam.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restP2pParamMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, count.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(p2pParam))
            )
            .andExpect(status().isBadRequest());

        // Validate the P2pParam in the database
        List<P2pParam> p2pParamList = p2pParamRepository.findAll();
        assertThat(p2pParamList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamP2pParam() throws Exception {
        int databaseSizeBeforeUpdate = p2pParamRepository.findAll().size();
        p2pParam.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restP2pParamMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(TestUtil.convertObjectToJsonBytes(p2pParam)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the P2pParam in the database
        List<P2pParam> p2pParamList = p2pParamRepository.findAll();
        assertThat(p2pParamList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteP2pParam() throws Exception {
        // Initialize the database
        p2pParamRepository.saveAndFlush(p2pParam);

        int databaseSizeBeforeDelete = p2pParamRepository.findAll().size();

        // Delete the p2pParam
        restP2pParamMockMvc
            .perform(delete(ENTITY_API_URL_ID, p2pParam.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        List<P2pParam> p2pParamList = p2pParamRepository.findAll();
        assertThat(p2pParamList).hasSize(databaseSizeBeforeDelete - 1);
    }
}
