package uz.paykap.gateway.dto;

import com.google.common.base.Objects;
import io.swagger.annotations.ApiModelProperty;
import org.apache.commons.lang3.builder.ToStringBuilder;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public class GatewayTranDTO {

    public GatewayTranDTO() {
    }

    public GatewayTranDTO(Long id, @NotNull Double amount) {
        this(id, amount, null);
    }

    public GatewayTranDTO(Long id, @NotNull Double amount, @Size(max = 32) String extId) {
        this(id, amount, extId, null);
    }

    public GatewayTranDTO(Long id, @NotNull Double amount, @Size(max = 32) String extId, @NotNull String extTime) {
        this(id, null, amount, null, 860, extId, extTime, true, false, CompareSchema.ONLY_EXT_ID);
    }

    public GatewayTranDTO(@NotNull Double amount, @NotNull @Size(max = 64) String account, @Size(max = 32) String extId, Boolean success) {
        this(null, null, amount, account, 860, extId, null, success, false, CompareSchema.ONLY_EXT_ID);
    }

    public GatewayTranDTO(Long id, @NotNull Long recipientId, @NotNull Double amount, @NotNull @Size(max = 64) String account, Integer currency, @Size(max = 32) String extId, @NotNull String extTime, Boolean success, Boolean reversal) {
        this(id, recipientId, amount, account, currency, extId, extTime, success, reversal, CompareSchema.ONLY_ID);//TODO JAVLON aka posmotrite , ne zahodit na nijnie konstruktori ! ! !
    }

    public GatewayTranDTO(Long id, @NotNull Long recipientId, @NotNull Double amount, @NotNull @Size(max = 64) String account, Integer currency, @Size(max = 32) String extId, @NotNull String extTime, Boolean success, Boolean reversal, CompareSchema compareSchema) {
        this(id, recipientId, amount, account, currency, extId, extTime, success, reversal, compareSchema, false);
    }

    public GatewayTranDTO(Long id, @NotNull Long recipientId, @NotNull Double amount, @NotNull @Size(max = 64) String account, Integer currency, @Size(max = 32) String extId, @NotNull String extTime, Boolean success, Boolean reversal, Integer integer) {
        this(id, recipientId, amount, account, currency, extId, extTime, success, reversal, CompareSchema.values()[integer], false);
    }

    public GatewayTranDTO(Long id, @NotNull Long recipientId, @NotNull Double amount, @NotNull @Size(max = 64) String account, Integer currency, @Size(max = 32) String extId, @NotNull String extTime, Boolean success, Boolean reversal, CompareSchema compareSchema, boolean isGatewayTransaction) {
        this.id = id;
        this.recipientId = recipientId;
        this.amount = amount;
        this.account = account;
        this.currency = currency;
        this.extId = extId;
        this.extTime = extTime;
        this.success = success;
        this.reversal = reversal;
        this.compareSchema = compareSchema;
        this.isGatewayTransaction = isGatewayTransaction;
    }

    public static GatewayTranDTO GatewayTranDTO(Long id, @NotNull Long recipientId, @NotNull Double amount, @NotNull @Size(max = 64) String account, Integer currency, @Size(max = 32) String extId, @NotNull String extTime, Boolean success, Boolean reversal, Integer integer) {
        if (integer < 0 || integer > CompareSchema.values().length) {
            return new GatewayTranDTO(id, recipientId, amount, account, currency, extId, extTime, success, reversal, CompareSchema.ONLY_EXT_ID, false);
        } else {
            return new GatewayTranDTO(id, recipientId, amount, account, currency, extId, extTime, success, reversal, CompareSchema.values()[integer], false);
        }
    }

    private CompareSchema compareSchema = CompareSchema.ONLY_EXT_ID;

    @ApiModelProperty(value = "Номер платежа в системе (BillMaster)")
    private Long id;

    @ApiModelProperty(value = "Внешний ид. трансакции для системы (BillMaster), внутренний ид. для получателя (eg.: Paynet)", example = "abc123")
    @Size(max = 32)
    private String extId;

    @ApiModelProperty(value = "Ид. получателя")
    @NotNull
    private Long recipientId;

    @ApiModelProperty(value = "Сумма платежа", notes = "см. Соотношение суммы")
    @NotNull
    private Double amount;

    @ApiModelProperty(value = "Лицевой счет", example = "Номер телефона, номер кошелка, ...")
    @NotNull
    @Size(max = 64)
    private String account;

    @ApiModelProperty(value = "Валюта, трёхзначный цифровой ISO 4217 ", example = "860 - Узбекский сум, 840 - Доллар США")
    private Integer currency;

    @ApiModelProperty(value = "Время запроса в систему получателя")
    @NotNull
    private String extTime;

    @ApiModelProperty(value = "Статус ответа", notes = "true - успешно, false - не успешно")
    private Boolean success = false;

    @ApiModelProperty(value = "Статус отмены", notes = "true - отменен, false - не отменен")
    private Boolean reversal = false;

    @ApiModelProperty(value = "Флаг", notes = "true - Транзакция с шлюха , false - Транзакция не с шлюза")
    private Boolean isGatewayTransaction = false;

    @Override
    public String toString() {
        return new ToStringBuilder(this)
            .append("compareSchema", compareSchema)
            .append("id", id)
            .append("extId", extId)
            .append("recipientId", recipientId)
            .append("amount", amount)
            .append("account", account)
            .append("currency", currency)
            .append("extTime", extTime)
            .append("success", success)
            .append("reversal", reversal)
            .append("isGatewayTransaction", isGatewayTransaction)
            .toString();
    }

    public Boolean getGatewayTransaction() {
        return isGatewayTransaction;
    }

    public void setGatewayTransaction(Boolean gatewayTransaction) {
        isGatewayTransaction = gatewayTransaction;
    }

    public Boolean getReversal() {
        return reversal;
    }

    public void setReversal(Boolean reversal) {
        this.reversal = reversal;
    }

    public Long getRecipientId() {
        return recipientId;
    }

    public void setRecipientId(Long recipientId) {
        this.recipientId = recipientId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public Integer getCurrency() {
        return currency;
    }

    public void setCurrency(Integer currency) {
        this.currency = currency;
    }

    public String getExtId() {
        return extId;
    }

    public void setExtId(String extId) {
        this.extId = extId;
    }

    public String getExtTime() {
        return extTime;
    }

    public void setExtTime(String extTime) {
        this.extTime = extTime;
    }

    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

    public GatewayTranDTO id(Long id) {
        this.id = id;
        return this;
    }

    public GatewayTranDTO amount(Double amount) {
        this.amount = amount;
        return this;
    }

    public GatewayTranDTO account(String account) {
        this.account = account;
        return this;
    }

    public GatewayTranDTO currency(Integer currency) {
        this.currency = currency;
        return this;
    }

    public GatewayTranDTO extId(String extId) {
        this.extId = extId;
        return this;
    }

    public GatewayTranDTO requestTime(String extTime) {
        this.extTime = extTime;
        return this;
    }

    public GatewayTranDTO success(Boolean success) {
        this.success = success;
        return this;
    }

    public GatewayTranDTO recipientId(Long recipientId) {
        this.recipientId = recipientId;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GatewayTranDTO that = (GatewayTranDTO) o;

        if (CompareSchema.ONLY_ID.equals(compareSchema)) {
            return Objects.equal(id, that.id);
        } else if (CompareSchema.ACCOUNT_AND_AMOUNT.equals(compareSchema)) {
            return Objects.equal(amount, that.amount) && Objects.equal(account, that.account);
        } else if (CompareSchema.ID_AND_AMOUNT.equals(compareSchema)) {
            return Objects.equal(id, that.id) && Objects.equal(amount, that.amount);
        } else { //DEFAULT id CompareSchema.ONLY_EXT_ID
            return Objects.equal(extId, that.extId);
        }
    }

    @Override
    public int hashCode() {
        if (compareSchema == CompareSchema.ONLY_ID) {
            return java.util.Objects.hash(id);
        } else if (compareSchema == CompareSchema.ONLY_EXT_ID) {
            return java.util.Objects.hash(extId);
        } else if (compareSchema == CompareSchema.ID_AND_AMOUNT) {
            return java.util.Objects.hash(id, amount);
        } else {
            return java.util.Objects.hash(amount, account);
        }
    }


    public GatewayTranDTO compareSchema(CompareSchema compareSchema) {
        this.compareSchema = compareSchema;
        return this;
    }
}
