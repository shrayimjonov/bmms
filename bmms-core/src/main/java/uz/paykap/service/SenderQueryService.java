package uz.paykap.service;

import java.util.List;
import java.util.List;
import javax.persistence.criteria.JoinType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.jhipster.service.QueryService;
import uz.paykap.domain.*; // for static metamodels
import uz.paykap.domain.Sender;
import uz.paykap.repository.SenderRepository;
import uz.paykap.service.criteria.SenderCriteria;
import uz.paykap.service.dto.SenderDTO;
import uz.paykap.service.mapper.SenderMapper;

/**
 * Service for executing complex queries for {@link Sender} entities in the database.
 * The main input is a {@link SenderCriteria} which gets converted to {@link Specification},
 * in a way that all the filters must apply.
 * It returns a {@link List} of {@link SenderDTO} or a {@link Page} of {@link SenderDTO} which fulfills the criteria.
 */
@Service
@Transactional(readOnly = true)
public class SenderQueryService extends QueryService<Sender> {

    private final Logger log = LoggerFactory.getLogger(SenderQueryService.class);

    private final SenderRepository senderRepository;

    private final SenderMapper senderMapper;

    public SenderQueryService(SenderRepository senderRepository, SenderMapper senderMapper) {
        this.senderRepository = senderRepository;
        this.senderMapper = senderMapper;
    }

    /**
     * Return a {@link List} of {@link SenderDTO} which matches the criteria from the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    public List<SenderDTO> findByCriteria(SenderCriteria criteria) {
        log.debug("find by criteria : {}", criteria);
        final Specification<Sender> specification = createSpecification(criteria);
        return senderMapper.toDto(senderRepository.findAll(specification));
    }

    /**
     * Return a {@link Page} of {@link SenderDTO} which matches the criteria from the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @param page The page, which should be returned.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    public Page<SenderDTO> findByCriteria(SenderCriteria criteria, Pageable page) {
        log.debug("find by criteria : {}, page: {}", criteria, page);
        final Specification<Sender> specification = createSpecification(criteria);
        return senderRepository.findAll(specification, page).map(senderMapper::toDto);
    }

    /**
     * Return the number of matching entities in the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the number of matching entities.
     */
    @Transactional(readOnly = true)
    public long countByCriteria(SenderCriteria criteria) {
        log.debug("count by criteria : {}", criteria);
        final Specification<Sender> specification = createSpecification(criteria);
        return senderRepository.count(specification);
    }

    /**
     * Function to convert {@link SenderCriteria} to a {@link Specification}
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching {@link Specification} of the entity.
     */
    private Specification<Sender> createSpecification(SenderCriteria criteria) {
        Specification<Sender> specification = Specification.where(null);
        if (criteria != null) {
            if (criteria.getId() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getId(), Sender_.id));
            }
            if (criteria.getName() != null) {
                specification = specification.and(buildStringSpecification(criteria.getName(), Sender_.name));
            }
            if (criteria.getOrd() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getOrd(), Sender_.ord));
            }
            if (criteria.getActive() != null) {
                specification = specification.and(buildSpecification(criteria.getActive(), Sender_.active));
            }
            if (criteria.getSpringBeanName() != null) {
                specification = specification.and(buildStringSpecification(criteria.getSpringBeanName(), Sender_.springBeanName));
            }
            if (criteria.getPayType() != null) {
                specification = specification.and(buildSpecification(criteria.getPayType(), Sender_.payType));
            }
            if (criteria.getAgentId() != null) {
                specification =
                    specification.and(
                        buildSpecification(criteria.getAgentId(), root -> root.join(Sender_.agent, JoinType.LEFT).get(Agent_.id))
                    );
            }
        }
        return specification;
    }
}
