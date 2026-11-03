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
import uz.paykap.PkbillmsApp;
import uz.paykap.IntegrationTest;
import uz.paykap.domain.Deposit;
import uz.paykap.domain.VirtualAccount;
import uz.paykap.domain.enumeration.ModeType;
import uz.paykap.repository.VirtualAccountRepository;
import uz.paykap.service.VirtualAccountQueryService;
import uz.paykap.service.VirtualAccountService;
import uz.paykap.service.dto.VirtualAccountDTO;
import uz.paykap.service.mapper.VirtualAccountMapper;
import uz.paykap.web.rest.errors.ExceptionTranslator;

/**
 * Test class for the VirtualAccountResource REST controller.
 *
 * @see VirtualAccountResource
 */
@RunWith(SpringRunner.class)
@IntegrationTest
public class VirtualAccountResourceIntTest {

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

    private static final ModeType DEFAULT_MODE = ModeType.TEST;
    private static final ModeType UPDATED_MODE = ModeType.DEV;

    @Autowired
    private VirtualAccountRepository virtualAccountRepository;

    @Autowired
    private VirtualAccountMapper virtualAccountMapper;

    @Autowired
    private VirtualAccountService virtualAccountService;

    @Autowired
    private VirtualAccountQueryService virtualAccountQueryService;

    @Autowired
    private MappingJackson2HttpMessageConverter jacksonMessageConverter;

    @Autowired
    private PageableHandlerMethodArgumentResolver pageableArgumentResolver;

    @Autowired
    private ExceptionTranslator exceptionTranslator;

    @Autowired
    private EntityManager em;

    private MockMvc restVirtualAccountMockMvc;

    private VirtualAccount virtualAccount;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        final VirtualAccountResource virtualAccountResource = new VirtualAccountResource(
            virtualAccountService,
            virtualAccountRepository,
            virtualAccountQueryService
        );
        this.restVirtualAccountMockMvc =
            MockMvcBuilders
                .standaloneSetup(virtualAccountResource)
                .setCustomArgumentResolvers(pageableArgumentResolver)
                .setControllerAdvice(exceptionTranslator)
                .setConversionService(createFormattingConversionService())
                .setMessageConverters(jacksonMessageConverter)
                .build();
    }

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

    @Before
    public void initTest() {
        virtualAccount = createEntity(em);
    }

    @Test
    @Transactional
    public void createVirtualAccount() throws Exception {
        int databaseSizeBeforeCreate = virtualAccountRepository.findAll().size();

        // Create the VirtualAccount
        VirtualAccountDTO virtualAccountDTO = virtualAccountMapper.toDto(virtualAccount);
        restVirtualAccountMockMvc
            .perform(
                post("/api/virtual-accounts")
                    .contentType(TestUtil.APPLICATION_JSON_UTF8)
                    .content(TestUtil.convertObjectToJsonBytes(virtualAccountDTO))
            )
            .andExpect(status().isCreated());

        // Validate the VirtualAccount in the database
        List<VirtualAccount> virtualAccountList = virtualAccountRepository.findAll();
        assertThat(virtualAccountList).hasSize(databaseSizeBeforeCreate + 1);
        VirtualAccount testVirtualAccount = virtualAccountList.get(virtualAccountList.size() - 1);
        assertThat(testVirtualAccount.getName()).isEqualTo(DEFAULT_NAME);
        assertThat(testVirtualAccount.getOrgName()).isEqualTo(DEFAULT_ORG_NAME);
        assertThat(testVirtualAccount.isActive()).isEqualTo(DEFAULT_ACTIVE);
        assertThat(testVirtualAccount.getInn()).isEqualTo(DEFAULT_INN);
        assertThat(testVirtualAccount.getUid()).isEqualTo(DEFAULT_UID);
        assertThat(testVirtualAccount.getMode()).isEqualTo(DEFAULT_MODE);
    }

    @Test
    @Transactional
    public void createVirtualAccountWithExistingId() throws Exception {
        int databaseSizeBeforeCreate = virtualAccountRepository.findAll().size();

        // Create the VirtualAccount with an existing ID
        virtualAccount.setId(1L);
        VirtualAccountDTO virtualAccountDTO = virtualAccountMapper.toDto(virtualAccount);

        // An entity with an existing ID cannot be created, so this API call must fail
        restVirtualAccountMockMvc
            .perform(
                post("/api/virtual-accounts")
                    .contentType(TestUtil.APPLICATION_JSON_UTF8)
                    .content(TestUtil.convertObjectToJsonBytes(virtualAccountDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the VirtualAccount in the database
        List<VirtualAccount> virtualAccountList = virtualAccountRepository.findAll();
        assertThat(virtualAccountList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    public void checkNameIsRequired() throws Exception {
        int databaseSizeBeforeTest = virtualAccountRepository.findAll().size();
        // set the field null
        virtualAccount.setName(null);

        // Create the VirtualAccount, which fails.
        VirtualAccountDTO virtualAccountDTO = virtualAccountMapper.toDto(virtualAccount);

        restVirtualAccountMockMvc
            .perform(
                post("/api/virtual-accounts")
                    .contentType(TestUtil.APPLICATION_JSON_UTF8)
                    .content(TestUtil.convertObjectToJsonBytes(virtualAccountDTO))
            )
            .andExpect(status().isBadRequest());

        List<VirtualAccount> virtualAccountList = virtualAccountRepository.findAll();
        assertThat(virtualAccountList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkOrgNameIsRequired() throws Exception {
        int databaseSizeBeforeTest = virtualAccountRepository.findAll().size();
        // set the field null
        virtualAccount.setOrgName(null);

        // Create the VirtualAccount, which fails.
        VirtualAccountDTO virtualAccountDTO = virtualAccountMapper.toDto(virtualAccount);

        restVirtualAccountMockMvc
            .perform(
                post("/api/virtual-accounts")
                    .contentType(TestUtil.APPLICATION_JSON_UTF8)
                    .content(TestUtil.convertObjectToJsonBytes(virtualAccountDTO))
            )
            .andExpect(status().isBadRequest());

        List<VirtualAccount> virtualAccountList = virtualAccountRepository.findAll();
        assertThat(virtualAccountList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkActiveIsRequired() throws Exception {
        int databaseSizeBeforeTest = virtualAccountRepository.findAll().size();
        // set the field null
        virtualAccount.setActive(null);

        // Create the VirtualAccount, which fails.
        VirtualAccountDTO virtualAccountDTO = virtualAccountMapper.toDto(virtualAccount);

        restVirtualAccountMockMvc
            .perform(
                post("/api/virtual-accounts")
                    .contentType(TestUtil.APPLICATION_JSON_UTF8)
                    .content(TestUtil.convertObjectToJsonBytes(virtualAccountDTO))
            )
            .andExpect(status().isBadRequest());

        List<VirtualAccount> virtualAccountList = virtualAccountRepository.findAll();
        assertThat(virtualAccountList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkInnIsRequired() throws Exception {
        int databaseSizeBeforeTest = virtualAccountRepository.findAll().size();
        // set the field null
        virtualAccount.setInn(null);

        // Create the VirtualAccount, which fails.
        VirtualAccountDTO virtualAccountDTO = virtualAccountMapper.toDto(virtualAccount);

        restVirtualAccountMockMvc
            .perform(
                post("/api/virtual-accounts")
                    .contentType(TestUtil.APPLICATION_JSON_UTF8)
                    .content(TestUtil.convertObjectToJsonBytes(virtualAccountDTO))
            )
            .andExpect(status().isBadRequest());

        List<VirtualAccount> virtualAccountList = virtualAccountRepository.findAll();
        assertThat(virtualAccountList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void getAllVirtualAccounts() throws Exception {
        // Initialize the database
        virtualAccountRepository.saveAndFlush(virtualAccount);

        // Get all the virtualAccountList
        restVirtualAccountMockMvc
            .perform(get("/api/virtual-accounts?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(virtualAccount.getId().intValue())))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME.toString())))
            .andExpect(jsonPath("$.[*].orgName").value(hasItem(DEFAULT_ORG_NAME.toString())))
            .andExpect(jsonPath("$.[*].active").value(hasItem(DEFAULT_ACTIVE.booleanValue())))
            .andExpect(jsonPath("$.[*].inn").value(hasItem(DEFAULT_INN.toString())))
            .andExpect(jsonPath("$.[*].uid").value(hasItem(DEFAULT_UID.intValue())))
            .andExpect(jsonPath("$.[*].mode").value(hasItem(DEFAULT_MODE.toString())));
    }

    @Test
    @Transactional
    public void getVirtualAccount() throws Exception {
        // Initialize the database
        virtualAccountRepository.saveAndFlush(virtualAccount);

        // Get the virtualAccount
        restVirtualAccountMockMvc
            .perform(get("/api/virtual-accounts/{id}", virtualAccount.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.id").value(virtualAccount.getId().intValue()))
            .andExpect(jsonPath("$.name").value(DEFAULT_NAME.toString()))
            .andExpect(jsonPath("$.orgName").value(DEFAULT_ORG_NAME.toString()))
            .andExpect(jsonPath("$.active").value(DEFAULT_ACTIVE.booleanValue()))
            .andExpect(jsonPath("$.inn").value(DEFAULT_INN.toString()))
            .andExpect(jsonPath("$.uid").value(DEFAULT_UID.intValue()))
            .andExpect(jsonPath("$.mode").value(DEFAULT_MODE.toString()));
    }

    @Test
    @Transactional
    public void getAllVirtualAccountsByNameIsEqualToSomething() throws Exception {
        // Initialize the database
        virtualAccountRepository.saveAndFlush(virtualAccount);

        // Get all the virtualAccountList where name equals to DEFAULT_NAME
        defaultVirtualAccountShouldBeFound("name.equals=" + DEFAULT_NAME);

        // Get all the virtualAccountList where name equals to UPDATED_NAME
        defaultVirtualAccountShouldNotBeFound("name.equals=" + UPDATED_NAME);
    }

    @Test
    @Transactional
    public void getAllVirtualAccountsByNameIsInShouldWork() throws Exception {
        // Initialize the database
        virtualAccountRepository.saveAndFlush(virtualAccount);

        // Get all the virtualAccountList where name in DEFAULT_NAME or UPDATED_NAME
        defaultVirtualAccountShouldBeFound("name.in=" + DEFAULT_NAME + "," + UPDATED_NAME);

        // Get all the virtualAccountList where name equals to UPDATED_NAME
        defaultVirtualAccountShouldNotBeFound("name.in=" + UPDATED_NAME);
    }

    @Test
    @Transactional
    public void getAllVirtualAccountsByNameIsNullOrNotNull() throws Exception {
        // Initialize the database
        virtualAccountRepository.saveAndFlush(virtualAccount);

        // Get all the virtualAccountList where name is not null
        defaultVirtualAccountShouldBeFound("name.specified=true");

        // Get all the virtualAccountList where name is null
        defaultVirtualAccountShouldNotBeFound("name.specified=false");
    }

    @Test
    @Transactional
    public void getAllVirtualAccountsByOrgNameIsEqualToSomething() throws Exception {
        // Initialize the database
        virtualAccountRepository.saveAndFlush(virtualAccount);

        // Get all the virtualAccountList where orgName equals to DEFAULT_ORG_NAME
        defaultVirtualAccountShouldBeFound("orgName.equals=" + DEFAULT_ORG_NAME);

        // Get all the virtualAccountList where orgName equals to UPDATED_ORG_NAME
        defaultVirtualAccountShouldNotBeFound("orgName.equals=" + UPDATED_ORG_NAME);
    }

    @Test
    @Transactional
    public void getAllVirtualAccountsByOrgNameIsInShouldWork() throws Exception {
        // Initialize the database
        virtualAccountRepository.saveAndFlush(virtualAccount);

        // Get all the virtualAccountList where orgName in DEFAULT_ORG_NAME or UPDATED_ORG_NAME
        defaultVirtualAccountShouldBeFound("orgName.in=" + DEFAULT_ORG_NAME + "," + UPDATED_ORG_NAME);

        // Get all the virtualAccountList where orgName equals to UPDATED_ORG_NAME
        defaultVirtualAccountShouldNotBeFound("orgName.in=" + UPDATED_ORG_NAME);
    }

    @Test
    @Transactional
    public void getAllVirtualAccountsByOrgNameIsNullOrNotNull() throws Exception {
        // Initialize the database
        virtualAccountRepository.saveAndFlush(virtualAccount);

        // Get all the virtualAccountList where orgName is not null
        defaultVirtualAccountShouldBeFound("orgName.specified=true");

        // Get all the virtualAccountList where orgName is null
        defaultVirtualAccountShouldNotBeFound("orgName.specified=false");
    }

    @Test
    @Transactional
    public void getAllVirtualAccountsByActiveIsEqualToSomething() throws Exception {
        // Initialize the database
        virtualAccountRepository.saveAndFlush(virtualAccount);

        // Get all the virtualAccountList where active equals to DEFAULT_ACTIVE
        defaultVirtualAccountShouldBeFound("active.equals=" + DEFAULT_ACTIVE);

        // Get all the virtualAccountList where active equals to UPDATED_ACTIVE
        defaultVirtualAccountShouldNotBeFound("active.equals=" + UPDATED_ACTIVE);
    }

    @Test
    @Transactional
    public void getAllVirtualAccountsByActiveIsInShouldWork() throws Exception {
        // Initialize the database
        virtualAccountRepository.saveAndFlush(virtualAccount);

        // Get all the virtualAccountList where active in DEFAULT_ACTIVE or UPDATED_ACTIVE
        defaultVirtualAccountShouldBeFound("active.in=" + DEFAULT_ACTIVE + "," + UPDATED_ACTIVE);

        // Get all the virtualAccountList where active equals to UPDATED_ACTIVE
        defaultVirtualAccountShouldNotBeFound("active.in=" + UPDATED_ACTIVE);
    }

    @Test
    @Transactional
    public void getAllVirtualAccountsByActiveIsNullOrNotNull() throws Exception {
        // Initialize the database
        virtualAccountRepository.saveAndFlush(virtualAccount);

        // Get all the virtualAccountList where active is not null
        defaultVirtualAccountShouldBeFound("active.specified=true");

        // Get all the virtualAccountList where active is null
        defaultVirtualAccountShouldNotBeFound("active.specified=false");
    }

    @Test
    @Transactional
    public void getAllVirtualAccountsByInnIsEqualToSomething() throws Exception {
        // Initialize the database
        virtualAccountRepository.saveAndFlush(virtualAccount);

        // Get all the virtualAccountList where inn equals to DEFAULT_INN
        defaultVirtualAccountShouldBeFound("inn.equals=" + DEFAULT_INN);

        // Get all the virtualAccountList where inn equals to UPDATED_INN
        defaultVirtualAccountShouldNotBeFound("inn.equals=" + UPDATED_INN);
    }

    @Test
    @Transactional
    public void getAllVirtualAccountsByInnIsInShouldWork() throws Exception {
        // Initialize the database
        virtualAccountRepository.saveAndFlush(virtualAccount);

        // Get all the virtualAccountList where inn in DEFAULT_INN or UPDATED_INN
        defaultVirtualAccountShouldBeFound("inn.in=" + DEFAULT_INN + "," + UPDATED_INN);

        // Get all the virtualAccountList where inn equals to UPDATED_INN
        defaultVirtualAccountShouldNotBeFound("inn.in=" + UPDATED_INN);
    }

    @Test
    @Transactional
    public void getAllVirtualAccountsByInnIsNullOrNotNull() throws Exception {
        // Initialize the database
        virtualAccountRepository.saveAndFlush(virtualAccount);

        // Get all the virtualAccountList where inn is not null
        defaultVirtualAccountShouldBeFound("inn.specified=true");

        // Get all the virtualAccountList where inn is null
        defaultVirtualAccountShouldNotBeFound("inn.specified=false");
    }

    @Test
    @Transactional
    public void getAllVirtualAccountsByUidIsEqualToSomething() throws Exception {
        // Initialize the database
        virtualAccountRepository.saveAndFlush(virtualAccount);

        // Get all the virtualAccountList where uid equals to DEFAULT_UID
        defaultVirtualAccountShouldBeFound("uid.equals=" + DEFAULT_UID);

        // Get all the virtualAccountList where uid equals to UPDATED_UID
        defaultVirtualAccountShouldNotBeFound("uid.equals=" + UPDATED_UID);
    }

    @Test
    @Transactional
    public void getAllVirtualAccountsByUidIsInShouldWork() throws Exception {
        // Initialize the database
        virtualAccountRepository.saveAndFlush(virtualAccount);

        // Get all the virtualAccountList where uid in DEFAULT_UID or UPDATED_UID
        defaultVirtualAccountShouldBeFound("uid.in=" + DEFAULT_UID + "," + UPDATED_UID);

        // Get all the virtualAccountList where uid equals to UPDATED_UID
        defaultVirtualAccountShouldNotBeFound("uid.in=" + UPDATED_UID);
    }

    @Test
    @Transactional
    public void getAllVirtualAccountsByUidIsNullOrNotNull() throws Exception {
        // Initialize the database
        virtualAccountRepository.saveAndFlush(virtualAccount);

        // Get all the virtualAccountList where uid is not null
        defaultVirtualAccountShouldBeFound("uid.specified=true");

        // Get all the virtualAccountList where uid is null
        defaultVirtualAccountShouldNotBeFound("uid.specified=false");
    }

    @Test
    @Transactional
    public void getAllVirtualAccountsByUidIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        virtualAccountRepository.saveAndFlush(virtualAccount);

        // Get all the virtualAccountList where uid greater than or equals to DEFAULT_UID
        defaultVirtualAccountShouldBeFound("uid.greaterOrEqualThan=" + DEFAULT_UID);

        // Get all the virtualAccountList where uid greater than or equals to UPDATED_UID
        defaultVirtualAccountShouldNotBeFound("uid.greaterOrEqualThan=" + UPDATED_UID);
    }

    @Test
    @Transactional
    public void getAllVirtualAccountsByUidIsLessThanSomething() throws Exception {
        // Initialize the database
        virtualAccountRepository.saveAndFlush(virtualAccount);

        // Get all the virtualAccountList where uid less than or equals to DEFAULT_UID
        defaultVirtualAccountShouldNotBeFound("uid.lessThan=" + DEFAULT_UID);

        // Get all the virtualAccountList where uid less than or equals to UPDATED_UID
        defaultVirtualAccountShouldBeFound("uid.lessThan=" + UPDATED_UID);
    }

    @Test
    @Transactional
    public void getAllVirtualAccountsByModeIsEqualToSomething() throws Exception {
        // Initialize the database
        virtualAccountRepository.saveAndFlush(virtualAccount);

        // Get all the virtualAccountList where mode equals to DEFAULT_MODE
        defaultVirtualAccountShouldBeFound("mode.equals=" + DEFAULT_MODE);

        // Get all the virtualAccountList where mode equals to UPDATED_MODE
        defaultVirtualAccountShouldNotBeFound("mode.equals=" + UPDATED_MODE);
    }

    @Test
    @Transactional
    public void getAllVirtualAccountsByModeIsInShouldWork() throws Exception {
        // Initialize the database
        virtualAccountRepository.saveAndFlush(virtualAccount);

        // Get all the virtualAccountList where mode in DEFAULT_MODE or UPDATED_MODE
        defaultVirtualAccountShouldBeFound("mode.in=" + DEFAULT_MODE + "," + UPDATED_MODE);

        // Get all the virtualAccountList where mode equals to UPDATED_MODE
        defaultVirtualAccountShouldNotBeFound("mode.in=" + UPDATED_MODE);
    }

    @Test
    @Transactional
    public void getAllVirtualAccountsByModeIsNullOrNotNull() throws Exception {
        // Initialize the database
        virtualAccountRepository.saveAndFlush(virtualAccount);

        // Get all the virtualAccountList where mode is not null
        defaultVirtualAccountShouldBeFound("mode.specified=true");

        // Get all the virtualAccountList where mode is null
        defaultVirtualAccountShouldNotBeFound("mode.specified=false");
    }

    @Test
    @Transactional
    public void getAllVirtualAccountsByDepositIsEqualToSomething() throws Exception {
        // Initialize the database
        Deposit deposit = DepositResourceIntTest.createEntity(em);
        em.persist(deposit);
        em.flush();
        virtualAccount.setDeposit(deposit);
        deposit.setVirtualAccount(virtualAccount);
        virtualAccountRepository.saveAndFlush(virtualAccount);
        Long depositId = deposit.getId();

        // Get all the virtualAccountList where deposit equals to depositId
        defaultVirtualAccountShouldBeFound("depositId.equals=" + depositId);

        // Get all the virtualAccountList where deposit equals to depositId + 1
        defaultVirtualAccountShouldNotBeFound("depositId.equals=" + (depositId + 1));
    }

    /**
     * Executes the search, and checks that the default entity is returned
     */
    private void defaultVirtualAccountShouldBeFound(String filter) throws Exception {
        restVirtualAccountMockMvc
            .perform(get("/api/virtual-accounts?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(virtualAccount.getId().intValue())))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME.toString())))
            .andExpect(jsonPath("$.[*].orgName").value(hasItem(DEFAULT_ORG_NAME.toString())))
            .andExpect(jsonPath("$.[*].active").value(hasItem(DEFAULT_ACTIVE.booleanValue())))
            .andExpect(jsonPath("$.[*].inn").value(hasItem(DEFAULT_INN.toString())))
            .andExpect(jsonPath("$.[*].uid").value(hasItem(DEFAULT_UID.intValue())))
            .andExpect(jsonPath("$.[*].mode").value(hasItem(DEFAULT_MODE.toString())));
    }

    /**
     * Executes the search, and checks that the default entity is not returned
     */
    private void defaultVirtualAccountShouldNotBeFound(String filter) throws Exception {
        restVirtualAccountMockMvc
            .perform(get("/api/virtual-accounts?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    @Transactional
    public void getNonExistingVirtualAccount() throws Exception {
        // Get the virtualAccount
        restVirtualAccountMockMvc.perform(get("/api/virtual-accounts/{id}", Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updateVirtualAccount() throws Exception {
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
                put("/api/virtual-accounts")
                    .contentType(TestUtil.APPLICATION_JSON_UTF8)
                    .content(TestUtil.convertObjectToJsonBytes(virtualAccountDTO))
            )
            .andExpect(status().isOk());

        // Validate the VirtualAccount in the database
        List<VirtualAccount> virtualAccountList = virtualAccountRepository.findAll();
        assertThat(virtualAccountList).hasSize(databaseSizeBeforeUpdate);
        VirtualAccount testVirtualAccount = virtualAccountList.get(virtualAccountList.size() - 1);
        assertThat(testVirtualAccount.getName()).isEqualTo(UPDATED_NAME);
        assertThat(testVirtualAccount.getOrgName()).isEqualTo(UPDATED_ORG_NAME);
        assertThat(testVirtualAccount.isActive()).isEqualTo(UPDATED_ACTIVE);
        assertThat(testVirtualAccount.getInn()).isEqualTo(UPDATED_INN);
        assertThat(testVirtualAccount.getUid()).isEqualTo(UPDATED_UID);
        assertThat(testVirtualAccount.getMode()).isEqualTo(UPDATED_MODE);
    }

    @Test
    @Transactional
    public void updateNonExistingVirtualAccount() throws Exception {
        int databaseSizeBeforeUpdate = virtualAccountRepository.findAll().size();

        // Create the VirtualAccount
        VirtualAccountDTO virtualAccountDTO = virtualAccountMapper.toDto(virtualAccount);

        // If the entity doesn't have an ID, it will be created instead of just being updated
        restVirtualAccountMockMvc
            .perform(
                put("/api/virtual-accounts")
                    .contentType(TestUtil.APPLICATION_JSON_UTF8)
                    .content(TestUtil.convertObjectToJsonBytes(virtualAccountDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the VirtualAccount in the database
        List<VirtualAccount> virtualAccountList = virtualAccountRepository.findAll();
        assertThat(virtualAccountList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    public void deleteVirtualAccount() throws Exception {
        // Initialize the database
        virtualAccountRepository.saveAndFlush(virtualAccount);

        int databaseSizeBeforeDelete = virtualAccountRepository.findAll().size();

        // Get the virtualAccount
        restVirtualAccountMockMvc
            .perform(delete("/api/virtual-accounts/{id}", virtualAccount.getId()).accept(TestUtil.APPLICATION_JSON_UTF8))
            .andExpect(status().isOk());

        // Validate the database is empty
        List<VirtualAccount> virtualAccountList = virtualAccountRepository.findAll();
        assertThat(virtualAccountList).hasSize(databaseSizeBeforeDelete - 1);
    }

    @Test
    @Transactional
    public void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(VirtualAccount.class);
        VirtualAccount virtualAccount1 = new VirtualAccount();
        virtualAccount1.setId(1L);
        VirtualAccount virtualAccount2 = new VirtualAccount();
        virtualAccount2.setId(virtualAccount1.getId());
        assertThat(virtualAccount1).isEqualTo(virtualAccount2);
        virtualAccount2.setId(2L);
        assertThat(virtualAccount1).isNotEqualTo(virtualAccount2);
        virtualAccount1.setId(null);
        assertThat(virtualAccount1).isNotEqualTo(virtualAccount2);
    }

    @Test
    @Transactional
    public void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(VirtualAccountDTO.class);
        VirtualAccountDTO virtualAccountDTO1 = new VirtualAccountDTO();
        virtualAccountDTO1.setId(1L);
        VirtualAccountDTO virtualAccountDTO2 = new VirtualAccountDTO();
        assertThat(virtualAccountDTO1).isNotEqualTo(virtualAccountDTO2);
        virtualAccountDTO2.setId(virtualAccountDTO1.getId());
        assertThat(virtualAccountDTO1).isEqualTo(virtualAccountDTO2);
        virtualAccountDTO2.setId(2L);
        assertThat(virtualAccountDTO1).isNotEqualTo(virtualAccountDTO2);
        virtualAccountDTO1.setId(null);
        assertThat(virtualAccountDTO1).isNotEqualTo(virtualAccountDTO2);
    }

    @Test
    @Transactional
    public void testEntityFromId() {
        assertThat(virtualAccountMapper.fromId(42L).getId()).isEqualTo(42);
        assertThat(virtualAccountMapper.fromId(null)).isNull();
    }
}
