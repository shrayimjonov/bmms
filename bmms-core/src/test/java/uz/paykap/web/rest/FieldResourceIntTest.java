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
import uz.devops.bmcore.enumeration.ControlType;
import uz.devops.bmcore.enumeration.FieldType;
import uz.devops.bmcore.enumeration.RequestType;
import uz.devops.bmcore.enumeration.Visibility;
import uz.paykap.PkbillmsApp;
import uz.paykap.IntegrationTest;
import uz.paykap.domain.*;
import uz.paykap.repository.FieldRepository;
import uz.paykap.repository.I18nRepository;
import uz.paykap.repository.RecipientRepository;
import uz.paykap.service.FieldQueryService;
import uz.paykap.service.FieldService;
import uz.paykap.service.dto.FieldDTO;
import uz.paykap.service.mapper.FieldMapper;
import uz.paykap.web.rest.errors.ExceptionTranslator;

/**
 * Test class for the FieldResource REST controller.
 *
 * @see FieldResource
 */
@RunWith(SpringRunner.class)
@IntegrationTest
public class FieldResourceIntTest {

    private static final String DEFAULT_NAME = "AAAAAAAAAA";
    private static final String UPDATED_NAME = "BBBBBBBBBB";

    private static final Long DEFAULT_EXT_ID = 1L;
    private static final Long UPDATED_EXT_ID = 2L;

    private static final FieldType DEFAULT_TYPE = FieldType.ACCOUNT;
    private static final FieldType UPDATED_TYPE = FieldType.AMOUNT;

    private static final ControlType DEFAULT_CONTROL_TYPE = ControlType.TEXT;
    private static final ControlType UPDATED_CONTROL_TYPE = ControlType.SELECT;

    private static final String DEFAULT_CONTENT = "AAAAAAAAAA";
    private static final String UPDATED_CONTENT = "BBBBBBBBBB";

    private static final String DEFAULT_VALIDATION = "AAAAAAAAAA";
    private static final String UPDATED_VALIDATION = "BBBBBBBBBB";

    private static final String DEFAULT_PLACEHOLDER = "AAAAAAAAAA";
    private static final String UPDATED_PLACEHOLDER = "BBBBBBBBBB";

    private static final String DEFAULT_MASK = "AAAAAAAAAA";
    private static final String UPDATED_MASK = "BBBBBBBBBB";

    private static final String DEFAULT_MASK_SYMBOL = "AA";
    private static final String UPDATED_MASK_SYMBOL = "BB";

    private static final Integer DEFAULT_LENGTH_MIN = 0;
    private static final Integer UPDATED_LENGTH_MIN = 1;

    private static final Integer DEFAULT_LENGTH_MAX = 0;
    private static final Integer UPDATED_LENGTH_MAX = 1;

    private static final String DEFAULT_REQUIRE_FIELD = "AAAAAAAAAA";
    private static final String UPDATED_REQUIRE_FIELD = "BBBBBBBBBB";

    private static final String DEFAULT_FILTER_BY = "AAAAAAAAAA";
    private static final String UPDATED_FILTER_BY = "BBBBBBBBBB";

    private static final Boolean DEFAULT_OPTIONAL = false;
    private static final Boolean UPDATED_OPTIONAL = true;

    private static final Boolean DEFAULT_OUTPUT = false;
    private static final Boolean UPDATED_OUTPUT = true;

    private static final Visibility DEFAULT_VISIBILITY = Visibility.HIDDEN;
    private static final Visibility UPDATED_VISIBILITY = Visibility.PRINTER;

    private static final Integer DEFAULT_ORD = 1;
    private static final Integer UPDATED_ORD = 2;

    private static final RequestType DEFAULT_REQUEST_TYPE = RequestType.CHECK;
    private static final RequestType UPDATED_REQUEST_TYPE = RequestType.PAY;

    @Autowired
    private FieldRepository fieldRepository;

    @Autowired
    private I18nRepository i18nRepository;

    @Mock
    private FieldRepository fieldRepositoryMock;

    @Autowired
    private FieldMapper fieldMapper;

    @Mock
    private FieldService fieldServiceMock;

    @Autowired
    private FieldService fieldService;

    @Autowired
    private FieldQueryService fieldQueryService;

    @Autowired
    private MappingJackson2HttpMessageConverter jacksonMessageConverter;

    @Autowired
    private PageableHandlerMethodArgumentResolver pageableArgumentResolver;

    @Autowired
    private ExceptionTranslator exceptionTranslator;

    @Autowired
    private RecipientRepository recipientRepository;

    @Autowired
    private EntityManager em;

    @Autowired
    private Validator validator;

    private MockMvc restFieldMockMvc;

    private Field field;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        final FieldResource fieldResource = new FieldResource(fieldService, fieldQueryService, recipientRepository, fieldRepository);
        this.restFieldMockMvc =
            MockMvcBuilders
                .standaloneSetup(fieldResource)
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
    public static Field createEntity(EntityManager em) {
        Field field = new Field()
            .name(DEFAULT_NAME)
            .extId(DEFAULT_EXT_ID)
            .type(DEFAULT_TYPE)
            .controlType(DEFAULT_CONTROL_TYPE)
            .content(DEFAULT_CONTENT)
            .validation(DEFAULT_VALIDATION)
            .placeholder(DEFAULT_PLACEHOLDER)
            .mask(DEFAULT_MASK)
            .maskSymbol(DEFAULT_MASK_SYMBOL)
            .lengthMin(DEFAULT_LENGTH_MIN)
            .lengthMax(DEFAULT_LENGTH_MAX)
            .requireField(DEFAULT_REQUIRE_FIELD)
            .filterBy(DEFAULT_FILTER_BY)
            .optional(DEFAULT_OPTIONAL)
            .output(DEFAULT_OUTPUT)
            .visibility(DEFAULT_VISIBILITY)
            .ord(DEFAULT_ORD)
            .requestType(DEFAULT_REQUEST_TYPE);
        // Add required entity
        Recipient recipient = RecipientResourceIntTest.createEntity(em);
        em.persist(recipient);
        em.flush();
        field.setRecipient(recipient);
        // Add required entity
        I18n i18n = I18nResourceIntTest.createEntity(em);
        //        em.persist(i18n);
        //        em.flush();
        field.getTitles().add(i18n);
        return field;
    }

    public static Field createEntity() {
        Field field = new Field()
            .name(DEFAULT_NAME)
            .extId(DEFAULT_EXT_ID)
            .type(DEFAULT_TYPE)
            .controlType(DEFAULT_CONTROL_TYPE)
            .content(DEFAULT_CONTENT)
            .validation(DEFAULT_VALIDATION)
            .placeholder(DEFAULT_PLACEHOLDER)
            .mask(DEFAULT_MASK)
            .maskSymbol(DEFAULT_MASK_SYMBOL)
            .lengthMin(DEFAULT_LENGTH_MIN)
            .lengthMax(DEFAULT_LENGTH_MAX)
            .requireField(DEFAULT_REQUIRE_FIELD)
            .filterBy(DEFAULT_FILTER_BY)
            .optional(DEFAULT_OPTIONAL)
            .output(DEFAULT_OUTPUT)
            .visibility(DEFAULT_VISIBILITY)
            .ord(DEFAULT_ORD)
            .requestType(DEFAULT_REQUEST_TYPE);
        return field;
    }

    @Before
    public void initTest() {
        field = createEntity(em);
    }

    @Test
    @Transactional
    public void createField() throws Exception {
        int databaseSizeBeforeCreate = fieldRepository.findAll().size();

        // Create the Field
        FieldDTO fieldDTO = fieldMapper.toDto(field);
        restFieldMockMvc
            .perform(post("/api/fields").contentType(TestUtil.APPLICATION_JSON_UTF8).content(TestUtil.convertObjectToJsonBytes(fieldDTO)))
            .andExpect(status().isCreated());

        // Validate the Field in the database
        List<Field> fieldList = fieldRepository.findAll();
        assertThat(fieldList).hasSize(databaseSizeBeforeCreate + 1);
        Field testField = fieldList.get(fieldList.size() - 1);
        assertThat(testField.getName()).isEqualTo(DEFAULT_NAME);
        assertThat(testField.getExtId()).isEqualTo(DEFAULT_EXT_ID);
        assertThat(testField.getType()).isEqualTo(DEFAULT_TYPE);
        assertThat(testField.getControlType()).isEqualTo(DEFAULT_CONTROL_TYPE);
        assertThat(testField.getContent()).isEqualTo(DEFAULT_CONTENT);
        assertThat(testField.getValidation()).isEqualTo(DEFAULT_VALIDATION);
        assertThat(testField.getPlaceholder()).isEqualTo(DEFAULT_PLACEHOLDER);
        assertThat(testField.getMask()).isEqualTo(DEFAULT_MASK);
        assertThat(testField.getMaskSymbol()).isEqualTo(DEFAULT_MASK_SYMBOL);
        assertThat(testField.getLengthMin()).isEqualTo(DEFAULT_LENGTH_MIN);
        assertThat(testField.getLengthMax()).isEqualTo(DEFAULT_LENGTH_MAX);
        assertThat(testField.getRequireField()).isEqualTo(DEFAULT_REQUIRE_FIELD);
        assertThat(testField.getFilterBy()).isEqualTo(DEFAULT_FILTER_BY);
        assertThat(testField.isOptional()).isEqualTo(DEFAULT_OPTIONAL);
        assertThat(testField.isOutput()).isEqualTo(DEFAULT_OUTPUT);
        assertThat(testField.getVisibility()).isEqualTo(DEFAULT_VISIBILITY);
        assertThat(testField.getOrd()).isEqualTo(DEFAULT_ORD);
        assertThat(testField.getRequestType()).isEqualTo(DEFAULT_REQUEST_TYPE);
    }

    @Test
    @Transactional
    public void createFieldWithExistingId() throws Exception {
        int databaseSizeBeforeCreate = fieldRepository.findAll().size();

        // Create the Field with an existing ID
        field.setId(1L);
        FieldDTO fieldDTO = fieldMapper.toDto(field);

        // An entity with an existing ID cannot be created, so this API call must fail
        restFieldMockMvc
            .perform(post("/api/fields").contentType(TestUtil.APPLICATION_JSON_UTF8).content(TestUtil.convertObjectToJsonBytes(fieldDTO)))
            .andExpect(status().isBadRequest());

        // Validate the Field in the database
        List<Field> fieldList = fieldRepository.findAll();
        assertThat(fieldList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    public void checkNameIsRequired() throws Exception {
        int databaseSizeBeforeTest = fieldRepository.findAll().size();
        // set the field null
        field.setName(null);

        // Create the Field, which fails.
        FieldDTO fieldDTO = fieldMapper.toDto(field);

        restFieldMockMvc
            .perform(post("/api/fields").contentType(TestUtil.APPLICATION_JSON_UTF8).content(TestUtil.convertObjectToJsonBytes(fieldDTO)))
            .andExpect(status().isBadRequest());

        List<Field> fieldList = fieldRepository.findAll();
        assertThat(fieldList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkExtIdIsRequired() throws Exception {
        int databaseSizeBeforeTest = fieldRepository.findAll().size();
        // set the field null
        field.setExtId(null);

        // Create the Field, which fails.
        FieldDTO fieldDTO = fieldMapper.toDto(field);

        restFieldMockMvc
            .perform(post("/api/fields").contentType(TestUtil.APPLICATION_JSON_UTF8).content(TestUtil.convertObjectToJsonBytes(fieldDTO)))
            .andExpect(status().isBadRequest());

        List<Field> fieldList = fieldRepository.findAll();
        assertThat(fieldList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkTypeIsRequired() throws Exception {
        int databaseSizeBeforeTest = fieldRepository.findAll().size();
        // set the field null
        field.setType(null);

        // Create the Field, which fails.
        FieldDTO fieldDTO = fieldMapper.toDto(field);

        restFieldMockMvc
            .perform(post("/api/fields").contentType(TestUtil.APPLICATION_JSON_UTF8).content(TestUtil.convertObjectToJsonBytes(fieldDTO)))
            .andExpect(status().isBadRequest());

        List<Field> fieldList = fieldRepository.findAll();
        assertThat(fieldList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkControlTypeIsRequired() throws Exception {
        int databaseSizeBeforeTest = fieldRepository.findAll().size();
        // set the field null
        field.setControlType(null);

        // Create the Field, which fails.
        FieldDTO fieldDTO = fieldMapper.toDto(field);

        restFieldMockMvc
            .perform(post("/api/fields").contentType(TestUtil.APPLICATION_JSON_UTF8).content(TestUtil.convertObjectToJsonBytes(fieldDTO)))
            .andExpect(status().isBadRequest());

        List<Field> fieldList = fieldRepository.findAll();
        assertThat(fieldList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void getAllFields() throws Exception {
        // Initialize the database
        fieldRepository.saveAndFlush(field);

        // Get all the fieldList
        restFieldMockMvc
            .perform(get("/api/fields?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(field.getId().intValue())))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME.toString())))
            .andExpect(jsonPath("$.[*].extId").value(hasItem(DEFAULT_EXT_ID.intValue())))
            .andExpect(jsonPath("$.[*].type").value(hasItem(DEFAULT_TYPE.toString())))
            .andExpect(jsonPath("$.[*].controlType").value(hasItem(DEFAULT_CONTROL_TYPE.toString())))
            .andExpect(jsonPath("$.[*].content").value(hasItem(DEFAULT_CONTENT.toString())))
            .andExpect(jsonPath("$.[*].validation").value(hasItem(DEFAULT_VALIDATION.toString())))
            .andExpect(jsonPath("$.[*].placeholder").value(hasItem(DEFAULT_PLACEHOLDER.toString())))
            .andExpect(jsonPath("$.[*].mask").value(hasItem(DEFAULT_MASK.toString())))
            .andExpect(jsonPath("$.[*].maskSymbol").value(hasItem(DEFAULT_MASK_SYMBOL.toString())))
            .andExpect(jsonPath("$.[*].lengthMin").value(hasItem(DEFAULT_LENGTH_MIN)))
            .andExpect(jsonPath("$.[*].lengthMax").value(hasItem(DEFAULT_LENGTH_MAX)))
            .andExpect(jsonPath("$.[*].requireField").value(hasItem(DEFAULT_REQUIRE_FIELD.toString())))
            .andExpect(jsonPath("$.[*].filterBy").value(hasItem(DEFAULT_FILTER_BY.toString())))
            .andExpect(jsonPath("$.[*].optional").value(hasItem(DEFAULT_OPTIONAL.booleanValue())))
            .andExpect(jsonPath("$.[*].output").value(hasItem(DEFAULT_OUTPUT.booleanValue())))
            .andExpect(jsonPath("$.[*].visibility").value(hasItem(DEFAULT_VISIBILITY.toString())))
            .andExpect(jsonPath("$.[*].ord").value(hasItem(DEFAULT_ORD)))
            .andExpect(jsonPath("$.[*].requestType").value(hasItem(DEFAULT_REQUEST_TYPE.toString())));
    }

    @SuppressWarnings({ "unchecked" })
    public void getAllFieldsWithEagerRelationshipsIsEnabled() throws Exception {
        FieldResource fieldResource = new FieldResource(fieldServiceMock, fieldQueryService, recipientRepository, fieldRepository);
        when(fieldServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        MockMvc restFieldMockMvc = MockMvcBuilders
            .standaloneSetup(fieldResource)
            .setCustomArgumentResolvers(pageableArgumentResolver)
            .setControllerAdvice(exceptionTranslator)
            .setConversionService(createFormattingConversionService())
            .setMessageConverters(jacksonMessageConverter)
            .build();

        restFieldMockMvc.perform(get("/api/fields?eagerload=true")).andExpect(status().isOk());

        verify(fieldServiceMock, times(1)).findAllWithEagerRelationships(any());
    }

    @SuppressWarnings({ "unchecked" })
    public void getAllFieldsWithEagerRelationshipsIsNotEnabled() throws Exception {
        FieldResource fieldResource = new FieldResource(fieldServiceMock, fieldQueryService, recipientRepository, fieldRepository);
        when(fieldServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));
        MockMvc restFieldMockMvc = MockMvcBuilders
            .standaloneSetup(fieldResource)
            .setCustomArgumentResolvers(pageableArgumentResolver)
            .setControllerAdvice(exceptionTranslator)
            .setConversionService(createFormattingConversionService())
            .setMessageConverters(jacksonMessageConverter)
            .build();

        restFieldMockMvc.perform(get("/api/fields?eagerload=true")).andExpect(status().isOk());

        verify(fieldServiceMock, times(1)).findAllWithEagerRelationships(any());
    }

    @Test
    @Transactional
    public void getField() throws Exception {
        // Initialize the database
        fieldRepository.saveAndFlush(field);

        // Get the field
        restFieldMockMvc
            .perform(get("/api/fields/{id}", field.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.id").value(field.getId().intValue()))
            .andExpect(jsonPath("$.name").value(DEFAULT_NAME.toString()))
            .andExpect(jsonPath("$.extId").value(DEFAULT_EXT_ID.intValue()))
            .andExpect(jsonPath("$.type").value(DEFAULT_TYPE.toString()))
            .andExpect(jsonPath("$.controlType").value(DEFAULT_CONTROL_TYPE.toString()))
            .andExpect(jsonPath("$.content").value(DEFAULT_CONTENT.toString()))
            .andExpect(jsonPath("$.validation").value(DEFAULT_VALIDATION.toString()))
            .andExpect(jsonPath("$.placeholder").value(DEFAULT_PLACEHOLDER.toString()))
            .andExpect(jsonPath("$.mask").value(DEFAULT_MASK.toString()))
            .andExpect(jsonPath("$.maskSymbol").value(DEFAULT_MASK_SYMBOL.toString()))
            .andExpect(jsonPath("$.lengthMin").value(DEFAULT_LENGTH_MIN))
            .andExpect(jsonPath("$.lengthMax").value(DEFAULT_LENGTH_MAX))
            .andExpect(jsonPath("$.requireField").value(DEFAULT_REQUIRE_FIELD.toString()))
            .andExpect(jsonPath("$.filterBy").value(DEFAULT_FILTER_BY.toString()))
            .andExpect(jsonPath("$.optional").value(DEFAULT_OPTIONAL.booleanValue()))
            .andExpect(jsonPath("$.output").value(DEFAULT_OUTPUT.booleanValue()))
            .andExpect(jsonPath("$.visibility").value(DEFAULT_VISIBILITY.toString()))
            .andExpect(jsonPath("$.ord").value(DEFAULT_ORD))
            .andExpect(jsonPath("$.requestType").value(DEFAULT_REQUEST_TYPE.toString()));
    }

    @Test
    @Transactional
    public void getAllFieldsByNameIsEqualToSomething() throws Exception {
        // Initialize the database
        fieldRepository.saveAndFlush(field);

        // Get all the fieldList where name equals to DEFAULT_NAME
        defaultFieldShouldBeFound("name.equals=" + DEFAULT_NAME);

        // Get all the fieldList where name equals to UPDATED_NAME
        defaultFieldShouldNotBeFound("name.equals=" + UPDATED_NAME);
    }

    @Test
    @Transactional
    public void getAllFieldsByNameIsInShouldWork() throws Exception {
        // Initialize the database
        fieldRepository.saveAndFlush(field);

        // Get all the fieldList where name in DEFAULT_NAME or UPDATED_NAME
        defaultFieldShouldBeFound("name.in=" + DEFAULT_NAME + "," + UPDATED_NAME);

        // Get all the fieldList where name equals to UPDATED_NAME
        defaultFieldShouldNotBeFound("name.in=" + UPDATED_NAME);
    }

    @Test
    @Transactional
    public void getAllFieldsByNameIsNullOrNotNull() throws Exception {
        // Initialize the database
        fieldRepository.saveAndFlush(field);

        // Get all the fieldList where name is not null
        defaultFieldShouldBeFound("name.specified=true");

        // Get all the fieldList where name is null
        defaultFieldShouldNotBeFound("name.specified=false");
    }

    @Test
    @Transactional
    public void getAllFieldsByExtIdIsEqualToSomething() throws Exception {
        // Initialize the database
        fieldRepository.saveAndFlush(field);

        // Get all the fieldList where extId equals to DEFAULT_EXT_ID
        defaultFieldShouldBeFound("extId.equals=" + DEFAULT_EXT_ID);

        // Get all the fieldList where extId equals to UPDATED_EXT_ID
        defaultFieldShouldNotBeFound("extId.equals=" + UPDATED_EXT_ID);
    }

    @Test
    @Transactional
    public void getAllFieldsByExtIdIsInShouldWork() throws Exception {
        // Initialize the database
        fieldRepository.saveAndFlush(field);

        // Get all the fieldList where extId in DEFAULT_EXT_ID or UPDATED_EXT_ID
        defaultFieldShouldBeFound("extId.in=" + DEFAULT_EXT_ID + "," + UPDATED_EXT_ID);

        // Get all the fieldList where extId equals to UPDATED_EXT_ID
        defaultFieldShouldNotBeFound("extId.in=" + UPDATED_EXT_ID);
    }

    @Test
    @Transactional
    public void getAllFieldsByExtIdIsNullOrNotNull() throws Exception {
        // Initialize the database
        fieldRepository.saveAndFlush(field);

        // Get all the fieldList where extId is not null
        defaultFieldShouldBeFound("extId.specified=true");

        // Get all the fieldList where extId is null
        defaultFieldShouldNotBeFound("extId.specified=false");
    }

    @Test
    @Transactional
    public void getAllFieldsByExtIdIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        fieldRepository.saveAndFlush(field);

        // Get all the fieldList where extId greater than or equals to DEFAULT_EXT_ID
        defaultFieldShouldBeFound("extId.greaterOrEqualThan=" + DEFAULT_EXT_ID);

        // Get all the fieldList where extId greater than or equals to UPDATED_EXT_ID
        defaultFieldShouldNotBeFound("extId.greaterOrEqualThan=" + UPDATED_EXT_ID);
    }

    @Test
    @Transactional
    public void getAllFieldsByExtIdIsLessThanSomething() throws Exception {
        // Initialize the database
        fieldRepository.saveAndFlush(field);

        // Get all the fieldList where extId less than or equals to DEFAULT_EXT_ID
        defaultFieldShouldNotBeFound("extId.lessThan=" + DEFAULT_EXT_ID);

        // Get all the fieldList where extId less than or equals to UPDATED_EXT_ID
        defaultFieldShouldBeFound("extId.lessThan=" + UPDATED_EXT_ID);
    }

    @Test
    @Transactional
    public void getAllFieldsByTypeIsEqualToSomething() throws Exception {
        // Initialize the database
        fieldRepository.saveAndFlush(field);

        // Get all the fieldList where type equals to DEFAULT_TYPE
        defaultFieldShouldBeFound("type.equals=" + DEFAULT_TYPE);

        // Get all the fieldList where type equals to UPDATED_TYPE
        defaultFieldShouldNotBeFound("type.equals=" + UPDATED_TYPE);
    }

    @Test
    @Transactional
    public void getAllFieldsByTypeIsInShouldWork() throws Exception {
        // Initialize the database
        fieldRepository.saveAndFlush(field);

        // Get all the fieldList where type in DEFAULT_TYPE or UPDATED_TYPE
        defaultFieldShouldBeFound("type.in=" + DEFAULT_TYPE + "," + UPDATED_TYPE);

        // Get all the fieldList where type equals to UPDATED_TYPE
        defaultFieldShouldNotBeFound("type.in=" + UPDATED_TYPE);
    }

    @Test
    @Transactional
    public void getAllFieldsByTypeIsNullOrNotNull() throws Exception {
        // Initialize the database
        fieldRepository.saveAndFlush(field);

        // Get all the fieldList where type is not null
        defaultFieldShouldBeFound("type.specified=true");

        // Get all the fieldList where type is null
        defaultFieldShouldNotBeFound("type.specified=false");
    }

    @Test
    @Transactional
    public void getAllFieldsByControlTypeIsEqualToSomething() throws Exception {
        // Initialize the database
        fieldRepository.saveAndFlush(field);

        // Get all the fieldList where controlType equals to DEFAULT_CONTROL_TYPE
        defaultFieldShouldBeFound("controlType.equals=" + DEFAULT_CONTROL_TYPE);

        // Get all the fieldList where controlType equals to UPDATED_CONTROL_TYPE
        defaultFieldShouldNotBeFound("controlType.equals=" + UPDATED_CONTROL_TYPE);
    }

    @Test
    @Transactional
    public void getAllFieldsByControlTypeIsInShouldWork() throws Exception {
        // Initialize the database
        fieldRepository.saveAndFlush(field);

        // Get all the fieldList where controlType in DEFAULT_CONTROL_TYPE or UPDATED_CONTROL_TYPE
        defaultFieldShouldBeFound("controlType.in=" + DEFAULT_CONTROL_TYPE + "," + UPDATED_CONTROL_TYPE);

        // Get all the fieldList where controlType equals to UPDATED_CONTROL_TYPE
        defaultFieldShouldNotBeFound("controlType.in=" + UPDATED_CONTROL_TYPE);
    }

    @Test
    @Transactional
    public void getAllFieldsByControlTypeIsNullOrNotNull() throws Exception {
        // Initialize the database
        fieldRepository.saveAndFlush(field);

        // Get all the fieldList where controlType is not null
        defaultFieldShouldBeFound("controlType.specified=true");

        // Get all the fieldList where controlType is null
        defaultFieldShouldNotBeFound("controlType.specified=false");
    }

    @Test
    @Transactional
    public void getAllFieldsByContentIsEqualToSomething() throws Exception {
        // Initialize the database
        fieldRepository.saveAndFlush(field);

        // Get all the fieldList where content equals to DEFAULT_CONTENT
        defaultFieldShouldBeFound("content.equals=" + DEFAULT_CONTENT);

        // Get all the fieldList where content equals to UPDATED_CONTENT
        defaultFieldShouldNotBeFound("content.equals=" + UPDATED_CONTENT);
    }

    @Test
    @Transactional
    public void getAllFieldsByContentIsInShouldWork() throws Exception {
        // Initialize the database
        fieldRepository.saveAndFlush(field);

        // Get all the fieldList where content in DEFAULT_CONTENT or UPDATED_CONTENT
        defaultFieldShouldBeFound("content.in=" + DEFAULT_CONTENT + "," + UPDATED_CONTENT);

        // Get all the fieldList where content equals to UPDATED_CONTENT
        defaultFieldShouldNotBeFound("content.in=" + UPDATED_CONTENT);
    }

    @Test
    @Transactional
    public void getAllFieldsByContentIsNullOrNotNull() throws Exception {
        // Initialize the database
        fieldRepository.saveAndFlush(field);

        // Get all the fieldList where content is not null
        defaultFieldShouldBeFound("content.specified=true");

        // Get all the fieldList where content is null
        defaultFieldShouldNotBeFound("content.specified=false");
    }

    @Test
    @Transactional
    public void getAllFieldsByValidationIsEqualToSomething() throws Exception {
        // Initialize the database
        fieldRepository.saveAndFlush(field);

        // Get all the fieldList where validation equals to DEFAULT_VALIDATION
        defaultFieldShouldBeFound("validation.equals=" + DEFAULT_VALIDATION);

        // Get all the fieldList where validation equals to UPDATED_VALIDATION
        defaultFieldShouldNotBeFound("validation.equals=" + UPDATED_VALIDATION);
    }

    @Test
    @Transactional
    public void getAllFieldsByValidationIsInShouldWork() throws Exception {
        // Initialize the database
        fieldRepository.saveAndFlush(field);

        // Get all the fieldList where validation in DEFAULT_VALIDATION or UPDATED_VALIDATION
        defaultFieldShouldBeFound("validation.in=" + DEFAULT_VALIDATION + "," + UPDATED_VALIDATION);

        // Get all the fieldList where validation equals to UPDATED_VALIDATION
        defaultFieldShouldNotBeFound("validation.in=" + UPDATED_VALIDATION);
    }

    @Test
    @Transactional
    public void getAllFieldsByValidationIsNullOrNotNull() throws Exception {
        // Initialize the database
        fieldRepository.saveAndFlush(field);

        // Get all the fieldList where validation is not null
        defaultFieldShouldBeFound("validation.specified=true");

        // Get all the fieldList where validation is null
        defaultFieldShouldNotBeFound("validation.specified=false");
    }

    @Test
    @Transactional
    public void getAllFieldsByPlaceholderIsEqualToSomething() throws Exception {
        // Initialize the database
        fieldRepository.saveAndFlush(field);

        // Get all the fieldList where placeholder equals to DEFAULT_PLACEHOLDER
        defaultFieldShouldBeFound("placeholder.equals=" + DEFAULT_PLACEHOLDER);

        // Get all the fieldList where placeholder equals to UPDATED_PLACEHOLDER
        defaultFieldShouldNotBeFound("placeholder.equals=" + UPDATED_PLACEHOLDER);
    }

    @Test
    @Transactional
    public void getAllFieldsByPlaceholderIsInShouldWork() throws Exception {
        // Initialize the database
        fieldRepository.saveAndFlush(field);

        // Get all the fieldList where placeholder in DEFAULT_PLACEHOLDER or UPDATED_PLACEHOLDER
        defaultFieldShouldBeFound("placeholder.in=" + DEFAULT_PLACEHOLDER + "," + UPDATED_PLACEHOLDER);

        // Get all the fieldList where placeholder equals to UPDATED_PLACEHOLDER
        defaultFieldShouldNotBeFound("placeholder.in=" + UPDATED_PLACEHOLDER);
    }

    @Test
    @Transactional
    public void getAllFieldsByPlaceholderIsNullOrNotNull() throws Exception {
        // Initialize the database
        fieldRepository.saveAndFlush(field);

        // Get all the fieldList where placeholder is not null
        defaultFieldShouldBeFound("placeholder.specified=true");

        // Get all the fieldList where placeholder is null
        defaultFieldShouldNotBeFound("placeholder.specified=false");
    }

    @Test
    @Transactional
    public void getAllFieldsByMaskIsEqualToSomething() throws Exception {
        // Initialize the database
        fieldRepository.saveAndFlush(field);

        // Get all the fieldList where mask equals to DEFAULT_MASK
        defaultFieldShouldBeFound("mask.equals=" + DEFAULT_MASK);

        // Get all the fieldList where mask equals to UPDATED_MASK
        defaultFieldShouldNotBeFound("mask.equals=" + UPDATED_MASK);
    }

    @Test
    @Transactional
    public void getAllFieldsByMaskIsInShouldWork() throws Exception {
        // Initialize the database
        fieldRepository.saveAndFlush(field);

        // Get all the fieldList where mask in DEFAULT_MASK or UPDATED_MASK
        defaultFieldShouldBeFound("mask.in=" + DEFAULT_MASK + "," + UPDATED_MASK);

        // Get all the fieldList where mask equals to UPDATED_MASK
        defaultFieldShouldNotBeFound("mask.in=" + UPDATED_MASK);
    }

    @Test
    @Transactional
    public void getAllFieldsByMaskIsNullOrNotNull() throws Exception {
        // Initialize the database
        fieldRepository.saveAndFlush(field);

        // Get all the fieldList where mask is not null
        defaultFieldShouldBeFound("mask.specified=true");

        // Get all the fieldList where mask is null
        defaultFieldShouldNotBeFound("mask.specified=false");
    }

    @Test
    @Transactional
    public void getAllFieldsByMaskSymbolIsEqualToSomething() throws Exception {
        // Initialize the database
        fieldRepository.saveAndFlush(field);

        // Get all the fieldList where maskSymbol equals to DEFAULT_MASK_SYMBOL
        defaultFieldShouldBeFound("maskSymbol.equals=" + DEFAULT_MASK_SYMBOL);

        // Get all the fieldList where maskSymbol equals to UPDATED_MASK_SYMBOL
        defaultFieldShouldNotBeFound("maskSymbol.equals=" + UPDATED_MASK_SYMBOL);
    }

    @Test
    @Transactional
    public void getAllFieldsByMaskSymbolIsInShouldWork() throws Exception {
        // Initialize the database
        fieldRepository.saveAndFlush(field);

        // Get all the fieldList where maskSymbol in DEFAULT_MASK_SYMBOL or UPDATED_MASK_SYMBOL
        defaultFieldShouldBeFound("maskSymbol.in=" + DEFAULT_MASK_SYMBOL + "," + UPDATED_MASK_SYMBOL);

        // Get all the fieldList where maskSymbol equals to UPDATED_MASK_SYMBOL
        defaultFieldShouldNotBeFound("maskSymbol.in=" + UPDATED_MASK_SYMBOL);
    }

    @Test
    @Transactional
    public void getAllFieldsByMaskSymbolIsNullOrNotNull() throws Exception {
        // Initialize the database
        fieldRepository.saveAndFlush(field);

        // Get all the fieldList where maskSymbol is not null
        defaultFieldShouldBeFound("maskSymbol.specified=true");

        // Get all the fieldList where maskSymbol is null
        defaultFieldShouldNotBeFound("maskSymbol.specified=false");
    }

    @Test
    @Transactional
    public void getAllFieldsByLengthMinIsEqualToSomething() throws Exception {
        // Initialize the database
        fieldRepository.saveAndFlush(field);

        // Get all the fieldList where lengthMin equals to DEFAULT_LENGTH_MIN
        defaultFieldShouldBeFound("lengthMin.equals=" + DEFAULT_LENGTH_MIN);

        // Get all the fieldList where lengthMin equals to UPDATED_LENGTH_MIN
        defaultFieldShouldNotBeFound("lengthMin.equals=" + UPDATED_LENGTH_MIN);
    }

    @Test
    @Transactional
    public void getAllFieldsByLengthMinIsInShouldWork() throws Exception {
        // Initialize the database
        fieldRepository.saveAndFlush(field);

        // Get all the fieldList where lengthMin in DEFAULT_LENGTH_MIN or UPDATED_LENGTH_MIN
        defaultFieldShouldBeFound("lengthMin.in=" + DEFAULT_LENGTH_MIN + "," + UPDATED_LENGTH_MIN);

        // Get all the fieldList where lengthMin equals to UPDATED_LENGTH_MIN
        defaultFieldShouldNotBeFound("lengthMin.in=" + UPDATED_LENGTH_MIN);
    }

    @Test
    @Transactional
    public void getAllFieldsByLengthMinIsNullOrNotNull() throws Exception {
        // Initialize the database
        fieldRepository.saveAndFlush(field);

        // Get all the fieldList where lengthMin is not null
        defaultFieldShouldBeFound("lengthMin.specified=true");

        // Get all the fieldList where lengthMin is null
        defaultFieldShouldNotBeFound("lengthMin.specified=false");
    }

    @Test
    @Transactional
    public void getAllFieldsByLengthMinIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        fieldRepository.saveAndFlush(field);

        // Get all the fieldList where lengthMin greater than or equals to DEFAULT_LENGTH_MIN
        defaultFieldShouldBeFound("lengthMin.greaterOrEqualThan=" + DEFAULT_LENGTH_MIN);

        // Get all the fieldList where lengthMin greater than or equals to (DEFAULT_LENGTH_MIN + 1)
        defaultFieldShouldNotBeFound("lengthMin.greaterOrEqualThan=" + (DEFAULT_LENGTH_MIN + 1));
    }

    @Test
    @Transactional
    public void getAllFieldsByLengthMinIsLessThanSomething() throws Exception {
        // Initialize the database
        fieldRepository.saveAndFlush(field);

        // Get all the fieldList where lengthMin less than or equals to DEFAULT_LENGTH_MIN
        defaultFieldShouldNotBeFound("lengthMin.lessThan=" + DEFAULT_LENGTH_MIN);

        // Get all the fieldList where lengthMin less than or equals to (DEFAULT_LENGTH_MIN + 1)
        defaultFieldShouldBeFound("lengthMin.lessThan=" + (DEFAULT_LENGTH_MIN + 1));
    }

    @Test
    @Transactional
    public void getAllFieldsByLengthMaxIsEqualToSomething() throws Exception {
        // Initialize the database
        fieldRepository.saveAndFlush(field);

        // Get all the fieldList where lengthMax equals to DEFAULT_LENGTH_MAX
        defaultFieldShouldBeFound("lengthMax.equals=" + DEFAULT_LENGTH_MAX);

        // Get all the fieldList where lengthMax equals to UPDATED_LENGTH_MAX
        defaultFieldShouldNotBeFound("lengthMax.equals=" + UPDATED_LENGTH_MAX);
    }

    @Test
    @Transactional
    public void getAllFieldsByLengthMaxIsInShouldWork() throws Exception {
        // Initialize the database
        fieldRepository.saveAndFlush(field);

        // Get all the fieldList where lengthMax in DEFAULT_LENGTH_MAX or UPDATED_LENGTH_MAX
        defaultFieldShouldBeFound("lengthMax.in=" + DEFAULT_LENGTH_MAX + "," + UPDATED_LENGTH_MAX);

        // Get all the fieldList where lengthMax equals to UPDATED_LENGTH_MAX
        defaultFieldShouldNotBeFound("lengthMax.in=" + UPDATED_LENGTH_MAX);
    }

    @Test
    @Transactional
    public void getAllFieldsByLengthMaxIsNullOrNotNull() throws Exception {
        // Initialize the database
        fieldRepository.saveAndFlush(field);

        // Get all the fieldList where lengthMax is not null
        defaultFieldShouldBeFound("lengthMax.specified=true");

        // Get all the fieldList where lengthMax is null
        defaultFieldShouldNotBeFound("lengthMax.specified=false");
    }

    @Test
    @Transactional
    public void getAllFieldsByLengthMaxIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        fieldRepository.saveAndFlush(field);

        // Get all the fieldList where lengthMax greater than or equals to DEFAULT_LENGTH_MAX
        defaultFieldShouldBeFound("lengthMax.greaterOrEqualThan=" + DEFAULT_LENGTH_MAX);

        // Get all the fieldList where lengthMax greater than or equals to (DEFAULT_LENGTH_MAX + 1)
        defaultFieldShouldNotBeFound("lengthMax.greaterOrEqualThan=" + (DEFAULT_LENGTH_MAX + 1));
    }

    @Test
    @Transactional
    public void getAllFieldsByLengthMaxIsLessThanSomething() throws Exception {
        // Initialize the database
        fieldRepository.saveAndFlush(field);

        // Get all the fieldList where lengthMax less than or equals to DEFAULT_LENGTH_MAX
        defaultFieldShouldNotBeFound("lengthMax.lessThan=" + DEFAULT_LENGTH_MAX);

        // Get all the fieldList where lengthMax less than or equals to (DEFAULT_LENGTH_MAX + 1)
        defaultFieldShouldBeFound("lengthMax.lessThan=" + (DEFAULT_LENGTH_MAX + 1));
    }

    @Test
    @Transactional
    public void getAllFieldsByRequireFieldIsEqualToSomething() throws Exception {
        // Initialize the database
        fieldRepository.saveAndFlush(field);

        // Get all the fieldList where requireField equals to DEFAULT_REQUIRE_FIELD
        defaultFieldShouldBeFound("requireField.equals=" + DEFAULT_REQUIRE_FIELD);

        // Get all the fieldList where requireField equals to UPDATED_REQUIRE_FIELD
        defaultFieldShouldNotBeFound("requireField.equals=" + UPDATED_REQUIRE_FIELD);
    }

    @Test
    @Transactional
    public void getAllFieldsByRequireFieldIsInShouldWork() throws Exception {
        // Initialize the database
        fieldRepository.saveAndFlush(field);

        // Get all the fieldList where requireField in DEFAULT_REQUIRE_FIELD or UPDATED_REQUIRE_FIELD
        defaultFieldShouldBeFound("requireField.in=" + DEFAULT_REQUIRE_FIELD + "," + UPDATED_REQUIRE_FIELD);

        // Get all the fieldList where requireField equals to UPDATED_REQUIRE_FIELD
        defaultFieldShouldNotBeFound("requireField.in=" + UPDATED_REQUIRE_FIELD);
    }

    @Test
    @Transactional
    public void getAllFieldsByRequireFieldIsNullOrNotNull() throws Exception {
        // Initialize the database
        fieldRepository.saveAndFlush(field);

        // Get all the fieldList where requireField is not null
        defaultFieldShouldBeFound("requireField.specified=true");

        // Get all the fieldList where requireField is null
        defaultFieldShouldNotBeFound("requireField.specified=false");
    }

    @Test
    @Transactional
    public void getAllFieldsByFilterByIsEqualToSomething() throws Exception {
        // Initialize the database
        fieldRepository.saveAndFlush(field);

        // Get all the fieldList where filterBy equals to DEFAULT_FILTER_BY
        defaultFieldShouldBeFound("filterBy.equals=" + DEFAULT_FILTER_BY);

        // Get all the fieldList where filterBy equals to UPDATED_FILTER_BY
        defaultFieldShouldNotBeFound("filterBy.equals=" + UPDATED_FILTER_BY);
    }

    @Test
    @Transactional
    public void getAllFieldsByFilterByIsInShouldWork() throws Exception {
        // Initialize the database
        fieldRepository.saveAndFlush(field);

        // Get all the fieldList where filterBy in DEFAULT_FILTER_BY or UPDATED_FILTER_BY
        defaultFieldShouldBeFound("filterBy.in=" + DEFAULT_FILTER_BY + "," + UPDATED_FILTER_BY);

        // Get all the fieldList where filterBy equals to UPDATED_FILTER_BY
        defaultFieldShouldNotBeFound("filterBy.in=" + UPDATED_FILTER_BY);
    }

    @Test
    @Transactional
    public void getAllFieldsByFilterByIsNullOrNotNull() throws Exception {
        // Initialize the database
        fieldRepository.saveAndFlush(field);

        // Get all the fieldList where filterBy is not null
        defaultFieldShouldBeFound("filterBy.specified=true");

        // Get all the fieldList where filterBy is null
        defaultFieldShouldNotBeFound("filterBy.specified=false");
    }

    @Test
    @Transactional
    public void getAllFieldsByOptionalIsEqualToSomething() throws Exception {
        // Initialize the database
        fieldRepository.saveAndFlush(field);

        // Get all the fieldList where optional equals to DEFAULT_OPTIONAL
        defaultFieldShouldBeFound("optional.equals=" + DEFAULT_OPTIONAL);

        // Get all the fieldList where optional equals to UPDATED_OPTIONAL
        defaultFieldShouldNotBeFound("optional.equals=" + UPDATED_OPTIONAL);
    }

    @Test
    @Transactional
    public void getAllFieldsByOptionalIsInShouldWork() throws Exception {
        // Initialize the database
        fieldRepository.saveAndFlush(field);

        // Get all the fieldList where optional in DEFAULT_OPTIONAL or UPDATED_OPTIONAL
        defaultFieldShouldBeFound("optional.in=" + DEFAULT_OPTIONAL + "," + UPDATED_OPTIONAL);

        // Get all the fieldList where optional equals to UPDATED_OPTIONAL
        defaultFieldShouldNotBeFound("optional.in=" + UPDATED_OPTIONAL);
    }

    @Test
    @Transactional
    public void getAllFieldsByOptionalIsNullOrNotNull() throws Exception {
        // Initialize the database
        fieldRepository.saveAndFlush(field);

        // Get all the fieldList where optional is not null
        defaultFieldShouldBeFound("optional.specified=true");

        // Get all the fieldList where optional is null
        defaultFieldShouldNotBeFound("optional.specified=false");
    }

    @Test
    @Transactional
    public void getAllFieldsByOutputIsEqualToSomething() throws Exception {
        // Initialize the database
        fieldRepository.saveAndFlush(field);

        // Get all the fieldList where output equals to DEFAULT_OUTPUT
        defaultFieldShouldBeFound("output.equals=" + DEFAULT_OUTPUT);

        // Get all the fieldList where output equals to UPDATED_OUTPUT
        defaultFieldShouldNotBeFound("output.equals=" + UPDATED_OUTPUT);
    }

    @Test
    @Transactional
    public void getAllFieldsByOutputIsInShouldWork() throws Exception {
        // Initialize the database
        fieldRepository.saveAndFlush(field);

        // Get all the fieldList where output in DEFAULT_OUTPUT or UPDATED_OUTPUT
        defaultFieldShouldBeFound("output.in=" + DEFAULT_OUTPUT + "," + UPDATED_OUTPUT);

        // Get all the fieldList where output equals to UPDATED_OUTPUT
        defaultFieldShouldNotBeFound("output.in=" + UPDATED_OUTPUT);
    }

    @Test
    @Transactional
    public void getAllFieldsByOutputIsNullOrNotNull() throws Exception {
        // Initialize the database
        fieldRepository.saveAndFlush(field);

        // Get all the fieldList where output is not null
        defaultFieldShouldBeFound("output.specified=true");

        // Get all the fieldList where output is null
        defaultFieldShouldNotBeFound("output.specified=false");
    }

    @Test
    @Transactional
    public void getAllFieldsByVisibilityIsEqualToSomething() throws Exception {
        // Initialize the database
        fieldRepository.saveAndFlush(field);

        // Get all the fieldList where visibility equals to DEFAULT_VISIBILITY
        defaultFieldShouldBeFound("visibility.equals=" + DEFAULT_VISIBILITY);

        // Get all the fieldList where visibility equals to UPDATED_VISIBILITY
        defaultFieldShouldNotBeFound("visibility.equals=" + UPDATED_VISIBILITY);
    }

    @Test
    @Transactional
    public void getAllFieldsByVisibilityIsInShouldWork() throws Exception {
        // Initialize the database
        fieldRepository.saveAndFlush(field);

        // Get all the fieldList where visibility in DEFAULT_VISIBILITY or UPDATED_VISIBILITY
        defaultFieldShouldBeFound("visibility.in=" + DEFAULT_VISIBILITY + "," + UPDATED_VISIBILITY);

        // Get all the fieldList where visibility equals to UPDATED_VISIBILITY
        defaultFieldShouldNotBeFound("visibility.in=" + UPDATED_VISIBILITY);
    }

    @Test
    @Transactional
    public void getAllFieldsByVisibilityIsNullOrNotNull() throws Exception {
        // Initialize the database
        fieldRepository.saveAndFlush(field);

        // Get all the fieldList where visibility is not null
        defaultFieldShouldBeFound("visibility.specified=true");

        // Get all the fieldList where visibility is null
        defaultFieldShouldNotBeFound("visibility.specified=false");
    }

    @Test
    @Transactional
    public void getAllFieldsByOrdIsEqualToSomething() throws Exception {
        // Initialize the database
        fieldRepository.saveAndFlush(field);

        // Get all the fieldList where ord equals to DEFAULT_ORD
        defaultFieldShouldBeFound("ord.equals=" + DEFAULT_ORD);

        // Get all the fieldList where ord equals to UPDATED_ORD
        defaultFieldShouldNotBeFound("ord.equals=" + UPDATED_ORD);
    }

    @Test
    @Transactional
    public void getAllFieldsByOrdIsInShouldWork() throws Exception {
        // Initialize the database
        fieldRepository.saveAndFlush(field);

        // Get all the fieldList where ord in DEFAULT_ORD or UPDATED_ORD
        defaultFieldShouldBeFound("ord.in=" + DEFAULT_ORD + "," + UPDATED_ORD);

        // Get all the fieldList where ord equals to UPDATED_ORD
        defaultFieldShouldNotBeFound("ord.in=" + UPDATED_ORD);
    }

    @Test
    @Transactional
    public void getAllFieldsByOrdIsNullOrNotNull() throws Exception {
        // Initialize the database
        fieldRepository.saveAndFlush(field);

        // Get all the fieldList where ord is not null
        defaultFieldShouldBeFound("ord.specified=true");

        // Get all the fieldList where ord is null
        defaultFieldShouldNotBeFound("ord.specified=false");
    }

    @Test
    @Transactional
    public void getAllFieldsByOrdIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        fieldRepository.saveAndFlush(field);

        // Get all the fieldList where ord greater than or equals to DEFAULT_ORD
        defaultFieldShouldBeFound("ord.greaterOrEqualThan=" + DEFAULT_ORD);

        // Get all the fieldList where ord greater than or equals to UPDATED_ORD
        defaultFieldShouldNotBeFound("ord.greaterOrEqualThan=" + UPDATED_ORD);
    }

    @Test
    @Transactional
    public void getAllFieldsByOrdIsLessThanSomething() throws Exception {
        // Initialize the database
        fieldRepository.saveAndFlush(field);

        // Get all the fieldList where ord less than or equals to DEFAULT_ORD
        defaultFieldShouldNotBeFound("ord.lessThan=" + DEFAULT_ORD);

        // Get all the fieldList where ord less than or equals to UPDATED_ORD
        defaultFieldShouldBeFound("ord.lessThan=" + UPDATED_ORD);
    }

    @Test
    @Transactional
    public void getAllFieldsByRequestTypeIsEqualToSomething() throws Exception {
        // Initialize the database
        fieldRepository.saveAndFlush(field);

        // Get all the fieldList where requestType equals to DEFAULT_REQUEST_TYPE
        defaultFieldShouldBeFound("requestType.equals=" + DEFAULT_REQUEST_TYPE);

        // Get all the fieldList where requestType equals to UPDATED_REQUEST_TYPE
        defaultFieldShouldNotBeFound("requestType.equals=" + UPDATED_REQUEST_TYPE);
    }

    @Test
    @Transactional
    public void getAllFieldsByRequestTypeIsInShouldWork() throws Exception {
        // Initialize the database
        fieldRepository.saveAndFlush(field);

        // Get all the fieldList where requestType in DEFAULT_REQUEST_TYPE or UPDATED_REQUEST_TYPE
        defaultFieldShouldBeFound("requestType.in=" + DEFAULT_REQUEST_TYPE + "," + UPDATED_REQUEST_TYPE);

        // Get all the fieldList where requestType equals to UPDATED_REQUEST_TYPE
        defaultFieldShouldNotBeFound("requestType.in=" + UPDATED_REQUEST_TYPE);
    }

    @Test
    @Transactional
    public void getAllFieldsByRequestTypeIsNullOrNotNull() throws Exception {
        // Initialize the database
        fieldRepository.saveAndFlush(field);

        // Get all the fieldList where requestType is not null
        defaultFieldShouldBeFound("requestType.specified=true");

        // Get all the fieldList where requestType is null
        defaultFieldShouldNotBeFound("requestType.specified=false");
    }

    @Test
    @Transactional
    public void getAllFieldsByRecipientIsEqualToSomething() throws Exception {
        // Initialize the database
        Recipient recipient = RecipientResourceIntTest.createEntity(em);
        em.persist(recipient);
        em.flush();
        field.setRecipient(recipient);
        fieldRepository.saveAndFlush(field);
        Long recipientId = recipient.getId();

        // Get all the fieldList where recipient equals to recipientId
        defaultFieldShouldBeFound("recipientId.equals=" + recipientId);

        // Get all the fieldList where recipient equals to recipientId + 1
        defaultFieldShouldNotBeFound("recipientId.equals=" + (recipientId + 1));
    }

    //    @Test
    @Transactional
    public void getAllFieldsByTitleIsEqualToSomething() throws Exception {
        // Initialize the database
        I18n title = I18nResourceIntTest.createEntity(em);
        em.persist(title);
        em.flush();
        field.addTitle(title);
        fieldRepository.saveAndFlush(field);
        Long titleId = title.getId();

        // Get all the fieldList where title equals to titleId
        defaultFieldShouldBeFound("titleId.equals=" + titleId);

        // Get all the fieldList where title equals to titleId + 1
        defaultFieldShouldNotBeFound("titleId.equals=" + (titleId + 1));
    }

    @Test
    @Transactional
    public void getAllFieldsByDescriptionIsEqualToSomething() throws Exception {
        // Initialize the database
        I18n description = I18nResourceIntTest.createEntity(em);
        em.persist(description);
        em.flush();
        field.addDescription(description);
        fieldRepository.saveAndFlush(field);
        Long descriptionId = description.getId();

        // Get all the fieldList where description equals to descriptionId
        defaultFieldShouldBeFound("descriptionId.equals=" + descriptionId);

        // Get all the fieldList where description equals to descriptionId + 1
        defaultFieldShouldNotBeFound("descriptionId.equals=" + (descriptionId + 1));
    }

    //    @Test
    @Transactional
    public void getAllFieldsByValueIsEqualToSomething() throws Exception {
        // Initialize the database
        FieldValue value = FieldValueResourceIntTest.createEntity(em);
        em.persist(value);
        em.flush();
        field.addValue(value);
        fieldRepository.saveAndFlush(field);
        Long valueId = value.getId();

        // Get all the fieldList where value equals to valueId
        defaultFieldShouldBeFound("valueId.equals=" + valueId);

        // Get all the fieldList where value equals to valueId + 1
        defaultFieldShouldNotBeFound("valueId.equals=" + (valueId + 1));
    }

    @Test
    @Transactional
    public void getAllFieldsByPrefixIsEqualToSomething() throws Exception {
        // Initialize the database
        Prefix prefix = PrefixResourceIntTest.createEntity(em);
        em.persist(prefix);
        em.flush();
        field.addPrefix(prefix);
        fieldRepository.saveAndFlush(field);
        Long prefixId = prefix.getId();

        // Get all the fieldList where prefix equals to prefixId
        defaultFieldShouldBeFound("prefixId.equals=" + prefixId);

        // Get all the fieldList where prefix equals to prefixId + 1
        defaultFieldShouldNotBeFound("prefixId.equals=" + (prefixId + 1));
    }

    /**
     * Executes the search, and checks that the default entity is returned
     */
    private void defaultFieldShouldBeFound(String filter) throws Exception {
        restFieldMockMvc
            .perform(get("/api/fields?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(field.getId().intValue())))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME.toString())))
            .andExpect(jsonPath("$.[*].extId").value(hasItem(DEFAULT_EXT_ID.intValue())))
            .andExpect(jsonPath("$.[*].type").value(hasItem(DEFAULT_TYPE.toString())))
            .andExpect(jsonPath("$.[*].controlType").value(hasItem(DEFAULT_CONTROL_TYPE.toString())))
            .andExpect(jsonPath("$.[*].content").value(hasItem(DEFAULT_CONTENT.toString())))
            .andExpect(jsonPath("$.[*].validation").value(hasItem(DEFAULT_VALIDATION.toString())))
            .andExpect(jsonPath("$.[*].placeholder").value(hasItem(DEFAULT_PLACEHOLDER.toString())))
            .andExpect(jsonPath("$.[*].mask").value(hasItem(DEFAULT_MASK.toString())))
            .andExpect(jsonPath("$.[*].maskSymbol").value(hasItem(DEFAULT_MASK_SYMBOL.toString())))
            .andExpect(jsonPath("$.[*].lengthMin").value(hasItem(DEFAULT_LENGTH_MIN)))
            .andExpect(jsonPath("$.[*].lengthMax").value(hasItem(DEFAULT_LENGTH_MAX)))
            .andExpect(jsonPath("$.[*].requireField").value(hasItem(DEFAULT_REQUIRE_FIELD.toString())))
            .andExpect(jsonPath("$.[*].filterBy").value(hasItem(DEFAULT_FILTER_BY.toString())))
            .andExpect(jsonPath("$.[*].optional").value(hasItem(DEFAULT_OPTIONAL.booleanValue())))
            .andExpect(jsonPath("$.[*].output").value(hasItem(DEFAULT_OUTPUT.booleanValue())))
            .andExpect(jsonPath("$.[*].visibility").value(hasItem(DEFAULT_VISIBILITY.toString())))
            .andExpect(jsonPath("$.[*].ord").value(hasItem(DEFAULT_ORD)))
            .andExpect(jsonPath("$.[*].requestType").value(hasItem(DEFAULT_REQUEST_TYPE.toString())));

        // Check, that the count call also returns 1
        restFieldMockMvc
            .perform(get("/api/fields/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(content().string("1"));
    }

    /**
     * Executes the search, and checks that the default entity is not returned
     */
    private void defaultFieldShouldNotBeFound(String filter) throws Exception {
        restFieldMockMvc
            .perform(get("/api/fields?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty());

        // Check, that the count call also returns 0
        restFieldMockMvc
            .perform(get("/api/fields/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(content().string("0"));
    }

    @Test
    @Transactional
    public void getNonExistingField() throws Exception {
        // Get the field
        restFieldMockMvc.perform(get("/api/fields/{id}", Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updateField() throws Exception {
        // Initialize the database
        fieldRepository.saveAndFlush(field);

        int databaseSizeBeforeUpdate = fieldRepository.findAll().size();

        // Update the field
        Field updatedField = fieldRepository.findById(field.getId()).get();
        // Disconnect from session so that the updates on updatedField are not directly saved in db
        em.detach(updatedField);
        updatedField
            .name(UPDATED_NAME)
            .extId(UPDATED_EXT_ID)
            .type(UPDATED_TYPE)
            .controlType(UPDATED_CONTROL_TYPE)
            .content(UPDATED_CONTENT)
            .validation(UPDATED_VALIDATION)
            .placeholder(UPDATED_PLACEHOLDER)
            .mask(UPDATED_MASK)
            .maskSymbol(UPDATED_MASK_SYMBOL)
            .lengthMin(UPDATED_LENGTH_MIN)
            .lengthMax(UPDATED_LENGTH_MAX)
            .requireField(UPDATED_REQUIRE_FIELD)
            .filterBy(UPDATED_FILTER_BY)
            .optional(UPDATED_OPTIONAL)
            .output(UPDATED_OUTPUT)
            .visibility(UPDATED_VISIBILITY)
            .ord(UPDATED_ORD)
            .requestType(UPDATED_REQUEST_TYPE);
        FieldDTO fieldDTO = fieldMapper.toDto(updatedField);

        restFieldMockMvc
            .perform(put("/api/fields").contentType(TestUtil.APPLICATION_JSON_UTF8).content(TestUtil.convertObjectToJsonBytes(fieldDTO)))
            .andExpect(status().isOk());

        // Validate the Field in the database
        List<Field> fieldList = fieldRepository.findAll();
        assertThat(fieldList).hasSize(databaseSizeBeforeUpdate);
        Field testField = fieldList.get(fieldList.size() - 1);
        assertThat(testField.getName()).isEqualTo(UPDATED_NAME);
        assertThat(testField.getExtId()).isEqualTo(UPDATED_EXT_ID);
        assertThat(testField.getType()).isEqualTo(UPDATED_TYPE);
        assertThat(testField.getControlType()).isEqualTo(UPDATED_CONTROL_TYPE);
        assertThat(testField.getContent()).isEqualTo(UPDATED_CONTENT);
        assertThat(testField.getValidation()).isEqualTo(UPDATED_VALIDATION);
        assertThat(testField.getPlaceholder()).isEqualTo(UPDATED_PLACEHOLDER);
        assertThat(testField.getMask()).isEqualTo(UPDATED_MASK);
        assertThat(testField.getMaskSymbol()).isEqualTo(UPDATED_MASK_SYMBOL);
        assertThat(testField.getLengthMin()).isEqualTo(UPDATED_LENGTH_MIN);
        assertThat(testField.getLengthMax()).isEqualTo(UPDATED_LENGTH_MAX);
        assertThat(testField.getRequireField()).isEqualTo(UPDATED_REQUIRE_FIELD);
        assertThat(testField.getFilterBy()).isEqualTo(UPDATED_FILTER_BY);
        assertThat(testField.isOptional()).isEqualTo(UPDATED_OPTIONAL);
        assertThat(testField.isOutput()).isEqualTo(UPDATED_OUTPUT);
        assertThat(testField.getVisibility()).isEqualTo(UPDATED_VISIBILITY);
        assertThat(testField.getOrd()).isEqualTo(UPDATED_ORD);
        assertThat(testField.getRequestType()).isEqualTo(UPDATED_REQUEST_TYPE);
    }

    @Test
    @Transactional
    public void updateNonExistingField() throws Exception {
        int databaseSizeBeforeUpdate = fieldRepository.findAll().size();

        // Create the Field
        FieldDTO fieldDTO = fieldMapper.toDto(field);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restFieldMockMvc
            .perform(put("/api/fields").contentType(TestUtil.APPLICATION_JSON_UTF8).content(TestUtil.convertObjectToJsonBytes(fieldDTO)))
            .andExpect(status().isBadRequest());

        // Validate the Field in the database
        List<Field> fieldList = fieldRepository.findAll();
        assertThat(fieldList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    public void deleteField() throws Exception {
        int i18nSizeBefore = i18nRepository.findAll().size();

        // Initialize the database
        fieldRepository.saveAndFlush(field);

        int databaseSizeBeforeDelete = fieldRepository.findAll().size();

        // Get the field
        restFieldMockMvc
            .perform(delete("/api/fields/{id}", field.getId()).accept(TestUtil.APPLICATION_JSON_UTF8))
            .andExpect(status().isOk());

        // Validate the database is empty
        List<Field> fieldList = fieldRepository.findAll();
        assertThat(fieldList).hasSize(databaseSizeBeforeDelete - 1);

        assertThat(i18nRepository.findAll().size()).isEqualTo(i18nSizeBefore);
    }

    @Test
    @Transactional
    public void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Field.class);
        Field field1 = new Field();
        field1.setId(1L);
        Field field2 = new Field();
        field2.setId(field1.getId());
        assertThat(field1).isEqualTo(field2);
        field2.setId(2L);
        assertThat(field1).isNotEqualTo(field2);
        field1.setId(null);
        assertThat(field1).isNotEqualTo(field2);
    }

    @Test
    @Transactional
    public void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(FieldDTO.class);
        FieldDTO fieldDTO1 = new FieldDTO();
        fieldDTO1.setId(1L);
        FieldDTO fieldDTO2 = new FieldDTO();
        assertThat(fieldDTO1).isNotEqualTo(fieldDTO2);
        fieldDTO2.setId(fieldDTO1.getId());
        assertThat(fieldDTO1).isEqualTo(fieldDTO2);
        fieldDTO2.setId(2L);
        assertThat(fieldDTO1).isNotEqualTo(fieldDTO2);
        fieldDTO1.setId(null);
        assertThat(fieldDTO1).isNotEqualTo(fieldDTO2);
    }
    //    @Test
    //    @Transactional
    //    public void testEntityFromId() {
    //        assertThat(fieldMapper.fromId(42L).getId()).isEqualTo(42);
    //        assertThat(fieldMapper.fromId(null)).isNull();
    //    }
}
