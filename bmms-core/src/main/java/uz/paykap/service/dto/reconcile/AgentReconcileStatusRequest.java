package uz.paykap.service.dto.reconcile;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AgentReconcileStatusRequest {
    @NotNull
    private Long id;
}
