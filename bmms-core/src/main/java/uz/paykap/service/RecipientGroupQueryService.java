package uz.paykap.service;

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
import uz.paykap.domain.*; // Dont change
import uz.paykap.repository.RecipientGroupRepository;
import uz.paykap.service.criteria.RecipientGroupCriteria;
import uz.paykap.service.dto.RecipientGroupDTO;
import uz.paykap.service.mapper.RecipientGroupMapper;

/**
 * Service for executing complex queries for {@link RecipientGroup} entities in the database.
 * The main input is a {@link RecipientGroupCriteria} which gets converted to {@link Specification},
 * in a way that all the filters must apply.
 * It returns a {@link List} of {@link RecipientGroupDTO} or a {@link Page} of {@link RecipientGroupDTO} which fulfills the criteria.
 */
@Service
@Transactional(readOnly = true)
public class RecipientGroupQueryService extends QueryService<RecipientGroup> {

    private final Logger log = LoggerFactory.getLogger(RecipientGroupQueryService.class);

    private final RecipientGroupRepository recipientGroupRepository;

    private final RecipientGroupMapper recipientGroupMapper;

    public RecipientGroupQueryService(RecipientGroupRepository recipientGroupRepository, RecipientGroupMapper recipientGroupMapper) {
        this.recipientGroupRepository = recipientGroupRepository;
        this.recipientGroupMapper = recipientGroupMapper;
    }

    /**
     * Return a {@link List} of {@link RecipientGroupDTO} which matches the criteria from the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    public List<RecipientGroupDTO> findByCriteria(RecipientGroupCriteria criteria) {
        log.debug("find by criteria : {}", criteria);
        final Specification<RecipientGroup> specification = createSpecification(criteria);
        return recipientGroupMapper.toDto(recipientGroupRepository.findAll(specification));
    }

    /**
     * Return a {@link Page} of {@link RecipientGroupDTO} which matches the criteria from the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @param page The page, which should be returned.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    public Page<RecipientGroupDTO> findByCriteria(RecipientGroupCriteria criteria, Pageable page) {
        log.debug("find by criteria : {}, page: {}", criteria, page);
        final Specification<RecipientGroup> specification = createSpecification(criteria);
        return recipientGroupRepository.findAll(specification, page).map(recipientGroupMapper::toDto);
    }

    /**
     * Return the number of matching entities in the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the number of matching entities.
     */
    @Transactional(readOnly = true)
    public long countByCriteria(RecipientGroupCriteria criteria) {
        log.debug("count by criteria : {}", criteria);
        final Specification<RecipientGroup> specification = createSpecification(criteria);
        return recipientGroupRepository.count(specification);
    }

    /**
     * Function to convert {@link RecipientGroupCriteria} to a {@link Specification}
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching {@link Specification} of the entity.
     */
    protected Specification<RecipientGroup> createSpecification(RecipientGroupCriteria criteria) {
        Specification<RecipientGroup> specification = Specification.where(null);
        if (criteria != null) {
            if (criteria.getId() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getId(), RecipientGroup_.id));
            }
            if (criteria.getName() != null) {
                specification = specification.and(buildStringSpecification(criteria.getName(), RecipientGroup_.name));
            }
            if (criteria.getActive() != null) {
                specification = specification.and(buildSpecification(criteria.getActive(), RecipientGroup_.active));
            }
            if (criteria.getMore() != null) {
                specification = specification.and(buildStringSpecification(criteria.getMore(), RecipientGroup_.more));
            }
            if (criteria.getRecipientId() != null) {
                specification =
                    specification.and(
                        buildSpecification(
                            criteria.getRecipientId(),
                            root -> root.join(RecipientGroup_.recipients, JoinType.LEFT).get(Recipient_.id)
                        )
                    );
            }
        }
        return specification;
    }
}
