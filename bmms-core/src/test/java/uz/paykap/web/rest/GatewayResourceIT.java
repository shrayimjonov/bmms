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
import uz.paykap.IntegrationTest;
import uz.paykap.domain.Deposit;
import uz.paykap.domain.Gateway;
import uz.paykap.repository.GatewayRepository;
import uz.paykap.service.criteria.GatewayCriteria;
import uz.paykap.service.dto.GatewayDTO;
import uz.paykap.service.mapper.GatewayMapper;

/**
 * Integration tests for the {@link GatewayResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class GatewayResourceIT {

    private static final String DEFAULT_NAME = "AAAAAAAAAA";
    private static final String UPDATED_NAME = "BBBBBBBBBB";

    private static final String DEFAULT_ORG = "AAAAAAAAAA";
    private static final String UPDATED_ORG = "BBBBBBBBBB";

    private static final Boolean DEFAULT_ACTIVE = false;
    private static final Boolean UPDATED_ACTIVE = true;

    private static final String DEFAULT_MFO = "AAAAAAAA";
    private static final String UPDATED_MFO = "BBBBBBBB";

    private static final Long DEFAULT_TAXNR = 1L;
    private static final Long UPDATED_TAXNR = 2L;
    private static final Long SMALLER_TAXNR = 1L - 1L;

    private static final String DEFAULT_BAN = "AAAAAAAAAA";
    private static final String UPDATED_BAN = "BBBBBBBBBB";

    private static final String DEFAULT_CONTRACT = "AAAAAAAAAA";
    private static final String UPDATED_CONTRACT = "BBBBBBBBBB";

    private static final String DEFAULT_SPRING_BEAN_NAME = "AAAAAAAAAA";
    private static final String UPDATED_SPRING_BEAN_NAME = "BBBBBBBBBB";

    private static final String ENTITY_API_URL = "/api/gateways";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong count = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private GatewayRepository gatewayRepository;

    @Autowired
    private GatewayMapper gatewayMapper;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restGatewayMockMvc;

    private Gateway gateway;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Gateway createEntity(EntityManager em) {
        Gateway gateway = new Gateway()
            .name(DEFAULT_NAME)
            .org(DEFAULT_ORG)
            .active(DEFAULT_ACTIVE)
            .mfo(DEFAULT_MFO)
            .taxnr(DEFAULT_TAXNR)
            .ban(DEFAULT_BAN)
            .contract(DEFAULT_CONTRACT)
            .springBeanName(DEFAULT_SPRING_BEAN_NAME);
        return gateway;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Gateway createUpdatedEntity(EntityManager em) {
        Gateway gateway = new Gateway()
            .name(UPDATED_NAME)
            .org(UPDATED_ORG)
            .active(UPDATED_ACTIVE)
            .mfo(UPDATED_MFO)
            .taxnr(UPDATED_TAXNR)
            .ban(UPDATED_BAN)
            .contract(UPDATED_CONTRACT)
            .springBeanName(UPDATED_SPRING_BEAN_NAME);
        return gateway;
    }

    @BeforeEach
    public void initTest() {
        gateway = createEntity(em);
    }

    @Test
    @Transactional
    void createGateway() throws Exception {
        int databaseSizeBeforeCreate = gatewayRepository.findAll().size();
        // Create the Gateway
        GatewayDTO gatewayDTO = gatewayMapper.toDto(gateway);
        restGatewayMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(gatewayDTO)))
            .andExpect(status().isCreated());

        // Validate the Gateway in the database
        List<Gateway> gatewayList = gatewayRepository.findAll();
        assertThat(gatewayList).hasSize(databaseSizeBeforeCreate + 1);
        Gateway testGateway = gatewayList.get(gatewayList.size() - 1);
        assertThat(testGateway.getName()).isEqualTo(DEFAULT_NAME);
        assertThat(testGateway.getOrg()).isEqualTo(DEFAULT_ORG);
        assertThat(testGateway.getActive()).isEqualTo(DEFAULT_ACTIVE);
        assertThat(testGateway.getMfo()).isEqualTo(DEFAULT_MFO);
        assertThat(testGateway.getTaxnr()).isEqualTo(DEFAULT_TAXNR);
        assertThat(testGateway.getBan()).isEqualTo(DEFAULT_BAN);
        assertThat(testGateway.getContract()).isEqualTo(DEFAULT_CONTRACT);
        assertThat(testGateway.getSpringBeanName()).isEqualTo(DEFAULT_SPRING_BEAN_NAME);
    }

    @Test
    @Transactional
    void createGatewayWithExistingId() throws Exception {
        // Create the Gateway with an existing ID
        gateway.setId(1L);
        GatewayDTO gatewayDTO = gatewayMapper.toDto(gateway);

        int databaseSizeBeforeCreate = gatewayRepository.findAll().size();

        // An entity with an existing ID cannot be created, so this API call must fail
        restGatewayMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(gatewayDTO)))
            .andExpect(status().isBadRequest());

        // Validate the Gateway in the database
        List<Gateway> gatewayList = gatewayRepository.findAll();
        assertThat(gatewayList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkNameIsRequired() throws Exception {
        int databaseSizeBeforeTest = gatewayRepository.findAll().size();
        // set the field null
        gateway.setName(null);

        // Create the Gateway, which fails.
        GatewayDTO gatewayDTO = gatewayMapper.toDto(gateway);

        restGatewayMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(gatewayDTO)))
            .andExpect(status().isBadRequest());

        List<Gateway> gatewayList = gatewayRepository.findAll();
        assertThat(gatewayList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkOrgIsRequired() throws Exception {
        int databaseSizeBeforeTest = gatewayRepository.findAll().size();
        // set the field null
        gateway.setOrg(null);

        // Create the Gateway, which fails.
        GatewayDTO gatewayDTO = gatewayMapper.toDto(gateway);

        restGatewayMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(gatewayDTO)))
            .andExpect(status().isBadRequest());

        List<Gateway> gatewayList = gatewayRepository.findAll();
        assertThat(gatewayList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkActiveIsRequired() throws Exception {
        int databaseSizeBeforeTest = gatewayRepository.findAll().size();
        // set the field null
        gateway.setActive(null);

        // Create the Gateway, which fails.
        GatewayDTO gatewayDTO = gatewayMapper.toDto(gateway);

        restGatewayMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(gatewayDTO)))
            .andExpect(status().isBadRequest());

        List<Gateway> gatewayList = gatewayRepository.findAll();
        assertThat(gatewayList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkMfoIsRequired() throws Exception {
        int databaseSizeBeforeTest = gatewayRepository.findAll().size();
        // set the field null
        gateway.setMfo(null);

        // Create the Gateway, which fails.
        GatewayDTO gatewayDTO = gatewayMapper.toDto(gateway);

        restGatewayMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(gatewayDTO)))
            .andExpect(status().isBadRequest());

        List<Gateway> gatewayList = gatewayRepository.findAll();
        assertThat(gatewayList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkBanIsRequired() throws Exception {
        int databaseSizeBeforeTest = gatewayRepository.findAll().size();
        // set the field null
        gateway.setBan(null);

        // Create the Gateway, which fails.
        GatewayDTO gatewayDTO = gatewayMapper.toDto(gateway);

        restGatewayMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(gatewayDTO)))
            .andExpect(status().isBadRequest());

        List<Gateway> gatewayList = gatewayRepository.findAll();
        assertThat(gatewayList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkContractIsRequired() throws Exception {
        int databaseSizeBeforeTest = gatewayRepository.findAll().size();
        // set the field null
        gateway.setContract(null);

        // Create the Gateway, which fails.
        GatewayDTO gatewayDTO = gatewayMapper.toDto(gateway);

        restGatewayMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(gatewayDTO)))
            .andExpect(status().isBadRequest());

        List<Gateway> gatewayList = gatewayRepository.findAll();
        assertThat(gatewayList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllGateways() throws Exception {
        // Initialize the database
        gatewayRepository.saveAndFlush(gateway);

        // Get all the gatewayList
        restGatewayMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(gateway.getId().intValue())))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME)))
            .andExpect(jsonPath("$.[*].org").value(hasItem(DEFAULT_ORG)))
            .andExpect(jsonPath("$.[*].active").value(hasItem(DEFAULT_ACTIVE.booleanValue())))
            .andExpect(jsonPath("$.[*].mfo").value(hasItem(DEFAULT_MFO)))
            .andExpect(jsonPath("$.[*].taxnr").value(hasItem(DEFAULT_TAXNR.intValue())))
            .andExpect(jsonPath("$.[*].ban").value(hasItem(DEFAULT_BAN)))
            .andExpect(jsonPath("$.[*].contract").value(hasItem(DEFAULT_CONTRACT)))
            .andExpect(jsonPath("$.[*].springBeanName").value(hasItem(DEFAULT_SPRING_BEAN_NAME)));
    }

    @Test
    @Transactional
    void getGateway() throws Exception {
        // Initialize the database
        gatewayRepository.saveAndFlush(gateway);

        // Get the gateway
        restGatewayMockMvc
            .perform(get(ENTITY_API_URL_ID, gateway.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(gateway.getId().intValue()))
            .andExpect(jsonPath("$.name").value(DEFAULT_NAME))
            .andExpect(jsonPath("$.org").value(DEFAULT_ORG))
            .andExpect(jsonPath("$.active").value(DEFAULT_ACTIVE.booleanValue()))
            .andExpect(jsonPath("$.mfo").value(DEFAULT_MFO))
            .andExpect(jsonPath("$.taxnr").value(DEFAULT_TAXNR.intValue()))
            .andExpect(jsonPath("$.ban").value(DEFAULT_BAN))
            .andExpect(jsonPath("$.contract").value(DEFAULT_CONTRACT))
            .andExpect(jsonPath("$.springBeanName").value(DEFAULT_SPRING_BEAN_NAME));
    }

    @Test
    @Transactional
    void getGatewaysByIdFiltering() throws Exception {
        // Initialize the database
        gatewayRepository.saveAndFlush(gateway);

        Long id = gateway.getId();

        defaultGatewayShouldBeFound("id.equals=" + id);
        defaultGatewayShouldNotBeFound("id.notEquals=" + id);

        defaultGatewayShouldBeFound("id.greaterThanOrEqual=" + id);
        defaultGatewayShouldNotBeFound("id.greaterThan=" + id);

        defaultGatewayShouldBeFound("id.lessThanOrEqual=" + id);
        defaultGatewayShouldNotBeFound("id.lessThan=" + id);
    }

    @Test
    @Transactional
    void getAllGatewaysByNameIsEqualToSomething() throws Exception {
        // Initialize the database
        gatewayRepository.saveAndFlush(gateway);

        // Get all the gatewayList where name equals to DEFAULT_NAME
        defaultGatewayShouldBeFound("name.equals=" + DEFAULT_NAME);

        // Get all the gatewayList where name equals to UPDATED_NAME
        defaultGatewayShouldNotBeFound("name.equals=" + UPDATED_NAME);
    }

    @Test
    @Transactional
    void getAllGatewaysByNameIsNotEqualToSomething() throws Exception {
        // Initialize the database
        gatewayRepository.saveAndFlush(gateway);

        // Get all the gatewayList where name not equals to DEFAULT_NAME
        defaultGatewayShouldNotBeFound("name.notEquals=" + DEFAULT_NAME);

        // Get all the gatewayList where name not equals to UPDATED_NAME
        defaultGatewayShouldBeFound("name.notEquals=" + UPDATED_NAME);
    }

    @Test
    @Transactional
    void getAllGatewaysByNameIsInShouldWork() throws Exception {
        // Initialize the database
        gatewayRepository.saveAndFlush(gateway);

        // Get all the gatewayList where name in DEFAULT_NAME or UPDATED_NAME
        defaultGatewayShouldBeFound("name.in=" + DEFAULT_NAME + "," + UPDATED_NAME);

        // Get all the gatewayList where name equals to UPDATED_NAME
        defaultGatewayShouldNotBeFound("name.in=" + UPDATED_NAME);
    }

    @Test
    @Transactional
    void getAllGatewaysByNameIsNullOrNotNull() throws Exception {
        // Initialize the database
        gatewayRepository.saveAndFlush(gateway);

        // Get all the gatewayList where name is not null
        defaultGatewayShouldBeFound("name.specified=true");

        // Get all the gatewayList where name is null
        defaultGatewayShouldNotBeFound("name.specified=false");
    }

    @Test
    @Transactional
    void getAllGatewaysByNameContainsSomething() throws Exception {
        // Initialize the database
        gatewayRepository.saveAndFlush(gateway);

        // Get all the gatewayList where name contains DEFAULT_NAME
        defaultGatewayShouldBeFound("name.contains=" + DEFAULT_NAME);

        // Get all the gatewayList where name contains UPDATED_NAME
        defaultGatewayShouldNotBeFound("name.contains=" + UPDATED_NAME);
    }

    @Test
    @Transactional
    void getAllGatewaysByNameNotContainsSomething() throws Exception {
        // Initialize the database
        gatewayRepository.saveAndFlush(gateway);

        // Get all the gatewayList where name does not contain DEFAULT_NAME
        defaultGatewayShouldNotBeFound("name.doesNotContain=" + DEFAULT_NAME);

        // Get all the gatewayList where name does not contain UPDATED_NAME
        defaultGatewayShouldBeFound("name.doesNotContain=" + UPDATED_NAME);
    }

    @Test
    @Transactional
    void getAllGatewaysByOrgIsEqualToSomething() throws Exception {
        // Initialize the database
        gatewayRepository.saveAndFlush(gateway);

        // Get all the gatewayList where org equals to DEFAULT_ORG
        defaultGatewayShouldBeFound("org.equals=" + DEFAULT_ORG);

        // Get all the gatewayList where org equals to UPDATED_ORG
        defaultGatewayShouldNotBeFound("org.equals=" + UPDATED_ORG);
    }

    @Test
    @Transactional
    void getAllGatewaysByOrgIsNotEqualToSomething() throws Exception {
        // Initialize the database
        gatewayRepository.saveAndFlush(gateway);

        // Get all the gatewayList where org not equals to DEFAULT_ORG
        defaultGatewayShouldNotBeFound("org.notEquals=" + DEFAULT_ORG);

        // Get all the gatewayList where org not equals to UPDATED_ORG
        defaultGatewayShouldBeFound("org.notEquals=" + UPDATED_ORG);
    }

    @Test
    @Transactional
    void getAllGatewaysByOrgIsInShouldWork() throws Exception {
        // Initialize the database
        gatewayRepository.saveAndFlush(gateway);

        // Get all the gatewayList where org in DEFAULT_ORG or UPDATED_ORG
        defaultGatewayShouldBeFound("org.in=" + DEFAULT_ORG + "," + UPDATED_ORG);

        // Get all the gatewayList where org equals to UPDATED_ORG
        defaultGatewayShouldNotBeFound("org.in=" + UPDATED_ORG);
    }

    @Test
    @Transactional
    void getAllGatewaysByOrgIsNullOrNotNull() throws Exception {
        // Initialize the database
        gatewayRepository.saveAndFlush(gateway);

        // Get all the gatewayList where org is not null
        defaultGatewayShouldBeFound("org.specified=true");

        // Get all the gatewayList where org is null
        defaultGatewayShouldNotBeFound("org.specified=false");
    }

    @Test
    @Transactional
    void getAllGatewaysByOrgContainsSomething() throws Exception {
        // Initialize the database
        gatewayRepository.saveAndFlush(gateway);

        // Get all the gatewayList where org contains DEFAULT_ORG
        defaultGatewayShouldBeFound("org.contains=" + DEFAULT_ORG);

        // Get all the gatewayList where org contains UPDATED_ORG
        defaultGatewayShouldNotBeFound("org.contains=" + UPDATED_ORG);
    }

    @Test
    @Transactional
    void getAllGatewaysByOrgNotContainsSomething() throws Exception {
        // Initialize the database
        gatewayRepository.saveAndFlush(gateway);

        // Get all the gatewayList where org does not contain DEFAULT_ORG
        defaultGatewayShouldNotBeFound("org.doesNotContain=" + DEFAULT_ORG);

        // Get all the gatewayList where org does not contain UPDATED_ORG
        defaultGatewayShouldBeFound("org.doesNotContain=" + UPDATED_ORG);
    }

    @Test
    @Transactional
    void getAllGatewaysByActiveIsEqualToSomething() throws Exception {
        // Initialize the database
        gatewayRepository.saveAndFlush(gateway);

        // Get all the gatewayList where active equals to DEFAULT_ACTIVE
        defaultGatewayShouldBeFound("active.equals=" + DEFAULT_ACTIVE);

        // Get all the gatewayList where active equals to UPDATED_ACTIVE
        defaultGatewayShouldNotBeFound("active.equals=" + UPDATED_ACTIVE);
    }

    @Test
    @Transactional
    void getAllGatewaysByActiveIsNotEqualToSomething() throws Exception {
        // Initialize the database
        gatewayRepository.saveAndFlush(gateway);

        // Get all the gatewayList where active not equals to DEFAULT_ACTIVE
        defaultGatewayShouldNotBeFound("active.notEquals=" + DEFAULT_ACTIVE);

        // Get all the gatewayList where active not equals to UPDATED_ACTIVE
        defaultGatewayShouldBeFound("active.notEquals=" + UPDATED_ACTIVE);
    }

    @Test
    @Transactional
    void getAllGatewaysByActiveIsInShouldWork() throws Exception {
        // Initialize the database
        gatewayRepository.saveAndFlush(gateway);

        // Get all the gatewayList where active in DEFAULT_ACTIVE or UPDATED_ACTIVE
        defaultGatewayShouldBeFound("active.in=" + DEFAULT_ACTIVE + "," + UPDATED_ACTIVE);

        // Get all the gatewayList where active equals to UPDATED_ACTIVE
        defaultGatewayShouldNotBeFound("active.in=" + UPDATED_ACTIVE);
    }

    @Test
    @Transactional
    void getAllGatewaysByActiveIsNullOrNotNull() throws Exception {
        // Initialize the database
        gatewayRepository.saveAndFlush(gateway);

        // Get all the gatewayList where active is not null
        defaultGatewayShouldBeFound("active.specified=true");

        // Get all the gatewayList where active is null
        defaultGatewayShouldNotBeFound("active.specified=false");
    }

    @Test
    @Transactional
    void getAllGatewaysByMfoIsEqualToSomething() throws Exception {
        // Initialize the database
        gatewayRepository.saveAndFlush(gateway);

        // Get all the gatewayList where mfo equals to DEFAULT_MFO
        defaultGatewayShouldBeFound("mfo.equals=" + DEFAULT_MFO);

        // Get all the gatewayList where mfo equals to UPDATED_MFO
        defaultGatewayShouldNotBeFound("mfo.equals=" + UPDATED_MFO);
    }

    @Test
    @Transactional
    void getAllGatewaysByMfoIsNotEqualToSomething() throws Exception {
        // Initialize the database
        gatewayRepository.saveAndFlush(gateway);

        // Get all the gatewayList where mfo not equals to DEFAULT_MFO
        defaultGatewayShouldNotBeFound("mfo.notEquals=" + DEFAULT_MFO);

        // Get all the gatewayList where mfo not equals to UPDATED_MFO
        defaultGatewayShouldBeFound("mfo.notEquals=" + UPDATED_MFO);
    }

    @Test
    @Transactional
    void getAllGatewaysByMfoIsInShouldWork() throws Exception {
        // Initialize the database
        gatewayRepository.saveAndFlush(gateway);

        // Get all the gatewayList where mfo in DEFAULT_MFO or UPDATED_MFO
        defaultGatewayShouldBeFound("mfo.in=" + DEFAULT_MFO + "," + UPDATED_MFO);

        // Get all the gatewayList where mfo equals to UPDATED_MFO
        defaultGatewayShouldNotBeFound("mfo.in=" + UPDATED_MFO);
    }

    @Test
    @Transactional
    void getAllGatewaysByMfoIsNullOrNotNull() throws Exception {
        // Initialize the database
        gatewayRepository.saveAndFlush(gateway);

        // Get all the gatewayList where mfo is not null
        defaultGatewayShouldBeFound("mfo.specified=true");

        // Get all the gatewayList where mfo is null
        defaultGatewayShouldNotBeFound("mfo.specified=false");
    }

    @Test
    @Transactional
    void getAllGatewaysByMfoContainsSomething() throws Exception {
        // Initialize the database
        gatewayRepository.saveAndFlush(gateway);

        // Get all the gatewayList where mfo contains DEFAULT_MFO
        defaultGatewayShouldBeFound("mfo.contains=" + DEFAULT_MFO);

        // Get all the gatewayList where mfo contains UPDATED_MFO
        defaultGatewayShouldNotBeFound("mfo.contains=" + UPDATED_MFO);
    }

    @Test
    @Transactional
    void getAllGatewaysByMfoNotContainsSomething() throws Exception {
        // Initialize the database
        gatewayRepository.saveAndFlush(gateway);

        // Get all the gatewayList where mfo does not contain DEFAULT_MFO
        defaultGatewayShouldNotBeFound("mfo.doesNotContain=" + DEFAULT_MFO);

        // Get all the gatewayList where mfo does not contain UPDATED_MFO
        defaultGatewayShouldBeFound("mfo.doesNotContain=" + UPDATED_MFO);
    }

    @Test
    @Transactional
    void getAllGatewaysByTaxnrIsEqualToSomething() throws Exception {
        // Initialize the database
        gatewayRepository.saveAndFlush(gateway);

        // Get all the gatewayList where taxnr equals to DEFAULT_TAXNR
        defaultGatewayShouldBeFound("taxnr.equals=" + DEFAULT_TAXNR);

        // Get all the gatewayList where taxnr equals to UPDATED_TAXNR
        defaultGatewayShouldNotBeFound("taxnr.equals=" + UPDATED_TAXNR);
    }

    @Test
    @Transactional
    void getAllGatewaysByTaxnrIsNotEqualToSomething() throws Exception {
        // Initialize the database
        gatewayRepository.saveAndFlush(gateway);

        // Get all the gatewayList where taxnr not equals to DEFAULT_TAXNR
        defaultGatewayShouldNotBeFound("taxnr.notEquals=" + DEFAULT_TAXNR);

        // Get all the gatewayList where taxnr not equals to UPDATED_TAXNR
        defaultGatewayShouldBeFound("taxnr.notEquals=" + UPDATED_TAXNR);
    }

    @Test
    @Transactional
    void getAllGatewaysByTaxnrIsInShouldWork() throws Exception {
        // Initialize the database
        gatewayRepository.saveAndFlush(gateway);

        // Get all the gatewayList where taxnr in DEFAULT_TAXNR or UPDATED_TAXNR
        defaultGatewayShouldBeFound("taxnr.in=" + DEFAULT_TAXNR + "," + UPDATED_TAXNR);

        // Get all the gatewayList where taxnr equals to UPDATED_TAXNR
        defaultGatewayShouldNotBeFound("taxnr.in=" + UPDATED_TAXNR);
    }

    @Test
    @Transactional
    void getAllGatewaysByTaxnrIsNullOrNotNull() throws Exception {
        // Initialize the database
        gatewayRepository.saveAndFlush(gateway);

        // Get all the gatewayList where taxnr is not null
        defaultGatewayShouldBeFound("taxnr.specified=true");

        // Get all the gatewayList where taxnr is null
        defaultGatewayShouldNotBeFound("taxnr.specified=false");
    }

    @Test
    @Transactional
    void getAllGatewaysByTaxnrIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        gatewayRepository.saveAndFlush(gateway);

        // Get all the gatewayList where taxnr is greater than or equal to DEFAULT_TAXNR
        defaultGatewayShouldBeFound("taxnr.greaterThanOrEqual=" + DEFAULT_TAXNR);

        // Get all the gatewayList where taxnr is greater than or equal to UPDATED_TAXNR
        defaultGatewayShouldNotBeFound("taxnr.greaterThanOrEqual=" + UPDATED_TAXNR);
    }

    @Test
    @Transactional
    void getAllGatewaysByTaxnrIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        gatewayRepository.saveAndFlush(gateway);

        // Get all the gatewayList where taxnr is less than or equal to DEFAULT_TAXNR
        defaultGatewayShouldBeFound("taxnr.lessThanOrEqual=" + DEFAULT_TAXNR);

        // Get all the gatewayList where taxnr is less than or equal to SMALLER_TAXNR
        defaultGatewayShouldNotBeFound("taxnr.lessThanOrEqual=" + SMALLER_TAXNR);
    }

    @Test
    @Transactional
    void getAllGatewaysByTaxnrIsLessThanSomething() throws Exception {
        // Initialize the database
        gatewayRepository.saveAndFlush(gateway);

        // Get all the gatewayList where taxnr is less than DEFAULT_TAXNR
        defaultGatewayShouldNotBeFound("taxnr.lessThan=" + DEFAULT_TAXNR);

        // Get all the gatewayList where taxnr is less than UPDATED_TAXNR
        defaultGatewayShouldBeFound("taxnr.lessThan=" + UPDATED_TAXNR);
    }

    @Test
    @Transactional
    void getAllGatewaysByTaxnrIsGreaterThanSomething() throws Exception {
        // Initialize the database
        gatewayRepository.saveAndFlush(gateway);

        // Get all the gatewayList where taxnr is greater than DEFAULT_TAXNR
        defaultGatewayShouldNotBeFound("taxnr.greaterThan=" + DEFAULT_TAXNR);

        // Get all the gatewayList where taxnr is greater than SMALLER_TAXNR
        defaultGatewayShouldBeFound("taxnr.greaterThan=" + SMALLER_TAXNR);
    }

    @Test
    @Transactional
    void getAllGatewaysByBanIsEqualToSomething() throws Exception {
        // Initialize the database
        gatewayRepository.saveAndFlush(gateway);

        // Get all the gatewayList where ban equals to DEFAULT_BAN
        defaultGatewayShouldBeFound("ban.equals=" + DEFAULT_BAN);

        // Get all the gatewayList where ban equals to UPDATED_BAN
        defaultGatewayShouldNotBeFound("ban.equals=" + UPDATED_BAN);
    }

    @Test
    @Transactional
    void getAllGatewaysByBanIsNotEqualToSomething() throws Exception {
        // Initialize the database
        gatewayRepository.saveAndFlush(gateway);

        // Get all the gatewayList where ban not equals to DEFAULT_BAN
        defaultGatewayShouldNotBeFound("ban.notEquals=" + DEFAULT_BAN);

        // Get all the gatewayList where ban not equals to UPDATED_BAN
        defaultGatewayShouldBeFound("ban.notEquals=" + UPDATED_BAN);
    }

    @Test
    @Transactional
    void getAllGatewaysByBanIsInShouldWork() throws Exception {
        // Initialize the database
        gatewayRepository.saveAndFlush(gateway);

        // Get all the gatewayList where ban in DEFAULT_BAN or UPDATED_BAN
        defaultGatewayShouldBeFound("ban.in=" + DEFAULT_BAN + "," + UPDATED_BAN);

        // Get all the gatewayList where ban equals to UPDATED_BAN
        defaultGatewayShouldNotBeFound("ban.in=" + UPDATED_BAN);
    }

    @Test
    @Transactional
    void getAllGatewaysByBanIsNullOrNotNull() throws Exception {
        // Initialize the database
        gatewayRepository.saveAndFlush(gateway);

        // Get all the gatewayList where ban is not null
        defaultGatewayShouldBeFound("ban.specified=true");

        // Get all the gatewayList where ban is null
        defaultGatewayShouldNotBeFound("ban.specified=false");
    }

    @Test
    @Transactional
    void getAllGatewaysByBanContainsSomething() throws Exception {
        // Initialize the database
        gatewayRepository.saveAndFlush(gateway);

        // Get all the gatewayList where ban contains DEFAULT_BAN
        defaultGatewayShouldBeFound("ban.contains=" + DEFAULT_BAN);

        // Get all the gatewayList where ban contains UPDATED_BAN
        defaultGatewayShouldNotBeFound("ban.contains=" + UPDATED_BAN);
    }

    @Test
    @Transactional
    void getAllGatewaysByBanNotContainsSomething() throws Exception {
        // Initialize the database
        gatewayRepository.saveAndFlush(gateway);

        // Get all the gatewayList where ban does not contain DEFAULT_BAN
        defaultGatewayShouldNotBeFound("ban.doesNotContain=" + DEFAULT_BAN);

        // Get all the gatewayList where ban does not contain UPDATED_BAN
        defaultGatewayShouldBeFound("ban.doesNotContain=" + UPDATED_BAN);
    }

    @Test
    @Transactional
    void getAllGatewaysByContractIsEqualToSomething() throws Exception {
        // Initialize the database
        gatewayRepository.saveAndFlush(gateway);

        // Get all the gatewayList where contract equals to DEFAULT_CONTRACT
        defaultGatewayShouldBeFound("contract.equals=" + DEFAULT_CONTRACT);

        // Get all the gatewayList where contract equals to UPDATED_CONTRACT
        defaultGatewayShouldNotBeFound("contract.equals=" + UPDATED_CONTRACT);
    }

    @Test
    @Transactional
    void getAllGatewaysByContractIsNotEqualToSomething() throws Exception {
        // Initialize the database
        gatewayRepository.saveAndFlush(gateway);

        // Get all the gatewayList where contract not equals to DEFAULT_CONTRACT
        defaultGatewayShouldNotBeFound("contract.notEquals=" + DEFAULT_CONTRACT);

        // Get all the gatewayList where contract not equals to UPDATED_CONTRACT
        defaultGatewayShouldBeFound("contract.notEquals=" + UPDATED_CONTRACT);
    }

    @Test
    @Transactional
    void getAllGatewaysByContractIsInShouldWork() throws Exception {
        // Initialize the database
        gatewayRepository.saveAndFlush(gateway);

        // Get all the gatewayList where contract in DEFAULT_CONTRACT or UPDATED_CONTRACT
        defaultGatewayShouldBeFound("contract.in=" + DEFAULT_CONTRACT + "," + UPDATED_CONTRACT);

        // Get all the gatewayList where contract equals to UPDATED_CONTRACT
        defaultGatewayShouldNotBeFound("contract.in=" + UPDATED_CONTRACT);
    }

    @Test
    @Transactional
    void getAllGatewaysByContractIsNullOrNotNull() throws Exception {
        // Initialize the database
        gatewayRepository.saveAndFlush(gateway);

        // Get all the gatewayList where contract is not null
        defaultGatewayShouldBeFound("contract.specified=true");

        // Get all the gatewayList where contract is null
        defaultGatewayShouldNotBeFound("contract.specified=false");
    }

    @Test
    @Transactional
    void getAllGatewaysByContractContainsSomething() throws Exception {
        // Initialize the database
        gatewayRepository.saveAndFlush(gateway);

        // Get all the gatewayList where contract contains DEFAULT_CONTRACT
        defaultGatewayShouldBeFound("contract.contains=" + DEFAULT_CONTRACT);

        // Get all the gatewayList where contract contains UPDATED_CONTRACT
        defaultGatewayShouldNotBeFound("contract.contains=" + UPDATED_CONTRACT);
    }

    @Test
    @Transactional
    void getAllGatewaysByContractNotContainsSomething() throws Exception {
        // Initialize the database
        gatewayRepository.saveAndFlush(gateway);

        // Get all the gatewayList where contract does not contain DEFAULT_CONTRACT
        defaultGatewayShouldNotBeFound("contract.doesNotContain=" + DEFAULT_CONTRACT);

        // Get all the gatewayList where contract does not contain UPDATED_CONTRACT
        defaultGatewayShouldBeFound("contract.doesNotContain=" + UPDATED_CONTRACT);
    }

    @Test
    @Transactional
    void getAllGatewaysBySpringBeanNameIsEqualToSomething() throws Exception {
        // Initialize the database
        gatewayRepository.saveAndFlush(gateway);

        // Get all the gatewayList where springBeanName equals to DEFAULT_SPRING_BEAN_NAME
        defaultGatewayShouldBeFound("springBeanName.equals=" + DEFAULT_SPRING_BEAN_NAME);

        // Get all the gatewayList where springBeanName equals to UPDATED_SPRING_BEAN_NAME
        defaultGatewayShouldNotBeFound("springBeanName.equals=" + UPDATED_SPRING_BEAN_NAME);
    }

    @Test
    @Transactional
    void getAllGatewaysBySpringBeanNameIsNotEqualToSomething() throws Exception {
        // Initialize the database
        gatewayRepository.saveAndFlush(gateway);

        // Get all the gatewayList where springBeanName not equals to DEFAULT_SPRING_BEAN_NAME
        defaultGatewayShouldNotBeFound("springBeanName.notEquals=" + DEFAULT_SPRING_BEAN_NAME);

        // Get all the gatewayList where springBeanName not equals to UPDATED_SPRING_BEAN_NAME
        defaultGatewayShouldBeFound("springBeanName.notEquals=" + UPDATED_SPRING_BEAN_NAME);
    }

    @Test
    @Transactional
    void getAllGatewaysBySpringBeanNameIsInShouldWork() throws Exception {
        // Initialize the database
        gatewayRepository.saveAndFlush(gateway);

        // Get all the gatewayList where springBeanName in DEFAULT_SPRING_BEAN_NAME or UPDATED_SPRING_BEAN_NAME
        defaultGatewayShouldBeFound("springBeanName.in=" + DEFAULT_SPRING_BEAN_NAME + "," + UPDATED_SPRING_BEAN_NAME);

        // Get all the gatewayList where springBeanName equals to UPDATED_SPRING_BEAN_NAME
        defaultGatewayShouldNotBeFound("springBeanName.in=" + UPDATED_SPRING_BEAN_NAME);
    }

    @Test
    @Transactional
    void getAllGatewaysBySpringBeanNameIsNullOrNotNull() throws Exception {
        // Initialize the database
        gatewayRepository.saveAndFlush(gateway);

        // Get all the gatewayList where springBeanName is not null
        defaultGatewayShouldBeFound("springBeanName.specified=true");

        // Get all the gatewayList where springBeanName is null
        defaultGatewayShouldNotBeFound("springBeanName.specified=false");
    }

    @Test
    @Transactional
    void getAllGatewaysBySpringBeanNameContainsSomething() throws Exception {
        // Initialize the database
        gatewayRepository.saveAndFlush(gateway);

        // Get all the gatewayList where springBeanName contains DEFAULT_SPRING_BEAN_NAME
        defaultGatewayShouldBeFound("springBeanName.contains=" + DEFAULT_SPRING_BEAN_NAME);

        // Get all the gatewayList where springBeanName contains UPDATED_SPRING_BEAN_NAME
        defaultGatewayShouldNotBeFound("springBeanName.contains=" + UPDATED_SPRING_BEAN_NAME);
    }

    @Test
    @Transactional
    void getAllGatewaysBySpringBeanNameNotContainsSomething() throws Exception {
        // Initialize the database
        gatewayRepository.saveAndFlush(gateway);

        // Get all the gatewayList where springBeanName does not contain DEFAULT_SPRING_BEAN_NAME
        defaultGatewayShouldNotBeFound("springBeanName.doesNotContain=" + DEFAULT_SPRING_BEAN_NAME);

        // Get all the gatewayList where springBeanName does not contain UPDATED_SPRING_BEAN_NAME
        defaultGatewayShouldBeFound("springBeanName.doesNotContain=" + UPDATED_SPRING_BEAN_NAME);
    }

    @Test
    @Transactional
    void getAllGatewaysByDepositIsEqualToSomething() throws Exception {
        // Initialize the database
        gatewayRepository.saveAndFlush(gateway);
        Deposit deposit = DepositResourceIT.createEntity(em);
        em.persist(deposit);
        em.flush();
        gateway.setDeposit(deposit);
        deposit.setGateway(gateway);
        gatewayRepository.saveAndFlush(gateway);
        Long depositId = deposit.getId();

        // Get all the gatewayList where deposit equals to depositId
        defaultGatewayShouldBeFound("depositId.equals=" + depositId);

        // Get all the gatewayList where deposit equals to (depositId + 1)
        defaultGatewayShouldNotBeFound("depositId.equals=" + (depositId + 1));
    }

    /**
     * Executes the search, and checks that the default entity is returned.
     */
    private void defaultGatewayShouldBeFound(String filter) throws Exception {
        restGatewayMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(gateway.getId().intValue())))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME)))
            .andExpect(jsonPath("$.[*].org").value(hasItem(DEFAULT_ORG)))
            .andExpect(jsonPath("$.[*].active").value(hasItem(DEFAULT_ACTIVE.booleanValue())))
            .andExpect(jsonPath("$.[*].mfo").value(hasItem(DEFAULT_MFO)))
            .andExpect(jsonPath("$.[*].taxnr").value(hasItem(DEFAULT_TAXNR.intValue())))
            .andExpect(jsonPath("$.[*].ban").value(hasItem(DEFAULT_BAN)))
            .andExpect(jsonPath("$.[*].contract").value(hasItem(DEFAULT_CONTRACT)))
            .andExpect(jsonPath("$.[*].springBeanName").value(hasItem(DEFAULT_SPRING_BEAN_NAME)));

        // Check, that the count call also returns 1
        restGatewayMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("1"));
    }

    /**
     * Executes the search, and checks that the default entity is not returned.
     */
    private void defaultGatewayShouldNotBeFound(String filter) throws Exception {
        restGatewayMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty());

        // Check, that the count call also returns 0
        restGatewayMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("0"));
    }

    @Test
    @Transactional
    void getNonExistingGateway() throws Exception {
        // Get the gateway
        restGatewayMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putNewGateway() throws Exception {
        // Initialize the database
        gatewayRepository.saveAndFlush(gateway);

        int databaseSizeBeforeUpdate = gatewayRepository.findAll().size();

        // Update the gateway
        Gateway updatedGateway = gatewayRepository.findById(gateway.getId()).get();
        // Disconnect from session so that the updates on updatedGateway are not directly saved in db
        em.detach(updatedGateway);
        updatedGateway
            .name(UPDATED_NAME)
            .org(UPDATED_ORG)
            .active(UPDATED_ACTIVE)
            .mfo(UPDATED_MFO)
            .taxnr(UPDATED_TAXNR)
            .ban(UPDATED_BAN)
            .contract(UPDATED_CONTRACT)
            .springBeanName(UPDATED_SPRING_BEAN_NAME);
        GatewayDTO gatewayDTO = gatewayMapper.toDto(updatedGateway);

        restGatewayMockMvc
            .perform(
                put(ENTITY_API_URL_ID, gatewayDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(gatewayDTO))
            )
            .andExpect(status().isOk());

        // Validate the Gateway in the database
        List<Gateway> gatewayList = gatewayRepository.findAll();
        assertThat(gatewayList).hasSize(databaseSizeBeforeUpdate);
        Gateway testGateway = gatewayList.get(gatewayList.size() - 1);
        assertThat(testGateway.getName()).isEqualTo(UPDATED_NAME);
        assertThat(testGateway.getOrg()).isEqualTo(UPDATED_ORG);
        assertThat(testGateway.getActive()).isEqualTo(UPDATED_ACTIVE);
        assertThat(testGateway.getMfo()).isEqualTo(UPDATED_MFO);
        assertThat(testGateway.getTaxnr()).isEqualTo(UPDATED_TAXNR);
        assertThat(testGateway.getBan()).isEqualTo(UPDATED_BAN);
        assertThat(testGateway.getContract()).isEqualTo(UPDATED_CONTRACT);
        assertThat(testGateway.getSpringBeanName()).isEqualTo(UPDATED_SPRING_BEAN_NAME);
    }

    @Test
    @Transactional
    void putNonExistingGateway() throws Exception {
        int databaseSizeBeforeUpdate = gatewayRepository.findAll().size();
        gateway.setId(count.incrementAndGet());

        // Create the Gateway
        GatewayDTO gatewayDTO = gatewayMapper.toDto(gateway);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restGatewayMockMvc
            .perform(
                put(ENTITY_API_URL_ID, gatewayDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(gatewayDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Gateway in the database
        List<Gateway> gatewayList = gatewayRepository.findAll();
        assertThat(gatewayList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchGateway() throws Exception {
        int databaseSizeBeforeUpdate = gatewayRepository.findAll().size();
        gateway.setId(count.incrementAndGet());

        // Create the Gateway
        GatewayDTO gatewayDTO = gatewayMapper.toDto(gateway);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restGatewayMockMvc
            .perform(
                put(ENTITY_API_URL_ID, count.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(gatewayDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Gateway in the database
        List<Gateway> gatewayList = gatewayRepository.findAll();
        assertThat(gatewayList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamGateway() throws Exception {
        int databaseSizeBeforeUpdate = gatewayRepository.findAll().size();
        gateway.setId(count.incrementAndGet());

        // Create the Gateway
        GatewayDTO gatewayDTO = gatewayMapper.toDto(gateway);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restGatewayMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(gatewayDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Gateway in the database
        List<Gateway> gatewayList = gatewayRepository.findAll();
        assertThat(gatewayList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateGatewayWithPatch() throws Exception {
        // Initialize the database
        gatewayRepository.saveAndFlush(gateway);

        int databaseSizeBeforeUpdate = gatewayRepository.findAll().size();

        // Update the gateway using partial update
        Gateway partialUpdatedGateway = new Gateway();
        partialUpdatedGateway.setId(gateway.getId());

        partialUpdatedGateway.active(UPDATED_ACTIVE).contract(UPDATED_CONTRACT);

        restGatewayMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedGateway.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedGateway))
            )
            .andExpect(status().isOk());

        // Validate the Gateway in the database
        List<Gateway> gatewayList = gatewayRepository.findAll();
        assertThat(gatewayList).hasSize(databaseSizeBeforeUpdate);
        Gateway testGateway = gatewayList.get(gatewayList.size() - 1);
        assertThat(testGateway.getName()).isEqualTo(DEFAULT_NAME);
        assertThat(testGateway.getOrg()).isEqualTo(DEFAULT_ORG);
        assertThat(testGateway.getActive()).isEqualTo(UPDATED_ACTIVE);
        assertThat(testGateway.getMfo()).isEqualTo(DEFAULT_MFO);
        assertThat(testGateway.getTaxnr()).isEqualTo(DEFAULT_TAXNR);
        assertThat(testGateway.getBan()).isEqualTo(DEFAULT_BAN);
        assertThat(testGateway.getContract()).isEqualTo(UPDATED_CONTRACT);
        assertThat(testGateway.getSpringBeanName()).isEqualTo(DEFAULT_SPRING_BEAN_NAME);
    }

    @Test
    @Transactional
    void fullUpdateGatewayWithPatch() throws Exception {
        // Initialize the database
        gatewayRepository.saveAndFlush(gateway);

        int databaseSizeBeforeUpdate = gatewayRepository.findAll().size();

        // Update the gateway using partial update
        Gateway partialUpdatedGateway = new Gateway();
        partialUpdatedGateway.setId(gateway.getId());

        partialUpdatedGateway
            .name(UPDATED_NAME)
            .org(UPDATED_ORG)
            .active(UPDATED_ACTIVE)
            .mfo(UPDATED_MFO)
            .taxnr(UPDATED_TAXNR)
            .ban(UPDATED_BAN)
            .contract(UPDATED_CONTRACT)
            .springBeanName(UPDATED_SPRING_BEAN_NAME);

        restGatewayMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedGateway.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedGateway))
            )
            .andExpect(status().isOk());

        // Validate the Gateway in the database
        List<Gateway> gatewayList = gatewayRepository.findAll();
        assertThat(gatewayList).hasSize(databaseSizeBeforeUpdate);
        Gateway testGateway = gatewayList.get(gatewayList.size() - 1);
        assertThat(testGateway.getName()).isEqualTo(UPDATED_NAME);
        assertThat(testGateway.getOrg()).isEqualTo(UPDATED_ORG);
        assertThat(testGateway.getActive()).isEqualTo(UPDATED_ACTIVE);
        assertThat(testGateway.getMfo()).isEqualTo(UPDATED_MFO);
        assertThat(testGateway.getTaxnr()).isEqualTo(UPDATED_TAXNR);
        assertThat(testGateway.getBan()).isEqualTo(UPDATED_BAN);
        assertThat(testGateway.getContract()).isEqualTo(UPDATED_CONTRACT);
        assertThat(testGateway.getSpringBeanName()).isEqualTo(UPDATED_SPRING_BEAN_NAME);
    }

    @Test
    @Transactional
    void patchNonExistingGateway() throws Exception {
        int databaseSizeBeforeUpdate = gatewayRepository.findAll().size();
        gateway.setId(count.incrementAndGet());

        // Create the Gateway
        GatewayDTO gatewayDTO = gatewayMapper.toDto(gateway);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restGatewayMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, gatewayDTO.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(gatewayDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Gateway in the database
        List<Gateway> gatewayList = gatewayRepository.findAll();
        assertThat(gatewayList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchGateway() throws Exception {
        int databaseSizeBeforeUpdate = gatewayRepository.findAll().size();
        gateway.setId(count.incrementAndGet());

        // Create the Gateway
        GatewayDTO gatewayDTO = gatewayMapper.toDto(gateway);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restGatewayMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, count.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(gatewayDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Gateway in the database
        List<Gateway> gatewayList = gatewayRepository.findAll();
        assertThat(gatewayList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamGateway() throws Exception {
        int databaseSizeBeforeUpdate = gatewayRepository.findAll().size();
        gateway.setId(count.incrementAndGet());

        // Create the Gateway
        GatewayDTO gatewayDTO = gatewayMapper.toDto(gateway);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restGatewayMockMvc
            .perform(
                patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(TestUtil.convertObjectToJsonBytes(gatewayDTO))
            )
            .andExpect(status().isMethodNotAllowed());

        // Validate the Gateway in the database
        List<Gateway> gatewayList = gatewayRepository.findAll();
        assertThat(gatewayList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteGateway() throws Exception {
        // Initialize the database
        gatewayRepository.saveAndFlush(gateway);

        int databaseSizeBeforeDelete = gatewayRepository.findAll().size();

        // Delete the gateway
        restGatewayMockMvc
            .perform(delete(ENTITY_API_URL_ID, gateway.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        List<Gateway> gatewayList = gatewayRepository.findAll();
        assertThat(gatewayList).hasSize(databaseSizeBeforeDelete - 1);
    }
}
