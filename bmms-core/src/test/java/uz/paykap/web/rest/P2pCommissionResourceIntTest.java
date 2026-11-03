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
import uz.paykap.domain.P2pCommission;
import uz.paykap.repository.P2pCommissionRepository;
import uz.paykap.service.P2pCommissionQueryService;
import uz.paykap.service.P2pCommissionService;
import uz.paykap.service.dto.P2pCommissionCriteria;
import uz.paykap.web.rest.errors.ExceptionTranslator;

/**
 * Test class for the P2pCommissionResource REST controller.
 *
 * @see P2pCommissionResource
 */
@RunWith(SpringRunner.class)
@IntegrationTest
public class P2pCommissionResourceIntTest {

    private static final String DEFAULT_NAME = "AAAAAAAAAA";
    private static final String UPDATED_NAME = "BBBBBBBBBB";

    private static final Double DEFAULT_UP = 0D;
    private static final Double UPDATED_UP = 1D;

    private static final Double DEFAULT_DOWN = 0D;
    private static final Double UPDATED_DOWN = 1D;

    private static final Double DEFAULT_FEE = 0D;
    private static final Double UPDATED_FEE = 1D;

    private static final String DEFAULT_DESCRIPTION = "AAAAAAAAAA";
    private static final String UPDATED_DESCRIPTION = "BBBBBBBBBB";

    @Autowired
    private P2pCommissionRepository p2pCommissionRepository;

    @Autowired
    private P2pCommissionService p2pCommissionService;

    @Autowired
    private P2pCommissionQueryService p2pCommissionQueryService;

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

    private MockMvc restP2pCommissionMockMvc;

    private P2pCommission p2pCommission;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        final P2pCommissionResource p2pCommissionResource = new P2pCommissionResource(
            p2pCommissionService,
            p2pCommissionRepository,
            p2pCommissionQueryService
        );
        this.restP2pCommissionMockMvc =
            MockMvcBuilders
                .standaloneSetup(p2pCommissionResource)
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
    public static P2pCommission createEntity(EntityManager em) {
        P2pCommission p2pCommission = new P2pCommission()
            .name(DEFAULT_NAME)
            .up(DEFAULT_UP)
            .down(DEFAULT_DOWN)
            .fee(DEFAULT_FEE)
            .description(DEFAULT_DESCRIPTION);
        return p2pCommission;
    }

    @Before
    public void initTest() {
        p2pCommission = createEntity(em);
    }

    @Test
    @Transactional
    public void createP2pCommission() throws Exception {
        int databaseSizeBeforeCreate = p2pCommissionRepository.findAll().size();

        // Create the P2pCommission
        restP2pCommissionMockMvc
            .perform(
                post("/api/p-2-p-commissions")
                    .contentType(TestUtil.APPLICATION_JSON_UTF8)
                    .content(TestUtil.convertObjectToJsonBytes(p2pCommission))
            )
            .andExpect(status().isCreated());

        // Validate the P2pCommission in the database
        List<P2pCommission> p2pCommissionList = p2pCommissionRepository.findAll();
        assertThat(p2pCommissionList).hasSize(databaseSizeBeforeCreate + 1);
        P2pCommission testP2pCommission = p2pCommissionList.get(p2pCommissionList.size() - 1);
        assertThat(testP2pCommission.getName()).isEqualTo(DEFAULT_NAME);
        assertThat(testP2pCommission.getUp()).isEqualTo(DEFAULT_UP);
        assertThat(testP2pCommission.getDown()).isEqualTo(DEFAULT_DOWN);
        assertThat(testP2pCommission.getFee()).isEqualTo(DEFAULT_FEE);
        assertThat(testP2pCommission.getDescription()).isEqualTo(DEFAULT_DESCRIPTION);
    }

    @Test
    @Transactional
    public void createP2pCommissionWithExistingId() throws Exception {
        int databaseSizeBeforeCreate = p2pCommissionRepository.findAll().size();

        // Create the P2pCommission with an existing ID
        p2pCommission.setId(1L);

        // An entity with an existing ID cannot be created, so this API call must fail
        restP2pCommissionMockMvc
            .perform(
                post("/api/p-2-p-commissions")
                    .contentType(TestUtil.APPLICATION_JSON_UTF8)
                    .content(TestUtil.convertObjectToJsonBytes(p2pCommission))
            )
            .andExpect(status().isBadRequest());

        // Validate the P2pCommission in the database
        List<P2pCommission> p2pCommissionList = p2pCommissionRepository.findAll();
        assertThat(p2pCommissionList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    public void checkNameIsRequired() throws Exception {
        int databaseSizeBeforeTest = p2pCommissionRepository.findAll().size();
        // set the field null
        p2pCommission.setName(null);

        // Create the P2pCommission, which fails.

        restP2pCommissionMockMvc
            .perform(
                post("/api/p-2-p-commissions")
                    .contentType(TestUtil.APPLICATION_JSON_UTF8)
                    .content(TestUtil.convertObjectToJsonBytes(p2pCommission))
            )
            .andExpect(status().isBadRequest());

        List<P2pCommission> p2pCommissionList = p2pCommissionRepository.findAll();
        assertThat(p2pCommissionList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void getAllP2pCommissions() throws Exception {
        // Initialize the database
        p2pCommissionRepository.saveAndFlush(p2pCommission);

        // Get all the p2pCommissionList
        restP2pCommissionMockMvc
            .perform(get("/api/p-2-p-commissions?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(p2pCommission.getId().intValue())))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME.toString())))
            .andExpect(jsonPath("$.[*].up").value(hasItem(DEFAULT_UP.doubleValue())))
            .andExpect(jsonPath("$.[*].down").value(hasItem(DEFAULT_DOWN.doubleValue())))
            .andExpect(jsonPath("$.[*].fee").value(hasItem(DEFAULT_FEE.doubleValue())))
            .andExpect(jsonPath("$.[*].description").value(hasItem(DEFAULT_DESCRIPTION.toString())));
    }

    @Test
    @Transactional
    public void getP2pCommission() throws Exception {
        // Initialize the database
        p2pCommissionRepository.saveAndFlush(p2pCommission);

        // Get the p2pCommission
        restP2pCommissionMockMvc
            .perform(get("/api/p-2-p-commissions/{id}", p2pCommission.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.id").value(p2pCommission.getId().intValue()))
            .andExpect(jsonPath("$.name").value(DEFAULT_NAME.toString()))
            .andExpect(jsonPath("$.up").value(DEFAULT_UP.doubleValue()))
            .andExpect(jsonPath("$.down").value(DEFAULT_DOWN.doubleValue()))
            .andExpect(jsonPath("$.fee").value(DEFAULT_FEE.doubleValue()))
            .andExpect(jsonPath("$.description").value(DEFAULT_DESCRIPTION.toString()));
    }

    @Test
    @Transactional
    public void getAllP2pCommissionsByNameIsEqualToSomething() throws Exception {
        // Initialize the database
        p2pCommissionRepository.saveAndFlush(p2pCommission);

        // Get all the p2pCommissionList where name equals to DEFAULT_NAME
        defaultP2pCommissionShouldBeFound("name.equals=" + DEFAULT_NAME);

        // Get all the p2pCommissionList where name equals to UPDATED_NAME
        defaultP2pCommissionShouldNotBeFound("name.equals=" + UPDATED_NAME);
    }

    @Test
    @Transactional
    public void getAllP2pCommissionsByNameIsInShouldWork() throws Exception {
        // Initialize the database
        p2pCommissionRepository.saveAndFlush(p2pCommission);

        // Get all the p2pCommissionList where name in DEFAULT_NAME or UPDATED_NAME
        defaultP2pCommissionShouldBeFound("name.in=" + DEFAULT_NAME + "," + UPDATED_NAME);

        // Get all the p2pCommissionList where name equals to UPDATED_NAME
        defaultP2pCommissionShouldNotBeFound("name.in=" + UPDATED_NAME);
    }

    @Test
    @Transactional
    public void getAllP2pCommissionsByNameIsNullOrNotNull() throws Exception {
        // Initialize the database
        p2pCommissionRepository.saveAndFlush(p2pCommission);

        // Get all the p2pCommissionList where name is not null
        defaultP2pCommissionShouldBeFound("name.specified=true");

        // Get all the p2pCommissionList where name is null
        defaultP2pCommissionShouldNotBeFound("name.specified=false");
    }

    @Test
    @Transactional
    public void getAllP2pCommissionsByUpIsEqualToSomething() throws Exception {
        // Initialize the database
        p2pCommissionRepository.saveAndFlush(p2pCommission);

        // Get all the p2pCommissionList where up equals to DEFAULT_UP
        defaultP2pCommissionShouldBeFound("up.equals=" + DEFAULT_UP);

        // Get all the p2pCommissionList where up equals to UPDATED_UP
        defaultP2pCommissionShouldNotBeFound("up.equals=" + UPDATED_UP);
    }

    @Test
    @Transactional
    public void getAllP2pCommissionsByUpIsInShouldWork() throws Exception {
        // Initialize the database
        p2pCommissionRepository.saveAndFlush(p2pCommission);

        // Get all the p2pCommissionList where up in DEFAULT_UP or UPDATED_UP
        defaultP2pCommissionShouldBeFound("up.in=" + DEFAULT_UP + "," + UPDATED_UP);

        // Get all the p2pCommissionList where up equals to UPDATED_UP
        defaultP2pCommissionShouldNotBeFound("up.in=" + UPDATED_UP);
    }

    @Test
    @Transactional
    public void getAllP2pCommissionsByUpIsNullOrNotNull() throws Exception {
        // Initialize the database
        p2pCommissionRepository.saveAndFlush(p2pCommission);

        // Get all the p2pCommissionList where up is not null
        defaultP2pCommissionShouldBeFound("up.specified=true");

        // Get all the p2pCommissionList where up is null
        defaultP2pCommissionShouldNotBeFound("up.specified=false");
    }

    @Test
    @Transactional
    public void getAllP2pCommissionsByDownIsEqualToSomething() throws Exception {
        // Initialize the database
        p2pCommissionRepository.saveAndFlush(p2pCommission);

        // Get all the p2pCommissionList where down equals to DEFAULT_DOWN
        defaultP2pCommissionShouldBeFound("down.equals=" + DEFAULT_DOWN);

        // Get all the p2pCommissionList where down equals to UPDATED_DOWN
        defaultP2pCommissionShouldNotBeFound("down.equals=" + UPDATED_DOWN);
    }

    @Test
    @Transactional
    public void getAllP2pCommissionsByDownIsInShouldWork() throws Exception {
        // Initialize the database
        p2pCommissionRepository.saveAndFlush(p2pCommission);

        // Get all the p2pCommissionList where down in DEFAULT_DOWN or UPDATED_DOWN
        defaultP2pCommissionShouldBeFound("down.in=" + DEFAULT_DOWN + "," + UPDATED_DOWN);

        // Get all the p2pCommissionList where down equals to UPDATED_DOWN
        defaultP2pCommissionShouldNotBeFound("down.in=" + UPDATED_DOWN);
    }

    @Test
    @Transactional
    public void getAllP2pCommissionsByDownIsNullOrNotNull() throws Exception {
        // Initialize the database
        p2pCommissionRepository.saveAndFlush(p2pCommission);

        // Get all the p2pCommissionList where down is not null
        defaultP2pCommissionShouldBeFound("down.specified=true");

        // Get all the p2pCommissionList where down is null
        defaultP2pCommissionShouldNotBeFound("down.specified=false");
    }

    @Test
    @Transactional
    public void getAllP2pCommissionsByFeeIsEqualToSomething() throws Exception {
        // Initialize the database
        p2pCommissionRepository.saveAndFlush(p2pCommission);

        // Get all the p2pCommissionList where fee equals to DEFAULT_FEE
        defaultP2pCommissionShouldBeFound("fee.equals=" + DEFAULT_FEE);

        // Get all the p2pCommissionList where fee equals to UPDATED_FEE
        defaultP2pCommissionShouldNotBeFound("fee.equals=" + UPDATED_FEE);
    }

    @Test
    @Transactional
    public void getAllP2pCommissionsByFeeIsInShouldWork() throws Exception {
        // Initialize the database
        p2pCommissionRepository.saveAndFlush(p2pCommission);

        // Get all the p2pCommissionList where fee in DEFAULT_FEE or UPDATED_FEE
        defaultP2pCommissionShouldBeFound("fee.in=" + DEFAULT_FEE + "," + UPDATED_FEE);

        // Get all the p2pCommissionList where fee equals to UPDATED_FEE
        defaultP2pCommissionShouldNotBeFound("fee.in=" + UPDATED_FEE);
    }

    @Test
    @Transactional
    public void getAllP2pCommissionsByFeeIsNullOrNotNull() throws Exception {
        // Initialize the database
        p2pCommissionRepository.saveAndFlush(p2pCommission);

        // Get all the p2pCommissionList where fee is not null
        defaultP2pCommissionShouldBeFound("fee.specified=true");

        // Get all the p2pCommissionList where fee is null
        defaultP2pCommissionShouldNotBeFound("fee.specified=false");
    }

    @Test
    @Transactional
    public void getAllP2pCommissionsByDescriptionIsEqualToSomething() throws Exception {
        // Initialize the database
        p2pCommissionRepository.saveAndFlush(p2pCommission);

        // Get all the p2pCommissionList where description equals to DEFAULT_DESCRIPTION
        defaultP2pCommissionShouldBeFound("description.equals=" + DEFAULT_DESCRIPTION);

        // Get all the p2pCommissionList where description equals to UPDATED_DESCRIPTION
        defaultP2pCommissionShouldNotBeFound("description.equals=" + UPDATED_DESCRIPTION);
    }

    @Test
    @Transactional
    public void getAllP2pCommissionsByDescriptionIsInShouldWork() throws Exception {
        // Initialize the database
        p2pCommissionRepository.saveAndFlush(p2pCommission);

        // Get all the p2pCommissionList where description in DEFAULT_DESCRIPTION or UPDATED_DESCRIPTION
        defaultP2pCommissionShouldBeFound("description.in=" + DEFAULT_DESCRIPTION + "," + UPDATED_DESCRIPTION);

        // Get all the p2pCommissionList where description equals to UPDATED_DESCRIPTION
        defaultP2pCommissionShouldNotBeFound("description.in=" + UPDATED_DESCRIPTION);
    }

    @Test
    @Transactional
    public void getAllP2pCommissionsByDescriptionIsNullOrNotNull() throws Exception {
        // Initialize the database
        p2pCommissionRepository.saveAndFlush(p2pCommission);

        // Get all the p2pCommissionList where description is not null
        defaultP2pCommissionShouldBeFound("description.specified=true");

        // Get all the p2pCommissionList where description is null
        defaultP2pCommissionShouldNotBeFound("description.specified=false");
    }

    /**
     * Executes the search, and checks that the default entity is returned
     */
    private void defaultP2pCommissionShouldBeFound(String filter) throws Exception {
        restP2pCommissionMockMvc
            .perform(get("/api/p-2-p-commissions?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(p2pCommission.getId().intValue())))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME.toString())))
            .andExpect(jsonPath("$.[*].up").value(hasItem(DEFAULT_UP.doubleValue())))
            .andExpect(jsonPath("$.[*].down").value(hasItem(DEFAULT_DOWN.doubleValue())))
            .andExpect(jsonPath("$.[*].fee").value(hasItem(DEFAULT_FEE.doubleValue())))
            .andExpect(jsonPath("$.[*].description").value(hasItem(DEFAULT_DESCRIPTION.toString())));

        // Check, that the count call also returns 1
        restP2pCommissionMockMvc
            .perform(get("/api/p-2-p-commissions/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(content().string("1"));
    }

    /**
     * Executes the search, and checks that the default entity is not returned
     */
    private void defaultP2pCommissionShouldNotBeFound(String filter) throws Exception {
        restP2pCommissionMockMvc
            .perform(get("/api/p-2-p-commissions?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty());

        // Check, that the count call also returns 0
        restP2pCommissionMockMvc
            .perform(get("/api/p-2-p-commissions/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(content().string("0"));
    }

    @Test
    @Transactional
    public void getNonExistingP2pCommission() throws Exception {
        // Get the p2pCommission
        restP2pCommissionMockMvc.perform(get("/api/p-2-p-commissions/{id}", Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updateP2pCommission() throws Exception {
        // Initialize the database
        p2pCommissionService.save(p2pCommission);

        int databaseSizeBeforeUpdate = p2pCommissionRepository.findAll().size();

        // Update the p2pCommission
        P2pCommission updatedP2pCommission = p2pCommissionRepository.findById(p2pCommission.getId()).get();
        // Disconnect from session so that the updates on updatedP2pCommission are not directly saved in db
        em.detach(updatedP2pCommission);
        updatedP2pCommission.name(UPDATED_NAME).up(UPDATED_UP).down(UPDATED_DOWN).fee(UPDATED_FEE).description(UPDATED_DESCRIPTION);

        restP2pCommissionMockMvc
            .perform(
                put("/api/p-2-p-commissions")
                    .contentType(TestUtil.APPLICATION_JSON_UTF8)
                    .content(TestUtil.convertObjectToJsonBytes(updatedP2pCommission))
            )
            .andExpect(status().isOk());

        // Validate the P2pCommission in the database
        List<P2pCommission> p2pCommissionList = p2pCommissionRepository.findAll();
        assertThat(p2pCommissionList).hasSize(databaseSizeBeforeUpdate);
        P2pCommission testP2pCommission = p2pCommissionList.get(p2pCommissionList.size() - 1);
        assertThat(testP2pCommission.getName()).isEqualTo(UPDATED_NAME);
        assertThat(testP2pCommission.getUp()).isEqualTo(UPDATED_UP);
        assertThat(testP2pCommission.getDown()).isEqualTo(UPDATED_DOWN);
        assertThat(testP2pCommission.getFee()).isEqualTo(UPDATED_FEE);
        assertThat(testP2pCommission.getDescription()).isEqualTo(UPDATED_DESCRIPTION);
    }

    @Test
    @Transactional
    public void updateNonExistingP2pCommission() throws Exception {
        int databaseSizeBeforeUpdate = p2pCommissionRepository.findAll().size();

        // Create the P2pCommission

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restP2pCommissionMockMvc
            .perform(
                put("/api/p-2-p-commissions")
                    .contentType(TestUtil.APPLICATION_JSON_UTF8)
                    .content(TestUtil.convertObjectToJsonBytes(p2pCommission))
            )
            .andExpect(status().isBadRequest());

        // Validate the P2pCommission in the database
        List<P2pCommission> p2pCommissionList = p2pCommissionRepository.findAll();
        assertThat(p2pCommissionList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    public void deleteP2pCommission() throws Exception {
        // Initialize the database
        p2pCommissionService.save(p2pCommission);

        int databaseSizeBeforeDelete = p2pCommissionRepository.findAll().size();

        // Get the p2pCommission
        restP2pCommissionMockMvc
            .perform(delete("/api/p-2-p-commissions/{id}", p2pCommission.getId()).accept(TestUtil.APPLICATION_JSON_UTF8))
            .andExpect(status().isOk());

        // Validate the database is empty
        List<P2pCommission> p2pCommissionList = p2pCommissionRepository.findAll();
        assertThat(p2pCommissionList).hasSize(databaseSizeBeforeDelete - 1);
    }

    @Test
    @Transactional
    public void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(P2pCommission.class);
        P2pCommission p2pCommission1 = new P2pCommission();
        p2pCommission1.setId(1L);
        P2pCommission p2pCommission2 = new P2pCommission();
        p2pCommission2.setId(p2pCommission1.getId());
        assertThat(p2pCommission1).isEqualTo(p2pCommission2);
        p2pCommission2.setId(2L);
        assertThat(p2pCommission1).isNotEqualTo(p2pCommission2);
        p2pCommission1.setId(null);
        assertThat(p2pCommission1).isNotEqualTo(p2pCommission2);
    }
}
