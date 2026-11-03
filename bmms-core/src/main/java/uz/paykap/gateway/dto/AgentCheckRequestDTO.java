package uz.paykap.gateway.dto;


import lombok.*;
import uz.devops.bmcore.dto.request.CheckRequest;


@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
public class AgentCheckRequestDTO extends CheckRequest {
    private String agentName;
}
