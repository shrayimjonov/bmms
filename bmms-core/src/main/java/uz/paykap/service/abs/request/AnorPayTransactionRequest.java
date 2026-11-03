package uz.paykap.service.abs.request;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@EqualsAndHashCode(callSuper = true)
@Data
@ToString(callSuper = true)
public class AnorPayTransactionRequest extends AbsTransactionRequest {

    @Size(min = 9, max = 9)
    @NotNull
    @ApiModelProperty(name = "Abs sender inn", notes = "innCl param in NCI", example = "123456789")
    private String innCl;

    @Size(min = 9, max = 9)
    @NotNull
    @ApiModelProperty(name = "Abs recipient inn", notes = "innCo param in NCI", example = "987654321")
    private String innCo;

    public AbsTransactionRequest withInnCo(String innCo) {
        this.innCo = innCo;
        return this;
    }

    public AnorPayTransactionRequest withInnCl(String innCl) {
        this.innCl = innCl;
        return this;
    }
}
