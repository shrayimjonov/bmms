package uz.paykap.service.mapper;

import org.mapstruct.*;
import uz.paykap.domain.*;
import uz.paykap.service.dto.ForeignPayDTO;

/**
 * Mapper for the entity {@link ForeignPay} and its DTO {@link ForeignPayDTO}.
 */
@Mapper(componentModel = "spring", uses = {})
public interface ForeignPayMapper extends EntityMapper<ForeignPayDTO, ForeignPay> {
    default ForeignPay fromId(Long id) {
        if (id == null) {
            return null;
        }
        ForeignPay foreignPay = new ForeignPay();
        foreignPay.setId(id);
        return foreignPay;
    }
}
