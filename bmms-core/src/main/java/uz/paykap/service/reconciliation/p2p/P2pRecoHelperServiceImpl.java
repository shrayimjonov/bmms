package uz.paykap.service.reconciliation.p2p;

import com.google.common.collect.Sets;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.internal.util.collections.CollectionHelper;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import uz.devops.bmcore.enumeration.PayType;
import uz.devops.bmcore.reconciliation.dto.CompareSchema;
import uz.devops.bmcore.reconciliation.dto.HasRecoSummary;
import uz.devops.bmcore.reconciliation.dto.RecoTranDTO;
import uz.devops.pusher.event.PusherEvent;
import uz.javlon.commons.base.BasicResult;
import uz.javlon.commons.base.BasicResultData;
import uz.javlon.commons.exception.CodedException;
import uz.javlon.commons.exception.ProblemEnum;
import uz.paykap.domain.ReconcileDiscrepancy;
import uz.paykap.domain.ReconcileSummary;
import uz.paykap.domain.enumeration.P2pProcessorType;
import uz.paykap.domain.enumeration.ReconcileSummaryStatus;
import uz.paykap.domain.enumeration.ReconcileSummaryType;
import uz.paykap.repository.TransactionRepository;
import uz.paykap.service.ReconcileDiscrepancyService;
import uz.paykap.service.ReconcileSummaryService;
import uz.paykap.service.humo.P2pRecoSummaryResult;
import uz.paykap.service.humo.ReconcileTranInfo;

import javax.validation.constraints.NotNull;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;

@Slf4j
@Service
@AllArgsConstructor
public class P2pRecoHelperServiceImpl implements P2pRecoHelperService {

    public static final String P_2_P_CREDIT_CONFIRM = "_P2P_CREDIT_CONFIRM";
    public static final String P_2_P_DEBIT_CONFIRM = "_P2P_DEBIT_CONFIRM";
    public static final String P_2_P_RECONCILE_ERROR = "P2P_RECONCILE_ERROR";
    private final TransactionRepository transactionRepository;
    private final ReconcileSummaryService reconcileSummaryService;
    private final ApplicationEventPublisher eventPublisher;
    private final ReconcileDiscrepancyService reconcileDiscrepancyService;

    @Override
    public BasicResult compareP2pCreditHold(HasRecoSummary recoSummary, boolean matchCounts, P2pProcessorType p2pProcessorType, Instant fromDate, Instant toDate) {
        throw new CodedException(ProblemEnum.NOT_IMPLEMENTED);
    }

    @Override
    public BasicResult compareP2pCreditConfirm(HasRecoSummary pcReconcileSummary, boolean matchCounts, P2pProcessorType p2pProcessorType, Instant fromDate, Instant toDate) {
        log.debug("compareP2pCreditConfirm...");
        log.debug("getSystemP2pCreditSummary(), preocessorType: {}. fromDate: {}, toDate: {}", p2pProcessorType, fromDate, toDate);
        HasRecoSummary systemP2pCreditSummary = getSystemP2pCreditSummary(p2pProcessorType, fromDate, toDate);
        log.debug("system p2p credit summary, count: {}, amount: {}", systemP2pCreditSummary.getTotalCount(), systemP2pCreditSummary.getTotalAmountTiyin());
        if (systemP2pCreditSummary.getTotalAmountTiyin() == null) {
            log.warn("compareP2pCreditConfirm() result null");
            return new BasicResult(false, "compareP2pCreditConfirm() result null");
        }

        if (!Objects.equals(systemP2pCreditSummary.getTotalAmountTiyin(), pcReconcileSummary.getTotalAmountTiyin())) {
            log.warn("p2p credit summary of amounts differs : [{}:{}] != [system:{}]", p2pProcessorType.name(), pcReconcileSummary.getTotalAmountTiyin(), systemP2pCreditSummary.getTotalAmountTiyin());
            ReconcileSummary reconcileSummary = reconcileSummaryService.saveP2pReconcileSummaryWithType(false, p2pProcessorType.name() + P_2_P_CREDIT_CONFIRM, fromDate, toDate, pcReconcileSummary.getTotalAmountTiyin(), pcReconcileSummary.getTotalCount(), systemP2pCreditSummary.getTotalAmountTiyin(), systemP2pCreditSummary.getTotalCount(), ReconcileSummaryStatus.COMPARED_FALSE, ReconcileSummaryType.P2P);
            sendPushNotification(reconcileSummary, fromDate, toDate);
            findDifferenceAndSave(pcReconcileSummary, fromDate, toDate, P_2_P_CREDIT_CONFIRM, reconcileSummary);
            return new BasicResult(false, "Summary of amounts differs : [gateway:" + pcReconcileSummary.getTotalAmountTiyin() + "] != [system:" + systemP2pCreditSummary.getTotalAmountTiyin() + "]");
        }

        if (matchCounts && !Objects.equals(systemP2pCreditSummary.getTotalCount(), pcReconcileSummary.getTotalCount())) {
            log.warn("Summary of counts differs : [{}:{}] != [system:{}]", p2pProcessorType.name(), pcReconcileSummary.getTotalCount(), systemP2pCreditSummary.getTotalCount());
            ReconcileSummary reconcileSummary = reconcileSummaryService.saveP2pReconcileSummaryWithType(false, p2pProcessorType.name() + P_2_P_CREDIT_CONFIRM, fromDate, toDate, pcReconcileSummary.getTotalAmountTiyin(), pcReconcileSummary.getTotalCount(), systemP2pCreditSummary.getTotalAmountTiyin(), systemP2pCreditSummary.getTotalCount(), ReconcileSummaryStatus.COMPARED_FALSE, ReconcileSummaryType.P2P);
            sendPushNotification(reconcileSummary, fromDate, toDate);
            findDifferenceAndSave(pcReconcileSummary, fromDate, toDate, P_2_P_CREDIT_CONFIRM, reconcileSummary);
            return new BasicResult(false, "Summary of accounts differs : [" + p2pProcessorType.name() + ":" + pcReconcileSummary.getTotalCount() + "] != [system:" + pcReconcileSummary.getTotalCount() + "]");
        }

        log.debug("p2p credit reconcile is success, for: {}", p2pProcessorType.name());
        reconcileSummaryService.saveP2pReconcileSummaryWithType(true, p2pProcessorType.name() + P_2_P_CREDIT_CONFIRM, fromDate, toDate, pcReconcileSummary.getTotalAmountTiyin(), pcReconcileSummary.getTotalCount(), systemP2pCreditSummary.getTotalAmountTiyin(), systemP2pCreditSummary.getTotalCount(), ReconcileSummaryStatus.COMPARED_OK, ReconcileSummaryType.P2P);

        return new BasicResult(true, "Successfully compared summary with " + p2pProcessorType.name());
    }

    @Override
    public BasicResult compareP2pDebitHold(HasRecoSummary recoSummary, boolean matchCounts, P2pProcessorType p2pProcessorType, Instant fromDate, Instant toDate) {
        throw new CodedException(ProblemEnum.NOT_IMPLEMENTED);
    }

    @Override
    public BasicResult compareP2pDebitConfirm(HasRecoSummary recoSummary, boolean matchCounts, P2pProcessorType p2pProcessorType, Instant fromDate, Instant toDate) {

        HasRecoSummary systemP2pDebitSummary = transactionRepository.getP2pDebitSummary(PayType.P2P, p2pProcessorType.name(), fromDate, toDate);

        if (systemP2pDebitSummary.getTotalAmountTiyin() == null) {
            log.warn("compareP2pCreditConfirm() result null");
            return new BasicResult(false, "compareP2pCreditConfirm() result null");
        }

        if (!Objects.equals(systemP2pDebitSummary.getTotalAmountTiyin(), recoSummary.getTotalAmountTiyin())) {
            log.warn("p2p credit summary of amounts differs : [{}:{}] != [system:{}]", p2pProcessorType.name(), recoSummary.getTotalAmountTiyin(), systemP2pDebitSummary.getTotalAmountTiyin());
            ReconcileSummary reconcileSummary = reconcileSummaryService.saveP2pReconcileSummaryWithType(false, p2pProcessorType.name() + P_2_P_DEBIT_CONFIRM, fromDate, toDate, recoSummary.getTotalAmountTiyin(), recoSummary.getTotalCount(), systemP2pDebitSummary.getTotalAmountTiyin(), systemP2pDebitSummary.getTotalCount(), ReconcileSummaryStatus.COMPARED_FALSE, ReconcileSummaryType.P2P);
            sendPushNotification(reconcileSummary, fromDate, toDate);
            findDifferenceAndSave(recoSummary, fromDate, toDate, P_2_P_DEBIT_CONFIRM, reconcileSummary);
            return new BasicResult(false, "Summary of amounts differs : [gateway:" + recoSummary.getTotalAmountTiyin() + "] != [system:" + systemP2pDebitSummary.getTotalAmountTiyin() + "]");
        }

        if (matchCounts && !Objects.equals(systemP2pDebitSummary.getTotalCount(), recoSummary.getTotalCount())) {
            log.warn("Summary of counts differs : [{}:{}] != [system:{}]", p2pProcessorType.name(), recoSummary.getTotalCount(), systemP2pDebitSummary.getTotalCount());
            ReconcileSummary reconcileSummary = reconcileSummaryService.saveP2pReconcileSummaryWithType(false, p2pProcessorType.name() + P_2_P_DEBIT_CONFIRM, fromDate, toDate, recoSummary.getTotalAmountTiyin(), recoSummary.getTotalCount(), systemP2pDebitSummary.getTotalAmountTiyin(), systemP2pDebitSummary.getTotalCount(), ReconcileSummaryStatus.COMPARED_FALSE, ReconcileSummaryType.P2P);
            sendPushNotification(reconcileSummary, fromDate, toDate);
            findDifferenceAndSave(recoSummary, fromDate, toDate, P_2_P_DEBIT_CONFIRM, reconcileSummary);
            return new BasicResult(false, "Summary of accounts differs : [" + p2pProcessorType.name() + ":" + recoSummary.getTotalCount() + "] != [system:" + recoSummary.getTotalCount() + "]");
        }

        log.debug("p2p credit reconcile is success, for: {}", p2pProcessorType.name());
        reconcileSummaryService.saveP2pReconcileSummaryWithType(true, p2pProcessorType.name() + P_2_P_DEBIT_CONFIRM, fromDate, toDate, recoSummary.getTotalAmountTiyin(), recoSummary.getTotalCount(), systemP2pDebitSummary.getTotalAmountTiyin(), systemP2pDebitSummary.getTotalCount(), ReconcileSummaryStatus.COMPARED_OK, ReconcileSummaryType.P2P);

        return new BasicResult(true, "Successfully compared summary with " + p2pProcessorType.name());
    }

    private void sendPushNotification(ReconcileSummary reconcileSummary, Instant fromDate, Instant toDate) {
        List<String> tags = List.of(P_2_P_RECONCILE_ERROR);

        Map<String, String> params = new HashMap<>();
        params.put("RECONCILE_ID", String.valueOf(reconcileSummary.getId()));
        params.put("PC_AMOUNT", String.valueOf(reconcileSummary.getGatewayAmount()));
        params.put("PC_COUNT", String.valueOf(reconcileSummary.getGatewayCount()));
        params.put("SYSTEM_AMOUNT", String.valueOf(reconcileSummary.getSystemAmount()));
        params.put("SYSTEM_COUNT", String.valueOf(reconcileSummary.getSystemCount()));
        params.put("PC_NAME", P2pProcessorType.HUMO.name());
        params.put("DATE", getFormattedDate(fromDate));
        params.put("TIME_FROM", getFormattedTime(fromDate));
        params.put("TIME_TO", getFormattedTime(toDate));

        PusherEvent pusherEvent = new PusherEvent(this, params, tags);
        log.debug("send push notify: {}", pusherEvent);
        eventPublisher.publishEvent(pusherEvent);
    }

    private String getFormattedTime(Instant fromDate) {
        ZonedDateTime zonedDateTime = fromDate.atZone(ZoneId.of("+5"));
        String hour = zonedDateTime.getHour() < 10 ? "0" + zonedDateTime.getHour() : String.valueOf(zonedDateTime.getHour());
        String minute = zonedDateTime.getMinute() < 10 ? "0" + zonedDateTime.getMinute() : String.valueOf(zonedDateTime.getMinute());
        String second = zonedDateTime.getSecond() < 10 ? "0" + zonedDateTime.getSecond() : String.valueOf(zonedDateTime.getSecond());
        return  hour + ":" + minute + ":" + second;
    }

    private String getFormattedDate(Instant fromDate) {
        ZonedDateTime zonedDateTime = fromDate.atZone(ZoneId.of("+5"));
        int monthValue = zonedDateTime.getMonthValue();
        String formattedMonthValue = monthValue < 10 ? "0" + monthValue : String.valueOf(monthValue);
        String formattedDayValue = zonedDateTime.getDayOfMonth() < 10 ? "0" + zonedDateTime.getDayOfMonth() : String.valueOf(zonedDateTime.getDayOfMonth());
        return formattedDayValue + "-" + formattedMonthValue + "-" + zonedDateTime.getYear();
    }

    public void findDifferenceAndSave(HasRecoSummary recoSummary, Instant fromDate, Instant toDate, String tranType, ReconcileSummary reconcileSummary) {

        P2pRecoSummaryResult reconcileDto = castToP2pReconcileResult(recoSummary, new P2pRecoSummaryResult());
        if (CollectionHelper.isEmpty(reconcileDto.getProcessingTranList())) {
            log.warn("processing transaction is empty for detail compare");
            return;
        }

        Set<ReconcileTranInfo> systemTrans = extractSystemTrans(fromDate, toDate, tranType);
        List<ReconcileTranInfo> processingTrans = reconcileDto.getProcessingTranList();

        var differenceTrans = findDifference(systemTrans, processingTrans);
        saveDifference(reconcileSummary, differenceTrans);
    }

    private P2pRecoSummaryResult castToP2pReconcileResult(HasRecoSummary recoSummary, P2pRecoSummaryResult reconcileDto) {
        if (!(recoSummary instanceof P2pRecoSummaryResult)) {
            return reconcileDto;
        }

        try {
            return  (P2pRecoSummaryResult) recoSummary;
        } catch (ClassCastException e) {
            e.printStackTrace();
            return reconcileDto;
        }
    }

    private Sets.SetView<ReconcileTranInfo> findDifference(Set<ReconcileTranInfo> systemTrans, List<ReconcileTranInfo> processingTrans) {
        return Sets.symmetricDifference(systemTrans, new HashSet<>(processingTrans));
    }

    private Set<ReconcileTranInfo> extractSystemTrans(Instant fromDate, Instant toDate, String tranType) {
        if (Objects.equals(tranType, P_2_P_CREDIT_CONFIRM)) {
            return transactionRepository.getCreditTransactionsByReconcile(PayType.P2P, P2pProcessorType.HUMO.name(), fromDate, toDate);
        }

        if (Objects.equals(tranType, P_2_P_DEBIT_CONFIRM)) {
            return transactionRepository.getDebitTransactionsByReconcile(PayType.P2P, P2pProcessorType.HUMO.name(), fromDate, toDate);
        }
        return new HashSet<>();
    }

    private void saveDifference(ReconcileSummary reconcileSummary, Sets.SetView<ReconcileTranInfo> differenceTrans) {
        log.debug("save difference...., count: {}", differenceTrans.size());
        differenceTrans.forEach(differenceTran -> saveNotFoundReconcileDiscrepancy(differenceTran, reconcileSummary));
    }

    private void saveNotFoundReconcileDiscrepancy(ReconcileTranInfo tran, ReconcileSummary reconcileSummary) {
        Optional<ReconcileDiscrepancy> optionalReconcileDiscrepancy = reconcileDiscrepancyService.findBySystemTranId(tran.getBmId());
        ReconcileDiscrepancy reconcileDiscrepancy = new ReconcileDiscrepancy();
        if (optionalReconcileDiscrepancy.isEmpty()) {
            reconcileDiscrepancy = new ReconcileDiscrepancy();
        }
        reconcileDiscrepancy.setReconcileSummary(reconcileSummary);
        reconcileDiscrepancy.setGatewayName(P2pProcessorType.HUMO.name());
        reconcileDiscrepancy.setGatewayAmount(tran.getAmount());
        reconcileDiscrepancy.setGatewayTransactionId(tran.getProcessingId());
        reconcileDiscrepancy.setGatewayStatus(Objects.equals(tran.getIsSystemTran(), Boolean.FALSE) && tran.getStatus());
        reconcileDiscrepancy.setGatewayDate(Objects.equals(tran.getIsSystemTran(), Boolean.FALSE) ? tran.getDate() : null);

        reconcileDiscrepancy.setSystemTransactionId(tran.getBmId());
        reconcileDiscrepancy.setSystemAmount(tran.getAmount());
        reconcileDiscrepancy.setSystemStatus(Objects.equals(tran.getIsSystemTran(), Boolean.TRUE) && tran.getStatus());
        reconcileDiscrepancy.setSystemDate(Objects.equals(tran.getIsSystemTran(), Boolean.TRUE) ? tran.getDate() : null);
        reconcileDiscrepancy.setComment("reconcile summary error");
        reconcileDiscrepancy.setClosed(false);

        reconcileDiscrepancyService.save(reconcileDiscrepancy);
    }

    @Override
    public BasicResultData<HasRecoSummary> getSummary(String s, @NotNull Instant instant, @NotNull Instant instant1) {
        throw new CodedException(ProblemEnum.NOT_IMPLEMENTED);
    }

    @Override
    public BasicResultData<HashSet<RecoTranDTO>> getDetailed(String s, @NotNull Instant instant, @NotNull Instant instant1) {
        throw new CodedException(ProblemEnum.NOT_IMPLEMENTED);
    }

    @Override
    public BasicResult compare(HasRecoSummary hasRecoSummary, boolean b, String s, @NotNull Instant instant, @NotNull Instant instant1) {
        return new BasicResult(true, "success");
    }

    @Override
    public BasicResult compare(HashSet<RecoTranDTO> hashSet, CompareSchema compareSchema, String s, @NotNull Instant instant, @NotNull Instant instant1) {
        return new BasicResult(true, "success");
    }

    private HasRecoSummary getSystemP2pCreditSummary(P2pProcessorType p2pProcessorType, Instant fromDate, Instant toDate) {
        HasRecoSummary p2pCreditSummary = new P2pRecoSummaryResult();
        try {
            p2pCreditSummary = transactionRepository.getP2pCreditSummary(PayType.P2P, p2pProcessorType.name(), fromDate, toDate);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return p2pCreditSummary;
    }
}
