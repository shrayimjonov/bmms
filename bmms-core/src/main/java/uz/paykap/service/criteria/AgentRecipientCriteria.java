package uz.paykap.service.criteria;

import tech.jhipster.service.Criteria;
import tech.jhipster.service.filter.*;

import java.io.Serializable;
import java.util.Objects;

/**
 * Criteria class for the {@link uz.paykap.domain.AgentRecipient} entity. This class is used
 * in {@link uz.paykap.web.rest.AgentRecipientResource} to receive all the possible filtering options from
 * the Http GET request parameters.
 * For example the following could be a valid request:
 * {@code /agent-recipients?id.greaterThan=5&attr1.contains=something&attr2.specified=false}
 * As Spring is unable to properly convert the types, unless specific {@link Filter} class are used, we need to use
 * fix type specific filters.
 */
public class AgentRecipientCriteria implements Serializable, Criteria {

    private static final long serialVersionUID = 1L;

    private LongFilter id;

    private DoubleFilter agentCommission;

    private DoubleFilter agentFee;

    private DoubleFilter systemCommissionUp;

    private DoubleFilter systemCommissionDown;

    private DoubleFilter systemFee;

    private BooleanFilter active;

    private IntegerFilter eposPort;

    private StringFilter eposMerchant;

    private StringFilter eposTerminal;

    private StringFilter requestGroup;

    private LongFilter agentId;

    private LongFilter recipientId;

    private LongFilter settingId;

    private LongFilter recipientGroupId;

    public AgentRecipientCriteria() {}

    public AgentRecipientCriteria(AgentRecipientCriteria other) {
        this.id = other.id == null ? null : other.id.copy();
        this.agentCommission = other.agentCommission == null ? null : other.agentCommission.copy();
        this.agentFee = other.agentFee == null ? null : other.agentFee.copy();
        this.systemCommissionUp = other.systemCommissionUp == null ? null : other.systemCommissionUp.copy();
        this.systemCommissionDown = other.systemCommissionDown == null ? null : other.systemCommissionDown.copy();
        this.systemFee = other.systemFee == null ? null : other.systemFee.copy();
        this.active = other.active == null ? null : other.active.copy();
        this.eposPort = other.eposPort == null ? null : other.eposPort.copy();
        this.eposMerchant = other.eposMerchant == null ? null : other.eposMerchant.copy();
        this.eposTerminal = other.eposTerminal == null ? null : other.eposTerminal.copy();
        this.requestGroup = other.requestGroup == null ? null : other.requestGroup.copy();
        this.agentId = other.agentId == null ? null : other.agentId.copy();
        this.recipientId = other.recipientId == null ? null : other.recipientId.copy();
        this.settingId = other.settingId == null ? null : other.settingId.copy();
        this.agentId = other.agentId == null ? null : other.agentId.copy();
        this.recipientGroupId = other.recipientGroupId == null ? null : other.recipientGroupId.copy();
    }

    @Override
    public AgentRecipientCriteria copy() {
        return new AgentRecipientCriteria(this);
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

    public DoubleFilter getAgentCommission() {
        return agentCommission;
    }

    public DoubleFilter agentCommission() {
        if (agentCommission == null) {
            agentCommission = new DoubleFilter();
        }
        return agentCommission;
    }

    public void setAgentCommission(DoubleFilter agentCommission) {
        this.agentCommission = agentCommission;
    }

    public DoubleFilter getAgentFee() {
        return agentFee;
    }

    public DoubleFilter agentFee() {
        if (agentFee == null) {
            agentFee = new DoubleFilter();
        }
        return agentFee;
    }

    public void setAgentFee(DoubleFilter agentFee) {
        this.agentFee = agentFee;
    }

    public DoubleFilter getSystemCommissionUp() {
        return systemCommissionUp;
    }

    public DoubleFilter systemCommissionUp() {
        if (systemCommissionUp == null) {
            systemCommissionUp = new DoubleFilter();
        }
        return systemCommissionUp;
    }

    public void setSystemCommissionUp(DoubleFilter systemCommissionUp) {
        this.systemCommissionUp = systemCommissionUp;
    }

    public DoubleFilter getSystemCommissionDown() {
        return systemCommissionDown;
    }

    public DoubleFilter systemCommissionDown() {
        if (systemCommissionDown == null) {
            systemCommissionDown = new DoubleFilter();
        }
        return systemCommissionDown;
    }

    public void setSystemCommissionDown(DoubleFilter systemCommissionDown) {
        this.systemCommissionDown = systemCommissionDown;
    }

    public DoubleFilter getSystemFee() {
        return systemFee;
    }

    public DoubleFilter systemFee() {
        if (systemFee == null) {
            systemFee = new DoubleFilter();
        }
        return systemFee;
    }

    public void setSystemFee(DoubleFilter systemFee) {
        this.systemFee = systemFee;
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

    public IntegerFilter getEposPort() {
        return eposPort;
    }

    public IntegerFilter eposPort() {
        if (eposPort == null) {
            eposPort = new IntegerFilter();
        }
        return eposPort;
    }

    public void setEposPort(IntegerFilter eposPort) {
        this.eposPort = eposPort;
    }

    public StringFilter getEposMerchant() {
        return eposMerchant;
    }

    public StringFilter eposMerchant() {
        if (eposMerchant == null) {
            eposMerchant = new StringFilter();
        }
        return eposMerchant;
    }

    public void setEposMerchant(StringFilter eposMerchant) {
        this.eposMerchant = eposMerchant;
    }

    public StringFilter getEposTerminal() {
        return eposTerminal;
    }

    public StringFilter eposTerminal() {
        if (eposTerminal == null) {
            eposTerminal = new StringFilter();
        }
        return eposTerminal;
    }

    public void setEposTerminal(StringFilter eposTerminal) {
        this.eposTerminal = eposTerminal;
    }

    public StringFilter getRequestGroup() {
        return requestGroup;
    }

    public StringFilter requestGroup() {
        if (requestGroup == null) {
            requestGroup = new StringFilter();
        }
        return requestGroup;
    }

    public void setRequestGroup(StringFilter requestGroup) {
        this.requestGroup = requestGroup;
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

    public LongFilter getSettingId() {
        return settingId;
    }

    public LongFilter settingId() {
        if (settingId == null) {
            settingId = new LongFilter();
        }
        return settingId;
    }

    public void setSettingId(LongFilter settingId) {
        this.settingId = settingId;
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

    public LongFilter getRecipientGroupId() {
        return recipientGroupId;
    }

    public LongFilter recipientGroupId() {
        if (recipientGroupId == null) {
            recipientGroupId = new LongFilter();
        }
        return recipientGroupId;
    }

    public void setRecipientGroupId(LongFilter recipientGroupId) {
        this.recipientGroupId = recipientGroupId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final AgentRecipientCriteria that = (AgentRecipientCriteria) o;
        return (
            Objects.equals(id, that.id) &&
            Objects.equals(agentCommission, that.agentCommission) &&
            Objects.equals(agentFee, that.agentFee) &&
            Objects.equals(systemCommissionUp, that.systemCommissionUp) &&
            Objects.equals(systemCommissionDown, that.systemCommissionDown) &&
            Objects.equals(systemFee, that.systemFee) &&
            Objects.equals(active, that.active) &&
            Objects.equals(eposPort, that.eposPort) &&
            Objects.equals(eposMerchant, that.eposMerchant) &&
            Objects.equals(eposTerminal, that.eposTerminal) &&
            Objects.equals(requestGroup, that.requestGroup) &&
            Objects.equals(agentId, that.agentId) &&
            Objects.equals(recipientId, that.recipientId) &&
            Objects.equals(settingId, that.settingId) &&
            Objects.equals(agentId, that.agentId) &&
            Objects.equals(recipientGroupId, that.recipientGroupId)
        );
    }

    @Override
    public int hashCode() {
        return Objects.hash(
            id,
            agentCommission,
            agentFee,
            systemCommissionUp,
            systemCommissionDown,
            systemFee,
            active,
            eposPort,
            eposMerchant,
            eposTerminal,
            requestGroup,
            agentId,
            recipientId,
            settingId,
            agentId,
            recipientGroupId
        );
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "AgentRecipientCriteria{" +
            (id != null ? "id=" + id + ", " : "") +
            (agentCommission != null ? "agentCommission=" + agentCommission + ", " : "") +
            (agentFee != null ? "agentFee=" + agentFee + ", " : "") +
            (systemCommissionUp != null ? "systemCommissionUp=" + systemCommissionUp + ", " : "") +
            (systemCommissionDown != null ? "systemCommissionDown=" + systemCommissionDown + ", " : "") +
            (systemFee != null ? "systemFee=" + systemFee + ", " : "") +
            (active != null ? "active=" + active + ", " : "") +
            (eposPort != null ? "eposPort=" + eposPort + ", " : "") +
            (eposMerchant != null ? "eposMerchant=" + eposMerchant + ", " : "") +
            (eposTerminal != null ? "eposTerminal=" + eposTerminal + ", " : "") +
            (requestGroup != null ? "requestGroup=" + requestGroup + ", " : "") +
            (agentId != null ? "agentId=" + agentId + ", " : "") +
            (recipientId != null ? "recipientId=" + recipientId + ", " : "") +
            (settingId != null ? "settingId=" + settingId + ", " : "") +
            (agentId != null ? "agentId=" + agentId + ", " : "") +
            (recipientGroupId != null ? "recipientGroupId=" + recipientGroupId + ", " : "") +
            "}";
    }
}
