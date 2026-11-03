package uz.paykap.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static uz.paykap.web.rest.TestUtil.createFormattingConversionService;

import java.util.*;
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
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Base64Utils;
import org.springframework.validation.Validator;
import uz.paykap.PkbillmsApp;
import uz.paykap.IntegrationTest;
import uz.paykap.domain.Agent;
import uz.paykap.domain.Deposit;
import uz.paykap.domain.I18n;
import uz.paykap.repository.AgentRepository;
import uz.paykap.service.AgentQueryService;
import uz.paykap.service.AgentService;
import uz.paykap.service.dto.AgentDTO;
import uz.paykap.service.mapper.AgentMapper;
import uz.paykap.web.rest.errors.ExceptionTranslator;

/**
 * Test class for the AgentResource REST controller.
 *
 * @see AgentResource
 */
@RunWith(SpringRunner.class)
@IntegrationTest
public class AgentResourceIntTest {

    private static final String DEFAULT_SHORT_NAME = "AAAAAAAAAA";
    private static final String UPDATED_SHORT_NAME = "BBBBBBBBBB";

    private static final Integer DEFAULT_AMOUNT_RATIO = 1;
    private static final Integer UPDATED_AMOUNT_RATIO = 2;

    private static final String DEFAULT_API_KEY = "AAAAAAAAAA";
    private static final String UPDATED_API_KEY = "BBBBBBBBBB";

    private static final Set<Integer> DEFAULT_CURRENCIES = new HashSet<>() {
        {
            add(860);
        }
    };
    private static final Set<Integer> UPDATED_CURRENCIES = new HashSet<>() {
        {
            add(860);
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

    private static final Boolean DEFAULT_ACTIVE = false;
    private static final Boolean UPDATED_ACTIVE = true;

    private static final String DEFAULT_PHONE_NUMBER = "AAAAAAAAAA";
    private static final String UPDATED_PHONE_NUMBER = "BBBBBBBBBB";

    private static final String DEFAULT_INN = "AAAAAAAAAA";
    private static final String UPDATED_INN = "BBBBBBBBBB";

    private static final String DEFAULT_ORG_NAME = "AAAAAAAAAA";
    private static final String UPDATED_ORG_NAME = "BBBBBBBBBB";

    @Autowired
    private AgentRepository agentRepository;

    @Mock
    private AgentRepository agentRepositoryMock;

    @Autowired
    private AgentMapper agentMapper;

    @Mock
    private AgentService agentServiceMock;

    @Autowired
    private AgentService agentService;

    @Autowired
    private AgentQueryService agentQueryService;

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

    private MockMvc restAgentMockMvc;

    private Agent agent;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        final AgentResource agentResource = new AgentResource(agentService, agentRepository, agentQueryService);
        this.restAgentMockMvc =
            MockMvcBuilders
                .standaloneSetup(agentResource)
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
        //        I18n i18n = I18nResourceIntTest.createEntity(em);
        //        em.persist(i18n);
        //        em.flush();
        //        agent.getTitles().add(i18n);
        return agent;
    }

    @Before
    public void initTest() {
        agent = createEntity(em);
    }

    @Test
    @Transactional
    public void createAgent() throws Exception {
        int databaseSizeBeforeCreate = agentRepository.findAll().size();

        // Create the Agent
        AgentDTO agentDTO = agentMapper.toDto(agent);
        restAgentMockMvc
            .perform(post("/api/agents").contentType(TestUtil.APPLICATION_JSON_UTF8).content(TestUtil.convertObjectToJsonBytes(agentDTO)))
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
        assertThat(testAgent.isActive()).isEqualTo(DEFAULT_ACTIVE);
        assertThat(testAgent.getPhoneNumber()).isEqualTo(DEFAULT_PHONE_NUMBER);
        assertThat(testAgent.getInn()).isEqualTo(DEFAULT_INN);
        assertThat(testAgent.getOrgName()).isEqualTo(DEFAULT_ORG_NAME);
    }

    @Test
    @Transactional
    public void createAgentWithExistingId() throws Exception {
        int databaseSizeBeforeCreate = agentRepository.findAll().size();

        // Create the Agent with an existing ID
        agent.setId(1L);
        AgentDTO agentDTO = agentMapper.toDto(agent);

        // An entity with an existing ID cannot be created, so this API call must fail
        restAgentMockMvc
            .perform(post("/api/agents").contentType(TestUtil.APPLICATION_JSON_UTF8).content(TestUtil.convertObjectToJsonBytes(agentDTO)))
            .andExpect(status().isBadRequest());

        // Validate the Agent in the database
        List<Agent> agentList = agentRepository.findAll();
        assertThat(agentList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    public void checkShortNameIsRequired() throws Exception {
        int databaseSizeBeforeTest = agentRepository.findAll().size();
        // set the field null
        agent.setShortName(null);

        // Create the Agent, which fails.
        AgentDTO agentDTO = agentMapper.toDto(agent);

        restAgentMockMvc
            .perform(post("/api/agents").contentType(TestUtil.APPLICATION_JSON_UTF8).content(TestUtil.convertObjectToJsonBytes(agentDTO)))
            .andExpect(status().isBadRequest());

        List<Agent> agentList = agentRepository.findAll();
        assertThat(agentList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkAmountRatioIsRequired() throws Exception {
        int databaseSizeBeforeTest = agentRepository.findAll().size();
        // set the field null
        agent.setAmountRatio(null);

        // Create the Agent, which fails.
        AgentDTO agentDTO = agentMapper.toDto(agent);

        restAgentMockMvc
            .perform(post("/api/agents").contentType(TestUtil.APPLICATION_JSON_UTF8).content(TestUtil.convertObjectToJsonBytes(agentDTO)))
            .andExpect(status().isBadRequest());

        List<Agent> agentList = agentRepository.findAll();
        assertThat(agentList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkApiKeyIsRequired() throws Exception {
        int databaseSizeBeforeTest = agentRepository.findAll().size();
        // set the field null
        agent.setApiKey(null);

        // Create the Agent, which fails.
        AgentDTO agentDTO = agentMapper.toDto(agent);

        restAgentMockMvc
            .perform(post("/api/agents").contentType(TestUtil.APPLICATION_JSON_UTF8).content(TestUtil.convertObjectToJsonBytes(agentDTO)))
            .andExpect(status().isBadRequest());

        List<Agent> agentList = agentRepository.findAll();
        assertThat(agentList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkCurrenciesIsRequired() throws Exception {
        int databaseSizeBeforeTest = agentRepository.findAll().size();
        // set the field null
        agent.setCurrencies(null);

        // Create the Agent, which fails.
        AgentDTO agentDTO = agentMapper.toDto(agent);

        restAgentMockMvc
            .perform(post("/api/agents").contentType(TestUtil.APPLICATION_JSON_UTF8).content(TestUtil.convertObjectToJsonBytes(agentDTO)))
            .andExpect(status().isBadRequest());

        List<Agent> agentList = agentRepository.findAll();
        assertThat(agentList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkActiveIsRequired() throws Exception {
        int databaseSizeBeforeTest = agentRepository.findAll().size();
        // set the field null
        agent.setActive(null);

        // Create the Agent, which fails.
        AgentDTO agentDTO = agentMapper.toDto(agent);

        restAgentMockMvc
            .perform(post("/api/agents").contentType(TestUtil.APPLICATION_JSON_UTF8).content(TestUtil.convertObjectToJsonBytes(agentDTO)))
            .andExpect(status().isBadRequest());

        List<Agent> agentList = agentRepository.findAll();
        assertThat(agentList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void getAllAgents() throws Exception {
        // Initialize the database
        agentRepository.saveAndFlush(agent);

        // Get all the agentList
        restAgentMockMvc
            .perform(get("/api/agents?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
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
            .andExpect(jsonPath("$.[*].phoneNumber").value(hasItem(DEFAULT_PHONE_NUMBER.toString())))
            .andExpect(jsonPath("$.[*].inn").value(hasItem(DEFAULT_INN.toString())))
            .andExpect(jsonPath("$.[*].orgName").value(hasItem(DEFAULT_ORG_NAME.toString())));
    }

    @SuppressWarnings({ "unchecked" })
    public void getAllAgentsWithEagerRelationshipsIsEnabled() throws Exception {
        AgentResource agentResource = new AgentResource(agentServiceMock, agentRepository, agentQueryService);
        when(agentServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        MockMvc restAgentMockMvc = MockMvcBuilders
            .standaloneSetup(agentResource)
            .setCustomArgumentResolvers(pageableArgumentResolver)
            .setControllerAdvice(exceptionTranslator)
            .setConversionService(createFormattingConversionService())
            .setMessageConverters(jacksonMessageConverter)
            .build();

        restAgentMockMvc.perform(get("/api/agents?eagerload=true")).andExpect(status().isOk());

        verify(agentServiceMock, times(1)).findAllWithEagerRelationships(any());
    }

    @SuppressWarnings({ "unchecked" })
    public void getAllAgentsWithEagerRelationshipsIsNotEnabled() throws Exception {
        AgentResource agentResource = new AgentResource(agentServiceMock, agentRepository, agentQueryService);
        when(agentServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));
        MockMvc restAgentMockMvc = MockMvcBuilders
            .standaloneSetup(agentResource)
            .setCustomArgumentResolvers(pageableArgumentResolver)
            .setControllerAdvice(exceptionTranslator)
            .setConversionService(createFormattingConversionService())
            .setMessageConverters(jacksonMessageConverter)
            .build();

        restAgentMockMvc.perform(get("/api/agents?eagerload=true")).andExpect(status().isOk());

        verify(agentServiceMock, times(1)).findAllWithEagerRelationships(any());
    }

    @Test
    @Transactional
    public void getAgent() throws Exception {
        // Initialize the database
        agentRepository.saveAndFlush(agent);

        // Get the agent
        restAgentMockMvc
            .perform(get("/api/agents/{id}", agent.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.id").value(agent.getId().intValue()))
            .andExpect(jsonPath("$.shortName").value(DEFAULT_SHORT_NAME.toString()))
            .andExpect(jsonPath("$.amountRatio").value(DEFAULT_AMOUNT_RATIO))
            .andExpect(jsonPath("$.apiKey").value(DEFAULT_API_KEY))
            .andExpect(jsonPath("$.currencies.[0]").value(DEFAULT_CURRENCIES.toArray()[0]))
            .andExpect(jsonPath("$.ipAddresses.[0]").value(DEFAULT_IP_ADDRESSES.toArray()[0]))
            .andExpect(jsonPath("$.logoContentType").value(DEFAULT_LOGO_CONTENT_TYPE))
            .andExpect(jsonPath("$.logo").value(Base64Utils.encodeToString(DEFAULT_LOGO)))
            .andExpect(jsonPath("$.ord").value(DEFAULT_ORD))
            .andExpect(jsonPath("$.active").value(DEFAULT_ACTIVE.booleanValue()))
            .andExpect(jsonPath("$.phoneNumber").value(DEFAULT_PHONE_NUMBER.toString()))
            .andExpect(jsonPath("$.inn").value(DEFAULT_INN.toString()))
            .andExpect(jsonPath("$.orgName").value(DEFAULT_ORG_NAME.toString()));
    }

    @Test
    @Transactional
    public void getAllAgentsByShortNameIsEqualToSomething() throws Exception {
        // Initialize the database
        agentRepository.saveAndFlush(agent);

        // Get all the agentList where shortName equals to DEFAULT_SHORT_NAME
        defaultAgentShouldBeFound("shortName.equals=" + DEFAULT_SHORT_NAME);

        // Get all the agentList where shortName equals to UPDATED_SHORT_NAME
        defaultAgentShouldNotBeFound("shortName.equals=" + UPDATED_SHORT_NAME);
    }

    @Test
    @Transactional
    public void getAllAgentsByShortNameIsInShouldWork() throws Exception {
        // Initialize the database
        agentRepository.saveAndFlush(agent);

        // Get all the agentList where shortName in DEFAULT_SHORT_NAME or UPDATED_SHORT_NAME
        defaultAgentShouldBeFound("shortName.in=" + DEFAULT_SHORT_NAME + "," + UPDATED_SHORT_NAME);

        // Get all the agentList where shortName equals to UPDATED_SHORT_NAME
        defaultAgentShouldNotBeFound("shortName.in=" + UPDATED_SHORT_NAME);
    }

    @Test
    @Transactional
    public void getAllAgentsByShortNameIsNullOrNotNull() throws Exception {
        // Initialize the database
        agentRepository.saveAndFlush(agent);

        // Get all the agentList where shortName is not null
        defaultAgentShouldBeFound("shortName.specified=true");

        // Get all the agentList where shortName is null
        defaultAgentShouldNotBeFound("shortName.specified=false");
    }

    @Test
    @Transactional
    public void getAllAgentsByAmountRatioIsEqualToSomething() throws Exception {
        // Initialize the database
        agentRepository.saveAndFlush(agent);

        // Get all the agentList where amountRatio equals to DEFAULT_AMOUNT_RATIO
        defaultAgentShouldBeFound("amountRatio.equals=" + DEFAULT_AMOUNT_RATIO);

        // Get all the agentList where amountRatio equals to UPDATED_AMOUNT_RATIO
        defaultAgentShouldNotBeFound("amountRatio.equals=" + UPDATED_AMOUNT_RATIO);
    }

    @Test
    @Transactional
    public void getAllAgentsByAmountRatioIsInShouldWork() throws Exception {
        // Initialize the database
        agentRepository.saveAndFlush(agent);

        // Get all the agentList where amountRatio in DEFAULT_AMOUNT_RATIO or UPDATED_AMOUNT_RATIO
        defaultAgentShouldBeFound("amountRatio.in=" + DEFAULT_AMOUNT_RATIO + "," + UPDATED_AMOUNT_RATIO);

        // Get all the agentList where amountRatio equals to UPDATED_AMOUNT_RATIO
        defaultAgentShouldNotBeFound("amountRatio.in=" + UPDATED_AMOUNT_RATIO);
    }

    @Test
    @Transactional
    public void getAllAgentsByAmountRatioIsNullOrNotNull() throws Exception {
        // Initialize the database
        agentRepository.saveAndFlush(agent);

        // Get all the agentList where amountRatio is not null
        defaultAgentShouldBeFound("amountRatio.specified=true");

        // Get all the agentList where amountRatio is null
        defaultAgentShouldNotBeFound("amountRatio.specified=false");
    }

    @Test
    @Transactional
    public void getAllAgentsByAmountRatioIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        agentRepository.saveAndFlush(agent);

        // Get all the agentList where amountRatio greater than or equals to DEFAULT_AMOUNT_RATIO
        defaultAgentShouldBeFound("amountRatio.greaterOrEqualThan=" + DEFAULT_AMOUNT_RATIO);
        // Get all the agentList where amountRatio greater than or equals to UPDATED_AMOUNT_RATIO
        //        defaultAgentShouldNotBeFound("amountRatio.greaterOrEqualThan=" + UPDATED_AMOUNT_RATIO);
    }

    @Test
    @Transactional
    public void getAllAgentsByAmountRatioIsLessThanSomething() throws Exception {
        // Initialize the database
        agentRepository.saveAndFlush(agent);

        // Get all the agentList where amountRatio less than or equals to DEFAULT_AMOUNT_RATIO
        defaultAgentShouldNotBeFound("amountRatio.lessThan=" + DEFAULT_AMOUNT_RATIO);

        // Get all the agentList where amountRatio less than or equals to UPDATED_AMOUNT_RATIO
        defaultAgentShouldBeFound("amountRatio.lessThan=" + UPDATED_AMOUNT_RATIO);
    }

    @Test
    @Transactional
    public void getAllAgentsByApiKeyIsEqualToSomething() throws Exception {
        // Initialize the database
        agentRepository.saveAndFlush(agent);

        // Get all the agentList where apiKey equals to DEFAULT_API_KEY
        defaultAgentShouldBeFound("apiKey.equals=" + DEFAULT_API_KEY);

        // Get all the agentList where apiKey equals to UPDATED_API_KEY
        defaultAgentShouldNotBeFound("apiKey.equals=" + UPDATED_API_KEY);
    }

    @Test
    @Transactional
    public void getAllAgentsByApiKeyIsInShouldWork() throws Exception {
        // Initialize the database
        agentRepository.saveAndFlush(agent);

        // Get all the agentList where apiKey in DEFAULT_API_KEY or UPDATED_API_KEY
        defaultAgentShouldBeFound("apiKey.in=" + DEFAULT_API_KEY + "," + UPDATED_API_KEY);

        // Get all the agentList where apiKey equals to UPDATED_API_KEY
        defaultAgentShouldNotBeFound("apiKey.in=" + UPDATED_API_KEY);
    }

    @Test
    @Transactional
    public void getAllAgentsByApiKeyIsNullOrNotNull() throws Exception {
        // Initialize the database
        agentRepository.saveAndFlush(agent);

        // Get all the agentList where apiKey is not null
        defaultAgentShouldBeFound("apiKey.specified=true");

        // Get all the agentList where apiKey is null
        defaultAgentShouldNotBeFound("apiKey.specified=false");
    }

    @Test
    @Transactional
    public void getAllAgentsByCurrenciesIsEqualToSomething() throws Exception {
        // Initialize the database
        agentRepository.saveAndFlush(agent);

        // Get all the agentList where currencies equals to DEFAULT_CURRENCIES
        defaultAgentShouldBeFound("currencies.equals=" + DEFAULT_CURRENCIES);
        // Get all the agentList where currencies equals to UPDATED_CURRENCIES
        //        defaultAgentShouldNotBeFound("currencies.equals=" + UPDATED_CURRENCIES);
    }

    @Test
    @Transactional
    public void getAllAgentsByCurrenciesIsInShouldWork() throws Exception {
        // Initialize the database
        agentRepository.saveAndFlush(agent);

        // Get all the agentList where currencies in DEFAULT_CURRENCIES or UPDATED_CURRENCIES
        defaultAgentShouldBeFound("currencies.in=" + DEFAULT_CURRENCIES + "," + UPDATED_CURRENCIES);
        // Get all the agentList where currencies equals to UPDATED_CURRENCIES
        //        defaultAgentShouldNotBeFound("currencies.in=" + UPDATED_CURRENCIES);
    }

    @Test
    @Transactional
    public void getAllAgentsByCurrenciesIsNullOrNotNull() throws Exception {
        // Initialize the database
        agentRepository.saveAndFlush(agent);

        // Get all the agentList where currencies is not null
        defaultAgentShouldBeFound("currencies.specified=true");
        // Get all the agentList where currencies is null
        //        defaultAgentShouldNotBeFound("currencies.specified=false");
    }

    @Test
    @Transactional
    public void getAllAgentsByIpAddressesIsEqualToSomething() throws Exception {
        // Initialize the database
        agentRepository.saveAndFlush(agent);

        // Get all the agentList where ipAddresses equals to DEFAULT_IP_ADDRESSES
        defaultAgentShouldBeFound("ipAddresses.equals=" + DEFAULT_IP_ADDRESSES);
        // Get all the agentList where ipAddresses equals to UPDATED_IP_ADDRESSES
        //        defaultAgentShouldNotBeFound("ipAddresses.equals=" + UPDATED_IP_ADDRESSES);
    }

    @Test
    @Transactional
    public void getAllAgentsByIpAddressesIsInShouldWork() throws Exception {
        // Initialize the database
        agentRepository.saveAndFlush(agent);

        // Get all the agentList where ipAddresses in DEFAULT_IP_ADDRESSES or UPDATED_IP_ADDRESSES
        defaultAgentShouldBeFound("ipAddresses.in=" + DEFAULT_IP_ADDRESSES + "," + UPDATED_IP_ADDRESSES);
        // Get all the agentList where ipAddresses equals to UPDATED_IP_ADDRESSES
        //        defaultAgentShouldNotBeFound("ipAddresses.in=" + UPDATED_IP_ADDRESSES);
    }

    @Test
    @Transactional
    public void getAllAgentsByIpAddressesIsNullOrNotNull() throws Exception {
        // Initialize the database
        agentRepository.saveAndFlush(agent);

        // Get all the agentList where ipAddresses is not null
        defaultAgentShouldBeFound("ipAddresses.specified=true");
        // Get all the agentList where ipAddresses is null
        //        defaultAgentShouldNotBeFound("ipAddresses.specified=false");
    }

    @Test
    @Transactional
    public void getAllAgentsByOrdIsEqualToSomething() throws Exception {
        // Initialize the database
        agentRepository.saveAndFlush(agent);

        // Get all the agentList where ord equals to DEFAULT_ORD
        defaultAgentShouldBeFound("ord.equals=" + DEFAULT_ORD);

        // Get all the agentList where ord equals to UPDATED_ORD
        defaultAgentShouldNotBeFound("ord.equals=" + UPDATED_ORD);
    }

    @Test
    @Transactional
    public void getAllAgentsByOrdIsInShouldWork() throws Exception {
        // Initialize the database
        agentRepository.saveAndFlush(agent);

        // Get all the agentList where ord in DEFAULT_ORD or UPDATED_ORD
        defaultAgentShouldBeFound("ord.in=" + DEFAULT_ORD + "," + UPDATED_ORD);

        // Get all the agentList where ord equals to UPDATED_ORD
        defaultAgentShouldNotBeFound("ord.in=" + UPDATED_ORD);
    }

    @Test
    @Transactional
    public void getAllAgentsByOrdIsNullOrNotNull() throws Exception {
        // Initialize the database
        agentRepository.saveAndFlush(agent);

        // Get all the agentList where ord is not null
        defaultAgentShouldBeFound("ord.specified=true");
        // Get all the agentList where ord is null
        //        defaultAgentShouldNotBeFound("ord.specified=false");
    }

    @Test
    @Transactional
    public void getAllAgentsByOrdIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        agentRepository.saveAndFlush(agent);

        // Get all the agentList where ord greater than or equals to DEFAULT_ORD
        defaultAgentShouldBeFound("ord.greaterOrEqualThan=" + DEFAULT_ORD);

        // Get all the agentList where ord greater than or equals to UPDATED_ORD
        defaultAgentShouldNotBeFound("ord.greaterOrEqualThan=" + UPDATED_ORD);
    }

    @Test
    @Transactional
    public void getAllAgentsByOrdIsLessThanSomething() throws Exception {
        // Initialize the database
        agentRepository.saveAndFlush(agent);

        // Get all the agentList where ord less than or equals to DEFAULT_ORD
        defaultAgentShouldNotBeFound("ord.lessThan=" + DEFAULT_ORD);

        // Get all the agentList where ord less than or equals to UPDATED_ORD
        defaultAgentShouldBeFound("ord.lessThan=" + UPDATED_ORD);
    }

    @Test
    @Transactional
    public void getAllAgentsByActiveIsEqualToSomething() throws Exception {
        // Initialize the database
        agentRepository.saveAndFlush(agent);

        // Get all the agentList where active equals to DEFAULT_ACTIVE
        defaultAgentShouldBeFound("active.equals=" + DEFAULT_ACTIVE);
        // Get all the agentList where active equals to UPDATED_ACTIVE
        //        defaultAgentShouldNotBeFound("active.equals=" + UPDATED_ACTIVE);
    }

    @Test
    @Transactional
    public void getAllAgentsByActiveIsInShouldWork() throws Exception {
        // Initialize the database
        agentRepository.saveAndFlush(agent);

        // Get all the agentList where active in DEFAULT_ACTIVE or UPDATED_ACTIVE
        defaultAgentShouldBeFound("active.in=" + DEFAULT_ACTIVE + "," + UPDATED_ACTIVE);
        // Get all the agentList where active equals to UPDATED_ACTIVE
        //        defaultAgentShouldNotBeFound("active.in=" + UPDATED_ACTIVE);
    }

    @Test
    @Transactional
    public void getAllAgentsByActiveIsNullOrNotNull() throws Exception {
        // Initialize the database
        agentRepository.saveAndFlush(agent);

        // Get all the agentList where active is not null
        defaultAgentShouldBeFound("active.specified=true");

        // Get all the agentList where active is null
        defaultAgentShouldNotBeFound("active.specified=false");
    }

    @Test
    @Transactional
    public void getAllAgentsByPhoneNumberIsEqualToSomething() throws Exception {
        // Initialize the database
        agentRepository.saveAndFlush(agent);

        // Get all the agentList where phoneNumber equals to DEFAULT_PHONE_NUMBER
        defaultAgentShouldBeFound("phoneNumber.equals=" + DEFAULT_PHONE_NUMBER);
        // Get all the agentList where phoneNumber equals to UPDATED_PHONE_NUMBER
        //        defaultAgentShouldNotBeFound("phoneNumber.equals=" + UPDATED_PHONE_NUMBER);
    }

    @Test
    @Transactional
    public void getAllAgentsByPhoneNumberIsInShouldWork() throws Exception {
        // Initialize the database
        agentRepository.saveAndFlush(agent);

        // Get all the agentList where phoneNumber in DEFAULT_PHONE_NUMBER or UPDATED_PHONE_NUMBER
        defaultAgentShouldBeFound("phoneNumber.in=" + DEFAULT_PHONE_NUMBER + "," + UPDATED_PHONE_NUMBER);
        // Get all the agentList where phoneNumber equals to UPDATED_PHONE_NUMBER
        //        defaultAgentShouldNotBeFound("phoneNumber.in=" + UPDATED_PHONE_NUMBER);
    }

    @Test
    @Transactional
    public void getAllAgentsByPhoneNumberIsNullOrNotNull() throws Exception {
        // Initialize the database
        agentRepository.saveAndFlush(agent);

        // Get all the agentList where phoneNumber is not null
        defaultAgentShouldBeFound("phoneNumber.specified=true");
        // Get all the agentList where phoneNumber is null
        //        defaultAgentShouldNotBeFound("phoneNumber.specified=false");
    }

    @Test
    @Transactional
    public void getAllAgentsByPhoneNumberIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        agentRepository.saveAndFlush(agent);

        // Get all the agentList where phoneNumber greater than or equals to DEFAULT_PHONE_NUMBER
        defaultAgentShouldBeFound("phoneNumber.greaterOrEqualThan=" + DEFAULT_PHONE_NUMBER);
        // Get all the agentList where phoneNumber greater than or equals to UPDATED_PHONE_NUMBER
        //        defaultAgentShouldNotBeFound("phoneNumber.greaterOrEqualThan=" + UPDATED_PHONE_NUMBER);
    }

    //    @Test
    @Transactional
    public void getAllAgentsByPhoneNumberIsLessThanSomething() throws Exception {
        // Initialize the database
        agentRepository.saveAndFlush(agent);

        // Get all the agentList where phoneNumber less than or equals to DEFAULT_PHONE_NUMBER
        defaultAgentShouldNotBeFound("phoneNumber.lessThan=" + DEFAULT_PHONE_NUMBER);

        // Get all the agentList where phoneNumber less than or equals to UPDATED_PHONE_NUMBER
        defaultAgentShouldBeFound("phoneNumber.lessThan=" + UPDATED_PHONE_NUMBER);
    }

    @Test
    @Transactional
    public void getAllAgentsByInnIsEqualToSomething() throws Exception {
        // Initialize the database
        agentRepository.saveAndFlush(agent);

        // Get all the agentList where inn equals to DEFAULT_INN
        defaultAgentShouldBeFound("inn.equals=" + DEFAULT_INN);
        // Get all the agentList where inn equals to UPDATED_INN
        //        defaultAgentShouldNotBeFound("inn.equals=" + UPDATED_INN);
    }

    @Test
    @Transactional
    public void getAllAgentsByInnIsInShouldWork() throws Exception {
        // Initialize the database
        agentRepository.saveAndFlush(agent);

        // Get all the agentList where inn in DEFAULT_INN or UPDATED_INN
        defaultAgentShouldBeFound("inn.in=" + DEFAULT_INN + "," + UPDATED_INN);
        // Get all the agentList where inn equals to UPDATED_INN
        //        defaultAgentShouldNotBeFound("inn.in=" + UPDATED_INN);
    }

    @Test
    @Transactional
    public void getAllAgentsByInnIsNullOrNotNull() throws Exception {
        // Initialize the database
        agentRepository.saveAndFlush(agent);

        // Get all the agentList where inn is not null
        defaultAgentShouldBeFound("inn.specified=true");
        // Get all the agentList where inn is null
        //        defaultAgentShouldNotBeFound("inn.specified=false");
    }

    @Test
    @Transactional
    public void getAllAgentsByOrgNameIsEqualToSomething() throws Exception {
        // Initialize the database
        agentRepository.saveAndFlush(agent);

        // Get all the agentList where orgName equals to DEFAULT_ORG_NAME
        defaultAgentShouldBeFound("orgName.equals=" + DEFAULT_ORG_NAME);
        // Get all the agentList where orgName equals to UPDATED_ORG_NAME
        //        defaultAgentShouldNotBeFound("orgName.equals=" + UPDATED_ORG_NAME);
    }

    @Test
    @Transactional
    public void getAllAgentsByOrgNameIsInShouldWork() throws Exception {
        // Initialize the database
        agentRepository.saveAndFlush(agent);

        // Get all the agentList where orgName in DEFAULT_ORG_NAME or UPDATED_ORG_NAME
        defaultAgentShouldBeFound("orgName.in=" + DEFAULT_ORG_NAME + "," + UPDATED_ORG_NAME);
        // Get all the agentList where orgName equals to UPDATED_ORG_NAME
        //        defaultAgentShouldNotBeFound("orgName.in=" + UPDATED_ORG_NAME);
    }

    @Test
    @Transactional
    public void getAllAgentsByOrgNameIsNullOrNotNull() throws Exception {
        // Initialize the database
        agentRepository.saveAndFlush(agent);

        // Get all the agentList where orgName is not null
        defaultAgentShouldBeFound("orgName.specified=true");
        // Get all the agentList where orgName is null
        //        defaultAgentShouldNotBeFound("orgName.specified=false");
    }

    @Test
    @Transactional
    public void getAllAgentsByTitleIsEqualToSomething() throws Exception {
        // Initialize the database
        I18n title = I18nResourceIntTest.createEntity(em);
        em.persist(title);
        em.flush();
        agent.addTitle(title);
        agentRepository.saveAndFlush(agent);
        Long titleId = title.getId();

        // Get all the agentList where title equals to titleId
        defaultAgentShouldBeFound("titleId.equals=" + titleId);

        // Get all the agentList where title equals to titleId + 1
        defaultAgentShouldNotBeFound("titleId.equals=" + (titleId + 1));
    }

    @Test
    @Transactional
    public void getAllAgentsByDepositIsEqualToSomething() throws Exception {
        // Initialize the database
        Deposit deposit = DepositResourceIntTest.createEntity(em);
        em.persist(deposit);
        em.flush();
        agent.setDeposit(deposit);
        deposit.setAgent(agent);
        agentRepository.saveAndFlush(agent);
        Long depositId = deposit.getId();

        // Get all the agentList where deposit equals to depositId
        defaultAgentShouldBeFound("depositId.equals=" + depositId);

        // Get all the agentList where deposit equals to depositId + 1
        defaultAgentShouldNotBeFound("depositId.equals=" + (depositId + 1));
    }

    /**
     * Executes the search, and checks that the default entity is returned
     */
    private void defaultAgentShouldBeFound(String filter) throws Exception {
        restAgentMockMvc
            .perform(get("/api/agents?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(agent.getId().intValue())))
            .andExpect(jsonPath("$.[*].shortName").value(hasItem(DEFAULT_SHORT_NAME)))
            .andExpect(jsonPath("$.[*].amountRatio").value(hasItem(DEFAULT_AMOUNT_RATIO)))
            .andExpect(jsonPath("$.[*].apiKey").value(hasItem(DEFAULT_API_KEY)))
            .andExpect(jsonPath("$.[*].currencies").value(hasSize(1)))
            .andExpect(jsonPath("$.[*].currencies[0]").value(hasItem(DEFAULT_CURRENCIES.toArray()[0])))
            .andExpect(jsonPath("$.[*].ipAddresses").value(hasSize(1)))
            .andExpect(jsonPath("$.[*].ipAddresses[0]").value(DEFAULT_IP_ADDRESSES.toArray()[0]))
            .andExpect(jsonPath("$.[*].logoContentType").value(hasItem(DEFAULT_LOGO_CONTENT_TYPE)))
            .andExpect(jsonPath("$.[*].logo").value(hasItem(Base64Utils.encodeToString(DEFAULT_LOGO))))
            .andExpect(jsonPath("$.[*].ord").value(hasItem(DEFAULT_ORD)))
            .andExpect(jsonPath("$.[*].active").value(hasItem(DEFAULT_ACTIVE.booleanValue())))
            .andExpect(jsonPath("$.[*].phoneNumber").value(hasItem(DEFAULT_PHONE_NUMBER.toString())))
            .andExpect(jsonPath("$.[*].inn").value(hasItem(DEFAULT_INN.toString())))
            .andExpect(jsonPath("$.[*].orgName").value(hasItem(DEFAULT_ORG_NAME.toString())));
    }

    /**
     * Executes the search, and checks that the default entity is not returned
     */
    private void defaultAgentShouldNotBeFound(String filter) throws Exception {
        restAgentMockMvc
            .perform(get("/api/agents?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty());

        // Check, that the count call also returns 0
        restAgentMockMvc
            .perform(get("/api/agents/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(content().string("0"));
    }

    @Test
    @Transactional
    public void getNonExistingAgent() throws Exception {
        // Get the agent
        restAgentMockMvc.perform(get("/api/agents/{id}", Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updateAgent() throws Exception {
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
            .perform(put("/api/agents").contentType(TestUtil.APPLICATION_JSON_UTF8).content(TestUtil.convertObjectToJsonBytes(agentDTO)))
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
        assertThat(testAgent.isActive()).isEqualTo(UPDATED_ACTIVE);
        assertThat(testAgent.getPhoneNumber()).isEqualTo(UPDATED_PHONE_NUMBER);
        assertThat(testAgent.getInn()).isEqualTo(UPDATED_INN);
        assertThat(testAgent.getOrgName()).isEqualTo(UPDATED_ORG_NAME);
    }

    @Test
    @Transactional
    public void updateNonExistingAgent() throws Exception {
        int databaseSizeBeforeUpdate = agentRepository.findAll().size();

        // Create the Agent
        AgentDTO agentDTO = agentMapper.toDto(agent);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restAgentMockMvc
            .perform(put("/api/agents").contentType(TestUtil.APPLICATION_JSON_UTF8).content(TestUtil.convertObjectToJsonBytes(agentDTO)))
            .andExpect(status().isBadRequest());

        // Validate the Agent in the database
        List<Agent> agentList = agentRepository.findAll();
        assertThat(agentList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    public void deleteAgent() throws Exception {
        // Initialize the database
        agentRepository.saveAndFlush(agent);

        int databaseSizeBeforeDelete = agentRepository.findAll().size();

        // Get the agent
        restAgentMockMvc
            .perform(delete("/api/agents/{id}", agent.getId()).accept(TestUtil.APPLICATION_JSON_UTF8))
            .andExpect(status().isOk());

        // Validate the database is empty
        List<Agent> agentList = agentRepository.findAll();
        assertThat(agentList).hasSize(databaseSizeBeforeDelete - 1);
    }

    @Test
    @Transactional
    public void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Agent.class);
        Agent agent1 = new Agent();
        agent1.setId(1L);
        Agent agent2 = new Agent();
        agent2.setId(agent1.getId());
        assertThat(agent1).isEqualTo(agent2);
        agent2.setId(2L);
        assertThat(agent1).isNotEqualTo(agent2);
        agent1.setId(null);
        assertThat(agent1).isNotEqualTo(agent2);
    }

    @Test
    @Transactional
    public void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(AgentDTO.class);
        AgentDTO agentDTO1 = new AgentDTO();
        agentDTO1.setId(1L);
        AgentDTO agentDTO2 = new AgentDTO();
        assertThat(agentDTO1).isNotEqualTo(agentDTO2);
        agentDTO2.setId(agentDTO1.getId());
        assertThat(agentDTO1).isEqualTo(agentDTO2);
        agentDTO2.setId(2L);
        assertThat(agentDTO1).isNotEqualTo(agentDTO2);
        agentDTO1.setId(null);
        assertThat(agentDTO1).isNotEqualTo(agentDTO2);
    }

    @Test
    @Transactional
    public void testEntityFromId() {
        assertThat(agentMapper.fromId(42L).getId()).isEqualTo(42);
        assertThat(agentMapper.fromId(null)).isNull();
    }
}
