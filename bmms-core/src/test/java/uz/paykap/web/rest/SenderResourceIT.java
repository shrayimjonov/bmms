package uz.paykap.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

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
import uz.devops.bmcore.enumeration.PayType;
import uz.paykap.IntegrationTest;
import uz.paykap.domain.Agent;
import uz.paykap.domain.Sender;
import uz.paykap.repository.SenderRepository;
import uz.paykap.service.dto.SenderDTO;
import uz.paykap.service.mapper.SenderMapper;

/**
 * Integration tests for the {@link SenderResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class SenderResourceIT {

    private static final String DEFAULT_NAME = "AAAAAAAAAA";
    private static final String UPDATED_NAME = "BBBBBBBBBB";

    private static final Long DEFAULT_ORD = 1L;
    private static final Long UPDATED_ORD = 2L;
    private static final Long SMALLER_ORD = 1L - 1L;

    private static final Boolean DEFAULT_ACTIVE = false;
    private static final Boolean UPDATED_ACTIVE = true;

    private static final String DEFAULT_SPRING_BEAN_NAME = "AAAAAAAAAA";
    private static final String UPDATED_SPRING_BEAN_NAME = "BBBBBBBBBB";

    private static final PayType DEFAULT_PAY_TYPE = PayType.CASH;
    private static final PayType UPDATED_PAY_TYPE = PayType.UZCARD;

    private static final String ENTITY_API_URL = "/api/senders";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong count = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private SenderRepository senderRepository;

    @Autowired
    private SenderMapper senderMapper;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restSenderMockMvc;

    private Sender sender;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Sender createEntity(EntityManager em) {
        Sender sender = new Sender()
            .name(DEFAULT_NAME)
            .ord(DEFAULT_ORD)
            .active(DEFAULT_ACTIVE)
            .springBeanName(DEFAULT_SPRING_BEAN_NAME)
            .payType(DEFAULT_PAY_TYPE);
        return sender;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Sender createUpdatedEntity(EntityManager em) {
        Sender sender = new Sender()
            .name(UPDATED_NAME)
            .ord(UPDATED_ORD)
            .active(UPDATED_ACTIVE)
            .springBeanName(UPDATED_SPRING_BEAN_NAME)
            .payType(UPDATED_PAY_TYPE);
        return sender;
    }

    @BeforeEach
    public void initTest() {
        sender = createEntity(em);
    }

    @Test
    @Transactional
    void createSender() throws Exception {
        int databaseSizeBeforeCreate = senderRepository.findAll().size();
        // Create the Sender
        SenderDTO senderDTO = senderMapper.toDto(sender);
        restSenderMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(senderDTO)))
            .andExpect(status().isCreated());

        // Validate the Sender in the database
        List<Sender> senderList = senderRepository.findAll();
        assertThat(senderList).hasSize(databaseSizeBeforeCreate + 1);
        Sender testSender = senderList.get(senderList.size() - 1);
        assertThat(testSender.getName()).isEqualTo(DEFAULT_NAME);
        assertThat(testSender.getOrd()).isEqualTo(DEFAULT_ORD);
        assertThat(testSender.getActive()).isEqualTo(DEFAULT_ACTIVE);
        assertThat(testSender.getSpringBeanName()).isEqualTo(DEFAULT_SPRING_BEAN_NAME);
        assertThat(testSender.getPayType()).isEqualTo(DEFAULT_PAY_TYPE);
    }

    @Test
    @Transactional
    void createSenderWithExistingId() throws Exception {
        // Create the Sender with an existing ID
        sender.setId(1L);
        SenderDTO senderDTO = senderMapper.toDto(sender);

        int databaseSizeBeforeCreate = senderRepository.findAll().size();

        // An entity with an existing ID cannot be created, so this API call must fail
        restSenderMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(senderDTO)))
            .andExpect(status().isBadRequest());

        // Validate the Sender in the database
        List<Sender> senderList = senderRepository.findAll();
        assertThat(senderList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkNameIsRequired() throws Exception {
        int databaseSizeBeforeTest = senderRepository.findAll().size();
        // set the field null
        sender.setName(null);

        // Create the Sender, which fails.
        SenderDTO senderDTO = senderMapper.toDto(sender);

        restSenderMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(senderDTO)))
            .andExpect(status().isBadRequest());

        List<Sender> senderList = senderRepository.findAll();
        assertThat(senderList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkOrdIsRequired() throws Exception {
        int databaseSizeBeforeTest = senderRepository.findAll().size();
        // set the field null
        sender.setOrd(null);

        // Create the Sender, which fails.
        SenderDTO senderDTO = senderMapper.toDto(sender);

        restSenderMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(senderDTO)))
            .andExpect(status().isBadRequest());

        List<Sender> senderList = senderRepository.findAll();
        assertThat(senderList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkActiveIsRequired() throws Exception {
        int databaseSizeBeforeTest = senderRepository.findAll().size();
        // set the field null
        sender.setActive(null);

        // Create the Sender, which fails.
        SenderDTO senderDTO = senderMapper.toDto(sender);

        restSenderMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(senderDTO)))
            .andExpect(status().isBadRequest());

        List<Sender> senderList = senderRepository.findAll();
        assertThat(senderList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkPayTypeIsRequired() throws Exception {
        int databaseSizeBeforeTest = senderRepository.findAll().size();
        // set the field null
        sender.setPayType(null);

        // Create the Sender, which fails.
        SenderDTO senderDTO = senderMapper.toDto(sender);

        restSenderMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(senderDTO)))
            .andExpect(status().isBadRequest());

        List<Sender> senderList = senderRepository.findAll();
        assertThat(senderList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllSenders() throws Exception {
        // Initialize the database
        senderRepository.saveAndFlush(sender);

        // Get all the senderList
        restSenderMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(sender.getId().intValue())))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME)))
            .andExpect(jsonPath("$.[*].ord").value(hasItem(DEFAULT_ORD.intValue())))
            .andExpect(jsonPath("$.[*].active").value(hasItem(DEFAULT_ACTIVE.booleanValue())))
            .andExpect(jsonPath("$.[*].springBeanName").value(hasItem(DEFAULT_SPRING_BEAN_NAME)))
            .andExpect(jsonPath("$.[*].payType").value(hasItem(DEFAULT_PAY_TYPE.toString())));
    }

    @Test
    @Transactional
    void getSender() throws Exception {
        // Initialize the database
        senderRepository.saveAndFlush(sender);

        // Get the sender
        restSenderMockMvc
            .perform(get(ENTITY_API_URL_ID, sender.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(sender.getId().intValue()))
            .andExpect(jsonPath("$.name").value(DEFAULT_NAME))
            .andExpect(jsonPath("$.ord").value(DEFAULT_ORD.intValue()))
            .andExpect(jsonPath("$.active").value(DEFAULT_ACTIVE.booleanValue()))
            .andExpect(jsonPath("$.springBeanName").value(DEFAULT_SPRING_BEAN_NAME))
            .andExpect(jsonPath("$.payType").value(DEFAULT_PAY_TYPE.toString()));
    }

    @Test
    @Transactional
    void getSendersByIdFiltering() throws Exception {
        // Initialize the database
        senderRepository.saveAndFlush(sender);

        Long id = sender.getId();

        defaultSenderShouldBeFound("id.equals=" + id);
        defaultSenderShouldNotBeFound("id.notEquals=" + id);

        defaultSenderShouldBeFound("id.greaterThanOrEqual=" + id);
        defaultSenderShouldNotBeFound("id.greaterThan=" + id);

        defaultSenderShouldBeFound("id.lessThanOrEqual=" + id);
        defaultSenderShouldNotBeFound("id.lessThan=" + id);
    }

    @Test
    @Transactional
    void getAllSendersByNameIsEqualToSomething() throws Exception {
        // Initialize the database
        senderRepository.saveAndFlush(sender);

        // Get all the senderList where name equals to DEFAULT_NAME
        defaultSenderShouldBeFound("name.equals=" + DEFAULT_NAME);

        // Get all the senderList where name equals to UPDATED_NAME
        defaultSenderShouldNotBeFound("name.equals=" + UPDATED_NAME);
    }

    @Test
    @Transactional
    void getAllSendersByNameIsNotEqualToSomething() throws Exception {
        // Initialize the database
        senderRepository.saveAndFlush(sender);

        // Get all the senderList where name not equals to DEFAULT_NAME
        defaultSenderShouldNotBeFound("name.notEquals=" + DEFAULT_NAME);

        // Get all the senderList where name not equals to UPDATED_NAME
        defaultSenderShouldBeFound("name.notEquals=" + UPDATED_NAME);
    }

    @Test
    @Transactional
    void getAllSendersByNameIsInShouldWork() throws Exception {
        // Initialize the database
        senderRepository.saveAndFlush(sender);

        // Get all the senderList where name in DEFAULT_NAME or UPDATED_NAME
        defaultSenderShouldBeFound("name.in=" + DEFAULT_NAME + "," + UPDATED_NAME);

        // Get all the senderList where name equals to UPDATED_NAME
        defaultSenderShouldNotBeFound("name.in=" + UPDATED_NAME);
    }

    @Test
    @Transactional
    void getAllSendersByNameIsNullOrNotNull() throws Exception {
        // Initialize the database
        senderRepository.saveAndFlush(sender);

        // Get all the senderList where name is not null
        defaultSenderShouldBeFound("name.specified=true");

        // Get all the senderList where name is null
        defaultSenderShouldNotBeFound("name.specified=false");
    }

    @Test
    @Transactional
    void getAllSendersByNameContainsSomething() throws Exception {
        // Initialize the database
        senderRepository.saveAndFlush(sender);

        // Get all the senderList where name contains DEFAULT_NAME
        defaultSenderShouldBeFound("name.contains=" + DEFAULT_NAME);

        // Get all the senderList where name contains UPDATED_NAME
        defaultSenderShouldNotBeFound("name.contains=" + UPDATED_NAME);
    }

    @Test
    @Transactional
    void getAllSendersByNameNotContainsSomething() throws Exception {
        // Initialize the database
        senderRepository.saveAndFlush(sender);

        // Get all the senderList where name does not contain DEFAULT_NAME
        defaultSenderShouldNotBeFound("name.doesNotContain=" + DEFAULT_NAME);

        // Get all the senderList where name does not contain UPDATED_NAME
        defaultSenderShouldBeFound("name.doesNotContain=" + UPDATED_NAME);
    }

    @Test
    @Transactional
    void getAllSendersByOrdIsEqualToSomething() throws Exception {
        // Initialize the database
        senderRepository.saveAndFlush(sender);

        // Get all the senderList where ord equals to DEFAULT_ORD
        defaultSenderShouldBeFound("ord.equals=" + DEFAULT_ORD);

        // Get all the senderList where ord equals to UPDATED_ORD
        defaultSenderShouldNotBeFound("ord.equals=" + UPDATED_ORD);
    }

    @Test
    @Transactional
    void getAllSendersByOrdIsNotEqualToSomething() throws Exception {
        // Initialize the database
        senderRepository.saveAndFlush(sender);

        // Get all the senderList where ord not equals to DEFAULT_ORD
        defaultSenderShouldNotBeFound("ord.notEquals=" + DEFAULT_ORD);

        // Get all the senderList where ord not equals to UPDATED_ORD
        defaultSenderShouldBeFound("ord.notEquals=" + UPDATED_ORD);
    }

    @Test
    @Transactional
    void getAllSendersByOrdIsInShouldWork() throws Exception {
        // Initialize the database
        senderRepository.saveAndFlush(sender);

        // Get all the senderList where ord in DEFAULT_ORD or UPDATED_ORD
        defaultSenderShouldBeFound("ord.in=" + DEFAULT_ORD + "," + UPDATED_ORD);

        // Get all the senderList where ord equals to UPDATED_ORD
        defaultSenderShouldNotBeFound("ord.in=" + UPDATED_ORD);
    }

    @Test
    @Transactional
    void getAllSendersByOrdIsNullOrNotNull() throws Exception {
        // Initialize the database
        senderRepository.saveAndFlush(sender);

        // Get all the senderList where ord is not null
        defaultSenderShouldBeFound("ord.specified=true");

        // Get all the senderList where ord is null
        defaultSenderShouldNotBeFound("ord.specified=false");
    }

    @Test
    @Transactional
    void getAllSendersByOrdIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        senderRepository.saveAndFlush(sender);

        // Get all the senderList where ord is greater than or equal to DEFAULT_ORD
        defaultSenderShouldBeFound("ord.greaterThanOrEqual=" + DEFAULT_ORD);

        // Get all the senderList where ord is greater than or equal to UPDATED_ORD
        defaultSenderShouldNotBeFound("ord.greaterThanOrEqual=" + UPDATED_ORD);
    }

    @Test
    @Transactional
    void getAllSendersByOrdIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        senderRepository.saveAndFlush(sender);

        // Get all the senderList where ord is less than or equal to DEFAULT_ORD
        defaultSenderShouldBeFound("ord.lessThanOrEqual=" + DEFAULT_ORD);

        // Get all the senderList where ord is less than or equal to SMALLER_ORD
        defaultSenderShouldNotBeFound("ord.lessThanOrEqual=" + SMALLER_ORD);
    }

    @Test
    @Transactional
    void getAllSendersByOrdIsLessThanSomething() throws Exception {
        // Initialize the database
        senderRepository.saveAndFlush(sender);

        // Get all the senderList where ord is less than DEFAULT_ORD
        defaultSenderShouldNotBeFound("ord.lessThan=" + DEFAULT_ORD);

        // Get all the senderList where ord is less than UPDATED_ORD
        defaultSenderShouldBeFound("ord.lessThan=" + UPDATED_ORD);
    }

    @Test
    @Transactional
    void getAllSendersByOrdIsGreaterThanSomething() throws Exception {
        // Initialize the database
        senderRepository.saveAndFlush(sender);

        // Get all the senderList where ord is greater than DEFAULT_ORD
        defaultSenderShouldNotBeFound("ord.greaterThan=" + DEFAULT_ORD);

        // Get all the senderList where ord is greater than SMALLER_ORD
        defaultSenderShouldBeFound("ord.greaterThan=" + SMALLER_ORD);
    }

    @Test
    @Transactional
    void getAllSendersByActiveIsEqualToSomething() throws Exception {
        // Initialize the database
        senderRepository.saveAndFlush(sender);

        // Get all the senderList where active equals to DEFAULT_ACTIVE
        defaultSenderShouldBeFound("active.equals=" + DEFAULT_ACTIVE);

        // Get all the senderList where active equals to UPDATED_ACTIVE
        defaultSenderShouldNotBeFound("active.equals=" + UPDATED_ACTIVE);
    }

    @Test
    @Transactional
    void getAllSendersByActiveIsNotEqualToSomething() throws Exception {
        // Initialize the database
        senderRepository.saveAndFlush(sender);

        // Get all the senderList where active not equals to DEFAULT_ACTIVE
        defaultSenderShouldNotBeFound("active.notEquals=" + DEFAULT_ACTIVE);

        // Get all the senderList where active not equals to UPDATED_ACTIVE
        defaultSenderShouldBeFound("active.notEquals=" + UPDATED_ACTIVE);
    }

    @Test
    @Transactional
    void getAllSendersByActiveIsInShouldWork() throws Exception {
        // Initialize the database
        senderRepository.saveAndFlush(sender);

        // Get all the senderList where active in DEFAULT_ACTIVE or UPDATED_ACTIVE
        defaultSenderShouldBeFound("active.in=" + DEFAULT_ACTIVE + "," + UPDATED_ACTIVE);

        // Get all the senderList where active equals to UPDATED_ACTIVE
        defaultSenderShouldNotBeFound("active.in=" + UPDATED_ACTIVE);
    }

    @Test
    @Transactional
    void getAllSendersByActiveIsNullOrNotNull() throws Exception {
        // Initialize the database
        senderRepository.saveAndFlush(sender);

        // Get all the senderList where active is not null
        defaultSenderShouldBeFound("active.specified=true");

        // Get all the senderList where active is null
        defaultSenderShouldNotBeFound("active.specified=false");
    }

    @Test
    @Transactional
    void getAllSendersBySpringBeanNameIsEqualToSomething() throws Exception {
        // Initialize the database
        senderRepository.saveAndFlush(sender);

        // Get all the senderList where springBeanName equals to DEFAULT_SPRING_BEAN_NAME
        defaultSenderShouldBeFound("springBeanName.equals=" + DEFAULT_SPRING_BEAN_NAME);

        // Get all the senderList where springBeanName equals to UPDATED_SPRING_BEAN_NAME
        defaultSenderShouldNotBeFound("springBeanName.equals=" + UPDATED_SPRING_BEAN_NAME);
    }

    @Test
    @Transactional
    void getAllSendersBySpringBeanNameIsNotEqualToSomething() throws Exception {
        // Initialize the database
        senderRepository.saveAndFlush(sender);

        // Get all the senderList where springBeanName not equals to DEFAULT_SPRING_BEAN_NAME
        defaultSenderShouldNotBeFound("springBeanName.notEquals=" + DEFAULT_SPRING_BEAN_NAME);

        // Get all the senderList where springBeanName not equals to UPDATED_SPRING_BEAN_NAME
        defaultSenderShouldBeFound("springBeanName.notEquals=" + UPDATED_SPRING_BEAN_NAME);
    }

    @Test
    @Transactional
    void getAllSendersBySpringBeanNameIsInShouldWork() throws Exception {
        // Initialize the database
        senderRepository.saveAndFlush(sender);

        // Get all the senderList where springBeanName in DEFAULT_SPRING_BEAN_NAME or UPDATED_SPRING_BEAN_NAME
        defaultSenderShouldBeFound("springBeanName.in=" + DEFAULT_SPRING_BEAN_NAME + "," + UPDATED_SPRING_BEAN_NAME);

        // Get all the senderList where springBeanName equals to UPDATED_SPRING_BEAN_NAME
        defaultSenderShouldNotBeFound("springBeanName.in=" + UPDATED_SPRING_BEAN_NAME);
    }

    @Test
    @Transactional
    void getAllSendersBySpringBeanNameIsNullOrNotNull() throws Exception {
        // Initialize the database
        senderRepository.saveAndFlush(sender);

        // Get all the senderList where springBeanName is not null
        defaultSenderShouldBeFound("springBeanName.specified=true");

        // Get all the senderList where springBeanName is null
        defaultSenderShouldNotBeFound("springBeanName.specified=false");
    }

    @Test
    @Transactional
    void getAllSendersBySpringBeanNameContainsSomething() throws Exception {
        // Initialize the database
        senderRepository.saveAndFlush(sender);

        // Get all the senderList where springBeanName contains DEFAULT_SPRING_BEAN_NAME
        defaultSenderShouldBeFound("springBeanName.contains=" + DEFAULT_SPRING_BEAN_NAME);

        // Get all the senderList where springBeanName contains UPDATED_SPRING_BEAN_NAME
        defaultSenderShouldNotBeFound("springBeanName.contains=" + UPDATED_SPRING_BEAN_NAME);
    }

    @Test
    @Transactional
    void getAllSendersBySpringBeanNameNotContainsSomething() throws Exception {
        // Initialize the database
        senderRepository.saveAndFlush(sender);

        // Get all the senderList where springBeanName does not contain DEFAULT_SPRING_BEAN_NAME
        defaultSenderShouldNotBeFound("springBeanName.doesNotContain=" + DEFAULT_SPRING_BEAN_NAME);

        // Get all the senderList where springBeanName does not contain UPDATED_SPRING_BEAN_NAME
        defaultSenderShouldBeFound("springBeanName.doesNotContain=" + UPDATED_SPRING_BEAN_NAME);
    }

    @Test
    @Transactional
    void getAllSendersByPayTypeIsEqualToSomething() throws Exception {
        // Initialize the database
        senderRepository.saveAndFlush(sender);

        // Get all the senderList where payType equals to DEFAULT_PAY_TYPE
        defaultSenderShouldBeFound("payType.equals=" + DEFAULT_PAY_TYPE);

        // Get all the senderList where payType equals to UPDATED_PAY_TYPE
        defaultSenderShouldNotBeFound("payType.equals=" + UPDATED_PAY_TYPE);
    }

    @Test
    @Transactional
    void getAllSendersByPayTypeIsNotEqualToSomething() throws Exception {
        // Initialize the database
        senderRepository.saveAndFlush(sender);

        // Get all the senderList where payType not equals to DEFAULT_PAY_TYPE
        defaultSenderShouldNotBeFound("payType.notEquals=" + DEFAULT_PAY_TYPE);

        // Get all the senderList where payType not equals to UPDATED_PAY_TYPE
        defaultSenderShouldBeFound("payType.notEquals=" + UPDATED_PAY_TYPE);
    }

    @Test
    @Transactional
    void getAllSendersByPayTypeIsInShouldWork() throws Exception {
        // Initialize the database
        senderRepository.saveAndFlush(sender);

        // Get all the senderList where payType in DEFAULT_PAY_TYPE or UPDATED_PAY_TYPE
        defaultSenderShouldBeFound("payType.in=" + DEFAULT_PAY_TYPE + "," + UPDATED_PAY_TYPE);

        // Get all the senderList where payType equals to UPDATED_PAY_TYPE
        defaultSenderShouldNotBeFound("payType.in=" + UPDATED_PAY_TYPE);
    }

    @Test
    @Transactional
    void getAllSendersByPayTypeIsNullOrNotNull() throws Exception {
        // Initialize the database
        senderRepository.saveAndFlush(sender);

        // Get all the senderList where payType is not null
        defaultSenderShouldBeFound("payType.specified=true");

        // Get all the senderList where payType is null
        defaultSenderShouldNotBeFound("payType.specified=false");
    }

    @Test
    @Transactional
    void getAllSendersByAgentIsEqualToSomething() throws Exception {
        // Initialize the database
        senderRepository.saveAndFlush(sender);
        Agent agent = AgentResourceIT.createEntity(em);
        em.persist(agent);
        em.flush();
        sender.setAgent(agent);
        senderRepository.saveAndFlush(sender);
        Long agentId = agent.getId();

        // Get all the senderList where agent equals to agentId
        defaultSenderShouldBeFound("agentId.equals=" + agentId);

        // Get all the senderList where agent equals to (agentId + 1)
        defaultSenderShouldNotBeFound("agentId.equals=" + (agentId + 1));
    }

    /**
     * Executes the search, and checks that the default entity is returned.
     */
    private void defaultSenderShouldBeFound(String filter) throws Exception {
        restSenderMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(sender.getId().intValue())))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME)))
            .andExpect(jsonPath("$.[*].ord").value(hasItem(DEFAULT_ORD.intValue())))
            .andExpect(jsonPath("$.[*].active").value(hasItem(DEFAULT_ACTIVE.booleanValue())))
            .andExpect(jsonPath("$.[*].springBeanName").value(hasItem(DEFAULT_SPRING_BEAN_NAME)))
            .andExpect(jsonPath("$.[*].payType").value(hasItem(DEFAULT_PAY_TYPE.toString())));

        // Check, that the count call also returns 1
        restSenderMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("1"));
    }

    /**
     * Executes the search, and checks that the default entity is not returned.
     */
    private void defaultSenderShouldNotBeFound(String filter) throws Exception {
        restSenderMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty());

        // Check, that the count call also returns 0
        restSenderMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("0"));
    }

    @Test
    @Transactional
    void getNonExistingSender() throws Exception {
        // Get the sender
        restSenderMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putNewSender() throws Exception {
        // Initialize the database
        senderRepository.saveAndFlush(sender);

        int databaseSizeBeforeUpdate = senderRepository.findAll().size();

        // Update the sender
        Sender updatedSender = senderRepository.findById(sender.getId()).get();
        // Disconnect from session so that the updates on updatedSender are not directly saved in db
        em.detach(updatedSender);
        updatedSender
            .name(UPDATED_NAME)
            .ord(UPDATED_ORD)
            .active(UPDATED_ACTIVE)
            .springBeanName(UPDATED_SPRING_BEAN_NAME)
            .payType(UPDATED_PAY_TYPE);
        SenderDTO senderDTO = senderMapper.toDto(updatedSender);

        restSenderMockMvc
            .perform(
                put(ENTITY_API_URL_ID, senderDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(senderDTO))
            )
            .andExpect(status().isOk());

        // Validate the Sender in the database
        List<Sender> senderList = senderRepository.findAll();
        assertThat(senderList).hasSize(databaseSizeBeforeUpdate);
        Sender testSender = senderList.get(senderList.size() - 1);
        assertThat(testSender.getName()).isEqualTo(UPDATED_NAME);
        assertThat(testSender.getOrd()).isEqualTo(UPDATED_ORD);
        assertThat(testSender.getActive()).isEqualTo(UPDATED_ACTIVE);
        assertThat(testSender.getSpringBeanName()).isEqualTo(UPDATED_SPRING_BEAN_NAME);
        assertThat(testSender.getPayType()).isEqualTo(UPDATED_PAY_TYPE);
    }

    @Test
    @Transactional
    void putNonExistingSender() throws Exception {
        int databaseSizeBeforeUpdate = senderRepository.findAll().size();
        sender.setId(count.incrementAndGet());

        // Create the Sender
        SenderDTO senderDTO = senderMapper.toDto(sender);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restSenderMockMvc
            .perform(
                put(ENTITY_API_URL_ID, senderDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(senderDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Sender in the database
        List<Sender> senderList = senderRepository.findAll();
        assertThat(senderList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchSender() throws Exception {
        int databaseSizeBeforeUpdate = senderRepository.findAll().size();
        sender.setId(count.incrementAndGet());

        // Create the Sender
        SenderDTO senderDTO = senderMapper.toDto(sender);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restSenderMockMvc
            .perform(
                put(ENTITY_API_URL_ID, count.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(senderDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Sender in the database
        List<Sender> senderList = senderRepository.findAll();
        assertThat(senderList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamSender() throws Exception {
        int databaseSizeBeforeUpdate = senderRepository.findAll().size();
        sender.setId(count.incrementAndGet());

        // Create the Sender
        SenderDTO senderDTO = senderMapper.toDto(sender);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restSenderMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(senderDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Sender in the database
        List<Sender> senderList = senderRepository.findAll();
        assertThat(senderList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateSenderWithPatch() throws Exception {
        // Initialize the database
        senderRepository.saveAndFlush(sender);

        int databaseSizeBeforeUpdate = senderRepository.findAll().size();

        // Update the sender using partial update
        Sender partialUpdatedSender = new Sender();
        partialUpdatedSender.setId(sender.getId());

        partialUpdatedSender.ord(UPDATED_ORD).active(UPDATED_ACTIVE);

        restSenderMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedSender.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedSender))
            )
            .andExpect(status().isOk());

        // Validate the Sender in the database
        List<Sender> senderList = senderRepository.findAll();
        assertThat(senderList).hasSize(databaseSizeBeforeUpdate);
        Sender testSender = senderList.get(senderList.size() - 1);
        assertThat(testSender.getName()).isEqualTo(DEFAULT_NAME);
        assertThat(testSender.getOrd()).isEqualTo(UPDATED_ORD);
        assertThat(testSender.getActive()).isEqualTo(UPDATED_ACTIVE);
        assertThat(testSender.getSpringBeanName()).isEqualTo(DEFAULT_SPRING_BEAN_NAME);
        assertThat(testSender.getPayType()).isEqualTo(DEFAULT_PAY_TYPE);
    }

    @Test
    @Transactional
    void fullUpdateSenderWithPatch() throws Exception {
        // Initialize the database
        senderRepository.saveAndFlush(sender);

        int databaseSizeBeforeUpdate = senderRepository.findAll().size();

        // Update the sender using partial update
        Sender partialUpdatedSender = new Sender();
        partialUpdatedSender.setId(sender.getId());

        partialUpdatedSender
            .name(UPDATED_NAME)
            .ord(UPDATED_ORD)
            .active(UPDATED_ACTIVE)
            .springBeanName(UPDATED_SPRING_BEAN_NAME)
            .payType(UPDATED_PAY_TYPE);

        restSenderMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedSender.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedSender))
            )
            .andExpect(status().isOk());

        // Validate the Sender in the database
        List<Sender> senderList = senderRepository.findAll();
        assertThat(senderList).hasSize(databaseSizeBeforeUpdate);
        Sender testSender = senderList.get(senderList.size() - 1);
        assertThat(testSender.getName()).isEqualTo(UPDATED_NAME);
        assertThat(testSender.getOrd()).isEqualTo(UPDATED_ORD);
        assertThat(testSender.getActive()).isEqualTo(UPDATED_ACTIVE);
        assertThat(testSender.getSpringBeanName()).isEqualTo(UPDATED_SPRING_BEAN_NAME);
        assertThat(testSender.getPayType()).isEqualTo(UPDATED_PAY_TYPE);
    }

    @Test
    @Transactional
    void patchNonExistingSender() throws Exception {
        int databaseSizeBeforeUpdate = senderRepository.findAll().size();
        sender.setId(count.incrementAndGet());

        // Create the Sender
        SenderDTO senderDTO = senderMapper.toDto(sender);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restSenderMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, senderDTO.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(senderDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Sender in the database
        List<Sender> senderList = senderRepository.findAll();
        assertThat(senderList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchSender() throws Exception {
        int databaseSizeBeforeUpdate = senderRepository.findAll().size();
        sender.setId(count.incrementAndGet());

        // Create the Sender
        SenderDTO senderDTO = senderMapper.toDto(sender);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restSenderMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, count.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(senderDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Sender in the database
        List<Sender> senderList = senderRepository.findAll();
        assertThat(senderList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamSender() throws Exception {
        int databaseSizeBeforeUpdate = senderRepository.findAll().size();
        sender.setId(count.incrementAndGet());

        // Create the Sender
        SenderDTO senderDTO = senderMapper.toDto(sender);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restSenderMockMvc
            .perform(
                patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(TestUtil.convertObjectToJsonBytes(senderDTO))
            )
            .andExpect(status().isMethodNotAllowed());

        // Validate the Sender in the database
        List<Sender> senderList = senderRepository.findAll();
        assertThat(senderList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteSender() throws Exception {
        // Initialize the database
        senderRepository.saveAndFlush(sender);

        int databaseSizeBeforeDelete = senderRepository.findAll().size();

        // Delete the sender
        restSenderMockMvc
            .perform(delete(ENTITY_API_URL_ID, sender.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        List<Sender> senderList = senderRepository.findAll();
        assertThat(senderList).hasSize(databaseSizeBeforeDelete - 1);
    }
}
