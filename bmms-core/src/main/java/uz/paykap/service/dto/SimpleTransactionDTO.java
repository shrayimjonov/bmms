package uz.paykap.service.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import uz.devops.bmcore.enumeration.PayType;
import uz.devops.bmcore.enumeration.TransactionStatus;

import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.time.Instant;

/**
 * Simple transaction projection for the UI.
 */
@Data
public class SimpleTransactionDTO extends AbstractAuditingDTO implements Serializable {

    private Long id;

    /**
     * Сумма запроса по валюте запроса, разряд зависит от настроек агента (agent.amountRatio)
     */
    @ApiModelProperty(value = "Сумма запроса по валюте запроса, разряд зависит от настроек агента (agent.amountRatio)")
    private Long requestAmount;

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
     * Снимающаяся сумма по валюте отправителя, разряд зависит от настроек отправителя (recipient.amountRatio)
     */
    @ApiModelProperty(value = "Снимающаяся сумма по валюте отправителя, разряд зависит от настроек отправителя (recipient.amountRatio)")
    private Long senderAmount;

    /**
     * ID транзакции в системе отправителя
     */
    @ApiModelProperty(value = "Ид. транзакции в системе отправителя")
    private String senderTrId;

    /**
     * Статус запроса в системе отправителя, нужен для резолюции статуса транзакции
     */
    @Size(max = 16)
    @ApiModelProperty(value = "Статус запроса в системе отправителя, нужен для резолюции статуса транзакции")
    private String senderStatus;

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
     * Поступающая сумма по валюте получателя, разряд зависит от настроек получателя (recipient.amountRatio)
     */
    @ApiModelProperty(value = "Поступающая сумма по валюте получателя, разряд зависит от настроек получателя (recipient.amountRatio)")
    private Long recipientAmount;

    /**
     * ID транзакции в системе получателя
     */
    @ApiModelProperty(value = "Ид. транзакции в системе получателя")
    private String recipientTrId;

    /**
     * Статус запроса в системе получателя, нужен для резолюции статуса транзакции
     */
    @Size(max = 16)
    @ApiModelProperty(value = "Статус запроса в системе получателя, нужен для резолюции статуса транзакции")
    private String recipientStatus;

    /**
     * Статус транзакции
     */
    @ApiModelProperty(value = "Статус транзакции")
    private TransactionStatus status;

    /**
     * Дата транзакции
     */
    @ApiModelProperty(value = "Дата транзакции")
    private Instant date;

    @NotNull
    private PayType payType;

    /**
     * Разрядность суммы, 1 - сум, 100 - тийин
     */
    @NotNull
    @ApiModelProperty(value = "Разрядность суммы, 1 - сум, 100 - тийин", required = true)
    private Integer amountRatio;

    @Size(max = 64)
    private String category;

}
