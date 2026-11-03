package uz.devops.bm.web.rest;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tech.jhipster.web.util.HeaderUtil;
import tech.jhipster.web.util.PaginationUtil;
import tech.jhipster.web.util.reactive.ResponseUtil;
import uz.devops.bm.domain.JsonForm;
import uz.devops.bm.repository.JsonFormRepository;
import uz.devops.bm.security.SecurityUtils;
import uz.devops.bm.service.JsonFormService;
import uz.devops.bm.service.criteria.JsonFormCriteria;
import uz.devops.bm.service.dto.JsonFormDTO;
import uz.devops.bm.service.errors.BadRequestAlertException;

/**
 * REST controller for managing {@link uz.devops.bm.domain.JsonForm}.
 */
@RestController
@RequestMapping("/api")
public class JsonFormResource {

    private final Logger log = LoggerFactory.getLogger(JsonFormResource.class);

    private static final List<String> ALLOWED_ORDERED_PROPERTIES = Collections.unmodifiableList(
        Arrays.asList("id", "name", "type", "data", "active", "description")
    );

    private static final String ENTITY_NAME = "jsonForm";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final JsonFormService jsonFormService;

    private final JsonFormRepository jsonFormRepository;

    public JsonFormResource(JsonFormService jsonFormService, JsonFormRepository jsonFormRepository) {
        this.jsonFormService = jsonFormService;
        this.jsonFormRepository = jsonFormRepository;
    }

    /**
     * {@code POST  /json-forms} : Create a new jsonForm.
     *
     * @param jsonFormDTO the jsonFormDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new jsonFormDTO, or with status {@code 400 (Bad Request)} if the jsonForm has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/json-forms")
    public Mono<ResponseEntity<JsonFormDTO>> createJsonForm(@Valid @RequestBody JsonFormDTO jsonFormDTO) throws URISyntaxException {
        log.debug("REST request to save JsonForm : {}", jsonFormDTO);
        if (jsonFormDTO.getId() != null) {
            throw new BadRequestAlertException("A new jsonForm cannot already have an ID", ENTITY_NAME, "idexists");
        }
        return SecurityUtils
            .getCurrentUserLogin()
            .flatMap(username -> {
                jsonFormDTO.setUsername(username);
                return jsonFormService
                    .save(jsonFormDTO)
                    .map(result -> {
                        try {
                            return ResponseEntity
                                .created(new URI("/api/json-forms/" + result.getId()))
                                .headers(
                                    HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, result.getId().toString())
                                )
                                .body(result);
                        } catch (URISyntaxException e) {
                            throw new RuntimeException(e);
                        }
                    });
            });
    }

    /**
     * {@code PUT  /json-forms/:id} : Updates an existing jsonForm.
     *
     * @param id          the id of the jsonFormDTO to save.
     * @param jsonFormDTO the jsonFormDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated jsonFormDTO,
     * or with status {@code 400 (Bad Request)} if the jsonFormDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the jsonFormDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/json-forms/{id}")
    public Mono<ResponseEntity<JsonFormDTO>> updateJsonForm(
        @PathVariable(value = "id", required = false) final Long id,
        @Valid @RequestBody JsonFormDTO jsonFormDTO
    ) throws URISyntaxException {
        log.debug("REST request to update JsonForm : {}, {}", id, jsonFormDTO);
        if (jsonFormDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, jsonFormDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }
        return SecurityUtils
            .getCurrentUserLogin()
            .flatMap(username ->
                jsonFormRepository
                    .existsById(id)
                    .flatMap(exists -> {
                        if (!exists) {
                            return Mono.error(new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound"));
                        }
                        jsonFormDTO.setUsername(username);

                        return jsonFormService
                            .save(jsonFormDTO)
                            .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND)))
                            .map(result ->
                                ResponseEntity
                                    .ok()
                                    .headers(
                                        HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, result.getId().toString())
                                    )
                                    .body(result)
                            );
                    })
            );
    }

    /**
     * {@code PATCH  /json-forms/:id} : Partial updates given fields of an existing jsonForm, field will ignore if it is null
     *
     * @param id          the id of the jsonFormDTO to save.
     * @param jsonFormDTO the jsonFormDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated jsonFormDTO,
     * or with status {@code 400 (Bad Request)} if the jsonFormDTO is not valid,
     * or with status {@code 404 (Not Found)} if the jsonFormDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the jsonFormDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/json-forms/{id}", consumes = "application/merge-patch+json")
    public Mono<ResponseEntity<JsonFormDTO>> partialUpdateJsonForm(
        @PathVariable(value = "id", required = false) final Long id,
        @NotNull @RequestBody JsonFormDTO jsonFormDTO
    ) throws URISyntaxException {
        log.debug("REST request to partial update JsonForm partially : {}, {}", id, jsonFormDTO);
        if (jsonFormDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, jsonFormDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        return jsonFormRepository
            .existsById(id)
            .flatMap(exists -> {
                if (!exists) {
                    return Mono.error(new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound"));
                }

                Mono<JsonFormDTO> result = jsonFormService.partialUpdate(jsonFormDTO);

                return result
                    .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND)))
                    .map(res ->
                        ResponseEntity
                            .ok()
                            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, res.getId().toString()))
                            .body(res)
                    );
            });
    }

    /**
     * {@code GET  /json-forms} : get all the jsonForms.
     *
     * @param pageable  the pagination information.
     * @param request   a {@link ServerHttpRequest} request.
     * @param eagerload flag to eager load entities from relationships (This is applicable for many-to-many).
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of jsonForms in body.
     */
    @GetMapping("/json-forms")
    public Mono<ResponseEntity<List<JsonFormDTO>>> getAllJsonForms(
        Pageable pageable,
        ServerHttpRequest request,
        @RequestParam(required = false, defaultValue = "false") boolean eagerload
    ) {
        log.debug("REST request to get a page of JsonForms");
        return jsonFormService
            .countAll()
            .zipWith(jsonFormService.findAll(pageable).collectList())
            .map(countWithEntities ->
                ResponseEntity
                    .ok()
                    .headers(
                        PaginationUtil.generatePaginationHttpHeaders(
                            UriComponentsBuilder.fromHttpRequest(request),
                            new PageImpl<>(countWithEntities.getT2(), pageable, countWithEntities.getT1())
                        )
                    )
                    .body(countWithEntities.getT2())
            );
    }

    /**
     * {@code GET  /json-forms/:id} : get the "id" jsonForm.
     *
     * @param id the id of the jsonFormDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the jsonFormDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/json-forms/{id}")
    public Mono<ResponseEntity<JsonFormDTO>> getJsonForm(@PathVariable Long id) {
        log.debug("REST request to get JsonForm : {}", id);
        Mono<JsonFormDTO> jsonFormDTO = jsonFormService.findOne(id);
        return ResponseUtil.wrapOrNotFound(jsonFormDTO);
    }

    /**
     * {@code DELETE  /json-forms/:id} : delete the "id" jsonForm.
     *
     * @param id the id of the jsonFormDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/json-forms/{id}")
    @ResponseStatus(code = HttpStatus.NO_CONTENT)
    public Mono<ResponseEntity<Void>> deleteJsonForm(@PathVariable Long id) {
        log.debug("REST request to delete JsonForm : {}", id);
        return jsonFormService
            .delete(id)
            .map(result ->
                ResponseEntity
                    .noContent()
                    .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString()))
                    .build()
            );
    }

    @GetMapping("/json-forms/by-criteria")
    public Mono<ResponseEntity<Flux<JsonFormDTO>>> getAllJsonFormsByCriteria(
        JsonFormCriteria criteria,
        ServerHttpRequest request,
        Pageable pageable
    ) {
        log.debug("REST request to find all JsonForms by criteria");

        if (!onlyContainsAllowedProperties(pageable)) {
            return Mono.just(ResponseEntity.badRequest().build());
        }

        return jsonFormService
            .countByCriteria(criteria)
            .map(total -> new PageImpl<>(new ArrayList<>(), pageable, total))
            .map(page -> PaginationUtil.generatePaginationHttpHeaders(UriComponentsBuilder.fromHttpRequest(request), page))
            .map(headers -> ResponseEntity.ok().headers(headers).body(jsonFormService.findByCriteria(criteria, pageable)));
    }

    @GetMapping("/json-forms/by-user")
    public ResponseEntity<Mono<List<JsonForm>>> getAllUserForms() {
        log.debug("REST request to get all user forms");
        return ResponseEntity
            .ok()
            .body(
                SecurityUtils
                    .getCurrentUserLogin()
                    .flatMap(username -> jsonFormRepository.findAllByUsers_loginOrUsersIsNull(username).collectList())
            );
    }

    private boolean onlyContainsAllowedProperties(Pageable pageable) {
        return pageable.getSort().stream().map(Sort.Order::getProperty).allMatch(ALLOWED_ORDERED_PROPERTIES::contains);
    }
}
