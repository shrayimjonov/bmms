package uz.paykap.service.action.transaction;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uz.devops.bmcore.enumeration.TransactionStatus;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@AllArgsConstructor
public class TransactionActionService {

    private final List<IActionTransaction> actions;
    private static Map<TransactionStatus, List<IActionTransaction>> actionMap = new HashMap<>();


    public List<IActionTransaction> getAll(TransactionStatus transactionStatus) {
        log.debug("Request to get active transaction actions");
        if (actionMap.get(transactionStatus) == null) {
            var result = actions.stream()
                    .filter(tran -> tran.isActive() && tran.isActiveByStatus(transactionStatus))
                    .collect(Collectors.toList());
            actionMap.put(transactionStatus, result);
        }
        return actionMap.get(transactionStatus);
    }
}
