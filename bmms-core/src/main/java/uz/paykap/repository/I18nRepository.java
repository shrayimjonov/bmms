package uz.paykap.repository;

import org.javers.spring.annotation.JaversSpringDataAuditable;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;
import uz.paykap.domain.I18n;

/**
 * Spring Data SQL repository for the I18n entity.
 */
@SuppressWarnings("unused")
@Repository
public interface I18nRepository extends JpaRepository<I18n, Long>, JpaSpecificationExecutor<I18n> {}
