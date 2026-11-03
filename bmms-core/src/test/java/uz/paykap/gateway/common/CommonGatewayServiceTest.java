package uz.paykap.gateway.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import uz.paykap.IntegrationTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.ResourcePatternUtils;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;
import uz.devops.bmcore.dto.request.CheckRequest;
import uz.devops.bmcore.dto.request.CommonPayRequest;
import uz.devops.bmcore.dto.request.PayRequest;
import uz.devops.bmcore.dto.response.SimpleBillResponse;
import uz.devops.bmcore.enumeration.RequestType;
import uz.javlon.commons.exception.CodedException;
import uz.javlon.commons.exception.ProblemEnum;
import uz.paykap.PkbillmsApp;
import uz.paykap.adapter.DataAdapterServiceV3;
import uz.paykap.adapter.common.CommonDataAdapterV3;
import uz.paykap.agent.tran.CommonTranServiceV3;
import uz.paykap.config.Constants;
import uz.paykap.IntegrationTest;
import uz.paykap.domain.AgentRecipient;
import uz.paykap.domain.Bill;
import uz.paykap.domain.Recipient;
import uz.paykap.gateway.common.simulation.SimulationBase;
import uz.paykap.repository.BillRepository;
import uz.paykap.repository.RecipientRepository;
import uz.paykap.service.AgentRecipientService;
import uz.paykap.service.AgentService;
import uz.paykap.service.dto.AgentDTO;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@IntegrationTest
public class CommonGatewayServiceTest {

    public static final String COMMON_RECIPIENTS_FOLDER = "simulation/";
    public static final String TEMP = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:uws=\"http://uws.provider.com/\">\n" +
        "     <soapenv:Header/>\n" +
        "     <soapenv:Body>\n" +
        "          <uws:GetStatementArguments>\n" +
        "               <password>PASSWORD</password>\n" +
        "               <username>USER_NAME</username>\n" +
        "               <dateFrom>PERIOD_FROM</dateFrom>\n" +
        "               <dateTo>PERIOD_TO</dateTo>\n" +
        "               <serviceId>1</serviceId>\n" +
        "               <onlyTransactionId>ONLY_TRANSACTION_ID</onlyTransactionId>\n" +
        "          </uws:GetStatementArguments>\n" +
        "     </soapenv:Body>\n" +
        "</soapenv:Envelope>\n";

    @Autowired
    private RecipientRepository recipientRepository;
    @Autowired
    private BillRepository billRepository;
    @Autowired
    private CommonTranServiceV3 commonTranServiceV3;
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

    @Test
    @WithMockUser(username = Constants.SYSTEM_AGENT_NAME)
    @Transactional
    public void payCommonV3() {

        adapterServiceV3.sync(CommonDataAdapterV3.NAME);

        Resource[] resources = loadRecipientResources();

        for (Resource resource : resources) {
            String recipientShortName = FilenameUtils.getBaseName(resource.getFilename());

            System.out.println("\n\n##################\n" + recipientShortName + "\n##################\n");

            SimulationBase simulationBase = loadRecipientResources(recipientShortName);

            Recipient recipient = recipientRepository.findTop1ByShortName(recipientShortName).orElseThrow(() -> new CodedException(ProblemEnum.SERVICE_NOT_FOUND));

            AgentDTO agent = agentService.getAgentOrCreate(Constants.SYSTEM_AGENT_NAME, 10000000);

            agentRecipientService.createOrGet(recipient.getId(), agent);

            //CHECK

            CheckRequest checkDTO = new CheckRequest();
            checkDTO.setRecipientId(recipient.getId());
            checkDTO.setAmount(simulationBase.getCheck().getAmount());
            checkDTO.setAccount(simulationBase.getCheck().getAccount());

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
        }
    }

    private void shouldBeValid(int billSize, Recipient recipient, SimpleBillResponse checkResponse, RequestType requestType, CheckRequest checkDTO) {
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
        assertThat(testBill.isSuccess()).isEqualTo(true);
        assertThat(testBill.getLogin()).isEqualTo(Constants.SYSTEM_AGENT_NAME);
//        assertThat(testBill.getFraudId()).isNotNull();

        //Validate checkResponse
        assertThat(checkResponse.getRecipientShortName()).isEqualTo(recipient.getShortName());
        assertThat(checkResponse.getAmountRatio()).isEqualTo(recipient.getAmountRatio());
        assertThat(checkResponse.getAmount()).isEqualTo(checkDTO.getAmount());
        assertThat(checkResponse.getAccount()).isEqualTo(checkDTO.getAccount());
        assertThat(checkResponse.getCurrency()).isEqualTo(checkDTO.getCurrency());
        assertThat(checkResponse.getRequestTime()).isNotNull();
        assertThat(checkResponse.getResponseTime()).isNotNull();
        assertThat(checkResponse.getResponseText()).isNotNull();
        assertThat(checkResponse.isSuccess()).isEqualTo(true);
    }

    private Resource[] loadRecipientResources(/*String pattern*/) {
        try {
            return ResourcePatternUtils.getResourcePatternResolver(resourceLoader).getResources("classpath*:simulation/*.json");
        } catch (IOException e) {
            return new Resource[]{};
        }
    }

    private SimulationBase loadRecipientResources(String fileName) {
        try {
            return objectMapper.readValue(IOUtils.toString(new ClassPathResource(COMMON_RECIPIENTS_FOLDER + fileName + ".json").getInputStream(), StandardCharsets.UTF_8), SimulationBase.class);
        } catch (IOException e) {
            throw new CodedException(ProblemEnum.OBJECT_NOT_FOUND);
        }
    }

}
