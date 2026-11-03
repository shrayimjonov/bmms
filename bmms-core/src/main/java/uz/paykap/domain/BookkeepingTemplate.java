package uz.paykap.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.io.Serializable;
import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import javax.persistence.*;
import javax.persistence.*;
import javax.validation.constraints.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import uz.devops.bmcore.enumeration.PayType;
import uz.paykap.domain.converter.ListPayTypeConverter;
import uz.paykap.domain.converter.SetPayTypeConverter;
import uz.paykap.domain.enumeration.BktPeriod;
import uz.paykap.domain.enumeration.BookkeepingAmountCalculateType;
import uz.paykap.domain.enumeration.BookkeepingPayType;
import uz.paykap.domain.enumeration.BookkeepingTransactionType;

/**
 * Данная форма предназначена для создания шаблонов проводок в разрезе групп
 */
@Entity
@Table(name = "bookkeeping_template")
public class BookkeepingTemplate extends AbstractAuditingEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    private Long id;

    /**
     * Группа
     */
    @NotNull
    @Column(name = "group_id", nullable = false)
    private Long groupId;

    /**
     * МФО Отправителя
     */
    @NotNull
    @Size(max = 8)
    @Column(name = "s_mfo", length = 8, nullable = false)
    private String sMfo;

    /**
     * ИНН Отправителя
     */
    @NotNull
    @Size(max = 16)
    @Column(name = "s_inn", length = 16, nullable = false)
    private String sInn;

    /**
     * Р/С Отправителя
     */
    @NotNull
    @Size(max = 32)
    @Column(name = "s_account", length = 32, nullable = false)
    private String sAccount;

    /**
     * МФО Получателя
     */
    @NotNull
    @Size(max = 8)
    @Column(name = "r_mfo", length = 8, nullable = false)
    private String rMfo;

    /**
     * ИНН Получателя
     */
    @NotNull
    @Size(max = 16)
    @Column(name = "r_inn", length = 16, nullable = false)
    private String rInn;

    /**
     * Р/С Получателя
     */
    @NotNull
    @Size(max = 32)
    @Column(name = "r_account", length = 32, nullable = false)
    private String rAccount;

    /**
     * Правило расчета суммы, SUM(amount) - SUM(comission) - SUM(fee), ROUND(SUM(comission), 2)
     */
    @NotNull
    @Size(max = 64)
    @Column(name = "calculate_amount_rule", length = 64, nullable = false)
    private String calculateAmountRule;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "period", nullable = false)
    private BktPeriod period;

    @NotNull
    @Size(max = 50)
    @Column(name = "pay_types", length = 50, nullable = false)
    @Convert(converter = SetPayTypeConverter.class)
    private HashSet<PayType> payTypes;

    @NotNull
    @Size(max = 256)
    @Column(name = "jhi_desc", length = 256, nullable = false)
    private String desc;

    @NotNull
    @Column(name = "s_type", nullable = false)
    private Long sType;

    @NotNull
    @Size(max = 128)
    @Column(name = "s_type_ids", length = 128, nullable = false)
    private String sTypeIds;

    @Size(max = 8)
    @Column(name = "doc_type", length = 8)
    private String docType;

    /**
     * Код платежа
     */
    @Size(max = 16)
    @Column(name = "doc_code", length = 16)
    private String docCode;

    /**
     * Код задачи
     */
    @Size(max = 8)
    @Column(name = "doc_task_code", length = 8)
    private String docTaskCode;

    @Size(max = 64)
    @Column(name = "r_account_name", length = 64)
    private String rAccountName;

    /**
     * Тип платежа проводки : за контракт , акт , поставщику и т.д.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "bookkeeping_pay_type")
    private BookkeepingPayType bookkeepingPayType;

    @Column(name = "control")
    private Long control;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "bookkeeping_type", nullable = false)
    private BookkeepingTransactionType bookkeepingType;

    /**
     * Уникальное название схемы бухгалтерского взаиморасчета
     */
    @NotNull
    @Size(max = 64)
    @Column(name = "name", length = 64, nullable = false)
    private String name;

    @Size(max = 256)
    @Column(name = "group_ids", length = 256)
    private String groupIds;

    @Column(name = "sub_recipient_id")
    private Long subRecipientId;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "amount_calculate_type", nullable = false)
    private BookkeepingAmountCalculateType amountCalculateType;

    @ManyToOne
    @JsonIgnoreProperties(value = { "deposit" }, allowSetters = true)
    private Gateway gateway;

    @ManyToOne
    @JsonIgnoreProperties(value = { "gateway", "category", "titles", "fullNames" }, allowSetters = true)
    private Recipient recipient;

    // jhipster-needle-entity-add-field - JHipster will add fields here
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public BookkeepingTemplate id(Long id) {
        this.id = id;
        return this;
    }

    public Long getGroupId() {
        return this.groupId;
    }

    public BookkeepingTemplate groupId(Long groupId) {
        this.groupId = groupId;
        return this;
    }

    public void setGroupId(Long groupId) {
        this.groupId = groupId;
    }

    public String getsMfo() {
        return this.sMfo;
    }

    public BookkeepingTemplate sMfo(String sMfo) {
        this.sMfo = sMfo;
        return this;
    }

    public void setsMfo(String sMfo) {
        this.sMfo = sMfo;
    }

    public String getsInn() {
        return this.sInn;
    }

    public BookkeepingTemplate sInn(String sInn) {
        this.sInn = sInn;
        return this;
    }

    public void setsInn(String sInn) {
        this.sInn = sInn;
    }

    public String getsAccount() {
        return this.sAccount;
    }

    public BookkeepingTemplate sAccount(String sAccount) {
        this.sAccount = sAccount;
        return this;
    }

    public void setsAccount(String sAccount) {
        this.sAccount = sAccount;
    }

    public String getrMfo() {
        return this.rMfo;
    }

    public BookkeepingTemplate rMfo(String rMfo) {
        this.rMfo = rMfo;
        return this;
    }

    public void setrMfo(String rMfo) {
        this.rMfo = rMfo;
    }

    public String getrInn() {
        return this.rInn;
    }

    public BookkeepingTemplate rInn(String rInn) {
        this.rInn = rInn;
        return this;
    }

    public void setrInn(String rInn) {
        this.rInn = rInn;
    }

    public String getrAccount() {
        return this.rAccount;
    }

    public BookkeepingTemplate rAccount(String rAccount) {
        this.rAccount = rAccount;
        return this;
    }

    public void setrAccount(String rAccount) {
        this.rAccount = rAccount;
    }

    public String getCalculateAmountRule() {
        return this.calculateAmountRule;
    }

    public BookkeepingTemplate calculateAmountRule(String calculateAmountRule) {
        this.calculateAmountRule = calculateAmountRule;
        return this;
    }

    public void setCalculateAmountRule(String calculateAmountRule) {
        this.calculateAmountRule = calculateAmountRule;
    }

    public BktPeriod getPeriod() {
        return this.period;
    }

    public BookkeepingTemplate period(BktPeriod period) {
        this.period = period;
        return this;
    }

    public void setPeriod(BktPeriod period) {
        this.period = period;
    }

    public HashSet<PayType> getPayTypes() {
        return payTypes;
    }

    public BookkeepingTemplate payTypes(HashSet<PayType> payTypes) {
        this.payTypes = payTypes;
        return this;
    }

    public void setPayTypes(HashSet<PayType> payTypes) {
        this.payTypes = payTypes;
    }

    public String getDesc() {
        return this.desc;
    }

    public BookkeepingTemplate desc(String desc) {
        this.desc = desc;
        return this;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public Long getsType() {
        return this.sType;
    }

    public BookkeepingTemplate sType(Long sType) {
        this.sType = sType;
        return this;
    }

    public void setsType(Long sType) {
        this.sType = sType;
    }

    public String getsTypeIds() {
        return this.sTypeIds;
    }

    public BookkeepingTemplate sTypeIds(String sTypeIds) {
        this.sTypeIds = sTypeIds;
        return this;
    }

    public void setsTypeIds(String sTypeIds) {
        this.sTypeIds = sTypeIds;
    }

    public String getDocType() {
        return this.docType;
    }

    public BookkeepingTemplate docType(String docType) {
        this.docType = docType;
        return this;
    }

    public void setDocType(String docType) {
        this.docType = docType;
    }

    public String getDocCode() {
        return this.docCode;
    }

    public BookkeepingTemplate docCode(String docCode) {
        this.docCode = docCode;
        return this;
    }

    public void setDocCode(String docCode) {
        this.docCode = docCode;
    }

    public String getDocTaskCode() {
        return this.docTaskCode;
    }

    public BookkeepingTemplate docTaskCode(String docTaskCode) {
        this.docTaskCode = docTaskCode;
        return this;
    }

    public void setDocTaskCode(String docTaskCode) {
        this.docTaskCode = docTaskCode;
    }

    public String getrAccountName() {
        return this.rAccountName;
    }

    public BookkeepingTemplate rAccountName(String rAccountName) {
        this.rAccountName = rAccountName;
        return this;
    }

    public void setrAccountName(String rAccountName) {
        this.rAccountName = rAccountName;
    }

    public BookkeepingPayType getBookkeepingPayType() {
        return this.bookkeepingPayType;
    }

    public BookkeepingTemplate bookkeepingPayType(BookkeepingPayType bookkeepingPayType) {
        this.bookkeepingPayType = bookkeepingPayType;
        return this;
    }

    public void setBookkeepingPayType(BookkeepingPayType bookkeepingPayType) {
        this.bookkeepingPayType = bookkeepingPayType;
    }

    public Long getControl() {
        return this.control;
    }

    public BookkeepingTemplate control(Long control) {
        this.control = control;
        return this;
    }

    public void setControl(Long control) {
        this.control = control;
    }

    public BookkeepingTransactionType getBookkeepingType() {
        return this.bookkeepingType;
    }

    public BookkeepingTemplate bookkeepingType(BookkeepingTransactionType bookkeepingType) {
        this.bookkeepingType = bookkeepingType;
        return this;
    }

    public void setBookkeepingType(BookkeepingTransactionType bookkeepingType) {
        this.bookkeepingType = bookkeepingType;
    }

    public String getName() {
        return this.name;
    }

    public BookkeepingTemplate name(String name) {
        this.name = name;
        return this;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGroupIds() {
        return this.groupIds;
    }

    public BookkeepingTemplate groupIds(String groupIds) {
        this.groupIds = groupIds;
        return this;
    }

    public void setGroupIds(String groupIds) {
        this.groupIds = groupIds;
    }

    public Long getSubRecipientId() {
        return this.subRecipientId;
    }

    public BookkeepingTemplate subRecipientId(Long subRecipientId) {
        this.subRecipientId = subRecipientId;
        return this;
    }

    public void setSubRecipientId(Long subRecipientId) {
        this.subRecipientId = subRecipientId;
    }

    public BookkeepingAmountCalculateType getAmountCalculateType() {
        return this.amountCalculateType;
    }

    public BookkeepingTemplate amountCalculateType(BookkeepingAmountCalculateType amountCalculateType) {
        this.amountCalculateType = amountCalculateType;
        return this;
    }

    public void setAmountCalculateType(BookkeepingAmountCalculateType amountCalculateType) {
        this.amountCalculateType = amountCalculateType;
    }

    public Gateway getGateway() {
        return this.gateway;
    }

    public BookkeepingTemplate gateway(Gateway gateway) {
        this.setGateway(gateway);
        return this;
    }

    public void setGateway(Gateway gateway) {
        this.gateway = gateway;
    }

    public Recipient getRecipient() {
        return this.recipient;
    }

    public BookkeepingTemplate recipient(Recipient recipient) {
        this.setRecipient(recipient);
        return this;
    }

    public void setRecipient(Recipient recipient) {
        this.recipient = recipient;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof BookkeepingTemplate)) {
            return false;
        }
        return id != null && id.equals(((BookkeepingTemplate) o).id);
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "BookkeepingTemplate{" +
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
            "}";
    }
}
