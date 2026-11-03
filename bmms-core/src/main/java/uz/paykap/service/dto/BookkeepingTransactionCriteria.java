package uz.paykap.service.dto;

import io.github.jhipster.service.filter.*;
import uz.paykap.domain.enumeration.BktPeriod;
import uz.paykap.domain.enumeration.BookkeepingPayType;
import uz.paykap.domain.enumeration.BookkeepingTransactionStatus;
import uz.paykap.domain.enumeration.BookkeepingTransactionType;

import java.io.Serializable;




/**
 * Criteria class for the BookkeepingTransaction entity. This class is used in BookkeepingTransactionResource to
 * receive all the possible filtering options from the Http GET request parameters.
 * For example the following could be a valid requests:
 * <code> /bookkeeping-transactions?id.greaterThan=5&amp;attr1.contains=something&amp;attr2.specified=false</code>
 * As Spring is unable to properly convert the types, unless specific {@link Filter} class are used, we need to use
 * fix type specific filters.
 */
public class BookkeepingTransactionCriteria implements Serializable {
    /**
     * Class for filtering BktPeriod
     */
    public static class BktPeriodFilter extends Filter<BktPeriod> {
    }

    /**
     * Class for filtering BookkeepingTransactionType
     */
    public static class BookkeepingTransactionTypeFilter extends Filter<BookkeepingTransactionType> {
    }

    /**
     * Class for filtering BookkeepingTransactionStatus
     */
    public static class BookkeepingTransactionStatusFilter extends Filter<BookkeepingTransactionStatus> {
    }

    /**
     * Class for filtering BookkeepingPayType
     */
    public static class BookkeepingPayTypeFilter extends Filter<BookkeepingPayType> {
    }

    private static final long serialVersionUID = 1L;


    private LongFilter id;

    private LongFilter groupId;

    private StringFilter sMfo;

    private StringFilter sInn;

    private StringFilter sAccount;

    private StringFilter rMfo;

    private StringFilter rInn;

    private StringFilter rAccount;

    private StringFilter amount;

    private BktPeriodFilter period;

    private BookkeepingTransactionTypeFilter bookkeepingType;

    private BooleanFilter success;

    private StringFilter details;

    private StringFilter docType;

    private StringFilter docCode;

    private StringFilter docTaskCode;

    private StringFilter rAccountName;

    private StringFilter confirmUserID;

    private StringFilter sendUserID;

    private StringFilter cancelUserID;

    private InstantFilter createTime;

    private InstantFilter confirmTime;

    private InstantFilter sendTime;

    private InstantFilter cancelTime;

    private LongFilter absId;

    private LongFilter docId;

    private LongFilter templateId;

    private LongFilter control;

    private StringFilter responseText;

    private BookkeepingTransactionStatusFilter type;

    private LongFilter unionId;

    private StringFilter groupIds;

    private StringFilter subRecipient;

    private LongFilter subRecipientId;

    private StringFilter comment;

    private BookkeepingPayTypeFilter bookkeepingPayType;

    private StringFilter params;

    private LongFilter gatewayId;

    private LongFilter recipientId;

    public BookkeepingTransactionCriteria() {
    }

    public LongFilter getId() {
        return id;
    }

    public void setId(LongFilter id) {
        this.id = id;
    }

    public LongFilter getGroupId() {
        return groupId;
    }

    public void setGroupId(LongFilter groupId) {
        this.groupId = groupId;
    }

    public StringFilter getsMfo() {
        return sMfo;
    }

    public void setsMfo(StringFilter sMfo) {
        this.sMfo = sMfo;
    }

    public StringFilter getsInn() {
        return sInn;
    }

    public void setsInn(StringFilter sInn) {
        this.sInn = sInn;
    }

    public StringFilter getsAccount() {
        return sAccount;
    }

    public void setsAccount(StringFilter sAccount) {
        this.sAccount = sAccount;
    }

    public StringFilter getrMfo() {
        return rMfo;
    }

    public void setrMfo(StringFilter rMfo) {
        this.rMfo = rMfo;
    }

    public StringFilter getrInn() {
        return rInn;
    }

    public void setrInn(StringFilter rInn) {
        this.rInn = rInn;
    }

    public StringFilter getrAccount() {
        return rAccount;
    }

    public void setrAccount(StringFilter rAccount) {
        this.rAccount = rAccount;
    }

    public StringFilter getAmount() {
        return amount;
    }

    public void setAmount(StringFilter amount) {
        this.amount = amount;
    }

    public BktPeriodFilter getPeriod() {
        return period;
    }

    public void setPeriod(BktPeriodFilter period) {
        this.period = period;
    }

    public BookkeepingTransactionTypeFilter getBookkeepingType() {
        return bookkeepingType;
    }

    public void setBookkeepingType(BookkeepingTransactionTypeFilter bookkeepingType) {
        this.bookkeepingType = bookkeepingType;
    }

    public BooleanFilter getSuccess() {
        return success;
    }

    public void setSuccess(BooleanFilter success) {
        this.success = success;
    }

    public StringFilter getDetails() {
        return details;
    }

    public void setDetails(StringFilter details) {
        this.details = details;
    }

    public StringFilter getDocType() {
        return docType;
    }

    public void setDocType(StringFilter docType) {
        this.docType = docType;
    }

    public StringFilter getDocCode() {
        return docCode;
    }

    public void setDocCode(StringFilter docCode) {
        this.docCode = docCode;
    }

    public StringFilter getDocTaskCode() {
        return docTaskCode;
    }

    public void setDocTaskCode(StringFilter docTaskCode) {
        this.docTaskCode = docTaskCode;
    }

    public StringFilter getrAccountName() {
        return rAccountName;
    }

    public void setrAccountName(StringFilter rAccountName) {
        this.rAccountName = rAccountName;
    }

    public StringFilter getConfirmUserID() {
        return confirmUserID;
    }

    public void setConfirmUserID(StringFilter confirmUserID) {
        this.confirmUserID = confirmUserID;
    }

    public StringFilter getSendUserID() {
        return sendUserID;
    }

    public void setSendUserID(StringFilter sendUserID) {
        this.sendUserID = sendUserID;
    }

    public StringFilter getCancelUserID() {
        return cancelUserID;
    }

    public void setCancelUserID(StringFilter cancelUserID) {
        this.cancelUserID = cancelUserID;
    }

    public InstantFilter getCreateTime() {
        return createTime;
    }

    public void setCreateTime(InstantFilter createTime) {
        this.createTime = createTime;
    }

    public InstantFilter getConfirmTime() {
        return confirmTime;
    }

    public void setConfirmTime(InstantFilter confirmTime) {
        this.confirmTime = confirmTime;
    }

    public InstantFilter getSendTime() {
        return sendTime;
    }

    public void setSendTime(InstantFilter sendTime) {
        this.sendTime = sendTime;
    }

    public InstantFilter getCancelTime() {
        return cancelTime;
    }

    public void setCancelTime(InstantFilter cancelTime) {
        this.cancelTime = cancelTime;
    }

    public LongFilter getAbsId() {
        return absId;
    }

    public void setAbsId(LongFilter absId) {
        this.absId = absId;
    }

    public LongFilter getDocId() {
        return docId;
    }

    public void setDocId(LongFilter docId) {
        this.docId = docId;
    }

    public LongFilter getTemplateId() {
        return templateId;
    }

    public void setTemplateId(LongFilter templateId) {
        this.templateId = templateId;
    }

    public LongFilter getControl() {
        return control;
    }

    public void setControl(LongFilter control) {
        this.control = control;
    }

    public StringFilter getResponseText() {
        return responseText;
    }

    public void setResponseText(StringFilter responseText) {
        this.responseText = responseText;
    }

    public BookkeepingTransactionStatusFilter getType() {
        return type;
    }

    public void setType(BookkeepingTransactionStatusFilter type) {
        this.type = type;
    }

    public LongFilter getUnionId() {
        return unionId;
    }

    public void setUnionId(LongFilter unionId) {
        this.unionId = unionId;
    }

    public StringFilter getGroupIds() {
        return groupIds;
    }

    public void setGroupIds(StringFilter groupIds) {
        this.groupIds = groupIds;
    }

    public StringFilter getSubRecipient() {
        return subRecipient;
    }

    public void setSubRecipient(StringFilter subRecipient) {
        this.subRecipient = subRecipient;
    }

    public LongFilter getSubRecipientId() {
        return subRecipientId;
    }

    public void setSubRecipientId(LongFilter subRecipientId) {
        this.subRecipientId = subRecipientId;
    }

    public StringFilter getComment() {
        return comment;
    }

    public void setComment(StringFilter comment) {
        this.comment = comment;
    }

    public BookkeepingPayTypeFilter getBookkeepingPayType() {
        return bookkeepingPayType;
    }

    public void setBookkeepingPayType(BookkeepingPayTypeFilter bookkeepingPayType) {
        this.bookkeepingPayType = bookkeepingPayType;
    }

    public StringFilter getParams() {
        return params;
    }

    public void setParams(StringFilter params) {
        this.params = params;
    }

    public LongFilter getGatewayId() {
        return gatewayId;
    }

    public void setGatewayId(LongFilter gatewayId) {
        this.gatewayId = gatewayId;
    }

    public LongFilter getRecipientId() {
        return recipientId;
    }

    public void setRecipientId(LongFilter recipientId) {
        this.recipientId = recipientId;
    }

    @Override
    public String toString() {
        return "BookkeepingTransactionCriteria{" +
                (id != null ? "id=" + id + ", " : "") +
                (groupId != null ? "groupId=" + groupId + ", " : "") +
                (sMfo != null ? "sMfo=" + sMfo + ", " : "") +
                (sInn != null ? "sInn=" + sInn + ", " : "") +
                (sAccount != null ? "sAccount=" + sAccount + ", " : "") +
                (rMfo != null ? "rMfo=" + rMfo + ", " : "") +
                (rInn != null ? "rInn=" + rInn + ", " : "") +
                (rAccount != null ? "rAccount=" + rAccount + ", " : "") +
                (amount != null ? "amount=" + amount + ", " : "") +
                (period != null ? "period=" + period + ", " : "") +
                (bookkeepingType != null ? "bookkeepingType=" + bookkeepingType + ", " : "") +
                (success != null ? "success=" + success + ", " : "") +
                (details != null ? "details=" + details + ", " : "") +
                (docType != null ? "docType=" + docType + ", " : "") +
                (docCode != null ? "docCode=" + docCode + ", " : "") +
                (docTaskCode != null ? "docTaskCode=" + docTaskCode + ", " : "") +
                (rAccountName != null ? "rAccountName=" + rAccountName + ", " : "") +
                (confirmUserID != null ? "confirmUserID=" + confirmUserID + ", " : "") +
                (sendUserID != null ? "sendUserID=" + sendUserID + ", " : "") +
                (cancelUserID != null ? "cancelUserID=" + cancelUserID + ", " : "") +
                (createTime != null ? "createTime=" + createTime + ", " : "") +
                (confirmTime != null ? "confirmTime=" + confirmTime + ", " : "") +
                (sendTime != null ? "sendTime=" + sendTime + ", " : "") +
                (cancelTime != null ? "cancelTime=" + cancelTime + ", " : "") +
                (absId != null ? "absId=" + absId + ", " : "") +
                (docId != null ? "docId=" + docId + ", " : "") +
                (templateId != null ? "templateId=" + templateId + ", " : "") +
                (control != null ? "control=" + control + ", " : "") +
                (responseText != null ? "responseText=" + responseText + ", " : "") +
                (type != null ? "type=" + type + ", " : "") +
                (unionId != null ? "unionId=" + unionId + ", " : "") +
                (groupIds != null ? "groupIds=" + groupIds + ", " : "") +
                (subRecipient != null ? "subRecipient=" + subRecipient + ", " : "") +
                (subRecipientId != null ? "subRecipientId=" + subRecipientId + ", " : "") +
                (comment != null ? "comment=" + comment + ", " : "") +
                (bookkeepingPayType != null ? "bookkeepingPayType=" + bookkeepingPayType + ", " : "") +
            (params != null ? "params=" + params + ", " : "") +
                (gatewayId != null ? "gatewayId=" + gatewayId + ", " : "") +
                (recipientId != null ? "recipientId=" + recipientId + ", " : "") +
            "}";
    }

}
