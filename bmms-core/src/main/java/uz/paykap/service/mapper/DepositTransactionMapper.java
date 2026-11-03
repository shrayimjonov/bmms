package uz.paykap.service.mapper;

import org.mapstruct.*;
import uz.paykap.domain.*;
import uz.paykap.service.dto.DepositTransactionDTO;

/**
 * Mapper for the entity {@link DepositTransaction} and its DTO {@link DepositTransactionDTO}.
 */
@Mapper(componentModel = "spring", uses = {})
public interface DepositTransactionMapper extends EntityMapper<DepositTransactionDTO, DepositTransaction> {}
