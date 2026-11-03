package uz.paykap.service;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import uz.paykap.IntegrationTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;
import uz.devops.bm.dummy.adapter.DummyDataAdapterV3;
import uz.paykap.PkbillmsApp;
import uz.paykap.adapter.DummyAdapterDelegate;
import uz.paykap.IntegrationTest;
import uz.paykap.domain.SimpleRecipient;
import uz.paykap.web.rest.util.DummyUtils;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@IntegrationTest
public class RecipientServiceTest {

    @Autowired
    private RecipientService recipientService;
    @Autowired
    private DummyAdapterDelegate dummyAdapterDelegate;

    @Test
    @Transactional
    @WithMockUser(roles = {"ADMIN"})
    public void testGetAllowedRecipients() {
        DummyUtils.prepareDummyData(dummyAdapterDelegate);

        Page<SimpleRecipient> namesV3 = recipientService.getOnlyAllowedRecipientNamesForAgentOrAllForAdmin("usd", PageRequest.of(0, 10));
        assertThat(namesV3).isNotNull();
        assertThat(namesV3).isNotEmpty();
    }

    @Test
    @Transactional
    @WithMockUser(username = DummyDataAdapterV3.DUMMY_AGENT)
    public void testGetAllowedRecipientNames() {

        DummyUtils.prepareDummyData(dummyAdapterDelegate);

        Page<String> allowedRecipientNames = recipientService.getAllowedRecipientNames(PageRequest.of(0, 10));


        assertThat(allowedRecipientNames).isNotNull();
        assertThat(allowedRecipientNames.getContent()).isNotNull();
        assertThat(allowedRecipientNames.getContent()).isNotEmpty();
    }
}
