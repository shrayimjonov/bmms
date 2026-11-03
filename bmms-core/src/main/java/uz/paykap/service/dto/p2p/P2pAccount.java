package uz.paykap.service.dto.p2p;

import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import uz.paykap.config.Constants;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@Data
public class P2pAccount {

    @ApiModelProperty(
        value = "Тип ид. получателя",
        notes = "PAN, BANK_ACCOUNT",
        example = "PAN"
    )
    @NotNull
    private P2pAccountType type = P2pAccountType.PAN;

    @ApiModelProperty(
        value = "Ид. получателя",
        example = "9860110000000000"
    )
    @NotNull
    private JsonNode params;

    @ApiModelProperty(
        value = "Валюта, трёхзначный цифровой ISO 4217 ",
        example = "860 - Узбекский сум, 840 - Доллар США",
        notes = "default : 860 - Узбекский сум"
    )
    @Max(999)
    @Min(100)
    private Integer currency = Constants.DEFAULT_CURRENCY_CODE;

    private Boolean commissionFree;

    private Long commissionFreeAmount;

    public P2pAccount withType(P2pAccountType type) {
        this.type = type;
        return this;
    }

    public P2pAccount withCurrency(Integer currency) {
        this.currency = currency;
        return this;
    }

    public P2pAccount withParams(JsonNode params) {
        this.params = params;
        return this;
    }
}
