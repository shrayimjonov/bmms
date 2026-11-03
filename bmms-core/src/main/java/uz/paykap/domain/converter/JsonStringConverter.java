package uz.paykap.domain.converter;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import lombok.SneakyThrows;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.Map;

@Converter
public class JsonStringConverter implements AttributeConverter<Map<String, String>, String> {

    //    private ObjectMapper objectMapper = new ObjectMapper();
    private Gson gson = new Gson();
    private Type type = new TypeToken<Map<String, String>>() {
    }.getType();

    @SneakyThrows
    @Override
    public String convertToDatabaseColumn(Map<String, String> attribute) {
        if (attribute == null) {
            return "{}";
        }
        return gson.toJson(attribute);
    }

    @Override
    public Map<String, String> convertToEntityAttribute(String dbData) {

        if (dbData == null) {
            return Collections.emptyMap();
        }

        try {
            return gson.fromJson(dbData, type);
        } catch (JsonSyntaxException e) {
            return Collections.emptyMap();
        }
    }
}
