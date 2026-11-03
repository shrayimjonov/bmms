package uz.paykap.gateway.dto;

import java.io.Serializable;

public class PaymentResultDTO implements Serializable {

    private Long id;

    private PaymentStatus status;

    private Integer statusCode;

    private String statusMessage;

    private String operatorData;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public void setStatus(PaymentStatus status) {
        this.status = status;
    }

    public Integer getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(Integer statusCode) {
        this.statusCode = statusCode;
    }

    public String getStatusMessage() {
        return statusMessage;
    }

    public void setStatusMessage(String statusMessage) {
        this.statusMessage = statusMessage;
    }

    public String getOperatorData() {
        return operatorData;
    }

    public void setOperatorData(String operatorData) {
        this.operatorData = operatorData;
    }

    @Override
    public String toString() {
        return "PaymentResultDTO{" +
            "id=" + id +
            ", status=" + status +
            ", statusCode=" + statusCode +
            ", statusMessage='" + statusMessage + '\'' +
            ", operatorData='" + operatorData + '\'' +
            '}';
    }
}
