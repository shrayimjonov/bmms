package uz.paykap.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;
import javax.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import uz.paykap.IntegrationTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import uz.paykap.IntegrationTest;
import uz.paykap.PkbillmsApp;
import uz.paykap.domain.P2pCommission;
import uz.paykap.repository.P2pCommissionRepository;
import uz.paykap.service.criteria.P2pCommissionCriteria;

/**
 * Integration tests for the {@link P2pCommissionResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
public class P2pCommissionResourceIT {

    private static final String DEFAULT_NAME = "AAAAAAAAAA";
    private static final String UPDATED_NAME = "BBBBBBBBBB";

    private static final Double DEFAULT_UP = 0D;
    private static final Double UPDATED_UP = 1D;
    private static final Double SMALLER_UP = 0D - 1D;

    private static final Double DEFAULT_DOWN = 0D;
    private static final Double UPDATED_DOWN = 1D;
    private static final Double SMALLER_DOWN = 0D - 1D;

    private static final Double DEFAULT_FEE = 0D;
    private static final Double UPDATED_FEE = 1D;
    private static final Double SMALLER_FEE = 0D - 1D;

    private static final String DEFAULT_DESCRIPTION = "AAAAAAAAAA";
    private static final String UPDATED_DESCRIPTION = "BBBBBBBBBB";

    private static final String ENTITY_API_URL = "/api/p-2-p-commissions";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong count = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private P2pCommissionRepository p2pCommissionRepository;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restP2pCommissionMockMvc;

    private P2pCommission p2pCommission;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static P2pCommission createEntity(EntityManager em) {
        P2pCommission p2pCommission = new P2pCommission()
            .name(DEFAULT_NAME)
            .up(DEFAULT_UP)
            .down(DEFAULT_DOWN)
            .fee(DEFAULT_FEE)
            .description(DEFAULT_DESCRIPTION);
        return p2pCommission;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static P2pCommission createUpdatedEntity(EntityManager em) {
        P2pCommission p2pCommission = new P2pCommission()
            .name(UPDATED_NAME)
            .up(UPDATED_UP)
            .down(UPDATED_DOWN)
            .fee(UPDATED_FEE)
            .description(UPDATED_DESCRIPTION);
        return p2pCommission;
    }

    @BeforeEach
    public void initTest() {
        p2pCommission = createEntity(em);
    }

    @Test
    @Transactional
    void createP2pCommission() throws Exception {
        int databaseSizeBeforeCreate = p2pCommissionRepository.findAll().size();
        // Create the P2pCommission
        restP2pCommissionMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(p2pCommission)))
            .andExpect(status().isCreated());

        // Validate the P2pCommission in the database
        List<P2pCommission> p2pCommissionList = p2pCommissionRepository.findAll();
        assertThat(p2pCommissionList).hasSize(databaseSizeBeforeCreate + 1);
        P2pCommission testP2pCommission = p2pCommissionList.get(p2pCommissionList.size() - 1);
        assertThat(testP2pCommission.getName()).isEqualTo(DEFAULT_NAME);
        assertThat(testP2pCommission.getUp()).isEqualTo(DEFAULT_UP);
        assertThat(testP2pCommission.getDown()).isEqualTo(DEFAULT_DOWN);
        assertThat(testP2pCommission.getFee()).isEqualTo(DEFAULT_FEE);
        assertThat(testP2pCommission.getDescription()).isEqualTo(DEFAULT_DESCRIPTION);
    }

    @Test
    @Transactional
    void createP2pCommissionWithExistingId() throws Exception {
        // Create the P2pCommission with an existing ID
        p2pCommission.setId(1L);

        int databaseSizeBeforeCreate = p2pCommissionRepository.findAll().size();

        // An entity with an existing ID cannot be created, so this API call must fail
        restP2pCommissionMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(p2pCommission)))
            .andExpect(status().isBadRequest());

        // Validate the P2pCommission in the database
        List<P2pCommission> p2pCommissionList = p2pCommissionRepository.findAll();
        assertThat(p2pCommissionList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkNameIsRequired() throws Exception {
        int databaseSizeBeforeTest = p2pCommissionRepository.findAll().size();
        // set the field null
        p2pCommission.setName(null);

        // Create the P2pCommission, which fails.

        restP2pCommissionMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(p2pCommission)))
            .andExpect(status().isBadRequest());

        List<P2pCommission> p2pCommissionList = p2pCommissionRepository.findAll();
        assertThat(p2pCommissionList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllP2pCommissions() throws Exception {
        // Initialize the database
        p2pCommissionRepository.saveAndFlush(p2pCommission);

        // Get all the p2pCommissionList
        restP2pCommissionMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(p2pCommission.getId().intValue())))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME)))
            .andExpect(jsonPath("$.[*].up").value(hasItem(DEFAULT_UP.doubleValue())))
            .andExpect(jsonPath("$.[*].down").value(hasItem(DEFAULT_DOWN.doubleValue())))
            .andExpect(jsonPath("$.[*].fee").value(hasItem(DEFAULT_FEE.doubleValue())))
            .andExpect(jsonPath("$.[*].description").value(hasItem(DEFAULT_DESCRIPTION)));
    }

    @Test
    @Transactional
    void getP2pCommission() throws Exception {
        // Initialize the database
        p2pCommissionRepository.saveAndFlush(p2pCommission);

        // Get the p2pCommission
        restP2pCommissionMockMvc
            .perform(get(ENTITY_API_URL_ID, p2pCommission.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(p2pCommission.getId().intValue()))
            .andExpect(jsonPath("$.name").value(DEFAULT_NAME))
            .andExpect(jsonPath("$.up").value(DEFAULT_UP.doubleValue()))
            .andExpect(jsonPath("$.down").value(DEFAULT_DOWN.doubleValue()))
            .andExpect(jsonPath("$.fee").value(DEFAULT_FEE.doubleValue()))
            .andExpect(jsonPath("$.description").value(DEFAULT_DESCRIPTION));
    }

//    @Test
    @Transactional
    void getP2pCommissionsByIdFiltering() throws Exception {
        // Initialize the database
        p2pCommissionRepository.saveAndFlush(p2pCommission);

        Long id = p2pCommission.getId();

        defaultP2pCommissionShouldBeFound("id.equals=" + id);
        defaultP2pCommissionShouldNotBeFound("id.notEquals=" + id);

        defaultP2pCommissionShouldBeFound("id.greaterThanOrEqual=" + id);
        defaultP2pCommissionShouldNotBeFound("id.greaterThan=" + id);

        defaultP2pCommissionShouldBeFound("id.lessThanOrEqual=" + id);
        defaultP2pCommissionShouldNotBeFound("id.lessThan=" + id);
    }

    @Test
    @Transactional
    void getAllP2pCommissionsByNameIsEqualToSomething() throws Exception {
        // Initialize the database
        p2pCommissionRepository.saveAndFlush(p2pCommission);

        // Get all the p2pCommissionList where name equals to DEFAULT_NAME
        defaultP2pCommissionShouldBeFound("name.equals=" + DEFAULT_NAME);

        // Get all the p2pCommissionList where name equals to UPDATED_NAME
        defaultP2pCommissionShouldNotBeFound("name.equals=" + UPDATED_NAME);
    }

//    @Test
    @Transactional
    void getAllP2pCommissionsByNameIsNotEqualToSomething() throws Exception {
        // Initialize the database
        p2pCommissionRepository.saveAndFlush(p2pCommission);

        // Get all the p2pCommissionList where name not equals to DEFAULT_NAME
        defaultP2pCommissionShouldNotBeFound("name.notEquals=" + DEFAULT_NAME);

        // Get all the p2pCommissionList where name not equals to UPDATED_NAME
        defaultP2pCommissionShouldBeFound("name.notEquals=" + UPDATED_NAME);
    }

    @Test
    @Transactional
    void getAllP2pCommissionsByNameIsInShouldWork() throws Exception {
        // Initialize the database
        p2pCommissionRepository.saveAndFlush(p2pCommission);

        // Get all the p2pCommissionList where name in DEFAULT_NAME or UPDATED_NAME
        defaultP2pCommissionShouldBeFound("name.in=" + DEFAULT_NAME + "," + UPDATED_NAME);

        // Get all the p2pCommissionList where name equals to UPDATED_NAME
        defaultP2pCommissionShouldNotBeFound("name.in=" + UPDATED_NAME);
    }

//    @Test
    @Transactional
    void getAllP2pCommissionsByNameIsNullOrNotNull() throws Exception {
        // Initialize the database
        p2pCommissionRepository.saveAndFlush(p2pCommission);

        // Get all the p2pCommissionList where name is not null
        defaultP2pCommissionShouldBeFound("name.specified=true");

        // Get all the p2pCommissionList where name is null
        defaultP2pCommissionShouldNotBeFound("name.specified=false");
    }

    @Test
    @Transactional
    void getAllP2pCommissionsByNameContainsSomething() throws Exception {
        // Initialize the database
        p2pCommissionRepository.saveAndFlush(p2pCommission);

        // Get all the p2pCommissionList where name contains DEFAULT_NAME
        defaultP2pCommissionShouldBeFound("name.contains=" + DEFAULT_NAME);

        // Get all the p2pCommissionList where name contains UPDATED_NAME
        defaultP2pCommissionShouldNotBeFound("name.contains=" + UPDATED_NAME);
    }

//    @Test
    @Transactional
    void getAllP2pCommissionsByNameNotContainsSomething() throws Exception {
        // Initialize the database
        p2pCommissionRepository.saveAndFlush(p2pCommission);

        // Get all the p2pCommissionList where name does not contain DEFAULT_NAME
        defaultP2pCommissionShouldNotBeFound("name.doesNotContain=" + DEFAULT_NAME);

        // Get all the p2pCommissionList where name does not contain UPDATED_NAME
        defaultP2pCommissionShouldBeFound("name.doesNotContain=" + UPDATED_NAME);
    }

//    @Test
    @Transactional
    void getAllP2pCommissionsByUpIsEqualToSomething() throws Exception {
        // Initialize the database
        p2pCommissionRepository.saveAndFlush(p2pCommission);

        // Get all the p2pCommissionList where up equals to DEFAULT_UP
        defaultP2pCommissionShouldBeFound("up.equals=" + DEFAULT_UP);

        // Get all the p2pCommissionList where up equals to UPDATED_UP
        defaultP2pCommissionShouldNotBeFound("up.equals=" + UPDATED_UP);
    }

//    @Test
    @Transactional
    void getAllP2pCommissionsByUpIsNotEqualToSomething() throws Exception {
        // Initialize the database
        p2pCommissionRepository.saveAndFlush(p2pCommission);

        // Get all the p2pCommissionList where up not equals to DEFAULT_UP
        defaultP2pCommissionShouldNotBeFound("up.notEquals=" + DEFAULT_UP);

        // Get all the p2pCommissionList where up not equals to UPDATED_UP
        defaultP2pCommissionShouldBeFound("up.notEquals=" + UPDATED_UP);
    }

//    @Test
    @Transactional
    void getAllP2pCommissionsByUpIsInShouldWork() throws Exception {
        // Initialize the database
        p2pCommissionRepository.saveAndFlush(p2pCommission);

        // Get all the p2pCommissionList where up in DEFAULT_UP or UPDATED_UP
        defaultP2pCommissionShouldBeFound("up.in=" + DEFAULT_UP + "," + UPDATED_UP);

        // Get all the p2pCommissionList where up equals to UPDATED_UP
        defaultP2pCommissionShouldNotBeFound("up.in=" + UPDATED_UP);
    }

//    @Test
    @Transactional
    void getAllP2pCommissionsByUpIsNullOrNotNull() throws Exception {
        // Initialize the database
        p2pCommissionRepository.saveAndFlush(p2pCommission);

        // Get all the p2pCommissionList where up is not null
        defaultP2pCommissionShouldBeFound("up.specified=true");

        // Get all the p2pCommissionList where up is null
        defaultP2pCommissionShouldNotBeFound("up.specified=false");
    }

//    @Test
    @Transactional
    void getAllP2pCommissionsByUpIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        p2pCommissionRepository.saveAndFlush(p2pCommission);

        // Get all the p2pCommissionList where up is greater than or equal to DEFAULT_UP
        defaultP2pCommissionShouldBeFound("up.greaterThanOrEqual=" + DEFAULT_UP);

        // Get all the p2pCommissionList where up is greater than or equal to (DEFAULT_UP + 1)
        defaultP2pCommissionShouldNotBeFound("up.greaterThanOrEqual=" + (DEFAULT_UP + 1));
    }

//    @Test
    @Transactional
    void getAllP2pCommissionsByUpIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        p2pCommissionRepository.saveAndFlush(p2pCommission);

        // Get all the p2pCommissionList where up is less than or equal to DEFAULT_UP
        defaultP2pCommissionShouldBeFound("up.lessThanOrEqual=" + DEFAULT_UP);

        // Get all the p2pCommissionList where up is less than or equal to SMALLER_UP
        defaultP2pCommissionShouldNotBeFound("up.lessThanOrEqual=" + SMALLER_UP);
    }

//    @Test
    @Transactional
    void getAllP2pCommissionsByUpIsLessThanSomething() throws Exception {
        // Initialize the database
        p2pCommissionRepository.saveAndFlush(p2pCommission);

        // Get all the p2pCommissionList where up is less than DEFAULT_UP
        defaultP2pCommissionShouldNotBeFound("up.lessThan=" + DEFAULT_UP);

        // Get all the p2pCommissionList where up is less than (DEFAULT_UP + 1)
        defaultP2pCommissionShouldBeFound("up.lessThan=" + (DEFAULT_UP + 1));
    }

//    @Test
    @Transactional
    void getAllP2pCommissionsByUpIsGreaterThanSomething() throws Exception {
        // Initialize the database
        p2pCommissionRepository.saveAndFlush(p2pCommission);

        // Get all the p2pCommissionList where up is greater than DEFAULT_UP
        defaultP2pCommissionShouldNotBeFound("up.greaterThan=" + DEFAULT_UP);

        // Get all the p2pCommissionList where up is greater than SMALLER_UP
        defaultP2pCommissionShouldBeFound("up.greaterThan=" + SMALLER_UP);
    }

//    @Test
    @Transactional
    void getAllP2pCommissionsByDownIsEqualToSomething() throws Exception {
        // Initialize the database
        p2pCommissionRepository.saveAndFlush(p2pCommission);

        // Get all the p2pCommissionList where down equals to DEFAULT_DOWN
        defaultP2pCommissionShouldBeFound("down.equals=" + DEFAULT_DOWN);

        // Get all the p2pCommissionList where down equals to UPDATED_DOWN
        defaultP2pCommissionShouldNotBeFound("down.equals=" + UPDATED_DOWN);
    }

//    @Test
    @Transactional
    void getAllP2pCommissionsByDownIsNotEqualToSomething() throws Exception {
        // Initialize the database
        p2pCommissionRepository.saveAndFlush(p2pCommission);

        // Get all the p2pCommissionList where down not equals to DEFAULT_DOWN
        defaultP2pCommissionShouldNotBeFound("down.notEquals=" + DEFAULT_DOWN);

        // Get all the p2pCommissionList where down not equals to UPDATED_DOWN
        defaultP2pCommissionShouldBeFound("down.notEquals=" + UPDATED_DOWN);
    }

//    @Test
    @Transactional
    void getAllP2pCommissionsByDownIsInShouldWork() throws Exception {
        // Initialize the database
        p2pCommissionRepository.saveAndFlush(p2pCommission);

        // Get all the p2pCommissionList where down in DEFAULT_DOWN or UPDATED_DOWN
        defaultP2pCommissionShouldBeFound("down.in=" + DEFAULT_DOWN + "," + UPDATED_DOWN);

        // Get all the p2pCommissionList where down equals to UPDATED_DOWN
        defaultP2pCommissionShouldNotBeFound("down.in=" + UPDATED_DOWN);
    }

//    @Test
    @Transactional
    void getAllP2pCommissionsByDownIsNullOrNotNull() throws Exception {
        // Initialize the database
        p2pCommissionRepository.saveAndFlush(p2pCommission);

        // Get all the p2pCommissionList where down is not null
        defaultP2pCommissionShouldBeFound("down.specified=true");

        // Get all the p2pCommissionList where down is null
        defaultP2pCommissionShouldNotBeFound("down.specified=false");
    }

//    @Test
    @Transactional
    void getAllP2pCommissionsByDownIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        p2pCommissionRepository.saveAndFlush(p2pCommission);

        // Get all the p2pCommissionList where down is greater than or equal to DEFAULT_DOWN
        defaultP2pCommissionShouldBeFound("down.greaterThanOrEqual=" + DEFAULT_DOWN);

        // Get all the p2pCommissionList where down is greater than or equal to (DEFAULT_DOWN + 1)
        defaultP2pCommissionShouldNotBeFound("down.greaterThanOrEqual=" + (DEFAULT_DOWN + 1));
    }

//    @Test
    @Transactional
    void getAllP2pCommissionsByDownIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        p2pCommissionRepository.saveAndFlush(p2pCommission);

        // Get all the p2pCommissionList where down is less than or equal to DEFAULT_DOWN
        defaultP2pCommissionShouldBeFound("down.lessThanOrEqual=" + DEFAULT_DOWN);

        // Get all the p2pCommissionList where down is less than or equal to SMALLER_DOWN
        defaultP2pCommissionShouldNotBeFound("down.lessThanOrEqual=" + SMALLER_DOWN);
    }

//    @Test
    @Transactional
    void getAllP2pCommissionsByDownIsLessThanSomething() throws Exception {
        // Initialize the database
        p2pCommissionRepository.saveAndFlush(p2pCommission);

        // Get all the p2pCommissionList where down is less than DEFAULT_DOWN
        defaultP2pCommissionShouldNotBeFound("down.lessThan=" + DEFAULT_DOWN);

        // Get all the p2pCommissionList where down is less than (DEFAULT_DOWN + 1)
        defaultP2pCommissionShouldBeFound("down.lessThan=" + (DEFAULT_DOWN + 1));
    }

//    @Test
    @Transactional
    void getAllP2pCommissionsByDownIsGreaterThanSomething() throws Exception {
        // Initialize the database
        p2pCommissionRepository.saveAndFlush(p2pCommission);

        // Get all the p2pCommissionList where down is greater than DEFAULT_DOWN
        defaultP2pCommissionShouldNotBeFound("down.greaterThan=" + DEFAULT_DOWN);

        // Get all the p2pCommissionList where down is greater than SMALLER_DOWN
        defaultP2pCommissionShouldBeFound("down.greaterThan=" + SMALLER_DOWN);
    }

//    @Test
    @Transactional
    void getAllP2pCommissionsByFeeIsEqualToSomething() throws Exception {
        // Initialize the database
        p2pCommissionRepository.saveAndFlush(p2pCommission);

        // Get all the p2pCommissionList where fee equals to DEFAULT_FEE
        defaultP2pCommissionShouldBeFound("fee.equals=" + DEFAULT_FEE);

        // Get all the p2pCommissionList where fee equals to UPDATED_FEE
        defaultP2pCommissionShouldNotBeFound("fee.equals=" + UPDATED_FEE);
    }

//    @Test
    @Transactional
    void getAllP2pCommissionsByFeeIsNotEqualToSomething() throws Exception {
        // Initialize the database
        p2pCommissionRepository.saveAndFlush(p2pCommission);

        // Get all the p2pCommissionList where fee not equals to DEFAULT_FEE
        defaultP2pCommissionShouldNotBeFound("fee.notEquals=" + DEFAULT_FEE);

        // Get all the p2pCommissionList where fee not equals to UPDATED_FEE
        defaultP2pCommissionShouldBeFound("fee.notEquals=" + UPDATED_FEE);
    }

//    @Test
    @Transactional
    void getAllP2pCommissionsByFeeIsInShouldWork() throws Exception {
        // Initialize the database
        p2pCommissionRepository.saveAndFlush(p2pCommission);

        // Get all the p2pCommissionList where fee in DEFAULT_FEE or UPDATED_FEE
        defaultP2pCommissionShouldBeFound("fee.in=" + DEFAULT_FEE + "," + UPDATED_FEE);

        // Get all the p2pCommissionList where fee equals to UPDATED_FEE
        defaultP2pCommissionShouldNotBeFound("fee.in=" + UPDATED_FEE);
    }

//    @Test
    @Transactional
    void getAllP2pCommissionsByFeeIsNullOrNotNull() throws Exception {
        // Initialize the database
        p2pCommissionRepository.saveAndFlush(p2pCommission);

        // Get all the p2pCommissionList where fee is not null
        defaultP2pCommissionShouldBeFound("fee.specified=true");

        // Get all the p2pCommissionList where fee is null
        defaultP2pCommissionShouldNotBeFound("fee.specified=false");
    }

//    @Test
    @Transactional
    void getAllP2pCommissionsByFeeIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        p2pCommissionRepository.saveAndFlush(p2pCommission);

        // Get all the p2pCommissionList where fee is greater than or equal to DEFAULT_FEE
        defaultP2pCommissionShouldBeFound("fee.greaterThanOrEqual=" + DEFAULT_FEE);

        // Get all the p2pCommissionList where fee is greater than or equal to (DEFAULT_FEE + 1)
        defaultP2pCommissionShouldNotBeFound("fee.greaterThanOrEqual=" + (DEFAULT_FEE + 1));
    }

//    @Test
    @Transactional
    void getAllP2pCommissionsByFeeIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        p2pCommissionRepository.saveAndFlush(p2pCommission);

        // Get all the p2pCommissionList where fee is less than or equal to DEFAULT_FEE
        defaultP2pCommissionShouldBeFound("fee.lessThanOrEqual=" + DEFAULT_FEE);

        // Get all the p2pCommissionList where fee is less than or equal to SMALLER_FEE
        defaultP2pCommissionShouldNotBeFound("fee.lessThanOrEqual=" + SMALLER_FEE);
    }

//    @Test
    @Transactional
    void getAllP2pCommissionsByFeeIsLessThanSomething() throws Exception {
        // Initialize the database
        p2pCommissionRepository.saveAndFlush(p2pCommission);

        // Get all the p2pCommissionList where fee is less than DEFAULT_FEE
        defaultP2pCommissionShouldNotBeFound("fee.lessThan=" + DEFAULT_FEE);

        // Get all the p2pCommissionList where fee is less than (DEFAULT_FEE + 1)
        defaultP2pCommissionShouldBeFound("fee.lessThan=" + (DEFAULT_FEE + 1));
    }

//    @Test
    @Transactional
    void getAllP2pCommissionsByFeeIsGreaterThanSomething() throws Exception {
        // Initialize the database
        p2pCommissionRepository.saveAndFlush(p2pCommission);

        // Get all the p2pCommissionList where fee is greater than DEFAULT_FEE
        defaultP2pCommissionShouldNotBeFound("fee.greaterThan=" + DEFAULT_FEE);

        // Get all the p2pCommissionList where fee is greater than SMALLER_FEE
        defaultP2pCommissionShouldBeFound("fee.greaterThan=" + SMALLER_FEE);
    }

    @Test
    @Transactional
    void getAllP2pCommissionsByDescriptionIsEqualToSomething() throws Exception {
        // Initialize the database
        p2pCommissionRepository.saveAndFlush(p2pCommission);

        // Get all the p2pCommissionList where description equals to DEFAULT_DESCRIPTION
        defaultP2pCommissionShouldBeFound("description.equals=" + DEFAULT_DESCRIPTION);

        // Get all the p2pCommissionList where description equals to UPDATED_DESCRIPTION
        defaultP2pCommissionShouldNotBeFound("description.equals=" + UPDATED_DESCRIPTION);
    }

//    @Test
    @Transactional
    void getAllP2pCommissionsByDescriptionIsNotEqualToSomething() throws Exception {
        // Initialize the database
        p2pCommissionRepository.saveAndFlush(p2pCommission);

        // Get all the p2pCommissionList where description not equals to DEFAULT_DESCRIPTION
        defaultP2pCommissionShouldNotBeFound("description.notEquals=" + DEFAULT_DESCRIPTION);

        // Get all the p2pCommissionList where description not equals to UPDATED_DESCRIPTION
        defaultP2pCommissionShouldBeFound("description.notEquals=" + UPDATED_DESCRIPTION);
    }

    @Test
    @Transactional
    void getAllP2pCommissionsByDescriptionIsInShouldWork() throws Exception {
        // Initialize the database
        p2pCommissionRepository.saveAndFlush(p2pCommission);

        // Get all the p2pCommissionList where description in DEFAULT_DESCRIPTION or UPDATED_DESCRIPTION
        defaultP2pCommissionShouldBeFound("description.in=" + DEFAULT_DESCRIPTION + "," + UPDATED_DESCRIPTION);

        // Get all the p2pCommissionList where description equals to UPDATED_DESCRIPTION
        defaultP2pCommissionShouldNotBeFound("description.in=" + UPDATED_DESCRIPTION);
    }

//    @Test
    @Transactional
    void getAllP2pCommissionsByDescriptionIsNullOrNotNull() throws Exception {
        // Initialize the database
        p2pCommissionRepository.saveAndFlush(p2pCommission);

        // Get all the p2pCommissionList where description is not null
        defaultP2pCommissionShouldBeFound("description.specified=true");

        // Get all the p2pCommissionList where description is null
        defaultP2pCommissionShouldNotBeFound("description.specified=false");
    }

    @Test
    @Transactional
    void getAllP2pCommissionsByDescriptionContainsSomething() throws Exception {
        // Initialize the database
        p2pCommissionRepository.saveAndFlush(p2pCommission);

        // Get all the p2pCommissionList where description contains DEFAULT_DESCRIPTION
        defaultP2pCommissionShouldBeFound("description.contains=" + DEFAULT_DESCRIPTION);

        // Get all the p2pCommissionList where description contains UPDATED_DESCRIPTION
        defaultP2pCommissionShouldNotBeFound("description.contains=" + UPDATED_DESCRIPTION);
    }

//    @Test
    @Transactional
    void getAllP2pCommissionsByDescriptionNotContainsSomething() throws Exception {
        // Initialize the database
        p2pCommissionRepository.saveAndFlush(p2pCommission);

        // Get all the p2pCommissionList where description does not contain DEFAULT_DESCRIPTION
        defaultP2pCommissionShouldNotBeFound("description.doesNotContain=" + DEFAULT_DESCRIPTION);

        // Get all the p2pCommissionList where description does not contain UPDATED_DESCRIPTION
        defaultP2pCommissionShouldBeFound("description.doesNotContain=" + UPDATED_DESCRIPTION);
    }

    /**
     * Executes the search, and checks that the default entity is returned.
     */
    private void defaultP2pCommissionShouldBeFound(String filter) throws Exception {
        restP2pCommissionMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(p2pCommission.getId().intValue())))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME)))
            .andExpect(jsonPath("$.[*].up").value(hasItem(DEFAULT_UP.doubleValue())))
            .andExpect(jsonPath("$.[*].down").value(hasItem(DEFAULT_DOWN.doubleValue())))
            .andExpect(jsonPath("$.[*].fee").value(hasItem(DEFAULT_FEE.doubleValue())))
            .andExpect(jsonPath("$.[*].description").value(hasItem(DEFAULT_DESCRIPTION)));

        // Check, that the count call also returns 1
        restP2pCommissionMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("1"));
    }

    /**
     * Executes the search, and checks that the default entity is not returned.
     */
    private void defaultP2pCommissionShouldNotBeFound(String filter) throws Exception {
        restP2pCommissionMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty());

        // Check, that the count call also returns 0
        restP2pCommissionMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("0"));
    }

    @Test
    @Transactional
    void getNonExistingP2pCommission() throws Exception {
        // Get the p2pCommission
        restP2pCommissionMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putNewP2pCommission() throws Exception {
        // Initialize the database
        p2pCommissionRepository.saveAndFlush(p2pCommission);

        int databaseSizeBeforeUpdate = p2pCommissionRepository.findAll().size();

        // Update the p2pCommission
        P2pCommission updatedP2pCommission = p2pCommissionRepository.findById(p2pCommission.getId()).get();
        // Disconnect from session so that the updates on updatedP2pCommission are not directly saved in db
        em.detach(updatedP2pCommission);
        updatedP2pCommission.name(UPDATED_NAME).up(UPDATED_UP).down(UPDATED_DOWN).fee(UPDATED_FEE).description(UPDATED_DESCRIPTION);

        restP2pCommissionMockMvc
            .perform(
                put(ENTITY_API_URL_ID, updatedP2pCommission.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(updatedP2pCommission))
            )
            .andExpect(status().isOk());

        // Validate the P2pCommission in the database
        List<P2pCommission> p2pCommissionList = p2pCommissionRepository.findAll();
        assertThat(p2pCommissionList).hasSize(databaseSizeBeforeUpdate);
        P2pCommission testP2pCommission = p2pCommissionList.get(p2pCommissionList.size() - 1);
        assertThat(testP2pCommission.getName()).isEqualTo(UPDATED_NAME);
        assertThat(testP2pCommission.getUp()).isEqualTo(UPDATED_UP);
        assertThat(testP2pCommission.getDown()).isEqualTo(UPDATED_DOWN);
        assertThat(testP2pCommission.getFee()).isEqualTo(UPDATED_FEE);
        assertThat(testP2pCommission.getDescription()).isEqualTo(UPDATED_DESCRIPTION);
    }

    @Test
    @Transactional
    void putNonExistingP2pCommission() throws Exception {
        int databaseSizeBeforeUpdate = p2pCommissionRepository.findAll().size();
        p2pCommission.setId(count.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restP2pCommissionMockMvc
            .perform(
                put(ENTITY_API_URL_ID, p2pCommission.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(p2pCommission))
            )
            .andExpect(status().isBadRequest());

        // Validate the P2pCommission in the database
        List<P2pCommission> p2pCommissionList = p2pCommissionRepository.findAll();
        assertThat(p2pCommissionList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchP2pCommission() throws Exception {
        int databaseSizeBeforeUpdate = p2pCommissionRepository.findAll().size();
        p2pCommission.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restP2pCommissionMockMvc
            .perform(
                put(ENTITY_API_URL_ID, count.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(p2pCommission))
            )
            .andExpect(status().isBadRequest());

        // Validate the P2pCommission in the database
        List<P2pCommission> p2pCommissionList = p2pCommissionRepository.findAll();
        assertThat(p2pCommissionList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamP2pCommission() throws Exception {
        int databaseSizeBeforeUpdate = p2pCommissionRepository.findAll().size();
        p2pCommission.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restP2pCommissionMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(p2pCommission)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the P2pCommission in the database
        List<P2pCommission> p2pCommissionList = p2pCommissionRepository.findAll();
        assertThat(p2pCommissionList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateP2pCommissionWithPatch() throws Exception {
        // Initialize the database
        p2pCommissionRepository.saveAndFlush(p2pCommission);

        int databaseSizeBeforeUpdate = p2pCommissionRepository.findAll().size();

        // Update the p2pCommission using partial update
        P2pCommission partialUpdatedP2pCommission = new P2pCommission();
        partialUpdatedP2pCommission.setId(p2pCommission.getId());

        partialUpdatedP2pCommission.up(UPDATED_UP).down(UPDATED_DOWN).fee(UPDATED_FEE);

        restP2pCommissionMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedP2pCommission.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedP2pCommission))
            )
            .andExpect(status().isOk());

        // Validate the P2pCommission in the database
        List<P2pCommission> p2pCommissionList = p2pCommissionRepository.findAll();
        assertThat(p2pCommissionList).hasSize(databaseSizeBeforeUpdate);
        P2pCommission testP2pCommission = p2pCommissionList.get(p2pCommissionList.size() - 1);
        assertThat(testP2pCommission.getName()).isEqualTo(DEFAULT_NAME);
        assertThat(testP2pCommission.getUp()).isEqualTo(UPDATED_UP);
        assertThat(testP2pCommission.getDown()).isEqualTo(UPDATED_DOWN);
        assertThat(testP2pCommission.getFee()).isEqualTo(UPDATED_FEE);
        assertThat(testP2pCommission.getDescription()).isEqualTo(DEFAULT_DESCRIPTION);
    }

    @Test
    @Transactional
    void fullUpdateP2pCommissionWithPatch() throws Exception {
        // Initialize the database
        p2pCommissionRepository.saveAndFlush(p2pCommission);

        int databaseSizeBeforeUpdate = p2pCommissionRepository.findAll().size();

        // Update the p2pCommission using partial update
        P2pCommission partialUpdatedP2pCommission = new P2pCommission();
        partialUpdatedP2pCommission.setId(p2pCommission.getId());

        partialUpdatedP2pCommission.name(UPDATED_NAME).up(UPDATED_UP).down(UPDATED_DOWN).fee(UPDATED_FEE).description(UPDATED_DESCRIPTION);

        restP2pCommissionMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedP2pCommission.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedP2pCommission))
            )
            .andExpect(status().isOk());

        // Validate the P2pCommission in the database
        List<P2pCommission> p2pCommissionList = p2pCommissionRepository.findAll();
        assertThat(p2pCommissionList).hasSize(databaseSizeBeforeUpdate);
        P2pCommission testP2pCommission = p2pCommissionList.get(p2pCommissionList.size() - 1);
        assertThat(testP2pCommission.getName()).isEqualTo(UPDATED_NAME);
        assertThat(testP2pCommission.getUp()).isEqualTo(UPDATED_UP);
        assertThat(testP2pCommission.getDown()).isEqualTo(UPDATED_DOWN);
        assertThat(testP2pCommission.getFee()).isEqualTo(UPDATED_FEE);
        assertThat(testP2pCommission.getDescription()).isEqualTo(UPDATED_DESCRIPTION);
    }

    @Test
    @Transactional
    void patchNonExistingP2pCommission() throws Exception {
        int databaseSizeBeforeUpdate = p2pCommissionRepository.findAll().size();
        p2pCommission.setId(count.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restP2pCommissionMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, p2pCommission.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(p2pCommission))
            )
            .andExpect(status().isBadRequest());

        // Validate the P2pCommission in the database
        List<P2pCommission> p2pCommissionList = p2pCommissionRepository.findAll();
        assertThat(p2pCommissionList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchP2pCommission() throws Exception {
        int databaseSizeBeforeUpdate = p2pCommissionRepository.findAll().size();
        p2pCommission.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restP2pCommissionMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, count.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(p2pCommission))
            )
            .andExpect(status().isBadRequest());

        // Validate the P2pCommission in the database
        List<P2pCommission> p2pCommissionList = p2pCommissionRepository.findAll();
        assertThat(p2pCommissionList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamP2pCommission() throws Exception {
        int databaseSizeBeforeUpdate = p2pCommissionRepository.findAll().size();
        p2pCommission.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restP2pCommissionMockMvc
            .perform(
                patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(TestUtil.convertObjectToJsonBytes(p2pCommission))
            )
            .andExpect(status().isMethodNotAllowed());

        // Validate the P2pCommission in the database
        List<P2pCommission> p2pCommissionList = p2pCommissionRepository.findAll();
        assertThat(p2pCommissionList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteP2pCommission() throws Exception {
        // Initialize the database
        p2pCommissionRepository.saveAndFlush(p2pCommission);

        int databaseSizeBeforeDelete = p2pCommissionRepository.findAll().size();

        // Delete the p2pCommission
        restP2pCommissionMockMvc
            .perform(delete(ENTITY_API_URL_ID, p2pCommission.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        List<P2pCommission> p2pCommissionList = p2pCommissionRepository.findAll();
        assertThat(p2pCommissionList).hasSize(databaseSizeBeforeDelete - 1);
    }
}
