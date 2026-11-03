package uz.paykap.service.p2p.dto.response;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import uz.javlon.commons.result.HasSuccess;
import uz.paykap.service.dto.p2p.BaseP2pRequest;
import uz.paykap.service.dto.p2p.BmResultStatus;
import uz.paykap.service.dto.result.BmResult;

import java.util.Objects;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class P2pResponse extends BaseP2pRequest {

    private boolean success = false;

    private Integer statusCode = BmResultStatus.UNKNOWN_ERROR.getCode();
    private String statusMessage = BmResultStatus.UNKNOWN_ERROR.getMessage();

    private P2pParticipantDto sender;
    private P2pParticipantDto recipient;
    private P2pRateDto rate;
    private Long bmId;

    public P2pResponse withStatusCode(BmResult result) {
        this.success = Objects.equals(result.getStatus().getCode(), HasSuccess.SUCCESS_CODE);
        this.statusMessage = result.getMessage();
        this.statusCode = result.getStatus().getCode();
        return this;
    }

    public P2pResponse withSuccess(boolean success) {
        this.success = success;
        return this;
    }

    public P2pResponse withStatusMessage(String statusMessage) {
        this.statusMessage = statusMessage;
        return this;
    }

    public P2pResponse fromBase(BaseP2pRequest baseP2pRequest) {
        setAgentTranId(baseP2pRequest.getAgentTranId());
        setAmount(baseP2pRequest.getAmount());
        setCurrency(baseP2pRequest.getCurrency());
        return this;
    }

    public P2pResponse withSender(P2pParticipantDto sender) {
        this.sender = sender;
        return this;
    }

    public P2pResponse withRecipient(P2pParticipantDto recipient) {
        this.recipient = recipient;
        return this;
    }

    public P2pResponse withAgentTranId(String agentTranId) {
        setAgentTranId(agentTranId);
        setAmount(0L);
        return this;
    }

    public P2pResponse withRate(P2pRateDto rate) {
        this.rate = rate;
        return this;
    }


    public P2pResponse withBmId(Long bmId) {
        this.bmId = bmId;
        return this;
    }
}
