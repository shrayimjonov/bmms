package uz.paykap.service;

import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import uz.paykap.service.dto.PropAndValue;
import uz.paykap.service.dto.SettingDTO;

/**
 * Service Interface for managing {@link uz.paykap.domain.Setting}.
 */
public interface SettingService {
    String SETTING_SYSTEM_GLOBAL = "system_global";

    /**
     * Save a setting.
     *
     * @param settingDTO the entity to save.
     * @return the persisted entity.
     */
    SettingDTO save(SettingDTO settingDTO);

    SettingDTO getGlobalSetting();

    //    SettingDTO getOrCreate(String shortName);

    SettingDTO getOrCreate(String shortName, PropAndValue... props);

    /**
     * Partially updates a setting.
     *
     * @param settingDTO the entity to update partially.
     * @return the persisted entity.
     */
    Optional<SettingDTO> partialUpdate(SettingDTO settingDTO);

    /**
     * Get all the settings.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    Page<SettingDTO> findAll(Pageable pageable);

    /**
     * Get all the settings with eager load of many-to-many relationships.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    Page<SettingDTO> findAllWithEagerRelationships(Pageable pageable);

    /**
     * Get the "id" setting.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Optional<SettingDTO> findOne(Long id);

    /**
     * Delete the "id" setting.
     *
     * @param id the id of the entity.
     */
    void delete(Long id);
}
