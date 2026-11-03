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
 * Criteria class for the {@link uz.paykap.domain.GroupSetting} entity. This class is used
 * in {@link uz.paykap.web.rest.GroupSettingResource} to receive all the possible filtering options from
 * the Http GET request parameters.
 * For example the following could be a valid request:
 * {@code /group-settings?id.greaterThan=5&attr1.contains=something&attr2.specified=false}
 * As Spring is unable to properly convert the types, unless specific {@link Filter} class are used, we need to use
 * fix type specific filters.
 */
public class GroupSettingCriteria implements Serializable, Criteria {

    private static final long serialVersionUID = 1L;

    private LongFilter id;

    private StringFilter requestGroup;

    private LongFilter settingTypeId;

    private LongFilter propId;

    public GroupSettingCriteria() {}

    public GroupSettingCriteria(GroupSettingCriteria other) {
        this.id = other.id == null ? null : other.id.copy();
        this.requestGroup = other.requestGroup == null ? null : other.requestGroup.copy();
        this.settingTypeId = other.settingTypeId == null ? null : other.settingTypeId.copy();
        this.propId = other.propId == null ? null : other.propId.copy();
    }

    @Override
    public GroupSettingCriteria copy() {
        return new GroupSettingCriteria(this);
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

    public StringFilter getRequestGroup() {
        return requestGroup;
    }

    public StringFilter requestGroup() {
        if (requestGroup == null) {
            requestGroup = new StringFilter();
        }
        return requestGroup;
    }

    public void setRequestGroup(StringFilter requestGroup) {
        this.requestGroup = requestGroup;
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
        final GroupSettingCriteria that = (GroupSettingCriteria) o;
        return (
            Objects.equals(id, that.id) &&
            Objects.equals(requestGroup, that.requestGroup) &&
            Objects.equals(settingTypeId, that.settingTypeId) &&
            Objects.equals(propId, that.propId)
        );
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, requestGroup, settingTypeId, propId);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "GroupSettingCriteria{" +
            (id != null ? "id=" + id + ", " : "") +
            (requestGroup != null ? "requestGroup=" + requestGroup + ", " : "") +
            (settingTypeId != null ? "settingTypeId=" + settingTypeId + ", " : "") +
            (propId != null ? "propId=" + propId + ", " : "") +
            "}";
    }
}
