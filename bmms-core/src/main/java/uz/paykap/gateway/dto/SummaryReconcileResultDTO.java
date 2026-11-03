package uz.paykap.gateway.dto;

import java.util.List;

public class SummaryReconcileResultDTO {

    private Long totalAmount;

    private Long totalCount;

    private List<ReconcileResultDTO> reconcileResultDTOS;

    public SummaryReconcileResultDTO(Long totalAmount, Long totalCount, List<ReconcileResultDTO> reconcileResultDTOS) {
        this.totalAmount = totalAmount;
        this.totalCount = totalCount;
        this.reconcileResultDTOS = reconcileResultDTOS;
    }

    @Override
    public String toString() {
        return "SummaryReconcileResultDTO{" +
            "totalAmount=" + totalAmount +
            ", totalCount=" + totalCount +
            ", reconcileResultDTOS=" + reconcileResultDTOS +
            '}';
    }

    public Long getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(Long totalAmount) {
        this.totalAmount = totalAmount;
    }

    public Long getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(Long totalCount) {
        this.totalCount = totalCount;
    }

    public List<ReconcileResultDTO> getReconcileResultDTOS() {
        return reconcileResultDTOS;
    }

    public void setReconcileResultDTOS(List<ReconcileResultDTO> reconcileResultDTOS) {
        this.reconcileResultDTOS = reconcileResultDTOS;
    }
}
