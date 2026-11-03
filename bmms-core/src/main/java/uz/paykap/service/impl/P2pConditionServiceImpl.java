package uz.paykap.service.impl;

import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.paykap.domain.P2pCondition;
import uz.paykap.repository.P2pConditionRepository;
import uz.paykap.service.P2pConditionService;

/**
 * Service Implementation for managing {@link P2pCondition}.
 */
@Service
@Transactional
public class P2pConditionServiceImpl implements P2pConditionService {

    private final Logger log = LoggerFactory.getLogger(P2pConditionServiceImpl.class);

    private final P2pConditionRepository p2pConditionRepository;

    public P2pConditionServiceImpl(P2pConditionRepository p2pConditionRepository) {
        this.p2pConditionRepository = p2pConditionRepository;
    }

    @Override
    public P2pCondition save(P2pCondition p2pCondition) {
        log.debug("Request to save P2pCondition : {}", p2pCondition);
        return p2pConditionRepository.save(p2pCondition);
    }

    @Override
    public Optional<P2pCondition> partialUpdate(P2pCondition p2pCondition) {
        log.debug("Request to partially update P2pCondition : {}", p2pCondition);

        return p2pConditionRepository
            .findById(p2pCondition.getId())
            .map(
                existingP2pCondition -> {
                    if (p2pCondition.getValue() != null) {
                        existingP2pCondition.setValue(p2pCondition.getValue());
                    }
                    if (p2pCondition.getDescription() != null) {
                        existingP2pCondition.setDescription(p2pCondition.getDescription());
                    }

                    return existingP2pCondition;
                }
            )
            .map(p2pConditionRepository::save);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<P2pCondition> findAll(Pageable pageable) {
        log.debug("Request to get all P2pConditions");
        return p2pConditionRepository.findAll(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<P2pCondition> findOne(Long id) {
        log.debug("Request to get P2pCondition : {}", id);
        return p2pConditionRepository.findById(id);
    }

    @Override
    public void delete(Long id) {
        log.debug("Request to delete P2pCondition : {}", id);
        p2pConditionRepository.deleteById(id);
    }
}
