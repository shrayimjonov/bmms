package uz.paykap.service.uzcard;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uz.devops.bmcore.gateway.GatewayResultHandler;
import uz.javlon.commons.result.HasData;
import uz.javlon.svgate.dto.DebitTranDTO;
import uz.javlon.svgate.dto.TranDTO;
import uz.javlon.svgate.service.SvGatePaymentServiceV2;
import uz.paykap.domain.Transaction;
import uz.paykap.service.p2p.dto.P2pParamResult;
import uz.paykap.service.p2p.dto.SenderAdditionalParam;
import uz.paykap.service.p2p.processor.UzcardParam;

import java.time.Instant;
import java.util.Optional;

@Slf4j
@Service
@AllArgsConstructor
public class SimpleUzcardCreditService extends AbstractUzcardCreditService {

    private final SvGatePaymentServiceV2 paymentService;

    @Override
    protected HasData<TranDTO> credit(GatewayResultHandler gatewayResultHandler, Transaction transaction, P2pParamResult p2pParam, UzcardParam recipientParam, String senderCard, String recipientCard, String extId, SenderAdditionalParam additionalParam) {

        final DebitTranDTO request = uzcardHelperService.getRequest(recipientCard, transaction.getRecipientAmount(), extId, recipientParam.getMerchantId(), recipientParam.getTerminalId());

        gatewayResultHandler.preSaveRequestParams(mapService.convert(request), Instant.now());

        return paymentService.credit(request);
    }

    @Override
    protected Optional<String> resolveSenderToken(String panOrToken) {
        return Optional.of(panOrToken);
    }

    @Override
    protected Optional<String> resolveRecipientPanOrToken(String panOrToken) {
        return returnCardTokenOrFetchFromSvGate(panOrToken);
    }

    @Override
    protected UzcardCreditService.UzcardCreditType supportsUzcardCreditType() {
        return UzcardCreditService.UzcardCreditType.UZCARD_CREDIT;
    }
}
