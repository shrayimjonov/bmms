package uz.paykap.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.jhipster.service.QueryService;
import uz.paykap.domain.P2pCommission_;
import uz.paykap.domain.P2pCondition_;
import uz.paykap.domain.P2pParam;
import uz.paykap.domain.P2pParam_;
import uz.paykap.repository.P2pParamRepository;
import uz.paykap.service.criteria.P2pParamCriteria;

import javax.persistence.criteria.JoinType;
import java.util.List;

/**
 * Service for executing complex queries for {@link P2pParam} entities in the database.
 * The main input is a {@link P2pParamCriteria} which gets converted to {@link Specification},
 * in a way that all the filters must apply.
 * It returns a {@link List} of {@link P2pParam} or a {@link Page} of {@link P2pParam} which fulfills the criteria.
 */
@Service
@Transactional(readOnly = true)
public class P2pParamQueryService extends QueryService<P2pParam> {

    private final Logger log = LoggerFactory.getLogger(P2pParamQueryService.class);

    private final P2pParamRepository p2pParamRepository;

    public P2pParamQueryService(P2pParamRepository p2pParamRepository) {
        this.p2pParamRepository = p2pParamRepository;
    }

    /**
     * Return a {@link List} of {@link P2pParam} which matches the criteria from the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    public List<P2pParam> findByCriteria(P2pParamCriteria criteria) {
        log.debug("find by criteria : {}", criteria);
        final Specification<P2pParam> specification = createSpecification(criteria);
        return p2pParamRepository.findAll(specification);
    }

    /**
     * Return a {@link Page} of {@link P2pParam} which matches the criteria from the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @param page The page, which should be returned.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    public Page<P2pParam> findByCriteria(P2pParamCriteria criteria, Pageable page) {
        log.debug("find by criteria : {}, page: {}", criteria, page);
        final Specification<P2pParam> specification = createSpecification(criteria);
        return p2pParamRepository.findAll(specification, page);
    }

    /**
     * Return the number of matching entities in the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the number of matching entities.
     */
    @Transactional(readOnly = true)
    public long countByCriteria(P2pParamCriteria criteria) {
        log.debug("count by criteria : {}", criteria);
        final Specification<P2pParam> specification = createSpecification(criteria);
        return p2pParamRepository.count(specification);
    }

    /**
     * Function to convert {@link P2pParamCriteria} to a {@link Specification}
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching {@link Specification} of the entity.
     */
    protected Specification<P2pParam> createSpecification(P2pParamCriteria criteria) {
        Specification<P2pParam> specification = Specification.where(null);
        if (criteria != null) {
            if (criteria.getId() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getId(), P2pParam_.id));
            }
            if (criteria.getPriority() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getPriority(), P2pParam_.priority));
            }
            if (criteria.getActive() != null) {
                specification = specification.and(buildSpecification(criteria.getActive(), P2pParam_.active));
            }
            if (criteria.getSenderPc() != null) {
                specification = specification.and(buildSpecification(criteria.getSenderPc(), P2pParam_.senderPc));
            }
            if (criteria.getRecipientPc() != null) {
                specification = specification.and(buildSpecification(criteria.getRecipientPc(), P2pParam_.recipientPc));
            }
            if (criteria.getName() != null) {
                specification = specification.and(buildStringSpecification(criteria.getName(), P2pParam_.name));
            }
            if (criteria.getSenderConditionId() != null) {
                specification =
                    specification.and(
                        buildSpecification(
                            criteria.getSenderConditionId(),
                            root -> root.join(P2pParam_.senderCondition, JoinType.LEFT).get(P2pCondition_.id)
                        )
                    );
            }
            if (criteria.getRecipientConditionId() != null) {
                specification =
                    specification.and(
                        buildSpecification(
                            criteria.getRecipientConditionId(),
                            root -> root.join(P2pParam_.recipientCondition, JoinType.LEFT).get(P2pCondition_.id)
                        )
                    );
            }
            if (criteria.getSenderPercentId() != null) {
                specification =
                    specification.and(
                        buildSpecification(
                            criteria.getSenderPercentId(),
                            root -> root.join(P2pParam_.senderPercent, JoinType.LEFT).get(P2pCommission_.id)
                        )
                    );
            }
            if (criteria.getRecipientPercentId() != null) {
                specification =
                    specification.and(
                        buildSpecification(
                            criteria.getRecipientPercentId(),
                            root -> root.join(P2pParam_.recipientPercent, JoinType.LEFT).get(P2pCommission_.id)
                        )
                    );
            }
            if (criteria.getPromo() != null) {
                specification = specification.and(buildStringSpecification(criteria.getPromo(), P2pParam_.promo));
            }
        }
        return specification;
    }
}
