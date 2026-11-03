package uz.paykap.repository;

import org.javers.spring.annotation.JaversSpringDataAuditable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import uz.paykap.domain.ResponseField;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;
import java.util.Optional;

/**
 * Spring Data SQL repository for the ResponseField entity.
 */
@Repository
@JaversSpringDataAuditable
public interface ResponseFieldRepository extends JpaRepository<ResponseField, Long>, JpaSpecificationExecutor<ResponseField> {
    @Query(
        value = "select distinct responseField from ResponseField responseField left join fetch responseField.labels",
        countQuery = "select count(distinct responseField) from ResponseField responseField"
    )
    Page<ResponseField> findAllWithEagerRelationships(Pageable pageable);

    @Query("select distinct responseField from ResponseField responseField left join fetch responseField.labels")
    List<ResponseField> findAllWithEagerRelationships();

    @Query("select responseField from ResponseField responseField left join fetch responseField.labels where responseField.id =:id")
    Optional<ResponseField> findOneWithEagerRelationships(@Param("id") Long id);

    Optional<ResponseField> findTop1ByNameAndRecipientId(@NotNull @Size(max = 128) String name, Long recipientId);

    @Query(
        value = "select distinct responseField from ResponseField responseField where responseField.recipient.active = true AND responseField.recipient.id in (select ar.recipient.id from AgentRecipient ar where ar.active = true and ar.agent.id = :agentId)"
    )
    List<ResponseField> findAllAllowed(@Param("agentId") Long agentId);

    @Query(
        value = "select distinct responseField from ResponseField responseField where responseField.visibility <> 'HIDDEN' and responseField.recipient.active = true AND responseField.recipient.id in (select ar.recipient.id from AgentRecipient ar where ar.active = true and ar.agent.id = :agentId)"
    )
    List<ResponseField> findAllAllowedAndNotHidden(@Param("agentId") Long agentId);

    @Query(
        value = "select distinct field from ResponseField field where field.recipient.active = true AND field.recipient.id = :recipientId AND field.recipient.id in (select ar.recipient.id from AgentRecipient ar where ar.active = true and ar.agent.shortName = :username)"
    )
    List<ResponseField> findByLoginAndRecipientId(@Param("recipientId") Long recipientId, @Param("username") String username);

    @Query(
        value = "select distinct field from ResponseField field where field.visibility <> 'HIDDEN' and field.recipient.active = true AND field.recipient.id = :recipientId AND field.recipient.id in (select ar.recipient.id from AgentRecipient ar where ar.active = true and ar.agent.shortName = :username)"
    )
    List<ResponseField> findAllowedByRecipientId(@Param("recipientId") Long recipientId, @Param("username") String username);
}
