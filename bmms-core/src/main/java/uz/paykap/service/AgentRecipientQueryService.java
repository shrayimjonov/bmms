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
import uz.paykap.repository.AgentRecipientRepository;
import uz.paykap.service.criteria.AgentRecipientCriteria;
import uz.paykap.service.dto.AgentRecipientDTO;
import uz.paykap.service.mapper.AgentRecipientMapper;

import javax.persistence.criteria.JoinType;
import java.util.List;

/**
 * Service for executing complex queries for {@link AgentRecipient} entities in the database.
 * The main input is a {@link AgentRecipientCriteria} which gets converted to {@link Specification},
 * in a way that all the filters must apply.
 * It returns a {@link List} of {@link AgentRecipientDTO} or a {@link Page} of {@link AgentRecipientDTO} which fulfills the criteria.
 */
@Service
@Transactional(readOnly = true)
public class AgentRecipientQueryService extends QueryService<AgentRecipient> {

    private final Logger log = LoggerFactory.getLogger(AgentRecipientQueryService.class);

    private final AgentRecipientRepository agentRecipientRepository;

    private final AgentRecipientMapper agentRecipientMapper;

    public AgentRecipientQueryService(AgentRecipientRepository agentRecipientRepository, AgentRecipientMapper agentRecipientMapper) {
        this.agentRecipientRepository = agentRecipientRepository;
        this.agentRecipientMapper = agentRecipientMapper;
    }

    /**
     * Return a {@link List} of {@link AgentRecipientDTO} which matches the criteria from the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    public List<AgentRecipientDTO> findByCriteria(AgentRecipientCriteria criteria) {
        log.debug("find by criteria : {}", criteria);
        final Specification<AgentRecipient> specification = createSpecification(criteria);
        return agentRecipientMapper.toDto(agentRecipientRepository.findAll(specification));
    }

    /**
     * Return a {@link Page} of {@link AgentRecipientDTO} which matches the criteria from the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @param page The page, which should be returned.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    public Page<AgentRecipientDTO> findByCriteria(AgentRecipientCriteria criteria, Pageable page) {
        log.debug("find by criteria : {}, page: {}", criteria, page);
        final Specification<AgentRecipient> specification = createSpecification(criteria);
        return agentRecipientRepository.findAll(specification, page).map(agentRecipientMapper::toDto);
    }

    /**
     * Return the number of matching entities in the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the number of matching entities.
     */
    @Transactional(readOnly = true)
    public long countByCriteria(AgentRecipientCriteria criteria) {
        log.debug("count by criteria : {}", criteria);
        final Specification<AgentRecipient> specification = createSpecification(criteria);
        return agentRecipientRepository.count(specification);
    }

    /**
     * Function to convert {@link AgentRecipientCriteria} to a {@link Specification}
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching {@link Specification} of the entity.
     */
    protected Specification<AgentRecipient> createSpecification(AgentRecipientCriteria criteria) {
        Specification<AgentRecipient> specification = Specification.where(null);
        if (criteria != null) {
            if (criteria.getId() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getId(), AgentRecipient_.id));
            }
            if (criteria.getAgentCommission() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getAgentCommission(), AgentRecipient_.agentCommission));
            }
            if (criteria.getAgentFee() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getAgentFee(), AgentRecipient_.agentFee));
            }
            if (criteria.getSystemCommissionUp() != null) {
                specification =
                    specification.and(buildRangeSpecification(criteria.getSystemCommissionUp(), AgentRecipient_.systemCommissionUp));
            }
            if (criteria.getSystemCommissionDown() != null) {
                specification =
                    specification.and(buildRangeSpecification(criteria.getSystemCommissionDown(), AgentRecipient_.systemCommissionDown));
            }
            if (criteria.getSystemFee() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getSystemFee(), AgentRecipient_.systemFee));
            }
            if (criteria.getActive() != null) {
                specification = specification.and(buildSpecification(criteria.getActive(), AgentRecipient_.active));
            }
            if (criteria.getEposPort() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getEposPort(), AgentRecipient_.eposPort));
            }
            if (criteria.getEposMerchant() != null) {
                specification = specification.and(buildStringSpecification(criteria.getEposMerchant(), AgentRecipient_.eposMerchant));
            }
            if (criteria.getRequestGroup() != null) {
                specification = specification.and(buildStringSpecification(criteria.getRequestGroup(), AgentRecipient_.requestGroup));
            }
            if (criteria.getEposTerminal() != null) {
                specification = specification.and(buildStringSpecification(criteria.getEposTerminal(), AgentRecipient_.eposTerminal));
            }
            if (criteria.getAgentId() != null) {
                specification =
                    specification.and(
                        buildSpecification(criteria.getAgentId(), root -> root.join(AgentRecipient_.agent, JoinType.LEFT).get(Agent_.id))
                    );
            }
            if (criteria.getRecipientId() != null) {
                specification =
                    specification.and(
                        buildSpecification(
                            criteria.getRecipientId(),
                            root -> root.join(AgentRecipient_.recipient, JoinType.LEFT).get(Recipient_.id)
                        )
                    );
            }
            if (criteria.getSettingId() != null) {
                specification =
                    specification.and(
                        buildSpecification(
                            criteria.getSettingId(),
                            root -> root.join(AgentRecipient_.setting, JoinType.LEFT).get(Setting_.id)
                        )
                    );
            }
            if (criteria.getAgentId() != null) {
                specification =
                    specification.and(
                        buildSpecification(criteria.getAgentId(), root -> root.join(AgentRecipient_.agent, JoinType.LEFT).get(Agent_.id))
                    );
            }
            if (criteria.getRecipientGroupId() != null) {
                specification =
                    specification.and(
                        buildSpecification(
                            criteria.getRecipientGroupId(),
                            root -> root.join(AgentRecipient_.recipientGroup, JoinType.LEFT).get(RecipientGroup_.id)
                        )
                    );
            }
        }
        return specification;
    }
}
