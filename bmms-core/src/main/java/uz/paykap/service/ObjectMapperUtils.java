package uz.paykap.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@AllArgsConstructor
public class ObjectMapperUtils {

    private final ObjectMapper mapper;

    public Map<String, String> objectToMap(Object o) {

        try {
            if (o instanceof Map) {
                return new HashMap<>((Map<String, String>) o);
            }
            return mapper.convertValue(o, Map.class);
        } catch (Exception e) {
            log.error("Exception when convert object to map");

            return Collections.emptyMap();
        }
    }

    public JsonNode fromMap(Map<String, String> map) {
        return mapper.convertValue(map, JsonNode.class);
    }
}
