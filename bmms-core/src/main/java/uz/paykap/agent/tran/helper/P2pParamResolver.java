package uz.paykap.agent.tran.helper;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uz.paykap.domain.enumeration.P2pProcessorType;
import uz.paykap.repository.P2pParamRepository;
import uz.paykap.service.p2p.dto.P2pParamResult;

import java.util.Optional;

@Slf4j
@Component
@AllArgsConstructor
public class P2pParamResolver {

    // p2pParamRepository.findAll().get(5).getSenderCondition()=6
    //p2pParamRepository.findAll().get(5).getSenderPercent()
    private final P2pParamRepository p2pParamRepository;

//    @Deprecated
//    public Optional<P2pParamResult> getP2pParam(
//            P2pProcessorType senderPc,
//            P2pProcessorType recipientPc,
//            String senderCondition,
//            String recipientCondition
//    ) {
//        return getP2pParam(senderPc, recipientPc, senderCondition, recipientCondition, null);
//    }

    public Optional<P2pParamResult> getP2pParam(
            P2pProcessorType senderPc,
            P2pProcessorType recipientPc,
            String senderCondition,
            String recipientCondition,
            String promo) {
        log.debug("getP2pParam({}, {}, {}, {})", senderPc, recipientPc, senderCondition, recipientCondition);

        Optional<P2pParamResult> paramResult;

        paramResult = p2pParamRepository.findBestMatchNative2WithPromo(
                String.valueOf(senderPc),
                String.valueOf(recipientPc),
                senderCondition,
                recipientCondition,
                promo
        );

        logResult(senderCondition, recipientCondition, paramResult);

        return paramResult;
    }

//    @Deprecated
//    public Optional<P2pParamResult> getP2pParamById(String senderCondition, String recipientCondition) {
//        return getP2pParamById(senderCondition, recipientCondition, null);
//    }

    public Optional<P2pParamResult> getP2pParamById(String senderCondition, String recipientCondition, String promo) {
        log.debug("getP2pParamById({}, {}, {})", senderCondition, recipientCondition, promo);

        Optional<P2pParamResult> paramResult = p2pParamRepository.findBestMatchNativeById(senderCondition, recipientCondition, promo);

        logResult(senderCondition, recipientCondition, paramResult);

        return paramResult;
    }


    private void logResult(String senderCondition, String recipientCondition, Optional<P2pParamResult> paramResult) {
        if (paramResult.isPresent()) {
            log.debug("P2P Params found : {}", paramResult.get().print());
        } else {
            log.warn("P2P Params not found for {} -> {}", senderCondition, recipientCondition);
        }
    }
}
