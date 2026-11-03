package uz.paykap.service.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import uz.devops.bmcore.dto.response.SimpleBillResponse;
import uz.paykap.domain.Bill;
import uz.paykap.service.dto.BillDTO;

/**
 * Mapper for the entity {@link Bill} and its DTO {@link BillDTO}.
 */
@Mapper(componentModel = "spring")
public interface BillMapper extends EntityMapper<BillDTO, Bill> {
    SimpleBillResponse toSimple(BillDTO billDTO);

    @Mapping(source = "responseJson", target = "data")
    SimpleBillResponse toSimple(Bill bill);

    @Mapping(target = "id", ignore = true)
    Bill clone(Bill bill);

    default Bill fromId(Long id) {
        if (id == null) {
            return null;
        }
        Bill bill = new Bill();
        bill.setId(id);
        return bill;
    }
}
