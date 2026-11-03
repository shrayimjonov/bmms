package uz.paykap.repository;

import java.util.List;
import java.util.List;
import java.util.Optional;
import java.util.Optional;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import org.javers.spring.annotation.JaversSpringDataAuditable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import uz.paykap.domain.FieldValue;

/**
 * Spring Data SQL repository for the FieldValue entity.
 */
@Repository
@JaversSpringDataAuditable
public interface FieldValueRepository extends JpaRepository<FieldValue, Long>, JpaSpecificationExecutor<FieldValue> {
    @Query(
        value = "select distinct fieldValue from FieldValue fieldValue left join fetch fieldValue.titles left join fetch fieldValue.descriptions",
        countQuery = "select count(distinct fieldValue) from FieldValue fieldValue"
    )
    Page<FieldValue> findAllWithEagerRelationships(Pageable pageable);

    @Query(
        "select distinct fieldValue from FieldValue fieldValue left join fetch fieldValue.titles left join fetch fieldValue.descriptions"
    )
    List<FieldValue> findAllWithEagerRelationships();

    @Query(
        "select fieldValue from FieldValue fieldValue left join fetch fieldValue.titles left join fetch fieldValue.descriptions where fieldValue.id =:id"
    )
    Optional<FieldValue> findOneWithEagerRelationships(@Param("id") Long id);

    Optional<FieldValue> findTop1ByFieldIdAndValue(Long field_id, @NotNull @Size(max = 64) String value);

    Optional<FieldValue> findTop1ByFieldIdOrderByOrdAsc(Long field_id);

    @Query(value = "select * from f_delete_field_value_cascade(?1)", nativeQuery = true)
    Integer deleteCascaded(Long fieldValueId);
}
