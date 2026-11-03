package uz.paykap.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;
import uz.paykap.domain.AbstractAuditingEntity;

import java.io.Serializable;
import java.time.Instant;
import java.util.List;

@NoRepositoryBean
public interface BaseRepository<T extends AbstractAuditingEntity, ID extends Serializable> extends JpaRepository<T, ID> {

    Page<T> findByCreator(Pageable pageable);

    List<T> findByCreator();

    Page<T> findByCreator(String login, Pageable pageable);

    List<T> findByCreator(String login);

    List<T> findLast(Long time);

    List<T> findLast(Instant time);

    List<T> findLastByCreator(Long time);

    List<T> findLastByCreator(String login, Long time);

    List<T> findLastByCreator(Instant time);

    List<T> findLastByCreator(String login, Instant time);

}
