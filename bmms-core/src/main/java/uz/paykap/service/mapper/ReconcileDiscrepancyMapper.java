package uz.paykap.service.mapper;

import org.mapstruct.*;
import uz.paykap.domain.*;
import uz.paykap.service.dto.ReconcileDiscrepancyDTO;

/**
 * Mapper for the entity {@link ReconcileDiscrepancy} and its DTO {@link ReconcileDiscrepancyDTO}.
 */
@Mapper(componentModel = "spring", uses = { ReconcileSummaryMapper.class })
public interface ReconcileDiscrepancyMapper extends EntityMapper<ReconcileDiscrepancyDTO, ReconcileDiscrepancy> {
    @Mapping(target = "reconcileSummary", source = "reconcileSummary", qualifiedByName = "id")
    ReconcileDiscrepancyDTO toDto(ReconcileDiscrepancy s);
}
