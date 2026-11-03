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
 * Criteria class for the {@link uz.paykap.domain.P2pCommission} entity. This class is used
 * in {@link uz.paykap.web.rest.P2pCommissionResource} to receive all the possible filtering options from
 * the Http GET request parameters.
 * For example the following could be a valid request:
 * {@code /p-2-p-commissions?id.greaterThan=5&attr1.contains=something&attr2.specified=false}
 * As Spring is unable to properly convert the types, unless specific {@link Filter} class are used, we need to use
 * fix type specific filters.
 */
public class P2pCommissionCriteria implements Serializable, Criteria {

    private static final long serialVersionUID = 1L;

    private LongFilter id;

    private StringFilter name;

    private DoubleFilter up;

    private DoubleFilter down;

    private DoubleFilter fee;

    private StringFilter description;

    public P2pCommissionCriteria() {}

    public P2pCommissionCriteria(P2pCommissionCriteria other) {
        this.id = other.id == null ? null : other.id.copy();
        this.name = other.name == null ? null : other.name.copy();
        this.up = other.up == null ? null : other.up.copy();
        this.down = other.down == null ? null : other.down.copy();
        this.fee = other.fee == null ? null : other.fee.copy();
        this.description = other.description == null ? null : other.description.copy();
    }

    @Override
    public P2pCommissionCriteria copy() {
        return new P2pCommissionCriteria(this);
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

    public DoubleFilter getUp() {
        return up;
    }

    public DoubleFilter up() {
        if (up == null) {
            up = new DoubleFilter();
        }
        return up;
    }

    public void setUp(DoubleFilter up) {
        this.up = up;
    }

    public DoubleFilter getDown() {
        return down;
    }

    public DoubleFilter down() {
        if (down == null) {
            down = new DoubleFilter();
        }
        return down;
    }

    public void setDown(DoubleFilter down) {
        this.down = down;
    }

    public DoubleFilter getFee() {
        return fee;
    }

    public DoubleFilter fee() {
        if (fee == null) {
            fee = new DoubleFilter();
        }
        return fee;
    }

    public void setFee(DoubleFilter fee) {
        this.fee = fee;
    }

    public StringFilter getDescription() {
        return description;
    }

    public StringFilter description() {
        if (description == null) {
            description = new StringFilter();
        }
        return description;
    }

    public void setDescription(StringFilter description) {
        this.description = description;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final P2pCommissionCriteria that = (P2pCommissionCriteria) o;
        return (
            Objects.equals(id, that.id) &&
            Objects.equals(name, that.name) &&
            Objects.equals(up, that.up) &&
            Objects.equals(down, that.down) &&
            Objects.equals(fee, that.fee) &&
            Objects.equals(description, that.description)
        );
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, up, down, fee, description);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "P2pCommissionCriteria{" +
            (id != null ? "id=" + id + ", " : "") +
            (name != null ? "name=" + name + ", " : "") +
            (up != null ? "up=" + up + ", " : "") +
            (down != null ? "down=" + down + ", " : "") +
            (fee != null ? "fee=" + fee + ", " : "") +
            (description != null ? "description=" + description + ", " : "") +
            "}";
    }
}
