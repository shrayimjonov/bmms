package uz.paykap.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.jhipster.service.QueryService;
import tech.jhipster.service.filter.StringFilter;
import uz.javlon.commons.exception.CodedException;
import uz.javlon.commons.exception.ProblemEnum;
import uz.paykap.domain.Transaction;
import uz.paykap.domain.Transaction_;
import uz.paykap.repository.TransactionRepository;
import uz.paykap.security.AuthoritiesConstants;
import uz.paykap.security.SecurityUtils;
import uz.paykap.service.criteria.TransactionCriteria;
import uz.paykap.service.dto.TransactionDTO;
import uz.paykap.service.mapper.TransactionMapper;

import java.util.List;

/**
 * Service for executing complex queries for {@link Transaction} entities in the database.
 * The main input is a {@link TransactionCriteria} which gets converted to {@link Specification},
 * in a way that all the filters must apply.
 * It returns a {@link List} of {@link TransactionDTO} or a {@link Page} of {@link TransactionDTO} which fulfills the criteria.
 */
@Service
@Transactional(readOnly = true)
public class TransactionQueryService extends QueryService<Transaction> {

    private final Logger log = LoggerFactory.getLogger(TransactionQueryService.class);

    private final TransactionRepository transactionRepository;

    private final TransactionMapper transactionMapper;

    public TransactionQueryService(TransactionRepository transactionRepository, TransactionMapper transactionMapper) {
        this.transactionRepository = transactionRepository;
        this.transactionMapper = transactionMapper;
    }

    /**
     * Return a {@link List} of {@link TransactionDTO} which matches the criteria from the database.
     *
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    public List<TransactionDTO> findByCriteria(TransactionCriteria criteria) {
        log.debug("find by criteria : {}", criteria);
        final Specification<Transaction> specification = createSpecification(criteria);
        return transactionMapper.toDto(transactionRepository.findAll(specification));
    }

    /**
     * Return a {@link Page} of {@link TransactionDTO} which matches the criteria from the database.
     *
     * @param criteria The object which holds all the filters, which the entities should match.
     * @param page     The page, which should be returned.
     * @return the matching entities.
     */
    //    @PreAuthorize("hasAnyRole('" + AuthoritiesConstants.ADMIN + "','" + AuthoritiesConstants.AGENT + "','" + AuthoritiesConstants.ROLE_VIEW_TRAN_FILTER + "')")
    @Transactional(readOnly = true)
    public Page<TransactionDTO> findByCriteria(TransactionCriteria criteria, Pageable page) {
        log.debug("find by criteria : {}, page: {}", criteria, page);

        if (SecurityUtils.hasCurrentUserThisAuthority(AuthoritiesConstants.AGENT)) {
            String currentUserLogin = SecurityUtils
                    .getCurrentUserLogin()
                    .orElseThrow(() -> new CodedException(ProblemEnum.USERNAME_NOT_FOUND));

            log.debug("User ---> " + currentUserLogin + " getting own transactions");

            StringFilter requestLogin = new StringFilter();
            requestLogin.setEquals(currentUserLogin);
            criteria.setRequestLogin(requestLogin);
        }

        final Specification<Transaction> specification = createSpecification(criteria);
        return transactionRepository.findAll(specification, page).map(transactionMapper::toDto);
    }

    /**
     * Return the number of matching entities in the database.
     *
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the number of matching entities.
     */
    @Transactional(readOnly = true)
    public long countByCriteria(TransactionCriteria criteria) {
        log.debug("count by criteria : {}", criteria);
        final Specification<Transaction> specification = createSpecification(criteria);
        return transactionRepository.count(specification);
    }

    /**
     * Function to convert {@link TransactionCriteria} to a {@link Specification}
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching {@link Specification} of the entity.
     */
    protected Specification<Transaction> createSpecification(TransactionCriteria criteria) {
        Specification<Transaction> specification = Specification.where(null);
        if (!SecurityUtils.hasCurrentUserThisAuthority("ROLE_ADMIN") &&
                !SecurityUtils.hasCurrentUserThisAuthority("ROLE_AGENT") &&
                SecurityUtils.hasCurrentUserThisAuthority("ROLE_RECIPIENT")) {
            var username = SecurityUtils.getCurrentUserLogin().get();
            criteria = criteria != null ? criteria : new TransactionCriteria();
            var recFilter = new StringFilter();
            recFilter.setEquals(username);
            criteria.setRecipientName(recFilter);
        }
        if (criteria != null) {
            if (criteria.getId() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getId(), Transaction_.id));
            }
            if (criteria.getRequestAmount() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getRequestAmount(), Transaction_.requestAmount));
            }
            if (criteria.getRequestCcy() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getRequestCcy(), Transaction_.requestCcy));
            }
            if (criteria.getRequestTrId() != null) {
                specification = specification.and(buildStringSpecification(criteria.getRequestTrId(), Transaction_.requestTrId));
            }
            if (criteria.getRequestLogin() != null) {
                specification = specification.and(buildStringSpecification(criteria.getRequestLogin(), Transaction_.requestLogin));
            }
            if (criteria.getRequestGroup() != null) {
                specification = specification.and(buildStringSpecification(criteria.getRequestGroup(), Transaction_.requestGroup));
            }
            if (criteria.getSenderName() != null) {
                specification = specification.and(buildStringSpecification(criteria.getSenderName(), Transaction_.senderName));
            }
            if (criteria.getSenderAccount() != null) {
                specification = specification.and(buildStringSpecification(criteria.getSenderAccount(), Transaction_.senderAccount));
            }
            if (criteria.getSenderServiceId() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getSenderServiceId(), Transaction_.senderServiceId));
            }
            if (criteria.getSenderAmount() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getSenderAmount(), Transaction_.senderAmount));
            }
            if (criteria.getSenderCcy() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getSenderCcy(), Transaction_.senderCcy));
            }
            if (criteria.getSenderTrId() != null) {
                specification = specification.and(buildStringSpecification(criteria.getSenderTrId(), Transaction_.senderTrId));
            }
            if (criteria.getSenderSuccess() != null) {
                specification = specification.and(buildSpecification(criteria.getSenderSuccess(), Transaction_.senderSuccess));
            }
            if (criteria.getSenderDate() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getSenderDate(), Transaction_.senderDate));
            }
            if (criteria.getSenderStatus() != null) {
                specification = specification.and(buildStringSpecification(criteria.getSenderStatus(), Transaction_.senderStatus));
            }
            if (criteria.getSenderStatusText() != null) {
                specification = specification.and(buildStringSpecification(criteria.getSenderStatusText(), Transaction_.senderStatusText));
            }
            if (criteria.getSenderBillId() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getSenderBillId(), Transaction_.senderBillId));
            }
            if (criteria.getSenderGateway() != null) {
                specification = specification.and(buildStringSpecification(criteria.getSenderGateway(), Transaction_.senderGateway));
            }
            if (criteria.getGatewayAmount() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getGatewayAmount(), Transaction_.gatewayAmount));
            }
            if (criteria.getRecipientName() != null) {
                specification = specification.and(buildStringSpecification(criteria.getRecipientName(), Transaction_.recipientName));
            }
            if (criteria.getRecipientAccount() != null) {
                specification = specification.and(buildStringSpecification(criteria.getRecipientAccount(), Transaction_.recipientAccount));
            }
            if (criteria.getRecipientServiceId() != null) {
                specification =
                        specification.and(buildRangeSpecification(criteria.getRecipientServiceId(), Transaction_.recipientServiceId));
            }
            if (criteria.getRecipientAmount() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getRecipientAmount(), Transaction_.recipientAmount));
            }
            if (criteria.getRecipientCcy() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getRecipientCcy(), Transaction_.recipientCcy));
            }
            if (criteria.getRecipientTrId() != null) {
                specification = specification.and(buildStringSpecification(criteria.getRecipientTrId(), Transaction_.recipientTrId));
            }
            if (criteria.getRecipientSuccess() != null) {
                specification = specification.and(buildSpecification(criteria.getRecipientSuccess(), Transaction_.recipientSuccess));
            }
            if (criteria.getRecipientDate() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getRecipientDate(), Transaction_.recipientDate));
            }
            if (criteria.getRecipientStatus() != null) {
                specification = specification.and(buildStringSpecification(criteria.getRecipientStatus(), Transaction_.recipientStatus));
            }
            if (criteria.getRecipientStatusText() != null) {
                specification =
                        specification.and(buildStringSpecification(criteria.getRecipientStatusText(), Transaction_.recipientStatusText));
            }
            if (criteria.getRecipientBillId() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getRecipientBillId(), Transaction_.recipientBillId));
            }
            if (criteria.getRecipientGateway() != null) {
                specification = specification.and(buildStringSpecification(criteria.getRecipientGateway(), Transaction_.recipientGateway));
            }
            if (criteria.getSystemCommissionUpAmount() != null) {
                specification =
                        specification.and(
                                buildRangeSpecification(criteria.getSystemCommissionUpAmount(), Transaction_.systemCommissionUpAmount)
                        );
            }
            if (criteria.getSystemCommissionDownAmount() != null) {
                specification =
                        specification.and(
                                buildRangeSpecification(criteria.getSystemCommissionDownAmount(), Transaction_.systemCommissionDownAmount)
                        );
            }
            if (criteria.getSystemFeeAmount() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getSystemFeeAmount(), Transaction_.systemFeeAmount));
            }
            if (criteria.getAgentFeeAmount() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getAgentFeeAmount(), Transaction_.agentFeeAmount));
            }
            if (criteria.getRoundAmount() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getRoundAmount(), Transaction_.roundAmount));
            }
            if (criteria.getSystemFee() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getSystemFee(), Transaction_.systemFee));
            }
            if (criteria.getSystemCurrency() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getSystemCurrency(), Transaction_.systemCurrency));
            }
            if (criteria.getCcyRate() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getCcyRate(), Transaction_.ccyRate));
            }
            if (criteria.getStatus() != null) {
                specification = specification.and(buildSpecification(criteria.getStatus(), Transaction_.status));
            }
            if (criteria.getComment() != null) {
                specification = specification.and(buildStringSpecification(criteria.getComment(), Transaction_.comment));
            }
            if (criteria.getDate() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getDate(), Transaction_.date));
            }
            if (criteria.getPayType() != null) {
                specification = specification.and(buildSpecification(criteria.getPayType(), Transaction_.payType));
            }
            if (criteria.getRequestSubAccount() != null) {
                specification =
                        specification.and(buildStringSpecification(criteria.getRequestSubAccount(), Transaction_.requestSubAccount));
            }
            if (criteria.getEposMerchant() != null) {
                specification = specification.and(buildStringSpecification(criteria.getEposMerchant(), Transaction_.eposMerchant));
            }
            if (criteria.getEposTerminal() != null) {
                specification = specification.and(buildStringSpecification(criteria.getEposTerminal(), Transaction_.eposTerminal));
            }
            if (criteria.getMain() != null) {
                specification = specification.and(buildSpecification(criteria.getMain(), Transaction_.main));
            }
            if (criteria.getAmountRatio() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getAmountRatio(), Transaction_.amountRatio));
            }
            if (criteria.getSenderBean() != null) {
                specification = specification.and(buildStringSpecification(criteria.getSenderBean(), Transaction_.senderBean));
            }
            if (criteria.getCategory() != null) {
                specification = specification.and(buildStringSpecification(criteria.getCategory(), Transaction_.category));
            }
            if (criteria.getPromo() != null) {
                specification = specification.and(buildStringSpecification(criteria.getPromo(), Transaction_.promo));
            }
        }
        return specification;
    }
}
