package uz.paykap.repository;

import java.util.Set;
import org.javers.spring.annotation.JaversSpringDataAuditable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import uz.paykap.domain.SettingProperty;

/**
 * Spring Data SQL repository for the SettingProperty entity.
 */
@SuppressWarnings("unused")
@Repository
@JaversSpringDataAuditable
public interface SettingPropertyRepository extends JpaRepository<SettingProperty, Long> {
    @Query(
        value = "select ar.setting.props from AgentRecipient ar where ar.recipient.shortName = :recipientShortName and ar.requestGroup = :requestGroup and ar.agent.shortName = :login"
    )
    Set<SettingProperty> findPropsByRecipientShortName(
        @Param("recipientShortName") String recipientShortName,
        @Param("requestGroup") String requestGroup,
        @Param("login") String login
    );

    @Query(
        value = "select ar.setting.props from AgentRecipient ar where ar.recipient.id = :recipientId and ar.requestGroup = :requestGroup and ar.agent.shortName = :login"
    )
    Set<SettingProperty> findPropsByRecipientId(
        @Param("recipientId") Long recipientId,
        @Param("login") String login,
        @Param("requestGroup") String requestGroup
    );
}
