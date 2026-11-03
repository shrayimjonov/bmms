package uz.paykap.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.*;
import javax.validation.constraints.*;

/**
 * Категории поставщиков
 */
@Entity
@Table(name = "category")
public class Category extends AbstractAuditingEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    private Long id;

    /**
     * Short name to show in UI
     */
    @NotNull
    @Size(max = 64)
    @Column(name = "short_name", length = 64, nullable = false)
    private String shortName;

    /**
     * Thumbnail file path or url
     */
    @Size(max = 64)
    @Column(name = "thumbnail", length = 64)
    private String thumbnail;

    /**
     * Thumbnail file
     */
    @Lob
    @Column(name = "thumbnail_file")
    private byte[] thumbnailFile;

    @Column(name = "thumbnail_file_content_type")
    private String thumbnailFileContentType;

    /**
     * Logo file path or url
     */
    @Size(max = 64)
    @Column(name = "logo", length = 64)
    private String logo;

    /**
     * Logo file
     */
    @Lob
    @Column(name = "logo_file")
    private byte[] logoFile;

    @Column(name = "logo_file_content_type")
    private String logoFileContentType;

    /**
     * Display order
     */
    @Column(name = "ord")
    private Integer ord;

    /**
     * Activeness
     */
    @NotNull
    @Column(name = "active", nullable = false)
    private Boolean active;

    @ManyToOne
    @JsonIgnoreProperties(value = { "parent", "names" }, allowSetters = true)
    private Category parent;

    @ManyToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE }, fetch = FetchType.EAGER)
    @NotNull
    @JoinTable(
        name = "rel_category__name",
        joinColumns = @JoinColumn(name = "category_id"),
        inverseJoinColumns = @JoinColumn(name = "name_id")
    )
    private Set<I18n> names = new HashSet<>();

    // jhipster-needle-entity-add-field - JHipster will add fields here
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Category id(Long id) {
        this.id = id;
        return this;
    }

    public String getShortName() {
        return this.shortName;
    }

    public Category shortName(String shortName) {
        this.shortName = shortName;
        return this;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    public String getThumbnail() {
        return this.thumbnail;
    }

    public Category thumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
        return this;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }

    public byte[] getThumbnailFile() {
        return this.thumbnailFile;
    }

    public Category thumbnailFile(byte[] thumbnailFile) {
        this.thumbnailFile = thumbnailFile;
        return this;
    }

    public void setThumbnailFile(byte[] thumbnailFile) {
        this.thumbnailFile = thumbnailFile;
    }

    public String getThumbnailFileContentType() {
        return this.thumbnailFileContentType;
    }

    public Category thumbnailFileContentType(String thumbnailFileContentType) {
        this.thumbnailFileContentType = thumbnailFileContentType;
        return this;
    }

    public void setThumbnailFileContentType(String thumbnailFileContentType) {
        this.thumbnailFileContentType = thumbnailFileContentType;
    }

    public String getLogo() {
        return this.logo;
    }

    public Category logo(String logo) {
        this.logo = logo;
        return this;
    }

    public void setLogo(String logo) {
        this.logo = logo;
    }

    public byte[] getLogoFile() {
        return this.logoFile;
    }

    public Category logoFile(byte[] logoFile) {
        this.logoFile = logoFile;
        return this;
    }

    public void setLogoFile(byte[] logoFile) {
        this.logoFile = logoFile;
    }

    public String getLogoFileContentType() {
        return this.logoFileContentType;
    }

    public Category logoFileContentType(String logoFileContentType) {
        this.logoFileContentType = logoFileContentType;
        return this;
    }

    public void setLogoFileContentType(String logoFileContentType) {
        this.logoFileContentType = logoFileContentType;
    }

    public Integer getOrd() {
        return this.ord;
    }

    public Category ord(Integer ord) {
        this.ord = ord;
        return this;
    }

    public void setOrd(Integer ord) {
        this.ord = ord;
    }

    public Boolean isActive() {
        return this.active;
    }

    public Boolean getActive() {
        return this.active;
    }

    public Category active(Boolean active) {
        this.active = active;
        return this;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public Category getParent() {
        return this.parent;
    }

    public Category parent(Category category) {
        this.setParent(category);
        return this;
    }

    public void setParent(Category category) {
        this.parent = category;
    }

    public Set<I18n> getNames() {
        return this.names;
    }

    public Category names(Set<I18n> i18ns) {
        this.setNames(i18ns);
        return this;
    }

    public Category addName(I18n i18n) {
        this.names.add(i18n);
        return this;
    }

    public Category removeName(I18n i18n) {
        this.names.remove(i18n);
        return this;
    }

    public void setNames(Set<I18n> i18ns) {
        this.names = i18ns;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Category)) {
            return false;
        }
        return id != null && id.equals(((Category) o).id);
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "Category{" +
            "id=" + getId() +
            ", shortName='" + getShortName() + "'" +
            ", thumbnail='" + getThumbnail() + "'" +
            ", thumbnailFile='" + getThumbnailFile() + "'" +
            ", thumbnailFileContentType='" + getThumbnailFileContentType() + "'" +
            ", logo='" + getLogo() + "'" +
            ", logoFile='" + getLogoFile() + "'" +
            ", logoFileContentType='" + getLogoFileContentType() + "'" +
            ", ord=" + getOrd() +
            ", active='" + getActive() + "'" +
            "}";
    }
}
