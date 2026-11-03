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
import uz.paykap.domain.Prefix;
import uz.paykap.domain.enumeration.PrefixType;
import uz.paykap.repository.PrefixRepository;
import uz.paykap.service.PrefixQueryService;
import uz.paykap.service.PrefixService;
import uz.paykap.service.dto.PrefixDTO;
import uz.paykap.service.mapper.PrefixMapper;
import uz.paykap.web.rest.errors.ExceptionTranslator;

/**
 * Test class for the PrefixResource REST controller.
 *
 * @see PrefixResource
 */
@RunWith(SpringRunner.class)
@IntegrationTest
public class PrefixResourceIntTest {

    private static final PrefixType DEFAULT_TYPE = PrefixType.STATIC;
    private static final PrefixType UPDATED_TYPE = PrefixType.SELECT;

    private static final String DEFAULT_VALUE = "AAAAAAAAAA";
    private static final String UPDATED_VALUE = "BBBBBBBBBB";

    private static final Boolean DEFAULT_OUTPUT = false;
    private static final Boolean UPDATED_OUTPUT = true;

    private static final String DEFAULT_REQUIRE_FIELD = "AAAAAAAAAA";
    private static final String UPDATED_REQUIRE_FIELD = "BBBBBBBBBB";

    @Autowired
    private PrefixRepository prefixRepository;

    @Autowired
    private PrefixMapper prefixMapper;

    @Autowired
    private PrefixService prefixService;

    @Autowired
    private MappingJackson2HttpMessageConverter jacksonMessageConverter;

    @Autowired
    private PageableHandlerMethodArgumentResolver pageableArgumentResolver;

    @Autowired
    private ExceptionTranslator exceptionTranslator;

    @Autowired
    private PrefixQueryService prefixQueryService;

    @Autowired
    private EntityManager em;

    @Autowired
    private Validator validator;

    private MockMvc restPrefixMockMvc;

    private Prefix prefix;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        final PrefixResource prefixResource = new PrefixResource(prefixService, prefixRepository, prefixQueryService);
        this.restPrefixMockMvc =
            MockMvcBuilders
                .standaloneSetup(prefixResource)
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
    public static Prefix createEntity(EntityManager em) {
        Prefix prefix = new Prefix().type(DEFAULT_TYPE).value(DEFAULT_VALUE).output(DEFAULT_OUTPUT).requireField(DEFAULT_REQUIRE_FIELD);
        return prefix;
    }

    @Before
    public void initTest() {
        prefix = createEntity(em);
    }

    @Test
    @Transactional
    public void createPrefix() throws Exception {
        int databaseSizeBeforeCreate = prefixRepository.findAll().size();

        // Create the Prefix
        PrefixDTO prefixDTO = prefixMapper.toDto(prefix);
        restPrefixMockMvc
            .perform(
                post("/api/prefixes").contentType(TestUtil.APPLICATION_JSON_UTF8).content(TestUtil.convertObjectToJsonBytes(prefixDTO))
            )
            .andExpect(status().isCreated());

        // Validate the Prefix in the database
        List<Prefix> prefixList = prefixRepository.findAll();
        assertThat(prefixList).hasSize(databaseSizeBeforeCreate + 1);
        Prefix testPrefix = prefixList.get(prefixList.size() - 1);
        assertThat(testPrefix.getType()).isEqualTo(DEFAULT_TYPE);
        assertThat(testPrefix.getValue()).isEqualTo(DEFAULT_VALUE);
        assertThat(testPrefix.isOutput()).isEqualTo(DEFAULT_OUTPUT);
        assertThat(testPrefix.getRequireField()).isEqualTo(DEFAULT_REQUIRE_FIELD);
    }

    @Test
    @Transactional
    public void createPrefixWithExistingId() throws Exception {
        int databaseSizeBeforeCreate = prefixRepository.findAll().size();

        // Create the Prefix with an existing ID
        prefix.setId(1L);
        PrefixDTO prefixDTO = prefixMapper.toDto(prefix);

        // An entity with an existing ID cannot be created, so this API call must fail
        restPrefixMockMvc
            .perform(
                post("/api/prefixes").contentType(TestUtil.APPLICATION_JSON_UTF8).content(TestUtil.convertObjectToJsonBytes(prefixDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Prefix in the database
        List<Prefix> prefixList = prefixRepository.findAll();
        assertThat(prefixList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    public void getAllPrefixes() throws Exception {
        // Initialize the database
        prefixRepository.saveAndFlush(prefix);

        // Get all the prefixList
        restPrefixMockMvc
            .perform(get("/api/prefixes?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(prefix.getId().intValue())))
            .andExpect(jsonPath("$.[*].type").value(hasItem(DEFAULT_TYPE.toString())))
            .andExpect(jsonPath("$.[*].value").value(hasItem(DEFAULT_VALUE.toString())))
            .andExpect(jsonPath("$.[*].output").value(hasItem(DEFAULT_OUTPUT.booleanValue())))
            .andExpect(jsonPath("$.[*].requireField").value(hasItem(DEFAULT_REQUIRE_FIELD.toString())));
    }

    @Test
    @Transactional
    public void getPrefix() throws Exception {
        // Initialize the database
        prefixRepository.saveAndFlush(prefix);

        // Get the prefix
        restPrefixMockMvc
            .perform(get("/api/prefixes/{id}", prefix.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.id").value(prefix.getId().intValue()))
            .andExpect(jsonPath("$.type").value(DEFAULT_TYPE.toString()))
            .andExpect(jsonPath("$.value").value(DEFAULT_VALUE.toString()))
            .andExpect(jsonPath("$.output").value(DEFAULT_OUTPUT.booleanValue()))
            .andExpect(jsonPath("$.requireField").value(DEFAULT_REQUIRE_FIELD.toString()));
    }

    @Test
    @Transactional
    public void getNonExistingPrefix() throws Exception {
        // Get the prefix
        restPrefixMockMvc.perform(get("/api/prefixes/{id}", Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updatePrefix() throws Exception {
        // Initialize the database
        prefixRepository.saveAndFlush(prefix);

        int databaseSizeBeforeUpdate = prefixRepository.findAll().size();

        // Update the prefix
        Prefix updatedPrefix = prefixRepository.findById(prefix.getId()).get();
        // Disconnect from session so that the updates on updatedPrefix are not directly saved in db
        em.detach(updatedPrefix);
        updatedPrefix.type(UPDATED_TYPE).value(UPDATED_VALUE).output(UPDATED_OUTPUT).requireField(UPDATED_REQUIRE_FIELD);
        PrefixDTO prefixDTO = prefixMapper.toDto(updatedPrefix);

        restPrefixMockMvc
            .perform(put("/api/prefixes").contentType(TestUtil.APPLICATION_JSON_UTF8).content(TestUtil.convertObjectToJsonBytes(prefixDTO)))
            .andExpect(status().isOk());

        // Validate the Prefix in the database
        List<Prefix> prefixList = prefixRepository.findAll();
        assertThat(prefixList).hasSize(databaseSizeBeforeUpdate);
        Prefix testPrefix = prefixList.get(prefixList.size() - 1);
        assertThat(testPrefix.getType()).isEqualTo(UPDATED_TYPE);
        assertThat(testPrefix.getValue()).isEqualTo(UPDATED_VALUE);
        assertThat(testPrefix.isOutput()).isEqualTo(UPDATED_OUTPUT);
        assertThat(testPrefix.getRequireField()).isEqualTo(UPDATED_REQUIRE_FIELD);
    }

    @Test
    @Transactional
    public void updateNonExistingPrefix() throws Exception {
        int databaseSizeBeforeUpdate = prefixRepository.findAll().size();

        // Create the Prefix
        PrefixDTO prefixDTO = prefixMapper.toDto(prefix);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restPrefixMockMvc
            .perform(put("/api/prefixes").contentType(TestUtil.APPLICATION_JSON_UTF8).content(TestUtil.convertObjectToJsonBytes(prefixDTO)))
            .andExpect(status().isBadRequest());

        // Validate the Prefix in the database
        List<Prefix> prefixList = prefixRepository.findAll();
        assertThat(prefixList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    public void deletePrefix() throws Exception {
        // Initialize the database
        prefixRepository.saveAndFlush(prefix);

        int databaseSizeBeforeDelete = prefixRepository.findAll().size();

        // Get the prefix
        restPrefixMockMvc
            .perform(delete("/api/prefixes/{id}", prefix.getId()).accept(TestUtil.APPLICATION_JSON_UTF8))
            .andExpect(status().isOk());

        // Validate the database is empty
        List<Prefix> prefixList = prefixRepository.findAll();
        assertThat(prefixList).hasSize(databaseSizeBeforeDelete - 1);
    }

    @Test
    @Transactional
    public void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Prefix.class);
        Prefix prefix1 = new Prefix();
        prefix1.setId(1L);
        Prefix prefix2 = new Prefix();
        prefix2.setId(prefix1.getId());
        assertThat(prefix1).isEqualTo(prefix2);
        prefix2.setId(2L);
        assertThat(prefix1).isNotEqualTo(prefix2);
        prefix1.setId(null);
        assertThat(prefix1).isNotEqualTo(prefix2);
    }

    @Test
    @Transactional
    public void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(PrefixDTO.class);
        PrefixDTO prefixDTO1 = new PrefixDTO();
        prefixDTO1.setId(1L);
        PrefixDTO prefixDTO2 = new PrefixDTO();
        assertThat(prefixDTO1).isNotEqualTo(prefixDTO2);
        prefixDTO2.setId(prefixDTO1.getId());
        assertThat(prefixDTO1).isEqualTo(prefixDTO2);
        prefixDTO2.setId(2L);
        assertThat(prefixDTO1).isNotEqualTo(prefixDTO2);
        prefixDTO1.setId(null);
        assertThat(prefixDTO1).isNotEqualTo(prefixDTO2);
    }
    //    @Test
    //    @Transactional
    //    public void testEntityFromId() {
    //        assertThat(prefixMapper.fromId(42L).getId()).isEqualTo(42);
    //        assertThat(prefixMapper.fromId(null)).isNull();
    //    }
}
