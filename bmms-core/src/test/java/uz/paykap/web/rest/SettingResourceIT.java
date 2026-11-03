package uz.paykap.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;
import javax.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import uz.paykap.IntegrationTest;
import uz.paykap.domain.Setting;
import uz.paykap.domain.SettingProperty;
import uz.paykap.repository.SettingRepository;
import uz.paykap.service.SettingService;
import uz.paykap.service.criteria.SettingCriteria;
import uz.paykap.service.dto.SettingDTO;
import uz.paykap.service.mapper.SettingMapper;

/**
 * Integration tests for the {@link SettingResource} REST controller.
 */
@IntegrationTest
@ExtendWith(MockitoExtension.class)
@AutoConfigureMockMvc
@WithMockUser
class SettingResourceIT {

    private static final String DEFAULT_SHORT_NAME = "AAAAAAAAAA";
    private static final String UPDATED_SHORT_NAME = "BBBBBBBBBB";

    private static final Integer DEFAULT_ORD = 1;
    private static final Integer UPDATED_ORD = 2;
    private static final Integer SMALLER_ORD = 1 - 1;

    private static final Boolean DEFAULT_ACTIVE = false;
    private static final Boolean UPDATED_ACTIVE = true;

    private static final String ENTITY_API_URL = "/api/settings";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong count = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private SettingRepository settingRepository;

    @Mock
    private SettingRepository settingRepositoryMock;

    @Autowired
    private SettingMapper settingMapper;

    @Mock
    private SettingService settingServiceMock;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restSettingMockMvc;

    private Setting setting;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Setting createEntity(EntityManager em) {
        Setting setting = new Setting().shortName(DEFAULT_SHORT_NAME).ord(DEFAULT_ORD).active(DEFAULT_ACTIVE);
        // Add required entity
        SettingProperty settingProperty;
        if (TestUtil.findAll(em, SettingProperty.class).isEmpty()) {
            settingProperty = SettingPropertyResourceIT.createEntity(em);
            //            em.persist(settingProperty);
            //            em.flush();
        } else {
            settingProperty = TestUtil.findAll(em, SettingProperty.class).get(0);
        }
        setting.getProps().put(settingProperty.getKey(), settingProperty);
        return setting;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Setting createUpdatedEntity(EntityManager em) {
        Setting setting = new Setting().shortName(UPDATED_SHORT_NAME).ord(UPDATED_ORD).active(UPDATED_ACTIVE);
        // Add required entity
        SettingProperty settingProperty;
        if (TestUtil.findAll(em, SettingProperty.class).isEmpty()) {
            settingProperty = SettingPropertyResourceIT.createUpdatedEntity(em);
            //            em.persist(settingProperty);
            //            em.flush();
        } else {
            settingProperty = TestUtil.findAll(em, SettingProperty.class).get(0);
        }
        setting.getProps().put(settingProperty.getKey(), settingProperty);
        return setting;
    }

    @BeforeEach
    public void initTest() {
        setting = createEntity(em);
    }

    @Test
    @Transactional
    void createSetting() throws Exception {
        int databaseSizeBeforeCreate = settingRepository.findAll().size();
        // Create the Setting
        SettingDTO settingDTO = settingMapper.toDto(setting);
        restSettingMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(settingDTO)))
            .andExpect(status().isCreated());

        // Validate the Setting in the database
        List<Setting> settingList = settingRepository.findAll();
        assertThat(settingList).hasSize(databaseSizeBeforeCreate + 1);
        Setting testSetting = settingList.get(settingList.size() - 1);
        assertThat(testSetting.getShortName()).isEqualTo(DEFAULT_SHORT_NAME);
        assertThat(testSetting.getOrd()).isEqualTo(DEFAULT_ORD);
        assertThat(testSetting.getActive()).isEqualTo(DEFAULT_ACTIVE);
    }

    @Test
    @Transactional
    void createSettingWithExistingId() throws Exception {
        // Create the Setting with an existing ID
        setting.setId(1L);
        SettingDTO settingDTO = settingMapper.toDto(setting);

        int databaseSizeBeforeCreate = settingRepository.findAll().size();

        // An entity with an existing ID cannot be created, so this API call must fail
        restSettingMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(settingDTO)))
            .andExpect(status().isBadRequest());

        // Validate the Setting in the database
        List<Setting> settingList = settingRepository.findAll();
        assertThat(settingList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkShortNameIsRequired() throws Exception {
        int databaseSizeBeforeTest = settingRepository.findAll().size();
        // set the field null
        setting.setShortName(null);

        // Create the Setting, which fails.
        SettingDTO settingDTO = settingMapper.toDto(setting);

        restSettingMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(settingDTO)))
            .andExpect(status().isBadRequest());

        List<Setting> settingList = settingRepository.findAll();
        assertThat(settingList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkActiveIsRequired() throws Exception {
        int databaseSizeBeforeTest = settingRepository.findAll().size();
        // set the field null
        setting.setActive(null);

        // Create the Setting, which fails.
        SettingDTO settingDTO = settingMapper.toDto(setting);

        restSettingMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(settingDTO)))
            .andExpect(status().isBadRequest());

        List<Setting> settingList = settingRepository.findAll();
        assertThat(settingList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllSettings() throws Exception {
        // Initialize the database
        settingRepository.saveAndFlush(setting);

        // Get all the settingList
        restSettingMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(setting.getId().intValue())))
            .andExpect(jsonPath("$.[*].shortName").value(hasItem(DEFAULT_SHORT_NAME)))
            .andExpect(jsonPath("$.[*].ord").value(hasItem(DEFAULT_ORD)))
            .andExpect(jsonPath("$.[*].active").value(hasItem(DEFAULT_ACTIVE.booleanValue())));
    }

    @SuppressWarnings({ "unchecked" })
    void getAllSettingsWithEagerRelationshipsIsEnabled() throws Exception {
        when(settingServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restSettingMockMvc.perform(get(ENTITY_API_URL + "?eagerload=true")).andExpect(status().isOk());

        verify(settingServiceMock, times(1)).findAllWithEagerRelationships(any());
    }

    @SuppressWarnings({ "unchecked" })
    void getAllSettingsWithEagerRelationshipsIsNotEnabled() throws Exception {
        when(settingServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restSettingMockMvc.perform(get(ENTITY_API_URL + "?eagerload=true")).andExpect(status().isOk());

        verify(settingServiceMock, times(1)).findAllWithEagerRelationships(any());
    }

    @Test
    @Transactional
    void getSetting() throws Exception {
        // Initialize the database
        settingRepository.saveAndFlush(setting);

        // Get the setting
        restSettingMockMvc
            .perform(get(ENTITY_API_URL_ID, setting.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(setting.getId().intValue()))
            .andExpect(jsonPath("$.shortName").value(DEFAULT_SHORT_NAME))
            .andExpect(jsonPath("$.ord").value(DEFAULT_ORD))
            .andExpect(jsonPath("$.active").value(DEFAULT_ACTIVE.booleanValue()));
    }

    @Test
    @Transactional
    void getSettingsByIdFiltering() throws Exception {
        // Initialize the database
        settingRepository.saveAndFlush(setting);

        Long id = setting.getId();

        defaultSettingShouldBeFound("id.equals=" + id);
        defaultSettingShouldNotBeFound("id.notEquals=" + id);

        defaultSettingShouldBeFound("id.greaterThanOrEqual=" + id);
        defaultSettingShouldNotBeFound("id.greaterThan=" + id);

        defaultSettingShouldBeFound("id.lessThanOrEqual=" + id);
        defaultSettingShouldNotBeFound("id.lessThan=" + id);
    }

    @Test
    @Transactional
    void getAllSettingsByShortNameIsEqualToSomething() throws Exception {
        // Initialize the database
        settingRepository.saveAndFlush(setting);

        // Get all the settingList where shortName equals to DEFAULT_SHORT_NAME
        defaultSettingShouldBeFound("shortName.equals=" + DEFAULT_SHORT_NAME);

        // Get all the settingList where shortName equals to UPDATED_SHORT_NAME
        defaultSettingShouldNotBeFound("shortName.equals=" + UPDATED_SHORT_NAME);
    }

    @Test
    @Transactional
    void getAllSettingsByShortNameIsNotEqualToSomething() throws Exception {
        // Initialize the database
        settingRepository.saveAndFlush(setting);

        // Get all the settingList where shortName not equals to DEFAULT_SHORT_NAME
        defaultSettingShouldNotBeFound("shortName.notEquals=" + DEFAULT_SHORT_NAME);

        // Get all the settingList where shortName not equals to UPDATED_SHORT_NAME
        defaultSettingShouldBeFound("shortName.notEquals=" + UPDATED_SHORT_NAME);
    }

    @Test
    @Transactional
    void getAllSettingsByShortNameIsInShouldWork() throws Exception {
        // Initialize the database
        settingRepository.saveAndFlush(setting);

        // Get all the settingList where shortName in DEFAULT_SHORT_NAME or UPDATED_SHORT_NAME
        defaultSettingShouldBeFound("shortName.in=" + DEFAULT_SHORT_NAME + "," + UPDATED_SHORT_NAME);

        // Get all the settingList where shortName equals to UPDATED_SHORT_NAME
        defaultSettingShouldNotBeFound("shortName.in=" + UPDATED_SHORT_NAME);
    }

    @Test
    @Transactional
    void getAllSettingsByShortNameIsNullOrNotNull() throws Exception {
        // Initialize the database
        settingRepository.saveAndFlush(setting);

        // Get all the settingList where shortName is not null
        defaultSettingShouldBeFound("shortName.specified=true");

        // Get all the settingList where shortName is null
        defaultSettingShouldNotBeFound("shortName.specified=false");
    }

    @Test
    @Transactional
    void getAllSettingsByShortNameContainsSomething() throws Exception {
        // Initialize the database
        settingRepository.saveAndFlush(setting);

        // Get all the settingList where shortName contains DEFAULT_SHORT_NAME
        defaultSettingShouldBeFound("shortName.contains=" + DEFAULT_SHORT_NAME);

        // Get all the settingList where shortName contains UPDATED_SHORT_NAME
        defaultSettingShouldNotBeFound("shortName.contains=" + UPDATED_SHORT_NAME);
    }

    @Test
    @Transactional
    void getAllSettingsByShortNameNotContainsSomething() throws Exception {
        // Initialize the database
        settingRepository.saveAndFlush(setting);

        // Get all the settingList where shortName does not contain DEFAULT_SHORT_NAME
        defaultSettingShouldNotBeFound("shortName.doesNotContain=" + DEFAULT_SHORT_NAME);

        // Get all the settingList where shortName does not contain UPDATED_SHORT_NAME
        defaultSettingShouldBeFound("shortName.doesNotContain=" + UPDATED_SHORT_NAME);
    }

    @Test
    @Transactional
    void getAllSettingsByOrdIsEqualToSomething() throws Exception {
        // Initialize the database
        settingRepository.saveAndFlush(setting);

        // Get all the settingList where ord equals to DEFAULT_ORD
        defaultSettingShouldBeFound("ord.equals=" + DEFAULT_ORD);

        // Get all the settingList where ord equals to UPDATED_ORD
        defaultSettingShouldNotBeFound("ord.equals=" + UPDATED_ORD);
    }

    @Test
    @Transactional
    void getAllSettingsByOrdIsNotEqualToSomething() throws Exception {
        // Initialize the database
        settingRepository.saveAndFlush(setting);

        // Get all the settingList where ord not equals to DEFAULT_ORD
        defaultSettingShouldNotBeFound("ord.notEquals=" + DEFAULT_ORD);

        // Get all the settingList where ord not equals to UPDATED_ORD
        defaultSettingShouldBeFound("ord.notEquals=" + UPDATED_ORD);
    }

    @Test
    @Transactional
    void getAllSettingsByOrdIsInShouldWork() throws Exception {
        // Initialize the database
        settingRepository.saveAndFlush(setting);

        // Get all the settingList where ord in DEFAULT_ORD or UPDATED_ORD
        defaultSettingShouldBeFound("ord.in=" + DEFAULT_ORD + "," + UPDATED_ORD);

        // Get all the settingList where ord equals to UPDATED_ORD
        defaultSettingShouldNotBeFound("ord.in=" + UPDATED_ORD);
    }

    @Test
    @Transactional
    void getAllSettingsByOrdIsNullOrNotNull() throws Exception {
        // Initialize the database
        settingRepository.saveAndFlush(setting);

        // Get all the settingList where ord is not null
        defaultSettingShouldBeFound("ord.specified=true");

        // Get all the settingList where ord is null
        defaultSettingShouldNotBeFound("ord.specified=false");
    }

    @Test
    @Transactional
    void getAllSettingsByOrdIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        settingRepository.saveAndFlush(setting);

        // Get all the settingList where ord is greater than or equal to DEFAULT_ORD
        defaultSettingShouldBeFound("ord.greaterThanOrEqual=" + DEFAULT_ORD);

        // Get all the settingList where ord is greater than or equal to UPDATED_ORD
        defaultSettingShouldNotBeFound("ord.greaterThanOrEqual=" + UPDATED_ORD);
    }

    @Test
    @Transactional
    void getAllSettingsByOrdIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        settingRepository.saveAndFlush(setting);

        // Get all the settingList where ord is less than or equal to DEFAULT_ORD
        defaultSettingShouldBeFound("ord.lessThanOrEqual=" + DEFAULT_ORD);

        // Get all the settingList where ord is less than or equal to SMALLER_ORD
        defaultSettingShouldNotBeFound("ord.lessThanOrEqual=" + SMALLER_ORD);
    }

    @Test
    @Transactional
    void getAllSettingsByOrdIsLessThanSomething() throws Exception {
        // Initialize the database
        settingRepository.saveAndFlush(setting);

        // Get all the settingList where ord is less than DEFAULT_ORD
        defaultSettingShouldNotBeFound("ord.lessThan=" + DEFAULT_ORD);

        // Get all the settingList where ord is less than UPDATED_ORD
        defaultSettingShouldBeFound("ord.lessThan=" + UPDATED_ORD);
    }

    @Test
    @Transactional
    void getAllSettingsByOrdIsGreaterThanSomething() throws Exception {
        // Initialize the database
        settingRepository.saveAndFlush(setting);

        // Get all the settingList where ord is greater than DEFAULT_ORD
        defaultSettingShouldNotBeFound("ord.greaterThan=" + DEFAULT_ORD);

        // Get all the settingList where ord is greater than SMALLER_ORD
        defaultSettingShouldBeFound("ord.greaterThan=" + SMALLER_ORD);
    }

    @Test
    @Transactional
    void getAllSettingsByActiveIsEqualToSomething() throws Exception {
        // Initialize the database
        settingRepository.saveAndFlush(setting);

        // Get all the settingList where active equals to DEFAULT_ACTIVE
        defaultSettingShouldBeFound("active.equals=" + DEFAULT_ACTIVE);

        // Get all the settingList where active equals to UPDATED_ACTIVE
        defaultSettingShouldNotBeFound("active.equals=" + UPDATED_ACTIVE);
    }

    @Test
    @Transactional
    void getAllSettingsByActiveIsNotEqualToSomething() throws Exception {
        // Initialize the database
        settingRepository.saveAndFlush(setting);

        // Get all the settingList where active not equals to DEFAULT_ACTIVE
        defaultSettingShouldNotBeFound("active.notEquals=" + DEFAULT_ACTIVE);

        // Get all the settingList where active not equals to UPDATED_ACTIVE
        defaultSettingShouldBeFound("active.notEquals=" + UPDATED_ACTIVE);
    }

    @Test
    @Transactional
    void getAllSettingsByActiveIsInShouldWork() throws Exception {
        // Initialize the database
        settingRepository.saveAndFlush(setting);

        // Get all the settingList where active in DEFAULT_ACTIVE or UPDATED_ACTIVE
        defaultSettingShouldBeFound("active.in=" + DEFAULT_ACTIVE + "," + UPDATED_ACTIVE);

        // Get all the settingList where active equals to UPDATED_ACTIVE
        defaultSettingShouldNotBeFound("active.in=" + UPDATED_ACTIVE);
    }

    @Test
    @Transactional
    void getAllSettingsByActiveIsNullOrNotNull() throws Exception {
        // Initialize the database
        settingRepository.saveAndFlush(setting);

        // Get all the settingList where active is not null
        defaultSettingShouldBeFound("active.specified=true");

        // Get all the settingList where active is null
        defaultSettingShouldNotBeFound("active.specified=false");
    }

    @Test
    @Transactional
    void getAllSettingsByPropIsEqualToSomething() throws Exception {
        // Initialize the database
        settingRepository.saveAndFlush(setting);
        SettingProperty prop = SettingPropertyResourceIT.createEntity(em);
        em.persist(prop);
        em.flush();
        setting.addProp(prop);
        settingRepository.saveAndFlush(setting);
        Long propId = prop.getId();

        // Get all the settingList where prop equals to propId
        defaultSettingShouldBeFound("propId.equals=" + propId);

        // Get all the settingList where prop equals to (propId + 1)
        defaultSettingShouldNotBeFound("propId.equals=" + (propId + 1));
    }

    /**
     * Executes the search, and checks that the default entity is returned.
     */
    private void defaultSettingShouldBeFound(String filter) throws Exception {
        restSettingMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(setting.getId().intValue())))
            .andExpect(jsonPath("$.[*].shortName").value(hasItem(DEFAULT_SHORT_NAME)))
            .andExpect(jsonPath("$.[*].ord").value(hasItem(DEFAULT_ORD)))
            .andExpect(jsonPath("$.[*].active").value(hasItem(DEFAULT_ACTIVE.booleanValue())));

        // Check, that the count call also returns 1
        restSettingMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("1"));
    }

    /**
     * Executes the search, and checks that the default entity is not returned.
     */
    private void defaultSettingShouldNotBeFound(String filter) throws Exception {
        restSettingMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty());

        // Check, that the count call also returns 0
        restSettingMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("0"));
    }

    @Test
    @Transactional
    void getNonExistingSetting() throws Exception {
        // Get the setting
        restSettingMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putNewSetting() throws Exception {
        // Initialize the database
        settingRepository.saveAndFlush(setting);

        int databaseSizeBeforeUpdate = settingRepository.findAll().size();

        // Update the setting
        Setting updatedSetting = settingRepository.findById(setting.getId()).get();
        // Disconnect from session so that the updates on updatedSetting are not directly saved in db
        em.detach(updatedSetting);
        updatedSetting.shortName(UPDATED_SHORT_NAME).ord(UPDATED_ORD).active(UPDATED_ACTIVE);
        SettingDTO settingDTO = settingMapper.toDto(updatedSetting);

        restSettingMockMvc
            .perform(
                put(ENTITY_API_URL_ID, settingDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(settingDTO))
            )
            .andExpect(status().isOk());

        // Validate the Setting in the database
        List<Setting> settingList = settingRepository.findAll();
        assertThat(settingList).hasSize(databaseSizeBeforeUpdate);
        Setting testSetting = settingList.get(settingList.size() - 1);
        assertThat(testSetting.getShortName()).isEqualTo(UPDATED_SHORT_NAME);
        assertThat(testSetting.getOrd()).isEqualTo(UPDATED_ORD);
        assertThat(testSetting.getActive()).isEqualTo(UPDATED_ACTIVE);
    }

    @Test
    @Transactional
    void putNonExistingSetting() throws Exception {
        int databaseSizeBeforeUpdate = settingRepository.findAll().size();
        setting.setId(count.incrementAndGet());

        // Create the Setting
        SettingDTO settingDTO = settingMapper.toDto(setting);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restSettingMockMvc
            .perform(
                put(ENTITY_API_URL_ID, settingDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(settingDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Setting in the database
        List<Setting> settingList = settingRepository.findAll();
        assertThat(settingList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchSetting() throws Exception {
        int databaseSizeBeforeUpdate = settingRepository.findAll().size();
        setting.setId(count.incrementAndGet());

        // Create the Setting
        SettingDTO settingDTO = settingMapper.toDto(setting);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restSettingMockMvc
            .perform(
                put(ENTITY_API_URL_ID, count.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(settingDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Setting in the database
        List<Setting> settingList = settingRepository.findAll();
        assertThat(settingList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamSetting() throws Exception {
        int databaseSizeBeforeUpdate = settingRepository.findAll().size();
        setting.setId(count.incrementAndGet());

        // Create the Setting
        SettingDTO settingDTO = settingMapper.toDto(setting);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restSettingMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(settingDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Setting in the database
        List<Setting> settingList = settingRepository.findAll();
        assertThat(settingList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateSettingWithPatch() throws Exception {
        // Initialize the database
        settingRepository.saveAndFlush(setting);

        int databaseSizeBeforeUpdate = settingRepository.findAll().size();

        // Update the setting using partial update
        Setting partialUpdatedSetting = new Setting();
        partialUpdatedSetting.setId(setting.getId());

        restSettingMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedSetting.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedSetting))
            )
            .andExpect(status().isOk());

        // Validate the Setting in the database
        List<Setting> settingList = settingRepository.findAll();
        assertThat(settingList).hasSize(databaseSizeBeforeUpdate);
        Setting testSetting = settingList.get(settingList.size() - 1);
        assertThat(testSetting.getShortName()).isEqualTo(DEFAULT_SHORT_NAME);
        assertThat(testSetting.getOrd()).isEqualTo(DEFAULT_ORD);
        assertThat(testSetting.getActive()).isEqualTo(DEFAULT_ACTIVE);
    }

    @Test
    @Transactional
    void fullUpdateSettingWithPatch() throws Exception {
        // Initialize the database
        settingRepository.saveAndFlush(setting);

        int databaseSizeBeforeUpdate = settingRepository.findAll().size();

        // Update the setting using partial update
        Setting partialUpdatedSetting = new Setting();
        partialUpdatedSetting.setId(setting.getId());

        partialUpdatedSetting.shortName(UPDATED_SHORT_NAME).ord(UPDATED_ORD).active(UPDATED_ACTIVE);

        restSettingMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedSetting.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedSetting))
            )
            .andExpect(status().isOk());

        // Validate the Setting in the database
        List<Setting> settingList = settingRepository.findAll();
        assertThat(settingList).hasSize(databaseSizeBeforeUpdate);
        Setting testSetting = settingList.get(settingList.size() - 1);
        assertThat(testSetting.getShortName()).isEqualTo(UPDATED_SHORT_NAME);
        assertThat(testSetting.getOrd()).isEqualTo(UPDATED_ORD);
        assertThat(testSetting.getActive()).isEqualTo(UPDATED_ACTIVE);
    }

    @Test
    @Transactional
    void patchNonExistingSetting() throws Exception {
        int databaseSizeBeforeUpdate = settingRepository.findAll().size();
        setting.setId(count.incrementAndGet());

        // Create the Setting
        SettingDTO settingDTO = settingMapper.toDto(setting);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restSettingMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, settingDTO.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(settingDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Setting in the database
        List<Setting> settingList = settingRepository.findAll();
        assertThat(settingList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchSetting() throws Exception {
        int databaseSizeBeforeUpdate = settingRepository.findAll().size();
        setting.setId(count.incrementAndGet());

        // Create the Setting
        SettingDTO settingDTO = settingMapper.toDto(setting);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restSettingMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, count.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(settingDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Setting in the database
        List<Setting> settingList = settingRepository.findAll();
        assertThat(settingList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamSetting() throws Exception {
        int databaseSizeBeforeUpdate = settingRepository.findAll().size();
        setting.setId(count.incrementAndGet());

        // Create the Setting
        SettingDTO settingDTO = settingMapper.toDto(setting);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restSettingMockMvc
            .perform(
                patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(TestUtil.convertObjectToJsonBytes(settingDTO))
            )
            .andExpect(status().isMethodNotAllowed());

        // Validate the Setting in the database
        List<Setting> settingList = settingRepository.findAll();
        assertThat(settingList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteSetting() throws Exception {
        // Initialize the database
        settingRepository.saveAndFlush(setting);

        int databaseSizeBeforeDelete = settingRepository.findAll().size();

        // Delete the setting
        restSettingMockMvc
            .perform(delete(ENTITY_API_URL_ID, setting.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        List<Setting> settingList = settingRepository.findAll();
        assertThat(settingList).hasSize(databaseSizeBeforeDelete - 1);
    }
}
