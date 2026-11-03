package uz.paykap.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uz.paykap.agent.tran.P2pTranServiceV3;
import uz.paykap.service.dto.p2p.P2pCancelRequest;
import uz.paykap.service.dto.p2p.P2pPayRequest;
import uz.paykap.service.dto.p2p.P2pRequestScopedContext;
import uz.paykap.service.dto.p2p.calc.P2pCalcRequest;
import uz.paykap.service.dto.p2p.calc.P2pCalcResponse;
import uz.paykap.service.p2p.dto.response.P2pResponse;

@Slf4j
@Service
@AllArgsConstructor
public class P2pCheckRequestDecoratorService {

    private final P2pRequestScopedContext p2pRequestScopedContext;
    private final P2pTranServiceV3 p2pTranServiceV3;
    private final ObjectMapper objectMapper;

    public P2pCalcResponse calcCommission(P2pCalcRequest p2pCheckRequest) {

        return p2pTranServiceV3.calcCommission(p2pCheckRequest);
    }

    public P2pResponse p2pInfo(P2pPayRequest p2PPayRequest) {

        p2pRequestScopedContext.setSenderAccount(p2PPayRequest.getFrom());
        p2pRequestScopedContext.setRecipientAccount(p2PPayRequest.getTo());
        p2pRequestScopedContext.setRequest(p2PPayRequest);

        return p2pTranServiceV3.getInfo();
    }

    public P2pResponse p2pPay(P2pPayRequest p2PPayRequest) {

        p2pRequestScopedContext.setSenderAccount(p2PPayRequest.getFrom());
        p2pRequestScopedContext.setRecipientAccount(p2PPayRequest.getTo());
        p2pRequestScopedContext.setRequest(p2PPayRequest);

        // TODO - move to Aspect for all methods in the class
        P2pResponse p2pResponse = p2pTranServiceV3.p2pPay();

        try {
            log.info("P2P pay response : \n{}", objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(p2pResponse));
        } catch (JsonProcessingException e) {
            log.info("P2P pay response : {}", p2pResponse);
        }

        return p2pResponse;
    }

    public P2pResponse p2pCancel(P2pCancelRequest p2pCancelRequest) {

        p2pRequestScopedContext.setAgentTranId(p2pCancelRequest.getAgentTranId());

        return p2pTranServiceV3.p2pCancel();
    }
}
