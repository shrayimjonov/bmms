package uz.paykap.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.*;
import javax.validation.constraints.*;
import uz.devops.bmcore.enumeration.Lang;
import uz.devops.bmcore.enumeration.RecipientType;

/**
 * Получатель денежных средств, поставщик услуг
 */
@Entity
@Table(name = "recipient")
public class Recipient extends AbstractAuditingEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    private Long id;

    /**
     * Уникальное название в шлюзе
     */
    @NotNull
    @Size(max = 64)
    @Column(name = "short_name", length = 64, nullable = false)
    private String shortName;

    /**
     * Service id, for inner business logic
     */
    @NotNull
    @Size(max = 64)
    @Column(name = "service_id", length = 64, nullable = false)
    private String serviceId;

    /**
     * Thumbnail file path or url
     */
    @Size(max = 64)
    @Column(name = "thumbnail", length = 64)
    private String thumbnail;

    /**
     * Logo file path or url
     */
    @Size(max = 64)
    @Column(name = "logo", length = 64)
    private String logo;

    /**
     * Используемая разрядность суммы в шлюзе получателя, 1 - сум, 100 - тийин
     */
    @NotNull
    @Column(name = "amount_ratio", nullable = false)
    private Integer amountRatio;

    /**
     * Валюта получателя
     */
    @NotNull
    @Column(name = "ccy", nullable = false)
    private Integer ccy;

    /**
     * Минимальная сумма платежа, разрядность зависит от recipient.limitRatio
     */
    @Column(name = "min_amount")
    private Long minAmount;

    /**
     * Максимальная сумма платежа, разрядность зависит от recipient.limitRatio
     */
    @Column(name = "max_amount")
    private Long maxAmount;

    /**
     * Разрядность макс. и мин. суммы платежа
     */
    @NotNull
    @Column(name = "limit_ratio", nullable = false)
    private Integer limitRatio;

    /**
     * Комиссия получателя
     */
    @Column(name = "commission")
    private Double commission;

    /**
     * Вознаграждение получателя
     */
    @Column(name = "fee")
    private Double fee;

    /**
     * Комиссия системы от получателя
     */
    @Column(name = "system_commission")
    private Double systemCommission;

    /**
     * Вознаграждение системы от получателя
     */
    @Column(name = "system_fee")
    private Double systemFee;

    /**
     * Вознаграждение поставщику (если участвует как отправитель средств)
     */
    @Column(name = "sender_commission")
    private Double senderCommission;

    /**
     * Display order
     */
    @Column(name = "ord")
    private Integer ord;

    @NotNull
    @Column(name = "active", nullable = false)
    private Boolean active;

    @Column(name = "jhi_check")
    private Boolean check;

    @Column(name = "pay")
    private Boolean pay;

    @Column(name = "status")
    private Boolean status;

    @Column(name = "cancel")
    private Boolean cancel;

    @Column(name = "can_deposit")
    private Boolean canDeposit;

    /**
     * Расчетный счет поставщика
     */
    @Size(max = 32)
    @Column(name = "settlement_account", length = 32)
    private String settlementAccount;

    /**
     * МФО счет поставщика
     */
    @Size(max = 16)
    @Column(name = "mfo", length = 16)
    private String mfo;

    /**
     * Инн счет поставщика
     */
    @Size(max = 32)
    @Column(name = "inn", length = 32)
    private String inn;

    @Enumerated(EnumType.STRING)
    @Column(name = "jhi_type")
    private RecipientType type;

    @Column(name = "reconcile")
    private Boolean reconcile;

    /**
     * Дополнительные параметры в виде json
     */
    @Lob
    @Column(name = "json_params")
    private String jsonParams;

    @ManyToOne
    @JsonIgnoreProperties(value = { "deposit" }, allowSetters = true)
    private Gateway gateway;

    @ManyToOne
    @JsonIgnoreProperties(value = { "parent", "names" }, allowSetters = true)
    private Category category;

    @ManyToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE }, fetch = FetchType.EAGER)
    @NotNull
    @JoinTable(
        name = "rel_recipient__title",
        joinColumns = @JoinColumn(name = "recipient_id"),
        inverseJoinColumns = @JoinColumn(name = "title_id")
    )
    private Set<I18n> titles = new HashSet<>();

    @ManyToMany(cascade = { CascadeType.ALL }, fetch = FetchType.EAGER)
    @NotNull
    @JoinTable(
        name = "rel_recipient__full_name",
        joinColumns = @JoinColumn(name = "recipient_id"),
        inverseJoinColumns = @JoinColumn(name = "full_name_id")
    )
    private Set<I18n> fullNames = new HashSet<>();

    // jhipster-needle-entity-add-field - JHipster will add fields here
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Recipient id(Long id) {
        this.id = id;
        return this;
    }

    public Boolean isActive() {
        return active;
    }

    public Boolean isCheck() {
        return check;
    }

    public Boolean isPay() {
        return pay;
    }

    public Boolean isStatus() {
        return status;
    }

    public Boolean isCancel() {
        return cancel;
    }

    public Boolean isCanDeposit() {
        return canDeposit;
    }

    public Boolean isReconcile() {
        return reconcile;
    }

    public String getShortName() {
        return this.shortName;
    }

    public Recipient shortName(String shortName) {
        this.shortName = shortName;
        return this;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    public String getServiceId() {
        return this.serviceId;
    }

    public Recipient serviceId(String serviceId) {
        this.serviceId = serviceId;
        return this;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    public String getThumbnail() {
        return this.thumbnail;
    }

    public Recipient thumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
        return this;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }

    public String getLogo() {
        return this.logo;
    }

    public Recipient logo(String logo) {
        this.logo = logo;
        return this;
    }

    public void setLogo(String logo) {
        this.logo = logo;
    }

    public Integer getAmountRatio() {
        return this.amountRatio;
    }

    public Recipient amountRatio(Integer amountRatio) {
        this.amountRatio = amountRatio;
        return this;
    }

    public void setAmountRatio(Integer amountRatio) {
        this.amountRatio = amountRatio;
    }

    public Integer getCcy() {
        return this.ccy;
    }

    public Recipient ccy(Integer ccy) {
        this.ccy = ccy;
        return this;
    }

    public void setCcy(Integer ccy) {
        this.ccy = ccy;
    }

    public Long getMinAmount() {
        return this.minAmount;
    }

    public Recipient minAmount(Long minAmount) {
        this.minAmount = minAmount;
        return this;
    }

    public void setMinAmount(Long minAmount) {
        this.minAmount = minAmount;
    }

    public Long getMaxAmount() {
        return this.maxAmount;
    }

    public Recipient maxAmount(Long maxAmount) {
        this.maxAmount = maxAmount;
        return this;
    }

    public void setMaxAmount(Long maxAmount) {
        this.maxAmount = maxAmount;
    }

    public Integer getLimitRatio() {
        return this.limitRatio;
    }

    public Recipient limitRatio(Integer limitRatio) {
        this.limitRatio = limitRatio;
        return this;
    }

    public void setLimitRatio(Integer limitRatio) {
        this.limitRatio = limitRatio;
    }

    public Double getCommission() {
        return this.commission;
    }

    public Recipient commission(Double commission) {
        this.commission = commission;
        return this;
    }

    public void setCommission(Double commission) {
        this.commission = commission;
    }

    public Double getFee() {
        return this.fee;
    }

    public Recipient fee(Double fee) {
        this.fee = fee;
        return this;
    }

    public void setFee(Double fee) {
        this.fee = fee;
    }

    public Double getSystemCommission() {
        return this.systemCommission;
    }

    public Recipient systemCommission(Double systemCommission) {
        this.systemCommission = systemCommission;
        return this;
    }

    public void setSystemCommission(Double systemCommission) {
        this.systemCommission = systemCommission;
    }

    public Double getSystemFee() {
        return this.systemFee;
    }

    public Recipient systemFee(Double systemFee) {
        this.systemFee = systemFee;
        return this;
    }

    public void setSystemFee(Double systemFee) {
        this.systemFee = systemFee;
    }

    public Double getSenderCommission() {
        return this.senderCommission;
    }

    public Recipient senderCommission(Double senderCommission) {
        this.senderCommission = senderCommission;
        return this;
    }

    public void setSenderCommission(Double senderCommission) {
        this.senderCommission = senderCommission;
    }

    public Integer getOrd() {
        return this.ord;
    }

    public Recipient ord(Integer ord) {
        this.ord = ord;
        return this;
    }

    public void setOrd(Integer ord) {
        this.ord = ord;
    }

    public Boolean getActive() {
        return this.active;
    }

    public Recipient active(Boolean active) {
        this.active = active;
        return this;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public Boolean getCheck() {
        return this.check;
    }

    public Recipient check(Boolean check) {
        this.check = check;
        return this;
    }

    public void setCheck(Boolean check) {
        this.check = check;
    }

    public Boolean getPay() {
        return this.pay;
    }

    public Recipient pay(Boolean pay) {
        this.pay = pay;
        return this;
    }

    public void setPay(Boolean pay) {
        this.pay = pay;
    }

    public Boolean getStatus() {
        return this.status;
    }

    public Recipient status(Boolean status) {
        this.status = status;
        return this;
    }

    public void setStatus(Boolean status) {
        this.status = status;
    }

    public Boolean getCancel() {
        return this.cancel;
    }

    public Recipient cancel(Boolean cancel) {
        this.cancel = cancel;
        return this;
    }

    public void setCancel(Boolean cancel) {
        this.cancel = cancel;
    }

    public Boolean getCanDeposit() {
        return this.canDeposit;
    }

    public Recipient canDeposit(Boolean canDeposit) {
        this.canDeposit = canDeposit;
        return this;
    }

    public void setCanDeposit(Boolean canDeposit) {
        this.canDeposit = canDeposit;
    }

    public String getSettlementAccount() {
        return this.settlementAccount;
    }

    public Recipient settlementAccount(String settlementAccount) {
        this.settlementAccount = settlementAccount;
        return this;
    }

    public void setSettlementAccount(String settlementAccount) {
        this.settlementAccount = settlementAccount;
    }

    public String getMfo() {
        return this.mfo;
    }

    public Recipient mfo(String mfo) {
        this.mfo = mfo;
        return this;
    }

    public void setMfo(String mfo) {
        this.mfo = mfo;
    }

    public String getInn() {
        return this.inn;
    }

    public Recipient inn(String inn) {
        this.inn = inn;
        return this;
    }

    public void setInn(String inn) {
        this.inn = inn;
    }

    public RecipientType getType() {
        return type;
    }

    public Recipient type(RecipientType type) {
        this.type = type;
        return this;
    }

    public void setType(RecipientType type) {
        this.type = type;
    }

    public Boolean getReconcile() {
        return this.reconcile;
    }

    public Recipient reconcile(Boolean reconcile) {
        this.reconcile = reconcile;
        return this;
    }

    public void setReconcile(Boolean reconcile) {
        this.reconcile = reconcile;
    }

    public String getJsonParams() {
        return jsonParams;
    }

    public void setJsonParams(String jsonParams) {
        this.jsonParams = jsonParams;
    }

    public Recipient jsonParams(String jsonParams) {
        this.setJsonParams(jsonParams);
        return this;
    }

    public Gateway getGateway() {
        return this.gateway;
    }

    public Recipient gateway(Gateway gateway) {
        this.setGateway(gateway);
        return this;
    }

    public void setGateway(Gateway gateway) {
        this.gateway = gateway;
    }

    public Category getCategory() {
        return this.category;
    }

    public Recipient category(Category category) {
        this.setCategory(category);
        return this;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public Set<I18n> getTitles() {
        return this.titles;
    }

    public Recipient titles(Set<I18n> i18ns) {
        this.setTitles(i18ns);
        return this;
    }

    public Recipient addTitle(Lang lang, String label) {
        I18n e = new I18n();
        e.setLang(lang);
        e.setText(label);
        this.titles.add(e);
        return this;
    }

    public Recipient addTitle(I18n i18n) {
        this.titles.add(i18n);
        return this;
    }

    public Recipient removeTitle(I18n i18n) {
        this.titles.remove(i18n);
        return this;
    }

    public void setTitles(Set<I18n> i18ns) {
        this.titles = i18ns;
    }

    public Set<I18n> getFullNames() {
        return this.fullNames;
    }

    public Recipient addFullName(Lang lang, String label) {
        I18n e = new I18n();
        e.setLang(lang);
        e.setText(label);
        this.fullNames.add(e);
        return this;
    }

    public Recipient fullNames(Set<I18n> i18ns) {
        this.setFullNames(i18ns);
        return this;
    }

    public Recipient addFullName(I18n i18n) {
        this.fullNames.add(i18n);
        return this;
    }

    public Recipient removeFullName(I18n i18n) {
        this.fullNames.remove(i18n);
        return this;
    }

    public void setFullNames(Set<I18n> i18ns) {
        this.fullNames = i18ns;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Recipient)) {
            return false;
        }
        return id != null && id.equals(((Recipient) o).id);
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "Recipient{" +
            "id=" + getId() +
            ", shortName='" + getShortName() + "'" +
            ", serviceId='" + getServiceId() + "'" +
//            ", thumbnail='" + getThumbnail() + "'" +
            ", logo='" + getLogo() + "'" +
            ", amountRatio=" + getAmountRatio() +
            ", ccy=" + getCcy() +
            ", minAmount=" + getMinAmount() +
            ", maxAmount=" + getMaxAmount() +
            ", limitRatio=" + getLimitRatio() +
            ", commission=" + getCommission() +
            ", fee=" + getFee() +
            ", systemCommission=" + getSystemCommission() +
            ", systemFee=" + getSystemFee() +
            ", senderCommission=" + getSenderCommission() +
            ", ord=" + getOrd() +
            ", active='" + getActive() + "'" +
            ", check='" + getCheck() + "'" +
            ", pay='" + getPay() + "'" +
            ", status='" + getStatus() + "'" +
            ", cancel='" + getCancel() + "'" +
            ", canDeposit='" + getCanDeposit() + "'" +
//            ", eposPort=" + getEposPort() +
//            ", eposMerchant=" + getEposMerchant() +
//            ", eposTerminal=" + getEposTerminal() +
            ", settlementAccount='" + getSettlementAccount() + "'" +
            ", mfo='" + getMfo() + "'" +
            ", inn='" + getInn() + "'" +
            ", reconcile='" + isReconcile() + "'" +
            ", type='" + getType() + "'" +
            "}";
    }
}
