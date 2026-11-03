package uz.paykap.web.rest;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;
import uz.devops.bmcore.dto.request.CheckRequest;
import uz.devops.bmcore.dto.request.CommonPayRequest;
import uz.devops.bmcore.dto.response.CommonResponse;
import uz.paykap.gateway.GatewayServiceResolver;

import javax.validation.Valid;


@ApiIgnore
@Api(value = "Ресурс для агента")
@RestController
@RequestMapping("/api/payments")
public class GatewayPaymentResource {

    private final Logger log = LoggerFactory.getLogger(GatewayPaymentResource.class);

    private final GatewayServiceResolver serviceResolver;

    public GatewayPaymentResource(GatewayServiceResolver serviceResolver) {
        this.serviceResolver = serviceResolver;
    }

    @ApiOperation("Проверка счета перед оплатой, создаст чек")
    @RequestMapping(value = "/check", method = RequestMethod.POST)
    public ResponseEntity<CommonResponse> check(@Valid @RequestBody CheckRequest checkDTO) {
        log.debug("REST request to check : {}", checkDTO);
        return ResponseEntity.ok(serviceResolver.check(checkDTO));
    }

    @ApiOperation("Оплата, подтверждение чека после проверки")
    @RequestMapping(value = "/pay", method = RequestMethod.POST)
    public ResponseEntity<CommonResponse> pay(@Valid @RequestBody CommonPayRequest payDTO) {
        log.debug("REST request to pay : {}", payDTO);
        return ResponseEntity.ok(serviceResolver.pay(payDTO));
    }

}
