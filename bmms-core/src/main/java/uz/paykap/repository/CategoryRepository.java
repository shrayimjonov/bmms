package uz.paykap.repository;

import org.javers.spring.annotation.JaversSpringDataAuditable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import uz.paykap.domain.Category;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;
import java.util.Optional;

/**
 * Spring Data SQL repository for the Category entity.
 */
@Repository
@JaversSpringDataAuditable
public interface CategoryRepository extends JpaRepository<Category, Long>, JpaSpecificationExecutor<Category>/*, BatchInsertable<Category>*/ {
    @Query(
        value = "select distinct category from Category category left join fetch category.names",
        countQuery = "select count(distinct category) from Category category"
    )
    Page<Category> findAllWithEagerRelationships(Pageable pageable);

    @Query("select distinct category from Category category left join fetch category.names")
    List<Category> findAllWithEagerRelationships();

    //    @Query(value = "select distinct category from Category category left join fetch category.names")
    @Query(
        value = "select distinct category from Category category left join fetch category.names where category.active = true AND category.id in (select distinct recipient.category.id from Recipient recipient where recipient.id in (select ar.recipient.id from AgentRecipient ar where ar.agent.id = :agentId))"
    )
    List<Category> findAllAllowed(@Param("agentId") Long agentId);

    @Query(
        value = "with recursive category_and_parents as (\n" +
        "    select *\n" +
        "    from category\n" +
        "    where id in (\n" +
        "        select distinct r.category_id\n" +
        "        from recipient r\n" +
        "                 inner join\n" +
        "             agent_recipient ar on r.id = ar.recipient_id\n" +
        "        where ar.agent_id = :agentId\n" +
        "          and r.category_id IS NOT NULL and r.active = true and ar.active = true\n" +
        "    )\n" +
        "    union all\n" +
        "    select c.*\n" +
        "    from category c\n" +
        "             join category parent_category on parent_category.parent_id = c.id\n" +
        ")\n" +
        "select distinct * \n" +
        "from category_and_parents order by id",
        nativeQuery = true
    )
    List<Category> findAllAllowedWithParents(@Param("agentId") Long agentId);

    @Query("select category from Category category left join fetch category.names where category.id =:id")
    Optional<Category> findOneWithEagerRelationships(@Param("id") Long id);

    Optional<Category> findTop1ByShortName(@NotNull @Size(max = 64) String shortName);
}
