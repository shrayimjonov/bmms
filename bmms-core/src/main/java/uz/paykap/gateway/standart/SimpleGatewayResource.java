package uz.paykap.gateway.standart;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;
import uz.javlon.commons.base.Result;

import javax.validation.Valid;

@ApiIgnore
@Api(value = "Ресурс для Сохранение стандартных  шлюзов")
@RestController
@RequestMapping("/api/simple")
public class SimpleGatewayResource {


    private final Logger log = LoggerFactory.getLogger(SimpleGatewayResource.class);

    public SimpleGatewayResource(SimpleGatewayService simpleGatewayService) {
        this.simpleGatewayService = simpleGatewayService;
    }

    @ApiOperation(value = "Сохранение стандартных  шлюзов", notes = "для поставщиков")
    @PostMapping("/save")
    public ResponseEntity<Result> save(@Valid @RequestBody SimpleGatewayParam simpleGatewayParam) {
        log.debug("SimpleGatewayParams to save : {}", simpleGatewayParam);
        return ResponseEntity.ok(simpleGatewayService.saveSimpleGateway(simpleGatewayParam));
    }

    private final SimpleGatewayService simpleGatewayService;
}
