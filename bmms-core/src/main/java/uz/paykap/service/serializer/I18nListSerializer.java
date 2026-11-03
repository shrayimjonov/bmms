package uz.paykap.service.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import uz.paykap.service.dto.simple.SimpleI18nDTO;

import java.io.IOException;
import java.util.Collection;

public class I18nListSerializer extends JsonSerializer<Collection<SimpleI18nDTO>> {

    @Override
    public void serialize(Collection<SimpleI18nDTO> values, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {

        jsonGenerator.writeStartObject();
        for (SimpleI18nDTO i18n : values) {
            jsonGenerator.writeObjectField(String.valueOf(i18n.getLang()).toLowerCase(), i18n.getText());
        }
        jsonGenerator.writeEndObject();
    }

}
