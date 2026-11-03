package uz.paykap.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
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
import uz.paykap.domain.DepositTransaction;
import uz.paykap.repository.DepositTransactionRepository;
import uz.paykap.service.criteria.DepositTransactionCriteria;
import uz.paykap.service.dto.DepositTransactionDTO;
import uz.paykap.service.mapper.DepositTransactionMapper;

/**
 * Integration tests for the {@link DepositTransactionResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class DepositTransactionResourceIT {

    private static final String DEFAULT_PAYER_TAXNR = "AAAAAAAAAA";
    private static final String UPDATED_PAYER_TAXNR = "BBBBBBBBBB";

    private static final String DEFAULT_RECIPIENT_TAXNR = "AAAAAAAAAA";
    private static final String UPDATED_RECIPIENT_TAXNR = "BBBBBBBBBB";

    private static final Long DEFAULT_AMOUNT = 1L;
    private static final Long UPDATED_AMOUNT = 2L;
    private static final Long SMALLER_AMOUNT = 1L - 1L;

    private static final Instant DEFAULT_UPDATE_DATE = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_UPDATE_DATE = Instant.now().truncatedTo(ChronoUnit.MILLIS);

    private static final String DEFAULT_COMMENT = "AAAAAAAAAA";
    private static final String UPDATED_COMMENT = "BBBBBBBBBB";

    private static final String DEFAULT_DOC = "AAAAAAAAAA";
    private static final String UPDATED_DOC = "BBBBBBBBBB";

    private static final String ENTITY_API_URL = "/api/deposit-transactions";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong count = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private DepositTransactionRepository depositTransactionRepository;

    @Autowired
    private DepositTransactionMapper depositTransactionMapper;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restDepositTransactionMockMvc;

    private DepositTransaction depositTransaction;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static DepositTransaction createEntity(EntityManager em) {
        DepositTransaction depositTransaction = new DepositTransaction()
            .payerTaxnr(DEFAULT_PAYER_TAXNR)
            .recipientTaxnr(DEFAULT_RECIPIENT_TAXNR)
            .amount(DEFAULT_AMOUNT)
            .updateDate(DEFAULT_UPDATE_DATE)
            .comment(DEFAULT_COMMENT)
            .doc(DEFAULT_DOC);
        return depositTransaction;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static DepositTransaction createUpdatedEntity(EntityManager em) {
        DepositTransaction depositTransaction = new DepositTransaction()
            .payerTaxnr(UPDATED_PAYER_TAXNR)
            .recipientTaxnr(UPDATED_RECIPIENT_TAXNR)
            .amount(UPDATED_AMOUNT)
            .updateDate(UPDATED_UPDATE_DATE)
            .comment(UPDATED_COMMENT)
            .doc(UPDATED_DOC);
        return depositTransaction;
    }

    @BeforeEach
    public void initTest() {
        depositTransaction = createEntity(em);
    }

    @Test
    @Transactional
    void createDepositTransaction() throws Exception {
        int databaseSizeBeforeCreate = depositTransactionRepository.findAll().size();
        // Create the DepositTransaction
        DepositTransactionDTO depositTransactionDTO = depositTransactionMapper.toDto(depositTransaction);
        restDepositTransactionMockMvc
            .perform(
                post(ENTITY_API_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(depositTransactionDTO))
            )
            .andExpect(status().isCreated());

        // Validate the DepositTransaction in the database
        List<DepositTransaction> depositTransactionList = depositTransactionRepository.findAll();
        assertThat(depositTransactionList).hasSize(databaseSizeBeforeCreate + 1);
        DepositTransaction testDepositTransaction = depositTransactionList.get(depositTransactionList.size() - 1);
        assertThat(testDepositTransaction.getPayerTaxnr()).isEqualTo(DEFAULT_PAYER_TAXNR);
        assertThat(testDepositTransaction.getRecipientTaxnr()).isEqualTo(DEFAULT_RECIPIENT_TAXNR);
        assertThat(testDepositTransaction.getAmount()).isEqualTo(DEFAULT_AMOUNT);
        assertThat(testDepositTransaction.getUpdateDate()).isEqualTo(DEFAULT_UPDATE_DATE);
        assertThat(testDepositTransaction.getComment()).isEqualTo(DEFAULT_COMMENT);
        assertThat(testDepositTransaction.getDoc()).isEqualTo(DEFAULT_DOC);
    }

    @Test
    @Transactional
    void createDepositTransactionWithExistingId() throws Exception {
        // Create the DepositTransaction with an existing ID
        depositTransaction.setId(1L);
        DepositTransactionDTO depositTransactionDTO = depositTransactionMapper.toDto(depositTransaction);

        int databaseSizeBeforeCreate = depositTransactionRepository.findAll().size();

        // An entity with an existing ID cannot be created, so this API call must fail
        restDepositTransactionMockMvc
            .perform(
                post(ENTITY_API_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(depositTransactionDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the DepositTransaction in the database
        List<DepositTransaction> depositTransactionList = depositTransactionRepository.findAll();
        assertThat(depositTransactionList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkAmountIsRequired() throws Exception {
        int databaseSizeBeforeTest = depositTransactionRepository.findAll().size();
        // set the field null
        depositTransaction.setAmount(null);

        // Create the DepositTransaction, which fails.
        DepositTransactionDTO depositTransactionDTO = depositTransactionMapper.toDto(depositTransaction);

        restDepositTransactionMockMvc
            .perform(
                post(ENTITY_API_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(depositTransactionDTO))
            )
            .andExpect(status().isBadRequest());

        List<DepositTransaction> depositTransactionList = depositTransactionRepository.findAll();
        assertThat(depositTransactionList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkUpdateDateIsRequired() throws Exception {
        int databaseSizeBeforeTest = depositTransactionRepository.findAll().size();
        // set the field null
        depositTransaction.setUpdateDate(null);

        // Create the DepositTransaction, which fails.
        DepositTransactionDTO depositTransactionDTO = depositTransactionMapper.toDto(depositTransaction);

        restDepositTransactionMockMvc
            .perform(
                post(ENTITY_API_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(depositTransactionDTO))
            )
            .andExpect(status().isBadRequest());

        List<DepositTransaction> depositTransactionList = depositTransactionRepository.findAll();
        assertThat(depositTransactionList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllDepositTransactions() throws Exception {
        // Initialize the database
        depositTransactionRepository.saveAndFlush(depositTransaction);

        // Get all the depositTransactionList
        restDepositTransactionMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(depositTransaction.getId().intValue())))
            .andExpect(jsonPath("$.[*].payerTaxnr").value(hasItem(DEFAULT_PAYER_TAXNR)))
            .andExpect(jsonPath("$.[*].recipientTaxnr").value(hasItem(DEFAULT_RECIPIENT_TAXNR)))
            .andExpect(jsonPath("$.[*].amount").value(hasItem(DEFAULT_AMOUNT.intValue())))
            .andExpect(jsonPath("$.[*].updateDate").value(hasItem(DEFAULT_UPDATE_DATE.toString())))
            .andExpect(jsonPath("$.[*].comment").value(hasItem(DEFAULT_COMMENT)))
            .andExpect(jsonPath("$.[*].doc").value(hasItem(DEFAULT_DOC)));
    }

    @Test
    @Transactional
    void getDepositTransaction() throws Exception {
        // Initialize the database
        depositTransactionRepository.saveAndFlush(depositTransaction);

        // Get the depositTransaction
        restDepositTransactionMockMvc
            .perform(get(ENTITY_API_URL_ID, depositTransaction.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(depositTransaction.getId().intValue()))
            .andExpect(jsonPath("$.payerTaxnr").value(DEFAULT_PAYER_TAXNR))
            .andExpect(jsonPath("$.recipientTaxnr").value(DEFAULT_RECIPIENT_TAXNR))
            .andExpect(jsonPath("$.amount").value(DEFAULT_AMOUNT.intValue()))
            .andExpect(jsonPath("$.updateDate").value(DEFAULT_UPDATE_DATE.toString()))
            .andExpect(jsonPath("$.comment").value(DEFAULT_COMMENT))
            .andExpect(jsonPath("$.doc").value(DEFAULT_DOC));
    }

    @Test
    @Transactional
    void getDepositTransactionsByIdFiltering() throws Exception {
        // Initialize the database
        depositTransactionRepository.saveAndFlush(depositTransaction);

        Long id = depositTransaction.getId();

        defaultDepositTransactionShouldBeFound("id.equals=" + id);
        defaultDepositTransactionShouldNotBeFound("id.notEquals=" + id);

        defaultDepositTransactionShouldBeFound("id.greaterThanOrEqual=" + id);
        defaultDepositTransactionShouldNotBeFound("id.greaterThan=" + id);

        defaultDepositTransactionShouldBeFound("id.lessThanOrEqual=" + id);
        defaultDepositTransactionShouldNotBeFound("id.lessThan=" + id);
    }

    @Test
    @Transactional
    void getAllDepositTransactionsByPayerTaxnrIsEqualToSomething() throws Exception {
        // Initialize the database
        depositTransactionRepository.saveAndFlush(depositTransaction);

        // Get all the depositTransactionList where payerTaxnr equals to DEFAULT_PAYER_TAXNR
        defaultDepositTransactionShouldBeFound("payerTaxnr.equals=" + DEFAULT_PAYER_TAXNR);

        // Get all the depositTransactionList where payerTaxnr equals to UPDATED_PAYER_TAXNR
        defaultDepositTransactionShouldNotBeFound("payerTaxnr.equals=" + UPDATED_PAYER_TAXNR);
    }

    @Test
    @Transactional
    void getAllDepositTransactionsByPayerTaxnrIsNotEqualToSomething() throws Exception {
        // Initialize the database
        depositTransactionRepository.saveAndFlush(depositTransaction);

        // Get all the depositTransactionList where payerTaxnr not equals to DEFAULT_PAYER_TAXNR
        defaultDepositTransactionShouldNotBeFound("payerTaxnr.notEquals=" + DEFAULT_PAYER_TAXNR);

        // Get all the depositTransactionList where payerTaxnr not equals to UPDATED_PAYER_TAXNR
        defaultDepositTransactionShouldBeFound("payerTaxnr.notEquals=" + UPDATED_PAYER_TAXNR);
    }

    @Test
    @Transactional
    void getAllDepositTransactionsByPayerTaxnrIsInShouldWork() throws Exception {
        // Initialize the database
        depositTransactionRepository.saveAndFlush(depositTransaction);

        // Get all the depositTransactionList where payerTaxnr in DEFAULT_PAYER_TAXNR or UPDATED_PAYER_TAXNR
        defaultDepositTransactionShouldBeFound("payerTaxnr.in=" + DEFAULT_PAYER_TAXNR + "," + UPDATED_PAYER_TAXNR);

        // Get all the depositTransactionList where payerTaxnr equals to UPDATED_PAYER_TAXNR
        defaultDepositTransactionShouldNotBeFound("payerTaxnr.in=" + UPDATED_PAYER_TAXNR);
    }

    @Test
    @Transactional
    void getAllDepositTransactionsByPayerTaxnrIsNullOrNotNull() throws Exception {
        // Initialize the database
        depositTransactionRepository.saveAndFlush(depositTransaction);

        // Get all the depositTransactionList where payerTaxnr is not null
        defaultDepositTransactionShouldBeFound("payerTaxnr.specified=true");

        // Get all the depositTransactionList where payerTaxnr is null
        defaultDepositTransactionShouldNotBeFound("payerTaxnr.specified=false");
    }

    @Test
    @Transactional
    void getAllDepositTransactionsByPayerTaxnrContainsSomething() throws Exception {
        // Initialize the database
        depositTransactionRepository.saveAndFlush(depositTransaction);

        // Get all the depositTransactionList where payerTaxnr contains DEFAULT_PAYER_TAXNR
        defaultDepositTransactionShouldBeFound("payerTaxnr.contains=" + DEFAULT_PAYER_TAXNR);

        // Get all the depositTransactionList where payerTaxnr contains UPDATED_PAYER_TAXNR
        defaultDepositTransactionShouldNotBeFound("payerTaxnr.contains=" + UPDATED_PAYER_TAXNR);
    }

    @Test
    @Transactional
    void getAllDepositTransactionsByPayerTaxnrNotContainsSomething() throws Exception {
        // Initialize the database
        depositTransactionRepository.saveAndFlush(depositTransaction);

        // Get all the depositTransactionList where payerTaxnr does not contain DEFAULT_PAYER_TAXNR
        defaultDepositTransactionShouldNotBeFound("payerTaxnr.doesNotContain=" + DEFAULT_PAYER_TAXNR);

        // Get all the depositTransactionList where payerTaxnr does not contain UPDATED_PAYER_TAXNR
        defaultDepositTransactionShouldBeFound("payerTaxnr.doesNotContain=" + UPDATED_PAYER_TAXNR);
    }

    @Test
    @Transactional
    void getAllDepositTransactionsByRecipientTaxnrIsEqualToSomething() throws Exception {
        // Initialize the database
        depositTransactionRepository.saveAndFlush(depositTransaction);

        // Get all the depositTransactionList where recipientTaxnr equals to DEFAULT_RECIPIENT_TAXNR
        defaultDepositTransactionShouldBeFound("recipientTaxnr.equals=" + DEFAULT_RECIPIENT_TAXNR);

        // Get all the depositTransactionList where recipientTaxnr equals to UPDATED_RECIPIENT_TAXNR
        defaultDepositTransactionShouldNotBeFound("recipientTaxnr.equals=" + UPDATED_RECIPIENT_TAXNR);
    }

    @Test
    @Transactional
    void getAllDepositTransactionsByRecipientTaxnrIsNotEqualToSomething() throws Exception {
        // Initialize the database
        depositTransactionRepository.saveAndFlush(depositTransaction);

        // Get all the depositTransactionList where recipientTaxnr not equals to DEFAULT_RECIPIENT_TAXNR
        defaultDepositTransactionShouldNotBeFound("recipientTaxnr.notEquals=" + DEFAULT_RECIPIENT_TAXNR);

        // Get all the depositTransactionList where recipientTaxnr not equals to UPDATED_RECIPIENT_TAXNR
        defaultDepositTransactionShouldBeFound("recipientTaxnr.notEquals=" + UPDATED_RECIPIENT_TAXNR);
    }

    @Test
    @Transactional
    void getAllDepositTransactionsByRecipientTaxnrIsInShouldWork() throws Exception {
        // Initialize the database
        depositTransactionRepository.saveAndFlush(depositTransaction);

        // Get all the depositTransactionList where recipientTaxnr in DEFAULT_RECIPIENT_TAXNR or UPDATED_RECIPIENT_TAXNR
        defaultDepositTransactionShouldBeFound("recipientTaxnr.in=" + DEFAULT_RECIPIENT_TAXNR + "," + UPDATED_RECIPIENT_TAXNR);

        // Get all the depositTransactionList where recipientTaxnr equals to UPDATED_RECIPIENT_TAXNR
        defaultDepositTransactionShouldNotBeFound("recipientTaxnr.in=" + UPDATED_RECIPIENT_TAXNR);
    }

    @Test
    @Transactional
    void getAllDepositTransactionsByRecipientTaxnrIsNullOrNotNull() throws Exception {
        // Initialize the database
        depositTransactionRepository.saveAndFlush(depositTransaction);

        // Get all the depositTransactionList where recipientTaxnr is not null
        defaultDepositTransactionShouldBeFound("recipientTaxnr.specified=true");

        // Get all the depositTransactionList where recipientTaxnr is null
        defaultDepositTransactionShouldNotBeFound("recipientTaxnr.specified=false");
    }

    @Test
    @Transactional
    void getAllDepositTransactionsByRecipientTaxnrContainsSomething() throws Exception {
        // Initialize the database
        depositTransactionRepository.saveAndFlush(depositTransaction);

        // Get all the depositTransactionList where recipientTaxnr contains DEFAULT_RECIPIENT_TAXNR
        defaultDepositTransactionShouldBeFound("recipientTaxnr.contains=" + DEFAULT_RECIPIENT_TAXNR);

        // Get all the depositTransactionList where recipientTaxnr contains UPDATED_RECIPIENT_TAXNR
        defaultDepositTransactionShouldNotBeFound("recipientTaxnr.contains=" + UPDATED_RECIPIENT_TAXNR);
    }

    @Test
    @Transactional
    void getAllDepositTransactionsByRecipientTaxnrNotContainsSomething() throws Exception {
        // Initialize the database
        depositTransactionRepository.saveAndFlush(depositTransaction);

        // Get all the depositTransactionList where recipientTaxnr does not contain DEFAULT_RECIPIENT_TAXNR
        defaultDepositTransactionShouldNotBeFound("recipientTaxnr.doesNotContain=" + DEFAULT_RECIPIENT_TAXNR);

        // Get all the depositTransactionList where recipientTaxnr does not contain UPDATED_RECIPIENT_TAXNR
        defaultDepositTransactionShouldBeFound("recipientTaxnr.doesNotContain=" + UPDATED_RECIPIENT_TAXNR);
    }

    @Test
    @Transactional
    void getAllDepositTransactionsByAmountIsEqualToSomething() throws Exception {
        // Initialize the database
        depositTransactionRepository.saveAndFlush(depositTransaction);

        // Get all the depositTransactionList where amount equals to DEFAULT_AMOUNT
        defaultDepositTransactionShouldBeFound("amount.equals=" + DEFAULT_AMOUNT);

        // Get all the depositTransactionList where amount equals to UPDATED_AMOUNT
        defaultDepositTransactionShouldNotBeFound("amount.equals=" + UPDATED_AMOUNT);
    }

    @Test
    @Transactional
    void getAllDepositTransactionsByAmountIsNotEqualToSomething() throws Exception {
        // Initialize the database
        depositTransactionRepository.saveAndFlush(depositTransaction);

        // Get all the depositTransactionList where amount not equals to DEFAULT_AMOUNT
        defaultDepositTransactionShouldNotBeFound("amount.notEquals=" + DEFAULT_AMOUNT);

        // Get all the depositTransactionList where amount not equals to UPDATED_AMOUNT
        defaultDepositTransactionShouldBeFound("amount.notEquals=" + UPDATED_AMOUNT);
    }

    @Test
    @Transactional
    void getAllDepositTransactionsByAmountIsInShouldWork() throws Exception {
        // Initialize the database
        depositTransactionRepository.saveAndFlush(depositTransaction);

        // Get all the depositTransactionList where amount in DEFAULT_AMOUNT or UPDATED_AMOUNT
        defaultDepositTransactionShouldBeFound("amount.in=" + DEFAULT_AMOUNT + "," + UPDATED_AMOUNT);

        // Get all the depositTransactionList where amount equals to UPDATED_AMOUNT
        defaultDepositTransactionShouldNotBeFound("amount.in=" + UPDATED_AMOUNT);
    }

    @Test
    @Transactional
    void getAllDepositTransactionsByAmountIsNullOrNotNull() throws Exception {
        // Initialize the database
        depositTransactionRepository.saveAndFlush(depositTransaction);

        // Get all the depositTransactionList where amount is not null
        defaultDepositTransactionShouldBeFound("amount.specified=true");

        // Get all the depositTransactionList where amount is null
        defaultDepositTransactionShouldNotBeFound("amount.specified=false");
    }

    @Test
    @Transactional
    void getAllDepositTransactionsByAmountIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        depositTransactionRepository.saveAndFlush(depositTransaction);

        // Get all the depositTransactionList where amount is greater than or equal to DEFAULT_AMOUNT
        defaultDepositTransactionShouldBeFound("amount.greaterThanOrEqual=" + DEFAULT_AMOUNT);

        // Get all the depositTransactionList where amount is greater than or equal to UPDATED_AMOUNT
        defaultDepositTransactionShouldNotBeFound("amount.greaterThanOrEqual=" + UPDATED_AMOUNT);
    }

    @Test
    @Transactional
    void getAllDepositTransactionsByAmountIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        depositTransactionRepository.saveAndFlush(depositTransaction);

        // Get all the depositTransactionList where amount is less than or equal to DEFAULT_AMOUNT
        defaultDepositTransactionShouldBeFound("amount.lessThanOrEqual=" + DEFAULT_AMOUNT);

        // Get all the depositTransactionList where amount is less than or equal to SMALLER_AMOUNT
        defaultDepositTransactionShouldNotBeFound("amount.lessThanOrEqual=" + SMALLER_AMOUNT);
    }

    @Test
    @Transactional
    void getAllDepositTransactionsByAmountIsLessThanSomething() throws Exception {
        // Initialize the database
        depositTransactionRepository.saveAndFlush(depositTransaction);

        // Get all the depositTransactionList where amount is less than DEFAULT_AMOUNT
        defaultDepositTransactionShouldNotBeFound("amount.lessThan=" + DEFAULT_AMOUNT);

        // Get all the depositTransactionList where amount is less than UPDATED_AMOUNT
        defaultDepositTransactionShouldBeFound("amount.lessThan=" + UPDATED_AMOUNT);
    }

    @Test
    @Transactional
    void getAllDepositTransactionsByAmountIsGreaterThanSomething() throws Exception {
        // Initialize the database
        depositTransactionRepository.saveAndFlush(depositTransaction);

        // Get all the depositTransactionList where amount is greater than DEFAULT_AMOUNT
        defaultDepositTransactionShouldNotBeFound("amount.greaterThan=" + DEFAULT_AMOUNT);

        // Get all the depositTransactionList where amount is greater than SMALLER_AMOUNT
        defaultDepositTransactionShouldBeFound("amount.greaterThan=" + SMALLER_AMOUNT);
    }

    @Test
    @Transactional
    void getAllDepositTransactionsByUpdateDateIsEqualToSomething() throws Exception {
        // Initialize the database
        depositTransactionRepository.saveAndFlush(depositTransaction);

        // Get all the depositTransactionList where updateDate equals to DEFAULT_UPDATE_DATE
        defaultDepositTransactionShouldBeFound("updateDate.equals=" + DEFAULT_UPDATE_DATE);

        // Get all the depositTransactionList where updateDate equals to UPDATED_UPDATE_DATE
        defaultDepositTransactionShouldNotBeFound("updateDate.equals=" + UPDATED_UPDATE_DATE);
    }

    @Test
    @Transactional
    void getAllDepositTransactionsByUpdateDateIsNotEqualToSomething() throws Exception {
        // Initialize the database
        depositTransactionRepository.saveAndFlush(depositTransaction);

        // Get all the depositTransactionList where updateDate not equals to DEFAULT_UPDATE_DATE
        defaultDepositTransactionShouldNotBeFound("updateDate.notEquals=" + DEFAULT_UPDATE_DATE);

        // Get all the depositTransactionList where updateDate not equals to UPDATED_UPDATE_DATE
        defaultDepositTransactionShouldBeFound("updateDate.notEquals=" + UPDATED_UPDATE_DATE);
    }

    @Test
    @Transactional
    void getAllDepositTransactionsByUpdateDateIsInShouldWork() throws Exception {
        // Initialize the database
        depositTransactionRepository.saveAndFlush(depositTransaction);

        // Get all the depositTransactionList where updateDate in DEFAULT_UPDATE_DATE or UPDATED_UPDATE_DATE
        defaultDepositTransactionShouldBeFound("updateDate.in=" + DEFAULT_UPDATE_DATE + "," + UPDATED_UPDATE_DATE);

        // Get all the depositTransactionList where updateDate equals to UPDATED_UPDATE_DATE
        defaultDepositTransactionShouldNotBeFound("updateDate.in=" + UPDATED_UPDATE_DATE);
    }

    @Test
    @Transactional
    void getAllDepositTransactionsByUpdateDateIsNullOrNotNull() throws Exception {
        // Initialize the database
        depositTransactionRepository.saveAndFlush(depositTransaction);

        // Get all the depositTransactionList where updateDate is not null
        defaultDepositTransactionShouldBeFound("updateDate.specified=true");

        // Get all the depositTransactionList where updateDate is null
        defaultDepositTransactionShouldNotBeFound("updateDate.specified=false");
    }

    @Test
    @Transactional
    void getAllDepositTransactionsByCommentIsEqualToSomething() throws Exception {
        // Initialize the database
        depositTransactionRepository.saveAndFlush(depositTransaction);

        // Get all the depositTransactionList where comment equals to DEFAULT_COMMENT
        defaultDepositTransactionShouldBeFound("comment.equals=" + DEFAULT_COMMENT);

        // Get all the depositTransactionList where comment equals to UPDATED_COMMENT
        defaultDepositTransactionShouldNotBeFound("comment.equals=" + UPDATED_COMMENT);
    }

    @Test
    @Transactional
    void getAllDepositTransactionsByCommentIsNotEqualToSomething() throws Exception {
        // Initialize the database
        depositTransactionRepository.saveAndFlush(depositTransaction);

        // Get all the depositTransactionList where comment not equals to DEFAULT_COMMENT
        defaultDepositTransactionShouldNotBeFound("comment.notEquals=" + DEFAULT_COMMENT);

        // Get all the depositTransactionList where comment not equals to UPDATED_COMMENT
        defaultDepositTransactionShouldBeFound("comment.notEquals=" + UPDATED_COMMENT);
    }

    @Test
    @Transactional
    void getAllDepositTransactionsByCommentIsInShouldWork() throws Exception {
        // Initialize the database
        depositTransactionRepository.saveAndFlush(depositTransaction);

        // Get all the depositTransactionList where comment in DEFAULT_COMMENT or UPDATED_COMMENT
        defaultDepositTransactionShouldBeFound("comment.in=" + DEFAULT_COMMENT + "," + UPDATED_COMMENT);

        // Get all the depositTransactionList where comment equals to UPDATED_COMMENT
        defaultDepositTransactionShouldNotBeFound("comment.in=" + UPDATED_COMMENT);
    }

    @Test
    @Transactional
    void getAllDepositTransactionsByCommentIsNullOrNotNull() throws Exception {
        // Initialize the database
        depositTransactionRepository.saveAndFlush(depositTransaction);

        // Get all the depositTransactionList where comment is not null
        defaultDepositTransactionShouldBeFound("comment.specified=true");

        // Get all the depositTransactionList where comment is null
        defaultDepositTransactionShouldNotBeFound("comment.specified=false");
    }

    @Test
    @Transactional
    void getAllDepositTransactionsByCommentContainsSomething() throws Exception {
        // Initialize the database
        depositTransactionRepository.saveAndFlush(depositTransaction);

        // Get all the depositTransactionList where comment contains DEFAULT_COMMENT
        defaultDepositTransactionShouldBeFound("comment.contains=" + DEFAULT_COMMENT);

        // Get all the depositTransactionList where comment contains UPDATED_COMMENT
        defaultDepositTransactionShouldNotBeFound("comment.contains=" + UPDATED_COMMENT);
    }

    @Test
    @Transactional
    void getAllDepositTransactionsByCommentNotContainsSomething() throws Exception {
        // Initialize the database
        depositTransactionRepository.saveAndFlush(depositTransaction);

        // Get all the depositTransactionList where comment does not contain DEFAULT_COMMENT
        defaultDepositTransactionShouldNotBeFound("comment.doesNotContain=" + DEFAULT_COMMENT);

        // Get all the depositTransactionList where comment does not contain UPDATED_COMMENT
        defaultDepositTransactionShouldBeFound("comment.doesNotContain=" + UPDATED_COMMENT);
    }

    @Test
    @Transactional
    void getAllDepositTransactionsByDocIsEqualToSomething() throws Exception {
        // Initialize the database
        depositTransactionRepository.saveAndFlush(depositTransaction);

        // Get all the depositTransactionList where doc equals to DEFAULT_DOC
        defaultDepositTransactionShouldBeFound("doc.equals=" + DEFAULT_DOC);

        // Get all the depositTransactionList where doc equals to UPDATED_DOC
        defaultDepositTransactionShouldNotBeFound("doc.equals=" + UPDATED_DOC);
    }

    @Test
    @Transactional
    void getAllDepositTransactionsByDocIsNotEqualToSomething() throws Exception {
        // Initialize the database
        depositTransactionRepository.saveAndFlush(depositTransaction);

        // Get all the depositTransactionList where doc not equals to DEFAULT_DOC
        defaultDepositTransactionShouldNotBeFound("doc.notEquals=" + DEFAULT_DOC);

        // Get all the depositTransactionList where doc not equals to UPDATED_DOC
        defaultDepositTransactionShouldBeFound("doc.notEquals=" + UPDATED_DOC);
    }

    @Test
    @Transactional
    void getAllDepositTransactionsByDocIsInShouldWork() throws Exception {
        // Initialize the database
        depositTransactionRepository.saveAndFlush(depositTransaction);

        // Get all the depositTransactionList where doc in DEFAULT_DOC or UPDATED_DOC
        defaultDepositTransactionShouldBeFound("doc.in=" + DEFAULT_DOC + "," + UPDATED_DOC);

        // Get all the depositTransactionList where doc equals to UPDATED_DOC
        defaultDepositTransactionShouldNotBeFound("doc.in=" + UPDATED_DOC);
    }

    @Test
    @Transactional
    void getAllDepositTransactionsByDocIsNullOrNotNull() throws Exception {
        // Initialize the database
        depositTransactionRepository.saveAndFlush(depositTransaction);

        // Get all the depositTransactionList where doc is not null
        defaultDepositTransactionShouldBeFound("doc.specified=true");

        // Get all the depositTransactionList where doc is null
        defaultDepositTransactionShouldNotBeFound("doc.specified=false");
    }

    @Test
    @Transactional
    void getAllDepositTransactionsByDocContainsSomething() throws Exception {
        // Initialize the database
        depositTransactionRepository.saveAndFlush(depositTransaction);

        // Get all the depositTransactionList where doc contains DEFAULT_DOC
        defaultDepositTransactionShouldBeFound("doc.contains=" + DEFAULT_DOC);

        // Get all the depositTransactionList where doc contains UPDATED_DOC
        defaultDepositTransactionShouldNotBeFound("doc.contains=" + UPDATED_DOC);
    }

    @Test
    @Transactional
    void getAllDepositTransactionsByDocNotContainsSomething() throws Exception {
        // Initialize the database
        depositTransactionRepository.saveAndFlush(depositTransaction);

        // Get all the depositTransactionList where doc does not contain DEFAULT_DOC
        defaultDepositTransactionShouldNotBeFound("doc.doesNotContain=" + DEFAULT_DOC);

        // Get all the depositTransactionList where doc does not contain UPDATED_DOC
        defaultDepositTransactionShouldBeFound("doc.doesNotContain=" + UPDATED_DOC);
    }

    /**
     * Executes the search, and checks that the default entity is returned.
     */
    private void defaultDepositTransactionShouldBeFound(String filter) throws Exception {
        restDepositTransactionMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(depositTransaction.getId().intValue())))
            .andExpect(jsonPath("$.[*].payerTaxnr").value(hasItem(DEFAULT_PAYER_TAXNR)))
            .andExpect(jsonPath("$.[*].recipientTaxnr").value(hasItem(DEFAULT_RECIPIENT_TAXNR)))
            .andExpect(jsonPath("$.[*].amount").value(hasItem(DEFAULT_AMOUNT.intValue())))
            .andExpect(jsonPath("$.[*].updateDate").value(hasItem(DEFAULT_UPDATE_DATE.toString())))
            .andExpect(jsonPath("$.[*].comment").value(hasItem(DEFAULT_COMMENT)))
            .andExpect(jsonPath("$.[*].doc").value(hasItem(DEFAULT_DOC)));

        // Check, that the count call also returns 1
        restDepositTransactionMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("1"));
    }

    /**
     * Executes the search, and checks that the default entity is not returned.
     */
    private void defaultDepositTransactionShouldNotBeFound(String filter) throws Exception {
        restDepositTransactionMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty());

        // Check, that the count call also returns 0
        restDepositTransactionMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("0"));
    }

    @Test
    @Transactional
    void getNonExistingDepositTransaction() throws Exception {
        // Get the depositTransaction
        restDepositTransactionMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putNewDepositTransaction() throws Exception {
        // Initialize the database
        depositTransactionRepository.saveAndFlush(depositTransaction);

        int databaseSizeBeforeUpdate = depositTransactionRepository.findAll().size();

        // Update the depositTransaction
        DepositTransaction updatedDepositTransaction = depositTransactionRepository.findById(depositTransaction.getId()).get();
        // Disconnect from session so that the updates on updatedDepositTransaction are not directly saved in db
        em.detach(updatedDepositTransaction);
        updatedDepositTransaction
            .payerTaxnr(UPDATED_PAYER_TAXNR)
            .recipientTaxnr(UPDATED_RECIPIENT_TAXNR)
            .amount(UPDATED_AMOUNT)
            .updateDate(UPDATED_UPDATE_DATE)
            .comment(UPDATED_COMMENT)
            .doc(UPDATED_DOC);
        DepositTransactionDTO depositTransactionDTO = depositTransactionMapper.toDto(updatedDepositTransaction);

        restDepositTransactionMockMvc
            .perform(
                put(ENTITY_API_URL_ID, depositTransactionDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(depositTransactionDTO))
            )
            .andExpect(status().isOk());

        // Validate the DepositTransaction in the database
        List<DepositTransaction> depositTransactionList = depositTransactionRepository.findAll();
        assertThat(depositTransactionList).hasSize(databaseSizeBeforeUpdate);
        DepositTransaction testDepositTransaction = depositTransactionList.get(depositTransactionList.size() - 1);
        assertThat(testDepositTransaction.getPayerTaxnr()).isEqualTo(UPDATED_PAYER_TAXNR);
        assertThat(testDepositTransaction.getRecipientTaxnr()).isEqualTo(UPDATED_RECIPIENT_TAXNR);
        assertThat(testDepositTransaction.getAmount()).isEqualTo(UPDATED_AMOUNT);
        assertThat(testDepositTransaction.getUpdateDate()).isEqualTo(UPDATED_UPDATE_DATE);
        assertThat(testDepositTransaction.getComment()).isEqualTo(UPDATED_COMMENT);
        assertThat(testDepositTransaction.getDoc()).isEqualTo(UPDATED_DOC);
    }

    @Test
    @Transactional
    void putNonExistingDepositTransaction() throws Exception {
        int databaseSizeBeforeUpdate = depositTransactionRepository.findAll().size();
        depositTransaction.setId(count.incrementAndGet());

        // Create the DepositTransaction
        DepositTransactionDTO depositTransactionDTO = depositTransactionMapper.toDto(depositTransaction);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restDepositTransactionMockMvc
            .perform(
                put(ENTITY_API_URL_ID, depositTransactionDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(depositTransactionDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the DepositTransaction in the database
        List<DepositTransaction> depositTransactionList = depositTransactionRepository.findAll();
        assertThat(depositTransactionList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchDepositTransaction() throws Exception {
        int databaseSizeBeforeUpdate = depositTransactionRepository.findAll().size();
        depositTransaction.setId(count.incrementAndGet());

        // Create the DepositTransaction
        DepositTransactionDTO depositTransactionDTO = depositTransactionMapper.toDto(depositTransaction);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restDepositTransactionMockMvc
            .perform(
                put(ENTITY_API_URL_ID, count.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(depositTransactionDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the DepositTransaction in the database
        List<DepositTransaction> depositTransactionList = depositTransactionRepository.findAll();
        assertThat(depositTransactionList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamDepositTransaction() throws Exception {
        int databaseSizeBeforeUpdate = depositTransactionRepository.findAll().size();
        depositTransaction.setId(count.incrementAndGet());

        // Create the DepositTransaction
        DepositTransactionDTO depositTransactionDTO = depositTransactionMapper.toDto(depositTransaction);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restDepositTransactionMockMvc
            .perform(
                put(ENTITY_API_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(depositTransactionDTO))
            )
            .andExpect(status().isMethodNotAllowed());

        // Validate the DepositTransaction in the database
        List<DepositTransaction> depositTransactionList = depositTransactionRepository.findAll();
        assertThat(depositTransactionList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateDepositTransactionWithPatch() throws Exception {
        // Initialize the database
        depositTransactionRepository.saveAndFlush(depositTransaction);

        int databaseSizeBeforeUpdate = depositTransactionRepository.findAll().size();

        // Update the depositTransaction using partial update
        DepositTransaction partialUpdatedDepositTransaction = new DepositTransaction();
        partialUpdatedDepositTransaction.setId(depositTransaction.getId());

        partialUpdatedDepositTransaction.recipientTaxnr(UPDATED_RECIPIENT_TAXNR);

        restDepositTransactionMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedDepositTransaction.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedDepositTransaction))
            )
            .andExpect(status().isOk());

        // Validate the DepositTransaction in the database
        List<DepositTransaction> depositTransactionList = depositTransactionRepository.findAll();
        assertThat(depositTransactionList).hasSize(databaseSizeBeforeUpdate);
        DepositTransaction testDepositTransaction = depositTransactionList.get(depositTransactionList.size() - 1);
        assertThat(testDepositTransaction.getPayerTaxnr()).isEqualTo(DEFAULT_PAYER_TAXNR);
        assertThat(testDepositTransaction.getRecipientTaxnr()).isEqualTo(UPDATED_RECIPIENT_TAXNR);
        assertThat(testDepositTransaction.getAmount()).isEqualTo(DEFAULT_AMOUNT);
        assertThat(testDepositTransaction.getUpdateDate()).isEqualTo(DEFAULT_UPDATE_DATE);
        assertThat(testDepositTransaction.getComment()).isEqualTo(DEFAULT_COMMENT);
        assertThat(testDepositTransaction.getDoc()).isEqualTo(DEFAULT_DOC);
    }

    @Test
    @Transactional
    void fullUpdateDepositTransactionWithPatch() throws Exception {
        // Initialize the database
        depositTransactionRepository.saveAndFlush(depositTransaction);

        int databaseSizeBeforeUpdate = depositTransactionRepository.findAll().size();

        // Update the depositTransaction using partial update
        DepositTransaction partialUpdatedDepositTransaction = new DepositTransaction();
        partialUpdatedDepositTransaction.setId(depositTransaction.getId());

        partialUpdatedDepositTransaction
            .payerTaxnr(UPDATED_PAYER_TAXNR)
            .recipientTaxnr(UPDATED_RECIPIENT_TAXNR)
            .amount(UPDATED_AMOUNT)
            .updateDate(UPDATED_UPDATE_DATE)
            .comment(UPDATED_COMMENT)
            .doc(UPDATED_DOC);

        restDepositTransactionMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedDepositTransaction.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedDepositTransaction))
            )
            .andExpect(status().isOk());

        // Validate the DepositTransaction in the database
        List<DepositTransaction> depositTransactionList = depositTransactionRepository.findAll();
        assertThat(depositTransactionList).hasSize(databaseSizeBeforeUpdate);
        DepositTransaction testDepositTransaction = depositTransactionList.get(depositTransactionList.size() - 1);
        assertThat(testDepositTransaction.getPayerTaxnr()).isEqualTo(UPDATED_PAYER_TAXNR);
        assertThat(testDepositTransaction.getRecipientTaxnr()).isEqualTo(UPDATED_RECIPIENT_TAXNR);
        assertThat(testDepositTransaction.getAmount()).isEqualTo(UPDATED_AMOUNT);
        assertThat(testDepositTransaction.getUpdateDate()).isEqualTo(UPDATED_UPDATE_DATE);
        assertThat(testDepositTransaction.getComment()).isEqualTo(UPDATED_COMMENT);
        assertThat(testDepositTransaction.getDoc()).isEqualTo(UPDATED_DOC);
    }

    @Test
    @Transactional
    void patchNonExistingDepositTransaction() throws Exception {
        int databaseSizeBeforeUpdate = depositTransactionRepository.findAll().size();
        depositTransaction.setId(count.incrementAndGet());

        // Create the DepositTransaction
        DepositTransactionDTO depositTransactionDTO = depositTransactionMapper.toDto(depositTransaction);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restDepositTransactionMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, depositTransactionDTO.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(depositTransactionDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the DepositTransaction in the database
        List<DepositTransaction> depositTransactionList = depositTransactionRepository.findAll();
        assertThat(depositTransactionList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchDepositTransaction() throws Exception {
        int databaseSizeBeforeUpdate = depositTransactionRepository.findAll().size();
        depositTransaction.setId(count.incrementAndGet());

        // Create the DepositTransaction
        DepositTransactionDTO depositTransactionDTO = depositTransactionMapper.toDto(depositTransaction);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restDepositTransactionMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, count.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(depositTransactionDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the DepositTransaction in the database
        List<DepositTransaction> depositTransactionList = depositTransactionRepository.findAll();
        assertThat(depositTransactionList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamDepositTransaction() throws Exception {
        int databaseSizeBeforeUpdate = depositTransactionRepository.findAll().size();
        depositTransaction.setId(count.incrementAndGet());

        // Create the DepositTransaction
        DepositTransactionDTO depositTransactionDTO = depositTransactionMapper.toDto(depositTransaction);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restDepositTransactionMockMvc
            .perform(
                patch(ENTITY_API_URL)
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(depositTransactionDTO))
            )
            .andExpect(status().isMethodNotAllowed());

        // Validate the DepositTransaction in the database
        List<DepositTransaction> depositTransactionList = depositTransactionRepository.findAll();
        assertThat(depositTransactionList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteDepositTransaction() throws Exception {
        // Initialize the database
        depositTransactionRepository.saveAndFlush(depositTransaction);

        int databaseSizeBeforeDelete = depositTransactionRepository.findAll().size();

        // Delete the depositTransaction
        restDepositTransactionMockMvc
            .perform(delete(ENTITY_API_URL_ID, depositTransaction.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        List<DepositTransaction> depositTransactionList = depositTransactionRepository.findAll();
        assertThat(depositTransactionList).hasSize(databaseSizeBeforeDelete - 1);
    }
}
