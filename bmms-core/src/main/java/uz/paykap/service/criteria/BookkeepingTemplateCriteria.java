package uz.paykap.service.criteria;

import java.io.Serializable;
import java.util.Objects;
import tech.jhipster.service.Criteria;
import tech.jhipster.service.filter.BooleanFilter;
import tech.jhipster.service.filter.DoubleFilter;
import tech.jhipster.service.filter.Filter;
import tech.jhipster.service.filter.FloatFilter;
import tech.jhipster.service.filter.IntegerFilter;
import tech.jhipster.service.filter.LongFilter;
import tech.jhipster.service.filter.StringFilter;
import uz.paykap.domain.enumeration.BktPeriod;
import uz.paykap.domain.enumeration.BookkeepingAmountCalculateType;
import uz.paykap.domain.enumeration.BookkeepingPayType;
import uz.paykap.domain.enumeration.BookkeepingTransactionType;

/**
 * Criteria class for the {@link uz.paykap.domain.BookkeepingTemplate} entity. This class is used
 * in {@link uz.paykap.web.rest.BookkeepingTemplateResource} to receive all the possible filtering options from
 * the Http GET request parameters.
 * For example the following could be a valid request:
 * {@code /bookkeeping-templates?id.greaterThan=5&attr1.contains=something&attr2.specified=false}
 * As Spring is unable to properly convert the types, unless specific {@link Filter} class are used, we need to use
 * fix type specific filters.
 */
public class BookkeepingTemplateCriteria implements Serializable, Criteria {

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
     * Class for filtering BookkeepingAmountCalculateType
     */
    public static class BookkeepingAmountCalculateTypeFilter extends Filter<BookkeepingAmountCalculateType> {

        public BookkeepingAmountCalculateTypeFilter() {}

        public BookkeepingAmountCalculateTypeFilter(BookkeepingAmountCalculateTypeFilter filter) {
            super(filter);
        }

        @Override
        public BookkeepingAmountCalculateTypeFilter copy() {
            return new BookkeepingAmountCalculateTypeFilter(this);
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

    private StringFilter calculateAmountRule;

    private BktPeriodFilter period;

    private StringFilter payTypes;

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

    public BookkeepingTemplateCriteria(BookkeepingTemplateCriteria other) {
        this.id = other.id == null ? null : other.id.copy();
        this.groupId = other.groupId == null ? null : other.groupId.copy();
        this.sMfo = other.sMfo == null ? null : other.sMfo.copy();
        this.sInn = other.sInn == null ? null : other.sInn.copy();
        this.sAccount = other.sAccount == null ? null : other.sAccount.copy();
        this.rMfo = other.rMfo == null ? null : other.rMfo.copy();
        this.rInn = other.rInn == null ? null : other.rInn.copy();
        this.rAccount = other.rAccount == null ? null : other.rAccount.copy();
        this.calculateAmountRule = other.calculateAmountRule == null ? null : other.calculateAmountRule.copy();
        this.period = other.period == null ? null : other.period.copy();
        this.payTypes = other.payTypes == null ? null : other.payTypes.copy();
        this.desc = other.desc == null ? null : other.desc.copy();
        this.sType = other.sType == null ? null : other.sType.copy();
        this.sTypeIds = other.sTypeIds == null ? null : other.sTypeIds.copy();
        this.docType = other.docType == null ? null : other.docType.copy();
        this.docCode = other.docCode == null ? null : other.docCode.copy();
        this.docTaskCode = other.docTaskCode == null ? null : other.docTaskCode.copy();
        this.rAccountName = other.rAccountName == null ? null : other.rAccountName.copy();
        this.bookkeepingPayType = other.bookkeepingPayType == null ? null : other.bookkeepingPayType.copy();
        this.control = other.control == null ? null : other.control.copy();
        this.bookkeepingType = other.bookkeepingType == null ? null : other.bookkeepingType.copy();
        this.name = other.name == null ? null : other.name.copy();
        this.groupIds = other.groupIds == null ? null : other.groupIds.copy();
        this.subRecipientId = other.subRecipientId == null ? null : other.subRecipientId.copy();
        this.amountCalculateType = other.amountCalculateType == null ? null : other.amountCalculateType.copy();
        this.gatewayId = other.gatewayId == null ? null : other.gatewayId.copy();
        this.recipientId = other.recipientId == null ? null : other.recipientId.copy();
    }

    @Override
    public BookkeepingTemplateCriteria copy() {
        return new BookkeepingTemplateCriteria(this);
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

    public StringFilter getCalculateAmountRule() {
        return calculateAmountRule;
    }

    public StringFilter calculateAmountRule() {
        if (calculateAmountRule == null) {
            calculateAmountRule = new StringFilter();
        }
        return calculateAmountRule;
    }

    public void setCalculateAmountRule(StringFilter calculateAmountRule) {
        this.calculateAmountRule = calculateAmountRule;
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

    public StringFilter getPayTypes() {
        return payTypes;
    }

    public StringFilter payTypes() {
        if (payTypes == null) {
            payTypes = new StringFilter();
        }
        return payTypes;
    }

    public void setPayTypes(StringFilter payTypes) {
        this.payTypes = payTypes;
    }

    public StringFilter getDesc() {
        return desc;
    }

    public StringFilter desc() {
        if (desc == null) {
            desc = new StringFilter();
        }
        return desc;
    }

    public void setDesc(StringFilter desc) {
        this.desc = desc;
    }

    public LongFilter getsType() {
        return sType;
    }

    public LongFilter sType() {
        if (sType == null) {
            sType = new LongFilter();
        }
        return sType;
    }

    public void setsType(LongFilter sType) {
        this.sType = sType;
    }

    public StringFilter getsTypeIds() {
        return sTypeIds;
    }

    public StringFilter sTypeIds() {
        if (sTypeIds == null) {
            sTypeIds = new StringFilter();
        }
        return sTypeIds;
    }

    public void setsTypeIds(StringFilter sTypeIds) {
        this.sTypeIds = sTypeIds;
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

    public StringFilter getName() {
        return name;
    }

    public StringFilter name() {
        if (name == null) {
            name = new StringFilter();
        }
        return name;
    }

    public void setName(StringFilter name) {
        this.name = name;
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

    public BookkeepingAmountCalculateTypeFilter getAmountCalculateType() {
        return amountCalculateType;
    }

    public BookkeepingAmountCalculateTypeFilter amountCalculateType() {
        if (amountCalculateType == null) {
            amountCalculateType = new BookkeepingAmountCalculateTypeFilter();
        }
        return amountCalculateType;
    }

    public void setAmountCalculateType(BookkeepingAmountCalculateTypeFilter amountCalculateType) {
        this.amountCalculateType = amountCalculateType;
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
        final BookkeepingTemplateCriteria that = (BookkeepingTemplateCriteria) o;
        return (
            Objects.equals(id, that.id) &&
            Objects.equals(groupId, that.groupId) &&
            Objects.equals(sMfo, that.sMfo) &&
            Objects.equals(sInn, that.sInn) &&
            Objects.equals(sAccount, that.sAccount) &&
            Objects.equals(rMfo, that.rMfo) &&
            Objects.equals(rInn, that.rInn) &&
            Objects.equals(rAccount, that.rAccount) &&
            Objects.equals(calculateAmountRule, that.calculateAmountRule) &&
            Objects.equals(period, that.period) &&
            Objects.equals(payTypes, that.payTypes) &&
            Objects.equals(desc, that.desc) &&
            Objects.equals(sType, that.sType) &&
            Objects.equals(sTypeIds, that.sTypeIds) &&
            Objects.equals(docType, that.docType) &&
            Objects.equals(docCode, that.docCode) &&
            Objects.equals(docTaskCode, that.docTaskCode) &&
            Objects.equals(rAccountName, that.rAccountName) &&
            Objects.equals(bookkeepingPayType, that.bookkeepingPayType) &&
            Objects.equals(control, that.control) &&
            Objects.equals(bookkeepingType, that.bookkeepingType) &&
            Objects.equals(name, that.name) &&
            Objects.equals(groupIds, that.groupIds) &&
            Objects.equals(subRecipientId, that.subRecipientId) &&
            Objects.equals(amountCalculateType, that.amountCalculateType) &&
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
            calculateAmountRule,
            period,
            payTypes,
            desc,
            sType,
            sTypeIds,
            docType,
            docCode,
            docTaskCode,
            rAccountName,
            bookkeepingPayType,
            control,
            bookkeepingType,
            name,
            groupIds,
            subRecipientId,
            amountCalculateType,
            gatewayId,
            recipientId
        );
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "BookkeepingTemplateCriteria{" +
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
            "}";
    }
}
