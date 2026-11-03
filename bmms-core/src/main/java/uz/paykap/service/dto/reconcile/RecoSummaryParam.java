package uz.paykap.service.dto.reconcile;

import lombok.Value;
import uz.paykap.domain.enumeration.ReconcilePeriod;
import uz.paykap.domain.enumeration.ReconcileSummaryStatus;

import java.time.Instant;

@Value(staticConstructor = "of")
public class RecoSummaryParam {
    Long reconcileId;
    Boolean isSuccess;
    Instant to;
    Long gatewayTotalAmount;
    Long gatewayTotalCount;
    Long systemTotalAmount;
    Long systemTotalCount;
    Integer amountRatio;
    ReconcileSummaryStatus reconcileSummaryStatus;
    Long gatewayId;
    ReconcilePeriod reconcilePeriod;
    String gatewayName;
}
