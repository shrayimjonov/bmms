package uz.paykap.service.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import uz.paykap.domain.I18n;

import java.io.IOException;
import java.util.Collection;

public class I18nListToObjectSerializer extends JsonSerializer<Collection<I18n>> {

    @Override
    public void serialize(Collection<I18n> values, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {

        jsonGenerator.writeStartObject();
        for (I18n i18n : values) {
            jsonGenerator.writeObjectField(String.valueOf(i18n.getLang()).toLowerCase(), i18n.getText());
        }
        jsonGenerator.writeEndObject();
    }

}
