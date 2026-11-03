package uz.paykap.service.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import javax.persistence.Lob;
import javax.validation.constraints.Max;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import uz.devops.bmcore.enumeration.RecipientType;

/**
 * A DTO for the {@link uz.paykap.domain.Recipient} entity.
 */
@NoArgsConstructor
@Getter
@Setter
@ToString
@ApiModel(description = "Получатель денежных средств, поставщик услуг")
public class RecipientDTO extends AbstractAuditingDTO implements Serializable {

    private Long id;

    /**
     * Уникальное название в шлюзе
     */
    @NotNull
    @Size(max = 64)
    @ApiModelProperty(value = "Уникальное название в шлюзе", required = true)
    private String shortName;

    /**
     * Service id, for inner business logic
     */
    @NotNull
    @Size(max = 64)
    @ApiModelProperty(value = "Service id, for inner business logic", required = true)
    private String serviceId;

    /**
     * Thumbnail file path or url
     */
    @Size(max = 64)
    @ApiModelProperty(value = "Thumbnail file path or url")
    private String thumbnail;

    /**
     * Logo file path or url
     */
    @Size(max = 64)
    @ApiModelProperty(value = "Logo file path or url")
    private String logo;

    /**
     * Использумая разрядность суммы в шлюзе получателя, 1 - сум, 100 - тийин
     */
    @NotNull
    @ApiModelProperty(value = "Использумая разрядность суммы в шлюзе получателя, 1 - сум, 100 - тийин", required = true)
    private Integer amountRatio;

    /**
     * Валюта получателя
     */
    @NotNull
    @ApiModelProperty(value = "Валюта получателя", required = true)
    private Integer ccy;

    /**
     * Минимальная сумма платежа, разрядность зависит от recipient.limitRatio
     */
    @ApiModelProperty(value = "Минимальная сумма платежа, разрядность зависит от recipient.limitRatio")
    private Long minAmount;

    /**
     * Максимальная сумма платежа, разрядность зависит от recipient.limitRatio
     */
    @ApiModelProperty(value = "Максимальная сумма платежа, разрядность зависит от recipient.limitRatio")
    private Long maxAmount;

    /**
     * Разрядность макс. и мин. суммы платежа
     */
    @NotNull
    @ApiModelProperty(value = "Разрядность макс. и мин. суммы платежа", required = true)
    private Integer limitRatio;

    /**
     * Комиссия получателя
     */
    @ApiModelProperty(value = "Комиссия получателя")
    private Double commission;

    /**
     * Вознаграждение получателя
     */
    @ApiModelProperty(value = "Вознаграждение получателя")
    private Double fee;

    /**
     * Комиссия системы от получателя
     */
    @ApiModelProperty(value = "Комиссия системы от получателя")
    private Double systemCommission;

    /**
     * Вознаграждение системы от получателя
     */
    @ApiModelProperty(value = "Вознаграждение системы от получателя")
    private Double systemFee;

    /**
     * Вознаграждение поставщику (если участвует как отправитель средств)
     */
    @ApiModelProperty(value = "Вознаграждение поставщику (если участвует как отправитель средств)")
    private Double senderCommission;

    /**
     * Display order
     */
    @ApiModelProperty(value = "Display order")
    private Integer ord;

    @NotNull
    private Boolean active;

    private Boolean check;

    private Boolean pay;

    private Boolean status;

    private Boolean cancel;

    private Boolean canDeposit;

    /**
     * Расчетный счет поставщика
     */
    @Size(max = 32)
    @ApiModelProperty(value = "Расчетный счет поставщика")
    private String settlementAccount;

    /**
     * МФО счет поставщика
     */
    @Size(max = 16)
    @ApiModelProperty(value = "МФО счет поставщика")
    private String mfo;

    /**
     * Recipient phone number
     */
    @ApiModelProperty(value = "Recipient phone number")
    private String phoneNumber;

    /**
     * Инн счет поставщика
     */
    @Size(max = 32)
    @ApiModelProperty(value = "Инн счет поставщика")
    private String inn;

    private Boolean reconcile;

    /**
     * Дополнительные параметры в виде json
     */
    @ApiModelProperty(value = "Дополнительные параметры в виде json")
    private String jsonParams;

    @Getter
    @Setter
    private RecipientType type;

    private GatewayDTO gateway;

    private CategoryDTO category;

    private Set<I18nDTO> titles = new HashSet<>();

    private Set<I18nDTO> fullNames = new HashSet<>();

    public RecipientDTO(Long id, String shortName) {
        this.shortName = shortName;
        this.id = id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof RecipientDTO)) {
            return false;
        }

        RecipientDTO recipientDTO = (RecipientDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, recipientDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
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

    public RecipientDTO withId(Long id) {
        this.id = id;
        return this;
    }

    public RecipientDTO withShortName(String shortName) {
        this.shortName = shortName;
        return this;
    }

    public RecipientDTO withServiceId(String serviceId) {
        this.serviceId = serviceId;
        return this;
    }

    public RecipientDTO withThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
        return this;
    }

    public RecipientDTO withLogo(String logo) {
        this.logo = logo;
        return this;
    }

    public RecipientDTO withAmountRatio(Integer amountRatio) {
        this.amountRatio = amountRatio;
        return this;
    }

    public RecipientDTO withCcy(Integer ccy) {
        this.ccy = ccy;
        return this;
    }

    public RecipientDTO withMinAmount(Long minAmount) {
        this.minAmount = minAmount;
        return this;
    }

    public RecipientDTO withMaxAmount(Long maxAmount) {
        this.maxAmount = maxAmount;
        return this;
    }

    public RecipientDTO withLimitRatio(Integer limitRatio) {
        this.limitRatio = limitRatio;
        return this;
    }

    public RecipientDTO withCommission(Double commission) {
        this.commission = commission;
        return this;
    }

    public RecipientDTO withFee(Double fee) {
        this.fee = fee;
        return this;
    }

    public RecipientDTO withSystemCommission(Double systemCommission) {
        this.systemCommission = systemCommission;
        return this;
    }

    public RecipientDTO withSystemFee(Double systemFee) {
        this.systemFee = systemFee;
        return this;
    }

    public RecipientDTO withSenderCommission(Double senderCommission) {
        this.senderCommission = senderCommission;
        return this;
    }

    public RecipientDTO withOrd(Integer ord) {
        this.ord = ord;
        return this;
    }

    public RecipientDTO withActive(Boolean active) {
        this.active = active;
        return this;
    }

    public RecipientDTO withCheck(Boolean check) {
        this.check = check;
        return this;
    }

    public RecipientDTO withPay(Boolean pay) {
        this.pay = pay;
        return this;
    }

    public RecipientDTO withStatus(Boolean status) {
        this.status = status;
        return this;
    }

    public RecipientDTO withCancel(Boolean cancel) {
        this.cancel = cancel;
        return this;
    }

    public RecipientDTO withCanDeposit(Boolean canDeposit) {
        this.canDeposit = canDeposit;
        return this;
    }

    public RecipientDTO withSettlementAccount(String settlementAccount) {
        this.settlementAccount = settlementAccount;
        return this;
    }

    public RecipientDTO withMfo(String mfo) {
        this.mfo = mfo;
        return this;
    }

    public RecipientDTO withPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
        return this;
    }

    public RecipientDTO withInn(String inn) {
        this.inn = inn;
        return this;
    }

    public RecipientDTO withReconcile(Boolean reconcile) {
        this.reconcile = reconcile;
        return this;
    }

    public RecipientDTO withJsonParams(String jsonParams) {
        this.jsonParams = jsonParams;
        return this;
    }

    public RecipientDTO withType(RecipientType type) {
        this.type = type;
        return this;
    }

    public RecipientDTO withGateway(GatewayDTO gateway) {
        this.gateway = gateway;
        return this;
    }

    public RecipientDTO withCategory(CategoryDTO category) {
        this.category = category;
        return this;
    }

    public RecipientDTO withTitles(Set<I18nDTO> titles) {
        this.titles = titles;
        return this;
    }

    public RecipientDTO withFullNames(Set<I18nDTO> fullNames) {
        this.fullNames = fullNames;
        return this;
    }
}
