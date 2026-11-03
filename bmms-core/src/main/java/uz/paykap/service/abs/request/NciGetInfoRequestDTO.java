package uz.paykap.service.abs.request;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(value = "Справочники ЦБ ")
public class NciGetInfoRequestDTO {

    @ApiModelProperty(value = "Наименование справочника (OFF_DAY - статус опер дня банка 0 открыт, 1 закрыт)")
    private String transId;

    @ApiModelProperty(value = "Наименование справочника (OFF_DAY - статус опер дня банка 0 открыт, 1 закрыт)")
    private String requestId;
}
