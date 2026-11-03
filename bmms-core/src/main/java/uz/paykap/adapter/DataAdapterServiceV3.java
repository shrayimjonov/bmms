package uz.paykap.adapter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.devops.bmcore.adapter.CommonDataAdapter;
import uz.devops.bmcore.adapter.dto.*;
import uz.devops.bmcore.enumeration.Lang;
import uz.devops.bmcore.enumeration.Visibility;
import uz.javlon.commons.base.Result;
import uz.javlon.commons.base.ResultData;
import uz.javlon.commons.exception.CodedException;
import uz.javlon.commons.exception.ProblemEnum;
import uz.paykap.config.Constants;
import uz.paykap.domain.*;
import uz.paykap.service.dto.AgentDTO;
import uz.paykap.service.dto.PropAndValue;
import uz.paykap.service.dto.SettingDTO;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/*
 * Used : Template Method Pattern
 * Source : https://springframework.guru/gang-of-four-design-patterns/template-method-pattern/
 * */
@Slf4j
@Service
public class DataAdapterServiceV3 {

    private final DataAdapterDelegate dataAdapterDelegate;
    private final ApplicationContext applicationContext;

    protected DataAdapterServiceV3(DataAdapterDelegate dataAdapterDelegate, ApplicationContext applicationContext) {
        this.dataAdapterDelegate = dataAdapterDelegate;
        this.applicationContext = applicationContext;
    }

    public ArrayList<DataAdapterResponse> getAllAdapters() {
        log.debug("Request to get all adapters");
        String[] adapterNames = applicationContext.getBeanNamesForType(CommonDataAdapter.class);
        ArrayList<DataAdapterResponse> list = new ArrayList<>(adapterNames.length);

        for (String adapterName : adapterNames) {
            CommonDataAdapter bean = applicationContext.getBean(adapterName, CommonDataAdapter.class);
            list.add(new DataAdapterResponse(adapterName, bean.supportsFileUpload()));
        }

        return list;
    }

    public Result sync(String adapterBeanName) {
        return sync(adapterBeanName, null);
    }

    public Result sync(String adapterBeanName, byte[] fileBytes) {

        CommonDataAdapter dataAdapter;

        Map<String, String> historyDetailsMap = new HashMap<>(0);

        DataAdapterHistory history = new DataAdapterHistory();
        history.setName(adapterBeanName);

        try {
            dataAdapter = applicationContext.getBean(adapterBeanName, CommonDataAdapter.class);

            log.info("Started syncing : {}", history.getName());

            historyDetailsMap.put("startDate", LocalDateTime.now().toString());
        } catch (Exception e) {
            log.error(e.getLocalizedMessage());
            historyDetailsMap.put("error", e.getLocalizedMessage());
            saveHistory(history, historyDetailsMap);
            return new Result(false, new CodedException("Error", e.getLocalizedMessage()));
        }

        ResultData<SharedDataAdapterDTO> result;

        if (fileBytes != null && dataAdapter.supportsFileUpload()) {
            result = dataAdapter.returnData(fileBytes);
        } else {
            result = dataAdapter.returnData();
        }

        checkAndSaveIfFailure(history, result, historyDetailsMap);

        log.info("Data fetched successfully");

        Gateway gateway = getGatewayOrCreate(dataAdapter.getGatewayShortName(), dataAdapter.getGatewayServiceBeanName());

        log.info("Gateway used : {}", gateway);

        Result importResult = importData(result.getData(), gateway);

        checkAndSaveIfFailure(history, importResult, historyDetailsMap);

        log.info("Data imported successfully");

        saveHistory(history, historyDetailsMap);

        log.info("History saved successfully");

        return result;
    }

    @Transactional
    protected Result importData(SharedDataAdapterDTO dto, Gateway gateway) {

        if (dto == null) {
            return Result.FAILED;
        }

        try {

            Map<String, Category> categoryMap = new HashMap<>(dto.getCategories().size());

            for (SharedCategory sharedCategory : dto.getCategories()) {
                addCategory(categoryMap, sharedCategory, dto.getCategories());
            }

            log.debug("Categories synced : {}", dto.getCategories().stream().map(c -> "'" + c.getShortName() + "'").collect(Collectors.joining(", ")));

            Set<Recipient> recipients = new HashSet<>();
            for (SharedRecipient sharedRecipient : dto.getRecipients()) {
                Recipient recipient = dataAdapterDelegate.getRecipientMapper().fromShared(sharedRecipient);
                recipient.setGateway(gateway);

                if (categoryMap.containsKey(sharedRecipient.getCategoryShortName())) {
                    recipient.setCategory(categoryMap.get(sharedRecipient.getCategoryShortName()));
                }

                recipient = getRecipientOrCreate(recipient);

                recipients.add(recipient);
            }

            recipients.forEach(recipient -> {
                Set<SharedField> sharedFields = dto.getRecipientNameAndFields().get(recipient.getShortName());
                Set<Field> fields = dataAdapterDelegate.getFieldMapper().fromShared(sharedFields);
                for (Field field : fields) {
                    field.setRecipient(recipient);
                    field = getFieldOrCreate(field);

                    for (Prefix prefix : field.getPrefixes()) {
                        prefix.setField(field);
                        getPrefixOrCreate(prefix);
                    }

                    for (FieldValue value : field.getValues()) {
                        value.setField(field);
                        getFieldValueOrCreate(value);
                    }
                }

            });

            recipients.forEach(recipient -> {
                Set<SharedResponseField> sharedFields = dto.getRecipientNameAndResponseFields().get(recipient.getShortName());
                Set<ResponseField> fields = dataAdapterDelegate.getResponseFieldMapper().fromShared(sharedFields);
                if (fields != null) {
                    for (ResponseField responseField : fields) {
                        responseField.setRecipient(recipient);
                        responseField.setVisibility(Visibility.ALL);
                        responseField = getResponseFieldOrCreate(responseField);
                    }
                }

            });

            RecipientGroup recipientGroup = new RecipientGroup();
            recipientGroup.active(true).name("Group " + gateway.getName());
            recipientGroup.setRecipients(recipients);
            recipientGroup = getRecipientGroupOrCreate(recipientGroup);

            log.debug("Saved recipientGroup : {}", recipientGroup.getName());

        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.getLocalizedMessage());
            return new Result(false, new CodedException(ProblemEnum.INTERNAL_ERROR, e.getLocalizedMessage()));
        }

        return Result.SUCCESS;
    }

    private Category addCategory(Map<String, Category> categoryMap, SharedCategory sharedCategory, HashSet<SharedCategory> categories) {

        // Check if category not added yet
        if (categoryMap.containsKey(sharedCategory.getShortName())) {
            log.debug("Category '{}' exists, return", sharedCategory.getShortName());
            return categoryMap.get(sharedCategory.getShortName());
        }

        Category category = dataAdapterDelegate.getCategoryMapper().fromShared(sharedCategory);

        // If parent category exists
        if (sharedCategory.getParentShortName() != null) {

            if (categoryMap.containsKey(sharedCategory.getParentShortName())) {

                // If parent already in map use it
                category.setParent(categoryMap.get(sharedCategory.getParentShortName()));
            } else {
                // If not found in map = parent not yet added = parent may be in the rest of list

                // Search from DB
                Optional<Category> categoryOptional = dataAdapterDelegate.getCategoryRepository().findTop1ByShortName(sharedCategory.getParentShortName());

                if (categoryOptional.isPresent()) {

                    // Uhh, found from DB, set it
                    category.setParent(categoryOptional.get());
                } else {

                    // Last try, find from given shared categories, and add to the map
                    for (SharedCategory cat : categories) {
                        if (Objects.equals(cat.getShortName(), sharedCategory.getParentShortName())) {
                            Category parent = addCategory(categoryMap, cat, categories);
                            category.setParent(parent);
                            break;
                        }
                    }
                }
            }

        }

        category = getCategoryOrCreate(category);
        categoryMap.put(category.getShortName(), category);
        return category;
    }

    private void checkAndSaveIfFailure(DataAdapterHistory history, Result result, Map<String, String> historyDetailsMap) {

        if (result == null) {
            historyDetailsMap.put("error", "Result is null");
            saveHistory(history, historyDetailsMap);
            throw new CodedException(ProblemEnum.INTERNAL_ERROR);
        }

        if (!result.isSuccess()) {
            historyDetailsMap.put("error", result.getException().getDetail());
            saveHistory(history, historyDetailsMap);
            throw result.getException();
        }
    }

    private void saveHistory(DataAdapterHistory history, Map<String, String> historyDetailsMap) {
        historyDetailsMap.put("endDate", LocalDateTime.now().toString());
        try {
            dataAdapterDelegate.getHistoryService().save(history.details(new ObjectMapper().writeValueAsString(historyDetailsMap)));
        } catch (JsonProcessingException e) {
            dataAdapterDelegate.getHistoryService().save(history.details("{}"));
        }
    }

    protected Prefix getPrefixOrCreate(Prefix prefix) {

        Optional<Prefix> optionalPrefix = dataAdapterDelegate.getPrefixRepository().findTop1ByValueAndFieldId(prefix.getValue(), prefix.getField().getId());

        log.debug("{}'s prefix {} {}", prefix.getField().getName(), prefix.getValue(), (optionalPrefix.isPresent() ? "found, returning" : "not found, saving into DB"));

        return optionalPrefix.orElseGet(() -> dataAdapterDelegate.getPrefixRepository().saveAndFlush(prefix));
    }

    protected Field getFieldOrCreate(Field field) {

        Optional<Field> optionalField = dataAdapterDelegate.getFieldRepository().findTop1ByNameAndRecipientIdAndRequireFieldAndFilterBy(field.getName(), field.getRecipient().getId(), field.getRequireField(), field.getFilterBy());

        log.debug("Field {} {}", field.getName(), (optionalField.isPresent() ? "found, returning" : "not found, saving into DB"));

        if (optionalField.isPresent()) {
            merge(optionalField.get(), field);
            return dataAdapterDelegate.getFieldRepository().saveAndFlush(optionalField.get());
        }

        return dataAdapterDelegate.getFieldRepository().saveAndFlush(field);
    }

    protected ResponseField getResponseFieldOrCreate(ResponseField responseField) {

        Optional<ResponseField> optionalField = dataAdapterDelegate.getResponseFieldRepository().findTop1ByNameAndRecipientId(responseField.getName(), responseField.getRecipient().getId());

        log.debug("Response field {} {}", responseField.getName(), (optionalField.isPresent() ? "found, updating" : "not found, saving into DB"));

        if (optionalField.isPresent()) {
            merge(optionalField.get(), responseField);
            return dataAdapterDelegate.getResponseFieldRepository().saveAndFlush(optionalField.get());
        }

        return dataAdapterDelegate.getResponseFieldRepository().saveAndFlush(responseField);
    }

    protected FieldValue getFieldValueOrCreate(FieldValue fieldValue) {
        Optional<FieldValue> optionalFieldValue = dataAdapterDelegate.getFieldValueRepository().findTop1ByFieldIdAndValue(fieldValue.getField().getId(), fieldValue.getValue());

        log.debug("{}'s value {} {}", fieldValue.getField().getName(), fieldValue.getValue(), (optionalFieldValue.isPresent() ? "found, updating" : "not found, saving into DB"));

        if (optionalFieldValue.isPresent()) {
            merge(optionalFieldValue.get(), fieldValue);
            return dataAdapterDelegate.getFieldValueRepository().saveAndFlush(optionalFieldValue.get());
        }

        return dataAdapterDelegate.getFieldValueRepository().saveAndFlush(fieldValue);
    }

    protected Gateway getGatewayOrCreate(String gatewayShortName, String gatewayServiceBeanName) {

        Optional<Gateway> gatewayOptional = dataAdapterDelegate.getGatewayRepository().findTop1ByName(gatewayShortName);

        if (gatewayOptional.isPresent()) {
            log.debug("Gateway {} {} found, returning without changes", gatewayOptional.get().getId(), gatewayOptional.get().getName());
            return gatewayOptional.get();
        }

        Gateway gateway = new Gateway()
                .active(true)
                .name(gatewayShortName)
                .org("OOO '" + gatewayShortName.toUpperCase() + "'")
                .mfo("12345")
                .ban("55555678901234567890")
                .contract("")
                .springBeanName(gatewayServiceBeanName)
                //
                ;

        log.debug("Saving Gateway : {}", gateway.getName());

        return dataAdapterDelegate.getGatewayRepository().save(gateway);
    }

    protected RecipientGroup getRecipientGroupOrCreate(RecipientGroup recipientGroup) {

        Optional<RecipientGroup> optional = dataAdapterDelegate.getRecipientGroupRepository().findTop1ByName(recipientGroup.getName());

        if (optional.isPresent()) {
            log.debug("Found Recipient Group : {}", optional.get().getName());
            return optional.get();
        }

        if (recipientGroup.isActive() == null) {
            recipientGroup.active(true);
        }

        return dataAdapterDelegate.getRecipientGroupRepository().save(recipientGroup);
    }

    protected Recipient getRecipientOrCreate(Recipient recipient) {

        Optional<Recipient> optional = dataAdapterDelegate.getRecipientRepository().findOneWithEagerRelationships(recipient.getShortName());

        if (optional.isPresent()) {
            log.debug("Found Recipient : {}", optional.get().getShortName());

            boolean updated = false;

            // Update gateway
            if (optional.get().getGateway() == null && recipient.getGateway() != null) {
                optional.get().setGateway(recipient.getGateway());
                updated = true;
            }

            // Update category
            if (optional.get().getCategory() == null && recipient.getCategory() != null) {
                optional.get().setCategory(recipient.getCategory());
                updated = true;
            }

            // Update logo
            if (recipient.getLogo() != null && !Objects.equals(optional.get().getLogo(), recipient.getLogo())) {
                optional.get().setLogo(recipient.getLogo());
                updated = true;
            }

            // Update serviceId
            if (recipient.getServiceId() != null && !Objects.equals(optional.get().getServiceId(), recipient.getServiceId())) {
                optional.get().setServiceId(recipient.getServiceId());
                updated = true;
            }

            // Update type
            if (recipient.getType() != null && !Objects.equals(optional.get().getType(), recipient.getType())) {
                optional.get().setType(recipient.getType());
                updated = true;
            }

            if (updated) {
                optional = Optional.of(dataAdapterDelegate.getRecipientRepository().save(optional.get()));
            }

            return optional.get();
        }

        if (recipient.getGateway() == null) {
            log.warn("Gateway must not be null");
            throw new CodedException(ProblemEnum.INVALID_PARAMS, "Gateway must not be null");
        }
        if (recipient.isActive() == null) {
            recipient.setActive(true);
        }
        if (recipient.getAmountRatio() == null) {
            recipient.setAmountRatio(Constants.DEFAULT_AMOUNT_RATIO);
        }
        if (recipient.getMaxAmount() == null) {
            recipient.setMaxAmount(100000000L);
        }
        if (recipient.getMinAmount() == null) {
            recipient.setMinAmount(100L);
        }
        if (recipient.getLimitRatio() == null) {
            recipient.setLimitRatio(Constants.DEFAULT_AMOUNT_RATIO);
        }
        if (recipient.getCommission() == null) {
            recipient.setCommission(0.5d);
        }
        if (recipient.getFee() == null) {
            recipient.setFee(0d);
        }
        if (recipient.getSystemCommission() == null) {
            recipient.setSystemCommission(1d);
        }
        if (recipient.getSystemFee() == null) {
            recipient.setSystemFee(0d);
        }
        if (recipient.getServiceId() == null) {
            recipient.setServiceId("");
        }
        if (recipient.getCcy() == null) {
            recipient.setCcy(Constants.UZS_CURRENCY);
        }

        log.debug("Saving Recipient : {}", recipient);

        log.info("Saving recipient : {} ...", recipient.getShortName());

        return dataAdapterDelegate.getRecipientRepository().save(recipient);
    }

    public boolean merge(Object existing, Object update) {

        if (!existing.getClass().isAssignableFrom(update.getClass())) {
            return false;
        }

        boolean updated = false;

        Method[] methods = existing.getClass().getMethods();

        for (Method fromMethod : methods) {

            if (Collection.class.isAssignableFrom(fromMethod.getReturnType())) {
                continue;
            }

            if (fromMethod.getDeclaringClass().equals(existing.getClass())
                    && fromMethod.getName().startsWith("get")) {

                String fromName = fromMethod.getName();
                String toName = fromName.replace("get", "set");

                try {
                    Method toMethod = existing.getClass().getMethod(toName, fromMethod.getReturnType());
                    Object value = fromMethod.invoke(update, (Object[]) null);
                    if (value != null) {
                        toMethod.invoke(existing, value);
                        updated = true;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        return updated;
    }

    protected boolean recipientExists(String recipientShortName, String gatewayShortName) {
        return dataAdapterDelegate.getRecipientRepository().findTop1ByShortNameAndGateway_Name(recipientShortName, gatewayShortName).isPresent();
    }

    protected Recipient getRecipientOrCreate(String recipientShortName, String gatewayShortName, String gatewayServiceBeanName, Integer ccy, Integer amountRatio) {
        return getRecipientOrCreate(recipientShortName, gatewayShortName, gatewayServiceBeanName, ccy, amountRatio, Collections.emptySet());
    }

    protected Recipient getRecipientOrCreate(String recipientShortName, String gatewayShortName, String gatewayServiceBeanName, Integer ccy, Integer amountRatio, Set<I18n> titles) {

        Optional<Recipient> optional = dataAdapterDelegate.getRecipientRepository().findTop1ByShortName(recipientShortName);

        if (optional.isPresent()) {
            log.debug("Found Recipient : {}", optional.get().getShortName());
            return optional.get();
        }

        Recipient recipient = new Recipient()
                .gateway(getGatewayOrCreate(gatewayShortName, gatewayServiceBeanName))
                .shortName(recipientShortName)
                .active(true)
                .maxAmount(100000000L) //10mln
                .minAmount(100L)
                .amountRatio(amountRatio)
                .limitRatio(amountRatio)
                .commission(0.5d)
                .fee(0d)
                .systemCommission(1d)
                .systemFee(0d)
                .serviceId("")
                .ccy(ccy)
                .titles(titles)
                .fullNames(titles)
                //
                ;

        log.debug("Saving recipient : {} ...", recipientShortName);

        return dataAdapterDelegate.getRecipientRepository().save(recipient);
    }

    protected Set<I18n> getI18ns(String nameForAll) {
        return getI18ns(nameForAll, nameForAll, nameForAll);
    }

    protected Set<I18n> getI18ns(String en, String ru, String uz) {
        HashSet<I18n> i18ns = new HashSet<>(3);
        i18ns.add(new I18n().lang(Lang.EN).text(en));
        i18ns.add(new I18n().lang(Lang.RU).text(ru));
        i18ns.add(new I18n().lang(Lang.UZ).text(uz));
        return i18ns;
    }

    public AgentDTO getAgentOrCreate(String shortName) {
        return getAgentOrCreate(shortName, 0L);
    }

    public AgentDTO getAgentOrCreate(String shortName, long balance) {
        return dataAdapterDelegate.getAgentService().getAgentOrCreate(shortName, balance);
    }

    public AgentDTO getAgentOrCreate(String shortName, long balance, String inn) {
        return dataAdapterDelegate.getAgentService().getAgentOrCreate(shortName, balance, inn);
    }

    public AgentDTO getAgentOrCreate(String shortName, long balance, String inn, Set<Integer> currencies) {
        return dataAdapterDelegate.getAgentService().getAgentOrCreate(shortName, balance, inn, currencies);
    }

    public AgentDTO getAgentOrCreate(Agent agent, long balance) {
        return dataAdapterDelegate.getAgentService().getAgentOrCreate(agent, balance);
    }

    public AgentRecipient saveAgentRecipient(Recipient recipient, AgentDTO agent) {

        log.debug("Adding AR : ('{}') '{}' - '{}'", recipient.getGateway().getName(), recipient.getShortName(), agent.getShortName());

        return dataAdapterDelegate.getAgentRecipientService().createOrGet(recipient.getId(), agent);
    }

    public AgentRecipient saveAgentRecipient(Recipient recipient, AgentDTO agent, Long settingId) {

        log.debug("Adding AR : ('{}') '{}' - '{}'", recipient.getGateway().getName(), recipient.getShortName(), agent.getShortName());

        return dataAdapterDelegate.getAgentRecipientService().createOrGet(recipient.getId(), agent, settingId);
    }

    public Category getCategoryOrCreate(Category category) {

        log.debug("Adding Cat : '{}'", category.getShortName());

        return dataAdapterDelegate.getCategoryService().getCategoryOrCreate(category);
    }

    public SettingDTO getSettingOrCreate(String name, PropAndValue... props) {

        log.debug("Adding Setting : '{}'", name);

        return dataAdapterDelegate.getSettingService().getOrCreate(name, props);
    }

    public boolean fieldExists(String fieldName, Long recipientId) {
        return dataAdapterDelegate.getFieldRepository().findTop1ByNameAndRecipientId(fieldName, recipientId).isPresent();
    }
}
