package uz.paykap.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static uz.paykap.web.rest.TestUtil.createFormattingConversionService;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
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
import org.springframework.validation.Validator;
import uz.paykap.PkbillmsApp;
import uz.paykap.IntegrationTest;
import uz.paykap.domain.DepositTransaction;
import uz.paykap.repository.DepositTransactionRepository;
import uz.paykap.service.DepositTransactionQueryService;
import uz.paykap.service.DepositTransactionService;
import uz.paykap.service.dto.DepositTransactionDTO;
import uz.paykap.service.mapper.DepositTransactionMapper;
import uz.paykap.web.rest.errors.ExceptionTranslator;

/**
 * Test class for the DepositTransactionResource REST controller.
 *
 * @see DepositTransactionResource
 */
@RunWith(SpringRunner.class)
@IntegrationTest
public class DepositTransactionResourceIntTest {

    private static final String DEFAULT_PAYER_TAXNR = "AAAAAAAAAA";
    private static final String UPDATED_PAYER_TAXNR = "BBBBBBBBBB";

    private static final String DEFAULT_RECIPIENT_TAXNR = "AAAAAAAAAA";
    private static final String UPDATED_RECIPIENT_TAXNR = "BBBBBBBBBB";

    private static final Long DEFAULT_AMOUNT = 1L;
    private static final Long UPDATED_AMOUNT = 2L;

    private static final Instant DEFAULT_UPDATE_DATE = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_UPDATE_DATE = Instant.now().truncatedTo(ChronoUnit.MILLIS);

    private static final String DEFAULT_COMMENT = "AAAAAAAAAA";
    private static final String UPDATED_COMMENT = "BBBBBBBBBB";

    private static final String DEFAULT_DOC = "AAAAAAAAAA";
    private static final String UPDATED_DOC = "BBBBBBBBBB";

    private static final String DEFAULT_AGENT_LOGIN = "AAAAAAAAAA";
    private static final String UPDATED_AGENT_LOGIN = "BBBBBBBBBB";

    @Autowired
    private DepositTransactionRepository depositTransactionRepository;

    @Autowired
    private DepositTransactionMapper depositTransactionMapper;

    @Autowired
    private DepositTransactionService depositTransactionService;

    @Autowired
    private DepositTransactionQueryService depositTransactionQueryService;

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

    private MockMvc restDepositTransactionMockMvc;

    private DepositTransaction depositTransaction;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        final DepositTransactionResource depositTransactionResource = new DepositTransactionResource(
            depositTransactionService,
            depositTransactionRepository,
            depositTransactionQueryService
        );
        this.restDepositTransactionMockMvc =
            MockMvcBuilders
                .standaloneSetup(depositTransactionResource)
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
    public static DepositTransaction createEntity(EntityManager em) {
        DepositTransaction depositTransaction = new DepositTransaction()
            .payerTaxnr(DEFAULT_PAYER_TAXNR)
            .recipientTaxnr(DEFAULT_RECIPIENT_TAXNR)
            .amount(DEFAULT_AMOUNT)
            .updateDate(DEFAULT_UPDATE_DATE)
            .comment(DEFAULT_COMMENT)
            .doc(DEFAULT_DOC)
            .agentLogin(DEFAULT_AGENT_LOGIN);
        return depositTransaction;
    }

    @Before
    public void initTest() {
        depositTransaction = createEntity(em);
    }

    @Test
    @Transactional
    public void createDepositTransaction() throws Exception {
        int databaseSizeBeforeCreate = depositTransactionRepository.findAll().size();

        // Create the DepositTransaction
        DepositTransactionDTO depositTransactionDTO = depositTransactionMapper.toDto(depositTransaction);
        restDepositTransactionMockMvc
            .perform(
                post("/api/deposit-transactions")
                    .contentType(TestUtil.APPLICATION_JSON_UTF8)
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
        assertThat(testDepositTransaction.getAgentLogin()).isEqualTo(DEFAULT_AGENT_LOGIN);
    }

    @Test
    @Transactional
    public void createDepositTransactionWithExistingId() throws Exception {
        int databaseSizeBeforeCreate = depositTransactionRepository.findAll().size();

        // Create the DepositTransaction with an existing ID
        depositTransaction.setId(1L);
        DepositTransactionDTO depositTransactionDTO = depositTransactionMapper.toDto(depositTransaction);

        // An entity with an existing ID cannot be created, so this API call must fail
        restDepositTransactionMockMvc
            .perform(
                post("/api/deposit-transactions")
                    .contentType(TestUtil.APPLICATION_JSON_UTF8)
                    .content(TestUtil.convertObjectToJsonBytes(depositTransactionDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the DepositTransaction in the database
        List<DepositTransaction> depositTransactionList = depositTransactionRepository.findAll();
        assertThat(depositTransactionList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    public void checkAmountIsRequired() throws Exception {
        int databaseSizeBeforeTest = depositTransactionRepository.findAll().size();
        // set the field null
        depositTransaction.setAmount(null);

        // Create the DepositTransaction, which fails.
        DepositTransactionDTO depositTransactionDTO = depositTransactionMapper.toDto(depositTransaction);

        restDepositTransactionMockMvc
            .perform(
                post("/api/deposit-transactions")
                    .contentType(TestUtil.APPLICATION_JSON_UTF8)
                    .content(TestUtil.convertObjectToJsonBytes(depositTransactionDTO))
            )
            .andExpect(status().isBadRequest());

        List<DepositTransaction> depositTransactionList = depositTransactionRepository.findAll();
        assertThat(depositTransactionList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkUpdateDateIsRequired() throws Exception {
        int databaseSizeBeforeTest = depositTransactionRepository.findAll().size();
        // set the field null
        depositTransaction.setUpdateDate(null);

        // Create the DepositTransaction, which fails.
        DepositTransactionDTO depositTransactionDTO = depositTransactionMapper.toDto(depositTransaction);

        restDepositTransactionMockMvc
            .perform(
                post("/api/deposit-transactions")
                    .contentType(TestUtil.APPLICATION_JSON_UTF8)
                    .content(TestUtil.convertObjectToJsonBytes(depositTransactionDTO))
            )
            .andExpect(status().isBadRequest());

        List<DepositTransaction> depositTransactionList = depositTransactionRepository.findAll();
        assertThat(depositTransactionList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void getAllDepositTransactions() throws Exception {
        // Initialize the database
        depositTransactionRepository.saveAndFlush(depositTransaction);

        // Get all the depositTransactionList
        restDepositTransactionMockMvc
            .perform(get("/api/deposit-transactions?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(depositTransaction.getId().intValue())))
            .andExpect(jsonPath("$.[*].payerTaxnr").value(hasItem(DEFAULT_PAYER_TAXNR.toString())))
            .andExpect(jsonPath("$.[*].recipientTaxnr").value(hasItem(DEFAULT_RECIPIENT_TAXNR.toString())))
            .andExpect(jsonPath("$.[*].amount").value(hasItem(DEFAULT_AMOUNT.intValue())))
            .andExpect(jsonPath("$.[*].updateDate").value(hasItem(DEFAULT_UPDATE_DATE.toString())))
            .andExpect(jsonPath("$.[*].comment").value(hasItem(DEFAULT_COMMENT.toString())))
            .andExpect(jsonPath("$.[*].doc").value(hasItem(DEFAULT_DOC.toString())))
            .andExpect(jsonPath("$.[*].agentLogin").value(hasItem(DEFAULT_AGENT_LOGIN.toString())));
    }

    @Test
    @Transactional
    public void getDepositTransaction() throws Exception {
        // Initialize the database
        depositTransactionRepository.saveAndFlush(depositTransaction);

        // Get the depositTransaction
        restDepositTransactionMockMvc
            .perform(get("/api/deposit-transactions/{id}", depositTransaction.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.id").value(depositTransaction.getId().intValue()))
            .andExpect(jsonPath("$.payerTaxnr").value(DEFAULT_PAYER_TAXNR.toString()))
            .andExpect(jsonPath("$.recipientTaxnr").value(DEFAULT_RECIPIENT_TAXNR.toString()))
            .andExpect(jsonPath("$.amount").value(DEFAULT_AMOUNT.intValue()))
            .andExpect(jsonPath("$.updateDate").value(DEFAULT_UPDATE_DATE.toString()))
            .andExpect(jsonPath("$.comment").value(DEFAULT_COMMENT.toString()))
            .andExpect(jsonPath("$.doc").value(DEFAULT_DOC.toString()))
            .andExpect(jsonPath("$.agentLogin").value(DEFAULT_AGENT_LOGIN.toString()));
    }

    @Test
    @Transactional
    public void getAllDepositTransactionsByPayerTaxnrIsEqualToSomething() throws Exception {
        // Initialize the database
        depositTransactionRepository.saveAndFlush(depositTransaction);

        // Get all the depositTransactionList where payerTaxnr equals to DEFAULT_PAYER_TAXNR
        defaultDepositTransactionShouldBeFound("payerTaxnr.equals=" + DEFAULT_PAYER_TAXNR);

        // Get all the depositTransactionList where payerTaxnr equals to UPDATED_PAYER_TAXNR
        defaultDepositTransactionShouldNotBeFound("payerTaxnr.equals=" + UPDATED_PAYER_TAXNR);
    }

    @Test
    @Transactional
    public void getAllDepositTransactionsByPayerTaxnrIsInShouldWork() throws Exception {
        // Initialize the database
        depositTransactionRepository.saveAndFlush(depositTransaction);

        // Get all the depositTransactionList where payerTaxnr in DEFAULT_PAYER_TAXNR or UPDATED_PAYER_TAXNR
        defaultDepositTransactionShouldBeFound("payerTaxnr.in=" + DEFAULT_PAYER_TAXNR + "," + UPDATED_PAYER_TAXNR);

        // Get all the depositTransactionList where payerTaxnr equals to UPDATED_PAYER_TAXNR
        defaultDepositTransactionShouldNotBeFound("payerTaxnr.in=" + UPDATED_PAYER_TAXNR);
    }

    @Test
    @Transactional
    public void getAllDepositTransactionsByPayerTaxnrIsNullOrNotNull() throws Exception {
        // Initialize the database
        depositTransactionRepository.saveAndFlush(depositTransaction);

        // Get all the depositTransactionList where payerTaxnr is not null
        defaultDepositTransactionShouldBeFound("payerTaxnr.specified=true");

        // Get all the depositTransactionList where payerTaxnr is null
        defaultDepositTransactionShouldNotBeFound("payerTaxnr.specified=false");
    }

    @Test
    @Transactional
    public void getAllDepositTransactionsByRecipientTaxnrIsEqualToSomething() throws Exception {
        // Initialize the database
        depositTransactionRepository.saveAndFlush(depositTransaction);

        // Get all the depositTransactionList where recipientTaxnr equals to DEFAULT_RECIPIENT_TAXNR
        defaultDepositTransactionShouldBeFound("recipientTaxnr.equals=" + DEFAULT_RECIPIENT_TAXNR);

        // Get all the depositTransactionList where recipientTaxnr equals to UPDATED_RECIPIENT_TAXNR
        defaultDepositTransactionShouldNotBeFound("recipientTaxnr.equals=" + UPDATED_RECIPIENT_TAXNR);
    }

    @Test
    @Transactional
    public void getAllDepositTransactionsByRecipientTaxnrIsInShouldWork() throws Exception {
        // Initialize the database
        depositTransactionRepository.saveAndFlush(depositTransaction);

        // Get all the depositTransactionList where recipientTaxnr in DEFAULT_RECIPIENT_TAXNR or UPDATED_RECIPIENT_TAXNR
        defaultDepositTransactionShouldBeFound("recipientTaxnr.in=" + DEFAULT_RECIPIENT_TAXNR + "," + UPDATED_RECIPIENT_TAXNR);

        // Get all the depositTransactionList where recipientTaxnr equals to UPDATED_RECIPIENT_TAXNR
        defaultDepositTransactionShouldNotBeFound("recipientTaxnr.in=" + UPDATED_RECIPIENT_TAXNR);
    }

    @Test
    @Transactional
    public void getAllDepositTransactionsByRecipientTaxnrIsNullOrNotNull() throws Exception {
        // Initialize the database
        depositTransactionRepository.saveAndFlush(depositTransaction);

        // Get all the depositTransactionList where recipientTaxnr is not null
        defaultDepositTransactionShouldBeFound("recipientTaxnr.specified=true");

        // Get all the depositTransactionList where recipientTaxnr is null
        defaultDepositTransactionShouldNotBeFound("recipientTaxnr.specified=false");
    }

    @Test
    @Transactional
    public void getAllDepositTransactionsByAmountIsEqualToSomething() throws Exception {
        // Initialize the database
        depositTransactionRepository.saveAndFlush(depositTransaction);

        // Get all the depositTransactionList where amount equals to DEFAULT_AMOUNT
        defaultDepositTransactionShouldBeFound("amount.equals=" + DEFAULT_AMOUNT);

        // Get all the depositTransactionList where amount equals to UPDATED_AMOUNT
        defaultDepositTransactionShouldNotBeFound("amount.equals=" + UPDATED_AMOUNT);
    }

    @Test
    @Transactional
    public void getAllDepositTransactionsByAmountIsInShouldWork() throws Exception {
        // Initialize the database
        depositTransactionRepository.saveAndFlush(depositTransaction);

        // Get all the depositTransactionList where amount in DEFAULT_AMOUNT or UPDATED_AMOUNT
        defaultDepositTransactionShouldBeFound("amount.in=" + DEFAULT_AMOUNT + "," + UPDATED_AMOUNT);

        // Get all the depositTransactionList where amount equals to UPDATED_AMOUNT
        defaultDepositTransactionShouldNotBeFound("amount.in=" + UPDATED_AMOUNT);
    }

    @Test
    @Transactional
    public void getAllDepositTransactionsByAmountIsNullOrNotNull() throws Exception {
        // Initialize the database
        depositTransactionRepository.saveAndFlush(depositTransaction);

        // Get all the depositTransactionList where amount is not null
        defaultDepositTransactionShouldBeFound("amount.specified=true");

        // Get all the depositTransactionList where amount is null
        defaultDepositTransactionShouldNotBeFound("amount.specified=false");
    }

    @Test
    @Transactional
    public void getAllDepositTransactionsByAmountIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        depositTransactionRepository.saveAndFlush(depositTransaction);

        // Get all the depositTransactionList where amount greater than or equals to DEFAULT_AMOUNT
        defaultDepositTransactionShouldBeFound("amount.greaterOrEqualThan=" + DEFAULT_AMOUNT);

        // Get all the depositTransactionList where amount greater than or equals to UPDATED_AMOUNT
        defaultDepositTransactionShouldNotBeFound("amount.greaterOrEqualThan=" + UPDATED_AMOUNT);
    }

    @Test
    @Transactional
    public void getAllDepositTransactionsByAmountIsLessThanSomething() throws Exception {
        // Initialize the database
        depositTransactionRepository.saveAndFlush(depositTransaction);

        // Get all the depositTransactionList where amount less than or equals to DEFAULT_AMOUNT
        defaultDepositTransactionShouldNotBeFound("amount.lessThan=" + DEFAULT_AMOUNT);

        // Get all the depositTransactionList where amount less than or equals to UPDATED_AMOUNT
        defaultDepositTransactionShouldBeFound("amount.lessThan=" + UPDATED_AMOUNT);
    }

    @Test
    @Transactional
    public void getAllDepositTransactionsByUpdateDateIsEqualToSomething() throws Exception {
        // Initialize the database
        depositTransactionRepository.saveAndFlush(depositTransaction);

        // Get all the depositTransactionList where updateDate equals to DEFAULT_UPDATE_DATE
        defaultDepositTransactionShouldBeFound("updateDate.equals=" + DEFAULT_UPDATE_DATE);

        // Get all the depositTransactionList where updateDate equals to UPDATED_UPDATE_DATE
        defaultDepositTransactionShouldNotBeFound("updateDate.equals=" + UPDATED_UPDATE_DATE);
    }

    @Test
    @Transactional
    public void getAllDepositTransactionsByUpdateDateIsInShouldWork() throws Exception {
        // Initialize the database
        depositTransactionRepository.saveAndFlush(depositTransaction);

        // Get all the depositTransactionList where updateDate in DEFAULT_UPDATE_DATE or UPDATED_UPDATE_DATE
        defaultDepositTransactionShouldBeFound("updateDate.in=" + DEFAULT_UPDATE_DATE + "," + UPDATED_UPDATE_DATE);

        // Get all the depositTransactionList where updateDate equals to UPDATED_UPDATE_DATE
        defaultDepositTransactionShouldNotBeFound("updateDate.in=" + UPDATED_UPDATE_DATE);
    }

    @Test
    @Transactional
    public void getAllDepositTransactionsByUpdateDateIsNullOrNotNull() throws Exception {
        // Initialize the database
        depositTransactionRepository.saveAndFlush(depositTransaction);

        // Get all the depositTransactionList where updateDate is not null
        defaultDepositTransactionShouldBeFound("updateDate.specified=true");

        // Get all the depositTransactionList where updateDate is null
        defaultDepositTransactionShouldNotBeFound("updateDate.specified=false");
    }

    @Test
    @Transactional
    public void getAllDepositTransactionsByCommentIsEqualToSomething() throws Exception {
        // Initialize the database
        depositTransactionRepository.saveAndFlush(depositTransaction);

        // Get all the depositTransactionList where comment equals to DEFAULT_COMMENT
        defaultDepositTransactionShouldBeFound("comment.equals=" + DEFAULT_COMMENT);

        // Get all the depositTransactionList where comment equals to UPDATED_COMMENT
        defaultDepositTransactionShouldNotBeFound("comment.equals=" + UPDATED_COMMENT);
    }

    @Test
    @Transactional
    public void getAllDepositTransactionsByCommentIsInShouldWork() throws Exception {
        // Initialize the database
        depositTransactionRepository.saveAndFlush(depositTransaction);

        // Get all the depositTransactionList where comment in DEFAULT_COMMENT or UPDATED_COMMENT
        defaultDepositTransactionShouldBeFound("comment.in=" + DEFAULT_COMMENT + "," + UPDATED_COMMENT);

        // Get all the depositTransactionList where comment equals to UPDATED_COMMENT
        defaultDepositTransactionShouldNotBeFound("comment.in=" + UPDATED_COMMENT);
    }

    @Test
    @Transactional
    public void getAllDepositTransactionsByCommentIsNullOrNotNull() throws Exception {
        // Initialize the database
        depositTransactionRepository.saveAndFlush(depositTransaction);

        // Get all the depositTransactionList where comment is not null
        defaultDepositTransactionShouldBeFound("comment.specified=true");

        // Get all the depositTransactionList where comment is null
        defaultDepositTransactionShouldNotBeFound("comment.specified=false");
    }

    @Test
    @Transactional
    public void getAllDepositTransactionsByDocIsEqualToSomething() throws Exception {
        // Initialize the database
        depositTransactionRepository.saveAndFlush(depositTransaction);

        // Get all the depositTransactionList where doc equals to DEFAULT_DOC
        defaultDepositTransactionShouldBeFound("doc.equals=" + DEFAULT_DOC);

        // Get all the depositTransactionList where doc equals to UPDATED_DOC
        defaultDepositTransactionShouldNotBeFound("doc.equals=" + UPDATED_DOC);
    }

    @Test
    @Transactional
    public void getAllDepositTransactionsByDocIsInShouldWork() throws Exception {
        // Initialize the database
        depositTransactionRepository.saveAndFlush(depositTransaction);

        // Get all the depositTransactionList where doc in DEFAULT_DOC or UPDATED_DOC
        defaultDepositTransactionShouldBeFound("doc.in=" + DEFAULT_DOC + "," + UPDATED_DOC);

        // Get all the depositTransactionList where doc equals to UPDATED_DOC
        defaultDepositTransactionShouldNotBeFound("doc.in=" + UPDATED_DOC);
    }

    @Test
    @Transactional
    public void getAllDepositTransactionsByDocIsNullOrNotNull() throws Exception {
        // Initialize the database
        depositTransactionRepository.saveAndFlush(depositTransaction);

        // Get all the depositTransactionList where doc is not null
        defaultDepositTransactionShouldBeFound("doc.specified=true");

        // Get all the depositTransactionList where doc is null
        defaultDepositTransactionShouldNotBeFound("doc.specified=false");
    }

    @Test
    @Transactional
    public void getAllDepositTransactionsByAgentLoginIsEqualToSomething() throws Exception {
        // Initialize the database
        depositTransactionRepository.saveAndFlush(depositTransaction);

        // Get all the depositTransactionList where agentLogin equals to DEFAULT_AGENT_LOGIN
        defaultDepositTransactionShouldBeFound("agentLogin.equals=" + DEFAULT_AGENT_LOGIN);

        // Get all the depositTransactionList where agentLogin equals to UPDATED_AGENT_LOGIN
        defaultDepositTransactionShouldNotBeFound("agentLogin.equals=" + UPDATED_AGENT_LOGIN);
    }

    @Test
    @Transactional
    public void getAllDepositTransactionsByAgentLoginIsInShouldWork() throws Exception {
        // Initialize the database
        depositTransactionRepository.saveAndFlush(depositTransaction);

        // Get all the depositTransactionList where agentLogin in DEFAULT_AGENT_LOGIN or UPDATED_AGENT_LOGIN
        defaultDepositTransactionShouldBeFound("agentLogin.in=" + DEFAULT_AGENT_LOGIN + "," + UPDATED_AGENT_LOGIN);

        // Get all the depositTransactionList where agentLogin equals to UPDATED_AGENT_LOGIN
        defaultDepositTransactionShouldNotBeFound("agentLogin.in=" + UPDATED_AGENT_LOGIN);
    }

    @Test
    @Transactional
    public void getAllDepositTransactionsByAgentLoginIsNullOrNotNull() throws Exception {
        // Initialize the database
        depositTransactionRepository.saveAndFlush(depositTransaction);

        // Get all the depositTransactionList where agentLogin is not null
        defaultDepositTransactionShouldBeFound("agentLogin.specified=true");

        // Get all the depositTransactionList where agentLogin is null
        defaultDepositTransactionShouldNotBeFound("agentLogin.specified=false");
    }

    /**
     * Executes the search, and checks that the default entity is returned
     */
    private void defaultDepositTransactionShouldBeFound(String filter) throws Exception {
        restDepositTransactionMockMvc
            .perform(get("/api/deposit-transactions?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(depositTransaction.getId().intValue())))
            .andExpect(jsonPath("$.[*].payerTaxnr").value(hasItem(DEFAULT_PAYER_TAXNR.toString())))
            .andExpect(jsonPath("$.[*].recipientTaxnr").value(hasItem(DEFAULT_RECIPIENT_TAXNR.toString())))
            .andExpect(jsonPath("$.[*].amount").value(hasItem(DEFAULT_AMOUNT.intValue())))
            .andExpect(jsonPath("$.[*].updateDate").value(hasItem(DEFAULT_UPDATE_DATE.toString())))
            .andExpect(jsonPath("$.[*].comment").value(hasItem(DEFAULT_COMMENT.toString())))
            .andExpect(jsonPath("$.[*].doc").value(hasItem(DEFAULT_DOC.toString())))
            .andExpect(jsonPath("$.[*].agentLogin").value(hasItem(DEFAULT_AGENT_LOGIN.toString())));

        // Check, that the count call also returns 1
        restDepositTransactionMockMvc
            .perform(get("/api/deposit-transactions/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(content().string("1"));
    }

    /**
     * Executes the search, and checks that the default entity is not returned
     */
    private void defaultDepositTransactionShouldNotBeFound(String filter) throws Exception {
        restDepositTransactionMockMvc
            .perform(get("/api/deposit-transactions?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty());

        // Check, that the count call also returns 0
        restDepositTransactionMockMvc
            .perform(get("/api/deposit-transactions/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(content().string("0"));
    }

    @Test
    @Transactional
    public void getNonExistingDepositTransaction() throws Exception {
        // Get the depositTransaction
        restDepositTransactionMockMvc.perform(get("/api/deposit-transactions/{id}", Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updateDepositTransaction() throws Exception {
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
            .doc(UPDATED_DOC)
            .agentLogin(UPDATED_AGENT_LOGIN);
        DepositTransactionDTO depositTransactionDTO = depositTransactionMapper.toDto(updatedDepositTransaction);

        restDepositTransactionMockMvc
            .perform(
                put("/api/deposit-transactions")
                    .contentType(TestUtil.APPLICATION_JSON_UTF8)
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
        assertThat(testDepositTransaction.getAgentLogin()).isEqualTo(UPDATED_AGENT_LOGIN);
    }

    @Test
    @Transactional
    public void updateNonExistingDepositTransaction() throws Exception {
        int databaseSizeBeforeUpdate = depositTransactionRepository.findAll().size();

        // Create the DepositTransaction
        DepositTransactionDTO depositTransactionDTO = depositTransactionMapper.toDto(depositTransaction);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restDepositTransactionMockMvc
            .perform(
                put("/api/deposit-transactions")
                    .contentType(TestUtil.APPLICATION_JSON_UTF8)
                    .content(TestUtil.convertObjectToJsonBytes(depositTransactionDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the DepositTransaction in the database
        List<DepositTransaction> depositTransactionList = depositTransactionRepository.findAll();
        assertThat(depositTransactionList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    public void deleteDepositTransaction() throws Exception {
        // Initialize the database
        depositTransactionRepository.saveAndFlush(depositTransaction);

        int databaseSizeBeforeDelete = depositTransactionRepository.findAll().size();

        // Get the depositTransaction
        restDepositTransactionMockMvc
            .perform(delete("/api/deposit-transactions/{id}", depositTransaction.getId()).accept(TestUtil.APPLICATION_JSON_UTF8))
            .andExpect(status().isOk());

        // Validate the database is empty
        List<DepositTransaction> depositTransactionList = depositTransactionRepository.findAll();
        assertThat(depositTransactionList).hasSize(databaseSizeBeforeDelete - 1);
    }

    @Test
    @Transactional
    public void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(DepositTransaction.class);
        DepositTransaction depositTransaction1 = new DepositTransaction();
        depositTransaction1.setId(1L);
        DepositTransaction depositTransaction2 = new DepositTransaction();
        depositTransaction2.setId(depositTransaction1.getId());
        assertThat(depositTransaction1).isEqualTo(depositTransaction2);
        depositTransaction2.setId(2L);
        assertThat(depositTransaction1).isNotEqualTo(depositTransaction2);
        depositTransaction1.setId(null);
        assertThat(depositTransaction1).isNotEqualTo(depositTransaction2);
    }

    @Test
    @Transactional
    public void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(DepositTransactionDTO.class);
        DepositTransactionDTO depositTransactionDTO1 = new DepositTransactionDTO();
        depositTransactionDTO1.setId(1L);
        DepositTransactionDTO depositTransactionDTO2 = new DepositTransactionDTO();
        assertThat(depositTransactionDTO1).isNotEqualTo(depositTransactionDTO2);
        depositTransactionDTO2.setId(depositTransactionDTO1.getId());
        assertThat(depositTransactionDTO1).isEqualTo(depositTransactionDTO2);
        depositTransactionDTO2.setId(2L);
        assertThat(depositTransactionDTO1).isNotEqualTo(depositTransactionDTO2);
        depositTransactionDTO1.setId(null);
        assertThat(depositTransactionDTO1).isNotEqualTo(depositTransactionDTO2);
    }
    //    @Test
    //    @Transactional
    //    public void testEntityFromId() {
    //        assertThat(depositTransactionMapper.fromId(42L).getId()).isEqualTo(42);
    //        assertThat(depositTransactionMapper.fromId(null)).isNull();
    //    }
}
