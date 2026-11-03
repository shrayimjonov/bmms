package uz.paykap.repository;

import org.javers.spring.annotation.JaversSpringDataAuditable;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;
import uz.paykap.domain.P2pCondition;

/**
 * Spring Data SQL repository for the P2pCondition entity.
 */
@SuppressWarnings("unused")
@Repository
@JaversSpringDataAuditable
public interface P2pConditionRepository extends JpaRepository<P2pCondition, Long>, JpaSpecificationExecutor<P2pCondition> {}
