package uz.paykap.service.dto;

import io.swagger.annotations.ApiModelProperty;
import java.io.Serializable;
import java.util.Objects;
import javax.validation.constraints.*;
import uz.devops.bmcore.enumeration.PayType;

/**
 * A DTO for the {@link uz.paykap.domain.Sender} entity.
 */
public class SenderDTO implements Serializable {

    private Long id;

    /**
     * Название
     */
    @NotNull
    @Size(max = 64)
    @ApiModelProperty(value = "Название", required = true)
    private String name;

    /**
     * Поряд. номер
     */
    @NotNull
    @ApiModelProperty(value = "Поряд. номер", required = true)
    private Long ord;

    /**
     * Активный
     */
    @NotNull
    @ApiModelProperty(value = "Активный", required = true)
    private Boolean active;

    /**
     * Класс реализации
     */
    @Size(max = 128)
    @ApiModelProperty(value = "Класс реализации")
    private String springBeanName;

    /**
     * Тип оплаты
     */
    @NotNull
    @ApiModelProperty(value = "Тип оплаты", required = true)
    private PayType payType;

    private Long agentId;

    private String agentShortName;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getOrd() {
        return ord;
    }

    public void setOrd(Long ord) {
        this.ord = ord;
    }

    public Boolean isActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public String getSpringBeanName() {
        return springBeanName;
    }

    public void setSpringBeanName(String springBeanName) {
        this.springBeanName = springBeanName;
    }

    public PayType getPayType() {
        return payType;
    }

    public void setPayType(PayType payType) {
        this.payType = payType;
    }

    public Long getAgentId() {
        return agentId;
    }

    public void setAgentId(Long agentId) {
        this.agentId = agentId;
    }

    public String getAgentShortName() {
        return agentShortName;
    }

    public void setAgentShortName(String agentShortName) {
        this.agentShortName = agentShortName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        SenderDTO senderDTO = (SenderDTO) o;
        if (senderDTO.getId() == null || getId() == null) {
            return false;
        }
        return Objects.equals(getId(), senderDTO.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    @Override
    public String toString() {
        return (
            "SenderDTO{" +
            "id=" +
            getId() +
            ", name='" +
            getName() +
            "'" +
            ", ord=" +
            getOrd() +
            ", active='" +
            isActive() +
            "'" +
            ", springBeanName='" +
            getSpringBeanName() +
            "'" +
            ", payType='" +
            getPayType() +
            "'" +
            ", agent=" +
            getAgentId() +
            ", agent='" +
            getAgentShortName() +
            "'" +
            "}"
        );
    }
}
