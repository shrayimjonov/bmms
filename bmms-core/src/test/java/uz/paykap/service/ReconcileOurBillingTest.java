package uz.paykap.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import uz.paykap.IntegrationTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;
import uz.devops.bmcore.enumeration.PayType;
import uz.devops.bmcore.enumeration.RequestType;
import uz.devops.bmcore.enumeration.TransactionStatus;
import uz.javlon.commons.exception.CodedException;
import uz.javlon.commons.exception.ProblemEnum;
import uz.paykap.PkbillmsApp;
import uz.paykap.adapter.DataAdapterServiceV3;
import uz.paykap.adapter.common.CommonDataAdapterV3;
import uz.paykap.agent.tran.CommonTranServiceV3;
import uz.paykap.IntegrationTest;
import uz.paykap.domain.Bill;
import uz.paykap.domain.Recipient;
import uz.paykap.domain.Transaction;
import uz.paykap.gateway.dto.ReconcileDTO;
import uz.paykap.gateway.dto.SummaryReconcileResultDTO;
import uz.paykap.repository.BillRepository;
import uz.paykap.repository.RecipientRepository;
import uz.paykap.repository.TransactionRepository;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@IntegrationTest
public class ReconcileOurBillingTest {

    @Autowired
    private CommonTranServiceV3 commonTranServiceV2;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private BillRepository billRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private DataAdapterServiceV3 adapterServiceV3;

    @Autowired
    private RecipientRepository recipientRepository;

    private Transaction createTransaction(Long recipientId, String gatewayName, Bill bill) {
        Transaction transaction = new Transaction()
            .date(Instant.now())
            .recipientBillId(bill.getId())
            .payType(PayType.AGENT)
            .recipientStatus("failed")
            .recipientSuccess(false)
            .recipientName("perfectum")
            .senderSuccess(true)
            .status(TransactionStatus.FAILED)
            .requestGroup("322")
            .senderGateway("perfectum")
            .requestTrId("1")
            .requestLogin("internal")
            .senderName("kapital")
            .senderAmount(100000L)
            .senderDate(getFromDate())
            .senderAccount("1610")
            .systemFeeAmount(10L)
            .amountRatio(100)
            .comment("10002")
            .systemCommissionDownAmount(5L)
            .senderGateway(gatewayName)
            .senderServiceId(recipientId)
            .status(TransactionStatus.SUCCESS)
            .requestAmount(10000L)
            .recipientServiceId(recipientId);
        return transactionRepository.save(transaction);
    }

    private Bill createBillCheck(Recipient recipient) {
        Map<String, Object> stringStringMap = new HashMap<>();
        stringStringMap.put("key", "value");

//        Map<String, String> map = new HashMap<>();
        Bill bill = new Bill()
            .success(true)
            .type(RequestType.CHECK)
            .amount(1L)
            .account("account")
            .responseCode("OK")
            .responseText("ok")
            .recipientId(recipient.getId())
            .amountRatio(100)
            .recipientShortName(recipient.getShortName())
            .login("userTest")
            .requestTime(Instant.now())
            .responseTime(Instant.now())
            .requestJson(stringStringMap);
        return billRepository.save(bill);
    }

    private String getStringFromParams(Map<String, String> params) {
        if (params == null || params.isEmpty()) {
            return "{}";
        }
        try {
            return objectMapper.writeValueAsString(params);
        } catch (JsonProcessingException e) {
            return "{\"error\":\"Could'n parse json\",\"data\":\"" + params.toString() + "\"}";
        }
    }

    private Instant getFromDate() {
        DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'");
        LocalDateTime then = LocalDateTime.now().minusDays(1);
        return Instant.parse(then.format(format) + "03:00:00.000Z");
    }

    private Instant getFromFromDate() {
        DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'");
        LocalDateTime then = LocalDateTime.now().minusDays(2);
        return Instant.parse(then.format(format) + "03:00:00.000Z");
    }

    private Transaction createWrongTransaction(Long recipientId, String gatewayName, Bill bill) {
        Transaction transaction = new Transaction()
            .date(getFromFromDate())
            .recipientBillId(bill.getId())
            .payType(PayType.AGENT)
            .recipientStatus("failed")
            .recipientSuccess(false)
            .recipientName("perfectum")
            .senderSuccess(true)
            .status(TransactionStatus.FAILED)
            .requestGroup("322")
            .senderGateway("perfectum")
            .requestTrId("1")
            .requestLogin("internal")
            .senderName("kapital")
            .senderAmount(100000L)
            .senderDate(getFromDate())
            .senderAccount("1610")
            .systemFeeAmount(10L)
            .amountRatio(100)
            .comment("10002")
            .systemCommissionDownAmount(5L)
            .senderGateway(gatewayName)
            .senderServiceId(recipientId)
            .status(TransactionStatus.SUCCESS)
            .requestAmount(10000L)
            .recipientServiceId(recipientId);
        return transactionRepository.save(transaction);
    }


    @Test
    @WithMockUser(value = "internal")
    @Transactional
    public void check() {

        adapterServiceV3.sync(CommonDataAdapterV3.NAME);

        Recipient recipient = recipientRepository.findTop1ByShortName("perfectum").orElseThrow(() -> new CodedException(ProblemEnum.INVALID_PARAMS));


        Bill bill = createBillCheck(recipient);

        Transaction transaction = createTransaction(recipient.getId(), "perfectum", bill);
        Transaction transaction1 = createTransaction(recipient.getId(), "perfectum", bill);
        Transaction transaction2 = createWrongTransaction(recipient.getId(), "perfectum", bill);

        int transactionsSize = transactionRepository.findAll().size();

        ReconcileDTO reconcileDTO = new ReconcileDTO();
        reconcileDTO.setFromTime(getFromDate());
        reconcileDTO.setToTime(Instant.now());
        reconcileDTO.setRecipientId(recipient.getId());

        SummaryReconcileResultDTO summaryReconcileResultDTO = commonTranServiceV2.reconcile(reconcileDTO);

        assertThat(summaryReconcileResultDTO.getReconcileResultDTOS().size() == 2);
        assertThat(summaryReconcileResultDTO.getReconcileResultDTOS().size() != transactionsSize);
        assertThat(summaryReconcileResultDTO.getReconcileResultDTOS().size() == summaryReconcileResultDTO.getTotalCount());
        assertThat((transaction.getRequestAmount() + transaction1.getRequestAmount()) == summaryReconcileResultDTO.getTotalAmount());


    }
}
