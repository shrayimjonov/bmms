package uz.paykap.domain;

import java.io.Serializable;
import java.time.Instant;
import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * Данная форма предназначена для хранения данных о транзакции при сверке
 */
@Entity
@Table(name = "reconcile_discrepancy")
public class ReconcileDiscrepancy extends AbstractAuditingEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    private Long id;

    /**
     * Уникальное название шлюза
     */
    @NotNull
    @Size(max = 64)
    @Column(name = "gateway_name", length = 64, nullable = false)
    private String gatewayName;

    /**
     * Номер платежа в системе
     */
    @NotNull
    @Column(name = "system_transaction_id", nullable = false)
    private Long systemTransactionId;

    /**
     * Номер платежа в шлюзе
     */
    @NotNull
    @Column(name = "gateway_transaction_id", nullable = false)
    private String gatewayTransactionId;

    /**
     * Сумма платежа в системе
     */
    @Column(name = "system_amount")
    private Long systemAmount;

    /**
     * Сумма платежа в шлюзе
     */
    @Column(name = "gateway_amount")
    private Long gatewayAmount;

    /**
     * Статус транзакции в системе
     */
    @Column(name = "system_status")
    private Boolean systemStatus;

    /**
     * Статус транзакции в шлюзе
     */
    @Column(name = "gateway_status")
    private Boolean gatewayStatus;

    /**
     * Дата транзакции в системе
     */
    @Column(name = "system_date")
    private Instant systemDate;

    /**
     * Дата транзакции в шлюзе
     */
    @Column(name = "gateway_date")
    private Instant gatewayDate;

    /**
     * Финальный статус проблемы
     */
    @Column(name = "closed")
    private Boolean closed;

    /**
     * Дополнительная информация
     */
    @Column(name = "comment")
    private String comment;

    @ManyToOne
    private ReconcileSummary reconcileSummary;

    // jhipster-needle-entity-add-field - JHipster will add fields here
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ReconcileDiscrepancy id(Long id) {
        this.id = id;
        return this;
    }

    public String getGatewayName() {
        return this.gatewayName;
    }

    public ReconcileDiscrepancy gatewayName(String gatewayName) {
        this.gatewayName = gatewayName;
        return this;
    }

    public void setGatewayName(String gatewayName) {
        this.gatewayName = gatewayName;
    }

    public Long getSystemTransactionId() {
        return this.systemTransactionId;
    }

    public ReconcileDiscrepancy systemTransactionId(Long systemTransactionId) {
        this.systemTransactionId = systemTransactionId;
        return this;
    }

    public void setSystemTransactionId(Long systemTransactionId) {
        this.systemTransactionId = systemTransactionId;
    }

    public String getGatewayTransactionId() {
        return this.gatewayTransactionId;
    }

    public ReconcileDiscrepancy gatewayTransactionId(String gatewayTransactionId) {
        this.gatewayTransactionId = gatewayTransactionId;
        return this;
    }

    public void setGatewayTransactionId(String gatewayTransactionId) {
        this.gatewayTransactionId = gatewayTransactionId;
    }

    public Long getSystemAmount() {
        return this.systemAmount;
    }

    public ReconcileDiscrepancy systemAmount(Long systemAmount) {
        this.systemAmount = systemAmount;
        return this;
    }

    public void setSystemAmount(Long systemAmount) {
        this.systemAmount = systemAmount;
    }

    public Long getGatewayAmount() {
        return this.gatewayAmount;
    }

    public ReconcileDiscrepancy gatewayAmount(Long gatewayAmount) {
        this.gatewayAmount = gatewayAmount;
        return this;
    }

    public void setGatewayAmount(Long gatewayAmount) {
        this.gatewayAmount = gatewayAmount;
    }

    public Boolean getSystemStatus() {
        return this.systemStatus;
    }

    public Boolean isSystemStatus() {
        return systemStatus;
    }

    public ReconcileDiscrepancy systemStatus(Boolean systemStatus) {
        this.systemStatus = systemStatus;
        return this;
    }

    public void setSystemStatus(Boolean systemStatus) {
        this.systemStatus = systemStatus;
    }

    public Boolean getGatewayStatus() {
        return this.gatewayStatus;
    }

    public Boolean isGatewayStatus() {
        return this.gatewayStatus;
    }

    public ReconcileDiscrepancy gatewayStatus(Boolean gatewayStatus) {
        this.gatewayStatus = gatewayStatus;
        return this;
    }

    public void setGatewayStatus(Boolean gatewayStatus) {
        this.gatewayStatus = gatewayStatus;
    }

    public Instant getSystemDate() {
        return this.systemDate;
    }

    public ReconcileDiscrepancy systemDate(Instant systemDate) {
        this.systemDate = systemDate;
        return this;
    }

    public void setSystemDate(Instant systemDate) {
        this.systemDate = systemDate;
    }

    public Instant getGatewayDate() {
        return this.gatewayDate;
    }

    public ReconcileDiscrepancy gatewayDate(Instant gatewayDate) {
        this.gatewayDate = gatewayDate;
        return this;
    }

    public void setGatewayDate(Instant gatewayDate) {
        this.gatewayDate = gatewayDate;
    }

    public Boolean getClosed() {
        return this.closed;
    }

    public Boolean isClosed() {
        return this.closed;
    }

    public ReconcileDiscrepancy closed(Boolean closed) {
        this.closed = closed;
        return this;
    }

    public void setClosed(Boolean closed) {
        this.closed = closed;
    }

    public String getComment() {
        return this.comment;
    }

    public ReconcileDiscrepancy comment(String comment) {
        this.comment = comment;
        return this;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public ReconcileSummary getReconcileSummary() {
        return this.reconcileSummary;
    }

    public ReconcileDiscrepancy reconcileSummary(ReconcileSummary reconcileSummary) {
        this.setReconcileSummary(reconcileSummary);
        return this;
    }

    public void setReconcileSummary(ReconcileSummary reconcileSummary) {
        this.reconcileSummary = reconcileSummary;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ReconcileDiscrepancy)) {
            return false;
        }
        return id != null && id.equals(((ReconcileDiscrepancy) o).id);
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "ReconcileDiscrepancy{" +
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
                "}";
    }
}
