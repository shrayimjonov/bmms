package uz.paykap.gateway.uzcard;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uz.javlon.commons.exception.CodedException;
import uz.javlon.commons.exception.ProblemEnum;
import uz.javlon.commons.result.HasData;
import uz.javlon.svgate.dto.sv.SvCardV2DTO;
import uz.javlon.svgate.dto.sv.SvResponseWithData;
import uz.javlon.svgate.service.SvGateCardServiceV2;
import uz.javlon.svgate.service.SvGatePaymentServiceV2;

import java.util.Collections;
import java.util.List;

@Slf4j
@Service
public class SvGateHelperService {
    private final SvGatePaymentServiceV2 svGatePaymentServiceV2;
    private final SvGateCardServiceV2 svGateCardServiceV2;

    public SvGateHelperService(SvGatePaymentServiceV2 svGatePaymentServiceV2, SvGateCardServiceV2 svGateCardServiceV2) {
        this.svGatePaymentServiceV2 = svGatePaymentServiceV2;
        this.svGateCardServiceV2 = svGateCardServiceV2;
    }

    public HasData<List<SvCardV2DTO>> getSvCard(String pan) {
        log.debug("P2p info request pan : {}", pan);
        var p2pInfoDTO = svGatePaymentServiceV2.p2pInfo(pan);
        log.debug("P2p info response : {}", p2pInfoDTO);
        if (p2pInfoDTO == null || p2pInfoDTO.getCardToken() == null || p2pInfoDTO.getCardToken().isEmpty()) {
            log.warn("UzCard token not found on pan : {}", pan);
            throw new CodedException(ProblemEnum.CARD_NOT_VALID);
        }
        log.debug("Sv gate get card request token :{}", p2pInfoDTO.getCardToken());
        var cards = svGateCardServiceV2.getCards(Collections.singletonList(p2pInfoDTO.getCardToken()));
        log.debug("Sv get card response : {}", cards);

        if (cards == null || !cards.isSuccess() || cards.getData() == null) {
            log.warn("UzCard card not found on token : {}", p2pInfoDTO.getCardToken());
            throw new CodedException(ProblemEnum.CARD_NOT_FOUND);
        }
        return cards;
    }
}
