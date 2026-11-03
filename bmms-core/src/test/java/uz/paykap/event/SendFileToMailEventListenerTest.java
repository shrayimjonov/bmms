package uz.paykap.event;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import uz.paykap.IntegrationTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.*;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
//import uz.javlon.kapitalsms.service.KapitalsmsService;
import uz.paykap.IntegrationTest;
import uz.paykap.PkbillmsApp;
import uz.paykap.adapter.verification.AutomaticTransactionVerifierServiceTest;
import uz.paykap.config.ApplicationProperties;
import uz.paykap.event.dto.AttachmentReceiverType;
import uz.paykap.event.dto.ReconcileFileReceiverDTO;
import uz.paykap.event.dto.SendEmailWithAttachmentEvent;
import uz.paykap.gateway.dto.SmsDTO;
import uz.paykap.service.MailService;

import javax.net.ssl.SSLContext;
import java.io.File;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

@IntegrationTest
@ExtendWith(MockitoExtension.class)
@AutoConfigureMockMvc
@WithMockUser
public class SendFileToMailEventListenerTest {


    @Autowired
    private ApplicationContext eventPublisher;

    @Autowired
    private ApplicationProperties properties;

    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;

    @Autowired
    private MailService mailService;

//    @Autowired
//    private KapitalsmsService kapitalsmsService;

    @Qualifier("kapitalsmsRestTemplate")
    private RestTemplate restTemplate;

    @Test
    @Transactional
    public void testSendEmailFromEventPublisher() {
        properties.setSimulateSendFileToEmail(true);

        File file = deleteFile();

        List<ReconcileFileReceiverDTO> recipientList = new ArrayList<>();
        recipientList.add(new ReconcileFileReceiverDTO("user@example.com", "C:\\projects\\billMaster\\pkbillms\\reconciliation\\Beeline-20181206\\Beeline.csv", AttachmentReceiverType.OTHER));
        SendEmailWithAttachmentEvent event = new SendEmailWithAttachmentEvent(this, " subject ", recipientList);

        eventPublisher.publishEvent(event);

        AutomaticTransactionVerifierServiceTest.issetFile(file);

//        eventPublisher.publishEvent(event);

//        verify(eventListener, times(2)).onApplicationEvent(event);// method bir marta chaqirildimi yo'qmi tekshiradi

        deleteFile();

    }


//    @Test
//    @Transactional
//    public void testSend() {
//        mailService.sendEmail("user@example.com", "user@example.com", "Hello World", "<p style=\"color:DarkBlue\"> <br>test</br>  <br>test2WithEnter</br> <br>test</br> <br>test</br></p>", null,true);
//    }

    @Test
    @Transactional
    public void testSend() {
        properties.setSimulateSendFileToEmail(false);
        mailService.sendEmailWithFile("user@example.com", "user@example.com", "test", "D:\\temp\\pkbillms\\PAY_SUCCESS.json", "hello world", false);
    }

//    @Test
    @Transactional
    public void testSendPhone() throws JsonProcessingException {
//        F2AProvider f2AProvider = new F2AProvider()
//            .decisionMaker((String login) -> true)
//            .recipientResolver((login -> String.valueOf(998901234567L)) //TODO
//                .userDeatailsService(userDetailsService)
//                .passwordEncoder(passwordEncoder());
//
//        f2AProvider.sendStrategy(kapitalsmsService)
//            .otpGenerator(new DefaultOtpGenerator());
//        restTemplate = new RestTemplate();
        String s = sendSms("998901234567", "Zdarova").getBody();

    }

    private ResponseEntity<String> sendSms(String phoneNumber, String text) throws JsonProcessingException {
        RestTemplate restTemplate = getRestTemplate();

        RestTemplate restTemplate1 = new RestTemplate();
        restTemplate1.setRequestFactory(new HttpComponentsClientHttpRequestFactory());

        // Add converters
        // Note I use the Jackson Converter, I removed the http form converter
        // because it is not needed when posting String, used for multipart forms.
        restTemplate1.getMessageConverters().add(new MappingJackson2HttpMessageConverter());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        ObjectMapper mapper = new ObjectMapper();
        SmsDTO smsDTO = new SmsDTO("REDACTED", phoneNumber, text);
        String body = mapper.writeValueAsString(smsDTO);

        return restTemplate1.exchange("https://internal.example.com:17617/SMS.svc/send", HttpMethod.POST, new HttpEntity<>(body,headers), String.class);
    }

    private RestTemplate getRestTemplate() {
        TrustStrategy acceptingTrustStrategy = (x509Certificates, s) -> true;
        SSLContext sslContext = null;
        try {
            sslContext = org.apache.http.ssl.SSLContexts.custom().loadTrustMaterial(null, acceptingTrustStrategy).build();
        } catch (NoSuchAlgorithmException | KeyManagementException | KeyStoreException e) {
            e.printStackTrace();
        }

        SSLConnectionSocketFactory csf = new SSLConnectionSocketFactory(sslContext, new NoopHostnameVerifier());

        CloseableHttpClient httpClient = HttpClients.custom()
            .setSSLSocketFactory(csf)
//            .setProxy(new HttpHost("127.0.0.1", 3737))
            .build();

        HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
        requestFactory.setHttpClient(httpClient);
        requestFactory.setReadTimeout(10000);
        requestFactory.setConnectTimeout(10000);
        return new RestTemplate(requestFactory);
    }

    private File deleteFile() {
        String fileName = System.getProperty("user.dir") + File.separator + "src" + File.separator + "main" + File.separator + "resources" + File.separator + "testEventListener" + File.separator + "test.txt";
        File file = new File(fileName);

        if (file.exists()) {
            file.delete();
        }
        return file;
    }
}
