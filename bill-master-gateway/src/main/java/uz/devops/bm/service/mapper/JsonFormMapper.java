package uz.devops.bm.service.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import uz.devops.bm.domain.JsonForm;
import uz.devops.bm.service.dto.JsonFormDTO;

/**
 * Mapper for the entity {@link JsonForm} and its DTO {@link JsonFormDTO}.
 */
@Mapper(componentModel = "spring", uses = { UserMapper.class })
public interface JsonFormMapper extends EntityMapper<JsonFormDTO, JsonForm> {
  @Mapping(target = "users", source = "users", qualifiedByName = "loginSet")
  JsonFormDTO toDto(JsonForm s);

  @Mapping(target = "removeUser", ignore = true)
  JsonForm toEntity(JsonFormDTO jsonFormDTO);
}
