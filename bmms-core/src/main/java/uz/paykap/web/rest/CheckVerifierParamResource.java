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
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import tech.jhipster.web.util.HeaderUtil;
import tech.jhipster.web.util.ResponseUtil;
import uz.paykap.domain.CheckVerifierParam;
import uz.paykap.repository.CheckVerifierParamRepository;
import uz.paykap.web.rest.errors.BadRequestAlertException;

/**
 * REST controller for managing {@link uz.paykap.domain.CheckVerifierParam}.
 */
@RestController
@RequestMapping("/api")
@Transactional
public class CheckVerifierParamResource {

    private final Logger log = LoggerFactory.getLogger(CheckVerifierParamResource.class);

    private static final String ENTITY_NAME = "pkbillmsCheckVerifierParam";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final CheckVerifierParamRepository checkVerifierParamRepository;

    public CheckVerifierParamResource(CheckVerifierParamRepository checkVerifierParamRepository) {
        this.checkVerifierParamRepository = checkVerifierParamRepository;
    }

    /**
     * {@code POST  /check-verifier-params} : Create a new checkVerifierParam.
     *
     * @param checkVerifierParam the checkVerifierParam to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new checkVerifierParam, or with status {@code 400 (Bad Request)} if the checkVerifierParam has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/check-verifier-params")
    public ResponseEntity<CheckVerifierParam> createCheckVerifierParam(@Valid @RequestBody CheckVerifierParam checkVerifierParam)
        throws URISyntaxException {
        log.debug("REST request to save CheckVerifierParam : {}", checkVerifierParam);
        if (checkVerifierParam.getId() != null) {
            throw new BadRequestAlertException("A new checkVerifierParam cannot already have an ID", ENTITY_NAME, "idexists");
        }
        CheckVerifierParam result = checkVerifierParamRepository.save(checkVerifierParam);
        return ResponseEntity
            .created(new URI("/api/check-verifier-params/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * {@code PUT  /check-verifier-params/:id} : Updates an existing checkVerifierParam.
     *
     * @param id the id of the checkVerifierParam to save.
     * @param checkVerifierParam the checkVerifierParam to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated checkVerifierParam,
     * or with status {@code 400 (Bad Request)} if the checkVerifierParam is not valid,
     * or with status {@code 500 (Internal Server Error)} if the checkVerifierParam couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/check-verifier-params/{id}")
    public ResponseEntity<CheckVerifierParam> updateCheckVerifierParam(
        @PathVariable(value = "id", required = false) final Long id,
        @Valid @RequestBody CheckVerifierParam checkVerifierParam
    ) throws URISyntaxException {
        log.debug("REST request to update CheckVerifierParam : {}, {}", id, checkVerifierParam);
        if (checkVerifierParam.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, checkVerifierParam.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!checkVerifierParamRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        CheckVerifierParam result = checkVerifierParamRepository.save(checkVerifierParam);
        return ResponseEntity
            .ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, checkVerifierParam.getId().toString()))
            .body(result);
    }

    /**
     * {@code PATCH  /check-verifier-params/:id} : Partial updates given fields of an existing checkVerifierParam, field will ignore if it is null
     *
     * @param id the id of the checkVerifierParam to save.
     * @param checkVerifierParam the checkVerifierParam to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated checkVerifierParam,
     * or with status {@code 400 (Bad Request)} if the checkVerifierParam is not valid,
     * or with status {@code 404 (Not Found)} if the checkVerifierParam is not found,
     * or with status {@code 500 (Internal Server Error)} if the checkVerifierParam couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/check-verifier-params/{id}", consumes = "application/merge-patch+json")
    public ResponseEntity<CheckVerifierParam> partialUpdateCheckVerifierParam(
        @PathVariable(value = "id", required = false) final Long id,
        @NotNull @RequestBody CheckVerifierParam checkVerifierParam
    ) throws URISyntaxException {
        log.debug("REST request to partial update CheckVerifierParam partially : {}, {}", id, checkVerifierParam);
        if (checkVerifierParam.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, checkVerifierParam.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!checkVerifierParamRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<CheckVerifierParam> result = checkVerifierParamRepository
            .findById(checkVerifierParam.getId())
            .map(
                existingCheckVerifierParam -> {
                    if (checkVerifierParam.getAccount() != null) {
                        existingCheckVerifierParam.setAccount(checkVerifierParam.getAccount());
                    }
                    if (checkVerifierParam.getAmountInTiyin() != null) {
                        existingCheckVerifierParam.setAmountInTiyin(checkVerifierParam.getAmountInTiyin());
                    }

                    return existingCheckVerifierParam;
                }
            )
            .map(checkVerifierParamRepository::save);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, checkVerifierParam.getId().toString())
        );
    }

    /**
     * {@code GET  /check-verifier-params} : get all the checkVerifierParams.
     *
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of checkVerifierParams in body.
     */
    @GetMapping("/check-verifier-params")
    public List<CheckVerifierParam> getAllCheckVerifierParams() {
        log.debug("REST request to get all CheckVerifierParams");
        return checkVerifierParamRepository.findAll();
    }

    /**
     * {@code GET  /check-verifier-params/:id} : get the "id" checkVerifierParam.
     *
     * @param id the id of the checkVerifierParam to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the checkVerifierParam, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/check-verifier-params/{id}")
    public ResponseEntity<CheckVerifierParam> getCheckVerifierParam(@PathVariable Long id) {
        log.debug("REST request to get CheckVerifierParam : {}", id);
        Optional<CheckVerifierParam> checkVerifierParam = checkVerifierParamRepository.findById(id);
        return ResponseUtil.wrapOrNotFound(checkVerifierParam);
    }

    /**
     * {@code DELETE  /check-verifier-params/:id} : delete the "id" checkVerifierParam.
     *
     * @param id the id of the checkVerifierParam to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/check-verifier-params/{id}")
    public ResponseEntity<Void> deleteCheckVerifierParam(@PathVariable Long id) {
        log.debug("REST request to delete CheckVerifierParam : {}", id);
        checkVerifierParamRepository.deleteById(id);
        return ResponseEntity
            .noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString()))
            .build();
    }
}
