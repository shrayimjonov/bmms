package uz.paykap.service.dto;

import uz.paykap.domain.SettingProperty;
import uz.paykap.domain.enumeration.Prop;

public class PropAndValue {
    private Prop prop;
    private String value;

    public PropAndValue(Prop prop, String value) {
        this.prop = prop;
        this.value = value;
    }

    public SettingProperty getSettingProperty() {
        return new SettingProperty().key(prop).value(value);
    }

    public Prop getProp() {
        return prop;
    }

    public void setProp(Prop prop) {
        this.prop = prop;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
