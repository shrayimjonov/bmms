package uz.paykap.domain.converter;

import java.util.HashSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import org.apache.commons.lang3.StringUtils;
import uz.devops.bmcore.enumeration.PayType;

@Converter
public class SetPayTypeConverter implements AttributeConverter<HashSet<PayType>, String> {

    @Override
    public String convertToDatabaseColumn(HashSet<PayType> payTypes) {
        if (payTypes == null || payTypes.isEmpty()) {
            return "";
        }
        return StringUtils.join(payTypes, ',');
    }

    @Override
    public HashSet<PayType> convertToEntityAttribute(String payTypesString) {
        if (StringUtils.isBlank(payTypesString)) {
            return new HashSet<>(0);
        }
        return Stream.of(payTypesString.split("\\s*,\\s*")).map(PayType::valueOf).collect(Collectors.toCollection(HashSet::new));
    }
}
