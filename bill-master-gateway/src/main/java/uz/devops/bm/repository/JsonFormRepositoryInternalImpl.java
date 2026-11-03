package uz.devops.bm.repository;

import static org.springframework.data.relational.core.query.Criteria.where;
import static org.springframework.data.relational.core.query.Query.query;

import io.r2dbc.spi.Row;
import io.r2dbc.spi.RowMetadata;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
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
import uz.devops.bm.domain.JsonForm;
import uz.devops.bm.domain.User;
import uz.devops.bm.repository.rowmapper.JsonFormRowMapper;
import uz.devops.bm.service.EntityManager;
import uz.devops.bm.service.EntityManager.LinkTable;

/**
 * Spring Data SQL reactive custom repository implementation for the JsonForm entity.
 */
@SuppressWarnings("unused")
class JsonFormRepositoryInternalImpl implements JsonFormRepositoryInternal {

    private final DatabaseClient db;
    private final R2dbcEntityTemplate r2dbcEntityTemplate;
    private final EntityManager entityManager;

    private final JsonFormRowMapper jsonformMapper;

    private static final Table entityTable = Table.aliased("json_form", EntityManager.ENTITY_ALIAS);

    private static final EntityManager.LinkTable userLink = new LinkTable("rel_json_form__user", "json_form_id", "user_id");

    public JsonFormRepositoryInternalImpl(R2dbcEntityTemplate template, EntityManager entityManager, JsonFormRowMapper jsonformMapper) {
        this.db = template.getDatabaseClient();
        this.r2dbcEntityTemplate = template;
        this.entityManager = entityManager;
        this.jsonformMapper = jsonformMapper;
    }

    @Override
    public Flux<JsonForm> findAllBy(Pageable pageable) {
        return findAllBy(pageable, null);
    }

    @Override
    public Flux<JsonForm> findAllBy(Pageable pageable, Criteria criteria) {
        return createQuery(pageable, criteria).all();
    }

    RowsFetchSpec<JsonForm> createQuery(Pageable pageable, Criteria criteria) {
        List<Expression> columns = JsonFormSqlHelper.getColumns(entityTable, EntityManager.ENTITY_ALIAS);
        SelectFromAndJoin selectFrom = Select.builder().select(columns).from(entityTable);

        String select = entityManager.createSelect(selectFrom, JsonForm.class, pageable, criteria);
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

    @Override
    public Flux<JsonForm> findAll() {
        return findAllBy(null, null);
    }

    @Override
    public Mono<JsonForm> findById(Long id) {
        return createQuery(null, where("id").is(id)).one();
    }

    @Override
    public Mono<JsonForm> findOneWithEagerRelationships(Long id) {
        return findById(id);
    }

    @Override
    public Flux<JsonForm> findAllWithEagerRelationships() {
        return findAll();
    }

    @Override
    public Flux<JsonForm> findAllWithEagerRelationships(Pageable page) {
        return findAllBy(page);
    }

    private JsonForm process(Row row, RowMetadata metadata) {
        JsonForm entity = jsonformMapper.apply(row, "e");
        return entity;
    }

    @Override
    public <S extends JsonForm> Mono<S> insert(S entity) {
        return entityManager.insert(entity);
    }

    @Override
    public <S extends JsonForm> Mono<S> save(S entity) {
        if (entity.getId() == null) {
            return insert(entity).flatMap(savedEntity -> updateRelations(savedEntity));
        } else {
            return update(entity)
                .map(numberOfUpdates -> {
                    if (numberOfUpdates.intValue() <= 0) {
                        throw new IllegalStateException("Unable to update JsonForm with id = " + entity.getId());
                    }
                    return entity;
                })
                .then(updateRelations(entity));
        }
    }

    @Override
    public Mono<Integer> update(JsonForm entity) {
        //fixme is this the proper way?
        return r2dbcEntityTemplate.update(entity).thenReturn(1);
    }

    @Override
    public Mono<Void> deleteById(Long entityId) {
        return deleteRelations(entityId)
            .then(r2dbcEntityTemplate.delete(JsonForm.class).matching(query(where("id").is(entityId))).all().then());
    }

    protected <S extends JsonForm> Mono<S> updateRelations(S entity) {
        Mono<Void> result = entityManager.updateLinkTable(userLink, entity.getId(), entity.getUsers().stream().map(User::getId)).then();
        return result.thenReturn(entity);
    }

    protected Mono<Void> deleteRelations(Long entityId) {
        return entityManager.deleteFromLinkTable(userLink, entityId);
    }
}

class JsonFormSqlHelper {

    static List<Expression> getColumns(Table table, String columnPrefix) {
        List<Expression> columns = new ArrayList<>();
        columns.add(Column.aliased("id", table, columnPrefix + "_id"));
        columns.add(Column.aliased("name", table, columnPrefix + "_name"));
        columns.add(Column.aliased("type", table, columnPrefix + "_type"));
        columns.add(Column.aliased("data", table, columnPrefix + "_data"));
        columns.add(Column.aliased("username", table, columnPrefix + "_username"));
        columns.add(Column.aliased("active", table, columnPrefix + "_active"));
        columns.add(Column.aliased("description", table, columnPrefix + "_description"));
        columns.add(Column.aliased("created_by", table, columnPrefix + "_created_by"));
        columns.add(Column.aliased("created_date", table, columnPrefix + "_created_date"));
        columns.add(Column.aliased("last_modified_by", table, columnPrefix + "_last_modified_by"));
        columns.add(Column.aliased("last_modified_date", table, columnPrefix + "_last_modified_date"));

        return columns;
    }
}
