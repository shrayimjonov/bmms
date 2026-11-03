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
import uz.paykap.domain.VirtualAccount;
import uz.paykap.domain.enumeration.ModeType;
import uz.paykap.repository.VirtualAccountRepository;
import uz.paykap.service.criteria.VirtualAccountCriteria;
import uz.paykap.service.dto.VirtualAccountDTO;
import uz.paykap.service.mapper.VirtualAccountMapper;

/**
 * Integration tests for the {@link VirtualAccountResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class VirtualAccountResourceIT {

    private static final String DEFAULT_NAME = "AAAAAAAAAA";
    private static final String UPDATED_NAME = "BBBBBBBBBB";

    private static final String DEFAULT_ORG_NAME = "AAAAAAAAAA";
    private static final String UPDATED_ORG_NAME = "BBBBBBBBBB";

    private static final Boolean DEFAULT_ACTIVE = false;
    private static final Boolean UPDATED_ACTIVE = true;

    private static final String DEFAULT_INN = "AAAAAAAAAA";
    private static final String UPDATED_INN = "BBBBBBBBBB";

    private static final Long DEFAULT_UID = 1L;
    private static final Long UPDATED_UID = 2L;
    private static final Long SMALLER_UID = 1L - 1L;

    private static final ModeType DEFAULT_MODE = ModeType.TEST;
    private static final ModeType UPDATED_MODE = ModeType.DEV;

    private static final String ENTITY_API_URL = "/api/virtual-accounts";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong count = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private VirtualAccountRepository virtualAccountRepository;

    @Autowired
    private VirtualAccountMapper virtualAccountMapper;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restVirtualAccountMockMvc;

    private VirtualAccount virtualAccount;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static VirtualAccount createEntity(EntityManager em) {
        VirtualAccount virtualAccount = new VirtualAccount()
            .name(DEFAULT_NAME)
            .orgName(DEFAULT_ORG_NAME)
            .active(DEFAULT_ACTIVE)
            .inn(DEFAULT_INN)
            .uid(DEFAULT_UID)
            .mode(DEFAULT_MODE);
        return virtualAccount;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static VirtualAccount createUpdatedEntity(EntityManager em) {
        VirtualAccount virtualAccount = new VirtualAccount()
            .name(UPDATED_NAME)
            .orgName(UPDATED_ORG_NAME)
            .active(UPDATED_ACTIVE)
            .inn(UPDATED_INN)
            .uid(UPDATED_UID)
            .mode(UPDATED_MODE);
        return virtualAccount;
    }

    @BeforeEach
    public void initTest() {
        virtualAccount = createEntity(em);
    }

    @Test
    @Transactional
    void createVirtualAccount() throws Exception {
        int databaseSizeBeforeCreate = virtualAccountRepository.findAll().size();
        // Create the VirtualAccount
        VirtualAccountDTO virtualAccountDTO = virtualAccountMapper.toDto(virtualAccount);
        restVirtualAccountMockMvc
            .perform(
                post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(virtualAccountDTO))
            )
            .andExpect(status().isCreated());

        // Validate the VirtualAccount in the database
        List<VirtualAccount> virtualAccountList = virtualAccountRepository.findAll();
        assertThat(virtualAccountList).hasSize(databaseSizeBeforeCreate + 1);
        VirtualAccount testVirtualAccount = virtualAccountList.get(virtualAccountList.size() - 1);
        assertThat(testVirtualAccount.getName()).isEqualTo(DEFAULT_NAME);
        assertThat(testVirtualAccount.getOrgName()).isEqualTo(DEFAULT_ORG_NAME);
        assertThat(testVirtualAccount.getActive()).isEqualTo(DEFAULT_ACTIVE);
        assertThat(testVirtualAccount.getInn()).isEqualTo(DEFAULT_INN);
        assertThat(testVirtualAccount.getUid()).isEqualTo(DEFAULT_UID);
        assertThat(testVirtualAccount.getMode()).isEqualTo(DEFAULT_MODE);
    }

    @Test
    @Transactional
    void createVirtualAccountWithExistingId() throws Exception {
        // Create the VirtualAccount with an existing ID
        virtualAccount.setId(1L);
        VirtualAccountDTO virtualAccountDTO = virtualAccountMapper.toDto(virtualAccount);

        int databaseSizeBeforeCreate = virtualAccountRepository.findAll().size();

        // An entity with an existing ID cannot be created, so this API call must fail
        restVirtualAccountMockMvc
            .perform(
                post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(virtualAccountDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the VirtualAccount in the database
        List<VirtualAccount> virtualAccountList = virtualAccountRepository.findAll();
        assertThat(virtualAccountList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkNameIsRequired() throws Exception {
        int databaseSizeBeforeTest = virtualAccountRepository.findAll().size();
        // set the field null
        virtualAccount.setName(null);

        // Create the VirtualAccount, which fails.
        VirtualAccountDTO virtualAccountDTO = virtualAccountMapper.toDto(virtualAccount);

        restVirtualAccountMockMvc
            .perform(
                post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(virtualAccountDTO))
            )
            .andExpect(status().isBadRequest());

        List<VirtualAccount> virtualAccountList = virtualAccountRepository.findAll();
        assertThat(virtualAccountList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkOrgNameIsRequired() throws Exception {
        int databaseSizeBeforeTest = virtualAccountRepository.findAll().size();
        // set the field null
        virtualAccount.setOrgName(null);

        // Create the VirtualAccount, which fails.
        VirtualAccountDTO virtualAccountDTO = virtualAccountMapper.toDto(virtualAccount);

        restVirtualAccountMockMvc
            .perform(
                post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(virtualAccountDTO))
            )
            .andExpect(status().isBadRequest());

        List<VirtualAccount> virtualAccountList = virtualAccountRepository.findAll();
        assertThat(virtualAccountList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkActiveIsRequired() throws Exception {
        int databaseSizeBeforeTest = virtualAccountRepository.findAll().size();
        // set the field null
        virtualAccount.setActive(null);

        // Create the VirtualAccount, which fails.
        VirtualAccountDTO virtualAccountDTO = virtualAccountMapper.toDto(virtualAccount);

        restVirtualAccountMockMvc
            .perform(
                post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(virtualAccountDTO))
            )
            .andExpect(status().isBadRequest());

        List<VirtualAccount> virtualAccountList = virtualAccountRepository.findAll();
        assertThat(virtualAccountList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkInnIsRequired() throws Exception {
        int databaseSizeBeforeTest = virtualAccountRepository.findAll().size();
        // set the field null
        virtualAccount.setInn(null);

        // Create the VirtualAccount, which fails.
        VirtualAccountDTO virtualAccountDTO = virtualAccountMapper.toDto(virtualAccount);

        restVirtualAccountMockMvc
            .perform(
                post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(virtualAccountDTO))
            )
            .andExpect(status().isBadRequest());

        List<VirtualAccount> virtualAccountList = virtualAccountRepository.findAll();
        assertThat(virtualAccountList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllVirtualAccounts() throws Exception {
        // Initialize the database
        virtualAccountRepository.saveAndFlush(virtualAccount);

        // Get all the virtualAccountList
        restVirtualAccountMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(virtualAccount.getId().intValue())))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME)))
            .andExpect(jsonPath("$.[*].orgName").value(hasItem(DEFAULT_ORG_NAME)))
            .andExpect(jsonPath("$.[*].active").value(hasItem(DEFAULT_ACTIVE.booleanValue())))
            .andExpect(jsonPath("$.[*].inn").value(hasItem(DEFAULT_INN)))
            .andExpect(jsonPath("$.[*].uid").value(hasItem(DEFAULT_UID.intValue())))
            .andExpect(jsonPath("$.[*].mode").value(hasItem(DEFAULT_MODE.toString())));
    }

    @Test
    @Transactional
    void getVirtualAccount() throws Exception {
        // Initialize the database
        virtualAccountRepository.saveAndFlush(virtualAccount);

        // Get the virtualAccount
        restVirtualAccountMockMvc
            .perform(get(ENTITY_API_URL_ID, virtualAccount.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(virtualAccount.getId().intValue()))
            .andExpect(jsonPath("$.name").value(DEFAULT_NAME))
            .andExpect(jsonPath("$.orgName").value(DEFAULT_ORG_NAME))
            .andExpect(jsonPath("$.active").value(DEFAULT_ACTIVE.booleanValue()))
            .andExpect(jsonPath("$.inn").value(DEFAULT_INN))
            .andExpect(jsonPath("$.uid").value(DEFAULT_UID.intValue()))
            .andExpect(jsonPath("$.mode").value(DEFAULT_MODE.toString()));
    }

    @Test
    @Transactional
    void getVirtualAccountsByIdFiltering() throws Exception {
        // Initialize the database
        virtualAccountRepository.saveAndFlush(virtualAccount);

        Long id = virtualAccount.getId();

        defaultVirtualAccountShouldBeFound("id.equals=" + id);
        defaultVirtualAccountShouldNotBeFound("id.notEquals=" + id);

        defaultVirtualAccountShouldBeFound("id.greaterThanOrEqual=" + id);
        defaultVirtualAccountShouldNotBeFound("id.greaterThan=" + id);

        defaultVirtualAccountShouldBeFound("id.lessThanOrEqual=" + id);
        defaultVirtualAccountShouldNotBeFound("id.lessThan=" + id);
    }

    @Test
    @Transactional
    void getAllVirtualAccountsByNameIsEqualToSomething() throws Exception {
        // Initialize the database
        virtualAccountRepository.saveAndFlush(virtualAccount);

        // Get all the virtualAccountList where name equals to DEFAULT_NAME
        defaultVirtualAccountShouldBeFound("name.equals=" + DEFAULT_NAME);

        // Get all the virtualAccountList where name equals to UPDATED_NAME
        defaultVirtualAccountShouldNotBeFound("name.equals=" + UPDATED_NAME);
    }

    @Test
    @Transactional
    void getAllVirtualAccountsByNameIsNotEqualToSomething() throws Exception {
        // Initialize the database
        virtualAccountRepository.saveAndFlush(virtualAccount);

        // Get all the virtualAccountList where name not equals to DEFAULT_NAME
        defaultVirtualAccountShouldNotBeFound("name.notEquals=" + DEFAULT_NAME);

        // Get all the virtualAccountList where name not equals to UPDATED_NAME
        defaultVirtualAccountShouldBeFound("name.notEquals=" + UPDATED_NAME);
    }

    @Test
    @Transactional
    void getAllVirtualAccountsByNameIsInShouldWork() throws Exception {
        // Initialize the database
        virtualAccountRepository.saveAndFlush(virtualAccount);

        // Get all the virtualAccountList where name in DEFAULT_NAME or UPDATED_NAME
        defaultVirtualAccountShouldBeFound("name.in=" + DEFAULT_NAME + "," + UPDATED_NAME);

        // Get all the virtualAccountList where name equals to UPDATED_NAME
        defaultVirtualAccountShouldNotBeFound("name.in=" + UPDATED_NAME);
    }

    @Test
    @Transactional
    void getAllVirtualAccountsByNameIsNullOrNotNull() throws Exception {
        // Initialize the database
        virtualAccountRepository.saveAndFlush(virtualAccount);

        // Get all the virtualAccountList where name is not null
        defaultVirtualAccountShouldBeFound("name.specified=true");

        // Get all the virtualAccountList where name is null
        defaultVirtualAccountShouldNotBeFound("name.specified=false");
    }

    @Test
    @Transactional
    void getAllVirtualAccountsByNameContainsSomething() throws Exception {
        // Initialize the database
        virtualAccountRepository.saveAndFlush(virtualAccount);

        // Get all the virtualAccountList where name contains DEFAULT_NAME
        defaultVirtualAccountShouldBeFound("name.contains=" + DEFAULT_NAME);

        // Get all the virtualAccountList where name contains UPDATED_NAME
        defaultVirtualAccountShouldNotBeFound("name.contains=" + UPDATED_NAME);
    }

    @Test
    @Transactional
    void getAllVirtualAccountsByNameNotContainsSomething() throws Exception {
        // Initialize the database
        virtualAccountRepository.saveAndFlush(virtualAccount);

        // Get all the virtualAccountList where name does not contain DEFAULT_NAME
        defaultVirtualAccountShouldNotBeFound("name.doesNotContain=" + DEFAULT_NAME);

        // Get all the virtualAccountList where name does not contain UPDATED_NAME
        defaultVirtualAccountShouldBeFound("name.doesNotContain=" + UPDATED_NAME);
    }

    @Test
    @Transactional
    void getAllVirtualAccountsByOrgNameIsEqualToSomething() throws Exception {
        // Initialize the database
        virtualAccountRepository.saveAndFlush(virtualAccount);

        // Get all the virtualAccountList where orgName equals to DEFAULT_ORG_NAME
        defaultVirtualAccountShouldBeFound("orgName.equals=" + DEFAULT_ORG_NAME);

        // Get all the virtualAccountList where orgName equals to UPDATED_ORG_NAME
        defaultVirtualAccountShouldNotBeFound("orgName.equals=" + UPDATED_ORG_NAME);
    }

    @Test
    @Transactional
    void getAllVirtualAccountsByOrgNameIsNotEqualToSomething() throws Exception {
        // Initialize the database
        virtualAccountRepository.saveAndFlush(virtualAccount);

        // Get all the virtualAccountList where orgName not equals to DEFAULT_ORG_NAME
        defaultVirtualAccountShouldNotBeFound("orgName.notEquals=" + DEFAULT_ORG_NAME);

        // Get all the virtualAccountList where orgName not equals to UPDATED_ORG_NAME
        defaultVirtualAccountShouldBeFound("orgName.notEquals=" + UPDATED_ORG_NAME);
    }

    @Test
    @Transactional
    void getAllVirtualAccountsByOrgNameIsInShouldWork() throws Exception {
        // Initialize the database
        virtualAccountRepository.saveAndFlush(virtualAccount);

        // Get all the virtualAccountList where orgName in DEFAULT_ORG_NAME or UPDATED_ORG_NAME
        defaultVirtualAccountShouldBeFound("orgName.in=" + DEFAULT_ORG_NAME + "," + UPDATED_ORG_NAME);

        // Get all the virtualAccountList where orgName equals to UPDATED_ORG_NAME
        defaultVirtualAccountShouldNotBeFound("orgName.in=" + UPDATED_ORG_NAME);
    }

    @Test
    @Transactional
    void getAllVirtualAccountsByOrgNameIsNullOrNotNull() throws Exception {
        // Initialize the database
        virtualAccountRepository.saveAndFlush(virtualAccount);

        // Get all the virtualAccountList where orgName is not null
        defaultVirtualAccountShouldBeFound("orgName.specified=true");

        // Get all the virtualAccountList where orgName is null
        defaultVirtualAccountShouldNotBeFound("orgName.specified=false");
    }

    @Test
    @Transactional
    void getAllVirtualAccountsByOrgNameContainsSomething() throws Exception {
        // Initialize the database
        virtualAccountRepository.saveAndFlush(virtualAccount);

        // Get all the virtualAccountList where orgName contains DEFAULT_ORG_NAME
        defaultVirtualAccountShouldBeFound("orgName.contains=" + DEFAULT_ORG_NAME);

        // Get all the virtualAccountList where orgName contains UPDATED_ORG_NAME
        defaultVirtualAccountShouldNotBeFound("orgName.contains=" + UPDATED_ORG_NAME);
    }

    @Test
    @Transactional
    void getAllVirtualAccountsByOrgNameNotContainsSomething() throws Exception {
        // Initialize the database
        virtualAccountRepository.saveAndFlush(virtualAccount);

        // Get all the virtualAccountList where orgName does not contain DEFAULT_ORG_NAME
        defaultVirtualAccountShouldNotBeFound("orgName.doesNotContain=" + DEFAULT_ORG_NAME);

        // Get all the virtualAccountList where orgName does not contain UPDATED_ORG_NAME
        defaultVirtualAccountShouldBeFound("orgName.doesNotContain=" + UPDATED_ORG_NAME);
    }

    @Test
    @Transactional
    void getAllVirtualAccountsByActiveIsEqualToSomething() throws Exception {
        // Initialize the database
        virtualAccountRepository.saveAndFlush(virtualAccount);

        // Get all the virtualAccountList where active equals to DEFAULT_ACTIVE
        defaultVirtualAccountShouldBeFound("active.equals=" + DEFAULT_ACTIVE);

        // Get all the virtualAccountList where active equals to UPDATED_ACTIVE
        defaultVirtualAccountShouldNotBeFound("active.equals=" + UPDATED_ACTIVE);
    }

    @Test
    @Transactional
    void getAllVirtualAccountsByActiveIsNotEqualToSomething() throws Exception {
        // Initialize the database
        virtualAccountRepository.saveAndFlush(virtualAccount);

        // Get all the virtualAccountList where active not equals to DEFAULT_ACTIVE
        defaultVirtualAccountShouldNotBeFound("active.notEquals=" + DEFAULT_ACTIVE);

        // Get all the virtualAccountList where active not equals to UPDATED_ACTIVE
        defaultVirtualAccountShouldBeFound("active.notEquals=" + UPDATED_ACTIVE);
    }

    @Test
    @Transactional
    void getAllVirtualAccountsByActiveIsInShouldWork() throws Exception {
        // Initialize the database
        virtualAccountRepository.saveAndFlush(virtualAccount);

        // Get all the virtualAccountList where active in DEFAULT_ACTIVE or UPDATED_ACTIVE
        defaultVirtualAccountShouldBeFound("active.in=" + DEFAULT_ACTIVE + "," + UPDATED_ACTIVE);

        // Get all the virtualAccountList where active equals to UPDATED_ACTIVE
        defaultVirtualAccountShouldNotBeFound("active.in=" + UPDATED_ACTIVE);
    }

    @Test
    @Transactional
    void getAllVirtualAccountsByActiveIsNullOrNotNull() throws Exception {
        // Initialize the database
        virtualAccountRepository.saveAndFlush(virtualAccount);

        // Get all the virtualAccountList where active is not null
        defaultVirtualAccountShouldBeFound("active.specified=true");

        // Get all the virtualAccountList where active is null
        defaultVirtualAccountShouldNotBeFound("active.specified=false");
    }

    @Test
    @Transactional
    void getAllVirtualAccountsByInnIsEqualToSomething() throws Exception {
        // Initialize the database
        virtualAccountRepository.saveAndFlush(virtualAccount);

        // Get all the virtualAccountList where inn equals to DEFAULT_INN
        defaultVirtualAccountShouldBeFound("inn.equals=" + DEFAULT_INN);

        // Get all the virtualAccountList where inn equals to UPDATED_INN
        defaultVirtualAccountShouldNotBeFound("inn.equals=" + UPDATED_INN);
    }

    @Test
    @Transactional
    void getAllVirtualAccountsByInnIsNotEqualToSomething() throws Exception {
        // Initialize the database
        virtualAccountRepository.saveAndFlush(virtualAccount);

        // Get all the virtualAccountList where inn not equals to DEFAULT_INN
        defaultVirtualAccountShouldNotBeFound("inn.notEquals=" + DEFAULT_INN);

        // Get all the virtualAccountList where inn not equals to UPDATED_INN
        defaultVirtualAccountShouldBeFound("inn.notEquals=" + UPDATED_INN);
    }

    @Test
    @Transactional
    void getAllVirtualAccountsByInnIsInShouldWork() throws Exception {
        // Initialize the database
        virtualAccountRepository.saveAndFlush(virtualAccount);

        // Get all the virtualAccountList where inn in DEFAULT_INN or UPDATED_INN
        defaultVirtualAccountShouldBeFound("inn.in=" + DEFAULT_INN + "," + UPDATED_INN);

        // Get all the virtualAccountList where inn equals to UPDATED_INN
        defaultVirtualAccountShouldNotBeFound("inn.in=" + UPDATED_INN);
    }

    @Test
    @Transactional
    void getAllVirtualAccountsByInnIsNullOrNotNull() throws Exception {
        // Initialize the database
        virtualAccountRepository.saveAndFlush(virtualAccount);

        // Get all the virtualAccountList where inn is not null
        defaultVirtualAccountShouldBeFound("inn.specified=true");

        // Get all the virtualAccountList where inn is null
        defaultVirtualAccountShouldNotBeFound("inn.specified=false");
    }

    @Test
    @Transactional
    void getAllVirtualAccountsByInnContainsSomething() throws Exception {
        // Initialize the database
        virtualAccountRepository.saveAndFlush(virtualAccount);

        // Get all the virtualAccountList where inn contains DEFAULT_INN
        defaultVirtualAccountShouldBeFound("inn.contains=" + DEFAULT_INN);

        // Get all the virtualAccountList where inn contains UPDATED_INN
        defaultVirtualAccountShouldNotBeFound("inn.contains=" + UPDATED_INN);
    }

    @Test
    @Transactional
    void getAllVirtualAccountsByInnNotContainsSomething() throws Exception {
        // Initialize the database
        virtualAccountRepository.saveAndFlush(virtualAccount);

        // Get all the virtualAccountList where inn does not contain DEFAULT_INN
        defaultVirtualAccountShouldNotBeFound("inn.doesNotContain=" + DEFAULT_INN);

        // Get all the virtualAccountList where inn does not contain UPDATED_INN
        defaultVirtualAccountShouldBeFound("inn.doesNotContain=" + UPDATED_INN);
    }

    @Test
    @Transactional
    void getAllVirtualAccountsByUidIsEqualToSomething() throws Exception {
        // Initialize the database
        virtualAccountRepository.saveAndFlush(virtualAccount);

        // Get all the virtualAccountList where uid equals to DEFAULT_UID
        defaultVirtualAccountShouldBeFound("uid.equals=" + DEFAULT_UID);

        // Get all the virtualAccountList where uid equals to UPDATED_UID
        defaultVirtualAccountShouldNotBeFound("uid.equals=" + UPDATED_UID);
    }

    @Test
    @Transactional
    void getAllVirtualAccountsByUidIsNotEqualToSomething() throws Exception {
        // Initialize the database
        virtualAccountRepository.saveAndFlush(virtualAccount);

        // Get all the virtualAccountList where uid not equals to DEFAULT_UID
        defaultVirtualAccountShouldNotBeFound("uid.notEquals=" + DEFAULT_UID);

        // Get all the virtualAccountList where uid not equals to UPDATED_UID
        defaultVirtualAccountShouldBeFound("uid.notEquals=" + UPDATED_UID);
    }

    @Test
    @Transactional
    void getAllVirtualAccountsByUidIsInShouldWork() throws Exception {
        // Initialize the database
        virtualAccountRepository.saveAndFlush(virtualAccount);

        // Get all the virtualAccountList where uid in DEFAULT_UID or UPDATED_UID
        defaultVirtualAccountShouldBeFound("uid.in=" + DEFAULT_UID + "," + UPDATED_UID);

        // Get all the virtualAccountList where uid equals to UPDATED_UID
        defaultVirtualAccountShouldNotBeFound("uid.in=" + UPDATED_UID);
    }

    @Test
    @Transactional
    void getAllVirtualAccountsByUidIsNullOrNotNull() throws Exception {
        // Initialize the database
        virtualAccountRepository.saveAndFlush(virtualAccount);

        // Get all the virtualAccountList where uid is not null
        defaultVirtualAccountShouldBeFound("uid.specified=true");

        // Get all the virtualAccountList where uid is null
        defaultVirtualAccountShouldNotBeFound("uid.specified=false");
    }

    @Test
    @Transactional
    void getAllVirtualAccountsByUidIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        virtualAccountRepository.saveAndFlush(virtualAccount);

        // Get all the virtualAccountList where uid is greater than or equal to DEFAULT_UID
        defaultVirtualAccountShouldBeFound("uid.greaterThanOrEqual=" + DEFAULT_UID);

        // Get all the virtualAccountList where uid is greater than or equal to UPDATED_UID
        defaultVirtualAccountShouldNotBeFound("uid.greaterThanOrEqual=" + UPDATED_UID);
    }

    @Test
    @Transactional
    void getAllVirtualAccountsByUidIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        virtualAccountRepository.saveAndFlush(virtualAccount);

        // Get all the virtualAccountList where uid is less than or equal to DEFAULT_UID
        defaultVirtualAccountShouldBeFound("uid.lessThanOrEqual=" + DEFAULT_UID);

        // Get all the virtualAccountList where uid is less than or equal to SMALLER_UID
        defaultVirtualAccountShouldNotBeFound("uid.lessThanOrEqual=" + SMALLER_UID);
    }

    @Test
    @Transactional
    void getAllVirtualAccountsByUidIsLessThanSomething() throws Exception {
        // Initialize the database
        virtualAccountRepository.saveAndFlush(virtualAccount);

        // Get all the virtualAccountList where uid is less than DEFAULT_UID
        defaultVirtualAccountShouldNotBeFound("uid.lessThan=" + DEFAULT_UID);

        // Get all the virtualAccountList where uid is less than UPDATED_UID
        defaultVirtualAccountShouldBeFound("uid.lessThan=" + UPDATED_UID);
    }

    @Test
    @Transactional
    void getAllVirtualAccountsByUidIsGreaterThanSomething() throws Exception {
        // Initialize the database
        virtualAccountRepository.saveAndFlush(virtualAccount);

        // Get all the virtualAccountList where uid is greater than DEFAULT_UID
        defaultVirtualAccountShouldNotBeFound("uid.greaterThan=" + DEFAULT_UID);

        // Get all the virtualAccountList where uid is greater than SMALLER_UID
        defaultVirtualAccountShouldBeFound("uid.greaterThan=" + SMALLER_UID);
    }

    @Test
    @Transactional
    void getAllVirtualAccountsByModeIsEqualToSomething() throws Exception {
        // Initialize the database
        virtualAccountRepository.saveAndFlush(virtualAccount);

        // Get all the virtualAccountList where mode equals to DEFAULT_MODE
        defaultVirtualAccountShouldBeFound("mode.equals=" + DEFAULT_MODE);

        // Get all the virtualAccountList where mode equals to UPDATED_MODE
        defaultVirtualAccountShouldNotBeFound("mode.equals=" + UPDATED_MODE);
    }

    @Test
    @Transactional
    void getAllVirtualAccountsByModeIsNotEqualToSomething() throws Exception {
        // Initialize the database
        virtualAccountRepository.saveAndFlush(virtualAccount);

        // Get all the virtualAccountList where mode not equals to DEFAULT_MODE
        defaultVirtualAccountShouldNotBeFound("mode.notEquals=" + DEFAULT_MODE);

        // Get all the virtualAccountList where mode not equals to UPDATED_MODE
        defaultVirtualAccountShouldBeFound("mode.notEquals=" + UPDATED_MODE);
    }

    @Test
    @Transactional
    void getAllVirtualAccountsByModeIsInShouldWork() throws Exception {
        // Initialize the database
        virtualAccountRepository.saveAndFlush(virtualAccount);

        // Get all the virtualAccountList where mode in DEFAULT_MODE or UPDATED_MODE
        defaultVirtualAccountShouldBeFound("mode.in=" + DEFAULT_MODE + "," + UPDATED_MODE);

        // Get all the virtualAccountList where mode equals to UPDATED_MODE
        defaultVirtualAccountShouldNotBeFound("mode.in=" + UPDATED_MODE);
    }

    @Test
    @Transactional
    void getAllVirtualAccountsByModeIsNullOrNotNull() throws Exception {
        // Initialize the database
        virtualAccountRepository.saveAndFlush(virtualAccount);

        // Get all the virtualAccountList where mode is not null
        defaultVirtualAccountShouldBeFound("mode.specified=true");

        // Get all the virtualAccountList where mode is null
        defaultVirtualAccountShouldNotBeFound("mode.specified=false");
    }

    @Test
    @Transactional
    void getAllVirtualAccountsByDepositIsEqualToSomething() throws Exception {
        // Initialize the database
        virtualAccountRepository.saveAndFlush(virtualAccount);
        Deposit deposit = DepositResourceIT.createEntity(em);
        em.persist(deposit);
        em.flush();
        virtualAccount.setDeposit(deposit);
        deposit.setVirtualAccount(virtualAccount);
        virtualAccountRepository.saveAndFlush(virtualAccount);
        Long depositId = deposit.getId();

        // Get all the virtualAccountList where deposit equals to depositId
        defaultVirtualAccountShouldBeFound("depositId.equals=" + depositId);

        // Get all the virtualAccountList where deposit equals to (depositId + 1)
        defaultVirtualAccountShouldNotBeFound("depositId.equals=" + (depositId + 1));
    }

    /**
     * Executes the search, and checks that the default entity is returned.
     */
    private void defaultVirtualAccountShouldBeFound(String filter) throws Exception {
        restVirtualAccountMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(virtualAccount.getId().intValue())))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME)))
            .andExpect(jsonPath("$.[*].orgName").value(hasItem(DEFAULT_ORG_NAME)))
            .andExpect(jsonPath("$.[*].active").value(hasItem(DEFAULT_ACTIVE.booleanValue())))
            .andExpect(jsonPath("$.[*].inn").value(hasItem(DEFAULT_INN)))
            .andExpect(jsonPath("$.[*].uid").value(hasItem(DEFAULT_UID.intValue())))
            .andExpect(jsonPath("$.[*].mode").value(hasItem(DEFAULT_MODE.toString())));

        // Check, that the count call also returns 1
        restVirtualAccountMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("1"));
    }

    /**
     * Executes the search, and checks that the default entity is not returned.
     */
    private void defaultVirtualAccountShouldNotBeFound(String filter) throws Exception {
        restVirtualAccountMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty());

        // Check, that the count call also returns 0
        restVirtualAccountMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("0"));
    }

    @Test
    @Transactional
    void getNonExistingVirtualAccount() throws Exception {
        // Get the virtualAccount
        restVirtualAccountMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putNewVirtualAccount() throws Exception {
        // Initialize the database
        virtualAccountRepository.saveAndFlush(virtualAccount);

        int databaseSizeBeforeUpdate = virtualAccountRepository.findAll().size();

        // Update the virtualAccount
        VirtualAccount updatedVirtualAccount = virtualAccountRepository.findById(virtualAccount.getId()).get();
        // Disconnect from session so that the updates on updatedVirtualAccount are not directly saved in db
        em.detach(updatedVirtualAccount);
        updatedVirtualAccount
            .name(UPDATED_NAME)
            .orgName(UPDATED_ORG_NAME)
            .active(UPDATED_ACTIVE)
            .inn(UPDATED_INN)
            .uid(UPDATED_UID)
            .mode(UPDATED_MODE);
        VirtualAccountDTO virtualAccountDTO = virtualAccountMapper.toDto(updatedVirtualAccount);

        restVirtualAccountMockMvc
            .perform(
                put(ENTITY_API_URL_ID, virtualAccountDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(virtualAccountDTO))
            )
            .andExpect(status().isOk());

        // Validate the VirtualAccount in the database
        List<VirtualAccount> virtualAccountList = virtualAccountRepository.findAll();
        assertThat(virtualAccountList).hasSize(databaseSizeBeforeUpdate);
        VirtualAccount testVirtualAccount = virtualAccountList.get(virtualAccountList.size() - 1);
        assertThat(testVirtualAccount.getName()).isEqualTo(UPDATED_NAME);
        assertThat(testVirtualAccount.getOrgName()).isEqualTo(UPDATED_ORG_NAME);
        assertThat(testVirtualAccount.getActive()).isEqualTo(UPDATED_ACTIVE);
        assertThat(testVirtualAccount.getInn()).isEqualTo(UPDATED_INN);
        assertThat(testVirtualAccount.getUid()).isEqualTo(UPDATED_UID);
        assertThat(testVirtualAccount.getMode()).isEqualTo(UPDATED_MODE);
    }

    @Test
    @Transactional
    void putNonExistingVirtualAccount() throws Exception {
        int databaseSizeBeforeUpdate = virtualAccountRepository.findAll().size();
        virtualAccount.setId(count.incrementAndGet());

        // Create the VirtualAccount
        VirtualAccountDTO virtualAccountDTO = virtualAccountMapper.toDto(virtualAccount);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restVirtualAccountMockMvc
            .perform(
                put(ENTITY_API_URL_ID, virtualAccountDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(virtualAccountDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the VirtualAccount in the database
        List<VirtualAccount> virtualAccountList = virtualAccountRepository.findAll();
        assertThat(virtualAccountList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchVirtualAccount() throws Exception {
        int databaseSizeBeforeUpdate = virtualAccountRepository.findAll().size();
        virtualAccount.setId(count.incrementAndGet());

        // Create the VirtualAccount
        VirtualAccountDTO virtualAccountDTO = virtualAccountMapper.toDto(virtualAccount);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restVirtualAccountMockMvc
            .perform(
                put(ENTITY_API_URL_ID, count.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(virtualAccountDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the VirtualAccount in the database
        List<VirtualAccount> virtualAccountList = virtualAccountRepository.findAll();
        assertThat(virtualAccountList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamVirtualAccount() throws Exception {
        int databaseSizeBeforeUpdate = virtualAccountRepository.findAll().size();
        virtualAccount.setId(count.incrementAndGet());

        // Create the VirtualAccount
        VirtualAccountDTO virtualAccountDTO = virtualAccountMapper.toDto(virtualAccount);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restVirtualAccountMockMvc
            .perform(
                put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(virtualAccountDTO))
            )
            .andExpect(status().isMethodNotAllowed());

        // Validate the VirtualAccount in the database
        List<VirtualAccount> virtualAccountList = virtualAccountRepository.findAll();
        assertThat(virtualAccountList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateVirtualAccountWithPatch() throws Exception {
        // Initialize the database
        virtualAccountRepository.saveAndFlush(virtualAccount);

        int databaseSizeBeforeUpdate = virtualAccountRepository.findAll().size();

        // Update the virtualAccount using partial update
        VirtualAccount partialUpdatedVirtualAccount = new VirtualAccount();
        partialUpdatedVirtualAccount.setId(virtualAccount.getId());

        partialUpdatedVirtualAccount.orgName(UPDATED_ORG_NAME);

        restVirtualAccountMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedVirtualAccount.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedVirtualAccount))
            )
            .andExpect(status().isOk());

        // Validate the VirtualAccount in the database
        List<VirtualAccount> virtualAccountList = virtualAccountRepository.findAll();
        assertThat(virtualAccountList).hasSize(databaseSizeBeforeUpdate);
        VirtualAccount testVirtualAccount = virtualAccountList.get(virtualAccountList.size() - 1);
        assertThat(testVirtualAccount.getName()).isEqualTo(DEFAULT_NAME);
        assertThat(testVirtualAccount.getOrgName()).isEqualTo(UPDATED_ORG_NAME);
        assertThat(testVirtualAccount.getActive()).isEqualTo(DEFAULT_ACTIVE);
        assertThat(testVirtualAccount.getInn()).isEqualTo(DEFAULT_INN);
        assertThat(testVirtualAccount.getUid()).isEqualTo(DEFAULT_UID);
        assertThat(testVirtualAccount.getMode()).isEqualTo(DEFAULT_MODE);
    }

    @Test
    @Transactional
    void fullUpdateVirtualAccountWithPatch() throws Exception {
        // Initialize the database
        virtualAccountRepository.saveAndFlush(virtualAccount);

        int databaseSizeBeforeUpdate = virtualAccountRepository.findAll().size();

        // Update the virtualAccount using partial update
        VirtualAccount partialUpdatedVirtualAccount = new VirtualAccount();
        partialUpdatedVirtualAccount.setId(virtualAccount.getId());

        partialUpdatedVirtualAccount
            .name(UPDATED_NAME)
            .orgName(UPDATED_ORG_NAME)
            .active(UPDATED_ACTIVE)
            .inn(UPDATED_INN)
            .uid(UPDATED_UID)
            .mode(UPDATED_MODE);

        restVirtualAccountMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedVirtualAccount.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedVirtualAccount))
            )
            .andExpect(status().isOk());

        // Validate the VirtualAccount in the database
        List<VirtualAccount> virtualAccountList = virtualAccountRepository.findAll();
        assertThat(virtualAccountList).hasSize(databaseSizeBeforeUpdate);
        VirtualAccount testVirtualAccount = virtualAccountList.get(virtualAccountList.size() - 1);
        assertThat(testVirtualAccount.getName()).isEqualTo(UPDATED_NAME);
        assertThat(testVirtualAccount.getOrgName()).isEqualTo(UPDATED_ORG_NAME);
        assertThat(testVirtualAccount.getActive()).isEqualTo(UPDATED_ACTIVE);
        assertThat(testVirtualAccount.getInn()).isEqualTo(UPDATED_INN);
        assertThat(testVirtualAccount.getUid()).isEqualTo(UPDATED_UID);
        assertThat(testVirtualAccount.getMode()).isEqualTo(UPDATED_MODE);
    }

    @Test
    @Transactional
    void patchNonExistingVirtualAccount() throws Exception {
        int databaseSizeBeforeUpdate = virtualAccountRepository.findAll().size();
        virtualAccount.setId(count.incrementAndGet());

        // Create the VirtualAccount
        VirtualAccountDTO virtualAccountDTO = virtualAccountMapper.toDto(virtualAccount);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restVirtualAccountMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, virtualAccountDTO.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(virtualAccountDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the VirtualAccount in the database
        List<VirtualAccount> virtualAccountList = virtualAccountRepository.findAll();
        assertThat(virtualAccountList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchVirtualAccount() throws Exception {
        int databaseSizeBeforeUpdate = virtualAccountRepository.findAll().size();
        virtualAccount.setId(count.incrementAndGet());

        // Create the VirtualAccount
        VirtualAccountDTO virtualAccountDTO = virtualAccountMapper.toDto(virtualAccount);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restVirtualAccountMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, count.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(virtualAccountDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the VirtualAccount in the database
        List<VirtualAccount> virtualAccountList = virtualAccountRepository.findAll();
        assertThat(virtualAccountList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamVirtualAccount() throws Exception {
        int databaseSizeBeforeUpdate = virtualAccountRepository.findAll().size();
        virtualAccount.setId(count.incrementAndGet());

        // Create the VirtualAccount
        VirtualAccountDTO virtualAccountDTO = virtualAccountMapper.toDto(virtualAccount);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restVirtualAccountMockMvc
            .perform(
                patch(ENTITY_API_URL)
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(virtualAccountDTO))
            )
            .andExpect(status().isMethodNotAllowed());

        // Validate the VirtualAccount in the database
        List<VirtualAccount> virtualAccountList = virtualAccountRepository.findAll();
        assertThat(virtualAccountList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteVirtualAccount() throws Exception {
        // Initialize the database
        virtualAccountRepository.saveAndFlush(virtualAccount);

        int databaseSizeBeforeDelete = virtualAccountRepository.findAll().size();

        // Delete the virtualAccount
        restVirtualAccountMockMvc
            .perform(delete(ENTITY_API_URL_ID, virtualAccount.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        List<VirtualAccount> virtualAccountList = virtualAccountRepository.findAll();
        assertThat(virtualAccountList).hasSize(databaseSizeBeforeDelete - 1);
    }
}
