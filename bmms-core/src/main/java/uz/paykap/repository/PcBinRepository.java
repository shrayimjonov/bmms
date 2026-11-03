package uz.paykap.repository;

import uz.paykap.domain.PcBin;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;
import uz.paykap.domain.enumeration.BinPcType;

import javax.validation.constraints.NotNull;
import java.util.List;


/**
 * Spring Data  repository for the PcBin entity.
 */
@SuppressWarnings("unused")
@Repository
public interface PcBinRepository extends JpaRepository<PcBin, Long>, JpaSpecificationExecutor<PcBin> {
    List<PcBin> findAllByPcType(@NotNull BinPcType pcType);
}
