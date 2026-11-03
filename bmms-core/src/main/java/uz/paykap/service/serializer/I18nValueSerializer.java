package uz.paykap.service.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import uz.paykap.service.dto.simple.SimpleI18nDTO;

import java.io.IOException;

public class I18nValueSerializer extends JsonSerializer<SimpleI18nDTO> {

    @Override
    public void serialize(SimpleI18nDTO i18n, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeStartObject();
        jsonGenerator.writeFieldName(String.valueOf(i18n.getLang()).toLowerCase());
        jsonGenerator.writeString(i18n.getText());
        jsonGenerator.writeEndObject();
    }


}
