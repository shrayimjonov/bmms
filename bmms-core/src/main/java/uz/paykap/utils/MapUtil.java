package uz.paykap.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.experimental.UtilityClass;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;

@UtilityClass
public class MapUtil {

    private final static ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private final static TypeReference<Map<String, String>> tr = new TypeReference<>() {
    };

    public static Map<String, String> convert(Object o) {
        return OBJECT_MAPPER.convertValue(o, tr);
    }

    public static Map<String, String> convert(String jsonObjectAsString) {
        if (jsonObjectAsString == null || jsonObjectAsString.isEmpty()) {
            return Collections.emptyMap();
        }
        try {
            return OBJECT_MAPPER.readValue(jsonObjectAsString, tr);
        } catch (IOException e) {
            return Collections.emptyMap();
        }
    }

    public static Map<String, String> errorMap(Exception e) {
        return Collections.singletonMap("error", e.getLocalizedMessage());
    }

    public static boolean keyNotFoundOrEmpty(Map<String, Object> params, String... names) {
        if (params == null) {
            return true;
        }
        for (String name : names) {
            if (!params.containsKey(name) || params.get(name) == null || String.valueOf(params.get(name)).isEmpty()) {
                return true;
            }
        }

        return false;
    }
}
