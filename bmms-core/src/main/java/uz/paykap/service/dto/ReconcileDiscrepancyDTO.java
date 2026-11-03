package uz.paykap.service.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;
import javax.validation.constraints.*;

/**
 * A DTO for the {@link uz.paykap.domain.ReconcileDiscrepancy} entity.
 */
@ApiModel(description = "Данная форма предназначена для хранения данных о транзакции при сверке")
public class ReconcileDiscrepancyDTO implements Serializable {

    private Long id;

    /**
     * Уникальное название шлюза
     */
    @NotNull
    @Size(max = 64)
    @ApiModelProperty(value = "Уникальное название шлюза", required = true)
    private String gatewayName;

    /**
     * Номер платежа в системе
     */
    @NotNull
    @ApiModelProperty(value = "Номер платежа в системе", required = true)
    private Long systemTransactionId;

    /**
     * Номер платежа в шлюзе
     */
    @NotNull
    @ApiModelProperty(value = "Номер платежа в шлюзе", required = true)
    private String gatewayTransactionId;

    /**
     * Сумма платежа в системе
     */
    @ApiModelProperty(value = "Сумма платежа в системе")
    private Long systemAmount;

    /**
     * Сумма платежа в шлюзе
     */
    @ApiModelProperty(value = "Сумма платежа в шлюзе")
    private Long gatewayAmount;

    /**
     * Статус транзакции в системе
     */
    @ApiModelProperty(value = "Статус транзакции в системе")
    private Boolean systemStatus;

    /**
     * Статус транзакции в шлюзе
     */
    @ApiModelProperty(value = "Статус транзакции в шлюзе")
    private Boolean gatewayStatus;

    /**
     * Дата транзакции в системе
     */
    @ApiModelProperty(value = "Дата транзакции в системе")
    private Instant systemDate;

    /**
     * Дата транзакции в шлюзе
     */
    @ApiModelProperty(value = "Дата транзакции в шлюзе")
    private Instant gatewayDate;

    /**
     * Финальный статус проблемы
     */
    @ApiModelProperty(value = "Финальный статус проблемы")
    private Boolean closed;

    /**
     * Дополнительная информация
     */
    @ApiModelProperty(value = "Дополнительная информация")
    private String comment;

    private ReconcileSummaryDTO reconcileSummary;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getGatewayName() {
        return gatewayName;
    }

    public void setGatewayName(String gatewayName) {
        this.gatewayName = gatewayName;
    }

    public Long getSystemTransactionId() {
        return systemTransactionId;
    }

    public void setSystemTransactionId(Long systemTransactionId) {
        this.systemTransactionId = systemTransactionId;
    }

    public String getGatewayTransactionId() {
        return gatewayTransactionId;
    }

    public void setGatewayTransactionId(String gatewayTransactionId) {
        this.gatewayTransactionId = gatewayTransactionId;
    }

    public Long getSystemAmount() {
        return systemAmount;
    }

    public void setSystemAmount(Long systemAmount) {
        this.systemAmount = systemAmount;
    }

    public Long getGatewayAmount() {
        return gatewayAmount;
    }

    public void setGatewayAmount(Long gatewayAmount) {
        this.gatewayAmount = gatewayAmount;
    }

    public Boolean getSystemStatus() {
        return systemStatus;
    }

    public void setSystemStatus(Boolean systemStatus) {
        this.systemStatus = systemStatus;
    }

    public Boolean getGatewayStatus() {
        return gatewayStatus;
    }

    public void setGatewayStatus(Boolean gatewayStatus) {
        this.gatewayStatus = gatewayStatus;
    }

    public Instant getSystemDate() {
        return systemDate;
    }

    public void setSystemDate(Instant systemDate) {
        this.systemDate = systemDate;
    }

    public Instant getGatewayDate() {
        return gatewayDate;
    }

    public void setGatewayDate(Instant gatewayDate) {
        this.gatewayDate = gatewayDate;
    }

    public Boolean getClosed() {
        return closed;
    }

    public void setClosed(Boolean closed) {
        this.closed = closed;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public ReconcileSummaryDTO getReconcileSummary() {
        return reconcileSummary;
    }

    public void setReconcileSummary(ReconcileSummaryDTO reconcileSummary) {
        this.reconcileSummary = reconcileSummary;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ReconcileDiscrepancyDTO)) {
            return false;
        }

        ReconcileDiscrepancyDTO reconcileDiscrepancyDTO = (ReconcileDiscrepancyDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, reconcileDiscrepancyDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "ReconcileDiscrepancyDTO{" +
            "id=" + getId() +
            ", gatewayName='" + getGatewayName() + "'" +
            ", systemTransactionId=" + getSystemTransactionId() +
            ", gatewayTransactionId='" + getGatewayTransactionId() + "'" +
            ", systemAmount=" + getSystemAmount() +
            ", gatewayAmount=" + getGatewayAmount() +
            ", systemStatus='" + getSystemStatus() + "'" +
            ", gatewayStatus='" + getGatewayStatus() + "'" +
            ", systemDate='" + getSystemDate() + "'" +
            ", gatewayDate='" + getGatewayDate() + "'" +
            ", closed='" + getClosed() + "'" +
            ", comment='" + getComment() + "'" +
            ", reconcileSummary=" + getReconcileSummary() +
            "}";
    }
}
