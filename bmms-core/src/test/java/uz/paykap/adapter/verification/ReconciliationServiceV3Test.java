package uz.paykap.adapter.verification;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;
import uz.devops.bm.paynet.config.PaynetConstants;
import uz.devops.bmcore.enumeration.TransactionStatus;
import uz.javlon.commons.base.Result;
import uz.paykap.PkbillmsApp;
import uz.paykap.adapter.DataAdapterServiceV3;
import uz.paykap.config.Constants;
import uz.paykap.config.TestSecurityConfiguration;
import uz.paykap.domain.Transaction;
import uz.paykap.repository.TransactionRepository;
import uz.paykap.service.P2pCheckRequestDecoratorService;
import uz.paykap.service.dto.p2p.P2pAccount;
import uz.paykap.service.dto.p2p.P2pAccountType;
import uz.paykap.service.dto.p2p.P2pPayRequest;
import uz.paykap.service.p2p.dto.response.P2pResponse;
import uz.paykap.utils.DateUtil;
import uz.paykap.web.rest.TransactionResourceIntTest;

import java.io.IOException;
import java.time.temporal.ChronoUnit;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest(
        classes = {PkbillmsApp.class, TestSecurityConfiguration.class},
        properties = {"bm-paynet.dataAdapterImpl=RPCv4", "bm-paynet.gatewayServiceImpl=RPCv5"/*, "bm-paynet.paynetJsonPath=paynet.json"*/}
)
public class ReconciliationServiceV3Test {

    @Autowired
    private ReconciliationServiceV3 reconciliationServiceV3;
    @Autowired
    private TransactionRepository transactionRepository;
    @Autowired
    private DataAdapterServiceV3 dataAdapterServiceV3;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private P2pCheckRequestDecoratorService p2pCheckRequestDecoratorService;

    @Before
    public void before() {

        dataAdapterServiceV3.sync(PaynetConstants.PAYNET_RPC_DATA_ADAPTER);

        Transaction transaction = TransactionResourceIntTest.createEntity(null);
        transaction.setStatus(TransactionStatus.SUCCESS);
        transaction.requestGroup(Constants.WMAGENT_LOGIN);
        transaction.requestLogin(Constants.WMAGENT_LOGIN);
        transaction.setRecipientGateway(PaynetConstants.PAYNET_RPC_GATEWAY_NAME);
        transaction.setGatewayAmount(3250000d);
        transaction.setRecipientAmount(3250000L);
        transaction.setRequestAmount(3250000L);
        transaction.setDate(DateUtil.todayStart().minus(1, ChronoUnit.DAYS).plusSeconds(5000));
        transaction.setRecipientDate(transaction.getDate());

        transactionRepository.save(transaction);

        Transaction transactionMehrli = TransactionResourceIntTest.createEntity(null);
        transactionMehrli.setId(144756419L);
        transactionMehrli.setStatus(TransactionStatus.SUCCESS);
        transactionMehrli.requestGroup(Constants.WMAGENT_LOGIN);
        transactionMehrli.requestLogin(Constants.WMAGENT_LOGIN);
        transactionMehrli.setRecipientGateway(PaynetConstants.PAYNET_RPC_GATEWAY_NAME);
        transactionMehrli.setGatewayAmount(2126d);
        transactionMehrli.setRecipientAmount(2126L);
        transactionMehrli.setRequestAmount(2126L);
        transactionMehrli.setDate(DateUtil.todayStart().minus(1, ChronoUnit.DAYS).plusSeconds(6000));
        transactionMehrli.setRecipientDate(transaction.getDate());
        transactionMehrli.setRecipientName("mehrli");

        Transaction transaction1 = transactionRepository.save(transactionMehrli);
    }

    @Test
    @Transactional
    public void testStartReconcileByGateway() {

        List<Transaction> all = transactionRepository.findAll();

        Result result = reconciliationServiceV3.startReconcileByGateway(DateUtil.getPrevDayBegin(), DateUtil.getPrevDayEnd());

        Assertions.assertThat(result).isNotNull();
        Assertions.assertThat(result.isSuccess()).isTrue();
    }

    @Test
    @Transactional
    public void testStartReconcileByP2p() {
        Result result = reconciliationServiceV3.startReconcileByP2p(DateUtil.getPrevDayBegin(), DateUtil.getPrevDayEnd());
        Assertions.assertThat(result).isNotNull();
        Assertions.assertThat(result.isSuccess()).isTrue();
    }


    @Test
    @Transactional
    @WithMockUser("JX")
    public void doHumoTransaction() throws IOException {


        var paramsString = "{ \"pan\": \"9860090000000000\", \n" +
                "\"exp\": \"1223\"}";

        var senderAccount = new P2pAccount()
                .withType(P2pAccountType.PAN)
                .withCurrency(860)
                .withParams(objectMapper.readTree(paramsString));

        var paramsString2 = "{ \"pan\": \"9860090000000000\", \n" +
                "\"exp\": \"1223\" }";

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

        Assertions.assertThat(simpleBillResponse).isNotNull();
        Assertions.assertThat(simpleBillResponse.isSuccess()).isTrue();
        Assertions.assertThat(simpleBillResponse.getSender()).isNotNull();
        Assertions.assertThat(simpleBillResponse.getRecipient()).isNotNull();
        Assertions.assertThat(simpleBillResponse.getStatusMessage()).isEqualTo("SUCCESS");
        Assertions.assertThat(simpleBillResponse.getRate()).isNotNull();
        Assertions.assertThat(simpleBillResponse.getCurrency()).isEqualTo(860);

    }
}
