package uz.paykap.service.dto.p2p.commission;

import lombok.Data;
import lombok.EqualsAndHashCode;
import uz.paykap.service.dto.p2p.BaseP2pRequest;

import javax.validation.constraints.NotNull;

@EqualsAndHashCode(callSuper = true)
@Data
public class P2pCalcResponse extends BaseP2pRequest {

    @NotNull
    private Long senderCommissionAmount;
    @NotNull
    private Integer senderCcy;
    @NotNull
    private Long recipientCommissionAmount;
    @NotNull
    private Integer recipientCcy;
    private boolean success = false;

    public P2pCalcResponse withAmount(Long amount) {
        super.withAmount(amount);
        return this;
    }

    public P2pCalcResponse withAgentTranId(String agentTranId) {
        super.withAgentTranId(agentTranId);
        return this;
    }

    public P2pCalcResponse withSenderCommissionAmount(Long senderCommissionAmount) {
        this.senderCommissionAmount = senderCommissionAmount;
        return this;
    }

    public P2pCalcResponse withRecipientCommissionAmount(Long recipientCommissionAmount) {
        this.recipientCommissionAmount = recipientCommissionAmount;
        return this;
    }

    public P2pCalcResponse withSenderCcy(Integer senderCcy) {
        this.senderCcy = senderCcy;
        return this;
    }

    public P2pCalcResponse withRecipientCcy(Integer recipientCcy) {
        this.recipientCcy = recipientCcy;
        return this;
    }

    public P2pCalcResponse withSuccess(boolean success) {
        this.success = success;
        return this;
    }
}
