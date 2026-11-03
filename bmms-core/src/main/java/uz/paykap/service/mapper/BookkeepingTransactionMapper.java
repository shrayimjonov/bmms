package uz.paykap.service.mapper;

import org.mapstruct.*;
import uz.paykap.domain.*;
import uz.paykap.service.dto.BookkeepingTransactionDTO;

/**
 * Mapper for the entity {@link BookkeepingTransaction} and its DTO {@link BookkeepingTransactionDTO}.
 */
@Mapper(componentModel = "spring", uses = { GatewayMapper.class, RecipientMapper.class })
public interface BookkeepingTransactionMapper extends EntityMapper<BookkeepingTransactionDTO, BookkeepingTransaction> {
    @Mapping(target = "gateway", source = "gateway", qualifiedByName = "name")
    @Mapping(target = "recipient", source = "recipient", qualifiedByName = "shortName")
    BookkeepingTransactionDTO toDto(BookkeepingTransaction s);
}
