package uz.paykap.domain;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.persistence.*;
import javax.validation.constraints.*;

import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Objects;

import org.javers.core.metamodel.annotation.DiffIgnore;
import uz.paykap.domain.enumeration.ReconcileSummaryStatus;

import uz.paykap.domain.enumeration.ReconcilePeriod;

import uz.paykap.domain.enumeration.ReconcileSummaryType;

/**
 * Данная форма предназначена для хранения общих данных о сверке
 */
@ApiModel(description = "Данная форма предназначена для хранения общих данных о сверке")
@Entity
@Table(name = "reconcile_summary")
@DiffIgnore
public class ReconcileSummary extends AbstractAuditingEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    private Long id;

    /**
     * Уникальный номер шлюза
     */
    @NotNull
    @ApiModelProperty(value = "Уникальный номер шлюза", required = true)
    @Column(name = "gateway_id", nullable = false)
    private Long gatewayId;

    /**
     * Общая сумма системы
     */
    @NotNull
    @ApiModelProperty(value = "Общая сумма системы", required = true)
    @Column(name = "system_amount", nullable = false)
    private Long systemAmount;

    /**
     * Общая сумма шляза
     */
    @NotNull
    @ApiModelProperty(value = "Общая сумма шляза", required = true)
    @Column(name = "gateway_amount", nullable = false)
    private Long gatewayAmount;

    /**
     * Общее количество транзакций в системе
     */
    @NotNull
    @ApiModelProperty(value = "Общее количество транзакций в системе", required = true)
    @Column(name = "system_count", nullable = false)
    private Long systemCount;

    /**
     * Общее количество транзакций у шлюза
     */
    @NotNull
    @ApiModelProperty(value = "Общее количество транзакций у шлюза", required = true)
    @Column(name = "gateway_count", nullable = false)
    private Long gatewayCount;

    /**
     * Дата
     */
    @ApiModelProperty(value = "Дата")
    @Column(name = "date")
    private LocalDate date;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private ReconcileSummaryStatus status;

    /**
     * Успешна ли сверка
     */
    @ApiModelProperty(value = "Успешна ли сверка")
    @Column(name = "success")
    private Boolean success;

    /**
     * Период (день , месяц) и т.д.
     */
    @ApiModelProperty(value = "Период (день , месяц) и т.д.")
    @Enumerated(EnumType.STRING)
    @Column(name = "period")
    private ReconcilePeriod period;

    @Size(max = 64)
    @Column(name = "gateway_name", length = 64)
    private String gatewayName;

    /**
     * с Дата
     */
    @ApiModelProperty(value = "с Дата")
    @Column(name = "from_date")
    private Instant fromDate;

    /**
     * до Дата
     */
    @ApiModelProperty(value = "до Дата")
    @Column(name = "to_date")
    private Instant toDate;

    /**
     * Тип
     */
    @ApiModelProperty(value = "Тип")
    @Enumerated(EnumType.STRING)
    @Column(name = "type")
    private ReconcileSummaryType type = ReconcileSummaryType.PAYMENT;

    // jhipster-needle-entity-add-field - JHipster will add fields here, do not remove
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ReconcileSummary id(Long id) {
        this.id = id;
        return this;
    }

    public Long getGatewayId() {
        return this.gatewayId;
    }

    public ReconcileSummary gatewayId(Long gatewayId) {
        this.gatewayId = gatewayId;
        return this;
    }

    public void setGatewayId(Long gatewayId) {
        this.gatewayId = gatewayId;
    }

    public Long getSystemAmount() {
        return this.systemAmount;
    }

    public ReconcileSummary systemAmount(Long systemAmount) {
        this.systemAmount = systemAmount;
        return this;
    }

    public void setSystemAmount(Long systemAmount) {
        this.systemAmount = systemAmount;
    }

    public Long getGatewayAmount() {
        return this.gatewayAmount;
    }

    public ReconcileSummary gatewayAmount(Long gatewayAmount) {
        this.gatewayAmount = gatewayAmount;
        return this;
    }

    public void setGatewayAmount(Long gatewayAmount) {
        this.gatewayAmount = gatewayAmount;
    }

    public Long getSystemCount() {
        return this.systemCount;
    }

    public ReconcileSummary systemCount(Long systemCount) {
        this.systemCount = systemCount;
        return this;
    }

    public void setSystemCount(Long systemCount) {
        this.systemCount = systemCount;
    }

    public Long getGatewayCount() {
        return this.gatewayCount;
    }

    public ReconcileSummary gatewayCount(Long gatewayCount) {
        this.gatewayCount = gatewayCount;
        return this;
    }

    public void setGatewayCount(Long gatewayCount) {
        this.gatewayCount = gatewayCount;
    }

    public LocalDate getDate() {
        return this.date;
    }

    public ReconcileSummary date(LocalDate date) {
        this.date = date;
        return this;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public ReconcileSummaryStatus getStatus() {
        return this.status;
    }

    public ReconcileSummary status(ReconcileSummaryStatus status) {
        this.status = status;
        return this;
    }

    public void setStatus(ReconcileSummaryStatus status) {
        this.status = status;
    }

    public Boolean getSuccess() {
        return this.success;
    }

    public Boolean isSuccess() {
        return this.success;
    }

    public ReconcileSummary success(Boolean success) {
        this.success = success;
        return this;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

    public ReconcilePeriod getPeriod() {
        return this.period;
    }

    public ReconcileSummary period(ReconcilePeriod period) {
        this.period = period;
        return this;
    }

    public void setPeriod(ReconcilePeriod period) {
        this.period = period;
    }

    public String getGatewayName() {
        return this.gatewayName;
    }

    public ReconcileSummary gatewayName(String gatewayName) {
        this.gatewayName = gatewayName;
        return this;
    }

    public void setGatewayName(String gatewayName) {
        this.gatewayName = gatewayName;
    }

    public Instant getFromDate() {
        return fromDate;
    }

    public ReconcileSummary fromDate(Instant fromDate) {
        this.fromDate = fromDate;
        return this;
    }

    public void setFromDate(Instant fromDate) {
        this.fromDate = fromDate;
    }

    public Instant getToDate() {
        return toDate;
    }

    public ReconcileSummary toDate(Instant toDate) {
        this.toDate = toDate;
        return this;
    }

    public void setToDate(Instant toDate) {
        this.toDate = toDate;
    }

    public ReconcileSummaryType getType() {
        return this.type;
    }

    public ReconcileSummary type(ReconcileSummaryType type) {
        this.type = type;
        return this;
    }

    public void setType(ReconcileSummaryType type) {
        this.type = type;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ReconcileSummary reconcileSummary = (ReconcileSummary) o;
        if (reconcileSummary.getId() == null || getId() == null) {
            return false;
        }
        return Objects.equals(getId(), reconcileSummary.getId());
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "ReconcileSummary{" +
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
