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
import uz.paykap.domain.VirtualAccount;
import uz.paykap.domain.VirtualAccountDetails;
import uz.paykap.domain.enumeration.VirtualAccountType;
import uz.paykap.repository.VirtualAccountDetailsRepository;
import uz.paykap.service.VirtualAccountDetailsQueryService;
import uz.paykap.service.VirtualAccountDetailsService;
import uz.paykap.service.dto.VirtualAccountDetailsDTO;
import uz.paykap.service.mapper.VirtualAccountDetailsMapper;
import uz.paykap.web.rest.errors.ExceptionTranslator;

/**
 * Test class for the VirtualAccountDetailsResource REST controller.
 *
 * @see VirtualAccountDetailsResource
 */
@RunWith(SpringRunner.class)
@IntegrationTest
public class VirtualAccountDetailsResourceIntTest {

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

    @Autowired
    private VirtualAccountDetailsRepository virtualAccountDetailsRepository;

    @Autowired
    private VirtualAccountDetailsMapper virtualAccountDetailsMapper;

    @Autowired
    private VirtualAccountDetailsService virtualAccountDetailsService;

    @Autowired
    private VirtualAccountDetailsQueryService virtualAccountDetailsQueryService;

    @Autowired
    private MappingJackson2HttpMessageConverter jacksonMessageConverter;

    @Autowired
    private PageableHandlerMethodArgumentResolver pageableArgumentResolver;

    @Autowired
    private ExceptionTranslator exceptionTranslator;

    @Autowired
    private EntityManager em;

    private MockMvc restVirtualAccountDetailsMockMvc;

    private VirtualAccountDetails virtualAccountDetails;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        final VirtualAccountDetailsResource virtualAccountDetailsResource = new VirtualAccountDetailsResource(
            virtualAccountDetailsService,
            virtualAccountDetailsRepository,
            virtualAccountDetailsQueryService
        );
        this.restVirtualAccountDetailsMockMvc =
            MockMvcBuilders
                .standaloneSetup(virtualAccountDetailsResource)
                .setCustomArgumentResolvers(pageableArgumentResolver)
                .setControllerAdvice(exceptionTranslator)
                .setConversionService(createFormattingConversionService())
                .setMessageConverters(jacksonMessageConverter)
                .build();
    }

    /**
     * Create an entity for this test.
     * <p>
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

    @Before
    public void initTest() {
        virtualAccountDetails = createEntity(em);
    }

    @Test
    @Transactional
    public void createVirtualAccountDetails() throws Exception {
        int databaseSizeBeforeCreate = virtualAccountDetailsRepository.findAll().size();

        // Create the VirtualAccountDetails
        VirtualAccountDetailsDTO virtualAccountDetailsDTO = virtualAccountDetailsMapper.toDto(virtualAccountDetails);
        restVirtualAccountDetailsMockMvc
            .perform(
                post("/api/virtual-account-details")
                    .contentType(TestUtil.APPLICATION_JSON_UTF8)
                    .content(TestUtil.convertObjectToJsonBytes(virtualAccountDetailsDTO))
            )
            .andExpect(status().isCreated());

        // Validate the VirtualAccountDetails in the database
        List<VirtualAccountDetails> virtualAccountDetailsList = virtualAccountDetailsRepository.findAll();
        assertThat(virtualAccountDetailsList).hasSize(databaseSizeBeforeCreate + 1);
        VirtualAccountDetails testVirtualAccountDetails = virtualAccountDetailsList.get(virtualAccountDetailsList.size() - 1);
        assertThat(testVirtualAccountDetails.getParams()).isEqualTo(DEFAULT_PARAMS);
        assertThat(testVirtualAccountDetails.getAccount()).isEqualTo(DEFAULT_ACCOUNT);
        assertThat(testVirtualAccountDetails.isActive()).isEqualTo(DEFAULT_ACTIVE);
        assertThat(testVirtualAccountDetails.getMfo()).isEqualTo(DEFAULT_MFO);
        assertThat(testVirtualAccountDetails.getType()).isEqualTo(DEFAULT_TYPE);
    }

    @Test
    @Transactional
    public void createVirtualAccountDetailsWithExistingId() throws Exception {
        int databaseSizeBeforeCreate = virtualAccountDetailsRepository.findAll().size();

        // Create the VirtualAccountDetails with an existing ID
        virtualAccountDetails.setId(1L);
        VirtualAccountDetailsDTO virtualAccountDetailsDTO = virtualAccountDetailsMapper.toDto(virtualAccountDetails);

        // An entity with an existing ID cannot be created, so this API call must fail
        restVirtualAccountDetailsMockMvc
            .perform(
                post("/api/virtual-account-details")
                    .contentType(TestUtil.APPLICATION_JSON_UTF8)
                    .content(TestUtil.convertObjectToJsonBytes(virtualAccountDetailsDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the VirtualAccountDetails in the database
        List<VirtualAccountDetails> virtualAccountDetailsList = virtualAccountDetailsRepository.findAll();
        assertThat(virtualAccountDetailsList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    public void checkParamsIsRequired() throws Exception {
        int databaseSizeBeforeTest = virtualAccountDetailsRepository.findAll().size();
        // set the field null
        virtualAccountDetails.setParams(null);

        // Create the VirtualAccountDetails, which fails.
        VirtualAccountDetailsDTO virtualAccountDetailsDTO = virtualAccountDetailsMapper.toDto(virtualAccountDetails);

        restVirtualAccountDetailsMockMvc
            .perform(
                post("/api/virtual-account-details")
                    .contentType(TestUtil.APPLICATION_JSON_UTF8)
                    .content(TestUtil.convertObjectToJsonBytes(virtualAccountDetailsDTO))
            )
            .andExpect(status().isBadRequest());

        List<VirtualAccountDetails> virtualAccountDetailsList = virtualAccountDetailsRepository.findAll();
        assertThat(virtualAccountDetailsList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkAccountIsRequired() throws Exception {
        int databaseSizeBeforeTest = virtualAccountDetailsRepository.findAll().size();
        // set the field null
        virtualAccountDetails.setAccount(null);

        // Create the VirtualAccountDetails, which fails.
        VirtualAccountDetailsDTO virtualAccountDetailsDTO = virtualAccountDetailsMapper.toDto(virtualAccountDetails);

        restVirtualAccountDetailsMockMvc
            .perform(
                post("/api/virtual-account-details")
                    .contentType(TestUtil.APPLICATION_JSON_UTF8)
                    .content(TestUtil.convertObjectToJsonBytes(virtualAccountDetailsDTO))
            )
            .andExpect(status().isBadRequest());

        List<VirtualAccountDetails> virtualAccountDetailsList = virtualAccountDetailsRepository.findAll();
        assertThat(virtualAccountDetailsList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkActiveIsRequired() throws Exception {
        int databaseSizeBeforeTest = virtualAccountDetailsRepository.findAll().size();
        // set the field null
        virtualAccountDetails.setActive(null);

        // Create the VirtualAccountDetails, which fails.
        VirtualAccountDetailsDTO virtualAccountDetailsDTO = virtualAccountDetailsMapper.toDto(virtualAccountDetails);

        restVirtualAccountDetailsMockMvc
            .perform(
                post("/api/virtual-account-details")
                    .contentType(TestUtil.APPLICATION_JSON_UTF8)
                    .content(TestUtil.convertObjectToJsonBytes(virtualAccountDetailsDTO))
            )
            .andExpect(status().isBadRequest());

        List<VirtualAccountDetails> virtualAccountDetailsList = virtualAccountDetailsRepository.findAll();
        assertThat(virtualAccountDetailsList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkMfoIsRequired() throws Exception {
        int databaseSizeBeforeTest = virtualAccountDetailsRepository.findAll().size();
        // set the field null
        virtualAccountDetails.setMfo(null);

        // Create the VirtualAccountDetails, which fails.
        VirtualAccountDetailsDTO virtualAccountDetailsDTO = virtualAccountDetailsMapper.toDto(virtualAccountDetails);

        restVirtualAccountDetailsMockMvc
            .perform(
                post("/api/virtual-account-details")
                    .contentType(TestUtil.APPLICATION_JSON_UTF8)
                    .content(TestUtil.convertObjectToJsonBytes(virtualAccountDetailsDTO))
            )
            .andExpect(status().isBadRequest());

        List<VirtualAccountDetails> virtualAccountDetailsList = virtualAccountDetailsRepository.findAll();
        assertThat(virtualAccountDetailsList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void getAllVirtualAccountDetails() throws Exception {
        // Initialize the database
        virtualAccountDetailsRepository.saveAndFlush(virtualAccountDetails);

        // Get all the virtualAccountDetailsList
        restVirtualAccountDetailsMockMvc
            .perform(get("/api/virtual-account-details?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(virtualAccountDetails.getId().intValue())))
            .andExpect(jsonPath("$.[*].params").value(hasItem(DEFAULT_PARAMS.toString())))
            .andExpect(jsonPath("$.[*].account").value(hasItem(DEFAULT_ACCOUNT.toString())))
            .andExpect(jsonPath("$.[*].active").value(hasItem(DEFAULT_ACTIVE.booleanValue())))
            .andExpect(jsonPath("$.[*].mfo").value(hasItem(DEFAULT_MFO.toString())))
            .andExpect(jsonPath("$.[*].type").value(hasItem(DEFAULT_TYPE.toString())));
    }

    @Test
    @Transactional
    public void getVirtualAccountDetails() throws Exception {
        // Initialize the database
        virtualAccountDetailsRepository.saveAndFlush(virtualAccountDetails);

        // Get the virtualAccountDetails
        restVirtualAccountDetailsMockMvc
            .perform(get("/api/virtual-account-details/{id}", virtualAccountDetails.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.id").value(virtualAccountDetails.getId().intValue()))
            .andExpect(jsonPath("$.params").value(DEFAULT_PARAMS.toString()))
            .andExpect(jsonPath("$.account").value(DEFAULT_ACCOUNT.toString()))
            .andExpect(jsonPath("$.active").value(DEFAULT_ACTIVE.booleanValue()))
            .andExpect(jsonPath("$.mfo").value(DEFAULT_MFO.toString()))
            .andExpect(jsonPath("$.type").value(DEFAULT_TYPE.toString()));
    }

    @Test
    @Transactional
    public void getAllVirtualAccountDetailsByParamsIsEqualToSomething() throws Exception {
        // Initialize the database
        virtualAccountDetailsRepository.saveAndFlush(virtualAccountDetails);

        // Get all the virtualAccountDetailsList where params equals to DEFAULT_PARAMS
        defaultVirtualAccountDetailsShouldBeFound("params.equals=" + DEFAULT_PARAMS);

        // Get all the virtualAccountDetailsList where params equals to UPDATED_PARAMS
        defaultVirtualAccountDetailsShouldNotBeFound("params.equals=" + UPDATED_PARAMS);
    }

    @Test
    @Transactional
    public void getAllVirtualAccountDetailsByParamsIsInShouldWork() throws Exception {
        // Initialize the database
        virtualAccountDetailsRepository.saveAndFlush(virtualAccountDetails);

        // Get all the virtualAccountDetailsList where params in DEFAULT_PARAMS or UPDATED_PARAMS
        defaultVirtualAccountDetailsShouldBeFound("params.in=" + DEFAULT_PARAMS + "," + UPDATED_PARAMS);

        // Get all the virtualAccountDetailsList where params equals to UPDATED_PARAMS
        defaultVirtualAccountDetailsShouldNotBeFound("params.in=" + UPDATED_PARAMS);
    }

    @Test
    @Transactional
    public void getAllVirtualAccountDetailsByParamsIsNullOrNotNull() throws Exception {
        // Initialize the database
        virtualAccountDetailsRepository.saveAndFlush(virtualAccountDetails);

        // Get all the virtualAccountDetailsList where params is not null
        defaultVirtualAccountDetailsShouldBeFound("params.specified=true");

        // Get all the virtualAccountDetailsList where params is null
        defaultVirtualAccountDetailsShouldNotBeFound("params.specified=false");
    }

    @Test
    @Transactional
    public void getAllVirtualAccountDetailsByAccountIsEqualToSomething() throws Exception {
        // Initialize the database
        virtualAccountDetailsRepository.saveAndFlush(virtualAccountDetails);

        // Get all the virtualAccountDetailsList where account equals to DEFAULT_ACCOUNT
        defaultVirtualAccountDetailsShouldBeFound("account.equals=" + DEFAULT_ACCOUNT);

        // Get all the virtualAccountDetailsList where account equals to UPDATED_ACCOUNT
        defaultVirtualAccountDetailsShouldNotBeFound("account.equals=" + UPDATED_ACCOUNT);
    }

    @Test
    @Transactional
    public void getAllVirtualAccountDetailsByAccountIsInShouldWork() throws Exception {
        // Initialize the database
        virtualAccountDetailsRepository.saveAndFlush(virtualAccountDetails);

        // Get all the virtualAccountDetailsList where account in DEFAULT_ACCOUNT or UPDATED_ACCOUNT
        defaultVirtualAccountDetailsShouldBeFound("account.in=" + DEFAULT_ACCOUNT + "," + UPDATED_ACCOUNT);

        // Get all the virtualAccountDetailsList where account equals to UPDATED_ACCOUNT
        defaultVirtualAccountDetailsShouldNotBeFound("account.in=" + UPDATED_ACCOUNT);
    }

    @Test
    @Transactional
    public void getAllVirtualAccountDetailsByAccountIsNullOrNotNull() throws Exception {
        // Initialize the database
        virtualAccountDetailsRepository.saveAndFlush(virtualAccountDetails);

        // Get all the virtualAccountDetailsList where account is not null
        defaultVirtualAccountDetailsShouldBeFound("account.specified=true");

        // Get all the virtualAccountDetailsList where account is null
        defaultVirtualAccountDetailsShouldNotBeFound("account.specified=false");
    }

    @Test
    @Transactional
    public void getAllVirtualAccountDetailsByActiveIsEqualToSomething() throws Exception {
        // Initialize the database
        virtualAccountDetailsRepository.saveAndFlush(virtualAccountDetails);

        // Get all the virtualAccountDetailsList where active equals to DEFAULT_ACTIVE
        defaultVirtualAccountDetailsShouldBeFound("active.equals=" + DEFAULT_ACTIVE);

        // Get all the virtualAccountDetailsList where active equals to UPDATED_ACTIVE
        defaultVirtualAccountDetailsShouldNotBeFound("active.equals=" + UPDATED_ACTIVE);
    }

    @Test
    @Transactional
    public void getAllVirtualAccountDetailsByActiveIsInShouldWork() throws Exception {
        // Initialize the database
        virtualAccountDetailsRepository.saveAndFlush(virtualAccountDetails);

        // Get all the virtualAccountDetailsList where active in DEFAULT_ACTIVE or UPDATED_ACTIVE
        defaultVirtualAccountDetailsShouldBeFound("active.in=" + DEFAULT_ACTIVE + "," + UPDATED_ACTIVE);

        // Get all the virtualAccountDetailsList where active equals to UPDATED_ACTIVE
        defaultVirtualAccountDetailsShouldNotBeFound("active.in=" + UPDATED_ACTIVE);
    }

    @Test
    @Transactional
    public void getAllVirtualAccountDetailsByActiveIsNullOrNotNull() throws Exception {
        // Initialize the database
        virtualAccountDetailsRepository.saveAndFlush(virtualAccountDetails);

        // Get all the virtualAccountDetailsList where active is not null
        defaultVirtualAccountDetailsShouldBeFound("active.specified=true");

        // Get all the virtualAccountDetailsList where active is null
        defaultVirtualAccountDetailsShouldNotBeFound("active.specified=false");
    }

    @Test
    @Transactional
    public void getAllVirtualAccountDetailsByMfoIsEqualToSomething() throws Exception {
        // Initialize the database
        virtualAccountDetailsRepository.saveAndFlush(virtualAccountDetails);

        // Get all the virtualAccountDetailsList where mfo equals to DEFAULT_MFO
        defaultVirtualAccountDetailsShouldBeFound("mfo.equals=" + DEFAULT_MFO);

        // Get all the virtualAccountDetailsList where mfo equals to UPDATED_MFO
        defaultVirtualAccountDetailsShouldNotBeFound("mfo.equals=" + UPDATED_MFO);
    }

    @Test
    @Transactional
    public void getAllVirtualAccountDetailsByMfoIsInShouldWork() throws Exception {
        // Initialize the database
        virtualAccountDetailsRepository.saveAndFlush(virtualAccountDetails);

        // Get all the virtualAccountDetailsList where mfo in DEFAULT_MFO or UPDATED_MFO
        defaultVirtualAccountDetailsShouldBeFound("mfo.in=" + DEFAULT_MFO + "," + UPDATED_MFO);

        // Get all the virtualAccountDetailsList where mfo equals to UPDATED_MFO
        defaultVirtualAccountDetailsShouldNotBeFound("mfo.in=" + UPDATED_MFO);
    }

    @Test
    @Transactional
    public void getAllVirtualAccountDetailsByMfoIsNullOrNotNull() throws Exception {
        // Initialize the database
        virtualAccountDetailsRepository.saveAndFlush(virtualAccountDetails);

        // Get all the virtualAccountDetailsList where mfo is not null
        defaultVirtualAccountDetailsShouldBeFound("mfo.specified=true");

        // Get all the virtualAccountDetailsList where mfo is null
        defaultVirtualAccountDetailsShouldNotBeFound("mfo.specified=false");
    }

    @Test
    @Transactional
    public void getAllVirtualAccountDetailsByTypeIsEqualToSomething() throws Exception {
        // Initialize the database
        virtualAccountDetailsRepository.saveAndFlush(virtualAccountDetails);

        // Get all the virtualAccountDetailsList where type equals to DEFAULT_TYPE
        defaultVirtualAccountDetailsShouldBeFound("type.equals=" + DEFAULT_TYPE);

        // Get all the virtualAccountDetailsList where type equals to UPDATED_TYPE
        defaultVirtualAccountDetailsShouldNotBeFound("type.equals=" + UPDATED_TYPE);
    }

    @Test
    @Transactional
    public void getAllVirtualAccountDetailsByTypeIsInShouldWork() throws Exception {
        // Initialize the database
        virtualAccountDetailsRepository.saveAndFlush(virtualAccountDetails);

        // Get all the virtualAccountDetailsList where type in DEFAULT_TYPE or UPDATED_TYPE
        defaultVirtualAccountDetailsShouldBeFound("type.in=" + DEFAULT_TYPE + "," + UPDATED_TYPE);

        // Get all the virtualAccountDetailsList where type equals to UPDATED_TYPE
        defaultVirtualAccountDetailsShouldNotBeFound("type.in=" + UPDATED_TYPE);
    }

    @Test
    @Transactional
    public void getAllVirtualAccountDetailsByTypeIsNullOrNotNull() throws Exception {
        // Initialize the database
        virtualAccountDetailsRepository.saveAndFlush(virtualAccountDetails);

        // Get all the virtualAccountDetailsList where type is not null
        defaultVirtualAccountDetailsShouldBeFound("type.specified=true");

        // Get all the virtualAccountDetailsList where type is null
        defaultVirtualAccountDetailsShouldNotBeFound("type.specified=false");
    }

    @Test
    @Transactional
    public void getAllVirtualAccountDetailsByVirtualAccountIsEqualToSomething() throws Exception {
        // Initialize the database
        VirtualAccount virtualAccount = VirtualAccountResourceIntTest.createEntity(em);
        em.persist(virtualAccount);
        em.flush();
        virtualAccountDetails.setVirtualAccount(virtualAccount);
        virtualAccountDetailsRepository.saveAndFlush(virtualAccountDetails);
        Long virtualAccountId = virtualAccount.getId();

        // Get all the virtualAccountDetailsList where virtualAccount equals to virtualAccountId
        defaultVirtualAccountDetailsShouldBeFound("virtualAccountId.equals=" + virtualAccountId);

        // Get all the virtualAccountDetailsList where virtualAccount equals to virtualAccountId + 1
        defaultVirtualAccountDetailsShouldNotBeFound("virtualAccountId.equals=" + (virtualAccountId + 1));
    }

    /**
     * Executes the search, and checks that the default entity is returned
     */
    private void defaultVirtualAccountDetailsShouldBeFound(String filter) throws Exception {
        restVirtualAccountDetailsMockMvc
            .perform(get("/api/virtual-account-details?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(virtualAccountDetails.getId().intValue())))
            .andExpect(jsonPath("$.[*].params").value(hasItem(DEFAULT_PARAMS.toString())))
            .andExpect(jsonPath("$.[*].account").value(hasItem(DEFAULT_ACCOUNT.toString())))
            .andExpect(jsonPath("$.[*].active").value(hasItem(DEFAULT_ACTIVE.booleanValue())))
            .andExpect(jsonPath("$.[*].mfo").value(hasItem(DEFAULT_MFO.toString())))
            .andExpect(jsonPath("$.[*].type").value(hasItem(DEFAULT_TYPE.toString())));
    }

    /**
     * Executes the search, and checks that the default entity is not returned
     */
    private void defaultVirtualAccountDetailsShouldNotBeFound(String filter) throws Exception {
        restVirtualAccountDetailsMockMvc
            .perform(get("/api/virtual-account-details?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    @Transactional
    public void getNonExistingVirtualAccountDetails() throws Exception {
        // Get the virtualAccountDetails
        restVirtualAccountDetailsMockMvc.perform(get("/api/virtual-account-details/{id}", Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updateVirtualAccountDetails() throws Exception {
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
                put("/api/virtual-account-details")
                    .contentType(TestUtil.APPLICATION_JSON_UTF8)
                    .content(TestUtil.convertObjectToJsonBytes(virtualAccountDetailsDTO))
            )
            .andExpect(status().isOk());

        // Validate the VirtualAccountDetails in the database
        List<VirtualAccountDetails> virtualAccountDetailsList = virtualAccountDetailsRepository.findAll();
        assertThat(virtualAccountDetailsList).hasSize(databaseSizeBeforeUpdate);
        VirtualAccountDetails testVirtualAccountDetails = virtualAccountDetailsList.get(virtualAccountDetailsList.size() - 1);
        assertThat(testVirtualAccountDetails.getParams()).isEqualTo(UPDATED_PARAMS);
        assertThat(testVirtualAccountDetails.getAccount()).isEqualTo(UPDATED_ACCOUNT);
        assertThat(testVirtualAccountDetails.isActive()).isEqualTo(UPDATED_ACTIVE);
        assertThat(testVirtualAccountDetails.getMfo()).isEqualTo(UPDATED_MFO);
        assertThat(testVirtualAccountDetails.getType()).isEqualTo(UPDATED_TYPE);
    }

    @Test
    @Transactional
    public void updateNonExistingVirtualAccountDetails() throws Exception {
        int databaseSizeBeforeUpdate = virtualAccountDetailsRepository.findAll().size();

        // Create the VirtualAccountDetails
        VirtualAccountDetailsDTO virtualAccountDetailsDTO = virtualAccountDetailsMapper.toDto(virtualAccountDetails);

        // If the entity doesn't have an ID, it will be created instead of just being updated
        restVirtualAccountDetailsMockMvc
            .perform(
                put("/api/virtual-account-details")
                    .contentType(TestUtil.APPLICATION_JSON_UTF8)
                    .content(TestUtil.convertObjectToJsonBytes(virtualAccountDetailsDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the VirtualAccountDetails in the database
        List<VirtualAccountDetails> virtualAccountDetailsList = virtualAccountDetailsRepository.findAll();
        assertThat(virtualAccountDetailsList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    public void deleteVirtualAccountDetails() throws Exception {
        // Initialize the database
        virtualAccountDetailsRepository.saveAndFlush(virtualAccountDetails);

        int databaseSizeBeforeDelete = virtualAccountDetailsRepository.findAll().size();

        // Get the virtualAccountDetails
        restVirtualAccountDetailsMockMvc
            .perform(delete("/api/virtual-account-details/{id}", virtualAccountDetails.getId()).accept(TestUtil.APPLICATION_JSON_UTF8))
            .andExpect(status().isOk());

        // Validate the database is empty
        List<VirtualAccountDetails> virtualAccountDetailsList = virtualAccountDetailsRepository.findAll();
        assertThat(virtualAccountDetailsList).hasSize(databaseSizeBeforeDelete - 1);
    }

    @Test
    @Transactional
    public void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(VirtualAccountDetails.class);
        VirtualAccountDetails virtualAccountDetails1 = new VirtualAccountDetails();
        virtualAccountDetails1.setId(1L);
        VirtualAccountDetails virtualAccountDetails2 = new VirtualAccountDetails();
        virtualAccountDetails2.setId(virtualAccountDetails1.getId());
        assertThat(virtualAccountDetails1).isEqualTo(virtualAccountDetails2);
        virtualAccountDetails2.setId(2L);
        assertThat(virtualAccountDetails1).isNotEqualTo(virtualAccountDetails2);
        virtualAccountDetails1.setId(null);
        assertThat(virtualAccountDetails1).isNotEqualTo(virtualAccountDetails2);
    }

    @Test
    @Transactional
    public void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(VirtualAccountDetailsDTO.class);
        VirtualAccountDetailsDTO virtualAccountDetailsDTO1 = new VirtualAccountDetailsDTO();
        virtualAccountDetailsDTO1.setId(1L);
        VirtualAccountDetailsDTO virtualAccountDetailsDTO2 = new VirtualAccountDetailsDTO();
        assertThat(virtualAccountDetailsDTO1).isNotEqualTo(virtualAccountDetailsDTO2);
        virtualAccountDetailsDTO2.setId(virtualAccountDetailsDTO1.getId());
        assertThat(virtualAccountDetailsDTO1).isEqualTo(virtualAccountDetailsDTO2);
        virtualAccountDetailsDTO2.setId(2L);
        assertThat(virtualAccountDetailsDTO1).isNotEqualTo(virtualAccountDetailsDTO2);
        virtualAccountDetailsDTO1.setId(null);
        assertThat(virtualAccountDetailsDTO1).isNotEqualTo(virtualAccountDetailsDTO2);
    }

    @Test
    @Transactional
    public void testEntityFromId() {
        assertThat(virtualAccountDetailsMapper.fromId(42L).getId()).isEqualTo(42);
        assertThat(virtualAccountDetailsMapper.fromId(null)).isNull();
    }
}
