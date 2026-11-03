package uz.paykap.service.criteria;

import tech.jhipster.service.Criteria;
import tech.jhipster.service.filter.*;
import uz.devops.bmcore.enumeration.PayType;
import uz.devops.bmcore.enumeration.TransactionStatus;

import java.io.Serializable;
import java.util.Objects;

/**
 * Criteria class for the {@link uz.paykap.domain.Transaction} entity. This class is used
 * in {@link uz.paykap.web.rest.TransactionResource} to receive all the possible filtering options from
 * the Http GET request parameters.
 * For example the following could be a valid request:
 * {@code /transactions?id.greaterThan=5&attr1.contains=something&attr2.specified=false}
 * As Spring is unable to properly convert the types, unless specific {@link Filter} class are used, we need to use
 * fix type specific filters.
 */
public class TransactionCriteria implements Serializable, Criteria {

    /**
     * Class for filtering TransactionStatus
     */
    public static class TransactionStatusFilter extends Filter<TransactionStatus> {

        public TransactionStatusFilter() {}

        public TransactionStatusFilter(TransactionStatusFilter filter) {
            super(filter);
        }

        @Override
        public TransactionStatusFilter copy() {
            return new TransactionStatusFilter(this);
        }
    }

    /**
     * Class for filtering PayType
     */
    public static class PayTypeFilter extends Filter<PayType> {

        public PayTypeFilter() {}

        public PayTypeFilter(PayTypeFilter filter) {
            super(filter);
        }

        @Override
        public PayTypeFilter copy() {
            return new PayTypeFilter(this);
        }
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

    private StringFilter category;

    private StringFilter promo;

    public TransactionCriteria() {}

    public TransactionCriteria(TransactionCriteria other) {
        this.id = other.id == null ? null : other.id.copy();
        this.requestAmount = other.requestAmount == null ? null : other.requestAmount.copy();
        this.requestCcy = other.requestCcy == null ? null : other.requestCcy.copy();
        this.requestTrId = other.requestTrId == null ? null : other.requestTrId.copy();
        this.requestLogin = other.requestLogin == null ? null : other.requestLogin.copy();
        this.requestGroup = other.requestGroup == null ? null : other.requestGroup.copy();
        this.senderName = other.senderName == null ? null : other.senderName.copy();
        this.senderAccount = other.senderAccount == null ? null : other.senderAccount.copy();
        this.senderServiceId = other.senderServiceId == null ? null : other.senderServiceId.copy();
        this.senderAmount = other.senderAmount == null ? null : other.senderAmount.copy();
        this.senderCcy = other.senderCcy == null ? null : other.senderCcy.copy();
        this.senderTrId = other.senderTrId == null ? null : other.senderTrId.copy();
        this.senderSuccess = other.senderSuccess == null ? null : other.senderSuccess.copy();
        this.senderDate = other.senderDate == null ? null : other.senderDate.copy();
        this.senderStatus = other.senderStatus == null ? null : other.senderStatus.copy();
        this.senderStatusText = other.senderStatusText == null ? null : other.senderStatusText.copy();
        this.senderBillId = other.senderBillId == null ? null : other.senderBillId.copy();
        this.senderGateway = other.senderGateway == null ? null : other.senderGateway.copy();
        this.gatewayAmount = other.gatewayAmount == null ? null : other.gatewayAmount.copy();
        this.recipientName = other.recipientName == null ? null : other.recipientName.copy();
        this.recipientAccount = other.recipientAccount == null ? null : other.recipientAccount.copy();
        this.recipientServiceId = other.recipientServiceId == null ? null : other.recipientServiceId.copy();
        this.recipientAmount = other.recipientAmount == null ? null : other.recipientAmount.copy();
        this.recipientCcy = other.recipientCcy == null ? null : other.recipientCcy.copy();
        this.recipientTrId = other.recipientTrId == null ? null : other.recipientTrId.copy();
        this.recipientSuccess = other.recipientSuccess == null ? null : other.recipientSuccess.copy();
        this.recipientDate = other.recipientDate == null ? null : other.recipientDate.copy();
        this.recipientStatus = other.recipientStatus == null ? null : other.recipientStatus.copy();
        this.recipientStatusText = other.recipientStatusText == null ? null : other.recipientStatusText.copy();
        this.recipientBillId = other.recipientBillId == null ? null : other.recipientBillId.copy();
        this.recipientGateway = other.recipientGateway == null ? null : other.recipientGateway.copy();
        this.systemCommissionUpAmount = other.systemCommissionUpAmount == null ? null : other.systemCommissionUpAmount.copy();
        this.systemCommissionDownAmount = other.systemCommissionDownAmount == null ? null : other.systemCommissionDownAmount.copy();
        this.systemFeeAmount = other.systemFeeAmount == null ? null : other.systemFeeAmount.copy();
        this.agentFeeAmount = other.agentFeeAmount == null ? null : other.agentFeeAmount.copy();
        this.roundAmount = other.roundAmount == null ? null : other.roundAmount.copy();
        this.systemFee = other.systemFee == null ? null : other.systemFee.copy();
        this.systemCurrency = other.systemCurrency == null ? null : other.systemCurrency.copy();
        this.ccyRate = other.ccyRate == null ? null : other.ccyRate.copy();
        this.status = other.status == null ? null : other.status.copy();
        this.comment = other.comment == null ? null : other.comment.copy();
        this.date = other.date == null ? null : other.date.copy();
        this.payType = other.payType == null ? null : other.payType.copy();
        this.requestSubAccount = other.requestSubAccount == null ? null : other.requestSubAccount.copy();
        this.eposMerchant = other.eposMerchant == null ? null : other.eposMerchant.copy();
        this.eposTerminal = other.eposTerminal == null ? null : other.eposTerminal.copy();
        this.main = other.main == null ? null : other.main.copy();
        this.amountRatio = other.amountRatio == null ? null : other.amountRatio.copy();
        this.senderBean = other.senderBean == null ? null : other.senderBean.copy();
        this.category = other.category == null ? null : other.category.copy();
        this.promo = other.promo == null ? null : other.promo.copy();
    }

    @Override
    public TransactionCriteria copy() {
        return new TransactionCriteria(this);
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

    public LongFilter getRequestAmount() {
        return requestAmount;
    }

    public LongFilter requestAmount() {
        if (requestAmount == null) {
            requestAmount = new LongFilter();
        }
        return requestAmount;
    }

    public void setRequestAmount(LongFilter requestAmount) {
        this.requestAmount = requestAmount;
    }

    public IntegerFilter getRequestCcy() {
        return requestCcy;
    }

    public IntegerFilter requestCcy() {
        if (requestCcy == null) {
            requestCcy = new IntegerFilter();
        }
        return requestCcy;
    }

    public void setRequestCcy(IntegerFilter requestCcy) {
        this.requestCcy = requestCcy;
    }

    public StringFilter getRequestTrId() {
        return requestTrId;
    }

    public StringFilter requestTrId() {
        if (requestTrId == null) {
            requestTrId = new StringFilter();
        }
        return requestTrId;
    }

    public void setRequestTrId(StringFilter requestTrId) {
        this.requestTrId = requestTrId;
    }

    public StringFilter getRequestLogin() {
        return requestLogin;
    }

    public StringFilter requestLogin() {
        if (requestLogin == null) {
            requestLogin = new StringFilter();
        }
        return requestLogin;
    }

    public void setRequestLogin(StringFilter requestLogin) {
        this.requestLogin = requestLogin;
    }

    public StringFilter getRequestGroup() {
        return requestGroup;
    }

    public StringFilter requestGroup() {
        if (requestGroup == null) {
            requestGroup = new StringFilter();
        }
        return requestGroup;
    }

    public void setRequestGroup(StringFilter requestGroup) {
        this.requestGroup = requestGroup;
    }

    public StringFilter getSenderName() {
        return senderName;
    }

    public StringFilter senderName() {
        if (senderName == null) {
            senderName = new StringFilter();
        }
        return senderName;
    }

    public void setSenderName(StringFilter senderName) {
        this.senderName = senderName;
    }

    public StringFilter getSenderAccount() {
        return senderAccount;
    }

    public StringFilter senderAccount() {
        if (senderAccount == null) {
            senderAccount = new StringFilter();
        }
        return senderAccount;
    }

    public void setSenderAccount(StringFilter senderAccount) {
        this.senderAccount = senderAccount;
    }

    public LongFilter getSenderServiceId() {
        return senderServiceId;
    }

    public LongFilter senderServiceId() {
        if (senderServiceId == null) {
            senderServiceId = new LongFilter();
        }
        return senderServiceId;
    }

    public void setSenderServiceId(LongFilter senderServiceId) {
        this.senderServiceId = senderServiceId;
    }

    public LongFilter getSenderAmount() {
        return senderAmount;
    }

    public LongFilter senderAmount() {
        if (senderAmount == null) {
            senderAmount = new LongFilter();
        }
        return senderAmount;
    }

    public void setSenderAmount(LongFilter senderAmount) {
        this.senderAmount = senderAmount;
    }

    public IntegerFilter getSenderCcy() {
        return senderCcy;
    }

    public IntegerFilter senderCcy() {
        if (senderCcy == null) {
            senderCcy = new IntegerFilter();
        }
        return senderCcy;
    }

    public void setSenderCcy(IntegerFilter senderCcy) {
        this.senderCcy = senderCcy;
    }

    public StringFilter getSenderTrId() {
        return senderTrId;
    }

    public StringFilter senderTrId() {
        if (senderTrId == null) {
            senderTrId = new StringFilter();
        }
        return senderTrId;
    }

    public void setSenderTrId(StringFilter senderTrId) {
        this.senderTrId = senderTrId;
    }

    public BooleanFilter getSenderSuccess() {
        return senderSuccess;
    }

    public BooleanFilter senderSuccess() {
        if (senderSuccess == null) {
            senderSuccess = new BooleanFilter();
        }
        return senderSuccess;
    }

    public void setSenderSuccess(BooleanFilter senderSuccess) {
        this.senderSuccess = senderSuccess;
    }

    public InstantFilter getSenderDate() {
        return senderDate;
    }

    public InstantFilter senderDate() {
        if (senderDate == null) {
            senderDate = new InstantFilter();
        }
        return senderDate;
    }

    public void setSenderDate(InstantFilter senderDate) {
        this.senderDate = senderDate;
    }

    public StringFilter getSenderStatus() {
        return senderStatus;
    }

    public StringFilter senderStatus() {
        if (senderStatus == null) {
            senderStatus = new StringFilter();
        }
        return senderStatus;
    }

    public void setSenderStatus(StringFilter senderStatus) {
        this.senderStatus = senderStatus;
    }

    public StringFilter getSenderStatusText() {
        return senderStatusText;
    }

    public StringFilter senderStatusText() {
        if (senderStatusText == null) {
            senderStatusText = new StringFilter();
        }
        return senderStatusText;
    }

    public void setSenderStatusText(StringFilter senderStatusText) {
        this.senderStatusText = senderStatusText;
    }

    public LongFilter getSenderBillId() {
        return senderBillId;
    }

    public LongFilter senderBillId() {
        if (senderBillId == null) {
            senderBillId = new LongFilter();
        }
        return senderBillId;
    }

    public void setSenderBillId(LongFilter senderBillId) {
        this.senderBillId = senderBillId;
    }

    public StringFilter getSenderGateway() {
        return senderGateway;
    }

    public StringFilter senderGateway() {
        if (senderGateway == null) {
            senderGateway = new StringFilter();
        }
        return senderGateway;
    }

    public void setSenderGateway(StringFilter senderGateway) {
        this.senderGateway = senderGateway;
    }

    public DoubleFilter getGatewayAmount() {
        return gatewayAmount;
    }

    public DoubleFilter gatewayAmount() {
        if (gatewayAmount == null) {
            gatewayAmount = new DoubleFilter();
        }
        return gatewayAmount;
    }

    public void setGatewayAmount(DoubleFilter gatewayAmount) {
        this.gatewayAmount = gatewayAmount;
    }

    public StringFilter getRecipientName() {
        return recipientName;
    }

    public StringFilter recipientName() {
        if (recipientName == null) {
            recipientName = new StringFilter();
        }
        return recipientName;
    }

    public void setRecipientName(StringFilter recipientName) {
        this.recipientName = recipientName;
    }

    public StringFilter getRecipientAccount() {
        return recipientAccount;
    }

    public StringFilter recipientAccount() {
        if (recipientAccount == null) {
            recipientAccount = new StringFilter();
        }
        return recipientAccount;
    }

    public void setRecipientAccount(StringFilter recipientAccount) {
        this.recipientAccount = recipientAccount;
    }

    public LongFilter getRecipientServiceId() {
        return recipientServiceId;
    }

    public LongFilter recipientServiceId() {
        if (recipientServiceId == null) {
            recipientServiceId = new LongFilter();
        }
        return recipientServiceId;
    }

    public void setRecipientServiceId(LongFilter recipientServiceId) {
        this.recipientServiceId = recipientServiceId;
    }

    public LongFilter getRecipientAmount() {
        return recipientAmount;
    }

    public LongFilter recipientAmount() {
        if (recipientAmount == null) {
            recipientAmount = new LongFilter();
        }
        return recipientAmount;
    }

    public void setRecipientAmount(LongFilter recipientAmount) {
        this.recipientAmount = recipientAmount;
    }

    public IntegerFilter getRecipientCcy() {
        return recipientCcy;
    }

    public IntegerFilter recipientCcy() {
        if (recipientCcy == null) {
            recipientCcy = new IntegerFilter();
        }
        return recipientCcy;
    }

    public void setRecipientCcy(IntegerFilter recipientCcy) {
        this.recipientCcy = recipientCcy;
    }

    public StringFilter getRecipientTrId() {
        return recipientTrId;
    }

    public StringFilter recipientTrId() {
        if (recipientTrId == null) {
            recipientTrId = new StringFilter();
        }
        return recipientTrId;
    }

    public void setRecipientTrId(StringFilter recipientTrId) {
        this.recipientTrId = recipientTrId;
    }

    public BooleanFilter getRecipientSuccess() {
        return recipientSuccess;
    }

    public BooleanFilter recipientSuccess() {
        if (recipientSuccess == null) {
            recipientSuccess = new BooleanFilter();
        }
        return recipientSuccess;
    }

    public void setRecipientSuccess(BooleanFilter recipientSuccess) {
        this.recipientSuccess = recipientSuccess;
    }

    public InstantFilter getRecipientDate() {
        return recipientDate;
    }

    public InstantFilter recipientDate() {
        if (recipientDate == null) {
            recipientDate = new InstantFilter();
        }
        return recipientDate;
    }

    public void setRecipientDate(InstantFilter recipientDate) {
        this.recipientDate = recipientDate;
    }

    public StringFilter getRecipientStatus() {
        return recipientStatus;
    }

    public StringFilter recipientStatus() {
        if (recipientStatus == null) {
            recipientStatus = new StringFilter();
        }
        return recipientStatus;
    }

    public void setRecipientStatus(StringFilter recipientStatus) {
        this.recipientStatus = recipientStatus;
    }

    public StringFilter getRecipientStatusText() {
        return recipientStatusText;
    }

    public StringFilter recipientStatusText() {
        if (recipientStatusText == null) {
            recipientStatusText = new StringFilter();
        }
        return recipientStatusText;
    }

    public void setRecipientStatusText(StringFilter recipientStatusText) {
        this.recipientStatusText = recipientStatusText;
    }

    public LongFilter getRecipientBillId() {
        return recipientBillId;
    }

    public LongFilter recipientBillId() {
        if (recipientBillId == null) {
            recipientBillId = new LongFilter();
        }
        return recipientBillId;
    }

    public void setRecipientBillId(LongFilter recipientBillId) {
        this.recipientBillId = recipientBillId;
    }

    public StringFilter getRecipientGateway() {
        return recipientGateway;
    }

    public StringFilter recipientGateway() {
        if (recipientGateway == null) {
            recipientGateway = new StringFilter();
        }
        return recipientGateway;
    }

    public void setRecipientGateway(StringFilter recipientGateway) {
        this.recipientGateway = recipientGateway;
    }

    public LongFilter getSystemCommissionUpAmount() {
        return systemCommissionUpAmount;
    }

    public LongFilter systemCommissionUpAmount() {
        if (systemCommissionUpAmount == null) {
            systemCommissionUpAmount = new LongFilter();
        }
        return systemCommissionUpAmount;
    }

    public void setSystemCommissionUpAmount(LongFilter systemCommissionUpAmount) {
        this.systemCommissionUpAmount = systemCommissionUpAmount;
    }

    public LongFilter getSystemCommissionDownAmount() {
        return systemCommissionDownAmount;
    }

    public LongFilter systemCommissionDownAmount() {
        if (systemCommissionDownAmount == null) {
            systemCommissionDownAmount = new LongFilter();
        }
        return systemCommissionDownAmount;
    }

    public void setSystemCommissionDownAmount(LongFilter systemCommissionDownAmount) {
        this.systemCommissionDownAmount = systemCommissionDownAmount;
    }

    public LongFilter getSystemFeeAmount() {
        return systemFeeAmount;
    }

    public LongFilter systemFeeAmount() {
        if (systemFeeAmount == null) {
            systemFeeAmount = new LongFilter();
        }
        return systemFeeAmount;
    }

    public void setSystemFeeAmount(LongFilter systemFeeAmount) {
        this.systemFeeAmount = systemFeeAmount;
    }

    public LongFilter getAgentFeeAmount() {
        return agentFeeAmount;
    }

    public LongFilter agentFeeAmount() {
        if (agentFeeAmount == null) {
            agentFeeAmount = new LongFilter();
        }
        return agentFeeAmount;
    }

    public void setAgentFeeAmount(LongFilter agentFeeAmount) {
        this.agentFeeAmount = agentFeeAmount;
    }

    public DoubleFilter getRoundAmount() {
        return roundAmount;
    }

    public DoubleFilter roundAmount() {
        if (roundAmount == null) {
            roundAmount = new DoubleFilter();
        }
        return roundAmount;
    }

    public void setRoundAmount(DoubleFilter roundAmount) {
        this.roundAmount = roundAmount;
    }

    public DoubleFilter getSystemFee() {
        return systemFee;
    }

    public DoubleFilter systemFee() {
        if (systemFee == null) {
            systemFee = new DoubleFilter();
        }
        return systemFee;
    }

    public void setSystemFee(DoubleFilter systemFee) {
        this.systemFee = systemFee;
    }

    public IntegerFilter getSystemCurrency() {
        return systemCurrency;
    }

    public IntegerFilter systemCurrency() {
        if (systemCurrency == null) {
            systemCurrency = new IntegerFilter();
        }
        return systemCurrency;
    }

    public void setSystemCurrency(IntegerFilter systemCurrency) {
        this.systemCurrency = systemCurrency;
    }

    public IntegerFilter getCcyRate() {
        return ccyRate;
    }

    public IntegerFilter ccyRate() {
        if (ccyRate == null) {
            ccyRate = new IntegerFilter();
        }
        return ccyRate;
    }

    public void setCcyRate(IntegerFilter ccyRate) {
        this.ccyRate = ccyRate;
    }

    public TransactionStatusFilter getStatus() {
        return status;
    }

    public TransactionStatusFilter status() {
        if (status == null) {
            status = new TransactionStatusFilter();
        }
        return status;
    }

    public void setStatus(TransactionStatusFilter status) {
        this.status = status;
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

    public InstantFilter getDate() {
        return date;
    }

    public InstantFilter date() {
        if (date == null) {
            date = new InstantFilter();
        }
        return date;
    }

    public void setDate(InstantFilter date) {
        this.date = date;
    }

    public PayTypeFilter getPayType() {
        return payType;
    }

    public PayTypeFilter payType() {
        if (payType == null) {
            payType = new PayTypeFilter();
        }
        return payType;
    }

    public void setPayType(PayTypeFilter payType) {
        this.payType = payType;
    }

    public StringFilter getRequestSubAccount() {
        return requestSubAccount;
    }

    public StringFilter requestSubAccount() {
        if (requestSubAccount == null) {
            requestSubAccount = new StringFilter();
        }
        return requestSubAccount;
    }

    public void setRequestSubAccount(StringFilter requestSubAccount) {
        this.requestSubAccount = requestSubAccount;
    }

    public StringFilter getEposMerchant() {
        return eposMerchant;
    }

    public StringFilter eposMerchant() {
        if (eposMerchant == null) {
            eposMerchant = new StringFilter();
        }
        return eposMerchant;
    }

    public void setEposMerchant(StringFilter eposMerchant) {
        this.eposMerchant = eposMerchant;
    }

    public StringFilter getEposTerminal() {
        return eposTerminal;
    }

    public StringFilter eposTerminal() {
        if (eposTerminal == null) {
            eposTerminal = new StringFilter();
        }
        return eposTerminal;
    }

    public void setEposTerminal(StringFilter eposTerminal) {
        this.eposTerminal = eposTerminal;
    }

    public BooleanFilter getMain() {
        return main;
    }

    public BooleanFilter main() {
        if (main == null) {
            main = new BooleanFilter();
        }
        return main;
    }

    public void setMain(BooleanFilter main) {
        this.main = main;
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

    public StringFilter getSenderBean() {
        return senderBean;
    }

    public StringFilter senderBean() {
        if (senderBean == null) {
            senderBean = new StringFilter();
        }
        return senderBean;
    }

    public void setSenderBean(StringFilter senderBean) {
        this.senderBean = senderBean;
    }

    public StringFilter getCategory() {
        return category;
    }

    public StringFilter category() {
        if (category == null) {
            category = new StringFilter();
        }
        return category;
    }

    public void setCategory(StringFilter category) {
        this.category = category;
    }

    public StringFilter getPromo() {
        return promo;
    }

    public StringFilter promo() {
        if (promo == null) {
            promo = new StringFilter();
        }
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
        return (
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
            Objects.equals(systemCommissionUpAmount, that.systemCommissionUpAmount) &&
            Objects.equals(systemCommissionDownAmount, that.systemCommissionDownAmount) &&
            Objects.equals(systemFeeAmount, that.systemFeeAmount) &&
            Objects.equals(agentFeeAmount, that.agentFeeAmount) &&
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
            Objects.equals(main, that.main) &&
            Objects.equals(amountRatio, that.amountRatio) &&
            Objects.equals(senderBean, that.senderBean) &&
            Objects.equals(category, that.category) &&
            Objects.equals(promo, that.promo)
        );
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
            systemCommissionUpAmount,
            systemCommissionDownAmount,
            systemFeeAmount,
            agentFeeAmount,
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
            main,
            amountRatio,
            senderBean,
            category,
            promo
        );
    }

    // prettier-ignore
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
            (category != null ? "category=" + category + ", " : "") +
            (promo != null ? "promo=" + promo + ", " : "") +
            "}";
    }
}
