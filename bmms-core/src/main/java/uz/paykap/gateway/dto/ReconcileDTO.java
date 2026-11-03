package uz.paykap.gateway.dto;

import java.time.Instant;

public class ReconcileDTO {

    private Instant fromTime;

    private Instant toTime;

    private Long recipientId;

    public Instant getFromTime() {
        return fromTime;
    }

    public void setFromTime(Instant fromTime) {
        this.fromTime = fromTime;
    }

    public Instant getToTime() {
        return toTime;
    }

    public void setToTime(Instant toTime) {
        this.toTime = toTime;
    }

    public Long getRecipientId() {
        return recipientId;
    }

    public void setRecipientId(Long recipientId) {
        this.recipientId = recipientId;
    }

    @Override
    public String toString() {
        return "ReconcileDTO{" +
            "fromTime=" + fromTime +
            ", toTime=" + toTime +
            ", recipientId=" + recipientId +
            '}';
    }
}
