package uz.paykap.service.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.io.Serializable;
import java.io.Serializable;
import java.util.*;
import java.util.Objects;
import javax.validation.constraints.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import uz.devops.bmcore.enumeration.PayType;
import uz.paykap.domain.enumeration.BktPeriod;
import uz.paykap.domain.enumeration.BookkeepingAmountCalculateType;
import uz.paykap.domain.enumeration.BookkeepingPayType;
import uz.paykap.domain.enumeration.BookkeepingTransactionType;

/**
 * A DTO for the {@link uz.paykap.domain.BookkeepingTemplate} entity.
 */
@ApiModel(description = "Данная форма предназначена для создания шаблонов проводок в разрезе групп")
public class BookkeepingTemplateDTO extends AbstractAuditingDTO implements Serializable {

    private Long id;

    /**
     * Группа
     */
    @NotNull
    @ApiModelProperty(value = "Группа", required = true)
    private Long groupId;

    /**
     * МФО Отправителя
     */
    @NotNull
    @Size(max = 8)
    @ApiModelProperty(value = "МФО Отправителя", required = true)
    private String sMfo;

    /**
     * ИНН Отправителя
     */
    @NotNull
    @Size(max = 16)
    @ApiModelProperty(value = "ИНН Отправителя", required = true)
    private String sInn;

    /**
     * Р/С Отправителя
     */
    @NotNull
    @Size(max = 32)
    @ApiModelProperty(value = "Р/С Отправителя", required = true)
    private String sAccount;

    /**
     * МФО Получателя
     */
    @NotNull
    @Size(max = 8)
    @ApiModelProperty(value = "МФО Получателя", required = true)
    private String rMfo;

    /**
     * ИНН Получателя
     */
    @NotNull
    @Size(max = 16)
    @ApiModelProperty(value = "ИНН Получателя", required = true)
    private String rInn;

    /**
     * Р/С Получателя
     */
    @NotNull
    @Size(max = 32)
    @ApiModelProperty(value = "Р/С Получателя", required = true)
    private String rAccount;

    /**
     * Правило расчета суммы, SUM(amount) - SUM(comission) - SUM(fee), ROUND(SUM(comission), 2)
     */
    @NotNull
    @Size(max = 64)
    @ApiModelProperty(value = "Правило расчета суммы, SUM(amount) - SUM(comission) - SUM(fee), ROUND(SUM(comission), 2)", required = true)
    private String calculateAmountRule;

    @NotNull
    private BktPeriod period;

    @NotNull
    @Size(max = 50)
    private Set<PayType> payTypes;

    @NotNull
    @Size(max = 256)
    private String desc;

    @NotNull
    private Long sType;

    @NotNull
    @Size(max = 128)
    private String sTypeIds;

    @Size(max = 8)
    private String docType;

    /**
     * Код платежа
     */
    @Size(max = 16)
    @ApiModelProperty(value = "Код платежа")
    private String docCode;

    /**
     * Код задачи
     */
    @Size(max = 8)
    @ApiModelProperty(value = "Код задачи")
    private String docTaskCode;

    @Size(max = 64)
    private String rAccountName;

    /**
     * Тип платежа проводки : за контракт , акт , поставщику и т.д.
     */
    @ApiModelProperty(value = "Тип платежа проводки : за контракт , акт , поставщику и т.д.")
    private BookkeepingPayType bookkeepingPayType;

    private Long control;

    @NotNull
    private BookkeepingTransactionType bookkeepingType;

    /**
     * Уникальное название схемы бухгалтерского взаиморасчета
     */
    @NotNull
    @Size(max = 64)
    @ApiModelProperty(value = "Уникальное название схемы бухгалтерского взаиморасчета", required = true)
    private String name;

    @Size(max = 256)
    private String groupIds;

    private Long subRecipientId;

    @NotNull
    private BookkeepingAmountCalculateType amountCalculateType;

    private GatewayDTO gateway;

    private RecipientDTO recipient;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getGroupId() {
        return groupId;
    }

    public void setGroupId(Long groupId) {
        this.groupId = groupId;
    }

    public String getsMfo() {
        return sMfo;
    }

    public void setsMfo(String sMfo) {
        this.sMfo = sMfo;
    }

    public String getsInn() {
        return sInn;
    }

    public void setsInn(String sInn) {
        this.sInn = sInn;
    }

    public String getsAccount() {
        return sAccount;
    }

    public void setsAccount(String sAccount) {
        this.sAccount = sAccount;
    }

    public String getrMfo() {
        return rMfo;
    }

    public void setrMfo(String rMfo) {
        this.rMfo = rMfo;
    }

    public String getrInn() {
        return rInn;
    }

    public void setrInn(String rInn) {
        this.rInn = rInn;
    }

    public String getrAccount() {
        return rAccount;
    }

    public void setrAccount(String rAccount) {
        this.rAccount = rAccount;
    }

    public String getCalculateAmountRule() {
        return calculateAmountRule;
    }

    public void setCalculateAmountRule(String calculateAmountRule) {
        this.calculateAmountRule = calculateAmountRule;
    }

    public BktPeriod getPeriod() {
        return period;
    }

    public void setPeriod(BktPeriod period) {
        this.period = period;
    }

    public Set<PayType> getPayTypes() {
        return payTypes;
    }

    public void setPayTypes(Set<PayType> payTypes) {
        this.payTypes = payTypes;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public Long getsType() {
        return sType;
    }

    public void setsType(Long sType) {
        this.sType = sType;
    }

    public String getsTypeIds() {
        return sTypeIds;
    }

    public void setsTypeIds(String sTypeIds) {
        this.sTypeIds = sTypeIds;
    }

    public String getDocType() {
        return docType;
    }

    public void setDocType(String docType) {
        this.docType = docType;
    }

    public String getDocCode() {
        return docCode;
    }

    public void setDocCode(String docCode) {
        this.docCode = docCode;
    }

    public String getDocTaskCode() {
        return docTaskCode;
    }

    public void setDocTaskCode(String docTaskCode) {
        this.docTaskCode = docTaskCode;
    }

    public String getrAccountName() {
        return rAccountName;
    }

    public void setrAccountName(String rAccountName) {
        this.rAccountName = rAccountName;
    }

    public BookkeepingPayType getBookkeepingPayType() {
        return bookkeepingPayType;
    }

    public void setBookkeepingPayType(BookkeepingPayType bookkeepingPayType) {
        this.bookkeepingPayType = bookkeepingPayType;
    }

    public Long getControl() {
        return control;
    }

    public void setControl(Long control) {
        this.control = control;
    }

    public BookkeepingTransactionType getBookkeepingType() {
        return bookkeepingType;
    }

    public void setBookkeepingType(BookkeepingTransactionType bookkeepingType) {
        this.bookkeepingType = bookkeepingType;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGroupIds() {
        return groupIds;
    }

    public void setGroupIds(String groupIds) {
        this.groupIds = groupIds;
    }

    public Long getSubRecipientId() {
        return subRecipientId;
    }

    public void setSubRecipientId(Long subRecipientId) {
        this.subRecipientId = subRecipientId;
    }

    public BookkeepingAmountCalculateType getAmountCalculateType() {
        return amountCalculateType;
    }

    public void setAmountCalculateType(BookkeepingAmountCalculateType amountCalculateType) {
        this.amountCalculateType = amountCalculateType;
    }

    public GatewayDTO getGateway() {
        return gateway;
    }

    public void setGateway(GatewayDTO gateway) {
        this.gateway = gateway;
    }

    public RecipientDTO getRecipient() {
        return recipient;
    }

    public void setRecipient(RecipientDTO recipient) {
        this.recipient = recipient;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof BookkeepingTemplateDTO)) {
            return false;
        }

        BookkeepingTemplateDTO bookkeepingTemplateDTO = (BookkeepingTemplateDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, bookkeepingTemplateDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "BookkeepingTemplateDTO{" +
            "id=" + getId() +
            ", groupId=" + getGroupId() +
            ", sMfo='" + getsMfo() + "'" +
            ", sInn='" + getsInn() + "'" +
            ", sAccount='" + getsAccount() + "'" +
            ", rMfo='" + getrMfo() + "'" +
            ", rInn='" + getrInn() + "'" +
            ", rAccount='" + getrAccount() + "'" +
            ", calculateAmountRule='" + getCalculateAmountRule() + "'" +
            ", period='" + getPeriod() + "'" +
            ", payTypes='" + getPayTypes() + "'" +
            ", desc='" + getDesc() + "'" +
            ", sType=" + getsType() +
            ", sTypeIds='" + getsTypeIds() + "'" +
            ", docType='" + getDocType() + "'" +
            ", docCode='" + getDocCode() + "'" +
            ", docTaskCode='" + getDocTaskCode() + "'" +
            ", rAccountName='" + getrAccountName() + "'" +
            ", bookkeepingPayType='" + getBookkeepingPayType() + "'" +
            ", control=" + getControl() +
            ", bookkeepingType='" + getBookkeepingType() + "'" +
            ", name='" + getName() + "'" +
            ", groupIds='" + getGroupIds() + "'" +
            ", subRecipientId=" + getSubRecipientId() +
            ", amountCalculateType='" + getAmountCalculateType() + "'" +
            ", gateway=" + getGateway() +
            ", recipient=" + getRecipient() +
            "}";
    }
}
