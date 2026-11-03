package uz.paykap.service;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.devops.bmcore.dto.request.CommonCheckRequest;
import uz.devops.bmcore.dto.request.CommonStatusRequest;
import uz.devops.bmcore.dto.response.SimpleBillResponse;
import uz.devops.bmcore.enumeration.PayType;
import uz.devops.bmcore.enumeration.TransactionStatus;
import uz.javlon.commons.exception.CodedException;
import uz.javlon.commons.exception.ProblemEnum;
import uz.paykap.domain.Agent;
import uz.paykap.domain.Bill;
import uz.paykap.domain.Transaction;
import uz.paykap.repository.BillRepository;
import uz.paykap.repository.TransactionRepository;
import uz.paykap.security.SecurityUtils;
import uz.paykap.service.dto.TransactionDTO;
import uz.paykap.service.mapper.TransactionMapper;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

/**
 * Service Implementation for managing {@link Transaction}.
 */
@Service
@Transactional
public class TransactionService {

    private final Logger log = LoggerFactory.getLogger(TransactionService.class);

    private final TransactionRepository transactionRepository;
    private final BillRepository billRepository;
    private final TransactionMapper transactionMapper;

    public TransactionService(TransactionRepository transactionRepository, BillRepository billRepository, TransactionMapper transactionMapper) {
        this.transactionRepository = transactionRepository;
        this.billRepository = billRepository;
        this.transactionMapper = transactionMapper;
    }

    public Long getTotalAmount(String gateway, Instant from, Instant to, Integer amountRatio) {
        return transactionRepository.getTotalRecipientAmount(gateway, from, to, amountRatio);
    }

    public Long getTotalAmount(String gateway, Instant from, Instant to) {
        return transactionRepository.getTotalRecipientAmount(gateway, from, to);
    }

    public Long getTotalCount(String gateway, Instant from, Instant to) {
        return transactionRepository.getTotalCount(gateway, from, to);
    }

    public TransactionDTO increaseAgentDeposit(Agent agent, Long depositAmount, Long depositId, Instant depositTime) {
        //TODO - add DepositGatewayService

        Transaction transaction = new Transaction();
        transaction =
            request(
                transaction,
                null,
                agent.getShortName(),
                SecurityUtils.getCurrentUserLogin().orElseThrow(() -> new CodedException(ProblemEnum.USERNAME_NOT_FOUND)),
                null,
                "system-" + agent.getShortName() + "-" + depositId + "-" + depositTime,
                0L,
                860
            );
        transaction = system(transaction, 0L, 860, 0d);
        transaction =
            sender(
                transaction,
                agent.getShortName(),
                -100L,
                "deposit-" + depositId + "-" + depositTime.toString(),
                depositAmount,
                depositId,
                true,
                "Success",
                agent.getShortName() + "-deposit-OK",
                agent.getShortName(),
                depositTime,
                860,
                agent.getShortName()
            );
        transaction =
            recipient(
                    transaction,
                    depositAmount,
                    agent.getShortName(),
                    depositTime,
                    860,
                    "system",
                    -100L,
                    "deposit-" + depositId + "-" + depositTime,
                    0L,
                    true,
                    "Success",
                    agent.getShortName() + "-deposit-OK"
            );

        return transactionMapper.toDto(transactionRepository.save(transaction));
    }

    public Transaction beforeCheck(String agentLogin, String agentSubLogin, CommonCheckRequest checkDTO, String recipientName) {
        Transaction transaction = new Transaction();

        request(
            transaction,
            null,
            agentLogin,
            agentSubLogin,
            null,
            checkDTO.getAgentTranId() != null ? checkDTO.getAgentTranId() : agentLogin,
            0L,
            860
        )
            .senderBillId(0L)
            .senderServiceId(0L);

        recipient(
            transaction,
            checkDTO.getAmount(),
            checkDTO.getAccount(),
            Instant.now(),
            checkDTO.getCurrency(),
            recipientName,
            checkDTO.getRecipientId(),
            "",
            0L,
            true,
            "new",
            "created"
        );

        //        system(transaction, )// TODO -

        return transaction;
    }

    public Transaction system(Transaction transaction, long feeAmount, int systemCcy, double feePercent) {
        return transaction.systemFee(feePercent).systemFeeAmount(feeAmount).systemCurrency(systemCcy).payType(PayType.UNKNOWN);
    }

    public Transaction sender(
        Transaction transaction,
        String senderName,
        Long serviceId,
        String tranId,
        Long amount,
        Long billId,
        boolean success,
        String status,
        String statusText,
        String paymentSystem,
        Instant date,
        int ccy,
        String account
    ) {
        return transaction
            .senderServiceId(serviceId)
            .senderTrId(tranId)
            .senderAmount(amount)
            .senderBillId(billId)
            .senderSuccess(success)
            .senderStatus(status)
            .senderStatusText(statusText)
            .senderName(senderName)
            .senderGateway(paymentSystem)
            .senderDate(date)
            .senderCcy(ccy)
            .senderAccount(account);
    }

    public Transaction recipient(
        Transaction transaction,
        Long amount,
        String account,
        Instant date,
        int ccy,
        String gatewayName,
        Long serviceId,
        String tranId,
        Long billId,
        boolean success,
        String status,
        String statusText
    ) {
        return transaction
            .recipientServiceId(serviceId)
            .recipientTrId(tranId)
            .recipientAmount(amount)
            .recipientBillId(billId)
            .recipientSuccess(success)
            .recipientStatus(status)
            .recipientStatusText(statusText)
            .recipientGateway(gatewayName)
            .recipientDate(date)
            .recipientCcy(ccy)
            .recipientAccount(account);
    }

    public Transaction request(
        Transaction transaction,
        Long recipientId,
        String agentLogin,
        String requestGroup,
        String requestSubGroup,
        String requestTrId,
        Long requestAmount,
        Integer requestCcy
    ) {
        return request(transaction, recipientId, agentLogin, requestGroup, requestSubGroup, requestTrId, requestAmount, requestCcy, "");
    }

    public Transaction request(
        Transaction transaction,
        Long recipientId,
        String agentLogin,
        String requestGroup,
        String requestSubGroup,
        String requestTrId,
        Long requestAmount,
        Integer requestCcy,
        String comment
    ) {
        return request(
            transaction,
            recipientId,
            agentLogin,
            requestGroup,
            requestSubGroup,
            requestTrId,
            requestAmount,
            requestCcy,
            comment,
            null,
            null
        );
    }

    public Transaction request(
        Transaction transaction,
        Long recipientId,
        String agentLogin,
        String requestGroup,
        String requestSubGroup,
        String requestTrId,
        Long requestAmount,
        Integer requestCcy,
        String comment,
        String eposMerchant,
        String eposTerminal
    ) {
        return transaction
            .requestLogin(agentLogin)
            .requestAmount(requestAmount)
            .requestCcy(requestCcy)
            .requestGroup(requestGroup != null ? requestGroup : agentLogin)
            .requestSubAccount(requestSubGroup != null ? requestSubGroup : agentLogin)
            .requestTrId(requestTrId != null ? requestTrId : "")
            .payType(PayType.UNKNOWN)
            .recipientServiceId(recipientId)
            .comment(comment)
            .roundAmount(0d)
            .status(TransactionStatus.NEW); //
    }

    public SimpleBillResponse getStatusSimpleDto(CommonStatusRequest commonStatusRequest) {
        log.info("getStatus request: {}", commonStatusRequest);
        if (commonStatusRequest.getBillId() != null) {
            log.debug("billId is not null: {}", commonStatusRequest.getBillId());
            return transactionMapper.toSimpleBill(
                transactionRepository
                    .findTop1ByRequestLoginAndId(
                        SecurityUtils.getCurrentUserLogin().orElseThrow(() -> new CodedException(ProblemEnum.USERNAME_NOT_FOUND)),
                        commonStatusRequest.getBillId()
                    )
                    .orElseThrow(() -> new CodedException(ProblemEnum.BILL_NOT_FOUND))
            );
        }

        if (!StringUtils.isEmpty(commonStatusRequest.getAgentTranId())) {
            log.debug("agent tran Id is not null: {}", commonStatusRequest.getAgentTranId());
            Transaction transaction = transactionRepository.findTop1ByRequestLoginAndRequestTrId(
                SecurityUtils.getCurrentUserLogin().orElseThrow(() -> new CodedException(ProblemEnum.USERNAME_NOT_FOUND)), commonStatusRequest.getAgentTranId()
            ).orElseThrow(() -> new CodedException(ProblemEnum.BILL_NOT_FOUND));
            log.info("find transaction: {}", transaction);
            SimpleBillResponse simpleBillResponse = transactionMapper.toSimpleBill(transaction);
            log.debug("mapping to simpleBillResponse: {}", simpleBillResponse);
            if (simpleBillResponse.getData() == null && transaction.getRecipientBillId() != null){
                log.debug("get bill by id: {}", transaction.getRecipientBillId());
                Optional<Bill> optionalBill = billRepository.findTop1ByIdAndSuccessAndLogin(transaction.getRecipientBillId(), true, transaction.getRequestLogin());
                log.debug("find optionalBill: {}", optionalBill);
                optionalBill.ifPresent(bill -> simpleBillResponse.setData(bill.getRequestJson()));
            }
            log.info("return status response: {}", simpleBillResponse);
            return simpleBillResponse;
        }

        throw new CodedException(ProblemEnum.INVALID_REQUEST);
    }

    public SimpleBillResponse toSimpleBillDto(Transaction transaction) {
        return transactionMapper.toSimpleBill(transaction);
    }

    /**
     * Save a transaction.
     *
     * @param transactionDTO the entity to save.
     * @return the persisted entity.
     */
    public TransactionDTO save(TransactionDTO transactionDTO) {
        log.debug("Request to save Transaction : {}", transactionDTO);
        Transaction transaction = transactionMapper.toEntity(transactionDTO);
        transaction = transactionRepository.save(transaction);
        return transactionMapper.toDto(transaction);
    }

    /**
     * Partially update a transaction.
     *
     * @param transactionDTO the entity to update partially.
     * @return the persisted entity.
     */
    public Optional<TransactionDTO> partialUpdate(TransactionDTO transactionDTO) {
        log.debug("Request to partially update Transaction : {}", transactionDTO);

        return transactionRepository
            .findById(transactionDTO.getId())
            .map(
                existingTransaction -> {
                    transactionMapper.partialUpdate(existingTransaction, transactionDTO);

                    return existingTransaction;
                }
            )
            .map(transactionRepository::save)
            .map(transactionMapper::toDto);
    }

    /**
     * Get all the transactions.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    @Transactional(readOnly = true)
    public Page<TransactionDTO> findAll(Pageable pageable) {
        log.debug("Request to get all Transactions");
        return transactionRepository.findAll(pageable).map(transactionMapper::toDto);
    }

    /**
     * Get one transaction by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    public Optional<TransactionDTO> findOne(Long id) {
        log.debug("Request to get Transaction : {}", id);
        return transactionRepository.findById(id).map(transactionMapper::toDto);
    }

    /**
     * Delete the transaction by id.
     *
     * @param id the id of the entity.
     */
    public void delete(Long id) {
        log.debug("Request to delete Transaction : {}", id);
        transactionRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public Optional<Transaction> getOne(Long id) {
        log.debug("Request to get Transaction : {}", id);
        return transactionRepository.findById(id);
    }
    //TODO - move to separate class, impl in V3
    //    @Override
    //    public WidgetData getWidgetData(Map<String, String> map) {
    //        List<KeyValue> keyValues = new ArrayList<>();
    //
    //        Instant todayStart = DateUtil.todayStart();
    //        Instant now = Instant.now();
    //        keyValues.add(new KeyValue("Операций", String.valueOf(transactionRepository.getTotalCount(todayStart, now))));
    //        keyValues.add(new KeyValue("На сумму", String.valueOf(transactionRepository.getTotalRecipientAmount(todayStart, now, 100))));
    //        keyValues.add(new KeyValue("Прибыль", String.valueOf(transactionRepository.getTotalRecipientProfit(todayStart, now, 100))));
    //
    //        for (String key : map.keySet()) {
    //            keyValues.add(new KeyValue(key, map.get(key)));
    //
    //        }
    //        return new WidgetData("Дневной отчет", keyValues, now);
    //
    //    }

    public List<TransactionDTO> getAllByAgentTranId(String requestLogin, List<String> agentTranIdList) {
        return transactionMapper.toDto(transactionRepository.findAllByRequestLoginAndSenderTrIdIn(requestLogin, new HashSet<>(agentTranIdList)));
    }

    public Long getTotalAmount(String login, String group, String subgroup, Instant from, Instant to, Integer amountRatio) {
        log.trace("getTotalCount started for user '{}.{}.{}' from {} to {} with amount ratio = {}", login, group, subgroup, from, to, amountRatio);
        return transactionRepository.getTotalRecipientAmount(login, group, subgroup, from, to, amountRatio);
    }

    public Long getTotalCount(String login, String group, String subgroup, Instant from, Instant to) {
        log.trace("getTotalCount started for user '{}.{}.{}' from {} to {}", login, group, subgroup, from, to);
        return transactionRepository.getTotalCount(login, group, subgroup, from, to);
    }

}
