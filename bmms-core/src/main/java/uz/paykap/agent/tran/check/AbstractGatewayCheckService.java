package uz.paykap.agent.tran.check;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uz.devops.bmcore.dto.GatewayQueryResult;
import uz.devops.bmcore.dto.request.CommonCheckRequest;
import uz.devops.bmcore.dto.request.SharedCheckDTO;
import uz.devops.bmcore.dto.response.SimpleBillResponse;
import uz.devops.bmcore.enumeration.RecipientType;
import uz.devops.bmcore.enumeration.TransactionStatus;
import uz.javlon.commons.result.HasData;
import uz.javlon.commons.result.HasResult;
import uz.paykap.agent.tran.context.CheckRequestScopedContext;
import uz.paykap.agent.tran.dto.CalculationService;
import uz.paykap.agent.tran.dto.RateService;
import uz.paykap.agent.tran.helper.AgentHelper;
import uz.paykap.domain.Transaction;
import uz.paykap.domain.enumeration.Currency;
import uz.paykap.gateway.GatewayServiceV3;
import uz.paykap.repository.TransactionRepository;
import uz.paykap.service.TransactionService;

import java.time.Instant;
import java.util.HashMap;
import java.util.Locale;
import java.util.Set;

@Slf4j
@Service
public abstract class AbstractGatewayCheckService {

    @Autowired
    protected CheckRequestScopedContext checkRequestScopedContext;
    @Autowired
    protected TransactionService transactionService;
    @Autowired
    protected TransactionRepository transactionRepository;
    @Autowired
    protected CalculationService calculationService;
    @Autowired
    protected GatewayServiceV3 gatewayServiceV3;
    @Autowired
    protected RateService rateService;
    @Autowired
    protected AgentHelper agentHelper;

    public SimpleBillResponse check(CommonCheckRequest checkDTO, String login, GatewayQueryResult gatewayQueryResult, Instant tranDate) {

        checkRequestScopedContext.setCheckRequest(checkDTO);
        checkRequestScopedContext.setUsername(login);
        checkRequestScopedContext.setRecipientParams(gatewayQueryResult);

        //TODO - fix, Assumed all agents work with amountRatio=100

        Transaction transaction = new Transaction()
                .amountRatio(checkDTO.getAmountRatio())
                .status(TransactionStatus.NEW)
                .category(gatewayQueryResult.getCategory());

        checkRequestScopedContext.setTransaction(transaction);

        transactionService.request(
                transaction,
                checkDTO.getRecipientId(),
                login,
                checkDTO.getRequestGroup(),
                checkDTO.getRequestSubGroup(),
                checkDTO.getAgentTranId(),
                checkDTO.getAmount(),
                checkDTO.getCurrency()
        );

        if (checkDTO.getAmount() != null) {
            HasData<Integer> currencyResult = agentHelper.getAllowedCurrency4Agent(gatewayQueryResult, checkDTO.getCurrency());

            if (!currencyResult.isSuccess()) {
                transaction
                        .status(TransactionStatus.CHECK_FAIL)
                        .comment(currencyResult.getMessage())
                        .senderSuccess(false)
                        .recipientSuccess(false)
                        .recipientName(gatewayQueryResult.getRecipientShortName())
                        .recipientGateway(gatewayQueryResult.getGatewayName())
                        .recipientAccount(checkDTO.getAccount())
                        .recipientStatusText(currencyResult.getMessage())
                        .date(Instant.now())
                ;

                transaction = transactionRepository.save(transaction);

                return getSimpleErrorResponse(transaction);
            }

            HasResult resultData = calculationService.validateAmount(checkDTO.getAmount(), currencyResult.getData(), gatewayQueryResult);

            if (!resultData.isSuccess()) {
                transaction
                        .status(TransactionStatus.CHECK_FAIL)
                        .comment(resultData.getMessage())
                        .senderSuccess(false)
                        .recipientSuccess(false)
                        .recipientName(gatewayQueryResult.getRecipientShortName())
                        .recipientGateway(gatewayQueryResult.getGatewayName())
                        .recipientAccount(checkDTO.getAccount())
                        .recipientStatusText(resultData.getMessage())
                        .date(Instant.now())
                ;

                transaction = transactionRepository.save(transaction);

                return getSimpleErrorResponse(transaction);
            }
        }

        // Need in calc
        transaction
                .eposMerchant(gatewayQueryResult.getEposMerchant())
                .eposTerminal(gatewayQueryResult.getEposTerminal())
                .amountRatio(gatewayQueryResult.getRecipientAmountRatio());

        if (checkDTO.getAmount() != null) {
            calculationService.calc(transaction, gatewayQueryResult);
            checkDTO.setGatewayAmount(transaction.getGatewayAmount());
        }

        transaction
                //Recipient data
                .recipientAccount(checkDTO.getAccount())
                .recipientCcy(gatewayQueryResult.getRecipientCcy())
                .recipientGateway(gatewayQueryResult.getGatewayName())
                .status(TransactionStatus.CHECKING)
                .date(tranDate)
                //Sender data   TODO JAVLON
                .senderName(login)
                .senderAccount(login)
                .senderTrId(checkDTO.getAgentTranId())
                .senderGateway(login)
        ;

        if (gatewayQueryResult.getRecipientShortName() != null) {
            transaction.setRecipientName(gatewayQueryResult.getRecipientShortName());
        }

        transaction = transactionRepository.save(transaction);

        log.debug("OK - transaction saved : {}", transaction);

        log.debug("INFO - gatewayServiceResolver.check() calling...");

        SharedCheckDTO sharedCheckDTO = new SharedCheckDTO()
                .login(login)
                .checkDTO(checkDTO)
                .recipientParams(gatewayQueryResult)
                .tranId(transaction.getId())
                .gatewayAmount(transaction.getGatewayAmount());

        SimpleBillResponse result = gatewayServiceV3.check(sharedCheckDTO);

        checkRequestScopedContext.setSimpleBillResponse(result);

        log.debug("OK - gatewayServiceResolver.check() : {}", result);

        setRecipientResults(transaction, result);

        afterCheckResult();

        transactionRepository.save(transaction);

        log.debug("OK - transaction updated : {}", transaction);

        prepareResult(transaction, result);

        log.info("<<<<<<<<<< Check Response <<<<<<<<<<:\n{}\n", result);

        return result;
    }

    protected abstract void afterCheckResult();

    protected abstract Set<RecipientType> getSupportedTypes();

    private SimpleBillResponse getSimpleErrorResponse(Transaction transaction) {
        SimpleBillResponse result = new SimpleBillResponse();
        result.id(transaction.getId());
        result.account(transaction.getRecipientAccount());
        result.amount(transaction.getRequestAmount());
        result.currency(transaction.getRequestCcy());
        result.status(transaction.getStatus());
        result.success(false);
        result.amountRatio(transaction.getAmountRatio());
        result.responseText(transaction.getSenderStatusText() + ", " + transaction.getRecipientStatusText());
        result.responseTime(transaction.getDate());
        result.requestTime(transaction.getDate());
//        result.supportsFinal(true); //TODO
        return result;
    }

    private void setRecipientResults(Transaction transaction, SimpleBillResponse result) {
        transaction
                .recipientDate(result.getResponseTime())
                .recipientName(result.getRecipientShortName())
                .recipientStatus(String.valueOf(result.isSuccess()))
                .recipientStatusText(result.getResponseText())
                .recipientSuccess(result.isSuccess())
                .status(result.isSuccess() ? TransactionStatus.CHECK_OK : TransactionStatus.CHECK_FAIL)
                .recipientBillId(result.getId())
                .recipientTrId(result.getExtId());
    }

    private void prepareResult(Transaction transaction, SimpleBillResponse result) {
        // Tran.id is main id
        result.setId(transaction.getId());
        result.setStatus(transaction.getStatus());
        result.setAmount(transaction.getRequestAmount());

        if (result.getData() == null) {
            result.setData(new HashMap<>(3));
        }

        result.getData().put("rate", String.valueOf(transaction.getCcyRate()));
        result.getData().put("recipientCcy", String.valueOf(transaction.getRecipientCcy()));
        result.getData().put("recipientAmount", String.format(Locale.US, "%.2f", transaction.getGatewayAmount()));
//        if (transaction.getSenderAmount() != null) {
//            result.getData().put("senderAmount", String.format(Locale.US, "%.2f", transaction.getSenderAmount() / 100d));
//        }
        result.getData().put("senderCcy", String.valueOf(transaction.getSenderCcy()));
    }
}
