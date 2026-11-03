package uz.paykap.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import uz.devops.bmcore.cache.CacheEvictable;
import uz.javlon.commons.base.ResultData;
import uz.javlon.commons.exception.CodedException;
import uz.javlon.commons.exception.ProblemEnum;
import uz.paykap.domain.Agent;
import uz.paykap.repository.*;
import uz.paykap.security.SecurityUtils;
import uz.paykap.service.dto.api.AllData;
import uz.paykap.service.dto.simple.SimpleRecipientDTO;
import uz.paykap.service.mapper.simple.SimpleCategoryMapper;
import uz.paykap.service.mapper.simple.SimpleFieldMapper;
import uz.paykap.service.mapper.simple.SimpleRecipientMapper;
import uz.paykap.service.mapper.simple.SimpleResponseFieldMapper;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@AllArgsConstructor
public class AllowedAgentService implements CacheEvictable {

    public static final String RECIPIENTS_BY_LOGIN_CACHE_NAME = "recipients-by-login";
    public static final String RECIPIENT_BY_ID_CACHE_NAME = "recipient-by-id";

    public String getAllByLogin() {
        Optional<String> login = SecurityUtils.getCurrentUserLogin();

        if (login.isPresent()) {
            return getAllByLoginAsString(login.get());
        }

        log.warn("Request to get all allowed categories and recipients : agent or login not found");

        return "{}";
    }

    @Deprecated
    //    @Cacheable(value = RECIPIENTS_BY_LOGIN_CACHE_NAME, key = "#agentOrLogin", condition = "#agentOrLogin != null")
    public AllData getAllByLogin(String agentOrLogin) {
        log.debug("Request to get all allowed categories and recipients");

        Agent agent = agentRepository.findTop1ByShortName(agentOrLogin).orElseThrow(() -> new CodedException(ProblemEnum.OBJECT_NOT_FOUND));

        final AllData allData = new AllData()
            .withCategories(categoryMapper.toDto(categoryRepository.findAllAllowedWithParents(agent.getId())))
            .withRecipients(recipientMapper.toDto(recipientRepository.findAllAllowed(agent.getId())))
            .withFields(fieldMapper.toDto(fieldRepository.findAllAllowed(agent.getId())))
            .withResponseFields(responseFieldMapper.toDto(responseFieldRepository.findAllAllowedAndNotHidden(agent.getId())));

        return allData;
    }

    @SneakyThrows
    @Cacheable(value = RECIPIENTS_BY_LOGIN_CACHE_NAME, key = "#agentOrLogin", condition = "#agentOrLogin != null", sync = true)
    public String getAllByLoginAsString(String agentOrLogin) {
        log.debug("Request to get all allowed categories and recipients as String by : {}", agentOrLogin);

        Agent agent = agentRepository.findTop1ByShortName(agentOrLogin).orElseThrow(() -> new CodedException(ProblemEnum.OBJECT_NOT_FOUND));

        final AllData allData = new AllData()
            .withCategories(categoryMapper.toDto(categoryRepository.findAllAllowedWithParents(agent.getId())))
            .withRecipients(recipientMapper.toDto(recipientRepository.findAllAllowed(agent.getId())))
            .withFields(fieldMapper.toDto(fieldRepository.findAllAllowed(agent.getId())))
            .withResponseFields(responseFieldMapper.toDto(responseFieldRepository.findAllAllowedAndNotHidden(agent.getId())));

        return objectMapper.writeValueAsString(allData);
    }

    // TODO - add cache
    public AllData getRecipientInfoByLoginAndId(Long recipientId) {
        log.debug("Request to get allowed recipient and fields");
        String username = SecurityUtils.getCurrentUserLogin().orElseThrow(() -> new CodedException(ProblemEnum.USERNAME_NOT_FOUND));

        return new AllData()
            //            .categories(categoryMapper.toDto(categoryRepository.findAllAllowedWithParents(agent.getId())))
            .withRecipients(recipientMapper.toDto(recipientRepository.findAllowedRecipient(recipientId,username)))
            .withFields(fieldMapper.toDto(fieldRepository.findFirstByLoginAndRecipientId(recipientId,username)))
            .withResponseFields(responseFieldMapper.toDto(responseFieldRepository.findAllowedByRecipientId(recipientId,username)))//
        ;
    }

    //    public List<SimpleRecipientDTO> getRecipientsByInn(String recipientInn) {
    //        log.debug("Request to get allowed recipient by inn: {}", recipientInn);
    //        return recipientMapper.toDto(recipientRepository.findAllowedRecipientByInn(recipientInn));
    //    }
    public ResultData<List<SimpleRecipientDTO>> getRecipientsByInn(String recipientInn) {
        log.debug("Request to get allowed recipient by inn: {}", recipientInn);
        Optional<String> currentUserLogin = SecurityUtils.getCurrentUserLogin();
        if (currentUserLogin.isEmpty()) {
            log.warn("currentUserLogin is empty..");
            return new ResultData<>(false, null, new CodedException(ProblemEnum.USERNAME_NOT_FOUND));
        }
        log.debug("username: {}", currentUserLogin.get());
        return new ResultData<>(recipientMapper.toDto(recipientRepository.findAllowedRecipientByInn(recipientInn, currentUserLogin.get())));
    }

    @SneakyThrows
    @Cacheable(value = RECIPIENT_BY_ID_CACHE_NAME, key = "#recipientId", condition = "#recipientId != null", sync = true)
    public String getRecipientInfoByLoginAndIdAsString(Long recipientId) {
        log.debug("Request to get allowed recipient and fields");
        String username = SecurityUtils.getCurrentUserLogin().orElseThrow(() -> new CodedException(ProblemEnum.USERNAME_NOT_FOUND));

        final AllData allData = new AllData()
            //            .categories(categoryMapper.toDto(categoryRepository.findAllAllowedWithParents(agent.getId())))
            .withRecipients(recipientMapper.toDto(recipientRepository.findAllowedRecipient(recipientId,username)))
            .withFields(fieldMapper.toDto(fieldRepository.findFirstByLoginAndRecipientId(recipientId,username)))
            .withResponseFields(responseFieldMapper.toDto(responseFieldRepository.findAllowedByRecipientId(recipientId,username)));
        return objectMapper.writeValueAsString(allData)//
        ;
    }

    private final SimpleCategoryMapper categoryMapper;
    private final SimpleRecipientMapper recipientMapper;
    private final SimpleFieldMapper fieldMapper;
    private final SimpleResponseFieldMapper responseFieldMapper;
    private final CategoryRepository categoryRepository;
    private final RecipientRepository recipientRepository;
    private final FieldRepository fieldRepository;
    private final ResponseFieldRepository responseFieldRepository;
    private final AgentRepository agentRepository;
    private final ObjectMapper objectMapper;

    @Override
    @CacheEvict(value = { RECIPIENTS_BY_LOGIN_CACHE_NAME, RECIPIENT_BY_ID_CACHE_NAME }, allEntries = true)
    public void evictAll() {
        log.info("Evicting {} {}...", RECIPIENTS_BY_LOGIN_CACHE_NAME, RECIPIENT_BY_ID_CACHE_NAME);
    }
}
