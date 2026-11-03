package uz.paykap.service.abs.request;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AbsTransactionInfoRequest {
    @ApiModelProperty("ID транзакции")
    private String transId;
    @ApiModelProperty(" ID подсистемы (0-general, 1-forex, 2-swift, 3-feruz)")
    private Integer groupId;
}
