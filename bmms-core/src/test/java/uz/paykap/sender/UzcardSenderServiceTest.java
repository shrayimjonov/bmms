package uz.paykap.sender;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;
import uz.devops.bm.dummy.adapter.DummyDataAdapterV3;
import uz.devops.bmcore.dto.request.CheckRequest;
import uz.devops.bmcore.dto.request.CommonCheckRequest;
import uz.devops.bmcore.dto.request.PayRequest;
import uz.devops.bmcore.dto.response.SimpleBillResponse;
import uz.devops.bmcore.enumeration.PayType;
import uz.javlon.svgate.dto.enumeration.PurposeType;
import uz.paykap.IntegrationTest;
import uz.paykap.adapter.DataAdapterServiceV3;
import uz.paykap.agent.tran.CommonTranServiceV3;
import uz.paykap.config.Constants;
import uz.paykap.domain.*;
import uz.paykap.domain.enumeration.Prop;
import uz.paykap.gateway.uzcard.UzcardDebitGatewayService;
import uz.paykap.repository.*;
import uz.paykap.sender.dto.SvgateParam;
import uz.paykap.sender.impl.UzcardSenderService;
import uz.paykap.service.AgentRecipientService;
import uz.paykap.service.AgentService;
import uz.paykap.service.DepositService;
import uz.paykap.service.GroupSettingService;
import uz.paykap.service.mapper.AgentMapper;
import uz.paykap.web.rest.SharedAgentResourceV3;
import uz.paykap.web.rest.SharedAgentUnregisteredPayResource;
import uz.paykap.web.rest.util.DummyUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static uz.javlon.svgate.service.simulation.PredefinedCardRepo2.*;

@RunWith(SpringRunner.class)
@IntegrationTest
public class UzcardSenderServiceTest {

    public static final String PAN = "0000000000000000";
    public static final String EXP_DATE = "1122";
    public static final String PHONE = "901299592";
    public static final String ACCOUNT = "123";
    public static final long AMOUNT = 500000L;
    public static final String UB_UZCARD = "UbUzcard";
    public static final String AGENT_TRAN_ID = "123456";
    public static final String LE_BAZAR = "LeBazar";

    @Autowired
    private UzcardSenderService uzcardSenderService;
    @Autowired
    private SharedAgentResourceV3 sharedAgentResourceV3;
    @Autowired
    private SharedAgentUnregisteredPayResource unregisteredPayResource;
    @Autowired
    private ForeignPayRepository foreignPayRepository;
    @Autowired
    private RecipientRepository recipientRepository;
    @Autowired
    private AgentService agentService;
    @Autowired
    private AgentMapper agentMapper;
    @Autowired
    private DepositService depositService;
    @Autowired
    private DepositRepository depositRepository;
    @Autowired
    private CommonTranServiceV3 commonTranServiceV3;
    @Autowired
    private AgentRepository agentRepository;
    @Autowired
    private AgentRecipientRepository agentRecipientRepository;
    @Autowired
    private DataAdapterServiceV3 dataAdapterServiceV3;
    @Autowired
    private GroupSettingService groupSettingService;
    @Autowired
    private AgentRecipientService agentRecipientService;
    @Autowired
    private SenderRepository senderRepository;
    @Autowired
    private SettingRepository settingRepository;
    @Autowired
    private TransactionRepository transactionRepository;

    private void registerSenderUzcard() {
        Sender sender = new Sender();
        sender
                .active(true)
                .name("Uzcard")
                .ord(1L)
                .payType(PayType.UZCARD)
                .springBeanName(UzcardSenderService.UZCARD_SENDER_BEAN_NAME)
                .agent(agentRepository.findTop1ByShortName(DummyDataAdapterV3.DUMMY_AGENT).get())
        //
        ;
        senderRepository.save(sender);
    }

    private SimpleBillResponse checkRequest(boolean registerSender, boolean setEposData) {
        return checkRequest(registerSender, setEposData, false);
    }

    @Autowired
    private ObjectMapper objectMapper;

    @SneakyThrows
    private SimpleBillResponse checkRequest(boolean registerSender, boolean setEposData, boolean setSetting) {

        Long settingId = null;

        if (setSetting) {
            Setting setting = new Setting()
                    .active(true)
                    .ord(1)
                    .shortName("Uzcard, Humo and Expression setting")
                    .addProp(new SettingProperty().key(Prop.UZCARD_MERCHANT).value("222444555"))
                    .addProp(new SettingProperty().key(Prop.UZCARD_TERMINAL).value("333666777"))
                    .addProp(new SettingProperty().key(Prop.HUMO_MERCHANT).value("ZZZ2222LLL111"))
                    .addProp(new SettingProperty().key(Prop.HUMO_TERMINAL).value("000EEE333"))
                    .addProp(new SettingProperty().key(Prop.EPOS_EX).value("#params['pan'] matches '^98606001.*' ? '1111,1111' : (#params['pan'] matches '^98606002.*' ? '2222,2222' : null)"))
                    //
                    ;
            setting = settingRepository.save(setting);
            settingId = setting.getId();
        }

        DummyUtils.prepareDummyData(dataAdapterServiceV3, groupSettingService, recipientRepository, agentRecipientService, settingId);

        if (registerSender) {
            registerSenderUzcard();
        }

        depositService.increaseAgentDeposit(DummyDataAdapterV3.DUMMY_AGENT, 10000000L);

        Optional<Recipient> recipient = recipientRepository.findTop1ByShortName(DummyDataAdapterV3.DUMMY_RECIPIENT_UZS);
        SvgateParam svgateParam = new SvgateParam();
        svgateParam.setPaymentPurpose(PurposeType.EWALLET);
        Map<String, Object> objectMap = new HashMap<>();
        objectMap.put("svgate", svgateParam);
        recipient.get().setJsonParams(objectMapper.writeValueAsString(objectMap));
        recipientRepository.save(recipient.get());

        assertThat(recipient.isPresent()).isTrue();
        Long recipientId = recipient.get().getId();

        if (setEposData) {
            setEposMerchantAndTerminal(recipientId);
        }

        CommonCheckRequest simpleCheckDTO = new CheckRequest();
        simpleCheckDTO.setAmount(AMOUNT);
        simpleCheckDTO.setAccount(ACCOUNT);
        simpleCheckDTO.setRecipientId(recipientId);
        simpleCheckDTO.setAgentTranId(AGENT_TRAN_ID);

        Map<String, Object> map = new HashMap<>();
        map.put(Constants.PAN_PARAM, PAN);
        map.put(Constants.EXPIRY_PARAM, EXP_DATE);
        map.put("phone", PHONE);
        simpleCheckDTO.setParams(map);

        return commonTranServiceV3.check(simpleCheckDTO);
    }

    private void setEposMerchantAndTerminal(Long recipientId) {
        Optional<AgentRecipient> agentRecipient = agentRecipientRepository.findTop1ByAgent_ShortNameAndRecipient_Id(DummyDataAdapterV3.DUMMY_AGENT, recipientId);
        assertThat(agentRecipient).isNotNull();
        agentRecipient.get().setEposMerchant("12345");
        agentRecipient.get().setEposTerminal("54321");

        agentRecipientRepository.save(agentRecipient.get());
    }

    private SimpleBillResponse payRequest() {
        SimpleBillResponse simpleBillResponse = checkRequest(true, true);
        PayRequest payRequest = new PayRequest();
        payRequest.setBillId(simpleBillResponse.getId());

        Map<String, Object> payMap = new HashMap<>();
        payMap.put("otp", "123321");
        payMap.put("company_inn", "123456");
        payMap.put("company_name", LE_BAZAR);
        payRequest.setParams(payMap);

        Agent agentd = agentMapper.toEntity(agentService.getAgentOrCreate("userTest"));

        Deposit deposit = new Deposit()
                .overdraftLimit(0L)
                .balance(0L)
                .balanceCcy(860)
                .bookedBalance(0L)
                .limitInform(0L)
                .limitDisable(0L)
                .contactGroup("12")
                .blocked(false)
                .unlimit(true)
                .agent(agentd);
        depositRepository.save(deposit);

//        Deposit leBazarDepositBeforeDecrease = depositRepository.findTop1ByAgent_ShortName(LE_BAZAR).get();

        return unregisteredPayResource.unregisteredPay(payRequest).getBody();
    }

    @Test
    @Transactional
    @WithMockUser(username = DummyDataAdapterV3.DUMMY_AGENT)
    public void canAgentProceed() {
        PayRequest payDTO = new PayRequest();
        payDTO.amount(10000L);

        HashMap<String, Object> params = new HashMap<>();
        params.put(UzcardDebitGatewayService.PAN_PARAM_NAME, DEFAULT_PAN);
        params.put(UzcardDebitGatewayService.EXP_PARAM_NAME, DEFAULT_EXPIRY);

        payDTO.setParams(params);
        boolean test = uzcardSenderService.canAgentProceed(PayType.UZCARD, "test", payDTO, 10000L).isSuccess();

        assertThat(test).isTrue();
    }

    @Test
    @Transactional
    @WithMockUser(username = DummyDataAdapterV3.DUMMY_AGENT)
    public void debitSuccess() {

        SimpleBillResponse checkDTO = checkRequest(true, true);

        assertThat(checkDTO).isNotNull();
        assertThat(checkDTO.isSuccess()).isTrue();

        PayRequest payDTO = new PayRequest();
        payDTO.amount(100000L);
        payDTO.setBillId(checkDTO.getId());
        payDTO.setType(PayType.UZCARD);

        HashMap<String, Object> params = new HashMap<>();
        params.put(UzcardDebitGatewayService.PAN_PARAM_NAME, DEFAULT_PAN);
        params.put(UzcardDebitGatewayService.EXP_PARAM_NAME, DEFAULT_EXPIRY);

        payDTO.setParams(params);

        SimpleBillResponse pay = commonTranServiceV3.pay(payDTO);

        assertThat(pay).isNotNull();
        assertThat(pay.isSuccess()).isTrue();
    }

    @Test
    @Transactional
    @WithMockUser(username = DummyDataAdapterV3.DUMMY_AGENT)
    public void debitUsingPcAndPanSuccess() {

        SimpleBillResponse checkDTO = checkRequest(true, true);

        assertThat(checkDTO).isNotNull();
        assertThat(checkDTO.isSuccess()).isTrue();

        PayRequest payDTO = new PayRequest();
        payDTO.amount(100000L);
        payDTO.setBillId(checkDTO.getId());
        payDTO.setType(PayType.PC);

        HashMap<String, Object> params = new HashMap<>();
        params.put(UzcardDebitGatewayService.PAN_PARAM_NAME, REAL_PAN);
        params.put(UzcardDebitGatewayService.EXP_PARAM_NAME, DEFAULT_EXPIRY);

        payDTO.setParams(params);

        SimpleBillResponse pay = commonTranServiceV3.pay(payDTO);

        assertThat(pay).isNotNull();
        assertThat(pay.isSuccess()).isTrue();
    }

    @Test
    @Transactional
    @WithMockUser(username = DummyDataAdapterV3.DUMMY_AGENT)
    public void debitUsingPcAndTokenSuccess() {

        SimpleBillResponse checkDTO = checkRequest(true, true);

        assertThat(checkDTO).isNotNull();
        assertThat(checkDTO.isSuccess()).isTrue();

        PayRequest payDTO = new PayRequest();
        payDTO.amount(100000L);
        payDTO.setBillId(checkDTO.getId());
        payDTO.setType(PayType.PC);

        HashMap<String, Object> params = new HashMap<>();
        params.put(UzcardDebitGatewayService.TOKEN_PARAM_NAME, DEFAULT_TOKEN);

        payDTO.setParams(params);

        SimpleBillResponse pay = commonTranServiceV3.pay(payDTO);

        assertThat(pay).isNotNull();
        assertThat(pay.isSuccess()).isTrue();
    }

    @Test
    @Transactional
    @WithMockUser(username = DummyDataAdapterV3.DUMMY_AGENT)
    public void debitSuccessWithSettingHumo() {

        SimpleBillResponse checkDTO = checkRequest(true, true, true);

        assertThat(checkDTO).isNotNull();
        assertThat(checkDTO.isSuccess()).isTrue();

        PayRequest payDTO = new PayRequest();
        payDTO.amount(100000L);
        payDTO.setBillId(checkDTO.getId());
        payDTO.setType(PayType.HUMO);

        HashMap<String, Object> params = new HashMap<>();
        params.put(UzcardDebitGatewayService.PAN_PARAM_NAME, "9860600000000000");
        params.put(UzcardDebitGatewayService.EXP_PARAM_NAME, DEFAULT_EXPIRY);

        payDTO.setParams(params);

        SimpleBillResponse pay = commonTranServiceV3.pay(payDTO);

        assertThat(pay).isNotNull();
        assertThat(pay.isSuccess()).isTrue();
    }

    @Test
    @Transactional
    @WithMockUser(username = DummyDataAdapterV3.DUMMY_AGENT)
    public void debitSuccessWithSettingExpression1111() {

        int databaseSizeBeforeCreate = transactionRepository.findAll().size();

        SimpleBillResponse checkDTO = checkRequest(true, true, true);

        assertThat(checkDTO).isNotNull();
        assertThat(checkDTO.isSuccess()).isTrue();

        PayRequest payDTO = new PayRequest();
        payDTO.amount(100000L);
        payDTO.setBillId(checkDTO.getId());
        payDTO.setType(PayType.HUMO);

        HashMap<String, Object> params = new HashMap<>();
        params.put(UzcardDebitGatewayService.PAN_PARAM_NAME, "9860600000000000"); // 98606001 -> 1111,1111
        params.put(UzcardDebitGatewayService.EXP_PARAM_NAME, DEFAULT_EXPIRY);

        payDTO.setParams(params);

        SimpleBillResponse pay = commonTranServiceV3.pay(payDTO);

        assertThat(pay).isNotNull();
        assertThat(pay.isSuccess()).isTrue();

        List<Transaction> transactionList = transactionRepository.findAll();
        assertThat(transactionList).hasSize(databaseSizeBeforeCreate + 1);
        Transaction testTransaction = transactionList.get(transactionList.size() - 1);
        assertThat(testTransaction.getEposMerchant()).isEqualTo("1111");
        assertThat(testTransaction.getEposTerminal()).isEqualTo("1111");
    }

    @Test
    @Transactional
    @WithMockUser(username = DummyDataAdapterV3.DUMMY_AGENT)
    public void debitSuccessWithSettingExpressionNotFound() {

        int databaseSizeBeforeCreate = transactionRepository.findAll().size();

        SimpleBillResponse checkDTO = checkRequest(true, true, true);

        assertThat(checkDTO).isNotNull();
        assertThat(checkDTO.isSuccess()).isTrue();

        PayRequest payDTO = new PayRequest();
        payDTO.amount(100000L);
        payDTO.setBillId(checkDTO.getId());
        payDTO.setType(PayType.HUMO);

        HashMap<String, Object> params = new HashMap<>();
        params.put(UzcardDebitGatewayService.PAN_PARAM_NAME, "9860600000000000"); // 98606003 -> ZZZ2222LLL111,000EEE333 - default from Humo params
        params.put(UzcardDebitGatewayService.EXP_PARAM_NAME, DEFAULT_EXPIRY);

        payDTO.setParams(params);

        SimpleBillResponse pay = commonTranServiceV3.pay(payDTO);

        assertThat(pay).isNotNull();
        assertThat(pay.isSuccess()).isTrue();

        List<Transaction> transactionList = transactionRepository.findAll();
        assertThat(transactionList).hasSize(databaseSizeBeforeCreate + 1);
        Transaction testTransaction = transactionList.get(transactionList.size() - 1);
        assertThat(testTransaction.getEposMerchant()).isEqualTo("ZZZ2222LLL111");
        assertThat(testTransaction.getEposTerminal()).isEqualTo("000EEE333");
    }

    @Test
    @Transactional
    @WithMockUser(username = DummyDataAdapterV3.DUMMY_AGENT)
    public void debitSuccessWithSettingExpression2222() {

        int databaseSizeBeforeCreate = transactionRepository.findAll().size();

        SimpleBillResponse checkDTO = checkRequest(true, true, true);

        assertThat(checkDTO).isNotNull();
        assertThat(checkDTO.isSuccess()).isTrue();

        PayRequest payDTO = new PayRequest();
        payDTO.amount(100000L);
        payDTO.setBillId(checkDTO.getId());
        payDTO.setType(PayType.HUMO);

        HashMap<String, Object> params = new HashMap<>();
        params.put(UzcardDebitGatewayService.PAN_PARAM_NAME, "9860600000000000"); // 98606002 -> 2222,2222
        params.put(UzcardDebitGatewayService.EXP_PARAM_NAME, DEFAULT_EXPIRY);

        payDTO.setParams(params);

        SimpleBillResponse pay = commonTranServiceV3.pay(payDTO);

        assertThat(pay).isNotNull();
        assertThat(pay.isSuccess()).isTrue();

        List<Transaction> transactionList = transactionRepository.findAll();
        assertThat(transactionList).hasSize(databaseSizeBeforeCreate + 1);
        Transaction testTransaction = transactionList.get(transactionList.size() - 1);
        assertThat(testTransaction.getEposMerchant()).isEqualTo("2222");
        assertThat(testTransaction.getEposTerminal()).isEqualTo("2222");
    }

    @Test
    @Transactional
    @WithMockUser(username = DummyDataAdapterV3.DUMMY_AGENT)
    public void debitSuccessWithSettingUzcard() {

        SimpleBillResponse checkDTO = checkRequest(true, true, true);

        assertThat(checkDTO).isNotNull();
        assertThat(checkDTO.isSuccess()).isTrue();

        PayRequest payDTO = new PayRequest();
        payDTO.amount(100000L);
        payDTO.setBillId(checkDTO.getId());
        payDTO.setType(PayType.UZCARD);

        HashMap<String, Object> params = new HashMap<>();
        params.put(UzcardDebitGatewayService.PAN_PARAM_NAME, DEFAULT_PAN);
        params.put(UzcardDebitGatewayService.EXP_PARAM_NAME, DEFAULT_EXPIRY);

        payDTO.setParams(params);

        SimpleBillResponse pay = commonTranServiceV3.pay(payDTO);

        assertThat(pay).isNotNull();
        assertThat(pay.isSuccess()).isTrue();
    }

    @Test
    @Transactional
    @WithMockUser(username = DummyDataAdapterV3.DUMMY_AGENT)
    public void debitShouldFailWhenEposDataNotSet() {

        SimpleBillResponse checkDTO = checkRequest(true, false);

        assertThat(checkDTO).isNotNull();
        assertThat(checkDTO.isSuccess()).isTrue();

        PayRequest payDTO = new PayRequest();
        payDTO.amount(100000L);
        payDTO.setBillId(checkDTO.getId());
        payDTO.setType(PayType.UZCARD);

        HashMap<String, Object> params = new HashMap<>();
        params.put(UzcardDebitGatewayService.PAN_PARAM_NAME, DEFAULT_PAN);
        params.put(UzcardDebitGatewayService.EXP_PARAM_NAME, DEFAULT_EXPIRY);

        payDTO.setParams(params);

        SimpleBillResponse pay = commonTranServiceV3.pay(payDTO);

        assertThat(pay).isNotNull();
        assertThat(pay.isSuccess()).isTrue();
    }

    @Test
    public void reverse() {
    }
}
