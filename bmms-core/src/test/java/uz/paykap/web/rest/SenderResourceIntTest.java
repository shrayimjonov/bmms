package uz.paykap.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static uz.paykap.web.rest.TestUtil.createFormattingConversionService;

import java.util.List;
import javax.persistence.EntityManager;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import uz.paykap.IntegrationTest;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import uz.devops.bmcore.enumeration.PayType;
import uz.paykap.PkbillmsApp;
import uz.paykap.IntegrationTest;
import uz.paykap.domain.Agent;
import uz.paykap.domain.Sender;
import uz.paykap.repository.SenderRepository;
import uz.paykap.service.SenderQueryService;
import uz.paykap.service.SenderService;
import uz.paykap.service.dto.SenderDTO;
import uz.paykap.service.mapper.SenderMapper;
import uz.paykap.web.rest.errors.ExceptionTranslator;

/**
 * Test class for the SenderResource REST controller.
 *
 * @see SenderResource
 */
@RunWith(SpringRunner.class)
@IntegrationTest
public class SenderResourceIntTest {

    private static final String DEFAULT_NAME = "AAAAAAAAAA";
    private static final String UPDATED_NAME = "BBBBBBBBBB";

    private static final Long DEFAULT_ORD = 1L;
    private static final Long UPDATED_ORD = 2L;

    private static final Boolean DEFAULT_ACTIVE = false;
    private static final Boolean UPDATED_ACTIVE = true;

    private static final String DEFAULT_SPRING_BEAN_NAME = "AAAAAAAAAA";
    private static final String UPDATED_SPRING_BEAN_NAME = "BBBBBBBBBB";

    private static final PayType DEFAULT_PAY_TYPE = PayType.CASH;
    private static final PayType UPDATED_PAY_TYPE = PayType.UZCARD;

    @Autowired
    private SenderRepository senderRepository;

    @Autowired
    private SenderMapper senderMapper;

    @Autowired
    private SenderService senderService;

    @Autowired
    private SenderQueryService senderQueryService;

    @Autowired
    private MappingJackson2HttpMessageConverter jacksonMessageConverter;

    @Autowired
    private PageableHandlerMethodArgumentResolver pageableArgumentResolver;

    @Autowired
    private ExceptionTranslator exceptionTranslator;

    @Autowired
    private EntityManager em;

    private MockMvc restSenderMockMvc;

    private Sender sender;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        final SenderResource senderResource = new SenderResource(senderService, senderRepository, senderQueryService);
        this.restSenderMockMvc =
            MockMvcBuilders
                .standaloneSetup(senderResource)
                .setCustomArgumentResolvers(pageableArgumentResolver)
                .setControllerAdvice(exceptionTranslator)
                .setConversionService(createFormattingConversionService())
                .setMessageConverters(jacksonMessageConverter)
                .build();
    }

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Sender createEntity(EntityManager em) {
        Sender sender = new Sender()
            .name(DEFAULT_NAME)
            .ord(DEFAULT_ORD)
            .active(DEFAULT_ACTIVE)
            .springBeanName(DEFAULT_SPRING_BEAN_NAME)
            .payType(DEFAULT_PAY_TYPE);
        return sender;
    }

    @Before
    public void initTest() {
        sender = createEntity(em);
    }

    @Test
    @Transactional
    public void createSender() throws Exception {
        int databaseSizeBeforeCreate = senderRepository.findAll().size();

        // Create the Sender
        SenderDTO senderDTO = senderMapper.toDto(sender);
        restSenderMockMvc
            .perform(post("/api/senders").contentType(TestUtil.APPLICATION_JSON_UTF8).content(TestUtil.convertObjectToJsonBytes(senderDTO)))
            .andExpect(status().isCreated());

        // Validate the Sender in the database
        List<Sender> senderList = senderRepository.findAll();
        assertThat(senderList).hasSize(databaseSizeBeforeCreate + 1);
        Sender testSender = senderList.get(senderList.size() - 1);
        assertThat(testSender.getName()).isEqualTo(DEFAULT_NAME);
        assertThat(testSender.getOrd()).isEqualTo(DEFAULT_ORD);
        assertThat(testSender.isActive()).isEqualTo(DEFAULT_ACTIVE);
        assertThat(testSender.getSpringBeanName()).isEqualTo(DEFAULT_SPRING_BEAN_NAME);
        assertThat(testSender.getPayType()).isEqualTo(DEFAULT_PAY_TYPE);
    }

    @Test
    @Transactional
    public void createSenderWithExistingId() throws Exception {
        int databaseSizeBeforeCreate = senderRepository.findAll().size();

        // Create the Sender with an existing ID
        sender.setId(1L);
        SenderDTO senderDTO = senderMapper.toDto(sender);

        // An entity with an existing ID cannot be created, so this API call must fail
        restSenderMockMvc
            .perform(post("/api/senders").contentType(TestUtil.APPLICATION_JSON_UTF8).content(TestUtil.convertObjectToJsonBytes(senderDTO)))
            .andExpect(status().isBadRequest());

        // Validate the Sender in the database
        List<Sender> senderList = senderRepository.findAll();
        assertThat(senderList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    public void checkNameIsRequired() throws Exception {
        int databaseSizeBeforeTest = senderRepository.findAll().size();
        // set the field null
        sender.setName(null);

        // Create the Sender, which fails.
        SenderDTO senderDTO = senderMapper.toDto(sender);

        restSenderMockMvc
            .perform(post("/api/senders").contentType(TestUtil.APPLICATION_JSON_UTF8).content(TestUtil.convertObjectToJsonBytes(senderDTO)))
            .andExpect(status().isBadRequest());

        List<Sender> senderList = senderRepository.findAll();
        assertThat(senderList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkOrdIsRequired() throws Exception {
        int databaseSizeBeforeTest = senderRepository.findAll().size();
        // set the field null
        sender.setOrd(null);

        // Create the Sender, which fails.
        SenderDTO senderDTO = senderMapper.toDto(sender);

        restSenderMockMvc
            .perform(post("/api/senders").contentType(TestUtil.APPLICATION_JSON_UTF8).content(TestUtil.convertObjectToJsonBytes(senderDTO)))
            .andExpect(status().isBadRequest());

        List<Sender> senderList = senderRepository.findAll();
        assertThat(senderList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkActiveIsRequired() throws Exception {
        int databaseSizeBeforeTest = senderRepository.findAll().size();
        // set the field null
        sender.setActive(null);

        // Create the Sender, which fails.
        SenderDTO senderDTO = senderMapper.toDto(sender);

        restSenderMockMvc
            .perform(post("/api/senders").contentType(TestUtil.APPLICATION_JSON_UTF8).content(TestUtil.convertObjectToJsonBytes(senderDTO)))
            .andExpect(status().isBadRequest());

        List<Sender> senderList = senderRepository.findAll();
        assertThat(senderList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkPayTypeIsRequired() throws Exception {
        int databaseSizeBeforeTest = senderRepository.findAll().size();
        // set the field null
        sender.setPayType(null);

        // Create the Sender, which fails.
        SenderDTO senderDTO = senderMapper.toDto(sender);

        restSenderMockMvc
            .perform(post("/api/senders").contentType(TestUtil.APPLICATION_JSON_UTF8).content(TestUtil.convertObjectToJsonBytes(senderDTO)))
            .andExpect(status().isBadRequest());

        List<Sender> senderList = senderRepository.findAll();
        assertThat(senderList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void getAllSenders() throws Exception {
        // Initialize the database
        senderRepository.saveAndFlush(sender);

        // Get all the senderList
        restSenderMockMvc
            .perform(get("/api/senders?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(sender.getId().intValue())))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME.toString())))
            .andExpect(jsonPath("$.[*].ord").value(hasItem(DEFAULT_ORD.intValue())))
            .andExpect(jsonPath("$.[*].active").value(hasItem(DEFAULT_ACTIVE.booleanValue())))
            .andExpect(jsonPath("$.[*].springBeanName").value(hasItem(DEFAULT_SPRING_BEAN_NAME.toString())))
            .andExpect(jsonPath("$.[*].payType").value(hasItem(DEFAULT_PAY_TYPE.toString())));
    }

    @Test
    @Transactional
    public void getSender() throws Exception {
        // Initialize the database
        senderRepository.saveAndFlush(sender);

        // Get the sender
        restSenderMockMvc
            .perform(get("/api/senders/{id}", sender.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.id").value(sender.getId().intValue()))
            .andExpect(jsonPath("$.name").value(DEFAULT_NAME.toString()))
            .andExpect(jsonPath("$.ord").value(DEFAULT_ORD.intValue()))
            .andExpect(jsonPath("$.active").value(DEFAULT_ACTIVE.booleanValue()))
            .andExpect(jsonPath("$.springBeanName").value(DEFAULT_SPRING_BEAN_NAME.toString()))
            .andExpect(jsonPath("$.payType").value(DEFAULT_PAY_TYPE.toString()));
    }

    @Test
    @Transactional
    public void getAllSendersByNameIsEqualToSomething() throws Exception {
        // Initialize the database
        senderRepository.saveAndFlush(sender);

        // Get all the senderList where name equals to DEFAULT_NAME
        defaultSenderShouldBeFound("name.equals=" + DEFAULT_NAME);

        // Get all the senderList where name equals to UPDATED_NAME
        defaultSenderShouldNotBeFound("name.equals=" + UPDATED_NAME);
    }

    @Test
    @Transactional
    public void getAllSendersByNameIsInShouldWork() throws Exception {
        // Initialize the database
        senderRepository.saveAndFlush(sender);

        // Get all the senderList where name in DEFAULT_NAME or UPDATED_NAME
        defaultSenderShouldBeFound("name.in=" + DEFAULT_NAME + "," + UPDATED_NAME);

        // Get all the senderList where name equals to UPDATED_NAME
        defaultSenderShouldNotBeFound("name.in=" + UPDATED_NAME);
    }

    @Test
    @Transactional
    public void getAllSendersByNameIsNullOrNotNull() throws Exception {
        // Initialize the database
        senderRepository.saveAndFlush(sender);

        // Get all the senderList where name is not null
        defaultSenderShouldBeFound("name.specified=true");

        // Get all the senderList where name is null
        defaultSenderShouldNotBeFound("name.specified=false");
    }

    @Test
    @Transactional
    public void getAllSendersByOrdIsEqualToSomething() throws Exception {
        // Initialize the database
        senderRepository.saveAndFlush(sender);

        // Get all the senderList where ord equals to DEFAULT_ORD
        defaultSenderShouldBeFound("ord.equals=" + DEFAULT_ORD);

        // Get all the senderList where ord equals to UPDATED_ORD
        defaultSenderShouldNotBeFound("ord.equals=" + UPDATED_ORD);
    }

    @Test
    @Transactional
    public void getAllSendersByOrdIsInShouldWork() throws Exception {
        // Initialize the database
        senderRepository.saveAndFlush(sender);

        // Get all the senderList where ord in DEFAULT_ORD or UPDATED_ORD
        defaultSenderShouldBeFound("ord.in=" + DEFAULT_ORD + "," + UPDATED_ORD);

        // Get all the senderList where ord equals to UPDATED_ORD
        defaultSenderShouldNotBeFound("ord.in=" + UPDATED_ORD);
    }

    @Test
    @Transactional
    public void getAllSendersByOrdIsNullOrNotNull() throws Exception {
        // Initialize the database
        senderRepository.saveAndFlush(sender);

        // Get all the senderList where ord is not null
        defaultSenderShouldBeFound("ord.specified=true");

        // Get all the senderList where ord is null
        defaultSenderShouldNotBeFound("ord.specified=false");
    }

    @Test
    @Transactional
    public void getAllSendersByOrdIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        senderRepository.saveAndFlush(sender);

        // Get all the senderList where ord greater than or equals to DEFAULT_ORD
        defaultSenderShouldBeFound("ord.greaterOrEqualThan=" + DEFAULT_ORD);

        // Get all the senderList where ord greater than or equals to UPDATED_ORD
        defaultSenderShouldNotBeFound("ord.greaterOrEqualThan=" + UPDATED_ORD);
    }

    @Test
    @Transactional
    public void getAllSendersByOrdIsLessThanSomething() throws Exception {
        // Initialize the database
        senderRepository.saveAndFlush(sender);

        // Get all the senderList where ord less than or equals to DEFAULT_ORD
        defaultSenderShouldNotBeFound("ord.lessThan=" + DEFAULT_ORD);

        // Get all the senderList where ord less than or equals to UPDATED_ORD
        defaultSenderShouldBeFound("ord.lessThan=" + UPDATED_ORD);
    }

    @Test
    @Transactional
    public void getAllSendersByActiveIsEqualToSomething() throws Exception {
        // Initialize the database
        senderRepository.saveAndFlush(sender);

        // Get all the senderList where active equals to DEFAULT_ACTIVE
        defaultSenderShouldBeFound("active.equals=" + DEFAULT_ACTIVE);

        // Get all the senderList where active equals to UPDATED_ACTIVE
        defaultSenderShouldNotBeFound("active.equals=" + UPDATED_ACTIVE);
    }

    @Test
    @Transactional
    public void getAllSendersByActiveIsInShouldWork() throws Exception {
        // Initialize the database
        senderRepository.saveAndFlush(sender);

        // Get all the senderList where active in DEFAULT_ACTIVE or UPDATED_ACTIVE
        defaultSenderShouldBeFound("active.in=" + DEFAULT_ACTIVE + "," + UPDATED_ACTIVE);

        // Get all the senderList where active equals to UPDATED_ACTIVE
        defaultSenderShouldNotBeFound("active.in=" + UPDATED_ACTIVE);
    }

    @Test
    @Transactional
    public void getAllSendersByActiveIsNullOrNotNull() throws Exception {
        // Initialize the database
        senderRepository.saveAndFlush(sender);

        // Get all the senderList where active is not null
        defaultSenderShouldBeFound("active.specified=true");

        // Get all the senderList where active is null
        defaultSenderShouldNotBeFound("active.specified=false");
    }

    @Test
    @Transactional
    public void getAllSendersBySpringBeanNameIsEqualToSomething() throws Exception {
        // Initialize the database
        senderRepository.saveAndFlush(sender);

        // Get all the senderList where springBeanName equals to DEFAULT_SPRING_BEAN_NAME
        defaultSenderShouldBeFound("springBeanName.equals=" + DEFAULT_SPRING_BEAN_NAME);

        // Get all the senderList where springBeanName equals to UPDATED_SPRING_BEAN_NAME
        defaultSenderShouldNotBeFound("springBeanName.equals=" + UPDATED_SPRING_BEAN_NAME);
    }

    @Test
    @Transactional
    public void getAllSendersBySpringBeanNameIsInShouldWork() throws Exception {
        // Initialize the database
        senderRepository.saveAndFlush(sender);

        // Get all the senderList where springBeanName in DEFAULT_SPRING_BEAN_NAME or UPDATED_SPRING_BEAN_NAME
        defaultSenderShouldBeFound("springBeanName.in=" + DEFAULT_SPRING_BEAN_NAME + "," + UPDATED_SPRING_BEAN_NAME);

        // Get all the senderList where springBeanName equals to UPDATED_SPRING_BEAN_NAME
        defaultSenderShouldNotBeFound("springBeanName.in=" + UPDATED_SPRING_BEAN_NAME);
    }

    @Test
    @Transactional
    public void getAllSendersBySpringBeanNameIsNullOrNotNull() throws Exception {
        // Initialize the database
        senderRepository.saveAndFlush(sender);

        // Get all the senderList where springBeanName is not null
        defaultSenderShouldBeFound("springBeanName.specified=true");

        // Get all the senderList where springBeanName is null
        defaultSenderShouldNotBeFound("springBeanName.specified=false");
    }

    @Test
    @Transactional
    public void getAllSendersByPayTypeIsEqualToSomething() throws Exception {
        // Initialize the database
        senderRepository.saveAndFlush(sender);

        // Get all the senderList where payType equals to DEFAULT_PAY_TYPE
        defaultSenderShouldBeFound("payType.equals=" + DEFAULT_PAY_TYPE);

        // Get all the senderList where payType equals to UPDATED_PAY_TYPE
        defaultSenderShouldNotBeFound("payType.equals=" + UPDATED_PAY_TYPE);
    }

    @Test
    @Transactional
    public void getAllSendersByPayTypeIsInShouldWork() throws Exception {
        // Initialize the database
        senderRepository.saveAndFlush(sender);

        // Get all the senderList where payType in DEFAULT_PAY_TYPE or UPDATED_PAY_TYPE
        defaultSenderShouldBeFound("payType.in=" + DEFAULT_PAY_TYPE + "," + UPDATED_PAY_TYPE);

        // Get all the senderList where payType equals to UPDATED_PAY_TYPE
        defaultSenderShouldNotBeFound("payType.in=" + UPDATED_PAY_TYPE);
    }

    @Test
    @Transactional
    public void getAllSendersByPayTypeIsNullOrNotNull() throws Exception {
        // Initialize the database
        senderRepository.saveAndFlush(sender);

        // Get all the senderList where payType is not null
        defaultSenderShouldBeFound("payType.specified=true");

        // Get all the senderList where payType is null
        defaultSenderShouldNotBeFound("payType.specified=false");
    }

    @Test
    @Transactional
    public void getAllSendersByAgentIsEqualToSomething() throws Exception {
        // Initialize the database
        Agent agent = AgentResourceIntTest.createEntity(em);
        em.persist(agent);
        em.flush();
        sender.setAgent(agent);
        senderRepository.saveAndFlush(sender);
        Long agentId = agent.getId();

        // Get all the senderList where agent equals to agentId
        defaultSenderShouldBeFound("agentId.equals=" + agentId);

        // Get all the senderList where agent equals to agentId + 1
        defaultSenderShouldNotBeFound("agentId.equals=" + (agentId + 1));
    }

    /**
     * Executes the search, and checks that the default entity is returned
     */
    private void defaultSenderShouldBeFound(String filter) throws Exception {
        restSenderMockMvc
            .perform(get("/api/senders?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(sender.getId().intValue())))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME.toString())))
            .andExpect(jsonPath("$.[*].ord").value(hasItem(DEFAULT_ORD.intValue())))
            .andExpect(jsonPath("$.[*].active").value(hasItem(DEFAULT_ACTIVE.booleanValue())))
            .andExpect(jsonPath("$.[*].springBeanName").value(hasItem(DEFAULT_SPRING_BEAN_NAME.toString())))
            .andExpect(jsonPath("$.[*].payType").value(hasItem(DEFAULT_PAY_TYPE.toString())));
    }

    /**
     * Executes the search, and checks that the default entity is not returned
     */
    private void defaultSenderShouldNotBeFound(String filter) throws Exception {
        restSenderMockMvc
            .perform(get("/api/senders?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    @Transactional
    public void getNonExistingSender() throws Exception {
        // Get the sender
        restSenderMockMvc.perform(get("/api/senders/{id}", Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updateSender() throws Exception {
        // Initialize the database
        senderRepository.saveAndFlush(sender);

        int databaseSizeBeforeUpdate = senderRepository.findAll().size();

        // Update the sender
        Sender updatedSender = senderRepository.findById(sender.getId()).get();
        // Disconnect from session so that the updates on updatedSender are not directly saved in db
        em.detach(updatedSender);
        updatedSender
            .name(UPDATED_NAME)
            .ord(UPDATED_ORD)
            .active(UPDATED_ACTIVE)
            .springBeanName(UPDATED_SPRING_BEAN_NAME)
            .payType(UPDATED_PAY_TYPE);
        SenderDTO senderDTO = senderMapper.toDto(updatedSender);

        restSenderMockMvc
            .perform(put("/api/senders").contentType(TestUtil.APPLICATION_JSON_UTF8).content(TestUtil.convertObjectToJsonBytes(senderDTO)))
            .andExpect(status().isOk());

        // Validate the Sender in the database
        List<Sender> senderList = senderRepository.findAll();
        assertThat(senderList).hasSize(databaseSizeBeforeUpdate);
        Sender testSender = senderList.get(senderList.size() - 1);
        assertThat(testSender.getName()).isEqualTo(UPDATED_NAME);
        assertThat(testSender.getOrd()).isEqualTo(UPDATED_ORD);
        assertThat(testSender.isActive()).isEqualTo(UPDATED_ACTIVE);
        assertThat(testSender.getSpringBeanName()).isEqualTo(UPDATED_SPRING_BEAN_NAME);
        assertThat(testSender.getPayType()).isEqualTo(UPDATED_PAY_TYPE);
    }

    @Test
    @Transactional
    public void updateNonExistingSender() throws Exception {
        int databaseSizeBeforeUpdate = senderRepository.findAll().size();

        // Create the Sender
        SenderDTO senderDTO = senderMapper.toDto(sender);

        // If the entity doesn't have an ID, it will be created instead of just being updated
        restSenderMockMvc
            .perform(put("/api/senders").contentType(TestUtil.APPLICATION_JSON_UTF8).content(TestUtil.convertObjectToJsonBytes(senderDTO)))
            .andExpect(status().isBadRequest());

        // Validate the Sender in the database
        List<Sender> senderList = senderRepository.findAll();
        assertThat(senderList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    public void deleteSender() throws Exception {
        // Initialize the database
        senderRepository.saveAndFlush(sender);

        int databaseSizeBeforeDelete = senderRepository.findAll().size();

        // Get the sender
        restSenderMockMvc
            .perform(delete("/api/senders/{id}", sender.getId()).accept(TestUtil.APPLICATION_JSON_UTF8))
            .andExpect(status().isOk());

        // Validate the database is empty
        List<Sender> senderList = senderRepository.findAll();
        assertThat(senderList).hasSize(databaseSizeBeforeDelete - 1);
    }

    @Test
    @Transactional
    public void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Sender.class);
        Sender sender1 = new Sender();
        sender1.setId(1L);
        Sender sender2 = new Sender();
        sender2.setId(sender1.getId());
        assertThat(sender1).isEqualTo(sender2);
        sender2.setId(2L);
        assertThat(sender1).isNotEqualTo(sender2);
        sender1.setId(null);
        assertThat(sender1).isNotEqualTo(sender2);
    }

    @Test
    @Transactional
    public void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(SenderDTO.class);
        SenderDTO senderDTO1 = new SenderDTO();
        senderDTO1.setId(1L);
        SenderDTO senderDTO2 = new SenderDTO();
        assertThat(senderDTO1).isNotEqualTo(senderDTO2);
        senderDTO2.setId(senderDTO1.getId());
        assertThat(senderDTO1).isEqualTo(senderDTO2);
        senderDTO2.setId(2L);
        assertThat(senderDTO1).isNotEqualTo(senderDTO2);
        senderDTO1.setId(null);
        assertThat(senderDTO1).isNotEqualTo(senderDTO2);
    }

    @Test
    @Transactional
    public void testEntityFromId() {
        assertThat(senderMapper.fromId(42L).getId()).isEqualTo(42);
        assertThat(senderMapper.fromId(null)).isNull();
    }
}
