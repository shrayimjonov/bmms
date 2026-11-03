package uz.paykap.service;

import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.paykap.domain.I18n;
import uz.paykap.repository.I18nRepository;
import uz.paykap.service.dto.I18nDTO;
import uz.paykap.service.mapper.I18nMapper;

/**
 * Service Implementation for managing {@link I18n}.
 */
@Service
@Transactional
public class I18nService {

    private final Logger log = LoggerFactory.getLogger(I18nService.class);

    private final I18nRepository i18nRepository;

    private final I18nMapper i18nMapper;

    public I18nService(I18nRepository i18nRepository, I18nMapper i18nMapper) {
        this.i18nRepository = i18nRepository;
        this.i18nMapper = i18nMapper;
    }

    /**
     * Save a i18n.
     *
     * @param i18nDTO the entity to save.
     * @return the persisted entity.
     */
    public I18nDTO save(I18nDTO i18nDTO) {
        log.debug("Request to save I18n : {}", i18nDTO);
        I18n i18n = i18nMapper.toEntity(i18nDTO);
        i18n = i18nRepository.save(i18n);
        return i18nMapper.toDto(i18n);
    }

    /**
     * Partially update a i18n.
     *
     * @param i18nDTO the entity to update partially.
     * @return the persisted entity.
     */
    public Optional<I18nDTO> partialUpdate(I18nDTO i18nDTO) {
        log.debug("Request to partially update I18n : {}", i18nDTO);

        return i18nRepository
            .findById(i18nDTO.getId())
            .map(
                existingI18n -> {
                    i18nMapper.partialUpdate(existingI18n, i18nDTO);

                    return existingI18n;
                }
            )
            .map(i18nRepository::save)
            .map(i18nMapper::toDto);
    }

    /**
     * Get all the i18ns.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    @Transactional(readOnly = true)
    public Page<I18nDTO> findAll(Pageable pageable) {
        log.debug("Request to get all I18ns");
        return i18nRepository.findAll(pageable).map(i18nMapper::toDto);
    }

    /**
     * Get one i18n by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    public Optional<I18nDTO> findOne(Long id) {
        log.debug("Request to get I18n : {}", id);
        return i18nRepository.findById(id).map(i18nMapper::toDto);
    }

    /**
     * Delete the i18n by id.
     *
     * @param id the id of the entity.
     */
    public void delete(Long id) {
        log.debug("Request to delete I18n : {}", id);
        i18nRepository.deleteById(id);
    }
}
