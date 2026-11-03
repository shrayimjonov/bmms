package uz.paykap.service.dto.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;

import java.util.HashMap;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CommonHttpResponse {
    private HttpStatus statusCode;
    private String statusMessage;
    private HttpHeaders headers;
    private String body;
    private Map<String, Object> data = new HashMap<>(0);
}
