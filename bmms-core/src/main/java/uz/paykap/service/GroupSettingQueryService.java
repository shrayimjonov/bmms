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
import uz.paykap.domain.GroupSetting;
import uz.paykap.repository.GroupSettingRepository;
import uz.paykap.service.criteria.GroupSettingCriteria;
import uz.paykap.service.dto.GroupSettingDTO;
import uz.paykap.service.mapper.GroupSettingMapper;

/**
 * Service for executing complex queries for {@link GroupSetting} entities in the database.
 * The main input is a {@link GroupSettingCriteria} which gets converted to {@link Specification},
 * in a way that all the filters must apply.
 * It returns a {@link List} of {@link GroupSettingDTO} or a {@link Page} of {@link GroupSettingDTO} which fulfills the criteria.
 */
@Service
@Transactional(readOnly = true)
public class GroupSettingQueryService extends QueryService<GroupSetting> {

    private final Logger log = LoggerFactory.getLogger(GroupSettingQueryService.class);

    private final GroupSettingRepository groupSettingRepository;

    private final GroupSettingMapper groupSettingMapper;

    public GroupSettingQueryService(GroupSettingRepository groupSettingRepository, GroupSettingMapper groupSettingMapper) {
        this.groupSettingRepository = groupSettingRepository;
        this.groupSettingMapper = groupSettingMapper;
    }

    /**
     * Return a {@link List} of {@link GroupSettingDTO} which matches the criteria from the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    public List<GroupSettingDTO> findByCriteria(GroupSettingCriteria criteria) {
        log.debug("find by criteria : {}", criteria);
        final Specification<GroupSetting> specification = createSpecification(criteria);
        return groupSettingMapper.toDto(groupSettingRepository.findAll(specification));
    }

    /**
     * Return a {@link Page} of {@link GroupSettingDTO} which matches the criteria from the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @param page The page, which should be returned.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    public Page<GroupSettingDTO> findByCriteria(GroupSettingCriteria criteria, Pageable page) {
        log.debug("find by criteria : {}, page: {}", criteria, page);
        final Specification<GroupSetting> specification = createSpecification(criteria);
        return groupSettingRepository.findAll(specification, page).map(groupSettingMapper::toDto);
    }

    /**
     * Return the number of matching entities in the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the number of matching entities.
     */
    @Transactional(readOnly = true)
    public long countByCriteria(GroupSettingCriteria criteria) {
        log.debug("count by criteria : {}", criteria);
        final Specification<GroupSetting> specification = createSpecification(criteria);
        return groupSettingRepository.count(specification);
    }

    /**
     * Function to convert {@link GroupSettingCriteria} to a {@link Specification}
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching {@link Specification} of the entity.
     */
    protected Specification<GroupSetting> createSpecification(GroupSettingCriteria criteria) {
        Specification<GroupSetting> specification = Specification.where(null);
        if (criteria != null) {
            if (criteria.getId() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getId(), GroupSetting_.id));
            }
            if (criteria.getRequestGroup() != null) {
                specification = specification.and(buildStringSpecification(criteria.getRequestGroup(), GroupSetting_.requestGroup));
            }
            if (criteria.getPropId() != null) {
                specification =
                    specification.and(
                        buildSpecification(
                            criteria.getPropId(),
                            root -> root.join(GroupSetting_.props, JoinType.LEFT).get(SettingProperty_.id)
                        )
                    );
            }
        }
        return specification;
    }
}
