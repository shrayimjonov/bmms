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
 * Criteria class for the {@link uz.paykap.domain.Gateway} entity. This class is used
 * in {@link uz.paykap.web.rest.GatewayResource} to receive all the possible filtering options from
 * the Http GET request parameters.
 * For example the following could be a valid request:
 * {@code /gateways?id.greaterThan=5&attr1.contains=something&attr2.specified=false}
 * As Spring is unable to properly convert the types, unless specific {@link Filter} class are used, we need to use
 * fix type specific filters.
 */
public class GatewayCriteria implements Serializable, Criteria {

    private static final long serialVersionUID = 1L;

    private LongFilter id;

    private StringFilter name;

    private StringFilter org;

    private BooleanFilter active;

    private StringFilter mfo;

    private LongFilter taxnr;

    private StringFilter ban;

    private StringFilter contract;

    private StringFilter springBeanName;

    private LongFilter depositId;

    public GatewayCriteria() {}

    public GatewayCriteria(GatewayCriteria other) {
        this.id = other.id == null ? null : other.id.copy();
        this.name = other.name == null ? null : other.name.copy();
        this.org = other.org == null ? null : other.org.copy();
        this.active = other.active == null ? null : other.active.copy();
        this.mfo = other.mfo == null ? null : other.mfo.copy();
        this.taxnr = other.taxnr == null ? null : other.taxnr.copy();
        this.ban = other.ban == null ? null : other.ban.copy();
        this.contract = other.contract == null ? null : other.contract.copy();
        this.springBeanName = other.springBeanName == null ? null : other.springBeanName.copy();
        this.depositId = other.depositId == null ? null : other.depositId.copy();
    }

    @Override
    public GatewayCriteria copy() {
        return new GatewayCriteria(this);
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

    public StringFilter getOrg() {
        return org;
    }

    public StringFilter org() {
        if (org == null) {
            org = new StringFilter();
        }
        return org;
    }

    public void setOrg(StringFilter org) {
        this.org = org;
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

    public StringFilter getMfo() {
        return mfo;
    }

    public StringFilter mfo() {
        if (mfo == null) {
            mfo = new StringFilter();
        }
        return mfo;
    }

    public void setMfo(StringFilter mfo) {
        this.mfo = mfo;
    }

    public LongFilter getTaxnr() {
        return taxnr;
    }

    public LongFilter taxnr() {
        if (taxnr == null) {
            taxnr = new LongFilter();
        }
        return taxnr;
    }

    public void setTaxnr(LongFilter taxnr) {
        this.taxnr = taxnr;
    }

    public StringFilter getBan() {
        return ban;
    }

    public StringFilter ban() {
        if (ban == null) {
            ban = new StringFilter();
        }
        return ban;
    }

    public void setBan(StringFilter ban) {
        this.ban = ban;
    }

    public StringFilter getContract() {
        return contract;
    }

    public StringFilter contract() {
        if (contract == null) {
            contract = new StringFilter();
        }
        return contract;
    }

    public void setContract(StringFilter contract) {
        this.contract = contract;
    }

    public StringFilter getSpringBeanName() {
        return springBeanName;
    }

    public StringFilter springBeanName() {
        if (springBeanName == null) {
            springBeanName = new StringFilter();
        }
        return springBeanName;
    }

    public void setSpringBeanName(StringFilter springBeanName) {
        this.springBeanName = springBeanName;
    }

    public LongFilter getDepositId() {
        return depositId;
    }

    public LongFilter depositId() {
        if (depositId == null) {
            depositId = new LongFilter();
        }
        return depositId;
    }

    public void setDepositId(LongFilter depositId) {
        this.depositId = depositId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final GatewayCriteria that = (GatewayCriteria) o;
        return (
            Objects.equals(id, that.id) &&
            Objects.equals(name, that.name) &&
            Objects.equals(org, that.org) &&
            Objects.equals(active, that.active) &&
            Objects.equals(mfo, that.mfo) &&
            Objects.equals(taxnr, that.taxnr) &&
            Objects.equals(ban, that.ban) &&
            Objects.equals(contract, that.contract) &&
            Objects.equals(springBeanName, that.springBeanName) &&
            Objects.equals(depositId, that.depositId)
        );
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, org, active, mfo, taxnr, ban, contract, springBeanName, depositId);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "GatewayCriteria{" +
            (id != null ? "id=" + id + ", " : "") +
            (name != null ? "name=" + name + ", " : "") +
            (org != null ? "org=" + org + ", " : "") +
            (active != null ? "active=" + active + ", " : "") +
            (mfo != null ? "mfo=" + mfo + ", " : "") +
            (taxnr != null ? "taxnr=" + taxnr + ", " : "") +
            (ban != null ? "ban=" + ban + ", " : "") +
            (contract != null ? "contract=" + contract + ", " : "") +
            (springBeanName != null ? "springBeanName=" + springBeanName + ", " : "") +
            (depositId != null ? "depositId=" + depositId + ", " : "") +
            "}";
    }
}
