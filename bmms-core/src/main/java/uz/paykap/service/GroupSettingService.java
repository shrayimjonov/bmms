package uz.paykap.service;

import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.javlon.svgate.config.SvgateProperties;
import uz.paykap.domain.GroupSetting;
import uz.paykap.domain.SettingProperty;
import uz.paykap.domain.enumeration.Prop;
import uz.paykap.repository.GroupSettingRepository;
import uz.paykap.service.dto.GroupSettingDTO;
import uz.paykap.service.mapper.GroupSettingMapper;

/**
 * Service Implementation for managing {@link GroupSetting}.
 */
@Service
@Transactional
public class GroupSettingService {

    private final Logger log = LoggerFactory.getLogger(GroupSettingService.class);

    private final GroupSettingRepository groupSettingRepository;
    private final GroupSettingMapper groupSettingMapper;
//    private final SettingTypeRepository settingTypeRepository;
    private final SvgateProperties svgateProperties;

    public GroupSettingService(
        GroupSettingRepository groupSettingRepository,
        GroupSettingMapper groupSettingMapper,
//        SettingTypeRepository settingTypeRepository,
        SvgateProperties svgateProperties
    ) {
        this.groupSettingRepository = groupSettingRepository;
        this.groupSettingMapper = groupSettingMapper;
//        this.settingTypeRepository = settingTypeRepository;
        this.svgateProperties = svgateProperties;
    }

    /**
     * Save a groupSetting.
     *
     * @param groupSettingDTO the entity to save.
     * @return the persisted entity.
     */
    public GroupSettingDTO save(GroupSettingDTO groupSettingDTO) {
        log.debug("Request to save GroupSetting : {}", groupSettingDTO);
        GroupSetting groupSetting = groupSettingMapper.toEntity(groupSettingDTO);
        groupSetting = groupSettingRepository.save(groupSetting);
        return groupSettingMapper.toDto(groupSetting);
    }

    public void save(List<GroupSettingDTO> groupSettingDTOS) {
        for (GroupSettingDTO settingDTO : groupSettingDTOS) {
            if (settingDTO == null) {
                continue;
            }
            log.debug("Request to save GroupSetting : {}", settingDTO);
            GroupSetting groupSetting = groupSettingMapper.toEntity(settingDTO);
            groupSetting = groupSettingRepository.save(groupSetting);
        }
    }

    /**
     * Partially update a groupSetting.
     *
     * @param groupSettingDTO the entity to update partially.
     * @return the persisted entity.
     */
    public Optional<GroupSettingDTO> partialUpdate(GroupSettingDTO groupSettingDTO) {
        log.debug("Request to partially update GroupSetting : {}", groupSettingDTO);

        return groupSettingRepository
            .findById(groupSettingDTO.getId())
            .map(
                existingGroupSetting -> {
                    groupSettingMapper.partialUpdate(existingGroupSetting, groupSettingDTO);

                    return existingGroupSetting;
                }
            )
            .map(groupSettingRepository::save)
            .map(groupSettingMapper::toDto);
    }

    /**
     * Get all the groupSettings.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    @Transactional(readOnly = true)
    public Page<GroupSettingDTO> findAll(Pageable pageable) {
        log.debug("Request to get all GroupSettings");
        return groupSettingRepository.findAll(pageable).map(groupSettingMapper::toDto);
    }

    /**
     * Get all the groupSettings with eager load of many-to-many relationships.
     *
     * @return the list of entities.
     */
    public Page<GroupSettingDTO> findAllWithEagerRelationships(Pageable pageable) {
        return groupSettingRepository.findAllWithEagerRelationships(pageable).map(groupSettingMapper::toDto);
    }

    /**
     * Get one groupSetting by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    public Optional<GroupSettingDTO> findOne(Long id) {
        log.debug("Request to get GroupSetting : {}", id);
        return groupSettingRepository.findOneWithEagerRelationships(id).map(groupSettingMapper::toDto);
    }

    @Transactional(readOnly = true)
    public Optional<GroupSetting> findOne(Long settingTypeId, String requestGroup) {
        log.debug("Request to get GroupSetting : {}, {}", settingTypeId, requestGroup);
//        return groupSettingRepository.findOne(settingTypeId, requestGroup);
        return groupSettingRepository.findOne(requestGroup);
    }

    /**
     * Delete the groupSetting by id.
     *
     * @param id the id of the entity.
     */
    public void delete(Long id) {
        log.debug("Request to delete GroupSetting : {}", id);
        groupSettingRepository.deleteById(id);
    }

    public GroupSetting createEposSettings(String settingTypeName, String requestGroup, String eposMerchant, String eposTerminal) {
        return createEposSettings(settingTypeName, requestGroup, eposMerchant, eposTerminal, String.valueOf(1234));
    }

    public GroupSetting createEposSettings(
        String settingTypeName,
        String requestGroup,
        String eposMerchant,
        String eposTerminal,
        String eposPort
    ) {
//        SettingType settingType = settingTypeRepository.save(new SettingType().name(settingTypeName));

        GroupSetting groupSetting = new GroupSetting()
            .requestGroup(requestGroup)
//            .settingType(settingType)
            .addProp(new SettingProperty().key(Prop.UZCARD_MERCHANT).value(eposMerchant))
            .addProp(new SettingProperty().key(Prop.UZCARD_TERMINAL).value(eposTerminal))
            .addProp(new SettingProperty().key(Prop.UZCARD_PORT).value(eposPort));

        return groupSettingRepository.save(groupSetting);
    }
}
