package uz.paykap.service;

import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.paykap.domain.FieldValue;
import uz.paykap.repository.FieldValueRepository;
import uz.paykap.service.dto.FieldValueDTO;
import uz.paykap.service.mapper.FieldValueMapper;

/**
 * Service Implementation for managing {@link FieldValue}.
 */
@Service
@Transactional
public class FieldValueService {

    private final Logger log = LoggerFactory.getLogger(FieldValueService.class);

    private final FieldValueRepository fieldValueRepository;

    private final FieldValueMapper fieldValueMapper;

    public FieldValueService(FieldValueRepository fieldValueRepository, FieldValueMapper fieldValueMapper) {
        this.fieldValueRepository = fieldValueRepository;
        this.fieldValueMapper = fieldValueMapper;
    }

    /**
     * Save a fieldValue.
     *
     * @param fieldValueDTO the entity to save.
     * @return the persisted entity.
     */
    public FieldValueDTO save(FieldValueDTO fieldValueDTO) {
        log.debug("Request to save FieldValue : {}", fieldValueDTO);
        FieldValue fieldValue = fieldValueMapper.toEntity(fieldValueDTO);
        fieldValue = fieldValueRepository.save(fieldValue);
        return fieldValueMapper.toDto(fieldValue);
    }

    /**
     * Partially update a fieldValue.
     *
     * @param fieldValueDTO the entity to update partially.
     * @return the persisted entity.
     */
    public Optional<FieldValueDTO> partialUpdate(FieldValueDTO fieldValueDTO) {
        log.debug("Request to partially update FieldValue : {}", fieldValueDTO);

        return fieldValueRepository
            .findById(fieldValueDTO.getId())
            .map(
                existingFieldValue -> {
                    fieldValueMapper.partialUpdate(existingFieldValue, fieldValueDTO);

                    return existingFieldValue;
                }
            )
            .map(fieldValueRepository::save)
            .map(fieldValueMapper::toDto);
    }

    /**
     * Get all the fieldValues.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    @Transactional(readOnly = true)
    public Page<FieldValueDTO> findAll(Pageable pageable) {
        log.debug("Request to get all FieldValues");
        return fieldValueRepository.findAll(pageable).map(fieldValueMapper::toDto);
    }

    /**
     * Get all the fieldValues with eager load of many-to-many relationships.
     *
     * @return the list of entities.
     */
    public Page<FieldValueDTO> findAllWithEagerRelationships(Pageable pageable) {
        return fieldValueRepository.findAllWithEagerRelationships(pageable).map(fieldValueMapper::toDto);
    }

    /**
     * Get one fieldValue by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    public Optional<FieldValueDTO> findOne(Long id) {
        log.debug("Request to get FieldValue : {}", id);
        return fieldValueRepository.findOneWithEagerRelationships(id).map(fieldValueMapper::toDto);
    }

    /**
     * Delete the fieldValue by id.
     *
     * @param id the id of the entity.
     */
    public void delete(Long id) {
        log.debug("Request to delete FieldValue : {}", id);
        fieldValueRepository.deleteById(id);
    }

    public void deleteCascaded(Long id) {
        log.debug("Request to delete FieldValue : {}", id);
        fieldValueRepository.deleteCascaded(id);
    }

    public FieldValue getValueOrCreate(FieldValue fieldValue) {
        log.debug("Request to get or create fieldValue : {}", fieldValue);
        return fieldValueRepository
            .findTop1ByFieldIdAndValue(fieldValue.getField().getId(), fieldValue.getValue())
            .orElseGet(() -> fieldValueRepository.save(fieldValue));
    }
}
