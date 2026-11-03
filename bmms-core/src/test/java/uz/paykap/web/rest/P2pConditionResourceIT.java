package uz.paykap.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;
import javax.persistence.EntityManager;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import uz.paykap.IntegrationTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.parameters.P;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import uz.paykap.IntegrationTest;
import uz.paykap.PkbillmsApp;
import uz.paykap.domain.P2pCondition;
import uz.paykap.repository.P2pConditionRepository;

/**
 * Integration tests for the {@link P2pConditionResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
public class P2pConditionResourceIT {

    private static final String DEFAULT_VALUE = "AAAAAAAAAA";
    private static final String UPDATED_VALUE = "BBBBBBBBBB";

    private static final String DEFAULT_DESCRIPTION = "AAAAAAAAAA";
    private static final String UPDATED_DESCRIPTION = "BBBBBBBBBB";

    private static final String ENTITY_API_URL = "/api/p-2-p-conditions";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong count = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private P2pConditionRepository p2pConditionRepository;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restP2pConditionMockMvc;

    private P2pCondition p2pCondition;

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

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static P2pCondition createUpdatedEntity(EntityManager em) {
        P2pCondition p2pCondition = new P2pCondition().value(UPDATED_VALUE).description(UPDATED_DESCRIPTION);
        return p2pCondition;
    }

    @BeforeEach
    public void initTest() {
        p2pCondition = createEntity(em);
    }

    @Test
    @Transactional
    void createP2pCondition() throws Exception {
        int databaseSizeBeforeCreate = p2pConditionRepository.findAll().size();
        // Create the P2pCondition
        restP2pConditionMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(p2pCondition)))
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
    void createP2pConditionWithExistingId() throws Exception {
        // Create the P2pCondition with an existing ID
        p2pCondition.setId(1L);

        int databaseSizeBeforeCreate = p2pConditionRepository.findAll().size();

        // An entity with an existing ID cannot be created, so this API call must fail
        restP2pConditionMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(p2pCondition)))
            .andExpect(status().isBadRequest());

        // Validate the P2pCondition in the database
        List<P2pCondition> p2pConditionList = p2pConditionRepository.findAll();
        assertThat(p2pConditionList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void getAllP2pConditions() throws Exception {
        // Initialize the database
        p2pConditionRepository.saveAndFlush(p2pCondition);

        // Get all the p2pConditionList
        restP2pConditionMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(p2pCondition.getId().intValue())))
            .andExpect(jsonPath("$.[*].value").value(hasItem(DEFAULT_VALUE)))
            .andExpect(jsonPath("$.[*].description").value(hasItem(DEFAULT_DESCRIPTION)));
    }

    @Test
    @Transactional
    void getP2pCondition() throws Exception {
        // Initialize the database
        p2pConditionRepository.saveAndFlush(p2pCondition);

        // Get the p2pCondition
        restP2pConditionMockMvc
            .perform(get(ENTITY_API_URL_ID, p2pCondition.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(p2pCondition.getId().intValue()))
            .andExpect(jsonPath("$.value").value(DEFAULT_VALUE))
            .andExpect(jsonPath("$.description").value(DEFAULT_DESCRIPTION));
    }

//    @Test
    @Transactional
    void getP2pConditionsByIdFiltering() throws Exception {
        // Initialize the database
        p2pConditionRepository.saveAndFlush(p2pCondition);

        Long id = p2pCondition.getId();

        defaultP2pConditionShouldBeFound("id.equals=" + id);
        //        defaultP2pConditionShouldNotBeFound("id.notEquals=" + id);

        defaultP2pConditionShouldBeFound("id.greaterThanOrEqual=" + id);
        defaultP2pConditionShouldNotBeFound("id.greaterThan=" + id);

        defaultP2pConditionShouldBeFound("id.lessThanOrEqual=" + id);
        defaultP2pConditionShouldNotBeFound("id.lessThan=" + id);
    }

    @Test
    @Transactional
    void getAllP2pConditionsByValueIsEqualToSomething() throws Exception {
        // Initialize the database
        p2pConditionRepository.saveAndFlush(p2pCondition);

        // Get all the p2pConditionList where value equals to DEFAULT_VALUE
        defaultP2pConditionShouldBeFound("value.equals=" + DEFAULT_VALUE);

        // Get all the p2pConditionList where value equals to UPDATED_VALUE
        defaultP2pConditionShouldNotBeFound("value.equals=" + UPDATED_VALUE);
    }

//    @Test
    @Transactional
    void getAllP2pConditionsByValueIsNotEqualToSomething() throws Exception {
        // Initialize the database
        p2pConditionRepository.saveAndFlush(p2pCondition);

        // Get all the p2pConditionList where value not equals to DEFAULT_VALUE
        defaultP2pConditionShouldNotBeFound("value.notEquals=" + DEFAULT_VALUE);

        // Get all the p2pConditionList where value not equals to UPDATED_VALUE
        defaultP2pConditionShouldBeFound("value.notEquals=" + UPDATED_VALUE);
    }

    @Test
    @Transactional
    void getAllP2pConditionsByValueIsInShouldWork() throws Exception {
        // Initialize the database
        p2pConditionRepository.saveAndFlush(p2pCondition);

        // Get all the p2pConditionList where value in DEFAULT_VALUE or UPDATED_VALUE
        defaultP2pConditionShouldBeFound("value.in=" + DEFAULT_VALUE + "," + UPDATED_VALUE);

        // Get all the p2pConditionList where value equals to UPDATED_VALUE
        defaultP2pConditionShouldNotBeFound("value.in=" + UPDATED_VALUE);
    }

//    @Test
    @Transactional
    void getAllP2pConditionsByValueIsNullOrNotNull() throws Exception {
        // Initialize the database
        p2pConditionRepository.saveAndFlush(p2pCondition);

        // Get all the p2pConditionList where value is not null
        defaultP2pConditionShouldBeFound("value.specified=true");

        // Get all the p2pConditionList where value is null
        defaultP2pConditionShouldNotBeFound("value.specified=false");
    }

    @Test
    @Transactional
    void getAllP2pConditionsByValueContainsSomething() throws Exception {
        // Initialize the database
        p2pConditionRepository.saveAndFlush(p2pCondition);

        // Get all the p2pConditionList where value contains DEFAULT_VALUE
        defaultP2pConditionShouldBeFound("value.contains=" + DEFAULT_VALUE);

        // Get all the p2pConditionList where value contains UPDATED_VALUE
        defaultP2pConditionShouldNotBeFound("value.contains=" + UPDATED_VALUE);
    }

//    @Test
    @Transactional
    void getAllP2pConditionsByValueNotContainsSomething() throws Exception {
        // Initialize the database
        p2pConditionRepository.saveAndFlush(p2pCondition);

        // Get all the p2pConditionList where value does not contain DEFAULT_VALUE
        defaultP2pConditionShouldNotBeFound("value.doesNotContain=" + DEFAULT_VALUE);

        // Get all the p2pConditionList where value does not contain UPDATED_VALUE
        defaultP2pConditionShouldBeFound("value.doesNotContain=" + UPDATED_VALUE);
    }

    @Test
    @Transactional
    void getAllP2pConditionsByDescriptionIsEqualToSomething() throws Exception {
        // Initialize the database
        p2pConditionRepository.saveAndFlush(p2pCondition);

        // Get all the p2pConditionList where description equals to DEFAULT_DESCRIPTION
        defaultP2pConditionShouldBeFound("description.equals=" + DEFAULT_DESCRIPTION);

        // Get all the p2pConditionList where description equals to UPDATED_DESCRIPTION
        defaultP2pConditionShouldNotBeFound("description.equals=" + UPDATED_DESCRIPTION);
    }

//    @Test
    @Transactional
    void getAllP2pConditionsByDescriptionIsNotEqualToSomething() throws Exception {
        // Initialize the database
        p2pConditionRepository.saveAndFlush(p2pCondition);

        // Get all the p2pConditionList where description not equals to DEFAULT_DESCRIPTION
        defaultP2pConditionShouldNotBeFound("description.notEquals=" + DEFAULT_DESCRIPTION);

        // Get all the p2pConditionList where description not equals to UPDATED_DESCRIPTION
        defaultP2pConditionShouldBeFound("description.notEquals=" + UPDATED_DESCRIPTION);
    }

    @Test
    @Transactional
    void getAllP2pConditionsByDescriptionIsInShouldWork() throws Exception {
        // Initialize the database
        p2pConditionRepository.saveAndFlush(p2pCondition);

        // Get all the p2pConditionList where description in DEFAULT_DESCRIPTION or UPDATED_DESCRIPTION
        defaultP2pConditionShouldBeFound("description.in=" + DEFAULT_DESCRIPTION + "," + UPDATED_DESCRIPTION);

        // Get all the p2pConditionList where description equals to UPDATED_DESCRIPTION
        defaultP2pConditionShouldNotBeFound("description.in=" + UPDATED_DESCRIPTION);
    }

//    @Test
    @Transactional
    void getAllP2pConditionsByDescriptionIsNullOrNotNull() throws Exception {
        // Initialize the database
        p2pConditionRepository.saveAndFlush(p2pCondition);

        // Get all the p2pConditionList where description is not null
        defaultP2pConditionShouldBeFound("description.specified=true");

        // Get all the p2pConditionList where description is null
        defaultP2pConditionShouldNotBeFound("description.specified=false");
    }

    @Test
    @Transactional
    void getAllP2pConditionsByDescriptionContainsSomething() throws Exception {
        // Initialize the database
        p2pConditionRepository.saveAndFlush(p2pCondition);

        // Get all the p2pConditionList where description contains DEFAULT_DESCRIPTION
        defaultP2pConditionShouldBeFound("description.contains=" + DEFAULT_DESCRIPTION);

        // Get all the p2pConditionList where description contains UPDATED_DESCRIPTION
        defaultP2pConditionShouldNotBeFound("description.contains=" + UPDATED_DESCRIPTION);
    }

//    @Test
    @Transactional
    void getAllP2pConditionsByDescriptionNotContainsSomething() throws Exception {
        // Initialize the database
        p2pConditionRepository.saveAndFlush(p2pCondition);

        // Get all the p2pConditionList where description does not contain DEFAULT_DESCRIPTION
        defaultP2pConditionShouldNotBeFound("description.doesNotContain=" + DEFAULT_DESCRIPTION);

        // Get all the p2pConditionList where description does not contain UPDATED_DESCRIPTION
        defaultP2pConditionShouldBeFound("description.doesNotContain=" + UPDATED_DESCRIPTION);
    }

    /**
     * Executes the search, and checks that the default entity is returned.
     */
    private void defaultP2pConditionShouldBeFound(String filter) throws Exception {
        defaultP2pConditionShouldBeFound(filter, 1);
    }

    /**
     * Executes the search, and checks that the default entity is returned.
     */
    private void defaultP2pConditionShouldBeFound(String filter, int expectedContentSize) throws Exception {
        restP2pConditionMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(p2pCondition.getId().intValue())))
            .andExpect(jsonPath("$.[*].value").value(hasItem(DEFAULT_VALUE)))
            .andExpect(jsonPath("$.[*].description").value(hasItem(DEFAULT_DESCRIPTION)));

        // Check, that the count call also returns 1
        restP2pConditionMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string(String.valueOf(expectedContentSize)));
    }

    /**
     * Executes the search, and checks that the default entity is not returned.
     */
    private void defaultP2pConditionShouldNotBeFound(String filter) throws Exception {
        restP2pConditionMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty());

        // Check, that the count call also returns 0
        restP2pConditionMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("0"));
    }

    @Test
    @Transactional
    void getNonExistingP2pCondition() throws Exception {
        // Get the p2pCondition
        restP2pConditionMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putNewP2pCondition() throws Exception {
        // Initialize the database
        p2pConditionRepository.saveAndFlush(p2pCondition);

        int databaseSizeBeforeUpdate = p2pConditionRepository.findAll().size();

        // Update the p2pCondition
        P2pCondition updatedP2pCondition = p2pConditionRepository.findById(p2pCondition.getId()).get();
        // Disconnect from session so that the updates on updatedP2pCondition are not directly saved in db
        em.detach(updatedP2pCondition);
        updatedP2pCondition.value(UPDATED_VALUE).description(UPDATED_DESCRIPTION);

        restP2pConditionMockMvc
            .perform(
                put(ENTITY_API_URL_ID, updatedP2pCondition.getId())
                    .contentType(MediaType.APPLICATION_JSON)
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
    void putNonExistingP2pCondition() throws Exception {
        int databaseSizeBeforeUpdate = p2pConditionRepository.findAll().size();
        p2pCondition.setId(count.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restP2pConditionMockMvc
            .perform(
                put(ENTITY_API_URL_ID, p2pCondition.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(p2pCondition))
            )
            .andExpect(status().isBadRequest());

        // Validate the P2pCondition in the database
        List<P2pCondition> p2pConditionList = p2pConditionRepository.findAll();
        assertThat(p2pConditionList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchP2pCondition() throws Exception {
        int databaseSizeBeforeUpdate = p2pConditionRepository.findAll().size();
        p2pCondition.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restP2pConditionMockMvc
            .perform(
                put(ENTITY_API_URL_ID, count.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(p2pCondition))
            )
            .andExpect(status().isBadRequest());

        // Validate the P2pCondition in the database
        List<P2pCondition> p2pConditionList = p2pConditionRepository.findAll();
        assertThat(p2pConditionList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamP2pCondition() throws Exception {
        int databaseSizeBeforeUpdate = p2pConditionRepository.findAll().size();
        p2pCondition.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restP2pConditionMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(p2pCondition)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the P2pCondition in the database
        List<P2pCondition> p2pConditionList = p2pConditionRepository.findAll();
        assertThat(p2pConditionList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateP2pConditionWithPatch() throws Exception {
        // Initialize the database
        p2pConditionRepository.saveAndFlush(p2pCondition);

        int databaseSizeBeforeUpdate = p2pConditionRepository.findAll().size();

        // Update the p2pCondition using partial update
        P2pCondition partialUpdatedP2pCondition = new P2pCondition();
        partialUpdatedP2pCondition.setId(p2pCondition.getId());

        partialUpdatedP2pCondition.value(UPDATED_VALUE);

        restP2pConditionMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedP2pCondition.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedP2pCondition))
            )
            .andExpect(status().isOk());

        // Validate the P2pCondition in the database
        List<P2pCondition> p2pConditionList = p2pConditionRepository.findAll();
        assertThat(p2pConditionList).hasSize(databaseSizeBeforeUpdate);
        P2pCondition testP2pCondition = p2pConditionList.get(p2pConditionList.size() - 1);
        assertThat(testP2pCondition.getValue()).isEqualTo(UPDATED_VALUE);
        assertThat(testP2pCondition.getDescription()).isEqualTo(DEFAULT_DESCRIPTION);
    }

    @Test
    @Transactional
    void fullUpdateP2pConditionWithPatch() throws Exception {
        // Initialize the database
        p2pConditionRepository.saveAndFlush(p2pCondition);

        int databaseSizeBeforeUpdate = p2pConditionRepository.findAll().size();

        // Update the p2pCondition using partial update
        P2pCondition partialUpdatedP2pCondition = new P2pCondition();
        partialUpdatedP2pCondition.setId(p2pCondition.getId());

        partialUpdatedP2pCondition.value(UPDATED_VALUE).description(UPDATED_DESCRIPTION);

        restP2pConditionMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedP2pCondition.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedP2pCondition))
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
    void patchNonExistingP2pCondition() throws Exception {
        int databaseSizeBeforeUpdate = p2pConditionRepository.findAll().size();
        p2pCondition.setId(count.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restP2pConditionMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, p2pCondition.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(p2pCondition))
            )
            .andExpect(status().isBadRequest());

        // Validate the P2pCondition in the database
        List<P2pCondition> p2pConditionList = p2pConditionRepository.findAll();
        assertThat(p2pConditionList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchP2pCondition() throws Exception {
        int databaseSizeBeforeUpdate = p2pConditionRepository.findAll().size();
        p2pCondition.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restP2pConditionMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, count.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(p2pCondition))
            )
            .andExpect(status().isBadRequest());

        // Validate the P2pCondition in the database
        List<P2pCondition> p2pConditionList = p2pConditionRepository.findAll();
        assertThat(p2pConditionList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamP2pCondition() throws Exception {
        int databaseSizeBeforeUpdate = p2pConditionRepository.findAll().size();
        p2pCondition.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restP2pConditionMockMvc
            .perform(
                patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(TestUtil.convertObjectToJsonBytes(p2pCondition))
            )
            .andExpect(status().isMethodNotAllowed());

        // Validate the P2pCondition in the database
        List<P2pCondition> p2pConditionList = p2pConditionRepository.findAll();
        assertThat(p2pConditionList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteP2pCondition() throws Exception {
        // Initialize the database
        p2pConditionRepository.saveAndFlush(p2pCondition);

        int databaseSizeBeforeDelete = p2pConditionRepository.findAll().size();

        // Delete the p2pCondition
        restP2pConditionMockMvc
            .perform(delete(ENTITY_API_URL_ID, p2pCondition.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        List<P2pCondition> p2pConditionList = p2pConditionRepository.findAll();
        assertThat(p2pConditionList).hasSize(databaseSizeBeforeDelete - 1);
    }
}
