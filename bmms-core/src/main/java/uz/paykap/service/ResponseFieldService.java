package uz.paykap.service;

import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.paykap.domain.ResponseField;
import uz.paykap.repository.ResponseFieldRepository;
import uz.paykap.service.dto.ResponseFieldDTO;
import uz.paykap.service.mapper.ResponseFieldMapper;

/**
 * Service Implementation for managing {@link ResponseField}.
 */
@Service
@Transactional
public class ResponseFieldService {

    private final Logger log = LoggerFactory.getLogger(ResponseFieldService.class);

    private final ResponseFieldRepository responseFieldRepository;

    private final ResponseFieldMapper responseFieldMapper;

    public ResponseFieldService(ResponseFieldRepository responseFieldRepository, ResponseFieldMapper responseFieldMapper) {
        this.responseFieldRepository = responseFieldRepository;
        this.responseFieldMapper = responseFieldMapper;
    }

    /**
     * Save a responseField.
     *
     * @param responseFieldDTO the entity to save.
     * @return the persisted entity.
     */
    public ResponseFieldDTO save(ResponseFieldDTO responseFieldDTO) {
        log.debug("Request to save ResponseField : {}", responseFieldDTO);
        ResponseField responseField = responseFieldMapper.toEntity(responseFieldDTO);
        responseField = responseFieldRepository.save(responseField);
        return responseFieldMapper.toDto(responseField);
    }

    /**
     * Partially update a responseField.
     *
     * @param responseFieldDTO the entity to update partially.
     * @return the persisted entity.
     */
    public Optional<ResponseFieldDTO> partialUpdate(ResponseFieldDTO responseFieldDTO) {
        log.debug("Request to partially update ResponseField : {}", responseFieldDTO);

        return responseFieldRepository
            .findById(responseFieldDTO.getId())
            .map(
                existingResponseField -> {
                    responseFieldMapper.partialUpdate(existingResponseField, responseFieldDTO);

                    return existingResponseField;
                }
            )
            .map(responseFieldRepository::save)
            .map(responseFieldMapper::toDto);
    }

    /**
     * Get all the responseFields.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    @Transactional(readOnly = true)
    public Page<ResponseFieldDTO> findAll(Pageable pageable) {
        log.debug("Request to get all ResponseFields");
        return responseFieldRepository.findAll(pageable).map(responseFieldMapper::toDto);
    }

    /**
     * Get all the responseFields with eager load of many-to-many relationships.
     *
     * @return the list of entities.
     */
    public Page<ResponseFieldDTO> findAllWithEagerRelationships(Pageable pageable) {
        return responseFieldRepository.findAllWithEagerRelationships(pageable).map(responseFieldMapper::toDto);
    }

    /**
     * Get one responseField by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    public Optional<ResponseFieldDTO> findOne(Long id) {
        log.debug("Request to get ResponseField : {}", id);
        return responseFieldRepository.findOneWithEagerRelationships(id).map(responseFieldMapper::toDto);
    }

    /**
     * Delete the responseField by id.
     *
     * @param id the id of the entity.
     */
    public void delete(Long id) {
        log.debug("Request to delete ResponseField : {}", id);
        responseFieldRepository.deleteById(id);
    }
}
