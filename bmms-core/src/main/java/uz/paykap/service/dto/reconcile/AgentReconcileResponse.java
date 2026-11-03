package uz.paykap.service.dto.reconcile;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AgentReconcileResponse {
    private Long id;
    private Instant fromDate;
    private Instant toDate;
    private Boolean success;
    private Integer statusCode;
    private String statusMessage;
}
