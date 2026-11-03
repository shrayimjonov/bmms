package uz.paykap.repository;

import org.javers.spring.annotation.JaversSpringDataAuditable;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import uz.devops.bmcore.enumeration.RequestType;
import uz.paykap.domain.Bill;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;


/**
 * Spring Data SQL repository for the Bill entity.
 */
@SuppressWarnings("unused")
@Repository
//@JaversSpringDataAuditable
public interface BillRepository extends JpaRepository<Bill, Long>, JpaSpecificationExecutor<Bill> {
    Optional<Bill> findTop1ByIdAndSuccessAndLogin(Long id, Boolean success, @Size(max = 64) String login);

    Optional<Bill> findTop1ByIdAndSuccessAndLoginAndType(Long id, Boolean success, @Size(max = 64) String login, @NotNull RequestType type);

    Optional<Bill> findTop1ByIdAndSuccessAndType(Long id, Boolean success, @NotNull RequestType type);

//    @Deprecated
    //    Optional<Bill> findTop1ByIdAndSuccessAndLoginAndTypeAndRecipientShortName(Long id, Boolean success, @Size(max = 64) String login, @NotNull RequestType type, @NotNull @Size(max = 64) String recipientShortName);

    //    Optional<Bill> findTop1ByIdAndLoginAndRecipientShortNameOrderByIdDesc(Long id, @Size(max = 64) String login, @NotNull @Size(max = 64) String recipientShortName);

    //    Optional<Bill> findTop1ByIdAndLoginAndTypeOrderByIdDesc(Long id, @Size(max = 64) String login, @NotNull RequestType type);

    //    Optional<Bill> findTop1BySessionIdAndSuccessAndLogin(String sessionId, Boolean success, @Size(max = 64) String login);

    //    Optional<Bill> findOneBySessionIdAndLogin(String sessionId, String login);

    //    Optional<Bill> findTop1ByIdAndLogin(Long id, String login);

    Optional<Bill> findTop1ByIdAndLoginAndType(Long id, @Size(max = 64) String login, @NotNull RequestType type);

    Optional<Bill> findTop1ByIdAndSuccess(Long id, Boolean success);

    Optional<Bill> findTop1ByIdAndType(Long id, @NotNull RequestType type);

    Optional<Bill> findTop1ByTransactionIdAndTypeAndSuccessTrue(Long id, @NotNull RequestType type);

    Optional<Bill> findTop1ByTransactionIdAndTypeAndSuccessTrueAndRecipientShortNameContains(
        Long transactionId,
        @NotNull RequestType type,
        @NotNull @Size(max = 64) String recipientShortName
    );

    //    Optional<Bill> findTop1ByIdAndTypeAndSuccess(Long id, @NotNull RequestType type, Boolean success);

    Optional<Bill> findTop1ByRecipientShortNameAndSuccess(@NotNull @Size(max = 64) String recipientShortName, Boolean success);

    List<Bill> findByTypeAndRecipientShortNameAndResponseTimeAfterAndResponseTextContainsAndSuccessTrue(RequestType requestType, @NotNull @Size(max = 64) String recipientShortName, Instant fromDate, String responseText, Pageable pageable);

    Optional<Bill> findTop1ByTransactionIdAndTypeAndLoginAndSuccessTrue(@NotNull Long transactionId, RequestType requestType, String login);

    Optional<Bill> findFirstByTransactionIdAndType(Long transactionId, @NotNull RequestType type);

    List<Bill> findAllByTransactionIdAndType(Long transactionId, @NotNull RequestType type);

    @Query(value = "select b from  Bill b where b.success=true and b.recipientShortName  in ( :shortNames) and b.transactionId= :tranId")
    List<Bill> findAllBillsWithShortname(@Param("shortNames") List<String> shortNames, @Param("tranId") Long tranId);

    @Modifying
    @Query("update Bill b set b.requestJson = ?3, b.requestTime = ?2 where b.id = ?1")
    int setRequestJson(Long billId, Instant requestTime, Map<String, Object> requestJson);

}
