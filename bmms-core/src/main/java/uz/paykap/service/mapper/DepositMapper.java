package uz.paykap.service.mapper;

import org.mapstruct.*;
import uz.paykap.domain.*;
import uz.paykap.service.dto.DepositDTO;
import uz.paykap.service.dto.SimpleDepositDTO;

/**
 * Mapper for the entity {@link Deposit} and its DTO {@link DepositDTO}.
 */
@Mapper(componentModel = "spring", uses = { GatewayMapper.class, AgentMapper.class, VirtualAccountMapper.class })
public interface DepositMapper extends EntityMapper<DepositDTO, Deposit> {
    @Mapping(target = "gateway", source = "gateway", qualifiedByName = "name")
    @Mapping(target = "agent", source = "agent", qualifiedByName = "shortName")
    @Mapping(target = "virtualAccount", source = "virtualAccount", qualifiedByName = "name")
    DepositDTO toDto(Deposit s);

    SimpleDepositDTO toSimple(DepositDTO depositDTO);
    SimpleDepositDTO toSimple(Deposit deposit);
}
