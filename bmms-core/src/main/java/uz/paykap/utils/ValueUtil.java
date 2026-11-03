package uz.paykap.utils;

import lombok.experimental.UtilityClass;

import java.util.Optional;

@UtilityClass
public class ValueUtil {

    public static <V> Optional<V> getFirstValid(V... values) {

        for (V value : values) {
            if (value != null) {
                return Optional.of(value);
            }
        }

        return Optional.empty();
    }

    public static Optional<String> getFirstValid(String... values) {

        if (values == null || values.length == 0) {
            return Optional.empty();
        }

        for (String value : values) {
            if (value != null && !value.isEmpty()) {
                return Optional.of(value);
            }
        }

        return Optional.empty();
    }

}
