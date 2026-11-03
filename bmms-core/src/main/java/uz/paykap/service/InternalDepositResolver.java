package uz.paykap.service;

import uz.paykap.domain.Deposit;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Optional;

public interface InternalDepositResolver {

    Optional<Deposit> findTop1ByAgent_ShortName(@NotNull @Size(max = 64) String agent_shortName);

    Optional<Deposit> findTop1ByAgent_ShortNameAndBalanceCcy(@NotNull @Size(max = 64) String agent_shortName, @NotNull Integer balanceCcy);

    Optional<Deposit> findTop1ByGateway_Name(@NotNull @Size(max = 64) String gateway_name);

    Optional<Deposit> findTop1ByAgent_Id(Long agentId);

    Optional<Deposit> findTop1ByAgent_IdAndBalanceCcy(Long agent_id, @NotNull Integer balanceCcy);

}
