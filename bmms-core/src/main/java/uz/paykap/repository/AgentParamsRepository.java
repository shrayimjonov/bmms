package uz.paykap.repository;

import java.util.Optional;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import org.javers.spring.annotation.JaversSpringDataAuditable;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;
import uz.paykap.domain.AgentParams;

/**
 * Spring Data SQL repository for the AgentParams entity.
 */
@SuppressWarnings("unused")
@Repository
@JaversSpringDataAuditable
public interface AgentParamsRepository extends JpaRepository<AgentParams, Long>, JpaSpecificationExecutor<AgentParams> {
    Optional<AgentParams> findTop1ByAgent_ShortName(@NotNull @Size(max = 64) String agent_shortName);
}
