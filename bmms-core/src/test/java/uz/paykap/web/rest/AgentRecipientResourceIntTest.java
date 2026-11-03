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
import uz.paykap.domain.*;
import uz.paykap.repository.AgentRecipientRepository;
import uz.paykap.repository.AgentRepository;
import uz.paykap.repository.RecipientRepository;
import uz.paykap.service.AgentRecipientQueryService;
import uz.paykap.service.AgentRecipientService;
import uz.paykap.service.dto.AgentRecipientDTO;
import uz.paykap.service.mapper.AgentRecipientMapper;
import uz.paykap.web.rest.errors.ExceptionTranslator;

/**
 * Test class for the AgentRecipientResource REST controller.
 *
 * @see AgentRecipientResource
 */
@RunWith(SpringRunner.class)
@IntegrationTest
public class AgentRecipientResourceIntTest {

    private static final Double DEFAULT_AGENT_COMMISSION = 0D;
    private static final Double UPDATED_AGENT_COMMISSION = 1D;

    private static final Double DEFAULT_AGENT_FEE = 0D;
    private static final Double UPDATED_AGENT_FEE = 1D;

    private static final Double DEFAULT_SYSTEM_COMMISSION_UP = 0D;
    private static final Double UPDATED_SYSTEM_COMMISSION_UP = 1D;

    private static final Double DEFAULT_SYSTEM_COMMISSION_DOWN = 0D;
    private static final Double UPDATED_SYSTEM_COMMISSION_DOWN = 1D;

    private static final Double DEFAULT_SYSTEM_FEE = 0D;
    private static final Double UPDATED_SYSTEM_FEE = 1D;

    private static final Boolean DEFAULT_ACTIVE = false;
    private static final Boolean UPDATED_ACTIVE = true;

    private static final Integer DEFAULT_EPOS_PORT = 0;
    private static final Integer UPDATED_EPOS_PORT = 1;

    private static final String DEFAULT_EPOS_MERCHANT = "AAAAAAAAAA";
    private static final String UPDATED_EPOS_MERCHANT = "BBBBBBBBBB";

    private static final String DEFAULT_EPOS_TERMINAL = "AAAAAAAAAA";
    private static final String UPDATED_EPOS_TERMINAL = "BBBBBBBBBB";

    private static final String DEFAULT_REQUEST_GROUP = "AAAAAAAAAA";
    private static final String UPDATED_REQUEST_GROUP = "BBBBBBBBBB";

    @Autowired
    private AgentRecipientRepository agentRecipientRepository;

    @Autowired
    private AgentRecipientMapper agentRecipientMapper;

    @Autowired
    private AgentRecipientService agentRecipientService;

    @Autowired
    private AgentRecipientQueryService agentRecipientQueryService;

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

    @Autowired
    private AgentRepository agentRepository;

    @Autowired
    private RecipientRepository recipientRepository;

    private MockMvc restAgentRecipientMockMvc;

    private AgentRecipient agentRecipient;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        final AgentRecipientResource agentRecipientResource = new AgentRecipientResource(
            agentRecipientService,
            agentRecipientQueryService,
            agentRepository,
            recipientRepository,
            agentRecipientRepository
        );
        this.restAgentRecipientMockMvc =
            MockMvcBuilders
                .standaloneSetup(agentRecipientResource)
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
    public static AgentRecipient createEntity(EntityManager em) {
        AgentRecipient agentRecipient = new AgentRecipient()
            .agentCommission(DEFAULT_AGENT_COMMISSION)
            .agentFee(DEFAULT_AGENT_FEE)
            .systemCommissionUp(DEFAULT_SYSTEM_COMMISSION_UP)
            .systemCommissionDown(DEFAULT_SYSTEM_COMMISSION_DOWN)
            .systemFee(DEFAULT_SYSTEM_FEE)
            .active(DEFAULT_ACTIVE)
            .eposPort(DEFAULT_EPOS_PORT)
            .eposMerchant(DEFAULT_EPOS_MERCHANT)
            .eposTerminal(DEFAULT_EPOS_TERMINAL);
        return agentRecipient;
    }

    @Before
    public void initTest() {
        agentRecipient = createEntity(em);
    }

    @Test
    @Transactional
    public void createAgentRecipient() throws Exception {
        int databaseSizeBeforeCreate = agentRecipientRepository.findAll().size();

        // Create the AgentRecipient
        AgentRecipientDTO agentRecipientDTO = agentRecipientMapper.toDto(agentRecipient);
        restAgentRecipientMockMvc
            .perform(
                post("/api/agent-recipients")
                    .contentType(TestUtil.APPLICATION_JSON_UTF8)
                    .content(TestUtil.convertObjectToJsonBytes(agentRecipientDTO))
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
        assertThat(testAgentRecipient.isActive()).isEqualTo(DEFAULT_ACTIVE);
        assertThat(testAgentRecipient.getEposPort()).isEqualTo(DEFAULT_EPOS_PORT);
        assertThat(testAgentRecipient.getEposMerchant()).isEqualTo(DEFAULT_EPOS_MERCHANT);
        assertThat(testAgentRecipient.getEposTerminal()).isEqualTo(DEFAULT_EPOS_TERMINAL);
    }

    @Test
    @Transactional
    public void createAgentRecipientWithExistingId() throws Exception {
        int databaseSizeBeforeCreate = agentRecipientRepository.findAll().size();

        // Create the AgentRecipient with an existing ID
        agentRecipient.setId(1L);
        AgentRecipientDTO agentRecipientDTO = agentRecipientMapper.toDto(agentRecipient);

        // An entity with an existing ID cannot be created, so this API call must fail
        restAgentRecipientMockMvc
            .perform(
                post("/api/agent-recipients")
                    .contentType(TestUtil.APPLICATION_JSON_UTF8)
                    .content(TestUtil.convertObjectToJsonBytes(agentRecipientDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the AgentRecipient in the database
        List<AgentRecipient> agentRecipientList = agentRecipientRepository.findAll();
        assertThat(agentRecipientList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    public void checkActiveIsRequired() throws Exception {
        int databaseSizeBeforeTest = agentRecipientRepository.findAll().size();
        // set the field null
        agentRecipient.setActive(null);

        // Create the AgentRecipient, which fails.
        AgentRecipientDTO agentRecipientDTO = agentRecipientMapper.toDto(agentRecipient);

        restAgentRecipientMockMvc
            .perform(
                post("/api/agent-recipients")
                    .contentType(TestUtil.APPLICATION_JSON_UTF8)
                    .content(TestUtil.convertObjectToJsonBytes(agentRecipientDTO))
            )
            .andExpect(status().isBadRequest());

        List<AgentRecipient> agentRecipientList = agentRecipientRepository.findAll();
        assertThat(agentRecipientList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void getAllAgentRecipients() throws Exception {
        // Initialize the database
        agentRecipientRepository.saveAndFlush(agentRecipient);

        // Get all the agentRecipientList
        restAgentRecipientMockMvc
            .perform(get("/api/agent-recipients?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(agentRecipient.getId().intValue())))
            .andExpect(jsonPath("$.[*].agentCommission").value(hasItem(DEFAULT_AGENT_COMMISSION.doubleValue())))
            .andExpect(jsonPath("$.[*].agentFee").value(hasItem(DEFAULT_AGENT_FEE.doubleValue())))
            .andExpect(jsonPath("$.[*].systemCommissionUp").value(hasItem(DEFAULT_SYSTEM_COMMISSION_UP.doubleValue())))
            .andExpect(jsonPath("$.[*].systemCommissionDown").value(hasItem(DEFAULT_SYSTEM_COMMISSION_DOWN.doubleValue())))
            .andExpect(jsonPath("$.[*].systemFee").value(hasItem(DEFAULT_SYSTEM_FEE.doubleValue())))
            .andExpect(jsonPath("$.[*].active").value(hasItem(DEFAULT_ACTIVE.booleanValue())))
            .andExpect(jsonPath("$.[*].eposPort").value(hasItem(DEFAULT_EPOS_PORT)))
            .andExpect(jsonPath("$.[*].eposMerchant").value(hasItem(DEFAULT_EPOS_MERCHANT.toString())))
            .andExpect(jsonPath("$.[*].eposTerminal").value(hasItem(DEFAULT_EPOS_TERMINAL.toString())))
            .andExpect(jsonPath("$.[*].requestGroup").value(hasItem(DEFAULT_REQUEST_GROUP.toString())));
    }

    @Test
    @Transactional
    public void getAgentRecipient() throws Exception {
        // Initialize the database
        agentRecipientRepository.saveAndFlush(agentRecipient);

        // Get the agentRecipient
        restAgentRecipientMockMvc
            .perform(get("/api/agent-recipients/{id}", agentRecipient.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.id").value(agentRecipient.getId().intValue()))
            .andExpect(jsonPath("$.agentCommission").value(DEFAULT_AGENT_COMMISSION.doubleValue()))
            .andExpect(jsonPath("$.agentFee").value(DEFAULT_AGENT_FEE.doubleValue()))
            .andExpect(jsonPath("$.systemCommissionUp").value(DEFAULT_SYSTEM_COMMISSION_UP.doubleValue()))
            .andExpect(jsonPath("$.systemCommissionDown").value(DEFAULT_SYSTEM_COMMISSION_DOWN.doubleValue()))
            .andExpect(jsonPath("$.systemFee").value(DEFAULT_SYSTEM_FEE.doubleValue()))
            .andExpect(jsonPath("$.active").value(DEFAULT_ACTIVE.booleanValue()))
            .andExpect(jsonPath("$.eposPort").value(DEFAULT_EPOS_PORT))
            .andExpect(jsonPath("$.eposMerchant").value(DEFAULT_EPOS_MERCHANT.toString()))
            .andExpect(jsonPath("$.eposTerminal").value(DEFAULT_EPOS_TERMINAL.toString()))
            .andExpect(jsonPath("$.requestGroup").value(DEFAULT_REQUEST_GROUP.toString()));
    }

    @Test
    @Transactional
    public void getAllAgentRecipientsByAgentCommissionIsEqualToSomething() throws Exception {
        // Initialize the database
        agentRecipientRepository.saveAndFlush(agentRecipient);

        // Get all the agentRecipientList where agentCommission equals to DEFAULT_AGENT_COMMISSION
        defaultAgentRecipientShouldBeFound("agentCommission.equals=" + DEFAULT_AGENT_COMMISSION);

        // Get all the agentRecipientList where agentCommission equals to UPDATED_AGENT_COMMISSION
        defaultAgentRecipientShouldNotBeFound("agentCommission.equals=" + UPDATED_AGENT_COMMISSION);
    }

    @Test
    @Transactional
    public void getAllAgentRecipientsByAgentCommissionIsInShouldWork() throws Exception {
        // Initialize the database
        agentRecipientRepository.saveAndFlush(agentRecipient);

        // Get all the agentRecipientList where agentCommission in DEFAULT_AGENT_COMMISSION or UPDATED_AGENT_COMMISSION
        defaultAgentRecipientShouldBeFound("agentCommission.in=" + DEFAULT_AGENT_COMMISSION + "," + UPDATED_AGENT_COMMISSION);

        // Get all the agentRecipientList where agentCommission equals to UPDATED_AGENT_COMMISSION
        defaultAgentRecipientShouldNotBeFound("agentCommission.in=" + UPDATED_AGENT_COMMISSION);
    }

    @Test
    @Transactional
    public void getAllAgentRecipientsByAgentCommissionIsNullOrNotNull() throws Exception {
        // Initialize the database
        agentRecipientRepository.saveAndFlush(agentRecipient);

        // Get all the agentRecipientList where agentCommission is not null
        defaultAgentRecipientShouldBeFound("agentCommission.specified=true");

        // Get all the agentRecipientList where agentCommission is null
        defaultAgentRecipientShouldNotBeFound("agentCommission.specified=false");
    }

    @Test
    @Transactional
    public void getAllAgentRecipientsByAgentFeeIsEqualToSomething() throws Exception {
        // Initialize the database
        agentRecipientRepository.saveAndFlush(agentRecipient);

        // Get all the agentRecipientList where agentFee equals to DEFAULT_AGENT_FEE
        defaultAgentRecipientShouldBeFound("agentFee.equals=" + DEFAULT_AGENT_FEE);

        // Get all the agentRecipientList where agentFee equals to UPDATED_AGENT_FEE
        defaultAgentRecipientShouldNotBeFound("agentFee.equals=" + UPDATED_AGENT_FEE);
    }

    @Test
    @Transactional
    public void getAllAgentRecipientsByAgentFeeIsInShouldWork() throws Exception {
        // Initialize the database
        agentRecipientRepository.saveAndFlush(agentRecipient);

        // Get all the agentRecipientList where agentFee in DEFAULT_AGENT_FEE or UPDATED_AGENT_FEE
        defaultAgentRecipientShouldBeFound("agentFee.in=" + DEFAULT_AGENT_FEE + "," + UPDATED_AGENT_FEE);

        // Get all the agentRecipientList where agentFee equals to UPDATED_AGENT_FEE
        defaultAgentRecipientShouldNotBeFound("agentFee.in=" + UPDATED_AGENT_FEE);
    }

    @Test
    @Transactional
    public void getAllAgentRecipientsByAgentFeeIsNullOrNotNull() throws Exception {
        // Initialize the database
        agentRecipientRepository.saveAndFlush(agentRecipient);

        // Get all the agentRecipientList where agentFee is not null
        defaultAgentRecipientShouldBeFound("agentFee.specified=true");

        // Get all the agentRecipientList where agentFee is null
        defaultAgentRecipientShouldNotBeFound("agentFee.specified=false");
    }

    @Test
    @Transactional
    public void getAllAgentRecipientsBySystemCommissionUpIsEqualToSomething() throws Exception {
        // Initialize the database
        agentRecipientRepository.saveAndFlush(agentRecipient);

        // Get all the agentRecipientList where systemCommissionUp equals to DEFAULT_SYSTEM_COMMISSION_UP
        defaultAgentRecipientShouldBeFound("systemCommissionUp.equals=" + DEFAULT_SYSTEM_COMMISSION_UP);

        // Get all the agentRecipientList where systemCommissionUp equals to UPDATED_SYSTEM_COMMISSION_UP
        defaultAgentRecipientShouldNotBeFound("systemCommissionUp.equals=" + UPDATED_SYSTEM_COMMISSION_UP);
    }

    @Test
    @Transactional
    public void getAllAgentRecipientsBySystemCommissionUpIsInShouldWork() throws Exception {
        // Initialize the database
        agentRecipientRepository.saveAndFlush(agentRecipient);

        // Get all the agentRecipientList where systemCommissionUp in DEFAULT_SYSTEM_COMMISSION_UP or UPDATED_SYSTEM_COMMISSION_UP
        defaultAgentRecipientShouldBeFound("systemCommissionUp.in=" + DEFAULT_SYSTEM_COMMISSION_UP + "," + UPDATED_SYSTEM_COMMISSION_UP);

        // Get all the agentRecipientList where systemCommissionUp equals to UPDATED_SYSTEM_COMMISSION_UP
        defaultAgentRecipientShouldNotBeFound("systemCommissionUp.in=" + UPDATED_SYSTEM_COMMISSION_UP);
    }

    @Test
    @Transactional
    public void getAllAgentRecipientsBySystemCommissionUpIsNullOrNotNull() throws Exception {
        // Initialize the database
        agentRecipientRepository.saveAndFlush(agentRecipient);

        // Get all the agentRecipientList where systemCommissionUp is not null
        defaultAgentRecipientShouldBeFound("systemCommissionUp.specified=true");

        // Get all the agentRecipientList where systemCommissionUp is null
        defaultAgentRecipientShouldNotBeFound("systemCommissionUp.specified=false");
    }

    @Test
    @Transactional
    public void getAllAgentRecipientsBySystemCommissionDownIsEqualToSomething() throws Exception {
        // Initialize the database
        agentRecipientRepository.saveAndFlush(agentRecipient);

        // Get all the agentRecipientList where systemCommissionDown equals to DEFAULT_SYSTEM_COMMISSION_DOWN
        defaultAgentRecipientShouldBeFound("systemCommissionDown.equals=" + DEFAULT_SYSTEM_COMMISSION_DOWN);

        // Get all the agentRecipientList where systemCommissionDown equals to UPDATED_SYSTEM_COMMISSION_DOWN
        defaultAgentRecipientShouldNotBeFound("systemCommissionDown.equals=" + UPDATED_SYSTEM_COMMISSION_DOWN);
    }

    @Test
    @Transactional
    public void getAllAgentRecipientsBySystemCommissionDownIsInShouldWork() throws Exception {
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
    public void getAllAgentRecipientsBySystemCommissionDownIsNullOrNotNull() throws Exception {
        // Initialize the database
        agentRecipientRepository.saveAndFlush(agentRecipient);

        // Get all the agentRecipientList where systemCommissionDown is not null
        defaultAgentRecipientShouldBeFound("systemCommissionDown.specified=true");

        // Get all the agentRecipientList where systemCommissionDown is null
        defaultAgentRecipientShouldNotBeFound("systemCommissionDown.specified=false");
    }

    @Test
    @Transactional
    public void getAllAgentRecipientsBySystemFeeIsEqualToSomething() throws Exception {
        // Initialize the database
        agentRecipientRepository.saveAndFlush(agentRecipient);

        // Get all the agentRecipientList where systemFee equals to DEFAULT_SYSTEM_FEE
        defaultAgentRecipientShouldBeFound("systemFee.equals=" + DEFAULT_SYSTEM_FEE);

        // Get all the agentRecipientList where systemFee equals to UPDATED_SYSTEM_FEE
        defaultAgentRecipientShouldNotBeFound("systemFee.equals=" + UPDATED_SYSTEM_FEE);
    }

    @Test
    @Transactional
    public void getAllAgentRecipientsBySystemFeeIsInShouldWork() throws Exception {
        // Initialize the database
        agentRecipientRepository.saveAndFlush(agentRecipient);

        // Get all the agentRecipientList where systemFee in DEFAULT_SYSTEM_FEE or UPDATED_SYSTEM_FEE
        defaultAgentRecipientShouldBeFound("systemFee.in=" + DEFAULT_SYSTEM_FEE + "," + UPDATED_SYSTEM_FEE);

        // Get all the agentRecipientList where systemFee equals to UPDATED_SYSTEM_FEE
        defaultAgentRecipientShouldNotBeFound("systemFee.in=" + UPDATED_SYSTEM_FEE);
    }

    @Test
    @Transactional
    public void getAllAgentRecipientsBySystemFeeIsNullOrNotNull() throws Exception {
        // Initialize the database
        agentRecipientRepository.saveAndFlush(agentRecipient);

        // Get all the agentRecipientList where systemFee is not null
        defaultAgentRecipientShouldBeFound("systemFee.specified=true");

        // Get all the agentRecipientList where systemFee is null
        defaultAgentRecipientShouldNotBeFound("systemFee.specified=false");
    }

    @Test
    @Transactional
    public void getAllAgentRecipientsByActiveIsEqualToSomething() throws Exception {
        // Initialize the database
        agentRecipientRepository.saveAndFlush(agentRecipient);

        // Get all the agentRecipientList where active equals to DEFAULT_ACTIVE
        defaultAgentRecipientShouldBeFound("active.equals=" + DEFAULT_ACTIVE);

        // Get all the agentRecipientList where active equals to UPDATED_ACTIVE
        defaultAgentRecipientShouldNotBeFound("active.equals=" + UPDATED_ACTIVE);
    }

    @Test
    @Transactional
    public void getAllAgentRecipientsByActiveIsInShouldWork() throws Exception {
        // Initialize the database
        agentRecipientRepository.saveAndFlush(agentRecipient);

        // Get all the agentRecipientList where active in DEFAULT_ACTIVE or UPDATED_ACTIVE
        defaultAgentRecipientShouldBeFound("active.in=" + DEFAULT_ACTIVE + "," + UPDATED_ACTIVE);

        // Get all the agentRecipientList where active equals to UPDATED_ACTIVE
        defaultAgentRecipientShouldNotBeFound("active.in=" + UPDATED_ACTIVE);
    }

    @Test
    @Transactional
    public void getAllAgentRecipientsByActiveIsNullOrNotNull() throws Exception {
        // Initialize the database
        agentRecipientRepository.saveAndFlush(agentRecipient);

        // Get all the agentRecipientList where active is not null
        defaultAgentRecipientShouldBeFound("active.specified=true");

        // Get all the agentRecipientList where active is null
        defaultAgentRecipientShouldNotBeFound("active.specified=false");
    }

    @Test
    @Transactional
    public void getAllAgentRecipientsByEposPortIsEqualToSomething() throws Exception {
        // Initialize the database
        agentRecipientRepository.saveAndFlush(agentRecipient);

        // Get all the agentRecipientList where eposPort equals to DEFAULT_EPOS_PORT
        defaultAgentRecipientShouldBeFound("eposPort.equals=" + DEFAULT_EPOS_PORT);

        // Get all the agentRecipientList where eposPort equals to UPDATED_EPOS_PORT
        defaultAgentRecipientShouldNotBeFound("eposPort.equals=" + UPDATED_EPOS_PORT);
    }

    @Test
    @Transactional
    public void getAllAgentRecipientsByEposPortIsInShouldWork() throws Exception {
        // Initialize the database
        agentRecipientRepository.saveAndFlush(agentRecipient);

        // Get all the agentRecipientList where eposPort in DEFAULT_EPOS_PORT or UPDATED_EPOS_PORT
        defaultAgentRecipientShouldBeFound("eposPort.in=" + DEFAULT_EPOS_PORT + "," + UPDATED_EPOS_PORT);

        // Get all the agentRecipientList where eposPort equals to UPDATED_EPOS_PORT
        defaultAgentRecipientShouldNotBeFound("eposPort.in=" + UPDATED_EPOS_PORT);
    }

    @Test
    @Transactional
    public void getAllAgentRecipientsByEposPortIsNullOrNotNull() throws Exception {
        // Initialize the database
        agentRecipientRepository.saveAndFlush(agentRecipient);

        // Get all the agentRecipientList where eposPort is not null
        defaultAgentRecipientShouldBeFound("eposPort.specified=true");

        // Get all the agentRecipientList where eposPort is null
        defaultAgentRecipientShouldNotBeFound("eposPort.specified=false");
    }

    @Test
    @Transactional
    public void getAllAgentRecipientsByEposPortIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        agentRecipientRepository.saveAndFlush(agentRecipient);

        // Get all the agentRecipientList where eposPort greater than or equals to DEFAULT_EPOS_PORT
        defaultAgentRecipientShouldBeFound("eposPort.greaterOrEqualThan=" + DEFAULT_EPOS_PORT);

        // Get all the agentRecipientList where eposPort greater than or equals to (DEFAULT_EPOS_PORT + 1)
        defaultAgentRecipientShouldNotBeFound("eposPort.greaterOrEqualThan=" + (DEFAULT_EPOS_PORT + 1));
    }

    @Test
    @Transactional
    public void getAllAgentRecipientsByEposPortIsLessThanSomething() throws Exception {
        // Initialize the database
        agentRecipientRepository.saveAndFlush(agentRecipient);

        // Get all the agentRecipientList where eposPort less than or equals to DEFAULT_EPOS_PORT
        defaultAgentRecipientShouldNotBeFound("eposPort.lessThan=" + DEFAULT_EPOS_PORT);

        // Get all the agentRecipientList where eposPort less than or equals to (DEFAULT_EPOS_PORT + 1)
        defaultAgentRecipientShouldBeFound("eposPort.lessThan=" + (DEFAULT_EPOS_PORT + 1));
    }

    @Test
    @Transactional
    public void getAllAgentRecipientsByEposMerchantIsEqualToSomething() throws Exception {
        // Initialize the database
        agentRecipientRepository.saveAndFlush(agentRecipient);

        // Get all the agentRecipientList where eposMerchant equals to DEFAULT_EPOS_MERCHANT
        defaultAgentRecipientShouldBeFound("eposMerchant.equals=" + DEFAULT_EPOS_MERCHANT);

        // Get all the agentRecipientList where eposMerchant equals to UPDATED_EPOS_MERCHANT
        defaultAgentRecipientShouldNotBeFound("eposMerchant.equals=" + UPDATED_EPOS_MERCHANT);
    }

    @Test
    @Transactional
    public void getAllAgentRecipientsByEposMerchantIsInShouldWork() throws Exception {
        // Initialize the database
        agentRecipientRepository.saveAndFlush(agentRecipient);

        // Get all the agentRecipientList where eposMerchant in DEFAULT_EPOS_MERCHANT or UPDATED_EPOS_MERCHANT
        defaultAgentRecipientShouldBeFound("eposMerchant.in=" + DEFAULT_EPOS_MERCHANT + "," + UPDATED_EPOS_MERCHANT);

        // Get all the agentRecipientList where eposMerchant equals to UPDATED_EPOS_MERCHANT
        defaultAgentRecipientShouldNotBeFound("eposMerchant.in=" + UPDATED_EPOS_MERCHANT);
    }

    @Test
    @Transactional
    public void getAllAgentRecipientsByEposMerchantIsNullOrNotNull() throws Exception {
        // Initialize the database
        agentRecipientRepository.saveAndFlush(agentRecipient);

        // Get all the agentRecipientList where eposMerchant is not null
        defaultAgentRecipientShouldBeFound("eposMerchant.specified=true");

        // Get all the agentRecipientList where eposMerchant is null
        defaultAgentRecipientShouldNotBeFound("eposMerchant.specified=false");
    }

    @Test
    @Transactional
    public void getAllAgentRecipientsByEposTerminalIsEqualToSomething() throws Exception {
        // Initialize the database
        agentRecipientRepository.saveAndFlush(agentRecipient);

        // Get all the agentRecipientList where eposTerminal equals to DEFAULT_EPOS_TERMINAL
        defaultAgentRecipientShouldBeFound("eposTerminal.equals=" + DEFAULT_EPOS_TERMINAL);

        // Get all the agentRecipientList where eposTerminal equals to UPDATED_EPOS_TERMINAL
        defaultAgentRecipientShouldNotBeFound("eposTerminal.equals=" + UPDATED_EPOS_TERMINAL);
    }

    @Test
    @Transactional
    public void getAllAgentRecipientsByEposTerminalIsInShouldWork() throws Exception {
        // Initialize the database
        agentRecipientRepository.saveAndFlush(agentRecipient);

        // Get all the agentRecipientList where eposTerminal in DEFAULT_EPOS_TERMINAL or UPDATED_EPOS_TERMINAL
        defaultAgentRecipientShouldBeFound("eposTerminal.in=" + DEFAULT_EPOS_TERMINAL + "," + UPDATED_EPOS_TERMINAL);

        // Get all the agentRecipientList where eposTerminal equals to UPDATED_EPOS_TERMINAL
        defaultAgentRecipientShouldNotBeFound("eposTerminal.in=" + UPDATED_EPOS_TERMINAL);
    }

    @Test
    @Transactional
    public void getAllAgentRecipientsByEposTerminalIsNullOrNotNull() throws Exception {
        // Initialize the database
        agentRecipientRepository.saveAndFlush(agentRecipient);

        // Get all the agentRecipientList where eposTerminal is not null
        defaultAgentRecipientShouldBeFound("eposTerminal.specified=true");

        // Get all the agentRecipientList where eposTerminal is null
        defaultAgentRecipientShouldNotBeFound("eposTerminal.specified=false");
    }

    @Test
    @Transactional
    public void getAllAgentRecipientsByRequestGroupIsEqualToSomething() throws Exception {
        // Initialize the database
        agentRecipientRepository.saveAndFlush(agentRecipient);

        // Get all the agentRecipientList where requestGroup equals to DEFAULT_REQUEST_GROUP
        defaultAgentRecipientShouldBeFound("requestGroup.equals=" + DEFAULT_REQUEST_GROUP);

        // Get all the agentRecipientList where requestGroup equals to UPDATED_REQUEST_GROUP
        defaultAgentRecipientShouldNotBeFound("requestGroup.equals=" + UPDATED_REQUEST_GROUP);
    }

    @Test
    @Transactional
    public void getAllAgentRecipientsByRequestGroupIsInShouldWork() throws Exception {
        // Initialize the database
        agentRecipientRepository.saveAndFlush(agentRecipient);

        // Get all the agentRecipientList where requestGroup in DEFAULT_REQUEST_GROUP or UPDATED_REQUEST_GROUP
        defaultAgentRecipientShouldBeFound("requestGroup.in=" + DEFAULT_REQUEST_GROUP + "," + UPDATED_REQUEST_GROUP);

        // Get all the agentRecipientList where requestGroup equals to UPDATED_REQUEST_GROUP
        defaultAgentRecipientShouldNotBeFound("requestGroup.in=" + UPDATED_REQUEST_GROUP);
    }

    @Test
    @Transactional
    public void getAllAgentRecipientsByRequestGroupIsNullOrNotNull() throws Exception {
        // Initialize the database
        agentRecipientRepository.saveAndFlush(agentRecipient);

        // Get all the agentRecipientList where requestGroup is not null
        defaultAgentRecipientShouldBeFound("requestGroup.specified=true");

        // Get all the agentRecipientList where requestGroup is null
        defaultAgentRecipientShouldNotBeFound("requestGroup.specified=false");
    }

    @Test
    @Transactional
    public void getAllAgentRecipientsByRecipientIsEqualToSomething() throws Exception {
        // Initialize the database
        Recipient recipient = RecipientResourceIntTest.createEntity(em);
        em.persist(recipient);
        em.flush();
        agentRecipient.setRecipient(recipient);
        agentRecipientRepository.saveAndFlush(agentRecipient);
        Long recipientId = recipient.getId();

        // Get all the agentRecipientList where recipient equals to recipientId
        defaultAgentRecipientShouldBeFound("recipientId.equals=" + recipientId);

        // Get all the agentRecipientList where recipient equals to recipientId + 1
        defaultAgentRecipientShouldNotBeFound("recipientId.equals=" + (recipientId + 1));
    }

    @Test
    @Transactional
    public void getAllAgentRecipientsBySettingIsEqualToSomething() throws Exception {
        // Initialize the database
        Setting setting = SettingResourceIntTest.createEntity(em);
        em.persist(setting);
        em.flush();
        agentRecipient.setSetting(setting);
        agentRecipientRepository.saveAndFlush(agentRecipient);
        Long settingId = setting.getId();

        // Get all the agentRecipientList where setting equals to settingId
        defaultAgentRecipientShouldBeFound("settingId.equals=" + settingId);

        // Get all the agentRecipientList where setting equals to settingId + 1
        defaultAgentRecipientShouldNotBeFound("settingId.equals=" + (settingId + 1));
    }

    @Test
    @Transactional
    public void getAllAgentRecipientsByAgentIsEqualToSomething() throws Exception {
        // Initialize the database
        Agent agent = AgentResourceIntTest.createEntity(em);
        em.persist(agent);
        em.flush();
        agentRecipient.setAgent(agent);
        agentRecipientRepository.saveAndFlush(agentRecipient);
        Long agentId = agent.getId();

        // Get all the agentRecipientList where agent equals to agentId
        defaultAgentRecipientShouldBeFound("agentId.equals=" + agentId);

        // Get all the agentRecipientList where agent equals to agentId + 1
        defaultAgentRecipientShouldNotBeFound("agentId.equals=" + (agentId + 1));
    }

    @Test
    @Transactional
    public void getAllAgentRecipientsByRecipientGroupIsEqualToSomething() throws Exception {
        // Initialize the database
        RecipientGroup recipientGroup = RecipientGroupResourceIntTest.createEntity(em);
        em.persist(recipientGroup);
        em.flush();
        agentRecipient.setRecipientGroup(recipientGroup);
        agentRecipientRepository.saveAndFlush(agentRecipient);
        Long recipientGroupId = recipientGroup.getId();

        // Get all the agentRecipientList where recipientGroup equals to recipientGroupId
        defaultAgentRecipientShouldBeFound("recipientGroupId.equals=" + recipientGroupId);

        // Get all the agentRecipientList where recipientGroup equals to recipientGroupId + 1
        defaultAgentRecipientShouldNotBeFound("recipientGroupId.equals=" + (recipientGroupId + 1));
    }

    /**
     * Executes the search, and checks that the default entity is returned
     */
    private void defaultAgentRecipientShouldBeFound(String filter) throws Exception {
        restAgentRecipientMockMvc
            .perform(get("/api/agent-recipients?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(agentRecipient.getId().intValue())))
            .andExpect(jsonPath("$.[*].agentCommission").value(hasItem(DEFAULT_AGENT_COMMISSION.doubleValue())))
            .andExpect(jsonPath("$.[*].agentFee").value(hasItem(DEFAULT_AGENT_FEE.doubleValue())))
            .andExpect(jsonPath("$.[*].systemCommissionUp").value(hasItem(DEFAULT_SYSTEM_COMMISSION_UP.doubleValue())))
            .andExpect(jsonPath("$.[*].systemCommissionDown").value(hasItem(DEFAULT_SYSTEM_COMMISSION_DOWN.doubleValue())))
            .andExpect(jsonPath("$.[*].systemFee").value(hasItem(DEFAULT_SYSTEM_FEE.doubleValue())))
            .andExpect(jsonPath("$.[*].active").value(hasItem(DEFAULT_ACTIVE.booleanValue())))
            .andExpect(jsonPath("$.[*].eposPort").value(hasItem(DEFAULT_EPOS_PORT)))
            .andExpect(jsonPath("$.[*].eposMerchant").value(hasItem(DEFAULT_EPOS_MERCHANT.toString())))
            .andExpect(jsonPath("$.[*].eposTerminal").value(hasItem(DEFAULT_EPOS_TERMINAL.toString())))
            .andExpect(jsonPath("$.[*].requestGroup").value(hasItem(DEFAULT_REQUEST_GROUP.toString())));

        // Check, that the count call also returns 1
        restAgentRecipientMockMvc
            .perform(get("/api/agent-recipients/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(content().string("1"));
    }

    /**
     * Executes the search, and checks that the default entity is not returned
     */
    private void defaultAgentRecipientShouldNotBeFound(String filter) throws Exception {
        restAgentRecipientMockMvc
            .perform(get("/api/agent-recipients?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty());

        // Check, that the count call also returns 0
        restAgentRecipientMockMvc
            .perform(get("/api/agent-recipients/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(content().string("0"));
    }

    @Test
    @Transactional
    public void getNonExistingAgentRecipient() throws Exception {
        // Get the agentRecipient
        restAgentRecipientMockMvc.perform(get("/api/agent-recipients/{id}", Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updateAgentRecipient() throws Exception {
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
            .eposTerminal(UPDATED_EPOS_TERMINAL);
        AgentRecipientDTO agentRecipientDTO = agentRecipientMapper.toDto(updatedAgentRecipient);

        restAgentRecipientMockMvc
            .perform(
                put("/api/agent-recipients")
                    .contentType(TestUtil.APPLICATION_JSON_UTF8)
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
        assertThat(testAgentRecipient.isActive()).isEqualTo(UPDATED_ACTIVE);
        assertThat(testAgentRecipient.getEposPort()).isEqualTo(UPDATED_EPOS_PORT);
        assertThat(testAgentRecipient.getEposMerchant()).isEqualTo(UPDATED_EPOS_MERCHANT);
        assertThat(testAgentRecipient.getEposTerminal()).isEqualTo(UPDATED_EPOS_TERMINAL);
    }

    @Test
    @Transactional
    public void updateNonExistingAgentRecipient() throws Exception {
        int databaseSizeBeforeUpdate = agentRecipientRepository.findAll().size();

        // Create the AgentRecipient
        AgentRecipientDTO agentRecipientDTO = agentRecipientMapper.toDto(agentRecipient);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restAgentRecipientMockMvc
            .perform(
                put("/api/agent-recipients")
                    .contentType(TestUtil.APPLICATION_JSON_UTF8)
                    .content(TestUtil.convertObjectToJsonBytes(agentRecipientDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the AgentRecipient in the database
        List<AgentRecipient> agentRecipientList = agentRecipientRepository.findAll();
        assertThat(agentRecipientList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    public void deleteAgentRecipient() throws Exception {
        // Initialize the database
        agentRecipientRepository.saveAndFlush(agentRecipient);

        int databaseSizeBeforeDelete = agentRecipientRepository.findAll().size();

        // Get the agentRecipient
        restAgentRecipientMockMvc
            .perform(delete("/api/agent-recipients/{id}", agentRecipient.getId()).accept(TestUtil.APPLICATION_JSON_UTF8))
            .andExpect(status().isOk());

        // Validate the database is empty
        List<AgentRecipient> agentRecipientList = agentRecipientRepository.findAll();
        assertThat(agentRecipientList).hasSize(databaseSizeBeforeDelete - 1);
    }

    @Test
    @Transactional
    public void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(AgentRecipient.class);
        AgentRecipient agentRecipient1 = new AgentRecipient();
        agentRecipient1.setId(1L);
        AgentRecipient agentRecipient2 = new AgentRecipient();
        agentRecipient2.setId(agentRecipient1.getId());
        assertThat(agentRecipient1).isEqualTo(agentRecipient2);
        agentRecipient2.setId(2L);
        assertThat(agentRecipient1).isNotEqualTo(agentRecipient2);
        agentRecipient1.setId(null);
        assertThat(agentRecipient1).isNotEqualTo(agentRecipient2);
    }

    @Test
    @Transactional
    public void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(AgentRecipientDTO.class);
        AgentRecipientDTO agentRecipientDTO1 = new AgentRecipientDTO();
        agentRecipientDTO1.setId(1L);
        AgentRecipientDTO agentRecipientDTO2 = new AgentRecipientDTO();
        assertThat(agentRecipientDTO1).isNotEqualTo(agentRecipientDTO2);
        agentRecipientDTO2.setId(agentRecipientDTO1.getId());
        assertThat(agentRecipientDTO1).isEqualTo(agentRecipientDTO2);
        agentRecipientDTO2.setId(2L);
        assertThat(agentRecipientDTO1).isNotEqualTo(agentRecipientDTO2);
        agentRecipientDTO1.setId(null);
        assertThat(agentRecipientDTO1).isNotEqualTo(agentRecipientDTO2);
    }
    //    @Test
    //    @Transactional
    //    public void testEntityFromId() {
    //        assertThat(agentRecipientMapper.fromId(42L).getId()).isEqualTo(42);
    //        assertThat(agentRecipientMapper.fromId(null)).isNull();
    //    }
}
