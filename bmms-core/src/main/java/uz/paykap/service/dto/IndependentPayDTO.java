package uz.paykap.service.dto;

import io.swagger.annotations.ApiModelProperty;
import uz.devops.bmcore.dto.request.CheckRequest;
import uz.devops.bmcore.enumeration.PayType;

public class IndependentPayDTO extends CheckRequest {

    @ApiModelProperty(value = "Тип платежа", notes = "Приемлем исходя из настроек поставщика в системе. default : AGENT, выставление счета агенту")
    private PayType type = PayType.AGENT;

    public PayType getType() {
        return type;
    }

    public void setType(PayType type) {
        this.type = type;
    }

    public IndependentPayDTO type(PayType type) {
        this.type = type;
        return this;
    }
}
