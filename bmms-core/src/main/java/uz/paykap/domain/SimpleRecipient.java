package uz.paykap.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModel;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import javax.persistence.*;
import javax.validation.constraints.NotNull;
import org.hibernate.annotations.Immutable;

/**
 * Получатель денежных средств, поставщик услуг
 */
@ApiModel(description = "Получатель денежных средств, поставщик услуг")
@Immutable
@Entity
@Table(name = "recipient")
public class SimpleRecipient implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    private Long id;

    /**
     * Уникальное название в шлюзе
     */
    @JsonIgnore
    @Column(name = "short_name", length = 64, nullable = false)
    private String shortName;

    @JsonIgnore
    @NotNull
    @Column(name = "active", nullable = false)
    private Boolean active;

    @ManyToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE })
    @NotNull
    @JoinTable(
        name = "rel_recipient__title",
        joinColumns = @JoinColumn(name = "recipient_id", referencedColumnName = "id"),
        inverseJoinColumns = @JoinColumn(name = "title_id", referencedColumnName = "id")
    )
    private Set<I18n> titles = new HashSet<>();

    // jhipster-needle-entity-add-field - JHipster will add fields here, do not remove
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getShortName() {
        return shortName;
    }

    public SimpleRecipient shortName(String shortName) {
        this.shortName = shortName;
        return this;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    public Boolean isActive() {
        return active;
    }

    public SimpleRecipient active(Boolean active) {
        this.active = active;
        return this;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public Set<I18n> getTitles() {
        return titles;
    }

    public SimpleRecipient titles(Set<I18n> i18ns) {
        this.titles = i18ns;
        return this;
    }

    public SimpleRecipient addTitle(I18n i18n) {
        this.titles.add(i18n);
        return this;
    }

    public SimpleRecipient removeTitle(I18n i18n) {
        this.titles.remove(i18n);
        return this;
    }

    public void setTitles(Set<I18n> i18ns) {
        this.titles = i18ns;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        SimpleRecipient recipient = (SimpleRecipient) o;
        if (recipient.getId() == null || getId() == null) {
            return false;
        }
        return Objects.equals(getId(), recipient.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    @Override
    public String toString() {
        return "Recipient{" + "id=" + getId() + ", shortName='" + getShortName() + "'" + ", active='" + isActive() + "'" + "}";
    }
}
