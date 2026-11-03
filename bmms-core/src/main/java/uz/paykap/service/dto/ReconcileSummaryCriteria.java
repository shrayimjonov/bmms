package uz.paykap.service.dto;

import java.io.Serializable;
import java.util.Objects;
import uz.paykap.domain.enumeration.ReconcileSummaryStatus;
import uz.paykap.domain.enumeration.ReconcilePeriod;
import uz.paykap.domain.enumeration.ReconcileSummaryType;
import io.github.jhipster.service.filter.BooleanFilter;
import io.github.jhipster.service.filter.DoubleFilter;
import io.github.jhipster.service.filter.Filter;
import io.github.jhipster.service.filter.FloatFilter;
import io.github.jhipster.service.filter.IntegerFilter;
import io.github.jhipster.service.filter.LongFilter;
import io.github.jhipster.service.filter.StringFilter;
import io.github.jhipster.service.filter.InstantFilter;
import io.github.jhipster.service.filter.LocalDateFilter;

/**
 * Criteria class for the ReconcileSummary entity. This class is used in ReconcileSummaryResource to
 * receive all the possible filtering options from the Http GET request parameters.
 * For example the following could be a valid requests:
 * <code> /reconcile-summaries?id.greaterThan=5&amp;attr1.contains=something&amp;attr2.specified=false</code>
 * As Spring is unable to properly convert the types, unless specific {@link Filter} class are used, we need to use
 * fix type specific filters.
 */
public class ReconcileSummaryCriteria implements Serializable {
    /**
     * Class for filtering ReconcileSummaryStatus
     */
    public static class ReconcileSummaryStatusFilter extends Filter<ReconcileSummaryStatus> {
    }

    /**
     * Class for filtering ReconcilePeriod
     */
    public static class ReconcilePeriodFilter extends Filter<ReconcilePeriod> {
    }
    /**
     * Class for filtering ReconcileSummaryType
     */
    public static class ReconcileSummaryTypeFilter extends Filter<ReconcileSummaryType> {
    }

    private static final long serialVersionUID = 1L;


    private LongFilter id;

    private LongFilter gatewayId;

    private LongFilter systemAmount;

    private LongFilter gatewayAmount;

    private LongFilter systemCount;

    private LongFilter gatewayCount;

    private LocalDateFilter date;

    private ReconcileSummaryStatusFilter status;

    private BooleanFilter success;

    private ReconcilePeriodFilter period;

    private StringFilter gatewayName;

    private InstantFilter fromDate;

    private InstantFilter toDate;

    private ReconcileSummaryTypeFilter type;

    public LongFilter getId() {
        return id;
    }

    public void setId(LongFilter id) {
        this.id = id;
    }

    public LongFilter getGatewayId() {
        return gatewayId;
    }

    public void setGatewayId(LongFilter gatewayId) {
        this.gatewayId = gatewayId;
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

    public LongFilter getSystemCount() {
        return systemCount;
    }

    public void setSystemCount(LongFilter systemCount) {
        this.systemCount = systemCount;
    }

    public LongFilter getGatewayCount() {
        return gatewayCount;
    }

    public void setGatewayCount(LongFilter gatewayCount) {
        this.gatewayCount = gatewayCount;
    }

    public LocalDateFilter getDate() {
        return date;
    }

    public void setDate(LocalDateFilter date) {
        this.date = date;
    }

    public ReconcileSummaryStatusFilter getStatus() {
        return status;
    }

    public void setStatus(ReconcileSummaryStatusFilter status) {
        this.status = status;
    }

    public BooleanFilter getSuccess() {
        return success;
    }

    public void setSuccess(BooleanFilter success) {
        this.success = success;
    }

    public ReconcilePeriodFilter getPeriod() {
        return period;
    }

    public void setPeriod(ReconcilePeriodFilter period) {
        this.period = period;
    }

    public StringFilter getGatewayName() {
        return gatewayName;
    }

    public void setGatewayName(StringFilter gatewayName) {
        this.gatewayName = gatewayName;
    }

    public InstantFilter getFromDate() {
        return fromDate;
    }

    public void setFromDate(InstantFilter fromDate) {
        this.fromDate = fromDate;
    }

    public InstantFilter getToDate() {
        return toDate;
    }

    public void setToDate(InstantFilter toDate) {
        this.toDate = toDate;
    }

    public ReconcileSummaryTypeFilter getType() {
        return type;
    }

    public void setType(ReconcileSummaryTypeFilter type) {
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
        final ReconcileSummaryCriteria that = (ReconcileSummaryCriteria) o;
        return
            Objects.equals(id, that.id) &&
            Objects.equals(gatewayId, that.gatewayId) &&
            Objects.equals(systemAmount, that.systemAmount) &&
            Objects.equals(gatewayAmount, that.gatewayAmount) &&
            Objects.equals(systemCount, that.systemCount) &&
            Objects.equals(gatewayCount, that.gatewayCount) &&
            Objects.equals(date, that.date) &&
            Objects.equals(status, that.status) &&
            Objects.equals(success, that.success) &&
            Objects.equals(period, that.period) &&
            Objects.equals(gatewayName, that.gatewayName) &&
            Objects.equals(fromDate, that.fromDate) &&
            Objects.equals(toDate, that.toDate) &&
            Objects.equals(type, that.type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
        id,
        gatewayId,
        systemAmount,
        gatewayAmount,
        systemCount,
        gatewayCount,
        date,
        status,
        success,
        period,
        gatewayName,
        fromDate,
        toDate,
        type
        );
    }

    @Override
    public String toString() {
        return "ReconcileSummaryCriteria{" +
                (id != null ? "id=" + id + ", " : "") +
                (gatewayId != null ? "gatewayId=" + gatewayId + ", " : "") +
                (systemAmount != null ? "systemAmount=" + systemAmount + ", " : "") +
                (gatewayAmount != null ? "gatewayAmount=" + gatewayAmount + ", " : "") +
                (systemCount != null ? "systemCount=" + systemCount + ", " : "") +
                (gatewayCount != null ? "gatewayCount=" + gatewayCount + ", " : "") +
                (date != null ? "date=" + date + ", " : "") +
                (status != null ? "status=" + status + ", " : "") +
                (success != null ? "success=" + success + ", " : "") +
                (period != null ? "period=" + period + ", " : "") +
                (gatewayName != null ? "gatewayName=" + gatewayName + ", " : "") +
                (fromDate != null ? "fromDate=" + fromDate + ", " : "") +
                (toDate != null ? "toDate=" + toDate + ", " : "") +
                (type != null ? "type=" + type + ", " : "") +
            "}";
    }

}
