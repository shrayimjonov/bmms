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
import uz.paykap.domain.Bill;
import uz.paykap.repository.BillRepository;
import uz.paykap.service.criteria.BillCriteria;
import uz.paykap.service.dto.BillDTO;
import uz.paykap.service.mapper.BillMapper;

/**
 * Service for executing complex queries for {@link Bill} entities in the database.
 * The main input is a {@link BillCriteria} which gets converted to {@link Specification},
 * in a way that all the filters must apply.
 * It returns a {@link List} of {@link BillDTO} or a {@link Page} of {@link BillDTO} which fulfills the criteria.
 */
@Service
@Transactional(readOnly = true)
public class BillQueryService extends QueryService<Bill> {

    private final Logger log = LoggerFactory.getLogger(BillQueryService.class);

    private final BillRepository billRepository;

    private final BillMapper billMapper;

    public BillQueryService(BillRepository billRepository, BillMapper billMapper) {
        this.billRepository = billRepository;
        this.billMapper = billMapper;
    }

    /**
     * Return a {@link List} of {@link BillDTO} which matches the criteria from the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    public List<BillDTO> findByCriteria(BillCriteria criteria) {
        log.debug("find by criteria : {}", criteria);
        final Specification<Bill> specification = createSpecification(criteria);
        return billMapper.toDto(billRepository.findAll(specification));
    }

    /**
     * Return a {@link Page} of {@link BillDTO} which matches the criteria from the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @param page The page, which should be returned.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    public Page<BillDTO> findByCriteria(BillCriteria criteria, Pageable page) {
        log.debug("find by criteria : {}, page: {}", criteria, page);
        final Specification<Bill> specification = createSpecification(criteria);
        return billRepository.findAll(specification, page).map(billMapper::toDto);
    }

    /**
     * Return the number of matching entities in the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the number of matching entities.
     */
    @Transactional(readOnly = true)
    public long countByCriteria(BillCriteria criteria) {
        log.debug("count by criteria : {}", criteria);
        final Specification<Bill> specification = createSpecification(criteria);
        return billRepository.count(specification);
    }

    /**
     * Function to convert {@link BillCriteria} to a {@link Specification}
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching {@link Specification} of the entity.
     */
    protected Specification<Bill> createSpecification(BillCriteria criteria) {
        Specification<Bill> specification = Specification.where(null);
        if (criteria != null) {
            if (criteria.getId() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getId(), Bill_.id));
            }
            if (criteria.getRecipientId() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getRecipientId(), Bill_.recipientId));
            }
            if (criteria.getRecipientShortName() != null) {
                specification = specification.and(buildStringSpecification(criteria.getRecipientShortName(), Bill_.recipientShortName));
            }
//            if (criteria.getSessionId() != null) {
//                specification = specification.and(buildStringSpecification(criteria.getSessionId(), Bill_.sessionId));
//            }
            if (criteria.getType() != null) {
                specification = specification.and(buildSpecification(criteria.getType(), Bill_.type));
            }
            if (criteria.getAmountRatio() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getAmountRatio(), Bill_.amountRatio));
            }
            if (criteria.getAmount() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getAmount(), Bill_.amount));
            }
            //            if (criteria.getProfit() != null) {
            //                specification = specification.and(buildRangeSpecification(criteria.getProfit(), Bill_.profit));
            //            }
            if (criteria.getAccount() != null) {
                specification = specification.and(buildStringSpecification(criteria.getAccount(), Bill_.account));
            }
            //TODO fix
            //            if (criteria.getParams() != null) {
            //                specification = specification.and(buildStringSpecification(criteria.getParams(), Bill_.params));
            //            }
            if (criteria.getCurrency() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getCurrency(), Bill_.currency));
            }
            if (criteria.getExtId() != null) {
                specification = specification.and(buildStringSpecification(criteria.getExtId(), Bill_.extId));
            }
            if (criteria.getRequestTime() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getRequestTime(), Bill_.requestTime));
            }
            //            if (criteria.getRequestJson() != null) {
            //                specification = specification.and(buildStringSpecification(criteria.getRequestJson(), Bill_.requestJson));
            //            }
            //            if (criteria.getResponseJson() != null) {
            //                specification = specification.and(buildStringSpecification(criteria.getResponseJson(), Bill_.responseJson));
            //            }
            if (criteria.getResponseTime() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getResponseTime(), Bill_.responseTime));
            }
            if (criteria.getResponseCode() != null) {
                specification = specification.and(buildStringSpecification(criteria.getResponseCode(), Bill_.responseCode));
            }
            if (criteria.getResponseText() != null) {
                specification = specification.and(buildStringSpecification(criteria.getResponseText(), Bill_.responseText));
            }
            if (criteria.getSuccess() != null) {
                specification = specification.and(buildSpecification(criteria.getSuccess(), Bill_.success));
            }
            if (criteria.getLogin() != null) {
                specification = specification.and(buildStringSpecification(criteria.getLogin(), Bill_.login));
            }
            if (criteria.getTransactionId() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getTransactionId(), Bill_.transactionId));
            }
            if (criteria.getOutExtId() != null) {
                specification = specification.and(buildStringSpecification(criteria.getOutExtId(), Bill_.outExtId));
            }
        }
        return specification;
    }
}
