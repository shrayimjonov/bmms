package uz.paykap.domain;

import org.apache.commons.lang3.StringUtils;
import org.javers.core.metamodel.annotation.DiffIgnore;
import uz.devops.bmcore.enumeration.PayType;
import uz.devops.bmcore.enumeration.TransactionStatus;

import javax.persistence.*;
import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

/**
 * A Transaction.
 */
@Entity
@Table(name = "transaction")
@DiffIgnore
public class Transaction extends AbstractAuditingEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    private Long id;

    /**
     * Сумма запроса по валюте запроса, разряд зависит от настроек агента (agent.amountRatio)
     */
    @Column(name = "request_amount")
    private Long requestAmount;

    /**
     * Валюта запроса, значения могут быть ограничены в зависимости от настроек агента
     */
    @Column(name = "request_ccy")
    private Integer requestCcy;

    /**
     * ID запроса, отправляет агент
     */
    @NotNull
    @Size(max = 72)
    @Column(name = "request_tr_id", length = 72, nullable = false)
    private String requestTrId;

    /**
     * Логин от которого приходит запрос, система сама определяет значение от имени пользователя
     */
    @NotNull
    @Size(max = 64)
    @Column(name = "request_login", length = 64, nullable = false)
    private String requestLogin;

    /**
     * Используется для группировки запросов
     */
    @Size(max = 64)
    @Column(name = "request_group", length = 64)
    private String requestGroup;

    /**
     * Наименование отправителя средств
     */
    @Size(max = 32)
    @Column(name = "sender_name", length = 32)
    private String senderName;

    /**
     * Номер счета с которого снимаются средства
     */
    @Size(max = 64)
    @Column(name = "sender_account", length = 64)
    private String senderAccount;

    /**
     * ID сервиса отправителя средств
     */
    @Column(name = "sender_service_id")
    private Long senderServiceId = 0L;

    /**
     * Снимающаяся сумма по валюте отправителя, разряд зависит от настроек отправителя (recipient.amountRatio)
     */
    @Column(name = "sender_amount")
    private Long senderAmount;

    /**
     * Валюта отправителя (recipient.ccy)
     */
    @Column(name = "sender_ccy")
    private Integer senderCcy;

    /**
     * ID транзакции в системе отправителя
     */
    @Column(name = "sender_tr_id")
    private String senderTrId;

    /**
     * Успешность запроса из системы отправителя
     */
    @Column(name = "sender_success")
    private Boolean senderSuccess;

    /**
     * Дата в системе отправителя, нужен для сверок
     */
    @Column(name = "sender_date")
    private Instant senderDate;

    /**
     * Статус запроса в системе отправителя, нужен для резолюции статуса транзакции
     */
    @Size(max = 16)
    @Column(name = "sender_status", length = 16)
    private String senderStatus;

    /**
     * Текст статус запроса в системе отправителя, для админа
     */
    @Size(max = 128)
    @Column(name = "sender_status_text", length = 128)
    private String senderStatusText;

    /**
     * ID чека отправителя из таблицы bill (bill.id)
     */
    @Column(name = "sender_bill_id")
    private Long senderBillId = 0L;

    /**
     * Наименование шлюза отправителя
     */
    @Column(name = "sender_gateway")
    private String senderGateway;

    /**
     * Поступающая сумма по валюте получателя, разряд и валюта зависит от настроек получателя (recipient.amountRatio и recipient.ccy)
     */
    @Column(name = "gateway_amount")
    private Double gatewayAmount;

    /**
     * Наименование получателя средств
     */
    @Size(max = 32)
    @Column(name = "recipient_name", length = 32)
    private String recipientName;

    /**
     * Номер счета на которую поступают средства
     */
    @Size(max = 64)
    @Column(name = "recipient_account", length = 64)
    private String recipientAccount;

    /**
     * ID сервиса получателя средств
     */
    @Column(name = "recipient_service_id")
    private Long recipientServiceId;

    /**
     * Поступающая сумма по валюте получателя, разряд зависит от настроек получателя (recipient.amountRatio)
     */
    @Column(name = "recipient_amount")
    private Long recipientAmount;

    /**
     * Валюта получателя (recipient.ccy)
     */
    @Column(name = "recipient_ccy")
    private Integer recipientCcy;

    /**
     * ID транзакции в системе получателя
     */
    @Column(name = "recipient_tr_id")
    private String recipientTrId;

    @Column(name = "recipient_success")
    private Boolean recipientSuccess;

    /**
     * Дата в системе получателя, нужен для сверок
     */
    @Column(name = "recipient_date")
    private Instant recipientDate;

    /**
     * Статус запроса в системе получателя, нужен для резолюции статуса транзакции
     */
    @Size(max = 16)
    @Column(name = "recipient_status", length = 16)
    private String recipientStatus;

    /**
     * Текст статус запроса в системе получателя, для админа
     */
    @Size(max = 128)
    @Column(name = "recipient_status_text", length = 128)
    private String recipientStatusText;

    /**
     * ID чека получателя из таблицы bill (bill.id)
     */
    @Column(name = "recipient_bill_id")
    private Long recipientBillId;

    /**
     * Наименование шлюза получателя
     */
    @Column(name = "recipient_gateway")
    private String recipientGateway;

    /**
     * Комиссия системы сверху суммы
     */
    @Column(name = "system_commission_up_amount")
    private Long systemCommissionUpAmount;

    /**
     * Комиссия системы с суммы
     */
    @Column(name = "system_commission_down_amount")
    private Long systemCommissionDownAmount;

    /**
     * Сумма всех вознаграждений системы за эту транзакцию
     */
    @Column(name = "system_fee_amount")
    private Long systemFeeAmount;

    /**
     * Сумма вознаграждения агента
     */
    @Column(name = "agent_fee_amount")
    private Long agentFeeAmount;

    /**
     * Сумма округлении gatewayAmount после сотой дроби, amount = 12.34567 -> gatewayAmount = 12.34, roundAmount = 0.00567
     */
    @DecimalMin(value = "0")
    @Column(name = "round_amount")
    private Double roundAmount;

    /**
     * Процент вознаграждений системы за эту транзакцию
     */
    @DecimalMin(value = "0")
    @DecimalMax(value = "100")
    @Column(name = "system_fee")
    private Double systemFee;

    /**
     * Валюта всех значений сум, относящихся к системе
     */
    @Column(name = "system_currency")
    private Integer systemCurrency;

    /**
     * Курс валюты в копейках
     */
    @Column(name = "ccy_rate")
    private Integer ccyRate;

    /**
     * Статус транзакции
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private TransactionStatus status;

    /**
     * Комментарии к транзакции
     */
    @NotNull
    @Size(max = 128)
    @Column(name = "comment", length = 128, nullable = false)
    private String comment = "";

    /**
     * Дата транзакции
     */
    @Column(name = "date")
    private Instant date;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "pay_type", nullable = false)
    private PayType payType;

    @Size(max = 32)
    @Column(name = "request_sub_account", length = 32)
    private String requestSubAccount;

    /**
     * Параметр Merchant Id для соединения с SvGate
     */
    @Size(max = 32)
    @Column(name = "epos_merchant", length = 32)
    private String eposMerchant;

    /**
     * Параметр Terminal Id для соединения с SvGate
     */
    @Size(max = 32)
    @Column(name = "epos_terminal", length = 32)
    private String eposTerminal;

    /**
     * Главная транзакция
     */
    @Column(name = "main")
    private Boolean main = true;

    /**
     * Разрядность суммы, 1 - сум, 100 - тийин
     */
    @NotNull
    @Column(name = "amount_ratio", nullable = false)
    private Integer amountRatio;

    /**
     * Название сервиса (bean) отправителя
     */
    @Size(max = 32)
    @Column(name = "sender_bean", length = 32)
    private String senderBean;

    @Size(max = 64)
    @Column(name = "category", length = 64)
    private String category;

    @Size(max = 128)
    @Column(name = "promo", length = 128)
    private String promo;

    // jhipster-needle-entity-add-field - JHipster will add fields here
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Transaction id(Long id) {
        this.id = id;
        return this;
    }

    public Long getRequestAmount() {
        return this.requestAmount;
    }

    public Transaction requestAmount(Long requestAmount) {
        this.requestAmount = requestAmount;
        return this;
    }

    public void setRequestAmount(Long requestAmount) {
        this.requestAmount = requestAmount;
    }

    public Integer getRequestCcy() {
        return this.requestCcy;
    }

    public Transaction requestCcy(Integer requestCcy) {
        this.requestCcy = requestCcy;
        return this;
    }

    public void setRequestCcy(Integer requestCcy) {
        this.requestCcy = requestCcy;
    }

    public String getRequestTrId() {
        return this.requestTrId;
    }

    public Transaction requestTrId(String requestTrId) {
        this.requestTrId = requestTrId;
        return this;
    }

    public void setRequestTrId(String requestTrId) {
        this.requestTrId = requestTrId;
    }

    public String getRequestLogin() {
        return this.requestLogin;
    }

    public Transaction requestLogin(String requestLogin) {
        this.requestLogin = requestLogin;
        return this;
    }

    public void setRequestLogin(String requestLogin) {
        this.requestLogin = requestLogin;
    }

    public String getRequestGroup() {
        return this.requestGroup;
    }

    public Transaction requestGroup(String requestGroup) {
        this.requestGroup = requestGroup;
        return this;
    }

    public void setRequestGroup(String requestGroup) {
        this.requestGroup = requestGroup;
    }

    public String getSenderName() {
        return this.senderName;
    }

    public Transaction senderName(String senderName) {
        this.senderName = senderName;
        return this;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public String getSenderAccount() {
        return this.senderAccount;
    }

    public Transaction senderAccount(String senderAccount) {
        this.senderAccount = senderAccount;
        return this;
    }

    public void setSenderAccount(String senderAccount) {
        this.senderAccount = senderAccount;
    }

    public Long getSenderServiceId() {
        return this.senderServiceId;
    }

    public Transaction senderServiceId(Long senderServiceId) {
        this.senderServiceId = senderServiceId;
        return this;
    }

    public void setSenderServiceId(Long senderServiceId) {
        this.senderServiceId = senderServiceId;
    }

    public Long getSenderAmount() {
        return this.senderAmount;
    }

    public Transaction senderAmount(Long senderAmount) {
        this.senderAmount = senderAmount;
        return this;
    }

    public void setSenderAmount(Long senderAmount) {
        this.senderAmount = senderAmount;
    }

    public Integer getSenderCcy() {
        return this.senderCcy;
    }

    public Transaction senderCcy(Integer senderCcy) {
        this.senderCcy = senderCcy;
        return this;
    }

    public void setSenderCcy(Integer senderCcy) {
        this.senderCcy = senderCcy;
    }

    public String getSenderTrId() {
        return this.senderTrId;
    }

    public Transaction senderTrId(String senderTrId) {
        this.senderTrId = senderTrId;
        return this;
    }

    public void setSenderTrId(String senderTrId) {
        this.senderTrId = senderTrId;
    }

    public Boolean getSenderSuccess() {
        return this.senderSuccess;
    }

    public Boolean isSenderSuccess() {
        return senderSuccess;
    }

    public Transaction senderSuccess(Boolean senderSuccess) {
        this.senderSuccess = senderSuccess;
        return this;
    }

    public void setSenderSuccess(Boolean senderSuccess) {
        this.senderSuccess = senderSuccess;
    }

    public Instant getSenderDate() {
        return this.senderDate;
    }

    public Transaction senderDate(Instant senderDate) {
        this.senderDate = senderDate;
        return this;
    }

    public void setSenderDate(Instant senderDate) {
        this.senderDate = senderDate;
    }

    public String getSenderStatus() {
        return this.senderStatus;
    }

    public Transaction senderStatus(String senderStatus) {
        if (senderStatus == null || senderStatus.isEmpty()) {
            this.senderStatus = "-";
        } else {
            this.senderStatus = StringUtils.left(senderStatus, 16);
        }
        return this;
    }

    public void setSenderStatus(String senderStatus) {
        this.senderStatus = senderStatus;
    }

    public String getSenderStatusText() {
        return this.senderStatusText;
    }

    public Transaction senderStatusText(String senderStatusText) {
        if (senderStatusText == null || senderStatusText.isEmpty()) {
            this.senderStatusText = "-";
        } else {
            this.senderStatusText = StringUtils.left(senderStatusText, 128);
        }
        //        this.senderStatusText = senderStatusText;
        return this;
    }

    public void setSenderStatusText(String senderStatusText) {
        if (senderStatusText == null || senderStatusText.isEmpty()) {
            this.senderStatusText = "-";
        } else {
            this.senderStatusText = StringUtils.left(senderStatusText, 128);
        }
        //        this.senderStatusText = senderStatusText;
    }

    public Long getSenderBillId() {
        return this.senderBillId;
    }

    public Transaction senderBillId(Long senderBillId) {
        this.senderBillId = senderBillId;
        return this;
    }

    public void setSenderBillId(Long senderBillId) {
        this.senderBillId = senderBillId;
    }

    public String getSenderGateway() {
        return this.senderGateway;
    }

    public Transaction senderGateway(String senderGateway) {
        this.senderGateway = senderGateway;
        return this;
    }

    public void setSenderGateway(String senderGateway) {
        this.senderGateway = senderGateway;
    }

    public Double getGatewayAmount() {
        return this.gatewayAmount;
    }

    public Transaction gatewayAmount(Double gatewayAmount) {
        this.gatewayAmount = gatewayAmount;
        return this;
    }

    public void setGatewayAmount(Double gatewayAmount) {
        this.gatewayAmount = gatewayAmount;
    }

    public String getRecipientName() {
        return this.recipientName;
    }

    public Transaction recipientName(String recipientName) {
        this.recipientName = recipientName;
        return this;
    }

    public void setRecipientName(String recipientName) {
        this.recipientName = recipientName;
    }

    public String getRecipientAccount() {
        return this.recipientAccount;
    }

    public Transaction recipientAccount(String recipientAccount) {
        this.recipientAccount = recipientAccount;
        return this;
    }

    public void setRecipientAccount(String recipientAccount) {
        this.recipientAccount = recipientAccount;
    }

    public Long getRecipientServiceId() {
        return this.recipientServiceId;
    }

    public Transaction recipientServiceId(Long recipientServiceId) {
        this.recipientServiceId = recipientServiceId;
        return this;
    }

    public void setRecipientServiceId(Long recipientServiceId) {
        this.recipientServiceId = recipientServiceId;
    }

    public Long getRecipientAmount() {
        return this.recipientAmount;
    }

    public Transaction recipientAmount(Long recipientAmount) {
        this.recipientAmount = recipientAmount;
        return this;
    }

    public void setRecipientAmount(Long recipientAmount) {
        this.recipientAmount = recipientAmount;
    }

    public Integer getRecipientCcy() {
        return this.recipientCcy;
    }

    public Transaction recipientCcy(Integer recipientCcy) {
        this.recipientCcy = recipientCcy;
        return this;
    }

    public void setRecipientCcy(Integer recipientCcy) {
        this.recipientCcy = recipientCcy;
    }

    public String getRecipientTrId() {
        return this.recipientTrId;
    }

    public Transaction recipientTrId(String recipientTrId) {
        this.recipientTrId = recipientTrId;
        return this;
    }

    public void setRecipientTrId(String recipientTrId) {
        this.recipientTrId = recipientTrId;
    }

    public Boolean getRecipientSuccess() {
        return this.recipientSuccess;
    }

    public Boolean isRecipientSuccess() {
        return recipientSuccess;
    }

    public Transaction recipientSuccess(Boolean recipientSuccess) {
        this.recipientSuccess = recipientSuccess;
        return this;
    }

    public void setRecipientSuccess(Boolean recipientSuccess) {
        this.recipientSuccess = recipientSuccess;
    }

    public Instant getRecipientDate() {
        return this.recipientDate;
    }

    public Transaction recipientDate(Instant recipientDate) {
        this.recipientDate = recipientDate;
        return this;
    }

    public void setRecipientDate(Instant recipientDate) {
        this.recipientDate = recipientDate;
    }

    public String getRecipientStatus() {
        return this.recipientStatus;
    }

    public Transaction recipientStatus(String recipientStatus) {
        this.recipientStatus = recipientStatus;
        return this;
    }

    public void setRecipientStatus(String recipientStatus) {
        this.recipientStatus = recipientStatus;
    }

    public String getRecipientStatusText() {
        return this.recipientStatusText;
    }

    public Transaction recipientStatusText(String recipientStatusText) {
        if (recipientStatusText == null || recipientStatusText.isEmpty()) {
            this.recipientStatusText = "-";
        } else {
            this.recipientStatusText = StringUtils.left(recipientStatusText, 128);
        }
        //        this.recipientStatusText = recipientStatusText;
        return this;
    }

    public Transaction nextRecipientStatusText(String recipientStatusText) {
        return recipientStatusText(this.getRecipientStatusText() + " > " + recipientStatusText);
    }

    public Transaction nextSenderStatusText(String senderStatusText) {
        return senderStatusText(this.getSenderStatusText() + " > " + senderStatusText);
    }

    public Transaction nextRecipientStatusText(TransactionStatus status) {
        return nextRecipientStatusText(String.valueOf(status));
    }

    public void setRecipientStatusText(String recipientStatusText) {
        if (recipientStatusText == null || recipientStatusText.isEmpty()) {
            this.recipientStatusText = "-";
        } else {
            this.recipientStatusText = StringUtils.left(recipientStatusText, 128);
        }
        //        this.recipientStatusText = recipientStatusText;
    }

    public Long getRecipientBillId() {
        return this.recipientBillId;
    }

    public Transaction recipientBillId(Long recipientBillId) {
        this.recipientBillId = recipientBillId;
        return this;
    }

    public void setRecipientBillId(Long recipientBillId) {
        this.recipientBillId = recipientBillId;
    }

    public String getRecipientGateway() {
        return this.recipientGateway;
    }

    public Transaction recipientGateway(String recipientGateway) {
        this.recipientGateway = recipientGateway;
        return this;
    }

    public void setRecipientGateway(String recipientGateway) {
        this.recipientGateway = recipientGateway;
    }

    public Long getSystemCommissionUpAmount() {
        return this.systemCommissionUpAmount;
    }

    public Transaction systemCommissionUpAmount(Long systemCommissionUpAmount) {
        this.systemCommissionUpAmount = systemCommissionUpAmount;
        return this;
    }

    public void setSystemCommissionUpAmount(Long systemCommissionUpAmount) {
        this.systemCommissionUpAmount = systemCommissionUpAmount;
    }

    public Long getSystemCommissionDownAmount() {
        return this.systemCommissionDownAmount;
    }

    public Transaction systemCommissionDownAmount(Long systemCommissionDownAmount) {
        this.systemCommissionDownAmount = systemCommissionDownAmount;
        return this;
    }

    public void setSystemCommissionDownAmount(Long systemCommissionDownAmount) {
        this.systemCommissionDownAmount = systemCommissionDownAmount;
    }

    public Long getSystemFeeAmount() {
        return this.systemFeeAmount;
    }

    public Transaction systemFeeAmount(Long systemFeeAmount) {
        this.systemFeeAmount = systemFeeAmount;
        return this;
    }

    public void setSystemFeeAmount(Long systemFeeAmount) {
        this.systemFeeAmount = systemFeeAmount;
    }

    public Long getAgentFeeAmount() {
        return this.agentFeeAmount;
    }

    public Transaction agentFeeAmount(Long agentFeeAmount) {
        this.agentFeeAmount = agentFeeAmount;
        return this;
    }

    public void setAgentFeeAmount(Long agentFeeAmount) {
        this.agentFeeAmount = agentFeeAmount;
    }

    public Double getRoundAmount() {
        return this.roundAmount;
    }

    public Transaction roundAmount(Double roundAmount) {
        this.roundAmount = roundAmount;
        return this;
    }

    public void setRoundAmount(Double roundAmount) {
        this.roundAmount = roundAmount;
    }

    public Double getSystemFee() {
        return this.systemFee;
    }

    public Transaction systemFee(Double systemFee) {
        this.systemFee = systemFee;
        return this;
    }

    public void setSystemFee(Double systemFee) {
        this.systemFee = systemFee;
    }

    public Integer getSystemCurrency() {
        return this.systemCurrency;
    }

    public Transaction systemCurrency(Integer systemCurrency) {
        this.systemCurrency = systemCurrency;
        return this;
    }

    public void setSystemCurrency(Integer systemCurrency) {
        this.systemCurrency = systemCurrency;
    }

    public Integer getCcyRate() {
        return this.ccyRate;
    }

    public Transaction ccyRate(Integer ccyRate) {
        this.ccyRate = ccyRate;
        return this;
    }

    public void setCcyRate(Integer ccyRate) {
        this.ccyRate = ccyRate;
    }

    public TransactionStatus getStatus() {
        return this.status;
    }

    public Transaction status(TransactionStatus status) {
        this.status = status;
        return this;
    }

    public void setStatus(TransactionStatus status) {
        this.status = status;
    }

    public String getComment() {
        return this.comment;
    }

    public Transaction comment(String comment) {
        this.comment = comment;
        return this;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public Instant getDate() {
        return this.date;
    }

    public Transaction date(Instant date) {
        this.date = date;
        return this;
    }

    public void setDate(Instant date) {
        this.date = date;
    }

    public PayType getPayType() {
        return this.payType;
    }

    public Transaction payType(PayType payType) {
        this.payType = payType;
        return this;
    }

    public void setPayType(PayType payType) {
        this.payType = payType;
    }

    public String getRequestSubAccount() {
        return this.requestSubAccount;
    }

    public Transaction requestSubAccount(String requestSubAccount) {
        this.requestSubAccount = requestSubAccount;
        return this;
    }

    public void setRequestSubAccount(String requestSubAccount) {
        this.requestSubAccount = requestSubAccount;
    }

    public String getEposMerchant() {
        return this.eposMerchant;
    }

    public Transaction eposMerchant(String eposMerchant) {
        if (eposMerchant == null || eposMerchant.isEmpty()) {
            this.eposMerchant = "-";
        } else {
            this.eposMerchant = StringUtils.left(eposMerchant, 32);
        }
        //        this.eposMerchant = eposMerchant;
        return this;
    }

    public void setEposMerchant(String eposMerchant) {
        if (eposMerchant == null || eposMerchant.isEmpty()) {
            this.eposMerchant = "-";
        } else {
            this.eposMerchant = StringUtils.left(eposMerchant, 32);
        }
    }

    public String getEposTerminal() {
        return this.eposTerminal;
    }

    public Transaction eposTerminal(String eposTerminal) {
        if (eposTerminal == null || eposTerminal.isEmpty()) {
            this.eposTerminal = "-";
        } else {
            this.eposTerminal = StringUtils.left(eposTerminal, 32);
        }
        //        this.eposTerminal = eposTerminal;
        return this;
    }

    public void setEposTerminal(String eposTerminal) {
        if (eposTerminal == null || eposTerminal.isEmpty()) {
            this.eposTerminal = "-";
        } else {
            this.eposTerminal = StringUtils.left(eposTerminal, 32);
        }
    }

    public Boolean getMain() {
        return this.main;
    }

    public Boolean isMain() {
        return main;
    }

    public Transaction main(Boolean main) {
        this.main = Objects.equals(main, Boolean.TRUE);
        return this;
    }

    public void setMain(Boolean main) {
        this.main = Objects.equals(main, Boolean.TRUE);
    }

    public Integer getAmountRatio() {
        return this.amountRatio;
    }

    public Transaction amountRatio(Integer amountRatio) {
        this.amountRatio = amountRatio;
        return this;
    }

    public void setAmountRatio(Integer amountRatio) {
        this.amountRatio = amountRatio;
    }

    public String getSenderBean() {
        return this.senderBean;
    }

    public Transaction senderBean(String senderBean) {
        this.senderBean = senderBean;
        return this;
    }

    public void setSenderBean(String senderBean) {
        this.senderBean = senderBean;
    }

    public String getCategory() {
        return this.category;
    }

    public Transaction category(String category) {
        this.category = category;
        return this;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getPromo() {
        return this.promo;
    }

    public Transaction promo(String promo) {
        this.promo = promo;
        return this;
    }

    public void setPromo(String promo) {
        this.promo = promo;
    }

    public String getFullStatusText() {
        return (
            (senderStatusText != null ? senderStatusText : "no sender text") +
            ", " +
            (recipientStatusText != null ? recipientStatusText : "no recipient text")
        );
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    public Transaction fillSenderPart(Bill senderBill) {
        return
                this/*.senderAmount(senderBill.getAmount())*/
            .senderCcy(senderBill.getCurrency())
            .senderTrId(senderBill.getExtId())
            .senderSuccess(senderBill.isSuccess())
            .senderDate(senderBill.getRequestTime())
            .senderStatus(senderBill.getResponseCode())
            .senderStatusText(senderBill.getResponseText())
            .senderBillId(senderBill.getId());
    }

    public Transaction fillRecipientPart(Bill recipientBill) {
        return this/*.recipientAmount(recipientBill.getAmount())*/
            .recipientCcy(recipientBill.getCurrency())
            .recipientTrId(recipientBill.getExtId())
            .recipientSuccess(recipientBill.isSuccess())
            .recipientDate(recipientBill.getRequestTime())
            .recipientStatus(recipientBill.getResponseCode())
            .recipientStatusText(recipientBill.getResponseText())
            .recipientBillId(recipientBill.getId());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Transaction)) {
            return false;
        }
        return id != null && id.equals(((Transaction) o).id);
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "Transaction{" +
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
