package uz.paykap.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.io.Serializable;
import java.time.Instant;
import javax.persistence.*;
import javax.validation.constraints.*;
import uz.paykap.domain.enumeration.BktPeriod;
import uz.paykap.domain.enumeration.BookkeepingPayType;
import uz.paykap.domain.enumeration.BookkeepingTransactionStatus;
import uz.paykap.domain.enumeration.BookkeepingTransactionType;

/**
 * Данная форма предназначена для создания проводок в разрезе групп
 */
@Entity
@Table(name = "bookkeeping_transaction")
public class BookkeepingTransaction extends AbstractAuditingEntity implements Serializable {

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
     * Сумма на отправку
     */
    @NotNull
    @Size(max = 64)
    @Column(name = "amount", length = 64, nullable = false)
    private String amount;

    /**
     * Периоды : день, неделя, месяц
     */
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "period", nullable = false)
    private BktPeriod period;

    /**
     * Тип бух. проводки : внутренний (внутри банка,межфилиальный) и внешний (поставщикам и т.д.)
     */
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "bookkeeping_type", nullable = false)
    private BookkeepingTransactionType bookkeepingType;

    @Column(name = "success")
    private Boolean success;

    /**
     * Детали
     */
    @NotNull
    @Size(max = 256)
    @Column(name = "details", length = 256, nullable = false)
    private String details;

    /**
     * Тип документа
     */
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

    /**
     * Наименование
     */
    @Size(max = 64)
    @Column(name = "r_account_name", length = 64)
    private String rAccountName;

    /**
     * Пользовтель , который подтвердил проводку
     */
    @Size(max = 128)
    @Column(name = "confirm_user_id", length = 128)
    private String confirmUserID;

    /**
     * Пользовтель , который успешно провел проводку
     */
    @Size(max = 128)
    @Column(name = "send_user_id", length = 128)
    private String sendUserID;

    /**
     * Пользовтель , который отменил проводку
     */
    @Size(max = 128)
    @Column(name = "cancel_user_id", length = 128)
    private String cancelUserID;

    /**
     * Время создания проводки
     */
    @Column(name = "create_time")
    private Instant createTime;

    /**
     * Время подтвреждения проводки
     */
    @Column(name = "confirm_time")
    private Instant confirmTime;

    /**
     * Время успешной обработки проводки
     */
    @Column(name = "send_time")
    private Instant sendTime;

    /**
     * Время отмены проводки
     */
    @Column(name = "cancel_time")
    private Instant cancelTime;

    /**
     * Номер документа в АБС (НСИ)
     */
    @Column(name = "abs_id")
    private Long absId;

    /**
     * Номер документа в АБС (биллинга)
     */
    @Column(name = "doc_id")
    private Long docId;

    /**
     * Уникальный номер шаблона
     */
    @Column(name = "template_id")
    private Long templateId;

    /**
     * Порядковый номер проведения проводки
     */
    @Column(name = "control")
    private Long control;

    /**
     * Ответ от АБС , статус проводки в АБС
     */
    @Size(max = 128)
    @Column(name = "response_text", length = 128)
    private String responseText;

    /**
     * Статус проводки
     */
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private BookkeepingTransactionStatus type;

    /**
     * Уникальный номер для проведения провобки в АБС
     */
    @Column(name = "union_id")
    private Long unionId;

    /**
     * Уникальные номера групп
     */
    @Size(max = 256)
    @Column(name = "group_ids", length = 256)
    private String groupIds;

    /**
     * Регион поставщика
     */
    @Size(max = 128)
    @Column(name = "sub_recipient", length = 128)
    private String subRecipient;

    /**
     * Уникальный номер региона поставщика
     */
    @Column(name = "sub_recipient_id")
    private Long subRecipientId;

    /**
     * Комментарий
     */
    @Size(max = 256)
    @Column(name = "comment", length = 256)
    private String comment;

    /**
     * Тип платежа проводки : за контракт , акт , поставщику и т.д.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "bookkeeping_pay_type")
    private BookkeepingPayType bookkeepingPayType;

    /**
     * Параметры проводки
     */
    @Size(max = 256)
    @Column(name = "params", length = 256)
    private String params;

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

    public BookkeepingTransaction id(Long id) {
        this.id = id;
        return this;
    }

    public Long getGroupId() {
        return this.groupId;
    }

    public BookkeepingTransaction groupId(Long groupId) {
        this.groupId = groupId;
        return this;
    }

    public void setGroupId(Long groupId) {
        this.groupId = groupId;
    }

    public String getsMfo() {
        return this.sMfo;
    }

    public BookkeepingTransaction sMfo(String sMfo) {
        this.sMfo = sMfo;
        return this;
    }

    public void setsMfo(String sMfo) {
        this.sMfo = sMfo;
    }

    public String getsInn() {
        return this.sInn;
    }

    public BookkeepingTransaction sInn(String sInn) {
        this.sInn = sInn;
        return this;
    }

    public void setsInn(String sInn) {
        this.sInn = sInn;
    }

    public String getsAccount() {
        return this.sAccount;
    }

    public BookkeepingTransaction sAccount(String sAccount) {
        this.sAccount = sAccount;
        return this;
    }

    public void setsAccount(String sAccount) {
        this.sAccount = sAccount;
    }

    public String getrMfo() {
        return this.rMfo;
    }

    public BookkeepingTransaction rMfo(String rMfo) {
        this.rMfo = rMfo;
        return this;
    }

    public void setrMfo(String rMfo) {
        this.rMfo = rMfo;
    }

    public String getrInn() {
        return this.rInn;
    }

    public BookkeepingTransaction rInn(String rInn) {
        this.rInn = rInn;
        return this;
    }

    public void setrInn(String rInn) {
        this.rInn = rInn;
    }

    public String getrAccount() {
        return this.rAccount;
    }

    public BookkeepingTransaction rAccount(String rAccount) {
        this.rAccount = rAccount;
        return this;
    }

    public void setrAccount(String rAccount) {
        this.rAccount = rAccount;
    }

    public String getAmount() {
        return this.amount;
    }

    public BookkeepingTransaction amount(String amount) {
        this.amount = amount;
        return this;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public BktPeriod getPeriod() {
        return this.period;
    }

    public BookkeepingTransaction period(BktPeriod period) {
        this.period = period;
        return this;
    }

    public void setPeriod(BktPeriod period) {
        this.period = period;
    }

    public BookkeepingTransactionType getBookkeepingType() {
        return this.bookkeepingType;
    }

    public BookkeepingTransaction bookkeepingType(BookkeepingTransactionType bookkeepingType) {
        this.bookkeepingType = bookkeepingType;
        return this;
    }

    public void setBookkeepingType(BookkeepingTransactionType bookkeepingType) {
        this.bookkeepingType = bookkeepingType;
    }

    public Boolean isSuccess() {
        return success;
    }

    public Boolean getSuccess() {
        return this.success;
    }

    public BookkeepingTransaction success(Boolean success) {
        this.success = success;
        return this;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

    public String getDetails() {
        return this.details;
    }

    public BookkeepingTransaction details(String details) {
        this.details = details;
        return this;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public String getDocType() {
        return this.docType;
    }

    public BookkeepingTransaction docType(String docType) {
        this.docType = docType;
        return this;
    }

    public void setDocType(String docType) {
        this.docType = docType;
    }

    public String getDocCode() {
        return this.docCode;
    }

    public BookkeepingTransaction docCode(String docCode) {
        this.docCode = docCode;
        return this;
    }

    public void setDocCode(String docCode) {
        this.docCode = docCode;
    }

    public String getDocTaskCode() {
        return this.docTaskCode;
    }

    public BookkeepingTransaction docTaskCode(String docTaskCode) {
        this.docTaskCode = docTaskCode;
        return this;
    }

    public void setDocTaskCode(String docTaskCode) {
        this.docTaskCode = docTaskCode;
    }

    public String getrAccountName() {
        return this.rAccountName;
    }

    public BookkeepingTransaction rAccountName(String rAccountName) {
        this.rAccountName = rAccountName;
        return this;
    }

    public void setrAccountName(String rAccountName) {
        this.rAccountName = rAccountName;
    }

    public String getConfirmUserID() {
        return this.confirmUserID;
    }

    public BookkeepingTransaction confirmUserID(String confirmUserID) {
        this.confirmUserID = confirmUserID;
        return this;
    }

    public void setConfirmUserID(String confirmUserID) {
        this.confirmUserID = confirmUserID;
    }

    public String getSendUserID() {
        return this.sendUserID;
    }

    public BookkeepingTransaction sendUserID(String sendUserID) {
        this.sendUserID = sendUserID;
        return this;
    }

    public void setSendUserID(String sendUserID) {
        this.sendUserID = sendUserID;
    }

    public String getCancelUserID() {
        return this.cancelUserID;
    }

    public BookkeepingTransaction cancelUserID(String cancelUserID) {
        this.cancelUserID = cancelUserID;
        return this;
    }

    public void setCancelUserID(String cancelUserID) {
        this.cancelUserID = cancelUserID;
    }

    public Instant getCreateTime() {
        return this.createTime;
    }

    public BookkeepingTransaction createTime(Instant createTime) {
        this.createTime = createTime;
        return this;
    }

    public void setCreateTime(Instant createTime) {
        this.createTime = createTime;
    }

    public Instant getConfirmTime() {
        return this.confirmTime;
    }

    public BookkeepingTransaction confirmTime(Instant confirmTime) {
        this.confirmTime = confirmTime;
        return this;
    }

    public void setConfirmTime(Instant confirmTime) {
        this.confirmTime = confirmTime;
    }

    public Instant getSendTime() {
        return this.sendTime;
    }

    public BookkeepingTransaction sendTime(Instant sendTime) {
        this.sendTime = sendTime;
        return this;
    }

    public void setSendTime(Instant sendTime) {
        this.sendTime = sendTime;
    }

    public Instant getCancelTime() {
        return this.cancelTime;
    }

    public BookkeepingTransaction cancelTime(Instant cancelTime) {
        this.cancelTime = cancelTime;
        return this;
    }

    public void setCancelTime(Instant cancelTime) {
        this.cancelTime = cancelTime;
    }

    public Long getAbsId() {
        return this.absId;
    }

    public BookkeepingTransaction absId(Long absId) {
        this.absId = absId;
        return this;
    }

    public void setAbsId(Long absId) {
        this.absId = absId;
    }

    public Long getDocId() {
        return this.docId;
    }

    public BookkeepingTransaction docId(Long docId) {
        this.docId = docId;
        return this;
    }

    public void setDocId(Long docId) {
        this.docId = docId;
    }

    public Long getTemplateId() {
        return this.templateId;
    }

    public BookkeepingTransaction templateId(Long templateId) {
        this.templateId = templateId;
        return this;
    }

    public void setTemplateId(Long templateId) {
        this.templateId = templateId;
    }

    public Long getControl() {
        return this.control;
    }

    public BookkeepingTransaction control(Long control) {
        this.control = control;
        return this;
    }

    public void setControl(Long control) {
        this.control = control;
    }

    public String getResponseText() {
        return this.responseText;
    }

    public BookkeepingTransaction responseText(String responseText) {
        this.responseText = responseText;
        return this;
    }

    public void setResponseText(String responseText) {
        this.responseText = responseText;
    }

    public BookkeepingTransactionStatus getType() {
        return this.type;
    }

    public BookkeepingTransaction type(BookkeepingTransactionStatus type) {
        this.type = type;
        return this;
    }

    public void setType(BookkeepingTransactionStatus type) {
        this.type = type;
    }

    public Long getUnionId() {
        return this.unionId;
    }

    public BookkeepingTransaction unionId(Long unionId) {
        this.unionId = unionId;
        return this;
    }

    public void setUnionId(Long unionId) {
        this.unionId = unionId;
    }

    public String getGroupIds() {
        return this.groupIds;
    }

    public BookkeepingTransaction groupIds(String groupIds) {
        this.groupIds = groupIds;
        return this;
    }

    public void setGroupIds(String groupIds) {
        this.groupIds = groupIds;
    }

    public String getSubRecipient() {
        return this.subRecipient;
    }

    public BookkeepingTransaction subRecipient(String subRecipient) {
        this.subRecipient = subRecipient;
        return this;
    }

    public void setSubRecipient(String subRecipient) {
        this.subRecipient = subRecipient;
    }

    public Long getSubRecipientId() {
        return this.subRecipientId;
    }

    public BookkeepingTransaction subRecipientId(Long subRecipientId) {
        this.subRecipientId = subRecipientId;
        return this;
    }

    public void setSubRecipientId(Long subRecipientId) {
        this.subRecipientId = subRecipientId;
    }

    public String getComment() {
        return this.comment;
    }

    public BookkeepingTransaction comment(String comment) {
        this.comment = comment;
        return this;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public BookkeepingPayType getBookkeepingPayType() {
        return this.bookkeepingPayType;
    }

    public BookkeepingTransaction bookkeepingPayType(BookkeepingPayType bookkeepingPayType) {
        this.bookkeepingPayType = bookkeepingPayType;
        return this;
    }

    public void setBookkeepingPayType(BookkeepingPayType bookkeepingPayType) {
        this.bookkeepingPayType = bookkeepingPayType;
    }

    public String getParams() {
        return this.params;
    }

    public BookkeepingTransaction params(String params) {
        this.params = params;
        return this;
    }

    public void setParams(String params) {
        this.params = params;
    }

    public Gateway getGateway() {
        return this.gateway;
    }

    public BookkeepingTransaction gateway(Gateway gateway) {
        this.setGateway(gateway);
        return this;
    }

    public void setGateway(Gateway gateway) {
        this.gateway = gateway;
    }

    public Recipient getRecipient() {
        return this.recipient;
    }

    public BookkeepingTransaction recipient(Recipient recipient) {
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
        if (!(o instanceof BookkeepingTransaction)) {
            return false;
        }
        return id != null && id.equals(((BookkeepingTransaction) o).id);
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "BookkeepingTransaction{" +
            "id=" + getId() +
            ", groupId=" + getGroupId() +
            ", sMfo='" + getsMfo() + "'" +
            ", sInn='" + getsInn() + "'" +
            ", sAccount='" + getsAccount() + "'" +
            ", rMfo='" + getrMfo() + "'" +
            ", rInn='" + getrInn() + "'" +
            ", rAccount='" + getrAccount() + "'" +
            ", amount='" + getAmount() + "'" +
            ", period='" + getPeriod() + "'" +
            ", bookkeepingType='" + getBookkeepingType() + "'" +
            ", success='" + getSuccess() + "'" +
            ", details='" + getDetails() + "'" +
            ", docType='" + getDocType() + "'" +
            ", docCode='" + getDocCode() + "'" +
            ", docTaskCode='" + getDocTaskCode() + "'" +
            ", rAccountName='" + getrAccountName() + "'" +
            ", confirmUserID='" + getConfirmUserID() + "'" +
            ", sendUserID='" + getSendUserID() + "'" +
            ", cancelUserID='" + getCancelUserID() + "'" +
            ", createTime='" + getCreateTime() + "'" +
            ", confirmTime='" + getConfirmTime() + "'" +
            ", sendTime='" + getSendTime() + "'" +
            ", cancelTime='" + getCancelTime() + "'" +
            ", absId=" + getAbsId() +
            ", docId=" + getDocId() +
            ", templateId=" + getTemplateId() +
            ", control=" + getControl() +
            ", responseText='" + getResponseText() + "'" +
            ", type='" + getType() + "'" +
            ", unionId=" + getUnionId() +
            ", groupIds='" + getGroupIds() + "'" +
            ", subRecipient='" + getSubRecipient() + "'" +
            ", subRecipientId=" + getSubRecipientId() +
            ", comment='" + getComment() + "'" +
            ", bookkeepingPayType='" + getBookkeepingPayType() + "'" +
            ", params='" + getParams() + "'" +
            "}";
    }
}
