package uz.paykap.service.dto.p2p;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class P2pCancelRequest {

    @ApiModelProperty("Отмена p2p")
    @NotNull
    private String agentTranId;

    public P2pCancelRequest withAgentTranId(String agentTranId) {
        this.agentTranId = agentTranId;
        return this;
    }
}
