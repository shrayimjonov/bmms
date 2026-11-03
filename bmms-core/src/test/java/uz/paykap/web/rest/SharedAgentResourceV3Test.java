package uz.paykap.web.rest;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.SneakyThrows;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.converter.ResourceHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.client.ExpectedCount;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.client.match.MockRestRequestMatchers;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import uz.devops.bm.dummy.adapter.DummyDataAdapterV3;
import uz.devops.bm.dummy.gateway.DummyGatewayServiceV3;
import uz.devops.bm.ncitrans.config.NciTransProperties;
import uz.devops.bmcore.dto.request.CheckRequest;
import uz.devops.bmcore.dto.request.CommonCheckRequest;
import uz.devops.bmcore.dto.request.CommonPayRequest;
import uz.devops.bmcore.dto.request.PayRequest;
import uz.devops.bmcore.dto.response.SimpleBillResponse;
import uz.devops.bmcore.enumeration.PayType;
import uz.devops.bmcore.enumeration.TransactionStatus;
import uz.devops.bmcore.util.FileDTO;
import uz.devops.bmcore.util.SharedContentStoreService;
import uz.javlon.commons.base.ResultData;
import uz.javlon.commons.base.SimpleResultData;
import uz.javlon.commons.exception.CodedException;
import uz.javlon.commons.exception.ProblemEnum;
import uz.paykap.IntegrationTest;
import uz.paykap.adapter.DataAdapterServiceV3;
import uz.paykap.agent.tran.CachedCbuService;
import uz.paykap.agent.tran.CommonTranServiceV3;
import uz.paykap.config.Constants;
import uz.paykap.domain.File;
import uz.paykap.domain.Recipient;
import uz.paykap.domain.Transaction;
import uz.paykap.repository.FileRepository;
import uz.paykap.repository.RecipientRepository;
import uz.paykap.repository.TransactionRepository;
import uz.paykap.service.AgentRecipientService;
import uz.paykap.service.DepositService;
import uz.paykap.service.ForeignPayService;
import uz.paykap.service.GroupSettingService;
import uz.paykap.service.abs.AbstractAbsService;
import uz.paykap.service.abs.response.AbsAccountResult;
import uz.paykap.service.abs.response.AbsTransactionResult;
import uz.paykap.web.rest.errors.ExceptionTranslator;
import uz.paykap.web.rest.util.DummyUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uz.paykap.web.rest.TestUtil.createFormattingConversionService;

@RunWith(SpringRunner.class)
@IntegrationTest
public class SharedAgentResourceV3Test {

    public static final String DUMMY_ACCOUNT = "974415655";
    public static final long DUMMY_AMOUNT = 3000000L;
    public static final String TRAN_ID = "TRAN_ID";
    public static final String TEST_ACCOUNT = "22222222222222222222";

    @Autowired
    private CommonTranServiceV3 commonTranServiceV3;

    //    @Test
    @WithMockUser(username = DummyDataAdapterV3.DUMMY_AGENT)
    public void testParallelDeposit() throws Exception {
//        dummyDataAdapterV3.sync(DummyDataAdapterV3.NAME);

        prepareDummyData();

        CountDownLatch latch = new CountDownLatch(20);

        long start = System.currentTimeMillis();

        for (long i = 0; i < 20; i++) {
            long finalI = i;
            new Thread(() -> {
//                System.out.println("START : " + finalI);
                try {
                    depositService.increaseAgentDeposit(DummyDataAdapterV3.DUMMY_AGENT, finalI);
                    System.out.println("DONE : " + finalI);
                } catch (Exception e) {
                    System.out.println("ERROR : " + finalI + " : " + e.getLocalizedMessage());
                }

                latch.countDown();
            }).start();
        }

        latch.await();

        System.out.println("Done : " + (System.currentTimeMillis() - start));
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
                        post("/api/agent/pay/v3")
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


    //TODO - fix amountRatio NotNull
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
                        post("/api/agent/pay/v3")
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

        SimpleBillResponse simpleBillDTO = getSimpleBillDTOv2(simpleCheckDTO, DummyDataAdapterV3.DUMMY_KIOSK_AGENT);

        assertThat(simpleBillDTO).isNotNull();
        assertThat(simpleBillDTO.isSuccess()).isTrue();
        assertThat(simpleBillDTO.getAmount()).isEqualTo(simpleCheckDTO.getAmount());
        assertThat(simpleBillDTO.getAccount()).isEqualTo(simpleCheckDTO.getAccount());

        CommonPayRequest payDTO = new PayRequest();
        payDTO.setAmount(DUMMY_AMOUNT);
        payDTO.setBillId(simpleBillDTO.getId());
        payDTO.setType(PayType.CASH);

        MvcResult mvcResult = restAgentMockMvc.perform(
                        post("/api/agent/pay/v3")
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
                        post("/api/agent/pay/v3")
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

        assertThat(testTransaction.getSystemFeeAmount()).isEqualTo(CommonAgentResourceTest.EXPECTED_SYSTEM_FEE);
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
//        dummyDataAdapterV3.sync(DummyDataAdapterV3.NAME);
        prepareDummyData();

        depositService.increaseAgentDeposit(DummyDataAdapterV3.DUMMY_AGENT, 10000000L);

        Optional<Recipient> recipient = recipientRepository.findTop1ByShortName(DummyDataAdapterV3.DUMMY_RECIPIENT_USD);

        assertThat(recipient.isPresent()).isTrue();
        Long recipientId = recipient.get().getId();

        commonCheckDTO = commonCheckDTO == null ? getSimpleCheckDTO() : commonCheckDTO;

        commonCheckDTO.setRecipientId(recipientId);

        MvcResult mvcResult = restAgentMockMvc.perform(
                        post("/api/agent/check/v3")
                                .content(TestUtil.convertObjectToJsonBytes(commonCheckDTO))
                                .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
                )
                .andExpect(status().isOk())
                .andReturn();

        assertThat(mvcResult.getResponse().getContentAsString()).isNotEmpty();

        return jacksonMessageConverter.getObjectMapper().readValue(mvcResult.getResponse().getContentAsByteArray(), SimpleBillResponse.class);
    }

    private SimpleBillResponse getSimpleBillDTOv2(CommonCheckRequest commonCheckDTO) throws Exception {
        return getSimpleBillDTOv2(commonCheckDTO, DummyDataAdapterV3.DUMMY_AGENT);
    }

    private SimpleBillResponse getSimpleBillDTOv2(CommonCheckRequest commonCheckDTO, String dummyAgent) throws Exception {
//        dummyDataAdapterV3.sync(DummyDataAdapterV3.NAME);
        prepareDummyData();

        depositService.increaseAgentDeposit(dummyAgent, 10000000L);

        Optional<Recipient> recipient = recipientRepository.findTop1ByShortName(DummyDataAdapterV3.DUMMY_RECIPIENT_USD);

        assertThat(recipient.isPresent()).isTrue();
        Long recipientId = recipient.get().getId();

        commonCheckDTO = commonCheckDTO == null ? getSimpleCheckDTO() : commonCheckDTO;

        commonCheckDTO.setRecipientId(recipientId);

        MvcResult mvcResult = restAgentMockMvc.perform(
                        post("/api/agent/check/v3")
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
    private DataAdapterServiceV3 dataAdapterServiceV3;
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

    public void prepareDummyData() {
        DummyUtils.prepareDummyData(dataAdapterServiceV3, groupSettingService, recipientRepository, agentRecipientService);
    }

    @Test
    @Transactional
    @WithMockUser(username = DummyDataAdapterV3.DUMMY_AGENT)
    public void testGetFile() throws Exception {

        testFileNameAndMimeTypeCases("test.txt", MediaType.TEXT_PLAIN_VALUE);
        testFileNameAndMimeTypeCases("test.uz.txt", MediaType.TEXT_PLAIN_VALUE);
        testFileNameAndMimeTypeCases("test.uz", MediaType.TEXT_PLAIN_VALUE);
        testFileNameAndMimeTypeCases("test", MediaType.TEXT_PLAIN_VALUE);
        testFileNameAndMimeTypeCases("test.png", MediaType.IMAGE_PNG_VALUE);
        testFileNameAndMimeTypeCases("test.uz.png", MediaType.IMAGE_PNG_VALUE);
        testFileNameAndMimeTypeCases("test", MediaType.IMAGE_PNG_VALUE);

        // long cases
        testFileNameAndMimeTypeCases("                 абв        где          ж123           ттт", MediaType.IMAGE_PNG_VALUE);
        testFileNameAndMimeTypeCases("Международного университета туризма и культурного наследия Шелковый путь", MediaType.IMAGE_PNG_VALUE);
        testFileNameAndMimeTypeCases("ТАШКЕНТСКИЙ ГОСУДАРСТВЕННЫЙ УНИВЕРСИТЕТ УЗБЕКСКОГО ЯЗЫКА И ЛИТЕРАТУРЫ", MediaType.IMAGE_PNG_VALUE);
        testFileNameAndMimeTypeCases("ABCDEFGHIJABCDEFGHIJABCDEFGHIJABCDEFGHIJABCDEFGHIJABCDEFGHIJ", MediaType.IMAGE_PNG_VALUE);
        testFileNameAndMimeTypeCases("abcdefghijabcdefghijabcdefghijabcdefghijabcdefghijabcdefghij", MediaType.IMAGE_PNG_VALUE);
        testFileNameAndMimeTypeCases("123456789012345678901234567890123456789012345678901234567890", MediaType.IMAGE_PNG_VALUE);
        testFileNameAndMimeTypeCases("1234567890123456789012345678901234567890123456789012345678901", MediaType.IMAGE_PNG_VALUE);
        testFileNameAndMimeTypeCases("12345678901234567890123456789012345678901234567890123456789012", MediaType.IMAGE_PNG_VALUE);
        testFileNameAndMimeTypeCases("1234567 8901234567890 1234567890123456 7890123456789 01234567890123", MediaType.IMAGE_PNG_VALUE);
        testFileNameAndMimeTypeCases("1234567890123456789012345678901234567890123456789012345678901234", MediaType.IMAGE_PNG_VALUE);
        testFileNameAndMimeTypeCases("12345678901234 5678901234567890123 45678901234567890123456789012345", MediaType.IMAGE_PNG_VALUE);
        testFileNameAndMimeTypeCases("1234567890123456789012345678901234567890123456789012345678901234567890", MediaType.IMAGE_PNG_VALUE);

        testFileNameAndMimeTypeCases("123456789_123456789_123456789_123456789_123456789_123456789", MediaType.IMAGE_PNG_VALUE);
        testFileNameAndMimeTypeCases("123456789_123456789_123456789_123456789_123456789_123456789_1", MediaType.IMAGE_PNG_VALUE);
        testFileNameAndMimeTypeCases("123456789_123456789_123456789_123456789_123456789_123456789_12", MediaType.IMAGE_PNG_VALUE);
        testFileNameAndMimeTypeCases("123456789_123456789_123456789_123456789_123456789_123456789_123", MediaType.IMAGE_PNG_VALUE);
        testFileNameAndMimeTypeCases("123456789_123456789_123456789_123456789_123456789_123456789_1234", MediaType.IMAGE_PNG_VALUE);
        testFileNameAndMimeTypeCases("123456789_123456789_123456789_123456789_123456789_123456789_12345", MediaType.IMAGE_PNG_VALUE);
        testFileNameAndMimeTypeCases("123456789_123456789_123456789_123456789_123456789_123456789_123456", MediaType.IMAGE_PNG_VALUE);
        testFileNameAndMimeTypeCases("123456789_123456789_123456789_123456789_123456789_123456789_1234567890", MediaType.IMAGE_PNG_VALUE);


    }

    private void testFileNameAndMimeTypeCases(String fileName, String contentType) throws Exception {
        int databaseSizeBeforeCreate = fileRepository.findAll().size();

        FileDTO fileDTO = new FileDTO()
                .withContent(new byte[]{1, 2, 3})
                .withName(fileName)
                .withMimeType(contentType)
                .withContentLength(0L);

        ResultData<String> fileNameResultData = sharedContentStoreService.storeFile(fileDTO);

        assertThat(fileNameResultData).isNotNull();
        assertThat(fileNameResultData.isSuccess()).isTrue();
        assertThat(fileNameResultData.getData()).isNotNull();

        MvcResult mvcResult = restAgentMockMvc.perform(get("/api/agent/file/" + fileNameResultData.getData()).contentType(contentType)).andExpect(status().isOk()).andReturn();

        assertThat(mvcResult.getResponse().getContentAsString()).isNotEmpty();

        List<File> files = fileRepository.findAll();
        assertThat(files).hasSize(databaseSizeBeforeCreate + 1);
        File testTransaction = files.get(files.size() - 1);

        assertThat(testTransaction.getName()).isEqualTo(fileNameResultData.getData());
        assertThat(testTransaction.getMimeType()).isEqualTo(contentType);
        assertThat(testTransaction.getMimeType()).isEqualTo(contentType);
    }

    @Autowired
    @Qualifier("anorHubOAuthRestTemplate")
    private OAuth2RestTemplate restTemplate;

    private MockRestServiceServer mockServer;

    @Ignore("Fix error WAITING")
    @SneakyThrows
    @Test
    @Transactional
    @WithMockUser(DummyDataAdapterV3.DUMMY_AGENT)
    public void nciDocNumNeedEqualsWithTranId() throws IOException {
        mockServer = MockRestServiceServer.createServer(restTemplate);
        restTemplate.getOAuth2ClientContext().setAccessToken(new DefaultOAuth2AccessToken("accesstoken"));

        mockServer
                .expect(
                        ExpectedCount.once(),
                        MockRestRequestMatchers.requestTo(Matchers.allOf(
                                Matchers.matchesPattern(nciTransProperties.getAnorHub().getUrl() + AbstractAbsService.GET_ACCOUNT_URL)
                        ))
                )
                .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
                .andRespond(withSuccess(getNciAccountSuccessResponse(), MediaType.APPLICATION_JSON));


        CheckRequest simpleCheckDTO = getSimpleCheckDTO();

        SimpleBillResponse simpleBillDTO = getSimpleBillDTO(simpleCheckDTO);

        Recipient dummyRecipient = recipientRepository.findTop1ByShortName(DummyDataAdapterV3.DUMMY_RECIPIENT_USD).orElseThrow(() -> new CodedException(ProblemEnum.NOT_FOUND));
        dummyRecipient.setSettlementAccount("22222222222222222223");
        recipientRepository.saveAndFlush(dummyRecipient);

        assertThat(simpleBillDTO).isNotNull();
        assertThat(simpleBillDTO.isSuccess()).isTrue();
        assertThat(simpleBillDTO.getAmount()).isEqualTo(simpleCheckDTO.getAmount());
        assertThat(simpleBillDTO.getAccount()).isEqualTo(simpleCheckDTO.getAccount());

        CommonPayRequest payDTO = new PayRequest();
//        payDTO.setAmount(DUMMY_AMOUNT);
        payDTO.setGatewayAmount(30000.03);
        payDTO.setBillId(simpleBillDTO.getId());
        payDTO.setType(PayType.NCI);
        payDTO.setParams(new HashMap<>());
        payDTO.getParams().put(Constants.ACCOUNT_PARAM, TEST_ACCOUNT);

        mockServer
                .expect(
                        ExpectedCount.once(),
                        MockRestRequestMatchers.requestTo(Matchers.allOf(
                                Matchers.matchesPattern(nciTransProperties.getAnorHub().getUrl() + AbstractAbsService.TRANSACTION_URL)
                        ))
                )
                .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
                .andExpect(MockRestRequestMatchers.jsonPath("$.docNum", Matchers.equalTo(String.valueOf(simpleBillDTO.getId()))))
                .andRespond(withSuccess(getSuccessResponse(), MediaType.APPLICATION_JSON));


        MvcResult mvcResult = restAgentMockMvc.perform(
                        post("/api/agent/pay/v3")
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
    }

    private byte[] getNciAccountSuccessResponse() throws JsonProcessingException {
        AbsAccountResult absAccountResult = new AbsAccountResult();
        absAccountResult.setErrorCode("0");
        absAccountResult.setErrorNote("SUCCESS");
        absAccountResult.setName("TEST NAME");
        absAccountResult.setTransId("777777");
        absAccountResult.setAccBal("22216");
        absAccountResult.setId("777777");
        absAccountResult.setSOutTmp("10000000");
        absAccountResult.setSOutTmp("10000000");

        List<AbsAccountResult> absAccountResults = new ArrayList<>();
        absAccountResults.add(absAccountResult);

        SimpleResultData<List<AbsAccountResult>> resultData = new SimpleResultData<>(absAccountResults);

        String result = jacksonMessageConverter.getObjectMapper().writeValueAsString(resultData);

        return result.getBytes(StandardCharsets.UTF_8);
    }

    private byte[] getSuccessResponse() throws JsonProcessingException {
        AbsTransactionResult result = new AbsTransactionResult();
        result.setCheckRequestId(String.valueOf(System.currentTimeMillis()));
        result.setErrorCode("0");
        result.setErrorNote("SUCCESS");
        result.setTransId(String.valueOf(System.currentTimeMillis()));
        result.setWaiting(false);

        SimpleResultData<AbsTransactionResult> resultData = new SimpleResultData<>(result);

        String resultString = jacksonMessageConverter.getObjectMapper().writeValueAsString(resultData);
        return resultString.getBytes();
    }

    @Autowired
    private NciTransProperties nciTransProperties;
    @Autowired
    private FileRepository fileRepository;
    @Autowired
    private TransactionRepository transactionRepository;
    @Autowired
    private DepositService depositService;
    @Autowired
    private CachedCbuService cbuService;
    @Autowired
    private GroupSettingService groupSettingService;
    @Autowired
    private AgentRecipientService agentRecipientService;
    @Autowired
    private ForeignPayService foreignPayService;
    @Autowired
    private SharedContentStoreService sharedContentStoreService;

    private MockMvc restAgentMockMvc;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        final SharedAgentResourceV3 adapterResource = new SharedAgentResourceV3(commonTranServiceV3, sharedContentStoreService);
        this.restAgentMockMvc = MockMvcBuilders.standaloneSetup(adapterResource)
                .setCustomArgumentResolvers(pageableArgumentResolver)
                .setControllerAdvice(exceptionTranslator)
                .setConversionService(createFormattingConversionService())
                .setMessageConverters(jacksonMessageConverter, new ResourceHttpMessageConverter()).build();
    }
}
