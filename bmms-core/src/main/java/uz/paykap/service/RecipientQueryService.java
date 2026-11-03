package uz.paykap.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.jhipster.service.QueryService;
import uz.paykap.domain.*;
import uz.paykap.repository.RecipientRepository;
import uz.paykap.service.criteria.RecipientCriteria;
import uz.paykap.service.dto.RecipientDTO;
import uz.paykap.service.mapper.RecipientMapper;

import javax.persistence.criteria.JoinType;
import java.util.List;

/**
 * Service for executing complex queries for {@link Recipient} entities in the database.
 * The main input is a {@link RecipientCriteria} which gets converted to {@link Specification},
 * in a way that all the filters must apply.
 * It returns a {@link List} of {@link RecipientDTO} or a {@link Page} of {@link RecipientDTO} which fulfills the criteria.
 */
@Service
@Transactional(readOnly = true)
public class RecipientQueryService extends QueryService<Recipient> {

    private final Logger log = LoggerFactory.getLogger(RecipientQueryService.class);

    private final RecipientRepository recipientRepository;

    private final RecipientMapper recipientMapper;

    public RecipientQueryService(RecipientRepository recipientRepository, RecipientMapper recipientMapper) {
        this.recipientRepository = recipientRepository;
        this.recipientMapper = recipientMapper;
    }

    /**
     * Return a {@link List} of {@link RecipientDTO} which matches the criteria from the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    public List<RecipientDTO> findByCriteria(RecipientCriteria criteria) {
        log.debug("find by criteria : {}", criteria);
        final Specification<Recipient> specification = createSpecification(criteria);
        return recipientMapper.toDto(recipientRepository.findAll(specification));
    }

    /**
     * Return a {@link Page} of {@link RecipientDTO} which matches the criteria from the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @param page The page, which should be returned.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    public Page<RecipientDTO> findByCriteria(RecipientCriteria criteria, Pageable page) {
        log.debug("find by criteria : {}, page: {}", criteria, page);
        final Specification<Recipient> specification = createSpecification(criteria);
        return recipientRepository.findAll(specification, page).map(recipientMapper::toDto);
    }

    /**
     * Return the number of matching entities in the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the number of matching entities.
     */
    @Transactional(readOnly = true)
    public long countByCriteria(RecipientCriteria criteria) {
        log.debug("count by criteria : {}", criteria);
        final Specification<Recipient> specification = createSpecification(criteria);
        return recipientRepository.count(specification);
    }

    /**
     * Function to convert {@link RecipientCriteria} to a {@link Specification}
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching {@link Specification} of the entity.
     */
    protected Specification<Recipient> createSpecification(RecipientCriteria criteria) {
        Specification<Recipient> specification = Specification.where(null);
        if (criteria != null) {
            if (criteria.getId() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getId(), Recipient_.id));
            }
            if (criteria.getShortName() != null) {
                specification = specification.and(buildStringSpecification(criteria.getShortName(), Recipient_.shortName));
            }
            if (criteria.getServiceId() != null) {
                specification = specification.and(buildStringSpecification(criteria.getServiceId(), Recipient_.serviceId));
            }
            if (criteria.getThumbnail() != null) {
                specification = specification.and(buildStringSpecification(criteria.getThumbnail(), Recipient_.thumbnail));
            }
            if (criteria.getLogo() != null) {
                specification = specification.and(buildStringSpecification(criteria.getLogo(), Recipient_.logo));
            }
            if (criteria.getAmountRatio() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getAmountRatio(), Recipient_.amountRatio));
            }
            if (criteria.getCcy() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getCcy(), Recipient_.ccy));
            }
            if (criteria.getMinAmount() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getMinAmount(), Recipient_.minAmount));
            }
            if (criteria.getMaxAmount() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getMaxAmount(), Recipient_.maxAmount));
            }
            if (criteria.getLimitRatio() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getLimitRatio(), Recipient_.limitRatio));
            }
            if (criteria.getCommission() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getCommission(), Recipient_.commission));
            }
            if (criteria.getFee() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getFee(), Recipient_.fee));
            }
            if (criteria.getSystemCommission() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getSystemCommission(), Recipient_.systemCommission));
            }
            if (criteria.getSystemFee() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getSystemFee(), Recipient_.systemFee));
            }
            if (criteria.getSenderCommission() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getSenderCommission(), Recipient_.senderCommission));
            }
            if (criteria.getOrd() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getOrd(), Recipient_.ord));
            }
            if (criteria.getActive() != null) {
                specification = specification.and(buildSpecification(criteria.getActive(), Recipient_.active));
            }
            if (criteria.getCheck() != null) {
                specification = specification.and(buildSpecification(criteria.getCheck(), Recipient_.check));
            }
            if (criteria.getPay() != null) {
                specification = specification.and(buildSpecification(criteria.getPay(), Recipient_.pay));
            }
            if (criteria.getStatus() != null) {
                specification = specification.and(buildSpecification(criteria.getStatus(), Recipient_.status));
            }
            if (criteria.getCancel() != null) {
                specification = specification.and(buildSpecification(criteria.getCancel(), Recipient_.cancel));
            }
            if (criteria.getCanDeposit() != null) {
                specification = specification.and(buildSpecification(criteria.getCanDeposit(), Recipient_.canDeposit));
            }
//            if (criteria.getEposPort() != null) {
//                specification = specification.and(buildRangeSpecification(criteria.getEposPort(), Recipient_.eposPort));
//            }
//            if (criteria.getEposMerchant() != null) {
//                specification = specification.and(buildRangeSpecification(criteria.getEposMerchant(), Recipient_.eposMerchant));
//            }
//            if (criteria.getEposTerminal() != null) {
//                specification = specification.and(buildRangeSpecification(criteria.getEposTerminal(), Recipient_.eposTerminal));
//            }
            if (criteria.getSettlementAccount() != null) {
                specification = specification.and(buildStringSpecification(criteria.getSettlementAccount(), Recipient_.settlementAccount));
            }
            if (criteria.getMfo() != null) {
                specification = specification.and(buildStringSpecification(criteria.getMfo(), Recipient_.mfo));
            }
            if (criteria.getInn() != null) {
                specification = specification.and(buildStringSpecification(criteria.getInn(), Recipient_.inn));
            }
            if (criteria.getReconcile() != null) {
                specification = specification.and(buildSpecification(criteria.getReconcile(), Recipient_.reconcile));
            }
            if (criteria.getType() != null) {
                specification = specification.and(buildSpecification(criteria.getType(), Recipient_.type));
            }
            if (criteria.getGatewayId() != null) {
                specification =
                    specification.and(
                        buildSpecification(criteria.getGatewayId(), root -> root.join(Recipient_.gateway, JoinType.LEFT).get(Gateway_.id))
                    );
            }
            if (criteria.getCategoryId() != null) {
                specification =
                    specification.and(
                        buildSpecification(
                            criteria.getCategoryId(),
                            root -> root.join(Recipient_.category, JoinType.LEFT).get(Category_.id)
                        )
                    );
            }
            if (criteria.getTitleId() != null) {
                specification =
                    specification.and(
                        buildSpecification(criteria.getTitleId(), root -> root.join(Recipient_.titles, JoinType.LEFT).get(I18n_.id))
                    );
            }
            if (criteria.getFullNameId() != null) {
                specification =
                    specification.and(
                        buildSpecification(criteria.getFullNameId(), root -> root.join(Recipient_.fullNames, JoinType.LEFT).get(I18n_.id))
                    );
            }
        }
        return specification;
    }
}
