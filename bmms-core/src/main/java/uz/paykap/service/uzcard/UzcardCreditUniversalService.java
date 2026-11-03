package uz.paykap.service.uzcard;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uz.devops.bmcore.gateway.GatewayResultHandler;
import uz.javlon.commons.result.HasData;
import uz.javlon.svgate.dto.TranDTO;
import uz.javlon.svgate.dto.sv.P2pUniversalRequest;
import uz.javlon.svgate.service.SvGateUniversalServiceV2;
import uz.paykap.domain.Transaction;
import uz.paykap.service.p2p.dto.P2pParamResult;
import uz.paykap.service.p2p.dto.SenderAdditionalParam;
import uz.paykap.service.p2p.processor.UzcardParam;

import java.time.Instant;
import java.util.Optional;

@Slf4j
@Service
@AllArgsConstructor
public class UzcardCreditUniversalService extends AbstractUzcardCreditService {

    private final SvGateUniversalServiceV2 universalServiceV2;

    @Override
    protected HasData<TranDTO> credit(GatewayResultHandler gatewayResultHandler, Transaction transaction, P2pParamResult p2pParam, UzcardParam recipientParam, String senderCard, String recipientCard, String extId, SenderAdditionalParam additionalParam) {

        Long feeAmount = transaction.getSystemCommissionUpAmount();

        if (feeAmount == 0) {
            feeAmount = null;
        }

        P2pUniversalRequest p2pUniversalRequest = new P2pUniversalRequest()
                .withSender(senderCard)
                .withAmount(transaction.getRecipientAmount())
                .withFeeAmount(feeAmount)
                .withExt(extId)
                .withMerchantId(recipientParam.getMerchantId())
                .withTerminalId(recipientParam.getTerminalId())
                .withRecipient(recipientCard);

        log.debug("Sending Uzcard Universal credit : {}", p2pUniversalRequest);

        gatewayResultHandler.preSaveRequestParams(mapService.convert(p2pUniversalRequest), Instant.now());

        return universalServiceV2.p2pUniversal(p2pUniversalRequest);
    }

    @Override
    protected Optional<String> resolveRecipientPanOrToken(String panOrToken) {
        return Optional.of(panOrToken);
    }

    @Override
    protected UzcardCreditService.UzcardCreditType supportsUzcardCreditType() {
        return UzcardCreditService.UzcardCreditType.UNIVERSAL;
    }
}
