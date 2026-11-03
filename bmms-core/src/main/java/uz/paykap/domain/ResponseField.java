package uz.paykap.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import uz.devops.bmcore.enumeration.RequestType;
import uz.devops.bmcore.enumeration.Visibility;
import uz.paykap.domain.enumeration.ResponseFieldType;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * A ResponseField.
 */
@Entity
@Table(name = "response_field")
public class ResponseField extends AbstractAuditingEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    private Long id;

    /**
     * Response field name; account_id, phone, login, ...
     */
    @NotNull
    @Size(max = 128)
    @Column(name = "name", length = 128, nullable = false)
    private String name;

    /**
     * Visible in UI, can be used for hidden static fields
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "visibility")
    private Visibility visibility;

    /**
     * Display order
     */
    @Column(name = "ord")
    private Integer ord;

    /**
     * Define to which of CHECK,PAY,STATUS,CANCEL,DEPOSIT,RECONCILE,CREDIT,DEBIT,P2P_CREDIT,P2P_DEBIT belongs that response field
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "request_type")
    private RequestType requestType;

    /**
     * Тип
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "type")
    private ResponseFieldType type;

    /**
     * Доля суммы, 1 - сум, 100 - тийин
     */
    @Column(name = "amount_ratio")
    private Integer amountRatio;

    @ManyToOne(optional = false)
    @NotNull
    @JsonIgnoreProperties(value = { "gateway", "category", "titles", "fullNames" }, allowSetters = true)
    private Recipient recipient;

    @ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @NotNull
    @JoinTable(
        name = "rel_response_field__label",
        joinColumns = @JoinColumn(name = "response_field_id"),
        inverseJoinColumns = @JoinColumn(name = "label_id")
    )
    private Set<I18n> labels = new HashSet<>();

    // jhipster-needle-entity-add-field - JHipster will add fields here
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ResponseField id(Long id) {
        this.id = id;
        return this;
    }

    public String getName() {
        return this.name;
    }

    public ResponseField name(String name) {
        this.name = name;
        return this;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Visibility getVisibility() {
        return this.visibility;
    }

    public ResponseField visibility(Visibility visibility) {
        this.visibility = visibility;
        return this;
    }

    public void setVisibility(Visibility visibility) {
        this.visibility = visibility;
    }

    public Integer getOrd() {
        return this.ord;
    }

    public ResponseField ord(Integer ord) {
        this.ord = ord;
        return this;
    }

    public void setOrd(Integer ord) {
        this.ord = ord;
    }

    public RequestType getRequestType() {
        return this.requestType;
    }

    public ResponseField requestType(RequestType requestType) {
        this.requestType = requestType;
        return this;
    }

    public void setRequestType(RequestType requestType) {
        this.requestType = requestType;
    }

    public ResponseFieldType getType() {
        return this.type;
    }

    public ResponseField type(ResponseFieldType type) {
        this.type = type;
        return this;
    }

    public void setType(ResponseFieldType type) {
        this.type = type;
    }

    public Integer getAmountRatio() {
        return this.amountRatio;
    }

    public ResponseField amountRatio(Integer amountRatio) {
        this.amountRatio = amountRatio;
        return this;
    }

    public void setAmountRatio(Integer amountRatio) {
        this.amountRatio = amountRatio;
    }

    public Recipient getRecipient() {
        return this.recipient;
    }

    public ResponseField recipient(Recipient recipient) {
        this.setRecipient(recipient);
        return this;
    }

    public void setRecipient(Recipient recipient) {
        this.recipient = recipient;
    }

    public Set<I18n> getLabels() {
        return this.labels;
    }

    public ResponseField labels(Set<I18n> i18ns) {
        this.setLabels(i18ns);
        return this;
    }

    public ResponseField addLabel(I18n i18n) {
        this.labels.add(i18n);
        return this;
    }

    public ResponseField removeLabel(I18n i18n) {
        this.labels.remove(i18n);
        return this;
    }

    public void setLabels(Set<I18n> i18ns) {
        this.labels = i18ns;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ResponseField)) {
            return false;
        }
        return id != null && id.equals(((ResponseField) o).id);
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "ResponseField{" +
            "id=" + getId() +
            ", name='" + getName() + "'" +
            ", visibility='" + getVisibility() + "'" +
            ", ord=" + getOrd() +
            ", requestType='" + getRequestType() + "'" +
            ", type='" + getType() + "'" +
            ", amountRatio=" + getAmountRatio() +
            "}";
    }
}
