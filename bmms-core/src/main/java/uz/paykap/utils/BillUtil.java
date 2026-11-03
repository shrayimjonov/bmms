package uz.paykap.utils;

import lombok.experimental.UtilityClass;
import uz.devops.bmcore.Constants;
import uz.devops.bmcore.enumeration.RequestType;
import uz.paykap.domain.Bill;

import java.time.Instant;

@UtilityClass
public class BillUtil {

    public static Bill getInitialBill(String username, Integer currency, String account, String recipientShortName, Long transactionId, Long amount, RequestType type) {
        return new Bill()
                .recipientId(0L)
                .recipientShortName(recipientShortName)
                .account(account)
                .amount(amount)
                .amountRatio(Constants.DEFAULT_AMOUNT_RATIO)
                .currency(currency)
                .type(type)
                .login(username)
                .requestTime(Instant.now())
                .responseTime(Instant.now())
                .success(false)
                .transactionId(transactionId);
    }

}
