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
import org.springframework.validation.Validator;
import uz.paykap.PkbillmsApp;
import uz.paykap.IntegrationTest;
import uz.paykap.domain.GroupSetting;
import uz.paykap.domain.SettingProperty;
import uz.paykap.domain.SettingType;
import uz.paykap.repository.GroupSettingRepository;
import uz.paykap.service.GroupSettingQueryService;
import uz.paykap.service.GroupSettingService;
import uz.paykap.service.dto.GroupSettingDTO;
import uz.paykap.service.mapper.GroupSettingMapper;
import uz.paykap.web.rest.errors.ExceptionTranslator;

/**
 * Test class for the GroupSettingResource REST controller.
 *
 * @see GroupSettingResource
 */
@RunWith(SpringRunner.class)
@IntegrationTest
public class GroupSettingResourceIntTest {

    private static final String DEFAULT_REQUEST_GROUP = "AAAAAAAAAA";
    private static final String UPDATED_REQUEST_GROUP = "BBBBBBBBBB";

    @Autowired
    private GroupSettingRepository groupSettingRepository;

    @Mock
    private GroupSettingRepository groupSettingRepositoryMock;

    @Autowired
    private GroupSettingMapper groupSettingMapper;

    @Mock
    private GroupSettingService groupSettingServiceMock;

    @Autowired
    private GroupSettingService groupSettingService;

    @Autowired
    private GroupSettingQueryService groupSettingQueryService;

    @Autowired
    private MappingJackson2HttpMessageConverter jacksonMessageConverter;

    @Autowired
    private PageableHandlerMethodArgumentResolver pageableArgumentResolver;

    @Autowired
    private ExceptionTranslator exceptionTranslator;

    @Autowired
    private EntityManager em;

    @Autowired
    private Validator validator;

    private MockMvc restGroupSettingMockMvc;

    private GroupSetting groupSetting;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        final GroupSettingResource groupSettingResource = new GroupSettingResource(
            groupSettingService,
            groupSettingRepository,
            groupSettingQueryService
        );
        this.restGroupSettingMockMvc =
            MockMvcBuilders
                .standaloneSetup(groupSettingResource)
                .setCustomArgumentResolvers(pageableArgumentResolver)
                .setControllerAdvice(exceptionTranslator)
                .setConversionService(createFormattingConversionService())
                .setMessageConverters(jacksonMessageConverter)
                .setValidator(validator)
                .build();
    }

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static GroupSetting createEntity(EntityManager em) {
//        SettingType settingType = SettingTypeResourceIntTest.createEntity(em);
//        em.persist(settingType);
//        em.flush();

        GroupSetting groupSetting = new GroupSetting().requestGroup(DEFAULT_REQUEST_GROUP)
//                .settingType(settingType)
                ;
        // Add required entity
        SettingProperty settingProperty = SettingPropertyResourceIntTest.createEntity(em);

        /* Below 2 lines commented because, we use Cascaded = ALL, so SettingProperty should not be already in db */
        //        em.persist(settingProperty);
        //        em.flush();

        groupSetting.getProps().put(settingProperty.getKey(), settingProperty);
        return groupSetting;
    }

    @Before
    public void initTest() {
        groupSetting = createEntity(em);
    }

    @Test
    @Transactional
    public void createGroupSetting() throws Exception {
        int databaseSizeBeforeCreate = groupSettingRepository.findAll().size();

        // Create the GroupSetting
        GroupSettingDTO groupSettingDTO = groupSettingMapper.toDto(groupSetting);
        restGroupSettingMockMvc
            .perform(
                post("/api/group-settings")
                    .contentType(TestUtil.APPLICATION_JSON_UTF8)
                    .content(TestUtil.convertObjectToJsonBytes(groupSettingDTO))
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
    public void createGroupSettingWithExistingId() throws Exception {
        int databaseSizeBeforeCreate = groupSettingRepository.findAll().size();

        // Create the GroupSetting with an existing ID
        groupSetting.setId(1L);
        GroupSettingDTO groupSettingDTO = groupSettingMapper.toDto(groupSetting);

        // An entity with an existing ID cannot be created, so this API call must fail
        restGroupSettingMockMvc
            .perform(
                post("/api/group-settings")
                    .contentType(TestUtil.APPLICATION_JSON_UTF8)
                    .content(TestUtil.convertObjectToJsonBytes(groupSettingDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the GroupSetting in the database
        List<GroupSetting> groupSettingList = groupSettingRepository.findAll();
        assertThat(groupSettingList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    public void checkRequestGroupIsRequired() throws Exception {
        int databaseSizeBeforeTest = groupSettingRepository.findAll().size();
        // set the field null
        groupSetting.setRequestGroup(null);

        // Create the GroupSetting, which fails.
        GroupSettingDTO groupSettingDTO = groupSettingMapper.toDto(groupSetting);

        restGroupSettingMockMvc
            .perform(
                post("/api/group-settings")
                    .contentType(TestUtil.APPLICATION_JSON_UTF8)
                    .content(TestUtil.convertObjectToJsonBytes(groupSettingDTO))
            )
            .andExpect(status().isBadRequest());

        List<GroupSetting> groupSettingList = groupSettingRepository.findAll();
        assertThat(groupSettingList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void getAllGroupSettings() throws Exception {
        // Initialize the database
        groupSettingRepository.saveAndFlush(groupSetting);

        // Get all the groupSettingList
        restGroupSettingMockMvc
            .perform(get("/api/group-settings?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(groupSetting.getId().intValue())))
            .andExpect(jsonPath("$.[*].requestGroup").value(hasItem(DEFAULT_REQUEST_GROUP.toString())));
    }

    @SuppressWarnings({ "unchecked" })
    public void getAllGroupSettingsWithEagerRelationshipsIsEnabled() throws Exception {
        GroupSettingResource groupSettingResource = new GroupSettingResource(
            groupSettingServiceMock,
            groupSettingRepository,
            groupSettingQueryService
        );
        when(groupSettingServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        MockMvc restGroupSettingMockMvc = MockMvcBuilders
            .standaloneSetup(groupSettingResource)
            .setCustomArgumentResolvers(pageableArgumentResolver)
            .setControllerAdvice(exceptionTranslator)
            .setConversionService(createFormattingConversionService())
            .setMessageConverters(jacksonMessageConverter)
            .build();

        restGroupSettingMockMvc.perform(get("/api/group-settings?eagerload=true")).andExpect(status().isOk());

        verify(groupSettingServiceMock, times(1)).findAllWithEagerRelationships(any());
    }

    @SuppressWarnings({ "unchecked" })
    public void getAllGroupSettingsWithEagerRelationshipsIsNotEnabled() throws Exception {
        GroupSettingResource groupSettingResource = new GroupSettingResource(
            groupSettingServiceMock,
            groupSettingRepository,
            groupSettingQueryService
        );
        when(groupSettingServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));
        MockMvc restGroupSettingMockMvc = MockMvcBuilders
            .standaloneSetup(groupSettingResource)
            .setCustomArgumentResolvers(pageableArgumentResolver)
            .setControllerAdvice(exceptionTranslator)
            .setConversionService(createFormattingConversionService())
            .setMessageConverters(jacksonMessageConverter)
            .build();

        restGroupSettingMockMvc.perform(get("/api/group-settings?eagerload=true")).andExpect(status().isOk());

        verify(groupSettingServiceMock, times(1)).findAllWithEagerRelationships(any());
    }

    @Test
    @Transactional
    public void getGroupSetting() throws Exception {
        // Initialize the database
        groupSettingRepository.saveAndFlush(groupSetting);

        // Get the groupSetting
        restGroupSettingMockMvc
            .perform(get("/api/group-settings/{id}", groupSetting.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.id").value(groupSetting.getId().intValue()))
            .andExpect(jsonPath("$.requestGroup").value(DEFAULT_REQUEST_GROUP.toString()));
    }

    @Test
    @Transactional
    public void getAllGroupSettingsByRequestGroupIsEqualToSomething() throws Exception {
        // Initialize the database
        groupSettingRepository.saveAndFlush(groupSetting);

        // Get all the groupSettingList where requestGroup equals to DEFAULT_REQUEST_GROUP
        defaultGroupSettingShouldBeFound("requestGroup.equals=" + DEFAULT_REQUEST_GROUP);

        // Get all the groupSettingList where requestGroup equals to UPDATED_REQUEST_GROUP
        defaultGroupSettingShouldNotBeFound("requestGroup.equals=" + UPDATED_REQUEST_GROUP);
    }

    @Test
    @Transactional
    public void getAllGroupSettingsByRequestGroupIsInShouldWork() throws Exception {
        // Initialize the database
        groupSettingRepository.saveAndFlush(groupSetting);

        // Get all the groupSettingList where requestGroup in DEFAULT_REQUEST_GROUP or UPDATED_REQUEST_GROUP
        defaultGroupSettingShouldBeFound("requestGroup.in=" + DEFAULT_REQUEST_GROUP + "," + UPDATED_REQUEST_GROUP);

        // Get all the groupSettingList where requestGroup equals to UPDATED_REQUEST_GROUP
        defaultGroupSettingShouldNotBeFound("requestGroup.in=" + UPDATED_REQUEST_GROUP);
    }

    @Test
    @Transactional
    public void getAllGroupSettingsByRequestGroupIsNullOrNotNull() throws Exception {
        // Initialize the database
        groupSettingRepository.saveAndFlush(groupSetting);

        // Get all the groupSettingList where requestGroup is not null
        defaultGroupSettingShouldBeFound("requestGroup.specified=true");

        // Get all the groupSettingList where requestGroup is null
        defaultGroupSettingShouldNotBeFound("requestGroup.specified=false");
    }

    @Test
    @Transactional
    public void getAllGroupSettingsByPropIsEqualToSomething() throws Exception {
        // Initialize the database
        SettingProperty prop = SettingPropertyResourceIntTest.createEntity(em);
        em.persist(prop);
        em.flush();
        groupSetting.addProp(prop);
        groupSettingRepository.saveAndFlush(groupSetting);
        Long propId = prop.getId();

        // Get all the groupSettingList where prop equals to propId
        defaultGroupSettingShouldBeFound("propId.equals=" + propId);

        // Get all the groupSettingList where prop equals to propId + 1
        defaultGroupSettingShouldNotBeFound("propId.equals=" + (propId + 1));
    }

    /**
     * Executes the search, and checks that the default entity is returned
     */
    private void defaultGroupSettingShouldBeFound(String filter) throws Exception {
        restGroupSettingMockMvc
            .perform(get("/api/group-settings?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(groupSetting.getId().intValue())))
            .andExpect(jsonPath("$.[*].requestGroup").value(hasItem(DEFAULT_REQUEST_GROUP.toString())));

        // Check, that the count call also returns 1
        restGroupSettingMockMvc
            .perform(get("/api/group-settings/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(content().string("1"));
    }

    /**
     * Executes the search, and checks that the default entity is not returned
     */
    private void defaultGroupSettingShouldNotBeFound(String filter) throws Exception {
        restGroupSettingMockMvc
            .perform(get("/api/group-settings?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty());

        // Check, that the count call also returns 0
        restGroupSettingMockMvc
            .perform(get("/api/group-settings/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(content().string("0"));
    }

    @Test
    @Transactional
    public void getNonExistingGroupSetting() throws Exception {
        // Get the groupSetting
        restGroupSettingMockMvc.perform(get("/api/group-settings/{id}", Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updateGroupSetting() throws Exception {
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
                put("/api/group-settings")
                    .contentType(TestUtil.APPLICATION_JSON_UTF8)
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
    public void updateNonExistingGroupSetting() throws Exception {
        int databaseSizeBeforeUpdate = groupSettingRepository.findAll().size();

        // Create the GroupSetting
        GroupSettingDTO groupSettingDTO = groupSettingMapper.toDto(groupSetting);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restGroupSettingMockMvc
            .perform(
                put("/api/group-settings")
                    .contentType(TestUtil.APPLICATION_JSON_UTF8)
                    .content(TestUtil.convertObjectToJsonBytes(groupSettingDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the GroupSetting in the database
        List<GroupSetting> groupSettingList = groupSettingRepository.findAll();
        assertThat(groupSettingList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    public void deleteGroupSetting() throws Exception {
        // Initialize the database
        groupSettingRepository.saveAndFlush(groupSetting);

        int databaseSizeBeforeDelete = groupSettingRepository.findAll().size();

        // Get the groupSetting
        restGroupSettingMockMvc
            .perform(delete("/api/group-settings/{id}", groupSetting.getId()).accept(TestUtil.APPLICATION_JSON_UTF8))
            .andExpect(status().isOk());

        // Validate the database is empty
        List<GroupSetting> groupSettingList = groupSettingRepository.findAll();
        assertThat(groupSettingList).hasSize(databaseSizeBeforeDelete - 1);
    }

    @Test
    @Transactional
    public void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(GroupSetting.class);
        GroupSetting groupSetting1 = new GroupSetting();
        groupSetting1.setId(1L);
        GroupSetting groupSetting2 = new GroupSetting();
        groupSetting2.setId(groupSetting1.getId());
        assertThat(groupSetting1).isEqualTo(groupSetting2);
        groupSetting2.setId(2L);
        assertThat(groupSetting1).isNotEqualTo(groupSetting2);
        groupSetting1.setId(null);
        assertThat(groupSetting1).isNotEqualTo(groupSetting2);
    }

    @Test
    @Transactional
    public void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(GroupSettingDTO.class);
        GroupSettingDTO groupSettingDTO1 = new GroupSettingDTO();
        groupSettingDTO1.setId(1L);
        GroupSettingDTO groupSettingDTO2 = new GroupSettingDTO();
        assertThat(groupSettingDTO1).isNotEqualTo(groupSettingDTO2);
        groupSettingDTO2.setId(groupSettingDTO1.getId());
        assertThat(groupSettingDTO1).isEqualTo(groupSettingDTO2);
        groupSettingDTO2.setId(2L);
        assertThat(groupSettingDTO1).isNotEqualTo(groupSettingDTO2);
        groupSettingDTO1.setId(null);
        assertThat(groupSettingDTO1).isNotEqualTo(groupSettingDTO2);
    }
    //
    //    @Test
    //    @Transactional
    //    public void testEntityFromId() {
    //        assertThat(groupSettingMapper.fromId(42L).getId()).isEqualTo(42);
    //        assertThat(groupSettingMapper.fromId(null)).isNull();
    //    }
}
