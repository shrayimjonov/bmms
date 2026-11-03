package uz.paykap.repository;

import java.util.List;
import java.util.Optional;

import org.javers.spring.annotation.JaversSpringDataAuditable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import uz.devops.bmcore.enumeration.RequestType;
import uz.paykap.domain.GatewayParam;
import uz.paykap.domain.Recipient;

/**
 * Spring Data SQL repository for the GatewayParam entity.
 */
@SuppressWarnings("unused")
@Repository
@JaversSpringDataAuditable
public interface GatewayParamRepository extends JpaRepository<GatewayParam, Long>, JpaSpecificationExecutor<GatewayParam> {
    Optional<GatewayParam> findTop1ByRecipientIdAndTypeAndActiveIsTrue(Long recipientId, RequestType type);

    Optional<GatewayParam> findTop1ByRecipientGatewayNameAndTypeAndActiveIsTrue(String gatewayName, RequestType type);

    Optional<GatewayParam> findTop1ByRecipientShortNameAndTypeAndActiveIsTrue(String recipientShortName, RequestType type);

    Optional<GatewayParam> findTop1ByRecipientAndType(Recipient recipient, RequestType type);

    List<GatewayParam> findAllByRecipient_Id(Long recipient_id);
}
