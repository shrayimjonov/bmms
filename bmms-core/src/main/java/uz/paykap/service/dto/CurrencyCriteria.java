package uz.paykap.service.dto;

import io.github.jhipster.service.filter.BigDecimalFilter;
import io.github.jhipster.service.filter.BooleanFilter;
import io.github.jhipster.service.filter.DoubleFilter;
import io.github.jhipster.service.filter.Filter;
import io.github.jhipster.service.filter.FloatFilter;
import io.github.jhipster.service.filter.IntegerFilter;
import io.github.jhipster.service.filter.LocalDateFilter;
import java.io.Serializable;
import java.util.Objects;
import tech.jhipster.service.filter.LongFilter;
import tech.jhipster.service.filter.StringFilter;

/**
 * Criteria class for the Currency entity. This class is used in CurrencyResource to
 * receive all the possible filtering options from the Http GET request parameters.
 * For example the following could be a valid requests:
 * <code> /currencies?id.greaterThan=5&amp;attr1.contains=something&amp;attr2.specified=false</code>
 * As Spring is unable to properly convert the types, unless specific {@link Filter} class are used, we need to use
 * fix type specific filters.
 */
public class CurrencyCriteria implements Serializable {

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

    public LongFilter getId() {
        return id;
    }

    public void setId(LongFilter id) {
        this.id = id;
    }

    public StringFilter getCode() {
        return code;
    }

    public void setCode(StringFilter code) {
        this.code = code;
    }

    public StringFilter getCcy() {
        return ccy;
    }

    public void setCcy(StringFilter ccy) {
        this.ccy = ccy;
    }

    public StringFilter getNameRu() {
        return nameRu;
    }

    public void setNameRu(StringFilter nameRu) {
        this.nameRu = nameRu;
    }

    public BigDecimalFilter getRate() {
        return rate;
    }

    public void setRate(BigDecimalFilter rate) {
        this.rate = rate;
    }

    public BigDecimalFilter getSellRate() {
        return sellRate;
    }

    public void setSellRate(BigDecimalFilter sellRate) {
        this.sellRate = sellRate;
    }

    public BigDecimalFilter getBuyRate() {
        return buyRate;
    }

    public void setBuyRate(BigDecimalFilter buyRate) {
        this.buyRate = buyRate;
    }

    public LocalDateFilter getDate() {
        return date;
    }

    public void setDate(LocalDateFilter date) {
        this.date = date;
    }

    public BooleanFilter getActive() {
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
            Objects.equals(ccyGroup, that.ccyGroup));
    }

    @Override
    public int hashCode() {
        return Objects.hash(
        id,
        code,
        ccy,
        nameRu,
        rate,
        sellRate,
        buyRate,
        date,
        active,
        ccyGroup
        );
    }

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
