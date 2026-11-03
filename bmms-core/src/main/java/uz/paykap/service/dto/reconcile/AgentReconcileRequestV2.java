package uz.paykap.service.dto.reconcile;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.time.Instant;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AgentReconcileRequestV2 {
    @NotNull
    private String group;
    @NotNull
    private String subgroup;
    @NotNull
    private Instant fromDate;
    @NotNull
    private Instant toDate;
    @NotNull
    private Set<ComplexReconcileTransaction> transactions;
}
