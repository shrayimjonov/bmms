package uz.paykap.domain;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Immutable;
import uz.devops.bmcore.enumeration.PayType;
import uz.devops.bmcore.enumeration.TransactionStatus;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.Instant;

/**
 * A Transaction.
 */
@Getter
@Setter
//@Entity
//@Immutable
//@Table(name = "v_simple_transaction")
public class SimpleTransaction implements Serializable {

    private static final long serialVersionUID = 123L;

    @Id
    private Long id;

    /**
     * Сумма запроса по валюте запроса, разряд зависит от настроек агента (agent.amountRatio)
     */
    @Column(name = "request_amount")
    private Long requestAmount;

    /**
     * ID запроса, отправляет агент
     */
    @NotNull
    @Column(name = "request_tr_id", length = 72, nullable = false)
    private String requestTrId;

    /**
     * Логин от которого приходит запрос, система сама определяет значение от имени пользователя
     */
    @NotNull
    @Column(name = "request_login", length = 64, nullable = false)
    private String requestLogin;

    /**
     * Наименование отправителя средств
     */
    @Column(name = "sender_name", length = 32)
    private String senderName;

    /**
     * Номер счета с которого снимаются средства
     */
    @Column(name = "sender_account", length = 64)
    private String senderAccount;

    /**
     * Снимающаяся сумма по валюте отправителя, разряд зависит от настроек отправителя (recipient.amountRatio)
     */
    @Column(name = "sender_amount")
    private Long senderAmount;

    /**
     * ID транзакции в системе отправителя
     */
    @Column(name = "sender_tr_id")
    private String senderTrId;

    /**
     * Статус запроса в системе отправителя, нужен для резолюции статуса транзакции
     */
    @Column(name = "sender_status", length = 16)
    private String senderStatus;

    /**
     * Наименование получателя средств
     */
    @Column(name = "recipient_name", length = 32)
    private String recipientName;

    /**
     * Номер счета на которую поступают средства
     */
    @Column(name = "recipient_account", length = 64)
    private String recipientAccount;

    /**
     * Поступающая сумма по валюте получателя, разряд зависит от настроек получателя (recipient.amountRatio)
     */
    @Column(name = "recipient_amount")
    private Long recipientAmount;

    /**
     * ID транзакции в системе получателя
     */
    @Column(name = "recipient_tr_id")
    private String recipientTrId;

    /**
     * Статус запроса в системе получателя, нужен для резолюции статуса транзакции
     */
    @Column(name = "recipient_status", length = 16)
    private String recipientStatus;

    /**
     * Статус транзакции
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private TransactionStatus status;

    /**
     * Дата транзакции
     */
    @Column(name = "date")
    private Instant date;

    @Enumerated(EnumType.STRING)
    @Column(name = "pay_type", nullable = false)
    private PayType payType;

    /**
     * Разрядность суммы, 1 - сум, 100 - тийин
     */
    @Column(name = "amount_ratio", nullable = false)
    private Integer amountRatio;

    @Column(name = "category", length = 64)
    private String category;

}
