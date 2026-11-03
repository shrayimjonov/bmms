package uz.paykap.service.dto;

import io.github.jhipster.service.filter.BooleanFilter;
import io.github.jhipster.service.filter.Filter;
import java.io.Serializable;
import tech.jhipster.service.filter.LongFilter;
import tech.jhipster.service.filter.StringFilter;
import uz.paykap.domain.enumeration.VirtualAccountType;

/**
 * Criteria class for the VirtualAccountDetails entity. This class is used in VirtualAccountDetailsResource to
 * receive all the possible filtering options from the Http GET request parameters.
 * For example the following could be a valid requests:
 * <code> /virtual-account-details?id.greaterThan=5&amp;attr1.contains=something&amp;attr2.specified=false</code>
 * As Spring is unable to properly convert the types, unless specific {@link Filter} class are used, we need to use
 * fix type specific filters.
 */
public class VirtualAccountDetailsCriteria implements Serializable {

    /**
     * Class for filtering VirtualAccountType
     */
    public static class VirtualAccountTypeFilter extends Filter<VirtualAccountType> {}

    private static final long serialVersionUID = 1L;

    private LongFilter id;

    private StringFilter params;

    private StringFilter account;

    private BooleanFilter active;

    private StringFilter mfo;

    private VirtualAccountTypeFilter type;

    private LongFilter virtualAccountId;

    public VirtualAccountDetailsCriteria() {}

    public LongFilter getId() {
        return id;
    }

    public void setId(LongFilter id) {
        this.id = id;
    }

    public StringFilter getParams() {
        return params;
    }

    public void setParams(StringFilter params) {
        this.params = params;
    }

    public StringFilter getAccount() {
        return account;
    }

    public void setAccount(StringFilter account) {
        this.account = account;
    }

    public BooleanFilter getActive() {
        return active;
    }

    public void setActive(BooleanFilter active) {
        this.active = active;
    }

    public StringFilter getMfo() {
        return mfo;
    }

    public void setMfo(StringFilter mfo) {
        this.mfo = mfo;
    }

    public VirtualAccountTypeFilter getType() {
        return type;
    }

    public void setType(VirtualAccountTypeFilter type) {
        this.type = type;
    }

    public LongFilter getVirtualAccountId() {
        return virtualAccountId;
    }

    public void setVirtualAccountId(LongFilter virtualAccountId) {
        this.virtualAccountId = virtualAccountId;
    }

    @Override
    public String toString() {
        return (
            "VirtualAccountDetailsCriteria{" +
            (id != null ? "id=" + id + ", " : "") +
            (params != null ? "params=" + params + ", " : "") +
            (account != null ? "account=" + account + ", " : "") +
            (active != null ? "active=" + active + ", " : "") +
            (mfo != null ? "mfo=" + mfo + ", " : "") +
            (type != null ? "type=" + type + ", " : "") +
            (virtualAccountId != null ? "virtualAccountId=" + virtualAccountId + ", " : "") +
            "}"
        );
    }
}
