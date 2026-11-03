package uz.paykap.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import uz.paykap.domain.File;

import java.util.Optional;
import java.util.UUID;

public interface FileRepository extends JpaRepository<File, UUID>, JpaSpecificationExecutor<File> {

    Optional<File> findFirstByName(String fileName);
}
