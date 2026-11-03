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
import uz.paykap.domain.VirtualAccount;
import uz.paykap.domain.VirtualAccountDetails;
import uz.paykap.domain.enumeration.VirtualAccountType;
import uz.paykap.repository.VirtualAccountDetailsRepository;
import uz.paykap.service.criteria.VirtualAccountDetailsCriteria;
import uz.paykap.service.dto.VirtualAccountDetailsDTO;
import uz.paykap.service.mapper.VirtualAccountDetailsMapper;

/**
 * Integration tests for the {@link VirtualAccountDetailsResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class VirtualAccountDetailsResourceIT {

    private static final String DEFAULT_PARAMS = "AAAAAAAAAA";
    private static final String UPDATED_PARAMS = "BBBBBBBBBB";

    private static final String DEFAULT_ACCOUNT = "AAAAAAAAAA";
    private static final String UPDATED_ACCOUNT = "BBBBBBBBBB";

    private static final Boolean DEFAULT_ACTIVE = false;
    private static final Boolean UPDATED_ACTIVE = true;

    private static final String DEFAULT_MFO = "AAAAAAAAAA";
    private static final String UPDATED_MFO = "BBBBBBBBBB";

    private static final VirtualAccountType DEFAULT_TYPE = VirtualAccountType.EPOS;
    private static final VirtualAccountType UPDATED_TYPE = VirtualAccountType.BANK_ACC;

    private static final String ENTITY_API_URL = "/api/virtual-account-details";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong count = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private VirtualAccountDetailsRepository virtualAccountDetailsRepository;

    @Autowired
    private VirtualAccountDetailsMapper virtualAccountDetailsMapper;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restVirtualAccountDetailsMockMvc;

    private VirtualAccountDetails virtualAccountDetails;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static VirtualAccountDetails createEntity(EntityManager em) {
        VirtualAccountDetails virtualAccountDetails = new VirtualAccountDetails()
            .params(DEFAULT_PARAMS)
            .account(DEFAULT_ACCOUNT)
            .active(DEFAULT_ACTIVE)
            .mfo(DEFAULT_MFO)
            .type(DEFAULT_TYPE);
        return virtualAccountDetails;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static VirtualAccountDetails createUpdatedEntity(EntityManager em) {
        VirtualAccountDetails virtualAccountDetails = new VirtualAccountDetails()
            .params(UPDATED_PARAMS)
            .account(UPDATED_ACCOUNT)
            .active(UPDATED_ACTIVE)
            .mfo(UPDATED_MFO)
            .type(UPDATED_TYPE);
        return virtualAccountDetails;
    }

    @BeforeEach
    public void initTest() {
        virtualAccountDetails = createEntity(em);
    }

    @Test
    @Transactional
    void createVirtualAccountDetails() throws Exception {
        int databaseSizeBeforeCreate = virtualAccountDetailsRepository.findAll().size();
        // Create the VirtualAccountDetails
        VirtualAccountDetailsDTO virtualAccountDetailsDTO = virtualAccountDetailsMapper.toDto(virtualAccountDetails);
        restVirtualAccountDetailsMockMvc
            .perform(
                post(ENTITY_API_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(virtualAccountDetailsDTO))
            )
            .andExpect(status().isCreated());

        // Validate the VirtualAccountDetails in the database
        List<VirtualAccountDetails> virtualAccountDetailsList = virtualAccountDetailsRepository.findAll();
        assertThat(virtualAccountDetailsList).hasSize(databaseSizeBeforeCreate + 1);
        VirtualAccountDetails testVirtualAccountDetails = virtualAccountDetailsList.get(virtualAccountDetailsList.size() - 1);
        assertThat(testVirtualAccountDetails.getParams()).isEqualTo(DEFAULT_PARAMS);
        assertThat(testVirtualAccountDetails.getAccount()).isEqualTo(DEFAULT_ACCOUNT);
        assertThat(testVirtualAccountDetails.getActive()).isEqualTo(DEFAULT_ACTIVE);
        assertThat(testVirtualAccountDetails.getMfo()).isEqualTo(DEFAULT_MFO);
        assertThat(testVirtualAccountDetails.getType()).isEqualTo(DEFAULT_TYPE);
    }

    @Test
    @Transactional
    void createVirtualAccountDetailsWithExistingId() throws Exception {
        // Create the VirtualAccountDetails with an existing ID
        virtualAccountDetails.setId(1L);
        VirtualAccountDetailsDTO virtualAccountDetailsDTO = virtualAccountDetailsMapper.toDto(virtualAccountDetails);

        int databaseSizeBeforeCreate = virtualAccountDetailsRepository.findAll().size();

        // An entity with an existing ID cannot be created, so this API call must fail
        restVirtualAccountDetailsMockMvc
            .perform(
                post(ENTITY_API_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(virtualAccountDetailsDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the VirtualAccountDetails in the database
        List<VirtualAccountDetails> virtualAccountDetailsList = virtualAccountDetailsRepository.findAll();
        assertThat(virtualAccountDetailsList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkParamsIsRequired() throws Exception {
        int databaseSizeBeforeTest = virtualAccountDetailsRepository.findAll().size();
        // set the field null
        virtualAccountDetails.setParams(null);

        // Create the VirtualAccountDetails, which fails.
        VirtualAccountDetailsDTO virtualAccountDetailsDTO = virtualAccountDetailsMapper.toDto(virtualAccountDetails);

        restVirtualAccountDetailsMockMvc
            .perform(
                post(ENTITY_API_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(virtualAccountDetailsDTO))
            )
            .andExpect(status().isBadRequest());

        List<VirtualAccountDetails> virtualAccountDetailsList = virtualAccountDetailsRepository.findAll();
        assertThat(virtualAccountDetailsList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkAccountIsRequired() throws Exception {
        int databaseSizeBeforeTest = virtualAccountDetailsRepository.findAll().size();
        // set the field null
        virtualAccountDetails.setAccount(null);

        // Create the VirtualAccountDetails, which fails.
        VirtualAccountDetailsDTO virtualAccountDetailsDTO = virtualAccountDetailsMapper.toDto(virtualAccountDetails);

        restVirtualAccountDetailsMockMvc
            .perform(
                post(ENTITY_API_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(virtualAccountDetailsDTO))
            )
            .andExpect(status().isBadRequest());

        List<VirtualAccountDetails> virtualAccountDetailsList = virtualAccountDetailsRepository.findAll();
        assertThat(virtualAccountDetailsList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkActiveIsRequired() throws Exception {
        int databaseSizeBeforeTest = virtualAccountDetailsRepository.findAll().size();
        // set the field null
        virtualAccountDetails.setActive(null);

        // Create the VirtualAccountDetails, which fails.
        VirtualAccountDetailsDTO virtualAccountDetailsDTO = virtualAccountDetailsMapper.toDto(virtualAccountDetails);

        restVirtualAccountDetailsMockMvc
            .perform(
                post(ENTITY_API_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(virtualAccountDetailsDTO))
            )
            .andExpect(status().isBadRequest());

        List<VirtualAccountDetails> virtualAccountDetailsList = virtualAccountDetailsRepository.findAll();
        assertThat(virtualAccountDetailsList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkMfoIsRequired() throws Exception {
        int databaseSizeBeforeTest = virtualAccountDetailsRepository.findAll().size();
        // set the field null
        virtualAccountDetails.setMfo(null);

        // Create the VirtualAccountDetails, which fails.
        VirtualAccountDetailsDTO virtualAccountDetailsDTO = virtualAccountDetailsMapper.toDto(virtualAccountDetails);

        restVirtualAccountDetailsMockMvc
            .perform(
                post(ENTITY_API_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(virtualAccountDetailsDTO))
            )
            .andExpect(status().isBadRequest());

        List<VirtualAccountDetails> virtualAccountDetailsList = virtualAccountDetailsRepository.findAll();
        assertThat(virtualAccountDetailsList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllVirtualAccountDetails() throws Exception {
        // Initialize the database
        virtualAccountDetailsRepository.saveAndFlush(virtualAccountDetails);

        // Get all the virtualAccountDetailsList
        restVirtualAccountDetailsMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(virtualAccountDetails.getId().intValue())))
            .andExpect(jsonPath("$.[*].params").value(hasItem(DEFAULT_PARAMS)))
            .andExpect(jsonPath("$.[*].account").value(hasItem(DEFAULT_ACCOUNT)))
            .andExpect(jsonPath("$.[*].active").value(hasItem(DEFAULT_ACTIVE.booleanValue())))
            .andExpect(jsonPath("$.[*].mfo").value(hasItem(DEFAULT_MFO)))
            .andExpect(jsonPath("$.[*].type").value(hasItem(DEFAULT_TYPE.toString())));
    }

    @Test
    @Transactional
    void getVirtualAccountDetails() throws Exception {
        // Initialize the database
        virtualAccountDetailsRepository.saveAndFlush(virtualAccountDetails);

        // Get the virtualAccountDetails
        restVirtualAccountDetailsMockMvc
            .perform(get(ENTITY_API_URL_ID, virtualAccountDetails.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(virtualAccountDetails.getId().intValue()))
            .andExpect(jsonPath("$.params").value(DEFAULT_PARAMS))
            .andExpect(jsonPath("$.account").value(DEFAULT_ACCOUNT))
            .andExpect(jsonPath("$.active").value(DEFAULT_ACTIVE.booleanValue()))
            .andExpect(jsonPath("$.mfo").value(DEFAULT_MFO))
            .andExpect(jsonPath("$.type").value(DEFAULT_TYPE.toString()));
    }

    @Test
    @Transactional
    void getVirtualAccountDetailsByIdFiltering() throws Exception {
        // Initialize the database
        virtualAccountDetailsRepository.saveAndFlush(virtualAccountDetails);

        Long id = virtualAccountDetails.getId();

        defaultVirtualAccountDetailsShouldBeFound("id.equals=" + id);
        defaultVirtualAccountDetailsShouldNotBeFound("id.notEquals=" + id);

        defaultVirtualAccountDetailsShouldBeFound("id.greaterThanOrEqual=" + id);
        defaultVirtualAccountDetailsShouldNotBeFound("id.greaterThan=" + id);

        defaultVirtualAccountDetailsShouldBeFound("id.lessThanOrEqual=" + id);
        defaultVirtualAccountDetailsShouldNotBeFound("id.lessThan=" + id);
    }

    @Test
    @Transactional
    void getAllVirtualAccountDetailsByParamsIsEqualToSomething() throws Exception {
        // Initialize the database
        virtualAccountDetailsRepository.saveAndFlush(virtualAccountDetails);

        // Get all the virtualAccountDetailsList where params equals to DEFAULT_PARAMS
        defaultVirtualAccountDetailsShouldBeFound("params.equals=" + DEFAULT_PARAMS);

        // Get all the virtualAccountDetailsList where params equals to UPDATED_PARAMS
        defaultVirtualAccountDetailsShouldNotBeFound("params.equals=" + UPDATED_PARAMS);
    }

    @Test
    @Transactional
    void getAllVirtualAccountDetailsByParamsIsNotEqualToSomething() throws Exception {
        // Initialize the database
        virtualAccountDetailsRepository.saveAndFlush(virtualAccountDetails);

        // Get all the virtualAccountDetailsList where params not equals to DEFAULT_PARAMS
        defaultVirtualAccountDetailsShouldNotBeFound("params.notEquals=" + DEFAULT_PARAMS);

        // Get all the virtualAccountDetailsList where params not equals to UPDATED_PARAMS
        defaultVirtualAccountDetailsShouldBeFound("params.notEquals=" + UPDATED_PARAMS);
    }

    @Test
    @Transactional
    void getAllVirtualAccountDetailsByParamsIsInShouldWork() throws Exception {
        // Initialize the database
        virtualAccountDetailsRepository.saveAndFlush(virtualAccountDetails);

        // Get all the virtualAccountDetailsList where params in DEFAULT_PARAMS or UPDATED_PARAMS
        defaultVirtualAccountDetailsShouldBeFound("params.in=" + DEFAULT_PARAMS + "," + UPDATED_PARAMS);

        // Get all the virtualAccountDetailsList where params equals to UPDATED_PARAMS
        defaultVirtualAccountDetailsShouldNotBeFound("params.in=" + UPDATED_PARAMS);
    }

    @Test
    @Transactional
    void getAllVirtualAccountDetailsByParamsIsNullOrNotNull() throws Exception {
        // Initialize the database
        virtualAccountDetailsRepository.saveAndFlush(virtualAccountDetails);

        // Get all the virtualAccountDetailsList where params is not null
        defaultVirtualAccountDetailsShouldBeFound("params.specified=true");

        // Get all the virtualAccountDetailsList where params is null
        defaultVirtualAccountDetailsShouldNotBeFound("params.specified=false");
    }

    @Test
    @Transactional
    void getAllVirtualAccountDetailsByParamsContainsSomething() throws Exception {
        // Initialize the database
        virtualAccountDetailsRepository.saveAndFlush(virtualAccountDetails);

        // Get all the virtualAccountDetailsList where params contains DEFAULT_PARAMS
        defaultVirtualAccountDetailsShouldBeFound("params.contains=" + DEFAULT_PARAMS);

        // Get all the virtualAccountDetailsList where params contains UPDATED_PARAMS
        defaultVirtualAccountDetailsShouldNotBeFound("params.contains=" + UPDATED_PARAMS);
    }

    @Test
    @Transactional
    void getAllVirtualAccountDetailsByParamsNotContainsSomething() throws Exception {
        // Initialize the database
        virtualAccountDetailsRepository.saveAndFlush(virtualAccountDetails);

        // Get all the virtualAccountDetailsList where params does not contain DEFAULT_PARAMS
        defaultVirtualAccountDetailsShouldNotBeFound("params.doesNotContain=" + DEFAULT_PARAMS);

        // Get all the virtualAccountDetailsList where params does not contain UPDATED_PARAMS
        defaultVirtualAccountDetailsShouldBeFound("params.doesNotContain=" + UPDATED_PARAMS);
    }

    @Test
    @Transactional
    void getAllVirtualAccountDetailsByAccountIsEqualToSomething() throws Exception {
        // Initialize the database
        virtualAccountDetailsRepository.saveAndFlush(virtualAccountDetails);

        // Get all the virtualAccountDetailsList where account equals to DEFAULT_ACCOUNT
        defaultVirtualAccountDetailsShouldBeFound("account.equals=" + DEFAULT_ACCOUNT);

        // Get all the virtualAccountDetailsList where account equals to UPDATED_ACCOUNT
        defaultVirtualAccountDetailsShouldNotBeFound("account.equals=" + UPDATED_ACCOUNT);
    }

    @Test
    @Transactional
    void getAllVirtualAccountDetailsByAccountIsNotEqualToSomething() throws Exception {
        // Initialize the database
        virtualAccountDetailsRepository.saveAndFlush(virtualAccountDetails);

        // Get all the virtualAccountDetailsList where account not equals to DEFAULT_ACCOUNT
        defaultVirtualAccountDetailsShouldNotBeFound("account.notEquals=" + DEFAULT_ACCOUNT);

        // Get all the virtualAccountDetailsList where account not equals to UPDATED_ACCOUNT
        defaultVirtualAccountDetailsShouldBeFound("account.notEquals=" + UPDATED_ACCOUNT);
    }

    @Test
    @Transactional
    void getAllVirtualAccountDetailsByAccountIsInShouldWork() throws Exception {
        // Initialize the database
        virtualAccountDetailsRepository.saveAndFlush(virtualAccountDetails);

        // Get all the virtualAccountDetailsList where account in DEFAULT_ACCOUNT or UPDATED_ACCOUNT
        defaultVirtualAccountDetailsShouldBeFound("account.in=" + DEFAULT_ACCOUNT + "," + UPDATED_ACCOUNT);

        // Get all the virtualAccountDetailsList where account equals to UPDATED_ACCOUNT
        defaultVirtualAccountDetailsShouldNotBeFound("account.in=" + UPDATED_ACCOUNT);
    }

    @Test
    @Transactional
    void getAllVirtualAccountDetailsByAccountIsNullOrNotNull() throws Exception {
        // Initialize the database
        virtualAccountDetailsRepository.saveAndFlush(virtualAccountDetails);

        // Get all the virtualAccountDetailsList where account is not null
        defaultVirtualAccountDetailsShouldBeFound("account.specified=true");

        // Get all the virtualAccountDetailsList where account is null
        defaultVirtualAccountDetailsShouldNotBeFound("account.specified=false");
    }

    @Test
    @Transactional
    void getAllVirtualAccountDetailsByAccountContainsSomething() throws Exception {
        // Initialize the database
        virtualAccountDetailsRepository.saveAndFlush(virtualAccountDetails);

        // Get all the virtualAccountDetailsList where account contains DEFAULT_ACCOUNT
        defaultVirtualAccountDetailsShouldBeFound("account.contains=" + DEFAULT_ACCOUNT);

        // Get all the virtualAccountDetailsList where account contains UPDATED_ACCOUNT
        defaultVirtualAccountDetailsShouldNotBeFound("account.contains=" + UPDATED_ACCOUNT);
    }

    @Test
    @Transactional
    void getAllVirtualAccountDetailsByAccountNotContainsSomething() throws Exception {
        // Initialize the database
        virtualAccountDetailsRepository.saveAndFlush(virtualAccountDetails);

        // Get all the virtualAccountDetailsList where account does not contain DEFAULT_ACCOUNT
        defaultVirtualAccountDetailsShouldNotBeFound("account.doesNotContain=" + DEFAULT_ACCOUNT);

        // Get all the virtualAccountDetailsList where account does not contain UPDATED_ACCOUNT
        defaultVirtualAccountDetailsShouldBeFound("account.doesNotContain=" + UPDATED_ACCOUNT);
    }

    @Test
    @Transactional
    void getAllVirtualAccountDetailsByActiveIsEqualToSomething() throws Exception {
        // Initialize the database
        virtualAccountDetailsRepository.saveAndFlush(virtualAccountDetails);

        // Get all the virtualAccountDetailsList where active equals to DEFAULT_ACTIVE
        defaultVirtualAccountDetailsShouldBeFound("active.equals=" + DEFAULT_ACTIVE);

        // Get all the virtualAccountDetailsList where active equals to UPDATED_ACTIVE
        defaultVirtualAccountDetailsShouldNotBeFound("active.equals=" + UPDATED_ACTIVE);
    }

    @Test
    @Transactional
    void getAllVirtualAccountDetailsByActiveIsNotEqualToSomething() throws Exception {
        // Initialize the database
        virtualAccountDetailsRepository.saveAndFlush(virtualAccountDetails);

        // Get all the virtualAccountDetailsList where active not equals to DEFAULT_ACTIVE
        defaultVirtualAccountDetailsShouldNotBeFound("active.notEquals=" + DEFAULT_ACTIVE);

        // Get all the virtualAccountDetailsList where active not equals to UPDATED_ACTIVE
        defaultVirtualAccountDetailsShouldBeFound("active.notEquals=" + UPDATED_ACTIVE);
    }

    @Test
    @Transactional
    void getAllVirtualAccountDetailsByActiveIsInShouldWork() throws Exception {
        // Initialize the database
        virtualAccountDetailsRepository.saveAndFlush(virtualAccountDetails);

        // Get all the virtualAccountDetailsList where active in DEFAULT_ACTIVE or UPDATED_ACTIVE
        defaultVirtualAccountDetailsShouldBeFound("active.in=" + DEFAULT_ACTIVE + "," + UPDATED_ACTIVE);

        // Get all the virtualAccountDetailsList where active equals to UPDATED_ACTIVE
        defaultVirtualAccountDetailsShouldNotBeFound("active.in=" + UPDATED_ACTIVE);
    }

    @Test
    @Transactional
    void getAllVirtualAccountDetailsByActiveIsNullOrNotNull() throws Exception {
        // Initialize the database
        virtualAccountDetailsRepository.saveAndFlush(virtualAccountDetails);

        // Get all the virtualAccountDetailsList where active is not null
        defaultVirtualAccountDetailsShouldBeFound("active.specified=true");

        // Get all the virtualAccountDetailsList where active is null
        defaultVirtualAccountDetailsShouldNotBeFound("active.specified=false");
    }

    @Test
    @Transactional
    void getAllVirtualAccountDetailsByMfoIsEqualToSomething() throws Exception {
        // Initialize the database
        virtualAccountDetailsRepository.saveAndFlush(virtualAccountDetails);

        // Get all the virtualAccountDetailsList where mfo equals to DEFAULT_MFO
        defaultVirtualAccountDetailsShouldBeFound("mfo.equals=" + DEFAULT_MFO);

        // Get all the virtualAccountDetailsList where mfo equals to UPDATED_MFO
        defaultVirtualAccountDetailsShouldNotBeFound("mfo.equals=" + UPDATED_MFO);
    }

    @Test
    @Transactional
    void getAllVirtualAccountDetailsByMfoIsNotEqualToSomething() throws Exception {
        // Initialize the database
        virtualAccountDetailsRepository.saveAndFlush(virtualAccountDetails);

        // Get all the virtualAccountDetailsList where mfo not equals to DEFAULT_MFO
        defaultVirtualAccountDetailsShouldNotBeFound("mfo.notEquals=" + DEFAULT_MFO);

        // Get all the virtualAccountDetailsList where mfo not equals to UPDATED_MFO
        defaultVirtualAccountDetailsShouldBeFound("mfo.notEquals=" + UPDATED_MFO);
    }

    @Test
    @Transactional
    void getAllVirtualAccountDetailsByMfoIsInShouldWork() throws Exception {
        // Initialize the database
        virtualAccountDetailsRepository.saveAndFlush(virtualAccountDetails);

        // Get all the virtualAccountDetailsList where mfo in DEFAULT_MFO or UPDATED_MFO
        defaultVirtualAccountDetailsShouldBeFound("mfo.in=" + DEFAULT_MFO + "," + UPDATED_MFO);

        // Get all the virtualAccountDetailsList where mfo equals to UPDATED_MFO
        defaultVirtualAccountDetailsShouldNotBeFound("mfo.in=" + UPDATED_MFO);
    }

    @Test
    @Transactional
    void getAllVirtualAccountDetailsByMfoIsNullOrNotNull() throws Exception {
        // Initialize the database
        virtualAccountDetailsRepository.saveAndFlush(virtualAccountDetails);

        // Get all the virtualAccountDetailsList where mfo is not null
        defaultVirtualAccountDetailsShouldBeFound("mfo.specified=true");

        // Get all the virtualAccountDetailsList where mfo is null
        defaultVirtualAccountDetailsShouldNotBeFound("mfo.specified=false");
    }

    @Test
    @Transactional
    void getAllVirtualAccountDetailsByMfoContainsSomething() throws Exception {
        // Initialize the database
        virtualAccountDetailsRepository.saveAndFlush(virtualAccountDetails);

        // Get all the virtualAccountDetailsList where mfo contains DEFAULT_MFO
        defaultVirtualAccountDetailsShouldBeFound("mfo.contains=" + DEFAULT_MFO);

        // Get all the virtualAccountDetailsList where mfo contains UPDATED_MFO
        defaultVirtualAccountDetailsShouldNotBeFound("mfo.contains=" + UPDATED_MFO);
    }

    @Test
    @Transactional
    void getAllVirtualAccountDetailsByMfoNotContainsSomething() throws Exception {
        // Initialize the database
        virtualAccountDetailsRepository.saveAndFlush(virtualAccountDetails);

        // Get all the virtualAccountDetailsList where mfo does not contain DEFAULT_MFO
        defaultVirtualAccountDetailsShouldNotBeFound("mfo.doesNotContain=" + DEFAULT_MFO);

        // Get all the virtualAccountDetailsList where mfo does not contain UPDATED_MFO
        defaultVirtualAccountDetailsShouldBeFound("mfo.doesNotContain=" + UPDATED_MFO);
    }

    @Test
    @Transactional
    void getAllVirtualAccountDetailsByTypeIsEqualToSomething() throws Exception {
        // Initialize the database
        virtualAccountDetailsRepository.saveAndFlush(virtualAccountDetails);

        // Get all the virtualAccountDetailsList where type equals to DEFAULT_TYPE
        defaultVirtualAccountDetailsShouldBeFound("type.equals=" + DEFAULT_TYPE);

        // Get all the virtualAccountDetailsList where type equals to UPDATED_TYPE
        defaultVirtualAccountDetailsShouldNotBeFound("type.equals=" + UPDATED_TYPE);
    }

    @Test
    @Transactional
    void getAllVirtualAccountDetailsByTypeIsNotEqualToSomething() throws Exception {
        // Initialize the database
        virtualAccountDetailsRepository.saveAndFlush(virtualAccountDetails);

        // Get all the virtualAccountDetailsList where type not equals to DEFAULT_TYPE
        defaultVirtualAccountDetailsShouldNotBeFound("type.notEquals=" + DEFAULT_TYPE);

        // Get all the virtualAccountDetailsList where type not equals to UPDATED_TYPE
        defaultVirtualAccountDetailsShouldBeFound("type.notEquals=" + UPDATED_TYPE);
    }

    @Test
    @Transactional
    void getAllVirtualAccountDetailsByTypeIsInShouldWork() throws Exception {
        // Initialize the database
        virtualAccountDetailsRepository.saveAndFlush(virtualAccountDetails);

        // Get all the virtualAccountDetailsList where type in DEFAULT_TYPE or UPDATED_TYPE
        defaultVirtualAccountDetailsShouldBeFound("type.in=" + DEFAULT_TYPE + "," + UPDATED_TYPE);

        // Get all the virtualAccountDetailsList where type equals to UPDATED_TYPE
        defaultVirtualAccountDetailsShouldNotBeFound("type.in=" + UPDATED_TYPE);
    }

    @Test
    @Transactional
    void getAllVirtualAccountDetailsByTypeIsNullOrNotNull() throws Exception {
        // Initialize the database
        virtualAccountDetailsRepository.saveAndFlush(virtualAccountDetails);

        // Get all the virtualAccountDetailsList where type is not null
        defaultVirtualAccountDetailsShouldBeFound("type.specified=true");

        // Get all the virtualAccountDetailsList where type is null
        defaultVirtualAccountDetailsShouldNotBeFound("type.specified=false");
    }

    @Test
    @Transactional
    void getAllVirtualAccountDetailsByVirtualAccountIsEqualToSomething() throws Exception {
        // Initialize the database
        virtualAccountDetailsRepository.saveAndFlush(virtualAccountDetails);
        VirtualAccount virtualAccount = VirtualAccountResourceIT.createEntity(em);
        em.persist(virtualAccount);
        em.flush();
        virtualAccountDetails.setVirtualAccount(virtualAccount);
        virtualAccountDetailsRepository.saveAndFlush(virtualAccountDetails);
        Long virtualAccountId = virtualAccount.getId();

        // Get all the virtualAccountDetailsList where virtualAccount equals to virtualAccountId
        defaultVirtualAccountDetailsShouldBeFound("virtualAccountId.equals=" + virtualAccountId);

        // Get all the virtualAccountDetailsList where virtualAccount equals to (virtualAccountId + 1)
        defaultVirtualAccountDetailsShouldNotBeFound("virtualAccountId.equals=" + (virtualAccountId + 1));
    }

    /**
     * Executes the search, and checks that the default entity is returned.
     */
    private void defaultVirtualAccountDetailsShouldBeFound(String filter) throws Exception {
        restVirtualAccountDetailsMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(virtualAccountDetails.getId().intValue())))
            .andExpect(jsonPath("$.[*].params").value(hasItem(DEFAULT_PARAMS)))
            .andExpect(jsonPath("$.[*].account").value(hasItem(DEFAULT_ACCOUNT)))
            .andExpect(jsonPath("$.[*].active").value(hasItem(DEFAULT_ACTIVE.booleanValue())))
            .andExpect(jsonPath("$.[*].mfo").value(hasItem(DEFAULT_MFO)))
            .andExpect(jsonPath("$.[*].type").value(hasItem(DEFAULT_TYPE.toString())));

        // Check, that the count call also returns 1
        restVirtualAccountDetailsMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("1"));
    }

    /**
     * Executes the search, and checks that the default entity is not returned.
     */
    private void defaultVirtualAccountDetailsShouldNotBeFound(String filter) throws Exception {
        restVirtualAccountDetailsMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty());

        // Check, that the count call also returns 0
        restVirtualAccountDetailsMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("0"));
    }

    @Test
    @Transactional
    void getNonExistingVirtualAccountDetails() throws Exception {
        // Get the virtualAccountDetails
        restVirtualAccountDetailsMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putNewVirtualAccountDetails() throws Exception {
        // Initialize the database
        virtualAccountDetailsRepository.saveAndFlush(virtualAccountDetails);

        int databaseSizeBeforeUpdate = virtualAccountDetailsRepository.findAll().size();

        // Update the virtualAccountDetails
        VirtualAccountDetails updatedVirtualAccountDetails = virtualAccountDetailsRepository.findById(virtualAccountDetails.getId()).get();
        // Disconnect from session so that the updates on updatedVirtualAccountDetails are not directly saved in db
        em.detach(updatedVirtualAccountDetails);
        updatedVirtualAccountDetails
            .params(UPDATED_PARAMS)
            .account(UPDATED_ACCOUNT)
            .active(UPDATED_ACTIVE)
            .mfo(UPDATED_MFO)
            .type(UPDATED_TYPE);
        VirtualAccountDetailsDTO virtualAccountDetailsDTO = virtualAccountDetailsMapper.toDto(updatedVirtualAccountDetails);

        restVirtualAccountDetailsMockMvc
            .perform(
                put(ENTITY_API_URL_ID, virtualAccountDetailsDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(virtualAccountDetailsDTO))
            )
            .andExpect(status().isOk());

        // Validate the VirtualAccountDetails in the database
        List<VirtualAccountDetails> virtualAccountDetailsList = virtualAccountDetailsRepository.findAll();
        assertThat(virtualAccountDetailsList).hasSize(databaseSizeBeforeUpdate);
        VirtualAccountDetails testVirtualAccountDetails = virtualAccountDetailsList.get(virtualAccountDetailsList.size() - 1);
        assertThat(testVirtualAccountDetails.getParams()).isEqualTo(UPDATED_PARAMS);
        assertThat(testVirtualAccountDetails.getAccount()).isEqualTo(UPDATED_ACCOUNT);
        assertThat(testVirtualAccountDetails.getActive()).isEqualTo(UPDATED_ACTIVE);
        assertThat(testVirtualAccountDetails.getMfo()).isEqualTo(UPDATED_MFO);
        assertThat(testVirtualAccountDetails.getType()).isEqualTo(UPDATED_TYPE);
    }

    @Test
    @Transactional
    void putNonExistingVirtualAccountDetails() throws Exception {
        int databaseSizeBeforeUpdate = virtualAccountDetailsRepository.findAll().size();
        virtualAccountDetails.setId(count.incrementAndGet());

        // Create the VirtualAccountDetails
        VirtualAccountDetailsDTO virtualAccountDetailsDTO = virtualAccountDetailsMapper.toDto(virtualAccountDetails);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restVirtualAccountDetailsMockMvc
            .perform(
                put(ENTITY_API_URL_ID, virtualAccountDetailsDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(virtualAccountDetailsDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the VirtualAccountDetails in the database
        List<VirtualAccountDetails> virtualAccountDetailsList = virtualAccountDetailsRepository.findAll();
        assertThat(virtualAccountDetailsList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchVirtualAccountDetails() throws Exception {
        int databaseSizeBeforeUpdate = virtualAccountDetailsRepository.findAll().size();
        virtualAccountDetails.setId(count.incrementAndGet());

        // Create the VirtualAccountDetails
        VirtualAccountDetailsDTO virtualAccountDetailsDTO = virtualAccountDetailsMapper.toDto(virtualAccountDetails);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restVirtualAccountDetailsMockMvc
            .perform(
                put(ENTITY_API_URL_ID, count.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(virtualAccountDetailsDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the VirtualAccountDetails in the database
        List<VirtualAccountDetails> virtualAccountDetailsList = virtualAccountDetailsRepository.findAll();
        assertThat(virtualAccountDetailsList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamVirtualAccountDetails() throws Exception {
        int databaseSizeBeforeUpdate = virtualAccountDetailsRepository.findAll().size();
        virtualAccountDetails.setId(count.incrementAndGet());

        // Create the VirtualAccountDetails
        VirtualAccountDetailsDTO virtualAccountDetailsDTO = virtualAccountDetailsMapper.toDto(virtualAccountDetails);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restVirtualAccountDetailsMockMvc
            .perform(
                put(ENTITY_API_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(virtualAccountDetailsDTO))
            )
            .andExpect(status().isMethodNotAllowed());

        // Validate the VirtualAccountDetails in the database
        List<VirtualAccountDetails> virtualAccountDetailsList = virtualAccountDetailsRepository.findAll();
        assertThat(virtualAccountDetailsList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateVirtualAccountDetailsWithPatch() throws Exception {
        // Initialize the database
        virtualAccountDetailsRepository.saveAndFlush(virtualAccountDetails);

        int databaseSizeBeforeUpdate = virtualAccountDetailsRepository.findAll().size();

        // Update the virtualAccountDetails using partial update
        VirtualAccountDetails partialUpdatedVirtualAccountDetails = new VirtualAccountDetails();
        partialUpdatedVirtualAccountDetails.setId(virtualAccountDetails.getId());

        partialUpdatedVirtualAccountDetails.params(UPDATED_PARAMS).active(UPDATED_ACTIVE).type(UPDATED_TYPE);

        restVirtualAccountDetailsMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedVirtualAccountDetails.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedVirtualAccountDetails))
            )
            .andExpect(status().isOk());

        // Validate the VirtualAccountDetails in the database
        List<VirtualAccountDetails> virtualAccountDetailsList = virtualAccountDetailsRepository.findAll();
        assertThat(virtualAccountDetailsList).hasSize(databaseSizeBeforeUpdate);
        VirtualAccountDetails testVirtualAccountDetails = virtualAccountDetailsList.get(virtualAccountDetailsList.size() - 1);
        assertThat(testVirtualAccountDetails.getParams()).isEqualTo(UPDATED_PARAMS);
        assertThat(testVirtualAccountDetails.getAccount()).isEqualTo(DEFAULT_ACCOUNT);
        assertThat(testVirtualAccountDetails.getActive()).isEqualTo(UPDATED_ACTIVE);
        assertThat(testVirtualAccountDetails.getMfo()).isEqualTo(DEFAULT_MFO);
        assertThat(testVirtualAccountDetails.getType()).isEqualTo(UPDATED_TYPE);
    }

    @Test
    @Transactional
    void fullUpdateVirtualAccountDetailsWithPatch() throws Exception {
        // Initialize the database
        virtualAccountDetailsRepository.saveAndFlush(virtualAccountDetails);

        int databaseSizeBeforeUpdate = virtualAccountDetailsRepository.findAll().size();

        // Update the virtualAccountDetails using partial update
        VirtualAccountDetails partialUpdatedVirtualAccountDetails = new VirtualAccountDetails();
        partialUpdatedVirtualAccountDetails.setId(virtualAccountDetails.getId());

        partialUpdatedVirtualAccountDetails
            .params(UPDATED_PARAMS)
            .account(UPDATED_ACCOUNT)
            .active(UPDATED_ACTIVE)
            .mfo(UPDATED_MFO)
            .type(UPDATED_TYPE);

        restVirtualAccountDetailsMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedVirtualAccountDetails.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedVirtualAccountDetails))
            )
            .andExpect(status().isOk());

        // Validate the VirtualAccountDetails in the database
        List<VirtualAccountDetails> virtualAccountDetailsList = virtualAccountDetailsRepository.findAll();
        assertThat(virtualAccountDetailsList).hasSize(databaseSizeBeforeUpdate);
        VirtualAccountDetails testVirtualAccountDetails = virtualAccountDetailsList.get(virtualAccountDetailsList.size() - 1);
        assertThat(testVirtualAccountDetails.getParams()).isEqualTo(UPDATED_PARAMS);
        assertThat(testVirtualAccountDetails.getAccount()).isEqualTo(UPDATED_ACCOUNT);
        assertThat(testVirtualAccountDetails.getActive()).isEqualTo(UPDATED_ACTIVE);
        assertThat(testVirtualAccountDetails.getMfo()).isEqualTo(UPDATED_MFO);
        assertThat(testVirtualAccountDetails.getType()).isEqualTo(UPDATED_TYPE);
    }

    @Test
    @Transactional
    void patchNonExistingVirtualAccountDetails() throws Exception {
        int databaseSizeBeforeUpdate = virtualAccountDetailsRepository.findAll().size();
        virtualAccountDetails.setId(count.incrementAndGet());

        // Create the VirtualAccountDetails
        VirtualAccountDetailsDTO virtualAccountDetailsDTO = virtualAccountDetailsMapper.toDto(virtualAccountDetails);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restVirtualAccountDetailsMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, virtualAccountDetailsDTO.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(virtualAccountDetailsDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the VirtualAccountDetails in the database
        List<VirtualAccountDetails> virtualAccountDetailsList = virtualAccountDetailsRepository.findAll();
        assertThat(virtualAccountDetailsList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchVirtualAccountDetails() throws Exception {
        int databaseSizeBeforeUpdate = virtualAccountDetailsRepository.findAll().size();
        virtualAccountDetails.setId(count.incrementAndGet());

        // Create the VirtualAccountDetails
        VirtualAccountDetailsDTO virtualAccountDetailsDTO = virtualAccountDetailsMapper.toDto(virtualAccountDetails);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restVirtualAccountDetailsMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, count.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(virtualAccountDetailsDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the VirtualAccountDetails in the database
        List<VirtualAccountDetails> virtualAccountDetailsList = virtualAccountDetailsRepository.findAll();
        assertThat(virtualAccountDetailsList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamVirtualAccountDetails() throws Exception {
        int databaseSizeBeforeUpdate = virtualAccountDetailsRepository.findAll().size();
        virtualAccountDetails.setId(count.incrementAndGet());

        // Create the VirtualAccountDetails
        VirtualAccountDetailsDTO virtualAccountDetailsDTO = virtualAccountDetailsMapper.toDto(virtualAccountDetails);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restVirtualAccountDetailsMockMvc
            .perform(
                patch(ENTITY_API_URL)
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(virtualAccountDetailsDTO))
            )
            .andExpect(status().isMethodNotAllowed());

        // Validate the VirtualAccountDetails in the database
        List<VirtualAccountDetails> virtualAccountDetailsList = virtualAccountDetailsRepository.findAll();
        assertThat(virtualAccountDetailsList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteVirtualAccountDetails() throws Exception {
        // Initialize the database
        virtualAccountDetailsRepository.saveAndFlush(virtualAccountDetails);

        int databaseSizeBeforeDelete = virtualAccountDetailsRepository.findAll().size();

        // Delete the virtualAccountDetails
        restVirtualAccountDetailsMockMvc
            .perform(delete(ENTITY_API_URL_ID, virtualAccountDetails.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        List<VirtualAccountDetails> virtualAccountDetailsList = virtualAccountDetailsRepository.findAll();
        assertThat(virtualAccountDetailsList).hasSize(databaseSizeBeforeDelete - 1);
    }
}
