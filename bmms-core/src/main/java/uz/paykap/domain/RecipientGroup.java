package uz.paykap.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.*;
import javax.validation.constraints.*;

/**
 * Группа поставщиков
 */
@Entity
@Table(name = "recipient_group")
public class RecipientGroup extends AbstractAuditingEntity implements Serializable {

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
     * Активность
     */
    @NotNull
    @Column(name = "active", nullable = false)
    private Boolean active;

    /**
     * Дополнительная информация
     */
    @Column(name = "more")
    private String more;

    @ManyToMany
    @NotNull
    @JoinTable(
        name = "rel_recipient_group__recipient",
        joinColumns = @JoinColumn(name = "recipient_group_id"),
        inverseJoinColumns = @JoinColumn(name = "recipient_id")
    )
    @JsonIgnoreProperties(value = { "gateway", "category", "titles", "fullNames" }, allowSetters = true)
    private Set<Recipient> recipients = new HashSet<>();

    // jhipster-needle-entity-add-field - JHipster will add fields here
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public RecipientGroup id(Long id) {
        this.id = id;
        return this;
    }

    public String getName() {
        return this.name;
    }

    public RecipientGroup name(String name) {
        this.name = name;
        return this;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Boolean isActive() {
        return this.active;
    }

    public Boolean getActive() {
        return this.active;
    }

    public RecipientGroup active(Boolean active) {
        this.active = active;
        return this;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public String getMore() {
        return this.more;
    }

    public RecipientGroup more(String more) {
        this.more = more;
        return this;
    }

    public void setMore(String more) {
        this.more = more;
    }

    public Set<Recipient> getRecipients() {
        return this.recipients;
    }

    public RecipientGroup recipients(Set<Recipient> recipients) {
        this.setRecipients(recipients);
        return this;
    }

    public RecipientGroup addRecipient(Recipient recipient) {
        this.recipients.add(recipient);
        return this;
    }

    public RecipientGroup removeRecipient(Recipient recipient) {
        this.recipients.remove(recipient);
        return this;
    }

    public void setRecipients(Set<Recipient> recipients) {
        this.recipients = recipients;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof RecipientGroup)) {
            return false;
        }
        return id != null && id.equals(((RecipientGroup) o).id);
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "RecipientGroup{" +
            "id=" + getId() +
            ", name='" + getName() + "'" +
            ", active='" + getActive() + "'" +
            ", more='" + getMore() + "'" +
            "}";
    }
}
