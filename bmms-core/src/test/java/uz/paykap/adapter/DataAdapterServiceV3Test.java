package uz.paykap.adapter;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import uz.devops.bm.paynet.config.PaynetConstants;
import uz.javlon.commons.base.Result;
import uz.paykap.IntegrationTest;

import java.util.ArrayList;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@IntegrationTest
public class DataAdapterServiceV3Test {

    @Autowired
    private DataAdapterServiceV3 dataAdapterServiceV3;

    @Autowired
    private DataAdapterDelegate dataAdapterDelegate;

    @Test
    @Transactional
    public void sync() {

        ArrayList<DataAdapterResponse> allAdapters2 = dataAdapterServiceV3.getAllAdapters();

        Assertions.assertThat(allAdapters2).isNotEmpty();

        for (DataAdapterResponse dataAdapterResponse : allAdapters2) {

            // ignore paynet json2 adapter version
            if (Objects.equals(dataAdapterResponse.getName(), PaynetConstants.PAYNET_JSON_DATA_ADAPTER)) {
                continue;
            }

            Result sync = dataAdapterServiceV3.sync(dataAdapterResponse.getName());
            Assertions.assertThat(sync).isNotNull();
            Assertions.assertThat(sync.isSuccess()).isTrue();
        }

    }

    @Test
    @Transactional
    public void resync() {

//        int categoriesSize = dataAdapterDelegate.getCategoryRepository().findAll().size();
//        int recipientsSize = dataAdapterDelegate.getRecipientRepository().findAll().size();
//        int fieldsSize = dataAdapterDelegate.getFieldRepository().findAll().size();
//        int fieldValuesSize = dataAdapterDelegate.getFieldValueRepository().findAll().size();
//        int prefixesSize = dataAdapterDelegate.getPrefixRepository().findAll().size();
//        int responseFieldSize = dataAdapterDelegate.getResponseFieldRepository().findAll().size();
//        int i18nsSize = dataAdapterDelegate.getPrefixRepository().findAll().size();

        ArrayList<DataAdapterResponse> allAdapters2 = dataAdapterServiceV3.getAllAdapters();

        Set<String> adapterNames = allAdapters2.stream().map(DataAdapterResponse::getName).collect(Collectors.toSet());

        Assertions.assertThat(allAdapters2).isNotEmpty();
        Assertions.assertThat(adapterNames).contains(PaynetConstants.PAYNET_RPC_DATA_ADAPTER);

        for (DataAdapterResponse dataAdapterResponse : allAdapters2) {

            // ignore paynet json2 adapter version
            if (Objects.equals(dataAdapterResponse.getName(), PaynetConstants.PAYNET_RPC_DATA_ADAPTER)) {
                Result sync = dataAdapterServiceV3.sync(dataAdapterResponse.getName());
                Assertions.assertThat(sync).isNotNull();
                Assertions.assertThat(sync.isSuccess()).isTrue();

                int categoriesSize = dataAdapterDelegate.getCategoryRepository().findAll().size();
                int recipientsSize = dataAdapterDelegate.getRecipientRepository().findAll().size();
                int fieldsSize = dataAdapterDelegate.getFieldRepository().findAll().size();
                int fieldValuesSize = dataAdapterDelegate.getFieldValueRepository().findAll().size();
                int prefixesSize = dataAdapterDelegate.getPrefixRepository().findAll().size();
                int responseFieldSize = dataAdapterDelegate.getResponseFieldRepository().findAll().size();

                sync = dataAdapterServiceV3.sync(dataAdapterResponse.getName());
                Assertions.assertThat(sync).isNotNull();
                Assertions.assertThat(sync.isSuccess()).isTrue();

                assertThat(dataAdapterDelegate.getCategoryRepository().findAll()).hasSize(categoriesSize);
                assertThat(dataAdapterDelegate.getRecipientRepository().findAll()).hasSize(recipientsSize);
                assertThat(dataAdapterDelegate.getFieldRepository().findAll()).hasSize(fieldsSize);
                assertThat(dataAdapterDelegate.getFieldValueRepository().findAll()).hasSize(fieldValuesSize);
                assertThat(dataAdapterDelegate.getResponseFieldRepository().findAll()).hasSize(responseFieldSize);
                assertThat(dataAdapterDelegate.getPrefixRepository().findAll()).hasSize(prefixesSize);
            }

        }
    }

}
