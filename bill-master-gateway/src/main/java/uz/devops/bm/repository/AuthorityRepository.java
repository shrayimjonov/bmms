package uz.devops.bm.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import uz.devops.bm.domain.Authority;

/**
 * Spring Data R2DBC repository for the {@link Authority} entity.
 */
public interface AuthorityRepository extends R2dbcRepository<Authority, String>, AuthorityRepositoryCustom {
    Mono<Authority> findOneByName(String name);

    Flux<Authority> findAllBy(Pageable pageable);
}
