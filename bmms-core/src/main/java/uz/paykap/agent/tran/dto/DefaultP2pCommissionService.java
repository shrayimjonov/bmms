package uz.paykap.agent.tran.dto;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uz.paykap.service.p2p.dto.response.P2pParticipantDto;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Slf4j
@Service
public class DefaultP2pCommissionService implements P2pCommissionService {

    @Override
    public P2pParticipantDto calcSenderCommission(P2pParticipantDto sender, BigDecimal senderCommissionPercent, BigDecimal senderAmountInSenderCcyInCents) {

        if (sender == null) {
            sender = new P2pParticipantDto();
        }

        if (senderCommissionPercent == null) {
            senderCommissionPercent = BigDecimal.ZERO;
        }

        BigDecimal senderCommissionAmountInCents = senderAmountInSenderCcyInCents.multiply(senderCommissionPercent).divide(BigDecimal.valueOf(100), 0, RoundingMode.DOWN);
        sender.setCommission(senderCommissionPercent);
        sender.setAmount(senderAmountInSenderCcyInCents.add(senderCommissionAmountInCents).longValue());
        sender.setCommissionAmount(senderCommissionAmountInCents.longValue());
        log.info("Commission decorator set sender commission to {}", senderCommissionPercent.longValue());

        return sender;
    }

    @Override
    public P2pParticipantDto calcRecipientCommission(P2pParticipantDto recipient, BigDecimal recipientCommissionPercent, BigDecimal recipientAmountInRecipientCcyInCents) {
        if (recipient == null) {
            recipient = new P2pParticipantDto();
        }

        if (recipientCommissionPercent == null) {
            recipientCommissionPercent = BigDecimal.ZERO;
        }

        BigDecimal recipientCommissionAmountInCents = recipientAmountInRecipientCcyInCents.multiply(recipientCommissionPercent).divide(BigDecimal.valueOf(100), 0, RoundingMode.DOWN);
        recipient.setCommission(recipientCommissionPercent);
        recipient.setAmount(recipientAmountInRecipientCcyInCents.add(recipientCommissionAmountInCents.negate()).longValue());
        recipient.setCommissionAmount(recipientCommissionAmountInCents.longValue());
        log.info("Commission decorator set recipient commission to {}", recipientCommissionPercent.longValue());

        return recipient;
    }
}
