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
import uz.paykap.domain.Field;
import uz.paykap.domain.FieldValue;
import uz.paykap.domain.I18n;
import uz.paykap.repository.FieldValueRepository;
import uz.paykap.service.FieldValueService;
import uz.paykap.service.criteria.FieldValueCriteria;
import uz.paykap.service.dto.FieldValueDTO;
import uz.paykap.service.mapper.FieldValueMapper;

/**
 * Integration tests for the {@link FieldValueResource} REST controller.
 */
@IntegrationTest
@ExtendWith(MockitoExtension.class)
@AutoConfigureMockMvc
@WithMockUser
class FieldValueResourceIT {

    private static final String DEFAULT_VALUE = "AAAAAAAAAA";
    private static final String UPDATED_VALUE = "BBBBBBBBBB";

    private static final Long DEFAULT_AMOUNT = 1L;
    private static final Long UPDATED_AMOUNT = 2L;
    private static final Long SMALLER_AMOUNT = 1L - 1L;

    private static final String DEFAULT_FILTER_BY = "AAAAAAAAAA";
    private static final String UPDATED_FILTER_BY = "BBBBBBBBBB";

    private static final Integer DEFAULT_ORD = 1;
    private static final Integer UPDATED_ORD = 2;
    private static final Integer SMALLER_ORD = 1 - 1;

    private static final String ENTITY_API_URL = "/api/field-values";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong count = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private FieldValueRepository fieldValueRepository;

    @Mock
    private FieldValueRepository fieldValueRepositoryMock;

    @Autowired
    private FieldValueMapper fieldValueMapper;

    @Mock
    private FieldValueService fieldValueServiceMock;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restFieldValueMockMvc;

    private FieldValue fieldValue;

    /**
     * Create an entity for this test.
     * <p>
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static FieldValue createEntity(EntityManager em) {
        FieldValue fieldValue = new FieldValue().value(DEFAULT_VALUE).amount(DEFAULT_AMOUNT).filterBy(DEFAULT_FILTER_BY).ord(DEFAULT_ORD);
        // Add required entity
        Field field;
        if (TestUtil.findAll(em, Field.class).isEmpty()) {
            field = FieldResourceIT.createEntity(em);
            em.persist(field);
            em.flush();
        } else {
            field = TestUtil.findAll(em, Field.class).get(0);
        }
        fieldValue.setField(field);
        // Add required entity
        I18n i18n;
//        if (TestUtil.findAll(em, I18n.class).isEmpty()) {
        i18n = I18nResourceIT.createEntity(em);
//            em.persist(i18n);
//            em.flush();
//        } else {
//            i18n = TestUtil.findAll(em, I18n.class).get(0);
//        }
        fieldValue.getTitles().add(i18n);
        return fieldValue;
    }

    /**
     * Create an updated entity for this test.
     * <p>
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static FieldValue createUpdatedEntity(EntityManager em) {
        FieldValue fieldValue = new FieldValue().value(UPDATED_VALUE).amount(UPDATED_AMOUNT).filterBy(UPDATED_FILTER_BY).ord(UPDATED_ORD);
        // Add required entity
        Field field;
        if (TestUtil.findAll(em, Field.class).isEmpty()) {
            field = FieldResourceIT.createUpdatedEntity(em);
            em.persist(field);
            em.flush();
        } else {
            field = TestUtil.findAll(em, Field.class).get(0);
        }
        fieldValue.setField(field);
        // Add required entity
        I18n i18n;
        if (TestUtil.findAll(em, I18n.class).isEmpty()) {
            i18n = I18nResourceIT.createUpdatedEntity(em);
            em.persist(i18n);
            em.flush();
        } else {
            i18n = TestUtil.findAll(em, I18n.class).get(0);
        }
        fieldValue.getTitles().add(i18n);
        return fieldValue;
    }

    @BeforeEach
    public void initTest() {
        fieldValue = createEntity(em);
    }

    @Test
    @Transactional
    void createFieldValue() throws Exception {
        int databaseSizeBeforeCreate = fieldValueRepository.findAll().size();
        // Create the FieldValue
        FieldValueDTO fieldValueDTO = fieldValueMapper.toDto(fieldValue);
        restFieldValueMockMvc
                .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(fieldValueDTO)))
                .andExpect(status().isCreated());

        // Validate the FieldValue in the database
        List<FieldValue> fieldValueList = fieldValueRepository.findAll();
        assertThat(fieldValueList).hasSize(databaseSizeBeforeCreate + 1);
        FieldValue testFieldValue = fieldValueList.get(fieldValueList.size() - 1);
        assertThat(testFieldValue.getValue()).isEqualTo(DEFAULT_VALUE);
        assertThat(testFieldValue.getAmount()).isEqualTo(DEFAULT_AMOUNT);
        assertThat(testFieldValue.getFilterBy()).isEqualTo(DEFAULT_FILTER_BY);
        assertThat(testFieldValue.getOrd()).isEqualTo(DEFAULT_ORD);
    }

    @Test
    @Transactional
    void createFieldValueWithExistingId() throws Exception {
        // Create the FieldValue with an existing ID
        fieldValue.setId(1L);
        FieldValueDTO fieldValueDTO = fieldValueMapper.toDto(fieldValue);

        int databaseSizeBeforeCreate = fieldValueRepository.findAll().size();

        // An entity with an existing ID cannot be created, so this API call must fail
        restFieldValueMockMvc
                .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(fieldValueDTO)))
                .andExpect(status().isBadRequest());

        // Validate the FieldValue in the database
        List<FieldValue> fieldValueList = fieldValueRepository.findAll();
        assertThat(fieldValueList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkValueIsRequired() throws Exception {
        int databaseSizeBeforeTest = fieldValueRepository.findAll().size();
        // set the field null
        fieldValue.setValue(null);

        // Create the FieldValue, which fails.
        FieldValueDTO fieldValueDTO = fieldValueMapper.toDto(fieldValue);

        restFieldValueMockMvc
                .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(fieldValueDTO)))
                .andExpect(status().isBadRequest());

        List<FieldValue> fieldValueList = fieldValueRepository.findAll();
        assertThat(fieldValueList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllFieldValues() throws Exception {
        // Initialize the database
        fieldValueRepository.saveAndFlush(fieldValue);

        // Get all the fieldValueList
        restFieldValueMockMvc
                .perform(get(ENTITY_API_URL + "?sort=id,desc"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.[*].id").value(hasItem(fieldValue.getId().intValue())))
                .andExpect(jsonPath("$.[*].value").value(hasItem(DEFAULT_VALUE)))
                .andExpect(jsonPath("$.[*].amount").value(hasItem(DEFAULT_AMOUNT.intValue())))
                .andExpect(jsonPath("$.[*].filterBy").value(hasItem(DEFAULT_FILTER_BY)))
                .andExpect(jsonPath("$.[*].ord").value(hasItem(DEFAULT_ORD)));
    }

    @SuppressWarnings({"unchecked"})
    void getAllFieldValuesWithEagerRelationshipsIsEnabled() throws Exception {
        when(fieldValueServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restFieldValueMockMvc.perform(get(ENTITY_API_URL + "?eagerload=true")).andExpect(status().isOk());

        verify(fieldValueServiceMock, times(1)).findAllWithEagerRelationships(any());
    }

    @SuppressWarnings({"unchecked"})
    void getAllFieldValuesWithEagerRelationshipsIsNotEnabled() throws Exception {
        when(fieldValueServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restFieldValueMockMvc.perform(get(ENTITY_API_URL + "?eagerload=true")).andExpect(status().isOk());

        verify(fieldValueServiceMock, times(1)).findAllWithEagerRelationships(any());
    }

    @Test
    @Transactional
    void getFieldValue() throws Exception {
        // Initialize the database
        fieldValueRepository.saveAndFlush(fieldValue);

        // Get the fieldValue
        restFieldValueMockMvc
                .perform(get(ENTITY_API_URL_ID, fieldValue.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.id").value(fieldValue.getId().intValue()))
                .andExpect(jsonPath("$.value").value(DEFAULT_VALUE))
                .andExpect(jsonPath("$.amount").value(DEFAULT_AMOUNT.intValue()))
                .andExpect(jsonPath("$.filterBy").value(DEFAULT_FILTER_BY))
                .andExpect(jsonPath("$.ord").value(DEFAULT_ORD));
    }

    @Test
    @Transactional
    void getFieldValuesByIdFiltering() throws Exception {
        // Initialize the database
        fieldValueRepository.saveAndFlush(fieldValue);

        Long id = fieldValue.getId();

        defaultFieldValueShouldBeFound("id.equals=" + id);
        defaultFieldValueShouldNotBeFound("id.notEquals=" + id);

        defaultFieldValueShouldBeFound("id.greaterThanOrEqual=" + id);
        defaultFieldValueShouldNotBeFound("id.greaterThan=" + id);

        defaultFieldValueShouldBeFound("id.lessThanOrEqual=" + id);
        defaultFieldValueShouldNotBeFound("id.lessThan=" + id);
    }

    @Test
    @Transactional
    void getAllFieldValuesByValueIsEqualToSomething() throws Exception {
        // Initialize the database
        fieldValueRepository.saveAndFlush(fieldValue);

        // Get all the fieldValueList where value equals to DEFAULT_VALUE
        defaultFieldValueShouldBeFound("value.equals=" + DEFAULT_VALUE);

        // Get all the fieldValueList where value equals to UPDATED_VALUE
        defaultFieldValueShouldNotBeFound("value.equals=" + UPDATED_VALUE);
    }

    @Test
    @Transactional
    void getAllFieldValuesByValueIsNotEqualToSomething() throws Exception {
        // Initialize the database
        fieldValueRepository.saveAndFlush(fieldValue);

        // Get all the fieldValueList where value not equals to DEFAULT_VALUE
        defaultFieldValueShouldNotBeFound("value.notEquals=" + DEFAULT_VALUE);

        // Get all the fieldValueList where value not equals to UPDATED_VALUE
        defaultFieldValueShouldBeFound("value.notEquals=" + UPDATED_VALUE);
    }

    @Test
    @Transactional
    void getAllFieldValuesByValueIsInShouldWork() throws Exception {
        // Initialize the database
        fieldValueRepository.saveAndFlush(fieldValue);

        // Get all the fieldValueList where value in DEFAULT_VALUE or UPDATED_VALUE
        defaultFieldValueShouldBeFound("value.in=" + DEFAULT_VALUE + "," + UPDATED_VALUE);

        // Get all the fieldValueList where value equals to UPDATED_VALUE
        defaultFieldValueShouldNotBeFound("value.in=" + UPDATED_VALUE);
    }

    @Test
    @Transactional
    void getAllFieldValuesByValueIsNullOrNotNull() throws Exception {
        // Initialize the database
        fieldValueRepository.saveAndFlush(fieldValue);

        // Get all the fieldValueList where value is not null
        defaultFieldValueShouldBeFound("value.specified=true");

        // Get all the fieldValueList where value is null
        defaultFieldValueShouldNotBeFound("value.specified=false");
    }

    @Test
    @Transactional
    void getAllFieldValuesByValueContainsSomething() throws Exception {
        // Initialize the database
        fieldValueRepository.saveAndFlush(fieldValue);

        // Get all the fieldValueList where value contains DEFAULT_VALUE
        defaultFieldValueShouldBeFound("value.contains=" + DEFAULT_VALUE);

        // Get all the fieldValueList where value contains UPDATED_VALUE
        defaultFieldValueShouldNotBeFound("value.contains=" + UPDATED_VALUE);
    }

    @Test
    @Transactional
    void getAllFieldValuesByValueNotContainsSomething() throws Exception {
        // Initialize the database
        fieldValueRepository.saveAndFlush(fieldValue);

        // Get all the fieldValueList where value does not contain DEFAULT_VALUE
        defaultFieldValueShouldNotBeFound("value.doesNotContain=" + DEFAULT_VALUE);

        // Get all the fieldValueList where value does not contain UPDATED_VALUE
        defaultFieldValueShouldBeFound("value.doesNotContain=" + UPDATED_VALUE);
    }

    @Test
    @Transactional
    void getAllFieldValuesByAmountIsEqualToSomething() throws Exception {
        // Initialize the database
        fieldValueRepository.saveAndFlush(fieldValue);

        // Get all the fieldValueList where amount equals to DEFAULT_AMOUNT
        defaultFieldValueShouldBeFound("amount.equals=" + DEFAULT_AMOUNT);

        // Get all the fieldValueList where amount equals to UPDATED_AMOUNT
        defaultFieldValueShouldNotBeFound("amount.equals=" + UPDATED_AMOUNT);
    }

    @Test
    @Transactional
    void getAllFieldValuesByAmountIsNotEqualToSomething() throws Exception {
        // Initialize the database
        fieldValueRepository.saveAndFlush(fieldValue);

        // Get all the fieldValueList where amount not equals to DEFAULT_AMOUNT
        defaultFieldValueShouldNotBeFound("amount.notEquals=" + DEFAULT_AMOUNT);

        // Get all the fieldValueList where amount not equals to UPDATED_AMOUNT
        defaultFieldValueShouldBeFound("amount.notEquals=" + UPDATED_AMOUNT);
    }

    @Test
    @Transactional
    void getAllFieldValuesByAmountIsInShouldWork() throws Exception {
        // Initialize the database
        fieldValueRepository.saveAndFlush(fieldValue);

        // Get all the fieldValueList where amount in DEFAULT_AMOUNT or UPDATED_AMOUNT
        defaultFieldValueShouldBeFound("amount.in=" + DEFAULT_AMOUNT + "," + UPDATED_AMOUNT);

        // Get all the fieldValueList where amount equals to UPDATED_AMOUNT
        defaultFieldValueShouldNotBeFound("amount.in=" + UPDATED_AMOUNT);
    }

    @Test
    @Transactional
    void getAllFieldValuesByAmountIsNullOrNotNull() throws Exception {
        // Initialize the database
        fieldValueRepository.saveAndFlush(fieldValue);

        // Get all the fieldValueList where amount is not null
        defaultFieldValueShouldBeFound("amount.specified=true");

        // Get all the fieldValueList where amount is null
        defaultFieldValueShouldNotBeFound("amount.specified=false");
    }

    @Test
    @Transactional
    void getAllFieldValuesByAmountIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        fieldValueRepository.saveAndFlush(fieldValue);

        // Get all the fieldValueList where amount is greater than or equal to DEFAULT_AMOUNT
        defaultFieldValueShouldBeFound("amount.greaterThanOrEqual=" + DEFAULT_AMOUNT);

        // Get all the fieldValueList where amount is greater than or equal to UPDATED_AMOUNT
        defaultFieldValueShouldNotBeFound("amount.greaterThanOrEqual=" + UPDATED_AMOUNT);
    }

    @Test
    @Transactional
    void getAllFieldValuesByAmountIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        fieldValueRepository.saveAndFlush(fieldValue);

        // Get all the fieldValueList where amount is less than or equal to DEFAULT_AMOUNT
        defaultFieldValueShouldBeFound("amount.lessThanOrEqual=" + DEFAULT_AMOUNT);

        // Get all the fieldValueList where amount is less than or equal to SMALLER_AMOUNT
        defaultFieldValueShouldNotBeFound("amount.lessThanOrEqual=" + SMALLER_AMOUNT);
    }

    @Test
    @Transactional
    void getAllFieldValuesByAmountIsLessThanSomething() throws Exception {
        // Initialize the database
        fieldValueRepository.saveAndFlush(fieldValue);

        // Get all the fieldValueList where amount is less than DEFAULT_AMOUNT
        defaultFieldValueShouldNotBeFound("amount.lessThan=" + DEFAULT_AMOUNT);

        // Get all the fieldValueList where amount is less than UPDATED_AMOUNT
        defaultFieldValueShouldBeFound("amount.lessThan=" + UPDATED_AMOUNT);
    }

    @Test
    @Transactional
    void getAllFieldValuesByAmountIsGreaterThanSomething() throws Exception {
        // Initialize the database
        fieldValueRepository.saveAndFlush(fieldValue);

        // Get all the fieldValueList where amount is greater than DEFAULT_AMOUNT
        defaultFieldValueShouldNotBeFound("amount.greaterThan=" + DEFAULT_AMOUNT);

        // Get all the fieldValueList where amount is greater than SMALLER_AMOUNT
        defaultFieldValueShouldBeFound("amount.greaterThan=" + SMALLER_AMOUNT);
    }

    @Test
    @Transactional
    void getAllFieldValuesByFilterByIsEqualToSomething() throws Exception {
        // Initialize the database
        fieldValueRepository.saveAndFlush(fieldValue);

        // Get all the fieldValueList where filterBy equals to DEFAULT_FILTER_BY
        defaultFieldValueShouldBeFound("filterBy.equals=" + DEFAULT_FILTER_BY);

        // Get all the fieldValueList where filterBy equals to UPDATED_FILTER_BY
        defaultFieldValueShouldNotBeFound("filterBy.equals=" + UPDATED_FILTER_BY);
    }

    @Test
    @Transactional
    void getAllFieldValuesByFilterByIsNotEqualToSomething() throws Exception {
        // Initialize the database
        fieldValueRepository.saveAndFlush(fieldValue);

        // Get all the fieldValueList where filterBy not equals to DEFAULT_FILTER_BY
        defaultFieldValueShouldNotBeFound("filterBy.notEquals=" + DEFAULT_FILTER_BY);

        // Get all the fieldValueList where filterBy not equals to UPDATED_FILTER_BY
        defaultFieldValueShouldBeFound("filterBy.notEquals=" + UPDATED_FILTER_BY);
    }

    @Test
    @Transactional
    void getAllFieldValuesByFilterByIsInShouldWork() throws Exception {
        // Initialize the database
        fieldValueRepository.saveAndFlush(fieldValue);

        // Get all the fieldValueList where filterBy in DEFAULT_FILTER_BY or UPDATED_FILTER_BY
        defaultFieldValueShouldBeFound("filterBy.in=" + DEFAULT_FILTER_BY + "," + UPDATED_FILTER_BY);

        // Get all the fieldValueList where filterBy equals to UPDATED_FILTER_BY
        defaultFieldValueShouldNotBeFound("filterBy.in=" + UPDATED_FILTER_BY);
    }

    @Test
    @Transactional
    void getAllFieldValuesByFilterByIsNullOrNotNull() throws Exception {
        // Initialize the database
        fieldValueRepository.saveAndFlush(fieldValue);

        // Get all the fieldValueList where filterBy is not null
        defaultFieldValueShouldBeFound("filterBy.specified=true");

        // Get all the fieldValueList where filterBy is null
        defaultFieldValueShouldNotBeFound("filterBy.specified=false");
    }

    @Test
    @Transactional
    void getAllFieldValuesByFilterByContainsSomething() throws Exception {
        // Initialize the database
        fieldValueRepository.saveAndFlush(fieldValue);

        // Get all the fieldValueList where filterBy contains DEFAULT_FILTER_BY
        defaultFieldValueShouldBeFound("filterBy.contains=" + DEFAULT_FILTER_BY);

        // Get all the fieldValueList where filterBy contains UPDATED_FILTER_BY
        defaultFieldValueShouldNotBeFound("filterBy.contains=" + UPDATED_FILTER_BY);
    }

    @Test
    @Transactional
    void getAllFieldValuesByFilterByNotContainsSomething() throws Exception {
        // Initialize the database
        fieldValueRepository.saveAndFlush(fieldValue);

        // Get all the fieldValueList where filterBy does not contain DEFAULT_FILTER_BY
        defaultFieldValueShouldNotBeFound("filterBy.doesNotContain=" + DEFAULT_FILTER_BY);

        // Get all the fieldValueList where filterBy does not contain UPDATED_FILTER_BY
        defaultFieldValueShouldBeFound("filterBy.doesNotContain=" + UPDATED_FILTER_BY);
    }

    @Test
    @Transactional
    void getAllFieldValuesByOrdIsEqualToSomething() throws Exception {
        // Initialize the database
        fieldValueRepository.saveAndFlush(fieldValue);

        // Get all the fieldValueList where ord equals to DEFAULT_ORD
        defaultFieldValueShouldBeFound("ord.equals=" + DEFAULT_ORD);

        // Get all the fieldValueList where ord equals to UPDATED_ORD
        defaultFieldValueShouldNotBeFound("ord.equals=" + UPDATED_ORD);
    }

    @Test
    @Transactional
    void getAllFieldValuesByOrdIsNotEqualToSomething() throws Exception {
        // Initialize the database
        fieldValueRepository.saveAndFlush(fieldValue);

        // Get all the fieldValueList where ord not equals to DEFAULT_ORD
        defaultFieldValueShouldNotBeFound("ord.notEquals=" + DEFAULT_ORD);

        // Get all the fieldValueList where ord not equals to UPDATED_ORD
        defaultFieldValueShouldBeFound("ord.notEquals=" + UPDATED_ORD);
    }

    @Test
    @Transactional
    void getAllFieldValuesByOrdIsInShouldWork() throws Exception {
        // Initialize the database
        fieldValueRepository.saveAndFlush(fieldValue);

        // Get all the fieldValueList where ord in DEFAULT_ORD or UPDATED_ORD
        defaultFieldValueShouldBeFound("ord.in=" + DEFAULT_ORD + "," + UPDATED_ORD);

        // Get all the fieldValueList where ord equals to UPDATED_ORD
        defaultFieldValueShouldNotBeFound("ord.in=" + UPDATED_ORD);
    }

    @Test
    @Transactional
    void getAllFieldValuesByOrdIsNullOrNotNull() throws Exception {
        // Initialize the database
        fieldValueRepository.saveAndFlush(fieldValue);

        // Get all the fieldValueList where ord is not null
        defaultFieldValueShouldBeFound("ord.specified=true");

        // Get all the fieldValueList where ord is null
        defaultFieldValueShouldNotBeFound("ord.specified=false");
    }

    @Test
    @Transactional
    void getAllFieldValuesByOrdIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        fieldValueRepository.saveAndFlush(fieldValue);

        // Get all the fieldValueList where ord is greater than or equal to DEFAULT_ORD
        defaultFieldValueShouldBeFound("ord.greaterThanOrEqual=" + DEFAULT_ORD);

        // Get all the fieldValueList where ord is greater than or equal to UPDATED_ORD
        defaultFieldValueShouldNotBeFound("ord.greaterThanOrEqual=" + UPDATED_ORD);
    }

    @Test
    @Transactional
    void getAllFieldValuesByOrdIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        fieldValueRepository.saveAndFlush(fieldValue);

        // Get all the fieldValueList where ord is less than or equal to DEFAULT_ORD
        defaultFieldValueShouldBeFound("ord.lessThanOrEqual=" + DEFAULT_ORD);

        // Get all the fieldValueList where ord is less than or equal to SMALLER_ORD
        defaultFieldValueShouldNotBeFound("ord.lessThanOrEqual=" + SMALLER_ORD);
    }

    @Test
    @Transactional
    void getAllFieldValuesByOrdIsLessThanSomething() throws Exception {
        // Initialize the database
        fieldValueRepository.saveAndFlush(fieldValue);

        // Get all the fieldValueList where ord is less than DEFAULT_ORD
        defaultFieldValueShouldNotBeFound("ord.lessThan=" + DEFAULT_ORD);

        // Get all the fieldValueList where ord is less than UPDATED_ORD
        defaultFieldValueShouldBeFound("ord.lessThan=" + UPDATED_ORD);
    }

    @Test
    @Transactional
    void getAllFieldValuesByOrdIsGreaterThanSomething() throws Exception {
        // Initialize the database
        fieldValueRepository.saveAndFlush(fieldValue);

        // Get all the fieldValueList where ord is greater than DEFAULT_ORD
        defaultFieldValueShouldNotBeFound("ord.greaterThan=" + DEFAULT_ORD);

        // Get all the fieldValueList where ord is greater than SMALLER_ORD
        defaultFieldValueShouldBeFound("ord.greaterThan=" + SMALLER_ORD);
    }

    @Test
    @Transactional
    void getAllFieldValuesByFieldIsEqualToSomething() throws Exception {
        // Initialize the database
        fieldValueRepository.saveAndFlush(fieldValue);
        Field field = FieldResourceIT.createEntity(em);
        em.persist(field);
        em.flush();
        fieldValue.setField(field);
        fieldValueRepository.saveAndFlush(fieldValue);
        Long fieldId = field.getId();

        // Get all the fieldValueList where field equals to fieldId
        defaultFieldValueShouldBeFound("fieldId.equals=" + fieldId);

        // Get all the fieldValueList where field equals to (fieldId + 1)
        defaultFieldValueShouldNotBeFound("fieldId.equals=" + (fieldId + 1));
    }

    @Test
    @Transactional
    void getAllFieldValuesByTitleIsEqualToSomething() throws Exception {
        // Initialize the database
        fieldValueRepository.saveAndFlush(fieldValue);
        I18n title = I18nResourceIT.createEntity(em);
        em.persist(title);
        em.flush();
        fieldValue.addTitle(title);
        fieldValueRepository.saveAndFlush(fieldValue);
        Long titleId = title.getId();

        // Get all the fieldValueList where title equals to titleId
        defaultFieldValueShouldBeFound("titleId.equals=" + titleId);

        // Get all the fieldValueList where title equals to (titleId + 1)
        defaultFieldValueShouldNotBeFound("titleId.equals=" + (titleId + 1));
    }

    @Test
    @Transactional
    void getAllFieldValuesByDescriptionIsEqualToSomething() throws Exception {
        // Initialize the database
        fieldValueRepository.saveAndFlush(fieldValue);
        I18n description = I18nResourceIT.createEntity(em);
        em.persist(description);
        em.flush();
        fieldValue.addDescription(description);
        fieldValueRepository.saveAndFlush(fieldValue);
        Long descriptionId = description.getId();

        // Get all the fieldValueList where description equals to descriptionId
        defaultFieldValueShouldBeFound("descriptionId.equals=" + descriptionId);

        // Get all the fieldValueList where description equals to (descriptionId + 1)
        defaultFieldValueShouldNotBeFound("descriptionId.equals=" + (descriptionId + 1));
    }

    /**
     * Executes the search, and checks that the default entity is returned.
     */
    private void defaultFieldValueShouldBeFound(String filter) throws Exception {
        restFieldValueMockMvc
                .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.[*].id").value(hasItem(fieldValue.getId().intValue())))
                .andExpect(jsonPath("$.[*].value").value(hasItem(DEFAULT_VALUE)))
                .andExpect(jsonPath("$.[*].amount").value(hasItem(DEFAULT_AMOUNT.intValue())))
                .andExpect(jsonPath("$.[*].filterBy").value(hasItem(DEFAULT_FILTER_BY)))
                .andExpect(jsonPath("$.[*].ord").value(hasItem(DEFAULT_ORD)));

        // Check, that the count call also returns 1
        restFieldValueMockMvc
                .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(content().string("1"));
    }

    /**
     * Executes the search, and checks that the default entity is not returned.
     */
    private void defaultFieldValueShouldNotBeFound(String filter) throws Exception {
        restFieldValueMockMvc
                .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());

        // Check, that the count call also returns 0
        restFieldValueMockMvc
                .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(content().string("0"));
    }

    @Test
    @Transactional
    void getNonExistingFieldValue() throws Exception {
        // Get the fieldValue
        restFieldValueMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putNewFieldValue() throws Exception {
        // Initialize the database
        fieldValueRepository.saveAndFlush(fieldValue);

        int databaseSizeBeforeUpdate = fieldValueRepository.findAll().size();

        // Update the fieldValue
        FieldValue updatedFieldValue = fieldValueRepository.findById(fieldValue.getId()).get();
        // Disconnect from session so that the updates on updatedFieldValue are not directly saved in db
        em.detach(updatedFieldValue);
        updatedFieldValue.value(UPDATED_VALUE).amount(UPDATED_AMOUNT).filterBy(UPDATED_FILTER_BY).ord(UPDATED_ORD);
        FieldValueDTO fieldValueDTO = fieldValueMapper.toDto(updatedFieldValue);

        restFieldValueMockMvc
                .perform(
                        put(ENTITY_API_URL_ID, fieldValueDTO.getId())
                                .contentType(MediaType.APPLICATION_JSON)
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
    }

    @Test
    @Transactional
    void putNonExistingFieldValue() throws Exception {
        int databaseSizeBeforeUpdate = fieldValueRepository.findAll().size();
        fieldValue.setId(count.incrementAndGet());

        // Create the FieldValue
        FieldValueDTO fieldValueDTO = fieldValueMapper.toDto(fieldValue);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restFieldValueMockMvc
                .perform(
                        put(ENTITY_API_URL_ID, fieldValueDTO.getId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(TestUtil.convertObjectToJsonBytes(fieldValueDTO))
                )
                .andExpect(status().isBadRequest());

        // Validate the FieldValue in the database
        List<FieldValue> fieldValueList = fieldValueRepository.findAll();
        assertThat(fieldValueList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchFieldValue() throws Exception {
        int databaseSizeBeforeUpdate = fieldValueRepository.findAll().size();
        fieldValue.setId(count.incrementAndGet());

        // Create the FieldValue
        FieldValueDTO fieldValueDTO = fieldValueMapper.toDto(fieldValue);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restFieldValueMockMvc
                .perform(
                        put(ENTITY_API_URL_ID, count.incrementAndGet())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(TestUtil.convertObjectToJsonBytes(fieldValueDTO))
                )
                .andExpect(status().isBadRequest());

        // Validate the FieldValue in the database
        List<FieldValue> fieldValueList = fieldValueRepository.findAll();
        assertThat(fieldValueList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamFieldValue() throws Exception {
        int databaseSizeBeforeUpdate = fieldValueRepository.findAll().size();
        fieldValue.setId(count.incrementAndGet());

        // Create the FieldValue
        FieldValueDTO fieldValueDTO = fieldValueMapper.toDto(fieldValue);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restFieldValueMockMvc
                .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(fieldValueDTO)))
                .andExpect(status().isMethodNotAllowed());

        // Validate the FieldValue in the database
        List<FieldValue> fieldValueList = fieldValueRepository.findAll();
        assertThat(fieldValueList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateFieldValueWithPatch() throws Exception {
        // Initialize the database
        fieldValueRepository.saveAndFlush(fieldValue);

        int databaseSizeBeforeUpdate = fieldValueRepository.findAll().size();

        // Update the fieldValue using partial update
        FieldValue partialUpdatedFieldValue = new FieldValue();
        partialUpdatedFieldValue.setId(fieldValue.getId());

        partialUpdatedFieldValue.filterBy(UPDATED_FILTER_BY);

        restFieldValueMockMvc
                .perform(
                        patch(ENTITY_API_URL_ID, partialUpdatedFieldValue.getId())
                                .contentType("application/merge-patch+json")
                                .content(TestUtil.convertObjectToJsonBytes(partialUpdatedFieldValue))
                )
                .andExpect(status().isOk());

        // Validate the FieldValue in the database
        List<FieldValue> fieldValueList = fieldValueRepository.findAll();
        assertThat(fieldValueList).hasSize(databaseSizeBeforeUpdate);
        FieldValue testFieldValue = fieldValueList.get(fieldValueList.size() - 1);
        assertThat(testFieldValue.getValue()).isEqualTo(DEFAULT_VALUE);
        assertThat(testFieldValue.getAmount()).isEqualTo(DEFAULT_AMOUNT);
        assertThat(testFieldValue.getFilterBy()).isEqualTo(UPDATED_FILTER_BY);
        assertThat(testFieldValue.getOrd()).isEqualTo(DEFAULT_ORD);
    }

    @Test
    @Transactional
    void fullUpdateFieldValueWithPatch() throws Exception {
        // Initialize the database
        fieldValueRepository.saveAndFlush(fieldValue);

        int databaseSizeBeforeUpdate = fieldValueRepository.findAll().size();

        // Update the fieldValue using partial update
        FieldValue partialUpdatedFieldValue = new FieldValue();
        partialUpdatedFieldValue.setId(fieldValue.getId());

        partialUpdatedFieldValue.value(UPDATED_VALUE).amount(UPDATED_AMOUNT).filterBy(UPDATED_FILTER_BY).ord(UPDATED_ORD);

        restFieldValueMockMvc
                .perform(
                        patch(ENTITY_API_URL_ID, partialUpdatedFieldValue.getId())
                                .contentType("application/merge-patch+json")
                                .content(TestUtil.convertObjectToJsonBytes(partialUpdatedFieldValue))
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
    }

    @Test
    @Transactional
    void patchNonExistingFieldValue() throws Exception {
        int databaseSizeBeforeUpdate = fieldValueRepository.findAll().size();
        fieldValue.setId(count.incrementAndGet());

        // Create the FieldValue
        FieldValueDTO fieldValueDTO = fieldValueMapper.toDto(fieldValue);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restFieldValueMockMvc
                .perform(
                        patch(ENTITY_API_URL_ID, fieldValueDTO.getId())
                                .contentType("application/merge-patch+json")
                                .content(TestUtil.convertObjectToJsonBytes(fieldValueDTO))
                )
                .andExpect(status().isBadRequest());

        // Validate the FieldValue in the database
        List<FieldValue> fieldValueList = fieldValueRepository.findAll();
        assertThat(fieldValueList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchFieldValue() throws Exception {
        int databaseSizeBeforeUpdate = fieldValueRepository.findAll().size();
        fieldValue.setId(count.incrementAndGet());

        // Create the FieldValue
        FieldValueDTO fieldValueDTO = fieldValueMapper.toDto(fieldValue);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restFieldValueMockMvc
                .perform(
                        patch(ENTITY_API_URL_ID, count.incrementAndGet())
                                .contentType("application/merge-patch+json")
                                .content(TestUtil.convertObjectToJsonBytes(fieldValueDTO))
                )
                .andExpect(status().isBadRequest());

        // Validate the FieldValue in the database
        List<FieldValue> fieldValueList = fieldValueRepository.findAll();
        assertThat(fieldValueList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamFieldValue() throws Exception {
        int databaseSizeBeforeUpdate = fieldValueRepository.findAll().size();
        fieldValue.setId(count.incrementAndGet());

        // Create the FieldValue
        FieldValueDTO fieldValueDTO = fieldValueMapper.toDto(fieldValue);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restFieldValueMockMvc
                .perform(
                        patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(TestUtil.convertObjectToJsonBytes(fieldValueDTO))
                )
                .andExpect(status().isMethodNotAllowed());

        // Validate the FieldValue in the database
        List<FieldValue> fieldValueList = fieldValueRepository.findAll();
        assertThat(fieldValueList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteFieldValue() throws Exception {
        // Initialize the database
        fieldValueRepository.saveAndFlush(fieldValue);

        int databaseSizeBeforeDelete = fieldValueRepository.findAll().size();

        // Delete the fieldValue
        restFieldValueMockMvc
                .perform(delete(ENTITY_API_URL_ID, fieldValue.getId()).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        // Validate the database contains one less item
        List<FieldValue> fieldValueList = fieldValueRepository.findAll();
        assertThat(fieldValueList).hasSize(databaseSizeBeforeDelete - 1);
    }
}
