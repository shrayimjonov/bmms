package uz.paykap.agent.tran.check;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uz.devops.bmcore.dto.response.SimpleBillResponse;
import uz.devops.bmcore.enumeration.RecipientType;
import uz.devops.bmcore.enumeration.TransactionStatus;
import uz.paykap.domain.Transaction;

import java.util.Set;

@Slf4j
@Service
public class AmountDictatorGatewayCheckService extends AbstractGatewayCheckService {

    private final Set<RecipientType> recipientTypes = Set.of(RecipientType.AMOUNT_DICTATOR, RecipientType.INVOICE);

    @Override
    protected void afterCheckResult() {

        if ((recipientTypes.contains(checkRequestScopedContext.getRecipientParams().getRecipientType())) && checkRequestScopedContext.getGatewayResult() != null) {

            final SimpleBillResponse result = checkRequestScopedContext.getSimpleBillResponse();
            final Transaction transaction = checkRequestScopedContext.getTransaction();

            log.debug("Recipient type : {}, recalculate amounts using recommended from recipient : {}", checkRequestScopedContext.getRecipientParams().getRecipientShortName(), checkRequestScopedContext.getGatewayResult().getRecommendedSenderAmountCents());

            if (checkRequestScopedContext.getGatewayResult().getRecommendedSenderAmountCents() == null || checkRequestScopedContext.getGatewayResult().getRecommendedSenderAmountCents() <= 0L) {
                String responseText = result.getResponseText() + " : Got invalid pay_amount=" + checkRequestScopedContext.getGatewayResult().getRecommendedSenderAmountCents() + " for recipient types " + recipientTypes;

                log.warn(responseText);

                result.setResponseText(responseText);
                result.setSuccess(Boolean.FALSE);
                result.setStatus(TransactionStatus.CHECK_FAIL);
            } else {
                Long recommendedSenderAmountCents = checkRequestScopedContext.getGatewayResult().getRecommendedSenderAmountCents();

                log.debug("Recipient {} recommendedSenderAmountCents : {}", checkRequestScopedContext.getRecipientParams().getRecipientShortName(), recommendedSenderAmountCents);

//                if (transaction.getRequestAmount() == null || checkRequestScopedContext.getRecipientParams().getRecipientType() == RecipientType.INVOICE) {
                /*  */
                transaction.setRequestAmount(recommendedSenderAmountCents);
//                }
//                else {
                transaction.setSenderAmount(recommendedSenderAmountCents);
                transaction.setSenderCcy(result.getCurrency());
                transaction.setGatewayAmount(recommendedSenderAmountCents * (checkRequestScopedContext.getRecipientParams().getRecipientAmountRatio() / 100d));
                transaction.setRecipientAmount(recommendedSenderAmountCents);
                transaction.setSystemCommissionUpAmount(transaction.getSenderAmount() - transaction.getRequestAmount());
//                }

//                calculateAfterGotAmountFromRecipient(checkRequestScopedContext.getRecipientParams(), transaction, result);
            }

        }
    }

    @Override
    protected Set<RecipientType> getSupportedTypes() {
        return recipientTypes;
    }
}
