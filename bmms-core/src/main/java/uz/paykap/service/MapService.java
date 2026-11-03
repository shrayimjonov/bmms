package uz.paykap.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Service
public class MapService {

    private final TypeReference<Map<String, Object>> tr = new TypeReference<>() {
    };

    private final ObjectMapper objectMapper;

    public MapService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public Map<String, Object>  convert(Object o) {
        return objectMapper.convertValue(o, tr);
    }

    public String convertToString(Object o, boolean isRequest) {

        if (o == null) {
            return "";
        }
        try {
            return objectMapper.writeValueAsString(o);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return o.toString();
        }
    }

    public Map<String, Object> convert(String jsonObjectAsString) {
        if (jsonObjectAsString == null || jsonObjectAsString.isEmpty()) {
            return Collections.emptyMap();
        }
        try {
            return objectMapper.readValue(jsonObjectAsString, tr);
        } catch (IOException e) {
            return Collections.emptyMap();
        }
    }

    @SneakyThrows
    public Map<String, Object> writeToMap(Object o, Boolean isRequest) {
        Map<String, Object> map = new HashMap<>();
        map.put(isRequest ? "request" : "response", objectMapper.writeValueAsString(o));
        return map;
    }

}
