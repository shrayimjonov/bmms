package uz.paykap.repository;

import org.javers.spring.annotation.JaversSpringDataAuditable;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;
import uz.paykap.domain.CheckVerifierParam;

/**
 * Spring Data SQL repository for the CheckVerifierParam entity.
 */
@SuppressWarnings("unused")
@Repository
@JaversSpringDataAuditable
public interface CheckVerifierParamRepository extends JpaRepository<CheckVerifierParam, Long> {}
