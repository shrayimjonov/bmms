package uz.paykap.service.mapper;

import org.mapstruct.*;
import uz.paykap.domain.*;
import uz.paykap.service.dto.ReconcileSummaryDTO;

/**
 * Mapper for the entity {@link ReconcileSummary} and its DTO {@link ReconcileSummaryDTO}.
 */
@Mapper(componentModel = "spring", uses = {})
public interface ReconcileSummaryMapper extends EntityMapper<ReconcileSummaryDTO, ReconcileSummary> {
    @Named("id")
    @Mapping(target = "id", source = "id")
    ReconcileSummaryDTO toDtoId(ReconcileSummary reconcileSummary);
}
