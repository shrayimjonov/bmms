package uz.paykap.web.rest;

import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for managing Bill.
 */
@RestController
@RequestMapping("/api")
public class EchoResource {

    private final Logger log = LoggerFactory.getLogger(EchoResource.class);

    @ApiOperation(value = "Echo запрос", notes = "Возвращает текст 'OK' если доступен")
    @GetMapping("/echo")
    public String getEcho() {
        log.debug("REST request to get echo");
        return "OK";
    }
}
