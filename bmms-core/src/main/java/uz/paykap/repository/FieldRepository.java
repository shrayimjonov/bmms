package uz.paykap.repository;

import org.javers.spring.annotation.JaversSpringDataAuditable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import uz.devops.bmcore.dto.AccountAndAmountNames;
import uz.devops.bmcore.enumeration.FieldType;
import uz.paykap.domain.Field;
import uz.paykap.domain.Recipient;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Spring Data SQL repository for the Field entity.
 */
@Repository
@JaversSpringDataAuditable
public interface FieldRepository extends JpaRepository<Field, Long>, JpaSpecificationExecutor<Field> {
    @Query(
        value = "select distinct field from Field field left join fetch field.titles left join fetch field.descriptions",
        countQuery = "select count(distinct field) from Field field"
    )
    Page<Field> findAllWithEagerRelationships(Pageable pageable);

    @Query("select distinct field from Field field left join fetch field.titles left join fetch field.descriptions")
    List<Field> findAllWithEagerRelationships();

    @Query(
        value = "select distinct field from Field field where field.recipient.active = true AND field.recipient.id in (select ar.recipient.id from AgentRecipient ar where ar.active = true and ar.agent.id = :agentId)"
    )
    List<Field> findAllAllowed(@Param("agentId") Long agentId);

    @Query(
        value = "select distinct field from Field field where field.recipient.active = true AND field.recipient.id = :recipientId AND field.recipient.id in (select ar.recipient.id from AgentRecipient ar where ar.active = true and ar.agent.shortName = :username)"
    )
    List<Field> findFirstByLoginAndRecipientId(@Param("recipientId") Long recipientId, @Param("username") String username);

    @Query("select field from Field field left join fetch field.titles left join fetch field.descriptions where field.id =:id")
    Optional<Field> findOneWithEagerRelationships(@Param("id") Long id);

    List<Field> findAllByRecipient(@NotNull Recipient recipient);

    Optional<Field> findTop1ByNameAndTypeAndRecipientId(@NotNull @Size(max = 64) String name, @NotNull FieldType type, Long recipient_id);

    Optional<Field> findTop1ByNameAndRecipientId(@NotNull @Size(max = 64) String name, Long recipientId);

    Optional<Field> findTop1ByNameAndRecipientIdAndRequireFieldAndFilterBy(@NotNull @Size(max = 64) String name, Long recipient_id, @Size(max = 64) String requireField, @Size(max = 16) String filterBy);

    Optional<Field> findTop1ByNameAndRecipient(@NotNull @Size(max = 64) String name, @NotNull Recipient recipient);

    Optional<Field> findTop1ByTypeAndRecipient(@NotNull FieldType type, @NotNull Recipient recipient);

    Optional<Field> findTop1ByTypeAndRecipientAndName(
        @NotNull FieldType type,
        @NotNull Recipient recipient,
        @NotNull @Size(max = 64) String name
    );

    Optional<Field> findTop1ByName(@NotNull @Size(max = 64) String name);

    Set<Field> findAllByRecipientId(Long recipientId);

    List<Field> findAllByRecipientAndType(@NotNull Recipient recipient, @NotNull FieldType type);

    //    @Query("select distinct field from Field field left join fetch field.values where field.recipient.id =:id")
    //    List<Field> findAllByRecipientIdWithValues(@Param("id") Long id);

    @Query("select distinct field from Field field left join fetch field.values where field.recipient.id =:id")
    List<AccountAndAmountNames> findAccountAndAmount(@Param("id") Long recipientId);

    @Query(value = "select * from f_delete_field_cascade(:fieldId)", nativeQuery = true)
    Integer deleteCascaded(@NotNull @Param("fieldId") Long fieldId);
}
