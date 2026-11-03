package uz.paykap.service.cancel.resolver;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uz.javlon.commons.exception.CodedException;
import uz.javlon.commons.exception.ProblemEnum;
import uz.paykap.domain.enumeration.P2pProcessorType;
import uz.paykap.service.cancel.CancelService;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class CancelServiceUtil {

    private final List<CancelService> cancelServiceList;
    private final Map<P2pProcessorType, CancelService> cancelServiceMap = new HashMap<>();

    public CancelServiceUtil(List<CancelService> cancelServiceList) {
        this.cancelServiceList = cancelServiceList;
    }

    @PostConstruct
    public void initCancelServices() {
        log.info("cancel service list : {}", cancelServiceList);
        cancelServiceList.forEach(cancelService -> cancelServiceMap.put(cancelService.supportedType(), cancelService));
        log.info("all cancel service map : {}", cancelServiceMap);
    }

    public CancelService getCancelService(String beanName) {
        log.debug("all cancel service map : {} all cancel service list : {}", cancelServiceMap, cancelServiceList);
        if (cancelServiceMap.containsKey(P2pProcessorType.valueOf(beanName))) {
            log.debug("Cancel service found on bean name : {}", beanName);
            return cancelServiceMap.get(P2pProcessorType.valueOf(beanName));
        }
        log.warn("cancel service not found on bean name: {}", beanName);
        throw new CodedException(ProblemEnum.OBJECT_NOT_FOUND, "cancel service not found on bean name : " + beanName);
    }
}
