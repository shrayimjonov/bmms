package uz.paykap.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import uz.devops.bmcore.dto.GatewayQueryResult;
import uz.devops.bmcore.enumeration.TransactionStatus;
import uz.devops.pusher.dto.PusherPaymentData;
import uz.devops.pusher.event.PusherPaymentEvent;
import uz.javlon.commons.util.CardUtil;
import uz.paykap.config.Constants;
import uz.paykap.domain.Transaction;
import uz.paykap.utils.NumberUtil;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@AllArgsConstructor
public class PushEventService {
    private final ApplicationEventPublisher eventPublisher;

    public void push(Transaction transaction, GatewayQueryResult gatewayInfo) {

        if (transaction.getStatus() != TransactionStatus.SUCCESS) {
            log.debug("Push rejected, bcz transaction status : {}", transaction.getStatus());
            return;
        }

        List<String> tags = new ArrayList<>(20);

        tags.add(Constants.TAG_BM_PAYMENT);
        tags.add(gatewayInfo.getRecipientShortName());
        tags.add(gatewayInfo.getGatewayName());
        tags.add(gatewayInfo.getGatewayBean());
        tags.add(transaction.getEposTerminal());
        tags.add(transaction.getEposMerchant());
        tags.add("SUCCESS=" + (transaction.getStatus() == TransactionStatus.SUCCESS));

        log.debug("Tags resolved for pusher : {}", tags);

        PusherPaymentData paymentData = new PusherPaymentData(
                String.valueOf(transaction.getId()),
                transaction.getRecipientName(),
                transaction.getEposMerchant(),
                transaction.getEposTerminal(),
                CardUtil.maskIfBankCard(transaction.getSenderAccount()),
                transaction.getRecipientAccount(),
                transaction.getGatewayAmount() != null ? NumberUtil.format(transaction.getGatewayAmount() / transaction.getAmountRatio()) : "---",
                transaction.getSystemFeeAmount() != null ? NumberUtil.format(transaction.getSystemFeeAmount().doubleValue()) : "---",
                transaction.getDate()
        );

        eventPublisher.publishEvent(new PusherPaymentEvent(this, paymentData, tags));
    }
}
