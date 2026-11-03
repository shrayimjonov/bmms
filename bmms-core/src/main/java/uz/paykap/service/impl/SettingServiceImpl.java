package uz.paykap.service.impl;

import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.paykap.domain.Setting;
import uz.paykap.repository.SettingRepository;
import uz.paykap.service.SettingService;
import uz.paykap.service.dto.PropAndValue;
import uz.paykap.service.dto.SettingDTO;
import uz.paykap.service.mapper.SettingMapper;

/**
 * Service Implementation for managing {@link Setting}.
 */
@Service
@Transactional
public class SettingServiceImpl implements SettingService {

    private final Logger log = LoggerFactory.getLogger(SettingServiceImpl.class);

    private final SettingRepository settingRepository;

    private final SettingMapper settingMapper;

    public SettingServiceImpl(SettingRepository settingRepository, SettingMapper settingMapper) {
        this.settingRepository = settingRepository;
        this.settingMapper = settingMapper;
    }

    @Override
    public SettingDTO save(SettingDTO settingDTO) {
        log.debug("Request to save Setting : {}", settingDTO);
        Setting setting = settingMapper.toEntity(settingDTO);
        setting = settingRepository.save(setting);
        return settingMapper.toDto(setting);
    }

    public SettingDTO getGlobalSetting() {
        return getOrCreate(SETTING_SYSTEM_GLOBAL);
    }

    @Override
    public SettingDTO getOrCreate(String shortName, PropAndValue... props) {
        Optional<Setting> top1ByShortName = settingRepository.findTop1ByShortName(shortName);

        if (top1ByShortName.isPresent()) {
            return settingMapper.toDto(top1ByShortName.get());
        }

        Setting setting = new Setting().active(true).ord(1000).shortName(shortName);

        for (PropAndValue prop : props) {
            if (prop.getProp() == null || prop.getValue() == null) {
                continue;
            }
            setting.addProp(prop.getSettingProperty());
        }

        return settingMapper.toDto(settingRepository.save(setting));
    }

    @Override
    public Optional<SettingDTO> partialUpdate(SettingDTO settingDTO) {
        log.debug("Request to partially update Setting : {}", settingDTO);

        return settingRepository
            .findById(settingDTO.getId())
            .map(
                existingSetting -> {
                    settingMapper.partialUpdate(existingSetting, settingDTO);

                    return existingSetting;
                }
            )
            .map(settingRepository::save)
            .map(settingMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SettingDTO> findAll(Pageable pageable) {
        log.debug("Request to get all Settings");
        return settingRepository.findAll(pageable).map(settingMapper::toDto);
    }

    public Page<SettingDTO> findAllWithEagerRelationships(Pageable pageable) {
        return settingRepository.findAllWithEagerRelationships(pageable).map(settingMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<SettingDTO> findOne(Long id) {
        log.debug("Request to get Setting : {}", id);
        return settingRepository.findOneWithEagerRelationships(id).map(settingMapper::toDto);
    }

    @Override
    public void delete(Long id) {
        log.debug("Request to delete Setting : {}", id);
        settingRepository.deleteById(id);
    }
}
