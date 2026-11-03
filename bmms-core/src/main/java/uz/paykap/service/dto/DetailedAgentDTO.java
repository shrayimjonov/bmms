package uz.paykap.service.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Data
public class DetailedAgentDTO implements Serializable {

    @NotNull
    private String password;
    @NotNull
    private String eMail;
    @NotNull
    private AgentDTO agentDTO;

    private AgentParamsDTO agentParamsDTO;
}
