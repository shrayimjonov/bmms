package uz.paykap.service.dto.common;

import org.apache.commons.lang3.builder.ToStringBuilder;

public class OptionalProperty<TYPE> {

    private boolean apply = false;
    private TYPE value;

    public boolean isApply() {
        return apply;
    }

    public void setApply(boolean apply) {
        this.apply = apply;
    }

    public TYPE getValue() {
        return value;
    }

    public void setValue(TYPE value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
            .append("apply", apply)
            .append("value", value)
            .toString();
    }
}
