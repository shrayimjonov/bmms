package uz.paykap.repository;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import uz.paykap.domain.Deposit;
import uz.paykap.domain.enumeration.DepositState;

import javax.persistence.LockModeType;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;
import java.util.Optional;

/**
 * Spring Data SQL repository for the Deposit entity.
 */
@SuppressWarnings("unused")
@Repository
//@JaversSpringDataAuditable
public interface DepositRepository extends JpaRepository<Deposit, Long>, JpaSpecificationExecutor<Deposit> {
    Optional<Deposit> findTop1ByAgent_ShortName(@NotNull @Size(max = 64) String agent_shortName);
    Optional<Deposit> findTop1ByAgent_Inn(@Size(max = 32) String agent_inn);

    Optional<Deposit> findTop1ByAgent_ShortNameAndBalanceCcy(@NotNull @Size(max = 64) String agent_shortName, @NotNull Integer balanceCcy);

    Optional<Deposit> findTop1ByVirtualAccount_OrgNameAndBalanceCcy(
        @NotNull @Size(max = 32) String virtualAccount_orgName,
        @NotNull Integer balanceCcy
    );

    Optional<Deposit> findTop1ByGateway_NameAndBalanceCcy(@NotNull @Size(max = 64) String gatewayName, @NotNull Integer balanceCcy);

    Optional<Deposit> findTop1ByGateway_Name(@NotNull @Size(max = 64) String gateway_name);

    Optional<Deposit> findTop1ByAgent_Id(Long agentId);

    Optional<Deposit> findTop1ByAgent_IdAndBalanceCcy(Long agent_id, @NotNull Integer balanceCcy);

    @Query(
        "select distinct d from Deposit d where d.balance <= d.limitInform AND d.blocked = false AND d.unlimit = false AND d.agent is NULL AND d.gateway is not NULL"
    )
    List<Deposit> findAllLimitInformReached();

    @Query(
        "select distinct d from Deposit d where d.balance <= d.limitDisable AND d.blocked = false AND d.unlimit = false AND d.agent is NULL AND d.gateway is not NULL"
    )
    List<Deposit> findAllLimitDisableReached();

    Optional<Deposit> findTop1ByVirtualAccount_Id(Long virtualAccount_id);

    Optional<Deposit> findTop1ByVirtualAccount_OrgName(@NotNull @Size(max = 32) String virtualAccount_orgName);

    // DO NOT USE, DO NOT REMOVE

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    // https://stackoverflow.com/a/23236216/1110253
    Optional<Deposit> findTop1ByAgent_ShortNameAndIdIsNotNull(@NotNull @Size(max = 64) String agent_shortName);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    // https://stackoverflow.com/a/23236216/1110253
    Optional<Deposit> findTop1ByAgent_ShortNameAndBalanceCcyAndIdIsNotNull(
        @NotNull @Size(max = 64) String agent_shortName,
        @NotNull Integer balanceCcy
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    // https://stackoverflow.com/a/23236216/1110253
    Optional<Deposit> findTop1ByGateway_NameAndIdIsNotNull(@NotNull @Size(max = 64) String gateway_name);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    // https://stackoverflow.com/a/23236216/1110253
    Optional<Deposit> findTop1ByAgent_IdAndIdIsNotNull(Long agentId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    // https://stackoverflow.com/a/23236216/1110253
    Optional<Deposit> findTop1ByAgent_IdAndBalanceCcyAndIdIsNotNull(Long agent_id, @NotNull Integer balanceCcy);

    @Modifying
    @Query("update Deposit d set d.state = :state, d.blocked = true where d.id = :id")
    void updateStatus(@Param("id") Long id, @Param("state") DepositState state);

    @Modifying
    @Query("update Deposit d set d.state = :state, d.blocked = true where d.agent.id = :id")
    void updateStatusByAgentId(@Param("id") Long id, @Param("state") DepositState state);
}
