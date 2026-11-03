package uz.paykap.service.dto.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpHeaders;
import uz.devops.bmcore.enumeration.RequestType;

import java.util.HashMap;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CommonHttpRequest {
    private RequestType requestType;
    private String url;
    private HttpHeaders headers;
    private String body;
    private Map<String, Object> params = new HashMap<>(0);
}
