package uz.paykap.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import uz.paykap.domain.Transaction;
import uz.paykap.gateway.dto.GatewayTranDTO;

import java.time.Instant;
import java.util.HashSet;


/**
 * Spring Data  repository for the Transaction entity.
 */
@SuppressWarnings("unused")
@Repository
public interface ReconciliationRepository extends JpaRepository<Transaction, Long>, JpaSpecificationExecutor<Transaction> {

    @Query(value = "select t.id, t.gateway_amount as amount, t.recipient_account as account, t.recipient_tr_id as extId, t.recipient_success as succeess, FALSE as reversal, FALSE as isGatewayTransaction from transaction t where t.recipient_gateway = :gatewayName and t.jhi_date >= :fromDate and t.jhi_date < :toDate and t.status = 'SUCCESS' order by t.jhi_date desc", nativeQuery = true)
    HashSet<GatewayTranDTO> getSetByGateway(@Param("gatewayName") String gatewayName, @Param("fromDate") Instant fromDate, @Param("toDate") Instant toDate);
}
