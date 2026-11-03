package uz.devops.bm.service.mapper;

import org.junit.jupiter.api.BeforeEach;

class UserGroupMapperTest {

    private UserGroupMapper userGroupMapper;

    @BeforeEach
    public void setUp() {
        userGroupMapper = new UserGroupMapperImpl();
    }
}
