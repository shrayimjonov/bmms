//package uz.paykap.web.rest;
//
//import static org.assertj.core.api.Assertions.assertThat;
//import static org.hamcrest.Matchers.hasItem;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
//
//import java.util.List;
//import java.util.Random;
//import java.util.concurrent.atomic.AtomicLong;
//import javax.persistence.EntityManager;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
//import org.springframework.http.MediaType;
//import org.springframework.security.test.context.support.WithMockUser;
//import org.springframework.test.web.servlet.MockMvc;
//import org.springframework.transaction.annotation.Transactional;
//import uz.paykap.IntegrationTest;
//import uz.paykap.domain.Agent;
//import uz.paykap.domain.AgentParams;
//import uz.paykap.repository.AgentParamsRepository;
//import uz.paykap.service.criteria.AgentParamsCriteria;
//import uz.paykap.service.dto.AgentParamsDTO;
//import uz.paykap.service.mapper.AgentParamsMapper;
//
///**
// * Integration tests for the {@link AgentParamsResource} REST controller.
// */
//@IntegrationTest
//@AutoConfigureMockMvc
//@WithMockUser
//class AgentParamsResourceIT {
//
//    private static final String DEFAULT_CALL_BACK_URL = "AAAAAAAAAA";
//    private static final String UPDATED_CALL_BACK_URL = "BBBBBBBBBB";
//
//    private static final String DEFAULT_USERNAME = "AAAAAAAAAA";
//    private static final String UPDATED_USERNAME = "BBBBBBBBBB";
//
//    private static final String DEFAULT_PASSWORD = "AAAAAAAAAA";
//    private static final String UPDATED_PASSWORD = "BBBBBBBBBB";
//
//    private static final String DEFAULT_BANK_CLIENT_ID = "AAAAAAAAAA";
//    private static final String UPDATED_BANK_CLIENT_ID = "BBBBBBBBBB";
//
//    private static final String DEFAULT_BANK_ACCOUNT_PREFIX = "AAAAAAAAAA";
//    private static final String UPDATED_BANK_ACCOUNT_PREFIX = "BBBBBBBBBB";
//
//    private static final String ENTITY_API_URL = "/api/agent-params";
//    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";
//
//    private static Random random = new Random();
//    private static AtomicLong count = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));
//
//    @Autowired
//    private AgentParamsRepository agentParamsRepository;
//
//    @Autowired
//    private AgentParamsMapper agentParamsMapper;
//
//    @Autowired
//    private EntityManager em;
//
//    @Autowired
//    private MockMvc restAgentParamsMockMvc;
//
//    private AgentParams agentParams;
//
//    /**
//     * Create an entity for this test.
//     *
//     * This is a static method, as tests for other entities might also need it,
//     * if they test an entity which requires the current entity.
//     */
//    public static AgentParams createEntity(EntityManager em) {
//        AgentParams agentParams = new AgentParams()
//            .callBackUrl(DEFAULT_CALL_BACK_URL)
//            .username(DEFAULT_USERNAME)
//            .password(DEFAULT_PASSWORD)
//            .bankClientId(DEFAULT_BANK_CLIENT_ID)
//            .bankAccountPrefix(DEFAULT_BANK_ACCOUNT_PREFIX);
//        return agentParams;
//    }
//
//    /**
//     * Create an updated entity for this test.
//     *
//     * This is a static method, as tests for other entities might also need it,
//     * if they test an entity which requires the current entity.
//     */
//    public static AgentParams createUpdatedEntity(EntityManager em) {
//        AgentParams agentParams = new AgentParams()
//            .callBackUrl(UPDATED_CALL_BACK_URL)
//            .username(UPDATED_USERNAME)
//            .password(UPDATED_PASSWORD)
//            .bankClientId(UPDATED_BANK_CLIENT_ID)
//            .bankAccountPrefix(UPDATED_BANK_ACCOUNT_PREFIX);
//        return agentParams;
//    }
//
//    @BeforeEach
//    public void initTest() {
//        agentParams = createEntity(em);
//    }
//
//    @Test
//    @Transactional
//    void createAgentParams() throws Exception {
//        int databaseSizeBeforeCreate = agentParamsRepository.findAll().size();
//        // Create the AgentParams
//        AgentParamsDTO agentParamsDTO = agentParamsMapper.toDto(agentParams);
//        restAgentParamsMockMvc
//            .perform(
//                post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(agentParamsDTO))
//            )
//            .andExpect(status().isCreated());
//
//        // Validate the AgentParams in the database
//        List<AgentParams> agentParamsList = agentParamsRepository.findAll();
//        assertThat(agentParamsList).hasSize(databaseSizeBeforeCreate + 1);
//        AgentParams testAgentParams = agentParamsList.get(agentParamsList.size() - 1);
//        assertThat(testAgentParams.getCallBackUrl()).isEqualTo(DEFAULT_CALL_BACK_URL);
//        assertThat(testAgentParams.getUsername()).isEqualTo(DEFAULT_USERNAME);
//        assertThat(testAgentParams.getPassword()).isEqualTo(DEFAULT_PASSWORD);
//        assertThat(testAgentParams.getBankClientId()).isEqualTo(DEFAULT_BANK_CLIENT_ID);
//        assertThat(testAgentParams.getBankAccountPrefix()).isEqualTo(DEFAULT_BANK_ACCOUNT_PREFIX);
//    }
//
//    @Test
//    @Transactional
//    void createAgentParamsWithExistingId() throws Exception {
//        // Create the AgentParams with an existing ID
//        agentParams.setId(1L);
//        AgentParamsDTO agentParamsDTO = agentParamsMapper.toDto(agentParams);
//
//        int databaseSizeBeforeCreate = agentParamsRepository.findAll().size();
//
//        // An entity with an existing ID cannot be created, so this API call must fail
//        restAgentParamsMockMvc
//            .perform(
//                post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(agentParamsDTO))
//            )
//            .andExpect(status().isBadRequest());
//
//        // Validate the AgentParams in the database
//        List<AgentParams> agentParamsList = agentParamsRepository.findAll();
//        assertThat(agentParamsList).hasSize(databaseSizeBeforeCreate);
//    }
//
//    @Test
//    @Transactional
//    void checkCallBackUrlIsRequired() throws Exception {
//        int databaseSizeBeforeTest = agentParamsRepository.findAll().size();
//        // set the field null
//        agentParams.setCallBackUrl(null);
//
//        // Create the AgentParams, which fails.
//        AgentParamsDTO agentParamsDTO = agentParamsMapper.toDto(agentParams);
//
//        restAgentParamsMockMvc
//            .perform(
//                post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(agentParamsDTO))
//            )
//            .andExpect(status().isBadRequest());
//
//        List<AgentParams> agentParamsList = agentParamsRepository.findAll();
//        assertThat(agentParamsList).hasSize(databaseSizeBeforeTest);
//    }
//
//    @Test
//    @Transactional
//    void getAllAgentParams() throws Exception {
//        // Initialize the database
//        agentParamsRepository.saveAndFlush(agentParams);
//
//        // Get all the agentParamsList
//        restAgentParamsMockMvc
//            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
//            .andExpect(status().isOk())
//            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
//            .andExpect(jsonPath("$.[*].id").value(hasItem(agentParams.getId().intValue())))
//            .andExpect(jsonPath("$.[*].callBackUrl").value(hasItem(DEFAULT_CALL_BACK_URL)))
//            .andExpect(jsonPath("$.[*].username").value(hasItem(DEFAULT_USERNAME)))
//            .andExpect(jsonPath("$.[*].password").value(hasItem(DEFAULT_PASSWORD)))
//            .andExpect(jsonPath("$.[*].bankClientId").value(hasItem(DEFAULT_BANK_CLIENT_ID)))
//            .andExpect(jsonPath("$.[*].bankAccountPrefix").value(hasItem(DEFAULT_BANK_ACCOUNT_PREFIX)));
//    }
//
//    @Test
//    @Transactional
//    void getAgentParams() throws Exception {
//        // Initialize the database
//        agentParamsRepository.saveAndFlush(agentParams);
//
//        // Get the agentParams
//        restAgentParamsMockMvc
//            .perform(get(ENTITY_API_URL_ID, agentParams.getId()))
//            .andExpect(status().isOk())
//            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
//            .andExpect(jsonPath("$.id").value(agentParams.getId().intValue()))
//            .andExpect(jsonPath("$.callBackUrl").value(DEFAULT_CALL_BACK_URL))
//            .andExpect(jsonPath("$.username").value(DEFAULT_USERNAME))
//            .andExpect(jsonPath("$.password").value(DEFAULT_PASSWORD))
//            .andExpect(jsonPath("$.bankClientId").value(DEFAULT_BANK_CLIENT_ID))
//            .andExpect(jsonPath("$.bankAccountPrefix").value(DEFAULT_BANK_ACCOUNT_PREFIX));
//    }
//
//    @Test
//    @Transactional
//    void getAgentParamsByIdFiltering() throws Exception {
//        // Initialize the database
//        agentParamsRepository.saveAndFlush(agentParams);
//
//        Long id = agentParams.getId();
//
//        defaultAgentParamsShouldBeFound("id.equals=" + id);
//        defaultAgentParamsShouldNotBeFound("id.notEquals=" + id);
//
//        defaultAgentParamsShouldBeFound("id.greaterThanOrEqual=" + id);
//        defaultAgentParamsShouldNotBeFound("id.greaterThan=" + id);
//
//        defaultAgentParamsShouldBeFound("id.lessThanOrEqual=" + id);
//        defaultAgentParamsShouldNotBeFound("id.lessThan=" + id);
//    }
//
//    @Test
//    @Transactional
//    void getAllAgentParamsByCallBackUrlIsEqualToSomething() throws Exception {
//        // Initialize the database
//        agentParamsRepository.saveAndFlush(agentParams);
//
//        // Get all the agentParamsList where callBackUrl equals to DEFAULT_CALL_BACK_URL
//        defaultAgentParamsShouldBeFound("callBackUrl.equals=" + DEFAULT_CALL_BACK_URL);
//
//        // Get all the agentParamsList where callBackUrl equals to UPDATED_CALL_BACK_URL
//        defaultAgentParamsShouldNotBeFound("callBackUrl.equals=" + UPDATED_CALL_BACK_URL);
//    }
//
//    @Test
//    @Transactional
//    void getAllAgentParamsByCallBackUrlIsNotEqualToSomething() throws Exception {
//        // Initialize the database
//        agentParamsRepository.saveAndFlush(agentParams);
//
//        // Get all the agentParamsList where callBackUrl not equals to DEFAULT_CALL_BACK_URL
//        defaultAgentParamsShouldNotBeFound("callBackUrl.notEquals=" + DEFAULT_CALL_BACK_URL);
//
//        // Get all the agentParamsList where callBackUrl not equals to UPDATED_CALL_BACK_URL
//        defaultAgentParamsShouldBeFound("callBackUrl.notEquals=" + UPDATED_CALL_BACK_URL);
//    }
//
//    @Test
//    @Transactional
//    void getAllAgentParamsByCallBackUrlIsInShouldWork() throws Exception {
//        // Initialize the database
//        agentParamsRepository.saveAndFlush(agentParams);
//
//        // Get all the agentParamsList where callBackUrl in DEFAULT_CALL_BACK_URL or UPDATED_CALL_BACK_URL
//        defaultAgentParamsShouldBeFound("callBackUrl.in=" + DEFAULT_CALL_BACK_URL + "," + UPDATED_CALL_BACK_URL);
//
//        // Get all the agentParamsList where callBackUrl equals to UPDATED_CALL_BACK_URL
//        defaultAgentParamsShouldNotBeFound("callBackUrl.in=" + UPDATED_CALL_BACK_URL);
//    }
//
//    @Test
//    @Transactional
//    void getAllAgentParamsByCallBackUrlIsNullOrNotNull() throws Exception {
//        // Initialize the database
//        agentParamsRepository.saveAndFlush(agentParams);
//
//        // Get all the agentParamsList where callBackUrl is not null
//        defaultAgentParamsShouldBeFound("callBackUrl.specified=true");
//
//        // Get all the agentParamsList where callBackUrl is null
//        defaultAgentParamsShouldNotBeFound("callBackUrl.specified=false");
//    }
//
//    @Test
//    @Transactional
//    void getAllAgentParamsByCallBackUrlContainsSomething() throws Exception {
//        // Initialize the database
//        agentParamsRepository.saveAndFlush(agentParams);
//
//        // Get all the agentParamsList where callBackUrl contains DEFAULT_CALL_BACK_URL
//        defaultAgentParamsShouldBeFound("callBackUrl.contains=" + DEFAULT_CALL_BACK_URL);
//
//        // Get all the agentParamsList where callBackUrl contains UPDATED_CALL_BACK_URL
//        defaultAgentParamsShouldNotBeFound("callBackUrl.contains=" + UPDATED_CALL_BACK_URL);
//    }
//
//    @Test
//    @Transactional
//    void getAllAgentParamsByCallBackUrlNotContainsSomething() throws Exception {
//        // Initialize the database
//        agentParamsRepository.saveAndFlush(agentParams);
//
//        // Get all the agentParamsList where callBackUrl does not contain DEFAULT_CALL_BACK_URL
//        defaultAgentParamsShouldNotBeFound("callBackUrl.doesNotContain=" + DEFAULT_CALL_BACK_URL);
//
//        // Get all the agentParamsList where callBackUrl does not contain UPDATED_CALL_BACK_URL
//        defaultAgentParamsShouldBeFound("callBackUrl.doesNotContain=" + UPDATED_CALL_BACK_URL);
//    }
//
//    @Test
//    @Transactional
//    void getAllAgentParamsByUsernameIsEqualToSomething() throws Exception {
//        // Initialize the database
//        agentParamsRepository.saveAndFlush(agentParams);
//
//        // Get all the agentParamsList where username equals to DEFAULT_USERNAME
//        defaultAgentParamsShouldBeFound("username.equals=" + DEFAULT_USERNAME);
//
//        // Get all the agentParamsList where username equals to UPDATED_USERNAME
//        defaultAgentParamsShouldNotBeFound("username.equals=" + UPDATED_USERNAME);
//    }
//
//    @Test
//    @Transactional
//    void getAllAgentParamsByUsernameIsNotEqualToSomething() throws Exception {
//        // Initialize the database
//        agentParamsRepository.saveAndFlush(agentParams);
//
//        // Get all the agentParamsList where username not equals to DEFAULT_USERNAME
//        defaultAgentParamsShouldNotBeFound("username.notEquals=" + DEFAULT_USERNAME);
//
//        // Get all the agentParamsList where username not equals to UPDATED_USERNAME
//        defaultAgentParamsShouldBeFound("username.notEquals=" + UPDATED_USERNAME);
//    }
//
//    @Test
//    @Transactional
//    void getAllAgentParamsByUsernameIsInShouldWork() throws Exception {
//        // Initialize the database
//        agentParamsRepository.saveAndFlush(agentParams);
//
//        // Get all the agentParamsList where username in DEFAULT_USERNAME or UPDATED_USERNAME
//        defaultAgentParamsShouldBeFound("username.in=" + DEFAULT_USERNAME + "," + UPDATED_USERNAME);
//
//        // Get all the agentParamsList where username equals to UPDATED_USERNAME
//        defaultAgentParamsShouldNotBeFound("username.in=" + UPDATED_USERNAME);
//    }
//
//    @Test
//    @Transactional
//    void getAllAgentParamsByUsernameIsNullOrNotNull() throws Exception {
//        // Initialize the database
//        agentParamsRepository.saveAndFlush(agentParams);
//
//        // Get all the agentParamsList where username is not null
//        defaultAgentParamsShouldBeFound("username.specified=true");
//
//        // Get all the agentParamsList where username is null
//        defaultAgentParamsShouldNotBeFound("username.specified=false");
//    }
//
//    @Test
//    @Transactional
//    void getAllAgentParamsByUsernameContainsSomething() throws Exception {
//        // Initialize the database
//        agentParamsRepository.saveAndFlush(agentParams);
//
//        // Get all the agentParamsList where username contains DEFAULT_USERNAME
//        defaultAgentParamsShouldBeFound("username.contains=" + DEFAULT_USERNAME);
//
//        // Get all the agentParamsList where username contains UPDATED_USERNAME
//        defaultAgentParamsShouldNotBeFound("username.contains=" + UPDATED_USERNAME);
//    }
//
//    @Test
//    @Transactional
//    void getAllAgentParamsByUsernameNotContainsSomething() throws Exception {
//        // Initialize the database
//        agentParamsRepository.saveAndFlush(agentParams);
//
//        // Get all the agentParamsList where username does not contain DEFAULT_USERNAME
//        defaultAgentParamsShouldNotBeFound("username.doesNotContain=" + DEFAULT_USERNAME);
//
//        // Get all the agentParamsList where username does not contain UPDATED_USERNAME
//        defaultAgentParamsShouldBeFound("username.doesNotContain=" + UPDATED_USERNAME);
//    }
//
//    @Test
//    @Transactional
//    void getAllAgentParamsByPasswordIsEqualToSomething() throws Exception {
//        // Initialize the database
//        agentParamsRepository.saveAndFlush(agentParams);
//
//        // Get all the agentParamsList where password equals to DEFAULT_PASSWORD
//        defaultAgentParamsShouldBeFound("password.equals=" + DEFAULT_PASSWORD);
//
//        // Get all the agentParamsList where password equals to UPDATED_PASSWORD
//        defaultAgentParamsShouldNotBeFound("password.equals=" + UPDATED_PASSWORD);
//    }
//
//    @Test
//    @Transactional
//    void getAllAgentParamsByPasswordIsNotEqualToSomething() throws Exception {
//        // Initialize the database
//        agentParamsRepository.saveAndFlush(agentParams);
//
//        // Get all the agentParamsList where password not equals to DEFAULT_PASSWORD
//        defaultAgentParamsShouldNotBeFound("password.notEquals=" + DEFAULT_PASSWORD);
//
//        // Get all the agentParamsList where password not equals to UPDATED_PASSWORD
//        defaultAgentParamsShouldBeFound("password.notEquals=" + UPDATED_PASSWORD);
//    }
//
//    @Test
//    @Transactional
//    void getAllAgentParamsByPasswordIsInShouldWork() throws Exception {
//        // Initialize the database
//        agentParamsRepository.saveAndFlush(agentParams);
//
//        // Get all the agentParamsList where password in DEFAULT_PASSWORD or UPDATED_PASSWORD
//        defaultAgentParamsShouldBeFound("password.in=" + DEFAULT_PASSWORD + "," + UPDATED_PASSWORD);
//
//        // Get all the agentParamsList where password equals to UPDATED_PASSWORD
//        defaultAgentParamsShouldNotBeFound("password.in=" + UPDATED_PASSWORD);
//    }
//
//    @Test
//    @Transactional
//    void getAllAgentParamsByPasswordIsNullOrNotNull() throws Exception {
//        // Initialize the database
//        agentParamsRepository.saveAndFlush(agentParams);
//
//        // Get all the agentParamsList where password is not null
//        defaultAgentParamsShouldBeFound("password.specified=true");
//
//        // Get all the agentParamsList where password is null
//        defaultAgentParamsShouldNotBeFound("password.specified=false");
//    }
//
//    @Test
//    @Transactional
//    void getAllAgentParamsByPasswordContainsSomething() throws Exception {
//        // Initialize the database
//        agentParamsRepository.saveAndFlush(agentParams);
//
//        // Get all the agentParamsList where password contains DEFAULT_PASSWORD
//        defaultAgentParamsShouldBeFound("password.contains=" + DEFAULT_PASSWORD);
//
//        // Get all the agentParamsList where password contains UPDATED_PASSWORD
//        defaultAgentParamsShouldNotBeFound("password.contains=" + UPDATED_PASSWORD);
//    }
//
//    @Test
//    @Transactional
//    void getAllAgentParamsByPasswordNotContainsSomething() throws Exception {
//        // Initialize the database
//        agentParamsRepository.saveAndFlush(agentParams);
//
//        // Get all the agentParamsList where password does not contain DEFAULT_PASSWORD
//        defaultAgentParamsShouldNotBeFound("password.doesNotContain=" + DEFAULT_PASSWORD);
//
//        // Get all the agentParamsList where password does not contain UPDATED_PASSWORD
//        defaultAgentParamsShouldBeFound("password.doesNotContain=" + UPDATED_PASSWORD);
//    }
//
//    @Test
//    @Transactional
//    void getAllAgentParamsByBankClientIdIsEqualToSomething() throws Exception {
//        // Initialize the database
//        agentParamsRepository.saveAndFlush(agentParams);
//
//        // Get all the agentParamsList where bankClientId equals to DEFAULT_BANK_CLIENT_ID
//        defaultAgentParamsShouldBeFound("bankClientId.equals=" + DEFAULT_BANK_CLIENT_ID);
//
//        // Get all the agentParamsList where bankClientId equals to UPDATED_BANK_CLIENT_ID
//        defaultAgentParamsShouldNotBeFound("bankClientId.equals=" + UPDATED_BANK_CLIENT_ID);
//    }
//
//    @Test
//    @Transactional
//    void getAllAgentParamsByBankClientIdIsNotEqualToSomething() throws Exception {
//        // Initialize the database
//        agentParamsRepository.saveAndFlush(agentParams);
//
//        // Get all the agentParamsList where bankClientId not equals to DEFAULT_BANK_CLIENT_ID
//        defaultAgentParamsShouldNotBeFound("bankClientId.notEquals=" + DEFAULT_BANK_CLIENT_ID);
//
//        // Get all the agentParamsList where bankClientId not equals to UPDATED_BANK_CLIENT_ID
//        defaultAgentParamsShouldBeFound("bankClientId.notEquals=" + UPDATED_BANK_CLIENT_ID);
//    }
//
//    @Test
//    @Transactional
//    void getAllAgentParamsByBankClientIdIsInShouldWork() throws Exception {
//        // Initialize the database
//        agentParamsRepository.saveAndFlush(agentParams);
//
//        // Get all the agentParamsList where bankClientId in DEFAULT_BANK_CLIENT_ID or UPDATED_BANK_CLIENT_ID
//        defaultAgentParamsShouldBeFound("bankClientId.in=" + DEFAULT_BANK_CLIENT_ID + "," + UPDATED_BANK_CLIENT_ID);
//
//        // Get all the agentParamsList where bankClientId equals to UPDATED_BANK_CLIENT_ID
//        defaultAgentParamsShouldNotBeFound("bankClientId.in=" + UPDATED_BANK_CLIENT_ID);
//    }
//
//    @Test
//    @Transactional
//    void getAllAgentParamsByBankClientIdIsNullOrNotNull() throws Exception {
//        // Initialize the database
//        agentParamsRepository.saveAndFlush(agentParams);
//
//        // Get all the agentParamsList where bankClientId is not null
//        defaultAgentParamsShouldBeFound("bankClientId.specified=true");
//
//        // Get all the agentParamsList where bankClientId is null
//        defaultAgentParamsShouldNotBeFound("bankClientId.specified=false");
//    }
//
//    @Test
//    @Transactional
//    void getAllAgentParamsByBankClientIdContainsSomething() throws Exception {
//        // Initialize the database
//        agentParamsRepository.saveAndFlush(agentParams);
//
//        // Get all the agentParamsList where bankClientId contains DEFAULT_BANK_CLIENT_ID
//        defaultAgentParamsShouldBeFound("bankClientId.contains=" + DEFAULT_BANK_CLIENT_ID);
//
//        // Get all the agentParamsList where bankClientId contains UPDATED_BANK_CLIENT_ID
//        defaultAgentParamsShouldNotBeFound("bankClientId.contains=" + UPDATED_BANK_CLIENT_ID);
//    }
//
//    @Test
//    @Transactional
//    void getAllAgentParamsByBankClientIdNotContainsSomething() throws Exception {
//        // Initialize the database
//        agentParamsRepository.saveAndFlush(agentParams);
//
//        // Get all the agentParamsList where bankClientId does not contain DEFAULT_BANK_CLIENT_ID
//        defaultAgentParamsShouldNotBeFound("bankClientId.doesNotContain=" + DEFAULT_BANK_CLIENT_ID);
//
//        // Get all the agentParamsList where bankClientId does not contain UPDATED_BANK_CLIENT_ID
//        defaultAgentParamsShouldBeFound("bankClientId.doesNotContain=" + UPDATED_BANK_CLIENT_ID);
//    }
//
//    @Test
//    @Transactional
//    void getAllAgentParamsByBankAccountPrefixIsEqualToSomething() throws Exception {
//        // Initialize the database
//        agentParamsRepository.saveAndFlush(agentParams);
//
//        // Get all the agentParamsList where bankAccountPrefix equals to DEFAULT_BANK_ACCOUNT_PREFIX
//        defaultAgentParamsShouldBeFound("bankAccountPrefix.equals=" + DEFAULT_BANK_ACCOUNT_PREFIX);
//
//        // Get all the agentParamsList where bankAccountPrefix equals to UPDATED_BANK_ACCOUNT_PREFIX
//        defaultAgentParamsShouldNotBeFound("bankAccountPrefix.equals=" + UPDATED_BANK_ACCOUNT_PREFIX);
//    }
//
//    @Test
//    @Transactional
//    void getAllAgentParamsByBankAccountPrefixIsNotEqualToSomething() throws Exception {
//        // Initialize the database
//        agentParamsRepository.saveAndFlush(agentParams);
//
//        // Get all the agentParamsList where bankAccountPrefix not equals to DEFAULT_BANK_ACCOUNT_PREFIX
//        defaultAgentParamsShouldNotBeFound("bankAccountPrefix.notEquals=" + DEFAULT_BANK_ACCOUNT_PREFIX);
//
//        // Get all the agentParamsList where bankAccountPrefix not equals to UPDATED_BANK_ACCOUNT_PREFIX
//        defaultAgentParamsShouldBeFound("bankAccountPrefix.notEquals=" + UPDATED_BANK_ACCOUNT_PREFIX);
//    }
//
//    @Test
//    @Transactional
//    void getAllAgentParamsByBankAccountPrefixIsInShouldWork() throws Exception {
//        // Initialize the database
//        agentParamsRepository.saveAndFlush(agentParams);
//
//        // Get all the agentParamsList where bankAccountPrefix in DEFAULT_BANK_ACCOUNT_PREFIX or UPDATED_BANK_ACCOUNT_PREFIX
//        defaultAgentParamsShouldBeFound("bankAccountPrefix.in=" + DEFAULT_BANK_ACCOUNT_PREFIX + "," + UPDATED_BANK_ACCOUNT_PREFIX);
//
//        // Get all the agentParamsList where bankAccountPrefix equals to UPDATED_BANK_ACCOUNT_PREFIX
//        defaultAgentParamsShouldNotBeFound("bankAccountPrefix.in=" + UPDATED_BANK_ACCOUNT_PREFIX);
//    }
//
//    @Test
//    @Transactional
//    void getAllAgentParamsByBankAccountPrefixIsNullOrNotNull() throws Exception {
//        // Initialize the database
//        agentParamsRepository.saveAndFlush(agentParams);
//
//        // Get all the agentParamsList where bankAccountPrefix is not null
//        defaultAgentParamsShouldBeFound("bankAccountPrefix.specified=true");
//
//        // Get all the agentParamsList where bankAccountPrefix is null
//        defaultAgentParamsShouldNotBeFound("bankAccountPrefix.specified=false");
//    }
//
//    @Test
//    @Transactional
//    void getAllAgentParamsByBankAccountPrefixContainsSomething() throws Exception {
//        // Initialize the database
//        agentParamsRepository.saveAndFlush(agentParams);
//
//        // Get all the agentParamsList where bankAccountPrefix contains DEFAULT_BANK_ACCOUNT_PREFIX
//        defaultAgentParamsShouldBeFound("bankAccountPrefix.contains=" + DEFAULT_BANK_ACCOUNT_PREFIX);
//
//        // Get all the agentParamsList where bankAccountPrefix contains UPDATED_BANK_ACCOUNT_PREFIX
//        defaultAgentParamsShouldNotBeFound("bankAccountPrefix.contains=" + UPDATED_BANK_ACCOUNT_PREFIX);
//    }
//
//    @Test
//    @Transactional
//    void getAllAgentParamsByBankAccountPrefixNotContainsSomething() throws Exception {
//        // Initialize the database
//        agentParamsRepository.saveAndFlush(agentParams);
//
//        // Get all the agentParamsList where bankAccountPrefix does not contain DEFAULT_BANK_ACCOUNT_PREFIX
//        defaultAgentParamsShouldNotBeFound("bankAccountPrefix.doesNotContain=" + DEFAULT_BANK_ACCOUNT_PREFIX);
//
//        // Get all the agentParamsList where bankAccountPrefix does not contain UPDATED_BANK_ACCOUNT_PREFIX
//        defaultAgentParamsShouldBeFound("bankAccountPrefix.doesNotContain=" + UPDATED_BANK_ACCOUNT_PREFIX);
//    }
//
//    @Test
//    @Transactional
//    void getAllAgentParamsByAgentIsEqualToSomething() throws Exception {
//        // Initialize the database
//        agentParamsRepository.saveAndFlush(agentParams);
//        Agent agent = AgentResourceIT.createEntity(em);
//        em.persist(agent);
//        em.flush();
//        agentParams.setAgent(agent);
//        agentParamsRepository.saveAndFlush(agentParams);
//        Long agentId = agent.getId();
//
//        // Get all the agentParamsList where agent equals to agentId
//        defaultAgentParamsShouldBeFound("agentId.equals=" + agentId);
//
//        // Get all the agentParamsList where agent equals to (agentId + 1)
//        defaultAgentParamsShouldNotBeFound("agentId.equals=" + (agentId + 1));
//    }
//
//    /**
//     * Executes the search, and checks that the default entity is returned.
//     */
//    private void defaultAgentParamsShouldBeFound(String filter) throws Exception {
//        restAgentParamsMockMvc
//            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
//            .andExpect(status().isOk())
//            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
//            .andExpect(jsonPath("$.[*].id").value(hasItem(agentParams.getId().intValue())))
//            .andExpect(jsonPath("$.[*].callBackUrl").value(hasItem(DEFAULT_CALL_BACK_URL)))
//            .andExpect(jsonPath("$.[*].username").value(hasItem(DEFAULT_USERNAME)))
//            .andExpect(jsonPath("$.[*].password").value(hasItem(DEFAULT_PASSWORD)))
//            .andExpect(jsonPath("$.[*].bankClientId").value(hasItem(DEFAULT_BANK_CLIENT_ID)))
//            .andExpect(jsonPath("$.[*].bankAccountPrefix").value(hasItem(DEFAULT_BANK_ACCOUNT_PREFIX)));
//
//        // Check, that the count call also returns 1
//        restAgentParamsMockMvc
//            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
//            .andExpect(status().isOk())
//            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
//            .andExpect(content().string("1"));
//    }
//
//    /**
//     * Executes the search, and checks that the default entity is not returned.
//     */
//    private void defaultAgentParamsShouldNotBeFound(String filter) throws Exception {
//        restAgentParamsMockMvc
//            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
//            .andExpect(status().isOk())
//            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
//            .andExpect(jsonPath("$").isArray())
//            .andExpect(jsonPath("$").isEmpty());
//
//        // Check, that the count call also returns 0
//        restAgentParamsMockMvc
//            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
//            .andExpect(status().isOk())
//            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
//            .andExpect(content().string("0"));
//    }
//
//    @Test
//    @Transactional
//    void getNonExistingAgentParams() throws Exception {
//        // Get the agentParams
//        restAgentParamsMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
//    }
//
//    @Test
//    @Transactional
//    void putNewAgentParams() throws Exception {
//        // Initialize the database
//        agentParamsRepository.saveAndFlush(agentParams);
//
//        int databaseSizeBeforeUpdate = agentParamsRepository.findAll().size();
//
//        // Update the agentParams
//        AgentParams updatedAgentParams = agentParamsRepository.findById(agentParams.getId()).get();
//        // Disconnect from session so that the updates on updatedAgentParams are not directly saved in db
//        em.detach(updatedAgentParams);
//        updatedAgentParams
//            .callBackUrl(UPDATED_CALL_BACK_URL)
//            .username(UPDATED_USERNAME)
//            .password(UPDATED_PASSWORD)
//            .bankClientId(UPDATED_BANK_CLIENT_ID)
//            .bankAccountPrefix(UPDATED_BANK_ACCOUNT_PREFIX);
//        AgentParamsDTO agentParamsDTO = agentParamsMapper.toDto(updatedAgentParams);
//
//        restAgentParamsMockMvc
//            .perform(
//                put(ENTITY_API_URL_ID, agentParamsDTO.getId())
//                    .contentType(MediaType.APPLICATION_JSON)
//                    .content(TestUtil.convertObjectToJsonBytes(agentParamsDTO))
//            )
//            .andExpect(status().isOk());
//
//        // Validate the AgentParams in the database
//        List<AgentParams> agentParamsList = agentParamsRepository.findAll();
//        assertThat(agentParamsList).hasSize(databaseSizeBeforeUpdate);
//        AgentParams testAgentParams = agentParamsList.get(agentParamsList.size() - 1);
//        assertThat(testAgentParams.getCallBackUrl()).isEqualTo(UPDATED_CALL_BACK_URL);
//        assertThat(testAgentParams.getUsername()).isEqualTo(UPDATED_USERNAME);
//        assertThat(testAgentParams.getPassword()).isEqualTo(UPDATED_PASSWORD);
//        assertThat(testAgentParams.getBankClientId()).isEqualTo(UPDATED_BANK_CLIENT_ID);
//        assertThat(testAgentParams.getBankAccountPrefix()).isEqualTo(UPDATED_BANK_ACCOUNT_PREFIX);
//    }
//
//    @Test
//    @Transactional
//    void putNonExistingAgentParams() throws Exception {
//        int databaseSizeBeforeUpdate = agentParamsRepository.findAll().size();
//        agentParams.setId(count.incrementAndGet());
//
//        // Create the AgentParams
//        AgentParamsDTO agentParamsDTO = agentParamsMapper.toDto(agentParams);
//
//        // If the entity doesn't have an ID, it will throw BadRequestAlertException
//        restAgentParamsMockMvc
//            .perform(
//                put(ENTITY_API_URL_ID, agentParamsDTO.getId())
//                    .contentType(MediaType.APPLICATION_JSON)
//                    .content(TestUtil.convertObjectToJsonBytes(agentParamsDTO))
//            )
//            .andExpect(status().isBadRequest());
//
//        // Validate the AgentParams in the database
//        List<AgentParams> agentParamsList = agentParamsRepository.findAll();
//        assertThat(agentParamsList).hasSize(databaseSizeBeforeUpdate);
//    }
//
//    @Test
//    @Transactional
//    void putWithIdMismatchAgentParams() throws Exception {
//        int databaseSizeBeforeUpdate = agentParamsRepository.findAll().size();
//        agentParams.setId(count.incrementAndGet());
//
//        // Create the AgentParams
//        AgentParamsDTO agentParamsDTO = agentParamsMapper.toDto(agentParams);
//
//        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
//        restAgentParamsMockMvc
//            .perform(
//                put(ENTITY_API_URL_ID, count.incrementAndGet())
//                    .contentType(MediaType.APPLICATION_JSON)
//                    .content(TestUtil.convertObjectToJsonBytes(agentParamsDTO))
//            )
//            .andExpect(status().isBadRequest());
//
//        // Validate the AgentParams in the database
//        List<AgentParams> agentParamsList = agentParamsRepository.findAll();
//        assertThat(agentParamsList).hasSize(databaseSizeBeforeUpdate);
//    }
//
//    @Test
//    @Transactional
//    void putWithMissingIdPathParamAgentParams() throws Exception {
//        int databaseSizeBeforeUpdate = agentParamsRepository.findAll().size();
//        agentParams.setId(count.incrementAndGet());
//
//        // Create the AgentParams
//        AgentParamsDTO agentParamsDTO = agentParamsMapper.toDto(agentParams);
//
//        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
//        restAgentParamsMockMvc
//            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(agentParamsDTO)))
//            .andExpect(status().isMethodNotAllowed());
//
//        // Validate the AgentParams in the database
//        List<AgentParams> agentParamsList = agentParamsRepository.findAll();
//        assertThat(agentParamsList).hasSize(databaseSizeBeforeUpdate);
//    }
//
//    @Test
//    @Transactional
//    void partialUpdateAgentParamsWithPatch() throws Exception {
//        // Initialize the database
//        agentParamsRepository.saveAndFlush(agentParams);
//
//        int databaseSizeBeforeUpdate = agentParamsRepository.findAll().size();
//
//        // Update the agentParams using partial update
//        AgentParams partialUpdatedAgentParams = new AgentParams();
//        partialUpdatedAgentParams.setId(agentParams.getId());
//
//        partialUpdatedAgentParams.username(UPDATED_USERNAME).bankAccountPrefix(UPDATED_BANK_ACCOUNT_PREFIX);
//
//        restAgentParamsMockMvc
//            .perform(
//                patch(ENTITY_API_URL_ID, partialUpdatedAgentParams.getId())
//                    .contentType("application/merge-patch+json")
//                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedAgentParams))
//            )
//            .andExpect(status().isOk());
//
//        // Validate the AgentParams in the database
//        List<AgentParams> agentParamsList = agentParamsRepository.findAll();
//        assertThat(agentParamsList).hasSize(databaseSizeBeforeUpdate);
//        AgentParams testAgentParams = agentParamsList.get(agentParamsList.size() - 1);
//        assertThat(testAgentParams.getCallBackUrl()).isEqualTo(DEFAULT_CALL_BACK_URL);
//        assertThat(testAgentParams.getUsername()).isEqualTo(UPDATED_USERNAME);
//        assertThat(testAgentParams.getPassword()).isEqualTo(DEFAULT_PASSWORD);
//        assertThat(testAgentParams.getBankClientId()).isEqualTo(DEFAULT_BANK_CLIENT_ID);
//        assertThat(testAgentParams.getBankAccountPrefix()).isEqualTo(UPDATED_BANK_ACCOUNT_PREFIX);
//    }
//
//    @Test
//    @Transactional
//    void fullUpdateAgentParamsWithPatch() throws Exception {
//        // Initialize the database
//        agentParamsRepository.saveAndFlush(agentParams);
//
//        int databaseSizeBeforeUpdate = agentParamsRepository.findAll().size();
//
//        // Update the agentParams using partial update
//        AgentParams partialUpdatedAgentParams = new AgentParams();
//        partialUpdatedAgentParams.setId(agentParams.getId());
//
//        partialUpdatedAgentParams
//            .callBackUrl(UPDATED_CALL_BACK_URL)
//            .username(UPDATED_USERNAME)
//            .password(UPDATED_PASSWORD)
//            .bankClientId(UPDATED_BANK_CLIENT_ID)
//            .bankAccountPrefix(UPDATED_BANK_ACCOUNT_PREFIX);
//
//        restAgentParamsMockMvc
//            .perform(
//                patch(ENTITY_API_URL_ID, partialUpdatedAgentParams.getId())
//                    .contentType("application/merge-patch+json")
//                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedAgentParams))
//            )
//            .andExpect(status().isOk());
//
//        // Validate the AgentParams in the database
//        List<AgentParams> agentParamsList = agentParamsRepository.findAll();
//        assertThat(agentParamsList).hasSize(databaseSizeBeforeUpdate);
//        AgentParams testAgentParams = agentParamsList.get(agentParamsList.size() - 1);
//        assertThat(testAgentParams.getCallBackUrl()).isEqualTo(UPDATED_CALL_BACK_URL);
//        assertThat(testAgentParams.getUsername()).isEqualTo(UPDATED_USERNAME);
//        assertThat(testAgentParams.getPassword()).isEqualTo(UPDATED_PASSWORD);
//        assertThat(testAgentParams.getBankClientId()).isEqualTo(UPDATED_BANK_CLIENT_ID);
//        assertThat(testAgentParams.getBankAccountPrefix()).isEqualTo(UPDATED_BANK_ACCOUNT_PREFIX);
//    }
//
//    @Test
//    @Transactional
//    void patchNonExistingAgentParams() throws Exception {
//        int databaseSizeBeforeUpdate = agentParamsRepository.findAll().size();
//        agentParams.setId(count.incrementAndGet());
//
//        // Create the AgentParams
//        AgentParamsDTO agentParamsDTO = agentParamsMapper.toDto(agentParams);
//
//        // If the entity doesn't have an ID, it will throw BadRequestAlertException
//        restAgentParamsMockMvc
//            .perform(
//                patch(ENTITY_API_URL_ID, agentParamsDTO.getId())
//                    .contentType("application/merge-patch+json")
//                    .content(TestUtil.convertObjectToJsonBytes(agentParamsDTO))
//            )
//            .andExpect(status().isBadRequest());
//
//        // Validate the AgentParams in the database
//        List<AgentParams> agentParamsList = agentParamsRepository.findAll();
//        assertThat(agentParamsList).hasSize(databaseSizeBeforeUpdate);
//    }
//
//    @Test
//    @Transactional
//    void patchWithIdMismatchAgentParams() throws Exception {
//        int databaseSizeBeforeUpdate = agentParamsRepository.findAll().size();
//        agentParams.setId(count.incrementAndGet());
//
//        // Create the AgentParams
//        AgentParamsDTO agentParamsDTO = agentParamsMapper.toDto(agentParams);
//
//        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
//        restAgentParamsMockMvc
//            .perform(
//                patch(ENTITY_API_URL_ID, count.incrementAndGet())
//                    .contentType("application/merge-patch+json")
//                    .content(TestUtil.convertObjectToJsonBytes(agentParamsDTO))
//            )
//            .andExpect(status().isBadRequest());
//
//        // Validate the AgentParams in the database
//        List<AgentParams> agentParamsList = agentParamsRepository.findAll();
//        assertThat(agentParamsList).hasSize(databaseSizeBeforeUpdate);
//    }
//
//    @Test
//    @Transactional
//    void patchWithMissingIdPathParamAgentParams() throws Exception {
//        int databaseSizeBeforeUpdate = agentParamsRepository.findAll().size();
//        agentParams.setId(count.incrementAndGet());
//
//        // Create the AgentParams
//        AgentParamsDTO agentParamsDTO = agentParamsMapper.toDto(agentParams);
//
//        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
//        restAgentParamsMockMvc
//            .perform(
//                patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(TestUtil.convertObjectToJsonBytes(agentParamsDTO))
//            )
//            .andExpect(status().isMethodNotAllowed());
//
//        // Validate the AgentParams in the database
//        List<AgentParams> agentParamsList = agentParamsRepository.findAll();
//        assertThat(agentParamsList).hasSize(databaseSizeBeforeUpdate);
//    }
//
//    @Test
//    @Transactional
//    void deleteAgentParams() throws Exception {
//        // Initialize the database
//        agentParamsRepository.saveAndFlush(agentParams);
//
//        int databaseSizeBeforeDelete = agentParamsRepository.findAll().size();
//
//        // Delete the agentParams
//        restAgentParamsMockMvc
//            .perform(delete(ENTITY_API_URL_ID, agentParams.getId()).accept(MediaType.APPLICATION_JSON))
//            .andExpect(status().isNoContent());
//
//        // Validate the database contains one less item
//        List<AgentParams> agentParamsList = agentParamsRepository.findAll();
//        assertThat(agentParamsList).hasSize(databaseSizeBeforeDelete - 1);
//    }
//}
