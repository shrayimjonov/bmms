package uz.paykap.web.rest;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import uz.devops.bm.paynet.config.PaynetConstants;
import uz.paykap.IntegrationTest;
import uz.paykap.adapter.DataAdapterResponse;
import uz.paykap.adapter.DataAdapterServiceV3;
import uz.paykap.config.ApplicationProperties;
import uz.paykap.domain.DataAdapterHistory;
import uz.paykap.domain.Gateway;
import uz.paykap.repository.DataAdapterHistoryRepository;
import uz.paykap.repository.GatewayRepository;
import uz.paykap.web.rest.errors.ExceptionTranslator;

import java.io.File;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uz.paykap.web.rest.TestUtil.createFormattingConversionService;

@RunWith(SpringRunner.class)
@IntegrationTest
public class DataAdapterResourceTest {

    @Autowired
    private MappingJackson2HttpMessageConverter jacksonMessageConverter;
    @Autowired
    private PageableHandlerMethodArgumentResolver pageableArgumentResolver;
    @Autowired
    private ExceptionTranslator exceptionTranslator;
    @Autowired
    private DataAdapterServiceV3 adapterService;
    @Autowired
    private DataAdapterHistoryRepository dataAdapterHistoryRepository;
    @Autowired
    private GatewayRepository gatewayRepository;
    @Autowired
    private ApplicationProperties properties;

    private MockMvc restAgentMockMvc;
    private Gson gson = new Gson();

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        final DataAdapterResource adapterResource = new DataAdapterResource(adapterService);
        this.restAgentMockMvc = MockMvcBuilders.standaloneSetup(adapterResource)
            .setCustomArgumentResolvers(pageableArgumentResolver)
            .setControllerAdvice(exceptionTranslator)
            .setConversionService(createFormattingConversionService())
            .setMessageConverters(jacksonMessageConverter).build();
    }

    @After
    public void clean() {
        String uploadDir = properties.getPaynet().getUploadDir();

        File dir = new File(uploadDir);

        if (!dir.exists()) {
            return;
        }

        for (File file : dir.listFiles()) {
            file.delete();
        }

        dir.delete();
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    @Transactional
    public void syncAll() throws Exception {
        MvcResult mvcResult = restAgentMockMvc.perform(get("/api/adapter/list"))
            .andExpect(status().isOk())
            .andReturn();

        Type type = new TypeToken<ArrayList<DataAdapterResponse>>() {
        }.getType();

        ArrayList<DataAdapterResponse> adapters = gson.fromJson(mvcResult.getResponse().getContentAsString(), type);
        assertThat(adapters.size()).isGreaterThan(0);

        for (DataAdapterResponse adapter : adapters) {
            if (Objects.equals(adapter.getName(), "osonDataAdapter") || Objects.equals(adapter.getName(), PaynetConstants.PAYNET_RPC_DATA_ADAPTER)) {
                continue;//TODO ZAFAR add simulation for osonDataAdapter !!
            }
            syncOne(adapter.getName());
        }

//        long categoriesCount = categoryRepository.count();
//        long recipientsCount = recipientRepository.count();
//        long fieldsCount = fieldRepository.count();
//        long fieldValuesCount = fieldValueRepository.count();

//        for (String adapter : adapters) {
//            syncOne(adapter);
//        }

//        long categoriesCount2 = categoryRepository.count();
//        long recipientsCount2 = recipientRepository.count();
//        long fieldsCount2 = fieldRepository.count();
//        long fieldValuesCount2 = fieldValueRepository.count();

//        assertThat(categoryRepository.count()).isEqualTo(categoriesCount);
//        assertThat(recipientRepository.count()).isEqualTo(recipientsCount);
//        assertThat(fieldsCount2).isEqualTo(fieldsCount);
//        assertThat(fieldValuesCount2).isEqualTo(fieldValuesCount);

//        properties.getPaynet().setPaynetJsonPath("paynetWM.json");
//        System.out.println("### test ### " + properties.getPaynet().getPaynetJsonPath());
//
//        for (String adapter : adapters) {
//            syncOne(adapter);
//        }
//
//        assertThat(categoryRepository.count()).isGreaterThan(categoriesCount);
//        assertThat(recipientRepository.count()).isGreaterThan(recipientsCount);
    }

    public void syncOne(String adapter) throws Exception {

        int databaseSizeBeforeCreate = dataAdapterHistoryRepository.findAll().size();
        int gwDatabaseSizeBeforeCreate = gatewayRepository.findAll().size();

        restAgentMockMvc.perform(get("/api/adapter/sync/{adapter}", adapter))
            .andExpect(status().isOk())
            .andReturn();

        List<DataAdapterHistory> dataAdapterHistoryList = dataAdapterHistoryRepository.findAll();
        assertThat(dataAdapterHistoryList).hasSize(databaseSizeBeforeCreate + 1);
        DataAdapterHistory testDataAdapterHistory = dataAdapterHistoryList.get(dataAdapterHistoryList.size() - 1);
        assertThat(testDataAdapterHistory.getName()).isNotEmpty();
        assertThat(testDataAdapterHistory.getDetails()).isNotEmpty();

        List<Gateway> gateways = gatewayRepository.findAll();
        assertThat(gateways).hasSize(databaseSizeBeforeCreate + 1);
        Gateway gateway = gateways.get(gateways.size() - 1);
        assertThat(gateway.getName()).isNotEmpty();

//        assertThat(mvcResult.getResponse().getContentAsString()).isNotEmpty();
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    @Transactional
    public void getListOfAgents() throws Exception {
        MvcResult mvcResult = restAgentMockMvc.perform(get("/api/adapter/list"))
            .andExpect(status().isOk())
            .andReturn();

        Type type = new TypeToken<ArrayList<DataAdapterResponse>>() {
        }.getType();

        ArrayList<DataAdapterResponse> agentsList = gson.fromJson(mvcResult.getResponse().getContentAsString(), type);
        assertThat(agentsList.size()).isGreaterThan(0);
    }
}
