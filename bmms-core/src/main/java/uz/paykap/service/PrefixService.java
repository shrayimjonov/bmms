package uz.paykap.service;

import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.paykap.domain.Prefix;
import uz.paykap.repository.PrefixRepository;
import uz.paykap.service.dto.PrefixDTO;
import uz.paykap.service.mapper.PrefixMapper;

/**
 * Service Implementation for managing {@link Prefix}.
 */
@Service
@Transactional
public class PrefixService {

    private final Logger log = LoggerFactory.getLogger(PrefixService.class);

    private final PrefixRepository prefixRepository;

    private final PrefixMapper prefixMapper;

    public PrefixService(PrefixRepository prefixRepository, PrefixMapper prefixMapper) {
        this.prefixRepository = prefixRepository;
        this.prefixMapper = prefixMapper;
    }

    /**
     * Save a prefix.
     *
     * @param prefixDTO the entity to save.
     * @return the persisted entity.
     */
    public PrefixDTO save(PrefixDTO prefixDTO) {
        log.debug("Request to save Prefix : {}", prefixDTO);
        Prefix prefix = prefixMapper.toEntity(prefixDTO);
        prefix = prefixRepository.save(prefix);
        return prefixMapper.toDto(prefix);
    }

    /**
     * Partially update a prefix.
     *
     * @param prefixDTO the entity to update partially.
     * @return the persisted entity.
     */
    public Optional<PrefixDTO> partialUpdate(PrefixDTO prefixDTO) {
        log.debug("Request to partially update Prefix : {}", prefixDTO);

        return prefixRepository
            .findById(prefixDTO.getId())
            .map(
                existingPrefix -> {
                    prefixMapper.partialUpdate(existingPrefix, prefixDTO);

                    return existingPrefix;
                }
            )
            .map(prefixRepository::save)
            .map(prefixMapper::toDto);
    }

    /**
     * Get all the prefixes.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    @Transactional(readOnly = true)
    public Page<PrefixDTO> findAll(Pageable pageable) {
        log.debug("Request to get all Prefixes");
        return prefixRepository.findAll(pageable).map(prefixMapper::toDto);
    }

    /**
     * Get one prefix by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    public Optional<PrefixDTO> findOne(Long id) {
        log.debug("Request to get Prefix : {}", id);
        return prefixRepository.findById(id).map(prefixMapper::toDto);
    }

    /**
     * Delete the prefix by id.
     *
     * @param id the id of the entity.
     */
    public void delete(Long id) {
        log.debug("Request to delete Prefix : {}", id);
        prefixRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<PrefixDTO> findAll() {
        log.debug("Request to get all Prefixes");
        return prefixMapper.toDto(prefixRepository.findAll());
    }
}
