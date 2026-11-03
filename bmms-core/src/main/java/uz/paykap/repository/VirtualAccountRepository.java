package uz.paykap.repository;

import java.util.Optional;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import org.javers.spring.annotation.JaversSpringDataAuditable;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;
import uz.paykap.domain.VirtualAccount;
import uz.paykap.domain.enumeration.ModeType;

/**
 * Spring Data SQL repository for the VirtualAccount entity.
 */
@SuppressWarnings("unused")
@Repository
@JaversSpringDataAuditable
public interface VirtualAccountRepository extends JpaRepository<VirtualAccount, Long>, JpaSpecificationExecutor<VirtualAccount> {
    Optional<VirtualAccount> findTop1ByIdAndMode(Long id, ModeType mode);

    Optional<VirtualAccount> findTop1ByOrgNameAndInn(@NotNull @Size(max = 32) String orgName, @NotNull String inn);
}
