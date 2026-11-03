package uz.paykap.service;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.devops.bmcore.Constants;
import uz.javlon.commons.exception.CodedException;
import uz.javlon.commons.exception.ProblemEnum;
import uz.paykap.domain.VirtualAccount;
import uz.paykap.domain.enumeration.ModeType;
import uz.paykap.repository.VirtualAccountRepository;
import uz.paykap.service.dto.ChangeVaModeDTO;
import uz.paykap.service.dto.VirtualAccountDTO;
import uz.paykap.service.mapper.VirtualAccountMapper;

/**
 * Service Implementation for managing {@link VirtualAccount}.
 */
@Service
@Transactional
public class VirtualAccountService {

    private final Logger log = LoggerFactory.getLogger(VirtualAccountService.class);

    private final VirtualAccountRepository virtualAccountRepository;

    private final VirtualAccountMapper virtualAccountMapper;

    private final DepositService depositService;

    public VirtualAccountService(
        VirtualAccountRepository virtualAccountRepository,
        VirtualAccountMapper virtualAccountMapper,
        DepositService depositService
    ) {
        this.virtualAccountRepository = virtualAccountRepository;
        this.virtualAccountMapper = virtualAccountMapper;
        this.depositService = depositService;
    }

    /**
     * Save a virtualAccount.
     *
     * @param virtualAccountDTO the entity to save.
     * @return the persisted entity.
     */
    public VirtualAccountDTO save(VirtualAccountDTO virtualAccountDTO) {
        log.debug("Request to save VirtualAccount : {}", virtualAccountDTO);
        VirtualAccount virtualAccount = virtualAccountMapper.toEntity(virtualAccountDTO);
        virtualAccount = virtualAccountRepository.save(virtualAccount);
        return virtualAccountMapper.toDto(virtualAccount);
    }

    /**
     * Partially update a virtualAccount.
     *
     * @param virtualAccountDTO the entity to update partially.
     * @return the persisted entity.
     */
    public Optional<VirtualAccountDTO> partialUpdate(VirtualAccountDTO virtualAccountDTO) {
        log.debug("Request to partially update VirtualAccount : {}", virtualAccountDTO);

        return virtualAccountRepository
            .findById(virtualAccountDTO.getId())
            .map(
                existingVirtualAccount -> {
                    virtualAccountMapper.partialUpdate(existingVirtualAccount, virtualAccountDTO);

                    return existingVirtualAccount;
                }
            )
            .map(virtualAccountRepository::save)
            .map(virtualAccountMapper::toDto);
    }

    /**
     * Get all the virtualAccounts.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    @Transactional(readOnly = true)
    public Page<VirtualAccountDTO> findAll(Pageable pageable) {
        log.debug("Request to get all VirtualAccounts");
        return virtualAccountRepository.findAll(pageable).map(virtualAccountMapper::toDto);
    }

    /**
     *  Get all the virtualAccounts where Deposit is {@code null}.
     *  @return the list of entities.
     */
    @Transactional(readOnly = true)
    public List<VirtualAccountDTO> findAllWhereDepositIsNull() {
        log.debug("Request to get all virtualAccounts where Deposit is null");
        return StreamSupport
            .stream(virtualAccountRepository.findAll().spliterator(), false)
            .filter(virtualAccount -> virtualAccount.getDeposit() == null)
            .map(virtualAccountMapper::toDto)
            .collect(Collectors.toCollection(LinkedList::new));
    }

    /**
     * Get one virtualAccount by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    public Optional<VirtualAccountDTO> findOne(Long id) {
        log.debug("Request to get VirtualAccount : {}", id);
        return virtualAccountRepository.findById(id).map(virtualAccountMapper::toDto);
    }

    /**
     * Delete the virtualAccount by id.
     *
     * @param id the id of the entity.
     */
    public void delete(Long id) {
        log.debug("Request to delete VirtualAccount : {}", id);
        virtualAccountRepository.deleteById(id);
    }

    public VirtualAccountDTO changeMode(ChangeVaModeDTO changeVaModeDTO) {
        log.debug("Change virtual account mode with id :{}", changeVaModeDTO.getId());

        Optional<VirtualAccount> virtualAccountOptional = virtualAccountRepository.findTop1ByIdAndMode(
            changeVaModeDTO.getId(),
            changeVaModeDTO.getFromMode()
        );

        if (!virtualAccountOptional.isPresent()) {
            log.warn("Virtual account with id :{} and mode :{} not found", changeVaModeDTO.getId(), changeVaModeDTO.getFromMode());
            throw new CodedException(ProblemEnum.OBJECT_NOT_FOUND, "virtual account");
        }

        if (changeVaModeDTO.getFromMode() != ModeType.DEV) {
            log.warn("Mode type is not DEV , virtual account id :{}", changeVaModeDTO.getId());
            throw new CodedException(ProblemEnum.INVALID_REQUEST, "mode type is not DEV");
        }

        VirtualAccount virtualAccount = virtualAccountOptional.get();
        virtualAccount.setMode(changeVaModeDTO.getToMode());

        return virtualAccountMapper.toDto(virtualAccountRepository.save(virtualAccount));
    }

    public VirtualAccount getOrCreateVirtualAccount(String orgName, String inn, ModeType modeType, Integer ccy) {
        Optional<VirtualAccount> virtualAccountOptional = virtualAccountRepository.findTop1ByOrgNameAndInn(orgName, inn);

        if (virtualAccountOptional.isPresent()) {
            log.debug("Found VirtualAccount :{}", virtualAccountOptional.get());
            return virtualAccountOptional.get();
        }

        VirtualAccount virtualAccount = new VirtualAccount().active(true).inn(inn).mode(modeType).name(orgName).orgName(orgName);

        virtualAccount = virtualAccountRepository.save(virtualAccount);

        log.debug("Saved VirtualAccount : {}", virtualAccount.getName());

        depositService.createVirtualAgentDeposit(virtualAccount.getId(), ccy);

        log.debug("Deposit for virtual agent :{} create", virtualAccount.getName());

        return virtualAccount;
    }

    public VirtualAccount getOrCreateVirtualAccount(String orgName, String inn) {
        return getOrCreateVirtualAccount(orgName, inn, ModeType.DEV, Constants.DEFAULT_CURRENCY_CODE);
    }

    public VirtualAccount getOrCreateVirtualAccount(String orgName, String inn, ModeType modeType) {
        return getOrCreateVirtualAccount(orgName, inn, modeType, Constants.DEFAULT_CURRENCY_CODE);
    }

    public VirtualAccount getByOrgNameAndInn(String orgName, String inn) {
        VirtualAccount virtualAccount = virtualAccountRepository
            .findTop1ByOrgNameAndInn(orgName, inn)
            .orElseThrow(() -> new CodedException(ProblemEnum.OBJECT_NOT_FOUND, "virtual account"));

        depositService.createVirtualAgentDeposit(virtualAccount.getId(), Constants.DEFAULT_CURRENCY_CODE);
        return virtualAccount;
    }
}
