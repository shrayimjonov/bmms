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
import uz.paykap.domain.CheckVerifierParam;
import uz.paykap.domain.Recipient;
import uz.paykap.repository.CheckVerifierParamRepository;

/**
 * Integration tests for the {@link CheckVerifierParamResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class CheckVerifierParamResourceIT {

    private static final String DEFAULT_ACCOUNT = "AAAAAAAAAA";
    private static final String UPDATED_ACCOUNT = "BBBBBBBBBB";

    private static final Long DEFAULT_AMOUNT_IN_TIYIN = 1L;
    private static final Long UPDATED_AMOUNT_IN_TIYIN = 2L;

    private static final String ENTITY_API_URL = "/api/check-verifier-params";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong count = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private CheckVerifierParamRepository checkVerifierParamRepository;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restCheckVerifierParamMockMvc;

    private CheckVerifierParam checkVerifierParam;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static CheckVerifierParam createEntity(EntityManager em) {
        CheckVerifierParam checkVerifierParam = new CheckVerifierParam().account(DEFAULT_ACCOUNT).amountInTiyin(DEFAULT_AMOUNT_IN_TIYIN);
        // Add required entity
        Recipient recipient;
        if (TestUtil.findAll(em, Recipient.class).isEmpty()) {
            recipient = RecipientResourceIT.createEntity(em);
            em.persist(recipient);
            em.flush();
        } else {
            recipient = TestUtil.findAll(em, Recipient.class).get(0);
        }
        checkVerifierParam.setRecipient(recipient);
        return checkVerifierParam;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static CheckVerifierParam createUpdatedEntity(EntityManager em) {
        CheckVerifierParam checkVerifierParam = new CheckVerifierParam().account(UPDATED_ACCOUNT).amountInTiyin(UPDATED_AMOUNT_IN_TIYIN);
        // Add required entity
        Recipient recipient;
        if (TestUtil.findAll(em, Recipient.class).isEmpty()) {
            recipient = RecipientResourceIT.createUpdatedEntity(em);
            em.persist(recipient);
            em.flush();
        } else {
            recipient = TestUtil.findAll(em, Recipient.class).get(0);
        }
        checkVerifierParam.setRecipient(recipient);
        return checkVerifierParam;
    }

    @BeforeEach
    public void initTest() {
        checkVerifierParam = createEntity(em);
    }

    @Test
    @Transactional
    void createCheckVerifierParam() throws Exception {
        int databaseSizeBeforeCreate = checkVerifierParamRepository.findAll().size();
        // Create the CheckVerifierParam
        restCheckVerifierParamMockMvc
            .perform(
                post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(checkVerifierParam))
            )
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
    void createCheckVerifierParamWithExistingId() throws Exception {
        // Create the CheckVerifierParam with an existing ID
        checkVerifierParam.setId(1L);

        int databaseSizeBeforeCreate = checkVerifierParamRepository.findAll().size();

        // An entity with an existing ID cannot be created, so this API call must fail
        restCheckVerifierParamMockMvc
            .perform(
                post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(checkVerifierParam))
            )
            .andExpect(status().isBadRequest());

        // Validate the CheckVerifierParam in the database
        List<CheckVerifierParam> checkVerifierParamList = checkVerifierParamRepository.findAll();
        assertThat(checkVerifierParamList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void getAllCheckVerifierParams() throws Exception {
        // Initialize the database
        checkVerifierParamRepository.saveAndFlush(checkVerifierParam);

        // Get all the checkVerifierParamList
        restCheckVerifierParamMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(checkVerifierParam.getId().intValue())))
            .andExpect(jsonPath("$.[*].account").value(hasItem(DEFAULT_ACCOUNT)))
            .andExpect(jsonPath("$.[*].amountInTiyin").value(hasItem(DEFAULT_AMOUNT_IN_TIYIN.intValue())));
    }

    @Test
    @Transactional
    void getCheckVerifierParam() throws Exception {
        // Initialize the database
        checkVerifierParamRepository.saveAndFlush(checkVerifierParam);

        // Get the checkVerifierParam
        restCheckVerifierParamMockMvc
            .perform(get(ENTITY_API_URL_ID, checkVerifierParam.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(checkVerifierParam.getId().intValue()))
            .andExpect(jsonPath("$.account").value(DEFAULT_ACCOUNT))
            .andExpect(jsonPath("$.amountInTiyin").value(DEFAULT_AMOUNT_IN_TIYIN.intValue()));
    }

    @Test
    @Transactional
    void getNonExistingCheckVerifierParam() throws Exception {
        // Get the checkVerifierParam
        restCheckVerifierParamMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putNewCheckVerifierParam() throws Exception {
        // Initialize the database
        checkVerifierParamRepository.saveAndFlush(checkVerifierParam);

        int databaseSizeBeforeUpdate = checkVerifierParamRepository.findAll().size();

        // Update the checkVerifierParam
        CheckVerifierParam updatedCheckVerifierParam = checkVerifierParamRepository.findById(checkVerifierParam.getId()).get();
        // Disconnect from session so that the updates on updatedCheckVerifierParam are not directly saved in db
        em.detach(updatedCheckVerifierParam);
        updatedCheckVerifierParam.account(UPDATED_ACCOUNT).amountInTiyin(UPDATED_AMOUNT_IN_TIYIN);

        restCheckVerifierParamMockMvc
            .perform(
                put(ENTITY_API_URL_ID, updatedCheckVerifierParam.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(updatedCheckVerifierParam))
            )
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
    void putNonExistingCheckVerifierParam() throws Exception {
        int databaseSizeBeforeUpdate = checkVerifierParamRepository.findAll().size();
        checkVerifierParam.setId(count.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restCheckVerifierParamMockMvc
            .perform(
                put(ENTITY_API_URL_ID, checkVerifierParam.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(checkVerifierParam))
            )
            .andExpect(status().isBadRequest());

        // Validate the CheckVerifierParam in the database
        List<CheckVerifierParam> checkVerifierParamList = checkVerifierParamRepository.findAll();
        assertThat(checkVerifierParamList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchCheckVerifierParam() throws Exception {
        int databaseSizeBeforeUpdate = checkVerifierParamRepository.findAll().size();
        checkVerifierParam.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restCheckVerifierParamMockMvc
            .perform(
                put(ENTITY_API_URL_ID, count.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(checkVerifierParam))
            )
            .andExpect(status().isBadRequest());

        // Validate the CheckVerifierParam in the database
        List<CheckVerifierParam> checkVerifierParamList = checkVerifierParamRepository.findAll();
        assertThat(checkVerifierParamList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamCheckVerifierParam() throws Exception {
        int databaseSizeBeforeUpdate = checkVerifierParamRepository.findAll().size();
        checkVerifierParam.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restCheckVerifierParamMockMvc
            .perform(
                put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(checkVerifierParam))
            )
            .andExpect(status().isMethodNotAllowed());

        // Validate the CheckVerifierParam in the database
        List<CheckVerifierParam> checkVerifierParamList = checkVerifierParamRepository.findAll();
        assertThat(checkVerifierParamList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateCheckVerifierParamWithPatch() throws Exception {
        // Initialize the database
        checkVerifierParamRepository.saveAndFlush(checkVerifierParam);

        int databaseSizeBeforeUpdate = checkVerifierParamRepository.findAll().size();

        // Update the checkVerifierParam using partial update
        CheckVerifierParam partialUpdatedCheckVerifierParam = new CheckVerifierParam();
        partialUpdatedCheckVerifierParam.setId(checkVerifierParam.getId());

        partialUpdatedCheckVerifierParam.amountInTiyin(UPDATED_AMOUNT_IN_TIYIN);

        restCheckVerifierParamMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedCheckVerifierParam.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedCheckVerifierParam))
            )
            .andExpect(status().isOk());

        // Validate the CheckVerifierParam in the database
        List<CheckVerifierParam> checkVerifierParamList = checkVerifierParamRepository.findAll();
        assertThat(checkVerifierParamList).hasSize(databaseSizeBeforeUpdate);
        CheckVerifierParam testCheckVerifierParam = checkVerifierParamList.get(checkVerifierParamList.size() - 1);
        assertThat(testCheckVerifierParam.getAccount()).isEqualTo(DEFAULT_ACCOUNT);
        assertThat(testCheckVerifierParam.getAmountInTiyin()).isEqualTo(UPDATED_AMOUNT_IN_TIYIN);
    }

    @Test
    @Transactional
    void fullUpdateCheckVerifierParamWithPatch() throws Exception {
        // Initialize the database
        checkVerifierParamRepository.saveAndFlush(checkVerifierParam);

        int databaseSizeBeforeUpdate = checkVerifierParamRepository.findAll().size();

        // Update the checkVerifierParam using partial update
        CheckVerifierParam partialUpdatedCheckVerifierParam = new CheckVerifierParam();
        partialUpdatedCheckVerifierParam.setId(checkVerifierParam.getId());

        partialUpdatedCheckVerifierParam.account(UPDATED_ACCOUNT).amountInTiyin(UPDATED_AMOUNT_IN_TIYIN);

        restCheckVerifierParamMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedCheckVerifierParam.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedCheckVerifierParam))
            )
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
    void patchNonExistingCheckVerifierParam() throws Exception {
        int databaseSizeBeforeUpdate = checkVerifierParamRepository.findAll().size();
        checkVerifierParam.setId(count.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restCheckVerifierParamMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, checkVerifierParam.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(checkVerifierParam))
            )
            .andExpect(status().isBadRequest());

        // Validate the CheckVerifierParam in the database
        List<CheckVerifierParam> checkVerifierParamList = checkVerifierParamRepository.findAll();
        assertThat(checkVerifierParamList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchCheckVerifierParam() throws Exception {
        int databaseSizeBeforeUpdate = checkVerifierParamRepository.findAll().size();
        checkVerifierParam.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restCheckVerifierParamMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, count.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(checkVerifierParam))
            )
            .andExpect(status().isBadRequest());

        // Validate the CheckVerifierParam in the database
        List<CheckVerifierParam> checkVerifierParamList = checkVerifierParamRepository.findAll();
        assertThat(checkVerifierParamList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamCheckVerifierParam() throws Exception {
        int databaseSizeBeforeUpdate = checkVerifierParamRepository.findAll().size();
        checkVerifierParam.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restCheckVerifierParamMockMvc
            .perform(
                patch(ENTITY_API_URL)
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(checkVerifierParam))
            )
            .andExpect(status().isMethodNotAllowed());

        // Validate the CheckVerifierParam in the database
        List<CheckVerifierParam> checkVerifierParamList = checkVerifierParamRepository.findAll();
        assertThat(checkVerifierParamList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteCheckVerifierParam() throws Exception {
        // Initialize the database
        checkVerifierParamRepository.saveAndFlush(checkVerifierParam);

        int databaseSizeBeforeDelete = checkVerifierParamRepository.findAll().size();

        // Delete the checkVerifierParam
        restCheckVerifierParamMockMvc
            .perform(delete(ENTITY_API_URL_ID, checkVerifierParam.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        List<CheckVerifierParam> checkVerifierParamList = checkVerifierParamRepository.findAll();
        assertThat(checkVerifierParamList).hasSize(databaseSizeBeforeDelete - 1);
    }
}
