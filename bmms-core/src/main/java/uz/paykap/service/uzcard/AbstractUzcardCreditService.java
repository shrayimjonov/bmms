package uz.paykap.service.uzcard;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import uz.devops.bmcore.gateway.GatewayResultHandler;
import uz.javlon.commons.result.HasData;
import uz.javlon.svgate.dto.TranDTO;
import uz.paykap.domain.Transaction;
import uz.paykap.domain.enumeration.P2pProcessorType;
import uz.paykap.repository.BillRepository;
import uz.paykap.service.MapService;
import uz.paykap.service.PcHelper;
import uz.paykap.service.p2p.dto.P2pParamResult;
import uz.paykap.service.p2p.dto.SenderAdditionalParam;
import uz.paykap.service.p2p.processor.UzcardParam;
import uz.paykap.utils.TransactionPrefixGenerator;

import java.util.Optional;

@Slf4j
public abstract class AbstractUzcardCreditService {

    @Autowired
    private PcHelper pcHelper;
    @Autowired
    protected MapService mapService;
    @Autowired
    protected UzcardHelperService uzcardHelperService;
    @Autowired
    protected TransactionPrefixGenerator transactionPrefixUtil;

    protected abstract HasData<TranDTO> credit(
            GatewayResultHandler gatewayResultHandler,
            Transaction transaction,
            P2pParamResult p2pParam,
            UzcardParam recipientParam,
            String senderCard,
            String recipientCard,
            String extId,
            SenderAdditionalParam additionalParam
    );

    protected Optional<String> resolveSenderToken(String panOrToken) {
        return uzcardHelperService.getSenderCardTokenOrCreate(panOrToken);
    }

    protected abstract Optional<String> resolveRecipientPanOrToken(String panOrToken);

    protected abstract UzcardCreditService.UzcardCreditType supportsUzcardCreditType();

    protected Optional<String> returnCardTokenOrFetchFromSvGate(String panOrToken) {
        if (pcHelper.isValidToken(panOrToken, P2pProcessorType.UZCARD)) {
            return Optional.of(panOrToken);
        } else if (pcHelper.isValidPan(panOrToken, P2pProcessorType.UZCARD)) {
            return uzcardHelperService.getRecipientCardTokenOrCreate(panOrToken);
        }
        return Optional.empty();
    }
}
