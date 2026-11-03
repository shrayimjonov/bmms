package uz.paykap.repository;

import java.util.Optional;
import org.javers.spring.annotation.JaversSpringDataAuditable;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;
import uz.paykap.domain.RecipientFileSchema;

/**
 * Spring Data SQL repository for the RecipientFileSchema entity.
 */
@SuppressWarnings("unused")
@Repository
//@JaversSpringDataAuditable
public interface RecipientFileSchemaRepository
    extends JpaRepository<RecipientFileSchema, Long>, JpaSpecificationExecutor<RecipientFileSchema> {
    Optional<RecipientFileSchema> findTop1ById(Long id);
}
