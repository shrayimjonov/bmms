package uz.paykap.adapter.humo;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uz.devops.bmcore.adapter.CommonDataAdapter;
import uz.devops.bmcore.adapter.dto.*;
import uz.devops.bmcore.enumeration.ControlType;
import uz.devops.bmcore.enumeration.FieldType;
import uz.javlon.commons.base.ResultData;
import uz.paykap.gateway.humo.HumoCreditGatewayServiceV3;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Slf4j
@Service(HumoCreditDataAdapterV3.HUMO_DATA_ADAPTER)
public class HumoCreditDataAdapterV3 extends CommonDataAdapter {

    public static final String HUMO_DATA_ADAPTER = "humoCreditDataAdapterV3";
    public static final String HUMO_GATEWAY_SHORT_NAME = "humo-credit-gw-v3";
    public static final String PAN_FIELD_NAME = "account";
    public static final String AMOUNT_FIELD_NAME = "amount";

    @Override
    public Class<?> getClazz() {
        return this.getClass();
    }

    @Override
    public String getGatewayShortName() {
        return HUMO_GATEWAY_SHORT_NAME;
    }

    @Override
    public String getGatewayServiceBeanName() {
        return HumoCreditGatewayServiceV3.NAME;
    }

    @Override
    public ResultData<SharedDataAdapterDTO> returnData() {

        SharedDataAdapterDTO sharedDataAdapterDTO = new SharedDataAdapterDTO();

        // Add recipient
        HashSet<SharedRecipient> recipients = new HashSet<>();
        SharedRecipient recipient = createRecipient(HumoCreditGatewayServiceV3.NAME, Optional.empty(), "1", this.getI18ns("Refill Humo", "Пополнение Humo", "Humo to'ldirish"));
        recipients.add(recipient);
        sharedDataAdapterDTO.setRecipients(recipients);

        // Add fields
        HashMap<String, HashSet<SharedField>> recipientNameAndFields = new HashMap<>();
        HashSet<SharedField> fields = new HashSet<>();
        fields.add(getAccountField(recipient.getShortName()));
        fields.add(getAmountField(recipient.getShortName()));
        recipientNameAndFields.put(HumoCreditGatewayServiceV3.NAME, fields);

        sharedDataAdapterDTO.setRecipientNameAndFields(recipientNameAndFields);

        return new ResultData<>(sharedDataAdapterDTO);
    }

    private SharedField getAccountField(String recipientShortName) {
        SharedField sharedField = (new SharedField()).name(PAN_FIELD_NAME).extId(0L).type(FieldType.ACCOUNT).controlType(ControlType.TEXT).placeholder("9860 1234 1234 1234").mask("9860\\d{12}").lengthMin(16).lengthMax(16).optional(false).output(true).visibility(uz.devops.bmcore.enumeration.Visibility.ALL).ord(1).recipientShortName(recipientShortName).titles(this.getI18ns("PAN", "Номер карты", "Karta raqami"));
        log.debug("add accountField: {}", sharedField);
        return sharedField;
    }

    private SharedField getAmountField(String shortName) {
        SharedField sharedField = (new SharedField()).name(AMOUNT_FIELD_NAME).extId(0L).type(FieldType.AMOUNT).controlType(ControlType.TEXT).validation("\\d+").lengthMin(1).lengthMax(32).optional(false).output(true).visibility(uz.devops.bmcore.enumeration.Visibility.ALL).ord(2).recipientShortName(shortName).titles(this.getI18ns("Amount", "Сумма", "Summa"));
        log.debug("add amountField: {}", sharedField);
        return sharedField;
    }

    private SharedRecipient createRecipient(String shortName, Optional<SharedCategory> optionalCategory, String serviceId, Set<SharedI18n> i18ns) {
        SharedRecipient recipient = new SharedRecipient();
        recipient.setShortName(shortName);
        recipient.setCcy(860);
        recipient.setAmountRatio(100);
        recipient.setLimitRatio(100);
        recipient.setTitles(i18ns);
        recipient.setFullNames(i18ns);
        recipient.setMinAmount(100L);
        recipient.setMaxAmount(10000000000L);
        recipient.setServiceId(serviceId);
        optionalCategory.ifPresent((optionalCategory1) -> {
            recipient.setCategoryShortName(optionalCategory1.getShortName());
        });
        log.debug("add recipient: {}", recipient);
        return recipient;
    }
}
