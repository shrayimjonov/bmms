package uz.paykap.service.serializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import uz.paykap.domain.enumeration.Prop;
import uz.paykap.service.dto.SettingPropertyDTO;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SettingGroupPropsMapToListDeserializer extends JsonDeserializer<Map<Prop, SettingPropertyDTO>> {

    private ObjectMapper objectMapper = new ObjectMapper();
    private JavaType customClassCollection = objectMapper.getTypeFactory().constructCollectionType(List.class, SettingPropertyDTO.class);

    @Override
    public Map<Prop, SettingPropertyDTO> deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {

        List<SettingPropertyDTO> props = deserializationContext.readValue(jsonParser, customClassCollection);

        Map<Prop, SettingPropertyDTO> map = new HashMap<>(props.size());

        for (SettingPropertyDTO property : props) {
            map.put(property.getKey(), property);
        }

        return map;
    }
}
