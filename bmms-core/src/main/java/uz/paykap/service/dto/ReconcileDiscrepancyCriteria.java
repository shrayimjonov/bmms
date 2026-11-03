package uz.paykap.service.dto;

import io.github.jhipster.service.filter.*;

import java.io.Serializable;
import java.util.Objects;

/**
 * Criteria class for the ReconcileDiscrepancy entity. This class is used in ReconcileDiscrepancyResource to
 * receive all the possible filtering options from the Http GET request parameters.
 * For example the following could be a valid requests:
 * <code> /reconcile-discrepancies?id.greaterThan=5&amp;attr1.contains=something&amp;attr2.specified=false</code>
 * As Spring is unable to properly convert the types, unless specific {@link Filter} class are used, we need to use
 * fix type specific filters.
 */
public class ReconcileDiscrepancyCriteria implements Serializable {

    private static final long serialVersionUID = 1L;

    private LongFilter id;

    private StringFilter gatewayName;

    private LongFilter systemTransactionId;

    private StringFilter gatewayTransactionId;

    private LongFilter systemAmount;

    private LongFilter gatewayAmount;

    private BooleanFilter systemStatus;

    private BooleanFilter gatewayStatus;

    private InstantFilter systemDate;

    private InstantFilter gatewayDate;

    private BooleanFilter closed;

    private StringFilter comment;

    private LongFilter reconcileSummaryId;

    public LongFilter getId() {
        return id;
    }

    public void setId(LongFilter id) {
        this.id = id;
    }

    public StringFilter getGatewayName() {
        return gatewayName;
    }

    public void setGatewayName(StringFilter gatewayName) {
        this.gatewayName = gatewayName;
    }

    public LongFilter getSystemTransactionId() {
        return systemTransactionId;
    }

    public void setSystemTransactionId(LongFilter systemTransactionId) {
        this.systemTransactionId = systemTransactionId;
    }

    public StringFilter getGatewayTransactionId() {
        return gatewayTransactionId;
    }

    public void setGatewayTransactionId(StringFilter gatewayTransactionId) {
        this.gatewayTransactionId = gatewayTransactionId;
    }

    public LongFilter getSystemAmount() {
        return systemAmount;
    }

    public void setSystemAmount(LongFilter systemAmount) {
        this.systemAmount = systemAmount;
    }

    public LongFilter getGatewayAmount() {
        return gatewayAmount;
    }

    public void setGatewayAmount(LongFilter gatewayAmount) {
        this.gatewayAmount = gatewayAmount;
    }

    public BooleanFilter getSystemStatus() {
        return systemStatus;
    }

    public void setSystemStatus(BooleanFilter systemStatus) {
        this.systemStatus = systemStatus;
    }

    public BooleanFilter getGatewayStatus() {
        return gatewayStatus;
    }

    public void setGatewayStatus(BooleanFilter gatewayStatus) {
        this.gatewayStatus = gatewayStatus;
    }

    public InstantFilter getSystemDate() {
        return systemDate;
    }

    public void setSystemDate(InstantFilter systemDate) {
        this.systemDate = systemDate;
    }

    public InstantFilter getGatewayDate() {
        return gatewayDate;
    }

    public void setGatewayDate(InstantFilter gatewayDate) {
        this.gatewayDate = gatewayDate;
    }

    public BooleanFilter getClosed() {
        return closed;
    }

    public void setClosed(BooleanFilter closed) {
        this.closed = closed;
    }

    public StringFilter getComment() {
        return comment;
    }

    public void setComment(StringFilter comment) {
        this.comment = comment;
    }

    public LongFilter getReconcileSummaryId() {
        return reconcileSummaryId;
    }

    public void setReconcileSummaryId(LongFilter reconcileSummaryId) {
        this.reconcileSummaryId = reconcileSummaryId;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final ReconcileDiscrepancyCriteria that = (ReconcileDiscrepancyCriteria) o;
        return
            Objects.equals(id, that.id) &&
                Objects.equals(gatewayName, that.gatewayName) &&
                Objects.equals(systemTransactionId, that.systemTransactionId) &&
                Objects.equals(gatewayTransactionId, that.gatewayTransactionId) &&
                Objects.equals(systemAmount, that.systemAmount) &&
                Objects.equals(gatewayAmount, that.gatewayAmount) &&
                Objects.equals(systemStatus, that.systemStatus) &&
                Objects.equals(gatewayStatus, that.gatewayStatus) &&
                Objects.equals(systemDate, that.systemDate) &&
                Objects.equals(gatewayDate, that.gatewayDate) &&
                Objects.equals(closed, that.closed) &&
                Objects.equals(comment, that.comment) &&
                Objects.equals(reconcileSummaryId, that.reconcileSummaryId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
            id,
            gatewayName,
            systemTransactionId,
            gatewayTransactionId,
            systemAmount,
            gatewayAmount,
            systemStatus,
            gatewayStatus,
            systemDate,
            gatewayDate,
            closed,
            comment,
            reconcileSummaryId
        );
    }

    @Override
    public String toString() {
        return "ReconcileDiscrepancyCriteria{" +
                (id != null ? "id=" + id + ", " : "") +
                (gatewayName != null ? "gatewayName=" + gatewayName + ", " : "") +
                (systemTransactionId != null ? "systemTransactionId=" + systemTransactionId + ", " : "") +
                (gatewayTransactionId != null ? "gatewayTransactionId=" + gatewayTransactionId + ", " : "") +
                (systemAmount != null ? "systemAmount=" + systemAmount + ", " : "") +
                (gatewayAmount != null ? "gatewayAmount=" + gatewayAmount + ", " : "") +
                (systemStatus != null ? "systemStatus=" + systemStatus + ", " : "") +
                (gatewayStatus != null ? "gatewayStatus=" + gatewayStatus + ", " : "") +
                (systemDate != null ? "systemDate=" + systemDate + ", " : "") +
                (gatewayDate != null ? "gatewayDate=" + gatewayDate + ", " : "") +
                (closed != null ? "closed=" + closed + ", " : "") +
                (comment != null ? "comment=" + comment + ", " : "") +
                (reconcileSummaryId != null ? "reconcileSummaryId=" + reconcileSummaryId + ", " : "") +
            "}";
    }

}
