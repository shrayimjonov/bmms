package uz.paykap.repository;

import java.util.List;
import java.util.Optional;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import org.javers.spring.annotation.JaversSpringDataAuditable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import uz.devops.bmcore.projection.GatewayResultParamProjection;
import uz.paykap.domain.Recipient;

/**
 * Spring Data SQL repository for the Recipient entity.
 */
@Repository
@JaversSpringDataAuditable
public interface RecipientRepository extends JpaRepository<Recipient, Long>, JpaSpecificationExecutor<Recipient> {
    @Query(
        value = "select distinct recipient from Recipient recipient left join fetch recipient.titles left join fetch recipient.fullNames",
        countQuery = "select count(distinct recipient) from Recipient recipient"
    )
    Page<Recipient> findAllWithEagerRelationships(Pageable pageable);

    @Query("select distinct recipient from Recipient recipient left join fetch recipient.titles left join fetch recipient.fullNames")
    List<Recipient> findAllWithEagerRelationships();

    @Query(
        "select recipient from Recipient recipient left join fetch recipient.titles left join fetch recipient.fullNames where recipient.id =:id"
    )
    Optional<Recipient> findOneWithEagerRelationships(@Param("id") Long id);

    @Query(
        value = "select distinct recipient from Recipient recipient left join fetch recipient.titles left join fetch recipient.fullNames where recipient.active = true AND recipient.id in (select ar.recipient.id from AgentRecipient ar where ar.agent.id = :agentId and ar.active = true)"
    )
    List<Recipient> findAllAllowed(@Param("agentId") Long agentId);

    @Query(
        value = "select distinct recipient from Recipient recipient left join fetch recipient.titles left join fetch recipient.fullNames where recipient.active = true AND recipient.id = :recipientId AND recipient.id in (select ar.recipient.id from AgentRecipient ar where ar.agent.shortName = :username and ar.active = true )"
    )
    List<Recipient> findAllowedRecipient(@Param("recipientId") Long recipientId, @Param("username") String username);

    @Query(
        value = "select distinct recipient from Recipient recipient left join fetch recipient.titles left join fetch recipient.fullNames where recipient.active = true AND recipient.inn = :recipientInn AND recipient.id in (select ar.recipient.id from AgentRecipient ar where ar.agent.shortName = ?#{principal.username} and ar.active = true) "
    )
    List<Recipient> findAllowedRecipientByInn(@Param("recipientInn") String recipientInn);

    @Query(
        value = "select distinct recipient from Recipient recipient left join fetch recipient.titles left join fetch recipient.fullNames where recipient.active = true AND recipient.inn = :recipientInn AND recipient.id in (select ar.recipient.id from AgentRecipient ar where ar.agent.shortName = :username and ar.active = true) "
    )
    List<Recipient> findAllowedRecipientByInn(@Param("recipientInn") String recipientInn, @Param("username") String username);

    @Query(
        value = "select distinct recipient from Recipient recipient left join fetch recipient.titles left join fetch recipient.fullNames where recipient.active = true AND recipient.id in (select ar.recipient.id from AgentRecipient ar where ar.active = true and ar.agent.shortName = ?#{principal.username})"
    )
    List<Recipient> getAllowedRecipients(Pageable pageable);

    @Query(
        "select recipient.shortName from Recipient recipient where recipient.active = true AND recipient.id in (select ar.recipient.id from AgentRecipient ar where ar.active = true and ar.agent.shortName = ?#{principal.username})"
    )
    Page<String> getAllowedRecipientNames(Pageable pageable);

    Optional<Recipient> findTop1ByShortName(@NotNull @Size(max = 64) String shortName);

    @Query(
        "select recipient from Recipient recipient left join fetch recipient.titles left join fetch recipient.fullNames where recipient.shortName =:shortName"
    )
    Optional<Recipient> findOneWithEagerRelationships(@Param("shortName") String shortName);

    Optional<Recipient> findTop1ByShortNameAndGateway_Name(
        @NotNull @Size(max = 64) String shortName,
        @NotNull @Size(max = 64) String gateway_name
    );

    Optional<Recipient> findTop1ByIdAndActiveIsTrue(@NotNull Long id);

    Optional<Recipient> findTop1ByGatewayName(@NotNull @Size(max = 64) String gateway_name);

    @Query(
        value = "select distinct recipient from Recipient recipient left join fetch recipient.gateway where recipient.gateway.springBeanName = :springBeanName"
    )
    List<Recipient> findAllWithGatewayBySpringBeanName(@Param("springBeanName") String gateway_springBeanName);

    List<Recipient> findAllByGatewaySpringBeanName(@Size(max = 128) String gateway_springBeanName);

    List<Recipient> findAllByGatewaySpringBeanNameAndActiveIsTrue(@Size(max = 128) String gateway_springBeanName);

    @Deprecated
    @Query(
        "select g.active as gatewayActive, r.active as recipientActive, g.springBeanName as gatewayBean \n" +
        "from Gateway g \n" +
        "       left join Recipient r on r.gateway.id = g.id \n" +
        "where r.id = :recipientId"
    )
    Optional<GatewayResultParamProjection> getGatewayBeanName(@Param("recipientId") Long recipientId);

    @Query(
        "select g.active as gatewayActive, r.active as recipientActive, g.springBeanName as gatewayBean \n" +
        "from Gateway g\n" +
        "       left join Recipient r on r.gateway.id = g.id\n" +
        "where r.shortName = :recipientShortName"
    )
    Optional<GatewayResultParamProjection> getByRecipientShortName(@Param("recipientShortName") String recipientShortName);

    @Deprecated
    @Query(
        "select g.active as gatewayActive, r.active as recipientActive, g.springBeanName as gatewayBean, r.id as recipientId \n" +
        "from Gateway g\n" +
        "       left join Recipient r on r.gateway.id = g.id\n" +
        "       left join AgentRecipient ar on ar.recipient.id = r.id\n" +
        "where r.shortName = :recipientName AND ar.agent.shortName = :login"
    )
    Optional<GatewayResultParamProjection> getGatewayBeanName(@Param("recipientName") String recipientName, @Param("login") String login);

    @Query(
        "select " +
        "g.active as gatewayActive, " +
        "r.active as recipientActive, " +
        "ar.active as agentRecipientActive, " +
        "r.check as hasCheck, " +
        "r.pay as hasPay, " +
        "r.status as hasStatus, " +
        "r.cancel as hasCancel, " +
        "r.canDeposit as hasDeposit, " +
        "g.springBeanName as gatewayBean, " +
        "g.name as gatewayName, " +
        "r.id as recipientId, " +
        "r.shortName as recipientShortName, " +
        "ar.agentFee as agentFee, " +
        "ar.systemFee as systemFee, " +
        "ar.systemCommissionUp as systemCommissionUp, " +
        "ar.systemCommissionDown as systemCommissionDown, " +
        "r.amountRatio as recipientAmountRatio, " +
        "r.ccy as recipientCcy, " +
        "r.minAmount as recipientMinAmount, " +
        "r.maxAmount as recipientMaxAmount, " +
        "r.limitRatio as recipientLimitRatio, " +
        "ar.eposMerchant as eposMerchant, " +
        "ar.eposTerminal as eposTerminal, " +
        "ar.eposPort as eposPort, " +
        "ar.agent.currencies, " +
        "r.type as recipientType, " +
        "r.serviceId as recipientServiceId, " +
        "ar.setting.id as settingId, " +
        "ar.jsonParams as jsonParams " +
        //            ", " +
        //            "r.category.shortName as category " +
        "from Gateway g\n" +
        "       left join Recipient r on r.gateway.id = g.id\n" +
        "       left join AgentRecipient ar on ar.recipient.id = r.id\n" +
        "where r.id = :recipientId AND ar.agent.shortName = :login AND (ar.requestGroup = :requestGroup OR ar.requestGroup is NULL OR ar.requestGroup = :login) order by ar.requestGroup ASC"
    )
    List<GatewayResultParamProjection> getGatewayQueryResultV4(
        @Param("recipientId") Long recipientId,
        @Param("login") String login,
        @Param("requestGroup") String requestGroup
    );

    @Query(
        "select " +
        "g.active as gatewayActive, " +
        "r.active as recipientActive, " +
        "ar.active as agentRecipientActive, " +
        "r.check as hasCheck, " +
        "r.pay as hasPay, " +
        "r.status as hasStatus, " +
        "r.cancel as hasCancel, " +
        "r.canDeposit as hasDeposit, " +
        "g.springBeanName as gatewayBean, " +
        "g.name as gatewayName, " +
        "r.id as recipientId, " +
        "r.shortName as recipientShortName, " +
        "ar.agentFee as agentFee, " +
        "ar.systemFee as systemFee, " +
        "ar.systemCommissionUp as systemCommissionUp, " +
        "ar.systemCommissionDown as systemCommissionDown, " +
        "r.amountRatio as recipientAmountRatio, " +
        "r.ccy as recipientCcy, " +
        "r.minAmount as recipientMinAmount, " +
        "r.maxAmount as recipientMaxAmount, " +
        "r.limitRatio as recipientLimitRatio, " +
        "ar.eposMerchant as eposMerchant, " +
        "ar.eposTerminal as eposTerminal, " +
        "ar.eposPort as eposPort, " +
        "ar.agent.currencies, " +
        "r.type as recipientType, " +
        "r.serviceId as recipientServiceId, " +
        "ar.setting.id as settingId, " +
        "ar.jsonParams as jsonParams, " +
        "ar.agent.currencies as agentCurrencies " +
        ", " +
        "c.shortName as category " +
        "from Gateway g\n" +
        "       left join Recipient r on r.gateway.id = g.id\n" +
        "       left join AgentRecipient ar on ar.recipient.id = r.id\n" +
        "       left join Category c on r.category.id = c.id \n" +
        "where r.id = :recipientId AND ar.agent.shortName = :login AND (ar.requestGroup = :requestGroup OR ar.requestGroup is NULL OR ar.requestGroup = :login) order by ar.requestGroup ASC"
    )
    List<GatewayResultParamProjection> getGatewayQueryResultV5(
        @Param("recipientId") Long recipientId,
        @Param("login") String login,
        @Param("requestGroup") String requestGroup,
        Pageable pageable
    );

    Optional<Recipient> getRecipientByShortName(@NotNull @Size(max = 64) String shortName);

    List<Recipient> findAllByGateway_Id(Long id);

    List<Recipient> findAllByInnAndActiveIsTrue(@Size(max = 32) String inn);

    List<Recipient> findAllByGatewayName(@NotNull @Size(max = 64) String gateway_name);

    @Query(value = "select * from f_delete_recipient_id_cascade(?1)", nativeQuery = true)
    Integer deleteCascaded(Long recipientId);
}
