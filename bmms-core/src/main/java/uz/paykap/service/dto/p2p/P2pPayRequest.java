package uz.paykap.service.dto.p2p;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import uz.paykap.service.dto.p2p.promo.Promo;

@Data
@EqualsAndHashCode(callSuper = true)
@ApiModel(description = "Объект запроса на P2P перевод")
public class P2pPayRequest extends BaseP2pRequest {

    @ApiModelProperty("Данные карты отправителя")
    private P2pAccount from;

    @ApiModelProperty("Данные карты получателя")
    private P2pAccount to;

    public P2pPayRequest withFrom(P2pAccount from) {
        this.from = from;
        return this;
    }

    public P2pPayRequest withTo(P2pAccount to) {
        this.to = to;
        return this;
    }

    public P2pPayRequest withAmount(Long amount) {
        super.withAmount(amount);
        return this;
    }

    public P2pPayRequest withAgentTranId(String agentTranId) {
        super.withAgentTranId(agentTranId);
        return this;
    }

    public P2pPayRequest withCurrency(Integer currency) {
        super.withCurrency(currency);
        return this;
    }

    public P2pPayRequest withPromo(Promo promo) {
        super.withPromo(promo);
        return this;
    }
}
