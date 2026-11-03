package uz.paykap.service.abs.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;
import uz.paykap.service.p2p.dto.NciTranDTO;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@ToString(callSuper = true)
@Getter
@Setter
@ApiModel(value = "Запрос на Создание пакетных-транзакции № 6")
public class AbsMultiTransactionDTO {

    @ApiModelProperty(value = "ID пакета")
    private Long transId;

    @ApiModelProperty(value = "из таблицы state_general")
    private String stateId;

    @ApiModelProperty(value = "Пакет транзакций JSON")
    private List<NciTranDTO> data;
}
