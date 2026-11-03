package uz.paykap.adapter.verification.dto;

import com.google.common.base.Objects;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public class SimpleGatewayTran {

    public SimpleGatewayTran() {
    }

    public SimpleGatewayTran(Long id, @NotNull Long amount) {
        this.id = id;
        this.amount = amount;
    }

    public SimpleGatewayTran(Long id, @NotNull Long amount, @Size(max = 32) String extId) {
        this.id = id;
        this.amount = amount;
        this.extId = extId;
    }

    @ApiModelProperty(value = "Номер платежа в системе", notes = "Используется для последуюших операций")
    private Long id;

    @ApiModelProperty(value = "Сумма платежа", notes = "см. Соотношение суммы")
    @NotNull
    private Long amount;

    @ApiModelProperty(value = "Внешний ид. трансакции для системы, внутренний ид. для получателя", example = "abc123")
    @Size(max = 32)
    private String extId;

    @Override
    public String toString() {
        return "SimpleGatewayTran{" +
            "id=" + id +
            ", amount=" + amount +
            ", extId='" + extId + '\'' +
            '}';
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getAmount() {
        return amount;
    }

    public void setAmount(Long amount) {
        this.amount = amount;
    }

    public String getExtId() {
        return extId;
    }

    public void setExtId(String extId) {
        this.extId = extId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SimpleGatewayTran that = (SimpleGatewayTran) o;

        if (that.getId() != null && this.getId() != null) {
            return Objects.equal(id, that.id);
        }

        //TODO
        return false;
    }
}
