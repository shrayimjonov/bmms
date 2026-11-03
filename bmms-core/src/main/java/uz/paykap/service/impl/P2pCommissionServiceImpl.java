package uz.paykap.service.impl;

import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.paykap.domain.P2pCommission;
import uz.paykap.repository.P2pCommissionRepository;
import uz.paykap.service.P2pCommissionService;

/**
 * Service Implementation for managing {@link P2pCommission}.
 */
@Service
@Transactional
public class P2pCommissionServiceImpl implements P2pCommissionService {

    private final Logger log = LoggerFactory.getLogger(P2pCommissionServiceImpl.class);

    private final P2pCommissionRepository p2pCommissionRepository;

    public P2pCommissionServiceImpl(P2pCommissionRepository p2pCommissionRepository) {
        this.p2pCommissionRepository = p2pCommissionRepository;
    }

    @Override
    public P2pCommission save(P2pCommission p2pCommission) {
        log.debug("Request to save P2pCommission : {}", p2pCommission);
        return p2pCommissionRepository.save(p2pCommission);
    }

    @Override
    public Optional<P2pCommission> partialUpdate(P2pCommission p2pCommission) {
        log.debug("Request to partially update P2pCommission : {}", p2pCommission);

        return p2pCommissionRepository
            .findById(p2pCommission.getId())
            .map(
                existingP2pCommission -> {
                    if (p2pCommission.getName() != null) {
                        existingP2pCommission.setName(p2pCommission.getName());
                    }
                    if (p2pCommission.getUp() != null) {
                        existingP2pCommission.setUp(p2pCommission.getUp());
                    }
                    if (p2pCommission.getDown() != null) {
                        existingP2pCommission.setDown(p2pCommission.getDown());
                    }
                    if (p2pCommission.getFee() != null) {
                        existingP2pCommission.setFee(p2pCommission.getFee());
                    }
                    if (p2pCommission.getDescription() != null) {
                        existingP2pCommission.setDescription(p2pCommission.getDescription());
                    }

                    return existingP2pCommission;
                }
            )
            .map(p2pCommissionRepository::save);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<P2pCommission> findAll(Pageable pageable) {
        log.debug("Request to get all P2pCommissions");
        return p2pCommissionRepository.findAll(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<P2pCommission> findOne(Long id) {
        log.debug("Request to get P2pCommission : {}", id);
        return p2pCommissionRepository.findById(id);
    }

    @Override
    public void delete(Long id) {
        log.debug("Request to delete P2pCommission : {}", id);
        p2pCommissionRepository.deleteById(id);
    }
}
