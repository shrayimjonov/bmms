package uz.paykap.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import uz.paykap.IntegrationTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;
import uz.devops.bmcore.enumeration.Lang;
import uz.paykap.PkbillmsApp;
import uz.paykap.config.Constants;
import uz.paykap.IntegrationTest;
import uz.paykap.domain.Agent;
import uz.paykap.domain.I18n;
import uz.paykap.domain.Recipient;
import uz.paykap.domain.RecipientGroup;
import uz.paykap.repository.*;
import uz.paykap.service.dto.AgentDTO;
import uz.paykap.service.dto.AgentRecipientDTO;
import uz.paykap.service.dto.RecipientGroupDTO;
import uz.paykap.service.mapper.RecipientGroupMapper;
import uz.paykap.service.mapper.RecipientMapper;

@RunWith(SpringRunner.class)
@IntegrationTest
public class AgentServiceTest {

    @Autowired
    private GatewayRepository gatewayRepository;

    @Autowired
    private RecipientRepository recipientRepository;

    @Autowired
    private AgentRecipientService agentRecipientService;

    @Autowired
    private AgentRepository agentRepository;

    @Autowired
    private RecipientGroupRepository recipientGroupRepository;

    @Autowired
    private RecipientMapper recipientMapper;

    @Autowired
    private RecipientGroupMapper recipientGroupMapper;

    @Autowired
    private AgentRecipientRepository agentRecipientRepository;

    @Test
    @Transactional
    public void createAgentRecipientGroup() {
        Agent agent = createAgent();

        AgentRecipientDTO agentRecipientDTO = createAgentRecipientDTO(agent);

        Recipient recipient = saveRecipient();

        RecipientGroup recipientGroup = createRecipientGroup(recipient);

        RecipientGroupDTO group = new RecipientGroupDTO();
        group.setId(recipientGroup.getId());
        agentRecipientDTO.setRecipientGroup(group);

        //        AgentRecipientGroupDTO agentRecipientGroupDTO = new AgentRecipientGroupDTO();
        //        agentRecipientGroupDTO.setAgentRecipientDTO(agentRecipientDTO);
        //        agentRecipientGroupDTO.setRecipientGroupDTO(recipientGroupMapper.toDto(recipientGroup));

        List<AgentRecipientDTO> agentRecipientDTOS = agentRecipientService.createOneOrAllByGroup(agentRecipientDTO);

        assertThat(agentRecipientDTOS.size()).isEqualTo(recipientGroup.getRecipients().size());

        AgentRecipientDTO agentRecipientDTOAfter = agentRecipientDTOS.iterator().next();

        assertThat(agentRecipientDTOAfter.getSystemCommissionDown()).isEqualTo(agentRecipientDTO.getSystemCommissionDown());
        assertThat(agentRecipientDTOAfter.getRecipientGroup().getId()).isEqualTo(recipientGroup.getId());
        assertThat(agentRecipientDTOAfter.getAgentCommission()).isEqualTo(agentRecipientDTO.getAgentCommission());
        assertThat(agentRecipientDTOAfter.getRecipient().getId()).isEqualTo(recipientGroup.getRecipients().iterator().next().getId());
        assertThat(agentRecipientDTOAfter.getAgent().getId()).isEqualTo(agentRecipientDTO.getAgent().getId());
    }

    private AgentRecipientDTO createAgentRecipientDTO(Agent agent) {
        AgentDTO dto = new AgentDTO();
        dto.setId(agent.getId());
        return new AgentRecipientDTO()
            .agentCommission(0.00)
            .agentFee(0.00)
            .systemCommissionUp(0.00)
            .systemCommissionDown(0.00)
            .systemFee(0.00)
            .active(true)
            .eposPort(1)
            .eposMerchant("123")
            .eposTerminal("1234")
            //            .settingId(1L)
            .agent(dto); //            .agentShortName(agent.getShortName())
    }

    private Agent createAgent() {
        Agent agent = new Agent()
            .shortName("Agent")
            .amountRatio(1)
            .apiKey("apiKey")
            .currencies(
                new HashSet<>() {
                    {
                        add(860);
                    }
                }
            )
            .ipAddresses(
                new HashSet<>() {
                    {
                        add("ip");
                    }
                }
            )
            .active(true)
            .titles(getI18ns("we", "we", "we"));
        return agentRepository.save(agent);
    }

    private RecipientGroup createRecipientGroup(Recipient recipient) {
        RecipientGroup recipientGroup = new RecipientGroup()
            .name("recipientGroup")
            .active(true)
            .more("more")
            .recipients(Collections.singleton(recipient));
        return recipientGroupRepository.save(recipientGroup);
    }

    private Recipient saveRecipient() {
        Recipient recipient = new Recipient()
            .serviceId("1")
            .active(true)
            .thumbnail("1.png")
            .logo("1.png")
            .amountRatio(12)
            .limitRatio(12)
            .shortName("Recipient")
            .ccy(Constants.UZS_CURRENCY)
            .commission(1D)
            .maxAmount(100000L)
            .minAmount(100L)
            .ord(1)
            .titles(getI18ns("EN", "RU", "UZ"));

        return recipientRepository.save(recipient);
    }

    private Set<I18n> getI18ns(String en, String ru, String uz) {
        HashSet<I18n> i18ns = new HashSet<>(3);
        i18ns.add(new I18n().lang(Lang.EN).text(en));
        i18ns.add(new I18n().lang(Lang.RU).text(ru));
        i18ns.add(new I18n().lang(Lang.UZ).text(uz));
        return i18ns;
    }
}
