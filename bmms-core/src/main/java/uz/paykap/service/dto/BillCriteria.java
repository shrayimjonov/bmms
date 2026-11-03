package uz.paykap.service.dto;

import io.github.jhipster.service.filter.*;
import uz.devops.bmcore.enumeration.RequestType;

import java.io.Serializable;
import java.util.Objects;

/**
 * Criteria class for the Bill entity. This class is used in BillResource to
 * receive all the possible filtering options from the Http GET request parameters.
 * For example the following could be a valid requests:
 * <code> /bills?id.greaterThan=5&amp;attr1.contains=something&amp;attr2.specified=false</code>
 * As Spring is unable to properly convert the types, unless specific {@link Filter} class are used, we need to use
 * fix type specific filters.
 */
public class BillCriteria implements Serializable {
    /**
     * Class for filtering RequestType
     */
    public static class RequestTypeFilter extends Filter<RequestType> {
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

    public LongFilter getId() {
        return id;
    }

    public void setId(LongFilter id) {
        this.id = id;
    }

    public LongFilter getRecipientId() {
        return recipientId;
    }

    public void setRecipientId(LongFilter recipientId) {
        this.recipientId = recipientId;
    }

    public StringFilter getRecipientShortName() {
        return recipientShortName;
    }

    public void setRecipientShortName(StringFilter recipientShortName) {
        this.recipientShortName = recipientShortName;
    }

    public StringFilter getSessionId() {
        return sessionId;
    }

    public void setSessionId(StringFilter sessionId) {
        this.sessionId = sessionId;
    }

    public RequestTypeFilter getType() {
        return type;
    }

    public void setType(RequestTypeFilter type) {
        this.type = type;
    }

    public IntegerFilter getAmountRatio() {
        return amountRatio;
    }

    public void setAmountRatio(IntegerFilter amountRatio) {
        this.amountRatio = amountRatio;
    }

    public LongFilter getAmount() {
        return amount;
    }

    public void setAmount(LongFilter amount) {
        this.amount = amount;
    }

    public LongFilter getProfit() {
        return profit;
    }

    public void setProfit(LongFilter profit) {
        this.profit = profit;
    }

    public StringFilter getAccount() {
        return account;
    }

    public void setAccount(StringFilter account) {
        this.account = account;
    }

    public StringFilter getParams() {
        return params;
    }

    public void setParams(StringFilter params) {
        this.params = params;
    }

    public IntegerFilter getCurrency() {
        return currency;
    }

    public void setCurrency(IntegerFilter currency) {
        this.currency = currency;
    }

    public StringFilter getExtId() {
        return extId;
    }

    public void setExtId(StringFilter extId) {
        this.extId = extId;
    }

    public StringFilter getCheckId() {
        return checkId;
    }

    public void setCheckId(StringFilter checkId) {
        this.checkId = checkId;
    }

    public StringFilter getPayId() {
        return payId;
    }

    public void setPayId(StringFilter payId) {
        this.payId = payId;
    }

    public InstantFilter getRequestTime() {
        return requestTime;
    }

    public void setRequestTime(InstantFilter requestTime) {
        this.requestTime = requestTime;
    }

    public StringFilter getRequestJson() {
        return requestJson;
    }

    public void setRequestJson(StringFilter requestJson) {
        this.requestJson = requestJson;
    }

    public StringFilter getResponseJson() {
        return responseJson;
    }

    public void setResponseJson(StringFilter responseJson) {
        this.responseJson = responseJson;
    }

    public InstantFilter getResponseTime() {
        return responseTime;
    }

    public void setResponseTime(InstantFilter responseTime) {
        this.responseTime = responseTime;
    }

    public StringFilter getResponseCode() {
        return responseCode;
    }

    public void setResponseCode(StringFilter responseCode) {
        this.responseCode = responseCode;
    }

    public StringFilter getResponseText() {
        return responseText;
    }

    public void setResponseText(StringFilter responseText) {
        this.responseText = responseText;
    }

    public BooleanFilter getSuccess() {
        return success;
    }

    public void setSuccess(BooleanFilter success) {
        this.success = success;
    }

    public StringFilter getLogin() {
        return login;
    }

    public void setLogin(StringFilter login) {
        this.login = login;
    }

    public LongFilter getFraudId() {
        return fraudId;
    }

    public void setFraudId(LongFilter fraudId) {
        this.fraudId = fraudId;
    }

    public LongFilter getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(LongFilter transactionId) {
        this.transactionId = transactionId;
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
        return
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
                Objects.equals(fraudId, that.fraudId);
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
            fraudId
        );
    }
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
            "}";
    }

}
