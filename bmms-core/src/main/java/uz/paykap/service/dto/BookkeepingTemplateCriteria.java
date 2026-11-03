package uz.paykap.service.dto;

import io.github.jhipster.service.filter.Filter;
import java.io.Serializable;
import tech.jhipster.service.filter.LongFilter;
import tech.jhipster.service.filter.StringFilter;
import uz.devops.bmcore.enumeration.PayType;
import uz.paykap.domain.enumeration.BktPeriod;
import uz.paykap.domain.enumeration.BookkeepingAmountCalculateType;
import uz.paykap.domain.enumeration.BookkeepingPayType;
import uz.paykap.domain.enumeration.BookkeepingTransactionType;

/**
 * Criteria class for the BookkeepingTemplate entity. This class is used in BookkeepingTemplateResource to
 * receive all the possible filtering options from the Http GET request parameters.
 * For example the following could be a valid requests:
 * <code> /bookkeeping-templates?id.greaterThan=5&amp;attr1.contains=something&amp;attr2.specified=false</code>
 * As Spring is unable to properly convert the types, unless specific {@link Filter} class are used, we need to use
 * fix type specific filters.
 */
public class BookkeepingTemplateCriteria implements Serializable {

    /**
     * Class for filtering BktPeriod
     */
    public static class BktPeriodFilter extends Filter<BktPeriod> {}

    /**
     * Class for filtering PayType
     */
    public static class PayTypeFilter extends Filter<PayType> {}

    /**
     * Class for filtering BookkeepingPayType
     */
    public static class BookkeepingPayTypeFilter extends Filter<BookkeepingPayType> {}

    /**
     * Class for filtering BookkeepingTransactionType
     */
    public static class BookkeepingTransactionTypeFilter extends Filter<BookkeepingTransactionType> {}

    /**
     * Class for filtering BookkeepingAmountCalculateType
     */
    public static class BookkeepingAmountCalculateTypeFilter extends Filter<BookkeepingAmountCalculateType> {}

    private static final long serialVersionUID = 1L;

    private LongFilter id;

    private LongFilter groupId;

    private StringFilter sMfo;

    private StringFilter sInn;

    private StringFilter sAccount;

    private StringFilter rMfo;

    private StringFilter rInn;

    private StringFilter rAccount;

    private StringFilter calculateAmountRule;

    private BktPeriodFilter period;

    private PayTypeFilter payTypes;

    private StringFilter desc;

    private LongFilter sType;

    private StringFilter sTypeIds;

    private StringFilter docType;

    private StringFilter docCode;

    private StringFilter docTaskCode;

    private StringFilter rAccountName;

    private BookkeepingPayTypeFilter bookkeepingPayType;

    private LongFilter control;

    private BookkeepingTransactionTypeFilter bookkeepingType;

    private StringFilter name;

    private StringFilter groupIds;

    private LongFilter subRecipientId;

    private BookkeepingAmountCalculateTypeFilter amountCalculateType;

    private LongFilter gatewayId;

    private LongFilter recipientId;

    public BookkeepingTemplateCriteria() {}

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

    public StringFilter getCalculateAmountRule() {
        return calculateAmountRule;
    }

    public void setCalculateAmountRule(StringFilter calculateAmountRule) {
        this.calculateAmountRule = calculateAmountRule;
    }

    public BktPeriodFilter getPeriod() {
        return period;
    }

    public void setPeriod(BktPeriodFilter period) {
        this.period = period;
    }

    public PayTypeFilter getPayTypes() {
        return payTypes;
    }

    public void setPayTypes(PayTypeFilter payTypes) {
        this.payTypes = payTypes;
    }

    public StringFilter getDesc() {
        return desc;
    }

    public void setDesc(StringFilter desc) {
        this.desc = desc;
    }

    public LongFilter getsType() {
        return sType;
    }

    public void setsType(LongFilter sType) {
        this.sType = sType;
    }

    public StringFilter getsTypeIds() {
        return sTypeIds;
    }

    public void setsTypeIds(StringFilter sTypeIds) {
        this.sTypeIds = sTypeIds;
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

    public BookkeepingPayTypeFilter getBookkeepingPayType() {
        return bookkeepingPayType;
    }

    public void setBookkeepingPayType(BookkeepingPayTypeFilter bookkeepingPayType) {
        this.bookkeepingPayType = bookkeepingPayType;
    }

    public LongFilter getControl() {
        return control;
    }

    public void setControl(LongFilter control) {
        this.control = control;
    }

    public BookkeepingTransactionTypeFilter getBookkeepingType() {
        return bookkeepingType;
    }

    public void setBookkeepingType(BookkeepingTransactionTypeFilter bookkeepingType) {
        this.bookkeepingType = bookkeepingType;
    }

    public StringFilter getName() {
        return name;
    }

    public void setName(StringFilter name) {
        this.name = name;
    }

    public StringFilter getGroupIds() {
        return groupIds;
    }

    public void setGroupIds(StringFilter groupIds) {
        this.groupIds = groupIds;
    }

    public LongFilter getSubRecipientId() {
        return subRecipientId;
    }

    public void setSubRecipientId(LongFilter subRecipientId) {
        this.subRecipientId = subRecipientId;
    }

    public BookkeepingAmountCalculateTypeFilter getAmountCalculateType() {
        return amountCalculateType;
    }

    public void setAmountCalculateType(BookkeepingAmountCalculateTypeFilter amountCalculateType) {
        this.amountCalculateType = amountCalculateType;
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
        return (
            "BookkeepingTemplateCriteria{" +
            (id != null ? "id=" + id + ", " : "") +
            (groupId != null ? "groupId=" + groupId + ", " : "") +
            (sMfo != null ? "sMfo=" + sMfo + ", " : "") +
            (sInn != null ? "sInn=" + sInn + ", " : "") +
            (sAccount != null ? "sAccount=" + sAccount + ", " : "") +
            (rMfo != null ? "rMfo=" + rMfo + ", " : "") +
            (rInn != null ? "rInn=" + rInn + ", " : "") +
            (rAccount != null ? "rAccount=" + rAccount + ", " : "") +
            (calculateAmountRule != null ? "calculateAmountRule=" + calculateAmountRule + ", " : "") +
            (period != null ? "period=" + period + ", " : "") +
            (payTypes != null ? "payTypes=" + payTypes + ", " : "") +
            (desc != null ? "desc=" + desc + ", " : "") +
            (sType != null ? "sType=" + sType + ", " : "") +
            (sTypeIds != null ? "sTypeIds=" + sTypeIds + ", " : "") +
            (docType != null ? "docType=" + docType + ", " : "") +
            (docCode != null ? "docCode=" + docCode + ", " : "") +
            (docTaskCode != null ? "docTaskCode=" + docTaskCode + ", " : "") +
            (rAccountName != null ? "rAccountName=" + rAccountName + ", " : "") +
            (bookkeepingPayType != null ? "bookkeepingPayType=" + bookkeepingPayType + ", " : "") +
            (control != null ? "control=" + control + ", " : "") +
            (bookkeepingType != null ? "bookkeepingType=" + bookkeepingType + ", " : "") +
            (name != null ? "name=" + name + ", " : "") +
            (groupIds != null ? "groupIds=" + groupIds + ", " : "") +
            (subRecipientId != null ? "subRecipientId=" + subRecipientId + ", " : "") +
            (amountCalculateType != null ? "amountCalculateType=" + amountCalculateType + ", " : "") +
            (gatewayId != null ? "gatewayId=" + gatewayId + ", " : "") +
            (recipientId != null ? "recipientId=" + recipientId + ", " : "") +
            "}"
        );
    }
}
