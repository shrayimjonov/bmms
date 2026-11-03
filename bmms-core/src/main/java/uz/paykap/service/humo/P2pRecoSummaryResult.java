package uz.paykap.service.humo;

import lombok.*;
import uz.devops.bmcore.reconciliation.dto.HasRecoSummary;

import java.util.List;

@ToString
@EqualsAndHashCode
public class P2pRecoSummaryResult implements HasRecoSummary {
    private Long totalAmountTiyin;
    private Long totalCount;
    private List<ReconcileTranInfo> processingTranList;

    public P2pRecoSummaryResult() {
    }

    public P2pRecoSummaryResult(Long totalAmountTiyin, Long totalCount) {
        this.totalAmountTiyin = totalAmountTiyin;
        this.totalCount = totalCount;
    }

    public P2pRecoSummaryResult(Long totalAmountTiyin, Long totalCount, List<ReconcileTranInfo> processingTranList) {
        this.totalAmountTiyin = totalAmountTiyin;
        this.totalCount = totalCount;
        this.processingTranList = processingTranList;
    }

    public void setTotalAmountTiyin(Long totalAmountTiyin) {
        this.totalAmountTiyin = totalAmountTiyin;
    }

    public void setTotalCount(Long totalCount) {
        this.totalCount = totalCount;
    }

    @Override
    public Long getTotalAmountTiyin() {
        return totalAmountTiyin;
    }

    @Override
    public Long getTotalCount() {
        return totalCount;
    }

    public List<ReconcileTranInfo> getProcessingTranList() {
        return processingTranList;
    }

    public void setProcessingTranList(List<ReconcileTranInfo> processingTranList) {
        this.processingTranList = processingTranList;
    }
}
