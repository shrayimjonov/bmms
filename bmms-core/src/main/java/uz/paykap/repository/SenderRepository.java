package uz.paykap.repository;

import java.util.List;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import uz.devops.bmcore.enumeration.PayType;
import uz.paykap.domain.Sender;

/**
 * Spring Data SQL repository for the Sender entity.
 */
@SuppressWarnings("unused")
@Repository
public interface SenderRepository extends JpaRepository<Sender, Long>, JpaSpecificationExecutor<Sender> {
    List<Sender> findAllByAgent_ShortNameAndPayTypeOrderByOrd(@NotNull @Size(max = 64) String agent_shortName, @NotNull PayType payType);
}
