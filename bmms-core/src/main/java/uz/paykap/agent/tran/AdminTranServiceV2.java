package uz.paykap.agent.tran;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uz.devops.bmcore.dto.GatewayQueryResult;
import uz.devops.bmcore.dto.request.*;
import uz.devops.bmcore.dto.response.SimpleBillResponse;
import uz.devops.bmcore.enumeration.RequestType;
import uz.devops.bmcore.enumeration.TransactionStatus;
import uz.javlon.commons.base.ResultData;
import uz.javlon.commons.exception.CodedException;
import uz.javlon.commons.exception.ProblemEnum;
import uz.javlon.commons.result.HasResult;
import uz.paykap.agent.tran.dto.CalculationService;
import uz.paykap.domain.Bill;
import uz.paykap.domain.Recipient;
import uz.paykap.domain.Transaction;
import uz.paykap.gateway.GatewayServiceV3;
import uz.paykap.repository.BillRepository;
import uz.paykap.repository.RecipientRepository;
import uz.paykap.repository.TransactionRepository;
import uz.paykap.security.SecurityUtils;
import uz.paykap.sender.SenderResolverV3;
import uz.paykap.service.TransactionService;
import uz.paykap.service.cached.GatewaySelectorService;

import java.time.Instant;
import java.util.Collections;

@Slf4j
@Service
@AllArgsConstructor
public class AdminTranServiceV2 {

    public static final String OK = "OK";
    public static final String INTERNAL = "internal";

    private final GatewaySelectorService gatewaySelectorService;

    private void validateAmountLimits(Long requestAmount, Long recipientMinAmount, Long recipientMaxAmount) {
        if (recipientMinAmount > requestAmount) {
            throw new CodedException(ProblemEnum.LIMIT_EXCEEDED);
        }

        log.debug("OK - Min limit not exceeded : {}", requestAmount);

        if (recipientMaxAmount < requestAmount) {
            throw new CodedException(ProblemEnum.LIMIT_EXCEEDED);
        }

        log.debug("OK - Max limit not exceeded : {}", requestAmount);
    }

    public SimpleBillResponse pay(CommonPayRequest payDTO) {
        String login = SecurityUtils.getCurrentUserLogin().orElseThrow(() -> new CodedException(ProblemEnum.USERNAME_NOT_FOUND));

        return pay(payDTO, login);
    }

    public SimpleBillResponse innerCancel(CommonCancelRequest cancelDTO, String login) {

        if (cancelDTO == null) {
            throw new CodedException(ProblemEnum.INVALID_PARAMS);
        }

        log.info("###### Inner cancel Request from {} ######: \n{}\n", login, cancelDTO);

        Transaction transaction = transactionRepository.findTop1ByIdAndStatus(cancelDTO.getBillId(), TransactionStatus.SUCCESS).orElseThrow(() -> new CodedException(ProblemEnum.BILL_NOT_FOUND));

        log.debug("Success transaction :{}", transaction.toString());

        Recipient recipient = recipientRepository.findTop1ByShortName(transaction.getRecipientName()).orElseThrow(() -> new CodedException(ProblemEnum.SERVICE_NOT_FOUND));

        log.debug("Recipient :{}", recipient.toString());

        if (recipient.isCancel() == Boolean.TRUE) {
            throw new CodedException("cancel", "cancel_is_on");
        }

//        String agentLogin = transaction.getRequestLogin();

        cancelDTO.setBillId(transaction.getRecipientBillId());

        Bill payBill = billRepository.findTop1ByIdAndType(transaction.getRecipientBillId(), RequestType.PAY).orElseThrow(() -> new CodedException(ProblemEnum.BILL_NOT_FOUND));

        log.debug("Found payBill : {}", payBill);

        Bill bill = new Bill()
                .type(RequestType.CANCEL)
                .login(login)
                .requestJson(payBill.getRequestJson())
//            .sessionId(payBill.getSessionId())
                .currency(payBill.getCurrency())
                .amountRatio(payBill.getAmountRatio())
//            .profit(payBill.getProfit())
                .recipientId(payBill.getRecipientId())
                .recipientShortName(payBill.getRecipientShortName())
                .account(payBill.getAccount())
                .amount(payBill.getAmount())
//            .payId(String.valueOf(payBill.getId()))
                .requestTime(Instant.now())
                .responseTime(Instant.now())
                .requestJson(Collections.emptyMap())
//            .fraudId(0L)
                ;

        bill.success(true)
                .responseCode("CANCELLED")
                .responseText("CANCELLED")
                .responseTime(Instant.now())

        ;

        billRepository.save(bill);

        transaction
                .recipientStatus("does not exist")
                .recipientStatusText("does not exist")
                .recipientSuccess(false)
                .status(TransactionStatus.CANCELLED)
                .requestLogin(login)
                .recipientBillId(bill.getId())
//            .recipientBillId(result.getId())
//
                .date(transaction.getDate())
        ;

        transactionRepository.save(transaction);

        SimpleBillResponse result = new SimpleBillResponse();
        result.setId(transaction.getId());
        result.setRecipientShortName(transaction.getRecipientName());
        result.amountRatio(recipient.getAmountRatio());
        result.amount(transaction.getRequestAmount());
        result.account(transaction.getRecipientAccount());
        result.currency(transaction.getCcyRate());
        result.success(true);

        result.setStatus(transaction.getStatus());

        log.info("###### Inner cancel Response ######: \n{}\n", result);
        return result;
    }

    public SimpleBillResponse cancel(CommonCancelRequest cancelDTO, String login) {

        if (cancelDTO == null) {
            throw new CodedException(ProblemEnum.INVALID_PARAMS);
        }

//        String login = SecurityUtils.getCurrentUserLogin().orElseThrow(() -> new CodedException(ProblemEnum.USERNAME_NOT_FOUND));

        log.info("###### Cancel Request from {} ######: \n{}\n", login, cancelDTO);

        Transaction transaction = transactionRepository.findTop1ByIdAndStatus(cancelDTO.getBillId(), TransactionStatus.SUCCESS).orElseThrow(() -> new CodedException(ProblemEnum.BILL_NOT_FOUND));

        String agentLogin = transaction.getRequestLogin();

        Recipient recipient = recipientRepository.findTop1ByShortName(transaction.getRecipientName()).orElseThrow(() -> new CodedException(ProblemEnum.SERVICE_NOT_FOUND));

        log.debug("Recipient :{}", recipient.toString());

        if (recipient.isCancel() == Boolean.FALSE) {
            throw new CodedException("cancel", "cancel_is_off");
        }

        cancelDTO.setBillId(transaction.getRecipientBillId());

        //tran id into cancelDTO
        cancelDTO.setTranId(transaction.getId());

        GatewayQueryResult gatewayQueryResult = gatewaySelectorService.getValidGatewayQueryResultV5(transaction.getRecipientServiceId(), login, transaction.getRequestGroup());

        SharedCancelDTO sharedCancelDTO = new SharedCancelDTO()
                .billId(transaction.getRecipientBillId())
                .tranId(transaction.getId())
                .login(agentLogin)
                .recipientParams(gatewayQueryResult)
                .recipientTrId(transaction.getRecipientTrId())
                .gatewayAmount(transaction.getGatewayAmount());

        SimpleBillResponse result = gatewayServiceV3.cancel(sharedCancelDTO);

        if (result == null) {
            throw new CodedException(ProblemEnum.NOT_IMPLEMENTED);
        }

        transaction
                .recipientStatus(result.isSuccess() == Boolean.TRUE ? "cancelled" : "cancel error")
                .recipientName(result.getRecipientShortName())
                .recipientStatusText(result.getResponseText())
                .recipientSuccess(result.isSuccess() == Boolean.FALSE)
                .status(result.isSuccess() == Boolean.TRUE ? TransactionStatus.RECIPIENT_CANCELLED : TransactionStatus.CANCEL_FAILED)
                .requestLogin(login)
                .date(transaction.getDate())
        ;

        if (result.isSuccess() == Boolean.TRUE) {
            transaction
                    .recipientDate(result.getResponseTime())
                    .recipientBillId(result.getId())
                    .recipientTrId(result.getExtId());
            log.debug("Cancel in recipient success");
//            depositService.increaseAgentDeposit(agentLogin, transaction.getRecipientAmount());

        }

        transaction = transactionRepository.save(transaction);

        if (result.isSuccess() == Boolean.TRUE) {
            ResultData<Transaction> senderCancel = senderResolver.reverse(transaction.getPayType(), transaction, agentLogin);
            log.debug("Sender cancel: {}", senderCancel);

            transaction
                    .senderStatus(senderCancel.isSuccess() == Boolean.TRUE ? "cancelled" : "cancel failed")
                    .senderSuccess(senderCancel.isSuccess() == Boolean.FALSE)
                    .senderDate(Instant.now())
                    .status(senderCancel.isSuccess() == Boolean.TRUE ? TransactionStatus.CANCELLED : TransactionStatus.CANCEL_FAILED);

            if (senderCancel.isSuccess() == Boolean.TRUE) {
                transaction
                        .status(TransactionStatus.CANCELLED);
                transaction = transactionRepository.save(transaction);

            }
        }


        // Tran.id is main id
        result.setId(transaction.getId());
        result.setStatus(transaction.getStatus());

        log.info("###### Cancel Response ######: \n{}\n", result);

        return result;
    }

    //TODO - Use CommonTranV2Service
    public SimpleBillResponse pay(CommonPayRequest payDTO, String login) {

        log.info("\n###### Pay Request from {} ######: \n{}\n", login, payDTO);
        Transaction transaction;

        transaction = transactionRepository.findTop1ByIdAndStatus(payDTO.getBillId(), TransactionStatus.CHECK_OK).orElseThrow(() -> new CodedException(ProblemEnum.BILL_NOT_FOUND));

        log.debug("Check transaction :{}", transaction.toString());

        if (transaction.getRequestAmount() == null) {

            log.debug("Amount NOT given on check, have to calculate financial information");

            if (payDTO.getAmount() == null) {

                log.warn("Amount NOT given on check and NOT in pay, throwing exception");

                throw new CodedException(ProblemEnum.INVALID_PARAMS, "amount");
            }

            log.debug("Amount GIVEN on pay, calculate");

            transaction.requestAmount(payDTO.getAmount());

            GatewayQueryResult gatewayQueryResult = gatewaySelectorService.getValidGatewayQueryResultV5(transaction.getRecipientServiceId(), transaction.getSenderName(), transaction.getRequestGroup());

//            validateAmountLimits(payDTO.getAmount(), gatewayQueryResult.getRecipientMinAmount(), gatewayQueryResult.getRecipientMaxAmount());

            calculationService.calc(transaction, gatewayQueryResult);
        }

        transaction.payType(payDTO.getType());

        transaction = transactionRepository.save(transaction);

        //TODO
        final HasResult canAgentProceed = senderResolver.canAgentProceed(payDTO.getType(), transaction.getSenderName(), payDTO, transaction.getSenderAmount());

        if (!canAgentProceed.isSuccess()) {
            log.debug("sender name : " + transaction.getSenderName() + " sender amount : " + transaction.getSenderAmount() + " pay type : " + payDTO.getType());
            throw new CodedException(ProblemEnum.INSUFFICIENT_FUNDS_ON_DEPOSIT, canAgentProceed.getMessage());
        }
//        ResultData<Transaction> debit;

        GatewayQueryResult gatewayQueryResult = gatewaySelectorService.getValidGatewayQueryResultV5(transaction.getRecipientServiceId(), login, transaction.getRequestGroup());

//        if (payDTO.getType() != PayType.UZCARD) {
//            debit = senderResolver.debit(payDTO, transaction, transaction.getSenderName());
//        } else {
//            transaction
//                .senderAmount(payDTO.getAmount())
//                .senderCcy(transaction.getRecipientCcy())
//                .senderDate(Instant.now())
//                .senderGateway(UzcardDataAdapter.UZCARD_DEBIT_GATEWAY_SHORT_NAME)
//                .senderStatus("OK")
//                .senderStatusText("OK")
//                .senderSuccess(true)
//                .requestLogin(login)
//            ;
//            debit = new ResultData<>(transaction);
//        }

//        if (!debit.isSuccess()) {
//            throw debit.getException();
//        }

        payDTO.setBillId(transaction.getRecipientBillId());

        payDTO.setGatewayAmount(transaction.getGatewayAmount());

        SharedPayDTO sharedPayDTO = new SharedPayDTO()
                .login(login)
                .payDTO(payDTO)
                .recipientParams(gatewayQueryResult)
                .tranId(transaction.getId())
                .gatewayAmount(transaction.getGatewayAmount());

        SimpleBillResponse result = gatewayServiceV3.pay(sharedPayDTO);

        TransactionStatus status = result.getStatus() != TransactionStatus.NEW ? result.getStatus() : (result.getSuccess() ? TransactionStatus.SUCCESS : TransactionStatus.FAILED);

        transaction
                .recipientDate(result.getResponseTime())
                .recipientStatus(String.valueOf(result.isSuccess()))
                .recipientName(result.getRecipientShortName())
                .recipientStatusText(result.getResponseText())
                .recipientSuccess(result.isSuccess())
                .status(status)
                .recipientBillId(result.getId())
                .recipientTrId(result.getExtId())
                .date(transaction.getDate())
        ;

        transactionRepository.save(transaction);

        if (status == TransactionStatus.FAILED) {
            senderResolver.reverse(payDTO.getType(), transaction, transaction.getRequestLogin());
        }

        // Tran.id is main id
        result.setId(transaction.getId());
        result.setStatus(transaction.getStatus());
        result.setAmount(transaction.getRequestAmount());

        log.info("###### Pay Response ######: \n{}\n", result);

        return result;
    }

    public SimpleBillResponse status(CommonStatusRequest statusDTO) {
        return transactionService.getStatusSimpleDto(statusDTO);
    }

    private final TransactionRepository transactionRepository;
    private final TransactionService transactionService;
    private final RecipientRepository recipientRepository;
    private final SenderResolverV3 senderResolver;
    private final CalculationService calculationService;
    private final BillRepository billRepository;
    private final GatewayServiceV3 gatewayServiceV3;
}
