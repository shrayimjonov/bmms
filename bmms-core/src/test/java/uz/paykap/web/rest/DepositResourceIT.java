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
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import uz.paykap.IntegrationTest;
import uz.paykap.domain.Agent;
import uz.paykap.domain.Deposit;
import uz.paykap.domain.Gateway;
import uz.paykap.domain.VirtualAccount;
import uz.paykap.repository.DepositRepository;
import uz.paykap.service.criteria.DepositCriteria;
import uz.paykap.service.dto.DepositDTO;
import uz.paykap.service.mapper.DepositMapper;

/**
 * Integration tests for the {@link DepositResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class DepositResourceIT {

    private static final Long DEFAULT_OVERDRAFT_LIMIT = 1L;
    private static final Long UPDATED_OVERDRAFT_LIMIT = 2L;
    private static final Long SMALLER_OVERDRAFT_LIMIT = 1L - 1L;

    private static final Long DEFAULT_BALANCE = 1L;
    private static final Long UPDATED_BALANCE = 2L;
    private static final Long SMALLER_BALANCE = 1L - 1L;

    private static final Integer DEFAULT_BALANCE_CCY = 1;
    private static final Integer UPDATED_BALANCE_CCY = 2;
    private static final Integer SMALLER_BALANCE_CCY = 1 - 1;

    private static final Long DEFAULT_BOOKED_BALANCE = 1L;
    private static final Long UPDATED_BOOKED_BALANCE = 2L;
    private static final Long SMALLER_BOOKED_BALANCE = 1L - 1L;

    private static final Long DEFAULT_LIMIT_INFORM = 1L;
    private static final Long UPDATED_LIMIT_INFORM = 2L;
    private static final Long SMALLER_LIMIT_INFORM = 1L - 1L;

    private static final Long DEFAULT_LIMIT_DISABLE = 1L;
    private static final Long UPDATED_LIMIT_DISABLE = 2L;
    private static final Long SMALLER_LIMIT_DISABLE = 1L - 1L;

    private static final String DEFAULT_CONTACT_GROUP = "AAAAAAAAAA";
    private static final String UPDATED_CONTACT_GROUP = "BBBBBBBBBB";

    private static final Boolean DEFAULT_BLOCKED = false;
    private static final Boolean UPDATED_BLOCKED = true;

    private static final Boolean DEFAULT_UNLIMIT = false;
    private static final Boolean UPDATED_UNLIMIT = true;

    private static final String ENTITY_API_URL = "/api/deposits";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong count = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private DepositRepository depositRepository;

    @Autowired
    private DepositMapper depositMapper;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restDepositMockMvc;

    private Deposit deposit;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Deposit createEntity(EntityManager em) {
        Deposit deposit = new Deposit()
            .overdraftLimit(DEFAULT_OVERDRAFT_LIMIT)
            .balance(DEFAULT_BALANCE)
            .balanceCcy(DEFAULT_BALANCE_CCY)
            .bookedBalance(DEFAULT_BOOKED_BALANCE)
            .limitInform(DEFAULT_LIMIT_INFORM)
            .limitDisable(DEFAULT_LIMIT_DISABLE)
            .contactGroup(DEFAULT_CONTACT_GROUP)
            .blocked(DEFAULT_BLOCKED)
            .unlimit(DEFAULT_UNLIMIT);
        return deposit;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Deposit createUpdatedEntity(EntityManager em) {
        Deposit deposit = new Deposit()
            .overdraftLimit(UPDATED_OVERDRAFT_LIMIT)
            .balance(UPDATED_BALANCE)
            .balanceCcy(UPDATED_BALANCE_CCY)
            .bookedBalance(UPDATED_BOOKED_BALANCE)
            .limitInform(UPDATED_LIMIT_INFORM)
            .limitDisable(UPDATED_LIMIT_DISABLE)
            .contactGroup(UPDATED_CONTACT_GROUP)
            .blocked(UPDATED_BLOCKED)
            .unlimit(UPDATED_UNLIMIT);
        return deposit;
    }

    @BeforeEach
    public void initTest() {
        deposit = createEntity(em);
    }

    @Test
    @Transactional
    void createDeposit() throws Exception {
        int databaseSizeBeforeCreate = depositRepository.findAll().size();
        // Create the Deposit
        DepositDTO depositDTO = depositMapper.toDto(deposit);
        restDepositMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(depositDTO)))
            .andExpect(status().isCreated());

        // Validate the Deposit in the database
        List<Deposit> depositList = depositRepository.findAll();
        assertThat(depositList).hasSize(databaseSizeBeforeCreate + 1);
        Deposit testDeposit = depositList.get(depositList.size() - 1);
        assertThat(testDeposit.getOverdraftLimit()).isEqualTo(DEFAULT_OVERDRAFT_LIMIT);
        assertThat(testDeposit.getBalance()).isEqualTo(DEFAULT_BALANCE);
        assertThat(testDeposit.getBalanceCcy()).isEqualTo(DEFAULT_BALANCE_CCY);
        assertThat(testDeposit.getBookedBalance()).isEqualTo(DEFAULT_BOOKED_BALANCE);
        assertThat(testDeposit.getLimitInform()).isEqualTo(DEFAULT_LIMIT_INFORM);
        assertThat(testDeposit.getLimitDisable()).isEqualTo(DEFAULT_LIMIT_DISABLE);
        assertThat(testDeposit.getContactGroup()).isEqualTo(DEFAULT_CONTACT_GROUP);
        assertThat(testDeposit.getBlocked()).isEqualTo(DEFAULT_BLOCKED);
        assertThat(testDeposit.getUnlimit()).isEqualTo(DEFAULT_UNLIMIT);
    }

    @Test
    @Transactional
    void createDepositWithExistingId() throws Exception {
        // Create the Deposit with an existing ID
        deposit.setId(1L);
        DepositDTO depositDTO = depositMapper.toDto(deposit);

        int databaseSizeBeforeCreate = depositRepository.findAll().size();

        // An entity with an existing ID cannot be created, so this API call must fail
        restDepositMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(depositDTO)))
            .andExpect(status().isBadRequest());

        // Validate the Deposit in the database
        List<Deposit> depositList = depositRepository.findAll();
        assertThat(depositList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkOverdraftLimitIsRequired() throws Exception {
        int databaseSizeBeforeTest = depositRepository.findAll().size();
        // set the field null
        deposit.setOverdraftLimit(null);

        // Create the Deposit, which fails.
        DepositDTO depositDTO = depositMapper.toDto(deposit);

        restDepositMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(depositDTO)))
            .andExpect(status().isBadRequest());

        List<Deposit> depositList = depositRepository.findAll();
        assertThat(depositList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkBalanceIsRequired() throws Exception {
        int databaseSizeBeforeTest = depositRepository.findAll().size();
        // set the field null
        deposit.setBalance(null);

        // Create the Deposit, which fails.
        DepositDTO depositDTO = depositMapper.toDto(deposit);

        restDepositMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(depositDTO)))
            .andExpect(status().isBadRequest());

        List<Deposit> depositList = depositRepository.findAll();
        assertThat(depositList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkBalanceCcyIsRequired() throws Exception {
        int databaseSizeBeforeTest = depositRepository.findAll().size();
        // set the field null
        deposit.setBalanceCcy(null);

        // Create the Deposit, which fails.
        DepositDTO depositDTO = depositMapper.toDto(deposit);

        restDepositMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(depositDTO)))
            .andExpect(status().isBadRequest());

        List<Deposit> depositList = depositRepository.findAll();
        assertThat(depositList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkBookedBalanceIsRequired() throws Exception {
        int databaseSizeBeforeTest = depositRepository.findAll().size();
        // set the field null
        deposit.setBookedBalance(null);

        // Create the Deposit, which fails.
        DepositDTO depositDTO = depositMapper.toDto(deposit);

        restDepositMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(depositDTO)))
            .andExpect(status().isBadRequest());

        List<Deposit> depositList = depositRepository.findAll();
        assertThat(depositList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkLimitInformIsRequired() throws Exception {
        int databaseSizeBeforeTest = depositRepository.findAll().size();
        // set the field null
        deposit.setLimitInform(null);

        // Create the Deposit, which fails.
        DepositDTO depositDTO = depositMapper.toDto(deposit);

        restDepositMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(depositDTO)))
            .andExpect(status().isBadRequest());

        List<Deposit> depositList = depositRepository.findAll();
        assertThat(depositList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkLimitDisableIsRequired() throws Exception {
        int databaseSizeBeforeTest = depositRepository.findAll().size();
        // set the field null
        deposit.setLimitDisable(null);

        // Create the Deposit, which fails.
        DepositDTO depositDTO = depositMapper.toDto(deposit);

        restDepositMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(depositDTO)))
            .andExpect(status().isBadRequest());

        List<Deposit> depositList = depositRepository.findAll();
        assertThat(depositList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkBlockedIsRequired() throws Exception {
        int databaseSizeBeforeTest = depositRepository.findAll().size();
        // set the field null
        deposit.setBlocked(null);

        // Create the Deposit, which fails.
        DepositDTO depositDTO = depositMapper.toDto(deposit);

        restDepositMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(depositDTO)))
            .andExpect(status().isBadRequest());

        List<Deposit> depositList = depositRepository.findAll();
        assertThat(depositList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkUnlimitIsRequired() throws Exception {
        int databaseSizeBeforeTest = depositRepository.findAll().size();
        // set the field null
        deposit.setUnlimit(null);

        // Create the Deposit, which fails.
        DepositDTO depositDTO = depositMapper.toDto(deposit);

        restDepositMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(depositDTO)))
            .andExpect(status().isBadRequest());

        List<Deposit> depositList = depositRepository.findAll();
        assertThat(depositList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllDeposits() throws Exception {
        // Initialize the database
        depositRepository.saveAndFlush(deposit);

        // Get all the depositList
        restDepositMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(deposit.getId().intValue())))
            .andExpect(jsonPath("$.[*].overdraftLimit").value(hasItem(DEFAULT_OVERDRAFT_LIMIT.intValue())))
            .andExpect(jsonPath("$.[*].balance").value(hasItem(DEFAULT_BALANCE.intValue())))
            .andExpect(jsonPath("$.[*].balanceCcy").value(hasItem(DEFAULT_BALANCE_CCY)))
            .andExpect(jsonPath("$.[*].bookedBalance").value(hasItem(DEFAULT_BOOKED_BALANCE.intValue())))
            .andExpect(jsonPath("$.[*].limitInform").value(hasItem(DEFAULT_LIMIT_INFORM.intValue())))
            .andExpect(jsonPath("$.[*].limitDisable").value(hasItem(DEFAULT_LIMIT_DISABLE.intValue())))
            .andExpect(jsonPath("$.[*].contactGroup").value(hasItem(DEFAULT_CONTACT_GROUP)))
            .andExpect(jsonPath("$.[*].blocked").value(hasItem(DEFAULT_BLOCKED.booleanValue())))
            .andExpect(jsonPath("$.[*].unlimit").value(hasItem(DEFAULT_UNLIMIT.booleanValue())));
    }

    @Test
    @Transactional
    void getDeposit() throws Exception {
        // Initialize the database
        depositRepository.saveAndFlush(deposit);

        // Get the deposit
        restDepositMockMvc
            .perform(get(ENTITY_API_URL_ID, deposit.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(deposit.getId().intValue()))
            .andExpect(jsonPath("$.overdraftLimit").value(DEFAULT_OVERDRAFT_LIMIT.intValue()))
            .andExpect(jsonPath("$.balance").value(DEFAULT_BALANCE.intValue()))
            .andExpect(jsonPath("$.balanceCcy").value(DEFAULT_BALANCE_CCY))
            .andExpect(jsonPath("$.bookedBalance").value(DEFAULT_BOOKED_BALANCE.intValue()))
            .andExpect(jsonPath("$.limitInform").value(DEFAULT_LIMIT_INFORM.intValue()))
            .andExpect(jsonPath("$.limitDisable").value(DEFAULT_LIMIT_DISABLE.intValue()))
            .andExpect(jsonPath("$.contactGroup").value(DEFAULT_CONTACT_GROUP))
            .andExpect(jsonPath("$.blocked").value(DEFAULT_BLOCKED.booleanValue()))
            .andExpect(jsonPath("$.unlimit").value(DEFAULT_UNLIMIT.booleanValue()));
    }

    @Test
    @Transactional
    void getDepositsByIdFiltering() throws Exception {
        // Initialize the database
        depositRepository.saveAndFlush(deposit);

        Long id = deposit.getId();

        defaultDepositShouldBeFound("id.equals=" + id);
        defaultDepositShouldNotBeFound("id.notEquals=" + id);

        defaultDepositShouldBeFound("id.greaterThanOrEqual=" + id);
        defaultDepositShouldNotBeFound("id.greaterThan=" + id);

        defaultDepositShouldBeFound("id.lessThanOrEqual=" + id);
        defaultDepositShouldNotBeFound("id.lessThan=" + id);
    }

    @Test
    @Transactional
    void getAllDepositsByOverdraftLimitIsEqualToSomething() throws Exception {
        // Initialize the database
        depositRepository.saveAndFlush(deposit);

        // Get all the depositList where overdraftLimit equals to DEFAULT_OVERDRAFT_LIMIT
        defaultDepositShouldBeFound("overdraftLimit.equals=" + DEFAULT_OVERDRAFT_LIMIT);

        // Get all the depositList where overdraftLimit equals to UPDATED_OVERDRAFT_LIMIT
        defaultDepositShouldNotBeFound("overdraftLimit.equals=" + UPDATED_OVERDRAFT_LIMIT);
    }

    @Test
    @Transactional
    void getAllDepositsByOverdraftLimitIsNotEqualToSomething() throws Exception {
        // Initialize the database
        depositRepository.saveAndFlush(deposit);

        // Get all the depositList where overdraftLimit not equals to DEFAULT_OVERDRAFT_LIMIT
        defaultDepositShouldNotBeFound("overdraftLimit.notEquals=" + DEFAULT_OVERDRAFT_LIMIT);

        // Get all the depositList where overdraftLimit not equals to UPDATED_OVERDRAFT_LIMIT
        defaultDepositShouldBeFound("overdraftLimit.notEquals=" + UPDATED_OVERDRAFT_LIMIT);
    }

    @Test
    @Transactional
    void getAllDepositsByOverdraftLimitIsInShouldWork() throws Exception {
        // Initialize the database
        depositRepository.saveAndFlush(deposit);

        // Get all the depositList where overdraftLimit in DEFAULT_OVERDRAFT_LIMIT or UPDATED_OVERDRAFT_LIMIT
        defaultDepositShouldBeFound("overdraftLimit.in=" + DEFAULT_OVERDRAFT_LIMIT + "," + UPDATED_OVERDRAFT_LIMIT);

        // Get all the depositList where overdraftLimit equals to UPDATED_OVERDRAFT_LIMIT
        defaultDepositShouldNotBeFound("overdraftLimit.in=" + UPDATED_OVERDRAFT_LIMIT);
    }

    @Test
    @Transactional
    void getAllDepositsByOverdraftLimitIsNullOrNotNull() throws Exception {
        // Initialize the database
        depositRepository.saveAndFlush(deposit);

        // Get all the depositList where overdraftLimit is not null
        defaultDepositShouldBeFound("overdraftLimit.specified=true");

        // Get all the depositList where overdraftLimit is null
        defaultDepositShouldNotBeFound("overdraftLimit.specified=false");
    }

    @Test
    @Transactional
    void getAllDepositsByOverdraftLimitIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        depositRepository.saveAndFlush(deposit);

        // Get all the depositList where overdraftLimit is greater than or equal to DEFAULT_OVERDRAFT_LIMIT
        defaultDepositShouldBeFound("overdraftLimit.greaterThanOrEqual=" + DEFAULT_OVERDRAFT_LIMIT);

        // Get all the depositList where overdraftLimit is greater than or equal to UPDATED_OVERDRAFT_LIMIT
        defaultDepositShouldNotBeFound("overdraftLimit.greaterThanOrEqual=" + UPDATED_OVERDRAFT_LIMIT);
    }

    @Test
    @Transactional
    void getAllDepositsByOverdraftLimitIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        depositRepository.saveAndFlush(deposit);

        // Get all the depositList where overdraftLimit is less than or equal to DEFAULT_OVERDRAFT_LIMIT
        defaultDepositShouldBeFound("overdraftLimit.lessThanOrEqual=" + DEFAULT_OVERDRAFT_LIMIT);

        // Get all the depositList where overdraftLimit is less than or equal to SMALLER_OVERDRAFT_LIMIT
        defaultDepositShouldNotBeFound("overdraftLimit.lessThanOrEqual=" + SMALLER_OVERDRAFT_LIMIT);
    }

    @Test
    @Transactional
    void getAllDepositsByOverdraftLimitIsLessThanSomething() throws Exception {
        // Initialize the database
        depositRepository.saveAndFlush(deposit);

        // Get all the depositList where overdraftLimit is less than DEFAULT_OVERDRAFT_LIMIT
        defaultDepositShouldNotBeFound("overdraftLimit.lessThan=" + DEFAULT_OVERDRAFT_LIMIT);

        // Get all the depositList where overdraftLimit is less than UPDATED_OVERDRAFT_LIMIT
        defaultDepositShouldBeFound("overdraftLimit.lessThan=" + UPDATED_OVERDRAFT_LIMIT);
    }

    @Test
    @Transactional
    void getAllDepositsByOverdraftLimitIsGreaterThanSomething() throws Exception {
        // Initialize the database
        depositRepository.saveAndFlush(deposit);

        // Get all the depositList where overdraftLimit is greater than DEFAULT_OVERDRAFT_LIMIT
        defaultDepositShouldNotBeFound("overdraftLimit.greaterThan=" + DEFAULT_OVERDRAFT_LIMIT);

        // Get all the depositList where overdraftLimit is greater than SMALLER_OVERDRAFT_LIMIT
        defaultDepositShouldBeFound("overdraftLimit.greaterThan=" + SMALLER_OVERDRAFT_LIMIT);
    }

    @Test
    @Transactional
    void getAllDepositsByBalanceIsEqualToSomething() throws Exception {
        // Initialize the database
        depositRepository.saveAndFlush(deposit);

        // Get all the depositList where balance equals to DEFAULT_BALANCE
        defaultDepositShouldBeFound("balance.equals=" + DEFAULT_BALANCE);

        // Get all the depositList where balance equals to UPDATED_BALANCE
        defaultDepositShouldNotBeFound("balance.equals=" + UPDATED_BALANCE);
    }

    @Test
    @Transactional
    void getAllDepositsByBalanceIsNotEqualToSomething() throws Exception {
        // Initialize the database
        depositRepository.saveAndFlush(deposit);

        // Get all the depositList where balance not equals to DEFAULT_BALANCE
        defaultDepositShouldNotBeFound("balance.notEquals=" + DEFAULT_BALANCE);

        // Get all the depositList where balance not equals to UPDATED_BALANCE
        defaultDepositShouldBeFound("balance.notEquals=" + UPDATED_BALANCE);
    }

    @Test
    @Transactional
    void getAllDepositsByBalanceIsInShouldWork() throws Exception {
        // Initialize the database
        depositRepository.saveAndFlush(deposit);

        // Get all the depositList where balance in DEFAULT_BALANCE or UPDATED_BALANCE
        defaultDepositShouldBeFound("balance.in=" + DEFAULT_BALANCE + "," + UPDATED_BALANCE);

        // Get all the depositList where balance equals to UPDATED_BALANCE
        defaultDepositShouldNotBeFound("balance.in=" + UPDATED_BALANCE);
    }

    @Test
    @Transactional
    void getAllDepositsByBalanceIsNullOrNotNull() throws Exception {
        // Initialize the database
        depositRepository.saveAndFlush(deposit);

        // Get all the depositList where balance is not null
        defaultDepositShouldBeFound("balance.specified=true");

        // Get all the depositList where balance is null
        defaultDepositShouldNotBeFound("balance.specified=false");
    }

    @Test
    @Transactional
    void getAllDepositsByBalanceIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        depositRepository.saveAndFlush(deposit);

        // Get all the depositList where balance is greater than or equal to DEFAULT_BALANCE
        defaultDepositShouldBeFound("balance.greaterThanOrEqual=" + DEFAULT_BALANCE);

        // Get all the depositList where balance is greater than or equal to UPDATED_BALANCE
        defaultDepositShouldNotBeFound("balance.greaterThanOrEqual=" + UPDATED_BALANCE);
    }

    @Test
    @Transactional
    void getAllDepositsByBalanceIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        depositRepository.saveAndFlush(deposit);

        // Get all the depositList where balance is less than or equal to DEFAULT_BALANCE
        defaultDepositShouldBeFound("balance.lessThanOrEqual=" + DEFAULT_BALANCE);

        // Get all the depositList where balance is less than or equal to SMALLER_BALANCE
        defaultDepositShouldNotBeFound("balance.lessThanOrEqual=" + SMALLER_BALANCE);
    }

    @Test
    @Transactional
    void getAllDepositsByBalanceIsLessThanSomething() throws Exception {
        // Initialize the database
        depositRepository.saveAndFlush(deposit);

        // Get all the depositList where balance is less than DEFAULT_BALANCE
        defaultDepositShouldNotBeFound("balance.lessThan=" + DEFAULT_BALANCE);

        // Get all the depositList where balance is less than UPDATED_BALANCE
        defaultDepositShouldBeFound("balance.lessThan=" + UPDATED_BALANCE);
    }

    @Test
    @Transactional
    void getAllDepositsByBalanceIsGreaterThanSomething() throws Exception {
        // Initialize the database
        depositRepository.saveAndFlush(deposit);

        // Get all the depositList where balance is greater than DEFAULT_BALANCE
        defaultDepositShouldNotBeFound("balance.greaterThan=" + DEFAULT_BALANCE);

        // Get all the depositList where balance is greater than SMALLER_BALANCE
        defaultDepositShouldBeFound("balance.greaterThan=" + SMALLER_BALANCE);
    }

    @Test
    @Transactional
    void getAllDepositsByBalanceCcyIsEqualToSomething() throws Exception {
        // Initialize the database
        depositRepository.saveAndFlush(deposit);

        // Get all the depositList where balanceCcy equals to DEFAULT_BALANCE_CCY
        defaultDepositShouldBeFound("balanceCcy.equals=" + DEFAULT_BALANCE_CCY);

        // Get all the depositList where balanceCcy equals to UPDATED_BALANCE_CCY
        defaultDepositShouldNotBeFound("balanceCcy.equals=" + UPDATED_BALANCE_CCY);
    }

    @Test
    @Transactional
    void getAllDepositsByBalanceCcyIsNotEqualToSomething() throws Exception {
        // Initialize the database
        depositRepository.saveAndFlush(deposit);

        // Get all the depositList where balanceCcy not equals to DEFAULT_BALANCE_CCY
        defaultDepositShouldNotBeFound("balanceCcy.notEquals=" + DEFAULT_BALANCE_CCY);

        // Get all the depositList where balanceCcy not equals to UPDATED_BALANCE_CCY
        defaultDepositShouldBeFound("balanceCcy.notEquals=" + UPDATED_BALANCE_CCY);
    }

    @Test
    @Transactional
    void getAllDepositsByBalanceCcyIsInShouldWork() throws Exception {
        // Initialize the database
        depositRepository.saveAndFlush(deposit);

        // Get all the depositList where balanceCcy in DEFAULT_BALANCE_CCY or UPDATED_BALANCE_CCY
        defaultDepositShouldBeFound("balanceCcy.in=" + DEFAULT_BALANCE_CCY + "," + UPDATED_BALANCE_CCY);

        // Get all the depositList where balanceCcy equals to UPDATED_BALANCE_CCY
        defaultDepositShouldNotBeFound("balanceCcy.in=" + UPDATED_BALANCE_CCY);
    }

    @Test
    @Transactional
    void getAllDepositsByBalanceCcyIsNullOrNotNull() throws Exception {
        // Initialize the database
        depositRepository.saveAndFlush(deposit);

        // Get all the depositList where balanceCcy is not null
        defaultDepositShouldBeFound("balanceCcy.specified=true");

        // Get all the depositList where balanceCcy is null
        defaultDepositShouldNotBeFound("balanceCcy.specified=false");
    }

    @Test
    @Transactional
    void getAllDepositsByBalanceCcyIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        depositRepository.saveAndFlush(deposit);

        // Get all the depositList where balanceCcy is greater than or equal to DEFAULT_BALANCE_CCY
        defaultDepositShouldBeFound("balanceCcy.greaterThanOrEqual=" + DEFAULT_BALANCE_CCY);

        // Get all the depositList where balanceCcy is greater than or equal to UPDATED_BALANCE_CCY
        defaultDepositShouldNotBeFound("balanceCcy.greaterThanOrEqual=" + UPDATED_BALANCE_CCY);
    }

    @Test
    @Transactional
    void getAllDepositsByBalanceCcyIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        depositRepository.saveAndFlush(deposit);

        // Get all the depositList where balanceCcy is less than or equal to DEFAULT_BALANCE_CCY
        defaultDepositShouldBeFound("balanceCcy.lessThanOrEqual=" + DEFAULT_BALANCE_CCY);

        // Get all the depositList where balanceCcy is less than or equal to SMALLER_BALANCE_CCY
        defaultDepositShouldNotBeFound("balanceCcy.lessThanOrEqual=" + SMALLER_BALANCE_CCY);
    }

    @Test
    @Transactional
    void getAllDepositsByBalanceCcyIsLessThanSomething() throws Exception {
        // Initialize the database
        depositRepository.saveAndFlush(deposit);

        // Get all the depositList where balanceCcy is less than DEFAULT_BALANCE_CCY
        defaultDepositShouldNotBeFound("balanceCcy.lessThan=" + DEFAULT_BALANCE_CCY);

        // Get all the depositList where balanceCcy is less than UPDATED_BALANCE_CCY
        defaultDepositShouldBeFound("balanceCcy.lessThan=" + UPDATED_BALANCE_CCY);
    }

    @Test
    @Transactional
    void getAllDepositsByBalanceCcyIsGreaterThanSomething() throws Exception {
        // Initialize the database
        depositRepository.saveAndFlush(deposit);

        // Get all the depositList where balanceCcy is greater than DEFAULT_BALANCE_CCY
        defaultDepositShouldNotBeFound("balanceCcy.greaterThan=" + DEFAULT_BALANCE_CCY);

        // Get all the depositList where balanceCcy is greater than SMALLER_BALANCE_CCY
        defaultDepositShouldBeFound("balanceCcy.greaterThan=" + SMALLER_BALANCE_CCY);
    }

    @Test
    @Transactional
    void getAllDepositsByBookedBalanceIsEqualToSomething() throws Exception {
        // Initialize the database
        depositRepository.saveAndFlush(deposit);

        // Get all the depositList where bookedBalance equals to DEFAULT_BOOKED_BALANCE
        defaultDepositShouldBeFound("bookedBalance.equals=" + DEFAULT_BOOKED_BALANCE);

        // Get all the depositList where bookedBalance equals to UPDATED_BOOKED_BALANCE
        defaultDepositShouldNotBeFound("bookedBalance.equals=" + UPDATED_BOOKED_BALANCE);
    }

    @Test
    @Transactional
    void getAllDepositsByBookedBalanceIsNotEqualToSomething() throws Exception {
        // Initialize the database
        depositRepository.saveAndFlush(deposit);

        // Get all the depositList where bookedBalance not equals to DEFAULT_BOOKED_BALANCE
        defaultDepositShouldNotBeFound("bookedBalance.notEquals=" + DEFAULT_BOOKED_BALANCE);

        // Get all the depositList where bookedBalance not equals to UPDATED_BOOKED_BALANCE
        defaultDepositShouldBeFound("bookedBalance.notEquals=" + UPDATED_BOOKED_BALANCE);
    }

    @Test
    @Transactional
    void getAllDepositsByBookedBalanceIsInShouldWork() throws Exception {
        // Initialize the database
        depositRepository.saveAndFlush(deposit);

        // Get all the depositList where bookedBalance in DEFAULT_BOOKED_BALANCE or UPDATED_BOOKED_BALANCE
        defaultDepositShouldBeFound("bookedBalance.in=" + DEFAULT_BOOKED_BALANCE + "," + UPDATED_BOOKED_BALANCE);

        // Get all the depositList where bookedBalance equals to UPDATED_BOOKED_BALANCE
        defaultDepositShouldNotBeFound("bookedBalance.in=" + UPDATED_BOOKED_BALANCE);
    }

    @Test
    @Transactional
    void getAllDepositsByBookedBalanceIsNullOrNotNull() throws Exception {
        // Initialize the database
        depositRepository.saveAndFlush(deposit);

        // Get all the depositList where bookedBalance is not null
        defaultDepositShouldBeFound("bookedBalance.specified=true");

        // Get all the depositList where bookedBalance is null
        defaultDepositShouldNotBeFound("bookedBalance.specified=false");
    }

    @Test
    @Transactional
    void getAllDepositsByBookedBalanceIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        depositRepository.saveAndFlush(deposit);

        // Get all the depositList where bookedBalance is greater than or equal to DEFAULT_BOOKED_BALANCE
        defaultDepositShouldBeFound("bookedBalance.greaterThanOrEqual=" + DEFAULT_BOOKED_BALANCE);

        // Get all the depositList where bookedBalance is greater than or equal to UPDATED_BOOKED_BALANCE
        defaultDepositShouldNotBeFound("bookedBalance.greaterThanOrEqual=" + UPDATED_BOOKED_BALANCE);
    }

    @Test
    @Transactional
    void getAllDepositsByBookedBalanceIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        depositRepository.saveAndFlush(deposit);

        // Get all the depositList where bookedBalance is less than or equal to DEFAULT_BOOKED_BALANCE
        defaultDepositShouldBeFound("bookedBalance.lessThanOrEqual=" + DEFAULT_BOOKED_BALANCE);

        // Get all the depositList where bookedBalance is less than or equal to SMALLER_BOOKED_BALANCE
        defaultDepositShouldNotBeFound("bookedBalance.lessThanOrEqual=" + SMALLER_BOOKED_BALANCE);
    }

    @Test
    @Transactional
    void getAllDepositsByBookedBalanceIsLessThanSomething() throws Exception {
        // Initialize the database
        depositRepository.saveAndFlush(deposit);

        // Get all the depositList where bookedBalance is less than DEFAULT_BOOKED_BALANCE
        defaultDepositShouldNotBeFound("bookedBalance.lessThan=" + DEFAULT_BOOKED_BALANCE);

        // Get all the depositList where bookedBalance is less than UPDATED_BOOKED_BALANCE
        defaultDepositShouldBeFound("bookedBalance.lessThan=" + UPDATED_BOOKED_BALANCE);
    }

    @Test
    @Transactional
    void getAllDepositsByBookedBalanceIsGreaterThanSomething() throws Exception {
        // Initialize the database
        depositRepository.saveAndFlush(deposit);

        // Get all the depositList where bookedBalance is greater than DEFAULT_BOOKED_BALANCE
        defaultDepositShouldNotBeFound("bookedBalance.greaterThan=" + DEFAULT_BOOKED_BALANCE);

        // Get all the depositList where bookedBalance is greater than SMALLER_BOOKED_BALANCE
        defaultDepositShouldBeFound("bookedBalance.greaterThan=" + SMALLER_BOOKED_BALANCE);
    }

    @Test
    @Transactional
    void getAllDepositsByLimitInformIsEqualToSomething() throws Exception {
        // Initialize the database
        depositRepository.saveAndFlush(deposit);

        // Get all the depositList where limitInform equals to DEFAULT_LIMIT_INFORM
        defaultDepositShouldBeFound("limitInform.equals=" + DEFAULT_LIMIT_INFORM);

        // Get all the depositList where limitInform equals to UPDATED_LIMIT_INFORM
        defaultDepositShouldNotBeFound("limitInform.equals=" + UPDATED_LIMIT_INFORM);
    }

    @Test
    @Transactional
    void getAllDepositsByLimitInformIsNotEqualToSomething() throws Exception {
        // Initialize the database
        depositRepository.saveAndFlush(deposit);

        // Get all the depositList where limitInform not equals to DEFAULT_LIMIT_INFORM
        defaultDepositShouldNotBeFound("limitInform.notEquals=" + DEFAULT_LIMIT_INFORM);

        // Get all the depositList where limitInform not equals to UPDATED_LIMIT_INFORM
        defaultDepositShouldBeFound("limitInform.notEquals=" + UPDATED_LIMIT_INFORM);
    }

    @Test
    @Transactional
    void getAllDepositsByLimitInformIsInShouldWork() throws Exception {
        // Initialize the database
        depositRepository.saveAndFlush(deposit);

        // Get all the depositList where limitInform in DEFAULT_LIMIT_INFORM or UPDATED_LIMIT_INFORM
        defaultDepositShouldBeFound("limitInform.in=" + DEFAULT_LIMIT_INFORM + "," + UPDATED_LIMIT_INFORM);

        // Get all the depositList where limitInform equals to UPDATED_LIMIT_INFORM
        defaultDepositShouldNotBeFound("limitInform.in=" + UPDATED_LIMIT_INFORM);
    }

    @Test
    @Transactional
    void getAllDepositsByLimitInformIsNullOrNotNull() throws Exception {
        // Initialize the database
        depositRepository.saveAndFlush(deposit);

        // Get all the depositList where limitInform is not null
        defaultDepositShouldBeFound("limitInform.specified=true");

        // Get all the depositList where limitInform is null
        defaultDepositShouldNotBeFound("limitInform.specified=false");
    }

    @Test
    @Transactional
    void getAllDepositsByLimitInformIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        depositRepository.saveAndFlush(deposit);

        // Get all the depositList where limitInform is greater than or equal to DEFAULT_LIMIT_INFORM
        defaultDepositShouldBeFound("limitInform.greaterThanOrEqual=" + DEFAULT_LIMIT_INFORM);

        // Get all the depositList where limitInform is greater than or equal to UPDATED_LIMIT_INFORM
        defaultDepositShouldNotBeFound("limitInform.greaterThanOrEqual=" + UPDATED_LIMIT_INFORM);
    }

    @Test
    @Transactional
    void getAllDepositsByLimitInformIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        depositRepository.saveAndFlush(deposit);

        // Get all the depositList where limitInform is less than or equal to DEFAULT_LIMIT_INFORM
        defaultDepositShouldBeFound("limitInform.lessThanOrEqual=" + DEFAULT_LIMIT_INFORM);

        // Get all the depositList where limitInform is less than or equal to SMALLER_LIMIT_INFORM
        defaultDepositShouldNotBeFound("limitInform.lessThanOrEqual=" + SMALLER_LIMIT_INFORM);
    }

    @Test
    @Transactional
    void getAllDepositsByLimitInformIsLessThanSomething() throws Exception {
        // Initialize the database
        depositRepository.saveAndFlush(deposit);

        // Get all the depositList where limitInform is less than DEFAULT_LIMIT_INFORM
        defaultDepositShouldNotBeFound("limitInform.lessThan=" + DEFAULT_LIMIT_INFORM);

        // Get all the depositList where limitInform is less than UPDATED_LIMIT_INFORM
        defaultDepositShouldBeFound("limitInform.lessThan=" + UPDATED_LIMIT_INFORM);
    }

    @Test
    @Transactional
    void getAllDepositsByLimitInformIsGreaterThanSomething() throws Exception {
        // Initialize the database
        depositRepository.saveAndFlush(deposit);

        // Get all the depositList where limitInform is greater than DEFAULT_LIMIT_INFORM
        defaultDepositShouldNotBeFound("limitInform.greaterThan=" + DEFAULT_LIMIT_INFORM);

        // Get all the depositList where limitInform is greater than SMALLER_LIMIT_INFORM
        defaultDepositShouldBeFound("limitInform.greaterThan=" + SMALLER_LIMIT_INFORM);
    }

    @Test
    @Transactional
    void getAllDepositsByLimitDisableIsEqualToSomething() throws Exception {
        // Initialize the database
        depositRepository.saveAndFlush(deposit);

        // Get all the depositList where limitDisable equals to DEFAULT_LIMIT_DISABLE
        defaultDepositShouldBeFound("limitDisable.equals=" + DEFAULT_LIMIT_DISABLE);

        // Get all the depositList where limitDisable equals to UPDATED_LIMIT_DISABLE
        defaultDepositShouldNotBeFound("limitDisable.equals=" + UPDATED_LIMIT_DISABLE);
    }

    @Test
    @Transactional
    void getAllDepositsByLimitDisableIsNotEqualToSomething() throws Exception {
        // Initialize the database
        depositRepository.saveAndFlush(deposit);

        // Get all the depositList where limitDisable not equals to DEFAULT_LIMIT_DISABLE
        defaultDepositShouldNotBeFound("limitDisable.notEquals=" + DEFAULT_LIMIT_DISABLE);

        // Get all the depositList where limitDisable not equals to UPDATED_LIMIT_DISABLE
        defaultDepositShouldBeFound("limitDisable.notEquals=" + UPDATED_LIMIT_DISABLE);
    }

    @Test
    @Transactional
    void getAllDepositsByLimitDisableIsInShouldWork() throws Exception {
        // Initialize the database
        depositRepository.saveAndFlush(deposit);

        // Get all the depositList where limitDisable in DEFAULT_LIMIT_DISABLE or UPDATED_LIMIT_DISABLE
        defaultDepositShouldBeFound("limitDisable.in=" + DEFAULT_LIMIT_DISABLE + "," + UPDATED_LIMIT_DISABLE);

        // Get all the depositList where limitDisable equals to UPDATED_LIMIT_DISABLE
        defaultDepositShouldNotBeFound("limitDisable.in=" + UPDATED_LIMIT_DISABLE);
    }

    @Test
    @Transactional
    void getAllDepositsByLimitDisableIsNullOrNotNull() throws Exception {
        // Initialize the database
        depositRepository.saveAndFlush(deposit);

        // Get all the depositList where limitDisable is not null
        defaultDepositShouldBeFound("limitDisable.specified=true");

        // Get all the depositList where limitDisable is null
        defaultDepositShouldNotBeFound("limitDisable.specified=false");
    }

    @Test
    @Transactional
    void getAllDepositsByLimitDisableIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        depositRepository.saveAndFlush(deposit);

        // Get all the depositList where limitDisable is greater than or equal to DEFAULT_LIMIT_DISABLE
        defaultDepositShouldBeFound("limitDisable.greaterThanOrEqual=" + DEFAULT_LIMIT_DISABLE);

        // Get all the depositList where limitDisable is greater than or equal to UPDATED_LIMIT_DISABLE
        defaultDepositShouldNotBeFound("limitDisable.greaterThanOrEqual=" + UPDATED_LIMIT_DISABLE);
    }

    @Test
    @Transactional
    void getAllDepositsByLimitDisableIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        depositRepository.saveAndFlush(deposit);

        // Get all the depositList where limitDisable is less than or equal to DEFAULT_LIMIT_DISABLE
        defaultDepositShouldBeFound("limitDisable.lessThanOrEqual=" + DEFAULT_LIMIT_DISABLE);

        // Get all the depositList where limitDisable is less than or equal to SMALLER_LIMIT_DISABLE
        defaultDepositShouldNotBeFound("limitDisable.lessThanOrEqual=" + SMALLER_LIMIT_DISABLE);
    }

    @Test
    @Transactional
    void getAllDepositsByLimitDisableIsLessThanSomething() throws Exception {
        // Initialize the database
        depositRepository.saveAndFlush(deposit);

        // Get all the depositList where limitDisable is less than DEFAULT_LIMIT_DISABLE
        defaultDepositShouldNotBeFound("limitDisable.lessThan=" + DEFAULT_LIMIT_DISABLE);

        // Get all the depositList where limitDisable is less than UPDATED_LIMIT_DISABLE
        defaultDepositShouldBeFound("limitDisable.lessThan=" + UPDATED_LIMIT_DISABLE);
    }

    @Test
    @Transactional
    void getAllDepositsByLimitDisableIsGreaterThanSomething() throws Exception {
        // Initialize the database
        depositRepository.saveAndFlush(deposit);

        // Get all the depositList where limitDisable is greater than DEFAULT_LIMIT_DISABLE
        defaultDepositShouldNotBeFound("limitDisable.greaterThan=" + DEFAULT_LIMIT_DISABLE);

        // Get all the depositList where limitDisable is greater than SMALLER_LIMIT_DISABLE
        defaultDepositShouldBeFound("limitDisable.greaterThan=" + SMALLER_LIMIT_DISABLE);
    }

    @Test
    @Transactional
    void getAllDepositsByContactGroupIsEqualToSomething() throws Exception {
        // Initialize the database
        depositRepository.saveAndFlush(deposit);

        // Get all the depositList where contactGroup equals to DEFAULT_CONTACT_GROUP
        defaultDepositShouldBeFound("contactGroup.equals=" + DEFAULT_CONTACT_GROUP);

        // Get all the depositList where contactGroup equals to UPDATED_CONTACT_GROUP
        defaultDepositShouldNotBeFound("contactGroup.equals=" + UPDATED_CONTACT_GROUP);
    }

    @Test
    @Transactional
    void getAllDepositsByContactGroupIsNotEqualToSomething() throws Exception {
        // Initialize the database
        depositRepository.saveAndFlush(deposit);

        // Get all the depositList where contactGroup not equals to DEFAULT_CONTACT_GROUP
        defaultDepositShouldNotBeFound("contactGroup.notEquals=" + DEFAULT_CONTACT_GROUP);

        // Get all the depositList where contactGroup not equals to UPDATED_CONTACT_GROUP
        defaultDepositShouldBeFound("contactGroup.notEquals=" + UPDATED_CONTACT_GROUP);
    }

    @Test
    @Transactional
    void getAllDepositsByContactGroupIsInShouldWork() throws Exception {
        // Initialize the database
        depositRepository.saveAndFlush(deposit);

        // Get all the depositList where contactGroup in DEFAULT_CONTACT_GROUP or UPDATED_CONTACT_GROUP
        defaultDepositShouldBeFound("contactGroup.in=" + DEFAULT_CONTACT_GROUP + "," + UPDATED_CONTACT_GROUP);

        // Get all the depositList where contactGroup equals to UPDATED_CONTACT_GROUP
        defaultDepositShouldNotBeFound("contactGroup.in=" + UPDATED_CONTACT_GROUP);
    }

    @Test
    @Transactional
    void getAllDepositsByContactGroupIsNullOrNotNull() throws Exception {
        // Initialize the database
        depositRepository.saveAndFlush(deposit);

        // Get all the depositList where contactGroup is not null
        defaultDepositShouldBeFound("contactGroup.specified=true");

        // Get all the depositList where contactGroup is null
        defaultDepositShouldNotBeFound("contactGroup.specified=false");
    }

    @Test
    @Transactional
    void getAllDepositsByContactGroupContainsSomething() throws Exception {
        // Initialize the database
        depositRepository.saveAndFlush(deposit);

        // Get all the depositList where contactGroup contains DEFAULT_CONTACT_GROUP
        defaultDepositShouldBeFound("contactGroup.contains=" + DEFAULT_CONTACT_GROUP);

        // Get all the depositList where contactGroup contains UPDATED_CONTACT_GROUP
        defaultDepositShouldNotBeFound("contactGroup.contains=" + UPDATED_CONTACT_GROUP);
    }

    @Test
    @Transactional
    void getAllDepositsByContactGroupNotContainsSomething() throws Exception {
        // Initialize the database
        depositRepository.saveAndFlush(deposit);

        // Get all the depositList where contactGroup does not contain DEFAULT_CONTACT_GROUP
        defaultDepositShouldNotBeFound("contactGroup.doesNotContain=" + DEFAULT_CONTACT_GROUP);

        // Get all the depositList where contactGroup does not contain UPDATED_CONTACT_GROUP
        defaultDepositShouldBeFound("contactGroup.doesNotContain=" + UPDATED_CONTACT_GROUP);
    }

    @Test
    @Transactional
    void getAllDepositsByBlockedIsEqualToSomething() throws Exception {
        // Initialize the database
        depositRepository.saveAndFlush(deposit);

        // Get all the depositList where blocked equals to DEFAULT_BLOCKED
        defaultDepositShouldBeFound("blocked.equals=" + DEFAULT_BLOCKED);

        // Get all the depositList where blocked equals to UPDATED_BLOCKED
        defaultDepositShouldNotBeFound("blocked.equals=" + UPDATED_BLOCKED);
    }

    @Test
    @Transactional
    void getAllDepositsByBlockedIsNotEqualToSomething() throws Exception {
        // Initialize the database
        depositRepository.saveAndFlush(deposit);

        // Get all the depositList where blocked not equals to DEFAULT_BLOCKED
        defaultDepositShouldNotBeFound("blocked.notEquals=" + DEFAULT_BLOCKED);

        // Get all the depositList where blocked not equals to UPDATED_BLOCKED
        defaultDepositShouldBeFound("blocked.notEquals=" + UPDATED_BLOCKED);
    }

    @Test
    @Transactional
    void getAllDepositsByBlockedIsInShouldWork() throws Exception {
        // Initialize the database
        depositRepository.saveAndFlush(deposit);

        // Get all the depositList where blocked in DEFAULT_BLOCKED or UPDATED_BLOCKED
        defaultDepositShouldBeFound("blocked.in=" + DEFAULT_BLOCKED + "," + UPDATED_BLOCKED);

        // Get all the depositList where blocked equals to UPDATED_BLOCKED
        defaultDepositShouldNotBeFound("blocked.in=" + UPDATED_BLOCKED);
    }

    @Test
    @Transactional
    void getAllDepositsByBlockedIsNullOrNotNull() throws Exception {
        // Initialize the database
        depositRepository.saveAndFlush(deposit);

        // Get all the depositList where blocked is not null
        defaultDepositShouldBeFound("blocked.specified=true");

        // Get all the depositList where blocked is null
        defaultDepositShouldNotBeFound("blocked.specified=false");
    }

    @Test
    @Transactional
    void getAllDepositsByUnlimitIsEqualToSomething() throws Exception {
        // Initialize the database
        depositRepository.saveAndFlush(deposit);

        // Get all the depositList where unlimit equals to DEFAULT_UNLIMIT
        defaultDepositShouldBeFound("unlimit.equals=" + DEFAULT_UNLIMIT);

        // Get all the depositList where unlimit equals to UPDATED_UNLIMIT
        defaultDepositShouldNotBeFound("unlimit.equals=" + UPDATED_UNLIMIT);
    }

    @Test
    @Transactional
    void getAllDepositsByUnlimitIsNotEqualToSomething() throws Exception {
        // Initialize the database
        depositRepository.saveAndFlush(deposit);

        // Get all the depositList where unlimit not equals to DEFAULT_UNLIMIT
        defaultDepositShouldNotBeFound("unlimit.notEquals=" + DEFAULT_UNLIMIT);

        // Get all the depositList where unlimit not equals to UPDATED_UNLIMIT
        defaultDepositShouldBeFound("unlimit.notEquals=" + UPDATED_UNLIMIT);
    }

    @Test
    @Transactional
    void getAllDepositsByUnlimitIsInShouldWork() throws Exception {
        // Initialize the database
        depositRepository.saveAndFlush(deposit);

        // Get all the depositList where unlimit in DEFAULT_UNLIMIT or UPDATED_UNLIMIT
        defaultDepositShouldBeFound("unlimit.in=" + DEFAULT_UNLIMIT + "," + UPDATED_UNLIMIT);

        // Get all the depositList where unlimit equals to UPDATED_UNLIMIT
        defaultDepositShouldNotBeFound("unlimit.in=" + UPDATED_UNLIMIT);
    }

    @Test
    @Transactional
    void getAllDepositsByUnlimitIsNullOrNotNull() throws Exception {
        // Initialize the database
        depositRepository.saveAndFlush(deposit);

        // Get all the depositList where unlimit is not null
        defaultDepositShouldBeFound("unlimit.specified=true");

        // Get all the depositList where unlimit is null
        defaultDepositShouldNotBeFound("unlimit.specified=false");
    }

    @Test
    @Transactional
    void getAllDepositsByGatewayIsEqualToSomething() throws Exception {
        // Initialize the database
        depositRepository.saveAndFlush(deposit);
        Gateway gateway = GatewayResourceIT.createEntity(em);
        em.persist(gateway);
        em.flush();
        deposit.setGateway(gateway);
        depositRepository.saveAndFlush(deposit);
        Long gatewayId = gateway.getId();

        // Get all the depositList where gateway equals to gatewayId
        defaultDepositShouldBeFound("gatewayId.equals=" + gatewayId);

        // Get all the depositList where gateway equals to (gatewayId + 1)
        defaultDepositShouldNotBeFound("gatewayId.equals=" + (gatewayId + 1));
    }

    @Test
    @Transactional
    void getAllDepositsByAgentIsEqualToSomething() throws Exception {
        // Initialize the database
        depositRepository.saveAndFlush(deposit);
        Agent agent = AgentResourceIT.createEntity(em);
        em.persist(agent);
        em.flush();
        deposit.setAgent(agent);
        depositRepository.saveAndFlush(deposit);
        Long agentId = agent.getId();

        // Get all the depositList where agent equals to agentId
        defaultDepositShouldBeFound("agentId.equals=" + agentId);

        // Get all the depositList where agent equals to (agentId + 1)
        defaultDepositShouldNotBeFound("agentId.equals=" + (agentId + 1));
    }

    @Test
    @Transactional
    void getAllDepositsByVirtualAccountIsEqualToSomething() throws Exception {
        // Initialize the database
        depositRepository.saveAndFlush(deposit);
        VirtualAccount virtualAccount = VirtualAccountResourceIT.createEntity(em);
        em.persist(virtualAccount);
        em.flush();
        deposit.setVirtualAccount(virtualAccount);
        depositRepository.saveAndFlush(deposit);
        Long virtualAccountId = virtualAccount.getId();

        // Get all the depositList where virtualAccount equals to virtualAccountId
        defaultDepositShouldBeFound("virtualAccountId.equals=" + virtualAccountId);

        // Get all the depositList where virtualAccount equals to (virtualAccountId + 1)
        defaultDepositShouldNotBeFound("virtualAccountId.equals=" + (virtualAccountId + 1));
    }

    /**
     * Executes the search, and checks that the default entity is returned.
     */
    private void defaultDepositShouldBeFound(String filter) throws Exception {
        restDepositMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(deposit.getId().intValue())))
            .andExpect(jsonPath("$.[*].overdraftLimit").value(hasItem(DEFAULT_OVERDRAFT_LIMIT.intValue())))
            .andExpect(jsonPath("$.[*].balance").value(hasItem(DEFAULT_BALANCE.intValue())))
            .andExpect(jsonPath("$.[*].balanceCcy").value(hasItem(DEFAULT_BALANCE_CCY)))
            .andExpect(jsonPath("$.[*].bookedBalance").value(hasItem(DEFAULT_BOOKED_BALANCE.intValue())))
            .andExpect(jsonPath("$.[*].limitInform").value(hasItem(DEFAULT_LIMIT_INFORM.intValue())))
            .andExpect(jsonPath("$.[*].limitDisable").value(hasItem(DEFAULT_LIMIT_DISABLE.intValue())))
            .andExpect(jsonPath("$.[*].contactGroup").value(hasItem(DEFAULT_CONTACT_GROUP)))
            .andExpect(jsonPath("$.[*].blocked").value(hasItem(DEFAULT_BLOCKED.booleanValue())))
            .andExpect(jsonPath("$.[*].unlimit").value(hasItem(DEFAULT_UNLIMIT.booleanValue())));

        // Check, that the count call also returns 1
        restDepositMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("1"));
    }

    /**
     * Executes the search, and checks that the default entity is not returned.
     */
    private void defaultDepositShouldNotBeFound(String filter) throws Exception {
        restDepositMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty());

        // Check, that the count call also returns 0
        restDepositMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("0"));
    }

    @Test
    @Transactional
    void getNonExistingDeposit() throws Exception {
        // Get the deposit
        restDepositMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putNewDeposit() throws Exception {
        // Initialize the database
        depositRepository.saveAndFlush(deposit);

        int databaseSizeBeforeUpdate = depositRepository.findAll().size();

        // Update the deposit
        Deposit updatedDeposit = depositRepository.findById(deposit.getId()).get();
        // Disconnect from session so that the updates on updatedDeposit are not directly saved in db
        em.detach(updatedDeposit);
        updatedDeposit
            .overdraftLimit(UPDATED_OVERDRAFT_LIMIT)
            .balance(UPDATED_BALANCE)
            .balanceCcy(UPDATED_BALANCE_CCY)
            .bookedBalance(UPDATED_BOOKED_BALANCE)
            .limitInform(UPDATED_LIMIT_INFORM)
            .limitDisable(UPDATED_LIMIT_DISABLE)
            .contactGroup(UPDATED_CONTACT_GROUP)
            .blocked(UPDATED_BLOCKED)
            .unlimit(UPDATED_UNLIMIT);
        DepositDTO depositDTO = depositMapper.toDto(updatedDeposit);

        restDepositMockMvc
            .perform(
                put(ENTITY_API_URL_ID, depositDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(depositDTO))
            )
            .andExpect(status().isOk());

        // Validate the Deposit in the database
        List<Deposit> depositList = depositRepository.findAll();
        assertThat(depositList).hasSize(databaseSizeBeforeUpdate);
        Deposit testDeposit = depositList.get(depositList.size() - 1);
        assertThat(testDeposit.getOverdraftLimit()).isEqualTo(UPDATED_OVERDRAFT_LIMIT);
        assertThat(testDeposit.getBalance()).isEqualTo(UPDATED_BALANCE);
        assertThat(testDeposit.getBalanceCcy()).isEqualTo(UPDATED_BALANCE_CCY);
        assertThat(testDeposit.getBookedBalance()).isEqualTo(UPDATED_BOOKED_BALANCE);
        assertThat(testDeposit.getLimitInform()).isEqualTo(UPDATED_LIMIT_INFORM);
        assertThat(testDeposit.getLimitDisable()).isEqualTo(UPDATED_LIMIT_DISABLE);
        assertThat(testDeposit.getContactGroup()).isEqualTo(UPDATED_CONTACT_GROUP);
        assertThat(testDeposit.getBlocked()).isEqualTo(UPDATED_BLOCKED);
        assertThat(testDeposit.getUnlimit()).isEqualTo(UPDATED_UNLIMIT);
    }

    @Test
    @Transactional
    void putNonExistingDeposit() throws Exception {
        int databaseSizeBeforeUpdate = depositRepository.findAll().size();
        deposit.setId(count.incrementAndGet());

        // Create the Deposit
        DepositDTO depositDTO = depositMapper.toDto(deposit);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restDepositMockMvc
            .perform(
                put(ENTITY_API_URL_ID, depositDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(depositDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Deposit in the database
        List<Deposit> depositList = depositRepository.findAll();
        assertThat(depositList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchDeposit() throws Exception {
        int databaseSizeBeforeUpdate = depositRepository.findAll().size();
        deposit.setId(count.incrementAndGet());

        // Create the Deposit
        DepositDTO depositDTO = depositMapper.toDto(deposit);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restDepositMockMvc
            .perform(
                put(ENTITY_API_URL_ID, count.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(depositDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Deposit in the database
        List<Deposit> depositList = depositRepository.findAll();
        assertThat(depositList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamDeposit() throws Exception {
        int databaseSizeBeforeUpdate = depositRepository.findAll().size();
        deposit.setId(count.incrementAndGet());

        // Create the Deposit
        DepositDTO depositDTO = depositMapper.toDto(deposit);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restDepositMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(depositDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Deposit in the database
        List<Deposit> depositList = depositRepository.findAll();
        assertThat(depositList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateDepositWithPatch() throws Exception {
        // Initialize the database
        depositRepository.saveAndFlush(deposit);

        int databaseSizeBeforeUpdate = depositRepository.findAll().size();

        // Update the deposit using partial update
        Deposit partialUpdatedDeposit = new Deposit();
        partialUpdatedDeposit.setId(deposit.getId());

        partialUpdatedDeposit
            .balanceCcy(UPDATED_BALANCE_CCY)
            .bookedBalance(UPDATED_BOOKED_BALANCE)
            .limitInform(UPDATED_LIMIT_INFORM)
            .unlimit(UPDATED_UNLIMIT);

        restDepositMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedDeposit.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedDeposit))
            )
            .andExpect(status().isOk());

        // Validate the Deposit in the database
        List<Deposit> depositList = depositRepository.findAll();
        assertThat(depositList).hasSize(databaseSizeBeforeUpdate);
        Deposit testDeposit = depositList.get(depositList.size() - 1);
        assertThat(testDeposit.getOverdraftLimit()).isEqualTo(DEFAULT_OVERDRAFT_LIMIT);
        assertThat(testDeposit.getBalance()).isEqualTo(DEFAULT_BALANCE);
        assertThat(testDeposit.getBalanceCcy()).isEqualTo(UPDATED_BALANCE_CCY);
        assertThat(testDeposit.getBookedBalance()).isEqualTo(UPDATED_BOOKED_BALANCE);
        assertThat(testDeposit.getLimitInform()).isEqualTo(UPDATED_LIMIT_INFORM);
        assertThat(testDeposit.getLimitDisable()).isEqualTo(DEFAULT_LIMIT_DISABLE);
        assertThat(testDeposit.getContactGroup()).isEqualTo(DEFAULT_CONTACT_GROUP);
        assertThat(testDeposit.getBlocked()).isEqualTo(DEFAULT_BLOCKED);
        assertThat(testDeposit.getUnlimit()).isEqualTo(UPDATED_UNLIMIT);
    }

    @Test
    @Transactional
    void fullUpdateDepositWithPatch() throws Exception {
        // Initialize the database
        depositRepository.saveAndFlush(deposit);

        int databaseSizeBeforeUpdate = depositRepository.findAll().size();

        // Update the deposit using partial update
        Deposit partialUpdatedDeposit = new Deposit();
        partialUpdatedDeposit.setId(deposit.getId());

        partialUpdatedDeposit
            .overdraftLimit(UPDATED_OVERDRAFT_LIMIT)
            .balance(UPDATED_BALANCE)
            .balanceCcy(UPDATED_BALANCE_CCY)
            .bookedBalance(UPDATED_BOOKED_BALANCE)
            .limitInform(UPDATED_LIMIT_INFORM)
            .limitDisable(UPDATED_LIMIT_DISABLE)
            .contactGroup(UPDATED_CONTACT_GROUP)
            .blocked(UPDATED_BLOCKED)
            .unlimit(UPDATED_UNLIMIT);

        restDepositMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedDeposit.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedDeposit))
            )
            .andExpect(status().isOk());

        // Validate the Deposit in the database
        List<Deposit> depositList = depositRepository.findAll();
        assertThat(depositList).hasSize(databaseSizeBeforeUpdate);
        Deposit testDeposit = depositList.get(depositList.size() - 1);
        assertThat(testDeposit.getOverdraftLimit()).isEqualTo(UPDATED_OVERDRAFT_LIMIT);
        assertThat(testDeposit.getBalance()).isEqualTo(UPDATED_BALANCE);
        assertThat(testDeposit.getBalanceCcy()).isEqualTo(UPDATED_BALANCE_CCY);
        assertThat(testDeposit.getBookedBalance()).isEqualTo(UPDATED_BOOKED_BALANCE);
        assertThat(testDeposit.getLimitInform()).isEqualTo(UPDATED_LIMIT_INFORM);
        assertThat(testDeposit.getLimitDisable()).isEqualTo(UPDATED_LIMIT_DISABLE);
        assertThat(testDeposit.getContactGroup()).isEqualTo(UPDATED_CONTACT_GROUP);
        assertThat(testDeposit.getBlocked()).isEqualTo(UPDATED_BLOCKED);
        assertThat(testDeposit.getUnlimit()).isEqualTo(UPDATED_UNLIMIT);
    }

    @Test
    @Transactional
    void patchNonExistingDeposit() throws Exception {
        int databaseSizeBeforeUpdate = depositRepository.findAll().size();
        deposit.setId(count.incrementAndGet());

        // Create the Deposit
        DepositDTO depositDTO = depositMapper.toDto(deposit);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restDepositMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, depositDTO.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(depositDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Deposit in the database
        List<Deposit> depositList = depositRepository.findAll();
        assertThat(depositList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchDeposit() throws Exception {
        int databaseSizeBeforeUpdate = depositRepository.findAll().size();
        deposit.setId(count.incrementAndGet());

        // Create the Deposit
        DepositDTO depositDTO = depositMapper.toDto(deposit);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restDepositMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, count.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(depositDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Deposit in the database
        List<Deposit> depositList = depositRepository.findAll();
        assertThat(depositList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamDeposit() throws Exception {
        int databaseSizeBeforeUpdate = depositRepository.findAll().size();
        deposit.setId(count.incrementAndGet());

        // Create the Deposit
        DepositDTO depositDTO = depositMapper.toDto(deposit);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restDepositMockMvc
            .perform(
                patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(TestUtil.convertObjectToJsonBytes(depositDTO))
            )
            .andExpect(status().isMethodNotAllowed());

        // Validate the Deposit in the database
        List<Deposit> depositList = depositRepository.findAll();
        assertThat(depositList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteDeposit() throws Exception {
        // Initialize the database
        depositRepository.saveAndFlush(deposit);

        int databaseSizeBeforeDelete = depositRepository.findAll().size();

        // Delete the deposit
        restDepositMockMvc
            .perform(delete("/api/deposits/hard/{id}", deposit.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        List<Deposit> depositList = depositRepository.findAll();
        assertThat(depositList).hasSize(databaseSizeBeforeDelete - 1);
    }
}
