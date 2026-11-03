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
import uz.paykap.domain.RecipientFileSchema;
import uz.paykap.repository.RecipientFileSchemaRepository;
import uz.paykap.service.criteria.RecipientFileSchemaCriteria;
import uz.paykap.service.dto.RecipientFileSchemaDTO;
import uz.paykap.service.mapper.RecipientFileSchemaMapper;

/**
 * Integration tests for the {@link RecipientFileSchemaResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class RecipientFileSchemaResourceIT {

    private static final String DEFAULT_NAME = "AAAAAAAAAA";
    private static final String UPDATED_NAME = "BBBBBBBBBB";

    private static final String DEFAULT_ID_LIST = "AAAAAAAAAA";
    private static final String UPDATED_ID_LIST = "BBBBBBBBBB";

    private static final String DEFAULT_ROOT = "AAAAAAAAAA";
    private static final String UPDATED_ROOT = "BBBBBBBBBB";

    private static final String ENTITY_API_URL = "/api/recipient-file-schemas";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong count = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private RecipientFileSchemaRepository recipientFileSchemaRepository;

    @Autowired
    private RecipientFileSchemaMapper recipientFileSchemaMapper;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restRecipientFileSchemaMockMvc;

    private RecipientFileSchema recipientFileSchema;

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

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static RecipientFileSchema createUpdatedEntity(EntityManager em) {
        RecipientFileSchema recipientFileSchema = new RecipientFileSchema().name(UPDATED_NAME).idList(UPDATED_ID_LIST).root(UPDATED_ROOT);
        return recipientFileSchema;
    }

    @BeforeEach
    public void initTest() {
        recipientFileSchema = createEntity(em);
    }

    @Test
    @Transactional
    void createRecipientFileSchema() throws Exception {
        int databaseSizeBeforeCreate = recipientFileSchemaRepository.findAll().size();
        // Create the RecipientFileSchema
        RecipientFileSchemaDTO recipientFileSchemaDTO = recipientFileSchemaMapper.toDto(recipientFileSchema);
        restRecipientFileSchemaMockMvc
            .perform(
                post(ENTITY_API_URL)
                    .contentType(MediaType.APPLICATION_JSON)
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
    void createRecipientFileSchemaWithExistingId() throws Exception {
        // Create the RecipientFileSchema with an existing ID
        recipientFileSchema.setId(1L);
        RecipientFileSchemaDTO recipientFileSchemaDTO = recipientFileSchemaMapper.toDto(recipientFileSchema);

        int databaseSizeBeforeCreate = recipientFileSchemaRepository.findAll().size();

        // An entity with an existing ID cannot be created, so this API call must fail
        restRecipientFileSchemaMockMvc
            .perform(
                post(ENTITY_API_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(recipientFileSchemaDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the RecipientFileSchema in the database
        List<RecipientFileSchema> recipientFileSchemaList = recipientFileSchemaRepository.findAll();
        assertThat(recipientFileSchemaList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkNameIsRequired() throws Exception {
        int databaseSizeBeforeTest = recipientFileSchemaRepository.findAll().size();
        // set the field null
        recipientFileSchema.setName(null);

        // Create the RecipientFileSchema, which fails.
        RecipientFileSchemaDTO recipientFileSchemaDTO = recipientFileSchemaMapper.toDto(recipientFileSchema);

        restRecipientFileSchemaMockMvc
            .perform(
                post(ENTITY_API_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(recipientFileSchemaDTO))
            )
            .andExpect(status().isBadRequest());

        List<RecipientFileSchema> recipientFileSchemaList = recipientFileSchemaRepository.findAll();
        assertThat(recipientFileSchemaList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkIdListIsRequired() throws Exception {
        int databaseSizeBeforeTest = recipientFileSchemaRepository.findAll().size();
        // set the field null
        recipientFileSchema.setIdList(null);

        // Create the RecipientFileSchema, which fails.
        RecipientFileSchemaDTO recipientFileSchemaDTO = recipientFileSchemaMapper.toDto(recipientFileSchema);

        restRecipientFileSchemaMockMvc
            .perform(
                post(ENTITY_API_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(recipientFileSchemaDTO))
            )
            .andExpect(status().isBadRequest());

        List<RecipientFileSchema> recipientFileSchemaList = recipientFileSchemaRepository.findAll();
        assertThat(recipientFileSchemaList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllRecipientFileSchemas() throws Exception {
        // Initialize the database
        recipientFileSchemaRepository.saveAndFlush(recipientFileSchema);

        // Get all the recipientFileSchemaList
        restRecipientFileSchemaMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(recipientFileSchema.getId().intValue())))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME)))
            .andExpect(jsonPath("$.[*].idList").value(hasItem(DEFAULT_ID_LIST)))
            .andExpect(jsonPath("$.[*].root").value(hasItem(DEFAULT_ROOT)));
    }

    @Test
    @Transactional
    void getRecipientFileSchema() throws Exception {
        // Initialize the database
        recipientFileSchemaRepository.saveAndFlush(recipientFileSchema);

        // Get the recipientFileSchema
        restRecipientFileSchemaMockMvc
            .perform(get(ENTITY_API_URL_ID, recipientFileSchema.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(recipientFileSchema.getId().intValue()))
            .andExpect(jsonPath("$.name").value(DEFAULT_NAME))
            .andExpect(jsonPath("$.idList").value(DEFAULT_ID_LIST))
            .andExpect(jsonPath("$.root").value(DEFAULT_ROOT));
    }

    @Test
    @Transactional
    void getRecipientFileSchemasByIdFiltering() throws Exception {
        // Initialize the database
        recipientFileSchemaRepository.saveAndFlush(recipientFileSchema);

        Long id = recipientFileSchema.getId();

        defaultRecipientFileSchemaShouldBeFound("id.equals=" + id);
        defaultRecipientFileSchemaShouldNotBeFound("id.notEquals=" + id);

        defaultRecipientFileSchemaShouldBeFound("id.greaterThanOrEqual=" + id);
        defaultRecipientFileSchemaShouldNotBeFound("id.greaterThan=" + id);

        defaultRecipientFileSchemaShouldBeFound("id.lessThanOrEqual=" + id);
        defaultRecipientFileSchemaShouldNotBeFound("id.lessThan=" + id);
    }

    @Test
    @Transactional
    void getAllRecipientFileSchemasByNameIsEqualToSomething() throws Exception {
        // Initialize the database
        recipientFileSchemaRepository.saveAndFlush(recipientFileSchema);

        // Get all the recipientFileSchemaList where name equals to DEFAULT_NAME
        defaultRecipientFileSchemaShouldBeFound("name.equals=" + DEFAULT_NAME);

        // Get all the recipientFileSchemaList where name equals to UPDATED_NAME
        defaultRecipientFileSchemaShouldNotBeFound("name.equals=" + UPDATED_NAME);
    }

    @Test
    @Transactional
    void getAllRecipientFileSchemasByNameIsNotEqualToSomething() throws Exception {
        // Initialize the database
        recipientFileSchemaRepository.saveAndFlush(recipientFileSchema);

        // Get all the recipientFileSchemaList where name not equals to DEFAULT_NAME
        defaultRecipientFileSchemaShouldNotBeFound("name.notEquals=" + DEFAULT_NAME);

        // Get all the recipientFileSchemaList where name not equals to UPDATED_NAME
        defaultRecipientFileSchemaShouldBeFound("name.notEquals=" + UPDATED_NAME);
    }

    @Test
    @Transactional
    void getAllRecipientFileSchemasByNameIsInShouldWork() throws Exception {
        // Initialize the database
        recipientFileSchemaRepository.saveAndFlush(recipientFileSchema);

        // Get all the recipientFileSchemaList where name in DEFAULT_NAME or UPDATED_NAME
        defaultRecipientFileSchemaShouldBeFound("name.in=" + DEFAULT_NAME + "," + UPDATED_NAME);

        // Get all the recipientFileSchemaList where name equals to UPDATED_NAME
        defaultRecipientFileSchemaShouldNotBeFound("name.in=" + UPDATED_NAME);
    }

    @Test
    @Transactional
    void getAllRecipientFileSchemasByNameIsNullOrNotNull() throws Exception {
        // Initialize the database
        recipientFileSchemaRepository.saveAndFlush(recipientFileSchema);

        // Get all the recipientFileSchemaList where name is not null
        defaultRecipientFileSchemaShouldBeFound("name.specified=true");

        // Get all the recipientFileSchemaList where name is null
        defaultRecipientFileSchemaShouldNotBeFound("name.specified=false");
    }

    @Test
    @Transactional
    void getAllRecipientFileSchemasByNameContainsSomething() throws Exception {
        // Initialize the database
        recipientFileSchemaRepository.saveAndFlush(recipientFileSchema);

        // Get all the recipientFileSchemaList where name contains DEFAULT_NAME
        defaultRecipientFileSchemaShouldBeFound("name.contains=" + DEFAULT_NAME);

        // Get all the recipientFileSchemaList where name contains UPDATED_NAME
        defaultRecipientFileSchemaShouldNotBeFound("name.contains=" + UPDATED_NAME);
    }

    @Test
    @Transactional
    void getAllRecipientFileSchemasByNameNotContainsSomething() throws Exception {
        // Initialize the database
        recipientFileSchemaRepository.saveAndFlush(recipientFileSchema);

        // Get all the recipientFileSchemaList where name does not contain DEFAULT_NAME
        defaultRecipientFileSchemaShouldNotBeFound("name.doesNotContain=" + DEFAULT_NAME);

        // Get all the recipientFileSchemaList where name does not contain UPDATED_NAME
        defaultRecipientFileSchemaShouldBeFound("name.doesNotContain=" + UPDATED_NAME);
    }

    @Test
    @Transactional
    void getAllRecipientFileSchemasByIdListIsEqualToSomething() throws Exception {
        // Initialize the database
        recipientFileSchemaRepository.saveAndFlush(recipientFileSchema);

        // Get all the recipientFileSchemaList where idList equals to DEFAULT_ID_LIST
        defaultRecipientFileSchemaShouldBeFound("idList.equals=" + DEFAULT_ID_LIST);

        // Get all the recipientFileSchemaList where idList equals to UPDATED_ID_LIST
        defaultRecipientFileSchemaShouldNotBeFound("idList.equals=" + UPDATED_ID_LIST);
    }

    @Test
    @Transactional
    void getAllRecipientFileSchemasByIdListIsNotEqualToSomething() throws Exception {
        // Initialize the database
        recipientFileSchemaRepository.saveAndFlush(recipientFileSchema);

        // Get all the recipientFileSchemaList where idList not equals to DEFAULT_ID_LIST
        defaultRecipientFileSchemaShouldNotBeFound("idList.notEquals=" + DEFAULT_ID_LIST);

        // Get all the recipientFileSchemaList where idList not equals to UPDATED_ID_LIST
        defaultRecipientFileSchemaShouldBeFound("idList.notEquals=" + UPDATED_ID_LIST);
    }

    @Test
    @Transactional
    void getAllRecipientFileSchemasByIdListIsInShouldWork() throws Exception {
        // Initialize the database
        recipientFileSchemaRepository.saveAndFlush(recipientFileSchema);

        // Get all the recipientFileSchemaList where idList in DEFAULT_ID_LIST or UPDATED_ID_LIST
        defaultRecipientFileSchemaShouldBeFound("idList.in=" + DEFAULT_ID_LIST + "," + UPDATED_ID_LIST);

        // Get all the recipientFileSchemaList where idList equals to UPDATED_ID_LIST
        defaultRecipientFileSchemaShouldNotBeFound("idList.in=" + UPDATED_ID_LIST);
    }

    @Test
    @Transactional
    void getAllRecipientFileSchemasByIdListIsNullOrNotNull() throws Exception {
        // Initialize the database
        recipientFileSchemaRepository.saveAndFlush(recipientFileSchema);

        // Get all the recipientFileSchemaList where idList is not null
        defaultRecipientFileSchemaShouldBeFound("idList.specified=true");

        // Get all the recipientFileSchemaList where idList is null
        defaultRecipientFileSchemaShouldNotBeFound("idList.specified=false");
    }

    @Test
    @Transactional
    void getAllRecipientFileSchemasByIdListContainsSomething() throws Exception {
        // Initialize the database
        recipientFileSchemaRepository.saveAndFlush(recipientFileSchema);

        // Get all the recipientFileSchemaList where idList contains DEFAULT_ID_LIST
        defaultRecipientFileSchemaShouldBeFound("idList.contains=" + DEFAULT_ID_LIST);

        // Get all the recipientFileSchemaList where idList contains UPDATED_ID_LIST
        defaultRecipientFileSchemaShouldNotBeFound("idList.contains=" + UPDATED_ID_LIST);
    }

    @Test
    @Transactional
    void getAllRecipientFileSchemasByIdListNotContainsSomething() throws Exception {
        // Initialize the database
        recipientFileSchemaRepository.saveAndFlush(recipientFileSchema);

        // Get all the recipientFileSchemaList where idList does not contain DEFAULT_ID_LIST
        defaultRecipientFileSchemaShouldNotBeFound("idList.doesNotContain=" + DEFAULT_ID_LIST);

        // Get all the recipientFileSchemaList where idList does not contain UPDATED_ID_LIST
        defaultRecipientFileSchemaShouldBeFound("idList.doesNotContain=" + UPDATED_ID_LIST);
    }

    @Test
    @Transactional
    void getAllRecipientFileSchemasByRootIsEqualToSomething() throws Exception {
        // Initialize the database
        recipientFileSchemaRepository.saveAndFlush(recipientFileSchema);

        // Get all the recipientFileSchemaList where root equals to DEFAULT_ROOT
        defaultRecipientFileSchemaShouldBeFound("root.equals=" + DEFAULT_ROOT);

        // Get all the recipientFileSchemaList where root equals to UPDATED_ROOT
        defaultRecipientFileSchemaShouldNotBeFound("root.equals=" + UPDATED_ROOT);
    }

    @Test
    @Transactional
    void getAllRecipientFileSchemasByRootIsNotEqualToSomething() throws Exception {
        // Initialize the database
        recipientFileSchemaRepository.saveAndFlush(recipientFileSchema);

        // Get all the recipientFileSchemaList where root not equals to DEFAULT_ROOT
        defaultRecipientFileSchemaShouldNotBeFound("root.notEquals=" + DEFAULT_ROOT);

        // Get all the recipientFileSchemaList where root not equals to UPDATED_ROOT
        defaultRecipientFileSchemaShouldBeFound("root.notEquals=" + UPDATED_ROOT);
    }

    @Test
    @Transactional
    void getAllRecipientFileSchemasByRootIsInShouldWork() throws Exception {
        // Initialize the database
        recipientFileSchemaRepository.saveAndFlush(recipientFileSchema);

        // Get all the recipientFileSchemaList where root in DEFAULT_ROOT or UPDATED_ROOT
        defaultRecipientFileSchemaShouldBeFound("root.in=" + DEFAULT_ROOT + "," + UPDATED_ROOT);

        // Get all the recipientFileSchemaList where root equals to UPDATED_ROOT
        defaultRecipientFileSchemaShouldNotBeFound("root.in=" + UPDATED_ROOT);
    }

    @Test
    @Transactional
    void getAllRecipientFileSchemasByRootIsNullOrNotNull() throws Exception {
        // Initialize the database
        recipientFileSchemaRepository.saveAndFlush(recipientFileSchema);

        // Get all the recipientFileSchemaList where root is not null
        defaultRecipientFileSchemaShouldBeFound("root.specified=true");

        // Get all the recipientFileSchemaList where root is null
        defaultRecipientFileSchemaShouldNotBeFound("root.specified=false");
    }

    @Test
    @Transactional
    void getAllRecipientFileSchemasByRootContainsSomething() throws Exception {
        // Initialize the database
        recipientFileSchemaRepository.saveAndFlush(recipientFileSchema);

        // Get all the recipientFileSchemaList where root contains DEFAULT_ROOT
        defaultRecipientFileSchemaShouldBeFound("root.contains=" + DEFAULT_ROOT);

        // Get all the recipientFileSchemaList where root contains UPDATED_ROOT
        defaultRecipientFileSchemaShouldNotBeFound("root.contains=" + UPDATED_ROOT);
    }

    @Test
    @Transactional
    void getAllRecipientFileSchemasByRootNotContainsSomething() throws Exception {
        // Initialize the database
        recipientFileSchemaRepository.saveAndFlush(recipientFileSchema);

        // Get all the recipientFileSchemaList where root does not contain DEFAULT_ROOT
        defaultRecipientFileSchemaShouldNotBeFound("root.doesNotContain=" + DEFAULT_ROOT);

        // Get all the recipientFileSchemaList where root does not contain UPDATED_ROOT
        defaultRecipientFileSchemaShouldBeFound("root.doesNotContain=" + UPDATED_ROOT);
    }

    /**
     * Executes the search, and checks that the default entity is returned.
     */
    private void defaultRecipientFileSchemaShouldBeFound(String filter) throws Exception {
        restRecipientFileSchemaMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(recipientFileSchema.getId().intValue())))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME)))
            .andExpect(jsonPath("$.[*].idList").value(hasItem(DEFAULT_ID_LIST)))
            .andExpect(jsonPath("$.[*].root").value(hasItem(DEFAULT_ROOT)));

        // Check, that the count call also returns 1
        restRecipientFileSchemaMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("1"));
    }

    /**
     * Executes the search, and checks that the default entity is not returned.
     */
    private void defaultRecipientFileSchemaShouldNotBeFound(String filter) throws Exception {
        restRecipientFileSchemaMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty());

        // Check, that the count call also returns 0
        restRecipientFileSchemaMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("0"));
    }

    @Test
    @Transactional
    void getNonExistingRecipientFileSchema() throws Exception {
        // Get the recipientFileSchema
        restRecipientFileSchemaMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putNewRecipientFileSchema() throws Exception {
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
                put(ENTITY_API_URL_ID, recipientFileSchemaDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
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
    void putNonExistingRecipientFileSchema() throws Exception {
        int databaseSizeBeforeUpdate = recipientFileSchemaRepository.findAll().size();
        recipientFileSchema.setId(count.incrementAndGet());

        // Create the RecipientFileSchema
        RecipientFileSchemaDTO recipientFileSchemaDTO = recipientFileSchemaMapper.toDto(recipientFileSchema);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restRecipientFileSchemaMockMvc
            .perform(
                put(ENTITY_API_URL_ID, recipientFileSchemaDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(recipientFileSchemaDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the RecipientFileSchema in the database
        List<RecipientFileSchema> recipientFileSchemaList = recipientFileSchemaRepository.findAll();
        assertThat(recipientFileSchemaList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchRecipientFileSchema() throws Exception {
        int databaseSizeBeforeUpdate = recipientFileSchemaRepository.findAll().size();
        recipientFileSchema.setId(count.incrementAndGet());

        // Create the RecipientFileSchema
        RecipientFileSchemaDTO recipientFileSchemaDTO = recipientFileSchemaMapper.toDto(recipientFileSchema);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restRecipientFileSchemaMockMvc
            .perform(
                put(ENTITY_API_URL_ID, count.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(recipientFileSchemaDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the RecipientFileSchema in the database
        List<RecipientFileSchema> recipientFileSchemaList = recipientFileSchemaRepository.findAll();
        assertThat(recipientFileSchemaList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamRecipientFileSchema() throws Exception {
        int databaseSizeBeforeUpdate = recipientFileSchemaRepository.findAll().size();
        recipientFileSchema.setId(count.incrementAndGet());

        // Create the RecipientFileSchema
        RecipientFileSchemaDTO recipientFileSchemaDTO = recipientFileSchemaMapper.toDto(recipientFileSchema);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restRecipientFileSchemaMockMvc
            .perform(
                put(ENTITY_API_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(recipientFileSchemaDTO))
            )
            .andExpect(status().isMethodNotAllowed());

        // Validate the RecipientFileSchema in the database
        List<RecipientFileSchema> recipientFileSchemaList = recipientFileSchemaRepository.findAll();
        assertThat(recipientFileSchemaList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateRecipientFileSchemaWithPatch() throws Exception {
        // Initialize the database
        recipientFileSchemaRepository.saveAndFlush(recipientFileSchema);

        int databaseSizeBeforeUpdate = recipientFileSchemaRepository.findAll().size();

        // Update the recipientFileSchema using partial update
        RecipientFileSchema partialUpdatedRecipientFileSchema = new RecipientFileSchema();
        partialUpdatedRecipientFileSchema.setId(recipientFileSchema.getId());

        partialUpdatedRecipientFileSchema.idList(UPDATED_ID_LIST).root(UPDATED_ROOT);

        restRecipientFileSchemaMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedRecipientFileSchema.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedRecipientFileSchema))
            )
            .andExpect(status().isOk());

        // Validate the RecipientFileSchema in the database
        List<RecipientFileSchema> recipientFileSchemaList = recipientFileSchemaRepository.findAll();
        assertThat(recipientFileSchemaList).hasSize(databaseSizeBeforeUpdate);
        RecipientFileSchema testRecipientFileSchema = recipientFileSchemaList.get(recipientFileSchemaList.size() - 1);
        assertThat(testRecipientFileSchema.getName()).isEqualTo(DEFAULT_NAME);
        assertThat(testRecipientFileSchema.getIdList()).isEqualTo(UPDATED_ID_LIST);
        assertThat(testRecipientFileSchema.getRoot()).isEqualTo(UPDATED_ROOT);
    }

    @Test
    @Transactional
    void fullUpdateRecipientFileSchemaWithPatch() throws Exception {
        // Initialize the database
        recipientFileSchemaRepository.saveAndFlush(recipientFileSchema);

        int databaseSizeBeforeUpdate = recipientFileSchemaRepository.findAll().size();

        // Update the recipientFileSchema using partial update
        RecipientFileSchema partialUpdatedRecipientFileSchema = new RecipientFileSchema();
        partialUpdatedRecipientFileSchema.setId(recipientFileSchema.getId());

        partialUpdatedRecipientFileSchema.name(UPDATED_NAME).idList(UPDATED_ID_LIST).root(UPDATED_ROOT);

        restRecipientFileSchemaMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedRecipientFileSchema.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedRecipientFileSchema))
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
    void patchNonExistingRecipientFileSchema() throws Exception {
        int databaseSizeBeforeUpdate = recipientFileSchemaRepository.findAll().size();
        recipientFileSchema.setId(count.incrementAndGet());

        // Create the RecipientFileSchema
        RecipientFileSchemaDTO recipientFileSchemaDTO = recipientFileSchemaMapper.toDto(recipientFileSchema);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restRecipientFileSchemaMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, recipientFileSchemaDTO.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(recipientFileSchemaDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the RecipientFileSchema in the database
        List<RecipientFileSchema> recipientFileSchemaList = recipientFileSchemaRepository.findAll();
        assertThat(recipientFileSchemaList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchRecipientFileSchema() throws Exception {
        int databaseSizeBeforeUpdate = recipientFileSchemaRepository.findAll().size();
        recipientFileSchema.setId(count.incrementAndGet());

        // Create the RecipientFileSchema
        RecipientFileSchemaDTO recipientFileSchemaDTO = recipientFileSchemaMapper.toDto(recipientFileSchema);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restRecipientFileSchemaMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, count.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(recipientFileSchemaDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the RecipientFileSchema in the database
        List<RecipientFileSchema> recipientFileSchemaList = recipientFileSchemaRepository.findAll();
        assertThat(recipientFileSchemaList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamRecipientFileSchema() throws Exception {
        int databaseSizeBeforeUpdate = recipientFileSchemaRepository.findAll().size();
        recipientFileSchema.setId(count.incrementAndGet());

        // Create the RecipientFileSchema
        RecipientFileSchemaDTO recipientFileSchemaDTO = recipientFileSchemaMapper.toDto(recipientFileSchema);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restRecipientFileSchemaMockMvc
            .perform(
                patch(ENTITY_API_URL)
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(recipientFileSchemaDTO))
            )
            .andExpect(status().isMethodNotAllowed());

        // Validate the RecipientFileSchema in the database
        List<RecipientFileSchema> recipientFileSchemaList = recipientFileSchemaRepository.findAll();
        assertThat(recipientFileSchemaList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteRecipientFileSchema() throws Exception {
        // Initialize the database
        recipientFileSchemaRepository.saveAndFlush(recipientFileSchema);

        int databaseSizeBeforeDelete = recipientFileSchemaRepository.findAll().size();

        // Delete the recipientFileSchema
        restRecipientFileSchemaMockMvc
            .perform(delete(ENTITY_API_URL_ID, recipientFileSchema.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        List<RecipientFileSchema> recipientFileSchemaList = recipientFileSchemaRepository.findAll();
        assertThat(recipientFileSchemaList).hasSize(databaseSizeBeforeDelete - 1);
    }
}
