package uz.paykap.adapter.verification.dto;

import org.apache.commons.lang3.builder.ToStringBuilder;

import java.time.Instant;

public class ReconciliationRequestDTO {

    private Instant toTime;
    private Instant fromTime;
    private String gatewayName;
    private String recipientShortName;

    public String getRecipientShortName() {
        return recipientShortName;
    }

    public void setRecipientShortName(String recipientShortName) {
        this.recipientShortName = recipientShortName;
    }

    public String getGatewayName() {
        return gatewayName;
    }

    public void setGatewayName(String gatewayName) {
        this.gatewayName = gatewayName;
    }

    public Instant getToTime() {
        return toTime;
    }

    public void setToTime(Instant toTime) {
        this.toTime = toTime;
    }

    public Instant getFromTime() {
        return fromTime;
    }

    public void setFromTime(Instant fromTime) {
        this.fromTime = fromTime;
    }


    public ReconciliationRequestDTO toTime(Instant toTime) {
        this.toTime = toTime;
        return this;
    }

    public ReconciliationRequestDTO fromTime(Instant fromTime) {
        this.fromTime = fromTime;
        return this;
    }

    public ReconciliationRequestDTO gatewayName(String gatewayName) {
        this.gatewayName = gatewayName;
        return this;
    }

    public ReconciliationRequestDTO recipientShortName(String recipientShortName) {
        this.recipientShortName = recipientShortName;
        return this;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
            .append("toTime", toTime)
            .append("fromTime", fromTime)
            .append("gatewayName", gatewayName)
            .append("recipientShortName", recipientShortName)
            .toString();
    }
}
