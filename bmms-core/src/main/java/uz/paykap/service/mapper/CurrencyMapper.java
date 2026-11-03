package uz.paykap.service.mapper;

import org.mapstruct.*;
import uz.devops.cbu.service.dto.json.ICurrency;
import uz.paykap.domain.*;
import uz.paykap.service.dto.CurrencyDTO;

/**
 * Mapper for the entity {@link Currency} and its DTO {@link CurrencyDTO}.
 */
@Mapper(componentModel = "spring", uses = {})
public interface CurrencyMapper extends EntityMapper<CurrencyDTO, Currency> {
    Currency fromCbu(ICurrency currency);
}
