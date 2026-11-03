package uz.paykap.service.criteria;

import java.io.Serializable;
import java.util.Objects;
import tech.jhipster.service.Criteria;
import tech.jhipster.service.filter.BooleanFilter;
import tech.jhipster.service.filter.Filter;
import tech.jhipster.service.filter.IntegerFilter;
import tech.jhipster.service.filter.LongFilter;
import tech.jhipster.service.filter.StringFilter;
import uz.devops.bmcore.enumeration.RequestType;
import uz.paykap.domain.enumeration.MethodType;
import uz.paykap.domain.enumeration.ModeType;
import uz.paykap.domain.enumeration.ProtocolType;

/**
 * Criteria class for the {@link uz.paykap.domain.GatewayParam} entity. This class is used
 * in {@link uz.paykap.web.rest.GatewayParamResource} to receive all the possible filtering options from
 * the Http GET request parameters.
 * For example the following could be a valid request:
 * {@code /gateway-params?id.greaterThan=5&attr1.contains=something&attr2.specified=false}
 * As Spring is unable to properly convert the types, unless specific {@link Filter} class are used, we need to use
 * fix type specific filters.
 */
public class GatewayParamCriteria implements Serializable, Criteria {

    /**
     * Class for filtering RequestType
     */
    public static class RequestTypeFilter extends Filter<RequestType> {

        public RequestTypeFilter() {}

        public RequestTypeFilter(RequestTypeFilter filter) {
            super(filter);
        }

        @Override
        public RequestTypeFilter copy() {
            return new RequestTypeFilter(this);
        }
    }

    /**
     * Class for filtering MethodType
     */
    public static class MethodTypeFilter extends Filter<MethodType> {

        public MethodTypeFilter() {}

        public MethodTypeFilter(MethodTypeFilter filter) {
            super(filter);
        }

        @Override
        public MethodTypeFilter copy() {
            return new MethodTypeFilter(this);
        }
    }

    /**
     * Class for filtering ProtocolType
     */
    public static class ProtocolTypeFilter extends Filter<ProtocolType> {

        public ProtocolTypeFilter() {}

        public ProtocolTypeFilter(ProtocolTypeFilter filter) {
            super(filter);
        }

        @Override
        public ProtocolTypeFilter copy() {
            return new ProtocolTypeFilter(this);
        }
    }

    /**
     * Class for filtering ModeType
     */
    public static class ModeTypeFilter extends Filter<ModeType> {

        public ModeTypeFilter() {}

        public ModeTypeFilter(ModeTypeFilter filter) {
            super(filter);
        }

        @Override
        public ModeTypeFilter copy() {
            return new ModeTypeFilter(this);
        }
    }

    private static final long serialVersionUID = 1L;

    private LongFilter id;

    private StringFilter url;

    private RequestTypeFilter type;

    private StringFilter username;

    private StringFilter password;

    private StringFilter token;

    private StringFilter body;

    private StringFilter statusQuery;

    private StringFilter messageQuery;

    private StringFilter successCode;

    private StringFilter callback;

    private IntegerFilter timeout;

    private StringFilter headers;

    private StringFilter dataQueries;

    private MethodTypeFilter httpMethodType;

    private ProtocolTypeFilter protocol;

    private StringFilter requestMediaType;

    private StringFilter responseMediaType;

    private BooleanFilter active;

    private ModeTypeFilter mode;

    private StringFilter extIdQuery;

    private BooleanFilter successOn2xx;

    private LongFilter recipientId;

    public GatewayParamCriteria() {}

    public GatewayParamCriteria(GatewayParamCriteria other) {
        this.id = other.id == null ? null : other.id.copy();
        this.url = other.url == null ? null : other.url.copy();
        this.type = other.type == null ? null : other.type.copy();
        this.username = other.username == null ? null : other.username.copy();
        this.password = other.password == null ? null : other.password.copy();
        this.token = other.token == null ? null : other.token.copy();
        this.body = other.body == null ? null : other.body.copy();
        this.statusQuery = other.statusQuery == null ? null : other.statusQuery.copy();
        this.messageQuery = other.messageQuery == null ? null : other.messageQuery.copy();
        this.successCode = other.successCode == null ? null : other.successCode.copy();
        this.callback = other.callback == null ? null : other.callback.copy();
        this.timeout = other.timeout == null ? null : other.timeout.copy();
        this.headers = other.headers == null ? null : other.headers.copy();
        this.dataQueries = other.dataQueries == null ? null : other.dataQueries.copy();
        this.httpMethodType = other.httpMethodType == null ? null : other.httpMethodType.copy();
        this.protocol = other.protocol == null ? null : other.protocol.copy();
        this.requestMediaType = other.requestMediaType == null ? null : other.requestMediaType.copy();
        this.responseMediaType = other.responseMediaType == null ? null : other.responseMediaType.copy();
        this.active = other.active == null ? null : other.active.copy();
        this.mode = other.mode == null ? null : other.mode.copy();
        this.extIdQuery = other.extIdQuery == null ? null : other.extIdQuery.copy();
        this.successOn2xx = other.successOn2xx == null ? null : other.successOn2xx.copy();
        this.recipientId = other.recipientId == null ? null : other.recipientId.copy();
    }

    @Override
    public GatewayParamCriteria copy() {
        return new GatewayParamCriteria(this);
    }

    public LongFilter getId() {
        return id;
    }

    public LongFilter id() {
        if (id == null) {
            id = new LongFilter();
        }
        return id;
    }

    public void setId(LongFilter id) {
        this.id = id;
    }

    public StringFilter getUrl() {
        return url;
    }

    public StringFilter url() {
        if (url == null) {
            url = new StringFilter();
        }
        return url;
    }

    public void setUrl(StringFilter url) {
        this.url = url;
    }

    public RequestTypeFilter getType() {
        return type;
    }

    public RequestTypeFilter type() {
        if (type == null) {
            type = new RequestTypeFilter();
        }
        return type;
    }

    public void setType(RequestTypeFilter type) {
        this.type = type;
    }

    public StringFilter getUsername() {
        return username;
    }

    public StringFilter username() {
        if (username == null) {
            username = new StringFilter();
        }
        return username;
    }

    public void setUsername(StringFilter username) {
        this.username = username;
    }

    public StringFilter getPassword() {
        return password;
    }

    public StringFilter password() {
        if (password == null) {
            password = new StringFilter();
        }
        return password;
    }

    public void setPassword(StringFilter password) {
        this.password = password;
    }

    public StringFilter getToken() {
        return token;
    }

    public StringFilter token() {
        if (token == null) {
            token = new StringFilter();
        }
        return token;
    }

    public void setToken(StringFilter token) {
        this.token = token;
    }

    public StringFilter getBody() {
        return body;
    }

    public StringFilter body() {
        if (body == null) {
            body = new StringFilter();
        }
        return body;
    }

    public void setBody(StringFilter body) {
        this.body = body;
    }

    public StringFilter getStatusQuery() {
        return statusQuery;
    }

    public StringFilter statusQuery() {
        if (statusQuery == null) {
            statusQuery = new StringFilter();
        }
        return statusQuery;
    }

    public void setStatusQuery(StringFilter statusQuery) {
        this.statusQuery = statusQuery;
    }

    public StringFilter getMessageQuery() {
        return messageQuery;
    }

    public StringFilter messageQuery() {
        if (messageQuery == null) {
            messageQuery = new StringFilter();
        }
        return messageQuery;
    }

    public void setMessageQuery(StringFilter messageQuery) {
        this.messageQuery = messageQuery;
    }

    public StringFilter getSuccessCode() {
        return successCode;
    }

    public StringFilter successCode() {
        if (successCode == null) {
            successCode = new StringFilter();
        }
        return successCode;
    }

    public void setSuccessCode(StringFilter successCode) {
        this.successCode = successCode;
    }

    public StringFilter getCallback() {
        return callback;
    }

    public StringFilter callback() {
        if (callback == null) {
            callback = new StringFilter();
        }
        return callback;
    }

    public void setCallback(StringFilter callback) {
        this.callback = callback;
    }

    public IntegerFilter getTimeout() {
        return timeout;
    }

    public IntegerFilter timeout() {
        if (timeout == null) {
            timeout = new IntegerFilter();
        }
        return timeout;
    }

    public void setTimeout(IntegerFilter timeout) {
        this.timeout = timeout;
    }

    public StringFilter getHeaders() {
        return headers;
    }

    public StringFilter headers() {
        if (headers == null) {
            headers = new StringFilter();
        }
        return headers;
    }

    public void setHeaders(StringFilter headers) {
        this.headers = headers;
    }

    public StringFilter getDataQueries() {
        return dataQueries;
    }

    public StringFilter dataQueries() {
        if (dataQueries == null) {
            dataQueries = new StringFilter();
        }
        return dataQueries;
    }

    public void setDataQueries(StringFilter dataQueries) {
        this.dataQueries = dataQueries;
    }

    public MethodTypeFilter getHttpMethodType() {
        return httpMethodType;
    }

    public MethodTypeFilter httpMethodType() {
        if (httpMethodType == null) {
            httpMethodType = new MethodTypeFilter();
        }
        return httpMethodType;
    }

    public void setHttpMethodType(MethodTypeFilter httpMethodType) {
        this.httpMethodType = httpMethodType;
    }

    public ProtocolTypeFilter getProtocol() {
        return protocol;
    }

    public ProtocolTypeFilter protocol() {
        if (protocol == null) {
            protocol = new ProtocolTypeFilter();
        }
        return protocol;
    }

    public void setProtocol(ProtocolTypeFilter protocol) {
        this.protocol = protocol;
    }

    public StringFilter getRequestMediaType() {
        return requestMediaType;
    }

    public StringFilter requestMediaType() {
        if (requestMediaType == null) {
            requestMediaType = new StringFilter();
        }
        return requestMediaType;
    }

    public void setRequestMediaType(StringFilter requestMediaType) {
        this.requestMediaType = requestMediaType;
    }

    public StringFilter getResponseMediaType() {
        return responseMediaType;
    }

    public StringFilter responseMediaType() {
        if (responseMediaType == null) {
            responseMediaType = new StringFilter();
        }
        return responseMediaType;
    }

    public void setResponseMediaType(StringFilter responseMediaType) {
        this.responseMediaType = responseMediaType;
    }

    public BooleanFilter getActive() {
        return active;
    }

    public BooleanFilter active() {
        if (active == null) {
            active = new BooleanFilter();
        }
        return active;
    }

    public void setActive(BooleanFilter active) {
        this.active = active;
    }

    public ModeTypeFilter getMode() {
        return mode;
    }

    public ModeTypeFilter mode() {
        if (mode == null) {
            mode = new ModeTypeFilter();
        }
        return mode;
    }

    public void setMode(ModeTypeFilter mode) {
        this.mode = mode;
    }

    public StringFilter getExtIdQuery() {
        return extIdQuery;
    }

    public StringFilter extIdQuery() {
        if (extIdQuery == null) {
            extIdQuery = new StringFilter();
        }
        return extIdQuery;
    }

    public void setExtIdQuery(StringFilter extIdQuery) {
        this.extIdQuery = extIdQuery;
    }

    public BooleanFilter getSuccessOn2xx() {
        return successOn2xx;
    }

    public BooleanFilter successOn2xx() {
        if (successOn2xx == null) {
            successOn2xx = new BooleanFilter();
        }
        return successOn2xx;
    }

    public void setSuccessOn2xx(BooleanFilter successOn2xx) {
        this.successOn2xx = successOn2xx;
    }

    public LongFilter getRecipientId() {
        return recipientId;
    }

    public LongFilter recipientId() {
        if (recipientId == null) {
            recipientId = new LongFilter();
        }
        return recipientId;
    }

    public void setRecipientId(LongFilter recipientId) {
        this.recipientId = recipientId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final GatewayParamCriteria that = (GatewayParamCriteria) o;
        return (
            Objects.equals(id, that.id) &&
            Objects.equals(url, that.url) &&
            Objects.equals(type, that.type) &&
            Objects.equals(username, that.username) &&
            Objects.equals(password, that.password) &&
            Objects.equals(token, that.token) &&
            Objects.equals(body, that.body) &&
            Objects.equals(statusQuery, that.statusQuery) &&
            Objects.equals(messageQuery, that.messageQuery) &&
            Objects.equals(successCode, that.successCode) &&
            Objects.equals(callback, that.callback) &&
            Objects.equals(timeout, that.timeout) &&
            Objects.equals(headers, that.headers) &&
            Objects.equals(dataQueries, that.dataQueries) &&
            Objects.equals(httpMethodType, that.httpMethodType) &&
            Objects.equals(protocol, that.protocol) &&
            Objects.equals(requestMediaType, that.requestMediaType) &&
            Objects.equals(responseMediaType, that.responseMediaType) &&
            Objects.equals(active, that.active) &&
            Objects.equals(mode, that.mode) &&
            Objects.equals(extIdQuery, that.extIdQuery) &&
            Objects.equals(successOn2xx, that.successOn2xx) &&
            Objects.equals(recipientId, that.recipientId)
        );
    }

    @Override
    public int hashCode() {
        return Objects.hash(
            id,
            url,
            type,
            username,
            password,
            token,
            body,
            statusQuery,
            messageQuery,
            successCode,
            callback,
            timeout,
            headers,
            dataQueries,
            httpMethodType,
            protocol,
            requestMediaType,
            responseMediaType,
            active,
            mode,
            extIdQuery,
            successOn2xx,
            recipientId
        );
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "GatewayParamCriteria{" +
            (id != null ? "id=" + id + ", " : "") +
            (url != null ? "url=" + url + ", " : "") +
            (type != null ? "type=" + type + ", " : "") +
            (username != null ? "username=" + username + ", " : "") +
            (password != null ? "password=" + password + ", " : "") +
            (token != null ? "token=" + token + ", " : "") +
            (body != null ? "body=" + body + ", " : "") +
            (statusQuery != null ? "statusQuery=" + statusQuery + ", " : "") +
            (messageQuery != null ? "messageQuery=" + messageQuery + ", " : "") +
            (successCode != null ? "successCode=" + successCode + ", " : "") +
            (callback != null ? "callback=" + callback + ", " : "") +
            (timeout != null ? "timeout=" + timeout + ", " : "") +
            (headers != null ? "headers=" + headers + ", " : "") +
            (dataQueries != null ? "dataQueries=" + dataQueries + ", " : "") +
            (httpMethodType != null ? "httpMethodType=" + httpMethodType + ", " : "") +
            (protocol != null ? "protocol=" + protocol + ", " : "") +
            (requestMediaType != null ? "requestMediaType=" + requestMediaType + ", " : "") +
            (responseMediaType != null ? "responseMediaType=" + responseMediaType + ", " : "") +
            (active != null ? "active=" + active + ", " : "") +
            (mode != null ? "mode=" + mode + ", " : "") +
            (extIdQuery != null ? "extIdQuery=" + extIdQuery + ", " : "") +
            (successOn2xx != null ? "successOn2xx=" + successOn2xx + ", " : "") +
            (recipientId != null ? "recipientId=" + recipientId + ", " : "") +
            "}";
    }
}
