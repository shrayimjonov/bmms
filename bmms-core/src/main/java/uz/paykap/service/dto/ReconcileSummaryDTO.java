package uz.paykap.service.dto;

import java.time.Instant;
import java.time.LocalDate;
import javax.validation.constraints.*;
import java.io.Serializable;
import java.util.Objects;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import uz.paykap.domain.enumeration.ReconcileSummaryStatus;
import uz.paykap.domain.enumeration.ReconcilePeriod;
import uz.paykap.domain.enumeration.ReconcileSummaryType;

/**
 * A DTO for the {@link uz.paykap.domain.ReconcileSummary} entity.
 */
@ApiModel(description = "Данная форма предназначена для хранения общих данных о сверке")
public class ReconcileSummaryDTO extends AbstractAuditingDTO implements Serializable {

    private Long id;

    /**
     * Уникальный номер шлюза
     */
    @NotNull
    @ApiModelProperty(value = "Уникальный номер шлюза", required = true)
    private Long gatewayId;

    /**
     * Общая сумма системы
     */
    @NotNull
    @ApiModelProperty(value = "Общая сумма системы", required = true)
    private Long systemAmount;

    /**
     * Общая сумма шляза
     */
    @NotNull
    @ApiModelProperty(value = "Общая сумма шляза", required = true)
    private Long gatewayAmount;

    /**
     * Общее количество транзакций в системе
     */
    @NotNull
    @ApiModelProperty(value = "Общее количество транзакций в системе", required = true)
    private Long systemCount;

    /**
     * Общее количество транзакций у шлюза
     */
    @NotNull
    @ApiModelProperty(value = "Общее количество транзакций у шлюза", required = true)
    private Long gatewayCount;

    /**
     * Дата
     */
    @ApiModelProperty(value = "Дата")
    private LocalDate date;

    private ReconcileSummaryStatus status;

    /**
     * Успешна ли сверка
     */
    @ApiModelProperty(value = "Успешна ли сверка")
    private Boolean success;

    /**
     * Период (день , месяц) и т.д.
     */
    @ApiModelProperty(value = "Период (день , месяц) и т.д.")
    private ReconcilePeriod period;

    @Size(max = 64)
    private String gatewayName;

    private Instant fromDate;

    private Instant toDate;
    /**
     * Тип
     */
    @ApiModelProperty(value = "Тип")
    private ReconcileSummaryType type;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getGatewayId() {
        return gatewayId;
    }

    public void setGatewayId(Long gatewayId) {
        this.gatewayId = gatewayId;
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

    public Long getSystemCount() {
        return systemCount;
    }

    public void setSystemCount(Long systemCount) {
        this.systemCount = systemCount;
    }

    public Long getGatewayCount() {
        return gatewayCount;
    }

    public void setGatewayCount(Long gatewayCount) {
        this.gatewayCount = gatewayCount;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public ReconcileSummaryStatus getStatus() {
        return status;
    }

    public void setStatus(ReconcileSummaryStatus status) {
        this.status = status;
    }

    public Boolean getSuccess() {
        return success;
    }

    public Boolean isSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

    public ReconcilePeriod getPeriod() {
        return period;
    }

    public void setPeriod(ReconcilePeriod period) {
        this.period = period;
    }

    public String getGatewayName() {
        return gatewayName;
    }

    public void setGatewayName(String gatewayName) {
        this.gatewayName = gatewayName;
    }

    public Instant getFromDate() {
        return fromDate;
    }

    public void setFromDate(Instant fromDate) {
        this.fromDate = fromDate;
    }

    public Instant getToDate() {
        return toDate;
    }

    public void setToDate(Instant toDate) {
        this.toDate = toDate;
    }

    public ReconcileSummaryType getType() {
        return type;
    }

    public void setType(ReconcileSummaryType type) {
        this.type = type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ReconcileSummaryDTO reconcileSummaryDTO = (ReconcileSummaryDTO) o;
        if (reconcileSummaryDTO.getId() == null || getId() == null) {
            return false;
        }
        return Objects.equals(getId(), reconcileSummaryDTO.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "ReconcileSummaryDTO{" +
            "id=" + getId() +
            ", gatewayId=" + getGatewayId() +
            ", systemAmount=" + getSystemAmount() +
            ", gatewayAmount=" + getGatewayAmount() +
            ", systemCount=" + getSystemCount() +
            ", gatewayCount=" + getGatewayCount() +
            ", date='" + getDate() + "'" +
            ", status='" + getStatus() + "'" +
            ", success='" + getSuccess() + "'" +
            ", period='" + getPeriod() + "'" +
            ", gatewayName='" + getGatewayName() + "'" +
            ", fromDate='" + getFromDate() + "'" +
            ", toDate='" + getToDate() + "'" +
            ", type='" + getType() + "'" +
            "}";
    }
}
