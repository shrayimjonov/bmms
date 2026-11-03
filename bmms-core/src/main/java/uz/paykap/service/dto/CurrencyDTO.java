package uz.paykap.service.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import uz.devops.cbu.service.dto.json.ICurrency;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Objects;

/**
 * A DTO for the {@link uz.paykap.domain.Currency} entity.
 */
@ApiModel(description = "Актуальный курс валюты")
public class CurrencyDTO implements ICurrency, Serializable {

    private Long id;

    /**
     * Числовой код валюты (digit-3) из ISO_4217. Например: 840, 978, 643 и др
     */
    @NotNull
    @Size(min = 3, max = 3)
    @ApiModelProperty(value = "Числовой код валюты (digit-3) из ISO_4217. Например: 840, 978, 643 и др", required = true)
    private String code;

    /**
     * Символьный код валюты (альфа-3) из ISO_4217. Например: USD, EUR, RUB и др
     */
    @NotNull
    @Size(min = 3, max = 3)
    @ApiModelProperty(value = "Символьный код валюты (альфа-3) из ISO_4217. Например: USD, EUR, RUB и др", required = true)
    private String ccy;

    /**
     * Наименование валюты на русском языке
     */
    @Size(max = 64)
    @ApiModelProperty(value = "Наименование валюты на русском языке")
    private String nameRu;

    /**
     * Курс валюты
     */
    @ApiModelProperty(value = "Курс валюты")
    private BigDecimal rate;

    /**
     * Курс валюты продажи
     */
    @ApiModelProperty(value = "Курс валюты продажи")
    private BigDecimal sellRate;

    /**
     * Курс валюты покупки
     */
    @ApiModelProperty(value = "Курс валюты покупки")
    private BigDecimal buyRate;

    /**
     * Дата активизации
     */
    @ApiModelProperty(value = "Дата активизации")
    private LocalDate date;

    /**
     * Activeness
     */
    @NotNull
    @ApiModelProperty(value = "Activeness", required = true)
    private Boolean active;

    @Size(max = 32)
    private String ccyGroup;

    /**
     * Дата обновление
     */
    @ApiModelProperty(value = "Дата обновление")
    private Instant lastModifiedDate;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getCcy() {
        return ccy;
    }

    public void setCcy(String ccy) {
        this.ccy = ccy;
    }

    public String getNameRu() {
        return nameRu;
    }

    public void setNameRu(String nameRu) {
        this.nameRu = nameRu;
    }

    public BigDecimal getRate() {
        return rate;
    }

    public void setRate(BigDecimal rate) {
        this.rate = rate;
    }

    public BigDecimal getSellRate() {
        return sellRate;
    }

    public void setSellRate(BigDecimal sellRate) {
        this.sellRate = sellRate;
    }

    public BigDecimal getBuyRate() {
        return buyRate;
    }

    public void setBuyRate(BigDecimal buyRate) {
        this.buyRate = buyRate;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public Boolean getActive() {
        return active;
    }

    public Boolean isActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public String getCcyGroup() {
        return ccyGroup;
    }

    public void setCcyGroup(String ccyGroup) {
        this.ccyGroup = ccyGroup;
    }

    public Instant getLastModifiedDate() {
        return lastModifiedDate;
    }

    public void setLastModifiedDate(Instant lastModifiedDate) {
        this.lastModifiedDate = lastModifiedDate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof CurrencyDTO)) {
            return false;
        }

        CurrencyDTO currencyDTO = (CurrencyDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, currencyDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "CurrencyDTO{" +
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
            ", lastModifedDate='" + getLastModifiedDate() + "'" +
            "}";
    }
}
