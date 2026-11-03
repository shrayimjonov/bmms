package uz.paykap.web.rest;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import uz.paykap.IntegrationTest;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.Validator;
import uz.paykap.PkbillmsApp;
import uz.paykap.config.ApplicationProperties;
import uz.paykap.IntegrationTest;
import uz.paykap.service.dto.GlobalSettings;
import uz.paykap.web.rest.errors.ExceptionTranslator;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uz.paykap.web.rest.TestUtil.createFormattingConversionService;

@RunWith(SpringRunner.class)
@IntegrationTest
public class GlobalSettingsResourceTest {

    private MockMvc restGatewayMockMvc;

    @Autowired
    private ApplicationProperties applicationProperties;

    @Autowired
    private MappingJackson2HttpMessageConverter jacksonMessageConverter;

    @Autowired
    private ExceptionTranslator exceptionTranslator;

    @Autowired
    private Validator validator;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        final GlobalSettingsResource gatewayResource = new GlobalSettingsResource(applicationProperties);
        this.restGatewayMockMvc = MockMvcBuilders.standaloneSetup(gatewayResource)
//            .setCustomArgumentResolvers(pageableArgumentResolver)
            .setControllerAdvice(exceptionTranslator)
            .setConversionService(createFormattingConversionService())
            .setMessageConverters(jacksonMessageConverter)
            .setValidator(validator).build();
    }

    @Test
    @Transactional
    public void setGlobalSettings() throws Exception {

        Boolean checkEnabled0 = applicationProperties.getCheckEnabled();
        Boolean payEnabled0 = applicationProperties.getPayEnabled();

        Boolean checkEnabled = applicationProperties.getCheckEnabled();
        Boolean payEnabled = applicationProperties.getPayEnabled();

        assert checkEnabled == Boolean.TRUE;
        assert payEnabled == Boolean.TRUE;

        GlobalSettings gatewayDTO = new GlobalSettings();
        gatewayDTO.setEnableCheck(Boolean.FALSE);
        gatewayDTO.setEnablePay(Boolean.FALSE);

        restGatewayMockMvc.perform(post("/api/global-settings/set")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(gatewayDTO)))
            .andExpect(status().isOk());

        checkEnabled = applicationProperties.getCheckEnabled();
        payEnabled = applicationProperties.getPayEnabled();

        assert checkEnabled == Boolean.FALSE;
        assert payEnabled == Boolean.FALSE;

        // Pass null params, should not change value

        gatewayDTO.setEnableCheck(null);
        gatewayDTO.setEnablePay(null);

        restGatewayMockMvc.perform(post("/api/global-settings/set")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(gatewayDTO)))
            .andExpect(status().isOk());

        checkEnabled = applicationProperties.getCheckEnabled();
        payEnabled = applicationProperties.getPayEnabled();

        assert checkEnabled == Boolean.FALSE;
        assert payEnabled == Boolean.FALSE;


        // Pass null body, should not change value

        restGatewayMockMvc.perform(post("/api/global-settings/set")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(""))
            .andExpect(status().isBadRequest());

        checkEnabled = applicationProperties.getCheckEnabled();
        payEnabled = applicationProperties.getPayEnabled();

        assert checkEnabled == Boolean.FALSE;
        assert payEnabled == Boolean.FALSE;

        applicationProperties.setCheckEnabled(checkEnabled0);
        applicationProperties.setPayEnabled(payEnabled0);

    }
}
