package uz.paykap.service.dto;

import io.github.jhipster.service.filter.*;
import uz.devops.bmcore.enumeration.PayType;
import uz.devops.bmcore.enumeration.TransactionStatus;

import java.io.Serializable;
import java.util.Objects;


/**
 * Criteria class for the Transaction entity. This class is used in TransactionResource to
 * receive all the possible filtering options from the Http GET request parameters.
 * For example the following could be a valid requests:
 * <code> /transactions?id.greaterThan=5&amp;attr1.contains=something&amp;attr2.specified=false</code>
 * As Spring is unable to properly convert the types, unless specific {@link Filter} class are used, we need to use
 * fix type specific filters.
 */
public class TransactionCriteria implements Serializable {
    /**
     * Class for filtering TransactionStatus
     */
    public static class TransactionStatusFilter extends Filter<TransactionStatus> {
    }

    /**
     * Class for filtering PayType
     */
    public static class PayTypeFilter extends Filter<PayType> {
    }

    private static final long serialVersionUID = 1L;


    private LongFilter id;

    private LongFilter requestAmount;

    private IntegerFilter requestCcy;

    private StringFilter requestTrId;

    private StringFilter requestLogin;

    private StringFilter requestGroup;

    private StringFilter senderName;

    private StringFilter senderAccount;

    private LongFilter senderServiceId;

    private LongFilter senderAmount;

    private IntegerFilter senderCcy;

    private StringFilter senderTrId;

    private BooleanFilter senderSuccess;

    private InstantFilter senderDate;

    private StringFilter senderStatus;

    private StringFilter senderStatusText;

    private LongFilter senderBillId;

    private StringFilter senderGateway;

    private DoubleFilter gatewayAmount;

    private StringFilter recipientName;

    private StringFilter recipientAccount;

    private LongFilter recipientServiceId;

    private LongFilter recipientAmount;

    private IntegerFilter recipientCcy;

    private StringFilter recipientTrId;

    private BooleanFilter recipientSuccess;

    private InstantFilter recipientDate;

    private StringFilter recipientStatus;

    private StringFilter recipientStatusText;

    private LongFilter recipientBillId;

    private StringFilter recipientGateway;

    private LongFilter systemCommissionUpAmount;

    private LongFilter systemCommissionDownAmount;

    private LongFilter systemFeeAmount;

    private LongFilter agentFeeAmount;

    private DoubleFilter roundAmount;

    private DoubleFilter systemFee;

    private IntegerFilter systemCurrency;

    private IntegerFilter ccyRate;

    private TransactionStatusFilter status;

    private StringFilter comment;

    private InstantFilter date;

    private PayTypeFilter payType;

    private StringFilter requestSubAccount;

    private StringFilter eposMerchant;

    private StringFilter eposTerminal;

    private BooleanFilter main;

    private IntegerFilter amountRatio;

    private StringFilter senderBean;

    private StringFilter promo;

    public TransactionCriteria() {
    }

    public LongFilter getId() {
        return id;
    }

    public void setId(LongFilter id) {
        this.id = id;
    }

    public LongFilter getRequestAmount() {
        return requestAmount;
    }

    public void setRequestAmount(LongFilter requestAmount) {
        this.requestAmount = requestAmount;
    }

    public IntegerFilter getRequestCcy() {
        return requestCcy;
    }

    public void setRequestCcy(IntegerFilter requestCcy) {
        this.requestCcy = requestCcy;
    }

    public StringFilter getRequestTrId() {
        return requestTrId;
    }

    public void setRequestTrId(StringFilter requestTrId) {
        this.requestTrId = requestTrId;
    }

    public StringFilter getRequestLogin() {
        return requestLogin;
    }

    public void setRequestLogin(StringFilter requestLogin) {
        this.requestLogin = requestLogin;
    }

    public StringFilter getRequestGroup() {
        return requestGroup;
    }

    public void setRequestGroup(StringFilter requestGroup) {
        this.requestGroup = requestGroup;
    }

    public StringFilter getSenderName() {
        return senderName;
    }

    public void setSenderName(StringFilter senderName) {
        this.senderName = senderName;
    }

    public StringFilter getSenderAccount() {
        return senderAccount;
    }

    public void setSenderAccount(StringFilter senderAccount) {
        this.senderAccount = senderAccount;
    }

    public LongFilter getSenderServiceId() {
        return senderServiceId;
    }

    public void setSenderServiceId(LongFilter senderServiceId) {
        this.senderServiceId = senderServiceId;
    }

    public LongFilter getSenderAmount() {
        return senderAmount;
    }

    public void setSenderAmount(LongFilter senderAmount) {
        this.senderAmount = senderAmount;
    }

    public IntegerFilter getSenderCcy() {
        return senderCcy;
    }

    public void setSenderCcy(IntegerFilter senderCcy) {
        this.senderCcy = senderCcy;
    }

    public StringFilter getSenderTrId() {
        return senderTrId;
    }

    public void setSenderTrId(StringFilter senderTrId) {
        this.senderTrId = senderTrId;
    }

    public BooleanFilter getSenderSuccess() {
        return senderSuccess;
    }

    public void setSenderSuccess(BooleanFilter senderSuccess) {
        this.senderSuccess = senderSuccess;
    }

    public InstantFilter getSenderDate() {
        return senderDate;
    }

    public void setSenderDate(InstantFilter senderDate) {
        this.senderDate = senderDate;
    }

    public StringFilter getSenderStatus() {
        return senderStatus;
    }

    public void setSenderStatus(StringFilter senderStatus) {
        this.senderStatus = senderStatus;
    }

    public StringFilter getSenderStatusText() {
        return senderStatusText;
    }

    public void setSenderStatusText(StringFilter senderStatusText) {
        this.senderStatusText = senderStatusText;
    }

    public LongFilter getSenderBillId() {
        return senderBillId;
    }

    public void setSenderBillId(LongFilter senderBillId) {
        this.senderBillId = senderBillId;
    }

    public StringFilter getSenderGateway() {
        return senderGateway;
    }

    public void setSenderGateway(StringFilter senderGateway) {
        this.senderGateway = senderGateway;
    }

    public DoubleFilter getGatewayAmount() {
        return gatewayAmount;
    }

    public void setGatewayAmount(DoubleFilter gatewayAmount) {
        this.gatewayAmount = gatewayAmount;
    }

    public StringFilter getRecipientName() {
        return recipientName;
    }

    public void setRecipientName(StringFilter recipientName) {
        this.recipientName = recipientName;
    }

    public StringFilter getRecipientAccount() {
        return recipientAccount;
    }

    public void setRecipientAccount(StringFilter recipientAccount) {
        this.recipientAccount = recipientAccount;
    }

    public LongFilter getRecipientServiceId() {
        return recipientServiceId;
    }

    public void setRecipientServiceId(LongFilter recipientServiceId) {
        this.recipientServiceId = recipientServiceId;
    }

    public LongFilter getRecipientAmount() {
        return recipientAmount;
    }

    public void setRecipientAmount(LongFilter recipientAmount) {
        this.recipientAmount = recipientAmount;
    }

    public IntegerFilter getRecipientCcy() {
        return recipientCcy;
    }

    public void setRecipientCcy(IntegerFilter recipientCcy) {
        this.recipientCcy = recipientCcy;
    }

    public StringFilter getRecipientTrId() {
        return recipientTrId;
    }

    public void setRecipientTrId(StringFilter recipientTrId) {
        this.recipientTrId = recipientTrId;
    }

    public BooleanFilter getRecipientSuccess() {
        return recipientSuccess;
    }

    public void setRecipientSuccess(BooleanFilter recipientSuccess) {
        this.recipientSuccess = recipientSuccess;
    }

    public InstantFilter getRecipientDate() {
        return recipientDate;
    }

    public void setRecipientDate(InstantFilter recipientDate) {
        this.recipientDate = recipientDate;
    }

    public StringFilter getRecipientStatus() {
        return recipientStatus;
    }

    public void setRecipientStatus(StringFilter recipientStatus) {
        this.recipientStatus = recipientStatus;
    }

    public StringFilter getRecipientStatusText() {
        return recipientStatusText;
    }

    public void setRecipientStatusText(StringFilter recipientStatusText) {
        this.recipientStatusText = recipientStatusText;
    }

    public LongFilter getRecipientBillId() {
        return recipientBillId;
    }

    public void setRecipientBillId(LongFilter recipientBillId) {
        this.recipientBillId = recipientBillId;
    }

    public StringFilter getRecipientGateway() {
        return recipientGateway;
    }

    public void setRecipientGateway(StringFilter recipientGateway) {
        this.recipientGateway = recipientGateway;
    }

    public LongFilter getSystemCommissionUpAmount() {
        return systemCommissionUpAmount;
    }

    public void setSystemCommissionUpAmount(LongFilter systemCommissionUpAmount) {
        this.systemCommissionUpAmount = systemCommissionUpAmount;
    }

    public LongFilter getSystemCommissionDownAmount() {
        return systemCommissionDownAmount;
    }

    public void setSystemCommissionDownAmount(LongFilter systemCommissionDownAmount) {
        this.systemCommissionDownAmount = systemCommissionDownAmount;
    }

    public LongFilter getSystemFeeAmount() {
        return systemFeeAmount;
    }

    public void setSystemFeeAmount(LongFilter systemFeeAmount) {
        this.systemFeeAmount = systemFeeAmount;
    }

    public LongFilter getAgentFeeAmount() {
        return agentFeeAmount;
    }

    public void setAgentFeeAmount(LongFilter agentFeeAmount) {
        this.agentFeeAmount = agentFeeAmount;
    }

    public DoubleFilter getRoundAmount() {
        return roundAmount;
    }

    public void setRoundAmount(DoubleFilter roundAmount) {
        this.roundAmount = roundAmount;
    }

    public DoubleFilter getSystemFee() {
        return systemFee;
    }

    public void setSystemFee(DoubleFilter systemFee) {
        this.systemFee = systemFee;
    }

    public IntegerFilter getSystemCurrency() {
        return systemCurrency;
    }

    public void setSystemCurrency(IntegerFilter systemCurrency) {
        this.systemCurrency = systemCurrency;
    }

    public IntegerFilter getCcyRate() {
        return ccyRate;
    }

    public void setCcyRate(IntegerFilter ccyRate) {
        this.ccyRate = ccyRate;
    }

    public TransactionStatusFilter getStatus() {
        return status;
    }

    public void setStatus(TransactionStatusFilter status) {
        this.status = status;
    }

    public StringFilter getComment() {
        return comment;
    }

    public void setComment(StringFilter comment) {
        this.comment = comment;
    }

    public InstantFilter getDate() {
        return date;
    }

    public void setDate(InstantFilter date) {
        this.date = date;
    }

    public PayTypeFilter getPayType() {
        return payType;
    }

    public void setPayType(PayTypeFilter payType) {
        this.payType = payType;
    }

    public StringFilter getRequestSubAccount() {
        return requestSubAccount;
    }

    public void setRequestSubAccount(StringFilter requestSubAccount) {
        this.requestSubAccount = requestSubAccount;
    }

    public StringFilter getEposMerchant() {
        return eposMerchant;
    }

    public void setEposMerchant(StringFilter eposMerchant) {
        this.eposMerchant = eposMerchant;
    }

    public StringFilter getEposTerminal() {
        return eposTerminal;
    }

    public void setEposTerminal(StringFilter eposTerminal) {
        this.eposTerminal = eposTerminal;
    }

    public BooleanFilter getMain() {
        return main;
    }

    public void setMain(BooleanFilter main) {
        this.main = main;
    }

    public IntegerFilter getAmountRatio() {
        return amountRatio;
    }

    public void setAmountRatio(IntegerFilter amountRatio) {
        this.amountRatio = amountRatio;
    }

    public StringFilter getSenderBean() {
        return senderBean;
    }

    public void setSenderBean(StringFilter senderBean) {
        this.senderBean = senderBean;
    }

    public StringFilter getPromo() {
        return promo;
    }

    public void setPromo(StringFilter promo) {
        this.promo = promo;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final TransactionCriteria that = (TransactionCriteria) o;
        return
            Objects.equals(id, that.id) &&
                Objects.equals(requestAmount, that.requestAmount) &&
                Objects.equals(requestCcy, that.requestCcy) &&
                Objects.equals(requestTrId, that.requestTrId) &&
                Objects.equals(requestLogin, that.requestLogin) &&
                Objects.equals(requestGroup, that.requestGroup) &&
                Objects.equals(senderName, that.senderName) &&
                Objects.equals(senderAccount, that.senderAccount) &&
                Objects.equals(senderServiceId, that.senderServiceId) &&
                Objects.equals(senderAmount, that.senderAmount) &&
                Objects.equals(senderCcy, that.senderCcy) &&
                Objects.equals(senderTrId, that.senderTrId) &&
                Objects.equals(senderSuccess, that.senderSuccess) &&
                Objects.equals(senderDate, that.senderDate) &&
                Objects.equals(senderStatus, that.senderStatus) &&
                Objects.equals(senderStatusText, that.senderStatusText) &&
                Objects.equals(senderBillId, that.senderBillId) &&
                Objects.equals(senderGateway, that.senderGateway) &&
                Objects.equals(gatewayAmount, that.gatewayAmount) &&
                Objects.equals(recipientName, that.recipientName) &&
                Objects.equals(recipientAccount, that.recipientAccount) &&
                Objects.equals(recipientServiceId, that.recipientServiceId) &&
                Objects.equals(recipientAmount, that.recipientAmount) &&
                Objects.equals(recipientCcy, that.recipientCcy) &&
                Objects.equals(recipientTrId, that.recipientTrId) &&
                Objects.equals(recipientSuccess, that.recipientSuccess) &&
                Objects.equals(recipientDate, that.recipientDate) &&
                Objects.equals(recipientStatus, that.recipientStatus) &&
                Objects.equals(recipientStatusText, that.recipientStatusText) &&
                Objects.equals(recipientBillId, that.recipientBillId) &&
                Objects.equals(recipientGateway, that.recipientGateway) &&
                Objects.equals(systemCommissionDownAmount, that.systemCommissionDownAmount) &&
                Objects.equals(systemCommissionUpAmount, that.systemCommissionUpAmount) &&
                Objects.equals(systemFeeAmount, that.systemFeeAmount) &&
                Objects.equals(roundAmount, that.roundAmount) &&
                Objects.equals(systemFee, that.systemFee) &&
                Objects.equals(systemCurrency, that.systemCurrency) &&
                Objects.equals(ccyRate, that.ccyRate) &&
                Objects.equals(status, that.status) &&
                Objects.equals(comment, that.comment) &&
                Objects.equals(date, that.date) &&
                Objects.equals(payType, that.payType) &&
                Objects.equals(requestSubAccount, that.requestSubAccount) &&
                Objects.equals(eposMerchant, that.eposMerchant) &&
                Objects.equals(eposTerminal, that.eposTerminal) &&
                Objects.equals(senderBean, that.senderBean) &&
                Objects.equals(promo, that.promo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
            id,
            requestAmount,
            requestCcy,
            requestTrId,
            requestLogin,
            requestGroup,
            senderName,
            senderAccount,
            senderServiceId,
            senderAmount,
            senderCcy,
            senderTrId,
            senderSuccess,
            senderDate,
            senderStatus,
            senderStatusText,
            senderBillId,
            senderGateway,
            gatewayAmount,
            recipientName,
            recipientAccount,
            recipientServiceId,
            recipientAmount,
            recipientCcy,
            recipientTrId,
            recipientSuccess,
            recipientDate,
            recipientStatus,
            recipientStatusText,
            recipientBillId,
            recipientGateway,
            systemCommissionDownAmount,
            systemCommissionUpAmount,
            systemFeeAmount,
            roundAmount,
            systemFee,
            systemCurrency,
            ccyRate,
            status,
            comment,
            date,
            payType,
            requestSubAccount,
            eposMerchant,
            eposTerminal,
            senderBean,
            promo
        );
    }

    @Override
    public String toString() {
        return "TransactionCriteria{" +
                (id != null ? "id=" + id + ", " : "") +
                (requestAmount != null ? "requestAmount=" + requestAmount + ", " : "") +
                (requestCcy != null ? "requestCcy=" + requestCcy + ", " : "") +
                (requestTrId != null ? "requestTrId=" + requestTrId + ", " : "") +
                (requestLogin != null ? "requestLogin=" + requestLogin + ", " : "") +
                (requestGroup != null ? "requestGroup=" + requestGroup + ", " : "") +
                (senderName != null ? "senderName=" + senderName + ", " : "") +
                (senderAccount != null ? "senderAccount=" + senderAccount + ", " : "") +
                (senderServiceId != null ? "senderServiceId=" + senderServiceId + ", " : "") +
                (senderAmount != null ? "senderAmount=" + senderAmount + ", " : "") +
                (senderCcy != null ? "senderCcy=" + senderCcy + ", " : "") +
                (senderTrId != null ? "senderTrId=" + senderTrId + ", " : "") +
                (senderSuccess != null ? "senderSuccess=" + senderSuccess + ", " : "") +
                (senderDate != null ? "senderDate=" + senderDate + ", " : "") +
                (senderStatus != null ? "senderStatus=" + senderStatus + ", " : "") +
                (senderStatusText != null ? "senderStatusText=" + senderStatusText + ", " : "") +
                (senderBillId != null ? "senderBillId=" + senderBillId + ", " : "") +
                (senderGateway != null ? "senderGateway=" + senderGateway + ", " : "") +
                (gatewayAmount != null ? "gatewayAmount=" + gatewayAmount + ", " : "") +
                (recipientName != null ? "recipientName=" + recipientName + ", " : "") +
                (recipientAccount != null ? "recipientAccount=" + recipientAccount + ", " : "") +
            (recipientServiceId != null ? "recipientServiceId=" + recipientServiceId + ", " : "") +
            (recipientAmount != null ? "recipientAmount=" + recipientAmount + ", " : "") +
            (recipientCcy != null ? "recipientCcy=" + recipientCcy + ", " : "") +
            (recipientTrId != null ? "recipientTrId=" + recipientTrId + ", " : "") +
            (recipientSuccess != null ? "recipientSuccess=" + recipientSuccess + ", " : "") +
            (recipientDate != null ? "recipientDate=" + recipientDate + ", " : "") +
            (recipientStatus != null ? "recipientStatus=" + recipientStatus + ", " : "") +
            (recipientStatusText != null ? "recipientStatusText=" + recipientStatusText + ", " : "") +
            (recipientBillId != null ? "recipientBillId=" + recipientBillId + ", " : "") +
            (recipientGateway != null ? "recipientGateway=" + recipientGateway + ", " : "") +
            (systemCommissionUpAmount != null ? "systemCommissionUpAmount=" + systemCommissionUpAmount + ", " : "") +
            (systemCommissionDownAmount != null ? "systemCommissionDownAmount=" + systemCommissionDownAmount + ", " : "") +
            (systemFeeAmount != null ? "systemFeeAmount=" + systemFeeAmount + ", " : "") +
            (agentFeeAmount != null ? "agentFeeAmount=" + agentFeeAmount + ", " : "") +
            (roundAmount != null ? "roundAmount=" + roundAmount + ", " : "") +
            (systemFee != null ? "systemFee=" + systemFee + ", " : "") +
            (systemCurrency != null ? "systemCurrency=" + systemCurrency + ", " : "") +
            (ccyRate != null ? "ccyRate=" + ccyRate + ", " : "") +
            (status != null ? "status=" + status + ", " : "") +
            (comment != null ? "comment=" + comment + ", " : "") +
            (date != null ? "date=" + date + ", " : "") +
            (payType != null ? "payType=" + payType + ", " : "") +
            (requestSubAccount != null ? "requestSubAccount=" + requestSubAccount + ", " : "") +
            (eposMerchant != null ? "eposMerchant=" + eposMerchant + ", " : "") +
                (eposTerminal != null ? "eposTerminal=" + eposTerminal + ", " : "") +
                (main != null ? "main=" + main + ", " : "") +
                (amountRatio != null ? "amountRatio=" + amountRatio + ", " : "") +
            (senderBean != null ? "senderBean=" + senderBean + ", " : "") +
            (promo != null ? "promo=" + promo + ", " : "") +
            "}";
    }

}
