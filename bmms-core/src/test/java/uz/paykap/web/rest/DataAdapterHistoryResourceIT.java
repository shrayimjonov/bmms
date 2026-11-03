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
import uz.paykap.domain.DataAdapterHistory;
import uz.paykap.repository.DataAdapterHistoryRepository;

/**
 * Integration tests for the {@link DataAdapterHistoryResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class DataAdapterHistoryResourceIT {

    private static final String DEFAULT_NAME = "AAAAAAAAAA";
    private static final String UPDATED_NAME = "BBBBBBBBBB";

    private static final String DEFAULT_DETAILS = "AAAAAAAAAA";
    private static final String UPDATED_DETAILS = "BBBBBBBBBB";

    private static final String ENTITY_API_URL = "/api/data-adapter-histories";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong count = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private DataAdapterHistoryRepository dataAdapterHistoryRepository;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restDataAdapterHistoryMockMvc;

    private DataAdapterHistory dataAdapterHistory;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static DataAdapterHistory createEntity(EntityManager em) {
        DataAdapterHistory dataAdapterHistory = new DataAdapterHistory().name(DEFAULT_NAME).details(DEFAULT_DETAILS);
        return dataAdapterHistory;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static DataAdapterHistory createUpdatedEntity(EntityManager em) {
        DataAdapterHistory dataAdapterHistory = new DataAdapterHistory().name(UPDATED_NAME).details(UPDATED_DETAILS);
        return dataAdapterHistory;
    }

    @BeforeEach
    public void initTest() {
        dataAdapterHistory = createEntity(em);
    }

    @Test
    @Transactional
    void createDataAdapterHistory() throws Exception {
        int databaseSizeBeforeCreate = dataAdapterHistoryRepository.findAll().size();
        // Create the DataAdapterHistory
        restDataAdapterHistoryMockMvc
            .perform(
                post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(dataAdapterHistory))
            )
            .andExpect(status().isCreated());

        // Validate the DataAdapterHistory in the database
        List<DataAdapterHistory> dataAdapterHistoryList = dataAdapterHistoryRepository.findAll();
        assertThat(dataAdapterHistoryList).hasSize(databaseSizeBeforeCreate + 1);
        DataAdapterHistory testDataAdapterHistory = dataAdapterHistoryList.get(dataAdapterHistoryList.size() - 1);
        assertThat(testDataAdapterHistory.getName()).isEqualTo(DEFAULT_NAME);
        assertThat(testDataAdapterHistory.getDetails()).isEqualTo(DEFAULT_DETAILS);
    }

    @Test
    @Transactional
    void createDataAdapterHistoryWithExistingId() throws Exception {
        // Create the DataAdapterHistory with an existing ID
        dataAdapterHistory.setId(1L);

        int databaseSizeBeforeCreate = dataAdapterHistoryRepository.findAll().size();

        // An entity with an existing ID cannot be created, so this API call must fail
        restDataAdapterHistoryMockMvc
            .perform(
                post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(dataAdapterHistory))
            )
            .andExpect(status().isBadRequest());

        // Validate the DataAdapterHistory in the database
        List<DataAdapterHistory> dataAdapterHistoryList = dataAdapterHistoryRepository.findAll();
        assertThat(dataAdapterHistoryList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkNameIsRequired() throws Exception {
        int databaseSizeBeforeTest = dataAdapterHistoryRepository.findAll().size();
        // set the field null
        dataAdapterHistory.setName(null);

        // Create the DataAdapterHistory, which fails.

        restDataAdapterHistoryMockMvc
            .perform(
                post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(dataAdapterHistory))
            )
            .andExpect(status().isBadRequest());

        List<DataAdapterHistory> dataAdapterHistoryList = dataAdapterHistoryRepository.findAll();
        assertThat(dataAdapterHistoryList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkDetailsIsRequired() throws Exception {
        int databaseSizeBeforeTest = dataAdapterHistoryRepository.findAll().size();
        // set the field null
        dataAdapterHistory.setDetails(null);

        // Create the DataAdapterHistory, which fails.

        restDataAdapterHistoryMockMvc
            .perform(
                post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(dataAdapterHistory))
            )
            .andExpect(status().isBadRequest());

        List<DataAdapterHistory> dataAdapterHistoryList = dataAdapterHistoryRepository.findAll();
        assertThat(dataAdapterHistoryList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllDataAdapterHistories() throws Exception {
        // Initialize the database
        dataAdapterHistoryRepository.saveAndFlush(dataAdapterHistory);

        // Get all the dataAdapterHistoryList
        restDataAdapterHistoryMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(dataAdapterHistory.getId().intValue())))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME)))
            .andExpect(jsonPath("$.[*].details").value(hasItem(DEFAULT_DETAILS)));
    }

    @Test
    @Transactional
    void getDataAdapterHistory() throws Exception {
        // Initialize the database
        dataAdapterHistoryRepository.saveAndFlush(dataAdapterHistory);

        // Get the dataAdapterHistory
        restDataAdapterHistoryMockMvc
            .perform(get(ENTITY_API_URL_ID, dataAdapterHistory.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(dataAdapterHistory.getId().intValue()))
            .andExpect(jsonPath("$.name").value(DEFAULT_NAME))
            .andExpect(jsonPath("$.details").value(DEFAULT_DETAILS));
    }

    @Test
    @Transactional
    void getNonExistingDataAdapterHistory() throws Exception {
        // Get the dataAdapterHistory
        restDataAdapterHistoryMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putNewDataAdapterHistory() throws Exception {
        // Initialize the database
        dataAdapterHistoryRepository.saveAndFlush(dataAdapterHistory);

        int databaseSizeBeforeUpdate = dataAdapterHistoryRepository.findAll().size();

        // Update the dataAdapterHistory
        DataAdapterHistory updatedDataAdapterHistory = dataAdapterHistoryRepository.findById(dataAdapterHistory.getId()).get();
        // Disconnect from session so that the updates on updatedDataAdapterHistory are not directly saved in db
        em.detach(updatedDataAdapterHistory);
        updatedDataAdapterHistory.name(UPDATED_NAME).details(UPDATED_DETAILS);

        restDataAdapterHistoryMockMvc
            .perform(
                put(ENTITY_API_URL_ID, updatedDataAdapterHistory.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(updatedDataAdapterHistory))
            )
            .andExpect(status().isOk());

        // Validate the DataAdapterHistory in the database
        List<DataAdapterHistory> dataAdapterHistoryList = dataAdapterHistoryRepository.findAll();
        assertThat(dataAdapterHistoryList).hasSize(databaseSizeBeforeUpdate);
        DataAdapterHistory testDataAdapterHistory = dataAdapterHistoryList.get(dataAdapterHistoryList.size() - 1);
        assertThat(testDataAdapterHistory.getName()).isEqualTo(UPDATED_NAME);
        assertThat(testDataAdapterHistory.getDetails()).isEqualTo(UPDATED_DETAILS);
    }

    @Test
    @Transactional
    void putNonExistingDataAdapterHistory() throws Exception {
        int databaseSizeBeforeUpdate = dataAdapterHistoryRepository.findAll().size();
        dataAdapterHistory.setId(count.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restDataAdapterHistoryMockMvc
            .perform(
                put(ENTITY_API_URL_ID, dataAdapterHistory.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(dataAdapterHistory))
            )
            .andExpect(status().isBadRequest());

        // Validate the DataAdapterHistory in the database
        List<DataAdapterHistory> dataAdapterHistoryList = dataAdapterHistoryRepository.findAll();
        assertThat(dataAdapterHistoryList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchDataAdapterHistory() throws Exception {
        int databaseSizeBeforeUpdate = dataAdapterHistoryRepository.findAll().size();
        dataAdapterHistory.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restDataAdapterHistoryMockMvc
            .perform(
                put(ENTITY_API_URL_ID, count.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(dataAdapterHistory))
            )
            .andExpect(status().isBadRequest());

        // Validate the DataAdapterHistory in the database
        List<DataAdapterHistory> dataAdapterHistoryList = dataAdapterHistoryRepository.findAll();
        assertThat(dataAdapterHistoryList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamDataAdapterHistory() throws Exception {
        int databaseSizeBeforeUpdate = dataAdapterHistoryRepository.findAll().size();
        dataAdapterHistory.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restDataAdapterHistoryMockMvc
            .perform(
                put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(dataAdapterHistory))
            )
            .andExpect(status().isMethodNotAllowed());

        // Validate the DataAdapterHistory in the database
        List<DataAdapterHistory> dataAdapterHistoryList = dataAdapterHistoryRepository.findAll();
        assertThat(dataAdapterHistoryList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateDataAdapterHistoryWithPatch() throws Exception {
        // Initialize the database
        dataAdapterHistoryRepository.saveAndFlush(dataAdapterHistory);

        int databaseSizeBeforeUpdate = dataAdapterHistoryRepository.findAll().size();

        // Update the dataAdapterHistory using partial update
        DataAdapterHistory partialUpdatedDataAdapterHistory = new DataAdapterHistory();
        partialUpdatedDataAdapterHistory.setId(dataAdapterHistory.getId());

        partialUpdatedDataAdapterHistory.name(UPDATED_NAME);

        restDataAdapterHistoryMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedDataAdapterHistory.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedDataAdapterHistory))
            )
            .andExpect(status().isOk());

        // Validate the DataAdapterHistory in the database
        List<DataAdapterHistory> dataAdapterHistoryList = dataAdapterHistoryRepository.findAll();
        assertThat(dataAdapterHistoryList).hasSize(databaseSizeBeforeUpdate);
        DataAdapterHistory testDataAdapterHistory = dataAdapterHistoryList.get(dataAdapterHistoryList.size() - 1);
        assertThat(testDataAdapterHistory.getName()).isEqualTo(UPDATED_NAME);
        assertThat(testDataAdapterHistory.getDetails()).isEqualTo(DEFAULT_DETAILS);
    }

    @Test
    @Transactional
    void fullUpdateDataAdapterHistoryWithPatch() throws Exception {
        // Initialize the database
        dataAdapterHistoryRepository.saveAndFlush(dataAdapterHistory);

        int databaseSizeBeforeUpdate = dataAdapterHistoryRepository.findAll().size();

        // Update the dataAdapterHistory using partial update
        DataAdapterHistory partialUpdatedDataAdapterHistory = new DataAdapterHistory();
        partialUpdatedDataAdapterHistory.setId(dataAdapterHistory.getId());

        partialUpdatedDataAdapterHistory.name(UPDATED_NAME).details(UPDATED_DETAILS);

        restDataAdapterHistoryMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedDataAdapterHistory.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedDataAdapterHistory))
            )
            .andExpect(status().isOk());

        // Validate the DataAdapterHistory in the database
        List<DataAdapterHistory> dataAdapterHistoryList = dataAdapterHistoryRepository.findAll();
        assertThat(dataAdapterHistoryList).hasSize(databaseSizeBeforeUpdate);
        DataAdapterHistory testDataAdapterHistory = dataAdapterHistoryList.get(dataAdapterHistoryList.size() - 1);
        assertThat(testDataAdapterHistory.getName()).isEqualTo(UPDATED_NAME);
        assertThat(testDataAdapterHistory.getDetails()).isEqualTo(UPDATED_DETAILS);
    }

    @Test
    @Transactional
    void patchNonExistingDataAdapterHistory() throws Exception {
        int databaseSizeBeforeUpdate = dataAdapterHistoryRepository.findAll().size();
        dataAdapterHistory.setId(count.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restDataAdapterHistoryMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, dataAdapterHistory.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(dataAdapterHistory))
            )
            .andExpect(status().isBadRequest());

        // Validate the DataAdapterHistory in the database
        List<DataAdapterHistory> dataAdapterHistoryList = dataAdapterHistoryRepository.findAll();
        assertThat(dataAdapterHistoryList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchDataAdapterHistory() throws Exception {
        int databaseSizeBeforeUpdate = dataAdapterHistoryRepository.findAll().size();
        dataAdapterHistory.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restDataAdapterHistoryMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, count.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(dataAdapterHistory))
            )
            .andExpect(status().isBadRequest());

        // Validate the DataAdapterHistory in the database
        List<DataAdapterHistory> dataAdapterHistoryList = dataAdapterHistoryRepository.findAll();
        assertThat(dataAdapterHistoryList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamDataAdapterHistory() throws Exception {
        int databaseSizeBeforeUpdate = dataAdapterHistoryRepository.findAll().size();
        dataAdapterHistory.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restDataAdapterHistoryMockMvc
            .perform(
                patch(ENTITY_API_URL)
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(dataAdapterHistory))
            )
            .andExpect(status().isMethodNotAllowed());

        // Validate the DataAdapterHistory in the database
        List<DataAdapterHistory> dataAdapterHistoryList = dataAdapterHistoryRepository.findAll();
        assertThat(dataAdapterHistoryList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteDataAdapterHistory() throws Exception {
        // Initialize the database
        dataAdapterHistoryRepository.saveAndFlush(dataAdapterHistory);

        int databaseSizeBeforeDelete = dataAdapterHistoryRepository.findAll().size();

        // Delete the dataAdapterHistory
        restDataAdapterHistoryMockMvc
            .perform(delete(ENTITY_API_URL_ID, dataAdapterHistory.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        List<DataAdapterHistory> dataAdapterHistoryList = dataAdapterHistoryRepository.findAll();
        assertThat(dataAdapterHistoryList).hasSize(databaseSizeBeforeDelete - 1);
    }
}
