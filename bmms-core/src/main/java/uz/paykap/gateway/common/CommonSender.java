package uz.paykap.gateway.common;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import uz.devops.bmcore.enumeration.RequestType;
import uz.paykap.domain.enumeration.MethodType;

import java.util.Map;

public interface CommonSender {

    ResponseEntity<String> sendAndReceive(RequestType requestType, String recipientShortName, MethodType httpMethod, String url, HttpHeaders headers, Map<String, Object> params);

    ResponseEntity<String> sendAndReceive(RequestType requestType, String recipientShortName, MethodType HttpMethod, String url, String body, HttpHeaders headers, Map<String, Object> params);

    @Deprecated
    ResponseEntity<String> sendAndReceive(RequestType requestType, String recipientShortName, String url, MultiValueMap<String, String> body, HttpHeaders headers, Map<String, Object> params);

    @Deprecated
    ResponseEntity<String> sendAndReceive(RequestType requestType, String recipientShortName, String url, HttpHeaders headers, MultiValueMap<String, Object> map, Map<String, Object> params);

}
