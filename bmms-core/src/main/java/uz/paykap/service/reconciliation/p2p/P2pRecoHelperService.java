package uz.paykap.service.reconciliation.p2p;

import uz.devops.bmcore.reconciliation.BaseRecoHelperService;
import uz.devops.bmcore.reconciliation.dto.HasRecoSummary;
import uz.javlon.commons.base.BasicResult;
import uz.paykap.domain.ReconcileSummary;
import uz.paykap.domain.enumeration.P2pProcessorType;

import javax.validation.constraints.NotNull;
import java.time.Instant;

public interface P2pRecoHelperService extends BaseRecoHelperService {

    BasicResult compareP2pCreditHold(HasRecoSummary recoSummary, boolean matchCounts, P2pProcessorType p2pProcessorType, @NotNull Instant fromDate, @NotNull Instant toDate);

    BasicResult compareP2pCreditConfirm(HasRecoSummary recoSummary, boolean matchCounts, P2pProcessorType p2pProcessorType, @NotNull Instant fromDate, @NotNull Instant toDate);

    BasicResult compareP2pDebitHold(HasRecoSummary recoSummary, boolean matchCounts, P2pProcessorType p2pProcessorType, @NotNull Instant fromDate, @NotNull Instant toDate);

    BasicResult compareP2pDebitConfirm(HasRecoSummary recoSummary, boolean matchCounts, P2pProcessorType p2pProcessorType, @NotNull Instant fromDate, @NotNull Instant toDate);
}
