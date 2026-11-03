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
import org.springframework.transaction.annotation.Transactional;
import uz.devops.bmcore.enumeration.ControlType;
import uz.devops.bmcore.enumeration.FieldType;
import uz.devops.bmcore.enumeration.RequestType;
import uz.devops.bmcore.enumeration.Visibility;
import uz.paykap.IntegrationTest;
import uz.paykap.domain.*;
import uz.paykap.repository.FieldRepository;
import uz.paykap.repository.FieldValueRepository;
import uz.paykap.service.FieldService;
import uz.paykap.service.dto.FieldDTO;
import uz.paykap.service.mapper.FieldMapper;

import javax.persistence.EntityManager;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for the {@link FieldResource} REST controller.
 */
@IntegrationTest
@ExtendWith(MockitoExtension.class)
@AutoConfigureMockMvc
@WithMockUser
class FieldResourceIT {

    private static final String DEFAULT_NAME = "AAAAAAAAAA";
    private static final String UPDATED_NAME = "BBBBBBBBBB";

    private static final Long DEFAULT_EXT_ID = 1L;
    private static final Long UPDATED_EXT_ID = 2L;
    private static final Long SMALLER_EXT_ID = 1L - 1L;

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
    private static final Integer SMALLER_LENGTH_MIN = 0 - 1;

    private static final Integer DEFAULT_LENGTH_MAX = 0;
    private static final Integer UPDATED_LENGTH_MAX = 1;
    private static final Integer SMALLER_LENGTH_MAX = 0 - 1;

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
    private static final Integer SMALLER_ORD = 1 - 1;

    private static final RequestType DEFAULT_REQUEST_TYPE = RequestType.CHECK;
    private static final RequestType UPDATED_REQUEST_TYPE = RequestType.PAY;

    private static final String ENTITY_API_URL = "/api/fields";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong count = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private FieldRepository fieldRepository;

    @Autowired
    private FieldValueRepository fieldValueRepository;

    @Mock
    private FieldRepository fieldRepositoryMock;

    @Autowired
    private FieldMapper fieldMapper;

    @Mock
    private FieldService fieldServiceMock;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restFieldMockMvc;

    private Field field;

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
        Recipient recipient;
        if (TestUtil.findAll(em, Recipient.class).isEmpty()) {
            recipient = RecipientResourceIT.createEntity(em);
            em.persist(recipient);
            em.flush();
        } else {
            recipient = TestUtil.findAll(em, Recipient.class).get(0);
        }
        field.setRecipient(recipient);
        // Add required entity
        I18n i18n;
        //        if (TestUtil.findAll(em, I18n.class).isEmpty()) {
        i18n = I18nResourceIT.createEntity(em);
        //            em.persist(i18n);
        //            em.flush();
        //        } else {
        //            i18n = TestUtil.findAll(em, I18n.class).get(0);
        //        }
        field.getTitles().add(i18n);

//        field.addValue(new FieldValue().value("123"));
//        field.addPrefix(new Prefix().value("+99890"));

        return field;
    }

    /**
     * Create an updated entity for this test.
     * <p>
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Field createUpdatedEntity(EntityManager em) {
        Field field = new Field()
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
        // Add required entity
        Recipient recipient;
        if (TestUtil.findAll(em, Recipient.class).isEmpty()) {
            recipient = RecipientResourceIT.createUpdatedEntity(em);
            em.persist(recipient);
            em.flush();
        } else {
            recipient = TestUtil.findAll(em, Recipient.class).get(0);
        }
        field.setRecipient(recipient);
        // Add required entity
        I18n i18n;
        if (TestUtil.findAll(em, I18n.class).isEmpty()) {
            i18n = I18nResourceIT.createUpdatedEntity(em);
            em.persist(i18n);
            em.flush();
        } else {
            i18n = TestUtil.findAll(em, I18n.class).get(0);
        }
        field.getTitles().add(i18n);
        return field;
    }

    @BeforeEach
    public void initTest() {
        field = createEntity(em);
    }

    @Test
    @Transactional
    void createFieldWithValues() {

        int databaseSizeBeforeCreate = fieldRepository.findAll().size();
        int fvDatabaseSizeBeforeCreate = fieldValueRepository.findAll().size();

        Field field = createEntity(em);

//        FieldValue fieldValue = new FieldValue();
//        fieldValue.setValue("123");
        field.addValue(new FieldValue().value("1111"));
        field.addValue(new FieldValue().value("2222"));
        field.addValue(new FieldValue().value("3333"));

//        field.setValues(null);

        field = fieldRepository.save(field);

//        Set<FieldValue> fieldValues = field.getValues();

        for (FieldValue value : field.getValues()) {
            value.setField(field);
        }

        fieldValueRepository.saveAll(field.getValues());

        List<Field> fieldList = fieldRepository.findAll();
        assertThat(fieldList).hasSize(databaseSizeBeforeCreate + 1);

        List<FieldValue> list = fieldValueRepository.findAll();
        assertThat(list).hasSize(fvDatabaseSizeBeforeCreate + 3);

    }

    @Test
    @Transactional
    void createField() throws Exception {
        int databaseSizeBeforeCreate = fieldRepository.findAll().size();
        // Create the Field
        FieldDTO fieldDTO = fieldMapper.toDto(field);
        restFieldMockMvc
                .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(fieldDTO)))
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
        assertThat(testField.getOptional()).isEqualTo(DEFAULT_OPTIONAL);
        assertThat(testField.getOutput()).isEqualTo(DEFAULT_OUTPUT);
        assertThat(testField.getVisibility()).isEqualTo(DEFAULT_VISIBILITY);
        assertThat(testField.getOrd()).isEqualTo(DEFAULT_ORD);
        assertThat(testField.getRequestType()).isEqualTo(DEFAULT_REQUEST_TYPE);
    }

    @Test
    @Transactional
    void createFieldWithExistingId() throws Exception {
        // Create the Field with an existing ID
        field.setId(1L);
        FieldDTO fieldDTO = fieldMapper.toDto(field);

        int databaseSizeBeforeCreate = fieldRepository.findAll().size();

        // An entity with an existing ID cannot be created, so this API call must fail
        restFieldMockMvc
                .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(fieldDTO)))
                .andExpect(status().isBadRequest());

        // Validate the Field in the database
        List<Field> fieldList = fieldRepository.findAll();
        assertThat(fieldList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkNameIsRequired() throws Exception {
        int databaseSizeBeforeTest = fieldRepository.findAll().size();
        // set the field null
        field.setName(null);

        // Create the Field, which fails.
        FieldDTO fieldDTO = fieldMapper.toDto(field);

        restFieldMockMvc
                .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(fieldDTO)))
                .andExpect(status().isBadRequest());

        List<Field> fieldList = fieldRepository.findAll();
        assertThat(fieldList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkExtIdIsRequired() throws Exception {
        int databaseSizeBeforeTest = fieldRepository.findAll().size();
        // set the field null
        field.setExtId(null);

        // Create the Field, which fails.
        FieldDTO fieldDTO = fieldMapper.toDto(field);

        restFieldMockMvc
                .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(fieldDTO)))
                .andExpect(status().isBadRequest());

        List<Field> fieldList = fieldRepository.findAll();
        assertThat(fieldList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkTypeIsRequired() throws Exception {
        int databaseSizeBeforeTest = fieldRepository.findAll().size();
        // set the field null
        field.setType(null);

        // Create the Field, which fails.
        FieldDTO fieldDTO = fieldMapper.toDto(field);

        restFieldMockMvc
                .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(fieldDTO)))
                .andExpect(status().isBadRequest());

        List<Field> fieldList = fieldRepository.findAll();
        assertThat(fieldList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkControlTypeIsRequired() throws Exception {
        int databaseSizeBeforeTest = fieldRepository.findAll().size();
        // set the field null
        field.setControlType(null);

        // Create the Field, which fails.
        FieldDTO fieldDTO = fieldMapper.toDto(field);

        restFieldMockMvc
                .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(fieldDTO)))
                .andExpect(status().isBadRequest());

        List<Field> fieldList = fieldRepository.findAll();
        assertThat(fieldList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllFields() throws Exception {
        // Initialize the database
        fieldRepository.saveAndFlush(field);

        // Get all the fieldList
        restFieldMockMvc
                .perform(get(ENTITY_API_URL + "?sort=id,desc"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.[*].id").value(hasItem(field.getId().intValue())))
                .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME)))
                .andExpect(jsonPath("$.[*].extId").value(hasItem(DEFAULT_EXT_ID.intValue())))
                .andExpect(jsonPath("$.[*].type").value(hasItem(DEFAULT_TYPE.toString())))
                .andExpect(jsonPath("$.[*].controlType").value(hasItem(DEFAULT_CONTROL_TYPE.toString())))
                .andExpect(jsonPath("$.[*].content").value(hasItem(DEFAULT_CONTENT)))
                .andExpect(jsonPath("$.[*].validation").value(hasItem(DEFAULT_VALIDATION)))
                .andExpect(jsonPath("$.[*].placeholder").value(hasItem(DEFAULT_PLACEHOLDER)))
                .andExpect(jsonPath("$.[*].mask").value(hasItem(DEFAULT_MASK)))
                .andExpect(jsonPath("$.[*].maskSymbol").value(hasItem(DEFAULT_MASK_SYMBOL)))
                .andExpect(jsonPath("$.[*].lengthMin").value(hasItem(DEFAULT_LENGTH_MIN)))
                .andExpect(jsonPath("$.[*].lengthMax").value(hasItem(DEFAULT_LENGTH_MAX)))
                .andExpect(jsonPath("$.[*].requireField").value(hasItem(DEFAULT_REQUIRE_FIELD)))
                .andExpect(jsonPath("$.[*].filterBy").value(hasItem(DEFAULT_FILTER_BY)))
                .andExpect(jsonPath("$.[*].optional").value(hasItem(DEFAULT_OPTIONAL.booleanValue())))
                .andExpect(jsonPath("$.[*].output").value(hasItem(DEFAULT_OUTPUT.booleanValue())))
                .andExpect(jsonPath("$.[*].visibility").value(hasItem(DEFAULT_VISIBILITY.toString())))
                .andExpect(jsonPath("$.[*].ord").value(hasItem(DEFAULT_ORD)))
                .andExpect(jsonPath("$.[*].requestType").value(hasItem(DEFAULT_REQUEST_TYPE.toString())));
    }

    @SuppressWarnings({"unchecked"})
    void getAllFieldsWithEagerRelationshipsIsEnabled() throws Exception {
        when(fieldServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restFieldMockMvc.perform(get(ENTITY_API_URL + "?eagerload=true")).andExpect(status().isOk());

        verify(fieldServiceMock, times(1)).findAllWithEagerRelationships(any());
    }

    @SuppressWarnings({"unchecked"})
    void getAllFieldsWithEagerRelationshipsIsNotEnabled() throws Exception {
        when(fieldServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restFieldMockMvc.perform(get(ENTITY_API_URL + "?eagerload=true")).andExpect(status().isOk());

        verify(fieldServiceMock, times(1)).findAllWithEagerRelationships(any());
    }

    @Test
    @Transactional
    void getField() throws Exception {
        // Initialize the database
        fieldRepository.saveAndFlush(field);

        // Get the field
        restFieldMockMvc
                .perform(get(ENTITY_API_URL_ID, field.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.id").value(field.getId().intValue()))
                .andExpect(jsonPath("$.name").value(DEFAULT_NAME))
                .andExpect(jsonPath("$.extId").value(DEFAULT_EXT_ID.intValue()))
                .andExpect(jsonPath("$.type").value(DEFAULT_TYPE.toString()))
                .andExpect(jsonPath("$.controlType").value(DEFAULT_CONTROL_TYPE.toString()))
                .andExpect(jsonPath("$.content").value(DEFAULT_CONTENT))
                .andExpect(jsonPath("$.validation").value(DEFAULT_VALIDATION))
                .andExpect(jsonPath("$.placeholder").value(DEFAULT_PLACEHOLDER))
                .andExpect(jsonPath("$.mask").value(DEFAULT_MASK))
                .andExpect(jsonPath("$.maskSymbol").value(DEFAULT_MASK_SYMBOL))
                .andExpect(jsonPath("$.lengthMin").value(DEFAULT_LENGTH_MIN))
                .andExpect(jsonPath("$.lengthMax").value(DEFAULT_LENGTH_MAX))
                .andExpect(jsonPath("$.requireField").value(DEFAULT_REQUIRE_FIELD))
                .andExpect(jsonPath("$.filterBy").value(DEFAULT_FILTER_BY))
                .andExpect(jsonPath("$.optional").value(DEFAULT_OPTIONAL.booleanValue()))
                .andExpect(jsonPath("$.output").value(DEFAULT_OUTPUT.booleanValue()))
                .andExpect(jsonPath("$.visibility").value(DEFAULT_VISIBILITY.toString()))
                .andExpect(jsonPath("$.ord").value(DEFAULT_ORD))
                .andExpect(jsonPath("$.requestType").value(DEFAULT_REQUEST_TYPE.toString()));
    }

    @Test
    @Transactional
    void getFieldsByIdFiltering() throws Exception {
        // Initialize the database
        fieldRepository.saveAndFlush(field);

        Long id = field.getId();

        defaultFieldShouldBeFound("id.equals=" + id);
        defaultFieldShouldNotBeFound("id.notEquals=" + id);

        defaultFieldShouldBeFound("id.greaterThanOrEqual=" + id);
        defaultFieldShouldNotBeFound("id.greaterThan=" + id);

        defaultFieldShouldBeFound("id.lessThanOrEqual=" + id);
        defaultFieldShouldNotBeFound("id.lessThan=" + id);
    }

    @Test
    @Transactional
    void getAllFieldsByNameIsEqualToSomething() throws Exception {
        // Initialize the database
        fieldRepository.saveAndFlush(field);

        // Get all the fieldList where name equals to DEFAULT_NAME
        defaultFieldShouldBeFound("name.equals=" + DEFAULT_NAME);

        // Get all the fieldList where name equals to UPDATED_NAME
        defaultFieldShouldNotBeFound("name.equals=" + UPDATED_NAME);
    }

    @Test
    @Transactional
    void getAllFieldsByNameIsNotEqualToSomething() throws Exception {
        // Initialize the database
        fieldRepository.saveAndFlush(field);

        // Get all the fieldList where name not equals to DEFAULT_NAME
        defaultFieldShouldNotBeFound("name.notEquals=" + DEFAULT_NAME);

        // Get all the fieldList where name not equals to UPDATED_NAME
        defaultFieldShouldBeFound("name.notEquals=" + UPDATED_NAME);
    }

    @Test
    @Transactional
    void getAllFieldsByNameIsInShouldWork() throws Exception {
        // Initialize the database
        fieldRepository.saveAndFlush(field);

        // Get all the fieldList where name in DEFAULT_NAME or UPDATED_NAME
        defaultFieldShouldBeFound("name.in=" + DEFAULT_NAME + "," + UPDATED_NAME);

        // Get all the fieldList where name equals to UPDATED_NAME
        defaultFieldShouldNotBeFound("name.in=" + UPDATED_NAME);
    }

    @Test
    @Transactional
    void getAllFieldsByNameIsNullOrNotNull() throws Exception {
        // Initialize the database
        fieldRepository.saveAndFlush(field);

        // Get all the fieldList where name is not null
        defaultFieldShouldBeFound("name.specified=true");

        // Get all the fieldList where name is null
        defaultFieldShouldNotBeFound("name.specified=false");
    }

    @Test
    @Transactional
    void getAllFieldsByNameContainsSomething() throws Exception {
        // Initialize the database
        fieldRepository.saveAndFlush(field);

        // Get all the fieldList where name contains DEFAULT_NAME
        defaultFieldShouldBeFound("name.contains=" + DEFAULT_NAME);

        // Get all the fieldList where name contains UPDATED_NAME
        defaultFieldShouldNotBeFound("name.contains=" + UPDATED_NAME);
    }

    @Test
    @Transactional
    void getAllFieldsByNameNotContainsSomething() throws Exception {
        // Initialize the database
        fieldRepository.saveAndFlush(field);

        // Get all the fieldList where name does not contain DEFAULT_NAME
        defaultFieldShouldNotBeFound("name.doesNotContain=" + DEFAULT_NAME);

        // Get all the fieldList where name does not contain UPDATED_NAME
        defaultFieldShouldBeFound("name.doesNotContain=" + UPDATED_NAME);
    }

    @Test
    @Transactional
    void getAllFieldsByExtIdIsEqualToSomething() throws Exception {
        // Initialize the database
        fieldRepository.saveAndFlush(field);

        // Get all the fieldList where extId equals to DEFAULT_EXT_ID
        defaultFieldShouldBeFound("extId.equals=" + DEFAULT_EXT_ID);

        // Get all the fieldList where extId equals to UPDATED_EXT_ID
        defaultFieldShouldNotBeFound("extId.equals=" + UPDATED_EXT_ID);
    }

    @Test
    @Transactional
    void getAllFieldsByExtIdIsNotEqualToSomething() throws Exception {
        // Initialize the database
        fieldRepository.saveAndFlush(field);

        // Get all the fieldList where extId not equals to DEFAULT_EXT_ID
        defaultFieldShouldNotBeFound("extId.notEquals=" + DEFAULT_EXT_ID);

        // Get all the fieldList where extId not equals to UPDATED_EXT_ID
        defaultFieldShouldBeFound("extId.notEquals=" + UPDATED_EXT_ID);
    }

    @Test
    @Transactional
    void getAllFieldsByExtIdIsInShouldWork() throws Exception {
        // Initialize the database
        fieldRepository.saveAndFlush(field);

        // Get all the fieldList where extId in DEFAULT_EXT_ID or UPDATED_EXT_ID
        defaultFieldShouldBeFound("extId.in=" + DEFAULT_EXT_ID + "," + UPDATED_EXT_ID);

        // Get all the fieldList where extId equals to UPDATED_EXT_ID
        defaultFieldShouldNotBeFound("extId.in=" + UPDATED_EXT_ID);
    }

    @Test
    @Transactional
    void getAllFieldsByExtIdIsNullOrNotNull() throws Exception {
        // Initialize the database
        fieldRepository.saveAndFlush(field);

        // Get all the fieldList where extId is not null
        defaultFieldShouldBeFound("extId.specified=true");

        // Get all the fieldList where extId is null
        defaultFieldShouldNotBeFound("extId.specified=false");
    }

    @Test
    @Transactional
    void getAllFieldsByExtIdIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        fieldRepository.saveAndFlush(field);

        // Get all the fieldList where extId is greater than or equal to DEFAULT_EXT_ID
        defaultFieldShouldBeFound("extId.greaterThanOrEqual=" + DEFAULT_EXT_ID);

        // Get all the fieldList where extId is greater than or equal to UPDATED_EXT_ID
        defaultFieldShouldNotBeFound("extId.greaterThanOrEqual=" + UPDATED_EXT_ID);
    }

    @Test
    @Transactional
    void getAllFieldsByExtIdIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        fieldRepository.saveAndFlush(field);

        // Get all the fieldList where extId is less than or equal to DEFAULT_EXT_ID
        defaultFieldShouldBeFound("extId.lessThanOrEqual=" + DEFAULT_EXT_ID);

        // Get all the fieldList where extId is less than or equal to SMALLER_EXT_ID
        defaultFieldShouldNotBeFound("extId.lessThanOrEqual=" + SMALLER_EXT_ID);
    }

    @Test
    @Transactional
    void getAllFieldsByExtIdIsLessThanSomething() throws Exception {
        // Initialize the database
        fieldRepository.saveAndFlush(field);

        // Get all the fieldList where extId is less than DEFAULT_EXT_ID
        defaultFieldShouldNotBeFound("extId.lessThan=" + DEFAULT_EXT_ID);

        // Get all the fieldList where extId is less than UPDATED_EXT_ID
        defaultFieldShouldBeFound("extId.lessThan=" + UPDATED_EXT_ID);
    }

    @Test
    @Transactional
    void getAllFieldsByExtIdIsGreaterThanSomething() throws Exception {
        // Initialize the database
        fieldRepository.saveAndFlush(field);

        // Get all the fieldList where extId is greater than DEFAULT_EXT_ID
        defaultFieldShouldNotBeFound("extId.greaterThan=" + DEFAULT_EXT_ID);

        // Get all the fieldList where extId is greater than SMALLER_EXT_ID
        defaultFieldShouldBeFound("extId.greaterThan=" + SMALLER_EXT_ID);
    }

    @Test
    @Transactional
    void getAllFieldsByTypeIsEqualToSomething() throws Exception {
        // Initialize the database
        fieldRepository.saveAndFlush(field);

        // Get all the fieldList where type equals to DEFAULT_TYPE
        defaultFieldShouldBeFound("type.equals=" + DEFAULT_TYPE);

        // Get all the fieldList where type equals to UPDATED_TYPE
        defaultFieldShouldNotBeFound("type.equals=" + UPDATED_TYPE);
    }

    @Test
    @Transactional
    void getAllFieldsByTypeIsNotEqualToSomething() throws Exception {
        // Initialize the database
        fieldRepository.saveAndFlush(field);

        // Get all the fieldList where type not equals to DEFAULT_TYPE
        defaultFieldShouldNotBeFound("type.notEquals=" + DEFAULT_TYPE);

        // Get all the fieldList where type not equals to UPDATED_TYPE
        defaultFieldShouldBeFound("type.notEquals=" + UPDATED_TYPE);
    }

    @Test
    @Transactional
    void getAllFieldsByTypeIsInShouldWork() throws Exception {
        // Initialize the database
        fieldRepository.saveAndFlush(field);

        // Get all the fieldList where type in DEFAULT_TYPE or UPDATED_TYPE
        defaultFieldShouldBeFound("type.in=" + DEFAULT_TYPE + "," + UPDATED_TYPE);

        // Get all the fieldList where type equals to UPDATED_TYPE
        defaultFieldShouldNotBeFound("type.in=" + UPDATED_TYPE);
    }

    @Test
    @Transactional
    void getAllFieldsByTypeIsNullOrNotNull() throws Exception {
        // Initialize the database
        fieldRepository.saveAndFlush(field);

        // Get all the fieldList where type is not null
        defaultFieldShouldBeFound("type.specified=true");

        // Get all the fieldList where type is null
        defaultFieldShouldNotBeFound("type.specified=false");
    }

    @Test
    @Transactional
    void getAllFieldsByControlTypeIsEqualToSomething() throws Exception {
        // Initialize the database
        fieldRepository.saveAndFlush(field);

        // Get all the fieldList where controlType equals to DEFAULT_CONTROL_TYPE
        defaultFieldShouldBeFound("controlType.equals=" + DEFAULT_CONTROL_TYPE);

        // Get all the fieldList where controlType equals to UPDATED_CONTROL_TYPE
        defaultFieldShouldNotBeFound("controlType.equals=" + UPDATED_CONTROL_TYPE);
    }

    @Test
    @Transactional
    void getAllFieldsByControlTypeIsNotEqualToSomething() throws Exception {
        // Initialize the database
        fieldRepository.saveAndFlush(field);

        // Get all the fieldList where controlType not equals to DEFAULT_CONTROL_TYPE
        defaultFieldShouldNotBeFound("controlType.notEquals=" + DEFAULT_CONTROL_TYPE);

        // Get all the fieldList where controlType not equals to UPDATED_CONTROL_TYPE
        defaultFieldShouldBeFound("controlType.notEquals=" + UPDATED_CONTROL_TYPE);
    }

    @Test
    @Transactional
    void getAllFieldsByControlTypeIsInShouldWork() throws Exception {
        // Initialize the database
        fieldRepository.saveAndFlush(field);

        // Get all the fieldList where controlType in DEFAULT_CONTROL_TYPE or UPDATED_CONTROL_TYPE
        defaultFieldShouldBeFound("controlType.in=" + DEFAULT_CONTROL_TYPE + "," + UPDATED_CONTROL_TYPE);

        // Get all the fieldList where controlType equals to UPDATED_CONTROL_TYPE
        defaultFieldShouldNotBeFound("controlType.in=" + UPDATED_CONTROL_TYPE);
    }

    @Test
    @Transactional
    void getAllFieldsByControlTypeIsNullOrNotNull() throws Exception {
        // Initialize the database
        fieldRepository.saveAndFlush(field);

        // Get all the fieldList where controlType is not null
        defaultFieldShouldBeFound("controlType.specified=true");

        // Get all the fieldList where controlType is null
        defaultFieldShouldNotBeFound("controlType.specified=false");
    }

    @Test
    @Transactional
    void getAllFieldsByContentIsEqualToSomething() throws Exception {
        // Initialize the database
        fieldRepository.saveAndFlush(field);

        // Get all the fieldList where content equals to DEFAULT_CONTENT
        defaultFieldShouldBeFound("content.equals=" + DEFAULT_CONTENT);

        // Get all the fieldList where content equals to UPDATED_CONTENT
        defaultFieldShouldNotBeFound("content.equals=" + UPDATED_CONTENT);
    }

    @Test
    @Transactional
    void getAllFieldsByContentIsNotEqualToSomething() throws Exception {
        // Initialize the database
        fieldRepository.saveAndFlush(field);

        // Get all the fieldList where content not equals to DEFAULT_CONTENT
        defaultFieldShouldNotBeFound("content.notEquals=" + DEFAULT_CONTENT);

        // Get all the fieldList where content not equals to UPDATED_CONTENT
        defaultFieldShouldBeFound("content.notEquals=" + UPDATED_CONTENT);
    }

    @Test
    @Transactional
    void getAllFieldsByContentIsInShouldWork() throws Exception {
        // Initialize the database
        fieldRepository.saveAndFlush(field);

        // Get all the fieldList where content in DEFAULT_CONTENT or UPDATED_CONTENT
        defaultFieldShouldBeFound("content.in=" + DEFAULT_CONTENT + "," + UPDATED_CONTENT);

        // Get all the fieldList where content equals to UPDATED_CONTENT
        defaultFieldShouldNotBeFound("content.in=" + UPDATED_CONTENT);
    }

    @Test
    @Transactional
    void getAllFieldsByContentIsNullOrNotNull() throws Exception {
        // Initialize the database
        fieldRepository.saveAndFlush(field);

        // Get all the fieldList where content is not null
        defaultFieldShouldBeFound("content.specified=true");

        // Get all the fieldList where content is null
        defaultFieldShouldNotBeFound("content.specified=false");
    }

    @Test
    @Transactional
    void getAllFieldsByContentContainsSomething() throws Exception {
        // Initialize the database
        fieldRepository.saveAndFlush(field);

        // Get all the fieldList where content contains DEFAULT_CONTENT
        defaultFieldShouldBeFound("content.contains=" + DEFAULT_CONTENT);

        // Get all the fieldList where content contains UPDATED_CONTENT
        defaultFieldShouldNotBeFound("content.contains=" + UPDATED_CONTENT);
    }

    @Test
    @Transactional
    void getAllFieldsByContentNotContainsSomething() throws Exception {
        // Initialize the database
        fieldRepository.saveAndFlush(field);

        // Get all the fieldList where content does not contain DEFAULT_CONTENT
        defaultFieldShouldNotBeFound("content.doesNotContain=" + DEFAULT_CONTENT);

        // Get all the fieldList where content does not contain UPDATED_CONTENT
        defaultFieldShouldBeFound("content.doesNotContain=" + UPDATED_CONTENT);
    }

    @Test
    @Transactional
    void getAllFieldsByValidationIsEqualToSomething() throws Exception {
        // Initialize the database
        fieldRepository.saveAndFlush(field);

        // Get all the fieldList where validation equals to DEFAULT_VALIDATION
        defaultFieldShouldBeFound("validation.equals=" + DEFAULT_VALIDATION);

        // Get all the fieldList where validation equals to UPDATED_VALIDATION
        defaultFieldShouldNotBeFound("validation.equals=" + UPDATED_VALIDATION);
    }

    @Test
    @Transactional
    void getAllFieldsByValidationIsNotEqualToSomething() throws Exception {
        // Initialize the database
        fieldRepository.saveAndFlush(field);

        // Get all the fieldList where validation not equals to DEFAULT_VALIDATION
        defaultFieldShouldNotBeFound("validation.notEquals=" + DEFAULT_VALIDATION);

        // Get all the fieldList where validation not equals to UPDATED_VALIDATION
        defaultFieldShouldBeFound("validation.notEquals=" + UPDATED_VALIDATION);
    }

    @Test
    @Transactional
    void getAllFieldsByValidationIsInShouldWork() throws Exception {
        // Initialize the database
        fieldRepository.saveAndFlush(field);

        // Get all the fieldList where validation in DEFAULT_VALIDATION or UPDATED_VALIDATION
        defaultFieldShouldBeFound("validation.in=" + DEFAULT_VALIDATION + "," + UPDATED_VALIDATION);

        // Get all the fieldList where validation equals to UPDATED_VALIDATION
        defaultFieldShouldNotBeFound("validation.in=" + UPDATED_VALIDATION);
    }

    @Test
    @Transactional
    void getAllFieldsByValidationIsNullOrNotNull() throws Exception {
        // Initialize the database
        fieldRepository.saveAndFlush(field);

        // Get all the fieldList where validation is not null
        defaultFieldShouldBeFound("validation.specified=true");

        // Get all the fieldList where validation is null
        defaultFieldShouldNotBeFound("validation.specified=false");
    }

    @Test
    @Transactional
    void getAllFieldsByValidationContainsSomething() throws Exception {
        // Initialize the database
        fieldRepository.saveAndFlush(field);

        // Get all the fieldList where validation contains DEFAULT_VALIDATION
        defaultFieldShouldBeFound("validation.contains=" + DEFAULT_VALIDATION);

        // Get all the fieldList where validation contains UPDATED_VALIDATION
        defaultFieldShouldNotBeFound("validation.contains=" + UPDATED_VALIDATION);
    }

    @Test
    @Transactional
    void getAllFieldsByValidationNotContainsSomething() throws Exception {
        // Initialize the database
        fieldRepository.saveAndFlush(field);

        // Get all the fieldList where validation does not contain DEFAULT_VALIDATION
        defaultFieldShouldNotBeFound("validation.doesNotContain=" + DEFAULT_VALIDATION);

        // Get all the fieldList where validation does not contain UPDATED_VALIDATION
        defaultFieldShouldBeFound("validation.doesNotContain=" + UPDATED_VALIDATION);
    }

    @Test
    @Transactional
    void getAllFieldsByPlaceholderIsEqualToSomething() throws Exception {
        // Initialize the database
        fieldRepository.saveAndFlush(field);

        // Get all the fieldList where placeholder equals to DEFAULT_PLACEHOLDER
        defaultFieldShouldBeFound("placeholder.equals=" + DEFAULT_PLACEHOLDER);

        // Get all the fieldList where placeholder equals to UPDATED_PLACEHOLDER
        defaultFieldShouldNotBeFound("placeholder.equals=" + UPDATED_PLACEHOLDER);
    }

    @Test
    @Transactional
    void getAllFieldsByPlaceholderIsNotEqualToSomething() throws Exception {
        // Initialize the database
        fieldRepository.saveAndFlush(field);

        // Get all the fieldList where placeholder not equals to DEFAULT_PLACEHOLDER
        defaultFieldShouldNotBeFound("placeholder.notEquals=" + DEFAULT_PLACEHOLDER);

        // Get all the fieldList where placeholder not equals to UPDATED_PLACEHOLDER
        defaultFieldShouldBeFound("placeholder.notEquals=" + UPDATED_PLACEHOLDER);
    }

    @Test
    @Transactional
    void getAllFieldsByPlaceholderIsInShouldWork() throws Exception {
        // Initialize the database
        fieldRepository.saveAndFlush(field);

        // Get all the fieldList where placeholder in DEFAULT_PLACEHOLDER or UPDATED_PLACEHOLDER
        defaultFieldShouldBeFound("placeholder.in=" + DEFAULT_PLACEHOLDER + "," + UPDATED_PLACEHOLDER);

        // Get all the fieldList where placeholder equals to UPDATED_PLACEHOLDER
        defaultFieldShouldNotBeFound("placeholder.in=" + UPDATED_PLACEHOLDER);
    }

    @Test
    @Transactional
    void getAllFieldsByPlaceholderIsNullOrNotNull() throws Exception {
        // Initialize the database
        fieldRepository.saveAndFlush(field);

        // Get all the fieldList where placeholder is not null
        defaultFieldShouldBeFound("placeholder.specified=true");

        // Get all the fieldList where placeholder is null
        defaultFieldShouldNotBeFound("placeholder.specified=false");
    }

    @Test
    @Transactional
    void getAllFieldsByPlaceholderContainsSomething() throws Exception {
        // Initialize the database
        fieldRepository.saveAndFlush(field);

        // Get all the fieldList where placeholder contains DEFAULT_PLACEHOLDER
        defaultFieldShouldBeFound("placeholder.contains=" + DEFAULT_PLACEHOLDER);

        // Get all the fieldList where placeholder contains UPDATED_PLACEHOLDER
        defaultFieldShouldNotBeFound("placeholder.contains=" + UPDATED_PLACEHOLDER);
    }

    @Test
    @Transactional
    void getAllFieldsByPlaceholderNotContainsSomething() throws Exception {
        // Initialize the database
        fieldRepository.saveAndFlush(field);

        // Get all the fieldList where placeholder does not contain DEFAULT_PLACEHOLDER
        defaultFieldShouldNotBeFound("placeholder.doesNotContain=" + DEFAULT_PLACEHOLDER);

        // Get all the fieldList where placeholder does not contain UPDATED_PLACEHOLDER
        defaultFieldShouldBeFound("placeholder.doesNotContain=" + UPDATED_PLACEHOLDER);
    }

    @Test
    @Transactional
    void getAllFieldsByMaskIsEqualToSomething() throws Exception {
        // Initialize the database
        fieldRepository.saveAndFlush(field);

        // Get all the fieldList where mask equals to DEFAULT_MASK
        defaultFieldShouldBeFound("mask.equals=" + DEFAULT_MASK);

        // Get all the fieldList where mask equals to UPDATED_MASK
        defaultFieldShouldNotBeFound("mask.equals=" + UPDATED_MASK);
    }

    @Test
    @Transactional
    void getAllFieldsByMaskIsNotEqualToSomething() throws Exception {
        // Initialize the database
        fieldRepository.saveAndFlush(field);

        // Get all the fieldList where mask not equals to DEFAULT_MASK
        defaultFieldShouldNotBeFound("mask.notEquals=" + DEFAULT_MASK);

        // Get all the fieldList where mask not equals to UPDATED_MASK
        defaultFieldShouldBeFound("mask.notEquals=" + UPDATED_MASK);
    }

    @Test
    @Transactional
    void getAllFieldsByMaskIsInShouldWork() throws Exception {
        // Initialize the database
        fieldRepository.saveAndFlush(field);

        // Get all the fieldList where mask in DEFAULT_MASK or UPDATED_MASK
        defaultFieldShouldBeFound("mask.in=" + DEFAULT_MASK + "," + UPDATED_MASK);

        // Get all the fieldList where mask equals to UPDATED_MASK
        defaultFieldShouldNotBeFound("mask.in=" + UPDATED_MASK);
    }

    @Test
    @Transactional
    void getAllFieldsByMaskIsNullOrNotNull() throws Exception {
        // Initialize the database
        fieldRepository.saveAndFlush(field);

        // Get all the fieldList where mask is not null
        defaultFieldShouldBeFound("mask.specified=true");

        // Get all the fieldList where mask is null
        defaultFieldShouldNotBeFound("mask.specified=false");
    }

    @Test
    @Transactional
    void getAllFieldsByMaskContainsSomething() throws Exception {
        // Initialize the database
        fieldRepository.saveAndFlush(field);

        // Get all the fieldList where mask contains DEFAULT_MASK
        defaultFieldShouldBeFound("mask.contains=" + DEFAULT_MASK);

        // Get all the fieldList where mask contains UPDATED_MASK
        defaultFieldShouldNotBeFound("mask.contains=" + UPDATED_MASK);
    }

    @Test
    @Transactional
    void getAllFieldsByMaskNotContainsSomething() throws Exception {
        // Initialize the database
        fieldRepository.saveAndFlush(field);

        // Get all the fieldList where mask does not contain DEFAULT_MASK
        defaultFieldShouldNotBeFound("mask.doesNotContain=" + DEFAULT_MASK);

        // Get all the fieldList where mask does not contain UPDATED_MASK
        defaultFieldShouldBeFound("mask.doesNotContain=" + UPDATED_MASK);
    }

    @Test
    @Transactional
    void getAllFieldsByMaskSymbolIsEqualToSomething() throws Exception {
        // Initialize the database
        fieldRepository.saveAndFlush(field);

        // Get all the fieldList where maskSymbol equals to DEFAULT_MASK_SYMBOL
        defaultFieldShouldBeFound("maskSymbol.equals=" + DEFAULT_MASK_SYMBOL);

        // Get all the fieldList where maskSymbol equals to UPDATED_MASK_SYMBOL
        defaultFieldShouldNotBeFound("maskSymbol.equals=" + UPDATED_MASK_SYMBOL);
    }

    @Test
    @Transactional
    void getAllFieldsByMaskSymbolIsNotEqualToSomething() throws Exception {
        // Initialize the database
        fieldRepository.saveAndFlush(field);

        // Get all the fieldList where maskSymbol not equals to DEFAULT_MASK_SYMBOL
        defaultFieldShouldNotBeFound("maskSymbol.notEquals=" + DEFAULT_MASK_SYMBOL);

        // Get all the fieldList where maskSymbol not equals to UPDATED_MASK_SYMBOL
        defaultFieldShouldBeFound("maskSymbol.notEquals=" + UPDATED_MASK_SYMBOL);
    }

    @Test
    @Transactional
    void getAllFieldsByMaskSymbolIsInShouldWork() throws Exception {
        // Initialize the database
        fieldRepository.saveAndFlush(field);

        // Get all the fieldList where maskSymbol in DEFAULT_MASK_SYMBOL or UPDATED_MASK_SYMBOL
        defaultFieldShouldBeFound("maskSymbol.in=" + DEFAULT_MASK_SYMBOL + "," + UPDATED_MASK_SYMBOL);

        // Get all the fieldList where maskSymbol equals to UPDATED_MASK_SYMBOL
        defaultFieldShouldNotBeFound("maskSymbol.in=" + UPDATED_MASK_SYMBOL);
    }

    @Test
    @Transactional
    void getAllFieldsByMaskSymbolIsNullOrNotNull() throws Exception {
        // Initialize the database
        fieldRepository.saveAndFlush(field);

        // Get all the fieldList where maskSymbol is not null
        defaultFieldShouldBeFound("maskSymbol.specified=true");

        // Get all the fieldList where maskSymbol is null
        defaultFieldShouldNotBeFound("maskSymbol.specified=false");
    }

    @Test
    @Transactional
    void getAllFieldsByMaskSymbolContainsSomething() throws Exception {
        // Initialize the database
        fieldRepository.saveAndFlush(field);

        // Get all the fieldList where maskSymbol contains DEFAULT_MASK_SYMBOL
        defaultFieldShouldBeFound("maskSymbol.contains=" + DEFAULT_MASK_SYMBOL);

        // Get all the fieldList where maskSymbol contains UPDATED_MASK_SYMBOL
        defaultFieldShouldNotBeFound("maskSymbol.contains=" + UPDATED_MASK_SYMBOL);
    }

    @Test
    @Transactional
    void getAllFieldsByMaskSymbolNotContainsSomething() throws Exception {
        // Initialize the database
        fieldRepository.saveAndFlush(field);

        // Get all the fieldList where maskSymbol does not contain DEFAULT_MASK_SYMBOL
        defaultFieldShouldNotBeFound("maskSymbol.doesNotContain=" + DEFAULT_MASK_SYMBOL);

        // Get all the fieldList where maskSymbol does not contain UPDATED_MASK_SYMBOL
        defaultFieldShouldBeFound("maskSymbol.doesNotContain=" + UPDATED_MASK_SYMBOL);
    }

    @Test
    @Transactional
    void getAllFieldsByLengthMinIsEqualToSomething() throws Exception {
        // Initialize the database
        fieldRepository.saveAndFlush(field);

        // Get all the fieldList where lengthMin equals to DEFAULT_LENGTH_MIN
        defaultFieldShouldBeFound("lengthMin.equals=" + DEFAULT_LENGTH_MIN);

        // Get all the fieldList where lengthMin equals to UPDATED_LENGTH_MIN
        defaultFieldShouldNotBeFound("lengthMin.equals=" + UPDATED_LENGTH_MIN);
    }

    @Test
    @Transactional
    void getAllFieldsByLengthMinIsNotEqualToSomething() throws Exception {
        // Initialize the database
        fieldRepository.saveAndFlush(field);

        // Get all the fieldList where lengthMin not equals to DEFAULT_LENGTH_MIN
        defaultFieldShouldNotBeFound("lengthMin.notEquals=" + DEFAULT_LENGTH_MIN);

        // Get all the fieldList where lengthMin not equals to UPDATED_LENGTH_MIN
        defaultFieldShouldBeFound("lengthMin.notEquals=" + UPDATED_LENGTH_MIN);
    }

    @Test
    @Transactional
    void getAllFieldsByLengthMinIsInShouldWork() throws Exception {
        // Initialize the database
        fieldRepository.saveAndFlush(field);

        // Get all the fieldList where lengthMin in DEFAULT_LENGTH_MIN or UPDATED_LENGTH_MIN
        defaultFieldShouldBeFound("lengthMin.in=" + DEFAULT_LENGTH_MIN + "," + UPDATED_LENGTH_MIN);

        // Get all the fieldList where lengthMin equals to UPDATED_LENGTH_MIN
        defaultFieldShouldNotBeFound("lengthMin.in=" + UPDATED_LENGTH_MIN);
    }

    @Test
    @Transactional
    void getAllFieldsByLengthMinIsNullOrNotNull() throws Exception {
        // Initialize the database
        fieldRepository.saveAndFlush(field);

        // Get all the fieldList where lengthMin is not null
        defaultFieldShouldBeFound("lengthMin.specified=true");

        // Get all the fieldList where lengthMin is null
        defaultFieldShouldNotBeFound("lengthMin.specified=false");
    }

    @Test
    @Transactional
    void getAllFieldsByLengthMinIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        fieldRepository.saveAndFlush(field);

        // Get all the fieldList where lengthMin is greater than or equal to DEFAULT_LENGTH_MIN
        defaultFieldShouldBeFound("lengthMin.greaterThanOrEqual=" + DEFAULT_LENGTH_MIN);

        // Get all the fieldList where lengthMin is greater than or equal to (DEFAULT_LENGTH_MIN + 1)
        defaultFieldShouldNotBeFound("lengthMin.greaterThanOrEqual=" + (DEFAULT_LENGTH_MIN + 1));
    }

    @Test
    @Transactional
    void getAllFieldsByLengthMinIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        fieldRepository.saveAndFlush(field);

        // Get all the fieldList where lengthMin is less than or equal to DEFAULT_LENGTH_MIN
        defaultFieldShouldBeFound("lengthMin.lessThanOrEqual=" + DEFAULT_LENGTH_MIN);

        // Get all the fieldList where lengthMin is less than or equal to SMALLER_LENGTH_MIN
        defaultFieldShouldNotBeFound("lengthMin.lessThanOrEqual=" + SMALLER_LENGTH_MIN);
    }

    @Test
    @Transactional
    void getAllFieldsByLengthMinIsLessThanSomething() throws Exception {
        // Initialize the database
        fieldRepository.saveAndFlush(field);

        // Get all the fieldList where lengthMin is less than DEFAULT_LENGTH_MIN
        defaultFieldShouldNotBeFound("lengthMin.lessThan=" + DEFAULT_LENGTH_MIN);

        // Get all the fieldList where lengthMin is less than (DEFAULT_LENGTH_MIN + 1)
        defaultFieldShouldBeFound("lengthMin.lessThan=" + (DEFAULT_LENGTH_MIN + 1));
    }

    @Test
    @Transactional
    void getAllFieldsByLengthMinIsGreaterThanSomething() throws Exception {
        // Initialize the database
        fieldRepository.saveAndFlush(field);

        // Get all the fieldList where lengthMin is greater than DEFAULT_LENGTH_MIN
        defaultFieldShouldNotBeFound("lengthMin.greaterThan=" + DEFAULT_LENGTH_MIN);

        // Get all the fieldList where lengthMin is greater than SMALLER_LENGTH_MIN
        defaultFieldShouldBeFound("lengthMin.greaterThan=" + SMALLER_LENGTH_MIN);
    }

    @Test
    @Transactional
    void getAllFieldsByLengthMaxIsEqualToSomething() throws Exception {
        // Initialize the database
        fieldRepository.saveAndFlush(field);

        // Get all the fieldList where lengthMax equals to DEFAULT_LENGTH_MAX
        defaultFieldShouldBeFound("lengthMax.equals=" + DEFAULT_LENGTH_MAX);

        // Get all the fieldList where lengthMax equals to UPDATED_LENGTH_MAX
        defaultFieldShouldNotBeFound("lengthMax.equals=" + UPDATED_LENGTH_MAX);
    }

    @Test
    @Transactional
    void getAllFieldsByLengthMaxIsNotEqualToSomething() throws Exception {
        // Initialize the database
        fieldRepository.saveAndFlush(field);

        // Get all the fieldList where lengthMax not equals to DEFAULT_LENGTH_MAX
        defaultFieldShouldNotBeFound("lengthMax.notEquals=" + DEFAULT_LENGTH_MAX);

        // Get all the fieldList where lengthMax not equals to UPDATED_LENGTH_MAX
        defaultFieldShouldBeFound("lengthMax.notEquals=" + UPDATED_LENGTH_MAX);
    }

    @Test
    @Transactional
    void getAllFieldsByLengthMaxIsInShouldWork() throws Exception {
        // Initialize the database
        fieldRepository.saveAndFlush(field);

        // Get all the fieldList where lengthMax in DEFAULT_LENGTH_MAX or UPDATED_LENGTH_MAX
        defaultFieldShouldBeFound("lengthMax.in=" + DEFAULT_LENGTH_MAX + "," + UPDATED_LENGTH_MAX);

        // Get all the fieldList where lengthMax equals to UPDATED_LENGTH_MAX
        defaultFieldShouldNotBeFound("lengthMax.in=" + UPDATED_LENGTH_MAX);
    }

    @Test
    @Transactional
    void getAllFieldsByLengthMaxIsNullOrNotNull() throws Exception {
        // Initialize the database
        fieldRepository.saveAndFlush(field);

        // Get all the fieldList where lengthMax is not null
        defaultFieldShouldBeFound("lengthMax.specified=true");

        // Get all the fieldList where lengthMax is null
        defaultFieldShouldNotBeFound("lengthMax.specified=false");
    }

    @Test
    @Transactional
    void getAllFieldsByLengthMaxIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        fieldRepository.saveAndFlush(field);

        // Get all the fieldList where lengthMax is greater than or equal to DEFAULT_LENGTH_MAX
        defaultFieldShouldBeFound("lengthMax.greaterThanOrEqual=" + DEFAULT_LENGTH_MAX);

        // Get all the fieldList where lengthMax is greater than or equal to (DEFAULT_LENGTH_MAX + 1)
        defaultFieldShouldNotBeFound("lengthMax.greaterThanOrEqual=" + (DEFAULT_LENGTH_MAX + 1));
    }

    @Test
    @Transactional
    void getAllFieldsByLengthMaxIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        fieldRepository.saveAndFlush(field);

        // Get all the fieldList where lengthMax is less than or equal to DEFAULT_LENGTH_MAX
        defaultFieldShouldBeFound("lengthMax.lessThanOrEqual=" + DEFAULT_LENGTH_MAX);

        // Get all the fieldList where lengthMax is less than or equal to SMALLER_LENGTH_MAX
        defaultFieldShouldNotBeFound("lengthMax.lessThanOrEqual=" + SMALLER_LENGTH_MAX);
    }

    @Test
    @Transactional
    void getAllFieldsByLengthMaxIsLessThanSomething() throws Exception {
        // Initialize the database
        fieldRepository.saveAndFlush(field);

        // Get all the fieldList where lengthMax is less than DEFAULT_LENGTH_MAX
        defaultFieldShouldNotBeFound("lengthMax.lessThan=" + DEFAULT_LENGTH_MAX);

        // Get all the fieldList where lengthMax is less than (DEFAULT_LENGTH_MAX + 1)
        defaultFieldShouldBeFound("lengthMax.lessThan=" + (DEFAULT_LENGTH_MAX + 1));
    }

    @Test
    @Transactional
    void getAllFieldsByLengthMaxIsGreaterThanSomething() throws Exception {
        // Initialize the database
        fieldRepository.saveAndFlush(field);

        // Get all the fieldList where lengthMax is greater than DEFAULT_LENGTH_MAX
        defaultFieldShouldNotBeFound("lengthMax.greaterThan=" + DEFAULT_LENGTH_MAX);

        // Get all the fieldList where lengthMax is greater than SMALLER_LENGTH_MAX
        defaultFieldShouldBeFound("lengthMax.greaterThan=" + SMALLER_LENGTH_MAX);
    }

    @Test
    @Transactional
    void getAllFieldsByRequireFieldIsEqualToSomething() throws Exception {
        // Initialize the database
        fieldRepository.saveAndFlush(field);

        // Get all the fieldList where requireField equals to DEFAULT_REQUIRE_FIELD
        defaultFieldShouldBeFound("requireField.equals=" + DEFAULT_REQUIRE_FIELD);

        // Get all the fieldList where requireField equals to UPDATED_REQUIRE_FIELD
        defaultFieldShouldNotBeFound("requireField.equals=" + UPDATED_REQUIRE_FIELD);
    }

    @Test
    @Transactional
    void getAllFieldsByRequireFieldIsNotEqualToSomething() throws Exception {
        // Initialize the database
        fieldRepository.saveAndFlush(field);

        // Get all the fieldList where requireField not equals to DEFAULT_REQUIRE_FIELD
        defaultFieldShouldNotBeFound("requireField.notEquals=" + DEFAULT_REQUIRE_FIELD);

        // Get all the fieldList where requireField not equals to UPDATED_REQUIRE_FIELD
        defaultFieldShouldBeFound("requireField.notEquals=" + UPDATED_REQUIRE_FIELD);
    }

    @Test
    @Transactional
    void getAllFieldsByRequireFieldIsInShouldWork() throws Exception {
        // Initialize the database
        fieldRepository.saveAndFlush(field);

        // Get all the fieldList where requireField in DEFAULT_REQUIRE_FIELD or UPDATED_REQUIRE_FIELD
        defaultFieldShouldBeFound("requireField.in=" + DEFAULT_REQUIRE_FIELD + "," + UPDATED_REQUIRE_FIELD);

        // Get all the fieldList where requireField equals to UPDATED_REQUIRE_FIELD
        defaultFieldShouldNotBeFound("requireField.in=" + UPDATED_REQUIRE_FIELD);
    }

    @Test
    @Transactional
    void getAllFieldsByRequireFieldIsNullOrNotNull() throws Exception {
        // Initialize the database
        fieldRepository.saveAndFlush(field);

        // Get all the fieldList where requireField is not null
        defaultFieldShouldBeFound("requireField.specified=true");

        // Get all the fieldList where requireField is null
        defaultFieldShouldNotBeFound("requireField.specified=false");
    }

    @Test
    @Transactional
    void getAllFieldsByRequireFieldContainsSomething() throws Exception {
        // Initialize the database
        fieldRepository.saveAndFlush(field);

        // Get all the fieldList where requireField contains DEFAULT_REQUIRE_FIELD
        defaultFieldShouldBeFound("requireField.contains=" + DEFAULT_REQUIRE_FIELD);

        // Get all the fieldList where requireField contains UPDATED_REQUIRE_FIELD
        defaultFieldShouldNotBeFound("requireField.contains=" + UPDATED_REQUIRE_FIELD);
    }

    @Test
    @Transactional
    void getAllFieldsByRequireFieldNotContainsSomething() throws Exception {
        // Initialize the database
        fieldRepository.saveAndFlush(field);

        // Get all the fieldList where requireField does not contain DEFAULT_REQUIRE_FIELD
        defaultFieldShouldNotBeFound("requireField.doesNotContain=" + DEFAULT_REQUIRE_FIELD);

        // Get all the fieldList where requireField does not contain UPDATED_REQUIRE_FIELD
        defaultFieldShouldBeFound("requireField.doesNotContain=" + UPDATED_REQUIRE_FIELD);
    }

    @Test
    @Transactional
    void getAllFieldsByFilterByIsEqualToSomething() throws Exception {
        // Initialize the database
        fieldRepository.saveAndFlush(field);

        // Get all the fieldList where filterBy equals to DEFAULT_FILTER_BY
        defaultFieldShouldBeFound("filterBy.equals=" + DEFAULT_FILTER_BY);

        // Get all the fieldList where filterBy equals to UPDATED_FILTER_BY
        defaultFieldShouldNotBeFound("filterBy.equals=" + UPDATED_FILTER_BY);
    }

    @Test
    @Transactional
    void getAllFieldsByFilterByIsNotEqualToSomething() throws Exception {
        // Initialize the database
        fieldRepository.saveAndFlush(field);

        // Get all the fieldList where filterBy not equals to DEFAULT_FILTER_BY
        defaultFieldShouldNotBeFound("filterBy.notEquals=" + DEFAULT_FILTER_BY);

        // Get all the fieldList where filterBy not equals to UPDATED_FILTER_BY
        defaultFieldShouldBeFound("filterBy.notEquals=" + UPDATED_FILTER_BY);
    }

    @Test
    @Transactional
    void getAllFieldsByFilterByIsInShouldWork() throws Exception {
        // Initialize the database
        fieldRepository.saveAndFlush(field);

        // Get all the fieldList where filterBy in DEFAULT_FILTER_BY or UPDATED_FILTER_BY
        defaultFieldShouldBeFound("filterBy.in=" + DEFAULT_FILTER_BY + "," + UPDATED_FILTER_BY);

        // Get all the fieldList where filterBy equals to UPDATED_FILTER_BY
        defaultFieldShouldNotBeFound("filterBy.in=" + UPDATED_FILTER_BY);
    }

    @Test
    @Transactional
    void getAllFieldsByFilterByIsNullOrNotNull() throws Exception {
        // Initialize the database
        fieldRepository.saveAndFlush(field);

        // Get all the fieldList where filterBy is not null
        defaultFieldShouldBeFound("filterBy.specified=true");

        // Get all the fieldList where filterBy is null
        defaultFieldShouldNotBeFound("filterBy.specified=false");
    }

    @Test
    @Transactional
    void getAllFieldsByFilterByContainsSomething() throws Exception {
        // Initialize the database
        fieldRepository.saveAndFlush(field);

        // Get all the fieldList where filterBy contains DEFAULT_FILTER_BY
        defaultFieldShouldBeFound("filterBy.contains=" + DEFAULT_FILTER_BY);

        // Get all the fieldList where filterBy contains UPDATED_FILTER_BY
        defaultFieldShouldNotBeFound("filterBy.contains=" + UPDATED_FILTER_BY);
    }

    @Test
    @Transactional
    void getAllFieldsByFilterByNotContainsSomething() throws Exception {
        // Initialize the database
        fieldRepository.saveAndFlush(field);

        // Get all the fieldList where filterBy does not contain DEFAULT_FILTER_BY
        defaultFieldShouldNotBeFound("filterBy.doesNotContain=" + DEFAULT_FILTER_BY);

        // Get all the fieldList where filterBy does not contain UPDATED_FILTER_BY
        defaultFieldShouldBeFound("filterBy.doesNotContain=" + UPDATED_FILTER_BY);
    }

    @Test
    @Transactional
    void getAllFieldsByOptionalIsEqualToSomething() throws Exception {
        // Initialize the database
        fieldRepository.saveAndFlush(field);

        // Get all the fieldList where optional equals to DEFAULT_OPTIONAL
        defaultFieldShouldBeFound("optional.equals=" + DEFAULT_OPTIONAL);

        // Get all the fieldList where optional equals to UPDATED_OPTIONAL
        defaultFieldShouldNotBeFound("optional.equals=" + UPDATED_OPTIONAL);
    }

    @Test
    @Transactional
    void getAllFieldsByOptionalIsNotEqualToSomething() throws Exception {
        // Initialize the database
        fieldRepository.saveAndFlush(field);

        // Get all the fieldList where optional not equals to DEFAULT_OPTIONAL
        defaultFieldShouldNotBeFound("optional.notEquals=" + DEFAULT_OPTIONAL);

        // Get all the fieldList where optional not equals to UPDATED_OPTIONAL
        defaultFieldShouldBeFound("optional.notEquals=" + UPDATED_OPTIONAL);
    }

    @Test
    @Transactional
    void getAllFieldsByOptionalIsInShouldWork() throws Exception {
        // Initialize the database
        fieldRepository.saveAndFlush(field);

        // Get all the fieldList where optional in DEFAULT_OPTIONAL or UPDATED_OPTIONAL
        defaultFieldShouldBeFound("optional.in=" + DEFAULT_OPTIONAL + "," + UPDATED_OPTIONAL);

        // Get all the fieldList where optional equals to UPDATED_OPTIONAL
        defaultFieldShouldNotBeFound("optional.in=" + UPDATED_OPTIONAL);
    }

    @Test
    @Transactional
    void getAllFieldsByOptionalIsNullOrNotNull() throws Exception {
        // Initialize the database
        fieldRepository.saveAndFlush(field);

        // Get all the fieldList where optional is not null
        defaultFieldShouldBeFound("optional.specified=true");

        // Get all the fieldList where optional is null
        defaultFieldShouldNotBeFound("optional.specified=false");
    }

    @Test
    @Transactional
    void getAllFieldsByOutputIsEqualToSomething() throws Exception {
        // Initialize the database
        fieldRepository.saveAndFlush(field);

        // Get all the fieldList where output equals to DEFAULT_OUTPUT
        defaultFieldShouldBeFound("output.equals=" + DEFAULT_OUTPUT);

        // Get all the fieldList where output equals to UPDATED_OUTPUT
        defaultFieldShouldNotBeFound("output.equals=" + UPDATED_OUTPUT);
    }

    @Test
    @Transactional
    void getAllFieldsByOutputIsNotEqualToSomething() throws Exception {
        // Initialize the database
        fieldRepository.saveAndFlush(field);

        // Get all the fieldList where output not equals to DEFAULT_OUTPUT
        defaultFieldShouldNotBeFound("output.notEquals=" + DEFAULT_OUTPUT);

        // Get all the fieldList where output not equals to UPDATED_OUTPUT
        defaultFieldShouldBeFound("output.notEquals=" + UPDATED_OUTPUT);
    }

    @Test
    @Transactional
    void getAllFieldsByOutputIsInShouldWork() throws Exception {
        // Initialize the database
        fieldRepository.saveAndFlush(field);

        // Get all the fieldList where output in DEFAULT_OUTPUT or UPDATED_OUTPUT
        defaultFieldShouldBeFound("output.in=" + DEFAULT_OUTPUT + "," + UPDATED_OUTPUT);

        // Get all the fieldList where output equals to UPDATED_OUTPUT
        defaultFieldShouldNotBeFound("output.in=" + UPDATED_OUTPUT);
    }

    @Test
    @Transactional
    void getAllFieldsByOutputIsNullOrNotNull() throws Exception {
        // Initialize the database
        fieldRepository.saveAndFlush(field);

        // Get all the fieldList where output is not null
        defaultFieldShouldBeFound("output.specified=true");

        // Get all the fieldList where output is null
        defaultFieldShouldNotBeFound("output.specified=false");
    }

    @Test
    @Transactional
    void getAllFieldsByVisibilityIsEqualToSomething() throws Exception {
        // Initialize the database
        fieldRepository.saveAndFlush(field);

        // Get all the fieldList where visibility equals to DEFAULT_VISIBILITY
        defaultFieldShouldBeFound("visibility.equals=" + DEFAULT_VISIBILITY);

        // Get all the fieldList where visibility equals to UPDATED_VISIBILITY
        defaultFieldShouldNotBeFound("visibility.equals=" + UPDATED_VISIBILITY);
    }

    @Test
    @Transactional
    void getAllFieldsByVisibilityIsNotEqualToSomething() throws Exception {
        // Initialize the database
        fieldRepository.saveAndFlush(field);

        // Get all the fieldList where visibility not equals to DEFAULT_VISIBILITY
        defaultFieldShouldNotBeFound("visibility.notEquals=" + DEFAULT_VISIBILITY);

        // Get all the fieldList where visibility not equals to UPDATED_VISIBILITY
        defaultFieldShouldBeFound("visibility.notEquals=" + UPDATED_VISIBILITY);
    }

    @Test
    @Transactional
    void getAllFieldsByVisibilityIsInShouldWork() throws Exception {
        // Initialize the database
        fieldRepository.saveAndFlush(field);

        // Get all the fieldList where visibility in DEFAULT_VISIBILITY or UPDATED_VISIBILITY
        defaultFieldShouldBeFound("visibility.in=" + DEFAULT_VISIBILITY + "," + UPDATED_VISIBILITY);

        // Get all the fieldList where visibility equals to UPDATED_VISIBILITY
        defaultFieldShouldNotBeFound("visibility.in=" + UPDATED_VISIBILITY);
    }

    @Test
    @Transactional
    void getAllFieldsByVisibilityIsNullOrNotNull() throws Exception {
        // Initialize the database
        fieldRepository.saveAndFlush(field);

        // Get all the fieldList where visibility is not null
        defaultFieldShouldBeFound("visibility.specified=true");

        // Get all the fieldList where visibility is null
        defaultFieldShouldNotBeFound("visibility.specified=false");
    }

    @Test
    @Transactional
    void getAllFieldsByOrdIsEqualToSomething() throws Exception {
        // Initialize the database
        fieldRepository.saveAndFlush(field);

        // Get all the fieldList where ord equals to DEFAULT_ORD
        defaultFieldShouldBeFound("ord.equals=" + DEFAULT_ORD);

        // Get all the fieldList where ord equals to UPDATED_ORD
        defaultFieldShouldNotBeFound("ord.equals=" + UPDATED_ORD);
    }

    @Test
    @Transactional
    void getAllFieldsByOrdIsNotEqualToSomething() throws Exception {
        // Initialize the database
        fieldRepository.saveAndFlush(field);

        // Get all the fieldList where ord not equals to DEFAULT_ORD
        defaultFieldShouldNotBeFound("ord.notEquals=" + DEFAULT_ORD);

        // Get all the fieldList where ord not equals to UPDATED_ORD
        defaultFieldShouldBeFound("ord.notEquals=" + UPDATED_ORD);
    }

    @Test
    @Transactional
    void getAllFieldsByOrdIsInShouldWork() throws Exception {
        // Initialize the database
        fieldRepository.saveAndFlush(field);

        // Get all the fieldList where ord in DEFAULT_ORD or UPDATED_ORD
        defaultFieldShouldBeFound("ord.in=" + DEFAULT_ORD + "," + UPDATED_ORD);

        // Get all the fieldList where ord equals to UPDATED_ORD
        defaultFieldShouldNotBeFound("ord.in=" + UPDATED_ORD);
    }

    @Test
    @Transactional
    void getAllFieldsByOrdIsNullOrNotNull() throws Exception {
        // Initialize the database
        fieldRepository.saveAndFlush(field);

        // Get all the fieldList where ord is not null
        defaultFieldShouldBeFound("ord.specified=true");

        // Get all the fieldList where ord is null
        defaultFieldShouldNotBeFound("ord.specified=false");
    }

    @Test
    @Transactional
    void getAllFieldsByOrdIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        fieldRepository.saveAndFlush(field);

        // Get all the fieldList where ord is greater than or equal to DEFAULT_ORD
        defaultFieldShouldBeFound("ord.greaterThanOrEqual=" + DEFAULT_ORD);

        // Get all the fieldList where ord is greater than or equal to UPDATED_ORD
        defaultFieldShouldNotBeFound("ord.greaterThanOrEqual=" + UPDATED_ORD);
    }

    @Test
    @Transactional
    void getAllFieldsByOrdIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        fieldRepository.saveAndFlush(field);

        // Get all the fieldList where ord is less than or equal to DEFAULT_ORD
        defaultFieldShouldBeFound("ord.lessThanOrEqual=" + DEFAULT_ORD);

        // Get all the fieldList where ord is less than or equal to SMALLER_ORD
        defaultFieldShouldNotBeFound("ord.lessThanOrEqual=" + SMALLER_ORD);
    }

    @Test
    @Transactional
    void getAllFieldsByOrdIsLessThanSomething() throws Exception {
        // Initialize the database
        fieldRepository.saveAndFlush(field);

        // Get all the fieldList where ord is less than DEFAULT_ORD
        defaultFieldShouldNotBeFound("ord.lessThan=" + DEFAULT_ORD);

        // Get all the fieldList where ord is less than UPDATED_ORD
        defaultFieldShouldBeFound("ord.lessThan=" + UPDATED_ORD);
    }

    @Test
    @Transactional
    void getAllFieldsByOrdIsGreaterThanSomething() throws Exception {
        // Initialize the database
        fieldRepository.saveAndFlush(field);

        // Get all the fieldList where ord is greater than DEFAULT_ORD
        defaultFieldShouldNotBeFound("ord.greaterThan=" + DEFAULT_ORD);

        // Get all the fieldList where ord is greater than SMALLER_ORD
        defaultFieldShouldBeFound("ord.greaterThan=" + SMALLER_ORD);
    }

    @Test
    @Transactional
    void getAllFieldsByRequestTypeIsEqualToSomething() throws Exception {
        // Initialize the database
        fieldRepository.saveAndFlush(field);

        // Get all the fieldList where requestType equals to DEFAULT_REQUEST_TYPE
        defaultFieldShouldBeFound("requestType.equals=" + DEFAULT_REQUEST_TYPE);

        // Get all the fieldList where requestType equals to UPDATED_REQUEST_TYPE
        defaultFieldShouldNotBeFound("requestType.equals=" + UPDATED_REQUEST_TYPE);
    }

    @Test
    @Transactional
    void getAllFieldsByRequestTypeIsNotEqualToSomething() throws Exception {
        // Initialize the database
        fieldRepository.saveAndFlush(field);

        // Get all the fieldList where requestType not equals to DEFAULT_REQUEST_TYPE
        defaultFieldShouldNotBeFound("requestType.notEquals=" + DEFAULT_REQUEST_TYPE);

        // Get all the fieldList where requestType not equals to UPDATED_REQUEST_TYPE
        defaultFieldShouldBeFound("requestType.notEquals=" + UPDATED_REQUEST_TYPE);
    }

    @Test
    @Transactional
    void getAllFieldsByRequestTypeIsInShouldWork() throws Exception {
        // Initialize the database
        fieldRepository.saveAndFlush(field);

        // Get all the fieldList where requestType in DEFAULT_REQUEST_TYPE or UPDATED_REQUEST_TYPE
        defaultFieldShouldBeFound("requestType.in=" + DEFAULT_REQUEST_TYPE + "," + UPDATED_REQUEST_TYPE);

        // Get all the fieldList where requestType equals to UPDATED_REQUEST_TYPE
        defaultFieldShouldNotBeFound("requestType.in=" + UPDATED_REQUEST_TYPE);
    }

    @Test
    @Transactional
    void getAllFieldsByRequestTypeIsNullOrNotNull() throws Exception {
        // Initialize the database
        fieldRepository.saveAndFlush(field);

        // Get all the fieldList where requestType is not null
        defaultFieldShouldBeFound("requestType.specified=true");

        // Get all the fieldList where requestType is null
        defaultFieldShouldNotBeFound("requestType.specified=false");
    }

    @Test
    @Transactional
    void getAllFieldsByRecipientIsEqualToSomething() throws Exception {
        // Initialize the database
        fieldRepository.saveAndFlush(field);
        Recipient recipient = RecipientResourceIT.createEntity(em);
        em.persist(recipient);
        em.flush();
        field.setRecipient(recipient);
        fieldRepository.saveAndFlush(field);
        Long recipientId = recipient.getId();

        // Get all the fieldList where recipient equals to recipientId
        defaultFieldShouldBeFound("recipientId.equals=" + recipientId);

        // Get all the fieldList where recipient equals to (recipientId + 1)
        defaultFieldShouldNotBeFound("recipientId.equals=" + (recipientId + 1));
    }

    @Test
    @Transactional
    void getAllFieldsByTitleIsEqualToSomething() throws Exception {
        // Initialize the database
        fieldRepository.saveAndFlush(field);
        I18n title = I18nResourceIT.createEntity(em);
        em.persist(title);
        em.flush();
        field.addTitle(title);
        fieldRepository.saveAndFlush(field);
        Long titleId = title.getId();

        // Get all the fieldList where title equals to titleId
        defaultFieldShouldBeFound("titleId.equals=" + titleId);

        // Get all the fieldList where title equals to (titleId + 1)
        defaultFieldShouldNotBeFound("titleId.equals=" + (titleId + 1));
    }

    @Test
    @Transactional
    void getAllFieldsByDescriptionIsEqualToSomething() throws Exception {
        // Initialize the database
        fieldRepository.saveAndFlush(field);
        I18n description = I18nResourceIT.createEntity(em);
        em.persist(description);
        em.flush();
        field.addDescription(description);
        fieldRepository.saveAndFlush(field);
        Long descriptionId = description.getId();

        // Get all the fieldList where description equals to descriptionId
        defaultFieldShouldBeFound("descriptionId.equals=" + descriptionId);

        // Get all the fieldList where description equals to (descriptionId + 1)
        defaultFieldShouldNotBeFound("descriptionId.equals=" + (descriptionId + 1));
    }

    @Test
    @Transactional
    void getAllFieldsByValueIsEqualToSomething() throws Exception {
        // Initialize the database
        fieldRepository.saveAndFlush(field);
        FieldValue value = FieldValueResourceIT.createEntity(em);
        em.persist(value);
        em.flush();
        field.addValue(value);
        fieldRepository.saveAndFlush(field);
        Long valueId = value.getId();

        // Get all the fieldList where value equals to valueId
        defaultFieldShouldBeFound("valueId.equals=" + valueId);

        // Get all the fieldList where value equals to (valueId + 1)
        defaultFieldShouldNotBeFound("valueId.equals=" + (valueId + 1));
    }

    @Test
    @Transactional
    void getAllFieldsByPrefixIsEqualToSomething() throws Exception {
        // Initialize the database
        fieldRepository.saveAndFlush(field);
        Prefix prefix = PrefixResourceIT.createEntity(em);
        em.persist(prefix);
        em.flush();
        field.addPrefix(prefix);
        fieldRepository.saveAndFlush(field);
        Long prefixId = prefix.getId();

        // Get all the fieldList where prefix equals to prefixId
        defaultFieldShouldBeFound("prefixId.equals=" + prefixId);

        // Get all the fieldList where prefix equals to (prefixId + 1)
        defaultFieldShouldNotBeFound("prefixId.equals=" + (prefixId + 1));
    }

    /**
     * Executes the search, and checks that the default entity is returned.
     */
    private void defaultFieldShouldBeFound(String filter) throws Exception {
        restFieldMockMvc
                .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.[*].id").value(hasItem(field.getId().intValue())))
                .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME)))
                .andExpect(jsonPath("$.[*].extId").value(hasItem(DEFAULT_EXT_ID.intValue())))
                .andExpect(jsonPath("$.[*].type").value(hasItem(DEFAULT_TYPE.toString())))
                .andExpect(jsonPath("$.[*].controlType").value(hasItem(DEFAULT_CONTROL_TYPE.toString())))
                .andExpect(jsonPath("$.[*].content").value(hasItem(DEFAULT_CONTENT)))
                .andExpect(jsonPath("$.[*].validation").value(hasItem(DEFAULT_VALIDATION)))
                .andExpect(jsonPath("$.[*].placeholder").value(hasItem(DEFAULT_PLACEHOLDER)))
                .andExpect(jsonPath("$.[*].mask").value(hasItem(DEFAULT_MASK)))
                .andExpect(jsonPath("$.[*].maskSymbol").value(hasItem(DEFAULT_MASK_SYMBOL)))
                .andExpect(jsonPath("$.[*].lengthMin").value(hasItem(DEFAULT_LENGTH_MIN)))
                .andExpect(jsonPath("$.[*].lengthMax").value(hasItem(DEFAULT_LENGTH_MAX)))
                .andExpect(jsonPath("$.[*].requireField").value(hasItem(DEFAULT_REQUIRE_FIELD)))
                .andExpect(jsonPath("$.[*].filterBy").value(hasItem(DEFAULT_FILTER_BY)))
                .andExpect(jsonPath("$.[*].optional").value(hasItem(DEFAULT_OPTIONAL.booleanValue())))
                .andExpect(jsonPath("$.[*].output").value(hasItem(DEFAULT_OUTPUT.booleanValue())))
                .andExpect(jsonPath("$.[*].visibility").value(hasItem(DEFAULT_VISIBILITY.toString())))
                .andExpect(jsonPath("$.[*].ord").value(hasItem(DEFAULT_ORD)))
                .andExpect(jsonPath("$.[*].requestType").value(hasItem(DEFAULT_REQUEST_TYPE.toString())));

        // Check, that the count call also returns 1
        restFieldMockMvc
                .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(content().string("1"));
    }

    /**
     * Executes the search, and checks that the default entity is not returned.
     */
    private void defaultFieldShouldNotBeFound(String filter) throws Exception {
        restFieldMockMvc
                .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());

        // Check, that the count call also returns 0
        restFieldMockMvc
                .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(content().string("0"));
    }

    @Test
    @Transactional
    void getNonExistingField() throws Exception {
        // Get the field
        restFieldMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putNewField() throws Exception {
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
                .perform(
                        put(ENTITY_API_URL_ID, fieldDTO.getId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(TestUtil.convertObjectToJsonBytes(fieldDTO))
                )
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
        assertThat(testField.getOptional()).isEqualTo(UPDATED_OPTIONAL);
        assertThat(testField.getOutput()).isEqualTo(UPDATED_OUTPUT);
        assertThat(testField.getVisibility()).isEqualTo(UPDATED_VISIBILITY);
        assertThat(testField.getOrd()).isEqualTo(UPDATED_ORD);
        assertThat(testField.getRequestType()).isEqualTo(UPDATED_REQUEST_TYPE);
    }

    @Test
    @Transactional
    void putNonExistingField() throws Exception {
        int databaseSizeBeforeUpdate = fieldRepository.findAll().size();
        field.setId(count.incrementAndGet());

        // Create the Field
        FieldDTO fieldDTO = fieldMapper.toDto(field);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restFieldMockMvc
                .perform(
                        put(ENTITY_API_URL_ID, fieldDTO.getId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(TestUtil.convertObjectToJsonBytes(fieldDTO))
                )
                .andExpect(status().isBadRequest());

        // Validate the Field in the database
        List<Field> fieldList = fieldRepository.findAll();
        assertThat(fieldList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchField() throws Exception {
        int databaseSizeBeforeUpdate = fieldRepository.findAll().size();
        field.setId(count.incrementAndGet());

        // Create the Field
        FieldDTO fieldDTO = fieldMapper.toDto(field);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restFieldMockMvc
                .perform(
                        put(ENTITY_API_URL_ID, count.incrementAndGet())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(TestUtil.convertObjectToJsonBytes(fieldDTO))
                )
                .andExpect(status().isBadRequest());

        // Validate the Field in the database
        List<Field> fieldList = fieldRepository.findAll();
        assertThat(fieldList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamField() throws Exception {
        int databaseSizeBeforeUpdate = fieldRepository.findAll().size();
        field.setId(count.incrementAndGet());

        // Create the Field
        FieldDTO fieldDTO = fieldMapper.toDto(field);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restFieldMockMvc
                .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(fieldDTO)))
                .andExpect(status().isMethodNotAllowed());

        // Validate the Field in the database
        List<Field> fieldList = fieldRepository.findAll();
        assertThat(fieldList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateFieldWithPatch() throws Exception {
        // Initialize the database
        fieldRepository.saveAndFlush(field);

        int databaseSizeBeforeUpdate = fieldRepository.findAll().size();

        // Update the field using partial update
        Field partialUpdatedField = new Field();
        partialUpdatedField.setId(field.getId());

        partialUpdatedField
                .placeholder(UPDATED_PLACEHOLDER)
                .mask(UPDATED_MASK)
                .filterBy(UPDATED_FILTER_BY)
                .output(UPDATED_OUTPUT)
                .ord(UPDATED_ORD);

        restFieldMockMvc
                .perform(
                        patch(ENTITY_API_URL_ID, partialUpdatedField.getId())
                                .contentType("application/merge-patch+json")
                                .content(TestUtil.convertObjectToJsonBytes(partialUpdatedField))
                )
                .andExpect(status().isOk());

        // Validate the Field in the database
        List<Field> fieldList = fieldRepository.findAll();
        assertThat(fieldList).hasSize(databaseSizeBeforeUpdate);
        Field testField = fieldList.get(fieldList.size() - 1);
        assertThat(testField.getName()).isEqualTo(DEFAULT_NAME);
        assertThat(testField.getExtId()).isEqualTo(DEFAULT_EXT_ID);
        assertThat(testField.getType()).isEqualTo(DEFAULT_TYPE);
        assertThat(testField.getControlType()).isEqualTo(DEFAULT_CONTROL_TYPE);
        assertThat(testField.getContent()).isEqualTo(DEFAULT_CONTENT);
        assertThat(testField.getValidation()).isEqualTo(DEFAULT_VALIDATION);
        assertThat(testField.getPlaceholder()).isEqualTo(UPDATED_PLACEHOLDER);
        assertThat(testField.getMask()).isEqualTo(UPDATED_MASK);
        assertThat(testField.getMaskSymbol()).isEqualTo(DEFAULT_MASK_SYMBOL);
        assertThat(testField.getLengthMin()).isEqualTo(DEFAULT_LENGTH_MIN);
        assertThat(testField.getLengthMax()).isEqualTo(DEFAULT_LENGTH_MAX);
        assertThat(testField.getRequireField()).isEqualTo(DEFAULT_REQUIRE_FIELD);
        assertThat(testField.getFilterBy()).isEqualTo(UPDATED_FILTER_BY);
        assertThat(testField.getOptional()).isEqualTo(DEFAULT_OPTIONAL);
        assertThat(testField.getOutput()).isEqualTo(UPDATED_OUTPUT);
        assertThat(testField.getVisibility()).isEqualTo(DEFAULT_VISIBILITY);
        assertThat(testField.getOrd()).isEqualTo(UPDATED_ORD);
        assertThat(testField.getRequestType()).isEqualTo(DEFAULT_REQUEST_TYPE);
    }

    @Test
    @Transactional
    void fullUpdateFieldWithPatch() throws Exception {
        // Initialize the database
        fieldRepository.saveAndFlush(field);

        int databaseSizeBeforeUpdate = fieldRepository.findAll().size();

        // Update the field using partial update
        Field partialUpdatedField = new Field();
        partialUpdatedField.setId(field.getId());

        partialUpdatedField
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

        restFieldMockMvc
                .perform(
                        patch(ENTITY_API_URL_ID, partialUpdatedField.getId())
                                .contentType("application/merge-patch+json")
                                .content(TestUtil.convertObjectToJsonBytes(partialUpdatedField))
                )
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
        assertThat(testField.getOptional()).isEqualTo(UPDATED_OPTIONAL);
        assertThat(testField.getOutput()).isEqualTo(UPDATED_OUTPUT);
        assertThat(testField.getVisibility()).isEqualTo(UPDATED_VISIBILITY);
        assertThat(testField.getOrd()).isEqualTo(UPDATED_ORD);
        assertThat(testField.getRequestType()).isEqualTo(UPDATED_REQUEST_TYPE);
    }

    @Test
    @Transactional
    void patchNonExistingField() throws Exception {
        int databaseSizeBeforeUpdate = fieldRepository.findAll().size();
        field.setId(count.incrementAndGet());

        // Create the Field
        FieldDTO fieldDTO = fieldMapper.toDto(field);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restFieldMockMvc
                .perform(
                        patch(ENTITY_API_URL_ID, fieldDTO.getId())
                                .contentType("application/merge-patch+json")
                                .content(TestUtil.convertObjectToJsonBytes(fieldDTO))
                )
                .andExpect(status().isBadRequest());

        // Validate the Field in the database
        List<Field> fieldList = fieldRepository.findAll();
        assertThat(fieldList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchField() throws Exception {
        int databaseSizeBeforeUpdate = fieldRepository.findAll().size();
        field.setId(count.incrementAndGet());

        // Create the Field
        FieldDTO fieldDTO = fieldMapper.toDto(field);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restFieldMockMvc
                .perform(
                        patch(ENTITY_API_URL_ID, count.incrementAndGet())
                                .contentType("application/merge-patch+json")
                                .content(TestUtil.convertObjectToJsonBytes(fieldDTO))
                )
                .andExpect(status().isBadRequest());

        // Validate the Field in the database
        List<Field> fieldList = fieldRepository.findAll();
        assertThat(fieldList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamField() throws Exception {
        int databaseSizeBeforeUpdate = fieldRepository.findAll().size();
        field.setId(count.incrementAndGet());

        // Create the Field
        FieldDTO fieldDTO = fieldMapper.toDto(field);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restFieldMockMvc
                .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(TestUtil.convertObjectToJsonBytes(fieldDTO)))
                .andExpect(status().isMethodNotAllowed());

        // Validate the Field in the database
        List<Field> fieldList = fieldRepository.findAll();
        assertThat(fieldList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteField() throws Exception {
        // Initialize the database
        fieldRepository.saveAndFlush(field);

        int databaseSizeBeforeDelete = fieldRepository.findAll().size();

        // Delete the field
        restFieldMockMvc
                .perform(delete(ENTITY_API_URL_ID, field.getId()).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        // Validate the database contains one less item
        List<Field> fieldList = fieldRepository.findAll();
        assertThat(fieldList).hasSize(databaseSizeBeforeDelete - 1);
    }
}
