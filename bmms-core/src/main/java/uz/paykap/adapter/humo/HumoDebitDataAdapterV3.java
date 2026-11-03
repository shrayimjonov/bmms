package uz.paykap.adapter.humo;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uz.devops.bmcore.Constants;
import uz.devops.bmcore.adapter.CommonDataAdapter;
import uz.devops.bmcore.adapter.dto.SharedCategory;
import uz.devops.bmcore.adapter.dto.SharedDataAdapterDTO;
import uz.devops.bmcore.adapter.dto.SharedField;
import uz.devops.bmcore.adapter.dto.SharedRecipient;
import uz.devops.bmcore.enumeration.ControlType;
import uz.devops.bmcore.enumeration.FieldType;
import uz.devops.bmcore.enumeration.RequestType;
import uz.devops.bmcore.enumeration.Visibility;
import uz.javlon.commons.base.ResultData;
import uz.paykap.config.ApplicationProperties;
import uz.paykap.gateway.humo.HumoDebitGatewayServiceV3;
import uz.paykap.repository.RecipientRepository;

import java.util.HashSet;

@Slf4j
@Service(HumoDebitDataAdapterV3.ADAPTER_NAME)
@AllArgsConstructor
public class HumoDebitDataAdapterV3 extends CommonDataAdapter {

    public static final String ADAPTER_NAME = "humoDebitDataAdapter";
    public static final String HUMO_DEBIT_GATEWAY_SHORT_NAME = "humo-debit-gateway";
    public static final String HUMO_DEBIT_RECIPIENT_SHORT_NAME = "humo-debit-recipient";
    private static final String PAN_FIELD_NAME = "account";
    public static final String EXPIRY_FIELD_NAME = "expiry";
    public static final String AMOUNT_FIELD_NAME = "amount";

    public final RecipientRepository recipientRepository;
    private final ApplicationProperties applicationProperties;

    @Override
    public Class<?> getClazz() {
        return this.getClass();
    }

    @Override
    public String getGatewayShortName() {
        return HUMO_DEBIT_GATEWAY_SHORT_NAME;
    }

    @Override
    public String getGatewayServiceBeanName() {
        return HumoDebitGatewayServiceV3.NAME;
    }

    @Override
    public ResultData<SharedDataAdapterDTO> returnData() {
        SharedDataAdapterDTO sharedDataAdapterDTO = new SharedDataAdapterDTO();
        HashSet<SharedCategory> categories = new HashSet<>();
        SharedCategory category = createCategory();
        categories.add(category);
        sharedDataAdapterDTO.setCategories(categories);

        HashSet<SharedRecipient> category1Recipients = createRecipients(category);
        sharedDataAdapterDTO.setRecipients(category1Recipients);

        sharedDataAdapterDTO.addFields(HUMO_DEBIT_RECIPIENT_SHORT_NAME, createPanField(), saveExpiryField(), saveAmountField());

        log.info("end synch...");
        return new ResultData<>(true, sharedDataAdapterDTO, null);
    }

    private SharedCategory createCategory() {
        SharedCategory category = new SharedCategory();
        category.setShortName("humo_debit_caregory");
        category.setActive(true);
        category.setNames(getI18ns("Humo снятия денег"));
        category.setOrd(0);

        log.debug("add category: {}", category);
        return category;
    }

    private HashSet<SharedRecipient> createRecipients(SharedCategory category) {
        HashSet<SharedRecipient> recipients = new HashSet<>();
        SharedRecipient recipient = createRecipient(category);
        recipients.add(recipient);

        return recipients;
    }

    private SharedRecipient createRecipient(SharedCategory category) {
        SharedRecipient recipient = new SharedRecipient();

        recipient.setShortName(HumoDebitDataAdapterV3.HUMO_DEBIT_RECIPIENT_SHORT_NAME);
        recipient.setCcy(Constants.UZS_CURRENCY);
        recipient.setAmountRatio(Constants.AMOUNT_RATIO_TIYIN);
        recipient.setLimitRatio(Constants.AMOUNT_RATIO_TIYIN);
        recipient.setTitles(getI18ns(HumoDebitDataAdapterV3.HUMO_DEBIT_RECIPIENT_SHORT_NAME));
        recipient.setFullNames(getI18ns(HumoDebitDataAdapterV3.HUMO_DEBIT_RECIPIENT_SHORT_NAME));
        recipient.setMinAmount(0L);
        recipient.setMaxAmount(1000000000L);
        recipient.setServiceId("");
        recipient.setEposMerchant(applicationProperties.getHumoDebitGateway().getEposMerchantId());
        recipient.setEposTerminal(applicationProperties.getHumoDebitGateway().getEposTerminalId());
        recipient.setCheck(true);
        recipient.setPay(true);
        recipient.setCancel(true);
        recipient.setStatus(true);
        recipient.setReconcile(true);
        recipient.setCategoryShortName(category.getShortName());

        log.debug("add recipient: {}", recipient);
        return recipient;
    }

    private SharedField createPanField() {

        SharedField panField = new SharedField()
                .name(PAN_FIELD_NAME)
                .extId(0L)
                .type(FieldType.ACCOUNT)
                .controlType(ControlType.TEXT)
                .validation("9860\\d{12}")
                .placeholder("9860 **** **** ****")
                .mask("9999 **** **** 9999")
                .maskSymbol("*")
                .lengthMin(16)
                .lengthMax(16)
                .optional(false)
                .output(true)
                .requestType(RequestType.CHECK)
                .visibility(uz.devops.bmcore.enumeration.Visibility.ALL)
                .ord(1)
                .recipientShortName(HumoDebitDataAdapterV3.HUMO_DEBIT_RECIPIENT_SHORT_NAME)
                .titles(getI18ns("Card Number", "Номер карты", "Karta raqami"))
                //
                ;
        log.debug("add accountField: {}", panField);
        return panField;
    }

    private SharedField saveExpiryField() {

        SharedField panField = new SharedField()
                .name(EXPIRY_FIELD_NAME)
                .extId(0L)
                .type(FieldType.OTHER)
                .controlType(ControlType.TEXT)
                .validation("^\\d{4}$")
                .placeholder("yyMM")
                .mask("dddd")
                .maskSymbol("*")
                .lengthMin(4)
                .lengthMax(4)
                .optional(false)
                .output(true)
                .visibility(Visibility.ALL)
                .ord(2)
                .recipientShortName(HumoDebitDataAdapterV3.HUMO_DEBIT_RECIPIENT_SHORT_NAME)
                .titles(getI18ns("Card Expiry", "Срок карты", "Karta muddati"))
                //
                ;
        log.debug("add accountField: {}", panField);
        return panField;
    }

    private SharedField saveAmountField() {

        SharedField panField = new SharedField()
                .name(AMOUNT_FIELD_NAME)
                .extId(0L)
                .type(FieldType.AMOUNT)
                .controlType(ControlType.TEXT)
                .validation("\\d+")
                .placeholder("")
                .lengthMin(1)
                .lengthMax(32)
                .optional(false)
                .output(true)
                .visibility(Visibility.ALL)
                .ord(3)
                .recipientShortName(HumoDebitDataAdapterV3.HUMO_DEBIT_RECIPIENT_SHORT_NAME)
                .titles(getI18ns("Amount", "Сумма", "Summa"));

        log.debug("add accountField: {}", panField);
        return panField;
    }
}
