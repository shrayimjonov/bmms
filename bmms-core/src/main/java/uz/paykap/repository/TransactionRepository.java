package uz.paykap.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import uz.devops.bmcore.enumeration.PayType;
import uz.devops.bmcore.enumeration.TransactionStatus;
import uz.devops.bmcore.reconciliation.dto.HasRecoSummary;
import uz.devops.bmcore.reconciliation.dto.RecoTranDTO;
import uz.paykap.domain.Transaction;
import uz.paykap.gateway.dto.BktCost;
import uz.paykap.gateway.dto.GatewaySuccessErrorTran;
import uz.paykap.gateway.dto.GatewayTranDTO;
import uz.paykap.gateway.dto.ReconcileResultDTO;
import uz.paykap.service.dto.reconcile.ComplexReconcileTransaction;
import uz.paykap.service.humo.ReconcileTranInfo;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.Instant;
import java.util.*;

/**
 * Spring Data SQL repository for the Transaction entity.
 */
@SuppressWarnings("unused")
@Repository
//@JaversSpringDataAuditable
public interface TransactionRepository extends JpaRepository<Transaction, Long>, JpaSpecificationExecutor<Transaction> {
//    Integer CCY_USD = 840;
//    Integer CCY_UZS = 860;
    String STATUS = "status";
    String FROM_DATE = "fromDate";
    String TO_DATE = "toDate";

    Optional<Transaction> findFirstByPayTypeAndId(@NotNull PayType payType, Long id);

    @Modifying
    @Query("delete from Transaction t where t.status in :statuses and t.date < :date")
    int deleteAllByStatusInAndDateBefore(@Param("statuses") List<TransactionStatus> errorStatuses, @Param("date") Instant date);

    //    Optional<Transaction> findTop1ByRequestLoginAndRecipientBillId(@NotNull @Size(max = 64) String requestLogin, @NotNull Long recipientBillId);

    //    Optional<Transaction> findTop1ByRequestLoginAndRecipientBillIdAndStatus(@NotNull @Size(max = 64) String requestLogin, @NotNull Long billId, TransactionStatus status);

    Optional<Transaction> findTop1ByRequestLoginAndIdAndStatus(
            @NotNull @Size(max = 64) String requestLogin,
            @NotNull Long id,
            TransactionStatus status
    );

    //    Optional<Transaction> findTop1ByRequestLoginAndIdAndStatusIn(@NotNull @Size(max = 64) String requestLogin, Long id, Collection<TransactionStatus> status);

    //    Optional<Transaction> findTop1ByRequestLoginAndStatus(@NotNull @Size(max = 64) String requestLogin, TransactionStatus status);

    Optional<Transaction> findTop1ByRequestLoginAndId(@NotNull @Size(max = 64) String requestLogin, @NotNull Long id);

    //    List<Transaction> findByRecipientNameAndDateBetween(String recipientName, Instant fromDate, Instant toDate);

    //    List<Transaction> findByRecipientGatewayAndDateBetween(String gatewayName, Instant fromDate, Instant toDate);

    //    HashSet<Transaction> findByRecipientGatewayAndDateBetweenOrderByIdAsc(String gatewayName, Instant fromDate, Instant toDate);

    HashSet<Transaction> findByRecipientGatewayAndDateBetweenAndStatusOrderByIdAsc(
            String recipientGateway,
            Instant date,
            Instant date2,
            TransactionStatus status
    );

    Optional<Transaction> findTop1ByRequestLoginAndRequestTrId(
            @NotNull @Size(max = 64) String requestLogin,
            @NotNull @Size(max = 64) String requestTrId
    );

    Optional<Transaction> findTop1ByRequestTrId(@NotNull @Size(max = 64) String requestTrId);

    //    Optional<Transaction> findTop1ByRequestLoginAndRequestTrIdAndSenderSuccessIsTrueAndRecipientSuccessIsTrue(@NotNull @Size(max = 64) String requestLogin, @NotNull @Size(max = 64) String requestTrId);

    List<Transaction> findByRequestLoginAndRecipientNameAndDateBetween(
            String requestLogin,
            String recipientName,
            Instant fromDate,
            Instant toDate
    );

    Optional<List<Transaction>> findAllByRequestGroupAndRecipientNameAndStatusAndRecipientSuccessAndRecipientDateBetween(
            @Size(max = 64) String requestGroup,
            @Size(max = 32) String recipientName,
            TransactionStatus status,
            Boolean recipientSuccess,
            Instant from,
            Instant to
    );

    Optional<List<Transaction>> findAllByRequestGroupAndRecipientGatewayAndStatusAndRecipientSuccessAndRecipientDateBetween(
            @Size(max = 64) String requestGroup,
            String recipientGateway,
            TransactionStatus status,
            Boolean recipientSuccess,
            Instant from,
            Instant to
    );

    Optional<Transaction> findFirstByPayType(PayType payType);

    Optional<Transaction> findFirstByPayTypeOrderByDateDesc(PayType payType);

    Optional<List<Transaction>> findAllByRecipientNameAndStatusAndRecipientSuccessAndRecipientDateBetween(
            @Size(max = 32) String recipientName,
            TransactionStatus status,
            Boolean recipientSuccess,
            Instant recipientDate,
            Instant recipientDate2
    );

    Optional<List<Transaction>> findAllByRecipientGatewayAndStatusAndRecipientSuccessAndRecipientDateBetween(
            String recipientGateway,
            TransactionStatus status,
            Boolean recipientSuccess,
            Instant recipientDate,
            Instant recipientDate2
    );

    List<Transaction> findAllByRecipientServiceIdAndStatus(Long recipientServiceId, TransactionStatus status);

    List<Transaction> findAllByRecipientGatewayAndStatusAndCreatedBy(String recipientGateway, TransactionStatus status, String createdBy);

    List<Transaction> findAllByStatusAndRecipientGatewayAndRecipientDateBetween(
            TransactionStatus status,
            String recipientGateway,
            Instant recipientDate,
            Instant recipientDate2
    );

    //    Optional<Transaction> findTop1ByRecipientBillId(Long recipientBillId);

    Optional<Transaction> findTop1ByIdAndStatus(Long id, TransactionStatus status);

    //    Optional<Transaction> findTop1ByIdAndStatusIn(Long id, Collection<TransactionStatus> status);

    Optional<Transaction> findTop1ById(Long id);

    //    Optional<Transaction> findTop1ByRequestTrIdAndStatus(@NotNull @Size(max = 64) String requestTrId, TransactionStatus status);
    //    Optional<Transaction> findTop1ByRequestTrIdAndStatus(@NotNull @Size(max = 64) String requestTrId, TransactionStatus status);

    Optional<Transaction> findTop1ByIdAndRecipientSuccess(Long id, Boolean recipientSuccess);

    //    Optional<Transaction> findTop1ByIdAndStatusIsNot(Long id, TransactionStatus status);

    //    Optional<Transaction> findTop1ByRecipientBillIdAndStatus(Long recipientBillId, TransactionStatus status);

    Optional<Transaction> findTop1ByRequestLoginAndRequestTrIdOrderByIdDesc(
            @NotNull @Size(max = 64) String requestLogin,
            @NotNull @Size(max = 64) String requestTrId
    );

    Optional<List<Transaction>> findAllBySenderSuccessAndStatusAndRequestLoginAndRecipientDateBetween(
            Boolean senderSuccess,
            TransactionStatus status,
            @NotNull @Size(max = 64) String requestLogin,
            Instant from,
            Instant to
    );

    Integer countTransactionByDateBetweenAndStatusOrStatus(
            Instant fromDate,
            Instant toDate,
            TransactionStatus status,
            TransactionStatus otherStatus
    );

    //    Integer countTransactionByDateBetweenAndStatusIn(Instant fromDate, Instant toDate, Set<TransactionStatus> status);

    @Query(
            "SELECT new uz.paykap.gateway.dto.GatewaySuccessErrorTran(t.recipientGateway, count(t.status), count(case when t.status in (:errorStatuses) then 1 else NULL end), count(case when t.status in (:errorStatuses) then 1 else NULL end) * 100 / NULLIF( count(t.status), 0)) from Transaction t where t.recipientDate > :fromDate and t.recipientDate < :toDate group by t.recipientGateway"
    )
    Set<GatewaySuccessErrorTran> getTransCountByGateway(
            @Param("fromDate") Instant fromDate,
            @Param("toDate") Instant toDate,
            @Param("errorStatuses") List<TransactionStatus> errorStatuses
    );

    //    @Query("SELECT t.recipientGateway from Transaction t where count(case when t.status in (:errorStatuses) then 1 else NULL end)/count(case when t.status in (:successStatuses) then 1 else NULL end) * 100 > :limitPercent and t.recipientDate > :fromDate and t.recipientDate < :toDate group by t.recipientGateway")
    //    @Query("select new uz.paykap.gateway.dto.GatewaySuccessErrorTran(t.recipient_gateway, count(case when t.status in ('FAILED', 'CHECK_FAILED', 'NEW') then 1 else NULL end) * 100 / NULLIF(count(case when t.status in ('SUCCESS', 'WAITING') then 1 else NULL end), 0), count(case when t.status in ('FAILED', 'CHECK_FAILED', 'NEW') then 1 else NULL end), count(case when t.status in ('SUCCESS', 'WAITING') then 1 else NULL end) ) from transaction t group by t.recipient_gateway")
    //    Set<GatewaySuccessErrorTran> getTransCountByGateway(@Param("fromDate") Instant fromDate, @Param("toDate") Instant toDate, @Param("successStatuses") Set<TransactionStatus> successStatuses, @Param("errorStatuses") Set<TransactionStatus> errorStatuses, @Param("limitPercent") Long limitPercent);

    //    HashSet<Transaction> findByRecipientGatewayAndStatusAndDateBetweenOrderByIdAsc(String gatewayName, TransactionStatus status, Instant fromDate, Instant toDate);

    List<Transaction> findByRequestLoginAndIdIn(String login, Iterable<Long> idList);

    List<Transaction> findByStatusAndDateBetween(TransactionStatus status, Instant fromDate, Instant toDate);

    List<Transaction> findByStatusAndDateAfter(TransactionStatus status, Instant fromDate, Pageable pageable);

    List<Transaction> findByStatusAndDateAfterOrderByIdDesc(TransactionStatus status, Instant fromDate, Pageable pageable);

    //    @Query("select new uz.paykap.gateway.dto.GatewayTranDTO(t.id, 1L, t.gatewayAmount, t.recipientAccount, t.recipientCcy, t.recipientTrId, '', true, false) from Transaction t where t.recipientGateway = :gatewayName and t.date >= :fromDate and t.date < :toDate and t.status = 'SUCCESS' order by t.id asc")
    //    HashSet<GatewayTranDTO> getSetByGateway(@Param("gatewayName") String gatewayName, @Param("fromDate") Instant fromDate, @Param("toDate") Instant toDate);

    @Query(
            "select new uz.paykap.gateway.dto.GatewayTranDTO(t.id, 1L, t.gatewayAmount * 100.0/:amountRatio, t.recipientAccount, t.recipientCcy, t.recipientTrId, '', true, false) from Transaction t where t.recipientGateway = :gatewayName and t.date >= :fromDate and t.date < :toDate and t.status = 'SUCCESS' order by t.id asc"
    )
    HashSet<GatewayTranDTO> getSetByGateway(
            @Param("gatewayName") String gatewayName,
            @Param("fromDate") Instant fromDate,
            @Param("toDate") Instant toDate,
            @Param("amountRatio") Double amountRatio
    );

    @Query(
            "select new uz.paykap.gateway.dto.GatewayTranDTO(t.id, 1L, t.gatewayAmount * 100.0/:amountRatio, t.recipientAccount, t.recipientCcy, t.recipientTrId, '', true, false) from Transaction t where t.recipientGateway = :gatewayName and t.date >= :fromDate and t.date < :toDate and t.status = 'SUCCESS' order by t.id asc"
    )
    HashSet<RecoTranDTO> getSetByGatewayV3(
            @Param("gatewayName") String gatewayName,
            @Param("fromDate") Instant fromDate,
            @Param("toDate") Instant toDate,
            @Param("amountRatio") Double amountRatio
    );

    @Query(
            "SELECT COALESCE(SUM(t.recipientAmount), 0) from Transaction t where t.date > :fromTime and t.date < :toTime and t.recipientSuccess = true and t.comment like %:token% and t.payType = 'P2P' and t.recipientGateway = 'UZCARD'"
    )
    Long getTotalAmountInPeriodUzcardRecipient(
            @Param("fromTime") Instant fromTime,
            @Param("toTime") Instant toTime,
            @Param("token") String token
    );

    @Query(
            "SELECT COALESCE(SUM(t.gatewayAmount), 0) from Transaction t where t.date > :fromTime and t.date < :toTime and t.recipientSuccess = true and t.status = 'SUCCESS' and t.recipientName = :recipientName"
    )
    Long getSumSuccessTransactionGatewayAmountByRecipientNameAndPeriod(
            @Param("fromTime") Instant fromTime,
            @Param("toTime") Instant toTime,
            @Param("recipientName") String recipientName
    );

    @Query(
            "SELECT COALESCE(count(t.id), 0) from Transaction t where t.date > :fromTime and t.date < :toTime and t.recipientSuccess = true and t.status = 'SUCCESS' and t.recipientName = :recipientName"
    )
    Long getCountSuccessTransactionIdByRecipientNameAndPeriod(
            @Param("fromTime") Instant fromTime,
            @Param("toTime") Instant toTime,
            @Param("recipientName") String recipientName
    );

    //    @Query("select new uz.paykap.gateway.dto.GatewayTranDTO(t.id, 1L, t.gatewayAmount, t.recipientAccount, t.recipientCcy, t.recipientTrId, '', true, false, :compareSchema) from Transaction t where t.recipientGateway = :gatewayName and t.date > :fromDate and t.date < :toDate and t.status = 'SUCCESS' order by t.id asc")
    //    HashSet<GatewayTranDTO> getSetByGateway2(@Param("gatewayName") String gatewayName, @Param("fromDate") Instant fromDate, @Param("toDate") Instant toDate, @Param("compareSchema") CompareSchema compareSchema);

    @Query(
            "select new uz.paykap.gateway.dto.GatewayTranDTO(t.id, 1L, t.gatewayAmount, t.recipientAccount, t.recipientCcy, t.recipientTrId, '', true, false) from Transaction t where t.recipientName = :recipientShortName and t.date > :fromDate and t.date < :toDate and t.status = 'SUCCESS' order by t.id asc"
    )
    HashSet<GatewayTranDTO> getSetByRecipient(
            @Param("recipientShortName") String recipientShortName,
            @Param("fromDate") Instant fromDate,
            @Param("toDate") Instant toDate
    );

    @Query(
            "select new uz.paykap.gateway.dto.GatewayTranDTO(t.id, 1L, t.gatewayAmount, t.recipientAccount, t.recipientCcy, t.recipientTrId, '', true, false, :integer) from Transaction t where t.recipientName = :recipientShortName and t.date > :fromDate and t.date < :toDate and t.status = 'SUCCESS' order by t.id asc"
    )
    HashSet<GatewayTranDTO> getSetByRecipientBySchemaOrder(
            @Param("recipientShortName") String recipientShortName,
            @Param("fromDate") Instant fromDate,
            @Param("toDate") Instant toDate,
            @Param("integer") Integer integer
    );

    //    @Deprecated
    //    @Query(value = "select COALESCE(t.gateway_amount, 0) from transaction t where t.recipient_gateway = :gatewayServiceName and date_trunc('day', t.recipient_date) = :date and t.status = 'SUCCESS'", nativeQuery = true)
    //    Long getSummaryForDate(@Param("gatewayServiceName") String gatewayServiceName, @Param("date") String date);

    @Query(
            "SELECT COALESCE(SUM(t.gatewayAmount), 0) from Transaction t where t.recipientGateway = :gatewayName and t.recipientDate > :fromDate and t.recipientDate < :toDate and t.status = 'SUCCESS' and t.requestGroup = t.requestLogin"
    )
    Long getTotalRecipientAmount(
            @Param("gatewayName") String gatewayName,
            @Param("fromDate") Instant fromDate,
            @Param("toDate") Instant toDate
    );

    @Query(
            "SELECT COALESCE(SUM(t.gatewayAmount), 0) from Transaction t where t.recipientDate > :fromDate and t.recipientDate < :toDate and t.status = :status and t.ccyRate= :ccy"
    )
    Long getTotalAmountByPeriod(
            @Param("ccy") Integer ccy,
            @Param(STATUS) TransactionStatus status,
            @Param(FROM_DATE) Instant fromDate,
            @Param(TO_DATE) Instant toDate
    );

    @Query("SELECT COALESCE(SUM(t.gatewayAmount), 0) from Transaction t where t.recipientDate > :fromDate and t.recipientDate < :toDate")
    Long getTotalAmount(@Param(FROM_DATE) Instant fromDate, @Param(TO_DATE) Instant toDate);

    @Query(
            "SELECT COALESCE(SUM(t.gatewayAmount), 0) from Transaction t where t.recipientDate > :fromDate and t.recipientDate < :toDate and t.status <> :status and t.ccyRate= :ccy"
    )
    Long getTotalAmountIsNotInsStatusByPeriod(
            @Param("ccy") Integer ccy,
            @Param(STATUS) TransactionStatus status,
            @Param(FROM_DATE) Instant fromDate,
            @Param(TO_DATE) Instant toDate
    );

    //    @Query("SELECT COALESCE(SUM(t.gatewayAmount), 0) from Transaction t where t.recipientGateway = :gatewayName and t.recipientDate > :fromDate and t.recipientDate < :toDate and t.status = 'SUCCESS' and t.requestGroup = :requestGroup")
    //    Long getTotalRecipientAmount(@Param("gatewayName") String gatewayName, @Param("fromDate") Instant fromDate, @Param("toDate") Instant toDate, @Param("requestGroup") String requestGroup);

    @Query(
            "SELECT COALESCE(SUM(t.gatewayAmount * (100/:recipientAmountRatio)), 0) from Transaction t where t.recipientGateway = :gatewayName and t.recipientDate > :fromDate and t.recipientDate < :toDate and t.status = 'SUCCESS' "
    )
    Long getTotalRecipientAmount(
            @Param("gatewayName") String gatewayName,
            @Param("fromDate") Instant fromDate,
            @Param("toDate") Instant toDate,
            @Param("recipientAmountRatio") Integer recipientAmountRatio
    );

    //    @Query("SELECT COALESCE(SUM(case when (t.status = 'SUCCESS') then (t.gatewayAmount * 2) when (t.status = 'CHECK_OK') then t.gatewayAmount else 0 end), 0) from Transaction t where t.recipientGateway = :gatewayName and t.recipientDate > :fromDate and t.recipientDate < :toDate and t.requestGroup = t.requestLogin")
    //    Long getTotalRecipientAmountPaynet(@Param("gatewayName") String gatewayName, @Param("fromDate") Instant fromDate, @Param("toDate") Instant toDate);

    @Query(
            "SELECT COALESCE(count(t.id), 0) from Transaction t where t.recipientGateway = :gatewayName and t.recipientDate > :fromDate and t.recipientDate < :toDate and t.status = 'SUCCESS'"
    )
    Long getTotalCount(@Param("gatewayName") String gatewayName, @Param("fromDate") Instant fromDate, @Param("toDate") Instant toDate);

    @Query(
            "SELECT COALESCE(count(t.id), 0) as totalCount, COALESCE(SUM(t.gatewayAmount), 0) as totalAmountTiyin from Transaction t where t.recipientGateway = :gatewayName and t.recipientDate > :fromDate and t.recipientDate < :toDate and t.status = 'SUCCESS'"
    )
    HasRecoSummary getTotal4Gateway(
            @Param("gatewayName") String gatewayName,
            @Param("fromDate") Instant fromDate,
            @Param("toDate") Instant toDate
    );

    //    @Query("SELECT COALESCE(count(t.id), 0) from Transaction t where t.recipientGateway = :gatewayName and t.recipientDate > :fromDate and t.recipientDate < :toDate and t.status = 'SUCCESS' and t.requestGroup = :requestGroup")
    //    Long getTotalCount(@Param("gatewayName") String gatewayName, @Param("fromDate") Instant fromDate, @Param("toDate") Instant toDate, @Param("requestGroup") String requestGroup);

    //    @Query("SELECT COALESCE(SUM(case when (t.status = 'SUCCESS') then 2 when (t.status = 'CHECK_OK') then 1 else 0 end), 0) from Transaction t where t.recipientGateway = :gatewayName and t.recipientDate > :fromDate and t.recipientDate < :toDate and t.requestGroup = t.requestLogin")
    //    Long getTotalCountPaynet(@Param("gatewayName") String gatewayName, @Param("fromDate") Instant fromDate, @Param("toDate") Instant toDate);

    @Query(
            "select new uz.paykap.gateway.dto." +
                    "ReconcileResultDTO(t.requestTrId, t.id ,t.requestAmount, t.recipientStatusText ,t.recipientAccount,t.recipientName,t.recipientServiceId,t.date)\n" +
                    "from Transaction t\n" +
                    "where t.status = 'SUCCESS' AND t.requestLogin = :login AND t.date between :fromTime AND :toTime"
    )
    List<ReconcileResultDTO> getReconcileData(
            @Param("fromTime") Instant fromTime,
            @Param("toTime") Instant toTime,
            @Param("login") String login
    );

    @Query(
            "SELECT COALESCE(count(t.id), 0) from Transaction t where t.requestLogin = :login and t.date > :fromTime and t.date < :toTime and t.status = 'SUCCESS'"
    )
    Long getTotalCountReconcile(@Param("fromTime") Instant fromTime, @Param("toTime") Instant toTime, @Param("login") String login);

    @Query(
            "SELECT COALESCE(SUM(t.requestAmount), 0) from Transaction t where t.requestLogin = :login and t.date > :fromTime and t.date < :toTime and t.status = 'SUCCESS'"
    )
    Long getTotalRecipientAmount(@Param("fromTime") Instant fromTime, @Param("toTime") Instant toTime, @Param("login") String login);

    @Query(
            "SELECT COALESCE(SUM(t.requestAmount), 0) from Transaction t where t.requestGroup = :requestGroup and t.date > :fromTime and t.date < :toTime and t.status = 'SUCCESS'"
    )
    Long getTotalAmountInPeriod(
            @Param("fromTime") Instant fromTime,
            @Param("toTime") Instant toTime,
            @Param("requestGroup") String requestGroup
    );

    //    List<Transaction> findAllByRequestLogin(@NotNull @Size(max = 64) String requestLogin);

    //    Optional<Transaction> findTop1ByIdAndRequestLogin(Long id, @NotNull @Size(max = 64) String requestLogin);

    //    @Query("SELECT COALESCE(count(t.id), 0) from Transaction t where  t.recipientDate > :fromDate and t.recipientDate < :toDate and t.status = 'SUCCESS' and t.requestGroup = t.requestLogin")
    //    Long getTotalCount(@Param("fromDate") Instant fromDate, @Param("toDate") Instant toDate);

    //    @Query("SELECT COALESCE(SUM(t.gatewayAmount * (100/:recipientAmountRatio)), 0) from Transaction t where  t.recipientDate > :fromDate and t.recipientDate < :toDate and t.status = 'SUCCESS' and t.requestGroup = t.requestLogin")
    //    Long getTotalRecipientAmount(@Param("fromDate") Instant fromDate, @Param("toDate") Instant toDate, @Param("recipientAmountRatio") Integer recipientAmountRatio);

    //    @Query("SELECT COALESCE(SUM(t.systemFeeAmount * (100/:recipientAmountRatio) + t.systemCommissionDownAmount * (100/:recipientAmountRatio) + t.systemCommissionUpAmount * (100/:recipientAmountRatio)), 0) from Transaction t where  t.recipientDate > :fromDate and t.recipientDate < :toDate and t.status = 'SUCCESS' and t.requestGroup = t.requestLogin")
    //    Long getTotalRecipientProfit(@Param("fromDate") Instant fromDate, @Param("toDate") Instant toDate, @Param("recipientAmountRatio") Integer recipientAmountRatio);

    @Query("SELECT DISTINCT (t.eposTerminal) from Transaction t where t.payType ='HUMO' and t.senderSuccess = true and t.date < :toTime")
    List<String> getHumoTerminalIds(@Param("toTime") Instant toTime);

    @Query(
            "SELECT COALESCE(SUM(t.requestAmount), 0) from Transaction t where t.date > :fromTime and t.date < :toTime and t.senderSuccess = true and t.eposTerminal = :terminalId and t.payType = 'HUMO'"
    )
    Long getTotalAmountInPeriodHumoSenderByTerminal(
            @Param("fromTime") Instant fromTime,
            @Param("toTime") Instant toTime,
            @Param("terminalId") String terminalId
    );

    @Query(
            "SELECT COALESCE(count(t.id), 0) from Transaction t where t.date > :fromTime and t.date < :toTime and t.senderSuccess = true and t.eposTerminal = :terminalId and t.payType = 'HUMO'"
    )
    Long getTotalCountInPeriodHumoSenderByTerminal(
            @Param("fromTime") Instant fromTime,
            @Param("toTime") Instant toTime,
            @Param("terminalId") String terminalId
    );

    @Query("SELECT DISTINCT (t.eposTerminal) from Transaction t where t.senderGateway ='HUMO' and t.status = :status and t.date < :toTime")
    List<String> findAllP2pSenderHumoTerminals(@Param("status") TransactionStatus status, @Param("toTime") Instant toTime);

    @Query("SELECT DISTINCT (t.eposTerminal) from Transaction t where t.recipientGateway = 'HUMO' and t.status = :status and t.date < :toTime")
    List<String> findAllP2pRecipientHumoTerminals(@Param("status") TransactionStatus status, @Param("toTime") Instant toTime);

    Optional<List<Transaction>> findAllByRecipientNameAndStatusAndRecipientSuccessAndRecipientDateBetweenAndPayTypeIn(
            @Size(max = 32) String recipientName,
            TransactionStatus status,
            Boolean recipientSuccess,
            Instant recipientDate,
            Instant recipientDate2,
            Collection<@NotNull PayType> payType
    );

    Optional<List<Transaction>> findAllByRecipientGatewayAndStatusAndRecipientSuccessAndRecipientDateBetweenAndPayTypeIn(
            String recipientGateway,
            TransactionStatus status,
            Boolean recipientSuccess,
            Instant recipientDate,
            Instant recipientDate2,
            Collection<@NotNull PayType> payType
    );

    Optional<List<Transaction>> findAllByStatusAndRecipientSuccessAndRecipientDateBetweenAndPayTypeIn(
            TransactionStatus status,
            Boolean recipientSuccess,
            Instant recipientDate,
            Instant recipientDate2,
            Collection<@NotNull PayType> payType
    );

    @Query(
            "select new uz.paykap.gateway.dto.BktCost(" +
                    "(SELECT COALESCE(SUM(t.requestAmount), 0) from  t where t.recipientName = :recipientName and t.status = :status and t.recipientSuccess = :recipientSuccess and t.recipientDate > :instantFrom and t.recipientDate < :instantTo and t.payType in :payType)," +
                    " (SELECT COALESCE(SUM(t.systemCommissionDownAmount), 0) from  t where t.recipientName = :recipientName and t.status = :status and t.recipientSuccess = :recipientSuccess and t.recipientDate > :instantFrom and t.recipientDate < :instantTo and t.payType in :payType)," +
                    " (SELECT COALESCE(SUM(t.systemFeeAmount), 0) from  t where t.recipientName = :recipientName and t.status = :status and t.recipientSuccess = :recipientSuccess and t.recipientDate > :instantFrom and t.recipientDate < :instantTo and t.payType in :payType)) FROM Transaction t"
    )
    BktCost getRecipientBktAmount(
            @Param("recipientName") String recipientName,
            @Param("status") TransactionStatus status,
            @Param("recipientSuccess") Boolean recipientSuccess,
            @Param("instantFrom") Instant instantFrom,
            @Param("instantTo") Instant instantTo,
            @Param("payType") Collection<PayType> payType
    );

    @Query(
            "select new uz.paykap.gateway.dto.BktCost(" +
                    "(SELECT COALESCE(SUM(t.requestAmount), 0) from  t where t.recipientGateway = :gatewayName and t.status = :status and t.recipientSuccess = :recipientSuccess and t.recipientDate > :instantFrom and t.recipientDate < :instantTo and t.payType in :payType)," +
                    " (SELECT COALESCE(SUM(t.systemCommissionDownAmount), 0) from  t where t.recipientGateway = :gatewayName and t.status = :status and t.recipientSuccess = :recipientSuccess and t.recipientDate > :instantFrom and t.recipientDate < :instantTo and t.payType in :payType)," +
                    " (SELECT COALESCE(SUM(t.systemFeeAmount), 0) from  t where t.recipientGateway = :gatewayName and t.status = :status and t.recipientSuccess = :recipientSuccess and t.recipientDate > :instantFrom and t.recipientDate < :instantTo and t.payType in :payType)) FROM Transaction t"
    )
    BktCost getGatewayBktAmount(
            @Param("gatewayName") String gatewayName,
            @Param("status") TransactionStatus status,
            @Param("recipientSuccess") Boolean recipientSuccess,
            @Param("instantFrom") Instant instantFrom,
            @Param("instantTo") Instant instantTo,
            @Param("payType") Collection<PayType> payType
    );

    @Query(
            "select new uz.paykap.gateway.dto.BktCost(" +
                    "(SELECT COALESCE(SUM(t.requestAmount), 0) from  t where t.status = :status and t.recipientSuccess = :recipientSuccess and t.recipientDate > :instantFrom and t.recipientDate < :instantTo and t.payType in :payType)," +
                    " (SELECT COALESCE(SUM(t.systemCommissionDownAmount), 0) from  t where t.status = :status and t.recipientSuccess = :recipientSuccess and t.recipientDate > :instantFrom and t.recipientDate < :instantTo and t.payType in :payType)," +
                    " (SELECT COALESCE(SUM(t.systemFeeAmount), 0) from  t where t.status = :status and t.recipientSuccess = :recipientSuccess and t.recipientDate > :instantFrom and t.recipientDate < :instantTo and t.payType in :payType)) FROM Transaction t"
    )
    BktCost getPayTypeBktAmount(
            @Param("status") TransactionStatus status,
            @Param("recipientSuccess") Boolean recipientSuccess,
            @Param("instantFrom") Instant instantFrom,
            @Param("instantTo") Instant instantTo,
            @Param("payType") Collection<PayType> payType
    );

    List<Transaction> findAllBySenderStatusAndSenderSuccessIsFalseAndDateBetween(
            @Size(max = 16) String senderStatus,
            Instant date,
            Instant date2
    );

    Optional<Transaction> findFirstByRequestTrId(@NotNull @Size(max = 72) String requestTrId);

    Optional<Transaction> findTop1ByIdAndRequestLoginAndStatus(Long id, String login, TransactionStatus transactionStatus);

    @Query(
            "select new uz.paykap.service.dto.reconcile.ComplexReconcileTransaction(" +
                    "tran.id, tran.requestTrId, tran.recipientAccount, tran.requestAmount, tran.requestCcy, tran.recipientDate, false) " +
                    "from Transaction tran " +
                    "where tran.requestGroup = :subgroup and tran.date >= :fromDate and tran.date < :toDate and tran.status = 'SUCCESS' " +
                    "order by tran.id asc"
    )
    HashSet<ComplexReconcileTransaction> getReconcileTransactionsBySubgroup(
            @Param("subgroup") String subgroup,
            @Param("fromDate") Instant fromDate,
            @Param("toDate") Instant toDate
    );

    @Query(
            "SELECT " +
                    "COALESCE(count(t.id), 0) as totalCount, " +
                    "COALESCE(SUM(t.gatewayAmount), 0) as totalAmountTiyin " +
                    "from Transaction t " +
                    "where t.requestGroup = :subgroup " +
                    "and t.recipientDate > :fromDate " +
                    "and t.recipientDate < :toDate " +
                    "and t.status = 'SUCCESS'"
    )
    HasRecoSummary getSummaryBySubgroup(
            @Param("subgroup") String subgroup,
            @Param("fromDate") Instant fromDate,
            @Param("toDate") Instant toDate
    );

//    List<Transaction> findAllByRequestLoginAndSenderTrIdLike(String requestLogin, Set<String> senderTranIdList);

    List<Transaction> findAllByRequestTrId(String requestTranId);

    List<Transaction> findAllByRequestLoginAndSenderTrIdIn(String requestLogin, Set<String> senderTranIdList);

    Optional<Transaction> findTop1ByIdAndAndSenderStatusAndRequestLogin(@NotNull Long id, @NotNull String senderStatus, @NotNull String requestLogin);

    @Query("select new uz.paykap.service.dto.reconcile.ComplexReconcileTransaction(" +
            "tran.id, tran.requestTrId, tran.recipientAccount, tran.requestAmount, tran.requestCcy, tran.recipientDate, false) " +
            "from Transaction tran " +
            "where tran.requestLogin = :agent and tran.requestGroup = :group and tran.requestSubAccount = :subgroup and tran.date >= :fromDate and tran.date < :toDate and tran.status = 'SUCCESS' " +
            "order by tran.id asc")
    HashSet<ComplexReconcileTransaction> getReconcileTransactionsByGroup(@Param("agent") String agent,
                                                                         @Param("group") String group,
                                                                         @Param("subgroup") String subgroup,
                                                                         @Param("fromDate") Instant fromDate,
                                                                         @Param("toDate") Instant toDate);

    @Query("SELECT " +
            "COALESCE(count(t.id), 0) as totalCount, " +
            "COALESCE(SUM(t.requestAmount), 0) as totalAmountTiyin " +
            "from Transaction t " +
            "where t.requestLogin = :agent " +
            "and t.requestGroup = :group " +
            "and t.requestSubAccount = :subgroup " +
            "and t.recipientDate > :fromDate " +
            "and t.recipientDate < :toDate " +
            "and t.status = 'SUCCESS'")
    HasRecoSummary getSummaryByGroup(@Param("agent") String agent,
                                     @Param("group") String group,
                                     @Param("subgroup") String subgroup,
                                     @Param("fromDate") Instant fromDate,
                                     @Param("toDate") Instant toDate);

    @Query("SELECT COALESCE(count(t.id), 0) from Transaction t where t.requestLogin = :login and t.requestGroup = :group and t.requestSubAccount = :subgroup and t.recipientDate > :fromDate and t.recipientDate < :toDate and t.status = 'SUCCESS'")
    Long getTotalCount(@Param("login") String login, @Param("group") String group, @Param("subgroup") String subgroup, @Param("fromDate") Instant fromDate, @Param("toDate") Instant toDate);

    @Query("SELECT COALESCE(SUM(t.gatewayAmount * (100/:recipientAmountRatio)), 0) from Transaction t where t.requestLogin = :login and t.requestGroup = :group and t.requestSubAccount = :subgroup and t.recipientDate > :fromDate and t.recipientDate < :toDate and t.status = 'SUCCESS' ")
    Long getTotalRecipientAmount(@Param("login") String login, @Param("group") String group, @Param("subgroup") String subgroup, @Param("fromDate") Instant fromDate, @Param("toDate") Instant toDate, @Param("recipientAmountRatio") Integer recipientAmountRatio);

    @Query("SELECT COALESCE(count(t.id), 0) as totalCount, COALESCE(SUM(t.recipientAmount), 0) as totalAmountTiyin " +
            "from Transaction t where t.payType = :payType and " +
                                        "t.recipientDate > :fromDate and " +
                                        "t.recipientDate < :toDate and " +
                                        "t.recipientSuccess = true and " +
                                        "t.recipientGateway = :p2pProcessorType")
    HasRecoSummary getP2pCreditSummary(@Param("payType") PayType payType, @Param("p2pProcessorType") String p2pProcessorType, @Param("fromDate") Instant fromDate, @Param("toDate") Instant toDate);

    @Query("SELECT COALESCE(count(t.id), 0) as totalCount, COALESCE(SUM(t.senderAmount), 0) as totalAmountTiyin " +
            "from Transaction t where t.payType = :payType and " +
                                        "t.senderDate > :fromDate and " +
                                        "t.senderDate < :toDate and " +
                                        "t.senderSuccess = true and " +
                                        "t.senderGateway = :p2pProcessorType")
    HasRecoSummary getP2pDebitSummary(@Param("payType") PayType payType, @Param("p2pProcessorType") String p2pProcessorType, @Param("fromDate") Instant fromDate, @Param("toDate") Instant toDate);

    Optional<Transaction> findTop1ByIdOrRecipientTrId(Long id, String recipientTrId);

    Optional<Transaction> findTop1ByIdOrSenderTrId(Long id, String recipientTrId);

    @Query(
            value = "select new uz.paykap.service.humo.ReconcileTranInfo(tran.id, tran.recipientTrId, tran.recipientAmount, tran.recipientSuccess, tran.recipientDate, true) " +
                    "from Transaction tran " +
                    "where  tran.payType = :payType and tran.recipientDate > :fromDate and tran.recipientDate < :toDate and tran.recipientSuccess = true and tran.recipientGateway = :p2pProcessorType")
    HashSet<ReconcileTranInfo> getCreditTransactionsByReconcile(
            @Param("payType") PayType payType,
            @Param("p2pProcessorType") String p2pProcessorType,
            @Param("fromDate") Instant fromDate,
            @Param("toDate") Instant toDate
    );

    @Query(
            value = "select new uz.paykap.service.humo.ReconcileTranInfo(tran.id, tran.senderTrId, tran.senderAmount, tran.senderSuccess, tran.senderDate, true) " +
                    "from Transaction tran " +
                    "where  tran.payType = :payType and tran.senderDate > :fromDate and tran.senderDate < :toDate and tran.senderSuccess = true and tran.senderGateway = :p2pProcessorType")
    HashSet<ReconcileTranInfo> getDebitTransactionsByReconcile(
            @Param("payType") PayType payType,
            @Param("p2pProcessorType") String p2pProcessorType,
            @Param("fromDate") Instant fromDate,
            @Param("toDate") Instant toDate
    );
}
