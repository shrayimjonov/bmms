package uz.devops.bm.service;

import org.springframework.data.domain.Pageable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import uz.devops.bm.domain.JsonForm;
import uz.devops.bm.service.criteria.JsonFormCriteria;
import uz.devops.bm.service.dto.JsonFormDTO;

/**
 * Service Interface for managing {@link uz.devops.bm.domain.JsonForm}.
 */
public interface JsonFormService {
    /**
     * Save a jsonForm.
     *
     * @param jsonFormDTO the entity to save.
     * @return the persisted entity.
     */
    Mono<JsonFormDTO> save(JsonFormDTO jsonFormDTO);

    /**
     * Partially updates a jsonForm.
     *
     * @param jsonFormDTO the entity to update partially.
     * @return the persisted entity.
     */
    Mono<JsonFormDTO> partialUpdate(JsonFormDTO jsonFormDTO);

    /**
     * Get all the jsonForms.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    Flux<JsonFormDTO> findAll(Pageable pageable);

    /**
     * Get all the jsonForms with eager load of many-to-many relationships.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    Flux<JsonFormDTO> findAllWithEagerRelationships(Pageable pageable);

    /**
     * Returns the number of jsonForms available.
     *
     * @return the number of entities in the database.
     */
    Mono<Long> countAll();

    /**
     * Get the "id" jsonForm.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Mono<JsonFormDTO> findOne(Long id);

    /**
     * Delete the "id" jsonForm.
     *
     * @param id the id of the entity.
     * @return a Mono to signal the deletion
     */
    Mono<Void> delete(Long id);

    Mono<Long> countByCriteria(JsonFormCriteria criteria);

    Flux<JsonFormDTO> findByCriteria(JsonFormCriteria criteria, Pageable pageable);
}
