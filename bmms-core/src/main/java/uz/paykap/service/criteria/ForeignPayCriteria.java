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
import uz.paykap.domain.enumeration.ForeignPayStatus;

/**
 * Criteria class for the {@link uz.paykap.domain.ForeignPay} entity. This class is used
 * in {@link uz.paykap.web.rest.ForeignPayResource} to receive all the possible filtering options from
 * the Http GET request parameters.
 * For example the following could be a valid request:
 * {@code /foreign-pays?id.greaterThan=5&attr1.contains=something&attr2.specified=false}
 * As Spring is unable to properly convert the types, unless specific {@link Filter} class are used, we need to use
 * fix type specific filters.
 */
public class ForeignPayCriteria implements Serializable, Criteria {

    /**
     * Class for filtering ForeignPayStatus
     */
    public static class ForeignPayStatusFilter extends Filter<ForeignPayStatus> {

        public ForeignPayStatusFilter() {}

        public ForeignPayStatusFilter(ForeignPayStatusFilter filter) {
            super(filter);
        }

        @Override
        public ForeignPayStatusFilter copy() {
            return new ForeignPayStatusFilter(this);
        }
    }

    private static final long serialVersionUID = 1L;

    private LongFilter id;

    private InstantFilter verifiedDate;

    private ForeignPayStatusFilter status;

    private StringFilter cardToken;

    private LongFilter billId;

    private StringFilter otp;

    private StringFilter phone;

    private IntegerFilter entryCount;

    public ForeignPayCriteria() {}

    public ForeignPayCriteria(ForeignPayCriteria other) {
        this.id = other.id == null ? null : other.id.copy();
        this.verifiedDate = other.verifiedDate == null ? null : other.verifiedDate.copy();
        this.status = other.status == null ? null : other.status.copy();
        this.cardToken = other.cardToken == null ? null : other.cardToken.copy();
        this.billId = other.billId == null ? null : other.billId.copy();
        this.otp = other.otp == null ? null : other.otp.copy();
        this.phone = other.phone == null ? null : other.phone.copy();
        this.entryCount = other.entryCount == null ? null : other.entryCount.copy();
    }

    @Override
    public ForeignPayCriteria copy() {
        return new ForeignPayCriteria(this);
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

    public InstantFilter getVerifiedDate() {
        return verifiedDate;
    }

    public InstantFilter verifiedDate() {
        if (verifiedDate == null) {
            verifiedDate = new InstantFilter();
        }
        return verifiedDate;
    }

    public void setVerifiedDate(InstantFilter verifiedDate) {
        this.verifiedDate = verifiedDate;
    }

    public ForeignPayStatusFilter getStatus() {
        return status;
    }

    public ForeignPayStatusFilter status() {
        if (status == null) {
            status = new ForeignPayStatusFilter();
        }
        return status;
    }

    public void setStatus(ForeignPayStatusFilter status) {
        this.status = status;
    }

    public StringFilter getCardToken() {
        return cardToken;
    }

    public StringFilter cardToken() {
        if (cardToken == null) {
            cardToken = new StringFilter();
        }
        return cardToken;
    }

    public void setCardToken(StringFilter cardToken) {
        this.cardToken = cardToken;
    }

    public LongFilter getBillId() {
        return billId;
    }

    public LongFilter billId() {
        if (billId == null) {
            billId = new LongFilter();
        }
        return billId;
    }

    public void setBillId(LongFilter billId) {
        this.billId = billId;
    }

    public StringFilter getOtp() {
        return otp;
    }

    public StringFilter otp() {
        if (otp == null) {
            otp = new StringFilter();
        }
        return otp;
    }

    public void setOtp(StringFilter otp) {
        this.otp = otp;
    }

    public StringFilter getPhone() {
        return phone;
    }

    public StringFilter phone() {
        if (phone == null) {
            phone = new StringFilter();
        }
        return phone;
    }

    public void setPhone(StringFilter phone) {
        this.phone = phone;
    }

    public IntegerFilter getEntryCount() {
        return entryCount;
    }

    public IntegerFilter entryCount() {
        if (entryCount == null) {
            entryCount = new IntegerFilter();
        }
        return entryCount;
    }

    public void setEntryCount(IntegerFilter entryCount) {
        this.entryCount = entryCount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final ForeignPayCriteria that = (ForeignPayCriteria) o;
        return (
            Objects.equals(id, that.id) &&
            Objects.equals(verifiedDate, that.verifiedDate) &&
            Objects.equals(status, that.status) &&
            Objects.equals(cardToken, that.cardToken) &&
            Objects.equals(billId, that.billId) &&
            Objects.equals(otp, that.otp) &&
            Objects.equals(phone, that.phone) &&
            Objects.equals(entryCount, that.entryCount)
        );
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, verifiedDate, status, cardToken, billId, otp, phone, entryCount);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "ForeignPayCriteria{" +
            (id != null ? "id=" + id + ", " : "") +
            (verifiedDate != null ? "verifiedDate=" + verifiedDate + ", " : "") +
            (status != null ? "status=" + status + ", " : "") +
            (cardToken != null ? "cardToken=" + cardToken + ", " : "") +
            (billId != null ? "billId=" + billId + ", " : "") +
            (otp != null ? "otp=" + otp + ", " : "") +
            (phone != null ? "phone=" + phone + ", " : "") +
            (entryCount != null ? "entryCount=" + entryCount + ", " : "") +
            "}";
    }
}
