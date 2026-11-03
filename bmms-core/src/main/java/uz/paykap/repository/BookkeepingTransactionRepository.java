package uz.paykap.repository;

import java.util.List;
import java.util.Optional;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import org.javers.spring.annotation.JaversSpringDataAuditable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;
import uz.paykap.domain.BookkeepingTransaction;
import uz.paykap.domain.Gateway;
import uz.paykap.domain.enumeration.BookkeepingPayType;
import uz.paykap.domain.enumeration.BookkeepingTransactionType;

/**
 * Spring Data SQL repository for the BookkeepingTransaction entity.
 */
@SuppressWarnings("unused")
@Repository
@JaversSpringDataAuditable
public interface BookkeepingTransactionRepository
    extends JpaRepository<BookkeepingTransaction, Long>, JpaSpecificationExecutor<BookkeepingTransaction> {
    Optional<BookkeepingTransaction> findTop1ByIdAndSuccess(Long id, Boolean success);

    Optional<List<BookkeepingTransaction>> findAllByTemplateIdAndSuccess(Long templateId, Boolean success);

    Optional<List<BookkeepingTransaction>> findAllByRecipient_IdAndSuccess(Long recipient_id, Boolean success);

    Optional<List<BookkeepingTransaction>> findAllByGatewayAndSuccess(Gateway gateway, Boolean success);

    Optional<List<BookkeepingTransaction>> findAllByBookkeepingType(@NotNull BookkeepingTransactionType bookkeepingType);

    Page<BookkeepingTransaction> findAllByBookkeepingType(@NotNull BookkeepingTransactionType bookkeepingType, Pageable pageable);

    Optional<BookkeepingTransaction> findTop1ByTemplateIdAndAmount(Long templateId, @NotNull @Size(max = 64) String amount);

    Optional<BookkeepingTransaction> findTopByBookkeepingPayTypeAndGroupIdsContains(
        BookkeepingPayType bookkeepingPayType,
        @Size(max = 256) String groupIds
    );
}
