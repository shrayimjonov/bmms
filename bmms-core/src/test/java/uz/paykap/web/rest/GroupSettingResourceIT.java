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
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import uz.paykap.IntegrationTest;
import uz.paykap.domain.GroupSetting;
import uz.paykap.domain.SettingProperty;
import uz.paykap.domain.SettingType;
import uz.paykap.repository.GroupSettingRepository;
import uz.paykap.service.GroupSettingService;
import uz.paykap.service.dto.GroupSettingDTO;
import uz.paykap.service.mapper.GroupSettingMapper;

/**
 * Integration tests for the {@link GroupSettingResource} REST controller.
 */
@IntegrationTest
@ExtendWith(MockitoExtension.class)
@AutoConfigureMockMvc
@WithMockUser
class GroupSettingResourceIT {

    private static final String DEFAULT_REQUEST_GROUP = "AAAAAAAAAA";
    private static final String UPDATED_REQUEST_GROUP = "BBBBBBBBBB";

    private static final String ENTITY_API_URL = "/api/group-settings";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong count = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private GroupSettingRepository groupSettingRepository;

    @Mock
    private GroupSettingRepository groupSettingRepositoryMock;

    @Autowired
    private GroupSettingMapper groupSettingMapper;

    @Mock
    private GroupSettingService groupSettingServiceMock;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restGroupSettingMockMvc;

    private GroupSetting groupSetting;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static GroupSetting createEntity(EntityManager em) {
        //        GroupSetting groupSetting = new GroupSetting().requestGroup(DEFAULT_REQUEST_GROUP);

//        SettingType settingType = SettingTypeResourceIntTest.createEntity(em);
//        em.persist(settingType);
//        em.flush();

        GroupSetting groupSetting = new GroupSetting().requestGroup(DEFAULT_REQUEST_GROUP)
//                .settingType(settingType)
                ;
        // Add required entity
        SettingProperty settingProperty;
        if (TestUtil.findAll(em, SettingProperty.class).isEmpty()) {
            settingProperty = SettingPropertyResourceIT.createEntity(em);
            //            em.persist(settingProperty);
            //            em.flush();
        } else {
            settingProperty = TestUtil.findAll(em, SettingProperty.class).get(0);
        }
        groupSetting.getProps().put(settingProperty.getKey(), settingProperty);
        return groupSetting;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static GroupSetting createUpdatedEntity(EntityManager em) {
        GroupSetting groupSetting = new GroupSetting().requestGroup(UPDATED_REQUEST_GROUP);
        // Add required entity
        SettingProperty settingProperty;
        if (TestUtil.findAll(em, SettingProperty.class).isEmpty()) {
            settingProperty = SettingPropertyResourceIT.createUpdatedEntity(em);
            em.persist(settingProperty);
            em.flush();
        } else {
            settingProperty = TestUtil.findAll(em, SettingProperty.class).get(0);
        }
        groupSetting.getProps().put(settingProperty.getKey(), settingProperty);
        return groupSetting;
    }

    @BeforeEach
    public void initTest() {
        groupSetting = createEntity(em);
    }

    @Test
    @Transactional
    void createGroupSetting() throws Exception {
        int databaseSizeBeforeCreate = groupSettingRepository.findAll().size();
        // Create the GroupSetting
        GroupSettingDTO groupSettingDTO = groupSettingMapper.toDto(groupSetting);
        restGroupSettingMockMvc
            .perform(
                post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(groupSettingDTO))
            )
            .andExpect(status().isCreated());

        // Validate the GroupSetting in the database
        List<GroupSetting> groupSettingList = groupSettingRepository.findAll();
        assertThat(groupSettingList).hasSize(databaseSizeBeforeCreate + 1);
        GroupSetting testGroupSetting = groupSettingList.get(groupSettingList.size() - 1);
        assertThat(testGroupSetting.getRequestGroup()).isEqualTo(DEFAULT_REQUEST_GROUP);
    }

    @Test
    @Transactional
    void createGroupSettingWithExistingId() throws Exception {
        // Create the GroupSetting with an existing ID
        groupSetting.setId(1L);
        GroupSettingDTO groupSettingDTO = groupSettingMapper.toDto(groupSetting);

        int databaseSizeBeforeCreate = groupSettingRepository.findAll().size();

        // An entity with an existing ID cannot be created, so this API call must fail
        restGroupSettingMockMvc
            .perform(
                post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(groupSettingDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the GroupSetting in the database
        List<GroupSetting> groupSettingList = groupSettingRepository.findAll();
        assertThat(groupSettingList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkRequestGroupIsRequired() throws Exception {
        int databaseSizeBeforeTest = groupSettingRepository.findAll().size();
        // set the field null
        groupSetting.setRequestGroup(null);

        // Create the GroupSetting, which fails.
        GroupSettingDTO groupSettingDTO = groupSettingMapper.toDto(groupSetting);

        restGroupSettingMockMvc
            .perform(
                post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(groupSettingDTO))
            )
            .andExpect(status().isBadRequest());

        List<GroupSetting> groupSettingList = groupSettingRepository.findAll();
        assertThat(groupSettingList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllGroupSettings() throws Exception {
        // Initialize the database
        groupSettingRepository.saveAndFlush(groupSetting);

        // Get all the groupSettingList
        restGroupSettingMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(groupSetting.getId().intValue())))
            .andExpect(jsonPath("$.[*].requestGroup").value(hasItem(DEFAULT_REQUEST_GROUP)));
    }

    @SuppressWarnings({ "unchecked" })
    void getAllGroupSettingsWithEagerRelationshipsIsEnabled() throws Exception {
        when(groupSettingServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restGroupSettingMockMvc.perform(get(ENTITY_API_URL + "?eagerload=true")).andExpect(status().isOk());

        verify(groupSettingServiceMock, times(1)).findAllWithEagerRelationships(any());
    }

    @SuppressWarnings({ "unchecked" })
    void getAllGroupSettingsWithEagerRelationshipsIsNotEnabled() throws Exception {
        when(groupSettingServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restGroupSettingMockMvc.perform(get(ENTITY_API_URL + "?eagerload=true")).andExpect(status().isOk());

        verify(groupSettingServiceMock, times(1)).findAllWithEagerRelationships(any());
    }

    @Test
    @Transactional
    void getGroupSetting() throws Exception {
        // Initialize the database
        groupSettingRepository.saveAndFlush(groupSetting);

        // Get the groupSetting
        restGroupSettingMockMvc
            .perform(get(ENTITY_API_URL_ID, groupSetting.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(groupSetting.getId().intValue()))
            .andExpect(jsonPath("$.requestGroup").value(DEFAULT_REQUEST_GROUP));
    }

    @Test
    @Transactional
    void getGroupSettingsByIdFiltering() throws Exception {
        // Initialize the database
        groupSettingRepository.saveAndFlush(groupSetting);

        Long id = groupSetting.getId();

        defaultGroupSettingShouldBeFound("id.equals=" + id);
        defaultGroupSettingShouldNotBeFound("id.notEquals=" + id);

        defaultGroupSettingShouldBeFound("id.greaterThanOrEqual=" + id);
        defaultGroupSettingShouldNotBeFound("id.greaterThan=" + id);

        defaultGroupSettingShouldBeFound("id.lessThanOrEqual=" + id);
        defaultGroupSettingShouldNotBeFound("id.lessThan=" + id);
    }

    @Test
    @Transactional
    void getAllGroupSettingsByRequestGroupIsEqualToSomething() throws Exception {
        // Initialize the database
        groupSettingRepository.saveAndFlush(groupSetting);

        // Get all the groupSettingList where requestGroup equals to DEFAULT_REQUEST_GROUP
        defaultGroupSettingShouldBeFound("requestGroup.equals=" + DEFAULT_REQUEST_GROUP);

        // Get all the groupSettingList where requestGroup equals to UPDATED_REQUEST_GROUP
        defaultGroupSettingShouldNotBeFound("requestGroup.equals=" + UPDATED_REQUEST_GROUP);
    }

    @Test
    @Transactional
    void getAllGroupSettingsByRequestGroupIsNotEqualToSomething() throws Exception {
        // Initialize the database
        groupSettingRepository.saveAndFlush(groupSetting);

        // Get all the groupSettingList where requestGroup not equals to DEFAULT_REQUEST_GROUP
        defaultGroupSettingShouldNotBeFound("requestGroup.notEquals=" + DEFAULT_REQUEST_GROUP);

        // Get all the groupSettingList where requestGroup not equals to UPDATED_REQUEST_GROUP
        defaultGroupSettingShouldBeFound("requestGroup.notEquals=" + UPDATED_REQUEST_GROUP);
    }

    @Test
    @Transactional
    void getAllGroupSettingsByRequestGroupIsInShouldWork() throws Exception {
        // Initialize the database
        groupSettingRepository.saveAndFlush(groupSetting);

        // Get all the groupSettingList where requestGroup in DEFAULT_REQUEST_GROUP or UPDATED_REQUEST_GROUP
        defaultGroupSettingShouldBeFound("requestGroup.in=" + DEFAULT_REQUEST_GROUP + "," + UPDATED_REQUEST_GROUP);

        // Get all the groupSettingList where requestGroup equals to UPDATED_REQUEST_GROUP
        defaultGroupSettingShouldNotBeFound("requestGroup.in=" + UPDATED_REQUEST_GROUP);
    }

    @Test
    @Transactional
    void getAllGroupSettingsByRequestGroupIsNullOrNotNull() throws Exception {
        // Initialize the database
        groupSettingRepository.saveAndFlush(groupSetting);

        // Get all the groupSettingList where requestGroup is not null
        defaultGroupSettingShouldBeFound("requestGroup.specified=true");

        // Get all the groupSettingList where requestGroup is null
        defaultGroupSettingShouldNotBeFound("requestGroup.specified=false");
    }

    @Test
    @Transactional
    void getAllGroupSettingsByRequestGroupContainsSomething() throws Exception {
        // Initialize the database
        groupSettingRepository.saveAndFlush(groupSetting);

        // Get all the groupSettingList where requestGroup contains DEFAULT_REQUEST_GROUP
        defaultGroupSettingShouldBeFound("requestGroup.contains=" + DEFAULT_REQUEST_GROUP);

        // Get all the groupSettingList where requestGroup contains UPDATED_REQUEST_GROUP
        defaultGroupSettingShouldNotBeFound("requestGroup.contains=" + UPDATED_REQUEST_GROUP);
    }

    @Test
    @Transactional
    void getAllGroupSettingsByRequestGroupNotContainsSomething() throws Exception {
        // Initialize the database
        groupSettingRepository.saveAndFlush(groupSetting);

        // Get all the groupSettingList where requestGroup does not contain DEFAULT_REQUEST_GROUP
        defaultGroupSettingShouldNotBeFound("requestGroup.doesNotContain=" + DEFAULT_REQUEST_GROUP);

        // Get all the groupSettingList where requestGroup does not contain UPDATED_REQUEST_GROUP
        defaultGroupSettingShouldBeFound("requestGroup.doesNotContain=" + UPDATED_REQUEST_GROUP);
    }

    @Test
    @Transactional
    void getAllGroupSettingsByPropIsEqualToSomething() throws Exception {
        // Initialize the database
        groupSettingRepository.saveAndFlush(groupSetting);
        SettingProperty prop = SettingPropertyResourceIT.createEntity(em);
        em.persist(prop);
        em.flush();
        groupSetting.addProp(prop);
        groupSettingRepository.saveAndFlush(groupSetting);
        Long propId = prop.getId();

        // Get all the groupSettingList where prop equals to propId
        defaultGroupSettingShouldBeFound("propId.equals=" + propId);

        // Get all the groupSettingList where prop equals to (propId + 1)
        defaultGroupSettingShouldNotBeFound("propId.equals=" + (propId + 1));
    }

    /**
     * Executes the search, and checks that the default entity is returned.
     */
    private void defaultGroupSettingShouldBeFound(String filter) throws Exception {
        restGroupSettingMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(groupSetting.getId().intValue())))
            .andExpect(jsonPath("$.[*].requestGroup").value(hasItem(DEFAULT_REQUEST_GROUP)));

        // Check, that the count call also returns 1
        restGroupSettingMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("1"));
    }

    /**
     * Executes the search, and checks that the default entity is not returned.
     */
    private void defaultGroupSettingShouldNotBeFound(String filter) throws Exception {
        restGroupSettingMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty());

        // Check, that the count call also returns 0
        restGroupSettingMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("0"));
    }

    @Test
    @Transactional
    void getNonExistingGroupSetting() throws Exception {
        // Get the groupSetting
        restGroupSettingMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putNewGroupSetting() throws Exception {
        // Initialize the database
        groupSettingRepository.saveAndFlush(groupSetting);

        int databaseSizeBeforeUpdate = groupSettingRepository.findAll().size();

        // Update the groupSetting
        GroupSetting updatedGroupSetting = groupSettingRepository.findById(groupSetting.getId()).get();
        // Disconnect from session so that the updates on updatedGroupSetting are not directly saved in db
        em.detach(updatedGroupSetting);
        updatedGroupSetting.requestGroup(UPDATED_REQUEST_GROUP);
        GroupSettingDTO groupSettingDTO = groupSettingMapper.toDto(updatedGroupSetting);

        restGroupSettingMockMvc
            .perform(
                put(ENTITY_API_URL_ID, groupSettingDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(groupSettingDTO))
            )
            .andExpect(status().isOk());

        // Validate the GroupSetting in the database
        List<GroupSetting> groupSettingList = groupSettingRepository.findAll();
        assertThat(groupSettingList).hasSize(databaseSizeBeforeUpdate);
        GroupSetting testGroupSetting = groupSettingList.get(groupSettingList.size() - 1);
        assertThat(testGroupSetting.getRequestGroup()).isEqualTo(UPDATED_REQUEST_GROUP);
    }

    @Test
    @Transactional
    void putNonExistingGroupSetting() throws Exception {
        int databaseSizeBeforeUpdate = groupSettingRepository.findAll().size();
        groupSetting.setId(count.incrementAndGet());

        // Create the GroupSetting
        GroupSettingDTO groupSettingDTO = groupSettingMapper.toDto(groupSetting);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restGroupSettingMockMvc
            .perform(
                put(ENTITY_API_URL_ID, groupSettingDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(groupSettingDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the GroupSetting in the database
        List<GroupSetting> groupSettingList = groupSettingRepository.findAll();
        assertThat(groupSettingList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchGroupSetting() throws Exception {
        int databaseSizeBeforeUpdate = groupSettingRepository.findAll().size();
        groupSetting.setId(count.incrementAndGet());

        // Create the GroupSetting
        GroupSettingDTO groupSettingDTO = groupSettingMapper.toDto(groupSetting);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restGroupSettingMockMvc
            .perform(
                put(ENTITY_API_URL_ID, count.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(groupSettingDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the GroupSetting in the database
        List<GroupSetting> groupSettingList = groupSettingRepository.findAll();
        assertThat(groupSettingList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamGroupSetting() throws Exception {
        int databaseSizeBeforeUpdate = groupSettingRepository.findAll().size();
        groupSetting.setId(count.incrementAndGet());

        // Create the GroupSetting
        GroupSettingDTO groupSettingDTO = groupSettingMapper.toDto(groupSetting);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restGroupSettingMockMvc
            .perform(
                put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(groupSettingDTO))
            )
            .andExpect(status().isMethodNotAllowed());

        // Validate the GroupSetting in the database
        List<GroupSetting> groupSettingList = groupSettingRepository.findAll();
        assertThat(groupSettingList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateGroupSettingWithPatch() throws Exception {
        // Initialize the database
        groupSettingRepository.saveAndFlush(groupSetting);

        int databaseSizeBeforeUpdate = groupSettingRepository.findAll().size();

        // Update the groupSetting using partial update
        GroupSetting partialUpdatedGroupSetting = new GroupSetting();
        partialUpdatedGroupSetting.setId(groupSetting.getId());

        restGroupSettingMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedGroupSetting.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedGroupSetting))
            )
            .andExpect(status().isOk());

        // Validate the GroupSetting in the database
        List<GroupSetting> groupSettingList = groupSettingRepository.findAll();
        assertThat(groupSettingList).hasSize(databaseSizeBeforeUpdate);
        GroupSetting testGroupSetting = groupSettingList.get(groupSettingList.size() - 1);
        assertThat(testGroupSetting.getRequestGroup()).isEqualTo(DEFAULT_REQUEST_GROUP);
    }

    @Test
    @Transactional
    void fullUpdateGroupSettingWithPatch() throws Exception {
        // Initialize the database
        groupSettingRepository.saveAndFlush(groupSetting);

        int databaseSizeBeforeUpdate = groupSettingRepository.findAll().size();

        // Update the groupSetting using partial update
        GroupSetting partialUpdatedGroupSetting = new GroupSetting();
        partialUpdatedGroupSetting.setId(groupSetting.getId());

        partialUpdatedGroupSetting.requestGroup(UPDATED_REQUEST_GROUP);

        restGroupSettingMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedGroupSetting.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedGroupSetting))
            )
            .andExpect(status().isOk());

        // Validate the GroupSetting in the database
        List<GroupSetting> groupSettingList = groupSettingRepository.findAll();
        assertThat(groupSettingList).hasSize(databaseSizeBeforeUpdate);
        GroupSetting testGroupSetting = groupSettingList.get(groupSettingList.size() - 1);
        assertThat(testGroupSetting.getRequestGroup()).isEqualTo(UPDATED_REQUEST_GROUP);
    }

    @Test
    @Transactional
    void patchNonExistingGroupSetting() throws Exception {
        int databaseSizeBeforeUpdate = groupSettingRepository.findAll().size();
        groupSetting.setId(count.incrementAndGet());

        // Create the GroupSetting
        GroupSettingDTO groupSettingDTO = groupSettingMapper.toDto(groupSetting);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restGroupSettingMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, groupSettingDTO.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(groupSettingDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the GroupSetting in the database
        List<GroupSetting> groupSettingList = groupSettingRepository.findAll();
        assertThat(groupSettingList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchGroupSetting() throws Exception {
        int databaseSizeBeforeUpdate = groupSettingRepository.findAll().size();
        groupSetting.setId(count.incrementAndGet());

        // Create the GroupSetting
        GroupSettingDTO groupSettingDTO = groupSettingMapper.toDto(groupSetting);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restGroupSettingMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, count.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(groupSettingDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the GroupSetting in the database
        List<GroupSetting> groupSettingList = groupSettingRepository.findAll();
        assertThat(groupSettingList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamGroupSetting() throws Exception {
        int databaseSizeBeforeUpdate = groupSettingRepository.findAll().size();
        groupSetting.setId(count.incrementAndGet());

        // Create the GroupSetting
        GroupSettingDTO groupSettingDTO = groupSettingMapper.toDto(groupSetting);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restGroupSettingMockMvc
            .perform(
                patch(ENTITY_API_URL)
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(groupSettingDTO))
            )
            .andExpect(status().isMethodNotAllowed());

        // Validate the GroupSetting in the database
        List<GroupSetting> groupSettingList = groupSettingRepository.findAll();
        assertThat(groupSettingList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteGroupSetting() throws Exception {
        // Initialize the database
        groupSettingRepository.saveAndFlush(groupSetting);

        int databaseSizeBeforeDelete = groupSettingRepository.findAll().size();

        // Delete the groupSetting
        restGroupSettingMockMvc
            .perform(delete(ENTITY_API_URL_ID, groupSetting.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        List<GroupSetting> groupSettingList = groupSettingRepository.findAll();
        assertThat(groupSettingList).hasSize(databaseSizeBeforeDelete - 1);
    }
}
