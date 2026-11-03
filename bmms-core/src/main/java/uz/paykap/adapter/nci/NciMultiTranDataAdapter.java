package uz.paykap.adapter.nci;

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
import uz.paykap.gateway.nci.NciMultiTransactionGatewayService;
import uz.paykap.repository.RecipientRepository;

import java.util.HashSet;

@Slf4j
@Service(NciMultiTranDataAdapter.ADAPTER_NAME)
public class NciMultiTranDataAdapter extends CommonDataAdapter {

    public static final String ADAPTER_NAME = "nciMultiTranDataAdapter";
    public static final String NCI_DEBIT_GATEWAY_SHORT_NAME = "nci-gateway";
    public static final String NCI_DEBIT_RECIPIENT_SHORT_NAME = "nci-multi-recipient";
    private static final String ACCOUNT = "account";
    private static final int TIYIN = 100;
    public static final int UZS = 860;
    public static final String SENDER_ACCOUNT = "29896000600001183444";
    public final RecipientRepository recipientRepository;

    public NciMultiTranDataAdapter(RecipientRepository recipientRepository) {
        this.recipientRepository = recipientRepository;
    }

    @Override
    public Class<?> getClazz() {
        return this.getClass();
    }

    @Override
    public String getGatewayShortName() {
        return NCI_DEBIT_GATEWAY_SHORT_NAME;
    }

    @Override
    public String getGatewayServiceBeanName() {
        return NciMultiTransactionGatewayService.NAME;
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

        sharedDataAdapterDTO.addFields(NCI_DEBIT_RECIPIENT_SHORT_NAME, createAccountField());

        log.info("end synch...");
        return new ResultData<>(true, sharedDataAdapterDTO, null);
    }

    private SharedCategory createCategory() {
        SharedCategory category = new SharedCategory();
        category.setShortName("nci_caregory");
        category.setActive(true);
        category.setNames(getI18ns("Проводка АБС"));
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
        recipient.setServiceId(SENDER_ACCOUNT);
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

    private SharedField createAccountField() {

        SharedField accountField = new SharedField()
                .name(ACCOUNT)
                .extId(0L)
                .type(FieldType.ACCOUNT)
                .controlType(ControlType.TEXT)
                .validation("d{20}")
                .placeholder("**** **** **** ****")
                .mask("**** **** **** ****")
                .maskSymbol("*")
                .lengthMin(20)
                .lengthMax(20)
                .optional(true)
                .output(true)
                .requestType(RequestType.CHECK)
                .visibility(Visibility.ALL)
                .ord(1)
                .recipientShortName(NciMultiTranDataAdapter.NCI_DEBIT_RECIPIENT_SHORT_NAME)
                .titles(getI18ns("Nci account", "Номер банковского счета", "Bank hisob raqami"))
                //
                ;
        log.debug("add accountField: {}", accountField);
        return accountField;
    }
}
