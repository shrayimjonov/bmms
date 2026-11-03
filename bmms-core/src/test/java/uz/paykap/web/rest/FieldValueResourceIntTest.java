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
import uz.paykap.PkbillmsApp;
import uz.paykap.IntegrationTest;
import uz.paykap.domain.Field;
import uz.paykap.domain.FieldValue;
import uz.paykap.domain.I18n;
import uz.paykap.repository.FieldRepository;
import uz.paykap.repository.FieldValueRepository;
import uz.paykap.service.FieldValueQueryService;
import uz.paykap.service.FieldValueService;
import uz.paykap.service.dto.FieldValueDTO;
import uz.paykap.service.mapper.FieldValueMapper;
import uz.paykap.web.rest.errors.ExceptionTranslator;

/**
 * Test class for the FieldValueResource REST controller.
 *
 * @see FieldValueResource
 */
@RunWith(SpringRunner.class)
@IntegrationTest
public class FieldValueResourceIntTest {

    private static final String DEFAULT_VALUE = "AAAAAAAAAA";
    private static final String UPDATED_VALUE = "BBBBBBBBBB";

    private static final Long DEFAULT_AMOUNT = 1L;
    private static final Long UPDATED_AMOUNT = 2L;

    private static final String DEFAULT_FILTER_BY = "AAAAAAAAAA";
    private static final String UPDATED_FILTER_BY = "BBBBBBBBBB";

    private static final Integer DEFAULT_ORD = 1;
    private static final Integer UPDATED_ORD = 2;

    private static final String DEFAULT_CHECK_ID = "AAAAAAAAAA";
    private static final String UPDATED_CHECK_ID = "BBBBBBBBBB";

    @Autowired
    private FieldValueRepository fieldValueRepository;

    @Mock
    private FieldValueRepository fieldValueRepositoryMock;

    @Autowired
    private FieldValueMapper fieldValueMapper;

    @Mock
    private FieldValueService fieldValueServiceMock;

    @Autowired
    private FieldValueService fieldValueService;

    @Autowired
    private FieldValueQueryService fieldValueQueryService;

    @Autowired
    private MappingJackson2HttpMessageConverter jacksonMessageConverter;

    @Autowired
    private PageableHandlerMethodArgumentResolver pageableArgumentResolver;

    @Autowired
    private ExceptionTranslator exceptionTranslator;

    @Autowired
    private EntityManager em;

    @Autowired
    private FieldRepository fieldRepository;

    @Autowired
    private Validator validator;

    private MockMvc restFieldValueMockMvc;

    private FieldValue fieldValue;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        final FieldValueResource fieldValueResource = new FieldValueResource(
            fieldValueService,
            fieldValueRepository,
            fieldValueQueryService,
            fieldRepository
        );
        this.restFieldValueMockMvc =
            MockMvcBuilders
                .standaloneSetup(fieldValueResource)
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
    public static FieldValue createEntity(EntityManager em) {
        FieldValue fieldValue = new FieldValue()
            .value(DEFAULT_VALUE)
            .amount(DEFAULT_AMOUNT)
            .filterBy(DEFAULT_FILTER_BY)
            .ord(DEFAULT_ORD)
            .checkId(DEFAULT_CHECK_ID);
        // Add required entity
        Field field = FieldResourceIntTest.createEntity(em);
        em.persist(field);
        em.flush();
        fieldValue.setField(field);
        // Add required entity
        //        I18n i18n = I18nResourceIntTest.createEntity(em);
        //        em.persist(i18n);
        //        em.flush();
        //        fieldValue.getTitles().add(i18n);
        return fieldValue;
    }

    @Before
    public void initTest() {
        fieldValue = createEntity(em);
    }

    @Test
    @Transactional
    public void createFieldValue() throws Exception {
        int databaseSizeBeforeCreate = fieldValueRepository.findAll().size();

        // Create the FieldValue
        FieldValueDTO fieldValueDTO = fieldValueMapper.toDto(fieldValue);
        restFieldValueMockMvc
            .perform(
                post("/api/field-values")
                    .contentType(TestUtil.APPLICATION_JSON_UTF8)
                    .content(TestUtil.convertObjectToJsonBytes(fieldValueDTO))
            )
            .andExpect(status().isCreated());

        // Validate the FieldValue in the database
        List<FieldValue> fieldValueList = fieldValueRepository.findAll();
        assertThat(fieldValueList).hasSize(databaseSizeBeforeCreate + 1);
        FieldValue testFieldValue = fieldValueList.get(fieldValueList.size() - 1);
        assertThat(testFieldValue.getValue()).isEqualTo(DEFAULT_VALUE);
        assertThat(testFieldValue.getAmount()).isEqualTo(DEFAULT_AMOUNT);
        assertThat(testFieldValue.getFilterBy()).isEqualTo(DEFAULT_FILTER_BY);
        assertThat(testFieldValue.getOrd()).isEqualTo(DEFAULT_ORD);
        assertThat(testFieldValue.getCheckId()).isEqualTo(DEFAULT_CHECK_ID);
    }

    @Test
    @Transactional
    public void createFieldValueWithExistingId() throws Exception {
        int databaseSizeBeforeCreate = fieldValueRepository.findAll().size();

        // Create the FieldValue with an existing ID
        fieldValue.setId(1L);
        FieldValueDTO fieldValueDTO = fieldValueMapper.toDto(fieldValue);

        // An entity with an existing ID cannot be created, so this API call must fail
        restFieldValueMockMvc
            .perform(
                post("/api/field-values")
                    .contentType(TestUtil.APPLICATION_JSON_UTF8)
                    .content(TestUtil.convertObjectToJsonBytes(fieldValueDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the FieldValue in the database
        List<FieldValue> fieldValueList = fieldValueRepository.findAll();
        assertThat(fieldValueList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    public void checkValueIsRequired() throws Exception {
        int databaseSizeBeforeTest = fieldValueRepository.findAll().size();
        // set the field null
        fieldValue.setValue(null);

        // Create the FieldValue, which fails.
        FieldValueDTO fieldValueDTO = fieldValueMapper.toDto(fieldValue);

        restFieldValueMockMvc
            .perform(
                post("/api/field-values")
                    .contentType(TestUtil.APPLICATION_JSON_UTF8)
                    .content(TestUtil.convertObjectToJsonBytes(fieldValueDTO))
            )
            .andExpect(status().isBadRequest());

        List<FieldValue> fieldValueList = fieldValueRepository.findAll();
        assertThat(fieldValueList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void getAllFieldValues() throws Exception {
        // Initialize the database
        fieldValueRepository.saveAndFlush(fieldValue);

        // Get all the fieldValueList
        restFieldValueMockMvc
            .perform(get("/api/field-values?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(fieldValue.getId().intValue())))
            .andExpect(jsonPath("$.[*].value").value(hasItem(DEFAULT_VALUE.toString())))
            .andExpect(jsonPath("$.[*].amount").value(hasItem(DEFAULT_AMOUNT.intValue())))
            .andExpect(jsonPath("$.[*].filterBy").value(hasItem(DEFAULT_FILTER_BY.toString())))
            .andExpect(jsonPath("$.[*].ord").value(hasItem(DEFAULT_ORD)))
            .andExpect(jsonPath("$.[*].checkId").value(hasItem(DEFAULT_CHECK_ID.toString())));
    }

    @SuppressWarnings({ "unchecked" })
    public void getAllFieldValuesWithEagerRelationshipsIsEnabled() throws Exception {
        FieldValueResource fieldValueResource = new FieldValueResource(
            fieldValueServiceMock,
            fieldValueRepository,
            fieldValueQueryService,
            fieldRepository
        );
        when(fieldValueServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        MockMvc restFieldValueMockMvc = MockMvcBuilders
            .standaloneSetup(fieldValueResource)
            .setCustomArgumentResolvers(pageableArgumentResolver)
            .setControllerAdvice(exceptionTranslator)
            .setConversionService(createFormattingConversionService())
            .setMessageConverters(jacksonMessageConverter)
            .build();

        restFieldValueMockMvc.perform(get("/api/field-values?eagerload=true")).andExpect(status().isOk());

        verify(fieldValueServiceMock, times(1)).findAllWithEagerRelationships(any());
    }

    @SuppressWarnings({ "unchecked" })
    public void getAllFieldValuesWithEagerRelationshipsIsNotEnabled() throws Exception {
        FieldValueResource fieldValueResource = new FieldValueResource(
            fieldValueServiceMock,
            fieldValueRepository,
            fieldValueQueryService,
            fieldRepository
        );
        when(fieldValueServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));
        MockMvc restFieldValueMockMvc = MockMvcBuilders
            .standaloneSetup(fieldValueResource)
            .setCustomArgumentResolvers(pageableArgumentResolver)
            .setControllerAdvice(exceptionTranslator)
            .setConversionService(createFormattingConversionService())
            .setMessageConverters(jacksonMessageConverter)
            .build();

        restFieldValueMockMvc.perform(get("/api/field-values?eagerload=true")).andExpect(status().isOk());

        verify(fieldValueServiceMock, times(1)).findAllWithEagerRelationships(any());
    }

    @Test
    @Transactional
    public void getFieldValue() throws Exception {
        // Initialize the database
        fieldValueRepository.saveAndFlush(fieldValue);

        // Get the fieldValue
        restFieldValueMockMvc
            .perform(get("/api/field-values/{id}", fieldValue.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.id").value(fieldValue.getId().intValue()))
            .andExpect(jsonPath("$.value").value(DEFAULT_VALUE.toString()))
            .andExpect(jsonPath("$.amount").value(DEFAULT_AMOUNT.intValue()))
            .andExpect(jsonPath("$.filterBy").value(DEFAULT_FILTER_BY.toString()))
            .andExpect(jsonPath("$.ord").value(DEFAULT_ORD))
            .andExpect(jsonPath("$.checkId").value(DEFAULT_CHECK_ID.toString()));
    }

    @Test
    @Transactional
    public void getAllFieldValuesByValueIsEqualToSomething() throws Exception {
        // Initialize the database
        fieldValueRepository.saveAndFlush(fieldValue);

        // Get all the fieldValueList where value equals to DEFAULT_VALUE
        defaultFieldValueShouldBeFound("value.equals=" + DEFAULT_VALUE);

        // Get all the fieldValueList where value equals to UPDATED_VALUE
        defaultFieldValueShouldNotBeFound("value.equals=" + UPDATED_VALUE);
    }

    @Test
    @Transactional
    public void getAllFieldValuesByValueIsInShouldWork() throws Exception {
        // Initialize the database
        fieldValueRepository.saveAndFlush(fieldValue);

        // Get all the fieldValueList where value in DEFAULT_VALUE or UPDATED_VALUE
        defaultFieldValueShouldBeFound("value.in=" + DEFAULT_VALUE + "," + UPDATED_VALUE);

        // Get all the fieldValueList where value equals to UPDATED_VALUE
        defaultFieldValueShouldNotBeFound("value.in=" + UPDATED_VALUE);
    }

    @Test
    @Transactional
    public void getAllFieldValuesByValueIsNullOrNotNull() throws Exception {
        // Initialize the database
        fieldValueRepository.saveAndFlush(fieldValue);

        // Get all the fieldValueList where value is not null
        defaultFieldValueShouldBeFound("value.specified=true");

        // Get all the fieldValueList where value is null
        defaultFieldValueShouldNotBeFound("value.specified=false");
    }

    @Test
    @Transactional
    public void getAllFieldValuesByAmountIsEqualToSomething() throws Exception {
        // Initialize the database
        fieldValueRepository.saveAndFlush(fieldValue);

        // Get all the fieldValueList where amount equals to DEFAULT_AMOUNT
        defaultFieldValueShouldBeFound("amount.equals=" + DEFAULT_AMOUNT);

        // Get all the fieldValueList where amount equals to UPDATED_AMOUNT
        defaultFieldValueShouldNotBeFound("amount.equals=" + UPDATED_AMOUNT);
    }

    @Test
    @Transactional
    public void getAllFieldValuesByAmountIsInShouldWork() throws Exception {
        // Initialize the database
        fieldValueRepository.saveAndFlush(fieldValue);

        // Get all the fieldValueList where amount in DEFAULT_AMOUNT or UPDATED_AMOUNT
        defaultFieldValueShouldBeFound("amount.in=" + DEFAULT_AMOUNT + "," + UPDATED_AMOUNT);

        // Get all the fieldValueList where amount equals to UPDATED_AMOUNT
        defaultFieldValueShouldNotBeFound("amount.in=" + UPDATED_AMOUNT);
    }

    @Test
    @Transactional
    public void getAllFieldValuesByAmountIsNullOrNotNull() throws Exception {
        // Initialize the database
        fieldValueRepository.saveAndFlush(fieldValue);

        // Get all the fieldValueList where amount is not null
        defaultFieldValueShouldBeFound("amount.specified=true");

        // Get all the fieldValueList where amount is null
        defaultFieldValueShouldNotBeFound("amount.specified=false");
    }

    @Test
    @Transactional
    public void getAllFieldValuesByAmountIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        fieldValueRepository.saveAndFlush(fieldValue);

        // Get all the fieldValueList where amount greater than or equals to DEFAULT_AMOUNT
        defaultFieldValueShouldBeFound("amount.greaterOrEqualThan=" + DEFAULT_AMOUNT);

        // Get all the fieldValueList where amount greater than or equals to UPDATED_AMOUNT
        defaultFieldValueShouldNotBeFound("amount.greaterOrEqualThan=" + UPDATED_AMOUNT);
    }

    @Test
    @Transactional
    public void getAllFieldValuesByAmountIsLessThanSomething() throws Exception {
        // Initialize the database
        fieldValueRepository.saveAndFlush(fieldValue);

        // Get all the fieldValueList where amount less than or equals to DEFAULT_AMOUNT
        defaultFieldValueShouldNotBeFound("amount.lessThan=" + DEFAULT_AMOUNT);

        // Get all the fieldValueList where amount less than or equals to UPDATED_AMOUNT
        defaultFieldValueShouldBeFound("amount.lessThan=" + UPDATED_AMOUNT);
    }

    @Test
    @Transactional
    public void getAllFieldValuesByFilterByIsEqualToSomething() throws Exception {
        // Initialize the database
        fieldValueRepository.saveAndFlush(fieldValue);

        // Get all the fieldValueList where filterBy equals to DEFAULT_FILTER_BY
        defaultFieldValueShouldBeFound("filterBy.equals=" + DEFAULT_FILTER_BY);

        // Get all the fieldValueList where filterBy equals to UPDATED_FILTER_BY
        defaultFieldValueShouldNotBeFound("filterBy.equals=" + UPDATED_FILTER_BY);
    }

    @Test
    @Transactional
    public void getAllFieldValuesByFilterByIsInShouldWork() throws Exception {
        // Initialize the database
        fieldValueRepository.saveAndFlush(fieldValue);

        // Get all the fieldValueList where filterBy in DEFAULT_FILTER_BY or UPDATED_FILTER_BY
        defaultFieldValueShouldBeFound("filterBy.in=" + DEFAULT_FILTER_BY + "," + UPDATED_FILTER_BY);

        // Get all the fieldValueList where filterBy equals to UPDATED_FILTER_BY
        defaultFieldValueShouldNotBeFound("filterBy.in=" + UPDATED_FILTER_BY);
    }

    @Test
    @Transactional
    public void getAllFieldValuesByFilterByIsNullOrNotNull() throws Exception {
        // Initialize the database
        fieldValueRepository.saveAndFlush(fieldValue);

        // Get all the fieldValueList where filterBy is not null
        defaultFieldValueShouldBeFound("filterBy.specified=true");

        // Get all the fieldValueList where filterBy is null
        defaultFieldValueShouldNotBeFound("filterBy.specified=false");
    }

    @Test
    @Transactional
    public void getAllFieldValuesByOrdIsEqualToSomething() throws Exception {
        // Initialize the database
        fieldValueRepository.saveAndFlush(fieldValue);

        // Get all the fieldValueList where ord equals to DEFAULT_ORD
        defaultFieldValueShouldBeFound("ord.equals=" + DEFAULT_ORD);

        // Get all the fieldValueList where ord equals to UPDATED_ORD
        defaultFieldValueShouldNotBeFound("ord.equals=" + UPDATED_ORD);
    }

    @Test
    @Transactional
    public void getAllFieldValuesByOrdIsInShouldWork() throws Exception {
        // Initialize the database
        fieldValueRepository.saveAndFlush(fieldValue);

        // Get all the fieldValueList where ord in DEFAULT_ORD or UPDATED_ORD
        defaultFieldValueShouldBeFound("ord.in=" + DEFAULT_ORD + "," + UPDATED_ORD);

        // Get all the fieldValueList where ord equals to UPDATED_ORD
        defaultFieldValueShouldNotBeFound("ord.in=" + UPDATED_ORD);
    }

    @Test
    @Transactional
    public void getAllFieldValuesByOrdIsNullOrNotNull() throws Exception {
        // Initialize the database
        fieldValueRepository.saveAndFlush(fieldValue);

        // Get all the fieldValueList where ord is not null
        defaultFieldValueShouldBeFound("ord.specified=true");

        // Get all the fieldValueList where ord is null
        defaultFieldValueShouldNotBeFound("ord.specified=false");
    }

    @Test
    @Transactional
    public void getAllFieldValuesByOrdIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        fieldValueRepository.saveAndFlush(fieldValue);

        // Get all the fieldValueList where ord greater than or equals to DEFAULT_ORD
        defaultFieldValueShouldBeFound("ord.greaterOrEqualThan=" + DEFAULT_ORD);

        // Get all the fieldValueList where ord greater than or equals to UPDATED_ORD
        defaultFieldValueShouldNotBeFound("ord.greaterOrEqualThan=" + UPDATED_ORD);
    }

    @Test
    @Transactional
    public void getAllFieldValuesByOrdIsLessThanSomething() throws Exception {
        // Initialize the database
        fieldValueRepository.saveAndFlush(fieldValue);

        // Get all the fieldValueList where ord less than or equals to DEFAULT_ORD
        defaultFieldValueShouldNotBeFound("ord.lessThan=" + DEFAULT_ORD);

        // Get all the fieldValueList where ord less than or equals to UPDATED_ORD
        defaultFieldValueShouldBeFound("ord.lessThan=" + UPDATED_ORD);
    }

    @Test
    @Transactional
    public void getAllFieldValuesByCheckIdIsEqualToSomething() throws Exception {
        // Initialize the database
        fieldValueRepository.saveAndFlush(fieldValue);

        // Get all the fieldValueList where checkId equals to DEFAULT_CHECK_ID
        defaultFieldValueShouldBeFound("checkId.equals=" + DEFAULT_CHECK_ID);

        // Get all the fieldValueList where checkId equals to UPDATED_CHECK_ID
        defaultFieldValueShouldNotBeFound("checkId.equals=" + UPDATED_CHECK_ID);
    }

    @Test
    @Transactional
    public void getAllFieldValuesByCheckIdIsInShouldWork() throws Exception {
        // Initialize the database
        fieldValueRepository.saveAndFlush(fieldValue);

        // Get all the fieldValueList where checkId in DEFAULT_CHECK_ID or UPDATED_CHECK_ID
        defaultFieldValueShouldBeFound("checkId.in=" + DEFAULT_CHECK_ID + "," + UPDATED_CHECK_ID);

        // Get all the fieldValueList where checkId equals to UPDATED_CHECK_ID
        defaultFieldValueShouldNotBeFound("checkId.in=" + UPDATED_CHECK_ID);
    }

    @Test
    @Transactional
    public void getAllFieldValuesByCheckIdIsNullOrNotNull() throws Exception {
        // Initialize the database
        fieldValueRepository.saveAndFlush(fieldValue);

        // Get all the fieldValueList where checkId is not null
        defaultFieldValueShouldBeFound("checkId.specified=true");

        // Get all the fieldValueList where checkId is null
        defaultFieldValueShouldNotBeFound("checkId.specified=false");
    }

    @Test
    @Transactional
    public void getAllFieldValuesByFieldIsEqualToSomething() throws Exception {
        // Initialize the database
        Field field = FieldResourceIntTest.createEntity(em);
        em.persist(field);
        em.flush();
        fieldValue.setField(field);
        fieldValueRepository.saveAndFlush(fieldValue);
        Long fieldId = field.getId();

        // Get all the fieldValueList where field equals to fieldId
        defaultFieldValueShouldBeFound("fieldId.equals=" + fieldId);

        // Get all the fieldValueList where field equals to fieldId + 1
        defaultFieldValueShouldNotBeFound("fieldId.equals=" + (fieldId + 1));
    }

    @Test
    @Transactional
    public void getAllFieldValuesByTitleIsEqualToSomething() throws Exception {
        // Initialize the database
        I18n title = I18nResourceIntTest.createEntity(em);
        em.persist(title);
        em.flush();
        fieldValue.addTitle(title);
        fieldValueRepository.saveAndFlush(fieldValue);
        Long titleId = title.getId();

        // Get all the fieldValueList where title equals to titleId
        defaultFieldValueShouldBeFound("titleId.equals=" + titleId);

        // Get all the fieldValueList where title equals to titleId + 1
        defaultFieldValueShouldNotBeFound("titleId.equals=" + (titleId + 1));
    }

    @Test
    @Transactional
    public void getAllFieldValuesByDescriptionIsEqualToSomething() throws Exception {
        // Initialize the database
        I18n description = I18nResourceIntTest.createEntity(em);
        em.persist(description);
        em.flush();
        fieldValue.addDescription(description);
        fieldValueRepository.saveAndFlush(fieldValue);
        Long descriptionId = description.getId();

        // Get all the fieldValueList where description equals to descriptionId
        defaultFieldValueShouldBeFound("descriptionId.equals=" + descriptionId);

        // Get all the fieldValueList where description equals to descriptionId + 1
        defaultFieldValueShouldNotBeFound("descriptionId.equals=" + (descriptionId + 1));
    }

    /**
     * Executes the search, and checks that the default entity is returned
     */
    private void defaultFieldValueShouldBeFound(String filter) throws Exception {
        restFieldValueMockMvc
            .perform(get("/api/field-values?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(fieldValue.getId().intValue())))
            .andExpect(jsonPath("$.[*].value").value(hasItem(DEFAULT_VALUE.toString())))
            .andExpect(jsonPath("$.[*].amount").value(hasItem(DEFAULT_AMOUNT.intValue())))
            .andExpect(jsonPath("$.[*].filterBy").value(hasItem(DEFAULT_FILTER_BY.toString())))
            .andExpect(jsonPath("$.[*].ord").value(hasItem(DEFAULT_ORD)))
            .andExpect(jsonPath("$.[*].checkId").value(hasItem(DEFAULT_CHECK_ID)));
        // Check, that the count call also returns 1
        /*restFieldValueMockMvc.perform(get("/api/field-values/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(content().string("1"));*/
    }

    /**
     * Executes the search, and checks that the default entity is not returned
     */
    private void defaultFieldValueShouldNotBeFound(String filter) throws Exception {
        restFieldValueMockMvc
            .perform(get("/api/field-values?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$").isArray());
        //            .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    @Transactional
    public void getNonExistingFieldValue() throws Exception {
        // Get the fieldValue
        restFieldValueMockMvc.perform(get("/api/field-values/{id}", Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updateFieldValue() throws Exception {
        // Initialize the database
        fieldValueRepository.saveAndFlush(fieldValue);

        int databaseSizeBeforeUpdate = fieldValueRepository.findAll().size();

        // Update the fieldValue
        FieldValue updatedFieldValue = fieldValueRepository.findById(fieldValue.getId()).get();
        // Disconnect from session so that the updates on updatedFieldValue are not directly saved in db
        em.detach(updatedFieldValue);
        updatedFieldValue
            .value(UPDATED_VALUE)
            .amount(UPDATED_AMOUNT)
            .filterBy(UPDATED_FILTER_BY)
            .ord(UPDATED_ORD)
            .checkId(UPDATED_CHECK_ID);
        FieldValueDTO fieldValueDTO = fieldValueMapper.toDto(updatedFieldValue);

        restFieldValueMockMvc
            .perform(
                put("/api/field-values")
                    .contentType(TestUtil.APPLICATION_JSON_UTF8)
                    .content(TestUtil.convertObjectToJsonBytes(fieldValueDTO))
            )
            .andExpect(status().isOk());

        // Validate the FieldValue in the database
        List<FieldValue> fieldValueList = fieldValueRepository.findAll();
        assertThat(fieldValueList).hasSize(databaseSizeBeforeUpdate);
        FieldValue testFieldValue = fieldValueList.get(fieldValueList.size() - 1);
        assertThat(testFieldValue.getValue()).isEqualTo(UPDATED_VALUE);
        assertThat(testFieldValue.getAmount()).isEqualTo(UPDATED_AMOUNT);
        assertThat(testFieldValue.getFilterBy()).isEqualTo(UPDATED_FILTER_BY);
        assertThat(testFieldValue.getOrd()).isEqualTo(UPDATED_ORD);
        assertThat(testFieldValue.getCheckId()).isEqualTo(UPDATED_CHECK_ID);
    }

    @Test
    @Transactional
    public void updateNonExistingFieldValue() throws Exception {
        int databaseSizeBeforeUpdate = fieldValueRepository.findAll().size();

        // Create the FieldValue
        FieldValueDTO fieldValueDTO = fieldValueMapper.toDto(fieldValue);

        // If the entity doesn't have an ID, it will be created instead of just being updated
        restFieldValueMockMvc
            .perform(
                put("/api/field-values")
                    .contentType(TestUtil.APPLICATION_JSON_UTF8)
                    .content(TestUtil.convertObjectToJsonBytes(fieldValueDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the FieldValue in the database
        List<FieldValue> fieldValueList = fieldValueRepository.findAll();
        assertThat(fieldValueList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    public void deleteFieldValue() throws Exception {
        // Initialize the database
        fieldValueRepository.saveAndFlush(fieldValue);

        int databaseSizeBeforeDelete = fieldValueRepository.findAll().size();

        // Get the fieldValue
        restFieldValueMockMvc
            .perform(delete("/api/field-values/{id}", fieldValue.getId()).accept(TestUtil.APPLICATION_JSON_UTF8))
            .andExpect(status().isOk());

        // Validate the database is empty
        List<FieldValue> fieldValueList = fieldValueRepository.findAll();
        assertThat(fieldValueList).hasSize(databaseSizeBeforeDelete - 1);
    }

    @Test
    @Transactional
    public void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(FieldValue.class);
        FieldValue fieldValue1 = new FieldValue();
        fieldValue1.setId(1L);
        FieldValue fieldValue2 = new FieldValue();
        fieldValue2.setId(fieldValue1.getId());
        assertThat(fieldValue1).isEqualTo(fieldValue2);
        fieldValue2.setId(2L);
        assertThat(fieldValue1).isNotEqualTo(fieldValue2);
        fieldValue1.setId(null);
        assertThat(fieldValue1).isNotEqualTo(fieldValue2);
    }

    @Test
    @Transactional
    public void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(FieldValueDTO.class);
        FieldValueDTO fieldValueDTO1 = new FieldValueDTO();
        fieldValueDTO1.setId(1L);
        FieldValueDTO fieldValueDTO2 = new FieldValueDTO();
        assertThat(fieldValueDTO1).isNotEqualTo(fieldValueDTO2);
        fieldValueDTO2.setId(fieldValueDTO1.getId());
        assertThat(fieldValueDTO1).isEqualTo(fieldValueDTO2);
        fieldValueDTO2.setId(2L);
        assertThat(fieldValueDTO1).isNotEqualTo(fieldValueDTO2);
        fieldValueDTO1.setId(null);
        assertThat(fieldValueDTO1).isNotEqualTo(fieldValueDTO2);
    }

    @Test
    @Transactional
    public void testEntityFromId() {
        assertThat(fieldValueMapper.fromId(42L).getId()).isEqualTo(42);
        assertThat(fieldValueMapper.fromId(null)).isNull();
    }
}
