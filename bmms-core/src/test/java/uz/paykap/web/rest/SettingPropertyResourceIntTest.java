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
import uz.paykap.domain.SettingProperty;
import uz.paykap.domain.enumeration.Prop;
import uz.paykap.repository.SettingPropertyRepository;
import uz.paykap.service.SettingPropertyService;
import uz.paykap.service.dto.SettingPropertyDTO;
import uz.paykap.service.mapper.SettingPropertyMapper;
import uz.paykap.web.rest.errors.ExceptionTranslator;

/**
 * Test class for the SettingPropertyResource REST controller.
 *
 * @see SettingPropertyResource
 */
@RunWith(SpringRunner.class)
@IntegrationTest
public class SettingPropertyResourceIntTest {

    private static final Prop DEFAULT_KEY = Prop.UZCARD_TERMINAL;
    private static final Prop UPDATED_KEY = Prop.UZCARD_MERCHANT;

    private static final String DEFAULT_VALUE = "AAAAAAAAAA";
    private static final String UPDATED_VALUE = "BBBBBBBBBB";

    @Autowired
    private SettingPropertyRepository settingPropertyRepository;

    @Autowired
    private SettingPropertyMapper settingPropertyMapper;

    @Autowired
    private SettingPropertyService settingPropertyService;

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

    private MockMvc restSettingPropertyMockMvc;

    private SettingProperty settingProperty;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        final SettingPropertyResource settingPropertyResource = new SettingPropertyResource(
            settingPropertyService,
            settingPropertyRepository
        );
        this.restSettingPropertyMockMvc =
            MockMvcBuilders
                .standaloneSetup(settingPropertyResource)
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
    public static SettingProperty createEntity(EntityManager em) {
        SettingProperty settingProperty = new SettingProperty().key(DEFAULT_KEY).value(DEFAULT_VALUE);
        return settingProperty;
    }

    @Before
    public void initTest() {
        settingProperty = createEntity(em);
    }

    @Test
    @Transactional
    public void createSettingProperty() throws Exception {
        int databaseSizeBeforeCreate = settingPropertyRepository.findAll().size();

        // Create the SettingProperty
        SettingPropertyDTO settingPropertyDTO = settingPropertyMapper.toDto(settingProperty);
        restSettingPropertyMockMvc
            .perform(
                post("/api/setting-properties")
                    .contentType(TestUtil.APPLICATION_JSON_UTF8)
                    .content(TestUtil.convertObjectToJsonBytes(settingPropertyDTO))
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
    public void createSettingPropertyWithExistingId() throws Exception {
        int databaseSizeBeforeCreate = settingPropertyRepository.findAll().size();

        // Create the SettingProperty with an existing ID
        settingProperty.setId(1L);
        SettingPropertyDTO settingPropertyDTO = settingPropertyMapper.toDto(settingProperty);

        // An entity with an existing ID cannot be created, so this API call must fail
        restSettingPropertyMockMvc
            .perform(
                post("/api/setting-properties")
                    .contentType(TestUtil.APPLICATION_JSON_UTF8)
                    .content(TestUtil.convertObjectToJsonBytes(settingPropertyDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the SettingProperty in the database
        List<SettingProperty> settingPropertyList = settingPropertyRepository.findAll();
        assertThat(settingPropertyList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    public void checkKeyIsRequired() throws Exception {
        int databaseSizeBeforeTest = settingPropertyRepository.findAll().size();
        // set the field null
        settingProperty.setKey(null);

        // Create the SettingProperty, which fails.
        SettingPropertyDTO settingPropertyDTO = settingPropertyMapper.toDto(settingProperty);

        restSettingPropertyMockMvc
            .perform(
                post("/api/setting-properties")
                    .contentType(TestUtil.APPLICATION_JSON_UTF8)
                    .content(TestUtil.convertObjectToJsonBytes(settingPropertyDTO))
            )
            .andExpect(status().isBadRequest());

        List<SettingProperty> settingPropertyList = settingPropertyRepository.findAll();
        assertThat(settingPropertyList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkValueIsRequired() throws Exception {
        int databaseSizeBeforeTest = settingPropertyRepository.findAll().size();
        // set the field null
        settingProperty.setValue(null);

        // Create the SettingProperty, which fails.
        SettingPropertyDTO settingPropertyDTO = settingPropertyMapper.toDto(settingProperty);

        restSettingPropertyMockMvc
            .perform(
                post("/api/setting-properties")
                    .contentType(TestUtil.APPLICATION_JSON_UTF8)
                    .content(TestUtil.convertObjectToJsonBytes(settingPropertyDTO))
            )
            .andExpect(status().isBadRequest());

        List<SettingProperty> settingPropertyList = settingPropertyRepository.findAll();
        assertThat(settingPropertyList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void getAllSettingProperties() throws Exception {
        // Initialize the database
        settingPropertyRepository.saveAndFlush(settingProperty);

        // Get all the settingPropertyList
        restSettingPropertyMockMvc
            .perform(get("/api/setting-properties?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(settingProperty.getId().intValue())))
            .andExpect(jsonPath("$.[*].key").value(hasItem(DEFAULT_KEY.toString())))
            .andExpect(jsonPath("$.[*].value").value(hasItem(DEFAULT_VALUE.toString())));
    }

    @Test
    @Transactional
    public void getSettingProperty() throws Exception {
        // Initialize the database
        settingPropertyRepository.saveAndFlush(settingProperty);

        // Get the settingProperty
        restSettingPropertyMockMvc
            .perform(get("/api/setting-properties/{id}", settingProperty.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.id").value(settingProperty.getId().intValue()))
            .andExpect(jsonPath("$.key").value(DEFAULT_KEY.toString()))
            .andExpect(jsonPath("$.value").value(DEFAULT_VALUE.toString()));
    }

    @Test
    @Transactional
    public void getNonExistingSettingProperty() throws Exception {
        // Get the settingProperty
        restSettingPropertyMockMvc.perform(get("/api/setting-properties/{id}", Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updateSettingProperty() throws Exception {
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
                put("/api/setting-properties")
                    .contentType(TestUtil.APPLICATION_JSON_UTF8)
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
    public void updateNonExistingSettingProperty() throws Exception {
        int databaseSizeBeforeUpdate = settingPropertyRepository.findAll().size();

        // Create the SettingProperty
        SettingPropertyDTO settingPropertyDTO = settingPropertyMapper.toDto(settingProperty);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restSettingPropertyMockMvc
            .perform(
                put("/api/setting-properties")
                    .contentType(TestUtil.APPLICATION_JSON_UTF8)
                    .content(TestUtil.convertObjectToJsonBytes(settingPropertyDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the SettingProperty in the database
        List<SettingProperty> settingPropertyList = settingPropertyRepository.findAll();
        assertThat(settingPropertyList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    public void deleteSettingProperty() throws Exception {
        // Initialize the database
        settingPropertyRepository.saveAndFlush(settingProperty);

        int databaseSizeBeforeDelete = settingPropertyRepository.findAll().size();

        // Get the settingProperty
        restSettingPropertyMockMvc
            .perform(delete("/api/setting-properties/{id}", settingProperty.getId()).accept(TestUtil.APPLICATION_JSON_UTF8))
            .andExpect(status().isOk());

        // Validate the database is empty
        List<SettingProperty> settingPropertyList = settingPropertyRepository.findAll();
        assertThat(settingPropertyList).hasSize(databaseSizeBeforeDelete - 1);
    }

    @Test
    @Transactional
    public void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(SettingProperty.class);
        SettingProperty settingProperty1 = new SettingProperty();
        settingProperty1.setId(1L);
        SettingProperty settingProperty2 = new SettingProperty();
        settingProperty2.setId(settingProperty1.getId());
        assertThat(settingProperty1).isEqualTo(settingProperty2);
        settingProperty2.setId(2L);
        assertThat(settingProperty1).isNotEqualTo(settingProperty2);
        settingProperty1.setId(null);
        assertThat(settingProperty1).isNotEqualTo(settingProperty2);
    }

    @Test
    @Transactional
    public void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(SettingPropertyDTO.class);
        SettingPropertyDTO settingPropertyDTO1 = new SettingPropertyDTO();
        settingPropertyDTO1.setId(1L);
        SettingPropertyDTO settingPropertyDTO2 = new SettingPropertyDTO();
        assertThat(settingPropertyDTO1).isNotEqualTo(settingPropertyDTO2);
        settingPropertyDTO2.setId(settingPropertyDTO1.getId());
        assertThat(settingPropertyDTO1).isEqualTo(settingPropertyDTO2);
        settingPropertyDTO2.setId(2L);
        assertThat(settingPropertyDTO1).isNotEqualTo(settingPropertyDTO2);
        settingPropertyDTO1.setId(null);
        assertThat(settingPropertyDTO1).isNotEqualTo(settingPropertyDTO2);
    }

    @Test
    @Transactional
    public void testEntityFromId() {
        assertThat(settingPropertyMapper.fromId(42L).getId()).isEqualTo(42);
        assertThat(settingPropertyMapper.fromId(null)).isNull();
    }
}
