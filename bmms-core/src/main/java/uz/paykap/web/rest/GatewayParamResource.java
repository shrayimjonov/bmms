package uz.paykap.web.rest;

import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import springfox.documentation.annotations.ApiIgnore;
import tech.jhipster.service.filter.LongFilter;
import tech.jhipster.web.util.HeaderUtil;
import tech.jhipster.web.util.PaginationUtil;
import tech.jhipster.web.util.ResponseUtil;
import uz.devops.bmcore.dto.request.CheckRequest;
import uz.devops.bmcore.enumeration.RequestType;
import uz.paykap.domain.Recipient;
import uz.paykap.gateway.common.CommonGatewayHelperService;
import uz.paykap.gateway.common.CommonGatewayServiceV3;
import uz.paykap.repository.GatewayParamRepository;
import uz.paykap.repository.RecipientRepository;
import uz.paykap.service.GatewayParamQueryService;
import uz.paykap.service.GatewayParamService;
import uz.paykap.service.criteria.GatewayParamCriteria;
import uz.paykap.service.dto.GatewayParamDTO;
import uz.paykap.service.dto.common.CommonHttpRequestWrapper;
import uz.paykap.web.rest.errors.BadRequestAlertException;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static uz.paykap.security.AuthoritiesConstants.ADMIN;

/**
 * REST controller for managing {@link uz.paykap.domain.GatewayParam}.
 */
@Slf4j
@ApiIgnore
@RequiredArgsConstructor
@RestController
@RequestMapping("/api")
public class GatewayParamResource {

    private static final String ENTITY_NAME = "pkbillmsGatewayParam";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final GatewayParamService gatewayParamService;
    private final CommonGatewayServiceV3 commonGatewayService;
    private final CommonGatewayHelperService commonGatewayHelperService;
    private final RecipientRepository recipientRepository;
    private final GatewayParamQueryService gatewayParamQueryService;
    private final GatewayParamRepository gatewayParamRepository;

    @PreAuthorize("hasAnyRole('" + ADMIN + "')")
    @ApiOperation("Проверка параметров шлюза")
    @PostMapping("/gateway-params/free/{requestType}")
    public ResponseEntity<LinkedHashMap<RequestType, CommonHttpRequestWrapper>> freeRequest(
        @Valid @RequestBody CheckRequest checkRequest,
        @PathVariable RequestType requestType
    ) {
        log.debug("REST request to free test : {}", checkRequest);
        return ResponseEntity.ok(commonGatewayHelperService.freeRequest(requestType, checkRequest));
    }

    /**
     * {@code POST  /gateway-params} : Create a new gatewayParam.
     *
     * @param gatewayParamDTO the gatewayParamDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new gatewayParamDTO, or with status {@code 400 (Bad Request)} if the gatewayParam has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/gateway-params")
    public ResponseEntity<GatewayParamDTO> createGatewayParam(@Valid @RequestBody GatewayParamDTO gatewayParamDTO)
        throws URISyntaxException {
        log.debug("REST request to save GatewayParam : {}", gatewayParamDTO);
        if (gatewayParamDTO.getId() != null) {
            throw new BadRequestAlertException("A new gatewayParam cannot already have an ID", ENTITY_NAME, "idexists");
        }
        GatewayParamDTO result = gatewayParamService.save(gatewayParamDTO);
        return ResponseEntity
            .created(new URI("/api/gateway-params/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * {@code PUT  /gateway-params/:id} : Updates an existing gatewayParam.
     *
     * @param id the id of the gatewayParamDTO to save.
     * @param gatewayParamDTO the gatewayParamDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated gatewayParamDTO,
     * or with status {@code 400 (Bad Request)} if the gatewayParamDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the gatewayParamDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/gateway-params/{id}")
    public ResponseEntity<GatewayParamDTO> updateGatewayParam(
        @PathVariable(value = "id", required = false) final Long id,
        @Valid @RequestBody GatewayParamDTO gatewayParamDTO
    ) throws URISyntaxException {
        log.debug("REST request to update GatewayParam : {}, {}", id, gatewayParamDTO);
        if (gatewayParamDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, gatewayParamDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!gatewayParamRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        GatewayParamDTO result = gatewayParamService.save(gatewayParamDTO);
        return ResponseEntity
            .ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, gatewayParamDTO.getId().toString()))
            .body(result);
    }

    /**
     * {@code PATCH  /gateway-params/:id} : Partial updates given fields of an existing gatewayParam, field will ignore if it is null
     *
     * @param id the id of the gatewayParamDTO to save.
     * @param gatewayParamDTO the gatewayParamDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated gatewayParamDTO,
     * or with status {@code 400 (Bad Request)} if the gatewayParamDTO is not valid,
     * or with status {@code 404 (Not Found)} if the gatewayParamDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the gatewayParamDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/gateway-params/{id}", consumes = "application/merge-patch+json")
    public ResponseEntity<GatewayParamDTO> partialUpdateGatewayParam(
        @PathVariable(value = "id", required = false) final Long id,
        @NotNull @RequestBody GatewayParamDTO gatewayParamDTO
    ) throws URISyntaxException {
        log.debug("REST request to partial update GatewayParam partially : {}, {}", id, gatewayParamDTO);
        if (gatewayParamDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, gatewayParamDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!gatewayParamRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<GatewayParamDTO> result = gatewayParamService.partialUpdate(gatewayParamDTO);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, gatewayParamDTO.getId().toString())
        );
    }

    /**
     * {@code GET  /gateway-params} : get all the gatewayParams.
     *
     * @param pageable the pagination information.
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of gatewayParams in body.
     */
    @GetMapping("/gateway-params")
    public ResponseEntity<List<GatewayParamDTO>> getAllGatewayParams(GatewayParamCriteria criteria, Pageable pageable, Boolean unPaged) {
        log.debug("REST request to get GatewayParams by criteria: {}", criteria);
        Page<GatewayParamDTO> page = gatewayParamQueryService.findByCriteria(criteria, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    @GetMapping("/gateway-params/criteria")
    public ResponseEntity<List<GatewayParamDTO>> getAllGatewayParams(
        GatewayParamCriteria criteria,
        Pageable pageable,
        String recipientShortName
    ) {
        log.debug("REST request to get GatewayParams by criteria: {}", criteria);
        log.debug("With recipient short name : {}", recipientShortName);
        Recipient recipient = recipientRepository.findTop1ByShortName(recipientShortName).orElse(null);
        if (recipient != null) {
            LongFilter longFilter = new LongFilter();
            longFilter.setEquals(recipient.getId());
            criteria.setRecipientId(longFilter);
        }
        Page<GatewayParamDTO> page = gatewayParamQueryService.findByCriteria(criteria, pageable);
        HttpHeaders headers = uz.paykap.web.rest.util.PaginationUtil.generatePaginationHttpHeaders(page, "/api/gateway-params");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    /**
     * {@code GET  /gateway-params/count} : count all the gatewayParams.
     *
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the count in body.
     */
    @GetMapping("/gateway-params/count")
    public ResponseEntity<Long> countGatewayParams(GatewayParamCriteria criteria) {
        log.debug("REST request to count GatewayParams by criteria: {}", criteria);
        return ResponseEntity.ok().body(gatewayParamQueryService.countByCriteria(criteria));
    }

    /**
     * {@code GET  /gateway-params/:id} : get the "id" gatewayParam.
     *
     * @param id the id of the gatewayParamDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the gatewayParamDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/gateway-params/{id}")
    public ResponseEntity<GatewayParamDTO> getGatewayParam(@PathVariable Long id) {
        log.debug("REST request to get GatewayParam : {}", id);
        Optional<GatewayParamDTO> gatewayParamDTO = gatewayParamService.findOne(id);
        return ResponseUtil.wrapOrNotFound(gatewayParamDTO);
    }

    /**
     * {@code DELETE  /gateway-params/:id} : delete the "id" gatewayParam.
     *
     * @param id the id of the gatewayParamDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/gateway-params/{id}")
    public ResponseEntity<Void> deleteGatewayParam(@PathVariable Long id) {
        log.debug("REST request to delete GatewayParam : {}", id);
        gatewayParamService.delete(id);
        return ResponseEntity
            .noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString()))
            .build();
    }

    @PostMapping("/gateway-params/save_list")
    public ResponseEntity<String> saveListGatewayParams(@Valid @RequestBody List<GatewayParamDTO> gatewayParamList) {
        if (gatewayParamList == null) {
            return ResponseEntity.badRequest().body("ERROR!");
        }

        log.debug("REST request to save GatewayParamList : {}", gatewayParamList);

        for (GatewayParamDTO gatewayParamDTO : gatewayParamList) {
            if (gatewayParamDTO != null) {
                gatewayParamService.save(gatewayParamDTO);
            }
        }
        return ResponseEntity.ok("SUCCESS!");
    }

    /**
     * GET  /gateway-params/byRecipients/:id : get the "id" recipient.
     *
     * @param id the id of the recipientDTO to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the List<gatewayParamDTO>, or with status 404 (Not Found)
     */
    @GetMapping("/gateway-params/byRecipient/{id}")
    public ResponseEntity<List<GatewayParamDTO>> getGatewayParamsByRecipientId(@PathVariable Long id) {
        log.debug("REST request to get GatewayParam list by Recipient ID: {}", id);
        return ResponseEntity.ok(gatewayParamService.findAllByRecipientId(id));
    }
}
