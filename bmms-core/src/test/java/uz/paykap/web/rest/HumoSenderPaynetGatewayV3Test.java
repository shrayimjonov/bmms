package uz.paykap.web.rest;

import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;
import uz.devops.bm.paynet.config.PaynetConstants;
import uz.devops.bmcore.dto.request.CheckRequest;
import uz.devops.bmcore.dto.request.PayRequest;
import uz.devops.bmcore.dto.response.SimpleBillResponse;
import uz.devops.bmcore.enumeration.Lang;
import uz.devops.bmcore.enumeration.PayType;
import uz.devops.bmcore.util.FileUtil;
import uz.javlon.commons.exception.CodedException;
import uz.paykap.IntegrationTest;
import uz.paykap.adapter.DataAdapterServiceV3;
import uz.paykap.domain.*;
import uz.paykap.repository.*;
import uz.paykap.sender.impl.HumoSenderService;
import uz.paykap.service.BillService;
import uz.paykap.web.rest.errors.BmTransactionException;

import javax.transaction.Transactional;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@RunWith(SpringRunner.class)
@IntegrationTest
public class HumoSenderPaynetGatewayV3Test {

    //    public static final String DUMMY_ACCOUNT = "974415655";
//    public static final long DUMMY_AMOUNT = 3000000L;
//    public static final String TRAN_ID = "TRAN_ID";
//    public static final String VALID_PAN = "9860090000000000";
    public static final String EXPIRE = "2403";
    public static final String DEBIT_PAN = "9860090000000000";
    public static final String BALANCE_EMPTY_PAN = "9860990000000000";
    public static final String UCELL_SHORT_NAME = FileUtil.cleanUpFileName("UCell");

    @Autowired
    private SharedAgentResourceV3 sharedAgentResourceV3;

    @Autowired
    private DataAdapterServiceV3 dataAdapterServiceV3;

    @Autowired
    private RecipientRepository recipientRepository;

    @Autowired
    private AgentRepository agentRepository;

    @Autowired
    private BillService billService;

    @Autowired
    private BillRepository billRepository;

    @Autowired
    private AgentRecipientRepository agentRecipientRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    @WithMockUser(value = "userTest")
    @Transactional
    public void testCheck() {
        int databaseBillSizeBefore = billService.findAll().size();

        SimpleBillResponse simpleBillDTO = check();

        assertThat(simpleBillDTO).isNotNull();
        assertThat(databaseBillSizeBefore).isNotEqualTo(billService.findAll().size());

        Optional<Bill> billOptional = billRepository.findById(simpleBillDTO.getId());

        Optional<Transaction> transactionOptional = transactionRepository.findTop1ById(simpleBillDTO.getId());

        assertThat(transactionOptional.isPresent()).isTrue();

//        assertThat(transactionOptional.get().getId()).isEqualTo("5f081042d51e9b1465987d19");
        assertThat(transactionOptional.get().isRecipientSuccess()).isTrue();
    }

    @Ignore("Zafar & Abdurahim, getRealCard returns always same PAN, make it more 'live' ")
    @Test
    @WithMockUser(value = "userTest")
    @Transactional
    public void testPay() {

        int databaseBillSizeBefore = billService.findAll().size();

        SimpleBillResponse simpleBillDTO = check();
        assertThat(simpleBillDTO).isNotNull();
        assertThat(databaseBillSizeBefore).isNotEqualTo(billService.findAll().size());

//        Optional<Bill> billOptional = billRepository.findById(simpleBillDTO.getId());
        Optional<Transaction> billOptional = transactionRepository.findTop1ById(simpleBillDTO.getId());

        assertThat(billOptional.isPresent()).isTrue();

//        assertThat(billOptional.get().getExtId()).isEqualTo("5f081042d51e9b1465987d19");
        assertThat(billOptional.get().isRecipientSuccess()).isTrue();


        ResponseEntity<SimpleBillResponse> simpleBillDTO1 = pay(simpleBillDTO.getId(), DEBIT_PAN, EXPIRE);

        assertThat(simpleBillDTO1.getBody()).isNotNull();
        assertThat(simpleBillDTO1.getBody().isSuccess()).isTrue();


        Optional<Bill> billOptional1 = billRepository.findTop1ByRecipientShortNameAndSuccess(HumoSenderService.HUMO_DEBIT_CONFIRM, true);
        assertThat(billOptional1.isPresent()).isTrue();

    }

    @Test
    @WithMockUser(value = "userTest")
    @Transactional
    public void testPayBalanceEmpty() {

        int databaseBillSizeBefore = billService.findAll().size();

        SimpleBillResponse simpleBillDTO = check();
        assertThat(simpleBillDTO).isNotNull();
        assertThat(databaseBillSizeBefore).isNotEqualTo(billService.findAll().size());

//        Optional<Bill> billOptional = billRepository.findById(simpleBillDTO.getId());
        Optional<Transaction> billOptional = transactionRepository.findTop1ById(simpleBillDTO.getId());

        assertThat(billOptional.isPresent()).isTrue();

//        assertThat(billOptional.get().getExtId()).isEqualTo("5f081042d51e9b1465987d19");
        assertThat(billOptional.get().isRecipientSuccess()).isTrue();

        assertThatThrownBy(() -> pay(simpleBillDTO.getId(), BALANCE_EMPTY_PAN, EXPIRE)).isInstanceOf(BmTransactionException.class).hasMessageContaining("INSUFFICIENT_FUNDS_ON_SENDER : Недостаточно средств на депозите");
    }

    //    @Test()
    @WithMockUser(value = "userTest")
    @Transactional
    public void testPayExpireNull() {

        int databaseBillSizeBefore = billService.findAll().size();

        SimpleBillResponse simpleBillDTO = check();
        assertThat(simpleBillDTO).isNotNull();
        assertThat(databaseBillSizeBefore).isNotEqualTo(billService.findAll().size());

//        Optional<Bill> billOptional = billRepository.findById(simpleBillDTO.getId());
        Optional<Transaction> billOptional = transactionRepository.findTop1ById(simpleBillDTO.getId());

        assertThat(billOptional.isPresent()).isTrue();

//        assertThat(billOptional.get().getExtId()).isEqualTo("5f081042d51e9b1465987d19");
        assertThat(billOptional.get().isRecipientSuccess()).isTrue();

        assertThatThrownBy(() -> pay(simpleBillDTO.getId(), DEBIT_PAN, null)).isInstanceOf(CodedException.class).hasMessageContaining("Не правильные параметры: invalid_params : expire");


    }

    @Test()
    @WithMockUser(value = "userTest")
    @Transactional
    public void testPayPanNull() {

        int databaseBillSizeBefore = billService.findAll().size();

        SimpleBillResponse simpleBillDTO = check();
        assertThat(simpleBillDTO).isNotNull();
        assertThat(databaseBillSizeBefore).isNotEqualTo(billService.findAll().size());

//        Optional<Bill> billOptional = billRepository.findById(simpleBillDTO.getId());
        Optional<Transaction> billOptional = transactionRepository.findTop1ById(simpleBillDTO.getId());

        assertThat(billOptional.isPresent()).isTrue();

//        assertThat(billOptional.get().getExtId()).isEqualTo("5f081042d51e9b1465987d19");
        assertThat(billOptional.get().isRecipientSuccess()).isTrue();

        assertThatThrownBy(() -> pay(simpleBillDTO.getId(), null, EXPIRE)).isInstanceOf(BmTransactionException.class).hasMessageContaining("INVALID_PARAMS : senderId 'null' not supported");


    }

//    @Test
//    @Transactional
//    public void testGetRealPan(){
//        GetRealCardRequestParams getRealCardRequestParams = new GetRealCardRequestParams();
//        getRealCardRequestParams.setCard(fakePan);
//
//        GetRealCardRequest getRealCardRequest = new GetRealCardRequest();
//        getRealCardRequest.setConnectionInfo(connectionInfo);
//        getRealCardRequest.setParameters(getRealCardRequestParams);
//
//
//        GetRealCardRequestBody getRealCardRequestBody = new GetRealCardRequestBody();
//        getRealCardRequestBody.setQueryCardRequest(getRealCardRequest);
//
//        GetRealCardRequestEnv cardRequest = new GetRealCardRequestEnv();
//        cardRequest.setBody(getRealCardRequestBody);
//
//        try {
//            GetRealCardResponseEnv getRealCardResponseEnv = issuingService.getRealCard(cardRequest);
//            return getRealCardResponseEnv.getBody().getQueryCardResponse().getRealCardDetails().getRealCardNumber();
//        } catch (Exception e) {
//            log.warn("Exception when get real pan : {}", e.getMessage());
//            throw new CodedException(ProblemEnum.INTERNAL_ERROR, "error when get real pan");
//        }
//
//    }

    private SimpleBillResponse check() {
        int databaseBillSizeBefore = billService.findAll().size();

        Recipient recipient = createAndGetRecipientUms();

        Agent agent = createAgent();

        AgentRecipient agentRecipient = createAgentRecipient(recipient, agent);

        return checkRequest(recipient);
    }

    private ResponseEntity<SimpleBillResponse> pay(Long simpleBillDTOId, String pan, String expire) {
        Map<String, Object> params = new HashMap<>(2);
        params.put("pan", pan);

        PayRequest payRequest = new PayRequest();
        payRequest.setParams(params);
        payRequest.setType(PayType.PC);
        payRequest.setBillId(simpleBillDTOId);

        return sharedAgentResourceV3.payV3(payRequest);
    }

    private Recipient createAndGetRecipientUms() {
//        commonDataAdapter.importData();

        dataAdapterServiceV3.sync(PaynetConstants.PAYNET_RPC_DATA_ADAPTER);
        return recipientRepository.findTop1ByShortName(UCELL_SHORT_NAME).get();

    }

    private Agent createAgent() {
        Set<Integer> ccy = new HashSet<>();
        ccy.add(860);
        Agent agent = new Agent()
                .shortName("userTest")
                .amountRatio(1)

                .apiKey("123")
                .currencies(ccy)
                .active(true)
                .titles(getI18ns("userTest", "userTest", "userTest"));

        return agentRepository.save(agent);
    }

    private AgentRecipient createAgentRecipient(Recipient recipient, Agent agent) {
        AgentRecipient agentRecipient = new AgentRecipient()
                .agentCommission(0.0)
                .agentFee(0.0)
                .active(true)
                .eposMerchant("010950461213101")
                .eposTerminal("09610001")
                .recipient(recipient)
                .agent(agent);
        return agentRecipientRepository.save(agentRecipient);
    }

    private SimpleBillResponse checkRequest(Recipient recipient) {

        CheckRequest checkRequest = new CheckRequest();
        checkRequest.setRecipientId(recipient.getId());// recipient UMS id in billms
        checkRequest.setAmount(50000L);
        checkRequest.setAccount("12345");// if recipient UMS  account-> phone number

        ResponseEntity<SimpleBillResponse> simpleBillDTO = sharedAgentResourceV3.checkV3(checkRequest);
        return simpleBillDTO.getBody();
    }


    private Set<I18n> getI18ns(String en, String ru, String uz) {
        en = en.length() > 62 ? en.substring(0, 60) : en;
        ru = ru.length() > 62 ? ru.substring(0, 60) : ru;
        uz = uz.length() > 62 ? uz.substring(0, 60) : uz;
        HashSet<I18n> i18ns = new HashSet<>(3);
        i18ns.add(new I18n().lang(Lang.EN).text(en));
        i18ns.add(new I18n().lang(Lang.RU).text(ru));
        i18ns.add(new I18n().lang(Lang.UZ).text(uz));
        return i18ns;
    }
}
