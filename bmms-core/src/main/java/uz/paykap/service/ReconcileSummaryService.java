package uz.paykap.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.paykap.domain.Gateway;
import uz.paykap.domain.ReconcileSummary;
import uz.paykap.domain.enumeration.ReconcilePeriod;
import uz.paykap.domain.enumeration.ReconcileSummaryStatus;
import uz.paykap.domain.enumeration.ReconcileSummaryType;
import uz.paykap.repository.GatewayRepository;
import uz.paykap.repository.ReconcileSummaryRepository;
import uz.paykap.service.dto.ReconcileSummaryDTO;
import uz.paykap.service.dto.reconcile.RecoSummaryParam;
import uz.paykap.service.mapper.ReconcileSummaryMapper;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * Service Implementation for managing {@link ReconcileSummary}.
 */
@Slf4j
@Service
@Transactional
@AllArgsConstructor
public class ReconcileSummaryService {

    private final ReconcileSummaryRepository reconcileSummaryRepository;
    private final ReconcileSummaryMapper reconcileSummaryMapper;
    private final GatewayRepository gatewayRepository;
    private final TransactionService transactionService;

    /**
     * Save a reconcileSummary.
     *
     * @param reconcileSummaryDTO the entity to save.
     * @return the persisted entity.
     */
    public ReconcileSummaryDTO save(ReconcileSummaryDTO reconcileSummaryDTO) {
        log.debug("Request to save ReconcileSummary : {}", reconcileSummaryDTO);
        ReconcileSummary reconcileSummary = reconcileSummaryMapper.toEntity(reconcileSummaryDTO);
        reconcileSummary = reconcileSummaryRepository.save(reconcileSummary);
        return reconcileSummaryMapper.toDto(reconcileSummary);
    }

    /**
     * Partially update a reconcileSummary.
     *
     * @param reconcileSummaryDTO the entity to update partially.
     * @return the persisted entity.
     */
    public Optional<ReconcileSummaryDTO> partialUpdate(ReconcileSummaryDTO reconcileSummaryDTO) {
        log.debug("Request to partially update ReconcileSummary : {}", reconcileSummaryDTO);

        return reconcileSummaryRepository
            .findById(reconcileSummaryDTO.getId())
            .map(
                existingReconcileSummary -> {
                    reconcileSummaryMapper.partialUpdate(existingReconcileSummary, reconcileSummaryDTO);

                    return existingReconcileSummary;
                }
            )
            .map(reconcileSummaryRepository::save)
            .map(reconcileSummaryMapper::toDto);
    }

    /**
     * Get all the reconcileSummaries.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    @Transactional(readOnly = true)
    public Page<ReconcileSummaryDTO> findAll(Pageable pageable) {
        log.debug("Request to get all ReconcileSummaries");
        return reconcileSummaryRepository.findAll(pageable).map(reconcileSummaryMapper::toDto);
    }

    /**
     * Get one reconcileSummary by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    public Optional<ReconcileSummaryDTO> findOne(Long id) {
        log.debug("Request to get ReconcileSummary : {}", id);
        return reconcileSummaryRepository.findById(id).map(reconcileSummaryMapper::toDto);
    }

    /**
     * Delete the reconcileSummary by id.
     *
     * @param id the id of the entity.
     */
    public void delete(Long id) {
        log.debug("Request to delete ReconcileSummary : {}", id);
        reconcileSummaryRepository.deleteById(id);
    }

    public ReconcileSummary saveReconcileSummary(
        boolean isSuccess,
        String gateway,
        Instant from,
        Instant to,
        Long gatewayTotalAmount,
        Long gatewayTotalCount
    ) {
        return saveReconcileSummary(isSuccess, gateway, from, to, gatewayTotalAmount, gatewayTotalCount, 100);
    }

    public ReconcileSummary saveReconcileSummary(
        boolean isSuccess,
        String gateway,
        Instant from,
        Instant to,
        Long gatewayTotalAmount,
        Long gatewayTotalCount,
        Integer amountRatio
    ) {
        return saveReconcileSummary(
            isSuccess,
            gateway,
            from,
            to,
            gatewayTotalAmount,
            gatewayTotalCount,
            transactionService.getTotalAmount(gateway, from, to, amountRatio),
            transactionService.getTotalCount(gateway, from, to),
            null
        );
    }

    public ReconcileSummary saveReconcileSummary(
        boolean isSuccess,
        String gateway,
        Instant from,
        Instant to,
        Long gatewayTotalAmount,
        Long gatewayTotalCount,
        Integer amountRatio,
        ReconcileSummaryStatus reconcileSummaryStatus
    ) {
        return saveReconcileSummary(
            isSuccess,
            gateway,
            from,
            to,
            gatewayTotalAmount,
            gatewayTotalCount,
            transactionService.getTotalAmount(gateway, from, to, amountRatio),
            transactionService.getTotalCount(gateway, from, to),
            reconcileSummaryStatus
        );
    }

    public ReconcileSummary saveReconcileSummary(
        boolean isSuccess,
        String gateway,
        Instant from,
        Instant to,
        Long gatewayTotalAmount,
        Long gatewayTotalCount,
        Long systemTotalAmount,
        Long systemTotalCount,
        ReconcileSummaryStatus reconcileSummaryStatus
    ) {
        ReconcileSummary summary = reconcileSummaryRepository
            .findTop1ByGatewayNameAndDate(gateway, LocalDateTime.ofInstant(to, ZoneOffset.UTC).toLocalDate())
            .orElse(new ReconcileSummary());

        Optional<Gateway> optionalGateway = gatewayRepository.findTop1ByName(gateway);

        Long gatewayId = -999L;

        if (optionalGateway.isPresent()) {
            gatewayId = optionalGateway.get().getId();
        }

        summary
            .date(LocalDateTime.ofInstant(to, ZoneOffset.UTC).toLocalDate())
            .gatewayAmount(gatewayTotalAmount)
            .gatewayCount(gatewayTotalCount)
            .gatewayId(gatewayId)
            .gatewayName(gateway)
            .period(ReconcilePeriod.DAY)
            .success(isSuccess)
            .systemAmount(systemTotalAmount)
            .systemCount(systemTotalCount)
            .status(reconcileSummaryStatus);

        return reconcileSummaryRepository.save(summary);
    }

    public ReconcileSummary saveP2pReconcileSummaryWithType(
        boolean isSuccess,
        String gateway,
        Instant from,
        Instant to,
        Long gatewayTotalAmount,
        Long gatewayTotalCount,
        Long systemTotalAmount,
        Long systemTotalCount,
        ReconcileSummaryStatus reconcileSummaryStatus,
        ReconcileSummaryType type
    ) {
        ReconcileSummary summary = new ReconcileSummary()
            .date(LocalDateTime.ofInstant(to, ZoneOffset.UTC).toLocalDate())
            .gatewayAmount(gatewayTotalAmount)
            .gatewayCount(gatewayTotalCount)
            .gatewayId(-999L)
            .gatewayName(gateway)
            .period(ReconcilePeriod.HOUR)
            .success(isSuccess)
            .systemAmount(systemTotalAmount)
            .systemCount(systemTotalCount)
            .status(reconcileSummaryStatus)
            .fromDate(from)
            .toDate(to)
            .type(type);

        return reconcileSummaryRepository.save(summary);
    }

    public ReconcileSummary saveReconcileSummary(
        Long recId,
        Boolean isSuccess,
        String gateway,
        Instant from,
        Instant to,
        Long gatewayTotalAmount,
        Long gatewayTotalCount,
        Integer amountRatio,
        ReconcileSummaryStatus reconcileSummaryStatus,
        Long gatewayId
    ) {
        log.trace("saveReconcileSummary started");
        return saveReconcileSummary(
            recId,
            isSuccess,
            gateway,
            to,
            gatewayTotalAmount,
            gatewayTotalCount,
            transactionService.getTotalAmount(gateway, from, to, amountRatio),
            transactionService.getTotalCount(gateway, from, to),
            reconcileSummaryStatus,
            gatewayId
        );
    }

    public ReconcileSummary saveReconcileSummary(
        Long recId,
        Boolean isSuccess,
        String agentName,
        Instant to,
        Long gatewayTotalAmount,
        Long gatewayTotalCount,
        Long systemTotalAmount,
        Long systemTotalCount,
        ReconcileSummaryStatus reconcileSummaryStatus,
        Long gatewayId
    ) {
        log.trace("saveReconcileSummary started");

        var summary = reconcileSummaryRepository
            .findTop1ByIdAndStatus(recId, ReconcileSummaryStatus.PROCESSING)
            .orElse(new ReconcileSummary());

        summary
            .date(LocalDateTime.ofInstant(to, ZoneOffset.UTC).toLocalDate())
            .gatewayAmount(gatewayTotalAmount)
            .gatewayCount(gatewayTotalCount)
            .gatewayId(gatewayId)
            .gatewayName(agentName)
            .period(ReconcilePeriod.DAY)
            .success(isSuccess)
            .systemAmount(systemTotalAmount)
            .systemCount(systemTotalCount)
                .status(reconcileSummaryStatus);

        return reconcileSummaryRepository.save(summary);
    }

    public Long getGatewayId(String gateway) {
        log.trace("getGatewayId started for : {}", gateway);
        Optional<Gateway> optionalGateway = gatewayRepository.findTop1ByName(gateway);
        return optionalGateway.isPresent() ? optionalGateway.get().getId() : -999L;
    }

    public ReconcileSummary saveReconcileSummary(RecoSummaryParam param, Supplier<ReconcileSummary> summarySupplier) {
        log.trace("saveReconcileSummary started");

        var summary = summarySupplier.get();

        if (summary == null) {
            log.trace("summary is null");
            return null;
        }

        summary
                .date(LocalDateTime.ofInstant(param.getTo(), ZoneOffset.UTC).toLocalDate())
                .gatewayAmount(param.getGatewayTotalAmount())
                .gatewayCount(param.getGatewayTotalCount())
                .gatewayId(param.getGatewayId())
                .gatewayName(param.getGatewayName())
                .period(param.getReconcilePeriod())
                .success(param.getIsSuccess())
                .systemAmount(param.getSystemTotalAmount())
                .systemCount(param.getSystemTotalCount())
                .status(param.getReconcileSummaryStatus());

        return reconcileSummaryRepository.save(summary);

    }
}
