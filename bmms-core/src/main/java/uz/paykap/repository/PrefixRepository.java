package uz.paykap.repository;

import java.util.Optional;
import javax.validation.constraints.Size;
import org.javers.spring.annotation.JaversSpringDataAuditable;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;
import uz.paykap.domain.Prefix;

/**
 * Spring Data SQL repository for the Prefix entity.
 */
@SuppressWarnings("unused")
@Repository
@JaversSpringDataAuditable
public interface PrefixRepository extends JpaRepository<Prefix, Long>, JpaSpecificationExecutor<Prefix> {
    Optional<Prefix> findTop1ByValueAndFieldId(@Size(max = 16) String value, Long fieldId);
}
