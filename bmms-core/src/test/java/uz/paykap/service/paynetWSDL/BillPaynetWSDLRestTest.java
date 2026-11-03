package uz.paykap.service.paynetWSDL;

import org.junit.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import uz.devops.bm.dummy.adapter.DummyDataAdapterV3;
import uz.paykap.adapter.DummyAdapterDelegate;
import uz.paykap.domain.Recipient;
import uz.paykap.repository.RecipientRepository;
import uz.paykap.service.DepositService;
import uz.paykap.web.rest.util.DummyUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/*@EnableAutoConfiguration(exclude = {
    org.springframework.boot.autoconfigure.security.oauth2.OAuth2AutoConfiguration.class
})*/
//@RunWith(SpringRunner.class)
//@SpringBootTest(classes = PkbillmsApp.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class BillPaynetWSDLRestTest {

    @Autowired
    private DummyAdapterDelegate dummyAdapterDelegate;
    @Autowired
    private DepositService depositService;
    @Autowired
    private RecipientRepository recipientRepository;

    @LocalServerPort
    int randomServerPort;

    @Before
    public void initTest() {
        DummyUtils.prepareDummyData(dummyAdapterDelegate);
    }

    private String billId;
    private final String DUMMY_RECIPIENT_ID = "1151";

    //    @Test
//    @WithMockUser(username = DummyDataAdapterV3.DUMMY_AGENT)
//    @Transactional
    public void getInformationRequest() {

        depositService.increaseAgentDeposit(DummyDataAdapterV3.DUMMY_AGENT, 10000000L);

        Optional<Recipient> recipient = recipientRepository.findTop1ByShortName(DummyDataAdapterV3.DUMMY_RECIPIENT_USD);

        assertThat(recipient.isPresent()).isTrue();
        Long recipientId = recipient.get().getId();

        String message = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\">\n" +
            "<soapenv:Body>\n" +
            "<ns1:GetInformationArguments xmlns:ns1=\"http://uws.provider.com/\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:type=\"ns1: GetInformationArguments\">\n" +
            "<password>REDACTED</password>\n" +
            "<username>" + DummyDataAdapterV3.DUMMY_AGENT + "</username>\n" +
            "<parameters>\n" +
            "<paramKey>account</paramKey>\n" +
            "<paramValue>634247</paramValue>\n" +
            "</parameters>\n" +
            "<serviceId>" + recipientId + "</serviceId>\n" +
            "</ns1:GetInformationArguments>\n" +
            "</soapenv:Body>\n" +
            "</soapenv:Envelope>";

        MultiValueMap<String, String> map = new HttpHeaders();
        map.set("Content-Type", "text/xml");
        HttpEntity<String> httpEntity = new HttpEntity<>(message, map);
        RestTemplate restTemplate = new RestTemplate();
        String response = restTemplate.postForObject("http://localhost:" + randomServerPort + "/ws", httpEntity, String.class);
        assert response != null;
        billId = response.substring(306, 310);
        System.out.println(response);
    }

    //    @Test
//    @WithMockUser(username = DummyDataAdapterV3.DUMMY_AGENT)
//    @Transactional
    public void performTransactionRequest() {
//        getInformationRequest();
        String message = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\">\n" +
            "<soapenv:Body>\n" +
            "<ns1:PerformTransactionArguments xmlns:ns1=\"http://uws.provider.com/\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:type=\"ns1:PerformTransactionArguments\">\n" +
            "<password>REDACTED</password>\n" +
            "<username>dummy-agent</username>\n" +
            "<amount>150000</amount>\n" +
            "<parameters>\n" +
            "<paramKey>account</paramKey>\n" +
            "<paramValue>1234567</paramValue>\n" +
            "</parameters>\n" +
            "<serviceId>" + DUMMY_RECIPIENT_ID + "</serviceId>\n" +
            "<transactionId>123</transactionId>\n" +
            "<transactionTime>2011-04-26T18:07:22</transactionTime>\n" +
            "</ns1:PerformTransactionArguments>\n" +
            "</soapenv:Body>\n" +
            "</soapenv:Envelope>";

        MultiValueMap<String, String> map = new HttpHeaders();
        map.set("Content-Type", "text/xml");
        HttpEntity<String> httpEntity = new HttpEntity<>(message, map);
        RestTemplate restTemplate = new RestTemplate();
        String response = restTemplate.postForObject("http://localhost:" + randomServerPort + "/ws", httpEntity, String.class);

        System.out.println(response);
    }

    //    @Test
//    @WithMockUser(username = DummyDataAdapterV3.DUMMY_AGENT)
//    @Transactional
    public void checkTransactionRequest() {
        performTransactionRequest();
        String message = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\">\n" +
            "<soapenv:Body><ns1:CheckTransactionArguments xmlns:ns1=\"http://uws.provider.com/\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:type=\"ns1: CheckTransactionArguments\">\n" +
            "<password>REDACTED</password>\n" +
            "<username>dummy-agent</username>\n" +
            "<serviceId>" + DUMMY_RECIPIENT_ID + "</serviceId>\n" +
            "<transactionId>" + billId + "</transactionId>\n" +
            "<transactionTime>2011-04-26T18:07:22</transactionTime>\n" +
            "<parameters>\n" +
            "<paramKey>reason</paramKey>\n" +
            "<paramValue></paramValue>\n" +
            "</parameters>\n" +
            "</ns1:CheckTransactionArguments>\n" +
            "</soapenv:Body>\n" +
            "</soapenv:Envelope>";

        MultiValueMap<String, String> map = new HttpHeaders();
        map.set("Content-Type", "text/xml");
        HttpEntity<String> httpEntity = new HttpEntity<>(message, map);
        RestTemplate restTemplate = new RestTemplate();
        String response = restTemplate.postForObject("http://localhost:" + randomServerPort + "/ws", httpEntity, String.class);

        System.out.println(response);
    }

    //    @Test
//    @WithMockUser(username = DummyDataAdapterV3.DUMMY_AGENT)
//    @Transactional
    public void getStatementRequest() {

        performTransactionRequest();

        String message = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\">\n" +
            "<soapenv:Body>\n" +
            "<ns1:GetStatementArguments xmlns:ns1=\"http://uws.provider.com/\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:type=\"ns1: GetStatementArguments\">\n" +
            "<password>REDACTED</password>\n" +
            "<username>dummy-agent</username>\n" +
            "<dateFrom>2018-10-09T08:00:00</dateFrom>\n" +
            "<dateTo>2018-10-29T08:00:00</dateTo>\n" +
            "<serviceId>" + DUMMY_RECIPIENT_ID + "</serviceId>\n" +
            "<onlyTransactionId>true</onlyTransactionId><parameters>\n" +
            "<paramKey>reason</paramKey>\n" +
            "<paramValue></paramValue>\n" +
            "</parameters>\n" +
            "</ns1:GetStatementArguments>\n" +
            "</soapenv:Body>\n" +
            "</soapenv:Envelope>";

        MultiValueMap<String, String> map = new HttpHeaders();
        map.set("Content-Type", "text/xml");
        HttpEntity<String> httpEntity = new HttpEntity<>(message, map);
        RestTemplate restTemplate = new RestTemplate();
        String response = restTemplate.postForObject("http://localhost:" + randomServerPort + "/ws", httpEntity, String.class);

        System.out.println(response);
    }

}
