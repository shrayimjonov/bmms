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
import uz.paykap.domain.DataAdapterHistory;
import uz.paykap.repository.DataAdapterHistoryRepository;
import uz.paykap.service.DataAdapterHistoryService;
import uz.paykap.web.rest.errors.BadRequestAlertException;

/**
 * REST controller for managing {@link uz.paykap.domain.DataAdapterHistory}.
 */
@ApiIgnore
@RestController
@RequestMapping("/api")
public class DataAdapterHistoryResource {

    private final Logger log = LoggerFactory.getLogger(DataAdapterHistoryResource.class);

    private static final String ENTITY_NAME = "pkbillmsDataAdapterHistory";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final DataAdapterHistoryService dataAdapterHistoryService;

    private final DataAdapterHistoryRepository dataAdapterHistoryRepository;

    public DataAdapterHistoryResource(
        DataAdapterHistoryService dataAdapterHistoryService,
        DataAdapterHistoryRepository dataAdapterHistoryRepository
    ) {
        this.dataAdapterHistoryService = dataAdapterHistoryService;
        this.dataAdapterHistoryRepository = dataAdapterHistoryRepository;
    }

    /**
     * {@code POST  /data-adapter-histories} : Create a new dataAdapterHistory.
     *
     * @param dataAdapterHistory the dataAdapterHistory to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new dataAdapterHistory, or with status {@code 400 (Bad Request)} if the dataAdapterHistory has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/data-adapter-histories")
    public ResponseEntity<DataAdapterHistory> createDataAdapterHistory(@Valid @RequestBody DataAdapterHistory dataAdapterHistory)
        throws URISyntaxException {
        log.debug("REST request to save DataAdapterHistory : {}", dataAdapterHistory);
        if (dataAdapterHistory.getId() != null) {
            throw new BadRequestAlertException("A new dataAdapterHistory cannot already have an ID", ENTITY_NAME, "idexists");
        }
        DataAdapterHistory result = dataAdapterHistoryService.save(dataAdapterHistory);
        return ResponseEntity
            .created(new URI("/api/data-adapter-histories/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * {@code PUT  /data-adapter-histories/:id} : Updates an existing dataAdapterHistory.
     *
     * @param id the id of the dataAdapterHistory to save.
     * @param dataAdapterHistory the dataAdapterHistory to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated dataAdapterHistory,
     * or with status {@code 400 (Bad Request)} if the dataAdapterHistory is not valid,
     * or with status {@code 500 (Internal Server Error)} if the dataAdapterHistory couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/data-adapter-histories/{id}")
    public ResponseEntity<DataAdapterHistory> updateDataAdapterHistory(
        @PathVariable(value = "id", required = false) final Long id,
        @Valid @RequestBody DataAdapterHistory dataAdapterHistory
    ) throws URISyntaxException {
        log.debug("REST request to update DataAdapterHistory : {}, {}", id, dataAdapterHistory);
        if (dataAdapterHistory.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, dataAdapterHistory.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!dataAdapterHistoryRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        DataAdapterHistory result = dataAdapterHistoryService.save(dataAdapterHistory);
        return ResponseEntity
            .ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, dataAdapterHistory.getId().toString()))
            .body(result);
    }

    /**
     * {@code PATCH  /data-adapter-histories/:id} : Partial updates given fields of an existing dataAdapterHistory, field will ignore if it is null
     *
     * @param id the id of the dataAdapterHistory to save.
     * @param dataAdapterHistory the dataAdapterHistory to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated dataAdapterHistory,
     * or with status {@code 400 (Bad Request)} if the dataAdapterHistory is not valid,
     * or with status {@code 404 (Not Found)} if the dataAdapterHistory is not found,
     * or with status {@code 500 (Internal Server Error)} if the dataAdapterHistory couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/data-adapter-histories/{id}", consumes = "application/merge-patch+json")
    public ResponseEntity<DataAdapterHistory> partialUpdateDataAdapterHistory(
        @PathVariable(value = "id", required = false) final Long id,
        @NotNull @RequestBody DataAdapterHistory dataAdapterHistory
    ) throws URISyntaxException {
        log.debug("REST request to partial update DataAdapterHistory partially : {}, {}", id, dataAdapterHistory);
        if (dataAdapterHistory.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, dataAdapterHistory.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!dataAdapterHistoryRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<DataAdapterHistory> result = dataAdapterHistoryService.partialUpdate(dataAdapterHistory);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, dataAdapterHistory.getId().toString())
        );
    }

    /**
     * {@code GET  /data-adapter-histories} : get all the dataAdapterHistories.
     *
     * @param pageable the pagination information.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of dataAdapterHistories in body.
     */
    @GetMapping("/data-adapter-histories")
    public ResponseEntity<List<DataAdapterHistory>> getAllDataAdapterHistories(Pageable pageable) {
        log.debug("REST request to get a page of DataAdapterHistories");
        Page<DataAdapterHistory> page = dataAdapterHistoryService.findAll(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /data-adapter-histories/:id} : get the "id" dataAdapterHistory.
     *
     * @param id the id of the dataAdapterHistory to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the dataAdapterHistory, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/data-adapter-histories/{id}")
    public ResponseEntity<DataAdapterHistory> getDataAdapterHistory(@PathVariable Long id) {
        log.debug("REST request to get DataAdapterHistory : {}", id);
        Optional<DataAdapterHistory> dataAdapterHistory = dataAdapterHistoryService.findOne(id);
        return ResponseUtil.wrapOrNotFound(dataAdapterHistory);
    }

    /**
     * {@code DELETE  /data-adapter-histories/:id} : delete the "id" dataAdapterHistory.
     *
     * @param id the id of the dataAdapterHistory to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/data-adapter-histories/{id}")
    public ResponseEntity<Void> deleteDataAdapterHistory(@PathVariable Long id) {
        log.debug("REST request to delete DataAdapterHistory : {}", id);
        dataAdapterHistoryService.delete(id);
        return ResponseEntity
            .noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString()))
            .build();
    }

    /**
     * GET  /data-adapter-histories/byName/:name : get the "name" dataAdapterHistory.
     *
     * @param name the name of the dataAdapterHistory to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the dataAdapterHistory, or with status 404 (Not Found)
     */
    @GetMapping("/data-adapter-histories/byName/{name}")
    public ResponseEntity<DataAdapterHistory> getDataAdapterHistoryByName(@PathVariable String name) {
        log.debug("REST request to get DataAdapterHistory : {}", name);
        DataAdapterHistory dataAdapterHistory = dataAdapterHistoryService.findOneByName(name);
        return ResponseEntity.ok(dataAdapterHistory);
    }
}
