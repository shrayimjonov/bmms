package uz.devops.bm.service.criteria;

import java.util.Collection;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.stereotype.Component;
import tech.jhipster.service.filter.Filter;
import tech.jhipster.service.filter.RangeFilter;
import tech.jhipster.service.filter.StringFilter;

@Component
public class QueryService {

    private static final String LIKE_CONDITION = "%";

    public <X extends Comparable<? super X>> Criteria buildStringCriteria(StringFilter filter, String column) {
        Criteria result = buildCriteria(filter, column);

        if (filter.getContains() != null) {
            if (filter instanceof CasedStringFilter) {
                result = result.and(containsSpecification(column, ((CasedStringFilter) filter).getCasedContains(), false));
            } else {
                result = result.and(containsSpecification(column, filter.getContains()));
            }
        }

        if (filter.getDoesNotContain() != null) {
            if (filter instanceof CasedStringFilter) {
                result = result.and(doesNotContainSpecification(column, ((CasedStringFilter) filter).getCasedNotContains(), false));
            } else {
                result = result.and(doesNotContainSpecification(column, filter.getDoesNotContain()));
            }
        }
        return result;
    }

    public <X extends Comparable<? super X>> Criteria buildRangeCriteria(RangeFilter<X> filter, String column) {
        Criteria result = buildCriteria(filter, column);

        if (filter.getGreaterThan() != null) {
            result = result.and(greaterThan(column, filter.getGreaterThan()));
        }
        if (filter.getGreaterThanOrEqual() != null) {
            result = result.and(greaterThanOrEqualTo(column, filter.getGreaterThanOrEqual()));
        }
        if (filter.getLessThan() != null) {
            result = result.and(lessThan(column, filter.getLessThan()));
        }
        if (filter.getLessThanOrEqual() != null) {
            result = result.and(lessThanOrEqualTo(column, filter.getLessThanOrEqual()));
        }
        return result;
    }

    public <X extends Comparable<? super X>> Criteria buildCriteria(Filter<X> filter, String column) {
        Criteria result = Criteria.empty();

        if (filter.getSpecified() != null) {
            result = result.and(byFieldSpecified(column, filter.getSpecified()));
        }
        if (filter.getEquals() != null) {
            result = result.and(equalsSpecification(column, filter.getEquals()));
        }
        if (filter.getNotEquals() != null) {
            result = result.and(notEqualsSpecification(column, filter.getNotEquals()));
        }
        if (filter.getNotIn() != null) {
            result = result.and(valueNotIn(column, filter.getNotIn()));
        }
        if (filter.getIn() != null) {
            result = result.and(valueIn(column, filter.getIn()));
        }
        return result;
    }

    protected <X> Criteria equalsSpecification(String columnName, final X value) {
        return Criteria.where(columnName).is(value);
    }

    protected <X> Criteria notEqualsSpecification(String column, final X value) {
        return Criteria.where(column).not(value);
    }

    protected Criteria containsSpecification(String column, final String value, boolean ignoreCase) {
        return Criteria.where(column).like(LIKE_CONDITION + value + LIKE_CONDITION).ignoreCase(ignoreCase); // TODO
    }

    protected Criteria containsSpecification(String column, final String value) {
        return containsSpecification(column, value, true);
    }

    protected Criteria doesNotContainSpecification(String column, final String value) {
        return doesNotContainSpecification(column, value, true);
    }

    protected Criteria doesNotContainSpecification(String column, final String value, boolean ignoreCase) {
        return Criteria.where(column).notLike(LIKE_CONDITION + value + LIKE_CONDITION).ignoreCase(ignoreCase);
    }

    protected <X> Criteria byFieldSpecified(String column, final boolean specified) {
        return specified ? Criteria.where(column).isNotNull() : Criteria.where(column).isNull();
    }

    protected <X> Criteria valueIn(String column, final Collection<X> values) {
        return Criteria.where(column).in(values);
    }

    protected <X> Criteria valueNotIn(String column, final Collection<X> values) {
        return Criteria.where(column).notIn(values);
    }

    protected <X extends Comparable<? super X>> Criteria greaterThanOrEqualTo(String column, final X value) {
        return Criteria.where(column).greaterThanOrEquals(value);
    }

    protected <X extends Comparable<? super X>> Criteria greaterThan(String column, final X value) {
        return Criteria.where(column).greaterThan(value);
    }

    protected <X extends Comparable<? super X>> Criteria lessThanOrEqualTo(String column, final X value) {
        return Criteria.where(column).lessThanOrEquals(value);
    }

    protected <X extends Comparable<? super X>> Criteria lessThan(String column, final X value) {
        return Criteria.where(column).lessThan(value);
    }
}
