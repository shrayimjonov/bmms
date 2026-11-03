package uz.devops.bm.web.rest;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;
import tech.jhipster.web.util.HeaderUtil;
import tech.jhipster.web.util.PaginationUtil;
import uz.devops.bm.domain.Authority;
import uz.devops.bm.repository.AuthorityRepository;
import uz.devops.bm.service.criteria.AuthorityCriteria;
import uz.devops.bm.service.errors.NameAlreadyUsedException;

@RestController
@RequestMapping("/api/authority")
@Slf4j
public class AuthorityResource {

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final AuthorityRepository authorityRepository;

    private static final List<String> ALLOWED_ORDERED_PROPERTIES = Collections.unmodifiableList(Arrays.asList("name", "details"));

    public AuthorityResource(AuthorityRepository authorityRepository) {
        this.authorityRepository = authorityRepository;
    }

    @PostMapping("/create")
    public Mono<ResponseEntity<Authority>> create(@Valid @RequestBody Authority authority) {
        log.debug("REST request to save Authority : {}", authority);
        return authorityRepository
            .findOneByName(authority.getName())
            .hasElement()
            .flatMap(nameExists -> {
                if (Boolean.TRUE.equals(nameExists)) {
                    return Mono.error(new NameAlreadyUsedException());
                }
                return authorityRepository.save(authority);
            })
            .map(result -> {
                try {
                    return ResponseEntity
                        .created(new URI("/api/authority/" + authority.getName()))
                        .headers(HeaderUtil.createAlert(applicationName, "authority.created", authority.getName()))
                        .body(authority);
                } catch (URISyntaxException e) {
                    throw new RuntimeException(e);
                }
            });
    }

    @DeleteMapping("/{name}")
    public Mono<Void> delete(@PathVariable String name) throws URISyntaxException {
        log.debug("REST request to delete Authority by name : {}", name);
        return authorityRepository.deleteById(name);
    }

    private boolean onlyContainsAllowedProperties(Pageable pageable) {
        return pageable.getSort().stream().map(Sort.Order::getProperty).allMatch(ALLOWED_ORDERED_PROPERTIES::contains);
    }

    @GetMapping("/get-all")
    public Mono<ResponseEntity<?>> getAll(AuthorityCriteria criteria, ServerHttpRequest request, Pageable pageable) {
        log.debug("REST request to find all Authority by criteria");

        if (!onlyContainsAllowedProperties(pageable)) {
            return Mono.just(ResponseEntity.badRequest().build());
        }

        return authorityRepository
            .countByCriteria(criteria)
            .map(total -> new PageImpl<>(new ArrayList<>(), pageable, total))
            .map(page -> PaginationUtil.generatePaginationHttpHeaders(UriComponentsBuilder.fromHttpRequest(request), page))
            .map(headers -> ResponseEntity.ok().headers(headers).body(authorityRepository.findByCriteria(criteria, pageable)));
    }
}
