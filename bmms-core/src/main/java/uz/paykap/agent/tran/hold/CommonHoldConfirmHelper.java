package uz.paykap.agent.tran.hold;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uz.devops.bmcore.dto.GatewayQueryResult;
import uz.devops.bmcore.dto.request.CommonPayRequest;
import uz.devops.bmcore.dto.request.SharedPayDTO;
import uz.devops.bmcore.dto.response.SimpleBillResponse;
import uz.devops.bmcore.enumeration.PayType;
import uz.devops.bmcore.enumeration.TransactionStatus;
import uz.devops.bmcore.gateway.GatewayBillingService;
import uz.javlon.commons.exception.CodedException;
import uz.javlon.commons.exception.ProblemEnum;
import uz.paykap.agent.tran.hold.util.HoldServiceUtil;
import uz.paykap.domain.Transaction;
import uz.paykap.gateway.GatewayServiceV3;
import uz.paykap.repository.TransactionRepository;
import uz.paykap.security.SecurityUtils;
import uz.paykap.sender.SenderResolverV3;
import uz.paykap.service.PushEventService;
import uz.paykap.service.cached.GatewaySelectorService;

import java.time.Instant;
import java.util.Objects;

@Slf4j
@Service
@AllArgsConstructor
public class CommonHoldConfirmHelper {
    private final TransactionRepository transactionRepository;
    private final GatewayServiceV3 gatewayServiceV3;
    private final SenderResolverV3 senderResolver;
    private final GatewaySelectorService gatewaySelectorService;
    private final HoldServiceUtil holdServiceUtil;
    private final PushEventService pushEventService;

    public SimpleBillResponse holdConfirm(CommonPayRequest payDTO) {
        String login = SecurityUtils.getCurrentUserLogin().orElseThrow(() -> new CodedException(ProblemEnum.USERNAME_NOT_FOUND));

        return holdConfirm(payDTO, holdServiceUtil.findTransaction(payDTO, login), login);
    }

    public SimpleBillResponse holdConfirm(CommonPayRequest payDTO, Transaction transaction, String login) {

        GatewayQueryResult gatewayQueryResult = gatewaySelectorService.getValidGatewayQueryResultV5(transaction.getRecipientServiceId(), login, transaction.getRequestGroup());

        GatewayBillingService gatewayService = holdServiceUtil.getGatewayService(transaction, gatewayQueryResult);

        SharedPayDTO sharedPayDTO = holdServiceUtil.getSharedPayDTO(payDTO, login, transaction, gatewayQueryResult);

        log.debug("Sending PAY request to {} ({}) : {}", gatewayQueryResult.getRecipientShortName(), gatewayService.getClass().getSimpleName(), sharedPayDTO);

        SimpleBillResponse result = gatewayServiceV3.pay(sharedPayDTO, gatewayService);

        log.debug("Received response from {} ({}) : {}", gatewayQueryResult.getRecipientShortName(), gatewayService.getClass().getSimpleName(), result);

        TransactionStatus status = result.getStatus() != TransactionStatus.NEW ? result.getStatus() : (Objects.equals(result.isSuccess(), Boolean.TRUE) ? TransactionStatus.DEBITED : TransactionStatus.GATEWAY_FAILED);

        transaction
                .recipientDate(result.getResponseTime())
                .recipientStatus(String.valueOf(result.isSuccess()))
                .recipientName(result.getRecipientShortName())
                .recipientStatusText(result.getResponseText())
                .recipientSuccess(result.isSuccess())
                .status(status)
                .recipientBillId(result.getId())
                .recipientTrId(result.getExtId())
                .date(Instant.now());

        transactionRepository.save(transaction);

        if (transaction.getPayType() == null || transaction.getPayType() == PayType.UNKNOWN) {
            result.setStatus(TransactionStatus.HOLD_CONFIRM_ERROR);
            result.setSuccess(false);
            result.setResponseText("Transaction pay type null or unknown");

            transaction.setStatus(TransactionStatus.FAILED);
            transaction.setSenderStatus("failed");
            transaction.setSenderSuccess(false);
            transaction.setSenderStatusText("Sender pay type unknown");
            transactionRepository.save(transaction);
            return result;
        }
        if (status == TransactionStatus.DEBITED) {
            log.info("confirm debit payDTO : {}", payDTO);
            var transactionResultData = senderResolver.holdConfirm(payDTO, transaction, login);
            transaction.setStatus(transactionResultData.isSuccess() ? TransactionStatus.SUCCESS : TransactionStatus.HOLD_CONFIRM_ERROR);
            transaction.setSenderSuccess(transactionResultData.isSuccess());
            transaction.setSenderStatus(String.valueOf(transactionResultData.isSuccess()));
            transaction.setSenderDate(Instant.now());
            if (!transactionResultData.isSuccess()) {
                transaction.setSenderStatusText(transactionResultData.getMessage());
            }
            transactionRepository.save(transaction);

            result.setStatus(transaction.getStatus());
            result.setSuccess(transactionResultData.isSuccess());
            result.setResponseText(transaction.getFullStatusText());

        } else {
            result.setStatus(TransactionStatus.FAILED);
            result.setSuccess(false);
            result.setResponseText("gateway service pay failed");
            transaction.setStatus(TransactionStatus.FAILED);
            transaction.setSenderStatus("failed");
            transaction.setSenderSuccess(false);
            transaction.setSenderStatusText("gateway service pay failed");
            transactionRepository.save(transaction);
            return result;
        }
        holdServiceUtil.prepareWithAdditionalResult(transaction, result);
        log.info("###### Pay Response ######: \n{}\n", result);
        pushEventService.push(transaction, gatewayQueryResult);
        return result;
    }
}
