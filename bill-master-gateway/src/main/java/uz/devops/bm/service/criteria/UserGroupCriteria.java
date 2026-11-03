package uz.devops.bm.service.criteria;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import tech.jhipster.service.Criteria;
import tech.jhipster.service.filter.BooleanFilter;
import tech.jhipster.service.filter.Filter;
import tech.jhipster.service.filter.LongFilter;
import tech.jhipster.service.filter.StringFilter;
import uz.devops.bm.domain.enumeration.UserGroupType;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserGroupCriteria implements Criteria, Serializable {

    private LongFilter id;
    private StringFilter name;
    private BooleanFilter active;
    private StringFilter details;
    private UserGroupTypeFilter type;

    @NoArgsConstructor
    public static class UserGroupTypeFilter extends Filter<UserGroupType> {

        public UserGroupTypeFilter(UserGroupCriteria.UserGroupTypeFilter filter) {
            super(filter);
        }

        @Override
        public UserGroupCriteria.UserGroupTypeFilter copy() {
            return new UserGroupCriteria.UserGroupTypeFilter(this);
        }
    }

    @Override
    public Criteria copy() {
        return null;
    }
}
