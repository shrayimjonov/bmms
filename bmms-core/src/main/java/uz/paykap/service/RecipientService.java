package uz.paykap.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.devops.bmcore.enumeration.FieldType;
import uz.devops.libs.keycloak.admin.service.KeycloakAdminService;
import uz.devops.libs.keycloak.admin.service.dto.AdminUserGroupDTO;
import uz.devops.libs.keycloak.admin.service.dto.KUserDTO;
import uz.javlon.commons.base.Result;
import uz.javlon.commons.exception.CodedException;
import uz.javlon.commons.exception.ProblemEnum;
import uz.paykap.client.uaa.UaaClient;
import uz.paykap.config.ApplicationProperties;
import uz.paykap.domain.Recipient;
import uz.paykap.domain.SimpleRecipient;
import uz.paykap.repository.RecipientRepository;
import uz.paykap.repository.SimpleRecipientRepository;
import uz.paykap.security.AuthoritiesConstants;
import uz.paykap.security.SecurityUtils;
import uz.paykap.service.cached.GatewaySelectorService;
import uz.paykap.service.dto.KeyWithNames;
import uz.paykap.service.dto.RecipientDTO;
import uz.paykap.service.mapper.RecipientMapper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Service Implementation for managing {@link Recipient}.
 */
@Slf4j
@Service
@AllArgsConstructor
@Transactional
public class RecipientService {

    private final RecipientRepository recipientRepository;
    private final RecipientMapper recipientMapper;
    private final SimpleRecipientRepository simpleRecipientRepository;
    private final GatewaySelectorService gatewaySelectorService;
    private final FieldService fieldService;
    private final UaaClient uaaClient;
    private final ApplicationProperties applicationProperties;
    private final KeycloakAdminService keycloakAdminService;

    /**
     * Save a recipient.
     *
     * @param recipientDTO the entity to save.
     * @return the persisted entity.
     */
    public RecipientDTO save(RecipientDTO recipientDTO) {
        log.debug("Request to save Recipient : {}", recipientDTO);
        Recipient recipient = recipientMapper.toEntity(recipientDTO);
        recipient = recipientRepository.save(recipient);

        if (recipientDTO.getId() == null) {
            // If new recipient
            fieldService.saveField(recipient, "account", "Account number", "Номер счета", "Xisob Raqami", FieldType.ACCOUNT, 1);
            fieldService.saveField(recipient, "amount", "Amount", "Сумма", "Summa", FieldType.AMOUNT, 2);
        }

        gatewaySelectorService.evictAll();

        KUserDTO user = new KUserDTO();
        user.setPassword(applicationProperties.getUaaUserDefaultPassword());
        user.setFirstName(recipient.getShortName());
        user.setLastName(recipient.getShortName());
        user.setUsername(recipient.getShortName());
        user.setEmail(recipient.getShortName() + "@gmail.com");
        user.setEnabled(true);
        user.setTemporary(false);
        var recGroup = new AdminUserGroupDTO();
        recGroup.setName("Recipients");
        user.setUserGroups(Collections.singleton(recGroup));
        keycloakAdminService.createUser(user);
        return recipientMapper.toDto(recipient);
    }

    /**
     * Partially update a recipient.
     *
     * @param recipientDTO the entity to update partially.
     * @return the persisted entity.
     */
    public Optional<RecipientDTO> partialUpdate(RecipientDTO recipientDTO) {
        log.debug("Request to partially update Recipient : {}", recipientDTO);

        return recipientRepository
            .findById(recipientDTO.getId())
            .map(
                existingRecipient -> {
                    recipientMapper.partialUpdate(existingRecipient, recipientDTO);

                    return existingRecipient;
                }
            )
            .map(recipientRepository::save)
            .map(recipientMapper::toDto);
    }

    /**
     * Get all the recipients.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    @Transactional(readOnly = true)
    public Page<RecipientDTO> findAll(Pageable pageable) {
        log.debug("Request to get all Recipients");
        return recipientRepository.findAll(pageable).map(recipientMapper::toDto);
    }

    /**
     * Get all the recipients with eager load of many-to-many relationships.
     *
     * @return the list of entities.
     */
    public Page<RecipientDTO> findAllWithEagerRelationships(Pageable pageable) {
        return recipientRepository.findAllWithEagerRelationships(pageable).map(recipientMapper::toDto);
    }

    /**
     * Get one recipient by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    public Optional<RecipientDTO> findOne(Long id) {
        log.debug("Request to get Recipient : {}", id);
        return recipientRepository.findOneWithEagerRelationships(id).map(recipientMapper::toDto);
    }

    /**
     * Delete the recipient by id.
     *
     * @param id the id of the entity.
     */
    public void delete(Long id) {
        log.debug("Request to delete Recipient : {}", id);
        recipientRepository.deleteById(id);
    }

    /**
     * Get list gateways by id.
     *
     * @param id the id of the entity
     * @return the entity
     */
    @Transactional(readOnly = true)
    public List<RecipientDTO> findAllByGatewayId(Long id) {
        return recipientMapper.toDto(recipientRepository.findAllByGateway_Id(id));
    }

    /**
     * Get list gateways by gatewayName.
     *
     * @param gatewayName the gatewayName of the entity
     * @return the entity
     */
    @Transactional(readOnly = true)
    public List<RecipientDTO> findAllByGatewayName(String gatewayName) {
        return recipientMapper.toDto(recipientRepository.findAllByGatewayName(gatewayName));
    }

    public void deleteCascaded(Long id) {
        log.debug("Request to delete Recipient : {}", id);
        recipientRepository.deleteCascaded(id);
    }

    @Transactional(readOnly = true)
    public List<RecipientDTO> findAll() {
        log.debug("Request to get all Recipients");
        return recipientMapper.toDto(recipientRepository.findAll());
    }

    public Recipient getRecipientOrThrow(Long id) {
        return recipientRepository
            .findTop1ByIdAndActiveIsTrue(id)
            .orElseThrow(() -> new CodedException(ProblemEnum.SERVICE_NOT_FOUND, "recipient.id = " + id));
    }

    public Optional<Recipient> getRecipient(Long id) {
        return recipientRepository.findTop1ByIdAndActiveIsTrue(id);
    }

    public RecipientDTO getRecipientByShortName(String shortName) {
        log.debug("Request to get Recipient by short name");
        return recipientMapper.toDto(recipientRepository.getRecipientByShortName(shortName).orElse(null));
    }

    public Optional<Recipient> getRecipient(String shortName) {
        log.debug("Request to get Recipient by short name");
        return recipientRepository.getRecipientByShortName(shortName);
    }

    public List<RecipientDTO> getRecipientByInn(String inn) {
        log.debug("Request to get Recipient by inn");
        return recipientMapper.toDto(recipientRepository.findAllByInnAndActiveIsTrue(inn));
    }

    public Recipient getRecipientOrCreate(Recipient recipient) {
        log.debug("Request to get Recipient or create : {} ", recipient);
        return recipientRepository.findTop1ByShortName(recipient.getShortName()).orElseGet(() -> recipientRepository.save(recipient));
    }

    public Result updateRecipients(List<RecipientDTO> list) {
        log.debug("Request to update Recipients :{}", list.toString());
        for (RecipientDTO recipientDTO : list) {
            Recipient recipient = recipientRepository.findTop1ByShortName(recipientDTO.getShortName()).orElse(null);
            if (recipient == null) {
                continue;
            }
            recipient.setActive(recipientDTO.isActive());
            recipient.setPay(recipientDTO.isPay());
            recipient.setCheck(recipientDTO.isCheck());
            recipient.setCancel(recipientDTO.isCancel());
            recipientRepository.save(recipient);
        }
        return Result.SUCCESS;
    }

    public Page<String> getAllowedRecipientNames(Pageable pageable) {
        return recipientRepository.getAllowedRecipientNames(pageable);
    }

    public Page<SimpleRecipient> getOnlyAllowedRecipientNamesForAgentOrAllForAdmin(String recipientNamePart, Pageable pageable) {
        if (recipientNamePart == null) {
            recipientNamePart = "";
        }

        if (SecurityUtils.hasCurrentUserThisAuthority(AuthoritiesConstants.ADMIN)) {
            return simpleRecipientRepository.findOnlyAllowedV2(recipientNamePart, pageable);
        } else if (
            SecurityUtils.hasCurrentUserThisAuthority(AuthoritiesConstants.ROLE_CAN_LIST_RECIPIENTS) ||
            SecurityUtils.hasCurrentUserThisAuthority(AuthoritiesConstants.AGENT)
        ) {
            return simpleRecipientRepository.findOnlyAllowedV2(
                SecurityUtils.getCurrentUserLogin().orElseThrow(() -> new CodedException(ProblemEnum.USERNAME_NOT_FOUND)),
                recipientNamePart,
                pageable
            );
        }

        return Page.empty(pageable);
    }

    public Page<KeyWithNames> getAllowedRecipients(Pageable pageable) {
        List<Recipient> allowedRecipients;

        if (SecurityUtils.hasCurrentUserThisAuthority(AuthoritiesConstants.AGENT)) {
            allowedRecipients = recipientRepository.getAllowedRecipients(pageable);
        } else {
            allowedRecipients = recipientRepository.findAll();
        }

        ArrayList<KeyWithNames> ns = new ArrayList<>(allowedRecipients.size());

        for (Recipient allowedRecipient : allowedRecipients) {
            ns.add(new KeyWithNames(allowedRecipient.getShortName(), allowedRecipient.getTitles()));
        }

        return new PageImpl<>(ns);
    }
}
