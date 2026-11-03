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
import uz.paykap.domain.Field;
import uz.paykap.domain.Prefix;
import uz.paykap.domain.enumeration.PrefixType;
import uz.paykap.repository.PrefixRepository;
import uz.paykap.service.criteria.PrefixCriteria;
import uz.paykap.service.dto.PrefixDTO;
import uz.paykap.service.mapper.PrefixMapper;

/**
 * Integration tests for the {@link PrefixResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class PrefixResourceIT {

    private static final PrefixType DEFAULT_TYPE = PrefixType.STATIC;
    private static final PrefixType UPDATED_TYPE = PrefixType.SELECT;

    private static final String DEFAULT_VALUE = "AAAAAAAAAA";
    private static final String UPDATED_VALUE = "BBBBBBBBBB";

    private static final Boolean DEFAULT_OUTPUT = false;
    private static final Boolean UPDATED_OUTPUT = true;

    private static final String DEFAULT_REQUIRE_FIELD = "AAAAAAAAAA";
    private static final String UPDATED_REQUIRE_FIELD = "BBBBBBBBBB";

    private static final String ENTITY_API_URL = "/api/prefixes";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong count = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private PrefixRepository prefixRepository;

    @Autowired
    private PrefixMapper prefixMapper;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restPrefixMockMvc;

    private Prefix prefix;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Prefix createEntity(EntityManager em) {
        Prefix prefix = new Prefix().type(DEFAULT_TYPE).value(DEFAULT_VALUE).output(DEFAULT_OUTPUT).requireField(DEFAULT_REQUIRE_FIELD);
        return prefix;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Prefix createUpdatedEntity(EntityManager em) {
        Prefix prefix = new Prefix().type(UPDATED_TYPE).value(UPDATED_VALUE).output(UPDATED_OUTPUT).requireField(UPDATED_REQUIRE_FIELD);
        return prefix;
    }

    @BeforeEach
    public void initTest() {
        prefix = createEntity(em);
    }

    @Test
    @Transactional
    void createPrefix() throws Exception {
        int databaseSizeBeforeCreate = prefixRepository.findAll().size();
        // Create the Prefix
        PrefixDTO prefixDTO = prefixMapper.toDto(prefix);
        restPrefixMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(prefixDTO)))
            .andExpect(status().isCreated());

        // Validate the Prefix in the database
        List<Prefix> prefixList = prefixRepository.findAll();
        assertThat(prefixList).hasSize(databaseSizeBeforeCreate + 1);
        Prefix testPrefix = prefixList.get(prefixList.size() - 1);
        assertThat(testPrefix.getType()).isEqualTo(DEFAULT_TYPE);
        assertThat(testPrefix.getValue()).isEqualTo(DEFAULT_VALUE);
        assertThat(testPrefix.getOutput()).isEqualTo(DEFAULT_OUTPUT);
        assertThat(testPrefix.getRequireField()).isEqualTo(DEFAULT_REQUIRE_FIELD);
    }

    @Test
    @Transactional
    void createPrefixWithExistingId() throws Exception {
        // Create the Prefix with an existing ID
        prefix.setId(1L);
        PrefixDTO prefixDTO = prefixMapper.toDto(prefix);

        int databaseSizeBeforeCreate = prefixRepository.findAll().size();

        // An entity with an existing ID cannot be created, so this API call must fail
        restPrefixMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(prefixDTO)))
            .andExpect(status().isBadRequest());

        // Validate the Prefix in the database
        List<Prefix> prefixList = prefixRepository.findAll();
        assertThat(prefixList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void getAllPrefixes() throws Exception {
        // Initialize the database
        prefixRepository.saveAndFlush(prefix);

        // Get all the prefixList
        restPrefixMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(prefix.getId().intValue())))
            .andExpect(jsonPath("$.[*].type").value(hasItem(DEFAULT_TYPE.toString())))
            .andExpect(jsonPath("$.[*].value").value(hasItem(DEFAULT_VALUE)))
            .andExpect(jsonPath("$.[*].output").value(hasItem(DEFAULT_OUTPUT.booleanValue())))
            .andExpect(jsonPath("$.[*].requireField").value(hasItem(DEFAULT_REQUIRE_FIELD)));
    }

    @Test
    @Transactional
    void getPrefix() throws Exception {
        // Initialize the database
        prefixRepository.saveAndFlush(prefix);

        // Get the prefix
        restPrefixMockMvc
            .perform(get(ENTITY_API_URL_ID, prefix.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(prefix.getId().intValue()))
            .andExpect(jsonPath("$.type").value(DEFAULT_TYPE.toString()))
            .andExpect(jsonPath("$.value").value(DEFAULT_VALUE))
            .andExpect(jsonPath("$.output").value(DEFAULT_OUTPUT.booleanValue()))
            .andExpect(jsonPath("$.requireField").value(DEFAULT_REQUIRE_FIELD));
    }

    @Test
    @Transactional
    void getPrefixesByIdFiltering() throws Exception {
        // Initialize the database
        prefixRepository.saveAndFlush(prefix);

        Long id = prefix.getId();

        defaultPrefixShouldBeFound("id.equals=" + id);
        defaultPrefixShouldNotBeFound("id.notEquals=" + id);

        defaultPrefixShouldBeFound("id.greaterThanOrEqual=" + id);
        defaultPrefixShouldNotBeFound("id.greaterThan=" + id);

        defaultPrefixShouldBeFound("id.lessThanOrEqual=" + id);
        defaultPrefixShouldNotBeFound("id.lessThan=" + id);
    }

    @Test
    @Transactional
    void getAllPrefixesByTypeIsEqualToSomething() throws Exception {
        // Initialize the database
        prefixRepository.saveAndFlush(prefix);

        // Get all the prefixList where type equals to DEFAULT_TYPE
        defaultPrefixShouldBeFound("type.equals=" + DEFAULT_TYPE);

        // Get all the prefixList where type equals to UPDATED_TYPE
        defaultPrefixShouldNotBeFound("type.equals=" + UPDATED_TYPE);
    }

    @Test
    @Transactional
    void getAllPrefixesByTypeIsNotEqualToSomething() throws Exception {
        // Initialize the database
        prefixRepository.saveAndFlush(prefix);

        // Get all the prefixList where type not equals to DEFAULT_TYPE
        defaultPrefixShouldNotBeFound("type.notEquals=" + DEFAULT_TYPE);

        // Get all the prefixList where type not equals to UPDATED_TYPE
        defaultPrefixShouldBeFound("type.notEquals=" + UPDATED_TYPE);
    }

    @Test
    @Transactional
    void getAllPrefixesByTypeIsInShouldWork() throws Exception {
        // Initialize the database
        prefixRepository.saveAndFlush(prefix);

        // Get all the prefixList where type in DEFAULT_TYPE or UPDATED_TYPE
        defaultPrefixShouldBeFound("type.in=" + DEFAULT_TYPE + "," + UPDATED_TYPE);

        // Get all the prefixList where type equals to UPDATED_TYPE
        defaultPrefixShouldNotBeFound("type.in=" + UPDATED_TYPE);
    }

    @Test
    @Transactional
    void getAllPrefixesByTypeIsNullOrNotNull() throws Exception {
        // Initialize the database
        prefixRepository.saveAndFlush(prefix);

        // Get all the prefixList where type is not null
        defaultPrefixShouldBeFound("type.specified=true");

        // Get all the prefixList where type is null
        defaultPrefixShouldNotBeFound("type.specified=false");
    }

    @Test
    @Transactional
    void getAllPrefixesByValueIsEqualToSomething() throws Exception {
        // Initialize the database
        prefixRepository.saveAndFlush(prefix);

        // Get all the prefixList where value equals to DEFAULT_VALUE
        defaultPrefixShouldBeFound("value.equals=" + DEFAULT_VALUE);

        // Get all the prefixList where value equals to UPDATED_VALUE
        defaultPrefixShouldNotBeFound("value.equals=" + UPDATED_VALUE);
    }

    @Test
    @Transactional
    void getAllPrefixesByValueIsNotEqualToSomething() throws Exception {
        // Initialize the database
        prefixRepository.saveAndFlush(prefix);

        // Get all the prefixList where value not equals to DEFAULT_VALUE
        defaultPrefixShouldNotBeFound("value.notEquals=" + DEFAULT_VALUE);

        // Get all the prefixList where value not equals to UPDATED_VALUE
        defaultPrefixShouldBeFound("value.notEquals=" + UPDATED_VALUE);
    }

    @Test
    @Transactional
    void getAllPrefixesByValueIsInShouldWork() throws Exception {
        // Initialize the database
        prefixRepository.saveAndFlush(prefix);

        // Get all the prefixList where value in DEFAULT_VALUE or UPDATED_VALUE
        defaultPrefixShouldBeFound("value.in=" + DEFAULT_VALUE + "," + UPDATED_VALUE);

        // Get all the prefixList where value equals to UPDATED_VALUE
        defaultPrefixShouldNotBeFound("value.in=" + UPDATED_VALUE);
    }

    @Test
    @Transactional
    void getAllPrefixesByValueIsNullOrNotNull() throws Exception {
        // Initialize the database
        prefixRepository.saveAndFlush(prefix);

        // Get all the prefixList where value is not null
        defaultPrefixShouldBeFound("value.specified=true");

        // Get all the prefixList where value is null
        defaultPrefixShouldNotBeFound("value.specified=false");
    }

    @Test
    @Transactional
    void getAllPrefixesByValueContainsSomething() throws Exception {
        // Initialize the database
        prefixRepository.saveAndFlush(prefix);

        // Get all the prefixList where value contains DEFAULT_VALUE
        defaultPrefixShouldBeFound("value.contains=" + DEFAULT_VALUE);

        // Get all the prefixList where value contains UPDATED_VALUE
        defaultPrefixShouldNotBeFound("value.contains=" + UPDATED_VALUE);
    }

    @Test
    @Transactional
    void getAllPrefixesByValueNotContainsSomething() throws Exception {
        // Initialize the database
        prefixRepository.saveAndFlush(prefix);

        // Get all the prefixList where value does not contain DEFAULT_VALUE
        defaultPrefixShouldNotBeFound("value.doesNotContain=" + DEFAULT_VALUE);

        // Get all the prefixList where value does not contain UPDATED_VALUE
        defaultPrefixShouldBeFound("value.doesNotContain=" + UPDATED_VALUE);
    }

    @Test
    @Transactional
    void getAllPrefixesByOutputIsEqualToSomething() throws Exception {
        // Initialize the database
        prefixRepository.saveAndFlush(prefix);

        // Get all the prefixList where output equals to DEFAULT_OUTPUT
        defaultPrefixShouldBeFound("output.equals=" + DEFAULT_OUTPUT);

        // Get all the prefixList where output equals to UPDATED_OUTPUT
        defaultPrefixShouldNotBeFound("output.equals=" + UPDATED_OUTPUT);
    }

    @Test
    @Transactional
    void getAllPrefixesByOutputIsNotEqualToSomething() throws Exception {
        // Initialize the database
        prefixRepository.saveAndFlush(prefix);

        // Get all the prefixList where output not equals to DEFAULT_OUTPUT
        defaultPrefixShouldNotBeFound("output.notEquals=" + DEFAULT_OUTPUT);

        // Get all the prefixList where output not equals to UPDATED_OUTPUT
        defaultPrefixShouldBeFound("output.notEquals=" + UPDATED_OUTPUT);
    }

    @Test
    @Transactional
    void getAllPrefixesByOutputIsInShouldWork() throws Exception {
        // Initialize the database
        prefixRepository.saveAndFlush(prefix);

        // Get all the prefixList where output in DEFAULT_OUTPUT or UPDATED_OUTPUT
        defaultPrefixShouldBeFound("output.in=" + DEFAULT_OUTPUT + "," + UPDATED_OUTPUT);

        // Get all the prefixList where output equals to UPDATED_OUTPUT
        defaultPrefixShouldNotBeFound("output.in=" + UPDATED_OUTPUT);
    }

    @Test
    @Transactional
    void getAllPrefixesByOutputIsNullOrNotNull() throws Exception {
        // Initialize the database
        prefixRepository.saveAndFlush(prefix);

        // Get all the prefixList where output is not null
        defaultPrefixShouldBeFound("output.specified=true");

        // Get all the prefixList where output is null
        defaultPrefixShouldNotBeFound("output.specified=false");
    }

    @Test
    @Transactional
    void getAllPrefixesByRequireFieldIsEqualToSomething() throws Exception {
        // Initialize the database
        prefixRepository.saveAndFlush(prefix);

        // Get all the prefixList where requireField equals to DEFAULT_REQUIRE_FIELD
        defaultPrefixShouldBeFound("requireField.equals=" + DEFAULT_REQUIRE_FIELD);

        // Get all the prefixList where requireField equals to UPDATED_REQUIRE_FIELD
        defaultPrefixShouldNotBeFound("requireField.equals=" + UPDATED_REQUIRE_FIELD);
    }

    @Test
    @Transactional
    void getAllPrefixesByRequireFieldIsNotEqualToSomething() throws Exception {
        // Initialize the database
        prefixRepository.saveAndFlush(prefix);

        // Get all the prefixList where requireField not equals to DEFAULT_REQUIRE_FIELD
        defaultPrefixShouldNotBeFound("requireField.notEquals=" + DEFAULT_REQUIRE_FIELD);

        // Get all the prefixList where requireField not equals to UPDATED_REQUIRE_FIELD
        defaultPrefixShouldBeFound("requireField.notEquals=" + UPDATED_REQUIRE_FIELD);
    }

    @Test
    @Transactional
    void getAllPrefixesByRequireFieldIsInShouldWork() throws Exception {
        // Initialize the database
        prefixRepository.saveAndFlush(prefix);

        // Get all the prefixList where requireField in DEFAULT_REQUIRE_FIELD or UPDATED_REQUIRE_FIELD
        defaultPrefixShouldBeFound("requireField.in=" + DEFAULT_REQUIRE_FIELD + "," + UPDATED_REQUIRE_FIELD);

        // Get all the prefixList where requireField equals to UPDATED_REQUIRE_FIELD
        defaultPrefixShouldNotBeFound("requireField.in=" + UPDATED_REQUIRE_FIELD);
    }

    @Test
    @Transactional
    void getAllPrefixesByRequireFieldIsNullOrNotNull() throws Exception {
        // Initialize the database
        prefixRepository.saveAndFlush(prefix);

        // Get all the prefixList where requireField is not null
        defaultPrefixShouldBeFound("requireField.specified=true");

        // Get all the prefixList where requireField is null
        defaultPrefixShouldNotBeFound("requireField.specified=false");
    }

    @Test
    @Transactional
    void getAllPrefixesByRequireFieldContainsSomething() throws Exception {
        // Initialize the database
        prefixRepository.saveAndFlush(prefix);

        // Get all the prefixList where requireField contains DEFAULT_REQUIRE_FIELD
        defaultPrefixShouldBeFound("requireField.contains=" + DEFAULT_REQUIRE_FIELD);

        // Get all the prefixList where requireField contains UPDATED_REQUIRE_FIELD
        defaultPrefixShouldNotBeFound("requireField.contains=" + UPDATED_REQUIRE_FIELD);
    }

    @Test
    @Transactional
    void getAllPrefixesByRequireFieldNotContainsSomething() throws Exception {
        // Initialize the database
        prefixRepository.saveAndFlush(prefix);

        // Get all the prefixList where requireField does not contain DEFAULT_REQUIRE_FIELD
        defaultPrefixShouldNotBeFound("requireField.doesNotContain=" + DEFAULT_REQUIRE_FIELD);

        // Get all the prefixList where requireField does not contain UPDATED_REQUIRE_FIELD
        defaultPrefixShouldBeFound("requireField.doesNotContain=" + UPDATED_REQUIRE_FIELD);
    }

    @Test
    @Transactional
    void getAllPrefixesByFieldIsEqualToSomething() throws Exception {
        // Initialize the database
        prefixRepository.saveAndFlush(prefix);
        Field field = FieldResourceIT.createEntity(em);
        em.persist(field);
        em.flush();
        prefix.setField(field);
        prefixRepository.saveAndFlush(prefix);
        Long fieldId = field.getId();

        // Get all the prefixList where field equals to fieldId
        defaultPrefixShouldBeFound("fieldId.equals=" + fieldId);

        // Get all the prefixList where field equals to (fieldId + 1)
        defaultPrefixShouldNotBeFound("fieldId.equals=" + (fieldId + 1));
    }

    /**
     * Executes the search, and checks that the default entity is returned.
     */
    private void defaultPrefixShouldBeFound(String filter) throws Exception {
        restPrefixMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(prefix.getId().intValue())))
            .andExpect(jsonPath("$.[*].type").value(hasItem(DEFAULT_TYPE.toString())))
            .andExpect(jsonPath("$.[*].value").value(hasItem(DEFAULT_VALUE)))
            .andExpect(jsonPath("$.[*].output").value(hasItem(DEFAULT_OUTPUT.booleanValue())))
            .andExpect(jsonPath("$.[*].requireField").value(hasItem(DEFAULT_REQUIRE_FIELD)));

        // Check, that the count call also returns 1
        restPrefixMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("1"));
    }

    /**
     * Executes the search, and checks that the default entity is not returned.
     */
    private void defaultPrefixShouldNotBeFound(String filter) throws Exception {
        restPrefixMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty());

        // Check, that the count call also returns 0
        restPrefixMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("0"));
    }

    @Test
    @Transactional
    void getNonExistingPrefix() throws Exception {
        // Get the prefix
        restPrefixMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putNewPrefix() throws Exception {
        // Initialize the database
        prefixRepository.saveAndFlush(prefix);

        int databaseSizeBeforeUpdate = prefixRepository.findAll().size();

        // Update the prefix
        Prefix updatedPrefix = prefixRepository.findById(prefix.getId()).get();
        // Disconnect from session so that the updates on updatedPrefix are not directly saved in db
        em.detach(updatedPrefix);
        updatedPrefix.type(UPDATED_TYPE).value(UPDATED_VALUE).output(UPDATED_OUTPUT).requireField(UPDATED_REQUIRE_FIELD);
        PrefixDTO prefixDTO = prefixMapper.toDto(updatedPrefix);

        restPrefixMockMvc
            .perform(
                put(ENTITY_API_URL_ID, prefixDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(prefixDTO))
            )
            .andExpect(status().isOk());

        // Validate the Prefix in the database
        List<Prefix> prefixList = prefixRepository.findAll();
        assertThat(prefixList).hasSize(databaseSizeBeforeUpdate);
        Prefix testPrefix = prefixList.get(prefixList.size() - 1);
        assertThat(testPrefix.getType()).isEqualTo(UPDATED_TYPE);
        assertThat(testPrefix.getValue()).isEqualTo(UPDATED_VALUE);
        assertThat(testPrefix.getOutput()).isEqualTo(UPDATED_OUTPUT);
        assertThat(testPrefix.getRequireField()).isEqualTo(UPDATED_REQUIRE_FIELD);
    }

    @Test
    @Transactional
    void putNonExistingPrefix() throws Exception {
        int databaseSizeBeforeUpdate = prefixRepository.findAll().size();
        prefix.setId(count.incrementAndGet());

        // Create the Prefix
        PrefixDTO prefixDTO = prefixMapper.toDto(prefix);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restPrefixMockMvc
            .perform(
                put(ENTITY_API_URL_ID, prefixDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(prefixDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Prefix in the database
        List<Prefix> prefixList = prefixRepository.findAll();
        assertThat(prefixList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchPrefix() throws Exception {
        int databaseSizeBeforeUpdate = prefixRepository.findAll().size();
        prefix.setId(count.incrementAndGet());

        // Create the Prefix
        PrefixDTO prefixDTO = prefixMapper.toDto(prefix);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restPrefixMockMvc
            .perform(
                put(ENTITY_API_URL_ID, count.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(prefixDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Prefix in the database
        List<Prefix> prefixList = prefixRepository.findAll();
        assertThat(prefixList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamPrefix() throws Exception {
        int databaseSizeBeforeUpdate = prefixRepository.findAll().size();
        prefix.setId(count.incrementAndGet());

        // Create the Prefix
        PrefixDTO prefixDTO = prefixMapper.toDto(prefix);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restPrefixMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(prefixDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Prefix in the database
        List<Prefix> prefixList = prefixRepository.findAll();
        assertThat(prefixList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdatePrefixWithPatch() throws Exception {
        // Initialize the database
        prefixRepository.saveAndFlush(prefix);

        int databaseSizeBeforeUpdate = prefixRepository.findAll().size();

        // Update the prefix using partial update
        Prefix partialUpdatedPrefix = new Prefix();
        partialUpdatedPrefix.setId(prefix.getId());

        partialUpdatedPrefix.requireField(UPDATED_REQUIRE_FIELD);

        restPrefixMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedPrefix.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedPrefix))
            )
            .andExpect(status().isOk());

        // Validate the Prefix in the database
        List<Prefix> prefixList = prefixRepository.findAll();
        assertThat(prefixList).hasSize(databaseSizeBeforeUpdate);
        Prefix testPrefix = prefixList.get(prefixList.size() - 1);
        assertThat(testPrefix.getType()).isEqualTo(DEFAULT_TYPE);
        assertThat(testPrefix.getValue()).isEqualTo(DEFAULT_VALUE);
        assertThat(testPrefix.getOutput()).isEqualTo(DEFAULT_OUTPUT);
        assertThat(testPrefix.getRequireField()).isEqualTo(UPDATED_REQUIRE_FIELD);
    }

    @Test
    @Transactional
    void fullUpdatePrefixWithPatch() throws Exception {
        // Initialize the database
        prefixRepository.saveAndFlush(prefix);

        int databaseSizeBeforeUpdate = prefixRepository.findAll().size();

        // Update the prefix using partial update
        Prefix partialUpdatedPrefix = new Prefix();
        partialUpdatedPrefix.setId(prefix.getId());

        partialUpdatedPrefix.type(UPDATED_TYPE).value(UPDATED_VALUE).output(UPDATED_OUTPUT).requireField(UPDATED_REQUIRE_FIELD);

        restPrefixMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedPrefix.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedPrefix))
            )
            .andExpect(status().isOk());

        // Validate the Prefix in the database
        List<Prefix> prefixList = prefixRepository.findAll();
        assertThat(prefixList).hasSize(databaseSizeBeforeUpdate);
        Prefix testPrefix = prefixList.get(prefixList.size() - 1);
        assertThat(testPrefix.getType()).isEqualTo(UPDATED_TYPE);
        assertThat(testPrefix.getValue()).isEqualTo(UPDATED_VALUE);
        assertThat(testPrefix.getOutput()).isEqualTo(UPDATED_OUTPUT);
        assertThat(testPrefix.getRequireField()).isEqualTo(UPDATED_REQUIRE_FIELD);
    }

    @Test
    @Transactional
    void patchNonExistingPrefix() throws Exception {
        int databaseSizeBeforeUpdate = prefixRepository.findAll().size();
        prefix.setId(count.incrementAndGet());

        // Create the Prefix
        PrefixDTO prefixDTO = prefixMapper.toDto(prefix);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restPrefixMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, prefixDTO.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(prefixDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Prefix in the database
        List<Prefix> prefixList = prefixRepository.findAll();
        assertThat(prefixList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchPrefix() throws Exception {
        int databaseSizeBeforeUpdate = prefixRepository.findAll().size();
        prefix.setId(count.incrementAndGet());

        // Create the Prefix
        PrefixDTO prefixDTO = prefixMapper.toDto(prefix);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restPrefixMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, count.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(prefixDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Prefix in the database
        List<Prefix> prefixList = prefixRepository.findAll();
        assertThat(prefixList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamPrefix() throws Exception {
        int databaseSizeBeforeUpdate = prefixRepository.findAll().size();
        prefix.setId(count.incrementAndGet());

        // Create the Prefix
        PrefixDTO prefixDTO = prefixMapper.toDto(prefix);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restPrefixMockMvc
            .perform(
                patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(TestUtil.convertObjectToJsonBytes(prefixDTO))
            )
            .andExpect(status().isMethodNotAllowed());

        // Validate the Prefix in the database
        List<Prefix> prefixList = prefixRepository.findAll();
        assertThat(prefixList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deletePrefix() throws Exception {
        // Initialize the database
        prefixRepository.saveAndFlush(prefix);

        int databaseSizeBeforeDelete = prefixRepository.findAll().size();

        // Delete the prefix
        restPrefixMockMvc
            .perform(delete(ENTITY_API_URL_ID, prefix.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        List<Prefix> prefixList = prefixRepository.findAll();
        assertThat(prefixList).hasSize(databaseSizeBeforeDelete - 1);
    }
}
