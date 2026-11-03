package uz.paykap.service.reconciliation;

import com.google.common.collect.Sets;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uz.devops.bmcore.reconciliation.GatewayRecoHelperService;
import uz.devops.bmcore.reconciliation.dto.CompareSchema;
import uz.devops.bmcore.reconciliation.dto.HasRecoSummary;
import uz.devops.bmcore.reconciliation.dto.RecoTranDTO;
import uz.javlon.commons.base.BasicResult;
import uz.javlon.commons.base.BasicResultData;
import uz.javlon.commons.base.Result;
import uz.javlon.commons.base.ResultData;
import uz.paykap.adapter.verification.common.ReconcileDelegateService;
import uz.paykap.domain.ReconcileDiscrepancy;
import uz.paykap.domain.ReconcileSummary;
import uz.paykap.domain.enumeration.ReconcileSummaryStatus;
import uz.paykap.event.dto.RecoFinishedEvent;
import uz.paykap.event.dto.ReconciliationFinishedEvent;
import uz.paykap.repository.TransactionRepository;
import uz.paykap.service.ReconcileSummaryService;

import javax.validation.constraints.NotNull;
import java.time.Instant;
import java.util.HashSet;
import java.util.Objects;

@Slf4j
@Service
@AllArgsConstructor
public class GatewayRecoHelperServiceImpl implements GatewayRecoHelperService {

    private final TransactionRepository transactionRepository;
    private final ReconcileDelegateService reconcileDelegateService;
    private final ReconcileSummaryService reconcileSummaryService;

    @Override
    public BasicResultData<HasRecoSummary> getSummary(String shortName, @NotNull final Instant from, @NotNull final Instant to) {
        log.debug("getTotal4Gateway : {}, {}, {}", shortName, from, to);
        return new BasicResultData<>(transactionRepository.getTotal4Gateway(shortName, from, to));
    }

    @Override
    public BasicResultData<HashSet<RecoTranDTO>> getDetailed(String shortName, @NotNull final Instant from, @NotNull final Instant to) {

        log.debug("Getting trans from db : {} : {} : {} ", shortName, from, to);

        HashSet<RecoTranDTO> transAsGatewayDTO = transactionRepository.getSetByGatewayV3(shortName, from, to, 1.0);

        log.debug("Retrieved trans : {} : {}", shortName, transAsGatewayDTO.size());

        return new BasicResultData<>(transAsGatewayDTO);
    }

    @Override
    public BasicResult compare(HasRecoSummary gatewaySummary, boolean matchCounts, String shortName, @NotNull final Instant from, @NotNull final Instant to) {

        BasicResultData<HasRecoSummary> systemResultData = getSummary(shortName, from, to);

        if (!systemResultData.isSuccess()) {
            log.warn("Get summary by {} failed for compare : {} : {} : {}", systemResultData.getResponseText(), shortName, from, to);
            return systemResultData;
        }

        if (!Objects.equals(systemResultData.getData().getTotalAmountTiyin(), gatewaySummary.getTotalAmountTiyin())) {
            log.warn("Summary of amounts differs : [{}:{}] != [system:{}]", shortName, gatewaySummary.getTotalAmountTiyin(), systemResultData.getData().getTotalAmountTiyin());
            reconcileSummaryService.saveReconcileSummary(false, shortName, from, to, gatewaySummary.getTotalAmountTiyin(), gatewaySummary.getTotalCount(), systemResultData.getData().getTotalAmountTiyin(), systemResultData.getData().getTotalCount(), ReconcileSummaryStatus.COMPARED_FALSE);
            return new BasicResult(false, "Summary of amounts differs : [gateway:" + gatewaySummary.getTotalAmountTiyin() + "] != [system:" + systemResultData.getData().getTotalAmountTiyin() + "]");
        }

        if (matchCounts && !Objects.equals(systemResultData.getData().getTotalCount(), gatewaySummary.getTotalCount())) {
            log.warn("Summary of counts differs : [{}:{}] != [system:{}]", shortName, gatewaySummary.getTotalCount(), systemResultData.getData().getTotalCount());
            reconcileSummaryService.saveReconcileSummary(false, shortName, from, to, gatewaySummary.getTotalAmountTiyin(), gatewaySummary.getTotalCount(), systemResultData.getData().getTotalAmountTiyin(), systemResultData.getData().getTotalCount(), ReconcileSummaryStatus.COMPARED_FALSE);
            return new BasicResult(false, "Summary of accounts differs : [" + shortName + ":" + gatewaySummary.getTotalCount() + "] != [system:" + systemResultData.getData().getTotalCount() + "]");
        }

        reconcileSummaryService.saveReconcileSummary(true, shortName, from, to, gatewaySummary.getTotalAmountTiyin(), gatewaySummary.getTotalCount(), systemResultData.getData().getTotalAmountTiyin(), systemResultData.getData().getTotalCount(), ReconcileSummaryStatus.COMPARED_OK);

        return new BasicResult(true, "Successfully compared summary with " + shortName);
    }

    @Override
    public BasicResult compare(HashSet<RecoTranDTO> gatewayDetailed, CompareSchema compareSchema, String shortName, @NotNull final Instant from, @NotNull final Instant to) {

        BasicResultData<HashSet<RecoTranDTO>> systemResultData = getDetailed(shortName, from, to);

        if (!systemResultData.isSuccess()) {
            log.warn("Get detailed by {} failed for compare : {} : {} : {}", systemResultData.getResponseText(), shortName, from, to);
            return systemResultData;
        }

        BasicResultData<HasRecoSummary> systemSummaryResultData = getSummary(shortName, from, to);

        Long totalAmount = 0L;
        Long totalCount = 0L;

        if (systemSummaryResultData.isSuccess()) {
            log.warn("Get summary by {} failed for compare : {} : {} : {}", systemResultData.getResponseText(), shortName, from, to);
            totalAmount = systemSummaryResultData.getData().getTotalAmountTiyin();
            totalCount = systemSummaryResultData.getData().getTotalCount();
        }

        Sets.SetView<RecoTranDTO> difference = Sets.symmetricDifference(systemResultData.getData(), gatewayDetailed);

        Double gatewayTotalSum = gatewayDetailed.stream().mapToDouble(RecoTranDTO::getAmount).sum();

        if (gatewayTotalSum == null) {
            gatewayTotalSum = 0d;
        }

        if (difference.isEmpty()) {
            //ALl ok, just result

            reconcileDelegateService.getReconcileSummaryService().saveReconcileSummary(true, shortName, from, to, gatewayTotalSum.longValue(), (long) gatewayDetailed.size(), totalAmount, totalCount, ReconcileSummaryStatus.COMPARED_OK);

            reconcileDelegateService.getPublisher().publishEvent(new ReconciliationFinishedEvent(this, shortName, new ResultData<>(true, null, null)));

            return new BasicResult(true, "Successfully compared detailed trans for {}" + shortName);
        }

        ReconcileSummary reconcileSummary = reconcileDelegateService.getReconcileSummaryService().saveReconcileSummary(false, shortName, from, to, gatewayTotalSum.longValue(), (long) gatewayDetailed.size(), 0, ReconcileSummaryStatus.COMPARED_FALSE);

        //If differs
        if (!Objects.equals(shortName, "Ucell")) {
            saveReconcileDiscrepancy(difference, shortName, reconcileSummary);
        }

        reconcileDelegateService.getPublisher().publishEvent(new RecoFinishedEvent(this, shortName, new ResultData<>(false, difference, null)));

        return new BasicResult(false, "Compare failed : Amount and count of transactions not equals");


    }

    private Result saveReconcileDiscrepancy(Sets.SetView<RecoTranDTO> difference, String gatewayName, ReconcileSummary reconcileSummary) {

        for (RecoTranDTO gatewayTranDTO : difference) {
            try {
                createOrUpdate(gatewayTranDTO, reconcileSummary);
            } catch (Exception e) {
                log.warn("Error when saving reconcileDiscrepancy : {} ", gatewayTranDTO.toString());
//                log.debug("Exception :{}", e.getLocalizedMessage());
            }
        }

        return Result.SUCCESS;
    }

    private ReconcileDiscrepancy createOrUpdate(RecoTranDTO gatewayTranDTO, ReconcileSummary reconcileSummary) {

        log.debug("Gateway tran to save in reconcile discrepancy :{}", gatewayTranDTO.toString());

        ReconcileDiscrepancy reconcileDiscrepancy = reconcileDelegateService.getReconcileDiscrepancyService().findBySystemTranId(gatewayTranDTO.getId()).orElse(null);

        if (reconcileDiscrepancy == null) {
            reconcileDiscrepancy = new ReconcileDiscrepancy();
            reconcileDiscrepancy.setSystemTransactionId(gatewayTranDTO.getId());
        }

        if (gatewayTranDTO.getIsGatewayTransaction() == Boolean.TRUE) {
            reconcileDiscrepancy.setGatewayAmount(gatewayTranDTO.getAmount().longValue());
            reconcileDiscrepancy.setGatewayStatus(gatewayTranDTO.getSuccess());
            reconcileDiscrepancy.setGatewayTransactionId(String.valueOf(gatewayTranDTO.getId()));
//            if (gatewayTranDTO.getExtId() != null) {
//                reconcileDiscrepancy.setGatewayTransactionId(Long.valueOf(gatewayTranDTO.getExtId()));
//            }
        } else {
            reconcileDiscrepancy.setSystemAmount(gatewayTranDTO.getAmount().longValue());
            reconcileDiscrepancy.setSystemStatus(gatewayTranDTO.getSuccess());
        }

        reconcileDiscrepancy.setReconcileSummary(reconcileSummary);
        reconcileDiscrepancy.setGatewayName(reconcileSummary.getGatewayName());

        if (reconcileDiscrepancy.getGatewayTransactionId() == null) {
            reconcileDiscrepancy.setGatewayTransactionId(String.valueOf(-1L));
        }

        if (reconcileDiscrepancy.getSystemTransactionId() == null) {
            reconcileDiscrepancy.setSystemTransactionId(-1L);
        }

        return reconcileDelegateService.getReconcileDiscrepancyService().save(reconcileDiscrepancy);
    }

}
