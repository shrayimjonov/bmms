package uz.paykap.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static uz.paykap.web.rest.TestUtil.createFormattingConversionService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
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
import uz.paykap.domain.Currency;
import uz.paykap.repository.CurrencyRepository;
import uz.paykap.service.CurrencyQueryService;
import uz.paykap.service.CurrencyService;
import uz.paykap.service.dto.CurrencyCriteria;
import uz.paykap.service.dto.CurrencyDTO;
import uz.paykap.service.mapper.CurrencyMapper;
import uz.paykap.web.rest.errors.ExceptionTranslator;

/**
 * Test class for the CurrencyResource REST controller.
 *
 * @see CurrencyResource
 */
@RunWith(SpringRunner.class)
@IntegrationTest
public class CurrencyResourceIntTest {

    private static final String DEFAULT_CODE = "AAA";
    private static final String UPDATED_CODE = "BBB";

    private static final String DEFAULT_CCY = "AAA";
    private static final String UPDATED_CCY = "BBB";

    private static final String DEFAULT_NAME_RU = "AAAAAAAAAA";
    private static final String UPDATED_NAME_RU = "BBBBBBBBBB";

    private static final BigDecimal DEFAULT_RATE = new BigDecimal(1);
    private static final BigDecimal UPDATED_RATE = new BigDecimal(2);

    private static final BigDecimal DEFAULT_SELL_RATE = new BigDecimal(1);
    private static final BigDecimal UPDATED_SELL_RATE = new BigDecimal(2);

    private static final BigDecimal DEFAULT_BUY_RATE = new BigDecimal(1);
    private static final BigDecimal UPDATED_BUY_RATE = new BigDecimal(2);

    private static final LocalDate DEFAULT_DATE = LocalDate.ofEpochDay(0L);
    private static final LocalDate UPDATED_DATE = LocalDate.now(ZoneId.systemDefault());

    private static final Boolean DEFAULT_ACTIVE = false;
    private static final Boolean UPDATED_ACTIVE = true;

    private static final String DEFAULT_CCY_GROUP = "AAAAAAAAAA";
    private static final String UPDATED_CCY_GROUP = "BBBBBBBBBB";

    @Autowired
    private CurrencyRepository currencyRepository;

    @Autowired
    private CurrencyMapper currencyMapper;

    @Autowired
    private CurrencyService currencyService;

    @Autowired
    private CurrencyQueryService currencyQueryService;

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

    private MockMvc restCurrencyMockMvc;

    private Currency currency;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        final CurrencyResource currencyResource = new CurrencyResource(currencyService, currencyRepository, currencyQueryService);
        this.restCurrencyMockMvc =
            MockMvcBuilders
                .standaloneSetup(currencyResource)
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
    public static Currency createEntity(EntityManager em) {
        Currency currency = new Currency()
            .code(DEFAULT_CODE)
            .ccy(DEFAULT_CCY)
            .nameRu(DEFAULT_NAME_RU)
            .rate(DEFAULT_RATE)
            .sellRate(DEFAULT_SELL_RATE)
            .buyRate(DEFAULT_BUY_RATE)
            .date(DEFAULT_DATE)
            .active(DEFAULT_ACTIVE)
            .ccyGroup(DEFAULT_CCY_GROUP);
        return currency;
    }

    @Before
    public void initTest() {
        currency = createEntity(em);
    }

    @Test
    @Transactional
    public void createCurrency() throws Exception {
        int databaseSizeBeforeCreate = currencyRepository.findAll().size();

        // Create the Currency
        CurrencyDTO currencyDTO = currencyMapper.toDto(currency);
        restCurrencyMockMvc
            .perform(
                post("/api/currencies").contentType(TestUtil.APPLICATION_JSON_UTF8).content(TestUtil.convertObjectToJsonBytes(currencyDTO))
            )
            .andExpect(status().isCreated());

        // Validate the Currency in the database
        List<Currency> currencyList = currencyRepository.findAll();
        assertThat(currencyList).hasSize(databaseSizeBeforeCreate + 1);
        Currency testCurrency = currencyList.get(currencyList.size() - 1);
        assertThat(testCurrency.getCode()).isEqualTo(DEFAULT_CODE);
        assertThat(testCurrency.getCcy()).isEqualTo(DEFAULT_CCY);
        assertThat(testCurrency.getNameRu()).isEqualTo(DEFAULT_NAME_RU);
        assertThat(testCurrency.getRate()).isEqualTo(DEFAULT_RATE);
        assertThat(testCurrency.getSellRate()).isEqualTo(DEFAULT_SELL_RATE);
        assertThat(testCurrency.getBuyRate()).isEqualTo(DEFAULT_BUY_RATE);
        assertThat(testCurrency.getDate()).isEqualTo(DEFAULT_DATE);
        assertThat(testCurrency.isActive()).isEqualTo(DEFAULT_ACTIVE);
        assertThat(testCurrency.getCcyGroup()).isEqualTo(DEFAULT_CCY_GROUP);
    }

    @Test
    @Transactional
    public void createCurrencyWithExistingId() throws Exception {
        int databaseSizeBeforeCreate = currencyRepository.findAll().size();

        // Create the Currency with an existing ID
        currency.setId(1L);
        CurrencyDTO currencyDTO = currencyMapper.toDto(currency);

        // An entity with an existing ID cannot be created, so this API call must fail
        restCurrencyMockMvc
            .perform(
                post("/api/currencies").contentType(TestUtil.APPLICATION_JSON_UTF8).content(TestUtil.convertObjectToJsonBytes(currencyDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Currency in the database
        List<Currency> currencyList = currencyRepository.findAll();
        assertThat(currencyList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    public void checkCodeIsRequired() throws Exception {
        int databaseSizeBeforeTest = currencyRepository.findAll().size();
        // set the field null
        currency.setCode(null);

        // Create the Currency, which fails.
        CurrencyDTO currencyDTO = currencyMapper.toDto(currency);

        restCurrencyMockMvc
            .perform(
                post("/api/currencies").contentType(TestUtil.APPLICATION_JSON_UTF8).content(TestUtil.convertObjectToJsonBytes(currencyDTO))
            )
            .andExpect(status().isBadRequest());

        List<Currency> currencyList = currencyRepository.findAll();
        assertThat(currencyList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkCcyIsRequired() throws Exception {
        int databaseSizeBeforeTest = currencyRepository.findAll().size();
        // set the field null
        currency.setCcy(null);

        // Create the Currency, which fails.
        CurrencyDTO currencyDTO = currencyMapper.toDto(currency);

        restCurrencyMockMvc
            .perform(
                post("/api/currencies").contentType(TestUtil.APPLICATION_JSON_UTF8).content(TestUtil.convertObjectToJsonBytes(currencyDTO))
            )
            .andExpect(status().isBadRequest());

        List<Currency> currencyList = currencyRepository.findAll();
        assertThat(currencyList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkActiveIsRequired() throws Exception {
        int databaseSizeBeforeTest = currencyRepository.findAll().size();
        // set the field null
        currency.setActive(null);

        // Create the Currency, which fails.
        CurrencyDTO currencyDTO = currencyMapper.toDto(currency);

        restCurrencyMockMvc
            .perform(
                post("/api/currencies").contentType(TestUtil.APPLICATION_JSON_UTF8).content(TestUtil.convertObjectToJsonBytes(currencyDTO))
            )
            .andExpect(status().isBadRequest());

        List<Currency> currencyList = currencyRepository.findAll();
        assertThat(currencyList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void getAllCurrencies() throws Exception {
        // Initialize the database
        currencyRepository.saveAndFlush(currency);

        // Get all the currencyList
        restCurrencyMockMvc
            .perform(get("/api/currencies?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(currency.getId().intValue())))
            .andExpect(jsonPath("$.[*].code").value(hasItem(DEFAULT_CODE.toString())))
            .andExpect(jsonPath("$.[*].ccy").value(hasItem(DEFAULT_CCY.toString())))
            .andExpect(jsonPath("$.[*].nameRu").value(hasItem(DEFAULT_NAME_RU.toString())))
            .andExpect(jsonPath("$.[*].rate").value(hasItem(DEFAULT_RATE.intValue())))
            .andExpect(jsonPath("$.[*].sellRate").value(hasItem(DEFAULT_SELL_RATE.intValue())))
            .andExpect(jsonPath("$.[*].buyRate").value(hasItem(DEFAULT_BUY_RATE.intValue())))
            .andExpect(jsonPath("$.[*].date").value(hasItem(DEFAULT_DATE.toString())))
            .andExpect(jsonPath("$.[*].active").value(hasItem(DEFAULT_ACTIVE.booleanValue())))
            .andExpect(jsonPath("$.[*].ccyGroup").value(hasItem(DEFAULT_CCY_GROUP.toString())));
    }

    @Test
    @Transactional
    public void getCurrency() throws Exception {
        // Initialize the database
        currencyRepository.saveAndFlush(currency);

        // Get the currency
        restCurrencyMockMvc
            .perform(get("/api/currencies/{id}", currency.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.id").value(currency.getId().intValue()))
            .andExpect(jsonPath("$.code").value(DEFAULT_CODE.toString()))
            .andExpect(jsonPath("$.ccy").value(DEFAULT_CCY.toString()))
            .andExpect(jsonPath("$.nameRu").value(DEFAULT_NAME_RU.toString()))
            .andExpect(jsonPath("$.rate").value(DEFAULT_RATE.intValue()))
            .andExpect(jsonPath("$.sellRate").value(DEFAULT_SELL_RATE.intValue()))
            .andExpect(jsonPath("$.buyRate").value(DEFAULT_BUY_RATE.intValue()))
            .andExpect(jsonPath("$.date").value(DEFAULT_DATE.toString()))
            .andExpect(jsonPath("$.active").value(DEFAULT_ACTIVE.booleanValue()))
            .andExpect(jsonPath("$.ccyGroup").value(DEFAULT_CCY_GROUP.toString()));
    }

    @Test
    @Transactional
    public void getAllCurrenciesByCodeIsEqualToSomething() throws Exception {
        // Initialize the database
        currencyRepository.saveAndFlush(currency);

        // Get all the currencyList where code equals to DEFAULT_CODE
        defaultCurrencyShouldBeFound("code.equals=" + DEFAULT_CODE);

        // Get all the currencyList where code equals to UPDATED_CODE
        defaultCurrencyShouldNotBeFound("code.equals=" + UPDATED_CODE);
    }

    @Test
    @Transactional
    public void getAllCurrenciesByCodeIsInShouldWork() throws Exception {
        // Initialize the database
        currencyRepository.saveAndFlush(currency);

        // Get all the currencyList where code in DEFAULT_CODE or UPDATED_CODE
        defaultCurrencyShouldBeFound("code.in=" + DEFAULT_CODE + "," + UPDATED_CODE);

        // Get all the currencyList where code equals to UPDATED_CODE
        defaultCurrencyShouldNotBeFound("code.in=" + UPDATED_CODE);
    }

    @Test
    @Transactional
    public void getAllCurrenciesByCodeIsNullOrNotNull() throws Exception {
        // Initialize the database
        currencyRepository.saveAndFlush(currency);

        // Get all the currencyList where code is not null
        defaultCurrencyShouldBeFound("code.specified=true");

        // Get all the currencyList where code is null
        defaultCurrencyShouldNotBeFound("code.specified=false");
    }

    @Test
    @Transactional
    public void getAllCurrenciesByCcyIsEqualToSomething() throws Exception {
        // Initialize the database
        currencyRepository.saveAndFlush(currency);

        // Get all the currencyList where ccy equals to DEFAULT_CCY
        defaultCurrencyShouldBeFound("ccy.equals=" + DEFAULT_CCY);

        // Get all the currencyList where ccy equals to UPDATED_CCY
        defaultCurrencyShouldNotBeFound("ccy.equals=" + UPDATED_CCY);
    }

    @Test
    @Transactional
    public void getAllCurrenciesByCcyIsInShouldWork() throws Exception {
        // Initialize the database
        currencyRepository.saveAndFlush(currency);

        // Get all the currencyList where ccy in DEFAULT_CCY or UPDATED_CCY
        defaultCurrencyShouldBeFound("ccy.in=" + DEFAULT_CCY + "," + UPDATED_CCY);

        // Get all the currencyList where ccy equals to UPDATED_CCY
        defaultCurrencyShouldNotBeFound("ccy.in=" + UPDATED_CCY);
    }

    @Test
    @Transactional
    public void getAllCurrenciesByCcyIsNullOrNotNull() throws Exception {
        // Initialize the database
        currencyRepository.saveAndFlush(currency);

        // Get all the currencyList where ccy is not null
        defaultCurrencyShouldBeFound("ccy.specified=true");

        // Get all the currencyList where ccy is null
        defaultCurrencyShouldNotBeFound("ccy.specified=false");
    }

    @Test
    @Transactional
    public void getAllCurrenciesByNameRuIsEqualToSomething() throws Exception {
        // Initialize the database
        currencyRepository.saveAndFlush(currency);

        // Get all the currencyList where nameRu equals to DEFAULT_NAME_RU
        defaultCurrencyShouldBeFound("nameRu.equals=" + DEFAULT_NAME_RU);

        // Get all the currencyList where nameRu equals to UPDATED_NAME_RU
        defaultCurrencyShouldNotBeFound("nameRu.equals=" + UPDATED_NAME_RU);
    }

    @Test
    @Transactional
    public void getAllCurrenciesByNameRuIsInShouldWork() throws Exception {
        // Initialize the database
        currencyRepository.saveAndFlush(currency);

        // Get all the currencyList where nameRu in DEFAULT_NAME_RU or UPDATED_NAME_RU
        defaultCurrencyShouldBeFound("nameRu.in=" + DEFAULT_NAME_RU + "," + UPDATED_NAME_RU);

        // Get all the currencyList where nameRu equals to UPDATED_NAME_RU
        defaultCurrencyShouldNotBeFound("nameRu.in=" + UPDATED_NAME_RU);
    }

    @Test
    @Transactional
    public void getAllCurrenciesByNameRuIsNullOrNotNull() throws Exception {
        // Initialize the database
        currencyRepository.saveAndFlush(currency);

        // Get all the currencyList where nameRu is not null
        defaultCurrencyShouldBeFound("nameRu.specified=true");

        // Get all the currencyList where nameRu is null
        defaultCurrencyShouldNotBeFound("nameRu.specified=false");
    }

    @Test
    @Transactional
    public void getAllCurrenciesByRateIsEqualToSomething() throws Exception {
        // Initialize the database
        currencyRepository.saveAndFlush(currency);

        // Get all the currencyList where rate equals to DEFAULT_RATE
        defaultCurrencyShouldBeFound("rate.equals=" + DEFAULT_RATE);

        // Get all the currencyList where rate equals to UPDATED_RATE
        defaultCurrencyShouldNotBeFound("rate.equals=" + UPDATED_RATE);
    }

    @Test
    @Transactional
    public void getAllCurrenciesByRateIsInShouldWork() throws Exception {
        // Initialize the database
        currencyRepository.saveAndFlush(currency);

        // Get all the currencyList where rate in DEFAULT_RATE or UPDATED_RATE
        defaultCurrencyShouldBeFound("rate.in=" + DEFAULT_RATE + "," + UPDATED_RATE);

        // Get all the currencyList where rate equals to UPDATED_RATE
        defaultCurrencyShouldNotBeFound("rate.in=" + UPDATED_RATE);
    }

    @Test
    @Transactional
    public void getAllCurrenciesByRateIsNullOrNotNull() throws Exception {
        // Initialize the database
        currencyRepository.saveAndFlush(currency);

        // Get all the currencyList where rate is not null
        defaultCurrencyShouldBeFound("rate.specified=true");

        // Get all the currencyList where rate is null
        defaultCurrencyShouldNotBeFound("rate.specified=false");
    }

    @Test
    @Transactional
    public void getAllCurrenciesBySellRateIsEqualToSomething() throws Exception {
        // Initialize the database
        currencyRepository.saveAndFlush(currency);

        // Get all the currencyList where sellRate equals to DEFAULT_SELL_RATE
        defaultCurrencyShouldBeFound("sellRate.equals=" + DEFAULT_SELL_RATE);

        // Get all the currencyList where sellRate equals to UPDATED_SELL_RATE
        defaultCurrencyShouldNotBeFound("sellRate.equals=" + UPDATED_SELL_RATE);
    }

    @Test
    @Transactional
    public void getAllCurrenciesBySellRateIsInShouldWork() throws Exception {
        // Initialize the database
        currencyRepository.saveAndFlush(currency);

        // Get all the currencyList where sellRate in DEFAULT_SELL_RATE or UPDATED_SELL_RATE
        defaultCurrencyShouldBeFound("sellRate.in=" + DEFAULT_SELL_RATE + "," + UPDATED_SELL_RATE);

        // Get all the currencyList where sellRate equals to UPDATED_SELL_RATE
        defaultCurrencyShouldNotBeFound("sellRate.in=" + UPDATED_SELL_RATE);
    }

    @Test
    @Transactional
    public void getAllCurrenciesBySellRateIsNullOrNotNull() throws Exception {
        // Initialize the database
        currencyRepository.saveAndFlush(currency);

        // Get all the currencyList where sellRate is not null
        defaultCurrencyShouldBeFound("sellRate.specified=true");

        // Get all the currencyList where sellRate is null
        defaultCurrencyShouldNotBeFound("sellRate.specified=false");
    }

    @Test
    @Transactional
    public void getAllCurrenciesByBuyRateIsEqualToSomething() throws Exception {
        // Initialize the database
        currencyRepository.saveAndFlush(currency);

        // Get all the currencyList where buyRate equals to DEFAULT_BUY_RATE
        defaultCurrencyShouldBeFound("buyRate.equals=" + DEFAULT_BUY_RATE);

        // Get all the currencyList where buyRate equals to UPDATED_BUY_RATE
        defaultCurrencyShouldNotBeFound("buyRate.equals=" + UPDATED_BUY_RATE);
    }

    @Test
    @Transactional
    public void getAllCurrenciesByBuyRateIsInShouldWork() throws Exception {
        // Initialize the database
        currencyRepository.saveAndFlush(currency);

        // Get all the currencyList where buyRate in DEFAULT_BUY_RATE or UPDATED_BUY_RATE
        defaultCurrencyShouldBeFound("buyRate.in=" + DEFAULT_BUY_RATE + "," + UPDATED_BUY_RATE);

        // Get all the currencyList where buyRate equals to UPDATED_BUY_RATE
        defaultCurrencyShouldNotBeFound("buyRate.in=" + UPDATED_BUY_RATE);
    }

    @Test
    @Transactional
    public void getAllCurrenciesByBuyRateIsNullOrNotNull() throws Exception {
        // Initialize the database
        currencyRepository.saveAndFlush(currency);

        // Get all the currencyList where buyRate is not null
        defaultCurrencyShouldBeFound("buyRate.specified=true");

        // Get all the currencyList where buyRate is null
        defaultCurrencyShouldNotBeFound("buyRate.specified=false");
    }

    @Test
    @Transactional
    public void getAllCurrenciesByDateIsEqualToSomething() throws Exception {
        // Initialize the database
        currencyRepository.saveAndFlush(currency);

        // Get all the currencyList where date equals to DEFAULT_DATE
        defaultCurrencyShouldBeFound("date.equals=" + DEFAULT_DATE);

        // Get all the currencyList where date equals to UPDATED_DATE
        defaultCurrencyShouldNotBeFound("date.equals=" + UPDATED_DATE);
    }

    @Test
    @Transactional
    public void getAllCurrenciesByDateIsInShouldWork() throws Exception {
        // Initialize the database
        currencyRepository.saveAndFlush(currency);

        // Get all the currencyList where date in DEFAULT_DATE or UPDATED_DATE
        defaultCurrencyShouldBeFound("date.in=" + DEFAULT_DATE + "," + UPDATED_DATE);

        // Get all the currencyList where date equals to UPDATED_DATE
        defaultCurrencyShouldNotBeFound("date.in=" + UPDATED_DATE);
    }

    @Test
    @Transactional
    public void getAllCurrenciesByDateIsNullOrNotNull() throws Exception {
        // Initialize the database
        currencyRepository.saveAndFlush(currency);

        // Get all the currencyList where date is not null
        defaultCurrencyShouldBeFound("date.specified=true");

        // Get all the currencyList where date is null
        defaultCurrencyShouldNotBeFound("date.specified=false");
    }

    @Test
    @Transactional
    public void getAllCurrenciesByDateIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        currencyRepository.saveAndFlush(currency);

        // Get all the currencyList where date greater than or equals to DEFAULT_DATE
        defaultCurrencyShouldBeFound("date.greaterOrEqualThan=" + DEFAULT_DATE);

        // Get all the currencyList where date greater than or equals to UPDATED_DATE
        defaultCurrencyShouldNotBeFound("date.greaterOrEqualThan=" + UPDATED_DATE);
    }

    @Test
    @Transactional
    public void getAllCurrenciesByDateIsLessThanSomething() throws Exception {
        // Initialize the database
        currencyRepository.saveAndFlush(currency);

        // Get all the currencyList where date less than or equals to DEFAULT_DATE
        defaultCurrencyShouldNotBeFound("date.lessThan=" + DEFAULT_DATE);

        // Get all the currencyList where date less than or equals to UPDATED_DATE
        defaultCurrencyShouldBeFound("date.lessThan=" + UPDATED_DATE);
    }

    @Test
    @Transactional
    public void getAllCurrenciesByActiveIsEqualToSomething() throws Exception {
        // Initialize the database
        currencyRepository.saveAndFlush(currency);

        // Get all the currencyList where active equals to DEFAULT_ACTIVE
        defaultCurrencyShouldBeFound("active.equals=" + DEFAULT_ACTIVE);

        // Get all the currencyList where active equals to UPDATED_ACTIVE
        defaultCurrencyShouldNotBeFound("active.equals=" + UPDATED_ACTIVE);
    }

    @Test
    @Transactional
    public void getAllCurrenciesByActiveIsInShouldWork() throws Exception {
        // Initialize the database
        currencyRepository.saveAndFlush(currency);

        // Get all the currencyList where active in DEFAULT_ACTIVE or UPDATED_ACTIVE
        defaultCurrencyShouldBeFound("active.in=" + DEFAULT_ACTIVE + "," + UPDATED_ACTIVE);

        // Get all the currencyList where active equals to UPDATED_ACTIVE
        defaultCurrencyShouldNotBeFound("active.in=" + UPDATED_ACTIVE);
    }

    @Test
    @Transactional
    public void getAllCurrenciesByActiveIsNullOrNotNull() throws Exception {
        // Initialize the database
        currencyRepository.saveAndFlush(currency);

        // Get all the currencyList where active is not null
        defaultCurrencyShouldBeFound("active.specified=true");

        // Get all the currencyList where active is null
        defaultCurrencyShouldNotBeFound("active.specified=false");
    }

    @Test
    @Transactional
    public void getAllCurrenciesByCcyGroupIsEqualToSomething() throws Exception {
        // Initialize the database
        currencyRepository.saveAndFlush(currency);

        // Get all the currencyList where ccyGroup equals to DEFAULT_CCY_GROUP
        defaultCurrencyShouldBeFound("ccyGroup.equals=" + DEFAULT_CCY_GROUP);

        // Get all the currencyList where ccyGroup equals to UPDATED_CCY_GROUP
        defaultCurrencyShouldNotBeFound("ccyGroup.equals=" + UPDATED_CCY_GROUP);
    }

    @Test
    @Transactional
    public void getAllCurrenciesByCcyGroupIsInShouldWork() throws Exception {
        // Initialize the database
        currencyRepository.saveAndFlush(currency);

        // Get all the currencyList where ccyGroup in DEFAULT_CCY_GROUP or UPDATED_CCY_GROUP
        defaultCurrencyShouldBeFound("ccyGroup.in=" + DEFAULT_CCY_GROUP + "," + UPDATED_CCY_GROUP);

        // Get all the currencyList where ccyGroup equals to UPDATED_CCY_GROUP
        defaultCurrencyShouldNotBeFound("ccyGroup.in=" + UPDATED_CCY_GROUP);
    }

    @Test
    @Transactional
    public void getAllCurrenciesByCcyGroupIsNullOrNotNull() throws Exception {
        // Initialize the database
        currencyRepository.saveAndFlush(currency);

        // Get all the currencyList where ccyGroup is not null
        defaultCurrencyShouldBeFound("ccyGroup.specified=true");

        // Get all the currencyList where ccyGroup is null
        defaultCurrencyShouldNotBeFound("ccyGroup.specified=false");
    }
    /**
     * Executes the search, and checks that the default entity is returned
     */
    private void defaultCurrencyShouldBeFound(String filter) throws Exception {
        restCurrencyMockMvc
            .perform(get("/api/currencies?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(currency.getId().intValue())))
            .andExpect(jsonPath("$.[*].code").value(hasItem(DEFAULT_CODE.toString())))
            .andExpect(jsonPath("$.[*].ccy").value(hasItem(DEFAULT_CCY.toString())))
            .andExpect(jsonPath("$.[*].nameRu").value(hasItem(DEFAULT_NAME_RU.toString())))
            .andExpect(jsonPath("$.[*].rate").value(hasItem(DEFAULT_RATE.intValue())))
            .andExpect(jsonPath("$.[*].sellRate").value(hasItem(DEFAULT_SELL_RATE.intValue())))
            .andExpect(jsonPath("$.[*].buyRate").value(hasItem(DEFAULT_BUY_RATE.intValue())))
            .andExpect(jsonPath("$.[*].date").value(hasItem(DEFAULT_DATE.toString())))
            .andExpect(jsonPath("$.[*].active").value(hasItem(DEFAULT_ACTIVE.booleanValue())))
            .andExpect(jsonPath("$.[*].ccyGroup").value(hasItem(DEFAULT_CCY_GROUP.toString())));

        // Check, that the count call also returns 1
        restCurrencyMockMvc
            .perform(get("/api/currencies/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(content().string("1"));
    }

    /**
     * Executes the search, and checks that the default entity is not returned
     */
    private void defaultCurrencyShouldNotBeFound(String filter) throws Exception {
        restCurrencyMockMvc
            .perform(get("/api/currencies?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty());

        // Check, that the count call also returns 0
        restCurrencyMockMvc
            .perform(get("/api/currencies/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(content().string("0"));
    }

    @Test
    @Transactional
    public void getNonExistingCurrency() throws Exception {
        // Get the currency
        restCurrencyMockMvc.perform(get("/api/currencies/{id}", Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updateCurrency() throws Exception {
        // Initialize the database
        currencyRepository.saveAndFlush(currency);

        int databaseSizeBeforeUpdate = currencyRepository.findAll().size();

        // Update the currency
        Currency updatedCurrency = currencyRepository.findById(currency.getId()).get();
        // Disconnect from session so that the updates on updatedCurrency are not directly saved in db
        em.detach(updatedCurrency);
        updatedCurrency
            .code(UPDATED_CODE)
            .ccy(UPDATED_CCY)
            .nameRu(UPDATED_NAME_RU)
            .rate(UPDATED_RATE)
            .sellRate(UPDATED_SELL_RATE)
            .buyRate(UPDATED_BUY_RATE)
            .date(UPDATED_DATE)
            .active(UPDATED_ACTIVE)
            .ccyGroup(UPDATED_CCY_GROUP);
        CurrencyDTO currencyDTO = currencyMapper.toDto(updatedCurrency);

        restCurrencyMockMvc
            .perform(
                put("/api/currencies").contentType(TestUtil.APPLICATION_JSON_UTF8).content(TestUtil.convertObjectToJsonBytes(currencyDTO))
            )
            .andExpect(status().isOk());

        // Validate the Currency in the database
        List<Currency> currencyList = currencyRepository.findAll();
        assertThat(currencyList).hasSize(databaseSizeBeforeUpdate);
        Currency testCurrency = currencyList.get(currencyList.size() - 1);
        assertThat(testCurrency.getCode()).isEqualTo(UPDATED_CODE);
        assertThat(testCurrency.getCcy()).isEqualTo(UPDATED_CCY);
        assertThat(testCurrency.getNameRu()).isEqualTo(UPDATED_NAME_RU);
        assertThat(testCurrency.getRate()).isEqualTo(UPDATED_RATE);
        assertThat(testCurrency.getSellRate()).isEqualTo(UPDATED_SELL_RATE);
        assertThat(testCurrency.getBuyRate()).isEqualTo(UPDATED_BUY_RATE);
        assertThat(testCurrency.getDate()).isEqualTo(UPDATED_DATE);
        assertThat(testCurrency.isActive()).isEqualTo(UPDATED_ACTIVE);
        assertThat(testCurrency.getCcyGroup()).isEqualTo(UPDATED_CCY_GROUP);
    }

    @Test
    @Transactional
    public void updateNonExistingCurrency() throws Exception {
        int databaseSizeBeforeUpdate = currencyRepository.findAll().size();

        // Create the Currency
        CurrencyDTO currencyDTO = currencyMapper.toDto(currency);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restCurrencyMockMvc
            .perform(
                put("/api/currencies").contentType(TestUtil.APPLICATION_JSON_UTF8).content(TestUtil.convertObjectToJsonBytes(currencyDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Currency in the database
        List<Currency> currencyList = currencyRepository.findAll();
        assertThat(currencyList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    public void deleteCurrency() throws Exception {
        // Initialize the database
        currencyRepository.saveAndFlush(currency);

        int databaseSizeBeforeDelete = currencyRepository.findAll().size();

        // Get the currency
        restCurrencyMockMvc
            .perform(delete("/api/currencies/{id}", currency.getId()).accept(TestUtil.APPLICATION_JSON_UTF8))
            .andExpect(status().isOk());

        // Validate the database is empty
        List<Currency> currencyList = currencyRepository.findAll();
        assertThat(currencyList).hasSize(databaseSizeBeforeDelete - 1);
    }

    @Test
    @Transactional
    public void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Currency.class);
        Currency currency1 = new Currency();
        currency1.setId(1L);
        Currency currency2 = new Currency();
        currency2.setId(currency1.getId());
        assertThat(currency1).isEqualTo(currency2);
        currency2.setId(2L);
        assertThat(currency1).isNotEqualTo(currency2);
        currency1.setId(null);
        assertThat(currency1).isNotEqualTo(currency2);
    }

    @Test
    @Transactional
    public void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(CurrencyDTO.class);
        CurrencyDTO currencyDTO1 = new CurrencyDTO();
        currencyDTO1.setId(1L);
        CurrencyDTO currencyDTO2 = new CurrencyDTO();
        assertThat(currencyDTO1).isNotEqualTo(currencyDTO2);
        currencyDTO2.setId(currencyDTO1.getId());
        assertThat(currencyDTO1).isEqualTo(currencyDTO2);
        currencyDTO2.setId(2L);
        assertThat(currencyDTO1).isNotEqualTo(currencyDTO2);
        currencyDTO1.setId(null);
        assertThat(currencyDTO1).isNotEqualTo(currencyDTO2);
    }
    //    @Test
    //    @Transactional
    //    public void testEntityFromId() {
    //        assertThat(currencyMapper.fromId(42L).getId()).isEqualTo(42);
    //        assertThat(currencyMapper.fromId(null)).isNull();
    //    }
}
