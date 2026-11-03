package uz.paykap.repository;

import org.javers.spring.annotation.JaversSpringDataAuditable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import uz.paykap.domain.ReconcileSummary;
import uz.paykap.domain.enumeration.ReconcileSummaryStatus;
import uz.paykap.domain.enumeration.ReconcileSummaryType;

import javax.validation.constraints.Size;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Spring Data SQL repository for the ReconcileSummary entity.
 */
@SuppressWarnings("unused")
@Repository
//@JaversSpringDataAuditable
public interface ReconcileSummaryRepository extends JpaRepository<ReconcileSummary, Long>, JpaSpecificationExecutor<ReconcileSummary> {
    Optional<List<ReconcileSummary>> findAllByDateBetween(LocalDate date, LocalDate date2);

    Optional<ReconcileSummary> findTop1ByGatewayNameAndDate(@Size(max = 64) String gatewayName, LocalDate date);

    Optional<ReconcileSummary> findTop1ByGatewayNameAndDateAndType(@Size(max = 64) String gatewayName, LocalDate date, ReconcileSummaryType type);

    Optional<ReconcileSummary> findTop1ByGatewayName(@Size(max = 64) String gatewayName);

    Optional<ReconcileSummary> findTop1ByGatewayNameAndDateAndStatus(
        @Size(max = 64) String gatewayName,
        LocalDate date,
        ReconcileSummaryStatus status
    );

    Optional<ReconcileSummary> findTop1ByGatewayNameOrderByCreatedDateDesc(@Size(max = 64) String gatewayName);

    Optional<ReconcileSummary> findTop1ByIdAndStatus(Long id, ReconcileSummaryStatus status);

    @Query(
            "select rec from ReconcileSummary rs left join Recipient rec on rs.gatewayId = rec.gateway.id where rec.id = :recipientId and rs.status = 'PROCESSING'"
    )
    Optional<ReconcileSummary> findByRecipientId(@Param("recipientId") Long recipientId);

    Optional<ReconcileSummary> findTopByGatewayNameAndStatusOrderByIdDesc(@Size(max = 64) String gatewayName, ReconcileSummaryStatus status);
}
