package uz.devops.bm.repository;

import org.springframework.data.domain.Pageable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import uz.devops.bm.domain.Authority;
import uz.devops.bm.service.criteria.AuthorityCriteria;

public interface AuthorityRepositoryCustom {
    /**
     *
     * @param criteria
     * @param pageable
     * @return
     */
    Flux<Authority> findByCriteria(AuthorityCriteria criteria, Pageable pageable);

    /**
     *
     * @param criteria
     * @return
     */
    Mono<Long> countByCriteria(AuthorityCriteria criteria);
}
