package uz.paykap.web.rest;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import uz.paykap.IntegrationTest;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import uz.devops.bm.dummy.adapter.DummyDataAdapterV3;
import uz.devops.bm.dummy.gateway.DummyGatewayServiceV3;
import uz.devops.bmcore.dto.request.CheckRequest;
import uz.devops.bmcore.dto.request.CommonCheckRequest;
import uz.devops.bmcore.dto.request.CommonPayRequest;
import uz.devops.bmcore.dto.request.PayRequest;
import uz.devops.bmcore.dto.response.SimpleBillResponse;
import uz.devops.bmcore.enumeration.PayType;
import uz.devops.bmcore.enumeration.TransactionStatus;
import uz.devops.bmcore.util.FileStorageService;
import uz.javlon.svgate.repository.BankRepository;
import uz.paykap.PkbillmsApp;
import uz.paykap.adapter.DummyAdapterDelegate;
import uz.paykap.agent.tran.CachedCbuService;
import uz.paykap.agent.tran.CommonTranServiceV3;
import uz.paykap.config.ApplicationProperties;
import uz.paykap.IntegrationTest;
import uz.paykap.domain.Gateway;
import uz.paykap.domain.Recipient;
import uz.paykap.domain.Transaction;
import uz.paykap.gateway.uzcard.UzcardDebitGatewayService;
import uz.paykap.repository.GatewayRepository;
import uz.paykap.repository.RecipientRepository;
import uz.paykap.repository.TransactionRepository;
import uz.paykap.service.AllowedAgentService;
import uz.paykap.service.DepositService;
import uz.paykap.service.GatewayService;
import uz.paykap.service.dto.SimpleDepositDTO;
import uz.paykap.service.mapper.BankMapper;
import uz.paykap.web.rest.errors.ExceptionTranslator;
import uz.paykap.web.rest.util.DummyUtils;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uz.paykap.web.rest.TestUtil.createFormattingConversionService;

@RunWith(SpringRunner.class)
@IntegrationTest
public class CommonAgentResourceTest {

    public static final String DUMMY_ACCOUNT = "974415655";
    public static final long DUMMY_AMOUNT = 3000000L;
    public static final String TRAN_ID = "TRAN_ID";
    public static final int EXPECTED_SYSTEM_FEE = 59400;

    @Autowired
    private CommonTranServiceV3 commonTranServiceV2;
    @Autowired
    private ApplicationProperties properties;

    @Autowired
    private GatewayService gatewayService;
    @Autowired
    private DummyAdapterDelegate dummyAdapterDelegate;
    @Autowired
    private GatewayRepository gatewayRepository;

    //    @Test // WORK OK in postgresql
    @WithMockUser(username = DummyDataAdapterV3.DUMMY_AGENT)
    public void testParallelDeposit() throws Exception {

        List<Gateway> gateways = gatewayRepository.findAll();

        assertThat(gateways.isEmpty()).isTrue();

        DummyUtils.prepareDummyData(dummyAdapterDelegate);

        gateways = gatewayRepository.findAll();

        assertThat(gateways.size()).isEqualTo(1);

        Gateway gateway = gateways.get(0);

        SimpleDepositDTO agentDeposit = depositService.getAgentDeposit();

        long beforeBalance = agentDeposit.getBalance();

        int count = 200;

        CountDownLatch latch = new CountDownLatch(count);
        AtomicInteger atomicIntegerSuccess = new AtomicInteger(0);
        AtomicInteger atomicIntegerErrors = new AtomicInteger(0);

        long start = System.currentTimeMillis();

        for (long i = 0; i < count; i++) {
            long finalI = i;
            new Thread(() -> {
//                System.out.println("START : " + finalI);
                try {
                    depositService.increaseAgentDeposit(DummyDataAdapterV3.DUMMY_AGENT, 1L);
                    System.out.println("DONE : " + finalI);
                    atomicIntegerSuccess.incrementAndGet();
                } catch (Exception e) {
                    System.out.println("ERROR : " + finalI + " : " + e.getLocalizedMessage());
                    atomicIntegerErrors.incrementAndGet();
                }

                latch.countDown();
            }).start();
        }

        latch.await(10, TimeUnit.SECONDS);

        assertThat(atomicIntegerSuccess.get()).isEqualTo(count);
        assertThat(atomicIntegerErrors.get()).isEqualTo(0);

        agentDeposit = depositService.getAgentDeposit();

        assertThat(agentDeposit.getBalance()).isEqualTo(beforeBalance + count);


        gatewayService.deleteCascaded(gateway.getId());
        gateways = gatewayRepository.findAll();
        assertThat(gateways.isEmpty()).isTrue();

        System.out.println("Done : " + (System.currentTimeMillis() - start));
    }

    @Test
    @Transactional
    @WithMockUser(username = DummyDataAdapterV3.DUMMY_AGENT)
    public void testGetAllData() throws Exception {


        DummyUtils.prepareDummyData(dummyAdapterDelegate);

        MvcResult mvcResult = restAgentMockMvc.perform(
                        get("/api/agent/recipients")
//                                .content(TestUtil.convertObjectToJsonBytes(payDTO))
//                                .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
                )
                .andExpect(status().isOk())
                .andReturn();

        String contentAsString = mvcResult.getResponse().getContentAsString();

        System.out.println(contentAsString);


    }

    @Test
    @Transactional
    @WithMockUser(username = DummyDataAdapterV3.DUMMY_AGENT)
    public void testCheck() throws Exception {

        int databaseSizeBeforeCreate = transactionRepository.findAll().size();

        Instant instant = Instant.now();

        CheckRequest simpleCheckDTO = getSimpleCheckDTO();

        SimpleBillResponse simpleBillDTO = getSimpleBillDTO(simpleCheckDTO);

        assertThat(simpleBillDTO).isNotNull();
        assertThat(simpleBillDTO.isSuccess()).isTrue();
        assertThat(simpleBillDTO.getAmount()).isEqualTo(simpleCheckDTO.getAmount());
        assertThat(simpleBillDTO.getAccount()).isEqualTo(simpleCheckDTO.getAccount());

        List<Transaction> transactionList = transactionRepository.findAll();
        assertThat(transactionList).hasSize(databaseSizeBeforeCreate + 1);
        Transaction testTransaction = transactionList.get(transactionList.size() - 1);

        assertThat(simpleBillDTO.getId()).isEqualTo(testTransaction.getId());

        testCheckTransactionCommissionDown(testTransaction, instant, simpleCheckDTO, DummyDataAdapterV3.DUMMY_AGENT);
    }

    @Test
    @Transactional
    @WithMockUser(username = DummyDataAdapterV3.DUMMY_AGENT)
    public void testPay() throws Exception {

        int databaseSizeBeforeCreate = transactionRepository.findAll().size();

        Instant instant = Instant.now();

        CheckRequest simpleCheckDTO = getSimpleCheckDTO();

        SimpleBillResponse simpleBillDTO = getSimpleBillDTO(simpleCheckDTO);

        assertThat(simpleBillDTO).isNotNull();
        assertThat(simpleBillDTO.isSuccess()).isTrue();
        assertThat(simpleBillDTO.getAmount()).isEqualTo(simpleCheckDTO.getAmount());
        assertThat(simpleBillDTO.getAccount()).isEqualTo(simpleCheckDTO.getAccount());

        CommonPayRequest payDTO = new PayRequest();
//        payDTO.setAmount(DUMMY_AMOUNT);
        payDTO.setGatewayAmount(30000.03);
        payDTO.setBillId(simpleBillDTO.getId());

        MvcResult mvcResult = restAgentMockMvc.perform(
            post("/api/agent/pay")
                .content(TestUtil.convertObjectToJsonBytes(payDTO))
                .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
        )
            .andExpect(status().isOk())
            .andReturn();

        simpleBillDTO = jacksonMessageConverter.getObjectMapper().readValue(mvcResult.getResponse().getContentAsByteArray(), SimpleBillResponse.class);

        assertThat(simpleBillDTO).isNotNull();
        assertThat(simpleBillDTO.isSuccess()).isTrue();
        assertThat(simpleBillDTO.getAmount()).isEqualTo(DUMMY_AMOUNT);
        assertThat(simpleBillDTO.getAccount()).isEqualTo(DUMMY_ACCOUNT);

        List<Transaction> transactionList = transactionRepository.findAll();
        assertThat(transactionList).hasSize(databaseSizeBeforeCreate + 1);
        Transaction testTransaction = transactionList.get(transactionList.size() - 1);

        testPayTransaction(testTransaction, instant, simpleCheckDTO, DummyDataAdapterV3.DUMMY_AGENT);
    }

    @Test
    @Transactional
    @WithMockUser(username = DummyDataAdapterV3.DUMMY_AGENT)
    public void testCheckWithoutAmountAndPayWithAmount() throws Exception {

        int databaseSizeBeforeCreate = transactionRepository.findAll().size();

        Instant instant = Instant.now();

        CheckRequest simpleCheckDTO = getSimpleCheckDTO();
        simpleCheckDTO.setAmount(null);

        SimpleBillResponse simpleBillDTO = getSimpleBillDTOv2(simpleCheckDTO);

        assertThat(simpleBillDTO).isNotNull();
        assertThat(simpleBillDTO.isSuccess()).isTrue();
        assertThat(simpleBillDTO.getAmount()).isEqualTo(simpleCheckDTO.getAmount());
        assertThat(simpleBillDTO.getAccount()).isEqualTo(simpleCheckDTO.getAccount());

        CommonPayRequest payDTO = new PayRequest();
        payDTO.setAmount(DUMMY_AMOUNT);
        payDTO.setBillId(simpleBillDTO.getId());

        MvcResult mvcResult = restAgentMockMvc.perform(
            post("/api/agent/pay/v2")
                .content(TestUtil.convertObjectToJsonBytes(payDTO))
                .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
        )
            .andExpect(status().isOk())
            .andReturn();

        simpleBillDTO = jacksonMessageConverter.getObjectMapper().readValue(mvcResult.getResponse().getContentAsByteArray(), SimpleBillResponse.class);

        assertThat(simpleBillDTO).isNotNull();
        assertThat(simpleBillDTO.isSuccess()).isTrue();
        assertThat(simpleBillDTO.getAmount()).isEqualTo(DUMMY_AMOUNT);
        assertThat(simpleBillDTO.getAccount()).isEqualTo(DUMMY_ACCOUNT);

        List<Transaction> transactionList = transactionRepository.findAll();
        assertThat(transactionList).hasSize(databaseSizeBeforeCreate + 1);
        Transaction testTransaction = transactionList.get(transactionList.size() - 1);

        testPayTransactionV2(testTransaction, instant, simpleCheckDTO, DummyDataAdapterV3.DUMMY_AGENT, TransactionStatus.SUCCESS, DUMMY_AMOUNT, 210000, 2790000);
    }

    @Test
    @Transactional
    @WithMockUser(username = DummyDataAdapterV3.DUMMY_KIOSK_AGENT)
    public void testCheckWithoutAmountAndPayWithAmount4Cash() throws Exception {

        int databaseSizeBeforeCreate = transactionRepository.findAll().size();

        Instant instant = Instant.now();

        CheckRequest simpleCheckDTO = getSimpleCheckDTO();
        simpleCheckDTO.setAmount(null);

        SimpleBillResponse simpleBillDTO = getSimpleBillDTOv2(simpleCheckDTO);

        depositService.increaseAgentDeposit(DummyDataAdapterV3.DUMMY_KIOSK_AGENT, DUMMY_AMOUNT * 2);

        assertThat(simpleBillDTO).isNotNull();
        assertThat(simpleBillDTO.isSuccess()).isTrue();
        assertThat(simpleBillDTO.getAmount()).isEqualTo(simpleCheckDTO.getAmount());
        assertThat(simpleBillDTO.getAccount()).isEqualTo(simpleCheckDTO.getAccount());

        CommonPayRequest payDTO = new PayRequest();
        payDTO.setAmount(DUMMY_AMOUNT);
        payDTO.setBillId(simpleBillDTO.getId());
        payDTO.setType(PayType.CASH);

        MvcResult mvcResult = restAgentMockMvc.perform(
            post("/api/agent/pay/v2")
                .content(TestUtil.convertObjectToJsonBytes(payDTO))
                .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
        )
            .andExpect(status().isOk())
            .andReturn();

        simpleBillDTO = jacksonMessageConverter.getObjectMapper().readValue(mvcResult.getResponse().getContentAsByteArray(), SimpleBillResponse.class);

        assertThat(simpleBillDTO).isNotNull();
        assertThat(simpleBillDTO.isSuccess()).isTrue();
        assertThat(simpleBillDTO.getAmount()).isEqualTo(DUMMY_AMOUNT);
        assertThat(simpleBillDTO.getAccount()).isEqualTo(DUMMY_ACCOUNT);

        List<Transaction> transactionList = transactionRepository.findAll();
        assertThat(transactionList).hasSize(databaseSizeBeforeCreate + 1);
        Transaction testTransaction = transactionList.get(transactionList.size() - 1);

        testPayTransactionV2(testTransaction, instant, simpleCheckDTO, DummyDataAdapterV3.DUMMY_KIOSK_AGENT, TransactionStatus.SUCCESS, DUMMY_AMOUNT, 240000, 2760000);
    }

    @Test
    @Transactional
    @WithMockUser(username = DummyDataAdapterV3.DUMMY_KIOSK_AGENT)
    public void testCheckForCommissionUpAgent() throws Exception {

        int databaseSizeBeforeCreate = transactionRepository.findAll().size();

        Instant instant = Instant.now();

        CheckRequest simpleCheckDTO = getSimpleCheckDTO();

        SimpleBillResponse simpleBillDTO = getSimpleBillDTO(simpleCheckDTO);

        assertThat(simpleBillDTO).isNotNull();
        assertThat(simpleBillDTO.isSuccess()).isTrue();
        assertThat(simpleBillDTO.getAmount()).isEqualTo(simpleCheckDTO.getAmount());
        assertThat(simpleBillDTO.getAccount()).isEqualTo(simpleCheckDTO.getAccount());

        List<Transaction> transactionList = transactionRepository.findAll();
        assertThat(transactionList).hasSize(databaseSizeBeforeCreate + 1);
        Transaction testTransaction = transactionList.get(transactionList.size() - 1);

        assertThat(simpleBillDTO.getId()).isEqualTo(testTransaction.getId());

        testCheckTransactionCommissionUp(testTransaction, instant, simpleCheckDTO, DummyDataAdapterV3.DUMMY_KIOSK_AGENT);
    }

    public void testCheckTransactionCommissionDown(Transaction testTransaction, Instant beforeTranInstant, CheckRequest checkDTO, String agentLogin) {

        assertThat(testTransaction.getRequestLogin()).isEqualTo(agentLogin);
        assertThat(testTransaction.getRequestTrId()).isEqualTo(TRAN_ID);
        assertThat(testTransaction.getRequestGroup()).isEqualTo(DummyDataAdapterV3.DUMMY_AGENT);
        assertThat(testTransaction.getComment()).isNotNull();
        assertThat(testTransaction.getAgentFeeAmount()).isEqualTo(150000);

        assertThat(testTransaction.getSystemFeeAmount()).isEqualTo(55800);
        assertThat(testTransaction.getSystemCurrency()).isEqualTo(860);
        assertThat(testTransaction.getSystemFee()).isEqualTo(2.0);
        assertThat(testTransaction.getStatus()).isEqualTo(TransactionStatus.CHECK_OK);
        assertThat(testTransaction.getGatewayAmount()).isEqualTo(3.58);
//        assertThat(testTransaction.getRoundAmount()).isEqualTo(0.00847129);
        assertThat(testTransaction.getRoundAmount()).isEqualTo(0.0);
        assertThat(testTransaction.getDate()).isAfter(beforeTranInstant);
        assertThat(testTransaction.getCcyRate()).isEqualTo(cbuService.getCurrencyRate(DummyDataAdapterV3.CURRENCY));

        assertThat(testTransaction.getRecipientAccount()).isEqualTo(checkDTO.getAccount());
        assertThat(testTransaction.getRecipientAmount()).isEqualTo(2790000);
        assertThat(testTransaction.getRecipientCcy()).isEqualTo(DummyDataAdapterV3.CURRENCY);
        assertThat(testTransaction.getRecipientGateway()).isEqualTo(DummyDataAdapterV3.DUMMY_GATEWAY_NAME);
        assertThat(testTransaction.getRecipientDate()).isAfter(beforeTranInstant);
        assertThat(testTransaction.getRecipientStatus()).isEqualTo(DummyGatewayServiceV3.RESPONSE_CODE);
        assertThat(testTransaction.getRecipientStatusText()).isEqualTo(DummyGatewayServiceV3.RESPONSE_TEXT);
        assertThat(testTransaction.isRecipientSuccess()).isEqualTo(true);
        assertThat(testTransaction.getRecipientBillId()).isNotNull();
        assertThat(testTransaction.getRecipientTrId()).isEqualTo(DummyGatewayServiceV3.EXT_CHECK_ID);
        assertThat(testTransaction.getRecipientServiceId()).isEqualTo(checkDTO.getRecipientId());

        assertThat(testTransaction.getSenderServiceId()).isEqualTo(0L);
//        assertThat(testTransaction.getSenderGateway()).isEqualTo(null);
//        assertThat(testTransaction.getSenderAccount()).isEqualTo(null);
        assertThat(testTransaction.getSenderAmount()).isEqualTo(3000000L);
        assertThat(testTransaction.getSystemCommissionUpAmount()).isEqualTo(0L);
        assertThat(testTransaction.getSystemCommissionDownAmount()).isEqualTo(210000L);
//        assertThat(testTransaction.getSenderCcy()).isEqualTo(null);
//        assertThat(testTransaction.getSenderTrId()).isEqualTo(null);
        assertThat(testTransaction.isSenderSuccess()).isEqualTo(null);
        assertThat(testTransaction.getSenderDate()).isNull();
        assertThat(testTransaction.getSenderStatus()).isEqualTo(null);
        assertThat(testTransaction.getSenderStatusText()).isEqualTo(null);
        assertThat(testTransaction.getSenderBillId()).isEqualTo(0L);
    }

    //    @Test
//    @Transactional
//    @WithMockUser(username = DummyDataAdapterV3.DUMMY_AGENT)
    public void testPayLowDepositError() throws Exception {

        int databaseSizeBeforeCreate = transactionRepository.findAll().size();

//        Instant instant = Instant.now();

        CommonCheckRequest simpleCheckDTO = getSimpleCheckDTO(10L);

        SimpleBillResponse simpleBillDTO = getSimpleBillDTO(simpleCheckDTO);

        assertThat(simpleBillDTO).isNotNull();
        assertThat(simpleBillDTO.isSuccess()).isTrue();
        assertThat(simpleBillDTO.getAmount()).isEqualTo(simpleCheckDTO.getAmount());
        assertThat(simpleBillDTO.getAccount()).isEqualTo(simpleCheckDTO.getAccount());

        CommonPayRequest payDTO = new PayRequest();
//        payDTO.setAmount(10L);
        payDTO.setBillId(simpleBillDTO.getId());

        MvcResult mvcResult = restAgentMockMvc.perform(
            post("/api/agent/pay")
                .content(TestUtil.convertObjectToJsonBytes(payDTO))
                .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
        )
            .andExpect(status().isBadRequest())
            .andReturn();

        List<Transaction> transactionList = transactionRepository.findAll();
        assertThat(transactionList).hasSize(databaseSizeBeforeCreate + 1);
        Transaction testTransaction = transactionList.get(transactionList.size() - 1);

//        testPayTransaction(testTransaction, instant, simpleCheckDTO, DummyDataAdapterV3.DUMMY_AGENT, TransactionStatus.CHECK_OK);
    }

    public void testPayTransaction(Transaction testTransaction, Instant beforeTranInstant, CheckRequest checkDTO, String agentLogin) {
        testPayTransaction(testTransaction, beforeTranInstant, checkDTO, agentLogin, TransactionStatus.SUCCESS);
    }

    public void testCheckTransactionCommissionUp(Transaction testTransaction, Instant beforeTranInstant, CheckRequest checkDTO, String agentLogin) {

        assertThat(testTransaction.getRequestLogin()).isEqualTo(agentLogin);
        assertThat(testTransaction.getRequestTrId()).isEqualTo(TRAN_ID);
        assertThat(testTransaction.getRequestGroup()).isEqualTo(DummyDataAdapterV3.DUMMY_KIOSK_AGENT);
        assertThat(testTransaction.getComment()).isNotNull();
        assertThat(testTransaction.getAgentFeeAmount()).isEqualTo(150000);

        assertThat(testTransaction.getSystemFeeAmount()).isEqualTo(EXPECTED_SYSTEM_FEE);
        assertThat(testTransaction.getSystemCurrency()).isEqualTo(860);
        assertThat(testTransaction.getSystemFee()).isEqualTo(2.0);
        assertThat(testTransaction.getStatus()).isEqualTo(TransactionStatus.CHECK_OK);
        assertThat(testTransaction.getGatewayAmount()).isEqualTo(3.81);
//        assertThat(testTransaction.getRoundAmount()).isEqualTo(0.005453);
        assertThat(testTransaction.getRoundAmount()).isEqualTo(0.0);
        assertThat(testTransaction.getDate()).isAfter(beforeTranInstant);
        assertThat(testTransaction.getCcyRate()).isEqualTo(cbuService.getCurrencyRate(DummyDataAdapterV3.CURRENCY));

        assertThat(testTransaction.getRecipientAccount()).isEqualTo(checkDTO.getAccount());
        assertThat(testTransaction.getRecipientAmount()).isEqualTo(2970000L);
        assertThat(testTransaction.getRecipientCcy()).isEqualTo(DummyDataAdapterV3.CURRENCY);
        assertThat(testTransaction.getRecipientGateway()).isEqualTo(DummyDataAdapterV3.DUMMY_GATEWAY_NAME);
        assertThat(testTransaction.getRecipientDate()).isAfter(beforeTranInstant);
        assertThat(testTransaction.getRecipientStatus()).isEqualTo(DummyGatewayServiceV3.RESPONSE_CODE);
        assertThat(testTransaction.getRecipientStatusText()).isEqualTo(DummyGatewayServiceV3.RESPONSE_TEXT);
        assertThat(testTransaction.isRecipientSuccess()).isEqualTo(true);
        assertThat(testTransaction.getRecipientBillId()).isNotNull();
        assertThat(testTransaction.getRecipientTrId()).isEqualTo(DummyGatewayServiceV3.EXT_CHECK_ID);
        assertThat(testTransaction.getRecipientServiceId()).isEqualTo(checkDTO.getRecipientId());

        assertThat(testTransaction.getSenderServiceId()).isEqualTo(0L);
//        assertThat(testTransaction.getSenderGateway()).isEqualTo(null);
//        assertThat(testTransaction.getSenderAccount()).isEqualTo(null);
        assertThat(testTransaction.getSenderAmount()).isEqualTo(3210000L);
        assertThat(testTransaction.getSystemCommissionUpAmount()).isEqualTo(210000L);
        assertThat(testTransaction.getSystemCommissionDownAmount()).isEqualTo(30000L);
//        assertThat(testTransaction.getSenderCcy()).isEqualTo(null);
//        assertThat(testTransaction.getSenderTrId()).isEqualTo(null);
        assertThat(testTransaction.isSenderSuccess()).isEqualTo(null);
        assertThat(testTransaction.getSenderDate()).isNull();
        assertThat(testTransaction.getSenderStatus()).isEqualTo(null);
        assertThat(testTransaction.getSenderStatusText()).isEqualTo(null);
        assertThat(testTransaction.getSenderBillId()).isEqualTo(0L);
    }

    private SimpleBillResponse getSimpleBillDTO(CommonCheckRequest commonCheckDTO) throws Exception {
        DummyUtils.prepareDummyData(dummyAdapterDelegate);

        depositService.increaseAgentDeposit(DummyDataAdapterV3.DUMMY_AGENT, 10000000L);

        Optional<Recipient> recipient = recipientRepository.findTop1ByShortName(DummyDataAdapterV3.DUMMY_RECIPIENT_USD);

        assertThat(recipient.isPresent()).isTrue();
        Long recipientId = recipient.get().getId();

        commonCheckDTO = commonCheckDTO == null ? getSimpleCheckDTO() : commonCheckDTO;

        commonCheckDTO.setRecipientId(recipientId);

        MvcResult mvcResult = restAgentMockMvc.perform(
            post("/api/agent/check")
                .content(TestUtil.convertObjectToJsonBytes(commonCheckDTO))
                .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
        )
            .andExpect(status().isOk())
            .andReturn();

        assertThat(mvcResult.getResponse().getContentAsString()).isNotEmpty();

        return jacksonMessageConverter.getObjectMapper().readValue(mvcResult.getResponse().getContentAsByteArray(), SimpleBillResponse.class);
    }

    private SimpleBillResponse getSimpleBillDTOv2(CommonCheckRequest commonCheckDTO) throws Exception {

        DummyUtils.prepareDummyData(dummyAdapterDelegate);

        depositService.increaseAgentDeposit(DummyDataAdapterV3.DUMMY_AGENT, 10000000L);

        Optional<Recipient> recipient = recipientRepository.findTop1ByShortName(DummyDataAdapterV3.DUMMY_RECIPIENT_USD);

        assertThat(recipient.isPresent()).isTrue();
        Long recipientId = recipient.get().getId();

        commonCheckDTO = commonCheckDTO == null ? getSimpleCheckDTO() : commonCheckDTO;

        commonCheckDTO.setRecipientId(recipientId);

        MvcResult mvcResult = restAgentMockMvc.perform(
            post("/api/agent/check/v2")
                .content(TestUtil.convertObjectToJsonBytes(commonCheckDTO))
                .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
        )
            .andExpect(status().isOk())
            .andReturn();

        assertThat(mvcResult.getResponse().getContentAsString()).isNotEmpty();

        return jacksonMessageConverter.getObjectMapper().readValue(mvcResult.getResponse().getContentAsByteArray(), SimpleBillResponse.class);
    }

    private CheckRequest getSimpleCheckDTO() {
        return getSimpleCheckDTO(DUMMY_AMOUNT);
    }

    public void testPayTransaction(Transaction testTransaction, Instant beforeTranInstant, CheckRequest checkDTO, String agentLogin, TransactionStatus status) {

        assertThat(testTransaction.getRequestLogin()).isEqualTo(agentLogin);
        assertThat(testTransaction.getRequestTrId()).isEqualTo(TRAN_ID);
        assertThat(testTransaction.getRequestGroup()).isEqualTo(DummyDataAdapterV3.DUMMY_AGENT);
        assertThat(testTransaction.getComment()).isNotNull();

//        assertThat(testTransaction.getSystemFeeAmount()).isEqualTo(DEFAULT_SYSTEM_FEE_AMOUNT);
//        assertThat(testTransaction.getSystemFeeCurrency()).isEqualTo(860);
//        assertThat(testTransaction.getSystemFeePercent()).isEqualTo(DEFAULT_SYSTEM_FEE_PERCENT);
        assertThat(testTransaction.getStatus()).isEqualTo(status);
        assertThat(testTransaction.getDate()).isAfter(beforeTranInstant);
        assertThat(testTransaction.getCcyRate()).isEqualTo(cbuService.getCurrencyRate(DummyDataAdapterV3.CURRENCY));

        assertThat(testTransaction.getRecipientAccount()).isEqualTo(checkDTO.getAccount());
        assertThat(testTransaction.getRecipientAmount()).isEqualTo(2790000);
        assertThat(testTransaction.getRecipientCcy()).isEqualTo(DummyDataAdapterV3.CURRENCY);
        assertThat(testTransaction.getRecipientGateway()).isEqualTo(DummyDataAdapterV3.DUMMY_GATEWAY_NAME);
        assertThat(testTransaction.getRecipientDate()).isAfter(beforeTranInstant);
        assertThat(testTransaction.getRecipientStatus()).isEqualTo(DummyGatewayServiceV3.RESPONSE_CODE);
        assertThat(testTransaction.getRecipientStatusText()).isEqualTo(DummyGatewayServiceV3.RESPONSE_TEXT);
        assertThat(testTransaction.isRecipientSuccess()).isEqualTo(true);
        assertThat(testTransaction.getRecipientBillId()).isNotNull();
        assertThat(testTransaction.getRecipientTrId()).isEqualTo(DummyGatewayServiceV3.EXT_PAY_ID);
        assertThat(testTransaction.getRecipientServiceId()).isEqualTo(checkDTO.getRecipientId());

        assertThat(testTransaction.getSenderServiceId()).isEqualTo(0L);
        assertThat(testTransaction.getSenderGateway()).isEqualTo(DummyDataAdapterV3.DUMMY_AGENT);
        assertThat(testTransaction.getSenderAccount()).isEqualTo(DummyDataAdapterV3.DUMMY_AGENT);
//        assertThat(testTransaction.getSenderAmount()).isEqualTo(checkDTO.getAmount()); //TODO - fix
        assertThat(testTransaction.getSenderCcy()).isEqualTo(DummyDataAdapterV3.CURRENCY);
        assertThat(testTransaction.getSenderTrId()).isEqualTo(TRAN_ID);
        assertThat(testTransaction.isSenderSuccess()).isEqualTo(true);
        assertThat(testTransaction.getSenderDate()).isAfter(beforeTranInstant);
        assertThat(testTransaction.getSenderStatus()).isEqualTo(CommonTranServiceV3.OK);
        assertThat(testTransaction.getSenderStatusText()).isEqualTo("AGENT_DEPOSIT_DECREASED");
        assertThat(testTransaction.getSenderBillId()).isEqualTo(0L);
    }

    private CheckRequest getSimpleCheckDTO(Long dummyAmount) {
        CheckRequest commonCheckDTO = new CheckRequest();

        commonCheckDTO.setAccount(DUMMY_ACCOUNT);
        commonCheckDTO.setAmount(dummyAmount);
        commonCheckDTO.setCurrency(860);
        commonCheckDTO.setAgentTranId(TRAN_ID);

        return commonCheckDTO;
    }

    @Autowired
    private MappingJackson2HttpMessageConverter jacksonMessageConverter;
    @Autowired
    private PageableHandlerMethodArgumentResolver pageableArgumentResolver;
    @Autowired
    private ExceptionTranslator exceptionTranslator;
    @Autowired
    private DummyDataAdapterV3 dummyDataAdapter;
    @Autowired
    private RecipientRepository recipientRepository;

    public void testPayTransactionV2(Transaction testTransaction, Instant beforeTranInstant, CheckRequest checkDTO, String agentLogin, TransactionStatus status, Long requestAmount, int commDown, int recipientAmount) {

        assertThat(testTransaction.getRequestLogin()).isEqualTo(agentLogin);
        assertThat(testTransaction.getRequestTrId()).isEqualTo(TRAN_ID);
        assertThat(testTransaction.getRequestGroup()).isEqualTo(agentLogin);
        assertThat(testTransaction.getComment()).isNotNull();

        assertThat(testTransaction.getSystemFeeAmount()).isEqualTo(Double.valueOf(testTransaction.getSystemFee() / 100d * (testTransaction.getRequestAmount() - testTransaction.getSystemCommissionDownAmount())).longValue());
        assertThat(testTransaction.getSystemFee()).isEqualTo(2.0);
        assertThat(testTransaction.getSystemCommissionDownAmount()).isEqualTo(commDown);
        assertThat(testTransaction.getSystemCommissionUpAmount()).isEqualTo(0);
        assertThat(testTransaction.getAgentFeeAmount()).isEqualTo(150000);

        assertThat(testTransaction.getStatus()).isEqualTo(status);
        assertThat(testTransaction.getDate()).isAfter(beforeTranInstant);
        assertThat(testTransaction.getCcyRate()).isEqualTo(cbuService.getCurrencyRate(DummyDataAdapterV3.CURRENCY));

        assertThat(testTransaction.getRecipientAccount()).isEqualTo(checkDTO.getAccount());
        assertThat(testTransaction.getRecipientAmount()).isEqualTo(recipientAmount);
        assertThat(testTransaction.getRecipientCcy()).isEqualTo(DummyDataAdapterV3.CURRENCY);
        assertThat(testTransaction.getRecipientGateway()).isEqualTo(DummyDataAdapterV3.DUMMY_GATEWAY_NAME);
        assertThat(testTransaction.getRecipientDate()).isAfter(beforeTranInstant);
        assertThat(testTransaction.getRecipientStatus()).isEqualTo(DummyGatewayServiceV3.RESPONSE_CODE);
        assertThat(testTransaction.getRecipientStatusText()).isEqualTo(DummyGatewayServiceV3.RESPONSE_TEXT);
        assertThat(testTransaction.isRecipientSuccess()).isEqualTo(true);
        assertThat(testTransaction.getRecipientBillId()).isNotNull();
        assertThat(testTransaction.getRecipientTrId()).isEqualTo(DummyGatewayServiceV3.EXT_PAY_ID);
        assertThat(testTransaction.getRecipientServiceId()).isEqualTo(checkDTO.getRecipientId());

        assertThat(testTransaction.getSenderServiceId()).isEqualTo(0L);
        assertThat(testTransaction.getSenderGateway()).isEqualTo(agentLogin);
        assertThat(testTransaction.getSenderAccount()).isEqualTo(agentLogin);
        assertThat(testTransaction.getSenderAmount()).isEqualTo(3000000L);
        assertThat(testTransaction.getSenderCcy()).isEqualTo(DummyDataAdapterV3.CURRENCY);
        assertThat(testTransaction.getSenderTrId()).isEqualTo(TRAN_ID);
        assertThat(testTransaction.isSenderSuccess()).isEqualTo(true);
        assertThat(testTransaction.getSenderDate()).isAfter(beforeTranInstant);
        assertThat(testTransaction.getSenderStatus()).isEqualTo(CommonTranServiceV3.OK);
        assertThat(testTransaction.getSenderStatusText()).isEqualTo("AGENT_DEPOSIT_DECREASED");
        assertThat(testTransaction.getSenderBillId()).isEqualTo(0L);
    }

    @Autowired
    private TransactionRepository transactionRepository;
    @Autowired
    private DepositService depositService;
    @Autowired
    private CachedCbuService cbuService;
    @Autowired
    private AllowedAgentService allowedAgentService;
    @Autowired
    private FileStorageService fileStorageService;
    @Autowired
    private UzcardDebitGatewayService uzcardDebitGatewayService;
    @Autowired
    private BankRepository bankRepository;

    @Autowired
    private BankMapper bankMapper;


    private MockMvc restAgentMockMvc;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        final SharedAgentResource adapterResource = new SharedAgentResource(allowedAgentService, fileStorageService,  /*commonTranService, */commonTranServiceV2, depositService, recipientRepository, uzcardDebitGatewayService, properties, bankRepository, bankMapper);
        this.restAgentMockMvc = MockMvcBuilders.standaloneSetup(adapterResource)
            .setCustomArgumentResolvers(pageableArgumentResolver)
            .setControllerAdvice(exceptionTranslator)
            .setConversionService(createFormattingConversionService())
            .setMessageConverters(jacksonMessageConverter).build();
    }
}
