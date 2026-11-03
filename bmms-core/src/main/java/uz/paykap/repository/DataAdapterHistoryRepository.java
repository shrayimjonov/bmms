package uz.paykap.repository;

import java.util.List;
import org.javers.spring.annotation.JaversSpringDataAuditable;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;
import uz.paykap.domain.DataAdapterHistory;

/**
 * Spring Data SQL repository for the DataAdapterHistory entity.
 */
@SuppressWarnings("unused")
@Repository
//@JaversSpringDataAuditable
public interface DataAdapterHistoryRepository extends JpaRepository<DataAdapterHistory, Long> {
    List<DataAdapterHistory> findByName(String name);
}
