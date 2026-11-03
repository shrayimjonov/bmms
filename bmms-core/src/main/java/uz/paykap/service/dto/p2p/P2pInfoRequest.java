package uz.paykap.service.dto.p2p;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotNull;

@EqualsAndHashCode(callSuper = true)
@Data
public class P2pInfoRequest extends BaseP2pRequest {

    @ApiModelProperty("Данные карты получателя")
    @NotNull
    private P2pAccount to;

    public P2pInfoRequest withTo(P2pAccount to) {
        this.to = to;
        return this;
    }

    public P2pInfoRequest withAmount(Long amount) {
        super.withAmount(amount);
        return this;
    }

    public P2pInfoRequest withAgentTranId(String agentTranId) {
        super.withAgentTranId(agentTranId);
        return this;
    }
}
