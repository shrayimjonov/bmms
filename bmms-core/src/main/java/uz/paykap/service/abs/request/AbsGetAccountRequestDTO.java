package uz.paykap.service.abs.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(value = "Получение список счетов.")
public class AbsGetAccountRequestDTO {

    @ApiModelProperty(value = "Номер счета")
    private String transId;

    @ApiModelProperty(value = "Наименование счета")
    private String name;

    @ApiModelProperty(value = "8 значный код клиента")
    private String clientId;

    @ApiModelProperty(value = "Балансовый счет (маска)")
    private String accBal;

    @ApiModelProperty(value = "Код валюты")
    private String currency;

    @ApiModelProperty(value = "3 значный порядковый номер")
    private String idOrder;
    private String requestId;
}
