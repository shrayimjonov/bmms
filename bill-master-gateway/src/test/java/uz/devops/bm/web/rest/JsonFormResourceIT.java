package uz.devops.bm.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import uz.devops.bm.IntegrationTest;
import uz.devops.bm.domain.JsonForm;
import uz.devops.bm.domain.enumeration.JsonFormType;
import uz.devops.bm.repository.JsonFormRepository;
import uz.devops.bm.service.EntityManager;
import uz.devops.bm.service.JsonFormService;
import uz.devops.bm.service.dto.JsonFormDTO;
import uz.devops.bm.service.mapper.JsonFormMapper;

/**
 * Integration tests for the {@link JsonFormResource} REST controller.
 */
@IntegrationTest
@ExtendWith(MockitoExtension.class)
@AutoConfigureWebTestClient
@WithMockUser
class JsonFormResourceIT {

    private static final String DEFAULT_NAME = "AAAAAAAAAA";
    private static final String UPDATED_NAME = "BBBBBBBBBB";

    private static final JsonFormType DEFAULT_TYPE = JsonFormType.FILTER;
    private static final JsonFormType UPDATED_TYPE = JsonFormType.FILTER;

    private static final String DEFAULT_DATA = "AAAAAAAAAA";
    private static final String UPDATED_DATA = "BBBBBBBBBB";

    private static final String DEFAULT_USERNAME = "AAAAAAAAAA";
    private static final String UPDATED_USERNAME = "BBBBBBBBBB";

    private static final Boolean DEFAULT_ACTIVE = false;
    private static final Boolean UPDATED_ACTIVE = true;

    private static final String DEFAULT_DESCRIPTION = "AAAAAAAAAA";
    private static final String UPDATED_DESCRIPTION = "BBBBBBBBBB";

    private static final String ENTITY_API_URL = "/api/json-forms";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong count = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private JsonFormRepository jsonFormRepository;

    @Mock
    private JsonFormRepository jsonFormRepositoryMock;

    @Autowired
    private JsonFormMapper jsonFormMapper;

    @Mock
    private JsonFormService jsonFormServiceMock;

    @Autowired
    private EntityManager em;

    @Autowired
    private WebTestClient webTestClient;

    private JsonForm jsonForm;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static JsonForm createEntity(EntityManager em) {
        JsonForm jsonForm = new JsonForm()
            .name(DEFAULT_NAME)
            .type(DEFAULT_TYPE)
            .data(DEFAULT_DATA)
            .username(DEFAULT_USERNAME)
            .active(DEFAULT_ACTIVE)
            .description(DEFAULT_DESCRIPTION);
        return jsonForm;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static JsonForm createUpdatedEntity(EntityManager em) {
        JsonForm jsonForm = new JsonForm()
            .name(UPDATED_NAME)
            .type(UPDATED_TYPE)
            .data(UPDATED_DATA)
            .username(UPDATED_USERNAME)
            .active(UPDATED_ACTIVE)
            .description(UPDATED_DESCRIPTION);
        return jsonForm;
    }

    public static void deleteEntities(EntityManager em) {
        try {
            em.deleteAll("rel_json_form__user").block();
            em.deleteAll(JsonForm.class).block();
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
        jsonForm = createEntity(em);
    }

    @Test
    void createJsonForm() throws Exception {
        int databaseSizeBeforeCreate = jsonFormRepository.findAll().collectList().block().size();
        // Create the JsonForm
        JsonFormDTO jsonFormDTO = jsonFormMapper.toDto(jsonForm);
        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(jsonFormDTO))
            .exchange()
            .expectStatus()
            .isCreated();

        // Validate the JsonForm in the database
        List<JsonForm> jsonFormList = jsonFormRepository.findAll().collectList().block();
        assertThat(jsonFormList).hasSize(databaseSizeBeforeCreate + 1);
        JsonForm testJsonForm = jsonFormList.get(jsonFormList.size() - 1);
        assertThat(testJsonForm.getName()).isEqualTo(DEFAULT_NAME);
        assertThat(testJsonForm.getType()).isEqualTo(DEFAULT_TYPE);
        assertThat(testJsonForm.getData()).isEqualTo(DEFAULT_DATA);
        assertThat(testJsonForm.getUsername()).isEqualTo(DEFAULT_USERNAME);
        assertThat(testJsonForm.getActive()).isEqualTo(DEFAULT_ACTIVE);
        assertThat(testJsonForm.getDescription()).isEqualTo(DEFAULT_DESCRIPTION);
    }

    @Test
    void createJsonFormWithExistingId() throws Exception {
        // Create the JsonForm with an existing ID
        jsonForm.setId(1L);
        JsonFormDTO jsonFormDTO = jsonFormMapper.toDto(jsonForm);

        int databaseSizeBeforeCreate = jsonFormRepository.findAll().collectList().block().size();

        // An entity with an existing ID cannot be created, so this API call must fail
        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(jsonFormDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the JsonForm in the database
        List<JsonForm> jsonFormList = jsonFormRepository.findAll().collectList().block();
        assertThat(jsonFormList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    void checkNameIsRequired() throws Exception {
        int databaseSizeBeforeTest = jsonFormRepository.findAll().collectList().block().size();
        // set the field null
        jsonForm.setName(null);

        // Create the JsonForm, which fails.
        JsonFormDTO jsonFormDTO = jsonFormMapper.toDto(jsonForm);

        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(jsonFormDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        List<JsonForm> jsonFormList = jsonFormRepository.findAll().collectList().block();
        assertThat(jsonFormList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    void checkTypeIsRequired() throws Exception {
        int databaseSizeBeforeTest = jsonFormRepository.findAll().collectList().block().size();
        // set the field null
        jsonForm.setType(null);

        // Create the JsonForm, which fails.
        JsonFormDTO jsonFormDTO = jsonFormMapper.toDto(jsonForm);

        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(jsonFormDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        List<JsonForm> jsonFormList = jsonFormRepository.findAll().collectList().block();
        assertThat(jsonFormList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    void getAllJsonForms() {
        // Initialize the database
        jsonFormRepository.save(jsonForm).block();

        // Get all the jsonFormList
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
            .value(hasItem(jsonForm.getId().intValue()))
            .jsonPath("$.[*].name")
            .value(hasItem(DEFAULT_NAME))
            .jsonPath("$.[*].type")
            .value(hasItem(DEFAULT_TYPE.toString()))
            .jsonPath("$.[*].data")
            .value(hasItem(DEFAULT_DATA.toString()))
            .jsonPath("$.[*].username")
            .value(hasItem(DEFAULT_USERNAME))
            .jsonPath("$.[*].active")
            .value(hasItem(DEFAULT_ACTIVE.booleanValue()))
            .jsonPath("$.[*].description")
            .value(hasItem(DEFAULT_DESCRIPTION));
    }

    @SuppressWarnings({ "unchecked" })
    void getAllJsonFormsWithEagerRelationshipsIsEnabled() {
        when(jsonFormServiceMock.findAllWithEagerRelationships(any())).thenReturn(Flux.empty());

        webTestClient.get().uri(ENTITY_API_URL + "?eagerload=true").exchange().expectStatus().isOk();

        verify(jsonFormServiceMock, times(1)).findAllWithEagerRelationships(any());
    }

    @SuppressWarnings({ "unchecked" })
    void getAllJsonFormsWithEagerRelationshipsIsNotEnabled() {
        when(jsonFormServiceMock.findAllWithEagerRelationships(any())).thenReturn(Flux.empty());

        webTestClient.get().uri(ENTITY_API_URL + "?eagerload=true").exchange().expectStatus().isOk();

        verify(jsonFormServiceMock, times(1)).findAllWithEagerRelationships(any());
    }

    @Test
    void getJsonForm() {
        // Initialize the database
        jsonFormRepository.save(jsonForm).block();

        // Get the jsonForm
        webTestClient
            .get()
            .uri(ENTITY_API_URL_ID, jsonForm.getId())
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isOk()
            .expectHeader()
            .contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.id")
            .value(is(jsonForm.getId().intValue()))
            .jsonPath("$.name")
            .value(is(DEFAULT_NAME))
            .jsonPath("$.type")
            .value(is(DEFAULT_TYPE.toString()))
            .jsonPath("$.data")
            .value(is(DEFAULT_DATA.toString()))
            .jsonPath("$.username")
            .value(is(DEFAULT_USERNAME))
            .jsonPath("$.active")
            .value(is(DEFAULT_ACTIVE.booleanValue()))
            .jsonPath("$.description")
            .value(is(DEFAULT_DESCRIPTION));
    }

    @Test
    void getNonExistingJsonForm() {
        // Get the jsonForm
        webTestClient
            .get()
            .uri(ENTITY_API_URL_ID, Long.MAX_VALUE)
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isNotFound();
    }

    @Test
    void putNewJsonForm() throws Exception {
        // Initialize the database
        jsonFormRepository.save(jsonForm).block();

        int databaseSizeBeforeUpdate = jsonFormRepository.findAll().collectList().block().size();

        // Update the jsonForm
        JsonForm updatedJsonForm = jsonFormRepository.findById(jsonForm.getId()).block();
        updatedJsonForm
            .name(UPDATED_NAME)
            .type(UPDATED_TYPE)
            .data(UPDATED_DATA)
            .username(UPDATED_USERNAME)
            .active(UPDATED_ACTIVE)
            .description(UPDATED_DESCRIPTION);
        JsonFormDTO jsonFormDTO = jsonFormMapper.toDto(updatedJsonForm);

        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, jsonFormDTO.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(jsonFormDTO))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the JsonForm in the database
        List<JsonForm> jsonFormList = jsonFormRepository.findAll().collectList().block();
        assertThat(jsonFormList).hasSize(databaseSizeBeforeUpdate);
        JsonForm testJsonForm = jsonFormList.get(jsonFormList.size() - 1);
        assertThat(testJsonForm.getName()).isEqualTo(UPDATED_NAME);
        assertThat(testJsonForm.getType()).isEqualTo(UPDATED_TYPE);
        assertThat(testJsonForm.getData()).isEqualTo(UPDATED_DATA);
        assertThat(testJsonForm.getUsername()).isEqualTo(UPDATED_USERNAME);
        assertThat(testJsonForm.getActive()).isEqualTo(UPDATED_ACTIVE);
        assertThat(testJsonForm.getDescription()).isEqualTo(UPDATED_DESCRIPTION);
    }

    @Test
    void putNonExistingJsonForm() throws Exception {
        int databaseSizeBeforeUpdate = jsonFormRepository.findAll().collectList().block().size();
        jsonForm.setId(count.incrementAndGet());

        // Create the JsonForm
        JsonFormDTO jsonFormDTO = jsonFormMapper.toDto(jsonForm);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, jsonFormDTO.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(jsonFormDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the JsonForm in the database
        List<JsonForm> jsonFormList = jsonFormRepository.findAll().collectList().block();
        assertThat(jsonFormList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    void putWithIdMismatchJsonForm() throws Exception {
        int databaseSizeBeforeUpdate = jsonFormRepository.findAll().collectList().block().size();
        jsonForm.setId(count.incrementAndGet());

        // Create the JsonForm
        JsonFormDTO jsonFormDTO = jsonFormMapper.toDto(jsonForm);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, count.incrementAndGet())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(jsonFormDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the JsonForm in the database
        List<JsonForm> jsonFormList = jsonFormRepository.findAll().collectList().block();
        assertThat(jsonFormList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    void putWithMissingIdPathParamJsonForm() throws Exception {
        int databaseSizeBeforeUpdate = jsonFormRepository.findAll().collectList().block().size();
        jsonForm.setId(count.incrementAndGet());

        // Create the JsonForm
        JsonFormDTO jsonFormDTO = jsonFormMapper.toDto(jsonForm);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(jsonFormDTO))
            .exchange()
            .expectStatus()
            .isEqualTo(405);

        // Validate the JsonForm in the database
        List<JsonForm> jsonFormList = jsonFormRepository.findAll().collectList().block();
        assertThat(jsonFormList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    void partialUpdateJsonFormWithPatch() throws Exception {
        // Initialize the database
        jsonFormRepository.save(jsonForm).block();

        int databaseSizeBeforeUpdate = jsonFormRepository.findAll().collectList().block().size();

        // Update the jsonForm using partial update
        JsonForm partialUpdatedJsonForm = new JsonForm();
        partialUpdatedJsonForm.setId(jsonForm.getId());

        partialUpdatedJsonForm.data(UPDATED_DATA).username(UPDATED_USERNAME).description(UPDATED_DESCRIPTION);

        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, partialUpdatedJsonForm.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(partialUpdatedJsonForm))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the JsonForm in the database
        List<JsonForm> jsonFormList = jsonFormRepository.findAll().collectList().block();
        assertThat(jsonFormList).hasSize(databaseSizeBeforeUpdate);
        JsonForm testJsonForm = jsonFormList.get(jsonFormList.size() - 1);
        assertThat(testJsonForm.getName()).isEqualTo(DEFAULT_NAME);
        assertThat(testJsonForm.getType()).isEqualTo(DEFAULT_TYPE);
        assertThat(testJsonForm.getData()).isEqualTo(UPDATED_DATA);
        assertThat(testJsonForm.getUsername()).isEqualTo(UPDATED_USERNAME);
        assertThat(testJsonForm.getActive()).isEqualTo(DEFAULT_ACTIVE);
        assertThat(testJsonForm.getDescription()).isEqualTo(UPDATED_DESCRIPTION);
    }

    @Test
    void fullUpdateJsonFormWithPatch() throws Exception {
        // Initialize the database
        jsonFormRepository.save(jsonForm).block();

        int databaseSizeBeforeUpdate = jsonFormRepository.findAll().collectList().block().size();

        // Update the jsonForm using partial update
        JsonForm partialUpdatedJsonForm = new JsonForm();
        partialUpdatedJsonForm.setId(jsonForm.getId());

        partialUpdatedJsonForm
            .name(UPDATED_NAME)
            .type(UPDATED_TYPE)
            .data(UPDATED_DATA)
            .username(UPDATED_USERNAME)
            .active(UPDATED_ACTIVE)
            .description(UPDATED_DESCRIPTION);

        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, partialUpdatedJsonForm.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(partialUpdatedJsonForm))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the JsonForm in the database
        List<JsonForm> jsonFormList = jsonFormRepository.findAll().collectList().block();
        assertThat(jsonFormList).hasSize(databaseSizeBeforeUpdate);
        JsonForm testJsonForm = jsonFormList.get(jsonFormList.size() - 1);
        assertThat(testJsonForm.getName()).isEqualTo(UPDATED_NAME);
        assertThat(testJsonForm.getType()).isEqualTo(UPDATED_TYPE);
        assertThat(testJsonForm.getData()).isEqualTo(UPDATED_DATA);
        assertThat(testJsonForm.getUsername()).isEqualTo(UPDATED_USERNAME);
        assertThat(testJsonForm.getActive()).isEqualTo(UPDATED_ACTIVE);
        assertThat(testJsonForm.getDescription()).isEqualTo(UPDATED_DESCRIPTION);
    }

    @Test
    void patchNonExistingJsonForm() throws Exception {
        int databaseSizeBeforeUpdate = jsonFormRepository.findAll().collectList().block().size();
        jsonForm.setId(count.incrementAndGet());

        // Create the JsonForm
        JsonFormDTO jsonFormDTO = jsonFormMapper.toDto(jsonForm);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, jsonFormDTO.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(jsonFormDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the JsonForm in the database
        List<JsonForm> jsonFormList = jsonFormRepository.findAll().collectList().block();
        assertThat(jsonFormList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    void patchWithIdMismatchJsonForm() throws Exception {
        int databaseSizeBeforeUpdate = jsonFormRepository.findAll().collectList().block().size();
        jsonForm.setId(count.incrementAndGet());

        // Create the JsonForm
        JsonFormDTO jsonFormDTO = jsonFormMapper.toDto(jsonForm);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, count.incrementAndGet())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(jsonFormDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the JsonForm in the database
        List<JsonForm> jsonFormList = jsonFormRepository.findAll().collectList().block();
        assertThat(jsonFormList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    void patchWithMissingIdPathParamJsonForm() throws Exception {
        int databaseSizeBeforeUpdate = jsonFormRepository.findAll().collectList().block().size();
        jsonForm.setId(count.incrementAndGet());

        // Create the JsonForm
        JsonFormDTO jsonFormDTO = jsonFormMapper.toDto(jsonForm);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(jsonFormDTO))
            .exchange()
            .expectStatus()
            .isEqualTo(405);

        // Validate the JsonForm in the database
        List<JsonForm> jsonFormList = jsonFormRepository.findAll().collectList().block();
        assertThat(jsonFormList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    void deleteJsonForm() {
        // Initialize the database
        jsonFormRepository.save(jsonForm).block();

        int databaseSizeBeforeDelete = jsonFormRepository.findAll().collectList().block().size();

        // Delete the jsonForm
        webTestClient
            .delete()
            .uri(ENTITY_API_URL_ID, jsonForm.getId())
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isNoContent();

        // Validate the database contains one less item
        List<JsonForm> jsonFormList = jsonFormRepository.findAll().collectList().block();
        assertThat(jsonFormList).hasSize(databaseSizeBeforeDelete - 1);
    }
}
