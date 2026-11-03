//package uz.devops.bm.web.rest;
//
//import static org.assertj.core.api.Assertions.assertThat;
//
//import java.util.HashSet;
//import java.util.List;
//import java.util.Map;
//import java.util.Set;
//import java.util.stream.Collectors;
//import org.apache.commons.lang3.RandomStringUtils;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
//import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
//import org.springframework.http.MediaType;
//import org.springframework.r2dbc.core.DatabaseClient;
//import org.springframework.security.test.context.support.WithMockUser;
//import org.springframework.test.web.reactive.server.WebTestClient;
//import reactor.core.publisher.Flux;
//import uz.devops.bm.IntegrationTest;
//import uz.devops.bm.config.Constants;
//import uz.devops.bm.domain.JsonForm;
//import uz.devops.bm.domain.User;
//import uz.devops.bm.domain.enumeration.JsonFormType;
//import uz.devops.bm.repository.JsonFormRepository;
//import uz.devops.bm.repository.UserRepository;
//import uz.devops.bm.service.dto.JsonFormDTO;
//import uz.devops.bm.service.mapper.JsonFormMapper;
//
//@IntegrationTest
//@AutoConfigureWebTestClient(timeout = "16000000000")
//@WithMockUser(JsonFormResourceTest.ABDURAHIM_USERNAME)
//public class JsonFormResourceTest {
//
//    public static final String DEFAULT_NAME = "name";
//    public static final JsonFormType DEFAULT_TYPE = JsonFormType.FILTER;
//    public static final String DEFAULT_DATA = "data";
//    public static final String DEFAULT_USERNAME = "username";
//    public static final Boolean DEFAULT_ACTIVE = true;
//    public static final String DEFAULT_DESCRIPTION = "desc";
//    private static final String ENTITY_API_URL = "/api/json-forms";
//    public static final String ABDURAHIM_USERNAME = "abdurahim";
//    private static final String DEFAULT_LOGIN = "johndoe";
//    private static final String DEFAULT_FIRSTNAME = "john";
//    private static final String DEFAULT_EMAIL = "johndoe@localhost";
//
//    private static final String DEFAULT_LASTNAME = "doe";
//    private static final String DEFAULT_IMAGEURL = "http://placehold.it/50x50";
//    private static final String DEFAULT_LANGKEY = "en";
//
//    @Autowired
//    private JsonFormRepository jsonFormRepository;
//
//    @Autowired
//    private UserRepository userRepository;
//
//    @Autowired
//    private JsonFormMapper jsonFormMapper;
//
//    @Autowired
//    private WebTestClient webTestClient;
//
//    @Autowired
//    private R2dbcEntityTemplate entityTemplate;
//
//    @Autowired
//    private DatabaseClient db;
//
//    @Test
//    void createJsonFormTest() throws Exception {
//        int databaseSizeBeforeCreate = jsonFormRepository.findAll().collectList().block().size();
//        // Create the JsonForm
//        JsonForm jsonForm = createJsonForm();
//        JsonFormDTO jsonFormDTO = jsonFormMapper.toDto(jsonForm);
//        webTestClient
//            .post()
//            .uri(ENTITY_API_URL)
//            .contentType(MediaType.APPLICATION_JSON)
//            .bodyValue(TestUtil.convertObjectToJsonBytes(jsonFormDTO))
//            .exchange()
//            .expectStatus()
//            .isCreated();
//
//        List<JsonForm> jsonFormList = jsonFormRepository.findAll().collectList().block();
//        assertThat(jsonFormList).hasSize(databaseSizeBeforeCreate + 1);
//        JsonForm testJsonForm = jsonFormList.get(jsonFormList.size() - 1);
//        assertThat(testJsonForm.getName()).isEqualTo(DEFAULT_NAME);
//        assertThat(testJsonForm.getType()).isEqualTo(DEFAULT_TYPE);
//        assertThat(testJsonForm.getData()).isEqualTo(DEFAULT_DATA);
//        assertThat(testJsonForm.getUsername()).isEqualTo(ABDURAHIM_USERNAME);
//        assertThat(testJsonForm.getActive()).isEqualTo(DEFAULT_ACTIVE);
//        assertThat(testJsonForm.getDescription()).isEqualTo(DEFAULT_DESCRIPTION);
//    }
//
//    @Test
//    void findByCriteria() throws Exception {
//        int databaseSizeBeforeCreate = jsonFormRepository.findAll().collectList().block().size();
//        // Create the JsonForm
//        JsonForm jsonForm = createJsonForm();
//        JsonForm jsonForm1 = createJsonForm("abdurahim");
//        jsonFormRepository.save(jsonForm).block();
//        jsonFormRepository.save(jsonForm1).block();
//
//        webTestClient
//            .get()
//            .uri(ENTITY_API_URL + "/by-criteria" + "/?username.equals=" + jsonForm1.getUsername())
//            //                .attribute("criteria", TestUtil.convertObjectToJsonBytes(criteria))
//            .exchange()
//            .expectBody()
//            .consumeWith(System.out::println);
//
//        List<JsonForm> jsonFormList = jsonFormRepository.findAll().collectList().block();
//        JsonForm testJsonForm = jsonFormList.get(jsonFormList.size() - 1);
//        assertThat(testJsonForm.getName()).isEqualTo(jsonForm1.getName());
//        assertThat(testJsonForm.getType()).isEqualTo(DEFAULT_TYPE);
//        assertThat(testJsonForm.getData()).isEqualTo(DEFAULT_DATA);
//        assertThat(testJsonForm.getUsername()).isEqualTo(jsonForm1.getUsername());
//        assertThat(testJsonForm.getActive()).isEqualTo(DEFAULT_ACTIVE);
//        assertThat(testJsonForm.getDescription()).isEqualTo(DEFAULT_DESCRIPTION);
//    }
//
//    @Test
//    void getAllJsonForms() {
//        int databaseSizeBeforeCreate = jsonFormRepository.findAll().collectList().block().size();
//        JsonForm jsonForm = createJsonForm();
//        Set<User> users = new HashSet<>();
//        User user = new User();
//        user.setLogin(DEFAULT_LOGIN + RandomStringUtils.randomAlphabetic(5));
////        user.setPassword(RandomStringUtils.random(60));
//        user.setActivated(true);
//        user.setEmail(RandomStringUtils.randomAlphabetic(5) + DEFAULT_EMAIL);
//        user.setFirstName(DEFAULT_FIRSTNAME);
//        user.setLastName(DEFAULT_LASTNAME);
//        user.setImageUrl(DEFAULT_IMAGEURL);
//        user.setLangKey(DEFAULT_LANGKEY);
//        user.setCreatedBy(Constants.SYSTEM);
//
//        User user1 = new User();
//        user1.setLogin(DEFAULT_LOGIN + RandomStringUtils.randomAlphabetic(5));
////        user1.setPassword(RandomStringUtils.random(60));
//        user1.setActivated(true);
//        user1.setEmail(RandomStringUtils.randomAlphabetic(5) + DEFAULT_EMAIL);
//        user1.setFirstName(DEFAULT_FIRSTNAME + "1");
//        user1.setLastName(DEFAULT_LASTNAME + "1");
//        user1.setImageUrl(DEFAULT_IMAGEURL + "1");
//        user1.setLangKey(DEFAULT_LANGKEY + "1");
//        user1.setCreatedBy(Constants.SYSTEM + "1");
//
//        user = userRepository.save(user).block();
//        user1 = userRepository.save(user1).block();
//
//        users.add(user);
//        users.add(user1);
//        jsonForm.setUsers(users);
//        jsonForm = jsonFormRepository.save(jsonForm).block();
//        //        userRepository
//        //            .findAllByJsonFormId(jsonForm.getId())
//        //            .flatMap(
//        //                user1 -> {
//        //                    System.out.println(user1);
//        //                    return Flux.empty();
//        //                }
//        //            );
//        Flux<Map<String, Object>> fetch = db
//            .sql(
//                "SELECT ju.id, login, password_hash, first_name, last_name, email, image_url, activated, lang_key, activation_key, reset_key, created_by, created_date, reset_date, last_modified_by, last_modified_date, phone_number FROM rel_json_form__user rjfu,jhi_user ju where rjfu.json_form_id=" +
//                jsonForm.getId()
//            )
//            .fetch()
//            .all();
//        System.out.println("salom");
//        //        List<Map<String, Object>> listMono = fetch.collect(Collectors.toSet()).block();
//        //        System.out.println(listMono);
//        //        userRepository
//        //            .findAllRelJsonFormUser()
//        //            .flatMap(
//        //                user1 -> {
//        //                    System.out.println(user1);
//        //                    return Flux.empty();
//        //                }
//        //            );
//        System.out.println(jsonForm);
//        webTestClient.get().uri(ENTITY_API_URL).exchange().expectBody().consumeWith(System.out::println);
//        List<JsonForm> jsonFormList = jsonFormRepository.findAllWithEagerRelationships().collectList().block();
//        assertThat(jsonFormList).isNotNull();
//        assertThat(jsonFormList).isNotEmpty();
//    }
//
//    public JsonForm createJsonForm() {
//        return new JsonForm()
//            .name(DEFAULT_NAME)
//            .type(DEFAULT_TYPE)
//            .data(DEFAULT_DATA)
//            .username(DEFAULT_USERNAME)
//            .active(DEFAULT_ACTIVE)
//            .description(DEFAULT_DESCRIPTION);
//    }
//
//    public JsonForm createJsonForm(String username) {
//        return new JsonForm()
//            .name(username)
//            .type(DEFAULT_TYPE)
//            .data(DEFAULT_DATA)
//            .username(username)
//            .active(DEFAULT_ACTIVE)
//            .description(DEFAULT_DESCRIPTION);
//    }
//}
