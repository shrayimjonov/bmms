package uz.paykap.service.dto.p2p;

import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;
import uz.paykap.domain.Transaction;
import uz.paykap.service.p2p.dto.P2pDTO;
import uz.paykap.service.p2p.dto.response.P2pParticipantDto;
import uz.paykap.service.p2p.dto.response.P2pResponse;
import uz.paykap.service.p2p.processor.ProcessorParam;
import uz.paykap.service.p2p.support.AbstractP2pRecipient;
import uz.paykap.service.p2p.support.AbstractP2pSender;

import java.util.Map;

@Getter
@Setter
@Component
// http://www.javabyexamples.com/request-scoped-data-with-spring-mvc
@RequestScope
public class P2pRequestScopedContext {

    private String username;
    private P2pAccount senderAccount;
    private P2pAccount recipientAccount;
    private P2pPayRequest request;
    private P2pInfoRequest infoRequest;

//    private P2pPayRequest p2pPayRequest;
    private Transaction transaction;

    private AbstractP2pSender<P2pDTO> senderService;
    private AbstractP2pRecipient<P2pDTO> recipientService;

    private P2pDTO senderDto;
    private P2pDTO recipientDto;

    private P2pResponse response;
    private P2pParticipantDto senderParticipantDto;
    private P2pParticipantDto recipientParticipantDto;

    //P2p cancel fields
    private String agentTranId;

    private Map<String, Object> recipientInfo;

}
