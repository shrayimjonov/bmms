package uz.paykap.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;
import javax.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import uz.devops.bmcore.enumeration.RequestType;
import uz.paykap.IntegrationTest;
import uz.paykap.domain.GatewayParam;
import uz.paykap.domain.Recipient;
import uz.paykap.domain.enumeration.MethodType;
import uz.paykap.domain.enumeration.ModeType;
import uz.paykap.domain.enumeration.ProtocolType;
import uz.paykap.repository.GatewayParamRepository;
import uz.paykap.service.dto.GatewayParamDTO;
import uz.paykap.service.mapper.GatewayParamMapper;

/**
 * Integration tests for the {@link GatewayParamResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class GatewayParamResourceIT {

    private static final String DEFAULT_URL = "AAAAAAAAAA";
    private static final String UPDATED_URL = "BBBBBBBBBB";

    private static final RequestType DEFAULT_TYPE = RequestType.CHECK;
    private static final RequestType UPDATED_TYPE = RequestType.PAY;

    private static final String DEFAULT_USERNAME = "AAAAAAAAAA";
    private static final String UPDATED_USERNAME = "BBBBBBBBBB";

    private static final String DEFAULT_PASSWORD = "AAAAAAAAAA";
    private static final String UPDATED_PASSWORD = "BBBBBBBBBB";

    private static final String DEFAULT_TOKEN = "AAAAAAAAAA";
    private static final String UPDATED_TOKEN = "BBBBBBBBBB";

    private static final String DEFAULT_BODY = "AAAAAAAAAA";
    private static final String UPDATED_BODY = "BBBBBBBBBB";

    private static final String DEFAULT_STATUS_QUERY = "AAAAAAAAAA";
    private static final String UPDATED_STATUS_QUERY = "BBBBBBBBBB";

    private static final String DEFAULT_MESSAGE_QUERY = "AAAAAAAAAA";
    private static final String UPDATED_MESSAGE_QUERY = "BBBBBBBBBB";

    private static final String DEFAULT_SUCCESS_CODE = "AAAAAAAAAA";
    private static final String UPDATED_SUCCESS_CODE = "BBBBBBBBBB";

    private static final String DEFAULT_CALLBACK = "AAAAAAAAAA";
    private static final String UPDATED_CALLBACK = "BBBBBBBBBB";

    private static final Integer DEFAULT_TIMEOUT = 1;
    private static final Integer UPDATED_TIMEOUT = 2;
    private static final Integer SMALLER_TIMEOUT = 1 - 1;

    private static final String DEFAULT_HEADERS = "AAAAAAAAAA";
    private static final String UPDATED_HEADERS = "BBBBBBBBBB";

    private static final String DEFAULT_DATA_QUERIES = "AAAAAAAAAA";
    private static final String UPDATED_DATA_QUERIES = "BBBBBBBBBB";

    private static final MethodType DEFAULT_HTTP_METHOD_TYPE = MethodType.GET;
    private static final MethodType UPDATED_HTTP_METHOD_TYPE = MethodType.POST;

    private static final ProtocolType DEFAULT_PROTOCOL = ProtocolType.REST;
    private static final ProtocolType UPDATED_PROTOCOL = ProtocolType.WS;

    private static final String DEFAULT_REQUEST_MEDIA_TYPE = "application/json";
    private static final String UPDATED_REQUEST_MEDIA_TYPE = "BBBBBBBBBB";

    private static final String DEFAULT_RESPONSE_MEDIA_TYPE = "application/json";
    private static final String UPDATED_RESPONSE_MEDIA_TYPE = "BBBBBBBBBB";

    private static final Boolean DEFAULT_ACTIVE = false;
    private static final Boolean UPDATED_ACTIVE = true;

    private static final ModeType DEFAULT_MODE = ModeType.TEST;
    private static final ModeType UPDATED_MODE = ModeType.DEV;

    private static final String DEFAULT_EXT_ID_QUERY = "AAAAAAAAAA";
    private static final String UPDATED_EXT_ID_QUERY = "BBBBBBBBBB";

    private static final Boolean DEFAULT_SUCCESS_ON_2_XX = false;
    private static final Boolean UPDATED_SUCCESS_ON_2_XX = true;

    private static final String ENTITY_API_URL = "/api/gateway-params";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong count = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private GatewayParamRepository gatewayParamRepository;

    @Autowired
    private GatewayParamMapper gatewayParamMapper;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restGatewayParamMockMvc;

    private GatewayParam gatewayParam;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static GatewayParam createEntity(EntityManager em) {
        GatewayParam gatewayParam = new GatewayParam()
            .url(DEFAULT_URL)
            .type(DEFAULT_TYPE)
            .username(DEFAULT_USERNAME)
            .password(DEFAULT_PASSWORD)
            .token(DEFAULT_TOKEN)
            .body(DEFAULT_BODY)
            .statusQuery(DEFAULT_STATUS_QUERY)
            .messageQuery(DEFAULT_MESSAGE_QUERY)
            .successCode(DEFAULT_SUCCESS_CODE)
            .callback(DEFAULT_CALLBACK)
            .timeout(DEFAULT_TIMEOUT)
            .headers(DEFAULT_HEADERS)
            .dataQueries(DEFAULT_DATA_QUERIES)
            .httpMethodType(DEFAULT_HTTP_METHOD_TYPE)
            .protocol(DEFAULT_PROTOCOL)
            .requestMediaType(DEFAULT_REQUEST_MEDIA_TYPE)
            .responseMediaType(DEFAULT_RESPONSE_MEDIA_TYPE)
            .active(DEFAULT_ACTIVE)
            .mode(DEFAULT_MODE)
            .extIdQuery(DEFAULT_EXT_ID_QUERY)
            .successOn2xx(DEFAULT_SUCCESS_ON_2_XX);
        return gatewayParam;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static GatewayParam createUpdatedEntity(EntityManager em) {
        GatewayParam gatewayParam = new GatewayParam()
            .url(UPDATED_URL)
            .type(UPDATED_TYPE)
            .username(UPDATED_USERNAME)
            .password(UPDATED_PASSWORD)
            .token(UPDATED_TOKEN)
            .body(UPDATED_BODY)
            .statusQuery(UPDATED_STATUS_QUERY)
            .messageQuery(UPDATED_MESSAGE_QUERY)
            .successCode(UPDATED_SUCCESS_CODE)
            .callback(UPDATED_CALLBACK)
            .timeout(UPDATED_TIMEOUT)
            .headers(UPDATED_HEADERS)
            .dataQueries(UPDATED_DATA_QUERIES)
            .httpMethodType(UPDATED_HTTP_METHOD_TYPE)
            .protocol(UPDATED_PROTOCOL)
            .requestMediaType(UPDATED_REQUEST_MEDIA_TYPE)
            .responseMediaType(UPDATED_RESPONSE_MEDIA_TYPE)
            .active(UPDATED_ACTIVE)
            .mode(UPDATED_MODE)
            .extIdQuery(UPDATED_EXT_ID_QUERY)
            .successOn2xx(UPDATED_SUCCESS_ON_2_XX);
        return gatewayParam;
    }

    @BeforeEach
    public void initTest() {
        gatewayParam = createEntity(em);
    }

    @Test
    @Transactional
    void createGatewayParam() throws Exception {
        int databaseSizeBeforeCreate = gatewayParamRepository.findAll().size();
        // Create the GatewayParam
        GatewayParamDTO gatewayParamDTO = gatewayParamMapper.toDto(gatewayParam);
        restGatewayParamMockMvc
            .perform(
                post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(gatewayParamDTO))
            )
            .andExpect(status().isCreated());

        // Validate the GatewayParam in the database
        List<GatewayParam> gatewayParamList = gatewayParamRepository.findAll();
        assertThat(gatewayParamList).hasSize(databaseSizeBeforeCreate + 1);
        GatewayParam testGatewayParam = gatewayParamList.get(gatewayParamList.size() - 1);
        assertThat(testGatewayParam.getUrl()).isEqualTo(DEFAULT_URL);
        assertThat(testGatewayParam.getType()).isEqualTo(DEFAULT_TYPE);
        assertThat(testGatewayParam.getUsername()).isEqualTo(DEFAULT_USERNAME);
        assertThat(testGatewayParam.getPassword()).isEqualTo(DEFAULT_PASSWORD);
        assertThat(testGatewayParam.getToken()).isEqualTo(DEFAULT_TOKEN);
        assertThat(testGatewayParam.getBody()).isEqualTo(DEFAULT_BODY);
        assertThat(testGatewayParam.getStatusQuery()).isEqualTo(DEFAULT_STATUS_QUERY);
        assertThat(testGatewayParam.getMessageQuery()).isEqualTo(DEFAULT_MESSAGE_QUERY);
        assertThat(testGatewayParam.getSuccessCode()).isEqualTo(DEFAULT_SUCCESS_CODE);
        assertThat(testGatewayParam.getCallback()).isEqualTo(DEFAULT_CALLBACK);
        assertThat(testGatewayParam.getTimeout()).isEqualTo(DEFAULT_TIMEOUT);
        assertThat(testGatewayParam.getHeaders()).isEqualTo(DEFAULT_HEADERS);
        assertThat(testGatewayParam.getDataQueries()).isEqualTo(DEFAULT_DATA_QUERIES);
        assertThat(testGatewayParam.getHttpMethodType()).isEqualTo(DEFAULT_HTTP_METHOD_TYPE);
        assertThat(testGatewayParam.getProtocol()).isEqualTo(DEFAULT_PROTOCOL);
        assertThat(testGatewayParam.getRequestMediaType()).isEqualTo(DEFAULT_REQUEST_MEDIA_TYPE);
        assertThat(testGatewayParam.getResponseMediaType()).isEqualTo(DEFAULT_RESPONSE_MEDIA_TYPE);
        assertThat(testGatewayParam.getActive()).isEqualTo(DEFAULT_ACTIVE);
        assertThat(testGatewayParam.getMode()).isEqualTo(DEFAULT_MODE);
        assertThat(testGatewayParam.getExtIdQuery()).isEqualTo(DEFAULT_EXT_ID_QUERY);
        assertThat(testGatewayParam.getSuccessOn2xx()).isEqualTo(DEFAULT_SUCCESS_ON_2_XX);
    }

    @Test
    @Transactional
    void createGatewayParamWithExistingId() throws Exception {
        // Create the GatewayParam with an existing ID
        gatewayParam.setId(1L);
        GatewayParamDTO gatewayParamDTO = gatewayParamMapper.toDto(gatewayParam);

        int databaseSizeBeforeCreate = gatewayParamRepository.findAll().size();

        // An entity with an existing ID cannot be created, so this API call must fail
        restGatewayParamMockMvc
            .perform(
                post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(gatewayParamDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the GatewayParam in the database
        List<GatewayParam> gatewayParamList = gatewayParamRepository.findAll();
        assertThat(gatewayParamList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkActiveIsRequired() throws Exception {
        int databaseSizeBeforeTest = gatewayParamRepository.findAll().size();
        // set the field null
        gatewayParam.setActive(null);

        // Create the GatewayParam, which fails.
        GatewayParamDTO gatewayParamDTO = gatewayParamMapper.toDto(gatewayParam);

        restGatewayParamMockMvc
            .perform(
                post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(gatewayParamDTO))
            )
            .andExpect(status().isBadRequest());

        List<GatewayParam> gatewayParamList = gatewayParamRepository.findAll();
        assertThat(gatewayParamList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllGatewayParams() throws Exception {
        // Initialize the database
        gatewayParamRepository.saveAndFlush(gatewayParam);

        // Get all the gatewayParamList
        restGatewayParamMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(gatewayParam.getId().intValue())))
            .andExpect(jsonPath("$.[*].url").value(hasItem(DEFAULT_URL)))
            .andExpect(jsonPath("$.[*].type").value(hasItem(DEFAULT_TYPE.toString())))
            .andExpect(jsonPath("$.[*].username").value(hasItem(DEFAULT_USERNAME)))
            .andExpect(jsonPath("$.[*].password").value(hasItem(DEFAULT_PASSWORD)))
            .andExpect(jsonPath("$.[*].token").value(hasItem(DEFAULT_TOKEN)))
            .andExpect(jsonPath("$.[*].body").value(hasItem(DEFAULT_BODY)))
            .andExpect(jsonPath("$.[*].statusQuery").value(hasItem(DEFAULT_STATUS_QUERY)))
            .andExpect(jsonPath("$.[*].messageQuery").value(hasItem(DEFAULT_MESSAGE_QUERY)))
            .andExpect(jsonPath("$.[*].successCode").value(hasItem(DEFAULT_SUCCESS_CODE)))
            .andExpect(jsonPath("$.[*].callback").value(hasItem(DEFAULT_CALLBACK)))
            .andExpect(jsonPath("$.[*].timeout").value(hasItem(DEFAULT_TIMEOUT)))
            .andExpect(jsonPath("$.[*].headers").value(hasItem(DEFAULT_HEADERS)))
            .andExpect(jsonPath("$.[*].dataQueries").value(hasItem(DEFAULT_DATA_QUERIES)))
            .andExpect(jsonPath("$.[*].httpMethodType").value(hasItem(DEFAULT_HTTP_METHOD_TYPE.toString())))
            .andExpect(jsonPath("$.[*].protocol").value(hasItem(DEFAULT_PROTOCOL.toString())))
            .andExpect(jsonPath("$.[*].requestMediaType").value(hasItem(DEFAULT_REQUEST_MEDIA_TYPE)))
            .andExpect(jsonPath("$.[*].responseMediaType").value(hasItem(DEFAULT_RESPONSE_MEDIA_TYPE)))
            .andExpect(jsonPath("$.[*].active").value(hasItem(DEFAULT_ACTIVE.booleanValue())))
            .andExpect(jsonPath("$.[*].mode").value(hasItem(DEFAULT_MODE.toString())))
            .andExpect(jsonPath("$.[*].extIdQuery").value(hasItem(DEFAULT_EXT_ID_QUERY)))
            .andExpect(jsonPath("$.[*].successOn2xx").value(hasItem(DEFAULT_SUCCESS_ON_2_XX.booleanValue())));
    }

    @Test
    @Transactional
    void getGatewayParam() throws Exception {
        // Initialize the database
        gatewayParamRepository.saveAndFlush(gatewayParam);

        // Get the gatewayParam
        restGatewayParamMockMvc
            .perform(get(ENTITY_API_URL_ID, gatewayParam.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(gatewayParam.getId().intValue()))
            .andExpect(jsonPath("$.url").value(DEFAULT_URL))
            .andExpect(jsonPath("$.type").value(DEFAULT_TYPE.toString()))
            .andExpect(jsonPath("$.username").value(DEFAULT_USERNAME))
            .andExpect(jsonPath("$.password").value(DEFAULT_PASSWORD))
            .andExpect(jsonPath("$.token").value(DEFAULT_TOKEN))
            .andExpect(jsonPath("$.body").value(DEFAULT_BODY))
            .andExpect(jsonPath("$.statusQuery").value(DEFAULT_STATUS_QUERY))
            .andExpect(jsonPath("$.messageQuery").value(DEFAULT_MESSAGE_QUERY))
            .andExpect(jsonPath("$.successCode").value(DEFAULT_SUCCESS_CODE))
            .andExpect(jsonPath("$.callback").value(DEFAULT_CALLBACK))
            .andExpect(jsonPath("$.timeout").value(DEFAULT_TIMEOUT))
            .andExpect(jsonPath("$.headers").value(DEFAULT_HEADERS))
            .andExpect(jsonPath("$.dataQueries").value(DEFAULT_DATA_QUERIES))
            .andExpect(jsonPath("$.httpMethodType").value(DEFAULT_HTTP_METHOD_TYPE.toString()))
            .andExpect(jsonPath("$.protocol").value(DEFAULT_PROTOCOL.toString()))
            .andExpect(jsonPath("$.requestMediaType").value(DEFAULT_REQUEST_MEDIA_TYPE))
            .andExpect(jsonPath("$.responseMediaType").value(DEFAULT_RESPONSE_MEDIA_TYPE))
            .andExpect(jsonPath("$.active").value(DEFAULT_ACTIVE.booleanValue()))
            .andExpect(jsonPath("$.mode").value(DEFAULT_MODE.toString()))
            .andExpect(jsonPath("$.extIdQuery").value(DEFAULT_EXT_ID_QUERY))
            .andExpect(jsonPath("$.successOn2xx").value(DEFAULT_SUCCESS_ON_2_XX.booleanValue()));
    }

    @Test
    @Transactional
    void getGatewayParamsByIdFiltering() throws Exception {
        // Initialize the database
        gatewayParamRepository.saveAndFlush(gatewayParam);

        Long id = gatewayParam.getId();

        defaultGatewayParamShouldBeFound("id.equals=" + id);
        defaultGatewayParamShouldNotBeFound("id.notEquals=" + id);

        defaultGatewayParamShouldBeFound("id.greaterThanOrEqual=" + id);
        defaultGatewayParamShouldNotBeFound("id.greaterThan=" + id);

        defaultGatewayParamShouldBeFound("id.lessThanOrEqual=" + id);
        defaultGatewayParamShouldNotBeFound("id.lessThan=" + id);
    }

    @Test
    @Transactional
    void getAllGatewayParamsByUrlIsEqualToSomething() throws Exception {
        // Initialize the database
        gatewayParamRepository.saveAndFlush(gatewayParam);

        // Get all the gatewayParamList where url equals to DEFAULT_URL
        defaultGatewayParamShouldBeFound("url.equals=" + DEFAULT_URL);

        // Get all the gatewayParamList where url equals to UPDATED_URL
        defaultGatewayParamShouldNotBeFound("url.equals=" + UPDATED_URL);
    }

    @Test
    @Transactional
    void getAllGatewayParamsByUrlIsNotEqualToSomething() throws Exception {
        // Initialize the database
        gatewayParamRepository.saveAndFlush(gatewayParam);

        // Get all the gatewayParamList where url not equals to DEFAULT_URL
        defaultGatewayParamShouldNotBeFound("url.notEquals=" + DEFAULT_URL);

        // Get all the gatewayParamList where url not equals to UPDATED_URL
        defaultGatewayParamShouldBeFound("url.notEquals=" + UPDATED_URL);
    }

    @Test
    @Transactional
    void getAllGatewayParamsByUrlIsInShouldWork() throws Exception {
        // Initialize the database
        gatewayParamRepository.saveAndFlush(gatewayParam);

        // Get all the gatewayParamList where url in DEFAULT_URL or UPDATED_URL
        defaultGatewayParamShouldBeFound("url.in=" + DEFAULT_URL + "," + UPDATED_URL);

        // Get all the gatewayParamList where url equals to UPDATED_URL
        defaultGatewayParamShouldNotBeFound("url.in=" + UPDATED_URL);
    }

    @Test
    @Transactional
    void getAllGatewayParamsByUrlIsNullOrNotNull() throws Exception {
        // Initialize the database
        gatewayParamRepository.saveAndFlush(gatewayParam);

        // Get all the gatewayParamList where url is not null
        defaultGatewayParamShouldBeFound("url.specified=true");

        // Get all the gatewayParamList where url is null
        defaultGatewayParamShouldNotBeFound("url.specified=false");
    }

    @Test
    @Transactional
    void getAllGatewayParamsByUrlContainsSomething() throws Exception {
        // Initialize the database
        gatewayParamRepository.saveAndFlush(gatewayParam);

        // Get all the gatewayParamList where url contains DEFAULT_URL
        defaultGatewayParamShouldBeFound("url.contains=" + DEFAULT_URL);

        // Get all the gatewayParamList where url contains UPDATED_URL
        defaultGatewayParamShouldNotBeFound("url.contains=" + UPDATED_URL);
    }

    @Test
    @Transactional
    void getAllGatewayParamsByUrlNotContainsSomething() throws Exception {
        // Initialize the database
        gatewayParamRepository.saveAndFlush(gatewayParam);

        // Get all the gatewayParamList where url does not contain DEFAULT_URL
        defaultGatewayParamShouldNotBeFound("url.doesNotContain=" + DEFAULT_URL);

        // Get all the gatewayParamList where url does not contain UPDATED_URL
        defaultGatewayParamShouldBeFound("url.doesNotContain=" + UPDATED_URL);
    }

    @Test
    @Transactional
    void getAllGatewayParamsByTypeIsEqualToSomething() throws Exception {
        // Initialize the database
        gatewayParamRepository.saveAndFlush(gatewayParam);

        // Get all the gatewayParamList where type equals to DEFAULT_TYPE
        defaultGatewayParamShouldBeFound("type.equals=" + DEFAULT_TYPE);

        // Get all the gatewayParamList where type equals to UPDATED_TYPE
        defaultGatewayParamShouldNotBeFound("type.equals=" + UPDATED_TYPE);
    }

    @Test
    @Transactional
    void getAllGatewayParamsByTypeIsNotEqualToSomething() throws Exception {
        // Initialize the database
        gatewayParamRepository.saveAndFlush(gatewayParam);

        // Get all the gatewayParamList where type not equals to DEFAULT_TYPE
        defaultGatewayParamShouldNotBeFound("type.notEquals=" + DEFAULT_TYPE);

        // Get all the gatewayParamList where type not equals to UPDATED_TYPE
        defaultGatewayParamShouldBeFound("type.notEquals=" + UPDATED_TYPE);
    }

    @Test
    @Transactional
    void getAllGatewayParamsByTypeIsInShouldWork() throws Exception {
        // Initialize the database
        gatewayParamRepository.saveAndFlush(gatewayParam);

        // Get all the gatewayParamList where type in DEFAULT_TYPE or UPDATED_TYPE
        defaultGatewayParamShouldBeFound("type.in=" + DEFAULT_TYPE + "," + UPDATED_TYPE);

        // Get all the gatewayParamList where type equals to UPDATED_TYPE
        defaultGatewayParamShouldNotBeFound("type.in=" + UPDATED_TYPE);
    }

    @Test
    @Transactional
    void getAllGatewayParamsByTypeIsNullOrNotNull() throws Exception {
        // Initialize the database
        gatewayParamRepository.saveAndFlush(gatewayParam);

        // Get all the gatewayParamList where type is not null
        defaultGatewayParamShouldBeFound("type.specified=true");

        // Get all the gatewayParamList where type is null
        defaultGatewayParamShouldNotBeFound("type.specified=false");
    }

    @Test
    @Transactional
    void getAllGatewayParamsByUsernameIsEqualToSomething() throws Exception {
        // Initialize the database
        gatewayParamRepository.saveAndFlush(gatewayParam);

        // Get all the gatewayParamList where username equals to DEFAULT_USERNAME
        defaultGatewayParamShouldBeFound("username.equals=" + DEFAULT_USERNAME);

        // Get all the gatewayParamList where username equals to UPDATED_USERNAME
        defaultGatewayParamShouldNotBeFound("username.equals=" + UPDATED_USERNAME);
    }

    @Test
    @Transactional
    void getAllGatewayParamsByUsernameIsNotEqualToSomething() throws Exception {
        // Initialize the database
        gatewayParamRepository.saveAndFlush(gatewayParam);

        // Get all the gatewayParamList where username not equals to DEFAULT_USERNAME
        defaultGatewayParamShouldNotBeFound("username.notEquals=" + DEFAULT_USERNAME);

        // Get all the gatewayParamList where username not equals to UPDATED_USERNAME
        defaultGatewayParamShouldBeFound("username.notEquals=" + UPDATED_USERNAME);
    }

    @Test
    @Transactional
    void getAllGatewayParamsByUsernameIsInShouldWork() throws Exception {
        // Initialize the database
        gatewayParamRepository.saveAndFlush(gatewayParam);

        // Get all the gatewayParamList where username in DEFAULT_USERNAME or UPDATED_USERNAME
        defaultGatewayParamShouldBeFound("username.in=" + DEFAULT_USERNAME + "," + UPDATED_USERNAME);

        // Get all the gatewayParamList where username equals to UPDATED_USERNAME
        defaultGatewayParamShouldNotBeFound("username.in=" + UPDATED_USERNAME);
    }

    @Test
    @Transactional
    void getAllGatewayParamsByUsernameIsNullOrNotNull() throws Exception {
        // Initialize the database
        gatewayParamRepository.saveAndFlush(gatewayParam);

        // Get all the gatewayParamList where username is not null
        defaultGatewayParamShouldBeFound("username.specified=true");

        // Get all the gatewayParamList where username is null
        defaultGatewayParamShouldNotBeFound("username.specified=false");
    }

    @Test
    @Transactional
    void getAllGatewayParamsByUsernameContainsSomething() throws Exception {
        // Initialize the database
        gatewayParamRepository.saveAndFlush(gatewayParam);

        // Get all the gatewayParamList where username contains DEFAULT_USERNAME
        defaultGatewayParamShouldBeFound("username.contains=" + DEFAULT_USERNAME);

        // Get all the gatewayParamList where username contains UPDATED_USERNAME
        defaultGatewayParamShouldNotBeFound("username.contains=" + UPDATED_USERNAME);
    }

    @Test
    @Transactional
    void getAllGatewayParamsByUsernameNotContainsSomething() throws Exception {
        // Initialize the database
        gatewayParamRepository.saveAndFlush(gatewayParam);

        // Get all the gatewayParamList where username does not contain DEFAULT_USERNAME
        defaultGatewayParamShouldNotBeFound("username.doesNotContain=" + DEFAULT_USERNAME);

        // Get all the gatewayParamList where username does not contain UPDATED_USERNAME
        defaultGatewayParamShouldBeFound("username.doesNotContain=" + UPDATED_USERNAME);
    }

    @Test
    @Transactional
    void getAllGatewayParamsByPasswordIsEqualToSomething() throws Exception {
        // Initialize the database
        gatewayParamRepository.saveAndFlush(gatewayParam);

        // Get all the gatewayParamList where password equals to DEFAULT_PASSWORD
        defaultGatewayParamShouldBeFound("password.equals=" + DEFAULT_PASSWORD);

        // Get all the gatewayParamList where password equals to UPDATED_PASSWORD
        defaultGatewayParamShouldNotBeFound("password.equals=" + UPDATED_PASSWORD);
    }

    @Test
    @Transactional
    void getAllGatewayParamsByPasswordIsNotEqualToSomething() throws Exception {
        // Initialize the database
        gatewayParamRepository.saveAndFlush(gatewayParam);

        // Get all the gatewayParamList where password not equals to DEFAULT_PASSWORD
        defaultGatewayParamShouldNotBeFound("password.notEquals=" + DEFAULT_PASSWORD);

        // Get all the gatewayParamList where password not equals to UPDATED_PASSWORD
        defaultGatewayParamShouldBeFound("password.notEquals=" + UPDATED_PASSWORD);
    }

    @Test
    @Transactional
    void getAllGatewayParamsByPasswordIsInShouldWork() throws Exception {
        // Initialize the database
        gatewayParamRepository.saveAndFlush(gatewayParam);

        // Get all the gatewayParamList where password in DEFAULT_PASSWORD or UPDATED_PASSWORD
        defaultGatewayParamShouldBeFound("password.in=" + DEFAULT_PASSWORD + "," + UPDATED_PASSWORD);

        // Get all the gatewayParamList where password equals to UPDATED_PASSWORD
        defaultGatewayParamShouldNotBeFound("password.in=" + UPDATED_PASSWORD);
    }

    @Test
    @Transactional
    void getAllGatewayParamsByPasswordIsNullOrNotNull() throws Exception {
        // Initialize the database
        gatewayParamRepository.saveAndFlush(gatewayParam);

        // Get all the gatewayParamList where password is not null
        defaultGatewayParamShouldBeFound("password.specified=true");

        // Get all the gatewayParamList where password is null
        defaultGatewayParamShouldNotBeFound("password.specified=false");
    }

    @Test
    @Transactional
    void getAllGatewayParamsByPasswordContainsSomething() throws Exception {
        // Initialize the database
        gatewayParamRepository.saveAndFlush(gatewayParam);

        // Get all the gatewayParamList where password contains DEFAULT_PASSWORD
        defaultGatewayParamShouldBeFound("password.contains=" + DEFAULT_PASSWORD);

        // Get all the gatewayParamList where password contains UPDATED_PASSWORD
        defaultGatewayParamShouldNotBeFound("password.contains=" + UPDATED_PASSWORD);
    }

    @Test
    @Transactional
    void getAllGatewayParamsByPasswordNotContainsSomething() throws Exception {
        // Initialize the database
        gatewayParamRepository.saveAndFlush(gatewayParam);

        // Get all the gatewayParamList where password does not contain DEFAULT_PASSWORD
        defaultGatewayParamShouldNotBeFound("password.doesNotContain=" + DEFAULT_PASSWORD);

        // Get all the gatewayParamList where password does not contain UPDATED_PASSWORD
        defaultGatewayParamShouldBeFound("password.doesNotContain=" + UPDATED_PASSWORD);
    }

    @Test
    @Transactional
    void getAllGatewayParamsByTokenIsEqualToSomething() throws Exception {
        // Initialize the database
        gatewayParamRepository.saveAndFlush(gatewayParam);

        // Get all the gatewayParamList where token equals to DEFAULT_TOKEN
        defaultGatewayParamShouldBeFound("token.equals=" + DEFAULT_TOKEN);

        // Get all the gatewayParamList where token equals to UPDATED_TOKEN
        defaultGatewayParamShouldNotBeFound("token.equals=" + UPDATED_TOKEN);
    }

    @Test
    @Transactional
    void getAllGatewayParamsByTokenIsNotEqualToSomething() throws Exception {
        // Initialize the database
        gatewayParamRepository.saveAndFlush(gatewayParam);

        // Get all the gatewayParamList where token not equals to DEFAULT_TOKEN
        defaultGatewayParamShouldNotBeFound("token.notEquals=" + DEFAULT_TOKEN);

        // Get all the gatewayParamList where token not equals to UPDATED_TOKEN
        defaultGatewayParamShouldBeFound("token.notEquals=" + UPDATED_TOKEN);
    }

    @Test
    @Transactional
    void getAllGatewayParamsByTokenIsInShouldWork() throws Exception {
        // Initialize the database
        gatewayParamRepository.saveAndFlush(gatewayParam);

        // Get all the gatewayParamList where token in DEFAULT_TOKEN or UPDATED_TOKEN
        defaultGatewayParamShouldBeFound("token.in=" + DEFAULT_TOKEN + "," + UPDATED_TOKEN);

        // Get all the gatewayParamList where token equals to UPDATED_TOKEN
        defaultGatewayParamShouldNotBeFound("token.in=" + UPDATED_TOKEN);
    }

    @Test
    @Transactional
    void getAllGatewayParamsByTokenIsNullOrNotNull() throws Exception {
        // Initialize the database
        gatewayParamRepository.saveAndFlush(gatewayParam);

        // Get all the gatewayParamList where token is not null
        defaultGatewayParamShouldBeFound("token.specified=true");

        // Get all the gatewayParamList where token is null
        defaultGatewayParamShouldNotBeFound("token.specified=false");
    }

    @Test
    @Transactional
    void getAllGatewayParamsByTokenContainsSomething() throws Exception {
        // Initialize the database
        gatewayParamRepository.saveAndFlush(gatewayParam);

        // Get all the gatewayParamList where token contains DEFAULT_TOKEN
        defaultGatewayParamShouldBeFound("token.contains=" + DEFAULT_TOKEN);

        // Get all the gatewayParamList where token contains UPDATED_TOKEN
        defaultGatewayParamShouldNotBeFound("token.contains=" + UPDATED_TOKEN);
    }

    @Test
    @Transactional
    void getAllGatewayParamsByTokenNotContainsSomething() throws Exception {
        // Initialize the database
        gatewayParamRepository.saveAndFlush(gatewayParam);

        // Get all the gatewayParamList where token does not contain DEFAULT_TOKEN
        defaultGatewayParamShouldNotBeFound("token.doesNotContain=" + DEFAULT_TOKEN);

        // Get all the gatewayParamList where token does not contain UPDATED_TOKEN
        defaultGatewayParamShouldBeFound("token.doesNotContain=" + UPDATED_TOKEN);
    }

    @Test
    @Transactional
    void getAllGatewayParamsByBodyIsEqualToSomething() throws Exception {
        // Initialize the database
        gatewayParamRepository.saveAndFlush(gatewayParam);

        // Get all the gatewayParamList where body equals to DEFAULT_BODY
        defaultGatewayParamShouldBeFound("body.equals=" + DEFAULT_BODY);

        // Get all the gatewayParamList where body equals to UPDATED_BODY
        defaultGatewayParamShouldNotBeFound("body.equals=" + UPDATED_BODY);
    }

    @Test
    @Transactional
    void getAllGatewayParamsByBodyIsNotEqualToSomething() throws Exception {
        // Initialize the database
        gatewayParamRepository.saveAndFlush(gatewayParam);

        // Get all the gatewayParamList where body not equals to DEFAULT_BODY
        defaultGatewayParamShouldNotBeFound("body.notEquals=" + DEFAULT_BODY);

        // Get all the gatewayParamList where body not equals to UPDATED_BODY
        defaultGatewayParamShouldBeFound("body.notEquals=" + UPDATED_BODY);
    }

    @Test
    @Transactional
    void getAllGatewayParamsByBodyIsInShouldWork() throws Exception {
        // Initialize the database
        gatewayParamRepository.saveAndFlush(gatewayParam);

        // Get all the gatewayParamList where body in DEFAULT_BODY or UPDATED_BODY
        defaultGatewayParamShouldBeFound("body.in=" + DEFAULT_BODY + "," + UPDATED_BODY);

        // Get all the gatewayParamList where body equals to UPDATED_BODY
        defaultGatewayParamShouldNotBeFound("body.in=" + UPDATED_BODY);
    }

    @Test
    @Transactional
    void getAllGatewayParamsByBodyIsNullOrNotNull() throws Exception {
        // Initialize the database
        gatewayParamRepository.saveAndFlush(gatewayParam);

        // Get all the gatewayParamList where body is not null
        defaultGatewayParamShouldBeFound("body.specified=true");

        // Get all the gatewayParamList where body is null
        defaultGatewayParamShouldNotBeFound("body.specified=false");
    }

    @Test
    @Transactional
    void getAllGatewayParamsByBodyContainsSomething() throws Exception {
        // Initialize the database
        gatewayParamRepository.saveAndFlush(gatewayParam);

        // Get all the gatewayParamList where body contains DEFAULT_BODY
        defaultGatewayParamShouldBeFound("body.contains=" + DEFAULT_BODY);

        // Get all the gatewayParamList where body contains UPDATED_BODY
        defaultGatewayParamShouldNotBeFound("body.contains=" + UPDATED_BODY);
    }

    @Test
    @Transactional
    void getAllGatewayParamsByBodyNotContainsSomething() throws Exception {
        // Initialize the database
        gatewayParamRepository.saveAndFlush(gatewayParam);

        // Get all the gatewayParamList where body does not contain DEFAULT_BODY
        defaultGatewayParamShouldNotBeFound("body.doesNotContain=" + DEFAULT_BODY);

        // Get all the gatewayParamList where body does not contain UPDATED_BODY
        defaultGatewayParamShouldBeFound("body.doesNotContain=" + UPDATED_BODY);
    }

    @Test
    @Transactional
    void getAllGatewayParamsByStatusQueryIsEqualToSomething() throws Exception {
        // Initialize the database
        gatewayParamRepository.saveAndFlush(gatewayParam);

        // Get all the gatewayParamList where statusQuery equals to DEFAULT_STATUS_QUERY
        defaultGatewayParamShouldBeFound("statusQuery.equals=" + DEFAULT_STATUS_QUERY);

        // Get all the gatewayParamList where statusQuery equals to UPDATED_STATUS_QUERY
        defaultGatewayParamShouldNotBeFound("statusQuery.equals=" + UPDATED_STATUS_QUERY);
    }

    @Test
    @Transactional
    void getAllGatewayParamsByStatusQueryIsNotEqualToSomething() throws Exception {
        // Initialize the database
        gatewayParamRepository.saveAndFlush(gatewayParam);

        // Get all the gatewayParamList where statusQuery not equals to DEFAULT_STATUS_QUERY
        defaultGatewayParamShouldNotBeFound("statusQuery.notEquals=" + DEFAULT_STATUS_QUERY);

        // Get all the gatewayParamList where statusQuery not equals to UPDATED_STATUS_QUERY
        defaultGatewayParamShouldBeFound("statusQuery.notEquals=" + UPDATED_STATUS_QUERY);
    }

    @Test
    @Transactional
    void getAllGatewayParamsByStatusQueryIsInShouldWork() throws Exception {
        // Initialize the database
        gatewayParamRepository.saveAndFlush(gatewayParam);

        // Get all the gatewayParamList where statusQuery in DEFAULT_STATUS_QUERY or UPDATED_STATUS_QUERY
        defaultGatewayParamShouldBeFound("statusQuery.in=" + DEFAULT_STATUS_QUERY + "," + UPDATED_STATUS_QUERY);

        // Get all the gatewayParamList where statusQuery equals to UPDATED_STATUS_QUERY
        defaultGatewayParamShouldNotBeFound("statusQuery.in=" + UPDATED_STATUS_QUERY);
    }

    @Test
    @Transactional
    void getAllGatewayParamsByStatusQueryIsNullOrNotNull() throws Exception {
        // Initialize the database
        gatewayParamRepository.saveAndFlush(gatewayParam);

        // Get all the gatewayParamList where statusQuery is not null
        defaultGatewayParamShouldBeFound("statusQuery.specified=true");

        // Get all the gatewayParamList where statusQuery is null
        defaultGatewayParamShouldNotBeFound("statusQuery.specified=false");
    }

    @Test
    @Transactional
    void getAllGatewayParamsByStatusQueryContainsSomething() throws Exception {
        // Initialize the database
        gatewayParamRepository.saveAndFlush(gatewayParam);

        // Get all the gatewayParamList where statusQuery contains DEFAULT_STATUS_QUERY
        defaultGatewayParamShouldBeFound("statusQuery.contains=" + DEFAULT_STATUS_QUERY);

        // Get all the gatewayParamList where statusQuery contains UPDATED_STATUS_QUERY
        defaultGatewayParamShouldNotBeFound("statusQuery.contains=" + UPDATED_STATUS_QUERY);
    }

    @Test
    @Transactional
    void getAllGatewayParamsByStatusQueryNotContainsSomething() throws Exception {
        // Initialize the database
        gatewayParamRepository.saveAndFlush(gatewayParam);

        // Get all the gatewayParamList where statusQuery does not contain DEFAULT_STATUS_QUERY
        defaultGatewayParamShouldNotBeFound("statusQuery.doesNotContain=" + DEFAULT_STATUS_QUERY);

        // Get all the gatewayParamList where statusQuery does not contain UPDATED_STATUS_QUERY
        defaultGatewayParamShouldBeFound("statusQuery.doesNotContain=" + UPDATED_STATUS_QUERY);
    }

    @Test
    @Transactional
    void getAllGatewayParamsByMessageQueryIsEqualToSomething() throws Exception {
        // Initialize the database
        gatewayParamRepository.saveAndFlush(gatewayParam);

        // Get all the gatewayParamList where messageQuery equals to DEFAULT_MESSAGE_QUERY
        defaultGatewayParamShouldBeFound("messageQuery.equals=" + DEFAULT_MESSAGE_QUERY);

        // Get all the gatewayParamList where messageQuery equals to UPDATED_MESSAGE_QUERY
        defaultGatewayParamShouldNotBeFound("messageQuery.equals=" + UPDATED_MESSAGE_QUERY);
    }

    @Test
    @Transactional
    void getAllGatewayParamsByMessageQueryIsNotEqualToSomething() throws Exception {
        // Initialize the database
        gatewayParamRepository.saveAndFlush(gatewayParam);

        // Get all the gatewayParamList where messageQuery not equals to DEFAULT_MESSAGE_QUERY
        defaultGatewayParamShouldNotBeFound("messageQuery.notEquals=" + DEFAULT_MESSAGE_QUERY);

        // Get all the gatewayParamList where messageQuery not equals to UPDATED_MESSAGE_QUERY
        defaultGatewayParamShouldBeFound("messageQuery.notEquals=" + UPDATED_MESSAGE_QUERY);
    }

    @Test
    @Transactional
    void getAllGatewayParamsByMessageQueryIsInShouldWork() throws Exception {
        // Initialize the database
        gatewayParamRepository.saveAndFlush(gatewayParam);

        // Get all the gatewayParamList where messageQuery in DEFAULT_MESSAGE_QUERY or UPDATED_MESSAGE_QUERY
        defaultGatewayParamShouldBeFound("messageQuery.in=" + DEFAULT_MESSAGE_QUERY + "," + UPDATED_MESSAGE_QUERY);

        // Get all the gatewayParamList where messageQuery equals to UPDATED_MESSAGE_QUERY
        defaultGatewayParamShouldNotBeFound("messageQuery.in=" + UPDATED_MESSAGE_QUERY);
    }

    @Test
    @Transactional
    void getAllGatewayParamsByMessageQueryIsNullOrNotNull() throws Exception {
        // Initialize the database
        gatewayParamRepository.saveAndFlush(gatewayParam);

        // Get all the gatewayParamList where messageQuery is not null
        defaultGatewayParamShouldBeFound("messageQuery.specified=true");

        // Get all the gatewayParamList where messageQuery is null
        defaultGatewayParamShouldNotBeFound("messageQuery.specified=false");
    }

    @Test
    @Transactional
    void getAllGatewayParamsByMessageQueryContainsSomething() throws Exception {
        // Initialize the database
        gatewayParamRepository.saveAndFlush(gatewayParam);

        // Get all the gatewayParamList where messageQuery contains DEFAULT_MESSAGE_QUERY
        defaultGatewayParamShouldBeFound("messageQuery.contains=" + DEFAULT_MESSAGE_QUERY);

        // Get all the gatewayParamList where messageQuery contains UPDATED_MESSAGE_QUERY
        defaultGatewayParamShouldNotBeFound("messageQuery.contains=" + UPDATED_MESSAGE_QUERY);
    }

    @Test
    @Transactional
    void getAllGatewayParamsByMessageQueryNotContainsSomething() throws Exception {
        // Initialize the database
        gatewayParamRepository.saveAndFlush(gatewayParam);

        // Get all the gatewayParamList where messageQuery does not contain DEFAULT_MESSAGE_QUERY
        defaultGatewayParamShouldNotBeFound("messageQuery.doesNotContain=" + DEFAULT_MESSAGE_QUERY);

        // Get all the gatewayParamList where messageQuery does not contain UPDATED_MESSAGE_QUERY
        defaultGatewayParamShouldBeFound("messageQuery.doesNotContain=" + UPDATED_MESSAGE_QUERY);
    }

    @Test
    @Transactional
    void getAllGatewayParamsBySuccessCodeIsEqualToSomething() throws Exception {
        // Initialize the database
        gatewayParamRepository.saveAndFlush(gatewayParam);

        // Get all the gatewayParamList where successCode equals to DEFAULT_SUCCESS_CODE
        defaultGatewayParamShouldBeFound("successCode.equals=" + DEFAULT_SUCCESS_CODE);

        // Get all the gatewayParamList where successCode equals to UPDATED_SUCCESS_CODE
        defaultGatewayParamShouldNotBeFound("successCode.equals=" + UPDATED_SUCCESS_CODE);
    }

    @Test
    @Transactional
    void getAllGatewayParamsBySuccessCodeIsNotEqualToSomething() throws Exception {
        // Initialize the database
        gatewayParamRepository.saveAndFlush(gatewayParam);

        // Get all the gatewayParamList where successCode not equals to DEFAULT_SUCCESS_CODE
        defaultGatewayParamShouldNotBeFound("successCode.notEquals=" + DEFAULT_SUCCESS_CODE);

        // Get all the gatewayParamList where successCode not equals to UPDATED_SUCCESS_CODE
        defaultGatewayParamShouldBeFound("successCode.notEquals=" + UPDATED_SUCCESS_CODE);
    }

    @Test
    @Transactional
    void getAllGatewayParamsBySuccessCodeIsInShouldWork() throws Exception {
        // Initialize the database
        gatewayParamRepository.saveAndFlush(gatewayParam);

        // Get all the gatewayParamList where successCode in DEFAULT_SUCCESS_CODE or UPDATED_SUCCESS_CODE
        defaultGatewayParamShouldBeFound("successCode.in=" + DEFAULT_SUCCESS_CODE + "," + UPDATED_SUCCESS_CODE);

        // Get all the gatewayParamList where successCode equals to UPDATED_SUCCESS_CODE
        defaultGatewayParamShouldNotBeFound("successCode.in=" + UPDATED_SUCCESS_CODE);
    }

    @Test
    @Transactional
    void getAllGatewayParamsBySuccessCodeIsNullOrNotNull() throws Exception {
        // Initialize the database
        gatewayParamRepository.saveAndFlush(gatewayParam);

        // Get all the gatewayParamList where successCode is not null
        defaultGatewayParamShouldBeFound("successCode.specified=true");

        // Get all the gatewayParamList where successCode is null
        defaultGatewayParamShouldNotBeFound("successCode.specified=false");
    }

    @Test
    @Transactional
    void getAllGatewayParamsBySuccessCodeContainsSomething() throws Exception {
        // Initialize the database
        gatewayParamRepository.saveAndFlush(gatewayParam);

        // Get all the gatewayParamList where successCode contains DEFAULT_SUCCESS_CODE
        defaultGatewayParamShouldBeFound("successCode.contains=" + DEFAULT_SUCCESS_CODE);

        // Get all the gatewayParamList where successCode contains UPDATED_SUCCESS_CODE
        defaultGatewayParamShouldNotBeFound("successCode.contains=" + UPDATED_SUCCESS_CODE);
    }

    @Test
    @Transactional
    void getAllGatewayParamsBySuccessCodeNotContainsSomething() throws Exception {
        // Initialize the database
        gatewayParamRepository.saveAndFlush(gatewayParam);

        // Get all the gatewayParamList where successCode does not contain DEFAULT_SUCCESS_CODE
        defaultGatewayParamShouldNotBeFound("successCode.doesNotContain=" + DEFAULT_SUCCESS_CODE);

        // Get all the gatewayParamList where successCode does not contain UPDATED_SUCCESS_CODE
        defaultGatewayParamShouldBeFound("successCode.doesNotContain=" + UPDATED_SUCCESS_CODE);
    }

    @Test
    @Transactional
    void getAllGatewayParamsByCallbackIsEqualToSomething() throws Exception {
        // Initialize the database
        gatewayParamRepository.saveAndFlush(gatewayParam);

        // Get all the gatewayParamList where callback equals to DEFAULT_CALLBACK
        defaultGatewayParamShouldBeFound("callback.equals=" + DEFAULT_CALLBACK);

        // Get all the gatewayParamList where callback equals to UPDATED_CALLBACK
        defaultGatewayParamShouldNotBeFound("callback.equals=" + UPDATED_CALLBACK);
    }

    @Test
    @Transactional
    void getAllGatewayParamsByCallbackIsNotEqualToSomething() throws Exception {
        // Initialize the database
        gatewayParamRepository.saveAndFlush(gatewayParam);

        // Get all the gatewayParamList where callback not equals to DEFAULT_CALLBACK
        defaultGatewayParamShouldNotBeFound("callback.notEquals=" + DEFAULT_CALLBACK);

        // Get all the gatewayParamList where callback not equals to UPDATED_CALLBACK
        defaultGatewayParamShouldBeFound("callback.notEquals=" + UPDATED_CALLBACK);
    }

    @Test
    @Transactional
    void getAllGatewayParamsByCallbackIsInShouldWork() throws Exception {
        // Initialize the database
        gatewayParamRepository.saveAndFlush(gatewayParam);

        // Get all the gatewayParamList where callback in DEFAULT_CALLBACK or UPDATED_CALLBACK
        defaultGatewayParamShouldBeFound("callback.in=" + DEFAULT_CALLBACK + "," + UPDATED_CALLBACK);

        // Get all the gatewayParamList where callback equals to UPDATED_CALLBACK
        defaultGatewayParamShouldNotBeFound("callback.in=" + UPDATED_CALLBACK);
    }

    @Test
    @Transactional
    void getAllGatewayParamsByCallbackIsNullOrNotNull() throws Exception {
        // Initialize the database
        gatewayParamRepository.saveAndFlush(gatewayParam);

        // Get all the gatewayParamList where callback is not null
        defaultGatewayParamShouldBeFound("callback.specified=true");

        // Get all the gatewayParamList where callback is null
        defaultGatewayParamShouldNotBeFound("callback.specified=false");
    }

    @Test
    @Transactional
    void getAllGatewayParamsByCallbackContainsSomething() throws Exception {
        // Initialize the database
        gatewayParamRepository.saveAndFlush(gatewayParam);

        // Get all the gatewayParamList where callback contains DEFAULT_CALLBACK
        defaultGatewayParamShouldBeFound("callback.contains=" + DEFAULT_CALLBACK);

        // Get all the gatewayParamList where callback contains UPDATED_CALLBACK
        defaultGatewayParamShouldNotBeFound("callback.contains=" + UPDATED_CALLBACK);
    }

    @Test
    @Transactional
    void getAllGatewayParamsByCallbackNotContainsSomething() throws Exception {
        // Initialize the database
        gatewayParamRepository.saveAndFlush(gatewayParam);

        // Get all the gatewayParamList where callback does not contain DEFAULT_CALLBACK
        defaultGatewayParamShouldNotBeFound("callback.doesNotContain=" + DEFAULT_CALLBACK);

        // Get all the gatewayParamList where callback does not contain UPDATED_CALLBACK
        defaultGatewayParamShouldBeFound("callback.doesNotContain=" + UPDATED_CALLBACK);
    }

    @Test
    @Transactional
    void getAllGatewayParamsByTimeoutIsEqualToSomething() throws Exception {
        // Initialize the database
        gatewayParamRepository.saveAndFlush(gatewayParam);

        // Get all the gatewayParamList where timeout equals to DEFAULT_TIMEOUT
        defaultGatewayParamShouldBeFound("timeout.equals=" + DEFAULT_TIMEOUT);

        // Get all the gatewayParamList where timeout equals to UPDATED_TIMEOUT
        defaultGatewayParamShouldNotBeFound("timeout.equals=" + UPDATED_TIMEOUT);
    }

    @Test
    @Transactional
    void getAllGatewayParamsByTimeoutIsNotEqualToSomething() throws Exception {
        // Initialize the database
        gatewayParamRepository.saveAndFlush(gatewayParam);

        // Get all the gatewayParamList where timeout not equals to DEFAULT_TIMEOUT
        defaultGatewayParamShouldNotBeFound("timeout.notEquals=" + DEFAULT_TIMEOUT);

        // Get all the gatewayParamList where timeout not equals to UPDATED_TIMEOUT
        defaultGatewayParamShouldBeFound("timeout.notEquals=" + UPDATED_TIMEOUT);
    }

    @Test
    @Transactional
    void getAllGatewayParamsByTimeoutIsInShouldWork() throws Exception {
        // Initialize the database
        gatewayParamRepository.saveAndFlush(gatewayParam);

        // Get all the gatewayParamList where timeout in DEFAULT_TIMEOUT or UPDATED_TIMEOUT
        defaultGatewayParamShouldBeFound("timeout.in=" + DEFAULT_TIMEOUT + "," + UPDATED_TIMEOUT);

        // Get all the gatewayParamList where timeout equals to UPDATED_TIMEOUT
        defaultGatewayParamShouldNotBeFound("timeout.in=" + UPDATED_TIMEOUT);
    }

    @Test
    @Transactional
    void getAllGatewayParamsByTimeoutIsNullOrNotNull() throws Exception {
        // Initialize the database
        gatewayParamRepository.saveAndFlush(gatewayParam);

        // Get all the gatewayParamList where timeout is not null
        defaultGatewayParamShouldBeFound("timeout.specified=true");

        // Get all the gatewayParamList where timeout is null
        defaultGatewayParamShouldNotBeFound("timeout.specified=false");
    }

    @Test
    @Transactional
    void getAllGatewayParamsByTimeoutIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        gatewayParamRepository.saveAndFlush(gatewayParam);

        // Get all the gatewayParamList where timeout is greater than or equal to DEFAULT_TIMEOUT
        defaultGatewayParamShouldBeFound("timeout.greaterThanOrEqual=" + DEFAULT_TIMEOUT);

        // Get all the gatewayParamList where timeout is greater than or equal to UPDATED_TIMEOUT
        defaultGatewayParamShouldNotBeFound("timeout.greaterThanOrEqual=" + UPDATED_TIMEOUT);
    }

    @Test
    @Transactional
    void getAllGatewayParamsByTimeoutIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        gatewayParamRepository.saveAndFlush(gatewayParam);

        // Get all the gatewayParamList where timeout is less than or equal to DEFAULT_TIMEOUT
        defaultGatewayParamShouldBeFound("timeout.lessThanOrEqual=" + DEFAULT_TIMEOUT);

        // Get all the gatewayParamList where timeout is less than or equal to SMALLER_TIMEOUT
        defaultGatewayParamShouldNotBeFound("timeout.lessThanOrEqual=" + SMALLER_TIMEOUT);
    }

    @Test
    @Transactional
    void getAllGatewayParamsByTimeoutIsLessThanSomething() throws Exception {
        // Initialize the database
        gatewayParamRepository.saveAndFlush(gatewayParam);

        // Get all the gatewayParamList where timeout is less than DEFAULT_TIMEOUT
        defaultGatewayParamShouldNotBeFound("timeout.lessThan=" + DEFAULT_TIMEOUT);

        // Get all the gatewayParamList where timeout is less than UPDATED_TIMEOUT
        defaultGatewayParamShouldBeFound("timeout.lessThan=" + UPDATED_TIMEOUT);
    }

    @Test
    @Transactional
    void getAllGatewayParamsByTimeoutIsGreaterThanSomething() throws Exception {
        // Initialize the database
        gatewayParamRepository.saveAndFlush(gatewayParam);

        // Get all the gatewayParamList where timeout is greater than DEFAULT_TIMEOUT
        defaultGatewayParamShouldNotBeFound("timeout.greaterThan=" + DEFAULT_TIMEOUT);

        // Get all the gatewayParamList where timeout is greater than SMALLER_TIMEOUT
        defaultGatewayParamShouldBeFound("timeout.greaterThan=" + SMALLER_TIMEOUT);
    }

    @Test
    @Transactional
    void getAllGatewayParamsByHeadersIsEqualToSomething() throws Exception {
        // Initialize the database
        gatewayParamRepository.saveAndFlush(gatewayParam);

        // Get all the gatewayParamList where headers equals to DEFAULT_HEADERS
        defaultGatewayParamShouldBeFound("headers.equals=" + DEFAULT_HEADERS);

        // Get all the gatewayParamList where headers equals to UPDATED_HEADERS
        defaultGatewayParamShouldNotBeFound("headers.equals=" + UPDATED_HEADERS);
    }

    @Test
    @Transactional
    void getAllGatewayParamsByHeadersIsNotEqualToSomething() throws Exception {
        // Initialize the database
        gatewayParamRepository.saveAndFlush(gatewayParam);

        // Get all the gatewayParamList where headers not equals to DEFAULT_HEADERS
        defaultGatewayParamShouldNotBeFound("headers.notEquals=" + DEFAULT_HEADERS);

        // Get all the gatewayParamList where headers not equals to UPDATED_HEADERS
        defaultGatewayParamShouldBeFound("headers.notEquals=" + UPDATED_HEADERS);
    }

    @Test
    @Transactional
    void getAllGatewayParamsByHeadersIsInShouldWork() throws Exception {
        // Initialize the database
        gatewayParamRepository.saveAndFlush(gatewayParam);

        // Get all the gatewayParamList where headers in DEFAULT_HEADERS or UPDATED_HEADERS
        defaultGatewayParamShouldBeFound("headers.in=" + DEFAULT_HEADERS + "," + UPDATED_HEADERS);

        // Get all the gatewayParamList where headers equals to UPDATED_HEADERS
        defaultGatewayParamShouldNotBeFound("headers.in=" + UPDATED_HEADERS);
    }

    @Test
    @Transactional
    void getAllGatewayParamsByHeadersIsNullOrNotNull() throws Exception {
        // Initialize the database
        gatewayParamRepository.saveAndFlush(gatewayParam);

        // Get all the gatewayParamList where headers is not null
        defaultGatewayParamShouldBeFound("headers.specified=true");

        // Get all the gatewayParamList where headers is null
        defaultGatewayParamShouldNotBeFound("headers.specified=false");
    }

    @Test
    @Transactional
    void getAllGatewayParamsByHeadersContainsSomething() throws Exception {
        // Initialize the database
        gatewayParamRepository.saveAndFlush(gatewayParam);

        // Get all the gatewayParamList where headers contains DEFAULT_HEADERS
        defaultGatewayParamShouldBeFound("headers.contains=" + DEFAULT_HEADERS);

        // Get all the gatewayParamList where headers contains UPDATED_HEADERS
        defaultGatewayParamShouldNotBeFound("headers.contains=" + UPDATED_HEADERS);
    }

    @Test
    @Transactional
    void getAllGatewayParamsByHeadersNotContainsSomething() throws Exception {
        // Initialize the database
        gatewayParamRepository.saveAndFlush(gatewayParam);

        // Get all the gatewayParamList where headers does not contain DEFAULT_HEADERS
        defaultGatewayParamShouldNotBeFound("headers.doesNotContain=" + DEFAULT_HEADERS);

        // Get all the gatewayParamList where headers does not contain UPDATED_HEADERS
        defaultGatewayParamShouldBeFound("headers.doesNotContain=" + UPDATED_HEADERS);
    }

    @Test
    @Transactional
    void getAllGatewayParamsByDataQueriesIsEqualToSomething() throws Exception {
        // Initialize the database
        gatewayParamRepository.saveAndFlush(gatewayParam);

        // Get all the gatewayParamList where dataQueries equals to DEFAULT_DATA_QUERIES
        defaultGatewayParamShouldBeFound("dataQueries.equals=" + DEFAULT_DATA_QUERIES);

        // Get all the gatewayParamList where dataQueries equals to UPDATED_DATA_QUERIES
        defaultGatewayParamShouldNotBeFound("dataQueries.equals=" + UPDATED_DATA_QUERIES);
    }

    @Test
    @Transactional
    void getAllGatewayParamsByDataQueriesIsNotEqualToSomething() throws Exception {
        // Initialize the database
        gatewayParamRepository.saveAndFlush(gatewayParam);

        // Get all the gatewayParamList where dataQueries not equals to DEFAULT_DATA_QUERIES
        defaultGatewayParamShouldNotBeFound("dataQueries.notEquals=" + DEFAULT_DATA_QUERIES);

        // Get all the gatewayParamList where dataQueries not equals to UPDATED_DATA_QUERIES
        defaultGatewayParamShouldBeFound("dataQueries.notEquals=" + UPDATED_DATA_QUERIES);
    }

    @Test
    @Transactional
    void getAllGatewayParamsByDataQueriesIsInShouldWork() throws Exception {
        // Initialize the database
        gatewayParamRepository.saveAndFlush(gatewayParam);

        // Get all the gatewayParamList where dataQueries in DEFAULT_DATA_QUERIES or UPDATED_DATA_QUERIES
        defaultGatewayParamShouldBeFound("dataQueries.in=" + DEFAULT_DATA_QUERIES + "," + UPDATED_DATA_QUERIES);

        // Get all the gatewayParamList where dataQueries equals to UPDATED_DATA_QUERIES
        defaultGatewayParamShouldNotBeFound("dataQueries.in=" + UPDATED_DATA_QUERIES);
    }

    @Test
    @Transactional
    void getAllGatewayParamsByDataQueriesIsNullOrNotNull() throws Exception {
        // Initialize the database
        gatewayParamRepository.saveAndFlush(gatewayParam);

        // Get all the gatewayParamList where dataQueries is not null
        defaultGatewayParamShouldBeFound("dataQueries.specified=true");

        // Get all the gatewayParamList where dataQueries is null
        defaultGatewayParamShouldNotBeFound("dataQueries.specified=false");
    }

    @Test
    @Transactional
    void getAllGatewayParamsByDataQueriesContainsSomething() throws Exception {
        // Initialize the database
        gatewayParamRepository.saveAndFlush(gatewayParam);

        // Get all the gatewayParamList where dataQueries contains DEFAULT_DATA_QUERIES
        defaultGatewayParamShouldBeFound("dataQueries.contains=" + DEFAULT_DATA_QUERIES);

        // Get all the gatewayParamList where dataQueries contains UPDATED_DATA_QUERIES
        defaultGatewayParamShouldNotBeFound("dataQueries.contains=" + UPDATED_DATA_QUERIES);
    }

    @Test
    @Transactional
    void getAllGatewayParamsByDataQueriesNotContainsSomething() throws Exception {
        // Initialize the database
        gatewayParamRepository.saveAndFlush(gatewayParam);

        // Get all the gatewayParamList where dataQueries does not contain DEFAULT_DATA_QUERIES
        defaultGatewayParamShouldNotBeFound("dataQueries.doesNotContain=" + DEFAULT_DATA_QUERIES);

        // Get all the gatewayParamList where dataQueries does not contain UPDATED_DATA_QUERIES
        defaultGatewayParamShouldBeFound("dataQueries.doesNotContain=" + UPDATED_DATA_QUERIES);
    }

    @Test
    @Transactional
    void getAllGatewayParamsByHttpMethodTypeIsEqualToSomething() throws Exception {
        // Initialize the database
        gatewayParamRepository.saveAndFlush(gatewayParam);

        // Get all the gatewayParamList where httpMethodType equals to DEFAULT_HTTP_METHOD_TYPE
        defaultGatewayParamShouldBeFound("httpMethodType.equals=" + DEFAULT_HTTP_METHOD_TYPE);

        // Get all the gatewayParamList where httpMethodType equals to UPDATED_HTTP_METHOD_TYPE
        defaultGatewayParamShouldNotBeFound("httpMethodType.equals=" + UPDATED_HTTP_METHOD_TYPE);
    }

    @Test
    @Transactional
    void getAllGatewayParamsByHttpMethodTypeIsNotEqualToSomething() throws Exception {
        // Initialize the database
        gatewayParamRepository.saveAndFlush(gatewayParam);

        // Get all the gatewayParamList where httpMethodType not equals to DEFAULT_HTTP_METHOD_TYPE
        defaultGatewayParamShouldNotBeFound("httpMethodType.notEquals=" + DEFAULT_HTTP_METHOD_TYPE);

        // Get all the gatewayParamList where httpMethodType not equals to UPDATED_HTTP_METHOD_TYPE
        defaultGatewayParamShouldBeFound("httpMethodType.notEquals=" + UPDATED_HTTP_METHOD_TYPE);
    }

    @Test
    @Transactional
    void getAllGatewayParamsByHttpMethodTypeIsInShouldWork() throws Exception {
        // Initialize the database
        gatewayParamRepository.saveAndFlush(gatewayParam);

        // Get all the gatewayParamList where httpMethodType in DEFAULT_HTTP_METHOD_TYPE or UPDATED_HTTP_METHOD_TYPE
        defaultGatewayParamShouldBeFound("httpMethodType.in=" + DEFAULT_HTTP_METHOD_TYPE + "," + UPDATED_HTTP_METHOD_TYPE);

        // Get all the gatewayParamList where httpMethodType equals to UPDATED_HTTP_METHOD_TYPE
        defaultGatewayParamShouldNotBeFound("httpMethodType.in=" + UPDATED_HTTP_METHOD_TYPE);
    }

    @Test
    @Transactional
    void getAllGatewayParamsByHttpMethodTypeIsNullOrNotNull() throws Exception {
        // Initialize the database
        gatewayParamRepository.saveAndFlush(gatewayParam);

        // Get all the gatewayParamList where httpMethodType is not null
        defaultGatewayParamShouldBeFound("httpMethodType.specified=true");

        // Get all the gatewayParamList where httpMethodType is null
        defaultGatewayParamShouldNotBeFound("httpMethodType.specified=false");
    }

    @Test
    @Transactional
    void getAllGatewayParamsByProtocolIsEqualToSomething() throws Exception {
        // Initialize the database
        gatewayParamRepository.saveAndFlush(gatewayParam);

        // Get all the gatewayParamList where protocol equals to DEFAULT_PROTOCOL
        defaultGatewayParamShouldBeFound("protocol.equals=" + DEFAULT_PROTOCOL);

        // Get all the gatewayParamList where protocol equals to UPDATED_PROTOCOL
        defaultGatewayParamShouldNotBeFound("protocol.equals=" + UPDATED_PROTOCOL);
    }

    @Test
    @Transactional
    void getAllGatewayParamsByProtocolIsNotEqualToSomething() throws Exception {
        // Initialize the database
        gatewayParamRepository.saveAndFlush(gatewayParam);

        // Get all the gatewayParamList where protocol not equals to DEFAULT_PROTOCOL
        defaultGatewayParamShouldNotBeFound("protocol.notEquals=" + DEFAULT_PROTOCOL);

        // Get all the gatewayParamList where protocol not equals to UPDATED_PROTOCOL
        defaultGatewayParamShouldBeFound("protocol.notEquals=" + UPDATED_PROTOCOL);
    }

    @Test
    @Transactional
    void getAllGatewayParamsByProtocolIsInShouldWork() throws Exception {
        // Initialize the database
        gatewayParamRepository.saveAndFlush(gatewayParam);

        // Get all the gatewayParamList where protocol in DEFAULT_PROTOCOL or UPDATED_PROTOCOL
        defaultGatewayParamShouldBeFound("protocol.in=" + DEFAULT_PROTOCOL + "," + UPDATED_PROTOCOL);

        // Get all the gatewayParamList where protocol equals to UPDATED_PROTOCOL
        defaultGatewayParamShouldNotBeFound("protocol.in=" + UPDATED_PROTOCOL);
    }

    @Test
    @Transactional
    void getAllGatewayParamsByProtocolIsNullOrNotNull() throws Exception {
        // Initialize the database
        gatewayParamRepository.saveAndFlush(gatewayParam);

        // Get all the gatewayParamList where protocol is not null
        defaultGatewayParamShouldBeFound("protocol.specified=true");

        // Get all the gatewayParamList where protocol is null
        defaultGatewayParamShouldNotBeFound("protocol.specified=false");
    }

    @Test
    @Transactional
    void getAllGatewayParamsByRequestMediaTypeIsEqualToSomething() throws Exception {
        // Initialize the database
        gatewayParamRepository.saveAndFlush(gatewayParam);

        // Get all the gatewayParamList where requestMediaType equals to DEFAULT_REQUEST_MEDIA_TYPE
        defaultGatewayParamShouldBeFound("requestMediaType.equals=" + DEFAULT_REQUEST_MEDIA_TYPE);

        // Get all the gatewayParamList where requestMediaType equals to UPDATED_REQUEST_MEDIA_TYPE
        defaultGatewayParamShouldNotBeFound("requestMediaType.equals=" + UPDATED_REQUEST_MEDIA_TYPE);
    }

    @Test
    @Transactional
    void getAllGatewayParamsByRequestMediaTypeIsNotEqualToSomething() throws Exception {
        // Initialize the database
        gatewayParamRepository.saveAndFlush(gatewayParam);

        // Get all the gatewayParamList where requestMediaType not equals to DEFAULT_REQUEST_MEDIA_TYPE
        defaultGatewayParamShouldNotBeFound("requestMediaType.notEquals=" + DEFAULT_REQUEST_MEDIA_TYPE);

        // Get all the gatewayParamList where requestMediaType not equals to UPDATED_REQUEST_MEDIA_TYPE
        defaultGatewayParamShouldBeFound("requestMediaType.notEquals=" + UPDATED_REQUEST_MEDIA_TYPE);
    }

    @Test
    @Transactional
    void getAllGatewayParamsByRequestMediaTypeIsInShouldWork() throws Exception {
        // Initialize the database
        gatewayParamRepository.saveAndFlush(gatewayParam);

        // Get all the gatewayParamList where requestMediaType in DEFAULT_REQUEST_MEDIA_TYPE or UPDATED_REQUEST_MEDIA_TYPE
        defaultGatewayParamShouldBeFound("requestMediaType.in=" + DEFAULT_REQUEST_MEDIA_TYPE + "," + UPDATED_REQUEST_MEDIA_TYPE);

        // Get all the gatewayParamList where requestMediaType equals to UPDATED_REQUEST_MEDIA_TYPE
        defaultGatewayParamShouldNotBeFound("requestMediaType.in=" + UPDATED_REQUEST_MEDIA_TYPE);
    }

    @Test
    @Transactional
    void getAllGatewayParamsByRequestMediaTypeIsNullOrNotNull() throws Exception {
        // Initialize the database
        gatewayParamRepository.saveAndFlush(gatewayParam);

        // Get all the gatewayParamList where requestMediaType is not null
        defaultGatewayParamShouldBeFound("requestMediaType.specified=true");

        // Get all the gatewayParamList where requestMediaType is null
        defaultGatewayParamShouldNotBeFound("requestMediaType.specified=false");
    }

    @Test
    @Transactional
    void getAllGatewayParamsByRequestMediaTypeContainsSomething() throws Exception {
        // Initialize the database
        gatewayParamRepository.saveAndFlush(gatewayParam);

        // Get all the gatewayParamList where requestMediaType contains DEFAULT_REQUEST_MEDIA_TYPE
        defaultGatewayParamShouldBeFound("requestMediaType.contains=" + DEFAULT_REQUEST_MEDIA_TYPE);

        // Get all the gatewayParamList where requestMediaType contains UPDATED_REQUEST_MEDIA_TYPE
        defaultGatewayParamShouldNotBeFound("requestMediaType.contains=" + UPDATED_REQUEST_MEDIA_TYPE);
    }

    @Test
    @Transactional
    void getAllGatewayParamsByRequestMediaTypeNotContainsSomething() throws Exception {
        // Initialize the database
        gatewayParamRepository.saveAndFlush(gatewayParam);

        // Get all the gatewayParamList where requestMediaType does not contain DEFAULT_REQUEST_MEDIA_TYPE
        defaultGatewayParamShouldNotBeFound("requestMediaType.doesNotContain=" + DEFAULT_REQUEST_MEDIA_TYPE);

        // Get all the gatewayParamList where requestMediaType does not contain UPDATED_REQUEST_MEDIA_TYPE
        defaultGatewayParamShouldBeFound("requestMediaType.doesNotContain=" + UPDATED_REQUEST_MEDIA_TYPE);
    }

    @Test
    @Transactional
    void getAllGatewayParamsByResponseMediaTypeIsEqualToSomething() throws Exception {
        // Initialize the database
        gatewayParamRepository.saveAndFlush(gatewayParam);

        // Get all the gatewayParamList where responseMediaType equals to DEFAULT_RESPONSE_MEDIA_TYPE
        defaultGatewayParamShouldBeFound("responseMediaType.equals=" + DEFAULT_RESPONSE_MEDIA_TYPE);

        // Get all the gatewayParamList where responseMediaType equals to UPDATED_RESPONSE_MEDIA_TYPE
        defaultGatewayParamShouldNotBeFound("responseMediaType.equals=" + UPDATED_RESPONSE_MEDIA_TYPE);
    }

    @Test
    @Transactional
    void getAllGatewayParamsByResponseMediaTypeIsNotEqualToSomething() throws Exception {
        // Initialize the database
        gatewayParamRepository.saveAndFlush(gatewayParam);

        // Get all the gatewayParamList where responseMediaType not equals to DEFAULT_RESPONSE_MEDIA_TYPE
        defaultGatewayParamShouldNotBeFound("responseMediaType.notEquals=" + DEFAULT_RESPONSE_MEDIA_TYPE);

        // Get all the gatewayParamList where responseMediaType not equals to UPDATED_RESPONSE_MEDIA_TYPE
        defaultGatewayParamShouldBeFound("responseMediaType.notEquals=" + UPDATED_RESPONSE_MEDIA_TYPE);
    }

    @Test
    @Transactional
    void getAllGatewayParamsByResponseMediaTypeIsInShouldWork() throws Exception {
        // Initialize the database
        gatewayParamRepository.saveAndFlush(gatewayParam);

        // Get all the gatewayParamList where responseMediaType in DEFAULT_RESPONSE_MEDIA_TYPE or UPDATED_RESPONSE_MEDIA_TYPE
        defaultGatewayParamShouldBeFound("responseMediaType.in=" + DEFAULT_RESPONSE_MEDIA_TYPE + "," + UPDATED_RESPONSE_MEDIA_TYPE);

        // Get all the gatewayParamList where responseMediaType equals to UPDATED_RESPONSE_MEDIA_TYPE
        defaultGatewayParamShouldNotBeFound("responseMediaType.in=" + UPDATED_RESPONSE_MEDIA_TYPE);
    }

    @Test
    @Transactional
    void getAllGatewayParamsByResponseMediaTypeIsNullOrNotNull() throws Exception {
        // Initialize the database
        gatewayParamRepository.saveAndFlush(gatewayParam);

        // Get all the gatewayParamList where responseMediaType is not null
        defaultGatewayParamShouldBeFound("responseMediaType.specified=true");

        // Get all the gatewayParamList where responseMediaType is null
        defaultGatewayParamShouldNotBeFound("responseMediaType.specified=false");
    }

    @Test
    @Transactional
    void getAllGatewayParamsByResponseMediaTypeContainsSomething() throws Exception {
        // Initialize the database
        gatewayParamRepository.saveAndFlush(gatewayParam);

        // Get all the gatewayParamList where responseMediaType contains DEFAULT_RESPONSE_MEDIA_TYPE
        defaultGatewayParamShouldBeFound("responseMediaType.contains=" + DEFAULT_RESPONSE_MEDIA_TYPE);

        // Get all the gatewayParamList where responseMediaType contains UPDATED_RESPONSE_MEDIA_TYPE
        defaultGatewayParamShouldNotBeFound("responseMediaType.contains=" + UPDATED_RESPONSE_MEDIA_TYPE);
    }

    @Test
    @Transactional
    void getAllGatewayParamsByResponseMediaTypeNotContainsSomething() throws Exception {
        // Initialize the database
        gatewayParamRepository.saveAndFlush(gatewayParam);

        // Get all the gatewayParamList where responseMediaType does not contain DEFAULT_RESPONSE_MEDIA_TYPE
        defaultGatewayParamShouldNotBeFound("responseMediaType.doesNotContain=" + DEFAULT_RESPONSE_MEDIA_TYPE);

        // Get all the gatewayParamList where responseMediaType does not contain UPDATED_RESPONSE_MEDIA_TYPE
        defaultGatewayParamShouldBeFound("responseMediaType.doesNotContain=" + UPDATED_RESPONSE_MEDIA_TYPE);
    }

    @Test
    @Transactional
    void getAllGatewayParamsByActiveIsEqualToSomething() throws Exception {
        // Initialize the database
        gatewayParamRepository.saveAndFlush(gatewayParam);

        // Get all the gatewayParamList where active equals to DEFAULT_ACTIVE
        defaultGatewayParamShouldBeFound("active.equals=" + DEFAULT_ACTIVE);

        // Get all the gatewayParamList where active equals to UPDATED_ACTIVE
        defaultGatewayParamShouldNotBeFound("active.equals=" + UPDATED_ACTIVE);
    }

    @Test
    @Transactional
    void getAllGatewayParamsByActiveIsNotEqualToSomething() throws Exception {
        // Initialize the database
        gatewayParamRepository.saveAndFlush(gatewayParam);

        // Get all the gatewayParamList where active not equals to DEFAULT_ACTIVE
        defaultGatewayParamShouldNotBeFound("active.notEquals=" + DEFAULT_ACTIVE);

        // Get all the gatewayParamList where active not equals to UPDATED_ACTIVE
        defaultGatewayParamShouldBeFound("active.notEquals=" + UPDATED_ACTIVE);
    }

    @Test
    @Transactional
    void getAllGatewayParamsByActiveIsInShouldWork() throws Exception {
        // Initialize the database
        gatewayParamRepository.saveAndFlush(gatewayParam);

        // Get all the gatewayParamList where active in DEFAULT_ACTIVE or UPDATED_ACTIVE
        defaultGatewayParamShouldBeFound("active.in=" + DEFAULT_ACTIVE + "," + UPDATED_ACTIVE);

        // Get all the gatewayParamList where active equals to UPDATED_ACTIVE
        defaultGatewayParamShouldNotBeFound("active.in=" + UPDATED_ACTIVE);
    }

    @Test
    @Transactional
    void getAllGatewayParamsByActiveIsNullOrNotNull() throws Exception {
        // Initialize the database
        gatewayParamRepository.saveAndFlush(gatewayParam);

        // Get all the gatewayParamList where active is not null
        defaultGatewayParamShouldBeFound("active.specified=true");

        // Get all the gatewayParamList where active is null
        defaultGatewayParamShouldNotBeFound("active.specified=false");
    }

    @Test
    @Transactional
    void getAllGatewayParamsByModeIsEqualToSomething() throws Exception {
        // Initialize the database
        gatewayParamRepository.saveAndFlush(gatewayParam);

        // Get all the gatewayParamList where mode equals to DEFAULT_MODE
        defaultGatewayParamShouldBeFound("mode.equals=" + DEFAULT_MODE);

        // Get all the gatewayParamList where mode equals to UPDATED_MODE
        defaultGatewayParamShouldNotBeFound("mode.equals=" + UPDATED_MODE);
    }

    @Test
    @Transactional
    void getAllGatewayParamsByModeIsNotEqualToSomething() throws Exception {
        // Initialize the database
        gatewayParamRepository.saveAndFlush(gatewayParam);

        // Get all the gatewayParamList where mode not equals to DEFAULT_MODE
        defaultGatewayParamShouldNotBeFound("mode.notEquals=" + DEFAULT_MODE);

        // Get all the gatewayParamList where mode not equals to UPDATED_MODE
        defaultGatewayParamShouldBeFound("mode.notEquals=" + UPDATED_MODE);
    }

    @Test
    @Transactional
    void getAllGatewayParamsByModeIsInShouldWork() throws Exception {
        // Initialize the database
        gatewayParamRepository.saveAndFlush(gatewayParam);

        // Get all the gatewayParamList where mode in DEFAULT_MODE or UPDATED_MODE
        defaultGatewayParamShouldBeFound("mode.in=" + DEFAULT_MODE + "," + UPDATED_MODE);

        // Get all the gatewayParamList where mode equals to UPDATED_MODE
        defaultGatewayParamShouldNotBeFound("mode.in=" + UPDATED_MODE);
    }

    @Test
    @Transactional
    void getAllGatewayParamsByModeIsNullOrNotNull() throws Exception {
        // Initialize the database
        gatewayParamRepository.saveAndFlush(gatewayParam);

        // Get all the gatewayParamList where mode is not null
        defaultGatewayParamShouldBeFound("mode.specified=true");

        // Get all the gatewayParamList where mode is null
        defaultGatewayParamShouldNotBeFound("mode.specified=false");
    }

    @Test
    @Transactional
    void getAllGatewayParamsByExtIdQueryIsEqualToSomething() throws Exception {
        // Initialize the database
        gatewayParamRepository.saveAndFlush(gatewayParam);

        // Get all the gatewayParamList where extIdQuery equals to DEFAULT_EXT_ID_QUERY
        defaultGatewayParamShouldBeFound("extIdQuery.equals=" + DEFAULT_EXT_ID_QUERY);

        // Get all the gatewayParamList where extIdQuery equals to UPDATED_EXT_ID_QUERY
        defaultGatewayParamShouldNotBeFound("extIdQuery.equals=" + UPDATED_EXT_ID_QUERY);
    }

    @Test
    @Transactional
    void getAllGatewayParamsByExtIdQueryIsNotEqualToSomething() throws Exception {
        // Initialize the database
        gatewayParamRepository.saveAndFlush(gatewayParam);

        // Get all the gatewayParamList where extIdQuery not equals to DEFAULT_EXT_ID_QUERY
        defaultGatewayParamShouldNotBeFound("extIdQuery.notEquals=" + DEFAULT_EXT_ID_QUERY);

        // Get all the gatewayParamList where extIdQuery not equals to UPDATED_EXT_ID_QUERY
        defaultGatewayParamShouldBeFound("extIdQuery.notEquals=" + UPDATED_EXT_ID_QUERY);
    }

    @Test
    @Transactional
    void getAllGatewayParamsByExtIdQueryIsInShouldWork() throws Exception {
        // Initialize the database
        gatewayParamRepository.saveAndFlush(gatewayParam);

        // Get all the gatewayParamList where extIdQuery in DEFAULT_EXT_ID_QUERY or UPDATED_EXT_ID_QUERY
        defaultGatewayParamShouldBeFound("extIdQuery.in=" + DEFAULT_EXT_ID_QUERY + "," + UPDATED_EXT_ID_QUERY);

        // Get all the gatewayParamList where extIdQuery equals to UPDATED_EXT_ID_QUERY
        defaultGatewayParamShouldNotBeFound("extIdQuery.in=" + UPDATED_EXT_ID_QUERY);
    }

    @Test
    @Transactional
    void getAllGatewayParamsByExtIdQueryIsNullOrNotNull() throws Exception {
        // Initialize the database
        gatewayParamRepository.saveAndFlush(gatewayParam);

        // Get all the gatewayParamList where extIdQuery is not null
        defaultGatewayParamShouldBeFound("extIdQuery.specified=true");

        // Get all the gatewayParamList where extIdQuery is null
        defaultGatewayParamShouldNotBeFound("extIdQuery.specified=false");
    }

    @Test
    @Transactional
    void getAllGatewayParamsByExtIdQueryContainsSomething() throws Exception {
        // Initialize the database
        gatewayParamRepository.saveAndFlush(gatewayParam);

        // Get all the gatewayParamList where extIdQuery contains DEFAULT_EXT_ID_QUERY
        defaultGatewayParamShouldBeFound("extIdQuery.contains=" + DEFAULT_EXT_ID_QUERY);

        // Get all the gatewayParamList where extIdQuery contains UPDATED_EXT_ID_QUERY
        defaultGatewayParamShouldNotBeFound("extIdQuery.contains=" + UPDATED_EXT_ID_QUERY);
    }

    @Test
    @Transactional
    void getAllGatewayParamsByExtIdQueryNotContainsSomething() throws Exception {
        // Initialize the database
        gatewayParamRepository.saveAndFlush(gatewayParam);

        // Get all the gatewayParamList where extIdQuery does not contain DEFAULT_EXT_ID_QUERY
        defaultGatewayParamShouldNotBeFound("extIdQuery.doesNotContain=" + DEFAULT_EXT_ID_QUERY);

        // Get all the gatewayParamList where extIdQuery does not contain UPDATED_EXT_ID_QUERY
        defaultGatewayParamShouldBeFound("extIdQuery.doesNotContain=" + UPDATED_EXT_ID_QUERY);
    }

    @Test
    @Transactional
    void getAllGatewayParamsBySuccessOn2xxIsEqualToSomething() throws Exception {
        // Initialize the database
        gatewayParamRepository.saveAndFlush(gatewayParam);

        // Get all the gatewayParamList where successOn2xx equals to DEFAULT_SUCCESS_ON_2_XX
        defaultGatewayParamShouldBeFound("successOn2xx.equals=" + DEFAULT_SUCCESS_ON_2_XX);

        // Get all the gatewayParamList where successOn2xx equals to UPDATED_SUCCESS_ON_2_XX
        defaultGatewayParamShouldNotBeFound("successOn2xx.equals=" + UPDATED_SUCCESS_ON_2_XX);
    }

    @Test
    @Transactional
    void getAllGatewayParamsBySuccessOn2xxIsNotEqualToSomething() throws Exception {
        // Initialize the database
        gatewayParamRepository.saveAndFlush(gatewayParam);

        // Get all the gatewayParamList where successOn2xx not equals to DEFAULT_SUCCESS_ON_2_XX
        defaultGatewayParamShouldNotBeFound("successOn2xx.notEquals=" + DEFAULT_SUCCESS_ON_2_XX);

        // Get all the gatewayParamList where successOn2xx not equals to UPDATED_SUCCESS_ON_2_XX
        defaultGatewayParamShouldBeFound("successOn2xx.notEquals=" + UPDATED_SUCCESS_ON_2_XX);
    }

    @Test
    @Transactional
    void getAllGatewayParamsBySuccessOn2xxIsInShouldWork() throws Exception {
        // Initialize the database
        gatewayParamRepository.saveAndFlush(gatewayParam);

        // Get all the gatewayParamList where successOn2xx in DEFAULT_SUCCESS_ON_2_XX or UPDATED_SUCCESS_ON_2_XX
        defaultGatewayParamShouldBeFound("successOn2xx.in=" + DEFAULT_SUCCESS_ON_2_XX + "," + UPDATED_SUCCESS_ON_2_XX);

        // Get all the gatewayParamList where successOn2xx equals to UPDATED_SUCCESS_ON_2_XX
        defaultGatewayParamShouldNotBeFound("successOn2xx.in=" + UPDATED_SUCCESS_ON_2_XX);
    }

    @Test
    @Transactional
    void getAllGatewayParamsBySuccessOn2xxIsNullOrNotNull() throws Exception {
        // Initialize the database
        gatewayParamRepository.saveAndFlush(gatewayParam);

        // Get all the gatewayParamList where successOn2xx is not null
        defaultGatewayParamShouldBeFound("successOn2xx.specified=true");

        // Get all the gatewayParamList where successOn2xx is null
        defaultGatewayParamShouldNotBeFound("successOn2xx.specified=false");
    }

    @Test
    @Transactional
    void getAllGatewayParamsByRecipientIsEqualToSomething() throws Exception {
        // Initialize the database
        gatewayParamRepository.saveAndFlush(gatewayParam);
        Recipient recipient = RecipientResourceIT.createEntity(em);
        em.persist(recipient);
        em.flush();
        gatewayParam.setRecipient(recipient);
        gatewayParamRepository.saveAndFlush(gatewayParam);
        Long recipientId = recipient.getId();

        // Get all the gatewayParamList where recipient equals to recipientId
        defaultGatewayParamShouldBeFound("recipientId.equals=" + recipientId);

        // Get all the gatewayParamList where recipient equals to (recipientId + 1)
        defaultGatewayParamShouldNotBeFound("recipientId.equals=" + (recipientId + 1));
    }

    /**
     * Executes the search, and checks that the default entity is returned.
     */
    private void defaultGatewayParamShouldBeFound(String filter) throws Exception {
        restGatewayParamMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(gatewayParam.getId().intValue())))
            .andExpect(jsonPath("$.[*].url").value(hasItem(DEFAULT_URL)))
            .andExpect(jsonPath("$.[*].type").value(hasItem(DEFAULT_TYPE.toString())))
            .andExpect(jsonPath("$.[*].username").value(hasItem(DEFAULT_USERNAME)))
            .andExpect(jsonPath("$.[*].password").value(hasItem(DEFAULT_PASSWORD)))
            .andExpect(jsonPath("$.[*].token").value(hasItem(DEFAULT_TOKEN)))
            .andExpect(jsonPath("$.[*].body").value(hasItem(DEFAULT_BODY)))
            .andExpect(jsonPath("$.[*].statusQuery").value(hasItem(DEFAULT_STATUS_QUERY)))
            .andExpect(jsonPath("$.[*].messageQuery").value(hasItem(DEFAULT_MESSAGE_QUERY)))
            .andExpect(jsonPath("$.[*].successCode").value(hasItem(DEFAULT_SUCCESS_CODE)))
            .andExpect(jsonPath("$.[*].callback").value(hasItem(DEFAULT_CALLBACK)))
            .andExpect(jsonPath("$.[*].timeout").value(hasItem(DEFAULT_TIMEOUT)))
            .andExpect(jsonPath("$.[*].headers").value(hasItem(DEFAULT_HEADERS)))
            .andExpect(jsonPath("$.[*].dataQueries").value(hasItem(DEFAULT_DATA_QUERIES)))
            .andExpect(jsonPath("$.[*].httpMethodType").value(hasItem(DEFAULT_HTTP_METHOD_TYPE.toString())))
            .andExpect(jsonPath("$.[*].protocol").value(hasItem(DEFAULT_PROTOCOL.toString())))
            .andExpect(jsonPath("$.[*].requestMediaType").value(hasItem(DEFAULT_REQUEST_MEDIA_TYPE)))
            .andExpect(jsonPath("$.[*].responseMediaType").value(hasItem(DEFAULT_RESPONSE_MEDIA_TYPE)))
            .andExpect(jsonPath("$.[*].active").value(hasItem(DEFAULT_ACTIVE.booleanValue())))
            .andExpect(jsonPath("$.[*].mode").value(hasItem(DEFAULT_MODE.toString())))
            .andExpect(jsonPath("$.[*].extIdQuery").value(hasItem(DEFAULT_EXT_ID_QUERY)))
            .andExpect(jsonPath("$.[*].successOn2xx").value(hasItem(DEFAULT_SUCCESS_ON_2_XX.booleanValue())));

        // Check, that the count call also returns 1
        restGatewayParamMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("1"));
    }

    /**
     * Executes the search, and checks that the default entity is not returned.
     */
    private void defaultGatewayParamShouldNotBeFound(String filter) throws Exception {
        restGatewayParamMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty());

        // Check, that the count call also returns 0
        restGatewayParamMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("0"));
    }

    @Test
    @Transactional
    void getNonExistingGatewayParam() throws Exception {
        // Get the gatewayParam
        restGatewayParamMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putNewGatewayParam() throws Exception {
        // Initialize the database
        gatewayParamRepository.saveAndFlush(gatewayParam);

        int databaseSizeBeforeUpdate = gatewayParamRepository.findAll().size();

        // Update the gatewayParam
        GatewayParam updatedGatewayParam = gatewayParamRepository.findById(gatewayParam.getId()).get();
        // Disconnect from session so that the updates on updatedGatewayParam are not directly saved in db
        em.detach(updatedGatewayParam);
        updatedGatewayParam
            .url(UPDATED_URL)
            .type(UPDATED_TYPE)
            .username(UPDATED_USERNAME)
            .password(UPDATED_PASSWORD)
            .token(UPDATED_TOKEN)
            .body(UPDATED_BODY)
            .statusQuery(UPDATED_STATUS_QUERY)
            .messageQuery(UPDATED_MESSAGE_QUERY)
            .successCode(UPDATED_SUCCESS_CODE)
            .callback(UPDATED_CALLBACK)
            .timeout(UPDATED_TIMEOUT)
            .headers(UPDATED_HEADERS)
            .dataQueries(UPDATED_DATA_QUERIES)
            .httpMethodType(UPDATED_HTTP_METHOD_TYPE)
            .protocol(UPDATED_PROTOCOL)
            .requestMediaType(UPDATED_REQUEST_MEDIA_TYPE)
            .responseMediaType(UPDATED_RESPONSE_MEDIA_TYPE)
            .active(UPDATED_ACTIVE)
            .mode(UPDATED_MODE)
            .extIdQuery(UPDATED_EXT_ID_QUERY)
            .successOn2xx(UPDATED_SUCCESS_ON_2_XX);
        GatewayParamDTO gatewayParamDTO = gatewayParamMapper.toDto(updatedGatewayParam);

        restGatewayParamMockMvc
            .perform(
                put(ENTITY_API_URL_ID, gatewayParamDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(gatewayParamDTO))
            )
            .andExpect(status().isOk());

        // Validate the GatewayParam in the database
        List<GatewayParam> gatewayParamList = gatewayParamRepository.findAll();
        assertThat(gatewayParamList).hasSize(databaseSizeBeforeUpdate);
        GatewayParam testGatewayParam = gatewayParamList.get(gatewayParamList.size() - 1);
        assertThat(testGatewayParam.getUrl()).isEqualTo(UPDATED_URL);
        assertThat(testGatewayParam.getType()).isEqualTo(UPDATED_TYPE);
        assertThat(testGatewayParam.getUsername()).isEqualTo(UPDATED_USERNAME);
        assertThat(testGatewayParam.getPassword()).isEqualTo(UPDATED_PASSWORD);
        assertThat(testGatewayParam.getToken()).isEqualTo(UPDATED_TOKEN);
        assertThat(testGatewayParam.getBody()).isEqualTo(UPDATED_BODY);
        assertThat(testGatewayParam.getStatusQuery()).isEqualTo(UPDATED_STATUS_QUERY);
        assertThat(testGatewayParam.getMessageQuery()).isEqualTo(UPDATED_MESSAGE_QUERY);
        assertThat(testGatewayParam.getSuccessCode()).isEqualTo(UPDATED_SUCCESS_CODE);
        assertThat(testGatewayParam.getCallback()).isEqualTo(UPDATED_CALLBACK);
        assertThat(testGatewayParam.getTimeout()).isEqualTo(UPDATED_TIMEOUT);
        assertThat(testGatewayParam.getHeaders()).isEqualTo(UPDATED_HEADERS);
        assertThat(testGatewayParam.getDataQueries()).isEqualTo(UPDATED_DATA_QUERIES);
        assertThat(testGatewayParam.getHttpMethodType()).isEqualTo(UPDATED_HTTP_METHOD_TYPE);
        assertThat(testGatewayParam.getProtocol()).isEqualTo(UPDATED_PROTOCOL);
        assertThat(testGatewayParam.getRequestMediaType()).isEqualTo(UPDATED_REQUEST_MEDIA_TYPE);
        assertThat(testGatewayParam.getResponseMediaType()).isEqualTo(UPDATED_RESPONSE_MEDIA_TYPE);
        assertThat(testGatewayParam.getActive()).isEqualTo(UPDATED_ACTIVE);
        assertThat(testGatewayParam.getMode()).isEqualTo(UPDATED_MODE);
        assertThat(testGatewayParam.getExtIdQuery()).isEqualTo(UPDATED_EXT_ID_QUERY);
        assertThat(testGatewayParam.getSuccessOn2xx()).isEqualTo(UPDATED_SUCCESS_ON_2_XX);
    }

    @Test
    @Transactional
    void putNonExistingGatewayParam() throws Exception {
        int databaseSizeBeforeUpdate = gatewayParamRepository.findAll().size();
        gatewayParam.setId(count.incrementAndGet());

        // Create the GatewayParam
        GatewayParamDTO gatewayParamDTO = gatewayParamMapper.toDto(gatewayParam);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restGatewayParamMockMvc
            .perform(
                put(ENTITY_API_URL_ID, gatewayParamDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(gatewayParamDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the GatewayParam in the database
        List<GatewayParam> gatewayParamList = gatewayParamRepository.findAll();
        assertThat(gatewayParamList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchGatewayParam() throws Exception {
        int databaseSizeBeforeUpdate = gatewayParamRepository.findAll().size();
        gatewayParam.setId(count.incrementAndGet());

        // Create the GatewayParam
        GatewayParamDTO gatewayParamDTO = gatewayParamMapper.toDto(gatewayParam);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restGatewayParamMockMvc
            .perform(
                put(ENTITY_API_URL_ID, count.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(gatewayParamDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the GatewayParam in the database
        List<GatewayParam> gatewayParamList = gatewayParamRepository.findAll();
        assertThat(gatewayParamList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamGatewayParam() throws Exception {
        int databaseSizeBeforeUpdate = gatewayParamRepository.findAll().size();
        gatewayParam.setId(count.incrementAndGet());

        // Create the GatewayParam
        GatewayParamDTO gatewayParamDTO = gatewayParamMapper.toDto(gatewayParam);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restGatewayParamMockMvc
            .perform(
                put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(gatewayParamDTO))
            )
            .andExpect(status().isMethodNotAllowed());

        // Validate the GatewayParam in the database
        List<GatewayParam> gatewayParamList = gatewayParamRepository.findAll();
        assertThat(gatewayParamList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateGatewayParamWithPatch() throws Exception {
        // Initialize the database
        gatewayParamRepository.saveAndFlush(gatewayParam);

        int databaseSizeBeforeUpdate = gatewayParamRepository.findAll().size();

        // Update the gatewayParam using partial update
        GatewayParam partialUpdatedGatewayParam = new GatewayParam();
        partialUpdatedGatewayParam.setId(gatewayParam.getId());

        partialUpdatedGatewayParam
            .url(UPDATED_URL)
            .username(UPDATED_USERNAME)
            .password(UPDATED_PASSWORD)
            .statusQuery(UPDATED_STATUS_QUERY)
            .messageQuery(UPDATED_MESSAGE_QUERY)
            .headers(UPDATED_HEADERS)
            .httpMethodType(UPDATED_HTTP_METHOD_TYPE)
            .protocol(UPDATED_PROTOCOL)
            .requestMediaType(UPDATED_REQUEST_MEDIA_TYPE)
            .extIdQuery(UPDATED_EXT_ID_QUERY);

        restGatewayParamMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedGatewayParam.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedGatewayParam))
            )
            .andExpect(status().isOk());

        // Validate the GatewayParam in the database
        List<GatewayParam> gatewayParamList = gatewayParamRepository.findAll();
        assertThat(gatewayParamList).hasSize(databaseSizeBeforeUpdate);
        GatewayParam testGatewayParam = gatewayParamList.get(gatewayParamList.size() - 1);
        assertThat(testGatewayParam.getUrl()).isEqualTo(UPDATED_URL);
        assertThat(testGatewayParam.getType()).isEqualTo(DEFAULT_TYPE);
        assertThat(testGatewayParam.getUsername()).isEqualTo(UPDATED_USERNAME);
        assertThat(testGatewayParam.getPassword()).isEqualTo(UPDATED_PASSWORD);
        assertThat(testGatewayParam.getToken()).isEqualTo(DEFAULT_TOKEN);
        assertThat(testGatewayParam.getBody()).isEqualTo(DEFAULT_BODY);
        assertThat(testGatewayParam.getStatusQuery()).isEqualTo(UPDATED_STATUS_QUERY);
        assertThat(testGatewayParam.getMessageQuery()).isEqualTo(UPDATED_MESSAGE_QUERY);
        assertThat(testGatewayParam.getSuccessCode()).isEqualTo(DEFAULT_SUCCESS_CODE);
        assertThat(testGatewayParam.getCallback()).isEqualTo(DEFAULT_CALLBACK);
        assertThat(testGatewayParam.getTimeout()).isEqualTo(DEFAULT_TIMEOUT);
        assertThat(testGatewayParam.getHeaders()).isEqualTo(UPDATED_HEADERS);
        assertThat(testGatewayParam.getDataQueries()).isEqualTo(DEFAULT_DATA_QUERIES);
        assertThat(testGatewayParam.getHttpMethodType()).isEqualTo(UPDATED_HTTP_METHOD_TYPE);
        assertThat(testGatewayParam.getProtocol()).isEqualTo(UPDATED_PROTOCOL);
        assertThat(testGatewayParam.getRequestMediaType()).isEqualTo(UPDATED_REQUEST_MEDIA_TYPE);
        assertThat(testGatewayParam.getResponseMediaType()).isEqualTo(DEFAULT_RESPONSE_MEDIA_TYPE);
        assertThat(testGatewayParam.getActive()).isEqualTo(DEFAULT_ACTIVE);
        assertThat(testGatewayParam.getMode()).isEqualTo(DEFAULT_MODE);
        assertThat(testGatewayParam.getExtIdQuery()).isEqualTo(UPDATED_EXT_ID_QUERY);
        assertThat(testGatewayParam.getSuccessOn2xx()).isEqualTo(DEFAULT_SUCCESS_ON_2_XX);
    }

    @Test
    @Transactional
    void fullUpdateGatewayParamWithPatch() throws Exception {
        // Initialize the database
        gatewayParamRepository.saveAndFlush(gatewayParam);

        int databaseSizeBeforeUpdate = gatewayParamRepository.findAll().size();

        // Update the gatewayParam using partial update
        GatewayParam partialUpdatedGatewayParam = new GatewayParam();
        partialUpdatedGatewayParam.setId(gatewayParam.getId());

        partialUpdatedGatewayParam
            .url(UPDATED_URL)
            .type(UPDATED_TYPE)
            .username(UPDATED_USERNAME)
            .password(UPDATED_PASSWORD)
            .token(UPDATED_TOKEN)
            .body(UPDATED_BODY)
            .statusQuery(UPDATED_STATUS_QUERY)
            .messageQuery(UPDATED_MESSAGE_QUERY)
            .successCode(UPDATED_SUCCESS_CODE)
            .callback(UPDATED_CALLBACK)
            .timeout(UPDATED_TIMEOUT)
            .headers(UPDATED_HEADERS)
            .dataQueries(UPDATED_DATA_QUERIES)
            .httpMethodType(UPDATED_HTTP_METHOD_TYPE)
            .protocol(UPDATED_PROTOCOL)
            .requestMediaType(UPDATED_REQUEST_MEDIA_TYPE)
            .responseMediaType(UPDATED_RESPONSE_MEDIA_TYPE)
            .active(UPDATED_ACTIVE)
            .mode(UPDATED_MODE)
            .extIdQuery(UPDATED_EXT_ID_QUERY)
            .successOn2xx(UPDATED_SUCCESS_ON_2_XX);

        restGatewayParamMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedGatewayParam.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedGatewayParam))
            )
            .andExpect(status().isOk());

        // Validate the GatewayParam in the database
        List<GatewayParam> gatewayParamList = gatewayParamRepository.findAll();
        assertThat(gatewayParamList).hasSize(databaseSizeBeforeUpdate);
        GatewayParam testGatewayParam = gatewayParamList.get(gatewayParamList.size() - 1);
        assertThat(testGatewayParam.getUrl()).isEqualTo(UPDATED_URL);
        assertThat(testGatewayParam.getType()).isEqualTo(UPDATED_TYPE);
        assertThat(testGatewayParam.getUsername()).isEqualTo(UPDATED_USERNAME);
        assertThat(testGatewayParam.getPassword()).isEqualTo(UPDATED_PASSWORD);
        assertThat(testGatewayParam.getToken()).isEqualTo(UPDATED_TOKEN);
        assertThat(testGatewayParam.getBody()).isEqualTo(UPDATED_BODY);
        assertThat(testGatewayParam.getStatusQuery()).isEqualTo(UPDATED_STATUS_QUERY);
        assertThat(testGatewayParam.getMessageQuery()).isEqualTo(UPDATED_MESSAGE_QUERY);
        assertThat(testGatewayParam.getSuccessCode()).isEqualTo(UPDATED_SUCCESS_CODE);
        assertThat(testGatewayParam.getCallback()).isEqualTo(UPDATED_CALLBACK);
        assertThat(testGatewayParam.getTimeout()).isEqualTo(UPDATED_TIMEOUT);
        assertThat(testGatewayParam.getHeaders()).isEqualTo(UPDATED_HEADERS);
        assertThat(testGatewayParam.getDataQueries()).isEqualTo(UPDATED_DATA_QUERIES);
        assertThat(testGatewayParam.getHttpMethodType()).isEqualTo(UPDATED_HTTP_METHOD_TYPE);
        assertThat(testGatewayParam.getProtocol()).isEqualTo(UPDATED_PROTOCOL);
        assertThat(testGatewayParam.getRequestMediaType()).isEqualTo(UPDATED_REQUEST_MEDIA_TYPE);
        assertThat(testGatewayParam.getResponseMediaType()).isEqualTo(UPDATED_RESPONSE_MEDIA_TYPE);
        assertThat(testGatewayParam.getActive()).isEqualTo(UPDATED_ACTIVE);
        assertThat(testGatewayParam.getMode()).isEqualTo(UPDATED_MODE);
        assertThat(testGatewayParam.getExtIdQuery()).isEqualTo(UPDATED_EXT_ID_QUERY);
        assertThat(testGatewayParam.getSuccessOn2xx()).isEqualTo(UPDATED_SUCCESS_ON_2_XX);
    }

    @Test
    @Transactional
    void patchNonExistingGatewayParam() throws Exception {
        int databaseSizeBeforeUpdate = gatewayParamRepository.findAll().size();
        gatewayParam.setId(count.incrementAndGet());

        // Create the GatewayParam
        GatewayParamDTO gatewayParamDTO = gatewayParamMapper.toDto(gatewayParam);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restGatewayParamMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, gatewayParamDTO.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(gatewayParamDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the GatewayParam in the database
        List<GatewayParam> gatewayParamList = gatewayParamRepository.findAll();
        assertThat(gatewayParamList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchGatewayParam() throws Exception {
        int databaseSizeBeforeUpdate = gatewayParamRepository.findAll().size();
        gatewayParam.setId(count.incrementAndGet());

        // Create the GatewayParam
        GatewayParamDTO gatewayParamDTO = gatewayParamMapper.toDto(gatewayParam);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restGatewayParamMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, count.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(gatewayParamDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the GatewayParam in the database
        List<GatewayParam> gatewayParamList = gatewayParamRepository.findAll();
        assertThat(gatewayParamList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamGatewayParam() throws Exception {
        int databaseSizeBeforeUpdate = gatewayParamRepository.findAll().size();
        gatewayParam.setId(count.incrementAndGet());

        // Create the GatewayParam
        GatewayParamDTO gatewayParamDTO = gatewayParamMapper.toDto(gatewayParam);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restGatewayParamMockMvc
            .perform(
                patch(ENTITY_API_URL)
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(gatewayParamDTO))
            )
            .andExpect(status().isMethodNotAllowed());

        // Validate the GatewayParam in the database
        List<GatewayParam> gatewayParamList = gatewayParamRepository.findAll();
        assertThat(gatewayParamList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteGatewayParam() throws Exception {
        // Initialize the database
        gatewayParamRepository.saveAndFlush(gatewayParam);

        int databaseSizeBeforeDelete = gatewayParamRepository.findAll().size();

        // Delete the gatewayParam
        restGatewayParamMockMvc
            .perform(delete(ENTITY_API_URL_ID, gatewayParam.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        List<GatewayParam> gatewayParamList = gatewayParamRepository.findAll();
        assertThat(gatewayParamList).hasSize(databaseSizeBeforeDelete - 1);
    }
}
