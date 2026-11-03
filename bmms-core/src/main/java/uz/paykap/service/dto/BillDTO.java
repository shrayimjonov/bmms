package uz.paykap.service.dto;

import io.swagger.annotations.ApiModelProperty;
import java.io.Serializable;
import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import javax.validation.constraints.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import lombok.*;
import uz.devops.bmcore.enumeration.RequestType;

/**
 * A DTO for the {@link uz.paykap.domain.Bill} entity.
 */
@Getter
@Setter
@ToString(callSuper = true)
public class BillDTO extends AbstractAuditingDTO implements Serializable {

    @ApiModelProperty(value = "Номер платежа в системе", notes = "Используется для последующих операций")
    private Long id;

    /**
     * ID получателя
     */
    @ApiModelProperty(value = "Ид. получателя")
    private Long recipientId;

    /**
     * Наименование получателя
     */
    @NotNull
    @Size(max = 64)
    @ApiModelProperty(value = "Наименование получателя", required = true)
    private String recipientShortName;

    /**
     * Тип запроса в биллинг получателя
     */
    @NotNull
    @ApiModelProperty(
        value = "Тип запроса в биллинг получателя",
        notes = "CHECK - проверка счета, PAY - оплата, STATUS - статус платежа, CANCEL - отмена платежа, DEPOSIT - проверка депозита (внут.)"
    )
    private RequestType type;

    /**
     * Разрядность суммы, 1 - сум, 100 - тийин
     */
    @NotNull
    @ApiModelProperty(value = "Соотношение суммы", notes = "Например: 1 - рубли, сумы, доллара; 100 - копейки, тийыны, центы")
    private Integer amountRatio;

    /**
     * Сумма платежа
     */
    @NotNull
    @ApiModelProperty(value = "Сумма платежа", required = true)
    private Long amount;

    /**
     * Лицевой счет
     */
    @NotNull
    @Size(max = 64)
    @ApiModelProperty(value = "Лицевой счет", required = true)
    private String account;

    /**
     * Валюта, трёхзначный цифровой ISO 4217
     */
    @ApiModelProperty(value = "Валюта, трёхзначный цифровой ISO 4217")
    private Integer currency;

    /**
     * Внешний ID транзакции для системы, внутренний ID для получателя
     */
    @Size(max = 32)
    @ApiModelProperty(value = "Внешний ид. транзакции для системы, внутренний ид. для получателя")
    private String extId;

    /**
     * Время запроса в систему получателя
     */
    @NotNull
    @ApiModelProperty(value = "Время запроса в систему получателя", required = true)
    private Instant requestTime;

    /**
     * Параметры запроса в формате JSON
     */
    @Size(max = 512)
    @ApiModelProperty(value = "Параметры запроса в формате JSON")
    private Map<String, Object> requestJson;

    /**
     * Параметры ответа в формате JSON
     */
    @Size(max = 512)
    @ApiModelProperty(value = "Параметры ответа в формате JSON")
    private Map<String, Object> responseJson;

    /**
     * Время ответа от системы получателя
     */
    @NotNull
    @ApiModelProperty(value = "Время ответа от системы получателя", required = true)
    private Instant responseTime;

    /**
     * Код ответа
     */
    @Size(max = 32)
    @ApiModelProperty(value = "Код ответа")
    private String responseCode;

    /**
     * Сообщения ответа
     */
    @Size(max = 128)
    @ApiModelProperty(value = "Сообщения ответа")
    private String responseText;

    /**
     * Статус ответа
     */
    @ApiModelProperty(value = "Статус ответа")
    private Boolean success;

    /**
     * Логин агента
     */
    @Size(max = 64)
    @ApiModelProperty(value = "Логин агента")
    private String login;

    /**
     * Номер транзакции
     */
    @ApiModelProperty(value = "Номер транзакции")
    private Long transactionId;

    /**
     * Наш идентификатор в Процессинговом центре
     */
    @Size(max = 128)
    @ApiModelProperty(value = "Наш ID в процессинговом центре")
    private String outExtId;

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof BillDTO)) {
            return false;
        }

        BillDTO billDTO = (BillDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, billDTO.id);
    }


}
