package uz.paykap.gateway.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class GatewaySuccessErrorTran {
    private String gatewayName;
    private Long total;
    private Long errorCount;
    private Long prc;
}
