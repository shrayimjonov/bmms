package uz.paykap.service;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import uz.paykap.domain.Deposit;
import uz.paykap.repository.DepositRepository;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Optional;

@Profile({"dev", "prod", "local"})
@Component("internalDepositResolver")
public class InternalDepositResolverPostgresql implements InternalDepositResolver {

    private final DepositRepository depositRepository;

    public InternalDepositResolverPostgresql(DepositRepository depositRepository) {
        this.depositRepository = depositRepository;
    }

    @Override
    public Optional<Deposit> findTop1ByAgent_ShortName(@NotNull @Size(max = 64) String agent_shortName) {
        return depositRepository.findTop1ByAgent_ShortNameAndIdIsNotNull(agent_shortName);
    }

    @Override
    public Optional<Deposit> findTop1ByAgent_ShortNameAndBalanceCcy(@NotNull @Size(max = 64) String agent_shortName, @NotNull Integer balanceCcy) {
        return depositRepository.findTop1ByAgent_ShortNameAndBalanceCcyAndIdIsNotNull(agent_shortName, balanceCcy);
    }

    @Override
    public Optional<Deposit> findTop1ByGateway_Name(@NotNull @Size(max = 64) String gateway_name) {
        return depositRepository.findTop1ByGateway_NameAndIdIsNotNull(gateway_name);
    }

    @Override
    public Optional<Deposit> findTop1ByAgent_Id(Long agentId) {
        return depositRepository.findTop1ByAgent_IdAndIdIsNotNull(agentId);
    }

    @Override
    public Optional<Deposit> findTop1ByAgent_IdAndBalanceCcy(Long agent_id, @NotNull Integer balanceCcy) {
        return depositRepository.findTop1ByAgent_IdAndBalanceCcyAndIdIsNotNull(agent_id, balanceCcy);
    }
}
