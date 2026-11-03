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
import uz.paykap.domain.RecipientFileSchema;
import uz.paykap.repository.RecipientFileSchemaRepository;
import uz.paykap.service.RecipientFileSchemaQueryService;
import uz.paykap.service.RecipientFileSchemaService;
import uz.paykap.service.dto.RecipientFileSchemaDTO;
import uz.paykap.service.mapper.RecipientFileSchemaMapper;
import uz.paykap.web.rest.errors.ExceptionTranslator;

/**
 * Test class for the RecipientFileSchemaResource REST controller.
 *
 * @see RecipientFileSchemaResource
 */
@RunWith(SpringRunner.class)
@IntegrationTest
public class RecipientFileSchemaResourceIntTest {

    private static final String DEFAULT_NAME = "AAAAAAAAAA";
    private static final String UPDATED_NAME = "BBBBBBBBBB";

    private static final String DEFAULT_ID_LIST = "AAAAAAAAAA";
    private static final String UPDATED_ID_LIST = "BBBBBBBBBB";

    private static final String DEFAULT_ROOT = "AAAAAAAAAA";
    private static final String UPDATED_ROOT = "BBBBBBBBBB";

    @Autowired
    private RecipientFileSchemaRepository recipientFileSchemaRepository;

    @Autowired
    private RecipientFileSchemaMapper recipientFileSchemaMapper;

    @Autowired
    private RecipientFileSchemaService recipientFileSchemaService;

    @Autowired
    private RecipientFileSchemaQueryService recipientFileSchemaQueryService;

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

    private MockMvc restRecipientFileSchemaMockMvc;

    private RecipientFileSchema recipientFileSchema;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        final RecipientFileSchemaResource recipientFileSchemaResource = new RecipientFileSchemaResource(
            recipientFileSchemaService,
            recipientFileSchemaRepository,
            recipientFileSchemaQueryService
        );
        this.restRecipientFileSchemaMockMvc =
            MockMvcBuilders
                .standaloneSetup(recipientFileSchemaResource)
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
    public static RecipientFileSchema createEntity(EntityManager em) {
        RecipientFileSchema recipientFileSchema = new RecipientFileSchema().name(DEFAULT_NAME).idList(DEFAULT_ID_LIST).root(DEFAULT_ROOT);
        return recipientFileSchema;
    }

    @Before
    public void initTest() {
        recipientFileSchema = createEntity(em);
    }

    @Test
    @Transactional
    public void createRecipientFileSchema() throws Exception {
        int databaseSizeBeforeCreate = recipientFileSchemaRepository.findAll().size();

        // Create the RecipientFileSchema
        RecipientFileSchemaDTO recipientFileSchemaDTO = recipientFileSchemaMapper.toDto(recipientFileSchema);
        restRecipientFileSchemaMockMvc
            .perform(
                post("/api/recipient-file-schemas")
                    .contentType(TestUtil.APPLICATION_JSON_UTF8)
                    .content(TestUtil.convertObjectToJsonBytes(recipientFileSchemaDTO))
            )
            .andExpect(status().isCreated());

        // Validate the RecipientFileSchema in the database
        List<RecipientFileSchema> recipientFileSchemaList = recipientFileSchemaRepository.findAll();
        assertThat(recipientFileSchemaList).hasSize(databaseSizeBeforeCreate + 1);
        RecipientFileSchema testRecipientFileSchema = recipientFileSchemaList.get(recipientFileSchemaList.size() - 1);
        assertThat(testRecipientFileSchema.getName()).isEqualTo(DEFAULT_NAME);
        assertThat(testRecipientFileSchema.getIdList()).isEqualTo(DEFAULT_ID_LIST);
        assertThat(testRecipientFileSchema.getRoot()).isEqualTo(DEFAULT_ROOT);
    }

    @Test
    @Transactional
    public void createRecipientFileSchemaWithExistingId() throws Exception {
        int databaseSizeBeforeCreate = recipientFileSchemaRepository.findAll().size();

        // Create the RecipientFileSchema with an existing ID
        recipientFileSchema.setId(1L);
        RecipientFileSchemaDTO recipientFileSchemaDTO = recipientFileSchemaMapper.toDto(recipientFileSchema);

        // An entity with an existing ID cannot be created, so this API call must fail
        restRecipientFileSchemaMockMvc
            .perform(
                post("/api/recipient-file-schemas")
                    .contentType(TestUtil.APPLICATION_JSON_UTF8)
                    .content(TestUtil.convertObjectToJsonBytes(recipientFileSchemaDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the RecipientFileSchema in the database
        List<RecipientFileSchema> recipientFileSchemaList = recipientFileSchemaRepository.findAll();
        assertThat(recipientFileSchemaList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    public void checkNameIsRequired() throws Exception {
        int databaseSizeBeforeTest = recipientFileSchemaRepository.findAll().size();
        // set the field null
        recipientFileSchema.setName(null);

        // Create the RecipientFileSchema, which fails.
        RecipientFileSchemaDTO recipientFileSchemaDTO = recipientFileSchemaMapper.toDto(recipientFileSchema);

        restRecipientFileSchemaMockMvc
            .perform(
                post("/api/recipient-file-schemas")
                    .contentType(TestUtil.APPLICATION_JSON_UTF8)
                    .content(TestUtil.convertObjectToJsonBytes(recipientFileSchemaDTO))
            )
            .andExpect(status().isBadRequest());

        List<RecipientFileSchema> recipientFileSchemaList = recipientFileSchemaRepository.findAll();
        assertThat(recipientFileSchemaList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkIdListIsRequired() throws Exception {
        int databaseSizeBeforeTest = recipientFileSchemaRepository.findAll().size();
        // set the field null
        recipientFileSchema.setIdList(null);

        // Create the RecipientFileSchema, which fails.
        RecipientFileSchemaDTO recipientFileSchemaDTO = recipientFileSchemaMapper.toDto(recipientFileSchema);

        restRecipientFileSchemaMockMvc
            .perform(
                post("/api/recipient-file-schemas")
                    .contentType(TestUtil.APPLICATION_JSON_UTF8)
                    .content(TestUtil.convertObjectToJsonBytes(recipientFileSchemaDTO))
            )
            .andExpect(status().isBadRequest());

        List<RecipientFileSchema> recipientFileSchemaList = recipientFileSchemaRepository.findAll();
        assertThat(recipientFileSchemaList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void getAllRecipientFileSchemas() throws Exception {
        // Initialize the database
        recipientFileSchemaRepository.saveAndFlush(recipientFileSchema);

        // Get all the recipientFileSchemaList
        restRecipientFileSchemaMockMvc
            .perform(get("/api/recipient-file-schemas?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(recipientFileSchema.getId().intValue())))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME.toString())))
            .andExpect(jsonPath("$.[*].idList").value(hasItem(DEFAULT_ID_LIST.toString())))
            .andExpect(jsonPath("$.[*].root").value(hasItem(DEFAULT_ROOT.toString())));
    }

    @Test
    @Transactional
    public void getRecipientFileSchema() throws Exception {
        // Initialize the database
        recipientFileSchemaRepository.saveAndFlush(recipientFileSchema);

        // Get the recipientFileSchema
        restRecipientFileSchemaMockMvc
            .perform(get("/api/recipient-file-schemas/{id}", recipientFileSchema.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.id").value(recipientFileSchema.getId().intValue()))
            .andExpect(jsonPath("$.name").value(DEFAULT_NAME.toString()))
            .andExpect(jsonPath("$.idList").value(DEFAULT_ID_LIST.toString()))
            .andExpect(jsonPath("$.root").value(DEFAULT_ROOT.toString()));
    }

    @Test
    @Transactional
    public void getAllRecipientFileSchemasByNameIsEqualToSomething() throws Exception {
        // Initialize the database
        recipientFileSchemaRepository.saveAndFlush(recipientFileSchema);

        // Get all the recipientFileSchemaList where name equals to DEFAULT_NAME
        defaultRecipientFileSchemaShouldBeFound("name.equals=" + DEFAULT_NAME);

        // Get all the recipientFileSchemaList where name equals to UPDATED_NAME
        defaultRecipientFileSchemaShouldNotBeFound("name.equals=" + UPDATED_NAME);
    }

    @Test
    @Transactional
    public void getAllRecipientFileSchemasByNameIsInShouldWork() throws Exception {
        // Initialize the database
        recipientFileSchemaRepository.saveAndFlush(recipientFileSchema);

        // Get all the recipientFileSchemaList where name in DEFAULT_NAME or UPDATED_NAME
        defaultRecipientFileSchemaShouldBeFound("name.in=" + DEFAULT_NAME + "," + UPDATED_NAME);

        // Get all the recipientFileSchemaList where name equals to UPDATED_NAME
        defaultRecipientFileSchemaShouldNotBeFound("name.in=" + UPDATED_NAME);
    }

    @Test
    @Transactional
    public void getAllRecipientFileSchemasByNameIsNullOrNotNull() throws Exception {
        // Initialize the database
        recipientFileSchemaRepository.saveAndFlush(recipientFileSchema);

        // Get all the recipientFileSchemaList where name is not null
        defaultRecipientFileSchemaShouldBeFound("name.specified=true");

        // Get all the recipientFileSchemaList where name is null
        defaultRecipientFileSchemaShouldNotBeFound("name.specified=false");
    }

    @Test
    @Transactional
    public void getAllRecipientFileSchemasByIdListIsEqualToSomething() throws Exception {
        // Initialize the database
        recipientFileSchemaRepository.saveAndFlush(recipientFileSchema);

        // Get all the recipientFileSchemaList where idList equals to DEFAULT_ID_LIST
        defaultRecipientFileSchemaShouldBeFound("idList.equals=" + DEFAULT_ID_LIST);

        // Get all the recipientFileSchemaList where idList equals to UPDATED_ID_LIST
        defaultRecipientFileSchemaShouldNotBeFound("idList.equals=" + UPDATED_ID_LIST);
    }

    @Test
    @Transactional
    public void getAllRecipientFileSchemasByIdListIsInShouldWork() throws Exception {
        // Initialize the database
        recipientFileSchemaRepository.saveAndFlush(recipientFileSchema);

        // Get all the recipientFileSchemaList where idList in DEFAULT_ID_LIST or UPDATED_ID_LIST
        defaultRecipientFileSchemaShouldBeFound("idList.in=" + DEFAULT_ID_LIST + "," + UPDATED_ID_LIST);

        // Get all the recipientFileSchemaList where idList equals to UPDATED_ID_LIST
        defaultRecipientFileSchemaShouldNotBeFound("idList.in=" + UPDATED_ID_LIST);
    }

    @Test
    @Transactional
    public void getAllRecipientFileSchemasByIdListIsNullOrNotNull() throws Exception {
        // Initialize the database
        recipientFileSchemaRepository.saveAndFlush(recipientFileSchema);

        // Get all the recipientFileSchemaList where idList is not null
        defaultRecipientFileSchemaShouldBeFound("idList.specified=true");

        // Get all the recipientFileSchemaList where idList is null
        defaultRecipientFileSchemaShouldNotBeFound("idList.specified=false");
    }

    @Test
    @Transactional
    public void getAllRecipientFileSchemasByRootIsEqualToSomething() throws Exception {
        // Initialize the database
        recipientFileSchemaRepository.saveAndFlush(recipientFileSchema);

        // Get all the recipientFileSchemaList where root equals to DEFAULT_ROOT
        defaultRecipientFileSchemaShouldBeFound("root.equals=" + DEFAULT_ROOT);

        // Get all the recipientFileSchemaList where root equals to UPDATED_ROOT
        defaultRecipientFileSchemaShouldNotBeFound("root.equals=" + UPDATED_ROOT);
    }

    @Test
    @Transactional
    public void getAllRecipientFileSchemasByRootIsInShouldWork() throws Exception {
        // Initialize the database
        recipientFileSchemaRepository.saveAndFlush(recipientFileSchema);

        // Get all the recipientFileSchemaList where root in DEFAULT_ROOT or UPDATED_ROOT
        defaultRecipientFileSchemaShouldBeFound("root.in=" + DEFAULT_ROOT + "," + UPDATED_ROOT);

        // Get all the recipientFileSchemaList where root equals to UPDATED_ROOT
        defaultRecipientFileSchemaShouldNotBeFound("root.in=" + UPDATED_ROOT);
    }

    @Test
    @Transactional
    public void getAllRecipientFileSchemasByRootIsNullOrNotNull() throws Exception {
        // Initialize the database
        recipientFileSchemaRepository.saveAndFlush(recipientFileSchema);

        // Get all the recipientFileSchemaList where root is not null
        defaultRecipientFileSchemaShouldBeFound("root.specified=true");

        // Get all the recipientFileSchemaList where root is null
        defaultRecipientFileSchemaShouldNotBeFound("root.specified=false");
    }

    /**
     * Executes the search, and checks that the default entity is returned
     */
    private void defaultRecipientFileSchemaShouldBeFound(String filter) throws Exception {
        restRecipientFileSchemaMockMvc
            .perform(get("/api/recipient-file-schemas?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(recipientFileSchema.getId().intValue())))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME.toString())))
            .andExpect(jsonPath("$.[*].idList").value(hasItem(DEFAULT_ID_LIST.toString())))
            .andExpect(jsonPath("$.[*].root").value(hasItem(DEFAULT_ROOT.toString())));

        // Check, that the count call also returns 1
        restRecipientFileSchemaMockMvc
            .perform(get("/api/recipient-file-schemas/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(content().string("1"));
    }

    /**
     * Executes the search, and checks that the default entity is not returned
     */
    private void defaultRecipientFileSchemaShouldNotBeFound(String filter) throws Exception {
        restRecipientFileSchemaMockMvc
            .perform(get("/api/recipient-file-schemas?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty());

        // Check, that the count call also returns 0
        restRecipientFileSchemaMockMvc
            .perform(get("/api/recipient-file-schemas/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(content().string("0"));
    }

    @Test
    @Transactional
    public void getNonExistingRecipientFileSchema() throws Exception {
        // Get the recipientFileSchema
        restRecipientFileSchemaMockMvc.perform(get("/api/recipient-file-schemas/{id}", Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updateRecipientFileSchema() throws Exception {
        // Initialize the database
        recipientFileSchemaRepository.saveAndFlush(recipientFileSchema);

        int databaseSizeBeforeUpdate = recipientFileSchemaRepository.findAll().size();

        // Update the recipientFileSchema
        RecipientFileSchema updatedRecipientFileSchema = recipientFileSchemaRepository.findById(recipientFileSchema.getId()).get();
        // Disconnect from session so that the updates on updatedRecipientFileSchema are not directly saved in db
        em.detach(updatedRecipientFileSchema);
        updatedRecipientFileSchema.name(UPDATED_NAME).idList(UPDATED_ID_LIST).root(UPDATED_ROOT);
        RecipientFileSchemaDTO recipientFileSchemaDTO = recipientFileSchemaMapper.toDto(updatedRecipientFileSchema);

        restRecipientFileSchemaMockMvc
            .perform(
                put("/api/recipient-file-schemas")
                    .contentType(TestUtil.APPLICATION_JSON_UTF8)
                    .content(TestUtil.convertObjectToJsonBytes(recipientFileSchemaDTO))
            )
            .andExpect(status().isOk());

        // Validate the RecipientFileSchema in the database
        List<RecipientFileSchema> recipientFileSchemaList = recipientFileSchemaRepository.findAll();
        assertThat(recipientFileSchemaList).hasSize(databaseSizeBeforeUpdate);
        RecipientFileSchema testRecipientFileSchema = recipientFileSchemaList.get(recipientFileSchemaList.size() - 1);
        assertThat(testRecipientFileSchema.getName()).isEqualTo(UPDATED_NAME);
        assertThat(testRecipientFileSchema.getIdList()).isEqualTo(UPDATED_ID_LIST);
        assertThat(testRecipientFileSchema.getRoot()).isEqualTo(UPDATED_ROOT);
    }

    @Test
    @Transactional
    public void updateNonExistingRecipientFileSchema() throws Exception {
        int databaseSizeBeforeUpdate = recipientFileSchemaRepository.findAll().size();

        // Create the RecipientFileSchema
        RecipientFileSchemaDTO recipientFileSchemaDTO = recipientFileSchemaMapper.toDto(recipientFileSchema);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restRecipientFileSchemaMockMvc
            .perform(
                put("/api/recipient-file-schemas")
                    .contentType(TestUtil.APPLICATION_JSON_UTF8)
                    .content(TestUtil.convertObjectToJsonBytes(recipientFileSchemaDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the RecipientFileSchema in the database
        List<RecipientFileSchema> recipientFileSchemaList = recipientFileSchemaRepository.findAll();
        assertThat(recipientFileSchemaList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    public void deleteRecipientFileSchema() throws Exception {
        // Initialize the database
        recipientFileSchemaRepository.saveAndFlush(recipientFileSchema);

        int databaseSizeBeforeDelete = recipientFileSchemaRepository.findAll().size();

        // Get the recipientFileSchema
        restRecipientFileSchemaMockMvc
            .perform(delete("/api/recipient-file-schemas/{id}", recipientFileSchema.getId()).accept(TestUtil.APPLICATION_JSON_UTF8))
            .andExpect(status().isOk());

        // Validate the database is empty
        List<RecipientFileSchema> recipientFileSchemaList = recipientFileSchemaRepository.findAll();
        assertThat(recipientFileSchemaList).hasSize(databaseSizeBeforeDelete - 1);
    }

    @Test
    @Transactional
    public void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(RecipientFileSchema.class);
        RecipientFileSchema recipientFileSchema1 = new RecipientFileSchema();
        recipientFileSchema1.setId(1L);
        RecipientFileSchema recipientFileSchema2 = new RecipientFileSchema();
        recipientFileSchema2.setId(recipientFileSchema1.getId());
        assertThat(recipientFileSchema1).isEqualTo(recipientFileSchema2);
        recipientFileSchema2.setId(2L);
        assertThat(recipientFileSchema1).isNotEqualTo(recipientFileSchema2);
        recipientFileSchema1.setId(null);
        assertThat(recipientFileSchema1).isNotEqualTo(recipientFileSchema2);
    }

    @Test
    @Transactional
    public void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(RecipientFileSchemaDTO.class);
        RecipientFileSchemaDTO recipientFileSchemaDTO1 = new RecipientFileSchemaDTO();
        recipientFileSchemaDTO1.setId(1L);
        RecipientFileSchemaDTO recipientFileSchemaDTO2 = new RecipientFileSchemaDTO();
        assertThat(recipientFileSchemaDTO1).isNotEqualTo(recipientFileSchemaDTO2);
        recipientFileSchemaDTO2.setId(recipientFileSchemaDTO1.getId());
        assertThat(recipientFileSchemaDTO1).isEqualTo(recipientFileSchemaDTO2);
        recipientFileSchemaDTO2.setId(2L);
        assertThat(recipientFileSchemaDTO1).isNotEqualTo(recipientFileSchemaDTO2);
        recipientFileSchemaDTO1.setId(null);
        assertThat(recipientFileSchemaDTO1).isNotEqualTo(recipientFileSchemaDTO2);
    }
    //    @Test
    //    @Transactional
    //    public void testEntityFromId() {
    //        assertThat(recipientFileSchemaMapper.fromId(42L).getId()).isEqualTo(42);
    //        assertThat(recipientFileSchemaMapper.fromId(null)).isNull();
    //    }
}
