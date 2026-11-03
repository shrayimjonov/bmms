package uz.paykap.repository.impl;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.transaction.annotation.Transactional;
import uz.paykap.domain.AbstractAuditingEntity;
import uz.paykap.repository.BaseRepository;
import uz.paykap.security.SecurityUtils;

import javax.persistence.EntityManager;
import java.io.Serializable;
import java.time.Instant;
import java.util.Collections;
import java.util.List;

public class BaseRepositoryImpl<T extends AbstractAuditingEntity, ID extends Serializable>
    extends SimpleJpaRepository<T, ID> implements BaseRepository<T, ID> {

    private static final String CREATED_BY = "createdBy";
    private static final String LAST_MODIFIED_DATE = "lastModifiedDate";

    public BaseRepositoryImpl(JpaEntityInformation<T, ?> entityInformation, EntityManager entityManager) {
        super(entityInformation, entityManager);
    }

    @Transactional(readOnly = true)
    public Page<T> findByCreator(Pageable pageable) {
        return SecurityUtils.getCurrentUserLogin().map(s -> findByCreator(s, pageable)).orElseGet(() -> new PageImpl<>(Collections.emptyList()));
    }

    @Transactional(readOnly = true)
    public List<T> findByCreator() {
        return SecurityUtils.getCurrentUserLogin().map(this::findByCreator).orElseGet(Collections::emptyList);
    }

    @Transactional(readOnly = true)
    public Page<T> findByCreator(String login, Pageable pageable) {
        return findAll((root, query, cb) -> cb.equal(root.get(CREATED_BY), login), pageable);
    }

    @Transactional(readOnly = true)
    public List<T> findByCreator(String login) {
        return getQuery((root, query, cb) -> cb.equal(root.get(CREATED_BY), login), (Sort) null).getResultList();
    }

    @Transactional(readOnly = true)
    public List<T> findLast(Long epochSeconds) {
        return findLast(Instant.ofEpochSecond(epochSeconds));
    }

    @Transactional(readOnly = true)
    public List<T> findLast(Instant time) {
        return getQuery((root, query, cb) -> cb.greaterThanOrEqualTo(root.get(LAST_MODIFIED_DATE), time), (Sort) null).getResultList();
    }

    @Transactional(readOnly = true)
    public List<T> findLastByCreator(Long epochSeconds) {
        return findLastByCreator(Instant.ofEpochSecond(epochSeconds));
    }

    @Transactional(readOnly = true)
    public List<T> findLastByCreator(String login, Long epochSeconds) {
        return findLastByCreator(login, Instant.ofEpochSecond(epochSeconds));
    }

    @Transactional(readOnly = true)
    public List<T> findLastByCreator(Instant time) {
        return SecurityUtils.getCurrentUserLogin().map(s -> findLastByCreator(s, time)).orElseGet(Collections::emptyList);
    }

    @Transactional(readOnly = true)
    public List<T> findLastByCreator(String login, Instant time) {
        return getQuery((root, query, cb) -> cb.and(cb.greaterThanOrEqualTo(root.get(LAST_MODIFIED_DATE), time), cb.equal(root.get(CREATED_BY), login)), (Sort) null).getResultList();
    }
}
