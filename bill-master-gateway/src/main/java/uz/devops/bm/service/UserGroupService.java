package uz.devops.bm.service;

import java.util.HashSet;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.data.relational.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import uz.devops.bm.config.Constants;
import uz.devops.bm.domain.UserGroup;
import uz.devops.bm.domain.enumeration.UserGroupType;
import uz.devops.bm.repository.AuthorityRepository;
import uz.devops.bm.repository.UserGroupRepository;
import uz.devops.bm.security.SecurityUtils;
import uz.devops.bm.service.criteria.QueryService;
import uz.devops.bm.service.criteria.UserCriteria;
import uz.devops.bm.service.criteria.UserGroupCriteria;
import uz.devops.bm.service.dto.AdminUserGroupDTO;
import uz.devops.bm.service.dto.UserGroupDTO;
import uz.devops.bm.service.mapper.UserGroupMapper;

/**
 * Service Implementation for managing {@link UserGroup}.
 */
@Service
@Transactional
public class UserGroupService {

    private final Logger log = LoggerFactory.getLogger(UserGroupService.class);

    private final UserGroupRepository userGroupRepository;

    private final UserGroupMapper userGroupMapper;

    private final AuthorityRepository authorityRepository;

    private final R2dbcEntityTemplate template;
    private final QueryService queryService;

    public UserGroupService(
        UserGroupRepository userGroupRepository,
        UserGroupMapper userGroupMapper,
        AuthorityRepository authorityRepository,
        R2dbcEntityTemplate template,
        QueryService queryService
    ) {
        this.userGroupRepository = userGroupRepository;
        this.userGroupMapper = userGroupMapper;
        this.authorityRepository = authorityRepository;
        this.template = template;
        this.queryService = queryService;
    }

    /**
     * Save a userGroup.
     *
     * @param userGroupDTO the entity to save.
     * @return the persisted entity.
     */
    public Mono<UserGroupDTO> save(AdminUserGroupDTO userGroupDTO) {
        log.debug("Request to save UserGroup : {}", userGroupDTO);
        UserGroup userGroup = new UserGroup();

        userGroup.setId(userGroupDTO.getId());
        userGroup.setActive(userGroupDTO.getActive());
        userGroup.setDetails(userGroupDTO.getDetails());
        userGroup.setName(userGroupDTO.getName());
        userGroup.setType(userGroupDTO.getType());

        return Flux
            .fromIterable(userGroupDTO.getAuthorities() != null ? userGroupDTO.getAuthorities() : new HashSet<>())
            .flatMap(authorityRepository::findById)
            .doOnNext(authority -> userGroup.getAuthorities().add(authority))
            .then(Mono.just(userGroup))
            .flatMap(this::saveUser)
            .map(userGroupMapper::toDto);
    }

    public Mono<UserGroup> saveUser(UserGroup userGroup) {
        return SecurityUtils
            .getCurrentUserLogin()
            .switchIfEmpty(Mono.just(Constants.SYSTEM))
            .flatMap(login -> {
                log.debug("UserGroup to save: {}", userGroup);
                // Saving the relationship can be done in an entity callback
                // once https://github.com/spring-projects/spring-data-r2dbc/issues/215 is done
                return userGroupRepository
                    .save(userGroup)
                    .flatMap(savedUserGroup ->
                        Flux
                            .fromIterable(userGroup.getAuthorities())
                            .flatMap(authority ->
                                userGroupRepository
                                    .deleteAllUserGroupAuthority(userGroup.getId())
                                    .then(userGroupRepository.saveUserGroupAuthority(savedUserGroup.getId(), authority.getName()))
                            //                                    .existsUserGroupAuthority(savedUser.getId(), authority.getName())
                            //                                    .flatMap(exist -> {
                            //                                        if (!exist) {
                            //                                            return userGroupRepository
                            //                                                .saveUserGroupAuthority(savedUser.getId(), authority.getName())
                            //                                                .then(Mono.just(savedUser));
                            //                                        }
                            //                                        return Mono.just(savedUser);
                            //                                    })
                            )
                            .then(Mono.just(savedUserGroup))
                    );
            });
    }

    /**
     * Partially update a userGroup.
     *
     * @param userGroupDTO the entity to update partially.
     * @return the persisted entity.
     */
    public Mono<UserGroupDTO> partialUpdate(UserGroupDTO userGroupDTO) {
        log.debug("Request to partially update UserGroup : {}", userGroupDTO);

        return userGroupRepository
            .findById(userGroupDTO.getId())
            .map(existingUserGroup -> {
                userGroupMapper.partialUpdate(existingUserGroup, userGroupDTO);

                return existingUserGroup;
            })
            .flatMap(userGroupRepository::save)
            .map(userGroupMapper::toDto);
    }

    /**
     * Get all the userGroups.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    @Transactional(readOnly = true)
    public Flux<UserGroupDTO> findAll(Pageable pageable) {
        log.debug("Request to get all UserGroups");
        return userGroupRepository.findAllBy(pageable).map(userGroupMapper::toDto);
    }

    /**
     * Returns the number of userGroups available.
     *
     * @return the number of entities in the database.
     */
    public Mono<Long> countAll() {
        return userGroupRepository.count();
    }

    /**
     * Get one userGroup by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    public Mono<UserGroupDTO> findOne(Long id) {
        log.debug("Request to get UserGroup : {}", id);
        return userGroupRepository.findById(id).map(userGroupMapper::toDto);
    }

    /**
     * Delete the userGroup by id.
     *
     * @param id the id of the entity.
     * @return a Mono to signal the deletion
     */
    public Mono<Object> delete(Long id) {
        log.debug("Request to delete UserGroup : {}", id);
        return userGroupRepository
            .findOneWithAuthoritiesById(id)
            .flatMap(userGroup ->
                Flux
                    .fromIterable(userGroup.getAuthorities())
                    .flatMap(authority -> userGroupRepository.deleteUserGroupAuthority(userGroup.getId(), authority.getName()))
                    .then(Mono.just(userGroup))
            )
            .flatMap(userGroup -> userGroupRepository.deleteById(userGroup.getId()));
    }

    @Transactional(readOnly = true)
    public Mono<List<AdminUserGroupDTO>> getAllManagedUsers(Pageable pageable) {
        log.debug("Request to get user group with authorities: {}", pageable);
        return userGroupRepository.findAllWithAuthorities(pageable).collectList().map(userGroupMapper::fromEntityToAUserGroup);
    }

    @Transactional(readOnly = true)
    public Mono<List<AdminUserGroupDTO>> getAllManagedUsersByType(Pageable pageable, UserGroupType type) {
        log.debug("Request to get user group with authorities by type , pageable: {}, \ntype:{}", pageable, type);
        return userGroupRepository.findAllWithAuthoritiesByType(pageable, type).collectList().map(userGroupMapper::fromEntityToAUserGroup);
    }

    @Transactional(readOnly = true)
    public Mono<Long> countManagedUsers() {
        return userGroupRepository.count();
    }

    @Transactional(readOnly = true)
    public Mono<UserGroup> getUserGroupWithAuthorities(Long id) {
        return userGroupRepository.findOneWithAuthoritiesById(id);
    }

    public Flux<AdminUserGroupDTO> findByCriteria(UserGroupCriteria criteria, Pageable pageable) {
        return template
            .select(UserGroup.class)
            .matching(Query.query(getCriteria(criteria)).with(pageable))
            .all()
            .map(userGroupMapper::fromEntityToAUserGroup);
    }

    public Mono<Long> countByCriteria(UserGroupCriteria criteria) {
        return template.select(UserGroup.class).matching(Query.query(getCriteria(criteria))).count();
    }

    private Criteria getCriteria(UserGroupCriteria criteria) {
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
        if (criteria.getActive() != null) {
            all = all.and(queryService.buildCriteria(criteria.getActive(), "active"));
        }
        if (criteria.getDetails() != null) {
            all = all.and(queryService.buildStringCriteria(criteria.getDetails(), "details"));
        }
        if (criteria.getType() != null) {
            all = all.and(queryService.buildCriteria(criteria.getType(), "type"));
        }
        return all;
    }
}
