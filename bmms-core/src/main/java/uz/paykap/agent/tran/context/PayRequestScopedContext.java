package uz.paykap.agent.tran.context;

import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;
import uz.devops.bmcore.dto.GatewayResult;
import uz.devops.bmcore.dto.request.PayRequest;
import uz.devops.bmcore.dto.response.SimpleBillResponse;
import uz.paykap.domain.Bill;
import uz.paykap.domain.Transaction;

@Getter
@Setter
@Component
// http://www.javabyexamples.com/request-scoped-data-with-spring-mvc
@RequestScope
public class PayRequestScopedContext {

    private String username;
    private Transaction transaction;
    private Bill checkBill;
    private Bill payBill;
    private PayRequest payRequest;
    private GatewayResult gatewayResult;
    private SimpleBillResponse simpleBillResponse;

    //P2p cancel fields
    private String agentTranId;

}
