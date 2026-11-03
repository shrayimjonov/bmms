package uz.paykap.repository;

import org.javers.spring.annotation.JaversSpringDataAuditable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import uz.paykap.domain.Agent;
import uz.paykap.domain.enumeration.AgentState;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;
import java.util.Optional;

/**
 * Spring Data SQL repository for the Agent entity.
 */
@Repository
@JaversSpringDataAuditable
public interface AgentRepository extends JpaRepository<Agent, Long>, JpaSpecificationExecutor<Agent> {
    @Query(
            value = "select distinct agent from Agent agent left join fetch agent.titles",
            countQuery = "select count(distinct agent) from Agent agent"
    )
    Page<Agent> findAllWithEagerRelationships(Pageable pageable);

    @Query("select distinct agent from Agent agent left join fetch agent.titles")
    List<Agent> findAllWithEagerRelationships();

    @Query("select agent from Agent agent left join fetch agent.titles where agent.id =:id")
    Optional<Agent> findOneWithEagerRelationships(@Param("id") Long id);

    Optional<Agent> findTop1ByShortName(@NotNull @Size(max = 64) String shortName);

    Optional<Agent> findByEmail(@NotNull String email);

    Optional<Agent> findTop1ByInnAndShortName(@Size(max = 32) String inn, @NotNull @Size(max = 64) String shortName);

    @Query("select agent.inn from Agent agent where agent.id =:id")
    String getAgentInn(@Param("id") Long id);

    @Modifying
    @Query("update Agent a set a.state = :state, a.active = false where a.id = :id")
    void updateStatus(@Param("id") Long id, @Param("state") AgentState state);
}
