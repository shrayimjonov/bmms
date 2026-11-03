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
import uz.devops.bmcore.enumeration.Lang;
import uz.paykap.IntegrationTest;
import uz.paykap.domain.I18n;
import uz.paykap.repository.I18nRepository;
import uz.paykap.service.criteria.I18nCriteria;
import uz.paykap.service.dto.I18nDTO;
import uz.paykap.service.mapper.I18nMapper;

/**
 * Integration tests for the {@link I18nResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class I18nResourceIT {

    private static final Lang DEFAULT_LANG = Lang.UZ;
    private static final Lang UPDATED_LANG = Lang.RU;

    private static final String DEFAULT_TEXT = "AAAAAAAAAA";
    private static final String UPDATED_TEXT = "BBBBBBBBBB";

    private static final String ENTITY_API_URL = "/api/i-18-ns";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong count = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private I18nRepository i18nRepository;

    @Autowired
    private I18nMapper i18nMapper;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restI18nMockMvc;

    private I18n i18n;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static I18n createEntity(EntityManager em) {
        I18n i18n = new I18n().lang(DEFAULT_LANG).text(DEFAULT_TEXT);
        return i18n;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static I18n createUpdatedEntity(EntityManager em) {
        I18n i18n = new I18n().lang(UPDATED_LANG).text(UPDATED_TEXT);
        return i18n;
    }

    @BeforeEach
    public void initTest() {
        i18n = createEntity(em);
    }

    @Test
    @Transactional
    void createI18n() throws Exception {
        int databaseSizeBeforeCreate = i18nRepository.findAll().size();
        // Create the I18n
        I18nDTO i18nDTO = i18nMapper.toDto(i18n);
        restI18nMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(i18nDTO)))
            .andExpect(status().isCreated());

        // Validate the I18n in the database
        List<I18n> i18nList = i18nRepository.findAll();
        assertThat(i18nList).hasSize(databaseSizeBeforeCreate + 1);
        I18n testI18n = i18nList.get(i18nList.size() - 1);
        assertThat(testI18n.getLang()).isEqualTo(DEFAULT_LANG);
        assertThat(testI18n.getText()).isEqualTo(DEFAULT_TEXT);
    }

    @Test
    @Transactional
    void createI18nWithExistingId() throws Exception {
        // Create the I18n with an existing ID
        i18n.setId(1L);
        I18nDTO i18nDTO = i18nMapper.toDto(i18n);

        int databaseSizeBeforeCreate = i18nRepository.findAll().size();

        // An entity with an existing ID cannot be created, so this API call must fail
        restI18nMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(i18nDTO)))
            .andExpect(status().isBadRequest());

        // Validate the I18n in the database
        List<I18n> i18nList = i18nRepository.findAll();
        assertThat(i18nList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkLangIsRequired() throws Exception {
        int databaseSizeBeforeTest = i18nRepository.findAll().size();
        // set the field null
        i18n.setLang(null);

        // Create the I18n, which fails.
        I18nDTO i18nDTO = i18nMapper.toDto(i18n);

        restI18nMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(i18nDTO)))
            .andExpect(status().isBadRequest());

        List<I18n> i18nList = i18nRepository.findAll();
        assertThat(i18nList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkTextIsRequired() throws Exception {
        int databaseSizeBeforeTest = i18nRepository.findAll().size();
        // set the field null
        i18n.setText(null);

        // Create the I18n, which fails.
        I18nDTO i18nDTO = i18nMapper.toDto(i18n);

        restI18nMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(i18nDTO)))
            .andExpect(status().isBadRequest());

        List<I18n> i18nList = i18nRepository.findAll();
        assertThat(i18nList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllI18ns() throws Exception {
        // Initialize the database
        i18nRepository.saveAndFlush(i18n);

        // Get all the i18nList
        restI18nMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(i18n.getId().intValue())))
            .andExpect(jsonPath("$.[*].lang").value(hasItem(DEFAULT_LANG.toString())))
            .andExpect(jsonPath("$.[*].text").value(hasItem(DEFAULT_TEXT)));
    }

    @Test
    @Transactional
    void getI18n() throws Exception {
        // Initialize the database
        i18nRepository.saveAndFlush(i18n);

        // Get the i18n
        restI18nMockMvc
            .perform(get(ENTITY_API_URL_ID, i18n.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(i18n.getId().intValue()))
            .andExpect(jsonPath("$.lang").value(DEFAULT_LANG.toString()))
            .andExpect(jsonPath("$.text").value(DEFAULT_TEXT));
    }

    @Test
    @Transactional
    void getI18nsByIdFiltering() throws Exception {
        // Initialize the database
        i18nRepository.saveAndFlush(i18n);

        Long id = i18n.getId();

        defaultI18nShouldBeFound("id.equals=" + id);
        defaultI18nShouldNotBeFound("id.notEquals=" + id);

        defaultI18nShouldBeFound("id.greaterThanOrEqual=" + id);
        defaultI18nShouldNotBeFound("id.greaterThan=" + id);

        defaultI18nShouldBeFound("id.lessThanOrEqual=" + id);
        defaultI18nShouldNotBeFound("id.lessThan=" + id);
    }

    @Test
    @Transactional
    void getAllI18nsByLangIsEqualToSomething() throws Exception {
        // Initialize the database
        i18nRepository.saveAndFlush(i18n);

        // Get all the i18nList where lang equals to DEFAULT_LANG
        defaultI18nShouldBeFound("lang.equals=" + DEFAULT_LANG);

        // Get all the i18nList where lang equals to UPDATED_LANG
        defaultI18nShouldNotBeFound("lang.equals=" + UPDATED_LANG);
    }

    @Test
    @Transactional
    void getAllI18nsByLangIsNotEqualToSomething() throws Exception {
        // Initialize the database
        i18nRepository.saveAndFlush(i18n);

        // Get all the i18nList where lang not equals to DEFAULT_LANG
        defaultI18nShouldNotBeFound("lang.notEquals=" + DEFAULT_LANG);

        // Get all the i18nList where lang not equals to UPDATED_LANG
        defaultI18nShouldBeFound("lang.notEquals=" + UPDATED_LANG);
    }

    @Test
    @Transactional
    void getAllI18nsByLangIsInShouldWork() throws Exception {
        // Initialize the database
        i18nRepository.saveAndFlush(i18n);

        // Get all the i18nList where lang in DEFAULT_LANG or UPDATED_LANG
        defaultI18nShouldBeFound("lang.in=" + DEFAULT_LANG + "," + UPDATED_LANG);

        // Get all the i18nList where lang equals to UPDATED_LANG
        defaultI18nShouldNotBeFound("lang.in=" + UPDATED_LANG);
    }

    @Test
    @Transactional
    void getAllI18nsByLangIsNullOrNotNull() throws Exception {
        // Initialize the database
        i18nRepository.saveAndFlush(i18n);

        // Get all the i18nList where lang is not null
        defaultI18nShouldBeFound("lang.specified=true");

        // Get all the i18nList where lang is null
        defaultI18nShouldNotBeFound("lang.specified=false");
    }

    @Test
    @Transactional
    void getAllI18nsByTextIsEqualToSomething() throws Exception {
        // Initialize the database
        i18nRepository.saveAndFlush(i18n);

        // Get all the i18nList where text equals to DEFAULT_TEXT
        defaultI18nShouldBeFound("text.equals=" + DEFAULT_TEXT);

        // Get all the i18nList where text equals to UPDATED_TEXT
        defaultI18nShouldNotBeFound("text.equals=" + UPDATED_TEXT);
    }

    @Test
    @Transactional
    void getAllI18nsByTextIsNotEqualToSomething() throws Exception {
        // Initialize the database
        i18nRepository.saveAndFlush(i18n);

        // Get all the i18nList where text not equals to DEFAULT_TEXT
        defaultI18nShouldNotBeFound("text.notEquals=" + DEFAULT_TEXT);

        // Get all the i18nList where text not equals to UPDATED_TEXT
        defaultI18nShouldBeFound("text.notEquals=" + UPDATED_TEXT);
    }

    @Test
    @Transactional
    void getAllI18nsByTextIsInShouldWork() throws Exception {
        // Initialize the database
        i18nRepository.saveAndFlush(i18n);

        // Get all the i18nList where text in DEFAULT_TEXT or UPDATED_TEXT
        defaultI18nShouldBeFound("text.in=" + DEFAULT_TEXT + "," + UPDATED_TEXT);

        // Get all the i18nList where text equals to UPDATED_TEXT
        defaultI18nShouldNotBeFound("text.in=" + UPDATED_TEXT);
    }

    @Test
    @Transactional
    void getAllI18nsByTextIsNullOrNotNull() throws Exception {
        // Initialize the database
        i18nRepository.saveAndFlush(i18n);

        // Get all the i18nList where text is not null
        defaultI18nShouldBeFound("text.specified=true");

        // Get all the i18nList where text is null
        defaultI18nShouldNotBeFound("text.specified=false");
    }

    @Test
    @Transactional
    void getAllI18nsByTextContainsSomething() throws Exception {
        // Initialize the database
        i18nRepository.saveAndFlush(i18n);

        // Get all the i18nList where text contains DEFAULT_TEXT
        defaultI18nShouldBeFound("text.contains=" + DEFAULT_TEXT);

        // Get all the i18nList where text contains UPDATED_TEXT
        defaultI18nShouldNotBeFound("text.contains=" + UPDATED_TEXT);
    }

    @Test
    @Transactional
    void getAllI18nsByTextNotContainsSomething() throws Exception {
        // Initialize the database
        i18nRepository.saveAndFlush(i18n);

        // Get all the i18nList where text does not contain DEFAULT_TEXT
        defaultI18nShouldNotBeFound("text.doesNotContain=" + DEFAULT_TEXT);

        // Get all the i18nList where text does not contain UPDATED_TEXT
        defaultI18nShouldBeFound("text.doesNotContain=" + UPDATED_TEXT);
    }

    /**
     * Executes the search, and checks that the default entity is returned.
     */
    private void defaultI18nShouldBeFound(String filter) throws Exception {
        restI18nMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(i18n.getId().intValue())))
            .andExpect(jsonPath("$.[*].lang").value(hasItem(DEFAULT_LANG.toString())))
            .andExpect(jsonPath("$.[*].text").value(hasItem(DEFAULT_TEXT)));

        // Check, that the count call also returns 1
        restI18nMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("1"));
    }

    /**
     * Executes the search, and checks that the default entity is not returned.
     */
    private void defaultI18nShouldNotBeFound(String filter) throws Exception {
        restI18nMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty());

        // Check, that the count call also returns 0
        restI18nMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("0"));
    }

    @Test
    @Transactional
    void getNonExistingI18n() throws Exception {
        // Get the i18n
        restI18nMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putNewI18n() throws Exception {
        // Initialize the database
        i18nRepository.saveAndFlush(i18n);

        int databaseSizeBeforeUpdate = i18nRepository.findAll().size();

        // Update the i18n
        I18n updatedI18n = i18nRepository.findById(i18n.getId()).get();
        // Disconnect from session so that the updates on updatedI18n are not directly saved in db
        em.detach(updatedI18n);
        updatedI18n.lang(UPDATED_LANG).text(UPDATED_TEXT);
        I18nDTO i18nDTO = i18nMapper.toDto(updatedI18n);

        restI18nMockMvc
            .perform(
                put(ENTITY_API_URL_ID, i18nDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(i18nDTO))
            )
            .andExpect(status().isOk());

        // Validate the I18n in the database
        List<I18n> i18nList = i18nRepository.findAll();
        assertThat(i18nList).hasSize(databaseSizeBeforeUpdate);
        I18n testI18n = i18nList.get(i18nList.size() - 1);
        assertThat(testI18n.getLang()).isEqualTo(UPDATED_LANG);
        assertThat(testI18n.getText()).isEqualTo(UPDATED_TEXT);
    }

    @Test
    @Transactional
    void putNonExistingI18n() throws Exception {
        int databaseSizeBeforeUpdate = i18nRepository.findAll().size();
        i18n.setId(count.incrementAndGet());

        // Create the I18n
        I18nDTO i18nDTO = i18nMapper.toDto(i18n);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restI18nMockMvc
            .perform(
                put(ENTITY_API_URL_ID, i18nDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(i18nDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the I18n in the database
        List<I18n> i18nList = i18nRepository.findAll();
        assertThat(i18nList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchI18n() throws Exception {
        int databaseSizeBeforeUpdate = i18nRepository.findAll().size();
        i18n.setId(count.incrementAndGet());

        // Create the I18n
        I18nDTO i18nDTO = i18nMapper.toDto(i18n);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restI18nMockMvc
            .perform(
                put(ENTITY_API_URL_ID, count.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(i18nDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the I18n in the database
        List<I18n> i18nList = i18nRepository.findAll();
        assertThat(i18nList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamI18n() throws Exception {
        int databaseSizeBeforeUpdate = i18nRepository.findAll().size();
        i18n.setId(count.incrementAndGet());

        // Create the I18n
        I18nDTO i18nDTO = i18nMapper.toDto(i18n);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restI18nMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(i18nDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the I18n in the database
        List<I18n> i18nList = i18nRepository.findAll();
        assertThat(i18nList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateI18nWithPatch() throws Exception {
        // Initialize the database
        i18nRepository.saveAndFlush(i18n);

        int databaseSizeBeforeUpdate = i18nRepository.findAll().size();

        // Update the i18n using partial update
        I18n partialUpdatedI18n = new I18n();
        partialUpdatedI18n.setId(i18n.getId());

        partialUpdatedI18n.text(UPDATED_TEXT);

        restI18nMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedI18n.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedI18n))
            )
            .andExpect(status().isOk());

        // Validate the I18n in the database
        List<I18n> i18nList = i18nRepository.findAll();
        assertThat(i18nList).hasSize(databaseSizeBeforeUpdate);
        I18n testI18n = i18nList.get(i18nList.size() - 1);
        assertThat(testI18n.getLang()).isEqualTo(DEFAULT_LANG);
        assertThat(testI18n.getText()).isEqualTo(UPDATED_TEXT);
    }

    @Test
    @Transactional
    void fullUpdateI18nWithPatch() throws Exception {
        // Initialize the database
        i18nRepository.saveAndFlush(i18n);

        int databaseSizeBeforeUpdate = i18nRepository.findAll().size();

        // Update the i18n using partial update
        I18n partialUpdatedI18n = new I18n();
        partialUpdatedI18n.setId(i18n.getId());

        partialUpdatedI18n.lang(UPDATED_LANG).text(UPDATED_TEXT);

        restI18nMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedI18n.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedI18n))
            )
            .andExpect(status().isOk());

        // Validate the I18n in the database
        List<I18n> i18nList = i18nRepository.findAll();
        assertThat(i18nList).hasSize(databaseSizeBeforeUpdate);
        I18n testI18n = i18nList.get(i18nList.size() - 1);
        assertThat(testI18n.getLang()).isEqualTo(UPDATED_LANG);
        assertThat(testI18n.getText()).isEqualTo(UPDATED_TEXT);
    }

    @Test
    @Transactional
    void patchNonExistingI18n() throws Exception {
        int databaseSizeBeforeUpdate = i18nRepository.findAll().size();
        i18n.setId(count.incrementAndGet());

        // Create the I18n
        I18nDTO i18nDTO = i18nMapper.toDto(i18n);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restI18nMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, i18nDTO.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(i18nDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the I18n in the database
        List<I18n> i18nList = i18nRepository.findAll();
        assertThat(i18nList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchI18n() throws Exception {
        int databaseSizeBeforeUpdate = i18nRepository.findAll().size();
        i18n.setId(count.incrementAndGet());

        // Create the I18n
        I18nDTO i18nDTO = i18nMapper.toDto(i18n);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restI18nMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, count.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(i18nDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the I18n in the database
        List<I18n> i18nList = i18nRepository.findAll();
        assertThat(i18nList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamI18n() throws Exception {
        int databaseSizeBeforeUpdate = i18nRepository.findAll().size();
        i18n.setId(count.incrementAndGet());

        // Create the I18n
        I18nDTO i18nDTO = i18nMapper.toDto(i18n);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restI18nMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(TestUtil.convertObjectToJsonBytes(i18nDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the I18n in the database
        List<I18n> i18nList = i18nRepository.findAll();
        assertThat(i18nList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteI18n() throws Exception {
        // Initialize the database
        i18nRepository.saveAndFlush(i18n);

        int databaseSizeBeforeDelete = i18nRepository.findAll().size();

        // Delete the i18n
        restI18nMockMvc
            .perform(delete(ENTITY_API_URL_ID, i18n.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        List<I18n> i18nList = i18nRepository.findAll();
        assertThat(i18nList).hasSize(databaseSizeBeforeDelete - 1);
    }
}
