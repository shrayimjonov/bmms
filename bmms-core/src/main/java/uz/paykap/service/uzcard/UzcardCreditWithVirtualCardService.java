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
import uz.paykap.config.ApplicationProperties;
import uz.paykap.domain.Transaction;
import uz.paykap.service.dto.p2p.BmResultStatus;
import uz.paykap.service.p2p.dto.P2pParamResult;
import uz.paykap.service.p2p.dto.SenderAdditionalParam;
import uz.paykap.service.p2p.processor.UzcardParam;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@Service
@AllArgsConstructor
public class UzcardCreditWithVirtualCardService extends AbstractUzcardCreditService {

    private final ApplicationProperties properties;
    private final SvGatePaymentServiceV2 paymentService;

    @Override
    protected HasData<TranDTO> credit(GatewayResultHandler gatewayResultHandler, Transaction transaction, P2pParamResult p2pParam, UzcardParam recipientParam, String senderCard, String recipientCard, String extId, SenderAdditionalParam additionalParam) {

        if (properties.getP2pUzcard() == null
                || properties.getP2pUzcard().getVirtualCardMap() == null
                || properties.getP2pUzcard().getVirtualCardMap().isEmpty()
                || !properties.getP2pUzcard().getVirtualCardMap().containsKey(p2pParam.getSenderPc())
                || !Objects.equals(properties.getP2pUzcard().getVirtualCardMap().get(p2pParam.getSenderPc()).getVirtualCardToken(), recipientParam.getVirtualCardToken())
        ) {

            String noVirtualCardText = "No virtual card token for " + p2pParam.getSenderPc();

//            GatewayResult result = new GatewayResult()
//                    .responseJson(Collections.singletonMap("error", noVirtualCardText))
//                    .responseTime(Instant.now())
//                    .responseText(noVirtualCardText)
//                    .responseCode("FAILED")
//                    .success(false);

            return new CommonResultData<>(BmResultStatus.INVALID_RECIPIENT_PARAMS, null, noVirtualCardText);
        }

        transaction.comment(transaction.getComment() + " (virtual:" + recipientParam.getVirtualCardToken() + ")");

        final HasData<Id2idDTO> id2IdRequest = paymentService.getId2IdRequest(recipientParam.getVirtualCardToken(), recipientCard, transaction.getRecipientAmount(), transaction.getRecipientAmount(), recipientParam.getMerchantId(), recipientParam.getTerminalId(), extId);

        if (!id2IdRequest.isSuccess()) {
            log.warn("Error when making id2id request : {}", id2IdRequest.getMessage());
            return new CommonResultData<>(id2IdRequest, null, id2IdRequest.getMessage());
        }

        gatewayResultHandler.preSaveRequestParams(mapService.convert(id2IdRequest.getData()), Instant.now());

        return paymentService.p2pId2Id(id2IdRequest.getData());
    }

    @Override
    protected Optional<String> resolveRecipientPanOrToken(String panOrToken) {
        return returnCardTokenOrFetchFromSvGate(panOrToken);
    }

    @Override
    protected UzcardCreditService.UzcardCreditType supportsUzcardCreditType() {
        return UzcardCreditService.UzcardCreditType.VIRTUAL_CARD;
    }

}
