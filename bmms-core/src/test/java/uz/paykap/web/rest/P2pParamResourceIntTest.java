package uz.paykap.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static uz.paykap.web.rest.TestUtil.createFormattingConversionService;

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
import uz.paykap.domain.P2pCommission;
import uz.paykap.domain.P2pCondition;
import uz.paykap.domain.P2pParam;
import uz.paykap.domain.enumeration.P2pProcessorType;
import uz.paykap.repository.P2pParamRepository;
import uz.paykap.service.P2pParamQueryService;
import uz.paykap.service.P2pParamService;
import uz.paykap.web.rest.errors.ExceptionTranslator;

/**
 * Test class for the P2pParamResource REST controller.
 *
 * @see P2pParamResource
 */
@RunWith(SpringRunner.class)
@IntegrationTest
public class P2pParamResourceIntTest {

    private static final Integer DEFAULT_PRIORITY = 1;
    private static final Integer UPDATED_PRIORITY = 2;

    private static final Boolean DEFAULT_ACTIVE = false;
    private static final Boolean UPDATED_ACTIVE = true;

    private static final P2pProcessorType DEFAULT_SENDER_PC = P2pProcessorType.UZCARD;
    private static final P2pProcessorType UPDATED_SENDER_PC = P2pProcessorType.HUMO;

    private static final P2pProcessorType DEFAULT_RECIPIENT_PC = P2pProcessorType.UZCARD;
    private static final P2pProcessorType UPDATED_RECIPIENT_PC = P2pProcessorType.HUMO;

    private static final String DEFAULT_VALUE = "AAAAAAAAAA";
    private static final String UPDATED_VALUE = "BBBBBBBBBB";

    @Autowired
    private P2pParamRepository p2pParamRepository;

    @Autowired
    private P2pParamService p2pParamService;

    @Autowired
    private P2pParamQueryService p2pParamQueryService;

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

    private MockMvc restP2pParamMockMvc;

    private P2pParam p2pParam;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        final P2pParamResource p2pParamResource = new P2pParamResource(p2pParamService, p2pParamRepository, p2pParamQueryService);
        this.restP2pParamMockMvc =
            MockMvcBuilders
                .standaloneSetup(p2pParamResource)
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
    public static P2pParam createEntity(EntityManager em) {
        P2pParam p2pParam = new P2pParam()
            .priority(DEFAULT_PRIORITY)
            .active(DEFAULT_ACTIVE)
            .senderPc(DEFAULT_SENDER_PC)
            .recipientPc(DEFAULT_RECIPIENT_PC)
            .value(DEFAULT_VALUE);
        // Add required entity
        P2pCondition p2pCondition = P2pConditionResourceIntTest.createEntity(em);
        em.persist(p2pCondition);
        em.flush();
        p2pParam.setSenderCondition(p2pCondition);
        // Add required entity
        p2pParam.setRecipientCondition(p2pCondition);
        // Add required entity
        P2pCommission p2pCommission = P2pCommissionResourceIntTest.createEntity(em);
        em.persist(p2pCommission);
        em.flush();
        p2pParam.setSenderPercent(p2pCommission);
        // Add required entity
        p2pParam.setRecipientPercent(p2pCommission);
        return p2pParam;
    }

    @Before
    public void initTest() {
        p2pParam = createEntity(em);
    }

    @Test
    @Transactional
    public void createP2pParam() throws Exception {
        int databaseSizeBeforeCreate = p2pParamRepository.findAll().size();

        // Create the P2pParam
        restP2pParamMockMvc
            .perform(
                post("/api/p-2-p-params").contentType(TestUtil.APPLICATION_JSON_UTF8).content(TestUtil.convertObjectToJsonBytes(p2pParam))
            )
            .andExpect(status().isCreated());

        // Validate the P2pParam in the database
        List<P2pParam> p2pParamList = p2pParamRepository.findAll();
        assertThat(p2pParamList).hasSize(databaseSizeBeforeCreate + 1);
        P2pParam testP2pParam = p2pParamList.get(p2pParamList.size() - 1);
        assertThat(testP2pParam.getPriority()).isEqualTo(DEFAULT_PRIORITY);
        assertThat(testP2pParam.isActive()).isEqualTo(DEFAULT_ACTIVE);
        assertThat(testP2pParam.getSenderPc()).isEqualTo(DEFAULT_SENDER_PC);
        assertThat(testP2pParam.getRecipientPc()).isEqualTo(DEFAULT_RECIPIENT_PC);
        assertThat(testP2pParam.getValue()).isEqualTo(DEFAULT_VALUE);
    }

    @Test
    @Transactional
    public void createP2pParamWithExistingId() throws Exception {
        int databaseSizeBeforeCreate = p2pParamRepository.findAll().size();

        // Create the P2pParam with an existing ID
        p2pParam.setId(1L);

        // An entity with an existing ID cannot be created, so this API call must fail
        restP2pParamMockMvc
            .perform(
                post("/api/p-2-p-params").contentType(TestUtil.APPLICATION_JSON_UTF8).content(TestUtil.convertObjectToJsonBytes(p2pParam))
            )
            .andExpect(status().isBadRequest());

        // Validate the P2pParam in the database
        List<P2pParam> p2pParamList = p2pParamRepository.findAll();
        assertThat(p2pParamList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    public void checkPriorityIsRequired() throws Exception {
        int databaseSizeBeforeTest = p2pParamRepository.findAll().size();
        // set the field null
        p2pParam.setPriority(null);

        // Create the P2pParam, which fails.

        restP2pParamMockMvc
            .perform(
                post("/api/p-2-p-params").contentType(TestUtil.APPLICATION_JSON_UTF8).content(TestUtil.convertObjectToJsonBytes(p2pParam))
            )
            .andExpect(status().isBadRequest());

        List<P2pParam> p2pParamList = p2pParamRepository.findAll();
        assertThat(p2pParamList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkActiveIsRequired() throws Exception {
        int databaseSizeBeforeTest = p2pParamRepository.findAll().size();
        // set the field null
        p2pParam.setActive(null);

        // Create the P2pParam, which fails.

        restP2pParamMockMvc
            .perform(
                post("/api/p-2-p-params").contentType(TestUtil.APPLICATION_JSON_UTF8).content(TestUtil.convertObjectToJsonBytes(p2pParam))
            )
            .andExpect(status().isBadRequest());

        List<P2pParam> p2pParamList = p2pParamRepository.findAll();
        assertThat(p2pParamList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkSenderPcIsRequired() throws Exception {
        int databaseSizeBeforeTest = p2pParamRepository.findAll().size();
        // set the field null
        p2pParam.setSenderPc(null);

        // Create the P2pParam, which fails.

        restP2pParamMockMvc
            .perform(
                post("/api/p-2-p-params").contentType(TestUtil.APPLICATION_JSON_UTF8).content(TestUtil.convertObjectToJsonBytes(p2pParam))
            )
            .andExpect(status().isBadRequest());

        List<P2pParam> p2pParamList = p2pParamRepository.findAll();
        assertThat(p2pParamList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkRecipientPcIsRequired() throws Exception {
        int databaseSizeBeforeTest = p2pParamRepository.findAll().size();
        // set the field null
        p2pParam.setRecipientPc(null);

        // Create the P2pParam, which fails.

        restP2pParamMockMvc
            .perform(
                post("/api/p-2-p-params").contentType(TestUtil.APPLICATION_JSON_UTF8).content(TestUtil.convertObjectToJsonBytes(p2pParam))
            )
            .andExpect(status().isBadRequest());

        List<P2pParam> p2pParamList = p2pParamRepository.findAll();
        assertThat(p2pParamList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void getAllP2pParams() throws Exception {
        // Initialize the database
        p2pParamRepository.saveAndFlush(p2pParam);

        // Get all the p2pParamList
        restP2pParamMockMvc
            .perform(get("/api/p-2-p-params?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(p2pParam.getId().intValue())))
            .andExpect(jsonPath("$.[*].priority").value(hasItem(DEFAULT_PRIORITY)))
            .andExpect(jsonPath("$.[*].active").value(hasItem(DEFAULT_ACTIVE.booleanValue())))
            .andExpect(jsonPath("$.[*].senderPc").value(hasItem(DEFAULT_SENDER_PC.toString())))
            .andExpect(jsonPath("$.[*].recipientPc").value(hasItem(DEFAULT_RECIPIENT_PC.toString())))
            .andExpect(jsonPath("$.[*].value").value(hasItem(DEFAULT_VALUE.toString())));
    }

    @Test
    @Transactional
    public void getP2pParam() throws Exception {
        // Initialize the database
        p2pParamRepository.saveAndFlush(p2pParam);

        // Get the p2pParam
        restP2pParamMockMvc
            .perform(get("/api/p-2-p-params/{id}", p2pParam.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.id").value(p2pParam.getId().intValue()))
            .andExpect(jsonPath("$.priority").value(DEFAULT_PRIORITY))
            .andExpect(jsonPath("$.active").value(DEFAULT_ACTIVE.booleanValue()))
            .andExpect(jsonPath("$.senderPc").value(DEFAULT_SENDER_PC.toString()))
            .andExpect(jsonPath("$.recipientPc").value(DEFAULT_RECIPIENT_PC.toString()))
            .andExpect(jsonPath("$.value").value(DEFAULT_VALUE.toString()));
    }

    @Test
    @Transactional
    public void getAllP2pParamsByPriorityIsEqualToSomething() throws Exception {
        // Initialize the database
        p2pParamRepository.saveAndFlush(p2pParam);

        // Get all the p2pParamList where priority equals to DEFAULT_PRIORITY
        defaultP2pParamShouldBeFound("priority.equals=" + DEFAULT_PRIORITY);

        // Get all the p2pParamList where priority equals to UPDATED_PRIORITY
        defaultP2pParamShouldNotBeFound("priority.equals=" + UPDATED_PRIORITY);
    }

    @Test
    @Transactional
    public void getAllP2pParamsByPriorityIsInShouldWork() throws Exception {
        // Initialize the database
        p2pParamRepository.saveAndFlush(p2pParam);

        // Get all the p2pParamList where priority in DEFAULT_PRIORITY or UPDATED_PRIORITY
        defaultP2pParamShouldBeFound("priority.in=" + DEFAULT_PRIORITY + "," + UPDATED_PRIORITY);

        // Get all the p2pParamList where priority equals to UPDATED_PRIORITY
        defaultP2pParamShouldNotBeFound("priority.in=" + UPDATED_PRIORITY);
    }

    @Test
    @Transactional
    public void getAllP2pParamsByPriorityIsNullOrNotNull() throws Exception {
        // Initialize the database
        p2pParamRepository.saveAndFlush(p2pParam);

        // Get all the p2pParamList where priority is not null
        defaultP2pParamShouldBeFound("priority.specified=true");

        // Get all the p2pParamList where priority is null
        defaultP2pParamShouldNotBeFound("priority.specified=false");
    }

    @Test
    @Transactional
    public void getAllP2pParamsByPriorityIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        p2pParamRepository.saveAndFlush(p2pParam);

        // Get all the p2pParamList where priority greater than or equals to DEFAULT_PRIORITY
        defaultP2pParamShouldBeFound("priority.greaterOrEqualThan=" + DEFAULT_PRIORITY);

        // Get all the p2pParamList where priority greater than or equals to UPDATED_PRIORITY
        defaultP2pParamShouldNotBeFound("priority.greaterOrEqualThan=" + UPDATED_PRIORITY);
    }

    @Test
    @Transactional
    public void getAllP2pParamsByPriorityIsLessThanSomething() throws Exception {
        // Initialize the database
        p2pParamRepository.saveAndFlush(p2pParam);

        // Get all the p2pParamList where priority less than or equals to DEFAULT_PRIORITY
        defaultP2pParamShouldNotBeFound("priority.lessThan=" + DEFAULT_PRIORITY);

        // Get all the p2pParamList where priority less than or equals to UPDATED_PRIORITY
        defaultP2pParamShouldBeFound("priority.lessThan=" + UPDATED_PRIORITY);
    }

    @Test
    @Transactional
    public void getAllP2pParamsByActiveIsEqualToSomething() throws Exception {
        // Initialize the database
        p2pParamRepository.saveAndFlush(p2pParam);

        // Get all the p2pParamList where active equals to DEFAULT_ACTIVE
        defaultP2pParamShouldBeFound("active.equals=" + DEFAULT_ACTIVE);

        // Get all the p2pParamList where active equals to UPDATED_ACTIVE
        defaultP2pParamShouldNotBeFound("active.equals=" + UPDATED_ACTIVE);
    }

    @Test
    @Transactional
    public void getAllP2pParamsByActiveIsInShouldWork() throws Exception {
        // Initialize the database
        p2pParamRepository.saveAndFlush(p2pParam);

        // Get all the p2pParamList where active in DEFAULT_ACTIVE or UPDATED_ACTIVE
        defaultP2pParamShouldBeFound("active.in=" + DEFAULT_ACTIVE + "," + UPDATED_ACTIVE);

        // Get all the p2pParamList where active equals to UPDATED_ACTIVE
        defaultP2pParamShouldNotBeFound("active.in=" + UPDATED_ACTIVE);
    }

    @Test
    @Transactional
    public void getAllP2pParamsByActiveIsNullOrNotNull() throws Exception {
        // Initialize the database
        p2pParamRepository.saveAndFlush(p2pParam);

        // Get all the p2pParamList where active is not null
        defaultP2pParamShouldBeFound("active.specified=true");

        // Get all the p2pParamList where active is null
        defaultP2pParamShouldNotBeFound("active.specified=false");
    }

    @Test
    @Transactional
    public void getAllP2pParamsBySenderPcIsEqualToSomething() throws Exception {
        // Initialize the database
        p2pParamRepository.saveAndFlush(p2pParam);

        // Get all the p2pParamList where senderPc equals to DEFAULT_SENDER_PC
        defaultP2pParamShouldBeFound("senderPc.equals=" + DEFAULT_SENDER_PC);

        // Get all the p2pParamList where senderPc equals to UPDATED_SENDER_PC
        defaultP2pParamShouldNotBeFound("senderPc.equals=" + UPDATED_SENDER_PC);
    }

    @Test
    @Transactional
    public void getAllP2pParamsBySenderPcIsInShouldWork() throws Exception {
        // Initialize the database
        p2pParamRepository.saveAndFlush(p2pParam);

        // Get all the p2pParamList where senderPc in DEFAULT_SENDER_PC or UPDATED_SENDER_PC
        defaultP2pParamShouldBeFound("senderPc.in=" + DEFAULT_SENDER_PC + "," + UPDATED_SENDER_PC);

        // Get all the p2pParamList where senderPc equals to UPDATED_SENDER_PC
        defaultP2pParamShouldNotBeFound("senderPc.in=" + UPDATED_SENDER_PC);
    }

    @Test
    @Transactional
    public void getAllP2pParamsBySenderPcIsNullOrNotNull() throws Exception {
        // Initialize the database
        p2pParamRepository.saveAndFlush(p2pParam);

        // Get all the p2pParamList where senderPc is not null
        defaultP2pParamShouldBeFound("senderPc.specified=true");

        // Get all the p2pParamList where senderPc is null
        defaultP2pParamShouldNotBeFound("senderPc.specified=false");
    }

    @Test
    @Transactional
    public void getAllP2pParamsByRecipientPcIsEqualToSomething() throws Exception {
        // Initialize the database
        p2pParamRepository.saveAndFlush(p2pParam);

        // Get all the p2pParamList where recipientPc equals to DEFAULT_RECIPIENT_PC
        defaultP2pParamShouldBeFound("recipientPc.equals=" + DEFAULT_RECIPIENT_PC);

        // Get all the p2pParamList where recipientPc equals to UPDATED_RECIPIENT_PC
        defaultP2pParamShouldNotBeFound("recipientPc.equals=" + UPDATED_RECIPIENT_PC);
    }

    @Test
    @Transactional
    public void getAllP2pParamsByRecipientPcIsInShouldWork() throws Exception {
        // Initialize the database
        p2pParamRepository.saveAndFlush(p2pParam);

        // Get all the p2pParamList where recipientPc in DEFAULT_RECIPIENT_PC or UPDATED_RECIPIENT_PC
        defaultP2pParamShouldBeFound("recipientPc.in=" + DEFAULT_RECIPIENT_PC + "," + UPDATED_RECIPIENT_PC);

        // Get all the p2pParamList where recipientPc equals to UPDATED_RECIPIENT_PC
        defaultP2pParamShouldNotBeFound("recipientPc.in=" + UPDATED_RECIPIENT_PC);
    }

    @Test
    @Transactional
    public void getAllP2pParamsByRecipientPcIsNullOrNotNull() throws Exception {
        // Initialize the database
        p2pParamRepository.saveAndFlush(p2pParam);

        // Get all the p2pParamList where recipientPc is not null
        defaultP2pParamShouldBeFound("recipientPc.specified=true");

        // Get all the p2pParamList where recipientPc is null
        defaultP2pParamShouldNotBeFound("recipientPc.specified=false");
    }

    @Test
    @Transactional
    public void getAllP2pParamsBySenderConditionIsEqualToSomething() throws Exception {
        // Initialize the database
        P2pCondition senderCondition = P2pConditionResourceIntTest.createEntity(em);
        em.persist(senderCondition);
        em.flush();
        p2pParam.setSenderCondition(senderCondition);
        p2pParamRepository.saveAndFlush(p2pParam);
        Long senderConditionId = senderCondition.getId();

        // Get all the p2pParamList where senderCondition equals to senderConditionId
        defaultP2pParamShouldBeFound("senderConditionId.equals=" + senderConditionId);

        // Get all the p2pParamList where senderCondition equals to senderConditionId + 1
        defaultP2pParamShouldNotBeFound("senderConditionId.equals=" + (senderConditionId + 1));
    }

    @Test
    @Transactional
    public void getAllP2pParamsByRecipientConditionIsEqualToSomething() throws Exception {
        // Initialize the database
        P2pCondition recipientCondition = P2pConditionResourceIntTest.createEntity(em);
        em.persist(recipientCondition);
        em.flush();
        p2pParam.setRecipientCondition(recipientCondition);
        p2pParamRepository.saveAndFlush(p2pParam);
        Long recipientConditionId = recipientCondition.getId();

        // Get all the p2pParamList where recipientCondition equals to recipientConditionId
        defaultP2pParamShouldBeFound("recipientConditionId.equals=" + recipientConditionId);

        // Get all the p2pParamList where recipientCondition equals to recipientConditionId + 1
        defaultP2pParamShouldNotBeFound("recipientConditionId.equals=" + (recipientConditionId + 1));
    }

    @Test
    @Transactional
    public void getAllP2pParamsBySenderPercentIsEqualToSomething() throws Exception {
        // Initialize the database
        P2pCommission senderPercent = P2pCommissionResourceIntTest.createEntity(em);
        em.persist(senderPercent);
        em.flush();
        p2pParam.setSenderPercent(senderPercent);
        p2pParamRepository.saveAndFlush(p2pParam);
        Long senderPercentId = senderPercent.getId();

        // Get all the p2pParamList where senderPercent equals to senderPercentId
        defaultP2pParamShouldBeFound("senderPercentId.equals=" + senderPercentId);

        // Get all the p2pParamList where senderPercent equals to senderPercentId + 1
        defaultP2pParamShouldNotBeFound("senderPercentId.equals=" + (senderPercentId + 1));
    }

    @Test
    @Transactional
    public void getAllP2pParamsByRecipientPercentIsEqualToSomething() throws Exception {
        // Initialize the database
        P2pCommission recipientPercent = P2pCommissionResourceIntTest.createEntity(em);
        em.persist(recipientPercent);
        em.flush();
        p2pParam.setRecipientPercent(recipientPercent);
        p2pParamRepository.saveAndFlush(p2pParam);
        Long recipientPercentId = recipientPercent.getId();

        // Get all the p2pParamList where recipientPercent equals to recipientPercentId
        defaultP2pParamShouldBeFound("recipientPercentId.equals=" + recipientPercentId);

        // Get all the p2pParamList where recipientPercent equals to recipientPercentId + 1
        defaultP2pParamShouldNotBeFound("recipientPercentId.equals=" + (recipientPercentId + 1));
    }

    /**
     * Executes the search, and checks that the default entity is returned
     */
    private void defaultP2pParamShouldBeFound(String filter) throws Exception {
        restP2pParamMockMvc
            .perform(get("/api/p-2-p-params?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(p2pParam.getId().intValue())))
            .andExpect(jsonPath("$.[*].priority").value(hasItem(DEFAULT_PRIORITY)))
            .andExpect(jsonPath("$.[*].active").value(hasItem(DEFAULT_ACTIVE.booleanValue())))
            .andExpect(jsonPath("$.[*].senderPc").value(hasItem(DEFAULT_SENDER_PC.toString())))
            .andExpect(jsonPath("$.[*].recipientPc").value(hasItem(DEFAULT_RECIPIENT_PC.toString())))
            .andExpect(jsonPath("$.[*].value").value(hasItem(DEFAULT_VALUE.toString())));

        // Check, that the count call also returns 1
        restP2pParamMockMvc
            .perform(get("/api/p-2-p-params/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(content().string("1"));
    }

    /**
     * Executes the search, and checks that the default entity is not returned
     */
    private void defaultP2pParamShouldNotBeFound(String filter) throws Exception {
        restP2pParamMockMvc
            .perform(get("/api/p-2-p-params?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty());

        // Check, that the count call also returns 0
        restP2pParamMockMvc
            .perform(get("/api/p-2-p-params/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(content().string("0"));
    }

    @Test
    @Transactional
    public void getNonExistingP2pParam() throws Exception {
        // Get the p2pParam
        restP2pParamMockMvc.perform(get("/api/p-2-p-params/{id}", Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updateP2pParam() throws Exception {
        // Initialize the database
        p2pParamService.save(p2pParam);

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
            .value(UPDATED_VALUE);

        restP2pParamMockMvc
            .perform(
                put("/api/p-2-p-params")
                    .contentType(TestUtil.APPLICATION_JSON_UTF8)
                    .content(TestUtil.convertObjectToJsonBytes(updatedP2pParam))
            )
            .andExpect(status().isOk());

        // Validate the P2pParam in the database
        List<P2pParam> p2pParamList = p2pParamRepository.findAll();
        assertThat(p2pParamList).hasSize(databaseSizeBeforeUpdate);
        P2pParam testP2pParam = p2pParamList.get(p2pParamList.size() - 1);
        assertThat(testP2pParam.getPriority()).isEqualTo(UPDATED_PRIORITY);
        assertThat(testP2pParam.isActive()).isEqualTo(UPDATED_ACTIVE);
        assertThat(testP2pParam.getSenderPc()).isEqualTo(UPDATED_SENDER_PC);
        assertThat(testP2pParam.getRecipientPc()).isEqualTo(UPDATED_RECIPIENT_PC);
        assertThat(testP2pParam.getValue()).isEqualTo(UPDATED_VALUE);
    }

    @Test
    @Transactional
    public void updateNonExistingP2pParam() throws Exception {
        int databaseSizeBeforeUpdate = p2pParamRepository.findAll().size();

        // Create the P2pParam

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restP2pParamMockMvc
            .perform(
                put("/api/p-2-p-params").contentType(TestUtil.APPLICATION_JSON_UTF8).content(TestUtil.convertObjectToJsonBytes(p2pParam))
            )
            .andExpect(status().isBadRequest());

        // Validate the P2pParam in the database
        List<P2pParam> p2pParamList = p2pParamRepository.findAll();
        assertThat(p2pParamList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    public void deleteP2pParam() throws Exception {
        // Initialize the database
        p2pParamService.save(p2pParam);

        int databaseSizeBeforeDelete = p2pParamRepository.findAll().size();

        // Get the p2pParam
        restP2pParamMockMvc
            .perform(delete("/api/p-2-p-params/{id}", p2pParam.getId()).accept(TestUtil.APPLICATION_JSON_UTF8))
            .andExpect(status().isOk());

        // Validate the database is empty
        List<P2pParam> p2pParamList = p2pParamRepository.findAll();
        assertThat(p2pParamList).hasSize(databaseSizeBeforeDelete - 1);
    }

    @Test
    @Transactional
    public void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(P2pParam.class);
        P2pParam p2pParam1 = new P2pParam();
        p2pParam1.setId(1L);
        P2pParam p2pParam2 = new P2pParam();
        p2pParam2.setId(p2pParam1.getId());
        assertThat(p2pParam1).isEqualTo(p2pParam2);
        p2pParam2.setId(2L);
        assertThat(p2pParam1).isNotEqualTo(p2pParam2);
        p2pParam1.setId(null);
        assertThat(p2pParam1).isNotEqualTo(p2pParam2);
    }
}
