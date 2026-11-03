package uz.paykap.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.io.Serializable;
import javax.persistence.*;
import javax.validation.constraints.*;

/**
 * Настройки для обратной связи с агентом
 */
@Entity
@Table(name = "agent_params")
public class AgentParams extends AbstractAuditingEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    private Long id;

    /**
     * Agent url to callback
     */
    @NotNull
    @Size(max = 512)
    @Column(name = "call_back_url", length = 512, nullable = false)
    private String callBackUrl;

    /**
     * Username
     */
    @Size(max = 128)
    @Column(name = "username", length = 128)
    private String username;

    /**
     * password
     */
    @Size(max = 128)
    @Column(name = "password", length = 128)
    private String password;

    @Size(min = 7, max = 20)
    @Column(name = "bank_client_id", length = 20, unique = true)
    private String bankClientId;

    @Size(max = 64)
    @Column(name = "bank_account_prefix", length = 64)
    private String bankAccountPrefix;

    @ManyToOne
    @JsonIgnoreProperties(value = { "titles", "deposit" }, allowSetters = true)
    private Agent agent;

    // jhipster-needle-entity-add-field - JHipster will add fields here
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public AgentParams id(Long id) {
        this.id = id;
        return this;
    }

    public String getCallBackUrl() {
        return this.callBackUrl;
    }

    public AgentParams callBackUrl(String callBackUrl) {
        this.callBackUrl = callBackUrl;
        return this;
    }

    public void setCallBackUrl(String callBackUrl) {
        this.callBackUrl = callBackUrl;
    }

    public String getUsername() {
        return this.username;
    }

    public AgentParams username(String username) {
        this.username = username;
        return this;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return this.password;
    }

    public AgentParams password(String password) {
        this.password = password;
        return this;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getBankClientId() {
        return this.bankClientId;
    }

    public AgentParams bankClientId(String bankClientId) {
        this.bankClientId = bankClientId;
        return this;
    }

    public void setBankClientId(String bankClientId) {
        this.bankClientId = bankClientId;
    }

    public String getBankAccountPrefix() {
        return this.bankAccountPrefix;
    }

    public AgentParams bankAccountPrefix(String bankAccountPrefix) {
        this.bankAccountPrefix = bankAccountPrefix;
        return this;
    }

    public void setBankAccountPrefix(String bankAccountPrefix) {
        this.bankAccountPrefix = bankAccountPrefix;
    }

    public Agent getAgent() {
        return this.agent;
    }

    public AgentParams agent(Agent agent) {
        this.setAgent(agent);
        return this;
    }

    public void setAgent(Agent agent) {
        this.agent = agent;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof AgentParams)) {
            return false;
        }
        return id != null && id.equals(((AgentParams) o).id);
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "AgentParams{" +
            "id=" + getId() +
            ", callBackUrl='" + getCallBackUrl() + "'" +
            ", username='" + getUsername() + "'" +
            ", password=''" +
            ", bankClientId='" + getBankClientId() + "'" +
            ", bankAccountPrefix='" + getBankAccountPrefix() + "'" +
            "}";
    }
}
