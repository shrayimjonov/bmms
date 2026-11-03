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
import uz.devops.bmcore.enumeration.Lang;
import uz.paykap.PkbillmsApp;
import uz.paykap.IntegrationTest;
import uz.paykap.domain.I18n;
import uz.paykap.repository.I18nRepository;
import uz.paykap.service.I18nQueryService;
import uz.paykap.service.I18nService;
import uz.paykap.service.dto.I18nDTO;
import uz.paykap.service.mapper.I18nMapper;
import uz.paykap.web.rest.errors.ExceptionTranslator;

/**
 * Test class for the I18nResource REST controller.
 *
 * @see I18nResource
 */
@RunWith(SpringRunner.class)
@IntegrationTest
public class I18nResourceIntTest {

    private static final Lang DEFAULT_LANG = Lang.UZ;
    private static final Lang UPDATED_LANG = Lang.RU;

    private static final String DEFAULT_TEXT = "AAAAAAAAAA";
    private static final String UPDATED_TEXT = "BBBBBBBBBB";

    @Autowired
    private I18nRepository i18nRepository;

    @Autowired
    private I18nMapper i18nMapper;

    @Autowired
    private I18nService i18nService;

    @Autowired
    private I18nQueryService i18nQueryService;

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

    private MockMvc restI18nMockMvc;

    private I18n i18n;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        final I18nResource i18nResource = new I18nResource(i18nService, i18nRepository, i18nQueryService);
        this.restI18nMockMvc =
            MockMvcBuilders
                .standaloneSetup(i18nResource)
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
    public static I18n createEntity(EntityManager em) {
        I18n i18n = new I18n().lang(DEFAULT_LANG).text(DEFAULT_TEXT);
        return i18n;
    }

    @Before
    public void initTest() {
        i18n = createEntity(em);
    }

    @Test
    @Transactional
    public void createI18n() throws Exception {
        int databaseSizeBeforeCreate = i18nRepository.findAll().size();

        // Create the I18n
        I18nDTO i18nDTO = i18nMapper.toDto(i18n);
        restI18nMockMvc
            .perform(post("/api/i-18-ns").contentType(TestUtil.APPLICATION_JSON_UTF8).content(TestUtil.convertObjectToJsonBytes(i18nDTO)))
            .andExpect(status().isCreated());

        // Validate the I18n in the database
        List<I18n> i18nList = i18nRepository.findAll();
        assertThat(i18nList).hasSize(databaseSizeBeforeCreate + 1);
        I18n testI18n = i18nList.get(i18nList.size() - 1);
        assertThat(testI18n.getLang()).isEqualTo(DEFAULT_LANG);
        assertThat(testI18n.getText()).isEqualTo(DEFAULT_TEXT);
    }

    @Test
    @Transactional
    public void createI18nWithExistingId() throws Exception {
        int databaseSizeBeforeCreate = i18nRepository.findAll().size();

        // Create the I18n with an existing ID
        i18n.setId(1L);
        I18nDTO i18nDTO = i18nMapper.toDto(i18n);

        // An entity with an existing ID cannot be created, so this API call must fail
        restI18nMockMvc
            .perform(post("/api/i-18-ns").contentType(TestUtil.APPLICATION_JSON_UTF8).content(TestUtil.convertObjectToJsonBytes(i18nDTO)))
            .andExpect(status().isBadRequest());

        // Validate the I18n in the database
        List<I18n> i18nList = i18nRepository.findAll();
        assertThat(i18nList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    public void checkLangIsRequired() throws Exception {
        int databaseSizeBeforeTest = i18nRepository.findAll().size();
        // set the field null
        i18n.setLang(null);

        // Create the I18n, which fails.
        I18nDTO i18nDTO = i18nMapper.toDto(i18n);

        restI18nMockMvc
            .perform(post("/api/i-18-ns").contentType(TestUtil.APPLICATION_JSON_UTF8).content(TestUtil.convertObjectToJsonBytes(i18nDTO)))
            .andExpect(status().isBadRequest());

        List<I18n> i18nList = i18nRepository.findAll();
        assertThat(i18nList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkTextIsRequired() throws Exception {
        int databaseSizeBeforeTest = i18nRepository.findAll().size();
        // set the field null
        i18n.setText(null);

        // Create the I18n, which fails.
        I18nDTO i18nDTO = i18nMapper.toDto(i18n);

        restI18nMockMvc
            .perform(post("/api/i-18-ns").contentType(TestUtil.APPLICATION_JSON_UTF8).content(TestUtil.convertObjectToJsonBytes(i18nDTO)))
            .andExpect(status().isBadRequest());

        List<I18n> i18nList = i18nRepository.findAll();
        assertThat(i18nList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void getAllI18ns() throws Exception {
        // Initialize the database
        i18nRepository.saveAndFlush(i18n);

        // Get all the i18nList
        restI18nMockMvc
            .perform(get("/api/i-18-ns?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(i18n.getId().intValue())))
            .andExpect(jsonPath("$.[*].lang").value(hasItem(DEFAULT_LANG.toString())))
            .andExpect(jsonPath("$.[*].text").value(hasItem(DEFAULT_TEXT.toString())));
    }

    @Test
    @Transactional
    public void getI18n() throws Exception {
        // Initialize the database
        i18nRepository.saveAndFlush(i18n);

        // Get the i18n
        restI18nMockMvc
            .perform(get("/api/i-18-ns/{id}", i18n.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.id").value(i18n.getId().intValue()))
            .andExpect(jsonPath("$.lang").value(DEFAULT_LANG.toString()))
            .andExpect(jsonPath("$.text").value(DEFAULT_TEXT.toString()));
    }

    @Test
    @Transactional
    public void getAllI18nsByLangIsEqualToSomething() throws Exception {
        // Initialize the database
        i18nRepository.saveAndFlush(i18n);

        // Get all the i18nList where lang equals to DEFAULT_LANG
        defaultI18nShouldBeFound("lang.equals=" + DEFAULT_LANG);

        // Get all the i18nList where lang equals to UPDATED_LANG
        defaultI18nShouldNotBeFound("lang.equals=" + UPDATED_LANG);
    }

    @Test
    @Transactional
    public void getAllI18nsByLangIsInShouldWork() throws Exception {
        // Initialize the database
        i18nRepository.saveAndFlush(i18n);

        // Get all the i18nList where lang in DEFAULT_LANG or UPDATED_LANG
        defaultI18nShouldBeFound("lang.in=" + DEFAULT_LANG + "," + UPDATED_LANG);

        // Get all the i18nList where lang equals to UPDATED_LANG
        defaultI18nShouldNotBeFound("lang.in=" + UPDATED_LANG);
    }

    @Test
    @Transactional
    public void getAllI18nsByLangIsNullOrNotNull() throws Exception {
        // Initialize the database
        i18nRepository.saveAndFlush(i18n);

        // Get all the i18nList where lang is not null
        defaultI18nShouldBeFound("lang.specified=true");

        // Get all the i18nList where lang is null
        defaultI18nShouldNotBeFound("lang.specified=false");
    }

    @Test
    @Transactional
    public void getAllI18nsByTextIsEqualToSomething() throws Exception {
        // Initialize the database
        i18nRepository.saveAndFlush(i18n);

        // Get all the i18nList where text equals to DEFAULT_TEXT
        defaultI18nShouldBeFound("text.equals=" + DEFAULT_TEXT);

        // Get all the i18nList where text equals to UPDATED_TEXT
        defaultI18nShouldNotBeFound("text.equals=" + UPDATED_TEXT);
    }

    @Test
    @Transactional
    public void getAllI18nsByTextIsInShouldWork() throws Exception {
        // Initialize the database
        i18nRepository.saveAndFlush(i18n);

        // Get all the i18nList where text in DEFAULT_TEXT or UPDATED_TEXT
        defaultI18nShouldBeFound("text.in=" + DEFAULT_TEXT + "," + UPDATED_TEXT);

        // Get all the i18nList where text equals to UPDATED_TEXT
        defaultI18nShouldNotBeFound("text.in=" + UPDATED_TEXT);
    }

    @Test
    @Transactional
    public void getAllI18nsByTextIsNullOrNotNull() throws Exception {
        // Initialize the database
        i18nRepository.saveAndFlush(i18n);

        // Get all the i18nList where text is not null
        defaultI18nShouldBeFound("text.specified=true");

        // Get all the i18nList where text is null
        defaultI18nShouldNotBeFound("text.specified=false");
    }

    /**
     * Executes the search, and checks that the default entity is returned
     */
    private void defaultI18nShouldBeFound(String filter) throws Exception {
        restI18nMockMvc
            .perform(get("/api/i-18-ns?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(i18n.getId().intValue())))
            .andExpect(jsonPath("$.[*].lang").value(hasItem(DEFAULT_LANG.toString())))
            .andExpect(jsonPath("$.[*].text").value(hasItem(DEFAULT_TEXT.toString())));

        // Check, that the count call also returns 1
        restI18nMockMvc
            .perform(get("/api/i-18-ns/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(content().string("1"));
    }

    /**
     * Executes the search, and checks that the default entity is not returned
     */
    private void defaultI18nShouldNotBeFound(String filter) throws Exception {
        restI18nMockMvc
            .perform(get("/api/i-18-ns?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty());

        // Check, that the count call also returns 0
        restI18nMockMvc
            .perform(get("/api/i-18-ns/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(content().string("0"));
    }

    @Test
    @Transactional
    public void getNonExistingI18n() throws Exception {
        // Get the i18n
        restI18nMockMvc.perform(get("/api/i-18-ns/{id}", Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updateI18n() throws Exception {
        // Initialize the database
        i18nRepository.saveAndFlush(i18n);

        int databaseSizeBeforeUpdate = i18nRepository.findAll().size();

        // Update the i18n
        I18n updatedI18n = i18nRepository.findById(i18n.getId()).get();
        // Disconnect from session so that the updates on updatedI18n are not directly saved in db
        em.detach(updatedI18n);
        updatedI18n.lang(UPDATED_LANG).text(UPDATED_TEXT);
        I18nDTO i18nDTO = i18nMapper.toDto(updatedI18n);

        restI18nMockMvc
            .perform(put("/api/i-18-ns").contentType(TestUtil.APPLICATION_JSON_UTF8).content(TestUtil.convertObjectToJsonBytes(i18nDTO)))
            .andExpect(status().isOk());

        // Validate the I18n in the database
        List<I18n> i18nList = i18nRepository.findAll();
        assertThat(i18nList).hasSize(databaseSizeBeforeUpdate);
        I18n testI18n = i18nList.get(i18nList.size() - 1);
        assertThat(testI18n.getLang()).isEqualTo(UPDATED_LANG);
        assertThat(testI18n.getText()).isEqualTo(UPDATED_TEXT);
    }

    @Test
    @Transactional
    public void updateNonExistingI18n() throws Exception {
        int databaseSizeBeforeUpdate = i18nRepository.findAll().size();

        // Create the I18n
        I18nDTO i18nDTO = i18nMapper.toDto(i18n);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restI18nMockMvc
            .perform(put("/api/i-18-ns").contentType(TestUtil.APPLICATION_JSON_UTF8).content(TestUtil.convertObjectToJsonBytes(i18nDTO)))
            .andExpect(status().isBadRequest());

        // Validate the I18n in the database
        List<I18n> i18nList = i18nRepository.findAll();
        assertThat(i18nList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    public void deleteI18n() throws Exception {
        // Initialize the database
        i18nRepository.saveAndFlush(i18n);

        int databaseSizeBeforeDelete = i18nRepository.findAll().size();

        // Get the i18n
        restI18nMockMvc
            .perform(delete("/api/i-18-ns/{id}", i18n.getId()).accept(TestUtil.APPLICATION_JSON_UTF8))
            .andExpect(status().isOk());

        // Validate the database is empty
        List<I18n> i18nList = i18nRepository.findAll();
        assertThat(i18nList).hasSize(databaseSizeBeforeDelete - 1);
    }

    @Test
    @Transactional
    public void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(I18n.class);
        I18n i18n1 = new I18n();
        i18n1.setId(1L);
        I18n i18n2 = new I18n();
        i18n2.setId(i18n1.getId());
        assertThat(i18n1).isEqualTo(i18n2);
        i18n2.setId(2L);
        assertThat(i18n1).isNotEqualTo(i18n2);
        i18n1.setId(null);
        assertThat(i18n1).isNotEqualTo(i18n2);
    }

    @Test
    @Transactional
    public void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(I18nDTO.class);
        I18nDTO i18nDTO1 = new I18nDTO();
        i18nDTO1.setId(1L);
        I18nDTO i18nDTO2 = new I18nDTO();
        assertThat(i18nDTO1).isNotEqualTo(i18nDTO2);
        i18nDTO2.setId(i18nDTO1.getId());
        assertThat(i18nDTO1).isEqualTo(i18nDTO2);
        i18nDTO2.setId(2L);
        assertThat(i18nDTO1).isNotEqualTo(i18nDTO2);
        i18nDTO1.setId(null);
        assertThat(i18nDTO1).isNotEqualTo(i18nDTO2);
    }
    //    @Test
    //    @Transactional
    //    public void testEntityFromId() {
    //        assertThat(i18nMapper.fromId(42L).getId()).isEqualTo(42);
    //        assertThat(i18nMapper.fromId(null)).isNull();
    //    }
}
