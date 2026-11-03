package uz.paykap.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import uz.paykap.domain.SimpleRecipient;

/**
 * Spring Data  repository for the Category entity.
 */
@SuppressWarnings("unused")
@Repository
public interface SimpleRecipientRepository extends org.springframework.data.repository.Repository<SimpleRecipient, Long> {

    @Query("select DISTINCT r " +
        "from SimpleRecipient r" +
        "         left join AgentRecipient ar on r.id = ar.recipient.id\n" +
        "         left join Agent a on ar.agent.id = a.id\n" +
        "         left join r.titles t " +
        "where "
        +
        "a.shortName = :agentShortName AND ar.active = true AND a.active = true AND "
        +
        "lower(t.text) like lower(concat('%', :recipientNamePart, '%'))"
    )
    Page<SimpleRecipient> findOnlyAllowedV2(@Param("agentShortName") String agentShortName, @Param("recipientNamePart") String recipientNamePart, Pageable pageable);

    @Query("select DISTINCT r " +
        "from SimpleRecipient r" +
        "         left join AgentRecipient ar on r.id = ar.recipient.id\n" +
        "         left join Agent a on ar.agent.id = a.id\n" +
        "         left join r.titles t " +
        "where "
//        +
//        "a.shortName = :agentShortName AND ar.active = true AND a.active = true AND "
        +
        "lower(t.text) like lower(concat('%', :recipientNamePart, '%'))"
    )
    Page<SimpleRecipient> findOnlyAllowedV2(@Param("recipientNamePart") String recipientNamePart, Pageable pageable);

}
