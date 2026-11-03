package uz.paykap.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;
import javax.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import uz.paykap.IntegrationTest;
import uz.paykap.domain.Recipient;
import uz.paykap.domain.RecipientGroup;
import uz.paykap.repository.RecipientGroupRepository;
import uz.paykap.service.RecipientGroupService;
import uz.paykap.service.criteria.RecipientGroupCriteria;
import uz.paykap.service.dto.RecipientGroupDTO;
import uz.paykap.service.mapper.RecipientGroupMapper;

/**
 * Integration tests for the {@link RecipientGroupResource} REST controller.
 */
@IntegrationTest
@ExtendWith(MockitoExtension.class)
@AutoConfigureMockMvc
@WithMockUser
class RecipientGroupResourceIT {

    private static final String DEFAULT_NAME = "AAAAAAAAAA";
    private static final String UPDATED_NAME = "BBBBBBBBBB";

    private static final Boolean DEFAULT_ACTIVE = false;
    private static final Boolean UPDATED_ACTIVE = true;

    private static final String DEFAULT_MORE = "AAAAAAAAAA";
    private static final String UPDATED_MORE = "BBBBBBBBBB";

    private static final String ENTITY_API_URL = "/api/recipient-groups";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong count = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private RecipientGroupRepository recipientGroupRepository;

    @Mock
    private RecipientGroupRepository recipientGroupRepositoryMock;

    @Autowired
    private RecipientGroupMapper recipientGroupMapper;

    @Mock
    private RecipientGroupService recipientGroupServiceMock;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restRecipientGroupMockMvc;

    private RecipientGroup recipientGroup;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static RecipientGroup createEntity(EntityManager em) {
        RecipientGroup recipientGroup = new RecipientGroup().name(DEFAULT_NAME).active(DEFAULT_ACTIVE).more(DEFAULT_MORE);
        // Add required entity
        Recipient recipient;
        if (TestUtil.findAll(em, Recipient.class).isEmpty()) {
            recipient = RecipientResourceIT.createEntity(em);
            em.persist(recipient);
            em.flush();
        } else {
            recipient = TestUtil.findAll(em, Recipient.class).get(0);
        }
        recipientGroup.getRecipients().add(recipient);
        return recipientGroup;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static RecipientGroup createUpdatedEntity(EntityManager em) {
        RecipientGroup recipientGroup = new RecipientGroup().name(UPDATED_NAME).active(UPDATED_ACTIVE).more(UPDATED_MORE);
        // Add required entity
        Recipient recipient;
        if (TestUtil.findAll(em, Recipient.class).isEmpty()) {
            recipient = RecipientResourceIT.createUpdatedEntity(em);
            em.persist(recipient);
            em.flush();
        } else {
            recipient = TestUtil.findAll(em, Recipient.class).get(0);
        }
        recipientGroup.getRecipients().add(recipient);
        return recipientGroup;
    }

    @BeforeEach
    public void initTest() {
        recipientGroup = createEntity(em);
    }

    @Test
    @Transactional
    void createRecipientGroup() throws Exception {
        int databaseSizeBeforeCreate = recipientGroupRepository.findAll().size();
        // Create the RecipientGroup
        RecipientGroupDTO recipientGroupDTO = recipientGroupMapper.toDto(recipientGroup);
        restRecipientGroupMockMvc
            .perform(
                post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(recipientGroupDTO))
            )
            .andExpect(status().isCreated());

        // Validate the RecipientGroup in the database
        List<RecipientGroup> recipientGroupList = recipientGroupRepository.findAll();
        assertThat(recipientGroupList).hasSize(databaseSizeBeforeCreate + 1);
        RecipientGroup testRecipientGroup = recipientGroupList.get(recipientGroupList.size() - 1);
        assertThat(testRecipientGroup.getName()).isEqualTo(DEFAULT_NAME);
        assertThat(testRecipientGroup.getActive()).isEqualTo(DEFAULT_ACTIVE);
        assertThat(testRecipientGroup.getMore()).isEqualTo(DEFAULT_MORE);
    }

    @Test
    @Transactional
    void createRecipientGroupWithExistingId() throws Exception {
        // Create the RecipientGroup with an existing ID
        recipientGroup.setId(1L);
        RecipientGroupDTO recipientGroupDTO = recipientGroupMapper.toDto(recipientGroup);

        int databaseSizeBeforeCreate = recipientGroupRepository.findAll().size();

        // An entity with an existing ID cannot be created, so this API call must fail
        restRecipientGroupMockMvc
            .perform(
                post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(recipientGroupDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the RecipientGroup in the database
        List<RecipientGroup> recipientGroupList = recipientGroupRepository.findAll();
        assertThat(recipientGroupList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkNameIsRequired() throws Exception {
        int databaseSizeBeforeTest = recipientGroupRepository.findAll().size();
        // set the field null
        recipientGroup.setName(null);

        // Create the RecipientGroup, which fails.
        RecipientGroupDTO recipientGroupDTO = recipientGroupMapper.toDto(recipientGroup);

        restRecipientGroupMockMvc
            .perform(
                post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(recipientGroupDTO))
            )
            .andExpect(status().isBadRequest());

        List<RecipientGroup> recipientGroupList = recipientGroupRepository.findAll();
        assertThat(recipientGroupList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkActiveIsRequired() throws Exception {
        int databaseSizeBeforeTest = recipientGroupRepository.findAll().size();
        // set the field null
        recipientGroup.setActive(null);

        // Create the RecipientGroup, which fails.
        RecipientGroupDTO recipientGroupDTO = recipientGroupMapper.toDto(recipientGroup);

        restRecipientGroupMockMvc
            .perform(
                post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(recipientGroupDTO))
            )
            .andExpect(status().isBadRequest());

        List<RecipientGroup> recipientGroupList = recipientGroupRepository.findAll();
        assertThat(recipientGroupList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllRecipientGroups() throws Exception {
        // Initialize the database
        recipientGroupRepository.saveAndFlush(recipientGroup);

        // Get all the recipientGroupList
        restRecipientGroupMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(recipientGroup.getId().intValue())))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME)))
            .andExpect(jsonPath("$.[*].active").value(hasItem(DEFAULT_ACTIVE.booleanValue())))
            .andExpect(jsonPath("$.[*].more").value(hasItem(DEFAULT_MORE)));
    }

    @SuppressWarnings({ "unchecked" })
    void getAllRecipientGroupsWithEagerRelationshipsIsEnabled() throws Exception {
        when(recipientGroupServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restRecipientGroupMockMvc.perform(get(ENTITY_API_URL + "?eagerload=true")).andExpect(status().isOk());

        verify(recipientGroupServiceMock, times(1)).findAllWithEagerRelationships(any());
    }

    @SuppressWarnings({ "unchecked" })
    void getAllRecipientGroupsWithEagerRelationshipsIsNotEnabled() throws Exception {
        when(recipientGroupServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restRecipientGroupMockMvc.perform(get(ENTITY_API_URL + "?eagerload=true")).andExpect(status().isOk());

        verify(recipientGroupServiceMock, times(1)).findAllWithEagerRelationships(any());
    }

    @Test
    @Transactional
    void getRecipientGroup() throws Exception {
        // Initialize the database
        recipientGroupRepository.saveAndFlush(recipientGroup);

        // Get the recipientGroup
        restRecipientGroupMockMvc
            .perform(get(ENTITY_API_URL_ID, recipientGroup.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(recipientGroup.getId().intValue()))
            .andExpect(jsonPath("$.name").value(DEFAULT_NAME))
            .andExpect(jsonPath("$.active").value(DEFAULT_ACTIVE.booleanValue()))
            .andExpect(jsonPath("$.more").value(DEFAULT_MORE));
    }

    @Test
    @Transactional
    void getRecipientGroupsByIdFiltering() throws Exception {
        // Initialize the database
        recipientGroupRepository.saveAndFlush(recipientGroup);

        Long id = recipientGroup.getId();

        defaultRecipientGroupShouldBeFound("id.equals=" + id);
        defaultRecipientGroupShouldNotBeFound("id.notEquals=" + id);

        defaultRecipientGroupShouldBeFound("id.greaterThanOrEqual=" + id);
        defaultRecipientGroupShouldNotBeFound("id.greaterThan=" + id);

        defaultRecipientGroupShouldBeFound("id.lessThanOrEqual=" + id);
        defaultRecipientGroupShouldNotBeFound("id.lessThan=" + id);
    }

    @Test
    @Transactional
    void getAllRecipientGroupsByNameIsEqualToSomething() throws Exception {
        // Initialize the database
        recipientGroupRepository.saveAndFlush(recipientGroup);

        // Get all the recipientGroupList where name equals to DEFAULT_NAME
        defaultRecipientGroupShouldBeFound("name.equals=" + DEFAULT_NAME);

        // Get all the recipientGroupList where name equals to UPDATED_NAME
        defaultRecipientGroupShouldNotBeFound("name.equals=" + UPDATED_NAME);
    }

    @Test
    @Transactional
    void getAllRecipientGroupsByNameIsNotEqualToSomething() throws Exception {
        // Initialize the database
        recipientGroupRepository.saveAndFlush(recipientGroup);

        // Get all the recipientGroupList where name not equals to DEFAULT_NAME
        defaultRecipientGroupShouldNotBeFound("name.notEquals=" + DEFAULT_NAME);

        // Get all the recipientGroupList where name not equals to UPDATED_NAME
        defaultRecipientGroupShouldBeFound("name.notEquals=" + UPDATED_NAME);
    }

    @Test
    @Transactional
    void getAllRecipientGroupsByNameIsInShouldWork() throws Exception {
        // Initialize the database
        recipientGroupRepository.saveAndFlush(recipientGroup);

        // Get all the recipientGroupList where name in DEFAULT_NAME or UPDATED_NAME
        defaultRecipientGroupShouldBeFound("name.in=" + DEFAULT_NAME + "," + UPDATED_NAME);

        // Get all the recipientGroupList where name equals to UPDATED_NAME
        defaultRecipientGroupShouldNotBeFound("name.in=" + UPDATED_NAME);
    }

    @Test
    @Transactional
    void getAllRecipientGroupsByNameIsNullOrNotNull() throws Exception {
        // Initialize the database
        recipientGroupRepository.saveAndFlush(recipientGroup);

        // Get all the recipientGroupList where name is not null
        defaultRecipientGroupShouldBeFound("name.specified=true");

        // Get all the recipientGroupList where name is null
        defaultRecipientGroupShouldNotBeFound("name.specified=false");
    }

    @Test
    @Transactional
    void getAllRecipientGroupsByNameContainsSomething() throws Exception {
        // Initialize the database
        recipientGroupRepository.saveAndFlush(recipientGroup);

        // Get all the recipientGroupList where name contains DEFAULT_NAME
        defaultRecipientGroupShouldBeFound("name.contains=" + DEFAULT_NAME);

        // Get all the recipientGroupList where name contains UPDATED_NAME
        defaultRecipientGroupShouldNotBeFound("name.contains=" + UPDATED_NAME);
    }

    @Test
    @Transactional
    void getAllRecipientGroupsByNameNotContainsSomething() throws Exception {
        // Initialize the database
        recipientGroupRepository.saveAndFlush(recipientGroup);

        // Get all the recipientGroupList where name does not contain DEFAULT_NAME
        defaultRecipientGroupShouldNotBeFound("name.doesNotContain=" + DEFAULT_NAME);

        // Get all the recipientGroupList where name does not contain UPDATED_NAME
        defaultRecipientGroupShouldBeFound("name.doesNotContain=" + UPDATED_NAME);
    }

    @Test
    @Transactional
    void getAllRecipientGroupsByActiveIsEqualToSomething() throws Exception {
        // Initialize the database
        recipientGroupRepository.saveAndFlush(recipientGroup);

        // Get all the recipientGroupList where active equals to DEFAULT_ACTIVE
        defaultRecipientGroupShouldBeFound("active.equals=" + DEFAULT_ACTIVE);

        // Get all the recipientGroupList where active equals to UPDATED_ACTIVE
        defaultRecipientGroupShouldNotBeFound("active.equals=" + UPDATED_ACTIVE);
    }

    @Test
    @Transactional
    void getAllRecipientGroupsByActiveIsNotEqualToSomething() throws Exception {
        // Initialize the database
        recipientGroupRepository.saveAndFlush(recipientGroup);

        // Get all the recipientGroupList where active not equals to DEFAULT_ACTIVE
        defaultRecipientGroupShouldNotBeFound("active.notEquals=" + DEFAULT_ACTIVE);

        // Get all the recipientGroupList where active not equals to UPDATED_ACTIVE
        defaultRecipientGroupShouldBeFound("active.notEquals=" + UPDATED_ACTIVE);
    }

    @Test
    @Transactional
    void getAllRecipientGroupsByActiveIsInShouldWork() throws Exception {
        // Initialize the database
        recipientGroupRepository.saveAndFlush(recipientGroup);

        // Get all the recipientGroupList where active in DEFAULT_ACTIVE or UPDATED_ACTIVE
        defaultRecipientGroupShouldBeFound("active.in=" + DEFAULT_ACTIVE + "," + UPDATED_ACTIVE);

        // Get all the recipientGroupList where active equals to UPDATED_ACTIVE
        defaultRecipientGroupShouldNotBeFound("active.in=" + UPDATED_ACTIVE);
    }

    @Test
    @Transactional
    void getAllRecipientGroupsByActiveIsNullOrNotNull() throws Exception {
        // Initialize the database
        recipientGroupRepository.saveAndFlush(recipientGroup);

        // Get all the recipientGroupList where active is not null
        defaultRecipientGroupShouldBeFound("active.specified=true");

        // Get all the recipientGroupList where active is null
        defaultRecipientGroupShouldNotBeFound("active.specified=false");
    }

    @Test
    @Transactional
    void getAllRecipientGroupsByMoreIsEqualToSomething() throws Exception {
        // Initialize the database
        recipientGroupRepository.saveAndFlush(recipientGroup);

        // Get all the recipientGroupList where more equals to DEFAULT_MORE
        defaultRecipientGroupShouldBeFound("more.equals=" + DEFAULT_MORE);

        // Get all the recipientGroupList where more equals to UPDATED_MORE
        defaultRecipientGroupShouldNotBeFound("more.equals=" + UPDATED_MORE);
    }

    @Test
    @Transactional
    void getAllRecipientGroupsByMoreIsNotEqualToSomething() throws Exception {
        // Initialize the database
        recipientGroupRepository.saveAndFlush(recipientGroup);

        // Get all the recipientGroupList where more not equals to DEFAULT_MORE
        defaultRecipientGroupShouldNotBeFound("more.notEquals=" + DEFAULT_MORE);

        // Get all the recipientGroupList where more not equals to UPDATED_MORE
        defaultRecipientGroupShouldBeFound("more.notEquals=" + UPDATED_MORE);
    }

    @Test
    @Transactional
    void getAllRecipientGroupsByMoreIsInShouldWork() throws Exception {
        // Initialize the database
        recipientGroupRepository.saveAndFlush(recipientGroup);

        // Get all the recipientGroupList where more in DEFAULT_MORE or UPDATED_MORE
        defaultRecipientGroupShouldBeFound("more.in=" + DEFAULT_MORE + "," + UPDATED_MORE);

        // Get all the recipientGroupList where more equals to UPDATED_MORE
        defaultRecipientGroupShouldNotBeFound("more.in=" + UPDATED_MORE);
    }

    @Test
    @Transactional
    void getAllRecipientGroupsByMoreIsNullOrNotNull() throws Exception {
        // Initialize the database
        recipientGroupRepository.saveAndFlush(recipientGroup);

        // Get all the recipientGroupList where more is not null
        defaultRecipientGroupShouldBeFound("more.specified=true");

        // Get all the recipientGroupList where more is null
        defaultRecipientGroupShouldNotBeFound("more.specified=false");
    }

    @Test
    @Transactional
    void getAllRecipientGroupsByMoreContainsSomething() throws Exception {
        // Initialize the database
        recipientGroupRepository.saveAndFlush(recipientGroup);

        // Get all the recipientGroupList where more contains DEFAULT_MORE
        defaultRecipientGroupShouldBeFound("more.contains=" + DEFAULT_MORE);

        // Get all the recipientGroupList where more contains UPDATED_MORE
        defaultRecipientGroupShouldNotBeFound("more.contains=" + UPDATED_MORE);
    }

    @Test
    @Transactional
    void getAllRecipientGroupsByMoreNotContainsSomething() throws Exception {
        // Initialize the database
        recipientGroupRepository.saveAndFlush(recipientGroup);

        // Get all the recipientGroupList where more does not contain DEFAULT_MORE
        defaultRecipientGroupShouldNotBeFound("more.doesNotContain=" + DEFAULT_MORE);

        // Get all the recipientGroupList where more does not contain UPDATED_MORE
        defaultRecipientGroupShouldBeFound("more.doesNotContain=" + UPDATED_MORE);
    }

    @Test
    @Transactional
    void getAllRecipientGroupsByRecipientIsEqualToSomething() throws Exception {
        // Initialize the database
        recipientGroupRepository.saveAndFlush(recipientGroup);
        Recipient recipient = RecipientResourceIT.createEntity(em);
        em.persist(recipient);
        em.flush();
        recipientGroup.addRecipient(recipient);
        recipientGroupRepository.saveAndFlush(recipientGroup);
        Long recipientId = recipient.getId();

        // Get all the recipientGroupList where recipient equals to recipientId
        defaultRecipientGroupShouldBeFound("recipientId.equals=" + recipientId);

        // Get all the recipientGroupList where recipient equals to (recipientId + 1)
        defaultRecipientGroupShouldNotBeFound("recipientId.equals=" + (recipientId + 1));
    }

    /**
     * Executes the search, and checks that the default entity is returned.
     */
    private void defaultRecipientGroupShouldBeFound(String filter) throws Exception {
        restRecipientGroupMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(recipientGroup.getId().intValue())))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME)))
            .andExpect(jsonPath("$.[*].active").value(hasItem(DEFAULT_ACTIVE.booleanValue())))
            .andExpect(jsonPath("$.[*].more").value(hasItem(DEFAULT_MORE)));

        // Check, that the count call also returns 1
        restRecipientGroupMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("1"));
    }

    /**
     * Executes the search, and checks that the default entity is not returned.
     */
    private void defaultRecipientGroupShouldNotBeFound(String filter) throws Exception {
        restRecipientGroupMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty());

        // Check, that the count call also returns 0
        restRecipientGroupMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("0"));
    }

    @Test
    @Transactional
    void getNonExistingRecipientGroup() throws Exception {
        // Get the recipientGroup
        restRecipientGroupMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putNewRecipientGroup() throws Exception {
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
                put(ENTITY_API_URL_ID, recipientGroupDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(recipientGroupDTO))
            )
            .andExpect(status().isOk());

        // Validate the RecipientGroup in the database
        List<RecipientGroup> recipientGroupList = recipientGroupRepository.findAll();
        assertThat(recipientGroupList).hasSize(databaseSizeBeforeUpdate);
        RecipientGroup testRecipientGroup = recipientGroupList.get(recipientGroupList.size() - 1);
        assertThat(testRecipientGroup.getName()).isEqualTo(UPDATED_NAME);
        assertThat(testRecipientGroup.getActive()).isEqualTo(UPDATED_ACTIVE);
        assertThat(testRecipientGroup.getMore()).isEqualTo(UPDATED_MORE);
    }

    @Test
    @Transactional
    void putNonExistingRecipientGroup() throws Exception {
        int databaseSizeBeforeUpdate = recipientGroupRepository.findAll().size();
        recipientGroup.setId(count.incrementAndGet());

        // Create the RecipientGroup
        RecipientGroupDTO recipientGroupDTO = recipientGroupMapper.toDto(recipientGroup);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restRecipientGroupMockMvc
            .perform(
                put(ENTITY_API_URL_ID, recipientGroupDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(recipientGroupDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the RecipientGroup in the database
        List<RecipientGroup> recipientGroupList = recipientGroupRepository.findAll();
        assertThat(recipientGroupList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchRecipientGroup() throws Exception {
        int databaseSizeBeforeUpdate = recipientGroupRepository.findAll().size();
        recipientGroup.setId(count.incrementAndGet());

        // Create the RecipientGroup
        RecipientGroupDTO recipientGroupDTO = recipientGroupMapper.toDto(recipientGroup);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restRecipientGroupMockMvc
            .perform(
                put(ENTITY_API_URL_ID, count.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(recipientGroupDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the RecipientGroup in the database
        List<RecipientGroup> recipientGroupList = recipientGroupRepository.findAll();
        assertThat(recipientGroupList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamRecipientGroup() throws Exception {
        int databaseSizeBeforeUpdate = recipientGroupRepository.findAll().size();
        recipientGroup.setId(count.incrementAndGet());

        // Create the RecipientGroup
        RecipientGroupDTO recipientGroupDTO = recipientGroupMapper.toDto(recipientGroup);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restRecipientGroupMockMvc
            .perform(
                put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(recipientGroupDTO))
            )
            .andExpect(status().isMethodNotAllowed());

        // Validate the RecipientGroup in the database
        List<RecipientGroup> recipientGroupList = recipientGroupRepository.findAll();
        assertThat(recipientGroupList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateRecipientGroupWithPatch() throws Exception {
        // Initialize the database
        recipientGroupRepository.saveAndFlush(recipientGroup);

        int databaseSizeBeforeUpdate = recipientGroupRepository.findAll().size();

        // Update the recipientGroup using partial update
        RecipientGroup partialUpdatedRecipientGroup = new RecipientGroup();
        partialUpdatedRecipientGroup.setId(recipientGroup.getId());

        partialUpdatedRecipientGroup.active(UPDATED_ACTIVE).more(UPDATED_MORE);

        restRecipientGroupMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedRecipientGroup.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedRecipientGroup))
            )
            .andExpect(status().isOk());

        // Validate the RecipientGroup in the database
        List<RecipientGroup> recipientGroupList = recipientGroupRepository.findAll();
        assertThat(recipientGroupList).hasSize(databaseSizeBeforeUpdate);
        RecipientGroup testRecipientGroup = recipientGroupList.get(recipientGroupList.size() - 1);
        assertThat(testRecipientGroup.getName()).isEqualTo(DEFAULT_NAME);
        assertThat(testRecipientGroup.getActive()).isEqualTo(UPDATED_ACTIVE);
        assertThat(testRecipientGroup.getMore()).isEqualTo(UPDATED_MORE);
    }

    @Test
    @Transactional
    void fullUpdateRecipientGroupWithPatch() throws Exception {
        // Initialize the database
        recipientGroupRepository.saveAndFlush(recipientGroup);

        int databaseSizeBeforeUpdate = recipientGroupRepository.findAll().size();

        // Update the recipientGroup using partial update
        RecipientGroup partialUpdatedRecipientGroup = new RecipientGroup();
        partialUpdatedRecipientGroup.setId(recipientGroup.getId());

        partialUpdatedRecipientGroup.name(UPDATED_NAME).active(UPDATED_ACTIVE).more(UPDATED_MORE);

        restRecipientGroupMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedRecipientGroup.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedRecipientGroup))
            )
            .andExpect(status().isOk());

        // Validate the RecipientGroup in the database
        List<RecipientGroup> recipientGroupList = recipientGroupRepository.findAll();
        assertThat(recipientGroupList).hasSize(databaseSizeBeforeUpdate);
        RecipientGroup testRecipientGroup = recipientGroupList.get(recipientGroupList.size() - 1);
        assertThat(testRecipientGroup.getName()).isEqualTo(UPDATED_NAME);
        assertThat(testRecipientGroup.getActive()).isEqualTo(UPDATED_ACTIVE);
        assertThat(testRecipientGroup.getMore()).isEqualTo(UPDATED_MORE);
    }

    @Test
    @Transactional
    void patchNonExistingRecipientGroup() throws Exception {
        int databaseSizeBeforeUpdate = recipientGroupRepository.findAll().size();
        recipientGroup.setId(count.incrementAndGet());

        // Create the RecipientGroup
        RecipientGroupDTO recipientGroupDTO = recipientGroupMapper.toDto(recipientGroup);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restRecipientGroupMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, recipientGroupDTO.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(recipientGroupDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the RecipientGroup in the database
        List<RecipientGroup> recipientGroupList = recipientGroupRepository.findAll();
        assertThat(recipientGroupList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchRecipientGroup() throws Exception {
        int databaseSizeBeforeUpdate = recipientGroupRepository.findAll().size();
        recipientGroup.setId(count.incrementAndGet());

        // Create the RecipientGroup
        RecipientGroupDTO recipientGroupDTO = recipientGroupMapper.toDto(recipientGroup);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restRecipientGroupMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, count.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(recipientGroupDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the RecipientGroup in the database
        List<RecipientGroup> recipientGroupList = recipientGroupRepository.findAll();
        assertThat(recipientGroupList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamRecipientGroup() throws Exception {
        int databaseSizeBeforeUpdate = recipientGroupRepository.findAll().size();
        recipientGroup.setId(count.incrementAndGet());

        // Create the RecipientGroup
        RecipientGroupDTO recipientGroupDTO = recipientGroupMapper.toDto(recipientGroup);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restRecipientGroupMockMvc
            .perform(
                patch(ENTITY_API_URL)
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(recipientGroupDTO))
            )
            .andExpect(status().isMethodNotAllowed());

        // Validate the RecipientGroup in the database
        List<RecipientGroup> recipientGroupList = recipientGroupRepository.findAll();
        assertThat(recipientGroupList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteRecipientGroup() throws Exception {
        // Initialize the database
        recipientGroupRepository.saveAndFlush(recipientGroup);

        int databaseSizeBeforeDelete = recipientGroupRepository.findAll().size();

        // Delete the recipientGroup
        restRecipientGroupMockMvc
            .perform(delete(ENTITY_API_URL_ID, recipientGroup.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        List<RecipientGroup> recipientGroupList = recipientGroupRepository.findAll();
        assertThat(recipientGroupList).hasSize(databaseSizeBeforeDelete - 1);
    }
}
