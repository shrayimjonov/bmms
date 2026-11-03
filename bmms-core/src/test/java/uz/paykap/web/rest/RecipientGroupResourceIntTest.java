package uz.paykap.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static uz.paykap.web.rest.TestUtil.createFormattingConversionService;

import java.util.ArrayList;
import java.util.List;
import javax.persistence.EntityManager;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import uz.paykap.IntegrationTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import uz.paykap.PkbillmsApp;
import uz.paykap.IntegrationTest;
import uz.paykap.domain.Recipient;
import uz.paykap.domain.RecipientGroup;
import uz.paykap.repository.RecipientGroupRepository;
import uz.paykap.service.RecipientGroupQueryService;
import uz.paykap.service.RecipientGroupService;
import uz.paykap.service.dto.RecipientGroupDTO;
import uz.paykap.service.mapper.RecipientGroupMapper;
import uz.paykap.web.rest.errors.ExceptionTranslator;

/**
 * Test class for the RecipientGroupResource REST controller.
 *
 * @see RecipientGroupResource
 */
@RunWith(SpringRunner.class)
@IntegrationTest
public class RecipientGroupResourceIntTest {

    private static final String DEFAULT_NAME = "AAAAAAAAAA";
    private static final String UPDATED_NAME = "BBBBBBBBBB";

    private static final Boolean DEFAULT_ACTIVE = false;
    private static final Boolean UPDATED_ACTIVE = true;

    private static final String DEFAULT_MORE = "AAAAAAAAAA";
    private static final String UPDATED_MORE = "BBBBBBBBBB";

    @Autowired
    private RecipientGroupRepository recipientGroupRepository;

    @Mock
    private RecipientGroupRepository recipientGroupRepositoryMock;

    @Autowired
    private RecipientGroupMapper recipientGroupMapper;

    @Mock
    private RecipientGroupService recipientGroupServiceMock;

    @Autowired
    private RecipientGroupService recipientGroupService;

    @Autowired
    private RecipientGroupQueryService recipientGroupQueryService;

    @Autowired
    private MappingJackson2HttpMessageConverter jacksonMessageConverter;

    @Autowired
    private PageableHandlerMethodArgumentResolver pageableArgumentResolver;

    @Autowired
    private ExceptionTranslator exceptionTranslator;

    @Autowired
    private EntityManager em;

    private MockMvc restRecipientGroupMockMvc;

    private RecipientGroup recipientGroup;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        final RecipientGroupResource recipientGroupResource = new RecipientGroupResource(
            recipientGroupService,
            recipientGroupRepository,
            recipientGroupQueryService
        );
        this.restRecipientGroupMockMvc =
            MockMvcBuilders
                .standaloneSetup(recipientGroupResource)
                .setCustomArgumentResolvers(pageableArgumentResolver)
                .setControllerAdvice(exceptionTranslator)
                .setConversionService(createFormattingConversionService())
                .setMessageConverters(jacksonMessageConverter)
                .build();
    }

    /**
     * Create an entity for this test.
     * <p>
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static RecipientGroup createEntity(EntityManager em) {
        RecipientGroup recipientGroup = new RecipientGroup().name(DEFAULT_NAME).active(DEFAULT_ACTIVE).more(DEFAULT_MORE);
        // Add required entity
        Recipient recipient = RecipientResourceIntTest.createEntity(em);
        em.persist(recipient);
        em.flush();
        recipientGroup.getRecipients().add(recipient);
        return recipientGroup;
    }

    @Before
    public void initTest() {
        recipientGroup = createEntity(em);
    }

    @Test
    @Transactional
    public void createRecipientGroup() throws Exception {
        int databaseSizeBeforeCreate = recipientGroupRepository.findAll().size();

        // Create the RecipientGroup
        RecipientGroupDTO recipientGroupDTO = recipientGroupMapper.toDto(recipientGroup);
        restRecipientGroupMockMvc
            .perform(
                post("/api/recipient-groups")
                    .contentType(TestUtil.APPLICATION_JSON_UTF8)
                    .content(TestUtil.convertObjectToJsonBytes(recipientGroupDTO))
            )
            .andExpect(status().isCreated());

        // Validate the RecipientGroup in the database
        List<RecipientGroup> recipientGroupList = recipientGroupRepository.findAll();
        assertThat(recipientGroupList).hasSize(databaseSizeBeforeCreate + 1);
        RecipientGroup testRecipientGroup = recipientGroupList.get(recipientGroupList.size() - 1);
        assertThat(testRecipientGroup.getName()).isEqualTo(DEFAULT_NAME);
        assertThat(testRecipientGroup.isActive()).isEqualTo(DEFAULT_ACTIVE);
        assertThat(testRecipientGroup.getMore()).isEqualTo(DEFAULT_MORE);
    }

    @Test
    @Transactional
    public void createRecipientGroupWithExistingId() throws Exception {
        int databaseSizeBeforeCreate = recipientGroupRepository.findAll().size();

        // Create the RecipientGroup with an existing ID
        recipientGroup.setId(1L);
        RecipientGroupDTO recipientGroupDTO = recipientGroupMapper.toDto(recipientGroup);

        // An entity with an existing ID cannot be created, so this API call must fail
        restRecipientGroupMockMvc
            .perform(
                post("/api/recipient-groups")
                    .contentType(TestUtil.APPLICATION_JSON_UTF8)
                    .content(TestUtil.convertObjectToJsonBytes(recipientGroupDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the RecipientGroup in the database
        List<RecipientGroup> recipientGroupList = recipientGroupRepository.findAll();
        assertThat(recipientGroupList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    public void checkNameIsRequired() throws Exception {
        int databaseSizeBeforeTest = recipientGroupRepository.findAll().size();
        // set the field null
        recipientGroup.setName(null);

        // Create the RecipientGroup, which fails.
        RecipientGroupDTO recipientGroupDTO = recipientGroupMapper.toDto(recipientGroup);

        restRecipientGroupMockMvc
            .perform(
                post("/api/recipient-groups")
                    .contentType(TestUtil.APPLICATION_JSON_UTF8)
                    .content(TestUtil.convertObjectToJsonBytes(recipientGroupDTO))
            )
            .andExpect(status().isBadRequest());

        List<RecipientGroup> recipientGroupList = recipientGroupRepository.findAll();
        assertThat(recipientGroupList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkActiveIsRequired() throws Exception {
        int databaseSizeBeforeTest = recipientGroupRepository.findAll().size();
        // set the field null
        recipientGroup.setActive(null);

        // Create the RecipientGroup, which fails.
        RecipientGroupDTO recipientGroupDTO = recipientGroupMapper.toDto(recipientGroup);

        restRecipientGroupMockMvc
            .perform(
                post("/api/recipient-groups")
                    .contentType(TestUtil.APPLICATION_JSON_UTF8)
                    .content(TestUtil.convertObjectToJsonBytes(recipientGroupDTO))
            )
            .andExpect(status().isBadRequest());

        List<RecipientGroup> recipientGroupList = recipientGroupRepository.findAll();
        assertThat(recipientGroupList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void getAllRecipientGroups() throws Exception {
        // Initialize the database
        recipientGroupRepository.saveAndFlush(recipientGroup);

        // Get all the recipientGroupList
        restRecipientGroupMockMvc
            .perform(get("/api/recipient-groups?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(recipientGroup.getId().intValue())))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME.toString())))
            .andExpect(jsonPath("$.[*].active").value(hasItem(DEFAULT_ACTIVE.booleanValue())))
            .andExpect(jsonPath("$.[*].more").value(hasItem(DEFAULT_MORE.toString())));
    }

    public void getAllRecipientGroupsWithEagerRelationshipsIsEnabled() throws Exception {
        RecipientGroupResource recipientGroupResource = new RecipientGroupResource(
            recipientGroupServiceMock,
            recipientGroupRepository,
            recipientGroupQueryService
        );
        when(recipientGroupServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        MockMvc restRecipientGroupMockMvc = MockMvcBuilders
            .standaloneSetup(recipientGroupResource)
            .setCustomArgumentResolvers(pageableArgumentResolver)
            .setControllerAdvice(exceptionTranslator)
            .setConversionService(createFormattingConversionService())
            .setMessageConverters(jacksonMessageConverter)
            .build();

        restRecipientGroupMockMvc.perform(get("/api/recipient-groups?eagerload=true")).andExpect(status().isOk());

        verify(recipientGroupServiceMock, times(1)).findAllWithEagerRelationships(any());
    }

    public void getAllRecipientGroupsWithEagerRelationshipsIsNotEnabled() throws Exception {
        RecipientGroupResource recipientGroupResource = new RecipientGroupResource(
            recipientGroupServiceMock,
            recipientGroupRepository,
            recipientGroupQueryService
        );
        when(recipientGroupServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));
        MockMvc restRecipientGroupMockMvc = MockMvcBuilders
            .standaloneSetup(recipientGroupResource)
            .setCustomArgumentResolvers(pageableArgumentResolver)
            .setControllerAdvice(exceptionTranslator)
            .setConversionService(createFormattingConversionService())
            .setMessageConverters(jacksonMessageConverter)
            .build();

        restRecipientGroupMockMvc.perform(get("/api/recipient-groups?eagerload=true")).andExpect(status().isOk());

        verify(recipientGroupServiceMock, times(1)).findAllWithEagerRelationships(any());
    }

    @Test
    @Transactional
    public void getRecipientGroup() throws Exception {
        // Initialize the database
        recipientGroupRepository.saveAndFlush(recipientGroup);

        // Get the recipientGroup
        restRecipientGroupMockMvc
            .perform(get("/api/recipient-groups/{id}", recipientGroup.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.id").value(recipientGroup.getId().intValue()))
            .andExpect(jsonPath("$.name").value(DEFAULT_NAME.toString()))
            .andExpect(jsonPath("$.active").value(DEFAULT_ACTIVE.booleanValue()))
            .andExpect(jsonPath("$.more").value(DEFAULT_MORE.toString()));
    }

    @Test
    @Transactional
    public void getAllRecipientGroupsByNameIsEqualToSomething() throws Exception {
        // Initialize the database
        recipientGroupRepository.saveAndFlush(recipientGroup);

        // Get all the recipientGroupList where name equals to DEFAULT_NAME
        defaultRecipientGroupShouldBeFound("name.equals=" + DEFAULT_NAME);

        // Get all the recipientGroupList where name equals to UPDATED_NAME
        defaultRecipientGroupShouldNotBeFound("name.equals=" + UPDATED_NAME);
    }

    @Test
    @Transactional
    public void getAllRecipientGroupsByNameIsInShouldWork() throws Exception {
        // Initialize the database
        recipientGroupRepository.saveAndFlush(recipientGroup);

        // Get all the recipientGroupList where name in DEFAULT_NAME or UPDATED_NAME
        defaultRecipientGroupShouldBeFound("name.in=" + DEFAULT_NAME + "," + UPDATED_NAME);

        // Get all the recipientGroupList where name equals to UPDATED_NAME
        defaultRecipientGroupShouldNotBeFound("name.in=" + UPDATED_NAME);
    }

    @Test
    @Transactional
    public void getAllRecipientGroupsByNameIsNullOrNotNull() throws Exception {
        // Initialize the database
        recipientGroupRepository.saveAndFlush(recipientGroup);

        // Get all the recipientGroupList where name is not null
        defaultRecipientGroupShouldBeFound("name.specified=true");

        // Get all the recipientGroupList where name is null
        defaultRecipientGroupShouldNotBeFound("name.specified=false");
    }

    @Test
    @Transactional
    public void getAllRecipientGroupsByActiveIsEqualToSomething() throws Exception {
        // Initialize the database
        recipientGroupRepository.saveAndFlush(recipientGroup);

        // Get all the recipientGroupList where active equals to DEFAULT_ACTIVE
        defaultRecipientGroupShouldBeFound("active.equals=" + DEFAULT_ACTIVE);

        // Get all the recipientGroupList where active equals to UPDATED_ACTIVE
        defaultRecipientGroupShouldNotBeFound("active.equals=" + UPDATED_ACTIVE);
    }

    @Test
    @Transactional
    public void getAllRecipientGroupsByActiveIsInShouldWork() throws Exception {
        // Initialize the database
        recipientGroupRepository.saveAndFlush(recipientGroup);

        // Get all the recipientGroupList where active in DEFAULT_ACTIVE or UPDATED_ACTIVE
        defaultRecipientGroupShouldBeFound("active.in=" + DEFAULT_ACTIVE + "," + UPDATED_ACTIVE);

        // Get all the recipientGroupList where active equals to UPDATED_ACTIVE
        defaultRecipientGroupShouldNotBeFound("active.in=" + UPDATED_ACTIVE);
    }

    @Test
    @Transactional
    public void getAllRecipientGroupsByActiveIsNullOrNotNull() throws Exception {
        // Initialize the database
        recipientGroupRepository.saveAndFlush(recipientGroup);

        // Get all the recipientGroupList where active is not null
        defaultRecipientGroupShouldBeFound("active.specified=true");

        // Get all the recipientGroupList where active is null
        defaultRecipientGroupShouldNotBeFound("active.specified=false");
    }

    @Test
    @Transactional
    public void getAllRecipientGroupsByMoreIsEqualToSomething() throws Exception {
        // Initialize the database
        recipientGroupRepository.saveAndFlush(recipientGroup);

        // Get all the recipientGroupList where more equals to DEFAULT_MORE
        defaultRecipientGroupShouldBeFound("more.equals=" + DEFAULT_MORE);

        // Get all the recipientGroupList where more equals to UPDATED_MORE
        defaultRecipientGroupShouldNotBeFound("more.equals=" + UPDATED_MORE);
    }

    @Test
    @Transactional
    public void getAllRecipientGroupsByMoreIsInShouldWork() throws Exception {
        // Initialize the database
        recipientGroupRepository.saveAndFlush(recipientGroup);

        // Get all the recipientGroupList where more in DEFAULT_MORE or UPDATED_MORE
        defaultRecipientGroupShouldBeFound("more.in=" + DEFAULT_MORE + "," + UPDATED_MORE);

        // Get all the recipientGroupList where more equals to UPDATED_MORE
        defaultRecipientGroupShouldNotBeFound("more.in=" + UPDATED_MORE);
    }

    @Test
    @Transactional
    public void getAllRecipientGroupsByMoreIsNullOrNotNull() throws Exception {
        // Initialize the database
        recipientGroupRepository.saveAndFlush(recipientGroup);

        // Get all the recipientGroupList where more is not null
        defaultRecipientGroupShouldBeFound("more.specified=true");

        // Get all the recipientGroupList where more is null
        defaultRecipientGroupShouldNotBeFound("more.specified=false");
    }

    @Test
    @Transactional
    public void getAllRecipientGroupsByRecipientIsEqualToSomething() throws Exception {
        // Initialize the database
        Recipient recipient = RecipientResourceIntTest.createEntity(em);
        em.persist(recipient);
        em.flush();
        recipientGroup.addRecipient(recipient);
        recipientGroupRepository.saveAndFlush(recipientGroup);
        Long recipientId = recipient.getId();

        // Get all the recipientGroupList where recipient equals to recipientId
        defaultRecipientGroupShouldBeFound("recipientId.equals=" + recipientId);

        // Get all the recipientGroupList where recipient equals to recipientId + 1
        defaultRecipientGroupShouldNotBeFound("recipientId.equals=" + (recipientId + 1));
    }

    /**
     * Executes the search, and checks that the default entity is returned
     */
    private void defaultRecipientGroupShouldBeFound(String filter) throws Exception {
        restRecipientGroupMockMvc
            .perform(get("/api/recipient-groups?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(recipientGroup.getId().intValue())))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME.toString())))
            .andExpect(jsonPath("$.[*].active").value(hasItem(DEFAULT_ACTIVE.booleanValue())))
            .andExpect(jsonPath("$.[*].more").value(hasItem(DEFAULT_MORE.toString())));
    }

    /**
     * Executes the search, and checks that the default entity is not returned
     */
    private void defaultRecipientGroupShouldNotBeFound(String filter) throws Exception {
        restRecipientGroupMockMvc
            .perform(get("/api/recipient-groups?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    @Transactional
    public void getNonExistingRecipientGroup() throws Exception {
        // Get the recipientGroup
        restRecipientGroupMockMvc.perform(get("/api/recipient-groups/{id}", Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updateRecipientGroup() throws Exception {
        // Initialize the database
        recipientGroupRepository.saveAndFlush(recipientGroup);

        int databaseSizeBeforeUpdate = recipientGroupRepository.findAll().size();

        // Update the recipientGroup
        RecipientGroup updatedRecipientGroup = recipientGroupRepository.findById(recipientGroup.getId()).get();
        // Disconnect from session so that the updates on updatedRecipientGroup are not directly saved in db
        em.detach(updatedRecipientGroup);
        updatedRecipientGroup.name(UPDATED_NAME).active(UPDATED_ACTIVE).more(UPDATED_MORE);
        RecipientGroupDTO recipientGroupDTO = recipientGroupMapper.toDto(updatedRecipientGroup);

        restRecipientGroupMockMvc
            .perform(
                put("/api/recipient-groups")
                    .contentType(TestUtil.APPLICATION_JSON_UTF8)
                    .content(TestUtil.convertObjectToJsonBytes(recipientGroupDTO))
            )
            .andExpect(status().isOk());

        // Validate the RecipientGroup in the database
        List<RecipientGroup> recipientGroupList = recipientGroupRepository.findAll();
        assertThat(recipientGroupList).hasSize(databaseSizeBeforeUpdate);
        RecipientGroup testRecipientGroup = recipientGroupList.get(recipientGroupList.size() - 1);
        assertThat(testRecipientGroup.getName()).isEqualTo(UPDATED_NAME);
        assertThat(testRecipientGroup.isActive()).isEqualTo(UPDATED_ACTIVE);
        assertThat(testRecipientGroup.getMore()).isEqualTo(UPDATED_MORE);
    }

    @Test
    @Transactional
    public void updateNonExistingRecipientGroup() throws Exception {
        int databaseSizeBeforeUpdate = recipientGroupRepository.findAll().size();

        // Create the RecipientGroup
        RecipientGroupDTO recipientGroupDTO = recipientGroupMapper.toDto(recipientGroup);

        // If the entity doesn't have an ID, it will be created instead of just being updated
        restRecipientGroupMockMvc
            .perform(
                put("/api/recipient-groups")
                    .contentType(TestUtil.APPLICATION_JSON_UTF8)
                    .content(TestUtil.convertObjectToJsonBytes(recipientGroupDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the RecipientGroup in the database
        List<RecipientGroup> recipientGroupList = recipientGroupRepository.findAll();
        assertThat(recipientGroupList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    public void deleteRecipientGroup() throws Exception {
        // Initialize the database
        recipientGroupRepository.saveAndFlush(recipientGroup);

        int databaseSizeBeforeDelete = recipientGroupRepository.findAll().size();

        // Get the recipientGroup
        restRecipientGroupMockMvc
            .perform(delete("/api/recipient-groups/{id}", recipientGroup.getId()).accept(TestUtil.APPLICATION_JSON_UTF8))
            .andExpect(status().isOk());

        // Validate the database is empty
        List<RecipientGroup> recipientGroupList = recipientGroupRepository.findAll();
        assertThat(recipientGroupList).hasSize(databaseSizeBeforeDelete - 1);
    }

    @Test
    @Transactional
    public void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(RecipientGroup.class);
        RecipientGroup recipientGroup1 = new RecipientGroup();
        recipientGroup1.setId(1L);
        RecipientGroup recipientGroup2 = new RecipientGroup();
        recipientGroup2.setId(recipientGroup1.getId());
        assertThat(recipientGroup1).isEqualTo(recipientGroup2);
        recipientGroup2.setId(2L);
        assertThat(recipientGroup1).isNotEqualTo(recipientGroup2);
        recipientGroup1.setId(null);
        assertThat(recipientGroup1).isNotEqualTo(recipientGroup2);
    }

    @Test
    @Transactional
    public void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(RecipientGroupDTO.class);
        RecipientGroupDTO recipientGroupDTO1 = new RecipientGroupDTO();
        recipientGroupDTO1.setId(1L);
        RecipientGroupDTO recipientGroupDTO2 = new RecipientGroupDTO();
        assertThat(recipientGroupDTO1).isNotEqualTo(recipientGroupDTO2);
        recipientGroupDTO2.setId(recipientGroupDTO1.getId());
        assertThat(recipientGroupDTO1).isEqualTo(recipientGroupDTO2);
        recipientGroupDTO2.setId(2L);
        assertThat(recipientGroupDTO1).isNotEqualTo(recipientGroupDTO2);
        recipientGroupDTO1.setId(null);
        assertThat(recipientGroupDTO1).isNotEqualTo(recipientGroupDTO2);
    }
    //    @Test
    //    @Transactional
    //    public void testEntityFromId() {
    //        assertThat(recipientGroupMapper.fromId(42L).getId()).isEqualTo(42);
    //        assertThat(recipientGroupMapper.fromId(null)).isNull();
    //    }
}
