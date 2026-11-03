package uz.paykap.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.javlon.commons.exception.CodedException;
import uz.javlon.commons.exception.ProblemEnum;
import uz.paykap.domain.P2pParam;
import uz.paykap.repository.P2pParamRepository;
import uz.paykap.service.P2pParamService;
import uz.paykap.service.dto.p2p.P2pParamInfoRequest;
import uz.paykap.service.p2p.dto.P2pParamResult;

import java.util.Optional;

/**
 * Service Implementation for managing {@link P2pParam}.
 */
@Service
@Transactional
public class P2pParamServiceImpl implements P2pParamService {

    private final Logger log = LoggerFactory.getLogger(P2pParamServiceImpl.class);

    private final P2pParamRepository p2pParamRepository;

    public P2pParamServiceImpl(P2pParamRepository p2pParamRepository) {
        this.p2pParamRepository = p2pParamRepository;
    }

    @Override
    public P2pParam save(P2pParam p2pParam) {
        log.debug("Request to save P2pParam : {}", p2pParam);
        return p2pParamRepository.save(p2pParam);
    }

    @Override
    public Optional<P2pParam> partialUpdate(P2pParam p2pParam) {
        log.debug("Request to partially update P2pParam : {}", p2pParam);

        return p2pParamRepository
            .findById(p2pParam.getId())
            .map(
                existingP2pParam -> {
                    if (p2pParam.getPriority() != null) {
                        existingP2pParam.setPriority(p2pParam.getPriority());
                    }
                    if (p2pParam.getActive() != null) {
                        existingP2pParam.setActive(p2pParam.getActive());
                    }
                    if (p2pParam.getSenderPc() != null) {
                        existingP2pParam.setSenderPc(p2pParam.getSenderPc());
                    }
                    if (p2pParam.getRecipientPc() != null) {
                        existingP2pParam.setRecipientPc(p2pParam.getRecipientPc());
                    }
                    if (p2pParam.getValue() != null) {
                        existingP2pParam.setValue(p2pParam.getValue());
                    }
                    if (p2pParam.getName() != null) {
                        existingP2pParam.setName(p2pParam.getName());
                    }

                    return existingP2pParam;
                }
            )
            .map(p2pParamRepository::save);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<P2pParam> findAll(Pageable pageable) {
        log.debug("Request to get all P2pParams");
        return p2pParamRepository.findAll(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<P2pParam> findOne(Long id) {
        log.debug("Request to get P2pParam : {}", id);
        return p2pParamRepository.findById(id);
    }

    @Override
    public void delete(Long id) {
        log.debug("Request to delete P2pParam : {}", id);
        p2pParamRepository.deleteById(id);
    }

    public P2pParamResult getP2pParamInfo(P2pParamInfoRequest p2pParamInfoRequest) {
        return p2pParamRepository
            .findBestMatchNative2WithPromo(
                p2pParamInfoRequest.getSenderPc().name(),
                p2pParamInfoRequest.getRecipientPc().name(),
                p2pParamInfoRequest.getSenderAccount(),
                p2pParamInfoRequest.getRecipientAccount(),
                p2pParamInfoRequest.getPromo()
            )
            .orElseThrow(() -> new CodedException(ProblemEnum.NOT_FOUND, "No p2p param match"));
    }
}
