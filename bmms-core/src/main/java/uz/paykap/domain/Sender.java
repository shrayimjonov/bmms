package uz.paykap.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.io.Serializable;
import javax.persistence.*;
import javax.validation.constraints.*;
import uz.devops.bmcore.enumeration.PayType;

/**
 * A Sender.
 */
@Entity
@Table(name = "sender")
public class Sender extends AbstractAuditingEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    private Long id;

    /**
     * Название
     */
    @NotNull
    @Size(max = 64)
    @Column(name = "name", length = 64, nullable = false)
    private String name;

    /**
     * Поряд. номер
     */
    @NotNull
    @Column(name = "ord", nullable = false)
    private Long ord;

    /**
     * Активный
     */
    @NotNull
    @Column(name = "active", nullable = false)
    private Boolean active;

    /**
     * Класс реализации
     */
    @Size(max = 128)
    @Column(name = "spring_bean_name", length = 128)
    private String springBeanName;

    /**
     * Тип оплаты
     */
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "pay_type", nullable = false)
    private PayType payType;

    @ManyToOne
    @JsonIgnoreProperties(value = { "titles", "deposit" }, allowSetters = true)
    private Agent agent;

    // jhipster-needle-entity-add-field - JHipster will add fields here
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Sender id(Long id) {
        this.id = id;
        return this;
    }

    public String getName() {
        return this.name;
    }

    public Sender name(String name) {
        this.name = name;
        return this;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getOrd() {
        return this.ord;
    }

    public Sender ord(Long ord) {
        this.ord = ord;
        return this;
    }

    public void setOrd(Long ord) {
        this.ord = ord;
    }

    public Boolean isActive() {
        return this.active;
    }

    public Boolean getActive() {
        return this.active;
    }

    public Sender active(Boolean active) {
        this.active = active;
        return this;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public String getSpringBeanName() {
        return this.springBeanName;
    }

    public Sender springBeanName(String springBeanName) {
        this.springBeanName = springBeanName;
        return this;
    }

    public void setSpringBeanName(String springBeanName) {
        this.springBeanName = springBeanName;
    }

    public PayType getPayType() {
        return this.payType;
    }

    public Sender payType(PayType payType) {
        this.payType = payType;
        return this;
    }

    public void setPayType(PayType payType) {
        this.payType = payType;
    }

    public Agent getAgent() {
        return this.agent;
    }

    public Sender agent(Agent agent) {
        this.setAgent(agent);
        return this;
    }

    public void setAgent(Agent agent) {
        this.agent = agent;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Sender)) {
            return false;
        }
        return id != null && id.equals(((Sender) o).id);
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "Sender{" +
            "id=" + getId() +
            ", name='" + getName() + "'" +
            ", ord=" + getOrd() +
            ", active='" + getActive() + "'" +
            ", springBeanName='" + getSpringBeanName() + "'" +
            ", payType='" + getPayType() + "'" +
            "}";
    }
}
