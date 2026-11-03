package uz.devops.bm.web.rest;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.gateway.support.NotFoundException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import uz.devops.bm.config.ApplicationProperties;

@Slf4j
@RestController
@RequestMapping("/api")
public class ContentForwardResource {

    private final RestTemplate restTemplate;
    private final ApplicationProperties applicationProperties;

    public ContentForwardResource(@Qualifier("forwardRest") RestTemplate restTemplate, ApplicationProperties applicationProperties) {
        this.restTemplate = restTemplate;
        this.applicationProperties = applicationProperties;
    }

    @GetMapping("/images/logo/{uri}")
    public ResponseEntity<?> getLogo(@PathVariable("uri") String logoUri) {
        log.debug("Request: {}", logoUri);
        var uri = applicationProperties.getContent().getUri() + "/" + logoUri;
        try {
            var image = restTemplate.getForObject(uri, byte[].class);
            return ResponseEntity.ok(image);
        } catch (Exception e) {
            log.warn("Can not get logo: {}, e:{}", logoUri, e.getMessage());
            throw new NotFoundException(e.getMessage());
        }
    }
    //    @RequestMapping("/images/logo/**")
    //    public ResponseEntity<?> mirrorRest(@RequestBody(required = false) String body,
    //                                        HttpMethod method, HttpServerRequest request, HttpServletResponse response)
    //        throws URISyntaxException {
    //        var requestUrl = request.uri();
    //
    //        var uri = new URI(
    //            applicationProperties.getContent().getProtocol(),
    //            null,
    //            applicationProperties.getContent().getUri(),
    //            applicationProperties.getContent().getPort(),
    //            applicationProperties.getContent().getPath(),
    //            null,
    //            null);
    //        uri = UriComponentsBuilder.fromUri(uri)
    //            .path(requestUrl)
    //            .build(true).toUri();
    //
    //        var headers = new HttpHeaders();
    //        var headerNames = request.requestHeaders();
    //        headerNames.forEach(it -> headers.set(it.getKey(), it.getValue()));
    //
    //
    //        HttpEntity<String> httpEntity = new HttpEntity<>(body, headers);
    //        RestTemplate restTemplate = new RestTemplate();
    //        try {
    //            return restTemplate.exchange(uri, method, httpEntity, String.class);
    //        } catch (HttpStatusCodeException e) {
    //            return ResponseEntity.status(e.getRawStatusCode())
    //                .headers(e.getResponseHeaders())
    //                .body(e.getResponseBodyAsString());
    //        }
    //    }
}
