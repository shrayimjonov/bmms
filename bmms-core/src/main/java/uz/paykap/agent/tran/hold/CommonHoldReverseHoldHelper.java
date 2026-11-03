package uz.paykap.agent.tran.hold;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uz.devops.bmcore.dto.request.SharedCancelDTO;
import uz.devops.bmcore.dto.response.SimpleBillResponse;
import uz.devops.bmcore.enumeration.RequestType;
import uz.devops.bmcore.enumeration.TransactionStatus;
import uz.javlon.commons.base.SimpleResult;
import uz.javlon.commons.exception.CodedException;
import uz.javlon.commons.exception.ProblemEnum;
import uz.javlon.commons.result.HasData;
import uz.paykap.config.Constants;
import uz.paykap.domain.*;
import uz.paykap.repository.*;
import uz.paykap.security.SecurityUtils;
import uz.paykap.sender.SenderResolverV3;
import uz.paykap.service.dto.p2p.BmResultStatus;
import uz.paykap.service.dto.result.BmResultData;
import uz.paykap.service.hold.PartialReverseRequest;
import uz.paykap.service.hold.PartialReverseResponse;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@Component
@AllArgsConstructor
public class CommonHoldReverseHoldHelper {

    private final TransactionRepository transactionRepository;
    private final SenderResolverV3 senderResolver;
    private final RecipientRepository recipientRepository;
    private final AgentRecipientRepository agentRecipientRepository;
    private final AgentRepository agentRepository;
    private final BillRepository billRepository;

    public PartialReverseResponse partialReverseHold(PartialReverseRequest request) {
        Optional<Transaction> holdTransaction = transactionRepository.findById(request.getBillId());
        log.debug("find transaction: {}", holdTransaction);
        if (holdTransaction.isEmpty()) {
            log.error("TRANSACTION NOT FOUND ERROR");
            return new PartialReverseResponse(errorResult(BmResultStatus.BILL_NOT_FOUND), false);
        }
        Transaction transaction = holdTransaction.get();
        String username = SecurityUtils.getCurrentUserLogin().orElseThrow(() -> new CodedException(ProblemEnum.USERNAME_NOT_FOUND));

        log.debug("verify transaction...");
        SimpleBillResponse verifyError = verifyTransaction(transaction, username, request);
        if (verifyError != null) return new PartialReverseResponse(verifyError, false);


        SimpleBillResponse cancelError = doCancelFirstTransaction(transaction);
        if (cancelError != null) return new PartialReverseResponse(cancelError, false);
        log.info("old hold transaction cancelled success!");

        Long recipientId = getRecipientId(transaction.getRecipientName(), username);
        log.debug("recipientId : {}", recipientId);

        Map<String, Object> params = getParams(transaction.getId(), username);
        Long amount = transaction.getRequestAmount() - request.getReverseAmount();

        return new PartialReverseResponse(null, true, username, transaction.getRecipientAccount(), recipientId, amount, params);
    }

    private Map<String, Object> getParams(Long transactionId, String login) {
        Bill checkBill = billRepository.findTop1ByTransactionIdAndTypeAndLoginAndSuccessTrue(transactionId, RequestType.PAY, login).orElseThrow(() -> new CodedException(ProblemEnum.BILL_NOT_FOUND, "check bill not found"));
        if (checkBill.getRequestJson() == null || checkBill.getRequestJson().get(Constants.PAN_PARAM) == null || checkBill.getRequestJson().get(Constants.EXPIRY_PARAM) == null) {
            throw new CodedException(ProblemEnum.NOT_FOUND, "card pan or expiry not found");
        }
        Map<String, Object> senderParams = new HashMap<>();
        senderParams.put(Constants.PAN_PARAM, checkBill.getRequestJson().get(Constants.PAN_PARAM));
        senderParams.put(Constants.EXPIRY_PARAM, checkBill.getRequestJson().get(Constants.EXPIRY_PARAM));
        return senderParams;
    }

    private SimpleBillResponse doCancelFirstTransaction(Transaction transaction) {
        SharedCancelDTO sharedCancelDTO = new SharedCancelDTO();
        sharedCancelDTO.setBillId(transaction.getId());
        log.info("reverseHold with sharedCancelDTO: {}", sharedCancelDTO);
        SimpleResult reverseResult = reverseHold(sharedCancelDTO);
        if (!reverseResult.isSuccess()) {
            log.error("PARTIAL REVERSE ERROR");
            return errorResult(BmResultStatus.PARTIAL_REVERSE_ERROR);
        }
        return null;
    }

    private SimpleBillResponse verifyTransaction(Transaction transaction, String username, PartialReverseRequest request) {
        if (!Objects.equals(transaction.getStatus(), TransactionStatus.HOLD_SUCCESS)) {
            log.error("error: transaction status is not HOLD_SUCCESS...");
            return errorResult(BmResultStatus.BILL_NOT_FOUND);
        }

        if (!Objects.equals(transaction.getRequestLogin(), username)) {
            log.error("error: TRANSACTION WITH THIS USER NOT FOUND...");
            return errorResult(BmResultStatus.TRANSACTION_WITH_THIS_USER_NOT_FOUND);
        }

        if (transaction.getRequestAmount() < request.getReverseAmount()) {
            log.error("error: INCORRECT AMOUNT...");
            return errorResult(BmResultStatus.INCORRECT_AMOUNT);
        }

        if (Objects.equals(transaction.getRequestAmount(), request.getReverseAmount())) {
            log.error("error: USE REVERSE...");
            return errorResult(BmResultStatus.USE_REVERSE);
        }
        return null;
    }

    private Long getRecipientId(String recipientName, String username) {
        Optional<Recipient> recipient = recipientRepository.findTop1ByShortName(recipientName);
        if (recipient.isEmpty()) {
            throw new CodedException(ProblemEnum.SERVICE_NOT_FOUND);
        }

        if (!Objects.equals(recipient.get().getActive(), Boolean.TRUE)) {
            throw new CodedException(ProblemEnum.SERVICE_NOT_ACTIVE);
        }

        Agent agent = agentRepository.findTop1ByShortName(username).orElseThrow(() -> new CodedException(ProblemEnum.USERNAME_NOT_FOUND, "agent not found"));

        AgentRecipient agentRecipient = agentRecipientRepository.findTop1ByAgent_IdAndRecipient_Id(agent.getId(), recipient.get().getId()).orElseThrow(() -> new CodedException(ProblemEnum.SERVICE_NOT_FOUND));
        if (!Objects.equals(agentRecipient.getActive(), Boolean.TRUE)) {
            throw new CodedException(ProblemEnum.SERVICE_NOT_FOUND);
        }
        return recipient.get().getId();
    }

    private SimpleBillResponse errorResult(BmResultStatus billNotFound) {
        return null;
    }

    public SimpleResult reverseHold(SharedCancelDTO cancelDTO) {
        String login = getCurrentUserName();
        log.info("\n\n###### Reverse Hold Request from {} ######: \n{}\n", login, cancelDTO);

        Transaction transaction = findHoldSuccessTransaction(cancelDTO.getBillId(), login);
        if (transaction == null) {
            log.warn("REVERSE HOLD FAILED");
            return SimpleResult.FAILED;
        }

        HasData<Bill> holdResultData = senderResolver.reverseHold(transaction, login);
        if (!holdResultData.isSuccess() || holdResultData.getData() == null || !holdResultData.getData().isSuccess()) {
            log.warn("REVERSE HOLD FAILED");
            return SimpleResult.FAILED;
        }

        return updateTransactionToReversed(transaction, holdResultData);
    }

    private SimpleResult updateTransactionToReversed(Transaction transaction, HasData<Bill> holdResultData) {
        transaction.setSenderStatus(holdResultData.getData().getResponseCode());
        transaction.setSenderStatusText(holdResultData.getData().getResponseText());
        transaction.setStatus(TransactionStatus.HOLD_CANCELLED);
        transaction.setSenderSuccess(false);
        log.debug("OK - Hold Reversed");
        transactionRepository.save(transaction);
        log.debug("transaction updated: {}", transaction);
        return SimpleResult.SUCCESS;
    }

    private String getCurrentUserName() {
        return SecurityUtils.getCurrentUserLogin().orElseThrow(() -> new CodedException(ProblemEnum.USERNAME_NOT_FOUND));
    }

    private Transaction findHoldSuccessTransaction(Long transactionId, String login) {
        Transaction transaction;

        if (!Objects.equals(login, Constants.ADMIN_USERNAME)) {

            log.debug("AGENT wants to pay");
            // AGENT
            transaction = transactionRepository.findTop1ByRequestLoginAndIdAndStatus(login, transactionId, TransactionStatus.HOLD_SUCCESS).orElseThrow(() -> new CodedException(ProblemEnum.BILL_NOT_FOUND));
        } else {

            log.debug("ADMIN wants to pay");
            // ADMIN
            transaction = transactionRepository.findTop1ByIdAndStatus(transactionId, TransactionStatus.HOLD_SUCCESS).orElseThrow(() -> new CodedException(ProblemEnum.BILL_NOT_FOUND));
        }
        log.debug("transaction find: {}", transaction);
        return transaction;
    }
}
