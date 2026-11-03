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
 * Criteria class for the {@link uz.paykap.domain.RecipientGroup} entity. This class is used
 * in {@link uz.paykap.web.rest.RecipientGroupResource} to receive all the possible filtering options from
 * the Http GET request parameters.
 * For example the following could be a valid request:
 * {@code /recipient-groups?id.greaterThan=5&attr1.contains=something&attr2.specified=false}
 * As Spring is unable to properly convert the types, unless specific {@link Filter} class are used, we need to use
 * fix type specific filters.
 */
public class RecipientGroupCriteria implements Serializable, Criteria {

    private static final long serialVersionUID = 1L;

    private LongFilter id;

    private StringFilter name;

    private BooleanFilter active;

    private StringFilter more;

    private LongFilter recipientId;

    public RecipientGroupCriteria() {}

    public RecipientGroupCriteria(RecipientGroupCriteria other) {
        this.id = other.id == null ? null : other.id.copy();
        this.name = other.name == null ? null : other.name.copy();
        this.active = other.active == null ? null : other.active.copy();
        this.more = other.more == null ? null : other.more.copy();
        this.recipientId = other.recipientId == null ? null : other.recipientId.copy();
    }

    @Override
    public RecipientGroupCriteria copy() {
        return new RecipientGroupCriteria(this);
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

    public StringFilter getMore() {
        return more;
    }

    public StringFilter more() {
        if (more == null) {
            more = new StringFilter();
        }
        return more;
    }

    public void setMore(StringFilter more) {
        this.more = more;
    }

    public LongFilter getRecipientId() {
        return recipientId;
    }

    public LongFilter recipientId() {
        if (recipientId == null) {
            recipientId = new LongFilter();
        }
        return recipientId;
    }

    public void setRecipientId(LongFilter recipientId) {
        this.recipientId = recipientId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final RecipientGroupCriteria that = (RecipientGroupCriteria) o;
        return (
            Objects.equals(id, that.id) &&
            Objects.equals(name, that.name) &&
            Objects.equals(active, that.active) &&
            Objects.equals(more, that.more) &&
            Objects.equals(recipientId, that.recipientId)
        );
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, active, more, recipientId);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "RecipientGroupCriteria{" +
            (id != null ? "id=" + id + ", " : "") +
            (name != null ? "name=" + name + ", " : "") +
            (active != null ? "active=" + active + ", " : "") +
            (more != null ? "more=" + more + ", " : "") +
            (recipientId != null ? "recipientId=" + recipientId + ", " : "") +
            "}";
    }
}
