package uz.paykap.service.dto;

import io.github.jhipster.service.filter.Filter;
import java.io.Serializable;
import tech.jhipster.service.filter.LongFilter;
import tech.jhipster.service.filter.StringFilter;

/**
 * Criteria class for the AgentParams entity. This class is used in AgentParamsResource to
 * receive all the possible filtering options from the Http GET request parameters.
 * For example the following could be a valid requests:
 * <code> /agent-params?id.greaterThan=5&amp;attr1.contains=something&amp;attr2.specified=false</code>
 * As Spring is unable to properly convert the types, unless specific {@link Filter} class are used, we need to use
 * fix type specific filters.
 */
public class AgentParamsCriteria implements Serializable {

    private static final long serialVersionUID = 1L;

    private LongFilter id;

    private StringFilter callBackUrl;

    private StringFilter username;

    private StringFilter password;

    private LongFilter agentId;

    public AgentParamsCriteria() {}

    public LongFilter getId() {
        return id;
    }

    public void setId(LongFilter id) {
        this.id = id;
    }

    public StringFilter getCallBackUrl() {
        return callBackUrl;
    }

    public void setCallBackUrl(StringFilter callBackUrl) {
        this.callBackUrl = callBackUrl;
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

    public LongFilter getAgentId() {
        return agentId;
    }

    public void setAgentId(LongFilter agentId) {
        this.agentId = agentId;
    }

    @Override
    public String toString() {
        return (
            "AgentParamsCriteria{" +
            (id != null ? "id=" + id + ", " : "") +
            (callBackUrl != null ? "callBackUrl=" + callBackUrl + ", " : "") +
            (username != null ? "username=" + username + ", " : "") +
            (password != null ? "password=" + password + ", " : "") +
            (agentId != null ? "agentId=" + agentId + ", " : "") +
            "}"
        );
    }
}
