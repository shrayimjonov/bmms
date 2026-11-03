package uz.paykap.service.dto;

import io.github.jhipster.service.filter.Filter;
import java.io.Serializable;
import java.util.Objects;
import tech.jhipster.service.filter.LongFilter;
import tech.jhipster.service.filter.StringFilter;

/**
 * Criteria class for the GroupSetting entity. This class is used in GroupSettingResource to
 * receive all the possible filtering options from the Http GET request parameters.
 * For example the following could be a valid requests:
 * <code> /group-settings?id.greaterThan=5&amp;attr1.contains=something&amp;attr2.specified=false</code>
 * As Spring is unable to properly convert the types, unless specific {@link Filter} class are used, we need to use
 * fix type specific filters.
 */
public class GroupSettingCriteria implements Serializable {

    private static final long serialVersionUID = 1L;

    private LongFilter id;

    private StringFilter requestGroup;

    private LongFilter settingTypeId;

    private LongFilter propId;

    public LongFilter getId() {
        return id;
    }

    public void setId(LongFilter id) {
        this.id = id;
    }

    public StringFilter getRequestGroup() {
        return requestGroup;
    }

    public void setRequestGroup(StringFilter requestGroup) {
        this.requestGroup = requestGroup;
    }

    public LongFilter getSettingTypeId() {
        return settingTypeId;
    }

    public void setSettingTypeId(LongFilter settingTypeId) {
        this.settingTypeId = settingTypeId;
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

    @Override
    public String toString() {
        return (
            "GroupSettingCriteria{" +
            (id != null ? "id=" + id + ", " : "") +
            (requestGroup != null ? "requestGroup=" + requestGroup + ", " : "") +
            (settingTypeId != null ? "settingTypeId=" + settingTypeId + ", " : "") +
            (propId != null ? "propId=" + propId + ", " : "") +
            "}"
        );
    }
}
