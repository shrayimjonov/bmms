package uz.paykap.service.uzcard;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import uz.devops.bmcore.gateway.GatewayResultHandler;
import uz.javlon.commons.result.HasData;
import uz.javlon.commons.util.CardUtil;
import uz.javlon.svgate.dto.TranDTO;
import uz.javlon.svgate.dto.sv.Doc;
import uz.javlon.svgate.dto.sv.P2pUniversalCreditRequest;
import uz.javlon.svgate.dto.sv.Sender;
import uz.javlon.svgate.service.SvGateUniversalServiceV2;
import uz.paykap.config.ApplicationProperties;
import uz.paykap.domain.Transaction;
import uz.paykap.domain.enumeration.P2pProcessorType;
import uz.paykap.service.PcHelper;
import uz.paykap.service.p2p.dto.P2pParamResult;
import uz.paykap.service.p2p.dto.SenderAdditionalParam;
import uz.paykap.service.p2p.processor.UzcardParam;
import uz.paykap.service.p2p.support.BillHandler;

import java.time.Instant;
import java.util.Optional;

@Slf4j
@Service
@AllArgsConstructor
public class UzcardCreditUniversalCreditService extends AbstractUzcardCreditService {

    private final SvGateUniversalServiceV2 universalServiceV2;
    private final ApplicationProperties properties;
    private final PcHelper pcHelper;

    @Override
    protected HasData<TranDTO> credit(GatewayResultHandler gatewayResultHandler, Transaction transaction, P2pParamResult p2pParam, UzcardParam recipientParam, String senderCard, String recipientCard, String extId, SenderAdditionalParam additionalParam) {

        String senderName = "";

        if (additionalParam != null && !StringUtils.isEmpty(additionalParam.getFirstName()) && !StringUtils.isEmpty(additionalParam.getLastName()) && !StringUtils.isEmpty(additionalParam.getMiddleName())) {
            senderName = additionalParam.getFirstName() + " " + additionalParam.getMiddleName() + " " + additionalParam.getLastName();
        }

        Sender sender = new Sender()
                .withFullName(senderName)
                .withLegalName(properties.getLegalNameMap().getOrDefault(StringUtils.left(senderCard, 6), properties.getDefaultP2pLegalName()))
                .withRefNum(transaction.getSenderTrId())
                .withSystem(transaction.getSenderGateway())
                .withId(CardUtil.mask(senderCard));

        if (additionalParam != null && !StringUtils.isEmpty(additionalParam.getFirstName()) && !StringUtils.isEmpty(additionalParam.getLastName()) && !StringUtils.isEmpty(additionalParam.getMiddleName())) {
            fillSender(sender, additionalParam);
        }

        P2pUniversalCreditRequest p2pUniversalCreditRequest = new P2pUniversalCreditRequest()
                .withAmount(transaction.getRecipientAmount())
                .withExt(extId)
                .withMerchantId(recipientParam.getMerchantId())
                .withTerminalId(recipientParam.getTerminalId())
                .withRecipient(recipientCard)
                .withSender(sender);

        log.debug("Sending request to p2p.universal.credit : {}", p2pUniversalCreditRequest);

        gatewayResultHandler.preSaveRequestParams(mapService.convert(p2pUniversalCreditRequest), Instant.now());

        return universalServiceV2.p2pUniversalCredit(p2pUniversalCreditRequest);
    }

    @Override
    protected Optional<String> resolveRecipientPanOrToken(String panOrToken) {
        if (pcHelper.isValidPan(panOrToken, P2pProcessorType.UZCARD) || pcHelper.isValidToken(panOrToken, P2pProcessorType.UZCARD)) {
            return Optional.of(panOrToken);
        }
        log.warn("Given recipientCard not PAN or Token : {}", panOrToken);
        return Optional.empty();
    }

    @Override
    protected Optional<String> resolveSenderToken(String senderAccount) {
        return Optional.of(senderAccount);
    }

    @Override
    protected UzcardCreditService.UzcardCreditType supportsUzcardCreditType() {
        return UzcardCreditService.UzcardCreditType.UNIVERSAL_CREDIT;
    }

    private void fillSender(Sender sender, SenderAdditionalParam additionalParam) {
        sender.setFirstName(additionalParam.getFirstName());
        sender.setLastName(additionalParam.getLastName());
        sender.setMiddleName(additionalParam.getMiddleName());

        Doc doc = new Doc()
                .withNationality(additionalParam.getCitizen())
//                .withBirthDate(additionalParam.getBirthDate() != null ?)
                .withType(additionalParam.getDocType())
                .withSeriesNumber(additionalParam.getDocNumber())
//                .withValidTo(additionalParam.getDocDate())
                .withMrz(additionalParam.getPinFl());
        if (additionalParam.getBirthDate() != null) {
            doc.withBirthDate(additionalParam.getBirthDate());
        }
        if (additionalParam.getDocDate() != null) {
            doc.withValidTo(additionalParam.getDocDate());
        }
        sender.setDoc(doc);
    }

}
