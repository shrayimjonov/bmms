package uz.paykap.service.humo;

import lombok.*;

import java.time.Instant;
import java.util.Objects;

@Getter
@Setter
@ToString
@NoArgsConstructor
public class ReconcileTranInfo {
    private Long bmId;
    private String processingId;
    private Long amount;
    private Boolean status;
    private Instant date;
    private Boolean isSystemTran;

    public ReconcileTranInfo(Long bmId, String processingId, Long amount, Boolean status, Instant date, Boolean isSystemTran) {
        this.bmId = bmId;
        this.processingId = processingId;
        this.amount = amount;
        this.status = status;
        this.date = date;
        this.isSystemTran = isSystemTran;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ReconcileTranInfo that = (ReconcileTranInfo) o;
        return Objects.equals(bmId, that.bmId) && Objects.equals(processingId, that.processingId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(bmId, processingId);
    }
}
