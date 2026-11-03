package uz.paykap.service.action.transaction;

import uz.devops.bmcore.enumeration.TransactionStatus;
import uz.paykap.service.action.IAction;

import java.util.List;

public interface IActionTransaction extends IAction {

    String[] getPathVariables();

    default Boolean isActiveByStatus(TransactionStatus status) {
        return getActiveStatuses().contains(status);
    }

    List<TransactionStatus> getActiveStatuses();
}
