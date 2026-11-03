package uz.paykap.adapter;

import org.springframework.stereotype.Service;
import uz.paykap.repository.RecipientRepository;
import uz.paykap.service.AgentRecipientService;
import uz.paykap.service.GroupSettingService;

@Service
public class DummyAdapterDelegate {
    private final GroupSettingService groupSettingService;
    private final DataAdapterServiceV3 dataAdapterServiceV3;
    private final RecipientRepository recipientRepository;
    private final AgentRecipientService agentRecipientService;

    public DummyAdapterDelegate(GroupSettingService groupSettingService, DataAdapterServiceV3 dataAdapterServiceV3, RecipientRepository recipientRepository, AgentRecipientService agentRecipientService) {
        this.groupSettingService = groupSettingService;
        this.dataAdapterServiceV3 = dataAdapterServiceV3;
        this.recipientRepository = recipientRepository;
        this.agentRecipientService = agentRecipientService;
    }

    public GroupSettingService getGroupSettingService() {
        return groupSettingService;
    }

    public DataAdapterServiceV3 getDataAdapterV3() {
        return dataAdapterServiceV3;
    }

    public RecipientRepository getRecipientRepository() {
        return recipientRepository;
    }

    public AgentRecipientService getAgentRecipientService() {
        return agentRecipientService;
    }
}
