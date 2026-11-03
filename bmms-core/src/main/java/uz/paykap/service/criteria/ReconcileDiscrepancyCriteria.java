package uz.paykap.service.criteria;

import java.io.Serializable;
import java.util.Objects;
import tech.jhipster.service.Criteria;
import tech.jhipster.service.filter.BooleanFilter;
import tech.jhipster.service.filter.DoubleFilter;
import tech.jhipster.service.filter.Filter;
import tech.jhipster.service.filter.FloatFilter;
import tech.jhipster.service.filter.InstantFilter;
import tech.jhipster.service.filter.IntegerFilter;
import tech.jhipster.service.filter.LongFilter;
import tech.jhipster.service.filter.StringFilter;

/**
 * Criteria class for the {@link uz.paykap.domain.ReconcileDiscrepancy} entity. This class is used
 * in {@link uz.paykap.web.rest.ReconcileDiscrepancyResource} to receive all the possible filtering options from
 * the Http GET request parameters.
 * For example the following could be a valid request:
 * {@code /reconcile-discrepancies?id.greaterThan=5&attr1.contains=something&attr2.specified=false}
 * As Spring is unable to properly convert the types, unless specific {@link Filter} class are used, we need to use
 * fix type specific filters.
 */
public class ReconcileDiscrepancyCriteria implements Serializable, Criteria {

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

    public ReconcileDiscrepancyCriteria() {}

    public ReconcileDiscrepancyCriteria(ReconcileDiscrepancyCriteria other) {
        this.id = other.id == null ? null : other.id.copy();
        this.gatewayName = other.gatewayName == null ? null : other.gatewayName.copy();
        this.systemTransactionId = other.systemTransactionId == null ? null : other.systemTransactionId.copy();
        this.gatewayTransactionId = other.gatewayTransactionId == null ? null : other.gatewayTransactionId.copy();
        this.systemAmount = other.systemAmount == null ? null : other.systemAmount.copy();
        this.gatewayAmount = other.gatewayAmount == null ? null : other.gatewayAmount.copy();
        this.systemStatus = other.systemStatus == null ? null : other.systemStatus.copy();
        this.gatewayStatus = other.gatewayStatus == null ? null : other.gatewayStatus.copy();
        this.systemDate = other.systemDate == null ? null : other.systemDate.copy();
        this.gatewayDate = other.gatewayDate == null ? null : other.gatewayDate.copy();
        this.closed = other.closed == null ? null : other.closed.copy();
        this.comment = other.comment == null ? null : other.comment.copy();
        this.reconcileSummaryId = other.reconcileSummaryId == null ? null : other.reconcileSummaryId.copy();
    }

    @Override
    public ReconcileDiscrepancyCriteria copy() {
        return new ReconcileDiscrepancyCriteria(this);
    }

    public LongFilter getId() {
        return id;
    }

    public LongFilter id() {
        if (id == null) {
            id = new LongFilter();
        }
        return id;
    }

    public void setId(LongFilter id) {
        this.id = id;
    }

    public StringFilter getGatewayName() {
        return gatewayName;
    }

    public StringFilter gatewayName() {
        if (gatewayName == null) {
            gatewayName = new StringFilter();
        }
        return gatewayName;
    }

    public void setGatewayName(StringFilter gatewayName) {
        this.gatewayName = gatewayName;
    }

    public LongFilter getSystemTransactionId() {
        return systemTransactionId;
    }

    public LongFilter systemTransactionId() {
        if (systemTransactionId == null) {
            systemTransactionId = new LongFilter();
        }
        return systemTransactionId;
    }

    public void setSystemTransactionId(LongFilter systemTransactionId) {
        this.systemTransactionId = systemTransactionId;
    }

    public StringFilter getGatewayTransactionId() {
        return gatewayTransactionId;
    }

    public StringFilter gatewayTransactionId() {
        if (gatewayTransactionId == null) {
            gatewayTransactionId = new StringFilter();
        }
        return gatewayTransactionId;
    }

    public void setGatewayTransactionId(StringFilter gatewayTransactionId) {
        this.gatewayTransactionId = gatewayTransactionId;
    }

    public LongFilter getSystemAmount() {
        return systemAmount;
    }

    public LongFilter systemAmount() {
        if (systemAmount == null) {
            systemAmount = new LongFilter();
        }
        return systemAmount;
    }

    public void setSystemAmount(LongFilter systemAmount) {
        this.systemAmount = systemAmount;
    }

    public LongFilter getGatewayAmount() {
        return gatewayAmount;
    }

    public LongFilter gatewayAmount() {
        if (gatewayAmount == null) {
            gatewayAmount = new LongFilter();
        }
        return gatewayAmount;
    }

    public void setGatewayAmount(LongFilter gatewayAmount) {
        this.gatewayAmount = gatewayAmount;
    }

    public BooleanFilter getSystemStatus() {
        return systemStatus;
    }

    public BooleanFilter systemStatus() {
        if (systemStatus == null) {
            systemStatus = new BooleanFilter();
        }
        return systemStatus;
    }

    public void setSystemStatus(BooleanFilter systemStatus) {
        this.systemStatus = systemStatus;
    }

    public BooleanFilter getGatewayStatus() {
        return gatewayStatus;
    }

    public BooleanFilter gatewayStatus() {
        if (gatewayStatus == null) {
            gatewayStatus = new BooleanFilter();
        }
        return gatewayStatus;
    }

    public void setGatewayStatus(BooleanFilter gatewayStatus) {
        this.gatewayStatus = gatewayStatus;
    }

    public InstantFilter getSystemDate() {
        return systemDate;
    }

    public InstantFilter systemDate() {
        if (systemDate == null) {
            systemDate = new InstantFilter();
        }
        return systemDate;
    }

    public void setSystemDate(InstantFilter systemDate) {
        this.systemDate = systemDate;
    }

    public InstantFilter getGatewayDate() {
        return gatewayDate;
    }

    public InstantFilter gatewayDate() {
        if (gatewayDate == null) {
            gatewayDate = new InstantFilter();
        }
        return gatewayDate;
    }

    public void setGatewayDate(InstantFilter gatewayDate) {
        this.gatewayDate = gatewayDate;
    }

    public BooleanFilter getClosed() {
        return closed;
    }

    public BooleanFilter closed() {
        if (closed == null) {
            closed = new BooleanFilter();
        }
        return closed;
    }

    public void setClosed(BooleanFilter closed) {
        this.closed = closed;
    }

    public StringFilter getComment() {
        return comment;
    }

    public StringFilter comment() {
        if (comment == null) {
            comment = new StringFilter();
        }
        return comment;
    }

    public void setComment(StringFilter comment) {
        this.comment = comment;
    }

    public LongFilter getReconcileSummaryId() {
        return reconcileSummaryId;
    }

    public LongFilter reconcileSummaryId() {
        if (reconcileSummaryId == null) {
            reconcileSummaryId = new LongFilter();
        }
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
        return (
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
            Objects.equals(reconcileSummaryId, that.reconcileSummaryId)
        );
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

    // prettier-ignore
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
