package uz.paykap.gateway.dto;

import java.time.Instant;

public class ReconcileResultDTO {


    private String agentTransactionId;

    private Long providerTransactionId;

    private Long amount;

    private String errorMsg;

    private String account;

    private String serviceName;

    private Long serviceId;

    private Instant transactionTime;

    public ReconcileResultDTO(String agentTransactionId, Long providerTransactionId, Long amount, String errorMsg, String account, String serviceName, Long serviceId, Instant transactionTime) {
        this.agentTransactionId = agentTransactionId;
        this.providerTransactionId = providerTransactionId;
        this.amount = amount;
        this.errorMsg = errorMsg;
        this.account = account;
        this.serviceName = serviceName;
        this.serviceId = serviceId;
        this.transactionTime = transactionTime;
    }

    public ReconcileResultDTO(String agentTransactionId, Long providerTransactionId, Long amount, String errorMsg, Instant transactionTime) {
        this.agentTransactionId = agentTransactionId;
        this.providerTransactionId = providerTransactionId;
        this.amount = amount;
        this.errorMsg = errorMsg;
        this.transactionTime = transactionTime;
    }

    @Override
    public String toString() {
        return "ReconcileResultDTO{" +
            "agentTransactionId=" + agentTransactionId +
            ", providerTransactionId=" + providerTransactionId +
            ", amount=" + amount +
            ", errorMsg='" + errorMsg + '\'' +
            ", account='" + account + '\'' +
            ", serviceName='" + serviceName + '\'' +
            ", serviceId='" + serviceId + '\'' +
            ", transactionTime=" + transactionTime +
            '}';
    }

    public Instant getTransactionTime() {
        return transactionTime;
    }

    public void setTransactionTime(Instant transactionTime) {
        this.transactionTime = transactionTime;
    }

    public String getTransactionId() {
        return agentTransactionId;
    }

    public void setTransactionId(String agentTransactionId) {
        this.agentTransactionId = agentTransactionId;
    }

    public Long getProviderTransactionId() {
        return providerTransactionId;
    }

    public void setProviderTransactionId(Long providerTransactionId) {
        this.providerTransactionId = providerTransactionId;
    }

    public Long getAmount() {
        return amount;
    }

    public void setAmount(Long amount) {
        this.amount = amount;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public Long getServiceId() {
        return serviceId;
    }

    public void setServiceId(Long serviceId) {
        this.serviceId = serviceId;
    }


}
