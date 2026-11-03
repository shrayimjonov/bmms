package uz.paykap.service.mapper;

import org.mapstruct.Mapper;
import uz.devops.bmcore.util.FileDTO;
import uz.paykap.domain.File;

import java.util.UUID;

/**
 * Mapper for the entity File and its DTO FileDTO.
 */
@Mapper(componentModel = "spring")
public interface FileMapper extends EntityMapper<FileDTO, File> {

    default File fromId(UUID id) {

        if (id == null) {
            return null;
        }

        File file = new File();
        file.setId(id);
        return file;
    }
}
