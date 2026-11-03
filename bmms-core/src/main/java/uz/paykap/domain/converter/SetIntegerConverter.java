package uz.paykap.domain.converter;

import org.apache.commons.lang3.StringUtils;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Converter
public class SetIntegerConverter implements AttributeConverter<Set<Integer>, String> {

    @Override
    public String convertToDatabaseColumn(Set<Integer> list) {
        if (list == null || list.isEmpty()) {
            return "";
        }
        return StringUtils.join(list, ',');
    }

    @Override
    public Set<Integer> convertToEntityAttribute(String stringData) {
        if (StringUtils.isBlank(stringData)) {
            return Collections.emptySet();
        }
        return Stream.of(stringData.split("\\s*,\\s*"))
            .map(Integer::parseInt)
            .collect(Collectors.toSet());
    }
}
