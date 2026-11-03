package uz.paykap.domain;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import javax.persistence.*;
import javax.validation.constraints.*;

import org.javers.core.metamodel.annotation.DiffIgnore;
import uz.devops.cbu.service.dto.json.ICurrency;

/**
 * Актуальный курс валюты
 */
@Entity
@Table(name = "currency")
@DiffIgnore
public class Currency extends AbstractAuditingEntity implements ICurrency, Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    private Long id;

    /**
     * Числовой код валюты (digit-3) из ISO_4217. Например: 840, 978, 643 и др
     */
    @NotNull
    @Size(min = 3, max = 3)
    @Column(name = "code", length = 3, nullable = false, unique = true)
    private String code;

    /**
     * Символьный код валюты (альфа-3) из ISO_4217. Например: USD, EUR, RUB и др
     */
    @NotNull
    @Size(min = 3, max = 3)
    @Column(name = "ccy", length = 3, nullable = false, unique = true)
    private String ccy;

    /**
     * Наименование валюты на русском языке
     */
    @Size(max = 64)
    @Column(name = "name_ru", length = 64)
    private String nameRu;

    /**
     * Курс валюты
     */
    @Column(name = "rate", precision = 21, scale = 2)
    private BigDecimal rate;

    /**
     * Курс валюты продажи
     */
    @Column(name = "sell_rate", precision = 21, scale = 2)
    private BigDecimal sellRate;

    /**
     * Курс валюты покупки
     */
    @Column(name = "buy_rate", precision = 21, scale = 2)
    private BigDecimal buyRate;

    /**
     * Дата активизации
     */
    @Column(name = "date")
    private LocalDate date;

    /**
     * Activeness
     */
    @NotNull
    @Column(name = "active", nullable = false)
    private Boolean active;

    @Size(max = 32)
    @Column(name = "ccy_group", length = 32)
    private String ccyGroup;

    // jhipster-needle-entity-add-field - JHipster will add fields here, do not remove
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Currency id(Long id) {
        this.id = id;
        return this;
    }

    public String getCode() {
        return this.code;
    }

    public Currency code(String code) {
        this.code = code;
        return this;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getCcy() {
        return this.ccy;
    }

    public Currency ccy(String ccy) {
        this.ccy = ccy;
        return this;
    }

    public void setCcy(String ccy) {
        this.ccy = ccy;
    }

    public String getNameRu() {
        return this.nameRu;
    }

    public Currency nameRu(String nameRu) {
        this.nameRu = nameRu;
        return this;
    }

    public void setNameRu(String nameRu) {
        this.nameRu = nameRu;
    }

    public BigDecimal getRate() {
        return this.rate;
    }

    public Currency rate(BigDecimal rate) {
        this.rate = rate;
        return this;
    }

    public void setRate(BigDecimal rate) {
        this.rate = rate;
    }

    public BigDecimal getSellRate() {
        return this.sellRate;
    }

    public Currency sellRate(BigDecimal sellRate) {
        this.sellRate = sellRate;
        return this;
    }

    public void setSellRate(BigDecimal sellRate) {
        this.sellRate = sellRate;
    }

    public BigDecimal getBuyRate() {
        return this.buyRate;
    }

    public Currency buyRate(BigDecimal buyRate) {
        this.buyRate = buyRate;
        return this;
    }

    public void setBuyRate(BigDecimal buyRate) {
        this.buyRate = buyRate;
    }

    public LocalDate getDate() {
        return this.date;
    }

    public Currency date(LocalDate date) {
        this.date = date;
        return this;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public Boolean isActive() {
        return this.active;
    }

    public Boolean getActive() {
        return this.active;
    }

    public Currency active(Boolean active) {
        this.active = active;
        return this;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public String getCcyGroup() {
        return ccyGroup;
    }

    public Currency ccyGroup(String ccyGroup) {
        this.ccyGroup = ccyGroup;
        return this;
    }

    public void setCcyGroup(String ccyGroup) {
        this.ccyGroup = ccyGroup;
    }
    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here, do not remove

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Currency)) {
            return false;
        }
        return id != null && id.equals(((Currency) o).id);
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "Currency{" +
            "id=" + getId() +
            ", code='" + getCode() + "'" +
            ", ccy='" + getCcy() + "'" +
            ", nameRu='" + getNameRu() + "'" +
            ", rate=" + getRate() +
            ", sellRate=" + getSellRate() +
            ", buyRate=" + getBuyRate() +
            ", date='" + getDate() + "'" +
            ", active='" + isActive() + "'" +
            ", ccyGroup='" + getCcyGroup() + "'" +
            "}";
    }
}
