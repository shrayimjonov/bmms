package uz.paykap.service.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.Lob;
import javax.validation.constraints.*;
import java.io.Serializable;
import java.util.Objects;

/**
 * A DTO for the {@link uz.paykap.domain.AgentRecipient} entity.
 */
@Getter
@Setter
@ToString
@ApiModel(description = "Доступные поставщики агенту")
public class AgentRecipientDTO extends AbstractAuditingDTO implements Serializable {

    private Long id;

    /**
     * Комиссия агента, которая снимается с внесенной суммы клиента в пользу агента
     */
    @DecimalMin(value = "0")
    @DecimalMax(value = "100")
    @ApiModelProperty(value = "Комиссия агента, которая снимается с внесенной суммы клиента в пользу агента")
    private Double agentCommission;

    /**
     * Вознаграждение агента, это отчисление системы с суммы платежа в пользу агента
     */
    @DecimalMin(value = "0")
    @DecimalMax(value = "100")
    @ApiModelProperty(value = "Вознаграждение агента, это отчисление системы с суммы платежа в пользу агента")
    private Double agentFee;

    /**
     * Комиссия системы, которая снимается сверх внесенной суммы клиента в пользу системы
     */
//    @DecimalMin(value = "0")
    @DecimalMax(value = "100")
    @ApiModelProperty(value = "Комиссия системы, которая снимается сверх внесенной суммы клиента в пользу системы")
    private Double systemCommissionUp;

    /**
     * Комиссия системы, которая снимается с внесенной суммы клиента в пользу системы
     */
    @DecimalMin(value = "0")
    @DecimalMax(value = "100")
    @ApiModelProperty(value = "Комиссия системы, которая снимается с внесенной суммы клиента в пользу системы")
    private Double systemCommissionDown;

    /**
     * Вознаграждение системы, это отчисления поставщика услуг с суммы платежа в пользу системы
     */
    @DecimalMin(value = "0")
    @DecimalMax(value = "100")
    @ApiModelProperty(value = "Вознаграждение системы, это отчисления поставщика услуг с суммы платежа в пользу системы")
    private Double systemFee;

    @NotNull
    private Boolean active;

    /**
     * Параметр Port для соединения с SvGate
     */
    @Min(value = 0)
    @Max(value = 65535)
    @ApiModelProperty(value = "Параметр Port для соединения с SvGate")
    private Integer eposPort;

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

    @Size(max = 64)
    private String requestGroup;

    /**
     * Дополнительные параметры в виде json
     */
    @ApiModelProperty(value = "Дополнительные параметры в виде json")
    @Lob
    private String jsonParams;

    private AgentDTO agent;

    private RecipientDTO recipient;

    private SettingDTO setting;

    private SettingTypeDTO settingType;

    private RecipientGroupDTO recipientGroup;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof AgentRecipientDTO)) {
            return false;
        }

        AgentRecipientDTO agentRecipientDTO = (AgentRecipientDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, agentRecipientDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    public AgentRecipientDTO agentCommission(Double agentCommission) {
        this.agentCommission = agentCommission;
        return this;
    }

    public AgentRecipientDTO agentFee(Double agentFee) {
        this.agentFee = agentFee;
        return this;
    }

    public AgentRecipientDTO systemCommissionUp(Double systemCommissionUp) {
        this.systemCommissionUp = systemCommissionUp;
        return this;
    }

    public AgentRecipientDTO systemCommissionDown(Double systemCommissionDown) {
        this.systemCommissionDown = systemCommissionDown;
        return this;
    }

    public AgentRecipientDTO systemFee(Double systemFee) {
        this.systemFee = systemFee;
        return this;
    }

    public AgentRecipientDTO active(Boolean active) {
        this.active = active;
        return this;
    }

    public AgentRecipientDTO agent(AgentDTO agent) {
        this.agent = agent;
        return this;
    }

    public AgentRecipientDTO recipient(RecipientDTO recipient) {
        this.recipient = recipient;
        return this;
    }

    public AgentRecipientDTO eposPort(Integer eposPort) {
        this.eposPort = eposPort;
        return this;
    }

    public AgentRecipientDTO settingType(SettingTypeDTO settingType) {
        this.settingType = settingType;
        return this;
    }

    public AgentRecipientDTO eposMerchant(String eposMerchant) {
        this.eposMerchant = eposMerchant;
        return this;
    }

    public AgentRecipientDTO eposTerminal(String eposTerminal) {
        this.eposTerminal = eposTerminal;
        return this;
    }

    public AgentRecipientDTO requestGroup(String requestGroup) {
        this.requestGroup = requestGroup;
        return this;
    }

    public AgentRecipientDTO setting(SettingDTO setting) {
        this.setting = setting;
        return this;
    }

    public AgentRecipientDTO jsonParams(String jsonParams) {
        this.jsonParams = jsonParams;
        return this;
    }
}
