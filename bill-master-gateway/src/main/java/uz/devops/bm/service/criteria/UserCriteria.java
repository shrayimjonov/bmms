package uz.devops.bm.service.criteria;

import java.io.Serializable;
import lombok.Data;
import tech.jhipster.service.Criteria;
import tech.jhipster.service.filter.BooleanFilter;
import tech.jhipster.service.filter.LongFilter;
import tech.jhipster.service.filter.StringFilter;

@Data
public class UserCriteria implements Serializable, Criteria {

    private LongFilter id;
    private StringFilter login;
    private StringFilter firstName;
    private StringFilter lastName;
    private StringFilter email;
    private BooleanFilter activated;
    private BooleanFilter male;
    private StringFilter phone;

    @Override
    public Criteria copy() {
        return null;
    }
}
