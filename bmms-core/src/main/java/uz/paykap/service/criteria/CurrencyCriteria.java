package uz.paykap.service.criteria;

import java.io.Serializable;
import java.util.Objects;
import tech.jhipster.service.Criteria;
import tech.jhipster.service.filter.BigDecimalFilter;
import tech.jhipster.service.filter.BooleanFilter;
import tech.jhipster.service.filter.DoubleFilter;
import tech.jhipster.service.filter.Filter;
import tech.jhipster.service.filter.FloatFilter;
import tech.jhipster.service.filter.IntegerFilter;
import tech.jhipster.service.filter.LocalDateFilter;
import tech.jhipster.service.filter.LongFilter;
import tech.jhipster.service.filter.StringFilter;

/**
 * Criteria class for the {@link uz.paykap.domain.Currency} entity. This class is used
 * in {@link uz.paykap.web.rest.CurrencyResource} to receive all the possible filtering options from
 * the Http GET request parameters.
 * For example the following could be a valid request:
 * {@code /currencies?id.greaterThan=5&attr1.contains=something&attr2.specified=false}
 * As Spring is unable to properly convert the types, unless specific {@link Filter} class are used, we need to use
 * fix type specific filters.
 */
public class CurrencyCriteria implements Serializable, Criteria {

    private static final long serialVersionUID = 1L;

    private LongFilter id;

    private StringFilter code;

    private StringFilter ccy;

    private StringFilter nameRu;

    private BigDecimalFilter rate;

    private BigDecimalFilter sellRate;

    private BigDecimalFilter buyRate;

    private LocalDateFilter date;

    private BooleanFilter active;

    private StringFilter ccyGroup;

    public CurrencyCriteria() {}

    public CurrencyCriteria(CurrencyCriteria other) {
        this.id = other.id == null ? null : other.id.copy();
        this.code = other.code == null ? null : other.code.copy();
        this.ccy = other.ccy == null ? null : other.ccy.copy();
        this.nameRu = other.nameRu == null ? null : other.nameRu.copy();
        this.rate = other.rate == null ? null : other.rate.copy();
        this.sellRate = other.sellRate == null ? null : other.sellRate.copy();
        this.buyRate = other.buyRate == null ? null : other.buyRate.copy();
        this.date = other.date == null ? null : other.date.copy();
        this.active = other.active == null ? null : other.active.copy();
        this.ccyGroup = other.ccyGroup == null ? null : other.ccyGroup.copy();
    }

    @Override
    public CurrencyCriteria copy() {
        return new CurrencyCriteria(this);
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

    public StringFilter getCode() {
        return code;
    }

    public StringFilter code() {
        if (code == null) {
            code = new StringFilter();
        }
        return code;
    }

    public void setCode(StringFilter code) {
        this.code = code;
    }

    public StringFilter getCcy() {
        return ccy;
    }

    public StringFilter ccy() {
        if (ccy == null) {
            ccy = new StringFilter();
        }
        return ccy;
    }

    public void setCcy(StringFilter ccy) {
        this.ccy = ccy;
    }

    public StringFilter getNameRu() {
        return nameRu;
    }

    public StringFilter nameRu() {
        if (nameRu == null) {
            nameRu = new StringFilter();
        }
        return nameRu;
    }

    public void setNameRu(StringFilter nameRu) {
        this.nameRu = nameRu;
    }

    public BigDecimalFilter getRate() {
        return rate;
    }

    public BigDecimalFilter rate() {
        if (rate == null) {
            rate = new BigDecimalFilter();
        }
        return rate;
    }

    public void setRate(BigDecimalFilter rate) {
        this.rate = rate;
    }

    public BigDecimalFilter getSellRate() {
        return sellRate;
    }

    public BigDecimalFilter sellRate() {
        if (sellRate == null) {
            sellRate = new BigDecimalFilter();
        }
        return sellRate;
    }

    public void setSellRate(BigDecimalFilter sellRate) {
        this.sellRate = sellRate;
    }

    public BigDecimalFilter getBuyRate() {
        return buyRate;
    }

    public BigDecimalFilter buyRate() {
        if (buyRate == null) {
            buyRate = new BigDecimalFilter();
        }
        return buyRate;
    }

    public void setBuyRate(BigDecimalFilter buyRate) {
        this.buyRate = buyRate;
    }

    public LocalDateFilter getDate() {
        return date;
    }

    public LocalDateFilter date() {
        if (date == null) {
            date = new LocalDateFilter();
        }
        return date;
    }

    public void setDate(LocalDateFilter date) {
        this.date = date;
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

    public StringFilter getCcyGroup() {
        return ccyGroup;
    }

    public void setCcyGroup(StringFilter ccyGroup) {
        this.ccyGroup = ccyGroup;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final CurrencyCriteria that = (CurrencyCriteria) o;
        return (
            Objects.equals(id, that.id) &&
            Objects.equals(code, that.code) &&
            Objects.equals(ccy, that.ccy) &&
            Objects.equals(nameRu, that.nameRu) &&
            Objects.equals(rate, that.rate) &&
            Objects.equals(sellRate, that.sellRate) &&
            Objects.equals(buyRate, that.buyRate) &&
            Objects.equals(date, that.date) &&
            Objects.equals(active, that.active) &&
            Objects.equals(ccyGroup, that.ccyGroup)
        );
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, code, ccy, nameRu, rate, sellRate, buyRate, date, active, ccyGroup);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "CurrencyCriteria{" +
            (id != null ? "id=" + id + ", " : "") +
            (code != null ? "code=" + code + ", " : "") +
            (ccy != null ? "ccy=" + ccy + ", " : "") +
            (nameRu != null ? "nameRu=" + nameRu + ", " : "") +
            (rate != null ? "rate=" + rate + ", " : "") +
            (sellRate != null ? "sellRate=" + sellRate + ", " : "") +
            (buyRate != null ? "buyRate=" + buyRate + ", " : "") +
            (date != null ? "date=" + date + ", " : "") +
            (active != null ? "active=" + active + ", " : "") +
            (ccyGroup != null ? "ccyGroup=" + ccyGroup + ", " : "") +
            "}";
    }
}
