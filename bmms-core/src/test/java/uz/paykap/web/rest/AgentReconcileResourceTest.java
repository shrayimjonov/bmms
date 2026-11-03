package uz.paykap.web.rest;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import uz.paykap.config.AgentReconcileTestConfig;
import uz.paykap.config.TestSecurityConfiguration;
import uz.paykap.service.dto.reconcile.AgentReconcileRequest;
import uz.paykap.service.dto.reconcile.AgentReconcileResponse;
import uz.paykap.service.dto.reconcile.AgentReconcileStatusRequest;
import uz.paykap.service.dto.reconcile.AgentReconcileStatusResponse;
import uz.paykap.service.dto.reconcile.enumeration.AgentReconcileStatus;
import uz.paykap.web.rest.util.AgentReconcileTestUtil;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.stream.Collectors;

import static uz.paykap.web.rest.util.AgentReconcileTestUtil.RECONCILE_RECIPIENT_NAME;
import static uz.paykap.web.rest.util.AgentReconcileTestUtil.SUCCESS_TRAN_COUNT;

@Slf4j
@SpringBootTest(classes = {AgentReconcileTestConfig.class, TestSecurityConfiguration.class}, properties = {"spring.main.allow-bean-definition-overriding=true"})
//@IntegrationTest
public class AgentReconcileResourceTest {

    public static final Instant DATE = Instant.EPOCH.plus(1, ChronoUnit.HOURS);
    public static final String API_RUN_URI = "/api/reconcile/agent/run";
    public static final String API_STATUS_URI = "/api/reconcile/agent/status";
    public static final long RECONCILE_FAKE_ID = 113483L;

    private MockMvc mockMvc;
    @Autowired
    private WebApplicationContext webApplicationContext;
    @Autowired
    private AgentReconcileTestUtil testUtil;

    @BeforeEach
    public void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }


    @Test
    @Transactional
    public void testReconcileSuccess() throws Throwable {
        log.info("testReconcileSuccess started");

        var recipientId = testUtil.createRecipient(RECONCILE_RECIPIENT_NAME).getId();
        var transactions = testUtil.getSavedTransactions(SUCCESS_TRAN_COUNT, 0, DATE, RECONCILE_RECIPIENT_NAME);

        var runRequest = testUtil.getSuccessReconcileRequest(DATE, transactions, recipientId, SUCCESS_TRAN_COUNT);
        var runResponse = testUtil.sendAndReceive(mockMvc, API_RUN_URI, runRequest, AgentReconcileResponse.class);

        Assertions.assertNotNull(runResponse);
        Assertions.assertTrue(runResponse.getSuccess());
        Assertions.assertEquals(AgentReconcileStatus.PROCESSING.getCode(), runResponse.getStatusCode());
        Assertions.assertEquals(AgentReconcileStatus.PROCESSING.getMessage(), runResponse.getStatusMessage());

        var statusRequest = new AgentReconcileStatusRequest(runResponse.getId());
        var statusResponse = testUtil.sendAndReceive(mockMvc, API_STATUS_URI, statusRequest, AgentReconcileStatusResponse.class);

        Assertions.assertNotNull(statusResponse);
        Assertions.assertTrue(statusResponse.getSuccess());
        Assertions.assertEquals(AgentReconcileStatus.SUCCESS.getCode(), statusResponse.getStatusCode());
        Assertions.assertEquals(AgentReconcileStatus.SUCCESS.getMessage(), statusResponse.getStatusMessage());

        Assertions.assertEquals(0, statusResponse.getBillTrans().size());
        Assertions.assertEquals(0, statusResponse.getAgentTrans().size());
    }

    @Test
    @Transactional
    public void testAgentTranDiscrepancies() throws Exception {
        log.info("testAgentTranDiscrepancies started");

        var recipientId = testUtil.createRecipient(RECONCILE_RECIPIENT_NAME).getId();
        var systemTransactions = testUtil.getSavedTransactions(SUCCESS_TRAN_COUNT, 0, DATE, RECONCILE_RECIPIENT_NAME);

        var extraTransactions = testUtil.getAgentTransactions(1099L, 5L, DATE);
        var runRequest = testUtil.getSuccessReconcileRequest(DATE, systemTransactions, recipientId, SUCCESS_TRAN_COUNT);
        runRequest.getTransactions().addAll(extraTransactions);

        var runResponse = testUtil.sendAndReceive(mockMvc, API_RUN_URI, runRequest, AgentReconcileResponse.class);

        Assertions.assertNotNull(runResponse);
        Assertions.assertTrue(runResponse.getSuccess());
        Assertions.assertEquals(AgentReconcileStatus.PROCESSING.getCode(), runResponse.getStatusCode());
        Assertions.assertEquals(AgentReconcileStatus.PROCESSING.getMessage(), runResponse.getStatusMessage());

        var statusRequest = new AgentReconcileStatusRequest(runResponse.getId());
        var statusResponse = testUtil.sendAndReceive(mockMvc, API_STATUS_URI, statusRequest, AgentReconcileStatusResponse.class);

        Assertions.assertNotNull(statusResponse);
        Assertions.assertFalse(statusResponse.getSuccess());
        Assertions.assertEquals(AgentReconcileStatus.FAILED.getCode(), statusResponse.getStatusCode());
        Assertions.assertEquals(AgentReconcileStatus.FAILED.getMessage(), statusResponse.getStatusMessage());

        Assertions.assertEquals(0, statusResponse.getBillTrans().size());
        Assertions.assertEquals(extraTransactions.size(), statusResponse.getAgentTrans().size());
        Assertions.assertTrue(testUtil.compareAgentTransactionsDifference(new HashSet<>(extraTransactions), statusResponse.getAgentTrans()));
    }

    @Test
    @Transactional
    public void testBillTranDiscrepancies() throws Exception {
        log.info("testBillTranDiscrepancies started");

        var recipientId = testUtil.createRecipient(RECONCILE_RECIPIENT_NAME).getId();
        var systemTransactions = testUtil.getSavedTransactions(SUCCESS_TRAN_COUNT, 0, DATE, RECONCILE_RECIPIENT_NAME);

        var runRequest = testUtil.getSuccessReconcileRequest(DATE, systemTransactions, recipientId, SUCCESS_TRAN_COUNT);
        var extraTransactions = runRequest.getTransactions().stream().skip(runRequest.getTransactions().size() - 2).collect(Collectors.toList());
        extraTransactions.forEach(runRequest.getTransactions()::remove);

        var runResponse = testUtil.sendAndReceive(mockMvc, API_RUN_URI, runRequest, AgentReconcileResponse.class);

        Assertions.assertNotNull(runResponse);
        Assertions.assertTrue(runResponse.getSuccess());
        Assertions.assertEquals(AgentReconcileStatus.PROCESSING.getCode(), runResponse.getStatusCode());
        Assertions.assertEquals(AgentReconcileStatus.PROCESSING.getMessage(), runResponse.getStatusMessage());

        var statusRequest = new AgentReconcileStatusRequest(runResponse.getId());
        var statusResponse = testUtil.sendAndReceive(mockMvc, API_STATUS_URI, statusRequest, AgentReconcileStatusResponse.class);

        Assertions.assertNotNull(statusResponse);
        Assertions.assertFalse(statusResponse.getSuccess());
        Assertions.assertEquals(AgentReconcileStatus.FAILED.getCode(), statusResponse.getStatusCode());
        Assertions.assertEquals(AgentReconcileStatus.FAILED.getMessage(), statusResponse.getStatusMessage());

        Assertions.assertEquals(0, statusResponse.getAgentTrans().size());
        Assertions.assertEquals(extraTransactions.size(), statusResponse.getBillTrans().size());
        Assertions.assertTrue(testUtil.compareSystemTransactionsDifference(new HashSet<>(extraTransactions), statusResponse.getBillTrans()));
    }

    @Test
    @Transactional
    public void testMixedTranDiscrepancies() throws Exception {
        log.info("testMixedTranDiscrepancies started");

        var recipientId = testUtil.createRecipient(RECONCILE_RECIPIENT_NAME).getId();
        var systemTransactions = testUtil.getSavedTransactions(SUCCESS_TRAN_COUNT, 0, DATE, RECONCILE_RECIPIENT_NAME);


        var runRequest = testUtil.getSuccessReconcileRequest(DATE, systemTransactions, recipientId, SUCCESS_TRAN_COUNT);

        var extraAgentTransactions = testUtil.getAgentTransactions(1099L, 5L, DATE);
        var extraSystemTransactions = runRequest.getTransactions().stream().skip(runRequest.getTransactions().size() - 2).collect(Collectors.toList());

        extraSystemTransactions.forEach(runRequest.getTransactions()::remove);
        runRequest.getTransactions().addAll(extraAgentTransactions);

        var runResponse = testUtil.sendAndReceive(mockMvc, API_RUN_URI, runRequest, AgentReconcileResponse.class);

        Assertions.assertNotNull(runResponse);
        Assertions.assertTrue(runResponse.getSuccess());
        Assertions.assertEquals(AgentReconcileStatus.PROCESSING.getCode(), runResponse.getStatusCode());
        Assertions.assertEquals(AgentReconcileStatus.PROCESSING.getMessage(), runResponse.getStatusMessage());

        var statusRequest = new AgentReconcileStatusRequest(runResponse.getId());
        var statusResponse = testUtil.sendAndReceive(mockMvc, API_STATUS_URI, statusRequest, AgentReconcileStatusResponse.class);

        Assertions.assertNotNull(statusResponse);
        Assertions.assertFalse(statusResponse.getSuccess());
        Assertions.assertEquals(AgentReconcileStatus.FAILED.getCode(), statusResponse.getStatusCode());
        Assertions.assertEquals(AgentReconcileStatus.FAILED.getMessage(), statusResponse.getStatusMessage());

        Assertions.assertEquals(extraSystemTransactions.size(), statusResponse.getBillTrans().size());
        Assertions.assertEquals(extraAgentTransactions.size(), statusResponse.getAgentTrans().size());
        Assertions.assertTrue(testUtil.compareSystemTransactionsDifference(new HashSet<>(extraSystemTransactions), statusResponse.getBillTrans()));
        Assertions.assertTrue(testUtil.compareAgentTransactionsDifference(new HashSet<>(extraAgentTransactions), statusResponse.getAgentTrans()));
    }

    @Test
    @Transactional
    public void testReconcileNotExist() throws Exception {
        log.info("testReconcileNotExist started");

        var statusRequest = new AgentReconcileStatusRequest(RECONCILE_FAKE_ID);
        var statusResponse = testUtil.sendAndReceive(mockMvc, API_STATUS_URI, statusRequest, AgentReconcileStatusResponse.class);

        Assertions.assertNotNull(statusResponse);
        Assertions.assertFalse(statusResponse.getSuccess());
        Assertions.assertEquals(AgentReconcileStatus.RECONCILE_NOT_FOUND.getCode(), statusResponse.getStatusCode());
        Assertions.assertEquals(AgentReconcileStatus.RECONCILE_NOT_FOUND.getMessage(), statusResponse.getStatusMessage());
    }

    @Test
    @Transactional
    public void testRecipientNotExist() throws Exception {
        log.info("testRecipientNotExist started");

        var recipientId = testUtil.createRecipient(RECONCILE_RECIPIENT_NAME).getId() + 1;
        var transactions = testUtil.getSavedTransactions(SUCCESS_TRAN_COUNT, 0, DATE, RECONCILE_RECIPIENT_NAME);

        var runRequest = testUtil.getSuccessReconcileRequest(DATE, transactions, recipientId, SUCCESS_TRAN_COUNT);
        var runResponse = testUtil.sendAndReceive(mockMvc, API_RUN_URI, runRequest, AgentReconcileResponse.class);

        Assertions.assertNotNull(runResponse);
        Assertions.assertFalse(runResponse.getSuccess());
        Assertions.assertEquals(AgentReconcileStatus.RECIPIENT_NOT_FOUND.getCode(), runResponse.getStatusCode());
        Assertions.assertEquals(AgentReconcileStatus.RECIPIENT_NOT_FOUND.getMessage(), runResponse.getStatusMessage());
    }

    @Test
    @Transactional
    public void testGatewayNotExist() throws Exception {
        log.info("testAgentTranDiscrepancies started");

        var recipientId = testUtil.createRecipient(RECONCILE_RECIPIENT_NAME, null).getId();
        var transactions = testUtil.getSavedTransactions(SUCCESS_TRAN_COUNT, 0, DATE, RECONCILE_RECIPIENT_NAME);

        var runRequest = testUtil.getSuccessReconcileRequest(DATE, transactions, recipientId, SUCCESS_TRAN_COUNT);
        var runResponse = testUtil.sendAndReceive(mockMvc, API_RUN_URI, runRequest, AgentReconcileResponse.class);

        Assertions.assertNotNull(runResponse);
        Assertions.assertFalse(runResponse.getSuccess());
        Assertions.assertEquals(AgentReconcileStatus.GATEWAY_NOT_FOUND.getCode(), runResponse.getStatusCode());
        Assertions.assertEquals(AgentReconcileStatus.GATEWAY_NOT_FOUND.getMessage(), runResponse.getStatusMessage());
    }

    @Test
    @Transactional
    public void testEmptyTransactionsConstraint() throws Exception {
        log.info("testAgentTranDiscrepancies started");

        var recipientId = testUtil.createRecipient(RECONCILE_RECIPIENT_NAME).getId();
        var systemTransactions = testUtil.getSavedTransactions(SUCCESS_TRAN_COUNT, 0, DATE, RECONCILE_RECIPIENT_NAME);

        var runRequest = testUtil.getSuccessReconcileRequest(DATE, new ArrayList<>(), recipientId, SUCCESS_TRAN_COUNT);
        var runResponse = testUtil.sendAndReceive(mockMvc, API_RUN_URI, runRequest, AgentReconcileResponse.class);

        Assertions.assertNotNull(runResponse);
        Assertions.assertTrue(runResponse.getSuccess());
        Assertions.assertEquals(AgentReconcileStatus.PROCESSING.getCode(), runResponse.getStatusCode());
        Assertions.assertEquals(AgentReconcileStatus.PROCESSING.getMessage(), runResponse.getStatusMessage());

        var statusRequest = new AgentReconcileStatusRequest(runResponse.getId());
        var statusResponse = testUtil.sendAndReceive(mockMvc, API_STATUS_URI, statusRequest, AgentReconcileStatusResponse.class);

        Assertions.assertNotNull(statusResponse);
        Assertions.assertFalse(statusResponse.getSuccess());
        Assertions.assertEquals(AgentReconcileStatus.FAILED.getCode(), statusResponse.getStatusCode());
        Assertions.assertEquals(AgentReconcileStatus.FAILED.getMessage(), statusResponse.getStatusMessage());

        Assertions.assertEquals(0, statusResponse.getAgentTrans().size());
        Assertions.assertEquals(SUCCESS_TRAN_COUNT, statusResponse.getBillTrans().size());
    }

    @Test
    @Transactional
    public void testReconcileMultiRequest() throws Throwable {
        log.info("testReconcileSuccess started");

        var recipientId = testUtil.createRecipient(RECONCILE_RECIPIENT_NAME).getId();

        testUtil.createEmptyProcessingReconcile(recipientId);

        var transactions = testUtil.getSavedTransactions(SUCCESS_TRAN_COUNT, 0, DATE, RECONCILE_RECIPIENT_NAME);

        var runRequest = testUtil.getSuccessReconcileRequest(DATE, transactions, recipientId, SUCCESS_TRAN_COUNT);
        var runResponse = testUtil.sendAndReceive(mockMvc, API_RUN_URI, runRequest, AgentReconcileResponse.class);

        Assertions.assertNotNull(runResponse);
        Assertions.assertFalse(runResponse.getSuccess());
        Assertions.assertEquals(AgentReconcileStatus.PROCESS_BUSY.getCode(), runResponse.getStatusCode());
        Assertions.assertEquals(AgentReconcileStatus.PROCESS_BUSY.getMessage(), runResponse.getStatusMessage());
    }

    @Test
    @Transactional
    public void testEmptySystemTrans() throws Exception {
        log.info("testAgentTranDiscrepancies started");

        var recipientId = testUtil.createRecipient(RECONCILE_RECIPIENT_NAME).getId();
        var date = Instant.now();

        var transactions = new HashSet<>(testUtil.getAgentTransactions(1099L, 5L, DATE));
        var runRequest = new AgentReconcileRequest(AgentReconcileTestUtil.SUBGROUP, recipientId, date.minus(10, ChronoUnit.MINUTES), date, transactions);
        runRequest.getTransactions().addAll(transactions);

        var runResponse = testUtil.sendAndReceive(mockMvc, API_RUN_URI, runRequest, AgentReconcileResponse.class);

        Assertions.assertNotNull(runResponse);
        Assertions.assertTrue(runResponse.getSuccess());
        Assertions.assertEquals(AgentReconcileStatus.PROCESSING.getCode(), runResponse.getStatusCode());
        Assertions.assertEquals(AgentReconcileStatus.PROCESSING.getMessage(), runResponse.getStatusMessage());

        var statusRequest = new AgentReconcileStatusRequest(runResponse.getId());
        var statusResponse = testUtil.sendAndReceive(mockMvc, API_STATUS_URI, statusRequest, AgentReconcileStatusResponse.class);

        Assertions.assertNotNull(statusResponse);
        Assertions.assertFalse(statusResponse.getSuccess());
        Assertions.assertEquals(AgentReconcileStatus.FAILED.getCode(), statusResponse.getStatusCode());
        Assertions.assertEquals(AgentReconcileStatus.FAILED.getMessage(), statusResponse.getStatusMessage());

        Assertions.assertEquals(0, statusResponse.getBillTrans().size());
        Assertions.assertEquals(transactions.size(), statusResponse.getAgentTrans().size());
        Assertions.assertTrue(testUtil.compareAgentTransactionsDifference(transactions, statusResponse.getAgentTrans()));
    }

    @Test
    @Transactional
    public void testReconcileRangeSuccess() throws Throwable {
        log.info("testReconcileRangeSuccess started");

        var from = DATE.minus(SUCCESS_TRAN_COUNT - 2, ChronoUnit.MINUTES);
        var recipientId = testUtil.createRecipient(RECONCILE_RECIPIENT_NAME).getId();
        var transactions = testUtil.getSavedTransactions(SUCCESS_TRAN_COUNT, 0, DATE, RECONCILE_RECIPIENT_NAME)
                .stream()
                .filter(transaction -> transaction.getDate().isAfter(from))
                .collect(Collectors.toList());

        var runRequest = testUtil.getSuccessReconcileRequest(DATE, transactions, recipientId, SUCCESS_TRAN_COUNT - 3);
        var runResponse = testUtil.sendAndReceive(mockMvc, API_RUN_URI, runRequest, AgentReconcileResponse.class);

        Assertions.assertNotNull(runResponse);
        Assertions.assertTrue(runResponse.getSuccess());
        Assertions.assertEquals(AgentReconcileStatus.PROCESSING.getCode(), runResponse.getStatusCode());
        Assertions.assertEquals(AgentReconcileStatus.PROCESSING.getMessage(), runResponse.getStatusMessage());

        var statusRequest = new AgentReconcileStatusRequest(runResponse.getId());
        var statusResponse = testUtil.sendAndReceive(mockMvc, API_STATUS_URI, statusRequest, AgentReconcileStatusResponse.class);

        log.debug("deserialized status result : {}", statusResponse);

        Assertions.assertNotNull(statusResponse);
        Assertions.assertTrue(statusResponse.getSuccess());
        Assertions.assertEquals(AgentReconcileStatus.SUCCESS.getCode(), statusResponse.getStatusCode());
        Assertions.assertEquals(AgentReconcileStatus.SUCCESS.getMessage(), statusResponse.getStatusMessage());

        Assertions.assertEquals(0, statusResponse.getBillTrans().size());
        Assertions.assertEquals(0, statusResponse.getAgentTrans().size());
    }
}
