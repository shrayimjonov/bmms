package uz.paykap.agent.tran;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.assertj.core.api.AssertionsForClassTypes;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;
import uz.devops.bm.dummy.adapter.DummyDataAdapterV3;
import uz.devops.bm.paynet.config.PaynetConstants;
import uz.devops.bmcore.dto.request.CheckRequest;
import uz.devops.bmcore.dto.request.CommonCheckRequest;
import uz.devops.bmcore.dto.request.PayRequest;
import uz.devops.bmcore.dto.request.StatusRequest;
import uz.devops.bmcore.dto.response.SimpleBillResponse;
import uz.devops.bmcore.enumeration.PayType;
import uz.devops.bmcore.enumeration.TransactionStatus;
import uz.paykap.IntegrationTest;
import uz.paykap.adapter.DummyAdapterDelegate;
import uz.paykap.config.Constants;
import uz.paykap.domain.Category;
import uz.paykap.domain.Recipient;
import uz.paykap.domain.Transaction;
import uz.paykap.repository.CategoryRepository;
import uz.paykap.repository.RecipientRepository;
import uz.paykap.repository.TransactionRepository;
import uz.paykap.service.dto.AbsParams;
import uz.paykap.web.rest.CategoryResourceIntTest;
import uz.paykap.web.rest.TransactionResourceIntTest;
import uz.paykap.web.rest.util.DummyUtils;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static uz.devops.bm.dummy.adapter.DummyDataAdapterV3.DUMMY_AGENT;
import static uz.devops.bm.dummy.adapter.DummyDataAdapterV3.DUMMY_AGENT_USD;

//import uz.paykap.adapter.paynet.PaynetDataAdapter;

@RunWith(SpringRunner.class)
@IntegrationTest
public class CommonTranServiceV3Test {

    public static final String AGENT_SHORTNAME = "abdurahimasz";
    @Autowired
    private CommonTranServiceV3 commonTranServiceV3;
    @Autowired
    private RecipientRepository recipientRepository;
    @Autowired
    private TransactionRepository transactionRepository;
    @Autowired
    private DummyAdapterDelegate dummyAdapterDelegate;
    @Autowired
    private CategoryRepository categoryRepository;

    @Test
    @WithMockUser(DUMMY_AGENT)
    @Transactional
    public void check() {

        DummyUtils.prepareDummyData(dummyAdapterDelegate);

        Optional<Recipient> optionalRecipient = recipientRepository.findTop1ByShortName(DummyDataAdapterV3.DUMMY_RECIPIENT_USD);

        Assume.assumeTrue(optionalRecipient.isPresent());

        CommonCheckRequest simpleCheckDTO = new CheckRequest();
        simpleCheckDTO.setAmount(1000000L);
        simpleCheckDTO.setAccount("933999959");
        simpleCheckDTO.setRecipientId(optionalRecipient.get().getId());

        SimpleBillResponse check = commonTranServiceV3.check(simpleCheckDTO);

        Assert.assertNotNull(check);

    }

    @Test
    @WithMockUser(DUMMY_AGENT)
    @Transactional
    public void testPayStatuses() {

        DummyUtils.prepareDummyData(dummyAdapterDelegate);

        Optional<Recipient> optionalRecipient = recipientRepository.findTop1ByShortName(DummyDataAdapterV3.DUMMY_RECIPIENT_USD);

        Assume.assumeTrue(optionalRecipient.isPresent());

        CheckRequest simpleCheckDTO = new CheckRequest();
        simpleCheckDTO.setAmount(1000000L);
        simpleCheckDTO.setAccount("933999959");
        simpleCheckDTO.setRecipientId(optionalRecipient.get().getId());

        // Check payment

        SimpleBillResponse check = commonTranServiceV3.check(simpleCheckDTO);

        // Test CHECK_OK

        Assert.assertNotNull(check);
        Assert.assertTrue(check.isSuccess());
        Assert.assertEquals(check.getStatus(), TransactionStatus.CHECK_OK);

        // Do payment

        PayRequest payDTO = new PayRequest();
        payDTO.setBillId(check.getId());

        SimpleBillResponse pay = commonTranServiceV3.pay(payDTO);

        // Test SUCCESS

        Assert.assertNotNull(pay);
        Assert.assertTrue(pay.isSuccess());
        Assert.assertEquals(pay.getStatus(), TransactionStatus.SUCCESS);

        // Do payment again 1

        payDTO.setBillId(check.getId());
        pay = commonTranServiceV3.pay(payDTO);

        // Test SUCCESS

        Assert.assertNotNull(pay);
        Assert.assertTrue(pay.isSuccess());
        Assert.assertEquals(pay.getStatus(), TransactionStatus.SUCCESS);

        // Do payment again 2

        payDTO.setBillId(check.getId());
        pay = commonTranServiceV3.pay(payDTO);

        // Test SUCCESS

        Assert.assertNotNull(pay);
        Assert.assertTrue(pay.isSuccess());
        Assert.assertEquals(pay.getStatus(), TransactionStatus.SUCCESS);

        // Simulate payment in progress

        Optional<Transaction> top1ById = transactionRepository.findTop1ById(check.getId());
        Assert.assertTrue(top1ById.isPresent());
        top1ById.get().setStatus(TransactionStatus.PAYING);
        top1ById.get().setRecipientSuccess(false);
        transactionRepository.save(top1ById.get());

        // Do payment while in progress

        payDTO.setBillId(check.getId());
        pay = commonTranServiceV3.pay(payDTO);

        // Test PAYING

        assertThat(pay).isNotNull();
        assertThat(pay.isSuccess()).isFalse();
        assertThat(pay.getStatus()).isEqualTo(TransactionStatus.PAYING);

        // Simulate waiting for provider

        top1ById.get().setStatus(TransactionStatus.WAITING);
        transactionRepository.save(top1ById.get());

        // Do payment while in progress

        payDTO.setBillId(check.getId());
        pay = commonTranServiceV3.pay(payDTO);

        // Test WAITING

        assertThat(pay).isNotNull();
        assertThat(pay.isSuccess()).isFalse();
        assertThat(pay.getStatus()).isEqualTo(TransactionStatus.WAITING);

        // Simulate FAILED

        top1ById.get().setStatus(TransactionStatus.FAILED);
        transactionRepository.save(top1ById.get());

        // Try failed payment

        payDTO.setBillId(check.getId());
        pay = commonTranServiceV3.pay(payDTO);

        // Test FAILED

        assertThat(pay).isNotNull();
        assertThat(pay.isSuccess()).isFalse();
        assertThat(pay.getStatus()).isEqualTo(TransactionStatus.FAILED);

    }

    @Autowired
    private ObjectMapper objectMapper;

    @SneakyThrows
    @Test
    @WithMockUser(DUMMY_AGENT)
    @Transactional
    public void testPayWithAccount() {

        DummyUtils.prepareDummyData(dummyAdapterDelegate);

        Optional<Recipient> optionalRecipient = recipientRepository.findTop1ByShortName(DummyDataAdapterV3.DUMMY_RECIPIENT_USD);
        AbsParams absParams = new AbsParams();
        absParams.setAccount("19997000000000491505");
        absParams.setMfo("00491");
        absParams.setName("schet dlya paynet");
        absParams.setPurpose("${account} popolneniya dlya uzcard gsm");
        absParams.setTranType("1061");
        absParams.setPurposeId("00638");
        absParams.setTax("");
        Map<String, Object> map = new HashMap<>();
        map.put("abs", absParams);

        Assume.assumeTrue(optionalRecipient.isPresent());

        optionalRecipient.get().setSettlementAccount("11111111111111111111");
        optionalRecipient.get().setJsonParams(objectMapper.writeValueAsString(map));
        recipientRepository.save(optionalRecipient.get());

        CheckRequest simpleCheckDTO = new CheckRequest();
        simpleCheckDTO.setAmount(1000000L);
        simpleCheckDTO.setAccount("933999959");
        simpleCheckDTO.setRecipientId(optionalRecipient.get().getId());

        // Check payment

        SimpleBillResponse check = commonTranServiceV3.check(simpleCheckDTO);

        // Test CHECK_OK

        Assert.assertNotNull(check);
        Assert.assertTrue(check.isSuccess());
        Assert.assertEquals(check.getStatus(), TransactionStatus.CHECK_OK);

        // Do payment

        PayRequest payDTO = new PayRequest();
        payDTO.setBillId(check.getId());

        final HashMap<String, Object> params = new HashMap<>();
        params.put("account", "22222222222222222222");
        params.put("name", "schet klienta");
        params.put("mfo", "01158");
        payDTO.setParams(params);
        payDTO.setType(PayType.NCI);

        SimpleBillResponse pay = commonTranServiceV3.pay(payDTO);

        // Test SUCCESS

        Assert.assertNotNull(pay);
        Assert.assertTrue(pay.isSuccess());
        Assert.assertEquals(pay.getStatus(), TransactionStatus.SUCCESS);

    }

    @Test
    @WithMockUser(DUMMY_AGENT_USD)
    @Transactional
    public void checkAgentUSD() {

        int databaseSizeBeforeCreate = transactionRepository.findAll().size();

        DummyUtils.prepareDummyData(dummyAdapterDelegate);

        Optional<Recipient> optionalRecipient = recipientRepository.findTop1ByShortName(DummyDataAdapterV3.DUMMY_RECIPIENT_UZS);

        Assume.assumeTrue(optionalRecipient.isPresent());

        final Category category = categoryRepository.save(CategoryResourceIntTest.createEntity(null));
        recipientRepository.save(optionalRecipient.get().category(category));

        CommonCheckRequest simpleCheckDTO = new CheckRequest();
        simpleCheckDTO.setAmount(1000L);
        simpleCheckDTO.setCurrency(840);
        simpleCheckDTO.setAccount("933999959");
        simpleCheckDTO.setAgentTranId("test");
        simpleCheckDTO.setRecipientId(optionalRecipient.get().getId());

        SimpleBillResponse check = commonTranServiceV3.check(simpleCheckDTO);

        Assert.assertNotNull(check);

        List<Transaction> transactionList = transactionRepository.findAll();
        assertThat(transactionList).hasSize(databaseSizeBeforeCreate + 1);
        Transaction testTransaction = transactionList.get(transactionList.size() - 1);
        assertThat(testTransaction.getRequestAmount()).isEqualTo(simpleCheckDTO.getAmount());
        assertThat(testTransaction.getRequestCcy()).isEqualTo(simpleCheckDTO.getCurrency());
        assertThat(testTransaction.getRequestTrId()).isEqualTo(simpleCheckDTO.getAgentTranId());
        assertThat(testTransaction.getRequestLogin()).isEqualTo(DUMMY_AGENT_USD);
        assertThat(testTransaction.getRequestGroup()).isEqualTo(DUMMY_AGENT_USD);
//        assertThat(testTransaction.getSenderName()).isEqualTo(DEFAULT_SENDER_NAME);
//        assertThat(testTransaction.getSenderAccount()).isEqualTo(DEFAULT_SENDER_ACCOUNT);
//        assertThat(testTransaction.getSenderServiceId()).isEqualTo(DEFAULT_SENDER_SERVICE_ID);
//        assertThat(testTransaction.getSenderAmount()).isEqualTo(8327863);
        assertThat(testTransaction.getSenderAmount()).isEqualTo(1070);
//        assertThat(testTransaction.getSenderCcy()).isEqualTo(860);//TODO
//        assertThat(testTransaction.getSenderTrId()).isEqualTo(DEFAULT_SENDER_TR_ID);
        assertThat(testTransaction.isSenderSuccess()).isEqualTo(null);
//        assertThat(testTransaction.getSenderDate()).isEqualTo(DEFAULT_SENDER_DATE);
//        assertThat(testTransaction.getSenderStatus()).isEqualTo(true);
//        assertThat(testTransaction.getSenderStatusText()).isEqualTo(DEFAULT_SENDER_STATUS_TEXT);
//        assertThat(testTransaction.getSenderBillId()).isEqualTo(DEFAULT_SENDER_BILL_ID);
//        assertThat(testTransaction.getSenderGateway()).isEqualTo(DEFAULT_SENDER_GATEWAY);
        assertThat(testTransaction.getGatewayAmount()).isEqualTo(77052.19);
        assertThat(testTransaction.getRecipientName()).isEqualTo(optionalRecipient.get().getShortName());
        assertThat(testTransaction.getRecipientAccount()).isEqualTo(simpleCheckDTO.getAccount());
//        assertThat(testTransaction.getRecipientServiceId()).isEqualTo(DEFAULT_RECIPIENT_SERVICE_ID);
        assertThat(testTransaction.getRecipientAmount()).isEqualTo(7705219);
        assertThat(testTransaction.getRecipientCcy()).isEqualTo(860);
//        assertThat(testTransaction.getRecipientTrId()).isEqualTo(DEFAULT_RECIPIENT_TR_ID);
        assertThat(testTransaction.isRecipientSuccess()).isEqualTo(Boolean.TRUE);
//        assertThat(testTransaction.getRecipientDate()).isEqualTo(DEFAULT_RECIPIENT_DATE);
//        assertThat(testTransaction.getRecipientStatus()).isEqualTo(DEFAULT_RECIPIENT_STATUS);
//        assertThat(testTransaction.getRecipientStatusText()).isEqualTo(DEFAULT_RECIPIENT_STATUS_TEXT);
//        assertThat(testTransaction.getRecipientBillId()).isEqualTo(DEFAULT_RECIPIENT_BILL_ID);
//        assertThat(testTransaction.getRecipientGateway()).isEqualTo(DEFAULT_RECIPIENT_GATEWAY);
        assertThat(testTransaction.getSystemCommissionUpAmount()).isEqualTo(544813);
        assertThat(testTransaction.getSystemCommissionDownAmount()).isEqualTo(77830);
//        assertThat(testTransaction.getSystemFeeAmount()).isEqualTo(DEFAULT_SYSTEM_FEE_AMOUNT);
//        assertThat(testTransaction.getAgentFeeAmount()).isEqualTo(DEFAULT_AGENT_FEE_AMOUNT);
        assertThat(testTransaction.getRoundAmount()).isNotNull();
//        assertThat(testTransaction.getSystemFee()).isEqualTo(DEFAULT_SYSTEM_FEE);
//        assertThat(testTransaction.getSystemCurrency()).isEqualTo(DEFAULT_SYSTEM_CURRENCY);
//        assertThat(testTransaction.getCcyRate()).isEqualTo(DEFAULT_CCY_RATE);
        assertThat(testTransaction.getStatus()).isEqualTo(TransactionStatus.CHECK_OK);
//        assertThat(testTransaction.getComment()).isEqualTo(DEFAULT_COMMENT);
//        assertThat(testTransaction.getDate()).isEqualTo(DEFAULT_DATE);
//        assertThat(testTransaction.getPayType()).isEqualTo(PayType.AGENT); //Not set on check
//        assertThat(testTransaction.getRequestSubAccount()).isEqualTo(DEFAULT_REQUEST_SUB_ACCOUNT);
//        assertThat(testTransaction.getEposMerchant()).isEqualTo(DEFAULT_EPOS_MERCHANT);
//        assertThat(testTransaction.getEposTerminal()).isEqualTo(DEFAULT_EPOS_TERMINAL);
    }

    @Test
    @WithMockUser(DUMMY_AGENT_USD)
    @Transactional
    public void checkAgentUSDAndRecipientUsd() {

        int databaseSizeBeforeCreate = transactionRepository.findAll().size();

        DummyUtils.prepareDummyData(dummyAdapterDelegate);

        Optional<Recipient> optionalRecipient = recipientRepository.findTop1ByShortName(DummyDataAdapterV3.DUMMY_RECIPIENT_USD);

        Assume.assumeTrue(optionalRecipient.isPresent());

        CommonCheckRequest simpleCheckDTO = new CheckRequest();
        simpleCheckDTO.setAmount(1000L);
        simpleCheckDTO.setCurrency(840);
        simpleCheckDTO.setAccount("933999959");
        simpleCheckDTO.setAgentTranId("test");
        simpleCheckDTO.setRecipientId(optionalRecipient.get().getId());

        SimpleBillResponse check = commonTranServiceV3.check(simpleCheckDTO);

        Assert.assertNotNull(check);

        List<Transaction> transactionList = transactionRepository.findAll();
        assertThat(transactionList).hasSize(databaseSizeBeforeCreate + 1);
        Transaction testTransaction = transactionList.get(transactionList.size() - 1);
        assertThat(testTransaction.getRequestAmount()).isEqualTo(simpleCheckDTO.getAmount());
        assertThat(testTransaction.getRequestCcy()).isEqualTo(simpleCheckDTO.getCurrency());
        assertThat(testTransaction.getRequestTrId()).isEqualTo(simpleCheckDTO.getAgentTranId());
        assertThat(testTransaction.getRequestLogin()).isEqualTo(DUMMY_AGENT_USD);
        assertThat(testTransaction.getRequestGroup()).isEqualTo(DUMMY_AGENT_USD);
//        assertThat(testTransaction.getSenderName()).isEqualTo(DEFAULT_SENDER_NAME);
//        assertThat(testTransaction.getSenderAccount()).isEqualTo(DEFAULT_SENDER_ACCOUNT);
//        assertThat(testTransaction.getSenderServiceId()).isEqualTo(DEFAULT_SENDER_SERVICE_ID);
//        assertThat(testTransaction.getSenderAmount()).isEqualTo(8327863);
        assertThat(testTransaction.getSenderAmount()).isEqualTo(1000);
//        assertThat(testTransaction.getSenderCcy()).isEqualTo(860);//TODO
//        assertThat(testTransaction.getSenderTrId()).isEqualTo(DEFAULT_SENDER_TR_ID);
        assertThat(testTransaction.isSenderSuccess()).isEqualTo(null);
//        assertThat(testTransaction.getSenderDate()).isEqualTo(DEFAULT_SENDER_DATE);
//        assertThat(testTransaction.getSenderStatus()).isEqualTo(true);
//        assertThat(testTransaction.getSenderStatusText()).isEqualTo(DEFAULT_SENDER_STATUS_TEXT);
//        assertThat(testTransaction.getSenderBillId()).isEqualTo(DEFAULT_SENDER_BILL_ID);
//        assertThat(testTransaction.getSenderGateway()).isEqualTo(DEFAULT_SENDER_GATEWAY);
        assertThat(testTransaction.getGatewayAmount()).isEqualTo(9.3);
        assertThat(testTransaction.getRecipientName()).isEqualTo(optionalRecipient.get().getShortName());
        assertThat(testTransaction.getRecipientAccount()).isEqualTo(simpleCheckDTO.getAccount());
//        assertThat(testTransaction.getRecipientServiceId()).isEqualTo(DEFAULT_RECIPIENT_SERVICE_ID);
        assertThat(testTransaction.getRecipientAmount()).isEqualTo(930);
        assertThat(testTransaction.getRecipientCcy()).isEqualTo(840);
//        assertThat(testTransaction.getRecipientTrId()).isEqualTo(DEFAULT_RECIPIENT_TR_ID);
        assertThat(testTransaction.isRecipientSuccess()).isEqualTo(Boolean.TRUE);
//        assertThat(testTransaction.getRecipientDate()).isEqualTo(DEFAULT_RECIPIENT_DATE);
//        assertThat(testTransaction.getRecipientStatus()).isEqualTo(DEFAULT_RECIPIENT_STATUS);
//        assertThat(testTransaction.getRecipientStatusText()).isEqualTo(DEFAULT_RECIPIENT_STATUS_TEXT);
//        assertThat(testTransaction.getRecipientBillId()).isEqualTo(DEFAULT_RECIPIENT_BILL_ID);
//        assertThat(testTransaction.getRecipientGateway()).isEqualTo(DEFAULT_RECIPIENT_GATEWAY);
        assertThat(testTransaction.getSystemCommissionUpAmount()).isEqualTo(0);
        assertThat(testTransaction.getSystemCommissionDownAmount()).isEqualTo(70);
//        assertThat(testTransaction.getSystemFeeAmount()).isEqualTo(DEFAULT_SYSTEM_FEE_AMOUNT);
//        assertThat(testTransaction.getAgentFeeAmount()).isEqualTo(DEFAULT_AGENT_FEE_AMOUNT);
        assertThat(testTransaction.getRoundAmount()).isNotNull();
//        assertThat(testTransaction.getSystemFee()).isEqualTo(DEFAULT_SYSTEM_FEE);
//        assertThat(testTransaction.getSystemCurrency()).isEqualTo(DEFAULT_SYSTEM_CURRENCY);
//        assertThat(testTransaction.getCcyRate()).isEqualTo(DEFAULT_CCY_RATE);
        assertThat(testTransaction.getStatus()).isEqualTo(TransactionStatus.CHECK_OK);
//        assertThat(testTransaction.getComment()).isEqualTo(DEFAULT_COMMENT);
//        assertThat(testTransaction.getDate()).isEqualTo(DEFAULT_DATE);
//        assertThat(testTransaction.getPayType()).isEqualTo(PayType.AGENT); //Not set on check
//        assertThat(testTransaction.getRequestSubAccount()).isEqualTo(DEFAULT_REQUEST_SUB_ACCOUNT);
//        assertThat(testTransaction.getEposMerchant()).isEqualTo(DEFAULT_EPOS_MERCHANT);
//        assertThat(testTransaction.getEposTerminal()).isEqualTo(DEFAULT_EPOS_TERMINAL);
    }

    @Test
    @WithMockUser(DUMMY_AGENT_USD)
    @Transactional
    public void checkAgentUSDMaxLimitExceeded() {

        int databaseSizeBeforeCreate = transactionRepository.findAll().size();

        DummyUtils.prepareDummyData(dummyAdapterDelegate);

        Optional<Recipient> optionalRecipient = recipientRepository.findTop1ByShortName(DummyDataAdapterV3.DUMMY_RECIPIENT_UZS);

        Assume.assumeTrue(optionalRecipient.isPresent());

        CommonCheckRequest simpleCheckDTO = new CheckRequest();
        simpleCheckDTO.setAmount(200000L);
        simpleCheckDTO.setCurrency(840);
        simpleCheckDTO.setAccount("933999959");
        simpleCheckDTO.setRecipientId(optionalRecipient.get().getId());

        SimpleBillResponse check = commonTranServiceV3.check(simpleCheckDTO);

        AssertionsForClassTypes.assertThat(check).isNotNull();
        AssertionsForClassTypes.assertThat(check.isSuccess()).isFalse();
        AssertionsForClassTypes.assertThat(check.getResponseText()).contains("Max");
        AssertionsForClassTypes.assertThat(check.getId()).isNotNull();

        List<Transaction> transactionList = transactionRepository.findAll();
        assertThat(transactionList).hasSize(databaseSizeBeforeCreate + 1);
        Transaction testTransaction = transactionList.get(transactionList.size() - 1);

        assertThat(testTransaction.getStatus()).isEqualTo(TransactionStatus.CHECK_FAIL);
    }

    @Test
    @WithMockUser(DUMMY_AGENT)
    @Transactional
    public void checkUSDNotAllowed() {

        int databaseSizeBeforeCreate = transactionRepository.findAll().size();

        DummyUtils.prepareDummyData(dummyAdapterDelegate);

        Optional<Recipient> optionalRecipient = recipientRepository.findTop1ByShortName(DummyDataAdapterV3.DUMMY_RECIPIENT_USD);

        Assume.assumeTrue(optionalRecipient.isPresent());

        CommonCheckRequest simpleCheckDTO = new CheckRequest();
        simpleCheckDTO.setAmount(5L);
        simpleCheckDTO.setCurrency(840);
        simpleCheckDTO.setAccount("933999959");
        simpleCheckDTO.setRecipientId(optionalRecipient.get().getId());

        SimpleBillResponse check = commonTranServiceV3.check(simpleCheckDTO);

        Assert.assertNotNull(check);

        //        Assert.assertNotNull(check);
        AssertionsForClassTypes.assertThat(check).isNotNull();
        AssertionsForClassTypes.assertThat(check.isSuccess()).isFalse();
        AssertionsForClassTypes.assertThat(check.getResponseText()).contains("currency not allowed");
        AssertionsForClassTypes.assertThat(check.getId()).isNotNull();

        List<Transaction> transactionList = transactionRepository.findAll();
        assertThat(transactionList).hasSize(databaseSizeBeforeCreate + 1);
        Transaction testTransaction = transactionList.get(transactionList.size() - 1);

        assertThat(testTransaction.getStatus()).isEqualTo(TransactionStatus.CHECK_FAIL);

    }

    //TODO - ccy not allowed test
    //TODO - ccy not set test

    @Test
    @WithMockUser(DUMMY_AGENT)
    @Transactional
    public void testAgentFee() {

        int databaseSizeBeforeCreate = transactionRepository.findAll().size();

        DummyUtils.prepareDummyData(dummyAdapterDelegate);

        Optional<Recipient> optionalRecipient = recipientRepository.findTop1ByShortName(DummyDataAdapterV3.DUMMY_RECIPIENT_USD);

        Assume.assumeTrue(optionalRecipient.isPresent());

        CommonCheckRequest simpleCheckDTO = new CheckRequest();
        simpleCheckDTO.setAmount(1000000L);
        simpleCheckDTO.setAccount("933999959");
        simpleCheckDTO.setRecipientId(optionalRecipient.get().getId());

        SimpleBillResponse check = commonTranServiceV3.check(simpleCheckDTO);

        Assert.assertNotNull(check);

        List<Transaction> transactionList = transactionRepository.findAll();
        assertThat(transactionList).hasSize(databaseSizeBeforeCreate + 1);
        Transaction testTransaction = transactionList.get(transactionList.size() - 1);

        assertThat(testTransaction.getStatus()).isEqualTo(TransactionStatus.CHECK_OK);
    }

    @Test
    @WithMockUser(Constants.WMAGENT_LOGIN)
    @Transactional
    public void testMultiStatus() {
        List<StatusRequest> statusDTOS = doTransaction();

        List<SimpleBillResponse> simpleBillDTOS = commonTranServiceV3.getStatusList(statusDTOS);

        assertThat(simpleBillDTOS).isNotNull();
        assertThat(simpleBillDTOS.size()).isGreaterThan(0);
        assertThat(statusDTOS.size()).isEqualTo(simpleBillDTOS.size());

        for (SimpleBillResponse simpleBillDTO : simpleBillDTOS) {
            assertThat(simpleBillDTO.isSuccess()).isTrue();
        }
    }

    private List<StatusRequest> doTransaction() {
        Transaction transaction = TransactionResourceIntTest.createEntity(null);
        transaction.setStatus(TransactionStatus.SUCCESS);
        transaction.requestGroup(Constants.WMAGENT_LOGIN);
        transaction.requestLogin(Constants.WMAGENT_LOGIN);
        transaction.setRecipientGateway(PaynetConstants.PAYNET_JSON_GATEWAY_NAME);
        transaction.setGatewayAmount(100d);
        transaction.setRecipientSuccess(true);
        transaction.setSenderSuccess(true);
        transaction.setRecipientAmount(10000L);
        transaction.setRequestAmount(10000L);
//        System.out.println(Instant.now().atOffset(ZoneOffset.ofHours(0)));
        transaction.setDate(LocalDateTime.now().toInstant(ZoneOffset.ofHours(0)));
        transaction.setRecipientDate(transaction.getDate());

        transaction = transactionRepository.save(transaction);

        Transaction transaction2 = TransactionResourceIntTest.createEntity(null);
        transaction2.setStatus(TransactionStatus.SUCCESS);
        transaction2.requestGroup(Constants.WMAGENT_LOGIN);
        transaction2.requestLogin(Constants.WMAGENT_LOGIN);
        transaction2.setRecipientGateway(PaynetConstants.PAYNET_JSON_GATEWAY_NAME);
        transaction2.setGatewayAmount(100d);
        transaction2.setRecipientAmount(10000L);
        transaction2.setRequestAmount(10000L);
        transaction2.setRecipientSuccess(true);
        transaction2.setSenderSuccess(true);
//        System.out.println(Instant.now().atOffset(ZoneOffset.ofHours(0)));
        transaction2.setDate(LocalDateTime.now().toInstant(ZoneOffset.ofHours(0)));
        transaction2.setRecipientDate(transaction.getDate());

        transaction2 = transactionRepository.save(transaction2);

        Transaction transaction3 = TransactionResourceIntTest.createEntity(null);
        transaction3.setStatus(TransactionStatus.SUCCESS);
        transaction3.requestGroup(Constants.WMAGENT_LOGIN);
        transaction3.requestLogin(Constants.WMAGENT_LOGIN);
        transaction3.setRecipientGateway(PaynetConstants.PAYNET_JSON_GATEWAY_NAME);
        transaction3.setGatewayAmount(100d);
        transaction3.setRecipientAmount(10000L);
        transaction3.setRequestAmount(10000L);
        transaction3.setRecipientSuccess(true);
        transaction3.setSenderSuccess(true);
        //        System.out.println(Instant.now().atOffset(ZoneOffset.ofHours(0)));
        transaction3.setDate(LocalDateTime.now().toInstant(ZoneOffset.ofHours(0)));
        transaction3.setRecipientDate(transaction.getDate());

        transaction3 = transactionRepository.save(transaction3);

        List<StatusRequest> statusDtos = new ArrayList<>();
        StatusRequest statusDTO = new StatusRequest();
        statusDTO.setBillId(transaction.getId());
        statusDtos.add(statusDTO);

        StatusRequest statusDTO1 = new StatusRequest();
        statusDTO1.setBillId(transaction2.getId());
        statusDtos.add(statusDTO1);

            /*StatusDTO statusDTO2 = new StatusDTO();
            statusDTO1.setBillId(transaction3.getId());
            statusDtos.add(statusDTO2);*/

        return statusDtos;
    }

    @Test
    public void fixRequest() {
        var param = new HashMap<String, Object>() {{
            put(Constants.PAN_PARAM, "8600120000000000");
            put(Constants.EXPIRY_PARAM, "2203");
        }};

        var result = commonTranServiceV3.fixUzcardExpiryDateFormatAndSet(param);
        assertThat(result).isNotNull();
        assertThat(result.get(Constants.EXPIRY_PARAM)).isNotNull();
        assertThat(result.get(Constants.EXPIRY_PARAM)).isEqualTo("2203");
        var param2 = new HashMap<String, Object>() {{
            put(Constants.PAN_PARAM, "9860120000000000");
            put(Constants.EXPIRY_PARAM, "2203");
        }};
        var result2 = commonTranServiceV3.fixUzcardExpiryDateFormatAndSet(param2);
        assertThat(result2).isNotNull();
        assertThat(result2.get(Constants.EXPIRY_PARAM)).isNotNull();
        assertThat(result2.get(Constants.EXPIRY_PARAM)).isEqualTo("2203");

        var param3 = new HashMap<String, Object>() {{
            put(Constants.PAN_PARAM, "0000123487657412");
            put(Constants.EXPIRY_PARAM, "2203");
        }};

        var expMessage = "";
        try {
            commonTranServiceV3.fixUzcardExpiryDateFormatAndSet(param3);
        } catch (Exception e) {
            expMessage = e.getMessage();
        }

//        assertThat(expMessage).contains("invalid_params");

    }
}
