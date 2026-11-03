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
import uz.paykap.domain.*; // for static metamodels
import uz.paykap.domain.ForeignPay;
import uz.paykap.repository.ForeignPayRepository;
import uz.paykap.service.criteria.ForeignPayCriteria;
import uz.paykap.service.dto.ForeignPayDTO;
import uz.paykap.service.mapper.ForeignPayMapper;

/**
 * Service for executing complex queries for {@link ForeignPay} entities in the database.
 * The main input is a {@link ForeignPayCriteria} which gets converted to {@link Specification},
 * in a way that all the filters must apply.
 * It returns a {@link List} of {@link ForeignPayDTO} or a {@link Page} of {@link ForeignPayDTO} which fulfills the criteria.
 */
@Service
@Transactional(readOnly = true)
public class ForeignPayQueryService extends QueryService<ForeignPay> {

    private final Logger log = LoggerFactory.getLogger(ForeignPayQueryService.class);

    private final ForeignPayRepository foreignPayRepository;

    private final ForeignPayMapper foreignPayMapper;

    public ForeignPayQueryService(ForeignPayRepository foreignPayRepository, ForeignPayMapper foreignPayMapper) {
        this.foreignPayRepository = foreignPayRepository;
        this.foreignPayMapper = foreignPayMapper;
    }

    /**
     * Return a {@link List} of {@link ForeignPayDTO} which matches the criteria from the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    public List<ForeignPayDTO> findByCriteria(ForeignPayCriteria criteria) {
        log.debug("find by criteria : {}", criteria);
        final Specification<ForeignPay> specification = createSpecification(criteria);
        return foreignPayMapper.toDto(foreignPayRepository.findAll(specification));
    }

    /**
     * Return a {@link Page} of {@link ForeignPayDTO} which matches the criteria from the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @param page The page, which should be returned.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    public Page<ForeignPayDTO> findByCriteria(ForeignPayCriteria criteria, Pageable page) {
        log.debug("find by criteria : {}, page: {}", criteria, page);
        final Specification<ForeignPay> specification = createSpecification(criteria);
        return foreignPayRepository.findAll(specification, page).map(foreignPayMapper::toDto);
    }

    /**
     * Return the number of matching entities in the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the number of matching entities.
     */
    @Transactional(readOnly = true)
    public long countByCriteria(ForeignPayCriteria criteria) {
        log.debug("count by criteria : {}", criteria);
        final Specification<ForeignPay> specification = createSpecification(criteria);
        return foreignPayRepository.count(specification);
    }

    /**
     * Function to convert {@link ForeignPayCriteria} to a {@link Specification}
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching {@link Specification} of the entity.
     */
    protected Specification<ForeignPay> createSpecification(ForeignPayCriteria criteria) {
        Specification<ForeignPay> specification = Specification.where(null);
        if (criteria != null) {
            if (criteria.getId() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getId(), ForeignPay_.id));
            }
            if (criteria.getVerifiedDate() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getVerifiedDate(), ForeignPay_.verifiedDate));
            }
            if (criteria.getStatus() != null) {
                specification = specification.and(buildSpecification(criteria.getStatus(), ForeignPay_.status));
            }
            if (criteria.getCardToken() != null) {
                specification = specification.and(buildStringSpecification(criteria.getCardToken(), ForeignPay_.cardToken));
            }
            if (criteria.getBillId() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getBillId(), ForeignPay_.billId));
            }
            if (criteria.getOtp() != null) {
                specification = specification.and(buildStringSpecification(criteria.getOtp(), ForeignPay_.otp));
            }
            if (criteria.getPhone() != null) {
                specification = specification.and(buildStringSpecification(criteria.getPhone(), ForeignPay_.phone));
            }
            if (criteria.getEntryCount() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getEntryCount(), ForeignPay_.entryCount));
            }
        }
        return specification;
    }
}
