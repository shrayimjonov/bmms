package uz.paykap.gateway.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.hamcrest.Matchers;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.client.ExpectedCount;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.client.match.MockRestRequestMatchers;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import uz.devops.bmcore.dto.request.CheckRequest;
import uz.devops.bmcore.dto.request.CommonPayRequest;
import uz.devops.bmcore.dto.request.PayRequest;
import uz.devops.bmcore.dto.response.SimpleBillResponse;
import uz.devops.bmcore.enumeration.RequestType;
import uz.devops.humo.config.HumoConfiguration;
import uz.javlon.svgate.service.simulation.PredefinedCardRepo2;
import uz.paykap.PkbillmsApp;
import uz.paykap.agent.tran.CommonTranServiceV3;
import uz.paykap.config.Constants;
import uz.paykap.config.DynamicGatewayServiceConfig;
import uz.paykap.config.SecurityBeanOverrideConfiguration;
import uz.paykap.domain.Bill;
import uz.paykap.domain.Gateway;
import uz.paykap.domain.GatewayParam;
import uz.paykap.domain.Recipient;
import uz.paykap.domain.enumeration.MethodType;
import uz.paykap.repository.BillRepository;
import uz.paykap.repository.GatewayParamRepository;
import uz.paykap.repository.GatewayRepository;
import uz.paykap.repository.RecipientRepository;
import uz.paykap.service.AgentRecipientService;
import uz.paykap.service.AgentService;
import uz.paykap.service.P2pCheckRequestDecoratorService;
import uz.paykap.service.dto.AgentDTO;
import uz.paykap.service.dto.p2p.P2pAccount;
import uz.paykap.service.dto.p2p.P2pAccountType;
import uz.paykap.service.dto.p2p.P2pPayRequest;
import uz.paykap.service.p2p.dto.response.P2pResponse;
import uz.paykap.web.rest.GatewayParamResourceIntTest;
import uz.paykap.web.rest.GatewayResourceIntTest;
import uz.paykap.web.rest.RecipientResourceIntTest;

import javax.persistence.EntityManager;
import java.net.SocketTimeoutException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {PkbillmsApp.class, SecurityBeanOverrideConfiguration.class}, properties = {"humo.simulate=false", })
public class P2pServiceTimeoutTest {

    @Autowired
    @Qualifier("humoMobileRestTemplate")
    private RestTemplate restTemplate;

    private MockRestServiceServer mockServer;

    @Autowired
    private RecipientRepository recipientRepository;
    @Autowired
    private GatewayParamRepository gatewayParamRepository;
    @Autowired
    private GatewayRepository gatewayRepository;
    @Autowired
    private BillRepository billRepository;
    @Autowired
    private CommonTranServiceV3 commonTranServiceV3;
    @Autowired
    private AgentService agentService;
    @Autowired
    private AgentRecipientService agentRecipientService;
    @Autowired
    private EntityManager entityManager;
    @Autowired
    private P2pCheckRequestDecoratorService p2pCheckRequestDecoratorService;

    @Autowired
    private ObjectMapper objectMapper;

    @SneakyThrows
    @Test
    @WithMockUser(username = Constants.SYSTEM_AGENT_NAME)
    @Transactional
    public void testP2pTimeout() {

        /* p2p-param-uzcard-kapital-humo-aab.json */

        mockServer = MockRestServiceServer.createServer(restTemplate);

        mockServer
                .expect(
                        ExpectedCount.once(),
                        MockRestRequestMatchers.requestTo(Matchers.anything())
                )
                .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
                .andRespond(
                        (response) -> {
                            throw new ResourceAccessException("Connection reset", new SocketTimeoutException("Connection timeout"));
                        }
                );

        var paramsString = "{ \"pan\": \"" + PredefinedCardRepo2.REAL_PAN + "\" }";

        var senderAccount = new P2pAccount()
                .withType(P2pAccountType.PAN)
                .withCurrency(860)
                .withParams(objectMapper.readTree(paramsString));

        var paramsString2 = "{ \"pan\": \"9860090000000000\", \"bin\": \"986009\" }";

        var recipientAccount = new P2pAccount()
                .withType(P2pAccountType.PAN)
                .withCurrency(860)
                .withParams(objectMapper.readTree(paramsString2));

        P2pPayRequest p2PPayRequest = new P2pPayRequest()
                .withFrom(senderAccount)
                .withTo(recipientAccount)
                .withAgentTranId("123")
                .withAmount(10000L);

        P2pResponse simpleBillResponse = p2pCheckRequestDecoratorService.p2pPay(p2PPayRequest);

        log.info(String.valueOf(simpleBillResponse));

        //        Assertions.assertThat(response).isNotNull();
        //        Assertions.assertThat(response.getStatusCode()).isNotNull();
        //        Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        Assertions.assertThat(simpleBillResponse).isNotNull();
        Assertions.assertThat(simpleBillResponse.isSuccess()).isTrue();

    }

    @Ignore("Only with online mock")
    @Test
    @WithMockUser(username = Constants.SYSTEM_AGENT_NAME)
    @Transactional
    public void payCommonV3FromBeeceptor() {

//        mockServer = MockRestServiceServer.createServer(restTemplate);

        final Gateway gateway = GatewayResourceIntTest.createEntity(entityManager);
        gateway.setSpringBeanName(CommonGatewayServiceV3.NAME);
        gateway.active(Boolean.TRUE);
        gatewayRepository.save(gateway);

        final Recipient recipient = RecipientResourceIntTest.createEntity(entityManager);
        recipient.active(Boolean.TRUE);
        recipient.pay(Boolean.TRUE);
        recipient.maxAmount(1000000L);
        recipient.minAmount(100L);
        recipient.gateway(gateway);
        recipientRepository.save(recipient);

        final GatewayParam gatewayParam = GatewayParamResourceIntTest.createEntity(entityManager);
        gatewayParam.active(Boolean.TRUE);
        gatewayParam.type(RequestType.PAY);
        gatewayParam.setHttpMethodType(MethodType.GET);
        gatewayParam.setUrl("https://javasparx.free.beeceptor.com/test");
        gatewayParamRepository.save(gatewayParam);

        AgentDTO agent = agentService.getAgentOrCreate(Constants.SYSTEM_AGENT_NAME, 10000000);
        agent.setActive(Boolean.TRUE);
        agentRecipientService.createOrGet(recipient.getId(), agent);

//        mockServer
//                .expect(
//                        ExpectedCount.once(),
//                        MockRestRequestMatchers.requestTo(Matchers.allOf(
//                                Matchers.matchesPattern(gatewayParam.getUrl() + ".*")
////                                ,
////                                Matchers.matchesPattern(".*TERMINAL_ID.*"),
////                                Matchers.matchesPattern(".*USERNAME.*"),
////                                Matchers.matchesPattern(".*ACT=2.*"),
////                                Matchers.matchesPattern(".*SERVICE_ID.*"),
////                                Matchers.matchesPattern(".*PASSWORD.*")
//                        ))
//                )
//                .andExpect(MockRestRequestMatchers.method(HttpMethod.GET))
//                .andRespond(
//                        (response) -> {
//                            throw new ResourceAccessException("Connection reset", new SocketTimeoutException("Connection timeout"));
//                        }
//                );

        gatewayParam.setActive(Boolean.TRUE);
        gatewayParam.setRecipient(recipient);

//        adapterServiceV3.sync(CommonDataAdapterV3.NAME);

//        Resource[] resources = loadRecipientResources();

//        for (Resource resource : resources) {
//            String recipientShortName = FilenameUtils.getBaseName(resource.getFilename());

//            System.out.println("\n\n##################\n" + recipientShortName + "\n##################\n");

//            SimulationBase simulationBase = loadRecipientResources(recipientShortName);

//        Recipient recipient = recipientRepository.findTop1ByShortName(recipientShortName).orElseThrow(() -> new CodedException(ProblemEnum.SERVICE_NOT_FOUND));

        //CHECK

        CheckRequest checkDTO = new CheckRequest();
        checkDTO.setRecipientId(recipient.getId());
        checkDTO.setAmount(10000L);
        checkDTO.setAccount("TEST_ACCOUNT");

        int billsSize = billRepository.findAll().size();

        SimpleBillResponse checkResponse = commonTranServiceV3.check(checkDTO);

        shouldBeValid(billsSize, recipient, checkResponse, RequestType.CHECK, checkDTO);

        //PAY

        CommonPayRequest payDTO = new PayRequest();
//            payDTO.setAmount(simulationBase.getCheck().getAmount());
        payDTO.setBillId(checkResponse.getId());

        billsSize = billRepository.findAll().size();

        System.out.println("Testing pay : " + recipient.getShortName() + " : " + payDTO);

        SimpleBillResponse result = commonTranServiceV3.pay(payDTO);

        shouldBeValid(billsSize, recipient, result, RequestType.PAY, checkDTO);
//        }
    }

    private void shouldBeValid(int billSize, Recipient recipient, SimpleBillResponse simpleBillResponse, RequestType requestType, CheckRequest checkDTO) {
        List<Bill> billList = billRepository.findAll();
        assertThat(billList).hasSize(billSize + 1);

        Bill testBill = billList.get(billList.size() - 1);
        assertThat(testBill.getRecipientId()).isEqualTo(recipient.getId());
        assertThat(testBill.getRecipientShortName()).isEqualTo(recipient.getShortName());
//        assertThat(testBill.getSessionId()).isEqualTo(DEFAULT_SESSION_ID);
        assertThat(testBill.getType()).isEqualTo(requestType);
        assertThat(testBill.getAmountRatio()).isEqualTo(recipient.getAmountRatio());
        assertThat(testBill.getAmount()).isEqualTo(checkDTO.getAmount() / 100 * recipient.getAmountRatio());
        assertThat(testBill.getAccount()).isEqualTo(checkDTO.getAccount());
//        assertThat(testBill.getProfit()).isEqualTo(0);
//        assertThat(testBill.getParams()).isEqualTo(DEFAULT_PARAMS);
        assertThat(testBill.getCurrency()).isEqualTo(checkDTO.getCurrency());
//        assertThat(testBill.getExtId()).isEqualTo(DEFAULT_EXT_ID);
//        assertThat(testBill.getCheckId()).isEqualTo(DEFAULT_CHECK_ID);
//        assertThat(testBill.getPayId()).isEqualTo(DEFAULT_PAY_ID);
        assertThat(testBill.getRequestTime()).isNotNull();
        assertThat(testBill.getRequestJson()).isNotNull();
        assertThat(testBill.getResponseJson()).isNotNull();
        assertThat(testBill.getResponseTime()).isNotNull();
        assertThat(testBill.getResponseCode()).isNotNull();
        assertThat(testBill.getResponseText()).isNotNull();
        assertThat(testBill.isSuccess()).isEqualTo(testBill.getType() == RequestType.CHECK);
        assertThat(testBill.getLogin()).isEqualTo(Constants.SYSTEM_AGENT_NAME);
//        assertThat(testBill.getFraudId()).isNotNull();

        //Validate simpleBillResponse
        assertThat(simpleBillResponse.getRecipientShortName()).isEqualTo(recipient.getShortName());
        assertThat(simpleBillResponse.getAmountRatio()).isEqualTo(recipient.getAmountRatio());
        assertThat(simpleBillResponse.getAmount()).isEqualTo(checkDTO.getAmount());
        assertThat(simpleBillResponse.getAccount()).isEqualTo(checkDTO.getAccount());
        assertThat(simpleBillResponse.getCurrency()).isEqualTo(checkDTO.getCurrency());
        assertThat(simpleBillResponse.getRequestTime()).isNotNull();
        assertThat(simpleBillResponse.getResponseTime()).isNotNull();
        assertThat(simpleBillResponse.getResponseText()).isNotNull();
        assertThat(simpleBillResponse.isSuccess()).isEqualTo(testBill.getType() == RequestType.CHECK);
    }

}
