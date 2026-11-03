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
import uz.devops.bmcore.enumeration.RequestType;
import uz.devops.bmcore.enumeration.Visibility;
import uz.paykap.PkbillmsApp;
import uz.paykap.IntegrationTest;
import uz.paykap.domain.I18n;
import uz.paykap.domain.Recipient;
import uz.paykap.domain.ResponseField;
import uz.paykap.repository.ResponseFieldRepository;
import uz.paykap.service.ResponseFieldQueryService;
import uz.paykap.service.ResponseFieldService;
import uz.paykap.service.dto.ResponseFieldDTO;
import uz.paykap.service.mapper.ResponseFieldMapper;
import uz.paykap.web.rest.errors.ExceptionTranslator;

/**
 * Test class for the ResponseFieldResource REST controller.
 *
 * @see ResponseFieldResource
 */
@RunWith(SpringRunner.class)
@IntegrationTest
public class ResponseFieldResourceIntTest {

    private static final String DEFAULT_NAME = "AAAAAAAAAA";
    private static final String UPDATED_NAME = "BBBBBBBBBB";

    private static final Visibility DEFAULT_VISIBILITY = Visibility.HIDDEN;
    private static final Visibility UPDATED_VISIBILITY = Visibility.PRINTER;

    private static final Integer DEFAULT_ORD = 1;
    private static final Integer UPDATED_ORD = 2;

    private static final RequestType DEFAULT_REQUEST_TYPE = RequestType.CHECK;
    private static final RequestType UPDATED_REQUEST_TYPE = RequestType.PAY;

    @Autowired
    private ResponseFieldRepository responseFieldRepository;

    @Mock
    private ResponseFieldRepository responseFieldRepositoryMock;

    @Autowired
    private ResponseFieldMapper responseFieldMapper;

    @Mock
    private ResponseFieldService responseFieldServiceMock;

    @Autowired
    private ResponseFieldService responseFieldService;

    @Autowired
    private ResponseFieldQueryService responseFieldQueryService;

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

    private MockMvc restResponseFieldMockMvc;

    private ResponseField responseField;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        final ResponseFieldResource responseFieldResource = new ResponseFieldResource(
            responseFieldService,
            responseFieldRepository,
            responseFieldQueryService
        );
        this.restResponseFieldMockMvc =
            MockMvcBuilders
                .standaloneSetup(responseFieldResource)
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
    public static ResponseField createEntity(EntityManager em) {
        return createEntity(em, RecipientResourceIntTest.createEntity(em));
    }

    /**
     * Create an entity for this test.
     * <p>
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static ResponseField createEntity(EntityManager em, Recipient recipient) {
        ResponseField responseField = new ResponseField()
            .name(DEFAULT_NAME)
            .visibility(DEFAULT_VISIBILITY)
            .ord(DEFAULT_ORD)
            .requestType(DEFAULT_REQUEST_TYPE);
        // Add required entity
        em.persist(recipient);
        em.flush();
        responseField.setRecipient(recipient);
        // Add required entity
        I18n i18n = I18nResourceIntTest.createEntity(em);
        em.persist(i18n);
        em.flush();
        responseField.getLabels().add(i18n);
        return responseField;
    }

    @Before
    public void initTest() {
        responseField = createEntity(em);
    }

    @Test
    @Transactional
    public void createResponseField() throws Exception {
        int databaseSizeBeforeCreate = responseFieldRepository.findAll().size();

        // Create the ResponseField
        ResponseFieldDTO responseFieldDTO = responseFieldMapper.toDto(responseField);
        restResponseFieldMockMvc
            .perform(
                post("/api/response-fields")
                    .contentType(TestUtil.APPLICATION_JSON_UTF8)
                    .content(TestUtil.convertObjectToJsonBytes(responseFieldDTO))
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
    public void createResponseFieldWithExistingId() throws Exception {
        int databaseSizeBeforeCreate = responseFieldRepository.findAll().size();

        // Create the ResponseField with an existing ID
        responseField.setId(1L);
        ResponseFieldDTO responseFieldDTO = responseFieldMapper.toDto(responseField);

        // An entity with an existing ID cannot be created, so this API call must fail
        restResponseFieldMockMvc
            .perform(
                post("/api/response-fields")
                    .contentType(TestUtil.APPLICATION_JSON_UTF8)
                    .content(TestUtil.convertObjectToJsonBytes(responseFieldDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the ResponseField in the database
        List<ResponseField> responseFieldList = responseFieldRepository.findAll();
        assertThat(responseFieldList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    public void checkNameIsRequired() throws Exception {
        int databaseSizeBeforeTest = responseFieldRepository.findAll().size();
        // set the field null
        responseField.setName(null);

        // Create the ResponseField, which fails.
        ResponseFieldDTO responseFieldDTO = responseFieldMapper.toDto(responseField);

        restResponseFieldMockMvc
            .perform(
                post("/api/response-fields")
                    .contentType(TestUtil.APPLICATION_JSON_UTF8)
                    .content(TestUtil.convertObjectToJsonBytes(responseFieldDTO))
            )
            .andExpect(status().isBadRequest());

        List<ResponseField> responseFieldList = responseFieldRepository.findAll();
        assertThat(responseFieldList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void getAllResponseFields() throws Exception {
        // Initialize the database
        responseFieldRepository.saveAndFlush(responseField);

        // Get all the responseFieldList
        restResponseFieldMockMvc
            .perform(get("/api/response-fields?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(responseField.getId().intValue())))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME.toString())))
            .andExpect(jsonPath("$.[*].visibility").value(hasItem(DEFAULT_VISIBILITY.toString())))
            .andExpect(jsonPath("$.[*].ord").value(hasItem(DEFAULT_ORD)))
            .andExpect(jsonPath("$.[*].requestType").value(hasItem(DEFAULT_REQUEST_TYPE.toString())));
    }

    @SuppressWarnings({ "unchecked" })
    public void getAllResponseFieldsWithEagerRelationshipsIsEnabled() throws Exception {
        ResponseFieldResource responseFieldResource = new ResponseFieldResource(
            responseFieldServiceMock,
            responseFieldRepository,
            responseFieldQueryService
        );
        when(responseFieldServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        MockMvc restResponseFieldMockMvc = MockMvcBuilders
            .standaloneSetup(responseFieldResource)
            .setCustomArgumentResolvers(pageableArgumentResolver)
            .setControllerAdvice(exceptionTranslator)
            .setConversionService(createFormattingConversionService())
            .setMessageConverters(jacksonMessageConverter)
            .build();

        restResponseFieldMockMvc.perform(get("/api/response-fields?eagerload=true")).andExpect(status().isOk());

        verify(responseFieldServiceMock, times(1)).findAllWithEagerRelationships(any());
    }

    @SuppressWarnings({ "unchecked" })
    public void getAllResponseFieldsWithEagerRelationshipsIsNotEnabled() throws Exception {
        ResponseFieldResource responseFieldResource = new ResponseFieldResource(
            responseFieldServiceMock,
            responseFieldRepository,
            responseFieldQueryService
        );
        when(responseFieldServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));
        MockMvc restResponseFieldMockMvc = MockMvcBuilders
            .standaloneSetup(responseFieldResource)
            .setCustomArgumentResolvers(pageableArgumentResolver)
            .setControllerAdvice(exceptionTranslator)
            .setConversionService(createFormattingConversionService())
            .setMessageConverters(jacksonMessageConverter)
            .build();

        restResponseFieldMockMvc.perform(get("/api/response-fields?eagerload=true")).andExpect(status().isOk());

        verify(responseFieldServiceMock, times(1)).findAllWithEagerRelationships(any());
    }

    @Test
    @Transactional
    public void getResponseField() throws Exception {
        // Initialize the database
        responseFieldRepository.saveAndFlush(responseField);

        // Get the responseField
        restResponseFieldMockMvc
            .perform(get("/api/response-fields/{id}", responseField.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.id").value(responseField.getId().intValue()))
            .andExpect(jsonPath("$.name").value(DEFAULT_NAME.toString()))
            .andExpect(jsonPath("$.visibility").value(DEFAULT_VISIBILITY.toString()))
            .andExpect(jsonPath("$.ord").value(DEFAULT_ORD))
            .andExpect(jsonPath("$.requestType").value(DEFAULT_REQUEST_TYPE.toString()));
    }

    @Test
    @Transactional
    public void getAllResponseFieldsByNameIsEqualToSomething() throws Exception {
        // Initialize the database
        responseFieldRepository.saveAndFlush(responseField);

        // Get all the responseFieldList where name equals to DEFAULT_NAME
        defaultResponseFieldShouldBeFound("name.equals=" + DEFAULT_NAME);

        // Get all the responseFieldList where name equals to UPDATED_NAME
        defaultResponseFieldShouldNotBeFound("name.equals=" + UPDATED_NAME);
    }

    @Test
    @Transactional
    public void getAllResponseFieldsByNameIsInShouldWork() throws Exception {
        // Initialize the database
        responseFieldRepository.saveAndFlush(responseField);

        // Get all the responseFieldList where name in DEFAULT_NAME or UPDATED_NAME
        defaultResponseFieldShouldBeFound("name.in=" + DEFAULT_NAME + "," + UPDATED_NAME);

        // Get all the responseFieldList where name equals to UPDATED_NAME
        defaultResponseFieldShouldNotBeFound("name.in=" + UPDATED_NAME);
    }

    @Test
    @Transactional
    public void getAllResponseFieldsByNameIsNullOrNotNull() throws Exception {
        // Initialize the database
        responseFieldRepository.saveAndFlush(responseField);

        // Get all the responseFieldList where name is not null
        defaultResponseFieldShouldBeFound("name.specified=true");

        // Get all the responseFieldList where name is null
        defaultResponseFieldShouldNotBeFound("name.specified=false");
    }

    @Test
    @Transactional
    public void getAllResponseFieldsByVisibilityIsEqualToSomething() throws Exception {
        // Initialize the database
        responseFieldRepository.saveAndFlush(responseField);

        // Get all the responseFieldList where visibility equals to DEFAULT_VISIBILITY
        defaultResponseFieldShouldBeFound("visibility.equals=" + DEFAULT_VISIBILITY);

        // Get all the responseFieldList where visibility equals to UPDATED_VISIBILITY
        defaultResponseFieldShouldNotBeFound("visibility.equals=" + UPDATED_VISIBILITY);
    }

    @Test
    @Transactional
    public void getAllResponseFieldsByVisibilityIsInShouldWork() throws Exception {
        // Initialize the database
        responseFieldRepository.saveAndFlush(responseField);

        // Get all the responseFieldList where visibility in DEFAULT_VISIBILITY or UPDATED_VISIBILITY
        defaultResponseFieldShouldBeFound("visibility.in=" + DEFAULT_VISIBILITY + "," + UPDATED_VISIBILITY);

        // Get all the responseFieldList where visibility equals to UPDATED_VISIBILITY
        defaultResponseFieldShouldNotBeFound("visibility.in=" + UPDATED_VISIBILITY);
    }

    @Test
    @Transactional
    public void getAllResponseFieldsByVisibilityIsNullOrNotNull() throws Exception {
        // Initialize the database
        responseFieldRepository.saveAndFlush(responseField);

        // Get all the responseFieldList where visibility is not null
        defaultResponseFieldShouldBeFound("visibility.specified=true");

        // Get all the responseFieldList where visibility is null
        defaultResponseFieldShouldNotBeFound("visibility.specified=false");
    }

    @Test
    @Transactional
    public void getAllResponseFieldsByOrdIsEqualToSomething() throws Exception {
        // Initialize the database
        responseFieldRepository.saveAndFlush(responseField);

        // Get all the responseFieldList where ord equals to DEFAULT_ORD
        defaultResponseFieldShouldBeFound("ord.equals=" + DEFAULT_ORD);

        // Get all the responseFieldList where ord equals to UPDATED_ORD
        defaultResponseFieldShouldNotBeFound("ord.equals=" + UPDATED_ORD);
    }

    @Test
    @Transactional
    public void getAllResponseFieldsByOrdIsInShouldWork() throws Exception {
        // Initialize the database
        responseFieldRepository.saveAndFlush(responseField);

        // Get all the responseFieldList where ord in DEFAULT_ORD or UPDATED_ORD
        defaultResponseFieldShouldBeFound("ord.in=" + DEFAULT_ORD + "," + UPDATED_ORD);

        // Get all the responseFieldList where ord equals to UPDATED_ORD
        defaultResponseFieldShouldNotBeFound("ord.in=" + UPDATED_ORD);
    }

    @Test
    @Transactional
    public void getAllResponseFieldsByOrdIsNullOrNotNull() throws Exception {
        // Initialize the database
        responseFieldRepository.saveAndFlush(responseField);

        // Get all the responseFieldList where ord is not null
        defaultResponseFieldShouldBeFound("ord.specified=true");

        // Get all the responseFieldList where ord is null
        defaultResponseFieldShouldNotBeFound("ord.specified=false");
    }

    @Test
    @Transactional
    public void getAllResponseFieldsByOrdIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        responseFieldRepository.saveAndFlush(responseField);

        // Get all the responseFieldList where ord greater than or equals to DEFAULT_ORD
        defaultResponseFieldShouldBeFound("ord.greaterOrEqualThan=" + DEFAULT_ORD);

        // Get all the responseFieldList where ord greater than or equals to UPDATED_ORD
        defaultResponseFieldShouldNotBeFound("ord.greaterOrEqualThan=" + UPDATED_ORD);
    }

    @Test
    @Transactional
    public void getAllResponseFieldsByOrdIsLessThanSomething() throws Exception {
        // Initialize the database
        responseFieldRepository.saveAndFlush(responseField);

        // Get all the responseFieldList where ord less than or equals to DEFAULT_ORD
        defaultResponseFieldShouldNotBeFound("ord.lessThan=" + DEFAULT_ORD);

        // Get all the responseFieldList where ord less than or equals to UPDATED_ORD
        defaultResponseFieldShouldBeFound("ord.lessThan=" + UPDATED_ORD);
    }

    @Test
    @Transactional
    public void getAllResponseFieldsByRequestTypeIsEqualToSomething() throws Exception {
        // Initialize the database
        responseFieldRepository.saveAndFlush(responseField);

        // Get all the responseFieldList where requestType equals to DEFAULT_REQUEST_TYPE
        defaultResponseFieldShouldBeFound("requestType.equals=" + DEFAULT_REQUEST_TYPE);

        // Get all the responseFieldList where requestType equals to UPDATED_REQUEST_TYPE
        defaultResponseFieldShouldNotBeFound("requestType.equals=" + UPDATED_REQUEST_TYPE);
    }

    @Test
    @Transactional
    public void getAllResponseFieldsByRequestTypeIsInShouldWork() throws Exception {
        // Initialize the database
        responseFieldRepository.saveAndFlush(responseField);

        // Get all the responseFieldList where requestType in DEFAULT_REQUEST_TYPE or UPDATED_REQUEST_TYPE
        defaultResponseFieldShouldBeFound("requestType.in=" + DEFAULT_REQUEST_TYPE + "," + UPDATED_REQUEST_TYPE);

        // Get all the responseFieldList where requestType equals to UPDATED_REQUEST_TYPE
        defaultResponseFieldShouldNotBeFound("requestType.in=" + UPDATED_REQUEST_TYPE);
    }

    @Test
    @Transactional
    public void getAllResponseFieldsByRequestTypeIsNullOrNotNull() throws Exception {
        // Initialize the database
        responseFieldRepository.saveAndFlush(responseField);

        // Get all the responseFieldList where requestType is not null
        defaultResponseFieldShouldBeFound("requestType.specified=true");

        // Get all the responseFieldList where requestType is null
        defaultResponseFieldShouldNotBeFound("requestType.specified=false");
    }

    @Test
    @Transactional
    public void getAllResponseFieldsByRecipientIsEqualToSomething() throws Exception {
        // Initialize the database
        Recipient recipient = RecipientResourceIntTest.createEntity(em);
        em.persist(recipient);
        em.flush();
        responseField.setRecipient(recipient);
        responseFieldRepository.saveAndFlush(responseField);
        Long recipientId = recipient.getId();

        // Get all the responseFieldList where recipient equals to recipientId
        defaultResponseFieldShouldBeFound("recipientId.equals=" + recipientId);

        // Get all the responseFieldList where recipient equals to recipientId + 1
        defaultResponseFieldShouldNotBeFound("recipientId.equals=" + (recipientId + 1));
    }

    @Test
    @Transactional
    public void getAllResponseFieldsByLabelIsEqualToSomething() throws Exception {
        // Initialize the database
        I18n label = I18nResourceIntTest.createEntity(em);
        em.persist(label);
        em.flush();
        responseField.addLabel(label);
        responseFieldRepository.saveAndFlush(responseField);
        Long labelId = label.getId();

        // Get all the responseFieldList where label equals to labelId
        defaultResponseFieldShouldBeFound("labelId.equals=" + labelId);

        // Get all the responseFieldList where label equals to labelId + 1
        defaultResponseFieldShouldNotBeFound("labelId.equals=" + (labelId + 1));
    }

    /**
     * Executes the search, and checks that the default entity is returned
     */
    private void defaultResponseFieldShouldBeFound(String filter) throws Exception {
        restResponseFieldMockMvc
            .perform(get("/api/response-fields?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(responseField.getId().intValue())))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME.toString())))
            .andExpect(jsonPath("$.[*].visibility").value(hasItem(DEFAULT_VISIBILITY.toString())))
            .andExpect(jsonPath("$.[*].ord").value(hasItem(DEFAULT_ORD)))
            .andExpect(jsonPath("$.[*].requestType").value(hasItem(DEFAULT_REQUEST_TYPE.toString())));

        // Check, that the count call also returns 1
        restResponseFieldMockMvc
            .perform(get("/api/response-fields/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(content().string("1"));
    }

    /**
     * Executes the search, and checks that the default entity is not returned
     */
    private void defaultResponseFieldShouldNotBeFound(String filter) throws Exception {
        restResponseFieldMockMvc
            .perform(get("/api/response-fields?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty());

        // Check, that the count call also returns 0
        restResponseFieldMockMvc
            .perform(get("/api/response-fields/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(content().string("0"));
    }

    @Test
    @Transactional
    public void getNonExistingResponseField() throws Exception {
        // Get the responseField
        restResponseFieldMockMvc.perform(get("/api/response-fields/{id}", Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updateResponseField() throws Exception {
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
                put("/api/response-fields")
                    .contentType(TestUtil.APPLICATION_JSON_UTF8)
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
    public void updateNonExistingResponseField() throws Exception {
        int databaseSizeBeforeUpdate = responseFieldRepository.findAll().size();

        // Create the ResponseField
        ResponseFieldDTO responseFieldDTO = responseFieldMapper.toDto(responseField);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restResponseFieldMockMvc
            .perform(
                put("/api/response-fields")
                    .contentType(TestUtil.APPLICATION_JSON_UTF8)
                    .content(TestUtil.convertObjectToJsonBytes(responseFieldDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the ResponseField in the database
        List<ResponseField> responseFieldList = responseFieldRepository.findAll();
        assertThat(responseFieldList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    public void deleteResponseField() throws Exception {
        // Initialize the database
        responseFieldRepository.saveAndFlush(responseField);

        int databaseSizeBeforeDelete = responseFieldRepository.findAll().size();

        // Get the responseField
        restResponseFieldMockMvc
            .perform(delete("/api/response-fields/{id}", responseField.getId()).accept(TestUtil.APPLICATION_JSON_UTF8))
            .andExpect(status().isOk());

        // Validate the database is empty
        List<ResponseField> responseFieldList = responseFieldRepository.findAll();
        assertThat(responseFieldList).hasSize(databaseSizeBeforeDelete - 1);
    }

    @Test
    @Transactional
    public void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(ResponseField.class);
        ResponseField responseField1 = new ResponseField();
        responseField1.setId(1L);
        ResponseField responseField2 = new ResponseField();
        responseField2.setId(responseField1.getId());
        assertThat(responseField1).isEqualTo(responseField2);
        responseField2.setId(2L);
        assertThat(responseField1).isNotEqualTo(responseField2);
        responseField1.setId(null);
        assertThat(responseField1).isNotEqualTo(responseField2);
    }

    @Test
    @Transactional
    public void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(ResponseFieldDTO.class);
        ResponseFieldDTO responseFieldDTO1 = new ResponseFieldDTO();
        responseFieldDTO1.setId(1L);
        ResponseFieldDTO responseFieldDTO2 = new ResponseFieldDTO();
        assertThat(responseFieldDTO1).isNotEqualTo(responseFieldDTO2);
        responseFieldDTO2.setId(responseFieldDTO1.getId());
        assertThat(responseFieldDTO1).isEqualTo(responseFieldDTO2);
        responseFieldDTO2.setId(2L);
        assertThat(responseFieldDTO1).isNotEqualTo(responseFieldDTO2);
        responseFieldDTO1.setId(null);
        assertThat(responseFieldDTO1).isNotEqualTo(responseFieldDTO2);
    }
    //    @Test
    //    @Transactional
    //    public void testEntityFromId() {
    //        assertThat(responseFieldMapper.fromId(42L).getId()).isEqualTo(42);
    //        assertThat(responseFieldMapper.fromId(null)).isNull();
    //    }
}
