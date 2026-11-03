package uz.paykap.agent.tran.check;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uz.devops.bmcore.dto.GatewayQueryResult;
import uz.devops.bmcore.dto.response.SimpleBillResponse;
import uz.devops.bmcore.enumeration.RecipientType;
import uz.devops.bmcore.enumeration.TransactionStatus;
import uz.paykap.domain.Transaction;
import uz.paykap.domain.enumeration.Currency;

import java.util.Set;

@Slf4j
@Service
public class GiftGatewayCheckService extends AbstractGatewayCheckService {

    @Override
    protected void afterCheckResult() {
        if (checkRequestScopedContext.getRecipientParams().getRecipientType() == RecipientType.GIFT && checkRequestScopedContext.getTransaction().getRequestAmount() == null) {

            final SimpleBillResponse result = checkRequestScopedContext.getSimpleBillResponse();

            if (result.getAmount() == null) {
                result.setResponseText(result.getResponseText() + " : Amount can not be null of type GIFT");
                result.setSuccess(Boolean.FALSE);
                result.setStatus(TransactionStatus.CHECK_FAIL);
            } else {
                calculateAfterGotAmountFromRecipient(checkRequestScopedContext.getRecipientParams(), checkRequestScopedContext.getTransaction(), result, result.getAmount());
            }
        }
    }

    private void calculateAfterGotAmountFromRecipient(GatewayQueryResult gatewayQueryResult, Transaction transaction, SimpleBillResponse result, Long requestAmountCents) {
        Integer origRequestCcy = transaction.getRequestCcy();
        transaction.setRequestAmount(rateService.convert(result.getCurrency(), requestAmountCents, origRequestCcy).longValue());
        calculationService.calc(transaction, gatewayQueryResult);
        result.setAmount(transaction.getSenderAmount());
        result.setCurrency(origRequestCcy);

        log.debug("Calculate using recipient's recommended amount, senderAmount : {} {}, gatewayAmount : {} {}", transaction.getSenderAmount(), Currency.get(transaction.getSenderCcy()), transaction.getGatewayAmount(), Currency.get(transaction.getRecipientCcy()));
    }

    @Override
    protected Set<RecipientType> getSupportedTypes() {
        return Set.of(RecipientType.GIFT);
    }
}
