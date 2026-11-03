package uz.paykap.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static uz.paykap.gateway.common.CommonGatewayServiceTest.COMMON_RECIPIENTS_FOLDER;
import static uz.paykap.web.rest.TestUtil.createFormattingConversionService;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import javax.persistence.EntityManager;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import uz.paykap.IntegrationTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.ResourcePatternUtils;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.Validator;
import uz.devops.bmcore.dto.request.CheckRequest;
import uz.devops.bmcore.enumeration.RequestType;
import uz.javlon.commons.exception.CodedException;
import uz.javlon.commons.exception.ProblemEnum;
import uz.paykap.PkbillmsApp;
import uz.paykap.adapter.DataAdapterServiceV3;
import uz.paykap.adapter.common.CommonDataAdapterV3;
import uz.paykap.config.Constants;
import uz.paykap.IntegrationTest;
import uz.paykap.domain.GatewayParam;
import uz.paykap.domain.Recipient;
import uz.paykap.domain.enumeration.MethodType;
import uz.paykap.domain.enumeration.ModeType;
import uz.paykap.domain.enumeration.ProtocolType;
import uz.paykap.gateway.common.CommonGatewayHelperService;
import uz.paykap.gateway.common.CommonGatewayServiceV3;
import uz.paykap.gateway.common.simulation.SimulationBase;
import uz.paykap.repository.GatewayParamRepository;
import uz.paykap.repository.RecipientRepository;
import uz.paykap.service.AgentRecipientService;
import uz.paykap.service.AgentService;
import uz.paykap.service.GatewayParamQueryService;
import uz.paykap.service.GatewayParamService;
import uz.paykap.service.dto.AgentDTO;
import uz.paykap.service.dto.GatewayParamDTO;
import uz.paykap.service.mapper.GatewayParamMapper;
import uz.paykap.web.rest.errors.ExceptionTranslator;

/**
 * Test class for the GatewayParamResource REST controller.
 *
 * @see GatewayParamResource
 */
@RunWith(SpringRunner.class)
@IntegrationTest
public class GatewayParamResourceIntTest {

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

    private static final String DEFAULT_HEADERS = "AAAAAAAAAA";
    private static final String UPDATED_HEADERS = "BBBBBBBBBB";

    private static final String DEFAULT_DATA_QUERIES = "AAAAAAAAAA";
    private static final String UPDATED_DATA_QUERIES = "BBBBBBBBBB";

    private static final MethodType DEFAULT_HTTP_METHOD_TYPE = MethodType.GET;
    private static final MethodType UPDATED_HTTP_METHOD_TYPE = MethodType.POST;

    private static final ProtocolType DEFAULT_PROTOCOL = ProtocolType.REST;
    private static final ProtocolType UPDATED_PROTOCOL = ProtocolType.WS;

    private static final String DEFAULT_REQUEST_MEDIA_TYPE = "AAAAAAAAAA";
    private static final String UPDATED_REQUEST_MEDIA_TYPE = "BBBBBBBBBB";

    private static final String DEFAULT_RESPONSE_MEDIA_TYPE = "AAAAAAAAAA";
    private static final String UPDATED_RESPONSE_MEDIA_TYPE = "BBBBBBBBBB";

    private static final Boolean DEFAULT_ACTIVE = false;
    private static final Boolean UPDATED_ACTIVE = true;

    private static final ModeType DEFAULT_MODE = ModeType.TEST;
    private static final ModeType UPDATED_MODE = ModeType.DEV;

    private static final String DEFAULT_EXT_ID_QUERY = "AAAAAAAAAA";
    private static final String UPDATED_EXT_ID_QUERY = "BBBBBBBBBB";

    @Autowired
    private GatewayParamRepository gatewayParamRepository;

    @Autowired
    private GatewayParamMapper gatewayParamMapper;

    @Autowired
    private GatewayParamService gatewayParamService;

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

    @Autowired
    private GatewayParamQueryService gatewayParamQueryService;

    @Autowired
    private RecipientRepository recipientRepository;

    @Autowired
    private DataAdapterServiceV3 adapterServiceV3;

    @Autowired
    private ResourceLoader resourceLoader;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AgentService agentService;

    @Autowired
    private AgentRecipientService agentRecipientService;

    private MockMvc restGatewayParamMockMvc;

    private GatewayParam gatewayParam;

    @Autowired
    private CommonGatewayServiceV3 commonGatewayServiceV3;

    @Autowired
    private CommonGatewayHelperService commonGatewayHelperService;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        final GatewayParamResource gatewayParamResource = new GatewayParamResource(
            gatewayParamService,
            commonGatewayServiceV3,
            commonGatewayHelperService,
            recipientRepository,
            gatewayParamQueryService,
            gatewayParamRepository
        );
        this.restGatewayParamMockMvc =
            MockMvcBuilders
                .standaloneSetup(gatewayParamResource)
                .setCustomArgumentResolvers(pageableArgumentResolver)
                .setControllerAdvice(exceptionTranslator)
                .setConversionService(createFormattingConversionService())
                .setMessageConverters(jacksonMessageConverter)
                .setValidator(validator)
                .build();
    }

    @Test
    @WithMockUser(username = Constants.SYSTEM_AGENT_NAME)
    @Transactional
    public void freeRequestTest() throws Exception {
        adapterServiceV3.sync(CommonDataAdapterV3.NAME);

        Resource[] resources = loadRecipientResources();

        for (Resource resource : resources) {
            String recipientShortName = FilenameUtils.getBaseName(resource.getFilename());

            System.out.println("\n\n##################\n" + recipientShortName + "\n##################\n");

            SimulationBase simulationBase = loadRecipientResources(recipientShortName);

            Recipient recipient = recipientRepository
                .findTop1ByShortName(recipientShortName)
                .orElseThrow(() -> new CodedException(ProblemEnum.SERVICE_NOT_FOUND));

            AgentDTO agent = agentService.getAgentOrCreate(Constants.SYSTEM_AGENT_NAME, 10000000);

            agentRecipientService.createOrGet(recipient.getId(), agent);

            //CHECK

            CheckRequest checkDTO = new CheckRequest();
            checkDTO.setRecipientId(recipient.getId());
            checkDTO.setAmount(simulationBase.getCheck().getAmount());
            checkDTO.setAccount(simulationBase.getCheck().getAccount());

            MvcResult mvcResult = restGatewayParamMockMvc
                .perform(
                    post("/api/gateway-params/free/CHECK")
                        .contentType(TestUtil.APPLICATION_JSON_UTF8)
                        .content(TestUtil.convertObjectToJsonBytes(checkDTO))
                )
                .andExpect(status().isOk())
                .andReturn();

            String result = mvcResult.getResponse().getContentAsString();

            System.out.println(result);

            MvcResult payMvcResult = restGatewayParamMockMvc
                .perform(
                    post("/api/gateway-params/free/PAY")
                        .contentType(TestUtil.APPLICATION_JSON_UTF8)
                        .content(TestUtil.convertObjectToJsonBytes(checkDTO))
                )
                .andExpect(status().isOk())
                .andReturn();

            String payResult = payMvcResult.getResponse().getContentAsString();

            System.out.println(payResult);
            //            restGatewayParamMockMvc.perform(post("/api/gateway-params/free/CANCEL")
            //                .contentType(TestUtil.APPLICATION_JSON_UTF8)
            //                .content(TestUtil.convertObjectToJsonBytes(checkDTO)))
            //                .andExpect(status().isOk());

            //PAY

            //            CommonPayRequest payDTO = new PayRequest();
            //            payDTO.setBillId(check.getId());
            //
            //            billsSize = billRepository.findAll().size();
            //
            //            System.out.println("Testing pay : " + recipient.getShortName() + " : " + payDTO);
            //
            //            SimpleBillResponse result = commonGatewayServiceV3.pay(payDTO);
            //
            //            shouldBeValid(billsSize, recipient, result, RequestType.PAY, checkDTO);
        }
    }

    private Resource[] loadRecipientResources(/*String pattern*/) {
        try {
            return ResourcePatternUtils.getResourcePatternResolver(resourceLoader).getResources("classpath*:simulation/*.json");
        } catch (IOException e) {
            return new Resource[] {};
        }
    }

    private SimulationBase loadRecipientResources(String fileName) {
        try {
            return objectMapper.readValue(
                IOUtils.toString(
                    new ClassPathResource(COMMON_RECIPIENTS_FOLDER + fileName + ".json").getInputStream(),
                    StandardCharsets.UTF_8
                ),
                SimulationBase.class
            );
        } catch (IOException e) {
            throw new CodedException(ProblemEnum.OBJECT_NOT_FOUND);
        }
    }

    /**
     * Create an entity for this test.
     * <p>
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
            .extIdQuery(DEFAULT_EXT_ID_QUERY);
        return gatewayParam;
    }

    @Before
    public void initTest() {
        gatewayParam = createEntity(em);
    }

    @Test
    @Transactional
    public void createGatewayParam() throws Exception {
        int databaseSizeBeforeCreate = gatewayParamRepository.findAll().size();

        // Create the GatewayParam
        GatewayParamDTO gatewayParamDTO = gatewayParamMapper.toDto(gatewayParam);
        restGatewayParamMockMvc
            .perform(
                post("/api/gateway-params")
                    .contentType(TestUtil.APPLICATION_JSON_UTF8)
                    .content(TestUtil.convertObjectToJsonBytes(gatewayParamDTO))
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
        assertThat(testGatewayParam.isActive()).isEqualTo(DEFAULT_ACTIVE);
        assertThat(testGatewayParam.getMode()).isEqualTo(DEFAULT_MODE);
        assertThat(testGatewayParam.getExtIdQuery()).isEqualTo(DEFAULT_EXT_ID_QUERY);
    }

    @Test
    @Transactional
    public void createGatewayParamWithExistingId() throws Exception {
        int databaseSizeBeforeCreate = gatewayParamRepository.findAll().size();

        // Create the GatewayParam with an existing ID
        gatewayParam.setId(1L);
        GatewayParamDTO gatewayParamDTO = gatewayParamMapper.toDto(gatewayParam);

        // An entity with an existing ID cannot be created, so this API call must fail
        restGatewayParamMockMvc
            .perform(
                post("/api/gateway-params")
                    .contentType(TestUtil.APPLICATION_JSON_UTF8)
                    .content(TestUtil.convertObjectToJsonBytes(gatewayParamDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the GatewayParam in the database
        List<GatewayParam> gatewayParamList = gatewayParamRepository.findAll();
        assertThat(gatewayParamList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    public void checkActiveIsRequired() throws Exception {
        int databaseSizeBeforeTest = gatewayParamRepository.findAll().size();
        // set the field null
        gatewayParam.setActive(null);

        // Create the GatewayParam, which fails.
        GatewayParamDTO gatewayParamDTO = gatewayParamMapper.toDto(gatewayParam);

        restGatewayParamMockMvc
            .perform(
                post("/api/gateway-params")
                    .contentType(TestUtil.APPLICATION_JSON_UTF8)
                    .content(TestUtil.convertObjectToJsonBytes(gatewayParamDTO))
            )
            .andExpect(status().isBadRequest());

        List<GatewayParam> gatewayParamList = gatewayParamRepository.findAll();
        assertThat(gatewayParamList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void getAllGatewayParams() throws Exception {
        // Initialize the database
        gatewayParamRepository.saveAndFlush(gatewayParam);

        // Get all the gatewayParamList
        restGatewayParamMockMvc
            .perform(get("/api/gateway-params?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(gatewayParam.getId().intValue())))
            .andExpect(jsonPath("$.[*].url").value(hasItem(DEFAULT_URL.toString())))
            .andExpect(jsonPath("$.[*].type").value(hasItem(DEFAULT_TYPE.toString())))
            .andExpect(jsonPath("$.[*].username").value(hasItem(DEFAULT_USERNAME.toString())))
            .andExpect(jsonPath("$.[*].password").value(hasItem(DEFAULT_PASSWORD.toString())))
            .andExpect(jsonPath("$.[*].token").value(hasItem(DEFAULT_TOKEN.toString())))
            .andExpect(jsonPath("$.[*].body").value(hasItem(DEFAULT_BODY.toString())))
            .andExpect(jsonPath("$.[*].statusQuery").value(hasItem(DEFAULT_STATUS_QUERY.toString())))
            .andExpect(jsonPath("$.[*].messageQuery").value(hasItem(DEFAULT_MESSAGE_QUERY.toString())))
            .andExpect(jsonPath("$.[*].successCode").value(hasItem(DEFAULT_SUCCESS_CODE.toString())))
            .andExpect(jsonPath("$.[*].callback").value(hasItem(DEFAULT_CALLBACK.toString())))
            .andExpect(jsonPath("$.[*].timeout").value(hasItem(DEFAULT_TIMEOUT)))
            .andExpect(jsonPath("$.[*].headers").value(hasItem(DEFAULT_HEADERS.toString())))
            .andExpect(jsonPath("$.[*].dataQueries").value(hasItem(DEFAULT_DATA_QUERIES.toString())))
            .andExpect(jsonPath("$.[*].httpMethodType").value(hasItem(DEFAULT_HTTP_METHOD_TYPE.toString())))
            .andExpect(jsonPath("$.[*].protocol").value(hasItem(DEFAULT_PROTOCOL.toString())))
            .andExpect(jsonPath("$.[*].requestMediaType").value(hasItem(DEFAULT_REQUEST_MEDIA_TYPE.toString())))
            .andExpect(jsonPath("$.[*].responseMediaType").value(hasItem(DEFAULT_RESPONSE_MEDIA_TYPE.toString())))
            .andExpect(jsonPath("$.[*].active").value(hasItem(DEFAULT_ACTIVE.booleanValue())))
            .andExpect(jsonPath("$.[*].mode").value(hasItem(DEFAULT_MODE.toString())))
            .andExpect(jsonPath("$.[*].extIdQuery").value(hasItem(DEFAULT_EXT_ID_QUERY.toString())));
    }

    @Test
    @Transactional
    public void getGatewayParam() throws Exception {
        // Initialize the database
        gatewayParamRepository.saveAndFlush(gatewayParam);

        // Get the gatewayParam
        restGatewayParamMockMvc
            .perform(get("/api/gateway-params/{id}", gatewayParam.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.id").value(gatewayParam.getId().intValue()))
            .andExpect(jsonPath("$.url").value(DEFAULT_URL.toString()))
            .andExpect(jsonPath("$.type").value(DEFAULT_TYPE.toString()))
            .andExpect(jsonPath("$.username").value(DEFAULT_USERNAME.toString()))
            .andExpect(jsonPath("$.password").value(DEFAULT_PASSWORD.toString()))
            .andExpect(jsonPath("$.token").value(DEFAULT_TOKEN.toString()))
            .andExpect(jsonPath("$.body").value(DEFAULT_BODY.toString()))
            .andExpect(jsonPath("$.statusQuery").value(DEFAULT_STATUS_QUERY.toString()))
            .andExpect(jsonPath("$.messageQuery").value(DEFAULT_MESSAGE_QUERY.toString()))
            .andExpect(jsonPath("$.successCode").value(DEFAULT_SUCCESS_CODE.toString()))
            .andExpect(jsonPath("$.callback").value(DEFAULT_CALLBACK.toString()))
            .andExpect(jsonPath("$.timeout").value(DEFAULT_TIMEOUT))
            .andExpect(jsonPath("$.headers").value(DEFAULT_HEADERS.toString()))
            .andExpect(jsonPath("$.dataQueries").value(DEFAULT_DATA_QUERIES.toString()))
            .andExpect(jsonPath("$.httpMethodType").value(DEFAULT_HTTP_METHOD_TYPE.toString()))
            .andExpect(jsonPath("$.protocol").value(DEFAULT_PROTOCOL.toString()))
            .andExpect(jsonPath("$.requestMediaType").value(DEFAULT_REQUEST_MEDIA_TYPE.toString()))
            .andExpect(jsonPath("$.responseMediaType").value(DEFAULT_RESPONSE_MEDIA_TYPE.toString()))
            .andExpect(jsonPath("$.active").value(DEFAULT_ACTIVE.booleanValue()))
            .andExpect(jsonPath("$.mode").value(DEFAULT_MODE.toString()))
            .andExpect(jsonPath("$.extIdQuery").value(DEFAULT_EXT_ID_QUERY.toString()));
    }

    @Test
    @Transactional
    public void getNonExistingGatewayParam() throws Exception {
        // Get the gatewayParam
        restGatewayParamMockMvc.perform(get("/api/gateway-params/{id}", Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updateGatewayParam() throws Exception {
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
            .extIdQuery(UPDATED_EXT_ID_QUERY);
        GatewayParamDTO gatewayParamDTO = gatewayParamMapper.toDto(updatedGatewayParam);

        restGatewayParamMockMvc
            .perform(
                put("/api/gateway-params")
                    .contentType(TestUtil.APPLICATION_JSON_UTF8)
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
        assertThat(testGatewayParam.isActive()).isEqualTo(UPDATED_ACTIVE);
        assertThat(testGatewayParam.getMode()).isEqualTo(UPDATED_MODE);
        assertThat(testGatewayParam.getExtIdQuery()).isEqualTo(UPDATED_EXT_ID_QUERY);
    }

    @Test
    @Transactional
    public void updateNonExistingGatewayParam() throws Exception {
        int databaseSizeBeforeUpdate = gatewayParamRepository.findAll().size();

        // Create the GatewayParam
        GatewayParamDTO gatewayParamDTO = gatewayParamMapper.toDto(gatewayParam);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restGatewayParamMockMvc
            .perform(
                put("/api/gateway-params")
                    .contentType(TestUtil.APPLICATION_JSON_UTF8)
                    .content(TestUtil.convertObjectToJsonBytes(gatewayParamDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the GatewayParam in the database
        List<GatewayParam> gatewayParamList = gatewayParamRepository.findAll();
        assertThat(gatewayParamList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    public void deleteGatewayParam() throws Exception {
        // Initialize the database
        gatewayParamRepository.saveAndFlush(gatewayParam);

        int databaseSizeBeforeDelete = gatewayParamRepository.findAll().size();

        // Get the gatewayParam
        restGatewayParamMockMvc
            .perform(delete("/api/gateway-params/{id}", gatewayParam.getId()).accept(TestUtil.APPLICATION_JSON_UTF8))
            .andExpect(status().isOk());

        // Validate the database is empty
        List<GatewayParam> gatewayParamList = gatewayParamRepository.findAll();
        assertThat(gatewayParamList).hasSize(databaseSizeBeforeDelete - 1);
    }

    @Test
    @Transactional
    public void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(GatewayParam.class);
        GatewayParam gatewayParam1 = new GatewayParam();
        gatewayParam1.setId(1L);
        GatewayParam gatewayParam2 = new GatewayParam();
        gatewayParam2.setId(gatewayParam1.getId());
        assertThat(gatewayParam1).isEqualTo(gatewayParam2);
        gatewayParam2.setId(2L);
        assertThat(gatewayParam1).isNotEqualTo(gatewayParam2);
        gatewayParam1.setId(null);
        assertThat(gatewayParam1).isNotEqualTo(gatewayParam2);
    }

    @Test
    @Transactional
    public void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(GatewayParamDTO.class);
        GatewayParamDTO gatewayParamDTO1 = new GatewayParamDTO();
        gatewayParamDTO1.setId(1L);
        GatewayParamDTO gatewayParamDTO2 = new GatewayParamDTO();
        assertThat(gatewayParamDTO1).isNotEqualTo(gatewayParamDTO2);
        gatewayParamDTO2.setId(gatewayParamDTO1.getId());
        assertThat(gatewayParamDTO1).isEqualTo(gatewayParamDTO2);
        gatewayParamDTO2.setId(2L);
        assertThat(gatewayParamDTO1).isNotEqualTo(gatewayParamDTO2);
        gatewayParamDTO1.setId(null);
        assertThat(gatewayParamDTO1).isNotEqualTo(gatewayParamDTO2);
    }
    //
    //    @Test
    //    @Transactional
    //    public void testEntityFromId() {
    //        assertThat(gatewayParamMapper.fromId(42L).getId()).isEqualTo(42);
    //        assertThat(gatewayParamMapper.fromId(null)).isNull();
    //    }
}
