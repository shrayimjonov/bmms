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
import uz.paykap.domain.Deposit;
import uz.paykap.domain.Gateway;
import uz.paykap.repository.GatewayRepository;
import uz.paykap.service.GatewayQueryService;
import uz.paykap.service.GatewayService;
import uz.paykap.service.dto.GatewayDTO;
import uz.paykap.service.mapper.GatewayMapper;
import uz.paykap.web.rest.errors.ExceptionTranslator;

/**
 * Test class for the GatewayResource REST controller.
 *
 * @see GatewayResource
 */
@RunWith(SpringRunner.class)
@IntegrationTest
public class GatewayResourceIntTest {

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

    private static final String DEFAULT_BAN = "AAAAAAAAAA";
    private static final String UPDATED_BAN = "BBBBBBBBBB";

    private static final String DEFAULT_CONTRACT = "AAAAAAAAAA";
    private static final String UPDATED_CONTRACT = "BBBBBBBBBB";

    private static final String DEFAULT_SPRING_BEAN_NAME = "AAAAAAAAAA";
    private static final String UPDATED_SPRING_BEAN_NAME = "BBBBBBBBBB";

    @Autowired
    private GatewayRepository gatewayRepository;

    @Autowired
    private GatewayMapper gatewayMapper;

    @Autowired
    private GatewayService gatewayService;

    @Autowired
    private GatewayQueryService gatewayQueryService;

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

    private MockMvc restGatewayMockMvc;

    private Gateway gateway;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        final GatewayResource gatewayResource = new GatewayResource(gatewayService, gatewayRepository, gatewayQueryService);
        this.restGatewayMockMvc =
            MockMvcBuilders
                .standaloneSetup(gatewayResource)
                .setCustomArgumentResolvers(pageableArgumentResolver)
                .setControllerAdvice(exceptionTranslator)
                .setConversionService(createFormattingConversionService())
                .setMessageConverters(jacksonMessageConverter)
                .setValidator(validator)
                .build();
    }

    /**
     * Create an entity for this test.
     * <p>
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

    @Before
    public void initTest() {
        gateway = createEntity(em);
    }

    @Test
    @Transactional
    public void createGateway() throws Exception {
        int databaseSizeBeforeCreate = gatewayRepository.findAll().size();

        // Create the Gateway
        GatewayDTO gatewayDTO = gatewayMapper.toDto(gateway);
        restGatewayMockMvc
            .perform(
                post("/api/gateways").contentType(TestUtil.APPLICATION_JSON_UTF8).content(TestUtil.convertObjectToJsonBytes(gatewayDTO))
            )
            .andExpect(status().isCreated());

        // Validate the Gateway in the database
        List<Gateway> gatewayList = gatewayRepository.findAll();
        assertThat(gatewayList).hasSize(databaseSizeBeforeCreate + 1);
        Gateway testGateway = gatewayList.get(gatewayList.size() - 1);
        assertThat(testGateway.getName()).isEqualTo(DEFAULT_NAME);
        assertThat(testGateway.getOrg()).isEqualTo(DEFAULT_ORG);
        assertThat(testGateway.isActive()).isEqualTo(DEFAULT_ACTIVE);
        assertThat(testGateway.getMfo()).isEqualTo(DEFAULT_MFO);
        assertThat(testGateway.getTaxnr()).isEqualTo(DEFAULT_TAXNR);
        assertThat(testGateway.getBan()).isEqualTo(DEFAULT_BAN);
        assertThat(testGateway.getContract()).isEqualTo(DEFAULT_CONTRACT);
        assertThat(testGateway.getSpringBeanName()).isEqualTo(DEFAULT_SPRING_BEAN_NAME);
    }

    @Test
    @Transactional
    public void createGatewayWithExistingId() throws Exception {
        int databaseSizeBeforeCreate = gatewayRepository.findAll().size();

        // Create the Gateway with an existing ID
        gateway.setId(1L);
        GatewayDTO gatewayDTO = gatewayMapper.toDto(gateway);

        // An entity with an existing ID cannot be created, so this API call must fail
        restGatewayMockMvc
            .perform(
                post("/api/gateways").contentType(TestUtil.APPLICATION_JSON_UTF8).content(TestUtil.convertObjectToJsonBytes(gatewayDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Gateway in the database
        List<Gateway> gatewayList = gatewayRepository.findAll();
        assertThat(gatewayList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    public void checkNameIsRequired() throws Exception {
        int databaseSizeBeforeTest = gatewayRepository.findAll().size();
        // set the field null
        gateway.setName(null);

        // Create the Gateway, which fails.
        GatewayDTO gatewayDTO = gatewayMapper.toDto(gateway);

        restGatewayMockMvc
            .perform(
                post("/api/gateways").contentType(TestUtil.APPLICATION_JSON_UTF8).content(TestUtil.convertObjectToJsonBytes(gatewayDTO))
            )
            .andExpect(status().isBadRequest());

        List<Gateway> gatewayList = gatewayRepository.findAll();
        assertThat(gatewayList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkOrgIsRequired() throws Exception {
        int databaseSizeBeforeTest = gatewayRepository.findAll().size();
        // set the field null
        gateway.setOrg(null);

        // Create the Gateway, which fails.
        GatewayDTO gatewayDTO = gatewayMapper.toDto(gateway);

        restGatewayMockMvc
            .perform(
                post("/api/gateways").contentType(TestUtil.APPLICATION_JSON_UTF8).content(TestUtil.convertObjectToJsonBytes(gatewayDTO))
            )
            .andExpect(status().isBadRequest());

        List<Gateway> gatewayList = gatewayRepository.findAll();
        assertThat(gatewayList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkActiveIsRequired() throws Exception {
        int databaseSizeBeforeTest = gatewayRepository.findAll().size();
        // set the field null
        gateway.setActive(null);

        // Create the Gateway, which fails.
        GatewayDTO gatewayDTO = gatewayMapper.toDto(gateway);

        restGatewayMockMvc
            .perform(
                post("/api/gateways").contentType(TestUtil.APPLICATION_JSON_UTF8).content(TestUtil.convertObjectToJsonBytes(gatewayDTO))
            )
            .andExpect(status().isBadRequest());

        List<Gateway> gatewayList = gatewayRepository.findAll();
        assertThat(gatewayList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkMfoIsRequired() throws Exception {
        int databaseSizeBeforeTest = gatewayRepository.findAll().size();
        // set the field null
        gateway.setMfo(null);

        // Create the Gateway, which fails.
        GatewayDTO gatewayDTO = gatewayMapper.toDto(gateway);

        restGatewayMockMvc
            .perform(
                post("/api/gateways").contentType(TestUtil.APPLICATION_JSON_UTF8).content(TestUtil.convertObjectToJsonBytes(gatewayDTO))
            )
            .andExpect(status().isBadRequest());

        List<Gateway> gatewayList = gatewayRepository.findAll();
        assertThat(gatewayList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkBanIsRequired() throws Exception {
        int databaseSizeBeforeTest = gatewayRepository.findAll().size();
        // set the field null
        gateway.setBan(null);

        // Create the Gateway, which fails.
        GatewayDTO gatewayDTO = gatewayMapper.toDto(gateway);

        restGatewayMockMvc
            .perform(
                post("/api/gateways").contentType(TestUtil.APPLICATION_JSON_UTF8).content(TestUtil.convertObjectToJsonBytes(gatewayDTO))
            )
            .andExpect(status().isBadRequest());

        List<Gateway> gatewayList = gatewayRepository.findAll();
        assertThat(gatewayList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkContractIsRequired() throws Exception {
        int databaseSizeBeforeTest = gatewayRepository.findAll().size();
        // set the field null
        gateway.setContract(null);

        // Create the Gateway, which fails.
        GatewayDTO gatewayDTO = gatewayMapper.toDto(gateway);

        restGatewayMockMvc
            .perform(
                post("/api/gateways").contentType(TestUtil.APPLICATION_JSON_UTF8).content(TestUtil.convertObjectToJsonBytes(gatewayDTO))
            )
            .andExpect(status().isBadRequest());

        List<Gateway> gatewayList = gatewayRepository.findAll();
        assertThat(gatewayList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void getAllGateways() throws Exception {
        // Initialize the database
        gatewayRepository.saveAndFlush(gateway);

        // Get all the gatewayList
        restGatewayMockMvc
            .perform(get("/api/gateways?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(gateway.getId().intValue())))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME.toString())))
            .andExpect(jsonPath("$.[*].org").value(hasItem(DEFAULT_ORG.toString())))
            .andExpect(jsonPath("$.[*].active").value(hasItem(DEFAULT_ACTIVE.booleanValue())))
            .andExpect(jsonPath("$.[*].mfo").value(hasItem(DEFAULT_MFO.toString())))
            .andExpect(jsonPath("$.[*].taxnr").value(hasItem(DEFAULT_TAXNR.intValue())))
            .andExpect(jsonPath("$.[*].ban").value(hasItem(DEFAULT_BAN.toString())))
            .andExpect(jsonPath("$.[*].contract").value(hasItem(DEFAULT_CONTRACT.toString())))
            .andExpect(jsonPath("$.[*].springBeanName").value(hasItem(DEFAULT_SPRING_BEAN_NAME.toString())));
    }

    @Test
    @Transactional
    public void getGateway() throws Exception {
        // Initialize the database
        gatewayRepository.saveAndFlush(gateway);

        // Get the gateway
        restGatewayMockMvc
            .perform(get("/api/gateways/{id}", gateway.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.id").value(gateway.getId().intValue()))
            .andExpect(jsonPath("$.name").value(DEFAULT_NAME.toString()))
            .andExpect(jsonPath("$.org").value(DEFAULT_ORG.toString()))
            .andExpect(jsonPath("$.active").value(DEFAULT_ACTIVE.booleanValue()))
            .andExpect(jsonPath("$.mfo").value(DEFAULT_MFO.toString()))
            .andExpect(jsonPath("$.taxnr").value(DEFAULT_TAXNR.intValue()))
            .andExpect(jsonPath("$.ban").value(DEFAULT_BAN.toString()))
            .andExpect(jsonPath("$.contract").value(DEFAULT_CONTRACT.toString()))
            .andExpect(jsonPath("$.springBeanName").value(DEFAULT_SPRING_BEAN_NAME.toString()));
    }

    @Test
    @Transactional
    public void getAllGatewaysByNameIsEqualToSomething() throws Exception {
        // Initialize the database
        gatewayRepository.saveAndFlush(gateway);

        // Get all the gatewayList where name equals to DEFAULT_NAME
        defaultGatewayShouldBeFound("name.equals=" + DEFAULT_NAME);

        // Get all the gatewayList where name equals to UPDATED_NAME
        defaultGatewayShouldNotBeFound("name.equals=" + UPDATED_NAME);
    }

    @Test
    @Transactional
    public void getAllGatewaysByNameIsInShouldWork() throws Exception {
        // Initialize the database
        gatewayRepository.saveAndFlush(gateway);

        // Get all the gatewayList where name in DEFAULT_NAME or UPDATED_NAME
        defaultGatewayShouldBeFound("name.in=" + DEFAULT_NAME + "," + UPDATED_NAME);

        // Get all the gatewayList where name equals to UPDATED_NAME
        defaultGatewayShouldNotBeFound("name.in=" + UPDATED_NAME);
    }

    @Test
    @Transactional
    public void getAllGatewaysByNameIsNullOrNotNull() throws Exception {
        // Initialize the database
        gatewayRepository.saveAndFlush(gateway);

        // Get all the gatewayList where name is not null
        defaultGatewayShouldBeFound("name.specified=true");

        // Get all the gatewayList where name is null
        defaultGatewayShouldNotBeFound("name.specified=false");
    }

    @Test
    @Transactional
    public void getAllGatewaysByOrgIsEqualToSomething() throws Exception {
        // Initialize the database
        gatewayRepository.saveAndFlush(gateway);

        // Get all the gatewayList where org equals to DEFAULT_ORG
        defaultGatewayShouldBeFound("org.equals=" + DEFAULT_ORG);

        // Get all the gatewayList where org equals to UPDATED_ORG
        defaultGatewayShouldNotBeFound("org.equals=" + UPDATED_ORG);
    }

    @Test
    @Transactional
    public void getAllGatewaysByOrgIsInShouldWork() throws Exception {
        // Initialize the database
        gatewayRepository.saveAndFlush(gateway);

        // Get all the gatewayList where org in DEFAULT_ORG or UPDATED_ORG
        defaultGatewayShouldBeFound("org.in=" + DEFAULT_ORG + "," + UPDATED_ORG);

        // Get all the gatewayList where org equals to UPDATED_ORG
        defaultGatewayShouldNotBeFound("org.in=" + UPDATED_ORG);
    }

    @Test
    @Transactional
    public void getAllGatewaysByOrgIsNullOrNotNull() throws Exception {
        // Initialize the database
        gatewayRepository.saveAndFlush(gateway);

        // Get all the gatewayList where org is not null
        defaultGatewayShouldBeFound("org.specified=true");

        // Get all the gatewayList where org is null
        defaultGatewayShouldNotBeFound("org.specified=false");
    }

    //    @Test
    @Transactional
    public void getAllGatewaysByActiveIsEqualToSomething() throws Exception {
        // Initialize the database
        gatewayRepository.saveAndFlush(gateway);

        // Get all the gatewayList where active equals to DEFAULT_ACTIVE
        defaultGatewayShouldBeFound("active.equals=" + DEFAULT_ACTIVE);

        // Get all the gatewayList where active equals to UPDATED_ACTIVE
        defaultGatewayShouldNotBeFound("active.equals=" + UPDATED_ACTIVE);
    }

    //    @Test
    @Transactional
    public void getAllGatewaysByActiveIsInShouldWork() throws Exception {
        // Initialize the database
        gatewayRepository.saveAndFlush(gateway);

        // Get all the gatewayList where active in DEFAULT_ACTIVE or UPDATED_ACTIVE
        defaultGatewayShouldBeFound("active.in=" + DEFAULT_ACTIVE + "," + UPDATED_ACTIVE);

        // Get all the gatewayList where active equals to UPDATED_ACTIVE
        defaultGatewayShouldNotBeFound("active.in=" + UPDATED_ACTIVE);
    }

    @Test
    @Transactional
    public void getAllGatewaysByActiveIsNullOrNotNull() throws Exception {
        // Initialize the database
        gatewayRepository.saveAndFlush(gateway);

        // Get all the gatewayList where active is not null
        defaultGatewayShouldBeFound("active.specified=true");

        // Get all the gatewayList where active is null
        defaultGatewayShouldNotBeFound("active.specified=false");
    }

    @Test
    @Transactional
    public void getAllGatewaysByMfoIsEqualToSomething() throws Exception {
        // Initialize the database
        gatewayRepository.saveAndFlush(gateway);

        // Get all the gatewayList where mfo equals to DEFAULT_MFO
        defaultGatewayShouldBeFound("mfo.equals=" + DEFAULT_MFO);

        // Get all the gatewayList where mfo equals to UPDATED_MFO
        defaultGatewayShouldNotBeFound("mfo.equals=" + UPDATED_MFO);
    }

    @Test
    @Transactional
    public void getAllGatewaysByMfoIsInShouldWork() throws Exception {
        // Initialize the database
        gatewayRepository.saveAndFlush(gateway);

        // Get all the gatewayList where mfo in DEFAULT_MFO or UPDATED_MFO
        defaultGatewayShouldBeFound("mfo.in=" + DEFAULT_MFO + "," + UPDATED_MFO);

        // Get all the gatewayList where mfo equals to UPDATED_MFO
        defaultGatewayShouldNotBeFound("mfo.in=" + UPDATED_MFO);
    }

    @Test
    @Transactional
    public void getAllGatewaysByMfoIsNullOrNotNull() throws Exception {
        // Initialize the database
        gatewayRepository.saveAndFlush(gateway);

        // Get all the gatewayList where mfo is not null
        defaultGatewayShouldBeFound("mfo.specified=true");

        // Get all the gatewayList where mfo is null
        defaultGatewayShouldNotBeFound("mfo.specified=false");
    }

    @Test
    @Transactional
    public void getAllGatewaysByTaxnrIsEqualToSomething() throws Exception {
        // Initialize the database
        gatewayRepository.saveAndFlush(gateway);

        // Get all the gatewayList where taxnr equals to DEFAULT_TAXNR
        defaultGatewayShouldBeFound("taxnr.equals=" + DEFAULT_TAXNR);

        // Get all the gatewayList where taxnr equals to UPDATED_TAXNR
        defaultGatewayShouldNotBeFound("taxnr.equals=" + UPDATED_TAXNR);
    }

    @Test
    @Transactional
    public void getAllGatewaysByTaxnrIsInShouldWork() throws Exception {
        // Initialize the database
        gatewayRepository.saveAndFlush(gateway);

        // Get all the gatewayList where taxnr in DEFAULT_TAXNR or UPDATED_TAXNR
        defaultGatewayShouldBeFound("taxnr.in=" + DEFAULT_TAXNR + "," + UPDATED_TAXNR);

        // Get all the gatewayList where taxnr equals to UPDATED_TAXNR
        defaultGatewayShouldNotBeFound("taxnr.in=" + UPDATED_TAXNR);
    }

    @Test
    @Transactional
    public void getAllGatewaysByTaxnrIsNullOrNotNull() throws Exception {
        // Initialize the database
        gatewayRepository.saveAndFlush(gateway);

        // Get all the gatewayList where taxnr is not null
        defaultGatewayShouldBeFound("taxnr.specified=true");

        // Get all the gatewayList where taxnr is null
        defaultGatewayShouldNotBeFound("taxnr.specified=false");
    }

    //    @Test
    @Transactional
    public void getAllGatewaysByTaxnrIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        gatewayRepository.saveAndFlush(gateway);

        // Get all the gatewayList where taxnr greater than or equals to DEFAULT_TAXNR
        defaultGatewayShouldBeFound("taxnr.greaterOrEqualThan=" + DEFAULT_TAXNR);

        // Get all the gatewayList where taxnr greater than or equals to UPDATED_TAXNR
        defaultGatewayShouldNotBeFound("taxnr.greaterOrEqualThan=" + UPDATED_TAXNR);
    }

    @Test
    @Transactional
    public void getAllGatewaysByTaxnrIsLessThanSomething() throws Exception {
        // Initialize the database
        gatewayRepository.saveAndFlush(gateway);

        // Get all the gatewayList where taxnr less than or equals to DEFAULT_TAXNR
        defaultGatewayShouldNotBeFound("taxnr.lessThan=" + DEFAULT_TAXNR);

        // Get all the gatewayList where taxnr less than or equals to UPDATED_TAXNR
        defaultGatewayShouldBeFound("taxnr.lessThan=" + UPDATED_TAXNR);
    }

    @Test
    @Transactional
    public void getAllGatewaysByBanIsEqualToSomething() throws Exception {
        // Initialize the database
        gatewayRepository.saveAndFlush(gateway);

        // Get all the gatewayList where ban equals to DEFAULT_BAN
        defaultGatewayShouldBeFound("ban.equals=" + DEFAULT_BAN);

        // Get all the gatewayList where ban equals to UPDATED_BAN
        defaultGatewayShouldNotBeFound("ban.equals=" + UPDATED_BAN);
    }

    @Test
    @Transactional
    public void getAllGatewaysByBanIsInShouldWork() throws Exception {
        // Initialize the database
        gatewayRepository.saveAndFlush(gateway);

        // Get all the gatewayList where ban in DEFAULT_BAN or UPDATED_BAN
        defaultGatewayShouldBeFound("ban.in=" + DEFAULT_BAN + "," + UPDATED_BAN);

        // Get all the gatewayList where ban equals to UPDATED_BAN
        defaultGatewayShouldNotBeFound("ban.in=" + UPDATED_BAN);
    }

    @Test
    @Transactional
    public void getAllGatewaysByBanIsNullOrNotNull() throws Exception {
        // Initialize the database
        gatewayRepository.saveAndFlush(gateway);

        // Get all the gatewayList where ban is not null
        defaultGatewayShouldBeFound("ban.specified=true");

        // Get all the gatewayList where ban is null
        defaultGatewayShouldNotBeFound("ban.specified=false");
    }

    @Test
    @Transactional
    public void getAllGatewaysByContractIsEqualToSomething() throws Exception {
        // Initialize the database
        gatewayRepository.saveAndFlush(gateway);

        // Get all the gatewayList where contract equals to DEFAULT_CONTRACT
        defaultGatewayShouldBeFound("contract.equals=" + DEFAULT_CONTRACT);

        // Get all the gatewayList where contract equals to UPDATED_CONTRACT
        defaultGatewayShouldNotBeFound("contract.equals=" + UPDATED_CONTRACT);
    }

    @Test
    @Transactional
    public void getAllGatewaysByContractIsInShouldWork() throws Exception {
        // Initialize the database
        gatewayRepository.saveAndFlush(gateway);

        // Get all the gatewayList where contract in DEFAULT_CONTRACT or UPDATED_CONTRACT
        defaultGatewayShouldBeFound("contract.in=" + DEFAULT_CONTRACT + "," + UPDATED_CONTRACT);

        // Get all the gatewayList where contract equals to UPDATED_CONTRACT
        defaultGatewayShouldNotBeFound("contract.in=" + UPDATED_CONTRACT);
    }

    @Test
    @Transactional
    public void getAllGatewaysByContractIsNullOrNotNull() throws Exception {
        // Initialize the database
        gatewayRepository.saveAndFlush(gateway);

        // Get all the gatewayList where contract is not null
        defaultGatewayShouldBeFound("contract.specified=true");

        // Get all the gatewayList where contract is null
        defaultGatewayShouldNotBeFound("contract.specified=false");
    }

    @Test
    @Transactional
    public void getAllGatewaysBySpringBeanNameIsEqualToSomething() throws Exception {
        // Initialize the database
        gatewayRepository.saveAndFlush(gateway);

        // Get all the gatewayList where springBeanName equals to DEFAULT_SPRING_BEAN_NAME
        defaultGatewayShouldBeFound("springBeanName.equals=" + DEFAULT_SPRING_BEAN_NAME);

        // Get all the gatewayList where springBeanName equals to UPDATED_SPRING_BEAN_NAME
        defaultGatewayShouldNotBeFound("springBeanName.equals=" + UPDATED_SPRING_BEAN_NAME);
    }

    @Test
    @Transactional
    public void getAllGatewaysBySpringBeanNameIsInShouldWork() throws Exception {
        // Initialize the database
        gatewayRepository.saveAndFlush(gateway);

        // Get all the gatewayList where springBeanName in DEFAULT_SPRING_BEAN_NAME or UPDATED_SPRING_BEAN_NAME
        defaultGatewayShouldBeFound("springBeanName.in=" + DEFAULT_SPRING_BEAN_NAME + "," + UPDATED_SPRING_BEAN_NAME);

        // Get all the gatewayList where springBeanName equals to UPDATED_SPRING_BEAN_NAME
        defaultGatewayShouldNotBeFound("springBeanName.in=" + UPDATED_SPRING_BEAN_NAME);
    }

    @Test
    @Transactional
    public void getAllGatewaysBySpringBeanNameIsNullOrNotNull() throws Exception {
        // Initialize the database
        gatewayRepository.saveAndFlush(gateway);

        // Get all the gatewayList where springBeanName is not null
        defaultGatewayShouldBeFound("springBeanName.specified=true");

        // Get all the gatewayList where springBeanName is null
        defaultGatewayShouldNotBeFound("springBeanName.specified=false");
    }

    @Test
    @Transactional
    public void getAllGatewaysByDepositIsEqualToSomething() throws Exception {
        // Initialize the database
        Deposit deposit = DepositResourceIntTest.createEntity(em);
        em.persist(deposit);
        em.flush();
        gateway.setDeposit(deposit);
        deposit.setGateway(gateway);
        gatewayRepository.saveAndFlush(gateway);
        Long depositId = deposit.getId();

        // Get all the gatewayList where deposit equals to depositId
        defaultGatewayShouldBeFound("depositId.equals=" + depositId);

        // Get all the gatewayList where deposit equals to depositId + 1
        defaultGatewayShouldNotBeFound("depositId.equals=" + (depositId + 1));
    }

    /**
     * Executes the search, and checks that the default entity is returned
     */
    private void defaultGatewayShouldBeFound(String filter) throws Exception {
        restGatewayMockMvc
            .perform(get("/api/gateways?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(gateway.getId().intValue())))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME.toString())))
            .andExpect(jsonPath("$.[*].org").value(hasItem(DEFAULT_ORG.toString())))
            .andExpect(jsonPath("$.[*].active").value(hasItem(DEFAULT_ACTIVE.booleanValue())))
            .andExpect(jsonPath("$.[*].mfo").value(hasItem(DEFAULT_MFO.toString())))
            .andExpect(jsonPath("$.[*].taxnr").value(hasItem(DEFAULT_TAXNR.intValue())))
            .andExpect(jsonPath("$.[*].ban").value(hasItem(DEFAULT_BAN.toString())))
            .andExpect(jsonPath("$.[*].contract").value(hasItem(DEFAULT_CONTRACT.toString())))
            .andExpect(jsonPath("$.[*].springBeanName").value(hasItem(DEFAULT_SPRING_BEAN_NAME.toString())));

        // Check, that the count call also returns 1
        restGatewayMockMvc
            .perform(get("/api/gateways/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(content().string("1"));
    }

    /**
     * Executes the search, and checks that the default entity is not returned
     */
    private void defaultGatewayShouldNotBeFound(String filter) throws Exception {
        restGatewayMockMvc
            .perform(get("/api/gateways?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty());

        // Check, that the count call also returns 0
        restGatewayMockMvc
            .perform(get("/api/gateways/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(content().string("0"));
    }

    @Test
    @Transactional
    public void getNonExistingGateway() throws Exception {
        // Get the gateway
        restGatewayMockMvc.perform(get("/api/gateways/{id}", Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updateGateway() throws Exception {
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
                put("/api/gateways").contentType(TestUtil.APPLICATION_JSON_UTF8).content(TestUtil.convertObjectToJsonBytes(gatewayDTO))
            )
            .andExpect(status().isOk());

        // Validate the Gateway in the database
        List<Gateway> gatewayList = gatewayRepository.findAll();
        assertThat(gatewayList).hasSize(databaseSizeBeforeUpdate);
        Gateway testGateway = gatewayList.get(gatewayList.size() - 1);
        assertThat(testGateway.getName()).isEqualTo(UPDATED_NAME);
        assertThat(testGateway.getOrg()).isEqualTo(UPDATED_ORG);
        assertThat(testGateway.isActive()).isEqualTo(UPDATED_ACTIVE);
        assertThat(testGateway.getMfo()).isEqualTo(UPDATED_MFO);
        assertThat(testGateway.getTaxnr()).isEqualTo(UPDATED_TAXNR);
        assertThat(testGateway.getBan()).isEqualTo(UPDATED_BAN);
        assertThat(testGateway.getContract()).isEqualTo(UPDATED_CONTRACT);
        assertThat(testGateway.getSpringBeanName()).isEqualTo(UPDATED_SPRING_BEAN_NAME);
    }

    @Test
    @Transactional
    public void updateNonExistingGateway() throws Exception {
        int databaseSizeBeforeUpdate = gatewayRepository.findAll().size();

        // Create the Gateway
        GatewayDTO gatewayDTO = gatewayMapper.toDto(gateway);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restGatewayMockMvc
            .perform(
                put("/api/gateways").contentType(TestUtil.APPLICATION_JSON_UTF8).content(TestUtil.convertObjectToJsonBytes(gatewayDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Gateway in the database
        List<Gateway> gatewayList = gatewayRepository.findAll();
        assertThat(gatewayList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    public void deleteGateway() throws Exception {
        // Initialize the database
        gatewayRepository.saveAndFlush(gateway);

        int databaseSizeBeforeDelete = gatewayRepository.findAll().size();

        // Get the gateway
        restGatewayMockMvc
            .perform(delete("/api/gateways/{id}", gateway.getId()).accept(TestUtil.APPLICATION_JSON_UTF8))
            .andExpect(status().isOk());

        // Validate the database is empty
        List<Gateway> gatewayList = gatewayRepository.findAll();
        assertThat(gatewayList).hasSize(databaseSizeBeforeDelete - 1);
    }

    @Test
    @Transactional
    public void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Gateway.class);
        Gateway gateway1 = new Gateway();
        gateway1.setId(1L);
        Gateway gateway2 = new Gateway();
        gateway2.setId(gateway1.getId());
        assertThat(gateway1).isEqualTo(gateway2);
        gateway2.setId(2L);
        assertThat(gateway1).isNotEqualTo(gateway2);
        gateway1.setId(null);
        assertThat(gateway1).isNotEqualTo(gateway2);
    }

    @Test
    @Transactional
    public void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(GatewayDTO.class);
        GatewayDTO gatewayDTO1 = new GatewayDTO();
        gatewayDTO1.setId(1L);
        GatewayDTO gatewayDTO2 = new GatewayDTO();
        assertThat(gatewayDTO1).isNotEqualTo(gatewayDTO2);
        gatewayDTO2.setId(gatewayDTO1.getId());
        assertThat(gatewayDTO1).isEqualTo(gatewayDTO2);
        gatewayDTO2.setId(2L);
        assertThat(gatewayDTO1).isNotEqualTo(gatewayDTO2);
        gatewayDTO1.setId(null);
        assertThat(gatewayDTO1).isNotEqualTo(gatewayDTO2);
    }
    //    @Test
    //    @Transactional
    //    public void testEntityFromId() {
    //        assertThat(gatewayMapper.fromId(42L).getId()).isEqualTo(42);
    //        assertThat(gatewayMapper.fromId(null)).isNull();
    //    }
}
