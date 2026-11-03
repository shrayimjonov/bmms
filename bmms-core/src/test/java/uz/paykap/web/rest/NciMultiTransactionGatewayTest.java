package uz.paykap.web.rest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.ResponseEntity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;
import uz.devops.bmcore.dto.request.CheckRequest;
import uz.devops.bmcore.dto.request.PayRequest;
import uz.devops.bmcore.dto.response.SimpleBillResponse;
import uz.devops.bmcore.enumeration.Lang;
import uz.devops.bmcore.enumeration.PayType;
import uz.devops.bmcore.util.FileUtil;
import uz.paykap.IntegrationTest;
import uz.paykap.adapter.DataAdapterServiceV3;
import uz.paykap.adapter.nci.NciMultiTranDataAdapter;
import uz.paykap.domain.*;
import uz.paykap.repository.*;
import uz.paykap.service.BillService;
import uz.paykap.service.abs.AbstractAbsService;
import uz.paykap.service.abs.response.MultiTransactionResultDTO;
import uz.paykap.service.dto.result.BmResultData;
import uz.paykap.service.p2p.dto.NciTranDTO;

import javax.transaction.Transactional;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@IntegrationTest
@Transactional
public class NciMultiTransactionGatewayTest {

    public static final String DEBIT_PAN = "9860330000000000";
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

    @MockBean
    @Autowired
    private AbstractAbsService nciHelperService;

    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private SharedAgentUnregisteredPayResource sharedAgentUnregisteredPayResource;

    @Test
    @WithMockUser(value = "userTest")
    @Transactional
    public void testPay() {
        int databaseBillSizeBefore = billService.findAll().size();
        when(nciHelperService.doMultiTransaction(ArgumentMatchers.any())).thenReturn(new BmResultData<>(Collections.singletonList(new MultiTransactionResultDTO())));
        SimpleBillResponse simpleBillDTO = check();
        assertThat(simpleBillDTO).isNotNull();
        assertThat(databaseBillSizeBefore).isNotEqualTo(billService.findAll().size());

        Optional<Transaction> billOptional = transactionRepository.findTop1ById(simpleBillDTO.getId());

        assertThat(billOptional.isPresent()).isTrue();

        assertThat(billOptional.get().isRecipientSuccess()).isTrue();

        ResponseEntity<SimpleBillResponse> simpleBillDTO1 = pay(simpleBillDTO.getId(), DEBIT_PAN);

        assertThat(simpleBillDTO1.getBody()).isNotNull();
        assertThat(simpleBillDTO1.getBody().isSuccess()).isTrue();

        Optional<Bill> billOptional1 = billRepository.findTop1ByRecipientShortNameAndSuccess(NciMultiTranDataAdapter.NCI_DEBIT_RECIPIENT_SHORT_NAME, true);
        assertThat(billOptional1.isPresent()).isTrue();
    }

    @Test
    @WithMockUser(value = "userTest")
    @Transactional
    public void testUnRegisteredPay() {
        int databaseBillSizeBefore = billService.findAll().size();
        MultiTransactionResultDTO resultDTO = new MultiTransactionResultDTO();
        resultDTO.setTransId("121212");

        when(nciHelperService.doMultiTransaction(ArgumentMatchers.any())).thenReturn(new BmResultData<>(Collections.singletonList(resultDTO)));
        SimpleBillResponse simpleBillDTO = unRegisteredCheck();
        assertThat(simpleBillDTO).isNotNull();
        assertThat(databaseBillSizeBefore).isNotEqualTo(billService.findAll().size());

        Optional<Transaction> billOptional = transactionRepository.findTop1ById(simpleBillDTO.getId());

        assertThat(billOptional.isPresent()).isTrue();

        assertThat(billOptional.get().isRecipientSuccess()).isTrue();

        ResponseEntity<SimpleBillResponse> simpleBillDTO1 = unRegisteredPay(simpleBillDTO.getId(), DEBIT_PAN);

        assertThat(simpleBillDTO1.getBody()).isNotNull();
        assertThat(simpleBillDTO1.getBody().isSuccess()).isTrue();

        Optional<Bill> billOptional1 = billRepository.findTop1ByRecipientShortNameAndSuccess(NciMultiTranDataAdapter.NCI_DEBIT_RECIPIENT_SHORT_NAME, true);
        assertThat(billOptional1.isPresent()).isTrue();
    }


    @SneakyThrows
    private SimpleBillResponse check() {
        int databaseBillSizeBefore = billService.findAll().size();

        Recipient recipient = createAndGetRecipientNciMultiTran();

        Agent agent = createAgent();

        AgentRecipient agentRecipient = createAgentRecipient(recipient, agent);

        return checkRequest(recipient);
    }

    @SneakyThrows
    private SimpleBillResponse unRegisteredCheck() {
        int databaseBillSizeBefore = billService.findAll().size();

        Recipient recipient = createAndGetRecipientNciMultiTran();

        Agent agent = createAgent();

        AgentRecipient agentRecipient = createAgentRecipient(recipient, agent);

        return unRegisteredCheckRequest(recipient);
    }

    private ResponseEntity<SimpleBillResponse> pay(Long simpleBillDTOId, String pan) {
        Map<String, Object> params = new HashMap<>(2);
        params.put("pan", pan);

        PayRequest payRequest = new PayRequest();
        payRequest.setParams(params);
        payRequest.setType(PayType.HUMO);
        payRequest.setBillId(simpleBillDTOId);

        return sharedAgentResourceV3.payV3(payRequest);
    }

    private ResponseEntity<SimpleBillResponse> unRegisteredPay(Long simpleBillDTOId, String pan) {
        Map<String, Object> map = new HashMap<>();
        map.put("otp", "123321");
        PayRequest payRequest = new PayRequest();
        payRequest.setType(PayType.HUMO);
        payRequest.setBillId(simpleBillDTOId);
        payRequest.setParams(map);
        return sharedAgentUnregisteredPayResource.unregisteredPay(payRequest);
    }

    private Recipient createAndGetRecipientNciMultiTran() {
        dataAdapterServiceV3.sync(NciMultiTranDataAdapter.ADAPTER_NAME);
        return recipientRepository.findTop1ByShortName(NciMultiTranDataAdapter.NCI_DEBIT_RECIPIENT_SHORT_NAME).get();
    }

    private Agent createAgent() {
        Set<Integer> ccy = new HashSet<>();
        ccy.add(860);
        Agent agent = new Agent()
                .shortName("userTest")
                .amountRatio(100)
                .apiKey("123")
                .currencies(ccy)
                .active(true)
                .titles(getI18ns("userTest", "userTest", "userTest"));

        return agentRepository.save(agent);
    }

    private AgentRecipient createAgentRecipient(Recipient recipient, Agent agent) throws JsonProcessingException {
        List<NciTranDTO> list = new ArrayList<>();
        NciTranDTO tranDTO = new NciTranDTO();
        tranDTO.setAccCL("acc_cl1");
        tranDTO.setAccCo("acc_co2");
        tranDTO.setOrder(3);
        tranDTO.setPurpose("purpose1");
        tranDTO.setSum("10000");
        tranDTO.setAmountPart(-1L);

        NciTranDTO dto = new NciTranDTO();
        dto.setAccCL("acc_cl2");
        dto.setAccCo("acc_co2");
        dto.setOrder(2);
        dto.setPurpose("purpose2");
        dto.setSum("10001");
        dto.setAmountPart(-99L);
        list.add(tranDTO);
        list.add(dto);
        var jsonParams = objectMapper.writeValueAsString(list);

        AgentRecipient agentRecipient = new AgentRecipient()
                .agentCommission(1d)
                .agentFee(0.0)
                .active(true)
                .eposMerchant("010950461213101")
                .eposTerminal("09610001")
                .recipient(recipient)
                .systemCommissionUp(-1d)
//                .jsonParams(jsonParams)
                .agent(agent);
        return agentRecipientRepository.save(agentRecipient);
    }

    private SimpleBillResponse checkRequest(Recipient recipient) {
        Map<String, Object> map = new HashMap<>();
        CheckRequest checkRequest = new CheckRequest();
        checkRequest.setRecipientId(recipient.getId());
        checkRequest.setAmount(50000L);
        checkRequest.setAccount("12345");
        List<NciTranDTO> list = new ArrayList<>();
        NciTranDTO tranDTO = new NciTranDTO();
        tranDTO.setAccCL("acc_cl1");
        tranDTO.setAccCo("acc_co2");
        tranDTO.setOrder(3);
        tranDTO.setPurpose("purpose1");
        tranDTO.setSum("10000");

        NciTranDTO dto = new NciTranDTO();
        dto.setAccCL("acc_cl2");
        dto.setAccCo("acc_co2");
        dto.setOrder(2);
        dto.setPurpose("purpose2");
        dto.setSum("10001");
        list.add(tranDTO);
        list.add(dto);
        map.put("transactions", list);
        checkRequest.setParams(map);

        ResponseEntity<SimpleBillResponse> simpleBillDTO = sharedAgentResourceV3.checkV3(checkRequest);
        return simpleBillDTO.getBody();
    }

    private SimpleBillResponse unRegisteredCheckRequest(Recipient recipient) {
        Map<String, Object> map = new HashMap<>();
        CheckRequest checkRequest = new CheckRequest();
        checkRequest.setRecipientId(recipient.getId());
        checkRequest.setAmount(50000L);
        checkRequest.setAccount("12345");
        List<NciTranDTO> list = new ArrayList<>();
        NciTranDTO tranDTO = new NciTranDTO();
        tranDTO.setAccCL("acc_cl1");
        tranDTO.setAccCo("acc_co2");
        tranDTO.setOrder(3);
        tranDTO.setPurpose("purpose1");
        tranDTO.setSum("10000");

        NciTranDTO dto = new NciTranDTO();
        dto.setAccCL("acc_cl2");
        dto.setAccCo("acc_co2");
        dto.setOrder(2);
        dto.setPurpose("purpose2");
        dto.setSum("10001");
        list.add(tranDTO);
        list.add(dto);
        map.put("transactions", list);
        map.put("pan", "9860330000000000");
        map.put("exp", "0226");
        checkRequest.setParams(map);

        ResponseEntity<SimpleBillResponse> simpleBillDTO = sharedAgentUnregisteredPayResource.unregisteredCheck(checkRequest);
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
