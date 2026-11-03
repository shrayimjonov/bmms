package uz.devops.bm.repository;

import static org.springframework.data.relational.core.query.Criteria.where;

import io.r2dbc.spi.Row;
import io.r2dbc.spi.RowMetadata;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.commons.beanutils.BeanComparator;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.r2dbc.convert.R2dbcConverter;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.data.relational.core.sql.Column;
import org.springframework.data.relational.core.sql.Expression;
import org.springframework.data.relational.core.sql.Select;
import org.springframework.data.relational.core.sql.SelectBuilder.SelectFromAndJoin;
import org.springframework.data.relational.core.sql.Table;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.r2dbc.core.RowsFetchSpec;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;
import uz.devops.bm.domain.Authority;
import uz.devops.bm.domain.UserGroup;
import uz.devops.bm.domain.enumeration.UserGroupType;
import uz.devops.bm.repository.rowmapper.UserGroupRowMapper;
import uz.devops.bm.service.EntityManager;

/**
 * Spring Data SQL reactive custom repository implementation for the UserGroup entity.
 */
@SuppressWarnings("unused")
class UserGroupRepositoryInternalImpl implements UserGroupRepositoryInternal {

    private final DatabaseClient db;
    private final R2dbcEntityTemplate r2dbcEntityTemplate;
    private final EntityManager entityManager;

    private final R2dbcConverter r2dbcConverter;

    private final UserGroupRowMapper usergroupMapper;

    private static final Table entityTable = Table.aliased("user_group", EntityManager.ENTITY_ALIAS);

    public UserGroupRepositoryInternalImpl(
        R2dbcEntityTemplate template,
        EntityManager entityManager,
        R2dbcConverter r2dbcConverter,
        UserGroupRowMapper usergroupMapper
    ) {
        this.db = template.getDatabaseClient();
        this.r2dbcEntityTemplate = template;
        this.entityManager = entityManager;
        this.r2dbcConverter = r2dbcConverter;
        this.usergroupMapper = usergroupMapper;
    }

    @Override
    public Flux<UserGroup> findAllBy(Pageable pageable) {
        return findAllBy(pageable, null);
    }

    @Override
    public Flux<UserGroup> findAllBy(Pageable pageable, Criteria criteria) {
        return createQuery(pageable, criteria).all();
    }

    @Override
    public Flux<UserGroup> findAllWithAuthorities(Pageable pageable) {
        String property = pageable.getSort().stream().map(Sort.Order::getProperty).findFirst().get();
        String direction = String.valueOf(pageable.getSort().stream().map(Sort.Order::getDirection).findFirst().get());
        long page = pageable.getPageNumber();
        long size = pageable.getPageSize();

        return db
            .sql("SELECT * FROM user_group u LEFT JOIN user_group_authority ua ON u.id=ua.user_group_id")
            .map((row, metadata) ->
                Tuples.of(r2dbcConverter.read(UserGroup.class, row, metadata), Optional.ofNullable(row.get("authority_name", String.class)))
            )
            .all()
            .groupBy(t -> t.getT1().getName())
            .flatMap(l -> l.collectList().map(t -> updateUserGroupWithAuthorities(t.get(0).getT1(), t)))
            .sort(
                Sort.Direction.fromString(direction) == Sort.DEFAULT_DIRECTION
                    ? new BeanComparator<>(property)
                    : new BeanComparator<>(property).reversed()
            )
            .skip(page * size)
            .take(size);
    }

    @Override
    public Flux<UserGroup> findAllWithAuthoritiesByType(Pageable pageable, UserGroupType type) {
        String property = pageable.getSort().stream().map(Sort.Order::getProperty).findFirst().get();
        String direction = String.valueOf(pageable.getSort().stream().map(Sort.Order::getDirection).findFirst().get());
        long page = pageable.getPageNumber();
        long size = pageable.getPageSize();

        return db
            .sql("SELECT * FROM user_group u WHERE u.type = :type LEFT JOIN user_group_authority ua ON u.id=ua.user_group_id")
            .map((row, metadata) ->
                Tuples.of(r2dbcConverter.read(UserGroup.class, row, metadata), Optional.ofNullable(row.get("authority_name", String.class)))
            )
            .all()
            .groupBy(t -> t.getT1().getName())
            .flatMap(l -> l.collectList().map(t -> updateUserGroupWithAuthorities(t.get(0).getT1(), t)))
            .sort(
                Sort.Direction.fromString(direction) == Sort.DEFAULT_DIRECTION
                    ? new BeanComparator<>(property)
                    : new BeanComparator<>(property).reversed()
            )
            .skip(page * size)
            .take(size);
    }

    @Override
    public Mono<UserGroup> findOneWithAuthoritiesById(Long id) {
        return findOneWithAuthoritiesBy("id", id);
    }

    private Mono<UserGroup> findOneWithAuthoritiesBy(String fieldName, Object fieldValue) {
        return db
            .sql(
                "SELECT * FROM user_group u LEFT JOIN user_group_authority ua ON u.id=ua.user_group_id WHERE u." +
                fieldName +
                " = :" +
                fieldName
            )
            .bind(fieldName, fieldValue)
            .map((row, metadata) ->
                Tuples.of(r2dbcConverter.read(UserGroup.class, row, metadata), Optional.ofNullable(row.get("authority_name", String.class)))
            )
            .all()
            .collectList()
            .filter(l -> !l.isEmpty())
            .map(l -> updateUserGroupWithAuthorities(l.get(0).getT1(), l));
    }

    RowsFetchSpec<UserGroup> createQuery(Pageable pageable, Criteria criteria) {
        List<Expression> columns = UserGroupSqlHelper.getColumns(entityTable, EntityManager.ENTITY_ALIAS);
        SelectFromAndJoin selectFrom = Select.builder().select(columns).from(entityTable);

        String select = entityManager.createSelect(selectFrom, UserGroup.class, pageable, criteria);
        String alias = entityTable.getReferenceName().getReference();
        String selectWhere = Optional
            .ofNullable(criteria)
            .map(crit ->
                new StringBuilder(select)
                    .append(" ")
                    .append("WHERE")
                    .append(" ")
                    .append(alias)
                    .append(".")
                    .append(crit.toString())
                    .toString()
            )
            .orElse(select); // TODO remove once https://github.com/spring-projects/spring-data-jdbc/issues/907 will be fixed
        return db.sql(selectWhere).map(this::process);
    }

    private UserGroup updateUserGroupWithAuthorities(UserGroup userGroup, List<Tuple2<UserGroup, Optional<String>>> tuples) {
        userGroup.setAuthorities(
            tuples
                .stream()
                .filter(t -> t.getT2().isPresent())
                .map(t -> {
                    Authority authority = new Authority();
                    authority.setName(t.getT2().get());
                    return authority;
                })
                .collect(Collectors.toSet())
        );

        return userGroup;
    }

    @Override
    public Flux<UserGroup> findAll() {
        return findAllBy(null, null);
    }

    @Override
    public Mono<UserGroup> findById(Long id) {
        return createQuery(null, where("id").is(id)).one();
    }

    private UserGroup process(Row row, RowMetadata metadata) {
        UserGroup entity = usergroupMapper.apply(row, "e");
        return entity;
    }

    @Override
    public <S extends UserGroup> Mono<S> insert(S entity) {
        return entityManager.insert(entity);
    }

    @Override
    public <S extends UserGroup> Mono<S> save(S entity) {
        if (entity.getId() == null) {
            return insert(entity);
        } else {
            return update(entity)
                .map(numberOfUpdates -> {
                    if (numberOfUpdates.intValue() <= 0) {
                        throw new IllegalStateException("Unable to update UserGroup with id = " + entity.getId());
                    }
                    return entity;
                });
        }
    }

    @Override
    public Mono<Integer> update(UserGroup entity) {
        //fixme is this the proper way?
        return r2dbcEntityTemplate.update(entity).thenReturn(1);
    }
}

class UserGroupSqlHelper {

    static List<Expression> getColumns(Table table, String columnPrefix) {
        List<Expression> columns = new ArrayList<>();
        columns.add(Column.aliased("id", table, columnPrefix + "_id"));
        columns.add(Column.aliased("name", table, columnPrefix + "_name"));
        columns.add(Column.aliased("active", table, columnPrefix + "_active"));
        columns.add(Column.aliased("details", table, columnPrefix + "_details"));
        columns.add(Column.aliased("type", table, columnPrefix + "_type"));

        return columns;
    }
}
