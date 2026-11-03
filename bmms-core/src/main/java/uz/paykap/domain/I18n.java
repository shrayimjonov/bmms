package uz.paykap.domain;

import java.io.Serializable;
import javax.persistence.*;
import javax.validation.constraints.*;
import org.apache.commons.lang3.StringUtils;
import org.javers.core.metamodel.annotation.DiffIgnore;
import uz.devops.bmcore.enumeration.Lang;

/**
 * Языковые Переводы
 */
@Entity
@Table(name = "i_18_n")
@DiffIgnore
public class I18n implements Serializable {

    private static final long serialVersionUID = 1L;
    public static final int TEXT_MAX_SIZE = 64;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    private Long id;

    /**
     * Language
     */
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "lang", nullable = false)
    private Lang lang;

    /**
     * Text of translation
     */
    @NotNull
    @Size(max = TEXT_MAX_SIZE)
    @Column(name = "text", length = TEXT_MAX_SIZE, nullable = false)
    private String text;

    // jhipster-needle-entity-add-field - JHipster will add fields here
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public I18n id(Long id) {
        this.id = id;
        return this;
    }

    public Lang getLang() {
        return this.lang;
    }

    public I18n lang(Lang lang) {
        this.lang = lang;
        return this;
    }

    public void setLang(Lang lang) {
        this.lang = lang;
    }

    public String getText() {
        return this.text;
    }

    public I18n text(String text) {
        if (text != null) {
            text = StringUtils.substring(text, 0, TEXT_MAX_SIZE);
        }

        this.text = text;
        return this;
    }

    public void setText(String text) {
        if (text != null) {
            text = StringUtils.substring(text, 0, TEXT_MAX_SIZE);
        }
        this.text = text;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof I18n)) {
            return false;
        }
        return id != null && id.equals(((I18n) o).id);
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "I18n{" +
            "id=" + getId() +
            ", lang='" + getLang() + "'" +
            ", text='" + getText() + "'" +
            "}";
    }
}
