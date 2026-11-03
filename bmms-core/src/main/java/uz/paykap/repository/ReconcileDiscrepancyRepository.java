package uz.paykap.repository;

import java.util.HashSet;
import java.util.Optional;
import javax.validation.constraints.NotNull;
import org.javers.spring.annotation.JaversSpringDataAuditable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import uz.paykap.domain.ReconcileDiscrepancy;
import uz.paykap.service.dto.reconcile.SimpleReconcileTransaction;

/**
 * Spring Data SQL repository for the ReconcileDiscrepancy entity.
 */
@SuppressWarnings("unused")
@Repository
@JaversSpringDataAuditable
public interface ReconcileDiscrepancyRepository
    extends JpaRepository<ReconcileDiscrepancy, Long>, JpaSpecificationExecutor<ReconcileDiscrepancy> {
    Optional<ReconcileDiscrepancy> findTop1BySystemTransactionId(@NotNull Long systemTransactionId);

    @Query(
        "select new uz.paykap.service.dto.reconcile.SimpleReconcileTransaction(reco.gatewayTransactionId, reco.gatewayAmount, reco.comment, reco.gatewayDate) " +
        "from ReconcileDiscrepancy reco " +
        "where reco.reconcileSummary.id = :summaryId and reco.gatewayTransactionId <> '-1L' and reco.systemStatus = false " +
        "order by reco.id asc"
    )
    HashSet<SimpleReconcileTransaction> getGatewayDiscrepancies(@Param("summaryId") Long summaryId);

    @Query(
        "select new uz.paykap.service.dto.reconcile.SimpleReconcileTransaction(reco.systemTransactionId, reco.systemAmount, reco.comment, reco.systemDate) " +
        "from ReconcileDiscrepancy reco " +
        "where reco.reconcileSummary.id = :summaryId and reco.systemTransactionId <> -1L and reco.gatewayStatus = false " +
        "order by reco.id asc"
    )
    HashSet<SimpleReconcileTransaction> getSystemDiscrepancies(@Param("summaryId") Long summaryId);

    Optional<ReconcileDiscrepancy> findTopByReconcileSummaryIdAndGatewayTransactionId(Long summaryId, String tranId);

    Optional<ReconcileDiscrepancy> findTopByReconcileSummaryIdAndSystemTransactionId(Long summaryId, Long tranId);
}
