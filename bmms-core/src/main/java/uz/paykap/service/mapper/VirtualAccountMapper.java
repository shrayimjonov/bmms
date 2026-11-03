package uz.paykap.service.mapper;

import org.mapstruct.*;
import uz.paykap.domain.*;
import uz.paykap.service.dto.VirtualAccountDTO;

/**
 * Mapper for the entity {@link VirtualAccount} and its DTO {@link VirtualAccountDTO}.
 */
@Mapper(componentModel = "spring", uses = {})
public interface VirtualAccountMapper extends EntityMapper<VirtualAccountDTO, VirtualAccount> {
    @Named("name")
    @Mapping(target = "id", source = "id")
    @Mapping(target = "name", source = "name")
    VirtualAccountDTO toDtoName(VirtualAccount virtualAccount);

    @Mapping(target = "deposit", ignore = true)
    VirtualAccount toEntity(VirtualAccountDTO virtualAccountDTO);

    default VirtualAccount fromId(Long id) {
        if (id == null) {
            return null;
        }
        VirtualAccount virtualAccount = new VirtualAccount();
        virtualAccount.setId(id);
        return virtualAccount;
    }
}
