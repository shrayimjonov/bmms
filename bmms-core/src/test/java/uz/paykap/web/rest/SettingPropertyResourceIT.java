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
import uz.paykap.domain.SettingProperty;
import uz.paykap.domain.enumeration.Prop;
import uz.paykap.repository.SettingPropertyRepository;
import uz.paykap.service.dto.SettingPropertyDTO;
import uz.paykap.service.mapper.SettingPropertyMapper;

/**
 * Integration tests for the {@link SettingPropertyResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class SettingPropertyResourceIT {

    private static final Prop DEFAULT_KEY = Prop.UZCARD_TERMINAL;
    private static final Prop UPDATED_KEY = Prop.UZCARD_MERCHANT;

    private static final String DEFAULT_VALUE = "AAAAAAAAAA";
    private static final String UPDATED_VALUE = "BBBBBBBBBB";

    private static final String ENTITY_API_URL = "/api/setting-properties";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong count = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private SettingPropertyRepository settingPropertyRepository;

    @Autowired
    private SettingPropertyMapper settingPropertyMapper;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restSettingPropertyMockMvc;

    private SettingProperty settingProperty;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static SettingProperty createEntity(EntityManager em) {
        SettingProperty settingProperty = new SettingProperty().key(DEFAULT_KEY).value(DEFAULT_VALUE);
        return settingProperty;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static SettingProperty createUpdatedEntity(EntityManager em) {
        SettingProperty settingProperty = new SettingProperty().key(UPDATED_KEY).value(UPDATED_VALUE);
        return settingProperty;
    }

    @BeforeEach
    public void initTest() {
        settingProperty = createEntity(em);
    }

    @Test
    @Transactional
    void createSettingProperty() throws Exception {
        int databaseSizeBeforeCreate = settingPropertyRepository.findAll().size();
        // Create the SettingProperty
        SettingPropertyDTO settingPropertyDTO = settingPropertyMapper.toDto(settingProperty);
        restSettingPropertyMockMvc
            .perform(
                post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(settingPropertyDTO))
            )
            .andExpect(status().isCreated());

        // Validate the SettingProperty in the database
        List<SettingProperty> settingPropertyList = settingPropertyRepository.findAll();
        assertThat(settingPropertyList).hasSize(databaseSizeBeforeCreate + 1);
        SettingProperty testSettingProperty = settingPropertyList.get(settingPropertyList.size() - 1);
        assertThat(testSettingProperty.getKey()).isEqualTo(DEFAULT_KEY);
        assertThat(testSettingProperty.getValue()).isEqualTo(DEFAULT_VALUE);
    }

    @Test
    @Transactional
    void createSettingPropertyWithExistingId() throws Exception {
        // Create the SettingProperty with an existing ID
        settingProperty.setId(1L);
        SettingPropertyDTO settingPropertyDTO = settingPropertyMapper.toDto(settingProperty);

        int databaseSizeBeforeCreate = settingPropertyRepository.findAll().size();

        // An entity with an existing ID cannot be created, so this API call must fail
        restSettingPropertyMockMvc
            .perform(
                post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(settingPropertyDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the SettingProperty in the database
        List<SettingProperty> settingPropertyList = settingPropertyRepository.findAll();
        assertThat(settingPropertyList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkKeyIsRequired() throws Exception {
        int databaseSizeBeforeTest = settingPropertyRepository.findAll().size();
        // set the field null
        settingProperty.setKey(null);

        // Create the SettingProperty, which fails.
        SettingPropertyDTO settingPropertyDTO = settingPropertyMapper.toDto(settingProperty);

        restSettingPropertyMockMvc
            .perform(
                post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(settingPropertyDTO))
            )
            .andExpect(status().isBadRequest());

        List<SettingProperty> settingPropertyList = settingPropertyRepository.findAll();
        assertThat(settingPropertyList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkValueIsRequired() throws Exception {
        int databaseSizeBeforeTest = settingPropertyRepository.findAll().size();
        // set the field null
        settingProperty.setValue(null);

        // Create the SettingProperty, which fails.
        SettingPropertyDTO settingPropertyDTO = settingPropertyMapper.toDto(settingProperty);

        restSettingPropertyMockMvc
            .perform(
                post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(settingPropertyDTO))
            )
            .andExpect(status().isBadRequest());

        List<SettingProperty> settingPropertyList = settingPropertyRepository.findAll();
        assertThat(settingPropertyList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllSettingProperties() throws Exception {
        // Initialize the database
        settingPropertyRepository.saveAndFlush(settingProperty);

        // Get all the settingPropertyList
        restSettingPropertyMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(settingProperty.getId().intValue())))
            .andExpect(jsonPath("$.[*].key").value(hasItem(DEFAULT_KEY.toString())))
            .andExpect(jsonPath("$.[*].value").value(hasItem(DEFAULT_VALUE)));
    }

    @Test
    @Transactional
    void getSettingProperty() throws Exception {
        // Initialize the database
        settingPropertyRepository.saveAndFlush(settingProperty);

        // Get the settingProperty
        restSettingPropertyMockMvc
            .perform(get(ENTITY_API_URL_ID, settingProperty.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(settingProperty.getId().intValue()))
            .andExpect(jsonPath("$.key").value(DEFAULT_KEY.toString()))
            .andExpect(jsonPath("$.value").value(DEFAULT_VALUE));
    }

    @Test
    @Transactional
    void getNonExistingSettingProperty() throws Exception {
        // Get the settingProperty
        restSettingPropertyMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putNewSettingProperty() throws Exception {
        // Initialize the database
        settingPropertyRepository.saveAndFlush(settingProperty);

        int databaseSizeBeforeUpdate = settingPropertyRepository.findAll().size();

        // Update the settingProperty
        SettingProperty updatedSettingProperty = settingPropertyRepository.findById(settingProperty.getId()).get();
        // Disconnect from session so that the updates on updatedSettingProperty are not directly saved in db
        em.detach(updatedSettingProperty);
        updatedSettingProperty.key(UPDATED_KEY).value(UPDATED_VALUE);
        SettingPropertyDTO settingPropertyDTO = settingPropertyMapper.toDto(updatedSettingProperty);

        restSettingPropertyMockMvc
            .perform(
                put(ENTITY_API_URL_ID, settingPropertyDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(settingPropertyDTO))
            )
            .andExpect(status().isOk());

        // Validate the SettingProperty in the database
        List<SettingProperty> settingPropertyList = settingPropertyRepository.findAll();
        assertThat(settingPropertyList).hasSize(databaseSizeBeforeUpdate);
        SettingProperty testSettingProperty = settingPropertyList.get(settingPropertyList.size() - 1);
        assertThat(testSettingProperty.getKey()).isEqualTo(UPDATED_KEY);
        assertThat(testSettingProperty.getValue()).isEqualTo(UPDATED_VALUE);
    }

    @Test
    @Transactional
    void putNonExistingSettingProperty() throws Exception {
        int databaseSizeBeforeUpdate = settingPropertyRepository.findAll().size();
        settingProperty.setId(count.incrementAndGet());

        // Create the SettingProperty
        SettingPropertyDTO settingPropertyDTO = settingPropertyMapper.toDto(settingProperty);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restSettingPropertyMockMvc
            .perform(
                put(ENTITY_API_URL_ID, settingPropertyDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(settingPropertyDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the SettingProperty in the database
        List<SettingProperty> settingPropertyList = settingPropertyRepository.findAll();
        assertThat(settingPropertyList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchSettingProperty() throws Exception {
        int databaseSizeBeforeUpdate = settingPropertyRepository.findAll().size();
        settingProperty.setId(count.incrementAndGet());

        // Create the SettingProperty
        SettingPropertyDTO settingPropertyDTO = settingPropertyMapper.toDto(settingProperty);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restSettingPropertyMockMvc
            .perform(
                put(ENTITY_API_URL_ID, count.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(settingPropertyDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the SettingProperty in the database
        List<SettingProperty> settingPropertyList = settingPropertyRepository.findAll();
        assertThat(settingPropertyList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamSettingProperty() throws Exception {
        int databaseSizeBeforeUpdate = settingPropertyRepository.findAll().size();
        settingProperty.setId(count.incrementAndGet());

        // Create the SettingProperty
        SettingPropertyDTO settingPropertyDTO = settingPropertyMapper.toDto(settingProperty);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restSettingPropertyMockMvc
            .perform(
                put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(settingPropertyDTO))
            )
            .andExpect(status().isMethodNotAllowed());

        // Validate the SettingProperty in the database
        List<SettingProperty> settingPropertyList = settingPropertyRepository.findAll();
        assertThat(settingPropertyList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateSettingPropertyWithPatch() throws Exception {
        // Initialize the database
        settingPropertyRepository.saveAndFlush(settingProperty);

        int databaseSizeBeforeUpdate = settingPropertyRepository.findAll().size();

        // Update the settingProperty using partial update
        SettingProperty partialUpdatedSettingProperty = new SettingProperty();
        partialUpdatedSettingProperty.setId(settingProperty.getId());

        partialUpdatedSettingProperty.value(UPDATED_VALUE);

        restSettingPropertyMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedSettingProperty.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedSettingProperty))
            )
            .andExpect(status().isOk());

        // Validate the SettingProperty in the database
        List<SettingProperty> settingPropertyList = settingPropertyRepository.findAll();
        assertThat(settingPropertyList).hasSize(databaseSizeBeforeUpdate);
        SettingProperty testSettingProperty = settingPropertyList.get(settingPropertyList.size() - 1);
        assertThat(testSettingProperty.getKey()).isEqualTo(DEFAULT_KEY);
        assertThat(testSettingProperty.getValue()).isEqualTo(UPDATED_VALUE);
    }

    @Test
    @Transactional
    void fullUpdateSettingPropertyWithPatch() throws Exception {
        // Initialize the database
        settingPropertyRepository.saveAndFlush(settingProperty);

        int databaseSizeBeforeUpdate = settingPropertyRepository.findAll().size();

        // Update the settingProperty using partial update
        SettingProperty partialUpdatedSettingProperty = new SettingProperty();
        partialUpdatedSettingProperty.setId(settingProperty.getId());

        partialUpdatedSettingProperty.key(UPDATED_KEY).value(UPDATED_VALUE);

        restSettingPropertyMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedSettingProperty.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedSettingProperty))
            )
            .andExpect(status().isOk());

        // Validate the SettingProperty in the database
        List<SettingProperty> settingPropertyList = settingPropertyRepository.findAll();
        assertThat(settingPropertyList).hasSize(databaseSizeBeforeUpdate);
        SettingProperty testSettingProperty = settingPropertyList.get(settingPropertyList.size() - 1);
        assertThat(testSettingProperty.getKey()).isEqualTo(UPDATED_KEY);
        assertThat(testSettingProperty.getValue()).isEqualTo(UPDATED_VALUE);
    }

    @Test
    @Transactional
    void patchNonExistingSettingProperty() throws Exception {
        int databaseSizeBeforeUpdate = settingPropertyRepository.findAll().size();
        settingProperty.setId(count.incrementAndGet());

        // Create the SettingProperty
        SettingPropertyDTO settingPropertyDTO = settingPropertyMapper.toDto(settingProperty);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restSettingPropertyMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, settingPropertyDTO.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(settingPropertyDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the SettingProperty in the database
        List<SettingProperty> settingPropertyList = settingPropertyRepository.findAll();
        assertThat(settingPropertyList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchSettingProperty() throws Exception {
        int databaseSizeBeforeUpdate = settingPropertyRepository.findAll().size();
        settingProperty.setId(count.incrementAndGet());

        // Create the SettingProperty
        SettingPropertyDTO settingPropertyDTO = settingPropertyMapper.toDto(settingProperty);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restSettingPropertyMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, count.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(settingPropertyDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the SettingProperty in the database
        List<SettingProperty> settingPropertyList = settingPropertyRepository.findAll();
        assertThat(settingPropertyList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamSettingProperty() throws Exception {
        int databaseSizeBeforeUpdate = settingPropertyRepository.findAll().size();
        settingProperty.setId(count.incrementAndGet());

        // Create the SettingProperty
        SettingPropertyDTO settingPropertyDTO = settingPropertyMapper.toDto(settingProperty);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restSettingPropertyMockMvc
            .perform(
                patch(ENTITY_API_URL)
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(settingPropertyDTO))
            )
            .andExpect(status().isMethodNotAllowed());

        // Validate the SettingProperty in the database
        List<SettingProperty> settingPropertyList = settingPropertyRepository.findAll();
        assertThat(settingPropertyList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteSettingProperty() throws Exception {
        // Initialize the database
        settingPropertyRepository.saveAndFlush(settingProperty);

        int databaseSizeBeforeDelete = settingPropertyRepository.findAll().size();

        // Delete the settingProperty
        restSettingPropertyMockMvc
            .perform(delete(ENTITY_API_URL_ID, settingProperty.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        List<SettingProperty> settingPropertyList = settingPropertyRepository.findAll();
        assertThat(settingPropertyList).hasSize(databaseSizeBeforeDelete - 1);
    }
}
