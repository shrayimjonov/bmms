package uz.paykap.domain.converter;

import org.apache.commons.lang3.StringUtils;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Converter
public class ListLongConverter implements AttributeConverter<List<Long>, String> {

    @Override
    public String convertToDatabaseColumn(List<Long> list) {
        if (list == null || list.isEmpty()) {
            return "";
        }
        return StringUtils.join(list, ',');
    }

    @Override
    public List<Long> convertToEntityAttribute(String stringData) {
        if (StringUtils.isBlank(stringData)) {
            return Collections.emptyList();
        }
        return Stream.of(stringData.split("\\s*,\\s*"))
            .map(Long::parseLong)
            .collect(Collectors.toList());
    }
}
