package uz.paykap.domain;


import javax.persistence.*;
import javax.validation.constraints.*;

import java.io.Serializable;
import java.util.Objects;

import uz.paykap.domain.enumeration.BinPcType;

/**
 * A PcBin.
 */
@Entity
@Table(name = "pc_bin")
public class PcBin implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    private Long id;

    @NotNull
    @Size(min = 1, max = 32)
    @Column(name = "bin", length = 32, nullable = false)
    private String bin;

    @Size(max = 128)
    @Column(name = "bank_name", length = 128)
    private String bankName;

    @Enumerated(EnumType.STRING)
    @Column(name = "pc_type")
    private BinPcType pcType;

    @NotNull
    @Column(name = "use_credit", nullable = false)
    private Boolean useCredit;

    @NotNull
    @Column(name = "use_debit", nullable = false)
    private Boolean useDebit;

    @NotNull
    @Column(name = "use_reverse", nullable = false)
    private Boolean useReverse;

    @NotNull
    @Column(name = "use_reconcile", nullable = false)
    private Boolean useReconcile;

    @Size(max = 256)
    @Column(name = "jhi_comment", length = 256)
    private String comment;

    // jhipster-needle-entity-add-field - JHipster will add fields here, do not remove
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getBin() {
        return bin;
    }

    public PcBin bin(String bin) {
        this.bin = bin;
        return this;
    }

    public void setBin(String bin) {
        this.bin = bin;
    }

    public String getBankName() {
        return bankName;
    }

    public PcBin bankName(String bankName) {
        this.bankName = bankName;
        return this;
    }

    public void setBankName(String bankName) {
        this.bankName = bankName;
    }

    public BinPcType getPcType() {
        return pcType;
    }

    public PcBin pcType(BinPcType pcType) {
        this.pcType = pcType;
        return this;
    }

    public void setPcType(BinPcType pcType) {
        this.pcType = pcType;
    }

    public Boolean isUseCredit() {
        return useCredit;
    }

    public PcBin useCredit(Boolean useCredit) {
        this.useCredit = useCredit;
        return this;
    }

    public void setUseCredit(Boolean useCredit) {
        this.useCredit = useCredit;
    }

    public Boolean isUseDebit() {
        return useDebit;
    }

    public PcBin useDebit(Boolean useDebit) {
        this.useDebit = useDebit;
        return this;
    }

    public void setUseDebit(Boolean useDebit) {
        this.useDebit = useDebit;
    }

    public Boolean isUseReverse() {
        return useReverse;
    }

    public PcBin useReverse(Boolean useReverse) {
        this.useReverse = useReverse;
        return this;
    }

    public void setUseReverse(Boolean useReverse) {
        this.useReverse = useReverse;
    }

    public Boolean isUseReconcile() {
        return useReconcile;
    }

    public PcBin useReconcile(Boolean useReconcile) {
        this.useReconcile = useReconcile;
        return this;
    }

    public void setUseReconcile(Boolean useReconcile) {
        this.useReconcile = useReconcile;
    }

    public String getComment() {
        return comment;
    }

    public PcBin comment(String comment) {
        this.comment = comment;
        return this;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here, do not remove

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        PcBin pcBin = (PcBin) o;
        if (pcBin.getId() == null || getId() == null) {
            return false;
        }
        return Objects.equals(getId(), pcBin.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    @Override
    public String toString() {
        return "PcBin{" +
            "id=" + getId() +
            ", bin='" + getBin() + "'" +
            ", bankName='" + getBankName() + "'" +
            ", pcType='" + getPcType() + "'" +
            ", useCredit='" + isUseCredit() + "'" +
            ", useDebit='" + isUseDebit() + "'" +
            ", useReverse='" + isUseReverse() + "'" +
            ", useReconcile='" + isUseReconcile() + "'" +
            ", comment='" + getComment() + "'" +
            "}";
    }
}
