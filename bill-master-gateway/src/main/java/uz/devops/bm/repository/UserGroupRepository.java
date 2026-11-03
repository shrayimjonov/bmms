package uz.devops.bm.repository;

import javax.validation.constraints.NotNull;
import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import uz.devops.bm.domain.User;
import uz.devops.bm.domain.UserGroup;
import uz.devops.bm.domain.enumeration.UserGroupType;

/**
 * Spring Data SQL reactive repository for the UserGroup entity.
 */
@SuppressWarnings("unused")
@Repository
public interface UserGroupRepository extends R2dbcRepository<UserGroup, Long>, UserGroupRepositoryInternal {
    Flux<UserGroup> findAllBy(Pageable pageable);

    // just to avoid having unambigous methods
    @Override
    Flux<UserGroup> findAll();

    @Override
    Mono<UserGroup> findById(Long id);

    Flux<UserGroup> findAllByTypeAndActive(UserGroupType type, @NotNull(message = "must not be null") Boolean active);

    @Query(
        "SELECT EXISTS (SELECT uga.user_group_id FROM user_group_authority uga WHERE uga.user_group_id = :userGroupId and uga.authority_name = :authority) "
    )
    Mono<Boolean> existsUserGroupAuthority(@Param("userGroupId") Long userGroupId, @Param("authority") String authority);

    @Query("INSERT INTO user_group_authority VALUES(:userGroupId, :authority) ")
    Mono<Integer> saveUserGroupAuthority(@Param("userGroupId") Long userGroupId, @Param("authority") String authority);

    @Query("DELETE  FROM user_group_authority uga WHERE uga.user_group_id = :userGroupId and uga.authority_name = :authority")
    Mono<Void> deleteUserGroupAuthority(@Param("userGroupId") Long userGroupId, @Param("authority") String authority);

    @Query("DELETE  FROM user_group_authority uga WHERE uga.user_group_id = :userGroupId")
    Mono<Void> deleteAllUserGroupAuthority(@Param("userGroupId") Long userGroupId);

    @Override
    <S extends UserGroup> Mono<S> save(S entity);
}

interface UserGroupRepositoryInternal {
    <S extends UserGroup> Mono<S> insert(S entity);

    <S extends UserGroup> Mono<S> save(S entity);

    Mono<Integer> update(UserGroup entity);

    Flux<UserGroup> findAll();

    Mono<UserGroup> findById(Long id);

    Flux<UserGroup> findAllBy(Pageable pageable);

    Flux<UserGroup> findAllBy(Pageable pageable, Criteria criteria);

    Flux<UserGroup> findAllWithAuthorities(Pageable pageable);

    Flux<UserGroup> findAllWithAuthoritiesByType(Pageable pageable, UserGroupType type);

    Mono<UserGroup> findOneWithAuthoritiesById(Long id);
}
