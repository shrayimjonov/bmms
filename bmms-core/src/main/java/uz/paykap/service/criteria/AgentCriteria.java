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
import uz.paykap.domain.enumeration.AgentState;
import uz.paykap.domain.enumeration.ForeignPayStatus;

/**
 * Criteria class for the {@link uz.paykap.domain.Agent} entity. This class is used
 * in {@link uz.paykap.web.rest.AgentResource} to receive all the possible filtering options from
 * the Http GET request parameters.
 * For example the following could be a valid request:
 * {@code /agents?id.greaterThan=5&attr1.contains=something&attr2.specified=false}
 * As Spring is unable to properly convert the types, unless specific {@link Filter} class are used, we need to use
 * fix type specific filters.
 */
public class AgentCriteria implements Serializable, Criteria {

    private static final long serialVersionUID = 1L;

    public static class AgentStateFilter extends Filter<AgentState> {

        public AgentStateFilter() {}

        public AgentStateFilter(AgentCriteria.AgentStateFilter filter) {
            super(filter);
        }

        @Override
        public AgentCriteria.AgentStateFilter copy() {
            return new AgentCriteria.AgentStateFilter(this);
        }
    }

    private LongFilter id;

    private StringFilter shortName;

    private IntegerFilter amountRatio;

    private StringFilter apiKey;

    private StringFilter currencies;

    private StringFilter ipAddresses;

    private IntegerFilter ord;

    private BooleanFilter active;

    private StringFilter phoneNumber;

    private StringFilter inn;

    private StringFilter orgName;

    private StringFilter email;

    private AgentStateFilter state;

    private LongFilter titleId;

    private LongFilter depositId;

    public AgentCriteria() {}

    public AgentCriteria(AgentCriteria other) {
        this.id = other.id == null ? null : other.id.copy();
        this.shortName = other.shortName == null ? null : other.shortName.copy();
        this.amountRatio = other.amountRatio == null ? null : other.amountRatio.copy();
        this.apiKey = other.apiKey == null ? null : other.apiKey.copy();
        this.currencies = other.currencies == null ? null : other.currencies.copy();
        this.ipAddresses = other.ipAddresses == null ? null : other.ipAddresses.copy();
        this.ord = other.ord == null ? null : other.ord.copy();
        this.active = other.active == null ? null : other.active.copy();
        this.phoneNumber = other.phoneNumber == null ? null : other.phoneNumber.copy();
        this.inn = other.inn == null ? null : other.inn.copy();
        this.orgName = other.orgName == null ? null : other.orgName.copy();
        this.email = other.email == null ? null : other.email.copy();
        this.state = other.state == null ? null : other.state.copy();
        this.titleId = other.titleId == null ? null : other.titleId.copy();
        this.depositId = other.depositId == null ? null : other.depositId.copy();
    }

    @Override
    public AgentCriteria copy() {
        return new AgentCriteria(this);
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

    public StringFilter getShortName() {
        return shortName;
    }

    public StringFilter shortName() {
        if (shortName == null) {
            shortName = new StringFilter();
        }
        return shortName;
    }

    public void setShortName(StringFilter shortName) {
        this.shortName = shortName;
    }

    public IntegerFilter getAmountRatio() {
        return amountRatio;
    }

    public IntegerFilter amountRatio() {
        if (amountRatio == null) {
            amountRatio = new IntegerFilter();
        }
        return amountRatio;
    }

    public void setAmountRatio(IntegerFilter amountRatio) {
        this.amountRatio = amountRatio;
    }

    public StringFilter getApiKey() {
        return apiKey;
    }

    public StringFilter apiKey() {
        if (apiKey == null) {
            apiKey = new StringFilter();
        }
        return apiKey;
    }

    public void setApiKey(StringFilter apiKey) {
        this.apiKey = apiKey;
    }

    public StringFilter getCurrencies() {
        return currencies;
    }

    public StringFilter currencies() {
        if (currencies == null) {
            currencies = new StringFilter();
        }
        return currencies;
    }

    public void setCurrencies(StringFilter currencies) {
        this.currencies = currencies;
    }

    public StringFilter getIpAddresses() {
        return ipAddresses;
    }

    public StringFilter ipAddresses() {
        if (ipAddresses == null) {
            ipAddresses = new StringFilter();
        }
        return ipAddresses;
    }

    public void setIpAddresses(StringFilter ipAddresses) {
        this.ipAddresses = ipAddresses;
    }

    public IntegerFilter getOrd() {
        return ord;
    }

    public IntegerFilter ord() {
        if (ord == null) {
            ord = new IntegerFilter();
        }
        return ord;
    }

    public void setOrd(IntegerFilter ord) {
        this.ord = ord;
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

    public StringFilter getPhoneNumber() {
        return phoneNumber;
    }

    public StringFilter phoneNumber() {
        if (phoneNumber == null) {
            phoneNumber = new StringFilter();
        }
        return phoneNumber;
    }

    public void setPhoneNumber(StringFilter phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public StringFilter getInn() {
        return inn;
    }

    public StringFilter inn() {
        if (inn == null) {
            inn = new StringFilter();
        }
        return inn;
    }

    public void setInn(StringFilter inn) {
        this.inn = inn;
    }

    public StringFilter getOrgName() {
        return orgName;
    }

    public StringFilter orgName() {
        if (orgName == null) {
            orgName = new StringFilter();
        }
        return orgName;
    }

    public void setOrgName(StringFilter orgName) {
        this.orgName = orgName;
    }

    public StringFilter getEmail() {
        return email;
    }

    public StringFilter email() {
        if (email == null) {
            email = new StringFilter();
        }
        return email;
    }

    public void setEmail(StringFilter email) {
        this.email = email;
    }

    public AgentStateFilter getState() {
        return state;
    }

    public AgentStateFilter state() {
        if (state == null) {
            state = new AgentStateFilter();
        }
        return state;
    }

    public void setState(AgentStateFilter state) {
        this.state = state;
    }

    public LongFilter getTitleId() {
        return titleId;
    }

    public LongFilter titleId() {
        if (titleId == null) {
            titleId = new LongFilter();
        }
        return titleId;
    }

    public void setTitleId(LongFilter titleId) {
        this.titleId = titleId;
    }

    public LongFilter getDepositId() {
        return depositId;
    }

    public LongFilter depositId() {
        if (depositId == null) {
            depositId = new LongFilter();
        }
        return depositId;
    }

    public void setDepositId(LongFilter depositId) {
        this.depositId = depositId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final AgentCriteria that = (AgentCriteria) o;
        return (
            Objects.equals(id, that.id) &&
            Objects.equals(shortName, that.shortName) &&
            Objects.equals(amountRatio, that.amountRatio) &&
            Objects.equals(apiKey, that.apiKey) &&
            Objects.equals(currencies, that.currencies) &&
            Objects.equals(ipAddresses, that.ipAddresses) &&
            Objects.equals(ord, that.ord) &&
            Objects.equals(active, that.active) &&
            Objects.equals(phoneNumber, that.phoneNumber) &&
            Objects.equals(inn, that.inn) &&
            Objects.equals(orgName, that.orgName) &&
            Objects.equals(email, that.email) &&
            Objects.equals(state, that.state) &&
            Objects.equals(titleId, that.titleId) &&
            Objects.equals(depositId, that.depositId)
        );
    }

    @Override
    public int hashCode() {
        return Objects.hash(
            id,
            shortName,
            amountRatio,
            apiKey,
            currencies,
            ipAddresses,
            ord,
            active,
            phoneNumber,
            inn,
            orgName,
            email,
            state,
            titleId,
            depositId
        );
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "AgentCriteria{" +
            (id != null ? "id=" + id + ", " : "") +
            (shortName != null ? "shortName=" + shortName + ", " : "") +
            (amountRatio != null ? "amountRatio=" + amountRatio + ", " : "") +
            (apiKey != null ? "apiKey=" + apiKey + ", " : "") +
            (currencies != null ? "currencies=" + currencies + ", " : "") +
            (ipAddresses != null ? "ipAddresses=" + ipAddresses + ", " : "") +
            (ord != null ? "ord=" + ord + ", " : "") +
            (active != null ? "active=" + active + ", " : "") +
            (phoneNumber != null ? "phoneNumber=" + phoneNumber + ", " : "") +
            (inn != null ? "inn=" + inn + ", " : "") +
            (orgName != null ? "orgName=" + orgName + ", " : "") +
            (email != null ? "email=" + email + ", " : "") +
            (state != null ? "state=" + state + ", " : "") +
            (titleId != null ? "titleId=" + titleId + ", " : "") +
            (depositId != null ? "depositId=" + depositId + ", " : "") +
            "}";
    }
}
