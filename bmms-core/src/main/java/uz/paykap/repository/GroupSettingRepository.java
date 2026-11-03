package uz.paykap.repository;

import java.util.List;
import java.util.Optional;
import org.javers.spring.annotation.JaversSpringDataAuditable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import uz.paykap.domain.GroupSetting;

/**
 * Spring Data SQL repository for the GroupSetting entity.
 */
@Repository
@JaversSpringDataAuditable
public interface GroupSettingRepository extends JpaRepository<GroupSetting, Long>, JpaSpecificationExecutor<GroupSetting> {
    @Query(
        value = "select distinct groupSetting from GroupSetting groupSetting left join fetch groupSetting.props",
        countQuery = "select count(distinct groupSetting) from GroupSetting groupSetting"
    )
    Page<GroupSetting> findAllWithEagerRelationships(Pageable pageable);

    @Query("select distinct groupSetting from GroupSetting groupSetting left join fetch groupSetting.props")
    List<GroupSetting> findAllWithEagerRelationships();

    @Query("select groupSetting from GroupSetting groupSetting left join fetch groupSetting.props where groupSetting.id =:id")
    Optional<GroupSetting> findOneWithEagerRelationships(@Param("id") Long id);

//    @Query(
//            "select group_setting from GroupSetting group_setting left join fetch group_setting.props where group_setting.settingType.id =:settingTypeId and group_setting.requestGroup = :requestGroup"
//    )
    @Query(
        "select group_setting from GroupSetting group_setting left join fetch group_setting.props where group_setting.requestGroup = :requestGroup"
    )
    Optional<GroupSetting> findOne(@Param("requestGroup") String requestGroup);

//    @Query(
//            "select group_setting from GroupSetting group_setting " +
//                    "left join fetch group_setting.props " +
//                    "where group_setting.settingType.id in (select distinct ar.settingType.id from AgentRecipient ar where ar.recipient.id = :recipientId and ar.agent.shortName = :agentLogin) and group_setting.requestGroup = :requestGroup"
//    )
    @Query(
        "select group_setting from GroupSetting group_setting " +
        "left join fetch group_setting.props " +
        "where group_setting.requestGroup = :requestGroup"
    )
    Optional<GroupSetting> findByRecipientAndGroup(
//        @Param("recipientId") Long recipientId,
        @Param("requestGroup") String requestGroup
//            ,
//        @Param("agentLogin") String agentLogin
    );
}
