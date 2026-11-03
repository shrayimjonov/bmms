package uz.paykap.web.rest.util;

import static org.assertj.core.api.Assertions.fail;

import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import uz.devops.bm.dummy.adapter.DummyDataAdapterV3;
import uz.devops.bmcore.dto.request.CheckRequest;
import uz.paykap.adapter.DataAdapterServiceV3;
import uz.paykap.adapter.DummyAdapterDelegate;
import uz.paykap.domain.AgentRecipient;
import uz.paykap.domain.GroupSetting;
import uz.paykap.domain.Recipient;
import uz.paykap.repository.RecipientRepository;
import uz.paykap.service.AgentRecipientService;
import uz.paykap.service.GroupSettingService;
import uz.paykap.service.dto.AgentDTO;

public final class DummyUtils {

    public static final String DUMMY_ACCOUNT = "974415655";
    public static final long DUMMY_AMOUNT = 3000000L;
    public static final String TRAN_ID = "TRAN_ID";

    public static CheckRequest getDummyCheckDTO(Long dummyAmount) {
        return getDummyCheckDTO(dummyAmount, DUMMY_ACCOUNT);
    }

    public static CheckRequest getDummyCheckDTO(Long dummyAmount, String account) {
        CheckRequest commonCheckDTO = new CheckRequest();

        commonCheckDTO.setAccount(account);
        commonCheckDTO.setAmount(dummyAmount);
        commonCheckDTO.setCurrency(860);
        commonCheckDTO.setAgentTranId(TRAN_ID);

        return commonCheckDTO;
    }

    public static void prepareDummyData(DummyAdapterDelegate dummyAdapterDelegate) {
        prepareDummyData(
            dummyAdapterDelegate.getDataAdapterV3(),
            dummyAdapterDelegate.getGroupSettingService(),
            dummyAdapterDelegate.getRecipientRepository(),
            dummyAdapterDelegate.getAgentRecipientService()
        );
    }

    public static void prepareDummyData(
        DataAdapterServiceV3 dataAdapterServiceV3,
        GroupSettingService groupSettingService,
        RecipientRepository recipientRepository,
        AgentRecipientService agentRecipientService
    ) {
        prepareDummyData(dataAdapterServiceV3, groupSettingService, recipientRepository, agentRecipientService, null);
    }

    public static void prepareDummyData(
        DataAdapterServiceV3 dataAdapterServiceV3,
        GroupSettingService groupSettingService,
        RecipientRepository recipientRepository,
        AgentRecipientService agentRecipientService,
        Long settingId
    ) {
        GroupSetting groupSetting = groupSettingService.createEposSettings(
            "Dummy Payment",
            DummyDataAdapterV3.DUMMY_REQUEST_GROUP,
            "123456",
            "5678"
        );

        AgentDTO agent = dataAdapterServiceV3.getAgentOrCreate(DummyDataAdapterV3.DUMMY_AGENT, 1000000L, DummyDataAdapterV3.INN);
        AgentDTO agentUsd = dataAdapterServiceV3.getAgentOrCreate(
            DummyDataAdapterV3.DUMMY_AGENT_USD,
            1000000L,
            DummyDataAdapterV3.INN,
            new HashSet<>() {
                {
                    add(840);
                }
            }
        );
        AgentDTO agentKiosk = dataAdapterServiceV3.getAgentOrCreate(DummyDataAdapterV3.DUMMY_KIOSK_AGENT, 1000000L, DummyDataAdapterV3.INN);

        dataAdapterServiceV3.sync(DummyDataAdapterV3.NAME);

        Optional<Recipient> recipientUSD = recipientRepository.findTop1ByShortName(DummyDataAdapterV3.DUMMY_RECIPIENT_USD);
        Optional<Recipient> recipientUZS = recipientRepository.findTop1ByShortName(DummyDataAdapterV3.DUMMY_RECIPIENT_UZS);

        if (recipientUSD.isEmpty()) {
            fail("recipientUSD not found");
        }

        if (recipientUZS.isEmpty()) {
            fail("recipientUZS not found");
        }

        recipientRepository.save(recipientUSD.get().check(true));
        recipientRepository.save(recipientUZS.get().check(true));

        final AgentRecipient agentRecipientUSD = agentRecipientService.createOrGet(recipientUSD.get().getId(), agent, settingId, 0d, 7d, 5d, 3d, 2d);
        final AgentRecipient agentRecipientUZS = agentRecipientService.createOrGet(recipientUZS.get().getId(), agent, settingId, 0d, 7d, 5d, 3d, 2d);

        agentRecipientUSD.setJsonParams("{\"a\":5,\"b\":67}");
        agentRecipientUZS.setJsonParams("{\"a\":77,\"b\":68}");

        agentRecipientService.save(agentRecipientUSD);
        agentRecipientService.save(agentRecipientUZS);

        agentRecipientService.createOrGet(
            recipientUSD.get().getId(),
            agentKiosk,
            settingId,
            7d,
            1d,
            5d,
            3d,
            2d
        );
        agentRecipientService.createOrGet(recipientUZS.get().getId(), agentKiosk, settingId, 0d, 7d, 5d, 3d, 2d);

        agentRecipientService.createOrGet(recipientUSD.get().getId(), agentUsd, settingId, 0d, 7d, 5d, 3d, 2d);
        agentRecipientService.createOrGet(recipientUZS.get().getId(), agentUsd, settingId, 7d, 1d, 5d, 0d, 2.5d);
    }

    public static void prepareDummyData(
        DataAdapterServiceV3 dataAdapterServiceV3,
        GroupSettingService groupSettingService,
        RecipientRepository recipientRepository,
        AgentRecipientService agentRecipientService,
        Long settingId,
        String shortname
    ) {
        GroupSetting groupSetting = groupSettingService.createEposSettings(
            "Dummy Payment",
            DummyDataAdapterV3.DUMMY_REQUEST_GROUP,
            "123456",
            "5678"
        );

        AgentDTO agent = dataAdapterServiceV3.getAgentOrCreate(shortname, 100000000000L, DummyDataAdapterV3.INN);
        AgentDTO agentUsd = dataAdapterServiceV3.getAgentOrCreate(
            DummyDataAdapterV3.DUMMY_AGENT_USD,
            1000000L,
            DummyDataAdapterV3.INN,
            new HashSet<>() {
                {
                    add(840);
                }
            }
        );
        AgentDTO agentKiosk = dataAdapterServiceV3.getAgentOrCreate(DummyDataAdapterV3.DUMMY_KIOSK_AGENT, 1000000L, DummyDataAdapterV3.INN);

        dataAdapterServiceV3.sync(DummyDataAdapterV3.NAME);

        Optional<Recipient> recipientUSD = recipientRepository.findTop1ByShortName(DummyDataAdapterV3.DUMMY_RECIPIENT_USD);
        Optional<Recipient> recipientUZS = recipientRepository.findTop1ByShortName(DummyDataAdapterV3.DUMMY_RECIPIENT_UZS);

        if (recipientUSD.isEmpty()) {
            fail("recipientUSD not found");
        }

        if (recipientUZS.isEmpty()) {
            fail("recipientUZS not found");
        }

        recipientRepository.save(recipientUSD.get().check(true));
        recipientRepository.save(recipientUZS.get().check(true));

        final AgentRecipient agentRecipientUSD = agentRecipientService.createOrGet(recipientUSD.get().getId(), agent, settingId, 0d, 7d, 5d, 3d, 2d);
        final AgentRecipient agentRecipientUZS = agentRecipientService.createOrGet(recipientUZS.get().getId(), agent, settingId, 0d, 7d, 5d, 3d, 2d);

        agentRecipientUSD.setJsonParams("{\"a\":5,\"b\":67}");
        agentRecipientUZS.setJsonParams("{\"a\":77,\"b\":68}");

        agentRecipientService.save(agentRecipientUSD);
        agentRecipientService.save(agentRecipientUZS);

        agentRecipientService.createOrGet(
            recipientUSD.get().getId(),
            agentKiosk,
            settingId,
            7d,
            1d,
            5d,
            3d,
            2d
        );
        agentRecipientService.createOrGet(recipientUZS.get().getId(), agentKiosk, settingId, 0d, 7d, 5d, 3d, 2d);

        agentRecipientService.createOrGet(recipientUSD.get().getId(), agentUsd, settingId, 0d, 7d, 5d, 3d, 2d);
        agentRecipientService.createOrGet(recipientUZS.get().getId(), agentUsd, settingId, 7d, 1d, 5d, 0d, 2.5d);
    }
}
