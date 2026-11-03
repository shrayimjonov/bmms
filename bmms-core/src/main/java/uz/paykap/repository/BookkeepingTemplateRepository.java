package uz.paykap.repository;

import java.util.List;
import java.util.Optional;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import org.javers.spring.annotation.JaversSpringDataAuditable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import uz.paykap.domain.BookkeepingTemplate;
import uz.paykap.domain.enumeration.BktPeriod;
import uz.paykap.domain.enumeration.BookkeepingPayType;
import uz.paykap.domain.enumeration.BookkeepingTransactionType;

/**
 * Spring Data SQL repository for the BookkeepingTemplate entity.
 */
@SuppressWarnings("unused")
@Repository
@JaversSpringDataAuditable
public interface BookkeepingTemplateRepository
    extends JpaRepository<BookkeepingTemplate, Long>, JpaSpecificationExecutor<BookkeepingTemplate> {
    Optional<List<BookkeepingTemplate>> findAllByRecipient_ShortNameAndBookkeepingTypeAndPeriodOrderByControlDesc(
        @NotNull @Size(max = 64) String recipient_shortName,
        @NotNull BookkeepingTransactionType bookkeepingType,
        @NotNull BktPeriod period
    );

    Optional<BookkeepingTemplate> findTop1ByBookkeepingPayTypeAndGroupIdsContains(
        BookkeepingPayType bookkeepingPayType,
        @Size(max = 256) String groupIds
    );
}
