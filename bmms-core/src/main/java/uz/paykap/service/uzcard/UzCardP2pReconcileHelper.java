package uz.paykap.service.uzcard;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import uz.javlon.svgate.dto.BaseReportDTO;
import uz.javlon.svgate.dto.sv.ReportShortDTO;
import uz.javlon.svgate.dto.sv.ShortTranDTO;
import uz.javlon.svgate.service.SvGateReportService;
import uz.paykap.adapter.verification.pc.PcTranType;
import uz.paykap.config.ApplicationProperties;
import uz.paykap.service.humo.P2pRecoSummaryResult;
import uz.paykap.service.humo.ReconcileTranInfo;
import uz.paykap.utils.DateUtil;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Slf4j
@Service
@AllArgsConstructor
public class UzCardP2pReconcileHelper {

    private final SvGateReportService reportService;
    private final ApplicationProperties applicationProperties;

    public P2pRecoSummaryResult summaryReconcile(PcTranType pcTranType, Instant fromDate, Instant toDate) {
        ReportShortDTO reportShort = createRequestAndGetReportShort(fromDate, toDate);
        log.debug("uzcard reconcile result size: {}", reportShort != null && reportShort.getTrans() != null ? reportShort.getTrans().size() : 0);
        return calculateAndReturnSummaryResult(reportShort, pcTranType, fromDate, toDate);
    }

    private ReportShortDTO createRequestAndGetReportShort(Instant fromDate, Instant toDate) {
        BaseReportDTO baseReportDTO = new BaseReportDTO();
        baseReportDTO.setFrom(DateUtil.toLocalDateTime(fromDate));
        baseReportDTO.setTo(DateUtil.toLocalDateTime(toDate));
        log.debug("send uzcard reportService.reportShort() request: {}", baseReportDTO);
        return reportService.reportShort(baseReportDTO).getData(); // todo - fix
    }

    private P2pRecoSummaryResult calculateAndReturnSummaryResult(ReportShortDTO reportShortDTO, PcTranType pcTranType, Instant fromDate, Instant toDate) {
        String startWithPrefix = getPrefix(pcTranType);
        log.debug("prefix: {}", startWithPrefix);
        List<ShortTranDTO> filteredTrans = filterByPrefix(reportShortDTO, startWithPrefix);
        log.debug("filteredTrans size: {}", filteredTrans.size());
        return calculateSummary(filteredTrans);
    }

    private String getPrefix(PcTranType pcTranType) {
        log.debug("getPrefix({})", pcTranType);
        if (applicationProperties.getTransactionPrefix() == null){
            log.debug("applicationProperties.getTransactionPrefix() = null");
            return "";
        }
        if (Objects.equals(pcTranType, PcTranType.DEBIT)) {
            log.debug("return prefix: {}", applicationProperties.getTransactionPrefix().getP2pDebitPrefix());
            return applicationProperties.getTransactionPrefix().getP2pDebitPrefix();
        }
        if (Objects.equals(pcTranType, PcTranType.CREDIT)) {
            log.debug("return prefix: {}", applicationProperties.getTransactionPrefix().getP2pDebitPrefix());
            return applicationProperties.getTransactionPrefix().getP2pCreditPrefix();
        }

        log.debug("return empty string");
        return "";
    }

    private List<ShortTranDTO> filterByPrefix(ReportShortDTO reportShortDTO, String startWithPrefix) {
        if (reportShortDTO == null || reportShortDTO.getTrans() == null) {
            log.warn("uzcard reportShort result is null");
            return Collections.emptyList();
        }
        log.debug("begin filter by prefix: {}", startWithPrefix);
        return reportShortDTO.getTrans().stream()
                .filter(tran -> !StringUtils.isEmpty(tran.getExtId()))
                .filter(tran -> tran.getExtId().startsWith(startWithPrefix))
                .collect(Collectors.toList());
    }

    private P2pRecoSummaryResult calculateSummary(List<ShortTranDTO> filteredTrans) {
        log.debug("calculateSummary...");
        P2pRecoSummaryResult summaryResult = new P2pRecoSummaryResult();
        List<ReconcileTranInfo> tranInfos = new ArrayList<>();
        AtomicLong amount = new AtomicLong();
        AtomicLong count = new AtomicLong();

        filteredTrans.forEach(tran -> {
            tranInfos.add(new ReconcileTranInfo(verifyAndParseBmId(tran.getExtId()), tran.getSvId(), tran.getAmount(), true, tran.getDate().toInstant(ZoneOffset.ofHours(+5)), false));
            amount.addAndGet(tran.getAmount());
            count.incrementAndGet();
        });

        summaryResult.setProcessingTranList(tranInfos);
        summaryResult.setTotalAmountTiyin(amount.get());
        summaryResult.setTotalCount(count.get());

        log.debug("return summaryResult: {}", summaryResult);
        return summaryResult;
    }

    private Long verifyAndParseBmId(String bmId) {
        if (StringUtils.isEmpty(bmId)) {
            return -1L;
        }

        String id = StringUtils.substringAfterLast(bmId, "_");
        if (StringUtils.isEmpty(id) || !NumberUtils.isNumber(id)) {
            return -1L;
        }

        try {
            return Long.parseLong(id);
        } catch (NumberFormatException e) {
            return -1L;
        }
    }
}
