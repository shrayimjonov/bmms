package uz.paykap.web.rest;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import springfox.documentation.annotations.ApiIgnore;
import tech.jhipster.web.util.HeaderUtil;
import tech.jhipster.web.util.PaginationUtil;
import tech.jhipster.web.util.ResponseUtil;
import uz.paykap.repository.RecipientFileSchemaRepository;
import uz.paykap.service.RecipientFileSchemaQueryService;
import uz.paykap.service.RecipientFileSchemaService;
import uz.paykap.service.criteria.RecipientFileSchemaCriteria;
import uz.paykap.service.dto.RecipientFileSchemaDTO;
import uz.paykap.web.rest.errors.BadRequestAlertException;

/**
 * REST controller for managing {@link uz.paykap.domain.RecipientFileSchema}.
 */
@ApiIgnore
@RestController
@RequestMapping("/api")
public class RecipientFileSchemaResource {

    private final Logger log = LoggerFactory.getLogger(RecipientFileSchemaResource.class);

    private static final String ENTITY_NAME = "pkbillmsRecipientFileSchema";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final RecipientFileSchemaService recipientFileSchemaService;

    private final RecipientFileSchemaRepository recipientFileSchemaRepository;

    private final RecipientFileSchemaQueryService recipientFileSchemaQueryService;

    public RecipientFileSchemaResource(
        RecipientFileSchemaService recipientFileSchemaService,
        RecipientFileSchemaRepository recipientFileSchemaRepository,
        RecipientFileSchemaQueryService recipientFileSchemaQueryService
    ) {
        this.recipientFileSchemaService = recipientFileSchemaService;
        this.recipientFileSchemaRepository = recipientFileSchemaRepository;
        this.recipientFileSchemaQueryService = recipientFileSchemaQueryService;
    }

    /**
     * {@code POST  /recipient-file-schemas} : Create a new recipientFileSchema.
     *
     * @param recipientFileSchemaDTO the recipientFileSchemaDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new recipientFileSchemaDTO, or with status {@code 400 (Bad Request)} if the recipientFileSchema has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/recipient-file-schemas")
    public ResponseEntity<RecipientFileSchemaDTO> createRecipientFileSchema(
        @Valid @RequestBody RecipientFileSchemaDTO recipientFileSchemaDTO
    ) throws URISyntaxException {
        log.debug("REST request to save RecipientFileSchema : {}", recipientFileSchemaDTO);
        if (recipientFileSchemaDTO.getId() != null) {
            throw new BadRequestAlertException("A new recipientFileSchema cannot already have an ID", ENTITY_NAME, "idexists");
        }
        RecipientFileSchemaDTO result = recipientFileSchemaService.save(recipientFileSchemaDTO);
        return ResponseEntity
            .created(new URI("/api/recipient-file-schemas/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * {@code PUT  /recipient-file-schemas/:id} : Updates an existing recipientFileSchema.
     *
     * @param id the id of the recipientFileSchemaDTO to save.
     * @param recipientFileSchemaDTO the recipientFileSchemaDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated recipientFileSchemaDTO,
     * or with status {@code 400 (Bad Request)} if the recipientFileSchemaDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the recipientFileSchemaDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/recipient-file-schemas/{id}")
    public ResponseEntity<RecipientFileSchemaDTO> updateRecipientFileSchema(
        @PathVariable(value = "id", required = false) final Long id,
        @Valid @RequestBody RecipientFileSchemaDTO recipientFileSchemaDTO
    ) throws URISyntaxException {
        log.debug("REST request to update RecipientFileSchema : {}, {}", id, recipientFileSchemaDTO);
        if (recipientFileSchemaDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, recipientFileSchemaDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!recipientFileSchemaRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        RecipientFileSchemaDTO result = recipientFileSchemaService.save(recipientFileSchemaDTO);
        return ResponseEntity
            .ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, recipientFileSchemaDTO.getId().toString()))
            .body(result);
    }

    /**
     * {@code PATCH  /recipient-file-schemas/:id} : Partial updates given fields of an existing recipientFileSchema, field will ignore if it is null
     *
     * @param id the id of the recipientFileSchemaDTO to save.
     * @param recipientFileSchemaDTO the recipientFileSchemaDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated recipientFileSchemaDTO,
     * or with status {@code 400 (Bad Request)} if the recipientFileSchemaDTO is not valid,
     * or with status {@code 404 (Not Found)} if the recipientFileSchemaDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the recipientFileSchemaDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/recipient-file-schemas/{id}", consumes = "application/merge-patch+json")
    public ResponseEntity<RecipientFileSchemaDTO> partialUpdateRecipientFileSchema(
        @PathVariable(value = "id", required = false) final Long id,
        @NotNull @RequestBody RecipientFileSchemaDTO recipientFileSchemaDTO
    ) throws URISyntaxException {
        log.debug("REST request to partial update RecipientFileSchema partially : {}, {}", id, recipientFileSchemaDTO);
        if (recipientFileSchemaDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, recipientFileSchemaDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!recipientFileSchemaRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<RecipientFileSchemaDTO> result = recipientFileSchemaService.partialUpdate(recipientFileSchemaDTO);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, recipientFileSchemaDTO.getId().toString())
        );
    }

    /**
     * {@code GET  /recipient-file-schemas} : get all the recipientFileSchemas.
     *
     * @param pageable the pagination information.
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of recipientFileSchemas in body.
     */
    @GetMapping("/recipient-file-schemas")
    public ResponseEntity<List<RecipientFileSchemaDTO>> getAllRecipientFileSchemas(
        RecipientFileSchemaCriteria criteria,
        Pageable pageable
    ) {
        log.debug("REST request to get RecipientFileSchemas by criteria: {}", criteria);
        Page<RecipientFileSchemaDTO> page = recipientFileSchemaQueryService.findByCriteria(criteria, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /recipient-file-schemas/count} : count all the recipientFileSchemas.
     *
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the count in body.
     */
    @GetMapping("/recipient-file-schemas/count")
    public ResponseEntity<Long> countRecipientFileSchemas(RecipientFileSchemaCriteria criteria) {
        log.debug("REST request to count RecipientFileSchemas by criteria: {}", criteria);
        return ResponseEntity.ok().body(recipientFileSchemaQueryService.countByCriteria(criteria));
    }

    /**
     * {@code GET  /recipient-file-schemas/:id} : get the "id" recipientFileSchema.
     *
     * @param id the id of the recipientFileSchemaDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the recipientFileSchemaDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/recipient-file-schemas/{id}")
    public ResponseEntity<RecipientFileSchemaDTO> getRecipientFileSchema(@PathVariable Long id) {
        log.debug("REST request to get RecipientFileSchema : {}", id);
        Optional<RecipientFileSchemaDTO> recipientFileSchemaDTO = recipientFileSchemaService.findOne(id);
        return ResponseUtil.wrapOrNotFound(recipientFileSchemaDTO);
    }

    /**
     * {@code DELETE  /recipient-file-schemas/:id} : delete the "id" recipientFileSchema.
     *
     * @param id the id of the recipientFileSchemaDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/recipient-file-schemas/{id}")
    public ResponseEntity<Void> deleteRecipientFileSchema(@PathVariable Long id) {
        log.debug("REST request to delete RecipientFileSchema : {}", id);
        recipientFileSchemaService.delete(id);
        return ResponseEntity
            .noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString()))
            .build();
    }
}
