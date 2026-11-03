//package uz.paykap.web.rest;
//
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RequestBody;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RestController;
//import uz.javlon.commons.exception.CodedException;
//import uz.javlon.commons.exception.ProblemEnum;
//import uz.paykap.client.ofd.OfdClient;
//import uz.paykap.event.dto.ofd.OfdRequestWrapperDTO;
//import uz.paykap.service.dto.ofd.OfdResponse;
//
//import javax.validation.Valid;
//
//@Slf4j
//@RestController
//@RequiredArgsConstructor
//@RequestMapping("/api")
//@ConditionalOnProperty(prefix = "application", name = "ofd.enabled", havingValue = "true")
//public class OfdProcessResource {
//
//    private final OfdClient ofdClient;
//
//    @PostMapping("/register-qr")
//    public ResponseEntity<OfdResponse> registerQr(@RequestBody @Valid OfdRequestWrapperDTO requestWrapper) {
//        log.debug("Request to register qr with request: {}", requestWrapper);
//        var ofdResponse = ofdClient.registerQr(requestWrapper);
//
//        if (ofdResponse.getBody() == null) {
//            log.warn("Ofd response is null with request: {}", requestWrapper);
//            throw new CodedException(ProblemEnum.INVALID_REQUEST);
//        }
//
//        log.debug("Ofd Response :{}", ofdResponse);
//        return ofdResponse;
//    }
//}
