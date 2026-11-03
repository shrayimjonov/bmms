package uz.paykap.domain;

import org.apache.commons.lang3.StringUtils;
import org.javers.core.metamodel.annotation.DiffIgnore;
import uz.devops.bmcore.domain.CommonBill;
import uz.devops.bmcore.dto.GatewayResult;
import uz.devops.bmcore.dto.HasGatewayResult;
import uz.devops.bmcore.enumeration.RequestType;
import uz.paykap.domain.converter.HashMapConverter;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.time.Instant;
import java.util.Map;
import java.util.Objects;

/**
 * A Bill.
 */
@Entity
@Table(name = "bill")
@DiffIgnore
public class Bill extends AbstractAuditingEntity implements CommonBill, Serializable, HasGatewayResult {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    private Long id;

    /**
     * Ид. получателя
     */
    @Column(name = "recipient_id")
    private Long recipientId;

    /**
     * Наименование получателя
     */
    @NotNull
    @Size(max = 64)
    @Column(name = "recipient_short_name", length = 64, nullable = false)
    private String recipientShortName;

    /**
     * Тип запроса в биллинг получателя
     */
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private RequestType type;

    /**
     * Разрядность суммы, 1 - сум, 100 - тийин
     */
    @NotNull
    @Column(name = "amount_ratio", nullable = false)
    private Integer amountRatio = 100;

    /**
     * Сумма платежа
     */
    @NotNull
    @Column(name = "amount", nullable = false)
    private Long amount = 0L;

    @NotNull
    @Size(max = 64)
    @Column(name = "account", length = 64, nullable = false)
    private String account;

    /**
     * Валюта, трёхзначный цифровой ISO 4217
     */
    @Column(name = "currency")
    private Integer currency;

    /**
     * Внешний идентификатор транзакции для системы, внутренний ид. для получателя
     */
    @Size(max = 32)
    @Column(name = "ext_id", length = 32)
    private String extId;

    /**
     * Время запроса в систему получателя
     */
    @NotNull
    @Column(name = "request_time", nullable = false)
    private Instant requestTime;

    @DiffIgnore
    @Lob
    @Column(name = "request_json")
    @Convert(converter = HashMapConverter.class)
    private Map<String, Object> requestJson;

    @DiffIgnore
    @Lob
    @Column(name = "response_json")
    @Convert(converter = HashMapConverter.class)
    private Map<String, Object> responseJson;

    /**
     * Время ответа от системы получателя
     */
    @NotNull
    @Column(name = "response_time", nullable = false)
    private Instant responseTime;

    /**
     * Код ответа
     */
    @Size(max = 32)
    @Column(name = "response_code", length = 32)
    private String responseCode;

    /**
     * Сообщения ответа
     */
    @Size(max = 128)
    @Column(name = "response_text", length = 128)
    private String responseText;

    /**
     * Статус ответа
     */
    @Column(name = "success")
    private Boolean success;

    /**
     * Логин агента
     */
    @Size(max = 64)
    @Column(name = "login", length = 64)
    private String login;

    /**
     * Номер транзакции
     */
    @Column(name = "transaction_id")
    private Long transactionId = -1L;

    /**
     * Наш идентификатор в Процессинговом центре
     */
    @Size(max = 128)
    @Column(name = "out_ext_id", length = 128)
    private String outExtId;

    public void fill(GatewayResult gatewayResult) {
        this
                .success(Objects.equals(gatewayResult.isSuccess(), Boolean.TRUE))
                .responseCode(gatewayResult.getResponseCode())
                .responseText(gatewayResult.getResponseText())
                .responseTime(gatewayResult.getResponseTime())
                .responseJson(gatewayResult.getResponseJson())
                .extId(gatewayResult.getExtId() == null || gatewayResult.getExtId().isEmpty() ? "-1" : gatewayResult.getExtId());
    }

    // jhipster-needle-entity-add-field - JHipster will add fields here
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Bill id(Long id) {
        this.id = id;
        return this;
    }

    public Long getRecipientId() {
        return this.recipientId;
    }

    public Bill recipientId(Long recipientId) {
        this.recipientId = recipientId;
        return this;
    }

    public void setRecipientId(Long recipientId) {
        this.recipientId = recipientId;
    }

    public String getRecipientShortName() {
        return this.recipientShortName;
    }

    public Bill recipientShortName(String recipientShortName) {
        if (recipientShortName == null || recipientShortName.isEmpty()) {
            this.recipientShortName = "-";
        } else {
            this.recipientShortName = StringUtils.left(recipientShortName, 64);
        }
        return this;
    }

    public void setRecipientShortName(String recipientShortName) {
        if (recipientShortName == null || recipientShortName.isEmpty()) {
            this.recipientShortName = "-";
        } else {
            this.recipientShortName = StringUtils.left(recipientShortName, 64);
        }
    }

//    //TODO - remove from model
//    @Deprecated
//    public String getSessionId() {
//        return this.sessionId;
//    }

    //TODO - remove from model
//    @Deprecated
//    public Bill sessionId(String sessionId) {
//        if (sessionId == null || sessionId.isEmpty()) {
//            this.sessionId = "-";
//        } else {
//            this.sessionId = StringUtils.left(sessionId, 32);
//        }
//
//        return this;
//    }

//    //TODO - remove from model
//    @Deprecated
//    public void setSessionId(String sessionId) {
//        if (sessionId == null || sessionId.isEmpty()) {
//            this.sessionId = "-";
//        } else {
//            this.sessionId = StringUtils.left(sessionId, 32);
//        }
//    }

    public RequestType getType() {
        return this.type;
    }

    public Bill type(RequestType type) {
        this.type = type;
        return this;
    }

    public void setType(RequestType type) {
        this.type = type;
    }

    public Integer getAmountRatio() {
        return this.amountRatio;
    }

    public Bill amountRatio(Integer amountRatio) {
        this.amountRatio = amountRatio;
        return this;
    }

    public void setAmountRatio(Integer amountRatio) {
        this.amountRatio = amountRatio;
    }

    public Long getAmount() {
        return this.amount;
    }

    public Bill amount(Long amount) {
        this.amount = amount;
        return this;
    }

    public void setAmount(Long amount) {
        this.amount = amount;
    }

    public String getAccount() {
        return this.account;
    }

    public Bill account(String account) {
        this.account = account;
        return this;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public Integer getCurrency() {
        return this.currency;
    }

    public Bill currency(Integer currency) {
        this.currency = currency;
        return this;
    }

    public void setCurrency(Integer currency) {
        this.currency = currency;
    }

    public String getExtId() {
        return this.extId;
    }

    @Override
    public Long getBillId() {
        return this.id;
    }

    public Bill extId(String extId) {
        if (extId == null || extId.isEmpty()) {
            this.extId = "-1";
        } else {
            this.extId = StringUtils.right(extId, 32);
        }
        return this;
    }

    public void setExtId(String extId) {
        if (extId == null || extId.isEmpty()) {
            this.extId = "-1";
        } else {
            this.extId = StringUtils.right(extId, 32);
        }
    }

    public Instant getRequestTime() {
        return this.requestTime;
    }

    public Bill requestTime(Instant requestTime) {
        this.requestTime = requestTime;
        return this;
    }

    public void setRequestTime(Instant requestTime) {
        this.requestTime = requestTime;
    }

    public Map<String, Object> getRequestJson() {
        return requestJson;
    }

    public Bill requestJson(Map<String, Object> requestJson) {
        this.requestJson = requestJson;
        return this;
    }

    public void setRequestJson(Map<String, Object> requestJson) {
        this.requestJson = requestJson;
    }

    public Map<String, Object> getResponseJson() {
        return responseJson;
    }

    public Bill responseJson(Map<String, Object> responseJson) {
        this.responseJson = responseJson;
        return this;
    }

    public void setResponseJson(Map<String, Object> responseJson) {
        this.responseJson = responseJson;
    }

    public Instant getResponseTime() {
        return this.responseTime;
    }

    public Bill responseTime(Instant responseTime) {
        if (responseTime == null) {
            responseTime = Instant.now();
        }
        this.responseTime = responseTime;
        return this;
    }

    public void setResponseTime(Instant responseTime) {
        this.responseTime = responseTime;
    }

    public String getResponseCode() {
        return this.responseCode;
    }

    public Bill responseCode(String responseCode) {
        if (responseCode == null || responseCode.isEmpty()) {
            this.responseCode = "-";
        } else {
            this.responseCode = StringUtils.left(responseCode, 32);
        }

        return this;
    }

    public void setResponseCode(String responseCode) {
        if (responseCode == null || responseCode.isEmpty()) {
            this.responseCode = "-";
        } else {
            this.responseCode = StringUtils.left(responseCode, 32);
        }
    }

    public String getResponseText() {
        return this.responseText;
    }

    public Bill responseText(String responseText) {
        if (responseText == null || responseText.isEmpty()) {
            this.responseText = "-";
        } else {
            this.responseText = StringUtils.left(responseText, 125);
        }

        return this;
    }

    public void setResponseText(String responseText) {
        if (responseText == null || responseText.isEmpty()) {
            this.responseText = "-";
        }

        this.responseText = StringUtils.left(responseText, 125);
    }

    public Boolean getSuccess() {
        return this.success;
    }

    public Boolean isSuccess() {
        return success;
    }

    public Bill success(Boolean success) {
        this.success = Objects.equals(success, Boolean.TRUE);
        return this;
    }

    @Override
    public void setBillId(Long billId) {
        this.id = billId;
    }

    public void setSuccess(Boolean success) {
        this.success = Objects.equals(success, Boolean.TRUE);
    }

    public String getLogin() {
        return this.login;
    }

    public Bill login(String login) {
        this.login = login;
        return this;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public Long getTransactionId() {
        return this.transactionId;
    }

    public Bill transactionId(Long transactionId) {
        this.transactionId = transactionId;
        return this;
    }

    public void setTransactionId(Long transactionId) {
        this.transactionId = transactionId;
    }

    public String getOutExtId() {
        return this.outExtId;
    }

    public Bill outExtId(String outExtId) {
        this.outExtId = outExtId;
        return this;
    }

    public void setOutExtId(String outExtId) {
        this.outExtId = outExtId;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Bill)) {
            return false;
        }
        return id != null && id.equals(((Bill) o).id);
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "Bill{" +
                "id=" + getId() +
                ", recipientId=" + getRecipientId() +
                ", recipientShortName='" + getRecipientShortName() + "'" +
//                ", sessionId='" + getSessionId() + "'" +
                ", type='" + getType() + "'" +
                ", amountRatio=" + getAmountRatio() +
                ", amount=" + getAmount() +
                ", account='" + getAccount() + "'" +
//                ", params='" + getParams() + "'" +
                ", currency=" + getCurrency() +
                ", extId='" + getExtId() + "'" +
//                ", checkId='" + getCheckId() + "'" +
//                ", payId='" + getPayId() + "'" +
                ", requestTime='" + getRequestTime() + "'" +
                ", requestJson='" + getRequestJson() + "'" +
                ", responseJson='" + getResponseJson() + "'" +
                ", responseTime='" + getResponseTime() + "'" +
                ", responseCode='" + getResponseCode() + "'" +
                ", responseText='" + getResponseText() + "'" +
                ", success='" + getSuccess() + "'" +
                ", login='" + getLogin() + "'" +
                ", outExtId='" + getOutExtId() + "'" +
//                ", fraudId=" + getFraudId() +
                ", transactionId=" + getTransactionId() +
                "}";
    }
}
