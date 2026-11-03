package uz.paykap.client.ofd;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import uz.devops.ofd.service.dto.OfdRequestWrapper;
import uz.devops.ofd.service.dto.response.OfdResponseDTO;
import uz.paykap.config.microservice.feign.AuthorizedFeignClient;

@ConditionalOnProperty(prefix = "application", name = "ofd.enabled", havingValue = "true")
@AuthorizedFeignClient(name = "ofdms", url = "${ofd-feign.ofdms-url}", fallback = OfdClientFallBack.class)
public interface OfdClient {
    @PostMapping(value = "/api/ofd/register")
    ResponseEntity<OfdResponseDTO> registerQr(@RequestBody OfdRequestWrapper ofdRequestWrapper);
}

@Component
@ConditionalOnProperty(prefix = "application", name = "ofd.enabled", havingValue = "true")
class OfdClientFallBack implements OfdClient {

    @Override
    public ResponseEntity<OfdResponseDTO> registerQr(OfdRequestWrapper ofdRequestWrapper) {
        return ResponseEntity.ok(new OfdResponseDTO());
    }
}
