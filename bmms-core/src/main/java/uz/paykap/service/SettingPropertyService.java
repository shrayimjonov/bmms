package uz.paykap.service;

import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import uz.paykap.service.dto.SettingPropertyDTO;

/**
 * Service Interface for managing {@link uz.paykap.domain.SettingProperty}.
 */
public interface SettingPropertyService {
    /**
     * Save a settingProperty.
     *
     * @param settingPropertyDTO the entity to save.
     * @return the persisted entity.
     */
    SettingPropertyDTO save(SettingPropertyDTO settingPropertyDTO);

    /**
     * Partially updates a settingProperty.
     *
     * @param settingPropertyDTO the entity to update partially.
     * @return the persisted entity.
     */
    Optional<SettingPropertyDTO> partialUpdate(SettingPropertyDTO settingPropertyDTO);

    /**
     * Get all the settingProperties.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    Page<SettingPropertyDTO> findAll(Pageable pageable);

    /**
     * Get the "id" settingProperty.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Optional<SettingPropertyDTO> findOne(Long id);

    /**
     * Delete the "id" settingProperty.
     *
     * @param id the id of the entity.
     */
    void delete(Long id);
}
