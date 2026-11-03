package uz.paykap.service.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import uz.paykap.domain.I18n;
import uz.paykap.service.serializer.I18nListToObjectSerializer;

import java.util.Set;

public class KeyWithNames {
    private String key;

    @JsonSerialize(using = I18nListToObjectSerializer.class)
    private Set<I18n> values;

    public KeyWithNames() {
    }

    public KeyWithNames(String key, Set<I18n> values) {
        this.key = key;
        this.values = values;
    }

    @Override
    public String toString() {
        return "KeyWithNames{" +
            "key='" + key + '\'' +
            ", values=" + values +
            '}';
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public Set<I18n> getValues() {
        return values;
    }

    public void setValues(Set<I18n> values) {
        this.values = values;
    }
}
