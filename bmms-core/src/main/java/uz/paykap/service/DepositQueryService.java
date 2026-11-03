package uz.paykap.service;

import lombok.AllArgsConstructor;
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
import uz.paykap.domain.*;
import uz.paykap.repository.AgentRepository;
import uz.paykap.repository.DepositRepository;
import uz.paykap.security.AuthoritiesConstants;
import uz.paykap.security.SecurityUtils;
import uz.paykap.service.criteria.DepositCriteria;
import uz.paykap.service.dto.DepositDTO;
import uz.paykap.service.mapper.DepositMapper;

import javax.persistence.criteria.JoinType;
import java.util.List;

/**
 * Service for executing complex queries for {@link Deposit} entities in the database.
 * The main input is a {@link DepositCriteria} which gets converted to {@link Specification},
 * in a way that all the filters must apply.
 * It returns a {@link List} of {@link DepositDTO} or a {@link Page} of {@link DepositDTO} which fulfills the criteria.
 */
@Service
@Transactional(readOnly = true)
@AllArgsConstructor
public class DepositQueryService extends QueryService<Deposit> {

    private final Logger log = LoggerFactory.getLogger(DepositQueryService.class);

    private final DepositRepository depositRepository;

    private final DepositMapper depositMapper;

    private final AgentRepository agentRepository;

    /**
     * Return a {@link List} of {@link DepositDTO} which matches the criteria from the database.
     *
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    public List<DepositDTO> findByCriteria(DepositCriteria criteria) {
        log.debug("find by criteria : {}", criteria);
        final Specification<Deposit> specification = createSpecification(criteria);
        return depositMapper.toDto(depositRepository.findAll(specification));
    }

    /**
     * Return a {@link Page} of {@link DepositDTO} which matches the criteria from the database.
     *
     * @param criteria The object which holds all the filters, which the entities should match.
     * @param page     The page, which should be returned.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    public Page<DepositDTO> findByCriteria(DepositCriteria criteria, Pageable page) {
        log.debug("find by criteria : {}, page: {}", criteria, page);

        if (SecurityUtils.hasCurrentUserThisAuthority(AuthoritiesConstants.AGENT)) {
            String currentUserLogin = SecurityUtils
                    .getCurrentUserLogin()
                    .orElseThrow(() -> new CodedException(ProblemEnum.USERNAME_NOT_FOUND));

            log.info("User --> " + currentUserLogin + " getting own deposits");

//            agentRepository.findTop1ByShortName(currentUserLogin)
//                    .ifPresent(resAgent -> {
//                        LongFilter requestAgentId = new LongFilter();
//                        requestAgentId.setEquals(resAgent.getId());
//                        criteria.setAgentId(requestAgentId);
//                    });

            final StringFilter agentShortName = new StringFilter();
            agentShortName.setEquals(currentUserLogin);
            criteria.setAgentShortName(agentShortName);
        }

        final Specification<Deposit> specification = createSpecification(criteria);
        return depositRepository.findAll(specification, page).map(depositMapper::toDto);
    }

    /**
     * Return the number of matching entities in the database.
     *
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the number of matching entities.
     */
    @Transactional(readOnly = true)
    public long countByCriteria(DepositCriteria criteria) {
        log.debug("count by criteria : {}", criteria);
        final Specification<Deposit> specification = createSpecification(criteria);
        return depositRepository.count(specification);
    }

    /**
     * Function to convert {@link DepositCriteria} to a {@link Specification}
     *
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching {@link Specification} of the entity.
     */
    protected Specification<Deposit> createSpecification(DepositCriteria criteria) {
        Specification<Deposit> specification = Specification.where(null);
        if (criteria != null) {
            if (criteria.getId() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getId(), Deposit_.id));
            }
            if (criteria.getOverdraftLimit() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getOverdraftLimit(), Deposit_.overdraftLimit));
            }
            if (criteria.getBalance() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getBalance(), Deposit_.balance));
            }
            if (criteria.getBalanceCcy() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getBalanceCcy(), Deposit_.balanceCcy));
            }
            if (criteria.getBookedBalance() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getBookedBalance(), Deposit_.bookedBalance));
            }
            if (criteria.getLimitInform() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getLimitInform(), Deposit_.limitInform));
            }
            if (criteria.getLimitDisable() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getLimitDisable(), Deposit_.limitDisable));
            }
            if (criteria.getContactGroup() != null) {
                specification = specification.and(buildStringSpecification(criteria.getContactGroup(), Deposit_.contactGroup));
            }
            if (criteria.getBlocked() != null) {
                specification = specification.and(buildSpecification(criteria.getBlocked(), Deposit_.blocked));
            }
            if (criteria.getUnlimit() != null) {
                specification = specification.and(buildSpecification(criteria.getUnlimit(), Deposit_.unlimit));
            }
            if (criteria.getState() != null) {
                specification = specification.and(buildSpecification(criteria.getState(), Deposit_.state));
            }
            if (criteria.getGatewayId() != null) {
                specification =
                        specification.and(
                                buildSpecification(criteria.getGatewayId(), root -> root.join(Deposit_.gateway, JoinType.LEFT).get(Gateway_.id))
                        );
            }
            if (criteria.getAgentId() != null) {
                specification =
                        specification.and(
                                buildSpecification(criteria.getAgentId(), root -> root.join(Deposit_.agent, JoinType.LEFT).get(Agent_.id))
                        );
            }
            if (criteria.getAgentShortName() != null) {
                specification =
                        specification.and(
                                buildSpecification(criteria.getAgentShortName(), root -> root.join(Deposit_.agent, JoinType.LEFT).get(Agent_.shortName))
                        );
            }
            if (criteria.getVirtualAccountId() != null) {
                specification =
                        specification.and(
                                buildSpecification(
                                        criteria.getVirtualAccountId(),
                                        root -> root.join(Deposit_.virtualAccount, JoinType.LEFT).get(VirtualAccount_.id)
                                )
                        );
            }
        }
        return specification;
    }
}
