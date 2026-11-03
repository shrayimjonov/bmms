package uz.paykap.adapter.nci.forex;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
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
import uz.paykap.gateway.nci.forex.NciForexTransactionGatewayServiceV3;
import uz.paykap.repository.RecipientRepository;

import java.util.HashSet;

@Slf4j
@Service(NciForexDataAdapterV3.ADAPTER_NAME)
public class NciForexDataAdapterV3 extends CommonDataAdapter {

    public static final String ADAPTER_NAME = "nciForexDataAdapterV3";
    public static final String NCI_DEBIT_GATEWAY_SHORT_NAME = "nci-forex-gateway";
    public static final String NCI_DEBIT_RECIPIENT_SHORT_NAME = "nci-forex-recipient";
    private static final String PAN_FIELD_NAME = "account";
    private static final int TIYIN = 100;
    public static final int UZS = 860;
    public static final String FOREX_SENDER_ACCOUNT = "19997643600001183001";
    public static final String SETTLEMENT_AMOUNT_VALUE = "SNT_V";
    public static final String RECEIVING_AMOUNT_VALUE = "RCV_V";
    public static final String BUY_RATE_VALUE = "B_RATE_V";
    public final RecipientRepository recipientRepository;
    private final ApplicationProperties applicationProperties;

    public NciForexDataAdapterV3(RecipientRepository recipientRepository, ApplicationProperties applicationProperties) {
        this.recipientRepository = recipientRepository;
        this.applicationProperties = applicationProperties;
    }

    @Override
    public Class getClazz() {
        return this.getClass();
    }

    @Override
    public String getGatewayShortName() {
        return NCI_DEBIT_GATEWAY_SHORT_NAME;
    }

    @Override
    public String getGatewayServiceBeanName() {
        return NciForexTransactionGatewayServiceV3.NAME;
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

        sharedDataAdapterDTO.addFields(NCI_DEBIT_RECIPIENT_SHORT_NAME, createPanField(), saveSettlementAmountField(), saveRecipientAmountField(), saveRateField(), saveAmountField());

        log.info("end synch...");
        return new ResultData<>(true, sharedDataAdapterDTO, null);
    }

    private SharedCategory createCategory() {
        SharedCategory category = new SharedCategory();
        category.setShortName("nci_forex_caregory");
        category.setActive(true);
        category.setNames(getI18ns("Конверсия АБС"));
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

        recipient.setShortName(NCI_DEBIT_RECIPIENT_SHORT_NAME);
        recipient.setCcy(UZS);
        recipient.setAmountRatio(TIYIN);
        recipient.setLimitRatio(TIYIN);
        recipient.setTitles(getI18ns(NCI_DEBIT_RECIPIENT_SHORT_NAME));
        recipient.setFullNames(getI18ns(NCI_DEBIT_RECIPIENT_SHORT_NAME));
        recipient.setMinAmount(0L);
        recipient.setMaxAmount(1000000000L);
        recipient.setServiceId(FOREX_SENDER_ACCOUNT);
        recipient.setAmountRatio(TIYIN);
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
            .validation("d{12}")
            .placeholder("998 ** *** ****")
            .mask("998 ** *** ****")
            .maskSymbol("*")
            .lengthMin(12)
            .lengthMax(12)
            .optional(false)
            .output(true)
            .requestType(RequestType.CHECK)
            .visibility(Visibility.ALL)
            .ord(1)
            .recipientShortName(NciForexDataAdapterV3.NCI_DEBIT_RECIPIENT_SHORT_NAME)
            .titles(getI18ns("Phone number", "Номер телефона", "Telefon raqami"))
            //
            ;
        log.debug("add accountField: {}", panField);
        return panField;
    }

    private SharedField saveSettlementAmountField() {

        SharedField panField = new SharedField()
            .name(SETTLEMENT_AMOUNT_VALUE)
            .extId(0L)
            .type(FieldType.OTHER)
            .controlType(ControlType.TEXT)
            .validation("^\\d{14}$")
            .placeholder("settlement amount")
            .mask("10000")
            .maskSymbol("*")
            .lengthMin(4)
            .lengthMax(40)
            .optional(false)
            .output(true)
            .visibility(Visibility.ALL)
            .ord(2)
            .recipientShortName(NciForexDataAdapterV3.NCI_DEBIT_RECIPIENT_SHORT_NAME)
            .titles(getI18ns("Sender amount", "Сумма отправителя", "Yuboruvchi summasi"))
            //
            ;
        log.debug("add accountField: {}", panField);
        return panField;
    }

    private SharedField saveRecipientAmountField() {

        SharedField panField = new SharedField()
            .name(RECEIVING_AMOUNT_VALUE)
            .extId(0L)
            .type(FieldType.OTHER)
            .controlType(ControlType.TEXT)
            .validation("^\\d{14}$")
            .placeholder("receiver amount")
            .mask("10000")
            .maskSymbol("*")
            .lengthMin(4)
            .lengthMax(40)
            .optional(false)
            .output(true)
            .visibility(Visibility.ALL)
            .ord(2)
            .recipientShortName(NciForexDataAdapterV3.NCI_DEBIT_RECIPIENT_SHORT_NAME)
            .titles(getI18ns("Receiver amount", "Сумма получателя", "Qabul qiluvchi summasi"))
            //
            ;
        log.debug("add accountField: {}", panField);
        return panField;
    }

    private SharedField saveRateField() {

        SharedField panField = new SharedField()
            .name(BUY_RATE_VALUE)
            .extId(0L)
            .type(FieldType.OTHER)
            .controlType(ControlType.TEXT)
            .placeholder("rate")
            .mask("0.1")
            .maskSymbol("*")
            .lengthMin(1)
            .lengthMax(10)
            .optional(false)
            .output(true)
            .visibility(Visibility.ALL)
            .ord(2)
            .recipientShortName(NciForexDataAdapterV3.NCI_DEBIT_RECIPIENT_SHORT_NAME)
            .titles(getI18ns("Rate", "Курс/Рейт", "Kurs/Reyt"))
            //
            ;
        log.debug("add accountField: {}", panField);
        return panField;
    }

    private SharedField saveAmountField() {

        SharedField panField = new SharedField()
            .name("amount")
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
            .recipientShortName(NciForexDataAdapterV3.NCI_DEBIT_RECIPIENT_SHORT_NAME)
            .titles(getI18ns("Amount", "Сумма", "Summa"));

        log.debug("add accountField: {}", panField);
        return panField;
    }
}
