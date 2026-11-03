package uz.paykap.gateway.standart;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import uz.devops.bmcore.enumeration.RequestType;

public interface SimpleBmSenderService {
    ResponseEntity<String> send(RequestType requestType, String url, String body, HttpHeaders headers, Long timeout);
}
