package uz.paykap.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.javers.core.metamodel.annotation.DiffIgnore;
import uz.paykap.domain.converter.SetIntegerConverter;
import uz.paykap.domain.converter.SetStringConverter;
import uz.paykap.domain.enumeration.AgentState;

/**
 * Агент, инициатор оплаты
 */
@Entity
@Table(name = "agent")
public class Agent extends AbstractAuditingEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    private Long id;

    /**
     * Short name to show in UI
     */
    @NotNull
    @Size(max = 64)
    @Column(name = "short_name", length = 64, nullable = false)
    private String shortName;

    /**
     * Доля суммы, 1 - сум, 100 - тийин
     */
    @NotNull
    @Column(name = "amount_ratio", nullable = false)
    private Integer amountRatio;

    /**
     * Agent's api key, comes in request to authorize one
     */
    @NotNull
    @Size(max = 64)
    @Column(name = "api_key", length = 64, nullable = false)
    private String apiKey;

    /**
     * Валюта разрешенных запросов
     */
    @NotNull
    @Size(max = 64)
    @Column(name = "currencies", length = 64, nullable = false)
    @Convert(converter = SetIntegerConverter.class)
    private Set<Integer> currencies;

    /**
     * Ip addresses for filtering
     */
    @Size(max = 32)
    @Column(name = "ip_addresses", length = 32)
    @Convert(converter = SetStringConverter.class)
    private Set<String> ipAddresses;

    /**
     * Logo file path or url
     */
    @DiffIgnore
    @Lob
    @Column(name = "logo")
    private byte[] logo;

    @Column(name = "logo_content_type")
    private String logoContentType;

    /**
     * Display order
     */
    @Column(name = "ord")
    private Integer ord;

    @NotNull
    @Column(name = "active", nullable = false)
    private Boolean active;

    @Size(max = 32)
    @Column(name = "phone_number", length = 32)
    private String phoneNumber;

    /**
     * Tax number (INN, VAT) of payer. see : https://en.wikipedia.org/wiki/VAT_identification_number
     */
    @Size(max = 32)
    @Column(name = "inn", length = 32)
    private String inn;

    @Size(max = 64)
    @Column(name = "org_name", length = 64)
    private String orgName;

    @Size(min = 4, max = 254)
    @Column(name = "email", length = 254, unique = true)
    private String email;

    /**
     * Состояние
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "state")
    private AgentState state;

    @ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @NotNull
    @JoinTable(name = "rel_agent__title", joinColumns = @JoinColumn(name = "agent_id"), inverseJoinColumns = @JoinColumn(name = "title_id"))
    private Set<I18n> titles = new HashSet<>();

    @DiffIgnore
    @JsonIgnoreProperties(value = { "gateway", "agent", "virtualAccount" }, allowSetters = true)
    @OneToOne(mappedBy = "agent")
    private Deposit deposit;

    // jhipster-needle-entity-add-field - JHipster will add fields here
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Agent id(Long id) {
        this.id = id;
        return this;
    }

    public String getShortName() {
        return this.shortName;
    }

    public Agent shortName(String shortName) {
        this.shortName = shortName;
        return this;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    public Integer getAmountRatio() {
        return this.amountRatio;
    }

    public Agent amountRatio(Integer amountRatio) {
        this.amountRatio = amountRatio;
        return this;
    }

    public void setAmountRatio(Integer amountRatio) {
        this.amountRatio = amountRatio;
    }

    public String getApiKey() {
        return this.apiKey;
    }

    public Agent apiKey(String apiKey) {
        this.apiKey = apiKey;
        return this;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public Set<Integer> getCurrencies() {
        return currencies;
    }

    public Agent currencies(Set<Integer> currencies) {
        this.currencies = currencies;
        return this;
    }

    public void setCurrencies(Set<Integer> currencies) {
        this.currencies = currencies;
    }

    public Set<String> getIpAddresses() {
        return ipAddresses;
    }

    public Agent ipAddresses(Set<String> ipAddresses) {
        this.ipAddresses = ipAddresses;
        return this;
    }

    public void setIpAddresses(Set<String> ipAddresses) {
        this.ipAddresses = ipAddresses;
    }

    public byte[] getLogo() {
        return this.logo;
    }

    public Agent logo(byte[] logo) {
        this.logo = logo;
        return this;
    }

    public void setLogo(byte[] logo) {
        this.logo = logo;
    }

    public String getLogoContentType() {
        return this.logoContentType;
    }

    public Agent logoContentType(String logoContentType) {
        this.logoContentType = logoContentType;
        return this;
    }

    public void setLogoContentType(String logoContentType) {
        this.logoContentType = logoContentType;
    }

    public Integer getOrd() {
        return this.ord;
    }

    public Agent ord(Integer ord) {
        this.ord = ord;
        return this;
    }

    public void setOrd(Integer ord) {
        this.ord = ord;
    }

    public Boolean isActive() {
        return this.active;
    }

    public Boolean getActive() {
        return this.active;
    }

    public Agent active(Boolean active) {
        this.active = active;
        return this;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public String getPhoneNumber() {
        return this.phoneNumber;
    }

    public Agent phoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
        return this;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getInn() {
        return this.inn;
    }

    public Agent inn(String inn) {
        this.inn = inn;
        return this;
    }

    public void setInn(String inn) {
        this.inn = inn;
    }

    public String getOrgName() {
        return this.orgName;
    }

    public Agent orgName(String orgName) {
        this.orgName = orgName;
        return this;
    }

    public void setOrgName(String orgName) {
        this.orgName = orgName;
    }

    public String getEmail() {
        return this.email;
    }

    public Agent email(String email) {
        this.email = email;
        return this;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public AgentState getState() {
        return this.state;
    }

    public Agent state(AgentState state) {
        this.state = state;
        return this;
    }

    public void setState(AgentState state) {
        this.state = state;
    }

    public Set<I18n> getTitles() {
        return titles;
    }

    public Agent titles(Set<I18n> i18ns) {
        this.titles = i18ns;
        return this;
    }

    public Agent addTitle(I18n i18n) {
        this.titles.add(i18n);
        return this;
    }

    public Agent removeTitle(I18n i18n) {
        this.titles.remove(i18n);
        return this;
    }

    public void setTitles(Set<I18n> i18ns) {
        this.titles = i18ns;
    }

    public Deposit getDeposit() {
        return this.deposit;
    }

    public Agent deposit(Deposit deposit) {
        this.setDeposit(deposit);
        return this;
    }

    public void setDeposit(Deposit deposit) {
        if (this.deposit != null) {
            this.deposit.setAgent(null);
        }
        if (deposit != null) {
            deposit.setAgent(this);
        }
        this.deposit = deposit;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Agent)) {
            return false;
        }
        return id != null && id.equals(((Agent) o).id);
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "Agent{" +
            "id=" + getId() +
            ", shortName='" + getShortName() + "'" +
            ", amountRatio=" + getAmountRatio() +
            ", apiKey='" + getApiKey() + "'" +
            ", currencies='" + getCurrencies() + "'" +
            ", ipAddresses='" + getIpAddresses() + "'" +
            ", logo='" + getLogo() + "'" +
            ", logoContentType='" + getLogoContentType() + "'" +
            ", ord=" + getOrd() +
            ", active='" + getActive() + "'" +
            ", phoneNumber='" + getPhoneNumber() + "'" +
            ", inn='" + getInn() + "'" +
            ", orgName='" + getOrgName() + "'" +
            ", email='" + getEmail() + "'" +
            ", state='" + getState() + "'" +
            "}";
    }
}
