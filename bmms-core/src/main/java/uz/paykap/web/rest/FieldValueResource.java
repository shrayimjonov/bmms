package uz.paykap.web.rest;

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
import tech.jhipster.service.filter.LongFilter;
import tech.jhipster.web.util.HeaderUtil;
import tech.jhipster.web.util.PaginationUtil;
import tech.jhipster.web.util.ResponseUtil;
import uz.paykap.domain.Field;
import uz.paykap.repository.FieldRepository;
import uz.paykap.repository.FieldValueRepository;
import uz.paykap.service.FieldValueQueryService;
import uz.paykap.service.FieldValueService;
import uz.paykap.service.criteria.FieldValueCriteria;
import uz.paykap.service.dto.FieldValueDTO;
import uz.paykap.web.rest.errors.BadRequestAlertException;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * REST controller for managing {@link uz.paykap.domain.FieldValue}.
 */
@ApiIgnore
@RestController
@RequestMapping("/api")
public class FieldValueResource {

    private final Logger log = LoggerFactory.getLogger(FieldValueResource.class);

    private static final String ENTITY_NAME = "pkbillmsFieldValue";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final FieldValueService fieldValueService;

    private final FieldValueRepository fieldValueRepository;

    private final FieldValueQueryService fieldValueQueryService;
    private final FieldRepository fieldRepository;

    public FieldValueResource(
        FieldValueService fieldValueService,
        FieldValueRepository fieldValueRepository,
        FieldValueQueryService fieldValueQueryService,
        FieldRepository fieldRepository
    ) {
        this.fieldValueService = fieldValueService;
        this.fieldValueRepository = fieldValueRepository;
        this.fieldValueQueryService = fieldValueQueryService;
        this.fieldRepository = fieldRepository;
    }

    /**
     * {@code POST  /field-values} : Create a new fieldValue.
     *
     * @param fieldValueDTO the fieldValueDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new fieldValueDTO, or with status {@code 400 (Bad Request)} if the fieldValue has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/field-values")
    public ResponseEntity<FieldValueDTO> createFieldValue(@Valid @RequestBody FieldValueDTO fieldValueDTO) throws URISyntaxException {
        log.debug("REST request to save FieldValue : {}", fieldValueDTO);
        if (fieldValueDTO.getId() != null) {
            throw new BadRequestAlertException("A new fieldValue cannot already have an ID", ENTITY_NAME, "idexists");
        }
        FieldValueDTO result = fieldValueService.save(fieldValueDTO);
        return ResponseEntity
            .created(new URI("/api/field-values/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * {@code PUT  /field-values/:id} : Updates an existing fieldValue.
     *
     * @param id the id of the fieldValueDTO to save.
     * @param fieldValueDTO the fieldValueDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated fieldValueDTO,
     * or with status {@code 400 (Bad Request)} if the fieldValueDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the fieldValueDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/field-values/{id}")
    public ResponseEntity<FieldValueDTO> updateFieldValue(
        @PathVariable(value = "id", required = false) final Long id,
        @Valid @RequestBody FieldValueDTO fieldValueDTO
    ) throws URISyntaxException {
        log.debug("REST request to update FieldValue : {}, {}", id, fieldValueDTO);
        if (fieldValueDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, fieldValueDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!fieldValueRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        FieldValueDTO result = fieldValueService.save(fieldValueDTO);
        return ResponseEntity
            .ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, fieldValueDTO.getId().toString()))
            .body(result);
    }

    /**
     * {@code PATCH  /field-values/:id} : Partial updates given fields of an existing fieldValue, field will ignore if it is null
     *
     * @param id the id of the fieldValueDTO to save.
     * @param fieldValueDTO the fieldValueDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated fieldValueDTO,
     * or with status {@code 400 (Bad Request)} if the fieldValueDTO is not valid,
     * or with status {@code 404 (Not Found)} if the fieldValueDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the fieldValueDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/field-values/{id}", consumes = "application/merge-patch+json")
    public ResponseEntity<FieldValueDTO> partialUpdateFieldValue(
        @PathVariable(value = "id", required = false) final Long id,
        @NotNull @RequestBody FieldValueDTO fieldValueDTO
    ) throws URISyntaxException {
        log.debug("REST request to partial update FieldValue partially : {}, {}", id, fieldValueDTO);
        if (fieldValueDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, fieldValueDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!fieldValueRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<FieldValueDTO> result = fieldValueService.partialUpdate(fieldValueDTO);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, fieldValueDTO.getId().toString())
        );
    }

    /**
     * GET  /field-values : get all the fieldValues.
     *
     * @param pageable the pagination information
     * @param criteria the criterias which the requested entities should match
     * @return the ResponseEntity with status 200 (OK) and the list of fieldValues in body
     */
    @GetMapping("/field-values/get")
    public ResponseEntity<List<FieldValueDTO>> getAllFieldValuesGet(FieldValueCriteria criteria, Pageable pageable, Boolean unPaged) {
        log.debug("REST request to get FieldValues by criteria: {}", criteria);
        Page<FieldValueDTO> page = fieldValueQueryService.findByCriteria(criteria, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    @GetMapping("/field-values/criteria")
    public ResponseEntity<List<FieldValueDTO>> getAllFieldValues(FieldValueCriteria criteria, Pageable pageable, String fieldName) {
        log.debug("REST request to get FieldValues by criteria: {}", criteria);
        log.debug("With field name :{}", fieldName);
        Field field = fieldRepository.findTop1ByName(fieldName).orElse(null);

        if (field != null) {
            LongFilter longFilter = new LongFilter();
            longFilter.setEquals(field.getId());
            criteria.setFieldId(longFilter);
        }
        Page<FieldValueDTO> page = fieldValueQueryService.findByCriteria(criteria, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    /**
     * {@code GET  /field-values} : get all the fieldValues.
     *
     * @param pageable the pagination information.
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of fieldValues in body.
     */
    @GetMapping("/field-values")
    public ResponseEntity<List<FieldValueDTO>> getAllFieldValues(FieldValueCriteria criteria, Pageable pageable, Boolean unPaged) {
        log.debug("REST request to get FieldValues by criteria: {}", criteria);
        Page<FieldValueDTO> page = fieldValueQueryService.findByCriteria(criteria, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /field-values/count} : count all the fieldValues.
     *
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the count in body.
     */
    @GetMapping("/field-values/count")
    public ResponseEntity<Long> countFieldValues(FieldValueCriteria criteria) {
        log.debug("REST request to count FieldValues by criteria: {}", criteria);
        return ResponseEntity.ok().body(fieldValueQueryService.countByCriteria(criteria));
    }

    /**
     * {@code GET  /field-values/:id} : get the "id" fieldValue.
     *
     * @param id the id of the fieldValueDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the fieldValueDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/field-values/{id}")
    public ResponseEntity<FieldValueDTO> getFieldValue(@PathVariable Long id) {
        log.debug("REST request to get FieldValue : {}", id);
        Optional<FieldValueDTO> fieldValueDTO = fieldValueService.findOne(id);
        return ResponseUtil.wrapOrNotFound(fieldValueDTO);
    }

    /**
     * {@code DELETE  /field-values/:id} : delete the "id" fieldValue.
     *
     * @param id the id of the fieldValueDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/field-values/{id}")
    public ResponseEntity<Void> deleteFieldValue(@PathVariable Long id) {
        log.debug("REST request to delete FieldValue : {}", id);
        fieldValueService.delete(id);
        return ResponseEntity
            .noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString()))
            .build();
    }

    @DeleteMapping("/field-values/cascaded/{id}")
    public ResponseEntity<Void> deleteFieldValueCascaded(@PathVariable Long id) {
        log.debug("REST request to delete FieldValue : {}", id);
        fieldValueService.deleteCascaded(id);
        return ResponseEntity
            .ok()
            .headers(uz.paykap.web.rest.util.HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, id.toString()))
            .build();
    }
}
