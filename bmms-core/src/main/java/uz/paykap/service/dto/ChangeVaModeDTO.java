package uz.paykap.service.dto;

import uz.paykap.domain.enumeration.ModeType;

import javax.validation.constraints.NotNull;

public class ChangeVaModeDTO {

    @NotNull
    private Long id;

    @NotNull
    private ModeType fromMode;

    @NotNull
    private ModeType toMode;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ModeType getFromMode() {
        return fromMode;
    }

    public void setFromMode(ModeType fromMode) {
        this.fromMode = fromMode;
    }

    public ModeType getToMode() {
        return toMode;
    }

    public void setToMode(ModeType toMode) {
        this.toMode = toMode;
    }

    @Override
    public String toString() {
        return "ChangeVaModeDTO{" +
            "id=" + id +
            ", fromMode=" + fromMode +
            ", toMode=" + toMode +
            '}';
    }
}
