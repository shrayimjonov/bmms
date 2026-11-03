package uz.paykap.service.dto;

public class ContractPaymentStatusDTO {

    private Boolean status;

    private String operationId;

    private String comment;

    public Boolean getStatus() {
        return status;
    }

    public ContractPaymentStatusDTO setStatus(Boolean status) {
        this.status = status;
        return this;
    }

    public String getOperationId() {
        return operationId;
    }

    public ContractPaymentStatusDTO setOperationId(String operationId) {
        this.operationId = operationId;
        return this;
    }

    public String getComment() {
        return comment;
    }

    public ContractPaymentStatusDTO setComment(String comment) {
        this.comment = comment;
        return this;
    }

    @Override
    public String toString() {
        return "ContractPaymentStatusDTO{" +
            "status=" + status +
            ", operationId='" + operationId + '\'' +
            ", comment='" + comment + '\'' +
            '}';
    }
}
