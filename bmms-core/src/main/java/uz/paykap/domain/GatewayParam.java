package uz.paykap.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.io.Serializable;
import javax.persistence.*;
import javax.validation.constraints.*;
import uz.devops.bmcore.enumeration.RequestType;
import uz.paykap.domain.enumeration.MethodType;
import uz.paykap.domain.enumeration.ModeType;
import uz.paykap.domain.enumeration.ProtocolType;

/**
 * A GatewayParam.
 */
@Entity
@Table(name = "gateway_param")
public class GatewayParam extends AbstractAuditingEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    private Long id;

    @Size(max = 512)
    @Column(name = "url", length = 512)
    private String url;

    @Enumerated(EnumType.STRING)
    @Column(name = "type")
    private RequestType type;

    @Size(max = 32)
    @Column(name = "username", length = 32)
    private String username;

    @Size(max = 64)
    @Column(name = "password", length = 64)
    private String password;

    @Size(max = 128)
    @Column(name = "token", length = 128)
    private String token;

    @Size(max = 2048)
    @Column(name = "body", length = 2048)
    private String body;

    @Size(max = 256)
    @Column(name = "status_query", length = 256)
    private String statusQuery;

    @Size(max = 256)
    @Column(name = "message_query", length = 256)
    private String messageQuery;

    @Size(max = 32)
    @Column(name = "success_code", length = 32)
    private String successCode;

    @Size(max = 512)
    @Column(name = "callback", length = 512)
    private String callback;

    @Column(name = "timeout")
    private Integer timeout;

    @Size(max = 1024)
    @Column(name = "headers", length = 1024)
    private String headers;

    @Size(max = 1024)
    @Column(name = "data_queries", length = 1024)
    private String dataQueries;

    @Enumerated(EnumType.STRING)
    @Column(name = "http_method_type")
    private MethodType httpMethodType;

    @Enumerated(EnumType.STRING)
    @Column(name = "protocol")
    private ProtocolType protocol;

    @Size(max = 64)
    @Column(name = "request_media_type", length = 64)
    private String requestMediaType = "application/json";

    @Size(max = 64)
    @Column(name = "response_media_type", length = 64)
    private String responseMediaType = "application/json";

    @NotNull
    @Column(name = "active", nullable = false)
    private Boolean active;

    @Enumerated(EnumType.STRING)
    @Column(name = "mode")
    private ModeType mode;

    @Column(name = "ext_id_query")
    private String extIdQuery;

    @Column(name = "success_on_2_xx", nullable = false)
    private Boolean successOn2xx;

    @ManyToOne
    @JsonIgnoreProperties(value = { "gateway", "category", "titles", "fullNames" }, allowSetters = true)
    private Recipient recipient;

    // jhipster-needle-entity-add-field - JHipster will add fields here
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public GatewayParam id(Long id) {
        this.id = id;
        return this;
    }

    public String getUrl() {
        return this.url;
    }

    public GatewayParam url(String url) {
        this.url = url;
        return this;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public RequestType getType() {
        return this.type;
    }

    public GatewayParam type(RequestType type) {
        this.type = type;
        return this;
    }

    public void setType(RequestType type) {
        this.type = type;
    }

    public String getUsername() {
        return this.username;
    }

    public GatewayParam username(String username) {
        this.username = username;
        return this;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return this.password;
    }

    public GatewayParam password(String password) {
        this.password = password;
        return this;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getToken() {
        return this.token;
    }

    public GatewayParam token(String token) {
        this.token = token;
        return this;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getBody() {
        return this.body;
    }

    public GatewayParam body(String body) {
        this.body = body;
        return this;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getStatusQuery() {
        return this.statusQuery;
    }

    public GatewayParam statusQuery(String statusQuery) {
        this.statusQuery = statusQuery;
        return this;
    }

    public void setStatusQuery(String statusQuery) {
        this.statusQuery = statusQuery;
    }

    public String getMessageQuery() {
        return this.messageQuery;
    }

    public GatewayParam messageQuery(String messageQuery) {
        this.messageQuery = messageQuery;
        return this;
    }

    public void setMessageQuery(String messageQuery) {
        this.messageQuery = messageQuery;
    }

    public String getSuccessCode() {
        return this.successCode;
    }

    public GatewayParam successCode(String successCode) {
        this.successCode = successCode;
        return this;
    }

    public void setSuccessCode(String successCode) {
        this.successCode = successCode;
    }

    public String getCallback() {
        return this.callback;
    }

    public GatewayParam callback(String callback) {
        this.callback = callback;
        return this;
    }

    public void setCallback(String callback) {
        this.callback = callback;
    }

    public Integer getTimeout() {
        return this.timeout;
    }

    public GatewayParam timeout(Integer timeout) {
        this.timeout = timeout;
        return this;
    }

    public void setTimeout(Integer timeout) {
        this.timeout = timeout;
    }

    public String getHeaders() {
        return this.headers;
    }

    public GatewayParam headers(String headers) {
        this.headers = headers;
        return this;
    }

    public void setHeaders(String headers) {
        this.headers = headers;
    }

    public String getDataQueries() {
        return this.dataQueries;
    }

    public GatewayParam dataQueries(String dataQueries) {
        this.dataQueries = dataQueries;
        return this;
    }

    public void setDataQueries(String dataQueries) {
        this.dataQueries = dataQueries;
    }

    public MethodType getHttpMethodType() {
        return this.httpMethodType;
    }

    public GatewayParam httpMethodType(MethodType httpMethodType) {
        this.httpMethodType = httpMethodType;
        return this;
    }

    public void setHttpMethodType(MethodType httpMethodType) {
        this.httpMethodType = httpMethodType;
    }

    public ProtocolType getProtocol() {
        return this.protocol;
    }

    public GatewayParam protocol(ProtocolType protocol) {
        this.protocol = protocol;
        return this;
    }

    public void setProtocol(ProtocolType protocol) {
        this.protocol = protocol;
    }

    public String getRequestMediaType() {
        return this.requestMediaType;
    }

    public GatewayParam requestMediaType(String requestMediaType) {
        this.requestMediaType = requestMediaType;
        return this;
    }

    public void setRequestMediaType(String requestMediaType) {
        this.requestMediaType = requestMediaType;
    }

    public String getResponseMediaType() {
        return this.responseMediaType;
    }

    public GatewayParam responseMediaType(String responseMediaType) {
        this.responseMediaType = responseMediaType;
        return this;
    }

    public void setResponseMediaType(String responseMediaType) {
        this.responseMediaType = responseMediaType;
    }

    public Boolean isActive() {
        return this.active;
    }

    public Boolean getActive() {
        return this.active;
    }

    public GatewayParam active(Boolean active) {
        this.active = active;
        return this;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public ModeType getMode() {
        return this.mode;
    }

    public GatewayParam mode(ModeType mode) {
        this.mode = mode;
        return this;
    }

    public void setMode(ModeType mode) {
        this.mode = mode;
    }

    public String getExtIdQuery() {
        return this.extIdQuery;
    }

    public GatewayParam extIdQuery(String extIdQuery) {
        this.extIdQuery = extIdQuery;
        return this;
    }

    public void setExtIdQuery(String extIdQuery) {
        this.extIdQuery = extIdQuery;
    }

    public Boolean isSuccessOn2xx() {
        return successOn2xx;
    }

    public Boolean getSuccessOn2xx() {
        return this.successOn2xx;
    }

    public GatewayParam successOn2xx(Boolean successOn2xx) {
        this.successOn2xx = successOn2xx;
        return this;
    }

    public void setSuccessOn2xx(Boolean successOn2xx) {
        this.successOn2xx = successOn2xx;
    }

    public Recipient getRecipient() {
        return this.recipient;
    }

    public GatewayParam recipient(Recipient recipient) {
        this.setRecipient(recipient);
        return this;
    }

    public void setRecipient(Recipient recipient) {
        this.recipient = recipient;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof GatewayParam)) {
            return false;
        }
        return id != null && id.equals(((GatewayParam) o).id);
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "GatewayParam{" +
            "id=" + getId() +
            ", url='" + getUrl() + "'" +
            ", type='" + getType() + "'" +
            ", username='" + getUsername() + "'" +
            ", password=''" +
            ", token='" + getToken() + "'" +
            ", body='" + getBody() + "'" +
            ", statusQuery='" + getStatusQuery() + "'" +
            ", messageQuery='" + getMessageQuery() + "'" +
            ", successCode='" + getSuccessCode() + "'" +
            ", callback='" + getCallback() + "'" +
            ", timeout=" + getTimeout() +
            ", headers='" + getHeaders() + "'" +
            ", dataQueries='" + getDataQueries() + "'" +
            ", httpMethodType='" + getHttpMethodType() + "'" +
            ", protocol='" + getProtocol() + "'" +
            ", requestMediaType='" + getRequestMediaType() + "'" +
            ", responseMediaType='" + getResponseMediaType() + "'" +
            ", active='" + getActive() + "'" +
            ", mode='" + getMode() + "'" +
            ", extIdQuery='" + getExtIdQuery() + "'" +
            ", successOn2xx='" + getSuccessOn2xx() + "'" +
            "}";
    }
}
