package uz.paykap.service.dto;

import java.io.Serializable;
import java.util.Objects;
import javax.validation.constraints.*;
import uz.devops.bmcore.enumeration.RequestType;
import uz.paykap.domain.enumeration.MethodType;
import uz.paykap.domain.enumeration.ModeType;
import uz.paykap.domain.enumeration.ProtocolType;

/**
 * A DTO for the {@link uz.paykap.domain.GatewayParam} entity.
 */
public class GatewayParamDTO implements Serializable {

    private Long id;

    @Size(max = 512)
    private String url;

    private RequestType type;

    @Size(max = 32)
    private String username;

    @Size(max = 64)
    private String password;

    @Size(max = 128)
    private String token;

    @Size(max = 2048)
    private String body;

    @Size(max = 256)
    private String statusQuery;

    @Size(max = 256)
    private String messageQuery;

    @Size(max = 32)
    private String successCode;

    @Size(max = 512)
    private String callback;

    private Integer timeout;

    @Size(max = 1024)
    private String headers;

    @Size(max = 1024)
    private String dataQueries;

    private MethodType httpMethodType;

    private ProtocolType protocol;

    @Size(max = 64)
    private String requestMediaType;

    @Size(max = 64)
    private String responseMediaType;

    @NotNull
    private Boolean active;

    private ModeType mode;

    private String extIdQuery;

    private Boolean successOn2xx;

    private RecipientDTO recipient;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public RequestType getType() {
        return type;
    }

    public void setType(RequestType type) {
        this.type = type;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getStatusQuery() {
        return statusQuery;
    }

    public void setStatusQuery(String statusQuery) {
        this.statusQuery = statusQuery;
    }

    public String getMessageQuery() {
        return messageQuery;
    }

    public void setMessageQuery(String messageQuery) {
        this.messageQuery = messageQuery;
    }

    public String getSuccessCode() {
        return successCode;
    }

    public void setSuccessCode(String successCode) {
        this.successCode = successCode;
    }

    public String getCallback() {
        return callback;
    }

    public void setCallback(String callback) {
        this.callback = callback;
    }

    public Integer getTimeout() {
        return timeout;
    }

    public void setTimeout(Integer timeout) {
        this.timeout = timeout;
    }

    public String getHeaders() {
        return headers;
    }

    public void setHeaders(String headers) {
        this.headers = headers;
    }

    public String getDataQueries() {
        return dataQueries;
    }

    public void setDataQueries(String dataQueries) {
        this.dataQueries = dataQueries;
    }

    public MethodType getHttpMethodType() {
        return httpMethodType;
    }

    public void setHttpMethodType(MethodType httpMethodType) {
        this.httpMethodType = httpMethodType;
    }

    public ProtocolType getProtocol() {
        return protocol;
    }

    public void setProtocol(ProtocolType protocol) {
        this.protocol = protocol;
    }

    public String getRequestMediaType() {
        return requestMediaType;
    }

    public void setRequestMediaType(String requestMediaType) {
        this.requestMediaType = requestMediaType;
    }

    public String getResponseMediaType() {
        return responseMediaType;
    }

    public void setResponseMediaType(String responseMediaType) {
        this.responseMediaType = responseMediaType;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public ModeType getMode() {
        return mode;
    }

    public void setMode(ModeType mode) {
        this.mode = mode;
    }

    public String getExtIdQuery() {
        return extIdQuery;
    }

    public void setExtIdQuery(String extIdQuery) {
        this.extIdQuery = extIdQuery;
    }

    public Boolean getSuccessOn2xx() {
        return successOn2xx;
    }

    public void setSuccessOn2xx(Boolean successOn2xx) {
        this.successOn2xx = successOn2xx;
    }

    public RecipientDTO getRecipient() {
        return recipient;
    }

    public void setRecipient(RecipientDTO recipient) {
        this.recipient = recipient;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof GatewayParamDTO)) {
            return false;
        }

        GatewayParamDTO gatewayParamDTO = (GatewayParamDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, gatewayParamDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "GatewayParamDTO{" +
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
            ", recipient=" + getRecipient() +
            "}";
    }
}
