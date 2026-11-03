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
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import springfox.documentation.annotations.ApiIgnore;
import tech.jhipster.web.util.HeaderUtil;
import tech.jhipster.web.util.PaginationUtil;
import tech.jhipster.web.util.ResponseUtil;
import uz.paykap.repository.SettingPropertyRepository;
import uz.paykap.service.SettingPropertyService;
import uz.paykap.service.dto.SettingPropertyDTO;
import uz.paykap.web.rest.errors.BadRequestAlertException;

/**
 * REST controller for managing {@link uz.paykap.domain.SettingProperty}.
 */
@ApiIgnore
@RestController
@RequestMapping("/api")
public class SettingPropertyResource {

    private final Logger log = LoggerFactory.getLogger(SettingPropertyResource.class);

    private static final String ENTITY_NAME = "pkbillmsSettingProperty";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final SettingPropertyService settingPropertyService;

    private final SettingPropertyRepository settingPropertyRepository;

    public SettingPropertyResource(SettingPropertyService settingPropertyService, SettingPropertyRepository settingPropertyRepository) {
        this.settingPropertyService = settingPropertyService;
        this.settingPropertyRepository = settingPropertyRepository;
    }

    /**
     * {@code POST  /setting-properties} : Create a new settingProperty.
     *
     * @param settingPropertyDTO the settingPropertyDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new settingPropertyDTO, or with status {@code 400 (Bad Request)} if the settingProperty has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/setting-properties")
    public ResponseEntity<SettingPropertyDTO> createSettingProperty(@Valid @RequestBody SettingPropertyDTO settingPropertyDTO)
        throws URISyntaxException {
        log.debug("REST request to save SettingProperty : {}", settingPropertyDTO);
        if (settingPropertyDTO.getId() != null) {
            throw new BadRequestAlertException("A new settingProperty cannot already have an ID", ENTITY_NAME, "idexists");
        }
        SettingPropertyDTO result = settingPropertyService.save(settingPropertyDTO);
        return ResponseEntity
            .created(new URI("/api/setting-properties/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * {@code PUT  /setting-properties/:id} : Updates an existing settingProperty.
     *
     * @param id the id of the settingPropertyDTO to save.
     * @param settingPropertyDTO the settingPropertyDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated settingPropertyDTO,
     * or with status {@code 400 (Bad Request)} if the settingPropertyDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the settingPropertyDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/setting-properties/{id}")
    public ResponseEntity<SettingPropertyDTO> updateSettingProperty(
        @PathVariable(value = "id", required = false) final Long id,
        @Valid @RequestBody SettingPropertyDTO settingPropertyDTO
    ) throws URISyntaxException {
        log.debug("REST request to update SettingProperty : {}, {}", id, settingPropertyDTO);
        if (settingPropertyDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, settingPropertyDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!settingPropertyRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        SettingPropertyDTO result = settingPropertyService.save(settingPropertyDTO);
        return ResponseEntity
            .ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, settingPropertyDTO.getId().toString()))
            .body(result);
    }

    /**
     * {@code PATCH  /setting-properties/:id} : Partial updates given fields of an existing settingProperty, field will ignore if it is null
     *
     * @param id the id of the settingPropertyDTO to save.
     * @param settingPropertyDTO the settingPropertyDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated settingPropertyDTO,
     * or with status {@code 400 (Bad Request)} if the settingPropertyDTO is not valid,
     * or with status {@code 404 (Not Found)} if the settingPropertyDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the settingPropertyDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/setting-properties/{id}", consumes = "application/merge-patch+json")
    public ResponseEntity<SettingPropertyDTO> partialUpdateSettingProperty(
        @PathVariable(value = "id", required = false) final Long id,
        @NotNull @RequestBody SettingPropertyDTO settingPropertyDTO
    ) throws URISyntaxException {
        log.debug("REST request to partial update SettingProperty partially : {}, {}", id, settingPropertyDTO);
        if (settingPropertyDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, settingPropertyDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!settingPropertyRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<SettingPropertyDTO> result = settingPropertyService.partialUpdate(settingPropertyDTO);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, settingPropertyDTO.getId().toString())
        );
    }

    /**
     * {@code GET  /setting-properties} : get all the settingProperties.
     *
     * @param pageable the pagination information.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of settingProperties in body.
     */
    @GetMapping("/setting-properties")
    public ResponseEntity<List<SettingPropertyDTO>> getAllSettingProperties(Pageable pageable) {
        log.debug("REST request to get a page of SettingProperties");
        Page<SettingPropertyDTO> page = settingPropertyService.findAll(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /setting-properties/:id} : get the "id" settingProperty.
     *
     * @param id the id of the settingPropertyDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the settingPropertyDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/setting-properties/{id}")
    public ResponseEntity<SettingPropertyDTO> getSettingProperty(@PathVariable Long id) {
        log.debug("REST request to get SettingProperty : {}", id);
        Optional<SettingPropertyDTO> settingPropertyDTO = settingPropertyService.findOne(id);
        return ResponseUtil.wrapOrNotFound(settingPropertyDTO);
    }

    /**
     * {@code DELETE  /setting-properties/:id} : delete the "id" settingProperty.
     *
     * @param id the id of the settingPropertyDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/setting-properties/{id}")
    public ResponseEntity<Void> deleteSettingProperty(@PathVariable Long id) {
        log.debug("REST request to delete SettingProperty : {}", id);
        settingPropertyService.delete(id);
        return ResponseEntity
            .noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString()))
            .build();
    }
}
