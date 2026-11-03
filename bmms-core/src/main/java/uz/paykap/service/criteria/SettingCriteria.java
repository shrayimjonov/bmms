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

/**
 * Criteria class for the {@link uz.paykap.domain.Setting} entity. This class is used
 * in {@link uz.paykap.web.rest.SettingResource} to receive all the possible filtering options from
 * the Http GET request parameters.
 * For example the following could be a valid request:
 * {@code /settings?id.greaterThan=5&attr1.contains=something&attr2.specified=false}
 * As Spring is unable to properly convert the types, unless specific {@link Filter} class are used, we need to use
 * fix type specific filters.
 */
public class SettingCriteria implements Serializable, Criteria {

    private static final long serialVersionUID = 1L;

    private LongFilter id;

    private StringFilter shortName;

    private IntegerFilter ord;

    private BooleanFilter active;

    private LongFilter propId;

    public SettingCriteria() {}

    public SettingCriteria(SettingCriteria other) {
        this.id = other.id == null ? null : other.id.copy();
        this.shortName = other.shortName == null ? null : other.shortName.copy();
        this.ord = other.ord == null ? null : other.ord.copy();
        this.active = other.active == null ? null : other.active.copy();
        this.propId = other.propId == null ? null : other.propId.copy();
    }

    @Override
    public SettingCriteria copy() {
        return new SettingCriteria(this);
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

    public StringFilter getShortName() {
        return shortName;
    }

    public StringFilter shortName() {
        if (shortName == null) {
            shortName = new StringFilter();
        }
        return shortName;
    }

    public void setShortName(StringFilter shortName) {
        this.shortName = shortName;
    }

    public IntegerFilter getOrd() {
        return ord;
    }

    public IntegerFilter ord() {
        if (ord == null) {
            ord = new IntegerFilter();
        }
        return ord;
    }

    public void setOrd(IntegerFilter ord) {
        this.ord = ord;
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

    public LongFilter getPropId() {
        return propId;
    }

    public LongFilter propId() {
        if (propId == null) {
            propId = new LongFilter();
        }
        return propId;
    }

    public void setPropId(LongFilter propId) {
        this.propId = propId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final SettingCriteria that = (SettingCriteria) o;
        return (
            Objects.equals(id, that.id) &&
            Objects.equals(shortName, that.shortName) &&
            Objects.equals(ord, that.ord) &&
            Objects.equals(active, that.active) &&
            Objects.equals(propId, that.propId)
        );
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, shortName, ord, active, propId);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "SettingCriteria{" +
            (id != null ? "id=" + id + ", " : "") +
            (shortName != null ? "shortName=" + shortName + ", " : "") +
            (ord != null ? "ord=" + ord + ", " : "") +
            (active != null ? "active=" + active + ", " : "") +
            (propId != null ? "propId=" + propId + ", " : "") +
            "}";
    }
}
