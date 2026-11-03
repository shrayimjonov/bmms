package uz.paykap.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.persistence.*;
import javax.validation.constraints.*;
import java.io.Serializable;

/**
 * Доступные поставщики агенту
 */
@Entity
@Table(name = "agent_recipient")
public class AgentRecipient extends AbstractAuditingEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    private Long id;

    /**
     * Комиссия агента, которая снимается с внесенной суммы клиента в пользу агента
     */
    @DecimalMin(value = "0")
    @DecimalMax(value = "100")
    @Column(name = "agent_commission")
    private Double agentCommission;

    /**
     * Вознаграждение агента, это отчисление системы с суммы платежа в пользу агента
     */
    @DecimalMin(value = "0")
    @DecimalMax(value = "100")
    @Column(name = "agent_fee")
    private Double agentFee;

    /**
     * Комиссия системы, которая снимается сверх внесенной суммы клиента в пользу системы
     */
//    @DecimalMin(value = "0")
    @DecimalMax(value = "100")
    @Column(name = "system_commission_up")
    private Double systemCommissionUp;

    /**
     * Комиссия системы, которая снимается с внесенной суммы клиента в пользу системы
     */
    @DecimalMin(value = "0")
    @DecimalMax(value = "100")
    @Column(name = "system_commission_down")
    private Double systemCommissionDown;

    /**
     * Вознаграждение системы, это отчисления поставщика услуг с суммы платежа в пользу системы
     */
    @DecimalMin(value = "0")
    @DecimalMax(value = "100")
    @Column(name = "system_fee")
    private Double systemFee;

    @NotNull
    @Column(name = "active", nullable = false)
    private Boolean active;

    /**
     * Параметр Port для соединения с SvGate
     */
    @Min(value = 0)
    @Max(value = 65535)
    @Column(name = "epos_port")
    private Integer eposPort;

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

    @Size(max = 64)
    @Column(name = "request_group", length = 64)
    private String requestGroup;

    /**
     * Дополнительные параметры в виде json
     */
    @Lob
    @Column(name = "json_params")
    private String jsonParams;

    @ManyToOne
    @JsonIgnoreProperties(value = { "gateway", "category", "titles", "fullNames" }, allowSetters = true)
    private Recipient recipient;

    @ManyToOne
    @JsonIgnoreProperties(value = { "props" }, allowSetters = true)
    private Setting setting;

    @ManyToOne
    @JsonIgnoreProperties(value = { "titles", "deposit" }, allowSetters = true)
    private Agent agent;

    @ManyToOne
    @JsonIgnoreProperties(value = { "recipients" }, allowSetters = true)
    private RecipientGroup recipientGroup;

    // jhipster-needle-entity-add-field - JHipster will add fields here
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public AgentRecipient id(Long id) {
        this.id = id;
        return this;
    }

    public Double getAgentCommission() {
        return this.agentCommission;
    }

    public AgentRecipient agentCommission(Double agentCommission) {
        this.agentCommission = agentCommission;
        return this;
    }

    public void setAgentCommission(Double agentCommission) {
        this.agentCommission = agentCommission;
    }

    public Double getAgentFee() {
        return this.agentFee;
    }

    public AgentRecipient agentFee(Double agentFee) {
        this.agentFee = agentFee;
        return this;
    }

    public void setAgentFee(Double agentFee) {
        this.agentFee = agentFee;
    }

    public Double getSystemCommissionUp() {
        return this.systemCommissionUp;
    }

    public AgentRecipient systemCommissionUp(Double systemCommissionUp) {
        this.systemCommissionUp = systemCommissionUp;
        return this;
    }

    public void setSystemCommissionUp(Double systemCommissionUp) {
        this.systemCommissionUp = systemCommissionUp;
    }

    public Double getSystemCommissionDown() {
        return this.systemCommissionDown;
    }

    public AgentRecipient systemCommissionDown(Double systemCommissionDown) {
        this.systemCommissionDown = systemCommissionDown;
        return this;
    }

    public void setSystemCommissionDown(Double systemCommissionDown) {
        this.systemCommissionDown = systemCommissionDown;
    }

    public Double getSystemFee() {
        return this.systemFee;
    }

    public AgentRecipient systemFee(Double systemFee) {
        this.systemFee = systemFee;
        return this;
    }

    public void setSystemFee(Double systemFee) {
        this.systemFee = systemFee;
    }

    public Boolean isActive() {
        return this.active;
    }

    public Boolean getActive() {
        return this.active;
    }

    public AgentRecipient active(Boolean active) {
        this.active = active;
        return this;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public Integer getEposPort() {
        return this.eposPort;
    }

    public AgentRecipient eposPort(Integer eposPort) {
        this.eposPort = eposPort;
        return this;
    }

    public void setEposPort(Integer eposPort) {
        this.eposPort = eposPort;
    }

    public String getEposMerchant() {
        return this.eposMerchant;
    }

    public AgentRecipient eposMerchant(String eposMerchant) {
        this.eposMerchant = eposMerchant;
        return this;
    }

    public void setEposMerchant(String eposMerchant) {
        this.eposMerchant = eposMerchant;
    }

    public String getEposTerminal() {
        return this.eposTerminal;
    }

    public AgentRecipient eposTerminal(String eposTerminal) {
        this.eposTerminal = eposTerminal;
        return this;
    }

    public void setEposTerminal(String eposTerminal) {
        this.eposTerminal = eposTerminal;
    }

    public String getRequestGroup() {
        return this.requestGroup;
    }

    public AgentRecipient requestGroup(String requestGroup) {
        this.requestGroup = requestGroup;
        return this;
    }

    public void setRequestGroup(String requestGroup) {
        this.requestGroup = requestGroup;
    }

    public String getJsonParams() {
        return this.jsonParams;
    }

    public AgentRecipient jsonParams(String jsonParams) {
        this.jsonParams = jsonParams;
        return this;
    }

    public void setJsonParams(String jsonParams) {
        this.jsonParams = jsonParams;
    }

    public Recipient getRecipient() {
        return this.recipient;
    }

    public AgentRecipient recipient(Recipient recipient) {
        this.setRecipient(recipient);
        return this;
    }

    public void setRecipient(Recipient recipient) {
        this.recipient = recipient;
    }

    public Setting getSetting() {
        return this.setting;
    }

    public AgentRecipient setting(Setting setting) {
        this.setSetting(setting);
        return this;
    }

    public void setSetting(Setting setting) {
        this.setting = setting;
    }

    public Agent getAgent() {
        return this.agent;
    }

    public AgentRecipient agent(Agent agent) {
        this.setAgent(agent);
        return this;
    }

    public void setAgent(Agent agent) {
        this.agent = agent;
    }

    public RecipientGroup getRecipientGroup() {
        return this.recipientGroup;
    }

    public AgentRecipient recipientGroup(RecipientGroup recipientGroup) {
        this.setRecipientGroup(recipientGroup);
        return this;
    }

    public void setRecipientGroup(RecipientGroup recipientGroup) {
        this.recipientGroup = recipientGroup;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof AgentRecipient)) {
            return false;
        }
        return id != null && id.equals(((AgentRecipient) o).id);
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "AgentRecipient{" +
            "id=" + getId() +
            ", agentCommission=" + getAgentCommission() +
            ", agentFee=" + getAgentFee() +
            ", systemCommissionUp=" + getSystemCommissionUp() +
            ", systemCommissionDown=" + getSystemCommissionDown() +
            ", systemFee=" + getSystemFee() +
            ", active='" + getActive() + "'" +
            ", eposPort=" + getEposPort() +
            ", eposMerchant='" + getEposMerchant() + "'" +
            ", eposTerminal='" + getEposTerminal() + "'" +
//            ", requestGroup='" + getRequestGroup() + "'" +
            ", jsonParams='" + getJsonParams() + "'" +
            "}";
    }
}
