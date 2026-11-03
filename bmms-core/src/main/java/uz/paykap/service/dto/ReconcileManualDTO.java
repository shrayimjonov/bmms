package uz.paykap.service.dto;

import java.time.Instant;
import java.time.LocalDate;

public class ReconcileManualDTO {
    private String gatewayService;
    private LocalDate localDate;

    public LocalDate getLocalDate() {
        return localDate;
    }

    public void setLocalDate(LocalDate localDate) {
        this.localDate = localDate;
    }

    private Instant fromTime;
    private Instant toTime;

    public String getGatewayService() {
        return gatewayService;
    }

    public void setGatewayService(String gatewayService) {
        this.gatewayService = gatewayService;
    }

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

    @Override
    public String toString() {
        return "ReconcileManualDTO{" +
            "gatewayService='" + gatewayService + '\'' +
            ", localDate=" + localDate +
            ", fromTime=" + fromTime +
            ", toTime=" + toTime +
            '}';
    }
}
