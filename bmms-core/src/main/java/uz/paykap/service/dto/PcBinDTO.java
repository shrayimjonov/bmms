package uz.paykap.service.dto;

import javax.validation.constraints.*;
import java.io.Serializable;
import java.util.Objects;
import uz.paykap.domain.enumeration.BinPcType;

/**
 * A DTO for the PcBin entity.
 */
public class PcBinDTO implements Serializable {

    private Long id;

    @NotNull
    @Size(min = 1, max = 32)
    private String bin;

    @Size(max = 128)
    private String bankName;

    private BinPcType pcType;

    @NotNull
    private Boolean useCredit;

    @NotNull
    private Boolean useDebit;

    @NotNull
    private Boolean useReverse;

    @NotNull
    private Boolean useReconcile;

    @Size(max = 256)
    private String comment;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getBin() {
        return bin;
    }

    public void setBin(String bin) {
        this.bin = bin;
    }

    public String getBankName() {
        return bankName;
    }

    public void setBankName(String bankName) {
        this.bankName = bankName;
    }

    public BinPcType getPcType() {
        return pcType;
    }

    public void setPcType(BinPcType pcType) {
        this.pcType = pcType;
    }

    public Boolean isUseCredit() {
        return useCredit;
    }

    public void setUseCredit(Boolean useCredit) {
        this.useCredit = useCredit;
    }

    public Boolean isUseDebit() {
        return useDebit;
    }

    public void setUseDebit(Boolean useDebit) {
        this.useDebit = useDebit;
    }

    public Boolean isUseReverse() {
        return useReverse;
    }

    public void setUseReverse(Boolean useReverse) {
        this.useReverse = useReverse;
    }

    public Boolean isUseReconcile() {
        return useReconcile;
    }

    public void setUseReconcile(Boolean useReconcile) {
        this.useReconcile = useReconcile;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        PcBinDTO pcBinDTO = (PcBinDTO) o;
        if (pcBinDTO.getId() == null || getId() == null) {
            return false;
        }
        return Objects.equals(getId(), pcBinDTO.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    @Override
    public String toString() {
        return "PcBinDTO{" +
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
