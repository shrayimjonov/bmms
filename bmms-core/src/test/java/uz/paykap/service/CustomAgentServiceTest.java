package uz.paykap.service;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import uz.paykap.IntegrationTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;
import uz.devops.bmcore.enumeration.Visibility;
import uz.paykap.PkbillmsApp;
import uz.paykap.config.Constants;
import uz.paykap.IntegrationTest;
import uz.paykap.domain.*;
import uz.paykap.repository.AgentRecipientRepository;
import uz.paykap.repository.AgentRepository;
import uz.paykap.repository.RecipientRepository;
import uz.paykap.repository.ResponseFieldRepository;
import uz.paykap.service.dto.api.AllData;
import uz.paykap.web.rest.CategoryResourceIntTest;
import uz.paykap.web.rest.ResponseFieldResourceIntTest;
import uz.paykap.web.rest.TestUtil;

import javax.persistence.EntityManager;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@IntegrationTest
public class CustomAgentServiceTest {

    @Autowired
    private AllowedAgentService customAgentService;
    @Autowired
    private RecipientRepository recipientRepository;
    @Autowired
    private ResponseFieldRepository responseFieldRepository;
    @Autowired
    private AgentRepository agentRepository;
    @Autowired
    private AgentRecipientRepository agentRecipientRepository;
    @Autowired
    private CategoryService categoryService;
    @Autowired
    private EntityManager em;

    @Test
    @Transactional
    public void getAllByLogin() {

        Category category0 = categoryService.getCategoryOrCreate(CategoryResourceIntTest.createEntity(null, "category0"));
        Category category1 = categoryService.getCategoryOrCreate(CategoryResourceIntTest.createEntity(null, "category1"));
        Category category2 = categoryService.getCategoryOrCreate(CategoryResourceIntTest.createEntity(null, "category2"));

        Agent agentNoOne = agentRepository.save(createAgent("no_one"));
        Agent agentOnlyOne = agentRepository.save(createAgent("only_one"));
        Agent agentAll = agentRepository.save(createAgent("all"));

        Recipient recipientDTO1 = recipientRepository.save(createRecipient(1, category1));
        Recipient recipientDTO2 = recipientRepository.save(createRecipient(2, category2));
        Recipient recipientDTO3 = recipientRepository.save(createRecipient(3, category2));

        ResponseField responseFieldHidden = responseFieldRepository.save(ResponseFieldResourceIntTest.createEntity(em, recipientDTO1));
        ResponseField responseField1 = responseFieldRepository.save(ResponseFieldResourceIntTest.createEntity(em, recipientDTO1).visibility(Visibility.ALL));

        //All
        AgentRecipient agentRecipient1 = agentRecipientRepository.save(createAgentRecipient(1, recipientDTO1, agentAll));
        AgentRecipient agentRecipient2 = agentRecipientRepository.save(createAgentRecipient(2, recipientDTO2, agentAll));
        AgentRecipient agentRecipient3 = agentRecipientRepository.save(createAgentRecipient(3, recipientDTO3, agentAll));

        //Only one
        AgentRecipient agentRecipient4 = agentRecipientRepository.save(createAgentRecipient(1, recipientDTO1, agentOnlyOne));

        AllData allAllowedAll = customAgentService.getAllByLogin(agentAll.getShortName());
        AllData allAllowedOnlyOne = customAgentService.getAllByLogin(agentOnlyOne.getShortName());
        AllData allAllowedNoOne = customAgentService.getAllByLogin(agentNoOne.getShortName());

        //check All
        assertThat(allAllowedAll.getRecipients()).isNotNull();
        assertThat(allAllowedAll.getRecipients()).isNotEmpty();
        assertThat(allAllowedAll.getRecipients()).hasSize(3);
        assertThat(allAllowedAll.getRecipients().get(0)).isNotNull();
        assertThat(allAllowedAll.getRecipients().get(0).getShortName()).isNotBlank();

        assertThat(allAllowedAll.getCategories()).isNotNull();
        assertThat(allAllowedAll.getCategories()).isNotEmpty();
        assertThat(allAllowedAll.getCategories()).hasSize(2);

        // check if result response fields NOT contains visibility=HIDDEN
        assertThat(allAllowedAll.getResponseFields()).isNotNull();
        assertThat(allAllowedAll.getResponseFields()).isNotEmpty();
        assertThat(allAllowedAll.getResponseFields()).hasSize(1);
        assertThat(allAllowedAll.getResponseFields().get(0).getVisibility()).isNotEqualTo(Visibility.HIDDEN);

        //check Only one
        assertThat(allAllowedOnlyOne.getRecipients()).isNotNull();
        assertThat(allAllowedOnlyOne.getRecipients()).isNotEmpty();
        assertThat(allAllowedOnlyOne.getRecipients()).hasSize(1);

        assertThat(allAllowedOnlyOne.getCategories()).isNotNull();
        assertThat(allAllowedOnlyOne.getCategories()).isNotEmpty();
        assertThat(allAllowedOnlyOne.getCategories()).hasSize(1);

        assertThat(allAllowedOnlyOne.getCategories().get(0).getId()).isEqualTo(allAllowedOnlyOne.getRecipients().get(0).getCategoryId());

        //check No one
        assertThat(allAllowedNoOne.getRecipients()).isNotNull();
        assertThat(allAllowedNoOne.getRecipients()).isEmpty();

    }

    public static Recipient createRecipient(Integer seq, Category category) {
        Recipient recipient = new Recipient()
            .shortName(seq + "_" + DEFAULT_SHORT_NAME)
            .ccy(Constants.UZS_CURRENCY)
            .serviceId(DEFAULT_SERVICE_ID)
            .thumbnail(new String(DEFAULT_THUMBNAIL))
            .logo(new String(DEFAULT_LOGO))
            .amountRatio(DEFAULT_AMOUNT_RATIO)
            .limitRatio(DEFAULT_AMOUNT_RATIO)
            .minAmount(DEFAULT_MIN_AMOUNT)
            .maxAmount(DEFAULT_MAX_AMOUNT)
            .commission(DEFAULT_COMMISSION)
            .fee(DEFAULT_FEE)
            .systemCommission(DEFAULT_SYSTEM_COMMISSION)
            .systemFee(DEFAULT_SYSTEM_FEE)
            .ord(DEFAULT_ORD)
            .active(DEFAULT_ACTIVE)
            .category(category)
            //
            ;
        // Add required entity
//        I18n i18n = I18nResourceIntTest.createEntity(em);
//        em.persist(i18n);
//        em.flush();
//        recipient.getTitles().add(i18n);
        // Add required entity
//        recipient.getFullNames().add(i18n);
        // Add required entity
//        GatewayParam gatewayParam = GatewayParamResourceIntTest.createEntity(em);
//        em.persist(gatewayParam);
//        em.flush();
//        recipient.setGatewayParam(gatewayParam);
        return recipient;
    }

    public static Agent createAgent(String seq) {
        Agent agent = new Agent()
            .shortName(seq + "_" + DEFAULT_SHORT_NAME)
            .amountRatio(DEFAULT_AMOUNT_RATIO)
            .apiKey(DEFAULT_API_KEY)
            .currencies(Constants.DEFAULT_CURRENCIES)
            .ipAddresses(Collections.emptySet())
            .logo(DEFAULT_LOGO)
            .logoContentType(DEFAULT_LOGO_CONTENT_TYPE)
            .ord(DEFAULT_ORD)
            .active(DEFAULT_ACTIVE);
        // Add required entity
//        I18n i18n = I18nResourceIntTest.createEntity(em);
//        em.persist(i18n);
//        em.flush();
//        agent.getTitles().add(i18n);
        return agent;
    }

    public AgentRecipient createAgentRecipient(Integer amountRatio, Recipient recipient, Agent agent) {
        AgentRecipient agentRecipient = new AgentRecipient()
//            .amountRatio(amountRatio)
            .agentCommission(DEFAULT_COMMISSION)
            .agentFee(DEFAULT_FEE)
            .systemCommissionDown(DEFAULT_SYSTEM_COMMISSION)
            .systemCommissionUp(DEFAULT_SYSTEM_COMMISSION)
            .systemFee(DEFAULT_SYSTEM_FEE)
            .active(DEFAULT_ACTIVE)
            .recipient(recipient)
            .agent(agent)
            //
            ;
        return agentRecipient;
    }

    private static final String DEFAULT_SHORT_NAME = "AAAAAAAAAA";

    private static final String DEFAULT_API_KEY = "AAAAAAAAAA";
    private static final String UPDATED_API_KEY = "BBBBBBBBBB";

    private static final Long DEFAULT_TAXNR = 1L;
    private static final Long UPDATED_TAXNR = 2L;

    private static final String DEFAULT_IP = "AAAAAAAAAA";
    private static final String UPDATED_IP = "BBBBBBBBBB";

    private static final byte[] DEFAULT_THUMBNAIL = TestUtil.createByteArray(1, "0");
    private static final byte[] UPDATED_THUMBNAIL = TestUtil.createByteArray(2, "1");
    private static final String DEFAULT_THUMBNAIL_CONTENT_TYPE = "image/jpg";
    private static final String UPDATED_THUMBNAIL_CONTENT_TYPE = "image/png";

    private static final byte[] DEFAULT_LOGO = TestUtil.createByteArray(1, "0");
    private static final byte[] UPDATED_LOGO = TestUtil.createByteArray(2, "1");
    private static final String DEFAULT_LOGO_CONTENT_TYPE = "image/jpg";
    private static final String UPDATED_LOGO_CONTENT_TYPE = "image/png";

    private static final Integer DEFAULT_ORD = 1;
    private static final Integer UPDATED_ORD = 2;

    private static final Boolean DEFAULT_ACTIVE = true;
    private static final Boolean UPDATED_ACTIVE = false;

    private static final String DEFAULT_SERVICE_ID = "AAAAAAAAAA";
    private static final String UPDATED_SERVICE_ID = "BBBBBBBBBB";

    private static final byte[] DEFAULT_THUMBNAIL_FILE = TestUtil.createByteArray(1, "0");
    private static final byte[] UPDATED_THUMBNAIL_FILE = TestUtil.createByteArray(2, "1");
    private static final String DEFAULT_THUMBNAIL_FILE_CONTENT_TYPE = "image/jpg";
    private static final String UPDATED_THUMBNAIL_FILE_CONTENT_TYPE = "image/png";

    private static final byte[] DEFAULT_LOGO_FILE = TestUtil.createByteArray(1, "0");
    private static final byte[] UPDATED_LOGO_FILE = TestUtil.createByteArray(2, "1");
    private static final String DEFAULT_LOGO_FILE_CONTENT_TYPE = "image/jpg";
    private static final String UPDATED_LOGO_FILE_CONTENT_TYPE = "image/png";

    private static final Integer DEFAULT_AMOUNT_RATIO = 1;
    private static final Integer UPDATED_AMOUNT_RATIO = 2;

    private static final Long DEFAULT_MIN_AMOUNT = 1L;
    private static final Long UPDATED_MIN_AMOUNT = 2L;

    private static final Long DEFAULT_MAX_AMOUNT = 1L;
    private static final Long UPDATED_MAX_AMOUNT = 2L;

    private static final Double DEFAULT_COMMISSION = 1D;
    private static final Double UPDATED_COMMISSION = 2D;

    private static final Double DEFAULT_FEE = 1D;
    private static final Double UPDATED_FEE = 2D;

    private static final Double DEFAULT_SYSTEM_COMMISSION = 1D;
    private static final Double UPDATED_SYSTEM_COMMISSION = 2D;

    private static final Double DEFAULT_SYSTEM_FEE = 1D;
    private static final Double UPDATED_SYSTEM_FEE = 2D;

}
