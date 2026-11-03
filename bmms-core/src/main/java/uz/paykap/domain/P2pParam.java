package uz.paykap.domain;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.hibernate.annotations.Type;
import uz.paykap.domain.enumeration.P2pProcessorType;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;

/**
 * P2P параметры\n@author Javlon
 */
@ApiModel(description = "P2P параметры\n@author Javlon")
@Entity
@Table(name = "p_2_p_param")
public class P2pParam extends AbstractAuditingEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    private Long id;

    /**
     * Приоритет
     */
    @NotNull
    @ApiModelProperty(value = "Приоритет", required = true)
    @Column(name = "priority", nullable = false)
    private Integer priority;

    /**
     * Активный
     */
    @NotNull
    @ApiModelProperty(value = "Активный", required = true)
    @Column(name = "active", nullable = false)
    private Boolean active;

    /**
     * ПЦ отправителя
     */
    @NotNull
    @ApiModelProperty(value = "ПЦ отправителя", required = true)
    @Enumerated(EnumType.STRING)
    @Column(name = "sender_pc", nullable = false)
    private P2pProcessorType senderPc;

    /**
     * ПЦ получателя
     */
    @NotNull
    @ApiModelProperty(value = "ПЦ получателя", required = true)
    @Enumerated(EnumType.STRING)
    @Column(name = "recipient_pc", nullable = false)
    private P2pProcessorType recipientPc;

    /**
     * Json параметры
     */
    @ApiModelProperty(value = "Json параметры")
    @Lob
    @Type(type = "org.hibernate.type.TextType")
    @Column(name = "value")
    private String value;

    @Size(max = 64)
    @Column(name = "name", length = 64)
    private String name;

    @ManyToOne(optional = false)
    @NotNull
    private P2pCondition senderCondition;

    @ManyToOne(optional = false)
    @NotNull
    private P2pCondition recipientCondition;

    @ManyToOne(optional = false)
    @NotNull
    private P2pCommission senderPercent;

    @ManyToOne(optional = false)
    @NotNull
    private P2pCommission recipientPercent;

    @ApiModelProperty(value = "Короткий код латинскими буквами и цыфрами")
    @Size(max = 128)
    @Column(name = "promo", length = 128)
    private String promo;

    // jhipster-needle-entity-add-field - JHipster will add fields here
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public P2pParam id(Long id) {
        this.id = id;
        return this;
    }

    public Integer getPriority() {
        return this.priority;
    }

    public P2pParam priority(Integer priority) {
        this.priority = priority;
        return this;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    public Boolean getActive() {
        return this.active;
    }

    public Boolean isActive() {
        return this.active;
    }

    public P2pParam active(Boolean active) {
        this.active = active;
        return this;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public P2pProcessorType getSenderPc() {
        return this.senderPc;
    }

    public P2pParam senderPc(P2pProcessorType senderPc) {
        this.senderPc = senderPc;
        return this;
    }

    public void setSenderPc(P2pProcessorType senderPc) {
        this.senderPc = senderPc;
    }

    public P2pProcessorType getRecipientPc() {
        return this.recipientPc;
    }

    public P2pParam recipientPc(P2pProcessorType recipientPc) {
        this.recipientPc = recipientPc;
        return this;
    }

    public void setRecipientPc(P2pProcessorType recipientPc) {
        this.recipientPc = recipientPc;
    }

    public String getValue() {
        return this.value;
    }

    public P2pParam value(String value) {
        this.value = value;
        return this;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getName() {
        return this.name;
    }

    public P2pParam name(String name) {
        this.name = name;
        return this;
    }

    public void setName(String name) {
        this.name = name;
    }

    public P2pCondition getSenderCondition() {
        return this.senderCondition;
    }

    public P2pParam senderCondition(P2pCondition p2pCondition) {
        this.setSenderCondition(p2pCondition);
        return this;
    }

    public void setSenderCondition(P2pCondition p2pCondition) {
        this.senderCondition = p2pCondition;
    }

    public P2pCondition getRecipientCondition() {
        return this.recipientCondition;
    }

    public P2pParam recipientCondition(P2pCondition p2pCondition) {
        this.setRecipientCondition(p2pCondition);
        return this;
    }

    public void setRecipientCondition(P2pCondition p2pCondition) {
        this.recipientCondition = p2pCondition;
    }

    public P2pCommission getSenderPercent() {
        return this.senderPercent;
    }

    public P2pParam senderPercent(P2pCommission p2pCommission) {
        this.setSenderPercent(p2pCommission);
        return this;
    }

    public void setSenderPercent(P2pCommission p2pCommission) {
        this.senderPercent = p2pCommission;
    }

    public P2pCommission getRecipientPercent() {
        return this.recipientPercent;
    }

    public P2pParam recipientPercent(P2pCommission p2pCommission) {
        this.setRecipientPercent(p2pCommission);
        return this;
    }

    public void setRecipientPercent(P2pCommission p2pCommission) {
        this.recipientPercent = p2pCommission;
    }

    public String getPromo() {
        return promo;
    }

    public void setPromo(String promo) {
        this.promo = promo;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof P2pParam)) {
            return false;
        }
        return id != null && id.equals(((P2pParam) o).id);
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "P2pParam{" +
            "id=" + getId() +
            ", priority=" + getPriority() +
            ", active='" + getActive() + "'" +
            ", senderPc='" + getSenderPc() + "'" +
            ", recipientPc='" + getRecipientPc() + "'" +
            ", value='" + getValue() + "'" +
            ", name='" + getName() + "'" +
            ", promo='" + getPromo() + "'" +
            "}";
    }
}
