package uz.paykap.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicLong;
import javax.persistence.EntityManager;
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
import org.springframework.util.Base64Utils;
import uz.paykap.IntegrationTest;
import uz.paykap.domain.Agent;
import uz.paykap.domain.Deposit;
import uz.paykap.domain.I18n;
import uz.paykap.domain.enumeration.AgentState;
import uz.paykap.repository.AgentRepository;
import uz.paykap.service.AgentService;
import uz.paykap.service.criteria.AgentCriteria;
import uz.paykap.service.dto.AgentDTO;
import uz.paykap.service.mapper.AgentMapper;

/**
 * Integration tests for the {@link AgentResource} REST controller.
 */
@IntegrationTest
@ExtendWith(MockitoExtension.class)
@AutoConfigureMockMvc
@WithMockUser
class AgentResourceIT {

    private static final String DEFAULT_SHORT_NAME = "AAAAAAAAAA";
    private static final String UPDATED_SHORT_NAME = "BBBBBBBBBB";

    private static final Integer DEFAULT_AMOUNT_RATIO = 1;
    private static final Integer UPDATED_AMOUNT_RATIO = 2;
    private static final Integer SMALLER_AMOUNT_RATIO = 1 - 1;

    private static final String DEFAULT_API_KEY = "AAAAAAAAAA";
    private static final String UPDATED_API_KEY = "BBBBBBBBBB";

    private static final Set<Integer> DEFAULT_CURRENCIES = new HashSet<>() {
        {
            add(860);
        }
    };
    private static final Set<Integer> UPDATED_CURRENCIES = new HashSet<>() {
        {
            add(840);
        }
    };

    private static final Set<String> DEFAULT_IP_ADDRESSES = new HashSet<>() {
        {
            add("AAAAAAAAAA");
        }
    };
    private static final Set<String> UPDATED_IP_ADDRESSES = new HashSet<>() {
        {
            add("BBBBBBBBBB");
        }
    };

    private static final byte[] DEFAULT_LOGO = TestUtil.createByteArray(1, "0");
    private static final byte[] UPDATED_LOGO = TestUtil.createByteArray(1, "1");
    private static final String DEFAULT_LOGO_CONTENT_TYPE = "image/jpg";
    private static final String UPDATED_LOGO_CONTENT_TYPE = "image/png";

    private static final Integer DEFAULT_ORD = 1;
    private static final Integer UPDATED_ORD = 2;
    private static final Integer SMALLER_ORD = 1 - 1;

    private static final Boolean DEFAULT_ACTIVE = false;
    private static final Boolean UPDATED_ACTIVE = true;

    private static final String DEFAULT_PHONE_NUMBER = "AAAAAAAAAA";
    private static final String UPDATED_PHONE_NUMBER = "BBBBBBBBBB";

    private static final String DEFAULT_INN = "AAAAAAAAAA";
    private static final String UPDATED_INN = "BBBBBBBBBB";

    private static final String DEFAULT_ORG_NAME = "AAAAAAAAAA";
    private static final String UPDATED_ORG_NAME = "BBBBBBBBBB";

    private static final String ENTITY_API_URL = "/api/agents";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong count = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private AgentRepository agentRepository;

    @Mock
    private AgentRepository agentRepositoryMock;

    @Autowired
    private AgentMapper agentMapper;

    @Mock
    private AgentService agentServiceMock;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restAgentMockMvc;

    private Agent agent;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Agent createEntity(EntityManager em) {
        Agent agent = new Agent()
            .shortName(DEFAULT_SHORT_NAME)
            .amountRatio(DEFAULT_AMOUNT_RATIO)
            .apiKey(DEFAULT_API_KEY)
            .currencies(DEFAULT_CURRENCIES)
            .ipAddresses(DEFAULT_IP_ADDRESSES)
            .logo(DEFAULT_LOGO)
            .logoContentType(DEFAULT_LOGO_CONTENT_TYPE)
            .ord(DEFAULT_ORD)
            .active(DEFAULT_ACTIVE)
            .phoneNumber(DEFAULT_PHONE_NUMBER)
            .inn(DEFAULT_INN)
            .orgName(DEFAULT_ORG_NAME);
        // Add required entity
        I18n i18n;
        if (TestUtil.findAll(em, I18n.class).isEmpty()) {
            i18n = I18nResourceIT.createEntity(em);
            //            em.persist(i18n);
            //            em.flush();
        } else {
            i18n = TestUtil.findAll(em, I18n.class).get(0);
        }
        agent.getTitles().add(i18n);
        return agent;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Agent createUpdatedEntity(EntityManager em) {
        Agent agent = new Agent()
            .shortName(UPDATED_SHORT_NAME)
            .amountRatio(UPDATED_AMOUNT_RATIO)
            .apiKey(UPDATED_API_KEY)
            .currencies(UPDATED_CURRENCIES)
            .ipAddresses(UPDATED_IP_ADDRESSES)
            .logo(UPDATED_LOGO)
            .logoContentType(UPDATED_LOGO_CONTENT_TYPE)
            .ord(UPDATED_ORD)
            .active(UPDATED_ACTIVE)
            .phoneNumber(UPDATED_PHONE_NUMBER)
            .inn(UPDATED_INN)
            .orgName(UPDATED_ORG_NAME);
        // Add required entity
        I18n i18n;
        if (TestUtil.findAll(em, I18n.class).isEmpty()) {
            i18n = I18nResourceIT.createUpdatedEntity(em);
            //            em.persist(i18n);
            //            em.flush();
        } else {
            i18n = TestUtil.findAll(em, I18n.class).get(0);
        }
        agent.getTitles().add(i18n);
        return agent;
    }

    @BeforeEach
    public void initTest() {
        agent = createEntity(em);
    }

    @Test
    @Transactional
    void createAgent() throws Exception {
        int databaseSizeBeforeCreate = agentRepository.findAll().size();
        // Create the Agent
        AgentDTO agentDTO = agentMapper.toDto(agent);
        restAgentMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(agentDTO)))
            .andExpect(status().isCreated());

        // Validate the Agent in the database
        List<Agent> agentList = agentRepository.findAll();
        assertThat(agentList).hasSize(databaseSizeBeforeCreate + 1);
        Agent testAgent = agentList.get(agentList.size() - 1);
        assertThat(testAgent.getShortName()).isEqualTo(DEFAULT_SHORT_NAME);
        assertThat(testAgent.getAmountRatio()).isEqualTo(DEFAULT_AMOUNT_RATIO);
        assertThat(testAgent.getApiKey()).isEqualTo(DEFAULT_API_KEY);
        assertThat(testAgent.getCurrencies()).isEqualTo(DEFAULT_CURRENCIES);
        assertThat(testAgent.getIpAddresses()).isEqualTo(DEFAULT_IP_ADDRESSES);
        assertThat(testAgent.getLogo()).isEqualTo(DEFAULT_LOGO);
        assertThat(testAgent.getLogoContentType()).isEqualTo(DEFAULT_LOGO_CONTENT_TYPE);
        assertThat(testAgent.getOrd()).isEqualTo(DEFAULT_ORD);
        assertThat(testAgent.getActive()).isEqualTo(DEFAULT_ACTIVE);
        assertThat(testAgent.getPhoneNumber()).isEqualTo(DEFAULT_PHONE_NUMBER);
        assertThat(testAgent.getInn()).isEqualTo(DEFAULT_INN);
        assertThat(testAgent.getOrgName()).isEqualTo(DEFAULT_ORG_NAME);
    }

    @Test
    @Transactional
    void createAgentWithExistingId() throws Exception {
        // Create the Agent with an existing ID
        agent.setId(1L);
        AgentDTO agentDTO = agentMapper.toDto(agent);

        int databaseSizeBeforeCreate = agentRepository.findAll().size();

        // An entity with an existing ID cannot be created, so this API call must fail
        restAgentMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(agentDTO)))
            .andExpect(status().isBadRequest());

        // Validate the Agent in the database
        List<Agent> agentList = agentRepository.findAll();
        assertThat(agentList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkShortNameIsRequired() throws Exception {
        int databaseSizeBeforeTest = agentRepository.findAll().size();
        // set the field null
        agent.setShortName(null);

        // Create the Agent, which fails.
        AgentDTO agentDTO = agentMapper.toDto(agent);

        restAgentMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(agentDTO)))
            .andExpect(status().isBadRequest());

        List<Agent> agentList = agentRepository.findAll();
        assertThat(agentList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkAmountRatioIsRequired() throws Exception {
        int databaseSizeBeforeTest = agentRepository.findAll().size();
        // set the field null
        agent.setAmountRatio(null);

        // Create the Agent, which fails.
        AgentDTO agentDTO = agentMapper.toDto(agent);

        restAgentMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(agentDTO)))
            .andExpect(status().isBadRequest());

        List<Agent> agentList = agentRepository.findAll();
        assertThat(agentList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkApiKeyIsRequired() throws Exception {
        int databaseSizeBeforeTest = agentRepository.findAll().size();
        // set the field null
        agent.setApiKey(null);

        // Create the Agent, which fails.
        AgentDTO agentDTO = agentMapper.toDto(agent);

        restAgentMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(agentDTO)))
            .andExpect(status().isBadRequest());

        List<Agent> agentList = agentRepository.findAll();
        assertThat(agentList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkCurrenciesIsRequired() throws Exception {
        int databaseSizeBeforeTest = agentRepository.findAll().size();
        // set the field null
        agent.setCurrencies(null);

        // Create the Agent, which fails.
        AgentDTO agentDTO = agentMapper.toDto(agent);

        restAgentMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(agentDTO)))
            .andExpect(status().isBadRequest());

        List<Agent> agentList = agentRepository.findAll();
        assertThat(agentList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkActiveIsRequired() throws Exception {
        int databaseSizeBeforeTest = agentRepository.findAll().size();
        // set the field null
        agent.setActive(null);

        // Create the Agent, which fails.
        AgentDTO agentDTO = agentMapper.toDto(agent);

        restAgentMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(agentDTO)))
            .andExpect(status().isBadRequest());

        List<Agent> agentList = agentRepository.findAll();
        assertThat(agentList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllAgents() throws Exception {
        // Initialize the database
        agentRepository.saveAndFlush(agent);

        // Get all the agentList
        restAgentMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(agent.getId().intValue())))
            .andExpect(jsonPath("$.[*].shortName").value(hasItem(DEFAULT_SHORT_NAME)))
            .andExpect(jsonPath("$.[*].amountRatio").value(hasItem(DEFAULT_AMOUNT_RATIO)))
            .andExpect(jsonPath("$.[*].apiKey").value(hasItem(DEFAULT_API_KEY)))
            .andExpect(jsonPath("$.[*].currencies[0]").value(hasItem(DEFAULT_CURRENCIES.toArray()[0])))
            .andExpect(jsonPath("$.[*].ipAddresses[0]").value(hasItem(DEFAULT_IP_ADDRESSES.toArray()[0])))
            .andExpect(jsonPath("$.[*].logoContentType").value(hasItem(DEFAULT_LOGO_CONTENT_TYPE)))
            .andExpect(jsonPath("$.[*].logo").value(hasItem(Base64Utils.encodeToString(DEFAULT_LOGO))))
            .andExpect(jsonPath("$.[*].ord").value(hasItem(DEFAULT_ORD)))
            .andExpect(jsonPath("$.[*].active").value(hasItem(DEFAULT_ACTIVE.booleanValue())))
            .andExpect(jsonPath("$.[*].phoneNumber").value(hasItem(DEFAULT_PHONE_NUMBER)))
            .andExpect(jsonPath("$.[*].inn").value(hasItem(DEFAULT_INN)))
            .andExpect(jsonPath("$.[*].orgName").value(hasItem(DEFAULT_ORG_NAME)));
    }

    @SuppressWarnings({ "unchecked" })
    void getAllAgentsWithEagerRelationshipsIsEnabled() throws Exception {
        when(agentServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restAgentMockMvc.perform(get(ENTITY_API_URL + "?eagerload=true")).andExpect(status().isOk());

        verify(agentServiceMock, times(1)).findAllWithEagerRelationships(any());
    }

    @SuppressWarnings({ "unchecked" })
    void getAllAgentsWithEagerRelationshipsIsNotEnabled() throws Exception {
        when(agentServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restAgentMockMvc.perform(get(ENTITY_API_URL + "?eagerload=true")).andExpect(status().isOk());

        verify(agentServiceMock, times(1)).findAllWithEagerRelationships(any());
    }

    @Test
    @Transactional
    void getAgent() throws Exception {
        // Initialize the database
        agentRepository.saveAndFlush(agent);

        // Get the agent
        restAgentMockMvc
            .perform(get(ENTITY_API_URL_ID, agent.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(agent.getId().intValue()))
            .andExpect(jsonPath("$.shortName").value(DEFAULT_SHORT_NAME))
            .andExpect(jsonPath("$.amountRatio").value(DEFAULT_AMOUNT_RATIO))
            .andExpect(jsonPath("$.apiKey").value(DEFAULT_API_KEY))
            .andExpect(jsonPath("$.currencies[0]").value(DEFAULT_CURRENCIES.toArray()[0]))
            .andExpect(jsonPath("$.ipAddresses[0]").value(DEFAULT_IP_ADDRESSES.toArray()[0]))
            .andExpect(jsonPath("$.logoContentType").value(DEFAULT_LOGO_CONTENT_TYPE))
            .andExpect(jsonPath("$.logo").value(Base64Utils.encodeToString(DEFAULT_LOGO)))
            .andExpect(jsonPath("$.ord").value(DEFAULT_ORD))
            .andExpect(jsonPath("$.active").value(DEFAULT_ACTIVE.booleanValue()))
            .andExpect(jsonPath("$.phoneNumber").value(DEFAULT_PHONE_NUMBER))
            .andExpect(jsonPath("$.inn").value(DEFAULT_INN))
            .andExpect(jsonPath("$.orgName").value(DEFAULT_ORG_NAME));
    }

    @Test
    @Transactional
    void getAgentsByIdFiltering() throws Exception {
        // Initialize the database
        agentRepository.saveAndFlush(agent);

        Long id = agent.getId();

        defaultAgentShouldBeFound("id.equals=" + id);
        defaultAgentShouldNotBeFound("id.notEquals=" + id);

        defaultAgentShouldBeFound("id.greaterThanOrEqual=" + id);
        defaultAgentShouldNotBeFound("id.greaterThan=" + id);

        defaultAgentShouldBeFound("id.lessThanOrEqual=" + id);
        defaultAgentShouldNotBeFound("id.lessThan=" + id);
    }

    @Test
    @Transactional
    void getAllAgentsByShortNameIsEqualToSomething() throws Exception {
        // Initialize the database
        agentRepository.saveAndFlush(agent);

        // Get all the agentList where shortName equals to DEFAULT_SHORT_NAME
        defaultAgentShouldBeFound("shortName.equals=" + DEFAULT_SHORT_NAME);

        // Get all the agentList where shortName equals to UPDATED_SHORT_NAME
        defaultAgentShouldNotBeFound("shortName.equals=" + UPDATED_SHORT_NAME);
    }

    @Test
    @Transactional
    void getAllAgentsByShortNameIsNotEqualToSomething() throws Exception {
        // Initialize the database
        agentRepository.saveAndFlush(agent);

        // Get all the agentList where shortName not equals to DEFAULT_SHORT_NAME
        defaultAgentShouldNotBeFound("shortName.notEquals=" + DEFAULT_SHORT_NAME);

        // Get all the agentList where shortName not equals to UPDATED_SHORT_NAME
        defaultAgentShouldBeFound("shortName.notEquals=" + UPDATED_SHORT_NAME);
    }

    @Test
    @Transactional
    void getAllAgentsByShortNameIsInShouldWork() throws Exception {
        // Initialize the database
        agentRepository.saveAndFlush(agent);

        // Get all the agentList where shortName in DEFAULT_SHORT_NAME or UPDATED_SHORT_NAME
        defaultAgentShouldBeFound("shortName.in=" + DEFAULT_SHORT_NAME + "," + UPDATED_SHORT_NAME);

        // Get all the agentList where shortName equals to UPDATED_SHORT_NAME
        defaultAgentShouldNotBeFound("shortName.in=" + UPDATED_SHORT_NAME);
    }

    @Test
    @Transactional
    void getAllAgentsByShortNameIsNullOrNotNull() throws Exception {
        // Initialize the database
        agentRepository.saveAndFlush(agent);

        // Get all the agentList where shortName is not null
        defaultAgentShouldBeFound("shortName.specified=true");

        // Get all the agentList where shortName is null
        defaultAgentShouldNotBeFound("shortName.specified=false");
    }

    @Test
    @Transactional
    void getAllAgentsByShortNameContainsSomething() throws Exception {
        // Initialize the database
        agentRepository.saveAndFlush(agent);

        // Get all the agentList where shortName contains DEFAULT_SHORT_NAME
        defaultAgentShouldBeFound("shortName.contains=" + DEFAULT_SHORT_NAME);

        // Get all the agentList where shortName contains UPDATED_SHORT_NAME
        defaultAgentShouldNotBeFound("shortName.contains=" + UPDATED_SHORT_NAME);
    }

    @Test
    @Transactional
    void getAllAgentsByShortNameNotContainsSomething() throws Exception {
        // Initialize the database
        agentRepository.saveAndFlush(agent);

        // Get all the agentList where shortName does not contain DEFAULT_SHORT_NAME
        defaultAgentShouldNotBeFound("shortName.doesNotContain=" + DEFAULT_SHORT_NAME);

        // Get all the agentList where shortName does not contain UPDATED_SHORT_NAME
        defaultAgentShouldBeFound("shortName.doesNotContain=" + UPDATED_SHORT_NAME);
    }

    @Test
    @Transactional
    void getAllAgentsByAmountRatioIsEqualToSomething() throws Exception {
        // Initialize the database
        agentRepository.saveAndFlush(agent);

        // Get all the agentList where amountRatio equals to DEFAULT_AMOUNT_RATIO
        defaultAgentShouldBeFound("amountRatio.equals=" + DEFAULT_AMOUNT_RATIO);

        // Get all the agentList where amountRatio equals to UPDATED_AMOUNT_RATIO
        defaultAgentShouldNotBeFound("amountRatio.equals=" + UPDATED_AMOUNT_RATIO);
    }

    @Test
    @Transactional
    void getAllAgentsByAmountRatioIsNotEqualToSomething() throws Exception {
        // Initialize the database
        agentRepository.saveAndFlush(agent);

        // Get all the agentList where amountRatio not equals to DEFAULT_AMOUNT_RATIO
        defaultAgentShouldNotBeFound("amountRatio.notEquals=" + DEFAULT_AMOUNT_RATIO);

        // Get all the agentList where amountRatio not equals to UPDATED_AMOUNT_RATIO
        defaultAgentShouldBeFound("amountRatio.notEquals=" + UPDATED_AMOUNT_RATIO);
    }

    @Test
    @Transactional
    void getAllAgentsByAmountRatioIsInShouldWork() throws Exception {
        // Initialize the database
        agentRepository.saveAndFlush(agent);

        // Get all the agentList where amountRatio in DEFAULT_AMOUNT_RATIO or UPDATED_AMOUNT_RATIO
        defaultAgentShouldBeFound("amountRatio.in=" + DEFAULT_AMOUNT_RATIO + "," + UPDATED_AMOUNT_RATIO);

        // Get all the agentList where amountRatio equals to UPDATED_AMOUNT_RATIO
        defaultAgentShouldNotBeFound("amountRatio.in=" + UPDATED_AMOUNT_RATIO);
    }

    @Test
    @Transactional
    void getAllAgentsByAmountRatioIsNullOrNotNull() throws Exception {
        // Initialize the database
        agentRepository.saveAndFlush(agent);

        // Get all the agentList where amountRatio is not null
        defaultAgentShouldBeFound("amountRatio.specified=true");

        // Get all the agentList where amountRatio is null
        defaultAgentShouldNotBeFound("amountRatio.specified=false");
    }

    @Test
    @Transactional
    void getAllAgentsByAmountRatioIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        agentRepository.saveAndFlush(agent);

        // Get all the agentList where amountRatio is greater than or equal to DEFAULT_AMOUNT_RATIO
        defaultAgentShouldBeFound("amountRatio.greaterThanOrEqual=" + DEFAULT_AMOUNT_RATIO);

        // Get all the agentList where amountRatio is greater than or equal to UPDATED_AMOUNT_RATIO
        defaultAgentShouldNotBeFound("amountRatio.greaterThanOrEqual=" + UPDATED_AMOUNT_RATIO);
    }

    @Test
    @Transactional
    void getAllAgentsByAmountRatioIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        agentRepository.saveAndFlush(agent);

        // Get all the agentList where amountRatio is less than or equal to DEFAULT_AMOUNT_RATIO
        defaultAgentShouldBeFound("amountRatio.lessThanOrEqual=" + DEFAULT_AMOUNT_RATIO);

        // Get all the agentList where amountRatio is less than or equal to SMALLER_AMOUNT_RATIO
        defaultAgentShouldNotBeFound("amountRatio.lessThanOrEqual=" + SMALLER_AMOUNT_RATIO);
    }

    @Test
    @Transactional
    void getAllAgentsByAmountRatioIsLessThanSomething() throws Exception {
        // Initialize the database
        agentRepository.saveAndFlush(agent);

        // Get all the agentList where amountRatio is less than DEFAULT_AMOUNT_RATIO
        defaultAgentShouldNotBeFound("amountRatio.lessThan=" + DEFAULT_AMOUNT_RATIO);

        // Get all the agentList where amountRatio is less than UPDATED_AMOUNT_RATIO
        defaultAgentShouldBeFound("amountRatio.lessThan=" + UPDATED_AMOUNT_RATIO);
    }

    @Test
    @Transactional
    void getAllAgentsByAmountRatioIsGreaterThanSomething() throws Exception {
        // Initialize the database
        agentRepository.saveAndFlush(agent);

        // Get all the agentList where amountRatio is greater than DEFAULT_AMOUNT_RATIO
        defaultAgentShouldNotBeFound("amountRatio.greaterThan=" + DEFAULT_AMOUNT_RATIO);

        // Get all the agentList where amountRatio is greater than SMALLER_AMOUNT_RATIO
        defaultAgentShouldBeFound("amountRatio.greaterThan=" + SMALLER_AMOUNT_RATIO);
    }

    @Test
    @Transactional
    void getAllAgentsByApiKeyIsEqualToSomething() throws Exception {
        // Initialize the database
        agentRepository.saveAndFlush(agent);

        // Get all the agentList where apiKey equals to DEFAULT_API_KEY
        defaultAgentShouldBeFound("apiKey.equals=" + DEFAULT_API_KEY);

        // Get all the agentList where apiKey equals to UPDATED_API_KEY
        defaultAgentShouldNotBeFound("apiKey.equals=" + UPDATED_API_KEY);
    }

    @Test
    @Transactional
    void getAllAgentsByApiKeyIsNotEqualToSomething() throws Exception {
        // Initialize the database
        agentRepository.saveAndFlush(agent);

        // Get all the agentList where apiKey not equals to DEFAULT_API_KEY
        defaultAgentShouldNotBeFound("apiKey.notEquals=" + DEFAULT_API_KEY);

        // Get all the agentList where apiKey not equals to UPDATED_API_KEY
        defaultAgentShouldBeFound("apiKey.notEquals=" + UPDATED_API_KEY);
    }

    @Test
    @Transactional
    void getAllAgentsByApiKeyIsInShouldWork() throws Exception {
        // Initialize the database
        agentRepository.saveAndFlush(agent);

        // Get all the agentList where apiKey in DEFAULT_API_KEY or UPDATED_API_KEY
        defaultAgentShouldBeFound("apiKey.in=" + DEFAULT_API_KEY + "," + UPDATED_API_KEY);

        // Get all the agentList where apiKey equals to UPDATED_API_KEY
        defaultAgentShouldNotBeFound("apiKey.in=" + UPDATED_API_KEY);
    }

    @Test
    @Transactional
    void getAllAgentsByApiKeyIsNullOrNotNull() throws Exception {
        // Initialize the database
        agentRepository.saveAndFlush(agent);

        // Get all the agentList where apiKey is not null
        defaultAgentShouldBeFound("apiKey.specified=true");

        // Get all the agentList where apiKey is null
        defaultAgentShouldNotBeFound("apiKey.specified=false");
    }

    @Test
    @Transactional
    void getAllAgentsByApiKeyContainsSomething() throws Exception {
        // Initialize the database
        agentRepository.saveAndFlush(agent);

        // Get all the agentList where apiKey contains DEFAULT_API_KEY
        defaultAgentShouldBeFound("apiKey.contains=" + DEFAULT_API_KEY);

        // Get all the agentList where apiKey contains UPDATED_API_KEY
        defaultAgentShouldNotBeFound("apiKey.contains=" + UPDATED_API_KEY);
    }

    @Test
    @Transactional
    void getAllAgentsByApiKeyNotContainsSomething() throws Exception {
        // Initialize the database
        agentRepository.saveAndFlush(agent);

        // Get all the agentList where apiKey does not contain DEFAULT_API_KEY
        defaultAgentShouldNotBeFound("apiKey.doesNotContain=" + DEFAULT_API_KEY);

        // Get all the agentList where apiKey does not contain UPDATED_API_KEY
        defaultAgentShouldBeFound("apiKey.doesNotContain=" + UPDATED_API_KEY);
    }

    //    @Test
    //    @Transactional
    //    void getAllAgentsByCurrenciesIsEqualToSomething() throws Exception {
    //        // Initialize the database
    //        agentRepository.saveAndFlush(agent);
    //
    //        // Get all the agentList where currencies equals to DEFAULT_CURRENCIES
    //        defaultAgentShouldBeFound("currencies.equals=" + DEFAULT_CURRENCIES);
    //
    //        // Get all the agentList where currencies equals to UPDATED_CURRENCIES
    //        defaultAgentShouldNotBeFound("currencies.equals=" + UPDATED_CURRENCIES);
    //    }

    //    @Test
    //    @Transactional
    //    void getAllAgentsByCurrenciesIsNotEqualToSomething() throws Exception {
    //        // Initialize the database
    //        agentRepository.saveAndFlush(agent);
    //
    //        // Get all the agentList where currencies not equals to DEFAULT_CURRENCIES
    //        defaultAgentShouldNotBeFound("currencies.notEquals=" + DEFAULT_CURRENCIES);
    //
    //        // Get all the agentList where currencies not equals to UPDATED_CURRENCIES
    //        defaultAgentShouldBeFound("currencies.notEquals=" + UPDATED_CURRENCIES);
    //    }

    //    @Test
    //    @Transactional
    //    void getAllAgentsByCurrenciesIsInShouldWork() throws Exception {
    //        // Initialize the database
    //        agentRepository.saveAndFlush(agent);
    //
    //        // Get all the agentList where currencies in DEFAULT_CURRENCIES or UPDATED_CURRENCIES
    //        defaultAgentShouldBeFound("currencies.in=" + DEFAULT_CURRENCIES + "," + UPDATED_CURRENCIES);
    //
    //        // Get all the agentList where currencies equals to UPDATED_CURRENCIES
    //        defaultAgentShouldNotBeFound("currencies.in=" + UPDATED_CURRENCIES);
    //    }
    //
    //    @Test
    //    @Transactional
    //    void getAllAgentsByCurrenciesIsNullOrNotNull() throws Exception {
    //        // Initialize the database
    //        agentRepository.saveAndFlush(agent);
    //
    //        // Get all the agentList where currencies is not null
    //        defaultAgentShouldBeFound("currencies.specified=true");
    //
    //        // Get all the agentList where currencies is null
    //        defaultAgentShouldNotBeFound("currencies.specified=false");
    //    }
    //
    //    @Test
    //    @Transactional
    //    void getAllAgentsByCurrenciesContainsSomething() throws Exception {
    //        // Initialize the database
    //        agentRepository.saveAndFlush(agent);
    //
    //        // Get all the agentList where currencies contains DEFAULT_CURRENCIES
    //        defaultAgentShouldBeFound("currencies.contains=" + DEFAULT_CURRENCIES);
    //
    //        // Get all the agentList where currencies contains UPDATED_CURRENCIES
    //        defaultAgentShouldNotBeFound("currencies.contains=" + UPDATED_CURRENCIES);
    //    }
    //
    ////    @Test
    ////    @Transactional
    ////    void getAllAgentsByCurrenciesNotContainsSomething() throws Exception {
    ////        // Initialize the database
    ////        agentRepository.saveAndFlush(agent);
    ////
    ////        // Get all the agentList where currencies does not contain DEFAULT_CURRENCIES
    ////        defaultAgentShouldNotBeFound("currencies.doesNotContain=" + DEFAULT_CURRENCIES);
    ////
    ////        // Get all the agentList where currencies does not contain UPDATED_CURRENCIES
    ////        defaultAgentShouldBeFound("currencies.doesNotContain=" + UPDATED_CURRENCIES);
    ////    }
    //
    //    @Test
    //    @Transactional
    //    void getAllAgentsByIpAddressesIsEqualToSomething() throws Exception {
    //        // Initialize the database
    //        agentRepository.saveAndFlush(agent);
    //
    //        // Get all the agentList where ipAddresses equals to DEFAULT_IP_ADDRESSES
    //        defaultAgentShouldBeFound("ipAddresses.equals=" + DEFAULT_IP_ADDRESSES);
    //
    //        // Get all the agentList where ipAddresses equals to UPDATED_IP_ADDRESSES
    //        defaultAgentShouldNotBeFound("ipAddresses.equals=" + UPDATED_IP_ADDRESSES);
    //    }
    //
    //    @Test
    //    @Transactional
    //    void getAllAgentsByIpAddressesIsNotEqualToSomething() throws Exception {
    //        // Initialize the database
    //        agentRepository.saveAndFlush(agent);
    //
    //        // Get all the agentList where ipAddresses not equals to DEFAULT_IP_ADDRESSES
    //        defaultAgentShouldNotBeFound("ipAddresses.notEquals=" + DEFAULT_IP_ADDRESSES);
    //
    //        // Get all the agentList where ipAddresses not equals to UPDATED_IP_ADDRESSES
    //        defaultAgentShouldBeFound("ipAddresses.notEquals=" + UPDATED_IP_ADDRESSES);
    //    }
    //
    //    @Test
    //    @Transactional
    //    void getAllAgentsByIpAddressesIsInShouldWork() throws Exception {
    //        // Initialize the database
    //        agentRepository.saveAndFlush(agent);
    //
    //        // Get all the agentList where ipAddresses in DEFAULT_IP_ADDRESSES or UPDATED_IP_ADDRESSES
    //        defaultAgentShouldBeFound("ipAddresses.in=" + DEFAULT_IP_ADDRESSES + "," + UPDATED_IP_ADDRESSES);
    //
    //        // Get all the agentList where ipAddresses equals to UPDATED_IP_ADDRESSES
    //        defaultAgentShouldNotBeFound("ipAddresses.in=" + UPDATED_IP_ADDRESSES);
    //    }
    //
    ////    @Test
    ////    @Transactional
    ////    void getAllAgentsByIpAddressesIsNullOrNotNull() throws Exception {
    ////        // Initialize the database
    ////        agentRepository.saveAndFlush(agent);
    ////
    ////        // Get all the agentList where ipAddresses is not null
    ////        defaultAgentShouldBeFound("ipAddresses.specified=true");
    ////
    ////        // Get all the agentList where ipAddresses is null
    ////        defaultAgentShouldNotBeFound("ipAddresses.specified=false");
    ////    }
    //
    //    @Test
    //    @Transactional
    //    void getAllAgentsByIpAddressesContainsSomething() throws Exception {
    //        // Initialize the database
    //        agentRepository.saveAndFlush(agent);
    //
    //        // Get all the agentList where ipAddresses contains DEFAULT_IP_ADDRESSES
    //        defaultAgentShouldBeFound("ipAddresses.contains=" + DEFAULT_IP_ADDRESSES);
    //
    //        // Get all the agentList where ipAddresses contains UPDATED_IP_ADDRESSES
    //        defaultAgentShouldNotBeFound("ipAddresses.contains=" + UPDATED_IP_ADDRESSES);
    //    }
    //
    //    @Test
    //    @Transactional
    //    void getAllAgentsByIpAddressesNotContainsSomething() throws Exception {
    //        // Initialize the database
    //        agentRepository.saveAndFlush(agent);
    //
    //        // Get all the agentList where ipAddresses does not contain DEFAULT_IP_ADDRESSES
    //        defaultAgentShouldNotBeFound("ipAddresses.doesNotContain=" + DEFAULT_IP_ADDRESSES);
    //
    //        // Get all the agentList where ipAddresses does not contain UPDATED_IP_ADDRESSES
    //        defaultAgentShouldBeFound("ipAddresses.doesNotContain=" + UPDATED_IP_ADDRESSES);
    //    }

    @Test
    @Transactional
    void getAllAgentsByOrdIsEqualToSomething() throws Exception {
        // Initialize the database
        agentRepository.saveAndFlush(agent);

        // Get all the agentList where ord equals to DEFAULT_ORD
        defaultAgentShouldBeFound("ord.equals=" + DEFAULT_ORD);

        // Get all the agentList where ord equals to UPDATED_ORD
        defaultAgentShouldNotBeFound("ord.equals=" + UPDATED_ORD);
    }

    @Test
    @Transactional
    void getAllAgentsByOrdIsNotEqualToSomething() throws Exception {
        // Initialize the database
        agentRepository.saveAndFlush(agent);

        // Get all the agentList where ord not equals to DEFAULT_ORD
        defaultAgentShouldNotBeFound("ord.notEquals=" + DEFAULT_ORD);

        // Get all the agentList where ord not equals to UPDATED_ORD
        defaultAgentShouldBeFound("ord.notEquals=" + UPDATED_ORD);
    }

    @Test
    @Transactional
    void getAllAgentsByOrdIsInShouldWork() throws Exception {
        // Initialize the database
        agentRepository.saveAndFlush(agent);

        // Get all the agentList where ord in DEFAULT_ORD or UPDATED_ORD
        defaultAgentShouldBeFound("ord.in=" + DEFAULT_ORD + "," + UPDATED_ORD);

        // Get all the agentList where ord equals to UPDATED_ORD
        defaultAgentShouldNotBeFound("ord.in=" + UPDATED_ORD);
    }

    @Test
    @Transactional
    void getAllAgentsByOrdIsNullOrNotNull() throws Exception {
        // Initialize the database
        agentRepository.saveAndFlush(agent);

        // Get all the agentList where ord is not null
        defaultAgentShouldBeFound("ord.specified=true");

        // Get all the agentList where ord is null
        defaultAgentShouldNotBeFound("ord.specified=false");
    }

    @Test
    @Transactional
    void getAllAgentsByOrdIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        agentRepository.saveAndFlush(agent);

        // Get all the agentList where ord is greater than or equal to DEFAULT_ORD
        defaultAgentShouldBeFound("ord.greaterThanOrEqual=" + DEFAULT_ORD);

        // Get all the agentList where ord is greater than or equal to UPDATED_ORD
        defaultAgentShouldNotBeFound("ord.greaterThanOrEqual=" + UPDATED_ORD);
    }

    @Test
    @Transactional
    void getAllAgentsByOrdIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        agentRepository.saveAndFlush(agent);

        // Get all the agentList where ord is less than or equal to DEFAULT_ORD
        defaultAgentShouldBeFound("ord.lessThanOrEqual=" + DEFAULT_ORD);

        // Get all the agentList where ord is less than or equal to SMALLER_ORD
        defaultAgentShouldNotBeFound("ord.lessThanOrEqual=" + SMALLER_ORD);
    }

    @Test
    @Transactional
    void getAllAgentsByOrdIsLessThanSomething() throws Exception {
        // Initialize the database
        agentRepository.saveAndFlush(agent);

        // Get all the agentList where ord is less than DEFAULT_ORD
        defaultAgentShouldNotBeFound("ord.lessThan=" + DEFAULT_ORD);

        // Get all the agentList where ord is less than UPDATED_ORD
        defaultAgentShouldBeFound("ord.lessThan=" + UPDATED_ORD);
    }

    @Test
    @Transactional
    void getAllAgentsByOrdIsGreaterThanSomething() throws Exception {
        // Initialize the database
        agentRepository.saveAndFlush(agent);

        // Get all the agentList where ord is greater than DEFAULT_ORD
        defaultAgentShouldNotBeFound("ord.greaterThan=" + DEFAULT_ORD);

        // Get all the agentList where ord is greater than SMALLER_ORD
        defaultAgentShouldBeFound("ord.greaterThan=" + SMALLER_ORD);
    }

    @Test
    @Transactional
    void getAllAgentsByActiveIsEqualToSomething() throws Exception {
        // Initialize the database
        agentRepository.saveAndFlush(agent);

        // Get all the agentList where active equals to DEFAULT_ACTIVE
        defaultAgentShouldBeFound("active.equals=" + DEFAULT_ACTIVE);

        // Get all the agentList where active equals to UPDATED_ACTIVE
        defaultAgentShouldNotBeFound("active.equals=" + UPDATED_ACTIVE);
    }

    @Test
    @Transactional
    void getAllAgentsByActiveIsNotEqualToSomething() throws Exception {
        // Initialize the database
        agentRepository.saveAndFlush(agent);

        // Get all the agentList where active not equals to DEFAULT_ACTIVE
        defaultAgentShouldNotBeFound("active.notEquals=" + DEFAULT_ACTIVE);

        // Get all the agentList where active not equals to UPDATED_ACTIVE
        defaultAgentShouldBeFound("active.notEquals=" + UPDATED_ACTIVE);
    }

    @Test
    @Transactional
    void getAllAgentsByActiveIsInShouldWork() throws Exception {
        // Initialize the database
        agentRepository.saveAndFlush(agent);

        // Get all the agentList where active in DEFAULT_ACTIVE or UPDATED_ACTIVE
        defaultAgentShouldBeFound("active.in=" + DEFAULT_ACTIVE + "," + UPDATED_ACTIVE);

        // Get all the agentList where active equals to UPDATED_ACTIVE
        defaultAgentShouldNotBeFound("active.in=" + UPDATED_ACTIVE);
    }

    @Test
    @Transactional
    void getAllAgentsByActiveIsNullOrNotNull() throws Exception {
        // Initialize the database
        agentRepository.saveAndFlush(agent);

        // Get all the agentList where active is not null
        defaultAgentShouldBeFound("active.specified=true");

        // Get all the agentList where active is null
        defaultAgentShouldNotBeFound("active.specified=false");
    }

    @Test
    @Transactional
    void getAllAgentsByPhoneNumberIsEqualToSomething() throws Exception {
        // Initialize the database
        agentRepository.saveAndFlush(agent);

        // Get all the agentList where phoneNumber equals to DEFAULT_PHONE_NUMBER
        defaultAgentShouldBeFound("phoneNumber.equals=" + DEFAULT_PHONE_NUMBER);

        // Get all the agentList where phoneNumber equals to UPDATED_PHONE_NUMBER
        defaultAgentShouldNotBeFound("phoneNumber.equals=" + UPDATED_PHONE_NUMBER);
    }

    @Test
    @Transactional
    void getAllAgentsByPhoneNumberIsNotEqualToSomething() throws Exception {
        // Initialize the database
        agentRepository.saveAndFlush(agent);

        // Get all the agentList where phoneNumber not equals to DEFAULT_PHONE_NUMBER
        defaultAgentShouldNotBeFound("phoneNumber.notEquals=" + DEFAULT_PHONE_NUMBER);

        // Get all the agentList where phoneNumber not equals to UPDATED_PHONE_NUMBER
        defaultAgentShouldBeFound("phoneNumber.notEquals=" + UPDATED_PHONE_NUMBER);
    }

    @Test
    @Transactional
    void getAllAgentsByPhoneNumberIsInShouldWork() throws Exception {
        // Initialize the database
        agentRepository.saveAndFlush(agent);

        // Get all the agentList where phoneNumber in DEFAULT_PHONE_NUMBER or UPDATED_PHONE_NUMBER
        defaultAgentShouldBeFound("phoneNumber.in=" + DEFAULT_PHONE_NUMBER + "," + UPDATED_PHONE_NUMBER);

        // Get all the agentList where phoneNumber equals to UPDATED_PHONE_NUMBER
        defaultAgentShouldNotBeFound("phoneNumber.in=" + UPDATED_PHONE_NUMBER);
    }

    @Test
    @Transactional
    void getAllAgentsByPhoneNumberIsNullOrNotNull() throws Exception {
        // Initialize the database
        agentRepository.saveAndFlush(agent);

        // Get all the agentList where phoneNumber is not null
        defaultAgentShouldBeFound("phoneNumber.specified=true");

        // Get all the agentList where phoneNumber is null
        defaultAgentShouldNotBeFound("phoneNumber.specified=false");
    }

    @Test
    @Transactional
    void getAllAgentsByPhoneNumberContainsSomething() throws Exception {
        // Initialize the database
        agentRepository.saveAndFlush(agent);

        // Get all the agentList where phoneNumber contains DEFAULT_PHONE_NUMBER
        defaultAgentShouldBeFound("phoneNumber.contains=" + DEFAULT_PHONE_NUMBER);

        // Get all the agentList where phoneNumber contains UPDATED_PHONE_NUMBER
        defaultAgentShouldNotBeFound("phoneNumber.contains=" + UPDATED_PHONE_NUMBER);
    }

    @Test
    @Transactional
    void getAllAgentsByPhoneNumberNotContainsSomething() throws Exception {
        // Initialize the database
        agentRepository.saveAndFlush(agent);

        // Get all the agentList where phoneNumber does not contain DEFAULT_PHONE_NUMBER
        defaultAgentShouldNotBeFound("phoneNumber.doesNotContain=" + DEFAULT_PHONE_NUMBER);

        // Get all the agentList where phoneNumber does not contain UPDATED_PHONE_NUMBER
        defaultAgentShouldBeFound("phoneNumber.doesNotContain=" + UPDATED_PHONE_NUMBER);
    }

    @Test
    @Transactional
    void getAllAgentsByInnIsEqualToSomething() throws Exception {
        // Initialize the database
        agentRepository.saveAndFlush(agent);

        // Get all the agentList where inn equals to DEFAULT_INN
        defaultAgentShouldBeFound("inn.equals=" + DEFAULT_INN);

        // Get all the agentList where inn equals to UPDATED_INN
        defaultAgentShouldNotBeFound("inn.equals=" + UPDATED_INN);
    }

    @Test
    @Transactional
    void getAllAgentsByInnIsNotEqualToSomething() throws Exception {
        // Initialize the database
        agentRepository.saveAndFlush(agent);

        // Get all the agentList where inn not equals to DEFAULT_INN
        defaultAgentShouldNotBeFound("inn.notEquals=" + DEFAULT_INN);

        // Get all the agentList where inn not equals to UPDATED_INN
        defaultAgentShouldBeFound("inn.notEquals=" + UPDATED_INN);
    }

    @Test
    @Transactional
    void getAllAgentsByInnIsInShouldWork() throws Exception {
        // Initialize the database
        agentRepository.saveAndFlush(agent);

        // Get all the agentList where inn in DEFAULT_INN or UPDATED_INN
        defaultAgentShouldBeFound("inn.in=" + DEFAULT_INN + "," + UPDATED_INN);

        // Get all the agentList where inn equals to UPDATED_INN
        defaultAgentShouldNotBeFound("inn.in=" + UPDATED_INN);
    }

    @Test
    @Transactional
    void getAllAgentsByInnIsNullOrNotNull() throws Exception {
        // Initialize the database
        agentRepository.saveAndFlush(agent);

        // Get all the agentList where inn is not null
        defaultAgentShouldBeFound("inn.specified=true");

        // Get all the agentList where inn is null
        defaultAgentShouldNotBeFound("inn.specified=false");
    }

    @Test
    @Transactional
    void getAllAgentsByInnContainsSomething() throws Exception {
        // Initialize the database
        agentRepository.saveAndFlush(agent);

        // Get all the agentList where inn contains DEFAULT_INN
        defaultAgentShouldBeFound("inn.contains=" + DEFAULT_INN);

        // Get all the agentList where inn contains UPDATED_INN
        defaultAgentShouldNotBeFound("inn.contains=" + UPDATED_INN);
    }

    @Test
    @Transactional
    void getAllAgentsByInnNotContainsSomething() throws Exception {
        // Initialize the database
        agentRepository.saveAndFlush(agent);

        // Get all the agentList where inn does not contain DEFAULT_INN
        defaultAgentShouldNotBeFound("inn.doesNotContain=" + DEFAULT_INN);

        // Get all the agentList where inn does not contain UPDATED_INN
        defaultAgentShouldBeFound("inn.doesNotContain=" + UPDATED_INN);
    }

    @Test
    @Transactional
    void getAllAgentsByOrgNameIsEqualToSomething() throws Exception {
        // Initialize the database
        agentRepository.saveAndFlush(agent);

        // Get all the agentList where orgName equals to DEFAULT_ORG_NAME
        defaultAgentShouldBeFound("orgName.equals=" + DEFAULT_ORG_NAME);

        // Get all the agentList where orgName equals to UPDATED_ORG_NAME
        defaultAgentShouldNotBeFound("orgName.equals=" + UPDATED_ORG_NAME);
    }

    @Test
    @Transactional
    void getAllAgentsByOrgNameIsNotEqualToSomething() throws Exception {
        // Initialize the database
        agentRepository.saveAndFlush(agent);

        // Get all the agentList where orgName not equals to DEFAULT_ORG_NAME
        defaultAgentShouldNotBeFound("orgName.notEquals=" + DEFAULT_ORG_NAME);

        // Get all the agentList where orgName not equals to UPDATED_ORG_NAME
        defaultAgentShouldBeFound("orgName.notEquals=" + UPDATED_ORG_NAME);
    }

    @Test
    @Transactional
    void getAllAgentsByOrgNameIsInShouldWork() throws Exception {
        // Initialize the database
        agentRepository.saveAndFlush(agent);

        // Get all the agentList where orgName in DEFAULT_ORG_NAME or UPDATED_ORG_NAME
        defaultAgentShouldBeFound("orgName.in=" + DEFAULT_ORG_NAME + "," + UPDATED_ORG_NAME);

        // Get all the agentList where orgName equals to UPDATED_ORG_NAME
        defaultAgentShouldNotBeFound("orgName.in=" + UPDATED_ORG_NAME);
    }

    @Test
    @Transactional
    void getAllAgentsByOrgNameIsNullOrNotNull() throws Exception {
        // Initialize the database
        agentRepository.saveAndFlush(agent);

        // Get all the agentList where orgName is not null
        defaultAgentShouldBeFound("orgName.specified=true");

        // Get all the agentList where orgName is null
        defaultAgentShouldNotBeFound("orgName.specified=false");
    }

    @Test
    @Transactional
    void getAllAgentsByOrgNameContainsSomething() throws Exception {
        // Initialize the database
        agentRepository.saveAndFlush(agent);

        // Get all the agentList where orgName contains DEFAULT_ORG_NAME
        defaultAgentShouldBeFound("orgName.contains=" + DEFAULT_ORG_NAME);

        // Get all the agentList where orgName contains UPDATED_ORG_NAME
        defaultAgentShouldNotBeFound("orgName.contains=" + UPDATED_ORG_NAME);
    }

    @Test
    @Transactional
    void getAllAgentsByOrgNameNotContainsSomething() throws Exception {
        // Initialize the database
        agentRepository.saveAndFlush(agent);

        // Get all the agentList where orgName does not contain DEFAULT_ORG_NAME
        defaultAgentShouldNotBeFound("orgName.doesNotContain=" + DEFAULT_ORG_NAME);

        // Get all the agentList where orgName does not contain UPDATED_ORG_NAME
        defaultAgentShouldBeFound("orgName.doesNotContain=" + UPDATED_ORG_NAME);
    }

    @Test
    @Transactional
    void getAllAgentsByTitleIsEqualToSomething() throws Exception {
        // Initialize the database
        agentRepository.saveAndFlush(agent);
        I18n title = I18nResourceIT.createEntity(em);
        em.persist(title);
        em.flush();
        agent.addTitle(title);
        agentRepository.saveAndFlush(agent);
        Long titleId = title.getId();

        // Get all the agentList where title equals to titleId
        defaultAgentShouldBeFound("titleId.equals=" + titleId);

        // Get all the agentList where title equals to (titleId + 1)
        defaultAgentShouldNotBeFound("titleId.equals=" + (titleId + 1));
    }

    @Test
    @Transactional
    void getAllAgentsByDepositIsEqualToSomething() throws Exception {
        // Initialize the database
        agentRepository.saveAndFlush(agent);
        Deposit deposit = DepositResourceIT.createEntity(em);
        em.persist(deposit);
        em.flush();
        agent.setDeposit(deposit);
        deposit.setAgent(agent);
        agentRepository.saveAndFlush(agent);
        Long depositId = deposit.getId();

        // Get all the agentList where deposit equals to depositId
        defaultAgentShouldBeFound("depositId.equals=" + depositId);

        // Get all the agentList where deposit equals to (depositId + 1)
        defaultAgentShouldNotBeFound("depositId.equals=" + (depositId + 1));
    }

    /**
     * Executes the search, and checks that the default entity is returned.
     */
    private void defaultAgentShouldBeFound(String filter) throws Exception {
        restAgentMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(agent.getId().intValue())))
            .andExpect(jsonPath("$.[*].shortName").value(hasItem(DEFAULT_SHORT_NAME)))
            .andExpect(jsonPath("$.[*].amountRatio").value(hasItem(DEFAULT_AMOUNT_RATIO)))
            .andExpect(jsonPath("$.[*].apiKey").value(hasItem(DEFAULT_API_KEY)))
            .andExpect(jsonPath("$.[*].currencies[0]").value(hasItem(DEFAULT_CURRENCIES.toArray()[0])))
            .andExpect(jsonPath("$.[*].ipAddresses[0]").value(hasItem(DEFAULT_IP_ADDRESSES.toArray()[0])))
            .andExpect(jsonPath("$.[*].logoContentType").value(hasItem(DEFAULT_LOGO_CONTENT_TYPE)))
            .andExpect(jsonPath("$.[*].logo").value(hasItem(Base64Utils.encodeToString(DEFAULT_LOGO))))
            .andExpect(jsonPath("$.[*].ord").value(hasItem(DEFAULT_ORD)))
            .andExpect(jsonPath("$.[*].active").value(hasItem(DEFAULT_ACTIVE.booleanValue())))
            .andExpect(jsonPath("$.[*].phoneNumber").value(hasItem(DEFAULT_PHONE_NUMBER)))
            .andExpect(jsonPath("$.[*].inn").value(hasItem(DEFAULT_INN)))
            .andExpect(jsonPath("$.[*].orgName").value(hasItem(DEFAULT_ORG_NAME)));

        // Check, that the count call also returns 1
        restAgentMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("1"));
    }

    /**
     * Executes the search, and checks that the default entity is not returned.
     */
    private void defaultAgentShouldNotBeFound(String filter) throws Exception {
        restAgentMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty());

        // Check, that the count call also returns 0
        restAgentMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("0"));
    }

    @Test
    @Transactional
    void getNonExistingAgent() throws Exception {
        // Get the agent
        restAgentMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putNewAgent() throws Exception {
        // Initialize the database
        agentRepository.saveAndFlush(agent);

        int databaseSizeBeforeUpdate = agentRepository.findAll().size();

        // Update the agent
        Agent updatedAgent = agentRepository.findById(agent.getId()).get();
        // Disconnect from session so that the updates on updatedAgent are not directly saved in db
        em.detach(updatedAgent);
        updatedAgent
            .shortName(UPDATED_SHORT_NAME)
            .amountRatio(UPDATED_AMOUNT_RATIO)
            .apiKey(UPDATED_API_KEY)
            .currencies(UPDATED_CURRENCIES)
            .ipAddresses(UPDATED_IP_ADDRESSES)
            .logo(UPDATED_LOGO)
            .logoContentType(UPDATED_LOGO_CONTENT_TYPE)
            .ord(UPDATED_ORD)
            .active(UPDATED_ACTIVE)
            .phoneNumber(UPDATED_PHONE_NUMBER)
            .inn(UPDATED_INN)
            .orgName(UPDATED_ORG_NAME);
        AgentDTO agentDTO = agentMapper.toDto(updatedAgent);

        restAgentMockMvc
            .perform(
                put(ENTITY_API_URL_ID, agentDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(agentDTO))
            )
            .andExpect(status().isOk());

        // Validate the Agent in the database
        List<Agent> agentList = agentRepository.findAll();
        assertThat(agentList).hasSize(databaseSizeBeforeUpdate);
        Agent testAgent = agentList.get(agentList.size() - 1);
        assertThat(testAgent.getShortName()).isEqualTo(UPDATED_SHORT_NAME);
        assertThat(testAgent.getAmountRatio()).isEqualTo(UPDATED_AMOUNT_RATIO);
        assertThat(testAgent.getApiKey()).isEqualTo(UPDATED_API_KEY);
        assertThat(testAgent.getCurrencies()).isEqualTo(UPDATED_CURRENCIES);
        assertThat(testAgent.getIpAddresses()).isEqualTo(UPDATED_IP_ADDRESSES);
        assertThat(testAgent.getLogo()).isEqualTo(UPDATED_LOGO);
        assertThat(testAgent.getLogoContentType()).isEqualTo(UPDATED_LOGO_CONTENT_TYPE);
        assertThat(testAgent.getOrd()).isEqualTo(UPDATED_ORD);
        assertThat(testAgent.getActive()).isEqualTo(UPDATED_ACTIVE);
        assertThat(testAgent.getPhoneNumber()).isEqualTo(UPDATED_PHONE_NUMBER);
        assertThat(testAgent.getInn()).isEqualTo(UPDATED_INN);
        assertThat(testAgent.getOrgName()).isEqualTo(UPDATED_ORG_NAME);
    }

    @Test
    @Transactional
    void putNonExistingAgent() throws Exception {
        int databaseSizeBeforeUpdate = agentRepository.findAll().size();
        agent.setId(count.incrementAndGet());

        // Create the Agent
        AgentDTO agentDTO = agentMapper.toDto(agent);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restAgentMockMvc
            .perform(
                put(ENTITY_API_URL_ID, agentDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(agentDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Agent in the database
        List<Agent> agentList = agentRepository.findAll();
        assertThat(agentList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchAgent() throws Exception {
        int databaseSizeBeforeUpdate = agentRepository.findAll().size();
        agent.setId(count.incrementAndGet());

        // Create the Agent
        AgentDTO agentDTO = agentMapper.toDto(agent);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restAgentMockMvc
            .perform(
                put(ENTITY_API_URL_ID, count.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(agentDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Agent in the database
        List<Agent> agentList = agentRepository.findAll();
        assertThat(agentList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamAgent() throws Exception {
        int databaseSizeBeforeUpdate = agentRepository.findAll().size();
        agent.setId(count.incrementAndGet());

        // Create the Agent
        AgentDTO agentDTO = agentMapper.toDto(agent);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restAgentMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(agentDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Agent in the database
        List<Agent> agentList = agentRepository.findAll();
        assertThat(agentList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateAgentWithPatch() throws Exception {
        // Initialize the database
        agentRepository.saveAndFlush(agent);

        int databaseSizeBeforeUpdate = agentRepository.findAll().size();

        // Update the agent using partial update
        Agent partialUpdatedAgent = new Agent();
        partialUpdatedAgent.setId(agent.getId());

        partialUpdatedAgent
            .shortName(UPDATED_SHORT_NAME)
            .currencies(UPDATED_CURRENCIES)
            .logo(UPDATED_LOGO)
            .logoContentType(UPDATED_LOGO_CONTENT_TYPE)
            .ord(UPDATED_ORD)
            .active(UPDATED_ACTIVE)
            .phoneNumber(UPDATED_PHONE_NUMBER)
            .inn(UPDATED_INN);

        restAgentMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedAgent.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedAgent))
            )
            .andExpect(status().isOk());

        // Validate the Agent in the database
        List<Agent> agentList = agentRepository.findAll();
        assertThat(agentList).hasSize(databaseSizeBeforeUpdate);
        Agent testAgent = agentList.get(agentList.size() - 1);
        assertThat(testAgent.getShortName()).isEqualTo(UPDATED_SHORT_NAME);
        assertThat(testAgent.getAmountRatio()).isEqualTo(DEFAULT_AMOUNT_RATIO);
        assertThat(testAgent.getApiKey()).isEqualTo(DEFAULT_API_KEY);
        assertThat(testAgent.getCurrencies()).isEqualTo(UPDATED_CURRENCIES);
        assertThat(testAgent.getIpAddresses()).isEqualTo(DEFAULT_IP_ADDRESSES);
        assertThat(testAgent.getLogo()).isEqualTo(UPDATED_LOGO);
        assertThat(testAgent.getLogoContentType()).isEqualTo(UPDATED_LOGO_CONTENT_TYPE);
        assertThat(testAgent.getOrd()).isEqualTo(UPDATED_ORD);
        assertThat(testAgent.getActive()).isEqualTo(UPDATED_ACTIVE);
        assertThat(testAgent.getPhoneNumber()).isEqualTo(UPDATED_PHONE_NUMBER);
        assertThat(testAgent.getInn()).isEqualTo(UPDATED_INN);
        assertThat(testAgent.getOrgName()).isEqualTo(DEFAULT_ORG_NAME);
    }

    @Test
    @Transactional
    void fullUpdateAgentWithPatch() throws Exception {
        // Initialize the database
        agentRepository.saveAndFlush(agent);

        int databaseSizeBeforeUpdate = agentRepository.findAll().size();

        // Update the agent using partial update
        Agent partialUpdatedAgent = new Agent();
        partialUpdatedAgent.setId(agent.getId());

        partialUpdatedAgent
            .shortName(UPDATED_SHORT_NAME)
            .amountRatio(UPDATED_AMOUNT_RATIO)
            .apiKey(UPDATED_API_KEY)
            .currencies(UPDATED_CURRENCIES)
            .ipAddresses(UPDATED_IP_ADDRESSES)
            .logo(UPDATED_LOGO)
            .logoContentType(UPDATED_LOGO_CONTENT_TYPE)
            .ord(UPDATED_ORD)
            .active(UPDATED_ACTIVE)
            .phoneNumber(UPDATED_PHONE_NUMBER)
            .inn(UPDATED_INN)
            .orgName(UPDATED_ORG_NAME);

        restAgentMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedAgent.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedAgent))
            )
            .andExpect(status().isOk());

        // Validate the Agent in the database
        List<Agent> agentList = agentRepository.findAll();
        assertThat(agentList).hasSize(databaseSizeBeforeUpdate);
        Agent testAgent = agentList.get(agentList.size() - 1);
        assertThat(testAgent.getShortName()).isEqualTo(UPDATED_SHORT_NAME);
        assertThat(testAgent.getAmountRatio()).isEqualTo(UPDATED_AMOUNT_RATIO);
        assertThat(testAgent.getApiKey()).isEqualTo(UPDATED_API_KEY);
        assertThat(testAgent.getCurrencies()).isEqualTo(UPDATED_CURRENCIES);
        assertThat(testAgent.getIpAddresses()).isEqualTo(UPDATED_IP_ADDRESSES);
        assertThat(testAgent.getLogo()).isEqualTo(UPDATED_LOGO);
        assertThat(testAgent.getLogoContentType()).isEqualTo(UPDATED_LOGO_CONTENT_TYPE);
        assertThat(testAgent.getOrd()).isEqualTo(UPDATED_ORD);
        assertThat(testAgent.getActive()).isEqualTo(UPDATED_ACTIVE);
        assertThat(testAgent.getPhoneNumber()).isEqualTo(UPDATED_PHONE_NUMBER);
        assertThat(testAgent.getInn()).isEqualTo(UPDATED_INN);
        assertThat(testAgent.getOrgName()).isEqualTo(UPDATED_ORG_NAME);
    }

    @Test
    @Transactional
    void patchNonExistingAgent() throws Exception {
        int databaseSizeBeforeUpdate = agentRepository.findAll().size();
        agent.setId(count.incrementAndGet());

        // Create the Agent
        AgentDTO agentDTO = agentMapper.toDto(agent);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restAgentMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, agentDTO.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(agentDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Agent in the database
        List<Agent> agentList = agentRepository.findAll();
        assertThat(agentList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchAgent() throws Exception {
        int databaseSizeBeforeUpdate = agentRepository.findAll().size();
        agent.setId(count.incrementAndGet());

        // Create the Agent
        AgentDTO agentDTO = agentMapper.toDto(agent);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restAgentMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, count.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(agentDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Agent in the database
        List<Agent> agentList = agentRepository.findAll();
        assertThat(agentList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamAgent() throws Exception {
        int databaseSizeBeforeUpdate = agentRepository.findAll().size();
        agent.setId(count.incrementAndGet());

        // Create the Agent
        AgentDTO agentDTO = agentMapper.toDto(agent);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restAgentMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(TestUtil.convertObjectToJsonBytes(agentDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Agent in the database
        List<Agent> agentList = agentRepository.findAll();
        assertThat(agentList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteAgent() throws Exception {
        // Initialize the database
        agentRepository.saveAndFlush(agent);

        int databaseSizeBeforeDelete = agentRepository.findAll().size();

        // Delete the agent
        restAgentMockMvc
            .perform(delete("/api/agents/hard/{id}", agent.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        List<Agent> agentList = agentRepository.findAll();
        assertThat(agentList).hasSize(databaseSizeBeforeDelete - 1);
//        Agent deletedAgent = agentList.iterator().next();
//        assertThat(deletedAgent).isNotNull();
//        assertThat(deletedAgent.getState()).isEqualTo(AgentState.DELETED);
    }
}
