package uz.paykap.repository;

import org.javers.spring.annotation.JaversSpringDataAuditable;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;
import uz.paykap.domain.P2pCommission;

/**
 * Spring Data SQL repository for the P2pCommission entity.
 */
@SuppressWarnings("unused")
@Repository
@JaversSpringDataAuditable
public interface P2pCommissionRepository extends JpaRepository<P2pCommission, Long>, JpaSpecificationExecutor<P2pCommission> {}
