package uz.paykap.repository;

import java.util.List;
import java.util.Optional;
import javax.validation.constraints.NotNull;
import org.javers.spring.annotation.JaversSpringDataAuditable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import uz.paykap.domain.Currency;

/**
 * Spring Data SQL repository for the Currency entity.
 */
@SuppressWarnings("unused")
@Repository
//@JaversSpringDataAuditable
public interface CurrencyRepository extends JpaRepository<Currency, Long>, JpaSpecificationExecutor<Currency> {
    Optional<Currency> findTop1ByActiveTrueAndCode(@NotNull String code);

    Optional<Currency> findTop1ByActiveTrueAndCodeAndCcyGroup(@NotNull String code, @NotNull String ccyGroup);

    List<Currency> findByActiveTrue();

    List<Currency> findByActiveTrueAndCcyGroup(@NotNull String ccyGroup);
}
