package uz.paykap.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.jhipster.service.QueryService;
import uz.paykap.domain.Setting;
import uz.paykap.domain.SettingProperty_;
import uz.paykap.domain.Setting_;
import uz.paykap.repository.SettingRepository;
import uz.paykap.service.criteria.SettingCriteria;
import uz.paykap.service.dto.SettingDTO;
import uz.paykap.service.mapper.SettingMapper;

import javax.persistence.criteria.JoinType;
import java.util.List;

/**
 * Service for executing complex queries for {@link Setting} entities in the database.
 * The main input is a {@link SettingCriteria} which gets converted to {@link Specification},
 * in a way that all the filters must apply.
 * It returns a {@link List} of {@link SettingDTO} or a {@link Page} of {@link SettingDTO} which fulfills the criteria.
 */
@Service
@Transactional(readOnly = true)
public class SettingQueryService extends QueryService<Setting> {

    private final Logger log = LoggerFactory.getLogger(SettingQueryService.class);

    private final SettingRepository settingRepository;

    private final SettingMapper settingMapper;

    public SettingQueryService(SettingRepository settingRepository, SettingMapper settingMapper) {
        this.settingRepository = settingRepository;
        this.settingMapper = settingMapper;
    }

    /**
     * Return a {@link List} of {@link SettingDTO} which matches the criteria from the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    public List<SettingDTO> findByCriteria(SettingCriteria criteria) {
        log.debug("find by criteria : {}", criteria);
        final Specification<Setting> specification = createSpecification(criteria);
        return settingMapper.toDto(settingRepository.findAll(specification));
    }

    /**
     * Return a {@link Page} of {@link SettingDTO} which matches the criteria from the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @param page The page, which should be returned.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    public Page<SettingDTO> findByCriteria(SettingCriteria criteria, Pageable page) {
        log.debug("find by criteria : {}, page: {}", criteria, page);
        final Specification<Setting> specification = createSpecification(criteria);
        return settingRepository.findAll(specification, page).map(settingMapper::toDto);
    }

    /**
     * Return the number of matching entities in the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the number of matching entities.
     */
    @Transactional(readOnly = true)
    public long countByCriteria(SettingCriteria criteria) {
        log.debug("count by criteria : {}", criteria);
        final Specification<Setting> specification = createSpecification(criteria);
        return settingRepository.count(specification);
    }

    /**
     * Function to convert {@link SettingCriteria} to a {@link Specification}
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching {@link Specification} of the entity.
     */
    protected Specification<Setting> createSpecification(SettingCriteria criteria) {
        Specification<Setting> specification = Specification.where(null);
        if (criteria != null) {
            if (criteria.getId() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getId(), Setting_.id));
            }
            if (criteria.getShortName() != null) {
                specification = specification.and(buildStringSpecification(criteria.getShortName(), Setting_.shortName));
            }
            if (criteria.getOrd() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getOrd(), Setting_.ord));
            }
            if (criteria.getActive() != null) {
                specification = specification.and(buildSpecification(criteria.getActive(), Setting_.active));
            }
            if (criteria.getPropId() != null) {
                specification =
                    specification.and(
                        buildSpecification(criteria.getPropId(), root -> root.join(Setting_.props, JoinType.LEFT).get(SettingProperty_.id))
                    );
            }
        }
        return specification;
    }
}
