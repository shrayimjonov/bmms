package uz.paykap.service.criteria;

import java.io.Serializable;
import java.util.Objects;
import tech.jhipster.service.Criteria;
import tech.jhipster.service.filter.BooleanFilter;
import tech.jhipster.service.filter.DoubleFilter;
import tech.jhipster.service.filter.Filter;
import tech.jhipster.service.filter.FloatFilter;
import tech.jhipster.service.filter.InstantFilter;
import tech.jhipster.service.filter.IntegerFilter;
import tech.jhipster.service.filter.LongFilter;
import tech.jhipster.service.filter.StringFilter;
import uz.paykap.domain.enumeration.BktPeriod;
import uz.paykap.domain.enumeration.BookkeepingPayType;
import uz.paykap.domain.enumeration.BookkeepingTransactionStatus;
import uz.paykap.domain.enumeration.BookkeepingTransactionType;

/**
 * Criteria class for the {@link uz.paykap.domain.BookkeepingTransaction} entity. This class is used
 * in {@link uz.paykap.web.rest.BookkeepingTransactionResource} to receive all the possible filtering options from
 * the Http GET request parameters.
 * For example the following could be a valid request:
 * {@code /bookkeeping-transactions?id.greaterThan=5&attr1.contains=something&attr2.specified=false}
 * As Spring is unable to properly convert the types, unless specific {@link Filter} class are used, we need to use
 * fix type specific filters.
 */
public class BookkeepingTransactionCriteria implements Serializable, Criteria {

    /**
     * Class for filtering BktPeriod
     */
    public static class BktPeriodFilter extends Filter<BktPeriod> {

        public BktPeriodFilter() {}

        public BktPeriodFilter(BktPeriodFilter filter) {
            super(filter);
        }

        @Override
        public BktPeriodFilter copy() {
            return new BktPeriodFilter(this);
        }
    }

    /**
     * Class for filtering BookkeepingTransactionType
     */
    public static class BookkeepingTransactionTypeFilter extends Filter<BookkeepingTransactionType> {

        public BookkeepingTransactionTypeFilter() {}

        public BookkeepingTransactionTypeFilter(BookkeepingTransactionTypeFilter filter) {
            super(filter);
        }

        @Override
        public BookkeepingTransactionTypeFilter copy() {
            return new BookkeepingTransactionTypeFilter(this);
        }
    }

    /**
     * Class for filtering BookkeepingTransactionStatus
     */
    public static class BookkeepingTransactionStatusFilter extends Filter<BookkeepingTransactionStatus> {

        public BookkeepingTransactionStatusFilter() {}

        public BookkeepingTransactionStatusFilter(BookkeepingTransactionStatusFilter filter) {
            super(filter);
        }

        @Override
        public BookkeepingTransactionStatusFilter copy() {
            return new BookkeepingTransactionStatusFilter(this);
        }
    }

    /**
     * Class for filtering BookkeepingPayType
     */
    public static class BookkeepingPayTypeFilter extends Filter<BookkeepingPayType> {

        public BookkeepingPayTypeFilter() {}

        public BookkeepingPayTypeFilter(BookkeepingPayTypeFilter filter) {
            super(filter);
        }

        @Override
        public BookkeepingPayTypeFilter copy() {
            return new BookkeepingPayTypeFilter(this);
        }
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

    public BookkeepingTransactionCriteria() {}

    public BookkeepingTransactionCriteria(BookkeepingTransactionCriteria other) {
        this.id = other.id == null ? null : other.id.copy();
        this.groupId = other.groupId == null ? null : other.groupId.copy();
        this.sMfo = other.sMfo == null ? null : other.sMfo.copy();
        this.sInn = other.sInn == null ? null : other.sInn.copy();
        this.sAccount = other.sAccount == null ? null : other.sAccount.copy();
        this.rMfo = other.rMfo == null ? null : other.rMfo.copy();
        this.rInn = other.rInn == null ? null : other.rInn.copy();
        this.rAccount = other.rAccount == null ? null : other.rAccount.copy();
        this.amount = other.amount == null ? null : other.amount.copy();
        this.period = other.period == null ? null : other.period.copy();
        this.bookkeepingType = other.bookkeepingType == null ? null : other.bookkeepingType.copy();
        this.success = other.success == null ? null : other.success.copy();
        this.details = other.details == null ? null : other.details.copy();
        this.docType = other.docType == null ? null : other.docType.copy();
        this.docCode = other.docCode == null ? null : other.docCode.copy();
        this.docTaskCode = other.docTaskCode == null ? null : other.docTaskCode.copy();
        this.rAccountName = other.rAccountName == null ? null : other.rAccountName.copy();
        this.confirmUserID = other.confirmUserID == null ? null : other.confirmUserID.copy();
        this.sendUserID = other.sendUserID == null ? null : other.sendUserID.copy();
        this.cancelUserID = other.cancelUserID == null ? null : other.cancelUserID.copy();
        this.createTime = other.createTime == null ? null : other.createTime.copy();
        this.confirmTime = other.confirmTime == null ? null : other.confirmTime.copy();
        this.sendTime = other.sendTime == null ? null : other.sendTime.copy();
        this.cancelTime = other.cancelTime == null ? null : other.cancelTime.copy();
        this.absId = other.absId == null ? null : other.absId.copy();
        this.docId = other.docId == null ? null : other.docId.copy();
        this.templateId = other.templateId == null ? null : other.templateId.copy();
        this.control = other.control == null ? null : other.control.copy();
        this.responseText = other.responseText == null ? null : other.responseText.copy();
        this.type = other.type == null ? null : other.type.copy();
        this.unionId = other.unionId == null ? null : other.unionId.copy();
        this.groupIds = other.groupIds == null ? null : other.groupIds.copy();
        this.subRecipient = other.subRecipient == null ? null : other.subRecipient.copy();
        this.subRecipientId = other.subRecipientId == null ? null : other.subRecipientId.copy();
        this.comment = other.comment == null ? null : other.comment.copy();
        this.bookkeepingPayType = other.bookkeepingPayType == null ? null : other.bookkeepingPayType.copy();
        this.params = other.params == null ? null : other.params.copy();
        this.gatewayId = other.gatewayId == null ? null : other.gatewayId.copy();
        this.recipientId = other.recipientId == null ? null : other.recipientId.copy();
    }

    @Override
    public BookkeepingTransactionCriteria copy() {
        return new BookkeepingTransactionCriteria(this);
    }

    public LongFilter getId() {
        return id;
    }

    public LongFilter id() {
        if (id == null) {
            id = new LongFilter();
        }
        return id;
    }

    public void setId(LongFilter id) {
        this.id = id;
    }

    public LongFilter getGroupId() {
        return groupId;
    }

    public LongFilter groupId() {
        if (groupId == null) {
            groupId = new LongFilter();
        }
        return groupId;
    }

    public void setGroupId(LongFilter groupId) {
        this.groupId = groupId;
    }

    public StringFilter getsMfo() {
        return sMfo;
    }

    public StringFilter sMfo() {
        if (sMfo == null) {
            sMfo = new StringFilter();
        }
        return sMfo;
    }

    public void setsMfo(StringFilter sMfo) {
        this.sMfo = sMfo;
    }

    public StringFilter getsInn() {
        return sInn;
    }

    public StringFilter sInn() {
        if (sInn == null) {
            sInn = new StringFilter();
        }
        return sInn;
    }

    public void setsInn(StringFilter sInn) {
        this.sInn = sInn;
    }

    public StringFilter getsAccount() {
        return sAccount;
    }

    public StringFilter sAccount() {
        if (sAccount == null) {
            sAccount = new StringFilter();
        }
        return sAccount;
    }

    public void setsAccount(StringFilter sAccount) {
        this.sAccount = sAccount;
    }

    public StringFilter getrMfo() {
        return rMfo;
    }

    public StringFilter rMfo() {
        if (rMfo == null) {
            rMfo = new StringFilter();
        }
        return rMfo;
    }

    public void setrMfo(StringFilter rMfo) {
        this.rMfo = rMfo;
    }

    public StringFilter getrInn() {
        return rInn;
    }

    public StringFilter rInn() {
        if (rInn == null) {
            rInn = new StringFilter();
        }
        return rInn;
    }

    public void setrInn(StringFilter rInn) {
        this.rInn = rInn;
    }

    public StringFilter getrAccount() {
        return rAccount;
    }

    public StringFilter rAccount() {
        if (rAccount == null) {
            rAccount = new StringFilter();
        }
        return rAccount;
    }

    public void setrAccount(StringFilter rAccount) {
        this.rAccount = rAccount;
    }

    public StringFilter getAmount() {
        return amount;
    }

    public StringFilter amount() {
        if (amount == null) {
            amount = new StringFilter();
        }
        return amount;
    }

    public void setAmount(StringFilter amount) {
        this.amount = amount;
    }

    public BktPeriodFilter getPeriod() {
        return period;
    }

    public BktPeriodFilter period() {
        if (period == null) {
            period = new BktPeriodFilter();
        }
        return period;
    }

    public void setPeriod(BktPeriodFilter period) {
        this.period = period;
    }

    public BookkeepingTransactionTypeFilter getBookkeepingType() {
        return bookkeepingType;
    }

    public BookkeepingTransactionTypeFilter bookkeepingType() {
        if (bookkeepingType == null) {
            bookkeepingType = new BookkeepingTransactionTypeFilter();
        }
        return bookkeepingType;
    }

    public void setBookkeepingType(BookkeepingTransactionTypeFilter bookkeepingType) {
        this.bookkeepingType = bookkeepingType;
    }

    public BooleanFilter getSuccess() {
        return success;
    }

    public BooleanFilter success() {
        if (success == null) {
            success = new BooleanFilter();
        }
        return success;
    }

    public void setSuccess(BooleanFilter success) {
        this.success = success;
    }

    public StringFilter getDetails() {
        return details;
    }

    public StringFilter details() {
        if (details == null) {
            details = new StringFilter();
        }
        return details;
    }

    public void setDetails(StringFilter details) {
        this.details = details;
    }

    public StringFilter getDocType() {
        return docType;
    }

    public StringFilter docType() {
        if (docType == null) {
            docType = new StringFilter();
        }
        return docType;
    }

    public void setDocType(StringFilter docType) {
        this.docType = docType;
    }

    public StringFilter getDocCode() {
        return docCode;
    }

    public StringFilter docCode() {
        if (docCode == null) {
            docCode = new StringFilter();
        }
        return docCode;
    }

    public void setDocCode(StringFilter docCode) {
        this.docCode = docCode;
    }

    public StringFilter getDocTaskCode() {
        return docTaskCode;
    }

    public StringFilter docTaskCode() {
        if (docTaskCode == null) {
            docTaskCode = new StringFilter();
        }
        return docTaskCode;
    }

    public void setDocTaskCode(StringFilter docTaskCode) {
        this.docTaskCode = docTaskCode;
    }

    public StringFilter getrAccountName() {
        return rAccountName;
    }

    public StringFilter rAccountName() {
        if (rAccountName == null) {
            rAccountName = new StringFilter();
        }
        return rAccountName;
    }

    public void setrAccountName(StringFilter rAccountName) {
        this.rAccountName = rAccountName;
    }

    public StringFilter getConfirmUserID() {
        return confirmUserID;
    }

    public StringFilter confirmUserID() {
        if (confirmUserID == null) {
            confirmUserID = new StringFilter();
        }
        return confirmUserID;
    }

    public void setConfirmUserID(StringFilter confirmUserID) {
        this.confirmUserID = confirmUserID;
    }

    public StringFilter getSendUserID() {
        return sendUserID;
    }

    public StringFilter sendUserID() {
        if (sendUserID == null) {
            sendUserID = new StringFilter();
        }
        return sendUserID;
    }

    public void setSendUserID(StringFilter sendUserID) {
        this.sendUserID = sendUserID;
    }

    public StringFilter getCancelUserID() {
        return cancelUserID;
    }

    public StringFilter cancelUserID() {
        if (cancelUserID == null) {
            cancelUserID = new StringFilter();
        }
        return cancelUserID;
    }

    public void setCancelUserID(StringFilter cancelUserID) {
        this.cancelUserID = cancelUserID;
    }

    public InstantFilter getCreateTime() {
        return createTime;
    }

    public InstantFilter createTime() {
        if (createTime == null) {
            createTime = new InstantFilter();
        }
        return createTime;
    }

    public void setCreateTime(InstantFilter createTime) {
        this.createTime = createTime;
    }

    public InstantFilter getConfirmTime() {
        return confirmTime;
    }

    public InstantFilter confirmTime() {
        if (confirmTime == null) {
            confirmTime = new InstantFilter();
        }
        return confirmTime;
    }

    public void setConfirmTime(InstantFilter confirmTime) {
        this.confirmTime = confirmTime;
    }

    public InstantFilter getSendTime() {
        return sendTime;
    }

    public InstantFilter sendTime() {
        if (sendTime == null) {
            sendTime = new InstantFilter();
        }
        return sendTime;
    }

    public void setSendTime(InstantFilter sendTime) {
        this.sendTime = sendTime;
    }

    public InstantFilter getCancelTime() {
        return cancelTime;
    }

    public InstantFilter cancelTime() {
        if (cancelTime == null) {
            cancelTime = new InstantFilter();
        }
        return cancelTime;
    }

    public void setCancelTime(InstantFilter cancelTime) {
        this.cancelTime = cancelTime;
    }

    public LongFilter getAbsId() {
        return absId;
    }

    public LongFilter absId() {
        if (absId == null) {
            absId = new LongFilter();
        }
        return absId;
    }

    public void setAbsId(LongFilter absId) {
        this.absId = absId;
    }

    public LongFilter getDocId() {
        return docId;
    }

    public LongFilter docId() {
        if (docId == null) {
            docId = new LongFilter();
        }
        return docId;
    }

    public void setDocId(LongFilter docId) {
        this.docId = docId;
    }

    public LongFilter getTemplateId() {
        return templateId;
    }

    public LongFilter templateId() {
        if (templateId == null) {
            templateId = new LongFilter();
        }
        return templateId;
    }

    public void setTemplateId(LongFilter templateId) {
        this.templateId = templateId;
    }

    public LongFilter getControl() {
        return control;
    }

    public LongFilter control() {
        if (control == null) {
            control = new LongFilter();
        }
        return control;
    }

    public void setControl(LongFilter control) {
        this.control = control;
    }

    public StringFilter getResponseText() {
        return responseText;
    }

    public StringFilter responseText() {
        if (responseText == null) {
            responseText = new StringFilter();
        }
        return responseText;
    }

    public void setResponseText(StringFilter responseText) {
        this.responseText = responseText;
    }

    public BookkeepingTransactionStatusFilter getType() {
        return type;
    }

    public BookkeepingTransactionStatusFilter type() {
        if (type == null) {
            type = new BookkeepingTransactionStatusFilter();
        }
        return type;
    }

    public void setType(BookkeepingTransactionStatusFilter type) {
        this.type = type;
    }

    public LongFilter getUnionId() {
        return unionId;
    }

    public LongFilter unionId() {
        if (unionId == null) {
            unionId = new LongFilter();
        }
        return unionId;
    }

    public void setUnionId(LongFilter unionId) {
        this.unionId = unionId;
    }

    public StringFilter getGroupIds() {
        return groupIds;
    }

    public StringFilter groupIds() {
        if (groupIds == null) {
            groupIds = new StringFilter();
        }
        return groupIds;
    }

    public void setGroupIds(StringFilter groupIds) {
        this.groupIds = groupIds;
    }

    public StringFilter getSubRecipient() {
        return subRecipient;
    }

    public StringFilter subRecipient() {
        if (subRecipient == null) {
            subRecipient = new StringFilter();
        }
        return subRecipient;
    }

    public void setSubRecipient(StringFilter subRecipient) {
        this.subRecipient = subRecipient;
    }

    public LongFilter getSubRecipientId() {
        return subRecipientId;
    }

    public LongFilter subRecipientId() {
        if (subRecipientId == null) {
            subRecipientId = new LongFilter();
        }
        return subRecipientId;
    }

    public void setSubRecipientId(LongFilter subRecipientId) {
        this.subRecipientId = subRecipientId;
    }

    public StringFilter getComment() {
        return comment;
    }

    public StringFilter comment() {
        if (comment == null) {
            comment = new StringFilter();
        }
        return comment;
    }

    public void setComment(StringFilter comment) {
        this.comment = comment;
    }

    public BookkeepingPayTypeFilter getBookkeepingPayType() {
        return bookkeepingPayType;
    }

    public BookkeepingPayTypeFilter bookkeepingPayType() {
        if (bookkeepingPayType == null) {
            bookkeepingPayType = new BookkeepingPayTypeFilter();
        }
        return bookkeepingPayType;
    }

    public void setBookkeepingPayType(BookkeepingPayTypeFilter bookkeepingPayType) {
        this.bookkeepingPayType = bookkeepingPayType;
    }

    public StringFilter getParams() {
        return params;
    }

    public StringFilter params() {
        if (params == null) {
            params = new StringFilter();
        }
        return params;
    }

    public void setParams(StringFilter params) {
        this.params = params;
    }

    public LongFilter getGatewayId() {
        return gatewayId;
    }

    public LongFilter gatewayId() {
        if (gatewayId == null) {
            gatewayId = new LongFilter();
        }
        return gatewayId;
    }

    public void setGatewayId(LongFilter gatewayId) {
        this.gatewayId = gatewayId;
    }

    public LongFilter getRecipientId() {
        return recipientId;
    }

    public LongFilter recipientId() {
        if (recipientId == null) {
            recipientId = new LongFilter();
        }
        return recipientId;
    }

    public void setRecipientId(LongFilter recipientId) {
        this.recipientId = recipientId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final BookkeepingTransactionCriteria that = (BookkeepingTransactionCriteria) o;
        return (
            Objects.equals(id, that.id) &&
            Objects.equals(groupId, that.groupId) &&
            Objects.equals(sMfo, that.sMfo) &&
            Objects.equals(sInn, that.sInn) &&
            Objects.equals(sAccount, that.sAccount) &&
            Objects.equals(rMfo, that.rMfo) &&
            Objects.equals(rInn, that.rInn) &&
            Objects.equals(rAccount, that.rAccount) &&
            Objects.equals(amount, that.amount) &&
            Objects.equals(period, that.period) &&
            Objects.equals(bookkeepingType, that.bookkeepingType) &&
            Objects.equals(success, that.success) &&
            Objects.equals(details, that.details) &&
            Objects.equals(docType, that.docType) &&
            Objects.equals(docCode, that.docCode) &&
            Objects.equals(docTaskCode, that.docTaskCode) &&
            Objects.equals(rAccountName, that.rAccountName) &&
            Objects.equals(confirmUserID, that.confirmUserID) &&
            Objects.equals(sendUserID, that.sendUserID) &&
            Objects.equals(cancelUserID, that.cancelUserID) &&
            Objects.equals(createTime, that.createTime) &&
            Objects.equals(confirmTime, that.confirmTime) &&
            Objects.equals(sendTime, that.sendTime) &&
            Objects.equals(cancelTime, that.cancelTime) &&
            Objects.equals(absId, that.absId) &&
            Objects.equals(docId, that.docId) &&
            Objects.equals(templateId, that.templateId) &&
            Objects.equals(control, that.control) &&
            Objects.equals(responseText, that.responseText) &&
            Objects.equals(type, that.type) &&
            Objects.equals(unionId, that.unionId) &&
            Objects.equals(groupIds, that.groupIds) &&
            Objects.equals(subRecipient, that.subRecipient) &&
            Objects.equals(subRecipientId, that.subRecipientId) &&
            Objects.equals(comment, that.comment) &&
            Objects.equals(bookkeepingPayType, that.bookkeepingPayType) &&
            Objects.equals(params, that.params) &&
            Objects.equals(gatewayId, that.gatewayId) &&
            Objects.equals(recipientId, that.recipientId)
        );
    }

    @Override
    public int hashCode() {
        return Objects.hash(
            id,
            groupId,
            sMfo,
            sInn,
            sAccount,
            rMfo,
            rInn,
            rAccount,
            amount,
            period,
            bookkeepingType,
            success,
            details,
            docType,
            docCode,
            docTaskCode,
            rAccountName,
            confirmUserID,
            sendUserID,
            cancelUserID,
            createTime,
            confirmTime,
            sendTime,
            cancelTime,
            absId,
            docId,
            templateId,
            control,
            responseText,
            type,
            unionId,
            groupIds,
            subRecipient,
            subRecipientId,
            comment,
            bookkeepingPayType,
            params,
            gatewayId,
            recipientId
        );
    }

    // prettier-ignore
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
