package uz.paykap.web.rest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Base64Utils;
import uz.paykap.IntegrationTest;
import uz.paykap.domain.Category;
import uz.paykap.domain.I18n;
import uz.paykap.repository.CategoryRepository;
import uz.paykap.service.CategoryService;
import uz.paykap.service.dto.CategoryDTO;
import uz.paykap.service.mapper.CategoryMapper;

import javax.persistence.EntityManager;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for the {@link CategoryResource} REST controller.
 */
@IntegrationTest
@ExtendWith(MockitoExtension.class)
@AutoConfigureMockMvc
@WithMockUser
class CategoryResourceIT {

    private static final String DEFAULT_SHORT_NAME = "AAAAAAAAAA";
    private static final String UPDATED_SHORT_NAME = "BBBBBBBBBB";

    private static final String DEFAULT_THUMBNAIL = "AAAAAAAAAA";
    private static final String UPDATED_THUMBNAIL = "BBBBBBBBBB";

    private static final byte[] DEFAULT_THUMBNAIL_FILE = TestUtil.createByteArray(1, "0");
    private static final byte[] UPDATED_THUMBNAIL_FILE = TestUtil.createByteArray(1, "1");
    private static final String DEFAULT_THUMBNAIL_FILE_CONTENT_TYPE = "image/jpg";
    private static final String UPDATED_THUMBNAIL_FILE_CONTENT_TYPE = "image/png";

    private static final String DEFAULT_LOGO = "AAAAAAAAAA";
    private static final String UPDATED_LOGO = "BBBBBBBBBB";

    private static final byte[] DEFAULT_LOGO_FILE = TestUtil.createByteArray(1, "0");
    private static final byte[] UPDATED_LOGO_FILE = TestUtil.createByteArray(1, "1");
    private static final String DEFAULT_LOGO_FILE_CONTENT_TYPE = "image/jpg";
    private static final String UPDATED_LOGO_FILE_CONTENT_TYPE = "image/png";

    private static final Integer DEFAULT_ORD = 1;
    private static final Integer UPDATED_ORD = 2;
    private static final Integer SMALLER_ORD = 1 - 1;

    private static final Boolean DEFAULT_ACTIVE = false;
    private static final Boolean UPDATED_ACTIVE = true;

    private static final String ENTITY_API_URL = "/api/categories";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong count = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private CategoryRepository categoryRepository;

    @Mock
    private CategoryRepository categoryRepositoryMock;

    @Autowired
    private CategoryMapper categoryMapper;

    @Mock
    private CategoryService categoryServiceMock;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restCategoryMockMvc;

    private Category category;

    public static Category createEntity(EntityManager em) {
        return createEntity(em, DEFAULT_SHORT_NAME);
    }

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Category createEntity(EntityManager em, String name) {
        Category category = new Category()
            .shortName(name)
            .thumbnail(DEFAULT_THUMBNAIL)
            .thumbnailFile(DEFAULT_THUMBNAIL_FILE)
            .thumbnailFileContentType(DEFAULT_THUMBNAIL_FILE_CONTENT_TYPE)
            .logo(DEFAULT_LOGO)
            .logoFile(DEFAULT_LOGO_FILE)
            .logoFileContentType(DEFAULT_LOGO_FILE_CONTENT_TYPE)
            .ord(DEFAULT_ORD)
            .active(DEFAULT_ACTIVE);
        // Add required entity
        I18n i18n;
        if (TestUtil.findAll(em, I18n.class).isEmpty()) {
            i18n = I18nResourceIT.createEntity(em);
            //            em.persist(i18n);
            //            em.flush();
        } else {
            i18n = TestUtil.findAll(em, I18n.class).get(0);
        }
        category.getNames().add(i18n);
        return category;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Category createUpdatedEntity(EntityManager em) {
        Category category = new Category()
            .shortName(UPDATED_SHORT_NAME)
            .thumbnail(UPDATED_THUMBNAIL)
            .thumbnailFile(UPDATED_THUMBNAIL_FILE)
            .thumbnailFileContentType(UPDATED_THUMBNAIL_FILE_CONTENT_TYPE)
            .logo(UPDATED_LOGO)
            .logoFile(UPDATED_LOGO_FILE)
            .logoFileContentType(UPDATED_LOGO_FILE_CONTENT_TYPE)
            .ord(UPDATED_ORD)
            .active(UPDATED_ACTIVE);
        // Add required entity
        I18n i18n;
        if (TestUtil.findAll(em, I18n.class).isEmpty()) {
            i18n = I18nResourceIT.createUpdatedEntity(em);
            em.persist(i18n);
            em.flush();
        } else {
            i18n = TestUtil.findAll(em, I18n.class).get(0);
        }
        category.getNames().add(i18n);
        return category;
    }

    @BeforeEach
    public void initTest() {
        category = createEntity(em);
    }

    @Test
    @Transactional
    void createCategory() throws Exception {
        int databaseSizeBeforeCreate = categoryRepository.findAll().size();
        // Create the Category
        CategoryDTO categoryDTO = categoryMapper.toDto(category);
        restCategoryMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(categoryDTO)))
            .andExpect(status().isCreated());

        // Validate the Category in the database
        List<Category> categoryList = categoryRepository.findAll();
        assertThat(categoryList).hasSize(databaseSizeBeforeCreate + 1);
        Category testCategory = categoryList.get(categoryList.size() - 1);
        assertThat(testCategory.getShortName()).isEqualTo(DEFAULT_SHORT_NAME);
        assertThat(testCategory.getThumbnail()).isEqualTo(DEFAULT_THUMBNAIL);
        assertThat(testCategory.getThumbnailFile()).isEqualTo(DEFAULT_THUMBNAIL_FILE);
        assertThat(testCategory.getThumbnailFileContentType()).isEqualTo(DEFAULT_THUMBNAIL_FILE_CONTENT_TYPE);
        assertThat(testCategory.getLogo()).isEqualTo(DEFAULT_LOGO);
        assertThat(testCategory.getLogoFile()).isEqualTo(null);
        assertThat(testCategory.getLogoFileContentType()).isEqualTo(DEFAULT_LOGO_FILE_CONTENT_TYPE);
        assertThat(testCategory.getOrd()).isEqualTo(DEFAULT_ORD);
        assertThat(testCategory.getActive()).isEqualTo(DEFAULT_ACTIVE);
    }

    @Test
    @Transactional
    void createCategoryWithExistingId() throws Exception {
        // Create the Category with an existing ID
        category.setId(1L);
        CategoryDTO categoryDTO = categoryMapper.toDto(category);

        int databaseSizeBeforeCreate = categoryRepository.findAll().size();

        // An entity with an existing ID cannot be created, so this API call must fail
        restCategoryMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(categoryDTO)))
            .andExpect(status().isBadRequest());

        // Validate the Category in the database
        List<Category> categoryList = categoryRepository.findAll();
        assertThat(categoryList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkShortNameIsRequired() throws Exception {
        int databaseSizeBeforeTest = categoryRepository.findAll().size();
        // set the field null
        category.setShortName(null);

        // Create the Category, which fails.
        CategoryDTO categoryDTO = categoryMapper.toDto(category);

        restCategoryMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(categoryDTO)))
            .andExpect(status().isBadRequest());

        List<Category> categoryList = categoryRepository.findAll();
        assertThat(categoryList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkActiveIsRequired() throws Exception {
        int databaseSizeBeforeTest = categoryRepository.findAll().size();
        // set the field null
        category.setActive(null);

        // Create the Category, which fails.
        CategoryDTO categoryDTO = categoryMapper.toDto(category);

        restCategoryMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(categoryDTO)))
            .andExpect(status().isBadRequest());

        List<Category> categoryList = categoryRepository.findAll();
        assertThat(categoryList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllCategories() throws Exception {
        // Initialize the database
        categoryRepository.saveAndFlush(category);

        // Get all the categoryList
        MvcResult mvcResult = restCategoryMockMvc
                .perform(get(ENTITY_API_URL + "?sort=id,desc"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.[*].id").value(hasItem(category.getId().intValue())))
                .andExpect(jsonPath("$.[*].shortName").value(hasItem(DEFAULT_SHORT_NAME)))
                .andExpect(jsonPath("$.[*].thumbnail").value(hasItem(DEFAULT_THUMBNAIL)))
                .andExpect(jsonPath("$.[*].thumbnailFileContentType").value(hasItem(DEFAULT_THUMBNAIL_FILE_CONTENT_TYPE)))
                .andExpect(jsonPath("$.[*].thumbnailFile").value(hasItem(Base64Utils.encodeToString(DEFAULT_THUMBNAIL_FILE))))
                .andExpect(jsonPath("$.[*].logo").value(hasItem(DEFAULT_LOGO)))
                .andExpect(jsonPath("$.[*].logoFileContentType").value(hasItem(DEFAULT_LOGO_FILE_CONTENT_TYPE)))
                .andExpect(jsonPath("$.[*].logoFile").value(hasItem(Base64Utils.encodeToString(DEFAULT_LOGO_FILE))))
                .andExpect(jsonPath("$.[*].ord").value(hasItem(DEFAULT_ORD)))
                .andExpect(jsonPath("$.[*].active").value(hasItem(DEFAULT_ACTIVE.booleanValue())))
                .andReturn();

        System.out.println(mvcResult.getResponse().getContentAsString());
    }

    @SuppressWarnings({ "unchecked" })
    void getAllCategoriesWithEagerRelationshipsIsEnabled() throws Exception {
        when(categoryServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restCategoryMockMvc.perform(get(ENTITY_API_URL + "?eagerload=true")).andExpect(status().isOk());

        verify(categoryServiceMock, times(1)).findAllWithEagerRelationships(any());
    }

    @SuppressWarnings({ "unchecked" })
    void getAllCategoriesWithEagerRelationshipsIsNotEnabled() throws Exception {
        when(categoryServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restCategoryMockMvc.perform(get(ENTITY_API_URL + "?eagerload=true")).andExpect(status().isOk());

        verify(categoryServiceMock, times(1)).findAllWithEagerRelationships(any());
    }

    @Test
    @Transactional
    void getCategory() throws Exception {
        // Initialize the database
        categoryRepository.saveAndFlush(category);

        // Get the category
        restCategoryMockMvc
            .perform(get(ENTITY_API_URL_ID, category.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(category.getId().intValue()))
            .andExpect(jsonPath("$.shortName").value(DEFAULT_SHORT_NAME))
            .andExpect(jsonPath("$.thumbnail").value(DEFAULT_THUMBNAIL))
            .andExpect(jsonPath("$.thumbnailFileContentType").value(DEFAULT_THUMBNAIL_FILE_CONTENT_TYPE))
            .andExpect(jsonPath("$.thumbnailFile").value(Base64Utils.encodeToString(DEFAULT_THUMBNAIL_FILE)))
            .andExpect(jsonPath("$.logo").value(DEFAULT_LOGO))
            .andExpect(jsonPath("$.logoFileContentType").value(DEFAULT_LOGO_FILE_CONTENT_TYPE))
            .andExpect(jsonPath("$.logoFile").value(Base64Utils.encodeToString(DEFAULT_LOGO_FILE)))
            .andExpect(jsonPath("$.ord").value(DEFAULT_ORD))
            .andExpect(jsonPath("$.active").value(DEFAULT_ACTIVE.booleanValue()));
    }

    @Test
    @Transactional
    void getCategoriesByIdFiltering() throws Exception {
        // Initialize the database
        categoryRepository.saveAndFlush(category);

        Long id = category.getId();

        defaultCategoryShouldBeFound("id.equals=" + id);
        defaultCategoryShouldNotBeFound("id.notEquals=" + id);

        defaultCategoryShouldBeFound("id.greaterThanOrEqual=" + id);
        defaultCategoryShouldNotBeFound("id.greaterThan=" + id);

        defaultCategoryShouldBeFound("id.lessThanOrEqual=" + id);
        defaultCategoryShouldNotBeFound("id.lessThan=" + id);
    }

    @Test
    @Transactional
    void getAllCategoriesByShortNameIsEqualToSomething() throws Exception {
        // Initialize the database
        categoryRepository.saveAndFlush(category);

        // Get all the categoryList where shortName equals to DEFAULT_SHORT_NAME
        defaultCategoryShouldBeFound("shortName.equals=" + DEFAULT_SHORT_NAME);

        // Get all the categoryList where shortName equals to UPDATED_SHORT_NAME
        defaultCategoryShouldNotBeFound("shortName.equals=" + UPDATED_SHORT_NAME);
    }

    @Test
    @Transactional
    void getAllCategoriesByShortNameIsNotEqualToSomething() throws Exception {
        // Initialize the database
        categoryRepository.saveAndFlush(category);

        // Get all the categoryList where shortName not equals to DEFAULT_SHORT_NAME
        defaultCategoryShouldNotBeFound("shortName.notEquals=" + DEFAULT_SHORT_NAME);

        // Get all the categoryList where shortName not equals to UPDATED_SHORT_NAME
        defaultCategoryShouldBeFound("shortName.notEquals=" + UPDATED_SHORT_NAME);
    }

    @Test
    @Transactional
    void getAllCategoriesByShortNameIsInShouldWork() throws Exception {
        // Initialize the database
        categoryRepository.saveAndFlush(category);

        // Get all the categoryList where shortName in DEFAULT_SHORT_NAME or UPDATED_SHORT_NAME
        defaultCategoryShouldBeFound("shortName.in=" + DEFAULT_SHORT_NAME + "," + UPDATED_SHORT_NAME);

        // Get all the categoryList where shortName equals to UPDATED_SHORT_NAME
        defaultCategoryShouldNotBeFound("shortName.in=" + UPDATED_SHORT_NAME);
    }

    @Test
    @Transactional
    void getAllCategoriesByShortNameIsNullOrNotNull() throws Exception {
        // Initialize the database
        categoryRepository.saveAndFlush(category);

        // Get all the categoryList where shortName is not null
        defaultCategoryShouldBeFound("shortName.specified=true");

        // Get all the categoryList where shortName is null
        defaultCategoryShouldNotBeFound("shortName.specified=false");
    }

    @Test
    @Transactional
    void getAllCategoriesByShortNameContainsSomething() throws Exception {
        // Initialize the database
        categoryRepository.saveAndFlush(category);

        // Get all the categoryList where shortName contains DEFAULT_SHORT_NAME
        defaultCategoryShouldBeFound("shortName.contains=" + DEFAULT_SHORT_NAME);

        // Get all the categoryList where shortName contains UPDATED_SHORT_NAME
        defaultCategoryShouldNotBeFound("shortName.contains=" + UPDATED_SHORT_NAME);
    }

    @Test
    @Transactional
    void getAllCategoriesByShortNameNotContainsSomething() throws Exception {
        // Initialize the database
        categoryRepository.saveAndFlush(category);

        // Get all the categoryList where shortName does not contain DEFAULT_SHORT_NAME
        defaultCategoryShouldNotBeFound("shortName.doesNotContain=" + DEFAULT_SHORT_NAME);

        // Get all the categoryList where shortName does not contain UPDATED_SHORT_NAME
        defaultCategoryShouldBeFound("shortName.doesNotContain=" + UPDATED_SHORT_NAME);
    }

    @Test
    @Transactional
    void getAllCategoriesByThumbnailIsEqualToSomething() throws Exception {
        // Initialize the database
        categoryRepository.saveAndFlush(category);

        // Get all the categoryList where thumbnail equals to DEFAULT_THUMBNAIL
        defaultCategoryShouldBeFound("thumbnail.equals=" + DEFAULT_THUMBNAIL);

        // Get all the categoryList where thumbnail equals to UPDATED_THUMBNAIL
        defaultCategoryShouldNotBeFound("thumbnail.equals=" + UPDATED_THUMBNAIL);
    }

    @Test
    @Transactional
    void getAllCategoriesByThumbnailIsNotEqualToSomething() throws Exception {
        // Initialize the database
        categoryRepository.saveAndFlush(category);

        // Get all the categoryList where thumbnail not equals to DEFAULT_THUMBNAIL
        defaultCategoryShouldNotBeFound("thumbnail.notEquals=" + DEFAULT_THUMBNAIL);

        // Get all the categoryList where thumbnail not equals to UPDATED_THUMBNAIL
        defaultCategoryShouldBeFound("thumbnail.notEquals=" + UPDATED_THUMBNAIL);
    }

    @Test
    @Transactional
    void getAllCategoriesByThumbnailIsInShouldWork() throws Exception {
        // Initialize the database
        categoryRepository.saveAndFlush(category);

        // Get all the categoryList where thumbnail in DEFAULT_THUMBNAIL or UPDATED_THUMBNAIL
        defaultCategoryShouldBeFound("thumbnail.in=" + DEFAULT_THUMBNAIL + "," + UPDATED_THUMBNAIL);

        // Get all the categoryList where thumbnail equals to UPDATED_THUMBNAIL
        defaultCategoryShouldNotBeFound("thumbnail.in=" + UPDATED_THUMBNAIL);
    }

    @Test
    @Transactional
    void getAllCategoriesByThumbnailIsNullOrNotNull() throws Exception {
        // Initialize the database
        categoryRepository.saveAndFlush(category);

        // Get all the categoryList where thumbnail is not null
        defaultCategoryShouldBeFound("thumbnail.specified=true");

        // Get all the categoryList where thumbnail is null
        defaultCategoryShouldNotBeFound("thumbnail.specified=false");
    }

    @Test
    @Transactional
    void getAllCategoriesByThumbnailContainsSomething() throws Exception {
        // Initialize the database
        categoryRepository.saveAndFlush(category);

        // Get all the categoryList where thumbnail contains DEFAULT_THUMBNAIL
        defaultCategoryShouldBeFound("thumbnail.contains=" + DEFAULT_THUMBNAIL);

        // Get all the categoryList where thumbnail contains UPDATED_THUMBNAIL
        defaultCategoryShouldNotBeFound("thumbnail.contains=" + UPDATED_THUMBNAIL);
    }

    @Test
    @Transactional
    void getAllCategoriesByThumbnailNotContainsSomething() throws Exception {
        // Initialize the database
        categoryRepository.saveAndFlush(category);

        // Get all the categoryList where thumbnail does not contain DEFAULT_THUMBNAIL
        defaultCategoryShouldNotBeFound("thumbnail.doesNotContain=" + DEFAULT_THUMBNAIL);

        // Get all the categoryList where thumbnail does not contain UPDATED_THUMBNAIL
        defaultCategoryShouldBeFound("thumbnail.doesNotContain=" + UPDATED_THUMBNAIL);
    }

    @Test
    @Transactional
    void getAllCategoriesByLogoIsEqualToSomething() throws Exception {
        // Initialize the database
        categoryRepository.saveAndFlush(category);

        // Get all the categoryList where logo equals to DEFAULT_LOGO
        defaultCategoryShouldBeFound("logo.equals=" + DEFAULT_LOGO);

        // Get all the categoryList where logo equals to UPDATED_LOGO
        defaultCategoryShouldNotBeFound("logo.equals=" + UPDATED_LOGO);
    }

    @Test
    @Transactional
    void getAllCategoriesByLogoIsNotEqualToSomething() throws Exception {
        // Initialize the database
        categoryRepository.saveAndFlush(category);

        // Get all the categoryList where logo not equals to DEFAULT_LOGO
        defaultCategoryShouldNotBeFound("logo.notEquals=" + DEFAULT_LOGO);

        // Get all the categoryList where logo not equals to UPDATED_LOGO
        defaultCategoryShouldBeFound("logo.notEquals=" + UPDATED_LOGO);
    }

    @Test
    @Transactional
    void getAllCategoriesByLogoIsInShouldWork() throws Exception {
        // Initialize the database
        categoryRepository.saveAndFlush(category);

        // Get all the categoryList where logo in DEFAULT_LOGO or UPDATED_LOGO
        defaultCategoryShouldBeFound("logo.in=" + DEFAULT_LOGO + "," + UPDATED_LOGO);

        // Get all the categoryList where logo equals to UPDATED_LOGO
        defaultCategoryShouldNotBeFound("logo.in=" + UPDATED_LOGO);
    }

    @Test
    @Transactional
    void getAllCategoriesByLogoIsNullOrNotNull() throws Exception {
        // Initialize the database
        categoryRepository.saveAndFlush(category);

        // Get all the categoryList where logo is not null
        defaultCategoryShouldBeFound("logo.specified=true");

        // Get all the categoryList where logo is null
        defaultCategoryShouldNotBeFound("logo.specified=false");
    }

    @Test
    @Transactional
    void getAllCategoriesByLogoContainsSomething() throws Exception {
        // Initialize the database
        categoryRepository.saveAndFlush(category);

        // Get all the categoryList where logo contains DEFAULT_LOGO
        defaultCategoryShouldBeFound("logo.contains=" + DEFAULT_LOGO);

        // Get all the categoryList where logo contains UPDATED_LOGO
        defaultCategoryShouldNotBeFound("logo.contains=" + UPDATED_LOGO);
    }

    @Test
    @Transactional
    void getAllCategoriesByLogoNotContainsSomething() throws Exception {
        // Initialize the database
        categoryRepository.saveAndFlush(category);

        // Get all the categoryList where logo does not contain DEFAULT_LOGO
        defaultCategoryShouldNotBeFound("logo.doesNotContain=" + DEFAULT_LOGO);

        // Get all the categoryList where logo does not contain UPDATED_LOGO
        defaultCategoryShouldBeFound("logo.doesNotContain=" + UPDATED_LOGO);
    }

    @Test
    @Transactional
    void getAllCategoriesByOrdIsEqualToSomething() throws Exception {
        // Initialize the database
        categoryRepository.saveAndFlush(category);

        // Get all the categoryList where ord equals to DEFAULT_ORD
        defaultCategoryShouldBeFound("ord.equals=" + DEFAULT_ORD);

        // Get all the categoryList where ord equals to UPDATED_ORD
        defaultCategoryShouldNotBeFound("ord.equals=" + UPDATED_ORD);
    }

    @Test
    @Transactional
    void getAllCategoriesByOrdIsNotEqualToSomething() throws Exception {
        // Initialize the database
        categoryRepository.saveAndFlush(category);

        // Get all the categoryList where ord not equals to DEFAULT_ORD
        defaultCategoryShouldNotBeFound("ord.notEquals=" + DEFAULT_ORD);

        // Get all the categoryList where ord not equals to UPDATED_ORD
        defaultCategoryShouldBeFound("ord.notEquals=" + UPDATED_ORD);
    }

    @Test
    @Transactional
    void getAllCategoriesByOrdIsInShouldWork() throws Exception {
        // Initialize the database
        categoryRepository.saveAndFlush(category);

        // Get all the categoryList where ord in DEFAULT_ORD or UPDATED_ORD
        defaultCategoryShouldBeFound("ord.in=" + DEFAULT_ORD + "," + UPDATED_ORD);

        // Get all the categoryList where ord equals to UPDATED_ORD
        defaultCategoryShouldNotBeFound("ord.in=" + UPDATED_ORD);
    }

    @Test
    @Transactional
    void getAllCategoriesByOrdIsNullOrNotNull() throws Exception {
        // Initialize the database
        categoryRepository.saveAndFlush(category);

        // Get all the categoryList where ord is not null
        defaultCategoryShouldBeFound("ord.specified=true");

        // Get all the categoryList where ord is null
        defaultCategoryShouldNotBeFound("ord.specified=false");
    }

    @Test
    @Transactional
    void getAllCategoriesByOrdIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        categoryRepository.saveAndFlush(category);

        // Get all the categoryList where ord is greater than or equal to DEFAULT_ORD
        defaultCategoryShouldBeFound("ord.greaterThanOrEqual=" + DEFAULT_ORD);

        // Get all the categoryList where ord is greater than or equal to UPDATED_ORD
        defaultCategoryShouldNotBeFound("ord.greaterThanOrEqual=" + UPDATED_ORD);
    }

    @Test
    @Transactional
    void getAllCategoriesByOrdIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        categoryRepository.saveAndFlush(category);

        // Get all the categoryList where ord is less than or equal to DEFAULT_ORD
        defaultCategoryShouldBeFound("ord.lessThanOrEqual=" + DEFAULT_ORD);

        // Get all the categoryList where ord is less than or equal to SMALLER_ORD
        defaultCategoryShouldNotBeFound("ord.lessThanOrEqual=" + SMALLER_ORD);
    }

    @Test
    @Transactional
    void getAllCategoriesByOrdIsLessThanSomething() throws Exception {
        // Initialize the database
        categoryRepository.saveAndFlush(category);

        // Get all the categoryList where ord is less than DEFAULT_ORD
        defaultCategoryShouldNotBeFound("ord.lessThan=" + DEFAULT_ORD);

        // Get all the categoryList where ord is less than UPDATED_ORD
        defaultCategoryShouldBeFound("ord.lessThan=" + UPDATED_ORD);
    }

    @Test
    @Transactional
    void getAllCategoriesByOrdIsGreaterThanSomething() throws Exception {
        // Initialize the database
        categoryRepository.saveAndFlush(category);

        // Get all the categoryList where ord is greater than DEFAULT_ORD
        defaultCategoryShouldNotBeFound("ord.greaterThan=" + DEFAULT_ORD);

        // Get all the categoryList where ord is greater than SMALLER_ORD
        defaultCategoryShouldBeFound("ord.greaterThan=" + SMALLER_ORD);
    }

    @Test
    @Transactional
    void getAllCategoriesByActiveIsEqualToSomething() throws Exception {
        // Initialize the database
        categoryRepository.saveAndFlush(category);

        // Get all the categoryList where active equals to DEFAULT_ACTIVE
        defaultCategoryShouldBeFound("active.equals=" + DEFAULT_ACTIVE);

        // Get all the categoryList where active equals to UPDATED_ACTIVE
        defaultCategoryShouldNotBeFound("active.equals=" + UPDATED_ACTIVE);
    }

    @Test
    @Transactional
    void getAllCategoriesByActiveIsNotEqualToSomething() throws Exception {
        // Initialize the database
        categoryRepository.saveAndFlush(category);

        // Get all the categoryList where active not equals to DEFAULT_ACTIVE
        defaultCategoryShouldNotBeFound("active.notEquals=" + DEFAULT_ACTIVE);

        // Get all the categoryList where active not equals to UPDATED_ACTIVE
        defaultCategoryShouldBeFound("active.notEquals=" + UPDATED_ACTIVE);
    }

    @Test
    @Transactional
    void getAllCategoriesByActiveIsInShouldWork() throws Exception {
        // Initialize the database
        categoryRepository.saveAndFlush(category);

        // Get all the categoryList where active in DEFAULT_ACTIVE or UPDATED_ACTIVE
        defaultCategoryShouldBeFound("active.in=" + DEFAULT_ACTIVE + "," + UPDATED_ACTIVE);

        // Get all the categoryList where active equals to UPDATED_ACTIVE
        defaultCategoryShouldNotBeFound("active.in=" + UPDATED_ACTIVE);
    }

    @Test
    @Transactional
    void getAllCategoriesByActiveIsNullOrNotNull() throws Exception {
        // Initialize the database
        categoryRepository.saveAndFlush(category);

        // Get all the categoryList where active is not null
        defaultCategoryShouldBeFound("active.specified=true");

        // Get all the categoryList where active is null
        defaultCategoryShouldNotBeFound("active.specified=false");
    }

    @Test
    @Transactional
    void getAllCategoriesByParentIsEqualToSomething() throws Exception {
        // Initialize the database
        categoryRepository.saveAndFlush(category);
        Category parent = CategoryResourceIT.createEntity(em, DEFAULT_SHORT_NAME + 2);
        em.persist(parent);
        em.flush();
        category.setParent(parent);
        //        category.setShortName(DEFAULT_SHORT_NAME + 2);
        categoryRepository.saveAndFlush(category);
        Long parentId = parent.getId();

        // Get all the categoryList where parent equals to parentId
        defaultCategoryShouldBeFound("parentId.equals=" + parentId);

        // Get all the categoryList where parent equals to (parentId + 1)
        defaultCategoryShouldNotBeFound("parentId.equals=" + (parentId + 1));
    }

    @Test
    @Transactional
    void getAllCategoriesByNameIsEqualToSomething() throws Exception {
        // Initialize the database
        categoryRepository.saveAndFlush(category);
        I18n name = I18nResourceIT.createEntity(em);
        em.persist(name);
        em.flush();
        category.addName(name);
        categoryRepository.saveAndFlush(category);
        Long nameId = name.getId();

        // Get all the categoryList where name equals to nameId
        defaultCategoryShouldBeFound("nameId.equals=" + nameId);

        // Get all the categoryList where name equals to (nameId + 1)
        defaultCategoryShouldNotBeFound("nameId.equals=" + (nameId + 1));
    }

    /**
     * Executes the search, and checks that the default entity is returned.
     */
    private void defaultCategoryShouldBeFound(String filter) throws Exception {
        restCategoryMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(category.getId().intValue())))
            .andExpect(jsonPath("$.[*].shortName").value(hasItem(DEFAULT_SHORT_NAME)))
            .andExpect(jsonPath("$.[*].thumbnail").value(hasItem(DEFAULT_THUMBNAIL)))
            .andExpect(jsonPath("$.[*].thumbnailFileContentType").value(hasItem(DEFAULT_THUMBNAIL_FILE_CONTENT_TYPE)))
            .andExpect(jsonPath("$.[*].thumbnailFile").value(hasItem(Base64Utils.encodeToString(DEFAULT_THUMBNAIL_FILE))))
            .andExpect(jsonPath("$.[*].logo").value(hasItem(DEFAULT_LOGO)))
            .andExpect(jsonPath("$.[*].logoFileContentType").value(hasItem(DEFAULT_LOGO_FILE_CONTENT_TYPE)))
            .andExpect(jsonPath("$.[*].logoFile").value(hasItem(Base64Utils.encodeToString(DEFAULT_LOGO_FILE))))
            .andExpect(jsonPath("$.[*].ord").value(hasItem(DEFAULT_ORD)))
            .andExpect(jsonPath("$.[*].active").value(hasItem(DEFAULT_ACTIVE.booleanValue())));

        // Check, that the count call also returns 1
        restCategoryMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("1"));
    }

    /**
     * Executes the search, and checks that the default entity is not returned.
     */
    private void defaultCategoryShouldNotBeFound(String filter) throws Exception {
        restCategoryMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty());

        // Check, that the count call also returns 0
        restCategoryMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("0"));
    }

    @Test
    @Transactional
    void getNonExistingCategory() throws Exception {
        // Get the category
        restCategoryMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putNewCategory() throws Exception {
        // Initialize the database
        categoryRepository.saveAndFlush(category);

        int databaseSizeBeforeUpdate = categoryRepository.findAll().size();

        // Update the category
        Category updatedCategory = categoryRepository.findById(category.getId()).get();
        // Disconnect from session so that the updates on updatedCategory are not directly saved in db
        em.detach(updatedCategory);
        updatedCategory
            .shortName(UPDATED_SHORT_NAME)
            .thumbnail(UPDATED_THUMBNAIL)
            .thumbnailFile(UPDATED_THUMBNAIL_FILE)
            .thumbnailFileContentType(UPDATED_THUMBNAIL_FILE_CONTENT_TYPE)
            .logo(UPDATED_LOGO)
            .logoFile(UPDATED_LOGO_FILE)
            .logoFileContentType(UPDATED_LOGO_FILE_CONTENT_TYPE)
            .ord(UPDATED_ORD)
            .active(UPDATED_ACTIVE);
        CategoryDTO categoryDTO = categoryMapper.toDto(updatedCategory);

        restCategoryMockMvc
            .perform(
                put(ENTITY_API_URL_ID, categoryDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(categoryDTO))
            )
            .andExpect(status().isOk());

        // Validate the Category in the database
        List<Category> categoryList = categoryRepository.findAll();
        assertThat(categoryList).hasSize(databaseSizeBeforeUpdate);
        Category testCategory = categoryList.get(categoryList.size() - 1);
        assertThat(testCategory.getShortName()).isEqualTo(UPDATED_SHORT_NAME);
        assertThat(testCategory.getThumbnail()).isEqualTo(UPDATED_THUMBNAIL);
        assertThat(testCategory.getThumbnailFile()).isEqualTo(UPDATED_THUMBNAIL_FILE);
        assertThat(testCategory.getThumbnailFileContentType()).isEqualTo(UPDATED_THUMBNAIL_FILE_CONTENT_TYPE);
        assertThat(testCategory.getLogo()).isEqualTo(UPDATED_LOGO);
        assertThat(testCategory.getLogoFile()).isEqualTo(null);
        assertThat(testCategory.getLogoFileContentType()).isEqualTo(UPDATED_LOGO_FILE_CONTENT_TYPE);
        assertThat(testCategory.getOrd()).isEqualTo(UPDATED_ORD);
        assertThat(testCategory.getActive()).isEqualTo(UPDATED_ACTIVE);
    }

    @Test
    @Transactional
    void putNonExistingCategory() throws Exception {
        int databaseSizeBeforeUpdate = categoryRepository.findAll().size();
        category.setId(count.incrementAndGet());

        // Create the Category
        CategoryDTO categoryDTO = categoryMapper.toDto(category);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restCategoryMockMvc
            .perform(
                put(ENTITY_API_URL_ID, categoryDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(categoryDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Category in the database
        List<Category> categoryList = categoryRepository.findAll();
        assertThat(categoryList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchCategory() throws Exception {
        int databaseSizeBeforeUpdate = categoryRepository.findAll().size();
        category.setId(count.incrementAndGet());

        // Create the Category
        CategoryDTO categoryDTO = categoryMapper.toDto(category);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restCategoryMockMvc
            .perform(
                put(ENTITY_API_URL_ID, count.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(categoryDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Category in the database
        List<Category> categoryList = categoryRepository.findAll();
        assertThat(categoryList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamCategory() throws Exception {
        int databaseSizeBeforeUpdate = categoryRepository.findAll().size();
        category.setId(count.incrementAndGet());

        // Create the Category
        CategoryDTO categoryDTO = categoryMapper.toDto(category);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restCategoryMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(categoryDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Category in the database
        List<Category> categoryList = categoryRepository.findAll();
        assertThat(categoryList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateCategoryWithPatch() throws Exception {
        // Initialize the database
        categoryRepository.saveAndFlush(category);

        int databaseSizeBeforeUpdate = categoryRepository.findAll().size();

        // Update the category using partial update
        Category partialUpdatedCategory = new Category();
        partialUpdatedCategory.setId(category.getId());

        partialUpdatedCategory
            .shortName(UPDATED_SHORT_NAME)
            .logo(UPDATED_LOGO)
            .logoFile(UPDATED_LOGO_FILE)
            .logoFileContentType(UPDATED_LOGO_FILE_CONTENT_TYPE)
            .ord(UPDATED_ORD);

        restCategoryMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedCategory.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedCategory))
            )
            .andExpect(status().isOk());

        // Validate the Category in the database
        List<Category> categoryList = categoryRepository.findAll();
        assertThat(categoryList).hasSize(databaseSizeBeforeUpdate);
        Category testCategory = categoryList.get(categoryList.size() - 1);
        assertThat(testCategory.getShortName()).isEqualTo(UPDATED_SHORT_NAME);
        assertThat(testCategory.getThumbnail()).isEqualTo(DEFAULT_THUMBNAIL);
        assertThat(testCategory.getThumbnailFile()).isEqualTo(DEFAULT_THUMBNAIL_FILE);
        assertThat(testCategory.getThumbnailFileContentType()).isEqualTo(DEFAULT_THUMBNAIL_FILE_CONTENT_TYPE);
        assertThat(testCategory.getLogo()).isEqualTo(UPDATED_LOGO);
        assertThat(testCategory.getLogoFile()).isEqualTo(null);
        assertThat(testCategory.getLogoFileContentType()).isEqualTo(UPDATED_LOGO_FILE_CONTENT_TYPE);
        assertThat(testCategory.getOrd()).isEqualTo(UPDATED_ORD);
        assertThat(testCategory.getActive()).isEqualTo(DEFAULT_ACTIVE);
    }

    @Test
    @Transactional
    void fullUpdateCategoryWithPatch() throws Exception {
        // Initialize the database
        categoryRepository.saveAndFlush(category);

        int databaseSizeBeforeUpdate = categoryRepository.findAll().size();

        // Update the category using partial update
        Category partialUpdatedCategory = new Category();
        partialUpdatedCategory.setId(category.getId());

        partialUpdatedCategory
            .shortName(UPDATED_SHORT_NAME)
            .thumbnail(UPDATED_THUMBNAIL)
            .thumbnailFile(UPDATED_THUMBNAIL_FILE)
            .thumbnailFileContentType(UPDATED_THUMBNAIL_FILE_CONTENT_TYPE)
            .logo(UPDATED_LOGO)
            .logoFile(UPDATED_LOGO_FILE)
            .logoFileContentType(UPDATED_LOGO_FILE_CONTENT_TYPE)
            .ord(UPDATED_ORD)
            .active(UPDATED_ACTIVE);

        restCategoryMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedCategory.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedCategory))
            )
            .andExpect(status().isOk());

        // Validate the Category in the database
        List<Category> categoryList = categoryRepository.findAll();
        assertThat(categoryList).hasSize(databaseSizeBeforeUpdate);
        Category testCategory = categoryList.get(categoryList.size() - 1);
        assertThat(testCategory.getShortName()).isEqualTo(UPDATED_SHORT_NAME);
        assertThat(testCategory.getThumbnail()).isEqualTo(UPDATED_THUMBNAIL);
        assertThat(testCategory.getThumbnailFile()).isEqualTo(UPDATED_THUMBNAIL_FILE);
        assertThat(testCategory.getThumbnailFileContentType()).isEqualTo(UPDATED_THUMBNAIL_FILE_CONTENT_TYPE);
        assertThat(testCategory.getLogo()).isEqualTo(UPDATED_LOGO);
        assertThat(testCategory.getLogoFile()).isEqualTo(null);
        assertThat(testCategory.getLogoFileContentType()).isEqualTo(UPDATED_LOGO_FILE_CONTENT_TYPE);
        assertThat(testCategory.getOrd()).isEqualTo(UPDATED_ORD);
        assertThat(testCategory.getActive()).isEqualTo(UPDATED_ACTIVE);
    }

    @Test
    @Transactional
    void patchNonExistingCategory() throws Exception {
        int databaseSizeBeforeUpdate = categoryRepository.findAll().size();
        category.setId(count.incrementAndGet());

        // Create the Category
        CategoryDTO categoryDTO = categoryMapper.toDto(category);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restCategoryMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, categoryDTO.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(categoryDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Category in the database
        List<Category> categoryList = categoryRepository.findAll();
        assertThat(categoryList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchCategory() throws Exception {
        int databaseSizeBeforeUpdate = categoryRepository.findAll().size();
        category.setId(count.incrementAndGet());

        // Create the Category
        CategoryDTO categoryDTO = categoryMapper.toDto(category);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restCategoryMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, count.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(categoryDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Category in the database
        List<Category> categoryList = categoryRepository.findAll();
        assertThat(categoryList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamCategory() throws Exception {
        int databaseSizeBeforeUpdate = categoryRepository.findAll().size();
        category.setId(count.incrementAndGet());

        // Create the Category
        CategoryDTO categoryDTO = categoryMapper.toDto(category);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restCategoryMockMvc
            .perform(
                patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(TestUtil.convertObjectToJsonBytes(categoryDTO))
            )
            .andExpect(status().isMethodNotAllowed());

        // Validate the Category in the database
        List<Category> categoryList = categoryRepository.findAll();
        assertThat(categoryList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteCategory() throws Exception {
        // Initialize the database
        categoryRepository.saveAndFlush(category);

        int databaseSizeBeforeDelete = categoryRepository.findAll().size();

        // Delete the category
        restCategoryMockMvc
            .perform(delete(ENTITY_API_URL_ID, category.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        List<Category> categoryList = categoryRepository.findAll();
        assertThat(categoryList).hasSize(databaseSizeBeforeDelete - 1);
    }
}
