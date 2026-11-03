package uz.paykap.sender.impl;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uz.devops.bmcore.dto.request.CommonPayRequest;
import uz.devops.bmcore.enumeration.PayType;
import uz.javlon.commons.base.ResultData;
import uz.javlon.commons.exception.CodedException;
import uz.javlon.commons.exception.ProblemEnum;
import uz.javlon.commons.result.CommonResult;
import uz.javlon.commons.result.CommonResultData;
import uz.javlon.commons.result.HasData;
import uz.javlon.commons.result.HasResult;
import uz.paykap.config.Constants;
import uz.paykap.domain.Transaction;
import uz.paykap.sender.SenderService;
import uz.paykap.service.DepositService;
import uz.paykap.service.TransactionService;

import java.time.Instant;

@Service
@Slf4j
@AllArgsConstructor
public class CashSenderService extends SenderService {

    private final DepositService depositService;
    private final TransactionService transactionService;

    @Override
    public HasResult canAgentProceed(PayType payType, String agentLogin, CommonPayRequest payDTO, Long amount) {
        return new CommonResult(depositService.canAgentProceedPayment(agentLogin, amount) ? HasResult.SUCCESS : HasResult.UNKNOWN_ERROR);
    }

    @Override
    public HasData<Transaction> debit(CommonPayRequest payDTO, Transaction transaction, String agentLogin) {

//        depositService.decreaseAgentDeposit(agentLogin, transaction.getSenderAmount() - transaction.getAgentFeeAmount());//TODO - check Javlon, reciAmount or senderAmount???
        depositService.decreaseAgentDeposit(agentLogin, transaction.getSenderAmount());//TODO - comment after adding trans.agentFeeAmount

        transactionService
                .sender(
                        transaction,
                        agentLogin,
                        0L,
                        transaction.getRequestTrId(),
                        transaction.getSenderAmount(),
                        0L,
                        Boolean.TRUE,
                        Constants.OK,
                        "AGENT_DEPOSIT_DECREASED",
                        agentLogin,
                        Instant.now(),
                        transaction.getRecipientCcy(),
                        agentLogin
                );

        return new CommonResultData<>(transaction);
    }

    @Override
    public ResultData<Transaction> reverse(PayType payType, Transaction transaction, String agentLogin) {

        depositService.increaseAgentDeposit(agentLogin, transaction.getSenderAmount());

        transactionService
                .sender(
                        transaction,
                        agentLogin,
                        0L,
                        transaction.getRequestTrId(),
                        transaction.getSenderAmount(),
                        0L,
                        Boolean.TRUE,
                        Constants.OK,
                        "AGENT_DEPOSIT_INCREASED_BACK",
                        agentLogin,
                        Instant.now(),
                        transaction.getRecipientCcy(),
                        transaction.getRequestGroup()
                );

        //TODO - impl
        return new ResultData<>(transaction);
    }

    @Override
    public HasData<Transaction> debitConfirm(CommonPayRequest payDTO, Transaction transaction, String agentLogin) {
        throw new CodedException(ProblemEnum.NOT_IMPLEMENTED);
    }

    @Override
    public PayType getSupportedPayType() {
        return PayType.CASH;
    }

    @Override
    public String getDescription() {
        return "Оплата наличными";
    }
}
