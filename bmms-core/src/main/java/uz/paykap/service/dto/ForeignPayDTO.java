package uz.paykap.service.dto;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;
import javax.validation.constraints.*;
import uz.paykap.domain.enumeration.ForeignPayStatus;

/**
 * A DTO for the {@link uz.paykap.domain.ForeignPay} entity.
 */
public class ForeignPayDTO implements Serializable {

    private Long id;

    @NotNull
    private Instant verifiedDate;

    @NotNull
    private ForeignPayStatus status;

    @NotNull
    private String cardToken;

    @NotNull
    private Long billId;

    @NotNull
    @Size(max = 6)
    private String otp;

    @NotNull
    @Size(max = 13)
    private String phone;

    @NotNull
    private Integer entryCount;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Instant getVerifiedDate() {
        return verifiedDate;
    }

    public void setVerifiedDate(Instant verifiedDate) {
        this.verifiedDate = verifiedDate;
    }

    public ForeignPayStatus getStatus() {
        return status;
    }

    public void setStatus(ForeignPayStatus status) {
        this.status = status;
    }

    public String getCardToken() {
        return cardToken;
    }

    public void setCardToken(String cardToken) {
        this.cardToken = cardToken;
    }

    public Long getBillId() {
        return billId;
    }

    public void setBillId(Long billId) {
        this.billId = billId;
    }

    public String getOtp() {
        return otp;
    }

    public void setOtp(String otp) {
        this.otp = otp;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public Integer getEntryCount() {
        return entryCount;
    }

    public void setEntryCount(Integer entryCount) {
        this.entryCount = entryCount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ForeignPayDTO)) {
            return false;
        }

        ForeignPayDTO foreignPayDTO = (ForeignPayDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, foreignPayDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "ForeignPayDTO{" +
            "id=" + getId() +
            ", verifiedDate='" + getVerifiedDate() + "'" +
            ", status='" + getStatus() + "'" +
            ", cardToken='" + getCardToken() + "'" +
            ", billId=" + getBillId() +
            ", otp='" + getOtp() + "'" +
            ", phone='" + getPhone() + "'" +
            ", entryCount=" + getEntryCount() +
            "}";
    }
}
