package uz.paykap.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.javlon.commons.base.Result;
import uz.javlon.commons.exception.CodedException;
import uz.javlon.commons.exception.ProblemEnum;
import uz.paykap.config.ApplicationProperties;
import uz.paykap.config.Constants;
import uz.paykap.domain.Agent;
import uz.paykap.domain.Deposit;
import uz.paykap.domain.enumeration.DepositState;
import uz.paykap.event.dto.AgentDepositModifiedEvent;
import uz.paykap.event.dto.AgentLowDepositEvent;
import uz.paykap.event.dto.ErrorModifyDepositEvent;
import uz.paykap.repository.AgentRepository;
import uz.paykap.repository.DepositRepository;
import uz.paykap.security.SecurityUtils;
import uz.paykap.service.dto.*;
import uz.paykap.service.mapper.DepositMapper;

import java.util.Optional;

/**
 * Service Implementation for managing {@link Deposit}.
 */
@Service
@Transactional
public class DepositService {

    private final Logger log = LoggerFactory.getLogger(DepositService.class);

    //TODO - impl
    public boolean canRecipientProceedPayment(Long recipientId, Long amount) {
        throw new CodedException(ProblemEnum.NOT_IMPLEMENTED); //TODO - impl
    }

    public SimpleDepositDTO getAgentDeposit() {
        Deposit deposit = depositRepository
                .findTop1ByAgent_ShortName(
                        SecurityUtils.getCurrentUserLogin().orElseThrow(() -> new CodedException(ProblemEnum.USERNAME_NOT_FOUND))
                )
                .orElseThrow(() -> new CodedException(ProblemEnum.USERNAME_NOT_FOUND));

        return depositMapper.toSimple(deposit);
    }

    public SimpleDepositDTO getAgentDeposit(String shortName) {
        Deposit deposit = depositRepository
                .findTop1ByAgent_ShortName(shortName)
                .orElseThrow(() -> new CodedException(ProblemEnum.USERNAME_NOT_FOUND, "agent deposit"));
        log.debug("Deposit by shortname: {}, deposit: {}", shortName, deposit);
        return depositMapper.toSimple(deposit);
    }

    public boolean canAgentProceedPayment(String agentLogin, Long amountInCcyInCents) {
        return canAgentProceedPayment(
                depositRepository
                        .findTop1ByAgent_ShortName(agentLogin)
                        .orElseThrow(() -> new CodedException(ProblemEnum.OBJECT_NOT_FOUND, "deposit")),
                amountInCcyInCents
        );
    }

    public boolean canAgentProceedPayment(Deposit deposit, Long amountInCcyInCents) {
        if (deposit.getBlocked()) {
            throw new CodedException(ProblemEnum.OBJECT_LOCKED, "deposit");
        }

        if (deposit.getUnlimit()) {
            return true;
        }

        //        long limit = Math.max(deposit.getLimitDisable(), deposit.getOverdraftLimit());

        //        Long limit = deposit.getLimitDisable() < deposit.getOverdraftLimit() ? deposit.getOverdraftLimit() : deposit.getLimitDisable();

        //TODO - impl with booked balance if needed
        //        return deposit.getBalance() - deposit.getBookedBalance() - amountInCcyInCents >= limit;

        return deposit.getBalance() - amountInCcyInCents >= Math.max(deposit.getLimitDisable(), deposit.getOverdraftLimit());
    }

    public Result decreaseAgentDeposit(String agentLogin, Long amount) {
        return decreaseAgentDeposit(agentLogin, amount, Constants.DEFAULT_CURRENCY_CODE);
    }

    public Result decreaseGatewayDeposit(String gatewayName, Long amount) {
        return processGatewayDeposit(gatewayName, amount, -1, Constants.DEFAULT_CURRENCY_CODE);
    }

    public Result increaseGatewayDeposit(String gatewayName, Long amount) {
        return processGatewayDeposit(gatewayName, amount, +1, Constants.DEFAULT_CURRENCY_CODE);
    }

    public Result decreaseAgentDeposit(String agentLogin, Long amount, Integer currency) {
        log.debug("decreaseAgentDeposit({},{})", agentLogin, amount);
        return processDeposit(agentLogin, amount, -1, currency);
    }

    public Result increaseAgentDeposit(String agentLogin, Long amount) {
        return increaseAgentDeposit(agentLogin, amount, Constants.DEFAULT_CURRENCY_CODE);
    }

    public Result increaseVirtualAccountDeposit(String orgName, Long amount) {
        log.debug("increaseVirtualAccountDeposit({},{})", orgName, amount);
        return processVaDeposit(orgName, amount, +1);
    }

    public Result decreaseVirtualAccountDeposit(String orgName, Long amount) {
        log.debug("decreaseVirtualAccountDeposit({},{})", orgName, amount);
        return processVaDeposit(orgName, amount, -1, Constants.DEFAULT_CURRENCY_CODE);
    }

    public Result increaseAgentDeposit(String agentLogin, Long amount, Integer currency) {
        log.debug("increaseAgentDeposit({},{})", agentLogin, amount);
        return processDeposit(agentLogin, amount, +1, currency);
    }

    private synchronized Result processVaDeposit(String agentLogin, Long amount, int multiplier) {
        return processVaDeposit(agentLogin, amount, multiplier, Constants.DEFAULT_CURRENCY_CODE);
    }

    //TODO - use internalDepositResolver
    private synchronized Result processVaDeposit(String vaOrgName, Long amount, int multiplier, Integer currency) {
        Optional<Deposit> deposit = depositRepository.findTop1ByVirtualAccount_OrgNameAndBalanceCcy(vaOrgName, currency);

        if (!deposit.isPresent()) {
            log.warn("Deposit not found for vaOrgName '{}'", vaOrgName);
            return new Result(
                false,
                new CodedException(ProblemEnum.NOT_FOUND, "Deposit not found with currency " + currency + " for user " + vaOrgName)
            );
        }
        //Check deposit only if decreasing, because increasing deposit always allowed
        if (multiplier == -1 && !canAgentProceedPayment(deposit.get(), amount)) {
            return new Result(false, new CodedException(ProblemEnum.LIMIT_EXCEEDED, "deposit"));
        }

        log.debug("## Deposit for virtual account '{}' before ({}) : {}", vaOrgName, multiplier, deposit.get().getBalance());

        deposit.get().setBalance(deposit.get().getBalance() + (multiplier * amount));

        log.debug("## Deposit for  virtual account '{}' after ({}) : {}", vaOrgName, multiplier, deposit.get().getBalance());

        try {
            depositRepository.saveAndFlush(deposit.get());
        } catch (Exception e) {
            log.warn("Error occurred while saving deposit ");
        }

        if (multiplier == -1 && deposit.get().getBalance() < deposit.get().getLimitInform()) {
            applicationEventPublisher.publishEvent(new AgentLowDepositEvent(this, null, deposit.get().getBalance(), vaOrgName));
        }

        return Result.SUCCESS;
    }

    private Result processDeposit(String agentLogin, Long amount, int multiplier) {
        return processDeposit(agentLogin, amount, multiplier, Constants.DEFAULT_CURRENCY_CODE);
    }

    private Result processDeposit(String agentLogin, Long amount, int multiplier, Integer currency) {
        Optional<Deposit> deposit = internalDepositResolver.findTop1ByAgent_ShortNameAndBalanceCcy(agentLogin, currency);

        if (deposit.isEmpty()) {
            log.warn("Deposit not found for agent '{}'", agentLogin);
            return new Result(
                false,
                new CodedException(ProblemEnum.NOT_FOUND, "Deposit not found with currency " + currency + " for user " + agentLogin)
            );
        }

        //Check deposit only if decreasing, because increasing deposit always allowed
        if (multiplier == -1 && !canAgentProceedPayment(deposit.get(), amount)) {
            return new Result(false, new CodedException(ProblemEnum.LIMIT_EXCEEDED, "deposit"));
        }

        log.debug("## Deposit for agent '{}' before ({}) : {}", agentLogin, multiplier, deposit.get().getBalance());

        deposit.get().setBalance(deposit.get().getBalance() + (multiplier * amount));

        log.debug("## Deposit for agent '{}' after ({}) : {}", agentLogin, multiplier, deposit.get().getBalance());

        try {
            depositRepository.saveAndFlush(deposit.get());
        } catch (Exception e) {
            applicationEventPublisher.publishEvent(new ErrorModifyDepositEvent(this, amount, deposit.get().getBalance(), agentLogin));
            log.warn("Error occurred while saving deposit : {}", e.getLocalizedMessage());
            return Result.FAILED;
        }

        if (multiplier == -1 && deposit.get().getBalance() < deposit.get().getLimitInform()) {
            applicationEventPublisher.publishEvent(new AgentLowDepositEvent(this, null, deposit.get().getBalance(), agentLogin));
        }

        return Result.SUCCESS;
    }

    //TODO - fix , use internalDepositResolver
    private synchronized Result processGatewayDeposit(String gatewayName, Long amount, int multiplier, Integer currency) {
        Optional<Deposit> deposit = depositRepository.findTop1ByGateway_NameAndBalanceCcy(gatewayName, currency);

        if (!deposit.isPresent()) {
            log.warn("Deposit not found for gateway '{}'", gatewayName);
            return new Result(
                false,
                new CodedException(ProblemEnum.NOT_FOUND, "Deposit not found with currency " + currency + " for gateway " + gatewayName)
            );
        }

        //Check deposit only if decreasing, because increasing deposit always allowed
        if (multiplier == -1 && !canAgentProceedPayment(deposit.get(), amount)) {
            return new Result(false, new CodedException(ProblemEnum.LIMIT_EXCEEDED, "deposit"));
        }

        log.info("## Deposit for gateway '{}' before ({}) : {}", gatewayName, multiplier, deposit.get().getBalance());

        deposit.get().setBalance(deposit.get().getBalance() + (multiplier * amount));

        log.info("## Deposit for gateway '{}' after ({}) : {}", gatewayName, multiplier, deposit.get().getBalance());

        try {
            depositRepository.saveAndFlush(deposit.get());
        } catch (Exception e) {
            log.warn("Error occurred while saving deposit ");
        }

        if (multiplier == -1 && deposit.get().getBalance() < deposit.get().getLimitInform()) {
            applicationEventPublisher.publishEvent(new AgentLowDepositEvent(this, null, deposit.get().getBalance(), gatewayName));
        }

        return Result.SUCCESS;
    }

    @Transactional
    public Result processEmission(ReplenishDepositDTO replenish) {
        if (replenish.getBalanceAmount() == null || replenish.getBalanceAmount() < 0) {
            throw new CodedException(ProblemEnum.INVALID_PARAMS);
        }

        DepositDTO agentDepositDTO;

        //TODO add supported currencies into add deposit form, Abdulloh, and uncomment below line;
        //        Optional<Deposit> agentDeposit = depositRepository.findTop1ByAgent_ShortNameAndBalanceCcy(replenish.getAgentLogin(), replenish.getBalanceCcy());

        Optional<Deposit> agentDeposit = internalDepositResolver.findTop1ByAgent_ShortName(replenish.getAgentLogin());

        if (agentDeposit.isPresent()) {
            agentDepositDTO = depositMapper.toDto(agentDeposit.get());
        } else {
            if (properties.getDeposit().getCreateIfNotExists()) {
                log.warn("## Deposit not found for agent : '{}', creating new ", replenish.getAgentLogin());
                agentDepositDTO = createAgentDeposit(replenish.getAgentLogin(), 0L, 0L);
            } else {
                log.warn("## Deposit not found for agent '{}'", replenish.getAgentLogin());
                return new Result(false, new CodedException(ProblemEnum.OBJECT_NOT_FOUND));
            }
        }

        //Set INN
        String agentInn = agentRepository.getAgentInn(agentDepositDTO.getAgent().getId());
        replenish.setRecipientTaxnr(agentInn);

        //Emboss agent deposit (will )

        log.info("## Deposit for agent '{}' before : {}", replenish.getAgentLogin(), agentDepositDTO.getBalance());

        Long beforeBalance = agentDepositDTO.getBalance();

        agentDepositDTO.setBalance(agentDepositDTO.getBalance() + (replenish.getBalanceAmount()));
        //        agentDepositDTO.setOverdraftLimit(replenish.getOverdraftLimit());

        log.info("## Deposit for agent '{}' after : {}", replenish.getAgentLogin(), agentDepositDTO.getBalance());

        boolean agentDepositSaved = false;

        try {
            agentDepositDTO = save(agentDepositDTO);
            agentDepositSaved = true;
        } catch (Exception e) {
            log.warn("Error occurred while saving agentDeposit ");
        }

        applicationEventPublisher.publishEvent(
            new AgentDepositModifiedEvent(this, beforeBalance, agentDepositDTO.getBalance(), replenish.getAgentLogin())
        );

        if (!agentDepositSaved) {
            log.warn("Exiting method, because error occurred while saving agentDeposit");
            return Result.FAILED;
        }

        //Emboss system deposit

        Deposit systemDeposit = internalDepositResolver
            .findTop1ByAgent_ShortName(Constants.SYSTEM_AGENT_NAME)
            .orElseThrow(() -> new CodedException(ProblemEnum.OBJECT_NOT_FOUND));
        replenish.setPayerTaxnr(agentRepository.getAgentInn(systemDeposit.getAgent().getId()));

        log.info("Deposit for 'system' before : {}", systemDeposit.getBalance());

        beforeBalance = systemDeposit.getBalance();

        systemDeposit.setBalance(systemDeposit.getBalance() - (replenish.getBalanceAmount()));

        log.info("Deposit for 'system' after : {}", systemDeposit.getBalance());

        try {
            systemDeposit = depositRepository.save(systemDeposit);

            applicationEventPublisher.publishEvent(
                new AgentDepositModifiedEvent(this, beforeBalance, systemDeposit.getBalance(), Constants.SYSTEM_AGENT_NAME)
            );

            depositTransactionService.save(replenish, beforeBalance, systemDeposit.getBalance(), agentDepositDTO.getBalanceCcy());
            //            depositTransactionService.save(replenish, agentDepositDTO.getBalanceCcy());

            return Result.SUCCESS;
        } catch (Exception e) {
            log.warn("Error occurred while saving systemDeposit ");
            return Result.FAILED;
        }
    }

    //TODO make synchronized, exception may occur while saving because of @Version
    public Result configureDepositSettings(DepositConfigDTO depositConfigDTO) {
        Deposit agentDeposit = internalDepositResolver
            .findTop1ByAgent_ShortName(depositConfigDTO.getAgentLogin())
            .orElseThrow(() -> new CodedException(ProblemEnum.OBJECT_NOT_FOUND, "deposit of agent"));

        boolean madeChanges = false;

        if (depositConfigDTO.getOverdraftLimit() != null) {
            agentDeposit.setOverdraftLimit(depositConfigDTO.getOverdraftLimit());
            madeChanges = true;
        }

        if (depositConfigDTO.getLimitDisable() != null) {
            agentDeposit.setLimitDisable(depositConfigDTO.getLimitDisable());
            madeChanges = true;
        }

        if (madeChanges) {
            try {
                depositRepository.save(agentDeposit);

                return Result.SUCCESS;
            } catch (Exception e) {
                log.warn("Error occurred while saving Deposit");

                return Result.FAILED;
            }
        } else {
            return Result.FAILED;
        }
    }

    public DepositDTO createAgentDeposit(String agentLogin, long balance, long overdraftLimit) {
        Agent agent = agentRepository.findTop1ByShortName(agentLogin).orElseThrow(() -> new CodedException(ProblemEnum.OBJECT_NOT_FOUND));

        return createAgentDeposit(agent.getId(), balance, overdraftLimit);
    }

    public DepositDTO createAgentDeposit(String agentLogin, long balance) {
        return createAgentDeposit(agentLogin, balance, 0L);
    }

    public DepositDTO createAgentDeposit(Long agentId) {
        return createAgentDeposit(agentId, 0L);
    }

    public DepositDTO createAgentDeposit(Long agentId, long balance) {
        return createAgentDeposit(agentId, balance, 0L);
    }

    public DepositDTO createAgentDeposit(Long agentId, long balance, long overdraftLimit) {
        return createAgentDeposit(agentId, balance, overdraftLimit, 860);
    }

    public DepositDTO createAgentDeposit(Long agentId, long balance, long overdraftLimit, Integer balanceCcy) {
        Optional<Deposit> optionalDeposit = depositRepository.findTop1ByAgent_IdAndBalanceCcy(agentId, balanceCcy);

        if (optionalDeposit.isPresent()) {
            log.debug("Found Deposit for agent[{}]", agentId);
            return depositMapper.toDto(optionalDeposit.get());
        }

        DepositDTO deposit = new DepositDTO();
        deposit.setBalance(balance);
        deposit.setUnlimit(false);
        deposit.setBlocked(false);
        deposit.setBookedBalance(0L);
        deposit.setLimitInform(0L);
        deposit.setLimitDisable(0L);
        deposit.setOverdraftLimit(overdraftLimit);
        deposit.setState(DepositState.NEW);

        AgentDTO agent = new AgentDTO();
        agent.setId(agentId);
        agent.setActive(true);
        agent.setAmountRatio(100);

        deposit.setAgent(agent);
        deposit.setBalanceCcy(balanceCcy);

        log.info("Creating Deposit for agent[{}] : {}", agentId, deposit);

        return save(deposit);
    }

    public DepositDTO createVirtualAgentDeposit(Long virtualAccountId, Integer balanceCcy) {
        Optional<Deposit> depositOptional = depositRepository.findTop1ByVirtualAccount_Id(virtualAccountId);

        if (depositOptional.isPresent()) {
            log.debug("Found Deposit for virtual account[{}]", depositOptional);
            return depositMapper.toDto(depositOptional.get());
        }

        DepositDTO deposit = new DepositDTO();
        deposit.setBalance(0L);
        deposit.setUnlimit(false);
        deposit.setBlocked(false);
        deposit.setBookedBalance(0L);
        deposit.setLimitInform(0L);
        deposit.setLimitDisable(0L);
        deposit.setOverdraftLimit(0L);
        //        deposit.setVirtualAccountId(virtualAccountId);
        deposit.setBalanceCcy(balanceCcy);

        log.info("Creating Deposit for virtual account[{}] : {}", virtualAccountId, deposit);

        return save(deposit);
    }

    public Deposit getDepositOrCreate(Deposit deposit) {
        log.debug("Getting Deposit : {}", deposit);
        return depositRepository
            .findTop1ByAgent_ShortName(deposit.getAgent().getShortName())
            .orElseGet(() -> depositRepository.save(deposit));
    }

    public Deposit clearBalance(Long virtualAccountId) {
        log.debug("Clearing balance with VA.id :{}", virtualAccountId);
        Optional<Deposit> depositOptional = depositRepository.findTop1ByVirtualAccount_Id(virtualAccountId);

        if (!depositOptional.isPresent()) {
            log.warn("deposit with VA.id :{} not found", virtualAccountId);
            throw new CodedException(ProblemEnum.OBJECT_NOT_FOUND, "deposit not found");
        }

        Deposit deposit = depositOptional.get();

        log.debug("current balance :{}", deposit.getBalance());

        deposit.setBalance(0L);

        return depositRepository.save(deposit);
    }

    /**
     * Save a deposit.
     *
     * @param depositDTO the entity to save.
     * @return the persisted entity.
     */
    public DepositDTO save(DepositDTO depositDTO) {
        log.debug("Request to save Deposit : {}", depositDTO);

        Deposit deposit = depositMapper.toEntity(depositDTO);
        deposit = depositRepository.save(deposit);
        return depositMapper.toDto(deposit);
    }

    /**
     * Partially update a deposit.
     *
     * @param depositDTO the entity to update partially.
     * @return the persisted entity.
     */
    public Optional<DepositDTO> partialUpdate(DepositDTO depositDTO) {
        log.debug("Request to partially update Deposit : {}", depositDTO);

        return depositRepository
            .findById(depositDTO.getId())
            .map(
                existingDeposit -> {
                    depositMapper.partialUpdate(existingDeposit, depositDTO);

                    return existingDeposit;
                }
            )
            .map(depositRepository::save)
            .map(depositMapper::toDto);
    }

    /**
     * Get all the deposits.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    @Transactional(readOnly = true)
    public Page<DepositDTO> findAll(Pageable pageable) {
        log.debug("Request to get all Deposits");
        return depositRepository.findAll(pageable).map(depositMapper::toDto);
    }

    /**
     * Get one deposit by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    public Optional<DepositDTO> findOne(Long id) {
        log.debug("Request to get Deposit : {}", id);
        return depositRepository.findById(id).map(depositMapper::toDto);
    }

    /**
     * Delete the deposit by id.
     *
     * @param id the id of the entity.
     */
    public void delete(Long id) {
        log.debug("Request to delete Deposit : {}", id);
        depositRepository.deleteById(id);
    }

    public void softDelete(Long id) {
        log.debug("Request to delete Deposit : {}", id);
        depositRepository.updateStatus(id, DepositState.DELETED);
    }

    public void softDeleteByAgentId(Long id) {
        log.debug("Request to delete Deposit by agent id: {}", id);

        depositRepository.updateStatusByAgentId(id, DepositState.DELETED);
    }

    private final DepositRepository depositRepository;
    private final DepositTransactionService depositTransactionService;
    private final AgentRepository agentRepository;
    private final DepositMapper depositMapper;
    private final ApplicationProperties properties;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final InternalDepositResolver internalDepositResolver;

    public DepositService(
        DepositRepository depositRepository,
        DepositTransactionService depositTransactionService,
        AgentRepository agentRepository,
        DepositMapper depositMapper,
        ApplicationProperties properties,
        ApplicationEventPublisher applicationEventPublisher,
        InternalDepositResolver internalDepositResolver
    ) {
        this.depositRepository = depositRepository;
        this.depositTransactionService = depositTransactionService;
        this.agentRepository = agentRepository;
        this.depositMapper = depositMapper;
        this.properties = properties;
        this.applicationEventPublisher = applicationEventPublisher;
        this.internalDepositResolver = internalDepositResolver;
    }
}
