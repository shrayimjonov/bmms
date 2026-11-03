package uz.paykap.service.mapper;

import org.mapstruct.*;
import uz.paykap.domain.*;
import uz.paykap.service.dto.VirtualAccountDetailsDTO;

/**
 * Mapper for the entity {@link VirtualAccountDetails} and its DTO {@link VirtualAccountDetailsDTO}.
 */
@Mapper(componentModel = "spring", uses = { VirtualAccountMapper.class })
public interface VirtualAccountDetailsMapper extends EntityMapper<VirtualAccountDetailsDTO, VirtualAccountDetails> {
    @Mapping(target = "virtualAccount", source = "virtualAccount", qualifiedByName = "name")
    VirtualAccountDetailsDTO toDto(VirtualAccountDetails s);

    //todo bu eskisi
    //    @Mapping(source = "virtualAccount.id", target = "virtualAccountId")
    //    @Mapping(source = "virtualAccount.name", target = "virtualAccountName")
    //    VirtualAccountDetailsDTO toDto(VirtualAccountDetails virtualAccountDetails);

    //    @Mapping(source = "virtualAccountId", target = "virtualAccount")
    //    VirtualAccountDetails toEntity(VirtualAccountDetailsDTO virtualAccountDetailsDTO);

    default VirtualAccountDetails fromId(Long id) {
        if (id == null) {
            return null;
        }
        VirtualAccountDetails virtualAccountDetails = new VirtualAccountDetails();
        virtualAccountDetails.setId(id);
        return virtualAccountDetails;
    }
}
