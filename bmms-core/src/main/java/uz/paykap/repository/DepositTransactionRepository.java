package uz.paykap.repository;

import org.javers.spring.annotation.JaversSpringDataAuditable;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;
import uz.paykap.domain.DepositTransaction;

/**
 * Spring Data SQL repository for the DepositTransaction entity.
 */
@SuppressWarnings("unused")
@Repository
@JaversSpringDataAuditable
public interface DepositTransactionRepository
    extends JpaRepository<DepositTransaction, Long>, JpaSpecificationExecutor<DepositTransaction> {}
