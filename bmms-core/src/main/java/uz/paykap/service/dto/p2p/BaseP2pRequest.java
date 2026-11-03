package uz.paykap.service.dto.p2p;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import uz.paykap.config.Constants;
import uz.paykap.service.dto.p2p.calc.P2pCalcRequest;
import uz.paykap.service.dto.p2p.promo.Promo;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@Data
public class BaseP2pRequest {

    @ApiModelProperty(notes = "Сумма платежа в тиинах", example = "100000", allowableValues = "range(0, infinity)")
    private Long amount;

    @ApiModelProperty(
            value = "Валюта, трёхзначный цифровой ISO 4217 ",
            example = "860 - Узбекский сум, 840 - Доллар США",
            notes = "default : 860 - Узбекский сум"
    )
    @Max(999)
    @Min(100)
    private Integer currency = Constants.DEFAULT_CURRENCY_CODE;

    @ApiModelProperty(notes = "Ид. транзакции агента", example = "6bad52f4-759c-11eb-9439-0242ac130002", required = true)
    @NotNull
    private String agentTranId;

    @ApiModelProperty(notes = "Авторизован ли запрос. default : false", example = "false")
    @NotNull
    private Boolean authorized = Boolean.FALSE;

    @ApiModelProperty("Промо")
    private Promo promo;

    public BaseP2pRequest withPromo(Promo promo) {
        this.promo = promo;
        return this;
    }

    public BaseP2pRequest withAmount(Long amount) {
        this.amount = amount;
        return this;
    }

    public BaseP2pRequest withAgentTranId(String agentTranId) {
        this.agentTranId = agentTranId;
        return this;
    }

    public BaseP2pRequest from(BaseP2pRequest baseP2pRequest) {
        this.agentTranId = baseP2pRequest.getAgentTranId();
        this.amount = baseP2pRequest.getAmount();
        return this;
    }

    public BaseP2pRequest withAuthorized(Boolean authorized) {
        this.authorized = authorized;
        return this;
    }

    public BaseP2pRequest withCurrency(Integer currency) {
        this.currency = currency;
        return this;
    }
}
