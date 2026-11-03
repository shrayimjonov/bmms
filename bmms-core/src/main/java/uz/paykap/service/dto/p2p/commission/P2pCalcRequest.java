package uz.paykap.service.dto.p2p.commission;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import uz.paykap.service.dto.p2p.BaseP2pRequest;
import uz.paykap.service.dto.p2p.P2pAccountType;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@EqualsAndHashCode(callSuper = true)
@Data
public class P2pCalcRequest extends BaseP2pRequest {

    @NotNull
    private P2pCalcAccount from;

    @NotNull
    private P2pCalcAccount to;

    public P2pCalcRequest withAmount(Long amount) {
        super.withAmount(amount);
        return this;
    }

    public P2pCalcRequest withAgentTranId(String agentTranId) {
        super.withAgentTranId(agentTranId);
        return this;
    }


    public P2pCalcRequest withFrom(P2pCalcAccount from) {
        this.from = from;
        return this;
    }

    public P2pCalcRequest withTo(P2pCalcAccount to) {
        this.to = to;
        return this;
    }
}
