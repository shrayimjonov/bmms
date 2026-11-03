package uz.paykap.web.rest;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.javers.core.Javers;
import org.javers.core.metamodel.object.CdoSnapshot;
import org.javers.repository.jql.QueryBuilder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;
import springfox.documentation.annotations.ApiIgnore;
import uz.paykap.domain.EntityAuditEvent;
import uz.paykap.security.AuthoritiesConstants;
import uz.paykap.web.rest.util.PaginationUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * REST controller for getting the audit events for entity
 */
@Slf4j
@RestController
@AllArgsConstructor
@ApiIgnore
@RequestMapping("/api")
public class JaversEntityAuditResource {

    public static final String DOMAIN_PACKAGE = "uz.paykap.domain";
    private final Javers javers;

    /**
     * fetches all the audited entity types
     *
     * @return
     */
    @RequestMapping(value = "/audits/entity/all", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole(\"" + AuthoritiesConstants.ADMIN + "\")")
    public List<String> getAuditedEntities() {
        return Arrays.asList(
                "I18n", "Recipient", "Agent", "AgentRecipient", "Category", "Prefix", "Field", "FieldValue", "Gateway", "VirtualAccount", "VirtualAccountDetails", "GatewayParam", "Bill", "Transaction", "DepositTransaction", "Deposit", "RecipientFileSchema", "BookkeepingTemplate", "BookkeepingTransaction", "DataAdapterHistory", "ForeignPay", "ReconcileSummary", "ReconcileDiscrepancy", "Setting", "SettingProperty", "AgentParams", "CheckVerifierParam", "GroupSetting", "SettingType", "RecipientGroup", "Sender", "P2pCondition", "P2pCommission", "P2pParam", "Currency", "ResponseField"
        );
    }

    /**
     * fetches the last 100 change list for an entity class, if limit is passed fetches that many changes
     *
     * @return
     */
    @RequestMapping(value = "/audits/entity/changes", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole(\"" + AuthoritiesConstants.ADMIN + "\")")
    public ResponseEntity<List<EntityAuditEvent>> getChanges(
            @RequestParam(value = "entityType") String entityType,
            @RequestParam(value = "limit") int limit,
            @RequestParam MultiValueMap<String, String> queryParams,
            UriComponentsBuilder uriBuilder
    ) throws ClassNotFoundException {
        log.debug("REST request to get a page of EntityAuditEvents");

        Class entityTypeToFetch = Class.forName(DOMAIN_PACKAGE + "." + entityType);
        QueryBuilder jqlQuery = QueryBuilder.byClass(entityTypeToFetch).limit(limit).withNewObjectChanges(true);

        List<CdoSnapshot> snapshots = javers.findSnapshots(jqlQuery.build());

        List<EntityAuditEvent> auditEvents = new ArrayList<>();

        snapshots.forEach(
                snapshot -> {
                    EntityAuditEvent event = EntityAuditEvent.fromJaversSnapshot(snapshot);
                    event.setEntityType(entityType);
                    auditEvents.add(event);
                }
        );

        Page<EntityAuditEvent> page = new PageImpl<>(auditEvents);

        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, uriBuilder.queryParams(queryParams).toUriString());

        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    /**
     * fetches a previous version for for an entity class and id
     *
     * @return
     */
    @RequestMapping(
            value = "/audits/entity/changes/version/previous",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @PreAuthorize("hasRole(\"" + AuthoritiesConstants.ADMIN + "\")")
    public ResponseEntity<EntityAuditEvent> getPrevVersion(
            @RequestParam(value = "qualifiedName") String qualifiedName,
            @RequestParam(value = "entityId") String entityId,
            @RequestParam(value = "commitVersion") Long commitVersion
    ) throws ClassNotFoundException {

        Class entityTypeToFetch = Class.forName(DOMAIN_PACKAGE + "." + qualifiedName);

        QueryBuilder jqlQuery = QueryBuilder
                .byInstanceId(entityId, entityTypeToFetch)
                .limit(1)
                .withVersion(commitVersion - 1)
                .withNewObjectChanges(true);

        List<CdoSnapshot> snapshots = javers.findSnapshots(jqlQuery.build());

        if (snapshots == null || snapshots.isEmpty()) {
            return new ResponseEntity<>(new EntityAuditEvent(), HttpStatus.OK);
        }

        EntityAuditEvent prev = EntityAuditEvent.fromJaversSnapshot(snapshots.get(0));

        return new ResponseEntity<>(prev, HttpStatus.OK);
    }

    @RequestMapping(value = "/audits/entity/history",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole(\"" + AuthoritiesConstants.ADMIN + "\")")
    public ResponseEntity<String> getHistoryByEntityId(@RequestParam(value = "qualifiedName") String qualifiedName,
                                                       @RequestParam(value = "entityId") Long entityId) throws ClassNotFoundException {

        Class<?> entityTypeToFetch = Class.forName(DOMAIN_PACKAGE + "." + qualifiedName);

        QueryBuilder jqlQuery = QueryBuilder.byInstanceId(entityId, entityTypeToFetch).withNewObjectChanges(true);

        List<CdoSnapshot> snapshots = javers.findSnapshots(jqlQuery.build());

        return new ResponseEntity<>(javers.getJsonConverter().toJson(snapshots), HttpStatus.OK);
    }


}
