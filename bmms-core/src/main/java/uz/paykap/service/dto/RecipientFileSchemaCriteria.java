package uz.paykap.service.dto;

import io.github.jhipster.service.filter.Filter;
import java.io.Serializable;
import java.util.Objects;
import tech.jhipster.service.filter.LongFilter;
import tech.jhipster.service.filter.StringFilter;

/**
 * Criteria class for the RecipientFileSchema entity. This class is used in RecipientFileSchemaResource to
 * receive all the possible filtering options from the Http GET request parameters.
 * For example the following could be a valid requests:
 * <code> /recipient-file-schemas?id.greaterThan=5&amp;attr1.contains=something&amp;attr2.specified=false</code>
 * As Spring is unable to properly convert the types, unless specific {@link Filter} class are used, we need to use
 * fix type specific filters.
 */
public class RecipientFileSchemaCriteria implements Serializable {

    private static final long serialVersionUID = 1L;

    private LongFilter id;

    private StringFilter name;

    private StringFilter idList;

    private StringFilter root;

    public LongFilter getId() {
        return id;
    }

    public void setId(LongFilter id) {
        this.id = id;
    }

    public StringFilter getName() {
        return name;
    }

    public void setName(StringFilter name) {
        this.name = name;
    }

    public StringFilter getIdList() {
        return idList;
    }

    public void setIdList(StringFilter idList) {
        this.idList = idList;
    }

    public StringFilter getRoot() {
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

    @Override
    public String toString() {
        return (
            "RecipientFileSchemaCriteria{" +
            (id != null ? "id=" + id + ", " : "") +
            (name != null ? "name=" + name + ", " : "") +
            (idList != null ? "idList=" + idList + ", " : "") +
            (root != null ? "root=" + root + ", " : "") +
            "}"
        );
    }
}
