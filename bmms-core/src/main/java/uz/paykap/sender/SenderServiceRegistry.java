package uz.paykap.sender;

import org.springframework.stereotype.Component;
import uz.devops.bmcore.enumeration.PayType;
import uz.devops.service.registry.AbstractMonoServiceMonoKeyRegistry;

@Component
public class SenderServiceRegistry extends AbstractMonoServiceMonoKeyRegistry<SenderService, PayType> {

    @Override
    protected Class<SenderService> getServiceClazz() {
        return SenderService.class;
    }

    @Override
    public String getDescription() {
        return "Регистратор отправителей денежных средств";
    }
}
