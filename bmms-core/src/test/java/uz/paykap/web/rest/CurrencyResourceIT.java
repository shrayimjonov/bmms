package uz.paykap.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static uz.paykap.web.rest.TestUtil.sameNumber;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;
import javax.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import uz.paykap.IntegrationTest;
import uz.paykap.domain.Currency;
import uz.paykap.repository.CurrencyRepository;
import uz.paykap.service.criteria.CurrencyCriteria;
import uz.paykap.service.dto.CurrencyDTO;
import uz.paykap.service.mapper.CurrencyMapper;

/**
 * Integration tests for the {@link CurrencyResource} REST controller.
 */
@Disabled("Fix later, need to simulate CBU service to return suitable data")
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class CurrencyResourceIT {

    private static final String DEFAULT_CODE = "AAA";
    private static final String UPDATED_CODE = "BBB";

    private static final String DEFAULT_CCY = "AAA";
    private static final String UPDATED_CCY = "BBB";

    private static final String DEFAULT_NAME_RU = "AAAAAAAAAA";
    private static final String UPDATED_NAME_RU = "BBBBBBBBBB";

    private static final BigDecimal DEFAULT_RATE = new BigDecimal(1);
    private static final BigDecimal UPDATED_RATE = new BigDecimal(2);
    private static final BigDecimal SMALLER_RATE = new BigDecimal(1 - 1);

    private static final BigDecimal DEFAULT_SELL_RATE = new BigDecimal(1);
    private static final BigDecimal UPDATED_SELL_RATE = new BigDecimal(2);
    private static final BigDecimal SMALLER_SELL_RATE = new BigDecimal(1 - 1);

    private static final BigDecimal DEFAULT_BUY_RATE = new BigDecimal(1);
    private static final BigDecimal UPDATED_BUY_RATE = new BigDecimal(2);
    private static final BigDecimal SMALLER_BUY_RATE = new BigDecimal(1 - 1);

    private static final LocalDate DEFAULT_DATE = LocalDate.ofEpochDay(0L);
    private static final LocalDate UPDATED_DATE = LocalDate.now(ZoneId.systemDefault());
    private static final LocalDate SMALLER_DATE = LocalDate.ofEpochDay(-1L);

    private static final Boolean DEFAULT_ACTIVE = false;
    private static final Boolean UPDATED_ACTIVE = true;

    private static final String ENTITY_API_URL = "/api/currencies";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong count = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private CurrencyRepository currencyRepository;

    @Autowired
    private CurrencyMapper currencyMapper;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restCurrencyMockMvc;

    private Currency currency;

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
            .active(DEFAULT_ACTIVE);
        return currency;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Currency createUpdatedEntity(EntityManager em) {
        Currency currency = new Currency()
            .code(UPDATED_CODE)
            .ccy(UPDATED_CCY)
            .nameRu(UPDATED_NAME_RU)
            .rate(UPDATED_RATE)
            .sellRate(UPDATED_SELL_RATE)
            .buyRate(UPDATED_BUY_RATE)
            .date(UPDATED_DATE)
            .active(UPDATED_ACTIVE);
        return currency;
    }

    @BeforeEach
    public void initTest() {
        currency = createEntity(em);
    }

    @Test
    @Transactional
    void createCurrency() throws Exception {
        int databaseSizeBeforeCreate = currencyRepository.findAll().size();
        // Create the Currency
        CurrencyDTO currencyDTO = currencyMapper.toDto(currency);
        restCurrencyMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(currencyDTO)))
            .andExpect(status().isCreated());

        // Validate the Currency in the database
        List<Currency> currencyList = currencyRepository.findAll();
        assertThat(currencyList).hasSize(databaseSizeBeforeCreate + 1);
        Currency testCurrency = currencyList.get(currencyList.size() - 1);
        assertThat(testCurrency.getCode()).isEqualTo(DEFAULT_CODE);
        assertThat(testCurrency.getCcy()).isEqualTo(DEFAULT_CCY);
        assertThat(testCurrency.getNameRu()).isEqualTo(DEFAULT_NAME_RU);
        assertThat(testCurrency.getRate()).isEqualByComparingTo(DEFAULT_RATE);
        assertThat(testCurrency.getSellRate()).isEqualByComparingTo(DEFAULT_SELL_RATE);
        assertThat(testCurrency.getBuyRate()).isEqualByComparingTo(DEFAULT_BUY_RATE);
        assertThat(testCurrency.getDate()).isEqualTo(DEFAULT_DATE);
        assertThat(testCurrency.getActive()).isEqualTo(DEFAULT_ACTIVE);
    }

    @Test
    @Transactional
    void createCurrencyWithExistingId() throws Exception {
        // Create the Currency with an existing ID
        currency.setId(1L);
        CurrencyDTO currencyDTO = currencyMapper.toDto(currency);

        int databaseSizeBeforeCreate = currencyRepository.findAll().size();

        // An entity with an existing ID cannot be created, so this API call must fail
        restCurrencyMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(currencyDTO)))
            .andExpect(status().isBadRequest());

        // Validate the Currency in the database
        List<Currency> currencyList = currencyRepository.findAll();
        assertThat(currencyList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkCodeIsRequired() throws Exception {
        int databaseSizeBeforeTest = currencyRepository.findAll().size();
        // set the field null
        currency.setCode(null);

        // Create the Currency, which fails.
        CurrencyDTO currencyDTO = currencyMapper.toDto(currency);

        restCurrencyMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(currencyDTO)))
            .andExpect(status().isBadRequest());

        List<Currency> currencyList = currencyRepository.findAll();
        assertThat(currencyList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkCcyIsRequired() throws Exception {
        int databaseSizeBeforeTest = currencyRepository.findAll().size();
        // set the field null
        currency.setCcy(null);

        // Create the Currency, which fails.
        CurrencyDTO currencyDTO = currencyMapper.toDto(currency);

        restCurrencyMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(currencyDTO)))
            .andExpect(status().isBadRequest());

        List<Currency> currencyList = currencyRepository.findAll();
        assertThat(currencyList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkActiveIsRequired() throws Exception {
        int databaseSizeBeforeTest = currencyRepository.findAll().size();
        // set the field null
        currency.setActive(null);

        // Create the Currency, which fails.
        CurrencyDTO currencyDTO = currencyMapper.toDto(currency);

        restCurrencyMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(currencyDTO)))
            .andExpect(status().isBadRequest());

        List<Currency> currencyList = currencyRepository.findAll();
        assertThat(currencyList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllCurrencies() throws Exception {
        // Initialize the database
        currencyRepository.saveAndFlush(currency);

        // Get all the currencyList
        restCurrencyMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(currency.getId().intValue())))
            .andExpect(jsonPath("$.[*].code").value(hasItem(DEFAULT_CODE)))
            .andExpect(jsonPath("$.[*].ccy").value(hasItem(DEFAULT_CCY)))
            .andExpect(jsonPath("$.[*].nameRu").value(hasItem(DEFAULT_NAME_RU)))
            .andExpect(jsonPath("$.[*].rate").value(hasItem(sameNumber(DEFAULT_RATE))))
            .andExpect(jsonPath("$.[*].sellRate").value(hasItem(sameNumber(DEFAULT_SELL_RATE))))
            .andExpect(jsonPath("$.[*].buyRate").value(hasItem(sameNumber(DEFAULT_BUY_RATE))))
            .andExpect(jsonPath("$.[*].date").value(hasItem(DEFAULT_DATE.toString())))
            .andExpect(jsonPath("$.[*].active").value(hasItem(DEFAULT_ACTIVE.booleanValue())));
    }

    @Test
    @Transactional
    void getCurrency() throws Exception {
        // Initialize the database
        currencyRepository.saveAndFlush(currency);

        // Get the currency
        restCurrencyMockMvc
            .perform(get(ENTITY_API_URL_ID, currency.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(currency.getId().intValue()))
            .andExpect(jsonPath("$.code").value(DEFAULT_CODE))
            .andExpect(jsonPath("$.ccy").value(DEFAULT_CCY))
            .andExpect(jsonPath("$.nameRu").value(DEFAULT_NAME_RU))
            .andExpect(jsonPath("$.rate").value(sameNumber(DEFAULT_RATE)))
            .andExpect(jsonPath("$.sellRate").value(sameNumber(DEFAULT_SELL_RATE)))
            .andExpect(jsonPath("$.buyRate").value(sameNumber(DEFAULT_BUY_RATE)))
            .andExpect(jsonPath("$.date").value(DEFAULT_DATE.toString()))
            .andExpect(jsonPath("$.active").value(DEFAULT_ACTIVE.booleanValue()));
    }

    @Test
    @Transactional
    void getCurrenciesByIdFiltering() throws Exception {
        // Initialize the database
        currencyRepository.saveAndFlush(currency);

        Long id = currency.getId();

        defaultCurrencyShouldBeFound("id.equals=" + id);
        defaultCurrencyShouldNotBeFound("id.notEquals=" + id);

        defaultCurrencyShouldBeFound("id.greaterThanOrEqual=" + id);
        defaultCurrencyShouldNotBeFound("id.greaterThan=" + id);

        defaultCurrencyShouldBeFound("id.lessThanOrEqual=" + id);
        defaultCurrencyShouldNotBeFound("id.lessThan=" + id);
    }

    @Test
    @Transactional
    void getAllCurrenciesByCodeIsEqualToSomething() throws Exception {
        // Initialize the database
        currencyRepository.saveAndFlush(currency);

        // Get all the currencyList where code equals to DEFAULT_CODE
        defaultCurrencyShouldBeFound("code.equals=" + DEFAULT_CODE);

        // Get all the currencyList where code equals to UPDATED_CODE
        defaultCurrencyShouldNotBeFound("code.equals=" + UPDATED_CODE);
    }

    @Test
    @Transactional
    void getAllCurrenciesByCodeIsNotEqualToSomething() throws Exception {
        // Initialize the database
        currencyRepository.saveAndFlush(currency);

        // Get all the currencyList where code not equals to DEFAULT_CODE
        defaultCurrencyShouldNotBeFound("code.notEquals=" + DEFAULT_CODE);

        // Get all the currencyList where code not equals to UPDATED_CODE
        defaultCurrencyShouldBeFound("code.notEquals=" + UPDATED_CODE);
    }

    @Test
    @Transactional
    void getAllCurrenciesByCodeIsInShouldWork() throws Exception {
        // Initialize the database
        currencyRepository.saveAndFlush(currency);

        // Get all the currencyList where code in DEFAULT_CODE or UPDATED_CODE
        defaultCurrencyShouldBeFound("code.in=" + DEFAULT_CODE + "," + UPDATED_CODE);

        // Get all the currencyList where code equals to UPDATED_CODE
        defaultCurrencyShouldNotBeFound("code.in=" + UPDATED_CODE);
    }

    @Test
    @Transactional
    void getAllCurrenciesByCodeIsNullOrNotNull() throws Exception {
        // Initialize the database
        currencyRepository.saveAndFlush(currency);

        // Get all the currencyList where code is not null
        defaultCurrencyShouldBeFound("code.specified=true");

        // Get all the currencyList where code is null
        defaultCurrencyShouldNotBeFound("code.specified=false");
    }

    @Test
    @Transactional
    void getAllCurrenciesByCodeContainsSomething() throws Exception {
        // Initialize the database
        currencyRepository.saveAndFlush(currency);

        // Get all the currencyList where code contains DEFAULT_CODE
        defaultCurrencyShouldBeFound("code.contains=" + DEFAULT_CODE);

        // Get all the currencyList where code contains UPDATED_CODE
        defaultCurrencyShouldNotBeFound("code.contains=" + UPDATED_CODE);
    }

    @Test
    @Transactional
    void getAllCurrenciesByCodeNotContainsSomething() throws Exception {
        // Initialize the database
        currencyRepository.saveAndFlush(currency);

        // Get all the currencyList where code does not contain DEFAULT_CODE
        defaultCurrencyShouldNotBeFound("code.doesNotContain=" + DEFAULT_CODE);

        // Get all the currencyList where code does not contain UPDATED_CODE
        defaultCurrencyShouldBeFound("code.doesNotContain=" + UPDATED_CODE);
    }

    @Test
    @Transactional
    void getAllCurrenciesByCcyIsEqualToSomething() throws Exception {
        // Initialize the database
        currencyRepository.saveAndFlush(currency);

        // Get all the currencyList where ccy equals to DEFAULT_CCY
        defaultCurrencyShouldBeFound("ccy.equals=" + DEFAULT_CCY);

        // Get all the currencyList where ccy equals to UPDATED_CCY
        defaultCurrencyShouldNotBeFound("ccy.equals=" + UPDATED_CCY);
    }

    @Test
    @Transactional
    void getAllCurrenciesByCcyIsNotEqualToSomething() throws Exception {
        // Initialize the database
        currencyRepository.saveAndFlush(currency);

        // Get all the currencyList where ccy not equals to DEFAULT_CCY
        defaultCurrencyShouldNotBeFound("ccy.notEquals=" + DEFAULT_CCY);

        // Get all the currencyList where ccy not equals to UPDATED_CCY
        defaultCurrencyShouldBeFound("ccy.notEquals=" + UPDATED_CCY);
    }

    @Test
    @Transactional
    void getAllCurrenciesByCcyIsInShouldWork() throws Exception {
        // Initialize the database
        currencyRepository.saveAndFlush(currency);

        // Get all the currencyList where ccy in DEFAULT_CCY or UPDATED_CCY
        defaultCurrencyShouldBeFound("ccy.in=" + DEFAULT_CCY + "," + UPDATED_CCY);

        // Get all the currencyList where ccy equals to UPDATED_CCY
        defaultCurrencyShouldNotBeFound("ccy.in=" + UPDATED_CCY);
    }

    @Test
    @Transactional
    void getAllCurrenciesByCcyIsNullOrNotNull() throws Exception {
        // Initialize the database
        currencyRepository.saveAndFlush(currency);

        // Get all the currencyList where ccy is not null
        defaultCurrencyShouldBeFound("ccy.specified=true");

        // Get all the currencyList where ccy is null
        defaultCurrencyShouldNotBeFound("ccy.specified=false");
    }

    @Test
    @Transactional
    void getAllCurrenciesByCcyContainsSomething() throws Exception {
        // Initialize the database
        currencyRepository.saveAndFlush(currency);

        // Get all the currencyList where ccy contains DEFAULT_CCY
        defaultCurrencyShouldBeFound("ccy.contains=" + DEFAULT_CCY);

        // Get all the currencyList where ccy contains UPDATED_CCY
        defaultCurrencyShouldNotBeFound("ccy.contains=" + UPDATED_CCY);
    }

    @Test
    @Transactional
    void getAllCurrenciesByCcyNotContainsSomething() throws Exception {
        // Initialize the database
        currencyRepository.saveAndFlush(currency);

        // Get all the currencyList where ccy does not contain DEFAULT_CCY
        defaultCurrencyShouldNotBeFound("ccy.doesNotContain=" + DEFAULT_CCY);

        // Get all the currencyList where ccy does not contain UPDATED_CCY
        defaultCurrencyShouldBeFound("ccy.doesNotContain=" + UPDATED_CCY);
    }

    @Test
    @Transactional
    void getAllCurrenciesByNameRuIsEqualToSomething() throws Exception {
        // Initialize the database
        currencyRepository.saveAndFlush(currency);

        // Get all the currencyList where nameRu equals to DEFAULT_NAME_RU
        defaultCurrencyShouldBeFound("nameRu.equals=" + DEFAULT_NAME_RU);

        // Get all the currencyList where nameRu equals to UPDATED_NAME_RU
        defaultCurrencyShouldNotBeFound("nameRu.equals=" + UPDATED_NAME_RU);
    }

    @Test
    @Transactional
    void getAllCurrenciesByNameRuIsNotEqualToSomething() throws Exception {
        // Initialize the database
        currencyRepository.saveAndFlush(currency);

        // Get all the currencyList where nameRu not equals to DEFAULT_NAME_RU
        defaultCurrencyShouldNotBeFound("nameRu.notEquals=" + DEFAULT_NAME_RU);

        // Get all the currencyList where nameRu not equals to UPDATED_NAME_RU
        defaultCurrencyShouldBeFound("nameRu.notEquals=" + UPDATED_NAME_RU);
    }

    @Test
    @Transactional
    void getAllCurrenciesByNameRuIsInShouldWork() throws Exception {
        // Initialize the database
        currencyRepository.saveAndFlush(currency);

        // Get all the currencyList where nameRu in DEFAULT_NAME_RU or UPDATED_NAME_RU
        defaultCurrencyShouldBeFound("nameRu.in=" + DEFAULT_NAME_RU + "," + UPDATED_NAME_RU);

        // Get all the currencyList where nameRu equals to UPDATED_NAME_RU
        defaultCurrencyShouldNotBeFound("nameRu.in=" + UPDATED_NAME_RU);
    }

    @Test
    @Transactional
    void getAllCurrenciesByNameRuIsNullOrNotNull() throws Exception {
        // Initialize the database
        currencyRepository.saveAndFlush(currency);

        // Get all the currencyList where nameRu is not null
        defaultCurrencyShouldBeFound("nameRu.specified=true");

        // Get all the currencyList where nameRu is null
        defaultCurrencyShouldNotBeFound("nameRu.specified=false");
    }

    @Test
    @Transactional
    void getAllCurrenciesByNameRuContainsSomething() throws Exception {
        // Initialize the database
        currencyRepository.saveAndFlush(currency);

        // Get all the currencyList where nameRu contains DEFAULT_NAME_RU
        defaultCurrencyShouldBeFound("nameRu.contains=" + DEFAULT_NAME_RU);

        // Get all the currencyList where nameRu contains UPDATED_NAME_RU
        defaultCurrencyShouldNotBeFound("nameRu.contains=" + UPDATED_NAME_RU);
    }

    @Test
    @Transactional
    void getAllCurrenciesByNameRuNotContainsSomething() throws Exception {
        // Initialize the database
        currencyRepository.saveAndFlush(currency);

        // Get all the currencyList where nameRu does not contain DEFAULT_NAME_RU
        defaultCurrencyShouldNotBeFound("nameRu.doesNotContain=" + DEFAULT_NAME_RU);

        // Get all the currencyList where nameRu does not contain UPDATED_NAME_RU
        defaultCurrencyShouldBeFound("nameRu.doesNotContain=" + UPDATED_NAME_RU);
    }

    @Test
    @Transactional
    void getAllCurrenciesByRateIsEqualToSomething() throws Exception {
        // Initialize the database
        currencyRepository.saveAndFlush(currency);

        // Get all the currencyList where rate equals to DEFAULT_RATE
        defaultCurrencyShouldBeFound("rate.equals=" + DEFAULT_RATE);

        // Get all the currencyList where rate equals to UPDATED_RATE
        defaultCurrencyShouldNotBeFound("rate.equals=" + UPDATED_RATE);
    }

    @Test
    @Transactional
    void getAllCurrenciesByRateIsNotEqualToSomething() throws Exception {
        // Initialize the database
        currencyRepository.saveAndFlush(currency);

        // Get all the currencyList where rate not equals to DEFAULT_RATE
        defaultCurrencyShouldNotBeFound("rate.notEquals=" + DEFAULT_RATE);

        // Get all the currencyList where rate not equals to UPDATED_RATE
        defaultCurrencyShouldBeFound("rate.notEquals=" + UPDATED_RATE);
    }

    @Test
    @Transactional
    void getAllCurrenciesByRateIsInShouldWork() throws Exception {
        // Initialize the database
        currencyRepository.saveAndFlush(currency);

        // Get all the currencyList where rate in DEFAULT_RATE or UPDATED_RATE
        defaultCurrencyShouldBeFound("rate.in=" + DEFAULT_RATE + "," + UPDATED_RATE);

        // Get all the currencyList where rate equals to UPDATED_RATE
        defaultCurrencyShouldNotBeFound("rate.in=" + UPDATED_RATE);
    }

    @Test
    @Transactional
    void getAllCurrenciesByRateIsNullOrNotNull() throws Exception {
        // Initialize the database
        currencyRepository.saveAndFlush(currency);

        // Get all the currencyList where rate is not null
        defaultCurrencyShouldBeFound("rate.specified=true");

        // Get all the currencyList where rate is null
        defaultCurrencyShouldNotBeFound("rate.specified=false");
    }

    @Test
    @Transactional
    void getAllCurrenciesByRateIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        currencyRepository.saveAndFlush(currency);

        // Get all the currencyList where rate is greater than or equal to DEFAULT_RATE
        defaultCurrencyShouldBeFound("rate.greaterThanOrEqual=" + DEFAULT_RATE);

        // Get all the currencyList where rate is greater than or equal to UPDATED_RATE
        defaultCurrencyShouldNotBeFound("rate.greaterThanOrEqual=" + UPDATED_RATE);
    }

    @Test
    @Transactional
    void getAllCurrenciesByRateIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        currencyRepository.saveAndFlush(currency);

        // Get all the currencyList where rate is less than or equal to DEFAULT_RATE
        defaultCurrencyShouldBeFound("rate.lessThanOrEqual=" + DEFAULT_RATE);

        // Get all the currencyList where rate is less than or equal to SMALLER_RATE
        defaultCurrencyShouldNotBeFound("rate.lessThanOrEqual=" + SMALLER_RATE);
    }

    @Test
    @Transactional
    void getAllCurrenciesByRateIsLessThanSomething() throws Exception {
        // Initialize the database
        currencyRepository.saveAndFlush(currency);

        // Get all the currencyList where rate is less than DEFAULT_RATE
        defaultCurrencyShouldNotBeFound("rate.lessThan=" + DEFAULT_RATE);

        // Get all the currencyList where rate is less than UPDATED_RATE
        defaultCurrencyShouldBeFound("rate.lessThan=" + UPDATED_RATE);
    }

    @Test
    @Transactional
    void getAllCurrenciesByRateIsGreaterThanSomething() throws Exception {
        // Initialize the database
        currencyRepository.saveAndFlush(currency);

        // Get all the currencyList where rate is greater than DEFAULT_RATE
        defaultCurrencyShouldNotBeFound("rate.greaterThan=" + DEFAULT_RATE);

        // Get all the currencyList where rate is greater than SMALLER_RATE
        defaultCurrencyShouldBeFound("rate.greaterThan=" + SMALLER_RATE);
    }

    @Test
    @Transactional
    void getAllCurrenciesBySellRateIsEqualToSomething() throws Exception {
        // Initialize the database
        currencyRepository.saveAndFlush(currency);

        // Get all the currencyList where sellRate equals to DEFAULT_SELL_RATE
        defaultCurrencyShouldBeFound("sellRate.equals=" + DEFAULT_SELL_RATE);

        // Get all the currencyList where sellRate equals to UPDATED_SELL_RATE
        defaultCurrencyShouldNotBeFound("sellRate.equals=" + UPDATED_SELL_RATE);
    }

    @Test
    @Transactional
    void getAllCurrenciesBySellRateIsNotEqualToSomething() throws Exception {
        // Initialize the database
        currencyRepository.saveAndFlush(currency);

        // Get all the currencyList where sellRate not equals to DEFAULT_SELL_RATE
        defaultCurrencyShouldNotBeFound("sellRate.notEquals=" + DEFAULT_SELL_RATE);

        // Get all the currencyList where sellRate not equals to UPDATED_SELL_RATE
        defaultCurrencyShouldBeFound("sellRate.notEquals=" + UPDATED_SELL_RATE);
    }

    @Test
    @Transactional
    void getAllCurrenciesBySellRateIsInShouldWork() throws Exception {
        // Initialize the database
        currencyRepository.saveAndFlush(currency);

        // Get all the currencyList where sellRate in DEFAULT_SELL_RATE or UPDATED_SELL_RATE
        defaultCurrencyShouldBeFound("sellRate.in=" + DEFAULT_SELL_RATE + "," + UPDATED_SELL_RATE);

        // Get all the currencyList where sellRate equals to UPDATED_SELL_RATE
        defaultCurrencyShouldNotBeFound("sellRate.in=" + UPDATED_SELL_RATE);
    }

    @Test
    @Transactional
    void getAllCurrenciesBySellRateIsNullOrNotNull() throws Exception {
        // Initialize the database
        currencyRepository.saveAndFlush(currency);

        // Get all the currencyList where sellRate is not null
        defaultCurrencyShouldBeFound("sellRate.specified=true");

        // Get all the currencyList where sellRate is null
        defaultCurrencyShouldNotBeFound("sellRate.specified=false");
    }

    @Test
    @Transactional
    void getAllCurrenciesBySellRateIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        currencyRepository.saveAndFlush(currency);

        // Get all the currencyList where sellRate is greater than or equal to DEFAULT_SELL_RATE
        defaultCurrencyShouldBeFound("sellRate.greaterThanOrEqual=" + DEFAULT_SELL_RATE);

        // Get all the currencyList where sellRate is greater than or equal to UPDATED_SELL_RATE
        defaultCurrencyShouldNotBeFound("sellRate.greaterThanOrEqual=" + UPDATED_SELL_RATE);
    }

    @Test
    @Transactional
    void getAllCurrenciesBySellRateIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        currencyRepository.saveAndFlush(currency);

        // Get all the currencyList where sellRate is less than or equal to DEFAULT_SELL_RATE
        defaultCurrencyShouldBeFound("sellRate.lessThanOrEqual=" + DEFAULT_SELL_RATE);

        // Get all the currencyList where sellRate is less than or equal to SMALLER_SELL_RATE
        defaultCurrencyShouldNotBeFound("sellRate.lessThanOrEqual=" + SMALLER_SELL_RATE);
    }

    @Test
    @Transactional
    void getAllCurrenciesBySellRateIsLessThanSomething() throws Exception {
        // Initialize the database
        currencyRepository.saveAndFlush(currency);

        // Get all the currencyList where sellRate is less than DEFAULT_SELL_RATE
        defaultCurrencyShouldNotBeFound("sellRate.lessThan=" + DEFAULT_SELL_RATE);

        // Get all the currencyList where sellRate is less than UPDATED_SELL_RATE
        defaultCurrencyShouldBeFound("sellRate.lessThan=" + UPDATED_SELL_RATE);
    }

    @Test
    @Transactional
    void getAllCurrenciesBySellRateIsGreaterThanSomething() throws Exception {
        // Initialize the database
        currencyRepository.saveAndFlush(currency);

        // Get all the currencyList where sellRate is greater than DEFAULT_SELL_RATE
        defaultCurrencyShouldNotBeFound("sellRate.greaterThan=" + DEFAULT_SELL_RATE);

        // Get all the currencyList where sellRate is greater than SMALLER_SELL_RATE
        defaultCurrencyShouldBeFound("sellRate.greaterThan=" + SMALLER_SELL_RATE);
    }

    @Test
    @Transactional
    void getAllCurrenciesByBuyRateIsEqualToSomething() throws Exception {
        // Initialize the database
        currencyRepository.saveAndFlush(currency);

        // Get all the currencyList where buyRate equals to DEFAULT_BUY_RATE
        defaultCurrencyShouldBeFound("buyRate.equals=" + DEFAULT_BUY_RATE);

        // Get all the currencyList where buyRate equals to UPDATED_BUY_RATE
        defaultCurrencyShouldNotBeFound("buyRate.equals=" + UPDATED_BUY_RATE);
    }

    @Test
    @Transactional
    void getAllCurrenciesByBuyRateIsNotEqualToSomething() throws Exception {
        // Initialize the database
        currencyRepository.saveAndFlush(currency);

        // Get all the currencyList where buyRate not equals to DEFAULT_BUY_RATE
        defaultCurrencyShouldNotBeFound("buyRate.notEquals=" + DEFAULT_BUY_RATE);

        // Get all the currencyList where buyRate not equals to UPDATED_BUY_RATE
        defaultCurrencyShouldBeFound("buyRate.notEquals=" + UPDATED_BUY_RATE);
    }

    @Test
    @Transactional
    void getAllCurrenciesByBuyRateIsInShouldWork() throws Exception {
        // Initialize the database
        currencyRepository.saveAndFlush(currency);

        // Get all the currencyList where buyRate in DEFAULT_BUY_RATE or UPDATED_BUY_RATE
        defaultCurrencyShouldBeFound("buyRate.in=" + DEFAULT_BUY_RATE + "," + UPDATED_BUY_RATE);

        // Get all the currencyList where buyRate equals to UPDATED_BUY_RATE
        defaultCurrencyShouldNotBeFound("buyRate.in=" + UPDATED_BUY_RATE);
    }

    @Test
    @Transactional
    void getAllCurrenciesByBuyRateIsNullOrNotNull() throws Exception {
        // Initialize the database
        currencyRepository.saveAndFlush(currency);

        // Get all the currencyList where buyRate is not null
        defaultCurrencyShouldBeFound("buyRate.specified=true");

        // Get all the currencyList where buyRate is null
        defaultCurrencyShouldNotBeFound("buyRate.specified=false");
    }

    @Test
    @Transactional
    void getAllCurrenciesByBuyRateIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        currencyRepository.saveAndFlush(currency);

        // Get all the currencyList where buyRate is greater than or equal to DEFAULT_BUY_RATE
        defaultCurrencyShouldBeFound("buyRate.greaterThanOrEqual=" + DEFAULT_BUY_RATE);

        // Get all the currencyList where buyRate is greater than or equal to UPDATED_BUY_RATE
        defaultCurrencyShouldNotBeFound("buyRate.greaterThanOrEqual=" + UPDATED_BUY_RATE);
    }

    @Test
    @Transactional
    void getAllCurrenciesByBuyRateIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        currencyRepository.saveAndFlush(currency);

        // Get all the currencyList where buyRate is less than or equal to DEFAULT_BUY_RATE
        defaultCurrencyShouldBeFound("buyRate.lessThanOrEqual=" + DEFAULT_BUY_RATE);

        // Get all the currencyList where buyRate is less than or equal to SMALLER_BUY_RATE
        defaultCurrencyShouldNotBeFound("buyRate.lessThanOrEqual=" + SMALLER_BUY_RATE);
    }

    @Test
    @Transactional
    void getAllCurrenciesByBuyRateIsLessThanSomething() throws Exception {
        // Initialize the database
        currencyRepository.saveAndFlush(currency);

        // Get all the currencyList where buyRate is less than DEFAULT_BUY_RATE
        defaultCurrencyShouldNotBeFound("buyRate.lessThan=" + DEFAULT_BUY_RATE);

        // Get all the currencyList where buyRate is less than UPDATED_BUY_RATE
        defaultCurrencyShouldBeFound("buyRate.lessThan=" + UPDATED_BUY_RATE);
    }

    @Test
    @Transactional
    void getAllCurrenciesByBuyRateIsGreaterThanSomething() throws Exception {
        // Initialize the database
        currencyRepository.saveAndFlush(currency);

        // Get all the currencyList where buyRate is greater than DEFAULT_BUY_RATE
        defaultCurrencyShouldNotBeFound("buyRate.greaterThan=" + DEFAULT_BUY_RATE);

        // Get all the currencyList where buyRate is greater than SMALLER_BUY_RATE
        defaultCurrencyShouldBeFound("buyRate.greaterThan=" + SMALLER_BUY_RATE);
    }

    @Test
    @Transactional
    void getAllCurrenciesByDateIsEqualToSomething() throws Exception {
        // Initialize the database
        currencyRepository.saveAndFlush(currency);

        // Get all the currencyList where date equals to DEFAULT_DATE
        defaultCurrencyShouldBeFound("date.equals=" + DEFAULT_DATE);

        // Get all the currencyList where date equals to UPDATED_DATE
        defaultCurrencyShouldNotBeFound("date.equals=" + UPDATED_DATE);
    }

    @Test
    @Transactional
    void getAllCurrenciesByDateIsNotEqualToSomething() throws Exception {
        // Initialize the database
        currencyRepository.saveAndFlush(currency);

        // Get all the currencyList where date not equals to DEFAULT_DATE
        defaultCurrencyShouldNotBeFound("date.notEquals=" + DEFAULT_DATE);

        // Get all the currencyList where date not equals to UPDATED_DATE
        defaultCurrencyShouldBeFound("date.notEquals=" + UPDATED_DATE);
    }

    @Test
    @Transactional
    void getAllCurrenciesByDateIsInShouldWork() throws Exception {
        // Initialize the database
        currencyRepository.saveAndFlush(currency);

        // Get all the currencyList where date in DEFAULT_DATE or UPDATED_DATE
        defaultCurrencyShouldBeFound("date.in=" + DEFAULT_DATE + "," + UPDATED_DATE);

        // Get all the currencyList where date equals to UPDATED_DATE
        defaultCurrencyShouldNotBeFound("date.in=" + UPDATED_DATE);
    }

    @Test
    @Transactional
    void getAllCurrenciesByDateIsNullOrNotNull() throws Exception {
        // Initialize the database
        currencyRepository.saveAndFlush(currency);

        // Get all the currencyList where date is not null
        defaultCurrencyShouldBeFound("date.specified=true");

        // Get all the currencyList where date is null
        defaultCurrencyShouldNotBeFound("date.specified=false");
    }

    @Test
    @Transactional
    void getAllCurrenciesByDateIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        currencyRepository.saveAndFlush(currency);

        // Get all the currencyList where date is greater than or equal to DEFAULT_DATE
        defaultCurrencyShouldBeFound("date.greaterThanOrEqual=" + DEFAULT_DATE);

        // Get all the currencyList where date is greater than or equal to UPDATED_DATE
        defaultCurrencyShouldNotBeFound("date.greaterThanOrEqual=" + UPDATED_DATE);
    }

    @Test
    @Transactional
    void getAllCurrenciesByDateIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        currencyRepository.saveAndFlush(currency);

        // Get all the currencyList where date is less than or equal to DEFAULT_DATE
        defaultCurrencyShouldBeFound("date.lessThanOrEqual=" + DEFAULT_DATE);

        // Get all the currencyList where date is less than or equal to SMALLER_DATE
        defaultCurrencyShouldNotBeFound("date.lessThanOrEqual=" + SMALLER_DATE);
    }

    @Test
    @Transactional
    void getAllCurrenciesByDateIsLessThanSomething() throws Exception {
        // Initialize the database
        currencyRepository.saveAndFlush(currency);

        // Get all the currencyList where date is less than DEFAULT_DATE
        defaultCurrencyShouldNotBeFound("date.lessThan=" + DEFAULT_DATE);

        // Get all the currencyList where date is less than UPDATED_DATE
        defaultCurrencyShouldBeFound("date.lessThan=" + UPDATED_DATE);
    }

    @Test
    @Transactional
    void getAllCurrenciesByDateIsGreaterThanSomething() throws Exception {
        // Initialize the database
        currencyRepository.saveAndFlush(currency);

        // Get all the currencyList where date is greater than DEFAULT_DATE
        defaultCurrencyShouldNotBeFound("date.greaterThan=" + DEFAULT_DATE);

        // Get all the currencyList where date is greater than SMALLER_DATE
        defaultCurrencyShouldBeFound("date.greaterThan=" + SMALLER_DATE);
    }

    @Test
    @Transactional
    void getAllCurrenciesByActiveIsEqualToSomething() throws Exception {
        // Initialize the database
        currencyRepository.saveAndFlush(currency);

        // Get all the currencyList where active equals to DEFAULT_ACTIVE
        defaultCurrencyShouldBeFound("active.equals=" + DEFAULT_ACTIVE);

        // Get all the currencyList where active equals to UPDATED_ACTIVE
        defaultCurrencyShouldNotBeFound("active.equals=" + UPDATED_ACTIVE);
    }

    @Test
    @Transactional
    void getAllCurrenciesByActiveIsNotEqualToSomething() throws Exception {
        // Initialize the database
        currencyRepository.saveAndFlush(currency);

        // Get all the currencyList where active not equals to DEFAULT_ACTIVE
        defaultCurrencyShouldNotBeFound("active.notEquals=" + DEFAULT_ACTIVE);

        // Get all the currencyList where active not equals to UPDATED_ACTIVE
        defaultCurrencyShouldBeFound("active.notEquals=" + UPDATED_ACTIVE);
    }

    @Test
    @Transactional
    void getAllCurrenciesByActiveIsInShouldWork() throws Exception {
        // Initialize the database
        currencyRepository.saveAndFlush(currency);

        // Get all the currencyList where active in DEFAULT_ACTIVE or UPDATED_ACTIVE
        defaultCurrencyShouldBeFound("active.in=" + DEFAULT_ACTIVE + "," + UPDATED_ACTIVE);

        // Get all the currencyList where active equals to UPDATED_ACTIVE
        defaultCurrencyShouldNotBeFound("active.in=" + UPDATED_ACTIVE);
    }

    @Test
    @Transactional
    void getAllCurrenciesByActiveIsNullOrNotNull() throws Exception {
        // Initialize the database
        currencyRepository.saveAndFlush(currency);

        // Get all the currencyList where active is not null
        defaultCurrencyShouldBeFound("active.specified=true");

        // Get all the currencyList where active is null
        defaultCurrencyShouldNotBeFound("active.specified=false");
    }

    /**
     * Executes the search, and checks that the default entity is returned.
     */
    private void defaultCurrencyShouldBeFound(String filter) throws Exception {
        restCurrencyMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(currency.getId().intValue())))
            .andExpect(jsonPath("$.[*].code").value(hasItem(DEFAULT_CODE)))
            .andExpect(jsonPath("$.[*].ccy").value(hasItem(DEFAULT_CCY)))
            .andExpect(jsonPath("$.[*].nameRu").value(hasItem(DEFAULT_NAME_RU)))
            .andExpect(jsonPath("$.[*].rate").value(hasItem(sameNumber(DEFAULT_RATE))))
            .andExpect(jsonPath("$.[*].sellRate").value(hasItem(sameNumber(DEFAULT_SELL_RATE))))
            .andExpect(jsonPath("$.[*].buyRate").value(hasItem(sameNumber(DEFAULT_BUY_RATE))))
            .andExpect(jsonPath("$.[*].date").value(hasItem(DEFAULT_DATE.toString())))
            .andExpect(jsonPath("$.[*].active").value(hasItem(DEFAULT_ACTIVE.booleanValue())));

        // Check, that the count call also returns 1
        restCurrencyMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("1"));
    }

    /**
     * Executes the search, and checks that the default entity is not returned.
     */
    private void defaultCurrencyShouldNotBeFound(String filter) throws Exception {
        restCurrencyMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty());

        // Check, that the count call also returns 0
        restCurrencyMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("0"));
    }

    @Test
    @Transactional
    void getNonExistingCurrency() throws Exception {
        // Get the currency
        restCurrencyMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putNewCurrency() throws Exception {
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
            .active(UPDATED_ACTIVE);
        CurrencyDTO currencyDTO = currencyMapper.toDto(updatedCurrency);

        restCurrencyMockMvc
            .perform(
                put(ENTITY_API_URL_ID, currencyDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(currencyDTO))
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
        assertThat(testCurrency.getActive()).isEqualTo(UPDATED_ACTIVE);
    }

    @Test
    @Transactional
    void putNonExistingCurrency() throws Exception {
        int databaseSizeBeforeUpdate = currencyRepository.findAll().size();
        currency.setId(count.incrementAndGet());

        // Create the Currency
        CurrencyDTO currencyDTO = currencyMapper.toDto(currency);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restCurrencyMockMvc
            .perform(
                put(ENTITY_API_URL_ID, currencyDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(currencyDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Currency in the database
        List<Currency> currencyList = currencyRepository.findAll();
        assertThat(currencyList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchCurrency() throws Exception {
        int databaseSizeBeforeUpdate = currencyRepository.findAll().size();
        currency.setId(count.incrementAndGet());

        // Create the Currency
        CurrencyDTO currencyDTO = currencyMapper.toDto(currency);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restCurrencyMockMvc
            .perform(
                put(ENTITY_API_URL_ID, count.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(currencyDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Currency in the database
        List<Currency> currencyList = currencyRepository.findAll();
        assertThat(currencyList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamCurrency() throws Exception {
        int databaseSizeBeforeUpdate = currencyRepository.findAll().size();
        currency.setId(count.incrementAndGet());

        // Create the Currency
        CurrencyDTO currencyDTO = currencyMapper.toDto(currency);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restCurrencyMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(currencyDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Currency in the database
        List<Currency> currencyList = currencyRepository.findAll();
        assertThat(currencyList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateCurrencyWithPatch() throws Exception {
        // Initialize the database
        currencyRepository.saveAndFlush(currency);

        int databaseSizeBeforeUpdate = currencyRepository.findAll().size();

        // Update the currency using partial update
        Currency partialUpdatedCurrency = new Currency();
        partialUpdatedCurrency.setId(currency.getId());

        partialUpdatedCurrency.ccy(UPDATED_CCY).rate(UPDATED_RATE).sellRate(UPDATED_SELL_RATE).active(UPDATED_ACTIVE);

        restCurrencyMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedCurrency.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedCurrency))
            )
            .andExpect(status().isOk());

        // Validate the Currency in the database
        List<Currency> currencyList = currencyRepository.findAll();
        assertThat(currencyList).hasSize(databaseSizeBeforeUpdate);
        Currency testCurrency = currencyList.get(currencyList.size() - 1);
        assertThat(testCurrency.getCode()).isEqualTo(DEFAULT_CODE);
        assertThat(testCurrency.getCcy()).isEqualTo(UPDATED_CCY);
        assertThat(testCurrency.getNameRu()).isEqualTo(DEFAULT_NAME_RU);
        assertThat(testCurrency.getRate()).isEqualByComparingTo(UPDATED_RATE);
        assertThat(testCurrency.getSellRate()).isEqualByComparingTo(UPDATED_SELL_RATE);
        assertThat(testCurrency.getBuyRate()).isEqualByComparingTo(DEFAULT_BUY_RATE);
        assertThat(testCurrency.getDate()).isEqualTo(DEFAULT_DATE);
        assertThat(testCurrency.getActive()).isEqualTo(UPDATED_ACTIVE);
    }

    @Test
    @Transactional
    void fullUpdateCurrencyWithPatch() throws Exception {
        // Initialize the database
        currencyRepository.saveAndFlush(currency);

        int databaseSizeBeforeUpdate = currencyRepository.findAll().size();

        // Update the currency using partial update
        Currency partialUpdatedCurrency = new Currency();
        partialUpdatedCurrency.setId(currency.getId());

        partialUpdatedCurrency
            .code(UPDATED_CODE)
            .ccy(UPDATED_CCY)
            .nameRu(UPDATED_NAME_RU)
            .rate(UPDATED_RATE)
            .sellRate(UPDATED_SELL_RATE)
            .buyRate(UPDATED_BUY_RATE)
            .date(UPDATED_DATE)
            .active(UPDATED_ACTIVE);

        restCurrencyMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedCurrency.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedCurrency))
            )
            .andExpect(status().isOk());

        // Validate the Currency in the database
        List<Currency> currencyList = currencyRepository.findAll();
        assertThat(currencyList).hasSize(databaseSizeBeforeUpdate);
        Currency testCurrency = currencyList.get(currencyList.size() - 1);
        assertThat(testCurrency.getCode()).isEqualTo(UPDATED_CODE);
        assertThat(testCurrency.getCcy()).isEqualTo(UPDATED_CCY);
        assertThat(testCurrency.getNameRu()).isEqualTo(UPDATED_NAME_RU);
        assertThat(testCurrency.getRate()).isEqualByComparingTo(UPDATED_RATE);
        assertThat(testCurrency.getSellRate()).isEqualByComparingTo(UPDATED_SELL_RATE);
        assertThat(testCurrency.getBuyRate()).isEqualByComparingTo(UPDATED_BUY_RATE);
        assertThat(testCurrency.getDate()).isEqualTo(UPDATED_DATE);
        assertThat(testCurrency.getActive()).isEqualTo(UPDATED_ACTIVE);
    }

    @Test
    @Transactional
    void patchNonExistingCurrency() throws Exception {
        int databaseSizeBeforeUpdate = currencyRepository.findAll().size();
        currency.setId(count.incrementAndGet());

        // Create the Currency
        CurrencyDTO currencyDTO = currencyMapper.toDto(currency);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restCurrencyMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, currencyDTO.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(currencyDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Currency in the database
        List<Currency> currencyList = currencyRepository.findAll();
        assertThat(currencyList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchCurrency() throws Exception {
        int databaseSizeBeforeUpdate = currencyRepository.findAll().size();
        currency.setId(count.incrementAndGet());

        // Create the Currency
        CurrencyDTO currencyDTO = currencyMapper.toDto(currency);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restCurrencyMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, count.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(currencyDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Currency in the database
        List<Currency> currencyList = currencyRepository.findAll();
        assertThat(currencyList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamCurrency() throws Exception {
        int databaseSizeBeforeUpdate = currencyRepository.findAll().size();
        currency.setId(count.incrementAndGet());

        // Create the Currency
        CurrencyDTO currencyDTO = currencyMapper.toDto(currency);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restCurrencyMockMvc
            .perform(
                patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(TestUtil.convertObjectToJsonBytes(currencyDTO))
            )
            .andExpect(status().isMethodNotAllowed());

        // Validate the Currency in the database
        List<Currency> currencyList = currencyRepository.findAll();
        assertThat(currencyList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteCurrency() throws Exception {
        // Initialize the database
        currencyRepository.saveAndFlush(currency);

        int databaseSizeBeforeDelete = currencyRepository.findAll().size();

        // Delete the currency
        restCurrencyMockMvc
            .perform(delete(ENTITY_API_URL_ID, currency.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        List<Currency> currencyList = currencyRepository.findAll();
        assertThat(currencyList).hasSize(databaseSizeBeforeDelete - 1);
    }
}
