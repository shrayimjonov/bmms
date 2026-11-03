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

import uz.paykap.domain.PcBin;
import uz.paykap.domain.*; // for static metamodels
import uz.paykap.repository.PcBinRepository;
import uz.paykap.service.dto.PcBinCriteria;
import uz.paykap.service.dto.PcBinDTO;
import uz.paykap.service.mapper.PcBinMapper;

/**
 * Service for executing complex queries for PcBin entities in the database.
 * The main input is a {@link PcBinCriteria} which gets converted to {@link Specification},
 * in a way that all the filters must apply.
 * It returns a {@link List} of {@link PcBinDTO} or a {@link Page} of {@link PcBinDTO} which fulfills the criteria.
 */
@Service
@Transactional(readOnly = true)
public class PcBinQueryService extends QueryService<PcBin> {

    private final Logger log = LoggerFactory.getLogger(PcBinQueryService.class);

    private final PcBinRepository pcBinRepository;

    private final PcBinMapper pcBinMapper;

    public PcBinQueryService(PcBinRepository pcBinRepository, PcBinMapper pcBinMapper) {
        this.pcBinRepository = pcBinRepository;
        this.pcBinMapper = pcBinMapper;
    }

    /**
     * Return a {@link List} of {@link PcBinDTO} which matches the criteria from the database
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    public List<PcBinDTO> findByCriteria(PcBinCriteria criteria) {
        log.debug("find by criteria : {}", criteria);
        final Specification<PcBin> specification = createSpecification(criteria);
        return pcBinMapper.toDto(pcBinRepository.findAll(specification));
    }

    /**
     * Return a {@link Page} of {@link PcBinDTO} which matches the criteria from the database
     * @param criteria The object which holds all the filters, which the entities should match.
     * @param page The page, which should be returned.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    public Page<PcBinDTO> findByCriteria(PcBinCriteria criteria, Pageable page) {
        log.debug("find by criteria : {}, page: {}", criteria, page);
        final Specification<PcBin> specification = createSpecification(criteria);
        return pcBinRepository.findAll(specification, page)
            .map(pcBinMapper::toDto);
    }

    /**
     * Return the number of matching entities in the database
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the number of matching entities.
     */
    @Transactional(readOnly = true)
    public long countByCriteria(PcBinCriteria criteria) {
        log.debug("count by criteria : {}", criteria);
        final Specification<PcBin> specification = createSpecification(criteria);
        return pcBinRepository.count(specification);
    }

    /**
     * Function to convert PcBinCriteria to a {@link Specification}
     */
    private Specification<PcBin> createSpecification(PcBinCriteria criteria) {
        Specification<PcBin> specification = Specification.where(null);
        if (criteria != null) {
            if (criteria.getId() != null) {
                specification = specification.and(buildSpecification(criteria.getId(), PcBin_.id));
            }
            if (criteria.getBin() != null) {
                specification = specification.and(buildStringSpecification(criteria.getBin(), PcBin_.bin));
            }
            if (criteria.getBankName() != null) {
                specification = specification.and(buildStringSpecification(criteria.getBankName(), PcBin_.bankName));
            }
            if (criteria.getPcType() != null) {
                specification = specification.and(buildSpecification(criteria.getPcType(), PcBin_.pcType));
            }
            if (criteria.getUseCredit() != null) {
                specification = specification.and(buildSpecification(criteria.getUseCredit(), PcBin_.useCredit));
            }
            if (criteria.getUseDebit() != null) {
                specification = specification.and(buildSpecification(criteria.getUseDebit(), PcBin_.useDebit));
            }
            if (criteria.getUseReverse() != null) {
                specification = specification.and(buildSpecification(criteria.getUseReverse(), PcBin_.useReverse));
            }
            if (criteria.getUseReconcile() != null) {
                specification = specification.and(buildSpecification(criteria.getUseReconcile(), PcBin_.useReconcile));
            }
            if (criteria.getComment() != null) {
                specification = specification.and(buildStringSpecification(criteria.getComment(), PcBin_.comment));
            }
        }
        return specification;
    }
}
