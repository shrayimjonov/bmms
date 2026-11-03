package uz.paykap.service.mapper;

import uz.paykap.domain.*;
import uz.paykap.service.dto.PcBinDTO;

import org.mapstruct.*;

/**
 * Mapper for the entity PcBin and its DTO PcBinDTO.
 */
@Mapper(componentModel = "spring", uses = {})
public interface PcBinMapper extends EntityMapper<PcBinDTO, PcBin> {



    default PcBin fromId(Long id) {
        if (id == null) {
            return null;
        }
        PcBin pcBin = new PcBin();
        pcBin.setId(id);
        return pcBin;
    }
}
