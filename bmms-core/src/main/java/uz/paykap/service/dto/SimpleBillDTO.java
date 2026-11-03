//package uz.paykap.service.dto;
//
//import com.fasterxml.jackson.annotation.JsonIgnore;
//import io.swagger.annotations.ApiModelProperty;
//import uz.devops.bmcore.dto.CommonBillDTO;
//import uz.devops.bmcore.enumeration.TransactionStatus;
//
//import javax.validation.constraints.NotNull;
//import javax.validation.constraints.Size;
//import java.io.Serializable;
//import java.time.Instant;
//import java.util.Map;
//import java.util.Objects;
//
///**
// * A DTO for the Bill entity.
// */
//public class CommonResponse implements CommonBillDTO, Serializable {
//
//    @ApiModelProperty(value = "Номер платежа в системе", notes = "Используется для последуюших операций")
//    private Long id;
//
//    @ApiModelProperty(value = "Наименование получателя")
//    @NotNull
//    @Size(max = 64)
//    private String recipientShortName;
//
//    @ApiModelProperty(value = "Номер сессии", notes = "Для использования в ПОС системах")
//    @NotNull
//    @Size(max = 32)
//    private String sessionId;
//
//    //TODO - turn on when we accept multi amountRatio from agents
//    @JsonIgnore
//    @ApiModelProperty(value = "Соотношение суммы", notes = "Например: 1 - рубли, сумы, доллара; 100 - копейки, тийыны, центы")
//    private Integer amountRatio;
//
//    @ApiModelProperty(value = "Сумма платежа", notes = "см. Соотношение суммы")
//    @NotNull
//    private Long amount;
//
//    @ApiModelProperty(value = "Лицевой счет", example = "Номер телефона, номер кошелка, ...")
//    @NotNull
//    @Size(max = 64)
//    private String account;
//
//    @ApiModelProperty(value = "Валюта, трёхзначный цифровой ISO 4217 ", example = "860 - Узбекский сум, 840 - Доллар США")
//    private Integer currency;
//
//    @ApiModelProperty(value = "Внешний ид. трансакции для системы, внутренний ид. для получателя", example = "abc123")
//    @Size(max = 32)
//    private String extId;
//
//    @ApiModelProperty(value = "Время запроса в систему получателя")
//    @NotNull
//    private Instant requestTime;
//
//    @ApiModelProperty(value = "Время ответа от системы получателя")
//    @NotNull
//    private Instant responseTime;
//
//    @ApiModelProperty(value = "Сообшение ответа", notes = "Сообшения ответов различаются у получателей. За успешный статус запроса отвечает поле 'success'")
//    @Size(max = 128)
//    private String responseText;
//
//    @ApiModelProperty(value = "Успешность ответа", notes = "true - успешно, false - не успешно, но зависит от статуса, т.е. может бить успешним но не оплачен. success - статус транзакции, если не false то можно продолжат операцию")
//    private Boolean success;
//
//    @ApiModelProperty(value = "Статус ответа", notes = "обычно CHECK_OK и SUCCESS в успешный, иногда WAITING для async услуг, если WAITING то надо вам проверять статус транзакции")
//    private TransactionStatus status = TransactionStatus.NEW;
//
//    @ApiModelProperty(value = "Доп. данные", example = "{\"задолжность\":\"123\"}")
//    private Map<String, String> data;
//
//    private String gatewayName;
//
//    public String getGatewayName() {
//        return gatewayName;
//    }
//
//    public void setGatewayName(String gatewayName) {
//        this.gatewayName = gatewayName;
//    }
//
//    @Override
//    public String toString() {
//        return "SimpleBillDTO{" +
//            "id=" + id +
//            ", recipientShortName='" + recipientShortName + '\'' +
//            ", sessionId='" + sessionId + '\'' +
//            ", amountRatio=" + amountRatio +
//            ", amount=" + amount +
//            ", account='" + account + '\'' +
//            ", currency=" + currency +
//            ", extId='" + extId + '\'' +
//            ", requestTime=" + requestTime +
//            ", responseTime=" + responseTime +
//            ", responseText='" + responseText + '\'' +
//            ", success=" + success +
//            ", status=" + status +
//            ", data=" + data +
//            ", gatewayName='" + gatewayName + '\'' +
//            '}';
//    }
//
//    public Map<String, String> getData() {
//        return data;
//    }
//
//    public TransactionStatus getStatus() {
//        return status;
//    }
//
//    public void setStatus(TransactionStatus status) {
//        this.status = status;
//    }
//
//    public void setData(Map<String, String> data) {
//        this.data = data;
//    }
//
//    public Long getId() {
//        return id;
//    }
//
//    public void setId(Long id) {
//        this.id = id;
//    }
//
//    public String getRecipientShortName() {
//        return recipientShortName;
//    }
//
//    public void setRecipientShortName(String recipientShortName) {
//        this.recipientShortName = recipientShortName;
//    }
//
//    public String getSessionId() {
//        return sessionId;
//    }
//
//    public void setSessionId(String sessionId) {
//        this.sessionId = sessionId;
//    }
//
//    public Integer getAmountRatio() {
//        return amountRatio;
//    }
//
//    public void setAmountRatio(Integer amountRatio) {
//        this.amountRatio = amountRatio;
//    }
//
//    public Long getAmount() {
//        return amount;
//    }
//
//    public void setAmount(Long amount) {
//        this.amount = amount;
//    }
//
//    public String getAccount() {
//        return account;
//    }
//
//    public void setAccount(String account) {
//        this.account = account;
//    }
//
//    public Integer getCurrency() {
//        return currency;
//    }
//
//    public void setCurrency(Integer currency) {
//        this.currency = currency;
//    }
//
//    public String getExtId() {
//        return extId;
//    }
//
//    public void setExtId(String extId) {
//        this.extId = extId;
//    }
//
//    public Instant getRequestTime() {
//        return requestTime;
//    }
//
//    public void setRequestTime(Instant requestTime) {
//        this.requestTime = requestTime;
//    }
//
//    public Instant getResponseTime() {
//        return responseTime;
//    }
//
//    public void setResponseTime(Instant responseTime) {
//        this.responseTime = responseTime;
//    }
//
//    public String getResponseText() {
//        return responseText;
//    }
//
//    public void setResponseText(String responseText) {
//        this.responseText = responseText;
//    }
//
//    public Boolean isSuccess() {
//        return success;
//    }
//
//    public void setSuccess(Boolean success) {
//        this.success = success;
//    }
//
//    @Override
//    public boolean equals(Object o) {
//        if (this == o) {
//            return true;
//        }
//        if (o == null || getClass() != o.getClass()) {
//            return false;
//        }
//
//        SimpleBillResponse billDTO = (SimpleBillDTO) o;
//        if (billDTO.getId() == null || getId() == null) {
//            return false;
//        }
//        return Objects.equals(getId(), billDTO.getId());
//    }
//
//    @Override
//    public int hashCode() {
//        return Objects.hashCode(getId());
//    }
//
//    public CommonResponse id(Long id) {
//        this.id = id;
//        return this;
//    }
//
//    public CommonResponse recipientShortName(String recipientShortName) {
//        this.recipientShortName = recipientShortName;
//        return this;
//    }
//
//    public CommonResponse sessionId(String sessionId) {
//        this.sessionId = sessionId;
//        return this;
//    }
//
//    public CommonResponse amountRatio(Integer amountRatio) {
//        this.amountRatio = amountRatio;
//        return this;
//    }
//
//    public CommonResponse amount(Long amount) {
//        this.amount = amount;
//        return this;
//    }
//
//    public CommonResponse account(String account) {
//        this.account = account;
//        return this;
//    }
//
//    public CommonResponse currency(Integer currency) {
//        this.currency = currency;
//        return this;
//    }
//
//    public CommonResponse extId(String extId) {
//        this.extId = extId;
//        return this;
//    }
//
//    public CommonResponse requestTime(Instant requestTime) {
//        this.requestTime = requestTime;
//        return this;
//    }
//
//    public CommonResponse responseTime(Instant responseTime) {
//        this.responseTime = responseTime;
//        return this;
//    }
//
//    public CommonResponse responseText(String responseText) {
//        this.responseText = responseText;
//        return this;
//    }
//
//    public CommonResponse success(Boolean success) {
//        this.success = success;
//        return this;
//    }
//
//    public CommonResponse data(Map<String, String> data) {
//        this.data = data;
//        return this;
//    }
//
//    public SimpleBillResponse status(final TransactionStatus status) {
//        this.status = status;
//        return this;
//    }
//}
