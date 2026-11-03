package uz.paykap.web.rest;

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
import uz.paykap.domain.CheckVerifierParam;
import uz.paykap.domain.Recipient;
import uz.paykap.repository.CheckVerifierParamRepository;
import uz.paykap.web.rest.errors.ExceptionTranslator;

import javax.persistence.EntityManager;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static uz.paykap.web.rest.TestUtil.createFormattingConversionService;

/**
 * Test class for the CheckVerifierParamResource REST controller.
 *
 * @see CheckVerifierParamResource
 */
@RunWith(SpringRunner.class)
@IntegrationTest
public class CheckVerifierParamResourceIntTest {

    private static final String DEFAULT_ACCOUNT = "AAAAAAAAAA";
    private static final String UPDATED_ACCOUNT = "BBBBBBBBBB";

    private static final Long DEFAULT_AMOUNT_IN_TIYIN = 1L;
    private static final Long UPDATED_AMOUNT_IN_TIYIN = 2L;

    @Autowired
    private CheckVerifierParamRepository checkVerifierParamRepository;


    @Autowired
    private MappingJackson2HttpMessageConverter jacksonMessageConverter;

    @Autowired
    private PageableHandlerMethodArgumentResolver pageableArgumentResolver;

    @Autowired
    private ExceptionTranslator exceptionTranslator;

    @Autowired
    private EntityManager em;

    private MockMvc restCheckVerifierParamMockMvc;

    private CheckVerifierParam checkVerifierParam;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        final CheckVerifierParamResource checkVerifierParamResource = new CheckVerifierParamResource(checkVerifierParamRepository);
        this.restCheckVerifierParamMockMvc = MockMvcBuilders.standaloneSetup(checkVerifierParamResource)
            .setCustomArgumentResolvers(pageableArgumentResolver)
            .setControllerAdvice(exceptionTranslator)
            .setConversionService(createFormattingConversionService())
            .setMessageConverters(jacksonMessageConverter).build();
    }

    /**
     * Create an entity for this test.
     * <p>
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static CheckVerifierParam createEntity(EntityManager em) {
        CheckVerifierParam checkVerifierParam = new CheckVerifierParam()
            .account(DEFAULT_ACCOUNT)
            .amountInTiyin(DEFAULT_AMOUNT_IN_TIYIN);
        // Add required entity
        Recipient recipient = RecipientResourceIntTest.createEntity(em);
        em.persist(recipient);
        em.flush();
        checkVerifierParam.setRecipient(recipient);
        return checkVerifierParam;
    }

    @Before
    public void initTest() {
        checkVerifierParam = createEntity(em);
    }

    @Test
    @Transactional
    public void createCheckVerifierParam() throws Exception {
        int databaseSizeBeforeCreate = checkVerifierParamRepository.findAll().size();

        // Create the CheckVerifierParam
        restCheckVerifierParamMockMvc.perform(post("/api/check-verifier-params")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(checkVerifierParam)))
            .andExpect(status().isCreated());

        // Validate the CheckVerifierParam in the database
        List<CheckVerifierParam> checkVerifierParamList = checkVerifierParamRepository.findAll();
        assertThat(checkVerifierParamList).hasSize(databaseSizeBeforeCreate + 1);
        CheckVerifierParam testCheckVerifierParam = checkVerifierParamList.get(checkVerifierParamList.size() - 1);
        assertThat(testCheckVerifierParam.getAccount()).isEqualTo(DEFAULT_ACCOUNT);
        assertThat(testCheckVerifierParam.getAmountInTiyin()).isEqualTo(DEFAULT_AMOUNT_IN_TIYIN);
    }

    @Test
    @Transactional
    public void createCheckVerifierParamWithExistingId() throws Exception {
        int databaseSizeBeforeCreate = checkVerifierParamRepository.findAll().size();

        // Create the CheckVerifierParam with an existing ID
        checkVerifierParam.setId(1L);

        // An entity with an existing ID cannot be created, so this API call must fail
        restCheckVerifierParamMockMvc.perform(post("/api/check-verifier-params")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(checkVerifierParam)))
            .andExpect(status().isBadRequest());

        // Validate the CheckVerifierParam in the database
        List<CheckVerifierParam> checkVerifierParamList = checkVerifierParamRepository.findAll();
        assertThat(checkVerifierParamList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    public void getAllCheckVerifierParams() throws Exception {
        // Initialize the database
        checkVerifierParamRepository.saveAndFlush(checkVerifierParam);

        // Get all the checkVerifierParamList
        restCheckVerifierParamMockMvc.perform(get("/api/check-verifier-params?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(checkVerifierParam.getId().intValue())))
            .andExpect(jsonPath("$.[*].account").value(hasItem(DEFAULT_ACCOUNT.toString())))
            .andExpect(jsonPath("$.[*].amountInTiyin").value(hasItem(DEFAULT_AMOUNT_IN_TIYIN.intValue())));
    }


    @Test
    @Transactional
    public void getCheckVerifierParam() throws Exception {
        // Initialize the database
        checkVerifierParamRepository.saveAndFlush(checkVerifierParam);

        // Get the checkVerifierParam
        restCheckVerifierParamMockMvc.perform(get("/api/check-verifier-params/{id}", checkVerifierParam.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.id").value(checkVerifierParam.getId().intValue()))
            .andExpect(jsonPath("$.account").value(DEFAULT_ACCOUNT.toString()))
            .andExpect(jsonPath("$.amountInTiyin").value(DEFAULT_AMOUNT_IN_TIYIN.intValue()));
    }

    @Test
    @Transactional
    public void getNonExistingCheckVerifierParam() throws Exception {
        // Get the checkVerifierParam
        restCheckVerifierParamMockMvc.perform(get("/api/check-verifier-params/{id}", Long.MAX_VALUE))
            .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updateCheckVerifierParam() throws Exception {
        // Initialize the database
        checkVerifierParamRepository.saveAndFlush(checkVerifierParam);

        int databaseSizeBeforeUpdate = checkVerifierParamRepository.findAll().size();

        // Update the checkVerifierParam
        CheckVerifierParam updatedCheckVerifierParam = checkVerifierParamRepository.findById(checkVerifierParam.getId()).get();
        // Disconnect from session so that the updates on updatedCheckVerifierParam are not directly saved in db
        em.detach(updatedCheckVerifierParam);
        updatedCheckVerifierParam
            .account(UPDATED_ACCOUNT)
            .amountInTiyin(UPDATED_AMOUNT_IN_TIYIN);

        restCheckVerifierParamMockMvc.perform(put("/api/check-verifier-params")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(updatedCheckVerifierParam)))
            .andExpect(status().isOk());

        // Validate the CheckVerifierParam in the database
        List<CheckVerifierParam> checkVerifierParamList = checkVerifierParamRepository.findAll();
        assertThat(checkVerifierParamList).hasSize(databaseSizeBeforeUpdate);
        CheckVerifierParam testCheckVerifierParam = checkVerifierParamList.get(checkVerifierParamList.size() - 1);
        assertThat(testCheckVerifierParam.getAccount()).isEqualTo(UPDATED_ACCOUNT);
        assertThat(testCheckVerifierParam.getAmountInTiyin()).isEqualTo(UPDATED_AMOUNT_IN_TIYIN);
    }

    @Test
    @Transactional
    public void updateNonExistingCheckVerifierParam() throws Exception {
        int databaseSizeBeforeUpdate = checkVerifierParamRepository.findAll().size();

        // Create the CheckVerifierParam

        // If the entity doesn't have an ID, it will be created instead of just being updated
        restCheckVerifierParamMockMvc.perform(put("/api/check-verifier-params")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(checkVerifierParam)))
            .andExpect(status().isBadRequest());

        // Validate the CheckVerifierParam in the database
        List<CheckVerifierParam> checkVerifierParamList = checkVerifierParamRepository.findAll();
        assertThat(checkVerifierParamList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    public void deleteCheckVerifierParam() throws Exception {
        // Initialize the database
        checkVerifierParamRepository.saveAndFlush(checkVerifierParam);

        int databaseSizeBeforeDelete = checkVerifierParamRepository.findAll().size();

        // Get the checkVerifierParam
        restCheckVerifierParamMockMvc.perform(delete("/api/check-verifier-params/{id}", checkVerifierParam.getId())
            .accept(TestUtil.APPLICATION_JSON_UTF8))
            .andExpect(status().isOk());

        // Validate the database is empty
        List<CheckVerifierParam> checkVerifierParamList = checkVerifierParamRepository.findAll();
        assertThat(checkVerifierParamList).hasSize(databaseSizeBeforeDelete - 1);
    }

    @Test
    @Transactional
    public void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(CheckVerifierParam.class);
        CheckVerifierParam checkVerifierParam1 = new CheckVerifierParam();
        checkVerifierParam1.setId(1L);
        CheckVerifierParam checkVerifierParam2 = new CheckVerifierParam();
        checkVerifierParam2.setId(checkVerifierParam1.getId());
        assertThat(checkVerifierParam1).isEqualTo(checkVerifierParam2);
        checkVerifierParam2.setId(2L);
        assertThat(checkVerifierParam1).isNotEqualTo(checkVerifierParam2);
        checkVerifierParam1.setId(null);
        assertThat(checkVerifierParam1).isNotEqualTo(checkVerifierParam2);
    }
}
