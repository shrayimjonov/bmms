package uz.paykap.repository;

import java.time.Instant;
import java.util.Optional;
import javax.validation.constraints.NotNull;
import org.javers.spring.annotation.JaversSpringDataAuditable;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;
import uz.paykap.domain.ForeignPay;
import uz.paykap.domain.enumeration.ForeignPayStatus;

/**
 * Spring Data SQL repository for the ForeignPay entity.
 */
@SuppressWarnings("unused")
@Repository
@JaversSpringDataAuditable
public interface ForeignPayRepository extends JpaRepository<ForeignPay, Long>, JpaSpecificationExecutor<ForeignPay> {
    Optional<ForeignPay> findTop1ByCreatedByAndCardTokenAndBillId(String login, String token, Long billId);
    Optional<ForeignPay> findTop1ByCreatedByAndCardTokenAndBillIdOrderByVerifiedDateDesc(String login, String token, Long billId);
    Optional<ForeignPay> findTop1ByCreatedByAndBillIdOrderByVerifiedDateDesc(String login, Long billId);
    Optional<ForeignPay> findTop1ByCreatedByAndCardTokenAndBillIdAndVerifiedDateAfterOrderByVerifiedDateDesc(
        String login,
        String token,
        Long billId,
        Instant minus120seconds
    );

    Optional<ForeignPay> findTop1ByCreatedByAndCardTokenAndStatusOrderByVerifiedDateDesc(
        String createdBy,
        @NotNull String cardToken,
        @NotNull ForeignPayStatus status
    );
}
