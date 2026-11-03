package uz.paykap.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static uz.paykap.web.rest.TestUtil.createFormattingConversionService;

import java.util.ArrayList;
import java.util.List;
import javax.persistence.EntityManager;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import uz.paykap.IntegrationTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.Validator;
import uz.paykap.domain.Setting;
import uz.paykap.domain.SettingProperty;
import uz.paykap.repository.SettingRepository;
import uz.paykap.service.SettingQueryService;
import uz.paykap.service.SettingService;
import uz.paykap.service.dto.SettingDTO;
import uz.paykap.service.epos.EposResolver;
import uz.paykap.service.mapper.SettingMapper;
import uz.paykap.web.rest.errors.ExceptionTranslator;

/**
 * Test class for the SettingResource REST controller.
 *
 * @see SettingResource
 */
@RunWith(SpringRunner.class)
@IntegrationTest
public class SettingResourceIntTest {

    private static final String DEFAULT_SHORT_NAME = "AAAAAAAAAA";
    private static final String UPDATED_SHORT_NAME = "BBBBBBBBBB";

    private static final Integer DEFAULT_ORD = 1;
    private static final Integer UPDATED_ORD = 2;

    private static final Boolean DEFAULT_ACTIVE = false;
    private static final Boolean UPDATED_ACTIVE = true;

    @Autowired
    private SettingRepository settingRepository;

    @Mock
    private SettingRepository settingRepositoryMock;

    @Autowired
    private SettingMapper settingMapper;

    @Mock
    private SettingService settingServiceMock;

    @Autowired
    private SettingService settingService;

    @Autowired
    private SettingQueryService settingQueryService;

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

    @Autowired
    private EposResolver eposResolver;

    private MockMvc restSettingMockMvc;

    private Setting setting;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        final SettingResource settingResource = new SettingResource(settingService, settingRepository, settingQueryService, eposResolver);
        this.restSettingMockMvc =
            MockMvcBuilders
                .standaloneSetup(settingResource)
                .setCustomArgumentResolvers(pageableArgumentResolver)
                .setControllerAdvice(exceptionTranslator)
                .setConversionService(createFormattingConversionService())
                .setMessageConverters(jacksonMessageConverter)
                .setValidator(validator)
                .build();
    }

    /**
     * Create an entity for this test.
     * <p>
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Setting createEntity(EntityManager em) {
        Setting setting = new Setting().shortName(DEFAULT_SHORT_NAME).ord(DEFAULT_ORD).active(DEFAULT_ACTIVE);
        // Add required entity
        SettingProperty settingProperty = SettingPropertyResourceIntTest.createEntity(em);
        //        em.persist(settingProperty);
        //        em.flush();
        setting.getProps().put(settingProperty.getKey(), settingProperty);
        return setting;
    }

    @Before
    public void initTest() {
        setting = createEntity(em);
    }

    @Test
    @Transactional
    public void createSetting() throws Exception {
        int databaseSizeBeforeCreate = settingRepository.findAll().size();

        // Create the Setting
        SettingDTO settingDTO = settingMapper.toDto(setting);
        restSettingMockMvc
            .perform(
                post("/api/settings").contentType(TestUtil.APPLICATION_JSON_UTF8).content(TestUtil.convertObjectToJsonBytes(settingDTO))
            )
            .andExpect(status().isCreated());

        // Validate the Setting in the database
        List<Setting> settingList = settingRepository.findAll();
        assertThat(settingList).hasSize(databaseSizeBeforeCreate + 1);
        Setting testSetting = settingList.get(settingList.size() - 1);
        assertThat(testSetting.getShortName()).isEqualTo(DEFAULT_SHORT_NAME);
        assertThat(testSetting.getOrd()).isEqualTo(DEFAULT_ORD);
        assertThat(testSetting.isActive()).isEqualTo(DEFAULT_ACTIVE);
    }

    @Test
    @Transactional
    public void createSettingWithExistingId() throws Exception {
        int databaseSizeBeforeCreate = settingRepository.findAll().size();

        // Create the Setting with an existing ID
        setting.setId(1L);
        SettingDTO settingDTO = settingMapper.toDto(setting);

        // An entity with an existing ID cannot be created, so this API call must fail
        restSettingMockMvc
            .perform(
                post("/api/settings").contentType(TestUtil.APPLICATION_JSON_UTF8).content(TestUtil.convertObjectToJsonBytes(settingDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Setting in the database
        List<Setting> settingList = settingRepository.findAll();
        assertThat(settingList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    public void checkShortNameIsRequired() throws Exception {
        int databaseSizeBeforeTest = settingRepository.findAll().size();
        // set the field null
        setting.setShortName(null);

        // Create the Setting, which fails.
        SettingDTO settingDTO = settingMapper.toDto(setting);

        restSettingMockMvc
            .perform(
                post("/api/settings").contentType(TestUtil.APPLICATION_JSON_UTF8).content(TestUtil.convertObjectToJsonBytes(settingDTO))
            )
            .andExpect(status().isBadRequest());

        List<Setting> settingList = settingRepository.findAll();
        assertThat(settingList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkActiveIsRequired() throws Exception {
        int databaseSizeBeforeTest = settingRepository.findAll().size();
        // set the field null
        setting.setActive(null);

        // Create the Setting, which fails.
        SettingDTO settingDTO = settingMapper.toDto(setting);

        restSettingMockMvc
            .perform(
                post("/api/settings").contentType(TestUtil.APPLICATION_JSON_UTF8).content(TestUtil.convertObjectToJsonBytes(settingDTO))
            )
            .andExpect(status().isBadRequest());

        List<Setting> settingList = settingRepository.findAll();
        assertThat(settingList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void getAllSettings() throws Exception {
        // Initialize the database
        settingRepository.saveAndFlush(setting);

        // Get all the settingList
        restSettingMockMvc
            .perform(get("/api/settings?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(setting.getId().intValue())))
            .andExpect(jsonPath("$.[*].shortName").value(hasItem(DEFAULT_SHORT_NAME.toString())))
            .andExpect(jsonPath("$.[*].ord").value(hasItem(DEFAULT_ORD)))
            .andExpect(jsonPath("$.[*].active").value(hasItem(DEFAULT_ACTIVE.booleanValue())));
    }

    @SuppressWarnings({ "unchecked" })
    public void getAllSettingsWithEagerRelationshipsIsEnabled() throws Exception {
        SettingResource settingResource = new SettingResource(settingServiceMock, settingRepository, settingQueryService, eposResolver);
        when(settingServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        MockMvc restSettingMockMvc = MockMvcBuilders
            .standaloneSetup(settingResource)
            .setCustomArgumentResolvers(pageableArgumentResolver)
            .setControllerAdvice(exceptionTranslator)
            .setConversionService(createFormattingConversionService())
            .setMessageConverters(jacksonMessageConverter)
            .build();

        restSettingMockMvc.perform(get("/api/settings?eagerload=true")).andExpect(status().isOk());

        verify(settingServiceMock, times(1)).findAllWithEagerRelationships(any());
    }

    @SuppressWarnings({ "unchecked" })
    public void getAllSettingsWithEagerRelationshipsIsNotEnabled() throws Exception {
        SettingResource settingResource = new SettingResource(settingServiceMock, settingRepository, settingQueryService, eposResolver);
        when(settingServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));
        MockMvc restSettingMockMvc = MockMvcBuilders
            .standaloneSetup(settingResource)
            .setCustomArgumentResolvers(pageableArgumentResolver)
            .setControllerAdvice(exceptionTranslator)
            .setConversionService(createFormattingConversionService())
            .setMessageConverters(jacksonMessageConverter)
            .build();

        restSettingMockMvc.perform(get("/api/settings?eagerload=true")).andExpect(status().isOk());

        verify(settingServiceMock, times(1)).findAllWithEagerRelationships(any());
    }

    @Test
    @Transactional
    public void getSetting() throws Exception {
        // Initialize the database
        settingRepository.saveAndFlush(setting);

        // Get the setting
        restSettingMockMvc
            .perform(get("/api/settings/{id}", setting.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.id").value(setting.getId().intValue()))
            .andExpect(jsonPath("$.shortName").value(DEFAULT_SHORT_NAME.toString()))
            .andExpect(jsonPath("$.ord").value(DEFAULT_ORD))
            .andExpect(jsonPath("$.active").value(DEFAULT_ACTIVE.booleanValue()));
    }

    @Test
    @Transactional
    public void getAllSettingsByShortNameIsEqualToSomething() throws Exception {
        // Initialize the database
        settingRepository.saveAndFlush(setting);

        // Get all the settingList where shortName equals to DEFAULT_SHORT_NAME
        defaultSettingShouldBeFound("shortName.equals=" + DEFAULT_SHORT_NAME);

        // Get all the settingList where shortName equals to UPDATED_SHORT_NAME
        defaultSettingShouldNotBeFound("shortName.equals=" + UPDATED_SHORT_NAME);
    }

    @Test
    @Transactional
    public void getAllSettingsByShortNameIsInShouldWork() throws Exception {
        // Initialize the database
        settingRepository.saveAndFlush(setting);

        // Get all the settingList where shortName in DEFAULT_SHORT_NAME or UPDATED_SHORT_NAME
        defaultSettingShouldBeFound("shortName.in=" + DEFAULT_SHORT_NAME + "," + UPDATED_SHORT_NAME);

        // Get all the settingList where shortName equals to UPDATED_SHORT_NAME
        defaultSettingShouldNotBeFound("shortName.in=" + UPDATED_SHORT_NAME);
    }

    @Test
    @Transactional
    public void getAllSettingsByShortNameIsNullOrNotNull() throws Exception {
        // Initialize the database
        settingRepository.saveAndFlush(setting);

        // Get all the settingList where shortName is not null
        defaultSettingShouldBeFound("shortName.specified=true");

        // Get all the settingList where shortName is null
        defaultSettingShouldNotBeFound("shortName.specified=false");
    }

    @Test
    @Transactional
    public void getAllSettingsByOrdIsEqualToSomething() throws Exception {
        // Initialize the database
        settingRepository.saveAndFlush(setting);

        // Get all the settingList where ord equals to DEFAULT_ORD
        defaultSettingShouldBeFound("ord.equals=" + DEFAULT_ORD);

        // Get all the settingList where ord equals to UPDATED_ORD
        defaultSettingShouldNotBeFound("ord.equals=" + UPDATED_ORD);
    }

    @Test
    @Transactional
    public void getAllSettingsByOrdIsInShouldWork() throws Exception {
        // Initialize the database
        settingRepository.saveAndFlush(setting);

        // Get all the settingList where ord in DEFAULT_ORD or UPDATED_ORD
        defaultSettingShouldBeFound("ord.in=" + DEFAULT_ORD + "," + UPDATED_ORD);

        // Get all the settingList where ord equals to UPDATED_ORD
        defaultSettingShouldNotBeFound("ord.in=" + UPDATED_ORD);
    }

    @Test
    @Transactional
    public void getAllSettingsByOrdIsNullOrNotNull() throws Exception {
        // Initialize the database
        settingRepository.saveAndFlush(setting);

        // Get all the settingList where ord is not null
        defaultSettingShouldBeFound("ord.specified=true");

        // Get all the settingList where ord is null
        defaultSettingShouldNotBeFound("ord.specified=false");
    }

    @Test
    @Transactional
    public void getAllSettingsByOrdIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        settingRepository.saveAndFlush(setting);

        // Get all the settingList where ord greater than or equals to DEFAULT_ORD
        defaultSettingShouldBeFound("ord.greaterOrEqualThan=" + DEFAULT_ORD);

        // Get all the settingList where ord greater than or equals to UPDATED_ORD
        defaultSettingShouldNotBeFound("ord.greaterOrEqualThan=" + UPDATED_ORD);
    }

    @Test
    @Transactional
    public void getAllSettingsByOrdIsLessThanSomething() throws Exception {
        // Initialize the database
        settingRepository.saveAndFlush(setting);

        // Get all the settingList where ord less than or equals to DEFAULT_ORD
        defaultSettingShouldNotBeFound("ord.lessThan=" + DEFAULT_ORD);

        // Get all the settingList where ord less than or equals to UPDATED_ORD
        defaultSettingShouldBeFound("ord.lessThan=" + UPDATED_ORD);
    }

    @Test
    @Transactional
    public void getAllSettingsByActiveIsEqualToSomething() throws Exception {
        // Initialize the database
        settingRepository.saveAndFlush(setting);

        // Get all the settingList where active equals to DEFAULT_ACTIVE
        defaultSettingShouldBeFound("active.equals=" + DEFAULT_ACTIVE);

        // Get all the settingList where active equals to UPDATED_ACTIVE
        defaultSettingShouldNotBeFound("active.equals=" + UPDATED_ACTIVE);
    }

    @Test
    @Transactional
    public void getAllSettingsByActiveIsInShouldWork() throws Exception {
        // Initialize the database
        settingRepository.saveAndFlush(setting);

        // Get all the settingList where active in DEFAULT_ACTIVE or UPDATED_ACTIVE
        defaultSettingShouldBeFound("active.in=" + DEFAULT_ACTIVE + "," + UPDATED_ACTIVE);

        // Get all the settingList where active equals to UPDATED_ACTIVE
        defaultSettingShouldNotBeFound("active.in=" + UPDATED_ACTIVE);
    }

    @Test
    @Transactional
    public void getAllSettingsByActiveIsNullOrNotNull() throws Exception {
        // Initialize the database
        settingRepository.saveAndFlush(setting);

        // Get all the settingList where active is not null
        defaultSettingShouldBeFound("active.specified=true");

        // Get all the settingList where active is null
        defaultSettingShouldNotBeFound("active.specified=false");
    }

    //    @Test
    @Transactional
    public void getAllSettingsByPropIsEqualToSomething() throws Exception {
        // Initialize the database
        SettingProperty prop = SettingPropertyResourceIntTest.createEntity(em);
        em.persist(prop);
        em.flush();
        setting.addProp(prop);
        settingRepository.saveAndFlush(setting);
        Long propId = prop.getId();

        // Get all the settingList where prop equals to propId
        defaultSettingShouldBeFound("propId.equals=" + propId);

        // Get all the settingList where prop equals to propId + 1
        defaultSettingShouldNotBeFound("propId.equals=" + (propId + 1));
    }

    /**
     * Executes the search, and checks that the default entity is returned
     */
    private void defaultSettingShouldBeFound(String filter) throws Exception {
        restSettingMockMvc
            .perform(get("/api/settings?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(setting.getId().intValue())))
            .andExpect(jsonPath("$.[*].shortName").value(hasItem(DEFAULT_SHORT_NAME.toString())))
            .andExpect(jsonPath("$.[*].ord").value(hasItem(DEFAULT_ORD)))
            .andExpect(jsonPath("$.[*].active").value(hasItem(DEFAULT_ACTIVE.booleanValue())));

        // Check, that the count call also returns 1
        restSettingMockMvc
            .perform(get("/api/settings/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(content().string("1"));
    }

    /**
     * Executes the search, and checks that the default entity is not returned
     */
    private void defaultSettingShouldNotBeFound(String filter) throws Exception {
        restSettingMockMvc
            .perform(get("/api/settings?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty());

        // Check, that the count call also returns 0
        restSettingMockMvc
            .perform(get("/api/settings/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(content().string("0"));
    }

    @Test
    @Transactional
    public void getNonExistingSetting() throws Exception {
        // Get the setting
        restSettingMockMvc.perform(get("/api/settings/{id}", Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updateSetting() throws Exception {
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
                put("/api/settings").contentType(TestUtil.APPLICATION_JSON_UTF8).content(TestUtil.convertObjectToJsonBytes(settingDTO))
            )
            .andExpect(status().isOk());

        // Validate the Setting in the database
        List<Setting> settingList = settingRepository.findAll();
        assertThat(settingList).hasSize(databaseSizeBeforeUpdate);
        Setting testSetting = settingList.get(settingList.size() - 1);
        assertThat(testSetting.getShortName()).isEqualTo(UPDATED_SHORT_NAME);
        assertThat(testSetting.getOrd()).isEqualTo(UPDATED_ORD);
        assertThat(testSetting.isActive()).isEqualTo(UPDATED_ACTIVE);
    }

    @Test
    @Transactional
    public void updateNonExistingSetting() throws Exception {
        int databaseSizeBeforeUpdate = settingRepository.findAll().size();

        // Create the Setting
        SettingDTO settingDTO = settingMapper.toDto(setting);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restSettingMockMvc
            .perform(
                put("/api/settings").contentType(TestUtil.APPLICATION_JSON_UTF8).content(TestUtil.convertObjectToJsonBytes(settingDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Setting in the database
        List<Setting> settingList = settingRepository.findAll();
        assertThat(settingList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    public void deleteSetting() throws Exception {
        // Initialize the database
        settingRepository.saveAndFlush(setting);

        int databaseSizeBeforeDelete = settingRepository.findAll().size();

        // Get the setting
        restSettingMockMvc
            .perform(delete("/api/settings/{id}", setting.getId()).accept(TestUtil.APPLICATION_JSON_UTF8))
            .andExpect(status().isOk());

        // Validate the database is empty
        List<Setting> settingList = settingRepository.findAll();
        assertThat(settingList).hasSize(databaseSizeBeforeDelete - 1);
    }

    @Test
    @Transactional
    public void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Setting.class);
        Setting setting1 = new Setting();
        setting1.setId(1L);
        Setting setting2 = new Setting();
        setting2.setId(setting1.getId());
        assertThat(setting1).isEqualTo(setting2);
        setting2.setId(2L);
        assertThat(setting1).isNotEqualTo(setting2);
        setting1.setId(null);
        assertThat(setting1).isNotEqualTo(setting2);
    }

    @Test
    @Transactional
    public void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(SettingDTO.class);
        SettingDTO settingDTO1 = new SettingDTO();
        settingDTO1.setId(1L);
        SettingDTO settingDTO2 = new SettingDTO();
        assertThat(settingDTO1).isNotEqualTo(settingDTO2);
        settingDTO2.setId(settingDTO1.getId());
        assertThat(settingDTO1).isEqualTo(settingDTO2);
        settingDTO2.setId(2L);
        assertThat(settingDTO1).isNotEqualTo(settingDTO2);
        settingDTO1.setId(null);
        assertThat(settingDTO1).isNotEqualTo(settingDTO2);
    }

    @Test
    @Transactional
    public void testEntityFromId() {
        assertThat(settingMapper.fromId(42L).getId()).isEqualTo(42);
        assertThat(settingMapper.fromId(null)).isNull();
    }
}
