package uz.paykap.domain.converter;

import org.apache.commons.lang3.StringUtils;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Converter
public class ListIntegerConverter implements AttributeConverter<List<Integer>, String> {

    @Override
    public String convertToDatabaseColumn(List<Integer> list) {
        if (list == null || list.isEmpty()) {
            return "";
        }
        return StringUtils.join(list, ',');
    }

    @Override
    public List<Integer> convertToEntityAttribute(String stringData) {
        if (StringUtils.isBlank(stringData)) {
            return Collections.emptyList();
        }
        return Stream.of(stringData.split("\\s*,\\s*"))
            .map(Integer::parseInt)
            .collect(Collectors.toList());
    }
}
