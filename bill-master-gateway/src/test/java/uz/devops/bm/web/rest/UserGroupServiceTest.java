package uz.devops.bm.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.SneakyThrows;
import net.bytebuddy.dynamic.scaffold.MethodGraph;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.reactive.server.WebTestClient;
import uz.devops.bm.IntegrationTest;
import uz.devops.bm.domain.Authority;
import uz.devops.bm.domain.UserGroup;
import uz.devops.bm.domain.enumeration.UserGroupType;
import uz.devops.bm.repository.AuthorityRepository;
import uz.devops.bm.repository.UserGroupRepository;
import uz.devops.bm.repository.UserRepository;
import uz.devops.bm.security.AuthoritiesConstants;
import uz.devops.bm.service.EntityManager;
import uz.devops.bm.service.dto.AdminUserGroupDTO;
import uz.devops.bm.service.dto.UserGroupDTO;

/**
 * Integration tests for the {@link UserGroupResource} REST controller.
 */
@IntegrationTest
@AutoConfigureWebTestClient
@WithMockUser
class UserGroupServiceTest {

    @Autowired
    private EntityManager em;

    @Autowired
    private UserGroupRepository userGroupRepository;

    @Autowired
    private UserRepository userRepository;

    private UserGroup userGroup;

    @Autowired
    private AuthorityRepository authorityRepository;

    @AfterEach
    public void cleanup() {
        deleteEntities(em);
    }

    @Autowired
    private WebTestClient webTestClient;

    @BeforeEach
    public void initTest() {
        deleteEntities(em);
        List<Authority> authorities = authorityRepository.findAll().collectList().block();

        userGroup = createEntity(em, authorities);
    }

    @Test
    void insertSqlQueryTest() {
        UserGroup userGroup2 = userGroupRepository.save(userGroup).block();

        Integer integer = userGroupRepository.saveUserGroupAuthority(userGroup2.getId(), "ROLE_BILL_WRITE").block();

        Boolean aBoolean = userGroupRepository.existsUserGroupAuthority(userGroup2.getId(), "ROLE_BILL_WRITE").block();

        assertThat(aBoolean).isTrue();
    }

    public static void deleteEntities(EntityManager em) {
        try {
            em.deleteAll(UserGroup.class).block();
        } catch (Exception e) {
            // It can fail, if other entities are still referring this - it will be removed later.
        }
    }

    public static UserGroup createEntity(EntityManager em, List<Authority> authorities) {
        //        Set<Authority> authoritySet = new HashSet<>(authorities);
        //                authoritySet.add(autho)
        UserGroup userGroup = new UserGroup()
            .name("DEFAULT_NAME")
            .active(true)
            //            .authorities(authoritySet)
            .details("DEFAULT_DETAILS")
            .type(UserGroupType.ADMIN);

        return userGroup;
    }
}
