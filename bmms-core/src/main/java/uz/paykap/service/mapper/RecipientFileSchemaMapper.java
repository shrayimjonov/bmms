package uz.paykap.service.mapper;

import org.mapstruct.*;
import uz.paykap.domain.*;
import uz.paykap.service.dto.RecipientFileSchemaDTO;

/**
 * Mapper for the entity {@link RecipientFileSchema} and its DTO {@link RecipientFileSchemaDTO}.
 */
@Mapper(componentModel = "spring", uses = {})
public interface RecipientFileSchemaMapper extends EntityMapper<RecipientFileSchemaDTO, RecipientFileSchema> {}
