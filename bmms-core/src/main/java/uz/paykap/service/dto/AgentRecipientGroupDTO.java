package uz.paykap.service.dto;

import lombok.Data;

@Data
public class AgentRecipientGroupDTO {

    private AgentRecipientDTO agentRecipientDTO;

    private RecipientGroupDTO recipientGroupDTO;

    @Override
    public String toString() {
        return "AgentRecipientGroupDTO{" +
            "agentRecipientDTO=" + agentRecipientDTO +
            ", recipientGroupDTO=" + recipientGroupDTO +
            '}';
    }
}
