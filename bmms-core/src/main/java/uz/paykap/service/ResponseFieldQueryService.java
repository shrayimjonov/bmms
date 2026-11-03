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
import uz.paykap.domain.I18n_;
import uz.paykap.domain.Recipient_;
import uz.paykap.domain.ResponseField;
import uz.paykap.domain.ResponseField_;
import uz.paykap.repository.ResponseFieldRepository;
import uz.paykap.service.criteria.ResponseFieldCriteria;
import uz.paykap.service.dto.ResponseFieldDTO;
import uz.paykap.service.mapper.ResponseFieldMapper;

/**
 * Service for executing complex queries for {@link ResponseField} entities in the database.
 * The main input is a {@link ResponseFieldCriteria} which gets converted to {@link Specification},
 * in a way that all the filters must apply.
 * It returns a {@link List} of {@link ResponseFieldDTO} or a {@link Page} of {@link ResponseFieldDTO} which fulfills the criteria.
 */
@Service
@Transactional(readOnly = true)
public class ResponseFieldQueryService extends QueryService<ResponseField> {

    private final Logger log = LoggerFactory.getLogger(ResponseFieldQueryService.class);

    private final ResponseFieldRepository responseFieldRepository;

    private final ResponseFieldMapper responseFieldMapper;

    public ResponseFieldQueryService(ResponseFieldRepository responseFieldRepository, ResponseFieldMapper responseFieldMapper) {
        this.responseFieldRepository = responseFieldRepository;
        this.responseFieldMapper = responseFieldMapper;
    }

    /**
     * Return a {@link List} of {@link ResponseFieldDTO} which matches the criteria from the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    public List<ResponseFieldDTO> findByCriteria(ResponseFieldCriteria criteria) {
        log.debug("find by criteria : {}", criteria);
        final Specification<ResponseField> specification = createSpecification(criteria);
        return responseFieldMapper.toDto(responseFieldRepository.findAll(specification));
    }

    /**
     * Return a {@link Page} of {@link ResponseFieldDTO} which matches the criteria from the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @param page The page, which should be returned.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    public Page<ResponseFieldDTO> findByCriteria(ResponseFieldCriteria criteria, Pageable page) {
        log.debug("find by criteria : {}, page: {}", criteria, page);
        final Specification<ResponseField> specification = createSpecification(criteria);
        return responseFieldRepository.findAll(specification, page).map(responseFieldMapper::toDto);
    }

    /**
     * Return the number of matching entities in the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the number of matching entities.
     */
    @Transactional(readOnly = true)
    public long countByCriteria(ResponseFieldCriteria criteria) {
        log.debug("count by criteria : {}", criteria);
        final Specification<ResponseField> specification = createSpecification(criteria);
        return responseFieldRepository.count(specification);
    }

    /**
     * Function to convert {@link ResponseFieldCriteria} to a {@link Specification}
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching {@link Specification} of the entity.
     */
    protected Specification<ResponseField> createSpecification(ResponseFieldCriteria criteria) {
        Specification<ResponseField> specification = Specification.where(null);
        if (criteria != null) {
            if (criteria.getId() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getId(), ResponseField_.id));
            }
            if (criteria.getName() != null) {
                specification = specification.and(buildStringSpecification(criteria.getName(), ResponseField_.name));
            }
            if (criteria.getVisibility() != null) {
                specification = specification.and(buildSpecification(criteria.getVisibility(), ResponseField_.visibility));
            }
            if (criteria.getOrd() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getOrd(), ResponseField_.ord));
            }
            if (criteria.getRequestType() != null) {
                specification = specification.and(buildSpecification(criteria.getRequestType(), ResponseField_.requestType));
            }
            if (criteria.getType() != null) {
                specification = specification.and(buildSpecification(criteria.getType(), ResponseField_.type));
            }
            if (criteria.getAmountRatio() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getAmountRatio(), ResponseField_.amountRatio));
            }
            if (criteria.getRecipientId() != null) {
                specification =
                    specification.and(
                        buildSpecification(
                            criteria.getRecipientId(),
                            root -> root.join(ResponseField_.recipient, JoinType.LEFT).get(Recipient_.id)
                        )
                    );
            }
            if (criteria.getLabelId() != null) {
                specification =
                    specification.and(
                        buildSpecification(criteria.getLabelId(), root -> root.join(ResponseField_.labels, JoinType.LEFT).get(I18n_.id))
                    );
            }
        }
        return specification;
    }
}
