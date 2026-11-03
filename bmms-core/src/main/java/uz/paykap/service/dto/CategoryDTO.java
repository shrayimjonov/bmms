package uz.paykap.service.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import javax.persistence.Lob;
import javax.validation.constraints.*;

/**
 * A DTO for the {@link uz.paykap.domain.Category} entity.
 */
@ApiModel(description = "Категории поставщиков")
public class CategoryDTO extends AbstractAuditingDTO implements Serializable {

    private Long id;

    /**
     * Short name to show in UI
     */
    @NotNull
    @Size(max = 64)
    @ApiModelProperty(value = "Short name to show in UI", required = true)
    private String shortName;

    /**
     * Thumbnail file path or url
     */
    @Size(max = 64)
    @ApiModelProperty(value = "Thumbnail file path or url")
    private String thumbnail;

    /**
     * Thumbnail file
     */
    @ApiModelProperty(value = "Thumbnail file")
    @Lob
    private byte[] thumbnailFile;

    private String thumbnailFileContentType;

    /**
     * Logo file path or url
     */
    @Size(max = 64)
    @ApiModelProperty(value = "Logo file path or url")
    private String logo;

    /**
     * Logo file
     */
    @ApiModelProperty(value = "Logo file")
    @Lob
    private byte[] logoFile;

    private String logoFileContentType;

    /**
     * Display order
     */
    @ApiModelProperty(value = "Display order")
    private Integer ord;

    /**
     * Activeness
     */
    @NotNull
    @ApiModelProperty(value = "Activeness", required = true)
    private Boolean active;

    private CategoryDTO parent;

    private Set<I18nDTO> names = new HashSet<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }

    public byte[] getThumbnailFile() {
        return thumbnailFile;
    }

    public void setThumbnailFile(byte[] thumbnailFile) {
        this.thumbnailFile = thumbnailFile;
    }

    public String getThumbnailFileContentType() {
        return thumbnailFileContentType;
    }

    public void setThumbnailFileContentType(String thumbnailFileContentType) {
        this.thumbnailFileContentType = thumbnailFileContentType;
    }

    public String getLogo() {
        return logo;
    }

    public void setLogo(String logo) {
        this.logo = logo;
    }

    public byte[] getLogoFile() {
        return logoFile;
    }

    public void setLogoFile(byte[] logoFile) {
        this.logoFile = logoFile;
    }

    public String getLogoFileContentType() {
        return logoFileContentType;
    }

    public void setLogoFileContentType(String logoFileContentType) {
        this.logoFileContentType = logoFileContentType;
    }

    public Integer getOrd() {
        return ord;
    }

    public void setOrd(Integer ord) {
        this.ord = ord;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public CategoryDTO getParent() {
        return parent;
    }

    public void setParent(CategoryDTO parent) {
        this.parent = parent;
    }

    public Set<I18nDTO> getNames() {
        return names;
    }

    public void setNames(Set<I18nDTO> names) {
        this.names = names;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof CategoryDTO)) {
            return false;
        }

        CategoryDTO categoryDTO = (CategoryDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, categoryDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "CategoryDTO{" +
            "id=" + getId() +
            ", shortName='" + getShortName() + "'" +
            ", thumbnail='" + getThumbnail() + "'" +
//            ", thumbnailFile='" + getThumbnailFile() + "'" +
            ", logo='" + getLogo() + "'" +
//            ", logoFile='" + getLogoFile() + "'" +
            ", ord=" + getOrd() +
            ", active='" + getActive() + "'" +
            ", parent=" + getParent() +
            ", names=" + getNames() +
            "}";
    }
}
