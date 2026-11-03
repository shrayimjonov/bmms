package uz.paykap.sender.impl;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uz.devops.bmcore.dto.request.CommonPayRequest;
import uz.devops.bmcore.enumeration.PayType;
import uz.javlon.commons.base.Result;
import uz.javlon.commons.base.ResultData;
import uz.javlon.commons.exception.CodedException;
import uz.javlon.commons.exception.ProblemEnum;
import uz.javlon.commons.result.CommonResult;
import uz.javlon.commons.result.CommonResultData;
import uz.javlon.commons.result.HasData;
import uz.javlon.commons.result.HasResult;
import uz.paykap.config.Constants;
import uz.paykap.domain.Transaction;
import uz.paykap.repository.TransactionRepository;
import uz.paykap.sender.SenderService;
import uz.paykap.sender.dto.SenderOtpParams;
import uz.paykap.service.DepositService;
import uz.paykap.service.TransactionService;
import uz.paykap.service.dto.p2p.BmResultStatus;

import java.time.Instant;

@Service
@Slf4j
@AllArgsConstructor
public class AgentDepositSenderService extends SenderService {

    private final DepositService depositService;
    private final TransactionService transactionService;
    private final TransactionRepository transactionRepository;

    @Override
    public HasResult canAgentProceed(PayType payType, String agentLogin, CommonPayRequest payDTO, Long amount) {
        return new CommonResult(depositService.canAgentProceedPayment(agentLogin, amount) ? HasResult.SUCCESS : HasResult.UNKNOWN_ERROR);
    }

    @Override
    public HasData<Transaction> debit(CommonPayRequest payDTO, Transaction transaction, String agentLogin) {

        // TODO - add bill on debit

        log.debug("Debiting : {}, {}", agentLogin, payDTO);

        Result result = depositService.decreaseAgentDeposit(
                agentLogin,
                transaction.getSenderAmount() - transaction.getAgentFeeAmount(),
                transaction.getRequestCcy()
        );

        if (result.isSuccess()) {
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

        // Not success

        transactionService
                .sender(
                        transaction,
                        agentLogin,
                        0L,
                        transaction.getRequestTrId(),
                        transaction.getSenderAmount(),
                        0L,
                        Boolean.FALSE,
                        Constants.DEPOSIT_UPDATE_ERROR,
                        Constants.DEPOSIT_UPDATE_ERROR,
                        agentLogin,
                        Instant.now(),
                        transaction.getRecipientCcy(),
                        agentLogin
                );
        transactionRepository.save(transaction);

        return new CommonResultData<>(BmResultStatus.SENDER_PC_ERROR, transaction, result.getException() != null ? result.getException().getLocalizedMessage() : "unknown error");
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

        return new ResultData<>(transaction);
    }

    @Override
    public HasData<Transaction> debitConfirm(CommonPayRequest payDTO, Transaction transaction, String agentLogin) {
        throw new CodedException(ProblemEnum.NOT_IMPLEMENTED);
    }

    @Override
    public PayType getSupportedPayType() {
        return PayType.AGENT;
    }

    @Override
    public HasData<SenderOtpParams> getOtpParams(String pan, String expiry) {
        return new CommonResultData<>(BmResultStatus.NOT_IMPLEMENTED, null);
    }

    @Override
    public String getDescription() {
        return "Снятие средств с депозита агента";
    }

}
