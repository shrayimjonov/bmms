package uz.devops.bm.service.criteria;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import tech.jhipster.service.Criteria;
import tech.jhipster.service.filter.*;
import uz.devops.bm.domain.enumeration.JsonFormType;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JsonFormCriteria implements Criteria, Serializable {

    private LongFilter id;

    private StringFilter name;

    private JsonFormTypeFilter type;

    private StringFilter data;

    private StringFilter username;

    private BooleanFilter active;

    private StringFilter description;

    private StringFilter createdBy;

    private InstantFilter createdDate;

    @NoArgsConstructor
    public static class JsonFormTypeFilter extends Filter<JsonFormType> {

        public JsonFormTypeFilter(JsonFormCriteria.JsonFormTypeFilter filter) {
            super(filter);
        }

        @Override
        public JsonFormCriteria.JsonFormTypeFilter copy() {
            return new JsonFormCriteria.JsonFormTypeFilter(this);
        }
    }

    @Override
    public Criteria copy() {
        return null;
    }
}
