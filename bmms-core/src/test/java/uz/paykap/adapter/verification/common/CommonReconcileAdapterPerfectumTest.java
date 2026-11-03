//package uz.paykap.adapter.verification.common;
//
//import org.junit.Test;
//import org.junit.runner.RunWith;
//import org.springframework.beans.factory.annotation.Autowired;
//import uz.paykap.IntegrationTest;
//import org.springframework.test.context.junit4.SpringRunner;
//import org.springframework.transaction.annotation.Transactional;
//import uz.javlon.commons.base.ResultData;
//import uz.paykap.PkbillmsApp;
//import uz.paykap.adapter.verification.dto.ReconciliationRequestDTO;
//import uz.paykap.IntegrationTest;
//import uz.paykap.domain.Gateway;
//import uz.paykap.domain.GatewayParam;
//import uz.paykap.domain.Recipient;
//import uz.paykap.domain.Transaction;
//import uz.paykap.domain.enumeration.MethodType;
//import uz.devops.bmcore.enumeration.PayType;
//import uz.devops.bmcore.enumeration.RequestType;
//import uz.devops.bmcore.enumeration.TransactionStatus;
//import uz.paykap.gateway.common.CommonGatewayService;
//import uz.paykap.gateway.dto.ReconcileGatewayDTO;
//import uz.paykap.repository.GatewayParamRepository;
//import uz.paykap.repository.GatewayRepository;
//import uz.paykap.repository.RecipientRepository;
//import uz.paykap.repository.TransactionRepository;
//import uz.paykap.service.dto.ReconcileManualDTO;
//import uz.paykap.utils.DateUtil;
//import uz.paykap.web.rest.GatewayParamResourceIntTest;
//import uz.paykap.web.rest.GatewayResourceIntTest;
//import uz.paykap.web.rest.RecipientResourceIntTest;
//import uz.paykap.web.rest.ReconciliationResource;
//
//import java.time.Instant;
//import java.time.LocalDate;
//
//import static org.assertj.core.api.Assertions.assertThat;
//
//@RunWith(SpringRunner.class)
//@IntegrationTest
//public class CommonReconcileAdapterPerfectumTest {
//
//    @Autowired
//    private GatewayParamRepository gatewayParamRepository;
//
//    @Autowired
//    private TransactionRepository transactionRepository;
//
//    @Autowired
//    private GatewayRepository gatewayRepository;
//
//    @Autowired
//    private RecipientRepository recipientRepository;
//
//    @Autowired
//    private CommonReconcileAdapter commonReconcileAdapter;
//
//    @Autowired
//    private ReconciliationResource reconciliationResource;
//
//    @Test
//    @Transactional
//    public void getReconcileList() {
//        Recipient recipient = getRecipient();
//
//        saveGatewayParamPerfectumReconcile(recipient);
//
//        ReconcileManualDTO reconcileManualDTO = new ReconcileManualDTO();
//        reconcileManualDTO.setLocalDate(LocalDate.now());
//        reconcileManualDTO.setGatewayService("Perfectum");
//
////        Transaction transaction = saveTransaction(recipient);
//        saveTransaction1(recipient);
//        saveTransaction2(recipient);
//        saveTransaction3(recipient);
//        saveTransaction4(recipient);
//
//        reconciliationResource.reconcileRecipient(reconcileManualDTO);
//
//
//        ReconciliationRequestDTO reconciliationRequestDTO = new ReconciliationRequestDTO();
//
//        reconciliationRequestDTO.setRecipientShortName(recipient.getShortName());
//        reconciliationRequestDTO.setToTime((DateUtil.todayStart()));
//        reconciliationRequestDTO.setFromTime(DateUtil.todayStart());
//
//        ResultData<ReconcileGatewayDTO> gatewayTranDTOS = commonReconcileAdapter.getReconcileList(reconciliationRequestDTO);
//
//        assertThat(gatewayTranDTOS != null);
//    }
//
//    private Recipient getRecipient() {
//        Recipient recipient = RecipientResourceIntTest.createEntity(null);
//        recipient.setShortName("Perfectum");
//        recipient.setServiceId("1");
//        recipient.setCheck(true);
//        recipient.setPay(true);
//        recipient.setActive(true);
//        recipient.setAmountRatio(1);
//        recipient.setGateway(getGateway());
//
//        return recipientRepository.save(recipient);
//    }
//
//    private Gateway getGateway() {
//        Gateway gateway = GatewayResourceIntTest.createEntity(null);
//        gateway.setActive(true);
//
//
//        gateway.setSpringBeanName(CommonGatewayService.NAME);
//        gateway.setName("Perfectum");
//        return gatewayRepository.save(gateway);
//    }
//
//    private void saveGatewayParamPerfectumReconcile(Recipient recipient) {
//        GatewayParam gatewayParam = GatewayParamResourceIntTest.createEntity(null);
//        gatewayParam.setActive(true);
//        gatewayParam.setRecipient(recipient);
//        gatewayParam.setType(RequestType.RECONCILE);
//        gatewayParam.setHttpMethodType(MethodType.GET);
//        gatewayParam.setTimeout(30000);
//        gatewayParam.setUrl("https://127.0.0.1/pay_net_v1.php?act=2&username=${username}&password=${password}&verification_date=27.09.2018");
//        gatewayParam.setRequestMediaType("application/xml");
////        gatewayParam.setHeaders("{\"SOAPAction\":\"\"}");
//        gatewayParam.setPassword("Kap345RwcPer1");
//        gatewayParam.setResponseMediaType("text/plain");
//        gatewayParam.setUsername("kapitalbank");
//        gatewayParam.setBody("");
//        gatewayParam.setStatusQuery("//*[local-name()='status']");
//        gatewayParam.setMessageQuery("//*[local-name()='errorMsg']");
//        gatewayParam.successCode("0");
//        gatewayParam.setDataQueries("{\"fieldSeparator\": \";\",\"groupSeparator\":\"\\n\",\"queryAmount\": \"2\",\"queryAccount\": \"1\",\"queryTranId\": \"3\",\"queryExtTime\": \"4\", \"requestDateFormat\": \"dd.MM.yyyy\"}");
//        gatewayParamRepository.save(gatewayParam);
//    }
//
//    private Transaction saveTransaction(Recipient recipient) {
//        Transaction transaction = new Transaction()
//            .requestAmount(100000L)
//            .requestCcy(860)
//            .requestTrId("qwerty")
//            .requestLogin("system")
//            .requestGroup("system")
//            .senderName("system")
//            .senderAccount("12345")
//            .senderServiceId(recipient.getId())
//            .senderAmount(100000L)
//            .senderCcy(860)
//            .senderTrId("qwerty")
//            .senderSuccess(true)
//            .senderDate(Instant.now())
//            .senderStatus("success")
//            .senderStatusText("success")
//            .senderBillId(1L)
//            .senderGateway(recipient.getGateway().getName())
//            .gatewayAmount(1000.0)
//            .recipientName(recipient.getShortName())
//            .recipientAccount("qwerty")
//            .recipientServiceId(recipient.getId())
//            .recipientAmount(1000L)
//            .recipientCcy(860)
//            .recipientTrId("qwerty")
//            .recipientSuccess(true)
//            .recipientDate(Instant.now())
//            .recipientStatus("success")
//            .recipientStatusText("success")
//            .recipientBillId(12345L)
//            .recipientGateway("Perfectum")
//            .status(TransactionStatus.SUCCESS)
//            .date(Instant.now())
//            .payType(PayType.CASH);
//        return transactionRepository.save(transaction);
//    }
//
//    private Transaction saveTransaction1(Recipient recipient) {
//        Transaction transaction = new Transaction()
//
//            .requestAmount(100000L)
//            .requestCcy(860)
//            .requestTrId("qwerty")
//            .requestLogin("system")
//            .requestGroup("system")
//            .senderName("system")
//            .senderAccount("8095858")
//            .senderServiceId(recipient.getId())
//            .senderAmount(100000L)
//            .senderCcy(860)
//            .senderTrId("qwerty")
//            .senderSuccess(true)
//            .senderDate(Instant.now())
//            .senderStatus("success")
//            .senderStatusText("success")
//            .senderBillId(1L)
//            .senderGateway(recipient.getGateway().getName())
//            .gatewayAmount(1000.0)
//            .recipientName(recipient.getShortName())
//            .recipientAccount("8095858")
//            .recipientServiceId(recipient.getId())
//            .recipientAmount(1000L)
//            .recipientCcy(860)
//            .recipientTrId("qwerty1")
//            .recipientSuccess(true)
//            .recipientDate(Instant.now())
//            .recipientStatus("success")
//            .recipientStatusText("success")
//            .recipientBillId(12345L)
//            .recipientGateway("Perfectum")
//            .status(TransactionStatus.SUCCESS)
//            .date(Instant.now())
//            .payType(PayType.CASH);
//        return transactionRepository.save(transaction);
//    }
//
//    private Transaction saveTransaction2(Recipient recipient) {
//        Transaction transaction = new Transaction()
//
//            .requestAmount(100000L)
//            .requestCcy(860)
//            .requestTrId("qwerty")
//            .requestLogin("system")
//            .requestGroup("system")
//            .senderName("system")
//            .senderAccount("8095858")
//            .senderServiceId(recipient.getId())
//            .senderAmount(100000L)
//            .senderCcy(860)
//            .senderTrId("qwerty")
//            .senderSuccess(true)
//            .senderDate(Instant.now())
//            .senderStatus("success")
//            .senderStatusText("success")
//            .senderBillId(1L)
//            .senderGateway(recipient.getGateway().getName())
//            .gatewayAmount(1000.0)
//            .recipientName(recipient.getShortName())
//            .recipientAccount("8095858")
//            .recipientServiceId(recipient.getId())
//            .recipientAmount(1000L)
//            .recipientCcy(860)
//            .recipientTrId("qwerty2")
//            .recipientSuccess(true)
//            .recipientDate(Instant.now())
//            .recipientStatus("success")
//            .recipientStatusText("success")
//            .recipientBillId(12345L)
//            .recipientGateway("Perfectum")
//            .status(TransactionStatus.SUCCESS)
//            .date(Instant.now())
//            .payType(PayType.CASH);
//        transaction.setId(300001253L);
//        return transactionRepository.save(transaction);
//    }
//
//    private Transaction saveTransaction3(Recipient recipient) {
//        Transaction transaction = new Transaction()
//
//            .requestAmount(100000L)
//            .requestCcy(860)
//            .requestTrId("qwerty")
//            .requestLogin("system")
//            .requestGroup("system")
//            .senderName("system")
//            .senderAccount("8095858")
//            .senderServiceId(recipient.getId())
//            .senderAmount(100000L)
//            .senderCcy(860)
//            .senderTrId("qwerty")
//            .senderSuccess(true)
//            .senderDate(Instant.now())
//            .senderStatus("success")
//            .senderStatusText("success")
//            .senderBillId(1L)
//            .senderGateway(recipient.getGateway().getName())
//            .gatewayAmount(1000.0)
//            .recipientName(recipient.getShortName())
//            .recipientAccount("8095858")
//            .recipientServiceId(recipient.getId())
//            .recipientAmount(1000L)
//            .recipientCcy(860)
//            .recipientTrId("qwerty3")
//            .recipientSuccess(true)
//            .recipientDate(Instant.now())
//            .recipientStatus("success")
//            .recipientStatusText("success")
//            .recipientBillId(12345L)
//            .recipientGateway("Perfectum")
//            .status(TransactionStatus.SUCCESS)
//            .date(Instant.now())
//            .payType(PayType.CASH);
//        transaction.setId(300001251L);
//        return transactionRepository.save(transaction);
//    }
//
//    private Transaction saveTransaction4(Recipient recipient) {
//        Transaction transaction = new Transaction()
//
//            .requestAmount(100000L)
//            .requestCcy(860)
//            .requestTrId("qwerty")
//            .requestLogin("system")
//            .requestGroup("system")
//            .senderName("system")
//            .senderAccount("8095858")
//            .senderServiceId(recipient.getId())
//            .senderAmount(100000L)
//            .senderCcy(860)
//            .senderTrId("qwerty4")
//            .senderSuccess(true)
//            .senderDate(Instant.now())
//            .senderStatus("success")
//            .senderStatusText("success")
//            .senderBillId(1L)
//            .senderGateway(recipient.getGateway().getName())
//            .gatewayAmount(1000.0)
//            .recipientName(recipient.getShortName())
//            .recipientAccount("8095858")
//            .recipientServiceId(recipient.getId())
//            .recipientAmount(1000L)
//            .recipientCcy(860)
//            .recipientTrId("qwerty")
//            .recipientSuccess(true)
//            .recipientDate(Instant.now())
//            .recipientStatus("success")
//            .recipientStatusText("success")
//            .recipientBillId(12345L)
//            .recipientGateway("Perfectum")
//            .status(TransactionStatus.SUCCESS)
//            .date(Instant.now())
//            .payType(PayType.CASH);
//        transaction.setId(300001254L);
//        return transactionRepository.save(transaction);
//    }
//}
