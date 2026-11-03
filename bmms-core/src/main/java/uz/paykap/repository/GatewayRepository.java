package uz.paykap.repository;

import org.javers.spring.annotation.JaversSpringDataAuditable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import uz.devops.bmcore.dto.GatewayQueryResult;
import uz.devops.bmcore.enumeration.RequestType;
import uz.devops.bmcore.projection.GatewayResultParamProjection;
import uz.paykap.domain.Gateway;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;
import java.util.Optional;

/**
 * Spring Data SQL repository for the Gateway entity.
 */
@SuppressWarnings("unused")
@Repository
@JaversSpringDataAuditable
public interface GatewayRepository extends JpaRepository<Gateway, Long>, JpaSpecificationExecutor<Gateway> {

    Optional<Gateway> findTop1ByName(String name);
    Optional<Gateway> findTop1ByNameAndActiveTrue(String name);
    List<Gateway> findAllByActive(@NotNull Boolean active);
    Optional<Gateway> findTop1BySpringBeanNameAndActiveTrue(@Size(max = 128) String springBeanName);

    @Query("Select DISTINCT springBeanName from Gateway")
    List<String> findDistinctSpringBeanName();

    @Query(
        "select g.active as gatewayActive, r.active as recipientActive, g.springBeanName as gatewayBean, g.name as gatewayName \n" +
        "from Gateway g \n" +
        "       left join Recipient r on r.gateway.id = g.id\n" +
        "       left join Bill b on r.id = b.recipientId\n" +
        "where b.id = :billId \n" +
        "  and b.login = :login \n" +
        "  and b.type = :requestType \n" +
        "  and b.success = :success"
    )
    Optional<GatewayResultParamProjection> getGatewayBeanName(
        @Param("billId") Long billId,
        @Param("login") String login,
        @Param("requestType") RequestType type,
        @Param("success") Boolean success
    );

    @Query(
        "select g.springBeanName as gatewayBean \n" +
        "from Gateway g \n" +
        "       left join Recipient r on r.gateway.id = g.id\n" +
        "       left join Bill b on r.id = b.recipientId\n" +
        "where b.id = :billId \n" +
        "  and b.login = :login \n" +
        "  and b.type = :requestType \n" +
        "  and b.success = :success"
    )
    Optional<String> getOnlyGatewayBeanName(
        @Param("billId") Long billId,
        @Param("login") String login,
        @Param("requestType") RequestType type,
        @Param("success") Boolean success
    );

    @Deprecated
    @Query(
        "select g.active as gatewayActive, r.active as recipientActive, g.springBeanName as gatewayBean \n" +
        "from Gateway g\n" +
        "       left join Recipient r on r.gateway.id = g.id\n" +
        "       left join Bill b on r.id = b.recipientId\n" +
        "where b.id = :billId\n" +
        "  and b.type = :requestType\n" +
        "  and b.success = :success"
    )
    Optional<GatewayResultParamProjection> getGatewayBeanName(
        @Param("billId") Long billId,
        @Param("requestType") RequestType type,
        @Param("success") Boolean success
    );

    @Query(
        "select g.springBeanName as gatewayBean \n" +
        "from Gateway g\n" +
        "       left join Recipient r on r.gateway.id = g.id\n" +
        "       left join Bill b on r.id = b.recipientId\n" +
        "where b.id = :billId\n" +
        "  and b.type = :requestType\n" +
        "  and b.success = :success"
    )
    Optional<String> getOnlyGatewayBeanName(
        @Param("billId") Long billId,
        @Param("requestType") RequestType type,
        @Param("success") Boolean success
    );

    @Query(value = "select * from f_delete_gateway_id_cascade(?1)", nativeQuery = true)
    Integer deleteCascaded(Long gatewayId);
}
