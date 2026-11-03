package uz.paykap.config.http;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.xml.MappingJackson2XmlHttpMessageConverter;
import org.springframework.web.client.RestTemplate;
import uz.devops.humo.config.HumoProperties;
import uz.devops.humoMiddleware.config.HumoMiddlewareProperties;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Slf4j
@Configuration
@EnableConfigurationProperties({HumoProperties.class, HumoMiddlewareProperties.class})
public class CustomRestTemplateConfig {

    @Autowired
    private HttpClientProperties properties;

    // humoMobileRestTemplate
    @Bean("humoMobileRestTemplate")
    public RestTemplate humoMobileRestTemplate(HttpComponentsClientHttpRequestFactory clientHttpRequestFactory) {
        log.info("$$$$$$$$$ humoMobileRestTemplate customized $$$$$$$$$$");
        final RestTemplate restTemplate = new RestTemplate(clientHttpRequestFactory);
        customize(restTemplate);

        List<HttpMessageConverter<?>> messageConverters = new ArrayList<>();
        MappingJackson2XmlHttpMessageConverter converter = new MappingJackson2XmlHttpMessageConverter();
        converter.getObjectMapper().registerModule(new JaxbAnnotationModule());
        converter.getObjectMapper().enable(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_USING_DEFAULT_VALUE);
        converter.setSupportedMediaTypes(Collections.singletonList(MediaType.TEXT_XML));
        messageConverters.add(new StringHttpMessageConverter(StandardCharsets.UTF_8));
        messageConverters.add(converter);
        restTemplate.setMessageConverters(messageConverters);

        return restTemplate;
    }

    // humoMiddlewareRestTemplate
    @Bean("humoMiddlewareRestTemplate")
    public RestTemplate humoMiddlewareRestTemplate(HttpComponentsClientHttpRequestFactory clientHttpRequestFactory) {
        log.info("$$$$$$$$$ humoMiddlewareRestTemplate customized $$$$$$$$$$");
        final RestTemplate restTemplate = new RestTemplate(clientHttpRequestFactory);
        MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter = new MappingJackson2HttpMessageConverter();
        restTemplate.getMessageConverters().add(mappingJackson2HttpMessageConverter);
        customize(restTemplate);
        return restTemplate;
    }

    @Bean("paynetRestTemplate")
    public RestTemplate paynetRestTemplate(HttpComponentsClientHttpRequestFactory clientHttpRequestFactory) {
        log.info("$$$$$$$$$ paynetRestTemplate customized $$$$$$$$$$");
        final RestTemplate restTemplate = new RestTemplate(clientHttpRequestFactory);
//        MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter = new MappingJackson2HttpMessageConverter();
//        restTemplate.getMessageConverters().add(mappingJackson2HttpMessageConverter);
        customize(restTemplate);
        return restTemplate;
    }

    // anorhubPluginRestTemplate
    // todo

    // issuingRestTemplate
    // Abdurahim

    // bmNciLoanRestTemplate
    // isLoan used? Abdurahim

    // paynetLogoRestTemplate
    // todo

    // nciIsApiRestTemplate
    // todo

    // nciTransRestTemplate
    // todo

    // bmTkbRestTemplate
    // todo

    // davrMunisRestTemplate

    // playRestTemplate

    private void customize(RestTemplate restTemplate) {
        ClientHttpRequestFactory requestFactory = restTemplate.getRequestFactory();
        // not-so-proudly copied from the TestRestTemplate code:
//        if (requestFactory instanceof InterceptingClientHttpRequestFactory) {
//            Field requestFactoryField = ReflectionUtils.findField(RestTemplate.class, "requestFactory");
//            ReflectionUtils.makeAccessible(requestFactoryField);
//            requestFactory = (ClientHttpRequestFactory) ReflectionUtils.getField(requestFactoryField, restTemplate);
//        }
        if (requestFactory instanceof HttpComponentsClientHttpRequestFactory) {
//            HttpComponentsClientHttpRequestFactory clientRequestFactory =
//                    (HttpComponentsClientHttpRequestFactory) requestFactory;
//            clientRequestFactory.setHttpClient(httpClient);
            if (properties.isLoggingEnabled()) {
                restTemplate.setRequestFactory(new BufferingClientHttpRequestFactory(requestFactory));
                restTemplate.getInterceptors().add(new LoggingClientHttpRequestInterceptor());
            }
        }


    }

    public class MappingJackson2HttpMessageConverter extends org.springframework.http.converter.json.MappingJackson2HttpMessageConverter {
        MappingJackson2HttpMessageConverter() {
            List<MediaType> mediaTypes = new ArrayList();
            mediaTypes.add(MediaType.valueOf(MediaType.ALL + ";charset=UTF-8"));
            this.setSupportedMediaTypes(mediaTypes);
        }
    }


}
