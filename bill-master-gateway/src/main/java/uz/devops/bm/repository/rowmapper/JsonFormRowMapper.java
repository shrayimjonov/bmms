package uz.devops.bm.repository.rowmapper;

import io.r2dbc.spi.Row;
import java.time.Instant;
import java.util.function.BiFunction;
import org.springframework.stereotype.Service;
import uz.devops.bm.domain.JsonForm;
import uz.devops.bm.domain.enumeration.JsonFormType;
import uz.devops.bm.service.ColumnConverter;

/**
 * Converter between {@link Row} to {@link JsonForm}, with proper type conversions.
 */
@Service
public class JsonFormRowMapper implements BiFunction<Row, String, JsonForm> {

    private final ColumnConverter converter;

    public JsonFormRowMapper(ColumnConverter converter) {
        this.converter = converter;
    }

    /**
     * Take a {@link Row} and a column prefix, and extract all the fields.
     * @return the {@link JsonForm} stored in the database.
     */
    @Override
    public JsonForm apply(Row row, String prefix) {
        JsonForm entity = new JsonForm();
        entity.setId(converter.fromRow(row, prefix + "_id", Long.class));
        entity.setName(converter.fromRow(row, prefix + "_name", String.class));
        entity.setType(converter.fromRow(row, prefix + "_type", JsonFormType.class));
        entity.setData(converter.fromRow(row, prefix + "_data", String.class));
        entity.setUsername(converter.fromRow(row, prefix + "_username", String.class));
        entity.setActive(converter.fromRow(row, prefix + "_active", Boolean.class));
        entity.setDescription(converter.fromRow(row, prefix + "_description", String.class));
        entity.setCreatedBy(converter.fromRow(row, prefix + "_created_by", String.class));
        entity.setCreatedDate(converter.fromRow(row, prefix + "_created_date", Instant.class));
        entity.setLastModifiedBy(converter.fromRow(row, prefix + "_last_modified_by", String.class));
        entity.setLastModifiedDate(converter.fromRow(row, prefix + "_last_modified_date", Instant.class));
        return entity;
    }
}
