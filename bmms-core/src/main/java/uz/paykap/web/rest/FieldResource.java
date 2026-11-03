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
import tech.jhipster.service.filter.LongFilter;
import tech.jhipster.web.util.HeaderUtil;
import tech.jhipster.web.util.PaginationUtil;
import tech.jhipster.web.util.ResponseUtil;
import uz.paykap.domain.Recipient;
import uz.paykap.repository.FieldRepository;
import uz.paykap.repository.RecipientRepository;
import uz.paykap.service.FieldQueryService;
import uz.paykap.service.FieldService;
import uz.paykap.service.criteria.FieldCriteria;
import uz.paykap.service.dto.FieldDTO;
import uz.paykap.web.rest.errors.BadRequestAlertException;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * REST controller for managing {@link uz.paykap.domain.Field}.
 */
@ApiIgnore
@RestController
@RequestMapping("/api")
public class FieldResource {

    private final Logger log = LoggerFactory.getLogger(FieldResource.class);

    private static final String ENTITY_NAME = "pkbillmsField";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final FieldService fieldService;

    private final FieldRepository fieldRepository;

    private final FieldQueryService fieldQueryService;
    private final RecipientRepository recipientRepository;

    public FieldResource(
        FieldService fieldService,
        FieldQueryService fieldQueryService,
        RecipientRepository recipientRepository,
        FieldRepository fieldRepository
    ) {
        this.fieldService = fieldService;
        this.fieldRepository = fieldRepository;
        this.fieldQueryService = fieldQueryService;
        this.recipientRepository = recipientRepository;
    }

    /**
     * {@code POST  /fields} : Create a new field.
     *
     * @param fieldDTO the fieldDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new fieldDTO, or with status {@code 400 (Bad Request)} if the field has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/fields")
    public ResponseEntity<FieldDTO> createField(@Valid @RequestBody FieldDTO fieldDTO) throws URISyntaxException {
        log.debug("REST request to save Field : {}", fieldDTO);
        if (fieldDTO.getId() != null) {
            throw new BadRequestAlertException("A new field cannot already have an ID", ENTITY_NAME, "idexists");
        }
        FieldDTO result = fieldService.save(fieldDTO);
        return ResponseEntity
            .created(new URI("/api/fields/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * {@code PUT  /fields/:id} : Updates an existing field.
     *
     * @param id the id of the fieldDTO to save.
     * @param fieldDTO the fieldDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated fieldDTO,
     * or with status {@code 400 (Bad Request)} if the fieldDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the fieldDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/fields/{id}")
    public ResponseEntity<FieldDTO> updateField(
        @PathVariable(value = "id", required = false) final Long id,
        @Valid @RequestBody FieldDTO fieldDTO
    ) throws URISyntaxException {
        log.debug("REST request to update Field : {}, {}", id, fieldDTO);
        if (fieldDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, fieldDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!fieldRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        FieldDTO result = fieldService.save(fieldDTO);
        return ResponseEntity
            .ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, fieldDTO.getId().toString()))
            .body(result);
    }

    /**
     * {@code PATCH  /fields/:id} : Partial updates given fields of an existing field, field will ignore if it is null
     *
     * @param id the id of the fieldDTO to save.
     * @param fieldDTO the fieldDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated fieldDTO,
     * or with status {@code 400 (Bad Request)} if the fieldDTO is not valid,
     * or with status {@code 404 (Not Found)} if the fieldDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the fieldDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/fields/{id}", consumes = "application/merge-patch+json")
    public ResponseEntity<FieldDTO> partialUpdateField(
        @PathVariable(value = "id", required = false) final Long id,
        @NotNull @RequestBody FieldDTO fieldDTO
    ) throws URISyntaxException {
        log.debug("REST request to partial update Field partially : {}, {}", id, fieldDTO);
        if (fieldDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, fieldDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!fieldRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<FieldDTO> result = fieldService.partialUpdate(fieldDTO);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, fieldDTO.getId().toString())
        );
    }

    /**
     * {@code GET  /fields} : get all the fields.
     *
     * @param pageable the pagination information.
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of fields in body.
     */
    @GetMapping("/fields")
    public ResponseEntity<List<FieldDTO>> getAllFields(FieldCriteria criteria, Pageable pageable, Boolean unPaged) {
        log.debug("REST request to get Fields by criteria: {}", criteria);
        Page<FieldDTO> page = fieldQueryService.findByCriteria(criteria, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    @GetMapping("/fields/criteria")
    public ResponseEntity<List<FieldDTO>> getAllFields(FieldCriteria criteria, Pageable pageable, String recipientShortName) {
        log.debug("REST request to get Fields by criteria: {}", criteria);
        log.debug("With recipient name :{}", recipientShortName);
        Recipient recipient = recipientRepository.findTop1ByShortName(recipientShortName).orElse(null);

        if (recipientShortName != null) {
            LongFilter longFilter = new LongFilter();
            longFilter.setEquals(recipient.getId());
            criteria.setRecipientId(longFilter);
        }

        Page<FieldDTO> page = fieldQueryService.findByCriteria(criteria, pageable);
        HttpHeaders headers = uz.paykap.web.rest.util.PaginationUtil.generatePaginationHttpHeaders(page, "/api/fields");
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /fields/count} : count all the fields.
     *
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the count in body.
     */
    @GetMapping("/fields/count")
    public ResponseEntity<Long> countFields(FieldCriteria criteria) {
        log.debug("REST request to count Fields by criteria: {}", criteria);
        return ResponseEntity.ok().body(fieldQueryService.countByCriteria(criteria));
    }

    /**
     * {@code GET  /fields/:id} : get the "id" field.
     *
     * @param id the id of the fieldDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the fieldDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/fields/{id}")
    public ResponseEntity<FieldDTO> getField(@PathVariable Long id) {
        log.debug("REST request to get Field : {}", id);
        Optional<FieldDTO> fieldDTO = fieldService.findOne(id);
        return ResponseUtil.wrapOrNotFound(fieldDTO);
    }

    /**
     * GET  /fields/byRecipient/:id : get the "id" recipient.
     *
     * @param id the id of the recipientDTO to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the list of fieldDTO, or with status 404 (Not Found)
     */
    @GetMapping("/fields/byRecipient/{id}")
    public ResponseEntity<List<FieldDTO>> getFieldsByRecipientId(@PathVariable Long id) {
        log.debug("REST request to get Fields by Recipient: {}", id);
        return ResponseEntity.ok(fieldService.findAllByRecipientId(id));
    }

    /**
     * {@code DELETE  /fields/:id} : delete the "id" field.
     *
     * @param id the id of the fieldDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/fields/{id}")
    public ResponseEntity<Void> deleteField(@PathVariable Long id) {
        log.debug("REST request to delete Field : {}", id);
        fieldService.delete(id);
        return ResponseEntity
            .noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString()))
            .build();
    }

    @DeleteMapping("/fields/cascaded/{id}")
    public ResponseEntity<Void> deleteFieldCascaded(@PathVariable Long id) {
        log.debug("REST request to delete Field : {}", id);
        fieldService.deleteCascaded(id);
        return ResponseEntity
            .ok()
            .headers(uz.paykap.web.rest.util.HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, id.toString()))
            .build();
    }
}
