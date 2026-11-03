package uz.paykap.service.dto;

import io.github.jhipster.service.filter.*;
import uz.paykap.domain.enumeration.ForeignPayStatus;

import java.io.Serializable;
import java.util.Objects;

/**
 * Criteria class for the ForeignPay entity. This class is used in ForeignPayResource to
 * receive all the possible filtering options from the Http GET request parameters.
 * For example the following could be a valid requests:
 * <code> /foreign-pays?id.greaterThan=5&amp;attr1.contains=something&amp;attr2.specified=false</code>
 * As Spring is unable to properly convert the types, unless specific {@link Filter} class are used, we need to use
 * fix type specific filters.
 */
public class ForeignPayCriteria implements Serializable {
    /**
     * Class for filtering ForeignPayStatus
     */
    public static class ForeignPayStatusFilter extends Filter<ForeignPayStatus> {
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

    public LongFilter getId() {
        return id;
    }

    public void setId(LongFilter id) {
        this.id = id;
    }

    public InstantFilter getVerifiedDate() {
        return verifiedDate;
    }

    public void setVerifiedDate(InstantFilter verifiedDate) {
        this.verifiedDate = verifiedDate;
    }

    public ForeignPayStatusFilter getStatus() {
        return status;
    }

    public void setStatus(ForeignPayStatusFilter status) {
        this.status = status;
    }

    public StringFilter getCardToken() {
        return cardToken;
    }

    public void setCardToken(StringFilter cardToken) {
        this.cardToken = cardToken;
    }

    public LongFilter getBillId() {
        return billId;
    }

    public void setBillId(LongFilter billId) {
        this.billId = billId;
    }

    public StringFilter getOtp() {
        return otp;
    }

    public void setOtp(StringFilter otp) {
        this.otp = otp;
    }

    public StringFilter getPhone() {
        return phone;
    }

    public void setPhone(StringFilter phone) {
        this.phone = phone;
    }

    public IntegerFilter getEntryCount() {
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
        return
            Objects.equals(id, that.id) &&
                Objects.equals(verifiedDate, that.verifiedDate) &&
                Objects.equals(status, that.status) &&
                Objects.equals(cardToken, that.cardToken) &&
                Objects.equals(billId, that.billId) &&
                Objects.equals(otp, that.otp) &&
                Objects.equals(phone, that.phone) &&
                Objects.equals(entryCount, that.entryCount);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
            id,
            verifiedDate,
            status,
            cardToken,
            billId,
            otp,
            phone,
            entryCount
        );
    }

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
