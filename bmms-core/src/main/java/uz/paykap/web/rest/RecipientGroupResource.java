package uz.paykap.web.rest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import springfox.documentation.annotations.ApiIgnore;
import tech.jhipster.web.util.HeaderUtil;
import tech.jhipster.web.util.PaginationUtil;
import tech.jhipster.web.util.ResponseUtil;
import uz.paykap.repository.RecipientGroupRepository;
import uz.paykap.service.RecipientGroupQueryService;
import uz.paykap.service.RecipientGroupService;
import uz.paykap.service.criteria.RecipientGroupCriteria;
import uz.paykap.service.dto.RecipientGroupDTO;
import uz.paykap.web.rest.errors.BadRequestAlertException;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * REST controller for managing {@link uz.paykap.domain.RecipientGroup}.
 */
@ApiIgnore
@RestController
@RequestMapping("/api")
public class RecipientGroupResource {

    private final Logger log = LoggerFactory.getLogger(RecipientGroupResource.class);

    private static final String ENTITY_NAME = "pkbillmsRecipientGroup";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final RecipientGroupService recipientGroupService;

    private final RecipientGroupRepository recipientGroupRepository;

    private final RecipientGroupQueryService recipientGroupQueryService;

    public RecipientGroupResource(
        RecipientGroupService recipientGroupService,
        RecipientGroupRepository recipientGroupRepository,
        RecipientGroupQueryService recipientGroupQueryService
    ) {
        this.recipientGroupService = recipientGroupService;
        this.recipientGroupRepository = recipientGroupRepository;
        this.recipientGroupQueryService = recipientGroupQueryService;
    }

    /**
     * {@code POST  /recipient-groups} : Create a new recipientGroup.
     *
     * @param recipientGroupDTO the recipientGroupDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new recipientGroupDTO, or with status {@code 400 (Bad Request)} if the recipientGroup has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/recipient-groups")
    public ResponseEntity<RecipientGroupDTO> createRecipientGroup(@Valid @RequestBody RecipientGroupDTO recipientGroupDTO)
        throws URISyntaxException {
        log.debug("REST request to save RecipientGroup : {}", recipientGroupDTO);
        if (recipientGroupDTO.getId() != null) {
            throw new BadRequestAlertException("A new recipientGroup cannot already have an ID", ENTITY_NAME, "idexists");
        }
        RecipientGroupDTO result = recipientGroupService.save(recipientGroupDTO);
        return ResponseEntity
            .created(new URI("/api/recipient-groups/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * {@code PUT  /recipient-groups/:id} : Updates an existing recipientGroup.
     *
     * @param id the id of the recipientGroupDTO to save.
     * @param recipientGroupDTO the recipientGroupDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated recipientGroupDTO,
     * or with status {@code 400 (Bad Request)} if the recipientGroupDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the recipientGroupDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/recipient-groups/{id}")
    public ResponseEntity<RecipientGroupDTO> updateRecipientGroup(
        @PathVariable(value = "id", required = false) final Long id,
        @Valid @RequestBody RecipientGroupDTO recipientGroupDTO
    ) throws URISyntaxException {
        log.debug("REST request to update RecipientGroup : {}, {}", id, recipientGroupDTO);
        if (recipientGroupDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, recipientGroupDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!recipientGroupRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        RecipientGroupDTO result = recipientGroupService.save(recipientGroupDTO);
        return ResponseEntity
            .ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, recipientGroupDTO.getId().toString()))
            .body(result);
    }

    /**
     * {@code PATCH  /recipient-groups/:id} : Partial updates given fields of an existing recipientGroup, field will ignore if it is null
     *
     * @param id the id of the recipientGroupDTO to save.
     * @param recipientGroupDTO the recipientGroupDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated recipientGroupDTO,
     * or with status {@code 400 (Bad Request)} if the recipientGroupDTO is not valid,
     * or with status {@code 404 (Not Found)} if the recipientGroupDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the recipientGroupDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/recipient-groups/{id}", consumes = "application/merge-patch+json")
    public ResponseEntity<RecipientGroupDTO> partialUpdateRecipientGroup(
        @PathVariable(value = "id", required = false) final Long id,
        @NotNull @RequestBody RecipientGroupDTO recipientGroupDTO
    ) throws URISyntaxException {
        log.debug("REST request to partial update RecipientGroup partially : {}, {}", id, recipientGroupDTO);
        if (recipientGroupDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, recipientGroupDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!recipientGroupRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<RecipientGroupDTO> result = recipientGroupService.partialUpdate(recipientGroupDTO);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, recipientGroupDTO.getId().toString())
        );
    }

    /**
     * {@code GET  /recipient-groups} : get all the recipientGroups.
     *
     * @param pageable the pagination information.
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of recipientGroups in body.
     */
    @GetMapping("/recipient-groups")
    public ResponseEntity<List<RecipientGroupDTO>> getAllRecipientGroups(RecipientGroupCriteria criteria, Pageable pageable, Boolean unPaged) {
        log.debug("REST request to get RecipientGroups by criteria: {}", criteria);
        Page<RecipientGroupDTO> page = recipientGroupQueryService.findByCriteria(criteria, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /recipient-groups/count} : count all the recipientGroups.
     *
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the count in body.
     */
    @GetMapping("/recipient-groups/count")
    public ResponseEntity<Long> countRecipientGroups(RecipientGroupCriteria criteria) {
        log.debug("REST request to count RecipientGroups by criteria: {}", criteria);
        return ResponseEntity.ok().body(recipientGroupQueryService.countByCriteria(criteria));
    }

    /**
     * {@code GET  /recipient-groups/:id} : get the "id" recipientGroup.
     *
     * @param id the id of the recipientGroupDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the recipientGroupDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/recipient-groups/{id}")
    public ResponseEntity<RecipientGroupDTO> getRecipientGroup(@PathVariable Long id) {
        log.debug("REST request to get RecipientGroup : {}", id);
        Optional<RecipientGroupDTO> recipientGroupDTO = recipientGroupService.findOne(id);
        return ResponseUtil.wrapOrNotFound(recipientGroupDTO);
    }

    /**
     * {@code DELETE  /recipient-groups/:id} : delete the "id" recipientGroup.
     *
     * @param id the id of the recipientGroupDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/recipient-groups/{id}")
    public ResponseEntity<Void> deleteRecipientGroup(@PathVariable Long id) {
        log.debug("REST request to delete RecipientGroup : {}", id);
        recipientGroupService.delete(id);
        return ResponseEntity
            .noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString()))
            .build();
    }
}
