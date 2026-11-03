package uz.paykap.service.dto;

import io.swagger.annotations.ApiModelProperty;
import uz.devops.bmcore.enumeration.PayType;
import uz.devops.bmcore.enumeration.TransactionStatus;

import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

/**
 * A DTO for the {@link uz.paykap.domain.Transaction} entity.
 */
public class TransactionDTO extends AbstractAuditingDTO implements Serializable {

    private Long id;

    /**
     * Сумма запроса по валюте запроса, разряд зависит от настроек агента (agent.amountRatio)
     */
    @ApiModelProperty(value = "Сумма запроса по валюте запроса, разряд зависит от настроек агента (agent.amountRatio)")
    private Long requestAmount;

    /**
     * Валюта запроса, значения могут быть ограничены в зависимости от настроек агента
     */
    @ApiModelProperty(value = "Валюта запроса, значения могут быть ограничены в зависимости от настроек агента")
    private Integer requestCcy;

    /**
     * Ид запроса, отправляет агент
     */
    @NotNull
    @Size(max = 72)
    @ApiModelProperty(value = "Ид. запроса, отправляет агент", required = true)
    private String requestTrId;

    /**
     * Логин от которого приходит запрос, система сама определяет значение от имени пользователя
     */
    @NotNull
    @Size(max = 64)
    @ApiModelProperty(value = "Логин от которого приходит запрос, система сама определяет значение от имени пользователя", required = true)
    private String requestLogin;

    /**
     * Используется для группировки запросов
     */
    @Size(max = 64)
    @ApiModelProperty(value = "Используется для группировки запросов")
    private String requestGroup;

    /**
     * Наименование отправителя средств
     */
    @Size(max = 32)
    @ApiModelProperty(value = "Наименование отправителя средств")
    private String senderName;

    /**
     * Номер счета с которого снимаются средства
     */
    @Size(max = 64)
    @ApiModelProperty(value = "Номер счета с которого снимаются средства")
    private String senderAccount;

    /**
     * ID сервиса отправителя средств
     */
    @ApiModelProperty(value = "Ид. сервиса отправителя средств")
    private Long senderServiceId;

    /**
     * Снимающаяся сумма по валюте отправителя, разряд зависит от настроек отправителя (recipient.amountRatio)
     */
    @ApiModelProperty(value = "Снимающаяся сумма по валюте отправителя, разряд зависит от настроек отправителя (recipient.amountRatio)")
    private Long senderAmount;

    /**
     * Валюта отправителя (recipient.ccy)
     */
    @ApiModelProperty(value = "Валюта отправителя (recipient.ccy)")
    private Integer senderCcy;

    /**
     * ID транзакции в системе отправителя
     */
    @ApiModelProperty(value = "Ид. транзакции в системе отправителя")
    private String senderTrId;

    /**
     * Успешность запроса из системы отправителя
     */
    @ApiModelProperty(value = "Успешность запроса из системы отправителя")
    private Boolean senderSuccess;

    /**
     * Дата в системе отправителя, нужен для сверок
     */
    @ApiModelProperty(value = "Дата в системе отправителя, нужен для сверок")
    private Instant senderDate;

    /**
     * Статус запроса в системе отправителя, нужен для резолюции статуса транзакции
     */
    @Size(max = 16)
    @ApiModelProperty(value = "Статус запроса в системе отправителя, нужен для резолюции статуса транзакции")
    private String senderStatus;

    /**
     * Текст статус запроса в системе отправителя, для админа
     */
    @Size(max = 128)
    @ApiModelProperty(value = "Текст статус запроса в системе отправителя, для админа")
    private String senderStatusText;

    /**
     * Ид. чека отправителя из таблицы bill (bill.id)
     */
    @ApiModelProperty(value = "Ид. чека отправителя из таблицы bill (bill.id)")
    private Long senderBillId;

    /**
     * Наименование шлюза отправителя
     */
    @ApiModelProperty(value = "Наименование шлюза отправителя")
    private String senderGateway;

    /**
     * Поступающая сумма по валюте получателя, разряд и валюта зависит от настроек получателя (recipient.amountRatio и recipient.ccy)
     */
    @ApiModelProperty(
            value = "Поступающая сумма по валюте получателя, разряд и валюта зависит от настроек получателя (recipient.amountRatio и recipient.ccy)"
    )
    private Double gatewayAmount;

    /**
     * Наименование получателя средств
     */
    @Size(max = 32)
    @ApiModelProperty(value = "Наименование получателя средств")
    private String recipientName;

    /**
     * Номер счета на которую поступают средства
     */
    @Size(max = 64)
    @ApiModelProperty(value = "Номер счета на которую поступают средства")
    private String recipientAccount;

    /**
     * ID сервиса получателя средств
     */
    @ApiModelProperty(value = "Ид. сервиса получателя средств")
    private Long recipientServiceId;

    /**
     * Поступающая сумма по валюте получателя, разряд зависит от настроек получателя (recipient.amountRatio)
     */
    @ApiModelProperty(value = "Поступающая сумма по валюте получателя, разряд зависит от настроек получателя (recipient.amountRatio)")
    private Long recipientAmount;

    /**
     * Валюта получателя (recipient.ccy)
     */
    @ApiModelProperty(value = "Валюта получателя (recipient.ccy)")
    private Integer recipientCcy;

    /**
     * ID транзакции в системе получателя
     */
    @ApiModelProperty(value = "Ид. транзакции в системе получателя")
    private String recipientTrId;

    private Boolean recipientSuccess;

    /**
     * Дата в системе получателя, нужен для сверок
     */
    @ApiModelProperty(value = "Дата в системе получателя, нужен для сверок")
    private Instant recipientDate;

    /**
     * Статус запроса в системе получателя, нужен для резолюции статуса транзакции
     */
    @Size(max = 16)
    @ApiModelProperty(value = "Статус запроса в системе получателя, нужен для резолюции статуса транзакции")
    private String recipientStatus;

    /**
     * Текст статус запроса в системе получателя, для админа
     */
    @Size(max = 128)
    @ApiModelProperty(value = "Текст статус запроса в системе получателя, для админа")
    private String recipientStatusText;

    /**
     * Ид. чека получателя из таблицы bill (bill.id)
     */
    @ApiModelProperty(value = "Ид. чека получателя из таблицы bill (bill.id)")
    private Long recipientBillId;

    /**
     * Наименование шлюза получателя
     */
    @ApiModelProperty(value = "Наименование шлюза получателя")
    private String recipientGateway;

    /**
     * Комиссия системы сверху суммы
     */
    @ApiModelProperty(value = "Комиссия системы сверху суммы")
    private Long systemCommissionUpAmount;

    /**
     * Комиссия системы с суммы
     */
    @ApiModelProperty(value = "Комиссия системы с суммы")
    private Long systemCommissionDownAmount;

    /**
     * Сумма всех вознаграждений системы за эту транзакцию
     */
    @ApiModelProperty(value = "Сумма всех вознаграждений системы за эту транзакцию")
    private Long systemFeeAmount;

    /**
     * Сумма вознаграждения агента
     */
    @ApiModelProperty(value = "Сумма вознаграждения агента")
    private Long agentFeeAmount;

    /**
     * Сумма округлении gatewayAmount после сотой дроби, amount = 12.34567 -> gatewayAmount = 12.34, roundAmount = 0.00567
     */
    @DecimalMin(value = "0")
    @ApiModelProperty(
            value = "Сумма округлении gatewayAmount после сотой дроби, amount = 12.34567 -> gatewayAmount = 12.34, roundAmount = 0.00567"
    )
    private Double roundAmount;

    /**
     * Процент вознаграждений системы за эту транзакцию
     */
    @DecimalMin(value = "0")
    @DecimalMax(value = "100")
    @ApiModelProperty(value = "Процент вознаграждений системы за эту транзакцию")
    private Double systemFee;

    /**
     * Валюта всех значений сум, относящихся к системе
     */
    @ApiModelProperty(value = "Валюта всех значений сум, относящихся к системе")
    private Integer systemCurrency;

    /**
     * Курс валюты в копейках
     */
    @ApiModelProperty(value = "Курс валюты в копейках")
    private Integer ccyRate;

    /**
     * Статус транзакции
     */
    @ApiModelProperty(value = "Статус транзакции")
    private TransactionStatus status;

    /**
     * Комментарии к транзакции
     */
    @NotNull
    @Size(max = 128)
    @ApiModelProperty(value = "Комментарии к транзакции", required = true)
    private String comment;

    /**
     * Дата транзакции
     */
    @ApiModelProperty(value = "Дата транзакции")
    private Instant date;

    @NotNull
    private PayType payType;

    @Size(max = 32)
    private String requestSubAccount;

    /**
     * Параметр Merchant Id для соединения с SvGate
     */
    @Size(max = 32)
    @ApiModelProperty(value = "Параметр Merchant Id для соединения с SvGate")
    private String eposMerchant;

    /**
     * Параметр Terminal Id для соединения с SvGate
     */
    @Size(max = 32)
    @ApiModelProperty(value = "Параметр Terminal Id для соединения с SvGate")
    private String eposTerminal;

    /**
     * Главная транзакция
     */
    @ApiModelProperty(value = "Главная транзакция")
    private Boolean main;

    /**
     * Разрядность суммы, 1 - сум, 100 - тийин
     */
    @NotNull
    @ApiModelProperty(value = "Разрядность суммы, 1 - сум, 100 - тийин", required = true)
    private Integer amountRatio;

    /**
     * Название сервиса (bean) отправителя
     */
    @Size(max = 32)
    @ApiModelProperty(value = "Название сервиса (bean) отправителя")
    private String senderBean;

    @Size(max = 64)
    private String category;

    @Size(max = 128)
    @ApiModelProperty(
            value = "Промокод",
            notes = "Короткий код латинскими буквами и цифрами",
            example = "5mln"
    )
    private String promo;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Boolean isSenderSuccess() {
        return senderSuccess;
    }

    public Boolean isRecipientSuccess() {
        return recipientSuccess;
    }

    public Long getRequestAmount() {
        return requestAmount;
    }

    public void setRequestAmount(Long requestAmount) {
        this.requestAmount = requestAmount;
    }

    public Integer getRequestCcy() {
        return requestCcy;
    }

    public void setRequestCcy(Integer requestCcy) {
        this.requestCcy = requestCcy;
    }

    public String getRequestTrId() {
        return requestTrId;
    }

    public void setRequestTrId(String requestTrId) {
        this.requestTrId = requestTrId;
    }

    public String getRequestLogin() {
        return requestLogin;
    }

    public void setRequestLogin(String requestLogin) {
        this.requestLogin = requestLogin;
    }

    public String getRequestGroup() {
        return requestGroup;
    }

    public void setRequestGroup(String requestGroup) {
        this.requestGroup = requestGroup;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public String getSenderAccount() {
        return senderAccount;
    }

    public void setSenderAccount(String senderAccount) {
        this.senderAccount = senderAccount;
    }

    public Long getSenderServiceId() {
        return senderServiceId;
    }

    public void setSenderServiceId(Long senderServiceId) {
        this.senderServiceId = senderServiceId;
    }

    public Long getSenderAmount() {
        return senderAmount;
    }

    public void setSenderAmount(Long senderAmount) {
        this.senderAmount = senderAmount;
    }

    public Integer getSenderCcy() {
        return senderCcy;
    }

    public void setSenderCcy(Integer senderCcy) {
        this.senderCcy = senderCcy;
    }

    public String getSenderTrId() {
        return senderTrId;
    }

    public void setSenderTrId(String senderTrId) {
        this.senderTrId = senderTrId;
    }

    public Boolean getSenderSuccess() {
        return senderSuccess;
    }

    public void setSenderSuccess(Boolean senderSuccess) {
        this.senderSuccess = senderSuccess;
    }

    public Instant getSenderDate() {
        return senderDate;
    }

    public void setSenderDate(Instant senderDate) {
        this.senderDate = senderDate;
    }

    public String getSenderStatus() {
        return senderStatus;
    }

    public void setSenderStatus(String senderStatus) {
        this.senderStatus = senderStatus;
    }

    public String getSenderStatusText() {
        return senderStatusText;
    }

    public void setSenderStatusText(String senderStatusText) {
        this.senderStatusText = senderStatusText;
    }

    public Long getSenderBillId() {
        return senderBillId;
    }

    public void setSenderBillId(Long senderBillId) {
        this.senderBillId = senderBillId;
    }

    public String getSenderGateway() {
        return senderGateway;
    }

    public void setSenderGateway(String senderGateway) {
        this.senderGateway = senderGateway;
    }

    public Double getGatewayAmount() {
        return gatewayAmount;
    }

    public void setGatewayAmount(Double gatewayAmount) {
        this.gatewayAmount = gatewayAmount;
    }

    public String getRecipientName() {
        return recipientName;
    }

    public void setRecipientName(String recipientName) {
        this.recipientName = recipientName;
    }

    public String getRecipientAccount() {
        return recipientAccount;
    }

    public void setRecipientAccount(String recipientAccount) {
        this.recipientAccount = recipientAccount;
    }

    public Long getRecipientServiceId() {
        return recipientServiceId;
    }

    public void setRecipientServiceId(Long recipientServiceId) {
        this.recipientServiceId = recipientServiceId;
    }

    public Long getRecipientAmount() {
        return recipientAmount;
    }

    public void setRecipientAmount(Long recipientAmount) {
        this.recipientAmount = recipientAmount;
    }

    public Integer getRecipientCcy() {
        return recipientCcy;
    }

    public void setRecipientCcy(Integer recipientCcy) {
        this.recipientCcy = recipientCcy;
    }

    public String getRecipientTrId() {
        return recipientTrId;
    }

    public void setRecipientTrId(String recipientTrId) {
        this.recipientTrId = recipientTrId;
    }

    public Boolean getRecipientSuccess() {
        return recipientSuccess;
    }

    public void setRecipientSuccess(Boolean recipientSuccess) {
        this.recipientSuccess = recipientSuccess;
    }

    public Instant getRecipientDate() {
        return recipientDate;
    }

    public void setRecipientDate(Instant recipientDate) {
        this.recipientDate = recipientDate;
    }

    public String getRecipientStatus() {
        return recipientStatus;
    }

    public void setRecipientStatus(String recipientStatus) {
        this.recipientStatus = recipientStatus;
    }

    public String getRecipientStatusText() {
        return recipientStatusText;
    }

    public void setRecipientStatusText(String recipientStatusText) {
        this.recipientStatusText = recipientStatusText;
    }

    public Long getRecipientBillId() {
        return recipientBillId;
    }

    public void setRecipientBillId(Long recipientBillId) {
        this.recipientBillId = recipientBillId;
    }

    public String getRecipientGateway() {
        return recipientGateway;
    }

    public void setRecipientGateway(String recipientGateway) {
        this.recipientGateway = recipientGateway;
    }

    public Long getSystemCommissionUpAmount() {
        return systemCommissionUpAmount;
    }

    public void setSystemCommissionUpAmount(Long systemCommissionUpAmount) {
        this.systemCommissionUpAmount = systemCommissionUpAmount;
    }

    public Long getSystemCommissionDownAmount() {
        return systemCommissionDownAmount;
    }

    public void setSystemCommissionDownAmount(Long systemCommissionDownAmount) {
        this.systemCommissionDownAmount = systemCommissionDownAmount;
    }

    public Long getSystemFeeAmount() {
        return systemFeeAmount;
    }

    public void setSystemFeeAmount(Long systemFeeAmount) {
        this.systemFeeAmount = systemFeeAmount;
    }

    public Long getAgentFeeAmount() {
        return agentFeeAmount;
    }

    public void setAgentFeeAmount(Long agentFeeAmount) {
        this.agentFeeAmount = agentFeeAmount;
    }

    public Double getRoundAmount() {
        return roundAmount;
    }

    public void setRoundAmount(Double roundAmount) {
        this.roundAmount = roundAmount;
    }

    public Double getSystemFee() {
        return systemFee;
    }

    public void setSystemFee(Double systemFee) {
        this.systemFee = systemFee;
    }

    public Integer getSystemCurrency() {
        return systemCurrency;
    }

    public void setSystemCurrency(Integer systemCurrency) {
        this.systemCurrency = systemCurrency;
    }

    public Integer getCcyRate() {
        return ccyRate;
    }

    public void setCcyRate(Integer ccyRate) {
        this.ccyRate = ccyRate;
    }

    public TransactionStatus getStatus() {
        return status;
    }

    public void setStatus(TransactionStatus status) {
        this.status = status;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public Instant getDate() {
        return date;
    }

    public void setDate(Instant date) {
        this.date = date;
    }

    public PayType getPayType() {
        return payType;
    }

    public void setPayType(PayType payType) {
        this.payType = payType;
    }

    public String getRequestSubAccount() {
        return requestSubAccount;
    }

    public void setRequestSubAccount(String requestSubAccount) {
        this.requestSubAccount = requestSubAccount;
    }

    public String getEposMerchant() {
        return eposMerchant;
    }

    public void setEposMerchant(String eposMerchant) {
        this.eposMerchant = eposMerchant;
    }

    public String getEposTerminal() {
        return eposTerminal;
    }

    public void setEposTerminal(String eposTerminal) {
        this.eposTerminal = eposTerminal;
    }

    public Boolean getMain() {
        return main;
    }

    public Boolean isMain() {
        return main;
    }

    public void setMain(Boolean main) {
        this.main = main;
    }

    public Integer getAmountRatio() {
        return amountRatio;
    }

    public void setAmountRatio(Integer amountRatio) {
        this.amountRatio = amountRatio;
    }

    public String getSenderBean() {
        return senderBean;
    }

    public void setSenderBean(String senderBean) {
        this.senderBean = senderBean;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getPromo() {
        return promo;
    }

    public void setPromo(String promo) {
        this.promo = promo;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof TransactionDTO)) {
            return false;
        }

        TransactionDTO transactionDTO = (TransactionDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, transactionDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "TransactionDTO{" +
            "id=" + getId() +
            ", requestAmount=" + getRequestAmount() +
            ", requestCcy=" + getRequestCcy() +
            ", requestTrId='" + getRequestTrId() + "'" +
            ", requestLogin='" + getRequestLogin() + "'" +
            ", requestGroup='" + getRequestGroup() + "'" +
            ", senderName='" + getSenderName() + "'" +
            ", senderAccount='" + getSenderAccount() + "'" +
            ", senderServiceId=" + getSenderServiceId() +
            ", senderAmount=" + getSenderAmount() +
            ", senderCcy=" + getSenderCcy() +
            ", senderTrId='" + getSenderTrId() + "'" +
            ", senderSuccess='" + getSenderSuccess() + "'" +
            ", senderDate='" + getSenderDate() + "'" +
            ", senderStatus='" + getSenderStatus() + "'" +
            ", senderStatusText='" + getSenderStatusText() + "'" +
            ", senderBillId=" + getSenderBillId() +
            ", senderGateway='" + getSenderGateway() + "'" +
            ", gatewayAmount=" + getGatewayAmount() +
            ", recipientName='" + getRecipientName() + "'" +
            ", recipientAccount='" + getRecipientAccount() + "'" +
            ", recipientServiceId=" + getRecipientServiceId() +
            ", recipientAmount=" + getRecipientAmount() +
            ", recipientCcy=" + getRecipientCcy() +
            ", recipientTrId='" + getRecipientTrId() + "'" +
            ", recipientSuccess='" + getRecipientSuccess() + "'" +
            ", recipientDate='" + getRecipientDate() + "'" +
            ", recipientStatus='" + getRecipientStatus() + "'" +
            ", recipientStatusText='" + getRecipientStatusText() + "'" +
            ", recipientBillId=" + getRecipientBillId() +
            ", recipientGateway='" + getRecipientGateway() + "'" +
            ", systemCommissionUpAmount=" + getSystemCommissionUpAmount() +
            ", systemCommissionDownAmount=" + getSystemCommissionDownAmount() +
            ", systemFeeAmount=" + getSystemFeeAmount() +
            ", agentFeeAmount=" + getAgentFeeAmount() +
            ", roundAmount=" + getRoundAmount() +
            ", systemFee=" + getSystemFee() +
            ", systemCurrency=" + getSystemCurrency() +
            ", ccyRate=" + getCcyRate() +
            ", status='" + getStatus() + "'" +
            ", comment='" + getComment() + "'" +
            ", date='" + getDate() + "'" +
            ", payType='" + getPayType() + "'" +
            ", requestSubAccount='" + getRequestSubAccount() + "'" +
            ", eposMerchant='" + getEposMerchant() + "'" +
            ", eposTerminal='" + getEposTerminal() + "'" +
            ", main='" + getMain() + "'" +
            ", amountRatio=" + getAmountRatio() +
            ", senderBean='" + getSenderBean() + "'" +
            ", category='" + getCategory() + "'" +
            ", promo='" + getPromo() + "'" +
            "}";
    }
}
