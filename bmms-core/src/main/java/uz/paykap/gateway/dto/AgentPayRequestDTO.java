package uz.paykap.gateway.dto;


import lombok.*;
import uz.devops.bmcore.dto.request.PayRequest;


@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
public class AgentPayRequestDTO extends PayRequest {
    private String agentName;
}
