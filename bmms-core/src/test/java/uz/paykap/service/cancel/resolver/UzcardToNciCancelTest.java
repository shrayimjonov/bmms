package uz.paykap.service.cancel.resolver;

import lombok.SneakyThrows;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;
import uz.devops.bmcore.enumeration.PayType;
import uz.devops.bmcore.enumeration.RequestType;
import uz.devops.bmcore.enumeration.TransactionStatus;
import uz.paykap.IntegrationTest;
import uz.paykap.domain.Bill;
import uz.paykap.domain.Transaction;
import uz.paykap.repository.BillRepository;
import uz.paykap.repository.TransactionRepository;
import uz.paykap.security.SecurityUtils;
import uz.paykap.service.abs.AbsStatusService;
import uz.paykap.service.abs.AbstractAbsService;
import uz.paykap.service.abs.request.AbsTransactionInfoRequest;
import uz.paykap.service.abs.request.AbsTransactionRequest;
import uz.paykap.service.abs.response.AbsTransactionInfo;
import uz.paykap.service.abs.response.AbsTransactionResult;
import uz.paykap.service.abs.response.AbsTransactionStatusResult;
import uz.paykap.service.dto.p2p.BmResultStatus;
import uz.paykap.service.dto.result.BmResultData;
import uz.paykap.utils.TransactionPrefixGenerator;

import java.time.Instant;

import static org.mockito.Mockito.when;

@Transactional
@RunWith(SpringRunner.class)
@IntegrationTest
public class UzcardToNciCancelTest {

    @Autowired
    private CancelResolver cancelResolver;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private BillRepository billRepository;

    @MockBean
    private AbsStatusService absStatusService;
    @Autowired
    private TransactionPrefixGenerator transactionPrefixGenerator;
    @MockBean
    private AbstractAbsService abstractAbsService;

    @SneakyThrows
    @Test
    @Transactional
    @WithMockUser(value = "abdurahim")
    public void cancel() {

        var beforeBillsSize = billRepository.findAll().size();
        Transaction transaction = createTransactionHumoToNci();
        AbsTransactionInfoRequest absTransactionInfoRequest = new AbsTransactionInfoRequest();
        createSenderBill(transaction);
        createRecipientBill(transaction);
        absTransactionInfoRequest.setTransId(transactionPrefixGenerator.getCancelTranSessionId(transaction.getId()));
        setupMocks(absTransactionInfoRequest, transaction.getRecipientTrId());
        var resultData = cancelResolver.cancel(transaction.getId());
        var bills = billRepository.findAll();

        Assertions.assertThat(bills).isNotNull();
        Assertions.assertThat(bills).isNotEmpty();
        Assertions.assertThat(bills).isNotEmpty();
        Assertions.assertThat(bills.size()).isGreaterThan(beforeBillsSize);

        Assertions.assertThat(resultData).isNotNull();
        Assertions.assertThat(resultData.isSuccess()).isNotNull();
        Assertions.assertThat(resultData.isSuccess()).isTrue();
    }

    public void setupMocks(AbsTransactionInfoRequest request, String nciTransId) {
        var nciResponse = createNciResponse(nciTransId);
        var nciTransaction = createNciTransaction(nciResponse.getData());
        when(absStatusService.checkBankOperDay()).thenReturn(new BmResultData<>(true));
        when(absStatusService.getTransactionStatus(ArgumentMatchers.any())).thenReturn(nciResponse);
        AbsTransactionStatusResult statusResult = new AbsTransactionStatusResult();
        statusResult.setErrorCode("0");
        statusResult.setErrorNote("SUCCESS");
        statusResult.setOldErrorCode("0");
        statusResult.setOldErrorNote("SUCCESS");
        statusResult.setTransId("149540623");
        statusResult.setTypeQuery(1);
        statusResult.setDateTime("2022-04-15 12:20:54.0");

        when(absStatusService.getRequestStatusAndBody(ArgumentMatchers.any())).thenReturn(new BmResultData<>(BmResultStatus.SUCCESS, statusResult));
        when(abstractAbsService.doTransaction(ArgumentMatchers.any())).thenReturn(new BmResultData<>(new AbsTransactionResult("" + System.currentTimeMillis(), "0", "SUCCESS", "")));
        when(abstractAbsService.sendCancelRequest(ArgumentMatchers.any())).thenReturn(new BmResultData<>(new AbsTransactionResult("" + System.currentTimeMillis(), "0", "SUCCESS", "")));
    }

    public AbsTransactionRequest createNciTransaction(AbsTransactionInfo absTransactionInfo) {
        AbsTransactionRequest transactionRequest = new AbsTransactionRequest();
        transactionRequest.setAccCl(absTransactionInfo.getAccCo());
        transactionRequest.setNameCl(absTransactionInfo.getNameCo());
        transactionRequest.setBankCl(absTransactionInfo.getBankCo());
        transactionRequest.setAccCo(absTransactionInfo.getAccCl());
        transactionRequest.setNameCo(absTransactionInfo.getNameCl());
        transactionRequest.setBankCo(absTransactionInfo.getBankCl());
        transactionRequest.setSumma(Long.valueOf(absTransactionInfo.getSumma()));
//        transactionRequest.setDocNum();// TODO: 22.01.2022 aniqlashtirish kere
//        transactionRequest.setDate(Timestamp.from(Instant.now()));
//        transactionRequest.setPurposeId(absTransactionInfo.get);// TODO: 22.01.2022 aniqlashtirish kere
        transactionRequest.setPurpose("OTMENA" + absTransactionInfo.getPurpose());// TODO: 22.01.2022 aniqlashtirish kere
        transactionRequest.setRequestId(transactionPrefixGenerator.getCancelTranSessionId());
        transactionRequest.setRequestType(RequestType.PAY);
        return transactionRequest;
    }

    public BmResultData<AbsTransactionInfo> createNciResponse(String transId) {
        AbsTransactionInfo info = new AbsTransactionInfo();
        info.setErrorCode("0");
        info.setErrorNote("SUCCESS");
        info.setTransId(transId);
        info.setBranch("1183");
//        info.setDocNum("108875202");
        info.setDateTime("21.01.2022");
        info.setBankCl("1183");
        info.setAccCl("17403000900001100000");
        info.setNameCl("Òðàíçèò ñ÷åò Humo äð áàíê - ñ÷åò 22616 (êîøåëåê) A 207324986");
        info.setBankCo("1183");
        info.setAccCo("22616000490114700000");
        info.setNameCo("SCHETA UPRAVLYAEMYE CHEREZ SISTEMY DO");
        info.setPurpose("00634Ïåðåâîä ñ ÏÊ ¹ \"986004OJEITX9124\" íà êîøåëåê SCHETA UPRAVLYAEMYE CHEREZ SISTEMY DO");
        info.setSumma(String.valueOf(1800000));
        info.setCurrency("0");
        info.setTypeDoc("06");
        info.setSDealId("106");
        info.setVDate("21.01.2022");
        info.setState("3");
        return new BmResultData<>(info);
    }

    @SneakyThrows
    public Transaction createTransactionHumoToNci() {
        Transaction transaction = new Transaction();
        transaction.setAgentFeeAmount(0L);
        transaction.setAmountRatio(100);
        transaction.setCcyRate(1);
        transaction.setComment("TOKEN -> ACCOUNT");
        transaction.setCreatedBy("anor_mobile");
        transaction.setCreatedDate(Instant.parse("2022-01-21T04:43:55.431993Z"));
        transaction.setDate(Instant.now());
//        transaction.setDate(Instant.parse("2022-01-21T04:43:55.431859Z"));
        transaction.setEposMerchant("00491000004911Q");
        transaction.setEposTerminal("60610015");
        transaction.setGatewayAmount(1800000.0);
        transaction.setId(108875202L);
        transaction.setLastModifiedBy("anor_mobile");
        transaction.setLastModifiedDate(Instant.parse("2022-01-21T04:43:56.521042Z"));
        transaction.setMain(true);
        transaction.setPayType(PayType.valueOf("P2P"));
        transaction.setRecipientAccount("22616000490114711001");
        transaction.setRecipientAmount(1800000L);
        transaction.setRecipientBillId(108875189L);
        transaction.setRecipientCcy(860);
        transaction.setRecipientDate(Instant.parse("2022-01-21T04:43:56.301127Z"));
        transaction.setRecipientGateway("NCI");
        transaction.setRecipientName("NciAccountRecipientService");
        transaction.setRecipientServiceId(1183L);
        transaction.setRecipientStatus("0");
        transaction.setRecipientStatusText("SUCCESS");
        transaction.setRecipientSuccess(true);
        transaction.setRecipientTrId("112538184");
        transaction.setRequestAmount(1800000L);
        transaction.setRequestCcy(null);
        transaction.setRequestGroup("anor_mobile");
        transaction.setRequestLogin("anor_mobile");
        transaction.setRequestSubAccount("anor_mobile");
        transaction.setRequestTrId("b6414633-cb4d-46ee-8345-4fc520ed9471");
        transaction.setRoundAmount((double) 0);
        transaction.setSenderAccount("986004OJEITX9124");
        transaction.setSenderAmount(1800000L);
        transaction.setSenderBean("HumoTokenSenderService");
        transaction.setSenderBillId(108875190L);
        transaction.setSenderCcy(860);
        transaction.setSenderDate(Instant.parse("2022-01-21T04:43:56.437399Z"));
        transaction.setSenderGateway("HUMO");
        transaction.setSenderName("HumoTokenSenderService");
        transaction.setSenderServiceId(0L);
        transaction.setSenderStatus("10");
        transaction.setSenderStatusText("Confirm success");
        transaction.setSenderSuccess(true);
        transaction.setSenderTrId("503304080");
        transaction.setStatus(TransactionStatus.valueOf("SUCCESS"));
        transaction.setSystemCommissionDownAmount(0L);
        transaction.setSystemCommissionUpAmount(0L);
        transaction.setSystemCurrency(860);
        transaction.setSystemFee((double) 0);
        transaction.setSystemFeeAmount(0L);
        return transactionRepository.save(transaction);
    }

    public Bill createSenderBill(Transaction transaction) {
        var userLogin = SecurityUtils.getCurrentUserLogin();
        Bill bill = new Bill().type(RequestType.P2P_DEBIT).amountRatio(transaction.getAmountRatio()).login(userLogin.orElse(null)).requestTime(Instant.now()).transactionId(transaction.getId()).responseTime(Instant.now()).recipientShortName("debit").amount(transaction.getSenderAmount()).account(transaction.getSenderAccount()).currency(transaction.getSenderCcy()).extId(transaction.getSenderTrId()).outExtId(String.valueOf(System.currentTimeMillis()));

        return billRepository.save(bill);
    }

    public Bill createRecipientBill(Transaction transaction) {
        var userLogin = SecurityUtils.getCurrentUserLogin();
        Bill bill = new Bill().type(RequestType.P2P_CREDIT).amountRatio(transaction.getAmountRatio()).login(userLogin.orElse(null)).requestTime(Instant.now()).transactionId(transaction.getId()).responseTime(Instant.now()).recipientShortName("credit").amount(transaction.getRecipientAmount()).account(transaction.getRecipientAccount()).currency(transaction.getRecipientCcy()).extId(transaction.getRecipientTrId()).outExtId(String.valueOf(System.currentTimeMillis()));

        return billRepository.save(bill);
    }
}
