package uz.paykap.service.p2p.register;

import lombok.SneakyThrows;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import uz.paykap.service.dto.p2p.BmResultStatus;
import uz.paykap.service.dto.p2p.P2pAccount;
import uz.paykap.service.dto.p2p.P2pAccountType;
import uz.paykap.service.dto.p2p.P2pRequestScopedContext;
import uz.paykap.service.dto.result.BmResult;
import uz.paykap.service.dto.result.BmResultData;
import uz.paykap.service.p2p.dto.P2pDTO;
import uz.paykap.service.p2p.support.AbstractP2pRecipient;
import uz.paykap.service.p2p.support.AbstractP2pSender;

import javax.annotation.PostConstruct;
import java.lang.reflect.ParameterizedType;
import java.util.*;

@Service
@Slf4j
public class P2pRegistry {

    private final Map<P2pAccountType, List<AbstractP2pSender<P2pDTO>>> sendersMap = new HashMap<>();
    private final Map<P2pAccountType, List<AbstractP2pRecipient<P2pDTO>>> recipientsMap = new HashMap<>();

    private final ApplicationContext applicationContext;
    private final P2pRequestScopedContext p2pRequestScopedContext;

    public P2pRegistry(ApplicationContext applicationContext, P2pRequestScopedContext p2pRequestScopedContext) {
        this.applicationContext = applicationContext;
        this.p2pRequestScopedContext = p2pRequestScopedContext;
    }

    @SneakyThrows
    @PostConstruct
    public void init() {

        var clazzLoader = applicationContext.getClassLoader();

        Map<String, AbstractP2pSender> beans = applicationContext.getBeansOfType(AbstractP2pSender.class);

        for (AbstractP2pSender value : beans.values()) {
            var validatorClazz = value.getClass();
            var validatorInterface = (ParameterizedType) validatorClazz.getGenericSuperclass();
            var validatorParamsType = validatorInterface.getActualTypeArguments()[0];
            var validatorParamsClazz = clazzLoader.loadClass(validatorParamsType.getTypeName());

            Optional<P2pAccountType> typeOptional = P2pAccountType.getByDtoClass(validatorParamsClazz);

            typeOptional.ifPresent(p2pAccountType -> registerSenders(p2pAccountType, value));
        }

        Map<String, AbstractP2pRecipient> recipientMap = applicationContext.getBeansOfType(AbstractP2pRecipient.class);

        for (AbstractP2pRecipient value : recipientMap.values()) {
            var validatorClazz = value.getClass();
            var validatorInterface = (ParameterizedType) validatorClazz.getGenericSuperclass();
            var validatorParamsType = validatorInterface.getActualTypeArguments()[0];
            var validatorParamsClazz = clazzLoader.loadClass(validatorParamsType.getTypeName());

            Optional<P2pAccountType> typeOptional = P2pAccountType.getByDtoClass(validatorParamsClazz);

            typeOptional.ifPresent(p2pAccountType -> registerRecipients(p2pAccountType, value));
        }

    }

    private void registerSenders(P2pAccountType type, AbstractP2pSender<P2pDTO> senderService) {

        if (!sendersMap.containsKey(type)) {
            sendersMap.put(type, new ArrayList<>());
        }

        sendersMap.get(type).add(senderService);
    }

    private void registerRecipients(P2pAccountType type, AbstractP2pRecipient<P2pDTO> service) {

        if (!recipientsMap.containsKey(type)) {
            recipientsMap.put(type, new ArrayList<>());
        }

        recipientsMap.get(type).add(service);
    }

    public BmResult getSenderService(P2pAccount account) {

        List<AbstractP2pSender<P2pDTO>> services = sendersMap.getOrDefault(account.getType(), Collections.emptyList());
        log.info("The type : {} support service : {}", account.getType(), services);
        for (AbstractP2pSender<P2pDTO> service : services) {

            BmResultData<P2pDTO> dtoResult = service.parseAndValidate(account.getParams());

            if (!dtoResult.isSuccess()) {
                continue;
            }

            BmResult p2pSupported = service.isSupported(dtoResult.getData(), account);

            if (p2pSupported.isSuccess()) {
                p2pRequestScopedContext.setSenderDto(dtoResult.getData());
                p2pRequestScopedContext.setSenderService(service);
                return new BmResult(BmResultStatus.SUCCESS);
            }

        }

        return new BmResult(BmResultStatus.INVALID_SENDER_PARAMS, "None sender service supports the account : " + account.getType());
    }

    public BmResult getRecipientService(P2pAccount account) {

        List<AbstractP2pRecipient<P2pDTO>> services = recipientsMap.getOrDefault(account.getType(), Collections.emptyList());

        for (AbstractP2pRecipient<P2pDTO> service : services) {

            BmResultData<P2pDTO> dtoResult = service.parseAndValidate(account.getParams());

            if (!dtoResult.isSuccess()) {
                continue;
            }

            BmResult p2pSupported = service.isSupported(dtoResult.getData(), account);

            if (p2pSupported.isSuccess()) {
                p2pRequestScopedContext.setRecipientDto(dtoResult.getData());
                p2pRequestScopedContext.setRecipientService(service);
                return new BmResult(BmResultStatus.SUCCESS);
            }

        }

        return new BmResult(BmResultStatus.INVALID_RECIPIENT_PARAMS, "None recipient service supports the account : " + account.getType());
    }

}
