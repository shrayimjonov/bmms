package uz.paykap.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import uz.devops.bmcore.dto.request.CheckRequest;
import uz.devops.bmcore.dto.request.CommonCheckRequest;
import uz.devops.bmcore.dto.response.SimpleBillResponse;
import uz.devops.bmcore.enumeration.TransactionStatus;
import uz.javlon.commons.exception.CodedException;
import uz.javlon.commons.exception.ProblemEnum;
import uz.paykap.agent.tran.CommonTranServiceV3;
import uz.paykap.config.ApplicationProperties;
import uz.paykap.domain.Bill;
import uz.paykap.domain.Transaction;
import uz.paykap.event.dto.SendToMailEvent;
import uz.paykap.repository.BillRepository;
import uz.paykap.repository.TransactionRepository;
import uz.paykap.security.SecurityUtils;
import uz.paykap.service.dto.PaymentEditDTO;
import uz.paykap.service.dto.TransactionDTO;
import uz.paykap.service.mapper.TransactionMapper;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class OverpayServiceV2 {

    protected void swapAndSave(Transaction transaction1, Transaction transaction2) {

        log.debug("transaction1 : {}", transaction1);
        log.debug("transaction2 : {}", transaction2);

//        Transaction clone1 = transactionMapper.clone(transaction1);
        Long id1 = transaction1.getId();

//        Transaction clone2 = transactionMapper.clone(transaction2);
        Long id2 = transaction2.getId();

        transaction1.setId(id2);
        transaction2.setId(id1);

        transaction1 = transactionRepository.save(transaction1);
        transaction2 = transactionRepository.save(transaction2);

        log.debug("Clone1 : {}", transaction1);
        log.debug("Clone2 : {}", transaction2);
    }

    public TransactionDTO edit(PaymentEditDTO paymentEditDTO) {

        String login = SecurityUtils.getCurrentUserLogin().orElseThrow(() -> new CodedException(ProblemEnum.USERNAME_NOT_FOUND));

        log.info("###### Edit Request by {} ######: \n{}\n", login, paymentEditDTO);

        Transaction mainTransaction = transactionMapper.clone(transactionRepository.findById(paymentEditDTO.getTransactionId()).orElseThrow(() -> new CodedException(ProblemEnum.BILL_NOT_FOUND)));

        log.debug("Main Transaction : {}", mainTransaction);

        Bill mainBill = billRepository.findById(mainTransaction.getRecipientBillId()).orElseThrow(() -> new CodedException(ProblemEnum.BILL_NOT_FOUND));

        String mfo = "";
        if (mainBill.getRequestJson() != null) {
            mfo = String.valueOf(mainBill.getRequestJson().get("mfo"));
        }

        CommonCheckRequest simpleCheckDTO = new CheckRequest()
            .recipientId(paymentEditDTO.getRecipientId())
            .params(paymentEditDTO.getParams())
            .agentParams(getAgentParams(mainTransaction, mfo))
            .account(paymentEditDTO.getAccount() != null ? paymentEditDTO.getAccount() : mainTransaction.getRecipientAccount())
            .amount(paymentEditDTO.getAmount() != null ? paymentEditDTO.getAmount() : mainTransaction.getRequestAmount())
            .agentTranId(mainTransaction.getRequestTrId())
            .currency(mainTransaction.getRequestCcy())
            .requestGroup(mainTransaction.getRequestGroup())
            .requestSubGroup(mainTransaction.getRequestSubAccount());

        log.debug("checkDto :{}", simpleCheckDTO);

        SimpleBillResponse simpleBillDTO = commonTranService.check(simpleCheckDTO, mainTransaction.getRequestLogin(), mainTransaction.getDate());

        log.debug("Check response :{}", simpleBillDTO);

        if (simpleBillDTO == null) {
            throw new CodedException(ProblemEnum.BILLING_ERROR);
        }

        mainTransaction.setMain(false);

        transactionRepository.save(mainTransaction);

        Transaction checkTransaction = transactionMapper.clone(transactionRepository.findTop1ByIdAndStatus(simpleBillDTO.getId(), TransactionStatus.CHECK_OK).orElseThrow(() -> new CodedException(ProblemEnum.BILLING_ERROR)));

        log.debug("Check transaction :{}", checkTransaction);

//        GatewayQueryResult gatewayQueryResult = gatewaySelectorService.getValidGatewayQueryResultV4(checkTransaction.getRecipientServiceId(), checkTransaction.getSenderName(), checkTransaction.getRequestGroup());
//
//        log.debug("Gateway query result :{}", gatewayQueryResult);
//
//        checkTransaction.requestAmount(paymentEditDTO.getAmount());
//
//        calculationService.calc(checkTransaction, gatewayQueryResult);
//
        checkTransaction.setPayType(mainTransaction.getPayType());

        String content = fillContentEdit(mainTransaction);

//        mailService.sendEmail(applicationProperties.getDefaultEmailToKapital(), "Данные по редактированию платежа", content);

        for (String mail : applicationProperties.getDefaultToMails()) {
            applicationEventPublisher.publishEvent(new SendToMailEvent(this, mail, "Данные по редактированию платежа", content, false, false));
        }
//        log.debug("After calculation :{}", checkTransaction);

//        swapAndSave(mainTransaction, checkTransaction);

        return transactionMapper.toDto(transactionRepository.save(checkTransaction));
    }

    private Map<String, Object> getAgentParams(Transaction transaction, String mfo) {
        Map<String, Object> agentparams = new HashMap<>();

        agentparams.put("requestSubGroup", transaction.getRequestSubAccount());
        agentparams.put("sessionid", transaction.getRequestTrId());
        agentparams.put("systemuserid", transaction.getRequestLogin());
        agentparams.put("machinegroup", transaction.getRequestGroup());
        agentparams.put("mfo", mfo);
        return agentparams;

    }

    private String fillContentEdit(Transaction transaction) {
        String login = SecurityUtils.getCurrentUserLogin().orElseThrow(() -> new CodedException(ProblemEnum.USERNAME_NOT_FOUND));

        return "Редактирование платежа:" +
            " \n GatewayTranId: " + transaction.getRecipientTrId() +
            " \n BankTranId: " + transaction.getId() +
            " \n Account: " + transaction.getRecipientAccount() +
            " \n Amount: " + transaction.getRequestAmount() +
            " \n Recipient: " + transaction.getRecipientName() +
            " \n Gateway: " + transaction.getRecipientGateway() +
            " \n Edited by: " + login;
    }

    private final CommonTranServiceV3 commonTranService;
    private final TransactionRepository transactionRepository;
    private final BillRepository billRepository;
    private final TransactionMapper transactionMapper;
    private final ApplicationProperties applicationProperties;
    private final ApplicationEventPublisher applicationEventPublisher;

    public OverpayServiceV2(CommonTranServiceV3 commonTranService, TransactionRepository transactionRepository, BillRepository billRepository, TransactionMapper transactionMapper, ApplicationProperties applicationProperties, ApplicationEventPublisher applicationEventPublisher) {

        this.commonTranService = commonTranService;
        this.transactionRepository = transactionRepository;
        this.billRepository = billRepository;
        this.transactionMapper = transactionMapper;
        this.applicationProperties = applicationProperties;
        this.applicationEventPublisher = applicationEventPublisher;
    }

}
