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
import org.springframework.util.Base64Utils;
import org.springframework.validation.Validator;
import uz.paykap.PkbillmsApp;
import uz.paykap.IntegrationTest;
import uz.paykap.domain.Category;
import uz.paykap.repository.CategoryRepository;
import uz.paykap.service.CategoryQueryService;
import uz.paykap.service.CategoryService;
import uz.paykap.service.dto.CategoryDTO;
import uz.paykap.service.mapper.CategoryMapper;
import uz.paykap.web.rest.errors.ExceptionTranslator;

/**
 * Test class for the CategoryResource REST controller.
 *
 * @see CategoryResource
 */
@RunWith(SpringRunner.class)
@IntegrationTest
public class CategoryResourceIntTest {

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

    private static final Boolean DEFAULT_ACTIVE = true;
    private static final Boolean UPDATED_ACTIVE = false;

    @Autowired
    private CategoryRepository categoryRepository;

    @Mock
    private CategoryRepository categoryRepositoryMock;

    @Autowired
    private CategoryMapper categoryMapper;

    @Mock
    private CategoryService categoryServiceMock;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private CategoryQueryService categoryQueryService;

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

    private MockMvc restCategoryMockMvc;

    private Category category;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        final CategoryResource categoryResource = new CategoryResource(categoryService, categoryQueryService, categoryRepository);
        this.restCategoryMockMvc =
            MockMvcBuilders
                .standaloneSetup(categoryResource)
                .setCustomArgumentResolvers(pageableArgumentResolver)
                .setControllerAdvice(exceptionTranslator)
                .setConversionService(createFormattingConversionService())
                .setMessageConverters(jacksonMessageConverter)
                .setValidator(validator)
                .build();
    }

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
        //        I18n i18n = I18nResourceIntTest.createEntity(em);
        //        em.persist(i18n);
        //        em.flush();
        //        category.getNames().add();
        return category;
    }

    @Before
    public void initTest() {
        category = createEntity(em);
    }

    @Test
    @Transactional
    public void createCategory() throws Exception {
        int databaseSizeBeforeCreate = categoryRepository.findAll().size();

        // Create the Category
        CategoryDTO categoryDTO = categoryMapper.toDto(category);
        restCategoryMockMvc
            .perform(
                post("/api/categories").contentType(TestUtil.APPLICATION_JSON_UTF8).content(TestUtil.convertObjectToJsonBytes(categoryDTO))
            )
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
        assertThat(testCategory.getLogoFile()).isEqualTo(DEFAULT_LOGO_FILE);
        assertThat(testCategory.getLogoFileContentType()).isEqualTo(DEFAULT_LOGO_FILE_CONTENT_TYPE);
        assertThat(testCategory.getOrd()).isEqualTo(DEFAULT_ORD);
        assertThat(testCategory.isActive()).isEqualTo(DEFAULT_ACTIVE);
    }

    @Test
    @Transactional
    public void createCategoryWithExistingId() throws Exception {
        int databaseSizeBeforeCreate = categoryRepository.findAll().size();

        // Create the Category with an existing ID
        category.setId(1L);
        CategoryDTO categoryDTO = categoryMapper.toDto(category);

        // An entity with an existing ID cannot be created, so this API call must fail
        restCategoryMockMvc
            .perform(
                post("/api/categories").contentType(TestUtil.APPLICATION_JSON_UTF8).content(TestUtil.convertObjectToJsonBytes(categoryDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Category in the database
        List<Category> categoryList = categoryRepository.findAll();
        assertThat(categoryList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    public void checkShortNameIsRequired() throws Exception {
        int databaseSizeBeforeTest = categoryRepository.findAll().size();
        // set the field null
        category.setShortName(null);

        // Create the Category, which fails.
        CategoryDTO categoryDTO = categoryMapper.toDto(category);

        restCategoryMockMvc
            .perform(
                post("/api/categories").contentType(TestUtil.APPLICATION_JSON_UTF8).content(TestUtil.convertObjectToJsonBytes(categoryDTO))
            )
            .andExpect(status().isBadRequest());

        List<Category> categoryList = categoryRepository.findAll();
        assertThat(categoryList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkActiveIsRequired() throws Exception {
        int databaseSizeBeforeTest = categoryRepository.findAll().size();
        // set the field null
        category.setActive(null);

        // Create the Category, which fails.
        CategoryDTO categoryDTO = categoryMapper.toDto(category);

        restCategoryMockMvc
            .perform(
                post("/api/categories").contentType(TestUtil.APPLICATION_JSON_UTF8).content(TestUtil.convertObjectToJsonBytes(categoryDTO))
            )
            .andExpect(status().isBadRequest());

        List<Category> categoryList = categoryRepository.findAll();
        assertThat(categoryList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void getAllCategories() throws Exception {
        // Initialize the database
        categoryRepository.saveAndFlush(category);

        // Get all the categoryList
        restCategoryMockMvc
            .perform(get("/api/categories?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.[*].id").value(hasItem(category.getId().intValue())))
            .andExpect(jsonPath("$.[*].shortName").value(hasItem(DEFAULT_SHORT_NAME.toString())))
            .andExpect(jsonPath("$.[*].thumbnail").value(hasItem(DEFAULT_THUMBNAIL.toString())))
            .andExpect(jsonPath("$.[*].thumbnailFileContentType").value(hasItem(DEFAULT_THUMBNAIL_FILE_CONTENT_TYPE)))
            .andExpect(jsonPath("$.[*].thumbnailFile").value(hasItem(Base64Utils.encodeToString(DEFAULT_THUMBNAIL_FILE))))
            .andExpect(jsonPath("$.[*].logo").value(hasItem(DEFAULT_LOGO.toString())))
            .andExpect(jsonPath("$.[*].logoFileContentType").value(hasItem(DEFAULT_LOGO_FILE_CONTENT_TYPE)))
            .andExpect(jsonPath("$.[*].logoFile").value(hasItem(Base64Utils.encodeToString(DEFAULT_LOGO_FILE))))
            .andExpect(jsonPath("$.[*].ord").value(hasItem(DEFAULT_ORD)))
            .andExpect(jsonPath("$.[*].active").value(hasItem(DEFAULT_ACTIVE.booleanValue())));
    }

    @SuppressWarnings({ "unchecked" })
    public void getAllCategoriesWithEagerRelationshipsIsEnabled() throws Exception {
        CategoryResource categoryResource = new CategoryResource(categoryServiceMock, categoryQueryService, categoryRepository);
        when(categoryServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        MockMvc restCategoryMockMvc = MockMvcBuilders
            .standaloneSetup(categoryResource)
            .setCustomArgumentResolvers(pageableArgumentResolver)
            .setControllerAdvice(exceptionTranslator)
            .setConversionService(createFormattingConversionService())
            .setMessageConverters(jacksonMessageConverter)
            .build();

        restCategoryMockMvc.perform(get("/api/categories?eagerload=true")).andExpect(status().isOk());

        verify(categoryServiceMock, times(1)).findAllWithEagerRelationships(any());
    }

    @SuppressWarnings({ "unchecked" })
    public void getAllCategoriesWithEagerRelationshipsIsNotEnabled() throws Exception {
        CategoryResource categoryResource = new CategoryResource(categoryServiceMock, categoryQueryService, categoryRepository);
        when(categoryServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));
        MockMvc restCategoryMockMvc = MockMvcBuilders
            .standaloneSetup(categoryResource)
            .setCustomArgumentResolvers(pageableArgumentResolver)
            .setControllerAdvice(exceptionTranslator)
            .setConversionService(createFormattingConversionService())
            .setMessageConverters(jacksonMessageConverter)
            .build();

        restCategoryMockMvc.perform(get("/api/categories?eagerload=true")).andExpect(status().isOk());

        verify(categoryServiceMock, times(1)).findAllWithEagerRelationships(any());
    }

    @Test
    @Transactional
    public void getCategory() throws Exception {
        // Initialize the database
        categoryRepository.saveAndFlush(category);

        // Get the category
        restCategoryMockMvc
            .perform(get("/api/categories/{id}", category.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.id").value(category.getId().intValue()))
            .andExpect(jsonPath("$.shortName").value(DEFAULT_SHORT_NAME.toString()))
            .andExpect(jsonPath("$.thumbnail").value(DEFAULT_THUMBNAIL.toString()))
            .andExpect(jsonPath("$.thumbnailFileContentType").value(DEFAULT_THUMBNAIL_FILE_CONTENT_TYPE))
            .andExpect(jsonPath("$.thumbnailFile").value(Base64Utils.encodeToString(DEFAULT_THUMBNAIL_FILE)))
            .andExpect(jsonPath("$.logo").value(DEFAULT_LOGO.toString()))
            .andExpect(jsonPath("$.logoFileContentType").value(DEFAULT_LOGO_FILE_CONTENT_TYPE))
            .andExpect(jsonPath("$.logoFile").value(Base64Utils.encodeToString(DEFAULT_LOGO_FILE)))
            .andExpect(jsonPath("$.ord").value(DEFAULT_ORD))
            .andExpect(jsonPath("$.active").value(DEFAULT_ACTIVE.booleanValue()));
    }

    @Test
    @Transactional
    public void getAllCategoriesByShortNameIsEqualToSomething() throws Exception {
        // Initialize the database
        categoryRepository.saveAndFlush(category);

        // Get all the categoryList where shortName equals to DEFAULT_SHORT_NAME
        defaultCategoryShouldBeFound("shortName.equals=" + DEFAULT_SHORT_NAME);

        // Get all the categoryList where shortName equals to UPDATED_SHORT_NAME
        defaultCategoryShouldNotBeFound("shortName.equals=" + UPDATED_SHORT_NAME);
    }

    @Test
    @Transactional
    public void getAllCategoriesByShortNameIsInShouldWork() throws Exception {
        // Initialize the database
        categoryRepository.saveAndFlush(category);

        // Get all the categoryList where shortName in DEFAULT_SHORT_NAME or UPDATED_SHORT_NAME
        defaultCategoryShouldBeFound("shortName.in=" + DEFAULT_SHORT_NAME + "," + UPDATED_SHORT_NAME);

        // Get all the categoryList where shortName equals to UPDATED_SHORT_NAME
        defaultCategoryShouldNotBeFound("shortName.in=" + UPDATED_SHORT_NAME);
    }

    @Test
    @Transactional
    public void getAllCategoriesByShortNameIsNullOrNotNull() throws Exception {
        // Initialize the database
        categoryRepository.saveAndFlush(category);

        // Get all the categoryList where shortName is not null
        defaultCategoryShouldBeFound("shortName.specified=true");

        // Get all the categoryList where shortName is null
        defaultCategoryShouldNotBeFound("shortName.specified=false");
    }

    @Test
    @Transactional
    public void getAllCategoriesByThumbnailIsEqualToSomething() throws Exception {
        // Initialize the database
        categoryRepository.saveAndFlush(category);

        // Get all the categoryList where thumbnail equals to DEFAULT_THUMBNAIL
        defaultCategoryShouldBeFound("thumbnail.equals=" + DEFAULT_THUMBNAIL);

        // Get all the categoryList where thumbnail equals to UPDATED_THUMBNAIL
        defaultCategoryShouldNotBeFound("thumbnail.equals=" + UPDATED_THUMBNAIL);
    }

    @Test
    @Transactional
    public void getAllCategoriesByThumbnailIsInShouldWork() throws Exception {
        // Initialize the database
        categoryRepository.saveAndFlush(category);

        // Get all the categoryList where thumbnail in DEFAULT_THUMBNAIL or UPDATED_THUMBNAIL
        defaultCategoryShouldBeFound("thumbnail.in=" + DEFAULT_THUMBNAIL + "," + UPDATED_THUMBNAIL);

        // Get all the categoryList where thumbnail equals to UPDATED_THUMBNAIL
        defaultCategoryShouldNotBeFound("thumbnail.in=" + UPDATED_THUMBNAIL);
    }

    @Test
    @Transactional
    public void getAllCategoriesByThumbnailIsNullOrNotNull() throws Exception {
        // Initialize the database
        categoryRepository.saveAndFlush(category);

        // Get all the categoryList where thumbnail is not null
        defaultCategoryShouldBeFound("thumbnail.specified=true");

        // Get all the categoryList where thumbnail is null
        defaultCategoryShouldNotBeFound("thumbnail.specified=false");
    }

    @Test
    @Transactional
    public void getAllCategoriesByLogoIsEqualToSomething() throws Exception {
        // Initialize the database
        categoryRepository.saveAndFlush(category);

        // Get all the categoryList where logo equals to DEFAULT_LOGO
        defaultCategoryShouldBeFound("logo.equals=" + DEFAULT_LOGO);

        // Get all the categoryList where logo equals to UPDATED_LOGO
        defaultCategoryShouldNotBeFound("logo.equals=" + UPDATED_LOGO);
    }

    @Test
    @Transactional
    public void getAllCategoriesByLogoIsInShouldWork() throws Exception {
        // Initialize the database
        categoryRepository.saveAndFlush(category);

        // Get all the categoryList where logo in DEFAULT_LOGO or UPDATED_LOGO
        defaultCategoryShouldBeFound("logo.in=" + DEFAULT_LOGO + "," + UPDATED_LOGO);

        // Get all the categoryList where logo equals to UPDATED_LOGO
        defaultCategoryShouldNotBeFound("logo.in=" + UPDATED_LOGO);
    }

    @Test
    @Transactional
    public void getAllCategoriesByLogoIsNullOrNotNull() throws Exception {
        // Initialize the database
        categoryRepository.saveAndFlush(category);

        // Get all the categoryList where logo is not null
        defaultCategoryShouldBeFound("logo.specified=true");

        // Get all the categoryList where logo is null
        defaultCategoryShouldNotBeFound("logo.specified=false");
    }

    @Test
    @Transactional
    public void getAllCategoriesByOrdIsEqualToSomething() throws Exception {
        // Initialize the database
        categoryRepository.saveAndFlush(category);

        // Get all the categoryList where ord equals to DEFAULT_ORD
        defaultCategoryShouldBeFound("ord.equals=" + DEFAULT_ORD);

        // Get all the categoryList where ord equals to UPDATED_ORD
        defaultCategoryShouldNotBeFound("ord.equals=" + UPDATED_ORD);
    }

    @Test
    @Transactional
    public void getAllCategoriesByOrdIsInShouldWork() throws Exception {
        // Initialize the database
        categoryRepository.saveAndFlush(category);

        // Get all the categoryList where ord in DEFAULT_ORD or UPDATED_ORD
        defaultCategoryShouldBeFound("ord.in=" + DEFAULT_ORD + "," + UPDATED_ORD);

        // Get all the categoryList where ord equals to UPDATED_ORD
        defaultCategoryShouldNotBeFound("ord.in=" + UPDATED_ORD);
    }

    @Test
    @Transactional
    public void getAllCategoriesByOrdIsNullOrNotNull() throws Exception {
        // Initialize the database
        categoryRepository.saveAndFlush(category);

        // Get all the categoryList where ord is not null
        defaultCategoryShouldBeFound("ord.specified=true");

        // Get all the categoryList where ord is null
        defaultCategoryShouldNotBeFound("ord.specified=false");
    }

    @Test
    @Transactional
    public void getAllCategoriesByOrdIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        categoryRepository.saveAndFlush(category);

        // Get all the categoryList where ord greater than or equals to DEFAULT_ORD
        defaultCategoryShouldBeFound("ord.greaterOrEqualThan=" + DEFAULT_ORD);

        // Get all the categoryList where ord greater than or equals to UPDATED_ORD
        defaultCategoryShouldNotBeFound("ord.greaterOrEqualThan=" + UPDATED_ORD);
    }

    @Test
    @Transactional
    public void getAllCategoriesByOrdIsLessThanSomething() throws Exception {
        // Initialize the database
        categoryRepository.saveAndFlush(category);

        // Get all the categoryList where ord less than or equals to DEFAULT_ORD
        defaultCategoryShouldNotBeFound("ord.lessThan=" + DEFAULT_ORD);

        // Get all the categoryList where ord less than or equals to UPDATED_ORD
        defaultCategoryShouldBeFound("ord.lessThan=" + UPDATED_ORD);
    }

    @Test
    @Transactional
    public void getAllCategoriesByActiveIsEqualToSomething() throws Exception {
        // Initialize the database
        categoryRepository.saveAndFlush(category);

        // Get all the categoryList where active equals to DEFAULT_ACTIVE
        defaultCategoryShouldBeFound("active.equals=" + DEFAULT_ACTIVE);

        // Get all the categoryList where active equals to UPDATED_ACTIVE
        defaultCategoryShouldNotBeFound("active.equals=" + UPDATED_ACTIVE);
    }

    @Test
    @Transactional
    public void getAllCategoriesByActiveIsInShouldWork() throws Exception {
        // Initialize the database
        categoryRepository.saveAndFlush(category);

        // Get all the categoryList where active in DEFAULT_ACTIVE or UPDATED_ACTIVE
        defaultCategoryShouldBeFound("active.in=" + DEFAULT_ACTIVE + "," + UPDATED_ACTIVE);

        // Get all the categoryList where active equals to UPDATED_ACTIVE
        defaultCategoryShouldNotBeFound("active.in=" + UPDATED_ACTIVE);
    }

    @Test
    @Transactional
    public void getAllCategoriesByActiveIsNullOrNotNull() throws Exception {
        // Initialize the database
        categoryRepository.saveAndFlush(category);

        // Get all the categoryList where active is not null
        defaultCategoryShouldBeFound("active.specified=true");

        // Get all the categoryList where active is null
        defaultCategoryShouldNotBeFound("active.specified=false");
    }

    @Test
    @Transactional
    public void getAllCategoriesByParentIsEqualToSomething() throws Exception {
        // Initialize the database
        Category parent = CategoryResourceIntTest.createEntity(em);
        em.persist(parent);
        em.flush();
        category.setParent(parent);
        category.setShortName(DEFAULT_SHORT_NAME + 2);
        categoryRepository.saveAndFlush(category);
        Long parentId = parent.getId();

        // Get all the categoryList where parent equals to parentId
        defaultCategoryShouldBeFound("parentId.equals=" + parentId);

        // Get all the categoryList where parent equals to parentId + 1
        defaultCategoryShouldNotBeFound("parentId.equals=" + (parentId + 1));
    }

    @Test
    @Transactional
    public void getAllCategoriesByNameIsEqualToSomething() throws Exception {
        // Initialize the database
        //        I18n name = I18nResourceIntTest.createEntity(em);
        //        em.persist(name);
        //        em.flush();
        //        category.addName(name);
        //        categoryRepository.saveAndFlush(category);
        //        Long nameId = name.getId();

        // Get all the categoryList where name equals to nameId
        //        defaultCategoryShouldBeFound("nameId.equals=" + nameId);

        // Get all the categoryList where name equals to nameId + 1
        //        defaultCategoryShouldNotBeFound("nameId.equals=" + (nameId + 1));
    }

    /**
     * Executes the search, and checks that the default entity is returned
     */
    private void defaultCategoryShouldBeFound(String filter) throws Exception {
        restCategoryMockMvc
            .perform(get("/api/categories?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(category.getId().intValue())))
            //            .andExpect(jsonPath("$.[*].shortName").value(hasItem(DEFAULT_SHORT_NAME.toString()))) //TODO - bcz, unique
            .andExpect(jsonPath("$.[*].thumbnail").value(hasItem(DEFAULT_THUMBNAIL.toString())))
            .andExpect(jsonPath("$.[*].thumbnailFileContentType").value(hasItem(DEFAULT_THUMBNAIL_FILE_CONTENT_TYPE)))
            .andExpect(jsonPath("$.[*].thumbnailFile").value(hasItem(Base64Utils.encodeToString(DEFAULT_THUMBNAIL_FILE))))
            .andExpect(jsonPath("$.[*].logo").value(hasItem(DEFAULT_LOGO.toString())))
            .andExpect(jsonPath("$.[*].logoFileContentType").value(hasItem(DEFAULT_LOGO_FILE_CONTENT_TYPE)))
            .andExpect(jsonPath("$.[*].logoFile").value(hasItem(Base64Utils.encodeToString(DEFAULT_LOGO_FILE))))
            .andExpect(jsonPath("$.[*].ord").value(hasItem(DEFAULT_ORD)))
            .andExpect(jsonPath("$.[*].active").value(hasItem(DEFAULT_ACTIVE.booleanValue())));

        // Check, that the count call also returns 1
        restCategoryMockMvc
            .perform(get("/api/categories/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(content().string("1"));
    }

    /**
     * Executes the search, and checks that the default entity is not returned
     */
    private void defaultCategoryShouldNotBeFound(String filter) throws Exception {
        restCategoryMockMvc
            .perform(get("/api/categories?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty());

        // Check, that the count call also returns 0
        restCategoryMockMvc
            .perform(get("/api/categories/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(content().string("0"));
    }

    @Test
    @Transactional
    public void getNonExistingCategory() throws Exception {
        // Get the category
        restCategoryMockMvc.perform(get("/api/categories/{id}", Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updateCategory() throws Exception {
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
                put("/api/categories").contentType(TestUtil.APPLICATION_JSON_UTF8).content(TestUtil.convertObjectToJsonBytes(categoryDTO))
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
        assertThat(testCategory.getLogoFile()).isEqualTo(UPDATED_LOGO_FILE);
        assertThat(testCategory.getLogoFileContentType()).isEqualTo(UPDATED_LOGO_FILE_CONTENT_TYPE);
        assertThat(testCategory.getOrd()).isEqualTo(UPDATED_ORD);
        assertThat(testCategory.isActive()).isEqualTo(UPDATED_ACTIVE);
    }

    @Test
    @Transactional
    public void updateNonExistingCategory() throws Exception {
        int databaseSizeBeforeUpdate = categoryRepository.findAll().size();

        // Create the Category
        CategoryDTO categoryDTO = categoryMapper.toDto(category);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restCategoryMockMvc
            .perform(
                put("/api/categories").contentType(TestUtil.APPLICATION_JSON_UTF8).content(TestUtil.convertObjectToJsonBytes(categoryDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Category in the database
        List<Category> categoryList = categoryRepository.findAll();
        assertThat(categoryList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    public void deleteCategory() throws Exception {
        // Initialize the database
        categoryRepository.saveAndFlush(category);

        int databaseSizeBeforeDelete = categoryRepository.findAll().size();

        // Get the category
        restCategoryMockMvc
            .perform(delete("/api/categories/{id}", category.getId()).accept(TestUtil.APPLICATION_JSON_UTF8))
            .andExpect(status().isOk());

        // Validate the database is empty
        List<Category> categoryList = categoryRepository.findAll();
        assertThat(categoryList).hasSize(databaseSizeBeforeDelete - 1);
    }

    @Test
    @Transactional
    public void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Category.class);
        Category category1 = new Category();
        category1.setId(1L);
        Category category2 = new Category();
        category2.setId(category1.getId());
        assertThat(category1).isEqualTo(category2);
        category2.setId(2L);
        assertThat(category1).isNotEqualTo(category2);
        category1.setId(null);
        assertThat(category1).isNotEqualTo(category2);
    }

    @Test
    @Transactional
    public void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(CategoryDTO.class);
        CategoryDTO categoryDTO1 = new CategoryDTO();
        categoryDTO1.setId(1L);
        CategoryDTO categoryDTO2 = new CategoryDTO();
        assertThat(categoryDTO1).isNotEqualTo(categoryDTO2);
        categoryDTO2.setId(categoryDTO1.getId());
        assertThat(categoryDTO1).isEqualTo(categoryDTO2);
        categoryDTO2.setId(2L);
        assertThat(categoryDTO1).isNotEqualTo(categoryDTO2);
        categoryDTO1.setId(null);
        assertThat(categoryDTO1).isNotEqualTo(categoryDTO2);
    }
    //    @Test
    //    @Transactional
    //    public void testEntityFromId() {
    //        assertThat(categoryMapper.fromId(42L).getId()).isEqualTo(42);
    //        assertThat(categoryMapper.fromId(null)).isNull();
    //    }
}
