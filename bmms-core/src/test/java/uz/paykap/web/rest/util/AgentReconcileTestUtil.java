package uz.paykap.web.rest.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.test.web.servlet.MockMvc;
import uz.devops.bmcore.enumeration.TransactionStatus;
import uz.paykap.domain.Gateway;
import uz.paykap.domain.Recipient;
import uz.paykap.domain.ReconcileSummary;
import uz.paykap.domain.Transaction;
import uz.paykap.domain.enumeration.ReconcileSummaryStatus;
import uz.paykap.repository.GatewayRepository;
import uz.paykap.repository.RecipientRepository;
import uz.paykap.repository.ReconcileSummaryRepository;
import uz.paykap.repository.TransactionRepository;
import uz.paykap.service.dto.reconcile.AgentReconcileRequest;
import uz.paykap.service.dto.reconcile.AgentReconcileRequestV2;
import uz.paykap.service.dto.reconcile.ComplexReconcileTransaction;
import uz.paykap.service.dto.reconcile.SimpleReconcileTransaction;
import uz.paykap.utils.AgentReconcileV2Util;
import uz.paykap.web.rest.GatewayResourceIntTest;
import uz.paykap.web.rest.RecipientResourceIntTest;
import uz.paykap.web.rest.TransactionResourceIntTest;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Slf4j
@Component
@AllArgsConstructor
public class AgentReconcileTestUtil {

    public static final int SUCCESS_TRAN_COUNT = 10;
    public static final int REQUEST_CCY = 860;
    public static final String RECONCILE_RECIPIENT_NAME = "RECONCILE_RECIPIENT";
    public static final long EMPTY_VALUE_LONG = 0L;
    public static final String SUBGROUP = "TEST_AGENT_REC";
    public static final String LOGIN = "LOGIN";
    public static final String GROUP = "GROUP";

    private final TransactionRepository transactionRepository;
    private final RecipientRepository recipientRepository;
    private final GatewayRepository gatewayRepository;
    private final ObjectMapper objectMapper;
    private final ReconcileSummaryRepository summaryRepository;
    private final AgentReconcileV2Util util;

    public ReconcileSummary createEmptyProcessingReconcile(Long recipientId) throws Exception {
        log.trace("createEmptyReconcileSummary started");

        var recipient = recipientRepository.findById(recipientId).orElseThrow(Exception::new);

        var summary = new ReconcileSummary()
                .gatewayId(recipient.getGateway().getId())
                .gatewayName(recipient.getGateway().getName())
                .gatewayAmount(EMPTY_VALUE_LONG)
                .systemAmount(EMPTY_VALUE_LONG)
                .systemCount(EMPTY_VALUE_LONG)
                .gatewayCount(EMPTY_VALUE_LONG)
                .status(ReconcileSummaryStatus.PROCESSING);

        return summaryRepository.save(summary);
    }

    public Recipient createRecipient(String name, Gateway gateway) {
        log.trace("createRecipient started");

        return recipientRepository.save(
                        RecipientResourceIntTest
                                .createEntity(null)
                                .shortName(name)
                                .active(true)
                                .status(true))
                .gateway(gateway);
    }

    public Recipient createRecipient(String name) {
        log.trace("createRecipient started");

        var gateway = gatewayRepository.save(
                GatewayResourceIntTest
                        .createEntity(null)
                        .name(name)
                        .active(true));

        return recipientRepository.save(
                        RecipientResourceIntTest
                                .createEntity(null)
                                .shortName(name)
                                .active(true)
                                .status(true))
                .gateway(gateway);
    }

    public Boolean compareAgentTransactionsDifference(
            Set<ComplexReconcileTransaction> extraTransactions,
            Set<SimpleReconcileTransaction> responseTransactions
    ) {
        log.trace("compareAgentTransactionsDifference started");

        if (extraTransactions.size() != responseTransactions.size()) {
            return false;
        }

        return extraTransactions
                .parallelStream()
                .allMatch(
                        extraTransaction ->
                                responseTransactions
                                        .stream()
                                        .anyMatch(
                                                responseTransaction ->
                                                        extraTransaction.getAgentTranId().equals(responseTransaction.getTranId()) &&
                                                                extraTransaction.getAccount().equals(responseTransaction.getAccount()) &&
                                                                extraTransaction.getAmount().equals(responseTransaction.getAmount())
                                        )
                );
    }

    public Boolean compareSystemTransactionsDifference(
            Set<ComplexReconcileTransaction> extraTransactions,
            Set<SimpleReconcileTransaction> responseTransactions
    ) {
        log.trace("compareSystemTransactionsDifference started");

        if (extraTransactions.size() != responseTransactions.size()) {
            return false;
        }

        return extraTransactions
                .parallelStream()
                .allMatch(
                        extraTransaction ->
                                responseTransactions
                                        .stream()
                                        .anyMatch(
                                                responseTransaction ->
                                                        extraTransaction.getBillTranId().equals(responseTransaction.getTranId()) &&
                                                                extraTransaction.getAccount().equals(responseTransaction.getAccount()) &&
                                                                extraTransaction.getAmount().equals(responseTransaction.getAmount())
                                        )
                );
    }

    public <TResponse> TResponse sendAndReceive(MockMvc mockMvc, String uri, Object content, Class<TResponse> clazz) throws Exception {
        log.debug("sendAndReceive started");

        var response = mockMvc.perform(
                        post(uri)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsBytes(content)))
                .andExpect(status().is2xxSuccessful())
                .andReturn()
                .getResponse()
                .getContentAsString();

        log.debug("response got with size : {}", response.length());

        return objectMapper.readValue(response, clazz);
    }

    public List<ComplexReconcileTransaction> getAgentTransactions(Long startIndex, Long count, Instant date) {
        log.trace("getAgentTransactions started");

        var elementCount = startIndex + count;
        var result = new ArrayList<ComplexReconcileTransaction>();

        for (var currentIndex = startIndex; currentIndex < elementCount; currentIndex++) {
            var stringIndex = String.valueOf(currentIndex);
            var transaction = new ComplexReconcileTransaction(stringIndex, stringIndex, stringIndex, currentIndex, REQUEST_CCY, date);
            result.add(transaction);
        }

        return result;
    }

    public List<Transaction> getSavedTransactions(Integer successTranCount, Integer failTranCount, Instant date, String recipientName) {
        var result = getUnsavedTransactions(successTranCount, failTranCount, date, recipientName);
        return transactionRepository.saveAll(result);
    }

    public Set<Transaction> getUnsavedTransactions(Integer successTranCount, Integer failTranCount, Instant date, String recipientName) {
        log.debug("createTransactions started");

        var transactions = new HashSet<Transaction>();
        transactions.addAll(createTransactions(0, successTranCount, TransactionStatus.SUCCESS, date, recipientName));
        transactions.addAll(
                createTransactions(successTranCount, failTranCount + successTranCount, TransactionStatus.FAILED, date, recipientName)
        );
        return transactions;
    }

    public AgentReconcileRequest getSuccessReconcileRequest(
            Instant date,
            List<Transaction> transactions,
            Long recipientId,
            Integer successTranCount
    ) {
        log.trace("getSuccessReconcileRequest started");
        return new AgentReconcileRequest(GROUP, recipientId, date.minus(successTranCount, ChronoUnit.MINUTES),
                date.plus(1, ChronoUnit.MINUTES), map(transactions));
    }

    private Set<ComplexReconcileTransaction> map(List<Transaction> transactions) {
        log.trace("map started");
        return transactions
                .stream()
                .map(
                        transaction ->
                                new ComplexReconcileTransaction(
                                        transaction.getId(),
                                        transaction.getRequestTrId(),
                                        transaction.getRecipientAccount(),
                                        transaction.getRequestAmount(),
                                        transaction.getRequestCcy(),
                                        transaction.getDate(),
                                        true
                                )
                )
                .collect(Collectors.toSet());
    }

    private List<Transaction> createTransactions(int from, int to, TransactionStatus status, Instant date, String gateway) {
        log.trace("createTransactions started");

        List<Transaction> transactions = new ArrayList<>();

        if (from >= to) {
            return transactions;
        }

        for (var i = from; i < to; i++) {
            var transaction = TransactionResourceIntTest.createEntity(null);
            var time = date.minus(i, ChronoUnit.MINUTES);

            transactions.add(
                    transaction
                            .requestAmount((long) i)
                            .recipientAccount(String.valueOf(i))
                            .status(status)
                            .date(time)
                            .requestSubAccount(SUBGROUP)
                            .requestGroup(GROUP)
                            .requestLogin(LOGIN)
                            .recipientDate(time)
                            .recipientGateway(gateway)
                            .requestCcy(REQUEST_CCY)
            );
        }

        transactionRepository.saveAll(transactions);
        return transactionRepository.saveAll(
                transactions
                        .stream()
                        .map(transaction -> transaction.requestTrId(String.valueOf(transaction.getId())))
                        .collect(Collectors.toList())
        );
    }

    public ReconcileSummary createEmptyReconcileV2Process() {
        log.trace("createEmptyReconcileSummary started");

        var summary = new ReconcileSummary()
                .gatewayId(AgentReconcileV2Util.GATEWAY_ID)
                .gatewayName(util.getGatewayName(LOGIN, GROUP, SUBGROUP))
                .gatewayAmount(EMPTY_VALUE_LONG)
                .systemAmount(EMPTY_VALUE_LONG)
                .systemCount(EMPTY_VALUE_LONG)
                .gatewayCount(EMPTY_VALUE_LONG)
                .status(ReconcileSummaryStatus.PROCESSING);

        return summaryRepository.save(summary);
    }

    public AgentReconcileRequestV2 getSuccessReconcileRequestV2(Instant date, List<Transaction> transactions, Long recipientId, Integer successTranCount) {
        log.trace("getSuccessReconcileRequest started");
        return new AgentReconcileRequestV2(GROUP, SUBGROUP, date.minus(successTranCount, ChronoUnit.MINUTES),
                date.plus(1, ChronoUnit.MINUTES), map(transactions));
    }
}
