package uz.paykap.repository;

import org.javers.spring.annotation.JaversSpringDataAuditable;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;
import uz.paykap.domain.VirtualAccountDetails;

/**
 * Spring Data SQL repository for the VirtualAccountDetails entity.
 */
@SuppressWarnings("unused")
@Repository
@JaversSpringDataAuditable
public interface VirtualAccountDetailsRepository
    extends JpaRepository<VirtualAccountDetails, Long>, JpaSpecificationExecutor<VirtualAccountDetails> {}
