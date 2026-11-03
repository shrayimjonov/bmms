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
 * Criteria class for the {@link uz.paykap.domain.RecipientFileSchema} entity. This class is used
 * in {@link uz.paykap.web.rest.RecipientFileSchemaResource} to receive all the possible filtering options from
 * the Http GET request parameters.
 * For example the following could be a valid request:
 * {@code /recipient-file-schemas?id.greaterThan=5&attr1.contains=something&attr2.specified=false}
 * As Spring is unable to properly convert the types, unless specific {@link Filter} class are used, we need to use
 * fix type specific filters.
 */
public class RecipientFileSchemaCriteria implements Serializable, Criteria {

    private static final long serialVersionUID = 1L;

    private LongFilter id;

    private StringFilter name;

    private StringFilter idList;

    private StringFilter root;

    public RecipientFileSchemaCriteria() {}

    public RecipientFileSchemaCriteria(RecipientFileSchemaCriteria other) {
        this.id = other.id == null ? null : other.id.copy();
        this.name = other.name == null ? null : other.name.copy();
        this.idList = other.idList == null ? null : other.idList.copy();
        this.root = other.root == null ? null : other.root.copy();
    }

    @Override
    public RecipientFileSchemaCriteria copy() {
        return new RecipientFileSchemaCriteria(this);
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

    public StringFilter getIdList() {
        return idList;
    }

    public StringFilter idList() {
        if (idList == null) {
            idList = new StringFilter();
        }
        return idList;
    }

    public void setIdList(StringFilter idList) {
        this.idList = idList;
    }

    public StringFilter getRoot() {
        return root;
    }

    public StringFilter root() {
        if (root == null) {
            root = new StringFilter();
        }
        return root;
    }

    public void setRoot(StringFilter root) {
        this.root = root;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final RecipientFileSchemaCriteria that = (RecipientFileSchemaCriteria) o;
        return (
            Objects.equals(id, that.id) &&
            Objects.equals(name, that.name) &&
            Objects.equals(idList, that.idList) &&
            Objects.equals(root, that.root)
        );
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, idList, root);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "RecipientFileSchemaCriteria{" +
            (id != null ? "id=" + id + ", " : "") +
            (name != null ? "name=" + name + ", " : "") +
            (idList != null ? "idList=" + idList + ", " : "") +
            (root != null ? "root=" + root + ", " : "") +
            "}";
    }
}
