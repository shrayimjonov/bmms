package uz.paykap.service.dto.p2p.promo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class Promo {

    @ApiModelProperty(
            value = "Промокод",
            notes = "Короткий код латинскими буквами и цыфрами",
            example = "5mln"
    )
    @NotNull
    private String code;

    public Promo withCode(String code) {
        this.code = code;
        return this;
    }
}
