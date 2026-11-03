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
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import uz.devops.bmcore.enumeration.RequestType;
import uz.devops.bmcore.enumeration.Visibility;
import uz.paykap.IntegrationTest;
import uz.paykap.domain.I18n;
import uz.paykap.domain.Recipient;
import uz.paykap.domain.ResponseField;
import uz.paykap.repository.ResponseFieldRepository;
import uz.paykap.service.ResponseFieldService;
import uz.paykap.service.dto.ResponseFieldDTO;
import uz.paykap.service.mapper.ResponseFieldMapper;

/**
 * Integration tests for the {@link ResponseFieldResource} REST controller.
 */
@IntegrationTest
@ExtendWith(MockitoExtension.class)
@AutoConfigureMockMvc
@WithMockUser
class ResponseFieldResourceIT {

    private static final String DEFAULT_NAME = "AAAAAAAAAA";
    private static final String UPDATED_NAME = "BBBBBBBBBB";

    private static final Visibility DEFAULT_VISIBILITY = Visibility.HIDDEN;
    private static final Visibility UPDATED_VISIBILITY = Visibility.PRINTER;

    private static final Integer DEFAULT_ORD = 1;
    private static final Integer UPDATED_ORD = 2;
    private static final Integer SMALLER_ORD = 1 - 1;

    private static final RequestType DEFAULT_REQUEST_TYPE = RequestType.CHECK;
    private static final RequestType UPDATED_REQUEST_TYPE = RequestType.PAY;

    private static final String ENTITY_API_URL = "/api/response-fields";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong count = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private ResponseFieldRepository responseFieldRepository;

    @Mock
    private ResponseFieldRepository responseFieldRepositoryMock;

    @Autowired
    private ResponseFieldMapper responseFieldMapper;

    @Mock
    private ResponseFieldService responseFieldServiceMock;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restResponseFieldMockMvc;

    private ResponseField responseField;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static ResponseField createEntity(EntityManager em) {
        ResponseField responseField = new ResponseField()
            .name(DEFAULT_NAME)
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
        responseField.setRecipient(recipient);
        // Add required entity
        I18n i18n;
        //        if (TestUtil.findAll(em, I18n.class).isEmpty()) {
        i18n = I18nResourceIT.createEntity(em);
        //            em.persist(i18n);
        //            em.flush();
        //        } else {
        //            i18n = TestUtil.findAll(em, I18n.class).get(0);
        //        }
        responseField.getLabels().add(i18n);
        return responseField;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static ResponseField createUpdatedEntity(EntityManager em) {
        ResponseField responseField = new ResponseField()
            .name(UPDATED_NAME)
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
        responseField.setRecipient(recipient);
        // Add required entity
        I18n i18n;
        if (TestUtil.findAll(em, I18n.class).isEmpty()) {
            i18n = I18nResourceIT.createUpdatedEntity(em);
            em.persist(i18n);
            em.flush();
        } else {
            i18n = TestUtil.findAll(em, I18n.class).get(0);
        }
        responseField.getLabels().add(i18n);
        return responseField;
    }

    @BeforeEach
    public void initTest() {
        responseField = createEntity(em);
    }

    @Test
    @Transactional
    void createResponseField() throws Exception {
        int databaseSizeBeforeCreate = responseFieldRepository.findAll().size();
        // Create the ResponseField
        ResponseFieldDTO responseFieldDTO = responseFieldMapper.toDto(responseField);
        restResponseFieldMockMvc
            .perform(
                post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(responseFieldDTO))
            )
            .andExpect(status().isCreated());

        // Validate the ResponseField in the database
        List<ResponseField> responseFieldList = responseFieldRepository.findAll();
        assertThat(responseFieldList).hasSize(databaseSizeBeforeCreate + 1);
        ResponseField testResponseField = responseFieldList.get(responseFieldList.size() - 1);
        assertThat(testResponseField.getName()).isEqualTo(DEFAULT_NAME);
        assertThat(testResponseField.getVisibility()).isEqualTo(DEFAULT_VISIBILITY);
        assertThat(testResponseField.getOrd()).isEqualTo(DEFAULT_ORD);
        assertThat(testResponseField.getRequestType()).isEqualTo(DEFAULT_REQUEST_TYPE);
    }

    @Test
    @Transactional
    void createResponseFieldWithExistingId() throws Exception {
        // Create the ResponseField with an existing ID
        responseField.setId(1L);
        ResponseFieldDTO responseFieldDTO = responseFieldMapper.toDto(responseField);

        int databaseSizeBeforeCreate = responseFieldRepository.findAll().size();

        // An entity with an existing ID cannot be created, so this API call must fail
        restResponseFieldMockMvc
            .perform(
                post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(responseFieldDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the ResponseField in the database
        List<ResponseField> responseFieldList = responseFieldRepository.findAll();
        assertThat(responseFieldList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkNameIsRequired() throws Exception {
        int databaseSizeBeforeTest = responseFieldRepository.findAll().size();
        // set the field null
        responseField.setName(null);

        // Create the ResponseField, which fails.
        ResponseFieldDTO responseFieldDTO = responseFieldMapper.toDto(responseField);

        restResponseFieldMockMvc
            .perform(
                post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(responseFieldDTO))
            )
            .andExpect(status().isBadRequest());

        List<ResponseField> responseFieldList = responseFieldRepository.findAll();
        assertThat(responseFieldList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllResponseFields() throws Exception {
        // Initialize the database
        responseFieldRepository.saveAndFlush(responseField);

        // Get all the responseFieldList
        restResponseFieldMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(responseField.getId().intValue())))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME)))
            .andExpect(jsonPath("$.[*].visibility").value(hasItem(DEFAULT_VISIBILITY.toString())))
            .andExpect(jsonPath("$.[*].ord").value(hasItem(DEFAULT_ORD)))
            .andExpect(jsonPath("$.[*].requestType").value(hasItem(DEFAULT_REQUEST_TYPE.toString())));
    }

    @SuppressWarnings({ "unchecked" })
    void getAllResponseFieldsWithEagerRelationshipsIsEnabled() throws Exception {
        when(responseFieldServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restResponseFieldMockMvc.perform(get(ENTITY_API_URL + "?eagerload=true")).andExpect(status().isOk());

        verify(responseFieldServiceMock, times(1)).findAllWithEagerRelationships(any());
    }

    @SuppressWarnings({ "unchecked" })
    void getAllResponseFieldsWithEagerRelationshipsIsNotEnabled() throws Exception {
        when(responseFieldServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restResponseFieldMockMvc.perform(get(ENTITY_API_URL + "?eagerload=true")).andExpect(status().isOk());

        verify(responseFieldServiceMock, times(1)).findAllWithEagerRelationships(any());
    }

    @Test
    @Transactional
    void getResponseField() throws Exception {
        // Initialize the database
        responseFieldRepository.saveAndFlush(responseField);

        // Get the responseField
        restResponseFieldMockMvc
            .perform(get(ENTITY_API_URL_ID, responseField.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(responseField.getId().intValue()))
            .andExpect(jsonPath("$.name").value(DEFAULT_NAME))
            .andExpect(jsonPath("$.visibility").value(DEFAULT_VISIBILITY.toString()))
            .andExpect(jsonPath("$.ord").value(DEFAULT_ORD))
            .andExpect(jsonPath("$.requestType").value(DEFAULT_REQUEST_TYPE.toString()));
    }

    @Test
    @Transactional
    void getResponseFieldsByIdFiltering() throws Exception {
        // Initialize the database
        responseFieldRepository.saveAndFlush(responseField);

        Long id = responseField.getId();

        defaultResponseFieldShouldBeFound("id.equals=" + id);
        defaultResponseFieldShouldNotBeFound("id.notEquals=" + id);

        defaultResponseFieldShouldBeFound("id.greaterThanOrEqual=" + id);
        defaultResponseFieldShouldNotBeFound("id.greaterThan=" + id);

        defaultResponseFieldShouldBeFound("id.lessThanOrEqual=" + id);
        defaultResponseFieldShouldNotBeFound("id.lessThan=" + id);
    }

    @Test
    @Transactional
    void getAllResponseFieldsByNameIsEqualToSomething() throws Exception {
        // Initialize the database
        responseFieldRepository.saveAndFlush(responseField);

        // Get all the responseFieldList where name equals to DEFAULT_NAME
        defaultResponseFieldShouldBeFound("name.equals=" + DEFAULT_NAME);

        // Get all the responseFieldList where name equals to UPDATED_NAME
        defaultResponseFieldShouldNotBeFound("name.equals=" + UPDATED_NAME);
    }

    @Test
    @Transactional
    void getAllResponseFieldsByNameIsNotEqualToSomething() throws Exception {
        // Initialize the database
        responseFieldRepository.saveAndFlush(responseField);

        // Get all the responseFieldList where name not equals to DEFAULT_NAME
        defaultResponseFieldShouldNotBeFound("name.notEquals=" + DEFAULT_NAME);

        // Get all the responseFieldList where name not equals to UPDATED_NAME
        defaultResponseFieldShouldBeFound("name.notEquals=" + UPDATED_NAME);
    }

    @Test
    @Transactional
    void getAllResponseFieldsByNameIsInShouldWork() throws Exception {
        // Initialize the database
        responseFieldRepository.saveAndFlush(responseField);

        // Get all the responseFieldList where name in DEFAULT_NAME or UPDATED_NAME
        defaultResponseFieldShouldBeFound("name.in=" + DEFAULT_NAME + "," + UPDATED_NAME);

        // Get all the responseFieldList where name equals to UPDATED_NAME
        defaultResponseFieldShouldNotBeFound("name.in=" + UPDATED_NAME);
    }

    @Test
    @Transactional
    void getAllResponseFieldsByNameIsNullOrNotNull() throws Exception {
        // Initialize the database
        responseFieldRepository.saveAndFlush(responseField);

        // Get all the responseFieldList where name is not null
        defaultResponseFieldShouldBeFound("name.specified=true");

        // Get all the responseFieldList where name is null
        defaultResponseFieldShouldNotBeFound("name.specified=false");
    }

    @Test
    @Transactional
    void getAllResponseFieldsByNameContainsSomething() throws Exception {
        // Initialize the database
        responseFieldRepository.saveAndFlush(responseField);

        // Get all the responseFieldList where name contains DEFAULT_NAME
        defaultResponseFieldShouldBeFound("name.contains=" + DEFAULT_NAME);

        // Get all the responseFieldList where name contains UPDATED_NAME
        defaultResponseFieldShouldNotBeFound("name.contains=" + UPDATED_NAME);
    }

    @Test
    @Transactional
    void getAllResponseFieldsByNameNotContainsSomething() throws Exception {
        // Initialize the database
        responseFieldRepository.saveAndFlush(responseField);

        // Get all the responseFieldList where name does not contain DEFAULT_NAME
        defaultResponseFieldShouldNotBeFound("name.doesNotContain=" + DEFAULT_NAME);

        // Get all the responseFieldList where name does not contain UPDATED_NAME
        defaultResponseFieldShouldBeFound("name.doesNotContain=" + UPDATED_NAME);
    }

    @Test
    @Transactional
    void getAllResponseFieldsByVisibilityIsEqualToSomething() throws Exception {
        // Initialize the database
        responseFieldRepository.saveAndFlush(responseField);

        // Get all the responseFieldList where visibility equals to DEFAULT_VISIBILITY
        defaultResponseFieldShouldBeFound("visibility.equals=" + DEFAULT_VISIBILITY);

        // Get all the responseFieldList where visibility equals to UPDATED_VISIBILITY
        defaultResponseFieldShouldNotBeFound("visibility.equals=" + UPDATED_VISIBILITY);
    }

    @Test
    @Transactional
    void getAllResponseFieldsByVisibilityIsNotEqualToSomething() throws Exception {
        // Initialize the database
        responseFieldRepository.saveAndFlush(responseField);

        // Get all the responseFieldList where visibility not equals to DEFAULT_VISIBILITY
        defaultResponseFieldShouldNotBeFound("visibility.notEquals=" + DEFAULT_VISIBILITY);

        // Get all the responseFieldList where visibility not equals to UPDATED_VISIBILITY
        defaultResponseFieldShouldBeFound("visibility.notEquals=" + UPDATED_VISIBILITY);
    }

    @Test
    @Transactional
    void getAllResponseFieldsByVisibilityIsInShouldWork() throws Exception {
        // Initialize the database
        responseFieldRepository.saveAndFlush(responseField);

        // Get all the responseFieldList where visibility in DEFAULT_VISIBILITY or UPDATED_VISIBILITY
        defaultResponseFieldShouldBeFound("visibility.in=" + DEFAULT_VISIBILITY + "," + UPDATED_VISIBILITY);

        // Get all the responseFieldList where visibility equals to UPDATED_VISIBILITY
        defaultResponseFieldShouldNotBeFound("visibility.in=" + UPDATED_VISIBILITY);
    }

    @Test
    @Transactional
    void getAllResponseFieldsByVisibilityIsNullOrNotNull() throws Exception {
        // Initialize the database
        responseFieldRepository.saveAndFlush(responseField);

        // Get all the responseFieldList where visibility is not null
        defaultResponseFieldShouldBeFound("visibility.specified=true");

        // Get all the responseFieldList where visibility is null
        defaultResponseFieldShouldNotBeFound("visibility.specified=false");
    }

    @Test
    @Transactional
    void getAllResponseFieldsByOrdIsEqualToSomething() throws Exception {
        // Initialize the database
        responseFieldRepository.saveAndFlush(responseField);

        // Get all the responseFieldList where ord equals to DEFAULT_ORD
        defaultResponseFieldShouldBeFound("ord.equals=" + DEFAULT_ORD);

        // Get all the responseFieldList where ord equals to UPDATED_ORD
        defaultResponseFieldShouldNotBeFound("ord.equals=" + UPDATED_ORD);
    }

    @Test
    @Transactional
    void getAllResponseFieldsByOrdIsNotEqualToSomething() throws Exception {
        // Initialize the database
        responseFieldRepository.saveAndFlush(responseField);

        // Get all the responseFieldList where ord not equals to DEFAULT_ORD
        defaultResponseFieldShouldNotBeFound("ord.notEquals=" + DEFAULT_ORD);

        // Get all the responseFieldList where ord not equals to UPDATED_ORD
        defaultResponseFieldShouldBeFound("ord.notEquals=" + UPDATED_ORD);
    }

    @Test
    @Transactional
    void getAllResponseFieldsByOrdIsInShouldWork() throws Exception {
        // Initialize the database
        responseFieldRepository.saveAndFlush(responseField);

        // Get all the responseFieldList where ord in DEFAULT_ORD or UPDATED_ORD
        defaultResponseFieldShouldBeFound("ord.in=" + DEFAULT_ORD + "," + UPDATED_ORD);

        // Get all the responseFieldList where ord equals to UPDATED_ORD
        defaultResponseFieldShouldNotBeFound("ord.in=" + UPDATED_ORD);
    }

    @Test
    @Transactional
    void getAllResponseFieldsByOrdIsNullOrNotNull() throws Exception {
        // Initialize the database
        responseFieldRepository.saveAndFlush(responseField);

        // Get all the responseFieldList where ord is not null
        defaultResponseFieldShouldBeFound("ord.specified=true");

        // Get all the responseFieldList where ord is null
        defaultResponseFieldShouldNotBeFound("ord.specified=false");
    }

    @Test
    @Transactional
    void getAllResponseFieldsByOrdIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        responseFieldRepository.saveAndFlush(responseField);

        // Get all the responseFieldList where ord is greater than or equal to DEFAULT_ORD
        defaultResponseFieldShouldBeFound("ord.greaterThanOrEqual=" + DEFAULT_ORD);

        // Get all the responseFieldList where ord is greater than or equal to UPDATED_ORD
        defaultResponseFieldShouldNotBeFound("ord.greaterThanOrEqual=" + UPDATED_ORD);
    }

    @Test
    @Transactional
    void getAllResponseFieldsByOrdIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        responseFieldRepository.saveAndFlush(responseField);

        // Get all the responseFieldList where ord is less than or equal to DEFAULT_ORD
        defaultResponseFieldShouldBeFound("ord.lessThanOrEqual=" + DEFAULT_ORD);

        // Get all the responseFieldList where ord is less than or equal to SMALLER_ORD
        defaultResponseFieldShouldNotBeFound("ord.lessThanOrEqual=" + SMALLER_ORD);
    }

    @Test
    @Transactional
    void getAllResponseFieldsByOrdIsLessThanSomething() throws Exception {
        // Initialize the database
        responseFieldRepository.saveAndFlush(responseField);

        // Get all the responseFieldList where ord is less than DEFAULT_ORD
        defaultResponseFieldShouldNotBeFound("ord.lessThan=" + DEFAULT_ORD);

        // Get all the responseFieldList where ord is less than UPDATED_ORD
        defaultResponseFieldShouldBeFound("ord.lessThan=" + UPDATED_ORD);
    }

    @Test
    @Transactional
    void getAllResponseFieldsByOrdIsGreaterThanSomething() throws Exception {
        // Initialize the database
        responseFieldRepository.saveAndFlush(responseField);

        // Get all the responseFieldList where ord is greater than DEFAULT_ORD
        defaultResponseFieldShouldNotBeFound("ord.greaterThan=" + DEFAULT_ORD);

        // Get all the responseFieldList where ord is greater than SMALLER_ORD
        defaultResponseFieldShouldBeFound("ord.greaterThan=" + SMALLER_ORD);
    }

    @Test
    @Transactional
    void getAllResponseFieldsByRequestTypeIsEqualToSomething() throws Exception {
        // Initialize the database
        responseFieldRepository.saveAndFlush(responseField);

        // Get all the responseFieldList where requestType equals to DEFAULT_REQUEST_TYPE
        defaultResponseFieldShouldBeFound("requestType.equals=" + DEFAULT_REQUEST_TYPE);

        // Get all the responseFieldList where requestType equals to UPDATED_REQUEST_TYPE
        defaultResponseFieldShouldNotBeFound("requestType.equals=" + UPDATED_REQUEST_TYPE);
    }

    @Test
    @Transactional
    void getAllResponseFieldsByRequestTypeIsNotEqualToSomething() throws Exception {
        // Initialize the database
        responseFieldRepository.saveAndFlush(responseField);

        // Get all the responseFieldList where requestType not equals to DEFAULT_REQUEST_TYPE
        defaultResponseFieldShouldNotBeFound("requestType.notEquals=" + DEFAULT_REQUEST_TYPE);

        // Get all the responseFieldList where requestType not equals to UPDATED_REQUEST_TYPE
        defaultResponseFieldShouldBeFound("requestType.notEquals=" + UPDATED_REQUEST_TYPE);
    }

    @Test
    @Transactional
    void getAllResponseFieldsByRequestTypeIsInShouldWork() throws Exception {
        // Initialize the database
        responseFieldRepository.saveAndFlush(responseField);

        // Get all the responseFieldList where requestType in DEFAULT_REQUEST_TYPE or UPDATED_REQUEST_TYPE
        defaultResponseFieldShouldBeFound("requestType.in=" + DEFAULT_REQUEST_TYPE + "," + UPDATED_REQUEST_TYPE);

        // Get all the responseFieldList where requestType equals to UPDATED_REQUEST_TYPE
        defaultResponseFieldShouldNotBeFound("requestType.in=" + UPDATED_REQUEST_TYPE);
    }

    @Test
    @Transactional
    void getAllResponseFieldsByRequestTypeIsNullOrNotNull() throws Exception {
        // Initialize the database
        responseFieldRepository.saveAndFlush(responseField);

        // Get all the responseFieldList where requestType is not null
        defaultResponseFieldShouldBeFound("requestType.specified=true");

        // Get all the responseFieldList where requestType is null
        defaultResponseFieldShouldNotBeFound("requestType.specified=false");
    }

    @Test
    @Transactional
    void getAllResponseFieldsByRecipientIsEqualToSomething() throws Exception {
        // Initialize the database
        responseFieldRepository.saveAndFlush(responseField);
        Recipient recipient = RecipientResourceIT.createEntity(em);
        em.persist(recipient);
        em.flush();
        responseField.setRecipient(recipient);
        responseFieldRepository.saveAndFlush(responseField);
        Long recipientId = recipient.getId();

        // Get all the responseFieldList where recipient equals to recipientId
        defaultResponseFieldShouldBeFound("recipientId.equals=" + recipientId);

        // Get all the responseFieldList where recipient equals to (recipientId + 1)
        defaultResponseFieldShouldNotBeFound("recipientId.equals=" + (recipientId + 1));
    }

    @Test
    @Transactional
    void getAllResponseFieldsByLabelIsEqualToSomething() throws Exception {
        // Initialize the database
        responseFieldRepository.saveAndFlush(responseField);
        I18n label = I18nResourceIT.createEntity(em);
        em.persist(label);
        em.flush();
        responseField.addLabel(label);
        responseFieldRepository.saveAndFlush(responseField);
        Long labelId = label.getId();

        // Get all the responseFieldList where label equals to labelId
        defaultResponseFieldShouldBeFound("labelId.equals=" + labelId);

        // Get all the responseFieldList where label equals to (labelId + 1)
        defaultResponseFieldShouldNotBeFound("labelId.equals=" + (labelId + 1));
    }

    /**
     * Executes the search, and checks that the default entity is returned.
     */
    private void defaultResponseFieldShouldBeFound(String filter) throws Exception {
        restResponseFieldMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(responseField.getId().intValue())))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME)))
            .andExpect(jsonPath("$.[*].visibility").value(hasItem(DEFAULT_VISIBILITY.toString())))
            .andExpect(jsonPath("$.[*].ord").value(hasItem(DEFAULT_ORD)))
            .andExpect(jsonPath("$.[*].requestType").value(hasItem(DEFAULT_REQUEST_TYPE.toString())));

        // Check, that the count call also returns 1
        restResponseFieldMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("1"));
    }

    /**
     * Executes the search, and checks that the default entity is not returned.
     */
    private void defaultResponseFieldShouldNotBeFound(String filter) throws Exception {
        restResponseFieldMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty());

        // Check, that the count call also returns 0
        restResponseFieldMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("0"));
    }

    @Test
    @Transactional
    void getNonExistingResponseField() throws Exception {
        // Get the responseField
        restResponseFieldMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putNewResponseField() throws Exception {
        // Initialize the database
        responseFieldRepository.saveAndFlush(responseField);

        int databaseSizeBeforeUpdate = responseFieldRepository.findAll().size();

        // Update the responseField
        ResponseField updatedResponseField = responseFieldRepository.findById(responseField.getId()).get();
        // Disconnect from session so that the updates on updatedResponseField are not directly saved in db
        em.detach(updatedResponseField);
        updatedResponseField.name(UPDATED_NAME).visibility(UPDATED_VISIBILITY).ord(UPDATED_ORD).requestType(UPDATED_REQUEST_TYPE);
        ResponseFieldDTO responseFieldDTO = responseFieldMapper.toDto(updatedResponseField);

        restResponseFieldMockMvc
            .perform(
                put(ENTITY_API_URL_ID, responseFieldDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(responseFieldDTO))
            )
            .andExpect(status().isOk());

        // Validate the ResponseField in the database
        List<ResponseField> responseFieldList = responseFieldRepository.findAll();
        assertThat(responseFieldList).hasSize(databaseSizeBeforeUpdate);
        ResponseField testResponseField = responseFieldList.get(responseFieldList.size() - 1);
        assertThat(testResponseField.getName()).isEqualTo(UPDATED_NAME);
        assertThat(testResponseField.getVisibility()).isEqualTo(UPDATED_VISIBILITY);
        assertThat(testResponseField.getOrd()).isEqualTo(UPDATED_ORD);
        assertThat(testResponseField.getRequestType()).isEqualTo(UPDATED_REQUEST_TYPE);
    }

    @Test
    @Transactional
    void putNonExistingResponseField() throws Exception {
        int databaseSizeBeforeUpdate = responseFieldRepository.findAll().size();
        responseField.setId(count.incrementAndGet());

        // Create the ResponseField
        ResponseFieldDTO responseFieldDTO = responseFieldMapper.toDto(responseField);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restResponseFieldMockMvc
            .perform(
                put(ENTITY_API_URL_ID, responseFieldDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(responseFieldDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the ResponseField in the database
        List<ResponseField> responseFieldList = responseFieldRepository.findAll();
        assertThat(responseFieldList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchResponseField() throws Exception {
        int databaseSizeBeforeUpdate = responseFieldRepository.findAll().size();
        responseField.setId(count.incrementAndGet());

        // Create the ResponseField
        ResponseFieldDTO responseFieldDTO = responseFieldMapper.toDto(responseField);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restResponseFieldMockMvc
            .perform(
                put(ENTITY_API_URL_ID, count.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(responseFieldDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the ResponseField in the database
        List<ResponseField> responseFieldList = responseFieldRepository.findAll();
        assertThat(responseFieldList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamResponseField() throws Exception {
        int databaseSizeBeforeUpdate = responseFieldRepository.findAll().size();
        responseField.setId(count.incrementAndGet());

        // Create the ResponseField
        ResponseFieldDTO responseFieldDTO = responseFieldMapper.toDto(responseField);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restResponseFieldMockMvc
            .perform(
                put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(responseFieldDTO))
            )
            .andExpect(status().isMethodNotAllowed());

        // Validate the ResponseField in the database
        List<ResponseField> responseFieldList = responseFieldRepository.findAll();
        assertThat(responseFieldList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateResponseFieldWithPatch() throws Exception {
        // Initialize the database
        responseFieldRepository.saveAndFlush(responseField);

        int databaseSizeBeforeUpdate = responseFieldRepository.findAll().size();

        // Update the responseField using partial update
        ResponseField partialUpdatedResponseField = new ResponseField();
        partialUpdatedResponseField.setId(responseField.getId());

        partialUpdatedResponseField.visibility(UPDATED_VISIBILITY).ord(UPDATED_ORD).requestType(UPDATED_REQUEST_TYPE);

        restResponseFieldMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedResponseField.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedResponseField))
            )
            .andExpect(status().isOk());

        // Validate the ResponseField in the database
        List<ResponseField> responseFieldList = responseFieldRepository.findAll();
        assertThat(responseFieldList).hasSize(databaseSizeBeforeUpdate);
        ResponseField testResponseField = responseFieldList.get(responseFieldList.size() - 1);
        assertThat(testResponseField.getName()).isEqualTo(DEFAULT_NAME);
        assertThat(testResponseField.getVisibility()).isEqualTo(UPDATED_VISIBILITY);
        assertThat(testResponseField.getOrd()).isEqualTo(UPDATED_ORD);
        assertThat(testResponseField.getRequestType()).isEqualTo(UPDATED_REQUEST_TYPE);
    }

    @Test
    @Transactional
    void fullUpdateResponseFieldWithPatch() throws Exception {
        // Initialize the database
        responseFieldRepository.saveAndFlush(responseField);

        int databaseSizeBeforeUpdate = responseFieldRepository.findAll().size();

        // Update the responseField using partial update
        ResponseField partialUpdatedResponseField = new ResponseField();
        partialUpdatedResponseField.setId(responseField.getId());

        partialUpdatedResponseField.name(UPDATED_NAME).visibility(UPDATED_VISIBILITY).ord(UPDATED_ORD).requestType(UPDATED_REQUEST_TYPE);

        restResponseFieldMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedResponseField.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedResponseField))
            )
            .andExpect(status().isOk());

        // Validate the ResponseField in the database
        List<ResponseField> responseFieldList = responseFieldRepository.findAll();
        assertThat(responseFieldList).hasSize(databaseSizeBeforeUpdate);
        ResponseField testResponseField = responseFieldList.get(responseFieldList.size() - 1);
        assertThat(testResponseField.getName()).isEqualTo(UPDATED_NAME);
        assertThat(testResponseField.getVisibility()).isEqualTo(UPDATED_VISIBILITY);
        assertThat(testResponseField.getOrd()).isEqualTo(UPDATED_ORD);
        assertThat(testResponseField.getRequestType()).isEqualTo(UPDATED_REQUEST_TYPE);
    }

    @Test
    @Transactional
    void patchNonExistingResponseField() throws Exception {
        int databaseSizeBeforeUpdate = responseFieldRepository.findAll().size();
        responseField.setId(count.incrementAndGet());

        // Create the ResponseField
        ResponseFieldDTO responseFieldDTO = responseFieldMapper.toDto(responseField);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restResponseFieldMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, responseFieldDTO.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(responseFieldDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the ResponseField in the database
        List<ResponseField> responseFieldList = responseFieldRepository.findAll();
        assertThat(responseFieldList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchResponseField() throws Exception {
        int databaseSizeBeforeUpdate = responseFieldRepository.findAll().size();
        responseField.setId(count.incrementAndGet());

        // Create the ResponseField
        ResponseFieldDTO responseFieldDTO = responseFieldMapper.toDto(responseField);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restResponseFieldMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, count.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(responseFieldDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the ResponseField in the database
        List<ResponseField> responseFieldList = responseFieldRepository.findAll();
        assertThat(responseFieldList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamResponseField() throws Exception {
        int databaseSizeBeforeUpdate = responseFieldRepository.findAll().size();
        responseField.setId(count.incrementAndGet());

        // Create the ResponseField
        ResponseFieldDTO responseFieldDTO = responseFieldMapper.toDto(responseField);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restResponseFieldMockMvc
            .perform(
                patch(ENTITY_API_URL)
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(responseFieldDTO))
            )
            .andExpect(status().isMethodNotAllowed());

        // Validate the ResponseField in the database
        List<ResponseField> responseFieldList = responseFieldRepository.findAll();
        assertThat(responseFieldList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteResponseField() throws Exception {
        // Initialize the database
        responseFieldRepository.saveAndFlush(responseField);

        int databaseSizeBeforeDelete = responseFieldRepository.findAll().size();

        // Delete the responseField
        restResponseFieldMockMvc
            .perform(delete(ENTITY_API_URL_ID, responseField.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        List<ResponseField> responseFieldList = responseFieldRepository.findAll();
        assertThat(responseFieldList).hasSize(databaseSizeBeforeDelete - 1);
    }
}
