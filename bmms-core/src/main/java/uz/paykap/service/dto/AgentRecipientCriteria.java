package uz.paykap.service.dto;

import io.github.jhipster.service.filter.*;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;


/**
 * Criteria class for the AgentRecipient entity. This class is used in AgentRecipientResource to
 * receive all the possible filtering options from the Http GET request parameters.
 * For example the following could be a valid requests:
 * <code> /agent-recipients?id.greaterThan=5&amp;attr1.contains=something&amp;attr2.specified=false</code>
 * As Spring is unable to properly convert the types, unless specific {@link Filter} class are used, we need to use
 * fix type specific filters.
 */
@Getter
@Setter
public class AgentRecipientCriteria implements Serializable {

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

    private LongFilter recipientId;

    private LongFilter settingId;

    private LongFilter agentId;

    private LongFilter settingTypeId;

    private LongFilter recipientGroupId;

    private StringFilter recipientShortName;

    private StringFilter agentShortName;

    public AgentRecipientCriteria() {
    }

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
                (settingTypeId != null ? "settingTypeId=" + settingTypeId + ", " : "") +
                (recipientGroupId != null ? "recipientGroupId=" + recipientGroupId + ", " : "") +
                (recipientShortName != null ? "recipientShortName=" + recipientShortName + ", " : "") +
                (agentShortName != null ? "agentShortName=" + agentShortName + ", " : "") +
            "}";
    }

}
