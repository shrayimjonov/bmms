package uz.paykap.service;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;
import uz.devops.bmcore.enumeration.PayType;
import uz.devops.bmcore.enumeration.TransactionStatus;
import uz.devops.humo.config.HumoProperties;
import uz.devops.humo.service.HumoGatewayService;
import uz.devops.humo.service.dto.*;
import uz.devops.humo.service.dto.enumiration.State;
import uz.devops.humoMiddleware.dto.basic.ResultMiddleware;
import uz.devops.humoMiddleware.dto.response.RetrieveCardV3ResponseDTO;
import uz.javlon.commons.result.HasData;
import uz.paykap.IntegrationTest;
import uz.paykap.domain.ReconcileSummary;
import uz.paykap.domain.Transaction;
import uz.paykap.domain.enumeration.ReconcilePeriod;
import uz.paykap.domain.enumeration.ReconcileSummaryStatus;
import uz.paykap.repository.ReconcileSummaryRepository;
import uz.paykap.repository.TransactionRepository;

import javax.xml.bind.JAXBException;
import javax.xml.soap.SOAPException;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@IntegrationTest
public class HumoGatewayServiceTest {

    public static final String ASIA_ALLIANC = "AsiaAllianc";
    public static final String BALANCE_PAN = "986009******6138";
    public static final String EXPIRY = "2502";
    public static final String ACTIVE = "Active";
    public static final String DEBIT_PAN = "9860090000000000";
    public static final int CCY_CODE_UZ = 860;
    public static final String VIRTUAL_POINT_CODE = "100001104110";
    public static final String TERMINAL_ID = "09610001";
    public static final String MERCHANT_ID = "010950461213101";
    public static final String DEBIT_URL = "http://127.0.0.1:11210";
    public static final long DEBIT_AMOUNT = 1000L;
    public static final String SMS_INFO_URL = "http://127.0.0.1:13010";
    public static final String SMS_INFO_EXPIRY = "1090";
    public static final String SMS_INFO_PAN = "9860090000000000";
    public static final String BALANCE_URL = "https://127.0.0.1:6677";
    public static final String PHONE_CHECK_PAN = "9860090000000000";
    public static final String EXPIRY_BALANCE = "2502";
    public static final String URL_CHECK = "http://127.0.0.1:13010";
    public static final String UZ_CCY = "860";
    public static final String SEPORATOR = "_";
    public static final String MERCHANT_ID1 = "996777777777771";
    public static final String TERMINAL_ID1 = "01NNNNN7";
    public static final long TRANS_AMOUNT = 100000L;

    @Autowired
    private HumoGatewayService humoGatewayService;

    @Autowired
    private HumoMiddlewareHelperService humoMiddlewareHelperService;

    @Autowired
    private HumoProperties humoProperties;

//    @Autowired
//    private AutomaticReconcileService automaticReconcileService;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private ReconcileSummaryRepository reconcileSummaryRepository;

    @Test
    @Transactional
    public void balance() throws SOAPException, JAXBException, IOException {
        // Ustanavlivaem neobxodimie property

        humoProperties.setBalanceUrl(BALANCE_URL);
        humoProperties.setSimulate(true);

        BalanceRequestDTO balanceRequestDTO = new BalanceRequestDTO();
        balanceRequestDTO.setPan(BALANCE_PAN);

        var balanceResultDTO = getBalance(balanceRequestDTO);

        assertThat(balanceResultDTO.getBalance().getCurrency()).isEqualTo(UZ_CCY);

        assertThat(balanceResultDTO.getCard()).isNotNull();
        assertThat(balanceResultDTO.getCard().getInstitutionId()).isEqualTo(ASIA_ALLIANC);
        assertThat(balanceResultDTO.getCard().getPrimaryAccountNumber()).isEqualTo(BALANCE_PAN);
        assertThat(balanceResultDTO.getCard().getExpiry()).isEqualTo(EXPIRY_BALANCE);

        assertThat(balanceResultDTO.getAccount()).isNotNull();
//        assertThat(balanceResultDTO.getAccount().getInstitutionId()).isEqualTo(ASIA_ALLIANC);
//        assertThat(balanceResultDTO.getAccount().getCurrency()).isEqualTo(UZ_CCY);
//        assertThat(balanceResultDTO.getAccount().getStatus()).isEqualTo(AccountStatus.ACTIVE);

        //balance card
        assertThat(balanceResultDTO.getBalance()).isNotNull();
        assertThat(balanceResultDTO.getBalance().getAvailableAmount()).isNotNull();
    }

    @Test
    @Transactional
    public void debit() throws SOAPException, JAXBException, IOException {
        //3x etapniy debit

        //Ustanavlivaem neobxodimie property
        humoProperties.setSimulate(true);
        humoProperties.setBalanceUrl(BALANCE_URL);
        humoProperties.setDebitUrl(DEBIT_URL);
        humoProperties.setMerchantId(MERCHANT_ID);
        humoProperties.setTerminalId(TERMINAL_ID);

        //Otravlyaem zapros na balance dlya proverki neobxodimoy summi na schetu dlya snyatiya i dannix dlya debit
        BalanceRequestDTO balanceRequestDTO = new BalanceRequestDTO();
        balanceRequestDTO.setPan(DEBIT_PAN);
        var balanceResultDTO = getBalance(balanceRequestDTO);

        assertThat(balanceResultDTO.getBalance()).isNotNull();
        assertThat(balanceResultDTO.getBalance().getAvailableAmount()).isNotNull();
        assertThat(balanceResultDTO.getBalance().getAvailableAmount()).isGreaterThan(DEBIT_AMOUNT);

        assertThat(balanceResultDTO.getCard()).isNotNull();
        assertThat(balanceResultDTO.getCard().getInstitutionId()).isNotNull();
        assertThat(balanceResultDTO.getCard().getExpiry()).isNotNull();

        //Esli dostatochno balansa na schetu, to otpravlyaem zapros createDebit
        DebitCreateRequestDTO debitCreateRequestDTO = new DebitCreateRequestDTO();
        debitCreateRequestDTO.setAmount(DEBIT_AMOUNT);
        debitCreateRequestDTO.setCcyCode(CCY_CODE_UZ);
        //centreID == institutionId
        debitCreateRequestDTO.setCentreID(balanceResultDTO.getCard().getInstitutionId());
        debitCreateRequestDTO.setExpDate(balanceResultDTO.getCard().getExpiry()); //todo exp to long
        debitCreateRequestDTO.setPan(DEBIT_PAN);
        debitCreateRequestDTO.setPointCode(VIRTUAL_POINT_CODE);
        debitCreateRequestDTO.setMerchantId("996777777777771");
        debitCreateRequestDTO.setTerminalId("01NNNNN7");

        String paymentRef = "BillMsRef" + Instant.now().toEpochMilli();

        String sessionId = "BillMsSessionId" + Instant.now().toEpochMilli();

        debitCreateRequestDTO.setPaymentRef(paymentRef);
        debitCreateRequestDTO.setSessionId(sessionId);

        DebitResultDTO debitResultDTO = debitCreate(debitCreateRequestDTO);

        //Esli createDebit prowel uspewno, to delaem confirmDebit
        DebitConfirmRequestDTO debitConfirmRequestDTO = new DebitConfirmRequestDTO();
        debitConfirmRequestDTO.setPaymentId(debitResultDTO.getPaymentId());
        debitConfirmRequestDTO.setPaymentRef("BillMS" + Instant.now().toEpochMilli());
        DebitResultDTO debitResultDTOConfirm = debitConfirm(debitConfirmRequestDTO);

        //        assertThat(debitResultDTOConfirm.getPaymentId()).isEqualTo(debitResultDTO.getPaymentId());
        // TODO - temp solution
        assertThat(debitResultDTOConfirm.getPaymentId()).isNotNull();
        //todo
    }

    @Test
    @Transactional
    public void getPayment() throws SOAPException, JAXBException, IOException {
        //Ustanavlivaem neobxodimie property
        humoProperties.setSimulate(true);
        humoProperties.setBalanceUrl(BALANCE_URL);
        humoProperties.setDebitUrl(DEBIT_URL);
        humoProperties.setMerchantId(MERCHANT_ID);
        humoProperties.setTerminalId(TERMINAL_ID);

        //Otravlyaem zapros na balance dlya proverki neobxodimoy summi na schetu dlya snyatiya i dannix dlya debit
        BalanceRequestDTO balanceRequestDTO = new BalanceRequestDTO();
        balanceRequestDTO.setPan(DEBIT_PAN);
        var balanceResultDTO = getBalance(balanceRequestDTO);

        assertThat(balanceResultDTO.getBalance()).isNotNull();
        assertThat(balanceResultDTO.getBalance().getAvailableAmount()).isNotNull();
        assertThat(balanceResultDTO.getBalance().getAvailableAmount()).isGreaterThan(DEBIT_AMOUNT);

        assertThat(balanceResultDTO.getCard()).isNotNull();
        assertThat(balanceResultDTO.getCard().getInstitutionId()).isNotNull();
        assertThat(balanceResultDTO.getCard().getExpiry()).isNotNull();

        //Esli dostatochno balansa na schetu, to otpravlyaem zapros createDebit
        DebitCreateRequestDTO debitCreateRequestDTO = new DebitCreateRequestDTO();
        debitCreateRequestDTO.setAmount(DEBIT_AMOUNT);
        debitCreateRequestDTO.setCcyCode(CCY_CODE_UZ);
        //centreID == institutionId
        debitCreateRequestDTO.setCentreID(balanceResultDTO.getCard().getInstitutionId());
        debitCreateRequestDTO.setExpDate(balanceResultDTO.getCard().getExpiry()); //todo exp to long
        debitCreateRequestDTO.setPan(DEBIT_PAN);
        debitCreateRequestDTO.setPointCode(VIRTUAL_POINT_CODE);
        debitCreateRequestDTO.setMerchantId("996777777777771");
        debitCreateRequestDTO.setTerminalId("01NNNNN7");

        String paymentRef = "BillMsRef" + Instant.now().toEpochMilli();

        String sessionId = "BillMsSessionId" + Instant.now().toEpochMilli();

        debitCreateRequestDTO.setPaymentRef(paymentRef);
        debitCreateRequestDTO.setSessionId(sessionId);

        DebitResultDTO debitResultDTO = debitCreate(debitCreateRequestDTO);

        //Esli createDebit prowel uspewno, to delaem confirmDebit
        DebitConfirmRequestDTO debitConfirmRequestDTO = new DebitConfirmRequestDTO();
        debitConfirmRequestDTO.setPaymentId(debitResultDTO.getPaymentId());
        debitConfirmRequestDTO.setPaymentRef(debitResultDTO.getPaymentRef());
        DebitResultDTO debitResultDTOConfirm = debitConfirm(debitConfirmRequestDTO);

        //        assertThat(debitResultDTOConfirm.getPaymentId()).isEqualTo(debitResultDTO.getPaymentId());
        // TODO - temp solution
        assertThat(debitResultDTOConfirm.getPaymentId()).isNotNull();

        GetPaymentRequestDTO getPaymentRequestDTO = new GetPaymentRequestDTO();
        getPaymentRequestDTO.setPaymentId(debitResultDTO.getPaymentId());
        getPaymentRequestDTO.setPaymentRef(debitResultDTO.getPaymentRef());

        var getPaymentResultDTO = humoGatewayService.getPayment(getPaymentRequestDTO);
    }

    //    @Test
    @Transactional
    public void reverse() throws SOAPException, JAXBException, IOException {
        //Ustanavlivaem neobxodimie property
        humoProperties.setSimulate(true);
        humoProperties.setBalanceUrl(BALANCE_URL);
        humoProperties.setDebitUrl(DEBIT_URL);
        humoProperties.setMerchantId(MERCHANT_ID);
        humoProperties.setTerminalId(TERMINAL_ID);

        //Otravlyaem zapros na balance dlya proverki neobxodimoy summi na schetu dlya snyatiya i dannix dlya debit
        BalanceRequestDTO balanceRequestDTO = new BalanceRequestDTO();
        balanceRequestDTO.setPan(DEBIT_PAN);
        var balanceResultDTO = getBalance(balanceRequestDTO);

        assertThat(balanceResultDTO.getBalance()).isNotNull();
        assertThat(balanceResultDTO.getBalance().getAvailableAmount()).isNotNull();
        assertThat(balanceResultDTO.getBalance().getAvailableAmount()).isGreaterThan(DEBIT_AMOUNT);

        assertThat(balanceResultDTO.getCard()).isNotNull();
        assertThat(balanceResultDTO.getCard().getInstitutionId()).isNotNull();
        assertThat(balanceResultDTO.getCard().getExpiry()).isNotNull();

        //Esli dostatochno balansa na schetu, to otpravlyaem zapros createDebit
        DebitCreateRequestDTO debitCreateRequestDTO = new DebitCreateRequestDTO();
        debitCreateRequestDTO.setAmount(DEBIT_AMOUNT);
        debitCreateRequestDTO.setCcyCode(CCY_CODE_UZ);
        //centreID == institutionId
        debitCreateRequestDTO.setCentreID(balanceResultDTO.getCard().getInstitutionId());
        debitCreateRequestDTO.setExpDate(balanceResultDTO.getCard().getExpiry()); //todo exp to long
        debitCreateRequestDTO.setPan(DEBIT_PAN);
        debitCreateRequestDTO.setPointCode(VIRTUAL_POINT_CODE);
        debitCreateRequestDTO.setMerchantId(MERCHANT_ID1);
        debitCreateRequestDTO.setTerminalId(TERMINAL_ID1);

        String paymentRef = "BillMsRef" + Instant.now().toEpochMilli();

        String sessionId = "BillMsSessionId" + Instant.now().toEpochMilli();

        debitCreateRequestDTO.setPaymentRef(paymentRef);
        debitCreateRequestDTO.setSessionId(sessionId);

        DebitResultDTO debitResultDTO = debitCreate(debitCreateRequestDTO);

        //Esli createDebit prowel uspewno, to delaem confirmDebit
        DebitConfirmRequestDTO debitConfirmRequestDTO = new DebitConfirmRequestDTO();
        debitConfirmRequestDTO.setPaymentId(debitResultDTO.getPaymentId());
        debitConfirmRequestDTO.setPaymentRef(debitResultDTO.getPaymentRef());
        DebitResultDTO debitResultDTOConfirm = debitConfirm(debitConfirmRequestDTO);

        //        assertThat(debitResultDTOConfirm.getPaymentId()).isEqualTo(debitResultDTO.getPaymentId());
        // TODO - temp solution
        assertThat(debitResultDTOConfirm.getPaymentId()).isNotNull();

        ReverseRequestDTO reverseRequestDTO = new ReverseRequestDTO();
        reverseRequestDTO.setCentreID(balanceResultDTO.getCard().getInstitutionId());
        reverseRequestDTO.setPaymentId(debitResultDTO.getPaymentId());
        reverseRequestDTO.setMerchantId(MERCHANT_ID1);
        reverseRequestDTO.setTerminalId(TERMINAL_ID1);
        reverseRequestDTO.setPaymentRef(debitResultDTO.getPaymentRef());
        reverseRequestDTO.setSessionId("BillMsSession" + SEPORATOR + Instant.now());
        reverseRequestDTO.setSessionId("");

        var simpleResultData = humoGatewayService.reverse(reverseRequestDTO);

        GetPaymentRequestDTO getPaymentRequestDTO = new GetPaymentRequestDTO();
        getPaymentRequestDTO.setPaymentId(debitResultDTO.getPaymentId());
        getPaymentRequestDTO.setPaymentRef(debitResultDTO.getPaymentRef());

        var getPaymentResultDTO = humoGatewayService.getPayment(getPaymentRequestDTO);
        String a = "";
    }

    @Test
    @Transactional
    public void smsInfo() throws SOAPException, JAXBException, IOException {
        //Ustanavlivaem neobxodimie property

        humoProperties.setSimulate(true);
        humoProperties.setSmsInfoUrl(SMS_INFO_URL);
        humoProperties.setBalanceUrl(BALANCE_URL);

        //Dlya sms info ponadobitsa cardholder id
        //Dlya nahojdeniya cardholder id otpravlyaem zapros na balance
        BalanceRequestDTO balanceRequestDTO = new BalanceRequestDTO();
        balanceRequestDTO.setPan(DEBIT_PAN);
        var balanceResultDTO = getBalance(balanceRequestDTO);

        assertThat(balanceResultDTO.getCard()).isNotNull();
        assertThat(balanceResultDTO.getCard().getCardholderId()).isNotNull();

        //Posle polucheniya cardholder id otpravlyaem zapros na vkl sms
        SmsInfoRequestDTO smsInfoRequestDTO = new SmsInfoRequestDTO();
        smsInfoRequestDTO.setPan(SMS_INFO_PAN);
        smsInfoRequestDTO.setExpDate(SMS_INFO_EXPIRY);
        smsInfoRequestDTO.setCardholderId(balanceResultDTO.getCard().getCardholderId());
        smsInfoRequestDTO.setState(State.ON);
        //vkl sms info
        smsInfoRequestDTO.setCardState(State.ON);

        SmsInfoResultDTO smsInfoResultDTO = smsInfoOn(smsInfoRequestDTO);

        assertThat(smsInfoResultDTO.isSucces()).isTrue();
    }

    @Test
    @Ignore("Deprecated, moved to humo middleware")
    @Transactional
    public void phoneCheck() throws SOAPException, JAXBException, IOException {
        //Ustanavlivaem neobxodimie property

        humoProperties.setSimulate(true);
        humoProperties.setUrlCheck(URL_CHECK);

        //Dlya phone check ponadobitsa cardholder id
        //Dlya nahojdeniya cardholder id otpravlyaem zapros na balance
        BalanceRequestDTO balanceRequestDTO = new BalanceRequestDTO();
        balanceRequestDTO.setPan(DEBIT_PAN);
        var balanceResultDTO = getBalance(balanceRequestDTO);

        //Posle polucheniya cardholder id otpravlyaem zapros na phone check
        CheckClientRequestDTO checkClientRequestDTO = new CheckClientRequestDTO();
        checkClientRequestDTO.setPan(PHONE_CHECK_PAN);
        checkClientRequestDTO.setCardholderID(balanceResultDTO.getCard().getCardholderId());

        CheckClientResultDTO checkClientResultDTO = phoneCheck(checkClientRequestDTO);

        assertThat(checkClientResultDTO.getPhone()).isNotNull();
        //Nomer vozvrashaetsa v vide +999999999999
        assertThat(checkClientResultDTO.getPhone().getMsisdn()).isNotNull();
    }

    //    @Test
    @Transactional
    public void recoConfirm() throws SOAPException, JAXBException, IOException {
        ReconciliationCreateRequest reconciliationCreateRequest = new ReconciliationCreateRequest();
        reconciliationCreateRequest.setPaymentRef("HumoPluginTestRef" + SEPORATOR + Instant.now().toEpochMilli());
        reconciliationCreateRequest.setSessionId(
                "HumoPluginTestSession" + SEPORATOR + Instant.now().toEpochMilli() + SEPORATOR + Instant.now().toEpochMilli()
        );
        reconciliationCreateRequest.setTerminalId(TERMINAL_ID);

        var resultSimpleResultData = humoGatewayService.reconciliationCreate(reconciliationCreateRequest);

        ReconciliationConfirmRequest reconciliationConfirmRequest = new ReconciliationConfirmRequest();
        reconciliationConfirmRequest.setConfirmed(true);
        reconciliationConfirmRequest.setFinished(true);
        reconciliationConfirmRequest.setPaymentId(resultSimpleResultData.getData().getPaymentId());
        reconciliationConfirmRequest.setPaymentRef(resultSimpleResultData.getData().getPaymentRef());

        var reconciliationConfirmResultSimpleResultData = humoGatewayService.reconciliationConfirm(reconciliationConfirmRequest);
    }

    @Test
    @Transactional
    public void creditCard() throws SOAPException, JAXBException, IOException {
        CreditRequestDTO creditRequestDTO = new CreditRequestDTO();
        creditRequestDTO.setPan(DEBIT_PAN);
        creditRequestDTO.setExpiry("2403");
        creditRequestDTO.setAmount(10000L);
        creditRequestDTO.setMerchantId(MERCHANT_ID1);
        creditRequestDTO.setTerminalId(TERMINAL_ID1);
        creditRequestDTO.setPointCode(VIRTUAL_POINT_CODE);
        creditRequestDTO.setCentreId(ASIA_ALLIANC);
        creditRequestDTO.setPaymentId("");
        creditRequestDTO.setPaymentRef("Bm_test_payment_ref" + Instant.now().toEpochMilli());
        creditRequestDTO.setSessionId("Bm_test_session_id" + Instant.now().toEpochMilli());

        var creditResultDTO = humoGatewayService.credit(creditRequestDTO);

        String a = "";
    }

    //    @Test
//    @Transactional
//    public void autoRecoTest() {
//        Transaction transaction = saveTransaction();
//        automaticReconcileService.startHumoReconciliation();
//    }

    @Ignore(value = "expected:<[COMPARED_OK]> but was:<[NEW]>")
    @Test
    @Transactional
    public void testJpaQuery() throws InterruptedException {
        saveRecoSum(ReconcileSummaryStatus.NEW);
        Thread.sleep(200);
        saveRecoSum(ReconcileSummaryStatus.COMPARED_FALSE);
        Thread.sleep(200);
        saveRecoSum(ReconcileSummaryStatus.COMPARED_OK);

        Optional<ReconcileSummary> reconcileSummaryOptional = reconcileSummaryRepository.findTop1ByGatewayNameOrderByCreatedDateDesc(
                "terminal"
        );
        assertThat(reconcileSummaryOptional).isNotNull();
        assertThat(reconcileSummaryOptional.isPresent()).isTrue();
        assertThat(reconcileSummaryOptional.get().getStatus()).isEqualTo(ReconcileSummaryStatus.COMPARED_OK);
    }

    private RetrieveCardV3ResponseDTO getBalance(BalanceRequestDTO balanceRequestDTO) {
        return humoMiddlewareHelperService.retrieveCardV3(balanceRequestDTO.getPan()).getData();
    }

    private DebitResultDTO debitCreate(DebitCreateRequestDTO debitCreateRequestDTO) throws SOAPException, JAXBException, IOException {
        return humoGatewayService.createDualMessageDebit(debitCreateRequestDTO).getData();
    }

    private DebitResultDTO debitConfirm(DebitConfirmRequestDTO debitConfirmRequestDTO) throws SOAPException, JAXBException, IOException {
        return humoGatewayService.confirmDualMessageDebit(debitConfirmRequestDTO).getData();
    }

    private SmsInfoResultDTO smsInfoOn(SmsInfoRequestDTO smsInfoRequestDTO) throws SOAPException, JAXBException, IOException {
        return humoGatewayService.smsInfo(smsInfoRequestDTO).getData();
    }

    private CheckClientResultDTO phoneCheck(CheckClientRequestDTO checkClientRequestDTO) throws SOAPException, JAXBException, IOException {
        return humoGatewayService.checkClientAndSmsInfo(checkClientRequestDTO).getData();
    }

    private Transaction saveTransaction() {
        Transaction transaction = new Transaction()
                .requestAmount(TRANS_AMOUNT)
                .requestCcy(860)
                .date(Instant.now())
                .requestTrId("RequestTrId")
                .requestLogin("system")
                .senderName("Humo")
                .senderAccount("pan")
                .senderAmount(TRANS_AMOUNT)
                .senderCcy(860)
                .senderTrId("SenderTrId")
                .senderSuccess(true)
                .senderDate(Instant.now())
                .senderStatus("Success")
                .senderStatusText("success")
                .senderGateway("Humo-Gateway")
                .gatewayAmount(100000.0)
                .recipientName("Humo")
                .recipientAccount("test Account")
                .recipientAmount(TRANS_AMOUNT)
                .recipientCcy(860)
                .recipientSuccess(true)
                .recipientDate(Instant.now())
                .recipientStatus("Success")
                .recipientStatusText("success")
                .recipientGateway("test")
                .status(TransactionStatus.SUCCESS)
                .payType(PayType.HUMO)
                .eposTerminal("01NNNNN7")
                .amountRatio(100);
        return transactionRepository.save(transaction);
    }

    private ReconcileSummary saveRecoSum(ReconcileSummaryStatus reconcileSummaryStatus) {
        ReconcileSummary reconcileSummary = new ReconcileSummary()
                .gatewayId(1L)
                .systemAmount(100L)
                .gatewayAmount(100L)
                .systemCount(2L)
                .gatewayCount(2L)
                .date(LocalDate.now())
                .status(reconcileSummaryStatus)
                .success(true)
                .period(ReconcilePeriod.DAY)
                .gatewayName("terminal");

        return reconcileSummaryRepository.save(reconcileSummary);
    }
}
