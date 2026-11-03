package uz.paykap.service.dto;

import io.github.jhipster.service.filter.*;

import java.io.Serializable;
import java.util.Objects;

/**
 * Criteria class for the Category entity. This class is used in CategoryResource to
 * receive all the possible filtering options from the Http GET request parameters.
 * For example the following could be a valid requests:
 * <code> /categories?id.greaterThan=5&amp;attr1.contains=something&amp;attr2.specified=false</code>
 * As Spring is unable to properly convert the types, unless specific {@link Filter} class are used, we need to use
 * fix type specific filters.
 */
public class CategoryCriteria implements Serializable {

    private static final long serialVersionUID = 1L;

    private LongFilter id;

    private StringFilter shortName;

    private StringFilter thumbnail;

    private StringFilter logo;

    private IntegerFilter ord;

    private BooleanFilter active;

    private LongFilter parentId;

    private LongFilter nameId;

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

    public StringFilter getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(StringFilter thumbnail) {
        this.thumbnail = thumbnail;
    }

    public StringFilter getLogo() {
        return logo;
    }

    public void setLogo(StringFilter logo) {
        this.logo = logo;
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

    public LongFilter getParentId() {
        return parentId;
    }

    public void setParentId(LongFilter parentId) {
        this.parentId = parentId;
    }

    public LongFilter getNameId() {
        return nameId;
    }

    public void setNameId(LongFilter nameId) {
        this.nameId = nameId;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final CategoryCriteria that = (CategoryCriteria) o;
        return
            Objects.equals(id, that.id) &&
                Objects.equals(shortName, that.shortName) &&
                Objects.equals(thumbnail, that.thumbnail) &&
                Objects.equals(logo, that.logo) &&
                Objects.equals(ord, that.ord) &&
                Objects.equals(active, that.active) &&
                Objects.equals(parentId, that.parentId) &&
                Objects.equals(nameId, that.nameId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
            id,
            shortName,
            thumbnail,
            logo,
            ord,
            active,
            parentId,
            nameId
        );
    }

    @Override
    public String toString() {
        return "CategoryCriteria{" +
                (id != null ? "id=" + id + ", " : "") +
                (shortName != null ? "shortName=" + shortName + ", " : "") +
                (thumbnail != null ? "thumbnail=" + thumbnail + ", " : "") +
                (logo != null ? "logo=" + logo + ", " : "") +
                (ord != null ? "ord=" + ord + ", " : "") +
                (active != null ? "active=" + active + ", " : "") +
                (parentId != null ? "parentId=" + parentId + ", " : "") +
                (nameId != null ? "nameId=" + nameId + ", " : "") +
            "}";
    }

}
