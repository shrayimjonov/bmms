package uz.paykap.service.abs.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@ToString(callSuper = true)
@Getter
@Setter
@ApiModel(value = "Поиск юридического клиента по реквизитам")
public class AbsGetLegalEntityRequestDTO {

    @ApiModelProperty(value = "ID клиента")
    private String transId;

    @ApiModelProperty(value = "Наименование")
    private String name;

    @ApiModelProperty(value = "ИНН")
    private String numberTaxRegistration;

    @ApiModelProperty(value = "тел номер")
    private String phone;

    @ApiModelProperty(value = "Факс")
    private String fax;

    @ApiModelProperty(value = "Емайл")
    private String email;

    @ApiModelProperty(value = "Адрес")
    private String postAddress;

    @ApiModelProperty(value = "PINFL")
    private String pinfl;
}
