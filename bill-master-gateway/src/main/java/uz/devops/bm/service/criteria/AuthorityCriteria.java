package uz.devops.bm.service.criteria;

import java.io.Serializable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import tech.jhipster.service.Criteria;
import tech.jhipster.service.filter.StringFilter;

@ToString
@EqualsAndHashCode
public class AuthorityCriteria implements Criteria, Serializable {

    @Setter
    private CasedStringFilter name;

    @Getter
    @Setter
    private StringFilter details;

    public CasedStringFilter getName() {
        if (name == null) {
            return null;
        }
        name.setTextCase(CasedStringFilter.TextCase.UPPER);
        return name;
    }

    @Override
    public Criteria copy() {
        return null;
    }
}
