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
import uz.paykap.domain.enumeration.ModeType;

/**
 * Criteria class for the {@link uz.paykap.domain.VirtualAccount} entity. This class is used
 * in {@link uz.paykap.web.rest.VirtualAccountResource} to receive all the possible filtering options from
 * the Http GET request parameters.
 * For example the following could be a valid request:
 * {@code /virtual-accounts?id.greaterThan=5&attr1.contains=something&attr2.specified=false}
 * As Spring is unable to properly convert the types, unless specific {@link Filter} class are used, we need to use
 * fix type specific filters.
 */
public class VirtualAccountCriteria implements Serializable, Criteria {

    /**
     * Class for filtering ModeType
     */
    public static class ModeTypeFilter extends Filter<ModeType> {

        public ModeTypeFilter() {}

        public ModeTypeFilter(ModeTypeFilter filter) {
            super(filter);
        }

        @Override
        public ModeTypeFilter copy() {
            return new ModeTypeFilter(this);
        }
    }

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

    public VirtualAccountCriteria(VirtualAccountCriteria other) {
        this.id = other.id == null ? null : other.id.copy();
        this.name = other.name == null ? null : other.name.copy();
        this.orgName = other.orgName == null ? null : other.orgName.copy();
        this.active = other.active == null ? null : other.active.copy();
        this.inn = other.inn == null ? null : other.inn.copy();
        this.uid = other.uid == null ? null : other.uid.copy();
        this.mode = other.mode == null ? null : other.mode.copy();
        this.depositId = other.depositId == null ? null : other.depositId.copy();
    }

    @Override
    public VirtualAccountCriteria copy() {
        return new VirtualAccountCriteria(this);
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

    public StringFilter getName() {
        return name;
    }

    public StringFilter name() {
        if (name == null) {
            name = new StringFilter();
        }
        return name;
    }

    public void setName(StringFilter name) {
        this.name = name;
    }

    public StringFilter getOrgName() {
        return orgName;
    }

    public StringFilter orgName() {
        if (orgName == null) {
            orgName = new StringFilter();
        }
        return orgName;
    }

    public void setOrgName(StringFilter orgName) {
        this.orgName = orgName;
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

    public StringFilter getInn() {
        return inn;
    }

    public StringFilter inn() {
        if (inn == null) {
            inn = new StringFilter();
        }
        return inn;
    }

    public void setInn(StringFilter inn) {
        this.inn = inn;
    }

    public LongFilter getUid() {
        return uid;
    }

    public LongFilter uid() {
        if (uid == null) {
            uid = new LongFilter();
        }
        return uid;
    }

    public void setUid(LongFilter uid) {
        this.uid = uid;
    }

    public ModeTypeFilter getMode() {
        return mode;
    }

    public ModeTypeFilter mode() {
        if (mode == null) {
            mode = new ModeTypeFilter();
        }
        return mode;
    }

    public void setMode(ModeTypeFilter mode) {
        this.mode = mode;
    }

    public LongFilter getDepositId() {
        return depositId;
    }

    public LongFilter depositId() {
        if (depositId == null) {
            depositId = new LongFilter();
        }
        return depositId;
    }

    public void setDepositId(LongFilter depositId) {
        this.depositId = depositId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final VirtualAccountCriteria that = (VirtualAccountCriteria) o;
        return (
            Objects.equals(id, that.id) &&
            Objects.equals(name, that.name) &&
            Objects.equals(orgName, that.orgName) &&
            Objects.equals(active, that.active) &&
            Objects.equals(inn, that.inn) &&
            Objects.equals(uid, that.uid) &&
            Objects.equals(mode, that.mode) &&
            Objects.equals(depositId, that.depositId)
        );
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, orgName, active, inn, uid, mode, depositId);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "VirtualAccountCriteria{" +
            (id != null ? "id=" + id + ", " : "") +
            (name != null ? "name=" + name + ", " : "") +
            (orgName != null ? "orgName=" + orgName + ", " : "") +
            (active != null ? "active=" + active + ", " : "") +
            (inn != null ? "inn=" + inn + ", " : "") +
            (uid != null ? "uid=" + uid + ", " : "") +
            (mode != null ? "mode=" + mode + ", " : "") +
            (depositId != null ? "depositId=" + depositId + ", " : "") +
            "}";
    }
}
