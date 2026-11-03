package uz.paykap.repository;

import java.util.List;
import java.util.Optional;
import org.javers.spring.annotation.JaversSpringDataAuditable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import uz.paykap.domain.RecipientGroup;

/**
 * Spring Data SQL repository for the RecipientGroup entity.
 */
@Repository
@JaversSpringDataAuditable
public interface RecipientGroupRepository extends JpaRepository<RecipientGroup, Long>, JpaSpecificationExecutor<RecipientGroup> {
    @Query(
        value = "select distinct recipientGroup from RecipientGroup recipientGroup left join fetch recipientGroup.recipients",
        countQuery = "select count(distinct recipientGroup) from RecipientGroup recipientGroup"
    )
    Page<RecipientGroup> findAllWithEagerRelationships(Pageable pageable);

    @Query("select distinct recipientGroup from RecipientGroup recipientGroup left join fetch recipientGroup.recipients")
    List<RecipientGroup> findAllWithEagerRelationships();

    @Query(
        "select recipientGroup from RecipientGroup recipientGroup left join fetch recipientGroup.recipients where recipientGroup.id =:id"
    )
    Optional<RecipientGroup> findOneWithEagerRelationships(@Param("id") Long id);

    Optional<RecipientGroup> findTop1ByName(String name);
}
