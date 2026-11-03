package uz.paykap.service.dto;

import io.github.jhipster.service.filter.*;

import java.io.Serializable;
import java.util.Objects;

/**
 * Criteria class for the Setting entity. This class is used in SettingResource to
 * receive all the possible filtering options from the Http GET request parameters.
 * For example the following could be a valid requests:
 * <code> /settings?id.greaterThan=5&amp;attr1.contains=something&amp;attr2.specified=false</code>
 * As Spring is unable to properly convert the types, unless specific {@link Filter} class are used, we need to use
 * fix type specific filters.
 */
public class SettingCriteria implements Serializable {

    private static final long serialVersionUID = 1L;

    private LongFilter id;

    private StringFilter shortName;

    private IntegerFilter ord;

    private BooleanFilter active;

    private LongFilter propId;

    public LongFilter getId() {
        return id;
    }

    public void setId(LongFilter id) {
        this.id = id;
    }

    public StringFilter getShortName() {
        return shortName;
    }

    public void setShortName(StringFilter shortName) {
        this.shortName = shortName;
    }

    public IntegerFilter getOrd() {
        return ord;
    }

    public void setOrd(IntegerFilter ord) {
        this.ord = ord;
    }

    public BooleanFilter getActive() {
        return active;
    }

    public void setActive(BooleanFilter active) {
        this.active = active;
    }

    public LongFilter getPropId() {
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
        return
            Objects.equals(id, that.id) &&
                Objects.equals(shortName, that.shortName) &&
                Objects.equals(ord, that.ord) &&
                Objects.equals(active, that.active) &&
                Objects.equals(propId, that.propId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
            id,
            shortName,
            ord,
            active,
            propId
        );
    }

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
