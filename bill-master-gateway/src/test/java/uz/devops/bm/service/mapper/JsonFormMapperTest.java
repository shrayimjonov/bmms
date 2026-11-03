package uz.devops.bm.service.mapper;

import org.junit.jupiter.api.BeforeEach;

class JsonFormMapperTest {

    private JsonFormMapper jsonFormMapper;

    @BeforeEach
    public void setUp() {
        jsonFormMapper = new JsonFormMapperImpl();
    }
}
