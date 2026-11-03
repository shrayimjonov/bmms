package uz.paykap.service.uzcard;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uz.devops.bmcore.gateway.GatewayResultHandler;
import uz.javlon.commons.result.CommonResultData;
import uz.javlon.commons.result.HasData;
import uz.javlon.svgate.dto.TranDTO;
import uz.javlon.svgate.dto.sv.Id2idDTO;
import uz.javlon.svgate.service.SvGatePaymentServiceV2;
import uz.paykap.domain.Transaction;
import uz.paykap.service.MapService;
import uz.paykap.service.p2p.dto.P2pParamResult;
import uz.paykap.service.p2p.dto.SenderAdditionalParam;
import uz.paykap.service.p2p.processor.UzcardParam;

import java.time.Instant;
import java.util.Optional;

@Slf4j
@Service
@AllArgsConstructor
public class UzcardCreditId2IdService extends AbstractUzcardCreditService {

    private final SvGatePaymentServiceV2 paymentService;

    @Override
    protected HasData<TranDTO> credit(GatewayResultHandler gatewayResultHandler, Transaction transaction, P2pParamResult p2pParam, UzcardParam recipientParam, String senderCard, String recipientCard, String extId, SenderAdditionalParam additionalParam) {

        final HasData<Id2idDTO> id2IdRequest = paymentService.getId2IdRequest(senderCard, recipientCard, transaction.getSenderAmount(), transaction.getRecipientAmount(), recipientParam.getMerchantId(), recipientParam.getTerminalId(), extId);

        if (!id2IdRequest.isSuccess()) {
            log.warn("Error when making id2id request : {}", id2IdRequest.getMessage());
            return new CommonResultData<>(id2IdRequest, null, id2IdRequest.getMessage());
        }

        gatewayResultHandler.preSaveRequestParams(mapService.convert(id2IdRequest.getData()), Instant.now());

        return paymentService.p2pId2Id(id2IdRequest.getData());
    }

    @Override
    protected Optional<String> resolveSenderToken(String panOrToken) {
        return returnCardTokenOrFetchFromSvGate(panOrToken);
    }

    @Override
    protected Optional<String> resolveRecipientPanOrToken(String panOrToken) {
        return returnCardTokenOrFetchFromSvGate(panOrToken);
    }

    @Override
    protected UzcardCreditService.UzcardCreditType supportsUzcardCreditType() {
        return UzcardCreditService.UzcardCreditType.UZCARD_P2P;
    }
}
