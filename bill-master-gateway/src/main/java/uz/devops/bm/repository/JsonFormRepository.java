package uz.devops.bm.repository;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import uz.devops.bm.config.Constants;
import uz.devops.bm.domain.JsonForm;

/**
 * Spring Data SQL reactive repository for the JsonForm entity.
 */
@SuppressWarnings("unused")
@Repository
public interface JsonFormRepository extends R2dbcRepository<JsonForm, Long>, JsonFormRepositoryInternal {
    Flux<JsonForm> findAllBy(Pageable pageable);

    @Override
    Mono<JsonForm> findOneWithEagerRelationships(Long id);

    @Override
    Flux<JsonForm> findAllWithEagerRelationships();

    @Override
    Flux<JsonForm> findAllWithEagerRelationships(Pageable page);

    @Override
    Mono<Void> deleteById(Long id);

    @Query(
        "SELECT entity.* FROM json_form entity JOIN rel_json_form__user joinTable ON entity.id = joinTable.json_form_id WHERE joinTable.user_id = :id"
    )
    Flux<JsonForm> findByUser(@Param(":id") String id);

    // just to avoid having unambigous methods
    @Override
    Flux<JsonForm> findAll();

    @Override
    Mono<JsonForm> findById(Long id);

    Flux<JsonForm> findAllByUsers_loginOrUsersIsNull(
        @NotNull @Pattern(regexp = Constants.LOGIN_REGEX) @Size(min = 1, max = 50) String users_login
    );

    @Override
    <S extends JsonForm> Mono<S> save(S entity);
}

interface JsonFormRepositoryInternal {
    <S extends JsonForm> Mono<S> insert(S entity);

    <S extends JsonForm> Mono<S> save(S entity);

    Mono<Integer> update(JsonForm entity);

    Flux<JsonForm> findAll();

    Mono<JsonForm> findById(Long id);

    Flux<JsonForm> findAllBy(Pageable pageable);

    Flux<JsonForm> findAllBy(Pageable pageable, Criteria criteria);

    Mono<JsonForm> findOneWithEagerRelationships(Long id);

    Flux<JsonForm> findAllWithEagerRelationships();

    Flux<JsonForm> findAllWithEagerRelationships(Pageable page);

    Mono<Void> deleteById(Long id);
}
