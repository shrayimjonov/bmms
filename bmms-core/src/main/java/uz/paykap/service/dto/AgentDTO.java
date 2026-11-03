package uz.paykap.service.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import javax.persistence.Lob;
import javax.validation.constraints.*;

import uz.paykap.domain.enumeration.AgentState;

/**
 * A DTO for the {@link uz.paykap.domain.Agent} entity.
 */
@ApiModel(description = "Агент, инициатор оплаты")
public class AgentDTO extends AbstractAuditingDTO implements Serializable {

    private Long id;

    /**
     * Short name to show in UI
     */
    @NotNull
    @Size(max = 64)
    @ApiModelProperty(value = "Short name to show in UI", required = true)
    private String shortName;

    /**
     * Доля суммы, 1 - сум, 100 - тийин
     */
    @NotNull
    @ApiModelProperty(value = "Доля суммы, 1 - сум, 100 - тийин", required = true)
    private Integer amountRatio;

    /**
     * Agent's api key, comes in request to authorize one
     */
    @NotNull
    @Size(max = 64)
    @ApiModelProperty(value = "Agent's api key, comes in request to authorize one", required = true)
    private String apiKey;

    /**
     * Валюта разрешенных запросов
     */
    @NotNull
    @Size(max = 64)
    @ApiModelProperty(value = "Валюта разрешенных запросов", required = true)
    private Set<Integer> currencies;

    /**
     * Ip addresses for filtering
     */
    @Size(max = 32)
    @ApiModelProperty(value = "Ip addresses for filtering")
    private Set<String> ipAddresses;

    /**
     * Logo file path or url
     */
    @ApiModelProperty(value = "Logo file path or url")
    @Lob
    private byte[] logo;

    private String logoContentType;

    /**
     * Display order
     */
    @ApiModelProperty(value = "Display order")
    private Integer ord;

    @NotNull
    private Boolean active;

    @Size(max = 32)
    private String phoneNumber;

    /**
     * Tax number (INN, VAT) of payer. see : https://en.wikipedia.org/wiki/VAT_identification_number
     */
    @Size(max = 32)
    @ApiModelProperty(value = "Tax number (INN, VAT) of payer. see : https://en.wikipedia.org/wiki/VAT_identification_number")
    private String inn;

    @Size(max = 64)
    private String orgName;

    @Size(min = 4, max = 254)
    private String email;

    /**
     * Состояние
     */
    @ApiModelProperty(value = "Состояние")
    private AgentState state;

    private Set<I18nDTO> titles = new HashSet<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    public Integer getAmountRatio() {
        return amountRatio;
    }

    public void setAmountRatio(Integer amountRatio) {
        this.amountRatio = amountRatio;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public Set<Integer> getCurrencies() {
        return currencies;
    }

    public void setCurrencies(Set<Integer> currencies) {
        this.currencies = currencies;
    }

    public Set<String> getIpAddresses() {
        return ipAddresses;
    }

    public void setIpAddresses(Set<String> ipAddresses) {
        this.ipAddresses = ipAddresses;
    }

    public byte[] getLogo() {
        return logo;
    }

    public void setLogo(byte[] logo) {
        this.logo = logo;
    }

    public String getLogoContentType() {
        return logoContentType;
    }

    public void setLogoContentType(String logoContentType) {
        this.logoContentType = logoContentType;
    }

    public Integer getOrd() {
        return ord;
    }

    public void setOrd(Integer ord) {
        this.ord = ord;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getInn() {
        return inn;
    }

    public void setInn(String inn) {
        this.inn = inn;
    }

    public String getOrgName() {
        return orgName;
    }

    public void setOrgName(String orgName) {
        this.orgName = orgName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public AgentState getState() {
        return state;
    }

    public void setState(AgentState state) {
        this.state = state;
    }

    public Set<I18nDTO> getTitles() {
        return titles;
    }

    public void setTitles(Set<I18nDTO> titles) {
        this.titles = titles;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof AgentDTO)) {
            return false;
        }

        AgentDTO agentDTO = (AgentDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, agentDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "AgentDTO{" +
            "id=" + getId() +
            ", shortName='" + getShortName() + "'" +
            ", amountRatio=" + getAmountRatio() +
            ", apiKey='" + getApiKey() + "'" +
            ", currencies='" + getCurrencies() + "'" +
            ", ipAddresses='" + getIpAddresses() + "'" +
            ", logo='" + getLogo() + "'" +
            ", ord=" + getOrd() +
            ", active='" + getActive() + "'" +
            ", phoneNumber='" + getPhoneNumber() + "'" +
            ", inn='" + getInn() + "'" +
            ", orgName='" + getOrgName() + "'" +
            ", email='" + getEmail() + "'" +
            ", state='" + getState() + "'" +
            ", titles=" + getTitles() +
            "}";
    }
}
