package uz.paykap.service.dto.p2p.calc;

import lombok.Data;
import lombok.EqualsAndHashCode;
import uz.paykap.service.dto.p2p.BaseP2pRequest;
import uz.paykap.service.dto.result.HasCodeAndMessage;

import javax.validation.constraints.NotNull;

@EqualsAndHashCode(callSuper = true)
@Data
public class P2pCalcResponse extends BaseP2pRequest {

    @NotNull
    private P2pCalcParticipantDto sender;
    @NotNull
    private P2pCalcParticipantDto recipient;
    @NotNull
    private HasCodeAndMessage status;

    private boolean success = false;

    public P2pCalcResponse withResult(HasCodeAndMessage result){
        this.success = result.isSuccess();
        this.status = result;
        return this;
    }

    public P2pCalcResponse withAmount(Long amount) {
        super.withAmount(amount);
        return this;
    }

    public P2pCalcResponse withAgentTranId(String agentTranId) {
        super.withAgentTranId(agentTranId);
        return this;
    }

    public P2pCalcResponse withSuccess(boolean success) {
        this.success = success;
        return this;
    }


    public P2pCalcResponse withSender(P2pCalcParticipantDto sender) {
        this.sender = sender;
        return this;
    }

    public P2pCalcResponse withRecipient(P2pCalcParticipantDto recipient) {
        this.recipient = recipient;
        return this;
    }
}
