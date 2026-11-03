package uz.devops.bm.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.data.relational.core.query.Query;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import uz.devops.bm.domain.JsonForm;
import uz.devops.bm.domain.User;
import uz.devops.bm.repository.JsonFormRepository;
import uz.devops.bm.service.JsonFormService;
import uz.devops.bm.service.criteria.JsonFormCriteria;
import uz.devops.bm.service.criteria.QueryService;
import uz.devops.bm.service.dto.JsonFormDTO;
import uz.devops.bm.service.mapper.JsonFormMapper;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Service Implementation for managing {@link JsonForm}.
 */
@Service
@Transactional
public class JsonFormServiceImpl implements JsonFormService {

  private final Logger log = LoggerFactory.getLogger(JsonFormServiceImpl.class);

  private final JsonFormRepository jsonFormRepository;

  private final JsonFormMapper jsonFormMapper;

  private final R2dbcEntityTemplate template;

  private final QueryService queryService;

  private final DatabaseClient databaseClient;

  public JsonFormServiceImpl(
    JsonFormRepository jsonFormRepository,
    JsonFormMapper jsonFormMapper,
    R2dbcEntityTemplate template,
    QueryService queryService,
    DatabaseClient databaseClient) {
    this.jsonFormRepository = jsonFormRepository;
    this.jsonFormMapper = jsonFormMapper;
    this.template = template;
    this.queryService = queryService;
    this.databaseClient = databaseClient;
  }

  @Override
  public Mono<JsonFormDTO> save(JsonFormDTO jsonFormDTO) {
    log.debug("Request to save JsonForm : {}", jsonFormDTO);
    return jsonFormRepository
      .save(jsonFormMapper.toEntity(jsonFormDTO))
      .map(jsonForm -> {
        System.out.println(jsonForm);
        return jsonFormMapper.toDto(jsonForm);
      });
  }

  @Override
  public Mono<JsonFormDTO> partialUpdate(JsonFormDTO jsonFormDTO) {
    log.debug("Request to partially update JsonForm : {}", jsonFormDTO);

    return jsonFormRepository
      .findById(jsonFormDTO.getId())
      .map(existingJsonForm -> {
        jsonFormMapper.partialUpdate(existingJsonForm, jsonFormDTO);

        return existingJsonForm;
      })
      .flatMap(jsonFormRepository::save)
      .map(jsonFormMapper::toDto);
  }

  @Override
  @Transactional(readOnly = true)
  public Flux<JsonFormDTO> findAll(Pageable pageable) {
    return jsonFormRepository.findAllBy(pageable).flatMap(this::getJsonFormAllUsers);
  }

  private Set<User> getUsers(List<Map<String, Object>> list) {
    Set<User> userSet = new HashSet<>();
    list.forEach(map -> {
      User user = new User();
      user.setId(map.get("id") != null ? (String) map.get("id") : null);
      user.setLogin(map.get("login") != null ? String.valueOf(map.get("login")) : null);
//            String encodedPassword = map.get("password_hash") != null
//                ? passwordEncoder.encode((CharSequence) map.get("password_hash"))
//                : null;
//            user.setPassword(encodedPassword)
      user.setFirstName(map.get("first_name") != null ? String.valueOf(map.get("first_name")) : null);
      user.setLastName(map.get("last_name") != null ? String.valueOf(map.get("last_name")) : null);
      user.setEmail(map.get("email") != null ? String.valueOf(map.get("email")) : null);
      user.setImageUrl(map.get("image_url") != null ? String.valueOf(map.get("image_url")) : null);
      user.setActivated(map.get("activated") != null ? Boolean.parseBoolean(String.valueOf(map.get("activated"))) : null);
      user.setLangKey(map.get("lang_key") != null ? String.valueOf(map.get("lang_key")) : null);
//            user.setActivationKey(map.get("activation_key") != null ? String.valueOf(map.get("activation_key")) : null);
//            user.setResetKey(map.get("reset_key") != null ? String.valueOf(map.get("reset_key")) : null);
      user.setCreatedBy(map.get("created_by") != null ? String.valueOf(map.get("created_by")) : null);

      LocalDateTime createdDate = (LocalDateTime) map.get("created_date");
      user.setCreatedDate(createdDate != null ? createdDate.toInstant(ZoneOffset.UTC) : null);

//            LocalDateTime resetDate = (LocalDateTime) map.get("reset_date");
//            user.setResetDate(resetDate != null ? resetDate.toInstant(ZoneOffset.UTC) : null);

      user.setLastModifiedBy(map.get("last_modified_by") != null ? String.valueOf(map.get("last_modified_by")) : null);

      LocalDateTime lastModifiedDate = (LocalDateTime) map.get("last_modified_date");
      user.setLastModifiedDate(lastModifiedDate != null ? lastModifiedDate.toInstant(ZoneOffset.UTC) : null);

//            user.setPhoneNumber(map.get("phone_number") != null ? String.valueOf(map.get("phone_number")) : null);
      userSet.add(user);
    });
    return userSet;
  }

  public Flux<JsonFormDTO> findAllWithEagerRelationships(Pageable pageable) {
    return jsonFormRepository.findAllWithEagerRelationships(pageable).map(jsonFormMapper::toDto);
  }

  public Mono<Long> countAll() {
    return jsonFormRepository.count();
  }

  @Override
  @Transactional(readOnly = true)
  public Mono<JsonFormDTO> findOne(Long id) {
    log.debug("Request to get JsonForm : {}", id);
    return jsonFormRepository.findOneWithEagerRelationships(id).flatMap(this::getJsonFormAllUsers);
  }

  private Mono<JsonFormDTO> getJsonFormAllUsers(JsonForm jsonForm) {
    return databaseClient
      .sql(
        "SELECT ju.id, login, password_hash, first_name, last_name, email, image_url, activated, lang_key, activation_key, reset_key, created_by, created_date, reset_date, last_modified_by, last_modified_date, phone_number FROM rel_json_form__user rjfu,jhi_user ju where rjfu.json_form_id=" +
          jsonForm.getId() +
          " and ju.id=rjfu.user_id"
      )
      .fetch()
      .all()
      .collectList()
      .flatMap(map -> {
        jsonForm.setUsers(getUsers(map));
        return Mono.just(jsonFormMapper.toDto(jsonForm));
      });
  }

  public Mono<Long> countByCriteria(JsonFormCriteria criteria) {
    return template.select(JsonForm.class).matching(Query.query(getCriteria(criteria))).count();
  }

  public Flux<JsonFormDTO> findByCriteria(JsonFormCriteria criteria, Pageable pageable) {
    return template.select(JsonForm.class).matching(Query.query(getCriteria(criteria)).with(pageable)).all().map(jsonFormMapper::toDto);
  }

  @Override
  public Mono<Void> delete(Long id) {
    log.debug("Request to delete JsonForm : {}", id);
    return jsonFormRepository.deleteById(id);
  }

  private Criteria getCriteria(JsonFormCriteria criteria) {
    var all = Criteria.empty();

    if (criteria == null) {
      return all;
    }

    if (criteria.getId() != null) {
      all = all.and(queryService.buildCriteria(criteria.getId(), "id"));
    }

    if (criteria.getName() != null) {
      all = all.and(queryService.buildStringCriteria(criteria.getName(), "name"));
    }

    if (criteria.getType() != null) {
      all = all.and(queryService.buildCriteria(criteria.getType(), "type"));
    }

    if (criteria.getData() != null) {
      all = all.and(queryService.buildStringCriteria(criteria.getData(), "data"));
    }

    if (criteria.getUsername() != null) {
      all = all.and(queryService.buildStringCriteria(criteria.getUsername(), "username"));
    }

    if (criteria.getActive() != null) {
      all = all.and(queryService.buildCriteria(criteria.getActive(), "active"));
    }

    if (criteria.getDescription() != null) {
      all = all.and(queryService.buildStringCriteria(criteria.getDescription(), "description"));
    }

    if (criteria.getCreatedBy() != null) {
      all = all.and(queryService.buildStringCriteria(criteria.getUsername(), "createdBy"));
    }

    if (criteria.getCreatedDate() != null) {
      all = all.and(queryService.buildStringCriteria(criteria.getUsername(), "createdDate"));
    }
    return all;
  }
}
