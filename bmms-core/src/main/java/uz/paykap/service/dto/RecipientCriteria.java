package uz.paykap.service.dto;

import io.github.jhipster.service.filter.*;
import uz.devops.bmcore.enumeration.RecipientType;

import java.io.Serializable;
import java.util.Objects;

/**
 * Criteria class for the Recipient entity. This class is used in RecipientResource to
 * receive all the possible filtering options from the Http GET request parameters.
 * For example the following could be a valid requests:
 * <code> /recipients?id.greaterThan=5&amp;attr1.contains=something&amp;attr2.specified=false</code>
 * As Spring is unable to properly convert the types, unless specific {@link Filter} class are used, we need to use
 * fix type specific filters.
 */
public class RecipientCriteria implements Serializable {
    /**
     * Class for filtering RecipientType
     */
    public static class RecipientTypeFilter extends Filter<RecipientType> {
    }

    private static final long serialVersionUID = 1L;


    private LongFilter id;

    private StringFilter shortName;

    private StringFilter serviceId;

    private StringFilter thumbnail;

    private StringFilter logo;

    private IntegerFilter amountRatio;

    private IntegerFilter ccy;

    private LongFilter minAmount;

    private LongFilter maxAmount;

    private IntegerFilter limitRatio;

    private DoubleFilter commission;

    private DoubleFilter fee;

    private DoubleFilter systemCommission;

    private DoubleFilter systemFee;

    private DoubleFilter senderCommission;

    private IntegerFilter ord;

    private BooleanFilter active;

    private BooleanFilter check;

    private BooleanFilter pay;

    private BooleanFilter status;

    private BooleanFilter cancel;

    private BooleanFilter canDeposit;

    private IntegerFilter eposPort;

    private LongFilter eposMerchant;

    private LongFilter eposTerminal;

    private StringFilter settlementAccount;

    private StringFilter mfo;

    private StringFilter inn;

    private BooleanFilter reconcile;

    private RecipientTypeFilter type;

    private LongFilter gatewayId;

    private LongFilter categoryId;

    private LongFilter titleId;

    private LongFilter fullNameId;

    public RecipientCriteria() {
    }

    public LongFilter getId() {
        return id;
    }

    public void setId(LongFilter id) {
        this.id = id;
    }

    public StringFilter getShortName() {
        return shortName;
    }

    public void setShortName(StringFilter shortName) {
        this.shortName = shortName;
    }

    public StringFilter getServiceId() {
        return serviceId;
    }

    public void setServiceId(StringFilter serviceId) {
        this.serviceId = serviceId;
    }

    public StringFilter getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(StringFilter thumbnail) {
        this.thumbnail = thumbnail;
    }

    public StringFilter getLogo() {
        return logo;
    }

    public void setLogo(StringFilter logo) {
        this.logo = logo;
    }

    public IntegerFilter getAmountRatio() {
        return amountRatio;
    }

    public void setAmountRatio(IntegerFilter amountRatio) {
        this.amountRatio = amountRatio;
    }

    public IntegerFilter getCcy() {
        return ccy;
    }

    public void setCcy(IntegerFilter ccy) {
        this.ccy = ccy;
    }

    public LongFilter getMinAmount() {
        return minAmount;
    }

    public void setMinAmount(LongFilter minAmount) {
        this.minAmount = minAmount;
    }

    public LongFilter getMaxAmount() {
        return maxAmount;
    }

    public void setMaxAmount(LongFilter maxAmount) {
        this.maxAmount = maxAmount;
    }

    public IntegerFilter getLimitRatio() {
        return limitRatio;
    }

    public void setLimitRatio(IntegerFilter limitRatio) {
        this.limitRatio = limitRatio;
    }

    public DoubleFilter getCommission() {
        return commission;
    }

    public void setCommission(DoubleFilter commission) {
        this.commission = commission;
    }

    public DoubleFilter getFee() {
        return fee;
    }

    public void setFee(DoubleFilter fee) {
        this.fee = fee;
    }

    public DoubleFilter getSystemCommission() {
        return systemCommission;
    }

    public void setSystemCommission(DoubleFilter systemCommission) {
        this.systemCommission = systemCommission;
    }

    public DoubleFilter getSystemFee() {
        return systemFee;
    }

    public void setSystemFee(DoubleFilter systemFee) {
        this.systemFee = systemFee;
    }

    public DoubleFilter getSenderCommission() {
        return senderCommission;
    }

    public void setSenderCommission(DoubleFilter senderCommission) {
        this.senderCommission = senderCommission;
    }

    public IntegerFilter getOrd() {
        return ord;
    }

    public void setOrd(IntegerFilter ord) {
        this.ord = ord;
    }

    public BooleanFilter getActive() {
        return active;
    }

    public void setActive(BooleanFilter active) {
        this.active = active;
    }

    public BooleanFilter getCheck() {
        return check;
    }

    public void setCheck(BooleanFilter check) {
        this.check = check;
    }

    public BooleanFilter getPay() {
        return pay;
    }

    public void setPay(BooleanFilter pay) {
        this.pay = pay;
    }

    public BooleanFilter getStatus() {
        return status;
    }

    public void setStatus(BooleanFilter status) {
        this.status = status;
    }

    public BooleanFilter getCancel() {
        return cancel;
    }

    public void setCancel(BooleanFilter cancel) {
        this.cancel = cancel;
    }

    public BooleanFilter getCanDeposit() {
        return canDeposit;
    }

    public void setCanDeposit(BooleanFilter canDeposit) {
        this.canDeposit = canDeposit;
    }

    public IntegerFilter getEposPort() {
        return eposPort;
    }

    public void setEposPort(IntegerFilter eposPort) {
        this.eposPort = eposPort;
    }

    public LongFilter getEposMerchant() {
        return eposMerchant;
    }

    public void setEposMerchant(LongFilter eposMerchant) {
        this.eposMerchant = eposMerchant;
    }

    public LongFilter getEposTerminal() {
        return eposTerminal;
    }

    public void setEposTerminal(LongFilter eposTerminal) {
        this.eposTerminal = eposTerminal;
    }

    public StringFilter getSettlementAccount() {
        return settlementAccount;
    }

    public void setSettlementAccount(StringFilter settlementAccount) {
        this.settlementAccount = settlementAccount;
    }

    public StringFilter getMfo() {
        return mfo;
    }

    public void setMfo(StringFilter mfo) {
        this.mfo = mfo;
    }

    public StringFilter getInn() {
        return inn;
    }

    public void setInn(StringFilter inn) {
        this.inn = inn;
    }

    public RecipientTypeFilter getType() {
        return type;
    }

    public void setType(RecipientTypeFilter type) {
        this.type = type;
    }

    public BooleanFilter getReconcile() {
        return reconcile;
    }

    public void setReconcile(BooleanFilter reconcile) {
        this.reconcile = reconcile;
    }

    public LongFilter getGatewayId() {
        return gatewayId;
    }

    public void setGatewayId(LongFilter gatewayId) {
        this.gatewayId = gatewayId;
    }

    public LongFilter getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(LongFilter categoryId) {
        this.categoryId = categoryId;
    }

    public LongFilter getTitleId() {
        return titleId;
    }

    public void setTitleId(LongFilter titleId) {
        this.titleId = titleId;
    }

    public LongFilter getFullNameId() {
        return fullNameId;
    }

    public void setFullNameId(LongFilter fullNameId) {
        this.fullNameId = fullNameId;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final RecipientCriteria that = (RecipientCriteria) o;
        return
            Objects.equals(id, that.id) &&
                Objects.equals(shortName, that.shortName) &&
                Objects.equals(serviceId, that.serviceId) &&
                Objects.equals(thumbnail, that.thumbnail) &&
                Objects.equals(logo, that.logo) &&
                Objects.equals(amountRatio, that.amountRatio) &&
                Objects.equals(ccy, that.ccy) &&
                Objects.equals(minAmount, that.minAmount) &&
                Objects.equals(maxAmount, that.maxAmount) &&
                Objects.equals(limitRatio, that.limitRatio) &&
                Objects.equals(commission, that.commission) &&
                Objects.equals(fee, that.fee) &&
                Objects.equals(systemCommission, that.systemCommission) &&
                Objects.equals(systemFee, that.systemFee) &&
                Objects.equals(senderCommission, that.senderCommission) &&
                Objects.equals(ord, that.ord) &&
                Objects.equals(active, that.active) &&
                Objects.equals(check, that.check) &&
                Objects.equals(pay, that.pay) &&
                Objects.equals(status, that.status) &&
                Objects.equals(cancel, that.cancel) &&
                Objects.equals(canDeposit, that.canDeposit) &&
                Objects.equals(eposPort, that.eposPort) &&
                Objects.equals(eposMerchant, that.eposMerchant) &&
                Objects.equals(eposTerminal, that.eposTerminal) &&
                Objects.equals(settlementAccount, that.settlementAccount) &&
                Objects.equals(mfo, that.mfo) &&
                Objects.equals(inn, that.inn) &&
                Objects.equals(type, that.type) &&
                Objects.equals(gatewayId, that.gatewayId) &&
                Objects.equals(categoryId, that.categoryId) &&
                Objects.equals(titleId, that.titleId) &&
                Objects.equals(fullNameId, that.fullNameId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
            id,
            shortName,
            serviceId,
            thumbnail,
            logo,
            amountRatio,
            ccy,
            minAmount,
            maxAmount,
            limitRatio,
            commission,
            fee,
            systemCommission,
            systemFee,
            senderCommission,
            ord,
            active,
            check,
            pay,
            status,
            cancel,
            canDeposit,
            eposPort,
            eposMerchant,
            eposTerminal,
            settlementAccount,
            mfo,
            inn,
            gatewayId,
            categoryId,
            titleId,
            fullNameId
        );
    }

    @Override
    public String toString() {
        return "RecipientCriteria{" +
                (id != null ? "id=" + id + ", " : "") +
                (shortName != null ? "shortName=" + shortName + ", " : "") +
                (serviceId != null ? "serviceId=" + serviceId + ", " : "") +
                (thumbnail != null ? "thumbnail=" + thumbnail + ", " : "") +
                (logo != null ? "logo=" + logo + ", " : "") +
                (amountRatio != null ? "amountRatio=" + amountRatio + ", " : "") +
                (ccy != null ? "ccy=" + ccy + ", " : "") +
                (minAmount != null ? "minAmount=" + minAmount + ", " : "") +
                (maxAmount != null ? "maxAmount=" + maxAmount + ", " : "") +
                (limitRatio != null ? "limitRatio=" + limitRatio + ", " : "") +
                (commission != null ? "commission=" + commission + ", " : "") +
                (fee != null ? "fee=" + fee + ", " : "") +
                (systemCommission != null ? "systemCommission=" + systemCommission + ", " : "") +
                (systemFee != null ? "systemFee=" + systemFee + ", " : "") +
                (senderCommission != null ? "senderCommission=" + senderCommission + ", " : "") +
                (ord != null ? "ord=" + ord + ", " : "") +
                (active != null ? "active=" + active + ", " : "") +
                (check != null ? "check=" + check + ", " : "") +
                (pay != null ? "pay=" + pay + ", " : "") +
            (status != null ? "status=" + status + ", " : "") +
            (cancel != null ? "cancel=" + cancel + ", " : "") +
            (canDeposit != null ? "canDeposit=" + canDeposit + ", " : "") +
            (eposPort != null ? "eposPort=" + eposPort + ", " : "") +
            (eposMerchant != null ? "eposMerchant=" + eposMerchant + ", " : "") +
            (eposTerminal != null ? "eposTerminal=" + eposTerminal + ", " : "") +
            (settlementAccount != null ? "settlementAccount=" + settlementAccount + ", " : "") +
            (mfo != null ? "mfo=" + mfo + ", " : "") +
            (inn != null ? "inn=" + inn + ", " : "") +
            (reconcile != null ? "reconcile=" + reconcile + ", " : "") +
            (type != null ? "type=" + type + ", " : "") +
            (gatewayId != null ? "gatewayId=" + gatewayId + ", " : "") +
            (categoryId != null ? "categoryId=" + categoryId + ", " : "") +
            (titleId != null ? "titleId=" + titleId + ", " : "") +
            (fullNameId != null ? "fullNameId=" + fullNameId + ", " : "") +
            "}";
    }

}
