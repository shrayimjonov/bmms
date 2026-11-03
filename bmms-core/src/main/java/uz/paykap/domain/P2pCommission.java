package uz.paykap.domain;

import io.swagger.annotations.ApiModelProperty;
import java.io.Serializable;
import javax.persistence.*;
import javax.validation.constraints.*;

/**
 * A P2pCommission.
 */
@Entity
@Table(name = "p_2_p_commission")
public class P2pCommission extends AbstractAuditingEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    private Long id;

    /**
     * Наименование
     */
    @NotNull
    @ApiModelProperty(value = "Наименование", required = true)
    @Column(name = "name", nullable = false, unique = true)
    private String name;

    /**
     * Комиссия сверху суммы
     */
    @DecimalMin(value = "0")
    @DecimalMax(value = "100")
    @ApiModelProperty(value = "Комиссия сверху суммы")
    @Column(name = "up")
    private Double up;

    /**
     * Комиссия с суммы
     */
    @DecimalMin(value = "0")
    @DecimalMax(value = "100")
    @ApiModelProperty(value = "Комиссия с суммы")
    @Column(name = "down")
    private Double down;

    /**
     * Вознаграждение
     */
    @DecimalMin(value = "0")
    @DecimalMax(value = "100")
    @ApiModelProperty(value = "Вознаграждение")
    @Column(name = "fee")
    private Double fee;

    /**
     * Описание
     */
    @ApiModelProperty(value = "Описание")
    @Column(name = "description")
    private String description;

    // jhipster-needle-entity-add-field - JHipster will add fields here
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public P2pCommission id(Long id) {
        this.id = id;
        return this;
    }

    public String getName() {
        return this.name;
    }

    public P2pCommission name(String name) {
        this.name = name;
        return this;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getUp() {
        return this.up;
    }

    public P2pCommission up(Double up) {
        this.up = up;
        return this;
    }

    public void setUp(Double up) {
        this.up = up;
    }

    public Double getDown() {
        return this.down;
    }

    public P2pCommission down(Double down) {
        this.down = down;
        return this;
    }

    public void setDown(Double down) {
        this.down = down;
    }

    public Double getFee() {
        return this.fee;
    }

    public P2pCommission fee(Double fee) {
        this.fee = fee;
        return this;
    }

    public void setFee(Double fee) {
        this.fee = fee;
    }

    public String getDescription() {
        return this.description;
    }

    public P2pCommission description(String description) {
        this.description = description;
        return this;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof P2pCommission)) {
            return false;
        }
        return id != null && id.equals(((P2pCommission) o).id);
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "P2pCommission{" +
            "id=" + getId() +
            ", name='" + getName() + "'" +
            ", up=" + getUp() +
            ", down=" + getDown() +
            ", fee=" + getFee() +
            ", description='" + getDescription() + "'" +
            "}";
    }
}
