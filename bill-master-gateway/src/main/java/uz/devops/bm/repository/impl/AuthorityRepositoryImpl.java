package uz.devops.bm.repository.impl;

import lombok.AllArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.data.relational.core.query.Query;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import uz.devops.bm.domain.Authority;
import uz.devops.bm.repository.AuthorityRepositoryCustom;
import uz.devops.bm.service.criteria.AuthorityCriteria;
import uz.devops.bm.service.criteria.QueryService;

@Service
@AllArgsConstructor
public class AuthorityRepositoryImpl implements AuthorityRepositoryCustom {

    private final R2dbcEntityTemplate template;
    private final QueryService queryService;

    @Override
    public Flux<Authority> findByCriteria(AuthorityCriteria criteria, Pageable pageable) {
        return template.select(Authority.class).matching(Query.query(getCriteria(criteria)).with(pageable)).all();
    }

    @Override
    public Mono<Long> countByCriteria(AuthorityCriteria criteria) {
        return template.select(Authority.class).matching(Query.query(getCriteria(criteria))).count();
    }

    private Criteria getCriteria(AuthorityCriteria criteria) {
        var all = Criteria.empty();

        if (criteria == null) {
            return all;
        }

        if (criteria.getName() != null) {
            all = all.and(queryService.buildStringCriteria(criteria.getName(), "name"));
        }

        if (criteria.getDetails() != null) {
            all = all.and(queryService.buildStringCriteria(criteria.getDetails(), "details"));
        }
        return all;
    }
}
