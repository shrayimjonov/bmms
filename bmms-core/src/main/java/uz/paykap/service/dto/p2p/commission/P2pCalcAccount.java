package uz.paykap.service.dto.p2p.commission;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import uz.paykap.service.dto.p2p.P2pAccountType;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class P2pCalcAccount {

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
    @NotBlank
    private String id;

    @ApiModelProperty(
        value = "Валюта, трёхзначный цифровой ISO 4217 ",
        example = "860 - Узбекский сум, 840 - Доллар США",
        notes = "default : 860 - Узбекский сум"
    )
    @Max(999)
    @Min(100)
    private Integer currency = 860;

    public P2pCalcAccount withType(P2pAccountType type) {
        this.type = type;
        return this;
    }

    public P2pCalcAccount withCurrency(Integer currency) {
        this.currency = currency;
        return this;
    }


    public P2pCalcAccount withId(String id) {
        this.id = id;
        return this;
    }
}
