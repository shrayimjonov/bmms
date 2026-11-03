package uz.paykap.service.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@ApiModel("Поиск счета по реквизитам")
@NoArgsConstructor
@AllArgsConstructor
public class AccountDTO implements Serializable {

    @ApiModelProperty("Номер счёта")
    private String id;

    @ApiModelProperty("Наименование счета")
    private String name;

    @ApiModelProperty("Исходящий остаток общий (может содержать неподтвержденные обороты)")
    private String balance;
}
