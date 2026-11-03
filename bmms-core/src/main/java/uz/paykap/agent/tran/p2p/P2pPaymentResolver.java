package uz.paykap.agent.tran.p2p;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.map.MultiKeyMap;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import uz.paykap.domain.enumeration.P2pProcessorType;

import javax.annotation.PostConstruct;
import java.util.Map;
import java.util.Objects;

@Slf4j
@Component
@AllArgsConstructor
public class P2pPaymentResolver {

    private final DefaultP2pTranService defaultP2pTranService;
    private final ApplicationContext applicationContext;

    //    public static final String NAME = "p2pPaymentResolver";
    //    private final ConcurrentHashMap<P2pProcessorType, P2pPaymentService> p2pPaymentMap = new ConcurrentHashMap<>(P2pProcessorType.values().length * P2pProcessorType.values().length);
    private final MultiKeyMap<P2pProcessorType, P2pPaymentService> map = new MultiKeyMap<>();

    @PostConstruct
    public void init() {

        Map<String, P2pPaymentService> beansOfType = applicationContext.getBeansOfType(P2pPaymentService.class);

        beansOfType.forEach((key, value) -> {
            if (!Objects.equals(key, DefaultP2pTranService.NAME)) {

                for (P2pProcessorType senderType : value.getSupportedSender()) {
                    for (P2pProcessorType recipientType : value.getSupportedRecipient()) {
                        map.put(senderType, recipientType, value);
                    }
                }

            }
        });

        log.info("P2p payment services : {}", map);

    }

    public P2pPaymentService resolve(P2pProcessorType senderPc, P2pProcessorType recipientPc) {

        P2pPaymentService p2pPaymentService = map.get(senderPc, recipientPc);

        if (p2pPaymentService == null) {
            return defaultP2pTranService;
        }

        return p2pPaymentService;
    }

}
