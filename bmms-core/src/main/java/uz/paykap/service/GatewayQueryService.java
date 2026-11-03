package uz.paykap.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.jhipster.service.QueryService;
import uz.paykap.domain.Deposit_;
import uz.paykap.domain.Gateway;
import uz.paykap.domain.Gateway_;
import uz.paykap.repository.GatewayRepository;
import uz.paykap.service.criteria.GatewayCriteria;
import uz.paykap.service.dto.GatewayDTO;
import uz.paykap.service.mapper.GatewayMapper;

import javax.persistence.criteria.JoinType;
import java.util.List;

/**
 * Service for executing complex queries for {@link Gateway} entities in the database.
 * The main input is a {@link GatewayCriteria} which gets converted to {@link Specification},
 * in a way that all the filters must apply.
 * It returns a {@link List} of {@link GatewayDTO} or a {@link Page} of {@link GatewayDTO} which fulfills the criteria.
 */
@Service
@Transactional(readOnly = true)
public class GatewayQueryService extends QueryService<Gateway> {

    private final Logger log = LoggerFactory.getLogger(GatewayQueryService.class);

    private final GatewayRepository gatewayRepository;

    private final GatewayMapper gatewayMapper;

    public GatewayQueryService(GatewayRepository gatewayRepository, GatewayMapper gatewayMapper) {
        this.gatewayRepository = gatewayRepository;
        this.gatewayMapper = gatewayMapper;
    }

    /**
     * Return a {@link List} of {@link GatewayDTO} which matches the criteria from the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    public List<GatewayDTO> findByCriteria(GatewayCriteria criteria) {
        log.debug("find by criteria : {}", criteria);
        final Specification<Gateway> specification = createSpecification(criteria);
        return gatewayMapper.toDto(gatewayRepository.findAll(specification));
    }

    /**
     * Return a {@link Page} of {@link GatewayDTO} which matches the criteria from the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @param page The page, which should be returned.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    public Page<GatewayDTO> findByCriteria(GatewayCriteria criteria, Pageable page) {
        log.debug("find by criteria : {}, page: {}", criteria, page);
        final Specification<Gateway> specification = createSpecification(criteria);
        return gatewayRepository.findAll(specification, page).map(gatewayMapper::toDto);
    }

    /**
     * Return the number of matching entities in the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the number of matching entities.
     */
    @Transactional(readOnly = true)
    public long countByCriteria(GatewayCriteria criteria) {
        log.debug("count by criteria : {}", criteria);
        final Specification<Gateway> specification = createSpecification(criteria);
        return gatewayRepository.count(specification);
    }

    /**
     * Function to convert {@link GatewayCriteria} to a {@link Specification}
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching {@link Specification} of the entity.
     */
    protected Specification<Gateway> createSpecification(GatewayCriteria criteria) {
        Specification<Gateway> specification = Specification.where(null);
        if (criteria != null) {
            if (criteria.getId() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getId(), Gateway_.id));
            }
            if (criteria.getName() != null) {
                specification = specification.and(buildStringSpecification(criteria.getName(), Gateway_.name));
            }
            if (criteria.getOrg() != null) {
                specification = specification.and(buildStringSpecification(criteria.getOrg(), Gateway_.org));
            }
            if (criteria.getActive() != null) {
                specification = specification.and(buildSpecification(criteria.getActive(), Gateway_.active));
            }
            if (criteria.getMfo() != null) {
                specification = specification.and(buildStringSpecification(criteria.getMfo(), Gateway_.mfo));
            }
            if (criteria.getTaxnr() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getTaxnr(), Gateway_.taxnr));
            }
            if (criteria.getBan() != null) {
                specification = specification.and(buildStringSpecification(criteria.getBan(), Gateway_.ban));
            }
            if (criteria.getContract() != null) {
                specification = specification.and(buildStringSpecification(criteria.getContract(), Gateway_.contract));
            }
            if (criteria.getSpringBeanName() != null) {
                specification = specification.and(buildStringSpecification(criteria.getSpringBeanName(), Gateway_.springBeanName));
            }
            if (criteria.getDepositId() != null) {
                specification =
                    specification.and(
                        buildSpecification(criteria.getDepositId(), root -> root.join(Gateway_.deposit, JoinType.LEFT).get(Deposit_.id))
                    );
            }
        }
        return specification;
    }
}
