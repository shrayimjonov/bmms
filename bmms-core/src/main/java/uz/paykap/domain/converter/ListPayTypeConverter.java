package uz.paykap.domain.converter;

import org.apache.commons.lang3.StringUtils;
import uz.devops.bmcore.enumeration.PayType;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Converter
public class ListPayTypeConverter implements AttributeConverter<List<PayType>, String> {


    @Override
    public String convertToDatabaseColumn(List<PayType> payTypes) {
        if (payTypes == null || payTypes.isEmpty()) {
            return "";
        }
        return StringUtils.join(payTypes, ',');
    }

    @Override
    public List<PayType> convertToEntityAttribute(String stringData) {
        if (StringUtils.isBlank(stringData)) {
            return Collections.emptyList();
        }
        return Stream.of(stringData.split("\\s*,\\s*"))
            .map(PayType::valueOf)
            .collect(Collectors.toList());
    }
}
