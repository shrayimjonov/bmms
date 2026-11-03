package uz.paykap.service.dto;

import java.io.Serializable;
import java.util.Objects;
import uz.paykap.domain.enumeration.BinPcType;
import tech.jhipster.service.filter.BooleanFilter;
import tech.jhipster.service.filter.DoubleFilter;
import tech.jhipster.service.filter.Filter;
import tech.jhipster.service.filter.FloatFilter;
import tech.jhipster.service.filter.IntegerFilter;
import tech.jhipster.service.filter.LongFilter;
import tech.jhipster.service.filter.StringFilter;

/**
 * Criteria class for the PcBin entity. This class is used in PcBinResource to
 * receive all the possible filtering options from the Http GET request parameters.
 * For example the following could be a valid requests:
 * <code> /pc-bins?id.greaterThan=5&amp;attr1.contains=something&amp;attr2.specified=false</code>
 * As Spring is unable to properly convert the types, unless specific {@link Filter} class are used, we need to use
 * fix type specific filters.
 */
public class PcBinCriteria implements Serializable {
    /**
     * Class for filtering BinPcType
     */
    public static class BinPcTypeFilter extends Filter<BinPcType> {
    }

    private static final long serialVersionUID = 1L;

    private LongFilter id;

    private StringFilter bin;

    private StringFilter bankName;

    private BinPcTypeFilter pcType;

    private BooleanFilter useCredit;

    private BooleanFilter useDebit;

    private BooleanFilter useReverse;

    private BooleanFilter useReconcile;

    private StringFilter comment;

    public LongFilter getId() {
        return id;
    }

    public void setId(LongFilter id) {
        this.id = id;
    }

    public StringFilter getBin() {
        return bin;
    }

    public void setBin(StringFilter bin) {
        this.bin = bin;
    }

    public StringFilter getBankName() {
        return bankName;
    }

    public void setBankName(StringFilter bankName) {
        this.bankName = bankName;
    }

    public BinPcTypeFilter getPcType() {
        return pcType;
    }

    public void setPcType(BinPcTypeFilter pcType) {
        this.pcType = pcType;
    }

    public BooleanFilter getUseCredit() {
        return useCredit;
    }

    public void setUseCredit(BooleanFilter useCredit) {
        this.useCredit = useCredit;
    }

    public BooleanFilter getUseDebit() {
        return useDebit;
    }

    public void setUseDebit(BooleanFilter useDebit) {
        this.useDebit = useDebit;
    }

    public BooleanFilter getUseReverse() {
        return useReverse;
    }

    public void setUseReverse(BooleanFilter useReverse) {
        this.useReverse = useReverse;
    }

    public BooleanFilter getUseReconcile() {
        return useReconcile;
    }

    public void setUseReconcile(BooleanFilter useReconcile) {
        this.useReconcile = useReconcile;
    }

    public StringFilter getComment() {
        return comment;
    }

    public void setComment(StringFilter comment) {
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
        final PcBinCriteria that = (PcBinCriteria) o;
        return
            Objects.equals(id, that.id) &&
            Objects.equals(bin, that.bin) &&
            Objects.equals(bankName, that.bankName) &&
            Objects.equals(pcType, that.pcType) &&
            Objects.equals(useCredit, that.useCredit) &&
            Objects.equals(useDebit, that.useDebit) &&
            Objects.equals(useReverse, that.useReverse) &&
            Objects.equals(useReconcile, that.useReconcile) &&
            Objects.equals(comment, that.comment);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
        id,
        bin,
        bankName,
        pcType,
        useCredit,
        useDebit,
        useReverse,
        useReconcile,
        comment
        );
    }

    @Override
    public String toString() {
        return "PcBinCriteria{" +
                (id != null ? "id=" + id + ", " : "") +
                (bin != null ? "bin=" + bin + ", " : "") +
                (bankName != null ? "bankName=" + bankName + ", " : "") +
                (pcType != null ? "pcType=" + pcType + ", " : "") +
                (useCredit != null ? "useCredit=" + useCredit + ", " : "") +
                (useDebit != null ? "useDebit=" + useDebit + ", " : "") +
                (useReverse != null ? "useReverse=" + useReverse + ", " : "") +
                (useReconcile != null ? "useReconcile=" + useReconcile + ", " : "") +
                (comment != null ? "comment=" + comment + ", " : "") +
            "}";
    }

}
