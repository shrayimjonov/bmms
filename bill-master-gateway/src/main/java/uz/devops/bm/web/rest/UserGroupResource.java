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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tech.jhipster.web.util.HeaderUtil;
import tech.jhipster.web.util.PaginationUtil;
import tech.jhipster.web.util.reactive.ResponseUtil;
import uz.devops.bm.domain.enumeration.UserGroupType;
import uz.devops.bm.repository.UserGroupRepository;
import uz.devops.bm.security.AuthoritiesConstants;
import uz.devops.bm.service.UserGroupService;
import uz.devops.bm.service.criteria.UserGroupCriteria;
import uz.devops.bm.service.dto.AdminUserGroupDTO;
import uz.devops.bm.service.dto.UserGroupDTO;
import uz.devops.bm.service.errors.BadRequestAlertException;

/**
 * REST controller for managing {@link uz.devops.bm.domain.UserGroup}.
 */
@RestController
@RequestMapping("/api")
public class UserGroupResource {

    private final Logger log = LoggerFactory.getLogger(UserGroupResource.class);

    private static final String ENTITY_NAME = "userGroup";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final UserGroupService userGroupService;

    private final UserGroupRepository userGroupRepository;

    public UserGroupResource(UserGroupService userGroupService, UserGroupRepository userGroupRepository) {
        this.userGroupService = userGroupService;
        this.userGroupRepository = userGroupRepository;
    }

    private static final List<String> ALLOWED_ORDERED_PROPERTIES = Collections.unmodifiableList(
        Arrays.asList("id", "name", "active", "details", "type", "authorities")
    );

    /**
     * {@code POST  /user-groups} : Create a new userGroup.
     *
     * @param userGroupDTO the userGroupDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new userGroupDTO, or with status {@code 400 (Bad Request)} if the userGroup has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/user-groups")
    public Mono<ResponseEntity<UserGroupDTO>> createUserGroup(@Valid @RequestBody AdminUserGroupDTO userGroupDTO)
        throws URISyntaxException {
        log.debug("REST request to save UserGroup : {}", userGroupDTO);
        if (userGroupDTO.getId() != null) {
            throw new BadRequestAlertException("A new userGroup cannot already have an ID", ENTITY_NAME, "idexists");
        }
        return userGroupService
            .save(userGroupDTO)
            .map(result -> {
                try {
                    return ResponseEntity
                        .created(new URI("/api/user-groups/" + result.getId()))
                        .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, result.getId().toString()))
                        .body(result);
                } catch (URISyntaxException e) {
                    throw new RuntimeException(e);
                }
            });
    }

    /**
     * {@code PUT  /user-groups/:id} : Updates an existing userGroup.
     *
     * @param id           the id of the userGroupDTO to save.
     * @param userGroupDTO the userGroupDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated userGroupDTO,
     * or with status {@code 400 (Bad Request)} if the userGroupDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the userGroupDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/user-groups/{id}")
    public Mono<ResponseEntity<UserGroupDTO>> updateUserGroup(
        @PathVariable(value = "id", required = false) final Long id,
        @Valid @RequestBody AdminUserGroupDTO userGroupDTO
    ) throws URISyntaxException {
        log.debug("REST request to update UserGroup : {}, {}", id, userGroupDTO);
        if (userGroupDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, userGroupDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        return userGroupRepository
            .existsById(id)
            .flatMap(exists -> {
                if (!exists) {
                    return Mono.error(new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound"));
                }

                return userGroupService
                    .save(userGroupDTO)
                    .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND)))
                    .map(result ->
                        ResponseEntity
                            .ok()
                            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, result.getId().toString()))
                            .body(result)
                    );
            });
    }

    /**
     * {@code PATCH  /user-groups/:id} : Partial updates given fields of an existing userGroup, field will ignore if it is null
     *
     * @param id           the id of the userGroupDTO to save.
     * @param userGroupDTO the userGroupDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated userGroupDTO,
     * or with status {@code 400 (Bad Request)} if the userGroupDTO is not valid,
     * or with status {@code 404 (Not Found)} if the userGroupDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the userGroupDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/user-groups/{id}", consumes = "application/merge-patch+json")
    public Mono<ResponseEntity<UserGroupDTO>> partialUpdateUserGroup(
        @PathVariable(value = "id", required = false) final Long id,
        @NotNull @RequestBody UserGroupDTO userGroupDTO
    ) throws URISyntaxException {
        log.debug("REST request to partial update UserGroup partially : {}, {}", id, userGroupDTO);
        if (userGroupDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, userGroupDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        return userGroupRepository
            .existsById(id)
            .flatMap(exists -> {
                if (!exists) {
                    return Mono.error(new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound"));
                }

                Mono<UserGroupDTO> result = userGroupService.partialUpdate(userGroupDTO);

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
     * {@code GET  /user-groups} : get all the userGroups.
     *
     * @param pageable the pagination information.
     * @param request  a {@link ServerHttpRequest} request.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of userGroups in body.
     */
    @GetMapping("/user-groups")
    public Mono<ResponseEntity<List<UserGroupDTO>>> getAllUserGroups(Pageable pageable, ServerHttpRequest request) {
        log.debug("REST request to get a page of UserGroups");
        return userGroupService
            .countAll()
            .zipWith(userGroupService.findAll(pageable).collectList())
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
     * {@code GET  /user-groups/:id} : get the "id" userGroup.
     *
     * @param id the id of the userGroupDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the userGroupDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/user-groups/{id}")
    public Mono<ResponseEntity<UserGroupDTO>> getUserGroup(@PathVariable Long id) {
        log.debug("REST request to get UserGroup : {}", id);
        Mono<UserGroupDTO> userGroupDTO = userGroupService.findOne(id);
        return ResponseUtil.wrapOrNotFound(userGroupDTO);
    }

    /**
     * {@code DELETE  /user-groups/:id} : delete the "id" userGroup.
     *
     * @param id the id of the userGroupDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/user-groups/{id}")
    @ResponseStatus(code = HttpStatus.NO_CONTENT)
    public Mono<ResponseEntity<Void>> deleteUserGroup(@PathVariable Long id) {
        log.debug("REST request to delete UserGroup : {}", id);
        return userGroupService
            .delete(id)
            .map(result ->
                ResponseEntity
                    .noContent()
                    .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString()))
                    .build()
            );
    }

    @GetMapping("/user-groups/all")
    public Mono<List<AdminUserGroupDTO>> getAllUserGroups(Pageable pageable) {
        log.debug("REST request to get all User Groups");
        if (!onlyContainsAllowedProperties(pageable)) {
            return Mono.error(new RuntimeException("Pageable null"));
        }

        return userGroupService
            .getAllManagedUsers(pageable)
            .flatMap(userGroups -> {
                log.debug("User Groups: {}", userGroups);
                return Mono.just(userGroups);
            });
    }

    @GetMapping("/user-groups/by-type/{type}")
    public Mono<List<AdminUserGroupDTO>> getAllUserGroupsByType(Pageable pageable, @PathVariable UserGroupType type) {
        log.debug("REST request to get all User Groups");
        if (!onlyContainsAllowedProperties(pageable)) {
            return Mono.error(new RuntimeException("Pageable null"));
        }

        return userGroupService
            .getAllManagedUsersByType(pageable, type)
            .flatMap(userGroups -> {
                log.debug("User Groups: {}", userGroups);
                return Mono.just(userGroups);
            });
    }

    private boolean onlyContainsAllowedProperties(Pageable pageable) {
        return pageable.getSort().stream().map(Sort.Order::getProperty).allMatch(ALLOWED_ORDERED_PROPERTIES::contains);
    }

    @GetMapping("/user-groups/by-criteria")
    public Mono<ResponseEntity<?>> getAllUsersByCriteria(UserGroupCriteria criteria, ServerHttpRequest request, Pageable pageable) {
        log.debug("REST request to find all User Group by criteria");

        if (!onlyContainsAllowedProperties(pageable)) {
            return Mono.just(ResponseEntity.badRequest().build());
        }

        return userGroupService
            .countByCriteria(criteria)
            .map(total -> new PageImpl<>(new ArrayList<>(), pageable, total))
            .map(page -> PaginationUtil.generatePaginationHttpHeaders(UriComponentsBuilder.fromHttpRequest(request), page))
            .map(headers -> ResponseEntity.ok().headers(headers).body(userGroupService.findByCriteria(criteria, pageable)));
    }
}
