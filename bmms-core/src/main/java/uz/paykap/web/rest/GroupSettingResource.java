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
import uz.paykap.repository.GroupSettingRepository;
import uz.paykap.service.GroupSettingQueryService;
import uz.paykap.service.GroupSettingService;
import uz.paykap.service.criteria.GroupSettingCriteria;
import uz.paykap.service.dto.GroupSettingDTO;
import uz.paykap.web.rest.errors.BadRequestAlertException;

/**
 * REST controller for managing {@link uz.paykap.domain.GroupSetting}.
 */
@ApiIgnore
@RestController
@RequestMapping("/api")
public class GroupSettingResource {

    private final Logger log = LoggerFactory.getLogger(GroupSettingResource.class);

    private static final String ENTITY_NAME = "pkbillmsGroupSetting";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final GroupSettingService groupSettingService;

    private final GroupSettingRepository groupSettingRepository;

    private final GroupSettingQueryService groupSettingQueryService;

    public GroupSettingResource(
        GroupSettingService groupSettingService,
        GroupSettingRepository groupSettingRepository,
        GroupSettingQueryService groupSettingQueryService
    ) {
        this.groupSettingService = groupSettingService;
        this.groupSettingRepository = groupSettingRepository;
        this.groupSettingQueryService = groupSettingQueryService;
    }

    /**
     * {@code POST  /group-settings} : Create a new groupSetting.
     *
     * @param groupSettingDTO the groupSettingDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new groupSettingDTO, or with status {@code 400 (Bad Request)} if the groupSetting has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/group-settings")
    public ResponseEntity<GroupSettingDTO> createGroupSetting(@Valid @RequestBody GroupSettingDTO groupSettingDTO)
        throws URISyntaxException {
        log.debug("REST request to save GroupSetting : {}", groupSettingDTO);
        if (groupSettingDTO.getId() != null) {
            throw new BadRequestAlertException("A new groupSetting cannot already have an ID", ENTITY_NAME, "idexists");
        }
        GroupSettingDTO result = groupSettingService.save(groupSettingDTO);
        return ResponseEntity
            .created(new URI("/api/group-settings/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * {@code PUT  /group-settings/:id} : Updates an existing groupSetting.
     *
     * @param id the id of the groupSettingDTO to save.
     * @param groupSettingDTO the groupSettingDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated groupSettingDTO,
     * or with status {@code 400 (Bad Request)} if the groupSettingDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the groupSettingDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/group-settings/{id}")
    public ResponseEntity<GroupSettingDTO> updateGroupSetting(
        @PathVariable(value = "id", required = false) final Long id,
        @Valid @RequestBody GroupSettingDTO groupSettingDTO
    ) throws URISyntaxException {
        log.debug("REST request to update GroupSetting : {}, {}", id, groupSettingDTO);
        if (groupSettingDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, groupSettingDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!groupSettingRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        GroupSettingDTO result = groupSettingService.save(groupSettingDTO);
        return ResponseEntity
            .ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, groupSettingDTO.getId().toString()))
            .body(result);
    }

    /**
     * {@code PATCH  /group-settings/:id} : Partial updates given fields of an existing groupSetting, field will ignore if it is null
     *
     * @param id the id of the groupSettingDTO to save.
     * @param groupSettingDTO the groupSettingDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated groupSettingDTO,
     * or with status {@code 400 (Bad Request)} if the groupSettingDTO is not valid,
     * or with status {@code 404 (Not Found)} if the groupSettingDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the groupSettingDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/group-settings/{id}", consumes = "application/merge-patch+json")
    public ResponseEntity<GroupSettingDTO> partialUpdateGroupSetting(
        @PathVariable(value = "id", required = false) final Long id,
        @NotNull @RequestBody GroupSettingDTO groupSettingDTO
    ) throws URISyntaxException {
        log.debug("REST request to partial update GroupSetting partially : {}, {}", id, groupSettingDTO);
        if (groupSettingDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, groupSettingDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!groupSettingRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<GroupSettingDTO> result = groupSettingService.partialUpdate(groupSettingDTO);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, groupSettingDTO.getId().toString())
        );
    }

    /**
     * {@code GET  /group-settings} : get all the groupSettings.
     *
     * @param pageable the pagination information.
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of groupSettings in body.
     */
    @GetMapping("/group-settings")
    public ResponseEntity<List<GroupSettingDTO>> getAllGroupSettings(GroupSettingCriteria criteria, Pageable pageable) {
        log.debug("REST request to get GroupSettings by criteria: {}", criteria);
        Page<GroupSettingDTO> page = groupSettingQueryService.findByCriteria(criteria, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /group-settings/count} : count all the groupSettings.
     *
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the count in body.
     */
    @GetMapping("/group-settings/count")
    public ResponseEntity<Long> countGroupSettings(GroupSettingCriteria criteria) {
        log.debug("REST request to count GroupSettings by criteria: {}", criteria);
        return ResponseEntity.ok().body(groupSettingQueryService.countByCriteria(criteria));
    }

    /**
     * {@code GET  /group-settings/:id} : get the "id" groupSetting.
     *
     * @param id the id of the groupSettingDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the groupSettingDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/group-settings/{id}")
    public ResponseEntity<GroupSettingDTO> getGroupSetting(@PathVariable Long id) {
        log.debug("REST request to get GroupSetting : {}", id);
        Optional<GroupSettingDTO> groupSettingDTO = groupSettingService.findOne(id);
        return ResponseUtil.wrapOrNotFound(groupSettingDTO);
    }

    /**
     * {@code DELETE  /group-settings/:id} : delete the "id" groupSetting.
     *
     * @param id the id of the groupSettingDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/group-settings/{id}")
    public ResponseEntity<Void> deleteGroupSetting(@PathVariable Long id) {
        log.debug("REST request to delete GroupSetting : {}", id);
        groupSettingService.delete(id);
        return ResponseEntity
            .noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString()))
            .build();
    }
    //    @PostMapping("/group-settings/list")
    //    @Timed
    //    public void createGroupSettingList(@Valid @RequestBody List<GroupSettingDTO> groupSettingDTO) throws URISyntaxException {
    //        if (groupSettingDTO == null || groupSettingDTO.size() == 0) {
    //            throw new BadRequestAlertException("Invalid list", ENTITY_NAME, "listnull");
    //        }
    //        log.debug("REST request to delete GroupSetting list : {}", groupSettingDTO.toString());
    //        groupSettingService.save(groupSettingDTO);
    //    }

}
