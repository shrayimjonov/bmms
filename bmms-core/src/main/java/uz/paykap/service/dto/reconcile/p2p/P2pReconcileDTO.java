package uz.paykap.service.dto.reconcile.p2p;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class P2pReconcileDTO {
    private Instant fromDate;
    private Instant toDate;
}
