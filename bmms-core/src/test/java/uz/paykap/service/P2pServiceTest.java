package uz.paykap.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.integration.xml.xpath.XPathUtils;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;
import uz.devops.bm.dummy.adapter.DummyDataAdapterV3;
import uz.devops.bmcore.enumeration.RequestType;
import uz.devops.bmcore.enumeration.TransactionStatus;
import uz.devops.humo.service.dto.v3.getpayment.GetPaymentStatusCodes;
import uz.javlon.svgate.domain.Bank;
import uz.javlon.svgate.dto.sv.Sender;
import uz.javlon.svgate.repository.BankRepository;
import uz.javlon.svgate.service.simulation.PredefinedCardRepo2;
import uz.paykap.PkbillmsApp;
import uz.paykap.adapter.DataAdapterServiceV3;
import uz.paykap.adapter.humo.HumoCreditDataAdapterV3;
import uz.paykap.adapter.verification.AutomaticReconcileService;
import uz.paykap.agent.tran.p2p.P2pHumoPaymentService;
import uz.paykap.config.Constants;
import uz.paykap.config.TestSecurityConfiguration;
import uz.paykap.domain.*;
import uz.paykap.domain.enumeration.DepositState;
import uz.paykap.domain.enumeration.P2pProcessorType;
import uz.paykap.gateway.humo.HumoCreditGatewayServiceV3;
import uz.paykap.repository.*;
import uz.paykap.service.dto.AgentDTO;
import uz.paykap.service.dto.SettingDTO;
import uz.paykap.service.dto.p2p.*;
import uz.paykap.service.dto.p2p.promo.Promo;
import uz.paykap.service.dto.result.BmResult;
import uz.paykap.service.humo.HumoP2pHelperService;
import uz.paykap.service.p2p.dto.response.P2pResponse;
import uz.paykap.service.p2p.register.P2pRegistry;
import uz.paykap.utils.PcUtil;
import uz.paykap.web.rest.AgentResourceIntTest;
import uz.paykap.web.rest.P2pAgentResource;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@ActiveProfiles(profiles = {"davr", "default"})
@RunWith(SpringRunner.class)
@SpringBootTest(
        classes = {PkbillmsApp.class, TestSecurityConfiguration.class},
        properties = {"rateService.impl=davrRateService", "nci-is-api.simulate=true", "application.reconciliation.enableAuto=true"}
)
public class P2pServiceTest {

    public static final String AGENT_TRAN_ID = "123";
    public static final String VISA_PAN = "4909570000000000";
    public static final long INFO_AMOUNT = 100L;
    public static final String TESHABOY_BOLTAYEV = "TESHABOY BOLTAYEV";
    public static final String JAVLON_ERALIYEV_COO = "TOSHMAT ESHMATOV COO";
    public static final String PROMO_CODE = "5mln";
    public static final String HUMO_PAN_ACTION_CODE_1 = "9860090000000000";

    @Autowired
    private P2pAgentResource p2pAgentResource;

    @Autowired
    private DataAdapterServiceV3 dataAdapterServiceV3;

    @Autowired
    private RecipientRepository recipientRepository;

    @Autowired
    private BankRepository bankRepository;

    @Autowired
    private AgentRecipientRepository agentRecipientRepository;

    @Autowired
    private P2pRegistry p2PRegistry;

    @Autowired
    private P2pCheckRequestDecoratorService p2pCheckRequestDecoratorService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private P2pCommissionRepository p2pCommissionRepository;

    @Autowired
    private P2pConditionRepository p2pConditionRepository;

    @Autowired
    private P2pParamRepository p2pParamRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private AutomaticReconcileService automaticReconcileService;

    @Autowired
    private HumoP2pHelperService humoP2pHelperService;
    @Autowired
    private ObjectMapperUtils objectMapperUtils;

    @Autowired
    private BillRepository billRepository;
    @Autowired
    private AgentRepository agentRepository;
    @Autowired
    private DepositRepository depositRepository;

    @SneakyThrows
    @Test
    @Transactional
    @WithMockUser(DummyDataAdapterV3.DUMMY_AGENT)
    public void testFullNameParser() {
        Sender sender = new Sender().withFullName("TESTOV TEST");

        Assertions.assertThat(sender.getLastName()).isNotNull();
        Assertions.assertThat(sender.getMiddleName()).isNotNull();
        Assertions.assertThat(sender.getLastName()).isNotNull();

        sender = new Sender().withFullName("TESTOV");

        Assertions.assertThat(sender.getLastName()).isNotNull();
        Assertions.assertThat(sender.getMiddleName()).isNotNull();
        Assertions.assertThat(sender.getLastName()).isNotNull();

        sender = new Sender().withFullName("");

        Assertions.assertThat(sender.getLastName()).isNotNull();
        Assertions.assertThat(sender.getMiddleName()).isNotNull();
        Assertions.assertThat(sender.getLastName()).isNotNull();

        sender = new Sender().withFullName(null);

        Assertions.assertThat(sender.getLastName()).isNotNull();
        Assertions.assertThat(sender.getMiddleName()).isNotNull();
        Assertions.assertThat(sender.getLastName()).isNotNull();
    }

    @SneakyThrows
    @Test
    @Transactional
    @WithMockUser(DummyDataAdapterV3.DUMMY_AGENT)
    public void payUzcardPan2Pan() {
        /* p2p-param-uzcard-foreign-foreign.json */

        int databaseSizeBeforeCreate = billRepository.findAll().size();

        var paramsString = "{ \"pan\": \"" + PredefinedCardRepo2.REAL_PAN + "\", \"phone\": \"998901234567\" }";

        var senderAccount = new P2pAccount().withType(P2pAccountType.PAN).withCurrency(860).withParams(objectMapper.readTree(paramsString));

        var paramsString2 = "{ \"pan\": \"" + PredefinedCardRepo2.REAL_PAN + "\" }";

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

        // Validate the Bill in the database
        List<Bill> billList = billRepository.findAll();
        assertThat(billList).hasSize(databaseSizeBeforeCreate + 2); // CHECK (getInfo) -> UNIVERSAL
        //        Bill testBill = billList.get(billList.size() - 2);
    }

    @SneakyThrows
    @Test
    @Transactional
    @WithMockUser(DummyDataAdapterV3.DUMMY_AGENT)
    public void cancelUzcardPan2Pan() {
        int databaseSizeBeforeCreate = billRepository.findAll().size();

        var paramsString = "{ \"pan\": \"" + PredefinedCardRepo2.REAL_PAN + "\" }";

        var senderAccount = new P2pAccount().withCurrency(860).withType(P2pAccountType.PAN).withParams(objectMapper.readTree(paramsString));

        var paramsString2 = "{ \"pan\": \"" + PredefinedCardRepo2.REAL_PAN + "\" }";

        var recipientAccount = new P2pAccount()
                .withCurrency(860)
                .withType(P2pAccountType.PAN)
                .withParams(objectMapper.readTree(paramsString2));

        P2pPayRequest p2PPayRequest = new P2pPayRequest()
                .withTo(recipientAccount)
                .withFrom(senderAccount)
                .withAgentTranId(AGENT_TRAN_ID)
                .withAmount(10000L);

        P2pResponse simpleBillResponse = p2pCheckRequestDecoratorService.p2pPay(p2PPayRequest);

        log.info(String.valueOf(simpleBillResponse));

        Assertions.assertThat(simpleBillResponse).isNotNull();
        Assertions.assertThat(simpleBillResponse.isSuccess()).isTrue();

        // Validate the Bill in the database
        List<Bill> billList = billRepository.findAll();
        assertThat(billList).hasSize(databaseSizeBeforeCreate + 2); // CHECK (getInfo) -> UNIVERSAL

        P2pCancelRequest p2pCancelRequest = new P2pCancelRequest();
        p2pCancelRequest.setAgentTranId(AGENT_TRAN_ID);

        P2pResponse p2pResponse = p2pCheckRequestDecoratorService.p2pCancel(p2pCancelRequest);
        Assertions.assertThat(p2pResponse).isNotNull();
        Assertions.assertThat(p2pResponse.isSuccess()).isTrue();

        // Validate the Bill in the database
        billList = billRepository.findAll();
        assertThat(billList).hasSize(databaseSizeBeforeCreate + 4); // CANCEL (uzcard-debit-reverse) -> CANCEL (uzcard-debit-reverse)
    }

    @SneakyThrows
    @Test
    @Transactional
    @WithMockUser(DummyDataAdapterV3.DUMMY_AGENT)
    public void payUzcardToken2PanShouldFail() {
        int databaseSizeBeforeCreate = billRepository.findAll().size();

        // sender bin not set
        var paramsString = "{ \"token\": \"" + PredefinedCardRepo2.DEFAULT_TOKEN + "\" }";

        var senderAccount = new P2pAccount()
                .withType(P2pAccountType.TOKEN)
                .withCurrency(860)
                .withParams(objectMapper.readTree(paramsString));

        var paramsString2 = "{ \"pan\": \"" + PredefinedCardRepo2.REAL_PAN + "\" }";

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
        Assertions.assertThat(simpleBillResponse.getStatusCode()).isEqualTo(BmResultStatus.INVALID_SENDER_PARAMS.getCode());
        Assertions.assertThat(simpleBillResponse).isNotNull();
        Assertions.assertThat(simpleBillResponse.isSuccess()).isFalse();

        // Validate the Bill in the database
        List<Bill> billList = billRepository.findAll();
        assertThat(billList).hasSize(databaseSizeBeforeCreate + 0); // ERROR (no bill)
    }

    @SneakyThrows
    @Test
    @Transactional
    @WithMockUser(DummyDataAdapterV3.DUMMY_AGENT)
    public void payHumoToken2HumoPanFailAndReverse() {
        int databaseSizeBeforeCreate = billRepository.findAll().size();

        // sender bin not set
        var paramsString = "{ \"token\": \"986009ZZZZZZ9999\", \"bin\": \"986009\" }";

        var senderAccount = new P2pAccount()
                .withType(P2pAccountType.TOKEN)
                .withCurrency(860)
                .withParams(objectMapper.readTree(paramsString));

        var paramsString2 = "{ \"pan\": \"" + HUMO_PAN_ACTION_CODE_1 + "\", \"bin\": \"986009\" }";

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
        //        Assertions.assertThat(simpleBillResponse.getStatusCode()).isEqualTo(BmResultStatus.INVALID_SENDER_PARAMS.getCode());
        Assertions.assertThat(simpleBillResponse).isNotNull();
        Assertions.assertThat(simpleBillResponse.isSuccess()).isFalse();

        // Validate the Bill in the database
        List<Bill> billList = billRepository.findAll();
        assertThat(billList).hasSize(databaseSizeBeforeCreate + 2);

        // CHECK (getInfo) -> P2P_CREDIT (DIRECT_P2P) (error)
    }

    @SneakyThrows
    @Test
    @Transactional
    @WithMockUser(DummyDataAdapterV3.DUMMY_AGENT)
    public void payUzcardToken2Pan() {
        int databaseSizeBeforeCreate = billRepository.findAll().size();

        // bin not set
        var paramsString = "{ \"token\": \"" + PredefinedCardRepo2.DEFAULT_TOKEN + "\", \"bin\": \"860009\" }";

        var senderAccount = new P2pAccount()
                .withType(P2pAccountType.TOKEN)
                .withCurrency(860)
                .withParams(objectMapper.readTree(paramsString));

        var paramsString2 = "{ \"pan\": \"" + PredefinedCardRepo2.REAL_PAN + "\" }";

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
        Assertions.assertThat(simpleBillResponse.getStatusCode()).isEqualTo(BmResultStatus.SUCCESS.getCode());
        Assertions.assertThat(simpleBillResponse).isNotNull();
        Assertions.assertThat(simpleBillResponse.isSuccess()).isTrue();

        // Validate the Bill in the database
        List<Bill> billList = billRepository.findAll();
        assertThat(billList).hasSize(databaseSizeBeforeCreate + 2);
        // CHECK (getInfo) -> UNIVERSAL

    }

//    @SneakyThrows
//    @Test
//    @Transactional
//    @WithMockUser(DummyDataAdapterV3.DUMMY_AGENT)
//    public void payUzcardToken2HumoToken() {
//        int databaseSizeBeforeCreate = billRepository.findAll().size();
//
//        // bin not set
//        var paramsString = "{ \"token\": \"" + PredefinedCardRepo2.REAL_TOKEN + "\", \"bin\": \"860049\" }";
//
//        var senderAccount = new P2pAccount()
//                .withType(P2pAccountType.TOKEN)
//                .withCurrency(860)
//                .withParams(objectMapper.readTree(paramsString));
//
//        var paramsString2 = "{ \"token\": \"986009ROAZEP6488\", \"bin\": \"986009\" }";
//
//        var recipientAccount = new P2pAccount()
//                .withType(P2pAccountType.TOKEN)
//                .withCurrency(860)
//                .withParams(objectMapper.readTree(paramsString2));
//
//        P2pPayRequest p2PPayRequest = new P2pPayRequest()
//                .withFrom(senderAccount)
//                .withTo(recipientAccount)
//                .withAgentTranId("123")
//                .withAmount(10000L);
//
//        P2pResponse simpleBillResponse = p2pCheckRequestDecoratorService.p2pPay(p2PPayRequest);
//
//        log.info(String.valueOf(simpleBillResponse));
//
//        //        Assertions.assertThat(response).isNotNull();
//        //        Assertions.assertThat(response.getStatusCode()).isNotNull();
//        Assertions.assertThat(simpleBillResponse.getStatusCode()).isEqualTo(BmResultStatus.SUCCESS.getCode());
//        Assertions.assertThat(simpleBillResponse).isNotNull();
//        Assertions.assertThat(simpleBillResponse.isSuccess()).isTrue();
//
//        // Validate the Bill in the database
//        List<Bill> billList = billRepository.findAll();
//        assertThat(billList).hasSize(databaseSizeBeforeCreate + 2);
//        // CHECK (getInfo) -> UNIVERSAL
//
//    }

    @SneakyThrows
    @Test
    @Transactional
    @WithMockUser(DummyDataAdapterV3.DUMMY_AGENT)
    public void cancelUzcardToken2Pan() {
        // bin not set
        var paramsString = "{ \"token\": \"" + PredefinedCardRepo2.DEFAULT_TOKEN + "\", \"bin\": \"860009\" }";

        var senderAccount = new P2pAccount()
                .withType(P2pAccountType.TOKEN)
                .withCurrency(860)
                .withParams(objectMapper.readTree(paramsString));

        var paramsString2 = "{ \"pan\": \"" + PredefinedCardRepo2.REAL_PAN + "\" }";

        var recipientAccount = new P2pAccount()
                .withType(P2pAccountType.PAN)
                .withCurrency(860)
                .withParams(objectMapper.readTree(paramsString2));

        P2pPayRequest p2PPayRequest = new P2pPayRequest()
                .withFrom(senderAccount)
                .withTo(recipientAccount)
                .withAgentTranId(AGENT_TRAN_ID)
                .withAmount(10000L);

        P2pResponse simpleBillResponse = p2pCheckRequestDecoratorService.p2pPay(p2PPayRequest);

        log.info(String.valueOf(simpleBillResponse));

        //        Assertions.assertThat(response).isNotNull();
        //        Assertions.assertThat(response.getStatusCode()).isNotNull();
        Assertions.assertThat(simpleBillResponse.getStatusCode()).isEqualTo(BmResultStatus.SUCCESS.getCode());
        Assertions.assertThat(simpleBillResponse).isNotNull();
        Assertions.assertThat(simpleBillResponse.isSuccess()).isTrue();

        P2pCancelRequest p2pCancelRequest = new P2pCancelRequest();
        p2pCancelRequest.setAgentTranId(AGENT_TRAN_ID);

        P2pResponse p2pResponse = p2pCheckRequestDecoratorService.p2pCancel(p2pCancelRequest);
        Assertions.assertThat(p2pResponse).isNotNull();
        Assertions.assertThat(p2pResponse.isSuccess()).isTrue();
    }

    @SneakyThrows
    @Test
    @Transactional
    @WithMockUser(DummyDataAdapterV3.DUMMY_AGENT)
    public void payHumoToken2Token() {
        var paramsString = "{ \"token\": \"986009ZZZZZZ9999\", \"bin\": \"986009\" }";

        var senderAccount = new P2pAccount()
                .withType(P2pAccountType.TOKEN)
                .withCurrency(860)
                .withParams(objectMapper.readTree(paramsString));

        var paramsString2 = "{ \"token\": \"986009ROAZEP6488\", \"bin\": \"986009\" }";

        var recipientAccount = new P2pAccount()
                .withType(P2pAccountType.TOKEN)
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

    @SneakyThrows
    @Test
    @Transactional
    @WithMockUser(DummyDataAdapterV3.DUMMY_AGENT)
    public void payMasterCardToken2HumoToken() {
        var paramsString = "{ \"token\": \"555536ZZZZZZ9999\", \"bin\": \"55553660\" }";

        var senderAccount = new P2pAccount()
                .withType(P2pAccountType.TOKEN)
                .withCurrency(860)
                .withParams(objectMapper.readTree(paramsString));

        var paramsString2 = "{ \"token\": \"986009ROAZEP6488\", \"bin\": \"986009\" }";

        var recipientAccount = new P2pAccount()
                .withType(P2pAccountType.TOKEN)
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

    @SneakyThrows
    @Test
    @Transactional
    @WithMockUser(DummyDataAdapterV3.DUMMY_AGENT)
    public void cancelHumoToken2Token() {
        //        prepareBank("860049");

        //        prepareP2pParams();

        var paramsString = "{ \"token\": \"986009ZZZZZZ9999\", \"bin\": \"986009\" }";

        var senderAccount = new P2pAccount()
                .withType(P2pAccountType.TOKEN)
                .withCurrency(860)
                .withParams(objectMapper.readTree(paramsString));

        var paramsString2 = "{ \"token\": \"986009ROAZEP6488\", \"bin\": \"986009\" }";

        var recipientAccount = new P2pAccount()
                .withType(P2pAccountType.TOKEN)
                .withCurrency(860)
                .withParams(objectMapper.readTree(paramsString2));

        P2pPayRequest p2PPayRequest = new P2pPayRequest()
                .withFrom(senderAccount)
                .withTo(recipientAccount)
                .withAgentTranId(AGENT_TRAN_ID)
                .withAmount(10000L);

        P2pResponse simpleBillResponse = p2pCheckRequestDecoratorService.p2pPay(p2PPayRequest);

        log.info(String.valueOf(simpleBillResponse));

        //        Assertions.assertThat(response).isNotNull();
        //        Assertions.assertThat(response.getStatusCode()).isNotNull();
        //        Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        Assertions.assertThat(simpleBillResponse).isNotNull();
        Assertions.assertThat(simpleBillResponse.isSuccess()).isTrue();

        P2pCancelRequest p2pCancelRequest = new P2pCancelRequest();
        p2pCancelRequest.setAgentTranId(AGENT_TRAN_ID);

        P2pResponse p2pResponse = p2pCheckRequestDecoratorService.p2pCancel(p2pCancelRequest);
        Assertions.assertThat(p2pResponse).isNotNull();
        Assertions.assertThat(p2pResponse.isSuccess()).isTrue();
    }

    @SneakyThrows
    @Test
    @Transactional
    @WithMockUser(DummyDataAdapterV3.DUMMY_AGENT)
    public void payHumoToken2Pan() {

        int databaseSizeBeforeCreate = billRepository.findAll().size();

        var paramsString = "{ \"token\": \"986009ZZZZZZ9999\", \"bin\": \"986009\" }";

        var senderAccount = new P2pAccount()
                .withType(P2pAccountType.TOKEN)
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

        // Validate the Bill in the database
        List<Bill> billList = billRepository.findAll();
        assertThat(billList).hasSize(databaseSizeBeforeCreate + 3);
        // CHECK (getInfo) -> humo-p2p -> humo-p2p-confirm
    }

    @SneakyThrows
    @Test
    @Transactional
    @WithMockUser(DummyDataAdapterV3.DUMMY_AGENT)
    public void cancelHumoToken2Pan() {

        int databaseSizeBeforeCreate = billRepository.findAll().size();
        int transSizeBeforeCreate = transactionRepository.findAll().size();

        var paramsString = "{ \"token\": \"986009ZZZZZZ9999\", \"bin\": \"986009\" }";

        var senderAccount = new P2pAccount()
                .withType(P2pAccountType.TOKEN)
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
                .withAgentTranId(AGENT_TRAN_ID)
                .withAmount(10000L);

        P2pResponse simpleBillResponse = p2pCheckRequestDecoratorService.p2pPay(p2PPayRequest);

        log.info(String.valueOf(simpleBillResponse));

        //        Assertions.assertThat(response).isNotNull();
        //        Assertions.assertThat(response.getStatusCode()).isNotNull();
        //        Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        Assertions.assertThat(simpleBillResponse).isNotNull();
        Assertions.assertThat(simpleBillResponse.isSuccess()).isTrue();

        P2pCancelRequest p2pCancelRequest = new P2pCancelRequest();
        p2pCancelRequest.setAgentTranId(AGENT_TRAN_ID);

        P2pResponse p2pResponse = p2pCheckRequestDecoratorService.p2pCancel(p2pCancelRequest);
        Assertions.assertThat(p2pResponse).isNotNull();
        Assertions.assertThat(p2pResponse.isSuccess()).isTrue();

        // Validate the Bill in the database
        List<Bill> billList = billRepository.findAll();
        assertThat(billList).hasSize(databaseSizeBeforeCreate + 4);
        // CHECK (getInfo) -> humo-p2p -> humo-p2p-confirm -> humo-reverse

        // Validate the Bill in the database
        List<Transaction> transactions = transactionRepository.findAll();
        assertThat(transactions).hasSize(transSizeBeforeCreate + 1);
        // CHECK (getInfo) -> humo-p2p -> humo-p2p-confirm -> humo-reverse

        Transaction transaction = transactions.get(transSizeBeforeCreate);

        assertTransactionReversed(transaction);

    }

    private void assertTransactionReversed(Transaction transaction) {
        assertThat(transaction.getSenderName()).isEqualTo(P2pHumoPaymentService.class.getSimpleName());
        assertThat(transaction.getSenderStatus()).isEqualTo("cancelled");
        assertThat(transaction.getSenderSuccess()).isFalse();

        assertThat(transaction.getRecipientName()).isEqualTo(P2pHumoPaymentService.class.getSimpleName());
        assertThat(transaction.getRecipientStatus()).isEqualTo("cancelled");
        assertThat(transaction.getRecipientSuccess()).isFalse();
    }

    @SneakyThrows
    @Test
    @Transactional
    @WithMockUser(DummyDataAdapterV3.DUMMY_AGENT)
    public void payHumoPan2Pan() {
        var paramsString = "{ \"pan\": \"9860090000000000\", \"bin\": \"986009\" }";

        var senderAccount = new P2pAccount().withType(P2pAccountType.PAN).withCurrency(860).withParams(objectMapper.readTree(paramsString));

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

    @Test
    public void testRegexp() {
        String foreignBinRegexp = "^(?=8600)(?!860063).*$";

        Pattern pattern = Pattern.compile(foreignBinRegexp);

        String anorCard = "8600630000000000";
        String ipakCard = "8600140000000000";
        String similarCard = "8600600000000000";

        assertThat(pattern.matcher(anorCard).matches()).isFalse();
        assertThat(pattern.matcher(ipakCard).matches()).isTrue();
        assertThat(pattern.matcher(similarCard).matches()).isTrue();

        assertThat(PcUtil.isForeignUzcardBin(anorCard)).isFalse();
        assertThat(PcUtil.isForeignUzcardBin(ipakCard)).isTrue();
        assertThat(PcUtil.isForeignUzcardBin(similarCard)).isTrue();
    }

    @SneakyThrows
    @Test
    @Transactional
    @WithMockUser(DummyDataAdapterV3.DUMMY_AGENT)
    public void cancelHumoPan2Pan() {

        int databaseSizeBeforeCreate = billRepository.findAll().size();
        int transSizeBeforeCreate = transactionRepository.findAll().size();

        var paramsString = "{ \"pan\": \"9860090000000000\", \"bin\": \"986009\" }";

        var senderAccount = new P2pAccount().withType(P2pAccountType.PAN).withCurrency(860).withParams(objectMapper.readTree(paramsString));

        var paramsString2 = "{ \"pan\": \"9860090000000000\", \"bin\": \"986009\" }";

        var recipientAccount = new P2pAccount()
                .withType(P2pAccountType.PAN)
                .withCurrency(860)
                .withParams(objectMapper.readTree(paramsString2));

        P2pPayRequest p2PPayRequest = new P2pPayRequest()
                .withFrom(senderAccount)
                .withTo(recipientAccount)
                .withAgentTranId(AGENT_TRAN_ID)
                .withAmount(10000L);

        P2pResponse simpleBillResponse = p2pCheckRequestDecoratorService.p2pPay(p2PPayRequest);

        log.info(String.valueOf(simpleBillResponse));

        //        Assertions.assertThat(response).isNotNull();
        //        Assertions.assertThat(response.getStatusCode()).isNotNull();
        //        Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        Assertions.assertThat(simpleBillResponse).isNotNull();
        Assertions.assertThat(simpleBillResponse.isSuccess()).isTrue();

        P2pCancelRequest p2pCancelRequest = new P2pCancelRequest();
        p2pCancelRequest.setAgentTranId(AGENT_TRAN_ID);

        P2pResponse p2pResponse = p2pCheckRequestDecoratorService.p2pCancel(p2pCancelRequest);
        Assertions.assertThat(p2pResponse).isNotNull();
        Assertions.assertThat(p2pResponse.isSuccess()).isTrue();


        // Validate the Bill in the database
        List<Bill> billList = billRepository.findAll();
        assertThat(billList).hasSize(databaseSizeBeforeCreate + 4);
        // CHECK (getInfo) -> humo-p2p -> humo-p2p-confirm -> humo-reverse

        // Validate the Bill in the database
        List<Transaction> transactions = transactionRepository.findAll();
        assertThat(transactions).hasSize(transSizeBeforeCreate + 1);
        // CHECK (getInfo) -> humo-p2p -> humo-p2p-confirm -> humo-reverse

        Transaction transaction = transactions.get(transSizeBeforeCreate);

        assertTransactionReversed(transaction);
    }

    //    @Test
    public void textXml() {
        String xml =
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                        "<SOAP-ENV:Envelope xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:ebppif1=\"urn:PaymentServer\"\n" +
                        ">\n" +
                        "    <SOAP-ENV:Body>\n" +
                        "        <SOAP-ENV:Fault>\n" +
                        "            <faultcode>SOAP-ENV:Client</faultcode>\n" +
                        "            <faultstring></faultstring>\n" +
                        "            <detail>\n" +
                        "                <ebppif1:PaymentServerException>\n" +
                        "                    <provider>RSW</provider>\n" +
                        "                    <error>109</error>\n" +
                        "                    <description>226066</description>\n" +
                        "                    <screen>RSW109</screen>\n" +
                        "                </ebppif1:PaymentServerException>\n" +
                        "            </detail>\n" +
                        "        </SOAP-ENV:Fault>\n" +
                        "    </SOAP-ENV:Body>\n" +
                        "</SOAP-ENV:Envelope>\n";

        Object evaluate = XPathUtils.evaluate(xml, "//error/text()");

        GetPaymentStatusCodes byActionCode = GetPaymentStatusCodes.getByActionCode(String.valueOf(evaluate));

        System.out.println(byActionCode.getMsgFull());
    }

    @SneakyThrows
    //    @Test
    @Transactional
    @WithMockUser(DummyDataAdapterV3.DUMMY_AGENT)
    public void checkUzcard2Humo() {
        syncHumoAdapter();

        prepareBank("986009");

        ResponseEntity<P2pResponse> response = checkAndGetResult(PredefinedCardRepo2.DEFAULT_TOKEN, "986009ROAZEP6488");
    }

    @SneakyThrows
    //    @Test
    @Transactional
    @WithMockUser(DummyDataAdapterV3.DUMMY_AGENT)
    public void payUzcard2Humo() {
        syncHumoAdapter();

        prepareBank("986009");

        ResponseEntity<P2pResponse> checkResponse = checkAndGetResult(PredefinedCardRepo2.DEFAULT_TOKEN, "986009ROAZEP6488");

        P2pPayRequest payRequest = new P2pPayRequest()//            .withBillId(checkResponse.getBody().getId())
                //
                ;

        ResponseEntity<P2pResponse> response = p2pAgentResource.pay(payRequest);

        Assertions.assertThat(response).isNotNull();
        Assertions.assertThat(response.getStatusCode()).isNotNull();
        Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        Assertions.assertThat(response.getBody()).isNotNull();
        Assertions.assertThat(response.getBody().isSuccess()).isTrue();
    }

    @SneakyThrows
    //    @Test
    @Transactional
    @WithMockUser(DummyDataAdapterV3.DUMMY_AGENT)
    public void checkHumo2Uzcard() {
        //        syncUzcardAdapter();

        prepareBank("860049");

        ResponseEntity<P2pResponse> response = checkAndGetResult("986009ROAZEP6488", PredefinedCardRepo2.REAL_PAN);
    }

    @Test
    @Transactional
    @WithMockUser(DummyDataAdapterV3.DUMMY_AGENT)
    public void testP2pInfoUzcardPan2Pan() throws IOException {
        prepareBank("860049");

        ResponseEntity<P2pResponse> response = toPanInfoAndGetResult(
                "{ \"pan\": \"" + PredefinedCardRepo2.REAL_PAN + "\" }",
                "{ \"pan\": \"" + PredefinedCardRepo2.REAL_PAN + "\" }",
                P2pAccountType.PAN,
                P2pAccountType.PAN
        );

        Assertions.assertThat(response).isNotNull();
        Assertions.assertThat(response.getStatusCode()).isNotNull();
        Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        Assertions.assertThat(response.getBody()).isNotNull();
        //        Assertions.assertThat(response.getBody().getSuccess()).isTrue();
    }

    @Test
    @Transactional
    @WithMockUser(DummyDataAdapterV3.DUMMY_AGENT)
    public void testP2pInfoUzcardPan2Pan2() throws IOException {
        prepareBank("860049");

        //        ResponseEntity<P2pResponse> response = toPanInfoAndGetResult("{ \"pan\": \"" + PredefinedCardRepo2.REAL_PAN_ANOR + "\" }", "{ \"pan\": \"" + PredefinedCardRepo2.REAL_PAN + "\" }", P2pAccountType.PAN, P2pAccountType.PAN);

        //        P2pAccount from = new P2pAccount()
        //            .withCurrency(860)
        //            .withParams(objectMapper.readTree(fromParams))
        //            .withType(fromType);
        //
        P2pAccount to = new P2pAccount()
                .withCurrency(860)
                .withParams(objectMapper.readTree("{ \"pan\": \"" + PredefinedCardRepo2.REAL_PAN + "\" }"))
                .withType(P2pAccountType.PAN);

        P2pPayRequest check = new P2pPayRequest()
                .withAmount(INFO_AMOUNT)
                .withAgentTranId("test-" + System.currentTimeMillis())
                //            .withFrom(from)
                .withTo(to)//
                ;

        ResponseEntity<P2pResponse> response = p2pAgentResource.getInfo(check);

        Assertions.assertThat(response).isNotNull();
        Assertions.assertThat(response.getStatusCode()).isNotNull();
        Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        Assertions.assertThat(response.getBody()).isNotNull();
        Assertions.assertThat(response.getBody().isSuccess()).isTrue();

        Assertions.assertThat(response.getBody().getRecipient()).isNotNull();
        Assertions.assertThat(response.getBody().getRecipient().getAmount()).isNotNull();
        Assertions.assertThat(response.getBody().getRecipient().getAmount()).isEqualTo(INFO_AMOUNT);
    }

    @Test
    @Transactional
    @WithMockUser(DummyDataAdapterV3.DUMMY_AGENT)
    public void testP2pInfoUzcardPan2MasterCardShouldFail() throws IOException {
        prepareBank("860049");

        ResponseEntity<P2pResponse> response = toPanInfoAndGetResult(
                "{ \"pan\": \"" + PredefinedCardRepo2.REAL_PAN + "\" }",
                "{ \"token\": \"555536ZZZZZZ9999\", \"bin\": \"55553660\" }",
                P2pAccountType.PAN,
                P2pAccountType.TOKEN
        );

        Assertions.assertThat(response).isNotNull();
        Assertions.assertThat(response.getStatusCode()).isNotNull();
        Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        Assertions.assertThat(response.getBody()).isNotNull();
        Assertions.assertThat(response.getBody().isSuccess()).isFalse();
    }

    @Test
    @Transactional
    @WithMockUser(DummyDataAdapterV3.DUMMY_AGENT)
    public void testP2pInfoUzcardToken2Pan() throws IOException {
        ResponseEntity<P2pResponse> response = toPanInfoAndGetResult(
                "{ \"token\": \"" + PredefinedCardRepo2.DEFAULT_TOKEN + "\" }",
                "{ \"pan\": \"" + PredefinedCardRepo2.REAL_PAN + "\" }",
                P2pAccountType.TOKEN,
                P2pAccountType.PAN
        );

        Assertions.assertThat(response).isNotNull();
        Assertions.assertThat(response.getStatusCode()).isNotNull();
        Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        Assertions.assertThat(response.getBody()).isNotNull();

        Assertions.assertThat(response.getBody().getSender()).isNotNull();
        Assertions.assertThat(response.getBody().getSender().getAmount()).isNotNull();
        Assertions.assertThat(response.getBody().getSender().getAmount()).isEqualTo(INFO_AMOUNT);

        Assertions.assertThat(response.getBody().getRecipient()).isNotNull();
        Assertions.assertThat(response.getBody().getRecipient().getAmount()).isNotNull();
        Assertions.assertThat(response.getBody().getRecipient().getAmount()).isEqualTo(INFO_AMOUNT);
        //        Assertions.assertThat(response.getBody().getSuccess()).isTrue();
    }

    @Ignore(value = "java.lang.AssertionError :921")
    @SneakyThrows
    @Test
    @Transactional
    @WithMockUser(DummyDataAdapterV3.DUMMY_AGENT)
    public void payUzcardPan2NciAccount() {
        /* p2p-param-uzcard-nci-anor.json */

        int databaseSizeBeforeCreate = billRepository.findAll().size();

        String senderName = JAVLON_ERALIYEV_COO;

        var paramsString = "{ \"pan\": \"" + PredefinedCardRepo2.REAL_PAN + "\", \"name\": \"" + senderName + "\" }";

        var senderAccount = new P2pAccount().withType(P2pAccountType.PAN).withCurrency(860).withParams(objectMapper.readTree(paramsString));

        String recipientName = TESHABOY_BOLTAYEV;

        var paramsString2 = "{ \"account\": \"22616000990000582001\", \"mfo\": \"00491\", \"name\": \"" + recipientName + "\" }";

        var recipientAccount = new P2pAccount()
                .withType(P2pAccountType.ACCOUNT)
                .withCurrency(860)
                .withParams(objectMapper.readTree(paramsString2));

        long amount = 10000L;

        P2pPayRequest p2PPayRequest = new P2pPayRequest()
                .withFrom(senderAccount)
                .withTo(recipientAccount)
                .withAgentTranId("123")
                .withAmount(amount);

        P2pResponse simpleBillResponse = p2pCheckRequestDecoratorService.p2pPay(p2PPayRequest);

        log.info(String.valueOf(simpleBillResponse));

        Assertions.assertThat(simpleBillResponse).isNotNull();
        Assertions.assertThat(simpleBillResponse.isSuccess()).isTrue();

        // Validate the Bill in the database
        List<Bill> billList = billRepository.findAll();
        assertThat(billList).hasSize(databaseSizeBeforeCreate + 4); // CHECK -> UZCARD_DEBIT -> NCI_CREDIT -> NCI_COMMISSION
        Bill testBill = billList.get(billList.size() - 2); // GET NCI_CREDIT
        //        assertThat(testBill.getRecipientId()).isEqualTo(DEFAULT_RECIPIENT_ID);
        //        assertThat(testBill.getRecipientShortName()).isEqualTo(DEFAULT_RECIPIENT_SHORT_NAME);
        //        assertThat(testBill.getSessionId()).isEqualTo(DEFAULT_SESSION_ID);
        assertThat(testBill.getType()).isEqualTo(RequestType.P2P_CREDIT);
        assertThat(testBill.getAmountRatio()).isEqualTo(100);
        assertThat(testBill.getAmount()).isEqualTo(amount);
        //        assertThat(testBill.getAccount()).isEqualTo(DEFAULT_ACCOUNT);
        assertThat(testBill.getCurrency()).isEqualTo(860);
        assertThat(testBill.getExtId()).isNotNull();
        //        assertThat(testBill.getRequestTime()).isEqualTo(DEFAULT_REQUEST_TIME);
        assertThat(testBill.getRequestJson()).isNotEmpty();
        assertThat(testBill.getRequestJson()).containsKeys("purpose", "accCl", "accCo");
        assertThat(testBill.getRequestJson()).containsEntry("purpose", "Перевод с ПК № \"8600490000000000\" на кошелек TESHABOY BOLTAYEV");
        assertThat(String.valueOf(testBill.getRequestJson().get("purpose"))).contains(PredefinedCardRepo2.REAL_PAN).contains(recipientName);
        assertThat(String.valueOf(testBill.getRequestJson().get("purpose"))).contains(PredefinedCardRepo2.REAL_PAN).contains(recipientName);
        assertThat(testBill.getResponseJson()).isNotEmpty();
        //        assertThat(testBill.getResponseCode()).isEqualTo(DEFAULT_RESPONSE_CODE);
        //        assertThat(testBill.getResponseText()).isEqualTo(DEFAULT_RESPONSE_TEXT);
        assertThat(testBill.isSuccess()).isEqualTo(true);
        assertThat(testBill.getLogin()).isEqualTo(DummyDataAdapterV3.DUMMY_AGENT);
        //        assertThat(testBill.getTransactionId()).isEqualTo(DEFAULT_TRANSACTION_ID);

        Bill commissionBill = billList.get(billList.size() - 1); // GET NCI_COMMISSION
        assertThat(commissionBill.getType()).isEqualTo(RequestType.DEPOSIT);
        assertThat(commissionBill.getAmountRatio()).isEqualTo(100);
        assertThat(commissionBill.getAmount()).isEqualTo(50);
        assertThat(testBill.getAccount()).isEqualTo("22616000990000582001");
        assertThat(commissionBill.getCurrency()).isEqualTo(860);
        assertThat(commissionBill.getExtId()).isNotNull();
        assertThat(commissionBill.getRequestJson()).isNotEmpty();
        assertThat(commissionBill.getRequestJson()).containsKeys("purpose", "accCl", "accCo");
        assertThat(commissionBill.getRequestJson())
                .containsEntry("purpose", "Доход от перевода с ПК № \"8600490000000000\" на счет TESHABOY BOLTAYEV от суммы 100.00 (0.50%)");
        assertThat(String.valueOf(commissionBill.getRequestJson().get("purpose"))).contains(PredefinedCardRepo2.REAL_PAN).contains(recipientName);
        assertThat(commissionBill.getResponseJson()).isNotEmpty();
        assertThat(commissionBill.isSuccess()).isEqualTo(true);
        assertThat(commissionBill.getLogin()).isEqualTo(DummyDataAdapterV3.DUMMY_AGENT);
    }

    @SneakyThrows
    @Test
    @Transactional
    @WithMockUser(DummyDataAdapterV3.DUMMY_AGENT)
    public void payUzcardPan2NciByRequisites() {
        /* p2p-param-uzcard-foreign-2-nci-foreign.json */

        int databaseSizeBeforeCreate = billRepository.findAll().size();

        var paramsString = "{ \"pan\": \"" + PredefinedCardRepo2.REAL_PAN + "\" }";

        var senderAccount = new P2pAccount().withType(P2pAccountType.PAN).withCurrency(860).withParams(objectMapper.readTree(paramsString));

        String recipientName = "TOSHMAT ESHMATOV";
        String purpose = "Оплата 100% за договор 333 от 01.01.21";
        var paramsString2 =
                "{ \"account\": \"22616000990000582001\", \"mfo\": \"01158\", \"name\": \"" +
                        recipientName +
                        "\", \"purpose\": \"" +
                        purpose +
                        "\", \"purposeId\": \"00667\", \"inn\": \"123456789\" }";

        var recipientAccount = new P2pAccount()
                .withType(P2pAccountType.ANOR)
                .withCurrency(860)
                .withParams(objectMapper.readTree(paramsString2));

        long amount = 10000L;
        P2pPayRequest p2PPayRequest = new P2pPayRequest()
                .withFrom(senderAccount)
                .withTo(recipientAccount)
                .withAgentTranId("123")
                .withAmount(amount);

        P2pResponse simpleBillResponse = p2pCheckRequestDecoratorService.p2pPay(p2PPayRequest);

        log.info(String.valueOf(simpleBillResponse));

        //        Assertions.assertThat(response).isNotNull();
        //        Assertions.assertThat(response.getStatusCode()).isNotNull();
        //        Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        Assertions.assertThat(simpleBillResponse).isNotNull();
        Assertions.assertThat(simpleBillResponse.isSuccess()).isTrue();

        // Validate the Bill in the database
        List<Bill> billList = billRepository.findAll(PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "id"))).getContent();
        assertThat(billList).hasSize(databaseSizeBeforeCreate + 4); // CHECK -> UZCARD_DEBIT -> NCI_CREDIT -> NCI_CONFIRM
        Bill testBill = billList.get(billList.size() - 2); // GET NCI_CREDIT
        //        assertThat(testBill.getRecipientId()).isEqualTo(DEFAULT_RECIPIENT_ID);
        //        assertThat(testBill.getRecipientShortName()).isEqualTo(DEFAULT_RECIPIENT_SHORT_NAME);
        //        assertThat(testBill.getSessionId()).isEqualTo(DEFAULT_SESSION_ID);
        assertThat(testBill.getType()).isEqualTo(RequestType.P2P_CREDIT);
        assertThat(testBill.getAmountRatio()).isEqualTo(100);
        assertThat(testBill.getAmount()).isEqualTo(amount);
        //        assertThat(testBill.getAccount()).isEqualTo(DEFAULT_ACCOUNT);
        assertThat(testBill.getCurrency()).isEqualTo(860);
        assertThat(testBill.getExtId()).isNotNull();
        //        assertThat(testBill.getRequestTime()).isEqualTo(DEFAULT_REQUEST_TIME);
        assertThat(testBill.getRequestJson()).isNotEmpty();
        assertThat(testBill.getRequestJson()).containsKeys("purpose", "accCl", "accCo");
        assertThat(testBill.getRequestJson().get("purpose")).isEqualTo(purpose);
        //            .contains(PredefinedCardRepo2.REAL_PAN)
        //            .contains(recipientName);
        assertThat(testBill.getResponseJson()).isNotEmpty();
        //        assertThat(testBill.getResponseCode()).isEqualTo(DEFAULT_RESPONSE_CODE);
        //        assertThat(testBill.getResponseText()).isEqualTo(DEFAULT_RESPONSE_TEXT);
        assertThat(testBill.isSuccess()).isEqualTo(true);
        assertThat(testBill.getLogin()).isEqualTo(DummyDataAdapterV3.DUMMY_AGENT);
        //        assertThat(testBill.getTransactionId()).isEqualTo(DEFAULT_TRANSACTION_ID);
    }

    @SneakyThrows
    @Test
    @Transactional
    @WithMockUser(DummyDataAdapterV3.DUMMY_AGENT)
    public void payNci2Nci() {
        int databaseSizeBeforeCreate = transactionRepository.findAll().size();
        Instant instant = Instant.now();

        var paramsString1 = "{ \"account\": \"22616000990000582001\", \"mfo\": \"00491\", \"name\": \"TOSHMAT ESHMATOV\" }";

        var senderAccount = new P2pAccount()
                .withType(P2pAccountType.ACCOUNT)
                .withCurrency(860)
                .withParams(objectMapper.readTree(paramsString1));

        var paramsString2 =
                "{ \"account\": \"22616000990000582002\", \"mfo\": \"00491\", \"name\": \"ESHMATOV TOSHMAT\", \"comment\": \"Test payment 2\" }";

        var recipientAccount = new P2pAccount()
                .withType(P2pAccountType.ACCOUNT)
                .withCurrency(860)
                .withParams(objectMapper.readTree(paramsString2));

        long amount = 10000L;
        P2pPayRequest p2PPayRequest = new P2pPayRequest()
                .withFrom(senderAccount)
                .withTo(recipientAccount)
                .withAgentTranId("123")
                .withAmount(amount);

        P2pResponse simpleBillResponse = p2pCheckRequestDecoratorService.p2pPay(p2PPayRequest);

        log.info(String.valueOf(simpleBillResponse));

        List<Transaction> transactionList = transactionRepository.findAll();
        assertThat(transactionList).hasSize(databaseSizeBeforeCreate + 1);
        Transaction testTransaction = transactionList.get(transactionList.size() - 1);

        testPayTransactionV2(
                testTransaction,
                instant,
                DummyDataAdapterV3.DUMMY_AGENT,
                TransactionStatus.SUCCESS,
                amount,
                0L,
                amount,
                "22616000990000582002",
                "22616000990000582001",
                P2pProcessorType.NCI,
                0L,
                amount
        );
        //        Assertions.assertThat(response).isNotNull();
        //        Assertions.assertThat(response.getStatusCode()).isNotNull();
        //        Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        Assertions.assertThat(simpleBillResponse).isNotNull();
        Assertions.assertThat(simpleBillResponse.isSuccess()).isTrue();

        Assertions.assertThat(simpleBillResponse.getSender()).isNotNull();
        Assertions.assertThat(simpleBillResponse.getSender().getAmount()).isNotNull();
        Assertions.assertThat(simpleBillResponse.getSender().getAmount()).isEqualTo(amount);

        Assertions.assertThat(simpleBillResponse.getRecipient()).isNotNull();
        Assertions.assertThat(simpleBillResponse.getRecipient().getAmount()).isNotNull();
        Assertions.assertThat(simpleBillResponse.getRecipient().getAmount()).isEqualTo(amount);
    }

    @SneakyThrows
    @Test
    @Transactional
    @WithMockUser(DummyDataAdapterV3.DUMMY_AGENT)
    public void payDeposit2Nci() {
        Deposit deposit = createDeposit("distri");
        var beforeDeposit = deposit.getBalance();
        var depositId = deposit.getId();

        int databaseSizeBeforeCreate = transactionRepository.findAll().size();
        Instant instant = Instant.now();

//        var paramsString1 = "{ \"shortName\": \"distri\", \"inn\": \"11831183\", \"pinfl\": \"00491004911111\" }";
        var paramsString1 = "{ \"shortName\": \"distri\" }";

        var senderAccount = new P2pAccount()
                .withType(P2pAccountType.DEPOSIT)
                .withCurrency(860)
                .withParams(objectMapper.readTree(paramsString1));

        var paramsString2 = "{ \"account\": \"22616000990000582002\", \"mfo\": \"00491\", \"name\": \"ESHMATOV TOSHMAT\", \"comment\": \"Test payment 2\" }";

        var recipientAccount = new P2pAccount()
                .withType(P2pAccountType.ACCOUNT)
                .withCurrency(860)
                .withParams(objectMapper.readTree(paramsString2));

        long amount = 10000L;
        P2pPayRequest p2PPayRequest = new P2pPayRequest()
                .withFrom(senderAccount)
                .withTo(recipientAccount)
                .withAgentTranId("123")
                .withAmount(amount);

        P2pResponse simpleBillResponse = p2pCheckRequestDecoratorService.p2pPay(p2PPayRequest);

        log.info(String.valueOf(simpleBillResponse));

        List<Transaction> transactionList = transactionRepository.findAll();
        assertThat(transactionList).hasSize(databaseSizeBeforeCreate + 1);
        Transaction testTransaction = transactionList.get(transactionList.size() - 1);

        testPayTransactionV2(
                testTransaction,
                instant,
                DummyDataAdapterV3.DUMMY_AGENT,
                TransactionStatus.SUCCESS,
                amount,
                0L,
                amount,
                "22616000990000582002",
                "distri",
                P2pProcessorType.NCI,
                0L,
                amount,
                P2pProcessorType.DEPOSIT
        );
        //        Assertions.assertThat(response).isNotNull();
        //        Assertions.assertThat(response.getStatusCode()).isNotNull();
        //        Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        Assertions.assertThat(simpleBillResponse).isNotNull();
        Assertions.assertThat(simpleBillResponse.isSuccess()).isTrue();

        Assertions.assertThat(simpleBillResponse.getSender()).isNotNull();
        Assertions.assertThat(simpleBillResponse.getSender().getAmount()).isNotNull();
        Assertions.assertThat(simpleBillResponse.getSender().getAmount()).isEqualTo(amount);

        Assertions.assertThat(simpleBillResponse.getRecipient()).isNotNull();
        Assertions.assertThat(simpleBillResponse.getRecipient().getAmount()).isNotNull();
        Assertions.assertThat(simpleBillResponse.getRecipient().getAmount()).isEqualTo(amount);

        var afterDeposit = depositRepository.findById(depositId);
        Assertions.assertThat(beforeDeposit).isGreaterThan(afterDeposit.get().getBalance());
        Assertions.assertThat(beforeDeposit).isEqualTo(afterDeposit.get().getBalance() + simpleBillResponse.getSender().getAmount());
    }

    private Deposit createDeposit(String agentName) {
        Deposit deposit = new Deposit();
        deposit.setBalance(100000000000L);
        deposit.setAgent(createAgent(agentName));
        deposit.setOverdraftLimit(100000000000L);
        deposit.setBalanceCcy(860);
        deposit.setBookedBalance(100000000000L);
        deposit.setLimitInform(10000000000L);
        deposit.setLimitDisable(0L);
        deposit.setBlocked(false);
        deposit.setUnlimit(true);
        deposit.setState(DepositState.ACTIVE);
        return depositRepository.save(deposit);
    }

    private Agent createAgent(String agentName) {
        Agent agent = AgentResourceIntTest.createEntity(null);
        agent.setShortName(agentName);
        agent.orgName("sd");
        agent.setActive(true);
        agent.setAmountRatio(100);
        return agentRepository.save(agent);
    }

    @SneakyThrows
    @Test
    @Transactional
    @WithMockUser(DummyDataAdapterV3.DUMMY_AGENT)
    public void infoNci2Nci() {
        int databaseSizeBeforeCreate = transactionRepository.findAll().size();

        var paramsString1 = "{ \"account\": \"22616000990000582001\", \"mfo\": \"00491\", \"name\": \"TOSHMAT ESHMATOV\" }";

        var senderAccount = new P2pAccount()
                .withType(P2pAccountType.ACCOUNT)
                .withCurrency(860)
                .withParams(objectMapper.readTree(paramsString1));

        var paramsString2 =
                "{ \"account\": \"22616000990000582002\", \"mfo\": \"00491\", \"name\": \"ESHMATOV TOSHMAT\", \"comment\": \"Test payment 2\" }";

        var recipientAccount = new P2pAccount()
                .withType(P2pAccountType.ACCOUNT)
                .withCurrency(860)
                .withParams(objectMapper.readTree(paramsString2));

        long amount = 10000L;

        P2pPayRequest p2PPayRequest = new P2pPayRequest()
                .withFrom(senderAccount)
                .withTo(recipientAccount)
                .withAgentTranId("123")
                .withAmount(amount);

        P2pResponse simpleBillResponse = p2pCheckRequestDecoratorService.p2pInfo(p2PPayRequest);

        log.info(String.valueOf(simpleBillResponse));

        List<Transaction> transactionList = transactionRepository.findAll();
        assertThat(transactionList).hasSize(databaseSizeBeforeCreate);

        Assertions.assertThat(simpleBillResponse).isNotNull();
        Assertions.assertThat(simpleBillResponse.isSuccess()).isTrue();

        Assertions.assertThat(simpleBillResponse.getSender()).isNotNull();
        Assertions.assertThat(simpleBillResponse.getSender().getAmount()).isNotNull();
        Assertions.assertThat(simpleBillResponse.getSender().getAmount()).isEqualTo(amount);

        Assertions.assertThat(simpleBillResponse.getRecipient()).isNotNull();
        Assertions.assertThat(simpleBillResponse.getRecipient().getAmount()).isNotNull();
        Assertions.assertThat(simpleBillResponse.getRecipient().getAmount()).isEqualTo(amount);
    }

    @SneakyThrows
    @Test
    @Transactional
    @WithMockUser(DummyDataAdapterV3.DUMMY_AGENT)
    public void infoNciUsd2NciUsd() {
        int databaseSizeBeforeCreate = transactionRepository.findAll().size();

        var paramsString1 = "{ \"account\": \"22616840990000582001\", \"mfo\": \"00491\", \"name\": \"" + TESHABOY_BOLTAYEV + "\" }";

        var senderAccount = new P2pAccount()
                .withType(P2pAccountType.ACCOUNT)
                .withCurrency(840)
                .withParams(objectMapper.readTree(paramsString1));

        var paramsString2 =
                "{ \"account\": \"22616840990000582002\", \"mfo\": \"00491\", \"name\": \"" +
                        JAVLON_ERALIYEV_COO +
                        "\", \"comment\": \"Test payment 2\" }";

        var recipientAccount = new P2pAccount()
                .withType(P2pAccountType.ACCOUNT)
                .withCurrency(840)
                .withParams(objectMapper.readTree(paramsString2));

        long amount = 100L;

        P2pPayRequest p2PPayRequest = new P2pPayRequest()
                .withFrom(senderAccount)
                .withTo(recipientAccount)
                .withAgentTranId("123")
                .withCurrency(840)
                .withAmount(amount);

        P2pResponse simpleBillResponse = p2pCheckRequestDecoratorService.p2pInfo(p2PPayRequest);

        log.info(String.valueOf(simpleBillResponse));

        List<Transaction> transactionList = transactionRepository.findAll();
        assertThat(transactionList).hasSize(databaseSizeBeforeCreate);

        Assertions.assertThat(simpleBillResponse).isNotNull();
        Assertions.assertThat(simpleBillResponse.isSuccess()).isTrue();

        Assertions.assertThat(simpleBillResponse.getSender()).isNotNull();
        Assertions.assertThat(simpleBillResponse.getSender().getAmount()).isNotNull();
        Assertions.assertThat(simpleBillResponse.getSender().getAmount()).isEqualTo(amount);

        Assertions.assertThat(simpleBillResponse.getRecipient()).isNotNull();
        Assertions.assertThat(simpleBillResponse.getRecipient().getAmount()).isNotNull();
        Assertions.assertThat(simpleBillResponse.getRecipient().getAmount()).isEqualTo(amount);
    }

    //    @SneakyThrows
    //    @Test
    //    @Transactional
    //    @WithMockUser(DummyDataAdapterV3.DUMMY_AGENT)
    //    public void payForeignUzcardPan2NciForeignAccount() {
    //
    //        var paramsString = "{ \"pan\": \"" + PredefinedCardRepo2.REAL_PAN + "\" }";
    //
    //        var senderAccount = new P2pAccount()
    //            .withType(P2pAccountType.PAN)
    //            .withCurrency(860)
    //            .withParams(objectMapper.readTree(paramsString));
    //
    //        var paramsString2 = "{ \"account\": \"22616000990000582001\", \"mfo\": \"01158\", \"name\": \"ABBOS AKA\", \"comment\": \"Оплата 100% за договор номер 34 от 01.02.21\", \"purposeId\": \"00667\", \"inn\": \"123456789\" }";
    //
    //        var recipientAccount = new P2pAccount()
    //            .withType(P2pAccountType.ACCOUNT)
    //            .withCurrency(860)
    //            .withParams(objectMapper.readTree(paramsString2));
    //
    //        P2pPayRequest p2PPayRequest = new P2pPayRequest()
    //            .withFrom(senderAccount)
    //            .withTo(recipientAccount)
    //            .withAgentTranId("123")
    //            .withAmount(10000L);
    //
    //        P2pResponse simpleBillResponse = p2pCheckRequestDecoratorService.p2pPay(p2PPayRequest);
    //
    //        log.info(String.valueOf(simpleBillResponse));
    //
    ////        Assertions.assertThat(response).isNotNull();
    ////        Assertions.assertThat(response.getStatusCode()).isNotNull();
    ////        Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    //        Assertions.assertThat(simpleBillResponse).isNotNull();
    //        Assertions.assertThat(simpleBillResponse.isSuccess()).isTrue();
    //    }

    @SneakyThrows
    @Test
    @Transactional
    @WithMockUser(DummyDataAdapterV3.DUMMY_AGENT)
    public void payUzcardPanToVisa() {
        int beforeBillList = billRepository.findAll().size();

        var paramsString = "{ \"pan\": \"" + PredefinedCardRepo2.REAL_PAN + "\" }";

        var senderAccount = new P2pAccount().withType(P2pAccountType.PAN).withCurrency(860).withParams(objectMapper.readTree(paramsString));

        var paramsString2 = "{ \"pan\": \"" + VISA_PAN + "\" }";

        var recipientAccount = new P2pAccount()
                .withType(P2pAccountType.PAN)
                .withCurrency(840)
                .withParams(objectMapper.readTree(paramsString2));

        long amount = 5000000L;
        P2pPayRequest p2PPayRequest = new P2pPayRequest()
                .withFrom(senderAccount)
                .withTo(recipientAccount)
                .withAgentTranId("123")
                .withAmount(amount);

        P2pResponse simpleBillResponse = p2pCheckRequestDecoratorService.p2pPay(p2PPayRequest);
        int afterBillList = billRepository.findAll().size();

        log.info(String.valueOf(simpleBillResponse));

        Assertions.assertThat(afterBillList).isGreaterThan(beforeBillList);
        Assertions.assertThat(simpleBillResponse).isNotNull();
        Assertions.assertThat(simpleBillResponse.getStatusCode()).isNotNull();
        Assertions.assertThat(simpleBillResponse.getStatusCode()).isEqualTo(0);
        Assertions.assertThat(simpleBillResponse).isNotNull();
        Assertions.assertThat(simpleBillResponse.isSuccess()).isTrue();

        Assertions.assertThat(simpleBillResponse.getSender()).isNotNull();
        Assertions.assertThat(simpleBillResponse.getSender().getAmount()).isNotNull();
        //        Assertions.assertThat(simpleBillResponse.getSender().getAmount()).isEqualTo(4989895L);
        Assertions.assertThat(simpleBillResponse.getSender().getAmount()).isIn(4989895L, 5000000L); // TODO - Test

        Assertions.assertThat(simpleBillResponse.getRecipient()).isNotNull();
        Assertions.assertThat(simpleBillResponse.getRecipient().getAmount()).isNotNull();
        Assertions.assertThat(simpleBillResponse.getRecipient().getAmount()).isEqualTo(467L);
    }

    @SneakyThrows
    @Test
    @Transactional
    @WithMockUser(DummyDataAdapterV3.DUMMY_AGENT)
    public void infoUzcardPanToVisa() {
        int beforeBillList = billRepository.findAll().size();

        var paramsString = "{ \"pan\": \"" + PredefinedCardRepo2.REAL_PAN + "\" }";

        var senderAccount = new P2pAccount().withType(P2pAccountType.PAN).withCurrency(860).withParams(objectMapper.readTree(paramsString));

        var paramsString2 = "{ \"pan\": \"" + VISA_PAN + "\" }";

        var recipientAccount = new P2pAccount()
                .withType(P2pAccountType.PAN)
                .withCurrency(840)
                .withParams(objectMapper.readTree(paramsString2));

        long amount = 500000L;
        P2pPayRequest p2PPayRequest = new P2pPayRequest()
                .withFrom(senderAccount)
                .withTo(recipientAccount)
                .withAgentTranId("123")
                .withAmount(amount);

        P2pResponse simpleBillResponse = p2pCheckRequestDecoratorService.p2pInfo(p2PPayRequest);
        int afterBillList = billRepository.findAll().size();

        log.info(String.valueOf(simpleBillResponse));

        Assertions.assertThat(afterBillList).isGreaterThan(beforeBillList);
        Assertions.assertThat(simpleBillResponse).isNotNull();
        Assertions.assertThat(simpleBillResponse.getStatusCode()).isNotNull();
        Assertions.assertThat(simpleBillResponse.getStatusCode()).isEqualTo(0);
        Assertions.assertThat(simpleBillResponse).isNotNull();
        Assertions.assertThat(simpleBillResponse.isSuccess()).isTrue();

        Assertions.assertThat(simpleBillResponse.getSender()).isNotNull();
        Assertions.assertThat(simpleBillResponse.getSender().getAmount()).isNotNull();
        //        Assertions.assertThat(simpleBillResponse.getSender().getAmount()).isEqualTo(491510L);
        Assertions.assertThat(simpleBillResponse.getSender().getAmount()).isIn(491510L, 500000L); // TODO - Test

        Assertions.assertThat(simpleBillResponse.getRecipient()).isNotNull();
        Assertions.assertThat(simpleBillResponse.getRecipient().getAmount()).isNotNull();
        Assertions.assertThat(simpleBillResponse.getRecipient().getAmount()).isEqualTo(46L);
    }

    @SneakyThrows
    @Test
    @Transactional
    @WithMockUser(DummyDataAdapterV3.DUMMY_AGENT)
    public void infoUzcardPanToVisaFail() {
        int beforeBillList = billRepository.findAll().size();

        var paramsString = "{ \"pan\": \"" + PredefinedCardRepo2.REAL_PAN + "\" }";

        var senderAccount = new P2pAccount().withType(P2pAccountType.PAN).withCurrency(860).withParams(objectMapper.readTree(paramsString));

        var paramsString2 = "{ \"pan\": \"" + "4909570000000000" + "\" }";

        var recipientAccount = new P2pAccount()
                .withType(P2pAccountType.PAN)
                .withCurrency(840)
                .withParams(objectMapper.readTree(paramsString2));

        long amount = 500000L;
        P2pPayRequest p2PPayRequest = new P2pPayRequest()
                .withFrom(senderAccount)
                .withTo(recipientAccount)
                .withAgentTranId("123")
                .withAmount(amount);

        P2pResponse simpleBillResponse = p2pCheckRequestDecoratorService.p2pInfo(p2PPayRequest);
        int afterBillList = billRepository.findAll().size();

        log.info(String.valueOf(simpleBillResponse));

        Assertions.assertThat(afterBillList).isGreaterThan(beforeBillList);
        Assertions.assertThat(simpleBillResponse).isNotNull();
        Assertions.assertThat(simpleBillResponse.getStatusCode()).isNotNull();
        Assertions.assertThat(simpleBillResponse.getStatusCode()).isEqualTo(-104);
        Assertions.assertThat(simpleBillResponse).isNotNull();
        Assertions.assertThat(simpleBillResponse.isSuccess()).isFalse();

        Assertions.assertThat(simpleBillResponse.getSender()).isNotNull();
        Assertions.assertThat(simpleBillResponse.getSender().getAmount()).isNotNull();
        Assertions.assertThat(simpleBillResponse.getSender().getAmount()).isEqualTo(500000L);

        Assertions.assertThat(simpleBillResponse.getRecipient()).isNotNull();
        Assertions.assertThat(simpleBillResponse.getRecipient().getAmount()).isNotNull();
        Assertions.assertThat(simpleBillResponse.getRecipient().getAmount()).isEqualTo(500000L);
    }

    @SneakyThrows
    @Test
    @Transactional
    @WithMockUser(DummyDataAdapterV3.DUMMY_AGENT)
    public void infoVisaToUzcardPan() {
        int beforeBillList = billRepository.findAll().size();

        var paramsString1 = "{ \"pan\": \"" + VISA_PAN + "\" }";

        var senderAccount = new P2pAccount()
                .withType(P2pAccountType.PAN)
                .withCurrency(840)
                .withParams(objectMapper.readTree(paramsString1));

        var paramsString2 = "{ \"pan\": \"" + PredefinedCardRepo2.REAL_PAN + "\" }";

        var recipientAccount = new P2pAccount()
                .withType(P2pAccountType.PAN)
                .withCurrency(860)
                .withParams(objectMapper.readTree(paramsString2));

        long amount = 500000;
        P2pPayRequest p2PPayRequest = new P2pPayRequest()
                .withFrom(senderAccount)
                .withTo(recipientAccount)
                .withAgentTranId("123")
                .withAmount(amount);

        P2pResponse simpleBillResponse = p2pCheckRequestDecoratorService.p2pInfo(p2PPayRequest);
        int afterBillList = billRepository.findAll().size();

        log.info(String.valueOf(simpleBillResponse));

        Assertions.assertThat(afterBillList).isGreaterThan(beforeBillList);
        Assertions.assertThat(afterBillList).isEqualTo(beforeBillList + 2);
        Assertions.assertThat(simpleBillResponse).isNotNull();
        Assertions.assertThat(simpleBillResponse.getStatusCode()).isNotNull();
        Assertions.assertThat(simpleBillResponse.getStatusCode()).isEqualTo(0);
        Assertions.assertThat(simpleBillResponse).isNotNull();
        Assertions.assertThat(simpleBillResponse.isSuccess()).isTrue();

        Assertions.assertThat(simpleBillResponse.getSender()).isNotNull();
        Assertions.assertThat(simpleBillResponse.getSender().getAmount()).isNotNull();
        Assertions.assertThat(simpleBillResponse.getSender().getAmount()).isEqualTo(47L);

        Assertions.assertThat(simpleBillResponse.getRecipient()).isNotNull();
        Assertions.assertThat(simpleBillResponse.getRecipient().getAmount()).isNotNull();
        Assertions.assertThat(simpleBillResponse.getRecipient().getAmount()).isEqualTo(497730L);
    }

    @SneakyThrows
    @Test
    @Transactional
    @WithMockUser(DummyDataAdapterV3.DUMMY_AGENT)
    public void infoVisaToUzcardPanWithWrongCcy() {
        int beforeBillList = billRepository.findAll().size();

        var paramsString1 = "{ \"pan\": \"" + VISA_PAN + "\" }";

        var senderAccount = new P2pAccount()
                .withType(P2pAccountType.PAN)
                .withCurrency(860) // <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<< should be 840
                .withParams(objectMapper.readTree(paramsString1));

        var paramsString2 = "{ \"pan\": \"" + PredefinedCardRepo2.REAL_PAN + "\" }";

        var recipientAccount = new P2pAccount()
                .withType(P2pAccountType.PAN)
                .withCurrency(860)
                .withParams(objectMapper.readTree(paramsString2));

        long amount = 500000;
        P2pPayRequest p2PPayRequest = new P2pPayRequest()
                .withFrom(senderAccount)
                .withTo(recipientAccount)
                .withAgentTranId("123")
                .withAmount(amount);

        P2pResponse simpleBillResponse = p2pCheckRequestDecoratorService.p2pInfo(p2PPayRequest);
        int afterBillList = billRepository.findAll().size();

        log.info(String.valueOf(simpleBillResponse));

        Assertions.assertThat(afterBillList).isEqualTo(beforeBillList);
        Assertions.assertThat(simpleBillResponse).isNotNull();
        Assertions.assertThat(simpleBillResponse.getStatusCode()).isNotNull();
        Assertions.assertThat(simpleBillResponse.getStatusCode()).isEqualTo(BmResultStatus.INVALID_SENDER_PARAMS.getCode());
        Assertions.assertThat(simpleBillResponse.isSuccess()).isFalse();

        Assertions.assertThat(simpleBillResponse.getSender()).isNotNull();
        Assertions.assertThat(simpleBillResponse.getSender().getAmount()).isNotNull();
        Assertions.assertThat(simpleBillResponse.getSender().getAmount()).isEqualTo(amount);

        Assertions.assertThat(simpleBillResponse.getRecipient()).isNotNull();
        Assertions.assertThat(simpleBillResponse.getRecipient().getAmount()).isNotNull();
        Assertions.assertThat(simpleBillResponse.getRecipient().getAmount()).isEqualTo(amount);
    }

    @SneakyThrows
    @Test
    @Transactional
    @WithMockUser(DummyDataAdapterV3.DUMMY_AGENT)
    public void payVisaToUzcardPan() {
        /* p2p-param-visa-uzcard-860049.json */

        int beforeBillList = billRepository.findAll().size();

        var paramsString1 = "{ \"pan\": \"" + VISA_PAN + "\" }";

        var senderAccount = new P2pAccount()
                .withType(P2pAccountType.PAN)
                .withCurrency(840)
                .withParams(objectMapper.readTree(paramsString1));

        var paramsString2 = "{ \"pan\": \"" + PredefinedCardRepo2.REAL_PAN + "\" }";

        var recipientAccount = new P2pAccount()
                .withType(P2pAccountType.PAN)
                .withCurrency(860)
                .withParams(objectMapper.readTree(paramsString2));

        long amount = 500000;
        P2pPayRequest p2PPayRequest = new P2pPayRequest()
                .withFrom(senderAccount)
                .withTo(recipientAccount)
                .withAgentTranId("123")
                .withAmount(amount);

        P2pResponse simpleBillResponse = p2pCheckRequestDecoratorService.p2pPay(p2PPayRequest);
        int afterBillList = billRepository.findAll().size();

        log.info(String.valueOf(simpleBillResponse));

        Assertions.assertThat(afterBillList).isGreaterThan(beforeBillList);
        Assertions.assertThat(afterBillList).isEqualTo(beforeBillList + 4); // UZCARD CHECK -> VISA DEBIT -> FOREX DEBIT -> UZCARD CREDIT
        Assertions.assertThat(simpleBillResponse).isNotNull();
        Assertions.assertThat(simpleBillResponse.getStatusCode()).isNotNull();
        Assertions.assertThat(simpleBillResponse.getStatusCode()).isEqualTo(0);
        Assertions.assertThat(simpleBillResponse).isNotNull();
        Assertions.assertThat(simpleBillResponse.isSuccess()).isTrue();

        Assertions.assertThat(simpleBillResponse.getSender()).isNotNull();
        Assertions.assertThat(simpleBillResponse.getSender().getAmount()).isNotNull();
        Assertions.assertThat(simpleBillResponse.getSender().getAmount()).isEqualTo(47L);

        Assertions.assertThat(simpleBillResponse.getRecipient()).isNotNull();
        Assertions.assertThat(simpleBillResponse.getRecipient().getAmount()).isNotNull();
        Assertions.assertThat(simpleBillResponse.getRecipient().getAmount()).isEqualTo(497730L);
    }

    @SneakyThrows
    @Test
    @Transactional
    @WithMockUser(DummyDataAdapterV3.DUMMY_AGENT)
    public void payVisaToUzcardPanWithCcy840() {
        /* p2p-param-visa-uzcard-860049.json */

        int beforeBillList = billRepository.findAll().size();

        var paramsString1 = "{ \"pan\": \"" + VISA_PAN + "\" }";

        var senderAccount = new P2pAccount()
                .withType(P2pAccountType.PAN)
                .withCurrency(840)
                .withParams(objectMapper.readTree(paramsString1));

        var paramsString2 = "{ \"pan\": \"" + PredefinedCardRepo2.REAL_PAN + "\" }";

        var recipientAccount = new P2pAccount()
                .withType(P2pAccountType.PAN)
                .withCurrency(860)
                .withParams(objectMapper.readTree(paramsString2));

        long amount = 10700;
        P2pPayRequest p2PPayRequest = new P2pPayRequest()
                .withFrom(senderAccount)
                .withTo(recipientAccount)
                .withAgentTranId("123")
                .withCurrency(840)
                .withAmount(amount);

        P2pResponse simpleBillResponse = p2pCheckRequestDecoratorService.p2pPay(p2PPayRequest);
        int afterBillList = billRepository.findAll().size();

        log.info(String.valueOf(simpleBillResponse));

        Assertions.assertThat(afterBillList).isGreaterThan(beforeBillList);
        Assertions.assertThat(afterBillList).isEqualTo(beforeBillList + 4); // UZCARD CHECK -> VISA DEBIT -> FOREX DEBIT -> UZCARD CREDIT
        Assertions.assertThat(simpleBillResponse).isNotNull();
        Assertions.assertThat(simpleBillResponse.getStatusCode()).isNotNull();
        Assertions.assertThat(simpleBillResponse.getStatusCode()).isEqualTo(0);
        Assertions.assertThat(simpleBillResponse).isNotNull();
        Assertions.assertThat(simpleBillResponse.isSuccess()).isTrue();

        Assertions.assertThat(simpleBillResponse.getSender()).isNotNull();
        Assertions.assertThat(simpleBillResponse.getSender().getAmount()).isNotNull();
        Assertions.assertThat(simpleBillResponse.getSender().getAmount()).isEqualTo(10700L);

        Assertions.assertThat(simpleBillResponse.getRecipient()).isNotNull();
        Assertions.assertThat(simpleBillResponse.getRecipient().getAmount()).isNotNull();
        Assertions.assertThat(simpleBillResponse.getRecipient().getAmount()).isEqualTo(113313000L);
    }

    @SneakyThrows
    @Test
    @Transactional
    @WithMockUser(DummyDataAdapterV3.DUMMY_AGENT)
    public void payVisaToUzcardPanWithWrongCcy() {
        int beforeBillList = billRepository.findAll().size();

        var paramsString1 = "{ \"pan\": \"" + VISA_PAN + "\" }";

        var senderAccount = new P2pAccount()
                .withType(P2pAccountType.PAN)
                .withCurrency(860) // <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<< should be 840
                .withParams(objectMapper.readTree(paramsString1));

        var paramsString2 = "{ \"pan\": \"" + PredefinedCardRepo2.REAL_PAN + "\" }";

        var recipientAccount = new P2pAccount()
                .withType(P2pAccountType.PAN)
                .withCurrency(860)
                .withParams(objectMapper.readTree(paramsString2));

        long amount = 500000;
        P2pPayRequest p2PPayRequest = new P2pPayRequest()
                .withFrom(senderAccount)
                .withTo(recipientAccount)
                .withAgentTranId("123")
                .withAmount(amount);

        P2pResponse simpleBillResponse = p2pCheckRequestDecoratorService.p2pPay(p2PPayRequest);
        int afterBillList = billRepository.findAll().size();

        log.info(String.valueOf(simpleBillResponse));

        Assertions.assertThat(afterBillList).isEqualTo(beforeBillList);
        Assertions.assertThat(simpleBillResponse).isNotNull();
        Assertions.assertThat(simpleBillResponse.getStatusCode()).isNotNull();
        Assertions.assertThat(simpleBillResponse.getStatusCode()).isEqualTo(BmResultStatus.INVALID_SENDER_PARAMS.getCode());
        Assertions.assertThat(simpleBillResponse.isSuccess()).isFalse();

        Assertions.assertThat(simpleBillResponse.getSender()).isNotNull();
        Assertions.assertThat(simpleBillResponse.getSender().getAmount()).isNotNull();
        Assertions.assertThat(simpleBillResponse.getSender().getAmount()).isEqualTo(amount);

        Assertions.assertThat(simpleBillResponse.getRecipient()).isNotNull();
        Assertions.assertThat(simpleBillResponse.getRecipient().getAmount()).isNotNull();
        Assertions.assertThat(simpleBillResponse.getRecipient().getAmount()).isEqualTo(amount);
    }

    @SneakyThrows
    @Test
    @Transactional
    @WithMockUser(DummyDataAdapterV3.DUMMY_AGENT)
    public void infoWithoutSenderToVisa() {
        int beforeBillList = billRepository.findAll().size();

        //        var paramsString = "{ \"pan\": \"" + PredefinedCardRepo2.REAL_PAN + "\" }";
        //
        //        var senderAccount = new P2pAccount()
        //            .withType(P2pAccountType.PAN)
        //            .withCurrency(860)
        //            .withParams(objectMapper.readTree(paramsString));

        var paramsString2 = "{ \"pan\": \"" + VISA_PAN + "\" }";

        var recipientAccount = new P2pAccount()
                .withType(P2pAccountType.PAN)
                .withCurrency(840)
                .withParams(objectMapper.readTree(paramsString2));

        long amount = 500000L;
        P2pPayRequest p2PPayRequest = new P2pPayRequest()
                //            .withFrom(senderAccount)
                .withTo(recipientAccount)
                .withAgentTranId("123")
                .withAmount(amount);

        P2pResponse simpleBillResponse = p2pCheckRequestDecoratorService.p2pInfo(p2PPayRequest);
        int afterBillList = billRepository.findAll().size();

        log.info(String.valueOf(simpleBillResponse));

        Assertions.assertThat(afterBillList).isGreaterThan(beforeBillList);
        Assertions.assertThat(simpleBillResponse).isNotNull();
        Assertions.assertThat(simpleBillResponse.getStatusCode()).isNotNull();
        Assertions.assertThat(simpleBillResponse.getStatusCode()).isEqualTo(0);
        Assertions.assertThat(simpleBillResponse).isNotNull();
        Assertions.assertThat(simpleBillResponse.isSuccess()).isTrue();

        //        Assertions.assertThat(simpleBillResponse.getSender()).isNotNull();
        //        Assertions.assertThat(simpleBillResponse.getSender().getAmount()).isNotNull();
        //        Assertions.assertThat(simpleBillResponse.getSender().getAmount()).isEqualTo(491510L);

        Assertions.assertThat(simpleBillResponse.getRecipient()).isNotNull();
        Assertions.assertThat(simpleBillResponse.getRecipient().getAmount()).isNotNull();
        //        Assertions.assertThat(simpleBillResponse.getRecipient().getAmount()).isEqualTo(46L);
    }

    //    @SneakyThrows
    //    @Test
    //    @Transactional
    //    @WithMockUser(DummyDataAdapterV3.DUMMY_AGENT)
    //    public void infoVisaToUzcardPanWithRequestCcyUZS() {
    //
    //        int beforeBillList = billRepository.findAll().size();
    //
    //        var paramsString = "{ \"pan\": \"" + VISA_PAN + "\" }";
    //
    //        var senderAccount = new P2pAccount()
    //            .withType(P2pAccountType.PAN)
    //            .withCurrency(840)
    //            .withParams(objectMapper.readTree(paramsString));
    //
    //        var paramsString2 = "{ \"pan\": \"" + PredefinedCardRepo2.REAL_PAN + "\" }";
    //
    //        var recipientAccount = new P2pAccount()
    //            .withType(P2pAccountType.PAN)
    //            .withCurrency(860)
    //            .withParams(objectMapper.readTree(paramsString2));
    //
    //        long amount = 500000L;
    //        P2pPayRequest p2PPayRequest = new P2pPayRequest()
    //            .withFrom(senderAccount)
    //            .withTo(recipientAccount)
    //            .withAgentTranId("123")
    //            .withAmount(amount);
    //
    //        P2pResponse simpleBillResponse = p2pCheckRequestDecoratorService.p2pInfo(p2PPayRequest);
    //        int afterBillList = billRepository.findAll().size();
    //
    //        log.info(String.valueOf(simpleBillResponse));
    //
    //        Assertions.assertThat(afterBillList).isGreaterThan(beforeBillList);
    //        Assertions.assertThat(simpleBillResponse).isNotNull();
    //        Assertions.assertThat(simpleBillResponse.getStatusCode()).isNotNull();
    //        Assertions.assertThat(simpleBillResponse.getStatusCode()).isEqualTo(0);
    //        Assertions.assertThat(simpleBillResponse).isNotNull();
    //        Assertions.assertThat(simpleBillResponse.isSuccess()).isTrue();
    //
    //        Assertions.assertThat(simpleBillResponse.getSender()).isNotNull();
    //        Assertions.assertThat(simpleBillResponse.getSender().getAmount()).isNotNull();
    //        Assertions.assertThat(simpleBillResponse.getSender().getAmount()).isEqualTo(491510L);
    //
    //        Assertions.assertThat(simpleBillResponse.getRecipient()).isNotNull();
    //        Assertions.assertThat(simpleBillResponse.getRecipient().getAmount()).isNotNull();
    //        Assertions.assertThat(simpleBillResponse.getRecipient().getAmount()).isEqualTo(46L);
    //    }

    @SneakyThrows
    @Test
    @Transactional
    @WithMockUser(DummyDataAdapterV3.DUMMY_AGENT)
    public void infoUzcardPanToVisaWithRequestCcyUSD() {
        int beforeBillList = billRepository.findAll().size();

        var paramsString = "{ \"pan\": \"" + PredefinedCardRepo2.REAL_PAN + "\" }";

        var senderAccount = new P2pAccount().withType(P2pAccountType.PAN).withCurrency(860).withParams(objectMapper.readTree(paramsString));

        var paramsString2 = "{ \"pan\": \"" + VISA_PAN + "\" }";

        var recipientAccount = new P2pAccount()
                .withType(P2pAccountType.PAN)
                .withCurrency(840)
                .withParams(objectMapper.readTree(paramsString2));

        long amount = 50;
        P2pPayRequest p2PPayRequest = new P2pPayRequest()
                .withFrom(senderAccount)
                .withTo(recipientAccount)
                .withAgentTranId("123")
                .withAmount(amount)
                .withCurrency(Constants.USD_CURRENCY);

        P2pResponse simpleBillResponse = p2pCheckRequestDecoratorService.p2pInfo(p2PPayRequest);
        int afterBillList = billRepository.findAll().size();

        log.info(String.valueOf(simpleBillResponse));

        Assertions.assertThat(afterBillList).isGreaterThan(beforeBillList);
        Assertions.assertThat(simpleBillResponse).isNotNull();
        Assertions.assertThat(simpleBillResponse.getStatusCode()).isNotNull();
        Assertions.assertThat(simpleBillResponse.getStatusCode()).isEqualTo(0);
        Assertions.assertThat(simpleBillResponse).isNotNull();
        Assertions.assertThat(simpleBillResponse.isSuccess()).isTrue();

        Assertions.assertThat(simpleBillResponse.getSender()).isNotNull();
        Assertions.assertThat(simpleBillResponse.getSender().getAmount()).isNotNull();
        Assertions.assertThat(simpleBillResponse.getSender().getAmount()).isEqualTo(534250L);

        Assertions.assertThat(simpleBillResponse.getRecipient()).isNotNull();
        Assertions.assertThat(simpleBillResponse.getRecipient().getAmount()).isNotNull();
        Assertions.assertThat(simpleBillResponse.getRecipient().getAmount()).isEqualTo(50L);
    }

    @SneakyThrows
    @Test
    @Transactional
    @WithMockUser(DummyDataAdapterV3.DUMMY_AGENT)
    public void payForeignUzcardPan2NciOwnAccount() {
        var paramsString = "{ \"pan\": \"" + PredefinedCardRepo2.REAL_PAN + "\" }";

        var senderAccount = new P2pAccount().withType(P2pAccountType.PAN).withCurrency(860).withParams(objectMapper.readTree(paramsString));

        var paramsString2 =
                "{ \"account\": \"22616000990000582001\", \"mfo\": \"00491\", \"name\": \"ABBOS AKA\", \"comment\": \"Test payment with Abbos aka\" }";

        var recipientAccount = new P2pAccount()
                .withType(P2pAccountType.ACCOUNT)
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

    @SneakyThrows
    @Test
    @Transactional
    @WithMockUser(DummyDataAdapterV3.DUMMY_AGENT)
    public void payForeignUzcardPan2NciAnorTransitAccount() {
        var paramsString = "{ \"pan\": \"" + PredefinedCardRepo2.REAL_PAN + "\" }";

        var senderAccount = new P2pAccount().withType(P2pAccountType.PAN).withCurrency(860).withParams(objectMapper.readTree(paramsString));

        var paramsString2 =
                "{ \"account\": \"20206000500000491000\", \"mfo\": \"01158\", \"name\": \"ABBOS AKA\", \"comment\": \"Test payment with Abbos aka\" }";

        var recipientAccount = new P2pAccount()
                .withType(P2pAccountType.ACCOUNT)
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

    @SneakyThrows
    @Test
    @Transactional
    @WithMockUser(DummyDataAdapterV3.DUMMY_AGENT)
    public void payAnorUzcard2AnorUzcardShouldUseUzcardP2p() {
        // Uzcard ANOR - Uzcard ANOR	p2p
        /* p2p-param-860063-860063.json */

        int databaseSizeBeforeCreate = transactionRepository.findAll().size();
        Instant instant = Instant.now();
        long amount = 10000L;

        String anorUzcardPan = PredefinedCardRepo2.REAL_PAN_ANOR;
        var paramsString2 = "{ \"pan\": \"" + anorUzcardPan + "\", \"bin\": \"860063\" }";

        var senderAccount = new P2pAccount()
                .withType(P2pAccountType.PAN)
                .withCurrency(860)
                .withParams(objectMapper.readTree(paramsString2));

        var paramsString = "{ \"pan\": \"" + PredefinedCardRepo2.REAL_PAN_ANOR + "\" }";

        var recipientAccount = new P2pAccount()
                .withType(P2pAccountType.PAN)
                .withCurrency(860)
                .withParams(objectMapper.readTree(paramsString));

        P2pPayRequest p2PPayRequest = new P2pPayRequest()
                .withFrom(senderAccount)
                .withTo(recipientAccount)
                .withAgentTranId("123")
                .withAmount(amount);

        P2pResponse simpleBillResponse = p2pCheckRequestDecoratorService.p2pPay(p2PPayRequest);

        log.info(String.valueOf(simpleBillResponse));

        //        Assertions.assertThat(response).isNotNull();
        //        Assertions.assertThat(response.getStatusCode()).isNotNull();
        //        Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        Assertions.assertThat(simpleBillResponse).isNotNull();
        Assertions.assertThat(simpleBillResponse.isSuccess()).isTrue();

        List<Transaction> transactionList = transactionRepository.findAll();
        assertThat(transactionList).hasSize(databaseSizeBeforeCreate + 1);
        Transaction testTransaction = transactionList.get(transactionList.size() - 1);

        assertThat(testTransaction.getComment()).isEqualTo("PAN -> PAN (type:UZCARD_P2P)");

        testCommon(testTransaction, TransactionStatus.SUCCESS, amount, instant, DummyDataAdapterV3.DUMMY_AGENT, 0L, 0L);
        testSender(testTransaction, P2pProcessorType.UZCARD, anorUzcardPan, amount, instant, true);
        testRecipient(testTransaction, P2pProcessorType.UZCARD, PredefinedCardRepo2.REAL_PAN_ANOR, amount, instant, true);
    }

    @SneakyThrows
    @Test
    @Transactional
    @WithMockUser(DummyDataAdapterV3.DUMMY_AGENT)
    public void payAnorNciAccount2UzcardForeignShouldUseUniversalCredit() {
        // со счета 20206, 22616, 20606 - др. банк Узкарт	universal.credit
        // 6;6;true;NCI;UZCARD;9;5;1;0;../fake-data/blob/p2p-param-nci-uzcard-foreign.json

        int databaseSizeBeforeCreate = transactionRepository.findAll().size();
        Instant instant = Instant.now();
        long amount = 10000L;

        String senderNciAccount = "22616000990000582001";
        var paramsString2 =
                "{ \"account\": \"" +
                        senderNciAccount +
                        "\", \"mfo\": \"00491\", \"name\": \"TOSHMAT ESHMATOV\", \"comment\": \"Test payment\" }";

        //22616000990000582001

        var senderAccount = new P2pAccount()
                .withType(P2pAccountType.ACCOUNT)
                .withCurrency(860)
                .withParams(objectMapper.readTree(paramsString2));

        var paramsString = "{ \"pan\": \"" + PredefinedCardRepo2.REAL_PAN + "\" }";

        var recipientAccount = new P2pAccount()
                .withType(P2pAccountType.PAN)
                .withCurrency(860)
                .withParams(objectMapper.readTree(paramsString));

        P2pPayRequest p2PPayRequest = new P2pPayRequest()
                .withFrom(senderAccount)
                .withTo(recipientAccount)
                .withAgentTranId("123")
                .withAmount(amount);

        P2pResponse simpleBillResponse = p2pCheckRequestDecoratorService.p2pPay(p2PPayRequest);

        log.info(String.valueOf(simpleBillResponse));

        //        Assertions.assertThat(response).isNotNull();
        //        Assertions.assertThat(response.getStatusCode()).isNotNull();
        //        Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        Assertions.assertThat(simpleBillResponse).isNotNull();
        Assertions.assertThat(simpleBillResponse.isSuccess()).isTrue();

        List<Transaction> transactionList = transactionRepository.findAll();
        assertThat(transactionList).hasSize(databaseSizeBeforeCreate + 1);
        Transaction testTransaction = transactionList.get(transactionList.size() - 1);

        assertThat(testTransaction.getComment()).isEqualTo("ACCOUNT -> PAN (type:UNIVERSAL_CREDIT)");

        testCommon(testTransaction, TransactionStatus.SUCCESS, amount, instant, DummyDataAdapterV3.DUMMY_AGENT, 0L, 100L);
        testSender(testTransaction, P2pProcessorType.NCI, senderNciAccount, amount + 100L, instant, true);
        testRecipient(testTransaction, P2pProcessorType.UZCARD, PredefinedCardRepo2.REAL_PAN, amount, instant, true);
        //        assertThat(testTransaction.getComment()).contains("virtual");
        //        testPayTransactionV2(testTransaction, instant, DummyDataAdapterV3.DUMMY_AGENT, TransactionStatus.SUCCESS, amount, 0L, amount, PredefinedCardRepo2.REAL_PAN, senderNciAccount);
    }

    @SneakyThrows
    @Test
    @Transactional
    @WithMockUser(DummyDataAdapterV3.DUMMY_AGENT)
    public void payYurikAnorNciAccount2UzcardForeignShouldDoCommissionTran() {
        // Cчет до востребования юр. лиц - Humo AAB
        // 2088;2088;true;NCI;UZCARD;2088;3;5;0;../fake-data/blob/p2p-param-nci-yurik-humo.json

        int beforeBillList = billRepository.findAll().size();

        int databaseSizeBeforeCreate = transactionRepository.findAll().size();
        Instant instant = Instant.now();
        long amount = 10000L;

        String senderNciAccount = "20208000400812952001";
        var paramsString2 =
                "{ \"account\": \"" +
                        senderNciAccount +
                        "\", \"mfo\": \"01060\", \"name\": \"OOO DEVOPS\" }";

        //22616000990000582001

        var senderAccount = new P2pAccount()
                .withType(P2pAccountType.ACCOUNT)
                .withCurrency(860)
                .withParams(objectMapper.readTree(paramsString2));

        var paramsString = "{ \"token\": \"986009ZZZZZZ9999\", \"bin\": \"986009\" }";

        var recipientAccount = new P2pAccount()
                .withType(P2pAccountType.TOKEN)
                .withCurrency(860)
                .withParams(objectMapper.readTree(paramsString));

        P2pPayRequest p2PPayRequest = new P2pPayRequest()
                .withFrom(senderAccount)
                .withTo(recipientAccount)
                .withAgentTranId("123")
                .withAmount(amount);

        P2pResponse simpleBillResponse = p2pCheckRequestDecoratorService.p2pPay(p2PPayRequest);

        log.info(String.valueOf(simpleBillResponse));

        //        Assertions.assertThat(response).isNotNull();
        //        Assertions.assertThat(response.getStatusCode()).isNotNull();
        //        Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        Assertions.assertThat(simpleBillResponse).isNotNull();
        Assertions.assertThat(simpleBillResponse.isSuccess()).isTrue();

        List<Transaction> transactionList = transactionRepository.findAll();
        assertThat(transactionList).hasSize(databaseSizeBeforeCreate + 1);
        Transaction testTransaction = transactionList.get(transactionList.size() - 1);

        assertThat(testTransaction.getComment()).isEqualTo("ACCOUNT -> TOKEN");

        testCommon(testTransaction, TransactionStatus.SUCCESS, amount, instant, DummyDataAdapterV3.DUMMY_AGENT, 0L, 50L);
        testSender(testTransaction, P2pProcessorType.NCI, senderNciAccount, amount + 50L, instant, true);
        testRecipient(testTransaction, P2pProcessorType.HUMO, "986009ZZZZZZ9999", amount, instant, true);
        //        assertThat(testTransaction.getComment()).contains("virtual");
        //        testPayTransactionV2(testTransaction, instant, DummyDataAdapterV3.DUMMY_AGENT, TransactionStatus.SUCCESS, amount, 0L, amount, PredefinedCardRepo2.REAL_PAN, senderNciAccount);

        int afterBillList = billRepository.findAll().size();

        log.info(String.valueOf(simpleBillResponse));

        Assertions.assertThat(afterBillList).isGreaterThan(beforeBillList);
        Assertions.assertThat(afterBillList).isEqualTo(beforeBillList + 4); // UZCARD CHECK -> NCI DEBIT -> COMMISSION DEBIT -> UZCARD CREDIT

        for (Bill bill : billRepository.findAll()) {
            if (Objects.equals(bill.getRecipientShortName(), "HUMO")) {
                assertThat(bill.getType()).isEqualTo(RequestType.CHECK);
            } else if (Objects.equals(bill.getRecipientShortName(), "nci-debit")) {
                assertThat(bill.getAmount()).isEqualTo(10050L);
            } else if (Objects.equals(bill.getRecipientShortName(), "Комисс. дох. по перев. с ЮЛ на ПК ФЛ - E-Pos (Humo)")) {
                assertThat(bill.getAmount()).isEqualTo(50L);
            } else if (Objects.equals(bill.getRecipientShortName(), "P2P_CREDIT")) {
                assertThat(bill.getAmount()).isEqualTo(10000L);
            }
        }

        Assertions.assertThat(simpleBillResponse).isNotNull();
        Assertions.assertThat(simpleBillResponse.getStatusCode()).isNotNull();
        Assertions.assertThat(simpleBillResponse.getStatusCode()).isEqualTo(0);
        Assertions.assertThat(simpleBillResponse).isNotNull();
        Assertions.assertThat(simpleBillResponse.isSuccess()).isTrue();

        Assertions.assertThat(simpleBillResponse.getSender()).isNotNull();
        Assertions.assertThat(simpleBillResponse.getSender().getAmount()).isNotNull();
        Assertions.assertThat(simpleBillResponse.getSender().getAmount()).isEqualTo(10050L);

        Assertions.assertThat(simpleBillResponse.getRecipient()).isNotNull();
        Assertions.assertThat(simpleBillResponse.getRecipient().getAmount()).isNotNull();
        Assertions.assertThat(simpleBillResponse.getRecipient().getAmount()).isEqualTo(10000L);
    }

    @SneakyThrows
    @Test
    @Transactional
    @WithMockUser(DummyDataAdapterV3.DUMMY_AGENT)
    public void payAnorUzcard2UzcardForeignShouldUseUniversal() {
        // Uzcard ANOR - Др. банк Uzcard	universal
        // 107;107;true;UZCARD;UZCARD;3;5;0;0;../fake-data/blob/p2p-param-humo-uzcard-universal.json

        int databaseSizeBeforeCreate = transactionRepository.findAll().size();
        Instant instant = Instant.now();
        long amount = 10000L;

        String anorUzcardPan = PredefinedCardRepo2.REAL_PAN_ANOR;
        var paramsString2 = "{ \"pan\": \"" + anorUzcardPan + "\", \"bin\": \"860063\", \"name\": \"TOSHMAT ESHMATOV TESHABOYEVICH\" }";

        var senderAccount = new P2pAccount()
                .withType(P2pAccountType.PAN)
                .withCurrency(860)
                .withParams(objectMapper.readTree(paramsString2));

        var paramsString = "{ \"pan\": \"" + PredefinedCardRepo2.REAL_PAN + "\" }";

        var recipientAccount = new P2pAccount()
                .withType(P2pAccountType.PAN)
                .withCurrency(860)
                .withParams(objectMapper.readTree(paramsString));

        P2pPayRequest p2PPayRequest = new P2pPayRequest()
                .withFrom(senderAccount)
                .withTo(recipientAccount)
                .withAgentTranId("123")
                .withAmount(amount);

        P2pResponse simpleBillResponse = p2pCheckRequestDecoratorService.p2pPay(p2PPayRequest);

        log.info(String.valueOf(simpleBillResponse));

        //        Assertions.assertThat(response).isNotNull();
        //        Assertions.assertThat(response.getStatusCode()).isNotNull();
        //        Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        Assertions.assertThat(simpleBillResponse).isNotNull();
        Assertions.assertThat(simpleBillResponse.isSuccess()).isTrue();

        List<Transaction> transactionList = transactionRepository.findAll();
        assertThat(transactionList).hasSize(databaseSizeBeforeCreate + 1);
        Transaction testTransaction = transactionList.get(transactionList.size() - 1);

        assertThat(testTransaction.getComment()).isEqualTo("PAN -> PAN (type:UNIVERSAL)");

        testCommon(testTransaction, TransactionStatus.SUCCESS, amount, instant, DummyDataAdapterV3.DUMMY_AGENT, 0L, 0L);
        testSender(testTransaction, P2pProcessorType.UZCARD, anorUzcardPan, amount, instant, true);
        testRecipient(testTransaction, P2pProcessorType.UZCARD, PredefinedCardRepo2.REAL_PAN, amount, instant, true);
    }

    @SneakyThrows
    @Test
    @Transactional
    @WithMockUser(DummyDataAdapterV3.DUMMY_AGENT)
    public void payHumoForeign2UzcardForeignShouldUseUniversalCredit() {
        // Др. банк Humo - Др. банк Uzcard	universal.credit
        // 106;106;true;HUMO;UZCARD;8;5;0;0;../fake-data/blob/p2p-param-humo-uzcard-universal-credit.json

        int databaseSizeBeforeCreate = transactionRepository.findAll().size();
        Instant instant = Instant.now();
        long amount = 10000L;

        String humoPan = "9860090000000000";
        var paramsString2 = "{ \"pan\": \"" + humoPan + "\", \"bin\": \"986009\", \"name\": \"TOSHMAT ESHMATOV TESHABOYEVICH\" }";

        var senderAccount = new P2pAccount()
                .withType(P2pAccountType.PAN)
                .withCurrency(860)
                .withParams(objectMapper.readTree(paramsString2));

        var paramsString = "{ \"pan\": \"" + PredefinedCardRepo2.REAL_PAN + "\" }";

        var recipientAccount = new P2pAccount()
                .withType(P2pAccountType.PAN)
                .withCurrency(860)
                .withParams(objectMapper.readTree(paramsString));

        P2pPayRequest p2PPayRequest = new P2pPayRequest()
                .withFrom(senderAccount)
                .withTo(recipientAccount)
                .withAgentTranId("123")
                .withAmount(amount);

        P2pResponse simpleBillResponse = p2pCheckRequestDecoratorService.p2pPay(p2PPayRequest);

        log.info(String.valueOf(simpleBillResponse));

        //        Assertions.assertThat(response).isNotNull();
        //        Assertions.assertThat(response.getStatusCode()).isNotNull();
        //        Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        Assertions.assertThat(simpleBillResponse).isNotNull();
        Assertions.assertThat(simpleBillResponse.isSuccess()).isTrue();

        List<Transaction> transactionList = transactionRepository.findAll();
        assertThat(transactionList).hasSize(databaseSizeBeforeCreate + 1);
        Transaction testTransaction = transactionList.get(transactionList.size() - 1);

        assertThat(testTransaction.getComment()).isEqualTo("PAN -> PAN (type:UNIVERSAL_CREDIT)");

        testCommon(testTransaction, TransactionStatus.SUCCESS, amount, instant, DummyDataAdapterV3.DUMMY_AGENT, 0L, 0L);
        testSender(testTransaction, P2pProcessorType.HUMO, humoPan, amount, instant, true);
        testRecipient(testTransaction, P2pProcessorType.UZCARD, PredefinedCardRepo2.REAL_PAN, amount, instant, true);
        //        testPayTransactionV2(testTransaction, instant, DummyDataAdapterV3.DUMMY_AGENT, TransactionStatus.SUCCESS, amount, 0L, amount, PredefinedCardRepo2.REAL_PAN, humoPan, P2pProcessorType.UZCARD, 0L, amount, P2pProcessorType.HUMO);
    }

    @SneakyThrows
    @Test
    @Transactional
    @WithMockUser(DummyDataAdapterV3.DUMMY_AGENT)
    public void payAnorHumo2UzcardForeignShouldUseUniversalCredit() {
        // ANOR humo - Др. банк Uzcard	universal.credit
        // 109;109;true;HUMO;UZCARD;7;5;0;0;../fake-data/blob/p2p-param-humo-uzcard-universal-credit.json

        int databaseSizeBeforeCreate = transactionRepository.findAll().size();
        Instant instant = Instant.now();
        long amount = 10000L;

        String humoPan = "9860600000000000";
        var paramsString2 = "{ \"pan\": \"" + humoPan + "\", \"bin\": \"986060\", \"name\": \"TOSHMAT ESHMATOV\" }";

        var senderAccount = new P2pAccount()
                .withType(P2pAccountType.PAN)
                .withCurrency(860)
                .withParams(objectMapper.readTree(paramsString2));

        var paramsString = "{ \"pan\": \"" + PredefinedCardRepo2.REAL_PAN + "\" }";

        var recipientAccount = new P2pAccount()
                .withType(P2pAccountType.PAN)
                .withCurrency(860)
                .withParams(objectMapper.readTree(paramsString));

        P2pPayRequest p2PPayRequest = new P2pPayRequest()
                .withFrom(senderAccount)
                .withTo(recipientAccount)
                .withAgentTranId("123")
                .withAmount(amount);

        P2pResponse simpleBillResponse = p2pCheckRequestDecoratorService.p2pPay(p2PPayRequest);

        log.info(String.valueOf(simpleBillResponse));

        //        Assertions.assertThat(response).isNotNull();
        //        Assertions.assertThat(response.getStatusCode()).isNotNull();
        //        Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        Assertions.assertThat(simpleBillResponse).isNotNull();
        Assertions.assertThat(simpleBillResponse.isSuccess()).isTrue();

        List<Transaction> transactionList = transactionRepository.findAll();
        assertThat(transactionList).hasSize(databaseSizeBeforeCreate + 1);
        Transaction testTransaction = transactionList.get(transactionList.size() - 1);

        assertThat(testTransaction.getComment()).isEqualTo("PAN -> PAN (type:UNIVERSAL_CREDIT)");

        testCommon(testTransaction, TransactionStatus.SUCCESS, amount, instant, DummyDataAdapterV3.DUMMY_AGENT, 0L, 0L);
        testSender(testTransaction, P2pProcessorType.HUMO, humoPan, amount, instant, true);
        testRecipient(testTransaction, P2pProcessorType.UZCARD, PredefinedCardRepo2.REAL_PAN, amount, instant, true);
        //        testPayTransactionV2(testTransaction, instant, DummyDataAdapterV3.DUMMY_AGENT, TransactionStatus.SUCCESS, amount, 0L, amount, PredefinedCardRepo2.REAL_PAN, humoPan, P2pProcessorType.UZCARD, 0L, amount, P2pProcessorType.HUMO);
    }

    @SneakyThrows
    @Test
    @Transactional
    @WithMockUser(DummyDataAdapterV3.DUMMY_AGENT)
    public void payAnorHumoToken2UzcardForeignShouldUseUniversalCredit() {
        // ANOR humo - Др. банк Uzcard	universal.credit
        // 109;109;true;HUMO;UZCARD;7;5;0;0;../fake-data/blob/p2p-param-humo-uzcard-universal-credit.json

        int databaseSizeBeforeCreate = transactionRepository.findAll().size();
        Instant instant = Instant.now();
        long amount = 10000L;

        String humoPan = "986060QMQNES3710";
        var paramsString2 = "{ \"token\": \"" + humoPan + "\", \"bin\": \"986060\", \"name\": \"TOSHMAT ESHMATOV\" }";

        var senderAccount = new P2pAccount()
                .withType(P2pAccountType.TOKEN)
                .withCurrency(860)
                .withParams(objectMapper.readTree(paramsString2));

        var paramsString = "{ \"pan\": \"" + PredefinedCardRepo2.REAL_PAN + "\" }";

        var recipientAccount = new P2pAccount()
                .withType(P2pAccountType.PAN)
                .withCurrency(860)
                .withParams(objectMapper.readTree(paramsString));

        P2pPayRequest p2PPayRequest = new P2pPayRequest()
                .withFrom(senderAccount)
                .withTo(recipientAccount)
                .withAgentTranId("123")
                .withAmount(amount);

        P2pResponse simpleBillResponse = p2pCheckRequestDecoratorService.p2pPay(p2PPayRequest);

        log.info(String.valueOf(simpleBillResponse));

        //        Assertions.assertThat(response).isNotNull();
        //        Assertions.assertThat(response.getStatusCode()).isNotNull();
        //        Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        Assertions.assertThat(simpleBillResponse).isNotNull();
        Assertions.assertThat(simpleBillResponse.isSuccess()).isTrue();

        List<Transaction> transactionList = transactionRepository.findAll();
        assertThat(transactionList).hasSize(databaseSizeBeforeCreate + 1);
        Transaction testTransaction = transactionList.get(transactionList.size() - 1);

        assertThat(testTransaction.getComment()).isEqualTo("TOKEN -> PAN (type:UNIVERSAL_CREDIT)");

        testCommon(testTransaction, TransactionStatus.SUCCESS, amount, instant, DummyDataAdapterV3.DUMMY_AGENT, 0L, 0L);
        testSender(testTransaction, P2pProcessorType.HUMO, humoPan, amount, instant, true);
        testRecipient(testTransaction, P2pProcessorType.UZCARD, PredefinedCardRepo2.REAL_PAN, amount, instant, true);
        //        testPayTransactionV2(testTransaction, instant, DummyDataAdapterV3.DUMMY_AGENT, TransactionStatus.SUCCESS, amount, 0L, amount, PredefinedCardRepo2.REAL_PAN, humoPan, P2pProcessorType.UZCARD, 0L, amount, P2pProcessorType.HUMO);
    }

    @SneakyThrows
    @Test
    @Transactional
    @WithMockUser(DummyDataAdapterV3.DUMMY_AGENT)
    public void payNciAccount2UzcardPanShouldUseUniversalCreditWithoutMiddleName() {
        int databaseSizeBeforeCreate = transactionRepository.findAll().size();
        Instant instant = Instant.now();
        long amount = 10000L;

        String humoPan = "9860090000000000";
        var paramsString2 = "{ \"pan\": \"" + humoPan + "\", \"bin\": \"986009\", \"name\": \"TOSHMAT ESHMATOV\" }";

        var senderAccount = new P2pAccount()
                .withType(P2pAccountType.PAN)
                .withCurrency(860)
                .withParams(objectMapper.readTree(paramsString2));

        var paramsString = "{ \"pan\": \"" + PredefinedCardRepo2.REAL_PAN + "\" }";

        var recipientAccount = new P2pAccount()
                .withType(P2pAccountType.PAN)
                .withCurrency(860)
                .withParams(objectMapper.readTree(paramsString));

        P2pPayRequest p2PPayRequest = new P2pPayRequest()
                .withFrom(senderAccount)
                .withTo(recipientAccount)
                .withAgentTranId("123")
                .withAmount(amount);

        P2pResponse simpleBillResponse = p2pCheckRequestDecoratorService.p2pPay(p2PPayRequest);

        log.info(String.valueOf(simpleBillResponse));

        //        Assertions.assertThat(response).isNotNull();
        //        Assertions.assertThat(response.getStatusCode()).isNotNull();
        //        Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        Assertions.assertThat(simpleBillResponse).isNotNull();
        Assertions.assertThat(simpleBillResponse.isSuccess()).isTrue();

        List<Transaction> transactionList = transactionRepository.findAll();
        assertThat(transactionList).hasSize(databaseSizeBeforeCreate + 1);
        Transaction testTransaction = transactionList.get(transactionList.size() - 1);

        assertThat(testTransaction.getComment()).isEqualTo("PAN -> PAN (type:UNIVERSAL_CREDIT)");

        testCommon(testTransaction, TransactionStatus.SUCCESS, amount, instant, DummyDataAdapterV3.DUMMY_AGENT, 0L, 0L);
        testSender(testTransaction, P2pProcessorType.HUMO, humoPan, amount, instant, true);
        testRecipient(testTransaction, P2pProcessorType.UZCARD, PredefinedCardRepo2.REAL_PAN, amount, instant, true);
        //        testPayTransactionV2(testTransaction, instant, DummyDataAdapterV3.DUMMY_AGENT, TransactionStatus.SUCCESS, amount, 0L, amount, PredefinedCardRepo2.REAL_PAN, humoPan, P2pProcessorType.UZCARD, 0L, amount, P2pProcessorType.HUMO);
    }

    @Ignore("Made name is never null")
    @SneakyThrows
    @Test
    @Transactional
    @WithMockUser(DummyDataAdapterV3.DUMMY_AGENT)
    public void payNciAccount2UzcardPanShouldUseUniversalCreditAndFailBczNameIsNull() {
        int databaseSizeBeforeCreate = transactionRepository.findAll().size();
        Instant instant = Instant.now();
        long amount = 10000L;

        String humoPan = "9860090000000000";
        var paramsString2 = "{ \"pan\": \"" + humoPan + "\", \"bin\": \"986009\" }";

        var senderAccount = new P2pAccount()
                .withType(P2pAccountType.PAN)
                .withCurrency(860)
                .withParams(objectMapper.readTree(paramsString2));

        var paramsString = "{ \"pan\": \"" + PredefinedCardRepo2.REAL_PAN + "\" }";

        var recipientAccount = new P2pAccount()
                .withType(P2pAccountType.PAN)
                .withCurrency(860)
                .withParams(objectMapper.readTree(paramsString));

        P2pPayRequest p2PPayRequest = new P2pPayRequest()
                .withFrom(senderAccount)
                .withTo(recipientAccount)
                .withAgentTranId("123")
                .withAmount(amount);

        P2pResponse simpleBillResponse = p2pCheckRequestDecoratorService.p2pPay(p2PPayRequest);

        log.info(String.valueOf(simpleBillResponse));

        //        Assertions.assertThat(response).isNotNull();
        //        Assertions.assertThat(response.getStatusCode()).isNotNull();
        //        Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        Assertions.assertThat(simpleBillResponse).isNotNull();
        Assertions.assertThat(simpleBillResponse.isSuccess()).isFalse();

        List<Transaction> transactionList = transactionRepository.findAll();
        assertThat(transactionList).hasSize(databaseSizeBeforeCreate + 1);
        Transaction testTransaction = transactionList.get(transactionList.size() - 1);

        assertThat(testTransaction.getComment()).isEqualTo("PAN -> PAN (type:UNIVERSAL_CREDIT)");

        testCommon(testTransaction, TransactionStatus.FAILED, amount, instant, DummyDataAdapterV3.DUMMY_AGENT, 0L, 0L);
        testSender(testTransaction, P2pProcessorType.HUMO, humoPan, amount, instant, false);
        //        testRecipient(testTransaction, P2pProcessorType.UZCARD, PredefinedCardRepo2.REAL_PAN, amount, instant, true);

        //        testPayTransactionV2(testTransaction, instant, DummyDataAdapterV3.DUMMY_AGENT, TransactionStatus.SUCCESS, amount, 0L, amount, PredefinedCardRepo2.REAL_PAN, humoPan, P2pProcessorType.UZCARD, 0L, amount, P2pProcessorType.HUMO);
    }

    // p2p-param-nci-uzcard-anor2.json

    @SneakyThrows
    @Test
    @Transactional
    @WithMockUser(DummyDataAdapterV3.DUMMY_AGENT)
    public void payNciAccount2AnorUzcardPanShouldUseSimpleCredit() {
        //  Счет 22616 (кошелек) ANOR - Uzcard ANOR	p2p

        int databaseSizeBeforeCreate = transactionRepository.findAll().size();
        Instant instant = Instant.now();
        long amount = 10000L;

        String senderNciAccount = "22616000990000582001";
        var paramsString2 =
                "{ \"account\": \"" +
                        senderNciAccount +
                        "\", \"mfo\": \"00491\", \"name\": \"TOSHMAT ESHMATOV\", \"comment\": \"Test payment\" }";

        // 22616000990000582001

        var senderAccount = new P2pAccount()
                .withType(P2pAccountType.ACCOUNT)
                .withCurrency(860)
                .withParams(objectMapper.readTree(paramsString2));

        var paramsString = "{ \"pan\": \"" + PredefinedCardRepo2.REAL_PAN_ANOR + "\" }";

        var recipientAccount = new P2pAccount()
                .withType(P2pAccountType.PAN)
                .withCurrency(860)
                .withParams(objectMapper.readTree(paramsString));

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

        List<Transaction> transactionList = transactionRepository.findAll();
        assertThat(transactionList).hasSize(databaseSizeBeforeCreate + 1);
        Transaction testTransaction = transactionList.get(transactionList.size() - 1);

        assertThat(testTransaction.getComment()).doesNotContain("virtual");
        testPayTransactionV2(
                testTransaction,
                instant,
                DummyDataAdapterV3.DUMMY_AGENT,
                TransactionStatus.SUCCESS,
                amount,
                0L,
                amount,
                PredefinedCardRepo2.REAL_PAN_ANOR,
                senderNciAccount
        );
    }

    // p2p-param-nci-uzcard-without-creditType.json

    @Ignore("Disabled bcs test is working but p2p_param conflicts with other test")
    @SneakyThrows
    @Test
    @Transactional
    @WithMockUser(DummyDataAdapterV3.DUMMY_AGENT)
    public void payNciAccount2AnorUzcardWithoutCreditTypeShouldFail() {
        //  Счет 22616 (кошелек) ANOR - Uzcard Ipak	p2p

        int databaseSizeBeforeCreate = transactionRepository.findAll().size();
        Instant instant = Instant.now();
        long amount = 10000L;

        String senderNciAccount = "22616000990000582001";
        var paramsString2 =
                "{ \"account\": \"" +
                        senderNciAccount +
                        "\", \"mfo\": \"00491\", \"name\": \"TOSHMAT ESHMATOV\", \"comment\": \"Test payment\" }";

        // 22616000990000582001

        var senderAccount = new P2pAccount()
                .withType(P2pAccountType.ACCOUNT)
                .withCurrency(860)
                .withParams(objectMapper.readTree(paramsString2));

        var paramsString = "{ \"pan\": \"8600140000000000\" }";

        var recipientAccount = new P2pAccount()
                .withType(P2pAccountType.PAN)
                .withCurrency(860)
                .withParams(objectMapper.readTree(paramsString));

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
        Assertions.assertThat(simpleBillResponse.isSuccess()).isFalse();

        List<Transaction> transactionList = transactionRepository.findAll();
        assertThat(transactionList).hasSize(databaseSizeBeforeCreate + 1);
        Transaction testTransaction = transactionList.get(transactionList.size() - 1);
        //        assertThat(testTransaction.getComment()).contains("virtual");
        //        testPayTransactionV2(testTransaction, instant, DummyDataAdapterV3.DUMMY_AGENT, TransactionStatus.SUCCESS, amount, 0L, amount, PredefinedCardRepo2.REAL_PAN_ANOR, senderNciAccount);
    }

    /* p2p-param-nci-uzcard-foreign.json */
    @SneakyThrows
    @Test
    @Transactional
    @WithMockUser(DummyDataAdapterV3.DUMMY_AGENT)
    public void payNciAccount2UzcardToken() {
        int databaseSizeBeforeCreate = transactionRepository.findAll().size();
        int billSizeBeforeCreate = billRepository.findAll().size();
        Instant instant = Instant.now();
        long amount = 10000L;

        String senderNciAccount = "22616000990000582001";
        var paramsString1 =
                "{ \"account\": \"" +
                        senderNciAccount +
                        "\", \"mfo\": \"00491\", \"name\": \"TOSHMAT ESHMATOV\", \"comment\": \"Test payment\" }";

        // 22616000990000582001

        var senderAccount = new P2pAccount()
                .withType(P2pAccountType.ACCOUNT)
                .withCurrency(860)
                .withParams(objectMapper.readTree(paramsString1));

        var paramsString2 = "{ \"token\": \"" + PredefinedCardRepo2.DEFAULT_TOKEN + "\", \"bin\": \"860049\" }";

        var recipientAccount = new P2pAccount()
                .withType(P2pAccountType.TOKEN)
                .withCurrency(860)
                .withParams(objectMapper.readTree(paramsString2));

        P2pPayRequest p2PPayRequest = new P2pPayRequest()
                .withFrom(senderAccount)
                .withTo(recipientAccount)
                .withAgentTranId("123")
                .withAmount(amount);

        P2pResponse simpleBillResponse = p2pCheckRequestDecoratorService.p2pPay(p2PPayRequest);

        log.info(String.valueOf(simpleBillResponse));

        //        Assertions.assertThat(response).isNotNull();
        //        Assertions.assertThat(response.getStatusCode()).isNotNull();
        //        Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        Assertions.assertThat(simpleBillResponse).isNotNull();
        Assertions.assertThat(simpleBillResponse.isSuccess()).isTrue();

        List<Transaction> transactionList = transactionRepository.findAll();
        assertThat(transactionList).hasSize(databaseSizeBeforeCreate + 1);
        Transaction testTransaction = transactionList.get(transactionList.size() - 1);

        testPayTransactionV2(
                testTransaction,
                instant,
                DummyDataAdapterV3.DUMMY_AGENT,
                TransactionStatus.SUCCESS,
                amount,
                0L,
                amount,
                PredefinedCardRepo2.DEFAULT_TOKEN,
                senderNciAccount
        );

        List<Bill> billList = billRepository.findAll();
        assertThat(billList).hasSize(billSizeBeforeCreate + 3);
        //        Transaction testTransaction = transactionList.get(transactionList.size() - 1);

        assertThat(testTransaction.getComment()).isEqualTo("ACCOUNT -> TOKEN (type:UNIVERSAL_CREDIT)");

        testCommon(testTransaction, TransactionStatus.SUCCESS, amount, instant, DummyDataAdapterV3.DUMMY_AGENT, 0L, 100L);
        testSender(testTransaction, P2pProcessorType.NCI, senderNciAccount, amount + 100L, instant, true);
        testRecipient(testTransaction, P2pProcessorType.UZCARD, "AAAAAAAAAA", amount, instant, true);
    }

    @Test
    @Transactional
    @WithMockUser(DummyDataAdapterV3.DUMMY_AGENT)
    public void testToHumoGetInfo() throws IOException {
        var paramsString = "{ \"token\": \"986009ZZZZZZ9999\", \"bin\": \"986009\" }";

        var recipAcc = new P2pAccount().withType(P2pAccountType.TOKEN).withCurrency(860).withParams(objectMapper.readTree(paramsString));

        var paramsString2 = "{ \"account\": \"20206840690000252001\"}";

        P2pPayRequest p2PPayRequest = new P2pPayRequest()
                //            .withFrom(senderAccount)
                .withTo(recipAcc)
                .withAgentTranId("123")
                .withAmount(10000L);

        P2pResponse simpleBillResponse = p2pCheckRequestDecoratorService.p2pInfo(p2PPayRequest);
        assertThat(simpleBillResponse).isNotNull();
    }

    private void prepareBank(String cardBin) {
        Bank bank = new Bank();
        bank
                .cardBin(cardBin)
                .cardLimitIn(1000000L)
                .cardLimitOut(100000000L)
                .name("Test Bank")
                .p2p(Boolean.TRUE)
                .terminalId("12345")
                .port(1)
                .merchantId("54321");
        bankRepository.saveAndFlush(bank);
    }

    private void syncHumoAdapter() {
        dataAdapterServiceV3.sync(HumoCreditDataAdapterV3.HUMO_DATA_ADAPTER);

        Optional<Recipient> recipient = recipientRepository.findTop1ByShortName(HumoCreditGatewayServiceV3.NAME);

        assertThat(recipient.isPresent()).isTrue();

        AgentDTO agent = dataAdapterServiceV3.getAgentOrCreate(DummyDataAdapterV3.DUMMY_AGENT, 1000000L, DummyDataAdapterV3.INN);
        AgentRecipient agentRecipient = dataAdapterServiceV3.saveAgentRecipient(recipient.get(), agent);

        agentRecipient.setEposMerchant("12345");
        agentRecipient.setEposTerminal("54321");
        agentRecipient.setEposPort(1);

        agentRecipientRepository.save(agentRecipient);
    }

    private ResponseEntity<P2pResponse> checkAndGetResult(String fromPan, String toPan) throws IOException {
        var fromParams = "{ \"pan\": \"" + fromPan + "\" }";
        var toParams = "{ \"pan\": \"" + toPan + "\" }";

        P2pAccount from = new P2pAccount().withCurrency(860).withParams(objectMapper.readTree(fromParams)).withType(P2pAccountType.TOKEN);

        P2pAccount to = new P2pAccount().withCurrency(860).withParams(objectMapper.readTree(toParams)).withType(P2pAccountType.PAN);

        P2pPayRequest check = new P2pPayRequest()
                .withAmount(100L)
                .withAgentTranId("test-" + System.currentTimeMillis())
                .withFrom(from)
                .withTo(to);

        ResponseEntity<P2pResponse> response = p2pAgentResource.pay(check);

        Assertions.assertThat(response).isNotNull();
        Assertions.assertThat(response.getStatusCode()).isNotNull();
        Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        Assertions.assertThat(response.getBody()).isNotNull();
        Assertions.assertThat(response.getBody().isSuccess()).isTrue();

        return response;
    }

    private ResponseEntity<P2pResponse> toPanInfoAndGetResult(
            String fromParams,
            String toParams,
            P2pAccountType fromType,
            P2pAccountType toType
    ) throws IOException {
        P2pAccount from = new P2pAccount().withCurrency(860).withParams(objectMapper.readTree(fromParams)).withType(fromType);

        P2pAccount to = new P2pAccount().withCurrency(860).withParams(objectMapper.readTree(toParams)).withType(toType);

        P2pPayRequest check = new P2pPayRequest()
                .withAmount(INFO_AMOUNT)
                .withAgentTranId("test-" + System.currentTimeMillis())
                .withFrom(from)
                .withTo(to)//
                ;

        ResponseEntity<P2pResponse> response = p2pAgentResource.getInfo(check);

        Assertions.assertThat(response).isNotNull();
        Assertions.assertThat(response.getStatusCode()).isNotNull();
        Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        Assertions.assertThat(response.getBody()).isNotNull();
        //        Assertions.assertThat(response.getBody().getSuccess()).isTrue();

        return response;
    }

    @SneakyThrows
    @Test
    @Transactional
    public void testValidator() {
        var paramsString = "{ \"pan\": \"8600660000000000\" }";
        var objectMapper = new ObjectMapper();

        var senderAccount = new P2pAccount();
        senderAccount.withType(P2pAccountType.PAN).withCurrency(860).withParams(objectMapper.readTree(paramsString));

        //        var result = validatorService.parse(senderAccount);

        BmResult supported = p2PRegistry.getSenderService(senderAccount);

        assertThat(supported).isNotNull();
        assertThat(supported.isSuccess()).isTrue();
        //        assertThat(supported.getData()).isNotNull();
    }

    @Test
    @Transactional
    public void testFormat() throws JsonProcessingException {
        double value = 100000000d;
        String format = String.format(Locale.US, "%.2f", value);

        System.out.println("Formatted : " + format);
        System.out.println("Value     : " + value);
        System.out.println(new ObjectMapper().writeValueAsString(Collections.singletonMap("test", format)));
        System.out.println(new ObjectMapper().writeValueAsString(Collections.singletonMap("test", value)));
    }

    @Test
    @Transactional
    @WithMockUser(DummyDataAdapterV3.DUMMY_AGENT)
    public void p2pReconcileQuery() {
        payHumoPan2Pan();
        payUzcardPan2Pan();
        payHumoToken2Pan();

        //        transactionRepository.findAllP2pHumoTerminals(TransactionStatus.SUCCESS);
        automaticReconcileService.getP2pTerminals();
    }

    @Test
    @Transactional
    public void testReadFromFile() {
        String centreId = humoP2pHelperService.getCentreId("9860010000000000");
        Assertions.assertThat(centreId).isNotNull();
        Assertions.assertThat(centreId).isEqualTo("Ipoteka");
    }

    @Test
    @Transactional
    public void testObjectMapper() {
        SettingDTO settingDTO = new SettingDTO();
        settingDTO.setId(1L);
        settingDTO.setActive(true);
        settingDTO.setOrd(1);
        settingDTO.setShortName("test");

        Map<String, String> map = objectMapperUtils.objectToMap(settingDTO);
        String a = "";
    }

    @Test
    @Transactional
    public void testSpEx() {
        ExpressionParser parser = new SpelExpressionParser();

        // Inventions Array
        StandardEvaluationContext teslaContext = new StandardEvaluationContext(
                //            new Transaction().senderAccount("9860600000000000")
        );
        //        teslaContext.setVariable("setting", new Setting().active(true)
        //            .addProp(new SettingProperty().key(Prop.HUMO_MERCHANT).value("11111"))
        //            .addProp(new SettingProperty().key(Prop.HUMO_TERMINAL).value("22222"))
        //        );

        teslaContext.setVariable(
                "tran",
                new Transaction()
                //            .senderAccount("9860600000000000")
        );

        boolean trueValue = parser.parseExpression("'98606001' matches '^9860.*'").getValue(Boolean.class);

        String terminal = parser
                .parseExpression(
                        "#tran.senderAccount?:'' matches '^98606001.*' ? '1111' : (#tran.senderAccount?:'' matches '^98606002.*' ? '2222' : '3333')"
                )
                .getValue(teslaContext, String.class);

        System.out.println(trueValue);
        System.out.println(terminal);
    }

    @Test
    @Transactional
    @WithMockUser("TEST_USER")
    public void test10MlnToSuccess() throws JsonProcessingException {
        var paramsString =
                "{ \"pan\": \"9860090000000000\", \"bin\": \"986009\", \"additionalParam\": {\"citizen\":  \"Tashkent\", \"birthDate\":  \"2022-01-05\", \"docType\":  \"Биометрическая паспорт гражданина Респ.Узбекистан\", \"docNumber\": \"AA0000000\", \"docDate\": \"2000-01-01\", \"pinFl\":  \"30000000000000\", \"firstName\":  \"Test\", \"lastName\":  \"Testov\", \"middleName\":  \"Testovich\"} }";

        var senderAccount = new P2pAccount().withType(P2pAccountType.PAN).withCurrency(860).withParams(objectMapper.readTree(paramsString));

        var paramsString2 = "{ \"pan\": \"8600490000000000\", \"exp\": \"1025\" }";

        var recipientAccount = new P2pAccount()
                .withType(P2pAccountType.PAN)
                .withCurrency(860)
                .withParams(objectMapper.readTree(paramsString2));

        P2pPayRequest p2PPayRequest = new P2pPayRequest()
                .withFrom(senderAccount)
                .withTo(recipientAccount)
                .withAgentTranId("123")
                .withAmount(10000L);

        ResponseEntity<P2pResponse> simpleBillResponseEntity = p2pAgentResource.pay(p2PPayRequest);

        P2pResponse simpleBillResponse = responseShouldBeNotNull(simpleBillResponseEntity);

        Assertions.assertThat(simpleBillResponse).isNotNull();
        Assertions.assertThat(simpleBillResponse.getStatusCode()).isNotNull();
        Assertions.assertThat(simpleBillResponse.getStatusCode()).isEqualTo(0);
        assertThat(simpleBillResponse.getStatusMessage()).isEqualTo(TransactionStatus.SUCCESS.name());
    }

    @Test
    @Transactional
    @WithMockUser("TEST_USER")
    public void test10MlnToSuccessForHumo() throws JsonProcessingException {
        var paramsString =
                "{ \"pan\": \"9860090000000000\", \"bin\": \"986009\", \"additionalParam\": {\"citizen\":  \"Tashkent\", \"birthDate\":  \"2022-01-05\", \"docType\":  \"Биометрическая паспорт гражданина Респ.Узбекистан\", \"docNumber\": \"BB1111111\", \"docDate\": \"2022-02-16\", \"pinFl\":  \"12312312312312312312\", \"firstName\":  \"FirstName\", \"lastName\":  \"LastName\", \"middleName\":  \"MiddleName\"} }";

        var senderAccount = new P2pAccount().withType(P2pAccountType.PAN).withCurrency(860).withParams(objectMapper.readTree(paramsString));

        var paramsString2 = "{ \"pan\": \"9860090000000000\", \"bin\": \"986009\"}";

        var recipientAccount = new P2pAccount()
                .withType(P2pAccountType.PAN)
                .withCurrency(860)
                .withParams(objectMapper.readTree(paramsString2));

        P2pPayRequest p2PPayRequest = new P2pPayRequest()
                .withFrom(senderAccount)
                .withTo(recipientAccount)
                .withAgentTranId("123")
                .withAmount(10000L);

        ResponseEntity<P2pResponse> simpleBillResponseEntity = p2pAgentResource.pay(p2PPayRequest);

        P2pResponse simpleBillResponse = responseShouldBeNotNull(simpleBillResponseEntity);

        Assertions.assertThat(simpleBillResponse).isNotNull();
        Assertions.assertThat(simpleBillResponse.getStatusCode()).isNotNull();
        Assertions.assertThat(simpleBillResponse.getStatusCode()).isEqualTo(0);
        assertThat(simpleBillResponse.getStatusMessage()).isEqualTo(TransactionStatus.SUCCESS.name());

        List<Bill> bills = billRepository.findAll();
        assertThat(bills).isNotNull();
        assertThat(bills.size()).isEqualTo(3);
        Optional<Bill> creditBillOpt = bills.stream().filter(bill -> Objects.equals(bill.getType(), RequestType.P2P_CREDIT)).findAny();
        assertThat(creditBillOpt.isPresent()).isTrue();
        Bill creditBill = creditBillOpt.get();
        assertThat(creditBill.getRequestJson()).isNotNull();
        assertThat(creditBill.getRequestJson().get("paymentRef")).isNotNull();
    }

    @Test
    @Transactional
    @WithMockUser("TEST_USER")
    public void shouldCalcCommission1PercentBczHasCommissionAndCommissionFreeIsFalse() throws JsonProcessingException {

        int databaseSizeBeforeCreate = transactionRepository.findAll().size();

//        boolean commissionFree = false;
        long requestAmount = 10000L;
        long expectedCommissionAmount = 100L;

        P2pPayRequest p2PPayRequest = getP2pCommissionBonusPayRequest(null, requestAmount);

        ResponseEntity<P2pResponse> simpleBillResponseEntity = p2pAgentResource.pay(p2PPayRequest);

        P2pResponse simpleBillResponse = responseShouldBeNotNull(simpleBillResponseEntity);

        amountsShouldBeEqualsThisAmounts(1, requestAmount, expectedCommissionAmount, simpleBillResponse);
    }

    @Test
    @Transactional
    @WithMockUser("TEST_USER")
    public void shouldUsePromoCodeAndCommissionFreeEpos() throws JsonProcessingException {

        int databaseSizeBeforeCreate = transactionRepository.findAll().size();

        long requestAmount = 10000L;

        long expectedCommissionAmount = 0L;

        P2pPayRequest p2PPayRequest = getP2pCommissionBonusPayRequest(PROMO_CODE, requestAmount);

        ResponseEntity<P2pResponse> simpleBillResponseEntity = p2pAgentResource.pay(p2PPayRequest);

        P2pResponse simpleBillResponse = responseShouldBeNotNull(simpleBillResponseEntity);

        amountsShouldBeEqualsThisAmounts(0d, requestAmount, expectedCommissionAmount, simpleBillResponse);

        List<Transaction> transactionList = transactionRepository.findAll();
        assertThat(transactionList).hasSize(databaseSizeBeforeCreate + 1);
        Transaction testTransaction = transactionList.get(transactionList.size() - 1);

        assertThat(testTransaction.getPromo()).isEqualTo(PROMO_CODE);
        assertThat(testTransaction.getSenderAmount()).isEqualTo(requestAmount);
        assertThat(testTransaction.getRecipientAmount()).isEqualTo(requestAmount);
    }

    @Test
    @Transactional
    @WithMockUser("TEST_USER")
    public void shouldNotUsePromoCodeAndWithCommissionEpos() throws JsonProcessingException {

        int databaseSizeBeforeCreate = transactionRepository.findAll().size();

        long requestAmount = 10000L;

        long expectedCommissionAmount = 100L;

        P2pPayRequest p2PPayRequest = getP2pCommissionBonusPayRequest(null, requestAmount);

        ResponseEntity<P2pResponse> simpleBillResponseEntity = p2pAgentResource.pay(p2PPayRequest);

        P2pResponse simpleBillResponse = responseShouldBeNotNull(simpleBillResponseEntity);

        amountsShouldBeEqualsThisAmounts(1d, requestAmount, expectedCommissionAmount, simpleBillResponse);

        List<Transaction> transactionList = transactionRepository.findAll();
        assertThat(transactionList).hasSize(databaseSizeBeforeCreate + 1);
        Transaction testTransaction = transactionList.get(transactionList.size() - 1);

        assertThat(testTransaction.getPromo()).isNull();
        assertThat(testTransaction.getSenderAmount()).isEqualTo(requestAmount + expectedCommissionAmount);
        assertThat(testTransaction.getRecipientAmount()).isEqualTo(requestAmount);
    }

    @Test
    @Transactional
    @WithMockUser("TEST_USER")
    public void p2pSenderCommissionAmountNeedEqualsToNil2() throws JsonProcessingException {

        int databaseSizeBeforeCreate = transactionRepository.findAll().size();

        long requestAmount = 10000L;
//        long commissionAmountWithoutBonus = 0;
        long commissionAmount = 0L;

//        saveCommissionBonusP2pParams(senderCommissionUp);

        P2pPayRequest p2PPayRequest = getP2pCommissionBonusPayRequest(PROMO_CODE, requestAmount);

        ResponseEntity<P2pResponse> simpleBillResponseEntity = p2pAgentResource.pay(p2PPayRequest);

        P2pResponse simpleBillResponse = responseShouldBeNotNull(simpleBillResponseEntity);

        amountsShouldBeEqualsThisAmounts(0d, requestAmount, commissionAmount, simpleBillResponse);
    }

    @Test
    @Transactional
    @WithMockUser("TEST_USER")
    public void p2pSenderCommissionAmountNeedCalculateFromRequestAmount() throws JsonProcessingException {
        double senderCommissionUp = 1d;
//        boolean commissionFree = false;
        long requestAmount = 10000L;
//        long commissionAmountWithoutBonus = requestAmount - commissionBonusAmount;
        long commissionAmount = BigDecimal.valueOf((requestAmount * senderCommissionUp) / 100).longValue();

        saveCommissionBonusP2pParams(senderCommissionUp);

        P2pPayRequest p2PPayRequest = getP2pCommissionBonusPayRequest(null, requestAmount);

        ResponseEntity<P2pResponse> simpleBillResponseEntity = p2pAgentResource.pay(p2PPayRequest);

        P2pResponse simpleBillResponse = responseShouldBeNotNull(simpleBillResponseEntity);

        amountsShouldBeEqualsThisAmounts(senderCommissionUp, requestAmount, commissionAmount, simpleBillResponse);
    }

    private void amountsShouldBeEqualsThisAmounts(double senderCommissionUp, long requestAmount, long commissionAmount, P2pResponse p2pResponse) {
        assertThat(p2pResponse.getSender()).isNotNull();
        assertThat(p2pResponse.getSender().getCommission().doubleValue()).isEqualTo(senderCommissionUp);
        assertThat(p2pResponse.getSender().getAmount()).isEqualTo(requestAmount + commissionAmount);
        Assertions.assertThat(p2pResponse.getSender().getCommissionAmount()).isEqualTo(commissionAmount);
    }

    private P2pResponse responseShouldBeNotNull(ResponseEntity<P2pResponse> simpleBillResponseEntity) {
        assertThat(simpleBillResponseEntity).isNotNull();
        assertThat(simpleBillResponseEntity.getBody()).isNotNull();

        P2pResponse simpleBillResponse = simpleBillResponseEntity.getBody();
        assertThat(simpleBillResponse).isNotNull();
        return simpleBillResponse;
    }

    private P2pPayRequest getP2pCommissionBonusPayRequest(String promoCode, long requestAmount) throws JsonProcessingException {

        final String pan1 = PredefinedCardRepo2.REAL_PAN;
        var paramsString = "{ \"pan\": \"" + pan1 + "\", \"exp\": \"1026\" }";

        var senderAccount = new P2pAccount().withType(P2pAccountType.PAN).withCurrency(860).withParams(objectMapper.readTree(paramsString));
//        senderAccount.setCommissionFree(commissionFree);

        final String pan2 = PredefinedCardRepo2.REAL_PAN;
        var paramsString2 = "{ \"pan\": \"" + pan2 + "\", \"exp\": \"1025\" }";

        var recipientAccount = new P2pAccount().withType(P2pAccountType.PAN).withCurrency(860).withParams(objectMapper.readTree(paramsString2));

        P2pPayRequest p2PPayRequest = new P2pPayRequest()
                .withFrom(senderAccount)
                .withTo(recipientAccount)
                .withAgentTranId("123")
                .withAmount(requestAmount);

        if (promoCode != null) {
            p2PPayRequest.withPromo(new Promo().withCode(promoCode));
        }

        return p2PPayRequest;
    }

    private double saveCommissionBonusP2pParams(double senderCommissionUp) {
        P2pCommission p2pCommission0 = getComm(0L, "0%", 0d, 0d, 0d, "0 комиссии");
        P2pCommission p2pCommission1 = getComm(1L, "1% комиссии сверху", senderCommissionUp, 0d, 0d, "1% от плателшика сверха");

        P2pCondition fiveMlnBonusCondition1 = getCond(8L, "^86000966", "5 млн бонус отправител");
        P2pCondition fiveMlnBonusCondition2 = getCond(9L, "^860049045", "5 млн бонус получател");

        getP2pParam(3L, 3, true, P2pProcessorType.UZCARD, P2pProcessorType.UZCARD, "{\"sender\":{\"merchantId\":123, \"terminalId\":234, \"port\": 4444, \"creditType\": \"UNIVERSAL\"}, \"recipient\":{\"merchantId\":123, \"terminalId\":234, \"port\": 4444, \"creditType\": \"UNIVERSAL\"}}",
                fiveMlnBonusCondition1,
                fiveMlnBonusCondition2,
                p2pCommission1,
                p2pCommission0
        );
        return senderCommissionUp;
    }

//    public void prepareP2pParams() {
//        P2pCommission p2pCommission0 = getComm(0L, "0%", 0d, 0d, 0d, "0 комиссии");
//        P2pCommission p2pCommission1 = getComm(1L, "1% комиссии сверху", 1d, 0d, 0d, "1% от плателшика сверха");
//
//        P2pCondition p2pCondition0 = getCond(0L, ".+", "Любая");
//        P2pCondition p2pCondition1 = getCond(1L, "^8600", "Любая Uzcard карта");
//        P2pCondition p2pCondition2 = getCond(2L, "^9860", "Любая Humo карта");
//        P2pCondition p2pCondition3 = getCond(3L, "^860063", "Uzcard Anorbank");
//        P2pCondition p2pCondition4 = getCond(4L, "^860014", "Uzcard Ipak Yuli");
//        P2pCondition p2pCondition5 = getCond(5L, "^22616", "Счет кошелка");
//        P2pCondition p2pCondition6 = getCond(6L, "^986060", "Humo Anorbank");
//        P2pCondition p2pCondition7 = getCond(7L, "^986009", "Humo Asia Alliance");
//
//        P2pParam p2pParam1 = getP2pParam(
//                1L,
//                1,
//                true,
//                P2pProcessorType.UZCARD,
//                P2pProcessorType.UZCARD,
//                "{\"sender\":{\"merchantId\":123, \"terminalId\":234, \"port\": 4444}, \"recipient\":{\"merchantId\":123, \"terminalId\":234, \"port\": 4444}}",
//                p2pCondition1,
//                p2pCondition3,
//                p2pCommission1,
//                p2pCommission0
//        );
//        P2pParam p2pParam2 = getP2pParam(
//                2L,
//                2,
//                true,
//                P2pProcessorType.UZCARD,
//                P2pProcessorType.UZCARD,
//                "{\"sender\":{\"merchantId\":123, \"terminalId\":234, \"port\": 4444}, \"recipient\":{\"merchantId\":123, \"terminalId\":234, \"port\": 4444}}",
//                p2pCondition1,
//                p2pCondition4,
//                p2pCommission1,
//                p2pCommission0
//        );
//    }

    private P2pParam getP2pParam(
            Long id,
            Integer priority,
            Boolean active,
            P2pProcessorType sender_pc,
            P2pProcessorType recipient_pc,
            String value,
            P2pCondition sender_condition,
            P2pCondition recipient_condition,
            P2pCommission sender_percent,
            P2pCommission recipient_percent
    ) {
        P2pParam p2pParam = new P2pParam();
        p2pParam.setId(id);
        p2pParam.setPriority(priority);
        p2pParam.setActive(active);
        p2pParam.setSenderPc(sender_pc);
        p2pParam.setRecipientPc(recipient_pc);
        p2pParam.setValue(value);
        p2pParam.setSenderCondition(sender_condition);
        p2pParam.setRecipientCondition(recipient_condition);
        p2pParam.setSenderPercent(sender_percent);
        p2pParam.setRecipientPercent(recipient_percent);
        return p2pParamRepository.save(p2pParam);
    }

    private P2pCommission getComm(Long id, String name, Double up, Double down, Double fee, String description) {
        P2pCommission p2pCommission = new P2pCommission();
        p2pCommission.setId(id);
        p2pCommission.setName(name);
        p2pCommission.setUp(up);
        p2pCommission.setDown(down);
        p2pCommission.setFee(fee);
        p2pCommission.setDescription(description);
        return p2pCommissionRepository.save(p2pCommission);
    }

    private P2pCondition getCond(Long id, String value, String description) {
        P2pCondition p2pCondition = new P2pCondition();
        p2pCondition.setId(id);
        p2pCondition.setValue(value);
        p2pCondition.setDescription(description);
        return p2pConditionRepository.save(p2pCondition);
    }

    public void testPayTransactionV2(
            Transaction testTransaction,
            Instant beforeTranInstant,
            String agentLogin,
            TransactionStatus status,
            Long requestAmount,
            Long commDown,
            Long recipientAmount,
            String account,
            String senderAccount
    ) {
        testPayTransactionV2(
                testTransaction,
                beforeTranInstant,
                agentLogin,
                status,
                requestAmount,
                commDown,
                recipientAmount,
                account,
                senderAccount,
                P2pProcessorType.UZCARD,
                100L,
                10100L
        );
    }

    public void testPayTransactionV2(
            Transaction testTransaction,
            Instant beforeTranInstant,
            String agentLogin,
            TransactionStatus status,
            Long requestAmount,
            Long commDown,
            Long recipientAmount,
            String recipientAccount,
            String senderAccount,
            P2pProcessorType recipientPc,
            Long commissionUpAmount,
            Long senderAmount
    ) {
        testPayTransactionV2(
                testTransaction,
                beforeTranInstant,
                agentLogin,
                status,
                requestAmount,
                commDown,
                recipientAmount,
                recipientAccount,
                senderAccount,
                recipientPc,
                commissionUpAmount,
                senderAmount,
                P2pProcessorType.NCI
        );
    }

    public void testPayTransactionV2(
            Transaction testTransaction,
            Instant beforeTranInstant,
            String agentLogin,
            TransactionStatus status,
            Long requestAmount,
            Long commissionDownAmount,
            Long recipientAmount,
            String recipientAccount,
            String senderAccount,
            P2pProcessorType recipientPc,
            Long commissionUpAmount,
            Long senderAmount,
            P2pProcessorType senderProcessorType
    ) {
        testCommon(testTransaction, status, requestAmount, beforeTranInstant, agentLogin, commissionDownAmount, commissionUpAmount);

        testRecipient(testTransaction, recipientPc, recipientAccount, recipientAmount, beforeTranInstant);

        testSender(testTransaction, senderProcessorType, senderAccount, senderAmount, beforeTranInstant);
    }

    private void testCommon(
            Transaction testTransaction,
            TransactionStatus status,
            Long requestAmount,
            Instant beforeTranInstant,
            String agentLogin,
            Long commissionDownAmount,
            Long commissionUpAmount
    ) {
        assertThat(testTransaction.getRequestLogin()).isEqualTo(agentLogin);
        assertThat(testTransaction.getRequestAmount()).isEqualTo(requestAmount);
        //        assertThat(testTransaction.getRequestTrId()).isEqualTo(TRAN_ID);
        assertThat(testTransaction.getRequestGroup()).isEqualTo(agentLogin);
        assertThat(testTransaction.getComment()).isNotNull();

        assertThat(testTransaction.getSystemFeeAmount())
                .isEqualTo(
                        Double
                                .valueOf(
                                        testTransaction.getSystemFee() /
                                                100d *
                                                (testTransaction.getRequestAmount() - testTransaction.getSystemCommissionDownAmount())
                                )
                                .longValue()
                );
        assertThat(testTransaction.getSystemFee()).isEqualTo(0.0);
        assertThat(testTransaction.getSystemCommissionDownAmount()).isEqualTo(commissionDownAmount);
        assertThat(testTransaction.getSystemCommissionUpAmount()).isEqualTo(commissionUpAmount);
        assertThat(testTransaction.getAgentFeeAmount()).isEqualTo(0);

        assertThat(testTransaction.getStatus()).isEqualTo(status);
        assertThat(testTransaction.getDate()).isAfterOrEqualTo(beforeTranInstant);
        //        assertThat(testTransaction.getCcyRate()).isEqualTo(cbuService.getCurrencyRate(DummyDataAdapterV3.CURRENCY));
    }

    private void testSender(
            Transaction testTransaction,
            P2pProcessorType senderProcessorType,
            String senderAccount,
            Long senderAmount,
            Instant beforeTranInstant
    ) {
        testSender(testTransaction, senderProcessorType, senderAccount, senderAmount, beforeTranInstant, true);
    }

    private void testSender(
            Transaction testTransaction,
            P2pProcessorType senderProcessorType,
            String senderAccount,
            Long senderAmount,
            Instant beforeTranInstant,
            boolean success
    ) {
        testSender(testTransaction, senderProcessorType, senderAccount, senderAmount, beforeTranInstant, success, Constants.UZS_CURRENCY);
    }

    private void testSender(
            Transaction testTransaction,
            P2pProcessorType senderProcessorType,
            String senderAccount,
            Long senderAmount,
            Instant beforeTranInstant,
            boolean success,
            Integer senderCurrency
    ) {
        //        assertThat(testTransaction.getSenderServiceId()).isEqualTo(1183);//saved mfo in long format for confirm method // TODO - avazxon
        assertThat(testTransaction.getSenderGateway()).isEqualTo(senderProcessorType.name());
        assertThat(testTransaction.getSenderAccount()).isEqualTo(senderAccount);
        assertThat(testTransaction.getSenderAmount()).isEqualTo(senderAmount);
        assertThat(testTransaction.getSenderCcy()).isEqualTo(senderCurrency);
        //        assertThat(testTransaction.getSenderTrId()).isEqualTo(TRAN_ID);
        assertThat(testTransaction.isSenderSuccess()).isEqualTo(success);
        assertThat(testTransaction.getSenderDate()).isAfterOrEqualTo(beforeTranInstant);
        assertThat(testTransaction.getSenderStatus()).isNotBlank();
        assertThat(testTransaction.getSenderStatusText()).isNotEmpty();
        assertThat(testTransaction.getSenderBillId()).isNotNull();
    }

    private void testRecipient(
            Transaction testTransaction,
            P2pProcessorType recipientPc,
            String recipientAccount,
            Long recipientAmount,
            Instant beforeTranInstant
    ) {
        testRecipient(testTransaction, recipientPc, recipientAccount, recipientAmount, beforeTranInstant, true);
    }

    private void testRecipient(
            Transaction testTransaction,
            P2pProcessorType recipientPc,
            String recipientAccount,
            Long recipientAmount,
            Instant beforeTranInstant,
            boolean success
    ) {
        testRecipient(testTransaction, recipientPc, recipientAccount, recipientAmount, beforeTranInstant, success, Constants.UZS_CURRENCY);
    }

    private void testRecipient(
            Transaction testTransaction,
            P2pProcessorType recipientPc,
            String recipientAccount,
            Long recipientAmount,
            Instant beforeTranInstant,
            boolean success,
            Integer recipientCcy
    ) {
        assertThat(testTransaction.getRecipientAccount()).isEqualTo(recipientAccount);
        assertThat(testTransaction.getRecipientAmount()).isEqualTo(recipientAmount);
        assertThat(testTransaction.getRecipientCcy()).isEqualTo(recipientCcy);
        assertThat(testTransaction.getRecipientGateway()).isEqualTo(recipientPc.name());
        assertThat(testTransaction.getRecipientDate()).isAfterOrEqualTo(beforeTranInstant);
        assertThat(testTransaction.getRecipientStatus()).isNotEmpty();
        assertThat(testTransaction.getRecipientStatusText()).isNotEmpty();
        assertThat(testTransaction.isRecipientSuccess()).isEqualTo(success);
        assertThat(testTransaction.getRecipientBillId()).isNotNull();
        assertThat(testTransaction.getRecipientTrId()).isNotEmpty();
        //        assertThat(testTransaction.getRecipientServiceId()).isEqualTo(checkDTO.getRecipientId());
    }
}
