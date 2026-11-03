package uz.paykap.agent.tran.dto;

import uz.paykap.service.p2p.dto.response.P2pParticipantDto;

import java.math.BigDecimal;

public interface P2pCommissionService {
    P2pParticipantDto calcSenderCommission(P2pParticipantDto sender, BigDecimal commission, BigDecimal senderAmountInRecipientCcyInCents);

    P2pParticipantDto calcRecipientCommission(P2pParticipantDto recipient, BigDecimal commission, BigDecimal recipientAmountInRecipientCcyInCents);
}
