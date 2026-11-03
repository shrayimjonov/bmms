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
import uz.paykap.domain.DataAdapterHistory;
import uz.paykap.repository.DataAdapterHistoryRepository;
import uz.paykap.service.DataAdapterHistoryService;
import uz.paykap.web.rest.errors.ExceptionTranslator;

/**
 * Test class for the DataAdapterHistoryResource REST controller.
 *
 * @see DataAdapterHistoryResource
 */
@RunWith(SpringRunner.class)
@IntegrationTest
public class DataAdapterHistoryResourceIntTest {

    private static final String DEFAULT_NAME = "AAAAAAAAAA";
    private static final String UPDATED_NAME = "BBBBBBBBBB";

    private static final String DEFAULT_DETAILS = "AAAAAAAAAA";
    private static final String UPDATED_DETAILS = "BBBBBBBBBB";

    @Autowired
    private DataAdapterHistoryRepository dataAdapterHistoryRepository;

    @Autowired
    private DataAdapterHistoryService dataAdapterHistoryService;

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

    private MockMvc restDataAdapterHistoryMockMvc;

    private DataAdapterHistory dataAdapterHistory;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        final DataAdapterHistoryResource dataAdapterHistoryResource = new DataAdapterHistoryResource(
            dataAdapterHistoryService,
            dataAdapterHistoryRepository
        );
        this.restDataAdapterHistoryMockMvc =
            MockMvcBuilders
                .standaloneSetup(dataAdapterHistoryResource)
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
    public static DataAdapterHistory createEntity(EntityManager em) {
        DataAdapterHistory dataAdapterHistory = new DataAdapterHistory().name(DEFAULT_NAME).details(DEFAULT_DETAILS);
        return dataAdapterHistory;
    }

    @Before
    public void initTest() {
        dataAdapterHistory = createEntity(em);
    }

    @Test
    @Transactional
    public void createDataAdapterHistory() throws Exception {
        int databaseSizeBeforeCreate = dataAdapterHistoryRepository.findAll().size();

        // Create the DataAdapterHistory
        restDataAdapterHistoryMockMvc
            .perform(
                post("/api/data-adapter-histories")
                    .contentType(TestUtil.APPLICATION_JSON_UTF8)
                    .content(TestUtil.convertObjectToJsonBytes(dataAdapterHistory))
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
    public void createDataAdapterHistoryWithExistingId() throws Exception {
        int databaseSizeBeforeCreate = dataAdapterHistoryRepository.findAll().size();

        // Create the DataAdapterHistory with an existing ID
        dataAdapterHistory.setId(1L);

        // An entity with an existing ID cannot be created, so this API call must fail
        restDataAdapterHistoryMockMvc
            .perform(
                post("/api/data-adapter-histories")
                    .contentType(TestUtil.APPLICATION_JSON_UTF8)
                    .content(TestUtil.convertObjectToJsonBytes(dataAdapterHistory))
            )
            .andExpect(status().isBadRequest());

        // Validate the DataAdapterHistory in the database
        List<DataAdapterHistory> dataAdapterHistoryList = dataAdapterHistoryRepository.findAll();
        assertThat(dataAdapterHistoryList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    public void checkNameIsRequired() throws Exception {
        int databaseSizeBeforeTest = dataAdapterHistoryRepository.findAll().size();
        // set the field null
        dataAdapterHistory.setName(null);

        // Create the DataAdapterHistory, which fails.

        restDataAdapterHistoryMockMvc
            .perform(
                post("/api/data-adapter-histories")
                    .contentType(TestUtil.APPLICATION_JSON_UTF8)
                    .content(TestUtil.convertObjectToJsonBytes(dataAdapterHistory))
            )
            .andExpect(status().isBadRequest());

        List<DataAdapterHistory> dataAdapterHistoryList = dataAdapterHistoryRepository.findAll();
        assertThat(dataAdapterHistoryList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkDetailsIsRequired() throws Exception {
        int databaseSizeBeforeTest = dataAdapterHistoryRepository.findAll().size();
        // set the field null
        dataAdapterHistory.setDetails(null);

        // Create the DataAdapterHistory, which fails.

        restDataAdapterHistoryMockMvc
            .perform(
                post("/api/data-adapter-histories")
                    .contentType(TestUtil.APPLICATION_JSON_UTF8)
                    .content(TestUtil.convertObjectToJsonBytes(dataAdapterHistory))
            )
            .andExpect(status().isBadRequest());

        List<DataAdapterHistory> dataAdapterHistoryList = dataAdapterHistoryRepository.findAll();
        assertThat(dataAdapterHistoryList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void getAllDataAdapterHistories() throws Exception {
        // Initialize the database
        dataAdapterHistoryRepository.saveAndFlush(dataAdapterHistory);

        // Get all the dataAdapterHistoryList
        restDataAdapterHistoryMockMvc
            .perform(get("/api/data-adapter-histories?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(dataAdapterHistory.getId().intValue())))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME.toString())))
            .andExpect(jsonPath("$.[*].details").value(hasItem(DEFAULT_DETAILS.toString())));
    }

    @Test
    @Transactional
    public void getDataAdapterHistory() throws Exception {
        // Initialize the database
        dataAdapterHistoryRepository.saveAndFlush(dataAdapterHistory);

        // Get the dataAdapterHistory
        restDataAdapterHistoryMockMvc
            .perform(get("/api/data-adapter-histories/{id}", dataAdapterHistory.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.id").value(dataAdapterHistory.getId().intValue()))
            .andExpect(jsonPath("$.name").value(DEFAULT_NAME.toString()))
            .andExpect(jsonPath("$.details").value(DEFAULT_DETAILS.toString()));
    }

    @Test
    @Transactional
    public void getNonExistingDataAdapterHistory() throws Exception {
        // Get the dataAdapterHistory
        restDataAdapterHistoryMockMvc.perform(get("/api/data-adapter-histories/{id}", Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updateDataAdapterHistory() throws Exception {
        // Initialize the database
        dataAdapterHistoryService.save(dataAdapterHistory);

        int databaseSizeBeforeUpdate = dataAdapterHistoryRepository.findAll().size();

        // Update the dataAdapterHistory
        DataAdapterHistory updatedDataAdapterHistory = dataAdapterHistoryRepository.findById(dataAdapterHistory.getId()).get();
        // Disconnect from session so that the updates on updatedDataAdapterHistory are not directly saved in db
        em.detach(updatedDataAdapterHistory);
        updatedDataAdapterHistory.name(UPDATED_NAME).details(UPDATED_DETAILS);

        restDataAdapterHistoryMockMvc
            .perform(
                put("/api/data-adapter-histories")
                    .contentType(TestUtil.APPLICATION_JSON_UTF8)
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
    public void updateNonExistingDataAdapterHistory() throws Exception {
        int databaseSizeBeforeUpdate = dataAdapterHistoryRepository.findAll().size();

        // Create the DataAdapterHistory

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restDataAdapterHistoryMockMvc
            .perform(
                put("/api/data-adapter-histories")
                    .contentType(TestUtil.APPLICATION_JSON_UTF8)
                    .content(TestUtil.convertObjectToJsonBytes(dataAdapterHistory))
            )
            .andExpect(status().isBadRequest());

        // Validate the DataAdapterHistory in the database
        List<DataAdapterHistory> dataAdapterHistoryList = dataAdapterHistoryRepository.findAll();
        assertThat(dataAdapterHistoryList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    public void deleteDataAdapterHistory() throws Exception {
        // Initialize the database
        dataAdapterHistoryService.save(dataAdapterHistory);

        int databaseSizeBeforeDelete = dataAdapterHistoryRepository.findAll().size();

        // Get the dataAdapterHistory
        restDataAdapterHistoryMockMvc
            .perform(delete("/api/data-adapter-histories/{id}", dataAdapterHistory.getId()).accept(TestUtil.APPLICATION_JSON_UTF8))
            .andExpect(status().isOk());

        // Validate the database is empty
        List<DataAdapterHistory> dataAdapterHistoryList = dataAdapterHistoryRepository.findAll();
        assertThat(dataAdapterHistoryList).hasSize(databaseSizeBeforeDelete - 1);
    }

    @Test
    @Transactional
    public void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(DataAdapterHistory.class);
        DataAdapterHistory dataAdapterHistory1 = new DataAdapterHistory();
        dataAdapterHistory1.setId(1L);
        DataAdapterHistory dataAdapterHistory2 = new DataAdapterHistory();
        dataAdapterHistory2.setId(dataAdapterHistory1.getId());
        assertThat(dataAdapterHistory1).isEqualTo(dataAdapterHistory2);
        dataAdapterHistory2.setId(2L);
        assertThat(dataAdapterHistory1).isNotEqualTo(dataAdapterHistory2);
        dataAdapterHistory1.setId(null);
        assertThat(dataAdapterHistory1).isNotEqualTo(dataAdapterHistory2);
    }
}
