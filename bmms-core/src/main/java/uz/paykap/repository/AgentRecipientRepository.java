package uz.paykap.repository;

import java.util.List;
import java.util.Optional;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import org.javers.spring.annotation.JaversSpringDataAuditable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import uz.paykap.domain.Agent;
import uz.paykap.domain.AgentRecipient;
import uz.paykap.domain.Recipient;

/**
 * Spring Data SQL repository for the AgentRecipient entity.
 */
@SuppressWarnings("unused")
@Repository
@JaversSpringDataAuditable
public interface AgentRecipientRepository extends JpaRepository<AgentRecipient, Long>, JpaSpecificationExecutor<AgentRecipient> {
    Optional<AgentRecipient> findTopByActiveIsTrueAndAgent_ShortNameAndRecipient_Id(
        @NotNull @Size(max = 64) String agentShortName,
        Long recipientId
    );

    Optional<AgentRecipient> findTop1ByAgent_IdAndRecipient_Id(Long agentId, Long recipientId);

    Optional<AgentRecipient> findTop1ByAgent_ShortNameAndRecipient_Id(String agentShortName, Long recipientId);

    Optional<AgentRecipient> findTop1ByAgent_IdAndRecipient_ShortName(Long agent_id, String recipientShortName);

    Optional<List<AgentRecipient>> findAllByAgent(Agent agent);

    Optional<List<AgentRecipient>> findTop50ByAgent(Agent agent);

    Optional<List<AgentRecipient>> findAllByAgent_ShortName(@NotNull @Size(max = 64) String agent_shortName);

    List<AgentRecipient> findAllByRecipient_IdAndActive(Long id, Boolean active);

    @Query(
        "Select DISTINCT recipient from AgentRecipient agentRecipient where agentRecipient.agent in (select agent from Agent agent where agent.shortName =:agentName AND agent.active = true)"
    )
    List<Recipient> findAllRecipientsByAgent(@Param("agentName") String agentName);
}
