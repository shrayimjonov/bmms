package uz.paykap.service;

import uz.paykap.domain.PcBin;
import uz.paykap.repository.PcBinRepository;
import uz.paykap.service.dto.PcBinDTO;
import uz.paykap.service.mapper.PcBinMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Service Implementation for managing PcBin.
 */
@Service
@Transactional
public class PcBinService {

    private final Logger log = LoggerFactory.getLogger(PcBinService.class);

    private final PcBinRepository pcBinRepository;

    private final PcBinMapper pcBinMapper;

    public PcBinService(PcBinRepository pcBinRepository, PcBinMapper pcBinMapper) {
        this.pcBinRepository = pcBinRepository;
        this.pcBinMapper = pcBinMapper;
    }

    /**
     * Save a pcBin.
     *
     * @param pcBinDTO the entity to save
     * @return the persisted entity
     */
    public PcBinDTO save(PcBinDTO pcBinDTO) {
        log.debug("Request to save PcBin : {}", pcBinDTO);

        PcBin pcBin = pcBinMapper.toEntity(pcBinDTO);
        pcBin = pcBinRepository.save(pcBin);
        return pcBinMapper.toDto(pcBin);
    }

    /**
     * Get all the pcBins.
     *
     * @param pageable the pagination information
     * @return the list of entities
     */
    @Transactional(readOnly = true)
    public Page<PcBinDTO> findAll(Pageable pageable) {
        log.debug("Request to get all PcBins");
        return pcBinRepository.findAll(pageable)
            .map(pcBinMapper::toDto);
    }


    /**
     * Get one pcBin by id.
     *
     * @param id the id of the entity
     * @return the entity
     */
    @Transactional(readOnly = true)
    public Optional<PcBinDTO> findOne(Long id) {
        log.debug("Request to get PcBin : {}", id);
        return pcBinRepository.findById(id)
            .map(pcBinMapper::toDto);
    }

    /**
     * Delete the pcBin by id.
     *
     * @param id the id of the entity
     */
    public void delete(Long id) {
        log.debug("Request to delete PcBin : {}", id);
        pcBinRepository.deleteById(id);
    }
}
