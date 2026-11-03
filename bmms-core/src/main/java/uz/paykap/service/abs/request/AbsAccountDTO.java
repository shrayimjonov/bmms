package uz.paykap.service.abs.request;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AbsAccountDTO {
    @ApiModelProperty("Номер счета")
    private String transId;
    @ApiModelProperty("Наименование счета")
    private String name;
    @ApiModelProperty("8 значный код клиента")
    private String clientId;
    @ApiModelProperty("Балансовый счет (маска)")
    private String accBal;
    @ApiModelProperty("Код валюты")
    private String currency;
    @ApiModelProperty("3 значный порядковый номер")
    private String idOrder;
    private String requestId;
}
