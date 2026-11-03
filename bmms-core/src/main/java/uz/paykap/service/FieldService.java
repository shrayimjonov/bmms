package uz.paykap.service;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.Optional;
import java.util.Optional;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.devops.bmcore.enumeration.ControlType;
import uz.devops.bmcore.enumeration.FieldType;
import uz.devops.bmcore.enumeration.Visibility;
import uz.paykap.domain.Field;
import uz.paykap.domain.I18n;
import uz.paykap.domain.Recipient;
import uz.paykap.repository.FieldRepository;
import uz.paykap.repository.FieldValueRepository;
import uz.paykap.repository.I18nRepository;
import uz.paykap.repository.PrefixRepository;
import uz.paykap.service.dto.FieldDTO;
import uz.paykap.service.dto.RecipientDTO;
import uz.paykap.service.mapper.FieldMapper;
import uz.paykap.utils.I18nUtil;

/**
 * Service Implementation for managing {@link Field}.
 */
@Slf4j
@Service
@Transactional
@AllArgsConstructor
public class FieldService {

    private final FieldRepository fieldRepository;
    private final FieldMapper fieldMapper;
    private final I18nRepository i18nRepository;
    private final PrefixRepository prefixRepository;
    private final FieldValueRepository fieldValueRepository;

    /**
     * Save a field.
     *
     * @param fieldDTO the entity to save.
     * @return the persisted entity.
     */
    public FieldDTO save(FieldDTO fieldDTO) {
        log.debug("Request to save Field : {}", fieldDTO);
        Field field = fieldMapper.toEntity(fieldDTO);
        field = fieldRepository.save(field);
        i18nRepository.saveAll(field.getTitles());
        i18nRepository.saveAll(field.getDescriptions());
        prefixRepository.saveAll(field.getPrefixes());
        fieldValueRepository.saveAll(field.getValues());

        return fieldMapper.toDto(field);
    }

    /**
     * Partially update a field.
     *
     * @param fieldDTO the entity to update partially.
     * @return the persisted entity.
     */
    public Optional<FieldDTO> partialUpdate(FieldDTO fieldDTO) {
        log.debug("Request to partially update Field : {}", fieldDTO);

        return fieldRepository
            .findById(fieldDTO.getId())
            .map(
                existingField -> {
                    fieldMapper.partialUpdate(existingField, fieldDTO);

                    return existingField;
                }
            )
            .map(fieldRepository::save)
            .map(fieldMapper::toDto);
    }

    /**
     * Get all the fields.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    @Transactional(readOnly = true)
    public Page<FieldDTO> findAll(Pageable pageable) {
        log.debug("Request to get all Fields");
        return fieldRepository.findAll(pageable).map(fieldMapper::toDto);
    }

    /**
     * Get all the fields with eager load of many-to-many relationships.
     *
     * @return the list of entities.
     */
    public Page<FieldDTO> findAllWithEagerRelationships(Pageable pageable) {
        return fieldRepository.findAllWithEagerRelationships(pageable).map(fieldMapper::toDto);
    }

    /**
     * Get one field by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    public Optional<FieldDTO> findOne(Long id) {
        log.debug("Request to get Field : {}", id);
        return fieldRepository.findOneWithEagerRelationships(id).map(fieldMapper::toDto);
    }

    /**
     * Delete the field by id.
     *
     * @param id the id of the entity.
     */
    public void delete(Long id) {
        log.debug("Request to delete Field : {}", id);
        fieldRepository.deleteById(id);
    }

    public void deleteCascaded(Long id) {
        log.debug("Request to delete Field : {}", id);
        fieldRepository.deleteCascaded(id);
    }

    @Transactional(readOnly = true)
    public List<FieldDTO> findAll() {
        log.debug("Request to get all Fields");

        return fieldMapper.toDto(fieldRepository.findAll());
    }

    @Transactional(readOnly = true)
    public List<FieldDTO> findAllByRecipient(RecipientDTO recipientDTO) {
        log.debug("Request to get Field : {}", recipientDTO);
        return fieldMapper.toDto(Lists.newArrayList(fieldRepository.findAllByRecipientId(recipientDTO.getId())));
    }

    @Transactional(readOnly = true)
    public List<FieldDTO> findAllByRecipientId(Long id) {
        log.debug("Request to get Field from service by: {}", id);
        return fieldMapper.toDto(Lists.newArrayList(fieldRepository.findAllByRecipientId(id)));
    }

    public Field getFieldOrCreate(Field field) {
        log.debug("Getting Field or create : {}", field);
        return fieldRepository
            .findTop1ByNameAndRecipient(field.getName(), field.getRecipient())
            .orElseGet(() -> fieldRepository.save(field));
    }

    public Set<Field> getFieldsByRecipient(RecipientDTO recipientDTO) {
        log.debug("Request to get Field : {}", recipientDTO);
        return fieldRepository.findAllByRecipientId(recipientDTO.getId());
    }

    public Field saveField(Recipient recipient, String name, String en, String ru, String uz, FieldType fieldType, int ord) {
        Set<I18n> i18nsDesc = I18nUtil.getI18ns("Input" + en, "Введите " + ru, uz + "kiriting");
        Set<I18n> i18nsTitle = I18nUtil.getI18ns(en, ru, uz);

        i18nRepository.saveAll(i18nsTitle);
        i18nRepository.saveAll(i18nsDesc);

        Field field = new Field()
            .name(name)
            .extId(0L)
            .descriptions(i18nsDesc)
            .type(fieldType)
            .controlType(ControlType.TEXT)
            .maskSymbol("*")
            .lengthMin(1)
            .lengthMax(30)
            .optional(false)
            .output(true)
            .visibility(Visibility.ALL)
            .ord(ord)
            .recipient(recipient)
            .titles(i18nsTitle);

        field = fieldRepository.save(field);

        return field;
    }
}
