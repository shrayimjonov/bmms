package uz.devops.bm.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;

import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.reactive.server.WebTestClient;
import uz.devops.bm.IntegrationTest;
import uz.devops.bm.domain.Authority;
import uz.devops.bm.domain.UserGroup;
import uz.devops.bm.domain.enumeration.UserGroupType;
import uz.devops.bm.repository.UserGroupRepository;
import uz.devops.bm.service.EntityManager;
import uz.devops.bm.service.dto.AdminUserDTO;
import uz.devops.bm.service.dto.AdminUserGroupDTO;
import uz.devops.bm.service.dto.UserGroupDTO;
import uz.devops.bm.service.mapper.UserGroupMapper;

/**
 * Integration tests for the {@link UserGroupResource} REST controller.
 */
@IntegrationTest
@AutoConfigureWebTestClient
@WithMockUser
class UserGroupResourceIT {

    private static final String DEFAULT_NAME = "AAAAAAAAAA";
    private static final String UPDATED_NAME = "BBBBBBBBBB";

    private static final Boolean DEFAULT_ACTIVE = false;
    private static final Boolean UPDATED_ACTIVE = true;

    private static final String DEFAULT_DETAILS = "AAAAAAAAAA";
    private static final String UPDATED_DETAILS = "BBBBBBBBBB";

    private static final UserGroupType DEFAULT_TYPE = UserGroupType.ADMIN;
    private static final UserGroupType UPDATED_TYPE = UserGroupType.AGENT;

    private static final String ENTITY_API_URL = "/api/user-groups";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong count = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private UserGroupRepository userGroupRepository;

    @Autowired
    private UserGroupMapper userGroupMapper;

    @Autowired
    private EntityManager em;

    @Autowired
    private WebTestClient webTestClient;

    private UserGroup userGroup;

    /**
     * Create an entity for this test.
     * <p>
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static UserGroup createEntity(EntityManager em) {
        Set<Authority> authoritySet = new HashSet<>();
        //        authoritySet.add()
        UserGroup userGroup = new UserGroup()
            .name(DEFAULT_NAME)
            .active(DEFAULT_ACTIVE)
            //            .authorities()
            .details(DEFAULT_DETAILS)
            .type(DEFAULT_TYPE);

        return userGroup;
    }

    /**
     * Create an updated entity for this test.
     * <p>
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static UserGroup createUpdatedEntity(EntityManager em) {
        UserGroup userGroup = new UserGroup().name(UPDATED_NAME).active(UPDATED_ACTIVE).details(UPDATED_DETAILS).type(UPDATED_TYPE);
        return userGroup;
    }

    public static void deleteEntities(EntityManager em) {
        try {
            em.deleteAll(UserGroup.class).block();
        } catch (Exception e) {
            // It can fail, if other entities are still referring this - it will be removed later.
        }
    }

    @AfterEach
    public void cleanup() {
        deleteEntities(em);
    }

    @BeforeEach
    public void initTest() {
        deleteEntities(em);
        userGroup = createEntity(em);
    }

    @Test
    void createUserGroup() throws Exception {
        int databaseSizeBeforeCreate = userGroupRepository.findAll().collectList().block().size();
        // Create the UserGroup
        UserGroupDTO userGroupDTO = userGroupMapper.toDto(userGroup);
        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(userGroupDTO))
            .exchange()
            .expectStatus()
            .isCreated();

        // Validate the UserGroup in the database
        List<UserGroup> userGroupList = userGroupRepository.findAll().collectList().block();
        assertThat(userGroupList).hasSize(databaseSizeBeforeCreate + 1);
        UserGroup testUserGroup = userGroupList.get(userGroupList.size() - 1);
        assertThat(testUserGroup.getName()).isEqualTo(DEFAULT_NAME);
        assertThat(testUserGroup.getActive()).isEqualTo(DEFAULT_ACTIVE);
        assertThat(testUserGroup.getDetails()).isEqualTo(DEFAULT_DETAILS);
        assertThat(testUserGroup.getType()).isEqualTo(DEFAULT_TYPE);
    }

    @Test
    void createUserGroupWithExistingId() throws Exception {
        // Create the UserGroup with an existing ID
        userGroup.setId(1L);
        UserGroupDTO userGroupDTO = userGroupMapper.toDto(userGroup);

        int databaseSizeBeforeCreate = userGroupRepository.findAll().collectList().block().size();

        // An entity with an existing ID cannot be created, so this API call must fail
        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(userGroupDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the UserGroup in the database
        List<UserGroup> userGroupList = userGroupRepository.findAll().collectList().block();
        assertThat(userGroupList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    void checkNameIsRequired() throws Exception {
        int databaseSizeBeforeTest = userGroupRepository.findAll().collectList().block().size();
        // set the field null
        userGroup.setName(null);

        // Create the UserGroup, which fails.
        UserGroupDTO userGroupDTO = userGroupMapper.toDto(userGroup);

        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(userGroupDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        List<UserGroup> userGroupList = userGroupRepository.findAll().collectList().block();
        assertThat(userGroupList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    void checkActiveIsRequired() throws Exception {
        int databaseSizeBeforeTest = userGroupRepository.findAll().collectList().block().size();
        // set the field null
        userGroup.setActive(null);

        // Create the UserGroup, which fails.
        UserGroupDTO userGroupDTO = userGroupMapper.toDto(userGroup);

        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(userGroupDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        List<UserGroup> userGroupList = userGroupRepository.findAll().collectList().block();
        assertThat(userGroupList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    void getAllUserGroups() {
        // Initialize the database
        userGroupRepository.save(userGroup).block();

        // Get all the userGroupList
        webTestClient
            .get()
            .uri(ENTITY_API_URL + "?sort=id,desc")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isOk()
            .expectHeader()
            .contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.[*].id")
            .value(hasItem(userGroup.getId().intValue()))
            .jsonPath("$.[*].name")
            .value(hasItem(DEFAULT_NAME))
            .jsonPath("$.[*].active")
            .value(hasItem(DEFAULT_ACTIVE.booleanValue()))
            .jsonPath("$.[*].details")
            .value(hasItem(DEFAULT_DETAILS))
            .jsonPath("$.[*].type")
            .value(hasItem(DEFAULT_TYPE.toString()));
    }

    @Test
    void getUserGroup() {
        // Initialize the database
        userGroupRepository.save(userGroup).block();

        // Get the userGroup
        webTestClient
            .get()
            .uri(ENTITY_API_URL_ID, userGroup.getId())
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isOk()
            .expectHeader()
            .contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.id")
            .value(is(userGroup.getId().intValue()))
            .jsonPath("$.name")
            .value(is(DEFAULT_NAME))
            .jsonPath("$.active")
            .value(is(DEFAULT_ACTIVE.booleanValue()))
            .jsonPath("$.details")
            .value(is(DEFAULT_DETAILS))
            .jsonPath("$.type")
            .value(is(DEFAULT_TYPE.toString()));
    }

    @Test
    void getNonExistingUserGroup() {
        // Get the userGroup
        webTestClient
            .get()
            .uri(ENTITY_API_URL_ID, Long.MAX_VALUE)
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isNotFound();
    }

    @Test
    void putNewUserGroup() throws Exception {
        // Initialize the database
        userGroupRepository.save(userGroup).block();

        int databaseSizeBeforeUpdate = userGroupRepository.findAll().collectList().block().size();

        // Update the userGroup
        UserGroup updatedUserGroup = userGroupRepository.findById(userGroup.getId()).block();
        updatedUserGroup.name(UPDATED_NAME).active(UPDATED_ACTIVE).details(UPDATED_DETAILS).type(UPDATED_TYPE);
        UserGroupDTO userGroupDTO = userGroupMapper.toDto(updatedUserGroup);

        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, userGroupDTO.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(userGroupDTO))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the UserGroup in the database
        List<UserGroup> userGroupList = userGroupRepository.findAll().collectList().block();
        assertThat(userGroupList).hasSize(databaseSizeBeforeUpdate);
        UserGroup testUserGroup = userGroupList.get(userGroupList.size() - 1);
        assertThat(testUserGroup.getName()).isEqualTo(UPDATED_NAME);
        assertThat(testUserGroup.getActive()).isEqualTo(UPDATED_ACTIVE);
        assertThat(testUserGroup.getDetails()).isEqualTo(UPDATED_DETAILS);
        assertThat(testUserGroup.getType()).isEqualTo(UPDATED_TYPE);
    }

    @Test
    void putNonExistingUserGroup() throws Exception {
        int databaseSizeBeforeUpdate = userGroupRepository.findAll().collectList().block().size();
        userGroup.setId(count.incrementAndGet());

        // Create the UserGroup
        UserGroupDTO userGroupDTO = userGroupMapper.toDto(userGroup);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, userGroupDTO.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(userGroupDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the UserGroup in the database
        List<UserGroup> userGroupList = userGroupRepository.findAll().collectList().block();
        assertThat(userGroupList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    void putWithIdMismatchUserGroup() throws Exception {
        int databaseSizeBeforeUpdate = userGroupRepository.findAll().collectList().block().size();
        userGroup.setId(count.incrementAndGet());

        // Create the UserGroup
        UserGroupDTO userGroupDTO = userGroupMapper.toDto(userGroup);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, count.incrementAndGet())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(userGroupDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the UserGroup in the database
        List<UserGroup> userGroupList = userGroupRepository.findAll().collectList().block();
        assertThat(userGroupList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    void putWithMissingIdPathParamUserGroup() throws Exception {
        int databaseSizeBeforeUpdate = userGroupRepository.findAll().collectList().block().size();
        userGroup.setId(count.incrementAndGet());

        // Create the UserGroup
        UserGroupDTO userGroupDTO = userGroupMapper.toDto(userGroup);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(userGroupDTO))
            .exchange()
            .expectStatus()
            .isEqualTo(405);

        // Validate the UserGroup in the database
        List<UserGroup> userGroupList = userGroupRepository.findAll().collectList().block();
        assertThat(userGroupList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    void partialUpdateUserGroupWithPatch() throws Exception {
        // Initialize the database
        userGroupRepository.save(userGroup).block();

        int databaseSizeBeforeUpdate = userGroupRepository.findAll().collectList().block().size();

        // Update the userGroup using partial update
        UserGroup partialUpdatedUserGroup = new UserGroup();
        partialUpdatedUserGroup.setId(userGroup.getId());

        partialUpdatedUserGroup.active(UPDATED_ACTIVE).details(UPDATED_DETAILS).type(UPDATED_TYPE);

        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, partialUpdatedUserGroup.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(partialUpdatedUserGroup))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the UserGroup in the database
        List<UserGroup> userGroupList = userGroupRepository.findAll().collectList().block();
        assertThat(userGroupList).hasSize(databaseSizeBeforeUpdate);
        UserGroup testUserGroup = userGroupList.get(userGroupList.size() - 1);
        assertThat(testUserGroup.getName()).isEqualTo(DEFAULT_NAME);
        assertThat(testUserGroup.getActive()).isEqualTo(UPDATED_ACTIVE);
        assertThat(testUserGroup.getDetails()).isEqualTo(UPDATED_DETAILS);
        assertThat(testUserGroup.getType()).isEqualTo(UPDATED_TYPE);
    }

    @Test
    void fullUpdateUserGroupWithPatch() throws Exception {
        // Initialize the database
        userGroupRepository.save(userGroup).block();

        int databaseSizeBeforeUpdate = userGroupRepository.findAll().collectList().block().size();

        // Update the userGroup using partial update
        UserGroup partialUpdatedUserGroup = new UserGroup();
        partialUpdatedUserGroup.setId(userGroup.getId());

        partialUpdatedUserGroup.name(UPDATED_NAME).active(UPDATED_ACTIVE).details(UPDATED_DETAILS).type(UPDATED_TYPE);

        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, partialUpdatedUserGroup.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(partialUpdatedUserGroup))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the UserGroup in the database
        List<UserGroup> userGroupList = userGroupRepository.findAll().collectList().block();
        assertThat(userGroupList).hasSize(databaseSizeBeforeUpdate);
        UserGroup testUserGroup = userGroupList.get(userGroupList.size() - 1);
        assertThat(testUserGroup.getName()).isEqualTo(UPDATED_NAME);
        assertThat(testUserGroup.getActive()).isEqualTo(UPDATED_ACTIVE);
        assertThat(testUserGroup.getDetails()).isEqualTo(UPDATED_DETAILS);
        assertThat(testUserGroup.getType()).isEqualTo(UPDATED_TYPE);
    }

    @Test
    void patchNonExistingUserGroup() throws Exception {
        int databaseSizeBeforeUpdate = userGroupRepository.findAll().collectList().block().size();
        userGroup.setId(count.incrementAndGet());

        // Create the UserGroup
        UserGroupDTO userGroupDTO = userGroupMapper.toDto(userGroup);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, userGroupDTO.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(userGroupDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the UserGroup in the database
        List<UserGroup> userGroupList = userGroupRepository.findAll().collectList().block();
        assertThat(userGroupList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    void patchWithIdMismatchUserGroup() throws Exception {
        int databaseSizeBeforeUpdate = userGroupRepository.findAll().collectList().block().size();
        userGroup.setId(count.incrementAndGet());

        // Create the UserGroup
        UserGroupDTO userGroupDTO = userGroupMapper.toDto(userGroup);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, count.incrementAndGet())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(userGroupDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the UserGroup in the database
        List<UserGroup> userGroupList = userGroupRepository.findAll().collectList().block();
        assertThat(userGroupList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    void patchWithMissingIdPathParamUserGroup() throws Exception {
        int databaseSizeBeforeUpdate = userGroupRepository.findAll().collectList().block().size();
        userGroup.setId(count.incrementAndGet());

        // Create the UserGroup
        UserGroupDTO userGroupDTO = userGroupMapper.toDto(userGroup);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(userGroupDTO))
            .exchange()
            .expectStatus()
            .isEqualTo(405);

        // Validate the UserGroup in the database
        List<UserGroup> userGroupList = userGroupRepository.findAll().collectList().block();
        assertThat(userGroupList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    void deleteUserGroup() {
        // Initialize the database
        userGroupRepository.save(userGroup).block();

        int databaseSizeBeforeDelete = userGroupRepository.findAll().collectList().block().size();

        // Delete the userGroup
        webTestClient
            .delete()
            .uri(ENTITY_API_URL_ID, userGroup.getId())
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isNoContent();

        // Validate the database contains one less item
        List<UserGroup> userGroupList = userGroupRepository.findAll().collectList().block();
        assertThat(userGroupList).hasSize(databaseSizeBeforeDelete - 1);
    }

    @Test
    void findUsersByCriteriaAndSorted() throws Exception {
        // Initialize the database
        userGroupRepository.save(userGroup).block();

        userGroup.setName("test1");
        userGroup.setDetails("test1 details");

        userGroupRepository.save(userGroup).block();

        userGroup.setName("test2");
        userGroup.setDetails("test2 details");

        userGroupRepository.save(userGroup).block();

        var foundUserGroup = webTestClient
            .get()
            .uri("/api/user-groups/by-criteria?sort=id,DESC&id.equals=" + userGroup.getId())
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isOk()
            .returnResult(AdminUserGroupDTO.class)
            .getResponseBody()
            .blockFirst();

        assertThat(foundUserGroup).isNotNull();
        assertThat(foundUserGroup.getName()).isEqualTo(userGroup.getName());
    }

    @Test
    void findUsersByLoginIn() {
        // Initialize the database
        userGroupRepository.save(userGroup).block();

        userGroup.setName("test1");
        userGroup.setDetails("test1 details");

        userGroupRepository.save(userGroup).block();

        userGroup.setName("test2");
        userGroup.setDetails("test2 details");

        userGroupRepository.save(userGroup).block();

        var list = userGroupRepository.findAll().collectList().block();

        System.out.println(list);

        webTestClient
            .get()
            .uri("/api/user-groups/by-criteria?sort=name,ASC&name.in=test2")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isOk()
            .expectHeader()
            .value("X-Total-Count", total -> assertThat(total).isEqualTo("1"))
            .expectBody()
            .jsonPath("$")
            .isArray()
            .consumeWith(System.out::println)
            .jsonPath("$[0].name")
            .isEqualTo("test2");
    }
}
