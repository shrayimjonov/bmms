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
import uz.paykap.domain.P2pCondition;
import uz.paykap.repository.P2pConditionRepository;
import uz.paykap.service.P2pConditionQueryService;
import uz.paykap.service.P2pConditionService;
import uz.paykap.service.dto.P2pConditionCriteria;
import uz.paykap.web.rest.errors.ExceptionTranslator;

/**
 * Test class for the P2pConditionResource REST controller.
 *
 * @see P2pConditionResource
 */
@RunWith(SpringRunner.class)
@IntegrationTest
public class P2pConditionResourceIntTest {

    private static final String DEFAULT_VALUE = "AAAAAAAAAA";
    private static final String UPDATED_VALUE = "BBBBBBBBBB";

    private static final String DEFAULT_DESCRIPTION = "AAAAAAAAAA";
    private static final String UPDATED_DESCRIPTION = "BBBBBBBBBB";

    @Autowired
    private P2pConditionRepository p2pConditionRepository;

    @Autowired
    private P2pConditionService p2pConditionService;

    @Autowired
    private P2pConditionQueryService p2pConditionQueryService;

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

    private MockMvc restP2pConditionMockMvc;

    private P2pCondition p2pCondition;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        final P2pConditionResource p2pConditionResource = new P2pConditionResource(
            p2pConditionService,
            p2pConditionRepository,
            p2pConditionQueryService
        );
        this.restP2pConditionMockMvc =
            MockMvcBuilders
                .standaloneSetup(p2pConditionResource)
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
    public static P2pCondition createEntity(EntityManager em) {
        P2pCondition p2pCondition = new P2pCondition().value(DEFAULT_VALUE).description(DEFAULT_DESCRIPTION);
        return p2pCondition;
    }

    @Before
    public void initTest() {
        p2pCondition = createEntity(em);
    }

    @Test
    @Transactional
    public void createP2pCondition() throws Exception {
        int databaseSizeBeforeCreate = p2pConditionRepository.findAll().size();

        // Create the P2pCondition
        restP2pConditionMockMvc
            .perform(
                post("/api/p-2-p-conditions")
                    .contentType(TestUtil.APPLICATION_JSON_UTF8)
                    .content(TestUtil.convertObjectToJsonBytes(p2pCondition))
            )
            .andExpect(status().isCreated());

        // Validate the P2pCondition in the database
        List<P2pCondition> p2pConditionList = p2pConditionRepository.findAll();
        assertThat(p2pConditionList).hasSize(databaseSizeBeforeCreate + 1);
        P2pCondition testP2pCondition = p2pConditionList.get(p2pConditionList.size() - 1);
        assertThat(testP2pCondition.getValue()).isEqualTo(DEFAULT_VALUE);
        assertThat(testP2pCondition.getDescription()).isEqualTo(DEFAULT_DESCRIPTION);
    }

    @Test
    @Transactional
    public void createP2pConditionWithExistingId() throws Exception {
        int databaseSizeBeforeCreate = p2pConditionRepository.findAll().size();

        // Create the P2pCondition with an existing ID
        p2pCondition.setId(1L);

        // An entity with an existing ID cannot be created, so this API call must fail
        restP2pConditionMockMvc
            .perform(
                post("/api/p-2-p-conditions")
                    .contentType(TestUtil.APPLICATION_JSON_UTF8)
                    .content(TestUtil.convertObjectToJsonBytes(p2pCondition))
            )
            .andExpect(status().isBadRequest());

        // Validate the P2pCondition in the database
        List<P2pCondition> p2pConditionList = p2pConditionRepository.findAll();
        assertThat(p2pConditionList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    public void getAllP2pConditions() throws Exception {
        // Initialize the database
        p2pConditionRepository.saveAndFlush(p2pCondition);

        // Get all the p2pConditionList
        restP2pConditionMockMvc
            .perform(get("/api/p-2-p-conditions?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(p2pCondition.getId().intValue())))
            .andExpect(jsonPath("$.[*].value").value(hasItem(DEFAULT_VALUE.toString())))
            .andExpect(jsonPath("$.[*].description").value(hasItem(DEFAULT_DESCRIPTION.toString())));
    }

    @Test
    @Transactional
    public void getP2pCondition() throws Exception {
        // Initialize the database
        p2pConditionRepository.saveAndFlush(p2pCondition);

        // Get the p2pCondition
        restP2pConditionMockMvc
            .perform(get("/api/p-2-p-conditions/{id}", p2pCondition.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.id").value(p2pCondition.getId().intValue()))
            .andExpect(jsonPath("$.value").value(DEFAULT_VALUE.toString()))
            .andExpect(jsonPath("$.description").value(DEFAULT_DESCRIPTION.toString()));
    }

    @Test
    @Transactional
    public void getAllP2pConditionsByValueIsEqualToSomething() throws Exception {
        // Initialize the database
        p2pConditionRepository.saveAndFlush(p2pCondition);

        // Get all the p2pConditionList where value equals to DEFAULT_VALUE
        defaultP2pConditionShouldBeFound("value.equals=" + DEFAULT_VALUE);

        // Get all the p2pConditionList where value equals to UPDATED_VALUE
        defaultP2pConditionShouldNotBeFound("value.equals=" + UPDATED_VALUE);
    }

    @Test
    @Transactional
    public void getAllP2pConditionsByValueIsInShouldWork() throws Exception {
        // Initialize the database
        p2pConditionRepository.saveAndFlush(p2pCondition);

        // Get all the p2pConditionList where value in DEFAULT_VALUE or UPDATED_VALUE
        defaultP2pConditionShouldBeFound("value.in=" + DEFAULT_VALUE + "," + UPDATED_VALUE);

        // Get all the p2pConditionList where value equals to UPDATED_VALUE
        defaultP2pConditionShouldNotBeFound("value.in=" + UPDATED_VALUE);
    }

    @Test
    @Transactional
    public void getAllP2pConditionsByValueIsNullOrNotNull() throws Exception {
        // Initialize the database
        p2pConditionRepository.saveAndFlush(p2pCondition);

        // Get all the p2pConditionList where value is not null
        defaultP2pConditionShouldBeFound("value.specified=true");

        // Get all the p2pConditionList where value is null
        defaultP2pConditionShouldNotBeFound("value.specified=false");
    }

    @Test
    @Transactional
    public void getAllP2pConditionsByDescriptionIsEqualToSomething() throws Exception {
        // Initialize the database
        p2pConditionRepository.saveAndFlush(p2pCondition);

        // Get all the p2pConditionList where description equals to DEFAULT_DESCRIPTION
        defaultP2pConditionShouldBeFound("description.equals=" + DEFAULT_DESCRIPTION);

        // Get all the p2pConditionList where description equals to UPDATED_DESCRIPTION
        defaultP2pConditionShouldNotBeFound("description.equals=" + UPDATED_DESCRIPTION);
    }

    @Test
    @Transactional
    public void getAllP2pConditionsByDescriptionIsInShouldWork() throws Exception {
        // Initialize the database
        p2pConditionRepository.saveAndFlush(p2pCondition);

        // Get all the p2pConditionList where description in DEFAULT_DESCRIPTION or UPDATED_DESCRIPTION
        defaultP2pConditionShouldBeFound("description.in=" + DEFAULT_DESCRIPTION + "," + UPDATED_DESCRIPTION);

        // Get all the p2pConditionList where description equals to UPDATED_DESCRIPTION
        defaultP2pConditionShouldNotBeFound("description.in=" + UPDATED_DESCRIPTION);
    }

    @Test
    @Transactional
    public void getAllP2pConditionsByDescriptionIsNullOrNotNull() throws Exception {
        // Initialize the database
        p2pConditionRepository.saveAndFlush(p2pCondition);

        // Get all the p2pConditionList where description is not null
        defaultP2pConditionShouldBeFound("description.specified=true");

        // Get all the p2pConditionList where description is null
        defaultP2pConditionShouldNotBeFound("description.specified=false");
    }

    /**
     * Executes the search, and checks that the default entity is returned
     */
    private void defaultP2pConditionShouldBeFound(String filter) throws Exception {
        restP2pConditionMockMvc
            .perform(get("/api/p-2-p-conditions?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(p2pCondition.getId().intValue())))
            .andExpect(jsonPath("$.[*].value").value(hasItem(DEFAULT_VALUE.toString())))
            .andExpect(jsonPath("$.[*].description").value(hasItem(DEFAULT_DESCRIPTION.toString())));

        // Check, that the count call also returns 1
        restP2pConditionMockMvc
            .perform(get("/api/p-2-p-conditions/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(content().string("1"));
    }

    /**
     * Executes the search, and checks that the default entity is not returned
     */
    private void defaultP2pConditionShouldNotBeFound(String filter) throws Exception {
        restP2pConditionMockMvc
            .perform(get("/api/p-2-p-conditions?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty());

        // Check, that the count call also returns 0
        restP2pConditionMockMvc
            .perform(get("/api/p-2-p-conditions/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(content().string("0"));
    }

    @Test
    @Transactional
    public void getNonExistingP2pCondition() throws Exception {
        // Get the p2pCondition
        restP2pConditionMockMvc.perform(get("/api/p-2-p-conditions/{id}", Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updateP2pCondition() throws Exception {
        // Initialize the database
        p2pConditionService.save(p2pCondition);

        int databaseSizeBeforeUpdate = p2pConditionRepository.findAll().size();

        // Update the p2pCondition
        P2pCondition updatedP2pCondition = p2pConditionRepository.findById(p2pCondition.getId()).get();
        // Disconnect from session so that the updates on updatedP2pCondition are not directly saved in db
        em.detach(updatedP2pCondition);
        updatedP2pCondition.value(UPDATED_VALUE).description(UPDATED_DESCRIPTION);

        restP2pConditionMockMvc
            .perform(
                put("/api/p-2-p-conditions")
                    .contentType(TestUtil.APPLICATION_JSON_UTF8)
                    .content(TestUtil.convertObjectToJsonBytes(updatedP2pCondition))
            )
            .andExpect(status().isOk());

        // Validate the P2pCondition in the database
        List<P2pCondition> p2pConditionList = p2pConditionRepository.findAll();
        assertThat(p2pConditionList).hasSize(databaseSizeBeforeUpdate);
        P2pCondition testP2pCondition = p2pConditionList.get(p2pConditionList.size() - 1);
        assertThat(testP2pCondition.getValue()).isEqualTo(UPDATED_VALUE);
        assertThat(testP2pCondition.getDescription()).isEqualTo(UPDATED_DESCRIPTION);
    }

    @Test
    @Transactional
    public void updateNonExistingP2pCondition() throws Exception {
        int databaseSizeBeforeUpdate = p2pConditionRepository.findAll().size();

        // Create the P2pCondition

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restP2pConditionMockMvc
            .perform(
                put("/api/p-2-p-conditions")
                    .contentType(TestUtil.APPLICATION_JSON_UTF8)
                    .content(TestUtil.convertObjectToJsonBytes(p2pCondition))
            )
            .andExpect(status().isBadRequest());

        // Validate the P2pCondition in the database
        List<P2pCondition> p2pConditionList = p2pConditionRepository.findAll();
        assertThat(p2pConditionList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    public void deleteP2pCondition() throws Exception {
        // Initialize the database
        p2pConditionService.save(p2pCondition);

        int databaseSizeBeforeDelete = p2pConditionRepository.findAll().size();

        // Get the p2pCondition
        restP2pConditionMockMvc
            .perform(delete("/api/p-2-p-conditions/{id}", p2pCondition.getId()).accept(TestUtil.APPLICATION_JSON_UTF8))
            .andExpect(status().isOk());

        // Validate the database is empty
        List<P2pCondition> p2pConditionList = p2pConditionRepository.findAll();
        assertThat(p2pConditionList).hasSize(databaseSizeBeforeDelete - 1);
    }

    @Test
    @Transactional
    public void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(P2pCondition.class);
        P2pCondition p2pCondition1 = new P2pCondition();
        p2pCondition1.setId(1L);
        P2pCondition p2pCondition2 = new P2pCondition();
        p2pCondition2.setId(p2pCondition1.getId());
        assertThat(p2pCondition1).isEqualTo(p2pCondition2);
        p2pCondition2.setId(2L);
        assertThat(p2pCondition1).isNotEqualTo(p2pCondition2);
        p2pCondition1.setId(null);
        assertThat(p2pCondition1).isNotEqualTo(p2pCondition2);
    }
}
