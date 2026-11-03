package uz.paykap.domain;

import java.io.Serializable;
import java.time.Instant;
import javax.persistence.*;
import javax.validation.constraints.*;
import uz.paykap.domain.enumeration.ForeignPayStatus;

/**
 * A ForeignPay.
 */
@Entity
@Table(name = "foreign_pay")
public class ForeignPay extends AbstractAuditingEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    private Long id;

    @NotNull
    @Column(name = "verified_date", nullable = false)
    private Instant verifiedDate;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ForeignPayStatus status;

    @NotNull
    @Column(name = "card_token", nullable = false)
    private String cardToken;

    @NotNull
    @Column(name = "bill_id", nullable = false)
    private Long billId;

    @NotNull
    @Size(max = 6)
    @Column(name = "otp", length = 6, nullable = false)
    private String otp;

    @NotNull
    @Size(max = 13)
    @Column(name = "phone", length = 13, nullable = false)
    private String phone;

    @NotNull
    @Column(name = "entry_count", nullable = false)
    private Integer entryCount;

    // jhipster-needle-entity-add-field - JHipster will add fields here
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ForeignPay id(Long id) {
        this.id = id;
        return this;
    }

    public Instant getVerifiedDate() {
        return this.verifiedDate;
    }

    public ForeignPay verifiedDate(Instant verifiedDate) {
        this.verifiedDate = verifiedDate;
        return this;
    }

    public void setVerifiedDate(Instant verifiedDate) {
        this.verifiedDate = verifiedDate;
    }

    public ForeignPayStatus getStatus() {
        return this.status;
    }

    public ForeignPay status(ForeignPayStatus status) {
        this.status = status;
        return this;
    }

    public void setStatus(ForeignPayStatus status) {
        this.status = status;
    }

    public String getCardToken() {
        return this.cardToken;
    }

    public ForeignPay cardToken(String cardToken) {
        this.cardToken = cardToken;
        return this;
    }

    public void setCardToken(String cardToken) {
        this.cardToken = cardToken;
    }

    public Long getBillId() {
        return this.billId;
    }

    public ForeignPay billId(Long billId) {
        this.billId = billId;
        return this;
    }

    public void setBillId(Long billId) {
        this.billId = billId;
    }

    public String getOtp() {
        return this.otp;
    }

    public ForeignPay otp(String otp) {
        this.otp = otp;
        return this;
    }

    public void setOtp(String otp) {
        this.otp = otp;
    }

    public String getPhone() {
        return this.phone;
    }

    public ForeignPay phone(String phone) {
        this.phone = phone;
        return this;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public Integer getEntryCount() {
        return this.entryCount;
    }

    public ForeignPay entryCount(Integer entryCount) {
        this.entryCount = entryCount;
        return this;
    }

    public void setEntryCount(Integer entryCount) {
        this.entryCount = entryCount;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ForeignPay)) {
            return false;
        }
        return id != null && id.equals(((ForeignPay) o).id);
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "ForeignPay{" +
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
