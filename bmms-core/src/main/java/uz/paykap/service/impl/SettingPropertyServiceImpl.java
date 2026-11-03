package uz.paykap.service.impl;

import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.paykap.domain.SettingProperty;
import uz.paykap.repository.SettingPropertyRepository;
import uz.paykap.service.SettingPropertyService;
import uz.paykap.service.dto.SettingPropertyDTO;
import uz.paykap.service.mapper.SettingPropertyMapper;

/**
 * Service Implementation for managing {@link SettingProperty}.
 */
@Service
@Transactional
public class SettingPropertyServiceImpl implements SettingPropertyService {

    private final Logger log = LoggerFactory.getLogger(SettingPropertyServiceImpl.class);

    private final SettingPropertyRepository settingPropertyRepository;

    private final SettingPropertyMapper settingPropertyMapper;

    public SettingPropertyServiceImpl(SettingPropertyRepository settingPropertyRepository, SettingPropertyMapper settingPropertyMapper) {
        this.settingPropertyRepository = settingPropertyRepository;
        this.settingPropertyMapper = settingPropertyMapper;
    }

    @Override
    public SettingPropertyDTO save(SettingPropertyDTO settingPropertyDTO) {
        log.debug("Request to save SettingProperty : {}", settingPropertyDTO);
        SettingProperty settingProperty = settingPropertyMapper.toEntity(settingPropertyDTO);
        settingProperty = settingPropertyRepository.save(settingProperty);
        return settingPropertyMapper.toDto(settingProperty);
    }

    @Override
    public Optional<SettingPropertyDTO> partialUpdate(SettingPropertyDTO settingPropertyDTO) {
        log.debug("Request to partially update SettingProperty : {}", settingPropertyDTO);

        return settingPropertyRepository
            .findById(settingPropertyDTO.getId())
            .map(
                existingSettingProperty -> {
                    settingPropertyMapper.partialUpdate(existingSettingProperty, settingPropertyDTO);

                    return existingSettingProperty;
                }
            )
            .map(settingPropertyRepository::save)
            .map(settingPropertyMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SettingPropertyDTO> findAll(Pageable pageable) {
        log.debug("Request to get all SettingProperties");
        return settingPropertyRepository.findAll(pageable).map(settingPropertyMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<SettingPropertyDTO> findOne(Long id) {
        log.debug("Request to get SettingProperty : {}", id);
        return settingPropertyRepository.findById(id).map(settingPropertyMapper::toDto);
    }

    @Override
    public void delete(Long id) {
        log.debug("Request to delete SettingProperty : {}", id);
        settingPropertyRepository.deleteById(id);
    }
}
