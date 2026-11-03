package uz.paykap.service.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;
import javax.validation.constraints.*;
import uz.paykap.domain.enumeration.BktPeriod;
import uz.paykap.domain.enumeration.BookkeepingPayType;
import uz.paykap.domain.enumeration.BookkeepingTransactionStatus;
import uz.paykap.domain.enumeration.BookkeepingTransactionType;

/**
 * A DTO for the {@link uz.paykap.domain.BookkeepingTransaction} entity.
 */
@ApiModel(description = "Данная форма предназначена для создания проводок в разрезе групп")
public class BookkeepingTransactionDTO implements Serializable {

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
     * Сумма на отправку
     */
    @NotNull
    @Size(max = 64)
    @ApiModelProperty(value = "Сумма на отправку", required = true)
    private String amount;

    /**
     * Периоды : день, неделя, месяц
     */
    @NotNull
    @ApiModelProperty(value = "Периоды : день, неделя, месяц", required = true)
    private BktPeriod period;

    /**
     * Тип бух. проводки : внутренний (внутри банка,межфилиальный) и внешний (поставщикам и т.д.)
     */
    @NotNull
    @ApiModelProperty(value = "Тип бух. проводки : внутренний (внутри банка,межфилиальный) и внешний (поставщикам и т.д.)", required = true)
    private BookkeepingTransactionType bookkeepingType;

    private Boolean success;

    /**
     * Детали
     */
    @NotNull
    @Size(max = 256)
    @ApiModelProperty(value = "Детали", required = true)
    private String details;

    /**
     * Тип документа
     */
    @Size(max = 8)
    @ApiModelProperty(value = "Тип документа")
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

    /**
     * Наименование
     */
    @Size(max = 64)
    @ApiModelProperty(value = "Наименование")
    private String rAccountName;

    /**
     * Пользовтель , который подтвердил проводку
     */
    @Size(max = 128)
    @ApiModelProperty(value = "Пользовтель , который подтвердил проводку")
    private String confirmUserID;

    /**
     * Пользовтель , который успешно провел проводку
     */
    @Size(max = 128)
    @ApiModelProperty(value = "Пользовтель , который успешно провел проводку")
    private String sendUserID;

    /**
     * Пользовтель , который отменил проводку
     */
    @Size(max = 128)
    @ApiModelProperty(value = "Пользовтель , который отменил проводку")
    private String cancelUserID;

    /**
     * Время создания проводки
     */
    @ApiModelProperty(value = "Время создания проводки")
    private Instant createTime;

    /**
     * Время подтвреждения проводки
     */
    @ApiModelProperty(value = "Время подтвреждения проводки")
    private Instant confirmTime;

    /**
     * Время успешной обработки проводки
     */
    @ApiModelProperty(value = "Время успешной обработки проводки")
    private Instant sendTime;

    /**
     * Время отмены проводки
     */
    @ApiModelProperty(value = "Время отмены проводки")
    private Instant cancelTime;

    /**
     * Номер документа в АБС (НСИ)
     */
    @ApiModelProperty(value = "Номер документа в АБС (НСИ)")
    private Long absId;

    /**
     * Номер документа в АБС (биллинга)
     */
    @ApiModelProperty(value = "Номер документа в АБС (биллинга)")
    private Long docId;

    /**
     * Уникальный номер шаблона
     */
    @ApiModelProperty(value = "Уникальный номер шаблона")
    private Long templateId;

    /**
     * Порядковый номер проведения проводки
     */
    @ApiModelProperty(value = "Порядковый номер проведения проводки")
    private Long control;

    /**
     * Ответ от АБС , статус проводки в АБС
     */
    @Size(max = 128)
    @ApiModelProperty(value = "Ответ от АБС , статус проводки в АБС")
    private String responseText;

    /**
     * Статус проводки
     */
    @NotNull
    @ApiModelProperty(value = "Статус проводки", required = true)
    private BookkeepingTransactionStatus type;

    /**
     * Уникальный номер для проведения провобки в АБС
     */
    @ApiModelProperty(value = "Уникальный номер для проведения провобки в АБС")
    private Long unionId;

    /**
     * Уникальные номера групп
     */
    @Size(max = 256)
    @ApiModelProperty(value = "Уникальные номера групп")
    private String groupIds;

    /**
     * Регион поставщика
     */
    @Size(max = 128)
    @ApiModelProperty(value = "Регион поставщика")
    private String subRecipient;

    /**
     * Уникальный номер региона поставщика
     */
    @ApiModelProperty(value = "Уникальный номер региона поставщика")
    private Long subRecipientId;

    /**
     * Комментарий
     */
    @Size(max = 256)
    @ApiModelProperty(value = "Комментарий")
    private String comment;

    /**
     * Тип платежа проводки : за контракт , акт , поставщику и т.д.
     */
    @ApiModelProperty(value = "Тип платежа проводки : за контракт , акт , поставщику и т.д.")
    private BookkeepingPayType bookkeepingPayType;

    /**
     * Параметры проводки
     */
    @Size(max = 256)
    @ApiModelProperty(value = "Параметры проводки")
    private String params;

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

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public BktPeriod getPeriod() {
        return period;
    }

    public void setPeriod(BktPeriod period) {
        this.period = period;
    }

    public BookkeepingTransactionType getBookkeepingType() {
        return bookkeepingType;
    }

    public void setBookkeepingType(BookkeepingTransactionType bookkeepingType) {
        this.bookkeepingType = bookkeepingType;
    }

    public Boolean getSuccess() {
        return success;
    }

    public Boolean isSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
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

    public String getConfirmUserID() {
        return confirmUserID;
    }

    public void setConfirmUserID(String confirmUserID) {
        this.confirmUserID = confirmUserID;
    }

    public String getSendUserID() {
        return sendUserID;
    }

    public void setSendUserID(String sendUserID) {
        this.sendUserID = sendUserID;
    }

    public String getCancelUserID() {
        return cancelUserID;
    }

    public void setCancelUserID(String cancelUserID) {
        this.cancelUserID = cancelUserID;
    }

    public Instant getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Instant createTime) {
        this.createTime = createTime;
    }

    public Instant getConfirmTime() {
        return confirmTime;
    }

    public void setConfirmTime(Instant confirmTime) {
        this.confirmTime = confirmTime;
    }

    public Instant getSendTime() {
        return sendTime;
    }

    public void setSendTime(Instant sendTime) {
        this.sendTime = sendTime;
    }

    public Instant getCancelTime() {
        return cancelTime;
    }

    public void setCancelTime(Instant cancelTime) {
        this.cancelTime = cancelTime;
    }

    public Long getAbsId() {
        return absId;
    }

    public void setAbsId(Long absId) {
        this.absId = absId;
    }

    public Long getDocId() {
        return docId;
    }

    public void setDocId(Long docId) {
        this.docId = docId;
    }

    public Long getTemplateId() {
        return templateId;
    }

    public void setTemplateId(Long templateId) {
        this.templateId = templateId;
    }

    public Long getControl() {
        return control;
    }

    public void setControl(Long control) {
        this.control = control;
    }

    public String getResponseText() {
        return responseText;
    }

    public void setResponseText(String responseText) {
        this.responseText = responseText;
    }

    public BookkeepingTransactionStatus getType() {
        return type;
    }

    public void setType(BookkeepingTransactionStatus type) {
        this.type = type;
    }

    public Long getUnionId() {
        return unionId;
    }

    public void setUnionId(Long unionId) {
        this.unionId = unionId;
    }

    public String getGroupIds() {
        return groupIds;
    }

    public void setGroupIds(String groupIds) {
        this.groupIds = groupIds;
    }

    public String getSubRecipient() {
        return subRecipient;
    }

    public void setSubRecipient(String subRecipient) {
        this.subRecipient = subRecipient;
    }

    public Long getSubRecipientId() {
        return subRecipientId;
    }

    public void setSubRecipientId(Long subRecipientId) {
        this.subRecipientId = subRecipientId;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public BookkeepingPayType getBookkeepingPayType() {
        return bookkeepingPayType;
    }

    public void setBookkeepingPayType(BookkeepingPayType bookkeepingPayType) {
        this.bookkeepingPayType = bookkeepingPayType;
    }

    public String getParams() {
        return params;
    }

    public void setParams(String params) {
        this.params = params;
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
        if (!(o instanceof BookkeepingTransactionDTO)) {
            return false;
        }

        BookkeepingTransactionDTO bookkeepingTransactionDTO = (BookkeepingTransactionDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, bookkeepingTransactionDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "BookkeepingTransactionDTO{" +
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
            ", gateway=" + getGateway() +
            ", recipient=" + getRecipient() +
            "}";
    }
}
