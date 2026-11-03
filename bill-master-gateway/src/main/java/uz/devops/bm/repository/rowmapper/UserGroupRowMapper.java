package uz.devops.bm.repository.rowmapper;

import io.r2dbc.spi.Row;
import java.util.function.BiFunction;
import org.springframework.stereotype.Service;
import uz.devops.bm.domain.Authority;
import uz.devops.bm.domain.UserGroup;
import uz.devops.bm.domain.enumeration.UserGroupType;
import uz.devops.bm.service.ColumnConverter;

/**
 * Converter between {@link Row} to {@link UserGroup}, with proper type conversions.
 */
@Service
public class UserGroupRowMapper implements BiFunction<Row, String, UserGroup> {

    private final ColumnConverter converter;

    public UserGroupRowMapper(ColumnConverter converter) {
        this.converter = converter;
    }

    /**
     * Take a {@link Row} and a column prefix, and extract all the fields.
     * @return the {@link UserGroup} stored in the database.
     */
    @Override
    public UserGroup apply(Row row, String prefix) {
        UserGroup entity = new UserGroup();
        entity.setId(converter.fromRow(row, prefix + "_id", Long.class));
        entity.setName(converter.fromRow(row, prefix + "_name", String.class));
        entity.setActive(converter.fromRow(row, prefix + "_active", Boolean.class));
        entity.setDetails(converter.fromRow(row, prefix + "_details", String.class));
        entity.setType(converter.fromRow(row, prefix + "_type", UserGroupType.class));
        //                entity.setAuthorities(converter.fromRow(row,prefix + "_authority", Authority.class));
        return entity;
    }
}
