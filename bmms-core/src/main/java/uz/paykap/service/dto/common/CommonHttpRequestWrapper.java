package uz.paykap.service.dto.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.minidev.json.annotate.JsonIgnore;
import uz.devops.bmcore.dto.GatewayResult;
import uz.devops.bmcore.enumeration.RequestType;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CommonHttpRequestWrapper {
    private CommonHttpRequest request;
    private CommonHttpResponse response;
    @JsonIgnore
    private GatewayResult gatewayResult;
}
