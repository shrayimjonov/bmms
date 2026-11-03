package uz.paykap.service.dto;

import io.github.jhipster.service.filter.BooleanFilter;
import io.github.jhipster.service.filter.DoubleFilter;
import io.github.jhipster.service.filter.Filter;
import io.github.jhipster.service.filter.FloatFilter;
import io.github.jhipster.service.filter.IntegerFilter;
import java.io.Serializable;
import java.util.Objects;
import tech.jhipster.service.filter.LongFilter;
import tech.jhipster.service.filter.StringFilter;

/**
 * Criteria class for the P2pCommission entity. This class is used in P2pCommissionResource to
 * receive all the possible filtering options from the Http GET request parameters.
 * For example the following could be a valid requests:
 * <code> /p-2-p-commissions?id.greaterThan=5&amp;attr1.contains=something&amp;attr2.specified=false</code>
 * As Spring is unable to properly convert the types, unless specific {@link Filter} class are used, we need to use
 * fix type specific filters.
 */
public class P2pCommissionCriteria implements Serializable {

    private static final long serialVersionUID = 1L;

    private LongFilter id;

    private StringFilter name;

    private DoubleFilter up;

    private DoubleFilter down;

    private DoubleFilter fee;

    private StringFilter description;

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

    public DoubleFilter getUp() {
        return up;
    }

    public void setUp(DoubleFilter up) {
        this.up = up;
    }

    public DoubleFilter getDown() {
        return down;
    }

    public void setDown(DoubleFilter down) {
        this.down = down;
    }

    public DoubleFilter getFee() {
        return fee;
    }

    public void setFee(DoubleFilter fee) {
        this.fee = fee;
    }

    public StringFilter getDescription() {
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

    @Override
    public String toString() {
        return (
            "P2pCommissionCriteria{" +
            (id != null ? "id=" + id + ", " : "") +
            (name != null ? "name=" + name + ", " : "") +
            (up != null ? "up=" + up + ", " : "") +
            (down != null ? "down=" + down + ", " : "") +
            (fee != null ? "fee=" + fee + ", " : "") +
            (description != null ? "description=" + description + ", " : "") +
            "}"
        );
    }
}
