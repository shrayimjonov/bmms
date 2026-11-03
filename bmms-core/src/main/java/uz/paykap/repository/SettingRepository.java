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
import uz.paykap.domain.Setting;
import uz.paykap.domain.enumeration.Prop;

/**
 * Spring Data SQL repository for the Setting entity.
 */
@Repository
@JaversSpringDataAuditable
public interface SettingRepository extends JpaRepository<Setting, Long>, JpaSpecificationExecutor<Setting> {
    @Query(
        value = "select distinct setting from Setting setting left join fetch setting.props",
        countQuery = "select count(distinct setting) from Setting setting"
    )
    Page<Setting> findAllWithEagerRelationships(Pageable pageable);

    @Query("select distinct setting from Setting setting left join fetch setting.props")
    List<Setting> findAllWithEagerRelationships();

    @Query("select setting from Setting setting left join fetch setting.props where setting.id =:id")
    Optional<Setting> findOneWithEagerRelationships(@Param("id") Long id);

    Optional<Setting> findTop1ByShortName(@NotNull @Size(max = 64) String shortName);

    Optional<Setting> findTop1ById(Long id);

    List<Setting> findAllByActiveTrue();
    //    @Query(value = "select distinct props.value from Setting setting left join fetch setting.props props where setting.id = :settingId and props.key = :key")
    //    List<String> findPropValues(@Param("settingId") Long settingId, @Param("key") Prop key);

}
