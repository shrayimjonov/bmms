package uz.paykap.domain;

import java.io.Serializable;
import javax.persistence.*;
import javax.validation.constraints.*;

/**
 * A RecipientFileSchema.
 */
@Entity
@Table(name = "recipient_file_schema")
public class RecipientFileSchema extends AbstractAuditingEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    private Long id;

    @NotNull
    @Size(max = 32)
    @Column(name = "name", length = 32, nullable = false)
    private String name;

    @NotNull
    @Size(max = 2048)
    @Column(name = "id_list", length = 2048, nullable = false)
    private String idList;

    @Size(max = 512)
    @Column(name = "root", length = 512)
    private String root;

    // jhipster-needle-entity-add-field - JHipster will add fields here
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public RecipientFileSchema id(Long id) {
        this.id = id;
        return this;
    }

    public String getName() {
        return this.name;
    }

    public RecipientFileSchema name(String name) {
        this.name = name;
        return this;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIdList() {
        return this.idList;
    }

    public RecipientFileSchema idList(String idList) {
        this.idList = idList;
        return this;
    }

    public void setIdList(String idList) {
        this.idList = idList;
    }

    public String getRoot() {
        return this.root;
    }

    public RecipientFileSchema root(String root) {
        this.root = root;
        return this;
    }

    public void setRoot(String root) {
        this.root = root;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof RecipientFileSchema)) {
            return false;
        }
        return id != null && id.equals(((RecipientFileSchema) o).id);
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "RecipientFileSchema{" +
            "id=" + getId() +
            ", name='" + getName() + "'" +
            ", idList='" + getIdList() + "'" +
            ", root='" + getRoot() + "'" +
            "}";
    }
}
