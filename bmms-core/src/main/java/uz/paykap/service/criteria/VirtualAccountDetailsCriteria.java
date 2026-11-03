package uz.paykap.service.criteria;

import java.io.Serializable;
import java.util.Objects;
import tech.jhipster.service.Criteria;
import tech.jhipster.service.filter.BooleanFilter;
import tech.jhipster.service.filter.DoubleFilter;
import tech.jhipster.service.filter.Filter;
import tech.jhipster.service.filter.FloatFilter;
import tech.jhipster.service.filter.IntegerFilter;
import tech.jhipster.service.filter.LongFilter;
import tech.jhipster.service.filter.StringFilter;
import uz.paykap.domain.enumeration.VirtualAccountType;

/**
 * Criteria class for the {@link uz.paykap.domain.VirtualAccountDetails} entity. This class is used
 * in {@link uz.paykap.web.rest.VirtualAccountDetailsResource} to receive all the possible filtering options from
 * the Http GET request parameters.
 * For example the following could be a valid request:
 * {@code /virtual-account-details?id.greaterThan=5&attr1.contains=something&attr2.specified=false}
 * As Spring is unable to properly convert the types, unless specific {@link Filter} class are used, we need to use
 * fix type specific filters.
 */
public class VirtualAccountDetailsCriteria implements Serializable, Criteria {

    /**
     * Class for filtering VirtualAccountType
     */
    public static class VirtualAccountTypeFilter extends Filter<VirtualAccountType> {

        public VirtualAccountTypeFilter() {}

        public VirtualAccountTypeFilter(VirtualAccountTypeFilter filter) {
            super(filter);
        }

        @Override
        public VirtualAccountTypeFilter copy() {
            return new VirtualAccountTypeFilter(this);
        }
    }

    private static final long serialVersionUID = 1L;

    private LongFilter id;

    private StringFilter params;

    private StringFilter account;

    private BooleanFilter active;

    private StringFilter mfo;

    private VirtualAccountTypeFilter type;

    private LongFilter virtualAccountId;

    public VirtualAccountDetailsCriteria() {}

    public VirtualAccountDetailsCriteria(VirtualAccountDetailsCriteria other) {
        this.id = other.id == null ? null : other.id.copy();
        this.params = other.params == null ? null : other.params.copy();
        this.account = other.account == null ? null : other.account.copy();
        this.active = other.active == null ? null : other.active.copy();
        this.mfo = other.mfo == null ? null : other.mfo.copy();
        this.type = other.type == null ? null : other.type.copy();
        this.virtualAccountId = other.virtualAccountId == null ? null : other.virtualAccountId.copy();
    }

    @Override
    public VirtualAccountDetailsCriteria copy() {
        return new VirtualAccountDetailsCriteria(this);
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

    public StringFilter getParams() {
        return params;
    }

    public StringFilter params() {
        if (params == null) {
            params = new StringFilter();
        }
        return params;
    }

    public void setParams(StringFilter params) {
        this.params = params;
    }

    public StringFilter getAccount() {
        return account;
    }

    public StringFilter account() {
        if (account == null) {
            account = new StringFilter();
        }
        return account;
    }

    public void setAccount(StringFilter account) {
        this.account = account;
    }

    public BooleanFilter getActive() {
        return active;
    }

    public BooleanFilter active() {
        if (active == null) {
            active = new BooleanFilter();
        }
        return active;
    }

    public void setActive(BooleanFilter active) {
        this.active = active;
    }

    public StringFilter getMfo() {
        return mfo;
    }

    public StringFilter mfo() {
        if (mfo == null) {
            mfo = new StringFilter();
        }
        return mfo;
    }

    public void setMfo(StringFilter mfo) {
        this.mfo = mfo;
    }

    public VirtualAccountTypeFilter getType() {
        return type;
    }

    public VirtualAccountTypeFilter type() {
        if (type == null) {
            type = new VirtualAccountTypeFilter();
        }
        return type;
    }

    public void setType(VirtualAccountTypeFilter type) {
        this.type = type;
    }

    public LongFilter getVirtualAccountId() {
        return virtualAccountId;
    }

    public LongFilter virtualAccountId() {
        if (virtualAccountId == null) {
            virtualAccountId = new LongFilter();
        }
        return virtualAccountId;
    }

    public void setVirtualAccountId(LongFilter virtualAccountId) {
        this.virtualAccountId = virtualAccountId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final VirtualAccountDetailsCriteria that = (VirtualAccountDetailsCriteria) o;
        return (
            Objects.equals(id, that.id) &&
            Objects.equals(params, that.params) &&
            Objects.equals(account, that.account) &&
            Objects.equals(active, that.active) &&
            Objects.equals(mfo, that.mfo) &&
            Objects.equals(type, that.type) &&
            Objects.equals(virtualAccountId, that.virtualAccountId)
        );
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, params, account, active, mfo, type, virtualAccountId);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "VirtualAccountDetailsCriteria{" +
            (id != null ? "id=" + id + ", " : "") +
            (params != null ? "params=" + params + ", " : "") +
            (account != null ? "account=" + account + ", " : "") +
            (active != null ? "active=" + active + ", " : "") +
            (mfo != null ? "mfo=" + mfo + ", " : "") +
            (type != null ? "type=" + type + ", " : "") +
            (virtualAccountId != null ? "virtualAccountId=" + virtualAccountId + ", " : "") +
            "}";
    }
}
