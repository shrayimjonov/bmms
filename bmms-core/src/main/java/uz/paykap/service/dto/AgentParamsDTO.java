package uz.paykap.service.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.io.Serializable;
import java.util.Objects;
import javax.validation.constraints.*;

/**
 * A DTO for the {@link uz.paykap.domain.AgentParams} entity.
 */
@ApiModel(description = "Настройки для обратной связи с агентом")
public class AgentParamsDTO implements Serializable {

    private Long id;

    /**
     * Agent url to callback
     */
    @NotNull
    @Size(max = 512)
    @ApiModelProperty(value = "Agent url to callback", required = true)
    private String callBackUrl;

    /**
     * Username
     */
    @Size(max = 128)
    @ApiModelProperty(value = "Username")
    private String username;

    /**
     * password
     */
    @Size(max = 128)
    @ApiModelProperty(value = "password")
    private String password;

    @Size(min = 7, max = 20)
    private String bankClientId;

    @Size(max = 64)
    private String bankAccountPrefix;

    private AgentDTO agent;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCallBackUrl() {
        return callBackUrl;
    }

    public void setCallBackUrl(String callBackUrl) {
        this.callBackUrl = callBackUrl;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getBankClientId() {
        return bankClientId;
    }

    public void setBankClientId(String bankClientId) {
        this.bankClientId = bankClientId;
    }

    public String getBankAccountPrefix() {
        return bankAccountPrefix;
    }

    public void setBankAccountPrefix(String bankAccountPrefix) {
        this.bankAccountPrefix = bankAccountPrefix;
    }

    public AgentDTO getAgent() {
        return agent;
    }

    public void setAgent(AgentDTO agent) {
        this.agent = agent;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof AgentParamsDTO)) {
            return false;
        }

        AgentParamsDTO agentParamsDTO = (AgentParamsDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, agentParamsDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "AgentParamsDTO{" +
            "id=" + getId() +
            ", callBackUrl='" + getCallBackUrl() + "'" +
            ", username='" + getUsername() + "'" +
            ", password=''" +
            ", bankClientId='" + getBankClientId() + "'" +
            ", bankAccountPrefix='" + getBankAccountPrefix() + "'" +
            ", agent=" + getAgent() +
            "}";
    }
}
