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
import org.springframework.util.Base64Utils;
import uz.paykap.IntegrationTest;
import uz.paykap.domain.Agent;
import uz.paykap.domain.AgentRecipient;
import uz.paykap.domain.Recipient;
import uz.paykap.domain.RecipientGroup;
import uz.paykap.domain.Setting;
import uz.paykap.domain.SettingType;
import uz.paykap.repository.AgentRecipientRepository;
import uz.paykap.service.criteria.AgentRecipientCriteria;
import uz.paykap.service.dto.AgentRecipientDTO;
import uz.paykap.service.mapper.AgentRecipientMapper;

/**
 * Integration tests for the {@link AgentRecipientResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class AgentRecipientResourceIT {

    private static final Double DEFAULT_AGENT_COMMISSION = 0D;
    private static final Double UPDATED_AGENT_COMMISSION = 1D;
    private static final Double SMALLER_AGENT_COMMISSION = 0D - 1D;

    private static final Double DEFAULT_AGENT_FEE = 0D;
    private static final Double UPDATED_AGENT_FEE = 1D;
    private static final Double SMALLER_AGENT_FEE = 0D - 1D;

    private static final Double DEFAULT_SYSTEM_COMMISSION_UP = 0D;
    private static final Double UPDATED_SYSTEM_COMMISSION_UP = 1D;
    private static final Double SMALLER_SYSTEM_COMMISSION_UP = 0D - 1D;

    private static final Double DEFAULT_SYSTEM_COMMISSION_DOWN = 0D;
    private static final Double UPDATED_SYSTEM_COMMISSION_DOWN = 1D;
    private static final Double SMALLER_SYSTEM_COMMISSION_DOWN = 0D - 1D;

    private static final Double DEFAULT_SYSTEM_FEE = 0D;
    private static final Double UPDATED_SYSTEM_FEE = 1D;
    private static final Double SMALLER_SYSTEM_FEE = 0D - 1D;

    private static final Boolean DEFAULT_ACTIVE = false;
    private static final Boolean UPDATED_ACTIVE = true;

    private static final Integer DEFAULT_EPOS_PORT = 0;
    private static final Integer UPDATED_EPOS_PORT = 1;
    private static final Integer SMALLER_EPOS_PORT = 0 - 1;

    private static final String DEFAULT_EPOS_MERCHANT = "AAAAAAAAAA";
    private static final String UPDATED_EPOS_MERCHANT = "BBBBBBBBBB";

    private static final String DEFAULT_EPOS_TERMINAL = "AAAAAAAAAA";
    private static final String UPDATED_EPOS_TERMINAL = "BBBBBBBBBB";

    private static final String DEFAULT_REQUEST_GROUP = "AAAAAAAAAA";
    private static final String UPDATED_REQUEST_GROUP = "BBBBBBBBBB";

    private static final String DEFAULT_JSON_PARAMS = "AAAAAAAAAA";
    private static final String UPDATED_JSON_PARAMS = "BBBBBBBBBB";

    private static final String ENTITY_API_URL = "/api/agent-recipients";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong count = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private AgentRecipientRepository agentRecipientRepository;

    @Autowired
    private AgentRecipientMapper agentRecipientMapper;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restAgentRecipientMockMvc;

    private AgentRecipient agentRecipient;

    /**
     * Create an entity for this test.
     * <p>
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static AgentRecipient createEntity(EntityManager em) {
        AgentRecipient agentRecipient = new AgentRecipient()
                .agentCommission(DEFAULT_AGENT_COMMISSION)
                .agentFee(DEFAULT_AGENT_FEE)
                .systemCommissionUp(DEFAULT_SYSTEM_COMMISSION_UP)
                .systemCommissionDown(DEFAULT_SYSTEM_COMMISSION_DOWN)
                .systemFee(DEFAULT_SYSTEM_FEE)
                .active(DEFAULT_ACTIVE)
                .eposPort(DEFAULT_EPOS_PORT)
                .requestGroup(DEFAULT_REQUEST_GROUP)
                .eposMerchant(DEFAULT_EPOS_MERCHANT)
                .eposTerminal(DEFAULT_EPOS_TERMINAL)
                .jsonParams(DEFAULT_JSON_PARAMS);
        return agentRecipient;
    }

    /**
     * Create an updated entity for this test.
     * <p>
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static AgentRecipient createUpdatedEntity(EntityManager em) {
        AgentRecipient agentRecipient = new AgentRecipient()
                .agentCommission(UPDATED_AGENT_COMMISSION)
                .agentFee(UPDATED_AGENT_FEE)
                .systemCommissionUp(UPDATED_SYSTEM_COMMISSION_UP)
                .systemCommissionDown(UPDATED_SYSTEM_COMMISSION_DOWN)
                .systemFee(UPDATED_SYSTEM_FEE)
                .active(UPDATED_ACTIVE)
                .eposPort(UPDATED_EPOS_PORT)
                .eposMerchant(UPDATED_EPOS_MERCHANT)
                .eposTerminal(UPDATED_EPOS_TERMINAL)
                .jsonParams(UPDATED_JSON_PARAMS);
        return agentRecipient;
    }

    @BeforeEach
    public void initTest() {
        agentRecipient = createEntity(em);
    }

    @Test
    @Transactional
    void createAgentRecipient() throws Exception {
        int databaseSizeBeforeCreate = agentRecipientRepository.findAll().size();
        // Create the AgentRecipient
        AgentRecipientDTO agentRecipientDTO = agentRecipientMapper.toDto(agentRecipient);
        restAgentRecipientMockMvc
                .perform(
                        post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(agentRecipientDTO))
                )
                .andExpect(status().isCreated());

        // Validate the AgentRecipient in the database
        List<AgentRecipient> agentRecipientList = agentRecipientRepository.findAll();
        assertThat(agentRecipientList).hasSize(databaseSizeBeforeCreate + 1);
        AgentRecipient testAgentRecipient = agentRecipientList.get(agentRecipientList.size() - 1);
        assertThat(testAgentRecipient.getAgentCommission()).isEqualTo(DEFAULT_AGENT_COMMISSION);
        assertThat(testAgentRecipient.getAgentFee()).isEqualTo(DEFAULT_AGENT_FEE);
        assertThat(testAgentRecipient.getSystemCommissionUp()).isEqualTo(DEFAULT_SYSTEM_COMMISSION_UP);
        assertThat(testAgentRecipient.getSystemCommissionDown()).isEqualTo(DEFAULT_SYSTEM_COMMISSION_DOWN);
        assertThat(testAgentRecipient.getSystemFee()).isEqualTo(DEFAULT_SYSTEM_FEE);
        assertThat(testAgentRecipient.getActive()).isEqualTo(DEFAULT_ACTIVE);
        assertThat(testAgentRecipient.getEposPort()).isEqualTo(DEFAULT_EPOS_PORT);
        assertThat(testAgentRecipient.getEposMerchant()).isEqualTo(DEFAULT_EPOS_MERCHANT);
        assertThat(testAgentRecipient.getEposTerminal()).isEqualTo(DEFAULT_EPOS_TERMINAL);
        assertThat(testAgentRecipient.getJsonParams()).isEqualTo(DEFAULT_JSON_PARAMS);
    }

    @Test
    @Transactional
    void createAgentRecipientWithExistingId() throws Exception {
        // Create the AgentRecipient with an existing ID
        agentRecipient.setId(1L);
        AgentRecipientDTO agentRecipientDTO = agentRecipientMapper.toDto(agentRecipient);

        int databaseSizeBeforeCreate = agentRecipientRepository.findAll().size();

        // An entity with an existing ID cannot be created, so this API call must fail
        restAgentRecipientMockMvc
                .perform(
                        post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(agentRecipientDTO))
                )
                .andExpect(status().isBadRequest());

        // Validate the AgentRecipient in the database
        List<AgentRecipient> agentRecipientList = agentRecipientRepository.findAll();
        assertThat(agentRecipientList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkActiveIsRequired() throws Exception {
        int databaseSizeBeforeTest = agentRecipientRepository.findAll().size();
        // set the field null
        agentRecipient.setActive(null);

        // Create the AgentRecipient, which fails.
        AgentRecipientDTO agentRecipientDTO = agentRecipientMapper.toDto(agentRecipient);

        restAgentRecipientMockMvc
                .perform(
                        post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(agentRecipientDTO))
                )
                .andExpect(status().isBadRequest());

        List<AgentRecipient> agentRecipientList = agentRecipientRepository.findAll();
        assertThat(agentRecipientList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllAgentRecipients() throws Exception {
        // Initialize the database
        agentRecipientRepository.saveAndFlush(agentRecipient);

        // Get all the agentRecipientList
        restAgentRecipientMockMvc
                .perform(get(ENTITY_API_URL + "?sort=id,desc"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.[*].id").value(hasItem(agentRecipient.getId().intValue())))
                .andExpect(jsonPath("$.[*].agentCommission").value(hasItem(DEFAULT_AGENT_COMMISSION.doubleValue())))
                .andExpect(jsonPath("$.[*].agentFee").value(hasItem(DEFAULT_AGENT_FEE.doubleValue())))
                .andExpect(jsonPath("$.[*].systemCommissionUp").value(hasItem(DEFAULT_SYSTEM_COMMISSION_UP.doubleValue())))
                .andExpect(jsonPath("$.[*].systemCommissionDown").value(hasItem(DEFAULT_SYSTEM_COMMISSION_DOWN.doubleValue())))
                .andExpect(jsonPath("$.[*].systemFee").value(hasItem(DEFAULT_SYSTEM_FEE.doubleValue())))
                .andExpect(jsonPath("$.[*].active").value(hasItem(DEFAULT_ACTIVE.booleanValue())))
                .andExpect(jsonPath("$.[*].eposPort").value(hasItem(DEFAULT_EPOS_PORT)))
                .andExpect(jsonPath("$.[*].eposMerchant").value(hasItem(DEFAULT_EPOS_MERCHANT)))
                .andExpect(jsonPath("$.[*].eposTerminal").value(hasItem(DEFAULT_EPOS_TERMINAL)))
                .andExpect(jsonPath("$.[*].requestGroup").value(hasItem(DEFAULT_REQUEST_GROUP)))
                .andExpect(jsonPath("$.[*].jsonParams").value(hasItem(DEFAULT_JSON_PARAMS.toString())));
    }

    @Test
    @Transactional
    void getAgentRecipient() throws Exception {
        // Initialize the database
        agentRecipientRepository.saveAndFlush(agentRecipient);

        // Get the agentRecipient
        restAgentRecipientMockMvc
                .perform(get(ENTITY_API_URL_ID, agentRecipient.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.id").value(agentRecipient.getId().intValue()))
                .andExpect(jsonPath("$.agentCommission").value(DEFAULT_AGENT_COMMISSION.doubleValue()))
                .andExpect(jsonPath("$.agentFee").value(DEFAULT_AGENT_FEE.doubleValue()))
                .andExpect(jsonPath("$.systemCommissionUp").value(DEFAULT_SYSTEM_COMMISSION_UP.doubleValue()))
                .andExpect(jsonPath("$.systemCommissionDown").value(DEFAULT_SYSTEM_COMMISSION_DOWN.doubleValue()))
                .andExpect(jsonPath("$.systemFee").value(DEFAULT_SYSTEM_FEE.doubleValue()))
                .andExpect(jsonPath("$.active").value(DEFAULT_ACTIVE.booleanValue()))
                .andExpect(jsonPath("$.eposPort").value(DEFAULT_EPOS_PORT))
                .andExpect(jsonPath("$.eposMerchant").value(DEFAULT_EPOS_MERCHANT))
                .andExpect(jsonPath("$.eposTerminal").value(DEFAULT_EPOS_TERMINAL))
                .andExpect(jsonPath("$.requestGroup").value(DEFAULT_REQUEST_GROUP))
                .andExpect(jsonPath("$.jsonParams").value(DEFAULT_JSON_PARAMS.toString()));
    }

    @Test
    @Transactional
    void getAgentRecipientsByIdFiltering() throws Exception {
        // Initialize the database
        agentRecipientRepository.saveAndFlush(agentRecipient);

        Long id = agentRecipient.getId();

        defaultAgentRecipientShouldBeFound("id.equals=" + id);
        defaultAgentRecipientShouldNotBeFound("id.notEquals=" + id);

        defaultAgentRecipientShouldBeFound("id.greaterThanOrEqual=" + id);
        defaultAgentRecipientShouldNotBeFound("id.greaterThan=" + id);

        defaultAgentRecipientShouldBeFound("id.lessThanOrEqual=" + id);
        defaultAgentRecipientShouldNotBeFound("id.lessThan=" + id);
    }

    @Test
    @Transactional
    void getAllAgentRecipientsByAgentCommissionIsEqualToSomething() throws Exception {
        // Initialize the database
        agentRecipientRepository.saveAndFlush(agentRecipient);

        // Get all the agentRecipientList where agentCommission equals to DEFAULT_AGENT_COMMISSION
        defaultAgentRecipientShouldBeFound("agentCommission.equals=" + DEFAULT_AGENT_COMMISSION);

        // Get all the agentRecipientList where agentCommission equals to UPDATED_AGENT_COMMISSION
        defaultAgentRecipientShouldNotBeFound("agentCommission.equals=" + UPDATED_AGENT_COMMISSION);
    }

    @Test
    @Transactional
    void getAllAgentRecipientsByAgentCommissionIsNotEqualToSomething() throws Exception {
        // Initialize the database
        agentRecipientRepository.saveAndFlush(agentRecipient);

        // Get all the agentRecipientList where agentCommission not equals to DEFAULT_AGENT_COMMISSION
        defaultAgentRecipientShouldNotBeFound("agentCommission.notEquals=" + DEFAULT_AGENT_COMMISSION);

        // Get all the agentRecipientList where agentCommission not equals to UPDATED_AGENT_COMMISSION
        defaultAgentRecipientShouldBeFound("agentCommission.notEquals=" + UPDATED_AGENT_COMMISSION);
    }

    @Test
    @Transactional
    void getAllAgentRecipientsByAgentCommissionIsInShouldWork() throws Exception {
        // Initialize the database
        agentRecipientRepository.saveAndFlush(agentRecipient);

        // Get all the agentRecipientList where agentCommission in DEFAULT_AGENT_COMMISSION or UPDATED_AGENT_COMMISSION
        defaultAgentRecipientShouldBeFound("agentCommission.in=" + DEFAULT_AGENT_COMMISSION + "," + UPDATED_AGENT_COMMISSION);

        // Get all the agentRecipientList where agentCommission equals to UPDATED_AGENT_COMMISSION
        defaultAgentRecipientShouldNotBeFound("agentCommission.in=" + UPDATED_AGENT_COMMISSION);
    }

    @Test
    @Transactional
    void getAllAgentRecipientsByAgentCommissionIsNullOrNotNull() throws Exception {
        // Initialize the database
        agentRecipientRepository.saveAndFlush(agentRecipient);

        // Get all the agentRecipientList where agentCommission is not null
        defaultAgentRecipientShouldBeFound("agentCommission.specified=true");

        // Get all the agentRecipientList where agentCommission is null
        defaultAgentRecipientShouldNotBeFound("agentCommission.specified=false");
    }

    @Test
    @Transactional
    void getAllAgentRecipientsByAgentCommissionIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        agentRecipientRepository.saveAndFlush(agentRecipient);

        // Get all the agentRecipientList where agentCommission is greater than or equal to DEFAULT_AGENT_COMMISSION
        defaultAgentRecipientShouldBeFound("agentCommission.greaterThanOrEqual=" + DEFAULT_AGENT_COMMISSION);

        // Get all the agentRecipientList where agentCommission is greater than or equal to (DEFAULT_AGENT_COMMISSION + 1)
        defaultAgentRecipientShouldNotBeFound("agentCommission.greaterThanOrEqual=" + (DEFAULT_AGENT_COMMISSION + 1));
    }

    @Test
    @Transactional
    void getAllAgentRecipientsByAgentCommissionIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        agentRecipientRepository.saveAndFlush(agentRecipient);

        // Get all the agentRecipientList where agentCommission is less than or equal to DEFAULT_AGENT_COMMISSION
        defaultAgentRecipientShouldBeFound("agentCommission.lessThanOrEqual=" + DEFAULT_AGENT_COMMISSION);

        // Get all the agentRecipientList where agentCommission is less than or equal to SMALLER_AGENT_COMMISSION
        defaultAgentRecipientShouldNotBeFound("agentCommission.lessThanOrEqual=" + SMALLER_AGENT_COMMISSION);
    }

    @Test
    @Transactional
    void getAllAgentRecipientsByAgentCommissionIsLessThanSomething() throws Exception {
        // Initialize the database
        agentRecipientRepository.saveAndFlush(agentRecipient);

        // Get all the agentRecipientList where agentCommission is less than DEFAULT_AGENT_COMMISSION
        defaultAgentRecipientShouldNotBeFound("agentCommission.lessThan=" + DEFAULT_AGENT_COMMISSION);

        // Get all the agentRecipientList where agentCommission is less than (DEFAULT_AGENT_COMMISSION + 1)
        defaultAgentRecipientShouldBeFound("agentCommission.lessThan=" + (DEFAULT_AGENT_COMMISSION + 1));
    }

    @Test
    @Transactional
    void getAllAgentRecipientsByAgentCommissionIsGreaterThanSomething() throws Exception {
        // Initialize the database
        agentRecipientRepository.saveAndFlush(agentRecipient);

        // Get all the agentRecipientList where agentCommission is greater than DEFAULT_AGENT_COMMISSION
        defaultAgentRecipientShouldNotBeFound("agentCommission.greaterThan=" + DEFAULT_AGENT_COMMISSION);

        // Get all the agentRecipientList where agentCommission is greater than SMALLER_AGENT_COMMISSION
        defaultAgentRecipientShouldBeFound("agentCommission.greaterThan=" + SMALLER_AGENT_COMMISSION);
    }

    @Test
    @Transactional
    void getAllAgentRecipientsByAgentFeeIsEqualToSomething() throws Exception {
        // Initialize the database
        agentRecipientRepository.saveAndFlush(agentRecipient);

        // Get all the agentRecipientList where agentFee equals to DEFAULT_AGENT_FEE
        defaultAgentRecipientShouldBeFound("agentFee.equals=" + DEFAULT_AGENT_FEE);

        // Get all the agentRecipientList where agentFee equals to UPDATED_AGENT_FEE
        defaultAgentRecipientShouldNotBeFound("agentFee.equals=" + UPDATED_AGENT_FEE);
    }

    @Test
    @Transactional
    void getAllAgentRecipientsByAgentFeeIsNotEqualToSomething() throws Exception {
        // Initialize the database
        agentRecipientRepository.saveAndFlush(agentRecipient);

        // Get all the agentRecipientList where agentFee not equals to DEFAULT_AGENT_FEE
        defaultAgentRecipientShouldNotBeFound("agentFee.notEquals=" + DEFAULT_AGENT_FEE);

        // Get all the agentRecipientList where agentFee not equals to UPDATED_AGENT_FEE
        defaultAgentRecipientShouldBeFound("agentFee.notEquals=" + UPDATED_AGENT_FEE);
    }

    @Test
    @Transactional
    void getAllAgentRecipientsByAgentFeeIsInShouldWork() throws Exception {
        // Initialize the database
        agentRecipientRepository.saveAndFlush(agentRecipient);

        // Get all the agentRecipientList where agentFee in DEFAULT_AGENT_FEE or UPDATED_AGENT_FEE
        defaultAgentRecipientShouldBeFound("agentFee.in=" + DEFAULT_AGENT_FEE + "," + UPDATED_AGENT_FEE);

        // Get all the agentRecipientList where agentFee equals to UPDATED_AGENT_FEE
        defaultAgentRecipientShouldNotBeFound("agentFee.in=" + UPDATED_AGENT_FEE);
    }

    @Test
    @Transactional
    void getAllAgentRecipientsByAgentFeeIsNullOrNotNull() throws Exception {
        // Initialize the database
        agentRecipientRepository.saveAndFlush(agentRecipient);

        // Get all the agentRecipientList where agentFee is not null
        defaultAgentRecipientShouldBeFound("agentFee.specified=true");

        // Get all the agentRecipientList where agentFee is null
        defaultAgentRecipientShouldNotBeFound("agentFee.specified=false");
    }

    @Test
    @Transactional
    void getAllAgentRecipientsByAgentFeeIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        agentRecipientRepository.saveAndFlush(agentRecipient);

        // Get all the agentRecipientList where agentFee is greater than or equal to DEFAULT_AGENT_FEE
        defaultAgentRecipientShouldBeFound("agentFee.greaterThanOrEqual=" + DEFAULT_AGENT_FEE);

        // Get all the agentRecipientList where agentFee is greater than or equal to (DEFAULT_AGENT_FEE + 1)
        defaultAgentRecipientShouldNotBeFound("agentFee.greaterThanOrEqual=" + (DEFAULT_AGENT_FEE + 1));
    }

    @Test
    @Transactional
    void getAllAgentRecipientsByAgentFeeIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        agentRecipientRepository.saveAndFlush(agentRecipient);

        // Get all the agentRecipientList where agentFee is less than or equal to DEFAULT_AGENT_FEE
        defaultAgentRecipientShouldBeFound("agentFee.lessThanOrEqual=" + DEFAULT_AGENT_FEE);

        // Get all the agentRecipientList where agentFee is less than or equal to SMALLER_AGENT_FEE
        defaultAgentRecipientShouldNotBeFound("agentFee.lessThanOrEqual=" + SMALLER_AGENT_FEE);
    }

    @Test
    @Transactional
    void getAllAgentRecipientsByAgentFeeIsLessThanSomething() throws Exception {
        // Initialize the database
        agentRecipientRepository.saveAndFlush(agentRecipient);

        // Get all the agentRecipientList where agentFee is less than DEFAULT_AGENT_FEE
        defaultAgentRecipientShouldNotBeFound("agentFee.lessThan=" + DEFAULT_AGENT_FEE);

        // Get all the agentRecipientList where agentFee is less than (DEFAULT_AGENT_FEE + 1)
        defaultAgentRecipientShouldBeFound("agentFee.lessThan=" + (DEFAULT_AGENT_FEE + 1));
    }

    @Test
    @Transactional
    void getAllAgentRecipientsByAgentFeeIsGreaterThanSomething() throws Exception {
        // Initialize the database
        agentRecipientRepository.saveAndFlush(agentRecipient);

        // Get all the agentRecipientList where agentFee is greater than DEFAULT_AGENT_FEE
        defaultAgentRecipientShouldNotBeFound("agentFee.greaterThan=" + DEFAULT_AGENT_FEE);

        // Get all the agentRecipientList where agentFee is greater than SMALLER_AGENT_FEE
        defaultAgentRecipientShouldBeFound("agentFee.greaterThan=" + SMALLER_AGENT_FEE);
    }

    @Test
    @Transactional
    void getAllAgentRecipientsBySystemCommissionUpIsEqualToSomething() throws Exception {
        // Initialize the database
        agentRecipientRepository.saveAndFlush(agentRecipient);

        // Get all the agentRecipientList where systemCommissionUp equals to DEFAULT_SYSTEM_COMMISSION_UP
        defaultAgentRecipientShouldBeFound("systemCommissionUp.equals=" + DEFAULT_SYSTEM_COMMISSION_UP);

        // Get all the agentRecipientList where systemCommissionUp equals to UPDATED_SYSTEM_COMMISSION_UP
        defaultAgentRecipientShouldNotBeFound("systemCommissionUp.equals=" + UPDATED_SYSTEM_COMMISSION_UP);
    }

    @Test
    @Transactional
    void getAllAgentRecipientsBySystemCommissionUpIsNotEqualToSomething() throws Exception {
        // Initialize the database
        agentRecipientRepository.saveAndFlush(agentRecipient);

        // Get all the agentRecipientList where systemCommissionUp not equals to DEFAULT_SYSTEM_COMMISSION_UP
        defaultAgentRecipientShouldNotBeFound("systemCommissionUp.notEquals=" + DEFAULT_SYSTEM_COMMISSION_UP);

        // Get all the agentRecipientList where systemCommissionUp not equals to UPDATED_SYSTEM_COMMISSION_UP
        defaultAgentRecipientShouldBeFound("systemCommissionUp.notEquals=" + UPDATED_SYSTEM_COMMISSION_UP);
    }

    @Test
    @Transactional
    void getAllAgentRecipientsBySystemCommissionUpIsInShouldWork() throws Exception {
        // Initialize the database
        agentRecipientRepository.saveAndFlush(agentRecipient);

        // Get all the agentRecipientList where systemCommissionUp in DEFAULT_SYSTEM_COMMISSION_UP or UPDATED_SYSTEM_COMMISSION_UP
        defaultAgentRecipientShouldBeFound("systemCommissionUp.in=" + DEFAULT_SYSTEM_COMMISSION_UP + "," + UPDATED_SYSTEM_COMMISSION_UP);

        // Get all the agentRecipientList where systemCommissionUp equals to UPDATED_SYSTEM_COMMISSION_UP
        defaultAgentRecipientShouldNotBeFound("systemCommissionUp.in=" + UPDATED_SYSTEM_COMMISSION_UP);
    }

    @Test
    @Transactional
    void getAllAgentRecipientsBySystemCommissionUpIsNullOrNotNull() throws Exception {
        // Initialize the database
        agentRecipientRepository.saveAndFlush(agentRecipient);

        // Get all the agentRecipientList where systemCommissionUp is not null
        defaultAgentRecipientShouldBeFound("systemCommissionUp.specified=true");

        // Get all the agentRecipientList where systemCommissionUp is null
        defaultAgentRecipientShouldNotBeFound("systemCommissionUp.specified=false");
    }

    @Test
    @Transactional
    void getAllAgentRecipientsBySystemCommissionUpIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        agentRecipientRepository.saveAndFlush(agentRecipient);

        // Get all the agentRecipientList where systemCommissionUp is greater than or equal to DEFAULT_SYSTEM_COMMISSION_UP
        defaultAgentRecipientShouldBeFound("systemCommissionUp.greaterThanOrEqual=" + DEFAULT_SYSTEM_COMMISSION_UP);

        // Get all the agentRecipientList where systemCommissionUp is greater than or equal to (DEFAULT_SYSTEM_COMMISSION_UP + 1)
        defaultAgentRecipientShouldNotBeFound("systemCommissionUp.greaterThanOrEqual=" + (DEFAULT_SYSTEM_COMMISSION_UP + 1));
    }

    @Test
    @Transactional
    void getAllAgentRecipientsBySystemCommissionUpIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        agentRecipientRepository.saveAndFlush(agentRecipient);

        // Get all the agentRecipientList where systemCommissionUp is less than or equal to DEFAULT_SYSTEM_COMMISSION_UP
        defaultAgentRecipientShouldBeFound("systemCommissionUp.lessThanOrEqual=" + DEFAULT_SYSTEM_COMMISSION_UP);

        // Get all the agentRecipientList where systemCommissionUp is less than or equal to SMALLER_SYSTEM_COMMISSION_UP
        defaultAgentRecipientShouldNotBeFound("systemCommissionUp.lessThanOrEqual=" + SMALLER_SYSTEM_COMMISSION_UP);
    }

    @Test
    @Transactional
    void getAllAgentRecipientsBySystemCommissionUpIsLessThanSomething() throws Exception {
        // Initialize the database
        agentRecipientRepository.saveAndFlush(agentRecipient);

        // Get all the agentRecipientList where systemCommissionUp is less than DEFAULT_SYSTEM_COMMISSION_UP
        defaultAgentRecipientShouldNotBeFound("systemCommissionUp.lessThan=" + DEFAULT_SYSTEM_COMMISSION_UP);

        // Get all the agentRecipientList where systemCommissionUp is less than (DEFAULT_SYSTEM_COMMISSION_UP + 1)
        defaultAgentRecipientShouldBeFound("systemCommissionUp.lessThan=" + (DEFAULT_SYSTEM_COMMISSION_UP + 1));
    }

    @Test
    @Transactional
    void getAllAgentRecipientsBySystemCommissionUpIsGreaterThanSomething() throws Exception {
        // Initialize the database
        agentRecipientRepository.saveAndFlush(agentRecipient);

        // Get all the agentRecipientList where systemCommissionUp is greater than DEFAULT_SYSTEM_COMMISSION_UP
        defaultAgentRecipientShouldNotBeFound("systemCommissionUp.greaterThan=" + DEFAULT_SYSTEM_COMMISSION_UP);

        // Get all the agentRecipientList where systemCommissionUp is greater than SMALLER_SYSTEM_COMMISSION_UP
        defaultAgentRecipientShouldBeFound("systemCommissionUp.greaterThan=" + SMALLER_SYSTEM_COMMISSION_UP);
    }

    @Test
    @Transactional
    void getAllAgentRecipientsBySystemCommissionDownIsEqualToSomething() throws Exception {
        // Initialize the database
        agentRecipientRepository.saveAndFlush(agentRecipient);

        // Get all the agentRecipientList where systemCommissionDown equals to DEFAULT_SYSTEM_COMMISSION_DOWN
        defaultAgentRecipientShouldBeFound("systemCommissionDown.equals=" + DEFAULT_SYSTEM_COMMISSION_DOWN);

        // Get all the agentRecipientList where systemCommissionDown equals to UPDATED_SYSTEM_COMMISSION_DOWN
        defaultAgentRecipientShouldNotBeFound("systemCommissionDown.equals=" + UPDATED_SYSTEM_COMMISSION_DOWN);
    }

    @Test
    @Transactional
    void getAllAgentRecipientsBySystemCommissionDownIsNotEqualToSomething() throws Exception {
        // Initialize the database
        agentRecipientRepository.saveAndFlush(agentRecipient);

        // Get all the agentRecipientList where systemCommissionDown not equals to DEFAULT_SYSTEM_COMMISSION_DOWN
        defaultAgentRecipientShouldNotBeFound("systemCommissionDown.notEquals=" + DEFAULT_SYSTEM_COMMISSION_DOWN);

        // Get all the agentRecipientList where systemCommissionDown not equals to UPDATED_SYSTEM_COMMISSION_DOWN
        defaultAgentRecipientShouldBeFound("systemCommissionDown.notEquals=" + UPDATED_SYSTEM_COMMISSION_DOWN);
    }

    @Test
    @Transactional
    void getAllAgentRecipientsBySystemCommissionDownIsInShouldWork() throws Exception {
        // Initialize the database
        agentRecipientRepository.saveAndFlush(agentRecipient);

        // Get all the agentRecipientList where systemCommissionDown in DEFAULT_SYSTEM_COMMISSION_DOWN or UPDATED_SYSTEM_COMMISSION_DOWN
        defaultAgentRecipientShouldBeFound(
                "systemCommissionDown.in=" + DEFAULT_SYSTEM_COMMISSION_DOWN + "," + UPDATED_SYSTEM_COMMISSION_DOWN
        );

        // Get all the agentRecipientList where systemCommissionDown equals to UPDATED_SYSTEM_COMMISSION_DOWN
        defaultAgentRecipientShouldNotBeFound("systemCommissionDown.in=" + UPDATED_SYSTEM_COMMISSION_DOWN);
    }

    @Test
    @Transactional
    void getAllAgentRecipientsBySystemCommissionDownIsNullOrNotNull() throws Exception {
        // Initialize the database
        agentRecipientRepository.saveAndFlush(agentRecipient);

        // Get all the agentRecipientList where systemCommissionDown is not null
        defaultAgentRecipientShouldBeFound("systemCommissionDown.specified=true");

        // Get all the agentRecipientList where systemCommissionDown is null
        defaultAgentRecipientShouldNotBeFound("systemCommissionDown.specified=false");
    }

    @Test
    @Transactional
    void getAllAgentRecipientsBySystemCommissionDownIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        agentRecipientRepository.saveAndFlush(agentRecipient);

        // Get all the agentRecipientList where systemCommissionDown is greater than or equal to DEFAULT_SYSTEM_COMMISSION_DOWN
        defaultAgentRecipientShouldBeFound("systemCommissionDown.greaterThanOrEqual=" + DEFAULT_SYSTEM_COMMISSION_DOWN);

        // Get all the agentRecipientList where systemCommissionDown is greater than or equal to (DEFAULT_SYSTEM_COMMISSION_DOWN + 1)
        defaultAgentRecipientShouldNotBeFound("systemCommissionDown.greaterThanOrEqual=" + (DEFAULT_SYSTEM_COMMISSION_DOWN + 1));
    }

    @Test
    @Transactional
    void getAllAgentRecipientsBySystemCommissionDownIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        agentRecipientRepository.saveAndFlush(agentRecipient);

        // Get all the agentRecipientList where systemCommissionDown is less than or equal to DEFAULT_SYSTEM_COMMISSION_DOWN
        defaultAgentRecipientShouldBeFound("systemCommissionDown.lessThanOrEqual=" + DEFAULT_SYSTEM_COMMISSION_DOWN);

        // Get all the agentRecipientList where systemCommissionDown is less than or equal to SMALLER_SYSTEM_COMMISSION_DOWN
        defaultAgentRecipientShouldNotBeFound("systemCommissionDown.lessThanOrEqual=" + SMALLER_SYSTEM_COMMISSION_DOWN);
    }

    @Test
    @Transactional
    void getAllAgentRecipientsBySystemCommissionDownIsLessThanSomething() throws Exception {
        // Initialize the database
        agentRecipientRepository.saveAndFlush(agentRecipient);

        // Get all the agentRecipientList where systemCommissionDown is less than DEFAULT_SYSTEM_COMMISSION_DOWN
        defaultAgentRecipientShouldNotBeFound("systemCommissionDown.lessThan=" + DEFAULT_SYSTEM_COMMISSION_DOWN);

        // Get all the agentRecipientList where systemCommissionDown is less than (DEFAULT_SYSTEM_COMMISSION_DOWN + 1)
        defaultAgentRecipientShouldBeFound("systemCommissionDown.lessThan=" + (DEFAULT_SYSTEM_COMMISSION_DOWN + 1));
    }

    @Test
    @Transactional
    void getAllAgentRecipientsBySystemCommissionDownIsGreaterThanSomething() throws Exception {
        // Initialize the database
        agentRecipientRepository.saveAndFlush(agentRecipient);

        // Get all the agentRecipientList where systemCommissionDown is greater than DEFAULT_SYSTEM_COMMISSION_DOWN
        defaultAgentRecipientShouldNotBeFound("systemCommissionDown.greaterThan=" + DEFAULT_SYSTEM_COMMISSION_DOWN);

        // Get all the agentRecipientList where systemCommissionDown is greater than SMALLER_SYSTEM_COMMISSION_DOWN
        defaultAgentRecipientShouldBeFound("systemCommissionDown.greaterThan=" + SMALLER_SYSTEM_COMMISSION_DOWN);
    }

    @Test
    @Transactional
    void getAllAgentRecipientsBySystemFeeIsEqualToSomething() throws Exception {
        // Initialize the database
        agentRecipientRepository.saveAndFlush(agentRecipient);

        // Get all the agentRecipientList where systemFee equals to DEFAULT_SYSTEM_FEE
        defaultAgentRecipientShouldBeFound("systemFee.equals=" + DEFAULT_SYSTEM_FEE);

        // Get all the agentRecipientList where systemFee equals to UPDATED_SYSTEM_FEE
        defaultAgentRecipientShouldNotBeFound("systemFee.equals=" + UPDATED_SYSTEM_FEE);
    }

    @Test
    @Transactional
    void getAllAgentRecipientsBySystemFeeIsNotEqualToSomething() throws Exception {
        // Initialize the database
        agentRecipientRepository.saveAndFlush(agentRecipient);

        // Get all the agentRecipientList where systemFee not equals to DEFAULT_SYSTEM_FEE
        defaultAgentRecipientShouldNotBeFound("systemFee.notEquals=" + DEFAULT_SYSTEM_FEE);

        // Get all the agentRecipientList where systemFee not equals to UPDATED_SYSTEM_FEE
        defaultAgentRecipientShouldBeFound("systemFee.notEquals=" + UPDATED_SYSTEM_FEE);
    }

    @Test
    @Transactional
    void getAllAgentRecipientsBySystemFeeIsInShouldWork() throws Exception {
        // Initialize the database
        agentRecipientRepository.saveAndFlush(agentRecipient);

        // Get all the agentRecipientList where systemFee in DEFAULT_SYSTEM_FEE or UPDATED_SYSTEM_FEE
        defaultAgentRecipientShouldBeFound("systemFee.in=" + DEFAULT_SYSTEM_FEE + "," + UPDATED_SYSTEM_FEE);

        // Get all the agentRecipientList where systemFee equals to UPDATED_SYSTEM_FEE
        defaultAgentRecipientShouldNotBeFound("systemFee.in=" + UPDATED_SYSTEM_FEE);
    }

    @Test
    @Transactional
    void getAllAgentRecipientsBySystemFeeIsNullOrNotNull() throws Exception {
        // Initialize the database
        agentRecipientRepository.saveAndFlush(agentRecipient);

        // Get all the agentRecipientList where systemFee is not null
        defaultAgentRecipientShouldBeFound("systemFee.specified=true");

        // Get all the agentRecipientList where systemFee is null
        defaultAgentRecipientShouldNotBeFound("systemFee.specified=false");
    }

    @Test
    @Transactional
    void getAllAgentRecipientsBySystemFeeIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        agentRecipientRepository.saveAndFlush(agentRecipient);

        // Get all the agentRecipientList where systemFee is greater than or equal to DEFAULT_SYSTEM_FEE
        defaultAgentRecipientShouldBeFound("systemFee.greaterThanOrEqual=" + DEFAULT_SYSTEM_FEE);

        // Get all the agentRecipientList where systemFee is greater than or equal to (DEFAULT_SYSTEM_FEE + 1)
        defaultAgentRecipientShouldNotBeFound("systemFee.greaterThanOrEqual=" + (DEFAULT_SYSTEM_FEE + 1));
    }

    @Test
    @Transactional
    void getAllAgentRecipientsBySystemFeeIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        agentRecipientRepository.saveAndFlush(agentRecipient);

        // Get all the agentRecipientList where systemFee is less than or equal to DEFAULT_SYSTEM_FEE
        defaultAgentRecipientShouldBeFound("systemFee.lessThanOrEqual=" + DEFAULT_SYSTEM_FEE);

        // Get all the agentRecipientList where systemFee is less than or equal to SMALLER_SYSTEM_FEE
        defaultAgentRecipientShouldNotBeFound("systemFee.lessThanOrEqual=" + SMALLER_SYSTEM_FEE);
    }

    @Test
    @Transactional
    void getAllAgentRecipientsBySystemFeeIsLessThanSomething() throws Exception {
        // Initialize the database
        agentRecipientRepository.saveAndFlush(agentRecipient);

        // Get all the agentRecipientList where systemFee is less than DEFAULT_SYSTEM_FEE
        defaultAgentRecipientShouldNotBeFound("systemFee.lessThan=" + DEFAULT_SYSTEM_FEE);

        // Get all the agentRecipientList where systemFee is less than (DEFAULT_SYSTEM_FEE + 1)
        defaultAgentRecipientShouldBeFound("systemFee.lessThan=" + (DEFAULT_SYSTEM_FEE + 1));
    }

    @Test
    @Transactional
    void getAllAgentRecipientsBySystemFeeIsGreaterThanSomething() throws Exception {
        // Initialize the database
        agentRecipientRepository.saveAndFlush(agentRecipient);

        // Get all the agentRecipientList where systemFee is greater than DEFAULT_SYSTEM_FEE
        defaultAgentRecipientShouldNotBeFound("systemFee.greaterThan=" + DEFAULT_SYSTEM_FEE);

        // Get all the agentRecipientList where systemFee is greater than SMALLER_SYSTEM_FEE
        defaultAgentRecipientShouldBeFound("systemFee.greaterThan=" + SMALLER_SYSTEM_FEE);
    }

    @Test
    @Transactional
    void getAllAgentRecipientsByActiveIsEqualToSomething() throws Exception {
        // Initialize the database
        agentRecipientRepository.saveAndFlush(agentRecipient);

        // Get all the agentRecipientList where active equals to DEFAULT_ACTIVE
        defaultAgentRecipientShouldBeFound("active.equals=" + DEFAULT_ACTIVE);

        // Get all the agentRecipientList where active equals to UPDATED_ACTIVE
        defaultAgentRecipientShouldNotBeFound("active.equals=" + UPDATED_ACTIVE);
    }

    @Test
    @Transactional
    void getAllAgentRecipientsByActiveIsNotEqualToSomething() throws Exception {
        // Initialize the database
        agentRecipientRepository.saveAndFlush(agentRecipient);

        // Get all the agentRecipientList where active not equals to DEFAULT_ACTIVE
        defaultAgentRecipientShouldNotBeFound("active.notEquals=" + DEFAULT_ACTIVE);

        // Get all the agentRecipientList where active not equals to UPDATED_ACTIVE
        defaultAgentRecipientShouldBeFound("active.notEquals=" + UPDATED_ACTIVE);
    }

    @Test
    @Transactional
    void getAllAgentRecipientsByActiveIsInShouldWork() throws Exception {
        // Initialize the database
        agentRecipientRepository.saveAndFlush(agentRecipient);

        // Get all the agentRecipientList where active in DEFAULT_ACTIVE or UPDATED_ACTIVE
        defaultAgentRecipientShouldBeFound("active.in=" + DEFAULT_ACTIVE + "," + UPDATED_ACTIVE);

        // Get all the agentRecipientList where active equals to UPDATED_ACTIVE
        defaultAgentRecipientShouldNotBeFound("active.in=" + UPDATED_ACTIVE);
    }

    @Test
    @Transactional
    void getAllAgentRecipientsByActiveIsNullOrNotNull() throws Exception {
        // Initialize the database
        agentRecipientRepository.saveAndFlush(agentRecipient);

        // Get all the agentRecipientList where active is not null
        defaultAgentRecipientShouldBeFound("active.specified=true");

        // Get all the agentRecipientList where active is null
        defaultAgentRecipientShouldNotBeFound("active.specified=false");
    }

    @Test
    @Transactional
    void getAllAgentRecipientsByEposPortIsEqualToSomething() throws Exception {
        // Initialize the database
        agentRecipientRepository.saveAndFlush(agentRecipient);

        // Get all the agentRecipientList where eposPort equals to DEFAULT_EPOS_PORT
        defaultAgentRecipientShouldBeFound("eposPort.equals=" + DEFAULT_EPOS_PORT);

        // Get all the agentRecipientList where eposPort equals to UPDATED_EPOS_PORT
        defaultAgentRecipientShouldNotBeFound("eposPort.equals=" + UPDATED_EPOS_PORT);
    }

    @Test
    @Transactional
    void getAllAgentRecipientsByEposPortIsNotEqualToSomething() throws Exception {
        // Initialize the database
        agentRecipientRepository.saveAndFlush(agentRecipient);

        // Get all the agentRecipientList where eposPort not equals to DEFAULT_EPOS_PORT
        defaultAgentRecipientShouldNotBeFound("eposPort.notEquals=" + DEFAULT_EPOS_PORT);

        // Get all the agentRecipientList where eposPort not equals to UPDATED_EPOS_PORT
        defaultAgentRecipientShouldBeFound("eposPort.notEquals=" + UPDATED_EPOS_PORT);
    }

    @Test
    @Transactional
    void getAllAgentRecipientsByEposPortIsInShouldWork() throws Exception {
        // Initialize the database
        agentRecipientRepository.saveAndFlush(agentRecipient);

        // Get all the agentRecipientList where eposPort in DEFAULT_EPOS_PORT or UPDATED_EPOS_PORT
        defaultAgentRecipientShouldBeFound("eposPort.in=" + DEFAULT_EPOS_PORT + "," + UPDATED_EPOS_PORT);

        // Get all the agentRecipientList where eposPort equals to UPDATED_EPOS_PORT
        defaultAgentRecipientShouldNotBeFound("eposPort.in=" + UPDATED_EPOS_PORT);
    }

    @Test
    @Transactional
    void getAllAgentRecipientsByEposPortIsNullOrNotNull() throws Exception {
        // Initialize the database
        agentRecipientRepository.saveAndFlush(agentRecipient);

        // Get all the agentRecipientList where eposPort is not null
        defaultAgentRecipientShouldBeFound("eposPort.specified=true");

        // Get all the agentRecipientList where eposPort is null
        defaultAgentRecipientShouldNotBeFound("eposPort.specified=false");
    }

    @Test
    @Transactional
    void getAllAgentRecipientsByEposPortIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        agentRecipientRepository.saveAndFlush(agentRecipient);

        // Get all the agentRecipientList where eposPort is greater than or equal to DEFAULT_EPOS_PORT
        defaultAgentRecipientShouldBeFound("eposPort.greaterThanOrEqual=" + DEFAULT_EPOS_PORT);

        // Get all the agentRecipientList where eposPort is greater than or equal to (DEFAULT_EPOS_PORT + 1)
        defaultAgentRecipientShouldNotBeFound("eposPort.greaterThanOrEqual=" + (DEFAULT_EPOS_PORT + 1));
    }

    @Test
    @Transactional
    void getAllAgentRecipientsByEposPortIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        agentRecipientRepository.saveAndFlush(agentRecipient);

        // Get all the agentRecipientList where eposPort is less than or equal to DEFAULT_EPOS_PORT
        defaultAgentRecipientShouldBeFound("eposPort.lessThanOrEqual=" + DEFAULT_EPOS_PORT);

        // Get all the agentRecipientList where eposPort is less than or equal to SMALLER_EPOS_PORT
        defaultAgentRecipientShouldNotBeFound("eposPort.lessThanOrEqual=" + SMALLER_EPOS_PORT);
    }

    @Test
    @Transactional
    void getAllAgentRecipientsByEposPortIsLessThanSomething() throws Exception {
        // Initialize the database
        agentRecipientRepository.saveAndFlush(agentRecipient);

        // Get all the agentRecipientList where eposPort is less than DEFAULT_EPOS_PORT
        defaultAgentRecipientShouldNotBeFound("eposPort.lessThan=" + DEFAULT_EPOS_PORT);

        // Get all the agentRecipientList where eposPort is less than (DEFAULT_EPOS_PORT + 1)
        defaultAgentRecipientShouldBeFound("eposPort.lessThan=" + (DEFAULT_EPOS_PORT + 1));
    }

    @Test
    @Transactional
    void getAllAgentRecipientsByEposPortIsGreaterThanSomething() throws Exception {
        // Initialize the database
        agentRecipientRepository.saveAndFlush(agentRecipient);

        // Get all the agentRecipientList where eposPort is greater than DEFAULT_EPOS_PORT
        defaultAgentRecipientShouldNotBeFound("eposPort.greaterThan=" + DEFAULT_EPOS_PORT);

        // Get all the agentRecipientList where eposPort is greater than SMALLER_EPOS_PORT
        defaultAgentRecipientShouldBeFound("eposPort.greaterThan=" + SMALLER_EPOS_PORT);
    }

    @Test
    @Transactional
    void getAllAgentRecipientsByEposMerchantIsEqualToSomething() throws Exception {
        // Initialize the database
        agentRecipientRepository.saveAndFlush(agentRecipient);

        // Get all the agentRecipientList where eposMerchant equals to DEFAULT_EPOS_MERCHANT
        defaultAgentRecipientShouldBeFound("eposMerchant.equals=" + DEFAULT_EPOS_MERCHANT);

        // Get all the agentRecipientList where eposMerchant equals to UPDATED_EPOS_MERCHANT
        defaultAgentRecipientShouldNotBeFound("eposMerchant.equals=" + UPDATED_EPOS_MERCHANT);
    }

    @Test
    @Transactional
    void getAllAgentRecipientsByEposMerchantIsNotEqualToSomething() throws Exception {
        // Initialize the database
        agentRecipientRepository.saveAndFlush(agentRecipient);

        // Get all the agentRecipientList where eposMerchant not equals to DEFAULT_EPOS_MERCHANT
        defaultAgentRecipientShouldNotBeFound("eposMerchant.notEquals=" + DEFAULT_EPOS_MERCHANT);

        // Get all the agentRecipientList where eposMerchant not equals to UPDATED_EPOS_MERCHANT
        defaultAgentRecipientShouldBeFound("eposMerchant.notEquals=" + UPDATED_EPOS_MERCHANT);
    }

    @Test
    @Transactional
    void getAllAgentRecipientsByEposMerchantIsInShouldWork() throws Exception {
        // Initialize the database
        agentRecipientRepository.saveAndFlush(agentRecipient);

        // Get all the agentRecipientList where eposMerchant in DEFAULT_EPOS_MERCHANT or UPDATED_EPOS_MERCHANT
        defaultAgentRecipientShouldBeFound("eposMerchant.in=" + DEFAULT_EPOS_MERCHANT + "," + UPDATED_EPOS_MERCHANT);

        // Get all the agentRecipientList where eposMerchant equals to UPDATED_EPOS_MERCHANT
        defaultAgentRecipientShouldNotBeFound("eposMerchant.in=" + UPDATED_EPOS_MERCHANT);
    }

    @Test
    @Transactional
    void getAllAgentRecipientsByEposMerchantIsNullOrNotNull() throws Exception {
        // Initialize the database
        agentRecipientRepository.saveAndFlush(agentRecipient);

        // Get all the agentRecipientList where eposMerchant is not null
        defaultAgentRecipientShouldBeFound("eposMerchant.specified=true");

        // Get all the agentRecipientList where eposMerchant is null
        defaultAgentRecipientShouldNotBeFound("eposMerchant.specified=false");
    }

    @Test
    @Transactional
    void getAllAgentRecipientsByEposMerchantContainsSomething() throws Exception {
        // Initialize the database
        agentRecipientRepository.saveAndFlush(agentRecipient);

        // Get all the agentRecipientList where eposMerchant contains DEFAULT_EPOS_MERCHANT
        defaultAgentRecipientShouldBeFound("eposMerchant.contains=" + DEFAULT_EPOS_MERCHANT);

        // Get all the agentRecipientList where eposMerchant contains UPDATED_EPOS_MERCHANT
        defaultAgentRecipientShouldNotBeFound("eposMerchant.contains=" + UPDATED_EPOS_MERCHANT);
    }

    @Test
    @Transactional
    void getAllAgentRecipientsByEposMerchantNotContainsSomething() throws Exception {
        // Initialize the database
        agentRecipientRepository.saveAndFlush(agentRecipient);

        // Get all the agentRecipientList where eposMerchant does not contain DEFAULT_EPOS_MERCHANT
        defaultAgentRecipientShouldNotBeFound("eposMerchant.doesNotContain=" + DEFAULT_EPOS_MERCHANT);

        // Get all the agentRecipientList where eposMerchant does not contain UPDATED_EPOS_MERCHANT
        defaultAgentRecipientShouldBeFound("eposMerchant.doesNotContain=" + UPDATED_EPOS_MERCHANT);
    }

    @Test
    @Transactional
    void getAllAgentRecipientsByEposTerminalIsEqualToSomething() throws Exception {
        // Initialize the database
        agentRecipientRepository.saveAndFlush(agentRecipient);

        // Get all the agentRecipientList where eposTerminal equals to DEFAULT_EPOS_TERMINAL
        defaultAgentRecipientShouldBeFound("eposTerminal.equals=" + DEFAULT_EPOS_TERMINAL);

        // Get all the agentRecipientList where eposTerminal equals to UPDATED_EPOS_TERMINAL
        defaultAgentRecipientShouldNotBeFound("eposTerminal.equals=" + UPDATED_EPOS_TERMINAL);
    }

    @Test
    @Transactional
    void getAllAgentRecipientsByEposTerminalIsNotEqualToSomething() throws Exception {
        // Initialize the database
        agentRecipientRepository.saveAndFlush(agentRecipient);

        // Get all the agentRecipientList where eposTerminal not equals to DEFAULT_EPOS_TERMINAL
        defaultAgentRecipientShouldNotBeFound("eposTerminal.notEquals=" + DEFAULT_EPOS_TERMINAL);

        // Get all the agentRecipientList where eposTerminal not equals to UPDATED_EPOS_TERMINAL
        defaultAgentRecipientShouldBeFound("eposTerminal.notEquals=" + UPDATED_EPOS_TERMINAL);
    }

    @Test
    @Transactional
    void getAllAgentRecipientsByEposTerminalIsInShouldWork() throws Exception {
        // Initialize the database
        agentRecipientRepository.saveAndFlush(agentRecipient);

        // Get all the agentRecipientList where eposTerminal in DEFAULT_EPOS_TERMINAL or UPDATED_EPOS_TERMINAL
        defaultAgentRecipientShouldBeFound("eposTerminal.in=" + DEFAULT_EPOS_TERMINAL + "," + UPDATED_EPOS_TERMINAL);

        // Get all the agentRecipientList where eposTerminal equals to UPDATED_EPOS_TERMINAL
        defaultAgentRecipientShouldNotBeFound("eposTerminal.in=" + UPDATED_EPOS_TERMINAL);
    }

    @Test
    @Transactional
    void getAllAgentRecipientsByEposTerminalIsNullOrNotNull() throws Exception {
        // Initialize the database
        agentRecipientRepository.saveAndFlush(agentRecipient);

        // Get all the agentRecipientList where eposTerminal is not null
        defaultAgentRecipientShouldBeFound("eposTerminal.specified=true");

        // Get all the agentRecipientList where eposTerminal is null
        defaultAgentRecipientShouldNotBeFound("eposTerminal.specified=false");
    }

    @Test
    @Transactional
    void getAllAgentRecipientsByEposTerminalContainsSomething() throws Exception {
        // Initialize the database
        agentRecipientRepository.saveAndFlush(agentRecipient);

        // Get all the agentRecipientList where eposTerminal contains DEFAULT_EPOS_TERMINAL
        defaultAgentRecipientShouldBeFound("eposTerminal.contains=" + DEFAULT_EPOS_TERMINAL);

        // Get all the agentRecipientList where eposTerminal contains UPDATED_EPOS_TERMINAL
        defaultAgentRecipientShouldNotBeFound("eposTerminal.contains=" + UPDATED_EPOS_TERMINAL);
    }

    @Test
    @Transactional
    void getAllAgentRecipientsByEposTerminalNotContainsSomething() throws Exception {
        // Initialize the database
        agentRecipientRepository.saveAndFlush(agentRecipient);

        // Get all the agentRecipientList where eposTerminal does not contain DEFAULT_EPOS_TERMINAL
        defaultAgentRecipientShouldNotBeFound("eposTerminal.doesNotContain=" + DEFAULT_EPOS_TERMINAL);

        // Get all the agentRecipientList where eposTerminal does not contain UPDATED_EPOS_TERMINAL
        defaultAgentRecipientShouldBeFound("eposTerminal.doesNotContain=" + UPDATED_EPOS_TERMINAL);
    }

    @Test
    @Transactional
    void getAllAgentRecipientsByRequestGroupIsEqualToSomething() throws Exception {
        // Initialize the database
        agentRecipientRepository.saveAndFlush(agentRecipient);

        // Get all the agentRecipientList where requestGroup equals to DEFAULT_REQUEST_GROUP
        defaultAgentRecipientShouldBeFound("requestGroup.equals=" + DEFAULT_REQUEST_GROUP);

        // Get all the agentRecipientList where requestGroup equals to UPDATED_REQUEST_GROUP
        defaultAgentRecipientShouldNotBeFound("requestGroup.equals=" + UPDATED_REQUEST_GROUP);
    }

    @Test
    @Transactional
    void getAllAgentRecipientsByRequestGroupIsNotEqualToSomething() throws Exception {
        // Initialize the database
        agentRecipientRepository.saveAndFlush(agentRecipient);

        // Get all the agentRecipientList where requestGroup not equals to DEFAULT_REQUEST_GROUP
//        defaultAgentRecipientShouldNotBeFound("requestGroup.notEquals=" + DEFAULT_REQUEST_GROUP);

        // Get all the agentRecipientList where requestGroup not equals to UPDATED_REQUEST_GROUP
        defaultAgentRecipientShouldBeFound("requestGroup.notEquals=" + UPDATED_REQUEST_GROUP);
    }

    @Test
    @Transactional
    void getAllAgentRecipientsByRequestGroupIsInShouldWork() throws Exception {
        // Initialize the database
        agentRecipientRepository.saveAndFlush(agentRecipient);

        // Get all the agentRecipientList where requestGroup in DEFAULT_REQUEST_GROUP or UPDATED_REQUEST_GROUP
        defaultAgentRecipientShouldBeFound("requestGroup.in=" + DEFAULT_REQUEST_GROUP + "," + UPDATED_REQUEST_GROUP);

        // Get all the agentRecipientList where requestGroup equals to UPDATED_REQUEST_GROUP
        defaultAgentRecipientShouldNotBeFound("requestGroup.in=" + UPDATED_REQUEST_GROUP);
    }

    @Test
    @Transactional
    void getAllAgentRecipientsByRequestGroupIsNullOrNotNull() throws Exception {
        // Initialize the database
        agentRecipientRepository.saveAndFlush(agentRecipient);

        // Get all the agentRecipientList where requestGroup is not null
        defaultAgentRecipientShouldBeFound("requestGroup.specified=true");

        // Get all the agentRecipientList where requestGroup is null
        defaultAgentRecipientShouldNotBeFound("requestGroup.specified=false");
    }

    @Test
    @Transactional
    void getAllAgentRecipientsByRequestGroupContainsSomething() throws Exception {
        // Initialize the database
        agentRecipientRepository.saveAndFlush(agentRecipient);

        // Get all the agentRecipientList where requestGroup contains DEFAULT_REQUEST_GROUP
        defaultAgentRecipientShouldBeFound("requestGroup.contains=" + DEFAULT_REQUEST_GROUP);

        // Get all the agentRecipientList where requestGroup contains UPDATED_REQUEST_GROUP
        defaultAgentRecipientShouldNotBeFound("requestGroup.contains=" + UPDATED_REQUEST_GROUP);
    }

    @Test
    @Transactional
    void getAllAgentRecipientsByRequestGroupNotContainsSomething() throws Exception {
        // Initialize the database
        agentRecipientRepository.saveAndFlush(agentRecipient);

        // Get all the agentRecipientList where requestGroup does not contain DEFAULT_REQUEST_GROUP
        defaultAgentRecipientShouldNotBeFound("requestGroup.doesNotContain=" + DEFAULT_REQUEST_GROUP);

        // Get all the agentRecipientList where requestGroup does not contain UPDATED_REQUEST_GROUP
        defaultAgentRecipientShouldBeFound("requestGroup.doesNotContain=" + UPDATED_REQUEST_GROUP);
    }

    @Test
    @Transactional
    void getAllAgentRecipientsByAgentIsEqualToSomething() throws Exception {
        // Initialize the database
        agentRecipientRepository.saveAndFlush(agentRecipient);
        Agent agent = AgentResourceIT.createEntity(em);
        em.persist(agent);
        em.flush();
        agentRecipient.setAgent(agent);
        agentRecipientRepository.saveAndFlush(agentRecipient);
        Long agentId = agent.getId();

        // Get all the agentRecipientList where agent equals to agentId
        defaultAgentRecipientShouldBeFound("agentId.equals=" + agentId);

        // Get all the agentRecipientList where agent equals to (agentId + 1)
        defaultAgentRecipientShouldNotBeFound("agentId.equals=" + (agentId + 1));
    }

    @Test
    @Transactional
    void getAllAgentRecipientsByRecipientIsEqualToSomething() throws Exception {
        // Initialize the database
        agentRecipientRepository.saveAndFlush(agentRecipient);
        Recipient recipient = RecipientResourceIT.createEntity(em);
        em.persist(recipient);
        em.flush();
        agentRecipient.setRecipient(recipient);
        agentRecipientRepository.saveAndFlush(agentRecipient);
        Long recipientId = recipient.getId();

        // Get all the agentRecipientList where recipient equals to recipientId
        defaultAgentRecipientShouldBeFound("recipientId.equals=" + recipientId);

        // Get all the agentRecipientList where recipient equals to (recipientId + 1)
        defaultAgentRecipientShouldNotBeFound("recipientId.equals=" + (recipientId + 1));
    }

    @Test
    @Transactional
    void getAllAgentRecipientsBySettingIsEqualToSomething() throws Exception {
        // Initialize the database
        agentRecipientRepository.saveAndFlush(agentRecipient);
        Setting setting = SettingResourceIT.createEntity(em);
        em.persist(setting);
        em.flush();
        agentRecipient.setSetting(setting);
        agentRecipientRepository.saveAndFlush(agentRecipient);
        Long settingId = setting.getId();

        // Get all the agentRecipientList where setting equals to settingId
        defaultAgentRecipientShouldBeFound("settingId.equals=" + settingId);

        // Get all the agentRecipientList where setting equals to (settingId + 1)
        defaultAgentRecipientShouldNotBeFound("settingId.equals=" + (settingId + 1));
    }

    @Test
    @Transactional
    void getAllAgentRecipientsByRecipientGroupIsEqualToSomething() throws Exception {
        // Initialize the database
        agentRecipientRepository.saveAndFlush(agentRecipient);
        RecipientGroup recipientGroup = RecipientGroupResourceIT.createEntity(em);
        em.persist(recipientGroup);
        em.flush();
        agentRecipient.setRecipientGroup(recipientGroup);
        agentRecipientRepository.saveAndFlush(agentRecipient);
        Long recipientGroupId = recipientGroup.getId();

        // Get all the agentRecipientList where recipientGroup equals to recipientGroupId
        defaultAgentRecipientShouldBeFound("recipientGroupId.equals=" + recipientGroupId);

        // Get all the agentRecipientList where recipientGroup equals to (recipientGroupId + 1)
        defaultAgentRecipientShouldNotBeFound("recipientGroupId.equals=" + (recipientGroupId + 1));
    }

    /**
     * Executes the search, and checks that the default entity is returned.
     */
    private void defaultAgentRecipientShouldBeFound(String filter) throws Exception {
        restAgentRecipientMockMvc
                .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.[*].id").value(hasItem(agentRecipient.getId().intValue())))
                .andExpect(jsonPath("$.[*].agentCommission").value(hasItem(DEFAULT_AGENT_COMMISSION.doubleValue())))
                .andExpect(jsonPath("$.[*].agentFee").value(hasItem(DEFAULT_AGENT_FEE.doubleValue())))
                .andExpect(jsonPath("$.[*].systemCommissionUp").value(hasItem(DEFAULT_SYSTEM_COMMISSION_UP.doubleValue())))
                .andExpect(jsonPath("$.[*].systemCommissionDown").value(hasItem(DEFAULT_SYSTEM_COMMISSION_DOWN.doubleValue())))
                .andExpect(jsonPath("$.[*].systemFee").value(hasItem(DEFAULT_SYSTEM_FEE.doubleValue())))
                .andExpect(jsonPath("$.[*].active").value(hasItem(DEFAULT_ACTIVE.booleanValue())))
                .andExpect(jsonPath("$.[*].eposPort").value(hasItem(DEFAULT_EPOS_PORT)))
                .andExpect(jsonPath("$.[*].eposMerchant").value(hasItem(DEFAULT_EPOS_MERCHANT)))
                .andExpect(jsonPath("$.[*].eposTerminal").value(hasItem(DEFAULT_EPOS_TERMINAL)))
                .andExpect(jsonPath("$.[*].requestGroup").value(hasItem(DEFAULT_REQUEST_GROUP)))
                .andExpect(jsonPath("$.[*].jsonParams").value(hasItem(DEFAULT_JSON_PARAMS.toString())));

        // Check, that the count call also returns 1
        restAgentRecipientMockMvc
                .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(content().string("1"));
    }

    /**
     * Executes the search, and checks that the default entity is not returned.
     */
    private void defaultAgentRecipientShouldNotBeFound(String filter) throws Exception {
        restAgentRecipientMockMvc
                .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());

        // Check, that the count call also returns 0
        restAgentRecipientMockMvc
                .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(content().string("0"));
    }

    @Test
    @Transactional
    void getNonExistingAgentRecipient() throws Exception {
        // Get the agentRecipient
        restAgentRecipientMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putNewAgentRecipient() throws Exception {
        // Initialize the database
        agentRecipientRepository.saveAndFlush(agentRecipient);

        int databaseSizeBeforeUpdate = agentRecipientRepository.findAll().size();

        // Update the agentRecipient
        AgentRecipient updatedAgentRecipient = agentRecipientRepository.findById(agentRecipient.getId()).get();
        // Disconnect from session so that the updates on updatedAgentRecipient are not directly saved in db
        em.detach(updatedAgentRecipient);
        updatedAgentRecipient
                .agentCommission(UPDATED_AGENT_COMMISSION)
                .agentFee(UPDATED_AGENT_FEE)
                .systemCommissionUp(UPDATED_SYSTEM_COMMISSION_UP)
                .systemCommissionDown(UPDATED_SYSTEM_COMMISSION_DOWN)
                .systemFee(UPDATED_SYSTEM_FEE)
                .active(UPDATED_ACTIVE)
                .eposPort(UPDATED_EPOS_PORT)
                .eposMerchant(UPDATED_EPOS_MERCHANT)
                .eposTerminal(UPDATED_EPOS_TERMINAL)
                .jsonParams(UPDATED_JSON_PARAMS);
        AgentRecipientDTO agentRecipientDTO = agentRecipientMapper.toDto(updatedAgentRecipient);

        restAgentRecipientMockMvc
                .perform(
                        put(ENTITY_API_URL_ID, agentRecipientDTO.getId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(TestUtil.convertObjectToJsonBytes(agentRecipientDTO))
                )
                .andExpect(status().isOk());

        // Validate the AgentRecipient in the database
        List<AgentRecipient> agentRecipientList = agentRecipientRepository.findAll();
        assertThat(agentRecipientList).hasSize(databaseSizeBeforeUpdate);
        AgentRecipient testAgentRecipient = agentRecipientList.get(agentRecipientList.size() - 1);
        assertThat(testAgentRecipient.getAgentCommission()).isEqualTo(UPDATED_AGENT_COMMISSION);
        assertThat(testAgentRecipient.getAgentFee()).isEqualTo(UPDATED_AGENT_FEE);
        assertThat(testAgentRecipient.getSystemCommissionUp()).isEqualTo(UPDATED_SYSTEM_COMMISSION_UP);
        assertThat(testAgentRecipient.getSystemCommissionDown()).isEqualTo(UPDATED_SYSTEM_COMMISSION_DOWN);
        assertThat(testAgentRecipient.getSystemFee()).isEqualTo(UPDATED_SYSTEM_FEE);
        assertThat(testAgentRecipient.getActive()).isEqualTo(UPDATED_ACTIVE);
        assertThat(testAgentRecipient.getEposPort()).isEqualTo(UPDATED_EPOS_PORT);
        assertThat(testAgentRecipient.getEposMerchant()).isEqualTo(UPDATED_EPOS_MERCHANT);
        assertThat(testAgentRecipient.getEposTerminal()).isEqualTo(UPDATED_EPOS_TERMINAL);
        assertThat(testAgentRecipient.getJsonParams()).isEqualTo(UPDATED_JSON_PARAMS);
    }

    @Test
    @Transactional
    void putNonExistingAgentRecipient() throws Exception {
        int databaseSizeBeforeUpdate = agentRecipientRepository.findAll().size();
        agentRecipient.setId(count.incrementAndGet());

        // Create the AgentRecipient
        AgentRecipientDTO agentRecipientDTO = agentRecipientMapper.toDto(agentRecipient);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restAgentRecipientMockMvc
                .perform(
                        put(ENTITY_API_URL_ID, agentRecipientDTO.getId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(TestUtil.convertObjectToJsonBytes(agentRecipientDTO))
                )
                .andExpect(status().isBadRequest());

        // Validate the AgentRecipient in the database
        List<AgentRecipient> agentRecipientList = agentRecipientRepository.findAll();
        assertThat(agentRecipientList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchAgentRecipient() throws Exception {
        int databaseSizeBeforeUpdate = agentRecipientRepository.findAll().size();
        agentRecipient.setId(count.incrementAndGet());

        // Create the AgentRecipient
        AgentRecipientDTO agentRecipientDTO = agentRecipientMapper.toDto(agentRecipient);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restAgentRecipientMockMvc
                .perform(
                        put(ENTITY_API_URL_ID, count.incrementAndGet())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(TestUtil.convertObjectToJsonBytes(agentRecipientDTO))
                )
                .andExpect(status().isBadRequest());

        // Validate the AgentRecipient in the database
        List<AgentRecipient> agentRecipientList = agentRecipientRepository.findAll();
        assertThat(agentRecipientList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamAgentRecipient() throws Exception {
        int databaseSizeBeforeUpdate = agentRecipientRepository.findAll().size();
        agentRecipient.setId(count.incrementAndGet());

        // Create the AgentRecipient
        AgentRecipientDTO agentRecipientDTO = agentRecipientMapper.toDto(agentRecipient);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restAgentRecipientMockMvc
                .perform(
                        put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(agentRecipientDTO))
                )
                .andExpect(status().isMethodNotAllowed());

        // Validate the AgentRecipient in the database
        List<AgentRecipient> agentRecipientList = agentRecipientRepository.findAll();
        assertThat(agentRecipientList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateAgentRecipientWithPatch() throws Exception {
        // Initialize the database
        agentRecipientRepository.saveAndFlush(agentRecipient);

        int databaseSizeBeforeUpdate = agentRecipientRepository.findAll().size();

        // Update the agentRecipient using partial update
        AgentRecipient partialUpdatedAgentRecipient = new AgentRecipient();
        partialUpdatedAgentRecipient.setId(agentRecipient.getId());

        partialUpdatedAgentRecipient.agentFee(UPDATED_AGENT_FEE).eposMerchant(UPDATED_EPOS_MERCHANT).eposTerminal(UPDATED_EPOS_TERMINAL);

        restAgentRecipientMockMvc
                .perform(
                        patch(ENTITY_API_URL_ID, partialUpdatedAgentRecipient.getId())
                                .contentType("application/merge-patch+json")
                                .content(TestUtil.convertObjectToJsonBytes(partialUpdatedAgentRecipient))
                )
                .andExpect(status().isOk());

        // Validate the AgentRecipient in the database
        List<AgentRecipient> agentRecipientList = agentRecipientRepository.findAll();
        assertThat(agentRecipientList).hasSize(databaseSizeBeforeUpdate);
        AgentRecipient testAgentRecipient = agentRecipientList.get(agentRecipientList.size() - 1);
        assertThat(testAgentRecipient.getAgentCommission()).isEqualTo(DEFAULT_AGENT_COMMISSION);
        assertThat(testAgentRecipient.getAgentFee()).isEqualTo(UPDATED_AGENT_FEE);
        assertThat(testAgentRecipient.getSystemCommissionUp()).isEqualTo(DEFAULT_SYSTEM_COMMISSION_UP);
        assertThat(testAgentRecipient.getSystemCommissionDown()).isEqualTo(DEFAULT_SYSTEM_COMMISSION_DOWN);
        assertThat(testAgentRecipient.getSystemFee()).isEqualTo(DEFAULT_SYSTEM_FEE);
        assertThat(testAgentRecipient.getActive()).isEqualTo(DEFAULT_ACTIVE);
        assertThat(testAgentRecipient.getEposPort()).isEqualTo(DEFAULT_EPOS_PORT);
        assertThat(testAgentRecipient.getEposMerchant()).isEqualTo(UPDATED_EPOS_MERCHANT);
        assertThat(testAgentRecipient.getEposTerminal()).isEqualTo(UPDATED_EPOS_TERMINAL);
        assertThat(testAgentRecipient.getJsonParams()).isEqualTo(DEFAULT_JSON_PARAMS);
    }

    @Test
    @Transactional
    void fullUpdateAgentRecipientWithPatch() throws Exception {
        // Initialize the database
        agentRecipientRepository.saveAndFlush(agentRecipient);

        int databaseSizeBeforeUpdate = agentRecipientRepository.findAll().size();

        // Update the agentRecipient using partial update
        AgentRecipient partialUpdatedAgentRecipient = new AgentRecipient();
        partialUpdatedAgentRecipient.setId(agentRecipient.getId());

        partialUpdatedAgentRecipient
                .agentCommission(UPDATED_AGENT_COMMISSION)
                .agentFee(UPDATED_AGENT_FEE)
                .systemCommissionUp(UPDATED_SYSTEM_COMMISSION_UP)
                .systemCommissionDown(UPDATED_SYSTEM_COMMISSION_DOWN)
                .systemFee(UPDATED_SYSTEM_FEE)
                .active(UPDATED_ACTIVE)
                .eposPort(UPDATED_EPOS_PORT)
                .eposMerchant(UPDATED_EPOS_MERCHANT)
                .eposTerminal(UPDATED_EPOS_TERMINAL)
                .jsonParams(UPDATED_JSON_PARAMS);

        restAgentRecipientMockMvc
                .perform(
                        patch(ENTITY_API_URL_ID, partialUpdatedAgentRecipient.getId())
                                .contentType("application/merge-patch+json")
                                .content(TestUtil.convertObjectToJsonBytes(partialUpdatedAgentRecipient))
                )
                .andExpect(status().isOk());

        // Validate the AgentRecipient in the database
        List<AgentRecipient> agentRecipientList = agentRecipientRepository.findAll();
        assertThat(agentRecipientList).hasSize(databaseSizeBeforeUpdate);
        AgentRecipient testAgentRecipient = agentRecipientList.get(agentRecipientList.size() - 1);
        assertThat(testAgentRecipient.getAgentCommission()).isEqualTo(UPDATED_AGENT_COMMISSION);
        assertThat(testAgentRecipient.getAgentFee()).isEqualTo(UPDATED_AGENT_FEE);
        assertThat(testAgentRecipient.getSystemCommissionUp()).isEqualTo(UPDATED_SYSTEM_COMMISSION_UP);
        assertThat(testAgentRecipient.getSystemCommissionDown()).isEqualTo(UPDATED_SYSTEM_COMMISSION_DOWN);
        assertThat(testAgentRecipient.getSystemFee()).isEqualTo(UPDATED_SYSTEM_FEE);
        assertThat(testAgentRecipient.getActive()).isEqualTo(UPDATED_ACTIVE);
        assertThat(testAgentRecipient.getEposPort()).isEqualTo(UPDATED_EPOS_PORT);
        assertThat(testAgentRecipient.getEposMerchant()).isEqualTo(UPDATED_EPOS_MERCHANT);
        assertThat(testAgentRecipient.getEposTerminal()).isEqualTo(UPDATED_EPOS_TERMINAL);
        assertThat(testAgentRecipient.getJsonParams()).isEqualTo(UPDATED_JSON_PARAMS);
    }

    @Test
    @Transactional
    void patchNonExistingAgentRecipient() throws Exception {
        int databaseSizeBeforeUpdate = agentRecipientRepository.findAll().size();
        agentRecipient.setId(count.incrementAndGet());

        // Create the AgentRecipient
        AgentRecipientDTO agentRecipientDTO = agentRecipientMapper.toDto(agentRecipient);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restAgentRecipientMockMvc
                .perform(
                        patch(ENTITY_API_URL_ID, agentRecipientDTO.getId())
                                .contentType("application/merge-patch+json")
                                .content(TestUtil.convertObjectToJsonBytes(agentRecipientDTO))
                )
                .andExpect(status().isBadRequest());

        // Validate the AgentRecipient in the database
        List<AgentRecipient> agentRecipientList = agentRecipientRepository.findAll();
        assertThat(agentRecipientList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchAgentRecipient() throws Exception {
        int databaseSizeBeforeUpdate = agentRecipientRepository.findAll().size();
        agentRecipient.setId(count.incrementAndGet());

        // Create the AgentRecipient
        AgentRecipientDTO agentRecipientDTO = agentRecipientMapper.toDto(agentRecipient);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restAgentRecipientMockMvc
                .perform(
                        patch(ENTITY_API_URL_ID, count.incrementAndGet())
                                .contentType("application/merge-patch+json")
                                .content(TestUtil.convertObjectToJsonBytes(agentRecipientDTO))
                )
                .andExpect(status().isBadRequest());

        // Validate the AgentRecipient in the database
        List<AgentRecipient> agentRecipientList = agentRecipientRepository.findAll();
        assertThat(agentRecipientList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamAgentRecipient() throws Exception {
        int databaseSizeBeforeUpdate = agentRecipientRepository.findAll().size();
        agentRecipient.setId(count.incrementAndGet());

        // Create the AgentRecipient
        AgentRecipientDTO agentRecipientDTO = agentRecipientMapper.toDto(agentRecipient);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restAgentRecipientMockMvc
                .perform(
                        patch(ENTITY_API_URL)
                                .contentType("application/merge-patch+json")
                                .content(TestUtil.convertObjectToJsonBytes(agentRecipientDTO))
                )
                .andExpect(status().isMethodNotAllowed());

        // Validate the AgentRecipient in the database
        List<AgentRecipient> agentRecipientList = agentRecipientRepository.findAll();
        assertThat(agentRecipientList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteAgentRecipient() throws Exception {
        // Initialize the database
        agentRecipientRepository.saveAndFlush(agentRecipient);

        int databaseSizeBeforeDelete = agentRecipientRepository.findAll().size();

        // Delete the agentRecipient
        restAgentRecipientMockMvc
                .perform(delete(ENTITY_API_URL_ID, agentRecipient.getId()).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        // Validate the database contains one less item
        List<AgentRecipient> agentRecipientList = agentRecipientRepository.findAll();
        assertThat(agentRecipientList).hasSize(databaseSizeBeforeDelete - 1);
    }
}
