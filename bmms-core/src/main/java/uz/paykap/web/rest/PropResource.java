package uz.paykap.web.rest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;
import uz.paykap.domain.enumeration.Prop;

import java.util.Arrays;
import java.util.List;

/**
 * REST controller for managing Prop.
 */
@ApiIgnore
@RestController
@RequestMapping("/api")
public class PropResource {

    private final Logger log = LoggerFactory.getLogger(PropResource.class);

    /**
     * GET  /props: get all the props.
     *
     * @return the ResponseEntity with status 200 (OK) and the list of prop in body
     */
    @GetMapping("/props")
    public ResponseEntity<List<Prop>> getAllProps() {
        log.debug("REST request to get all props to array");
        return ResponseEntity.ok(Arrays.asList(Prop.values()));
    }
}
