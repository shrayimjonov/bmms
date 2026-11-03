package uz.paykap.service.p2p.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NciTranDTO {

    @ApiModelProperty(value = "Transaction order")
    public Integer order;

    @ApiModelProperty(value = "Номер банковского счета отправителя")
    @JsonProperty("CL")
    public String accCL;

    @ApiModelProperty(value = "Номер банковского счета получателя")
    @JsonProperty("CO")
    public String accCo;

    @ApiModelProperty(value = "Сумма")
    @JsonProperty("SUM")
    public String sum;

    @ApiModelProperty(value = "Цель")
    @JsonProperty("PUR")
    public String purpose;

    @ApiModelProperty(value = "сумма Часть")
    @DecimalMin(value = "1")
    @DecimalMax(value = "100")
    @JsonProperty("amountPart")
    public Long amountPart;
}
