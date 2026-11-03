package uz.paykap.adapter;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.stereotype.Service;
import uz.paykap.repository.*;
import uz.paykap.service.*;
import uz.paykap.service.mapper.CategoryMapper;
import uz.paykap.service.mapper.FieldMapper;
import uz.paykap.service.mapper.RecipientMapper;
import uz.paykap.service.mapper.ResponseFieldMapper;

@Service
@Getter
@AllArgsConstructor
public class DataAdapterDelegate {

    private final DataAdapterHistoryService historyService;
    private final GatewayRepository gatewayRepository;
    private final AgentRepository agentRepository;
    private final AgentService agentService;
    private final RecipientRepository recipientRepository;
    private final AgentRecipientService agentRecipientService;
    private final CategoryService categoryService;
    private final FieldRepository fieldRepository;
    private final ResponseFieldRepository responseFieldRepository;
    private final SettingService settingService;
    private final RecipientMapper recipientMapper;
    private final FieldMapper fieldMapper;
    private final ResponseFieldMapper responseFieldMapper;
    private final PrefixRepository prefixRepository;
    private final FieldValueRepository fieldValueRepository;
    private final CategoryMapper categoryMapper;
    private final CategoryRepository categoryRepository;
    private final RecipientGroupRepository recipientGroupRepository;
}
