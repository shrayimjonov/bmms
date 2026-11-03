package uz.paykap.service.cancel.resolver;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.mockito.ArgumentMatchers;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;
import uz.devops.bmcore.enumeration.PayType;
import uz.devops.bmcore.enumeration.RequestType;
import uz.devops.bmcore.enumeration.TransactionStatus;
import uz.javlon.commons.result.CommonResultData;
import uz.javlon.svgate.dto.TranDTO;
import uz.javlon.svgate.dto.sv.SvResponseWithData;
import uz.javlon.svgate.service.SvGatePaymentServiceV2;
import uz.paykap.IntegrationTest;
import uz.paykap.domain.Bill;
import uz.paykap.domain.Transaction;
import uz.paykap.repository.BillRepository;
import uz.paykap.repository.TransactionRepository;
import uz.paykap.security.SecurityUtils;
import uz.paykap.service.dto.p2p.BmResultStatus;
import uz.paykap.utils.TransactionPrefixGenerator;

import java.time.Instant;
import java.util.List;

import static org.mockito.Mockito.when;

@Transactional
@RunWith(SpringRunner.class)
@IntegrationTest
public class UzcardToHumoCancelTest {

    @Autowired
    private CancelResolver cancelResolver;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private BillRepository billRepository;

    @MockBean
    private SvGatePaymentServiceV2 svGatePaymentService;
    @Autowired
    private TransactionPrefixGenerator transactionPrefixGenerator;
    @Autowired
    private ObjectMapper objectMapper;
    private final String reverseResponse = "[\n" +
        "        {\n" +
        "            \"id\": \"1331290850\",\n" +
        "            \"username\": \"anorbank\",\n" +
        "            \"refNum\": \"013539759512\",\n" +
        "            \"ext\": \"BM_P2P_D_107866034\",\n" +
        "            \"pan\": \"860005******1023\",\n" +
        "            \"pan2\": null,\n" +
        "            \"expiry\": \"2609\",\n" +
        "            \"tranType\": \"DEBIT\",\n" +
        "            \"transType\": 0,\n" +
        "            \"date7\": \"0117194905\",\n" +
        "            \"date12\": \"220117194905\",\n" +
        "            \"amount\": 5000000,\n" +
        "            \"currency\": 860,\n" +
        "            \"stan\": \"271304\",\n" +
        "            \"field38\": \"759512\",\n" +
        "            \"field48\": \"\",\n" +
        "            \"field91\": \"\",\n" +
        "            \"merchantId\": \"90631981309\",\n" +
        "            \"terminalId\": \"92300146\",\n" +
        "            \"resp\": 0,\n" +
        "            \"respText\": \"Successful transaction\",\n" +
        "            \"respSV\": \"00\",\n" +
        "            \"status\": \"OK\",\n" +
        "            \"reversal\": 0\n" +
        "        },\n" +
        "        {\n" +
        "            \"id\": \"1331290869\",\n" +
        "            \"username\": \"anorbank\",\n" +
        "            \"refNum\": \"013539759512\",\n" +
        "            \"ext\": \"BM_P2P_D_107866034\",\n" +
        "            \"pan\": \"860005******1023\",\n" +
        "            \"pan2\": null,\n" +
        "            \"expiry\": \"2609\",\n" +
        "            \"tranType\": \"REVERSAL\",\n" +
        "            \"transType\": 0,\n" +
        "            \"date7\": \"0117194905\",\n" +
        "            \"date12\": \"220117194905\",\n" +
        "            \"amount\": 5000000,\n" +
        "            \"currency\": 860,\n" +
        "            \"stan\": \"271304\",\n" +
        "            \"field38\": \"759512\",\n" +
        "            \"field48\": \"\",\n" +
        "            \"field91\": \"\",\n" +
        "            \"merchantId\": \"90631981309\",\n" +
        "            \"terminalId\": \"92300146\",\n" +
        "            \"resp\": 0,\n" +
        "            \"respText\": \"Successful transaction\",\n" +
        "            \"respSV\": \"00\",\n" +
        "            \"status\": \"ROK\",\n" +
        "            \"reversal\": 0\n" +
        "        }\n" +
        "    ]";

    @SneakyThrows
    @Test
    @Transactional
    @WithMockUser(value = "abdurahim")
    public void cancel() {
        var beforeBillsSize = billRepository.findAll().size();
        Transaction transaction = createTransactionHumoToUzcard();
        createSenderBill(transaction);
        createRecipientBill(transaction);

        String infoResponse = " [ {\n" +
            "            \"id\": \"1331290620\",\n" +
            "            \"username\": \"anorbank\",\n" +
            "            \"refNum\": \"013539758795\",\n" +
            "            \"ext\": \"BM_P2P_C_107866029\",\n" +
            "            \"pan\": \"860031******9505\",\n" +
            "            \"pan2\": null,\n" +
            "            \"expiry\": \"2505\",\n" +
            "            \"tranType\": \"P2P_CREDIT\",\n" +
            "            \"transType\": 0,\n" +
            "            \"date7\": \"0117194859\",\n" +
            "            \"date12\": \"220117194859\",\n" +
            "            \"amount\": 40000000,\n" +
            "            \"currency\": 860,\n" +
            "            \"stan\": \"546185\",\n" +
            "            \"field38\": \"758795\",\n" +
            "            \"field48\": \"\",\n" +
            "            \"field91\": \"\",\n" +
            "            \"merchantId\": \"90631981360\",\n" +
            "            \"terminalId\": \"92300186\",\n" +
            "            \"resp\": 0,\n" +
            "            \"respText\": \"Successful transaction\",\n" +
            "            \"respSV\": \"00\",\n" +
            "            \"status\": \"OK\",\n" +
            "            \"reversal\": 0\n" +
            "        }]";
        final List<TranDTO> list = objectMapper.readValue(infoResponse, new TypeReference<>() {
        });
        when(svGatePaymentService.statusByExtId(ArgumentMatchers.any())).thenReturn(new CommonResultData<>(list));
        var typeReference = new TypeReference<List<TranDTO>>() {
        };
        var svResponseWithData = new CommonResultData<>(BmResultStatus.SUCCESS, objectMapper.readValue(reverseResponse, typeReference));
//        svResponseWithData.setData(objectMapper.readValue(reverseResponse, typeReference));
//        svResponseWithData.setCode(0);
//        svResponseWithData.setMessage("success");
        when(svGatePaymentService.reverse(ArgumentMatchers.anyString())).thenReturn(svResponseWithData);

        var resultData = cancelResolver.cancel(transaction.getId());
        var bills = billRepository.findAll();

        Assertions.assertThat(bills).isNotNull();
        Assertions.assertThat(bills).isNotEmpty();
        Assertions.assertThat(bills.size()).isGreaterThan(beforeBillsSize);

        Assertions.assertThat(resultData).isNotNull();
        Assertions.assertThat(resultData.isSuccess()).isNotNull();
        Assertions.assertThat(resultData.isSuccess()).isTrue();
    }

    @SneakyThrows
    public Transaction createTransactionHumoToUzcard() {
        Transaction transaction = new Transaction();
        transaction.setAgentFeeAmount(0L);
        transaction.setAmountRatio(100);
        transaction.setCcyRate(1);
        transaction.setComment("TOKEN -> PAN");
        transaction.setCreatedBy("anor_mobile");
        transaction.setCreatedDate(Instant.now());
        transaction.setDate(Instant.now());
        transaction.setEposMerchant(null);
        transaction.setEposTerminal(null);
        transaction.setGatewayAmount(10000000D);
        transaction.setId(108161277L);
        transaction.setLastModifiedBy("anor_mobile");
        transaction.setLastModifiedDate(Instant.now());
        transaction.setMain(true);
        transaction.setPayType(PayType.valueOf("P2P"));
        transaction.setRecipientAccount("860006******0064");
        transaction.setRecipientAmount(10000000L);
        transaction.setRecipientBillId(null);
        transaction.setRecipientCcy(860);
        transaction.setRecipientDate(null);
        transaction.setRecipientGateway("UZCARD");
        transaction.setRecipientName("UzcardPanRecipientService");
        transaction.setRecipientServiceId(0L);
        transaction.setRecipientStatus(null);
        transaction.setRecipientStatusText("8600060000000000");
        transaction.setRecipientSuccess(null);
        transaction.setRecipientTrId(null);
        transaction.setRequestAmount(10000000L);
        transaction.setRequestCcy(null);
        transaction.setRequestGroup("anor_mobile");
        transaction.setRequestLogin("anor_mobile");
        transaction.setRequestSubAccount("anor_mobile");
        transaction.setRequestTrId("746f48b5-88f4-47c1-bf75-36bda64f6d9c");
        transaction.setRoundAmount(0D);
        transaction.setSenderAccount("986060YMSOJU0625");
        transaction.setSenderAmount(10000000L);
        transaction.setSenderBean("HumoTokenSenderService");
        transaction.setSenderBillId(0L);
        transaction.setSenderCcy(860);
        transaction.setSenderDate(null);
        transaction.setSenderGateway("HUMO");
        transaction.setSenderName("HumoTokenSenderService");
        transaction.setSenderServiceId(0L);
        transaction.setSenderStatus(null);
        transaction.setSenderStatusText(null);
        transaction.setSenderSuccess(null);
        transaction.setSenderTrId(null);
        transaction.setStatus(TransactionStatus.valueOf("PAYING"));
        transaction.setSystemCommissionDownAmount(0L);
        transaction.setSystemCommissionUpAmount(0L);
        transaction.setSystemCurrency(860);
        transaction.setSystemFee(0D);
        transaction.setSystemFeeAmount(0L);
        transaction.setRecipientTrId(String.valueOf(System.currentTimeMillis()));
        transaction.setSenderTrId(String.valueOf(System.currentTimeMillis()));
        return transactionRepository.save(transaction);
    }

    public Bill createSenderBill(Transaction transaction) {
        var userLogin = SecurityUtils.getCurrentUserLogin();
        Bill bill = new Bill()
            .type(RequestType.P2P_DEBIT)
            .amountRatio(transaction.getAmountRatio())
            .login(userLogin.orElse(null))
            .requestTime(Instant.now())
            .transactionId(transaction.getId())
            .responseTime(Instant.now())
            .recipientShortName("debit")
            .amount(transaction.getSenderAmount())
            .account(transaction.getSenderAccount())
            .currency(transaction.getSenderCcy())
            .extId(transaction.getSenderTrId())
            .outExtId(String.valueOf(System.currentTimeMillis()));

        return billRepository.save(bill);
    }

    public Bill createRecipientBill(Transaction transaction) {
        var userLogin = SecurityUtils.getCurrentUserLogin();
        Bill bill = new Bill()
            .type(RequestType.P2P_CREDIT)
            .amountRatio(transaction.getAmountRatio())
            .login(userLogin.orElse(null))
            .requestTime(Instant.now())
            .transactionId(transaction.getId())
            .responseTime(Instant.now())
            .recipientShortName("credit")
            .amount(transaction.getRecipientAmount())
            .account(transaction.getRecipientAccount())
            .currency(transaction.getRecipientCcy())
            .extId(transaction.getRecipientTrId())
            .outExtId(String.valueOf(System.currentTimeMillis()));

        return billRepository.save(bill);
    }
}
