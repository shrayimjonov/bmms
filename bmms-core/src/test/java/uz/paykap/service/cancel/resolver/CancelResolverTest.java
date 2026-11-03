package uz.paykap.service.cancel.resolver;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;
import org.apache.commons.lang3.RandomUtils;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;
import uz.devops.bmcore.enumeration.PayType;
import uz.devops.bmcore.enumeration.RequestType;
import uz.devops.bmcore.enumeration.TransactionStatus;
import uz.javlon.commons.result.CommonResultData;
import uz.javlon.commons.result.HasData;
import uz.javlon.svgate.dto.TranDTO;
import uz.javlon.svgate.dto.enumeration.TranStatus;
import uz.javlon.svgate.dto.enumeration.TranType;
import uz.javlon.svgate.service.SvGatePaymentServiceV2;
import uz.paykap.IntegrationTest;
import uz.paykap.config.Constants;
import uz.paykap.domain.Bill;
import uz.paykap.domain.Transaction;
import uz.paykap.repository.BillRepository;
import uz.paykap.repository.TransactionRepository;
import uz.paykap.security.SecurityUtils;
import uz.paykap.service.dto.p2p.BmResultStatus;

@Transactional
@RunWith(SpringRunner.class)
@IntegrationTest
public class CancelResolverTest {

    @Autowired
    private CancelResolver cancelResolver;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private BillRepository billRepository;

    @MockBean
    private SvGatePaymentServiceV2 svGatePaymentService;

    @Autowired
    private SyncTransactionService syncTransactionService;

    @Test
    @Transactional
    public void syncTransaction() throws JsonProcessingException {
        mockUzcardToSuccess();
        var transaction = CancelResolverTest.createTransaction(true, true);
        transactionRepository.save(transaction);
        createSenderBill(transaction);
        createRecipientBill(transaction);

        var resultData = syncTransactionService.syncTransaction(transaction.getId());
        System.out.println("Synced Transaction: " + objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(resultData.getData()));
        Assertions.assertThat(resultData).isNotNull();
        Assertions.assertThat(resultData.isSuccess()).isNotNull();
        Assertions.assertThat(resultData.isSuccess()).isTrue();
    }

    @Test
    @Transactional
    @WithMockUser(value = "abdurahim")
    public void cancelSender() throws JsonProcessingException {
        mockUzcardToSuccess();
        Transaction transaction = createTransaction(false, true);
        transaction = transactionRepository.save(transaction);
        createSenderBill(transaction);
        createRecipientBill(transaction);
        var resultData = cancelResolver.cancel(transaction.getId());
        System.out.println("Cancelled Transaction: " + objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(resultData.getData()));
        billRepository.findAll();
        Assertions.assertThat(resultData).isNotNull();
        Assertions.assertThat(resultData.isSuccess()).isNotNull();
        Assertions.assertThat(resultData.isSuccess()).isTrue();
    }

    @Test
    @Transactional
    public void cancelSenderProd() throws JsonProcessingException {
        mockUzcardToSuccess();
        Transaction transaction = createTransactionUzcardProd();
        transaction = transactionRepository.save(transaction);
        createSenderBill(transaction);
        createRecipientBill(transaction);
        var resultData = cancelResolver.cancel(transaction.getId());
        Assertions.assertThat(resultData).isNotNull();
        Assertions.assertThat(resultData.isSuccess()).isNotNull();
        Assertions.assertThat(resultData.isSuccess()).isTrue();
    }

    @Test
    @Transactional
    public void cancelRecipient() throws JsonProcessingException {
        mockUzcardToSuccess();
        Transaction transaction = createTransaction(true, false);
        transaction = transactionRepository.save(transaction);
        createSenderBill(transaction);
        createRecipientBill(transaction);
        var resultData = cancelResolver.cancel(transaction.getId());
        Assertions.assertThat(resultData).isNotNull();
        Assertions.assertThat(resultData.isSuccess()).isNotNull();
        Assertions.assertThat(resultData.isSuccess()).isTrue();
    }

    @Test
    @Transactional
    public void cancelRecipientAndSender() throws JsonProcessingException {
        mockUzcardToSuccess();
        Transaction transaction = createTransaction(true, true);
        transaction = transactionRepository.save(transaction);
        createSenderBill(transaction);
        createRecipientBill(transaction);
        var resultData = cancelResolver.cancel(transaction.getId());
        Assertions.assertThat(resultData).isNotNull();
        Assertions.assertThat(resultData.isSuccess()).isNotNull();
        Assertions.assertThat(resultData.isSuccess()).isTrue();
    }

    private void mockUzcardToSuccess() throws JsonProcessingException {
        var uzcardToUzcardTransaction = getUzcardToUzcardTransaction();
        var reverseResponse = UzcardToUzCardCancelTest.getReverseResponse();
        var tranDTOS = objectMapper.readValue(uzcardToUzcardTransaction, new TypeReference<List<TranDTO>>() {});
        var reverseTranDtos = objectMapper.readValue(reverseResponse, new TypeReference<List<TranDTO>>() {});

        Mockito
            .when(svGatePaymentService.statusByExtId(ArgumentMatchers.any()))
            .thenReturn(new CommonResultData<>(BmResultStatus.SUCCESS, tranDTOS));

        Mockito
            .when(svGatePaymentService.reverse(ArgumentMatchers.any()))
            .thenReturn(new CommonResultData<>(BmResultStatus.SUCCESS, reverseTranDtos));
    }

    private HasData<List<TranDTO>> createTranDtos() {
        TranDTO tranDTO = new TranDTO();
        tranDTO.setRefNum("123123123");
        tranDTO.setResp(0);
        tranDTO.setStatus(TranStatus.OK);
        tranDTO.setAmount(1000L);
        tranDTO.setExternalId(String.valueOf(Instant.now().toEpochMilli()));
        tranDTO.setPan("8600490000000000");
        tranDTO.setTranType(TranType.P2P);
        tranDTO.setDate7(ZonedDateTime.now());
        tranDTO.setStan(RandomUtils.nextInt());
        tranDTO.setMerchantId("123");
        tranDTO.setTerminalId("234");
        return new CommonResultData<>(Collections.singletonList(tranDTO));
    }

    public static Transaction createTransaction(Boolean recipientSuccess, Boolean senderSuccess) {
        Transaction transaction = new Transaction();
        transaction.setRequestAmount(1000L);
        transaction.setRecipientCcy(Constants.UZS_CURRENCY);
        transaction.setRequestTrId(String.valueOf(Instant.now().toEpochMilli()));
        transaction.setRequestLogin("system");
        transaction.setSenderName("system");
        transaction.setSenderAccount("system");
        transaction.setSenderAmount(1000L);
        transaction.setSenderCcy(Constants.UZS_CURRENCY);
        transaction.setSenderSuccess(senderSuccess);
        transaction.setSenderDate(Instant.now());
        transaction.setSenderStatus("Ok");
        transaction.setSenderStatusText("Ok");
        transaction.setSenderGateway("UZCARD");
        //        transaction.setSenderSuccess();
        transaction.setGatewayAmount(1000D);
        transaction.setRecipientName("UZCARD");
        transaction.setRecipientAccount("1231");
        transaction.setRecipientAmount(1000L);
        transaction.setRecipientSuccess(recipientSuccess);
        transaction.setRecipientDate(Instant.now());
        //        transaction.setRecipientStatus();
        //        transaction.setRecipientBillId();
        transaction.setRecipientGateway("HUMO");
        //        transaction.setSystemCommissionUpAmount
        transaction.setCcyRate(Constants.UZS_CURRENCY);
        transaction.setStatus(TransactionStatus.NEW);
        transaction.setDate(Instant.now());
        transaction.setPayType(PayType.P2P);
        transaction.setEposMerchant("epos_merchant");
        transaction.setEposTerminal("epos_terminal");
        transaction.setAmountRatio(100);

        return transaction;
    }

    public static Transaction createTransactionUzcardProd() {
        Transaction transaction = new Transaction();
        transaction.setAgentFeeAmount(0L);
        transaction.setAmountRatio(100);
        transaction.setCcyRate(1);
        transaction.setComment("TOKEN -> PAN");
        transaction.setCreatedBy("anor_mobile");
        transaction.setCreatedDate(Instant.now());
        transaction.setDate(Instant.now());
        transaction.setEposMerchant("00491000004911Z>00491000004911Z");
        transaction.setEposTerminal("6061001H>6061001H");
        transaction.setGatewayAmount(50000d);
        transaction.setId(105276245L);
        transaction.setLastModifiedBy("anor_mobile");
        transaction.setLastModifiedDate(Instant.now());
        transaction.setMain(true);
        transaction.setPayType(PayType.P2P);
        transaction.setRecipientAccount("9860180000000000");
        transaction.setRecipientAmount(50000L);
        transaction.setRecipientBillId(105276272L);
        transaction.setRecipientCcy(860);
        transaction.setRecipientDate(Instant.now());
        transaction.setRecipientGateway("UZCARD");
        transaction.setRecipientName("HumoPanRecipientService");
        transaction.setRecipientServiceId(0L);
        transaction.setRecipientStatus("4");
        transaction.setRecipientStatusText("Success");
        transaction.setRecipientSuccess(true);
        transaction.setRecipientTrId("478017594");
        transaction.setRequestAmount(50000L);
        transaction.setRequestCcy(null);
        transaction.setRequestGroup("anor_mobile");
        transaction.setRequestLogin("anor_mobile");
        transaction.setRequestSubAccount("anor_mobile");
        transaction.setRequestTrId("6b613dd2-befe-4b50-bf4e-dab0fa8c536d");
        transaction.setRoundAmount(0D);
        transaction.setSenderAccount("986060GZOKYW7659");
        transaction.setSenderAmount(50000L);
        transaction.setSenderBean("HumoTokenSenderService");
        transaction.setSenderBillId(105276273L);
        transaction.setSenderCcy(860);
        transaction.setSenderDate(Instant.now());
        transaction.setSenderGateway("UZCARD");
        transaction.setSenderName("HumoTokenSenderService");
        transaction.setSenderServiceId(0L);
        transaction.setSenderStatus("10");
        transaction.setSenderStatusText("Confirm success");
        transaction.setSenderSuccess(true);
        transaction.setSenderTrId("478017591");
        transaction.setStatus(TransactionStatus.SUCCESS);
        transaction.setSystemCommissionDownAmount(0L);
        transaction.setSystemCommissionUpAmount(0L);
        transaction.setSystemCurrency(860);
        transaction.setSystemFee(0D);
        transaction.setSystemFeeAmount(0L);
        return transaction;
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

    public HasData<List<TranDTO>> reverse(String svId) {
        TranDTO tranDTO = new TranDTO();
        tranDTO.setRefNum(svId);
        tranDTO.setResp(0);
        tranDTO.setStatus(TranStatus.OK);
        tranDTO.setAmount(1000L);
        tranDTO.setExternalId(String.valueOf(Instant.now().toEpochMilli()));
        tranDTO.setPan("8600490000000000");
        tranDTO.setTranType(TranType.P2P);
        tranDTO.setDate7(ZonedDateTime.now());
        tranDTO.setStan(RandomUtils.nextInt());
        tranDTO.setMerchantId("123");
        tranDTO.setTerminalId("234");
        return new CommonResultData<>(Collections.singletonList(tranDTO));
    }

    private String getUzcardToUzcardTransaction() {
        return (
            "[\n" +
            "  {\n" +
            "    \"id\": \"1971470368\",\n" +
            "    \"externalId\": \"ANOR_BM_P2P_C_194746547\",\n" +
            "    \"username\": \"anorbank\",\n" +
            "    \"pan\": \"860049******6725\",\n" +
            "    \"pan2\": null,\n" +
            "    \"tranType\": \"P2P_MAIN\",\n" +
            "    \"amount\": \"50000\",\n" +
            "    \"amountCredit\": null,\n" +
            "    \"stan\": \"708725\",\n" +
            "    \"date7\": \"0117194859\",\n" +
            "    \"date12\": \"220117194859\",\n" +
            "    \"commission\": null,\n" +
            "    \"expiry\": \"1249\",\n" +
            "    \"refNum\": \"015829533454\",\n" +
            "    \"merchantId\": \"90631981294\",\n" +
            "    \"terminalId\": \"92300135\",\n" +
            "    \"currency\": \"860\",\n" +
            "    \"field38\": \"\",\n" +
            "    \"field48\": \"\",\n" +
            "    \"field91\": \"\",\n" +
            "    \"resp\": \"0\",\n" +
            "    \"respText\": \"Successful transaction\",\n" +
            "    \"respSV\": \"00\",\n" +
            "    \"status\": \"OK\"\n" +
            "  },\n" +
            "  {\n" +
            "    \"id\": \"1971470368\",\n" +
            "    \"externalId\": \"ANOR_BM_P2P_C_194746547\",\n" +
            "    \"username\": \"anorbank\",\n" +
            "    \"pan\": \"860049******6725\",\n" +
            "    \"pan2\": null,\n" +
            "    \"tranType\": \"P2P_DEBIT\",\n" +
            "    \"amount\": \"50000\",\n" +
            "    \"amountCredit\": null,\n" +
            "    \"stan\": \"533457\",\n" +
            "    \"date7\": \"0117194859\",\n" +
            "    \"date12\": \"220117194859\",\n" +
            "    \"commission\": null,\n" +
            "    \"expiry\": \"1249\",\n" +
            "    \"refNum\": \"015829533457\",\n" +
            "    \"merchantId\": \"90631981294\",\n" +
            "    \"terminalId\": \"92300135\",\n" +
            "    \"currency\": \"860\",\n" +
            "    \"field38\": \"533457\",\n" +
            "    \"field48\": \"\",\n" +
            "    \"field91\": \"\",\n" +
            "    \"resp\": \"0\",\n" +
            "    \"respText\": \"Successful transaction\",\n" +
            "    \"respSV\": \"00\",\n" +
            "    \"status\": \"OK\"\n" +
            "  },\n" +
            "  {\n" +
            "    \"id\": \"1971470368\",\n" +
            "    \"externalId\": \"ANOR_BM_P2P_C_194746547\",\n" +
            "    \"username\": \"anorbank\",\n" +
            "    \"pan\": \"860049******7543\",\n" +
            "    \"pan2\": null,\n" +
            "    \"tranType\": \"P2P_CREDIT\",\n" +
            "    \"amount\": \"50000\",\n" +
            "    \"amountCredit\": null,\n" +
            "    \"stan\": \"533459\",\n" +
            "    \"date7\": \"0117194859\",\n" +
            "    \"date12\": \"220117194859\",\n" +
            "    \"commission\": null,\n" +
            "    \"expiry\": \"1249\",\n" +
            "    \"refNum\": \"015829533459\",\n" +
            "    \"merchantId\": \"90631981294\",\n" +
            "    \"terminalId\": \"92300135\",\n" +
            "    \"currency\": \"860\",\n" +
            "    \"field38\": \"533457\",\n" +
            "    \"field48\": \"\",\n" +
            "    \"field91\": \"\",\n" +
            "    \"resp\": \"0\",\n" +
            "    \"respText\": \"Successful transaction\",\n" +
            "    \"respSV\": \"00\",\n" +
            "    \"status\": \"OK\"\n" +
            "  }\n" +
            "]\n"
        );
    }
}
