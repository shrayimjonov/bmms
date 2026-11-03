package uz.paykap.service.dto;

import io.github.jhipster.service.filter.*;
import uz.devops.bmcore.enumeration.RequestType;
import uz.paykap.domain.enumeration.MethodType;
import uz.paykap.domain.enumeration.ModeType;
import uz.paykap.domain.enumeration.ProtocolType;

import java.io.Serializable;


/**
 * Criteria class for the GatewayParam entity. This class is used in GatewayParamResource to
 * receive all the possible filtering options from the Http GET request parameters.
 * For example the following could be a valid requests:
 * <code> /gateway-params?id.greaterThan=5&amp;attr1.contains=something&amp;attr2.specified=false</code>
 * As Spring is unable to properly convert the types, unless specific {@link Filter} class are used, we need to use
 * fix type specific filters.
 */
public class GatewayParamCriteria implements Serializable {
    /**
     * Class for filtering RequestType
     */
    public static class RequestTypeFilter extends Filter<RequestType> {
    }

    /**
     * Class for filtering MethodType
     */
    public static class MethodTypeFilter extends Filter<MethodType> {
    }

    /**
     * Class for filtering ProtocolType
     */
    public static class ProtocolTypeFilter extends Filter<ProtocolType> {
    }

    /**
     * Class for filtering ModeType
     */
    public static class ModeTypeFilter extends Filter<ModeType> {
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

    public GatewayParamCriteria() {
    }

    public LongFilter getId() {
        return id;
    }

    public void setId(LongFilter id) {
        this.id = id;
    }

    public StringFilter getUrl() {
        return url;
    }

    public void setUrl(StringFilter url) {
        this.url = url;
    }

    public RequestTypeFilter getType() {
        return type;
    }

    public void setType(RequestTypeFilter type) {
        this.type = type;
    }

    public StringFilter getUsername() {
        return username;
    }

    public void setUsername(StringFilter username) {
        this.username = username;
    }

    public StringFilter getPassword() {
        return password;
    }

    public void setPassword(StringFilter password) {
        this.password = password;
    }

    public StringFilter getToken() {
        return token;
    }

    public void setToken(StringFilter token) {
        this.token = token;
    }

    public StringFilter getBody() {
        return body;
    }

    public void setBody(StringFilter body) {
        this.body = body;
    }

    public StringFilter getStatusQuery() {
        return statusQuery;
    }

    public void setStatusQuery(StringFilter statusQuery) {
        this.statusQuery = statusQuery;
    }

    public StringFilter getMessageQuery() {
        return messageQuery;
    }

    public void setMessageQuery(StringFilter messageQuery) {
        this.messageQuery = messageQuery;
    }

    public StringFilter getSuccessCode() {
        return successCode;
    }

    public void setSuccessCode(StringFilter successCode) {
        this.successCode = successCode;
    }

    public StringFilter getCallback() {
        return callback;
    }

    public void setCallback(StringFilter callback) {
        this.callback = callback;
    }

    public IntegerFilter getTimeout() {
        return timeout;
    }

    public void setTimeout(IntegerFilter timeout) {
        this.timeout = timeout;
    }

    public StringFilter getHeaders() {
        return headers;
    }

    public void setHeaders(StringFilter headers) {
        this.headers = headers;
    }

    public StringFilter getDataQueries() {
        return dataQueries;
    }

    public void setDataQueries(StringFilter dataQueries) {
        this.dataQueries = dataQueries;
    }

    public MethodTypeFilter getHttpMethodType() {
        return httpMethodType;
    }

    public void setHttpMethodType(MethodTypeFilter httpMethodType) {
        this.httpMethodType = httpMethodType;
    }

    public ProtocolTypeFilter getProtocol() {
        return protocol;
    }

    public void setProtocol(ProtocolTypeFilter protocol) {
        this.protocol = protocol;
    }

    public StringFilter getRequestMediaType() {
        return requestMediaType;
    }

    public void setRequestMediaType(StringFilter requestMediaType) {
        this.requestMediaType = requestMediaType;
    }

    public StringFilter getResponseMediaType() {
        return responseMediaType;
    }

    public void setResponseMediaType(StringFilter responseMediaType) {
        this.responseMediaType = responseMediaType;
    }

    public BooleanFilter getActive() {
        return active;
    }

    public void setActive(BooleanFilter active) {
        this.active = active;
    }

    public ModeTypeFilter getMode() {
        return mode;
    }

    public void setMode(ModeTypeFilter mode) {
        this.mode = mode;
    }

    public StringFilter getExtIdQuery() {
        return extIdQuery;
    }

    public void setExtIdQuery(StringFilter extIdQuery) {
        this.extIdQuery = extIdQuery;
    }

    public BooleanFilter getSuccessOn2xx() {
        return successOn2xx;
    }

    public void setSuccessOn2xx(BooleanFilter successOn2xx) {
        this.successOn2xx = successOn2xx;
    }

    public LongFilter getRecipientId() {
        return recipientId;
    }

    public void setRecipientId(LongFilter recipientId) {
        this.recipientId = recipientId;
    }

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
