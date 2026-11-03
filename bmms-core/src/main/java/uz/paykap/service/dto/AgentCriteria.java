package uz.paykap.service.dto;

import io.github.jhipster.service.filter.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;


/**
 * Criteria class for the Agent entity. This class is used in AgentResource to
 * receive all the possible filtering options from the Http GET request parameters.
 * For example the following could be a valid requests:
 * <code> /agents?id.greaterThan=5&amp;attr1.contains=something&amp;attr2.specified=false</code>
 * As Spring is unable to properly convert the types, unless specific {@link Filter} class are used, we need to use
 * fix type specific filters.
 */
@Getter
@Setter
@ToString
public class AgentCriteria implements Serializable {
    private static final long serialVersionUID = 1L;


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

    private LongFilter titleId;

    private LongFilter depositId;

    public AgentCriteria() {
    }

}
