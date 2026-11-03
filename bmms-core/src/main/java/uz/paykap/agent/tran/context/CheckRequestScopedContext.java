package uz.paykap.agent.tran.context;

import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;
import uz.devops.bmcore.domain.CommonBill;
import uz.devops.bmcore.dto.GatewayQueryResult;
import uz.devops.bmcore.dto.GatewayResult;
import uz.devops.bmcore.dto.request.CommonCheckRequest;
import uz.devops.bmcore.dto.response.SimpleBillResponse;
import uz.paykap.domain.Transaction;

@Getter
@Setter
@Component
// http://www.javabyexamples.com/request-scoped-data-with-spring-mvc
@RequestScope
public class CheckRequestScopedContext {

    private String username;
    private Transaction transaction;
    private CommonBill checkBill;
    private CommonCheckRequest checkRequest;
    private SimpleBillResponse simpleBillResponse;
    private GatewayResult gatewayResult;
    private GatewayQueryResult recipientParams;

}
