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
 * Criteria class for the {@link uz.paykap.domain.Category} entity. This class is used
 * in {@link uz.paykap.web.rest.CategoryResource} to receive all the possible filtering options from
 * the Http GET request parameters.
 * For example the following could be a valid request:
 * {@code /categories?id.greaterThan=5&attr1.contains=something&attr2.specified=false}
 * As Spring is unable to properly convert the types, unless specific {@link Filter} class are used, we need to use
 * fix type specific filters.
 */
public class CategoryCriteria implements Serializable, Criteria {

    private static final long serialVersionUID = 1L;

    private LongFilter id;

    private StringFilter shortName;

    private StringFilter thumbnail;

    private StringFilter logo;

    private IntegerFilter ord;

    private BooleanFilter active;

    private LongFilter parentId;

    private LongFilter nameId;

    public CategoryCriteria() {}

    public CategoryCriteria(CategoryCriteria other) {
        this.id = other.id == null ? null : other.id.copy();
        this.shortName = other.shortName == null ? null : other.shortName.copy();
        this.thumbnail = other.thumbnail == null ? null : other.thumbnail.copy();
        this.logo = other.logo == null ? null : other.logo.copy();
        this.ord = other.ord == null ? null : other.ord.copy();
        this.active = other.active == null ? null : other.active.copy();
        this.parentId = other.parentId == null ? null : other.parentId.copy();
        this.nameId = other.nameId == null ? null : other.nameId.copy();
    }

    @Override
    public CategoryCriteria copy() {
        return new CategoryCriteria(this);
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

    public StringFilter getThumbnail() {
        return thumbnail;
    }

    public StringFilter thumbnail() {
        if (thumbnail == null) {
            thumbnail = new StringFilter();
        }
        return thumbnail;
    }

    public void setThumbnail(StringFilter thumbnail) {
        this.thumbnail = thumbnail;
    }

    public StringFilter getLogo() {
        return logo;
    }

    public StringFilter logo() {
        if (logo == null) {
            logo = new StringFilter();
        }
        return logo;
    }

    public void setLogo(StringFilter logo) {
        this.logo = logo;
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

    public LongFilter getParentId() {
        return parentId;
    }

    public LongFilter parentId() {
        if (parentId == null) {
            parentId = new LongFilter();
        }
        return parentId;
    }

    public void setParentId(LongFilter parentId) {
        this.parentId = parentId;
    }

    public LongFilter getNameId() {
        return nameId;
    }

    public LongFilter nameId() {
        if (nameId == null) {
            nameId = new LongFilter();
        }
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
        return (
            Objects.equals(id, that.id) &&
            Objects.equals(shortName, that.shortName) &&
            Objects.equals(thumbnail, that.thumbnail) &&
            Objects.equals(logo, that.logo) &&
            Objects.equals(ord, that.ord) &&
            Objects.equals(active, that.active) &&
            Objects.equals(parentId, that.parentId) &&
            Objects.equals(nameId, that.nameId)
        );
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, shortName, thumbnail, logo, ord, active, parentId, nameId);
    }

    // prettier-ignore
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
