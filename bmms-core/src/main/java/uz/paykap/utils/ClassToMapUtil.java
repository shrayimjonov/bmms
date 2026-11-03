package uz.paykap.utils;

import lombok.experimental.UtilityClass;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

@UtilityClass
public final class ClassToMapUtil {

    public static Map<String, String> convertObjToMapString(Class clazz, String prefix, Object o) {
        Map<String,String> stringMap = new HashMap<>();
        for (Field field : clazz.getDeclaredFields()){
            field.setAccessible(true);

            String key = prefix + "." + field.getName();

            try {
                stringMap.put(key, String.valueOf(field.get(o)));
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }

        return stringMap;
    }
}
