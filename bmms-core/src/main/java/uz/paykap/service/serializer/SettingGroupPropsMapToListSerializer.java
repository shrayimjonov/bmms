package uz.paykap.service.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import uz.paykap.domain.enumeration.Prop;
import uz.paykap.service.dto.SettingPropertyDTO;

import java.io.IOException;
import java.util.Map;

public class SettingGroupPropsMapToListSerializer extends JsonSerializer<Map<Prop, SettingPropertyDTO>> {

    @Override
    public void serialize(Map<Prop, SettingPropertyDTO> values, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {

        jsonGenerator.writeStartArray();

        for (SettingPropertyDTO value : values.values()) {
            jsonGenerator.writeObject(value);
        }

        jsonGenerator.writeEndArray();

    }

}
