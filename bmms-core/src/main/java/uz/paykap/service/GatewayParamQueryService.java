package uz.paykap.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.jhipster.service.QueryService;
import uz.paykap.domain.GatewayParam;
import uz.paykap.domain.GatewayParam_;
import uz.paykap.domain.Recipient_;
import uz.paykap.repository.GatewayParamRepository;
import uz.paykap.service.criteria.GatewayParamCriteria;
import uz.paykap.service.dto.GatewayParamDTO;
import uz.paykap.service.mapper.GatewayParamMapper;

import javax.persistence.criteria.JoinType;
import java.util.List;

/**
 * Service for executing complex queries for {@link GatewayParam} entities in the database.
 * The main input is a {@link GatewayParamCriteria} which gets converted to {@link Specification},
 * in a way that all the filters must apply.
 * It returns a {@link List} of {@link GatewayParamDTO} or a {@link Page} of {@link GatewayParamDTO} which fulfills the criteria.
 */
@Service
@Transactional(readOnly = true)
public class GatewayParamQueryService extends QueryService<GatewayParam> {

    private final Logger log = LoggerFactory.getLogger(GatewayParamQueryService.class);

    private final GatewayParamRepository gatewayParamRepository;

    private final GatewayParamMapper gatewayParamMapper;

    public GatewayParamQueryService(GatewayParamRepository gatewayParamRepository, GatewayParamMapper gatewayParamMapper) {
        this.gatewayParamRepository = gatewayParamRepository;
        this.gatewayParamMapper = gatewayParamMapper;
    }

    /**
     * Return a {@link List} of {@link GatewayParamDTO} which matches the criteria from the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    public List<GatewayParamDTO> findByCriteria(GatewayParamCriteria criteria) {
        log.debug("find by criteria : {}", criteria);
        final Specification<GatewayParam> specification = createSpecification(criteria);
        return gatewayParamMapper.toDto(gatewayParamRepository.findAll(specification));
    }

    /**
     * Return a {@link Page} of {@link GatewayParamDTO} which matches the criteria from the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @param page The page, which should be returned.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    public Page<GatewayParamDTO> findByCriteria(GatewayParamCriteria criteria, Pageable page) {
        log.debug("find by criteria : {}, page: {}", criteria, page);
        final Specification<GatewayParam> specification = createSpecification(criteria);
        return gatewayParamRepository.findAll(specification, page).map(gatewayParamMapper::toDto);
    }

    /**
     * Return the number of matching entities in the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the number of matching entities.
     */
    @Transactional(readOnly = true)
    public long countByCriteria(GatewayParamCriteria criteria) {
        log.debug("count by criteria : {}", criteria);
        final Specification<GatewayParam> specification = createSpecification(criteria);
        return gatewayParamRepository.count(specification);
    }

    /**
     * Function to convert {@link GatewayParamCriteria} to a {@link Specification}
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching {@link Specification} of the entity.
     */
    protected Specification<GatewayParam> createSpecification(GatewayParamCriteria criteria) {
        Specification<GatewayParam> specification = Specification.where(null);
        if (criteria != null) {
            if (criteria.getId() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getId(), GatewayParam_.id));
            }
            if (criteria.getUrl() != null) {
                specification = specification.and(buildStringSpecification(criteria.getUrl(), GatewayParam_.url));
            }
            if (criteria.getType() != null) {
                specification = specification.and(buildSpecification(criteria.getType(), GatewayParam_.type));
            }
            if (criteria.getUsername() != null) {
                specification = specification.and(buildStringSpecification(criteria.getUsername(), GatewayParam_.username));
            }
            if (criteria.getPassword() != null) {
                specification = specification.and(buildStringSpecification(criteria.getPassword(), GatewayParam_.password));
            }
            if (criteria.getToken() != null) {
                specification = specification.and(buildStringSpecification(criteria.getToken(), GatewayParam_.token));
            }
            if (criteria.getBody() != null) {
                specification = specification.and(buildStringSpecification(criteria.getBody(), GatewayParam_.body));
            }
            if (criteria.getStatusQuery() != null) {
                specification = specification.and(buildStringSpecification(criteria.getStatusQuery(), GatewayParam_.statusQuery));
            }
            if (criteria.getMessageQuery() != null) {
                specification = specification.and(buildStringSpecification(criteria.getMessageQuery(), GatewayParam_.messageQuery));
            }
            if (criteria.getSuccessCode() != null) {
                specification = specification.and(buildStringSpecification(criteria.getSuccessCode(), GatewayParam_.successCode));
            }
            if (criteria.getCallback() != null) {
                specification = specification.and(buildStringSpecification(criteria.getCallback(), GatewayParam_.callback));
            }
            if (criteria.getTimeout() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getTimeout(), GatewayParam_.timeout));
            }
            if (criteria.getHeaders() != null) {
                specification = specification.and(buildStringSpecification(criteria.getHeaders(), GatewayParam_.headers));
            }
            if (criteria.getDataQueries() != null) {
                specification = specification.and(buildStringSpecification(criteria.getDataQueries(), GatewayParam_.dataQueries));
            }
            if (criteria.getHttpMethodType() != null) {
                specification = specification.and(buildSpecification(criteria.getHttpMethodType(), GatewayParam_.httpMethodType));
            }
            if (criteria.getProtocol() != null) {
                specification = specification.and(buildSpecification(criteria.getProtocol(), GatewayParam_.protocol));
            }
            if (criteria.getRequestMediaType() != null) {
                specification = specification.and(buildStringSpecification(criteria.getRequestMediaType(), GatewayParam_.requestMediaType));
            }
            if (criteria.getResponseMediaType() != null) {
                specification =
                    specification.and(buildStringSpecification(criteria.getResponseMediaType(), GatewayParam_.responseMediaType));
            }
            if (criteria.getActive() != null) {
                specification = specification.and(buildSpecification(criteria.getActive(), GatewayParam_.active));
            }
            if (criteria.getMode() != null) {
                specification = specification.and(buildSpecification(criteria.getMode(), GatewayParam_.mode));
            }
            if (criteria.getExtIdQuery() != null) {
                specification = specification.and(buildStringSpecification(criteria.getExtIdQuery(), GatewayParam_.extIdQuery));
            }
            if (criteria.getSuccessOn2xx() != null) {
                specification = specification.and(buildSpecification(criteria.getSuccessOn2xx(), GatewayParam_.successOn2xx));
            }
            if (criteria.getRecipientId() != null) {
                specification =
                    specification.and(
                        buildSpecification(
                            criteria.getRecipientId(),
                            root -> root.join(GatewayParam_.recipient, JoinType.LEFT).get(Recipient_.id)
                        )
                    );
            }
        }
        return specification;
    }
}
