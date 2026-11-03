package uz.paykap.domain.converter;

import org.apache.commons.lang3.StringUtils;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Converter
public class SetStringConverter implements AttributeConverter<Set<String>, String> {

    @Override
    public String convertToDatabaseColumn(Set<String> list) {
        if (list == null || list.isEmpty()) {
            return "";
        }
        return String.join(",", list);
    }

    @Override
    public Set<String> convertToEntityAttribute(String stringData) {
        if (StringUtils.isBlank(stringData)) {
            return Collections.emptySet();
        }
        return Stream.of(stringData.split("\\s*,\\s*"))
            .collect(Collectors.toSet());
    }
}
