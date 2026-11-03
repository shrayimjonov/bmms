package uz.paykap.service.dto;

import io.github.jhipster.service.filter.BooleanFilter;
import io.github.jhipster.service.filter.Filter;
import java.io.Serializable;
import java.util.Objects;
import tech.jhipster.service.filter.LongFilter;
import tech.jhipster.service.filter.StringFilter;

/**
 * Criteria class for the Gateway entity. This class is used in GatewayResource to
 * receive all the possible filtering options from the Http GET request parameters.
 * For example the following could be a valid requests:
 * <code> /gateways?id.greaterThan=5&amp;attr1.contains=something&amp;attr2.specified=false</code>
 * As Spring is unable to properly convert the types, unless specific {@link Filter} class are used, we need to use
 * fix type specific filters.
 */
public class GatewayCriteria implements Serializable {

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

    public StringFilter getOrg() {
        return org;
    }

    public void setOrg(StringFilter org) {
        this.org = org;
    }

    public BooleanFilter getActive() {
        return active;
    }

    public void setActive(BooleanFilter active) {
        this.active = active;
    }

    public StringFilter getMfo() {
        return mfo;
    }

    public void setMfo(StringFilter mfo) {
        this.mfo = mfo;
    }

    public LongFilter getTaxnr() {
        return taxnr;
    }

    public void setTaxnr(LongFilter taxnr) {
        this.taxnr = taxnr;
    }

    public StringFilter getBan() {
        return ban;
    }

    public void setBan(StringFilter ban) {
        this.ban = ban;
    }

    public StringFilter getContract() {
        return contract;
    }

    public void setContract(StringFilter contract) {
        this.contract = contract;
    }

    public StringFilter getSpringBeanName() {
        return springBeanName;
    }

    public void setSpringBeanName(StringFilter springBeanName) {
        this.springBeanName = springBeanName;
    }

    public LongFilter getDepositId() {
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

    @Override
    public String toString() {
        return (
            "GatewayCriteria{" +
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
            "}"
        );
    }
}
