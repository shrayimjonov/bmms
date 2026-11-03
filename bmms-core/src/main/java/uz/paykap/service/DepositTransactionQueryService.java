package uz.paykap.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.jhipster.service.QueryService;
import uz.paykap.domain.DepositTransaction;
import uz.paykap.domain.DepositTransaction_;
import uz.paykap.repository.DepositTransactionRepository;
import uz.paykap.service.criteria.DepositTransactionCriteria;
import uz.paykap.service.dto.DepositTransactionDTO;
import uz.paykap.service.mapper.DepositTransactionMapper;

import java.util.List;

/**
 * Service for executing complex queries for {@link DepositTransaction} entities in the database.
 * The main input is a {@link DepositTransactionCriteria} which gets converted to {@link Specification},
 * in a way that all the filters must apply.
 * It returns a {@link List} of {@link DepositTransactionDTO} or a {@link Page} of {@link DepositTransactionDTO} which fulfills the criteria.
 */
@Service
@Transactional(readOnly = true)
public class DepositTransactionQueryService extends QueryService<DepositTransaction> {

    private final Logger log = LoggerFactory.getLogger(DepositTransactionQueryService.class);

    private final DepositTransactionRepository depositTransactionRepository;

    private final DepositTransactionMapper depositTransactionMapper;

    public DepositTransactionQueryService(
        DepositTransactionRepository depositTransactionRepository,
        DepositTransactionMapper depositTransactionMapper
    ) {
        this.depositTransactionRepository = depositTransactionRepository;
        this.depositTransactionMapper = depositTransactionMapper;
    }

    /**
     * Return a {@link List} of {@link DepositTransactionDTO} which matches the criteria from the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    public List<DepositTransactionDTO> findByCriteria(DepositTransactionCriteria criteria) {
        log.debug("find by criteria : {}", criteria);
        final Specification<DepositTransaction> specification = createSpecification(criteria);
        return depositTransactionMapper.toDto(depositTransactionRepository.findAll(specification));
    }

    /**
     * Return a {@link Page} of {@link DepositTransactionDTO} which matches the criteria from the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @param page The page, which should be returned.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    public Page<DepositTransactionDTO> findByCriteria(DepositTransactionCriteria criteria, Pageable page) {
        log.debug("find by criteria : {}, page: {}", criteria, page);
        final Specification<DepositTransaction> specification = createSpecification(criteria);
        return depositTransactionRepository.findAll(specification, page).map(depositTransactionMapper::toDto);
    }

    /**
     * Return the number of matching entities in the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the number of matching entities.
     */
    @Transactional(readOnly = true)
    public long countByCriteria(DepositTransactionCriteria criteria) {
        log.debug("count by criteria : {}", criteria);
        final Specification<DepositTransaction> specification = createSpecification(criteria);
        return depositTransactionRepository.count(specification);
    }

    /**
     * Function to convert {@link DepositTransactionCriteria} to a {@link Specification}
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching {@link Specification} of the entity.
     */
    protected Specification<DepositTransaction> createSpecification(DepositTransactionCriteria criteria) {
        Specification<DepositTransaction> specification = Specification.where(null);
        if (criteria != null) {
            if (criteria.getId() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getId(), DepositTransaction_.id));
            }
            if (criteria.getPayerTaxnr() != null) {
                specification = specification.and(buildStringSpecification(criteria.getPayerTaxnr(), DepositTransaction_.payerTaxnr));
            }
            if (criteria.getRecipientTaxnr() != null) {
                specification =
                    specification.and(buildStringSpecification(criteria.getRecipientTaxnr(), DepositTransaction_.recipientTaxnr));
            }
            if (criteria.getAmount() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getAmount(), DepositTransaction_.amount));
            }
            if (criteria.getUpdateDate() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getUpdateDate(), DepositTransaction_.updateDate));
            }
            if (criteria.getComment() != null) {
                specification = specification.and(buildStringSpecification(criteria.getComment(), DepositTransaction_.comment));
            }
            if (criteria.getDoc() != null) {
                specification = specification.and(buildStringSpecification(criteria.getDoc(), DepositTransaction_.doc));
            }
        }
        return specification;
    }
}
