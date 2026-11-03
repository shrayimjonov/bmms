package uz.paykap.service.dto;

import io.swagger.annotations.ApiModelProperty;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import uz.devops.bmcore.enumeration.RequestType;
import uz.devops.bmcore.enumeration.Visibility;
import uz.paykap.domain.enumeration.ResponseFieldType;

/**
 * A DTO for the {@link uz.paykap.domain.ResponseField} entity.
 */
public class ResponseFieldDTO implements Serializable {

    private Long id;

    /**
     * Response field name; account_id, phone, login, ...
     */
    @NotNull
    @Size(max = 128)
    @ApiModelProperty(value = "Response field name; account_id, phone, login, ...", required = true)
    private String name;

    /**
     * Visible in UI, can be used for hidden static fields
     */
    @ApiModelProperty(value = "Visible in UI, can be used for hidden static fields")
    private Visibility visibility;

    /**
     * Display order
     */
    @ApiModelProperty(value = "Display order")
    private Integer ord;

    /**
     * Define to which of CHECK,PAY,STATUS,CANCEL,DEPOSIT,RECONCILE,CREDIT,DEBIT,P2P_CREDIT,P2P_DEBIT belongs that response field
     */
    @ApiModelProperty(
        value = "Define to which of CHECK,PAY,STATUS,CANCEL,DEPOSIT,RECONCILE,CREDIT,DEBIT,P2P_CREDIT,P2P_DEBIT belongs that response field"
    )
    private RequestType requestType;

    /**
     * Тип
     */
    @ApiModelProperty(value = "Тип")
    private ResponseFieldType type;

    /**
     * Доля суммы, 1 - сум, 100 - тийин
     */
    @ApiModelProperty(value = "Доля суммы, 1 - сум, 100 - тийин", required = false)
    private Integer amountRatio;

    private RecipientDTO recipient;

    private Set<I18nDTO> labels = new HashSet<>();

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

    public Visibility getVisibility() {
        return visibility;
    }

    public void setVisibility(Visibility visibility) {
        this.visibility = visibility;
    }

    public Integer getOrd() {
        return ord;
    }

    public void setOrd(Integer ord) {
        this.ord = ord;
    }

    public RequestType getRequestType() {
        return requestType;
    }

    public void setRequestType(RequestType requestType) {
        this.requestType = requestType;
    }

    public ResponseFieldType getType() {
        return type;
    }

    public void setType(ResponseFieldType type) {
        this.type = type;
    }

    public Integer getAmountRatio() {
        return amountRatio;
    }

    public void setAmountRatio(Integer amountRatio) {
        this.amountRatio = amountRatio;
    }

    public RecipientDTO getRecipient() {
        return recipient;
    }

    public void setRecipient(RecipientDTO recipient) {
        this.recipient = recipient;
    }

    public Set<I18nDTO> getLabels() {
        return labels;
    }

    public void setLabels(Set<I18nDTO> labels) {
        this.labels = labels;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ResponseFieldDTO)) {
            return false;
        }

        ResponseFieldDTO responseFieldDTO = (ResponseFieldDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, responseFieldDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "ResponseFieldDTO{" +
            "id=" + getId() +
            ", name='" + getName() + "'" +
            ", visibility='" + getVisibility() + "'" +
            ", ord=" + getOrd() +
            ", requestType='" + getRequestType() + "'" +
            ", type='" + getType() + "'" +
            ", amountRatio=" + getAmountRatio() +
            ", recipient=" + getRecipient() +
            ", labels=" + getLabels() +
            "}";
    }
}
