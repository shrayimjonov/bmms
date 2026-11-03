package uz.paykap.service.dto;

import io.github.jhipster.service.filter.BooleanFilter;
import io.github.jhipster.service.filter.Filter;
import java.io.Serializable;
import tech.jhipster.service.filter.LongFilter;
import tech.jhipster.service.filter.StringFilter;
import uz.paykap.domain.enumeration.ModeType;

/**
 * Criteria class for the VirtualAccount entity. This class is used in VirtualAccountResource to
 * receive all the possible filtering options from the Http GET request parameters.
 * For example the following could be a valid requests:
 * <code> /virtual-accounts?id.greaterThan=5&amp;attr1.contains=something&amp;attr2.specified=false</code>
 * As Spring is unable to properly convert the types, unless specific {@link Filter} class are used, we need to use
 * fix type specific filters.
 */
public class VirtualAccountCriteria implements Serializable {

    /**
     * Class for filtering ModeType
     */
    public static class ModeTypeFilter extends Filter<ModeType> {}

    private static final long serialVersionUID = 1L;

    private LongFilter id;

    private StringFilter name;

    private StringFilter orgName;

    private BooleanFilter active;

    private StringFilter inn;

    private LongFilter uid;

    private ModeTypeFilter mode;

    private LongFilter depositId;

    public VirtualAccountCriteria() {}

    public LongFilter getId() {
        return id;
    }

    public void setId(LongFilter id) {
        this.id = id;
    }

    public StringFilter getName() {
        return name;
    }

    public void setName(StringFilter name) {
        this.name = name;
    }

    public StringFilter getOrgName() {
        return orgName;
    }

    public void setOrgName(StringFilter orgName) {
        this.orgName = orgName;
    }

    public BooleanFilter getActive() {
        return active;
    }

    public void setActive(BooleanFilter active) {
        this.active = active;
    }

    public StringFilter getInn() {
        return inn;
    }

    public void setInn(StringFilter inn) {
        this.inn = inn;
    }

    public LongFilter getUid() {
        return uid;
    }

    public void setUid(LongFilter uid) {
        this.uid = uid;
    }

    public ModeTypeFilter getMode() {
        return mode;
    }

    public void setMode(ModeTypeFilter mode) {
        this.mode = mode;
    }

    public LongFilter getDepositId() {
        return depositId;
    }

    public void setDepositId(LongFilter depositId) {
        this.depositId = depositId;
    }

    @Override
    public String toString() {
        return (
            "VirtualAccountCriteria{" +
            (id != null ? "id=" + id + ", " : "") +
            (name != null ? "name=" + name + ", " : "") +
            (orgName != null ? "orgName=" + orgName + ", " : "") +
            (active != null ? "active=" + active + ", " : "") +
            (inn != null ? "inn=" + inn + ", " : "") +
            (uid != null ? "uid=" + uid + ", " : "") +
            (mode != null ? "mode=" + mode + ", " : "") +
            (depositId != null ? "depositId=" + depositId + ", " : "") +
            "}"
        );
    }
}
