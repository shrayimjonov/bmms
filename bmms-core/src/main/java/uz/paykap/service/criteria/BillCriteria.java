package uz.paykap.service.criteria;

import java.io.Serializable;
import java.util.Objects;
import tech.jhipster.service.Criteria;
import tech.jhipster.service.filter.BooleanFilter;
import tech.jhipster.service.filter.Filter;
import tech.jhipster.service.filter.InstantFilter;
import tech.jhipster.service.filter.IntegerFilter;
import tech.jhipster.service.filter.LongFilter;
import tech.jhipster.service.filter.StringFilter;
import uz.devops.bmcore.enumeration.RequestType;

/**
 * Criteria class for the {@link uz.paykap.domain.Bill} entity. This class is used
 * in {@link uz.paykap.web.rest.BillResource} to receive all the possible filtering options from
 * the Http GET request parameters.
 * For example the following could be a valid request:
 * {@code /bills?id.greaterThan=5&attr1.contains=something&attr2.specified=false}
 * As Spring is unable to properly convert the types, unless specific {@link Filter} class are used, we need to use
 * fix type specific filters.
 */
public class BillCriteria implements Serializable, Criteria {

    /**
     * Class for filtering RequestType
     */
    public static class RequestTypeFilter extends Filter<RequestType> {

        public RequestTypeFilter() {}

        public RequestTypeFilter(RequestTypeFilter filter) {
            super(filter);
        }

        @Override
        public RequestTypeFilter copy() {
            return new RequestTypeFilter(this);
        }
    }

    private static final long serialVersionUID = 1L;

    private LongFilter id;

    private LongFilter recipientId;

    private StringFilter recipientShortName;

    private StringFilter sessionId;

    private RequestTypeFilter type;

    private IntegerFilter amountRatio;

    private LongFilter amount;

    private LongFilter profit;

    private StringFilter account;

    private StringFilter params;

    private IntegerFilter currency;

    private StringFilter extId;

    private StringFilter checkId;

    private StringFilter payId;

    private InstantFilter requestTime;

    private StringFilter requestJson;

    private StringFilter responseJson;

    private InstantFilter responseTime;

    private StringFilter responseCode;

    private StringFilter responseText;

    private BooleanFilter success;

    private StringFilter login;

    private LongFilter fraudId;

    private LongFilter transactionId;

    private StringFilter outExtId;

    public BillCriteria() {}

    public BillCriteria(BillCriteria other) {
        this.id = other.id == null ? null : other.id.copy();
        this.recipientId = other.recipientId == null ? null : other.recipientId.copy();
        this.recipientShortName = other.recipientShortName == null ? null : other.recipientShortName.copy();
        this.sessionId = other.sessionId == null ? null : other.sessionId.copy();
        this.type = other.type == null ? null : other.type.copy();
        this.amountRatio = other.amountRatio == null ? null : other.amountRatio.copy();
        this.amount = other.amount == null ? null : other.amount.copy();
        this.profit = other.profit == null ? null : other.profit.copy();
        this.account = other.account == null ? null : other.account.copy();
        this.params = other.params == null ? null : other.params.copy();
        this.currency = other.currency == null ? null : other.currency.copy();
        this.extId = other.extId == null ? null : other.extId.copy();
        this.checkId = other.checkId == null ? null : other.checkId.copy();
        this.payId = other.payId == null ? null : other.payId.copy();
        this.requestTime = other.requestTime == null ? null : other.requestTime.copy();
        this.requestJson = other.requestJson == null ? null : other.requestJson.copy();
        this.responseJson = other.responseJson == null ? null : other.responseJson.copy();
        this.responseTime = other.responseTime == null ? null : other.responseTime.copy();
        this.responseCode = other.responseCode == null ? null : other.responseCode.copy();
        this.responseText = other.responseText == null ? null : other.responseText.copy();
        this.success = other.success == null ? null : other.success.copy();
        this.login = other.login == null ? null : other.login.copy();
        this.fraudId = other.fraudId == null ? null : other.fraudId.copy();
        this.transactionId = other.transactionId == null ? null : other.transactionId.copy();
        this.outExtId = other.outExtId == null ? null : other.outExtId.copy();
    }

    @Override
    public BillCriteria copy() {
        return new BillCriteria(this);
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

    public StringFilter getRecipientShortName() {
        return recipientShortName;
    }

    public StringFilter recipientShortName() {
        if (recipientShortName == null) {
            recipientShortName = new StringFilter();
        }
        return recipientShortName;
    }

    public void setRecipientShortName(StringFilter recipientShortName) {
        this.recipientShortName = recipientShortName;
    }

    public StringFilter getSessionId() {
        return sessionId;
    }

    public StringFilter sessionId() {
        if (sessionId == null) {
            sessionId = new StringFilter();
        }
        return sessionId;
    }

    public void setSessionId(StringFilter sessionId) {
        this.sessionId = sessionId;
    }

    public RequestTypeFilter getType() {
        return type;
    }

    public RequestTypeFilter type() {
        if (type == null) {
            type = new RequestTypeFilter();
        }
        return type;
    }

    public void setType(RequestTypeFilter type) {
        this.type = type;
    }

    public IntegerFilter getAmountRatio() {
        return amountRatio;
    }

    public IntegerFilter amountRatio() {
        if (amountRatio == null) {
            amountRatio = new IntegerFilter();
        }
        return amountRatio;
    }

    public void setAmountRatio(IntegerFilter amountRatio) {
        this.amountRatio = amountRatio;
    }

    public LongFilter getAmount() {
        return amount;
    }

    public LongFilter amount() {
        if (amount == null) {
            amount = new LongFilter();
        }
        return amount;
    }

    public void setAmount(LongFilter amount) {
        this.amount = amount;
    }

    public LongFilter getProfit() {
        return profit;
    }

    public LongFilter profit() {
        if (profit == null) {
            profit = new LongFilter();
        }
        return profit;
    }

    public void setProfit(LongFilter profit) {
        this.profit = profit;
    }

    public StringFilter getAccount() {
        return account;
    }

    public StringFilter account() {
        if (account == null) {
            account = new StringFilter();
        }
        return account;
    }

    public void setAccount(StringFilter account) {
        this.account = account;
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

    public IntegerFilter getCurrency() {
        return currency;
    }

    public IntegerFilter currency() {
        if (currency == null) {
            currency = new IntegerFilter();
        }
        return currency;
    }

    public void setCurrency(IntegerFilter currency) {
        this.currency = currency;
    }

    public StringFilter getExtId() {
        return extId;
    }

    public StringFilter extId() {
        if (extId == null) {
            extId = new StringFilter();
        }
        return extId;
    }

    public void setExtId(StringFilter extId) {
        this.extId = extId;
    }

    public StringFilter getCheckId() {
        return checkId;
    }

    public StringFilter checkId() {
        if (checkId == null) {
            checkId = new StringFilter();
        }
        return checkId;
    }

    public void setCheckId(StringFilter checkId) {
        this.checkId = checkId;
    }

    public StringFilter getPayId() {
        return payId;
    }

    public StringFilter payId() {
        if (payId == null) {
            payId = new StringFilter();
        }
        return payId;
    }

    public void setPayId(StringFilter payId) {
        this.payId = payId;
    }

    public InstantFilter getRequestTime() {
        return requestTime;
    }

    public InstantFilter requestTime() {
        if (requestTime == null) {
            requestTime = new InstantFilter();
        }
        return requestTime;
    }

    public void setRequestTime(InstantFilter requestTime) {
        this.requestTime = requestTime;
    }

    public StringFilter getRequestJson() {
        return requestJson;
    }

    public StringFilter requestJson() {
        if (requestJson == null) {
            requestJson = new StringFilter();
        }
        return requestJson;
    }

    public void setRequestJson(StringFilter requestJson) {
        this.requestJson = requestJson;
    }

    public StringFilter getResponseJson() {
        return responseJson;
    }

    public StringFilter responseJson() {
        if (responseJson == null) {
            responseJson = new StringFilter();
        }
        return responseJson;
    }

    public void setResponseJson(StringFilter responseJson) {
        this.responseJson = responseJson;
    }

    public InstantFilter getResponseTime() {
        return responseTime;
    }

    public InstantFilter responseTime() {
        if (responseTime == null) {
            responseTime = new InstantFilter();
        }
        return responseTime;
    }

    public void setResponseTime(InstantFilter responseTime) {
        this.responseTime = responseTime;
    }

    public StringFilter getResponseCode() {
        return responseCode;
    }

    public StringFilter responseCode() {
        if (responseCode == null) {
            responseCode = new StringFilter();
        }
        return responseCode;
    }

    public void setResponseCode(StringFilter responseCode) {
        this.responseCode = responseCode;
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

    public StringFilter getLogin() {
        return login;
    }

    public StringFilter login() {
        if (login == null) {
            login = new StringFilter();
        }
        return login;
    }

    public void setLogin(StringFilter login) {
        this.login = login;
    }

    public LongFilter getFraudId() {
        return fraudId;
    }

    public LongFilter fraudId() {
        if (fraudId == null) {
            fraudId = new LongFilter();
        }
        return fraudId;
    }

    public void setFraudId(LongFilter fraudId) {
        this.fraudId = fraudId;
    }

    public LongFilter getTransactionId() {
        return transactionId;
    }

    public LongFilter transactionId() {
        if (transactionId == null) {
            transactionId = new LongFilter();
        }
        return transactionId;
    }

    public void setTransactionId(LongFilter transactionId) {
        this.transactionId = transactionId;
    }

    public StringFilter getOutExtId() {
        return outExtId;
    }

    public StringFilter outExtId() {
        if (outExtId == null) {
            outExtId = new StringFilter();
        }
        return outExtId;
    }

    public void setOutExtId(StringFilter outExtId) {
        this.outExtId = outExtId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final BillCriteria that = (BillCriteria) o;
        return (
            Objects.equals(id, that.id) &&
            Objects.equals(recipientId, that.recipientId) &&
            Objects.equals(recipientShortName, that.recipientShortName) &&
            Objects.equals(sessionId, that.sessionId) &&
            Objects.equals(type, that.type) &&
            Objects.equals(amountRatio, that.amountRatio) &&
            Objects.equals(amount, that.amount) &&
            Objects.equals(profit, that.profit) &&
            Objects.equals(account, that.account) &&
            Objects.equals(params, that.params) &&
            Objects.equals(currency, that.currency) &&
            Objects.equals(extId, that.extId) &&
            Objects.equals(checkId, that.checkId) &&
            Objects.equals(payId, that.payId) &&
            Objects.equals(requestTime, that.requestTime) &&
            Objects.equals(requestJson, that.requestJson) &&
            Objects.equals(responseJson, that.responseJson) &&
            Objects.equals(responseTime, that.responseTime) &&
            Objects.equals(responseCode, that.responseCode) &&
            Objects.equals(responseText, that.responseText) &&
            Objects.equals(success, that.success) &&
            Objects.equals(login, that.login) &&
            Objects.equals(fraudId, that.fraudId) &&
            Objects.equals(transactionId, that.transactionId) &&
            Objects.equals(outExtId, that.outExtId)
        );
    }

    @Override
    public int hashCode() {
        return Objects.hash(
            id,
            recipientId,
            recipientShortName,
            sessionId,
            type,
            amountRatio,
            amount,
            profit,
            account,
            params,
            currency,
            extId,
            checkId,
            payId,
            requestTime,
            requestJson,
            responseJson,
            responseTime,
            responseCode,
            responseText,
            success,
            login,
            fraudId,
            transactionId,
            outExtId
        );
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "BillCriteria{" +
            (id != null ? "id=" + id + ", " : "") +
            (recipientId != null ? "recipientId=" + recipientId + ", " : "") +
            (recipientShortName != null ? "recipientShortName=" + recipientShortName + ", " : "") +
            (sessionId != null ? "sessionId=" + sessionId + ", " : "") +
            (type != null ? "type=" + type + ", " : "") +
            (amountRatio != null ? "amountRatio=" + amountRatio + ", " : "") +
            (amount != null ? "amount=" + amount + ", " : "") +
            (profit != null ? "profit=" + profit + ", " : "") +
            (account != null ? "account=" + account + ", " : "") +
            (params != null ? "params=" + params + ", " : "") +
            (currency != null ? "currency=" + currency + ", " : "") +
            (extId != null ? "extId=" + extId + ", " : "") +
            (checkId != null ? "checkId=" + checkId + ", " : "") +
            (payId != null ? "payId=" + payId + ", " : "") +
            (requestTime != null ? "requestTime=" + requestTime + ", " : "") +
            (requestJson != null ? "requestJson=" + requestJson + ", " : "") +
            (responseJson != null ? "responseJson=" + responseJson + ", " : "") +
            (responseTime != null ? "responseTime=" + responseTime + ", " : "") +
            (responseCode != null ? "responseCode=" + responseCode + ", " : "") +
            (responseText != null ? "responseText=" + responseText + ", " : "") +
            (success != null ? "success=" + success + ", " : "") +
            (login != null ? "login=" + login + ", " : "") +
            (fraudId != null ? "fraudId=" + fraudId + ", " : "") +
            (transactionId != null ? "transactionId=" + transactionId + ", " : "") +
            (outExtId != null ? "outExtId=" + outExtId + ", " : "") +
            "}";
    }
}
