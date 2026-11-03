package uz.paykap.service.criteria;

import java.io.Serializable;
import java.util.Objects;
import tech.jhipster.service.Criteria;
import tech.jhipster.service.filter.BooleanFilter;
import tech.jhipster.service.filter.DoubleFilter;
import tech.jhipster.service.filter.Filter;
import tech.jhipster.service.filter.FloatFilter;
import tech.jhipster.service.filter.IntegerFilter;
import tech.jhipster.service.filter.LongFilter;
import tech.jhipster.service.filter.StringFilter;

/**
 * Criteria class for the {@link uz.paykap.domain.AgentParams} entity. This class is used
 * in {@link uz.paykap.web.rest.AgentParamsResource} to receive all the possible filtering options from
 * the Http GET request parameters.
 * For example the following could be a valid request:
 * {@code /agent-params?id.greaterThan=5&attr1.contains=something&attr2.specified=false}
 * As Spring is unable to properly convert the types, unless specific {@link Filter} class are used, we need to use
 * fix type specific filters.
 */
public class AgentParamsCriteria implements Serializable, Criteria {

    private static final long serialVersionUID = 1L;

    private LongFilter id;

    private StringFilter callBackUrl;

    private StringFilter username;

    private StringFilter password;

    private StringFilter bankClientId;

    private StringFilter bankAccountPrefix;

    private LongFilter agentId;

    public AgentParamsCriteria() {}

    public AgentParamsCriteria(AgentParamsCriteria other) {
        this.id = other.id == null ? null : other.id.copy();
        this.callBackUrl = other.callBackUrl == null ? null : other.callBackUrl.copy();
        this.username = other.username == null ? null : other.username.copy();
        this.password = other.password == null ? null : other.password.copy();
        this.bankClientId = other.bankClientId == null ? null : other.bankClientId.copy();
        this.bankAccountPrefix = other.bankAccountPrefix == null ? null : other.bankAccountPrefix.copy();
        this.agentId = other.agentId == null ? null : other.agentId.copy();
    }

    @Override
    public AgentParamsCriteria copy() {
        return new AgentParamsCriteria(this);
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

    public StringFilter getCallBackUrl() {
        return callBackUrl;
    }

    public StringFilter callBackUrl() {
        if (callBackUrl == null) {
            callBackUrl = new StringFilter();
        }
        return callBackUrl;
    }

    public void setCallBackUrl(StringFilter callBackUrl) {
        this.callBackUrl = callBackUrl;
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

    public StringFilter getBankClientId() {
        return bankClientId;
    }

    public StringFilter bankClientId() {
        if (bankClientId == null) {
            bankClientId = new StringFilter();
        }
        return bankClientId;
    }

    public void setBankClientId(StringFilter bankClientId) {
        this.bankClientId = bankClientId;
    }

    public StringFilter getBankAccountPrefix() {
        return bankAccountPrefix;
    }

    public StringFilter bankAccountPrefix() {
        if (bankAccountPrefix == null) {
            bankAccountPrefix = new StringFilter();
        }
        return bankAccountPrefix;
    }

    public void setBankAccountPrefix(StringFilter bankAccountPrefix) {
        this.bankAccountPrefix = bankAccountPrefix;
    }

    public LongFilter getAgentId() {
        return agentId;
    }

    public LongFilter agentId() {
        if (agentId == null) {
            agentId = new LongFilter();
        }
        return agentId;
    }

    public void setAgentId(LongFilter agentId) {
        this.agentId = agentId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final AgentParamsCriteria that = (AgentParamsCriteria) o;
        return (
            Objects.equals(id, that.id) &&
            Objects.equals(callBackUrl, that.callBackUrl) &&
            Objects.equals(username, that.username) &&
            Objects.equals(password, that.password) &&
            Objects.equals(bankClientId, that.bankClientId) &&
            Objects.equals(bankAccountPrefix, that.bankAccountPrefix) &&
            Objects.equals(agentId, that.agentId)
        );
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, callBackUrl, username, password, bankClientId, bankAccountPrefix, agentId);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "AgentParamsCriteria{" +
            (id != null ? "id=" + id + ", " : "") +
            (callBackUrl != null ? "callBackUrl=" + callBackUrl + ", " : "") +
            (username != null ? "username=" + username + ", " : "") +
            (password != null ? "password=" + password + ", " : "") +
            (bankClientId != null ? "bankClientId=" + bankClientId + ", " : "") +
            (bankAccountPrefix != null ? "bankAccountPrefix=" + bankAccountPrefix + ", " : "") +
            (agentId != null ? "agentId=" + agentId + ", " : "") +
            "}";
    }
}
